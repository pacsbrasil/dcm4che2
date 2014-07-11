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
 * Portions created by the Initial Developer are Copyright (C) 2014
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
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.*;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ImportDcmDirDelegate extends Thread {

    Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
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
            } else {
                return;
            }
        } else {
            JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.importFailiure.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationContext.mainScreenObj.refreshLocalDB();
            }
        });

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
            ApplicationContext.mainScreenObj.setProgressText(ApplicationContext.currentBundle.getString("MainScreen.importingProgressLabel.text"));
            ApplicationContext.mainScreenObj.initializeProgressBar(absolutePathList.size());
            for (int i = 0; i < absolutePathList.size(); i++) {
                File currentFile = new File(absolutePathList.get(i));
                if (isDicomFile(currentFile)) {
                    importedFileCount++;
                    readAndImportDicomFile(currentFile);
                }
            }
            ApplicationContext.mainScreenObj.hideProgressBar();
            JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, importedFileCount + " " + ApplicationContext.currentBundle.getString("MainScreen.import.filesCopied.text"), ApplicationContext.currentBundle.getString("MainScreen.importMenuItem.text"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
        } else {
            return;
        }
    }

    private void readAndImportDicomFile(File parseFile) throws CompressedDcmOnMacException {
//        DicomInputStream dis = null;
//        try {
//            dis = new DicomInputStream(parseFile);
//            DicomObject data = new BasicDicomObject();
//            data = dis.readDicomObject();
//            if (data != null) {
//                if (Platform.getCurrentPlatform().equals(Platform.MAC)) {
//                    if (data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ExplicitVRLittleEndian.uid()) || data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ImplicitVRLittleEndian.uid())) {
//                        if (saveAsLink) {
//                            ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, parseFile.getAbsolutePath(), true);
//                        } else {
//                            File destination = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID));
//                            if (!destination.exists()) {
//                                destination.mkdirs();
//                            }
//                            File destinationFile = new File(destination, data.getString(Tags.SOPInstanceUID));
//                            copy(parseFile, destinationFile);
//                            ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, destinationFile.getAbsolutePath(), true);
//                        }
//                    } else {
//                        throw new CompressedDcmOnMacException();
//                    }
//                } else {
//                    if (saveAsLink) {
//                        ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, parseFile.getAbsolutePath(), true);
//                    } else {
//                        File destination = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID));
//                        if (!destination.exists()) {
//                            destination.mkdirs();
//                        }
//                        File destinationFile = new File(destination, data.getString(Tags.SOPInstanceUID));
//                        copy(parseFile, destinationFile);
//                        ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, destinationFile.getAbsolutePath(), true);
//                    }
//                }
//            }
//        } catch (IOException ex) {
//            ApplicationContext.logger.log(Level.INFO, "Unable to import file", ex);
//        } catch (NullPointerException ex) {
//            ApplicationContext.logger.log(Level.INFO, "Unable to import file", ex);
//        } finally {
//            if (dis != null) {
//                try {
//                    dis.close();
//                } catch (Exception ex) {
//                    ApplicationContext.logger.log(Level.INFO, "Unable to import file", ex);
//                }
//            }
//        }
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
            ApplicationContext.logger.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            ApplicationContext.logger.log(Level.INFO, null, ex);
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
            ApplicationContext.logger.log(Level.INFO, "ImportDcmDirDelegate", ex);
        }
    }

    public void checkIsLink() {
        int link = JOptionPane.showOptionDialog(null, ApplicationContext.currentBundle.getString("MainScreen.importConfirmation.text"), ApplicationContext.currentBundle.getString("MainScreen.importConfirmation.title.text"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("MainScreen.import.copy.text"), ApplicationContext.currentBundle.getString("MainScreen.import.link.text")}, "default");
        if (link == 0) {
            saveAsLink = false;
        } else if (link == 1) {
            saveAsLink = true;
        } else {
            skip = true;
        }
    }
}