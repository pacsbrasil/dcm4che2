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

package org.dcm4che2.data;

/** Provides tag constants.*/
public class UID {

    /** Private constructor */
    private UID() {
    }
    
    public static final String forName(String name) {
       try {
          return (String) UID.class.getField(name).get(null);
       } catch (IllegalAccessException e) {
          throw new Error(e);
       } catch (NoSuchFieldException e) {
          throw new IllegalArgumentException("Unknown UID Name: " + name);
       }
    }

    /** Private Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PRIVATE_STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_FIND = "1.2.40.0.13.1.5.1.4.1.2.2.1";

    /** Private Blocked Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PRIVATE_BLOCKED_STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_FIND = "1.2.40.0.13.1.5.1.4.1.2.2.1.1";

    /** Private Virtual Multiframe Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PRIVATE_VIRTUAL_MULTIFRAME_STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_FIND = "1.2.40.0.13.1.5.1.4.1.2.2.1.2";

    /** Verification SOP Class - SOP Class */
    public static final String VERIFICATION_SOP_CLASS = "1.2.840.10008.1.1";

    /** Implicit VR Little Endian - Transfer Syntax */
    public static final String IMPLICIT_VR_LITTLE_ENDIAN = "1.2.840.10008.1.2";

    /** Explicit VR Little Endian - Transfer Syntax */
    public static final String EXPLICIT_VR_LITTLE_ENDIAN = "1.2.840.10008.1.2.1";

    /** Deflated Explicit VR Little Endian - Transfer Syntax */
    public static final String DEFLATED_EXPLICIT_VR_LITTLE_ENDIAN = "1.2.840.10008.1.2.1.99";

    /** Explicit VR Big Endian - Transfer Syntax */
    public static final String EXPLICIT_VR_BIG_ENDIAN = "1.2.840.10008.1.2.2";

    /** MPEG2 Main Profile @ Main Level - Transfer Syntax */
    public static final String MPEG2 = "1.2.840.10008.1.2.4.100";

    /** JPEG Baseline (Process 1) - Transfer Syntax */
    public static final String JPEG_BASELINE_1 = "1.2.840.10008.1.2.4.50";

    /** JPEG Extended (Process 2 & 4) - Transfer Syntax */
    public static final String JPEG_EXTENDED_2_4 = "1.2.840.10008.1.2.4.51";

    /** JPEG Extended (Process 3 & 5) (Retired) - Transfer Syntax */
    public static final String JPEG_EXTENDED_3_5_RETIRED = "1.2.840.10008.1.2.4.52";

    /** JPEG Spectral Selection, Non-Hierarchical (Process 6 & 8) (Retired) - Transfer Syntax */
    public static final String JPEG_SPECTRAL_SELECTION_NON_HIERARCHICAL_6_8_RETIRED = "1.2.840.10008.1.2.4.53";

    /** JPEG Spectral Selection, Non-Hierarchical (Process 7 & 9) (Retired) - Transfer Syntax */
    public static final String JPEG_SPECTRAL_SELECTION_NON_HIERARCHICAL_7_9_RETIRED = "1.2.840.10008.1.2.4.54";

    /** JPEG Full Progression, Non-Hierarchical (Process 10 & 12) (Retired) - Transfer Syntax */
    public static final String JPEG_FULL_PROGRESSION_NON_HIERARCHICAL_10_12_RETIRED = "1.2.840.10008.1.2.4.55";

    /** JPEG Full Progression, Non-Hierarchical (Process 11 & 13) (Retired) - Transfer Syntax */
    public static final String JPEG_FULL_PROGRESSION_NON_HIERARCHICAL_11_13_RETIRED = "1.2.840.10008.1.2.4.56";

    /** JPEG Lossless, Non-Hierarchical (Process 14) - Transfer Syntax */
    public static final String JPEG_LOSSLESS_NON_HIERARCHICAL_14 = "1.2.840.10008.1.2.4.57";

    /** JPEG Lossless, Non-Hierarchical (Process 15) (Retired) - Transfer Syntax */
    public static final String JPEG_LOSSLESS_NON_HIERARCHICAL_15_RETIRED = "1.2.840.10008.1.2.4.58";

    /** JPEG Extended, Hierarchical (Process 16 & 18) (Retired) - Transfer Syntax */
    public static final String JPEG_EXTENDED_HIERARCHICAL_16_18_RETIRED = "1.2.840.10008.1.2.4.59";

    /** JPEG Extended, Hierarchical (Process 17 & 19) (Retired) - Transfer Syntax */
    public static final String JPEG_EXTENDED_HIERARCHICAL_17_19_RETIRED = "1.2.840.10008.1.2.4.60";

    /** JPEG Spectral Selection, Hierarchical (Process 20 & 22) (Retired) - Transfer Syntax */
    public static final String JPEG_SPECTRAL_SELECTION_HIERARCHICAL_20_22_RETIRED = "1.2.840.10008.1.2.4.61";

    /** JPEG Spectral Selection, Hierarchical (Process 21 & 23) (Retired) - Transfer Syntax */
    public static final String JPEG_SPECTRAL_SELECTION_HIERARCHICAL_21_23_RETIRED = "1.2.840.10008.1.2.4.62";

    /** JPEG Full Progression, Hierarchical (Process 24 & 26) (Retired) - Transfer Syntax */
    public static final String JPEG_FULL_PROGRESSION_HIERARCHICAL_24_26_RETIRED = "1.2.840.10008.1.2.4.63";

