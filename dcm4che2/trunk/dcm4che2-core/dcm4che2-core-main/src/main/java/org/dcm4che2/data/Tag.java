/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

/** Provides tag constants.*/
public class Tag {

    /** Private constructor */
    private Tag() {
    }
    
    public static final int forName(String name) {
       try {
          return Tag.class.getField(name).getInt(null);
       } catch (IllegalAccessException e) {
          throw new Error(e);
       } catch (NoSuchFieldException e) {
          throw new IllegalArgumentException("Unkown Tag Name: " + name);
       }
    }

    /** (0000,0000) VR=UL, VM=1 Group Length  */
    public static final int GroupLength00000000 = 0x00000000;

    /** (0000,0001) VR=, VM= Length to End RET */
    public static final int LengthtoEndRET00000001 = 0x00000001;

    /** (0000,0002) VR=UI, VM=1 Affected SOP Class UID  */
    public static final int AffectedSOPClassUID = 0x00000002;

    /** (0000,0003) VR=UI, VM=1 Requested SOP Class UID  */
    public static final int RequestedSOPClassUID = 0x00000003;

    /** (0000,0010) VR=, VM= Recognition Code RET */
    public static final int RecognitionCodeRET00000010 = 0x00000010;

    /** (0000,0100) VR=US, VM=1 Command Field  */
    public static final int CommandField = 0x00000100;

    /** (0000,0110) VR=US, VM=1 Message ID  */
    public static final int MessageID = 0x00000110;

    /** (0000,0120) VR=US, VM=1 Message ID Being Responded To  */
    public static final int MessageIDBeingRespondedTo = 0x00000120;

    /** (0000,0200) VR=, VM= Initiator RET */
    public static final int InitiatorRET = 0x00000200;

    /** (0000,0300) VR=, VM= Receiver RET */
    public static final int ReceiverRET = 0x00000300;

    /** (0000,0400) VR=, VM= Find Location RET */
    public static final int FindLocationRET = 0x00000400;

    /** (0000,0600) VR=AE, VM=1 Move Destination  */
    public static final int MoveDestination = 0x00000600;

    /** (0000,0700) VR=US, VM=1 Priority  */
    public static final int Priority = 0x00000700;

    /** (0000,0800) VR=US, VM=1 Data Set Type  */
    public static final int DataSetType = 0x00000800;

    /** (0000,0850) VR=, VM= Number of Matches RET */
    public static final int NumberofMatchesRET = 0x00000850;

    /** (0000,0860) VR=, VM= Response Sequence Number RET */
    public static final int ResponseSequenceNumberRET = 0x00000860;

    /** (0000,0900) VR=US, VM=1 Status  */
    public static final int Status = 0x00000900;

    /** (0000,0901) VR=AT, VM=1-n Offending Element  */
    public static final int OffendingElement = 0x00000901;

    /** (0000,0902) VR=LO, VM=1 Error Comment  */
    public static final int ErrorComment = 0x00000902;

    /** (0000,0903) VR=US, VM=1 Error ID  */
    public static final int ErrorID = 0x00000903;

    /** (0000,1000) VR=UI, VM=1 Affected SOP Instance UID  */
    public static final int AffectedSOPInstanceUID = 0x00001000;

    /** (0000,1001) VR=UI, VM=1 Requested SOP Instance UID  */
    public static final int RequestedSOPInstanceUID = 0x00001001;

    /** (0000,1002) VR=US, VM=1 Event Type ID  */
    public static final int EventTypeID = 0x00001002;

    /** (0000,1005) VR=AT, VM=1-n Attribute Identifier List  */
    public static final int AttributeIdentifierList = 0x00001005;

    /** (0000,1008) VR=US, VM=1 Action Type ID  */
    public static final int ActionTypeID = 0x00001008;

    /** (0000,1020) VR=US, VM=1 Number of Remaining Sub-operations  */
    public static final int NumberofRemainingSuboperations = 0x00001020;

    /** (0000,1021) VR=US, VM=1 Number of Completed Sub-operations  */
    public static final int NumberofCompletedSuboperations = 0x00001021;

    /** (0000,1022) VR=US, VM=1 Number of Failed Sub-operations  */
    public static final int NumberofFailedSuboperations = 0x00001022;

    /** (0000,1023) VR=US, VM=1 Number of Warning Sub-operations  */
    public static final int NumberofWarningSuboperations = 0x00001023;

    /** (0000,1030) VR=AE, VM=1 Move Originator Application Entity Title  */
    public static final int MoveOriginatorApplicationEntityTitle = 0x00001030;

    /** (0000,1031) VR=US, VM=1 Move Originator Message ID  */
    public static final int MoveOriginatorMessageID = 0x00001031;

    /** (0000,4000) VR=, VM= DIALOG Receiver RET */
    public static final int DIALOGReceiverRET = 0x00004000;

    /** (0000,4010) VR=, VM= Terminal Type RET */
    public static final int TerminalTypeRET = 0x00004010;

    /** (0000,5010) VR=, VM= Message Set ID RET */
    public static final int MessageSetIDRET = 0x00005010;

    /** (0000,5020) VR=, VM= End Message ID RET */
    public static final int EndMessageIDRET = 0x00005020;

    /** (0000,5110) VR=, VM= Display Format RET */
    public static final int DisplayFormatRET = 0x00005110;

    /** (0000,5120) VR=, VM= Page Position ID RET */
    public static final int PagePositionIDRET = 0x00005120;

    /** (0000,5130) VR=, VM= Text Format ID RET */
    public static final int TextFormatIDRET = 0x00005130;

    /** (0000,5140) VR=, VM= Nor/Rev RET */
    public static final int NorRevRET = 0x00005140;

    /** (0000,5150) VR=, VM= Add Gray Scale RET */
    public static final int AddGrayScaleRET = 0x00005150;

    /** (0000,5160) VR=, VM= Borders RET */
    public static final int BordersRET = 0x00005160;

    /** (0000,5170) VR=, VM= Copies RET */
    public static final int CopiesRET = 0x00005170;

    /** (0000,5180) VR=, VM= Magnification Type RET */
    public static final int MagnificationTypeRET = 0x00005180;

    /** (0000,5190) VR=, VM= Erase RET */
    public static final int EraseRET = 0x00005190;

    /** (0000,51A0) VR=, VM= Print RET */
    public static final int PrintRET = 0x000051A0;

    /** (0000,51B0) VR=, VM= Overlays RET */
    public static final int OverlaysRET = 0x000051B0;

    /** (0002,0000) VR=UL, VM=1 Group Length  */
    public static final int GroupLength00020000 = 0x00020000;

    /** (0002,0001) VR=OB, VM=1 File Meta Information Version  */
    public static final int FileMetaInformationVersion = 0x00020001;

    /** (0002,0002) VR=UI, VM=1 Media Storage SOP Class UID  */
    public static final int MediaStorageSOPClassUID = 0x00020002;

    /** (0002,0003) VR=UI, VM=1 Media Storage SOP Instance UID  */
    public static final int MediaStorageSOPInstanceUID = 0x00020003;

    /** (0002,0010) VR=UI, VM=1 Transfer Syntax UID  */
    public static final int TransferSyntaxUID = 0x00020010;

    /** (0002,0012) VR=UI, VM=1 Implementation Class UID  */
    public static final int ImplementationClassUID = 0x00020012;

    /** (0002,0013) VR=SH, VM=1 Implementation Version Name  */
    public static final int ImplementationVersionName = 0x00020013;

    /** (0002,0016) VR=AE, VM=1 Source Application Entity Title  */
    public static final int SourceApplicationEntityTitle = 0x00020016;

    /** (0002,0100) VR=UI, VM=1 Private Information Creator UID  */
    public static final int PrivateInformationCreatorUID = 0x00020100;

    /** (0002,0102) VR=OB, VM=1 Private Information  */
    public static final int PrivateInformation = 0x00020102;

    /** (0004,0000) VR=UL, VM=1 Group Length  */
    public static final int GroupLength00040000 = 0x00040000;

    /** (0004,1130) VR=CS, VM=1 File-set ID  */
    public static final int FilesetID = 0x00041130;

    /** (0004,1141) VR=CS, VM=1-8 File-set Descriptor File ID  */
    public static final int FilesetDescriptorFileID = 0x00041141;

    /** (0004,1142) VR=CS, VM=1 Specific Character Set of File-set Descriptor File  */
    public static final int SpecificCharacterSetofFilesetDescriptorFile = 0x00041142;

    /** (0004,1200) VR=UL, VM=1 Offset of the First Directory Record of the Root Directory Entity  */
    public static final int OffsetoftheFirstDirectoryRecordoftheRootDirectoryEntity = 0x00041200;

    /** (0004,1202) VR=UL, VM=1 Offset of the Last Directory Record of the Root Directory Entity  */
    public static final int OffsetoftheLastDirectoryRecordoftheRootDirectoryEntity = 0x00041202;

    /** (0004,1212) VR=US, VM=1 File-set Consistency Flag  */
    public static final int FilesetConsistencyFlag = 0x00041212;

    /** (0004,1220) VR=SQ, VM=1 Directory Record Sequence  */
    public static final int DirectoryRecordSequence = 0x00041220;

    /** (0004,1400) VR=UL, VM=1 Offset of the Next Directory Record  */
    public static final int OffsetoftheNextDirectoryRecord = 0x00041400;

    /** (0004,1410) VR=US, VM=1 Record In-use Flag  */
    public static final int RecordInuseFlag = 0x00041410;

    /** (0004,1420) VR=UL, VM=1 Offset of Referenced Lower-Level Directory Entity  */
    public static final int OffsetofReferencedLowerLevelDirectoryEntity = 0x00041420;

    /** (0004,1430) VR=CS, VM=1 Directory Record Type  */
    public static final int DirectoryRecordType = 0x00041430;

    /** (0004,1432) VR=UI, VM=1 Private Record UID  */
    public static final int PrivateRecordUID = 0x00041432;

    /** (0004,1500) VR=CS, VM=1-8 Referenced File ID  */
    public static final int ReferencedFileID = 0x00041500;

    /** (0004,1504) VR=UL, VM=1 MRDR Directory Record Offset  */
    public static final int MRDRDirectoryRecordOffset = 0x00041504;

    /** (0004,1510) VR=UI, VM=1 Referenced SOP Class UID in File  */
    public static final int ReferencedSOPClassUIDinFile = 0x00041510;

    /** (0004,1511) VR=UI, VM=1 Referenced SOP Instance UID in File  */
    public static final int ReferencedSOPInstanceUIDinFile = 0x00041511;

    /** (0004,1512) VR=UI, VM=1 Referenced Transfer Syntax UID in File  */
    public static final int ReferencedTransferSyntaxUIDinFile = 0x00041512;

    /** (0004,151A) VR=UI, VM=1-n Referenced Related General SOP Class UID in File  */
    public static final int ReferencedRelatedGeneralSOPClassUIDinFile = 0x0004151A;

    /** (0004,1600) VR=UL, VM=1 Number of References  */
    public static final int NumberofReferences = 0x00041600;

    /** (0008,0001) VR=, VM= Length to End RET */
    public static final int LengthtoEndRET00080001 = 0x00080001;

    /** (0008,0005) VR=CS, VM=1-n Specific Character Set  */
    public static final int SpecificCharacterSet = 0x00080005;

    /** (0008,0008) VR=CS, VM=1-n Image Type  */
    public static final int ImageType = 0x00080008;

    /** (0008,0010) VR=, VM= Recognition Code RET */
    public static final int RecognitionCodeRET00080010 = 0x00080010;

    /** (0008,0012) VR=DA, VM=1 Instance Creation Date  */
    public static final int InstanceCreationDate = 0x00080012;

    /** (0008,0013) VR=TM, VM=1 Instance Creation Time  */
    public static final int InstanceCreationTime = 0x00080013;

    /** (0008,0014) VR=UI, VM=1 Instance Creator UID  */
    public static final int InstanceCreatorUID = 0x00080014;

    /** (0008,0016) VR=UI, VM=1 SOP Class UID  */
    public static final int SOPClassUID = 0x00080016;

    /** (0008,0018) VR=UI, VM=1 SOP Instance UID  */
    public static final int SOPInstanceUID = 0x00080018;

    /** (0008,001A) VR=UI, VM=1-n Related General SOP Class UID  */
    public static final int RelatedGeneralSOPClassUID = 0x0008001A;

    /** (0008,001B) VR=UI, VM=1 Original Specialized SOP Class UID  */
    public static final int OriginalSpecializedSOPClassUID = 0x0008001B;

    /** (0008,0020) VR=DA, VM=1 Study Date  */
    public static final int StudyDate = 0x00080020;

    /** (0008,0021) VR=DA, VM=1 Series Date  */
    public static final int SeriesDate = 0x00080021;

    /** (0008,0022) VR=DA, VM=1 Acquisition Date  */
    public static final int AcquisitionDate = 0x00080022;

    /** (0008,0023) VR=DA, VM=1 Content Date  */
    public static final int ContentDate = 0x00080023;

    /** (0008,0024) VR=DA, VM=1 Overlay Date  */
    public static final int OverlayDate = 0x00080024;

    /** (0008,0025) VR=DA, VM=1 Curve Date  */
    public static final int CurveDate = 0x00080025;

    /** (0008,002A) VR=DT, VM=1 Acquisition Datetime  */
    public static final int AcquisitionDatetime = 0x0008002A;

    /** (0008,0030) VR=TM, VM=1 Study Time  */
    public static final int StudyTime = 0x00080030;

    /** (0008,0031) VR=TM, VM=1 Series Time  */
    public static final int SeriesTime = 0x00080031;

    /** (0008,0032) VR=TM, VM=1 Acquisition Time  */
    public static final int AcquisitionTime = 0x00080032;

    /** (0008,0033) VR=TM, VM=1 Content Time  */
    public static final int ContentTime = 0x00080033;

    /** (0008,0034) VR=TM, VM=1 Overlay Time  */
    public static final int OverlayTime = 0x00080034;

    /** (0008,0035) VR=TM, VM=1 Curve Time  */
    public static final int CurveTime = 0x00080035;

    /** (0008,0040) VR=, VM= Data Set Type RET */
    public static final int DataSetTypeRET = 0x00080040;

    /** (0008,0041) VR=, VM= Data Set Subtype RET */
    public static final int DataSetSubtypeRET = 0x00080041;

    /** (0008,0042) VR=CS, VM=1 Nuclear Medicine Series Type RET */
    public static final int NuclearMedicineSeriesTypeRET = 0x00080042;

    /** (0008,0050) VR=SH, VM=1 Accession Number  */
    public static final int AccessionNumber = 0x00080050;

    /** (0008,0052) VR=CS, VM=1 Query/Retrieve Level  */
    public static final int QueryRetrieveLevel = 0x00080052;

    /** (0008,0054) VR=AE, VM=1-n Retrieve AE Title  */
    public static final int RetrieveAETitle = 0x00080054;

    /** (0008,0056) VR=CS, VM=1 Instance Availability  */
    public static final int InstanceAvailability = 0x00080056;

    /** (0008,0058) VR=UI, VM=1-n Failed SOP Instance UID List  */
    public static final int FailedSOPInstanceUIDList = 0x00080058;

    /** (0008,0060) VR=CS, VM=1 Modality  */
    public static final int Modality = 0x00080060;

    /** (0008,0061) VR=CS, VM=1-n Modalities in Study  */
    public static final int ModalitiesinStudy = 0x00080061;

    /** (0008,0062) VR=UI, VM=1-n SOP Classes in Study  */
    public static final int SOPClassesinStudy = 0x00080062;

    /** (0008,0064) VR=CS, VM=1 Conversion Type  */
    public static final int ConversionType = 0x00080064;

    /** (0008,0068) VR=CS, VM=1 Presentation Intent Type  */
    public static final int PresentationIntentType = 0x00080068;

    /** (0008,0070) VR=LO, VM=1 Manufacturer  */
    public static final int Manufacturer = 0x00080070;

    /** (0008,0080) VR=LO, VM=1 Institution Name  */
    public static final int InstitutionName = 0x00080080;

    /** (0008,0081) VR=ST, VM=1 Institution Address  */
    public static final int InstitutionAddress = 0x00080081;

    /** (0008,0082) VR=SQ, VM=1 Institution Code Sequence  */
    public static final int InstitutionCodeSequence = 0x00080082;

    /** (0008,0090) VR=PN, VM=1 Referring Physician's Name  */
    public static final int ReferringPhysiciansName = 0x00080090;

    /** (0008,0092) VR=ST, VM=1 Referring Physician's Address  */
    public static final int ReferringPhysiciansAddress = 0x00080092;

    /** (0008,0094) VR=SH, VM=1-n Referring Physician's Telephone Numbers  */
    public static final int ReferringPhysiciansTelephoneNumbers = 0x00080094;

    /** (0008,0096) VR=SQ, VM=1 Referring Physician Identification Sequence  */
    public static final int ReferringPhysicianIdentificationSequence = 0x00080096;

    /** (0008,0100) VR=SH, VM=1 Code Value  */
    public static final int CodeValue = 0x00080100;

    /** (0008,0102) VR=SH, VM=1 Coding Scheme Designator  */
    public static final int CodingSchemeDesignator = 0x00080102;

    /** (0008,0103) VR=SH, VM=1 Coding Scheme Version  */
    public static final int CodingSchemeVersion = 0x00080103;

    /** (0008,0104) VR=LO, VM=1 Code Meaning  */
    public static final int CodeMeaning = 0x00080104;

    /** (0008,0105) VR=CS, VM=1 Mapping Resource  */
    public static final int MappingResource = 0x00080105;

    /** (0008,0106) VR=DT, VM=1 Context Group Version  */
    public static final int ContextGroupVersion = 0x00080106;

    /** (0008,0107) VR=DT, VM=1 Context Group Local Version  */
    public static final int ContextGroupLocalVersion = 0x00080107;

    /** (0008,010B) VR=CS, VM=1 Context Group Extension Flag  */
    public static final int ContextGroupExtensionFlag = 0x0008010B;

    /** (0008,010C) VR=UI, VM=1 Coding Scheme UID  */
    public static final int CodingSchemeUID = 0x0008010C;

    /** (0008,010D) VR=UI, VM=1 Context Group Extension Creator UID  */
    public static final int ContextGroupExtensionCreatorUID = 0x0008010D;

    /** (0008,010F) VR=CS, VM=1 Context Identifier  */
    public static final int ContextIdentifier = 0x0008010F;

    /** (0008,0110) VR=SQ, VM=1 Coding Scheme Identification Sequence  */
    public static final int CodingSchemeIdentificationSequence = 0x00080110;

    /** (0008,0112) VR=LO, VM=1 Coding Scheme Registry  */
    public static final int CodingSchemeRegistry = 0x00080112;

    /** (0008,0114) VR=ST, VM=1 Coding Scheme External ID  */
    public static final int CodingSchemeExternalID = 0x00080114;

    /** (0008,0115) VR=ST, VM=1 Coding Scheme Name  */
    public static final int CodingSchemeName = 0x00080115;

    /** (0008,0116) VR=ST, VM=1 Responsible Organization  */
    public static final int ResponsibleOrganization = 0x00080116;

    /** (0008,0201) VR=SH, VM=1 Timezone Offset From UTC  */
    public static final int TimezoneOffsetFromUTC = 0x00080201;

    /** (0008,1000) VR=, VM= Network ID RET */
    public static final int NetworkIDRET = 0x00081000;

    /** (0008,1010) VR=SH, VM=1 Station Name  */
    public static final int StationName = 0x00081010;

    /** (0008,1030) VR=LO, VM=1 Study Description  */
    public static final int StudyDescription = 0x00081030;

    /** (0008,1032) VR=SQ, VM=1 Procedure Code Sequence  */
    public static final int ProcedureCodeSequence = 0x00081032;

    /** (0008,103E) VR=LO, VM=1 Series Description  */
    public static final int SeriesDescription = 0x0008103E;

    /** (0008,1040) VR=LO, VM=1 Institutional Department Name  */
    public static final int InstitutionalDepartmentName = 0x00081040;

    /** (0008,1048) VR=PN, VM=1-n Physician(s) of Record  */
    public static final int PhysiciansofRecord = 0x00081048;

    /** (0008,1049) VR=SQ, VM=1 Physician(s) of Record Identification Sequence  */
    public static final int PhysiciansofRecordIdentificationSequence = 0x00081049;

    /** (0008,1050) VR=PN, VM=1-n Performing Physician's Name  */
    public static final int PerformingPhysiciansName = 0x00081050;

    /** (0008,1052) VR=SQ, VM=1 Performing Physician Identification Sequence  */
    public static final int PerformingPhysicianIdentificationSequence = 0x00081052;

    /** (0008,1060) VR=PN, VM=1-n Name of Physician(s) Reading Study  */
    public static final int NameofPhysiciansReadingStudy = 0x00081060;

    /** (0008,1062) VR=SQ, VM=1 Physician(s) Reading Study Identification Sequence  */
    public static final int PhysiciansReadingStudyIdentificationSequence = 0x00081062;

    /** (0008,1070) VR=PN, VM=1-n Operators' Name  */
    public static final int OperatorsName = 0x00081070;

    /** (0008,1072) VR=SQ, VM=1 Operator Identification Sequence  */
    public static final int OperatorIdentificationSequence = 0x00081072;

    /** (0008,1080) VR=LO, VM=1-n Admitting Diagnoses Description  */
    public static final int AdmittingDiagnosesDescription = 0x00081080;

    /** (0008,1084) VR=SQ, VM=1 Admitting Diagnoses Code Sequence  */
    public static final int AdmittingDiagnosesCodeSequence = 0x00081084;

    /** (0008,1090) VR=LO, VM=1 Manufacturer's Model Name  */
    public static final int ManufacturersModelName = 0x00081090;

    /** (0008,1100) VR=SQ, VM=1 Referenced Results Sequence  */
    public static final int ReferencedResultsSequence = 0x00081100;

    /** (0008,1110) VR=SQ, VM=1 Referenced Study Sequence  */
    public static final int ReferencedStudySequence = 0x00081110;

    /** (0008,1111) VR=SQ, VM=1 Referenced Performed Procedure Step Sequence  */
    public static final int ReferencedPerformedProcedureStepSequence = 0x00081111;

    /** (0008,1115) VR=SQ, VM=1 Referenced Series Sequence  */
    public static final int ReferencedSeriesSequence = 0x00081115;

    /** (0008,1120) VR=SQ, VM=1 Referenced Patient Sequence  */
    public static final int ReferencedPatientSequence = 0x00081120;

    /** (0008,1125) VR=SQ, VM=1 Referenced Visit Sequence  */
    public static final int ReferencedVisitSequence = 0x00081125;

    /** (0008,1130) VR=SQ, VM=1 Referenced Overlay Sequence  */
    public static final int ReferencedOverlaySequence00081130 = 0x00081130;

    /** (0008,113A) VR=SQ, VM=1 Referenced Waveform Sequence  */
    public static final int ReferencedWaveformSequence = 0x0008113A;

    /** (0008,1140) VR=SQ, VM=1 Referenced Image Sequence  */
    public static final int ReferencedImageSequence = 0x00081140;

    /** (0008,1145) VR=SQ, VM=1 Referenced Curve Sequence  */
    public static final int ReferencedCurveSequence = 0x00081145;

    /** (0008,114A) VR=SQ, VM=1 Referenced Instance Sequence  */
    public static final int ReferencedInstanceSequence = 0x0008114A;

    /** (0008,1150) VR=UI, VM=1 Referenced SOP Class UID  */
    public static final int ReferencedSOPClassUID = 0x00081150;

    /** (0008,1155) VR=UI, VM=1 Referenced SOP Instance UID  */
    public static final int ReferencedSOPInstanceUID = 0x00081155;

    /** (0008,115A) VR=UI, VM=1-n SOP Classes Supported  */
    public static final int SOPClassesSupported = 0x0008115A;

    /** (0008,1160) VR=IS, VM=1-n Referenced Frame Number  */
    public static final int ReferencedFrameNumber = 0x00081160;

    /** (0008,1195) VR=UI, VM=1 Transaction UID  */
    public static final int TransactionUID = 0x00081195;

    /** (0008,1197) VR=US, VM=1 Failure Reason  */
    public static final int FailureReason = 0x00081197;

    /** (0008,1198) VR=SQ, VM=1 Failed SOP Sequence  */
    public static final int FailedSOPSequence = 0x00081198;

    /** (0008,1199) VR=SQ, VM=1 Referenced SOP Sequence  */
    public static final int ReferencedSOPSequence = 0x00081199;

    /** (0008,1200) VR=, VM= Studies Containing Other Referenced Instances Sequence  */
    public static final int StudiesContainingOtherReferencedInstancesSequence = 0x00081200;

    /** (0008,1250) VR=SQ, VM=1 Related Series Sequence  */
    public static final int RelatedSeriesSequence = 0x00081250;

    /** (0008,2110) VR=CS, VM=1 Lossy Image Compression RET */
    public static final int LossyImageCompressionRET = 0x00082110;

    /** (0008,2111) VR=ST, VM=1 Derivation Description  */
    public static final int DerivationDescription = 0x00082111;

    /** (0008,2112) VR=SQ, VM=1 Source Image Sequence  */
    public static final int SourceImageSequence = 0x00082112;

    /** (0008,2120) VR=SH, VM=1 Stage Name  */
    public static final int StageName = 0x00082120;

    /** (0008,2122) VR=IS, VM=1 Stage Number  */
    public static final int StageNumber = 0x00082122;

    /** (0008,2124) VR=IS, VM=1 Number of Stages  */
    public static final int NumberofStages = 0x00082124;

    /** (0008,2127) VR=SH, VM=1 View Name  */
    public static final int ViewName = 0x00082127;

    /** (0008,2128) VR=IS, VM=1 View Number  */
    public static final int ViewNumber = 0x00082128;

    /** (0008,2129) VR=IS, VM=1 Number of Event Timers  */
    public static final int NumberofEventTimers = 0x00082129;

    /** (0008,212A) VR=IS, VM=1 Number of Views in Stage  */
    public static final int NumberofViewsinStage = 0x0008212A;

    /** (0008,2130) VR=DS, VM=1-n Event Elapsed Time(s)  */
    public static final int EventElapsedTimes = 0x00082130;

    /** (0008,2132) VR=LO, VM=1-n Event Timer Name(s)  */
    public static final int EventTimerNames = 0x00082132;

    /** (0008,2142) VR=IS, VM=1 Start Trim  */
    public static final int StartTrim = 0x00082142;

    /** (0008,2143) VR=IS, VM=1 Stop Trim  */
    public static final int StopTrim = 0x00082143;

    /** (0008,2144) VR=IS, VM=1 Recommended Display Frame Rate  */
    public static final int RecommendedDisplayFrameRate = 0x00082144;

    /** (0008,2200) VR=CS, VM=1 Transducer Position RET */
    public static final int TransducerPositionRET = 0x00082200;

    /** (0008,2204) VR=CS, VM=1 Transducer Orientation RET */
    public static final int TransducerOrientationRET = 0x00082204;

    /** (0008,2208) VR=CS, VM=1 Anatomic Structure RET */
    public static final int AnatomicStructureRET = 0x00082208;

    /** (0008,2218) VR=SQ, VM=1 Anatomic Region Sequence  */
    public static final int AnatomicRegionSequence = 0x00082218;

    /** (0008,2220) VR=SQ, VM=1 Anatomic Region Modifier Sequence  */
    public static final int AnatomicRegionModifierSequence = 0x00082220;

    /** (0008,2228) VR=SQ, VM=1 Primary Anatomic Structure Sequence  */
    public static final int PrimaryAnatomicStructureSequence = 0x00082228;

    /** (0008,2229) VR=SQ, VM=1 Anatomic Structure, Space or Region Sequence  */
    public static final int AnatomicStructureSpaceorRegionSequence = 0x00082229;

    /** (0008,2230) VR=SQ, VM=1 Primary Anatomic Structure Modifier Sequence  */
    public static final int PrimaryAnatomicStructureModifierSequence = 0x00082230;

    /** (0008,2240) VR=SQ, VM=1 Transducer Position Sequence  */
    public static final int TransducerPositionSequence = 0x00082240;

    /** (0008,2242) VR=SQ, VM=1 Transducer Position Modifier Sequence  */
    public static final int TransducerPositionModifierSequence = 0x00082242;

    /** (0008,2244) VR=SQ, VM=1 Transducer Orientation Sequence  */
    public static final int TransducerOrientationSequence = 0x00082244;

    /** (0008,2246) VR=SQ, VM=1 Transducer Orientation Modifier Sequence  */
    public static final int TransducerOrientationModifierSequence = 0x00082246;

    /** (0008,3001) VR=SQ, VM=1 Alternate Representation Sequence  */
    public static final int AlternateRepresentationSequence = 0x00083001;

    /** (0008,4000) VR=, VM= Comments RET */
    public static final int CommentsRET00084000 = 0x00084000;

    /** (0008,9007) VR=CS, VM=4 Frame Type  */
    public static final int FrameType = 0x00089007;

    /** (0008,9092) VR=SQ, VM=1 Referenced Image Evidence Sequence  */
    public static final int ReferencedImageEvidenceSequence = 0x00089092;

    /** (0008,9121) VR=SQ, VM=1 Referenced Raw Data Sequence  */
    public static final int ReferencedRawDataSequence = 0x00089121;

    /** (0008,9123) VR=UI, VM=1 Creator-Version UID  */
    public static final int CreatorVersionUID = 0x00089123;

    /** (0008,9124) VR=SQ, VM=1 Derivation Image Sequence  */
    public static final int DerivationImageSequence = 0x00089124;

    /** (0008,9154) VR=SQ, VM=1 Source Image Evidence Sequence  */
    public static final int SourceImageEvidenceSequence = 0x00089154;

    /** (0008,9205) VR=CS, VM=1 Pixel Presentation  */
    public static final int PixelPresentation = 0x00089205;

    /** (0008,9206) VR=CS, VM=1 Volumetric Properties  */
    public static final int VolumetricProperties = 0x00089206;

    /** (0008,9207) VR=CS, VM=1 Volume Based Calculation Technique  */
    public static final int VolumeBasedCalculationTechnique = 0x00089207;

    /** (0008,9208) VR=CS, VM=1 Complex Image Component  */
    public static final int ComplexImageComponent = 0x00089208;

    /** (0008,9209) VR=CS, VM=1 Acquisition Contrast  */
    public static final int AcquisitionContrast = 0x00089209;

    /** (0008,9215) VR=SQ, VM=1 Derivation Code Sequence  */
    public static final int DerivationCodeSequence = 0x00089215;

    /** (0008,9237) VR=SQ, VM=1 Referenced Grayscale Presentation State Sequence  */
    public static final int ReferencedGrayscalePresentationStateSequence = 0x00089237;

    /** (0010,0010) VR=PN, VM=1 Patient's Name  */
    public static final int PatientsName = 0x00100010;

    /** (0010,0020) VR=LO, VM=1 Patient ID  */
    public static final int PatientID = 0x00100020;

    /** (0010,0021) VR=LO, VM=1 Issuer of Patient ID  */
    public static final int IssuerofPatientID = 0x00100021;

    /** (0010,0030) VR=DA, VM=1 Patient's Birth Date  */
    public static final int PatientsBirthDate = 0x00100030;

    /** (0010,0032) VR=TM, VM=1 Patient's Birth Time  */
    public static final int PatientsBirthTime = 0x00100032;

    /** (0010,0040) VR=CS, VM=1 Patient's Sex  */
    public static final int PatientsSex = 0x00100040;

    /** (0010,0050) VR=SQ, VM=1 Patient's Insurance Plan Code Sequence  */
    public static final int PatientsInsurancePlanCodeSequence = 0x00100050;

    /** (0010,0101) VR=SQ, VM=1 Patient's Primary Language Code Sequence  */
    public static final int PatientsPrimaryLanguageCodeSequence = 0x00100101;

    /** (0010,0102) VR=SQ, VM=1 Patient's Primary Language Code Modifier Sequence  */
    public static final int PatientsPrimaryLanguageCodeModifierSequence = 0x00100102;

    /** (0010,1000) VR=LO, VM=1-n Other Patient IDs  */
    public static final int OtherPatientIDs = 0x00101000;

    /** (0010,1001) VR=PN, VM=1-n Other Patient Names  */
    public static final int OtherPatientNames = 0x00101001;

    /** (0010,1005) VR=PN, VM=1 Patient's Birth Name  */
    public static final int PatientsBirthName = 0x00101005;

    /** (0010,1010) VR=AS, VM=1 Patient's Age  */
    public static final int PatientsAge = 0x00101010;

    /** (0010,1020) VR=DS, VM=1 Patient's Size  */
    public static final int PatientsSize = 0x00101020;

    /** (0010,1030) VR=DS, VM=1 Patient's Weight  */
    public static final int PatientsWeight = 0x00101030;

    /** (0010,1040) VR=LO, VM=1 Patient's Address  */
    public static final int PatientsAddress = 0x00101040;

    /** (0010,1050) VR=, VM= Insurance Plan Identification RET */
    public static final int InsurancePlanIdentificationRET = 0x00101050;

    /** (0010,1060) VR=PN, VM=1 Patient's Mother's Birth Name  */
    public static final int PatientsMothersBirthName = 0x00101060;

    /** (0010,1080) VR=LO, VM=1 Military Rank  */
    public static final int MilitaryRank = 0x00101080;

    /** (0010,1081) VR=LO, VM=1 Branch of Service  */
    public static final int BranchofService = 0x00101081;

    /** (0010,1090) VR=LO, VM=1 Medical Record Locator  */
    public static final int MedicalRecordLocator = 0x00101090;

    /** (0010,2000) VR=LO, VM=1-n Medical Alerts  */
    public static final int MedicalAlerts = 0x00102000;

    /** (0010,2110) VR=LO, VM=1-n Contrast Allergies  */
    public static final int ContrastAllergies = 0x00102110;

    /** (0010,2150) VR=LO, VM=1 Country of Residence  */
    public static final int CountryofResidence = 0x00102150;

    /** (0010,2152) VR=LO, VM=1 Region of Residence  */
    public static final int RegionofResidence = 0x00102152;

    /** (0010,2154) VR=SH, VM=1-n Patient's Telephone Numbers  */
    public static final int PatientsTelephoneNumbers = 0x00102154;

    /** (0010,2160) VR=SH, VM=1 Ethnic Group  */
    public static final int EthnicGroup = 0x00102160;

    /** (0010,2180) VR=SH, VM=1 Occupation  */
    public static final int Occupation = 0x00102180;

    /** (0010,21A0) VR=CS, VM=1 Smoking Status  */
    public static final int SmokingStatus = 0x001021A0;

    /** (0010,21B0) VR=LT, VM=1 Additional Patient History  */
    public static final int AdditionalPatientHistory = 0x001021B0;

    /** (0010,21C0) VR=US, VM=1 Pregnancy Status  */
    public static final int PregnancyStatus = 0x001021C0;

    /** (0010,21D0) VR=DA, VM=1 Last Menstrual Date  */
    public static final int LastMenstrualDate = 0x001021D0;

    /** (0010,21F0) VR=LO, VM=1 Patient's Religious Preference  */
    public static final int PatientsReligiousPreference = 0x001021F0;

    /** (0010,4000) VR=LT, VM=1 Patient Comments  */
    public static final int PatientComments = 0x00104000;

    /** (0012,0010) VR=LO, VM=1 Clinical Trial Sponsor Name  */
    public static final int ClinicalTrialSponsorName = 0x00120010;

    /** (0012,0020) VR=LO, VM=1 Clinical Trial Protocol ID  */
    public static final int ClinicalTrialProtocolID = 0x00120020;

    /** (0012,0021) VR=LO, VM=1 Clinical Trial Protocol Name  */
    public static final int ClinicalTrialProtocolName = 0x00120021;

    /** (0012,0030) VR=LO, VM=1 Clinical Trial Site ID  */
    public static final int ClinicalTrialSiteID = 0x00120030;

    /** (0012,0031) VR=LO, VM=1 Clinical Trial Site Name  */
    public static final int ClinicalTrialSiteName = 0x00120031;

    /** (0012,0040) VR=LO, VM=1 Clinical Trial Subject ID  */
    public static final int ClinicalTrialSubjectID = 0x00120040;

    /** (0012,0042) VR=LO, VM=1 Clinical Trial Subject Reading ID  */
    public static final int ClinicalTrialSubjectReadingID = 0x00120042;

    /** (0012,0050) VR=LO, VM=1 Clinical Trial Time Point ID  */
    public static final int ClinicalTrialTimePointID = 0x00120050;

    /** (0012,0051) VR=ST, VM=1 Clinical Trial Time Point Description  */
    public static final int ClinicalTrialTimePointDescription = 0x00120051;

    /** (0012,0060) VR=LO, VM=1 Clinical Trial Coordinating Center Name  */
    public static final int ClinicalTrialCoordinatingCenterName = 0x00120060;

    /** (0018,0010) VR=LO, VM=1 Contrast/Bolus Agent  */
    public static final int ContrastBolusAgent = 0x00180010;

    /** (0018,0012) VR=SQ, VM=1 Contrast/Bolus Agent Sequence  */
    public static final int ContrastBolusAgentSequence = 0x00180012;

    /** (0018,0014) VR=SQ, VM=1 Contrast/Bolus Administration Route Sequence  */
    public static final int ContrastBolusAdministrationRouteSequence = 0x00180014;

    /** (0018,0015) VR=CS, VM=1 Body Part Examined  */
    public static final int BodyPartExamined = 0x00180015;

    /** (0018,0020) VR=CS, VM=1-n Scanning Sequence  */
    public static final int ScanningSequence = 0x00180020;

    /** (0018,0021) VR=CS, VM=1-n Sequence Variant  */
    public static final int SequenceVariant = 0x00180021;

    /** (0018,0022) VR=CS, VM=1-n Scan Options  */
    public static final int ScanOptions = 0x00180022;

    /** (0018,0023) VR=CS, VM=1 MR Acquisition Type  */
    public static final int MRAcquisitionType = 0x00180023;

    /** (0018,0024) VR=SH, VM=1 Sequence Name  */
    public static final int SequenceName = 0x00180024;

    /** (0018,0025) VR=CS, VM=1 Angio Flag  */
    public static final int AngioFlag = 0x00180025;

    /** (0018,0026) VR=SQ, VM=1 Intervention Drug Information Sequence  */
    public static final int InterventionDrugInformationSequence = 0x00180026;

    /** (0018,0027) VR=TM, VM=1 Intervention Drug Stop Time  */
    public static final int InterventionDrugStopTime = 0x00180027;

    /** (0018,0028) VR=DS, VM=1 Intervention Drug Dose  */
    public static final int InterventionDrugDose = 0x00180028;

    /** (0018,0029) VR=SQ, VM=1 Intervention Drug Sequence  */
    public static final int InterventionDrugSequence = 0x00180029;

    /** (0018,002A) VR=SQ, VM=1 Additional Drug Sequence  */
    public static final int AdditionalDrugSequence = 0x0018002A;

