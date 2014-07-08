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
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
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
package in.raster.mayam.delegates;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.OverlayUtils;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class DicomImageReader {

    public static BufferedImage readDicomFile(File dicomFile) {

        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("DICOM").next();
        BufferedImage tempImage = null;
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(dicomFile);
            reader.setInput(iis, false);
            tempImage = reader.read(0);
            DicomObject obj = new DicomInputStream(dicomFile).readDicomObject();
            String overlayData = obj.getString(Tag.OverlayData);
            if (overlayData != null && overlayData.length() > 0) {
                tempImage = combineImages(tempImage, OverlayUtils.extractOverlay(obj, Tag.OverlayData, reader, "FFFFFF"));
            }
        } catch (IOException ex) {
            Logger.getLogger(DicomImageReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tempImage;
    }

    private static BufferedImage combineImages(BufferedImage tempImage, BufferedImage overlayImg) {
        Graphics2D g2d = (Graphics2D) tempImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.drawImage(overlayImg, 0, 0, null);
        g2d.dispose();
        return tempImage;
    }

    //Export
    public static BufferedImage[] readMultiFrames(File dicomFile) {
        BufferedImage[] bufferedImages = null;
        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("DICOM").next();
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(dicomFile);
            reader.setInput(iis, false);
            if (reader.getNumImages(true) > 0) {
                bufferedImages = new BufferedImage[reader.getNumImages(true)];
                for (int i = 0; i < bufferedImages.length; i++) {
                    bufferedImages[i] = reader.read(i);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DicomImageReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bufferedImages;
    }
}