    /** JPEG Full Progression, Hierarchical (Process 25 & 27) (Retired) - Transfer Syntax */
    public static final String JPEG_FULL_PROGRESSION_HIERARCHICAL_25_27_RETIRED = "1.2.840.10008.1.2.4.64";

    /** JPEG Lossless, Hierarchical (Process 28) (Retired) - Transfer Syntax */
    public static final String JPEG_LOSSLESS_HIERARCHICAL_28_RETIRED = "1.2.840.10008.1.2.4.65";

    /** JPEG Lossless, Hierarchical (Process 29) (Retired) - Transfer Syntax */
    public static final String JPEG_LOSSLESS_HIERARCHICAL_29_RETIRED = "1.2.840.10008.1.2.4.66";

    /** JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1]) - Transfer Syntax */
    public static final String JPEG_LOSSLESS = "1.2.840.10008.1.2.4.70";

    /** JPEG-LS Lossless Image Compression - Transfer Syntax */
    public static final String JPEG_LS_LOSSLESS = "1.2.840.10008.1.2.4.80";

    /** JPEG-LS Lossy (Near-Lossless) Image Compression - Transfer Syntax */
    public static final String JPEG_LS_LOSSY_NEAR_LOSSLESS = "1.2.840.10008.1.2.4.81";

    /** JPEG 2000 Image Compression (Lossless Only) - Transfer Syntax */
    public static final String JPEG_2000_LOSSLESS_ONLY = "1.2.840.10008.1.2.4.90";

    /** JPEG 2000 Image Compression - Transfer Syntax */
    public static final String JPEG_2000 = "1.2.840.10008.1.2.4.91";

    /** JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only) - Transfer Syntax */
    public static final String JPEG_2000_PART_2_MULTI_COMPONENT_LOSSLESS_ONLY = "1.2.840.10008.1.2.4.92";

    /** JPEG 2000 Part 2 Multi-component Image Compression - Transfer Syntax */
    public static final String JPEG_2000_PART_2_MULTI_COMPONENT = "1.2.840.10008.1.2.4.93";

    /** JPIP Referenced - Transfer Syntax */
    public static final String JPIP_REFERENCED = "1.2.840.10008.1.2.4.94";

    /** JPIP Referenced Deflate - Transfer Syntax */
    public static final String JPIP_REFERENCED_DEFLATE = "1.2.840.10008.1.2.4.95";

    /** RLE Lossless - Transfer Syntax */
    public static final String RLE_LOSSLESS = "1.2.840.10008.1.2.5";

    /** RFC 2557 MIME encapsulation - Transfer Syntax */
    public static final String RFC_2557_MIME_ENCAPSULATION = "1.2.840.10008.1.2.6.1";

    /** Storage Commitment Push Model SOP Class - SOP Class */
    public static final String STORAGE_COMMITMENT_PUSH_MODEL_SOP_CLASS = "1.2.840.10008.1.20.1";

    /** Storage Commitment Push Model SOP Instance - Well-known SOP Instance */
    public static final String STORAGE_COMMITMENT_PUSH_MODEL_SOP_INSTANCE = "1.2.840.10008.1.20.1.1";

    /** Storage Commitment Pull Model SOP Class (Retired) - SOP Class */
    public static final String STORAGE_COMMITMENT_PULL_MODEL_SOP_CLASS_RETIRED = "1.2.840.10008.1.20.2";

    /** Storage Commitment Pull Model SOP Instance (Retired) - Well-known SOP Instance */
    public static final String STORAGE_COMMITMENT_PULL_MODEL_SOP_INSTANCE_RETIRED = "1.2.840.10008.1.20.2.1";

    /** Media Storage Directory Storage - SOP Class */
    public static final String MEDIA_STORAGE_DIRECTORY_STORAGE = "1.2.840.10008.1.3.10";

    /** Talairach Brain Atlas Frame of Reference - Well-known frame of reference */
    public static final String TALAIRACH_BRAIN_ATLAS_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.1";

    /** SPM2 GRAY Frame of Reference - Well-known frame of reference */
    public static final String SPM2_GRAY_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.10";

    /** SPM2 WHITE Frame of Reference - Well-known frame of reference */
    public static final String SPM2_WHITE_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.11";

    /** SPM2 CSF Frame of Reference - Well-known frame of reference */
    public static final String SPM2_CSF_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.12";

    /** SPM2 BRAINMASK Frame of Reference - Well-known frame of reference */
    public static final String SPM2_BRAINMASK_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.13";

    /** SPM2 AVG305T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2_AVG305T1_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.14";

    /** SPM2 AVG152T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2_AVG152T1_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.15";

    /** SPM2 AVG152T2 Frame of Reference - Well-known frame of reference */
    public static final String SPM2_AVG152T2_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.16";

    /** SPM2 AVG152PD Frame of Reference - Well-known frame of reference */
    public static final String SPM2_AVG152PD_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.17";

    /** SPM2 SINGLESUBJT1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2_SINGLESUBJT1_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.18";

    /** SPM2 T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2_T1_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.2";

    /** SPM2 T2 Frame of Reference - Well-known frame of reference */
    public static final String SPM2_T2_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.3";

    /** SPM2 PD Frame of Reference - Well-known frame of reference */
    public static final String SPM2_PD_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.4";