    /** (0018,0030) VR=LO, VM=1-n Radionuclide RET */
    public static final int RadionuclideRET = 0x00180030;

    /** (0018,0031) VR=LO, VM=1 Radiopharmaceutical  */
    public static final int Radiopharmaceutical = 0x00180031;

    /** (0018,0032) VR=DS, VM=1 Energy Window Centerline RET */
    public static final int EnergyWindowCenterlineRET = 0x00180032;

    /** (0018,0033) VR=DS, VM=1-n Energy Window Total Width RET */
    public static final int EnergyWindowTotalWidthRET = 0x00180033;

    /** (0018,0034) VR=LO, VM=1 Intervention Drug Name  */
    public static final int InterventionDrugName = 0x00180034;

    /** (0018,0035) VR=TM, VM=1 Intervention Drug Start Time  */
    public static final int InterventionDrugStartTime = 0x00180035;

    /** (0018,0036) VR=SQ, VM=1 Intervention Sequence  */
    public static final int InterventionSequence = 0x00180036;

    /** (0018,0037) VR=CS, VM=1 Therapy Type RET */
    public static final int TherapyTypeRET = 0x00180037;

    /** (0018,0038) VR=CS, VM=1 Intervention Status  */
    public static final int InterventionStatus = 0x00180038;

    /** (0018,0039) VR=CS, VM=1 Therapy Description RET */
    public static final int TherapyDescriptionRET = 0x00180039;

    /** (0018,003A) VR=ST, VM=1 Intervention Description  */
    public static final int InterventionDescription = 0x0018003A;

    /** (0018,0040) VR=IS, VM=1 Cine Rate  */
    public static final int CineRate = 0x00180040;

    /** (0018,0050) VR=DS, VM=1 Slice Thickness  */
    public static final int SliceThickness = 0x00180050;

    /** (0018,0060) VR=DS, VM=1 KVP  */
    public static final int KVP = 0x00180060;

    /** (0018,0070) VR=IS, VM=1 Counts Accumulated  */
    public static final int CountsAccumulated = 0x00180070;

    /** (0018,0071) VR=CS, VM=1 Acquisition Termination Condition  */
    public static final int AcquisitionTerminationCondition = 0x00180071;

    /** (0018,0072) VR=DS, VM=1 Effective Duration  */
    public static final int EffectiveDuration = 0x00180072;

    /** (0018,0073) VR=CS, VM=1 Acquisition Start Condition  */
    public static final int AcquisitionStartCondition = 0x00180073;

    /** (0018,0074) VR=IS, VM=1 Acquisition Start Condition Data  */
    public static final int AcquisitionStartConditionData = 0x00180074;

    /** (0018,0075) VR=IS, VM=1 Acquisition Termination Condition Data  */
    public static final int AcquisitionTerminationConditionData = 0x00180075;

    /** (0018,0080) VR=DS, VM=1 Repetition Time  */
    public static final int RepetitionTime = 0x00180080;

    /** (0018,0081) VR=DS, VM=1 Echo Time  */
    public static final int EchoTime = 0x00180081;

    /** (0018,0082) VR=DS, VM=1 Inversion Time  */
    public static final int InversionTime = 0x00180082;

    /** (0018,0083) VR=DS, VM=1 Number of Averages  */
    public static final int NumberofAverages = 0x00180083;

    /** (0018,0084) VR=DS, VM=1 Imaging Frequency  */
    public static final int ImagingFrequency = 0x00180084;

    /** (0018,0085) VR=SH, VM=1 Imaged Nucleus  */
    public static final int ImagedNucleus = 0x00180085;

    /** (0018,0086) VR=IS, VM=1-n Echo Number(s)  */
    public static final int EchoNumbers = 0x00180086;

    /** (0018,0087) VR=DS, VM=1 Magnetic Field Strength  */
    public static final int MagneticFieldStrength = 0x00180087;

    /** (0018,0088) VR=DS, VM=1 Spacing Between Slices  */
    public static final int SpacingBetweenSlices = 0x00180088;

    /** (0018,0089) VR=IS, VM=1 Number of Phase Encoding Steps  */
    public static final int NumberofPhaseEncodingSteps = 0x00180089;

    /** (0018,0090) VR=DS, VM=1 Data Collection Diameter  */
    public static final int DataCollectionDiameter = 0x00180090;

    /** (0018,0091) VR=IS, VM=1 Echo Train Length  */
    public static final int EchoTrainLength = 0x00180091;

    /** (0018,0093) VR=DS, VM=1 Percent Sampling  */
    public static final int PercentSampling = 0x00180093;

    /** (0018,0094) VR=DS, VM=1 Percent Phase Field of View  */
    public static final int PercentPhaseFieldofView = 0x00180094;

    /** (0018,0095) VR=DS, VM=1 Pixel Bandwidth  */
    public static final int PixelBandwidth = 0x00180095;

    /** (0018,1000) VR=LO, VM=1 Device Serial Number  */
    public static final int DeviceSerialNumber = 0x00181000;

    /** (0018,1004) VR=LO, VM=1 Plate ID  */
    public static final int PlateID = 0x00181004;

    /** (0018,1010) VR=LO, VM=1 Secondary Capture Device ID  */
    public static final int SecondaryCaptureDeviceID = 0x00181010;

    /** (0018,1011) VR=LO, VM=1 Hardcopy Creation Device ID  */
    public static final int HardcopyCreationDeviceID = 0x00181011;

    /** (0018,1012) VR=DA, VM=1 Date of Secondary Capture  */
    public static final int DateofSecondaryCapture = 0x00181012;

    /** (0018,1014) VR=TM, VM=1 Time of Secondary Capture  */
    public static final int TimeofSecondaryCapture = 0x00181014;

    /** (0018,1016) VR=LO, VM=1 Secondary Capture Device Manufacturer  */
    public static final int SecondaryCaptureDeviceManufacturer = 0x00181016;

    /** (0018,1017) VR=LO, VM=1 Hardcopy Device Manufacturer  */
    public static final int HardcopyDeviceManufacturer = 0x00181017;

    /** (0018,1018) VR=LO, VM=1 Secondary Capture Device Manufacturer's Model Name  */
    public static final int SecondaryCaptureDeviceManufacturersModelName = 0x00181018;

    /** (0018,1019) VR=LO, VM=1-n Secondary Capture Device Software Version(s)  */
    public static final int SecondaryCaptureDeviceSoftwareVersions = 0x00181019;

    /** (0018,101A) VR=LO, VM=1-n Hardcopy Device Software Version  */
    public static final int HardcopyDeviceSoftwareVersion = 0x0018101A;

    /** (0018,101B) VR=LO, VM=1 Hardcopy Device Manfuacturer's Model Name  */
    public static final int HardcopyDeviceManfuacturersModelName = 0x0018101B;

    /** (0018,1020) VR=LO, VM=1-n Software Version(s)  */
    public static final int SoftwareVersions = 0x00181020;

    /** (0018,1022) VR=SH, VM=1 Video Image Format Acquired  */
    public static final int VideoImageFormatAcquired = 0x00181022;

    /** (0018,1023) VR=LO, VM=1 Digital Image Format Acquired  */
    public static final int DigitalImageFormatAcquired = 0x00181023;

    /** (0018,1030) VR=LO, VM=1 Protocol Name  */
    public static final int ProtocolName = 0x00181030;

    /** (0018,1040) VR=LO, VM=1 Contrast/Bolus Route  */
    public static final int ContrastBolusRoute = 0x00181040;

    /** (0018,1041) VR=DS, VM=1 Contrast/Bolus Volume  */
    public static final int ContrastBolusVolume = 0x00181041;

    /** (0018,1042) VR=TM, VM=1 Contrast/Bolus Start Time  */
    public static final int ContrastBolusStartTime = 0x00181042;

    /** (0018,1043) VR=TM, VM=1 Contrast/Bolus Stop Time  */
    public static final int ContrastBolusStopTime = 0x00181043;

    /** (0018,1044) VR=DS, VM=1 Contrast/Bolus Total Dose  */
    public static final int ContrastBolusTotalDose = 0x00181044;

    /** (0018,1045) VR=IS, VM=1 Syringe Counts  */
    public static final int SyringeCounts = 0x00181045;

    /** (0018,1046) VR=DS, VM=1-n Contrast Flow Rate  */
    public static final int ContrastFlowRate = 0x00181046;

    /** (0018,1047) VR=DS, VM=1-n Contrast Flow Duration  */
    public static final int ContrastFlowDuration = 0x00181047;

    /** (0018,1048) VR=CS, VM=1 Contrast/Bolus Ingredient  */
    public static final int ContrastBolusIngredient = 0x00181048;

    /** (0018,1049) VR=DS, VM=1 Contrast/Bolus Ingredient Concentration  */
    public static final int ContrastBolusIngredientConcentration = 0x00181049;

    /** (0018,1050) VR=DS, VM=1 Spatial Resolution  */
    public static final int SpatialResolution = 0x00181050;

    /** (0018,1060) VR=DS, VM=1 Trigger Time  */
    public static final int TriggerTime = 0x00181060;

    /** (0018,1061) VR=LO, VM=1 Trigger Source or Type  */
    public static final int TriggerSourceorType = 0x00181061;

    /** (0018,1062) VR=IS, VM=1 Nominal Interval  */
    public static final int NominalInterval = 0x00181062;

    /** (0018,1063) VR=DS, VM=1 Frame Time  */
    public static final int FrameTime = 0x00181063;

    /** (0018,1064) VR=LO, VM=1 Framing Type  */
    public static final int FramingType = 0x00181064;

    /** (0018,1065) VR=DS, VM=1-n Frame Time Vector  */
    public static final int FrameTimeVector = 0x00181065;

    /** (0018,1066) VR=DS, VM=1 Frame Delay  */
    public static final int FrameDelay = 0x00181066;

    /** (0018,1067) VR=DS, VM=1 Image Trigger Delay  */
    public static final int ImageTriggerDelay = 0x00181067;

    /** (0018,1068) VR=DS, VM=1 Multiplex Group Time Offset  */
    public static final int MultiplexGroupTimeOffset = 0x00181068;

    /** (0018,1069) VR=DS, VM=1 Trigger Time Offset  */
    public static final int TriggerTimeOffset = 0x00181069;

    /** (0018,106A) VR=CS, VM=1 Synchronization Trigger  */
    public static final int SynchronizationTrigger = 0x0018106A;

    /** (0018,106C) VR=US, VM=2 Synchronization Channel  */
    public static final int SynchronizationChannel = 0x0018106C;

    /** (0018,106E) VR=UL, VM=1 Trigger Sample Position  */
    public static final int TriggerSamplePosition = 0x0018106E;

    /** (0018,1070) VR=LO, VM=1 Radiopharmaceutical Route  */
    public static final int RadiopharmaceuticalRoute = 0x00181070;

    /** (0018,1071) VR=DS, VM=1 Radiopharmaceutical Volume  */
    public static final int RadiopharmaceuticalVolume = 0x00181071;

    /** (0018,1072) VR=TM, VM=1 Radiopharmaceutical Start Time  */
    public static final int RadiopharmaceuticalStartTime = 0x00181072;

    /** (0018,1073) VR=TM, VM=1 Radiopharmaceutical Stop Time  */
    public static final int RadiopharmaceuticalStopTime = 0x00181073;

    /** (0018,1074) VR=DS, VM=1 Radionuclide Total Dose  */
    public static final int RadionuclideTotalDose = 0x00181074;

    /** (0018,1075) VR=DS, VM=1 Radionuclide Half Life  */
    public static final int RadionuclideHalfLife = 0x00181075;

    /** (0018,1076) VR=DS, VM=1 Radionuclide Positron Fraction  */
    public static final int RadionuclidePositronFraction = 0x00181076;

    /** (0018,1077) VR=DS, VM=1 Radiopharmaceutical Specific Activity  */
    public static final int RadiopharmaceuticalSpecificActivity = 0x00181077;

    /** (0018,1080) VR=CS, VM=1 Beat Rejection Flag  */
    public static final int BeatRejectionFlag = 0x00181080;

    /** (0018,1081) VR=IS, VM=1 Low R-R Value  */
    public static final int LowRRValue = 0x00181081;

    /** (0018,1082) VR=IS, VM=1 High R-R Value  */
    public static final int HighRRValue = 0x00181082;

    /** (0018,1083) VR=IS, VM=1 Intervals Acquired  */
    public static final int IntervalsAcquired = 0x00181083;

    /** (0018,1084) VR=IS, VM=1 Intervals Rejected  */
    public static final int IntervalsRejected = 0x00181084;

    /** (0018,1085) VR=LO, VM=1 PVC Rejection  */
    public static final int PVCRejection = 0x00181085;

    /** (0018,1086) VR=IS, VM=1 Skip Beats  */
    public static final int SkipBeats = 0x00181086;

    /** (0018,1088) VR=IS, VM=1 Heart Rate  */
    public static final int HeartRate = 0x00181088;

    /** (0018,1090) VR=IS, VM=1 Cardiac Number of Images  */
    public static final int CardiacNumberofImages = 0x00181090;

    /** (0018,1094) VR=IS, VM=1 Trigger Window  */
    public static final int TriggerWindow = 0x00181094;

    /** (0018,1100) VR=DS, VM=1 Reconstruction Diameter  */
    public static final int ReconstructionDiameter = 0x00181100;

    /** (0018,1110) VR=DS, VM=1 Distance Source to Detector  */
    public static final int DistanceSourcetoDetector = 0x00181110;

    /** (0018,1111) VR=DS, VM=1 Distance Source to Patient  */
    public static final int DistanceSourcetoPatient = 0x00181111;

    /** (0018,1114) VR=DS, VM=1 Estimated Radiographic Magnification Factor  */
    public static final int EstimatedRadiographicMagnificationFactor = 0x00181114;

    /** (0018,1120) VR=DS, VM=1 Gantry/Detector Tilt  */
    public static final int GantryDetectorTilt = 0x00181120;

    /** (0018,1121) VR=DS, VM=1 Gantry/Detector Slew  */
    public static final int GantryDetectorSlew = 0x00181121;

    /** (0018,1130) VR=DS, VM=1 Table Height  */
    public static final int TableHeight = 0x00181130;

    /** (0018,1131) VR=DS, VM=1 Table Traverse  */
    public static final int TableTraverse = 0x00181131;

    /** (0018,1134) VR=CS, VM=1 Table Motion  */
    public static final int TableMotion = 0x00181134;

    /** (0018,1135) VR=DS, VM=1-n Table Vertical Increment  */
    public static final int TableVerticalIncrement = 0x00181135;

    /** (0018,1136) VR=DS, VM=1-n Table Lateral Increment  */
    public static final int TableLateralIncrement = 0x00181136;

    /** (0018,1137) VR=DS, VM=1-n Table Longitudinal Increment  */
    public static final int TableLongitudinalIncrement = 0x00181137;

    /** (0018,1138) VR=DS, VM=1 Table Angle  */
    public static final int TableAngle = 0x00181138;

    /** (0018,113A) VR=CS, VM=1 Table Type  */
    public static final int TableType = 0x0018113A;

    /** (0018,1140) VR=CS, VM=1 Rotation Direction  */
    public static final int RotationDirection = 0x00181140;

    /** (0018,1141) VR=DS, VM=1 Angular Position  */
    public static final int AngularPosition = 0x00181141;

    /** (0018,1142) VR=DS, VM=1-n Radial Position  */
    public static final int RadialPosition = 0x00181142;

    /** (0018,1143) VR=DS, VM=1 Scan Arc  */
    public static final int ScanArc = 0x00181143;

    /** (0018,1144) VR=DS, VM=1 Angular Step  */
    public static final int AngularStep = 0x00181144;

    /** (0018,1145) VR=DS, VM=1 Center of Rotation Offset  */
    public static final int CenterofRotationOffset = 0x00181145;

    /** (0018,1146) VR=DS, VM=1-n Rotation Offset RET */
    public static final int RotationOffsetRET = 0x00181146;

    /** (0018,1147) VR=CS, VM=1 Field of View Shape  */
    public static final int FieldofViewShape = 0x00181147;

    /** (0018,1149) VR=IS, VM=1-2 Field of View Dimension(s)  */
    public static final int FieldofViewDimensions = 0x00181149;

    /** (0018,1150) VR=IS, VM=1 Exposure Time  */
    public static final int ExposureTime = 0x00181150;

    /** (0018,1151) VR=IS, VM=1 X-ray Tube Current  */
    public static final int XrayTubeCurrent = 0x00181151;

    /** (0018,1152) VR=IS, VM=1 Exposure  */
    public static final int Exposure = 0x00181152;

    /** (0018,1153) VR=IS, VM=1 Exposure in uAs  */
    public static final int ExposureinuAs = 0x00181153;

    /** (0018,1154) VR=DS, VM=1 Average Pulse Width  */
    public static final int AveragePulseWidth = 0x00181154;

    /** (0018,1155) VR=CS, VM=1 Radiation Setting  */
    public static final int RadiationSetting = 0x00181155;

    /** (0018,1156) VR=CS, VM=1 Rectification Type  */
    public static final int RectificationType = 0x00181156;

    /** (0018,115A) VR=CS, VM=1 Radiation Mode  */
    public static final int RadiationMode = 0x0018115A;

    /** (0018,115E) VR=DS, VM=1 Image Area Dose Product  */
    public static final int ImageAreaDoseProduct = 0x0018115E;

    /** (0018,1160) VR=SH, VM=1 Filter Type  */
    public static final int FilterType = 0x00181160;

    /** (0018,1161) VR=LO, VM=1-n Type of Filters  */
    public static final int TypeofFilters = 0x00181161;

    /** (0018,1162) VR=DS, VM=1 Intensifier Size  */
    public static final int IntensifierSize = 0x00181162;

    /** (0018,1164) VR=DS, VM=2 Imager Pixel Spacing  */
    public static final int ImagerPixelSpacing = 0x00181164;

    /** (0018,1166) VR=CS, VM=1-n Grid  */
    public static final int Grid = 0x00181166;

    /** (0018,1170) VR=IS, VM=1 Generator Power  */
    public static final int GeneratorPower = 0x00181170;

    /** (0018,1180) VR=SH, VM=1 Collimator/grid Name  */
    public static final int CollimatorgridName = 0x00181180;

    /** (0018,1181) VR=CS, VM=1 Collimator Type  */
    public static final int CollimatorType = 0x00181181;

    /** (0018,1182) VR=IS, VM=1-2 Focal Distance  */
    public static final int FocalDistance = 0x00181182;

    /** (0018,1183) VR=DS, VM=1-2 X Focus Center  */
    public static final int XFocusCenter = 0x00181183;

    /** (0018,1184) VR=DS, VM=1-2 Y Focus Center  */
    public static final int YFocusCenter = 0x00181184;

    /** (0018,1190) VR=DS, VM=1-n Focal Spot(s)  */
    public static final int FocalSpots = 0x00181190;

    /** (0018,1191) VR=CS, VM=1 Anode Target Material  */
    public static final int AnodeTargetMaterial = 0x00181191;

    /** (0018,11A0) VR=DS, VM=1 Body Part Thickness  */
    public static final int BodyPartThickness = 0x001811A0;

    /** (0018,11A2) VR=DS, VM=1 Compression Force  */
    public static final int CompressionForce = 0x001811A2;

    /** (0018,1200) VR=DA, VM=1-n Date of Last Calibration  */
    public static final int DateofLastCalibration = 0x00181200;

    /** (0018,1201) VR=TM, VM=1-n Time of Last Calibration  */
    public static final int TimeofLastCalibration = 0x00181201;

    /** (0018,1210) VR=SH, VM=1-n Convolution Kernel  */
    public static final int ConvolutionKernel = 0x00181210;

    /** (0018,1240) VR=, VM= Upper/Lower Pixel Values RET */
    public static final int UpperLowerPixelValuesRET = 0x00181240;

    /** (0018,1242) VR=IS, VM=1 Actual Frame Duration  */
    public static final int ActualFrameDuration = 0x00181242;

    /** (0018,1243) VR=IS, VM=1 Count Rate  */
    public static final int CountRate = 0x00181243;

    /** (0018,1244) VR=US, VM=1 Preferred Playback Sequencing  */
    public static final int PreferredPlaybackSequencing = 0x00181244;

    /** (0018,1250) VR=SH, VM=1 Receive Coil Name  */
    public static final int ReceiveCoilName = 0x00181250;

    /** (0018,1251) VR=SH, VM=1 Transmit Coil Name  */
    public static final int TransmitCoilName = 0x00181251;

    /** (0018,1260) VR=SH, VM=1 Plate Type  */
    public static final int PlateType = 0x00181260;

    /** (0018,1261) VR=LO, VM=1 Phosphor Type  */
    public static final int PhosphorType = 0x00181261;

    /** (0018,1300) VR=DS, VM=1 Scan Velocity  */
    public static final int ScanVelocity = 0x00181300;

    /** (0018,1301) VR=CS, VM=1-n Whole Body Technique  */
    public static final int WholeBodyTechnique = 0x00181301;

    /** (0018,1302) VR=IS, VM=1 Scan Length  */
    public static final int ScanLength = 0x00181302;

    /** (0018,1310) VR=US, VM=4 Acquisition Matrix  */
    public static final int AcquisitionMatrix = 0x00181310;

    /** (0018,1312) VR=CS, VM=1 In-plane Phase Encoding Direction  */
    public static final int InplanePhaseEncodingDirection = 0x00181312;

    /** (0018,1314) VR=DS, VM=1 Flip Angle  */
    public static final int FlipAngle = 0x00181314;

    /** (0018,1315) VR=CS, VM=1 Variable Flip Angle Flag  */
    public static final int VariableFlipAngleFlag = 0x00181315;

    /** (0018,1316) VR=DS, VM=1 SAR  */
    public static final int SAR = 0x00181316;

    /** (0018,1318) VR=DS, VM=1 dB/dt  */
    public static final int dBdt = 0x00181318;

    /** (0018,1400) VR=LO, VM=1 Acquisition Device Processing Description  */
    public static final int AcquisitionDeviceProcessingDescription = 0x00181400;

    /** (0018,1401) VR=LO, VM=1 Acquisition Device Processing Code  */
    public static final int AcquisitionDeviceProcessingCode = 0x00181401;

    /** (0018,1402) VR=CS, VM=1 Cassette Orientation  */
    public static final int CassetteOrientation = 0x00181402;

    /** (0018,1403) VR=CS, VM=1 Cassette Size  */
    public static final int CassetteSize = 0x00181403;

    /** (0018,1404) VR=US, VM=1 Exposures on Plate  */
    public static final int ExposuresonPlate = 0x00181404;

    /** (0018,1405) VR=IS, VM=1 Relative X-ray Exposure  */
    public static final int RelativeXrayExposure = 0x00181405;

    /** (0018,1450) VR=DS, VM=1 Column Angulation  */
    public static final int ColumnAngulation = 0x00181450;

    /** (0018,1460) VR=DS, VM=1 Tomo Layer Height  */
    public static final int TomoLayerHeight = 0x00181460;

    /** (0018,1470) VR=DS, VM=1 Tomo Angle  */
    public static final int TomoAngle = 0x00181470;

    /** (0018,1480) VR=DS, VM=1 Tomo Time  */
    public static final int TomoTime = 0x00181480;

    /** (0018,1490) VR=CS, VM=1 Tomo Type  */
    public static final int TomoType = 0x00181490;

    /** (0018,1491) VR=CS, VM=1 Tomo Class  */
    public static final int TomoClass = 0x00181491;

    /** (0018,1495) VR=IS, VM=1 Number of Tomosynthesis Source Images  */
    public static final int NumberofTomosynthesisSourceImages = 0x00181495;

    /** (0018,1500) VR=CS, VM=1 Positioner Motion  */
    public static final int PositionerMotion = 0x00181500;

    /** (0018,1508) VR=CS, VM=1 Positioner Type  */
    public static final int PositionerType = 0x00181508;

    /** (0018,1510) VR=DS, VM=1 Positioner Primary Angle  */
    public static final int PositionerPrimaryAngle = 0x00181510;

    /** (0018,1511) VR=DS, VM=1 Positioner Secondary Angle  */
    public static final int PositionerSecondaryAngle = 0x00181511;

    /** (0018,1520) VR=DS, VM=1-n Positioner Primary Angle Increment  */
    public static final int PositionerPrimaryAngleIncrement = 0x00181520;

    /** (0018,1521) VR=DS, VM=1-n Positioner Secondary Angle Increment  */
    public static final int PositionerSecondaryAngleIncrement = 0x00181521;

    /** (0018,1530) VR=DS, VM=1 Detector Primary Angle  */
    public static final int DetectorPrimaryAngle = 0x00181530;

    /** (0018,1531) VR=DS, VM=1 Detector Secondary Angle  */
    public static final int DetectorSecondaryAngle = 0x00181531;

    /** (0018,1600) VR=CS, VM=1-3 Shutter Shape  */
    public static final int ShutterShape = 0x00181600;

    /** (0018,1602) VR=IS, VM=1 Shutter Left Vertical Edge  */
    public static final int ShutterLeftVerticalEdge = 0x00181602;

    /** (0018,1604) VR=IS, VM=1 Shutter Right Vertical Edge  */
    public static final int ShutterRightVerticalEdge = 0x00181604;

    /** (0018,1606) VR=IS, VM=1 Shutter Upper Horizontal Edge  */
    public static final int ShutterUpperHorizontalEdge = 0x00181606;

    /** (0018,1608) VR=IS, VM=1 Shutter Lower Horizontal Edge  */
    public static final int ShutterLowerHorizontalEdge = 0x00181608;

    /** (0018,1610) VR=IS, VM=2 Center of Circular Shutter  */
    public static final int CenterofCircularShutter = 0x00181610;

    /** (0018,1612) VR=IS, VM=1 Radius of Circular Shutter  */
    public static final int RadiusofCircularShutter = 0x00181612;

    /** (0018,1620) VR=IS, VM=2-2n Vertices of the Polygonal Shutter  */
    public static final int VerticesofthePolygonalShutter = 0x00181620;

    /** (0018,1622) VR=US, VM=1 Shutter Presentation Value  */
    public static final int ShutterPresentationValue = 0x00181622;

    /** (0018,1623) VR=US, VM=1 Shutter Overlay Group  */
    public static final int ShutterOverlayGroup = 0x00181623;

    /** (0018,1700) VR=CS, VM=1-3 Collimator Shape  */
    public static final int CollimatorShape = 0x00181700;

    /** (0018,1702) VR=IS, VM=1 Collimator Left Vertical Edge  */
    public static final int CollimatorLeftVerticalEdge = 0x00181702;

    /** (0018,1704) VR=IS, VM=1 Collimator Right Vertical Edge  */
    public static final int CollimatorRightVerticalEdge = 0x00181704;

    /** (0018,1706) VR=IS, VM=1 Collimator Upper Horizontal Edge  */
    public static final int CollimatorUpperHorizontalEdge = 0x00181706;

    /** (0018,1708) VR=IS, VM=1 Collimator Lower Horizontal Edge  */
    public static final int CollimatorLowerHorizontalEdge = 0x00181708;

    /** (0018,1710) VR=IS, VM=2 Center of Circular Collimator  */
    public static final int CenterofCircularCollimator = 0x00181710;

    /** (0018,1712) VR=IS, VM=1 Radius of Circular Collimator  */
    public static final int RadiusofCircularCollimator = 0x00181712;

    /** (0018,1720) VR=IS, VM=2-2n Vertices of the Polygonal Collimator  */
    public static final int VerticesofthePolygonalCollimator = 0x00181720;

    /** (0018,1800) VR=CS, VM=1 Acquisition Time Synchronized  */
    public static final int AcquisitionTimeSynchronized = 0x00181800;

    /** (0018,1801) VR=SH, VM=1 Time Source  */
    public static final int TimeSource = 0x00181801;

    /** (0018,1802) VR=CS, VM=1 Time Distribution Protocol  */
    public static final int TimeDistributionProtocol = 0x00181802;

    /** (0018,1803) VR=LO, VM=1 NTP Source Address  */
    public static final int NTPSourceAddress = 0x00181803;

    /** (0018,2001) VR=IS, VM=1-n Page Number Vector  */
    public static final int PageNumberVector = 0x00182001;

    /** (0018,2002) VR=SH, VM=1-n Frame Label Vector  */
    public static final int FrameLabelVector = 0x00182002;

    /** (0018,2003) VR=DS, VM=1-n Frame Primary Angle Vector  */
    public static final int FramePrimaryAngleVector = 0x00182003;

    /** (0018,2004) VR=DS, VM=1-n Frame Secondary Angle Vector  */
    public static final int FrameSecondaryAngleVector = 0x00182004;

    /** (0018,2005) VR=DS, VM=1-n Slice Location Vector  */
    public static final int SliceLocationVector = 0x00182005;

    /** (0018,2006) VR=SH, VM=1-n Display Window Label Vector  */
    public static final int DisplayWindowLabelVector = 0x00182006;

    /** (0018,2010) VR=DS, VM=2 Nominal Scanned Pixel Spacing  */
    public static final int NominalScannedPixelSpacing = 0x00182010;

    /** (0018,2020) VR=CS, VM=1 Digitizing Device Transport Direction  */
    public static final int DigitizingDeviceTransportDirection = 0x00182020;

    /** (0018,2030) VR=DS, VM=1 Rotation of Scanned Film  */
    public static final int RotationofScannedFilm = 0x00182030;

    /** (0018,3100) VR=CS, VM=1 IVUS Acquisition  */
    public static final int IVUSAcquisition = 0x00183100;

    /** (0018,3101) VR=DS, VM=1 IVUS Pullback Rate  */
    public static final int IVUSPullbackRate = 0x00183101;

    /** (0018,3102) VR=DS, VM=1 IVUS Gated Rate  */
    public static final int IVUSGatedRate = 0x00183102;

    /** (0018,3103) VR=IS, VM=1 IVUS Pullback Start Frame Number  */
    public static final int IVUSPullbackStartFrameNumber = 0x00183103;

    /** (0018,3104) VR=IS, VM=1 IVUS Pullback Stop Frame Number  */
    public static final int IVUSPullbackStopFrameNumber = 0x00183104;

    /** (0018,3105) VR=IS, VM=1-n Lesion Number  */
    public static final int LesionNumber = 0x00183105;

    /** (0018,4000) VR=, VM= Comments RET */
    public static final int CommentsRET00184000 = 0x00184000;

    /** (0018,5000) VR=SH, VM=1-n Output Power  */
    public static final int OutputPower = 0x00185000;

    /** (0018,5010) VR=LO, VM=3 Transducer Data  */
    public static final int TransducerData = 0x00185010;

    /** (0018,5012) VR=DS, VM=1 Focus Depth  */
    public static final int FocusDepth = 0x00185012;

    /** (0018,5020) VR=LO, VM=1 Processing Function  */
    public static final int ProcessingFunction = 0x00185020;

    /** (0018,5021) VR=LO, VM=1 Postprocessing Function  */
    public static final int PostprocessingFunction = 0x00185021;

    /** (0018,5022) VR=DS, VM=1 Mechanical Index  */
    public static final int MechanicalIndex = 0x00185022;

    /** (0018,5024) VR=DS, VM=1 Bone Thermal Index  */
    public static final int BoneThermalIndex = 0x00185024;

    /** (0018,5026) VR=DS, VM=1 Cranial Thermal Index  */
    public static final int CranialThermalIndex = 0x00185026;

    /** (0018,5027) VR=DS, VM=1 Soft Tissue Thermal Index  */
    public static final int SoftTissueThermalIndex = 0x00185027;

    /** (0018,5028) VR=DS, VM=1 Soft Tissue-focus Thermal Index  */
    public static final int SoftTissuefocusThermalIndex = 0x00185028;

    /** (0018,5029) VR=DS, VM=1 Soft Tissue-surface Thermal Index  */
    public static final int SoftTissuesurfaceThermalIndex = 0x00185029;

    /** (0018,5030) VR=, VM= Dynamic Range RET */
    public static final int DynamicRangeRET = 0x00185030;

    /** (0018,5040) VR=, VM= Total Gain RET */
    public static final int TotalGainRET = 0x00185040;

    /** (0018,5050) VR=IS, VM=1 Depth of Scan Field  */
    public static final int DepthofScanField = 0x00185050;

    /** (0018,5100) VR=CS, VM=1 Patient Position  */
    public static final int PatientPosition = 0x00185100;

    /** (0018,5101) VR=CS, VM=1 View Position  */
    public static final int ViewPosition = 0x00185101;

    /** (0018,5104) VR=SQ, VM=1 Projection Eponymous Name Code Sequence  */
    public static final int ProjectionEponymousNameCodeSequence = 0x00185104;

    /** (0018,5210) VR=DS, VM=6 Image Transformation Matrix RET */
    public static final int ImageTransformationMatrixRET = 0x00185210;

    /** (0018,5212) VR=DS, VM=3 Image Translation Vector RET */
    public static final int ImageTranslationVectorRET = 0x00185212;

    /** (0018,6000) VR=DS, VM=1 Sensitivity  */
    public static final int Sensitivity = 0x00186000;

    /** (0018,6011) VR=SQ, VM=1 Sequence of Ultrasound Regions  */
    public static final int SequenceofUltrasoundRegions = 0x00186011;

    /** (0018,6012) VR=US, VM=1 Region Spatial Format  */
    public static final int RegionSpatialFormat = 0x00186012;

    /** (0018,6014) VR=US, VM=1 Region Data Type  */
    public static final int RegionDataType = 0x00186014;

    /** (0018,6016) VR=UL, VM=1 Region Flags  */
    public static final int RegionFlags = 0x00186016;

    /** (0018,6018) VR=UL, VM=1 Region Location Min X0  */
    public static final int RegionLocationMinX0 = 0x00186018;

    /** (0018,601A) VR=UL, VM=1 Region Location Min Y0  */
    public static final int RegionLocationMinY0 = 0x0018601A;

    /** (0018,601C) VR=UL, VM=1 Region Location Max X1  */
    public static final int RegionLocationMaxX1 = 0x0018601C;

    /** (0018,601E) VR=UL, VM=1 Region Location Max Y1  */
    public static final int RegionLocationMaxY1 = 0x0018601E;

    /** (0018,6020) VR=SL, VM=1 Reference Pixel X0  */
    public static final int ReferencePixelX0 = 0x00186020;

    /** (0018,6022) VR=SL, VM=1 Reference Pixel Y0  */
    public static final int ReferencePixelY0 = 0x00186022;

    /** (0018,6024) VR=US, VM=1 Physical Units X Direction  */
    public static final int PhysicalUnitsXDirection = 0x00186024;

    /** (0018,6026) VR=US, VM=1 Physical Units Y Direction  */
    public static final int PhysicalUnitsYDirection = 0x00186026;

    /** (0018,6028) VR=FD, VM=1 Reference Pixel Physical Value X  */
    public static final int ReferencePixelPhysicalValueX = 0x00186028;

    /** (0018,602A) VR=FD, VM=1 Reference Pixel Physical Value Y  */
    public static final int ReferencePixelPhysicalValueY = 0x0018602A;

    /** (0018,602C) VR=FD, VM=1 Physical Delta X  */
    public static final int PhysicalDeltaX = 0x0018602C;

    /** (0018,602E) VR=FD, VM=1 Physical Delta Y  */
    public static final int PhysicalDeltaY = 0x0018602E;

    /** (0018,6030) VR=UL, VM=1 Transducer Frequency  */
    public static final int TransducerFrequency = 0x00186030;

    /** (0018,6031) VR=CS, VM=1 Transducer Type  */
    public static final int TransducerType = 0x00186031;

    /** (0018,6032) VR=UL, VM=1 Pulse Repetition Frequency  */
    public static final int PulseRepetitionFrequency = 0x00186032;

    /** (0018,6034) VR=FD, VM=1 Doppler Correction Angle  */
    public static final int DopplerCorrectionAngle = 0x00186034;

    /** (0018,6036) VR=FD, VM=1 Steering Angle  */
    public static final int SteeringAngle = 0x00186036;

    /** (0018,6038) VR=UL, VM=1 Doppler Sample Volume X Position RET */
    public static final int DopplerSampleVolumeXPositionRET = 0x00186038;

    /** (0018,6039) VR=SL, VM=1 Doppler Sample Volume X Position  */
    public static final int DopplerSampleVolumeXPosition = 0x00186039;

    /** (0018,603A) VR=UL, VM=1 Doppler Sample Volume Y Position RET */
    public static final int DopplerSampleVolumeYPositionRET = 0x0018603A;

    /** (0018,603B) VR=SL, VM=1 Doppler Sample Volume Y Position  */
    public static final int DopplerSampleVolumeYPosition = 0x0018603B;

    /** (0018,603C) VR=UL, VM=1 TM-Line Position X0 RET */
    public static final int TMLinePositionX0RET = 0x0018603C;

    /** (0018,603D) VR=SL, VM=1 TM-Line Position X0  */
    public static final int TMLinePositionX0 = 0x0018603D;

    /** (0018,603E) VR=UL, VM=1 TM-Line Position Y0 RET */
    public static final int TMLinePositionY0RET = 0x0018603E;

    /** (0018,603F) VR=SL, VM=1 TM-Line Position Y0  */
    public static final int TMLinePositionY0 = 0x0018603F;

    /** (0018,6040) VR=UL, VM=1 TM-Line Position X1 RET */
    public static final int TMLinePositionX1RET = 0x00186040;

    /** (0018,6041) VR=SL, VM=1 TM-Line Position X1  */
    public static final int TMLinePositionX1 = 0x00186041;

    /** (0018,6042) VR=UL, VM=1 TM-Line Position Y1 RET */
    public static final int TMLinePositionY1RET = 0x00186042;

    /** (0018,6043) VR=SL, VM=1 TM-Line Position Y1  */
    public static final int TMLinePositionY1 = 0x00186043;

    /** (0018,6044) VR=US, VM=1 Pixel Component Organization  */
    public static final int PixelComponentOrganization = 0x00186044;

    /** (0018,6046) VR=UL, VM=1 Pixel Component Mask  */
    public static final int PixelComponentMask = 0x00186046;

    /** (0018,6048) VR=UL, VM=1 Pixel Component Range Start  */
    public static final int PixelComponentRangeStart = 0x00186048;

    /** (0018,604A) VR=UL, VM=1 Pixel Component Range Stop  */
    public static final int PixelComponentRangeStop = 0x0018604A;

    /** (0018,604C) VR=US, VM=1 Pixel Component Physical Units  */
    public static final int PixelComponentPhysicalUnits = 0x0018604C;

    /** (0018,604E) VR=US, VM=1 Pixel Component Data Type  */
    public static final int PixelComponentDataType = 0x0018604E;

    /** (0018,6050) VR=UL, VM=1 Number of Table Break Points  */
    public static final int NumberofTableBreakPoints = 0x00186050;

    /** (0018,6052) VR=UL, VM=1-n Table of X Break Points  */
    public static final int TableofXBreakPoints = 0x00186052;

