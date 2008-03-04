/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che2.imageioimpl.plugins.dcm;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;

import junit.framework.TestCase;

/**
 * Tests the DICOM Image Writer
 * 
 * @author bwallace
 */
public class DicomImageWriterTest extends TestCase {

    static {
        ImageIO.scanForPlugins();
    };

    static final boolean eraseDicom = false;

    /**
     * Test to see that we can find an image writer, and that it is of the
     * correct class. If multiple DICOM writers are available, then the dcm4che2
     * must currently be the first one.
     * 
     */
    public void testFindDicomImageWriter() {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("DICOM")
                .next();
        assert writer != null;
        assert writer instanceof DicomImageWriter;
    }

    /**
     * Tests single frame lossless writing.
     */
    public void testSingleFrameLossless() throws IOException {
        String name = "ct-write";
        DicomImageReader reader = createImageReader("ct.dcm");
        BufferedImage bi = readRawBufferedImage(reader, 0);
        DicomStreamMetaData newMeta = copyMeta(reader, UID.JPEGLSLossless);
        ImageInputStream iis = writeImage(newMeta, bi, name);
        DicomImageReader readerNew = createImageReader(iis);
        BufferedImage biNew = readRawBufferedImage(readerNew, 0);
        ImageDiff diff = new ImageDiff(bi, biNew, name, 0);
        assert diff.getMaxDiff() == 0;
    }

    /** Returns an input stream containing the written data */
    private ImageInputStream writeImage(DicomStreamMetaData newMeta,
            BufferedImage bi, String name) throws IOException {
        File f = new File(name + ".dcm");
        if (f.exists())
            f.delete();
        ImageOutputStream imageOutput = new FileImageOutputStream(f);
        DicomImageWriter writer = (DicomImageWriter) new DicomImageWriterSpi()
                .createWriterInstance();
        IIOImage iioimage = new IIOImage(bi, null, null);
        writer.setOutput(imageOutput);
        writer.write(newMeta, iioimage, null);
        imageOutput.close();
        return new FileImageInputStream(f);
    }

    private DicomStreamMetaData copyMeta(DicomImageReader reader, String tsuid)
            throws IOException {
        DicomStreamMetaData oldMeta = (DicomStreamMetaData) reader
                .getStreamMetadata();
        DicomStreamMetaData ret = new DicomStreamMetaData();
        DicomObject ds = oldMeta.getDicomObject();
        DicomObject newDs = new BasicDicomObject();
        ds.copyTo(newDs);
        newDs.putString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        ret.setDicomObject(newDs);
        return ret;
    }

    public static BufferedImage readRawBufferedImage(DicomImageReader reader,
            int frame) throws IOException {
        WritableRaster raster = (WritableRaster) reader.readRaster(0, null);
        DicomObject ds = ((DicomStreamMetaData) reader.getStreamMetadata())
                .getDicomObject();
        ColorModel cm = ColorModelFactory.createColorModel(ds);
        return new BufferedImage(cm, raster, false, null);
    }

    /**
     * Return an image reader on the given resource Example resources are:
     * mr.dcm ct.dcm cr-multiframe.dcm cr-monochrome1.dcm mlut_*.dcm
     */
    public static DicomImageReader createImageReader(String resource)
            throws IOException {
        InputStream is = DicomImageWriterTest.class
                .getResourceAsStream(resource);
        assert is != null;
        return createImageReader(ImageIO.createImageInputStream(is));
    }

    /** Returns an image reader on the given filename */
    public static DicomImageReader createImageReader(ImageInputStream is)
            throws IOException {
        assert is != null;
        DicomImageReaderSpi spi = new DicomImageReaderSpi();
        DicomImageReader reader = (DicomImageReader) spi
                .createReaderInstance(null);
        reader.setInput(is, true);
        return reader;
    }

}