    /** SPM2 EPI Frame of Reference - Well-known frame of reference */
    public static final String SPM2_EPI_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.5";

    /** SPM2 FIL T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2_FIL_T1_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.6";

    /** SPM2 PET Frame of Reference - Well-known frame of reference */
    public static final String SPM2_PET_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.7";

    /** SPM2 TRANSM Frame of Reference - Well-known frame of reference */
    public static final String SPM2_TRANSM_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.8";

    /** SPM2 SPECT Frame of Reference - Well-known frame of reference */
    public static final String SPM2_SPECT_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.1.9";

    /** ICBM 452 T1 Frame of Reference - Well-known frame of reference */
    public static final String ICBM_452_T1_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.2.1";

    /** ICBM Single Subject MRI Frame of Reference - Well-known frame of reference */
    public static final String ICBM_SINGLE_SUBJECT_MRI_FRAME_OF_REFERENCE = "1.2.840.10008.1.4.2.2";

    /** Procedural Event Logging SOP Class - SOP Class */
    public static final String PROCEDURAL_EVENT_LOGGING_SOP_CLASS = "1.2.840.10008.1.40";

    /** Procedural Event Logging SOP Instance - Well-known SOP Instance */
    public static final String PROCEDURAL_EVENT_LOGGING_SOP_INSTANCE = "1.2.840.10008.1.40.1";

    /** Basic Study Content Notification SOP Class (Retired) - SOP Class */
    public static final String BASIC_STUDY_CONTENT_NOTIFICATION_SOP_CLASS_RETIRED = "1.2.840.10008.1.9";

    /** dicomDeviceName - LDAP OID */
    public static final String dicomDeviceName = "1.2.840.10008.15.0.3.1";

    /** dicomAssociationInitiator - LDAP OID */
    public static final String dicomAssociationInitiator = "1.2.840.10008.15.0.3.10";

    /** dicomAssociationAcceptor - LDAP OID */
    public static final String dicomAssociationAcceptor = "1.2.840.10008.15.0.3.11";

    /** dicomHostname - LDAP OID */
    public static final String dicomHostname = "1.2.840.10008.15.0.3.12";

    /** dicomPort - LDAP OID */
    public static final String dicomPort = "1.2.840.10008.15.0.3.13";

    /** dicomSOPClass - LDAP OID */
    public static final String dicomSOPClass = "1.2.840.10008.15.0.3.14";

    /** dicomTransferRole - LDAP OID */
    public static final String dicomTransferRole = "1.2.840.10008.15.0.3.15";

    /** dicomTransferSyntax - LDAP OID */
    public static final String dicomTransferSyntax = "1.2.840.10008.15.0.3.16";

    /** dicomPrimaryDeviceType - LDAP OID */
    public static final String dicomPrimaryDeviceType = "1.2.840.10008.15.0.3.17";

    /** dicomRelatedDeviceReference - LDAP OID */
    public static final String dicomRelatedDeviceReference = "1.2.840.10008.15.0.3.18";

    /** dicomPreferredCalledAETitle - LDAP OID */
    public static final String dicomPreferredCalledAETitle = "1.2.840.10008.15.0.3.19";

    /** dicomDescription - LDAP OID */
    public static final String dicomDescription = "1.2.840.10008.15.0.3.2";

    /** dicomTLSCyphersuite - LDAP OID */
    public static final String dicomTLSCyphersuite = "1.2.840.10008.15.0.3.20";

    /** dicomAuthorizedNodeCertificateReference - LDAP OID */
    public static final String dicomAuthorizedNodeCertificateReference = "1.2.840.10008.15.0.3.21";

    /** dicomThisNodeCertificateReference - LDAP OID */
    public static final String dicomThisNodeCertificateReference = "1.2.840.10008.15.0.3.22";

    /** dicomInstalled - LDAP OID */
    public static final String dicomInstalled = "1.2.840.10008.15.0.3.23";

    /** dicomStationName - LDAP OID */
    public static final String dicomStationName = "1.2.840.10008.15.0.3.24";

    /** dicomDeviceSerialNumber - LDAP OID */
    public static final String dicomDeviceSerialNumber = "1.2.840.10008.15.0.3.25";

    /** dicomInstitutionName - LDAP OID */
    public static final String dicomInstitutionName = "1.2.840.10008.15.0.3.26";

    /** dicomInstitutionAddress - LDAP OID */
    public static final String dicomInstitutionAddress = "1.2.840.10008.15.0.3.27";

    /** dicomInstitutionDepartmentName - LDAP OID */
    public static final String dicomInstitutionDepartmentName = "1.2.840.10008.15.0.3.28";

    /** dicomIssuerOfPatientID - LDAP OID */
    public static final String dicomIssuerOfPatientID = "1.2.840.10008.15.0.3.29";

    /** dicomManufacturer - LDAP OID */
    public static final String dicomManufacturer = "1.2.840.10008.15.0.3.3";

    /** dicomPreferredCallingAETitle - LDAP OID */
    public static final String dicomPreferredCallingAETitle = "1.2.840.10008.15.0.3.30";

    /** dicomSupportedCharacterSet - LDAP OID */
    public static final String dicomSupportedCharacterSet = "1.2.840.10008.15.0.3.31";

    /** dicomManufacturerModelName - LDAP OID */
    public static final String dicomManufacturerModelName = "1.2.840.10008.15.0.3.4";

    /** dicomSoftwareVersion - LDAP OID */
    public static final String dicomSoftwareVersion = "1.2.840.10008.15.0.3.5";

