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
package in.raster.mayam.delegate;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.exception.CompressedDcmOnMacException;
import in.raster.mayam.facade.ApplicationFacade;
import in.raster.mayam.facade.Platform;
import in.raster.mayam.form.MainScreen;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.media.DirectoryRecordType;
import org.dcm4che2.media.DicomDirReader;
import sun.awt.shell.ShellFolder;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ImportDcmDirDelegate {

    private DicomDirReader dicomDir;
    private File dcmDirFile;
    private boolean copyAsLink = false;
    private DicomObject dataset = new BasicDicomObject();

    public ImportDcmDirDelegate() {
        findDcmDirInMedia();
        //this.start();
    }

    public ImportDcmDirDelegate(File dcmDirFile) {
        this.dcmDirFile = dcmDirFile;
        // this.start();
    }

    public void findAndRun() {
        findDcmDirInMedia();
        try {
            run();
        } catch (CompressedDcmOnMacException e) {
            JOptionPane.showMessageDialog(ApplicationContext.mainScreen, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws CompressedDcmOnMacException, Exception {
        readDcmDirFile();
        if (dicomDir != null) {
            new DirParser().parseDir();
            MainScreen.showLocalDBStorage();
        }

    }

    /**
     * This routine used to read the dicom dir file.
     */
    public void readDcmDirFile() {
        try {
            dicomDir = new DicomDirReader(dcmDirFile);
            dicomDir.setShowInactiveRecords(false);
        } catch (NullPointerException ex) {
            // This Exception will be raised if cd drive is not ready for finding the file   

            JOptionPane.showMessageDialog(null, "Drive reading error please try again later ", null, JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ImportDcmDirDelegate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Member inner class DirParser used to parse the dicom dir file
     */
    class DirParser {

        private void parseDir() throws CompressedDcmOnMacException, Exception {
            parsePatient();
        }

        /**
         *
         * @throws IOException
         */
        private void parsePatient() throws CompressedDcmOnMacException, Exception {
            DicomObject patient = dicomDir.findFirstRootRecord();
            while (patient != null) {
                if (DirectoryRecordType.PATIENT.equals(patient.getString(Tag.DirectoryRecordType))) {
                    new DatasetUpdator().updatePatientInfo(patient);
                    parseStudy(patient);

                }
                patient = dicomDir.findNextSiblingRecord(patient);
            }
        }

        /**
         *
         * @param patient
         * @throws IOException
         */
        private void parseStudy(DicomObject patient) throws IOException, CompressedDcmOnMacException {
            DicomObject study = dicomDir.findFirstChildRecord(patient);
            while (study != null) {
                if (DirectoryRecordType.STUDY.equals(study.getString(Tag.DirectoryRecordType))) {
                    //update study level tag values
                    new DatasetUpdator().updateStudyInfo(study);
                    parseSeries(study);
                }
                study = dicomDir.findNextSiblingRecord(study);
            }//study loop completed
        }

        /**
         *
         * @param study
         * @throws IOException
         */
        private void parseSeries(DicomObject study) throws IOException, CompressedDcmOnMacException {
            DicomObject series = dicomDir.findFirstChildRecord(study);
            while (series != null) {
                if (DirectoryRecordType.SERIES.equals(series.getString(Tag.DirectoryRecordType))) {
                    //update series level tag values
                    new DatasetUpdator().updateSeriesInfo(series);
                    parseInstance(series);
                }
                series = dicomDir.findNextSiblingRecord(series);
            }//series loop completed
        }

        /**
         *
         * @param series-
         * @throws IOException
         */
        private void parseInstance(DicomObject series) throws IOException, CompressedDcmOnMacException {
            DicomObject instance = dicomDir.findFirstChildRecord(series);
            while (instance != null) {
                if (DirectoryRecordType.IMAGE.equals(instance.getString(Tag.DirectoryRecordType))) {
                    //update instance level tag values
                    new DatasetUpdator().updateInstanceInfo(instance);
                    File f = dicomDir.toReferencedFile(instance);
                    //import to database
                    if (Platform.getCurrentPlatform().equals(Platform.MAC)) {
                        if (instance.getString(Tag.ReferencedTransferSyntaxUIDInFile).equalsIgnoreCase(TransferSyntax.ExplicitVRLittleEndian.uid()) || instance.getString(Tag.ReferencedTransferSyntaxUIDInFile).equalsIgnoreCase(TransferSyntax.ImplicitVRLittleEndian.uid())) {
                            ApplicationContext.databaseRef.importDataToDatabase(dataset, f, copyAsLink);
                        } else {
                            throw new CompressedDcmOnMacException();
                            // JOptionPane.showMessageDialog(ApplicationContext.mainScreen, "Compressed image cannot be opened in Mac");
                        }
                    } else {
                        ApplicationContext.databaseRef.importDataToDatabase(dataset, f, copyAsLink);
                    }
                }
                instance = dicomDir.findNextSiblingRecord(instance);
            }//instance loop completed
        }
    }

    /**
     * Member inner class DatasetUpdator used to update the dicom object related information
     */
    class DatasetUpdator {

        /**
         *
         * @param next-It contains the patient related dicom tags information
         */
        private void updatePatientInfo(DicomObject next) {
            updateTag(dataset, Tag.PatientName, next.getString(Tag.PatientName));
            updateTag(dataset, Tag.PatientID, next.getString(Tag.PatientID));
            updateTag(dataset, Tag.PatientSex, next.getString(Tag.PatientSex));
        }

        /**
         *
         * @param study-It contains the study related dicom tags information
         */
        private void updateStudyInfo(DicomObject study) {
            updateTag(dataset, Tag.StudyInstanceUID, study.getString(Tag.StudyInstanceUID));
            updateTag(dataset, Tag.StudyDate, study.getString(Tag.StudyDate));
            updateTag(dataset, Tag.AccessionNumber, study.getString(Tag.AccessionNumber));
            updateTag(dataset, Tag.ReferringPhysicianName, study.getString(Tag.ReferringPhysicianName));
            updateTag(dataset, Tag.NumberOfStudyRelatedSeries, study.getString(Tag.NumberOfStudyRelatedSeries));
            updateTag(dataset, Tag.NumberOfStudyRelatedInstances, study.getString(Tag.NumberOfStudyRelatedInstances));
            updateTag(dataset, Tag.StudyDescription, study.getString(Tag.StudyDescription));
            updateTag(dataset, Tag.Modality, study.getString(Tag.Modality));
        }

        /**
         *
         * @param series-It contains the series related dicom tags information
         */
        private void updateSeriesInfo(DicomObject series) {
            updateTag(dataset, Tag.SeriesDate, series.getString(Tag.SeriesDate));
            updateTag(dataset, Tag.NumberOfSeriesRelatedInstances, series.getString(Tag.NumberOfSeriesRelatedInstances));
            updateTag(dataset, Tag.InstitutionName, series.getString(Tag.InstitutionName));
            updateTag(dataset, Tag.SeriesNumber, series.getString(Tag.SeriesNumber));
            updateTag(dataset, Tag.Modality, series.getString(Tag.Modality));
            updateTag(dataset, Tag.SeriesDescription, series.getString(Tag.SeriesDescription));
            updateTag(dataset, Tag.SeriesInstanceUID, series.getString(Tag.SeriesInstanceUID));
            updateTag(dataset, Tag.SeriesNumber, series.getString(Tag.SeriesNumber));
        }

        /**
         *
         * @param instance-It contains the instance related dicom tags information
         */
        private void updateInstanceInfo(DicomObject instance) {
            updateTag(dataset, Tag.SOPInstanceUID, instance.getString(Tag.ReferencedSOPInstanceUIDInFile));
        }

        /**
         *
         * @param dObj-dicom object it will contains the dicom tag information
         * @param tag-Tag constant used to specify the tag information
         * @param newValue-values to be replaced
         */
        private void updateTag(DicomObject dObj, int tag, String newValue) {
            VR vr = dObj.vrOf(tag);
            dObj.putString(tag, vr, newValue);
        }
    }

    /**
     * This sub routine used to find the media location based on the platform
     */
    public void findDcmDirInMedia() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            File[] drives = null;
            // list Filesystem roots
            if (os.startsWith("win")) {
                drives = ShellFolder.listRoots();
            } else if (os.startsWith("mac")) {
                drives = new File("/Volumes").listFiles();
            } else {
                drives = new File("/media").listFiles();
            }
            // search for dicomdir in system roots
            for (int i = 0; i < drives.length; i++) {
                File dicomDirCD = new File(drives[i] + File.separator + "DICOMDIR");
                if (dicomDirCD.canRead()) {
                    dcmDirFile = dicomDirCD;
                    break;
                }
                dicomDirCD = new File(drives[i] + File.separator + "dicomdir");
                if (dicomDirCD.canRead()) {
                    dcmDirFile = dicomDirCD;
                    break;
                }
                //Some Unix needs the point at the end
                dicomDirCD = new File(drives[i] + File.separator + "dicomdir.");
                if (dicomDirCD.canRead()) {
                    dcmDirFile = dicomDirCD;
                    break;
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public static void findAndLoadDcmDirFiles() {
        try {
            ApplicationContext.databaseRef.deleteCopyAsLinkStudies();
            ImportDcmDirDelegate importDcmDirDelegate = new ImportDcmDirDelegate();
            importDcmDirDelegate.findAndLoadDcmDir();
            if (importDcmDirDelegate.dcmDirFile != null && importDcmDirDelegate.dcmDirFile.getParent().startsWith(ApplicationFacade.binPath)) {
                importDcmDirDelegate.copyAsLink = true;
                importDcmDirDelegate.run();
            }
        } catch (CompressedDcmOnMacException e) {
            JOptionPane.showMessageDialog(ApplicationContext.mainScreen, e.getMessage());
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void findAndLoadDcmDir() {
        try {
            String userDir = ApplicationFacade.binPath;
            File file = new File(userDir + File.separator + "DCMDIR");
            if (file.canRead()) {
                dcmDirFile = file;
            }
            file = new File(userDir + File.separator + "dicomdir");
            if (file.canRead()) {
                dcmDirFile = file;
            }
            file = new File(userDir + File.separator + "dicomdir.");
            if (file.canRead()) {
                dcmDirFile = file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCopyAsLink() {
        return copyAsLink;
    }

    public void setCopyAsLink(boolean copyAsLink) {
        this.copyAsLink = copyAsLink;
    }
}
