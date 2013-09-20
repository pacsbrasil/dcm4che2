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
import in.raster.mayam.exception.CompressedDcmOnMacException;
import in.raster.mayam.facade.Platform;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.*;
import org.dcm4che2.image.OverlayUtils;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.tool.dcm2xml.Dcm2Xml;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ImportDcmDirDelegate extends Thread {

    Calendar today = Calendar.getInstance();
    String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE);
    boolean saveAsLink = false, skip = false;
    OutputStream outStream = null;
    boolean isDirectory = false, isVideo = false;
    File file = null;
    private File importFolder;
    ArrayList<String> absolutePathList = new ArrayList();
    FileInputStream fileinstream;
    ImageInputStream iis = null;
    Iterator<ImageReader> iter = null;
    ImageReader reader = null;
    int maxThumbwidth = 75;
    int importedFileCount = 0;

    public ImportDcmDirDelegate() {
    }

    public ImportDcmDirDelegate(File file, boolean isDirectory) {
        this.file = file;
        this.isDirectory = isDirectory;
    }

    @Override
    public void run() {
        if (isDirectory) {
            setImportFolder(file);
            readAndUpdateByFolder();
        } else if (isDicomFile(file)) {
            checkIsLink();
            if (!skip) {
                readAndImportDicomFile(file);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ApplicationContext.mainScreenObj.refreshLocalDB();
                    }
                });

            } else {
                return;
            }
        } else {
            JOptionPane.showMessageDialog(ApplicationContext.mainScreenObj, "Please select a valid DICOM File", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setImportFolder(File importFolder) {
        this.importFolder = importFolder;
    }

    private ArrayList getAbsolutePathArray() {
        addPath(importFolder.getAbsolutePath());
        return absolutePathList;
    }

    private void addPath(String directoryPath) {
        File directory = new File(directoryPath);
        String[] listOfFiles = directory.list();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (new File(directory.getAbsolutePath() + File.separator + listOfFiles[i]).isDirectory()) {
                addPath(new String(directory.getAbsolutePath() + File.separator + listOfFiles[i]));
            } else {
                absolutePathList.add(new String(directory.getAbsolutePath() + File.separator + listOfFiles[i]));
            }
        }
    }

    private void readAndUpdateByFolder() {
        getAbsolutePathArray();
        checkIsLink();
        if (!skip) {
            ApplicationContext.mainScreenObj.setProgressText("Importing");
            ApplicationContext.mainScreenObj.initializeProgressBar(absolutePathList.size());
            for (int i = 0; i < absolutePathList.size(); i++) {
                ApplicationContext.mainScreenObj.incrementProgressValue();
                File currentFile = new File(absolutePathList.get(i));
                if (isDicomFile(currentFile)) {
                    importedFileCount++;
                    readAndImportDicomFile(currentFile);
                }
            }
            ApplicationContext.mainScreenObj.hideProgressBar();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationContext.mainScreenObj.refreshLocalDB();
                }
            });
            JOptionPane.showMessageDialog(ApplicationContext.mainScreenObj, importedFileCount + " Files imported", "Import", JOptionPane.INFORMATION_MESSAGE);
        } else {
            return;
        }
    }

    private void readAndImportDicomFile(File parseFile) throws CompressedDcmOnMacException {
        DicomInputStream dis = null;
        File thumbnail = null;
        try {
            dis = new DicomInputStream(parseFile);
            DicomObject data = new BasicDicomObject();
            data = dis.readDicomObject();
            if (data != null) {
                if (Platform.getCurrentPlatform().equals(Platform.MAC)) {
                    if (data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ExplicitVRLittleEndian.uid()) || data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ImplicitVRLittleEndian.uid())) {
                        if (saveAsLink) {
                            thumbnail = new File(ApplicationContext.getAppDirectory() + File.separator + "Thumbnails");
                            ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, parseFile.getAbsolutePath());
                        } else {
                            thumbnail = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID) + File.separator + "Thumbnails");
                            File destination = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID));
                            if (!destination.exists()) {
                                destination.mkdirs();
                            }
                            File destinationFile = new File(destination, data.getString(Tags.SOPInstanceUID));
                            copy(parseFile, destinationFile);
                            ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, destinationFile.getAbsolutePath());
                        }
                    } else {
                        throw new CompressedDcmOnMacException();
                    }
                } else {
                    if (saveAsLink) {
                        thumbnail = new File(ApplicationContext.getAppDirectory() + File.separator + "Thumbnails");
                        ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, parseFile.getAbsolutePath());
                    } else {
                        thumbnail = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID) + File.separator + "Thumbnails");
                        File destination = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID));
                        if (!destination.exists()) {
                            destination.mkdirs();
                        }
                        File destinationFile = new File(destination, data.getString(Tags.SOPInstanceUID));
                        copy(parseFile, destinationFile);
                        ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, destinationFile.getAbsolutePath());
                    }
                }
            }
            if (!data.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) && !data.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) && !data.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) {
                if (!thumbnail.exists()) {
                    thumbnail.mkdirs();
                }
                createThumbnail(parseFile, new File(thumbnail + File.separator + data.getString(Tags.SOPInstanceUID)), data.getString(Tags.StudyInstanceUID), data.getString(Tags.SeriesInstanceUID), data.getString(Tags.SOPInstanceUID));
            } else {
                File videoFile = null;
                if (!saveAsLink) {
                    videoFile = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID) + File.separator + data.getString(Tags.SOPInstanceUID) + "_V" + File.separator + "video.xml");
                } else {
                    videoFile = new File(ApplicationContext.getAppDirectory() + File.separator + "Videos" + File.separator + data.getString(Tags.SOPInstanceUID) + "_V" + File.separator + "video.xml");
                }
                videoFile.getParentFile().mkdirs();
                try {
                    videoFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                }
                Dcm2Xml.main(new String[]{parseFile.getAbsolutePath(), "-X", "-o", videoFile.getAbsolutePath()});
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder;
                try {
                    dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(videoFile);
                    NodeList elementsByTagName1 = doc.getElementsByTagName("item");
                    for (int k = 0; k < elementsByTagName1.getLength(); k++) {
                        Node item = elementsByTagName1.item(k);
                        NamedNodeMap attributes = item.getAttributes();
                        if (attributes.getNamedItem("src") != null) {
                            Node namedItem = attributes.getNamedItem("src");
                            videoFile = new File(videoFile.getParentFile() + File.separator + namedItem.getNodeValue());
                            videoFile.renameTo(new File(videoFile.getAbsolutePath() + ".mpg"));
                            ApplicationContext.databaseRef.update("image", "FileStoreUrl", videoFile.getAbsolutePath() + ".mpg", "SopUID", data.getString(Tags.SOPInstanceUID));
                        }
                    }
                    dBuilder = null;
                    dbFactory = null;
                } catch (IOException ex) {
                    Logger.getLogger(ImportDcmDirDelegate.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(ImportDcmDirDelegate.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(ImportDcmDirDelegate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
//            System.out.println("Null in readAndImportDicomFile() : " + ex.getMessage());
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean isDicomFile(File file) {
        try {
            fileinstream = new FileInputStream(file);
            byte[] dcm = new byte[4];
            fileinstream.skip(128);
            int read = fileinstream.read(dcm, 0, 4);
            if (dcm[0] == 68 && dcm[1] == 73 && dcm[2] == 67 && dcm[3] == 77) {
                return true;
            }
        } catch (FileNotFoundException ex) {
//            System.out.println("File not found : " + ex.getMessage());
        } catch (IOException ex) {
//            System.out.println("IOException : " + ex.getMessage());
        }
        return false;
    }

    private void copy(File src, File destination) {
        try {
            fileinstream = new FileInputStream(src);
            outStream = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileinstream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            fileinstream.close();
            outStream.close();
            System.out.println("File : " + src.getAbsolutePath() + " copied successfully");
        } catch (IOException ex) {
//            System.out.println("IO Exception : " + ex.getMessage());
        }
    }

    public void checkIsLink() {
        int link = JOptionPane.showOptionDialog(null, "Do you want to copy or link files?", "Copy or link", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"   Copy   ", "   Link   "}, "default");
        if (link == 0) {
            saveAsLink = false;
        } else if (link == 1) {
            saveAsLink = true;
        } else {
            skip = true;
        }
    }

    private void createThumbnail(File srcFile, File destFile, String studyUid, String seriesUid, String sopUid) {
        Image loadedImage;
        int width = 60, height = 60;
        BufferedImage currentBufferedImage = null, imageToWrite = null;
        try {
            iis = ImageIO.createImageInputStream(srcFile);
            try {
                iter = ImageIO.getImageReadersByFormatName("DICOM");
                ImageReader reader = (ImageReader) iter.next();
                reader.setInput(iis, false);
                FileInputStream fis = new FileInputStream(srcFile);
                DicomInputStream dis = new DicomInputStream(fis);
                DicomObject obj = dis.readDicomObject();
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

                if (reader.getNumImages(true) > 0) {
                    loadedImage = new ImageIcon(currentBufferedImage).getImage();
                    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                    BufferedImage temp = gc.createCompatibleImage(width, height);
                    Graphics2D g2 = temp.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(loadedImage, 0, 0, temp.getWidth(), temp.getHeight(), null);
                    imageToWrite = temp;
                    g2.dispose();
                }

                if (obj.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                    loadedImage = new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/pdficon.jpeg")).getImage();
                    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                    BufferedImage temp = gc.createCompatibleImage(loadedImage.getWidth(null), loadedImage.getHeight(null));
                    Graphics2D g2 = temp.createGraphics();
                    g2.drawImage(loadedImage, 0, 0, null);
                    imageToWrite = temp;
                }
                ImageIO.write(imageToWrite, "jpeg", destFile);
                ApplicationContext.databaseRef.updateThumbnailStatus(studyUid, seriesUid, sopUid);
            } catch (ConcurrentModificationException cme) {
            } catch (NullPointerException npe) {
            } catch (NegativeArraySizeException nase) {
            } catch (NoSuchElementException elementException) {
            }
        } catch (IOException ex) {
            Logger.getLogger(ImportDcmDirDelegate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
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