    /** dicomVendorData - LDAP OID */
    public static final String dicomVendorData = "1.2.840.10008.15.0.3.6";

    /** dicomAETitle - LDAP OID */
    public static final String dicomAETitle = "1.2.840.10008.15.0.3.7";

    /** dicomNetworkConnectionReference - LDAP OID */
    public static final String dicomNetworkConnectionReference = "1.2.840.10008.15.0.3.8";

    /** dicomApplicationCluster - LDAP OID */
    public static final String dicomApplicationCluster = "1.2.840.10008.15.0.3.9";

    /** dicomConfigurationRoot - LDAP OID */
    public static final String dicomConfigurationRoot = "1.2.840.10008.15.0.4.1";

    /** dicomDevicesRoot - LDAP OID */
    public static final String dicomDevicesRoot = "1.2.840.10008.15.0.4.2";

    /** dicomUniqueAETitlesRegistryRoot - LDAP OID */
    public static final String dicomUniqueAETitlesRegistryRoot = "1.2.840.10008.15.0.4.3";

    /** dicomDevice - LDAP OID */
    public static final String dicomDevice = "1.2.840.10008.15.0.4.4";

    /** dicomNetworkAE - LDAP OID */
    public static final String dicomNetworkAE = "1.2.840.10008.15.0.4.5";

    /** dicomNetworkConnection - LDAP OID */
    public static final String dicomNetworkConnection = "1.2.840.10008.15.0.4.6";

    /** dicomUniqueAETitle - LDAP OID */
    public static final String dicomUniqueAETitle = "1.2.840.10008.15.0.4.7";

    /** dicomTransferCapability - LDAP OID */
    public static final String dicomTransferCapability = "1.2.840.10008.15.0.4.8";

    /** DICOM Controlled Terminology - Coding Scheme */
    public static final String DICOM_CONTROLLED_TERMINOLOGY = "1.2.840.10008.2.16.4";

    /** DICOM UID Registry - DICOM UIDs as a Coding Scheme */
    public static final String DICOM_UID_REGISTRY = "1.2.840.10008.2.6.1";

    /** DICOM Application Context Name - Application Context Name */
    public static final String DICOM_APPLICATION_CONTEXT_NAME = "1.2.840.10008.3.1.1.1";

    /** Detached Patient Management SOP Class (Retired) - SOP Class */
    public static final String DETACHED_PATIENT_MANAGEMENT_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.1.1";

    /** Detached Patient Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String DETACHED_PATIENT_MANAGEMENT_META_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.1.4";

    /** Detached Visit Management SOP Class (Retired) - SOP Class */
    public static final String DETACHED_VISIT_MANAGEMENT_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.2.1";

    /** Detached Study Management SOP Class (Retired) - SOP Class */
    public static final String DETACHED_STUDY_MANAGEMENT_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.3.1";

    /** Study Component Management SOP Class (Retired) - SOP Class */
    public static final String STUDY_COMPONENT_MANAGEMENT_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.3.2";

    /** Modality Performed Procedure Step SOP Class - SOP Class */
    public static final String MODALITY_PERFORMED_PROCEDURE_STEP_SOP_CLASS = "1.2.840.10008.3.1.2.3.3";

    /** Modality Performed Procedure Step Retrieve SOP Class - SOP Class */
    public static final String MODALITY_PERFORMED_PROCEDURE_STEP_RETRIEVE_SOP_CLASS = "1.2.840.10008.3.1.2.3.4";

    /** Modality Performed Procedure Step Notification SOP Class - SOP Class */
    public static final String MODALITY_PERFORMED_PROCEDURE_STEP_NOTIFICATION_SOP_CLASS = "1.2.840.10008.3.1.2.3.5";

    /** Detached Results Management SOP Class (Retired) - SOP Class */
    public static final String DETACHED_RESULTS_MANAGEMENT_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.5.1";

    /** Detached Results Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String DETACHED_RESULTS_MANAGEMENT_META_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.5.4";

    /** Detached Study Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String DETACHED_STUDY_MANAGEMENT_META_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.5.5";

    /** Detached Interpretation Management SOP Class (Retired) - SOP Class */
    public static final String DETACHED_INTERPRETATION_MANAGEMENT_SOP_CLASS_RETIRED = "1.2.840.10008.3.1.2.6.1";

    /** Storage Service Class - Service Class */
    public static final String STORAGE_SERVICE_CLASS = "1.2.840.10008.4.2";

    /** Basic Film Session SOP Class - SOP Class */
    public static final String BASIC_FILM_SESSION_SOP_CLASS = "1.2.840.10008.5.1.1.1";

    /** Print Job SOP Class - SOP Class */
    public static final String PRINT_JOB_SOP_CLASS = "1.2.840.10008.5.1.1.14";

    /** Basic Annotation Box SOP Class - SOP Class */
    public static final String BASIC_ANNOTATION_BOX_SOP_CLASS = "1.2.840.10008.5.1.1.15";

    /** Printer SOP Class - SOP Class */
    public static final String PRINTER_SOP_CLASS = "1.2.840.10008.5.1.1.16";

    /** Printer Configuration Retrieval SOP Class - SOP Class */
    public static final String PRINTER_CONFIGURATION_RETRIEVAL_SOP_CLASS = "1.2.840.10008.5.1.1.16.376";

