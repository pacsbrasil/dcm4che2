/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DatasetSerializer;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.VRs;
import org.dcm4che.dict.TagDictionary;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.stream.ImageOutputStream;

import org.xml.sax.ContentHandler;

/** Implementation of <code>Dataset</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
abstract class BaseDatasetImpl extends DcmObjectImpl implements Dataset {
   
   private FileMetaInfo fmi = null;
   
   private int[] grTags = new int[8];
   private int[] grLens = new int[8];
   private int grCount = 0;
   protected int totLen = 0;
   
   public final Dataset setFileMetaInfo(FileMetaInfo fmi) {
      this.fmi = fmi;
      return this;
   }
   
   public FileMetaInfo getFileMetaInfo() {
      return fmi;
   }
   
   public String toString() {
      return "[" + size() + " elements]";
   }
   
   private int[] ensureCapacity(int[] old, int n) {
      if (n <= old.length) {
         return old;
      }
      int[] retval = new int[old.length << 1];
      System.arraycopy(old, 0, retval, 0, old.length);
      return retval;
   }
   
   public int calcLength(DcmEncodeParam param) {
      totLen = 0;
      grCount = 0;
      
      int curGrTag, prevGrTag = -1;
      for (Iterator iter = iterator(); iter.hasNext();) {
         DcmElement el = (DcmElement)iter.next();
         curGrTag = el.tag() & 0xffff0000;
         if (curGrTag != prevGrTag) {
            grCount++;
            grTags = ensureCapacity(grTags, grCount + 1);
            grLens = ensureCapacity(grLens, grCount + 1);
            grTags[grCount-1] = prevGrTag = curGrTag;
            grLens[grCount-1] = 0;
         }
         grLens[grCount-1] +=
         (param.explicitVR && !VRs.isLengthField16Bit(el.vr())) ? 12 : 8;
         if (el instanceof ValueElement)
            grLens[grCount-1] += el.length();
         else if (el instanceof FragmentElement)
            grLens[grCount-1] += ((FragmentElement)el).calcLength();
         else
            grLens[grCount-1] += ((SQElement)el).calcLength(param);
      }
      grTags[grCount] = -1;
      if (!param.skipGroupLen)
         totLen += grCount * 12;
      for (int i = 0; i < grCount; ++i) {
         totLen += grLens[i];
      }
      return totLen;
   }
   
   public int length() {
      return totLen;
   }
   
   public void clear() {
      super.clear();
      totLen = 0;
   }
   
   
   public void writeDataset(DcmHandler handler, DcmEncodeParam param)
   throws IOException {
      if (!(param.skipGroupLen && param.undefItemLen && param.undefSeqLen))
         calcLength(param);
      handler.startDataset();
      handler.setDcmDecodeParam(param);
      doWrite(handler, param);
      handler.endDataset();
   }
   
   private void doWrite(DcmHandler handler, DcmEncodeParam param)
   throws IOException {
      int grIndex = 0;
      for (Iterator iter = iterator(); iter.hasNext();) {
         DcmElement el = (DcmElement)iter.next();
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
            for (int j = 0, m = el.vm(); j < m;) {
               BaseDatasetImpl ds = (BaseDatasetImpl)el.getItem(j);
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
            for (int j = 0, m = el.vm(); j < m;) {
               ByteBuffer bb = el.getDataFragment(j, param.byteOrder);
               handler.fragment(++j, offset, bb.array(), bb.arrayOffset(),
               bb.limit());
               if (offset != -1L) {
                  offset += (bb.limit() + 9)&(~1);
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
   
   public void writeDataset(OutputStream out, DcmEncodeParam param)
   throws IOException {
      if (param == null) {
         param = DcmDecodeParam.IVR_LE;
      }
      writeDataset(new DcmStreamHandlerImpl(param.deflated ?
            new DeflaterOutputStream(out) : out),
      param);
   }
   
   public void writeFile(OutputStream out, DcmEncodeParam param)
   throws IOException {
      FileMetaInfo fmi = getFileMetaInfo();
      if (fmi != null) {
         fmi.write(out);
         if (param == null) {
            param = DcmDecodeParam.valueOf(fmi.getTransferSyntaxUID());
         }
      }
      writeDataset(out , param);
   }
   
   public void writeDataset(ImageOutputStream out, DcmEncodeParam param)
   throws IOException {
      if (param == null) {
         param = DcmDecodeParam.IVR_LE;
      }
      writeDataset(param.deflated
            ? new DcmStreamHandlerImpl(new DeflaterOutputStream(
               new OutputStreamAdapter(out)))
            : new DcmStreamHandlerImpl(out),
         param);
   }
   
   public void writeFile(ImageOutputStream out, DcmEncodeParam param)
   throws IOException {
      FileMetaInfo fmi = getFileMetaInfo();
      if (fmi != null) {
         fmi.write(out);
         if (param == null) {
            param = DcmDecodeParam.valueOf(fmi.getTransferSyntaxUID());
         }
      }
      writeDataset(out, param);
   }
   
   public void writeDataset(ContentHandler ch, TagDictionary dict)
   throws IOException {
      writeDataset(new DcmHandlerAdapter(ch, dict), DcmDecodeParam.EVR_LE);
   }
   
   public void writeFile(ContentHandler ch, TagDictionary dict)
   throws IOException {
      DcmHandlerAdapter xml = new DcmHandlerAdapter(ch, dict);
      xml.startDcmFile();
      FileMetaInfo fmi = getFileMetaInfo();
      if (fmi != null) {
         fmi.write(xml);
      }
      writeDataset(xml, DcmDecodeParam.EVR_LE);
      xml.endDcmFile();
   }
   
   public Dataset subSet(int fromTag, int toTag) {
      return new FilterDataset.Segment(this, fromTag, toTag);
   }
   
   public Dataset subSet(Dataset filter) {
      return new FilterDataset.Selection(this, filter);
   }
   
   protected Object writeReplace() throws java.io.ObjectStreamException {
      return new DatasetSerializer(this);
   }
}