    /** (0018,6054) VR=FD, VM=1-n Table of Y Break Points  */
    public static final int TableofYBreakPoints = 0x00186054;

    /** (0018,6056) VR=UL, VM=1 Number of Table Entries  */
    public static final int NumberofTableEntries = 0x00186056;

    /** (0018,6058) VR=UL, VM=1-n Table of Pixel Values  */
    public static final int TableofPixelValues = 0x00186058;

    /** (0018,605A) VR=FL, VM=1-n Table of Parameter Values  */
    public static final int TableofParameterValues = 0x0018605A;

    /** (0018,6060) VR=FL, VM=1-n R Wave Time Vector  */
    public static final int RWaveTimeVector = 0x00186060;

    /** (0018,7000) VR=CS, VM=1 Detector Conditions Nominal Flag  */
    public static final int DetectorConditionsNominalFlag = 0x00187000;

    /** (0018,7001) VR=DS, VM=1 Detector Temperature  */
    public static final int DetectorTemperature = 0x00187001;

    /** (0018,7004) VR=CS, VM=1 Detector Type  */
    public static final int DetectorType = 0x00187004;

    /** (0018,7005) VR=CS, VM=1 Detector Configuration  */
    public static final int DetectorConfiguration = 0x00187005;

    /** (0018,7006) VR=LT, VM=1 Detector Description  */
    public static final int DetectorDescription = 0x00187006;

    /** (0018,7008) VR=LT, VM=1 Detector Mode  */
    public static final int DetectorMode = 0x00187008;

    /** (0018,700A) VR=SH, VM=1 Detector ID  */
    public static final int DetectorID = 0x0018700A;

    /** (0018,700C) VR=DA, VM=1 Date of Last Detector Calibration  */
    public static final int DateofLastDetectorCalibration = 0x0018700C;

    /** (0018,700E) VR=TM, VM=1 Time of Last Detector Calibration  */
    public static final int TimeofLastDetectorCalibration = 0x0018700E;

    /** (0018,7010) VR=IS, VM=1 Exposures on Detector Since Last Calibration  */
    public static final int ExposuresonDetectorSinceLastCalibration = 0x00187010;

    /** (0018,7011) VR=IS, VM=1 Exposures on Detector Since Manufactured  */
    public static final int ExposuresonDetectorSinceManufactured = 0x00187011;

    /** (0018,7012) VR=DS, VM=1 Detector Time Since Last Exposure  */
    public static final int DetectorTimeSinceLastExposure = 0x00187012;

    /** (0018,7014) VR=DS, VM=1 Detector Active Time  */
    public static final int DetectorActiveTime = 0x00187014;

    /** (0018,7016) VR=DS, VM=1 Detector Activation Offset From Exposure  */
    public static final int DetectorActivationOffsetFromExposure = 0x00187016;

    /** (0018,701A) VR=DS, VM=2 Detector Binning  */
    public static final int DetectorBinning = 0x0018701A;

    /** (0018,7020) VR=DS, VM=2 Detector Element Physical Size  */
    public static final int DetectorElementPhysicalSize = 0x00187020;

    /** (0018,7022) VR=DS, VM=2 Detector Element Spacing  */
    public static final int DetectorElementSpacing = 0x00187022;

    /** (0018,7024) VR=CS, VM=1 Detector Active Shape  */
    public static final int DetectorActiveShape = 0x00187024;

    /** (0018,7026) VR=DS, VM=1-2 Detector Active Dimension(s)  */
    public static final int DetectorActiveDimensions = 0x00187026;

    /** (0018,7028) VR=DS, VM=2 Detector Active Origin  */
    public static final int DetectorActiveOrigin = 0x00187028;

    /** (0018,702A) VR=LO, VM=1 Detector Manufacturer Name  */
    public static final int DetectorManufacturerName = 0x0018702A;

    /** (0018,702B) VR=LO, VM=1 Detector Manufacturer's Model Name  */
    public static final int DetectorManufacturersModelName = 0x0018702B;

    /** (0018,7030) VR=DS, VM=2 Field of View Origin  */
    public static final int FieldofViewOrigin = 0x00187030;

    /** (0018,7032) VR=DS, VM=1 Field of View Rotation  */
    public static final int FieldofViewRotation = 0x00187032;

    /** (0018,7034) VR=CS, VM=1 Field of View Horizontal Flip  */
    public static final int FieldofViewHorizontalFlip = 0x00187034;

    /** (0018,7040) VR=LT, VM=1 Grid Absorbing Material  */
    public static final int GridAbsorbingMaterial = 0x00187040;

    /** (0018,7041) VR=LT, VM=1 Grid Spacing Material  */
    public static final int GridSpacingMaterial = 0x00187041;

    /** (0018,7042) VR=DS, VM=1 Grid Thickness  */
    public static final int GridThickness = 0x00187042;

    /** (0018,7044) VR=DS, VM=1 Grid Pitch  */
    public static final int GridPitch = 0x00187044;

    /** (0018,7046) VR=IS, VM=2 Grid Aspect Ratio  */
    public static final int GridAspectRatio = 0x00187046;

    /** (0018,7048) VR=DS, VM=1 Grid Period  */
    public static final int GridPeriod = 0x00187048;

    /** (0018,704C) VR=DS, VM=1 Grid Focal Distance  */
    public static final int GridFocalDistance = 0x0018704C;

    /** (0018,7050) VR=CS, VM=1-n Filter Material  */
    public static final int FilterMaterial = 0x00187050;

    /** (0018,7052) VR=DS, VM=1-n Filter Thickness Minimum  */
    public static final int FilterThicknessMinimum = 0x00187052;

    /** (0018,7054) VR=DS, VM=1-n Filter Thickness Maximum  */
    public static final int FilterThicknessMaximum = 0x00187054;

    /** (0018,7060) VR=CS, VM=1 Exposure Control Mode  */
    public static final int ExposureControlMode = 0x00187060;

    /** (0018,7062) VR=LT, VM=1 Exposure Control Mode Description  */
    public static final int ExposureControlModeDescription = 0x00187062;

    /** (0018,7064) VR=CS, VM=1 Exposure Status  */
    public static final int ExposureStatus = 0x00187064;

    /** (0018,7065) VR=DS, VM=1 Phototimer Setting  */
    public static final int PhototimerSetting = 0x00187065;

    /** (0018,8150) VR=DS, VM=1 Exposure Time in uS  */
    public static final int ExposureTimeinuS = 0x00188150;

    /** (0018,8151) VR=DS, VM=1 X-Ray Tube Current in uA  */
    public static final int XRayTubeCurrentinuA = 0x00188151;

    /** (0018,9004) VR=CS, VM=1 Content Qualification  */
    public static final int ContentQualification = 0x00189004;

    /** (0018,9005) VR=SH, VM=1 Pulse Sequence Name  */
    public static final int PulseSequenceName = 0x00189005;

    /** (0018,9006) VR=SQ, VM=1 MR Imaging Modifier Sequence  */
    public static final int MRImagingModifierSequence = 0x00189006;

    /** (0018,9008) VR=CS, VM=1 Echo Pulse Sequence  */
    public static final int EchoPulseSequence = 0x00189008;

    /** (0018,9009) VR=CS, VM=1 Inversion Recovery  */
    public static final int InversionRecovery = 0x00189009;

    /** (0018,9010) VR=CS, VM=1 Flow Compensation  */
    public static final int FlowCompensation = 0x00189010;

    /** (0018,9011) VR=CS, VM=1 Multiple Spin Echo  */
    public static final int MultipleSpinEcho = 0x00189011;

    /** (0018,9012) VR=CS, VM=1 Multi-planar Excitation  */
    public static final int MultiplanarExcitation = 0x00189012;

    /** (0018,9014) VR=CS, VM=1 Phase Contrast  */
    public static final int PhaseContrast = 0x00189014;

    /** (0018,9015) VR=CS, VM=1 Time of Flight Contrast  */
    public static final int TimeofFlightContrast = 0x00189015;

    /** (0018,9016) VR=CS, VM=1 Spoiling  */
    public static final int Spoiling = 0x00189016;

    /** (0018,9017) VR=CS, VM=1 Steady State Pulse Sequence  */
    public static final int SteadyStatePulseSequence = 0x00189017;

    /** (0018,9018) VR=CS, VM=1 Echo Planar Pulse Sequence  */
    public static final int EchoPlanarPulseSequence = 0x00189018;

    /** (0018,9019) VR=FD, VM=1 Tag Angle First Axis  */
    public static final int TagAngleFirstAxis = 0x00189019;

    /** (0018,9020) VR=CS, VM=1 Magnetization Transfer  */
    public static final int MagnetizationTransfer = 0x00189020;

    /** (0018,9021) VR=CS, VM=1 T2 Preparation  */
    public static final int T2Preparation = 0x00189021;

    /** (0018,9022) VR=CS, VM=1 Blood Signal Nulling  */
    public static final int BloodSignalNulling = 0x00189022;

    /** (0018,9024) VR=CS, VM=1 Saturation Recovery  */
    public static final int SaturationRecovery = 0x00189024;

    /** (0018,9025) VR=CS, VM=1 Spectrally Selected Suppression  */
    public static final int SpectrallySelectedSuppression = 0x00189025;

    /** (0018,9026) VR=CS, VM=1 Spectrally Selected Excitation  */
    public static final int SpectrallySelectedExcitation = 0x00189026;

    /** (0018,9027) VR=CS, VM=1 Spatial Pre-saturation  */
    public static final int SpatialPresaturation = 0x00189027;

    /** (0018,9028) VR=CS, VM=1 Tagging  */
    public static final int Tagging = 0x00189028;

    /** (0018,9029) VR=CS, VM=1 Oversampling Phase  */
    public static final int OversamplingPhase = 0x00189029;

    /** (0018,9030) VR=FD, VM=1 Tag Spacing First Dimension  */
    public static final int TagSpacingFirstDimension = 0x00189030;

    /** (0018,9032) VR=CS, VM=1 Geometry of k-Space Traversal  */
    public static final int GeometryofkSpaceTraversal = 0x00189032;

    /** (0018,9033) VR=CS, VM=1 Segmented k-Space Traversal  */
    public static final int SegmentedkSpaceTraversal = 0x00189033;

    /** (0018,9034) VR=CS, VM=1 Rectilinear Phase Encode Reordering  */
    public static final int RectilinearPhaseEncodeReordering = 0x00189034;

    /** (0018,9035) VR=FD, VM=1 Tag Thickness  */
    public static final int TagThickness = 0x00189035;

    /** (0018,9036) VR=CS, VM=1 Partial Fourier Direction  */
    public static final int PartialFourierDirection = 0x00189036;

    /** (0018,9037) VR=CS, VM=1 Cardiac Synchronization Technique  */
    public static final int CardiacSynchronizationTechnique = 0x00189037;

    /** (0018,9041) VR=LO, VM=1 Receive Coil Manufacturer Name  */
    public static final int ReceiveCoilManufacturerName = 0x00189041;

    /** (0018,9042) VR=SQ, VM=1 MR Receive Coil Sequence  */
    public static final int MRReceiveCoilSequence = 0x00189042;

    /** (0018,9043) VR=CS, VM=1 Receive Coil Type  */
    public static final int ReceiveCoilType = 0x00189043;

    /** (0018,9044) VR=CS, VM=1 Quadrature Receive Coil  */
    public static final int QuadratureReceiveCoil = 0x00189044;

    /** (0018,9045) VR=SQ, VM=1 Multi-Coil Definition Sequence  */
    public static final int MultiCoilDefinitionSequence = 0x00189045;

    /** (0018,9046) VR=LO, VM=1 Multi-Coil Configuration  */
    public static final int MultiCoilConfiguration = 0x00189046;

    /** (0018,9047) VR=SH, VM=1 Multi-Coil Element Name  */
    public static final int MultiCoilElementName = 0x00189047;

    /** (0018,9048) VR=CS, VM=1 Multi-Coil Element Used  */
    public static final int MultiCoilElementUsed = 0x00189048;

    /** (0018,9049) VR=SQ, VM=1 MR Transmit Coil Sequence  */
    public static final int MRTransmitCoilSequence = 0x00189049;

    /** (0018,9050) VR=LO, VM=1 Transmit Coil Manufacturer Name  */
    public static final int TransmitCoilManufacturerName = 0x00189050;

    /** (0018,9051) VR=CS, VM=1 Transmit Coil Type  */
    public static final int TransmitCoilType = 0x00189051;

    /** (0018,9052) VR=FD, VM=1-2 Spectral Width  */
    public static final int SpectralWidth = 0x00189052;

    /** (0018,9053) VR=FD, VM=1-2 Chemical Shift Reference  */
    public static final int ChemicalShiftReference = 0x00189053;

    /** (0018,9054) VR=CS, VM=1 Volume Localization Technique  */
    public static final int VolumeLocalizationTechnique = 0x00189054;

    /** (0018,9058) VR=US, VM=1 MR Acquisition Frequency Encoding Steps  */
    public static final int MRAcquisitionFrequencyEncodingSteps = 0x00189058;

    /** (0018,9059) VR=CS, VM=1 De-coupling  */
    public static final int Decoupling = 0x00189059;

    /** (0018,9060) VR=CS, VM=1-2 De-coupled Nucleus  */
    public static final int DecoupledNucleus = 0x00189060;

    /** (0018,9061) VR=FD, VM=1-2 De-coupling Frequency  */
    public static final int DecouplingFrequency = 0x00189061;

    /** (0018,9062) VR=CS, VM=1 De-coupling Method  */
    public static final int DecouplingMethod = 0x00189062;

    /** (0018,9063) VR=FD, VM=1-2 De-coupling Chemical Shift Reference  */
    public static final int DecouplingChemicalShiftReference = 0x00189063;

    /** (0018,9064) VR=CS, VM=1 k-space Filtering  */
    public static final int kspaceFiltering = 0x00189064;

    /** (0018,9065) VR=CS, VM=1-2 Time Domain Filtering  */
    public static final int TimeDomainFiltering = 0x00189065;

    /** (0018,9066) VR=US, VM=1-2 Number of Zero fills  */
    public static final int NumberofZerofills = 0x00189066;

    /** (0018,9067) VR=CS, VM=1 Baseline Correction  */
    public static final int BaselineCorrection = 0x00189067;

    /** (0018,9069) VR=FD, VM=1 Parallel Reduction Factor In-plane  */
    public static final int ParallelReductionFactorInplane = 0x00189069;

    /** (0018,9070) VR=FD, VM=1 Cardiac R-R Interval Specified  */
    public static final int CardiacRRIntervalSpecified = 0x00189070;

    /** (0018,9073) VR=FD, VM=1 Acquisition Duration  */
    public static final int AcquisitionDuration = 0x00189073;

    /** (0018,9074) VR=DT, VM=1 Frame Acquisition Datetime  */
    public static final int FrameAcquisitionDatetime = 0x00189074;

    /** (0018,9075) VR=CS, VM=1 Diffusion Directionality  */
    public static final int DiffusionDirectionality = 0x00189075;

    /** (0018,9076) VR=SQ, VM=1 Diffusion Gradient Direction Sequence  */
    public static final int DiffusionGradientDirectionSequence = 0x00189076;

    /** (0018,9077) VR=CS, VM=1 Parallel Acquisition  */
    public static final int ParallelAcquisition = 0x00189077;

    /** (0018,9078) VR=CS, VM=1 Parallel Acquisition Technique  */
    public static final int ParallelAcquisitionTechnique = 0x00189078;

    /** (0018,9079) VR=FD, VM=1-n Inversion Times  */
    public static final int InversionTimes = 0x00189079;

    /** (0018,9080) VR=ST, VM=1 Metabolite Map Description  */
    public static final int MetaboliteMapDescription = 0x00189080;

    /** (0018,9081) VR=CS, VM=1 Partial Fourier  */
    public static final int PartialFourier = 0x00189081;

    /** (0018,9082) VR=FD, VM=1 Effective Echo Time  */
    public static final int EffectiveEchoTime = 0x00189082;

    /** (0018,9083) VR=SQ, VM=1 Metabolite Map Code Sequence  */
    public static final int MetaboliteMapCodeSequence = 0x00189083;

    /** (0018,9084) VR=SQ, VM=1 Chemical Shift Sequence  */
    public static final int ChemicalShiftSequence = 0x00189084;

    /** (0018,9085) VR=CS, VM=1 Cardiac Signal Source  */
    public static final int CardiacSignalSource = 0x00189085;

    /** (0018,9087) VR=FD, VM=1 Diffusion b-value  */
    public static final int Diffusionbvalue = 0x00189087;

    /** (0018,9089) VR=FD, VM=3 Diffusion Gradient Orientation  */
    public static final int DiffusionGradientOrientation = 0x00189089;

    /** (0018,9090) VR=FD, VM=3 Velocity Encoding Direction  */
    public static final int VelocityEncodingDirection = 0x00189090;

    /** (0018,9091) VR=FD, VM=1 Velocity Encoding Minimum Value  */
    public static final int VelocityEncodingMinimumValue = 0x00189091;

    /** (0018,9093) VR=US, VM=1 Number of k-Space Trajectories  */
    public static final int NumberofkSpaceTrajectories = 0x00189093;

    /** (0018,9094) VR=CS, VM=1 Coverage of k-Space  */
    public static final int CoverageofkSpace = 0x00189094;

    /** (0018,9095) VR=UL, VM=1 Spectroscopy Acquisition Phase Rows  */
    public static final int SpectroscopyAcquisitionPhaseRows = 0x00189095;

    /** (0018,9098) VR=FD, VM=1-2 Transmitter Frequency  */
    public static final int TransmitterFrequency = 0x00189098;

    /** (0018,9100) VR=CS, VM=1-2 Resonant Nucleus  */
    public static final int ResonantNucleus = 0x00189100;

    /** (0018,9101) VR=CS, VM=1 Frequency Correction  */
    public static final int FrequencyCorrection = 0x00189101;

    /** (0018,9103) VR=SQ, VM=1 MR Spectroscopy FOV/Geometry Sequence  */
    public static final int MRSpectroscopyFOVGeometrySequence = 0x00189103;

    /** (0018,9104) VR=FD, VM=1 Slab Thickness  */
    public static final int SlabThickness = 0x00189104;

    /** (0018,9105) VR=FD, VM=3 Slab Orientation  */
    public static final int SlabOrientation = 0x00189105;

    /** (0018,9106) VR=FD, VM=3 Mid Slab Position  */
    public static final int MidSlabPosition = 0x00189106;

    /** (0018,9107) VR=SQ, VM=1 MR Spatial Saturation Sequence  */
    public static final int MRSpatialSaturationSequence = 0x00189107;

    /** (0018,9112) VR=SQ, VM=1 MR Timing and Related Parameters Sequence  */
    public static final int MRTimingandRelatedParametersSequence = 0x00189112;

    /** (0018,9114) VR=SQ, VM=1 MR Echo Sequence  */
    public static final int MREchoSequence = 0x00189114;

    /** (0018,9115) VR=SQ, VM=1 MR Modifier Sequence  */
    public static final int MRModifierSequence = 0x00189115;

    /** (0018,9117) VR=SQ, VM=1 MR Diffusion Sequence  */
    public static final int MRDiffusionSequence = 0x00189117;

    /** (0018,9118) VR=SQ, VM=1 Cardiac Trigger Sequence  */
    public static final int CardiacTriggerSequence = 0x00189118;

    /** (0018,9119) VR=SQ, VM=1 MR Averages Sequence  */
    public static final int MRAveragesSequence = 0x00189119;

    /** (0018,9125) VR=SQ, VM=1 MR FOV/Geometry Sequence  */
    public static final int MRFOVGeometrySequence = 0x00189125;

    /** (0018,9126) VR=SQ, VM=1 Volume Localization Sequence  */
    public static final int VolumeLocalizationSequence = 0x00189126;

    /** (0018,9127) VR=UL, VM=1 Spectroscopy Acquisition Data Columns  */
    public static final int SpectroscopyAcquisitionDataColumns = 0x00189127;

    /** (0018,9147) VR=CS, VM=1 Diffusion Anisotropy Type  */
    public static final int DiffusionAnisotropyType = 0x00189147;

    /** (0018,9151) VR=DT, VM=1 Frame Reference Datetime  */
    public static final int FrameReferenceDatetime = 0x00189151;

    /** (0018,9152) VR=SQ, VM=1 MR Metabolite Map Sequence  */
    public static final int MRMetaboliteMapSequence = 0x00189152;

    /** (0018,9155) VR=FD, VM=1 Parallel Reduction Factor out-of-plane  */
    public static final int ParallelReductionFactoroutofplane = 0x00189155;

    /** (0018,9159) VR=UL, VM=1 Spectroscopy Acquisition Out-of-plane Phase Steps  */
    public static final int SpectroscopyAcquisitionOutofplanePhaseSteps = 0x00189159;

    /** (0018,9166) VR=CS, VM=1 Bulk Motion Status  */
    public static final int BulkMotionStatus = 0x00189166;

    /** (0018,9168) VR=FD, VM=1 Parallel Reduction Factor Second In-plane  */
    public static final int ParallelReductionFactorSecondInplane = 0x00189168;

    /** (0018,9169) VR=CS, VM=1 Cardiac Beat Rejection Technique  */
    public static final int CardiacBeatRejectionTechnique = 0x00189169;

    /** (0018,9170) VR=CS, VM=1 Respiratory Motion Compensation Technique  */
    public static final int RespiratoryMotionCompensationTechnique = 0x00189170;

    /** (0018,9171) VR=CS, VM=1 Respiratory Signal Source  */
    public static final int RespiratorySignalSource = 0x00189171;

    /** (0018,9172) VR=CS, VM=1 Bulk Motion Compensation Technique  */
    public static final int BulkMotionCompensationTechnique = 0x00189172;

    /** (0018,9173) VR=CS, VM=1 Bulk Motion Signal Source  */
    public static final int BulkMotionSignalSource = 0x00189173;

    /** (0018,9174) VR=CS, VM=1 Applicable Safety Standard Agency  */
    public static final int ApplicableSafetyStandardAgency = 0x00189174;

    /** (0018,9175) VR=LO, VM=1 Applicable Safety Standard Description  */
    public static final int ApplicableSafetyStandardDescription = 0x00189175;

    /** (0018,9176) VR=SQ, VM=1 Operating Mode Sequence  */
    public static final int OperatingModeSequence = 0x00189176;

    /** (0018,9177) VR=CS, VM=1 Operating Mode Type  */
    public static final int OperatingModeType = 0x00189177;

    /** (0018,9178) VR=CS, VM=1 Operating Mode  */
    public static final int OperatingMode = 0x00189178;

    /** (0018,9179) VR=CS, VM=1 Specific Absorption Rate Definition  */
    public static final int SpecificAbsorptionRateDefinition = 0x00189179;

    /** (0018,9180) VR=CS, VM=1 Gradient Output Type  */
    public static final int GradientOutputType = 0x00189180;

    /** (0018,9181) VR=FD, VM=1 Specific Absorption Rate Value  */
    public static final int SpecificAbsorptionRateValue = 0x00189181;

    /** (0018,9182) VR=FD, VM=1 Gradient Output  */
    public static final int GradientOutput = 0x00189182;

    /** (0018,9183) VR=CS, VM=1 Flow Compensation Direction  */
    public static final int FlowCompensationDirection = 0x00189183;

    /** (0018,9184) VR=FD, VM=1 Tagging Delay  */
    public static final int TaggingDelay = 0x00189184;

    /** (0018,9195) VR=FD, VM=1 Chemical Shifts Minimum Integration Limit in Hz Retired */
    public static final int ChemicalShiftsMinimumIntegrationLimitinHzRetired = 0x00189195;

    /** (0018,9196) VR=FD, VM=1 Chemical Shifts Maximum Integration Limit in Hz Retired */
    public static final int ChemicalShiftsMaximumIntegrationLimitinHzRetired = 0x00189196;

    /** (0018,9197) VR=SQ, VM=1 MR Velocity Encoding Sequence  */
    public static final int MRVelocityEncodingSequence = 0x00189197;

    /** (0018,9198) VR=CS, VM=1 First Order Phase Correction  */
    public static final int FirstOrderPhaseCorrection = 0x00189198;

    /** (0018,9199) VR=CS, VM=1 Water Referenced Phase Correction  */
    public static final int WaterReferencedPhaseCorrection = 0x00189199;

    /** (0018,9200) VR=CS, VM=1 MR Spectroscopy Acquisition Type  */
    public static final int MRSpectroscopyAcquisitionType = 0x00189200;

    /** (0018,9214) VR=CS, VM=1 Respiratory Cycle Position  */
    public static final int RespiratoryCyclePosition = 0x00189214;

    /** (0018,9217) VR=FD, VM=1 Velocity Encoding Maximum Value  */
    public static final int VelocityEncodingMaximumValue = 0x00189217;

    /** (0018,9218) VR=FD, VM=1 Tag Spacing Second Dimension  */
    public static final int TagSpacingSecondDimension = 0x00189218;

    /** (0018,9219) VR=SS, VM=1 Tag Angle Second Axis  */
    public static final int TagAngleSecondAxis = 0x00189219;

    /** (0018,9220) VR=FD, VM=1 Frame Acquisition Duration  */
    public static final int FrameAcquisitionDuration = 0x00189220;

    /** (0018,9226) VR=SQ, VM=1 MR Image Frame Type Sequence  */
    public static final int MRImageFrameTypeSequence = 0x00189226;

    /** (0018,9227) VR=SQ, VM=1 MR Spectroscopy Frame Type Sequence  */
    public static final int MRSpectroscopyFrameTypeSequence = 0x00189227;

    /** (0018,9231) VR=US, VM=1 MR Acquisition Phase Encoding Steps in-plane  */
    public static final int MRAcquisitionPhaseEncodingStepsinplane = 0x00189231;

    /** (0018,9232) VR=US, VM=1 MR Acquisition Phase Encoding Steps out-of-plane  */
    public static final int MRAcquisitionPhaseEncodingStepsoutofplane = 0x00189232;

    /** (0018,9234) VR=UL, VM=1 Spectroscopy Acquisition Phase Columns  */
    public static final int SpectroscopyAcquisitionPhaseColumns = 0x00189234;

    /** (0018,9236) VR=CS, VM=1 Cardiac Cycle Position  */
    public static final int CardiacCyclePosition = 0x00189236;

    /** (0018,9239) VR=SQ, VM=1 Specific Absorption Rate Sequence  */
    public static final int SpecificAbsorptionRateSequence = 0x00189239;

    /** (0018,9240) VR=US, VM=1 RF Echo Train Length  */
    public static final int RFEchoTrainLength = 0x00189240;

    /** (0018,9241) VR=US, VM=1 Gradient Echo Train Length  */
    public static final int GradientEchoTrainLength = 0x00189241;

    /** (0018,9295) VR=FD, VM=1 Chemical Shifts Minimum Integration Limit in ppm  */
    public static final int ChemicalShiftsMinimumIntegrationLimitinppm = 0x00189295;

    /** (0018,9296) VR=FD, VM=1 Chemical Shifts Maximum Integration Limit in ppm  */
    public static final int ChemicalShiftsMaximumIntegrationLimitinppm = 0x00189296;

    /** (0018,9301) VR=SQ, VM=1 CT Acquisition Type Sequence  */
    public static final int CTAcquisitionTypeSequence = 0x00189301;

    /** (0018,9302) VR=CS, VM=1 Acquisition Type  */
    public static final int AcquisitionType = 0x00189302;

    /** (0018,9303) VR=FD, VM=1 Tube Angle  */
    public static final int TubeAngle = 0x00189303;

    /** (0018,9304) VR=SQ, VM=1 CT Acquisition Details Sequence  */
    public static final int CTAcquisitionDetailsSequence = 0x00189304;

    /** (0018,9305) VR=FD, VM=1 Revolution Time  */
    public static final int RevolutionTime = 0x00189305;

    /** (0018,9306) VR=FD, VM=1 Single Collimation Width  */
    public static final int SingleCollimationWidth = 0x00189306;

    /** (0018,9307) VR=FD, VM=1 Total Collimation Width  */
    public static final int TotalCollimationWidth = 0x00189307;

    /** (0018,9308) VR=SQ, VM=1 CT Table Dynamics Sequence  */
    public static final int CTTableDynamicsSequence = 0x00189308;

    /** (0018,9309) VR=FD, VM=1 Table Speed  */
    public static final int TableSpeed = 0x00189309;

    /** (0018,9310) VR=FD, VM=1 Table Feed per Rotation  */
    public static final int TableFeedperRotation = 0x00189310;

    /** (0018,9311) VR=FD, VM=1 Spiral Pitch Factor  */
    public static final int SpiralPitchFactor = 0x00189311;

    /** (0018,9312) VR=SQ, VM=1 CT Geometry Sequence  */
    public static final int CTGeometrySequence = 0x00189312;

    /** (0018,9313) VR=FD, VM=3 Data Collection Center (Patient)  */
    public static final int DataCollectionCenterPatient = 0x00189313;

    /** (0018,9314) VR=SQ, VM=1 CT Reconstruction Sequence  */
    public static final int CTReconstructionSequence = 0x00189314;

    /** (0018,9315) VR=CS, VM=1 Reconstruction Algorithm  */
    public static final int ReconstructionAlgorithm = 0x00189315;

    /** (0018,9316) VR=CS, VM=1 Convolution Kernel Group  */
    public static final int ConvolutionKernelGroup = 0x00189316;

    /** (0018,9317) VR=FD, VM=2 Reconstruction Field of View  */
    public static final int ReconstructionFieldofView = 0x00189317;

    /** (0018,9318) VR=FD, VM=3 Reconstruction Target Center (Patient)  */
    public static final int ReconstructionTargetCenterPatient = 0x00189318;

    /** (0018,9319) VR=FD, VM=1 Reconstruction Angle  */
    public static final int ReconstructionAngle = 0x00189319;

    /** (0018,9320) VR=SH, VM=1 Image Filter  */
    public static final int ImageFilter = 0x00189320;

    /** (0018,9321) VR=SQ, VM=1 CT Exposure Sequence  */
    public static final int CTExposureSequence = 0x00189321;

    /** (0018,9322) VR=FD, VM=2 Reconstruction Pixel Spacing  */
    public static final int ReconstructionPixelSpacing = 0x00189322;

    /** (0018,9323) VR=CS, VM=1 Exposure Modulation Type  */
    public static final int ExposureModulationType = 0x00189323;

    /** (0018,9324) VR=FD, VM=1 Estimated Dose Saving  */
    public static final int EstimatedDoseSaving = 0x00189324;

    /** (0018,9325) VR=SQ, VM=1 CT X-ray Details Sequence  */
    public static final int CTXrayDetailsSequence = 0x00189325;

    /** (0018,9326) VR=SQ, VM=1 CT Position Sequence  */
    public static final int CTPositionSequence = 0x00189326;

    /** (0018,9327) VR=FD, VM=1 Table Position  */
    public static final int TablePosition = 0x00189327;

    /** (0018,9328) VR=FD, VM=1 Exposure Time in ms  */
    public static final int ExposureTimeinms = 0x00189328;

    /** (0018,9329) VR=SQ, VM=1 CT Image Frame Type Sequence  */
    public static final int CTImageFrameTypeSequence = 0x00189329;

    /** (0018,9330) VR=FD, VM=1 X-Ray Tube Current in mA  */
    public static final int XRayTubeCurrentinmA = 0x00189330;

    /** (0018,9332) VR=FD, VM=1 Exposure in mAs  */
    public static final int ExposureinmAs = 0x00189332;

    /** (0018,9333) VR=CS, VM=1 Constant Volume Flag  */
    public static final int ConstantVolumeFlag = 0x00189333;

    /** (0018,9334) VR=CS, VM=1 Fluoroscopy Flag  */
    public static final int FluoroscopyFlag = 0x00189334;

    /** (0018,9335) VR=FD, VM=1 Distance Source to Data Collection Center  */
    public static final int DistanceSourcetoDataCollectionCenter = 0x00189335;

    /** (0018,9337) VR=US, VM=1 Contrast/Bolus Agent Number  */
    public static final int ContrastBolusAgentNumber = 0x00189337;

    /** (0018,9338) VR=SQ, VM=1 Contrast/Bolus Ingredient Code Sequence  */
    public static final int ContrastBolusIngredientCodeSequence = 0x00189338;

    /** (0018,9340) VR=SQ, VM=1 Contrast Administration Profile Sequence  */
    public static final int ContrastAdministrationProfileSequence = 0x00189340;

    /** (0018,9341) VR=SQ, VM=1 Contrast/Bolus Usage Sequence  */
    public static final int ContrastBolusUsageSequence = 0x00189341;

    /** (0018,9342) VR=CS, VM=1 Contrast/Bolus Agent Administered  */
    public static final int ContrastBolusAgentAdministered = 0x00189342;

    /** (0018,9343) VR=CS, VM=1 Contrast/Bolus Agent Detected  */
    public static final int ContrastBolusAgentDetected = 0x00189343;

    /** (0018,9344) VR=CS, VM=1 Contrast/Bolus Agent Phase  */
    public static final int ContrastBolusAgentPhase = 0x00189344;

    /** (0018,9345) VR=FD, VM=1 CTDIvol  */
    public static final int CTDIvol = 0x00189345;

    /** (0018,A001) VR=SQ, VM=1 Contributing Equipment Sequence  */
    public static final int ContributingEquipmentSequence = 0x0018A001;

    /** (0018,A002) VR=DT, VM=1 Contribution DateTime  */
    public static final int ContributionDateTime = 0x0018A002;

    /** (0018,A003) VR=ST, VM=1 Contribution Description  */
    public static final int ContributionDescription = 0x0018A003;

    /** (0020,000D) VR=UI, VM=1 Study Instance UID  */
    public static final int StudyInstanceUID = 0x0020000D;

    /** (0020,000E) VR=UI, VM=1 Series Instance UID  */
    public static final int SeriesInstanceUID = 0x0020000E;

    /** (0020,0010) VR=SH, VM=1 Study ID  */
    public static final int StudyID = 0x00200010;

    /** (0020,0011) VR=IS, VM=1 Series Number  */
    public static final int SeriesNumber = 0x00200011;

    /** (0020,0012) VR=IS, VM=1 Acquisition Number  */
    public static final int AcquisitionNumber = 0x00200012;

    /** (0020,0013) VR=IS, VM=1 Instance Number  */
    public static final int InstanceNumber = 0x00200013;

    /** (0020,0014) VR=IS, VM=1 Isotope Number RET */
    public static final int IsotopeNumberRET = 0x00200014;

    /** (0020,0015) VR=IS, VM=1 Phase Number RET */
    public static final int PhaseNumberRET = 0x00200015;

    /** (0020,0016) VR=IS, VM=1 Interval Number RET */
    public static final int IntervalNumberRET = 0x00200016;

    /** (0020,0017) VR=IS, VM=1 Time Slot Number RET */
    public static final int TimeSlotNumberRET = 0x00200017;

    /** (0020,0018) VR=IS, VM=1 Angle Number RET */
    public static final int AngleNumberRET = 0x00200018;

    /** (0020,0019) VR=IS, VM=1 Item Number  */
    public static final int ItemNumber = 0x00200019;

    /** (0020,0020) VR=CS, VM=2 Patient Orientation  */
    public static final int PatientOrientation = 0x00200020;

    /** (0020,0022) VR=IS, VM=1 Overlay Number  */
    public static final int OverlayNumber = 0x00200022;

    /** (0020,0024) VR=IS, VM=1 Curve Number  */
    public static final int CurveNumber = 0x00200024;

    /** (0020,0026) VR=IS, VM=1 Lookup Table Number  */
    public static final int LookupTableNumber = 0x00200026;

    /** (0020,0030) VR=, VM= Image Position RET */
    public static final int ImagePositionRET = 0x00200030;

    /** (0020,0032) VR=DS, VM=3 Image Position (Patient)  */
    public static final int ImagePositionPatient = 0x00200032;

    /** (0020,0035) VR=, VM= Image Orientation RET */
    public static final int ImageOrientationRET = 0x00200035;

    /** (0020,0037) VR=DS, VM=6 Image Orientation (Patient)  */
    public static final int ImageOrientationPatient = 0x00200037;

    /** (0020,0050) VR=, VM= Location RET */
    public static final int LocationRET = 0x00200050;

    /** (0020,0052) VR=UI, VM=1 Frame of Reference UID  */
    public static final int FrameofReferenceUID = 0x00200052;

    /** (0020,0060) VR=CS, VM=1 Laterality  */
    public static final int Laterality = 0x00200060;

    /** (0020,0062) VR=CS, VM=1 Image Laterality  */
    public static final int ImageLaterality = 0x00200062;

    /** (0020,0070) VR=, VM= Image Geometry Type RET */
    public static final int ImageGeometryTypeRET = 0x00200070;

    /** (0020,0080) VR=, VM= Masking Image RET */
    public static final int MaskingImageRET = 0x00200080;

    /** (0020,0100) VR=IS, VM=1 Temporal Position Identifier  */
    public static final int TemporalPositionIdentifier = 0x00200100;

    /** (0020,0105) VR=IS, VM=1 Number of Temporal Positions  */
    public static final int NumberofTemporalPositions = 0x00200105;

    /** (0020,0110) VR=DS, VM=1 Temporal Resolution  */
    public static final int TemporalResolution = 0x00200110;

    /** (0020,0200) VR=UI, VM=1 Synchronization Frame of Reference UID  */
    public static final int SynchronizationFrameofReferenceUID = 0x00200200;

    /** (0020,1000) VR=IS, VM=1 Series in Study  */
    public static final int SeriesinStudy = 0x00201000;

    /** (0020,1001) VR=, VM= Acquisitions in Series RET */
    public static final int AcquisitionsinSeriesRET = 0x00201001;

    /** (0020,1002) VR=IS, VM=1 Images in Acquisition  */
    public static final int ImagesinAcquisition = 0x00201002;

    /** (0020,1004) VR=IS, VM=1 Acquisitions in Study  */
    public static final int AcquisitionsinStudy = 0x00201004;

    /** (0020,1020) VR=, VM= Reference RET */
    public static final int ReferenceRET = 0x00201020;

    /** (0020,1040) VR=LO, VM=1 Position Reference Indicator  */
    public static final int PositionReferenceIndicator = 0x00201040;

    /** (0020,1041) VR=DS, VM=1 Slice Location  */
    public static final int SliceLocation = 0x00201041;

    /** (0020,1070) VR=IS, VM=1-n Other Study Numbers  */
    public static final int OtherStudyNumbers = 0x00201070;

    /** (0020,1200) VR=IS, VM=1 Number of Patient Related Studies  */
    public static final int NumberofPatientRelatedStudies = 0x00201200;

    /** (0020,1202) VR=IS, VM=1 Number of Patient Related Series  */
    public static final int NumberofPatientRelatedSeries = 0x00201202;

    /** (0020,1204) VR=IS, VM=1 Number of Patient Related Instances  */
    public static final int NumberofPatientRelatedInstances = 0x00201204;

