/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4cheri.data;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DatasetSerializer;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.util.UIDGenerator;

import org.xml.sax.ContentHandler;
import javax.xml.transform.Transformer;

/**
 *  Implementation of <code>Dataset</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @created  March, 2002
 * @version  $Revision$
 * @see  "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
abstract class BaseDatasetImpl extends DcmObjectImpl implements Dataset
{

    private FileMetaInfo fmi = null;

    private int[] grTags = new int[8];
    private int[] grLens = new int[8];
    private int grCount = 0;
    /**  Description of the Field */
    protected int totLen = 0;

    private static SAXTransformerFactory tfFactory;
    private static Templates templates;
    private static TagDictionary tagDictionary;


    private static SAXTransformerFactory getTransformerFactory()
    {
        if (tfFactory == null) {
            tfFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        }
        return tfFactory;
    }


    static class ConfigurationError extends Error
    {
        ConfigurationError(String msg, Exception x)
        {
            super(msg, x);
        }
    }


    private static Templates getTemplates()
    {
        if (templates == null) {
            InputStream in = BaseDatasetImpl.class.getResourceAsStream("dump.xsl");
            try {
                templates = getTransformerFactory().newTemplates(new StreamSource(in));
            } catch (Exception e) {
                throw new ConfigurationError("Failed to load/compile dump.xsl", e);
            }
        }
        return templates;
    }


    private static TagDictionary getTagDictionary()
    {
        if (tagDictionary == null) {
            DictionaryFactory df = DictionaryFactory.getInstance();
            tagDictionary = df.getDefaultTagDictionary();
        }
        return tagDictionary;
    }


    /**
     *  Sets the fileMetaInfo attribute of the BaseDatasetImpl object
     *
     * @param  fmi The new fileMetaInfo value
     * @return  Description of the Return Value
     */
    public final Dataset setFileMetaInfo(FileMetaInfo fmi)
    {
        this.fmi = fmi;
        return this;
    }


    /**
     *  Gets the fileMetaInfo attribute of the BaseDatasetImpl object
     *
     * @return  The fileMetaInfo value
     */
    public FileMetaInfo getFileMetaInfo()
    {
        return fmi;
    }


    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     */
    public String toString()
    {
        return "[" + size() + " elements]";
    }


    private int[] ensureCapacity(int[] old, int n)
    {
        if (n <= old.length) {
            return old;
        }
        int[] retval = new int[old.length << 1];
        System.arraycopy(old, 0, retval, 0, old.length);
        return retval;
    }


    /**
     *  Description of the Method
     *
     * @param  param Description of the Parameter
     * @return  Description of the Return Value
     */
    public int calcLength(DcmEncodeParam param)
    {
        totLen = 0;
        grCount = 0;

        int curGrTag;

        int prevGrTag = -1;
        for (Iterator iter = iterator(); iter.hasNext(); ) {
            DcmElement el = (DcmElement) iter.next();
            curGrTag = el.tag() & 0xffff0000;
            if (curGrTag != prevGrTag) {
                grCount++;
                grTags = ensureCapacity(grTags, grCount + 1);
                grLens = ensureCapacity(grLens, grCount + 1);
                grTags[grCount - 1] = prevGrTag = curGrTag;
                grLens[grCount - 1] = 0;
            }
            grLens[grCount - 1] +=
                    (param.explicitVR && !VRs.isLengthField16Bit(el.vr())) ? 12 : 8;
            if (el instanceof ValueElement) {
                grLens[grCount - 1] += el.length();
            } else if (el instanceof FragmentElement) {
                grLens[grCount - 1] += ((FragmentElement) el).calcLength();
            } else {
                grLens[grCount - 1] += ((SQElement) el).calcLength(param);
            }
        }
        grTags[grCount] = -1;
        if (!param.skipGroupLen) {
            totLen += grCount * 12;
        }
        for (int i = 0; i < grCount; ++i) {
            totLen += grLens[i];
        }
        return totLen;
    }


    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     */
    public int length()
    {
        return totLen;
    }


    /**  Description of the Method */
    public void clear()
    {
        super.clear();
        totLen = 0;
    }


    /**
     *  Description of the Method
     *
     * @param  handler Description of the Parameter
     * @param  param Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void writeDataset(DcmHandler handler, DcmEncodeParam param)
        throws IOException
    {
        if (!(param.skipGroupLen && param.undefItemLen && param.undefSeqLen)) {
            calcLength(param);
        }
        handler.startDataset();
        handler.setDcmDecodeParam(param);
        doWrite(handler, param);
        handler.endDataset();
    }


    private void doWrite(DcmHandler handler, DcmEncodeParam param)
        throws IOException
    {
        int grIndex = 0;
        for (Iterator iter = iterator(); iter.hasNext(); ) {
            DcmElement el = (DcmElement) iter.next();
            if (!param.skipGroupLen
                     && grTags[grIndex] == (el.tag() & 0xffff0000)) {
                byte[] b4 = new byte[4];
                ByteBuffer.wrap(b4).order(param.byteOrder)
                        .putInt(grLens[grIndex]);
                handler.startElement(grTags[grIndex], VRs.UL,
                        el.getStreamPosition());
                handler.value(b4, 0, 4);
                handler.endElement();
                ++grIndex;
            }
            if (el instanceof SQElement) {
                int len = param.undefSeqLen ? -1 : el.length();
                handler.startElement(el.tag(), VRs.SQ, el.getStreamPosition());
                handler.startSequence(len);
                for (int j = 0, m = el.vm(); j < m; ) {
                    BaseDatasetImpl ds = (BaseDatasetImpl) el.getItem(j);
                    int itemlen = param.undefItemLen ? -1 : ds.length();
                    handler.startItem(++j, ds.getItemOffset(), itemlen);
                    ds.doWrite(handler, param);
                    handler.endItem(itemlen);
                }
                handler.endSequence(len);
                handler.endElement();
            } else if (el instanceof FragmentElement) {
                long offset = el.getStreamPosition();
                handler.startElement(el.tag(), el.vr(), offset);
                handler.startSequence(-1);
                if (offset != -1L) {
                    offset += 12;
                }
                for (int j = 0, m = el.vm(); j < m; ) {
                    ByteBuffer bb = el.getDataFragment(j, param.byteOrder);
                    handler.fragment(++j, offset, bb.array(), bb.arrayOffset(),
                            bb.limit());
                    if (offset != -1L) {
                        offset += (bb.limit() + 9) & (~1);
                    }
                }
                handler.endSequence(-1);
                handler.endElement();
            } else {
                int len = el.length();
                handler.startElement(el.tag(), el.vr(), el.getStreamPosition());
                ByteBuffer bb = el.getByteBuffer(param.byteOrder);
                handler.value(bb.array(), bb.arrayOffset(), bb.limit());
                handler.endElement();
            }
        }
    }


    /**
     *  Description of the Method
     *
     * @param  out Description of the Parameter
     * @param  param Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void writeDataset(OutputStream out, DcmEncodeParam param)
        throws IOException
    {
        if (param == null) {
            param = DcmDecodeParam.IVR_LE;
        }
        writeDataset(new DcmStreamHandlerImpl(param.deflated ?
                new DeflaterOutputStream(out) : out),
                param);
    }


    /**
     *  Description of the Method
     *
     * @param  out Description of the Parameter
     * @param  param Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void writeFile(OutputStream out, DcmEncodeParam param)
        throws IOException
    {
        FileMetaInfo fmi = getFileMetaInfo();
        if (fmi != null) {
            param = checkCompatibility(fmi, param);
            fmi.write(out);
        }
        writeDataset(out, param);
    }


    /**
     *  Description of the Method
     *
     * @param  out Description of the Parameter
     * @param  param Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void writeDataset(ImageOutputStream out, DcmEncodeParam param)
        throws IOException
    {
        if (param == null) {
            param = DcmDecodeParam.IVR_LE;
        }
        writeDataset(param.deflated
                 ? new DcmStreamHandlerImpl(new DeflaterOutputStream(
                new OutputStreamAdapter(out)))
                 : new DcmStreamHandlerImpl(out),
                param);
    }


    private DcmEncodeParam checkCompatibility(FileMetaInfo fmi,
            DcmEncodeParam param)
    {
        DcmEncodeParam fmiParam = DcmDecodeParam.valueOf(fmi.getTransferSyntaxUID());
        if (param == null) {
            return fmiParam;
        }
        if (param.byteOrder == fmiParam.byteOrder
                 && param.explicitVR == fmiParam.explicitVR
                 && param.encapsulated == fmiParam.encapsulated
                 && param.deflated == fmiParam.deflated) {
            return param;
        }
        throw new IllegalArgumentException("param: " + param
                 + " does not match with " + fmi);
    }


    /**
     *  Description of the Method
     *
     * @param  out Description of the Parameter
     * @param  param Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void writeFile(ImageOutputStream out, DcmEncodeParam param)
        throws IOException
    {
        FileMetaInfo fmi = getFileMetaInfo();
        if (fmi != null) {
            param = checkCompatibility(fmi, param);
            fmi.write(out);
        }
        writeDataset(out, param);
    }


    /**
     *  Description of the Method
     *
     * @param  ch Description of the Parameter
     * @param  dict Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void writeDataset(ContentHandler ch, TagDictionary dict)
        throws IOException
    {
        writeDataset(new DcmHandlerAdapter(ch, dict), DcmDecodeParam.EVR_LE);
    }


    /**
     *  Description of the Method
     *
     * @param  out Description of the Parameter
     * @param  param Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void dumpDataset(OutputStream out, Map param)
        throws IOException
    {
        dumpDataset(new StreamResult(out), param);
    }


    /**
     *  Description of the Method
     *
     * @param  w Description of the Parameter
     * @param  param Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void dumpDataset(Writer w, Map param)
        throws IOException
    {
        dumpDataset(new StreamResult(w), param);
    }


    private void dumpDataset(Result result, Map param)
        throws IOException
    {
        TransformerHandler th;
        try {
            th = getTransformerFactory().newTransformerHandler(getTemplates());
            if (param != null) {
                Transformer t = th.getTransformer();
                for (Iterator it = param.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry e = (Map.Entry) it.next();
                    t.setParameter((String) e.getKey(), e.getValue());
                }
            }
        } catch (Exception e) {
            throw new ConfigurationError("Failed to initialize XSLT", e);
        }
        th.setResult(result);
        writeDataset(th, getTagDictionary());
    }


    /**
     *  Description of the Method
     *
     * @param  ch Description of the Parameter
     * @param  dict Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void writeFile(ContentHandler ch, TagDictionary dict)
        throws IOException
    {
        DcmHandlerAdapter xml = new DcmHandlerAdapter(ch, dict);
        xml.startDcmFile();
        FileMetaInfo fmi = getFileMetaInfo();
        if (fmi != null) {
            fmi.write(xml);
        }
        writeDataset(xml, DcmDecodeParam.EVR_LE);
        xml.endDcmFile();
    }


    /**
     *  Description of the Method
     *
     * @param  fromTag Description of the Parameter
     * @param  toTag Description of the Parameter
     * @return  Description of the Return Value
     */
    public Dataset subSet(int fromTag, int toTag)
    {
        return new FilterDataset.Segment(this, fromTag, toTag);
    }


    /**
     *  Description of the Method
     *
     * @param  filter Description of the Parameter
     * @return  Description of the Return Value
     */
    public Dataset subSet(Dataset filter)
    {
        return new FilterDataset.Selection(this, filter);
    }


    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     * @exception  java.io.ObjectStreamException Description of the Exception
     */
    protected Object writeReplace()
        throws java.io.ObjectStreamException
    {
        return new DatasetSerializer(this);
    }

    private static final ColorSpace sRGB =
       ColorSpace.getInstance(ColorSpace.CS_sRGB);
   
    private static final ImageTypeSpecifier RGB_PLANE =
       ImageTypeSpecifier.createBanded(sRGB,
          new int[]{0,1,2},
          new int[]{0,0,0},
          DataBuffer.TYPE_BYTE,
          false,false);
   
    private static final ImageTypeSpecifier RGB_PIXEL =
       ImageTypeSpecifier.createInterleaved(sRGB,
          new int[]{0,1,2},
          DataBuffer.TYPE_BYTE,
          false,false);
    
    public BufferedImage toBufferedImage()
    {
        int width = getInt(Tags.Columns, -1);
        int height = getInt(Tags.Rows, -1);
        
        if (width == -1 || height == -1)
            throw new IllegalStateException("Illegal width/height: width = "
                                            + width + ", height = " + height);
        
        int bitsAllocd = getInt(Tags.BitsAllocated, -1);
        int bitsStored = getInt(Tags.BitsStored, -1);
        int highBit = getInt(Tags.HighBit, -1);
        int pixelRep = getInt(Tags.PixelRepresentation, -1);
        String pmi = getString(Tags.PhotometricInterpretation, null);
        int spp = getInt(Tags.SamplesPerPixel, -1);
        int planarConf = getInt(Tags.PlanarConfiguration, -1);
        ByteBuffer pixelData = getByteBuffer(Tags.PixelData);
        
        //TODO better error checking!
        if (bitsAllocd == -1 || bitsStored == -1 || highBit == -1
            || pixelRep == -1 || pmi == null || spp == -1
            || (planarConf == -1 && spp > 1) || pixelData == null) {
                throw new IllegalStateException("Invalid Image Pixel Module!");
        }
        
        boolean signed = (pixelRep == 1);
        if (planarConf == -1)
            planarConf = 1;
        //TODO check for other types here
        int dataBufType = (bitsAllocd <= 8) ? DataBuffer.TYPE_BYTE
                                            : DataBuffer.TYPE_USHORT;
        BufferedImage bi;
        
        //create BufferedImage
        if (pmi.equals("RGB")) {
            switch (planarConf) {
                case 0:
                    bi = RGB_PIXEL.createBufferedImage(width, height);
                    break;
                case 1:
                    bi = RGB_PLANE.createBufferedImage(width, height);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Planar Configuration");
            }
        }
        else if (pmi.equals("MONOCHROME1") || pmi.equals("MONOCHROME2")
                 || pmi.equals("PALETTE COLOR")) {
            ColorModelFactory cmFactory = ColorModelFactory.getInstance();
            bi = new ImageTypeSpecifier(
                cmFactory.getColorModel(cmFactory.makeParam(this)),
                new PixelInterleavedSampleModel(dataBufType, 1, 1, 1, 1,
                    new int[]{0})).createBufferedImage(width, height);
        }
        else { //unsupported photometric interpretation
            throw new UnsupportedOperationException(
                "Unsupported Photometric Interpretation: " + pmi);
        }
        
        //read pixeldata into the BufferedImage
        DataBuffer dataBuf = bi.getRaster().getDataBuffer();
        Object dest = null;
        if (planarConf == 0) { //read single bank for PixelInterleavedModel
            switch (dataBufType) {
                case DataBuffer.TYPE_BYTE:
                    dest = ((DataBufferByte)dataBuf).getData();
                    break;
                case DataBuffer.TYPE_USHORT:
                    dest = ((DataBufferUShort)dataBuf).getData();
                    break;
            }
            readPixelData(pixelData, dest, dataBufType, bitsAllocd, bitsStored,
                          highBit, spp, width, height);
        }
        else {
            for (int i = 0; i < spp; i++) { //read each bank of BandedSampleModel seperately
                switch (dataBufType) {
                    case DataBuffer.TYPE_BYTE:
                        dest = ((DataBufferByte)dataBuf).getData(i);
                        break;
                    case DataBuffer.TYPE_USHORT:
                        dest = ((DataBufferUShort)dataBuf).getData(i);
                        break;
                }
                readPixelData(pixelData, dest, dataBufType, bitsAllocd, bitsStored,
                              highBit, 1, width, height);
            }
        }
        
        return bi;
    }
    
    private void readPixelData(ByteBuffer pixelData,
                               Object dest, int dataType,
                               int ba, int bs, int hb,
                               int spp, int width, int height)
    {
        int size = width * height * spp;
        int i = 0;
        
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                byte[] bufByte = (byte[])dest;
                while (i < size) {
                    bufByte[i++] = pixelData.get();
                }
                break;
            case DataBuffer.TYPE_USHORT:
                short[] bufUShort = (short[])dest;
                while (i < size) {
                    bufUShort[i++] = pixelData.getShort();
                }
                break;
        }
    }
    
    public Dataset putBufferedImage(BufferedImage bi)
    {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        UIDGenerator uidGen = UIDGenerator.getInstance();
        Date now = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmmss.SSS");
        
        ds.putCS(Tags.ConversionType, "WSD");                               // Type 1
        ds.putCS(Tags.Modality, "OT");                                      // Type 3
        ds.putIS(Tags.InstanceNumber, "1");                                 // Type 2
        ds.putDA(Tags.DateOfSecondaryCapture, dateFormatter.format(now));   // Type 3
        ds.putTM(Tags.TimeOfSecondaryCapture, timeFormatter.format(now));   // Type 3
        ds.putUI(Tags.SOPClassUID, UIDs.SecondaryCaptureImageStorage);      // Type 1
        ds.putUI(Tags.SOPInstanceUID, uidGen.createUID());                  // Type 1
        
        return ds;
    }
}

