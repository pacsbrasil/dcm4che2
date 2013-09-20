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

import in.raster.mayam.context.ApplicationContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.OverlayUtils;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ConstructThumbnails {

    String studyInstanceUid, seriesInstanceUid, sopInstanceUid;
    File parent;
    BufferedImage currentBufferedImage = null;
    int instanceNumber, maxThumbwidth = 75;

    public ConstructThumbnails(String studyInstanceUid, String seriesInstanceUid) {
        this.studyInstanceUid = studyInstanceUid;
        this.seriesInstanceUid = seriesInstanceUid;
        createThumbnails();
    }

    private synchronized void createThumbnails() {
        ArrayList<String> imageLocations = ApplicationContext.databaseRef.getLocationsBasedOnSeries(studyInstanceUid, seriesInstanceUid);
        try {
            File seriesDir = new File(imageLocations.get(0)).getParentFile();
            parent = new File(seriesDir + File.separator + "Thumbnails");
            if (!parent.exists()) {
                parent.mkdirs();
            }
        } catch (IndexOutOfBoundsException exception) {
            //ignore
        }

        for (int j = 0; j < imageLocations.size(); j++) {
            BufferedImage imageToWrite = createThumbnailImages(new File(imageLocations.get(j)));
            String fileName;
            fileName = sopInstanceUid;

            try {
                ImageIO.write(imageToWrite, "jpeg", new File(parent, fileName));
            } catch (IOException ex) {
                Logger.getLogger(ConstructThumbnails.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ConcurrentModificationException cme) {
            } catch (IllegalArgumentException ile) {
            } catch (NullPointerException npe) {
            }
            ApplicationContext.databaseRef.updateThumbnailStatus(studyInstanceUid, seriesInstanceUid, sopInstanceUid);
        }

    }

    /*
     * Will reads the dicom file and constructs the Buffered Image
     *
     */
    private BufferedImage createThumbnailImages(File file) {
        Image loadedImage = null;
        int width = 60, height = 60;
        BufferedImage imageToReturn = null;
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            try {
                Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
                ImageReader reader = (ImageReader) iter.next();
                reader.setInput(iis, false);
                FileInputStream fis = new FileInputStream(file);
                DicomInputStream dis = new DicomInputStream(fis);
                DicomObject obj = dis.readDicomObject();
                instanceNumber = obj.getInt(Tags.InstanceNumber);
                if (reader.getNumImages(true) > 0) {
                    currentBufferedImage = reader.read(0);
                    String overlayData = obj.getString(Tag.OverlayData);
                    if (overlayData != null && overlayData.length() > 0) {
                        BufferedImage overlayImg = OverlayUtils.extractOverlay(obj, Tag.OverlayData, reader, "FFFFFF");
                        combineImages(currentBufferedImage, overlayImg);
                    }
                }
                int rows = obj.getInt(Tags.Rows);
                int cols = obj.getInt(Tags.Columns);
                if (rows != 0 && cols != 0) {
                    if (rows < cols) {
                        height = Math.min(rows, cols) * maxThumbwidth / Math.max(rows, cols);
                        width = maxThumbwidth;
                    } else {
                        width = Math.min(rows, cols) * maxThumbwidth / Math.max(rows, cols);
                        height = maxThumbwidth;
                    }
                }
                sopInstanceUid = obj.getString(Tags.SOPInstanceUID);

                if (reader.getNumImages(true) > 0) {
                    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                    imageToReturn = gc.createCompatibleImage(width, height);
                    Graphics2D g2 = imageToReturn.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(currentBufferedImage, 0, 0, imageToReturn.getWidth(), imageToReturn.getHeight(), null);
                    g2.dispose();
                }

                if (obj.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                    loadedImage = new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/pdficon.jpeg")).getImage();
                    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                    BufferedImage temp = gc.createCompatibleImage(loadedImage.getWidth(null), loadedImage.getHeight(null));
                    Graphics2D g2 = temp.createGraphics();
                    g2.drawImage(loadedImage, 0, 0, null);
                    imageToReturn = temp;
                }
            } catch (Exception e) {
            }
        } catch (IOException ex) {
            Logger.getLogger(ConstructThumbnails.class.getName()).log(Level.SEVERE, null, ex);
        }
        return imageToReturn;
    }

    private void combineImages(BufferedImage currentBufferedImage, BufferedImage overlayImg) {
        Graphics2D g2d = currentBufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.drawImage(overlayImg, 0, 0, null);
        g2d.dispose();
    }
}