    /** (0020,1206) VR=IS, VM=1 Number of Study Related Series  */
    public static final int NumberofStudyRelatedSeries = 0x00201206;

    /** (0020,1208) VR=IS, VM=1 Number of Study Related Instances  */
    public static final int NumberofStudyRelatedInstances = 0x00201208;

    /** (0020,1209) VR=IS, VM=1 Number of Series Related Instances  */
    public static final int NumberofSeriesRelatedInstances = 0x00201209;

    /** (0020,31xx) VR=, VM= Source Image Ids RET */
    public static final int SourceImageIdsRET = 0x00203100;

    /** (0020,3401) VR=, VM= Modifying Device ID RET */
    public static final int ModifyingDeviceIDRET = 0x00203401;

    /** (0020,3402) VR=, VM= Modified Image ID RET */
    public static final int ModifiedImageIDRET = 0x00203402;

    /** (0020,3403) VR=, VM= Modified Image Date RET */
    public static final int ModifiedImageDateRET = 0x00203403;

    /** (0020,3404) VR=, VM= Modifying Device Manufacturer RET */
    public static final int ModifyingDeviceManufacturerRET = 0x00203404;

    /** (0020,3405) VR=, VM= Modified Image Time RET */
    public static final int ModifiedImageTimeRET = 0x00203405;

    /** (0020,3406) VR=, VM= Modified Image Description RET */
    public static final int ModifiedImageDescriptionRET = 0x00203406;

    /** (0020,4000) VR=LT, VM=1 Image Comments  */
    public static final int ImageComments = 0x00204000;

    /** (0020,5000) VR=, VM= Original Image Identification RET */
    public static final int OriginalImageIdentificationRET = 0x00205000;

    /** (0020,5002) VR=, VM= Original Image Identification Nomenclature RET */
    public static final int OriginalImageIdentificationNomenclatureRET = 0x00205002;

    /** (0020,9056) VR=SH, VM=1 Stack ID  */
    public static final int StackID = 0x00209056;

    /** (0020,9057) VR=UL, VM=1 In-Stack Position Number  */
    public static final int InStackPositionNumber = 0x00209057;

    /** (0020,9071) VR=SQ, VM=1 Frame Anatomy Sequence  */
    public static final int FrameAnatomySequence = 0x00209071;

    /** (0020,9072) VR=CS, VM=1 Frame Laterality  */
    public static final int FrameLaterality = 0x00209072;

    /** (0020,9111) VR=SQ, VM=1 Frame Content Sequence  */
    public static final int FrameContentSequence = 0x00209111;

    /** (0020,9113) VR=SQ, VM=1 Plane Position Sequence  */
    public static final int PlanePositionSequence = 0x00209113;

    /** (0020,9116) VR=SQ, VM=1 Plane Orientation Sequence  */
    public static final int PlaneOrientationSequence = 0x00209116;

    /** (0020,9128) VR=UL, VM=1 Temporal Position Index  */
    public static final int TemporalPositionIndex = 0x00209128;

    /** (0020,9153) VR=FD, VM=1 Trigger Delay Time  */
    public static final int TriggerDelayTime = 0x00209153;

    /** (0020,9156) VR=US, VM=1 Frame Acquisition Number  */
    public static final int FrameAcquisitionNumber = 0x00209156;

    /** (0020,9157) VR=UL, VM=1-n Dimension Index Values  */
    public static final int DimensionIndexValues = 0x00209157;

    /** (0020,9158) VR=LT, VM=1 Frame Comments  */
    public static final int FrameComments = 0x00209158;

    /** (0020,9161) VR=UI, VM=1 Concatenation UID  */
    public static final int ConcatenationUID = 0x00209161;

    /** (0020,9162) VR=US, VM=1 In-concatenation Number  */
    public static final int InconcatenationNumber = 0x00209162;

    /** (0020,9163) VR=US, VM=1 In-concatenation Total Number  */
    public static final int InconcatenationTotalNumber = 0x00209163;

    /** (0020,9164) VR=UI, VM=1 Dimension Organization UID  */
    public static final int DimensionOrganizationUID = 0x00209164;

    /** (0020,9165) VR=AT, VM=1 Dimension Index Pointer  */
    public static final int DimensionIndexPointer = 0x00209165;

    /** (0020,9167) VR=AT, VM=1 Functional Group Pointer  */
    public static final int FunctionalGroupPointer = 0x00209167;

    /** (0020,9213) VR=LO, VM=1 Dimension Index Private Creator  */
    public static final int DimensionIndexPrivateCreator = 0x00209213;

    /** (0020,9221) VR=SQ, VM=1 Dimension Organization Sequence  */
    public static final int DimensionOrganizationSequence = 0x00209221;

    /** (0020,9222) VR=SQ, VM=1 Dimension Index Sequence  */
    public static final int DimensionIndexSequence = 0x00209222;

    /** (0020,9228) VR=UL, VM=1 Concatenation Frame Offset Number  */
    public static final int ConcatenationFrameOffsetNumber = 0x00209228;

    /** (0020,9238) VR=LO, VM=1 Functional Group Private Creator  */
    public static final int FunctionalGroupPrivateCreator = 0x00209238;

    /** (0022,0001) VR=US, VM=1 Light Path Filter Pass-Through Wavelength  */
    public static final int LightPathFilterPassThroughWavelength = 0x00220001;

    /** (0022,0002) VR=US, VM=2 Light Path Filter Pass Band  */
    public static final int LightPathFilterPassBand = 0x00220002;

    /** (0022,0003) VR=US, VM=1 Image Path Filter Pass-Through Wavelength  */
    public static final int ImagePathFilterPassThroughWavelength = 0x00220003;

    /** (0022,0004) VR=US, VM=2 Image Path Filter Pass Band  */
    public static final int ImagePathFilterPassBand = 0x00220004;

    /** (0022,0005) VR=CS, VM=1 Patient Eye Movement Commanded  */
    public static final int PatientEyeMovementCommanded = 0x00220005;

    /** (0022,0006) VR=SQ, VM=1 Patient Eye Movement Command Code Sequence  */
    public static final int PatientEyeMovementCommandCodeSequence = 0x00220006;

    /** (0022,0007) VR=FL, VM=1 Spherical Lens Power  */
    public static final int SphericalLensPower = 0x00220007;

    /** (0022,0008) VR=FL, VM=1 Cylinder Lens Power  */
    public static final int CylinderLensPower = 0x00220008;

    /** (0022,0009) VR=FL, VM=1 Cylinder Axis  */
    public static final int CylinderAxis = 0x00220009;

    /** (0022,000A) VR=FL, VM=1 Emmetropic Magnification  */
    public static final int EmmetropicMagnification = 0x0022000A;

    /** (0022,000B) VR=FL, VM=1 Intra Ocular Pressure  */
    public static final int IntraOcularPressure = 0x0022000B;

    /** (0022,000C) VR=FL, VM=1 Horizontal Field of View  */
    public static final int HorizontalFieldofView = 0x0022000C;

    /** (0022,000D) VR=CS, VM=1 Pupil Dilated  */
    public static final int PupilDilated = 0x0022000D;

    /** (0022,000E) VR=FL, VM=1 Degree of Dilation  */
    public static final int DegreeofDilation = 0x0022000E;

    /** (0022,0010) VR=FL, VM=1 Stereo Baseline Angle  */
    public static final int StereoBaselineAngle = 0x00220010;

    /** (0022,0011) VR=FL, VM=1 Stereo Baseline Displacement  */
    public static final int StereoBaselineDisplacement = 0x00220011;

    /** (0022,0012) VR=FL, VM=1 Stereo Horizontal Pixel Offset  */
    public static final int StereoHorizontalPixelOffset = 0x00220012;

    /** (0022,0013) VR=FL, VM=1 Stereo Vertical Pixel Offset  */
    public static final int StereoVerticalPixelOffset = 0x00220013;

    /** (0022,0014) VR=FL, VM=1 Stereo Rotation  */
    public static final int StereoRotation = 0x00220014;

    /** (0022,0015) VR=SQ, VM=1 Acquisition Device Type Code Sequence  */
    public static final int AcquisitionDeviceTypeCodeSequence = 0x00220015;

    /** (0022,0016) VR=SQ, VM=1 Illumination Type Code Sequence  */
    public static final int IlluminationTypeCodeSequence = 0x00220016;

    /** (0022,0017) VR=SQ, VM=1 Light Path Filter Type Stack Code Sequence  */
    public static final int LightPathFilterTypeStackCodeSequence = 0x00220017;

    /** (0022,0018) VR=SQ, VM=1 Image Path Filter Type Stack Code Sequence  */
    public static final int ImagePathFilterTypeStackCodeSequence = 0x00220018;

    /** (0022,0019) VR=SQ, VM=1 Lenses Code Sequence  */
    public static final int LensesCodeSequence = 0x00220019;

    /** (0022,001A) VR=SQ, VM=1 Channel Description Code Sequence  */
    public static final int ChannelDescriptionCodeSequence = 0x0022001A;

    /** (0022,001B) VR=SQ, VM=1 Refractive State Sequence  */
    public static final int RefractiveStateSequence = 0x0022001B;

    /** (0022,001C) VR=SQ, VM=1 Mydriatic Agent Code Sequence  */
    public static final int MydriaticAgentCodeSequence = 0x0022001C;

    /** (0022,001D) VR=SQ, VM=1 Relative Image Position Code Sequence  */
    public static final int RelativeImagePositionCodeSequence = 0x0022001D;

    /** (0022,0020) VR=SQ, VM=1 Stereo Pairs Sequence  */
    public static final int StereoPairsSequence = 0x00220020;

    /** (0022,0021) VR=SQ, VM=1 Left Image Sequence  */
    public static final int LeftImageSequence = 0x00220021;

    /** (0022,0022) VR=SQ, VM=1 Right Image Sequence  */
    public static final int RightImageSequence = 0x00220022;

    /** (0028,0002) VR=US, VM=1 Samples per Pixel  */
    public static final int SamplesperPixel = 0x00280002;

    /** (0028,0003) VR=US, VM=1 Samples per Pixel Used  */
    public static final int SamplesperPixelUsed = 0x00280003;

    /** (0028,0004) VR=CS, VM=1 Photometric Interpretation  */
    public static final int PhotometricInterpretation = 0x00280004;

    /** (0028,0005) VR=, VM= Image Dimensions RET */
    public static final int ImageDimensionsRET = 0x00280005;

    /** (0028,0006) VR=US, VM=1 Planar Configuration  */
    public static final int PlanarConfiguration = 0x00280006;

    /** (0028,0008) VR=IS, VM=1 Number of Frames  */
    public static final int NumberofFrames = 0x00280008;

    /** (0028,0009) VR=AT, VM=1-n Frame Increment Pointer  */
    public static final int FrameIncrementPointer = 0x00280009;

    /** (0028,000A) VR=AT, VM=1-n Frame Dimension Pointer  */
    public static final int FrameDimensionPointer = 0x0028000A;

    /** (0028,0010) VR=US, VM=1 Rows  */
    public static final int Rows = 0x00280010;

    /** (0028,0011) VR=US, VM=1 Columns  */
    public static final int Columns = 0x00280011;

    /** (0028,0012) VR=US, VM=1 Planes  */
    public static final int Planes = 0x00280012;

    /** (0028,0014) VR=US, VM=1 Ultrasound Color Data Present  */
    public static final int UltrasoundColorDataPresent = 0x00280014;

    /** (0028,0030) VR=DS, VM=2 Pixel Spacing  */
    public static final int PixelSpacing = 0x00280030;

    /** (0028,0031) VR=DS, VM=2 Zoom Factor  */
    public static final int ZoomFactor = 0x00280031;

    /** (0028,0032) VR=DS, VM=2 Zoom Center  */
    public static final int ZoomCenter = 0x00280032;

    /** (0028,0034) VR=IS, VM=2 Pixel Aspect Ratio  */
    public static final int PixelAspectRatio = 0x00280034;

    /** (0028,0040) VR=, VM= Image Format RET */
    public static final int ImageFormatRET = 0x00280040;

    /** (0028,0050) VR=, VM= Manipulated Image RET */
    public static final int ManipulatedImageRET = 0x00280050;

    /** (0028,0051) VR=CS, VM=1-n Corrected Image  */
    public static final int CorrectedImage = 0x00280051;

    /** (0028,0060) VR=, VM= Compression Code RET */
    public static final int CompressionCodeRET00280060 = 0x00280060;

    /** (0028,0100) VR=US, VM=1 Bits Allocated  */
    public static final int BitsAllocated = 0x00280100;

    /** (0028,0101) VR=US, VM=1 Bits Stored  */
    public static final int BitsStored = 0x00280101;

    /** (0028,0102) VR=US, VM=1 High Bit  */
    public static final int HighBit = 0x00280102;

    /** (0028,0103) VR=US, VM=1 Pixel Representation  */
    public static final int PixelRepresentation = 0x00280103;

    /** (0028,0104) VR=, VM= Smallest Valid Pixel Value RET */
    public static final int SmallestValidPixelValueRET = 0x00280104;

    /** (0028,0105) VR=, VM= Largest Valid Pixel Value RET */
    public static final int LargestValidPixelValueRET = 0x00280105;

    /** (0028,0106) VR=US|SS, VM=1 Smallest Image Pixel Value  */
    public static final int SmallestImagePixelValue = 0x00280106;

    /** (0028,0107) VR=US|SS, VM=1 Largest Image Pixel Value  */
    public static final int LargestImagePixelValue = 0x00280107;

    /** (0028,0108) VR=US|SS, VM=1 Smallest Pixel Value in Series  */
    public static final int SmallestPixelValueinSeries = 0x00280108;

    /** (0028,0109) VR=US|SS, VM=1 Largest Pixel Value in Series  */
    public static final int LargestPixelValueinSeries = 0x00280109;

    /** (0028,0110) VR=US|SS, VM=1 Smallest Image Pixel Value in Plane  */
    public static final int SmallestImagePixelValueinPlane = 0x00280110;

    /** (0028,0111) VR=US|SS, VM=1 Largest Image Pixel Value in Plane  */
    public static final int LargestImagePixelValueinPlane = 0x00280111;

    /** (0028,0120) VR=US|SS, VM=1 Pixel Padding Value  */
    public static final int PixelPaddingValue = 0x00280120;

    /** (0028,0200) VR=, VM= Image Location RET */
    public static final int ImageLocationRET = 0x00280200;

    /** (0028,0300) VR=CS, VM=1 Quality Control Image  */
    public static final int QualityControlImage = 0x00280300;

    /** (0028,0301) VR=CS, VM=1 Burned In Annotation  */
    public static final int BurnedInAnnotation = 0x00280301;

    /** (0028,1040) VR=CS, VM=1 Pixel Intensity Relationship  */
    public static final int PixelIntensityRelationship = 0x00281040;

    /** (0028,1041) VR=SS, VM=1 Pixel Intensity Relationship Sign  */
    public static final int PixelIntensityRelationshipSign = 0x00281041;

    /** (0028,1050) VR=DS, VM=1-n Window Center  */
    public static final int WindowCenter = 0x00281050;

    /** (0028,1051) VR=DS, VM=1-n Window Width  */
    public static final int WindowWidth = 0x00281051;

    /** (0028,1052) VR=DS, VM=1 Rescale Intercept  */
    public static final int RescaleIntercept = 0x00281052;

    /** (0028,1053) VR=DS, VM=1 Rescale Slope  */
    public static final int RescaleSlope = 0x00281053;

    /** (0028,1054) VR=LO, VM=1 Rescale Type  */
    public static final int RescaleType = 0x00281054;

    /** (0028,1055) VR=LO, VM=1-n Window Center & Width Explanation  */
    public static final int WindowCenterWidthExplanation = 0x00281055;

    /** (0028,1080) VR=, VM= Gray Scale RET */
    public static final int GrayScaleRET = 0x00281080;

    /** (0028,1090) VR=CS, VM=1 Recommended Viewing Mode  */
    public static final int RecommendedViewingMode = 0x00281090;

    /** (0028,1100) VR=, VM= Gray Lookup Table Descriptor RET */
    public static final int GrayLookupTableDescriptorRET = 0x00281100;

    /** (0028,1101) VR=US|SS, VM=3 Red Palette Color Lookup Table Descriptor  */
    public static final int RedPaletteColorLookupTableDescriptor = 0x00281101;

    /** (0028,1102) VR=US|SS, VM=3 Green Palette Color Lookup Table Descriptor  */
    public static final int GreenPaletteColorLookupTableDescriptor = 0x00281102;

    /** (0028,1103) VR=US|SS, VM=3 Blue Palette Color Lookup Table Descriptor  */
    public static final int BluePaletteColorLookupTableDescriptor = 0x00281103;

    /** (0028,1199) VR=UI, VM=1 Palette Color Lookup Table UID  */
    public static final int PaletteColorLookupTableUID = 0x00281199;

    /** (0028,1200) VR=, VM= Gray Lookup Table Data RET */
    public static final int GrayLookupTableDataRET = 0x00281200;

    /** (0028,1201) VR=OW, VM=1 Red Palette Color Lookup Table Data  */
    public static final int RedPaletteColorLookupTableData = 0x00281201;

    /** (0028,1202) VR=OW, VM=1 Green Palette Color Lookup Table Data  */
    public static final int GreenPaletteColorLookupTableData = 0x00281202;

    /** (0028,1203) VR=OW, VM=1 Blue Palette Color Lookup Table Data  */
    public static final int BluePaletteColorLookupTableData = 0x00281203;

    /** (0028,1221) VR=OW, VM=1 Segmented Red Palette Color Lookup Table Data  */
    public static final int SegmentedRedPaletteColorLookupTableData = 0x00281221;

    /** (0028,1222) VR=OW, VM=1 Segmented Green Palette Color Lookup Table Data  */
    public static final int SegmentedGreenPaletteColorLookupTableData = 0x00281222;

    /** (0028,1223) VR=OW, VM=1 Segmented Blue Palette Color Lookup Table Data  */
    public static final int SegmentedBluePaletteColorLookupTableData = 0x00281223;

    /** (0028,1300) VR=CS, VM=1 Implant Present  */
    public static final int ImplantPresent = 0x00281300;

    /** (0028,1350) VR=CS, VM=1 Partial View  */
    public static final int PartialView = 0x00281350;

    /** (0028,1351) VR=ST, VM=1 Partial View Description  */
    public static final int PartialViewDescription = 0x00281351;

    /** (0028,2110) VR=CS, VM=1 Lossy Image Compression  */
    public static final int LossyImageCompression = 0x00282110;

    /** (0028,2112) VR=DS, VM=1-n Lossy Image Compression Ratio  */
    public static final int LossyImageCompressionRatio = 0x00282112;

    /** (0028,2114) VR=CS, VM=1-n Lossy Image Compression Method  */
    public static final int LossyImageCompressionMethod = 0x00282114;

    /** (0028,3000) VR=SQ, VM=1 Modality LUT Sequence  */
    public static final int ModalityLUTSequence = 0x00283000;

    /** (0028,3002) VR=US|SS, VM=3 LUT Descriptor  */
    public static final int LUTDescriptor = 0x00283002;

    /** (0028,3003) VR=LO, VM=1 LUT Explanation  */
    public static final int LUTExplanation = 0x00283003;

    /** (0028,3004) VR=LO, VM=1 Modality LUT Type  */
    public static final int ModalityLUTType = 0x00283004;

    /** (0028,3006) VR=US|SS|OW, VM=1-n1 LUT Data  */
    public static final int LUTData = 0x00283006;

    /** (0028,3010) VR=SQ, VM=1 VOI LUT Sequence  */
    public static final int VOILUTSequence = 0x00283010;

    /** (0028,3110) VR=SQ, VM=1 Softcopy VOI LUT Sequence  */
    public static final int SoftcopyVOILUTSequence = 0x00283110;

    /** (0028,4000) VR=, VM= Comments RET */
    public static final int CommentsRET00284000 = 0x00284000;

    /** (0028,5000) VR=SQ, VM=1 Bi-Plane Acquisition Sequence  */
    public static final int BiPlaneAcquisitionSequence = 0x00285000;

    /** (0028,6010) VR=US, VM=1 Representative Frame Number  */
    public static final int RepresentativeFrameNumber = 0x00286010;

    /** (0028,6020) VR=US, VM=1-n Frame Numbers of Interest (FOI)  */
    public static final int FrameNumbersofInterestFOI = 0x00286020;

    /** (0028,6022) VR=LO, VM=1-n Frame(s) of Interest Description  */
    public static final int FramesofInterestDescription = 0x00286022;

    /** (0028,6023) VR=CS, VM=1-n Frame of Interest Type  */
    public static final int FrameofInterestType = 0x00286023;

    /** (0028,6030) VR=US, VM=1-n Mask Pointer(s) RET */
    public static final int MaskPointersRET = 0x00286030;

    /** (0028,6040) VR=US, VM=1-n R Wave Pointer  */
    public static final int RWavePointer = 0x00286040;

    /** (0028,6100) VR=SQ, VM=1 Mask Subtraction Sequence  */
    public static final int MaskSubtractionSequence = 0x00286100;

    /** (0028,6101) VR=CS, VM=1 Mask Operation  */
    public static final int MaskOperation = 0x00286101;

    /** (0028,6102) VR=US, VM=2-2n Applicable Frame Range  */
    public static final int ApplicableFrameRange = 0x00286102;

    /** (0028,6110) VR=US, VM=1-n Mask Frame Numbers  */
    public static final int MaskFrameNumbers = 0x00286110;

    /** (0028,6112) VR=US, VM=1 Contrast Frame Averaging  */
    public static final int ContrastFrameAveraging = 0x00286112;

    /** (0028,6114) VR=FL, VM=2 Mask Sub-pixel Shift  */
    public static final int MaskSubpixelShift = 0x00286114;

    /** (0028,6120) VR=SS, VM=1 TID Offset  */
    public static final int TIDOffset = 0x00286120;

    /** (0028,6190) VR=ST, VM=1 Mask Operation Explanation  */
    public static final int MaskOperationExplanation = 0x00286190;

    /** (0028,9001) VR=UL, VM=1 Data Point Rows  */
    public static final int DataPointRows = 0x00289001;

    /** (0028,9002) VR=UL, VM=1 Data Point Columns  */
    public static final int DataPointColumns = 0x00289002;

    /** (0028,9003) VR=CS, VM=1 Signal Domain Columns  */
    public static final int SignalDomainColumns = 0x00289003;

    /** (0028,9099) VR=US, VM=1 Largest Monochrome Pixel Value Retired */
    public static final int LargestMonochromePixelValueRetired = 0x00289099;

    /** (0028,9108) VR=CS, VM=1 Data Representation  */
    public static final int DataRepresentation = 0x00289108;

    /** (0028,9110) VR=SQ, VM=1 Pixel Measures Sequence  */
    public static final int PixelMeasuresSequence = 0x00289110;

    /** (0028,9132) VR=SQ, VM=1 Frame VOI LUT Sequence  */
    public static final int FrameVOILUTSequence = 0x00289132;

    /** (0028,9145) VR=SQ, VM=1 Pixel Value Transformation Sequence  */
    public static final int PixelValueTransformationSequence = 0x00289145;

    /** (0028,9235) VR=CS, VM=1 Signal Domain Rows  */
    public static final int SignalDomainRows = 0x00289235;

    /** (0032,000A) VR=CS, VM=1 Study Status ID  */
    public static final int StudyStatusID = 0x0032000A;

    /** (0032,000C) VR=CS, VM=1 Study Priority ID  */
    public static final int StudyPriorityID = 0x0032000C;

    /** (0032,0012) VR=LO, VM=1 Study ID Issuer  */
    public static final int StudyIDIssuer = 0x00320012;

    /** (0032,0032) VR=DA, VM=1 Study Verified Date  */
    public static final int StudyVerifiedDate = 0x00320032;

    /** (0032,0033) VR=TM, VM=1 Study Verified Time  */
    public static final int StudyVerifiedTime = 0x00320033;

    /** (0032,0034) VR=DA, VM=1 Study Read Date  */
    public static final int StudyReadDate = 0x00320034;

    /** (0032,0035) VR=TM, VM=1 Study Read Time  */
    public static final int StudyReadTime = 0x00320035;

    /** (0032,1000) VR=DA, VM=1 Scheduled Study Start Date  */
    public static final int ScheduledStudyStartDate = 0x00321000;

    /** (0032,1001) VR=TM, VM=1 Scheduled Study Start Time  */
    public static final int ScheduledStudyStartTime = 0x00321001;

    /** (0032,1010) VR=DA, VM=1 Scheduled Study Stop Date  */
    public static final int ScheduledStudyStopDate = 0x00321010;

    /** (0032,1011) VR=TM, VM=1 Scheduled Study Stop Time  */
    public static final int ScheduledStudyStopTime = 0x00321011;

    /** (0032,1020) VR=LO, VM=1 Scheduled Study Location  */
    public static final int ScheduledStudyLocation = 0x00321020;

    /** (0032,1021) VR=AE, VM=1-n Scheduled Study Location AE Title(s)  */
    public static final int ScheduledStudyLocationAETitles = 0x00321021;

    /** (0032,1030) VR=LO, VM=1 Reason for Study  */
    public static final int ReasonforStudy = 0x00321030;

    /** (0032,1031) VR=SQ, VM=1 Requesting Physician Identification Sequence  */
    public static final int RequestingPhysicianIdentificationSequence = 0x00321031;

    /** (0032,1032) VR=PN, VM=1 Requesting Physician  */
    public static final int RequestingPhysician = 0x00321032;

    /** (0032,1033) VR=LO, VM=1 Requesting Service  */
    public static final int RequestingService = 0x00321033;

    /** (0032,1040) VR=DA, VM=1 Study Arrival Date  */
    public static final int StudyArrivalDate = 0x00321040;

    /** (0032,1041) VR=TM, VM=1 Study Arrival Time  */
    public static final int StudyArrivalTime = 0x00321041;

    /** (0032,1050) VR=DA, VM=1 Study Completion Date  */
    public static final int StudyCompletionDate = 0x00321050;

    /** (0032,1051) VR=TM, VM=1 Study Completion Time  */
    public static final int StudyCompletionTime = 0x00321051;

    /** (0032,1055) VR=CS, VM=1 Study Component Status ID  */
    public static final int StudyComponentStatusID = 0x00321055;

    /** (0032,1060) VR=LO, VM=1 Requested Procedure Description  */
    public static final int RequestedProcedureDescription = 0x00321060;

    /** (0032,1064) VR=SQ, VM=1 Requested Procedure Code Sequence  */
    public static final int RequestedProcedureCodeSequence = 0x00321064;

    /** (0032,1070) VR=LO, VM=1 Requested Contrast Agent  */
    public static final int RequestedContrastAgent = 0x00321070;

    /** (0032,4000) VR=LT, VM=1 Study Comments  */
    public static final int StudyComments = 0x00324000;

    /** (0038,0004) VR=SQ, VM=1 Referenced Patient Alias Sequence  */
    public static final int ReferencedPatientAliasSequence = 0x00380004;

    /** (0038,0008) VR=CS, VM=1 Visit Status ID  */
    public static final int VisitStatusID = 0x00380008;

    /** (0038,0010) VR=LO, VM=1 Admission ID  */
    public static final int AdmissionID = 0x00380010;

    /** (0038,0011) VR=LO, VM=1 Issuer of Admission ID  */
    public static final int IssuerofAdmissionID = 0x00380011;

    /** (0038,0016) VR=LO, VM=1 Route of Admissions  */
    public static final int RouteofAdmissions = 0x00380016;

    /** (0038,001A) VR=DA, VM=1 Scheduled Admission Date  */
    public static final int ScheduledAdmissionDate = 0x0038001A;

    /** (0038,001B) VR=TM, VM=1 Scheduled Admission Time  */
    public static final int ScheduledAdmissionTime = 0x0038001B;

    /** (0038,001C) VR=DA, VM=1 Scheduled Discharge Date  */
    public static final int ScheduledDischargeDate = 0x0038001C;

    /** (0038,001D) VR=TM, VM=1 Scheduled Discharge Time  */
    public static final int ScheduledDischargeTime = 0x0038001D;

    /** (0038,001E) VR=LO, VM=1 Scheduled Patient Institution Residence  */
    public static final int ScheduledPatientInstitutionResidence = 0x0038001E;

    /** (0038,0020) VR=DA, VM=1 Admitting Date  */
    public static final int AdmittingDate = 0x00380020;

    /** (0038,0021) VR=TM, VM=1 Admitting Time  */
    public static final int AdmittingTime = 0x00380021;

    /** (0038,0030) VR=DA, VM=1 Discharge Date  */
    public static final int DischargeDate = 0x00380030;

    /** (0038,0032) VR=TM, VM=1 Discharge Time  */
    public static final int DischargeTime = 0x00380032;

    /** (0038,0040) VR=LO, VM=1 Discharge Diagnosis Description  */
    public static final int DischargeDiagnosisDescription = 0x00380040;

    /** (0038,0044) VR=SQ, VM=1 Discharge Diagnosis Code Sequence  */
    public static final int DischargeDiagnosisCodeSequence = 0x00380044;

    /** (0038,0050) VR=LO, VM=1 Special Needs  */
    public static final int SpecialNeeds = 0x00380050;

    /** (0038,0300) VR=LO, VM=1 Current Patient Location  */
    public static final int CurrentPatientLocation = 0x00380300;

    /** (0038,0400) VR=LO, VM=1 Patient's Institution Residence  */
    public static final int PatientsInstitutionResidence = 0x00380400;

    /** (0038,0500) VR=LO, VM=1 Patient State  */
    public static final int PatientState = 0x00380500;

    /** (0038,0502) VR=SQ, VM=1 Patient Clinical Trial Participation Sequence  */
    public static final int PatientClinicalTrialParticipationSequence = 0x00380502;

    /** (0038,4000) VR=LT, VM=1 Visit Comments  */
    public static final int VisitComments = 0x00384000;

    /** (003A,0004) VR=CS, VM=1 Waveform Originality  */
    public static final int WaveformOriginality = 0x003A0004;

    /** (003A,0005) VR=US, VM=1 Number of Waveform Channels  */
    public static final int NumberofWaveformChannels = 0x003A0005;

    /** (003A,0010) VR=UL, VM=1 Number of Waveform Samples  */
    public static final int NumberofWaveformSamples = 0x003A0010;

    /** (003A,001A) VR=DS, VM=1 Sampling Frequency  */
    public static final int SamplingFrequency = 0x003A001A;

    /** (003A,0020) VR=SH, VM=1 Multiplex Group Label  */
    public static final int MultiplexGroupLabel = 0x003A0020;

    /** (003A,0200) VR=SQ, VM=1 Channel Definition Sequence  */
    public static final int ChannelDefinitionSequence = 0x003A0200;

    /** (003A,0202) VR=IS, VM=1 Waveform Channel Number  */
    public static final int WaveformChannelNumber = 0x003A0202;

    /** (003A,0203) VR=SH, VM=1 Channel Label  */
    public static final int ChannelLabel = 0x003A0203;

    /** (003A,0205) VR=CS, VM=1-n Channel Status  */
    public static final int ChannelStatus = 0x003A0205;

    /** (003A,0208) VR=SQ, VM=1 Channel Source Sequence  */
    public static final int ChannelSourceSequence = 0x003A0208;

    /** (003A,0209) VR=SQ, VM=1 Channel Source Modifiers Sequence  */
    public static final int ChannelSourceModifiersSequence = 0x003A0209;

    /** (003A,020A) VR=SQ, VM=1 Source Waveform Sequence  */
    public static final int SourceWaveformSequence = 0x003A020A;

    /** (003A,020C) VR=LO, VM=1 Channel Derivation Description  */
    public static final int ChannelDerivationDescription = 0x003A020C;

    /** (003A,0210) VR=DS, VM=1 Channel Sensitivity  */
    public static final int ChannelSensitivity = 0x003A0210;

    /** (003A,0211) VR=SQ, VM=1 Channel Sensitivity Units Sequence  */
    public static final int ChannelSensitivityUnitsSequence = 0x003A0211;

    /** (003A,0212) VR=DS, VM=1 Channel Sensitivity Correction Factor  */
    public static final int ChannelSensitivityCorrectionFactor = 0x003A0212;

    /** (003A,0213) VR=DS, VM=1 Channel Baseline  */
    public static final int ChannelBaseline = 0x003A0213;

    /** (003A,0214) VR=DS, VM=1 Channel Time Skew  */
    public static final int ChannelTimeSkew = 0x003A0214;

    /** (003A,0215) VR=DS, VM=1 Channel Sample Skew  */
    public static final int ChannelSampleSkew = 0x003A0215;

    /** (003A,0218) VR=DS, VM=1 Channel Offset  */
    public static final int ChannelOffset = 0x003A0218;

    /** (003A,021A) VR=US, VM=1 Waveform Bits Stored  */
    public static final int WaveformBitsStored = 0x003A021A;

    /** (003A,0220) VR=DS, VM=1 Filter Low Frequency  */
    public static final int FilterLowFrequency = 0x003A0220;

    /** (003A,0221) VR=DS, VM=1 Filter High Frequency  */
    public static final int FilterHighFrequency = 0x003A0221;

    /** (003A,0222) VR=DS, VM=1 Notch Filter Frequency  */
    public static final int NotchFilterFrequency = 0x003A0222;

    /** (003A,0223) VR=DS, VM=1 Notch Filter Bandwidth  */
    public static final int NotchFilterBandwidth = 0x003A0223;

    /** (003A,0300) VR=SQ, VM=1 Multiplexed Audio Channels Description Code Sequence  */
    public static final int MultiplexedAudioChannelsDescriptionCodeSequence = 0x003A0300;

    /** (003A,0301) VR=IS, VM=1 Channel Identification Code  */
    public static final int ChannelIdentificationCode = 0x003A0301;

    /** (003A,0302) VR=CS, VM=1 Channel Mode  */
    public static final int ChannelMode = 0x003A0302;

    /** (0040,0001) VR=AE, VM=1-n Scheduled Station AE Title  */
    public static final int ScheduledStationAETitle = 0x00400001;

    /** (0040,0002) VR=DA, VM=1 Scheduled Procedure Step Start Date  */
    public static final int ScheduledProcedureStepStartDate = 0x00400002;

    /** (0040,0003) VR=TM, VM=1 Scheduled Procedure Step Start Time  */
    public static final int ScheduledProcedureStepStartTime = 0x00400003;

    /** (0040,0004) VR=DA, VM=1 Scheduled Procedure Step End Date  */
    public static final int ScheduledProcedureStepEndDate = 0x00400004;

    /** (0040,0005) VR=TM, VM=1 Scheduled Procedure Step End Time  */
    public static final int ScheduledProcedureStepEndTime = 0x00400005;

    /** (0040,0006) VR=PN, VM=1 Scheduled Performing Physician's Name  */
    public static final int ScheduledPerformingPhysiciansName = 0x00400006;

    /** (0040,0007) VR=LO, VM=1 Scheduled Procedure Step Description  */
    public static final int ScheduledProcedureStepDescription = 0x00400007;

    /** (0040,0008) VR=SQ, VM=1 Scheduled Protocol Code Sequence  */
    public static final int ScheduledProtocolCodeSequence = 0x00400008;

    /** (0040,0009) VR=SH, VM=1 Scheduled Procedure Step ID  */
    public static final int ScheduledProcedureStepID = 0x00400009;

    /** (0040,000A) VR=SQ, VM=1 Stage Code Sequence  */
    public static final int StageCodeSequence = 0x0040000A;

    /** (0040,000B) VR=SQ, VM=1 Scheduled Performing Physician Identification Sequence  */
    public static final int ScheduledPerformingPhysicianIdentificationSequence = 0x0040000B;

    /** (0040,0010) VR=SH, VM=1-n Scheduled Station Name  */
    public static final int ScheduledStationName = 0x00400010;

    /** (0040,0011) VR=SH, VM=1 Scheduled Procedure Step Location  */
    public static final int ScheduledProcedureStepLocation = 0x00400011;

    /** (0040,0012) VR=LO, VM=1 Pre-Medication  */
    public static final int PreMedication = 0x00400012;

    /** (0040,0020) VR=CS, VM=1 Scheduled Procedure Step Status  */
    public static final int ScheduledProcedureStepStatus = 0x00400020;

    /** (0040,0100) VR=SQ, VM=1 Scheduled Procedure Step Sequence  */
    public static final int ScheduledProcedureStepSequence = 0x00400100;

    /** (0040,0220) VR=SQ, VM=1 Referenced Non-Image Composite SOP Instance Sequence  */
    public static final int ReferencedNonImageCompositeSOPInstanceSequence = 0x00400220;

    /** (0040,0241) VR=AE, VM=1 Performed Station AE Title  */
    public static final int PerformedStationAETitle = 0x00400241;

    /** (0040,0242) VR=SH, VM=1 Performed Station Name  */
    public static final int PerformedStationName = 0x00400242;

    /** (0040,0243) VR=SH, VM=1 Performed Location  */
    public static final int PerformedLocation = 0x00400243;

    /** (0040,0244) VR=DA, VM=1 Performed Procedure Step Start Date  */
    public static final int PerformedProcedureStepStartDate = 0x00400244;

    /** (0040,0245) VR=TM, VM=1 Performed Procedure Step Start Time  */
    public static final int PerformedProcedureStepStartTime = 0x00400245;

    /** (0040,0250) VR=DA, VM=1 Performed Procedure Step End Date  */
    public static final int PerformedProcedureStepEndDate = 0x00400250;

    /** (0040,0251) VR=TM, VM=1 Performed Procedure Step End Time  */
    public static final int PerformedProcedureStepEndTime = 0x00400251;

    /** (0040,0252) VR=CS, VM=1 Performed Procedure Step Status  */
    public static final int PerformedProcedureStepStatus = 0x00400252;

    /** (0040,0253) VR=SH, VM=1 Performed Procedure Step ID  */
    public static final int PerformedProcedureStepID = 0x00400253;

    /** (0040,0254) VR=LO, VM=1 Performed Procedure Step Description  */
    public static final int PerformedProcedureStepDescription = 0x00400254;

    /** (0040,0255) VR=LO, VM=1 Performed Procedure Type Description  */
    public static final int PerformedProcedureTypeDescription = 0x00400255;

    /** (0040,0260) VR=SQ, VM=1 Performed Protocol Code Sequence  */
    public static final int PerformedProtocolCodeSequence = 0x00400260;

    /** (0040,0270) VR=SQ, VM=1 Scheduled Step Attributes Sequence  */
    public static final int ScheduledStepAttributesSequence = 0x00400270;

    /** (0040,0275) VR=SQ, VM=1 Request Attributes Sequence  */
    public static final int RequestAttributesSequence = 0x00400275;

    /** (0040,0280) VR=ST, VM=1 Comments on the Performed Procedure Step  */
    public static final int CommentsonthePerformedProcedureStep = 0x00400280;

