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
import org.dcm4che2.io.DicomCodingException;

/**
 *
 * @author devishree
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
//                ApplicationContext.mainScreenObj.setProgressText(ApplicationContext.currentBundle.getString("MainScreen.importingProgressLabel.text"));
                readDicomDir();
            }
        } else {
            int showOptionDialog = JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.import.noDicomDir.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("YesButtons.text"), ApplicationContext.currentBundle.getString("NoButtons.text")}, "default");
            if (showOptionDialog == 0) {
                final JFileChooser fc = new JFileChooser(file.getAbsolutePath());
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if(!pathname.isDirectory()) {
                            return pathname.getName().equalsIgnoreCase("dicomdir");
                        } else {
                            return true;
                        }
                    }

                    @Override
                    public String getDescription() {
                        return "DICOMDIR";
                    }
                });
                fc.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (e.getActionCommand().equalsIgnoreCase(JFileChooser.APPROVE_SELECTION)) {
                            dicomdir = fc.getSelectedFile();
                            isLink();
                            if(!skip) {
                                readDicomDir();
                            }
                        }
                    }
                });
                fc.setVisible(true);
                fc.showOpenDialog(ApplicationContext.mainScreenObj);
            }
        }
        return null;
    }

    private void setDicomDir() {
        if (isDirectory) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.equalsIgnoreCase("dicomdir");
                }
            };

            File[] children = file.listFiles(filter);
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    if (children[i].isFile() && children[i].canRead()) {
                        this.dicomdir = children[i];
                        break;
                    }
                }
            }
        }
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

    private void readDicomDir() {
        try {
            try {
                dicomDirReader = new DicomDirReader(dicomdir);
            } catch (DicomCodingException ex) {
                JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.import.notValid.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
                return;
            }
            dicomDirReader.setShowInactiveRecords(false);
            DicomObject patient = dicomDirReader.findFirstRootRecord();
            while (patient != null) {
                if (DirectoryRecordType.PATIENT.equals(patient.getString(Tag.DirectoryRecordType))) {
                    ApplicationContext.databaseRef.insertPatientInfo(patient);
                    DicomObject study = dicomDirReader.findFirstChildRecord(patient);
                    while (study != null) {
                        if (DirectoryRecordType.STUDY.equals(study.getString(Tag.DirectoryRecordType))) {
                            ApplicationContext.databaseRef.insertStudyInfo(study, saveAsLink, patient.getString(Tags.PatientID));

                            DicomObject series = dicomDirReader.findFirstChildRecord(study);
                            while (series != null) {
                                if (DirectoryRecordType.SERIES.equals(series.getString(Tag.DirectoryRecordType))) {
                                    ApplicationContext.databaseRef.insertSeriesInfo(series, patient.getString(Tags.PatientID), study.getString(Tags.StudyInstanceUID));
                                    DicomObject instance = dicomDirReader.findFirstChildRecord(series);

                                    while (instance != null) {
                                        if (DirectoryRecordType.IMAGE.equals(instance.getString(Tag.DirectoryRecordType))) {
                                            File file = dicomDirReader.toReferencedFile(instance);

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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationContext.mainScreenObj.refreshLocalDB();
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(ImportDcmDir.class.getName()).log(Level.SEVERE, null, ex);
        }
//        catch(DicomCodingException ex) {
//            JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.import.notValid.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
//        }
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
}