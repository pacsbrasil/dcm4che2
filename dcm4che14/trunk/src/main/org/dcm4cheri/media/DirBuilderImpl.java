/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

package org.dcm4cheri.media;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.media.DirBuilder;
import org.dcm4che.media.DirBuilderPref;
import org.dcm4che.media.DirRecord;
import org.dcm4che.media.DirWriter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DirBuilderImpl implements DirBuilder {
    
    private final DirWriter writer;
    private final DirBuilderPref pref;

    private String curPatID;
    private DirRecord curPatRec;
    private String curStudyUID;
    private DirRecord curStudyRec;
    private String curSeriesUID;
    private DirRecord curSeriesRec;

    /** Creates a new instance of DirBuilderImpl */
    public DirBuilderImpl(DirWriter writer, DirBuilderPref pref) {
        this.writer = writer;
        this.pref = pref;
    }

    public int addFileRef(File file) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        Dataset ds = DirReaderImpl.factory.newDataset();
        try {
            ds.read(in, FileFormat.DICOM_FILE, Tags.PixelData);
        } finally {
            in.close();
        }
        return addFileRef(writer.toFileIDs(file), ds);
    }
    
    public int addFileRef(String[] fileIDs, Dataset ds) throws IOException {
        FileMetaInfo fmi = ds.getFileMetaInfo();
        if (fmi == null) {
            throw new IllegalArgumentException("Missing File Meta Information");
        }
        String tsUID = fmi.getTransferSyntaxUID();
        if (tsUID == null) {
            throw new IllegalArgumentException("Missing Transfer Syntax UID");
        }
        String classUID = fmi.getMediaStorageSOPClassUID();
        if (classUID == null) {
            throw new IllegalArgumentException("Missing SOP Class UID");
        }
        if (!classUID.equals(ds.getString(Tags.SOPClassUID))) {
            throw new IllegalArgumentException("Mismatch SOP Class UID");
        }
        String type = (String)REC_TYPE_MAP.get(classUID);
        if (type == null) {
            throw new UnsupportedOperationException("classUID:" + classUID);
        }
        String instUID = fmi.getMediaStorageSOPInstanceUID();
        if (instUID == null) {
            throw new IllegalArgumentException("Missing SOP Instance UID");
        }
        if (!instUID.equals(ds.getString(Tags.SOPInstanceUID))) {
            throw new IllegalArgumentException("Mismatch SOP Instance UID");
        }
        String seriesUID = ds.getString(Tags.SeriesInstanceUID);
        if (seriesUID == null) {
            throw new IllegalArgumentException("Missing Series Instance UID");
        }
        String studyUID = ds.getString(Tags.StudyInstanceUID);
        if (studyUID == null) {
            throw new IllegalArgumentException("Missing Study Instance UID");
        }
        String patID = ds.getString(Tags.PatientID, "");
        int count = 0;
        if (!patID.equals(curPatID)) {
            count += addPatRec(ds, patID);
        }
        if (!studyUID.equals(curStudyUID)) {
            count += addStudyRec(ds, studyUID);
        }
        if (!seriesUID.equals(curSeriesUID)) {
            count += addSeriesRec(ds, seriesUID);
        }
        writer.add(curSeriesRec, type,
                ds.newView(pref.getTagsForRecordType(type)),
                fileIDs, classUID, instUID, tsUID);
        ++count;
        return count;
    }

    private int addPatRec(Dataset ds, String patID) throws IOException {
        this.curSeriesUID = null;
        this.curSeriesRec = null;
        this.curStudyUID = null;
        this.curStudyRec = null;
        this.curPatID = patID;
        for (DirRecord dr = writer.getFirstRecord(true); dr != null;
                dr = dr.getNextSibling(true)) {
            if ("PATIENT".equals(dr.getType()) && patID.equals(
                            dr.getDataset().getString(Tags.PatientID))) {
                curPatRec = dr;
                return 0;
            }
        }
        curPatRec = writer.add(null, "PATIENT",
                ds.newView(pref.getTagsForRecordType("PATIENT")));
        return 1;
    }
    
    private int addStudyRec(Dataset ds, String studyUID) throws IOException {
        this.curSeriesUID = null;
        this.curSeriesRec = null;
        this.curStudyUID = studyUID;
        for (DirRecord dr = curPatRec.getFirstChild(true); dr != null;
                dr = dr.getNextSibling(true)) {
            if ("STUDY".equals(dr.getType()) && studyUID.equals(
                            dr.getDataset().getString(Tags.StudyInstanceUID))) {
                curStudyRec = dr;
                return 0;
            }
        }
        curStudyRec = writer.add(curPatRec, "STUDY",
                ds.newView(pref.getTagsForRecordType("STUDY")));
        return 1;
    }
    
    private int addSeriesRec(Dataset ds, String seriesUID) throws IOException {
        this.curSeriesUID = seriesUID;
        for (DirRecord dr = curStudyRec.getFirstChild(true); dr != null;
                dr = dr.getNextSibling(true)) {
            if ("SERIES".equals(dr.getType()) && seriesUID.equals(
                            dr.getDataset().getString(Tags.SeriesInstanceUID))) {
                curSeriesRec = dr;
                return 0;
            }
        }
        curSeriesRec = writer.add(curStudyRec, "SERIES",
                ds.newView(pref.getTagsForRecordType("SERIES")));
        return 1;
    }
    
    public void close() throws IOException {
        writer.close();
    }
    
    private static HashMap REC_TYPE_MAP = new HashMap(79);
    static {
        REC_TYPE_MAP.put(UIDs.StoredPrintStorage, "STORED PRINT");
        REC_TYPE_MAP.put(UIDs.HardcopyGrayscaleImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.HardcopyGrayscaleImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.HardcopyColorImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.ComputedRadiographyImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.DigitalXRayImageStorageForPresentation, "IMAGE");
        REC_TYPE_MAP.put(UIDs.DigitalXRayImageStorageForProcessing, "IMAGE");
        REC_TYPE_MAP.put(UIDs.DigitalMammographyXRayImageStorageForPresentation, "IMAGE");
        REC_TYPE_MAP.put(UIDs.DigitalMammographyXRayImageStorageForProcessing, "IMAGE");
        REC_TYPE_MAP.put(UIDs.DigitalIntraoralXRayImageStorageForPresentation, "IMAGE");
        REC_TYPE_MAP.put(UIDs.DigitalIntraoralXRayImageStorageForProcessing, "IMAGE");
        REC_TYPE_MAP.put(UIDs.CTImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.UltrasoundMultiframeImageStorageRetired, "IMAGE");
        REC_TYPE_MAP.put(UIDs.UltrasoundMultiframeImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.MRImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.NuclearMedicineImageStorageRetired, "IMAGE");
        REC_TYPE_MAP.put(UIDs.UltrasoundImageStorageRetired, "IMAGE");
        REC_TYPE_MAP.put(UIDs.UltrasoundImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.SecondaryCaptureImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.MultiframeSingleBitSecondaryCaptureImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.MultiframeGrayscaleByteSecondaryCaptureImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.MultiframeGrayscaleWordSecondaryCaptureImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.MultiframeColorSecondaryCaptureImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.StandaloneOverlayStorage, "OVERLAY");
        REC_TYPE_MAP.put(UIDs.StandaloneCurveStorage, "CURVE");
        REC_TYPE_MAP.put(UIDs.TwelveLeadECGWaveformStorage, "WAVEFORM");
        REC_TYPE_MAP.put(UIDs.GeneralECGWaveformStorage, "WAVEFORM");
        REC_TYPE_MAP.put(UIDs.AmbulatoryECGWaveformStorage, "WAVEFORM");
        REC_TYPE_MAP.put(UIDs.HemodynamicWaveformStorage, "WAVEFORM");
        REC_TYPE_MAP.put(UIDs.CardiacElectrophysiologyWaveformStorage, "WAVEFORM");
        REC_TYPE_MAP.put(UIDs.BasicVoiceAudioWaveformStorage, "WAVEFORM");
        REC_TYPE_MAP.put(UIDs.StandaloneModalityLUTStorage, "MODALITY LUT");
        REC_TYPE_MAP.put(UIDs.StandaloneVOILUTStorage, "VOI LUT");
        REC_TYPE_MAP.put(UIDs.GrayscaleSoftcopyPresentationStateStorage, "PRESENTATION");
        REC_TYPE_MAP.put(UIDs.XRayAngiographicImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.XRayRadiofluoroscopicImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.XRayAngiographicBiPlaneImageStorageRetired, "IMAGE");
        REC_TYPE_MAP.put(UIDs.NuclearMedicineImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.VLImageStorageRetired, "IMAGE");
        REC_TYPE_MAP.put(UIDs.VLMultiframeImageStorageRetired, "IMAGE");
        REC_TYPE_MAP.put(UIDs.VLEndoscopicImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.VLMicroscopicImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.VLSlideCoordinatesMicroscopicImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.VLPhotographicImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.BasicTextSR, "SR DOCUMENT");
        REC_TYPE_MAP.put(UIDs.EnhancedSR, "SR DOCUMENT");
        REC_TYPE_MAP.put(UIDs.ComprehensiveSR, "SR DOCUMENT");
        REC_TYPE_MAP.put(UIDs.MammographyCADSR, "SR DOCUMENT");
        REC_TYPE_MAP.put(UIDs.KeyObjectSelectionDocument, "KEY OBJECT DOC");        
        REC_TYPE_MAP.put(UIDs.PositronEmissionTomographyImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.StandalonePETCurveStorage, "CURVE");
        REC_TYPE_MAP.put(UIDs.RTImageStorage, "IMAGE");
        REC_TYPE_MAP.put(UIDs.RTDoseStorage, "RT DOSE");
        REC_TYPE_MAP.put(UIDs.RTStructureSetStorage, "RT STRUCTURE SET");
        REC_TYPE_MAP.put(UIDs.RTBeamsTreatmentRecordStorage, "RT TREAT RECORD");
        REC_TYPE_MAP.put(UIDs.RTPlanStorage, "RT PLAN");
        REC_TYPE_MAP.put(UIDs.RTBrachyTreatmentRecordStorage, "RT TREAT RECORD");
        REC_TYPE_MAP.put(UIDs.RTTreatmentSummaryRecordStorage, "RT TREAT RECORD");
    }
}