    /** (0040,0281) VR=SQ, VM=1 Performed Procedure Step Discontinuation Reason Code Sequence  */
    public static final int PerformedProcedureStepDiscontinuationReasonCodeSequence = 0x00400281;

    /** (0040,0293) VR=SQ, VM=1 Quantity Sequence  */
    public static final int QuantitySequence = 0x00400293;

    /** (0040,0294) VR=DS, VM=1 Quantity  */
    public static final int Quantity = 0x00400294;

    /** (0040,0295) VR=SQ, VM=1 Measuring Units Sequence  */
    public static final int MeasuringUnitsSequence = 0x00400295;

    /** (0040,0296) VR=SQ, VM=1 Billing Item Sequence  */
    public static final int BillingItemSequence = 0x00400296;

    /** (0040,0300) VR=US, VM=1 Total Time of Fluoroscopy  */
    public static final int TotalTimeofFluoroscopy = 0x00400300;

    /** (0040,0301) VR=US, VM=1 Total Number of Exposures  */
    public static final int TotalNumberofExposures = 0x00400301;

    /** (0040,0302) VR=US, VM=1 Entrance Dose  */
    public static final int EntranceDose = 0x00400302;

    /** (0040,0303) VR=US, VM=1-2 Exposed Area  */
    public static final int ExposedArea = 0x00400303;

    /** (0040,0306) VR=DS, VM=1 Distance Source to Entrance  */
    public static final int DistanceSourcetoEntrance = 0x00400306;

    /** (0040,0307) VR=DS, VM=1 Distance Source to Support RET */
    public static final int DistanceSourcetoSupportRET = 0x00400307;

    /** (0040,030E) VR=SQ, VM=1 Exposure Dose Sequence  */
    public static final int ExposureDoseSequence = 0x0040030E;

    /** (0040,0310) VR=ST, VM=1 Comments on Radiation Dose  */
    public static final int CommentsonRadiationDose = 0x00400310;

    /** (0040,0312) VR=DS, VM=1 X-Ray Output  */
    public static final int XRayOutput = 0x00400312;

    /** (0040,0314) VR=DS, VM=1 Half Value Layer  */
    public static final int HalfValueLayer = 0x00400314;

    /** (0040,0316) VR=DS, VM=1 Organ Dose  */
    public static final int OrganDose = 0x00400316;

    /** (0040,0318) VR=CS, VM=1 Organ Exposed  */
    public static final int OrganExposed = 0x00400318;

    /** (0040,0320) VR=SQ, VM=1 Billing Procedure Step Sequence  */
    public static final int BillingProcedureStepSequence = 0x00400320;

    /** (0040,0321) VR=SQ, VM=1 Film Consumption Sequence  */
    public static final int FilmConsumptionSequence = 0x00400321;

    /** (0040,0324) VR=SQ, VM=1 Billing Supplies and Devices Sequence  */
    public static final int BillingSuppliesandDevicesSequence = 0x00400324;

    /** (0040,0330) VR=SQ, VM=1 Referenced Procedure Step Sequence RET */
    public static final int ReferencedProcedureStepSequenceRET = 0x00400330;

    /** (0040,0340) VR=SQ, VM=1 Performed Series Sequence  */
    public static final int PerformedSeriesSequence = 0x00400340;

    /** (0040,0400) VR=LT, VM=1 Comments on the Scheduled Procedure Step  */
    public static final int CommentsontheScheduledProcedureStep = 0x00400400;

    /** (0040,0440) VR=SQ, VM=1 Protocol Context Sequence  */
    public static final int ProtocolContextSequence = 0x00400440;

    /** (0040,0441) VR=SQ, VM=1 Content Item Modifier Sequence  */
    public static final int ContentItemModifierSequence = 0x00400441;

    /** (0040,050A) VR=LO, VM=1 Specimen Accession Number  */
    public static final int SpecimenAccessionNumber = 0x0040050A;

    /** (0040,0550) VR=SQ, VM=1 Specimen Sequence  */
    public static final int SpecimenSequence = 0x00400550;

    /** (0040,0551) VR=LO, VM=1 Specimen Identifier  */
    public static final int SpecimenIdentifier = 0x00400551;

    /** (0040,0555) VR=SQ, VM=1 Acquisition Context Sequence  */
    public static final int AcquisitionContextSequence = 0x00400555;

    /** (0040,0556) VR=ST, VM=1 Acquisition Context Description  */
    public static final int AcquisitionContextDescription = 0x00400556;

    /** (0040,059A) VR=SQ, VM=1 Specimen Type Code Sequence  */
    public static final int SpecimenTypeCodeSequence = 0x0040059A;

    /** (0040,06FA) VR=LO, VM=1 Slide Identifier  */
    public static final int SlideIdentifier = 0x004006FA;

    /** (0040,071A) VR=SQ, VM=1 Image Center Point Coordinates Sequence  */
    public static final int ImageCenterPointCoordinatesSequence = 0x0040071A;

    /** (0040,072A) VR=DS, VM=1 X offset in Slide Coordinate System  */
    public static final int XoffsetinSlideCoordinateSystem = 0x0040072A;

    /** (0040,073A) VR=DS, VM=1 Y offset in Slide Coordinate System  */
    public static final int YoffsetinSlideCoordinateSystem = 0x0040073A;

    /** (0040,074A) VR=DS, VM=1 Z offset in Slide Coordinate System  */
    public static final int ZoffsetinSlideCoordinateSystem = 0x0040074A;

    /** (0040,08D8) VR=SQ, VM=1 Pixel Spacing Sequence  */
    public static final int PixelSpacingSequence = 0x004008D8;

    /** (0040,08DA) VR=SQ, VM=1 Coordinate System Axis Code Sequence  */
    public static final int CoordinateSystemAxisCodeSequence = 0x004008DA;

    /** (0040,08EA) VR=SQ, VM=1 Measurement Units Code Sequence  */
    public static final int MeasurementUnitsCodeSequence = 0x004008EA;

    /** (0040,1001) VR=SH, VM=1 Requested Procedure ID  */
    public static final int RequestedProcedureID = 0x00401001;

    /** (0040,1002) VR=LO, VM=1 Reason for the Requested Procedure  */
    public static final int ReasonfortheRequestedProcedure = 0x00401002;

    /** (0040,1003) VR=SH, VM=1 Requested Procedure Priority  */
    public static final int RequestedProcedurePriority = 0x00401003;

    /** (0040,1004) VR=LO, VM=1 Patient Transport Arrangements  */
    public static final int PatientTransportArrangements = 0x00401004;

    /** (0040,1005) VR=LO, VM=1 Requested Procedure Location  */
    public static final int RequestedProcedureLocation = 0x00401005;

    /** (0040,1006) VR=SH, VM=1 Placer Order Number / Procedure RET */
    public static final int PlacerOrderNumberProcedureRET = 0x00401006;

    /** (0040,1007) VR=SH, VM=1 Filler Order Number / Procedure RET */
    public static final int FillerOrderNumberProcedureRET = 0x00401007;

    /** (0040,1008) VR=LO, VM=1 Confidentiality Code  */
    public static final int ConfidentialityCode = 0x00401008;

    /** (0040,1009) VR=SH, VM=1 Reporting Priority  */
    public static final int ReportingPriority = 0x00401009;

    /** (0040,100A) VR=SQ, VM=1 Reason for Requested Procedure Code Sequence  */
    public static final int ReasonforRequestedProcedureCodeSequence = 0x0040100A;

    /** (0040,1010) VR=PN, VM=1-n Names of Intended Recipients of Results  */
    public static final int NamesofIntendedRecipientsofResults = 0x00401010;

    /** (0040,1011) VR=SQ, VM=1 Intended Recipients of Results Identification Sequence  */
    public static final int IntendedRecipientsofResultsIdentificationSequence = 0x00401011;

    /** (0040,1101) VR=SQ, VM=1 Person Identification Code Sequence  */
    public static final int PersonIdentificationCodeSequence = 0x00401101;

    /** (0040,1102) VR=ST, VM=1 Person's Address  */
    public static final int PersonsAddress = 0x00401102;

    /** (0040,1103) VR=LO, VM=1-n Person's Telephone Numbers  */
    public static final int PersonsTelephoneNumbers = 0x00401103;

    /** (0040,1400) VR=LT, VM=1 Requested Procedure Comments  */
    public static final int RequestedProcedureComments = 0x00401400;

    /** (0040,2001) VR=LO, VM=1 Reason for the Imaging Service Request RET */
    public static final int ReasonfortheImagingServiceRequestRET = 0x00402001;

    /** (0040,2004) VR=DA, VM=1 Issue Date of Imaging Service Request  */
    public static final int IssueDateofImagingServiceRequest = 0x00402004;

    /** (0040,2005) VR=TM, VM=1 Issue Time of Imaging Service Request  */
    public static final int IssueTimeofImagingServiceRequest = 0x00402005;

    /** (0040,2006) VR=SH, VM=1 Placer Order Number / Imaging Service Request RET */
    public static final int PlacerOrderNumberImagingServiceRequestRET = 0x00402006;

    /** (0040,2007) VR=SH, VM=1 Filler Order Number / Imaging Service Request RET */
    public static final int FillerOrderNumberImagingServiceRequestRET = 0x00402007;

    /** (0040,2008) VR=PN, VM=1 Order Entered By  */
    public static final int OrderEnteredBy = 0x00402008;

    /** (0040,2009) VR=SH, VM=1 Order Enterer's Location  */
    public static final int OrderEnterersLocation = 0x00402009;

    /** (0040,2010) VR=SH, VM=1 Order Callback Phone Number  */
    public static final int OrderCallbackPhoneNumber = 0x00402010;

    /** (0040,2016) VR=LO, VM=1 Placer Order Number / Imaging Service Request  */
    public static final int PlacerOrderNumberImagingServiceRequest = 0x00402016;

    /** (0040,2017) VR=LO, VM=1 Filler Order Number / Imaging Service Request  */
    public static final int FillerOrderNumberImagingServiceRequest = 0x00402017;

    /** (0040,2400) VR=LT, VM=1 Imaging Service Request Comments  */
    public static final int ImagingServiceRequestComments = 0x00402400;

    /** (0040,3001) VR=LO, VM=1 Confidentiality Constraint on Patient Data Description  */
    public static final int ConfidentialityConstraintonPatientDataDescription = 0x00403001;

    /** (0040,4001) VR=CS, VM=1 General Purpose Scheduled Procedure Step Status  */
    public static final int GeneralPurposeScheduledProcedureStepStatus = 0x00404001;

    /** (0040,4002) VR=CS, VM=1 General Purpose Performed Procedure Step Status  */
    public static final int GeneralPurposePerformedProcedureStepStatus = 0x00404002;

    /** (0040,4003) VR=CS, VM=1 General Purpose Scheduled Procedure Step Priority  */
    public static final int GeneralPurposeScheduledProcedureStepPriority = 0x00404003;

    /** (0040,4004) VR=SQ, VM=1 Scheduled Processing Applications Code Sequence  */
    public static final int ScheduledProcessingApplicationsCodeSequence = 0x00404004;

    /** (0040,4005) VR=DT, VM=1 Scheduled Procedure Step Start Date and Time  */
    public static final int ScheduledProcedureStepStartDateandTime = 0x00404005;

    /** (0040,4006) VR=CS, VM=1 Multiple Copies Flag  */
    public static final int MultipleCopiesFlag = 0x00404006;

    /** (0040,4007) VR=SQ, VM=1 Performed Processing Applications Code Sequence  */
    public static final int PerformedProcessingApplicationsCodeSequence = 0x00404007;

    /** (0040,4009) VR=SQ, VM=1 Human Performer Code Sequence  */
    public static final int HumanPerformerCodeSequence = 0x00404009;

    /** (0040,4010) VR=DT, VM=1 Scheduled Procedure Step Modification Date and Time  */
    public static final int ScheduledProcedureStepModificationDateandTime = 0x00404010;

    /** (0040,4011) VR=DT, VM=1 Expected Completion Date and Time  */
    public static final int ExpectedCompletionDateandTime = 0x00404011;

    /** (0040,4015) VR=SQ, VM=1 Resulting General Purpose Performed Procedure Steps Sequence  */
    public static final int ResultingGeneralPurposePerformedProcedureStepsSequence = 0x00404015;

    /** (0040,4016) VR=SQ, VM=1 Referenced General Purpose Scheduled Procedure Step Sequence  */
    public static final int ReferencedGeneralPurposeScheduledProcedureStepSequence = 0x00404016;

    /** (0040,4018) VR=SQ, VM=1 Scheduled Workitem Code Sequence  */
    public static final int ScheduledWorkitemCodeSequence = 0x00404018;

    /** (0040,4019) VR=SQ, VM=1 Performed Workitem Code Sequence  */
    public static final int PerformedWorkitemCodeSequence = 0x00404019;

    /** (0040,4020) VR=CS, VM=1 Input Availability Flag  */
    public static final int InputAvailabilityFlag = 0x00404020;

    /** (0040,4021) VR=SQ, VM=1 Input InformationSequence  */
    public static final int InputInformationSequence = 0x00404021;

    /** (0040,4022) VR=SQ, VM=1 Relevant Information Sequence  */
    public static final int RelevantInformationSequence = 0x00404022;

    /** (0040,4023) VR=UI, VM=1 Referenced General Purpose Scheduled Procedure Step Transaction UID  */
    public static final int ReferencedGeneralPurposeScheduledProcedureStepTransactionUID = 0x00404023;

    /** (0040,4025) VR=SQ, VM=1 Scheduled Station Name Code Sequence  */
    public static final int ScheduledStationNameCodeSequence = 0x00404025;

    /** (0040,4026) VR=SQ, VM=1 Scheduled Station Class Code Sequence  */
    public static final int ScheduledStationClassCodeSequence = 0x00404026;

    /** (0040,4027) VR=SQ, VM=1 Scheduled Station Geographic Location Code Sequence  */
    public static final int ScheduledStationGeographicLocationCodeSequence = 0x00404027;

    /** (0040,4028) VR=SQ, VM=1 Performed Station Name Code Sequence  */
    public static final int PerformedStationNameCodeSequence = 0x00404028;

    /** (0040,4029) VR=SQ, VM=1 Performed Station Class Code Sequence  */
    public static final int PerformedStationClassCodeSequence = 0x00404029;

    /** (0040,4030) VR=SQ, VM=1 Performed Station Geographic Location Code Sequence  */
    public static final int PerformedStationGeographicLocationCodeSequence = 0x00404030;

    /** (0040,4031) VR=SQ, VM=1 Requested Subsequent Workitem Code Sequence  */
    public static final int RequestedSubsequentWorkitemCodeSequence = 0x00404031;

    /** (0040,4032) VR=SQ, VM=1 Non-DICOM Output Code Sequence  */
    public static final int NonDICOMOutputCodeSequence = 0x00404032;

    /** (0040,4033) VR=SQ, VM=1 Output Information Sequence  */
    public static final int OutputInformationSequence = 0x00404033;

    /** (0040,4034) VR=SQ, VM=1 Scheduled Human Performers Sequence  */
    public static final int ScheduledHumanPerformersSequence = 0x00404034;

    /** (0040,4035) VR=SQ, VM=1 Actual Human Performers Sequence  */
    public static final int ActualHumanPerformersSequence = 0x00404035;

    /** (0040,4036) VR=LO, VM=1 Human Performer's Organization  */
    public static final int HumanPerformersOrganization = 0x00404036;

    /** (0040,4037) VR=PN, VM=1 Human Performer's Name  */
    public static final int HumanPerformersName = 0x00404037;

    /** (0040,8302) VR=DS, VM=1 Entrance Dose in mGy  */
    public static final int EntranceDoseinmGy = 0x00408302;

    /** (0040,9096) VR=SQ, VM=1 Real World Value Mapping Sequence  */
    public static final int RealWorldValueMappingSequence = 0x00409096;

    /** (0040,9098) VR=SQ, VM=1 Pixel Value Mapping Code Sequence  */
    public static final int PixelValueMappingCodeSequence = 0x00409098;

    /** (0040,9210) VR=SH, VM=1 LUT Label  */
    public static final int LUTLabel = 0x00409210;

    /** (0040,9211) VR=US/SS, VM=1 Real World Value Last Value Mapped  */
    public static final int RealWorldValueLastValueMapped = 0x00409211;

    /** (0040,9212) VR=FD, VM=1-n Real World Value LUT Data  */
    public static final int RealWorldValueLUTData = 0x00409212;

    /** (0040,9216) VR=US/SS, VM=1 Real World Value First Value Mapped  */
    public static final int RealWorldValueFirstValueMapped = 0x00409216;

    /** (0040,9224) VR=FD, VM=1 Real World Value Intercept  */
    public static final int RealWorldValueIntercept = 0x00409224;

    /** (0040,9225) VR=FD, VM=1 Real World Value Slope  */
    public static final int RealWorldValueSlope = 0x00409225;

    /** (0040,A010) VR=CS, VM=1 Relationship Type  */
    public static final int RelationshipType = 0x0040A010;

    /** (0040,A027) VR=LO, VM=1 Verifying Organization  */
    public static final int VerifyingOrganization = 0x0040A027;

    /** (0040,A030) VR=DT, VM=1 Verification DateTime  */
    public static final int VerificationDateTime = 0x0040A030;

    /** (0040,A032) VR=DT, VM=1 Observation DateTime  */
    public static final int ObservationDateTime = 0x0040A032;

    /** (0040,A040) VR=CS, VM=1 Value Type  */
    public static final int ValueType = 0x0040A040;

    /** (0040,A043) VR=SQ, VM=1 Concept Name Code Sequence  */
    public static final int ConceptNameCodeSequence = 0x0040A043;

    /** (0040,A050) VR=CS, VM=1 Continuity Of Content  */
    public static final int ContinuityOfContent = 0x0040A050;

    /** (0040,A073) VR=SQ, VM=1 Verifying Observer Sequence  */
    public static final int VerifyingObserverSequence = 0x0040A073;

    /** (0040,A075) VR=PN, VM=1 Verifying Observer Name  */
    public static final int VerifyingObserverName = 0x0040A075;

    /** (0040,A088) VR=SQ, VM=1 Verifying Observer Identification Code Sequence  */
    public static final int VerifyingObserverIdentificationCodeSequence = 0x0040A088;

    /** (0040,A0B0) VR=US, VM=2-2n Referenced Waveform Channels  */
    public static final int ReferencedWaveformChannels = 0x0040A0B0;

    /** (0040,A120) VR=DT, VM=1 DateTime  */
    public static final int DateTime = 0x0040A120;

    /** (0040,A121) VR=DA, VM=1 Date  */
    public static final int Date = 0x0040A121;

    /** (0040,A122) VR=TM, VM=1 Time  */
    public static final int Time = 0x0040A122;

    /** (0040,A123) VR=PN, VM=1 Person Name  */
    public static final int PersonName = 0x0040A123;

    /** (0040,A124) VR=UI, VM=1 UID  */
    public static final int UID = 0x0040A124;

    /** (0040,A130) VR=CS, VM=1 Temporal Range Type  */
    public static final int TemporalRangeType = 0x0040A130;

    /** (0040,A132) VR=UL, VM=1-n Referenced Sample Positions  */
    public static final int ReferencedSamplePositions = 0x0040A132;

    /** (0040,A136) VR=US, VM=1-n Referenced Frame Numbers  */
    public static final int ReferencedFrameNumbers = 0x0040A136;

    /** (0040,A138) VR=DS, VM=1-n Referenced Time Offsets  */
    public static final int ReferencedTimeOffsets = 0x0040A138;

    /** (0040,A13A) VR=DT, VM=1-n Referenced Datetime  */
    public static final int ReferencedDatetime = 0x0040A13A;

    /** (0040,A160) VR=UT, VM=1 Text Value  */
    public static final int TextValue = 0x0040A160;

    /** (0040,A168) VR=SQ, VM=1 Concept Code Sequence  */
    public static final int ConceptCodeSequence = 0x0040A168;

    /** (0040,A170) VR=SQ, VM=1 Purpose of Reference Code Sequence  */
    public static final int PurposeofReferenceCodeSequence = 0x0040A170;

    /** (0040,A180) VR=US, VM=1 Annotation Group Number  */
    public static final int AnnotationGroupNumber = 0x0040A180;

    /** (0040,A195) VR=SQ, VM=1 Modifier Code Sequence  */
    public static final int ModifierCodeSequence = 0x0040A195;

    /** (0040,A300) VR=SQ, VM=1 Measured Value Sequence  */
    public static final int MeasuredValueSequence = 0x0040A300;

    /** (0040,A301) VR=SQ, VM=1 Numeric Value Qualifier Code Sequence  */
    public static final int NumericValueQualifierCodeSequence = 0x0040A301;

    /** (0040,A30A) VR=DS, VM=1-n Numeric Value  */
    public static final int NumericValue = 0x0040A30A;

    /** (0040,A360) VR=SQ, VM=1 Predecessor Documents Sequence  */
    public static final int PredecessorDocumentsSequence = 0x0040A360;

    /** (0040,A370) VR=SQ, VM=1 Referenced Request Sequence  */
    public static final int ReferencedRequestSequence = 0x0040A370;

    /** (0040,A372) VR=SQ, VM=1 Performed Procedure Code Sequence  */
    public static final int PerformedProcedureCodeSequence = 0x0040A372;

    /** (0040,A375) VR=SQ, VM=1 Current Requested Procedure Evidence Sequence  */
    public static final int CurrentRequestedProcedureEvidenceSequence = 0x0040A375;

    /** (0040,A385) VR=SQ, VM=1 Pertinent Other Evidence Sequence  */
    public static final int PertinentOtherEvidenceSequence = 0x0040A385;

    /** (0040,A491) VR=CS, VM=1 Completion Flag  */
    public static final int CompletionFlag = 0x0040A491;

    /** (0040,A492) VR=LO, VM=1 Completion Flag Description  */
    public static final int CompletionFlagDescription = 0x0040A492;

    /** (0040,A493) VR=CS, VM=1 Verification Flag  */
    public static final int VerificationFlag = 0x0040A493;

    /** (0040,A504) VR=SQ, VM=1 Content Template Sequence  */
    public static final int ContentTemplateSequence = 0x0040A504;

    /** (0040,A525) VR=SQ, VM=1 Identical Documents Sequence  */
    public static final int IdenticalDocumentsSequence = 0x0040A525;

    /** (0040,A730) VR=SQ, VM=1 Content Sequence  */
    public static final int ContentSequence = 0x0040A730;

    /** (0040,B020) VR=SQ, VM=1 Annotation Sequence  */
    public static final int AnnotationSequence = 0x0040B020;

    /** (0040,DB00) VR=CS, VM=1 Template Identifier  */
    public static final int TemplateIdentifier = 0x0040DB00;

    /** (0040,DB06) VR=DT, VM=1 Template Version RET */
    public static final int TemplateVersionRET = 0x0040DB06;

    /** (0040,DB07) VR=DT, VM=1 Template Local Version RET */
    public static final int TemplateLocalVersionRET = 0x0040DB07;

    /** (0040,DB0B) VR=CS, VM=1 Template Extension Flag RET */
    public static final int TemplateExtensionFlagRET = 0x0040DB0B;

    /** (0040,DB0C) VR=UI, VM=1 Template Extension Organization UID RET */
    public static final int TemplateExtensionOrganizationUIDRET = 0x0040DB0C;

    /** (0040,DB0D) VR=UI, VM=1 Template Extension Creator UID RET */
    public static final int TemplateExtensionCreatorUIDRET = 0x0040DB0D;

    /** (0040,DB73) VR=UL, VM=1-n Referenced Content Item Identifier  */
    public static final int ReferencedContentItemIdentifier = 0x0040DB73;

    /** (0042,0010) VR=ST, VM=1 Document Title  */
    public static final int DocumentTitle = 0x00420010;

    /** (0042,0011) VR=OB, VM=1 Encapsulated Document  */
    public static final int EncapsulatedDocument = 0x00420011;

    /** (0042,0012) VR=LO, VM=1 MIME Type of Encapsulated Document  */
    public static final int MIMETypeofEncapsulatedDocument = 0x00420012;

    /** (0042,0013) VR=SQ, VM=1 Source Instance Sequence  */
    public static final int SourceInstanceSequence = 0x00420013;

    /** (0050,0004) VR=CS, VM=1 Calibration Image  */
    public static final int CalibrationImage = 0x00500004;

    /** (0050,0010) VR=SQ, VM=1 Device Sequence  */
    public static final int DeviceSequence = 0x00500010;

    /** (0050,0014) VR=DS, VM=1 Device Length  */
    public static final int DeviceLength = 0x00500014;

    /** (0050,0016) VR=DS, VM=1 Device Diameter  */
    public static final int DeviceDiameter = 0x00500016;

    /** (0050,0017) VR=CS, VM=1 Device Diameter Units  */
    public static final int DeviceDiameterUnits = 0x00500017;

    /** (0050,0018) VR=DS, VM=1 Device Volume  */
    public static final int DeviceVolume = 0x00500018;

    /** (0050,0019) VR=DS, VM=1 Inter-marker Distance  */
    public static final int IntermarkerDistance = 0x00500019;

    /** (0050,0020) VR=LO, VM=1 Device Description  */
    public static final int DeviceDescription = 0x00500020;

    /** (0054,0010) VR=US, VM=1-n Energy Window Vector  */
    public static final int EnergyWindowVector = 0x00540010;

    /** (0054,0011) VR=US, VM=1 Number of Energy Windows  */
    public static final int NumberofEnergyWindows = 0x00540011;

    /** (0054,0012) VR=SQ, VM=1 Energy Window Information Sequence  */
    public static final int EnergyWindowInformationSequence = 0x00540012;

    /** (0054,0013) VR=SQ, VM=1 Energy Window Range Sequence  */
    public static final int EnergyWindowRangeSequence = 0x00540013;

    /** (0054,0014) VR=DS, VM=1 Energy Window Lower Limit  */
    public static final int EnergyWindowLowerLimit = 0x00540014;

    /** (0054,0015) VR=DS, VM=1 Energy Window Upper Limit  */
    public static final int EnergyWindowUpperLimit = 0x00540015;

    /** (0054,0016) VR=SQ, VM=1 Radiopharmaceutical Information Sequence  */
    public static final int RadiopharmaceuticalInformationSequence = 0x00540016;

    /** (0054,0017) VR=IS, VM=1 Residual Syringe Counts  */
    public static final int ResidualSyringeCounts = 0x00540017;

    /** (0054,0018) VR=SH, VM=1 Energy Window Name  */
    public static final int EnergyWindowName = 0x00540018;

    /** (0054,0020) VR=US, VM=1-n Detector Vector  */
    public static final int DetectorVector = 0x00540020;

    /** (0054,0021) VR=US, VM=1 Number of Detectors  */
    public static final int NumberofDetectors = 0x00540021;

    /** (0054,0022) VR=SQ, VM=1 Detector Information Sequence  */
    public static final int DetectorInformationSequence = 0x00540022;

    /** (0054,0030) VR=US, VM=1-n Phase Vector  */
    public static final int PhaseVector = 0x00540030;

    /** (0054,0031) VR=US, VM=1 Number of Phases  */
    public static final int NumberofPhases = 0x00540031;

    /** (0054,0032) VR=SQ, VM=1 Phase Information Sequence  */
    public static final int PhaseInformationSequence = 0x00540032;

    /** (0054,0033) VR=US, VM=1 Number of Frames in Phase  */
    public static final int NumberofFramesinPhase = 0x00540033;

    /** (0054,0036) VR=IS, VM=1 Phase Delay  */
    public static final int PhaseDelay = 0x00540036;

    /** (0054,0038) VR=IS, VM=1 Pause Between Frames  */
    public static final int PauseBetweenFrames = 0x00540038;

    /** (0054,0039) VR=CS, VM=1 Phase Description  */
    public static final int PhaseDescription = 0x00540039;

    /** (0054,0050) VR=US, VM=1-n Rotation Vector  */
    public static final int RotationVector = 0x00540050;

    /** (0054,0051) VR=US, VM=1 Number of Rotations  */
    public static final int NumberofRotations = 0x00540051;

    /** (0054,0052) VR=SQ, VM=1 Rotation Information Sequence  */
    public static final int RotationInformationSequence = 0x00540052;

    /** (0054,0053) VR=US, VM=1 Number of Frames in Rotation  */
    public static final int NumberofFramesinRotation = 0x00540053;

    /** (0054,0060) VR=US, VM=1-n R-R Interval Vector  */
    public static final int RRIntervalVector = 0x00540060;

    /** (0054,0061) VR=US, VM=1 Number of R-R Intervals  */
    public static final int NumberofRRIntervals = 0x00540061;

    /** (0054,0062) VR=SQ, VM=1 Gated Information Sequence  */
    public static final int GatedInformationSequence = 0x00540062;

    /** (0054,0063) VR=SQ, VM=1 Data Information Sequence  */
    public static final int DataInformationSequence = 0x00540063;

    /** (0054,0070) VR=US, VM=1-n Time Slot Vector  */
    public static final int TimeSlotVector = 0x00540070;

    /** (0054,0071) VR=US, VM=1 Number of Time Slots  */
    public static final int NumberofTimeSlots = 0x00540071;

    /** (0054,0072) VR=SQ, VM=1 Time Slot Information Sequence  */
    public static final int TimeSlotInformationSequence = 0x00540072;

    /** (0054,0073) VR=DS, VM=1 Time Slot Time  */
    public static final int TimeSlotTime = 0x00540073;

    /** (0054,0080) VR=US, VM=1-n Slice Vector  */
    public static final int SliceVector = 0x00540080;

    /** (0054,0081) VR=US, VM=1 Number of Slices  */
    public static final int NumberofSlices = 0x00540081;

    /** (0054,0090) VR=US, VM=1-n Angular View Vector  */
    public static final int AngularViewVector = 0x00540090;

    /** (0054,0100) VR=US, VM=1-n Time Slice Vector  */
    public static final int TimeSliceVector = 0x00540100;

    /** (0054,0101) VR=US, VM=1 Number of Time Slices  */
    public static final int NumberofTimeSlices = 0x00540101;

    /** (0054,0200) VR=DS, VM=1 Start Angle  */
    public static final int StartAngle = 0x00540200;

    /** (0054,0202) VR=CS, VM=1 Type of Detector Motion  */
    public static final int TypeofDetectorMotion = 0x00540202;

    /** (0054,0210) VR=IS, VM=1-n Trigger Vector  */
    public static final int TriggerVector = 0x00540210;

    /** (0054,0211) VR=US, VM=1 Number of Triggers in Phase  */
    public static final int NumberofTriggersinPhase = 0x00540211;

    /** (0054,0220) VR=SQ, VM=1 View Code Sequence  */
    public static final int ViewCodeSequence = 0x00540220;

    /** (0054,0222) VR=SQ, VM=1 View Modifier Code Sequence  */
    public static final int ViewModifierCodeSequence = 0x00540222;

    /** (0054,0300) VR=SQ, VM=1 Radionuclide Code Sequence  */
    public static final int RadionuclideCodeSequence = 0x00540300;

    /** (0054,0302) VR=SQ, VM=1 Administration Route Code Sequence  */
    public static final int AdministrationRouteCodeSequence = 0x00540302;

    /** (0054,0304) VR=SQ, VM=1 Radiopharmaceutical Code Sequence  */
    public static final int RadiopharmaceuticalCodeSequence = 0x00540304;

    /** (0054,0306) VR=SQ, VM=1 Calibration Data Sequence  */
    public static final int CalibrationDataSequence = 0x00540306;

    /** (0054,0308) VR=US, VM=1 Energy Window Number  */
    public static final int EnergyWindowNumber = 0x00540308;

    /** (0054,0400) VR=SH, VM=1 Image ID  */
    public static final int ImageID = 0x00540400;

    /** (0054,0410) VR=SQ, VM=1 Patient Orientation Code Sequence  */
    public static final int PatientOrientationCodeSequence = 0x00540410;

    /** (0054,0412) VR=SQ, VM=1 Patient Orientation Modifier Code Sequence  */
    public static final int PatientOrientationModifierCodeSequence = 0x00540412;

    /** (0054,0414) VR=SQ, VM=1 Patient Gantry Relationship Code Sequence  */
    public static final int PatientGantryRelationshipCodeSequence = 0x00540414;

    /** (0054,0500) VR=CS, VM=1 Slice Progression Direction  */
    public static final int SliceProgressionDirection = 0x00540500;

    /** (0054,1000) VR=CS, VM=2 Series Type  */
    public static final int SeriesType = 0x00541000;

    /** (0054,1001) VR=CS, VM=1 Units  */
    public static final int Units = 0x00541001;

    /** (0054,1002) VR=CS, VM=1 Counts Source  */
    public static final int CountsSource = 0x00541002;

    /** (0054,1004) VR=CS, VM=1 Reprojection Method  */
    public static final int ReprojectionMethod = 0x00541004;

    /** (0054,1100) VR=CS, VM=1 Randoms Correction Method  */
    public static final int RandomsCorrectionMethod = 0x00541100;

    /** (0054,1101) VR=LO, VM=1 Attenuation Correction Method  */
    public static final int AttenuationCorrectionMethod = 0x00541101;

    /** (0054,1102) VR=CS, VM=1 Decay Correction  */
    public static final int DecayCorrection = 0x00541102;

    /** (0054,1103) VR=LO, VM=1 Reconstruction Method  */
    public static final int ReconstructionMethod = 0x00541103;

    /** (0054,1104) VR=LO, VM=1 Detector Lines of Response Used  */
    public static final int DetectorLinesofResponseUsed = 0x00541104;

    /** (0054,1105) VR=LO, VM=1 Scatter Correction Method  */
    public static final int ScatterCorrectionMethod = 0x00541105;

    /** (0054,1200) VR=DS, VM=1 Axial Acceptance  */
    public static final int AxialAcceptance = 0x00541200;

    /** (0054,1201) VR=IS, VM=2 Axial Mash  */
    public static final int AxialMash = 0x00541201;

    /** (0054,1202) VR=IS, VM=1 Transverse Mash  */
    public static final int TransverseMash = 0x00541202;

    /** (0054,1203) VR=DS, VM=2 Detector Element Size  */
    public static final int DetectorElementSize = 0x00541203;

    /** (0054,1210) VR=DS, VM=1 Coincidence Window Width  */
    public static final int CoincidenceWindowWidth = 0x00541210;

    /** (0054,1220) VR=CS, VM=1-n Secondary Counts Type  */
    public static final int SecondaryCountsType = 0x00541220;

    /** (0054,1300) VR=DS, VM=1 Frame Reference Time  */
    public static final int FrameReferenceTime = 0x00541300;

    /** (0054,1310) VR=IS, VM=1 Primary (Prompts) Counts Accumulated  */
    public static final int PrimaryPromptsCountsAccumulated = 0x00541310;

    /** (0054,1311) VR=IS, VM=1-n Secondary Counts Accumulated  */
    public static final int SecondaryCountsAccumulated = 0x00541311;

    /** (0054,1320) VR=DS, VM=1 Slice Sensitivity Factor  */
    public static final int SliceSensitivityFactor = 0x00541320;

    /** (0054,1321) VR=DS, VM=1 Decay Factor  */
    public static final int DecayFactor = 0x00541321;

    /** (0054,1322) VR=DS, VM=1 Dose Calibration Factor  */
    public static final int DoseCalibrationFactor = 0x00541322;

    /** (0054,1323) VR=DS, VM=1 Scatter Fraction Factor  */
    public static final int ScatterFractionFactor = 0x00541323;

    /** (0054,1324) VR=DS, VM=1 Dead Time Factor  */
    public static final int DeadTimeFactor = 0x00541324;

    /** (0054,1330) VR=US, VM=1 Image Index  */
    public static final int ImageIndex = 0x00541330;

    /** (0054,1400) VR=CS, VM=1-n Counts Included  */
    public static final int CountsIncluded = 0x00541400;

    /** (0054,1401) VR=CS, VM=1 Dead Time Correction Flag  */
    public static final int DeadTimeCorrectionFlag = 0x00541401;

    /** (0060,3000) VR=SQ, VM=1 Histogram Sequence  */
    public static final int HistogramSequence = 0x00603000;

    /** (0060,3002) VR=US, VM=1 Histogram Number of Bins  */
    public static final int HistogramNumberofBins = 0x00603002;

    /** (0060,3004) VR=US|SS, VM=1 Histogram First Bin Value  */
    public static final int HistogramFirstBinValue = 0x00603004;

    /** (0060,3006) VR=US|SS, VM=1 Histogram Last Bin Value  */
    public static final int HistogramLastBinValue = 0x00603006;

    /** (0060,3008) VR=US, VM=1 Histogram Bin Width  */
    public static final int HistogramBinWidth = 0x00603008;

    /** (0060,3010) VR=LO, VM=1 Histogram Explanation  */
    public static final int HistogramExplanation = 0x00603010;

    /** (0060,3020) VR=UL, VM=1-n Histogram Data  */
    public static final int HistogramData = 0x00603020;

    /** (0070,0001) VR=SQ, VM=1 Graphic Annotation Sequence  */
    public static final int GraphicAnnotationSequence = 0x00700001;

    /** (0070,0002) VR=CS, VM=1 Graphic Layer  */
    public static final int GraphicLayer = 0x00700002;

    /** (0070,0003) VR=CS, VM=1 Bounding Box Annotation Units  */
    public static final int BoundingBoxAnnotationUnits = 0x00700003;

    /** (0070,0004) VR=CS, VM=1 Anchor Point Annotation Units  */
    public static final int AnchorPointAnnotationUnits = 0x00700004;

    /** (0070,0005) VR=CS, VM=1 Graphic Annotation Units  */
    public static final int GraphicAnnotationUnits = 0x00700005;

    /** (0070,0006) VR=ST, VM=1 Unformatted Text Value  */
    public static final int UnformattedTextValue = 0x00700006;

    /** (0070,0008) VR=SQ, VM=1 Text Object Sequence  */
    public static final int TextObjectSequence = 0x00700008;

    /** (0070,0009) VR=SQ, VM=1 Graphic Object Sequence  */
    public static final int GraphicObjectSequence = 0x00700009;

    /** (0070,0010) VR=FL, VM=2 Bounding Box Top Left Hand Corner  */
    public static final int BoundingBoxTopLeftHandCorner = 0x00700010;

    /** (0070,0011) VR=FL, VM=2 Bounding Box Bottom Right Hand Corner  */
    public static final int BoundingBoxBottomRightHandCorner = 0x00700011;

    /** (0070,0012) VR=CS, VM=1 Bounding Box Text Horizontal Justification  */
    public static final int BoundingBoxTextHorizontalJustification = 0x00700012;

    /** (0070,0014) VR=FL, VM=2 Anchor Point  */
    public static final int AnchorPoint = 0x00700014;

    /** (0070,0015) VR=CS, VM=1 Anchor Point Visibility  */
    public static final int AnchorPointVisibility = 0x00700015;

    /** (0070,0020) VR=US, VM=1 Graphic Dimensions  */
    public static final int GraphicDimensions = 0x00700020;