    /** Printer SOP Instance - Well-known Printer SOP Instance */
    public static final String PRINTER_SOP_INSTANCE = "1.2.840.10008.5.1.1.17";

    /** Printer Configuration Retrieval SOP Instance - Well-known Printer SOP Instance */
    public static final String PRINTER_CONFIGURATION_RETRIEVAL_SOP_INSTANCE = "1.2.840.10008.5.1.1.17.376";

    /** Basic Color Print Management Meta SOP Class - Meta SOP Class */
    public static final String BASIC_COLOR_PRINT_MANAGEMENT_META_SOP_CLASS = "1.2.840.10008.5.1.1.18";

    /** Referenced Color Print Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String REFERENCED_COLOR_PRINT_MANAGEMENT_META_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.18.1";

    /** Basic Film Box SOP Class - SOP Class */
    public static final String BASIC_FILM_BOX_SOP_CLASS = "1.2.840.10008.5.1.1.2";

    /** VOI LUT Box SOP Class - SOP Class */
    public static final String VOI_LUT_BOX_SOP_CLASS = "1.2.840.10008.5.1.1.22";

    /** Presentation LUT SOP Class - SOP Class */
    public static final String PRESENTATION_LUT_SOP_CLASS = "1.2.840.10008.5.1.1.23";

    /** Image Overlay Box SOP Class (Retired) - SOP Class */
    public static final String IMAGE_OVERLAY_BOX_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.24";

    /** Basic Print Image Overlay Box SOP Class (Retired) - SOP Class */
    public static final String BASIC_PRINT_IMAGE_OVERLAY_BOX_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.24.1";

    /** Print Queue SOP Instance (Retired) - Well-known Print Queue SOP Instance */
    public static final String PRINT_QUEUE_SOP_INSTANCE_RETIRED = "1.2.840.10008.5.1.1.25";

    /** Print Queue Management SOP Class (Retired) - SOP Class */
    public static final String PRINT_QUEUE_MANAGEMENT_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.26";

    /** Stored Print Storage SOP Class (Retired) - SOP Class */
    public static final String STORED_PRINT_STORAGE_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.27";

    /** Hardcopy Grayscale Image Storage SOP Class (Retired) - SOP Class */
    public static final String HARDCOPY_GRAYSCALE_IMAGE_STORAGE_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.29";

    /** Hardcopy Color Image Storage SOP Class (Retired) - SOP Class */
    public static final String HARDCOPY_COLOR_IMAGE_STORAGE_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.30";

    /** Pull Print Request SOP Class (Retired) - SOP Class */
    public static final String PULL_PRINT_REQUEST_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.31";

    /** Pull Stored Print Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String PULL_STORED_PRINT_MANAGEMENT_META_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.32";

    /** Media Creation Management SOP Class UID - SOP Class */
    public static final String MEDIA_CREATION_MANAGEMENT_SOP_CLASS_UID = "1.2.840.10008.5.1.1.33";

    /** Basic Grayscale Image Box SOP Class - SOP Class */
    public static final String BASIC_GRAYSCALE_IMAGE_BOX_SOP_CLASS = "1.2.840.10008.5.1.1.4";

    /** Basic Color Image Box SOP Class - SOP Class */
    public static final String BASIC_COLOR_IMAGE_BOX_SOP_CLASS = "1.2.840.10008.5.1.1.4.1";

    /** Referenced Image Box SOP Class (Retired) - SOP Class */
    public static final String REFERENCED_IMAGE_BOX_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.4.2";

    /** Basic Grayscale Print Management Meta SOP Class - Meta SOP Class */
    public static final String BASIC_GRAYSCALE_PRINT_MANAGEMENT_META_SOP_CLASS = "1.2.840.10008.5.1.1.9";

    /** Referenced Grayscale Print Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String REFERENCED_GRAYSCALE_PRINT_MANAGEMENT_META_SOP_CLASS_RETIRED = "1.2.840.10008.5.1.1.9.1";

    /** Computed Radiography Image Storage - SOP Class */
    public static final String COMPUTED_RADIOGRAPHY_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.1";

    /** Digital X-Ray Image Storage - For Presentation - SOP Class */
    public static final String DIGITAL_X_RAY_IMAGE_STORAGE_FOR_PRESENTATION = "1.2.840.10008.5.1.4.1.1.1.1";

    /** Digital X-Ray Image Storage - For Processing - SOP Class */
    public static final String DIGITAL_X_RAY_IMAGE_STORAGE_FOR_PROCESSING = "1.2.840.10008.5.1.4.1.1.1.1.1";

    /** Digital Mammography X-Ray Image Storage - For Presentation - SOP Class */
    public static final String DIGITAL_MAMMOGRAPHY_X_RAY_IMAGE_STORAGE_FOR_PRESENTATION = "1.2.840.10008.5.1.4.1.1.1.2";

    /** Digital Mammography X-Ray Image Storage - For Processing - SOP Class */
    public static final String DIGITAL_MAMMOGRAPHY_X_RAY_IMAGE_STORAGE_FOR_PROCESSING = "1.2.840.10008.5.1.4.1.1.1.2.1";

    /** Digital Intra-oral X-Ray Image Storage - For Presentation - SOP Class */
    public static final String DIGITAL_INTRA_ORAL_X_RAY_IMAGE_STORAGE_FOR_PRESENTATION = "1.2.840.10008.5.1.4.1.1.1.3";

