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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.media.DicomDirReader;
import org.dcm4che2.media.DirectoryRecordType;
import java.nio.channels.FileChannel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomCodingException;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ImportDcmDir extends SwingWorker<Void, Void> {

    boolean isDirectory = false, isVideo = false;
    boolean saveAsLink = false, skip = false;
    File file = null;
    Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
    String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator;
    //DicomDir
    File dicomdir = null;
    DicomDirReader dicomDirReader = null;
    int importedFileCount = 0;
    File importFolder = null;

    public ImportDcmDir(File file, boolean isDirectory) {
        this.file = file;
        this.isDirectory = isDirectory;
    }

    @Override
    protected Void doInBackground() throws Exception {
        setDicomDir();
        if (dicomdir != null) {
            isLink();
            if (!skip) {
                readDicomDir();
            }
        } else {
            //Import without DICOMDIR
            int showOptionDialog = JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.import.noDicomDir.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("YesButtons.text"), ApplicationContext.currentBundle.getString("NoButtons.text")}, "default");
            if (showOptionDialog == 0) {
                OpenFilesManually();
            }
            //Get DICOMDIR file from user
//                final JFileChooser fc = new JFileChooser(file.getAbsolutePath());
//                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//                fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
//                    @Override
//                    public boolean accept(File pathname) {
//                        if (!pathname.isDirectory()) {
//                            return pathname.getName().equalsIgnoreCase("dicomdir");
//                        } else {
//                            return true;
//                        }
//                    }
//
//                    @Override
//                    public String getDescription() {
//                        return "DICOMDIR";
//                    }
//                });
//                fc.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        if (e.getActionCommand().equalsIgnoreCase(JFileChooser.APPROVE_SELECTION)) {
//                            dicomdir = fc.getSelectedFile();
//                            isLink();
//                            if (!skip) {
//                                readDicomDir();
//                            }
//                        }
//                    }
//                });              

        }
        return null;
    }

    /*
     * Import without DICOMDIR
     */
    private void OpenFilesManually() {
        final JFileChooser fc = new JFileChooser(file.getAbsolutePath());
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        fc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equalsIgnoreCase(JFileChooser.APPROVE_SELECTION)) {
                    importFolder = fc.getSelectedFile();
                    new ImportByFolder().start();
                }
            }
        });
        fc.setVisible(true);
        fc.showOpenDialog(ApplicationContext.mainScreenObj);
    }
    //Filter to find the DICOMDIR file
    FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.equalsIgnoreCase("dicomdir");
        }
    };

    private void setDicomDir() {
        if (isDirectory) {
            if (!searchForDICOMDIR(file)) {
                File[] children = file.listFiles(); //Still could not find the DICOMDIR file
                if (this.dicomdir == null && children.length > 0) {
                    for (int i = 0; i < children.length; i++) {
                        if (children[i].isDirectory()) {
                            if (searchForDICOMDIR(children[i])) {
                                break;
                            }
                        }

                    }
                }
            }
        }
    }

    /*
     * You may add recursive call here to interior directories 
     * For now this method search in immediate directories alone and not in immediate dirctory's children
     */
    private boolean searchForDICOMDIR(File directory) {
        File[] children = directory.listFiles(filter);
        if (children.length > 0) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].isFile() && children[i].canRead()) {
                    this.dicomdir = children[i];
                    return true;
                }

            }
        }
        return false;
    }

    private void isLink() {
        int link = JOptionPane.showOptionDialog(null, ApplicationContext.currentBundle.getString("MainScreen.importConfirmation.text"), ApplicationContext.currentBundle.getString("MainScreen.importConfirmation.title.text"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("MainScreen.import.copy.text"), ApplicationContext.currentBundle.getString("MainScreen.import.link.text")}, "default");
        switch (link) {
            case 0:
                saveAsLink = false;
                break;
            case 1:
                saveAsLink = true;
                break;
            default:
                skip = true;
                break;
        }
    }

    private void showInvalidDicomDirError() {
        int showOptionDialog = JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.import.notValid.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("YesButtons.text"), ApplicationContext.currentBundle.getString("NoButtons.text")}, "default");
        if (showOptionDialog == 0) {
            OpenFilesManually();
        }
    }

    private void readDicomDir() {
        try {
            try {
                dicomDirReader = new DicomDirReader(dicomdir);
            } catch (DicomCodingException ex) {
                int showOptionDialog = JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.import.notValid.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("YesButtons.text"), ApplicationContext.currentBundle.getString("NoButtons.text")}, "default");
                if (showOptionDialog == 0) {
                    OpenFilesManually();
                }
                return;
            }
            dicomDirReader.setShowInactiveRecords(false);
            DicomObject patient = dicomDirReader.findFirstRootRecord();
            while (patient != null) {
                if (DirectoryRecordType.PATIENT.equals(patient.getString(Tag.DirectoryRecordType))) {
                    if (patient.getString(Tags.PatientID) == null) {
                        showInvalidDicomDirError();
                        return;
                    }
                    ApplicationContext.databaseRef.insertPatientInfo(patient);
                    DicomObject study = dicomDirReader.findFirstChildRecord(patient);
                    while (study != null) {
                        if (DirectoryRecordType.STUDY.equals(study.getString(Tag.DirectoryRecordType))) {
                            if (study.getString(Tags.StudyInstanceUID) == null) {
                                showInvalidDicomDirError();
                                return;
                            }
                            ApplicationContext.databaseRef.insertStudyInfo(study, saveAsLink, patient.getString(Tags.PatientID));

                            DicomObject series = dicomDirReader.findFirstChildRecord(study);
                            while (series != null) {
                                if (DirectoryRecordType.SERIES.equals(series.getString(Tag.DirectoryRecordType))) {
                                    if (series.getString(Tags.SeriesInstanceUID) == null) {
                                        showInvalidDicomDirError();
                                        return;
                                    }
                                    ApplicationContext.databaseRef.insertSeriesInfo(series, patient.getString(Tags.PatientID), study.getString(Tags.StudyInstanceUID));
                                    DicomObject instance = dicomDirReader.findFirstChildRecord(series);

                                    while (instance != null) {
                                        if (instance.getString(Tags.SOPInstanceUID) == null) {
                                            showInvalidDicomDirError();
                                            return;
                                        }

                                        if (DirectoryRecordType.IMAGE.equals(instance.getString(Tag.DirectoryRecordType))) {
                                            File file = dicomDirReader.toReferencedFile(instance);

                                            /*
                                             * Linux is case sensitive, so this if part is used to map the dicomdir path in linux file system
                                             * 
                                             */
                                            if (!file.exists()) {
                                                String filePath = file.getAbsolutePath();
                                                String newFilePath = filePath.replace(filePath.substring(dicomdir.getParent().length()), filePath.substring(dicomdir.getParent().length()).toLowerCase());
                                                file = new File(newFilePath);
                                            }

                                            if (file.exists()) {
                                                ApplicationContext.databaseRef.insertImageInfo(instance, saveAsLink ? file.getAbsolutePath() : dest + study.getString(Tags.StudyInstanceUID) + File.separator + series.getString(Tags.SeriesInstanceUID) + File.separator + instance.getString(Tags.SOPInstanceUID), saveAsLink, saveAsLink, patient.getString(Tags.PatientID), study.getString(Tags.StudyInstanceUID), series.getString(Tags.SeriesInstanceUID));
                                                if (Platform.getCurrentPlatform().equals(Platform.MAC) && !instance.getString(Tags.TransferSyntaxUID).equals(TransferSyntax.ExplicitVRLittleEndian.uid()) && !instance.getString(Tags.TransferSyntaxUID).equals(TransferSyntax.ImplicitVRLittleEndian.uid())) {
                                                    throw new CompressedDcmOnMacException();
                                                } else {
                                                    if (!saveAsLink) {
                                                        File destination = new File(dest + study.getString(Tags.StudyInstanceUID) + File.separator + series.getString(Tags.SeriesInstanceUID));
                                                        if (!destination.exists()) {
                                                            destination.mkdirs();
                                                        }
                                                        copy(file, destination.getAbsolutePath() + File.separator + instance.getString(Tags.SOPInstanceUID));
                                                    }
                                                    ApplicationContext.databaseRef.addInstanceCount(study.getString(Tags.StudyInstanceUID), series.getString(Tags.SeriesInstanceUID));
                                                }
                                                importedFileCount++;
                                            } else {
                                                System.err.println("File : " + file.getAbsolutePath() + " not exist.");
                                            }
                                        }
                                        instance = dicomDirReader.findNextSiblingRecord(instance);
                                    }
                                }
                                series = dicomDirReader.findNextSiblingRecord(series);
                            }
                        }
                        study = dicomDirReader.findNextSiblingRecord(study);
                    }
                }
                patient = dicomDirReader.findNextSiblingRecord(patient);
            }
            JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, importedFileCount + " " + ApplicationContext.currentBundle.getString("MainScreen.import.filesCopied.text"), ApplicationContext.currentBundle.getString("MainScreen.importMenuItem.text"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationContext.mainScreenObj.refreshLocalDB();
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(ImportDcmDir.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void copy(File src, String destination) {
//        try {
//            fileinstream = new FileInputStream(src);
//            outStream = new FileOutputStream(destination);
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = fileinstream.read(buffer)) > 0) {
//                outStream.write(buffer, 0, length);
//            }
//            fileinstream.close();
//            outStream.close();
//            System.out.println("File : " + src.getAbsolutePath() + " copied successfully");
//        } catch (IOException ex) {
//            ApplicationContext.logger.log(Level.INFO, "ImportDcmDir", ex);
//        }

        //Channel transmission
        FileChannel source = null;
        FileChannel destChannel = null;

        try {
            source = new FileInputStream(src).getChannel();
            destChannel = new FileOutputStream(destination).getChannel();
            destChannel.transferFrom(source, 0, source.size());
            System.out.println("File : " + src.getAbsolutePath() + " copied successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
                if (destChannel != null) {
                    destChannel.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ImportDcmDir.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void readAndUpdateByFolder(File importFolder) {
        if (!skip) {
            ApplicationContext.mainScreenObj.setProgressText(ApplicationContext.currentBundle.getString("MainScreen.importingProgressLabel.text"));
            ApplicationContext.mainScreenObj.initializeProgressBar(0);
            readFiles(importFolder);

            ApplicationContext.mainScreenObj.hideProgressBar();
            JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, importedFileCount + " " + ApplicationContext.currentBundle.getString("MainScreen.import.filesCopied.text"), ApplicationContext.currentBundle.getString("MainScreen.importMenuItem.text"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationContext.mainScreenObj.refreshLocalDB();
                }
            });
        }
    }

    private void readFiles(File folder) {
        File[] dicomFiles = folder.listFiles();
        ApplicationContext.mainScreenObj.addMaximum(dicomFiles.length);
        for (int i = 0; i < dicomFiles.length; i++) {
            if (dicomFiles[i].isFile()) {
                if (isDicomFile(dicomFiles[i])) {
                    importedFileCount++;
                    readAndImportDicomFile(dicomFiles[i]);
                } else {
                    System.err.println(dicomFiles[i].getName() + " is not a valid DICOM File.");
                }
            } else {
                readFiles(dicomFiles[i]);
            }

        }
    }

    private void readAndImportDicomFile(File dicomFile) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(dicomFile);
            DicomObject data = new BasicDicomObject();
            data = dis.readDicomObject();
            if (data != null) {
                if (Platform.getCurrentPlatform().equals(Platform.MAC)) {
                    if (data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ExplicitVRLittleEndian.uid()) || data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ImplicitVRLittleEndian.uid())) {
                        if (saveAsLink) {
                            ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, dicomFile.getAbsolutePath());
                        } else {
                            File destination = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID));
                            if (!destination.exists()) {
                                destination.mkdirs();
                            }
                            copy(dicomFile, destination + File.separator + data.getString(Tags.SOPInstanceUID));
                            ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, destination + File.separator + data.getString(Tags.SOPInstanceUID));
                        }
                        ApplicationContext.databaseRef.addInstanceCount(data.getString(Tags.StudyInstanceUID), data.getString(Tags.SeriesInstanceUID));
                    } else {
                        throw new CompressedDcmOnMacException();
                    }
                } else {
                    if (saveAsLink) {
                        ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, dicomFile.getAbsolutePath());
                    } else {
                        File destination = new File(dest + File.separator + data.getString(Tags.StudyInstanceUID) + File.separator + data.getString(Tags.SeriesInstanceUID));
                        if (!destination.exists()) {
                            destination.mkdirs();
                        }
                        copy(dicomFile, destination + File.separator + data.getString(Tags.SOPInstanceUID));
                        ApplicationContext.databaseRef.writeDatasetInfo(data, saveAsLink, destination + File.separator + data.getString(Tags.SOPInstanceUID));
                    }
                    ApplicationContext.databaseRef.addInstanceCount(data.getString(Tags.StudyInstanceUID), data.getString(Tags.SeriesInstanceUID));
                }
            }
        } catch (IOException ex) {
            System.err.println("Unable to import file " + ex.getMessage());
            ApplicationContext.logger.log(Level.INFO, "Unable to import file", ex);
        } catch (NullPointerException ex) {
            ApplicationContext.logger.log(Level.INFO, "Unable to import file", ex);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception ex) {
                    ApplicationContext.logger.log(Level.INFO, "Unable to import file", ex);
                }
            }
        }
    }

    /*
     * Validates the DICOM File
     */
    private boolean isDicomFile(File file) {
        try {
            FileInputStream fileinstream = new FileInputStream(file);
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

    private class ImportByFolder extends Thread {

        @Override
        public void run() {
            if (importFolder.isDirectory()) {
                readAndUpdateByFolder(importFolder);
            } else {
                //Single file 
                if (importFolder.getName().toLowerCase().contains("dicomdir")) { //If dicomdir file is chosen
                    dicomdir = importFolder;
                    readDicomDir();
                } else { // The chosen file is not the dicomdir
                    readAndImportDicomFile(importFolder);
                    JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, "1 " + ApplicationContext.currentBundle.getString("MainScreen.import.filesCopied.text"), ApplicationContext.currentBundle.getString("MainScreen.importMenuItem.text"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ApplicationContext.mainScreenObj.refreshLocalDB();
                        }
                    });
                }

            }
        }
    }
}