    /** (0070,0021) VR=US, VM=1 Number of Graphic Points  */
    public static final int NumberofGraphicPoints = 0x00700021;

    /** (0070,0022) VR=FL, VM=2-n Graphic Data  */
    public static final int GraphicData = 0x00700022;

    /** (0070,0023) VR=CS, VM=1 Graphic Type  */
    public static final int GraphicType = 0x00700023;

    /** (0070,0024) VR=CS, VM=1 Graphic Filled  */
    public static final int GraphicFilled = 0x00700024;

    /** (0070,0041) VR=CS, VM=1 Image Horizontal Flip  */
    public static final int ImageHorizontalFlip = 0x00700041;

    /** (0070,0042) VR=US, VM=1 Image Rotation  */
    public static final int ImageRotation = 0x00700042;

    /** (0070,0052) VR=SL, VM=2 Displayed Area Top Left Hand Corner  */
    public static final int DisplayedAreaTopLeftHandCorner = 0x00700052;

    /** (0070,0053) VR=SL, VM=2 Displayed Area Bottom Right Hand Corner  */
    public static final int DisplayedAreaBottomRightHandCorner = 0x00700053;

    /** (0070,005A) VR=SQ, VM=1 Displayed Area Selection Sequence  */
    public static final int DisplayedAreaSelectionSequence = 0x0070005A;

    /** (0070,0060) VR=SQ, VM=1 Graphic Layer Sequence  */
    public static final int GraphicLayerSequence = 0x00700060;

    /** (0070,0062) VR=IS, VM=1 Graphic Layer Order  */
    public static final int GraphicLayerOrder = 0x00700062;

    /** (0070,0066) VR=US, VM=1 Graphic Layer Recommended Display Grayscale Value  */
    public static final int GraphicLayerRecommendedDisplayGrayscaleValue = 0x00700066;

    /** (0070,0067) VR=US, VM=3 Graphic Layer Recommended Display RGB Value  */
    public static final int GraphicLayerRecommendedDisplayRGBValue = 0x00700067;

    /** (0070,0068) VR=LO, VM=1 Graphic Layer Description  */
    public static final int GraphicLayerDescription = 0x00700068;

    /** (0070,0080) VR=CS, VM=1 Content Label  */
    public static final int ContentLabel = 0x00700080;

    /** (0070,0081) VR=LO, VM=1 Content Description  */
    public static final int ContentDescription = 0x00700081;

    /** (0070,0082) VR=DA, VM=1 Presentation Creation Date  */
    public static final int PresentationCreationDate = 0x00700082;

    /** (0070,0083) VR=TM, VM=1 Presentation Creation Time  */
    public static final int PresentationCreationTime = 0x00700083;

    /** (0070,0084) VR=PN, VM=1 Content Creator's Name  */
    public static final int ContentCreatorsName = 0x00700084;

    /** (0070,0100) VR=CS, VM=1 Presentation Size Mode  */
    public static final int PresentationSizeMode = 0x00700100;

    /** (0070,0101) VR=DS, VM=2 Presentation Pixel Spacing  */
    public static final int PresentationPixelSpacing = 0x00700101;

    /** (0070,0102) VR=IS, VM=2 Presentation Pixel Aspect Ratio  */
    public static final int PresentationPixelAspectRatio = 0x00700102;

    /** (0070,0103) VR=FL, VM=1 Presentation Pixel Magnification Ratio  */
    public static final int PresentationPixelMagnificationRatio = 0x00700103;

    /** (0070,0306) VR=CS, VM=1 Shape Type  */
    public static final int ShapeType = 0x00700306;

    /** (0070,0308) VR=SQ, VM=1 Registration Sequence  */
    public static final int RegistrationSequence = 0x00700308;

    /** (0070,0309) VR=SQ, VM=1 Matrix Registration Sequence  */
    public static final int MatrixRegistrationSequence = 0x00700309;

    /** (0070,030A) VR=SQ, VM=1 Matrix Sequence  */
    public static final int MatrixSequence = 0x0070030A;

    /** (0070,030C) VR=CS, VM=1 Frame of Reference Transformation Matrix Type  */
    public static final int FrameofReferenceTransformationMatrixType = 0x0070030C;

    /** (0070,030D) VR=SQ, VM=1 Registration Type Code Sequence  */
    public static final int RegistrationTypeCodeSequence = 0x0070030D;

    /** (0070,030F) VR=ST, VM=1 Fiducial Description  */
    public static final int FiducialDescription = 0x0070030F;

    /** (0070,0310) VR=SH, VM=1 Fiducial Identifier  */
    public static final int FiducialIdentifier = 0x00700310;

    /** (0070,0311) VR=SQ, VM=1 Fiducial Identifier Code Sequence  */
    public static final int FiducialIdentifierCodeSequence = 0x00700311;

    /** (0070,0312) VR=FD, VM=1 Contour Uncertainty Radius  */
    public static final int ContourUncertaintyRadius = 0x00700312;

    /** (0070,0314) VR=SQ, VM=1 Used Fiducials Sequence  */
    public static final int UsedFiducialsSequence = 0x00700314;

    /** (0070,0318) VR=SQ, VM=1 Graphic Coordinates Data Sequence  */
    public static final int GraphicCoordinatesDataSequence = 0x00700318;

    /** (0070,031A) VR=UI, VM=1 Fiducial UID  */
    public static final int FiducialUID = 0x0070031A;

    /** (0070,031C) VR=SQ, VM=1 Fiducial Set Sequence  */
    public static final int FiducialSetSequence = 0x0070031C;

    /** (0070,031E) VR=SQ, VM=1 Fiducial Sequence  */
    public static final int FiducialSequence = 0x0070031E;

    /** (0072,0002) VR=SH, VM=1 Hanging Protocol Name  */
    public static final int HangingProtocolName = 0x00720002;

    /** (0072,0004) VR=LO, VM=1 Hanging Protocol Description  */
    public static final int HangingProtocolDescription = 0x00720004;

    /** (0072,0006) VR=CS, VM=1 Hanging Protocol Level  */
    public static final int HangingProtocolLevel = 0x00720006;

    /** (0072,0008) VR=LO, VM=1 Hanging Protocol Creator  */
    public static final int HangingProtocolCreator = 0x00720008;

    /** (0072,000A) VR=DT, VM=1 Hanging Protocol Creation Datetime  */
    public static final int HangingProtocolCreationDatetime = 0x0072000A;

    /** (0072,000C) VR=SQ, VM=1 Hanging Protocol Definition Sequence  */
    public static final int HangingProtocolDefinitionSequence = 0x0072000C;

    /** (0072,000E) VR=SQ, VM=1 Hanging Protocol User Identification Code Sequence  */
    public static final int HangingProtocolUserIdentificationCodeSequence = 0x0072000E;

    /** (0072,0010) VR=LO, VM=1 Hanging Protocol User Group Name  */
    public static final int HangingProtocolUserGroupName = 0x00720010;

    /** (0072,0012) VR=SQ, VM=1 Source Hanging Protocol Sequence  */
    public static final int SourceHangingProtocolSequence = 0x00720012;

    /** (0072,0014) VR=US, VM=1 Number of Priors Referenced  */
    public static final int NumberofPriorsReferenced = 0x00720014;

    /** (0072,0020) VR=SQ, VM=1 Image Sets Sequence  */
    public static final int ImageSetsSequence = 0x00720020;

    /** (0072,0022) VR=SQ, VM=1 Image Set Selector Sequence  */
    public static final int ImageSetSelectorSequence = 0x00720022;

    /** (0072,0024) VR=CS, VM=1 Image Set Selector Usage Flag  */
    public static final int ImageSetSelectorUsageFlag = 0x00720024;

    /** (0072,0026) VR=AT, VM=1 Selector Attribute  */
    public static final int SelectorAttribute = 0x00720026;

    /** (0072,0028) VR=US, VM=1 Selector Value Number  */
    public static final int SelectorValueNumber = 0x00720028;

    /** (0072,0030) VR=SQ, VM=1 Time Based Image Sets Sequence  */
    public static final int TimeBasedImageSetsSequence = 0x00720030;

    /** (0072,0032) VR=US, VM=1 Image Set Number  */
    public static final int ImageSetNumber = 0x00720032;

    /** (0072,0034) VR=CS, VM=1 Image Set Selector Category  */
    public static final int ImageSetSelectorCategory = 0x00720034;

    /** (0072,0038) VR=US, VM=2 Relative Time  */
    public static final int RelativeTime = 0x00720038;

    /** (0072,003A) VR=CS, VM=1 Relative Time Units  */
    public static final int RelativeTimeUnits = 0x0072003A;

    /** (0072,003C) VR=SS, VM=2 Abstract Prior Value  */
    public static final int AbstractPriorValue = 0x0072003C;

    /** (0072,003E) VR=SQ, VM=1 Abstract Prior Code Sequence  */
    public static final int AbstractPriorCodeSequence = 0x0072003E;

    /** (0072,0040) VR=LO, VM=1 Image Set Label  */
    public static final int ImageSetLabel = 0x00720040;

    /** (0072,0050) VR=CS, VM=1 Selector Attribute VR  */
    public static final int SelectorAttributeVR = 0x00720050;

    /** (0072,0052) VR=AT, VM=1 Selector Sequence Pointer  */
    public static final int SelectorSequencePointer = 0x00720052;

    /** (0072,0054) VR=LO, VM=1 Selector Sequence Pointer Private Creator  */
    public static final int SelectorSequencePointerPrivateCreator = 0x00720054;

    /** (0072,0056) VR=LO, VM=1 Selector Attribute Private Creator  */
    public static final int SelectorAttributePrivateCreator = 0x00720056;

    /** (0072,0060) VR=AT, VM=1-n Selector AT Value  */
    public static final int SelectorATValue = 0x00720060;

    /** (0072,0062) VR=CS, VM=1-n Selector CS Value  */
    public static final int SelectorCSValue = 0x00720062;

    /** (0072,0064) VR=IS, VM=1-n Selector IS Value  */
    public static final int SelectorISValue = 0x00720064;

    /** (0072,0066) VR=LO, VM=1-n Selector LO Value  */
    public static final int SelectorLOValue = 0x00720066;

    /** (0072,0068) VR=LT, VM=1-n Selector LT Value  */
    public static final int SelectorLTValue = 0x00720068;

    /** (0072,006A) VR=PN, VM=1-n Selector PN Value  */
    public static final int SelectorPNValue = 0x0072006A;

    /** (0072,006C) VR=SH, VM=1-n Selector SH Value  */
    public static final int SelectorSHValue = 0x0072006C;

    /** (0072,006E) VR=ST, VM=1-n Selector ST Value  */
    public static final int SelectorSTValue = 0x0072006E;

    /** (0072,0070) VR=UT, VM=1-n Selector UT Value  */
    public static final int SelectorUTValue = 0x00720070;

    /** (0072,0072) VR=DS, VM=1-n Selector DS Value  */
    public static final int SelectorDSValue = 0x00720072;

    /** (0072,0074) VR=FD, VM=1-n Selector FD Value  */
    public static final int SelectorFDValue = 0x00720074;

    /** (0072,0076) VR=FL, VM=1-n Selector FL Value  */
    public static final int SelectorFLValue = 0x00720076;

    /** (0072,0078) VR=UL, VM=1-n Selector UL Value  */
    public static final int SelectorULValue = 0x00720078;

    /** (0072,007A) VR=US, VM=1-n Selector US Value  */
    public static final int SelectorUSValue = 0x0072007A;

    /** (0072,007C) VR=SL, VM=1-n Selector SL Value  */
    public static final int SelectorSLValue = 0x0072007C;

    /** (0072,007E) VR=SS, VM=1-n Selector SS Value  */
    public static final int SelectorSSValue = 0x0072007E;

    /** (0072,0080) VR=SQ, VM=1 Selector Code Sequence Value  */
    public static final int SelectorCodeSequenceValue = 0x00720080;

    /** (0072,0100) VR=US, VM=1 Number of Screens  */
    public static final int NumberofScreens = 0x00720100;

    /** (0072,0102) VR=SQ, VM=1 Nominal Screen Definition Sequence  */
    public static final int NominalScreenDefinitionSequence = 0x00720102;

    /** (0072,0104) VR=US, VM=1 Number of Vertical Pixels  */
    public static final int NumberofVerticalPixels = 0x00720104;

    /** (0072,0106) VR=US, VM=1 Number of Horizontal Pixels  */
    public static final int NumberofHorizontalPixels = 0x00720106;

    /** (0072,0108) VR=FD, VM=4 Display Environment Spatial Position  */
    public static final int DisplayEnvironmentSpatialPosition = 0x00720108;

    /** (0072,010A) VR=US, VM=1 Screen Minimum Grayscale Bit Depth  */
    public static final int ScreenMinimumGrayscaleBitDepth = 0x0072010A;

    /** (0072,010C) VR=US, VM=1 Screen Minimum Color Bit Depth  */
    public static final int ScreenMinimumColorBitDepth = 0x0072010C;

    /** (0072,010E) VR=US, VM=1 Application Maximum Repaint Time  */
    public static final int ApplicationMaximumRepaintTime = 0x0072010E;

    /** (0072,0200) VR=SQ, VM=1 Display Sets Sequence  */
    public static final int DisplaySetsSequence = 0x00720200;

    /** (0072,0202) VR=US, VM=1 Display Set Number  */
    public static final int DisplaySetNumber = 0x00720202;

    /** (0072,0204) VR=US, VM=1 Display Set Presentation Group  */
    public static final int DisplaySetPresentationGroup = 0x00720204;

    /** (0072,0206) VR=LO, VM=1 Display Set Presentation Group Description  */
    public static final int DisplaySetPresentationGroupDescription = 0x00720206;

    /** (0072,0208) VR=CS, VM=1 Partial Data Display Handling  */
    public static final int PartialDataDisplayHandling = 0x00720208;

    /** (0072,0210) VR=SQ, VM=1 Synchronized Scrolling Sequence  */
    public static final int SynchronizedScrollingSequence = 0x00720210;

    /** (0072,0212) VR=US, VM=2-n Display Set Scrolling Group  */
    public static final int DisplaySetScrollingGroup = 0x00720212;

    /** (0072,0214) VR=SQ, VM=1 Navigation Indicator Sequence  */
    public static final int NavigationIndicatorSequence = 0x00720214;

    /** (0072,0216) VR=US, VM=1 Navigation Display Set  */
    public static final int NavigationDisplaySet = 0x00720216;

    /** (0072,0218) VR=US, VM=1-n Reference Display Sets  */
    public static final int ReferenceDisplaySets = 0x00720218;

    /** (0072,0300) VR=SQ, VM=1 Image Boxes Sequence  */
    public static final int ImageBoxesSequence = 0x00720300;

    /** (0072,0302) VR=US, VM=1 Image Box Number  */
    public static final int ImageBoxNumber = 0x00720302;

    /** (0072,0304) VR=CS, VM=1 Image Box Layout Type  */
    public static final int ImageBoxLayoutType = 0x00720304;

    /** (0072,0306) VR=US, VM=1 Image Box Tile Horizontal Dimension  */
    public static final int ImageBoxTileHorizontalDimension = 0x00720306;

    /** (0072,0308) VR=US, VM=1 Image Box Tile Vertical Dimension  */
    public static final int ImageBoxTileVerticalDimension = 0x00720308;

    /** (0072,0310) VR=CS, VM=1 Image Box Scroll Direction  */
    public static final int ImageBoxScrollDirection = 0x00720310;

    /** (0072,0312) VR=CS, VM=1 Image Box Small Scroll Type  */
    public static final int ImageBoxSmallScrollType = 0x00720312;

    /** (0072,0314) VR=US, VM=1 Image Box Small Scroll Amount  */
    public static final int ImageBoxSmallScrollAmount = 0x00720314;

    /** (0072,0316) VR=CS, VM=1 Image Box Large Scroll Type  */
    public static final int ImageBoxLargeScrollType = 0x00720316;

    /** (0072,0318) VR=US, VM=1 Image Box Large Scroll Amount  */
    public static final int ImageBoxLargeScrollAmount = 0x00720318;

    /** (0072,0320) VR=US, VM=1 Image Box Overlap Priority  */
    public static final int ImageBoxOverlapPriority = 0x00720320;

    /** (0072,0330) VR=FD, VM=1 Cine Relative to Real-Time  */
    public static final int CineRelativetoRealTime = 0x00720330;

    /** (0072,0400) VR=SQ, VM=1 Filter Operations Sequence  */
    public static final int FilterOperationsSequence = 0x00720400;

    /** (0072,0402) VR=CS, VM=1 Filter-by Category  */
    public static final int FilterbyCategory = 0x00720402;

    /** (0072,0404) VR=CS, VM=1 Filter-by Attribute Presence  */
    public static final int FilterbyAttributePresence = 0x00720404;

    /** (0072,0406) VR=CS, VM=1 Filter-by Operator  */
    public static final int FilterbyOperator = 0x00720406;

    /** (0072,0500) VR=CS, VM=1 Blending Operation Type  */
    public static final int BlendingOperationType = 0x00720500;

    /** (0072,0510) VR=CS, VM=1 Reformatting Operation Type  */
    public static final int ReformattingOperationType = 0x00720510;

    /** (0072,0512) VR=FD, VM=1 Reformatting Thickness  */
    public static final int ReformattingThickness = 0x00720512;

    /** (0072,0514) VR=FD, VM=1 Reformatting Interval  */
    public static final int ReformattingInterval = 0x00720514;

    /** (0072,0516) VR=CS, VM=1 Reformatting Operation Initial View Direction  */
    public static final int ReformattingOperationInitialViewDirection = 0x00720516;

    /** (0072,0520) VR=CS, VM=1-n 3D Rendering Type  */
    public static final int _3DRenderingType = 0x00720520;

    /** (0072,0600) VR=SQ, VM=1 Sorting Operations Sequence  */
    public static final int SortingOperationsSequence = 0x00720600;

    /** (0072,0602) VR=CS, VM=1 Sort-by Category  */
    public static final int SortbyCategory = 0x00720602;

    /** (0072,0604) VR=CS, VM=1 Sorting Direction  */
    public static final int SortingDirection = 0x00720604;

    /** (0072,0700) VR=CS, VM=2 Display Set Patient Orientation  */
    public static final int DisplaySetPatientOrientation = 0x00720700;

    /** (0072,0702) VR=CS, VM=1 VOI Type  */
    public static final int VOIType = 0x00720702;

    /** (0072,0704) VR=CS, VM=1 Pseudo-color Type  */
    public static final int PseudocolorType = 0x00720704;

    /** (0072,0706) VR=CS, VM=1 Show Grayscale Inverted  */
    public static final int ShowGrayscaleInverted = 0x00720706;

    /** (0072,0710) VR=CS, VM=1 Show Image True Size Flag  */
    public static final int ShowImageTrueSizeFlag = 0x00720710;

    /** (0072,0712) VR=CS, VM=1 Show Graphic Annotation Flag  */
    public static final int ShowGraphicAnnotationFlag = 0x00720712;

    /** (0072,0714) VR=CS, VM=1 Show Patient Demographics Flag  */
    public static final int ShowPatientDemographicsFlag = 0x00720714;

    /** (0072,0716) VR=CS, VM=1 Show Acquisition Techniques Flag  */
    public static final int ShowAcquisitionTechniquesFlag = 0x00720716;

    /** (0088,0130) VR=SH, VM=1 Storage Media File-set ID  */
    public static final int StorageMediaFilesetID = 0x00880130;

    /** (0088,0140) VR=UI, VM=1 Storage Media File-set UID  */
    public static final int StorageMediaFilesetUID = 0x00880140;

    /** (0088,0200) VR=SQ, VM=1 Icon Image Sequence  */
    public static final int IconImageSequence = 0x00880200;

    /** (0088,0904) VR=LO, VM=1 Topic Title  */
    public static final int TopicTitle = 0x00880904;

    /** (0088,0906) VR=ST, VM=1 Topic Subject  */
    public static final int TopicSubject = 0x00880906;

    /** (0088,0910) VR=LO, VM=1 Topic Author  */
    public static final int TopicAuthor = 0x00880910;

    /** (0088,0912) VR=LO, VM=1-32 Topic Key Words  */
    public static final int TopicKeyWords = 0x00880912;

    /** (0100,0410) VR=CS, VM=1 SOP Instance Status  */
    public static final int SOPInstanceStatus = 0x01000410;

    /** (0100,0420) VR=DT, VM=1 SOP Authorization Date and Time  */
    public static final int SOPAuthorizationDateandTime = 0x01000420;

    /** (0100,0424) VR=LT, VM=1 SOP Authorization Comment  */
    public static final int SOPAuthorizationComment = 0x01000424;

    /** (0100,0426) VR=LO, VM=1 Authorization Equipment Certification Number  */
    public static final int AuthorizationEquipmentCertificationNumber = 0x01000426;

    /** (0400,0005) VR=US, VM=1 MAC ID Number  */
    public static final int MACIDNumber = 0x04000005;

    /** (0400,0010) VR=UI, VM=1 MAC Calculation Transfer Syntax UID  */
    public static final int MACCalculationTransferSyntaxUID = 0x04000010;

    /** (0400,0015) VR=CS, VM=1 MAC Algorithm  */
    public static final int MACAlgorithm = 0x04000015;

    /** (0400,0020) VR=AT, VM=1-n Data Elements Signed  */
    public static final int DataElementsSigned = 0x04000020;

    /** (0400,0100) VR=UI, VM=1 Digital Signature UID  */
    public static final int DigitalSignatureUID = 0x04000100;

    /** (0400,0105) VR=DT, VM=1 Digital Signature DateTime  */
    public static final int DigitalSignatureDateTime = 0x04000105;

    /** (0400,0110) VR=CS, VM=1 Certificate Type  */
    public static final int CertificateType = 0x04000110;

    /** (0400,0115) VR=OB, VM=1 Certificate of Signer  */
    public static final int CertificateofSigner = 0x04000115;

    /** (0400,0120) VR=OB, VM=1 Signature  */
    public static final int Signature = 0x04000120;

    /** (0400,0305) VR=CS, VM=1 Certified Timestamp Type  */
    public static final int CertifiedTimestampType = 0x04000305;

    /** (0400,0310) VR=OB, VM=1 Certified Timestamp  */
    public static final int CertifiedTimestamp = 0x04000310;

    /** (0400,0500) VR=SQ, VM=1 Encrypted Attributes Sequence  */
    public static final int EncryptedAttributesSequence = 0x04000500;

    /** (0400,0510) VR=UI, VM=1 Encrypted Content Transfer Syntax UID  */
    public static final int EncryptedContentTransferSyntaxUID = 0x04000510;

    /** (0400,0520) VR=OB, VM=1 Encrypted Content  */
    public static final int EncryptedContent = 0x04000520;

    /** (0400,0550) VR=SQ, VM=1 Modified Attributes Sequence  */
    public static final int ModifiedAttributesSequence = 0x04000550;

    /** (2000,0010) VR=IS, VM=1 Number of Copies  */
    public static final int NumberofCopies = 0x20000010;

    /** (2000,001E) VR=SQ, VM=1 Printer Configuration Sequence  */
    public static final int PrinterConfigurationSequence = 0x2000001E;

    /** (2000,0020) VR=CS, VM=1 Print Priority  */
    public static final int PrintPriority = 0x20000020;

    /** (2000,0030) VR=CS, VM=1 Medium Type  */
    public static final int MediumType = 0x20000030;

    /** (2000,0040) VR=CS, VM=1 Film Destination  */
    public static final int FilmDestination = 0x20000040;

    /** (2000,0050) VR=LO, VM=1 Film Session Label  */
    public static final int FilmSessionLabel = 0x20000050;

    /** (2000,0060) VR=IS, VM=1 Memory Allocation  */
    public static final int MemoryAllocation = 0x20000060;

    /** (2000,0061) VR=IS, VM=1 Maximum Memory Allocation  */
    public static final int MaximumMemoryAllocation = 0x20000061;

    /** (2000,0062) VR=CS, VM=1 Color Image Printing Flag  */
    public static final int ColorImagePrintingFlag = 0x20000062;

    /** (2000,0063) VR=CS, VM=1 Collation Flag  */
    public static final int CollationFlag = 0x20000063;

    /** (2000,0065) VR=CS, VM=1 Annotation Flag  */
    public static final int AnnotationFlag = 0x20000065;

    /** (2000,0067) VR=CS, VM=1 Image Overlay Flag  */
    public static final int ImageOverlayFlag = 0x20000067;

    /** (2000,0069) VR=CS, VM=1 Presentation LUT Flag  */
    public static final int PresentationLUTFlag = 0x20000069;

    /** (2000,006A) VR=CS, VM=1 Image Box Presentation LUT Flag  */
    public static final int ImageBoxPresentationLUTFlag = 0x2000006A;

    /** (2000,00A0) VR=US, VM=1 Memory Bit Depth  */
    public static final int MemoryBitDepth = 0x200000A0;

    /** (2000,00A1) VR=US, VM=1 Printing Bit Depth  */
    public static final int PrintingBitDepth = 0x200000A1;

    /** (2000,00A2) VR=SQ, VM=1 Media Installed Sequence  */
    public static final int MediaInstalledSequence = 0x200000A2;

    /** (2000,00A4) VR=SQ, VM=1 Other Media Available Sequence  */
    public static final int OtherMediaAvailableSequence = 0x200000A4;

    /** (2000,00A8) VR=SQ, VM=1 Supported Image Display Formats Sequence  */
    public static final int SupportedImageDisplayFormatsSequence = 0x200000A8;

    /** (2000,0500) VR=SQ, VM=1 Referenced Film Box Sequence  */
    public static final int ReferencedFilmBoxSequence = 0x20000500;

    /** (2000,0510) VR=SQ, VM=1 Referenced Stored Print Sequence  */
    public static final int ReferencedStoredPrintSequence = 0x20000510;

    /** (2010,0010) VR=ST, VM=1 Image Display Format  */
    public static final int ImageDisplayFormat = 0x20100010;

    /** (2010,0030) VR=CS, VM=1 Annotation Display Format ID  */
    public static final int AnnotationDisplayFormatID = 0x20100030;

    /** (2010,0040) VR=CS, VM=1 Film Orientation  */
    public static final int FilmOrientation = 0x20100040;

    /** (2010,0050) VR=CS, VM=1 Film Size ID  */
    public static final int FilmSizeID = 0x20100050;

    /** (2010,0052) VR=CS, VM=1 Printer Resolution ID  */
    public static final int PrinterResolutionID = 0x20100052;

    /** (2010,0054) VR=CS, VM=1 Default Printer Resolution ID  */
    public static final int DefaultPrinterResolutionID = 0x20100054;

    /** (2010,0060) VR=CS, VM=1 Magnification Type  */
    public static final int MagnificationType = 0x20100060;

    /** (2010,0080) VR=CS, VM=1 Smoothing Type  */
    public static final int SmoothingType = 0x20100080;

    /** (2010,00A6) VR=CS, VM=1 Default Magnification Type  */
    public static final int DefaultMagnificationType = 0x201000A6;

    /** (2010,00A7) VR=CS, VM=1-n Other Magnification Types Available  */
    public static final int OtherMagnificationTypesAvailable = 0x201000A7;

    /** (2010,00A8) VR=CS, VM=1 Default Smoothing Type  */
    public static final int DefaultSmoothingType = 0x201000A8;

    /** (2010,00A9) VR=CS, VM=1-n Other Smoothing Types Available  */
    public static final int OtherSmoothingTypesAvailable = 0x201000A9;

    /** (2010,0100) VR=CS, VM=1 Border Density  */
    public static final int BorderDensity = 0x20100100;

    /** (2010,0110) VR=CS, VM=1 Empty Image Density  */
    public static final int EmptyImageDensity = 0x20100110;

    /** (2010,0120) VR=US, VM=1 Min Density  */
    public static final int MinDensity = 0x20100120;

    /** (2010,0130) VR=US, VM=1 Max Density  */
    public static final int MaxDensity = 0x20100130;

    /** (2010,0140) VR=CS, VM=1 Trim  */
    public static final int Trim = 0x20100140;

    /** (2010,0150) VR=ST, VM=1 Configuration Information  */
    public static final int ConfigurationInformation = 0x20100150;

    /** (2010,0152) VR=LT, VM=1 Configuration Information Description  */
    public static final int ConfigurationInformationDescription = 0x20100152;

    /** (2010,0154) VR=IS, VM=1 Maximum Collated Films  */
    public static final int MaximumCollatedFilms = 0x20100154;

    /** (2010,015E) VR=US, VM=1 Illumination  */
    public static final int Illumination = 0x2010015E;

    /** (2010,0160) VR=US, VM=1 Reflected Ambient Light  */
    public static final int ReflectedAmbientLight = 0x20100160;

    /** (2010,0376) VR=DS, VM=2 Printer Pixel Spacing  */
    public static final int PrinterPixelSpacing = 0x20100376;

    /** (2010,0500) VR=SQ, VM=1 Referenced Film Session Sequence  */
    public static final int ReferencedFilmSessionSequence = 0x20100500;

    /** (2010,0510) VR=SQ, VM=1 Referenced Image Box Sequence  */
    public static final int ReferencedImageBoxSequence = 0x20100510;

    /** (2010,0520) VR=SQ, VM=1 Referenced Basic Annotation Box Sequence  */
    public static final int ReferencedBasicAnnotationBoxSequence = 0x20100520;

    /** (2020,0010) VR=US, VM=1 Image Position  */
    public static final int ImagePosition = 0x20200010;

    /** (2020,0020) VR=CS, VM=1 Polarity  */
    public static final int Polarity = 0x20200020;

    /** (2020,0030) VR=DS, VM=1 Requested Image Size  */
    public static final int RequestedImageSize = 0x20200030;

    /** (2020,0040) VR=CS, VM=1 Requested Decimate/Crop Behavior  */
    public static final int RequestedDecimateCropBehavior = 0x20200040;

    /** (2020,0050) VR=CS, VM=1 Requested Resolution ID  */
    public static final int RequestedResolutionID = 0x20200050;

    /** (2020,00A0) VR=CS, VM=1 Requested Image Size Flag  */
    public static final int RequestedImageSizeFlag = 0x202000A0;

    /** (2020,00A2) VR=CS, VM=1 Decimate/Crop Result  */
    public static final int DecimateCropResult = 0x202000A2;

    /** (2020,0110) VR=SQ, VM=1 Basic Grayscale Image Sequence  */
    public static final int BasicGrayscaleImageSequence = 0x20200110;

    /** (2020,0111) VR=SQ, VM=1 Basic Color Image Sequence  */
    public static final int BasicColorImageSequence = 0x20200111;

    /** (2020,0130) VR=SQ, VM=1 Referenced Image Overlay Box Sequence RET */
    public static final int ReferencedImageOverlayBoxSequenceRET = 0x20200130;

    /** (2020,0140) VR=SQ, VM=1 Referenced VOI LUT Box Sequence RET */
    public static final int ReferencedVOILUTBoxSequenceRET = 0x20200140;

    /** (2030,0010) VR=US, VM=1 Annotation Position  */
    public static final int AnnotationPosition = 0x20300010;

    /** (2030,0020) VR=LO, VM=1 Text String  */
    public static final int TextString = 0x20300020;

    /** (2040,0010) VR=SQ, VM=1 Referenced Overlay Plane Sequence  */
    public static final int ReferencedOverlayPlaneSequence = 0x20400010;

    /** (2040,0011) VR=US, VM=1-99 Referenced Overlay Plane Groups  */
    public static final int ReferencedOverlayPlaneGroups = 0x20400011;

    /** (2040,0020) VR=SQ, VM=1 Overlay Pixel Data Sequence  */
    public static final int OverlayPixelDataSequence = 0x20400020;

    /** (2040,0060) VR=CS, VM=1 Overlay Magnification Type  */
    public static final int OverlayMagnificationType = 0x20400060;

    /** (2040,0070) VR=CS, VM=1 Overlay Smoothing Type  */
    public static final int OverlaySmoothingType = 0x20400070;

    /** (2040,0072) VR=CS, VM=1 Overlay or Image Magnification  */
    public static final int OverlayorImageMagnification = 0x20400072;

    /** (2040,0074) VR=US, VM=1 Magnify to Number of Columns  */
    public static final int MagnifytoNumberofColumns = 0x20400074;

    /** (2040,0080) VR=CS, VM=1 Overlay Foreground Density  */
    public static final int OverlayForegroundDensity = 0x20400080;

    /** (2040,0082) VR=CS, VM=1 Overlay Background Density  */
    public static final int OverlayBackgroundDensity = 0x20400082;

    /** (2040,0090) VR=CS, VM=1 Overlay Mode RET */
    public static final int OverlayModeRET = 0x20400090;

    /** (2040,0100) VR=CS, VM=1 Threshold Density RET */
    public static final int ThresholdDensityRET = 0x20400100;

    /** (2040,0500) VR=SQ, VM=1 Referenced Image Box Sequence RET */
    public static final int ReferencedImageBoxSequenceRET = 0x20400500;

    /** (2050,0010) VR=SQ, VM=1 Presentation LUT Sequence  */
    public static final int PresentationLUTSequence = 0x20500010;

    /** (2050,0020) VR=CS, VM=1 Presentation LUT Shape  */
    public static final int PresentationLUTShape = 0x20500020;

    /** (2050,0500) VR=SQ, VM=1 Referenced Presentation LUT Sequence  */
    public static final int ReferencedPresentationLUTSequence = 0x20500500;

    /** (2100,0010) VR=SH, VM=1 Print Job ID  */
    public static final int PrintJobID = 0x21000010;

    /** (2100,0020) VR=CS, VM=1 Execution Status  */
    public static final int ExecutionStatus = 0x21000020;

    /** (2100,0030) VR=CS, VM=1 Execution Status Info  */
    public static final int ExecutionStatusInfo = 0x21000030;

    /** (2100,0040) VR=DA, VM=1 Creation Date  */
    public static final int CreationDate = 0x21000040;

    /** (2100,0050) VR=TM, VM=1 Creation Time  */
    public static final int CreationTime = 0x21000050;

    /** (2100,0070) VR=AE, VM=1 Originator  */
    public static final int Originator = 0x21000070;

    /** (2100,0140) VR=AE, VM=1 Destination AE  */
    public static final int DestinationAE = 0x21000140;

    /** (2100,0160) VR=SH, VM=1 Owner ID  */
    public static final int OwnerID = 0x21000160;

    /** (2100,0170) VR=IS, VM=1 Number of Films  */
    public static final int NumberofFilms = 0x21000170;

    /** (2100,0500) VR=SQ, VM=1 Referenced Print Job Sequence  */
    public static final int ReferencedPrintJobSequence21000500 = 0x21000500;

    /** (2110,0010) VR=CS, VM=1 Printer Status  */
    public static final int PrinterStatus = 0x21100010;

    /** (2110,0020) VR=CS, VM=1 Printer Status Info  */
    public static final int PrinterStatusInfo = 0x21100020;

    /** (2110,0030) VR=LO, VM=1 Printer Name  */
    public static final int PrinterName = 0x21100030;

    /** (2110,0099) VR=SH, VM=1 Print Queue ID  */
    public static final int PrintQueueID = 0x21100099;

    /** (2120,0010) VR=CS, VM=1 Queue Status  */
    public static final int QueueStatus = 0x21200010;

    /** (2120,0050) VR=SQ, VM=1 Print Job Description Sequence  */
    public static final int PrintJobDescriptionSequence = 0x21200050;

    /** (2120,0070) VR=SQ, VM=1 Referenced Print Job Sequence  */
    public static final int ReferencedPrintJobSequence21200070 = 0x21200070;

    /** (2130,0010) VR=SQ, VM=1 Print Management Capabilities Sequence  */
    public static final int PrintManagementCapabilitiesSequence = 0x21300010;

    /** (2130,0015) VR=SQ, VM=1 Printer Characteristics Sequence  */
    public static final int PrinterCharacteristicsSequence = 0x21300015;

    /** (2130,0030) VR=SQ, VM=1 Film Box Content Sequence  */
    public static final int FilmBoxContentSequence = 0x21300030;

    /** (2130,0040) VR=SQ, VM=1 Image Box Content Sequence  */
    public static final int ImageBoxContentSequence = 0x21300040;

    /** (2130,0050) VR=SQ, VM=1 Annotation Content Sequence  */
    public static final int AnnotationContentSequence = 0x21300050;

    /** (2130,0060) VR=SQ, VM=1 Image Overlay Box Content Sequence  */
    public static final int ImageOverlayBoxContentSequence = 0x21300060;

    /** (2130,0080) VR=SQ, VM=1 Presentation LUT Content Sequence  */
    public static final int PresentationLUTContentSequence = 0x21300080;

    /** (2130,00A0) VR=SQ, VM=1 Proposed Study Sequence  */
    public static final int ProposedStudySequence = 0x213000A0;

    /** (2130,00C0) VR=SQ, VM=1 Original Image Sequence  */
    public static final int OriginalImageSequence = 0x213000C0;

    /** (2200,0001) VR=CS, VM=1 Label Using Information Extracted From Instances  */
    public static final int LabelUsingInformationExtractedFromInstances = 0x22000001;

    /** (2200,0002) VR=UT, VM=1 Label Text  */
    public static final int LabelText = 0x22000002;

    /** (2200,0003) VR=CS, VM=1 Label Style Selection  */
    public static final int LabelStyleSelection = 0x22000003;

    /** (2200,0004) VR=LT, VM=1 Media Disposition  */
    public static final int MediaDisposition = 0x22000004;

    /** (2200,0005) VR=LT, VM=1 Barcode Value  */
    public static final int BarcodeValue = 0x22000005;

    /** (2200,0006) VR=CS, VM=1 Barcode Symbology  */
    public static final int BarcodeSymbology = 0x22000006;

    /** (2200,0007) VR=CS, VM=1 Allow Media Splitting  */
    public static final int AllowMediaSplitting = 0x22000007;

    /** (2200,0008) VR=CS, VM=1 Include Non-DICOM Objects  */
    public static final int IncludeNonDICOMObjects = 0x22000008;

    /** (2200,0009) VR=CS, VM=1 Include Display Application  */
    public static final int IncludeDisplayApplication = 0x22000009;

    /** (2200,000A) VR=CS, VM=1 Preserve Composite Instances After Media Creation  */
    public static final int PreserveCompositeInstancesAfterMediaCreation = 0x2200000A;

    /** (2200,000B) VR=US, VM=1 Total Number of Pieces of Media Created  */
    public static final int TotalNumberofPiecesofMediaCreated = 0x2200000B;

    /** (2200,000C) VR=LO, VM=1 Requested Media Application Profile  */
    public static final int RequestedMediaApplicationProfile = 0x2200000C;

    /** (2200,000D) VR=SQ, VM=1 Referenced Storage Media Sequence  */
    public static final int ReferencedStorageMediaSequence = 0x2200000D;

    /** (2200,000E) VR=AT, VM=1-n Failure Attributes  */
    public static final int FailureAttributes = 0x2200000E;

