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
package org.dcm4che2.media;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since 16.07.2006
 */

public class BasicApplicationProfile implements ApplicationProfile {

    private static final int[] PATIENT_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.PATIENTS_NAME, Tag.PATIENT_ID };

    private static final int[] STUDY_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.STUDY_DATE, Tag.STUDY_TIME, Tag.ACCESSION_NUMBER,
            Tag.STUDY_DESCRIPTION, Tag.STUDY_INSTANCE_UID, Tag.STUDY_ID };

    private static final int[] SERIES_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.MODALITY, Tag.SERIES_INSTANCE_UID, Tag.SERIES_NUMBER };

    private static final int[] IMAGE_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.INSTANCE_NUMBER };

    private static final int[] RT_DOSE_SET = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.INSTANCE_NUMBER, Tag.DOSE_SUMMATION_TYPE };

    private static final int[] RT_STRUCTURE_SET = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.INSTANCE_NUMBER, Tag.STRUCTURE_SET_LABEL, Tag.STRUCTURE_SET_DATE,
            Tag.STRUCTURE_SET_TIME };

    private static final int[] RT_PLAN_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.INSTANCE_NUMBER, Tag.RT_PLAN_LABEL, Tag.RT_PLAN_DATE,
            Tag.RT_PLAN_TIME };

    private static final int[] RT_TREATMENT_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.INSTANCE_NUMBER, Tag.TREATMENT_DATE, Tag.TREATMENT_TIME };

    private static final int[] PRESENTATION_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.REFERENCED_SERIES_SEQUENCE, Tag.INSTANCE_NUMBER,
            Tag.CONTENT_LABEL, Tag.CONTENT_DESCRIPTION,
            Tag.PRESENTATION_CREATION_DATE, Tag.PRESENTATION_CREATION_TIME,
            Tag.CONTENT_CREATORS_NAME, };

    private static final int[] SR_DOCUMENT_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.CONTENT_DATE, Tag.CONTENT_TIME, Tag.INSTANCE_NUMBER,
            Tag.VERIFICATION_DATE_TIME, Tag.CONCEPT_NAME_CODE_SEQUENCE,
            Tag.COMPLETION_FLAG, Tag.VERIFICATION_FLAG, };

    private static final int[] KEY_OBJECT_DOCUMENT_KEYS = {
            Tag.SPECIFIC_CHARACTER_SET, Tag.CONTENT_DATE, Tag.CONTENT_TIME,
            Tag.INSTANCE_NUMBER, Tag.CONCEPT_NAME_CODE_SEQUENCE };

    private static final int[] WAVEFROM_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.CONTENT_DATE, Tag.CONTENT_TIME, Tag.INSTANCE_NUMBER };

    private static final int[] SPECTROSCOPY_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.IMAGE_TYPE, Tag.CONTENT_DATE, Tag.CONTENT_TIME,
            Tag.REFERENCED_IMAGE_EVIDENCE_SEQUENCE, Tag.INSTANCE_NUMBER,
            Tag.NUMBER_OF_FRAMES, Tag.ROWS, Tag.COLUMNS, Tag.DATA_POINT_ROWS,
            Tag.DATA_POINT_COLUMNS };

    private static final int[] RAWDATA_KEYS = WAVEFROM_KEYS;

    private static final int[] REGISTRATION_KEYS = { Tag.SPECIFIC_CHARACTER_SET,
            Tag.CONTENT_DATE, Tag.CONTENT_TIME, Tag.INSTANCE_NUMBER,
            Tag.CONTENT_LABEL, Tag.CONTENT_DESCRIPTION, Tag.CONTENT_CREATORS_NAME };

    private static final int[] FIDUCIAL_KEYS = REGISTRATION_KEYS;

    private static final int[] HANGING_PROTOCOL_KEYS = {
            Tag.SPECIFIC_CHARACTER_SET, Tag.HANGING_PROTOCOL_NAME,
            Tag.HANGING_PROTOCOL_DESCRIPTION, Tag.HANGING_PROTOCOL_LEVEL,
            Tag.HANGING_PROTOCOL_CREATOR, Tag.HANGING_PROTOCOL_CREATION_DATETIME,
            Tag.HANGING_PROTOCOL_DEFINITION_SEQUENCE,
            Tag.NUMBER_OF_PRIORS_REFERENCED,
            Tag.HANGING_PROTOCOL_USER_IDENTIFICATION_CODE_SEQUENCE };

    private static final int[] ENCAPSULATED_DOCUMENT_KEYS = {
            Tag.SPECIFIC_CHARACTER_SET, Tag.CONTENT_DATE, Tag.CONTENT_TIME,
            Tag.INSTANCE_NUMBER, Tag.CONCEPT_NAME_CODE_SEQUENCE,
            Tag.DOCUMENT_TITLE, Tag.MIME_TYPE_OF_ENCAPSULATED_DOCUMENT };

    private static final int[] HL7_STRUCTURED_DOCUMENT_KEYS = {
            Tag.SPECIFIC_CHARACTER_SET, Tag.HL7_INSTANCE_IDENTIFIER,
            Tag.HL7_DOCUMENT_EFFECTIVE_TIME,
            Tag.HL7_DOCUMENT_TYPE_CODE_SEQUENCE, Tag.DOCUMENT_TITLE };

    private static final int[] REAL_WORLD_VALUE_MAPPING_KEYS = REGISTRATION_KEYS;

    private int[] patientKeys = PATIENT_KEYS;
    private int[] studyKeys = STUDY_KEYS;
    private int[] seriesKeys = SERIES_KEYS;
    private int[] imageKeys = IMAGE_KEYS;
    private int[] rtDoseKeys = RT_DOSE_SET;
    private int[] rtStructureSetKeys = RT_STRUCTURE_SET;
    private int[] rtPlanKeys = RT_PLAN_KEYS;
    private int[] rtTreatmentRecordKeys = RT_TREATMENT_KEYS;
    private int[] presentationKeys = PRESENTATION_KEYS;
    private int[] waveformKeys = WAVEFROM_KEYS;
    private int[] srDocumentKeys = SR_DOCUMENT_KEYS;
    private int[] keyObjectDocumentKeys = KEY_OBJECT_DOCUMENT_KEYS;
    private int[] spectroscopyKeys = SPECTROSCOPY_KEYS;
    private int[] rawdataKeys = RAWDATA_KEYS;
    private int[] registrationKeys = REGISTRATION_KEYS;
    private int[] fiducialKeys = FIDUCIAL_KEYS;
    private int[] hangingProtocolKeys = HANGING_PROTOCOL_KEYS;
    private int[] encapsulatedDocumentKeys = ENCAPSULATED_DOCUMENT_KEYS;
    private int[] hl7StructuredDocumentKeys = HL7_STRUCTURED_DOCUMENT_KEYS;
    private int[] realWorldValueMappingKeys = REAL_WORLD_VALUE_MAPPING_KEYS;

    public final int[] getEncapsulatedDocumentKeys() {
        return (int[]) encapsulatedDocumentKeys.clone();
    }
    
    public final void setEncapsulatedDocumentKeys(int[] encapsulatedDocumentKeys) {
        this.encapsulatedDocumentKeys = (int[]) encapsulatedDocumentKeys.clone();
    }
    
    public final int[] getFiducialKeys() {
        return (int[]) fiducialKeys.clone();
    }
    
    public final void setFiducialKeys(int[] fiducialKeys) {
        this.fiducialKeys = (int[]) fiducialKeys.clone();
    }
    
    public final int[] getHangingProtocolKeys() {
        return (int[]) hangingProtocolKeys.clone();
    }
    
    public final void setHangingProtocolKeys(int[] hangingProtocolKeys) {
        this.hangingProtocolKeys = (int[]) hangingProtocolKeys.clone();
    }
    
    public final int[] getHl7StructuredDocumentKeys() {
        return (int[]) hl7StructuredDocumentKeys.clone();
    }
    
    public final void setHl7StructuredDocumentKeys(
            int[] hl7StructuredDocumentKeys) {
        this.hl7StructuredDocumentKeys = (int[]) hl7StructuredDocumentKeys.clone();
    }
    
    public final int[] getImageKeys() {
        return (int[]) imageKeys.clone();
    }
    
    public final void setImageKeys(int[] imageKeys) {
        this.imageKeys = (int[]) imageKeys.clone();
    }
    
    public final int[] getKeyObjectDocumentKeys() {
        return (int[]) keyObjectDocumentKeys.clone();
    }
    
    public final void setKeyObjectDocumentKeys(int[] keyObjectDocumentKeys) {
        this.keyObjectDocumentKeys = (int[]) keyObjectDocumentKeys.clone();
    }
    
    public final int[] getPatientKeys() {
        return (int[]) patientKeys.clone();
    }
    
    public final void setPatientKeys(int[] patientKeys) {
        this.patientKeys = (int[]) patientKeys.clone();
    }
    
    public final int[] getPresentationKeys() {
        return (int[]) presentationKeys.clone();
    }
    
    public final void setPresentationKeys(int[] presentationKeys) {
        this.presentationKeys = (int[]) presentationKeys.clone();
    }
    
    public final int[] getRawdataKeys() {
        return (int[]) rawdataKeys.clone();
    }
    
    public final void setRawdataKeys(int[] rawdataKeys) {
        this.rawdataKeys = (int[]) rawdataKeys.clone();
    }
    
    public final int[] getRealWorldValueMappingKeys() {
        return (int[]) realWorldValueMappingKeys.clone();
    }
    
    public final void setRealWorldValueMappingKeys(
            int[] realWorldValueMappingKeys) {
        this.realWorldValueMappingKeys = (int[]) realWorldValueMappingKeys.clone();
    }
    
    public final int[] getRegistrationKeys() {
        return (int[]) registrationKeys.clone();
    }
    
    public final void setRegistrationKeys(int[] registrationKeys) {
        this.registrationKeys = (int[]) registrationKeys.clone();
    }
    
    public final int[] getRtDoseKeys() {
        return (int[]) rtDoseKeys.clone();
    }
    
    public final void setRtDoseKeys(int[] rtDoseKeys) {
        this.rtDoseKeys = (int[]) rtDoseKeys.clone();
    }
    
    public final int[] getRtPlanKeys() {
        return (int[]) rtPlanKeys.clone();
    }
    
    public final void setRtPlanKeys(int[] rtPlanKeys) {
        this.rtPlanKeys = (int[]) rtPlanKeys.clone();
    }
    
    public final int[] getRtStructureSetKeys() {
        return (int[]) rtStructureSetKeys.clone();
    }
    
    public final void setRtStructureSetKeys(int[] rtStructureSetKeys) {
        this.rtStructureSetKeys = (int[]) rtStructureSetKeys.clone();
    }
    
    public final int[] getRtTreatmentRecordKeys() {
        return (int[]) rtTreatmentRecordKeys.clone();
    }
    
    public final void setRtTreatmentRecordKeys(int[] rtTreatmentRecordKeys) {
        this.rtTreatmentRecordKeys = (int[]) rtTreatmentRecordKeys.clone();
    }
    
    public final int[] getSeriesKeys() {
        return (int[]) seriesKeys.clone();
    }
    
    public final void setSeriesKeys(int[] seriesKeys) {
        this.seriesKeys = (int[]) seriesKeys.clone();
    }
    
    public final int[] getSpectroscopyKeys() {
        return (int[]) spectroscopyKeys.clone();
    }
    
    public final void setSpectroscopyKeys(int[] spectroscopyKeys) {
        this.spectroscopyKeys = (int[]) spectroscopyKeys.clone();
    }
    
    public final int[] getSrDocumentKeys() {
        return (int[]) srDocumentKeys.clone();
    }
    
    public final void setSrDocumentKeys(int[] srDocumentKeys) {
        this.srDocumentKeys = (int[]) srDocumentKeys.clone();
    }
    
    public final int[] getStudyKeys() {
        return (int[]) studyKeys.clone();
    }
    
    public final void setStudyKeys(int[] studyKeys) {
        this.studyKeys = (int[]) studyKeys.clone();
    }
    
    public final int[] getWaveformKeys() {
        return (int[]) waveformKeys.clone();
    }
    
    public final void setWaveformKeys(int[] waveformKeys) {
        this.waveformKeys = (int[]) waveformKeys.clone();
    }
    
    private DicomObject makeRecord(String type, int[] keys, DicomObject dcmobj) {
        DicomObject rec = new BasicDicomObject();
        rec.putString(Tag.DIRECTORY_RECORD_TYPE, VR.CS, type);
        dcmobj.subSet(keys).copyTo(rec);
        return rec;
    }

    private DicomObject makeRecord(String type, int[] keys, DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(type,  keys, dcmobj);
        rec.putStrings(Tag.REFERENCED_FILE_ID, VR.CS, fileIDs);
        rec.putString(Tag.REFERENCED_SOP_INSTANCE_UID_IN_FILE, VR.UI,
                dcmobj.getString(Tag.MEDIA_STORAGE_SOP_INSTANCE_UID));
        rec.putString(Tag.REFERENCED_SOP_CLASS_UID_IN_FILE, VR.UI,
                dcmobj.getString(Tag.MEDIA_STORAGE_SOP_CLASS_UID));
        rec.putString(Tag.REFERENCED_TRANSFER_SYNTAX_UID_IN_FILE, VR.UI,
                dcmobj.getString(Tag.TRANSFER_SYNTAX_UID));
        String relcuid = dcmobj.getString(Tag.RELATED_GENERAL_SOP_CLASS_UID);
        if (relcuid != null) {
            rec.putString(
                    Tag.REFERENCED_RELATED_GENERAL_SOP_CLASS_UID_IN_FILE, VR.UI,
                    relcuid);
        }
        return rec;
    }
        
    public DicomObject makePatientDirectoryRecord(DicomObject dcmobj) {
        DicomObject rec = makeRecord(DirectoryRecordType.PATIENT, patientKeys,
                dcmobj);
        if (!rec.contains(Tag.PATIENTS_NAME)) {
            rec.putNull(Tag.PATIENTS_NAME, VR.PN);
        }
        if (!rec.containsValue(Tag.PATIENT_ID)) {
            rec.putString(Tag.PATIENT_ID, VR.LO, dcmobj
                    .getString(Tag.STUDY_INSTANCE_UID));
        }
        return rec;
    }

    public DicomObject makeStudyDirectoryRecord(DicomObject dcmobj) {
        DicomObject rec = makeRecord(DirectoryRecordType.STUDY, studyKeys,
                dcmobj);
        return rec;
    }

    public DicomObject makeSeriesDirectoryRecord(DicomObject dcmobj) {
        DicomObject rec = makeRecord(DirectoryRecordType.SERIES, seriesKeys,
                dcmobj);
        return rec;
    }

    public DicomObject makeInstanceDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        String cuid = dcmobj.getString(Tag.MEDIA_STORAGE_SOP_CLASS_UID);
        switch (cuid.hashCode()) {
        case -525617006:
            if (UID.RAW_DATA_STORAGE.equals(cuid)) {
                return makeRawDataDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case -525617005:
            if (UID.REAL_WORLD_VALUE_MAPPING_STORAGE.equals(cuid)) {
                return makeRealWorldValueMappingDirectorRecord(dcmobj, fileIDs);
            }
            break;
        case 789790566:
            if (UID.ENCAPSULATED_PDF_STORAGE.equals(cuid)) {
                return makeEncapsulatedDocumentDirectorRecord(dcmobj, fileIDs);
            }
            break;
        case 792796575:
            if (UID.RT_DOSE_STORAGE.equals(cuid)) {
                return makeRTDoseDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 792796576:
            if (UID.RT_STRUCTURE_SET_STORAGE.equals(cuid)) {
                return makeRTStructuredSetDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 792796577:
            if (UID.RT_BEAMS_TREATMENT_RECORD_STORAGE.equals(cuid)) {
                return makeRTTreatmentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 792796578:
            if (UID.RT_PLAN_STORAGE.equals(cuid)) {
                return makeRTPlanDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 792796579:
            if (UID.RT_BRACHY_TREATMENT_RECORD_STORAGE.equals(cuid)) {
                return makeRTTreatmentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 792796580:
            if (UID.RT_TREATMENT_SUMMARY_RECORD_STORAGE.equals(cuid)) {
                return makeRTTreatmentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 792796581:
            if (UID.RT_ION_PLAN_STORAGE.equals(cuid)) {
                return makeRTPlanDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 792796582:
            if (UID.RT_ION_BEAMS_TREATMENT_RECORD_STORAGE.equals(cuid)) {
                return makeRTTreatmentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796487868:
            if (UID.BASIC_TEXT_SR.equals(cuid)) {
                return makeSRDocumentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796487900:
            if (UID.ENHANCED_SR.equals(cuid)) {
                return makeSRDocumentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796487932:
            if (UID.COMPREHENSIVE_SR.equals(cuid)) {
                return makeSRDocumentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796487960:
            if (UID.PROCEDURE_LOG_STORAGE.equals(cuid)) {
                return makeSRDocumentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796487991:
            if (UID.MAMMOGRAPHY_CAD_SR.equals(cuid)) {
                return makeSRDocumentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796488000:
            if (UID.KEY_OBJECT_SELECTION_DOCUMENT.equals(cuid)) {
                return makeKeyObjectDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796488027:
            if (UID.CHEST_CAD_SR.equals(cuid)) {
                return makeSRDocumentDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 796488029:
            if (UID.X_RAY_RADIATION_DOSE_SR.equals(cuid)) {
                return makeSRDocumentDirectoryRecord(dcmobj, fileIDs);
            }
            break;            
        case 797116269:
            if (UID._12_LEAD_ECG_WAVEFORM_STORAGE.equals(cuid)) {
                return makeWaveformDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 797116270:
            if (UID.GENERAL_ECG_WAVEFORM_STORAGE.equals(cuid)) {
                return makeWaveformDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 797116271:
            if (UID.AMBULATORY_ECG_WAVEFORM_STORAGE.equals(cuid)) {
                return makeWaveformDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 797117230:
            if (UID.HEMODYNAMIC_WAVEFORM_STORAGE.equals(cuid)) {
                return makeWaveformDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 797118191:
            if (UID.CARDIAC_ELECTROPHYSIOLOGY_WAVEFORM_STORAGE.equals(cuid)) {
                return makeWaveformDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 797119152:
            if (UID.BASIC_VOICE_AUDIO_WAVEFORM_STORAGE.equals(cuid)) {
                return makeWaveformDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 885739878:
            if (UID.MR_SPECTROSCOPY_STORAGE.equals(cuid)) {
                return makeSpectroscopyDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 1688045877:
            if (UID.GRAYSCALE_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS.equals(cuid)) {
                return makePresentationStateDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 1688045878:
            if (UID.COLOR_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS.equals(cuid)) {
                return makePresentationStateDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 1688045879:
            if (UID.PSEUDO_COLOR_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS.equals(cuid)) {
                return makePresentationStateDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 1688045880:
            if (UID.BLENDING_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS.equals(cuid)) {
                return makePresentationStateDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 1688199637:
            if (UID.SPATIAL_REGISTRATION_STORAGE.equals(cuid)) {
                return makeRegistrationDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        case 1688199638:
            if (UID.SPATIAL_FIDUCIALS_STORAGE.equals(cuid)) {
                return makeFiducialDirectoryRecord(dcmobj, fileIDs);
            }
            break;
        }
        return makeImageDirectoryRecord(dcmobj, fileIDs);
    }

    public DicomObject makeImageDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.IMAGE, imageKeys,
                dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeRTDoseDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.RT_DOSE, rtDoseKeys,
                dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeRTStructuredSetDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.RT_STRUCTURE_SET,
                rtStructureSetKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeRTPlanDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.RT_PLAN, rtPlanKeys,
                dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeRTTreatmentDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.RT_TREAT_RECORD,
                rtTreatmentRecordKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makePresentationStateDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.PRESENTATION,
                presentationKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeWaveformDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.WAVEFORM,
                waveformKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeSRDocumentDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.SR_DOCUMENT,
                srDocumentKeys, dcmobj, fileIDs);
        copyConceptNameModifiers(dcmobj, rec);
        return rec;
    }

    public DicomObject makeKeyObjectDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.KEY_OBJECT_DOC,
                keyObjectDocumentKeys, dcmobj, fileIDs);
        copyConceptNameModifiers(dcmobj, rec);
        return rec;
    }

    private void copyConceptNameModifiers(DicomObject dcmobj, DicomObject rec) {
        DicomElement objsq = dcmobj.get(Tag.CONTENT_SEQUENCE);
        DicomElement recsq = null;
        DicomObject item;
        for (int i = 0, n = objsq.countItems(); i < n; i++) {
            item = objsq.getDicomObject(i);
            if ("HAS CONCEPT MOD".equals(item.getString(Tag.RELATIONSHIP_TYPE))) {
                if (recsq == null) { // lazy sequence creation
                    recsq = rec.putSequence(Tag.CONTENT_SEQUENCE);
                }
                recsq.addDicomObject(item);
            }
        }
    }

    public DicomObject makeSpectroscopyDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.SPECTROSCOPY,
                spectroscopyKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeRawDataDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.RAW_DATA, rawdataKeys,
                dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeRegistrationDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.REGISTRATION,
                registrationKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeFiducialDirectoryRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.FIDUCIAL,
                fiducialKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeHangingProtocolDirectorRecord(DicomObject dcmobj,
            String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.HANGING_PROTOCOL,
                hangingProtocolKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeEncapsulatedDocumentDirectorRecord(
            DicomObject dcmobj, String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.ENCAP_DOC,
                encapsulatedDocumentKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeHL7StructuredDocumentDirectorRecord(
            DicomObject dcmobj, String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.HL7_STRUC_DOC,
                hl7StructuredDocumentKeys, dcmobj, fileIDs);
        return rec;
    }

    public DicomObject makeRealWorldValueMappingDirectorRecord(
            DicomObject dcmobj, String[] fileIDs) {
        DicomObject rec = makeRecord(DirectoryRecordType.VALUE_MAP,
                realWorldValueMappingKeys, dcmobj, fileIDs);
        return rec;
    }

}