    /** Digital Intra-oral X-Ray Image Storage - For Processing - SOP Class */
    public static final String DIGITAL_INTRA_ORAL_X_RAY_IMAGE_STORAGE_FOR_PROCESSING = "1.2.840.10008.5.1.4.1.1.1.3.1";

    /** Standalone Modality LUT Storage (Retired) - SOP Class */
    public static final String STANDALONE_MODALITY_LUT_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.10";

    /** Encapsulated PDF Storage - SOP Class */
    public static final String ENCAPSULATED_PDF_STORAGE = "1.2.840.10008.5.1.4.1.1.104.1";

    /** Standalone VOI LUT Storage (Retired) - SOP Class */
    public static final String STANDALONE_VOI_LUT_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.11";

    /** Grayscale Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String GRAYSCALE_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS = "1.2.840.10008.5.1.4.1.1.11.1";

    /** Color Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String COLOR_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS = "1.2.840.10008.5.1.4.1.1.11.2";

    /** Pseudo-Color Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String PSEUDO_COLOR_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS = "1.2.840.10008.5.1.4.1.1.11.3";

    /** Blending Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String BLENDING_SOFTCOPY_PRESENTATION_STATE_STORAGE_SOP_CLASS = "1.2.840.10008.5.1.4.1.1.11.4";

    /** X-Ray Angiographic Image Storage - SOP Class */
    public static final String X_RAY_ANGIOGRAPHIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.12.1";

    /** Enhanced XA Image Storage - SOP Class */
    public static final String ENHANCED_XA_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.12.1.1";

    /** X-Ray Radiofluoroscopic Image Storage - SOP Class */
    public static final String X_RAY_RADIOFLUOROSCOPIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.12.2";

    /** Enhanced XRF Image Storage - SOP Class */
    public static final String ENHANCED_XRF_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.12.2.1";

    /** X-Ray Angiographic Bi-Plane Image Storage (Retired) - SOP Class */
    public static final String X_RAY_ANGIOGRAPHIC_BI_PLANE_IMAGE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.12.3";

    /** Positron Emission Tomography Image Storage - SOP Class */
    public static final String POSITRON_EMISSION_TOMOGRAPHY_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.128";

    /** Standalone PET Curve Storage (Retired) - SOP Class */
    public static final String STANDALONE_PET_CURVE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.129";

    /** CT Image Storage - SOP Class */
    public static final String CT_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.2";

    /** Enhanced CT Image Storage - SOP Class */
    public static final String ENHANCED_CT_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.2.1";

    /** Nuclear Medicine Image Storage - SOP Class */
    public static final String NUCLEAR_MEDICINE_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.20";

    /** Ultrasound Multi-frame Image Storage (Retired) - SOP Class */
    public static final String ULTRASOUND_MULTI_FRAME_IMAGE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.3";

    /** Ultrasound Multi-frame Image Storage - SOP Class */
    public static final String ULTRASOUND_MULTI_FRAME_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.3.1";

    /** MR Image Storage - SOP Class */
    public static final String MR_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.4";

    /** Enhanced MR Image Storage - SOP Class */
    public static final String ENHANCED_MR_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.4.1";

    /** MR Spectroscopy Storage - SOP Class */
    public static final String MR_SPECTROSCOPY_STORAGE = "1.2.840.10008.5.1.4.1.1.4.2";

    /** RT Image Storage - SOP Class */
    public static final String RT_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.481.1";

    /** RT Dose Storage - SOP Class */
    public static final String RT_DOSE_STORAGE = "1.2.840.10008.5.1.4.1.1.481.2";

    /** RT Structure Set Storage - SOP Class */
    public static final String RT_STRUCTURE_SET_STORAGE = "1.2.840.10008.5.1.4.1.1.481.3";

    /** RT Beams Treatment Record Storage - SOP Class */
    public static final String RT_BEAMS_TREATMENT_RECORD_STORAGE = "1.2.840.10008.5.1.4.1.1.481.4";

    /** RT Plan Storage - SOP Class */
    public static final String RT_PLAN_STORAGE = "1.2.840.10008.5.1.4.1.1.481.5";

    /** RT Brachy Treatment Record Storage - SOP Class */
    public static final String RT_BRACHY_TREATMENT_RECORD_STORAGE = "1.2.840.10008.5.1.4.1.1.481.6";

    /** RT Treatment Summary Record Storage - SOP Class */
    public static final String RT_TREATMENT_SUMMARY_RECORD_STORAGE = "1.2.840.10008.5.1.4.1.1.481.7";

    /** RT Ion Plan Storage - SOP Class */
    public static final String RT_ION_PLAN_STORAGE = "1.2.840.10008.5.1.4.1.1.481.8";

    /** RT Ion Beams Treatment Record Storage - SOP Class */
    public static final String RT_ION_BEAMS_TREATMENT_RECORD_STORAGE = "1.2.840.10008.5.1.4.1.1.481.9";

    /** Nuclear Medicine Image Storage (Retired) - SOP Class */
    public static final String NUCLEAR_MEDICINE_IMAGE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.5";

    /** Ultrasound Image Storage (Retired) - SOP Class */
    public static final String ULTRASOUND_IMAGE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.6";

    /** Ultrasound Image Storage - SOP Class */
    public static final String ULTRASOUND_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.6.1";

    /** Raw Data Storage - SOP Class */
    public static final String RAW_DATA_STORAGE = "1.2.840.10008.5.1.4.1.1.66";