    /** (2200,000F) VR=CS, VM=1 Allow Lossy Compression  */
    public static final int AllowLossyCompression = 0x2200000F;

    /** (2200,0020) VR=CS, VM=1 Request Priority  */
    public static final int RequestPriority = 0x22000020;

    /** (3002,0002) VR=SH, VM=1 RT Image Label  */
    public static final int RTImageLabel = 0x30020002;

    /** (3002,0003) VR=LO, VM=1 RT Image Name  */
    public static final int RTImageName = 0x30020003;

    /** (3002,0004) VR=ST, VM=1 RT Image Description  */
    public static final int RTImageDescription = 0x30020004;

    /** (3002,000A) VR=CS, VM=1 Reported Values Origin  */
    public static final int ReportedValuesOrigin = 0x3002000A;

    /** (3002,000C) VR=CS, VM=1 RT Image Plane  */
    public static final int RTImagePlane = 0x3002000C;

    /** (3002,000D) VR=DS, VM=3 X-Ray Image Receptor Translation  */
    public static final int XRayImageReceptorTranslation = 0x3002000D;

    /** (3002,000E) VR=DS, VM=1 X-Ray Image Receptor Angle  */
    public static final int XRayImageReceptorAngle = 0x3002000E;

    /** (3002,0010) VR=DS, VM=6 RT Image Orientation  */
    public static final int RTImageOrientation = 0x30020010;

    /** (3002,0011) VR=DS, VM=2 Image Plane Pixel Spacing  */
    public static final int ImagePlanePixelSpacing = 0x30020011;

    /** (3002,0012) VR=DS, VM=2 RT Image Position  */
    public static final int RTImagePosition = 0x30020012;

    /** (3002,0020) VR=SH, VM=1 Radiation Machine Name  */
    public static final int RadiationMachineName = 0x30020020;

    /** (3002,0022) VR=DS, VM=1 Radiation Machine SAD  */
    public static final int RadiationMachineSAD = 0x30020022;

    /** (3002,0024) VR=DS, VM=1 Radiation Machine SSD  */
    public static final int RadiationMachineSSD = 0x30020024;

    /** (3002,0026) VR=DS, VM=1 RT Image SID  */
    public static final int RTImageSID = 0x30020026;

    /** (3002,0028) VR=DS, VM=1 Source to Reference Object Distance  */
    public static final int SourcetoReferenceObjectDistance = 0x30020028;

    /** (3002,0029) VR=IS, VM=1 Fraction Number  */
    public static final int FractionNumber = 0x30020029;

    /** (3002,0030) VR=SQ, VM=1 Exposure Sequence  */
    public static final int ExposureSequence = 0x30020030;

    /** (3002,0032) VR=DS, VM=1 Meterset Exposure  */
    public static final int MetersetExposure = 0x30020032;

    /** (3002,0034) VR=DS, VM=4 Diaphragm Position  */
    public static final int DiaphragmPosition = 0x30020034;

    /** (3002,0040) VR=SQ, VM=1 Fluence Map Sequence  */
    public static final int FluenceMapSequence = 0x30020040;

    /** (3002,0041) VR=CS, VM=1 Fluence Data Source  */
    public static final int FluenceDataSource = 0x30020041;

    /** (3002,0042) VR=DS, VM=1 Fluence Data Scale  */
    public static final int FluenceDataScale = 0x30020042;

    /** (3004,0001) VR=CS, VM=1 DVH Type  */
    public static final int DVHType = 0x30040001;

    /** (3004,0002) VR=CS, VM=1 Dose Units  */
    public static final int DoseUnits = 0x30040002;

    /** (3004,0004) VR=CS, VM=1 Dose Type  */
    public static final int DoseType = 0x30040004;

    /** (3004,0006) VR=LO, VM=1 Dose Comment  */
    public static final int DoseComment = 0x30040006;

    /** (3004,0008) VR=DS, VM=3 Normalization Point  */
    public static final int NormalizationPoint = 0x30040008;

    /** (3004,000A) VR=CS, VM=1 Dose Summation Type  */
    public static final int DoseSummationType = 0x3004000A;

    /** (3004,000C) VR=DS, VM=2-n Grid Frame Offset Vector  */
    public static final int GridFrameOffsetVector = 0x3004000C;

    /** (3004,000E) VR=DS, VM=1 Dose Grid Scaling  */
    public static final int DoseGridScaling = 0x3004000E;

    /** (3004,0010) VR=SQ, VM=1 RT Dose ROI Sequence  */
    public static final int RTDoseROISequence = 0x30040010;

    /** (3004,0012) VR=DS, VM=1 Dose Value  */
    public static final int DoseValue = 0x30040012;

    /** (3004,0014) VR=CS, VM=1-3 Tissue Heterogeneity Correction  */
    public static final int TissueHeterogeneityCorrection = 0x30040014;

    /** (3004,0040) VR=DS, VM=3 DVH Normalization Point  */
    public static final int DVHNormalizationPoint = 0x30040040;

    /** (3004,0042) VR=DS, VM=1 DVH Normalization Dose Value  */
    public static final int DVHNormalizationDoseValue = 0x30040042;

    /** (3004,0050) VR=SQ, VM=1 DVH Sequence  */
    public static final int DVHSequence = 0x30040050;

    /** (3004,0052) VR=DS, VM=1 DVH Dose Scaling  */
    public static final int DVHDoseScaling = 0x30040052;

    /** (3004,0054) VR=CS, VM=1 DVH Volume Units  */
    public static final int DVHVolumeUnits = 0x30040054;

    /** (3004,0056) VR=IS, VM=1 DVH Number of Bins  */
    public static final int DVHNumberofBins = 0x30040056;

    /** (3004,0058) VR=DS, VM=2-2n DVH Data  */
    public static final int DVHData = 0x30040058;

    /** (3004,0060) VR=SQ, VM=1 DVH Referenced ROI Sequence  */
    public static final int DVHReferencedROISequence = 0x30040060;

    /** (3004,0062) VR=CS, VM=1 DVH ROI Contribution Type  */
    public static final int DVHROIContributionType = 0x30040062;

    /** (3004,0070) VR=DS, VM=1 DVH Minimum Dose  */
    public static final int DVHMinimumDose = 0x30040070;

    /** (3004,0072) VR=DS, VM=1 DVH Maximum Dose  */
    public static final int DVHMaximumDose = 0x30040072;

    /** (3004,0074) VR=DS, VM=1 DVH Mean Dose  */
    public static final int DVHMeanDose = 0x30040074;

    /** (3006,0002) VR=SH, VM=1 Structure Set Label  */
    public static final int StructureSetLabel = 0x30060002;

    /** (3006,0004) VR=LO, VM=1 Structure Set Name  */
    public static final int StructureSetName = 0x30060004;

    /** (3006,0006) VR=ST, VM=1 Structure Set Description  */
    public static final int StructureSetDescription = 0x30060006;

    /** (3006,0008) VR=DA, VM=1 Structure Set Date  */
    public static final int StructureSetDate = 0x30060008;

    /** (3006,0009) VR=TM, VM=1 Structure Set Time  */
    public static final int StructureSetTime = 0x30060009;

    /** (3006,0010) VR=SQ, VM=1 Referenced Frame of Reference Sequence  */
    public static final int ReferencedFrameofReferenceSequence = 0x30060010;

    /** (3006,0012) VR=SQ, VM=1 RT Referenced Study Sequence  */
    public static final int RTReferencedStudySequence = 0x30060012;

    /** (3006,0014) VR=SQ, VM=1 RT Referenced Series Sequence  */
    public static final int RTReferencedSeriesSequence = 0x30060014;

    /** (3006,0016) VR=SQ, VM=1 Contour Image Sequence  */
    public static final int ContourImageSequence = 0x30060016;

    /** (3006,0020) VR=SQ, VM=1 Structure Set ROI Sequence  */
    public static final int StructureSetROISequence = 0x30060020;

    /** (3006,0022) VR=IS, VM=1 ROI Number  */
    public static final int ROINumber = 0x30060022;

    /** (3006,0024) VR=UI, VM=1 Referenced Frame of Reference UID  */
    public static final int ReferencedFrameofReferenceUID = 0x30060024;

    /** (3006,0026) VR=LO, VM=1 ROI Name  */
    public static final int ROIName = 0x30060026;

    /** (3006,0028) VR=ST, VM=1 ROI Description  */
    public static final int ROIDescription = 0x30060028;

    /** (3006,002A) VR=IS, VM=3 ROI Display Color  */
    public static final int ROIDisplayColor = 0x3006002A;

    /** (3006,002C) VR=DS, VM=1 ROI Volume  */
    public static final int ROIVolume = 0x3006002C;

    /** (3006,0030) VR=SQ, VM=1 RT Related ROI Sequence  */
    public static final int RTRelatedROISequence = 0x30060030;

    /** (3006,0033) VR=CS, VM=1 RT ROI Relationship  */
    public static final int RTROIRelationship = 0x30060033;

    /** (3006,0036) VR=CS, VM=1 ROI Generation Algorithm  */
    public static final int ROIGenerationAlgorithm = 0x30060036;

    /** (3006,0038) VR=LO, VM=1 ROI Generation Description  */
    public static final int ROIGenerationDescription = 0x30060038;

    /** (3006,0039) VR=SQ, VM=1 ROI Contour Sequence  */
    public static final int ROIContourSequence = 0x30060039;

    /** (3006,0040) VR=SQ, VM=1 Contour Sequence  */
    public static final int ContourSequence = 0x30060040;

    /** (3006,0042) VR=CS, VM=1 Contour Geometric Type  */
    public static final int ContourGeometricType = 0x30060042;

    /** (3006,0044) VR=DS, VM=1 Contour Slab Thickness  */
    public static final int ContourSlabThickness = 0x30060044;

    /** (3006,0045) VR=DS, VM=3 Contour Offset Vector  */
    public static final int ContourOffsetVector = 0x30060045;

    /** (3006,0046) VR=IS, VM=1 Number of Contour Points  */
    public static final int NumberofContourPoints = 0x30060046;

    /** (3006,0048) VR=IS, VM=1 Contour Number  */
    public static final int ContourNumber = 0x30060048;

    /** (3006,0049) VR=IS, VM=1-n Attached Contours  */
    public static final int AttachedContours = 0x30060049;

    /** (3006,0050) VR=DS, VM=3-3n Contour Data  */
    public static final int ContourData = 0x30060050;

    /** (3006,0080) VR=SQ, VM=1 RT ROI Observations Sequence  */
    public static final int RTROIObservationsSequence = 0x30060080;

    /** (3006,0082) VR=IS, VM=1 Observation Number  */
    public static final int ObservationNumber = 0x30060082;

    /** (3006,0084) VR=IS, VM=1 Referenced ROI Number  */
    public static final int ReferencedROINumber = 0x30060084;

    /** (3006,0085) VR=SH, VM=1 ROI Observation Label  */
    public static final int ROIObservationLabel = 0x30060085;

    /** (3006,0086) VR=SQ, VM=1 RT ROI Identification Code Sequence  */
    public static final int RTROIIdentificationCodeSequence = 0x30060086;

    /** (3006,0088) VR=ST, VM=1 ROI Observation Description  */
    public static final int ROIObservationDescription = 0x30060088;

    /** (3006,00A0) VR=SQ, VM=1 Related RT ROI Observations Sequence  */
    public static final int RelatedRTROIObservationsSequence = 0x300600A0;

    /** (3006,00A4) VR=CS, VM=1 RT ROI Interpreted Type  */
    public static final int RTROIInterpretedType = 0x300600A4;

    /** (3006,00A6) VR=PN, VM=1 ROI Interpreter  */
    public static final int ROIInterpreter = 0x300600A6;

    /** (3006,00B0) VR=SQ, VM=1 ROI Physical Properties Sequence  */
    public static final int ROIPhysicalPropertiesSequence = 0x300600B0;

    /** (3006,00B2) VR=CS, VM=1 ROI Physical Property  */
    public static final int ROIPhysicalProperty = 0x300600B2;

    /** (3006,00B4) VR=DS, VM=1 ROI Physical Property Value  */
    public static final int ROIPhysicalPropertyValue = 0x300600B4;

    /** (3006,00C0) VR=SQ, VM=1 Frame of Reference Relationship Sequence  */
    public static final int FrameofReferenceRelationshipSequence = 0x300600C0;

    /** (3006,00C2) VR=UI, VM=1 Related Frame of Reference UID  */
    public static final int RelatedFrameofReferenceUID = 0x300600C2;

    /** (3006,00C4) VR=CS, VM=1 Frame of Reference Transformation Type  */
    public static final int FrameofReferenceTransformationType = 0x300600C4;

    /** (3006,00C6) VR=DS, VM=16 Frame of Reference Transformation Matrix  */
    public static final int FrameofReferenceTransformationMatrix = 0x300600C6;

    /** (3006,00C8) VR=LO, VM=1 Frame of Reference Transformation Comment  */
    public static final int FrameofReferenceTransformationComment = 0x300600C8;

    /** (3008,0010) VR=SQ, VM=1 Measured Dose Reference Sequence  */
    public static final int MeasuredDoseReferenceSequence = 0x30080010;

    /** (3008,0012) VR=ST, VM=1 Measured Dose Description  */
    public static final int MeasuredDoseDescription = 0x30080012;

    /** (3008,0014) VR=CS, VM=1 Measured Dose Type  */
    public static final int MeasuredDoseType = 0x30080014;

    /** (3008,0016) VR=DS, VM=1 Measured Dose Value  */
    public static final int MeasuredDoseValue = 0x30080016;

    /** (3008,0020) VR=SQ, VM=1 Treatment Session Beam Sequence  */
    public static final int TreatmentSessionBeamSequence = 0x30080020;

    /** (3008,0022) VR=IS, VM=1 Current Fraction Number  */
    public static final int CurrentFractionNumber = 0x30080022;

    /** (3008,0024) VR=DA, VM=1 Treatment Control Point Date  */
    public static final int TreatmentControlPointDate = 0x30080024;

    /** (3008,0025) VR=TM, VM=1 Treatment Control Point Time  */
    public static final int TreatmentControlPointTime = 0x30080025;

    /** (3008,002A) VR=CS, VM=1 Treatment Termination Status  */
    public static final int TreatmentTerminationStatus = 0x3008002A;

    /** (3008,002B) VR=SH, VM=1 Treatment Termination Code  */
    public static final int TreatmentTerminationCode = 0x3008002B;

    /** (3008,002C) VR=CS, VM=1 Treatment Verification Status  */
    public static final int TreatmentVerificationStatus = 0x3008002C;

    /** (3008,0030) VR=SQ, VM=1 Referenced Treatment Record Sequence  */
    public static final int ReferencedTreatmentRecordSequence = 0x30080030;

    /** (3008,0032) VR=DS, VM=1 Specified Primary Meterset  */
    public static final int SpecifiedPrimaryMeterset = 0x30080032;

    /** (3008,0033) VR=DS, VM=1 Specified Secondary Meterset  */
    public static final int SpecifiedSecondaryMeterset = 0x30080033;

    /** (3008,0036) VR=DS, VM=1 Delivered Primary Meterset  */
    public static final int DeliveredPrimaryMeterset = 0x30080036;

    /** (3008,0037) VR=DS, VM=1 Delivered Secondary Meterset  */
    public static final int DeliveredSecondaryMeterset = 0x30080037;

    /** (3008,003A) VR=DS, VM=1 Specified Treatment Time  */
    public static final int SpecifiedTreatmentTime = 0x3008003A;

    /** (3008,003B) VR=DS, VM=1 Delivered Treatment Time  */
    public static final int DeliveredTreatmentTime = 0x3008003B;

    /** (3008,0040) VR=SQ, VM=1 Control Point Delivery Sequence  */
    public static final int ControlPointDeliverySequence = 0x30080040;

    /** (3008,0042) VR=DS, VM=1 Specified Meterset  */
    public static final int SpecifiedMeterset = 0x30080042;

    /** (3008,0044) VR=DS, VM=1 Delivered Meterset  */
    public static final int DeliveredMeterset = 0x30080044;

    /** (3008,0048) VR=DS, VM=1 Dose Rate Delivered  */
    public static final int DoseRateDelivered = 0x30080048;

    /** (3008,0050) VR=SQ, VM=1 Treatment Summary Calculated Dose Reference Sequence  */
    public static final int TreatmentSummaryCalculatedDoseReferenceSequence = 0x30080050;

    /** (3008,0052) VR=DS, VM=1 Cumulative Dose to Dose Reference  */
    public static final int CumulativeDosetoDoseReference = 0x30080052;

    /** (3008,0054) VR=DA, VM=1 First Treatment Date  */
    public static final int FirstTreatmentDate = 0x30080054;

    /** (3008,0056) VR=DA, VM=1 Most Recent Treatment Date  */
    public static final int MostRecentTreatmentDate = 0x30080056;

    /** (3008,005A) VR=IS, VM=1 Number of Fractions Delivered  */
    public static final int NumberofFractionsDelivered = 0x3008005A;

    /** (3008,0060) VR=SQ, VM=1 Override Sequence  */
    public static final int OverrideSequence = 0x30080060;

    /** (3008,0062) VR=AT, VM=1 Override Parameter Pointer  */
    public static final int OverrideParameterPointer = 0x30080062;

    /** (3008,0064) VR=IS, VM=1 Measured Dose Reference Number  */
    public static final int MeasuredDoseReferenceNumber = 0x30080064;

    /** (3008,0066) VR=ST, VM=1 Override Reason  */
    public static final int OverrideReason = 0x30080066;

    /** (3008,0070) VR=SQ, VM=1 Calculated Dose Reference Sequence  */
    public static final int CalculatedDoseReferenceSequence = 0x30080070;

    /** (3008,0072) VR=IS, VM=1 Calculated Dose Reference Number  */
    public static final int CalculatedDoseReferenceNumber = 0x30080072;

    /** (3008,0074) VR=ST, VM=1 Calculated Dose Reference Description  */
    public static final int CalculatedDoseReferenceDescription = 0x30080074;

    /** (3008,0076) VR=DS, VM=1 Calculated Dose Reference Dose Value  */
    public static final int CalculatedDoseReferenceDoseValue = 0x30080076;

    /** (3008,0078) VR=DS, VM=1 Start Meterset  */
    public static final int StartMeterset = 0x30080078;

    /** (3008,007A) VR=DS, VM=1 End Meterset  */
    public static final int EndMeterset = 0x3008007A;

    /** (3008,0080) VR=SQ, VM=1 Referenced Measured Dose Reference Sequence  */
    public static final int ReferencedMeasuredDoseReferenceSequence = 0x30080080;

    /** (3008,0082) VR=IS, VM=1 Referenced Measured Dose Reference Number  */
    public static final int ReferencedMeasuredDoseReferenceNumber = 0x30080082;

    /** (3008,0090) VR=SQ, VM=1 Referenced Calculated Dose Reference Sequence  */
    public static final int ReferencedCalculatedDoseReferenceSequence = 0x30080090;

    /** (3008,0092) VR=IS, VM=1 Referenced Calculated Dose Reference Number  */
    public static final int ReferencedCalculatedDoseReferenceNumber = 0x30080092;

    /** (3008,00A0) VR=SQ, VM=1 Beam Limiting Device Leaf Pairs Sequence  */
    public static final int BeamLimitingDeviceLeafPairsSequence = 0x300800A0;

    /** (3008,00B0) VR=SQ, VM=1 Recorded Wedge Sequence  */
    public static final int RecordedWedgeSequence = 0x300800B0;

    /** (3008,00C0) VR=SQ, VM=1 Recorded Compensator Sequence  */
    public static final int RecordedCompensatorSequence = 0x300800C0;

    /** (3008,00D0) VR=SQ, VM=1 Recorded Block Sequence  */
    public static final int RecordedBlockSequence = 0x300800D0;

    /** (3008,00E0) VR=SQ, VM=1 Treatment Summary Measured Dose Reference Sequence  */
    public static final int TreatmentSummaryMeasuredDoseReferenceSequence = 0x300800E0;

    /** (3008,0100) VR=SQ, VM=1 Recorded Source Sequence  */
    public static final int RecordedSourceSequence = 0x30080100;

    /** (3008,0105) VR=LO, VM=1 Source Serial Number  */
    public static final int SourceSerialNumber = 0x30080105;

    /** (3008,0110) VR=SQ, VM=1 Treatment Session Application Setup Sequence  */
    public static final int TreatmentSessionApplicationSetupSequence = 0x30080110;

    /** (3008,0116) VR=CS, VM=1 Application Setup Check  */
    public static final int ApplicationSetupCheck = 0x30080116;

    /** (3008,0120) VR=SQ, VM=1 Recorded Brachy Accessory Device Sequence  */
    public static final int RecordedBrachyAccessoryDeviceSequence = 0x30080120;

    /** (3008,0122) VR=IS, VM=1 Referenced Brachy Accessory Device Number  */
    public static final int ReferencedBrachyAccessoryDeviceNumber = 0x30080122;

    /** (3008,0130) VR=SQ, VM=1 Recorded Channel Sequence  */
    public static final int RecordedChannelSequence = 0x30080130;

    /** (3008,0132) VR=DS, VM=1 Specified Channel Total Time  */
    public static final int SpecifiedChannelTotalTime = 0x30080132;

    /** (3008,0134) VR=DS, VM=1 Delivered Channel Total Time  */
    public static final int DeliveredChannelTotalTime = 0x30080134;

    /** (3008,0136) VR=IS, VM=1 Specified Number of Pulses  */
    public static final int SpecifiedNumberofPulses = 0x30080136;

    /** (3008,0138) VR=IS, VM=1 Delivered Number of Pulses  */
    public static final int DeliveredNumberofPulses = 0x30080138;

    /** (3008,013A) VR=DS, VM=1 Specified Pulse Repetition Interval  */
    public static final int SpecifiedPulseRepetitionInterval = 0x3008013A;

    /** (3008,013C) VR=DS, VM=1 Delivered Pulse Repetition Interval  */
    public static final int DeliveredPulseRepetitionInterval = 0x3008013C;

    /** (3008,0140) VR=SQ, VM=1 Recorded Source Applicator Sequence  */
    public static final int RecordedSourceApplicatorSequence = 0x30080140;

    /** (3008,0142) VR=IS, VM=1 Referenced Source Applicator Number  */
    public static final int ReferencedSourceApplicatorNumber = 0x30080142;

    /** (3008,0150) VR=SQ, VM=1 Recorded Channel Shield Sequence  */
    public static final int RecordedChannelShieldSequence = 0x30080150;

    /** (3008,0152) VR=IS, VM=1 Referenced Channel Shield Number  */
    public static final int ReferencedChannelShieldNumber = 0x30080152;

    /** (3008,0160) VR=SQ, VM=1 Brachy Control Point Delivered Sequence  */
    public static final int BrachyControlPointDeliveredSequence = 0x30080160;

    /** (3008,0162) VR=DA, VM=1 Safe Position Exit Date  */
    public static final int SafePositionExitDate = 0x30080162;

    /** (3008,0164) VR=TM, VM=1 Safe Position Exit Time  */
    public static final int SafePositionExitTime = 0x30080164;

    /** (3008,0166) VR=DA, VM=1 Safe Position Return Date  */
    public static final int SafePositionReturnDate = 0x30080166;

    /** (3008,0168) VR=TM, VM=1 Safe Position Return Time  */
    public static final int SafePositionReturnTime = 0x30080168;

    /** (3008,0200) VR=CS, VM=1 Current Treatment Status  */
    public static final int CurrentTreatmentStatus = 0x30080200;

    /** (3008,0202) VR=ST, VM=1 Treatment Status Comment  */
    public static final int TreatmentStatusComment = 0x30080202;

    /** (3008,0220) VR=SQ, VM=1 Fraction Group Summary Sequence  */
    public static final int FractionGroupSummarySequence = 0x30080220;

    /** (3008,0223) VR=IS, VM=1 Referenced Fraction Number  */
    public static final int ReferencedFractionNumber = 0x30080223;

    /** (3008,0224) VR=CS, VM=1 Fraction Group Type  */
    public static final int FractionGroupType = 0x30080224;

    /** (3008,0230) VR=CS, VM=1 Beam Stopper Position  */
    public static final int BeamStopperPosition = 0x30080230;

    /** (3008,0240) VR=SQ, VM=1 Fraction Status Summary Sequence  */
    public static final int FractionStatusSummarySequence = 0x30080240;

    /** (3008,0250) VR=DA, VM=1 Treatment Date  */
    public static final int TreatmentDate = 0x30080250;

    /** (3008,0251) VR=TM, VM=1 Treatment Time  */
    public static final int TreatmentTime = 0x30080251;

    /** (300A,0002) VR=SH, VM=1 RT Plan Label  */
    public static final int RTPlanLabel = 0x300A0002;

    /** (300A,0003) VR=LO, VM=1 RT Plan Name  */
    public static final int RTPlanName = 0x300A0003;

    /** (300A,0004) VR=ST, VM=1 RT Plan Description  */
    public static final int RTPlanDescription = 0x300A0004;

    /** (300A,0006) VR=DA, VM=1 RT Plan Date  */
    public static final int RTPlanDate = 0x300A0006;

    /** (300A,0007) VR=TM, VM=1 RT Plan Time  */
    public static final int RTPlanTime = 0x300A0007;

    /** (300A,0009) VR=LO, VM=1-n Treatment Protocols  */
    public static final int TreatmentProtocols = 0x300A0009;

    /** (300A,000A) VR=CS, VM=1 Treatment Intent  */
    public static final int TreatmentIntent = 0x300A000A;

    /** (300A,000B) VR=LO, VM=1-n Treatment Sites  */
    public static final int TreatmentSites = 0x300A000B;

    /** (300A,000C) VR=CS, VM=1 RT Plan Geometry  */
    public static final int RTPlanGeometry = 0x300A000C;

    /** (300A,000E) VR=ST, VM=1 Prescription Description  */
    public static final int PrescriptionDescription = 0x300A000E;

    /** (300A,0010) VR=SQ, VM=1 Dose Reference Sequence  */
    public static final int DoseReferenceSequence = 0x300A0010;

    /** (300A,0012) VR=IS, VM=1 Dose Reference Number  */
    public static final int DoseReferenceNumber = 0x300A0012;

    /** (300A,0013) VR=UI, VM=1 Dose Reference UID  */
    public static final int DoseReferenceUID = 0x300A0013;

    /** (300A,0014) VR=CS, VM=1 Dose Reference Structure Type  */
    public static final int DoseReferenceStructureType = 0x300A0014;

    /** (300A,0015) VR=CS, VM=1 Nominal Beam Energy Unit  */
    public static final int NominalBeamEnergyUnit = 0x300A0015;

    /** (300A,0016) VR=LO, VM=1 Dose Reference Description  */
    public static final int DoseReferenceDescription = 0x300A0016;

    /** (300A,0018) VR=DS, VM=3 Dose Reference Point Coordinates  */
    public static final int DoseReferencePointCoordinates = 0x300A0018;

    /** (300A,001A) VR=DS, VM=1 Nominal Prior Dose  */
    public static final int NominalPriorDose = 0x300A001A;

    /** (300A,0020) VR=CS, VM=1 Dose Reference Type  */
    public static final int DoseReferenceType = 0x300A0020;

    /** (300A,0021) VR=DS, VM=1 Constraint Weight  */
    public static final int ConstraintWeight = 0x300A0021;

    /** (300A,0022) VR=DS, VM=1 Delivery Warning Dose  */
    public static final int DeliveryWarningDose = 0x300A0022;

    /** (300A,0023) VR=DS, VM=1 Delivery Maximum Dose  */
    public static final int DeliveryMaximumDose = 0x300A0023;

    /** (300A,0025) VR=DS, VM=1 Target Minimum Dose  */
    public static final int TargetMinimumDose = 0x300A0025;

    /** (300A,0026) VR=DS, VM=1 Target Prescription Dose  */
    public static final int TargetPrescriptionDose = 0x300A0026;

    /** (300A,0027) VR=DS, VM=1 Target Maximum Dose  */
    public static final int TargetMaximumDose = 0x300A0027;

    /** (300A,0028) VR=DS, VM=1 Target Underdose Volume Fraction  */
    public static final int TargetUnderdoseVolumeFraction = 0x300A0028;

    /** (300A,002A) VR=DS, VM=1 Organ at Risk Full-volume Dose  */
    public static final int OrganatRiskFullvolumeDose = 0x300A002A;

    /** (300A,002B) VR=DS, VM=1 Organ at Risk Limit Dose  */
    public static final int OrganatRiskLimitDose = 0x300A002B;

    /** (300A,002C) VR=DS, VM=1 Organ at Risk Maximum Dose  */
    public static final int OrganatRiskMaximumDose = 0x300A002C;

    /** (300A,002D) VR=DS, VM=1 Organ at Risk Overdose Volume Fraction  */
    public static final int OrganatRiskOverdoseVolumeFraction = 0x300A002D;

    /** (300A,0040) VR=SQ, VM=1 Tolerance Table Sequence  */
    public static final int ToleranceTableSequence = 0x300A0040;

    /** (300A,0042) VR=IS, VM=1 Tolerance Table Number  */
    public static final int ToleranceTableNumber = 0x300A0042;

    /** (300A,0043) VR=SH, VM=1 Tolerance Table Label  */
    public static final int ToleranceTableLabel = 0x300A0043;

    /** (300A,0044) VR=DS, VM=1 Gantry Angle Tolerance  */
    public static final int GantryAngleTolerance = 0x300A0044;

    /** (300A,0046) VR=DS, VM=1 Beam Limiting Device Angle Tolerance  */
    public static final int BeamLimitingDeviceAngleTolerance = 0x300A0046;

    /** (300A,0048) VR=SQ, VM=1 Beam Limiting Device Tolerance Sequence  */
    public static final int BeamLimitingDeviceToleranceSequence = 0x300A0048;

    /** (300A,004A) VR=DS, VM=1 Beam Limiting Device Position Tolerance  */
    public static final int BeamLimitingDevicePositionTolerance = 0x300A004A;

    /** (300A,004C) VR=DS, VM=1 Patient Support Angle Tolerance  */
    public static final int PatientSupportAngleTolerance = 0x300A004C;

    /** (300A,004E) VR=DS, VM=1 Table Top Eccentric Angle Tolerance  */
    public static final int TableTopEccentricAngleTolerance = 0x300A004E;

    /** (300A,0051) VR=DS, VM=1 Table Top Vertical Position Tolerance  */
    public static final int TableTopVerticalPositionTolerance = 0x300A0051;

    /** (300A,0052) VR=DS, VM=1 Table Top Longitudinal Position Tolerance  */
    public static final int TableTopLongitudinalPositionTolerance = 0x300A0052;

    /** (300A,0053) VR=DS, VM=1 Table Top Lateral Position Tolerance  */
    public static final int TableTopLateralPositionTolerance = 0x300A0053;

    /** (300A,0055) VR=CS, VM=1 RT Plan Relationship  */
    public static final int RTPlanRelationship = 0x300A0055;

    /** (300A,0070) VR=SQ, VM=1 Fraction Group Sequence  */
    public static final int FractionGroupSequence = 0x300A0070;

    /** (300A,0071) VR=IS, VM=1 Fraction Group Number  */
    public static final int FractionGroupNumber = 0x300A0071;

    /** (300A,0072) VR=LO, VM=1 Fraction Group Description  */
    public static final int FractionGroupDescription = 0x300A0072;

    /** (300A,0078) VR=IS, VM=1 Number of Fractions Planned  */
    public static final int NumberofFractionsPlanned = 0x300A0078;

    /** (300A,0079) VR=IS, VM=1 Number of Fraction Pattern Digits Per Day  */
    public static final int NumberofFractionPatternDigitsPerDay = 0x300A0079;

    /** (300A,007A) VR=IS, VM=1 Repeat Fraction Cycle Length  */
    public static final int RepeatFractionCycleLength = 0x300A007A;

    /** (300A,007B) VR=LT, VM=1 Fraction Pattern  */
    public static final int FractionPattern = 0x300A007B;

    /** (300A,0080) VR=IS, VM=1 Number of Beams  */
    public static final int NumberofBeams = 0x300A0080;

    /** (300A,0082) VR=DS, VM=3 Beam Dose Specification Point  */
    public static final int BeamDoseSpecificationPoint = 0x300A0082;

    /** (300A,0084) VR=DS, VM=1 Beam Dose  */
    public static final int BeamDose = 0x300A0084;

    /** (300A,0086) VR=DS, VM=1 Beam Meterset  */
    public static final int BeamMeterset = 0x300A0086;

    /** (300A,00A0) VR=IS, VM=1 Number of Brachy Application Setups  */
    public static final int NumberofBrachyApplicationSetups = 0x300A00A0;

    /** (300A,00A2) VR=DS, VM=3 Brachy Application Setup Dose Specification Point  */
    public static final int BrachyApplicationSetupDoseSpecificationPoint = 0x300A00A2;

    /** (300A,00A4) VR=DS, VM=1 Brachy Application Setup Dose  */
    public static final int BrachyApplicationSetupDose = 0x300A00A4;

    /** (300A,00B0) VR=SQ, VM=1 Beam Sequence  */
    public static final int BeamSequence = 0x300A00B0;

    /** (300A,00B2) VR=SH, VM=1 Treatment Machine Name  */
    public static final int TreatmentMachineName = 0x300A00B2;

    /** (300A,00B3) VR=CS, VM=1 Primary Dosimeter Unit  */
    public static final int PrimaryDosimeterUnit = 0x300A00B3;

    /** (300A,00B4) VR=DS, VM=1 Source-Axis Distance  */
    public static final int SourceAxisDistance = 0x300A00B4;

    /** (300A,00B6) VR=SQ, VM=1 Beam Limiting Device Sequence  */
    public static final int BeamLimitingDeviceSequence = 0x300A00B6;

    /** (300A,00B8) VR=CS, VM=1 RT Beam Limiting Device Type  */
    public static final int RTBeamLimitingDeviceType = 0x300A00B8;

    /** (300A,00BA) VR=DS, VM=1 Source to Beam Limiting Device Distance  */
    public static final int SourcetoBeamLimitingDeviceDistance = 0x300A00BA;

    /** (300A,00BC) VR=IS, VM=1 Number of Leaf/Jaw Pairs  */
    public static final int NumberofLeafJawPairs = 0x300A00BC;

    /** (300A,00BE) VR=DS, VM=3-n Leaf Position Boundaries  */
    public static final int LeafPositionBoundaries = 0x300A00BE;

    /** (300A,00C0) VR=IS, VM=1 Beam Number  */
    public static final int BeamNumber = 0x300A00C0;

    /** (300A,00C2) VR=LO, VM=1 Beam Name  */
    public static final int BeamName = 0x300A00C2;

    /** (300A,00C3) VR=ST, VM=1 Beam Description  */
    public static final int BeamDescription = 0x300A00C3;

    /** (300A,00C4) VR=CS, VM=1 Beam Type  */
    public static final int BeamType = 0x300A00C4;

    /** (300A,00C6) VR=CS, VM=1 Radiation Type  */
    public static final int RadiationType = 0x300A00C6;

    /** (300A,00C7) VR=CS, VM=1 High-Dose Technique Type  */
    public static final int HighDoseTechniqueType = 0x300A00C7;

    /** (300A,00C8) VR=IS, VM=1 Reference Image Number  */
    public static final int ReferenceImageNumber = 0x300A00C8;

    /** (300A,00CA) VR=SQ, VM=1 Planned Verification Image Sequence  */
    public static final int PlannedVerificationImageSequence = 0x300A00CA;

    /** (300A,00CC) VR=LO, VM=1-n Imaging Device-Specific Acquisition Parameters  */
    public static final int ImagingDeviceSpecificAcquisitionParameters = 0x300A00CC;

    /** (300A,00CE) VR=CS, VM=1 Treatment Delivery Type  */
    public static final int TreatmentDeliveryType = 0x300A00CE;

    /** (300A,00D0) VR=IS, VM=1 Number of Wedges  */
    public static final int NumberofWedges = 0x300A00D0;

    /** (300A,00D1) VR=SQ, VM=1 Wedge Sequence  */
    public static final int WedgeSequence = 0x300A00D1;

    /** (300A,00D2) VR=IS, VM=1 Wedge Number  */
    public static final int WedgeNumber = 0x300A00D2;

    /** (300A,00D3) VR=CS, VM=1 Wedge Type  */
    public static final int WedgeType = 0x300A00D3;

    /** (300A,00D4) VR=SH, VM=1 Wedge ID  */
    public static final int WedgeID = 0x300A00D4;

    /** (300A,00D5) VR=IS, VM=1 Wedge Angle  */
    public static final int WedgeAngle = 0x300A00D5;

    /** (300A,00D6) VR=DS, VM=1 Wedge Factor  */
    public static final int WedgeFactor = 0x300A00D6;

    /** (300A,00D8) VR=DS, VM=1 Wedge Orientation  */
    public static final int WedgeOrientation = 0x300A00D8;

    /** (300A,00DA) VR=DS, VM=1 Source to Wedge Tray Distance  */
    public static final int SourcetoWedgeTrayDistance = 0x300A00DA;

    /** (300A,00DC) VR=SH, VM=1 Bolus ID  */
    public static final int BolusID = 0x300A00DC;

    /** (300A,00DD) VR=ST, VM=1 Bolus Description  */
    public static final int BolusDescription = 0x300A00DD;

    /** (300A,00E0) VR=IS, VM=1 Number of Compensators  */
    public static final int NumberofCompensators = 0x300A00E0;

    /** (300A,00E1) VR=SH, VM=1 Material ID  */
    public static final int MaterialID = 0x300A00E1;

    /** (300A,00E2) VR=DS, VM=1 Total Compensator Tray Factor  */
    public static final int TotalCompensatorTrayFactor = 0x300A00E2;

    /** (300A,00E3) VR=SQ, VM=1 Compensator Sequence  */
    public static final int CompensatorSequence = 0x300A00E3;

    /** (300A,00E4) VR=IS, VM=1 Compensator Number  */
    public static final int CompensatorNumber = 0x300A00E4;

    /** (300A,00E5) VR=SH, VM=1 Compensator ID  */
    public static final int CompensatorID = 0x300A00E5;

    /** (300A,00E6) VR=DS, VM=1 Source to Compensator Tray Distance  */
    public static final int SourcetoCompensatorTrayDistance = 0x300A00E6;

    /** (300A,00E7) VR=IS, VM=1 Compensator Rows  */
    public static final int CompensatorRows = 0x300A00E7;

    /** (300A,00E8) VR=IS, VM=1 Compensator Columns  */
    public static final int CompensatorColumns = 0x300A00E8;

    /** (300A,00E9) VR=DS, VM=2 Compensator Pixel Spacing  */
    public static final int CompensatorPixelSpacing = 0x300A00E9;

    /** (300A,00EA) VR=DS, VM=2 Compensator Position  */
    public static final int CompensatorPosition = 0x300A00EA;

    /** (300A,00EB) VR=DS, VM=1-n Compensator Transmission Data  */
    public static final int CompensatorTransmissionData = 0x300A00EB;