    /** Spatial Registration Storage - SOP Class */
    public static final String SPATIAL_REGISTRATION_STORAGE = "1.2.840.10008.5.1.4.1.1.66.1";

    /** Spatial Fiducials Storage - SOP Class */
    public static final String SPATIAL_FIDUCIALS_STORAGE = "1.2.840.10008.5.1.4.1.1.66.2";

    /** Deformable Spatial Registration Storage - SOP Class */
    public static final String DEFORMABLE_SPATIAL_REGISTRATION_STORAGE = "1.2.840.10008.5.1.4.1.1.66.3";

    /** Segmentation Storage - SOP Class */
    public static final String SEGMENTATION_STORAGE = "1.2.840.10008.5.1.4.1.1.66.4";

    /** Real World Value Mapping Storage - SOP Class */
    public static final String REAL_WORLD_VALUE_MAPPING_STORAGE = "1.2.840.10008.5.1.4.1.1.67";

    /** Secondary Capture Image Storage - SOP Class */
    public static final String SECONDARY_CAPTURE_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.7";

    /** Multi-frame Single Bit Secondary Capture Image Storage - SOP Class */
    public static final String MULTI_FRAME_SINGLE_BIT_SECONDARY_CAPTURE_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.7.1";

    /** Multi-frame Grayscale Byte Secondary Capture Image Storage - SOP Class */
    public static final String MULTI_FRAME_GRAYSCALE_BYTE_SECONDARY_CAPTURE_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.7.2";

    /** Multi-frame Grayscale Word Secondary Capture Image Storage - SOP Class */
    public static final String MULTI_FRAME_GRAYSCALE_WORD_SECONDARY_CAPTURE_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.7.3";

    /** Multi-frame True Color Secondary Capture Image Storage - SOP Class */
    public static final String MULTI_FRAME_TRUE_COLOR_SECONDARY_CAPTURE_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.7.4";

    /** VL Image Storage (Retired) -  */
    public static final String VL_IMAGE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.77.1";

    /** VL Endoscopic Image Storage - SOP Class */
    public static final String VL_ENDOSCOPIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.1";

    /** Video Endoscopic Image Storage - SOP Class */
    public static final String VIDEO_ENDOSCOPIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.1.1";

    /** VL Microscopic Image Storage - SOP Class */
    public static final String VL_MICROSCOPIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.2";

    /** Video Microscopic Image Storage - SOP Class */
    public static final String VIDEO_MICROSCOPIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.2.1";

    /** VL Slide-Coordinates Microscopic Image Storage - SOP Class */
    public static final String VL_SLIDE_COORDINATES_MICROSCOPIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.3";

    /** VL Photographic Image Storage - SOP Class */
    public static final String VL_PHOTOGRAPHIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.4";

    /** Video Photographic Image Storage - SOP Class */
    public static final String VIDEO_PHOTOGRAPHIC_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.4.1";

    /** Ophthalmic Photography 8 Bit Image Storage - SOP Class */
    public static final String OPHTHALMIC_PHOTOGRAPHY_8_BIT_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.5.1";

    /** Ophthalmic Photography 16 Bit Image Storage - SOP Class */
    public static final String OPHTHALMIC_PHOTOGRAPHY_16_BIT_IMAGE_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.5.2";

    /** Stereometric Relationship Storage - SOP Class */
    public static final String STEREOMETRIC_RELATIONSHIP_STORAGE = "1.2.840.10008.5.1.4.1.1.77.1.5.3";

    /** VL Multi-frame Image Storage (Retired) -  */
    public static final String VL_MULTI_FRAME_IMAGE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.77.2";

    /** Standalone Overlay Storage (Retired) - SOP Class */
    public static final String STANDALONE_OVERLAY_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.8";

    /** Basic Text SR - SOP Class */
    public static final String BASIC_TEXT_SR = "1.2.840.10008.5.1.4.1.1.88.11";

    /** Enhanced SR - SOP Class */
    public static final String ENHANCED_SR = "1.2.840.10008.5.1.4.1.1.88.22";

    /** Comprehensive SR - SOP Class */
    public static final String COMPREHENSIVE_SR = "1.2.840.10008.5.1.4.1.1.88.33";

    /** Procedure Log Storage - SOP Class */
    public static final String PROCEDURE_LOG_STORAGE = "1.2.840.10008.5.1.4.1.1.88.40";

    /** Mammography CAD SR - SOP Class */
    public static final String MAMMOGRAPHY_CAD_SR = "1.2.840.10008.5.1.4.1.1.88.50";

    /** Key Object Selection Document - SOP Class */
    public static final String KEY_OBJECT_SELECTION_DOCUMENT = "1.2.840.10008.5.1.4.1.1.88.59";

    /** Chest CAD SR - SOP Class */
    public static final String CHEST_CAD_SR = "1.2.840.10008.5.1.4.1.1.88.65";

    /** X-Ray Radiation Dose SR - SOP Class */
    public static final String X_RAY_RADIATION_DOSE_SR = "1.2.840.10008.5.1.4.1.1.88.67";

    /** Standalone Curve Storage (Retired) - SOP Class */
    public static final String STANDALONE_CURVE_STORAGE_RETIRED = "1.2.840.10008.5.1.4.1.1.9";

    /** 12-lead ECG Waveform Storage - SOP Class */
    public static final String _12_LEAD_ECG_WAVEFORM_STORAGE = "1.2.840.10008.5.1.4.1.1.9.1.1";

    /** General ECG Waveform Storage - SOP Class */
    public static final String GENERAL_ECG_WAVEFORM_STORAGE = "1.2.840.10008.5.1.4.1.1.9.1.2";

    /** Ambulatory ECG Waveform Storage - SOP Class */
    public static final String AMBULATORY_ECG_WAVEFORM_STORAGE = "1.2.840.10008.5.1.4.1.1.9.1.3";

    /** Hemodynamic Waveform Storage - SOP Class */
    public static final String HEMODYNAMIC_WAVEFORM_STORAGE = "1.2.840.10008.5.1.4.1.1.9.2.1";

    /** Cardiac Electrophysiology Waveform Storage - SOP Class */
    public static final String CARDIAC_ELECTROPHYSIOLOGY_WAVEFORM_STORAGE = "1.2.840.10008.5.1.4.1.1.9.3.1";

    /** Basic Voice Audio Waveform Storage - SOP Class */
    public static final String BASIC_VOICE_AUDIO_WAVEFORM_STORAGE = "1.2.840.10008.5.1.4.1.1.9.4.1";

    /** Patient Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PATIENT_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_FIND = "1.2.840.10008.5.1.4.1.2.1.1";

    /** Patient Root Query/Retrieve Information Model - MOVE - SOP Class */
    public static final String PATIENT_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_MOVE = "1.2.840.10008.5.1.4.1.2.1.2";

    /** Patient Root Query/Retrieve Information Model - GET - SOP Class */
    public static final String PATIENT_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_GET = "1.2.840.10008.5.1.4.1.2.1.3";

    /** Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_FIND = "1.2.840.10008.5.1.4.1.2.2.1";

    /** Study Root Query/Retrieve Information Model - MOVE - SOP Class */
    public static final String STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_MOVE = "1.2.840.10008.5.1.4.1.2.2.2";

    /** Study Root Query/Retrieve Information Model - GET - SOP Class */
    public static final String STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_GET = "1.2.840.10008.5.1.4.1.2.2.3";

    /** Patient/Study Only Query/Retrieve Information Model - FIND (Retired) - SOP Class */
    public static final String PATIENT_STUDY_ONLY_QUERY_RETRIEVE_INFORMATION_MODEL_FIND_RETIRED = "1.2.840.10008.5.1.4.1.2.3.1";

    /** Patient/Study Only Query/Retrieve Information Model - MOVE (Retired) - SOP Class */
    public static final String PATIENT_STUDY_ONLY_QUERY_RETRIEVE_INFORMATION_MODEL_MOVE_RETIRED = "1.2.840.10008.5.1.4.1.2.3.2";

    /** Patient/Study Only Query/Retrieve Information Model - GET (Retired) - SOP Class */
    public static final String PATIENT_STUDY_ONLY_QUERY_RETRIEVE_INFORMATION_MODEL_GET_RETIRED = "1.2.840.10008.5.1.4.1.2.3.3";

    /** Modality Worklist Information Model - FIND - SOP Class */
    public static final String MODALITY_WORKLIST_INFORMATION_MODEL_FIND = "1.2.840.10008.5.1.4.31";

    /** General Purpose Worklist Management Meta SOP Class - Meta SOP Class */
    public static final String GENERAL_PURPOSE_WORKLIST_MANAGEMENT_META_SOP_CLASS = "1.2.840.10008.5.1.4.32";

    /** General Purpose Worklist Information Model - FIND - SOP Class */
    public static final String GENERAL_PURPOSE_WORKLIST_INFORMATION_MODEL_FIND = "1.2.840.10008.5.1.4.32.1";

    /** General Purpose Scheduled Procedure Step SOP Class - SOP Class */
    public static final String GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_SOP_CLASS = "1.2.840.10008.5.1.4.32.2";

    /** General Purpose Performed Procedure Step SOP Class - SOP Class */
    public static final String GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_SOP_CLASS = "1.2.840.10008.5.1.4.32.3";

    /** Instance Availability Notification SOP Class - SOP Class */
    public static final String INSTANCE_AVAILABILITY_NOTIFICATION_SOP_CLASS = "1.2.840.10008.5.1.4.33";

    /** General Relevant Patient Information Query - SOP Class */
    public static final String GENERAL_RELEVANT_PATIENT_INFORMATION_QUERY = "1.2.840.10008.5.1.4.37.1";

    /** Breast Imaging Relevant Patient Information Query - SOP Class */
    public static final String BREAST_IMAGING_RELEVANT_PATIENT_INFORMATION_QUERY = "1.2.840.10008.5.1.4.37.2";

    /** Cardiac Relevant Patient Information Query - SOP Class */
    public static final String CARDIAC_RELEVANT_PATIENT_INFORMATION_QUERY = "1.2.840.10008.5.1.4.37.3";

    /** Hanging Protocol Storage - SOP Class */
    public static final String HANGING_PROTOCOL_STORAGE = "1.2.840.10008.5.1.4.38.1";

    /** Hanging Protocol Information Model - FIND - SOP Class */
    public static final String HANGING_PROTOCOL_INFORMATION_MODEL_FIND = "1.2.840.10008.5.1.4.38.2";

    /** Hanging Protocol Information Model - MOVE - SOP Class */
    public static final String HANGING_PROTOCOL_INFORMATION_MODEL_MOVE = "1.2.840.10008.5.1.4.38.3";

}