    /** (300A,00EC) VR=DS, VM=1-n Compensator Thickness Data  */
    public static final int CompensatorThicknessData = 0x300A00EC;

    /** (300A,00ED) VR=IS, VM=1 Number of Boli  */
    public static final int NumberofBoli = 0x300A00ED;

    /** (300A,00EE) VR=CS, VM=1 Compensator Type  */
    public static final int CompensatorType = 0x300A00EE;

    /** (300A,00F0) VR=IS, VM=1 Number of Blocks  */
    public static final int NumberofBlocks = 0x300A00F0;

    /** (300A,00F2) VR=DS, VM=1 Total Block Tray Factor  */
    public static final int TotalBlockTrayFactor = 0x300A00F2;

    /** (300A,00F4) VR=SQ, VM=1 Block Sequence  */
    public static final int BlockSequence = 0x300A00F4;

    /** (300A,00F5) VR=SH, VM=1 Block Tray ID  */
    public static final int BlockTrayID = 0x300A00F5;

    /** (300A,00F6) VR=DS, VM=1 Source to Block Tray Distance  */
    public static final int SourcetoBlockTrayDistance = 0x300A00F6;

    /** (300A,00F8) VR=CS, VM=1 Block Type  */
    public static final int BlockType = 0x300A00F8;

    /** (300A,00F9) VR=LO, VM=1 Accessory Code  */
    public static final int AccessoryCode = 0x300A00F9;

    /** (300A,00FA) VR=CS, VM=1 Block Divergence  */
    public static final int BlockDivergence = 0x300A00FA;

    /** (300A,00FB) VR=CS, VM=1 Block Mounting Position  */
    public static final int BlockMountingPosition = 0x300A00FB;

    /** (300A,00FC) VR=IS, VM=1 Block Number  */
    public static final int BlockNumber = 0x300A00FC;

    /** (300A,00FE) VR=LO, VM=1 Block Name  */
    public static final int BlockName = 0x300A00FE;

    /** (300A,0100) VR=DS, VM=1 Block Thickness  */
    public static final int BlockThickness = 0x300A0100;

    /** (300A,0102) VR=DS, VM=1 Block Transmission  */
    public static final int BlockTransmission = 0x300A0102;

    /** (300A,0104) VR=IS, VM=1 Block Number of Points  */
    public static final int BlockNumberofPoints = 0x300A0104;

    /** (300A,0106) VR=DS, VM=2-2n Block Data  */
    public static final int BlockData = 0x300A0106;

    /** (300A,0107) VR=SQ, VM=1 Applicator Sequence  */
    public static final int ApplicatorSequence = 0x300A0107;

    /** (300A,0108) VR=SH, VM=1 Applicator ID  */
    public static final int ApplicatorID = 0x300A0108;

    /** (300A,0109) VR=CS, VM=1 Applicator Type  */
    public static final int ApplicatorType = 0x300A0109;

    /** (300A,010A) VR=LO, VM=1 Applicator Description  */
    public static final int ApplicatorDescription = 0x300A010A;

    /** (300A,010C) VR=DS, VM=1 Cumulative Dose Reference Coefficient  */
    public static final int CumulativeDoseReferenceCoefficient = 0x300A010C;

    /** (300A,010E) VR=DS, VM=1 Final Cumulative Meterset Weight  */
    public static final int FinalCumulativeMetersetWeight = 0x300A010E;

    /** (300A,0110) VR=IS, VM=1 Number of Control Points  */
    public static final int NumberofControlPoints = 0x300A0110;

    /** (300A,0111) VR=SQ, VM=1 Control Point Sequence  */
    public static final int ControlPointSequence = 0x300A0111;

    /** (300A,0112) VR=IS, VM=1 Control Point Index  */
    public static final int ControlPointIndex = 0x300A0112;

    /** (300A,0114) VR=DS, VM=1 Nominal Beam Energy  */
    public static final int NominalBeamEnergy = 0x300A0114;

    /** (300A,0115) VR=DS, VM=1 Dose Rate Set  */
    public static final int DoseRateSet = 0x300A0115;

    /** (300A,0116) VR=SQ, VM=1 Wedge Position Sequence  */
    public static final int WedgePositionSequence = 0x300A0116;

    /** (300A,0118) VR=CS, VM=1 Wedge Position  */
    public static final int WedgePosition = 0x300A0118;

    /** (300A,011A) VR=SQ, VM=1 Beam Limiting Device Position Sequence  */
    public static final int BeamLimitingDevicePositionSequence = 0x300A011A;

    /** (300A,011C) VR=DS, VM=2-2n Leaf/Jaw Positions  */
    public static final int LeafJawPositions = 0x300A011C;

    /** (300A,011E) VR=DS, VM=1 Gantry Angle  */
    public static final int GantryAngle = 0x300A011E;

    /** (300A,011F) VR=CS, VM=1 Gantry Rotation Direction  */
    public static final int GantryRotationDirection = 0x300A011F;

    /** (300A,0120) VR=DS, VM=1 Beam Limiting Device Angle  */
    public static final int BeamLimitingDeviceAngle = 0x300A0120;

    /** (300A,0121) VR=CS, VM=1 Beam Limiting Device Rotation Direction  */
    public static final int BeamLimitingDeviceRotationDirection = 0x300A0121;

    /** (300A,0122) VR=DS, VM=1 Patient Support Angle  */
    public static final int PatientSupportAngle = 0x300A0122;

    /** (300A,0123) VR=CS, VM=1 Patient Support Rotation Direction  */
    public static final int PatientSupportRotationDirection = 0x300A0123;

    /** (300A,0124) VR=DS, VM=1 Table Top Eccentric Axis Distance  */
    public static final int TableTopEccentricAxisDistance = 0x300A0124;

    /** (300A,0125) VR=DS, VM=1 Table Top Eccentric Angle  */
    public static final int TableTopEccentricAngle = 0x300A0125;

    /** (300A,0126) VR=CS, VM=1 Table Top Eccentric Rotation Direction  */
    public static final int TableTopEccentricRotationDirection = 0x300A0126;

    /** (300A,0128) VR=DS, VM=1 Table Top Vertical Position  */
    public static final int TableTopVerticalPosition = 0x300A0128;

    /** (300A,0129) VR=DS, VM=1 Table Top Longitudinal Position  */
    public static final int TableTopLongitudinalPosition = 0x300A0129;

    /** (300A,012A) VR=DS, VM=1 Table Top Lateral Position  */
    public static final int TableTopLateralPosition = 0x300A012A;

    /** (300A,012C) VR=DS, VM=3 Isocenter Position  */
    public static final int IsocenterPosition = 0x300A012C;

    /** (300A,012E) VR=DS, VM=3 Surface Entry Point  */
    public static final int SurfaceEntryPoint = 0x300A012E;

    /** (300A,0130) VR=DS, VM=1 Source to Surface Distance  */
    public static final int SourcetoSurfaceDistance = 0x300A0130;

    /** (300A,0134) VR=DS, VM=1 Cumulative Meterset Weight  */
    public static final int CumulativeMetersetWeight = 0x300A0134;

    /** (300A,0180) VR=SQ, VM=1 Patient Setup Sequence  */
    public static final int PatientSetupSequence = 0x300A0180;

    /** (300A,0182) VR=IS, VM=1 Patient Setup Number  */
    public static final int PatientSetupNumber = 0x300A0182;

    /** (300A,0183) VR=LO, VM=1 Patient Setup Label  */
    public static final int PatientSetupLabel = 0x300A0183;

    /** (300A,0184) VR=LO, VM=1 Patient Additional Position  */
    public static final int PatientAdditionalPosition = 0x300A0184;

    /** (300A,0190) VR=SQ, VM=1 Fixation Device Sequence  */
    public static final int FixationDeviceSequence = 0x300A0190;

    /** (300A,0192) VR=CS, VM=1 Fixation Device Type  */
    public static final int FixationDeviceType = 0x300A0192;

    /** (300A,0194) VR=SH, VM=1 Fixation Device Label  */
    public static final int FixationDeviceLabel = 0x300A0194;

    /** (300A,0196) VR=ST, VM=1 Fixation Device Description  */
    public static final int FixationDeviceDescription = 0x300A0196;

    /** (300A,0198) VR=SH, VM=1 Fixation Device Position  */
    public static final int FixationDevicePosition = 0x300A0198;

    /** (300A,0199) VR=FL, VM=1 Fixation Device Pitch Angle  */
    public static final int FixationDevicePitchAngle = 0x300A0199;

    /** (300A,01A0) VR=SQ, VM=1 Shielding Device Sequence  */
    public static final int ShieldingDeviceSequence = 0x300A01A0;

    /** (300A,01A2) VR=CS, VM=1 Shielding Device Type  */
    public static final int ShieldingDeviceType = 0x300A01A2;

    /** (300A,01A4) VR=SH, VM=1 Shielding Device Label  */
    public static final int ShieldingDeviceLabel = 0x300A01A4;

    /** (300A,01A6) VR=ST, VM=1 Shielding Device Description  */
    public static final int ShieldingDeviceDescription = 0x300A01A6;

    /** (300A,01A8) VR=SH, VM=1 Shielding Device Position  */
    public static final int ShieldingDevicePosition = 0x300A01A8;

    /** (300A,01B0) VR=CS, VM=1 Setup Technique  */
    public static final int SetupTechnique = 0x300A01B0;

    /** (300A,01B2) VR=ST, VM=1 Setup Technique Description  */
    public static final int SetupTechniqueDescription = 0x300A01B2;

    /** (300A,01B4) VR=SQ, VM=1 Setup Device Sequence  */
    public static final int SetupDeviceSequence = 0x300A01B4;

    /** (300A,01B6) VR=CS, VM=1 Setup Device Type  */
    public static final int SetupDeviceType = 0x300A01B6;

    /** (300A,01B8) VR=SH, VM=1 Setup Device Label  */
    public static final int SetupDeviceLabel = 0x300A01B8;

    /** (300A,01BA) VR=ST, VM=1 Setup Device Description  */
    public static final int SetupDeviceDescription = 0x300A01BA;

    /** (300A,01BC) VR=DS, VM=1 Setup Device Parameter  */
    public static final int SetupDeviceParameter = 0x300A01BC;

    /** (300A,01D0) VR=ST, VM=1 Setup Reference Description  */
    public static final int SetupReferenceDescription = 0x300A01D0;

    /** (300A,01D2) VR=DS, VM=1 Table Top Vertical Setup Displacement  */
    public static final int TableTopVerticalSetupDisplacement = 0x300A01D2;

    /** (300A,01D4) VR=DS, VM=1 Table Top Longitudinal Setup Displacement  */
    public static final int TableTopLongitudinalSetupDisplacement = 0x300A01D4;

    /** (300A,01D6) VR=DS, VM=1 Table Top Lateral Setup Displacement  */
    public static final int TableTopLateralSetupDisplacement = 0x300A01D6;

    /** (300A,0200) VR=CS, VM=1 Brachy Treatment Technique  */
    public static final int BrachyTreatmentTechnique = 0x300A0200;

    /** (300A,0202) VR=CS, VM=1 Brachy Treatment Type  */
    public static final int BrachyTreatmentType = 0x300A0202;

    /** (300A,0206) VR=SQ, VM=1 Treatment Machine Sequence  */
    public static final int TreatmentMachineSequence = 0x300A0206;

    /** (300A,0210) VR=SQ, VM=1 Source Sequence  */
    public static final int SourceSequence = 0x300A0210;

    /** (300A,0212) VR=IS, VM=1 Source Number  */
    public static final int SourceNumber = 0x300A0212;

    /** (300A,0214) VR=CS, VM=1 Source Type  */
    public static final int SourceType = 0x300A0214;

    /** (300A,0216) VR=LO, VM=1 Source Manufacturer  */
    public static final int SourceManufacturer = 0x300A0216;

    /** (300A,0218) VR=DS, VM=1 Active Source Diameter  */
    public static final int ActiveSourceDiameter = 0x300A0218;

    /** (300A,021A) VR=DS, VM=1 Active Source Length  */
    public static final int ActiveSourceLength = 0x300A021A;

    /** (300A,0222) VR=DS, VM=1 Source Encapsulation Nominal Thickness  */
    public static final int SourceEncapsulationNominalThickness = 0x300A0222;

    /** (300A,0224) VR=DS, VM=1 Source Encapsulation Nominal Transmission  */
    public static final int SourceEncapsulationNominalTransmission = 0x300A0224;

    /** (300A,0226) VR=LO, VM=1 Source Isotope Name  */
    public static final int SourceIsotopeName = 0x300A0226;

    /** (300A,0228) VR=DS, VM=1 Source Isotope Half Life  */
    public static final int SourceIsotopeHalfLife = 0x300A0228;

    /** (300A,022A) VR=DS, VM=1 Reference Air Kerma Rate  */
    public static final int ReferenceAirKermaRate = 0x300A022A;

    /** (300A,022C) VR=DA, VM=1 Air Kerma Rate Reference Date  */
    public static final int AirKermaRateReferenceDate = 0x300A022C;

    /** (300A,022E) VR=TM, VM=1 Air Kerma Rate Reference Time  */
    public static final int AirKermaRateReferenceTime = 0x300A022E;

    /** (300A,0230) VR=SQ, VM=1 Application Setup Sequence  */
    public static final int ApplicationSetupSequence = 0x300A0230;

    /** (300A,0232) VR=CS, VM=1 Application Setup Type  */
    public static final int ApplicationSetupType = 0x300A0232;

    /** (300A,0234) VR=IS, VM=1 Application Setup Number  */
    public static final int ApplicationSetupNumber = 0x300A0234;

    /** (300A,0236) VR=LO, VM=1 Application Setup Name  */
    public static final int ApplicationSetupName = 0x300A0236;

    /** (300A,0238) VR=LO, VM=1 Application Setup Manufacturer  */
    public static final int ApplicationSetupManufacturer = 0x300A0238;

    /** (300A,0240) VR=IS, VM=1 Template Number  */
    public static final int TemplateNumber = 0x300A0240;

    /** (300A,0242) VR=SH, VM=1 Template Type  */
    public static final int TemplateType = 0x300A0242;

    /** (300A,0244) VR=LO, VM=1 Template Name  */
    public static final int TemplateName = 0x300A0244;

    /** (300A,0250) VR=DS, VM=1 Total Reference Air Kerma  */
    public static final int TotalReferenceAirKerma = 0x300A0250;

    /** (300A,0260) VR=SQ, VM=1 Brachy Accessory Device Sequence  */
    public static final int BrachyAccessoryDeviceSequence = 0x300A0260;

    /** (300A,0262) VR=IS, VM=1 Brachy Accessory Device Number  */
    public static final int BrachyAccessoryDeviceNumber = 0x300A0262;

    /** (300A,0263) VR=SH, VM=1 Brachy Accessory Device ID  */
    public static final int BrachyAccessoryDeviceID = 0x300A0263;

    /** (300A,0264) VR=CS, VM=1 Brachy Accessory Device Type  */
    public static final int BrachyAccessoryDeviceType = 0x300A0264;

    /** (300A,0266) VR=LO, VM=1 Brachy Accessory Device Name  */
    public static final int BrachyAccessoryDeviceName = 0x300A0266;

    /** (300A,026A) VR=DS, VM=1 Brachy Accessory Device Nominal Thickness  */
    public static final int BrachyAccessoryDeviceNominalThickness = 0x300A026A;

    /** (300A,026C) VR=DS, VM=1 Brachy Accessory Device Nominal Transmission  */
    public static final int BrachyAccessoryDeviceNominalTransmission = 0x300A026C;

    /** (300A,0280) VR=SQ, VM=1 Channel Sequence  */
    public static final int ChannelSequence = 0x300A0280;

    /** (300A,0282) VR=IS, VM=1 Channel Number  */
    public static final int ChannelNumber = 0x300A0282;

    /** (300A,0284) VR=DS, VM=1 Channel Length  */
    public static final int ChannelLength = 0x300A0284;

    /** (300A,0286) VR=DS, VM=1 Channel Total Time  */
    public static final int ChannelTotalTime = 0x300A0286;

    /** (300A,0288) VR=CS, VM=1 Source Movement Type  */
    public static final int SourceMovementType = 0x300A0288;

    /** (300A,028A) VR=IS, VM=1 Number of Pulses  */
    public static final int NumberofPulses = 0x300A028A;

    /** (300A,028C) VR=DS, VM=1 Pulse Repetition Interval  */
    public static final int PulseRepetitionInterval = 0x300A028C;

    /** (300A,0290) VR=IS, VM=1 Source Applicator Number  */
    public static final int SourceApplicatorNumber = 0x300A0290;

    /** (300A,0291) VR=SH, VM=1 Source Applicator ID  */
    public static final int SourceApplicatorID = 0x300A0291;

    /** (300A,0292) VR=CS, VM=1 Source Applicator Type  */
    public static final int SourceApplicatorType = 0x300A0292;

    /** (300A,0294) VR=LO, VM=1 Source Applicator Name  */
    public static final int SourceApplicatorName = 0x300A0294;

    /** (300A,0296) VR=DS, VM=1 Source Applicator Length  */
    public static final int SourceApplicatorLength = 0x300A0296;

    /** (300A,0298) VR=LO, VM=1 Source Applicator Manufacturer  */
    public static final int SourceApplicatorManufacturer = 0x300A0298;

    /** (300A,029C) VR=DS, VM=1 Source Applicator Wall Nominal Thickness  */
    public static final int SourceApplicatorWallNominalThickness = 0x300A029C;

    /** (300A,029E) VR=DS, VM=1 Source Applicator Wall Nominal Transmission  */
    public static final int SourceApplicatorWallNominalTransmission = 0x300A029E;

    /** (300A,02A0) VR=DS, VM=1 Source Applicator Step Size  */
    public static final int SourceApplicatorStepSize = 0x300A02A0;

    /** (300A,02A2) VR=IS, VM=1 Transfer Tube Number  */
    public static final int TransferTubeNumber = 0x300A02A2;

    /** (300A,02A4) VR=DS, VM=1 Transfer Tube Length  */
    public static final int TransferTubeLength = 0x300A02A4;

    /** (300A,02B0) VR=SQ, VM=1 Channel Shield Sequence  */
    public static final int ChannelShieldSequence = 0x300A02B0;

    /** (300A,02B2) VR=IS, VM=1 Channel Shield Number  */
    public static final int ChannelShieldNumber = 0x300A02B2;

    /** (300A,02B3) VR=SH, VM=1 Channel Shield ID  */
    public static final int ChannelShieldID = 0x300A02B3;

    /** (300A,02B4) VR=LO, VM=1 Channel Shield Name  */
    public static final int ChannelShieldName = 0x300A02B4;

    /** (300A,02B8) VR=DS, VM=1 Channel Shield Nominal Thickness  */
    public static final int ChannelShieldNominalThickness = 0x300A02B8;

    /** (300A,02BA) VR=DS, VM=1 Channel Shield Nominal Transmission  */
    public static final int ChannelShieldNominalTransmission = 0x300A02BA;

    /** (300A,02C8) VR=DS, VM=1 Final Cumulative Time Weight  */
    public static final int FinalCumulativeTimeWeight = 0x300A02C8;

    /** (300A,02D0) VR=SQ, VM=1 Brachy Control Point Sequence  */
    public static final int BrachyControlPointSequence = 0x300A02D0;

    /** (300A,02D2) VR=DS, VM=1 Control Point Relative Position  */
    public static final int ControlPointRelativePosition = 0x300A02D2;

    /** (300A,02D4) VR=DS, VM=3 Control Point 3D Position  */
    public static final int ControlPoint3DPosition = 0x300A02D4;

    /** (300A,02D6) VR=DS, VM=1 Cumulative Time Weight  */
    public static final int CumulativeTimeWeight = 0x300A02D6;

    /** (300A,02E0) VR=CS, VM=1 Compensator Divergence  */
    public static final int CompensatorDivergence = 0x300A02E0;

    /** (300A,02E1) VR=CS, VM=1 Compensator Mounting Position  */
    public static final int CompensatorMountingPosition = 0x300A02E1;

    /** (300A,02E2) VR=DS, VM=1-n Source to Compensator Distance  */
    public static final int SourcetoCompensatorDistance = 0x300A02E2;

    /** (300A,019A) VR=FL, VM=1 Fixation Device Roll Angle  */
    public static final int FixationDeviceRollAngle = 0x300A019A;

    /** (300C,0002) VR=SQ, VM=1 Referenced RT Plan Sequence  */
    public static final int ReferencedRTPlanSequence = 0x300C0002;

    /** (300C,0004) VR=SQ, VM=1 Referenced Beam Sequence  */
    public static final int ReferencedBeamSequence = 0x300C0004;

    /** (300C,0006) VR=IS, VM=1 Referenced Beam Number  */
    public static final int ReferencedBeamNumber = 0x300C0006;

    /** (300C,0007) VR=IS, VM=1 Referenced Reference Image Number  */
    public static final int ReferencedReferenceImageNumber = 0x300C0007;

    /** (300C,0008) VR=DS, VM=1 Start Cumulative Meterset Weight  */
    public static final int StartCumulativeMetersetWeight = 0x300C0008;

    /** (300C,0009) VR=DS, VM=1 End Cumulative Meterset Weight  */
    public static final int EndCumulativeMetersetWeight = 0x300C0009;

    /** (300C,000A) VR=SQ, VM=1 Referenced Brachy Application Setup Sequence  */
    public static final int ReferencedBrachyApplicationSetupSequence = 0x300C000A;

    /** (300C,000C) VR=IS, VM=1 Referenced Brachy Application Setup Number  */
    public static final int ReferencedBrachyApplicationSetupNumber = 0x300C000C;

    /** (300C,000E) VR=IS, VM=1 Referenced Source Number  */
    public static final int ReferencedSourceNumber = 0x300C000E;

    /** (300C,0020) VR=SQ, VM=1 Referenced Fraction Group Sequence  */
    public static final int ReferencedFractionGroupSequence = 0x300C0020;

    /** (300C,0022) VR=IS, VM=1 Referenced Fraction Group Number  */
    public static final int ReferencedFractionGroupNumber = 0x300C0022;

    /** (300C,0040) VR=SQ, VM=1 Referenced Verification Image Sequence  */
    public static final int ReferencedVerificationImageSequence = 0x300C0040;

    /** (300C,0042) VR=SQ, VM=1 Referenced Reference Image Sequence  */
    public static final int ReferencedReferenceImageSequence = 0x300C0042;

    /** (300C,0050) VR=SQ, VM=1 Referenced Dose Reference Sequence  */
    public static final int ReferencedDoseReferenceSequence = 0x300C0050;

    /** (300C,0051) VR=IS, VM=1 Referenced Dose Reference Number  */
    public static final int ReferencedDoseReferenceNumber = 0x300C0051;

    /** (300C,0055) VR=SQ, VM=1 Brachy Referenced Dose Reference Sequence  */
    public static final int BrachyReferencedDoseReferenceSequence = 0x300C0055;

    /** (300C,0060) VR=SQ, VM=1 Referenced Structure Set Sequence  */
    public static final int ReferencedStructureSetSequence = 0x300C0060;

    /** (300C,006A) VR=IS, VM=1 Referenced Patient Setup Number  */
    public static final int ReferencedPatientSetupNumber = 0x300C006A;

    /** (300C,0080) VR=SQ, VM=1 Referenced Dose Sequence  */
    public static final int ReferencedDoseSequence = 0x300C0080;

    /** (300C,00A0) VR=IS, VM=1 Referenced Tolerance Table Number  */
    public static final int ReferencedToleranceTableNumber = 0x300C00A0;

    /** (300C,00B0) VR=SQ, VM=1 Referenced Bolus Sequence  */
    public static final int ReferencedBolusSequence = 0x300C00B0;

    /** (300C,00C0) VR=IS, VM=1 Referenced Wedge Number  */
    public static final int ReferencedWedgeNumber = 0x300C00C0;

    /** (300C,00D0) VR=IS, VM=1 Referenced Compensator Number  */
    public static final int ReferencedCompensatorNumber = 0x300C00D0;

    /** (300C,00E0) VR=IS, VM=1 Referenced Block Number  */
    public static final int ReferencedBlockNumber = 0x300C00E0;

    /** (300C,00F0) VR=IS, VM=1 Referenced Control Point Index  */
    public static final int ReferencedControlPointIndex = 0x300C00F0;

    /** (300E,0002) VR=CS, VM=1 Approval Status  */
    public static final int ApprovalStatus = 0x300E0002;

    /** (300E,0004) VR=DA, VM=1 Review Date  */
    public static final int ReviewDate = 0x300E0004;

    /** (300E,0005) VR=TM, VM=1 Review Time  */
    public static final int ReviewTime = 0x300E0005;

    /** (300E,0008) VR=PN, VM=1 Reviewer Name  */
    public static final int ReviewerName = 0x300E0008;

    /** (4000,0010) VR=, VM= Arbitrary RET */
    public static final int ArbitraryRET = 0x40000010;

    /** (4000,4000) VR=, VM= Comments RET */
    public static final int CommentsRET40004000 = 0x40004000;

    /** (4008,0040) VR=SH, VM=1 Results ID  */
    public static final int ResultsID = 0x40080040;

    /** (4008,0042) VR=LO, VM=1 Results ID Issuer  */
    public static final int ResultsIDIssuer = 0x40080042;

    /** (4008,0050) VR=SQ, VM=1 Referenced Interpretation Sequence  */
    public static final int ReferencedInterpretationSequence = 0x40080050;

    /** (4008,0100) VR=DA, VM=1 Interpretation Recorded Date  */
    public static final int InterpretationRecordedDate = 0x40080100;

    /** (4008,0101) VR=TM, VM=1 Interpretation Recorded Time  */
    public static final int InterpretationRecordedTime = 0x40080101;

    /** (4008,0102) VR=PN, VM=1 Interpretation Recorder  */
    public static final int InterpretationRecorder = 0x40080102;

    /** (4008,0103) VR=LO, VM=1 Reference to Recorded Sound  */
    public static final int ReferencetoRecordedSound = 0x40080103;

    /** (4008,0108) VR=DA, VM=1 Interpretation Transcription Date  */
    public static final int InterpretationTranscriptionDate = 0x40080108;

    /** (4008,0109) VR=TM, VM=1 Interpretation Transcription Time  */
    public static final int InterpretationTranscriptionTime = 0x40080109;

    /** (4008,010A) VR=PN, VM=1 Interpretation Transcriber  */
    public static final int InterpretationTranscriber = 0x4008010A;

    /** (4008,010B) VR=ST, VM=1 Interpretation Text  */
    public static final int InterpretationText = 0x4008010B;

    /** (4008,010C) VR=PN, VM=1 Interpretation Author  */
    public static final int InterpretationAuthor = 0x4008010C;

    /** (4008,0111) VR=SQ, VM=1 Interpretation Approver Sequence  */
    public static final int InterpretationApproverSequence = 0x40080111;

    /** (4008,0112) VR=DA, VM=1 Interpretation Approval Date  */
    public static final int InterpretationApprovalDate = 0x40080112;

    /** (4008,0113) VR=TM, VM=1 Interpretation Approval Time  */
    public static final int InterpretationApprovalTime = 0x40080113;

    /** (4008,0114) VR=PN, VM=1 Physician Approving Interpretation  */
    public static final int PhysicianApprovingInterpretation = 0x40080114;

    /** (4008,0115) VR=LT, VM=1 Interpretation Diagnosis Description  */
    public static final int InterpretationDiagnosisDescription = 0x40080115;

    /** (4008,0117) VR=SQ, VM=1 Interpretation Diagnosis Code Sequence  */
    public static final int InterpretationDiagnosisCodeSequence = 0x40080117;

    /** (4008,0118) VR=SQ, VM=1 Results Distribution List Sequence  */
    public static final int ResultsDistributionListSequence = 0x40080118;

    /** (4008,0119) VR=PN, VM=1 Distribution Name  */
    public static final int DistributionName = 0x40080119;

    /** (4008,011A) VR=LO, VM=1 Distribution Address  */
    public static final int DistributionAddress = 0x4008011A;

    /** (4008,0200) VR=SH, VM=1 Interpretation ID  */
    public static final int InterpretationID = 0x40080200;

    /** (4008,0202) VR=LO, VM=1 Interpretation ID Issuer  */
    public static final int InterpretationIDIssuer = 0x40080202;

    /** (4008,0210) VR=CS, VM=1 Interpretation Type ID  */
    public static final int InterpretationTypeID = 0x40080210;

    /** (4008,0212) VR=CS, VM=1 Interpretation Status ID  */
    public static final int InterpretationStatusID = 0x40080212;

    /** (4008,0300) VR=ST, VM=1 Impressions  */
    public static final int Impressions = 0x40080300;

    /** (4008,4000) VR=ST, VM=1 Results Comments  */
    public static final int ResultsComments = 0x40084000;

    /** (4FFE,0001) VR=SQ, VM=1 MAC Parameters Sequence  */
    public static final int MACParametersSequence = 0x4FFE0001;

    /** (50xx,0005) VR=US, VM=1 Curve Dimensions  */
    public static final int CurveDimensions = 0x50000005;

    /** (50xx,0010) VR=US, VM=1 Number of Points  */
    public static final int NumberofPoints = 0x50000010;

    /** (50xx,0020) VR=CS, VM=1 Type of Data  */
    public static final int TypeofData = 0x50000020;

    /** (50xx,0022) VR=LO, VM=1 Curve Description  */
    public static final int CurveDescription = 0x50000022;

    /** (50xx,0030) VR=SH, VM=1-n Axis Units  */
    public static final int AxisUnits = 0x50000030;

    /** (50xx,0040) VR=SH, VM=1-n Axis Labels  */
    public static final int AxisLabels = 0x50000040;

    /** (50xx,0103) VR=US, VM=1 Data Value Representation  */
    public static final int DataValueRepresentation = 0x50000103;

    /** (50xx,0104) VR=US, VM=1-n Minimum Coordinate Value  */
    public static final int MinimumCoordinateValue = 0x50000104;

    /** (50xx,0105) VR=US, VM=1-n Maximum Coordinate Value  */
    public static final int MaximumCoordinateValue = 0x50000105;

    /** (50xx,0106) VR=SH, VM=1-n Curve Range  */
    public static final int CurveRange = 0x50000106;

    /** (50xx,0110) VR=US, VM=1-n Curve Data Descriptor  */
    public static final int CurveDataDescriptor = 0x50000110;

    /** (50xx,0112) VR=US, VM=1-n Coordinate Start Value  */
    public static final int CoordinateStartValue = 0x50000112;

    /** (50xx,0114) VR=US, VM=1-n Coordinate Step Value  */
    public static final int CoordinateStepValue = 0x50000114;

    /** (50xx,1001) VR=CS, VM=1 Curve Activation Layer  */
    public static final int CurveActivationLayer = 0x50001001;

    /** (50xx,2000) VR=US, VM=1 Audio Type  */
    public static final int AudioType = 0x50002000;

    /** (50xx,2002) VR=US, VM=1 Audio Sample Format  */
    public static final int AudioSampleFormat = 0x50002002;

    /** (50xx,2004) VR=US, VM=1 Number of Channels  */
    public static final int NumberofChannels = 0x50002004;

    /** (50xx,2006) VR=UL, VM=1 Number of Samples  */
    public static final int NumberofSamples = 0x50002006;

    /** (50xx,2008) VR=UL, VM=1 Sample Rate  */
    public static final int SampleRate = 0x50002008;

    /** (50xx,200A) VR=UL, VM=1 Total Time  */
    public static final int TotalTime = 0x5000200A;

    /** (50xx,200C) VR=OW|OB, VM=1 Audio Sample Data  */
    public static final int AudioSampleData = 0x5000200C;

    /** (50xx,200E) VR=LT, VM=1 Audio Comments  */
    public static final int AudioComments = 0x5000200E;

    /** (50xx,2500) VR=LO, VM=1 Curve Label  */
    public static final int CurveLabel = 0x50002500;

    /** (50xx,2600) VR=SQ, VM=1 Referenced Overlay Sequence  */
    public static final int ReferencedOverlaySequence50xx2600 = 0x50002600;

    /** (50xx,2610) VR=US, VM=1 Referenced Overlay Group  */
    public static final int ReferencedOverlayGroup = 0x50002610;

    /** (50xx,3000) VR=OW|OB, VM=1 Curve Data  */
    public static final int CurveData = 0x50003000;

    /** (5200,9229) VR=SQ, VM=1 Shared Functional Groups Sequence  */
    public static final int SharedFunctionalGroupsSequence = 0x52009229;

    /** (5200,9230) VR=SQ, VM=1 Per-frame Functional Groups Sequence  */
    public static final int PerframeFunctionalGroupsSequence = 0x52009230;

    /** (5400,0100) VR=SQ, VM=1 Waveform Sequence  */
    public static final int WaveformSequence = 0x54000100;

    /** (5400,0110) VR=OB|OW, VM=1 Channel Minimum Value  */
    public static final int ChannelMinimumValue = 0x54000110;

    /** (5400,0112) VR=OB|OW, VM=1 Channel Maximum Value  */
    public static final int ChannelMaximumValue = 0x54000112;

    /** (5400,1004) VR=US, VM=1 Waveform Bits Allocated  */
    public static final int WaveformBitsAllocated = 0x54001004;

    /** (5400,1006) VR=CS, VM=1 Waveform Sample Interpretation  */
    public static final int WaveformSampleInterpretation = 0x54001006;

    /** (5400,100A) VR=OB|OW, VM=1 Waveform Padding Value  */
    public static final int WaveformPaddingValue = 0x5400100A;

    /** (5400,1010) VR=OB|OW, VM=1 Waveform Data  */
    public static final int WaveformData = 0x54001010;

    /** (5600,0010) VR=OF, VM=1 First Order Phase Correction Angle  */
    public static final int FirstOrderPhaseCorrectionAngle = 0x56000010;

    /** (5600,0020) VR=OF, VM=1 Spectroscopy Data  */
    public static final int SpectroscopyData = 0x56000020;

    /** (60xx,0010) VR=US, VM=1 Overlay Rows  */
    public static final int OverlayRows = 0x60000010;

    /** (60xx,0011) VR=US, VM=1 Overlay Columns  */
    public static final int OverlayColumns = 0x60000011;

    /** (60xx,0012) VR=US, VM=1 Overlay Planes  */
    public static final int OverlayPlanes = 0x60000012;

    /** (60xx,0015) VR=IS, VM=1 Number of Frames in Overlay  */
    public static final int NumberofFramesinOverlay = 0x60000015;

    /** (60xx,0022) VR=LO, VM=1 Overlay Description  */
    public static final int OverlayDescription = 0x60000022;

    /** (60xx,0040) VR=CS, VM=1 Overlay Type  */
    public static final int OverlayType = 0x60000040;

    /** (60xx,0045) VR=LO, VM=1 Overlay Subtype  */
    public static final int OverlaySubtype = 0x60000045;

    /** (60xx,0050) VR=SS, VM=2 Overlay Origin  */
    public static final int OverlayOrigin = 0x60000050;

    /** (60xx,0051) VR=US, VM=1 Image Frame Origin  */
    public static final int ImageFrameOrigin = 0x60000051;

    /** (60xx,0052) VR=US, VM=1 Overlay Plane Origin  */
    public static final int OverlayPlaneOrigin = 0x60000052;

    /** (60xx,0060) VR=, VM= Compression Code RET */
    public static final int CompressionCodeRET60xx0060 = 0x60000060;

    /** (60xx,0100) VR=US, VM=1 Overlay Bits Allocated  */
    public static final int OverlayBitsAllocated = 0x60000100;

    /** (60xx,0102) VR=US, VM=1 Overlay Bit Position  */
    public static final int OverlayBitPosition = 0x60000102;

    /** (60xx,0110) VR=, VM= Overlay Format RET */
    public static final int OverlayFormatRET = 0x60000110;

    /** (60xx,0200) VR=, VM= Overlay Location RET */
    public static final int OverlayLocationRET = 0x60000200;

    /** (60xx,1001) VR=CS, VM=1 Overlay Activation Layer  */
    public static final int OverlayActivationLayer = 0x60001001;

    /** (60xx,1100) VR=US, VM=1 Overlay Descriptor - Gray RET */
    public static final int OverlayDescriptorGrayRET = 0x60001100;

    /** (60xx,1101) VR=US, VM=1 Overlay Descriptor - Red RET */
    public static final int OverlayDescriptorRedRET = 0x60001101;

    /** (60xx,1102) VR=US, VM=1 Overlay Descriptor - Green RET */
    public static final int OverlayDescriptorGreenRET = 0x60001102;

    /** (60xx,1103) VR=US, VM=1 Overlay Descriptor - Blue RET */
    public static final int OverlayDescriptorBlueRET = 0x60001103;

    /** (60xx,1200) VR=US, VM=1-n Overlays- Gray RET */
    public static final int OverlaysGrayRET = 0x60001200;

    /** (60xx,1201) VR=US, VM=1-n Overlays - Red RET */
    public static final int OverlaysRedRET = 0x60001201;

    /** (60xx,1202) VR=US, VM=1-n Overlays - Green RET */
    public static final int OverlaysGreenRET = 0x60001202;

    /** (60xx,1203) VR=US, VM=1-n Overlays- Blue RET */
    public static final int OverlaysBlueRET = 0x60001203;

    /** (60xx,1301) VR=IS, VM=1 ROI Area  */
    public static final int ROIArea = 0x60001301;

    /** (60xx,1302) VR=DS, VM=1 ROI Mean  */
    public static final int ROIMean = 0x60001302;

    /** (60xx,1303) VR=DS, VM=1 ROI Standard Deviation  */
    public static final int ROIStandardDeviation = 0x60001303;

    /** (60xx,1500) VR=LO, VM=1 Overlay Label  */
    public static final int OverlayLabel = 0x60001500;

    /** (60xx,3000) VR=OB|OW, VM=1 Overlay Data  */
    public static final int OverlayData = 0x60003000;

    /** (60xx,4000) VR=, VM= Comments RET */
    public static final int CommentsRET60xx4000 = 0x60004000;

    /** (7FE0,0010) VR=OW|OB, VM=1 Pixel Data  */
    public static final int PixelData = 0x7FE00010;

    /** (FFFA,FFFA) VR=SQ, VM=1 Digital Signatures Sequence  */
    public static final int DigitalSignaturesSequence = 0xFFFAFFFA;

    /** (FFFC,FFFC) VR=OB, VM=1 Data Set Trailing Padding  */
    public static final int DataSetTrailingPadding = 0xFFFCFFFC;

    /** (FFFE,E000) VR=, VM=1 Item  */
    public static final int Item = 0xFFFEE000;

    /** (FFFE,E00D) VR=, VM=1 Item Delimitation Item  */
    public static final int ItemDelimitationItem = 0xFFFEE00D;

    /** (FFFE,E0DD) VR=, VM=1 Sequence Delimitation Item  */
    public static final int SequenceDelimitationItem = 0xFFFEE0DD;

}