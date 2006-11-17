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

import java.util.StringTokenizer;

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
          throw new IllegalArgumentException("Unknown Tag Name: " + name);
       }
    }

    public static int toTag(String s) {
        try {
            return (int) Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return Tag.forName(s);
        }
    }
    
    public static int[] toTagPath(String expr) {
        StringTokenizer stk = new StringTokenizer(expr, "/[]", true);
        int[] tagPath = new int[stk.countTokens()];
        int i= 0;
        char delim = '/';
        while (stk.hasMoreTokens()) {
            String s = stk.nextToken();
            char ch0 = s.charAt(0);
            switch (ch0) {
            case '/':
                if (delim == '/') {
                    tagPath[i] = 0;
                    i++;
                }
            case '[':
            case ']':
                delim = ch0;              
                break;
            default:
                tagPath[i] = (delim == '[') ? Integer.parseInt(s)-1 : toTag(s);
                ++i;
                break;
            }
        }
        if (i < tagPath.length) {
            int[] tmp = new int[i];
            System.arraycopy(tagPath, 0, tmp, 0, i);
            tagPath = tmp;
        }
        return tagPath;
    }
        
    /** (0000,0000) VR=UL, VM=1 Group Length  */
    public static final int GROUP_LENGTH_00000000 = 0x00000000;
        
    /** (0000,0001) VR=UL, VM=1 Length to End RET */
    public static final int LENGTH_TO_END_00000001_RET = 0x00000001;
        
    /** (0000,0002) VR=UI, VM=1 Affected SOP Class UID  */
    public static final int AFFECTED_SOP_CLASS_UID = 0x00000002;
        
    /** (0000,0003) VR=UI, VM=1 Requested SOP Class UID  */
    public static final int REQUESTED_SOP_CLASS_UID = 0x00000003;
        
    /** (0000,0010) VR=CS, VM=1 Recognition Code RET */
    public static final int RECOGNITION_CODE_00000010_RET = 0x00000010;
        
    /** (0000,0100) VR=US, VM=1 Command Field  */
    public static final int COMMAND_FIELD = 0x00000100;
        
    /** (0000,0110) VR=US, VM=1 Message ID  */
    public static final int MESSAGE_ID = 0x00000110;
        
    /** (0000,0120) VR=US, VM=1 Message ID Being Responded To  */
    public static final int MESSAGE_ID_BEING_RESPONDED_TO = 0x00000120;
        
    /** (0000,0200) VR=AE, VM=1 Initiator RET */
    public static final int INITIATOR_RET = 0x00000200;
        
    /** (0000,0300) VR=AE, VM=1 Receiver RET */
    public static final int RECEIVER_RET = 0x00000300;
        
    /** (0000,0400) VR=AE, VM=1 Find Location RET */
    public static final int FIND_LOCATION_RET = 0x00000400;
        
    /** (0000,0600) VR=AE, VM=1 Move Destination  */
    public static final int MOVE_DESTINATION = 0x00000600;
        
    /** (0000,0700) VR=US, VM=1 Priority  */
    public static final int PRIORITY = 0x00000700;
        
    /** (0000,0800) VR=US, VM=1 Data Set Type  */
    public static final int DATA_SET_TYPE = 0x00000800;
        
    /** (0000,0850) VR=US, VM=1 Number of Matches RET */
    public static final int NUMBER_OF_MATCHES_RET = 0x00000850;
        
    /** (0000,0860) VR=US, VM=1 Response Sequence Number RET */
    public static final int RESPONSE_SEQUENCE_NUMBER_RET = 0x00000860;
        
    /** (0000,0900) VR=US, VM=1 Status  */
    public static final int STATUS = 0x00000900;
        
    /** (0000,0901) VR=AT, VM=1-n Offending Element  */
    public static final int OFFENDING_ELEMENT = 0x00000901;
        
    /** (0000,0902) VR=LO, VM=1 Error Comment  */
    public static final int ERROR_COMMENT = 0x00000902;
        
    /** (0000,0903) VR=US, VM=1 Error ID  */
    public static final int ERROR_ID = 0x00000903;
        
    /** (0000,1000) VR=UI, VM=1 Affected SOP Instance UID  */
    public static final int AFFECTED_SOP_INSTANCE_UID = 0x00001000;
        
    /** (0000,1001) VR=UI, VM=1 Requested SOP Instance UID  */
    public static final int REQUESTED_SOP_INSTANCE_UID = 0x00001001;
        
    /** (0000,1002) VR=US, VM=1 Event Type ID  */
    public static final int EVENT_TYPE_ID = 0x00001002;
        
    /** (0000,1005) VR=AT, VM=1-n Attribute Identifier List  */
    public static final int ATTRIBUTE_IDENTIFIER_LIST = 0x00001005;
        
    /** (0000,1008) VR=US, VM=1 Action Type ID  */
    public static final int ACTION_TYPE_ID = 0x00001008;
        
    /** (0000,1020) VR=US, VM=1 Number of Remaining Sub-operations  */
    public static final int NUMBER_OF_REMAINING_SUB_OPERATIONS = 0x00001020;
        
    /** (0000,1021) VR=US, VM=1 Number of Completed Sub-operations  */
    public static final int NUMBER_OF_COMPLETED_SUB_OPERATIONS = 0x00001021;
        
    /** (0000,1022) VR=US, VM=1 Number of Failed Sub-operations  */
    public static final int NUMBER_OF_FAILED_SUB_OPERATIONS = 0x00001022;
        
    /** (0000,1023) VR=US, VM=1 Number of Warning Sub-operations  */
    public static final int NUMBER_OF_WARNING_SUB_OPERATIONS = 0x00001023;
        
    /** (0000,1030) VR=AE, VM=1 Move Originator Application Entity Title  */
    public static final int MOVE_ORIGINATOR_APPLICATION_ENTITY_TITLE = 0x00001030;
        
    /** (0000,1031) VR=US, VM=1 Move Originator Message ID  */
    public static final int MOVE_ORIGINATOR_MESSAGE_ID = 0x00001031;
        
    /** (0000,4000) VR=AT, VM=1 DIALOG Receiver RET */
    public static final int DIALOG_RECEIVER_RET = 0x00004000;
        
    /** (0000,4010) VR=AT, VM=1 Terminal Type RET */
    public static final int TERMINAL_TYPE_RET = 0x00004010;
        
    /** (0000,5010) VR=SH, VM=1 Message Set ID RET */
    public static final int MESSAGE_SET_ID_RET = 0x00005010;
        
    /** (0000,5020) VR=SH, VM=1 End Message ID RET */
    public static final int END_MESSAGE_ID_RET = 0x00005020;
        
    /** (0000,5110) VR=AT, VM=1 Display Format RET */
    public static final int DISPLAY_FORMAT_RET = 0x00005110;
        
    /** (0000,5120) VR=AT, VM=1 Page Position ID RET */
    public static final int PAGE_POSITION_ID_RET = 0x00005120;
        
    /** (0000,5130) VR=CS, VM=1 Text Format ID RET */
    public static final int TEXT_FORMAT_ID_RET = 0x00005130;
        
    /** (0000,5140) VR=CS, VM=1 Nor/Rev RET */
    public static final int NOR_REV_RET = 0x00005140;
        
    /** (0000,5150) VR=CS, VM=1 Add Gray Scale RET */
    public static final int ADD_GRAY_SCALE_RET = 0x00005150;
        
    /** (0000,5160) VR=CS, VM=1 Borders RET */
    public static final int BORDERS_RET = 0x00005160;
        
    /** (0000,5170) VR=IS, VM=1 Copies RET */
    public static final int COPIES_RET = 0x00005170;
        
    /** (0000,5180) VR=CS, VM=1 Magnification Type RET */
    public static final int MAGNIFICATION_TYPE_RET = 0x00005180;
        
    /** (0000,5190) VR=CS, VM=1 Erase RET */
    public static final int ERASE_RET = 0x00005190;
        
    /** (0000,51A0) VR=CS, VM=1 Print RET */
    public static final int PRINT_RET = 0x000051A0;
        
    /** (0000,51B0) VR=US, VM=1-n Overlays RET */
    public static final int OVERLAYS_RET = 0x000051B0;
        
    /** (0002,0000) VR=UL, VM=1 Group Length  */
    public static final int GROUP_LENGTH_00020000 = 0x00020000;
        
    /** (0002,0001) VR=OB, VM=1 File Meta Information Version  */
    public static final int FILE_META_INFORMATION_VERSION = 0x00020001;
        
    /** (0002,0002) VR=UI, VM=1 Media Storage SOP Class UID  */
    public static final int MEDIA_STORAGE_SOP_CLASS_UID = 0x00020002;
        
    /** (0002,0003) VR=UI, VM=1 Media Storage SOP Instance UID  */
    public static final int MEDIA_STORAGE_SOP_INSTANCE_UID = 0x00020003;
        
    /** (0002,0010) VR=UI, VM=1 Transfer Syntax UID  */
    public static final int TRANSFER_SYNTAX_UID = 0x00020010;
        
    /** (0002,0012) VR=UI, VM=1 Implementation Class UID  */
    public static final int IMPLEMENTATION_CLASS_UID = 0x00020012;
        
    /** (0002,0013) VR=SH, VM=1 Implementation Version Name  */
    public static final int IMPLEMENTATION_VERSION_NAME = 0x00020013;
        
    /** (0002,0016) VR=AE, VM=1 Source Application Entity Title  */
    public static final int SOURCE_APPLICATION_ENTITY_TITLE = 0x00020016;
        
    /** (0002,0100) VR=UI, VM=1 Private Information Creator UID  */
    public static final int PRIVATE_INFORMATION_CREATOR_UID = 0x00020100;
        
    /** (0002,0102) VR=OB, VM=1 Private Information  */
    public static final int PRIVATE_INFORMATION = 0x00020102;
        
    /** (0004,0000) VR=UL, VM=1 Group Length  */
    public static final int GROUP_LENGTH_00040000 = 0x00040000;
        
    /** (0004,1130) VR=CS, VM=1 File-set ID  */
    public static final int FILE_SET_ID = 0x00041130;
        
    /** (0004,1141) VR=CS, VM=1-8 File-set Descriptor File ID  */
    public static final int FILE_SET_DESCRIPTOR_FILE_ID = 0x00041141;
        
    /** (0004,1142) VR=CS, VM=1 Specific Character Set of File-set Descriptor File  */
    public static final int SPECIFIC_CHARACTER_SET_OF_FILE_SET_DESCRIPTOR_FILE = 0x00041142;
        
    /** (0004,1200) VR=UL, VM=1 Offset of the First Directory Record of the Root Directory Entity  */
    public static final int OFFSET_OF_THE_FIRST_DIRECTORY_RECORD_OF_THE_ROOT_DIRECTORY_ENTITY = 0x00041200;
        
    /** (0004,1202) VR=UL, VM=1 Offset of the Last Directory Record of the Root Directory Entity  */
    public static final int OFFSET_OF_THE_LAST_DIRECTORY_RECORD_OF_THE_ROOT_DIRECTORY_ENTITY = 0x00041202;
        
    /** (0004,1212) VR=US, VM=1 File-set Consistency Flag  */
    public static final int FILE_SET_CONSISTENCY_FLAG = 0x00041212;
        
    /** (0004,1220) VR=SQ, VM=1 Directory Record Sequence  */
    public static final int DIRECTORY_RECORD_SEQUENCE = 0x00041220;
        
    /** (0004,1400) VR=UL, VM=1 Offset of the Next Directory Record  */
    public static final int OFFSET_OF_THE_NEXT_DIRECTORY_RECORD = 0x00041400;
        
    /** (0004,1410) VR=US, VM=1 Record In-use Flag  */
    public static final int RECORD_IN_USE_FLAG = 0x00041410;
        
    /** (0004,1420) VR=UL, VM=1 Offset of Referenced Lower-Level Directory Entity  */
    public static final int OFFSET_OF_REFERENCED_LOWER_LEVEL_DIRECTORY_ENTITY = 0x00041420;
        
    /** (0004,1430) VR=CS, VM=1 Directory Record Type  */
    public static final int DIRECTORY_RECORD_TYPE = 0x00041430;
        
    /** (0004,1432) VR=UI, VM=1 Private Record UID  */
    public static final int PRIVATE_RECORD_UID = 0x00041432;
        
    /** (0004,1500) VR=CS, VM=1-8 Referenced File ID  */
    public static final int REFERENCED_FILE_ID = 0x00041500;
        
    /** (0004,1504) VR=UL, VM=1 MRDR Directory Record Offset RET */
    public static final int MRDR_DIRECTORY_RECORD_OFFSET_RET = 0x00041504;
        
    /** (0004,1510) VR=UI, VM=1 Referenced SOP Class UID in File  */
    public static final int REFERENCED_SOP_CLASS_UID_IN_FILE = 0x00041510;
        
    /** (0004,1511) VR=UI, VM=1 Referenced SOP Instance UID in File  */
    public static final int REFERENCED_SOP_INSTANCE_UID_IN_FILE = 0x00041511;
        
    /** (0004,1512) VR=UI, VM=1 Referenced Transfer Syntax UID in File  */
    public static final int REFERENCED_TRANSFER_SYNTAX_UID_IN_FILE = 0x00041512;
        
    /** (0004,151A) VR=UI, VM=1-n Referenced Related General SOP Class UID in File  */
    public static final int REFERENCED_RELATED_GENERAL_SOP_CLASS_UID_IN_FILE = 0x0004151A;
        
    /** (0004,1600) VR=UL, VM=1 Number of References RET */
    public static final int NUMBER_OF_REFERENCES_RET = 0x00041600;
        
    /** (0008,0001) VR=UL, VM=1 Length to End RET */
    public static final int LENGTH_TO_END_00080001_RET = 0x00080001;
        
    /** (0008,0005) VR=CS, VM=1-n Specific Character Set  */
    public static final int SPECIFIC_CHARACTER_SET = 0x00080005;
        
    /** (0008,0008) VR=CS, VM=1-n Image Type  */
    public static final int IMAGE_TYPE = 0x00080008;
        
    /** (0008,0010) VR=CS, VM=1 Recognition Code RET */
    public static final int RECOGNITION_CODE_00080010_RET = 0x00080010;
        
    /** (0008,0012) VR=DA, VM=1 Instance Creation Date  */
    public static final int INSTANCE_CREATION_DATE = 0x00080012;
        
    /** (0008,0013) VR=TM, VM=1 Instance Creation Time  */
    public static final int INSTANCE_CREATION_TIME = 0x00080013;
        
    /** (0008,0014) VR=UI, VM=1 Instance Creator UID  */
    public static final int INSTANCE_CREATOR_UID = 0x00080014;
        
    /** (0008,0016) VR=UI, VM=1 SOP Class UID  */
    public static final int SOP_CLASS_UID = 0x00080016;
        
    /** (0008,0018) VR=UI, VM=1 SOP Instance UID  */
    public static final int SOP_INSTANCE_UID = 0x00080018;
        
    /** (0008,001A) VR=UI, VM=1-n Related General SOP Class UID  */
    public static final int RELATED_GENERAL_SOP_CLASS_UID = 0x0008001A;
        
    /** (0008,001B) VR=UI, VM=1 Original Specialized SOP Class UID  */
    public static final int ORIGINAL_SPECIALIZED_SOP_CLASS_UID = 0x0008001B;
        
    /** (0008,0020) VR=DA, VM=1 Study Date  */
    public static final int STUDY_DATE = 0x00080020;
        
    /** (0008,0021) VR=DA, VM=1 Series Date  */
    public static final int SERIES_DATE = 0x00080021;
        
    /** (0008,0022) VR=DA, VM=1 Acquisition Date  */
    public static final int ACQUISITION_DATE = 0x00080022;
        
    /** (0008,0023) VR=DA, VM=1 Content Date  */
    public static final int CONTENT_DATE = 0x00080023;
        
    /** (0008,0024) VR=DA, VM=1 Overlay Date RET */
    public static final int OVERLAY_DATE_RET = 0x00080024;
        
    /** (0008,0025) VR=DA, VM=1 Curve Date RET */
    public static final int CURVE_DATE_RET = 0x00080025;
        
    /** (0008,002A) VR=DT, VM=1 Acquisition Datetime  */
    public static final int ACQUISITION_DATETIME = 0x0008002A;
        
    /** (0008,0030) VR=TM, VM=1 Study Time  */
    public static final int STUDY_TIME = 0x00080030;
        
    /** (0008,0031) VR=TM, VM=1 Series Time  */
    public static final int SERIES_TIME = 0x00080031;
        
    /** (0008,0032) VR=TM, VM=1 Acquisition Time  */
    public static final int ACQUISITION_TIME = 0x00080032;
        
    /** (0008,0033) VR=TM, VM=1 Content Time  */
    public static final int CONTENT_TIME = 0x00080033;
        
    /** (0008,0034) VR=TM, VM=1 Overlay Time RET */
    public static final int OVERLAY_TIME_RET = 0x00080034;
        
    /** (0008,0035) VR=TM, VM=1 Curve Time RET */
    public static final int CURVE_TIME_RET = 0x00080035;
        
    /** (0008,0040) VR=US, VM=1 Data Set Type RET */
    public static final int DATA_SET_TYPE_RET = 0x00080040;
        
    /** (0008,0041) VR=LO, VM=1 Data Set Subtype RET */
    public static final int DATA_SET_SUBTYPE_RET = 0x00080041;
        
    /** (0008,0042) VR=CS, VM=1 Nuclear Medicine Series Type RET */
    public static final int NUCLEAR_MEDICINE_SERIES_TYPE_RET = 0x00080042;
        
    /** (0008,0050) VR=SH, VM=1 Accession Number  */
    public static final int ACCESSION_NUMBER = 0x00080050;
        
    /** (0008,0052) VR=CS, VM=1 Query/Retrieve Level  */
    public static final int QUERY_RETRIEVE_LEVEL = 0x00080052;
        
    /** (0008,0054) VR=AE, VM=1-n Retrieve AE Title  */
    public static final int RETRIEVE_AE_TITLE = 0x00080054;
        
    /** (0008,0056) VR=CS, VM=1 Instance Availability  */
    public static final int INSTANCE_AVAILABILITY = 0x00080056;
        
    /** (0008,0058) VR=UI, VM=1-n Failed SOP Instance UID List  */
    public static final int FAILED_SOP_INSTANCE_UID_LIST = 0x00080058;
        
    /** (0008,0060) VR=CS, VM=1 Modality  */
    public static final int MODALITY = 0x00080060;
        
    /** (0008,0061) VR=CS, VM=1-n Modalities in Study  */
    public static final int MODALITIES_IN_STUDY = 0x00080061;
        
    /** (0008,0062) VR=UI, VM=1-n SOP Classes in Study  */
    public static final int SOP_CLASSES_IN_STUDY = 0x00080062;
        
    /** (0008,0064) VR=CS, VM=1 Conversion Type  */
    public static final int CONVERSION_TYPE = 0x00080064;
        
    /** (0008,0068) VR=CS, VM=1 Presentation Intent Type  */
    public static final int PRESENTATION_INTENT_TYPE = 0x00080068;
        
    /** (0008,0070) VR=LO, VM=1 Manufacturer  */
    public static final int MANUFACTURER = 0x00080070;
        
    /** (0008,0080) VR=LO, VM=1 Institution Name  */
    public static final int INSTITUTION_NAME = 0x00080080;
        
    /** (0008,0081) VR=ST, VM=1 Institution Address  */
    public static final int INSTITUTION_ADDRESS = 0x00080081;
        
    /** (0008,0082) VR=SQ, VM=1 Institution Code Sequence  */
    public static final int INSTITUTION_CODE_SEQUENCE = 0x00080082;
        
    /** (0008,0090) VR=PN, VM=1 Referring Physician's Name  */
    public static final int REFERRING_PHYSICIANS_NAME = 0x00080090;
        
    /** (0008,0092) VR=ST, VM=1 Referring Physician's Address  */
    public static final int REFERRING_PHYSICIANS_ADDRESS = 0x00080092;
        
    /** (0008,0094) VR=SH, VM=1-n Referring Physician's Telephone Numbers  */
    public static final int REFERRING_PHYSICIANS_TELEPHONE_NUMBERS = 0x00080094;
        
    /** (0008,0096) VR=SQ, VM=1 Referring Physician Identification Sequence  */
    public static final int REFERRING_PHYSICIAN_IDENTIFICATION_SEQUENCE = 0x00080096;
        
    /** (0008,0100) VR=SH, VM=1 Code Value  */
    public static final int CODE_VALUE = 0x00080100;
        
    /** (0008,0102) VR=SH, VM=1 Coding Scheme Designator  */
    public static final int CODING_SCHEME_DESIGNATOR = 0x00080102;
        
    /** (0008,0103) VR=SH, VM=1 Coding Scheme Version  */
    public static final int CODING_SCHEME_VERSION = 0x00080103;
        
    /** (0008,0104) VR=LO, VM=1 Code Meaning  */
    public static final int CODE_MEANING = 0x00080104;
        
    /** (0008,0105) VR=CS, VM=1 Mapping Resource  */
    public static final int MAPPING_RESOURCE = 0x00080105;
        
    /** (0008,0106) VR=DT, VM=1 Context Group Version  */
    public static final int CONTEXT_GROUP_VERSION = 0x00080106;
        
    /** (0008,0107) VR=DT, VM=1 Context Group Local Version  */
    public static final int CONTEXT_GROUP_LOCAL_VERSION = 0x00080107;
        
    /** (0008,010B) VR=CS, VM=1 Context Group Extension Flag  */
    public static final int CONTEXT_GROUP_EXTENSION_FLAG = 0x0008010B;
        
    /** (0008,010C) VR=UI, VM=1 Coding Scheme UID  */
    public static final int CODING_SCHEME_UID = 0x0008010C;
        
    /** (0008,010D) VR=UI, VM=1 Context Group Extension Creator UID  */
    public static final int CONTEXT_GROUP_EXTENSION_CREATOR_UID = 0x0008010D;
        
    /** (0008,010F) VR=CS, VM=1 Context Identifier  */
    public static final int CONTEXT_IDENTIFIER = 0x0008010F;
        
    /** (0008,0110) VR=SQ, VM=1 Coding Scheme Identification Sequence  */
    public static final int CODING_SCHEME_IDENTIFICATION_SEQUENCE = 0x00080110;
        
    /** (0008,0112) VR=LO, VM=1 Coding Scheme Registry  */
    public static final int CODING_SCHEME_REGISTRY = 0x00080112;
        
    /** (0008,0114) VR=ST, VM=1 Coding Scheme External ID  */
    public static final int CODING_SCHEME_EXTERNAL_ID = 0x00080114;
        
    /** (0008,0115) VR=ST, VM=1 Coding Scheme Name  */
    public static final int CODING_SCHEME_NAME = 0x00080115;
        
    /** (0008,0116) VR=ST, VM=1 Responsible Organization  */
    public static final int RESPONSIBLE_ORGANIZATION_00080116 = 0x00080116;
        
    /** (0008,0201) VR=SH, VM=1 Timezone Offset From UTC  */
    public static final int TIMEZONE_OFFSET_FROM_UTC = 0x00080201;
        
    /** (0008,1000) VR=AE, VM=1 Network ID RET */
    public static final int NETWORK_ID_RET = 0x00081000;
        
    /** (0008,1010) VR=SH, VM=1 Station Name  */
    public static final int STATION_NAME = 0x00081010;
        
    /** (0008,1030) VR=LO, VM=1 Study Description  */
    public static final int STUDY_DESCRIPTION = 0x00081030;
        
    /** (0008,1032) VR=SQ, VM=1 Procedure Code Sequence  */
    public static final int PROCEDURE_CODE_SEQUENCE = 0x00081032;
        
    /** (0008,103E) VR=LO, VM=1 Series Description  */
    public static final int SERIES_DESCRIPTION = 0x0008103E;
        
    /** (0008,1040) VR=LO, VM=1 Institutional Department Name  */
    public static final int INSTITUTIONAL_DEPARTMENT_NAME = 0x00081040;
        
    /** (0008,1048) VR=PN, VM=1-n Physician(s) of Record  */
    public static final int PHYSICIANS_OF_RECORD = 0x00081048;
        
    /** (0008,1049) VR=SQ, VM=1 Physician(s) of Record Identification Sequence  */
    public static final int PHYSICIANS_OF_RECORD_IDENTIFICATION_SEQUENCE = 0x00081049;
        
    /** (0008,1050) VR=PN, VM=1-n Performing Physician's Name  */
    public static final int PERFORMING_PHYSICIANS_NAME = 0x00081050;
        
    /** (0008,1052) VR=SQ, VM=1 Performing Physician Identification Sequence  */
    public static final int PERFORMING_PHYSICIAN_IDENTIFICATION_SEQUENCE = 0x00081052;
        
    /** (0008,1060) VR=PN, VM=1-n Name of Physician(s) Reading Study  */
    public static final int NAME_OF_PHYSICIANS_READING_STUDY = 0x00081060;
        
    /** (0008,1062) VR=SQ, VM=1 Physician(s) Reading Study Identification Sequence  */
    public static final int PHYSICIANS_READING_STUDY_IDENTIFICATION_SEQUENCE = 0x00081062;
        
    /** (0008,1070) VR=PN, VM=1-n Operators' Name  */
    public static final int OPERATORS_NAME = 0x00081070;
        
    /** (0008,1072) VR=SQ, VM=1 Operator Identification Sequence  */
    public static final int OPERATOR_IDENTIFICATION_SEQUENCE = 0x00081072;
        
    /** (0008,1080) VR=LO, VM=1-n Admitting Diagnoses Description  */
    public static final int ADMITTING_DIAGNOSES_DESCRIPTION = 0x00081080;
        
    /** (0008,1084) VR=SQ, VM=1 Admitting Diagnoses Code Sequence  */
    public static final int ADMITTING_DIAGNOSES_CODE_SEQUENCE = 0x00081084;
        
    /** (0008,1090) VR=LO, VM=1 Manufacturer's Model Name  */
    public static final int MANUFACTURERS_MODEL_NAME = 0x00081090;
        
    /** (0008,1100) VR=SQ, VM=1 Referenced Results Sequence RET */
    public static final int REFERENCED_RESULTS_SEQUENCE_RET = 0x00081100;
        
    /** (0008,1110) VR=SQ, VM=1 Referenced Study Sequence  */
    public static final int REFERENCED_STUDY_SEQUENCE = 0x00081110;
        
    /** (0008,1111) VR=SQ, VM=1 Referenced Performed Procedure Step Sequence  */
    public static final int REFERENCED_PERFORMED_PROCEDURE_STEP_SEQUENCE = 0x00081111;
        
    /** (0008,1115) VR=SQ, VM=1 Referenced Series Sequence  */
    public static final int REFERENCED_SERIES_SEQUENCE = 0x00081115;
        
    /** (0008,1120) VR=SQ, VM=1 Referenced Patient Sequence  */
    public static final int REFERENCED_PATIENT_SEQUENCE = 0x00081120;
        
    /** (0008,1125) VR=SQ, VM=1 Referenced Visit Sequence  */
    public static final int REFERENCED_VISIT_SEQUENCE = 0x00081125;
        
    /** (0008,1130) VR=SQ, VM=1 Referenced Overlay Sequence RET */
    public static final int REFERENCED_OVERLAY_SEQUENCE_RET = 0x00081130;
        
    /** (0008,113A) VR=SQ, VM=1 Referenced Waveform Sequence  */
    public static final int REFERENCED_WAVEFORM_SEQUENCE = 0x0008113A;
        
    /** (0008,1140) VR=SQ, VM=1 Referenced Image Sequence  */
    public static final int REFERENCED_IMAGE_SEQUENCE = 0x00081140;
        
    /** (0008,1145) VR=SQ, VM=1 Referenced Curve Sequence RET */
    public static final int REFERENCED_CURVE_SEQUENCE_RET = 0x00081145;
        
    /** (0008,114A) VR=SQ, VM=1 Referenced Instance Sequence  */
    public static final int REFERENCED_INSTANCE_SEQUENCE = 0x0008114A;
        
    /** (0008,114B) VR=SQ, VM=1 Referenced Real World Value Mapping Instance Sequence  */
    public static final int REFERENCED_REAL_WORLD_VALUE_MAPPING_INSTANCE_SEQUENCE = 0x0008114B;
        
    /** (0008,1150) VR=UI, VM=1 Referenced SOP Class UID  */
    public static final int REFERENCED_SOP_CLASS_UID = 0x00081150;
        
    /** (0008,1155) VR=UI, VM=1 Referenced SOP Instance UID  */
    public static final int REFERENCED_SOP_INSTANCE_UID = 0x00081155;
        
    /** (0008,115A) VR=UI, VM=1-n SOP Classes Supported  */
    public static final int SOP_CLASSES_SUPPORTED = 0x0008115A;
        
    /** (0008,1160) VR=IS, VM=1-n Referenced Frame Number  */
    public static final int REFERENCED_FRAME_NUMBER = 0x00081160;
        
    /** (0008,1195) VR=UI, VM=1 Transaction UID  */
    public static final int TRANSACTION_UID = 0x00081195;
        
    /** (0008,1197) VR=US, VM=1 Failure Reason  */
    public static final int FAILURE_REASON = 0x00081197;
        
    /** (0008,1198) VR=SQ, VM=1 Failed SOP Sequence  */
    public static final int FAILED_SOP_SEQUENCE = 0x00081198;
        
    /** (0008,1199) VR=SQ, VM=1 Referenced SOP Sequence  */
    public static final int REFERENCED_SOP_SEQUENCE = 0x00081199;
        
    /** (0008,1200) VR=SQ, VM=1 Studies Containing Other Referenced Instances Sequence  */
    public static final int STUDIES_CONTAINING_OTHER_REFERENCED_INSTANCES_SEQUENCE = 0x00081200;
        
    /** (0008,1250) VR=SQ, VM=1 Related Series Sequence  */
    public static final int RELATED_SERIES_SEQUENCE = 0x00081250;
        
    /** (0008,2110) VR=CS, VM=1 Lossy Image Compression RET */
    public static final int LOSSY_IMAGE_COMPRESSION_RET = 0x00082110;
        
    /** (0008,2111) VR=ST, VM=1 Derivation Description  */
    public static final int DERIVATION_DESCRIPTION = 0x00082111;
        
    /** (0008,2112) VR=SQ, VM=1 Source Image Sequence  */
    public static final int SOURCE_IMAGE_SEQUENCE = 0x00082112;
        
    /** (0008,2120) VR=SH, VM=1 Stage Name  */
    public static final int STAGE_NAME = 0x00082120;
        
    /** (0008,2122) VR=IS, VM=1 Stage Number  */
    public static final int STAGE_NUMBER = 0x00082122;
        
    /** (0008,2124) VR=IS, VM=1 Number of Stages  */
    public static final int NUMBER_OF_STAGES = 0x00082124;
        
    /** (0008,2127) VR=SH, VM=1 View Name  */
    public static final int VIEW_NAME = 0x00082127;
        
    /** (0008,2128) VR=IS, VM=1 View Number  */
    public static final int VIEW_NUMBER = 0x00082128;
        
    /** (0008,2129) VR=IS, VM=1 Number of Event Timers  */
    public static final int NUMBER_OF_EVENT_TIMERS = 0x00082129;
        
    /** (0008,212A) VR=IS, VM=1 Number of Views in Stage  */
    public static final int NUMBER_OF_VIEWS_IN_STAGE = 0x0008212A;
        
    /** (0008,2130) VR=DS, VM=1-n Event Elapsed Time(s)  */
    public static final int EVENT_ELAPSED_TIMES = 0x00082130;
        
    /** (0008,2132) VR=LO, VM=1-n Event Timer Name(s)  */
    public static final int EVENT_TIMER_NAMES = 0x00082132;
        
    /** (0008,2142) VR=IS, VM=1 Start Trim  */
    public static final int START_TRIM = 0x00082142;
        
    /** (0008,2143) VR=IS, VM=1 Stop Trim  */
    public static final int STOP_TRIM = 0x00082143;
        
    /** (0008,2144) VR=IS, VM=1 Recommended Display Frame Rate  */
    public static final int RECOMMENDED_DISPLAY_FRAME_RATE = 0x00082144;
        
    /** (0008,2200) VR=CS, VM=1 Transducer Position RET */
    public static final int TRANSDUCER_POSITION_RET = 0x00082200;
        
    /** (0008,2204) VR=CS, VM=1 Transducer Orientation RET */
    public static final int TRANSDUCER_ORIENTATION_RET = 0x00082204;
        
    /** (0008,2208) VR=CS, VM=1 Anatomic Structure RET */
    public static final int ANATOMIC_STRUCTURE_RET = 0x00082208;
        
    /** (0008,2218) VR=SQ, VM=1 Anatomic Region Sequence  */
    public static final int ANATOMIC_REGION_SEQUENCE = 0x00082218;
        
    /** (0008,2220) VR=SQ, VM=1 Anatomic Region Modifier Sequence  */
    public static final int ANATOMIC_REGION_MODIFIER_SEQUENCE = 0x00082220;
        
    /** (0008,2228) VR=SQ, VM=1 Primary Anatomic Structure Sequence  */
    public static final int PRIMARY_ANATOMIC_STRUCTURE_SEQUENCE = 0x00082228;
        
    /** (0008,2229) VR=SQ, VM=1 Anatomic Structure, Space or Region Sequence  */
    public static final int ANATOMIC_STRUCTURE_SPACE_OR_REGION_SEQUENCE = 0x00082229;
        
    /** (0008,2230) VR=SQ, VM=1 Primary Anatomic Structure Modifier Sequence  */
    public static final int PRIMARY_ANATOMIC_STRUCTURE_MODIFIER_SEQUENCE = 0x00082230;
        
    /** (0008,2240) VR=SQ, VM=1 Transducer Position Sequence RET */
    public static final int TRANSDUCER_POSITION_SEQUENCE_RET = 0x00082240;
        
    /** (0008,2242) VR=SQ, VM=1 Transducer Position Modifier Sequence RET */
    public static final int TRANSDUCER_POSITION_MODIFIER_SEQUENCE_RET = 0x00082242;
        
    /** (0008,2244) VR=SQ, VM=1 Transducer Orientation Sequence RET */
    public static final int TRANSDUCER_ORIENTATION_SEQUENCE_RET = 0x00082244;
        
    /** (0008,2246) VR=SQ, VM=1 Transducer Orientation Modifier Sequence RET */
    public static final int TRANSDUCER_ORIENTATION_MODIFIER_SEQUENCE_RET = 0x00082246;
        
    /** (0008,3001) VR=SQ, VM=1 Alternate Representation Sequence  */
    public static final int ALTERNATE_REPRESENTATION_SEQUENCE = 0x00083001;
        
    /** (0008,3010) VR=UI, VM=1 Irradiation Event UID  */
    public static final int IRRADIATION_EVENT_UID = 0x00083010;
        
    /** (0008,4000) VR=LT, VM=1 Identifying Comments RET */
    public static final int IDENTIFYING_COMMENTS_RET = 0x00084000;
        
    /** (0008,9007) VR=CS, VM=4 Frame Type  */
    public static final int FRAME_TYPE = 0x00089007;
        
    /** (0008,9092) VR=SQ, VM=1 Referenced Image Evidence Sequence  */
    public static final int REFERENCED_IMAGE_EVIDENCE_SEQUENCE = 0x00089092;
        
    /** (0008,9121) VR=SQ, VM=1 Referenced Raw Data Sequence  */
    public static final int REFERENCED_RAW_DATA_SEQUENCE = 0x00089121;
        
    /** (0008,9123) VR=UI, VM=1 Creator-Version UID  */
    public static final int CREATOR_VERSION_UID = 0x00089123;
        
    /** (0008,9124) VR=SQ, VM=1 Derivation Image Sequence  */
    public static final int DERIVATION_IMAGE_SEQUENCE = 0x00089124;
        
    /** (0008,9154) VR=SQ, VM=1 Source Image Evidence Sequence  */
    public static final int SOURCE_IMAGE_EVIDENCE_SEQUENCE = 0x00089154;
        
    /** (0008,9205) VR=CS, VM=1 Pixel Presentation  */
    public static final int PIXEL_PRESENTATION = 0x00089205;
        
    /** (0008,9206) VR=CS, VM=1 Volumetric Properties  */
    public static final int VOLUMETRIC_PROPERTIES = 0x00089206;
        
    /** (0008,9207) VR=CS, VM=1 Volume Based Calculation Technique  */
    public static final int VOLUME_BASED_CALCULATION_TECHNIQUE = 0x00089207;
        
    /** (0008,9208) VR=CS, VM=1 Complex Image Component  */
    public static final int COMPLEX_IMAGE_COMPONENT = 0x00089208;
        
    /** (0008,9209) VR=CS, VM=1 Acquisition Contrast  */
    public static final int ACQUISITION_CONTRAST = 0x00089209;
        
    /** (0008,9215) VR=SQ, VM=1 Derivation Code Sequence  */
    public static final int DERIVATION_CODE_SEQUENCE = 0x00089215;
        
    /** (0008,9237) VR=SQ, VM=1 Referenced Grayscale Presentation State Sequence  */
    public static final int REFERENCED_GRAYSCALE_PRESENTATION_STATE_SEQUENCE = 0x00089237;
        
    /** (0008,9410) VR=SQ, VM=1 Referenced Other Plane Sequence  */
    public static final int REFERENCED_OTHER_PLANE_SEQUENCE = 0x00089410;
        
    /** (0008,9458) VR=SQ, VM=1 Frame Display Sequence  */
    public static final int FRAME_DISPLAY_SEQUENCE = 0x00089458;
        
    /** (0008,9459) VR=FL, VM=1 Recommended Display Frame Rate in Float  */
    public static final int RECOMMENDED_DISPLAY_FRAME_RATE_IN_FLOAT = 0x00089459;
        
    /** (0008,9460) VR=CS, VM=1 Skip Frame Range Flag  */
    public static final int SKIP_FRAME_RANGE_FLAG = 0x00089460;
        
    /** (0010,0010) VR=PN, VM=1 Patient's Name  */
    public static final int PATIENTS_NAME = 0x00100010;
        
    /** (0010,0020) VR=LO, VM=1 Patient ID  */
    public static final int PATIENT_ID = 0x00100020;
        
    /** (0010,0021) VR=LO, VM=1 Issuer of Patient ID  */
    public static final int ISSUER_OF_PATIENT_ID = 0x00100021;
        
    /** (0010,0022) VR=CS, VM=1 Type of Patient ID  */
    public static final int TYPE_OF_PATIENT_ID = 0x00100022;
        
    /** (0010,0030) VR=DA, VM=1 Patient's Birth Date  */
    public static final int PATIENTS_BIRTH_DATE = 0x00100030;
        
    /** (0010,0032) VR=TM, VM=1 Patient's Birth Time  */
    public static final int PATIENTS_BIRTH_TIME = 0x00100032;
        
    /** (0010,0040) VR=CS, VM=1 Patient's Sex  */
    public static final int PATIENTS_SEX = 0x00100040;
        
    /** (0010,0050) VR=SQ, VM=1 Patient's Insurance Plan Code Sequence  */
    public static final int PATIENTS_INSURANCE_PLAN_CODE_SEQUENCE = 0x00100050;
        
    /** (0010,0101) VR=SQ, VM=1 Patient's Primary Language Code Sequence  */
    public static final int PATIENTS_PRIMARY_LANGUAGE_CODE_SEQUENCE = 0x00100101;
        
    /** (0010,0102) VR=SQ, VM=1 Patient's Primary Language Code Modifier Sequence  */
    public static final int PATIENTS_PRIMARY_LANGUAGE_CODE_MODIFIER_SEQUENCE = 0x00100102;
        
    /** (0010,1000) VR=LO, VM=1-n Other Patient IDs  */
    public static final int OTHER_PATIENT_IDS = 0x00101000;
        
    /** (0010,1001) VR=PN, VM=1-n Other Patient Names  */
    public static final int OTHER_PATIENT_NAMES = 0x00101001;
        
    /** (0010,1002) VR=SQ, VM=1 Other Patient IDs Sequence  */
    public static final int OTHER_PATIENT_IDS_SEQUENCE = 0x00101002;
        
    /** (0010,1005) VR=PN, VM=1 Patient's Birth Name  */
    public static final int PATIENTS_BIRTH_NAME = 0x00101005;
        
    /** (0010,1010) VR=AS, VM=1 Patient's Age  */
    public static final int PATIENTS_AGE = 0x00101010;
        
    /** (0010,1020) VR=DS, VM=1 Patient's Size  */
    public static final int PATIENTS_SIZE = 0x00101020;
        
    /** (0010,1030) VR=DS, VM=1 Patient's Weight  */
    public static final int PATIENTS_WEIGHT = 0x00101030;
        
    /** (0010,1040) VR=LO, VM=1 Patient's Address  */
    public static final int PATIENTS_ADDRESS = 0x00101040;
        
    /** (0010,1050) VR=LO, VM=1-n Insurance Plan Identification RET */
    public static final int INSURANCE_PLAN_IDENTIFICATION_RET = 0x00101050;
        
    /** (0010,1060) VR=PN, VM=1 Patient's Mother's Birth Name  */
    public static final int PATIENTS_MOTHERS_BIRTH_NAME = 0x00101060;
        
    /** (0010,1080) VR=LO, VM=1 Military Rank  */
    public static final int MILITARY_RANK = 0x00101080;
        
    /** (0010,1081) VR=LO, VM=1 Branch of Service  */
    public static final int BRANCH_OF_SERVICE = 0x00101081;
        
    /** (0010,1090) VR=LO, VM=1 Medical Record Locator  */
    public static final int MEDICAL_RECORD_LOCATOR = 0x00101090;
        
    /** (0010,2000) VR=LO, VM=1-n Medical Alerts  */
    public static final int MEDICAL_ALERTS = 0x00102000;
        
    /** (0010,2110) VR=LO, VM=1-n Contrast Allergies  */
    public static final int CONTRAST_ALLERGIES = 0x00102110;
        
    /** (0010,2150) VR=LO, VM=1 Country of Residence  */
    public static final int COUNTRY_OF_RESIDENCE = 0x00102150;
        
    /** (0010,2152) VR=LO, VM=1 Region of Residence  */
    public static final int REGION_OF_RESIDENCE = 0x00102152;
        
    /** (0010,2154) VR=SH, VM=1-n Patient's Telephone Numbers  */
    public static final int PATIENTS_TELEPHONE_NUMBERS = 0x00102154;
        
    /** (0010,2160) VR=SH, VM=1 Ethnic Group  */
    public static final int ETHNIC_GROUP = 0x00102160;
        
    /** (0010,2180) VR=SH, VM=1 Occupation  */
    public static final int OCCUPATION = 0x00102180;
        
    /** (0010,21A0) VR=CS, VM=1 Smoking Status  */
    public static final int SMOKING_STATUS = 0x001021A0;
        
    /** (0010,21B0) VR=LT, VM=1 Additional Patient History  */
    public static final int ADDITIONAL_PATIENT_HISTORY = 0x001021B0;
        
    /** (0010,21C0) VR=US, VM=1 Pregnancy Status  */
    public static final int PREGNANCY_STATUS = 0x001021C0;
        
    /** (0010,21D0) VR=DA, VM=1 Last Menstrual Date  */
    public static final int LAST_MENSTRUAL_DATE = 0x001021D0;
        
    /** (0010,21F0) VR=LO, VM=1 Patient's Religious Preference  */
    public static final int PATIENTS_RELIGIOUS_PREFERENCE = 0x001021F0;
        
    /** (0010,2201) VR=LO, VM=1 Patient Species Description  */
    public static final int PATIENT_SPECIES_DESCRIPTION = 0x00102201;
        
    /** (0010,2202) VR=SQ, VM=1 Patient Species Code Sequence  */
    public static final int PATIENT_SPECIES_CODE_SEQUENCE = 0x00102202;
        
    /** (0010,2203) VR=CS, VM=1 Patient's Sex Neutered  */
    public static final int PATIENTS_SEX_NEUTERED = 0x00102203;
        
    /** (0010,2292) VR=LO, VM=1 Patient Breed Description  */
    public static final int PATIENT_BREED_DESCRIPTION = 0x00102292;
        
    /** (0010,2293) VR=SQ, VM=1 Patient Breed Code Sequence  */
    public static final int PATIENT_BREED_CODE_SEQUENCE = 0x00102293;
        
    /** (0010,2294) VR=SQ, VM=1 Breed Registration Sequence  */
    public static final int BREED_REGISTRATION_SEQUENCE = 0x00102294;
        
    /** (0010,2295) VR=LO, VM=1 Breed Registration Number  */
    public static final int BREED_REGISTRATION_NUMBER = 0x00102295;
        
    /** (0010,2296) VR=SQ, VM=1 Breed Registry Code Sequence  */
    public static final int BREED_REGISTRY_CODE_SEQUENCE = 0x00102296;
        
    /** (0010,2297) VR=PN, VM=1 Responsible Person  */
    public static final int RESPONSIBLE_PERSON = 0x00102297;
        
    /** (0010,2298) VR=CS, VM=1 Responsible Person Role  */
    public static final int RESPONSIBLE_PERSON_ROLE = 0x00102298;
        
    /** (0010,2299) VR=LO, VM=1 Responsible Organization  */
    public static final int RESPONSIBLE_ORGANIZATION_00102299 = 0x00102299;
        
    /** (0010,4000) VR=LT, VM=1 Patient Comments  */
    public static final int PATIENT_COMMENTS = 0x00104000;
        
    /** (0010,9431) VR=FL, VM=1 Examined Body Thickness  */
    public static final int EXAMINED_BODY_THICKNESS = 0x00109431;
        
    /** (0012,0010) VR=LO, VM=1 Clinical Trial Sponsor Name  */
    public static final int CLINICAL_TRIAL_SPONSOR_NAME = 0x00120010;
        
    /** (0012,0020) VR=LO, VM=1 Clinical Trial Protocol ID  */
    public static final int CLINICAL_TRIAL_PROTOCOL_ID = 0x00120020;
        
    /** (0012,0021) VR=LO, VM=1 Clinical Trial Protocol Name  */
    public static final int CLINICAL_TRIAL_PROTOCOL_NAME = 0x00120021;
        
    /** (0012,0030) VR=LO, VM=1 Clinical Trial Site ID  */
    public static final int CLINICAL_TRIAL_SITE_ID = 0x00120030;
        
    /** (0012,0031) VR=LO, VM=1 Clinical Trial Site Name  */
    public static final int CLINICAL_TRIAL_SITE_NAME = 0x00120031;
        
    /** (0012,0040) VR=LO, VM=1 Clinical Trial Subject ID  */
    public static final int CLINICAL_TRIAL_SUBJECT_ID = 0x00120040;
        
    /** (0012,0042) VR=LO, VM=1 Clinical Trial Subject Reading ID  */
    public static final int CLINICAL_TRIAL_SUBJECT_READING_ID = 0x00120042;
        
    /** (0012,0050) VR=LO, VM=1 Clinical Trial Time Point ID  */
    public static final int CLINICAL_TRIAL_TIME_POINT_ID = 0x00120050;
        
    /** (0012,0051) VR=ST, VM=1 Clinical Trial Time Point Description  */
    public static final int CLINICAL_TRIAL_TIME_POINT_DESCRIPTION = 0x00120051;
        
    /** (0012,0060) VR=LO, VM=1 Clinical Trial Coordinating Center Name  */
    public static final int CLINICAL_TRIAL_COORDINATING_CENTER_NAME = 0x00120060;
        
    /** (0012,0062) VR=CS, VM=1 Patient Identify Removed  */
    public static final int PATIENT_IDENTIFY_REMOVED = 0x00120062;
        
    /** (0012,0063) VR=LO, VM=1-n De-identification Method  */
    public static final int DE_IDENTIFICATION_METHOD = 0x00120063;
        
    /** (0012,0064) VR=SQ, VM=1 De-identification Method Code Sequence  */
    public static final int DE_IDENTIFICATION_METHOD_CODE_SEQUENCE = 0x00120064;
        
    /** (0018,0010) VR=LO, VM=1 Contrast/Bolus Agent  */
    public static final int CONTRAST_BOLUS_AGENT = 0x00180010;
        
    /** (0018,0012) VR=SQ, VM=1 Contrast/Bolus Agent Sequence  */
    public static final int CONTRAST_BOLUS_AGENT_SEQUENCE = 0x00180012;
        
    /** (0018,0014) VR=SQ, VM=1 Contrast/Bolus Administration Route Sequence  */
    public static final int CONTRAST_BOLUS_ADMINISTRATION_ROUTE_SEQUENCE = 0x00180014;
        
    /** (0018,0015) VR=CS, VM=1 Body Part Examined  */
    public static final int BODY_PART_EXAMINED = 0x00180015;
        
    /** (0018,0020) VR=CS, VM=1-n Scanning Sequence  */
    public static final int SCANNING_SEQUENCE = 0x00180020;
        
    /** (0018,0021) VR=CS, VM=1-n Sequence Variant  */
    public static final int SEQUENCE_VARIANT = 0x00180021;
        
    /** (0018,0022) VR=CS, VM=1-n Scan Options  */
    public static final int SCAN_OPTIONS = 0x00180022;
        
    /** (0018,0023) VR=CS, VM=1 MR Acquisition Type  */
    public static final int MR_ACQUISITION_TYPE = 0x00180023;
        
    /** (0018,0024) VR=SH, VM=1 Sequence Name  */
    public static final int SEQUENCE_NAME = 0x00180024;
        
    /** (0018,0025) VR=CS, VM=1 Angio Flag  */
    public static final int ANGIO_FLAG = 0x00180025;
        
    /** (0018,0026) VR=SQ, VM=1 Intervention Drug Information Sequence  */
    public static final int INTERVENTION_DRUG_INFORMATION_SEQUENCE = 0x00180026;
        
    /** (0018,0027) VR=TM, VM=1 Intervention Drug Stop Time  */
    public static final int INTERVENTION_DRUG_STOP_TIME = 0x00180027;
        
    /** (0018,0028) VR=DS, VM=1 Intervention Drug Dose  */
    public static final int INTERVENTION_DRUG_DOSE = 0x00180028;
        
    /** (0018,0029) VR=SQ, VM=1 Intervention Drug Sequence  */
    public static final int INTERVENTION_DRUG_SEQUENCE = 0x00180029;
        
    /** (0018,002A) VR=SQ, VM=1 Additional Drug Sequence  */
    public static final int ADDITIONAL_DRUG_SEQUENCE = 0x0018002A;
        
    /** (0018,0030) VR=LO, VM=1-n Radionuclide RET */
    public static final int RADIONUCLIDE_RET = 0x00180030;
        
    /** (0018,0031) VR=LO, VM=1 Radiopharmaceutical  */
    public static final int RADIOPHARMACEUTICAL = 0x00180031;
        
    /** (0018,0032) VR=DS, VM=1 Energy Window Centerline RET */
    public static final int ENERGY_WINDOW_CENTERLINE_RET = 0x00180032;
        
    /** (0018,0033) VR=DS, VM=1-n Energy Window Total Width RET */
    public static final int ENERGY_WINDOW_TOTAL_WIDTH_RET = 0x00180033;
        
    /** (0018,0034) VR=LO, VM=1 Intervention Drug Name  */
    public static final int INTERVENTION_DRUG_NAME = 0x00180034;
        
    /** (0018,0035) VR=TM, VM=1 Intervention Drug Start Time  */
    public static final int INTERVENTION_DRUG_START_TIME = 0x00180035;
        
    /** (0018,0036) VR=SQ, VM=1 Intervention Sequence  */
    public static final int INTERVENTION_SEQUENCE = 0x00180036;
        
    /** (0018,0037) VR=CS, VM=1 Therapy Type RET */
    public static final int THERAPY_TYPE_RET = 0x00180037;
        
    /** (0018,0038) VR=CS, VM=1 Intervention Status  */
    public static final int INTERVENTION_STATUS = 0x00180038;
        
    /** (0018,0039) VR=CS, VM=1 Therapy Description RET */
    public static final int THERAPY_DESCRIPTION_RET = 0x00180039;
        
    /** (0018,003A) VR=ST, VM=1 Intervention Description  */
    public static final int INTERVENTION_DESCRIPTION = 0x0018003A;
        
    /** (0018,0040) VR=IS, VM=1 Cine Rate  */
    public static final int CINE_RATE = 0x00180040;
        
    /** (0018,0050) VR=DS, VM=1 Slice Thickness  */
    public static final int SLICE_THICKNESS = 0x00180050;
        
    /** (0018,0060) VR=DS, VM=1 KVP  */
    public static final int KVP = 0x00180060;
        
    /** (0018,0070) VR=IS, VM=1 Counts Accumulated  */
    public static final int COUNTS_ACCUMULATED = 0x00180070;
        
    /** (0018,0071) VR=CS, VM=1 Acquisition Termination Condition  */
    public static final int ACQUISITION_TERMINATION_CONDITION = 0x00180071;
        
    /** (0018,0072) VR=DS, VM=1 Effective Duration  */
    public static final int EFFECTIVE_DURATION = 0x00180072;
        
    /** (0018,0073) VR=CS, VM=1 Acquisition Start Condition  */
    public static final int ACQUISITION_START_CONDITION = 0x00180073;
        
    /** (0018,0074) VR=IS, VM=1 Acquisition Start Condition Data  */
    public static final int ACQUISITION_START_CONDITION_DATA = 0x00180074;
        
    /** (0018,0075) VR=IS, VM=1 Acquisition Termination Condition Data  */
    public static final int ACQUISITION_TERMINATION_CONDITION_DATA = 0x00180075;
        
    /** (0018,0080) VR=DS, VM=1 Repetition Time  */
    public static final int REPETITION_TIME = 0x00180080;
        
    /** (0018,0081) VR=DS, VM=1 Echo Time  */
    public static final int ECHO_TIME = 0x00180081;
        
    /** (0018,0082) VR=DS, VM=1 Inversion Time  */
    public static final int INVERSION_TIME = 0x00180082;
        
    /** (0018,0083) VR=DS, VM=1 Number of Averages  */
    public static final int NUMBER_OF_AVERAGES = 0x00180083;
        
    /** (0018,0084) VR=DS, VM=1 Imaging Frequency  */
    public static final int IMAGING_FREQUENCY = 0x00180084;
        
    /** (0018,0085) VR=SH, VM=1 Imaged Nucleus  */
    public static final int IMAGED_NUCLEUS = 0x00180085;
        
    /** (0018,0086) VR=IS, VM=1-n Echo Number(s)  */
    public static final int ECHO_NUMBERS = 0x00180086;
        
    /** (0018,0087) VR=DS, VM=1 Magnetic Field Strength  */
    public static final int MAGNETIC_FIELD_STRENGTH = 0x00180087;
        
    /** (0018,0088) VR=DS, VM=1 Spacing Between Slices  */
    public static final int SPACING_BETWEEN_SLICES = 0x00180088;
        
    /** (0018,0089) VR=IS, VM=1 Number of Phase Encoding Steps  */
    public static final int NUMBER_OF_PHASE_ENCODING_STEPS = 0x00180089;
        
    /** (0018,0090) VR=DS, VM=1 Data Collection Diameter  */
    public static final int DATA_COLLECTION_DIAMETER = 0x00180090;
        
    /** (0018,0091) VR=IS, VM=1 Echo Train Length  */
    public static final int ECHO_TRAIN_LENGTH = 0x00180091;
        
    /** (0018,0093) VR=DS, VM=1 Percent Sampling  */
    public static final int PERCENT_SAMPLING = 0x00180093;
        
    /** (0018,0094) VR=DS, VM=1 Percent Phase Field of View  */
    public static final int PERCENT_PHASE_FIELD_OF_VIEW = 0x00180094;
        
    /** (0018,0095) VR=DS, VM=1 Pixel Bandwidth  */
    public static final int PIXEL_BANDWIDTH = 0x00180095;
        
    /** (0018,1000) VR=LO, VM=1 Device Serial Number  */
    public static final int DEVICE_SERIAL_NUMBER = 0x00181000;
        
    /** (0018,1002) VR=UI, VM=1 Device UID  */
    public static final int DEVICE_UID = 0x00181002;
        
    /** (0018,1004) VR=LO, VM=1 Plate ID  */
    public static final int PLATE_ID = 0x00181004;
        
    /** (0018,1005) VR=LO, VM=1 Generator ID  */
    public static final int GENERATOR_ID = 0x00181005;
        
    /** (0018,1006) VR=LO, VM=1 Grid ID  */
    public static final int GRID_ID = 0x00181006;
        
    /** (0018,1007) VR=LO, VM=1 Cassette ID  */
    public static final int CASSETTE_ID = 0x00181007;
        
    /** (0018,1008) VR=LO, VM=1 Gantry ID  */
    public static final int GANTRY_ID = 0x00181008;
        
    /** (0018,1010) VR=LO, VM=1 Secondary Capture Device ID  */
    public static final int SECONDARY_CAPTURE_DEVICE_ID = 0x00181010;
        
    /** (0018,1011) VR=LO, VM=1 Hardcopy Creation Device ID  */
    public static final int HARDCOPY_CREATION_DEVICE_ID = 0x00181011;
        
    /** (0018,1012) VR=DA, VM=1 Date of Secondary Capture  */
    public static final int DATE_OF_SECONDARY_CAPTURE = 0x00181012;
        
    /** (0018,1014) VR=TM, VM=1 Time of Secondary Capture  */
    public static final int TIME_OF_SECONDARY_CAPTURE = 0x00181014;
        
    /** (0018,1016) VR=LO, VM=1 Secondary Capture Device Manufacturer  */
    public static final int SECONDARY_CAPTURE_DEVICE_MANUFACTURER = 0x00181016;
        
    /** (0018,1017) VR=LO, VM=1 Hardcopy Device Manufacturer  */
    public static final int HARDCOPY_DEVICE_MANUFACTURER = 0x00181017;
        
    /** (0018,1018) VR=LO, VM=1 Secondary Capture Device Manufacturer's Model Name  */
    public static final int SECONDARY_CAPTURE_DEVICE_MANUFACTURERS_MODEL_NAME = 0x00181018;
        
    /** (0018,1019) VR=LO, VM=1-n Secondary Capture Device Software Version(s)  */
    public static final int SECONDARY_CAPTURE_DEVICE_SOFTWARE_VERSIONS = 0x00181019;
        
    /** (0018,101A) VR=LO, VM=1-n Hardcopy Device Software Version  */
    public static final int HARDCOPY_DEVICE_SOFTWARE_VERSION = 0x0018101A;
        
    /** (0018,101B) VR=LO, VM=1 Hardcopy Device Manufacturer's Model Name  */
    public static final int HARDCOPY_DEVICE_MANUFACTURERS_MODEL_NAME = 0x0018101B;
        
    /** (0018,1020) VR=LO, VM=1-n Software Version(s)  */
    public static final int SOFTWARE_VERSIONS = 0x00181020;
        
    /** (0018,1022) VR=SH, VM=1 Video Image Format Acquired  */
    public static final int VIDEO_IMAGE_FORMAT_ACQUIRED = 0x00181022;
        
    /** (0018,1023) VR=LO, VM=1 Digital Image Format Acquired  */
    public static final int DIGITAL_IMAGE_FORMAT_ACQUIRED = 0x00181023;
        
    /** (0018,1030) VR=LO, VM=1 Protocol Name  */
    public static final int PROTOCOL_NAME = 0x00181030;
        
    /** (0018,1040) VR=LO, VM=1 Contrast/Bolus Route  */
    public static final int CONTRAST_BOLUS_ROUTE = 0x00181040;
        
    /** (0018,1041) VR=DS, VM=1 Contrast/Bolus Volume  */
    public static final int CONTRAST_BOLUS_VOLUME = 0x00181041;
        
    /** (0018,1042) VR=TM, VM=1 Contrast/Bolus Start Time  */
    public static final int CONTRAST_BOLUS_START_TIME = 0x00181042;
        
    /** (0018,1043) VR=TM, VM=1 Contrast/Bolus Stop Time  */
    public static final int CONTRAST_BOLUS_STOP_TIME = 0x00181043;
        
    /** (0018,1044) VR=DS, VM=1 Contrast/Bolus Total Dose  */
    public static final int CONTRAST_BOLUS_TOTAL_DOSE = 0x00181044;
        
    /** (0018,1045) VR=IS, VM=1 Syringe Counts  */
    public static final int SYRINGE_COUNTS = 0x00181045;
        
    /** (0018,1046) VR=DS, VM=1-n Contrast Flow Rate  */
    public static final int CONTRAST_FLOW_RATE = 0x00181046;
        
    /** (0018,1047) VR=DS, VM=1-n Contrast Flow Duration  */
    public static final int CONTRAST_FLOW_DURATION = 0x00181047;
        
    /** (0018,1048) VR=CS, VM=1 Contrast/Bolus Ingredient  */
    public static final int CONTRAST_BOLUS_INGREDIENT = 0x00181048;
        
    /** (0018,1049) VR=DS, VM=1 Contrast/Bolus Ingredient Concentration  */
    public static final int CONTRAST_BOLUS_INGREDIENT_CONCENTRATION = 0x00181049;
        
    /** (0018,1050) VR=DS, VM=1 Spatial Resolution  */
    public static final int SPATIAL_RESOLUTION = 0x00181050;
        
    /** (0018,1060) VR=DS, VM=1 Trigger Time  */
    public static final int TRIGGER_TIME = 0x00181060;
        
    /** (0018,1061) VR=LO, VM=1 Trigger Source or Type  */
    public static final int TRIGGER_SOURCE_OR_TYPE = 0x00181061;
        
    /** (0018,1062) VR=IS, VM=1 Nominal Interval  */
    public static final int NOMINAL_INTERVAL = 0x00181062;
        
    /** (0018,1063) VR=DS, VM=1 Frame Time  */
    public static final int FRAME_TIME = 0x00181063;
        
    /** (0018,1064) VR=LO, VM=1 Framing Type  */
    public static final int FRAMING_TYPE = 0x00181064;
        
    /** (0018,1065) VR=DS, VM=1-n Frame Time Vector  */
    public static final int FRAME_TIME_VECTOR = 0x00181065;
        
    /** (0018,1066) VR=DS, VM=1 Frame Delay  */
    public static final int FRAME_DELAY = 0x00181066;
        
    /** (0018,1067) VR=DS, VM=1 Image Trigger Delay  */
    public static final int IMAGE_TRIGGER_DELAY = 0x00181067;
        
    /** (0018,1068) VR=DS, VM=1 Multiplex Group Time Offset  */
    public static final int MULTIPLEX_GROUP_TIME_OFFSET = 0x00181068;
        
    /** (0018,1069) VR=DS, VM=1 Trigger Time Offset  */
    public static final int TRIGGER_TIME_OFFSET = 0x00181069;
        
    /** (0018,106A) VR=CS, VM=1 Synchronization Trigger  */
    public static final int SYNCHRONIZATION_TRIGGER = 0x0018106A;
        
    /** (0018,106C) VR=US, VM=2 Synchronization Channel  */
    public static final int SYNCHRONIZATION_CHANNEL = 0x0018106C;
        
    /** (0018,106E) VR=UL, VM=1 Trigger Sample Position  */
    public static final int TRIGGER_SAMPLE_POSITION = 0x0018106E;
        
    /** (0018,1070) VR=LO, VM=1 Radiopharmaceutical Route  */
    public static final int RADIOPHARMACEUTICAL_ROUTE = 0x00181070;
        
    /** (0018,1071) VR=DS, VM=1 Radiopharmaceutical Volume  */
    public static final int RADIOPHARMACEUTICAL_VOLUME = 0x00181071;
        
    /** (0018,1072) VR=TM, VM=1 Radiopharmaceutical Start Time  */
    public static final int RADIOPHARMACEUTICAL_START_TIME = 0x00181072;
        
    /** (0018,1073) VR=TM, VM=1 Radiopharmaceutical Stop Time  */
    public static final int RADIOPHARMACEUTICAL_STOP_TIME = 0x00181073;
        
    /** (0018,1074) VR=DS, VM=1 Radionuclide Total Dose  */
    public static final int RADIONUCLIDE_TOTAL_DOSE = 0x00181074;
        
    /** (0018,1075) VR=DS, VM=1 Radionuclide Half Life  */
    public static final int RADIONUCLIDE_HALF_LIFE = 0x00181075;
        
    /** (0018,1076) VR=DS, VM=1 Radionuclide Positron Fraction  */
    public static final int RADIONUCLIDE_POSITRON_FRACTION = 0x00181076;
        
    /** (0018,1077) VR=DS, VM=1 Radiopharmaceutical Specific Activity  */
    public static final int RADIOPHARMACEUTICAL_SPECIFIC_ACTIVITY = 0x00181077;
        
    /** (0018,1078) VR=DT, VM=1 Radiopharmaceutical Start Datetime  */
    public static final int RADIOPHARMACEUTICAL_START_DATETIME = 0x00181078;
        
    /** (0018,1079) VR=DT, VM=1 Radiopharmaceutical Stop Datetime  */
    public static final int RADIOPHARMACEUTICAL_STOP_DATETIME = 0x00181079;
        
    /** (0018,1080) VR=CS, VM=1 Beat Rejection Flag  */
    public static final int BEAT_REJECTION_FLAG = 0x00181080;
        
    /** (0018,1081) VR=IS, VM=1 Low R-R Value  */
    public static final int LOW_R_R_VALUE = 0x00181081;
        
    /** (0018,1082) VR=IS, VM=1 High R-R Value  */
    public static final int HIGH_R_R_VALUE = 0x00181082;
        
    /** (0018,1083) VR=IS, VM=1 Intervals Acquired  */
    public static final int INTERVALS_ACQUIRED = 0x00181083;
        
    /** (0018,1084) VR=IS, VM=1 Intervals Rejected  */
    public static final int INTERVALS_REJECTED = 0x00181084;
        
    /** (0018,1085) VR=LO, VM=1 PVC Rejection  */
    public static final int PVC_REJECTION = 0x00181085;
        
    /** (0018,1086) VR=IS, VM=1 Skip Beats  */
    public static final int SKIP_BEATS = 0x00181086;
        
    /** (0018,1088) VR=IS, VM=1 Heart Rate  */
    public static final int HEART_RATE = 0x00181088;
        
    /** (0018,1090) VR=IS, VM=1 Cardiac Number of Images  */
    public static final int CARDIAC_NUMBER_OF_IMAGES = 0x00181090;
        
    /** (0018,1094) VR=IS, VM=1 Trigger Window  */
    public static final int TRIGGER_WINDOW = 0x00181094;
        
    /** (0018,1100) VR=DS, VM=1 Reconstruction Diameter  */
    public static final int RECONSTRUCTION_DIAMETER = 0x00181100;
        
    /** (0018,1110) VR=DS, VM=1 Distance Source to Detector  */
    public static final int DISTANCE_SOURCE_TO_DETECTOR = 0x00181110;
        
    /** (0018,1111) VR=DS, VM=1 Distance Source to Patient  */
    public static final int DISTANCE_SOURCE_TO_PATIENT = 0x00181111;
        
    /** (0018,1114) VR=DS, VM=1 Estimated Radiographic Magnification Factor  */
    public static final int ESTIMATED_RADIOGRAPHIC_MAGNIFICATION_FACTOR = 0x00181114;
        
    /** (0018,1120) VR=DS, VM=1 Gantry/Detector Tilt  */
    public static final int GANTRY_DETECTOR_TILT = 0x00181120;
        
    /** (0018,1121) VR=DS, VM=1 Gantry/Detector Slew  */
    public static final int GANTRY_DETECTOR_SLEW = 0x00181121;
        
    /** (0018,1130) VR=DS, VM=1 Table Height  */
    public static final int TABLE_HEIGHT = 0x00181130;
        
    /** (0018,1131) VR=DS, VM=1 Table Traverse  */
    public static final int TABLE_TRAVERSE = 0x00181131;
        
    /** (0018,1134) VR=CS, VM=1 Table Motion  */
    public static final int TABLE_MOTION = 0x00181134;
        
    /** (0018,1135) VR=DS, VM=1-n Table Vertical Increment  */
    public static final int TABLE_VERTICAL_INCREMENT = 0x00181135;
        
    /** (0018,1136) VR=DS, VM=1-n Table Lateral Increment  */
    public static final int TABLE_LATERAL_INCREMENT = 0x00181136;
        
    /** (0018,1137) VR=DS, VM=1-n Table Longitudinal Increment  */
    public static final int TABLE_LONGITUDINAL_INCREMENT = 0x00181137;
        
    /** (0018,1138) VR=DS, VM=1 Table Angle  */
    public static final int TABLE_ANGLE = 0x00181138;
        
    /** (0018,113A) VR=CS, VM=1 Table Type  */
    public static final int TABLE_TYPE = 0x0018113A;
        
    /** (0018,1140) VR=CS, VM=1 Rotation Direction  */
    public static final int ROTATION_DIRECTION = 0x00181140;
        
    /** (0018,1141) VR=DS, VM=1 Angular Position  */
    public static final int ANGULAR_POSITION = 0x00181141;
        
    /** (0018,1142) VR=DS, VM=1-n Radial Position  */
    public static final int RADIAL_POSITION = 0x00181142;
        
    /** (0018,1143) VR=DS, VM=1 Scan Arc  */
    public static final int SCAN_ARC = 0x00181143;
        
    /** (0018,1144) VR=DS, VM=1 Angular Step  */
    public static final int ANGULAR_STEP = 0x00181144;
        
    /** (0018,1145) VR=DS, VM=1 Center of Rotation Offset  */
    public static final int CENTER_OF_ROTATION_OFFSET = 0x00181145;
        
    /** (0018,1146) VR=DS, VM=1-n Rotation Offset RET */
    public static final int ROTATION_OFFSET_RET = 0x00181146;
        
    /** (0018,1147) VR=CS, VM=1 Field of View Shape  */
    public static final int FIELD_OF_VIEW_SHAPE = 0x00181147;
        
    /** (0018,1149) VR=IS, VM=1-2 Field of View Dimension(s)  */
    public static final int FIELD_OF_VIEW_DIMENSIONS = 0x00181149;
        
    /** (0018,1150) VR=IS, VM=1 Exposure Time  */
    public static final int EXPOSURE_TIME = 0x00181150;
        
    /** (0018,1151) VR=IS, VM=1 X-ray Tube Current  */
    public static final int X_RAY_TUBE_CURRENT = 0x00181151;
        
    /** (0018,1152) VR=IS, VM=1 Exposure  */
    public static final int EXPOSURE = 0x00181152;
        
    /** (0018,1153) VR=IS, VM=1 Exposure in uAs  */
    public static final int EXPOSURE_IN_UAS = 0x00181153;
        
    /** (0018,1154) VR=DS, VM=1 Average Pulse Width  */
    public static final int AVERAGE_PULSE_WIDTH = 0x00181154;
        
    /** (0018,1155) VR=CS, VM=1 Radiation Setting  */
    public static final int RADIATION_SETTING = 0x00181155;
        
    /** (0018,1156) VR=CS, VM=1 Rectification Type  */
    public static final int RECTIFICATION_TYPE = 0x00181156;
        
    /** (0018,115A) VR=CS, VM=1 Radiation Mode  */
    public static final int RADIATION_MODE = 0x0018115A;
        
    /** (0018,115E) VR=DS, VM=1 Image and Fluoroscopy Area Dose Product  */
    public static final int IMAGE_AND_FLUOROSCOPY_AREA_DOSE_PRODUCT = 0x0018115E;
        
    /** (0018,1160) VR=SH, VM=1 Filter Type  */
    public static final int FILTER_TYPE = 0x00181160;
        
    /** (0018,1161) VR=LO, VM=1-n Type of Filters  */
    public static final int TYPE_OF_FILTERS = 0x00181161;
        
    /** (0018,1162) VR=DS, VM=1 Intensifier Size  */
    public static final int INTENSIFIER_SIZE = 0x00181162;
        
    /** (0018,1164) VR=DS, VM=2 Imager Pixel Spacing  */
    public static final int IMAGER_PIXEL_SPACING = 0x00181164;
        
    /** (0018,1166) VR=CS, VM=1-n Grid  */
    public static final int GRID = 0x00181166;
        
    /** (0018,1170) VR=IS, VM=1 Generator Power  */
    public static final int GENERATOR_POWER = 0x00181170;
        
    /** (0018,1180) VR=SH, VM=1 Collimator/grid Name  */
    public static final int COLLIMATOR_GRID_NAME = 0x00181180;
        
    /** (0018,1181) VR=CS, VM=1 Collimator Type  */
    public static final int COLLIMATOR_TYPE = 0x00181181;
        
    /** (0018,1182) VR=IS, VM=1-2 Focal Distance  */
    public static final int FOCAL_DISTANCE = 0x00181182;
        
    /** (0018,1183) VR=DS, VM=1-2 X Focus Center  */
    public static final int X_FOCUS_CENTER = 0x00181183;
        
    /** (0018,1184) VR=DS, VM=1-2 Y Focus Center  */
    public static final int Y_FOCUS_CENTER = 0x00181184;
        
    /** (0018,1190) VR=DS, VM=1-n Focal Spot(s)  */
    public static final int FOCAL_SPOTS = 0x00181190;
        
    /** (0018,1191) VR=CS, VM=1 Anode Target Material  */
    public static final int ANODE_TARGET_MATERIAL = 0x00181191;
        
    /** (0018,11A0) VR=DS, VM=1 Body Part Thickness  */
    public static final int BODY_PART_THICKNESS = 0x001811A0;
        
    /** (0018,11A2) VR=DS, VM=1 Compression Force  */
    public static final int COMPRESSION_FORCE = 0x001811A2;
        
    /** (0018,1200) VR=DA, VM=1-n Date of Last Calibration  */
    public static final int DATE_OF_LAST_CALIBRATION = 0x00181200;
        
    /** (0018,1201) VR=TM, VM=1-n Time of Last Calibration  */
    public static final int TIME_OF_LAST_CALIBRATION = 0x00181201;
        
    /** (0018,1210) VR=SH, VM=1-n Convolution Kernel  */
    public static final int CONVOLUTION_KERNEL = 0x00181210;
        
    /** (0018,1240) VR=IS, VM=1-n Upper/Lower Pixel Values RET */
    public static final int UPPER_LOWER_PIXEL_VALUES_RET = 0x00181240;
        
    /** (0018,1242) VR=IS, VM=1 Actual Frame Duration  */
    public static final int ACTUAL_FRAME_DURATION = 0x00181242;
        
    /** (0018,1243) VR=IS, VM=1 Count Rate  */
    public static final int COUNT_RATE = 0x00181243;
        
    /** (0018,1244) VR=US, VM=1 Preferred Playback Sequencing  */
    public static final int PREFERRED_PLAYBACK_SEQUENCING = 0x00181244;
        
    /** (0018,1250) VR=SH, VM=1 Receive Coil Name  */
    public static final int RECEIVE_COIL_NAME = 0x00181250;
        
    /** (0018,1251) VR=SH, VM=1 Transmit Coil Name  */
    public static final int TRANSMIT_COIL_NAME = 0x00181251;
        
    /** (0018,1260) VR=SH, VM=1 Plate Type  */
    public static final int PLATE_TYPE = 0x00181260;
        
    /** (0018,1261) VR=LO, VM=1 Phosphor Type  */
    public static final int PHOSPHOR_TYPE = 0x00181261;
        
    /** (0018,1300) VR=DS, VM=1 Scan Velocity  */
    public static final int SCAN_VELOCITY = 0x00181300;
        
    /** (0018,1301) VR=CS, VM=1-n Whole Body Technique  */
    public static final int WHOLE_BODY_TECHNIQUE = 0x00181301;
        
    /** (0018,1302) VR=IS, VM=1 Scan Length  */
    public static final int SCAN_LENGTH = 0x00181302;
        
    /** (0018,1310) VR=US, VM=4 Acquisition Matrix  */
    public static final int ACQUISITION_MATRIX = 0x00181310;
        
    /** (0018,1312) VR=CS, VM=1 In-plane Phase Encoding Direction  */
    public static final int IN_PLANE_PHASE_ENCODING_DIRECTION = 0x00181312;
        
    /** (0018,1314) VR=DS, VM=1 Flip Angle  */
    public static final int FLIP_ANGLE = 0x00181314;
        
    /** (0018,1315) VR=CS, VM=1 Variable Flip Angle Flag  */
    public static final int VARIABLE_FLIP_ANGLE_FLAG = 0x00181315;
        
    /** (0018,1316) VR=DS, VM=1 SAR  */
    public static final int SAR = 0x00181316;
        
    /** (0018,1318) VR=DS, VM=1 dB/dt  */
    public static final int DB_DT = 0x00181318;
        
    /** (0018,1400) VR=LO, VM=1 Acquisition Device Processing Description  */
    public static final int ACQUISITION_DEVICE_PROCESSING_DESCRIPTION = 0x00181400;
        
    /** (0018,1401) VR=LO, VM=1 Acquisition Device Processing Code  */
    public static final int ACQUISITION_DEVICE_PROCESSING_CODE = 0x00181401;
        
    /** (0018,1402) VR=CS, VM=1 Cassette Orientation  */
    public static final int CASSETTE_ORIENTATION = 0x00181402;
        
    /** (0018,1403) VR=CS, VM=1 Cassette Size  */
    public static final int CASSETTE_SIZE = 0x00181403;
        
    /** (0018,1404) VR=US, VM=1 Exposures on Plate  */
    public static final int EXPOSURES_ON_PLATE = 0x00181404;
        
    /** (0018,1405) VR=IS, VM=1 Relative X-ray Exposure  */
    public static final int RELATIVE_X_RAY_EXPOSURE = 0x00181405;
        
    /** (0018,1450) VR=DS, VM=1 Column Angulation  */
    public static final int COLUMN_ANGULATION = 0x00181450;
        
    /** (0018,1460) VR=DS, VM=1 Tomo Layer Height  */
    public static final int TOMO_LAYER_HEIGHT = 0x00181460;
        
    /** (0018,1470) VR=DS, VM=1 Tomo Angle  */
    public static final int TOMO_ANGLE = 0x00181470;
        
    /** (0018,1480) VR=DS, VM=1 Tomo Time  */
    public static final int TOMO_TIME = 0x00181480;
        
    /** (0018,1490) VR=CS, VM=1 Tomo Type  */
    public static final int TOMO_TYPE = 0x00181490;
        
    /** (0018,1491) VR=CS, VM=1 Tomo Class  */
    public static final int TOMO_CLASS = 0x00181491;
        
    /** (0018,1495) VR=IS, VM=1 Number of Tomosynthesis Source Images  */
    public static final int NUMBER_OF_TOMOSYNTHESIS_SOURCE_IMAGES = 0x00181495;
        
    /** (0018,1500) VR=CS, VM=1 Positioner Motion  */
    public static final int POSITIONER_MOTION = 0x00181500;
        
    /** (0018,1508) VR=CS, VM=1 Positioner Type  */
    public static final int POSITIONER_TYPE = 0x00181508;
        
    /** (0018,1510) VR=DS, VM=1 Positioner Primary Angle  */
    public static final int POSITIONER_PRIMARY_ANGLE = 0x00181510;
        
    /** (0018,1511) VR=DS, VM=1 Positioner Secondary Angle  */
    public static final int POSITIONER_SECONDARY_ANGLE = 0x00181511;
        
    /** (0018,1520) VR=DS, VM=1-n Positioner Primary Angle Increment  */
    public static final int POSITIONER_PRIMARY_ANGLE_INCREMENT = 0x00181520;
        
    /** (0018,1521) VR=DS, VM=1-n Positioner Secondary Angle Increment  */
    public static final int POSITIONER_SECONDARY_ANGLE_INCREMENT = 0x00181521;
        
    /** (0018,1530) VR=DS, VM=1 Detector Primary Angle  */
    public static final int DETECTOR_PRIMARY_ANGLE = 0x00181530;
        
    /** (0018,1531) VR=DS, VM=1 Detector Secondary Angle  */
    public static final int DETECTOR_SECONDARY_ANGLE = 0x00181531;
        
    /** (0018,1600) VR=CS, VM=1-3 Shutter Shape  */
    public static final int SHUTTER_SHAPE = 0x00181600;
        
    /** (0018,1602) VR=IS, VM=1 Shutter Left Vertical Edge  */
    public static final int SHUTTER_LEFT_VERTICAL_EDGE = 0x00181602;
        
    /** (0018,1604) VR=IS, VM=1 Shutter Right Vertical Edge  */
    public static final int SHUTTER_RIGHT_VERTICAL_EDGE = 0x00181604;
        
    /** (0018,1606) VR=IS, VM=1 Shutter Upper Horizontal Edge  */
    public static final int SHUTTER_UPPER_HORIZONTAL_EDGE = 0x00181606;
        
    /** (0018,1608) VR=IS, VM=1 Shutter Lower Horizontal Edge  */
    public static final int SHUTTER_LOWER_HORIZONTAL_EDGE = 0x00181608;
        
    /** (0018,1610) VR=IS, VM=2 Center of Circular Shutter  */
    public static final int CENTER_OF_CIRCULAR_SHUTTER = 0x00181610;
        
    /** (0018,1612) VR=IS, VM=1 Radius of Circular Shutter  */
    public static final int RADIUS_OF_CIRCULAR_SHUTTER = 0x00181612;
        
    /** (0018,1620) VR=IS, VM=2-2n Vertices of the Polygonal Shutter  */
    public static final int VERTICES_OF_THE_POLYGONAL_SHUTTER = 0x00181620;
        
    /** (0018,1622) VR=US, VM=1 Shutter Presentation Value  */
    public static final int SHUTTER_PRESENTATION_VALUE = 0x00181622;
        
    /** (0018,1623) VR=US, VM=1 Shutter Overlay Group  */
    public static final int SHUTTER_OVERLAY_GROUP = 0x00181623;
        
    /** (0018,1624) VR=US, VM=3 Shutter Presentation Color CIELab Value  */
    public static final int SHUTTER_PRESENTATION_COLOR_CIELAB_VALUE = 0x00181624;
        
    /** (0018,1700) VR=CS, VM=1-3 Collimator Shape  */
    public static final int COLLIMATOR_SHAPE = 0x00181700;
        
    /** (0018,1702) VR=IS, VM=1 Collimator Left Vertical Edge  */
    public static final int COLLIMATOR_LEFT_VERTICAL_EDGE = 0x00181702;
        
    /** (0018,1704) VR=IS, VM=1 Collimator Right Vertical Edge  */
    public static final int COLLIMATOR_RIGHT_VERTICAL_EDGE = 0x00181704;
        
    /** (0018,1706) VR=IS, VM=1 Collimator Upper Horizontal Edge  */
    public static final int COLLIMATOR_UPPER_HORIZONTAL_EDGE = 0x00181706;
        
    /** (0018,1708) VR=IS, VM=1 Collimator Lower Horizontal Edge  */
    public static final int COLLIMATOR_LOWER_HORIZONTAL_EDGE = 0x00181708;
        
    /** (0018,1710) VR=IS, VM=2 Center of Circular Collimator  */
    public static final int CENTER_OF_CIRCULAR_COLLIMATOR = 0x00181710;
        
    /** (0018,1712) VR=IS, VM=1 Radius of Circular Collimator  */
    public static final int RADIUS_OF_CIRCULAR_COLLIMATOR = 0x00181712;
        
    /** (0018,1720) VR=IS, VM=2-2n Vertices of the Polygonal Collimator  */
    public static final int VERTICES_OF_THE_POLYGONAL_COLLIMATOR = 0x00181720;
        
    /** (0018,1800) VR=CS, VM=1 Acquisition Time Synchronized  */
    public static final int ACQUISITION_TIME_SYNCHRONIZED = 0x00181800;
        
    /** (0018,1801) VR=SH, VM=1 Time Source  */
    public static final int TIME_SOURCE = 0x00181801;
        
    /** (0018,1802) VR=CS, VM=1 Time Distribution Protocol  */
    public static final int TIME_DISTRIBUTION_PROTOCOL = 0x00181802;
        
    /** (0018,1803) VR=LO, VM=1 NTP Source Address  */
    public static final int NTP_SOURCE_ADDRESS = 0x00181803;
        
    /** (0018,2001) VR=IS, VM=1-n Page Number Vector  */
    public static final int PAGE_NUMBER_VECTOR = 0x00182001;
        
    /** (0018,2002) VR=SH, VM=1-n Frame Label Vector  */
    public static final int FRAME_LABEL_VECTOR = 0x00182002;
        
    /** (0018,2003) VR=DS, VM=1-n Frame Primary Angle Vector  */
    public static final int FRAME_PRIMARY_ANGLE_VECTOR = 0x00182003;
        
    /** (0018,2004) VR=DS, VM=1-n Frame Secondary Angle Vector  */
    public static final int FRAME_SECONDARY_ANGLE_VECTOR = 0x00182004;
        
    /** (0018,2005) VR=DS, VM=1-n Slice Location Vector  */
    public static final int SLICE_LOCATION_VECTOR = 0x00182005;
        
    /** (0018,2006) VR=SH, VM=1-n Display Window Label Vector  */
    public static final int DISPLAY_WINDOW_LABEL_VECTOR = 0x00182006;
        
    /** (0018,2010) VR=DS, VM=2 Nominal Scanned Pixel Spacing  */
    public static final int NOMINAL_SCANNED_PIXEL_SPACING = 0x00182010;
        
    /** (0018,2020) VR=CS, VM=1 Digitizing Device Transport Direction  */
    public static final int DIGITIZING_DEVICE_TRANSPORT_DIRECTION = 0x00182020;
        
    /** (0018,2030) VR=DS, VM=1 Rotation of Scanned Film  */
    public static final int ROTATION_OF_SCANNED_FILM = 0x00182030;
        
    /** (0018,3100) VR=CS, VM=1 IVUS Acquisition  */
    public static final int IVUS_ACQUISITION = 0x00183100;
        
    /** (0018,3101) VR=DS, VM=1 IVUS Pullback Rate  */
    public static final int IVUS_PULLBACK_RATE = 0x00183101;
        
    /** (0018,3102) VR=DS, VM=1 IVUS Gated Rate  */
    public static final int IVUS_GATED_RATE = 0x00183102;
        
    /** (0018,3103) VR=IS, VM=1 IVUS Pullback Start Frame Number  */
    public static final int IVUS_PULLBACK_START_FRAME_NUMBER = 0x00183103;
        
    /** (0018,3104) VR=IS, VM=1 IVUS Pullback Stop Frame Number  */
    public static final int IVUS_PULLBACK_STOP_FRAME_NUMBER = 0x00183104;
        
    /** (0018,3105) VR=IS, VM=1-n Lesion Number  */
    public static final int LESION_NUMBER = 0x00183105;
        
    /** (0018,4000) VR=LT, VM=1 Acquisition Comments RET */
    public static final int ACQUISITION_COMMENTS_RET = 0x00184000;
        
    /** (0018,5000) VR=SH, VM=1-n Output Power  */
    public static final int OUTPUT_POWER = 0x00185000;
        
    /** (0018,5010) VR=LO, VM=3 Transducer Data  */
    public static final int TRANSDUCER_DATA = 0x00185010;
        
    /** (0018,5012) VR=DS, VM=1 Focus Depth  */
    public static final int FOCUS_DEPTH = 0x00185012;
        
    /** (0018,5020) VR=LO, VM=1 Processing Function  */
    public static final int PROCESSING_FUNCTION = 0x00185020;
        
    /** (0018,5021) VR=LO, VM=1 Postprocessing Function  */
    public static final int POSTPROCESSING_FUNCTION = 0x00185021;
        
    /** (0018,5022) VR=DS, VM=1 Mechanical Index  */
    public static final int MECHANICAL_INDEX = 0x00185022;
        
    /** (0018,5024) VR=DS, VM=1 Bone Thermal Index  */
    public static final int BONE_THERMAL_INDEX = 0x00185024;
        
    /** (0018,5026) VR=DS, VM=1 Cranial Thermal Index  */
    public static final int CRANIAL_THERMAL_INDEX = 0x00185026;
        
    /** (0018,5027) VR=DS, VM=1 Soft Tissue Thermal Index  */
    public static final int SOFT_TISSUE_THERMAL_INDEX = 0x00185027;
        
    /** (0018,5028) VR=DS, VM=1 Soft Tissue-focus Thermal Index  */
    public static final int SOFT_TISSUE_FOCUS_THERMAL_INDEX = 0x00185028;
        
    /** (0018,5029) VR=DS, VM=1 Soft Tissue-surface Thermal Index  */
    public static final int SOFT_TISSUE_SURFACE_THERMAL_INDEX = 0x00185029;
        
    /** (0018,5030) VR=DS, VM=1 Dynamic Range RET */
    public static final int DYNAMIC_RANGE_RET = 0x00185030;
        
    /** (0018,5040) VR=DS, VM=1 Total Gain RET */
    public static final int TOTAL_GAIN_RET = 0x00185040;
        
    /** (0018,5050) VR=IS, VM=1 Depth of Scan Field  */
    public static final int DEPTH_OF_SCAN_FIELD = 0x00185050;
        
    /** (0018,5100) VR=CS, VM=1 Patient Position  */
    public static final int PATIENT_POSITION = 0x00185100;
        
    /** (0018,5101) VR=CS, VM=1 View Position  */
    public static final int VIEW_POSITION = 0x00185101;
        
    /** (0018,5104) VR=SQ, VM=1 Projection Eponymous Name Code Sequence  */
    public static final int PROJECTION_EPONYMOUS_NAME_CODE_SEQUENCE = 0x00185104;
        
    /** (0018,5210) VR=DS, VM=6 Image Transformation Matrix RET */
    public static final int IMAGE_TRANSFORMATION_MATRIX_RET = 0x00185210;
        
    /** (0018,5212) VR=DS, VM=3 Image Translation Vector RET */
    public static final int IMAGE_TRANSLATION_VECTOR_RET = 0x00185212;
        
    /** (0018,6000) VR=DS, VM=1 Sensitivity  */
    public static final int SENSITIVITY = 0x00186000;
        
    /** (0018,6011) VR=SQ, VM=1 Sequence of Ultrasound Regions  */
    public static final int SEQUENCE_OF_ULTRASOUND_REGIONS = 0x00186011;
        
    /** (0018,6012) VR=US, VM=1 Region Spatial Format  */
    public static final int REGION_SPATIAL_FORMAT = 0x00186012;
        
    /** (0018,6014) VR=US, VM=1 Region Data Type  */
    public static final int REGION_DATA_TYPE = 0x00186014;
        
    /** (0018,6016) VR=UL, VM=1 Region Flags  */
    public static final int REGION_FLAGS = 0x00186016;
        
    /** (0018,6018) VR=UL, VM=1 Region Location Min X0  */
    public static final int REGION_LOCATION_MIN_X0 = 0x00186018;
        
    /** (0018,601A) VR=UL, VM=1 Region Location Min Y0  */
    public static final int REGION_LOCATION_MIN_Y0 = 0x0018601A;
        
    /** (0018,601C) VR=UL, VM=1 Region Location Max X1  */
    public static final int REGION_LOCATION_MAX_X1 = 0x0018601C;
        
    /** (0018,601E) VR=UL, VM=1 Region Location Max Y1  */
    public static final int REGION_LOCATION_MAX_Y1 = 0x0018601E;
        
    /** (0018,6020) VR=SL, VM=1 Reference Pixel X0  */
    public static final int REFERENCE_PIXEL_X0 = 0x00186020;
        
    /** (0018,6022) VR=SL, VM=1 Reference Pixel Y0  */
    public static final int REFERENCE_PIXEL_Y0 = 0x00186022;
        
    /** (0018,6024) VR=US, VM=1 Physical Units X Direction  */
    public static final int PHYSICAL_UNITS_X_DIRECTION = 0x00186024;
        
    /** (0018,6026) VR=US, VM=1 Physical Units Y Direction  */
    public static final int PHYSICAL_UNITS_Y_DIRECTION = 0x00186026;
        
    /** (0018,6028) VR=FD, VM=1 Reference Pixel Physical Value X  */
    public static final int REFERENCE_PIXEL_PHYSICAL_VALUE_X = 0x00186028;
        
    /** (0018,602A) VR=FD, VM=1 Reference Pixel Physical Value Y  */
    public static final int REFERENCE_PIXEL_PHYSICAL_VALUE_Y = 0x0018602A;
        
    /** (0018,602C) VR=FD, VM=1 Physical Delta X  */
    public static final int PHYSICAL_DELTA_X = 0x0018602C;
        
    /** (0018,602E) VR=FD, VM=1 Physical Delta Y  */
    public static final int PHYSICAL_DELTA_Y = 0x0018602E;
        
    /** (0018,6030) VR=UL, VM=1 Transducer Frequency  */
    public static final int TRANSDUCER_FREQUENCY = 0x00186030;
        
    /** (0018,6031) VR=CS, VM=1 Transducer Type  */
    public static final int TRANSDUCER_TYPE = 0x00186031;
        
    /** (0018,6032) VR=UL, VM=1 Pulse Repetition Frequency  */
    public static final int PULSE_REPETITION_FREQUENCY = 0x00186032;
        
    /** (0018,6034) VR=FD, VM=1 Doppler Correction Angle  */
    public static final int DOPPLER_CORRECTION_ANGLE = 0x00186034;
        
    /** (0018,6036) VR=FD, VM=1 Steering Angle  */
    public static final int STEERING_ANGLE = 0x00186036;
        
    /** (0018,6038) VR=UL, VM=1 Doppler Sample Volume X Position RET */
    public static final int DOPPLER_SAMPLE_VOLUME_X_POSITION_RET = 0x00186038;
        
    /** (0018,6039) VR=SL, VM=1 Doppler Sample Volume X Position  */
    public static final int DOPPLER_SAMPLE_VOLUME_X_POSITION = 0x00186039;
        
    /** (0018,603A) VR=UL, VM=1 Doppler Sample Volume Y Position RET */
    public static final int DOPPLER_SAMPLE_VOLUME_Y_POSITION_RET = 0x0018603A;
        
    /** (0018,603B) VR=SL, VM=1 Doppler Sample Volume Y Position  */
    public static final int DOPPLER_SAMPLE_VOLUME_Y_POSITION = 0x0018603B;
        
    /** (0018,603C) VR=UL, VM=1 TM-Line Position X0 RET */
    public static final int TM_LINE_POSITION_X0_RET = 0x0018603C;
        
    /** (0018,603D) VR=SL, VM=1 TM-Line Position X0  */
    public static final int TM_LINE_POSITION_X0 = 0x0018603D;
        
    /** (0018,603E) VR=UL, VM=1 TM-Line Position Y0 RET */
    public static final int TM_LINE_POSITION_Y0_RET = 0x0018603E;
        
    /** (0018,603F) VR=SL, VM=1 TM-Line Position Y0  */
    public static final int TM_LINE_POSITION_Y0 = 0x0018603F;
        
    /** (0018,6040) VR=UL, VM=1 TM-Line Position X1 RET */
    public static final int TM_LINE_POSITION_X1_RET = 0x00186040;
        
    /** (0018,6041) VR=SL, VM=1 TM-Line Position X1  */
    public static final int TM_LINE_POSITION_X1 = 0x00186041;
        
    /** (0018,6042) VR=UL, VM=1 TM-Line Position Y1 RET */
    public static final int TM_LINE_POSITION_Y1_RET = 0x00186042;
        
    /** (0018,6043) VR=SL, VM=1 TM-Line Position Y1  */
    public static final int TM_LINE_POSITION_Y1 = 0x00186043;
        
    /** (0018,6044) VR=US, VM=1 Pixel Component Organization  */
    public static final int PIXEL_COMPONENT_ORGANIZATION = 0x00186044;
        
    /** (0018,6046) VR=UL, VM=1 Pixel Component Mask  */
    public static final int PIXEL_COMPONENT_MASK = 0x00186046;
        
    /** (0018,6048) VR=UL, VM=1 Pixel Component Range Start  */
    public static final int PIXEL_COMPONENT_RANGE_START = 0x00186048;
        
    /** (0018,604A) VR=UL, VM=1 Pixel Component Range Stop  */
    public static final int PIXEL_COMPONENT_RANGE_STOP = 0x0018604A;
        
    /** (0018,604C) VR=US, VM=1 Pixel Component Physical Units  */
    public static final int PIXEL_COMPONENT_PHYSICAL_UNITS = 0x0018604C;
        
    /** (0018,604E) VR=US, VM=1 Pixel Component Data Type  */
    public static final int PIXEL_COMPONENT_DATA_TYPE = 0x0018604E;
        
    /** (0018,6050) VR=UL, VM=1 Number of Table Break Points  */
    public static final int NUMBER_OF_TABLE_BREAK_POINTS = 0x00186050;
        
    /** (0018,6052) VR=UL, VM=1-n Table of X Break Points  */
    public static final int TABLE_OF_X_BREAK_POINTS = 0x00186052;
        
    /** (0018,6054) VR=FD, VM=1-n Table of Y Break Points  */
    public static final int TABLE_OF_Y_BREAK_POINTS = 0x00186054;
        
    /** (0018,6056) VR=UL, VM=1 Number of Table Entries  */
    public static final int NUMBER_OF_TABLE_ENTRIES = 0x00186056;
        
    /** (0018,6058) VR=UL, VM=1-n Table of Pixel Values  */
    public static final int TABLE_OF_PIXEL_VALUES = 0x00186058;
        
    /** (0018,605A) VR=FL, VM=1-n Table of Parameter Values  */
    public static final int TABLE_OF_PARAMETER_VALUES = 0x0018605A;
        
    /** (0018,6060) VR=FL, VM=1-n R Wave Time Vector  */
    public static final int R_WAVE_TIME_VECTOR = 0x00186060;
        
    /** (0018,7000) VR=CS, VM=1 Detector Conditions Nominal Flag  */
    public static final int DETECTOR_CONDITIONS_NOMINAL_FLAG = 0x00187000;
        
    /** (0018,7001) VR=DS, VM=1 Detector Temperature  */
    public static final int DETECTOR_TEMPERATURE = 0x00187001;
        
    /** (0018,7004) VR=CS, VM=1 Detector Type  */
    public static final int DETECTOR_TYPE = 0x00187004;
        
    /** (0018,7005) VR=CS, VM=1 Detector Configuration  */
    public static final int DETECTOR_CONFIGURATION = 0x00187005;
        
    /** (0018,7006) VR=LT, VM=1 Detector Description  */
    public static final int DETECTOR_DESCRIPTION = 0x00187006;
        
    /** (0018,7008) VR=LT, VM=1 Detector Mode  */
    public static final int DETECTOR_MODE = 0x00187008;
        
    /** (0018,700A) VR=SH, VM=1 Detector ID  */
    public static final int DETECTOR_ID = 0x0018700A;
        
    /** (0018,700C) VR=DA, VM=1 Date of Last Detector Calibration  */
    public static final int DATE_OF_LAST_DETECTOR_CALIBRATION = 0x0018700C;
        
    /** (0018,700E) VR=TM, VM=1 Time of Last Detector Calibration  */
    public static final int TIME_OF_LAST_DETECTOR_CALIBRATION = 0x0018700E;
        
    /** (0018,7010) VR=IS, VM=1 Exposures on Detector Since Last Calibration  */
    public static final int EXPOSURES_ON_DETECTOR_SINCE_LAST_CALIBRATION = 0x00187010;
        
    /** (0018,7011) VR=IS, VM=1 Exposures on Detector Since Manufactured  */
    public static final int EXPOSURES_ON_DETECTOR_SINCE_MANUFACTURED = 0x00187011;
        
    /** (0018,7012) VR=DS, VM=1 Detector Time Since Last Exposure  */
    public static final int DETECTOR_TIME_SINCE_LAST_EXPOSURE = 0x00187012;
        
    /** (0018,7014) VR=DS, VM=1 Detector Active Time  */
    public static final int DETECTOR_ACTIVE_TIME = 0x00187014;
        
    /** (0018,7016) VR=DS, VM=1 Detector Activation Offset From Exposure  */
    public static final int DETECTOR_ACTIVATION_OFFSET_FROM_EXPOSURE = 0x00187016;
        
    /** (0018,701A) VR=DS, VM=2 Detector Binning  */
    public static final int DETECTOR_BINNING = 0x0018701A;
        
    /** (0018,7020) VR=DS, VM=2 Detector Element Physical Size  */
    public static final int DETECTOR_ELEMENT_PHYSICAL_SIZE = 0x00187020;
        
    /** (0018,7022) VR=DS, VM=2 Detector Element Spacing  */
    public static final int DETECTOR_ELEMENT_SPACING = 0x00187022;
        
    /** (0018,7024) VR=CS, VM=1 Detector Active Shape  */
    public static final int DETECTOR_ACTIVE_SHAPE = 0x00187024;
        
    /** (0018,7026) VR=DS, VM=1-2 Detector Active Dimension(s)  */
    public static final int DETECTOR_ACTIVE_DIMENSIONS = 0x00187026;
        
    /** (0018,7028) VR=DS, VM=2 Detector Active Origin  */
    public static final int DETECTOR_ACTIVE_ORIGIN = 0x00187028;
        
    /** (0018,702A) VR=LO, VM=1 Detector Manufacturer Name  */
    public static final int DETECTOR_MANUFACTURER_NAME = 0x0018702A;
        
    /** (0018,702B) VR=LO, VM=1 Detector Manufacturer's Model Name  */
    public static final int DETECTOR_MANUFACTURERS_MODEL_NAME = 0x0018702B;
        
    /** (0018,7030) VR=DS, VM=2 Field of View Origin  */
    public static final int FIELD_OF_VIEW_ORIGIN = 0x00187030;
        
    /** (0018,7032) VR=DS, VM=1 Field of View Rotation  */
    public static final int FIELD_OF_VIEW_ROTATION = 0x00187032;
        
    /** (0018,7034) VR=CS, VM=1 Field of View Horizontal Flip  */
    public static final int FIELD_OF_VIEW_HORIZONTAL_FLIP = 0x00187034;
        
    /** (0018,7040) VR=LT, VM=1 Grid Absorbing Material  */
    public static final int GRID_ABSORBING_MATERIAL = 0x00187040;
        
    /** (0018,7041) VR=LT, VM=1 Grid Spacing Material  */
    public static final int GRID_SPACING_MATERIAL = 0x00187041;
        
    /** (0018,7042) VR=DS, VM=1 Grid Thickness  */
    public static final int GRID_THICKNESS = 0x00187042;
        
    /** (0018,7044) VR=DS, VM=1 Grid Pitch  */
    public static final int GRID_PITCH = 0x00187044;
        
    /** (0018,7046) VR=IS, VM=2 Grid Aspect Ratio  */
    public static final int GRID_ASPECT_RATIO = 0x00187046;
        
    /** (0018,7048) VR=DS, VM=1 Grid Period  */
    public static final int GRID_PERIOD = 0x00187048;
        
    /** (0018,704C) VR=DS, VM=1 Grid Focal Distance  */
    public static final int GRID_FOCAL_DISTANCE = 0x0018704C;
        
    /** (0018,7050) VR=CS, VM=1-n Filter Material  */
    public static final int FILTER_MATERIAL = 0x00187050;
        
    /** (0018,7052) VR=DS, VM=1-n Filter Thickness Minimum  */
    public static final int FILTER_THICKNESS_MINIMUM = 0x00187052;
        
    /** (0018,7054) VR=DS, VM=1-n Filter Thickness Maximum  */
    public static final int FILTER_THICKNESS_MAXIMUM = 0x00187054;
        
    /** (0018,7060) VR=CS, VM=1 Exposure Control Mode  */
    public static final int EXPOSURE_CONTROL_MODE = 0x00187060;
        
    /** (0018,7062) VR=LT, VM=1 Exposure Control Mode Description  */
    public static final int EXPOSURE_CONTROL_MODE_DESCRIPTION = 0x00187062;
        
    /** (0018,7064) VR=CS, VM=1 Exposure Status  */
    public static final int EXPOSURE_STATUS = 0x00187064;
        
    /** (0018,7065) VR=DS, VM=1 Phototimer Setting  */
    public static final int PHOTOTIMER_SETTING = 0x00187065;
        
    /** (0018,8150) VR=DS, VM=1 Exposure Time in uS  */
    public static final int EXPOSURE_TIME_IN_US = 0x00188150;
        
    /** (0018,8151) VR=DS, VM=1 X-Ray Tube Current in uA  */
    public static final int X_RAY_TUBE_CURRENT_IN_UA = 0x00188151;
        
    /** (0018,9004) VR=CS, VM=1 Content Qualification  */
    public static final int CONTENT_QUALIFICATION = 0x00189004;
        
    /** (0018,9005) VR=SH, VM=1 Pulse Sequence Name  */
    public static final int PULSE_SEQUENCE_NAME = 0x00189005;
        
    /** (0018,9006) VR=SQ, VM=1 MR Imaging Modifier Sequence  */
    public static final int MR_IMAGING_MODIFIER_SEQUENCE = 0x00189006;
        
    /** (0018,9008) VR=CS, VM=1 Echo Pulse Sequence  */
    public static final int ECHO_PULSE_SEQUENCE = 0x00189008;
        
    /** (0018,9009) VR=CS, VM=1 Inversion Recovery  */
    public static final int INVERSION_RECOVERY = 0x00189009;
        
    /** (0018,9010) VR=CS, VM=1 Flow Compensation  */
    public static final int FLOW_COMPENSATION = 0x00189010;
        
    /** (0018,9011) VR=CS, VM=1 Multiple Spin Echo  */
    public static final int MULTIPLE_SPIN_ECHO = 0x00189011;
        
    /** (0018,9012) VR=CS, VM=1 Multi-planar Excitation  */
    public static final int MULTI_PLANAR_EXCITATION = 0x00189012;
        
    /** (0018,9014) VR=CS, VM=1 Phase Contrast  */
    public static final int PHASE_CONTRAST = 0x00189014;
        
    /** (0018,9015) VR=CS, VM=1 Time of Flight Contrast  */
    public static final int TIME_OF_FLIGHT_CONTRAST = 0x00189015;
        
    /** (0018,9016) VR=CS, VM=1 Spoiling  */
    public static final int SPOILING = 0x00189016;
        
    /** (0018,9017) VR=CS, VM=1 Steady State Pulse Sequence  */
    public static final int STEADY_STATE_PULSE_SEQUENCE = 0x00189017;
        
    /** (0018,9018) VR=CS, VM=1 Echo Planar Pulse Sequence  */
    public static final int ECHO_PLANAR_PULSE_SEQUENCE = 0x00189018;
        
    /** (0018,9019) VR=FD, VM=1 Tag Angle First Axis  */
    public static final int TAG_ANGLE_FIRST_AXIS = 0x00189019;
        
    /** (0018,9020) VR=CS, VM=1 Magnetization Transfer  */
    public static final int MAGNETIZATION_TRANSFER = 0x00189020;
        
    /** (0018,9021) VR=CS, VM=1 T2 Preparation  */
    public static final int T2_PREPARATION = 0x00189021;
        
    /** (0018,9022) VR=CS, VM=1 Blood Signal Nulling  */
    public static final int BLOOD_SIGNAL_NULLING = 0x00189022;
        
    /** (0018,9024) VR=CS, VM=1 Saturation Recovery  */
    public static final int SATURATION_RECOVERY = 0x00189024;
        
    /** (0018,9025) VR=CS, VM=1 Spectrally Selected Suppression  */
    public static final int SPECTRALLY_SELECTED_SUPPRESSION = 0x00189025;
        
    /** (0018,9026) VR=CS, VM=1 Spectrally Selected Excitation  */
    public static final int SPECTRALLY_SELECTED_EXCITATION = 0x00189026;
        
    /** (0018,9027) VR=CS, VM=1 Spatial Pre-saturation  */
    public static final int SPATIAL_PRE_SATURATION = 0x00189027;
        
    /** (0018,9028) VR=CS, VM=1 Tagging  */
    public static final int TAGGING = 0x00189028;
        
    /** (0018,9029) VR=CS, VM=1 Oversampling Phase  */
    public static final int OVERSAMPLING_PHASE = 0x00189029;
        
    /** (0018,9030) VR=FD, VM=1 Tag Spacing First Dimension  */
    public static final int TAG_SPACING_FIRST_DIMENSION = 0x00189030;
        
    /** (0018,9032) VR=CS, VM=1 Geometry of k-Space Traversal  */
    public static final int GEOMETRY_OF_K_SPACE_TRAVERSAL = 0x00189032;
        
    /** (0018,9033) VR=CS, VM=1 Segmented k-Space Traversal  */
    public static final int SEGMENTED_K_SPACE_TRAVERSAL = 0x00189033;
        
    /** (0018,9034) VR=CS, VM=1 Rectilinear Phase Encode Reordering  */
    public static final int RECTILINEAR_PHASE_ENCODE_REORDERING = 0x00189034;
        
    /** (0018,9035) VR=FD, VM=1 Tag Thickness  */
    public static final int TAG_THICKNESS = 0x00189035;
        
    /** (0018,9036) VR=CS, VM=1 Partial Fourier Direction  */
    public static final int PARTIAL_FOURIER_DIRECTION = 0x00189036;
        
    /** (0018,9037) VR=CS, VM=1 Cardiac Synchronization Technique  */
    public static final int CARDIAC_SYNCHRONIZATION_TECHNIQUE = 0x00189037;
        
    /** (0018,9041) VR=LO, VM=1 Receive Coil Manufacturer Name  */
    public static final int RECEIVE_COIL_MANUFACTURER_NAME = 0x00189041;
        
    /** (0018,9042) VR=SQ, VM=1 MR Receive Coil Sequence  */
    public static final int MR_RECEIVE_COIL_SEQUENCE = 0x00189042;
        
    /** (0018,9043) VR=CS, VM=1 Receive Coil Type  */
    public static final int RECEIVE_COIL_TYPE = 0x00189043;
        
    /** (0018,9044) VR=CS, VM=1 Quadrature Receive Coil  */
    public static final int QUADRATURE_RECEIVE_COIL = 0x00189044;
        
    /** (0018,9045) VR=SQ, VM=1 Multi-Coil Definition Sequence  */
    public static final int MULTI_COIL_DEFINITION_SEQUENCE = 0x00189045;
        
    /** (0018,9046) VR=LO, VM=1 Multi-Coil Configuration  */
    public static final int MULTI_COIL_CONFIGURATION = 0x00189046;
        
    /** (0018,9047) VR=SH, VM=1 Multi-Coil Element Name  */
    public static final int MULTI_COIL_ELEMENT_NAME = 0x00189047;
        
    /** (0018,9048) VR=CS, VM=1 Multi-Coil Element Used  */
    public static final int MULTI_COIL_ELEMENT_USED = 0x00189048;
        
    /** (0018,9049) VR=SQ, VM=1 MR Transmit Coil Sequence  */
    public static final int MR_TRANSMIT_COIL_SEQUENCE = 0x00189049;
        
    /** (0018,9050) VR=LO, VM=1 Transmit Coil Manufacturer Name  */
    public static final int TRANSMIT_COIL_MANUFACTURER_NAME = 0x00189050;
        
    /** (0018,9051) VR=CS, VM=1 Transmit Coil Type  */
    public static final int TRANSMIT_COIL_TYPE = 0x00189051;
        
    /** (0018,9052) VR=FD, VM=1-2 Spectral Width  */
    public static final int SPECTRAL_WIDTH = 0x00189052;
        
    /** (0018,9053) VR=FD, VM=1-2 Chemical Shift Reference  */
    public static final int CHEMICAL_SHIFT_REFERENCE = 0x00189053;
        
    /** (0018,9054) VR=CS, VM=1 Volume Localization Technique  */
    public static final int VOLUME_LOCALIZATION_TECHNIQUE = 0x00189054;
        
    /** (0018,9058) VR=US, VM=1 MR Acquisition Frequency Encoding Steps  */
    public static final int MR_ACQUISITION_FREQUENCY_ENCODING_STEPS = 0x00189058;
        
    /** (0018,9059) VR=CS, VM=1 De-coupling  */
    public static final int DE_COUPLING = 0x00189059;
        
    /** (0018,9060) VR=CS, VM=1-2 De-coupled Nucleus  */
    public static final int DE_COUPLED_NUCLEUS = 0x00189060;
        
    /** (0018,9061) VR=FD, VM=1-2 De-coupling Frequency  */
    public static final int DE_COUPLING_FREQUENCY = 0x00189061;
        
    /** (0018,9062) VR=CS, VM=1 De-coupling Method  */
    public static final int DE_COUPLING_METHOD = 0x00189062;
        
    /** (0018,9063) VR=FD, VM=1-2 De-coupling Chemical Shift Reference  */
    public static final int DE_COUPLING_CHEMICAL_SHIFT_REFERENCE = 0x00189063;
        
    /** (0018,9064) VR=CS, VM=1 k-space Filtering  */
    public static final int K_SPACE_FILTERING = 0x00189064;
        
    /** (0018,9065) VR=CS, VM=1-2 Time Domain Filtering  */
    public static final int TIME_DOMAIN_FILTERING = 0x00189065;
        
    /** (0018,9066) VR=US, VM=1-2 Number of Zero fills  */
    public static final int NUMBER_OF_ZERO_FILLS = 0x00189066;
        
    /** (0018,9067) VR=CS, VM=1 Baseline Correction  */
    public static final int BASELINE_CORRECTION = 0x00189067;
        
    /** (0018,9069) VR=FD, VM=1 Parallel Reduction Factor In-plane  */
    public static final int PARALLEL_REDUCTION_FACTOR_IN_PLANE = 0x00189069;
        
    /** (0018,9070) VR=FD, VM=1 Cardiac R-R Interval Specified  */
    public static final int CARDIAC_R_R_INTERVAL_SPECIFIED = 0x00189070;
        
    /** (0018,9073) VR=FD, VM=1 Acquisition Duration  */
    public static final int ACQUISITION_DURATION = 0x00189073;
        
    /** (0018,9074) VR=DT, VM=1 Frame Acquisition Datetime  */
    public static final int FRAME_ACQUISITION_DATETIME = 0x00189074;
        
    /** (0018,9075) VR=CS, VM=1 Diffusion Directionality  */
    public static final int DIFFUSION_DIRECTIONALITY = 0x00189075;
        
    /** (0018,9076) VR=SQ, VM=1 Diffusion Gradient Direction Sequence  */
    public static final int DIFFUSION_GRADIENT_DIRECTION_SEQUENCE = 0x00189076;
        
    /** (0018,9077) VR=CS, VM=1 Parallel Acquisition  */
    public static final int PARALLEL_ACQUISITION = 0x00189077;
        
    /** (0018,9078) VR=CS, VM=1 Parallel Acquisition Technique  */
    public static final int PARALLEL_ACQUISITION_TECHNIQUE = 0x00189078;
        
    /** (0018,9079) VR=FD, VM=1-n Inversion Times  */
    public static final int INVERSION_TIMES = 0x00189079;
        
    /** (0018,9080) VR=ST, VM=1 Metabolite Map Description  */
    public static final int METABOLITE_MAP_DESCRIPTION = 0x00189080;
        
    /** (0018,9081) VR=CS, VM=1 Partial Fourier  */
    public static final int PARTIAL_FOURIER = 0x00189081;
        
    /** (0018,9082) VR=FD, VM=1 Effective Echo Time  */
    public static final int EFFECTIVE_ECHO_TIME = 0x00189082;
        
    /** (0018,9083) VR=SQ, VM=1 Metabolite Map Code Sequence  */
    public static final int METABOLITE_MAP_CODE_SEQUENCE = 0x00189083;
        
    /** (0018,9084) VR=SQ, VM=1 Chemical Shift Sequence  */
    public static final int CHEMICAL_SHIFT_SEQUENCE = 0x00189084;
        
    /** (0018,9085) VR=CS, VM=1 Cardiac Signal Source  */
    public static final int CARDIAC_SIGNAL_SOURCE = 0x00189085;
        
    /** (0018,9087) VR=FD, VM=1 Diffusion b-value  */
    public static final int DIFFUSION_B_VALUE = 0x00189087;
        
    /** (0018,9089) VR=FD, VM=3 Diffusion Gradient Orientation  */
    public static final int DIFFUSION_GRADIENT_ORIENTATION = 0x00189089;
        
    /** (0018,9090) VR=FD, VM=3 Velocity Encoding Direction  */
    public static final int VELOCITY_ENCODING_DIRECTION = 0x00189090;
        
    /** (0018,9091) VR=FD, VM=1 Velocity Encoding Minimum Value  */
    public static final int VELOCITY_ENCODING_MINIMUM_VALUE = 0x00189091;
        
    /** (0018,9093) VR=US, VM=1 Number of k-Space Trajectories  */
    public static final int NUMBER_OF_K_SPACE_TRAJECTORIES = 0x00189093;
        
    /** (0018,9094) VR=CS, VM=1 Coverage of k-Space  */
    public static final int COVERAGE_OF_K_SPACE = 0x00189094;
        
    /** (0018,9095) VR=UL, VM=1 Spectroscopy Acquisition Phase Rows  */
    public static final int SPECTROSCOPY_ACQUISITION_PHASE_ROWS = 0x00189095;
        
    /** (0018,9098) VR=FD, VM=1-2 Transmitter Frequency  */
    public static final int TRANSMITTER_FREQUENCY = 0x00189098;
        
    /** (0018,9100) VR=CS, VM=1-2 Resonant Nucleus  */
    public static final int RESONANT_NUCLEUS = 0x00189100;
        
    /** (0018,9101) VR=CS, VM=1 Frequency Correction  */
    public static final int FREQUENCY_CORRECTION = 0x00189101;
        
    /** (0018,9103) VR=SQ, VM=1 MR Spectroscopy FOV/Geometry Sequence  */
    public static final int MR_SPECTROSCOPY_FOV_GEOMETRY_SEQUENCE = 0x00189103;
        
    /** (0018,9104) VR=FD, VM=1 Slab Thickness  */
    public static final int SLAB_THICKNESS = 0x00189104;
        
    /** (0018,9105) VR=FD, VM=3 Slab Orientation  */
    public static final int SLAB_ORIENTATION = 0x00189105;
        
    /** (0018,9106) VR=FD, VM=3 Mid Slab Position  */
    public static final int MID_SLAB_POSITION = 0x00189106;
        
    /** (0018,9107) VR=SQ, VM=1 MR Spatial Saturation Sequence  */
    public static final int MR_SPATIAL_SATURATION_SEQUENCE = 0x00189107;
        
    /** (0018,9112) VR=SQ, VM=1 MR Timing and Related Parameters Sequence  */
    public static final int MR_TIMING_AND_RELATED_PARAMETERS_SEQUENCE = 0x00189112;
        
    /** (0018,9114) VR=SQ, VM=1 MR Echo Sequence  */
    public static final int MR_ECHO_SEQUENCE = 0x00189114;
        
    /** (0018,9115) VR=SQ, VM=1 MR Modifier Sequence  */
    public static final int MR_MODIFIER_SEQUENCE = 0x00189115;
        
    /** (0018,9117) VR=SQ, VM=1 MR Diffusion Sequence  */
    public static final int MR_DIFFUSION_SEQUENCE = 0x00189117;
        
    /** (0018,9118) VR=SQ, VM=1 Cardiac Trigger Sequence  */
    public static final int CARDIAC_TRIGGER_SEQUENCE = 0x00189118;
        
    /** (0018,9119) VR=SQ, VM=1 MR Averages Sequence  */
    public static final int MR_AVERAGES_SEQUENCE = 0x00189119;
        
    /** (0018,9125) VR=SQ, VM=1 MR FOV/Geometry Sequence  */
    public static final int MR_FOV_GEOMETRY_SEQUENCE = 0x00189125;
        
    /** (0018,9126) VR=SQ, VM=1 Volume Localization Sequence  */
    public static final int VOLUME_LOCALIZATION_SEQUENCE = 0x00189126;
        
    /** (0018,9127) VR=UL, VM=1 Spectroscopy Acquisition Data Columns  */
    public static final int SPECTROSCOPY_ACQUISITION_DATA_COLUMNS = 0x00189127;
        
    /** (0018,9147) VR=CS, VM=1 Diffusion Anisotropy Type  */
    public static final int DIFFUSION_ANISOTROPY_TYPE = 0x00189147;
        
    /** (0018,9151) VR=DT, VM=1 Frame Reference Datetime  */
    public static final int FRAME_REFERENCE_DATETIME = 0x00189151;
        
    /** (0018,9152) VR=SQ, VM=1 MR Metabolite Map Sequence  */
    public static final int MR_METABOLITE_MAP_SEQUENCE = 0x00189152;
        
    /** (0018,9155) VR=FD, VM=1 Parallel Reduction Factor out-of-plane  */
    public static final int PARALLEL_REDUCTION_FACTOR_OUT_OF_PLANE = 0x00189155;
        
    /** (0018,9159) VR=UL, VM=1 Spectroscopy Acquisition Out-of-plane Phase Steps  */
    public static final int SPECTROSCOPY_ACQUISITION_OUT_OF_PLANE_PHASE_STEPS = 0x00189159;
        
    /** (0018,9166) VR=CS, VM=1 Bulk Motion Status  */
    public static final int BULK_MOTION_STATUS = 0x00189166;
        
    /** (0018,9168) VR=FD, VM=1 Parallel Reduction Factor Second In-plane  */
    public static final int PARALLEL_REDUCTION_FACTOR_SECOND_IN_PLANE = 0x00189168;
        
    /** (0018,9169) VR=CS, VM=1 Cardiac Beat Rejection Technique  */
    public static final int CARDIAC_BEAT_REJECTION_TECHNIQUE = 0x00189169;
        
    /** (0018,9170) VR=CS, VM=1 Respiratory Motion Compensation Technique  */
    public static final int RESPIRATORY_MOTION_COMPENSATION_TECHNIQUE = 0x00189170;
        
    /** (0018,9171) VR=CS, VM=1 Respiratory Signal Source  */
    public static final int RESPIRATORY_SIGNAL_SOURCE = 0x00189171;
        
    /** (0018,9172) VR=CS, VM=1 Bulk Motion Compensation Technique  */
    public static final int BULK_MOTION_COMPENSATION_TECHNIQUE = 0x00189172;
        
    /** (0018,9173) VR=CS, VM=1 Bulk Motion Signal Source  */
    public static final int BULK_MOTION_SIGNAL_SOURCE = 0x00189173;
        
    /** (0018,9174) VR=CS, VM=1 Applicable Safety Standard Agency  */
    public static final int APPLICABLE_SAFETY_STANDARD_AGENCY = 0x00189174;
        
    /** (0018,9175) VR=LO, VM=1 Applicable Safety Standard Description  */
    public static final int APPLICABLE_SAFETY_STANDARD_DESCRIPTION = 0x00189175;
        
    /** (0018,9176) VR=SQ, VM=1 Operating Mode Sequence  */
    public static final int OPERATING_MODE_SEQUENCE = 0x00189176;
        
    /** (0018,9177) VR=CS, VM=1 Operating Mode Type  */
    public static final int OPERATING_MODE_TYPE = 0x00189177;
        
    /** (0018,9178) VR=CS, VM=1 Operating Mode  */
    public static final int OPERATING_MODE = 0x00189178;
        
    /** (0018,9179) VR=CS, VM=1 Specific Absorption Rate Definition  */
    public static final int SPECIFIC_ABSORPTION_RATE_DEFINITION = 0x00189179;
        
    /** (0018,9180) VR=CS, VM=1 Gradient Output Type  */
    public static final int GRADIENT_OUTPUT_TYPE = 0x00189180;
        
    /** (0018,9181) VR=FD, VM=1 Specific Absorption Rate Value  */
    public static final int SPECIFIC_ABSORPTION_RATE_VALUE = 0x00189181;
        
    /** (0018,9182) VR=FD, VM=1 Gradient Output  */
    public static final int GRADIENT_OUTPUT = 0x00189182;
        
    /** (0018,9183) VR=CS, VM=1 Flow Compensation Direction  */
    public static final int FLOW_COMPENSATION_DIRECTION = 0x00189183;
        
    /** (0018,9184) VR=FD, VM=1 Tagging Delay  */
    public static final int TAGGING_DELAY = 0x00189184;
        
    /** (0018,9185) VR=ST, VM=1 Respiratory Motion Compensation Technique Description  */
    public static final int RESPIRATORY_MOTION_COMPENSATION_TECHNIQUE_DESCRIPTION = 0x00189185;
        
    /** (0018,9186) VR=SH, VM=1 Respiratory Signal Source ID  */
    public static final int RESPIRATORY_SIGNAL_SOURCE_ID = 0x00189186;
        
    /** (0018,9195) VR=FD, VM=1 Chemical Shifts Minimum Integration Limit in Hz RET */
    public static final int CHEMICAL_SHIFTS_MINIMUM_INTEGRATION_LIMIT_IN_HZ_RET = 0x00189195;
        
    /** (0018,9196) VR=FD, VM=1 Chemical Shifts Maximum Integration Limit in Hz RET */
    public static final int CHEMICAL_SHIFTS_MAXIMUM_INTEGRATION_LIMIT_IN_HZ_RET = 0x00189196;
        
    /** (0018,9197) VR=SQ, VM=1 MR Velocity Encoding Sequence  */
    public static final int MR_VELOCITY_ENCODING_SEQUENCE = 0x00189197;
        
    /** (0018,9198) VR=CS, VM=1 First Order Phase Correction  */
    public static final int FIRST_ORDER_PHASE_CORRECTION = 0x00189198;
        
    /** (0018,9199) VR=CS, VM=1 Water Referenced Phase Correction  */
    public static final int WATER_REFERENCED_PHASE_CORRECTION = 0x00189199;
        
    /** (0018,9200) VR=CS, VM=1 MR Spectroscopy Acquisition Type  */
    public static final int MR_SPECTROSCOPY_ACQUISITION_TYPE = 0x00189200;
        
    /** (0018,9214) VR=CS, VM=1 Respiratory Cycle Position  */
    public static final int RESPIRATORY_CYCLE_POSITION = 0x00189214;
        
    /** (0018,9217) VR=FD, VM=1 Velocity Encoding Maximum Value  */
    public static final int VELOCITY_ENCODING_MAXIMUM_VALUE = 0x00189217;
        
    /** (0018,9218) VR=FD, VM=1 Tag Spacing Second Dimension  */
    public static final int TAG_SPACING_SECOND_DIMENSION = 0x00189218;
        
    /** (0018,9219) VR=SS, VM=1 Tag Angle Second Axis  */
    public static final int TAG_ANGLE_SECOND_AXIS = 0x00189219;
        
    /** (0018,9220) VR=FD, VM=1 Frame Acquisition Duration  */
    public static final int FRAME_ACQUISITION_DURATION = 0x00189220;
        
    /** (0018,9226) VR=SQ, VM=1 MR Image Frame Type Sequence  */
    public static final int MR_IMAGE_FRAME_TYPE_SEQUENCE = 0x00189226;
        
    /** (0018,9227) VR=SQ, VM=1 MR Spectroscopy Frame Type Sequence  */
    public static final int MR_SPECTROSCOPY_FRAME_TYPE_SEQUENCE = 0x00189227;
        
    /** (0018,9231) VR=US, VM=1 MR Acquisition Phase Encoding Steps in-plane  */
    public static final int MR_ACQUISITION_PHASE_ENCODING_STEPS_IN_PLANE = 0x00189231;
        
    /** (0018,9232) VR=US, VM=1 MR Acquisition Phase Encoding Steps out-of-plane  */
    public static final int MR_ACQUISITION_PHASE_ENCODING_STEPS_OUT_OF_PLANE = 0x00189232;
        
    /** (0018,9234) VR=UL, VM=1 Spectroscopy Acquisition Phase Columns  */
    public static final int SPECTROSCOPY_ACQUISITION_PHASE_COLUMNS = 0x00189234;
        
    /** (0018,9236) VR=CS, VM=1 Cardiac Cycle Position  */
    public static final int CARDIAC_CYCLE_POSITION = 0x00189236;
        
    /** (0018,9239) VR=SQ, VM=1 Specific Absorption Rate Sequence  */
    public static final int SPECIFIC_ABSORPTION_RATE_SEQUENCE = 0x00189239;
        
    /** (0018,9240) VR=US, VM=1 RF Echo Train Length  */
    public static final int RF_ECHO_TRAIN_LENGTH = 0x00189240;
        
    /** (0018,9241) VR=US, VM=1 Gradient Echo Train Length  */
    public static final int GRADIENT_ECHO_TRAIN_LENGTH = 0x00189241;
        
    /** (0018,9295) VR=FD, VM=1 Chemical Shifts Minimum Integration Limit in ppm  */
    public static final int CHEMICAL_SHIFTS_MINIMUM_INTEGRATION_LIMIT_IN_PPM = 0x00189295;
        
    /** (0018,9296) VR=FD, VM=1 Chemical Shifts Maximum Integration Limit in ppm  */
    public static final int CHEMICAL_SHIFTS_MAXIMUM_INTEGRATION_LIMIT_IN_PPM = 0x00189296;
        
    /** (0018,9301) VR=SQ, VM=1 CT Acquisition Type Sequence  */
    public static final int CT_ACQUISITION_TYPE_SEQUENCE = 0x00189301;
        
    /** (0018,9302) VR=CS, VM=1 Acquisition Type  */
    public static final int ACQUISITION_TYPE = 0x00189302;
        
    /** (0018,9303) VR=FD, VM=1 Tube Angle  */
    public static final int TUBE_ANGLE = 0x00189303;
        
    /** (0018,9304) VR=SQ, VM=1 CT Acquisition Details Sequence  */
    public static final int CT_ACQUISITION_DETAILS_SEQUENCE = 0x00189304;
        
    /** (0018,9305) VR=FD, VM=1 Revolution Time  */
    public static final int REVOLUTION_TIME = 0x00189305;
        
    /** (0018,9306) VR=FD, VM=1 Single Collimation Width  */
    public static final int SINGLE_COLLIMATION_WIDTH = 0x00189306;
        
    /** (0018,9307) VR=FD, VM=1 Total Collimation Width  */
    public static final int TOTAL_COLLIMATION_WIDTH = 0x00189307;
        
    /** (0018,9308) VR=SQ, VM=1 CT Table Dynamics Sequence  */
    public static final int CT_TABLE_DYNAMICS_SEQUENCE = 0x00189308;
        
    /** (0018,9309) VR=FD, VM=1 Table Speed  */
    public static final int TABLE_SPEED = 0x00189309;
        
    /** (0018,9310) VR=FD, VM=1 Table Feed per Rotation  */
    public static final int TABLE_FEED_PER_ROTATION = 0x00189310;
        
    /** (0018,9311) VR=FD, VM=1 Spiral Pitch Factor  */
    public static final int SPIRAL_PITCH_FACTOR = 0x00189311;
        
    /** (0018,9312) VR=SQ, VM=1 CT Geometry Sequence  */
    public static final int CT_GEOMETRY_SEQUENCE = 0x00189312;
        
    /** (0018,9313) VR=FD, VM=3 Data Collection Center (Patient)  */
    public static final int DATA_COLLECTION_CENTER_PATIENT = 0x00189313;
        
    /** (0018,9314) VR=SQ, VM=1 CT Reconstruction Sequence  */
    public static final int CT_RECONSTRUCTION_SEQUENCE = 0x00189314;
        
    /** (0018,9315) VR=CS, VM=1 Reconstruction Algorithm  */
    public static final int RECONSTRUCTION_ALGORITHM = 0x00189315;
        
    /** (0018,9316) VR=CS, VM=1 Convolution Kernel Group  */
    public static final int CONVOLUTION_KERNEL_GROUP = 0x00189316;
        
    /** (0018,9317) VR=FD, VM=2 Reconstruction Field of View  */
    public static final int RECONSTRUCTION_FIELD_OF_VIEW = 0x00189317;
        
    /** (0018,9318) VR=FD, VM=3 Reconstruction Target Center (Patient)  */
    public static final int RECONSTRUCTION_TARGET_CENTER_PATIENT = 0x00189318;
        
    /** (0018,9319) VR=FD, VM=1 Reconstruction Angle  */
    public static final int RECONSTRUCTION_ANGLE = 0x00189319;
        
    /** (0018,9320) VR=SH, VM=1 Image Filter  */
    public static final int IMAGE_FILTER = 0x00189320;
        
    /** (0018,9321) VR=SQ, VM=1 CT Exposure Sequence  */
    public static final int CT_EXPOSURE_SEQUENCE = 0x00189321;
        
    /** (0018,9322) VR=FD, VM=2 Reconstruction Pixel Spacing  */
    public static final int RECONSTRUCTION_PIXEL_SPACING = 0x00189322;
        
    /** (0018,9323) VR=CS, VM=1 Exposure Modulation Type  */
    public static final int EXPOSURE_MODULATION_TYPE = 0x00189323;
        
    /** (0018,9324) VR=FD, VM=1 Estimated Dose Saving  */
    public static final int ESTIMATED_DOSE_SAVING = 0x00189324;
        
    /** (0018,9325) VR=SQ, VM=1 CT X-ray Details Sequence  */
    public static final int CT_X_RAY_DETAILS_SEQUENCE = 0x00189325;
        
    /** (0018,9326) VR=SQ, VM=1 CT Position Sequence  */
    public static final int CT_POSITION_SEQUENCE = 0x00189326;
        
    /** (0018,9327) VR=FD, VM=1 Table Position  */
    public static final int TABLE_POSITION = 0x00189327;
        
    /** (0018,9328) VR=FD, VM=1 Exposure Time in ms  */
    public static final int EXPOSURE_TIME_IN_MS = 0x00189328;
        
    /** (0018,9329) VR=SQ, VM=1 CT Image Frame Type Sequence  */
    public static final int CT_IMAGE_FRAME_TYPE_SEQUENCE = 0x00189329;
        
    /** (0018,9330) VR=FD, VM=1 X-Ray Tube Current in mA  */
    public static final int X_RAY_TUBE_CURRENT_IN_MA = 0x00189330;
        
    /** (0018,9332) VR=FD, VM=1 Exposure in mAs  */
    public static final int EXPOSURE_IN_MAS = 0x00189332;
        
    /** (0018,9333) VR=CS, VM=1 Constant Volume Flag  */
    public static final int CONSTANT_VOLUME_FLAG = 0x00189333;
        
    /** (0018,9334) VR=CS, VM=1 Fluoroscopy Flag  */
    public static final int FLUOROSCOPY_FLAG = 0x00189334;
        
    /** (0018,9335) VR=FD, VM=1 Distance Source to Data Collection Center  */
    public static final int DISTANCE_SOURCE_TO_DATA_COLLECTION_CENTER = 0x00189335;
        
    /** (0018,9337) VR=US, VM=1 Contrast/Bolus Agent Number  */
    public static final int CONTRAST_BOLUS_AGENT_NUMBER = 0x00189337;
        
    /** (0018,9338) VR=SQ, VM=1 Contrast/Bolus Ingredient Code Sequence  */
    public static final int CONTRAST_BOLUS_INGREDIENT_CODE_SEQUENCE = 0x00189338;
        
    /** (0018,9340) VR=SQ, VM=1 Contrast Administration Profile Sequence  */
    public static final int CONTRAST_ADMINISTRATION_PROFILE_SEQUENCE = 0x00189340;
        
    /** (0018,9341) VR=SQ, VM=1 Contrast/Bolus Usage Sequence  */
    public static final int CONTRAST_BOLUS_USAGE_SEQUENCE = 0x00189341;
        
    /** (0018,9342) VR=CS, VM=1 Contrast/Bolus Agent Administered  */
    public static final int CONTRAST_BOLUS_AGENT_ADMINISTERED = 0x00189342;
        
    /** (0018,9343) VR=CS, VM=1 Contrast/Bolus Agent Detected  */
    public static final int CONTRAST_BOLUS_AGENT_DETECTED = 0x00189343;
        
    /** (0018,9344) VR=CS, VM=1 Contrast/Bolus Agent Phase  */
    public static final int CONTRAST_BOLUS_AGENT_PHASE = 0x00189344;
        
    /** (0018,9345) VR=FD, VM=1 CTDIvol  */
    public static final int CTDIVOL = 0x00189345;
        
    /** (0018,9401) VR=SQ, VM=1 Projection Pixel Calibration Sequence  */
    public static final int PROJECTION_PIXEL_CALIBRATION_SEQUENCE = 0x00189401;
        
    /** (0018,9402) VR=FL, VM=1 Distance Source to Isocenter  */
    public static final int DISTANCE_SOURCE_TO_ISOCENTER = 0x00189402;
        
    /** (0018,9403) VR=FL, VM=1 Distance Object to Table Top  */
    public static final int DISTANCE_OBJECT_TO_TABLE_TOP = 0x00189403;
        
    /** (0018,9404) VR=FL, VM=2 Object Pixel Spacing in Center of Beam  */
    public static final int OBJECT_PIXEL_SPACING_IN_CENTER_OF_BEAM = 0x00189404;
        
    /** (0018,9405) VR=SQ, VM=1 Positioner Position Sequence  */
    public static final int POSITIONER_POSITION_SEQUENCE = 0x00189405;
        
    /** (0018,9406) VR=SQ, VM=1 Table Position Sequence  */
    public static final int TABLE_POSITION_SEQUENCE = 0x00189406;
        
    /** (0018,9407) VR=SQ, VM=1 Collimator Shape Sequence  */
    public static final int COLLIMATOR_SHAPE_SEQUENCE = 0x00189407;
        
    /** (0018,9412) VR=SQ, VM=1 XA/XRF Frame Characteristics Sequence  */
    public static final int XA_XRF_FRAME_CHARACTERISTICS_SEQUENCE = 0x00189412;
        
    /** (0018,9417) VR=SQ, VM=1 Frame Acquisition Sequence  */
    public static final int FRAME_ACQUISITION_SEQUENCE = 0x00189417;
        
    /** (0018,9420) VR=CS, VM=1 X-Ray Receptor Type  */
    public static final int X_RAY_RECEPTOR_TYPE = 0x00189420;
        
    /** (0018,9423) VR=LO, VM=1 Acquisition Protocol Name  */
    public static final int ACQUISITION_PROTOCOL_NAME = 0x00189423;
        
    /** (0018,9424) VR=LT, VM=1 Acquisition Protocol Description  */
    public static final int ACQUISITION_PROTOCOL_DESCRIPTION = 0x00189424;
        
    /** (0018,9425) VR=CS, VM=1 Contrast/Bolus Ingredient Opaque  */
    public static final int CONTRAST_BOLUS_INGREDIENT_OPAQUE = 0x00189425;
        
    /** (0018,9426) VR=FL, VM=1 Distance Receptor Plane to Detector Housing  */
    public static final int DISTANCE_RECEPTOR_PLANE_TO_DETECTOR_HOUSING = 0x00189426;
        
    /** (0018,9427) VR=CS, VM=1 Intensifier Active Shape  */
    public static final int INTENSIFIER_ACTIVE_SHAPE = 0x00189427;
        
    /** (0018,9428) VR=FL, VM=1-2 Intensifier Active Dimension(s)  */
    public static final int INTENSIFIER_ACTIVE_DIMENSIONS = 0x00189428;
        
    /** (0018,9429) VR=FL, VM=2 Physical Detector Size  */
    public static final int PHYSICAL_DETECTOR_SIZE = 0x00189429;
        
    /** (0018,9430) VR=US, VM=2 Position of Isocenter Projection  */
    public static final int POSITION_OF_ISOCENTER_PROJECTION = 0x00189430;
        
    /** (0018,9432) VR=SQ, VM=1 Field of View Sequence  */
    public static final int FIELD_OF_VIEW_SEQUENCE = 0x00189432;
        
    /** (0018,9433) VR=LO, VM=1 Field of View Description  */
    public static final int FIELD_OF_VIEW_DESCRIPTION = 0x00189433;
        
    /** (0018,9434) VR=SQ, VM=1 Exposure Control Sensing Regions Sequence  */
    public static final int EXPOSURE_CONTROL_SENSING_REGIONS_SEQUENCE = 0x00189434;
        
    /** (0018,9435) VR=CS, VM=1 Exposure Control Sensing Region Shape  */
    public static final int EXPOSURE_CONTROL_SENSING_REGION_SHAPE = 0x00189435;
        
    /** (0018,9436) VR=SS, VM=1 Exposure Control Sensing Region Left Vertical Edge  */
    public static final int EXPOSURE_CONTROL_SENSING_REGION_LEFT_VERTICAL_EDGE = 0x00189436;
        
    /** (0018,9437) VR=SS, VM=1 Exposure Control Sensing Region Right Vertical Edge  */
    public static final int EXPOSURE_CONTROL_SENSING_REGION_RIGHT_VERTICAL_EDGE = 0x00189437;
        
    /** (0018,9438) VR=SS, VM=1 Exposure Control Sensing Region Upper Horizontal Edge  */
    public static final int EXPOSURE_CONTROL_SENSING_REGION_UPPER_HORIZONTAL_EDGE = 0x00189438;
        
    /** (0018,9439) VR=SS, VM=1 Exposure Control Sensing Region Lower Horizontal Edge  */
    public static final int EXPOSURE_CONTROL_SENSING_REGION_LOWER_HORIZONTAL_EDGE = 0x00189439;
        
    /** (0018,9440) VR=SS, VM=2 Center of Circular Exposure Control Sensing Region  */
    public static final int CENTER_OF_CIRCULAR_EXPOSURE_CONTROL_SENSING_REGION = 0x00189440;
        
    /** (0018,9441) VR=US, VM=1 Radius of Circular Exposure Control Sensing Region  */
    public static final int RADIUS_OF_CIRCULAR_EXPOSURE_CONTROL_SENSING_REGION = 0x00189441;
        
    /** (0018,9442) VR=SS, VM=2-n Vertices of the Polygonal Exposure Control Sensing Region  */
    public static final int VERTICES_OF_THE_POLYGONAL_EXPOSURE_CONTROL_SENSING_REGION = 0x00189442;
        
    /** (0018,9447) VR=FL, VM=1 Column Angulation (Patient)  */
    public static final int COLUMN_ANGULATION_PATIENT = 0x00189447;
        
    /** (0018,9449) VR=FL, VM=1 Beam Angle  */
    public static final int BEAM_ANGLE = 0x00189449;
        
    /** (0018,9451) VR=SQ, VM=1 Frame Detector Parameters Sequence  */
    public static final int FRAME_DETECTOR_PARAMETERS_SEQUENCE = 0x00189451;
        
    /** (0018,9452) VR=FL, VM=1 Calculated Anatomy Thickness  */
    public static final int CALCULATED_ANATOMY_THICKNESS = 0x00189452;
        
    /** (0018,9455) VR=SQ, VM=1 Calibration Sequence  */
    public static final int CALIBRATION_SEQUENCE = 0x00189455;
        
    /** (0018,9456) VR=SQ, VM=1 Object Thickness Sequence  */
    public static final int OBJECT_THICKNESS_SEQUENCE = 0x00189456;
        
    /** (0018,9457) VR=CS, VM=1 Plane Identification  */
    public static final int PLANE_IDENTIFICATION = 0x00189457;
        
    /** (0018,9461) VR=FL, VM=1-2 Field of View Dimension(s) in Float  */
    public static final int FIELD_OF_VIEW_DIMENSIONS_IN_FLOAT = 0x00189461;
        
    /** (0018,9462) VR=SQ, VM=1 Isocenter Reference System Sequence  */
    public static final int ISOCENTER_REFERENCE_SYSTEM_SEQUENCE = 0x00189462;
        
    /** (0018,9463) VR=FL, VM=1 Positioner Isocenter Primary Angle  */
    public static final int POSITIONER_ISOCENTER_PRIMARY_ANGLE = 0x00189463;
        
    /** (0018,9464) VR=FL, VM=1 Positioner Isocenter Secondary Angle  */
    public static final int POSITIONER_ISOCENTER_SECONDARY_ANGLE = 0x00189464;
        
    /** (0018,9465) VR=FL, VM=1 Positioner Isocenter Detector Rotation Angle  */
    public static final int POSITIONER_ISOCENTER_DETECTOR_ROTATION_ANGLE = 0x00189465;
        
    /** (0018,9466) VR=FL, VM=1 Table X Position to Isocenter  */
    public static final int TABLE_X_POSITION_TO_ISOCENTER = 0x00189466;
        
    /** (0018,9467) VR=FL, VM=1 Table Y Position to Isocenter  */
    public static final int TABLE_Y_POSITION_TO_ISOCENTER = 0x00189467;
        
    /** (0018,9468) VR=FL, VM=1 Table Z Position to Isocenter  */
    public static final int TABLE_Z_POSITION_TO_ISOCENTER = 0x00189468;
        
    /** (0018,9469) VR=FL, VM=1 Table Horizontal Rotation Angle  */
    public static final int TABLE_HORIZONTAL_ROTATION_ANGLE = 0x00189469;
        
    /** (0018,9470) VR=FL, VM=1 Table Head Tilt Angle  */
    public static final int TABLE_HEAD_TILT_ANGLE = 0x00189470;
        
    /** (0018,9471) VR=FL, VM=1 Table Cradle Tilt Angle  */
    public static final int TABLE_CRADLE_TILT_ANGLE = 0x00189471;
        
    /** (0018,9472) VR=SQ, VM=1 Frame Display Shutter Sequence  */
    public static final int FRAME_DISPLAY_SHUTTER_SEQUENCE = 0x00189472;
        
    /** (0018,9473) VR=FL, VM=1 Acquired Image Area Dose Product  */
    public static final int ACQUIRED_IMAGE_AREA_DOSE_PRODUCT = 0x00189473;
        
    /** (0018,9474) VR=CS, VM=1 C-arm Positioner Tabletop Relationship  */
    public static final int C_ARM_POSITIONER_TABLETOP_RELATIONSHIP = 0x00189474;
        
    /** (0018,9476) VR=SQ, VM=1 X-Ray Geometry Sequence  */
    public static final int X_RAY_GEOMETRY_SEQUENCE = 0x00189476;
        
    /** (0018,9477) VR=SQ, VM=1 Irradiation Event Identification Sequence  */
    public static final int IRRADIATION_EVENT_IDENTIFICATION_SEQUENCE = 0x00189477;
        
    /** (0018,A001) VR=SQ, VM=1 Contributing Equipment Sequence  */
    public static final int CONTRIBUTING_EQUIPMENT_SEQUENCE = 0x0018A001;
        
    /** (0018,A002) VR=DT, VM=1 Contribution Date Time  */
    public static final int CONTRIBUTION_DATE_TIME = 0x0018A002;
        
    /** (0018,A003) VR=ST, VM=1 Contribution Description  */
    public static final int CONTRIBUTION_DESCRIPTION = 0x0018A003;
        
    /** (0020,000D) VR=UI, VM=1 Study Instance UID  */
    public static final int STUDY_INSTANCE_UID = 0x0020000D;
        
    /** (0020,000E) VR=UI, VM=1 Series Instance UID  */
    public static final int SERIES_INSTANCE_UID = 0x0020000E;
        
    /** (0020,0010) VR=SH, VM=1 Study ID  */
    public static final int STUDY_ID = 0x00200010;
        
    /** (0020,0011) VR=IS, VM=1 Series Number  */
    public static final int SERIES_NUMBER = 0x00200011;
        
    /** (0020,0012) VR=IS, VM=1 Acquisition Number  */
    public static final int ACQUISITION_NUMBER = 0x00200012;
        
    /** (0020,0013) VR=IS, VM=1 Instance Number  */
    public static final int INSTANCE_NUMBER = 0x00200013;
        
    /** (0020,0014) VR=IS, VM=1 Isotope Number RET */
    public static final int ISOTOPE_NUMBER_RET = 0x00200014;
        
    /** (0020,0015) VR=IS, VM=1 Phase Number RET */
    public static final int PHASE_NUMBER_RET = 0x00200015;
        
    /** (0020,0016) VR=IS, VM=1 Interval Number RET */
    public static final int INTERVAL_NUMBER_RET = 0x00200016;
        
    /** (0020,0017) VR=IS, VM=1 Time Slot Number RET */
    public static final int TIME_SLOT_NUMBER_RET = 0x00200017;
        
    /** (0020,0018) VR=IS, VM=1 Angle Number RET */
    public static final int ANGLE_NUMBER_RET = 0x00200018;
        
    /** (0020,0019) VR=IS, VM=1 Item Number  */
    public static final int ITEM_NUMBER = 0x00200019;
        
    /** (0020,0020) VR=CS, VM=2 Patient Orientation  */
    public static final int PATIENT_ORIENTATION = 0x00200020;
        
    /** (0020,0022) VR=IS, VM=1 Overlay Number RET */
    public static final int OVERLAY_NUMBER_RET = 0x00200022;
        
    /** (0020,0024) VR=IS, VM=1 Curve Number RET */
    public static final int CURVE_NUMBER_RET = 0x00200024;
        
    /** (0020,0026) VR=IS, VM=1 Lookup Table Number RET */
    public static final int LOOKUP_TABLE_NUMBER_RET = 0x00200026;
        
    /** (0020,0030) VR=DS, VM=3 Image Position RET */
    public static final int IMAGE_POSITION_RET = 0x00200030;
        
    /** (0020,0032) VR=DS, VM=3 Image Position (Patient)  */
    public static final int IMAGE_POSITION_PATIENT = 0x00200032;
        
    /** (0020,0035) VR=DS, VM=6 Image Orientation RET */
    public static final int IMAGE_ORIENTATION_RET = 0x00200035;
        
    /** (0020,0037) VR=DS, VM=6 Image Orientation (Patient)  */
    public static final int IMAGE_ORIENTATION_PATIENT = 0x00200037;
        
    /** (0020,0050) VR=DS, VM=1 Location RET */
    public static final int LOCATION_RET = 0x00200050;
        
    /** (0020,0052) VR=UI, VM=1 Frame of Reference UID  */
    public static final int FRAME_OF_REFERENCE_UID = 0x00200052;
        
    /** (0020,0060) VR=CS, VM=1 Laterality  */
    public static final int LATERALITY = 0x00200060;
        
    /** (0020,0062) VR=CS, VM=1 Image Laterality  */
    public static final int IMAGE_LATERALITY = 0x00200062;
        
    /** (0020,0070) VR=LO, VM=1 Image Geometry Type RET */
    public static final int IMAGE_GEOMETRY_TYPE_RET = 0x00200070;
        
    /** (0020,0080) VR=CS, VM=1-n Masking Image RET */
    public static final int MASKING_IMAGE_RET = 0x00200080;
        
    /** (0020,0100) VR=IS, VM=1 Temporal Position Identifier  */
    public static final int TEMPORAL_POSITION_IDENTIFIER = 0x00200100;
        
    /** (0020,0105) VR=IS, VM=1 Number of Temporal Positions  */
    public static final int NUMBER_OF_TEMPORAL_POSITIONS = 0x00200105;
        
    /** (0020,0110) VR=DS, VM=1 Temporal Resolution  */
    public static final int TEMPORAL_RESOLUTION = 0x00200110;
        
    /** (0020,0200) VR=UI, VM=1 Synchronization Frame of Reference UID  */
    public static final int SYNCHRONIZATION_FRAME_OF_REFERENCE_UID = 0x00200200;
        
    /** (0020,1000) VR=IS, VM=1 Series in Study RET */
    public static final int SERIES_IN_STUDY_RET = 0x00201000;
        
    /** (0020,1001) VR=IS, VM=1 Acquisitions in Series RET */
    public static final int ACQUISITIONS_IN_SERIES_RET = 0x00201001;
        
    /** (0020,1002) VR=IS, VM=1 Images in Acquisition  */
    public static final int IMAGES_IN_ACQUISITION = 0x00201002;
        
    /** (0020,1004) VR=IS, VM=1 Acquisitions in Study RET */
    public static final int ACQUISITIONS_IN_STUDY_RET = 0x00201004;
        
    /** (0020,1020) VR=CS, VM=1-n Reference RET */
    public static final int REFERENCE_RET = 0x00201020;
        
    /** (0020,1040) VR=LO, VM=1 Position Reference Indicator  */
    public static final int POSITION_REFERENCE_INDICATOR = 0x00201040;
        
    /** (0020,1041) VR=DS, VM=1 Slice Location  */
    public static final int SLICE_LOCATION = 0x00201041;
        
    /** (0020,1070) VR=IS, VM=1-n Other Study Numbers RET */
    public static final int OTHER_STUDY_NUMBERS_RET = 0x00201070;
        
    /** (0020,1200) VR=IS, VM=1 Number of Patient Related Studies  */
    public static final int NUMBER_OF_PATIENT_RELATED_STUDIES = 0x00201200;
        
    /** (0020,1202) VR=IS, VM=1 Number of Patient Related Series  */
    public static final int NUMBER_OF_PATIENT_RELATED_SERIES = 0x00201202;
        
    /** (0020,1204) VR=IS, VM=1 Number of Patient Related Instances  */
    public static final int NUMBER_OF_PATIENT_RELATED_INSTANCES = 0x00201204;
        
    /** (0020,1206) VR=IS, VM=1 Number of Study Related Series  */
    public static final int NUMBER_OF_STUDY_RELATED_SERIES = 0x00201206;
        
    /** (0020,1208) VR=IS, VM=1 Number of Study Related Instances  */
    public static final int NUMBER_OF_STUDY_RELATED_INSTANCES = 0x00201208;
        
    /** (0020,1209) VR=IS, VM=1 Number of Series Related Instances  */
    public static final int NUMBER_OF_SERIES_RELATED_INSTANCES = 0x00201209;
        
    /** (0020,31xx) VR=CS, VM=1-n Source Image IDs RET */
    public static final int SOURCE_IMAGE_IDS_RET = 0x00203100;
        
    /** (0020,3401) VR=CS, VM=1 Modifying Device ID RET */
    public static final int MODIFYING_DEVICE_ID_RET = 0x00203401;
        
    /** (0020,3402) VR=CS, VM=1 Modified Image ID RET */
    public static final int MODIFIED_IMAGE_ID_RET = 0x00203402;
        
    /** (0020,3403) VR=DA, VM=1 Modified Image Date RET */
    public static final int MODIFIED_IMAGE_DATE_RET = 0x00203403;
        
    /** (0020,3404) VR=LO, VM=1 Modifying Device Manufacturer RET */
    public static final int MODIFYING_DEVICE_MANUFACTURER_RET = 0x00203404;
        
    /** (0020,3405) VR=TM, VM=1 Modified Image Time RET */
    public static final int MODIFIED_IMAGE_TIME_RET = 0x00203405;
        
    /** (0020,3406) VR=LO, VM=1 Modified Image Description RET */
    public static final int MODIFIED_IMAGE_DESCRIPTION_RET = 0x00203406;
        
    /** (0020,4000) VR=LT, VM=1 Image Comments  */
    public static final int IMAGE_COMMENTS = 0x00204000;
        
    /** (0020,5000) VR=AT, VM=1-n Original Image Identification RET */
    public static final int ORIGINAL_IMAGE_IDENTIFICATION_RET = 0x00205000;
        
    /** (0020,5002) VR=CS, VM=1-n Original Image Identification Nomenclature RET */
    public static final int ORIGINAL_IMAGE_IDENTIFICATION_NOMENCLATURE_RET = 0x00205002;
        
    /** (0020,9056) VR=SH, VM=1 Stack ID  */
    public static final int STACK_ID = 0x00209056;
        
    /** (0020,9057) VR=UL, VM=1 In-Stack Position Number  */
    public static final int IN_STACK_POSITION_NUMBER = 0x00209057;
        
    /** (0020,9071) VR=SQ, VM=1 Frame Anatomy Sequence  */
    public static final int FRAME_ANATOMY_SEQUENCE = 0x00209071;
        
    /** (0020,9072) VR=CS, VM=1 Frame Laterality  */
    public static final int FRAME_LATERALITY = 0x00209072;
        
    /** (0020,9111) VR=SQ, VM=1 Frame Content Sequence  */
    public static final int FRAME_CONTENT_SEQUENCE = 0x00209111;
        
    /** (0020,9113) VR=SQ, VM=1 Plane Position Sequence  */
    public static final int PLANE_POSITION_SEQUENCE = 0x00209113;
        
    /** (0020,9116) VR=SQ, VM=1 Plane Orientation Sequence  */
    public static final int PLANE_ORIENTATION_SEQUENCE = 0x00209116;
        
    /** (0020,9128) VR=UL, VM=1 Temporal Position Index  */
    public static final int TEMPORAL_POSITION_INDEX = 0x00209128;
        
    /** (0020,9153) VR=FD, VM=1 Cardiac Trigger Delay Time  */
    public static final int CARDIAC_TRIGGER_DELAY_TIME = 0x00209153;
        
    /** (0020,9156) VR=US, VM=1 Frame Acquisition Number  */
    public static final int FRAME_ACQUISITION_NUMBER = 0x00209156;
        
    /** (0020,9157) VR=UL, VM=1-n Dimension Index Values  */
    public static final int DIMENSION_INDEX_VALUES = 0x00209157;
        
    /** (0020,9158) VR=LT, VM=1 Frame Comments  */
    public static final int FRAME_COMMENTS = 0x00209158;
        
    /** (0020,9161) VR=UI, VM=1 Concatenation UID  */
    public static final int CONCATENATION_UID = 0x00209161;
        
    /** (0020,9162) VR=US, VM=1 In-concatenation Number  */
    public static final int IN_CONCATENATION_NUMBER = 0x00209162;
        
    /** (0020,9163) VR=US, VM=1 In-concatenation Total Number  */
    public static final int IN_CONCATENATION_TOTAL_NUMBER = 0x00209163;
        
    /** (0020,9164) VR=UI, VM=1 Dimension Organization UID  */
    public static final int DIMENSION_ORGANIZATION_UID = 0x00209164;
        
    /** (0020,9165) VR=AT, VM=1 Dimension Index Pointer  */
    public static final int DIMENSION_INDEX_POINTER = 0x00209165;
        
    /** (0020,9167) VR=AT, VM=1 Functional Group Pointer  */
    public static final int FUNCTIONAL_GROUP_POINTER = 0x00209167;
        
    /** (0020,9213) VR=LO, VM=1 Dimension Index Private Creator  */
    public static final int DIMENSION_INDEX_PRIVATE_CREATOR = 0x00209213;
        
    /** (0020,9221) VR=SQ, VM=1 Dimension Organization Sequence  */
    public static final int DIMENSION_ORGANIZATION_SEQUENCE = 0x00209221;
        
    /** (0020,9222) VR=SQ, VM=1 Dimension Index Sequence  */
    public static final int DIMENSION_INDEX_SEQUENCE = 0x00209222;
        
    /** (0020,9228) VR=UL, VM=1 Concatenation Frame Offset Number  */
    public static final int CONCATENATION_FRAME_OFFSET_NUMBER = 0x00209228;
        
    /** (0020,9238) VR=LO, VM=1 Functional Group Private Creator  */
    public static final int FUNCTIONAL_GROUP_PRIVATE_CREATOR = 0x00209238;
        
    /** (0020,9251) VR=FD, VM=1 R - R Interval Time Measured  */
    public static final int R_R_INTERVAL_TIME_MEASURED = 0x00209251;
        
    /** (0020,9253) VR=SQ, VM=1 Respiratory Trigger Sequence  */
    public static final int RESPIRATORY_TRIGGER_SEQUENCE = 0x00209253;
        
    /** (0020,9254) VR=FD, VM=1 Respiratory Interval Time  */
    public static final int RESPIRATORY_INTERVAL_TIME = 0x00209254;
        
    /** (0020,9255) VR=FD, VM=1 Respiratory Trigger Delay Time  */
    public static final int RESPIRATORY_TRIGGER_DELAY_TIME = 0x00209255;
        
    /** (0020,9256) VR=FD, VM=1 Respiratory Trigger Delay Threshold  */
    public static final int RESPIRATORY_TRIGGER_DELAY_THRESHOLD = 0x00209256;
        
    /** (0020,9421) VR=LO, VM=1 Dimension Description Label  */
    public static final int DIMENSION_DESCRIPTION_LABEL = 0x00209421;
        
    /** (0020,9450) VR=SQ, VM=1 Patient Orientation in Frame Sequence  */
    public static final int PATIENT_ORIENTATION_IN_FRAME_SEQUENCE = 0x00209450;
        
    /** (0020,9453) VR=LO, VM=1 Frame Label  */
    public static final int FRAME_LABEL = 0x00209453;
        
    /** (0022,0001) VR=US, VM=1 Light Path Filter Pass-Through Wavelength  */
    public static final int LIGHT_PATH_FILTER_PASS_THROUGH_WAVELENGTH = 0x00220001;
        
    /** (0022,0002) VR=US, VM=2 Light Path Filter Pass Band  */
    public static final int LIGHT_PATH_FILTER_PASS_BAND = 0x00220002;
        
    /** (0022,0003) VR=US, VM=1 Image Path Filter Pass-Through Wavelength  */
    public static final int IMAGE_PATH_FILTER_PASS_THROUGH_WAVELENGTH = 0x00220003;
        
    /** (0022,0004) VR=US, VM=2 Image Path Filter Pass Band  */
    public static final int IMAGE_PATH_FILTER_PASS_BAND = 0x00220004;
        
    /** (0022,0005) VR=CS, VM=1 Patient Eye Movement Commanded  */
    public static final int PATIENT_EYE_MOVEMENT_COMMANDED = 0x00220005;
        
    /** (0022,0006) VR=SQ, VM=1 Patient Eye Movement Command Code Sequence  */
    public static final int PATIENT_EYE_MOVEMENT_COMMAND_CODE_SEQUENCE = 0x00220006;
        
    /** (0022,0007) VR=FL, VM=1 Spherical Lens Power  */
    public static final int SPHERICAL_LENS_POWER = 0x00220007;
        
    /** (0022,0008) VR=FL, VM=1 Cylinder Lens Power  */
    public static final int CYLINDER_LENS_POWER = 0x00220008;
        
    /** (0022,0009) VR=FL, VM=1 Cylinder Axis  */
    public static final int CYLINDER_AXIS = 0x00220009;
        
    /** (0022,000A) VR=FL, VM=1 Emmetropic Magnification  */
    public static final int EMMETROPIC_MAGNIFICATION = 0x0022000A;
        
    /** (0022,000B) VR=FL, VM=1 Intra Ocular Pressure  */
    public static final int INTRA_OCULAR_PRESSURE = 0x0022000B;
        
    /** (0022,000C) VR=FL, VM=1 Horizontal Field of View  */
    public static final int HORIZONTAL_FIELD_OF_VIEW = 0x0022000C;
        
    /** (0022,000D) VR=CS, VM=1 Pupil Dilated  */
    public static final int PUPIL_DILATED = 0x0022000D;
        
    /** (0022,000E) VR=FL, VM=1 Degree of Dilation  */
    public static final int DEGREE_OF_DILATION = 0x0022000E;
        
    /** (0022,0010) VR=FL, VM=1 Stereo Baseline Angle  */
    public static final int STEREO_BASELINE_ANGLE = 0x00220010;
        
    /** (0022,0011) VR=FL, VM=1 Stereo Baseline Displacement  */
    public static final int STEREO_BASELINE_DISPLACEMENT = 0x00220011;
        
    /** (0022,0012) VR=FL, VM=1 Stereo Horizontal Pixel Offset  */
    public static final int STEREO_HORIZONTAL_PIXEL_OFFSET = 0x00220012;
        
    /** (0022,0013) VR=FL, VM=1 Stereo Vertical Pixel Offset  */
    public static final int STEREO_VERTICAL_PIXEL_OFFSET = 0x00220013;
        
    /** (0022,0014) VR=FL, VM=1 Stereo Rotation  */
    public static final int STEREO_ROTATION = 0x00220014;
        
    /** (0022,0015) VR=SQ, VM=1 Acquisition Device Type Code Sequence  */
    public static final int ACQUISITION_DEVICE_TYPE_CODE_SEQUENCE = 0x00220015;
        
    /** (0022,0016) VR=SQ, VM=1 Illumination Type Code Sequence  */
    public static final int ILLUMINATION_TYPE_CODE_SEQUENCE = 0x00220016;
        
    /** (0022,0017) VR=SQ, VM=1 Light Path Filter Type Stack Code Sequence  */
    public static final int LIGHT_PATH_FILTER_TYPE_STACK_CODE_SEQUENCE = 0x00220017;
        
    /** (0022,0018) VR=SQ, VM=1 Image Path Filter Type Stack Code Sequence  */
    public static final int IMAGE_PATH_FILTER_TYPE_STACK_CODE_SEQUENCE = 0x00220018;
        
    /** (0022,0019) VR=SQ, VM=1 Lenses Code Sequence  */
    public static final int LENSES_CODE_SEQUENCE = 0x00220019;
        
    /** (0022,001A) VR=SQ, VM=1 Channel Description Code Sequence  */
    public static final int CHANNEL_DESCRIPTION_CODE_SEQUENCE = 0x0022001A;
        
    /** (0022,001B) VR=SQ, VM=1 Refractive State Sequence  */
    public static final int REFRACTIVE_STATE_SEQUENCE = 0x0022001B;
        
    /** (0022,001C) VR=SQ, VM=1 Mydriatic Agent Code Sequence  */
    public static final int MYDRIATIC_AGENT_CODE_SEQUENCE = 0x0022001C;
        
    /** (0022,001D) VR=SQ, VM=1 Relative Image Position Code Sequence  */
    public static final int RELATIVE_IMAGE_POSITION_CODE_SEQUENCE = 0x0022001D;
        
    /** (0022,0020) VR=SQ, VM=1 Stereo Pairs Sequence  */
    public static final int STEREO_PAIRS_SEQUENCE = 0x00220020;
        
    /** (0022,0021) VR=SQ, VM=1 Left Image Sequence  */
    public static final int LEFT_IMAGE_SEQUENCE = 0x00220021;
        
    /** (0022,0022) VR=SQ, VM=1 Right Image Sequence  */
    public static final int RIGHT_IMAGE_SEQUENCE = 0x00220022;
        
    /** (0028,0002) VR=US, VM=1 Samples per Pixel  */
    public static final int SAMPLES_PER_PIXEL = 0x00280002;
        
    /** (0028,0003) VR=US, VM=1 Samples per Pixel Used  */
    public static final int SAMPLES_PER_PIXEL_USED = 0x00280003;
        
    /** (0028,0004) VR=CS, VM=1 Photometric Interpretation  */
    public static final int PHOTOMETRIC_INTERPRETATION = 0x00280004;
        
    /** (0028,0005) VR=US, VM=1 Image Dimensions RET */
    public static final int IMAGE_DIMENSIONS_RET = 0x00280005;
        
    /** (0028,0006) VR=US, VM=1 Planar Configuration  */
    public static final int PLANAR_CONFIGURATION = 0x00280006;
        
    /** (0028,0008) VR=IS, VM=1 Number of Frames  */
    public static final int NUMBER_OF_FRAMES = 0x00280008;
        
    /** (0028,0009) VR=AT, VM=1-n Frame Increment Pointer  */
    public static final int FRAME_INCREMENT_POINTER = 0x00280009;
        
    /** (0028,000A) VR=AT, VM=1-n Frame Dimension Pointer  */
    public static final int FRAME_DIMENSION_POINTER = 0x0028000A;
        
    /** (0028,0010) VR=US, VM=1 Rows  */
    public static final int ROWS = 0x00280010;
        
    /** (0028,0011) VR=US, VM=1 Columns  */
    public static final int COLUMNS = 0x00280011;
        
    /** (0028,0012) VR=US, VM=1 Planes  */
    public static final int PLANES = 0x00280012;
        
    /** (0028,0014) VR=US, VM=1 Ultrasound Color Data Present  */
    public static final int ULTRASOUND_COLOR_DATA_PRESENT = 0x00280014;
        
    /** (0028,0030) VR=DS, VM=2 Pixel Spacing  */
    public static final int PIXEL_SPACING = 0x00280030;
        
    /** (0028,0031) VR=DS, VM=2 Zoom Factor  */
    public static final int ZOOM_FACTOR = 0x00280031;
        
    /** (0028,0032) VR=DS, VM=2 Zoom Center  */
    public static final int ZOOM_CENTER = 0x00280032;
        
    /** (0028,0034) VR=IS, VM=2 Pixel Aspect Ratio  */
    public static final int PIXEL_ASPECT_RATIO = 0x00280034;
        
    /** (0028,0040) VR=CS, VM=1 Image Format RET */
    public static final int IMAGE_FORMAT_RET = 0x00280040;
        
    /** (0028,0050) VR=LO, VM=1-n Manipulated Image RET */
    public static final int MANIPULATED_IMAGE_RET = 0x00280050;
        
    /** (0028,0051) VR=CS, VM=1-n Corrected Image  */
    public static final int CORRECTED_IMAGE = 0x00280051;
        
    /** (0028,0060) VR=CS, VM=1 Compression Code RET */
    public static final int COMPRESSION_CODE_RET = 0x00280060;
        
    /** (0028,0100) VR=US, VM=1 Bits Allocated  */
    public static final int BITS_ALLOCATED = 0x00280100;
        
    /** (0028,0101) VR=US, VM=1 Bits Stored  */
    public static final int BITS_STORED = 0x00280101;
        
    /** (0028,0102) VR=US, VM=1 High Bit  */
    public static final int HIGH_BIT = 0x00280102;
        
    /** (0028,0103) VR=US, VM=1 Pixel Representation  */
    public static final int PIXEL_REPRESENTATION = 0x00280103;
        
    /** (0028,0104) VR=US|SS, VM=1 Smallest Valid Pixel Value RET */
    public static final int SMALLEST_VALID_PIXEL_VALUE_RET = 0x00280104;
        
    /** (0028,0105) VR=US|SS, VM=1 Largest Valid Pixel Value RET */
    public static final int LARGEST_VALID_PIXEL_VALUE_RET = 0x00280105;
        
    /** (0028,0106) VR=US|SS, VM=1 Smallest Image Pixel Value  */
    public static final int SMALLEST_IMAGE_PIXEL_VALUE = 0x00280106;
        
    /** (0028,0107) VR=US|SS, VM=1 Largest Image Pixel Value  */
    public static final int LARGEST_IMAGE_PIXEL_VALUE = 0x00280107;
        
    /** (0028,0108) VR=US|SS, VM=1 Smallest Pixel Value in Series  */
    public static final int SMALLEST_PIXEL_VALUE_IN_SERIES = 0x00280108;
        
    /** (0028,0109) VR=US|SS, VM=1 Largest Pixel Value in Series  */
    public static final int LARGEST_PIXEL_VALUE_IN_SERIES = 0x00280109;
        
    /** (0028,0110) VR=US|SS, VM=1 Smallest Image Pixel Value in Plane  */
    public static final int SMALLEST_IMAGE_PIXEL_VALUE_IN_PLANE = 0x00280110;
        
    /** (0028,0111) VR=US|SS, VM=1 Largest Image Pixel Value in Plane  */
    public static final int LARGEST_IMAGE_PIXEL_VALUE_IN_PLANE = 0x00280111;
        
    /** (0028,0120) VR=US|SS, VM=1 Pixel Padding Value  */
    public static final int PIXEL_PADDING_VALUE = 0x00280120;
        
    /** (0028,0200) VR=US, VM=1 Image Location RET */
    public static final int IMAGE_LOCATION_RET = 0x00280200;
        
    /** (0028,0300) VR=CS, VM=1 Quality Control Image  */
    public static final int QUALITY_CONTROL_IMAGE = 0x00280300;
        
    /** (0028,0301) VR=CS, VM=1 Burned In Annotation  */
    public static final int BURNED_IN_ANNOTATION = 0x00280301;
        
    /** (0028,0402) VR=CS, VM=1 Pixel Spacing Calibration Type  */
    public static final int PIXEL_SPACING_CALIBRATION_TYPE = 0x00280402;
        
    /** (0028,0404) VR=LO, VM=1 Pixel Spacing Calibration Description  */
    public static final int PIXEL_SPACING_CALIBRATION_DESCRIPTION = 0x00280404;
        
    /** (0028,1040) VR=CS, VM=1 Pixel Intensity Relationship  */
    public static final int PIXEL_INTENSITY_RELATIONSHIP = 0x00281040;
        
    /** (0028,1041) VR=SS, VM=1 Pixel Intensity Relationship Sign  */
    public static final int PIXEL_INTENSITY_RELATIONSHIP_SIGN = 0x00281041;
        
    /** (0028,1050) VR=DS, VM=1-n Window Center  */
    public static final int WINDOW_CENTER = 0x00281050;
        
    /** (0028,1051) VR=DS, VM=1-n Window Width  */
    public static final int WINDOW_WIDTH = 0x00281051;
        
    /** (0028,1052) VR=DS, VM=1 Rescale Intercept  */
    public static final int RESCALE_INTERCEPT = 0x00281052;
        
    /** (0028,1053) VR=DS, VM=1 Rescale Slope  */
    public static final int RESCALE_SLOPE = 0x00281053;
        
    /** (0028,1054) VR=LO, VM=1 Rescale Type  */
    public static final int RESCALE_TYPE = 0x00281054;
        
    /** (0028,1055) VR=LO, VM=1-n Window Center & Width Explanation  */
    public static final int WINDOW_CENTER_WIDTH_EXPLANATION = 0x00281055;
        
    /** (0028,1056) VR=CS, VM=1 VOI LUT Function  */
    public static final int VOI_LUT_FUNCTION = 0x00281056;
        
    /** (0028,1080) VR=CS, VM=1 Gray Scale RET */
    public static final int GRAY_SCALE_RET = 0x00281080;
        
    /** (0028,1090) VR=CS, VM=1 Recommended Viewing Mode  */
    public static final int RECOMMENDED_VIEWING_MODE = 0x00281090;
        
    /** (0028,1100) VR=US|SS, VM=3 Gray Lookup Table Descriptor RET */
    public static final int GRAY_LOOKUP_TABLE_DESCRIPTOR_RET = 0x00281100;
        
    /** (0028,1101) VR=US|SS, VM=3 Red Palette Color Lookup Table Descriptor  */
    public static final int RED_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR = 0x00281101;
        
    /** (0028,1102) VR=US|SS, VM=3 Green Palette Color Lookup Table Descriptor  */
    public static final int GREEN_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR = 0x00281102;
        
    /** (0028,1103) VR=US|SS, VM=3 Blue Palette Color Lookup Table Descriptor  */
    public static final int BLUE_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR = 0x00281103;
        
    /** (0028,1199) VR=UI, VM=1 Palette Color Lookup Table UID  */
    public static final int PALETTE_COLOR_LOOKUP_TABLE_UID = 0x00281199;
        
    /** (0028,1200) VR=US|SS|OW, VM=1-n1 Gray Lookup Table Data RET */
    public static final int GRAY_LOOKUP_TABLE_DATA_RET = 0x00281200;
        
    /** (0028,1201) VR=OW, VM=1 Red Palette Color Lookup Table Data  */
    public static final int RED_PALETTE_COLOR_LOOKUP_TABLE_DATA = 0x00281201;
        
    /** (0028,1202) VR=OW, VM=1 Green Palette Color Lookup Table Data  */
    public static final int GREEN_PALETTE_COLOR_LOOKUP_TABLE_DATA = 0x00281202;
        
    /** (0028,1203) VR=OW, VM=1 Blue Palette Color Lookup Table Data  */
    public static final int BLUE_PALETTE_COLOR_LOOKUP_TABLE_DATA = 0x00281203;
        
    /** (0028,1221) VR=OW, VM=1 Segmented Red Palette Color Lookup Table Data  */
    public static final int SEGMENTED_RED_PALETTE_COLOR_LOOKUP_TABLE_DATA = 0x00281221;
        
    /** (0028,1222) VR=OW, VM=1 Segmented Green Palette Color Lookup Table Data  */
    public static final int SEGMENTED_GREEN_PALETTE_COLOR_LOOKUP_TABLE_DATA = 0x00281222;
        
    /** (0028,1223) VR=OW, VM=1 Segmented Blue Palette Color Lookup Table Data  */
    public static final int SEGMENTED_BLUE_PALETTE_COLOR_LOOKUP_TABLE_DATA = 0x00281223;
        
    /** (0028,1300) VR=CS, VM=1 Implant Present  */
    public static final int IMPLANT_PRESENT = 0x00281300;
        
    /** (0028,1350) VR=CS, VM=1 Partial View  */
    public static final int PARTIAL_VIEW = 0x00281350;
        
    /** (0028,1351) VR=ST, VM=1 Partial View Description  */
    public static final int PARTIAL_VIEW_DESCRIPTION = 0x00281351;
        
    /** (0028,1352) VR=SQ, VM=1 Partial View Code Sequence  */
    public static final int PARTIAL_VIEW_CODE_SEQUENCE = 0x00281352;
        
    /** (0028,135A) VR=CS, VM=1 Spatial Locations Preserved  */
    public static final int SPATIAL_LOCATIONS_PRESERVED = 0x0028135A;
        
    /** (0028,2000) VR=OB, VM=1 ICC Profile  */
    public static final int ICC_PROFILE = 0x00282000;
        
    /** (0028,2110) VR=CS, VM=1 Lossy Image Compression  */
    public static final int LOSSY_IMAGE_COMPRESSION = 0x00282110;
        
    /** (0028,2112) VR=DS, VM=1-n Lossy Image Compression Ratio  */
    public static final int LOSSY_IMAGE_COMPRESSION_RATIO = 0x00282112;
        
    /** (0028,2114) VR=CS, VM=1-n Lossy Image Compression Method  */
    public static final int LOSSY_IMAGE_COMPRESSION_METHOD = 0x00282114;
        
    /** (0028,3000) VR=SQ, VM=1 Modality LUT Sequence  */
    public static final int MODALITY_LUT_SEQUENCE = 0x00283000;
        
    /** (0028,3002) VR=US|SS, VM=3 LUT Descriptor  */
    public static final int LUT_DESCRIPTOR = 0x00283002;
        
    /** (0028,3003) VR=LO, VM=1 LUT Explanation  */
    public static final int LUT_EXPLANATION = 0x00283003;
        
    /** (0028,3004) VR=LO, VM=1 Modality LUT Type  */
    public static final int MODALITY_LUT_TYPE = 0x00283004;
        
    /** (0028,3006) VR=US|SS|OW, VM=1-n1 LUT Data  */
    public static final int LUT_DATA = 0x00283006;
        
    /** (0028,3010) VR=SQ, VM=1 VOI LUT Sequence  */
    public static final int VOI_LUT_SEQUENCE = 0x00283010;
        
    /** (0028,3110) VR=SQ, VM=1 Softcopy VOI LUT Sequence  */
    public static final int SOFTCOPY_VOI_LUT_SEQUENCE = 0x00283110;
        
    /** (0028,4000) VR=LT, VM=1 Image Presentation Comments RET */
    public static final int IMAGE_PRESENTATION_COMMENTS_RET = 0x00284000;
        
    /** (0028,5000) VR=SQ, VM=1 Bi-Plane Acquisition Sequence  */
    public static final int BI_PLANE_ACQUISITION_SEQUENCE = 0x00285000;
        
    /** (0028,6010) VR=US, VM=1 Representative Frame Number  */
    public static final int REPRESENTATIVE_FRAME_NUMBER = 0x00286010;
        
    /** (0028,6020) VR=US, VM=1-n Frame Numbers of Interest (FOI)  */
    public static final int FRAME_NUMBERS_OF_INTEREST_FOI = 0x00286020;
        
    /** (0028,6022) VR=LO, VM=1-n Frame(s) of Interest Description  */
    public static final int FRAMES_OF_INTEREST_DESCRIPTION = 0x00286022;
        
    /** (0028,6023) VR=CS, VM=1-n Frame of Interest Type  */
    public static final int FRAME_OF_INTEREST_TYPE = 0x00286023;
        
    /** (0028,6030) VR=US, VM=1-n Mask Pointer(s) RET */
    public static final int MASK_POINTERS_RET = 0x00286030;
        
    /** (0028,6040) VR=US, VM=1-n R Wave Pointer  */
    public static final int R_WAVE_POINTER = 0x00286040;
        
    /** (0028,6100) VR=SQ, VM=1 Mask Subtraction Sequence  */
    public static final int MASK_SUBTRACTION_SEQUENCE = 0x00286100;
        
    /** (0028,6101) VR=CS, VM=1 Mask Operation  */
    public static final int MASK_OPERATION = 0x00286101;
        
    /** (0028,6102) VR=US, VM=2-2n Applicable Frame Range  */
    public static final int APPLICABLE_FRAME_RANGE = 0x00286102;
        
    /** (0028,6110) VR=US, VM=1-n Mask Frame Numbers  */
    public static final int MASK_FRAME_NUMBERS = 0x00286110;
        
    /** (0028,6112) VR=US, VM=1 Contrast Frame Averaging  */
    public static final int CONTRAST_FRAME_AVERAGING = 0x00286112;
        
    /** (0028,6114) VR=FL, VM=2 Mask Sub-pixel Shift  */
    public static final int MASK_SUB_PIXEL_SHIFT = 0x00286114;
        
    /** (0028,6120) VR=SS, VM=1 TID Offset  */
    public static final int TID_OFFSET = 0x00286120;
        
    /** (0028,6190) VR=ST, VM=1 Mask Operation Explanation  */
    public static final int MASK_OPERATION_EXPLANATION = 0x00286190;
        
    /** (0028,7FE0) VR=UT, VM=1 Pixel Data Provider URL  */
    public static final int PIXEL_DATA_PROVIDER_URL = 0x00287FE0;
        
    /** (0028,9001) VR=UL, VM=1 Data Point Rows  */
    public static final int DATA_POINT_ROWS = 0x00289001;
        
    /** (0028,9002) VR=UL, VM=1 Data Point Columns  */
    public static final int DATA_POINT_COLUMNS = 0x00289002;
        
    /** (0028,9003) VR=CS, VM=1 Signal Domain Columns  */
    public static final int SIGNAL_DOMAIN_COLUMNS = 0x00289003;
        
    /** (0028,9099) VR=US, VM=1 Largest Monochrome Pixel Value RET */
    public static final int LARGEST_MONOCHROME_PIXEL_VALUE_RET = 0x00289099;
        
    /** (0028,9108) VR=CS, VM=1 Data Representation  */
    public static final int DATA_REPRESENTATION = 0x00289108;
        
    /** (0028,9110) VR=SQ, VM=1 Pixel Measures Sequence  */
    public static final int PIXEL_MEASURES_SEQUENCE = 0x00289110;
        
    /** (0028,9132) VR=SQ, VM=1 Frame VOI LUT Sequence  */
    public static final int FRAME_VOI_LUT_SEQUENCE = 0x00289132;
        
    /** (0028,9145) VR=SQ, VM=1 Pixel Value Transformation Sequence  */
    public static final int PIXEL_VALUE_TRANSFORMATION_SEQUENCE = 0x00289145;
        
    /** (0028,9235) VR=CS, VM=1 Signal Domain Rows  */
    public static final int SIGNAL_DOMAIN_ROWS = 0x00289235;
        
    /** (0028,9411) VR=FL, VM=1 Display Filter Percentage  */
    public static final int DISPLAY_FILTER_PERCENTAGE = 0x00289411;
        
    /** (0028,9415) VR=SQ, VM=1 Frame Pixel Shift Sequence  */
    public static final int FRAME_PIXEL_SHIFT_SEQUENCE = 0x00289415;
        
    /** (0028,9416) VR=US, VM=1 Subtraction Item ID  */
    public static final int SUBTRACTION_ITEM_ID = 0x00289416;
        
    /** (0028,9422) VR=SQ, VM=1 Pixel Intensity Relationship LUT Sequence  */
    public static final int PIXEL_INTENSITY_RELATIONSHIP_LUT_SEQUENCE = 0x00289422;
        
    /** (0028,9443) VR=SQ, VM=1 Frame Pixel Data Properties Sequence  */
    public static final int FRAME_PIXEL_DATA_PROPERTIES_SEQUENCE = 0x00289443;
        
    /** (0028,9444) VR=CS, VM=1 Geometrical Properties  */
    public static final int GEOMETRICAL_PROPERTIES = 0x00289444;
        
    /** (0028,9445) VR=FL, VM=1 Geometric Maximum Distortion  */
    public static final int GEOMETRIC_MAXIMUM_DISTORTION = 0x00289445;
        
    /** (0028,9446) VR=CS, VM=1-n Image Processing Applied  */
    public static final int IMAGE_PROCESSING_APPLIED = 0x00289446;
        
    /** (0028,9454) VR=CS, VM=1 Mask Selection Mode  */
    public static final int MASK_SELECTION_MODE = 0x00289454;
        
    /** (0028,9474) VR=CS, VM=1 LUT Function  */
    public static final int LUT_FUNCTION = 0x00289474;
        
    /** (0032,000A) VR=CS, VM=1 Study Status ID RET */
    public static final int STUDY_STATUS_ID_RET = 0x0032000A;
        
    /** (0032,000C) VR=CS, VM=1 Study Priority ID RET */
    public static final int STUDY_PRIORITY_ID_RET = 0x0032000C;
        
    /** (0032,0012) VR=LO, VM=1 Study ID Issuer RET */
    public static final int STUDY_ID_ISSUER_RET = 0x00320012;
        
    /** (0032,0032) VR=DA, VM=1 Study Verified Date RET */
    public static final int STUDY_VERIFIED_DATE_RET = 0x00320032;
        
    /** (0032,0033) VR=TM, VM=1 Study Verified Time RET */
    public static final int STUDY_VERIFIED_TIME_RET = 0x00320033;
        
    /** (0032,0034) VR=DA, VM=1 Study Read Date RET */
    public static final int STUDY_READ_DATE_RET = 0x00320034;
        
    /** (0032,0035) VR=TM, VM=1 Study Read Time RET */
    public static final int STUDY_READ_TIME_RET = 0x00320035;
        
    /** (0032,1000) VR=DA, VM=1 Scheduled Study Start Date RET */
    public static final int SCHEDULED_STUDY_START_DATE_RET = 0x00321000;
        
    /** (0032,1001) VR=TM, VM=1 Scheduled Study Start Time RET */
    public static final int SCHEDULED_STUDY_START_TIME_RET = 0x00321001;
        
    /** (0032,1010) VR=DA, VM=1 Scheduled Study Stop Date RET */
    public static final int SCHEDULED_STUDY_STOP_DATE_RET = 0x00321010;
        
    /** (0032,1011) VR=TM, VM=1 Scheduled Study Stop Time RET */
    public static final int SCHEDULED_STUDY_STOP_TIME_RET = 0x00321011;
        
    /** (0032,1020) VR=LO, VM=1 Scheduled Study Location RET */
    public static final int SCHEDULED_STUDY_LOCATION_RET = 0x00321020;
        
    /** (0032,1021) VR=AE, VM=1-n Scheduled Study Location AE Title RET */
    public static final int SCHEDULED_STUDY_LOCATION_AE_TITLE_RET = 0x00321021;
        
    /** (0032,1030) VR=LO, VM=1 Reason for Study RET */
    public static final int REASON_FOR_STUDY_RET = 0x00321030;
        
    /** (0032,1031) VR=SQ, VM=1 Requesting Physician Identification Sequence  */
    public static final int REQUESTING_PHYSICIAN_IDENTIFICATION_SEQUENCE = 0x00321031;
        
    /** (0032,1032) VR=PN, VM=1 Requesting Physician  */
    public static final int REQUESTING_PHYSICIAN = 0x00321032;
        
    /** (0032,1033) VR=LO, VM=1 Requesting Service  */
    public static final int REQUESTING_SERVICE = 0x00321033;
        
    /** (0032,1040) VR=DA, VM=1 Study Arrival Date RET */
    public static final int STUDY_ARRIVAL_DATE_RET = 0x00321040;
        
    /** (0032,1041) VR=TM, VM=1 Study Arrival Time RET */
    public static final int STUDY_ARRIVAL_TIME_RET = 0x00321041;
        
    /** (0032,1050) VR=DA, VM=1 Study Completion Date RET */
    public static final int STUDY_COMPLETION_DATE_RET = 0x00321050;
        
    /** (0032,1051) VR=TM, VM=1 Study Completion Time RET */
    public static final int STUDY_COMPLETION_TIME_RET = 0x00321051;
        
    /** (0032,1055) VR=CS, VM=1 Study Component Status ID RET */
    public static final int STUDY_COMPONENT_STATUS_ID_RET = 0x00321055;
        
    /** (0032,1060) VR=LO, VM=1 Requested Procedure Description  */
    public static final int REQUESTED_PROCEDURE_DESCRIPTION = 0x00321060;
        
    /** (0032,1064) VR=SQ, VM=1 Requested Procedure Code Sequence  */
    public static final int REQUESTED_PROCEDURE_CODE_SEQUENCE = 0x00321064;
        
    /** (0032,1070) VR=LO, VM=1 Requested Contrast Agent  */
    public static final int REQUESTED_CONTRAST_AGENT = 0x00321070;
        
    /** (0032,4000) VR=LT, VM=1 Study Comments  */
    public static final int STUDY_COMMENTS = 0x00324000;
        
    /** (0038,0004) VR=SQ, VM=1 Referenced Patient Alias Sequence  */
    public static final int REFERENCED_PATIENT_ALIAS_SEQUENCE = 0x00380004;
        
    /** (0038,0008) VR=CS, VM=1 Visit Status ID  */
    public static final int VISIT_STATUS_ID = 0x00380008;
        
    /** (0038,0010) VR=LO, VM=1 Admission ID  */
    public static final int ADMISSION_ID = 0x00380010;
        
    /** (0038,0011) VR=LO, VM=1 Issuer of Admission ID  */
    public static final int ISSUER_OF_ADMISSION_ID = 0x00380011;
        
    /** (0038,0016) VR=LO, VM=1 Route of Admissions  */
    public static final int ROUTE_OF_ADMISSIONS = 0x00380016;
        
    /** (0038,001A) VR=DA, VM=1 Scheduled Admission Date RET */
    public static final int SCHEDULED_ADMISSION_DATE_RET = 0x0038001A;
        
    /** (0038,001B) VR=TM, VM=1 Scheduled Admission Time RET */
    public static final int SCHEDULED_ADMISSION_TIME_RET = 0x0038001B;
        
    /** (0038,001C) VR=DA, VM=1 Scheduled Discharge Date RET */
    public static final int SCHEDULED_DISCHARGE_DATE_RET = 0x0038001C;
        
    /** (0038,001D) VR=TM, VM=1 Scheduled Discharge Time RET */
    public static final int SCHEDULED_DISCHARGE_TIME_RET = 0x0038001D;
        
    /** (0038,001E) VR=LO, VM=1 Scheduled Patient Institution Residence RET */
    public static final int SCHEDULED_PATIENT_INSTITUTION_RESIDENCE_RET = 0x0038001E;
        
    /** (0038,0020) VR=DA, VM=1 Admitting Date  */
    public static final int ADMITTING_DATE = 0x00380020;
        
    /** (0038,0021) VR=TM, VM=1 Admitting Time  */
    public static final int ADMITTING_TIME = 0x00380021;
        
    /** (0038,0030) VR=DA, VM=1 Discharge Date RET */
    public static final int DISCHARGE_DATE_RET = 0x00380030;
        
    /** (0038,0032) VR=TM, VM=1 Discharge Time RET */
    public static final int DISCHARGE_TIME_RET = 0x00380032;
        
    /** (0038,0040) VR=LO, VM=1 Discharge Diagnosis Description RET */
    public static final int DISCHARGE_DIAGNOSIS_DESCRIPTION_RET = 0x00380040;
        
    /** (0038,0044) VR=SQ, VM=1 Discharge Diagnosis Code Sequence RET */
    public static final int DISCHARGE_DIAGNOSIS_CODE_SEQUENCE_RET = 0x00380044;
        
    /** (0038,0050) VR=LO, VM=1 Special Needs  */
    public static final int SPECIAL_NEEDS = 0x00380050;
        
    /** (0038,0100) VR=SQ, VM=1 Pertinent Documents Sequence  */
    public static final int PERTINENT_DOCUMENTS_SEQUENCE = 0x00380100;
        
    /** (0038,0300) VR=LO, VM=1 Current Patient Location  */
    public static final int CURRENT_PATIENT_LOCATION = 0x00380300;
        
    /** (0038,0400) VR=LO, VM=1 Patient's Institution Residence  */
    public static final int PATIENTS_INSTITUTION_RESIDENCE = 0x00380400;
        
    /** (0038,0500) VR=LO, VM=1 Patient State  */
    public static final int PATIENT_STATE = 0x00380500;
        
    /** (0038,0502) VR=SQ, VM=1 Patient Clinical Trial Participation Sequence  */
    public static final int PATIENT_CLINICAL_TRIAL_PARTICIPATION_SEQUENCE = 0x00380502;
        
    /** (0038,4000) VR=LT, VM=1 Visit Comments  */
    public static final int VISIT_COMMENTS = 0x00384000;
        
    /** (003A,0004) VR=CS, VM=1 Waveform Originality  */
    public static final int WAVEFORM_ORIGINALITY = 0x003A0004;
        
    /** (003A,0005) VR=US, VM=1 Number of Waveform Channels  */
    public static final int NUMBER_OF_WAVEFORM_CHANNELS = 0x003A0005;
        
    /** (003A,0010) VR=UL, VM=1 Number of Waveform Samples  */
    public static final int NUMBER_OF_WAVEFORM_SAMPLES = 0x003A0010;
        
    /** (003A,001A) VR=DS, VM=1 Sampling Frequency  */
    public static final int SAMPLING_FREQUENCY = 0x003A001A;
        
    /** (003A,0020) VR=SH, VM=1 Multiplex Group Label  */
    public static final int MULTIPLEX_GROUP_LABEL = 0x003A0020;
        
    /** (003A,0200) VR=SQ, VM=1 Channel Definition Sequence  */
    public static final int CHANNEL_DEFINITION_SEQUENCE = 0x003A0200;
        
    /** (003A,0202) VR=IS, VM=1 Waveform Channel Number  */
    public static final int WAVEFORM_CHANNEL_NUMBER = 0x003A0202;
        
    /** (003A,0203) VR=SH, VM=1 Channel Label  */
    public static final int CHANNEL_LABEL = 0x003A0203;
        
    /** (003A,0205) VR=CS, VM=1-n Channel Status  */
    public static final int CHANNEL_STATUS = 0x003A0205;
        
    /** (003A,0208) VR=SQ, VM=1 Channel Source Sequence  */
    public static final int CHANNEL_SOURCE_SEQUENCE = 0x003A0208;
        
    /** (003A,0209) VR=SQ, VM=1 Channel Source Modifiers Sequence  */
    public static final int CHANNEL_SOURCE_MODIFIERS_SEQUENCE = 0x003A0209;
        
    /** (003A,020A) VR=SQ, VM=1 Source Waveform Sequence  */
    public static final int SOURCE_WAVEFORM_SEQUENCE = 0x003A020A;
        
    /** (003A,020C) VR=LO, VM=1 Channel Derivation Description  */
    public static final int CHANNEL_DERIVATION_DESCRIPTION = 0x003A020C;
        
    /** (003A,0210) VR=DS, VM=1 Channel Sensitivity  */
    public static final int CHANNEL_SENSITIVITY = 0x003A0210;
        
    /** (003A,0211) VR=SQ, VM=1 Channel Sensitivity Units Sequence  */
    public static final int CHANNEL_SENSITIVITY_UNITS_SEQUENCE = 0x003A0211;
        
    /** (003A,0212) VR=DS, VM=1 Channel Sensitivity Correction Factor  */
    public static final int CHANNEL_SENSITIVITY_CORRECTION_FACTOR = 0x003A0212;
        
    /** (003A,0213) VR=DS, VM=1 Channel Baseline  */
    public static final int CHANNEL_BASELINE = 0x003A0213;
        
    /** (003A,0214) VR=DS, VM=1 Channel Time Skew  */
    public static final int CHANNEL_TIME_SKEW = 0x003A0214;
        
    /** (003A,0215) VR=DS, VM=1 Channel Sample Skew  */
    public static final int CHANNEL_SAMPLE_SKEW = 0x003A0215;
        
    /** (003A,0218) VR=DS, VM=1 Channel Offset  */
    public static final int CHANNEL_OFFSET = 0x003A0218;
        
    /** (003A,021A) VR=US, VM=1 Waveform Bits Stored  */
    public static final int WAVEFORM_BITS_STORED = 0x003A021A;
        
    /** (003A,0220) VR=DS, VM=1 Filter Low Frequency  */
    public static final int FILTER_LOW_FREQUENCY = 0x003A0220;
        
    /** (003A,0221) VR=DS, VM=1 Filter High Frequency  */
    public static final int FILTER_HIGH_FREQUENCY = 0x003A0221;
        
    /** (003A,0222) VR=DS, VM=1 Notch Filter Frequency  */
    public static final int NOTCH_FILTER_FREQUENCY = 0x003A0222;
        
    /** (003A,0223) VR=DS, VM=1 Notch Filter Bandwidth  */
    public static final int NOTCH_FILTER_BANDWIDTH = 0x003A0223;
        
    /** (003A,0300) VR=SQ, VM=1 Multiplexed Audio Channels Description Code Sequence  */
    public static final int MULTIPLEXED_AUDIO_CHANNELS_DESCRIPTION_CODE_SEQUENCE = 0x003A0300;
        
    /** (003A,0301) VR=IS, VM=1 Channel Identification Code  */
    public static final int CHANNEL_IDENTIFICATION_CODE = 0x003A0301;
        
    /** (003A,0302) VR=CS, VM=1 Channel Mode  */
    public static final int CHANNEL_MODE = 0x003A0302;
        
    /** (0040,0001) VR=AE, VM=1-n Scheduled Station AE Title  */
    public static final int SCHEDULED_STATION_AE_TITLE = 0x00400001;
        
    /** (0040,0002) VR=DA, VM=1 Scheduled Procedure Step Start Date  */
    public static final int SCHEDULED_PROCEDURE_STEP_START_DATE = 0x00400002;
        
    /** (0040,0003) VR=TM, VM=1 Scheduled Procedure Step Start Time  */
    public static final int SCHEDULED_PROCEDURE_STEP_START_TIME = 0x00400003;
        
    /** (0040,0004) VR=DA, VM=1 Scheduled Procedure Step End Date  */
    public static final int SCHEDULED_PROCEDURE_STEP_END_DATE = 0x00400004;
        
    /** (0040,0005) VR=TM, VM=1 Scheduled Procedure Step End Time  */
    public static final int SCHEDULED_PROCEDURE_STEP_END_TIME = 0x00400005;
        
    /** (0040,0006) VR=PN, VM=1 Scheduled Performing Physician's Name  */
    public static final int SCHEDULED_PERFORMING_PHYSICIANS_NAME = 0x00400006;
        
    /** (0040,0007) VR=LO, VM=1 Scheduled Procedure Step Description  */
    public static final int SCHEDULED_PROCEDURE_STEP_DESCRIPTION = 0x00400007;
        
    /** (0040,0008) VR=SQ, VM=1 Scheduled Protocol Code Sequence  */
    public static final int SCHEDULED_PROTOCOL_CODE_SEQUENCE = 0x00400008;
        
    /** (0040,0009) VR=SH, VM=1 Scheduled Procedure Step ID  */
    public static final int SCHEDULED_PROCEDURE_STEP_ID = 0x00400009;
        
    /** (0040,000A) VR=SQ, VM=1 Stage Code Sequence  */
    public static final int STAGE_CODE_SEQUENCE = 0x0040000A;
        
    /** (0040,000B) VR=SQ, VM=1 Scheduled Performing Physician Identification Sequence  */
    public static final int SCHEDULED_PERFORMING_PHYSICIAN_IDENTIFICATION_SEQUENCE = 0x0040000B;
        
    /** (0040,0010) VR=SH, VM=1-n Scheduled Station Name  */
    public static final int SCHEDULED_STATION_NAME = 0x00400010;
        
    /** (0040,0011) VR=SH, VM=1 Scheduled Procedure Step Location  */
    public static final int SCHEDULED_PROCEDURE_STEP_LOCATION = 0x00400011;
        
    /** (0040,0012) VR=LO, VM=1 Pre-Medication  */
    public static final int PRE_MEDICATION = 0x00400012;
        
    /** (0040,0020) VR=CS, VM=1 Scheduled Procedure Step Status  */
    public static final int SCHEDULED_PROCEDURE_STEP_STATUS = 0x00400020;
        
    /** (0040,0100) VR=SQ, VM=1 Scheduled Procedure Step Sequence  */
    public static final int SCHEDULED_PROCEDURE_STEP_SEQUENCE = 0x00400100;
        
    /** (0040,0220) VR=SQ, VM=1 Referenced Non-Image Composite SOP Instance Sequence  */
    public static final int REFERENCED_NON_IMAGE_COMPOSITE_SOP_INSTANCE_SEQUENCE = 0x00400220;
        
    /** (0040,0241) VR=AE, VM=1 Performed Station AE Title  */
    public static final int PERFORMED_STATION_AE_TITLE = 0x00400241;
        
    /** (0040,0242) VR=SH, VM=1 Performed Station Name  */
    public static final int PERFORMED_STATION_NAME = 0x00400242;
        
    /** (0040,0243) VR=SH, VM=1 Performed Location  */
    public static final int PERFORMED_LOCATION = 0x00400243;
        
    /** (0040,0244) VR=DA, VM=1 Performed Procedure Step Start Date  */
    public static final int PERFORMED_PROCEDURE_STEP_START_DATE = 0x00400244;
        
    /** (0040,0245) VR=TM, VM=1 Performed Procedure Step Start Time  */
    public static final int PERFORMED_PROCEDURE_STEP_START_TIME = 0x00400245;
        
    /** (0040,0250) VR=DA, VM=1 Performed Procedure Step End Date  */
    public static final int PERFORMED_PROCEDURE_STEP_END_DATE = 0x00400250;
        
    /** (0040,0251) VR=TM, VM=1 Performed Procedure Step End Time  */
    public static final int PERFORMED_PROCEDURE_STEP_END_TIME = 0x00400251;
        
    /** (0040,0252) VR=CS, VM=1 Performed Procedure Step Status  */
    public static final int PERFORMED_PROCEDURE_STEP_STATUS = 0x00400252;
        
    /** (0040,0253) VR=SH, VM=1 Performed Procedure Step ID  */
    public static final int PERFORMED_PROCEDURE_STEP_ID = 0x00400253;
        
    /** (0040,0254) VR=LO, VM=1 Performed Procedure Step Description  */
    public static final int PERFORMED_PROCEDURE_STEP_DESCRIPTION = 0x00400254;
        
    /** (0040,0255) VR=LO, VM=1 Performed Procedure Type Description  */
    public static final int PERFORMED_PROCEDURE_TYPE_DESCRIPTION = 0x00400255;
        
    /** (0040,0260) VR=SQ, VM=1 Performed Protocol Code Sequence  */
    public static final int PERFORMED_PROTOCOL_CODE_SEQUENCE = 0x00400260;
        
    /** (0040,0270) VR=SQ, VM=1 Scheduled Step Attributes Sequence  */
    public static final int SCHEDULED_STEP_ATTRIBUTES_SEQUENCE = 0x00400270;
        
    /** (0040,0275) VR=SQ, VM=1 Request Attributes Sequence  */
    public static final int REQUEST_ATTRIBUTES_SEQUENCE = 0x00400275;
        
    /** (0040,0280) VR=ST, VM=1 Comments on the Performed Procedure Step  */
    public static final int COMMENTS_ON_THE_PERFORMED_PROCEDURE_STEP = 0x00400280;
        
    /** (0040,0281) VR=SQ, VM=1 Performed Procedure Step Discontinuation Reason Code Sequence  */
    public static final int PERFORMED_PROCEDURE_STEP_DISCONTINUATION_REASON_CODE_SEQUENCE = 0x00400281;
        
    /** (0040,0293) VR=SQ, VM=1 Quantity Sequence  */
    public static final int QUANTITY_SEQUENCE = 0x00400293;
        
    /** (0040,0294) VR=DS, VM=1 Quantity  */
    public static final int QUANTITY = 0x00400294;
        
    /** (0040,0295) VR=SQ, VM=1 Measuring Units Sequence  */
    public static final int MEASURING_UNITS_SEQUENCE = 0x00400295;
        
    /** (0040,0296) VR=SQ, VM=1 Billing Item Sequence  */
    public static final int BILLING_ITEM_SEQUENCE = 0x00400296;
        
    /** (0040,0300) VR=US, VM=1 Total Time of Fluoroscopy  */
    public static final int TOTAL_TIME_OF_FLUOROSCOPY = 0x00400300;
        
    /** (0040,0301) VR=US, VM=1 Total Number of Exposures  */
    public static final int TOTAL_NUMBER_OF_EXPOSURES = 0x00400301;
        
    /** (0040,0302) VR=US, VM=1 Entrance Dose  */
    public static final int ENTRANCE_DOSE = 0x00400302;
        
    /** (0040,0303) VR=US, VM=1-2 Exposed Area  */
    public static final int EXPOSED_AREA = 0x00400303;
        
    /** (0040,0306) VR=DS, VM=1 Distance Source to Entrance  */
    public static final int DISTANCE_SOURCE_TO_ENTRANCE = 0x00400306;
        
    /** (0040,0307) VR=DS, VM=1 Distance Source to Support RET */
    public static final int DISTANCE_SOURCE_TO_SUPPORT_RET = 0x00400307;
        
    /** (0040,030E) VR=SQ, VM=1 Exposure Dose Sequence  */
    public static final int EXPOSURE_DOSE_SEQUENCE = 0x0040030E;
        
    /** (0040,0310) VR=ST, VM=1 Comments on Radiation Dose  */
    public static final int COMMENTS_ON_RADIATION_DOSE = 0x00400310;
        
    /** (0040,0312) VR=DS, VM=1 X-Ray Output  */
    public static final int X_RAY_OUTPUT = 0x00400312;
        
    /** (0040,0314) VR=DS, VM=1 Half Value Layer  */
    public static final int HALF_VALUE_LAYER = 0x00400314;
        
    /** (0040,0316) VR=DS, VM=1 Organ Dose  */
    public static final int ORGAN_DOSE = 0x00400316;
        
    /** (0040,0318) VR=CS, VM=1 Organ Exposed  */
    public static final int ORGAN_EXPOSED = 0x00400318;
        
    /** (0040,0320) VR=SQ, VM=1 Billing Procedure Step Sequence  */
    public static final int BILLING_PROCEDURE_STEP_SEQUENCE = 0x00400320;
        
    /** (0040,0321) VR=SQ, VM=1 Film Consumption Sequence  */
    public static final int FILM_CONSUMPTION_SEQUENCE = 0x00400321;
        
    /** (0040,0324) VR=SQ, VM=1 Billing Supplies and Devices Sequence  */
    public static final int BILLING_SUPPLIES_AND_DEVICES_SEQUENCE = 0x00400324;
        
    /** (0040,0330) VR=SQ, VM=1 Referenced Procedure Step Sequence RET */
    public static final int REFERENCED_PROCEDURE_STEP_SEQUENCE_RET = 0x00400330;
        
    /** (0040,0340) VR=SQ, VM=1 Performed Series Sequence  */
    public static final int PERFORMED_SERIES_SEQUENCE = 0x00400340;
        
    /** (0040,0400) VR=LT, VM=1 Comments on the Scheduled Procedure Step  */
    public static final int COMMENTS_ON_THE_SCHEDULED_PROCEDURE_STEP = 0x00400400;
        
    /** (0040,0440) VR=SQ, VM=1 Protocol Context Sequence  */
    public static final int PROTOCOL_CONTEXT_SEQUENCE = 0x00400440;
        
    /** (0040,0441) VR=SQ, VM=1 Content Item Modifier Sequence  */
    public static final int CONTENT_ITEM_MODIFIER_SEQUENCE = 0x00400441;
        
    /** (0040,050A) VR=LO, VM=1 Specimen Accession Number  */
    public static final int SPECIMEN_ACCESSION_NUMBER = 0x0040050A;
        
    /** (0040,0550) VR=SQ, VM=1 Specimen Sequence  */
    public static final int SPECIMEN_SEQUENCE = 0x00400550;
        
    /** (0040,0551) VR=LO, VM=1 Specimen Identifier  */
    public static final int SPECIMEN_IDENTIFIER = 0x00400551;
        
    /** (0040,0555) VR=SQ, VM=1 Acquisition Context Sequence  */
    public static final int ACQUISITION_CONTEXT_SEQUENCE = 0x00400555;
        
    /** (0040,0556) VR=ST, VM=1 Acquisition Context Description  */
    public static final int ACQUISITION_CONTEXT_DESCRIPTION = 0x00400556;
        
    /** (0040,059A) VR=SQ, VM=1 Specimen Type Code Sequence  */
    public static final int SPECIMEN_TYPE_CODE_SEQUENCE = 0x0040059A;
        
    /** (0040,06FA) VR=LO, VM=1 Slide Identifier  */
    public static final int SLIDE_IDENTIFIER = 0x004006FA;
        
    /** (0040,071A) VR=SQ, VM=1 Image Center Point Coordinates Sequence  */
    public static final int IMAGE_CENTER_POINT_COORDINATES_SEQUENCE = 0x0040071A;
        
    /** (0040,072A) VR=DS, VM=1 X offset in Slide Coordinate System  */
    public static final int X_OFFSET_IN_SLIDE_COORDINATE_SYSTEM = 0x0040072A;
        
    /** (0040,073A) VR=DS, VM=1 Y offset in Slide Coordinate System  */
    public static final int Y_OFFSET_IN_SLIDE_COORDINATE_SYSTEM = 0x0040073A;
        
    /** (0040,074A) VR=DS, VM=1 Z offset in Slide Coordinate System  */
    public static final int Z_OFFSET_IN_SLIDE_COORDINATE_SYSTEM = 0x0040074A;
        
    /** (0040,08D8) VR=SQ, VM=1 Pixel Spacing Sequence  */
    public static final int PIXEL_SPACING_SEQUENCE = 0x004008D8;
        
    /** (0040,08DA) VR=SQ, VM=1 Coordinate System Axis Code Sequence  */
    public static final int COORDINATE_SYSTEM_AXIS_CODE_SEQUENCE = 0x004008DA;
        
    /** (0040,08EA) VR=SQ, VM=1 Measurement Units Code Sequence  */
    public static final int MEASUREMENT_UNITS_CODE_SEQUENCE = 0x004008EA;
        
    /** (0040,1001) VR=SH, VM=1 Requested Procedure ID  */
    public static final int REQUESTED_PROCEDURE_ID = 0x00401001;
        
    /** (0040,1002) VR=LO, VM=1 Reason for the Requested Procedure  */
    public static final int REASON_FOR_THE_REQUESTED_PROCEDURE = 0x00401002;
        
    /** (0040,1003) VR=SH, VM=1 Requested Procedure Priority  */
    public static final int REQUESTED_PROCEDURE_PRIORITY = 0x00401003;
        
    /** (0040,1004) VR=LO, VM=1 Patient Transport Arrangements  */
    public static final int PATIENT_TRANSPORT_ARRANGEMENTS = 0x00401004;
        
    /** (0040,1005) VR=LO, VM=1 Requested Procedure Location  */
    public static final int REQUESTED_PROCEDURE_LOCATION = 0x00401005;
        
    /** (0040,1006) VR=SH, VM=1 Placer Order Number / Procedure RET */
    public static final int PLACER_ORDER_NUMBER_PROCEDURE_RET = 0x00401006;
        
    /** (0040,1007) VR=SH, VM=1 Filler Order Number / Procedure RET */
    public static final int FILLER_ORDER_NUMBER_PROCEDURE_RET = 0x00401007;
        
    /** (0040,1008) VR=LO, VM=1 Confidentiality Code  */
    public static final int CONFIDENTIALITY_CODE = 0x00401008;
        
    /** (0040,1009) VR=SH, VM=1 Reporting Priority  */
    public static final int REPORTING_PRIORITY = 0x00401009;
        
    /** (0040,100A) VR=SQ, VM=1 Reason for Requested Procedure Code Sequence  */
    public static final int REASON_FOR_REQUESTED_PROCEDURE_CODE_SEQUENCE = 0x0040100A;
        
    /** (0040,1010) VR=PN, VM=1-n Names of Intended Recipients of Results  */
    public static final int NAMES_OF_INTENDED_RECIPIENTS_OF_RESULTS = 0x00401010;
        
    /** (0040,1011) VR=SQ, VM=1 Intended Recipients of Results Identification Sequence  */
    public static final int INTENDED_RECIPIENTS_OF_RESULTS_IDENTIFICATION_SEQUENCE = 0x00401011;
        
    /** (0040,1101) VR=SQ, VM=1 Person Identification Code Sequence  */
    public static final int PERSON_IDENTIFICATION_CODE_SEQUENCE = 0x00401101;
        
    /** (0040,1102) VR=ST, VM=1 Person's Address  */
    public static final int PERSONS_ADDRESS = 0x00401102;
        
    /** (0040,1103) VR=LO, VM=1-n Person's Telephone Numbers  */
    public static final int PERSONS_TELEPHONE_NUMBERS = 0x00401103;
        
    /** (0040,1400) VR=LT, VM=1 Requested Procedure Comments  */
    public static final int REQUESTED_PROCEDURE_COMMENTS = 0x00401400;
        
    /** (0040,2001) VR=LO, VM=1 Reason for the Imaging Service Request RET */
    public static final int REASON_FOR_THE_IMAGING_SERVICE_REQUEST_RET = 0x00402001;
        
    /** (0040,2004) VR=DA, VM=1 Issue Date of Imaging Service Request  */
    public static final int ISSUE_DATE_OF_IMAGING_SERVICE_REQUEST = 0x00402004;
        
    /** (0040,2005) VR=TM, VM=1 Issue Time of Imaging Service Request  */
    public static final int ISSUE_TIME_OF_IMAGING_SERVICE_REQUEST = 0x00402005;
        
    /** (0040,2006) VR=SH, VM=1 Placer Order Number / Imaging Service Request RET */
    public static final int PLACER_ORDER_NUMBER_IMAGING_SERVICE_REQUEST_RET = 0x00402006;
        
    /** (0040,2007) VR=SH, VM=1 Filler Order Number / Imaging Service Request RET */
    public static final int FILLER_ORDER_NUMBER_IMAGING_SERVICE_REQUEST_RET = 0x00402007;
        
    /** (0040,2008) VR=PN, VM=1 Order Entered By  */
    public static final int ORDER_ENTERED_BY = 0x00402008;
        
    /** (0040,2009) VR=SH, VM=1 Order Enterer's Location  */
    public static final int ORDER_ENTERERS_LOCATION = 0x00402009;
        
    /** (0040,2010) VR=SH, VM=1 Order Callback Phone Number  */
    public static final int ORDER_CALLBACK_PHONE_NUMBER = 0x00402010;
        
    /** (0040,2016) VR=LO, VM=1 Placer Order Number / Imaging Service Request  */
    public static final int PLACER_ORDER_NUMBER_IMAGING_SERVICE_REQUEST = 0x00402016;
        
    /** (0040,2017) VR=LO, VM=1 Filler Order Number / Imaging Service Request  */
    public static final int FILLER_ORDER_NUMBER_IMAGING_SERVICE_REQUEST = 0x00402017;
        
    /** (0040,2400) VR=LT, VM=1 Imaging Service Request Comments  */
    public static final int IMAGING_SERVICE_REQUEST_COMMENTS = 0x00402400;
        
    /** (0040,3001) VR=LO, VM=1 Confidentiality Constraint on Patient Data Description  */
    public static final int CONFIDENTIALITY_CONSTRAINT_ON_PATIENT_DATA_DESCRIPTION = 0x00403001;
        
    /** (0040,4001) VR=CS, VM=1 General Purpose Scheduled Procedure Step Status  */
    public static final int GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_STATUS = 0x00404001;
        
    /** (0040,4002) VR=CS, VM=1 General Purpose Performed Procedure Step Status  */
    public static final int GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_STATUS = 0x00404002;
        
    /** (0040,4003) VR=CS, VM=1 General Purpose Scheduled Procedure Step Priority  */
    public static final int GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_PRIORITY = 0x00404003;
        
    /** (0040,4004) VR=SQ, VM=1 Scheduled Processing Applications Code Sequence  */
    public static final int SCHEDULED_PROCESSING_APPLICATIONS_CODE_SEQUENCE = 0x00404004;
        
    /** (0040,4005) VR=DT, VM=1 Scheduled Procedure Step Start Date and Time  */
    public static final int SCHEDULED_PROCEDURE_STEP_START_DATE_AND_TIME = 0x00404005;
        
    /** (0040,4006) VR=CS, VM=1 Multiple Copies Flag  */
    public static final int MULTIPLE_COPIES_FLAG = 0x00404006;
        
    /** (0040,4007) VR=SQ, VM=1 Performed Processing Applications Code Sequence  */
    public static final int PERFORMED_PROCESSING_APPLICATIONS_CODE_SEQUENCE = 0x00404007;
        
    /** (0040,4009) VR=SQ, VM=1 Human Performer Code Sequence  */
    public static final int HUMAN_PERFORMER_CODE_SEQUENCE = 0x00404009;
        
    /** (0040,4010) VR=DT, VM=1 Scheduled Procedure Step Modification Date and Time  */
    public static final int SCHEDULED_PROCEDURE_STEP_MODIFICATION_DATE_AND_TIME = 0x00404010;
        
    /** (0040,4011) VR=DT, VM=1 Expected Completion Date and Time  */
    public static final int EXPECTED_COMPLETION_DATE_AND_TIME = 0x00404011;
        
    /** (0040,4015) VR=SQ, VM=1 Resulting General Purpose Performed Procedure Steps Sequence  */
    public static final int RESULTING_GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEPS_SEQUENCE = 0x00404015;
        
    /** (0040,4016) VR=SQ, VM=1 Referenced General Purpose Scheduled Procedure Step Sequence  */
    public static final int REFERENCED_GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_SEQUENCE = 0x00404016;
        
    /** (0040,4018) VR=SQ, VM=1 Scheduled Workitem Code Sequence  */
    public static final int SCHEDULED_WORKITEM_CODE_SEQUENCE = 0x00404018;
        
    /** (0040,4019) VR=SQ, VM=1 Performed Workitem Code Sequence  */
    public static final int PERFORMED_WORKITEM_CODE_SEQUENCE = 0x00404019;
        
    /** (0040,4020) VR=CS, VM=1 Input Availability Flag  */
    public static final int INPUT_AVAILABILITY_FLAG = 0x00404020;
        
    /** (0040,4021) VR=SQ, VM=1 Input Information Sequence  */
    public static final int INPUT_INFORMATION_SEQUENCE = 0x00404021;
        
    /** (0040,4022) VR=SQ, VM=1 Relevant Information Sequence  */
    public static final int RELEVANT_INFORMATION_SEQUENCE = 0x00404022;
        
    /** (0040,4023) VR=UI, VM=1 Referenced General Purpose Scheduled Procedure Step Transaction UID  */
    public static final int REFERENCED_GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_TRANSACTION_UID = 0x00404023;
        
    /** (0040,4025) VR=SQ, VM=1 Scheduled Station Name Code Sequence  */
    public static final int SCHEDULED_STATION_NAME_CODE_SEQUENCE = 0x00404025;
        
    /** (0040,4026) VR=SQ, VM=1 Scheduled Station Class Code Sequence  */
    public static final int SCHEDULED_STATION_CLASS_CODE_SEQUENCE = 0x00404026;
        
    /** (0040,4027) VR=SQ, VM=1 Scheduled Station Geographic Location Code Sequence  */
    public static final int SCHEDULED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE = 0x00404027;
        
    /** (0040,4028) VR=SQ, VM=1 Performed Station Name Code Sequence  */
    public static final int PERFORMED_STATION_NAME_CODE_SEQUENCE = 0x00404028;
        
    /** (0040,4029) VR=SQ, VM=1 Performed Station Class Code Sequence  */
    public static final int PERFORMED_STATION_CLASS_CODE_SEQUENCE = 0x00404029;
        
    /** (0040,4030) VR=SQ, VM=1 Performed Station Geographic Location Code Sequence  */
    public static final int PERFORMED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE = 0x00404030;
        
    /** (0040,4031) VR=SQ, VM=1 Requested Subsequent Workitem Code Sequence  */
    public static final int REQUESTED_SUBSEQUENT_WORKITEM_CODE_SEQUENCE = 0x00404031;
        
    /** (0040,4032) VR=SQ, VM=1 Non-DICOM Output Code Sequence  */
    public static final int NON_DICOM_OUTPUT_CODE_SEQUENCE = 0x00404032;
        
    /** (0040,4033) VR=SQ, VM=1 Output Information Sequence  */
    public static final int OUTPUT_INFORMATION_SEQUENCE = 0x00404033;
        
    /** (0040,4034) VR=SQ, VM=1 Scheduled Human Performers Sequence  */
    public static final int SCHEDULED_HUMAN_PERFORMERS_SEQUENCE = 0x00404034;
        
    /** (0040,4035) VR=SQ, VM=1 Actual Human Performers Sequence  */
    public static final int ACTUAL_HUMAN_PERFORMERS_SEQUENCE = 0x00404035;
        
    /** (0040,4036) VR=LO, VM=1 Human Performer's Organization  */
    public static final int HUMAN_PERFORMERS_ORGANIZATION = 0x00404036;
        
    /** (0040,4037) VR=PN, VM=1 Human Performer's Name  */
    public static final int HUMAN_PERFORMERS_NAME = 0x00404037;
        
    /** (0040,8302) VR=DS, VM=1 Entrance Dose in mGy  */
    public static final int ENTRANCE_DOSE_IN_MGY = 0x00408302;
        
    /** (0040,9094) VR=SQ, VM=1 Referenced Image Real World Value Mapping Sequence  */
    public static final int REFERENCED_IMAGE_REAL_WORLD_VALUE_MAPPING_SEQUENCE = 0x00409094;
        
    /** (0040,9096) VR=SQ, VM=1 Real World Value Mapping Sequence  */
    public static final int REAL_WORLD_VALUE_MAPPING_SEQUENCE = 0x00409096;
        
    /** (0040,9098) VR=SQ, VM=1 Pixel Value Mapping Code Sequence  */
    public static final int PIXEL_VALUE_MAPPING_CODE_SEQUENCE = 0x00409098;
        
    /** (0040,9210) VR=SH, VM=1 LUT Label  */
    public static final int LUT_LABEL = 0x00409210;
        
    /** (0040,9211) VR=US|SS, VM=1 Real World Value Last Value Mapped  */
    public static final int REAL_WORLD_VALUE_LAST_VALUE_MAPPED = 0x00409211;
        
    /** (0040,9212) VR=FD, VM=1-n Real World Value LUT Data  */
    public static final int REAL_WORLD_VALUE_LUT_DATA = 0x00409212;
        
    /** (0040,9216) VR=US|SS, VM=1 Real World Value First Value Mapped  */
    public static final int REAL_WORLD_VALUE_FIRST_VALUE_MAPPED = 0x00409216;
        
    /** (0040,9224) VR=FD, VM=1 Real World Value Intercept  */
    public static final int REAL_WORLD_VALUE_INTERCEPT = 0x00409224;
        
    /** (0040,9225) VR=FD, VM=1 Real World Value Slope  */
    public static final int REAL_WORLD_VALUE_SLOPE = 0x00409225;
        
    /** (0040,A010) VR=CS, VM=1 Relationship Type  */
    public static final int RELATIONSHIP_TYPE = 0x0040A010;
        
    /** (0040,A027) VR=LO, VM=1 Verifying Organization  */
    public static final int VERIFYING_ORGANIZATION = 0x0040A027;
        
    /** (0040,A030) VR=DT, VM=1 Verification Date Time  */
    public static final int VERIFICATION_DATE_TIME = 0x0040A030;
        
    /** (0040,A032) VR=DT, VM=1 Observation Date Time  */
    public static final int OBSERVATION_DATE_TIME = 0x0040A032;
        
    /** (0040,A040) VR=CS, VM=1 Value Type  */
    public static final int VALUE_TYPE = 0x0040A040;
        
    /** (0040,A043) VR=SQ, VM=1 Concept Name Code Sequence  */
    public static final int CONCEPT_NAME_CODE_SEQUENCE = 0x0040A043;
        
    /** (0040,A050) VR=CS, VM=1 Continuity Of Content  */
    public static final int CONTINUITY_OF_CONTENT = 0x0040A050;
        
    /** (0040,A073) VR=SQ, VM=1 Verifying Observer Sequence  */
    public static final int VERIFYING_OBSERVER_SEQUENCE = 0x0040A073;
        
    /** (0040,A075) VR=PN, VM=1 Verifying Observer Name  */
    public static final int VERIFYING_OBSERVER_NAME = 0x0040A075;
        
    /** (0040,A078) VR=SQ, VM=1 Author Observer Sequence  */
    public static final int AUTHOR_OBSERVER_SEQUENCE = 0x0040A078;
        
    /** (0040,A07A) VR=SQ, VM=1 Participant Sequence  */
    public static final int PARTICIPANT_SEQUENCE = 0x0040A07A;
        
    /** (0040,A07C) VR=SQ, VM=1 Custodial Organization Sequence  */
    public static final int CUSTODIAL_ORGANIZATION_SEQUENCE = 0x0040A07C;
        
    /** (0040,A080) VR=CS, VM=1 Participation Type  */
    public static final int PARTICIPATION_TYPE = 0x0040A080;
        
    /** (0040,A082) VR=DT, VM=1 Participation Datetime  */
    public static final int PARTICIPATION_DATETIME = 0x0040A082;
        
    /** (0040,A084) VR=CS, VM=1 Observer Type  */
    public static final int OBSERVER_TYPE = 0x0040A084;
        
    /** (0040,A088) VR=SQ, VM=1 Verifying Observer Identification Code Sequence  */
    public static final int VERIFYING_OBSERVER_IDENTIFICATION_CODE_SEQUENCE = 0x0040A088;
        
    /** (0040,A090) VR=SQ, VM=1 Equivalent CDA Document Sequence  */
    public static final int EQUIVALENT_CDA_DOCUMENT_SEQUENCE = 0x0040A090;
        
    /** (0040,A0B0) VR=US, VM=2-2n Referenced Waveform Channels  */
    public static final int REFERENCED_WAVEFORM_CHANNELS = 0x0040A0B0;
        
    /** (0040,A120) VR=DT, VM=1 DateTime  */
    public static final int DATETIME = 0x0040A120;
        
    /** (0040,A121) VR=DA, VM=1 Date  */
    public static final int DATE = 0x0040A121;
        
    /** (0040,A122) VR=TM, VM=1 Time  */
    public static final int TIME = 0x0040A122;
        
    /** (0040,A123) VR=PN, VM=1 Person Name  */
    public static final int PERSON_NAME = 0x0040A123;
        
    /** (0040,A124) VR=UI, VM=1 UID  */
    public static final int UID = 0x0040A124;
        
    /** (0040,A130) VR=CS, VM=1 Temporal Range Type  */
    public static final int TEMPORAL_RANGE_TYPE = 0x0040A130;
        
    /** (0040,A132) VR=UL, VM=1-n Referenced Sample Positions  */
    public static final int REFERENCED_SAMPLE_POSITIONS = 0x0040A132;
        
    /** (0040,A136) VR=US, VM=1-n Referenced Frame Numbers  */
    public static final int REFERENCED_FRAME_NUMBERS = 0x0040A136;
        
    /** (0040,A138) VR=DS, VM=1-n Referenced Time Offsets  */
    public static final int REFERENCED_TIME_OFFSETS = 0x0040A138;
        
    /** (0040,A13A) VR=DT, VM=1-n Referenced Datetime  */
    public static final int REFERENCED_DATETIME = 0x0040A13A;
        
    /** (0040,A160) VR=UT, VM=1 Text Value  */
    public static final int TEXT_VALUE = 0x0040A160;
        
    /** (0040,A168) VR=SQ, VM=1 Concept Code Sequence  */
    public static final int CONCEPT_CODE_SEQUENCE = 0x0040A168;
        
    /** (0040,A170) VR=SQ, VM=1 Purpose of Reference Code Sequence  */
    public static final int PURPOSE_OF_REFERENCE_CODE_SEQUENCE = 0x0040A170;
        
    /** (0040,A180) VR=US, VM=1 Annotation Group Number  */
    public static final int ANNOTATION_GROUP_NUMBER = 0x0040A180;
        
    /** (0040,A195) VR=SQ, VM=1 Modifier Code Sequence  */
    public static final int MODIFIER_CODE_SEQUENCE = 0x0040A195;
        
    /** (0040,A300) VR=SQ, VM=1 Measured Value Sequence  */
    public static final int MEASURED_VALUE_SEQUENCE = 0x0040A300;
        
    /** (0040,A301) VR=SQ, VM=1 Numeric Value Qualifier Code Sequence  */
    public static final int NUMERIC_VALUE_QUALIFIER_CODE_SEQUENCE = 0x0040A301;
        
    /** (0040,A30A) VR=DS, VM=1-n Numeric Value  */
    public static final int NUMERIC_VALUE = 0x0040A30A;
        
    /** (0040,A360) VR=SQ, VM=1 Predecessor Documents Sequence  */
    public static final int PREDECESSOR_DOCUMENTS_SEQUENCE = 0x0040A360;
        
    /** (0040,A370) VR=SQ, VM=1 Referenced Request Sequence  */
    public static final int REFERENCED_REQUEST_SEQUENCE = 0x0040A370;
        
    /** (0040,A372) VR=SQ, VM=1 Performed Procedure Code Sequence  */
    public static final int PERFORMED_PROCEDURE_CODE_SEQUENCE = 0x0040A372;
        
    /** (0040,A375) VR=SQ, VM=1 Current Requested Procedure Evidence Sequence  */
    public static final int CURRENT_REQUESTED_PROCEDURE_EVIDENCE_SEQUENCE = 0x0040A375;
        
    /** (0040,A385) VR=SQ, VM=1 Pertinent Other Evidence Sequence  */
    public static final int PERTINENT_OTHER_EVIDENCE_SEQUENCE = 0x0040A385;
        
    /** (0040,A390) VR=SQ, VM=1 HL7 Structured Document Reference Sequence  */
    public static final int HL7_STRUCTURED_DOCUMENT_REFERENCE_SEQUENCE = 0x0040A390;
        
    /** (0040,A491) VR=CS, VM=1 Completion Flag  */
    public static final int COMPLETION_FLAG = 0x0040A491;
        
    /** (0040,A492) VR=LO, VM=1 Completion Flag Description  */
    public static final int COMPLETION_FLAG_DESCRIPTION = 0x0040A492;
        
    /** (0040,A493) VR=CS, VM=1 Verification Flag  */
    public static final int VERIFICATION_FLAG = 0x0040A493;
        
    /** (0040,A504) VR=SQ, VM=1 Content Template Sequence  */
    public static final int CONTENT_TEMPLATE_SEQUENCE = 0x0040A504;
        
    /** (0040,A525) VR=SQ, VM=1 Identical Documents Sequence  */
    public static final int IDENTICAL_DOCUMENTS_SEQUENCE = 0x0040A525;
        
    /** (0040,A730) VR=SQ, VM=1 Content Sequence  */
    public static final int CONTENT_SEQUENCE = 0x0040A730;
        
    /** (0040,B020) VR=SQ, VM=1 Annotation Sequence  */
    public static final int ANNOTATION_SEQUENCE = 0x0040B020;
        
    /** (0040,DB00) VR=CS, VM=1 Template Identifier  */
    public static final int TEMPLATE_IDENTIFIER = 0x0040DB00;
        
    /** (0040,DB06) VR=DT, VM=1 Template Version RET */
    public static final int TEMPLATE_VERSION_RET = 0x0040DB06;
        
    /** (0040,DB07) VR=DT, VM=1 Template Local Version RET */
    public static final int TEMPLATE_LOCAL_VERSION_RET = 0x0040DB07;
        
    /** (0040,DB0B) VR=CS, VM=1 Template Extension Flag RET */
    public static final int TEMPLATE_EXTENSION_FLAG_RET = 0x0040DB0B;
        
    /** (0040,DB0C) VR=UI, VM=1 Template Extension Organization UID RET */
    public static final int TEMPLATE_EXTENSION_ORGANIZATION_UID_RET = 0x0040DB0C;
        
    /** (0040,DB0D) VR=UI, VM=1 Template Extension Creator UID RET */
    public static final int TEMPLATE_EXTENSION_CREATOR_UID_RET = 0x0040DB0D;
        
    /** (0040,DB73) VR=UL, VM=1-n Referenced Content Item Identifier  */
    public static final int REFERENCED_CONTENT_ITEM_IDENTIFIER = 0x0040DB73;
        
    /** (0040,E001) VR=ST, VM=1 HL7 Instance Identifier  */
    public static final int HL7_INSTANCE_IDENTIFIER = 0x0040E001;
        
    /** (0040,E004) VR=DT, VM=1 HL7 Document Effective Time  */
    public static final int HL7_DOCUMENT_EFFECTIVE_TIME = 0x0040E004;
        
    /** (0040,E006) VR=SQ, VM=1 HL7 Document Type Code Sequence  */
    public static final int HL7_DOCUMENT_TYPE_CODE_SEQUENCE = 0x0040E006;
        
    /** (0040,E010) VR=UT, VM=1 Retrieve URI  */
    public static final int RETRIEVE_URI = 0x0040E010;
        
    /** (0042,0010) VR=ST, VM=1 Document Title  */
    public static final int DOCUMENT_TITLE = 0x00420010;
        
    /** (0042,0011) VR=OB, VM=1 Encapsulated Document  */
    public static final int ENCAPSULATED_DOCUMENT = 0x00420011;
        
    /** (0042,0012) VR=LO, VM=1 MIME Type of Encapsulated Document  */
    public static final int MIME_TYPE_OF_ENCAPSULATED_DOCUMENT = 0x00420012;
        
    /** (0042,0013) VR=SQ, VM=1 Source Instance Sequence  */
    public static final int SOURCE_INSTANCE_SEQUENCE = 0x00420013;
        
    /** (0050,0004) VR=CS, VM=1 Calibration Image  */
    public static final int CALIBRATION_IMAGE = 0x00500004;
        
    /** (0050,0010) VR=SQ, VM=1 Device Sequence  */
    public static final int DEVICE_SEQUENCE = 0x00500010;
        
    /** (0050,0014) VR=DS, VM=1 Device Length  */
    public static final int DEVICE_LENGTH = 0x00500014;
        
    /** (0050,0016) VR=DS, VM=1 Device Diameter  */
    public static final int DEVICE_DIAMETER = 0x00500016;
        
    /** (0050,0017) VR=CS, VM=1 Device Diameter Units  */
    public static final int DEVICE_DIAMETER_UNITS = 0x00500017;
        
    /** (0050,0018) VR=DS, VM=1 Device Volume  */
    public static final int DEVICE_VOLUME = 0x00500018;
        
    /** (0050,0019) VR=DS, VM=1 Intermarker Distance  */
    public static final int INTERMARKER_DISTANCE = 0x00500019;
        
    /** (0050,0020) VR=LO, VM=1 Device Description  */
    public static final int DEVICE_DESCRIPTION = 0x00500020;
        
    /** (0054,0010) VR=US, VM=1-n Energy Window Vector  */
    public static final int ENERGY_WINDOW_VECTOR = 0x00540010;
        
    /** (0054,0011) VR=US, VM=1 Number of Energy Windows  */
    public static final int NUMBER_OF_ENERGY_WINDOWS = 0x00540011;
        
    /** (0054,0012) VR=SQ, VM=1 Energy Window Information Sequence  */
    public static final int ENERGY_WINDOW_INFORMATION_SEQUENCE = 0x00540012;
        
    /** (0054,0013) VR=SQ, VM=1 Energy Window Range Sequence  */
    public static final int ENERGY_WINDOW_RANGE_SEQUENCE = 0x00540013;
        
    /** (0054,0014) VR=DS, VM=1 Energy Window Lower Limit  */
    public static final int ENERGY_WINDOW_LOWER_LIMIT = 0x00540014;
        
    /** (0054,0015) VR=DS, VM=1 Energy Window Upper Limit  */
    public static final int ENERGY_WINDOW_UPPER_LIMIT = 0x00540015;
        
    /** (0054,0016) VR=SQ, VM=1 Radiopharmaceutical Information Sequence  */
    public static final int RADIOPHARMACEUTICAL_INFORMATION_SEQUENCE = 0x00540016;
        
    /** (0054,0017) VR=IS, VM=1 Residual Syringe Counts  */
    public static final int RESIDUAL_SYRINGE_COUNTS = 0x00540017;
        
    /** (0054,0018) VR=SH, VM=1 Energy Window Name  */
    public static final int ENERGY_WINDOW_NAME = 0x00540018;
        
    /** (0054,0020) VR=US, VM=1-n Detector Vector  */
    public static final int DETECTOR_VECTOR = 0x00540020;
        
    /** (0054,0021) VR=US, VM=1 Number of Detectors  */
    public static final int NUMBER_OF_DETECTORS = 0x00540021;
        
    /** (0054,0022) VR=SQ, VM=1 Detector Information Sequence  */
    public static final int DETECTOR_INFORMATION_SEQUENCE = 0x00540022;
        
    /** (0054,0030) VR=US, VM=1-n Phase Vector  */
    public static final int PHASE_VECTOR = 0x00540030;
        
    /** (0054,0031) VR=US, VM=1 Number of Phases  */
    public static final int NUMBER_OF_PHASES = 0x00540031;
        
    /** (0054,0032) VR=SQ, VM=1 Phase Information Sequence  */
    public static final int PHASE_INFORMATION_SEQUENCE = 0x00540032;
        
    /** (0054,0033) VR=US, VM=1 Number of Frames in Phase  */
    public static final int NUMBER_OF_FRAMES_IN_PHASE = 0x00540033;
        
    /** (0054,0036) VR=IS, VM=1 Phase Delay  */
    public static final int PHASE_DELAY = 0x00540036;
        
    /** (0054,0038) VR=IS, VM=1 Pause Between Frames  */
    public static final int PAUSE_BETWEEN_FRAMES = 0x00540038;
        
    /** (0054,0039) VR=CS, VM=1 Phase Description  */
    public static final int PHASE_DESCRIPTION = 0x00540039;
        
    /** (0054,0050) VR=US, VM=1-n Rotation Vector  */
    public static final int ROTATION_VECTOR = 0x00540050;
        
    /** (0054,0051) VR=US, VM=1 Number of Rotations  */
    public static final int NUMBER_OF_ROTATIONS = 0x00540051;
        
    /** (0054,0052) VR=SQ, VM=1 Rotation Information Sequence  */
    public static final int ROTATION_INFORMATION_SEQUENCE = 0x00540052;
        
    /** (0054,0053) VR=US, VM=1 Number of Frames in Rotation  */
    public static final int NUMBER_OF_FRAMES_IN_ROTATION = 0x00540053;
        
    /** (0054,0060) VR=US, VM=1-n R-R Interval Vector  */
    public static final int R_R_INTERVAL_VECTOR = 0x00540060;
        
    /** (0054,0061) VR=US, VM=1 Number of R-R Intervals  */
    public static final int NUMBER_OF_R_R_INTERVALS = 0x00540061;
        
    /** (0054,0062) VR=SQ, VM=1 Gated Information Sequence  */
    public static final int GATED_INFORMATION_SEQUENCE = 0x00540062;
        
    /** (0054,0063) VR=SQ, VM=1 Data Information Sequence  */
    public static final int DATA_INFORMATION_SEQUENCE = 0x00540063;
        
    /** (0054,0070) VR=US, VM=1-n Time Slot Vector  */
    public static final int TIME_SLOT_VECTOR = 0x00540070;
        
    /** (0054,0071) VR=US, VM=1 Number of Time Slots  */
    public static final int NUMBER_OF_TIME_SLOTS = 0x00540071;
        
    /** (0054,0072) VR=SQ, VM=1 Time Slot Information Sequence  */
    public static final int TIME_SLOT_INFORMATION_SEQUENCE = 0x00540072;
        
    /** (0054,0073) VR=DS, VM=1 Time Slot Time  */
    public static final int TIME_SLOT_TIME = 0x00540073;
        
    /** (0054,0080) VR=US, VM=1-n Slice Vector  */
    public static final int SLICE_VECTOR = 0x00540080;
        
    /** (0054,0081) VR=US, VM=1 Number of Slices  */
    public static final int NUMBER_OF_SLICES = 0x00540081;
        
    /** (0054,0090) VR=US, VM=1-n Angular View Vector  */
    public static final int ANGULAR_VIEW_VECTOR = 0x00540090;
        
    /** (0054,0100) VR=US, VM=1-n Time Slice Vector  */
    public static final int TIME_SLICE_VECTOR = 0x00540100;
        
    /** (0054,0101) VR=US, VM=1 Number of Time Slices  */
    public static final int NUMBER_OF_TIME_SLICES = 0x00540101;
        
    /** (0054,0200) VR=DS, VM=1 Start Angle  */
    public static final int START_ANGLE = 0x00540200;
        
    /** (0054,0202) VR=CS, VM=1 Type of Detector Motion  */
    public static final int TYPE_OF_DETECTOR_MOTION = 0x00540202;
        
    /** (0054,0210) VR=IS, VM=1-n Trigger Vector  */
    public static final int TRIGGER_VECTOR = 0x00540210;
        
    /** (0054,0211) VR=US, VM=1 Number of Triggers in Phase  */
    public static final int NUMBER_OF_TRIGGERS_IN_PHASE = 0x00540211;
        
    /** (0054,0220) VR=SQ, VM=1 View Code Sequence  */
    public static final int VIEW_CODE_SEQUENCE = 0x00540220;
        
    /** (0054,0222) VR=SQ, VM=1 View Modifier Code Sequence  */
    public static final int VIEW_MODIFIER_CODE_SEQUENCE = 0x00540222;
        
    /** (0054,0300) VR=SQ, VM=1 Radionuclide Code Sequence  */
    public static final int RADIONUCLIDE_CODE_SEQUENCE = 0x00540300;
        
    /** (0054,0302) VR=SQ, VM=1 Administration Route Code Sequence  */
    public static final int ADMINISTRATION_ROUTE_CODE_SEQUENCE = 0x00540302;
        
    /** (0054,0304) VR=SQ, VM=1 Radiopharmaceutical Code Sequence  */
    public static final int RADIOPHARMACEUTICAL_CODE_SEQUENCE = 0x00540304;
        
    /** (0054,0306) VR=SQ, VM=1 Calibration Data Sequence  */
    public static final int CALIBRATION_DATA_SEQUENCE = 0x00540306;
        
    /** (0054,0308) VR=US, VM=1 Energy Window Number  */
    public static final int ENERGY_WINDOW_NUMBER = 0x00540308;
        
    /** (0054,0400) VR=SH, VM=1 Image ID  */
    public static final int IMAGE_ID = 0x00540400;
        
    /** (0054,0410) VR=SQ, VM=1 Patient Orientation Code Sequence  */
    public static final int PATIENT_ORIENTATION_CODE_SEQUENCE = 0x00540410;
        
    /** (0054,0412) VR=SQ, VM=1 Patient Orientation Modifier Code Sequence  */
    public static final int PATIENT_ORIENTATION_MODIFIER_CODE_SEQUENCE = 0x00540412;
        
    /** (0054,0414) VR=SQ, VM=1 Patient Gantry Relationship Code Sequence  */
    public static final int PATIENT_GANTRY_RELATIONSHIP_CODE_SEQUENCE = 0x00540414;
        
    /** (0054,0500) VR=CS, VM=1 Slice Progression Direction  */
    public static final int SLICE_PROGRESSION_DIRECTION = 0x00540500;
        
    /** (0054,1000) VR=CS, VM=2 Series Type  */
    public static final int SERIES_TYPE = 0x00541000;
        
    /** (0054,1001) VR=CS, VM=1 Units  */
    public static final int UNITS = 0x00541001;
        
    /** (0054,1002) VR=CS, VM=1 Counts Source  */
    public static final int COUNTS_SOURCE = 0x00541002;
        
    /** (0054,1004) VR=CS, VM=1 Reprojection Method  */
    public static final int REPROJECTION_METHOD = 0x00541004;
        
    /** (0054,1100) VR=CS, VM=1 Randoms Correction Method  */
    public static final int RANDOMS_CORRECTION_METHOD = 0x00541100;
        
    /** (0054,1101) VR=LO, VM=1 Attenuation Correction Method  */
    public static final int ATTENUATION_CORRECTION_METHOD = 0x00541101;
        
    /** (0054,1102) VR=CS, VM=1 Decay Correction  */
    public static final int DECAY_CORRECTION = 0x00541102;
        
    /** (0054,1103) VR=LO, VM=1 Reconstruction Method  */
    public static final int RECONSTRUCTION_METHOD = 0x00541103;
        
    /** (0054,1104) VR=LO, VM=1 Detector Lines of Response Used  */
    public static final int DETECTOR_LINES_OF_RESPONSE_USED = 0x00541104;
        
    /** (0054,1105) VR=LO, VM=1 Scatter Correction Method  */
    public static final int SCATTER_CORRECTION_METHOD = 0x00541105;
        
    /** (0054,1200) VR=DS, VM=1 Axial Acceptance  */
    public static final int AXIAL_ACCEPTANCE = 0x00541200;
        
    /** (0054,1201) VR=IS, VM=2 Axial Mash  */
    public static final int AXIAL_MASH = 0x00541201;
        
    /** (0054,1202) VR=IS, VM=1 Transverse Mash  */
    public static final int TRANSVERSE_MASH = 0x00541202;
        
    /** (0054,1203) VR=DS, VM=2 Detector Element Size  */
    public static final int DETECTOR_ELEMENT_SIZE = 0x00541203;
        
    /** (0054,1210) VR=DS, VM=1 Coincidence Window Width  */
    public static final int COINCIDENCE_WINDOW_WIDTH = 0x00541210;
        
    /** (0054,1220) VR=CS, VM=1-n Secondary Counts Type  */
    public static final int SECONDARY_COUNTS_TYPE = 0x00541220;
        
    /** (0054,1300) VR=DS, VM=1 Frame Reference Time  */
    public static final int FRAME_REFERENCE_TIME = 0x00541300;
        
    /** (0054,1310) VR=IS, VM=1 Primary (Prompts) Counts Accumulated  */
    public static final int PRIMARY_PROMPTS_COUNTS_ACCUMULATED = 0x00541310;
        
    /** (0054,1311) VR=IS, VM=1-n Secondary Counts Accumulated  */
    public static final int SECONDARY_COUNTS_ACCUMULATED = 0x00541311;
        
    /** (0054,1320) VR=DS, VM=1 Slice Sensitivity Factor  */
    public static final int SLICE_SENSITIVITY_FACTOR = 0x00541320;
        
    /** (0054,1321) VR=DS, VM=1 Decay Factor  */
    public static final int DECAY_FACTOR = 0x00541321;
        
    /** (0054,1322) VR=DS, VM=1 Dose Calibration Factor  */
    public static final int DOSE_CALIBRATION_FACTOR = 0x00541322;
        
    /** (0054,1323) VR=DS, VM=1 Scatter Fraction Factor  */
    public static final int SCATTER_FRACTION_FACTOR = 0x00541323;
        
    /** (0054,1324) VR=DS, VM=1 Dead Time Factor  */
    public static final int DEAD_TIME_FACTOR = 0x00541324;
        
    /** (0054,1330) VR=US, VM=1 Image Index  */
    public static final int IMAGE_INDEX = 0x00541330;
        
    /** (0054,1400) VR=CS, VM=1-n Counts Included  */
    public static final int COUNTS_INCLUDED = 0x00541400;
        
    /** (0054,1401) VR=CS, VM=1 Dead Time Correction Flag  */
    public static final int DEAD_TIME_CORRECTION_FLAG = 0x00541401;
        
    /** (0060,3000) VR=SQ, VM=1 Histogram Sequence  */
    public static final int HISTOGRAM_SEQUENCE = 0x00603000;
        
    /** (0060,3002) VR=US, VM=1 Histogram Number of Bins  */
    public static final int HISTOGRAM_NUMBER_OF_BINS = 0x00603002;
        
    /** (0060,3004) VR=US|SS, VM=1 Histogram First Bin Value  */
    public static final int HISTOGRAM_FIRST_BIN_VALUE = 0x00603004;
        
    /** (0060,3006) VR=US|SS, VM=1 Histogram Last Bin Value  */
    public static final int HISTOGRAM_LAST_BIN_VALUE = 0x00603006;
        
    /** (0060,3008) VR=US, VM=1 Histogram Bin Width  */
    public static final int HISTOGRAM_BIN_WIDTH = 0x00603008;
        
    /** (0060,3010) VR=LO, VM=1 Histogram Explanation  */
    public static final int HISTOGRAM_EXPLANATION = 0x00603010;
        
    /** (0060,3020) VR=UL, VM=1-n Histogram Data  */
    public static final int HISTOGRAM_DATA = 0x00603020;
        
    /** (0062,0001) VR=CS, VM=1 Segmentation Type  */
    public static final int SEGMENTATION_TYPE = 0x00620001;
        
    /** (0062,0002) VR=SQ, VM=1 Segment Sequence  */
    public static final int SEGMENT_SEQUENCE = 0x00620002;
        
    /** (0062,0003) VR=SQ, VM=1 Segmented Property Category Code Sequence  */
    public static final int SEGMENTED_PROPERTY_CATEGORY_CODE_SEQUENCE = 0x00620003;
        
    /** (0062,0004) VR=US, VM=1 Segment Number  */
    public static final int SEGMENT_NUMBER = 0x00620004;
        
    /** (0062,0005) VR=LO, VM=1 Segment Label  */
    public static final int SEGMENT_LABEL = 0x00620005;
        
    /** (0062,0006) VR=ST, VM=1 Segment Description  */
    public static final int SEGMENT_DESCRIPTION = 0x00620006;
        
    /** (0062,0008) VR=CS, VM=1 Segment Algorithm Type  */
    public static final int SEGMENT_ALGORITHM_TYPE = 0x00620008;
        
    /** (0062,0009) VR=LO, VM=1 Segment Algorithm Name  */
    public static final int SEGMENT_ALGORITHM_NAME = 0x00620009;
        
    /** (0062,000A) VR=SQ, VM=1 Segment Identification Sequence  */
    public static final int SEGMENT_IDENTIFICATION_SEQUENCE = 0x0062000A;
        
    /** (0062,000B) VR=US, VM=1-n Referenced Segment Number  */
    public static final int REFERENCED_SEGMENT_NUMBER = 0x0062000B;
        
    /** (0062,000C) VR=US, VM=1 Recommended Display Grayscale Value  */
    public static final int RECOMMENDED_DISPLAY_GRAYSCALE_VALUE = 0x0062000C;
        
    /** (0062,000D) VR=US, VM=3 Recommended Display CIELab Value  */
    public static final int RECOMMENDED_DISPLAY_CIELAB_VALUE = 0x0062000D;
        
    /** (0062,000E) VR=US, VM=1 Maximum Fractional Value  */
    public static final int MAXIMUM_FRACTIONAL_VALUE = 0x0062000E;
        
    /** (0062,000F) VR=SQ, VM=1 Segmented Property Type Code Sequence  */
    public static final int SEGMENTED_PROPERTY_TYPE_CODE_SEQUENCE = 0x0062000F;
        
    /** (0062,0010) VR=CS, VM=1 Segmentation Fractional Type  */
    public static final int SEGMENTATION_FRACTIONAL_TYPE = 0x00620010;
        
    /** (0064,0002) VR=SQ, VM=1 Deformable Registration Sequence  */
    public static final int DEFORMABLE_REGISTRATION_SEQUENCE = 0x00640002;
        
    /** (0064,0003) VR=UI, VM=1 Source Frame of Reference UID  */
    public static final int SOURCE_FRAME_OF_REFERENCE_UID = 0x00640003;
        
    /** (0064,0005) VR=SQ, VM=1 Deformable Registration Grid Sequence  */
    public static final int DEFORMABLE_REGISTRATION_GRID_SEQUENCE = 0x00640005;
        
    /** (0064,0007) VR=UL, VM=3 Grid Dimensions  */
    public static final int GRID_DIMENSIONS = 0x00640007;
        
    /** (0064,0008) VR=FD, VM=3 Grid Resolution  */
    public static final int GRID_RESOLUTION = 0x00640008;
        
    /** (0064,0009) VR=OF, VM=1 Vector Grid Data  */
    public static final int VECTOR_GRID_DATA = 0x00640009;
        
    /** (0064,000F) VR=SQ, VM=1 Pre Deformation Matrix Registration Sequence  */
    public static final int PRE_DEFORMATION_MATRIX_REGISTRATION_SEQUENCE = 0x0064000F;
        
    /** (0064,0010) VR=SQ, VM=1 Post Deformation Matrix Registration Sequence  */
    public static final int POST_DEFORMATION_MATRIX_REGISTRATION_SEQUENCE = 0x00640010;
        
    /** (0070,0001) VR=SQ, VM=1 Graphic Annotation Sequence  */
    public static final int GRAPHIC_ANNOTATION_SEQUENCE = 0x00700001;
        
    /** (0070,0002) VR=CS, VM=1 Graphic Layer  */
    public static final int GRAPHIC_LAYER = 0x00700002;
        
    /** (0070,0003) VR=CS, VM=1 Bounding Box Annotation Units  */
    public static final int BOUNDING_BOX_ANNOTATION_UNITS = 0x00700003;
        
    /** (0070,0004) VR=CS, VM=1 Anchor Point Annotation Units  */
    public static final int ANCHOR_POINT_ANNOTATION_UNITS = 0x00700004;
        
    /** (0070,0005) VR=CS, VM=1 Graphic Annotation Units  */
    public static final int GRAPHIC_ANNOTATION_UNITS = 0x00700005;
        
    /** (0070,0006) VR=ST, VM=1 Unformatted Text Value  */
    public static final int UNFORMATTED_TEXT_VALUE = 0x00700006;
        
    /** (0070,0008) VR=SQ, VM=1 Text Object Sequence  */
    public static final int TEXT_OBJECT_SEQUENCE = 0x00700008;
        
    /** (0070,0009) VR=SQ, VM=1 Graphic Object Sequence  */
    public static final int GRAPHIC_OBJECT_SEQUENCE = 0x00700009;
        
    /** (0070,0010) VR=FL, VM=2 Bounding Box Top Left Hand Corner  */
    public static final int BOUNDING_BOX_TOP_LEFT_HAND_CORNER = 0x00700010;
        
    /** (0070,0011) VR=FL, VM=2 Bounding Box Bottom Right Hand Corner  */
    public static final int BOUNDING_BOX_BOTTOM_RIGHT_HAND_CORNER = 0x00700011;
        
    /** (0070,0012) VR=CS, VM=1 Bounding Box Text Horizontal Justification  */
    public static final int BOUNDING_BOX_TEXT_HORIZONTAL_JUSTIFICATION = 0x00700012;
        
    /** (0070,0014) VR=FL, VM=2 Anchor Point  */
    public static final int ANCHOR_POINT = 0x00700014;
        
    /** (0070,0015) VR=CS, VM=1 Anchor Point Visibility  */
    public static final int ANCHOR_POINT_VISIBILITY = 0x00700015;
        
    /** (0070,0020) VR=US, VM=1 Graphic Dimensions  */
    public static final int GRAPHIC_DIMENSIONS = 0x00700020;
        
    /** (0070,0021) VR=US, VM=1 Number of Graphic Points  */
    public static final int NUMBER_OF_GRAPHIC_POINTS = 0x00700021;
        
    /** (0070,0022) VR=FL, VM=2-n Graphic Data  */
    public static final int GRAPHIC_DATA = 0x00700022;
        
    /** (0070,0023) VR=CS, VM=1 Graphic Type  */
    public static final int GRAPHIC_TYPE = 0x00700023;
        
    /** (0070,0024) VR=CS, VM=1 Graphic Filled  */
    public static final int GRAPHIC_FILLED = 0x00700024;
        
    /** (0070,0041) VR=CS, VM=1 Image Horizontal Flip  */
    public static final int IMAGE_HORIZONTAL_FLIP = 0x00700041;
        
    /** (0070,0042) VR=US, VM=1 Image Rotation  */
    public static final int IMAGE_ROTATION = 0x00700042;
        
    /** (0070,0052) VR=SL, VM=2 Displayed Area Top Left Hand Corner  */
    public static final int DISPLAYED_AREA_TOP_LEFT_HAND_CORNER = 0x00700052;
        
    /** (0070,0053) VR=SL, VM=2 Displayed Area Bottom Right Hand Corner  */
    public static final int DISPLAYED_AREA_BOTTOM_RIGHT_HAND_CORNER = 0x00700053;
        
    /** (0070,005A) VR=SQ, VM=1 Displayed Area Selection Sequence  */
    public static final int DISPLAYED_AREA_SELECTION_SEQUENCE = 0x0070005A;
        
    /** (0070,0060) VR=SQ, VM=1 Graphic Layer Sequence  */
    public static final int GRAPHIC_LAYER_SEQUENCE = 0x00700060;
        
    /** (0070,0062) VR=IS, VM=1 Graphic Layer Order  */
    public static final int GRAPHIC_LAYER_ORDER = 0x00700062;
        
    /** (0070,0066) VR=US, VM=1 Graphic Layer Recommended Display Grayscale Value  */
    public static final int GRAPHIC_LAYER_RECOMMENDED_DISPLAY_GRAYSCALE_VALUE = 0x00700066;
        
    /** (0070,0067) VR=US, VM=3 Graphic Layer Recommended Display RGB Value RET */
    public static final int GRAPHIC_LAYER_RECOMMENDED_DISPLAY_RGB_VALUE_RET = 0x00700067;
        
    /** (0070,0068) VR=LO, VM=1 Graphic Layer Description  */
    public static final int GRAPHIC_LAYER_DESCRIPTION = 0x00700068;
        
    /** (0070,0080) VR=CS, VM=1 Content Label  */
    public static final int CONTENT_LABEL = 0x00700080;
        
    /** (0070,0081) VR=LO, VM=1 Content Description  */
    public static final int CONTENT_DESCRIPTION = 0x00700081;
        
    /** (0070,0082) VR=DA, VM=1 Presentation Creation Date  */
    public static final int PRESENTATION_CREATION_DATE = 0x00700082;
        
    /** (0070,0083) VR=TM, VM=1 Presentation Creation Time  */
    public static final int PRESENTATION_CREATION_TIME = 0x00700083;
        
    /** (0070,0084) VR=PN, VM=1 Content Creator's Name  */
    public static final int CONTENT_CREATORS_NAME = 0x00700084;
        
    /** (0070,0086) VR=SQ, VM=1 Content Creator's Identification Code Sequence  */
    public static final int CONTENT_CREATORS_IDENTIFICATION_CODE_SEQUENCE = 0x00700086;
        
    /** (0070,0100) VR=CS, VM=1 Presentation Size Mode  */
    public static final int PRESENTATION_SIZE_MODE = 0x00700100;
        
    /** (0070,0101) VR=DS, VM=2 Presentation Pixel Spacing  */
    public static final int PRESENTATION_PIXEL_SPACING = 0x00700101;
        
    /** (0070,0102) VR=IS, VM=2 Presentation Pixel Aspect Ratio  */
    public static final int PRESENTATION_PIXEL_ASPECT_RATIO = 0x00700102;
        
    /** (0070,0103) VR=FL, VM=1 Presentation Pixel Magnification Ratio  */
    public static final int PRESENTATION_PIXEL_MAGNIFICATION_RATIO = 0x00700103;
        
    /** (0070,0306) VR=CS, VM=1 Shape Type  */
    public static final int SHAPE_TYPE = 0x00700306;
        
    /** (0070,0308) VR=SQ, VM=1 Registration Sequence  */
    public static final int REGISTRATION_SEQUENCE = 0x00700308;
        
    /** (0070,0309) VR=SQ, VM=1 Matrix Registration Sequence  */
    public static final int MATRIX_REGISTRATION_SEQUENCE = 0x00700309;
        
    /** (0070,030A) VR=SQ, VM=1 Matrix Sequence  */
    public static final int MATRIX_SEQUENCE = 0x0070030A;
        
    /** (0070,030C) VR=CS, VM=1 Frame of Reference Transformation Matrix Type  */
    public static final int FRAME_OF_REFERENCE_TRANSFORMATION_MATRIX_TYPE = 0x0070030C;
        
    /** (0070,030D) VR=SQ, VM=1 Registration Type Code Sequence  */
    public static final int REGISTRATION_TYPE_CODE_SEQUENCE = 0x0070030D;
        
    /** (0070,030F) VR=ST, VM=1 Fiducial Description  */
    public static final int FIDUCIAL_DESCRIPTION = 0x0070030F;
        
    /** (0070,0310) VR=SH, VM=1 Fiducial Identifier  */
    public static final int FIDUCIAL_IDENTIFIER = 0x00700310;
        
    /** (0070,0311) VR=SQ, VM=1 Fiducial Identifier Code Sequence  */
    public static final int FIDUCIAL_IDENTIFIER_CODE_SEQUENCE = 0x00700311;
        
    /** (0070,0312) VR=FD, VM=1 Contour Uncertainty Radius  */
    public static final int CONTOUR_UNCERTAINTY_RADIUS = 0x00700312;
        
    /** (0070,0314) VR=SQ, VM=1 Used Fiducials Sequence  */
    public static final int USED_FIDUCIALS_SEQUENCE = 0x00700314;
        
    /** (0070,0318) VR=SQ, VM=1 Graphic Coordinates Data Sequence  */
    public static final int GRAPHIC_COORDINATES_DATA_SEQUENCE = 0x00700318;
        
    /** (0070,031A) VR=UI, VM=1 Fiducial UID  */
    public static final int FIDUCIAL_UID = 0x0070031A;
        
    /** (0070,031C) VR=SQ, VM=1 Fiducial Set Sequence  */
    public static final int FIDUCIAL_SET_SEQUENCE = 0x0070031C;
        
    /** (0070,031E) VR=SQ, VM=1 Fiducial Sequence  */
    public static final int FIDUCIAL_SEQUENCE = 0x0070031E;
        
    /** (0070,0401) VR=US, VM=3 Graphic Layer Recommended Display CIELab Value  */
    public static final int GRAPHIC_LAYER_RECOMMENDED_DISPLAY_CIELAB_VALUE = 0x00700401;
        
    /** (0070,0402) VR=SQ, VM=1 Blending Sequence  */
    public static final int BLENDING_SEQUENCE = 0x00700402;
        
    /** (0070,0403) VR=FL, VM=1 Relative Opacity  */
    public static final int RELATIVE_OPACITY = 0x00700403;
        
    /** (0070,0404) VR=SQ, VM=1 Referenced Spatial Registration Sequence  */
    public static final int REFERENCED_SPATIAL_REGISTRATION_SEQUENCE = 0x00700404;
        
    /** (0070,0405) VR=CS, VM=1 Blending Position  */
    public static final int BLENDING_POSITION = 0x00700405;
        
    /** (0072,0002) VR=SH, VM=1 Hanging Protocol Name  */
    public static final int HANGING_PROTOCOL_NAME = 0x00720002;
        
    /** (0072,0004) VR=LO, VM=1 Hanging Protocol Description  */
    public static final int HANGING_PROTOCOL_DESCRIPTION = 0x00720004;
        
    /** (0072,0006) VR=CS, VM=1 Hanging Protocol Level  */
    public static final int HANGING_PROTOCOL_LEVEL = 0x00720006;
        
    /** (0072,0008) VR=LO, VM=1 Hanging Protocol Creator  */
    public static final int HANGING_PROTOCOL_CREATOR = 0x00720008;
        
    /** (0072,000A) VR=DT, VM=1 Hanging Protocol Creation Datetime  */
    public static final int HANGING_PROTOCOL_CREATION_DATETIME = 0x0072000A;
        
    /** (0072,000C) VR=SQ, VM=1 Hanging Protocol Definition Sequence  */
    public static final int HANGING_PROTOCOL_DEFINITION_SEQUENCE = 0x0072000C;
        
    /** (0072,000E) VR=SQ, VM=1 Hanging Protocol User Identification Code Sequence  */
    public static final int HANGING_PROTOCOL_USER_IDENTIFICATION_CODE_SEQUENCE = 0x0072000E;
        
    /** (0072,0010) VR=LO, VM=1 Hanging Protocol User Group Name  */
    public static final int HANGING_PROTOCOL_USER_GROUP_NAME = 0x00720010;
        
    /** (0072,0012) VR=SQ, VM=1 Source Hanging Protocol Sequence  */
    public static final int SOURCE_HANGING_PROTOCOL_SEQUENCE = 0x00720012;
        
    /** (0072,0014) VR=US, VM=1 Number of Priors Referenced  */
    public static final int NUMBER_OF_PRIORS_REFERENCED = 0x00720014;
        
    /** (0072,0020) VR=SQ, VM=1 Image Sets Sequence  */
    public static final int IMAGE_SETS_SEQUENCE = 0x00720020;
        
    /** (0072,0022) VR=SQ, VM=1 Image Set Selector Sequence  */
    public static final int IMAGE_SET_SELECTOR_SEQUENCE = 0x00720022;
        
    /** (0072,0024) VR=CS, VM=1 Image Set Selector Usage Flag  */
    public static final int IMAGE_SET_SELECTOR_USAGE_FLAG = 0x00720024;
        
    /** (0072,0026) VR=AT, VM=1 Selector Attribute  */
    public static final int SELECTOR_ATTRIBUTE = 0x00720026;
        
    /** (0072,0028) VR=US, VM=1 Selector Value Number  */
    public static final int SELECTOR_VALUE_NUMBER = 0x00720028;
        
    /** (0072,0030) VR=SQ, VM=1 Time Based Image Sets Sequence  */
    public static final int TIME_BASED_IMAGE_SETS_SEQUENCE = 0x00720030;
        
    /** (0072,0032) VR=US, VM=1 Image Set Number  */
    public static final int IMAGE_SET_NUMBER = 0x00720032;
        
    /** (0072,0034) VR=CS, VM=1 Image Set Selector Category  */
    public static final int IMAGE_SET_SELECTOR_CATEGORY = 0x00720034;
        
    /** (0072,0038) VR=US, VM=2 Relative Time  */
    public static final int RELATIVE_TIME = 0x00720038;
        
    /** (0072,003A) VR=CS, VM=1 Relative Time Units  */
    public static final int RELATIVE_TIME_UNITS = 0x0072003A;
        
    /** (0072,003C) VR=SS, VM=2 Abstract Prior Value  */
    public static final int ABSTRACT_PRIOR_VALUE = 0x0072003C;
        
    /** (0072,003E) VR=SQ, VM=1 Abstract Prior Code Sequence  */
    public static final int ABSTRACT_PRIOR_CODE_SEQUENCE = 0x0072003E;
        
    /** (0072,0040) VR=LO, VM=1 Image Set Label  */
    public static final int IMAGE_SET_LABEL = 0x00720040;
        
    /** (0072,0050) VR=CS, VM=1 Selector Attribute VR  */
    public static final int SELECTOR_ATTRIBUTE_VR = 0x00720050;
        
    /** (0072,0052) VR=AT, VM=1 Selector Sequence Pointer  */
    public static final int SELECTOR_SEQUENCE_POINTER = 0x00720052;
        
    /** (0072,0054) VR=LO, VM=1 Selector Sequence Pointer Private Creator  */
    public static final int SELECTOR_SEQUENCE_POINTER_PRIVATE_CREATOR = 0x00720054;
        
    /** (0072,0056) VR=LO, VM=1 Selector Attribute Private Creator  */
    public static final int SELECTOR_ATTRIBUTE_PRIVATE_CREATOR = 0x00720056;
        
    /** (0072,0060) VR=AT, VM=1-n Selector AT Value  */
    public static final int SELECTOR_AT_VALUE = 0x00720060;
        
    /** (0072,0062) VR=CS, VM=1-n Selector CS Value  */
    public static final int SELECTOR_CS_VALUE = 0x00720062;
        
    /** (0072,0064) VR=IS, VM=1-n Selector IS Value  */
    public static final int SELECTOR_IS_VALUE = 0x00720064;
        
    /** (0072,0066) VR=LO, VM=1-n Selector LO Value  */
    public static final int SELECTOR_LO_VALUE = 0x00720066;
        
    /** (0072,0068) VR=LT, VM=1-n Selector LT Value  */
    public static final int SELECTOR_LT_VALUE = 0x00720068;
        
    /** (0072,006A) VR=PN, VM=1-n Selector PN Value  */
    public static final int SELECTOR_PN_VALUE = 0x0072006A;
        
    /** (0072,006C) VR=SH, VM=1-n Selector SH Value  */
    public static final int SELECTOR_SH_VALUE = 0x0072006C;
        
    /** (0072,006E) VR=ST, VM=1-n Selector ST Value  */
    public static final int SELECTOR_ST_VALUE = 0x0072006E;
        
    /** (0072,0070) VR=UT, VM=1-n Selector UT Value  */
    public static final int SELECTOR_UT_VALUE = 0x00720070;
        
    /** (0072,0072) VR=DS, VM=1-n Selector DS Value  */
    public static final int SELECTOR_DS_VALUE = 0x00720072;
        
    /** (0072,0074) VR=FD, VM=1-n Selector FD Value  */
    public static final int SELECTOR_FD_VALUE = 0x00720074;
        
    /** (0072,0076) VR=FL, VM=1-n Selector FL Value  */
    public static final int SELECTOR_FL_VALUE = 0x00720076;
        
    /** (0072,0078) VR=UL, VM=1-n Selector UL Value  */
    public static final int SELECTOR_UL_VALUE = 0x00720078;
        
    /** (0072,007A) VR=US, VM=1-n Selector US Value  */
    public static final int SELECTOR_US_VALUE = 0x0072007A;
        
    /** (0072,007C) VR=SL, VM=1-n Selector SL Value  */
    public static final int SELECTOR_SL_VALUE = 0x0072007C;
        
    /** (0072,007E) VR=SS, VM=1-n Selector SS Value  */
    public static final int SELECTOR_SS_VALUE = 0x0072007E;
        
    /** (0072,0080) VR=SQ, VM=1 Selector Code Sequence Value  */
    public static final int SELECTOR_CODE_SEQUENCE_VALUE = 0x00720080;
        
    /** (0072,0100) VR=US, VM=1 Number of Screens  */
    public static final int NUMBER_OF_SCREENS = 0x00720100;
        
    /** (0072,0102) VR=SQ, VM=1 Nominal Screen Definition Sequence  */
    public static final int NOMINAL_SCREEN_DEFINITION_SEQUENCE = 0x00720102;
        
    /** (0072,0104) VR=US, VM=1 Number of Vertical Pixels  */
    public static final int NUMBER_OF_VERTICAL_PIXELS = 0x00720104;
        
    /** (0072,0106) VR=US, VM=1 Number of Horizontal Pixels  */
    public static final int NUMBER_OF_HORIZONTAL_PIXELS = 0x00720106;
        
    /** (0072,0108) VR=FD, VM=4 Display Environment Spatial Position  */
    public static final int DISPLAY_ENVIRONMENT_SPATIAL_POSITION = 0x00720108;
        
    /** (0072,010A) VR=US, VM=1 Screen Minimum Grayscale Bit Depth  */
    public static final int SCREEN_MINIMUM_GRAYSCALE_BIT_DEPTH = 0x0072010A;
        
    /** (0072,010C) VR=US, VM=1 Screen Minimum Color Bit Depth  */
    public static final int SCREEN_MINIMUM_COLOR_BIT_DEPTH = 0x0072010C;
        
    /** (0072,010E) VR=US, VM=1 Application Maximum Repaint Time  */
    public static final int APPLICATION_MAXIMUM_REPAINT_TIME = 0x0072010E;
        
    /** (0072,0200) VR=SQ, VM=1 Display Sets Sequence  */
    public static final int DISPLAY_SETS_SEQUENCE = 0x00720200;
        
    /** (0072,0202) VR=US, VM=1 Display Set Number  */
    public static final int DISPLAY_SET_NUMBER = 0x00720202;
        
    /** (0072,0203) VR=LO, VM=1 Display Set Label  */
    public static final int DISPLAY_SET_LABEL = 0x00720203;
        
    /** (0072,0204) VR=US, VM=1 Display Set Presentation Group  */
    public static final int DISPLAY_SET_PRESENTATION_GROUP = 0x00720204;
        
    /** (0072,0206) VR=LO, VM=1 Display Set Presentation Group Description  */
    public static final int DISPLAY_SET_PRESENTATION_GROUP_DESCRIPTION = 0x00720206;
        
    /** (0072,0208) VR=CS, VM=1 Partial Data Display Handling  */
    public static final int PARTIAL_DATA_DISPLAY_HANDLING = 0x00720208;
        
    /** (0072,0210) VR=SQ, VM=1 Synchronized Scrolling Sequence  */
    public static final int SYNCHRONIZED_SCROLLING_SEQUENCE = 0x00720210;
        
    /** (0072,0212) VR=US, VM=2-n Display Set Scrolling Group  */
    public static final int DISPLAY_SET_SCROLLING_GROUP = 0x00720212;
        
    /** (0072,0214) VR=SQ, VM=1 Navigation Indicator Sequence  */
    public static final int NAVIGATION_INDICATOR_SEQUENCE = 0x00720214;
        
    /** (0072,0216) VR=US, VM=1 Navigation Display Set  */
    public static final int NAVIGATION_DISPLAY_SET = 0x00720216;
        
    /** (0072,0218) VR=US, VM=1-n Reference Display Sets  */
    public static final int REFERENCE_DISPLAY_SETS = 0x00720218;
        
    /** (0072,0300) VR=SQ, VM=1 Image Boxes Sequence  */
    public static final int IMAGE_BOXES_SEQUENCE = 0x00720300;
        
    /** (0072,0302) VR=US, VM=1 Image Box Number  */
    public static final int IMAGE_BOX_NUMBER = 0x00720302;
        
    /** (0072,0304) VR=CS, VM=1 Image Box Layout Type  */
    public static final int IMAGE_BOX_LAYOUT_TYPE = 0x00720304;
        
    /** (0072,0306) VR=US, VM=1 Image Box Tile Horizontal Dimension  */
    public static final int IMAGE_BOX_TILE_HORIZONTAL_DIMENSION = 0x00720306;
        
    /** (0072,0308) VR=US, VM=1 Image Box Tile Vertical Dimension  */
    public static final int IMAGE_BOX_TILE_VERTICAL_DIMENSION = 0x00720308;
        
    /** (0072,0310) VR=CS, VM=1 Image Box Scroll Direction  */
    public static final int IMAGE_BOX_SCROLL_DIRECTION = 0x00720310;
        
    /** (0072,0312) VR=CS, VM=1 Image Box Small Scroll Type  */
    public static final int IMAGE_BOX_SMALL_SCROLL_TYPE = 0x00720312;
        
    /** (0072,0314) VR=US, VM=1 Image Box Small Scroll Amount  */
    public static final int IMAGE_BOX_SMALL_SCROLL_AMOUNT = 0x00720314;
        
    /** (0072,0316) VR=CS, VM=1 Image Box Large Scroll Type  */
    public static final int IMAGE_BOX_LARGE_SCROLL_TYPE = 0x00720316;
        
    /** (0072,0318) VR=US, VM=1 Image Box Large Scroll Amount  */
    public static final int IMAGE_BOX_LARGE_SCROLL_AMOUNT = 0x00720318;
        
    /** (0072,0320) VR=US, VM=1 Image Box Overlap Priority  */
    public static final int IMAGE_BOX_OVERLAP_PRIORITY = 0x00720320;
        
    /** (0072,0330) VR=FD, VM=1 Cine Relative to Real-Time  */
    public static final int CINE_RELATIVE_TO_REAL_TIME = 0x00720330;
        
    /** (0072,0400) VR=SQ, VM=1 Filter Operations Sequence  */
    public static final int FILTER_OPERATIONS_SEQUENCE = 0x00720400;
        
    /** (0072,0402) VR=CS, VM=1 Filter-by Category  */
    public static final int FILTER_BY_CATEGORY = 0x00720402;
        
    /** (0072,0404) VR=CS, VM=1 Filter-by Attribute Presence  */
    public static final int FILTER_BY_ATTRIBUTE_PRESENCE = 0x00720404;
        
    /** (0072,0406) VR=CS, VM=1 Filter-by Operator  */
    public static final int FILTER_BY_OPERATOR = 0x00720406;
        
    /** (0072,0500) VR=CS, VM=1 Blending Operation Type  */
    public static final int BLENDING_OPERATION_TYPE = 0x00720500;
        
    /** (0072,0510) VR=CS, VM=1 Reformatting Operation Type  */
    public static final int REFORMATTING_OPERATION_TYPE = 0x00720510;
        
    /** (0072,0512) VR=FD, VM=1 Reformatting Thickness  */
    public static final int REFORMATTING_THICKNESS = 0x00720512;
        
    /** (0072,0514) VR=FD, VM=1 Reformatting Interval  */
    public static final int REFORMATTING_INTERVAL = 0x00720514;
        
    /** (0072,0516) VR=CS, VM=1 Reformatting Operation Initial View Direction  */
    public static final int REFORMATTING_OPERATION_INITIAL_VIEW_DIRECTION = 0x00720516;
        
    /** (0072,0520) VR=CS, VM=1-n 3D Rendering Type  */
    public static final int _3D_RENDERING_TYPE = 0x00720520;
        
    /** (0072,0600) VR=SQ, VM=1 Sorting Operations Sequence  */
    public static final int SORTING_OPERATIONS_SEQUENCE = 0x00720600;
        
    /** (0072,0602) VR=CS, VM=1 Sort-by Category  */
    public static final int SORT_BY_CATEGORY = 0x00720602;
        
    /** (0072,0604) VR=CS, VM=1 Sorting Direction  */
    public static final int SORTING_DIRECTION = 0x00720604;
        
    /** (0072,0700) VR=CS, VM=2 Display Set Patient Orientation  */
    public static final int DISPLAY_SET_PATIENT_ORIENTATION = 0x00720700;
        
    /** (0072,0702) VR=CS, VM=1 VOI Type  */
    public static final int VOI_TYPE = 0x00720702;
        
    /** (0072,0704) VR=CS, VM=1 Pseudo-color Type  */
    public static final int PSEUDO_COLOR_TYPE = 0x00720704;
        
    /** (0072,0706) VR=CS, VM=1 Show Grayscale Inverted  */
    public static final int SHOW_GRAYSCALE_INVERTED = 0x00720706;
        
    /** (0072,0710) VR=CS, VM=1 Show Image True Size Flag  */
    public static final int SHOW_IMAGE_TRUE_SIZE_FLAG = 0x00720710;
        
    /** (0072,0712) VR=CS, VM=1 Show Graphic Annotation Flag  */
    public static final int SHOW_GRAPHIC_ANNOTATION_FLAG = 0x00720712;
        
    /** (0072,0714) VR=CS, VM=1 Show Patient Demographics Flag  */
    public static final int SHOW_PATIENT_DEMOGRAPHICS_FLAG = 0x00720714;
        
    /** (0072,0716) VR=CS, VM=1 Show Acquisition Techniques Flag  */
    public static final int SHOW_ACQUISITION_TECHNIQUES_FLAG = 0x00720716;
        
    /** (0072,0717) VR=CS, VM=1 Display Set Horizontal Justification  */
    public static final int DISPLAY_SET_HORIZONTAL_JUSTIFICATION = 0x00720717;
        
    /** (0072,0718) VR=CS, VM=1 Display Set Vertical Justification  */
    public static final int DISPLAY_SET_VERTICAL_JUSTIFICATION = 0x00720718;
        
    /** (0088,0130) VR=SH, VM=1 Storage Media File-set ID  */
    public static final int STORAGE_MEDIA_FILE_SET_ID = 0x00880130;
        
    /** (0088,0140) VR=UI, VM=1 Storage Media File-set UID  */
    public static final int STORAGE_MEDIA_FILE_SET_UID = 0x00880140;
        
    /** (0088,0200) VR=SQ, VM=1 Icon Image Sequence  */
    public static final int ICON_IMAGE_SEQUENCE = 0x00880200;
        
    /** (0088,0904) VR=LO, VM=1 Topic Title  */
    public static final int TOPIC_TITLE = 0x00880904;
        
    /** (0088,0906) VR=ST, VM=1 Topic Subject  */
    public static final int TOPIC_SUBJECT = 0x00880906;
        
    /** (0088,0910) VR=LO, VM=1 Topic Author  */
    public static final int TOPIC_AUTHOR = 0x00880910;
        
    /** (0088,0912) VR=LO, VM=1-32 Topic Keywords  */
    public static final int TOPIC_KEYWORDS = 0x00880912;
        
    /** (0100,0410) VR=CS, VM=1 SOP Instance Status  */
    public static final int SOP_INSTANCE_STATUS = 0x01000410;
        
    /** (0100,0420) VR=DT, VM=1 SOP Authorization Date and Time  */
    public static final int SOP_AUTHORIZATION_DATE_AND_TIME = 0x01000420;
        
    /** (0100,0424) VR=LT, VM=1 SOP Authorization Comment  */
    public static final int SOP_AUTHORIZATION_COMMENT = 0x01000424;
        
    /** (0100,0426) VR=LO, VM=1 Authorization Equipment Certification Number  */
    public static final int AUTHORIZATION_EQUIPMENT_CERTIFICATION_NUMBER = 0x01000426;
        
    /** (0400,0005) VR=US, VM=1 MAC ID Number  */
    public static final int MAC_ID_NUMBER = 0x04000005;
        
    /** (0400,0010) VR=UI, VM=1 MAC Calculation Transfer Syntax UID  */
    public static final int MAC_CALCULATION_TRANSFER_SYNTAX_UID = 0x04000010;
        
    /** (0400,0015) VR=CS, VM=1 MAC Algorithm  */
    public static final int MAC_ALGORITHM = 0x04000015;
        
    /** (0400,0020) VR=AT, VM=1-n Data Elements Signed  */
    public static final int DATA_ELEMENTS_SIGNED = 0x04000020;
        
    /** (0400,0100) VR=UI, VM=1 Digital Signature UID  */
    public static final int DIGITAL_SIGNATURE_UID = 0x04000100;
        
    /** (0400,0105) VR=DT, VM=1 Digital Signature DateTime  */
    public static final int DIGITAL_SIGNATURE_DATETIME = 0x04000105;
        
    /** (0400,0110) VR=CS, VM=1 Certificate Type  */
    public static final int CERTIFICATE_TYPE = 0x04000110;
        
    /** (0400,0115) VR=OB, VM=1 Certificate of Signer  */
    public static final int CERTIFICATE_OF_SIGNER = 0x04000115;
        
    /** (0400,0120) VR=OB, VM=1 Signature  */
    public static final int SIGNATURE = 0x04000120;
        
    /** (0400,0305) VR=CS, VM=1 Certified Timestamp Type  */
    public static final int CERTIFIED_TIMESTAMP_TYPE = 0x04000305;
        
    /** (0400,0310) VR=OB, VM=1 Certified Timestamp  */
    public static final int CERTIFIED_TIMESTAMP = 0x04000310;
        
    /** (0400,0401) VR=SQ, VM=1 Digital Signature Purpose Code Sequence  */
    public static final int DIGITAL_SIGNATURE_PURPOSE_CODE_SEQUENCE = 0x04000401;
        
    /** (0400,0402) VR=SQ, VM=1 Referenced Digital Signature Sequence  */
    public static final int REFERENCED_DIGITAL_SIGNATURE_SEQUENCE = 0x04000402;
        
    /** (0400,0403) VR=SQ, VM=1 Referenced SOP Instance MAC Sequence  */
    public static final int REFERENCED_SOP_INSTANCE_MAC_SEQUENCE = 0x04000403;
        
    /** (0400,0404) VR=OB, VM=1 MAC  */
    public static final int MAC = 0x04000404;
        
    /** (0400,0500) VR=SQ, VM=1 Encrypted Attributes Sequence  */
    public static final int ENCRYPTED_ATTRIBUTES_SEQUENCE = 0x04000500;
        
    /** (0400,0510) VR=UI, VM=1 Encrypted Content Transfer Syntax UID  */
    public static final int ENCRYPTED_CONTENT_TRANSFER_SYNTAX_UID = 0x04000510;
        
    /** (0400,0520) VR=OB, VM=1 Encrypted Content  */
    public static final int ENCRYPTED_CONTENT = 0x04000520;
        
    /** (0400,0550) VR=SQ, VM=1 Modified Attributes Sequence  */
    public static final int MODIFIED_ATTRIBUTES_SEQUENCE = 0x04000550;
        
    /** (2000,0010) VR=IS, VM=1 Number of Copies  */
    public static final int NUMBER_OF_COPIES = 0x20000010;
        
    /** (2000,001E) VR=SQ, VM=1 Printer Configuration Sequence  */
    public static final int PRINTER_CONFIGURATION_SEQUENCE = 0x2000001E;
        
    /** (2000,0020) VR=CS, VM=1 Print Priority  */
    public static final int PRINT_PRIORITY = 0x20000020;
        
    /** (2000,0030) VR=CS, VM=1 Medium Type  */
    public static final int MEDIUM_TYPE = 0x20000030;
        
    /** (2000,0040) VR=CS, VM=1 Film Destination  */
    public static final int FILM_DESTINATION = 0x20000040;
        
    /** (2000,0050) VR=LO, VM=1 Film Session Label  */
    public static final int FILM_SESSION_LABEL = 0x20000050;
        
    /** (2000,0060) VR=IS, VM=1 Memory Allocation  */
    public static final int MEMORY_ALLOCATION = 0x20000060;
        
    /** (2000,0061) VR=IS, VM=1 Maximum Memory Allocation  */
    public static final int MAXIMUM_MEMORY_ALLOCATION = 0x20000061;
        
    /** (2000,0062) VR=CS, VM=1 Color Image Printing Flag RET */
    public static final int COLOR_IMAGE_PRINTING_FLAG_RET = 0x20000062;
        
    /** (2000,0063) VR=CS, VM=1 Collation Flag RET */
    public static final int COLLATION_FLAG_RET = 0x20000063;
        
    /** (2000,0065) VR=CS, VM=1 Annotation Flag RET */
    public static final int ANNOTATION_FLAG_RET = 0x20000065;
        
    /** (2000,0067) VR=CS, VM=1 Image Overlay Flag RET */
    public static final int IMAGE_OVERLAY_FLAG_RET = 0x20000067;
        
    /** (2000,0069) VR=CS, VM=1 Presentation LUT Flag RET */
    public static final int PRESENTATION_LUT_FLAG_RET = 0x20000069;
        
    /** (2000,006A) VR=CS, VM=1 Image Box Presentation LUT Flag RET */
    public static final int IMAGE_BOX_PRESENTATION_LUT_FLAG_RET = 0x2000006A;
        
    /** (2000,00A0) VR=US, VM=1 Memory Bit Depth  */
    public static final int MEMORY_BIT_DEPTH = 0x200000A0;
        
    /** (2000,00A1) VR=US, VM=1 Printing Bit Depth  */
    public static final int PRINTING_BIT_DEPTH = 0x200000A1;
        
    /** (2000,00A2) VR=SQ, VM=1 Media Installed Sequence  */
    public static final int MEDIA_INSTALLED_SEQUENCE = 0x200000A2;
        
    /** (2000,00A4) VR=SQ, VM=1 Other Media Available Sequence  */
    public static final int OTHER_MEDIA_AVAILABLE_SEQUENCE = 0x200000A4;
        
    /** (2000,00A8) VR=SQ, VM=1 Supported Image Display Formats Sequence  */
    public static final int SUPPORTED_IMAGE_DISPLAY_FORMATS_SEQUENCE = 0x200000A8;
        
    /** (2000,0500) VR=SQ, VM=1 Referenced Film Box Sequence  */
    public static final int REFERENCED_FILM_BOX_SEQUENCE = 0x20000500;
        
    /** (2000,0510) VR=SQ, VM=1 Referenced Stored Print Sequence  */
    public static final int REFERENCED_STORED_PRINT_SEQUENCE = 0x20000510;
        
    /** (2010,0010) VR=ST, VM=1 Image Display Format  */
    public static final int IMAGE_DISPLAY_FORMAT = 0x20100010;
        
    /** (2010,0030) VR=CS, VM=1 Annotation Display Format ID  */
    public static final int ANNOTATION_DISPLAY_FORMAT_ID = 0x20100030;
        
    /** (2010,0040) VR=CS, VM=1 Film Orientation  */
    public static final int FILM_ORIENTATION = 0x20100040;
        
    /** (2010,0050) VR=CS, VM=1 Film Size ID  */
    public static final int FILM_SIZE_ID = 0x20100050;
        
    /** (2010,0052) VR=CS, VM=1 Printer Resolution ID  */
    public static final int PRINTER_RESOLUTION_ID = 0x20100052;
        
    /** (2010,0054) VR=CS, VM=1 Default Printer Resolution ID  */
    public static final int DEFAULT_PRINTER_RESOLUTION_ID = 0x20100054;
        
    /** (2010,0060) VR=CS, VM=1 Magnification Type  */
    public static final int MAGNIFICATION_TYPE = 0x20100060;
        
    /** (2010,0080) VR=CS, VM=1 Smoothing Type  */
    public static final int SMOOTHING_TYPE = 0x20100080;
        
    /** (2010,00A6) VR=CS, VM=1 Default Magnification Type  */
    public static final int DEFAULT_MAGNIFICATION_TYPE = 0x201000A6;
        
    /** (2010,00A7) VR=CS, VM=1-n Other Magnification Types Available  */
    public static final int OTHER_MAGNIFICATION_TYPES_AVAILABLE = 0x201000A7;
        
    /** (2010,00A8) VR=CS, VM=1 Default Smoothing Type  */
    public static final int DEFAULT_SMOOTHING_TYPE = 0x201000A8;
        
    /** (2010,00A9) VR=CS, VM=1-n Other Smoothing Types Available  */
    public static final int OTHER_SMOOTHING_TYPES_AVAILABLE = 0x201000A9;
        
    /** (2010,0100) VR=CS, VM=1 Border Density  */
    public static final int BORDER_DENSITY = 0x20100100;
        
    /** (2010,0110) VR=CS, VM=1 Empty Image Density  */
    public static final int EMPTY_IMAGE_DENSITY = 0x20100110;
        
    /** (2010,0120) VR=US, VM=1 Min Density  */
    public static final int MIN_DENSITY = 0x20100120;
        
    /** (2010,0130) VR=US, VM=1 Max Density  */
    public static final int MAX_DENSITY = 0x20100130;
        
    /** (2010,0140) VR=CS, VM=1 Trim  */
    public static final int TRIM = 0x20100140;
        
    /** (2010,0150) VR=ST, VM=1 Configuration Information  */
    public static final int CONFIGURATION_INFORMATION = 0x20100150;
        
    /** (2010,0152) VR=LT, VM=1 Configuration Information Description  */
    public static final int CONFIGURATION_INFORMATION_DESCRIPTION = 0x20100152;
        
    /** (2010,0154) VR=IS, VM=1 Maximum Collated Films  */
    public static final int MAXIMUM_COLLATED_FILMS = 0x20100154;
        
    /** (2010,015E) VR=US, VM=1 Illumination  */
    public static final int ILLUMINATION = 0x2010015E;
        
    /** (2010,0160) VR=US, VM=1 Reflected Ambient Light  */
    public static final int REFLECTED_AMBIENT_LIGHT = 0x20100160;
        
    /** (2010,0376) VR=DS, VM=2 Printer Pixel Spacing  */
    public static final int PRINTER_PIXEL_SPACING = 0x20100376;
        
    /** (2010,0500) VR=SQ, VM=1 Referenced Film Session Sequence  */
    public static final int REFERENCED_FILM_SESSION_SEQUENCE = 0x20100500;
        
    /** (2010,0510) VR=SQ, VM=1 Referenced Image Box Sequence  */
    public static final int REFERENCED_IMAGE_BOX_SEQUENCE = 0x20100510;
        
    /** (2010,0520) VR=SQ, VM=1 Referenced Basic Annotation Box Sequence  */
    public static final int REFERENCED_BASIC_ANNOTATION_BOX_SEQUENCE = 0x20100520;
        
    /** (2020,0010) VR=US, VM=1 Image Position  */
    public static final int IMAGE_POSITION = 0x20200010;
        
    /** (2020,0020) VR=CS, VM=1 Polarity  */
    public static final int POLARITY = 0x20200020;
        
    /** (2020,0030) VR=DS, VM=1 Requested Image Size  */
    public static final int REQUESTED_IMAGE_SIZE = 0x20200030;
        
    /** (2020,0040) VR=CS, VM=1 Requested Decimate/Crop Behavior  */
    public static final int REQUESTED_DECIMATE_CROP_BEHAVIOR = 0x20200040;
        
    /** (2020,0050) VR=CS, VM=1 Requested Resolution ID  */
    public static final int REQUESTED_RESOLUTION_ID = 0x20200050;
        
    /** (2020,00A0) VR=CS, VM=1 Requested Image Size Flag  */
    public static final int REQUESTED_IMAGE_SIZE_FLAG = 0x202000A0;
        
    /** (2020,00A2) VR=CS, VM=1 Decimate/Crop Result  */
    public static final int DECIMATE_CROP_RESULT = 0x202000A2;
        
    /** (2020,0110) VR=SQ, VM=1 Basic Grayscale Image Sequence  */
    public static final int BASIC_GRAYSCALE_IMAGE_SEQUENCE = 0x20200110;
        
    /** (2020,0111) VR=SQ, VM=1 Basic Color Image Sequence  */
    public static final int BASIC_COLOR_IMAGE_SEQUENCE = 0x20200111;
        
    /** (2020,0130) VR=SQ, VM=1 Referenced Image Overlay Box Sequence RET */
    public static final int REFERENCED_IMAGE_OVERLAY_BOX_SEQUENCE_RET = 0x20200130;
        
    /** (2020,0140) VR=SQ, VM=1 Referenced VOI LUT Box Sequence RET */
    public static final int REFERENCED_VOI_LUT_BOX_SEQUENCE_RET = 0x20200140;
        
    /** (2030,0010) VR=US, VM=1 Annotation Position  */
    public static final int ANNOTATION_POSITION = 0x20300010;
        
    /** (2030,0020) VR=LO, VM=1 Text String  */
    public static final int TEXT_STRING = 0x20300020;
        
    /** (2040,0010) VR=SQ, VM=1 Referenced Overlay Plane Sequence RET */
    public static final int REFERENCED_OVERLAY_PLANE_SEQUENCE_RET = 0x20400010;
        
    /** (2040,0011) VR=US, VM=1-99 Referenced Overlay Plane Groups RET */
    public static final int REFERENCED_OVERLAY_PLANE_GROUPS_RET = 0x20400011;
        
    /** (2040,0020) VR=SQ, VM=1 Overlay Pixel Data Sequence RET */
    public static final int OVERLAY_PIXEL_DATA_SEQUENCE_RET = 0x20400020;
        
    /** (2040,0060) VR=CS, VM=1 Overlay Magnification Type RET */
    public static final int OVERLAY_MAGNIFICATION_TYPE_RET = 0x20400060;
        
    /** (2040,0070) VR=CS, VM=1 Overlay Smoothing Type RET */
    public static final int OVERLAY_SMOOTHING_TYPE_RET = 0x20400070;
        
    /** (2040,0072) VR=CS, VM=1 Overlay or Image Magnification RET */
    public static final int OVERLAY_OR_IMAGE_MAGNIFICATION_RET = 0x20400072;
        
    /** (2040,0074) VR=US, VM=1 Magnify to Number of Columns RET */
    public static final int MAGNIFY_TO_NUMBER_OF_COLUMNS_RET = 0x20400074;
        
    /** (2040,0080) VR=CS, VM=1 Overlay Foreground Density RET */
    public static final int OVERLAY_FOREGROUND_DENSITY_RET = 0x20400080;
        
    /** (2040,0082) VR=CS, VM=1 Overlay Background Density RET */
    public static final int OVERLAY_BACKGROUND_DENSITY_RET = 0x20400082;
        
    /** (2040,0090) VR=CS, VM=1 Overlay Mode RET */
    public static final int OVERLAY_MODE_RET = 0x20400090;
        
    /** (2040,0100) VR=CS, VM=1 Threshold Density RET */
    public static final int THRESHOLD_DENSITY_RET = 0x20400100;
        
    /** (2040,0500) VR=SQ, VM=1 Referenced Image Box Sequence RET */
    public static final int REFERENCED_IMAGE_BOX_SEQUENCE_RET = 0x20400500;
        
    /** (2050,0010) VR=SQ, VM=1 Presentation LUT Sequence  */
    public static final int PRESENTATION_LUT_SEQUENCE = 0x20500010;
        
    /** (2050,0020) VR=CS, VM=1 Presentation LUT Shape  */
    public static final int PRESENTATION_LUT_SHAPE = 0x20500020;
        
    /** (2050,0500) VR=SQ, VM=1 Referenced Presentation LUT Sequence  */
    public static final int REFERENCED_PRESENTATION_LUT_SEQUENCE = 0x20500500;
        
    /** (2100,0010) VR=SH, VM=1 Print Job ID  */
    public static final int PRINT_JOB_ID = 0x21000010;
        
    /** (2100,0020) VR=CS, VM=1 Execution Status  */
    public static final int EXECUTION_STATUS = 0x21000020;
        
    /** (2100,0030) VR=CS, VM=1 Execution Status Info  */
    public static final int EXECUTION_STATUS_INFO = 0x21000030;
        
    /** (2100,0040) VR=DA, VM=1 Creation Date  */
    public static final int CREATION_DATE = 0x21000040;
        
    /** (2100,0050) VR=TM, VM=1 Creation Time  */
    public static final int CREATION_TIME = 0x21000050;
        
    /** (2100,0070) VR=AE, VM=1 Originator  */
    public static final int ORIGINATOR = 0x21000070;
        
    /** (2100,0140) VR=AE, VM=1 Destination AE  */
    public static final int DESTINATION_AE = 0x21000140;
        
    /** (2100,0160) VR=SH, VM=1 Owner ID  */
    public static final int OWNER_ID = 0x21000160;
        
    /** (2100,0170) VR=IS, VM=1 Number of Films  */
    public static final int NUMBER_OF_FILMS = 0x21000170;
        
    /** (2100,0500) VR=SQ, VM=1 Referenced Print Job Sequence (Pull Stored Print) RET */
    public static final int REFERENCED_PRINT_JOB_SEQUENCE_PULL_STORED_PRINT_RET = 0x21000500;
        
    /** (2110,0010) VR=CS, VM=1 Printer Status  */
    public static final int PRINTER_STATUS = 0x21100010;
        
    /** (2110,0020) VR=CS, VM=1 Printer Status Info  */
    public static final int PRINTER_STATUS_INFO = 0x21100020;
        
    /** (2110,0030) VR=LO, VM=1 Printer Name  */
    public static final int PRINTER_NAME = 0x21100030;
        
    /** (2110,0099) VR=SH, VM=1 Print Queue ID RET */
    public static final int PRINT_QUEUE_ID_RET = 0x21100099;
        
    /** (2120,0010) VR=CS, VM=1 Queue Status RET */
    public static final int QUEUE_STATUS_RET = 0x21200010;
        
    /** (2120,0050) VR=SQ, VM=1 Print Job Description Sequence RET */
    public static final int PRINT_JOB_DESCRIPTION_SEQUENCE_RET = 0x21200050;
        
    /** (2120,0070) VR=SQ, VM=1 Referenced Print Job Sequence RET */
    public static final int REFERENCED_PRINT_JOB_SEQUENCE_RET = 0x21200070;
        
    /** (2130,0010) VR=SQ, VM=1 Print Management Capabilities Sequence RET */
    public static final int PRINT_MANAGEMENT_CAPABILITIES_SEQUENCE_RET = 0x21300010;
        
    /** (2130,0015) VR=SQ, VM=1 Printer Characteristics Sequence RET */
    public static final int PRINTER_CHARACTERISTICS_SEQUENCE_RET = 0x21300015;
        
    /** (2130,0030) VR=SQ, VM=1 Film Box Content Sequence RET */
    public static final int FILM_BOX_CONTENT_SEQUENCE_RET = 0x21300030;
        
    /** (2130,0040) VR=SQ, VM=1 Image Box Content Sequence RET */
    public static final int IMAGE_BOX_CONTENT_SEQUENCE_RET = 0x21300040;
        
    /** (2130,0050) VR=SQ, VM=1 Annotation Content Sequence RET */
    public static final int ANNOTATION_CONTENT_SEQUENCE_RET = 0x21300050;
        
    /** (2130,0060) VR=SQ, VM=1 Image Overlay Box Content Sequence RET */
    public static final int IMAGE_OVERLAY_BOX_CONTENT_SEQUENCE_RET = 0x21300060;
        
    /** (2130,0080) VR=SQ, VM=1 Presentation LUT Content Sequence RET */
    public static final int PRESENTATION_LUT_CONTENT_SEQUENCE_RET = 0x21300080;
        
    /** (2130,00A0) VR=SQ, VM=1 Proposed Study Sequence RET */
    public static final int PROPOSED_STUDY_SEQUENCE_RET = 0x213000A0;
        
    /** (2130,00C0) VR=SQ, VM=1 Original Image Sequence RET */
    public static final int ORIGINAL_IMAGE_SEQUENCE_RET = 0x213000C0;
        
    /** (2200,0001) VR=CS, VM=1 Label Using Information Extracted From Instances  */
    public static final int LABEL_USING_INFORMATION_EXTRACTED_FROM_INSTANCES = 0x22000001;
        
    /** (2200,0002) VR=UT, VM=1 Label Text  */
    public static final int LABEL_TEXT = 0x22000002;
        
    /** (2200,0003) VR=CS, VM=1 Label Style Selection  */
    public static final int LABEL_STYLE_SELECTION = 0x22000003;
        
    /** (2200,0004) VR=LT, VM=1 Media Disposition  */
    public static final int MEDIA_DISPOSITION = 0x22000004;
        
    /** (2200,0005) VR=LT, VM=1 Barcode Value  */
    public static final int BARCODE_VALUE = 0x22000005;
        
    /** (2200,0006) VR=CS, VM=1 Barcode Symbology  */
    public static final int BARCODE_SYMBOLOGY = 0x22000006;
        
    /** (2200,0007) VR=CS, VM=1 Allow Media Splitting  */
    public static final int ALLOW_MEDIA_SPLITTING = 0x22000007;
        
    /** (2200,0008) VR=CS, VM=1 Include Non-DICOM Objects  */
    public static final int INCLUDE_NON_DICOM_OBJECTS = 0x22000008;
        
    /** (2200,0009) VR=CS, VM=1 Include Display Application  */
    public static final int INCLUDE_DISPLAY_APPLICATION = 0x22000009;
        
    /** (2200,000A) VR=CS, VM=1 Preserve Composite Instances After Media Creation  */
    public static final int PRESERVE_COMPOSITE_INSTANCES_AFTER_MEDIA_CREATION = 0x2200000A;
        
    /** (2200,000B) VR=US, VM=1 Total Number of Pieces of Media Created  */
    public static final int TOTAL_NUMBER_OF_PIECES_OF_MEDIA_CREATED = 0x2200000B;
        
    /** (2200,000C) VR=LO, VM=1 Requested Media Application Profile  */
    public static final int REQUESTED_MEDIA_APPLICATION_PROFILE = 0x2200000C;
        
    /** (2200,000D) VR=SQ, VM=1 Referenced Storage Media Sequence  */
    public static final int REFERENCED_STORAGE_MEDIA_SEQUENCE = 0x2200000D;
        
    /** (2200,000E) VR=AT, VM=1-n Failure Attributes  */
    public static final int FAILURE_ATTRIBUTES = 0x2200000E;
        
    /** (2200,000F) VR=CS, VM=1 Allow Lossy Compression  */
    public static final int ALLOW_LOSSY_COMPRESSION = 0x2200000F;
        
    /** (2200,0020) VR=CS, VM=1 Request Priority  */
    public static final int REQUEST_PRIORITY = 0x22000020;
        
    /** (3002,0002) VR=SH, VM=1 RT Image Label  */
    public static final int RT_IMAGE_LABEL = 0x30020002;
        
    /** (3002,0003) VR=LO, VM=1 RT Image Name  */
    public static final int RT_IMAGE_NAME = 0x30020003;
        
    /** (3002,0004) VR=ST, VM=1 RT Image Description  */
    public static final int RT_IMAGE_DESCRIPTION = 0x30020004;
        
    /** (3002,000A) VR=CS, VM=1 Reported Values Origin  */
    public static final int REPORTED_VALUES_ORIGIN = 0x3002000A;
        
    /** (3002,000C) VR=CS, VM=1 RT Image Plane  */
    public static final int RT_IMAGE_PLANE = 0x3002000C;
        
    /** (3002,000D) VR=DS, VM=3 X-Ray Image Receptor Translation  */
    public static final int X_RAY_IMAGE_RECEPTOR_TRANSLATION = 0x3002000D;
        
    /** (3002,000E) VR=DS, VM=1 X-Ray Image Receptor Angle  */
    public static final int X_RAY_IMAGE_RECEPTOR_ANGLE = 0x3002000E;
        
    /** (3002,0010) VR=DS, VM=6 RT Image Orientation  */
    public static final int RT_IMAGE_ORIENTATION = 0x30020010;
        
    /** (3002,0011) VR=DS, VM=2 Image Plane Pixel Spacing  */
    public static final int IMAGE_PLANE_PIXEL_SPACING = 0x30020011;
        
    /** (3002,0012) VR=DS, VM=2 RT Image Position  */
    public static final int RT_IMAGE_POSITION = 0x30020012;
        
    /** (3002,0020) VR=SH, VM=1 Radiation Machine Name  */
    public static final int RADIATION_MACHINE_NAME = 0x30020020;
        
    /** (3002,0022) VR=DS, VM=1 Radiation Machine SAD  */
    public static final int RADIATION_MACHINE_SAD = 0x30020022;
        
    /** (3002,0024) VR=DS, VM=1 Radiation Machine SSD  */
    public static final int RADIATION_MACHINE_SSD = 0x30020024;
        
    /** (3002,0026) VR=DS, VM=1 RT Image SID  */
    public static final int RT_IMAGE_SID = 0x30020026;
        
    /** (3002,0028) VR=DS, VM=1 Source to Reference Object Distance  */
    public static final int SOURCE_TO_REFERENCE_OBJECT_DISTANCE = 0x30020028;
        
    /** (3002,0029) VR=IS, VM=1 Fraction Number  */
    public static final int FRACTION_NUMBER = 0x30020029;
        
    /** (3002,0030) VR=SQ, VM=1 Exposure Sequence  */
    public static final int EXPOSURE_SEQUENCE = 0x30020030;
        
    /** (3002,0032) VR=DS, VM=1 Meterset Exposure  */
    public static final int METERSET_EXPOSURE = 0x30020032;
        
    /** (3002,0034) VR=DS, VM=4 Diaphragm Position  */
    public static final int DIAPHRAGM_POSITION = 0x30020034;
        
    /** (3002,0040) VR=SQ, VM=1 Fluence Map Sequence  */
    public static final int FLUENCE_MAP_SEQUENCE = 0x30020040;
        
    /** (3002,0041) VR=CS, VM=1 Fluence Data Source  */
    public static final int FLUENCE_DATA_SOURCE = 0x30020041;
        
    /** (3002,0042) VR=DS, VM=1 Fluence Data Scale  */
    public static final int FLUENCE_DATA_SCALE = 0x30020042;
        
    /** (3004,0001) VR=CS, VM=1 DVH Type  */
    public static final int DVH_TYPE = 0x30040001;
        
    /** (3004,0002) VR=CS, VM=1 Dose Units  */
    public static final int DOSE_UNITS = 0x30040002;
        
    /** (3004,0004) VR=CS, VM=1 Dose Type  */
    public static final int DOSE_TYPE = 0x30040004;
        
    /** (3004,0006) VR=LO, VM=1 Dose Comment  */
    public static final int DOSE_COMMENT = 0x30040006;
        
    /** (3004,0008) VR=DS, VM=3 Normalization Point  */
    public static final int NORMALIZATION_POINT = 0x30040008;
        
    /** (3004,000A) VR=CS, VM=1 Dose Summation Type  */
    public static final int DOSE_SUMMATION_TYPE = 0x3004000A;
        
    /** (3004,000C) VR=DS, VM=2-n Grid Frame Offset Vector  */
    public static final int GRID_FRAME_OFFSET_VECTOR = 0x3004000C;
        
    /** (3004,000E) VR=DS, VM=1 Dose Grid Scaling  */
    public static final int DOSE_GRID_SCALING = 0x3004000E;
        
    /** (3004,0010) VR=SQ, VM=1 RT Dose ROI Sequence  */
    public static final int RT_DOSE_ROI_SEQUENCE = 0x30040010;
        
    /** (3004,0012) VR=DS, VM=1 Dose Value  */
    public static final int DOSE_VALUE = 0x30040012;
        
    /** (3004,0014) VR=CS, VM=1-3 Tissue Heterogeneity Correction  */
    public static final int TISSUE_HETEROGENEITY_CORRECTION = 0x30040014;
        
    /** (3004,0040) VR=DS, VM=3 DVH Normalization Point  */
    public static final int DVH_NORMALIZATION_POINT = 0x30040040;
        
    /** (3004,0042) VR=DS, VM=1 DVH Normalization Dose Value  */
    public static final int DVH_NORMALIZATION_DOSE_VALUE = 0x30040042;
        
    /** (3004,0050) VR=SQ, VM=1 DVH Sequence  */
    public static final int DVH_SEQUENCE = 0x30040050;
        
    /** (3004,0052) VR=DS, VM=1 DVH Dose Scaling  */
    public static final int DVH_DOSE_SCALING = 0x30040052;
        
    /** (3004,0054) VR=CS, VM=1 DVH Volume Units  */
    public static final int DVH_VOLUME_UNITS = 0x30040054;
        
    /** (3004,0056) VR=IS, VM=1 DVH Number of Bins  */
    public static final int DVH_NUMBER_OF_BINS = 0x30040056;
        
    /** (3004,0058) VR=DS, VM=2-2n DVH Data  */
    public static final int DVH_DATA = 0x30040058;
        
    /** (3004,0060) VR=SQ, VM=1 DVH Referenced ROI Sequence  */
    public static final int DVH_REFERENCED_ROI_SEQUENCE = 0x30040060;
        
    /** (3004,0062) VR=CS, VM=1 DVH ROI Contribution Type  */
    public static final int DVH_ROI_CONTRIBUTION_TYPE = 0x30040062;
        
    /** (3004,0070) VR=DS, VM=1 DVH Minimum Dose  */
    public static final int DVH_MINIMUM_DOSE = 0x30040070;
        
    /** (3004,0072) VR=DS, VM=1 DVH Maximum Dose  */
    public static final int DVH_MAXIMUM_DOSE = 0x30040072;
        
    /** (3004,0074) VR=DS, VM=1 DVH Mean Dose  */
    public static final int DVH_MEAN_DOSE = 0x30040074;
        
    /** (3006,0002) VR=SH, VM=1 Structure Set Label  */
    public static final int STRUCTURE_SET_LABEL = 0x30060002;
        
    /** (3006,0004) VR=LO, VM=1 Structure Set Name  */
    public static final int STRUCTURE_SET_NAME = 0x30060004;
        
    /** (3006,0006) VR=ST, VM=1 Structure Set Description  */
    public static final int STRUCTURE_SET_DESCRIPTION = 0x30060006;
        
    /** (3006,0008) VR=DA, VM=1 Structure Set Date  */
    public static final int STRUCTURE_SET_DATE = 0x30060008;
        
    /** (3006,0009) VR=TM, VM=1 Structure Set Time  */
    public static final int STRUCTURE_SET_TIME = 0x30060009;
        
    /** (3006,0010) VR=SQ, VM=1 Referenced Frame of Reference Sequence  */
    public static final int REFERENCED_FRAME_OF_REFERENCE_SEQUENCE = 0x30060010;
        
    /** (3006,0012) VR=SQ, VM=1 RT Referenced Study Sequence  */
    public static final int RT_REFERENCED_STUDY_SEQUENCE = 0x30060012;
        
    /** (3006,0014) VR=SQ, VM=1 RT Referenced Series Sequence  */
    public static final int RT_REFERENCED_SERIES_SEQUENCE = 0x30060014;
        
    /** (3006,0016) VR=SQ, VM=1 Contour Image Sequence  */
    public static final int CONTOUR_IMAGE_SEQUENCE = 0x30060016;
        
    /** (3006,0020) VR=SQ, VM=1 Structure Set ROI Sequence  */
    public static final int STRUCTURE_SET_ROI_SEQUENCE = 0x30060020;
        
    /** (3006,0022) VR=IS, VM=1 ROI Number  */
    public static final int ROI_NUMBER = 0x30060022;
        
    /** (3006,0024) VR=UI, VM=1 Referenced Frame of Reference UID  */
    public static final int REFERENCED_FRAME_OF_REFERENCE_UID = 0x30060024;
        
    /** (3006,0026) VR=LO, VM=1 ROI Name  */
    public static final int ROI_NAME = 0x30060026;
        
    /** (3006,0028) VR=ST, VM=1 ROI Description  */
    public static final int ROI_DESCRIPTION = 0x30060028;
        
    /** (3006,002A) VR=IS, VM=3 ROI Display Color  */
    public static final int ROI_DISPLAY_COLOR = 0x3006002A;
        
    /** (3006,002C) VR=DS, VM=1 ROI Volume  */
    public static final int ROI_VOLUME = 0x3006002C;
        
    /** (3006,0030) VR=SQ, VM=1 RT Related ROI Sequence  */
    public static final int RT_RELATED_ROI_SEQUENCE = 0x30060030;
        
    /** (3006,0033) VR=CS, VM=1 RT ROI Relationship  */
    public static final int RT_ROI_RELATIONSHIP = 0x30060033;
        
    /** (3006,0036) VR=CS, VM=1 ROI Generation Algorithm  */
    public static final int ROI_GENERATION_ALGORITHM = 0x30060036;
        
    /** (3006,0038) VR=LO, VM=1 ROI Generation Description  */
    public static final int ROI_GENERATION_DESCRIPTION = 0x30060038;
        
    /** (3006,0039) VR=SQ, VM=1 ROI Contour Sequence  */
    public static final int ROI_CONTOUR_SEQUENCE = 0x30060039;
        
    /** (3006,0040) VR=SQ, VM=1 Contour Sequence  */
    public static final int CONTOUR_SEQUENCE = 0x30060040;
        
    /** (3006,0042) VR=CS, VM=1 Contour Geometric Type  */
    public static final int CONTOUR_GEOMETRIC_TYPE = 0x30060042;
        
    /** (3006,0044) VR=DS, VM=1 Contour Slab Thickness  */
    public static final int CONTOUR_SLAB_THICKNESS = 0x30060044;
        
    /** (3006,0045) VR=DS, VM=3 Contour Offset Vector  */
    public static final int CONTOUR_OFFSET_VECTOR = 0x30060045;
        
    /** (3006,0046) VR=IS, VM=1 Number of Contour Points  */
    public static final int NUMBER_OF_CONTOUR_POINTS = 0x30060046;
        
    /** (3006,0048) VR=IS, VM=1 Contour Number  */
    public static final int CONTOUR_NUMBER = 0x30060048;
        
    /** (3006,0049) VR=IS, VM=1-n Attached Contours  */
    public static final int ATTACHED_CONTOURS = 0x30060049;
        
    /** (3006,0050) VR=DS, VM=3-3n Contour Data  */
    public static final int CONTOUR_DATA = 0x30060050;
        
    /** (3006,0080) VR=SQ, VM=1 RT ROI Observations Sequence  */
    public static final int RT_ROI_OBSERVATIONS_SEQUENCE = 0x30060080;
        
    /** (3006,0082) VR=IS, VM=1 Observation Number  */
    public static final int OBSERVATION_NUMBER = 0x30060082;
        
    /** (3006,0084) VR=IS, VM=1 Referenced ROI Number  */
    public static final int REFERENCED_ROI_NUMBER = 0x30060084;
        
    /** (3006,0085) VR=SH, VM=1 ROI Observation Label  */
    public static final int ROI_OBSERVATION_LABEL = 0x30060085;
        
    /** (3006,0086) VR=SQ, VM=1 RT ROI Identification Code Sequence  */
    public static final int RT_ROI_IDENTIFICATION_CODE_SEQUENCE = 0x30060086;
        
    /** (3006,0088) VR=ST, VM=1 ROI Observation Description  */
    public static final int ROI_OBSERVATION_DESCRIPTION = 0x30060088;
        
    /** (3006,00A0) VR=SQ, VM=1 Related RT ROI Observations Sequence  */
    public static final int RELATED_RT_ROI_OBSERVATIONS_SEQUENCE = 0x300600A0;
        
    /** (3006,00A4) VR=CS, VM=1 RT ROI Interpreted Type  */
    public static final int RT_ROI_INTERPRETED_TYPE = 0x300600A4;
        
    /** (3006,00A6) VR=PN, VM=1 ROI Interpreter  */
    public static final int ROI_INTERPRETER = 0x300600A6;
        
    /** (3006,00B0) VR=SQ, VM=1 ROI Physical Properties Sequence  */
    public static final int ROI_PHYSICAL_PROPERTIES_SEQUENCE = 0x300600B0;
        
    /** (3006,00B2) VR=CS, VM=1 ROI Physical Property  */
    public static final int ROI_PHYSICAL_PROPERTY = 0x300600B2;
        
    /** (3006,00B4) VR=DS, VM=1 ROI Physical Property Value  */
    public static final int ROI_PHYSICAL_PROPERTY_VALUE = 0x300600B4;
        
    /** (3006,00C0) VR=SQ, VM=1 Frame of Reference Relationship Sequence  */
    public static final int FRAME_OF_REFERENCE_RELATIONSHIP_SEQUENCE = 0x300600C0;
        
    /** (3006,00C2) VR=UI, VM=1 Related Frame of Reference UID  */
    public static final int RELATED_FRAME_OF_REFERENCE_UID = 0x300600C2;
        
    /** (3006,00C4) VR=CS, VM=1 Frame of Reference Transformation Type  */
    public static final int FRAME_OF_REFERENCE_TRANSFORMATION_TYPE = 0x300600C4;
        
    /** (3006,00C6) VR=DS, VM=16 Frame of Reference Transformation Matrix  */
    public static final int FRAME_OF_REFERENCE_TRANSFORMATION_MATRIX = 0x300600C6;
        
    /** (3006,00C8) VR=LO, VM=1 Frame of Reference Transformation Comment  */
    public static final int FRAME_OF_REFERENCE_TRANSFORMATION_COMMENT = 0x300600C8;
        
    /** (3008,0010) VR=SQ, VM=1 Measured Dose Reference Sequence  */
    public static final int MEASURED_DOSE_REFERENCE_SEQUENCE = 0x30080010;
        
    /** (3008,0012) VR=ST, VM=1 Measured Dose Description  */
    public static final int MEASURED_DOSE_DESCRIPTION = 0x30080012;
        
    /** (3008,0014) VR=CS, VM=1 Measured Dose Type  */
    public static final int MEASURED_DOSE_TYPE = 0x30080014;
        
    /** (3008,0016) VR=DS, VM=1 Measured Dose Value  */
    public static final int MEASURED_DOSE_VALUE = 0x30080016;
        
    /** (3008,0020) VR=SQ, VM=1 Treatment Session Beam Sequence  */
    public static final int TREATMENT_SESSION_BEAM_SEQUENCE = 0x30080020;
        
    /** (3008,0021) VR=SQ, VM=1 Treatment Session Ion Beam Sequence  */
    public static final int TREATMENT_SESSION_ION_BEAM_SEQUENCE = 0x30080021;
        
    /** (3008,0022) VR=IS, VM=1 Current Fraction Number  */
    public static final int CURRENT_FRACTION_NUMBER = 0x30080022;
        
    /** (3008,0024) VR=DA, VM=1 Treatment Control Point Date  */
    public static final int TREATMENT_CONTROL_POINT_DATE = 0x30080024;
        
    /** (3008,0025) VR=TM, VM=1 Treatment Control Point Time  */
    public static final int TREATMENT_CONTROL_POINT_TIME = 0x30080025;
        
    /** (3008,002A) VR=CS, VM=1 Treatment Termination Status  */
    public static final int TREATMENT_TERMINATION_STATUS = 0x3008002A;
        
    /** (3008,002B) VR=SH, VM=1 Treatment Termination Code  */
    public static final int TREATMENT_TERMINATION_CODE = 0x3008002B;
        
    /** (3008,002C) VR=CS, VM=1 Treatment Verification Status  */
    public static final int TREATMENT_VERIFICATION_STATUS = 0x3008002C;
        
    /** (3008,0030) VR=SQ, VM=1 Referenced Treatment Record Sequence  */
    public static final int REFERENCED_TREATMENT_RECORD_SEQUENCE = 0x30080030;
        
    /** (3008,0032) VR=DS, VM=1 Specified Primary Meterset  */
    public static final int SPECIFIED_PRIMARY_METERSET = 0x30080032;
        
    /** (3008,0033) VR=DS, VM=1 Specified Secondary Meterset  */
    public static final int SPECIFIED_SECONDARY_METERSET = 0x30080033;
        
    /** (3008,0036) VR=DS, VM=1 Delivered Primary Meterset  */
    public static final int DELIVERED_PRIMARY_METERSET = 0x30080036;
        
    /** (3008,0037) VR=DS, VM=1 Delivered Secondary Meterset  */
    public static final int DELIVERED_SECONDARY_METERSET = 0x30080037;
        
    /** (3008,003A) VR=DS, VM=1 Specified Treatment Time  */
    public static final int SPECIFIED_TREATMENT_TIME = 0x3008003A;
        
    /** (3008,003B) VR=DS, VM=1 Delivered Treatment Time  */
    public static final int DELIVERED_TREATMENT_TIME = 0x3008003B;
        
    /** (3008,0040) VR=SQ, VM=1 Control Point Delivery Sequence  */
    public static final int CONTROL_POINT_DELIVERY_SEQUENCE = 0x30080040;
        
    /** (3008,0041) VR=SQ, VM=1 Ion Control Point Delivery Sequence  */
    public static final int ION_CONTROL_POINT_DELIVERY_SEQUENCE = 0x30080041;
        
    /** (3008,0042) VR=DS, VM=1 Specified Meterset  */
    public static final int SPECIFIED_METERSET = 0x30080042;
        
    /** (3008,0044) VR=DS, VM=1 Delivered Meterset  */
    public static final int DELIVERED_METERSET = 0x30080044;
        
    /** (3008,0045) VR=FL, VM=1 Meterset Rate Set  */
    public static final int METERSET_RATE_SET = 0x30080045;
        
    /** (3008,0046) VR=FL, VM=1 Meterset Rate Delivered  */
    public static final int METERSET_RATE_DELIVERED = 0x30080046;
        
    /** (3008,0047) VR=FL, VM=1-n Scan Spot Metersets Delivered  */
    public static final int SCAN_SPOT_METERSETS_DELIVERED = 0x30080047;
        
    /** (3008,0048) VR=DS, VM=1 Dose Rate Delivered  */
    public static final int DOSE_RATE_DELIVERED = 0x30080048;
        
    /** (3008,0050) VR=SQ, VM=1 Treatment Summary Calculated Dose Reference Sequence  */
    public static final int TREATMENT_SUMMARY_CALCULATED_DOSE_REFERENCE_SEQUENCE = 0x30080050;
        
    /** (3008,0052) VR=DS, VM=1 Cumulative Dose to Dose Reference  */
    public static final int CUMULATIVE_DOSE_TO_DOSE_REFERENCE = 0x30080052;
        
    /** (3008,0054) VR=DA, VM=1 First Treatment Date  */
    public static final int FIRST_TREATMENT_DATE = 0x30080054;
        
    /** (3008,0056) VR=DA, VM=1 Most Recent Treatment Date  */
    public static final int MOST_RECENT_TREATMENT_DATE = 0x30080056;
        
    /** (3008,005A) VR=IS, VM=1 Number of Fractions Delivered  */
    public static final int NUMBER_OF_FRACTIONS_DELIVERED = 0x3008005A;
        
    /** (3008,0060) VR=SQ, VM=1 Override Sequence  */
    public static final int OVERRIDE_SEQUENCE = 0x30080060;
        
    /** (3008,0061) VR=AT, VM=1 Parameter Sequence Pointer  */
    public static final int PARAMETER_SEQUENCE_POINTER = 0x30080061;
        
    /** (3008,0062) VR=AT, VM=1 Override Parameter Pointer  */
    public static final int OVERRIDE_PARAMETER_POINTER = 0x30080062;
        
    /** (3008,0063) VR=IS, VM=1 Parameter Item Index  */
    public static final int PARAMETER_ITEM_INDEX = 0x30080063;
        
    /** (3008,0064) VR=IS, VM=1 Measured Dose Reference Number  */
    public static final int MEASURED_DOSE_REFERENCE_NUMBER = 0x30080064;
        
    /** (3008,0065) VR=AT, VM=1 Parameter Pointer  */
    public static final int PARAMETER_POINTER = 0x30080065;
        
    /** (3008,0066) VR=ST, VM=1 Override Reason  */
    public static final int OVERRIDE_REASON = 0x30080066;
        
    /** (3008,0068) VR=SQ, VM=1 Corrected Parameter Sequence  */
    public static final int CORRECTED_PARAMETER_SEQUENCE = 0x30080068;
        
    /** (3008,006A) VR=FL, VM=1 Correction Value  */
    public static final int CORRECTION_VALUE = 0x3008006A;
        
    /** (3008,0070) VR=SQ, VM=1 Calculated Dose Reference Sequence  */
    public static final int CALCULATED_DOSE_REFERENCE_SEQUENCE = 0x30080070;
        
    /** (3008,0072) VR=IS, VM=1 Calculated Dose Reference Number  */
    public static final int CALCULATED_DOSE_REFERENCE_NUMBER = 0x30080072;
        
    /** (3008,0074) VR=ST, VM=1 Calculated Dose Reference Description  */
    public static final int CALCULATED_DOSE_REFERENCE_DESCRIPTION = 0x30080074;
        
    /** (3008,0076) VR=DS, VM=1 Calculated Dose Reference Dose Value  */
    public static final int CALCULATED_DOSE_REFERENCE_DOSE_VALUE = 0x30080076;
        
    /** (3008,0078) VR=DS, VM=1 Start Meterset  */
    public static final int START_METERSET = 0x30080078;
        
    /** (3008,007A) VR=DS, VM=1 End Meterset  */
    public static final int END_METERSET = 0x3008007A;
        
    /** (3008,0080) VR=SQ, VM=1 Referenced Measured Dose Reference Sequence  */
    public static final int REFERENCED_MEASURED_DOSE_REFERENCE_SEQUENCE = 0x30080080;
        
    /** (3008,0082) VR=IS, VM=1 Referenced Measured Dose Reference Number  */
    public static final int REFERENCED_MEASURED_DOSE_REFERENCE_NUMBER = 0x30080082;
        
    /** (3008,0090) VR=SQ, VM=1 Referenced Calculated Dose Reference Sequence  */
    public static final int REFERENCED_CALCULATED_DOSE_REFERENCE_SEQUENCE = 0x30080090;
        
    /** (3008,0092) VR=IS, VM=1 Referenced Calculated Dose Reference Number  */
    public static final int REFERENCED_CALCULATED_DOSE_REFERENCE_NUMBER = 0x30080092;
        
    /** (3008,00A0) VR=SQ, VM=1 Beam Limiting Device Leaf Pairs Sequence  */
    public static final int BEAM_LIMITING_DEVICE_LEAF_PAIRS_SEQUENCE = 0x300800A0;
        
    /** (3008,00B0) VR=SQ, VM=1 Recorded Wedge Sequence  */
    public static final int RECORDED_WEDGE_SEQUENCE = 0x300800B0;
        
    /** (3008,00C0) VR=SQ, VM=1 Recorded Compensator Sequence  */
    public static final int RECORDED_COMPENSATOR_SEQUENCE = 0x300800C0;
        
    /** (3008,00D0) VR=SQ, VM=1 Recorded Block Sequence  */
    public static final int RECORDED_BLOCK_SEQUENCE = 0x300800D0;
        
    /** (3008,00E0) VR=SQ, VM=1 Treatment Summary Measured Dose Reference Sequence  */
    public static final int TREATMENT_SUMMARY_MEASURED_DOSE_REFERENCE_SEQUENCE = 0x300800E0;
        
    /** (3008,00F0) VR=SQ, VM=1 Recorded Snout Sequence  */
    public static final int RECORDED_SNOUT_SEQUENCE = 0x300800F0;
        
    /** (3008,00F2) VR=SQ, VM=1 Recorded Range Shifter Sequence  */
    public static final int RECORDED_RANGE_SHIFTER_SEQUENCE = 0x300800F2;
        
    /** (3008,00F4) VR=SQ, VM=1 Recorded Lateral Spreading Device Sequence  */
    public static final int RECORDED_LATERAL_SPREADING_DEVICE_SEQUENCE = 0x300800F4;
        
    /** (3008,00F6) VR=SQ, VM=1 Recorded Range Modulator Sequence  */
    public static final int RECORDED_RANGE_MODULATOR_SEQUENCE = 0x300800F6;
        
    /** (3008,0100) VR=SQ, VM=1 Recorded Source Sequence  */
    public static final int RECORDED_SOURCE_SEQUENCE = 0x30080100;
        
    /** (3008,0105) VR=LO, VM=1 Source Serial Number  */
    public static final int SOURCE_SERIAL_NUMBER = 0x30080105;
        
    /** (3008,0110) VR=SQ, VM=1 Treatment Session Application Setup Sequence  */
    public static final int TREATMENT_SESSION_APPLICATION_SETUP_SEQUENCE = 0x30080110;
        
    /** (3008,0116) VR=CS, VM=1 Application Setup Check  */
    public static final int APPLICATION_SETUP_CHECK = 0x30080116;
        
    /** (3008,0120) VR=SQ, VM=1 Recorded Brachy Accessory Device Sequence  */
    public static final int RECORDED_BRACHY_ACCESSORY_DEVICE_SEQUENCE = 0x30080120;
        
    /** (3008,0122) VR=IS, VM=1 Referenced Brachy Accessory Device Number  */
    public static final int REFERENCED_BRACHY_ACCESSORY_DEVICE_NUMBER = 0x30080122;
        
    /** (3008,0130) VR=SQ, VM=1 Recorded Channel Sequence  */
    public static final int RECORDED_CHANNEL_SEQUENCE = 0x30080130;
        
    /** (3008,0132) VR=DS, VM=1 Specified Channel Total Time  */
    public static final int SPECIFIED_CHANNEL_TOTAL_TIME = 0x30080132;
        
    /** (3008,0134) VR=DS, VM=1 Delivered Channel Total Time  */
    public static final int DELIVERED_CHANNEL_TOTAL_TIME = 0x30080134;
        
    /** (3008,0136) VR=IS, VM=1 Specified Number of Pulses  */
    public static final int SPECIFIED_NUMBER_OF_PULSES = 0x30080136;
        
    /** (3008,0138) VR=IS, VM=1 Delivered Number of Pulses  */
    public static final int DELIVERED_NUMBER_OF_PULSES = 0x30080138;
        
    /** (3008,013A) VR=DS, VM=1 Specified Pulse Repetition Interval  */
    public static final int SPECIFIED_PULSE_REPETITION_INTERVAL = 0x3008013A;
        
    /** (3008,013C) VR=DS, VM=1 Delivered Pulse Repetition Interval  */
    public static final int DELIVERED_PULSE_REPETITION_INTERVAL = 0x3008013C;
        
    /** (3008,0140) VR=SQ, VM=1 Recorded Source Applicator Sequence  */
    public static final int RECORDED_SOURCE_APPLICATOR_SEQUENCE = 0x30080140;
        
    /** (3008,0142) VR=IS, VM=1 Referenced Source Applicator Number  */
    public static final int REFERENCED_SOURCE_APPLICATOR_NUMBER = 0x30080142;
        
    /** (3008,0150) VR=SQ, VM=1 Recorded Channel Shield Sequence  */
    public static final int RECORDED_CHANNEL_SHIELD_SEQUENCE = 0x30080150;
        
    /** (3008,0152) VR=IS, VM=1 Referenced Channel Shield Number  */
    public static final int REFERENCED_CHANNEL_SHIELD_NUMBER = 0x30080152;
        
    /** (3008,0160) VR=SQ, VM=1 Brachy Control Point Delivered Sequence  */
    public static final int BRACHY_CONTROL_POINT_DELIVERED_SEQUENCE = 0x30080160;
        
    /** (3008,0162) VR=DA, VM=1 Safe Position Exit Date  */
    public static final int SAFE_POSITION_EXIT_DATE = 0x30080162;
        
    /** (3008,0164) VR=TM, VM=1 Safe Position Exit Time  */
    public static final int SAFE_POSITION_EXIT_TIME = 0x30080164;
        
    /** (3008,0166) VR=DA, VM=1 Safe Position Return Date  */
    public static final int SAFE_POSITION_RETURN_DATE = 0x30080166;
        
    /** (3008,0168) VR=TM, VM=1 Safe Position Return Time  */
    public static final int SAFE_POSITION_RETURN_TIME = 0x30080168;
        
    /** (3008,0200) VR=CS, VM=1 Current Treatment Status  */
    public static final int CURRENT_TREATMENT_STATUS = 0x30080200;
        
    /** (3008,0202) VR=ST, VM=1 Treatment Status Comment  */
    public static final int TREATMENT_STATUS_COMMENT = 0x30080202;
        
    /** (3008,0220) VR=SQ, VM=1 Fraction Group Summary Sequence  */
    public static final int FRACTION_GROUP_SUMMARY_SEQUENCE = 0x30080220;
        
    /** (3008,0223) VR=IS, VM=1 Referenced Fraction Number  */
    public static final int REFERENCED_FRACTION_NUMBER = 0x30080223;
        
    /** (3008,0224) VR=CS, VM=1 Fraction Group Type  */
    public static final int FRACTION_GROUP_TYPE = 0x30080224;
        
    /** (3008,0230) VR=CS, VM=1 Beam Stopper Position  */
    public static final int BEAM_STOPPER_POSITION = 0x30080230;
        
    /** (3008,0240) VR=SQ, VM=1 Fraction Status Summary Sequence  */
    public static final int FRACTION_STATUS_SUMMARY_SEQUENCE = 0x30080240;
        
    /** (3008,0250) VR=DA, VM=1 Treatment Date  */
    public static final int TREATMENT_DATE = 0x30080250;
        
    /** (3008,0251) VR=TM, VM=1 Treatment Time  */
    public static final int TREATMENT_TIME = 0x30080251;
        
    /** (300A,0002) VR=SH, VM=1 RT Plan Label  */
    public static final int RT_PLAN_LABEL = 0x300A0002;
        
    /** (300A,0003) VR=LO, VM=1 RT Plan Name  */
    public static final int RT_PLAN_NAME = 0x300A0003;
        
    /** (300A,0004) VR=ST, VM=1 RT Plan Description  */
    public static final int RT_PLAN_DESCRIPTION = 0x300A0004;
        
    /** (300A,0006) VR=DA, VM=1 RT Plan Date  */
    public static final int RT_PLAN_DATE = 0x300A0006;
        
    /** (300A,0007) VR=TM, VM=1 RT Plan Time  */
    public static final int RT_PLAN_TIME = 0x300A0007;
        
    /** (300A,0009) VR=LO, VM=1-n Treatment Protocols  */
    public static final int TREATMENT_PROTOCOLS = 0x300A0009;
        
    /** (300A,000A) VR=CS, VM=1 Plan Intent  */
    public static final int PLAN_INTENT = 0x300A000A;
        
    /** (300A,000B) VR=LO, VM=1-n Treatment Sites  */
    public static final int TREATMENT_SITES = 0x300A000B;
        
    /** (300A,000C) VR=CS, VM=1 RT Plan Geometry  */
    public static final int RT_PLAN_GEOMETRY = 0x300A000C;
        
    /** (300A,000E) VR=ST, VM=1 Prescription Description  */
    public static final int PRESCRIPTION_DESCRIPTION = 0x300A000E;
        
    /** (300A,0010) VR=SQ, VM=1 Dose Reference Sequence  */
    public static final int DOSE_REFERENCE_SEQUENCE = 0x300A0010;
        
    /** (300A,0012) VR=IS, VM=1 Dose Reference Number  */
    public static final int DOSE_REFERENCE_NUMBER = 0x300A0012;
        
    /** (300A,0013) VR=UI, VM=1 Dose Reference UID  */
    public static final int DOSE_REFERENCE_UID = 0x300A0013;
        
    /** (300A,0014) VR=CS, VM=1 Dose Reference Structure Type  */
    public static final int DOSE_REFERENCE_STRUCTURE_TYPE = 0x300A0014;
        
    /** (300A,0015) VR=CS, VM=1 Nominal Beam Energy Unit  */
    public static final int NOMINAL_BEAM_ENERGY_UNIT = 0x300A0015;
        
    /** (300A,0016) VR=LO, VM=1 Dose Reference Description  */
    public static final int DOSE_REFERENCE_DESCRIPTION = 0x300A0016;
        
    /** (300A,0018) VR=DS, VM=3 Dose Reference Point Coordinates  */
    public static final int DOSE_REFERENCE_POINT_COORDINATES = 0x300A0018;
        
    /** (300A,001A) VR=DS, VM=1 Nominal Prior Dose  */
    public static final int NOMINAL_PRIOR_DOSE = 0x300A001A;
        
    /** (300A,0020) VR=CS, VM=1 Dose Reference Type  */
    public static final int DOSE_REFERENCE_TYPE = 0x300A0020;
        
    /** (300A,0021) VR=DS, VM=1 Constraint Weight  */
    public static final int CONSTRAINT_WEIGHT = 0x300A0021;
        
    /** (300A,0022) VR=DS, VM=1 Delivery Warning Dose  */
    public static final int DELIVERY_WARNING_DOSE = 0x300A0022;
        
    /** (300A,0023) VR=DS, VM=1 Delivery Maximum Dose  */
    public static final int DELIVERY_MAXIMUM_DOSE = 0x300A0023;
        
    /** (300A,0025) VR=DS, VM=1 Target Minimum Dose  */
    public static final int TARGET_MINIMUM_DOSE = 0x300A0025;
        
    /** (300A,0026) VR=DS, VM=1 Target Prescription Dose  */
    public static final int TARGET_PRESCRIPTION_DOSE = 0x300A0026;
        
    /** (300A,0027) VR=DS, VM=1 Target Maximum Dose  */
    public static final int TARGET_MAXIMUM_DOSE = 0x300A0027;
        
    /** (300A,0028) VR=DS, VM=1 Target Underdose Volume Fraction  */
    public static final int TARGET_UNDERDOSE_VOLUME_FRACTION = 0x300A0028;
        
    /** (300A,002A) VR=DS, VM=1 Organ at Risk Full-volume Dose  */
    public static final int ORGAN_AT_RISK_FULL_VOLUME_DOSE = 0x300A002A;
        
    /** (300A,002B) VR=DS, VM=1 Organ at Risk Limit Dose  */
    public static final int ORGAN_AT_RISK_LIMIT_DOSE = 0x300A002B;
        
    /** (300A,002C) VR=DS, VM=1 Organ at Risk Maximum Dose  */
    public static final int ORGAN_AT_RISK_MAXIMUM_DOSE = 0x300A002C;
        
    /** (300A,002D) VR=DS, VM=1 Organ at Risk Overdose Volume Fraction  */
    public static final int ORGAN_AT_RISK_OVERDOSE_VOLUME_FRACTION = 0x300A002D;
        
    /** (300A,0040) VR=SQ, VM=1 Tolerance Table Sequence  */
    public static final int TOLERANCE_TABLE_SEQUENCE = 0x300A0040;
        
    /** (300A,0042) VR=IS, VM=1 Tolerance Table Number  */
    public static final int TOLERANCE_TABLE_NUMBER = 0x300A0042;
        
    /** (300A,0043) VR=SH, VM=1 Tolerance Table Label  */
    public static final int TOLERANCE_TABLE_LABEL = 0x300A0043;
        
    /** (300A,0044) VR=DS, VM=1 Gantry Angle Tolerance  */
    public static final int GANTRY_ANGLE_TOLERANCE = 0x300A0044;
        
    /** (300A,0046) VR=DS, VM=1 Beam Limiting Device Angle Tolerance  */
    public static final int BEAM_LIMITING_DEVICE_ANGLE_TOLERANCE = 0x300A0046;
        
    /** (300A,0048) VR=SQ, VM=1 Beam Limiting Device Tolerance Sequence  */
    public static final int BEAM_LIMITING_DEVICE_TOLERANCE_SEQUENCE = 0x300A0048;
        
    /** (300A,004A) VR=DS, VM=1 Beam Limiting Device Position Tolerance  */
    public static final int BEAM_LIMITING_DEVICE_POSITION_TOLERANCE = 0x300A004A;
        
    /** (300A,004B) VR=FL, VM=1 Snout Position Tolerance  */
    public static final int SNOUT_POSITION_TOLERANCE = 0x300A004B;
        
    /** (300A,004C) VR=DS, VM=1 Patient Support Angle Tolerance  */
    public static final int PATIENT_SUPPORT_ANGLE_TOLERANCE = 0x300A004C;
        
    /** (300A,004E) VR=DS, VM=1 Table Top Eccentric Angle Tolerance  */
    public static final int TABLE_TOP_ECCENTRIC_ANGLE_TOLERANCE = 0x300A004E;
        
    /** (300A,004F) VR=FL, VM=1 Table Top Pitch Angle Tolerance  */
    public static final int TABLE_TOP_PITCH_ANGLE_TOLERANCE = 0x300A004F;
        
    /** (300A,0050) VR=FL, VM=1 Table Top Roll Angle Tolerance  */
    public static final int TABLE_TOP_ROLL_ANGLE_TOLERANCE = 0x300A0050;
        
    /** (300A,0051) VR=DS, VM=1 Table Top Vertical Position Tolerance  */
    public static final int TABLE_TOP_VERTICAL_POSITION_TOLERANCE = 0x300A0051;
        
    /** (300A,0052) VR=DS, VM=1 Table Top Longitudinal Position Tolerance  */
    public static final int TABLE_TOP_LONGITUDINAL_POSITION_TOLERANCE = 0x300A0052;
        
    /** (300A,0053) VR=DS, VM=1 Table Top Lateral Position Tolerance  */
    public static final int TABLE_TOP_LATERAL_POSITION_TOLERANCE = 0x300A0053;
        
    /** (300A,0055) VR=CS, VM=1 RT Plan Relationship  */
    public static final int RT_PLAN_RELATIONSHIP = 0x300A0055;
        
    /** (300A,0070) VR=SQ, VM=1 Fraction Group Sequence  */
    public static final int FRACTION_GROUP_SEQUENCE = 0x300A0070;
        
    /** (300A,0071) VR=IS, VM=1 Fraction Group Number  */
    public static final int FRACTION_GROUP_NUMBER = 0x300A0071;
        
    /** (300A,0072) VR=LO, VM=1 Fraction Group Description  */
    public static final int FRACTION_GROUP_DESCRIPTION = 0x300A0072;
        
    /** (300A,0078) VR=IS, VM=1 Number of Fractions Planned  */
    public static final int NUMBER_OF_FRACTIONS_PLANNED = 0x300A0078;
        
    /** (300A,0079) VR=IS, VM=1 Number of Fraction Pattern Digits Per Day  */
    public static final int NUMBER_OF_FRACTION_PATTERN_DIGITS_PER_DAY = 0x300A0079;
        
    /** (300A,007A) VR=IS, VM=1 Repeat Fraction Cycle Length  */
    public static final int REPEAT_FRACTION_CYCLE_LENGTH = 0x300A007A;
        
    /** (300A,007B) VR=LT, VM=1 Fraction Pattern  */
    public static final int FRACTION_PATTERN = 0x300A007B;
        
    /** (300A,0080) VR=IS, VM=1 Number of Beams  */
    public static final int NUMBER_OF_BEAMS = 0x300A0080;
        
    /** (300A,0082) VR=DS, VM=3 Beam Dose Specification Point  */
    public static final int BEAM_DOSE_SPECIFICATION_POINT = 0x300A0082;
        
    /** (300A,0084) VR=DS, VM=1 Beam Dose  */
    public static final int BEAM_DOSE = 0x300A0084;
        
    /** (300A,0086) VR=DS, VM=1 Beam Meterset  */
    public static final int BEAM_METERSET = 0x300A0086;
        
    /** (300A,0088) VR=FL, VM=1 Beam Dose Point Depth  */
    public static final int BEAM_DOSE_POINT_DEPTH = 0x300A0088;
        
    /** (300A,0089) VR=FL, VM=1 Beam Dose Point Equivalent Depth  */
    public static final int BEAM_DOSE_POINT_EQUIVALENT_DEPTH = 0x300A0089;
        
    /** (300A,008A) VR=FL, VM=1 Beam Dose Point SSD  */
    public static final int BEAM_DOSE_POINT_SSD = 0x300A008A;
        
    /** (300A,00A0) VR=IS, VM=1 Number of Brachy Application Setups  */
    public static final int NUMBER_OF_BRACHY_APPLICATION_SETUPS = 0x300A00A0;
        
    /** (300A,00A2) VR=DS, VM=3 Brachy Application Setup Dose Specification Point  */
    public static final int BRACHY_APPLICATION_SETUP_DOSE_SPECIFICATION_POINT = 0x300A00A2;
        
    /** (300A,00A4) VR=DS, VM=1 Brachy Application Setup Dose  */
    public static final int BRACHY_APPLICATION_SETUP_DOSE = 0x300A00A4;
        
    /** (300A,00B0) VR=SQ, VM=1 Beam Sequence  */
    public static final int BEAM_SEQUENCE = 0x300A00B0;
        
    /** (300A,00B2) VR=SH, VM=1 Treatment Machine Name  */
    public static final int TREATMENT_MACHINE_NAME = 0x300A00B2;
        
    /** (300A,00B3) VR=CS, VM=1 Primary Dosimeter Unit  */
    public static final int PRIMARY_DOSIMETER_UNIT = 0x300A00B3;
        
    /** (300A,00B4) VR=DS, VM=1 Source-Axis Distance  */
    public static final int SOURCE_AXIS_DISTANCE = 0x300A00B4;
        
    /** (300A,00B6) VR=SQ, VM=1 Beam Limiting Device Sequence  */
    public static final int BEAM_LIMITING_DEVICE_SEQUENCE = 0x300A00B6;
        
    /** (300A,00B8) VR=CS, VM=1 RT Beam Limiting Device Type  */
    public static final int RT_BEAM_LIMITING_DEVICE_TYPE = 0x300A00B8;
        
    /** (300A,00BA) VR=DS, VM=1 Source to Beam Limiting Device Distance  */
    public static final int SOURCE_TO_BEAM_LIMITING_DEVICE_DISTANCE = 0x300A00BA;
        
    /** (300A,00BB) VR=FL, VM=1 Isocenter to Beam Limiting Device Distance  */
    public static final int ISOCENTER_TO_BEAM_LIMITING_DEVICE_DISTANCE = 0x300A00BB;
        
    /** (300A,00BC) VR=IS, VM=1 Number of Leaf/Jaw Pairs  */
    public static final int NUMBER_OF_LEAF_JAW_PAIRS = 0x300A00BC;
        
    /** (300A,00BE) VR=DS, VM=3-n Leaf Position Boundaries  */
    public static final int LEAF_POSITION_BOUNDARIES = 0x300A00BE;
        
    /** (300A,00C0) VR=IS, VM=1 Beam Number  */
    public static final int BEAM_NUMBER = 0x300A00C0;
        
    /** (300A,00C2) VR=LO, VM=1 Beam Name  */
    public static final int BEAM_NAME = 0x300A00C2;
        
    /** (300A,00C3) VR=ST, VM=1 Beam Description  */
    public static final int BEAM_DESCRIPTION = 0x300A00C3;
        
    /** (300A,00C4) VR=CS, VM=1 Beam Type  */
    public static final int BEAM_TYPE = 0x300A00C4;
        
    /** (300A,00C6) VR=CS, VM=1 Radiation Type  */
    public static final int RADIATION_TYPE = 0x300A00C6;
        
    /** (300A,00C7) VR=CS, VM=1 High-Dose Technique Type  */
    public static final int HIGH_DOSE_TECHNIQUE_TYPE = 0x300A00C7;
        
    /** (300A,00C8) VR=IS, VM=1 Reference Image Number  */
    public static final int REFERENCE_IMAGE_NUMBER = 0x300A00C8;
        
    /** (300A,00CA) VR=SQ, VM=1 Planned Verification Image Sequence  */
    public static final int PLANNED_VERIFICATION_IMAGE_SEQUENCE = 0x300A00CA;
        
    /** (300A,00CC) VR=LO, VM=1-n Imaging Device-Specific Acquisition Parameters  */
    public static final int IMAGING_DEVICE_SPECIFIC_ACQUISITION_PARAMETERS = 0x300A00CC;
        
    /** (300A,00CE) VR=CS, VM=1 Treatment Delivery Type  */
    public static final int TREATMENT_DELIVERY_TYPE = 0x300A00CE;
        
    /** (300A,00D0) VR=IS, VM=1 Number of Wedges  */
    public static final int NUMBER_OF_WEDGES = 0x300A00D0;
        
    /** (300A,00D1) VR=SQ, VM=1 Wedge Sequence  */
    public static final int WEDGE_SEQUENCE = 0x300A00D1;
        
    /** (300A,00D2) VR=IS, VM=1 Wedge Number  */
    public static final int WEDGE_NUMBER = 0x300A00D2;
        
    /** (300A,00D3) VR=CS, VM=1 Wedge Type  */
    public static final int WEDGE_TYPE = 0x300A00D3;
        
    /** (300A,00D4) VR=SH, VM=1 Wedge ID  */
    public static final int WEDGE_ID = 0x300A00D4;
        
    /** (300A,00D5) VR=IS, VM=1 Wedge Angle  */
    public static final int WEDGE_ANGLE = 0x300A00D5;
        
    /** (300A,00D6) VR=DS, VM=1 Wedge Factor  */
    public static final int WEDGE_FACTOR = 0x300A00D6;
        
    /** (300A,00D7) VR=FL, VM=1 Total Wedge Tray Water-Equivalent Thickness  */
    public static final int TOTAL_WEDGE_TRAY_WATER_EQUIVALENT_THICKNESS = 0x300A00D7;
        
    /** (300A,00D8) VR=DS, VM=1 Wedge Orientation  */
    public static final int WEDGE_ORIENTATION = 0x300A00D8;
        
    /** (300A,00D9) VR=FL, VM=1 Isocenter to Wedge Tray Distance  */
    public static final int ISOCENTER_TO_WEDGE_TRAY_DISTANCE = 0x300A00D9;
        
    /** (300A,00DA) VR=DS, VM=1 Source to Wedge Tray Distance  */
    public static final int SOURCE_TO_WEDGE_TRAY_DISTANCE = 0x300A00DA;
        
    /** (300A,00DB) VR=FL, VM=1 Wedge Thin Edge Position  */
    public static final int WEDGE_THIN_EDGE_POSITION = 0x300A00DB;
        
    /** (300A,00DC) VR=SH, VM=1 Bolus ID  */
    public static final int BOLUS_ID = 0x300A00DC;
        
    /** (300A,00DD) VR=ST, VM=1 Bolus Description  */
    public static final int BOLUS_DESCRIPTION = 0x300A00DD;
        
    /** (300A,00E0) VR=IS, VM=1 Number of Compensators  */
    public static final int NUMBER_OF_COMPENSATORS = 0x300A00E0;
        
    /** (300A,00E1) VR=SH, VM=1 Material ID  */
    public static final int MATERIAL_ID = 0x300A00E1;
        
    /** (300A,00E2) VR=DS, VM=1 Total Compensator Tray Factor  */
    public static final int TOTAL_COMPENSATOR_TRAY_FACTOR = 0x300A00E2;
        
    /** (300A,00E3) VR=SQ, VM=1 Compensator Sequence  */
    public static final int COMPENSATOR_SEQUENCE = 0x300A00E3;
        
    /** (300A,00E4) VR=IS, VM=1 Compensator Number  */
    public static final int COMPENSATOR_NUMBER = 0x300A00E4;
        
    /** (300A,00E5) VR=SH, VM=1 Compensator ID  */
    public static final int COMPENSATOR_ID = 0x300A00E5;
        
    /** (300A,00E6) VR=DS, VM=1 Source to Compensator Tray Distance  */
    public static final int SOURCE_TO_COMPENSATOR_TRAY_DISTANCE = 0x300A00E6;
        
    /** (300A,00E7) VR=IS, VM=1 Compensator Rows  */
    public static final int COMPENSATOR_ROWS = 0x300A00E7;
        
    /** (300A,00E8) VR=IS, VM=1 Compensator Columns  */
    public static final int COMPENSATOR_COLUMNS = 0x300A00E8;
        
    /** (300A,00E9) VR=DS, VM=2 Compensator Pixel Spacing  */
    public static final int COMPENSATOR_PIXEL_SPACING = 0x300A00E9;
        
    /** (300A,00EA) VR=DS, VM=2 Compensator Position  */
    public static final int COMPENSATOR_POSITION = 0x300A00EA;
        
    /** (300A,00EB) VR=DS, VM=1-n Compensator Transmission Data  */
    public static final int COMPENSATOR_TRANSMISSION_DATA = 0x300A00EB;
        
    /** (300A,00EC) VR=DS, VM=1-n Compensator Thickness Data  */
    public static final int COMPENSATOR_THICKNESS_DATA = 0x300A00EC;
        
    /** (300A,00ED) VR=IS, VM=1 Number of Boli  */
    public static final int NUMBER_OF_BOLI = 0x300A00ED;
        
    /** (300A,00EE) VR=CS, VM=1 Compensator Type  */
    public static final int COMPENSATOR_TYPE = 0x300A00EE;
        
    /** (300A,00F0) VR=IS, VM=1 Number of Blocks  */
    public static final int NUMBER_OF_BLOCKS = 0x300A00F0;
        
    /** (300A,00F2) VR=DS, VM=1 Total Block Tray Factor  */
    public static final int TOTAL_BLOCK_TRAY_FACTOR = 0x300A00F2;
        
    /** (300A,00F3) VR=FL, VM=1 Total Block Tray Water-Equivalent Thickness  */
    public static final int TOTAL_BLOCK_TRAY_WATER_EQUIVALENT_THICKNESS = 0x300A00F3;
        
    /** (300A,00F4) VR=SQ, VM=1 Block Sequence  */
    public static final int BLOCK_SEQUENCE = 0x300A00F4;
        
    /** (300A,00F5) VR=SH, VM=1 Block Tray ID  */
    public static final int BLOCK_TRAY_ID = 0x300A00F5;
        
    /** (300A,00F6) VR=DS, VM=1 Source to Block Tray Distance  */
    public static final int SOURCE_TO_BLOCK_TRAY_DISTANCE = 0x300A00F6;
        
    /** (300A,00F7) VR=FL, VM=1 Isocenter to Block Tray Distance  */
    public static final int ISOCENTER_TO_BLOCK_TRAY_DISTANCE = 0x300A00F7;
        
    /** (300A,00F8) VR=CS, VM=1 Block Type  */
    public static final int BLOCK_TYPE = 0x300A00F8;
        
    /** (300A,00F9) VR=LO, VM=1 Accessory Code  */
    public static final int ACCESSORY_CODE = 0x300A00F9;
        
    /** (300A,00FA) VR=CS, VM=1 Block Divergence  */
    public static final int BLOCK_DIVERGENCE = 0x300A00FA;
        
    /** (300A,00FB) VR=CS, VM=1 Block Mounting Position  */
    public static final int BLOCK_MOUNTING_POSITION = 0x300A00FB;
        
    /** (300A,00FC) VR=IS, VM=1 Block Number  */
    public static final int BLOCK_NUMBER = 0x300A00FC;
        
    /** (300A,00FE) VR=LO, VM=1 Block Name  */
    public static final int BLOCK_NAME = 0x300A00FE;
        
    /** (300A,0100) VR=DS, VM=1 Block Thickness  */
    public static final int BLOCK_THICKNESS = 0x300A0100;
        
    /** (300A,0102) VR=DS, VM=1 Block Transmission  */
    public static final int BLOCK_TRANSMISSION = 0x300A0102;
        
    /** (300A,0104) VR=IS, VM=1 Block Number of Points  */
    public static final int BLOCK_NUMBER_OF_POINTS = 0x300A0104;
        
    /** (300A,0106) VR=DS, VM=2-2n Block Data  */
    public static final int BLOCK_DATA = 0x300A0106;
        
    /** (300A,0107) VR=SQ, VM=1 Applicator Sequence  */
    public static final int APPLICATOR_SEQUENCE = 0x300A0107;
        
    /** (300A,0108) VR=SH, VM=1 Applicator ID  */
    public static final int APPLICATOR_ID = 0x300A0108;
        
    /** (300A,0109) VR=CS, VM=1 Applicator Type  */
    public static final int APPLICATOR_TYPE = 0x300A0109;
        
    /** (300A,010A) VR=LO, VM=1 Applicator Description  */
    public static final int APPLICATOR_DESCRIPTION = 0x300A010A;
        
    /** (300A,010C) VR=DS, VM=1 Cumulative Dose Reference Coefficient  */
    public static final int CUMULATIVE_DOSE_REFERENCE_COEFFICIENT = 0x300A010C;
        
    /** (300A,010E) VR=DS, VM=1 Final Cumulative Meterset Weight  */
    public static final int FINAL_CUMULATIVE_METERSET_WEIGHT = 0x300A010E;
        
    /** (300A,0110) VR=IS, VM=1 Number of Control Points  */
    public static final int NUMBER_OF_CONTROL_POINTS = 0x300A0110;
        
    /** (300A,0111) VR=SQ, VM=1 Control Point Sequence  */
    public static final int CONTROL_POINT_SEQUENCE = 0x300A0111;
        
    /** (300A,0112) VR=IS, VM=1 Control Point Index  */
    public static final int CONTROL_POINT_INDEX = 0x300A0112;
        
    /** (300A,0114) VR=DS, VM=1 Nominal Beam Energy  */
    public static final int NOMINAL_BEAM_ENERGY = 0x300A0114;
        
    /** (300A,0115) VR=DS, VM=1 Dose Rate Set  */
    public static final int DOSE_RATE_SET = 0x300A0115;
        
    /** (300A,0116) VR=SQ, VM=1 Wedge Position Sequence  */
    public static final int WEDGE_POSITION_SEQUENCE = 0x300A0116;
        
    /** (300A,0118) VR=CS, VM=1 Wedge Position  */
    public static final int WEDGE_POSITION = 0x300A0118;
        
    /** (300A,011A) VR=SQ, VM=1 Beam Limiting Device Position Sequence  */
    public static final int BEAM_LIMITING_DEVICE_POSITION_SEQUENCE = 0x300A011A;
        
    /** (300A,011C) VR=DS, VM=2-2n Leaf/Jaw Positions  */
    public static final int LEAF_JAW_POSITIONS = 0x300A011C;
        
    /** (300A,011E) VR=DS, VM=1 Gantry Angle  */
    public static final int GANTRY_ANGLE = 0x300A011E;
        
    /** (300A,011F) VR=CS, VM=1 Gantry Rotation Direction  */
    public static final int GANTRY_ROTATION_DIRECTION = 0x300A011F;
        
    /** (300A,0120) VR=DS, VM=1 Beam Limiting Device Angle  */
    public static final int BEAM_LIMITING_DEVICE_ANGLE = 0x300A0120;
        
    /** (300A,0121) VR=CS, VM=1 Beam Limiting Device Rotation Direction  */
    public static final int BEAM_LIMITING_DEVICE_ROTATION_DIRECTION = 0x300A0121;
        
    /** (300A,0122) VR=DS, VM=1 Patient Support Angle  */
    public static final int PATIENT_SUPPORT_ANGLE = 0x300A0122;
        
    /** (300A,0123) VR=CS, VM=1 Patient Support Rotation Direction  */
    public static final int PATIENT_SUPPORT_ROTATION_DIRECTION = 0x300A0123;
        
    /** (300A,0124) VR=DS, VM=1 Table Top Eccentric Axis Distance  */
    public static final int TABLE_TOP_ECCENTRIC_AXIS_DISTANCE = 0x300A0124;
        
    /** (300A,0125) VR=DS, VM=1 Table Top Eccentric Angle  */
    public static final int TABLE_TOP_ECCENTRIC_ANGLE = 0x300A0125;
        
    /** (300A,0126) VR=CS, VM=1 Table Top Eccentric Rotation Direction  */
    public static final int TABLE_TOP_ECCENTRIC_ROTATION_DIRECTION = 0x300A0126;
        
    /** (300A,0128) VR=DS, VM=1 Table Top Vertical Position  */
    public static final int TABLE_TOP_VERTICAL_POSITION = 0x300A0128;
        
    /** (300A,0129) VR=DS, VM=1 Table Top Longitudinal Position  */
    public static final int TABLE_TOP_LONGITUDINAL_POSITION = 0x300A0129;
        
    /** (300A,012A) VR=DS, VM=1 Table Top Lateral Position  */
    public static final int TABLE_TOP_LATERAL_POSITION = 0x300A012A;
        
    /** (300A,012C) VR=DS, VM=3 Isocenter Position  */
    public static final int ISOCENTER_POSITION = 0x300A012C;
        
    /** (300A,012E) VR=DS, VM=3 Surface Entry Point  */
    public static final int SURFACE_ENTRY_POINT = 0x300A012E;
        
    /** (300A,0130) VR=DS, VM=1 Source to Surface Distance  */
    public static final int SOURCE_TO_SURFACE_DISTANCE = 0x300A0130;
        
    /** (300A,0134) VR=DS, VM=1 Cumulative Meterset Weight  */
    public static final int CUMULATIVE_METERSET_WEIGHT = 0x300A0134;
        
    /** (300A,0140) VR=FL, VM=1 Table Top Pitch Angle  */
    public static final int TABLE_TOP_PITCH_ANGLE = 0x300A0140;
        
    /** (300A,0142) VR=CS, VM=1 Table Top Pitch Rotation Direction  */
    public static final int TABLE_TOP_PITCH_ROTATION_DIRECTION = 0x300A0142;
        
    /** (300A,0144) VR=FL, VM=1 Table Top Roll Angle  */
    public static final int TABLE_TOP_ROLL_ANGLE = 0x300A0144;
        
    /** (300A,0146) VR=CS, VM=1 Table Top Roll Rotation Direction  */
    public static final int TABLE_TOP_ROLL_ROTATION_DIRECTION = 0x300A0146;
        
    /** (300A,0148) VR=FL, VM=1 Head Fixation Angle  */
    public static final int HEAD_FIXATION_ANGLE = 0x300A0148;
        
    /** (300A,014A) VR=FL, VM=1 Gantry Pitch Angle  */
    public static final int GANTRY_PITCH_ANGLE = 0x300A014A;
        
    /** (300A,014C) VR=CS, VM=1 Gantry Pitch Rotation Direction  */
    public static final int GANTRY_PITCH_ROTATION_DIRECTION = 0x300A014C;
        
    /** (300A,014E) VR=FL, VM=1 Gantry Pitch Angle Tolerance  */
    public static final int GANTRY_PITCH_ANGLE_TOLERANCE = 0x300A014E;
        
    /** (300A,0180) VR=SQ, VM=1 Patient Setup Sequence  */
    public static final int PATIENT_SETUP_SEQUENCE = 0x300A0180;
        
    /** (300A,0182) VR=IS, VM=1 Patient Setup Number  */
    public static final int PATIENT_SETUP_NUMBER = 0x300A0182;
        
    /** (300A,0183) VR=LO, VM=1 Patient Setup Label  */
    public static final int PATIENT_SETUP_LABEL = 0x300A0183;
        
    /** (300A,0184) VR=LO, VM=1 Patient Additional Position  */
    public static final int PATIENT_ADDITIONAL_POSITION = 0x300A0184;
        
    /** (300A,0190) VR=SQ, VM=1 Fixation Device Sequence  */
    public static final int FIXATION_DEVICE_SEQUENCE = 0x300A0190;
        
    /** (300A,0192) VR=CS, VM=1 Fixation Device Type  */
    public static final int FIXATION_DEVICE_TYPE = 0x300A0192;
        
    /** (300A,0194) VR=SH, VM=1 Fixation Device Label  */
    public static final int FIXATION_DEVICE_LABEL = 0x300A0194;
        
    /** (300A,0196) VR=ST, VM=1 Fixation Device Description  */
    public static final int FIXATION_DEVICE_DESCRIPTION = 0x300A0196;
        
    /** (300A,0198) VR=SH, VM=1 Fixation Device Position  */
    public static final int FIXATION_DEVICE_POSITION = 0x300A0198;
        
    /** (300A,0199) VR=FL, VM=1 Fixation Device Pitch Angle  */
    public static final int FIXATION_DEVICE_PITCH_ANGLE = 0x300A0199;
        
    /** (300A,019A) VR=FL, VM=1 Fixation Device Roll Angle  */
    public static final int FIXATION_DEVICE_ROLL_ANGLE = 0x300A019A;
        
    /** (300A,01A0) VR=SQ, VM=1 Shielding Device Sequence  */
    public static final int SHIELDING_DEVICE_SEQUENCE = 0x300A01A0;
        
    /** (300A,01A2) VR=CS, VM=1 Shielding Device Type  */
    public static final int SHIELDING_DEVICE_TYPE = 0x300A01A2;
        
    /** (300A,01A4) VR=SH, VM=1 Shielding Device Label  */
    public static final int SHIELDING_DEVICE_LABEL = 0x300A01A4;
        
    /** (300A,01A6) VR=ST, VM=1 Shielding Device Description  */
    public static final int SHIELDING_DEVICE_DESCRIPTION = 0x300A01A6;
        
    /** (300A,01A8) VR=SH, VM=1 Shielding Device Position  */
    public static final int SHIELDING_DEVICE_POSITION = 0x300A01A8;
        
    /** (300A,01B0) VR=CS, VM=1 Setup Technique  */
    public static final int SETUP_TECHNIQUE = 0x300A01B0;
        
    /** (300A,01B2) VR=ST, VM=1 Setup Technique Description  */
    public static final int SETUP_TECHNIQUE_DESCRIPTION = 0x300A01B2;
        
    /** (300A,01B4) VR=SQ, VM=1 Setup Device Sequence  */
    public static final int SETUP_DEVICE_SEQUENCE = 0x300A01B4;
        
    /** (300A,01B6) VR=CS, VM=1 Setup Device Type  */
    public static final int SETUP_DEVICE_TYPE = 0x300A01B6;
        
    /** (300A,01B8) VR=SH, VM=1 Setup Device Label  */
    public static final int SETUP_DEVICE_LABEL = 0x300A01B8;
        
    /** (300A,01BA) VR=ST, VM=1 Setup Device Description  */
    public static final int SETUP_DEVICE_DESCRIPTION = 0x300A01BA;
        
    /** (300A,01BC) VR=DS, VM=1 Setup Device Parameter  */
    public static final int SETUP_DEVICE_PARAMETER = 0x300A01BC;
        
    /** (300A,01D0) VR=ST, VM=1 Setup Reference Description  */
    public static final int SETUP_REFERENCE_DESCRIPTION = 0x300A01D0;
        
    /** (300A,01D2) VR=DS, VM=1 Table Top Vertical Setup Displacement  */
    public static final int TABLE_TOP_VERTICAL_SETUP_DISPLACEMENT = 0x300A01D2;
        
    /** (300A,01D4) VR=DS, VM=1 Table Top Longitudinal Setup Displacement  */
    public static final int TABLE_TOP_LONGITUDINAL_SETUP_DISPLACEMENT = 0x300A01D4;
        
    /** (300A,01D6) VR=DS, VM=1 Table Top Lateral Setup Displacement  */
    public static final int TABLE_TOP_LATERAL_SETUP_DISPLACEMENT = 0x300A01D6;
        
    /** (300A,0200) VR=CS, VM=1 Brachy Treatment Technique  */
    public static final int BRACHY_TREATMENT_TECHNIQUE = 0x300A0200;
        
    /** (300A,0202) VR=CS, VM=1 Brachy Treatment Type  */
    public static final int BRACHY_TREATMENT_TYPE = 0x300A0202;
        
    /** (300A,0206) VR=SQ, VM=1 Treatment Machine Sequence  */
    public static final int TREATMENT_MACHINE_SEQUENCE = 0x300A0206;
        
    /** (300A,0210) VR=SQ, VM=1 Source Sequence  */
    public static final int SOURCE_SEQUENCE = 0x300A0210;
        
    /** (300A,0212) VR=IS, VM=1 Source Number  */
    public static final int SOURCE_NUMBER = 0x300A0212;
        
    /** (300A,0214) VR=CS, VM=1 Source Type  */
    public static final int SOURCE_TYPE = 0x300A0214;
        
    /** (300A,0216) VR=LO, VM=1 Source Manufacturer  */
    public static final int SOURCE_MANUFACTURER = 0x300A0216;
        
    /** (300A,0218) VR=DS, VM=1 Active Source Diameter  */
    public static final int ACTIVE_SOURCE_DIAMETER = 0x300A0218;
        
    /** (300A,021A) VR=DS, VM=1 Active Source Length  */
    public static final int ACTIVE_SOURCE_LENGTH = 0x300A021A;
        
    /** (300A,0222) VR=DS, VM=1 Source Encapsulation Nominal Thickness  */
    public static final int SOURCE_ENCAPSULATION_NOMINAL_THICKNESS = 0x300A0222;
        
    /** (300A,0224) VR=DS, VM=1 Source Encapsulation Nominal Transmission  */
    public static final int SOURCE_ENCAPSULATION_NOMINAL_TRANSMISSION = 0x300A0224;
        
    /** (300A,0226) VR=LO, VM=1 Source Isotope Name  */
    public static final int SOURCE_ISOTOPE_NAME = 0x300A0226;
        
    /** (300A,0228) VR=DS, VM=1 Source Isotope Half Life  */
    public static final int SOURCE_ISOTOPE_HALF_LIFE = 0x300A0228;
        
    /** (300A,0229) VR=CS, VM=1 Source Strength Units  */
    public static final int SOURCE_STRENGTH_UNITS = 0x300A0229;
        
    /** (300A,022A) VR=DS, VM=1 Reference Air Kerma Rate  */
    public static final int REFERENCE_AIR_KERMA_RATE = 0x300A022A;
        
    /** (300A,022B) VR=DS, VM=1 Source Strength  */
    public static final int SOURCE_STRENGTH = 0x300A022B;
        
    /** (300A,022C) VR=DA, VM=1 Source Strength Reference Date  */
    public static final int SOURCE_STRENGTH_REFERENCE_DATE = 0x300A022C;
        
    /** (300A,022E) VR=TM, VM=1 Source Strength Reference Time  */
    public static final int SOURCE_STRENGTH_REFERENCE_TIME = 0x300A022E;
        
    /** (300A,0230) VR=SQ, VM=1 Application Setup Sequence  */
    public static final int APPLICATION_SETUP_SEQUENCE = 0x300A0230;
        
    /** (300A,0232) VR=CS, VM=1 Application Setup Type  */
    public static final int APPLICATION_SETUP_TYPE = 0x300A0232;
        
    /** (300A,0234) VR=IS, VM=1 Application Setup Number  */
    public static final int APPLICATION_SETUP_NUMBER = 0x300A0234;
        
    /** (300A,0236) VR=LO, VM=1 Application Setup Name  */
    public static final int APPLICATION_SETUP_NAME = 0x300A0236;
        
    /** (300A,0238) VR=LO, VM=1 Application Setup Manufacturer  */
    public static final int APPLICATION_SETUP_MANUFACTURER = 0x300A0238;
        
    /** (300A,0240) VR=IS, VM=1 Template Number  */
    public static final int TEMPLATE_NUMBER = 0x300A0240;
        
    /** (300A,0242) VR=SH, VM=1 Template Type  */
    public static final int TEMPLATE_TYPE = 0x300A0242;
        
    /** (300A,0244) VR=LO, VM=1 Template Name  */
    public static final int TEMPLATE_NAME = 0x300A0244;
        
    /** (300A,0250) VR=DS, VM=1 Total Reference Air Kerma  */
    public static final int TOTAL_REFERENCE_AIR_KERMA = 0x300A0250;
        
    /** (300A,0260) VR=SQ, VM=1 Brachy Accessory Device Sequence  */
    public static final int BRACHY_ACCESSORY_DEVICE_SEQUENCE = 0x300A0260;
        
    /** (300A,0262) VR=IS, VM=1 Brachy Accessory Device Number  */
    public static final int BRACHY_ACCESSORY_DEVICE_NUMBER = 0x300A0262;
        
    /** (300A,0263) VR=SH, VM=1 Brachy Accessory Device ID  */
    public static final int BRACHY_ACCESSORY_DEVICE_ID = 0x300A0263;
        
    /** (300A,0264) VR=CS, VM=1 Brachy Accessory Device Type  */
    public static final int BRACHY_ACCESSORY_DEVICE_TYPE = 0x300A0264;
        
    /** (300A,0266) VR=LO, VM=1 Brachy Accessory Device Name  */
    public static final int BRACHY_ACCESSORY_DEVICE_NAME = 0x300A0266;
        
    /** (300A,026A) VR=DS, VM=1 Brachy Accessory Device Nominal Thickness  */
    public static final int BRACHY_ACCESSORY_DEVICE_NOMINAL_THICKNESS = 0x300A026A;
        
    /** (300A,026C) VR=DS, VM=1 Brachy Accessory Device Nominal Transmission  */
    public static final int BRACHY_ACCESSORY_DEVICE_NOMINAL_TRANSMISSION = 0x300A026C;
        
    /** (300A,0280) VR=SQ, VM=1 Channel Sequence  */
    public static final int CHANNEL_SEQUENCE = 0x300A0280;
        
    /** (300A,0282) VR=IS, VM=1 Channel Number  */
    public static final int CHANNEL_NUMBER = 0x300A0282;
        
    /** (300A,0284) VR=DS, VM=1 Channel Length  */
    public static final int CHANNEL_LENGTH = 0x300A0284;
        
    /** (300A,0286) VR=DS, VM=1 Channel Total Time  */
    public static final int CHANNEL_TOTAL_TIME = 0x300A0286;
        
    /** (300A,0288) VR=CS, VM=1 Source Movement Type  */
    public static final int SOURCE_MOVEMENT_TYPE = 0x300A0288;
        
    /** (300A,028A) VR=IS, VM=1 Number of Pulses  */
    public static final int NUMBER_OF_PULSES = 0x300A028A;
        
    /** (300A,028C) VR=DS, VM=1 Pulse Repetition Interval  */
    public static final int PULSE_REPETITION_INTERVAL = 0x300A028C;
        
    /** (300A,0290) VR=IS, VM=1 Source Applicator Number  */
    public static final int SOURCE_APPLICATOR_NUMBER = 0x300A0290;
        
    /** (300A,0291) VR=SH, VM=1 Source Applicator ID  */
    public static final int SOURCE_APPLICATOR_ID = 0x300A0291;
        
    /** (300A,0292) VR=CS, VM=1 Source Applicator Type  */
    public static final int SOURCE_APPLICATOR_TYPE = 0x300A0292;
        
    /** (300A,0294) VR=LO, VM=1 Source Applicator Name  */
    public static final int SOURCE_APPLICATOR_NAME = 0x300A0294;
        
    /** (300A,0296) VR=DS, VM=1 Source Applicator Length  */
    public static final int SOURCE_APPLICATOR_LENGTH = 0x300A0296;
        
    /** (300A,0298) VR=LO, VM=1 Source Applicator Manufacturer  */
    public static final int SOURCE_APPLICATOR_MANUFACTURER = 0x300A0298;
        
    /** (300A,029C) VR=DS, VM=1 Source Applicator Wall Nominal Thickness  */
    public static final int SOURCE_APPLICATOR_WALL_NOMINAL_THICKNESS = 0x300A029C;
        
    /** (300A,029E) VR=DS, VM=1 Source Applicator Wall Nominal Transmission  */
    public static final int SOURCE_APPLICATOR_WALL_NOMINAL_TRANSMISSION = 0x300A029E;
        
    /** (300A,02A0) VR=DS, VM=1 Source Applicator Step Size  */
    public static final int SOURCE_APPLICATOR_STEP_SIZE = 0x300A02A0;
        
    /** (300A,02A2) VR=IS, VM=1 Transfer Tube Number  */
    public static final int TRANSFER_TUBE_NUMBER = 0x300A02A2;
        
    /** (300A,02A4) VR=DS, VM=1 Transfer Tube Length  */
    public static final int TRANSFER_TUBE_LENGTH = 0x300A02A4;
        
    /** (300A,02B0) VR=SQ, VM=1 Channel Shield Sequence  */
    public static final int CHANNEL_SHIELD_SEQUENCE = 0x300A02B0;
        
    /** (300A,02B2) VR=IS, VM=1 Channel Shield Number  */
    public static final int CHANNEL_SHIELD_NUMBER = 0x300A02B2;
        
    /** (300A,02B3) VR=SH, VM=1 Channel Shield ID  */
    public static final int CHANNEL_SHIELD_ID = 0x300A02B3;
        
    /** (300A,02B4) VR=LO, VM=1 Channel Shield Name  */
    public static final int CHANNEL_SHIELD_NAME = 0x300A02B4;
        
    /** (300A,02B8) VR=DS, VM=1 Channel Shield Nominal Thickness  */
    public static final int CHANNEL_SHIELD_NOMINAL_THICKNESS = 0x300A02B8;
        
    /** (300A,02BA) VR=DS, VM=1 Channel Shield Nominal Transmission  */
    public static final int CHANNEL_SHIELD_NOMINAL_TRANSMISSION = 0x300A02BA;
        
    /** (300A,02C8) VR=DS, VM=1 Final Cumulative Time Weight  */
    public static final int FINAL_CUMULATIVE_TIME_WEIGHT = 0x300A02C8;
        
    /** (300A,02D0) VR=SQ, VM=1 Brachy Control Point Sequence  */
    public static final int BRACHY_CONTROL_POINT_SEQUENCE = 0x300A02D0;
        
    /** (300A,02D2) VR=DS, VM=1 Control Point Relative Position  */
    public static final int CONTROL_POINT_RELATIVE_POSITION = 0x300A02D2;
        
    /** (300A,02D4) VR=DS, VM=3 Control Point 3D Position  */
    public static final int CONTROL_POINT_3D_POSITION = 0x300A02D4;
        
    /** (300A,02D6) VR=DS, VM=1 Cumulative Time Weight  */
    public static final int CUMULATIVE_TIME_WEIGHT = 0x300A02D6;
        
    /** (300A,02E0) VR=CS, VM=1 Compensator Divergence  */
    public static final int COMPENSATOR_DIVERGENCE = 0x300A02E0;
        
    /** (300A,02E1) VR=CS, VM=1 Compensator Mounting Position  */
    public static final int COMPENSATOR_MOUNTING_POSITION = 0x300A02E1;
        
    /** (300A,02E2) VR=DS, VM=1-n Source to Compensator Distance  */
    public static final int SOURCE_TO_COMPENSATOR_DISTANCE = 0x300A02E2;
        
    /** (300A,02E3) VR=FL, VM=1 Total Compensator Tray Water-Equivalent Thickness  */
    public static final int TOTAL_COMPENSATOR_TRAY_WATER_EQUIVALENT_THICKNESS = 0x300A02E3;
        
    /** (300A,02E4) VR=FL, VM=1 Isocenter to Compensator Tray Distance  */
    public static final int ISOCENTER_TO_COMPENSATOR_TRAY_DISTANCE = 0x300A02E4;
        
    /** (300A,02E5) VR=FL, VM=1 Compensator Column Offset  */
    public static final int COMPENSATOR_COLUMN_OFFSET = 0x300A02E5;
        
    /** (300A,02E6) VR=FL, VM=1-n Isocenter to Compensator Distances  */
    public static final int ISOCENTER_TO_COMPENSATOR_DISTANCES = 0x300A02E6;
        
    /** (300A,02E7) VR=FL, VM=1 Compensator Relative Stopping Power Ratio  */
    public static final int COMPENSATOR_RELATIVE_STOPPING_POWER_RATIO = 0x300A02E7;
        
    /** (300A,02E8) VR=FL, VM=1 Compensator Milling Tool Diameter  */
    public static final int COMPENSATOR_MILLING_TOOL_DIAMETER = 0x300A02E8;
        
    /** (300A,02EA) VR=SQ, VM=1 Ion Range Compensator Sequence  */
    public static final int ION_RANGE_COMPENSATOR_SEQUENCE = 0x300A02EA;
        
    /** (300A,0302) VR=IS, VM=1 Radiation Mass Number  */
    public static final int RADIATION_MASS_NUMBER = 0x300A0302;
        
    /** (300A,0304) VR=IS, VM=1 Radiation Atomic Number  */
    public static final int RADIATION_ATOMIC_NUMBER = 0x300A0304;
        
    /** (300A,0306) VR=SS, VM=1 Radiation Charge State  */
    public static final int RADIATION_CHARGE_STATE = 0x300A0306;
        
    /** (300A,0308) VR=CS, VM=1 Scan Mode  */
    public static final int SCAN_MODE = 0x300A0308;
        
    /** (300A,030A) VR=FL, VM=2 Virtual Source-Axis Distances  */
    public static final int VIRTUAL_SOURCE_AXIS_DISTANCES = 0x300A030A;
        
    /** (300A,030C) VR=SQ, VM=1 Snout Sequence  */
    public static final int SNOUT_SEQUENCE = 0x300A030C;
        
    /** (300A,030D) VR=FL, VM=1 Snout Position  */
    public static final int SNOUT_POSITION = 0x300A030D;
        
    /** (300A,030F) VR=SH, VM=1 Snout ID  */
    public static final int SNOUT_ID = 0x300A030F;
        
    /** (300A,0312) VR=IS, VM=1 Number of Range Shifters  */
    public static final int NUMBER_OF_RANGE_SHIFTERS = 0x300A0312;
        
    /** (300A,0314) VR=SQ, VM=1 Range Shifter Sequence  */
    public static final int RANGE_SHIFTER_SEQUENCE = 0x300A0314;
        
    /** (300A,0316) VR=IS, VM=1 Range Shifter Number  */
    public static final int RANGE_SHIFTER_NUMBER = 0x300A0316;
        
    /** (300A,0318) VR=SH, VM=1 Range Shifter ID  */
    public static final int RANGE_SHIFTER_ID = 0x300A0318;
        
    /** (300A,0320) VR=CS, VM=1 Range Shifter Type  */
    public static final int RANGE_SHIFTER_TYPE = 0x300A0320;
        
    /** (300A,0322) VR=LO, VM=1 Range Shifter Description  */
    public static final int RANGE_SHIFTER_DESCRIPTION = 0x300A0322;
        
    /** (300A,0330) VR=IS, VM=1 Number of Lateral Spreading Devices  */
    public static final int NUMBER_OF_LATERAL_SPREADING_DEVICES = 0x300A0330;
        
    /** (300A,0332) VR=SQ, VM=1 Lateral Spreading Device Sequence  */
    public static final int LATERAL_SPREADING_DEVICE_SEQUENCE = 0x300A0332;
        
    /** (300A,0334) VR=IS, VM=1 Lateral Spreading Device Number  */
    public static final int LATERAL_SPREADING_DEVICE_NUMBER = 0x300A0334;
        
    /** (300A,0336) VR=SH, VM=1 Lateral Spreading Device ID  */
    public static final int LATERAL_SPREADING_DEVICE_ID = 0x300A0336;
        
    /** (300A,0338) VR=CS, VM=1 Lateral Spreading Device Type  */
    public static final int LATERAL_SPREADING_DEVICE_TYPE = 0x300A0338;
        
    /** (300A,033A) VR=LO, VM=1 Lateral Spreading Device Description  */
    public static final int LATERAL_SPREADING_DEVICE_DESCRIPTION = 0x300A033A;
        
    /** (300A,033C) VR=FL, VM=1 Lateral Spreading Device Water Equivalent Thickness  */
    public static final int LATERAL_SPREADING_DEVICE_WATER_EQUIVALENT_THICKNESS = 0x300A033C;
        
    /** (300A,0340) VR=IS, VM=1 Number of Range Modulators  */
    public static final int NUMBER_OF_RANGE_MODULATORS = 0x300A0340;
        
    /** (300A,0342) VR=SQ, VM=1 Range Modulator Sequence  */
    public static final int RANGE_MODULATOR_SEQUENCE = 0x300A0342;
        
    /** (300A,0344) VR=IS, VM=1 Range Modulator Number  */
    public static final int RANGE_MODULATOR_NUMBER = 0x300A0344;
        
    /** (300A,0346) VR=SH, VM=1 Range Modulator ID  */
    public static final int RANGE_MODULATOR_ID = 0x300A0346;
        
    /** (300A,0348) VR=CS, VM=1 Range Modulator Type  */
    public static final int RANGE_MODULATOR_TYPE = 0x300A0348;
        
    /** (300A,034A) VR=LO, VM=1 Range Modulator Description  */
    public static final int RANGE_MODULATOR_DESCRIPTION = 0x300A034A;
        
    /** (300A,034C) VR=SH, VM=1 Beam Current Modulation ID  */
    public static final int BEAM_CURRENT_MODULATION_ID = 0x300A034C;
        
    /** (300A,0350) VR=CS, VM=1 Patient Support Type  */
    public static final int PATIENT_SUPPORT_TYPE = 0x300A0350;
        
    /** (300A,0352) VR=SH, VM=1 Patient Support ID  */
    public static final int PATIENT_SUPPORT_ID = 0x300A0352;
        
    /** (300A,0354) VR=LO, VM=1 Patient Support Accessory Code  */
    public static final int PATIENT_SUPPORT_ACCESSORY_CODE = 0x300A0354;
        
    /** (300A,0356) VR=FL, VM=1 Fixation Light Azimuthal Angle  */
    public static final int FIXATION_LIGHT_AZIMUTHAL_ANGLE = 0x300A0356;
        
    /** (300A,0358) VR=FL, VM=1 Fixation Light Polar Angle  */
    public static final int FIXATION_LIGHT_POLAR_ANGLE = 0x300A0358;
        
    /** (300A,035A) VR=FL, VM=1 Meterset Rate  */
    public static final int METERSET_RATE = 0x300A035A;
        
    /** (300A,0360) VR=SQ, VM=1 Range Shifter Settings Sequence  */
    public static final int RANGE_SHIFTER_SETTINGS_SEQUENCE = 0x300A0360;
        
    /** (300A,0362) VR=LO, VM=1 Range Shifter Setting  */
    public static final int RANGE_SHIFTER_SETTING = 0x300A0362;
        
    /** (300A,0364) VR=FL, VM=1 Isocenter to Range Shifter Distance  */
    public static final int ISOCENTER_TO_RANGE_SHIFTER_DISTANCE = 0x300A0364;
        
    /** (300A,0366) VR=FL, VM=1 Range Shifter Water Equivalent Thickness  */
    public static final int RANGE_SHIFTER_WATER_EQUIVALENT_THICKNESS = 0x300A0366;
        
    /** (300A,0370) VR=SQ, VM=1 Lateral Spreading Device Settings Sequence  */
    public static final int LATERAL_SPREADING_DEVICE_SETTINGS_SEQUENCE = 0x300A0370;
        
    /** (300A,0372) VR=LO, VM=1 Lateral Spreading Device Setting  */
    public static final int LATERAL_SPREADING_DEVICE_SETTING = 0x300A0372;
        
    /** (300A,0374) VR=FL, VM=1 Isocenter to Lateral Spreading Device Distance  */
    public static final int ISOCENTER_TO_LATERAL_SPREADING_DEVICE_DISTANCE = 0x300A0374;
        
    /** (300A,0380) VR=SQ, VM=1 Range Modulator Settings Sequence  */
    public static final int RANGE_MODULATOR_SETTINGS_SEQUENCE = 0x300A0380;
        
    /** (300A,0382) VR=FL, VM=1 Range Modulator Gating Start Value  */
    public static final int RANGE_MODULATOR_GATING_START_VALUE = 0x300A0382;
        
    /** (300A,0384) VR=FL, VM=1 Range Modulator Gating Stop Value  */
    public static final int RANGE_MODULATOR_GATING_STOP_VALUE = 0x300A0384;
        
    /** (300A,0386) VR=FL, VM=1 Range Modulator Gating Start Water Equivalent Thickness  */
    public static final int RANGE_MODULATOR_GATING_START_WATER_EQUIVALENT_THICKNESS = 0x300A0386;
        
    /** (300A,0388) VR=FL, VM=1 Range Modulator Gating Stop Water Equivalent Thickness  */
    public static final int RANGE_MODULATOR_GATING_STOP_WATER_EQUIVALENT_THICKNESS = 0x300A0388;
        
    /** (300A,038A) VR=FL, VM=1 Isocenter to Range Modulator Distance  */
    public static final int ISOCENTER_TO_RANGE_MODULATOR_DISTANCE = 0x300A038A;
        
    /** (300A,0390) VR=SH, VM=1 Scan Spot Tune ID  */
    public static final int SCAN_SPOT_TUNE_ID = 0x300A0390;
        
    /** (300A,0392) VR=IS, VM=1 Number of Scan Spot Positions  */
    public static final int NUMBER_OF_SCAN_SPOT_POSITIONS = 0x300A0392;
        
    /** (300A,0394) VR=FL, VM=1-n Scan Spot Position Map  */
    public static final int SCAN_SPOT_POSITION_MAP = 0x300A0394;
        
    /** (300A,0396) VR=FL, VM=1-n Scan Spot Meterset Weights  */
    public static final int SCAN_SPOT_METERSET_WEIGHTS = 0x300A0396;
        
    /** (300A,0398) VR=FL, VM=2 Scanning Spot Size  */
    public static final int SCANNING_SPOT_SIZE = 0x300A0398;
        
    /** (300A,039A) VR=IS, VM=1 Number of Paintings  */
    public static final int NUMBER_OF_PAINTINGS = 0x300A039A;
        
    /** (300A,03A0) VR=SQ, VM=1 Ion Tolerance Table Sequence  */
    public static final int ION_TOLERANCE_TABLE_SEQUENCE = 0x300A03A0;
        
    /** (300A,03A2) VR=SQ, VM=1 Ion Beam Sequence  */
    public static final int ION_BEAM_SEQUENCE = 0x300A03A2;
        
    /** (300A,03A4) VR=SQ, VM=1 Ion Beam Limiting Device Sequence  */
    public static final int ION_BEAM_LIMITING_DEVICE_SEQUENCE = 0x300A03A4;
        
    /** (300A,03A6) VR=SQ, VM=1 Ion Block Sequence  */
    public static final int ION_BLOCK_SEQUENCE = 0x300A03A6;
        
    /** (300A,03A8) VR=SQ, VM=1 Ion Control Point Sequence  */
    public static final int ION_CONTROL_POINT_SEQUENCE = 0x300A03A8;
        
    /** (300A,03AA) VR=SQ, VM=1 Ion Wedge Sequence  */
    public static final int ION_WEDGE_SEQUENCE = 0x300A03AA;
        
    /** (300A,03AC) VR=SQ, VM=1 Ion Wedge Position Sequence  */
    public static final int ION_WEDGE_POSITION_SEQUENCE = 0x300A03AC;
        
    /** (300A,0401) VR=SQ, VM=1 Referenced Setup Image Sequence  */
    public static final int REFERENCED_SETUP_IMAGE_SEQUENCE = 0x300A0401;
        
    /** (300A,0402) VR=ST, VM=1 Setup Image Comment  */
    public static final int SETUP_IMAGE_COMMENT = 0x300A0402;
        
    /** (300A,0410) VR=SQ, VM=1 Motion Synchronization Sequence  */
    public static final int MOTION_SYNCHRONIZATION_SEQUENCE = 0x300A0410;
        
    /** (300C,0002) VR=SQ, VM=1 Referenced RT Plan Sequence  */
    public static final int REFERENCED_RT_PLAN_SEQUENCE = 0x300C0002;
        
    /** (300C,0004) VR=SQ, VM=1 Referenced Beam Sequence  */
    public static final int REFERENCED_BEAM_SEQUENCE = 0x300C0004;
        
    /** (300C,0006) VR=IS, VM=1 Referenced Beam Number  */
    public static final int REFERENCED_BEAM_NUMBER = 0x300C0006;
        
    /** (300C,0007) VR=IS, VM=1 Referenced Reference Image Number  */
    public static final int REFERENCED_REFERENCE_IMAGE_NUMBER = 0x300C0007;
        
    /** (300C,0008) VR=DS, VM=1 Start Cumulative Meterset Weight  */
    public static final int START_CUMULATIVE_METERSET_WEIGHT = 0x300C0008;
        
    /** (300C,0009) VR=DS, VM=1 End Cumulative Meterset Weight  */
    public static final int END_CUMULATIVE_METERSET_WEIGHT = 0x300C0009;
        
    /** (300C,000A) VR=SQ, VM=1 Referenced Brachy Application Setup Sequence  */
    public static final int REFERENCED_BRACHY_APPLICATION_SETUP_SEQUENCE = 0x300C000A;
        
    /** (300C,000C) VR=IS, VM=1 Referenced Brachy Application Setup Number  */
    public static final int REFERENCED_BRACHY_APPLICATION_SETUP_NUMBER = 0x300C000C;
        
    /** (300C,000E) VR=IS, VM=1 Referenced Source Number  */
    public static final int REFERENCED_SOURCE_NUMBER = 0x300C000E;
        
    /** (300C,0020) VR=SQ, VM=1 Referenced Fraction Group Sequence  */
    public static final int REFERENCED_FRACTION_GROUP_SEQUENCE = 0x300C0020;
        
    /** (300C,0022) VR=IS, VM=1 Referenced Fraction Group Number  */
    public static final int REFERENCED_FRACTION_GROUP_NUMBER = 0x300C0022;
        
    /** (300C,0040) VR=SQ, VM=1 Referenced Verification Image Sequence  */
    public static final int REFERENCED_VERIFICATION_IMAGE_SEQUENCE = 0x300C0040;
        
    /** (300C,0042) VR=SQ, VM=1 Referenced Reference Image Sequence  */
    public static final int REFERENCED_REFERENCE_IMAGE_SEQUENCE = 0x300C0042;
        
    /** (300C,0050) VR=SQ, VM=1 Referenced Dose Reference Sequence  */
    public static final int REFERENCED_DOSE_REFERENCE_SEQUENCE = 0x300C0050;
        
    /** (300C,0051) VR=IS, VM=1 Referenced Dose Reference Number  */
    public static final int REFERENCED_DOSE_REFERENCE_NUMBER = 0x300C0051;
        
    /** (300C,0055) VR=SQ, VM=1 Brachy Referenced Dose Reference Sequence  */
    public static final int BRACHY_REFERENCED_DOSE_REFERENCE_SEQUENCE = 0x300C0055;
        
    /** (300C,0060) VR=SQ, VM=1 Referenced Structure Set Sequence  */
    public static final int REFERENCED_STRUCTURE_SET_SEQUENCE = 0x300C0060;
        
    /** (300C,006A) VR=IS, VM=1 Referenced Patient Setup Number  */
    public static final int REFERENCED_PATIENT_SETUP_NUMBER = 0x300C006A;
        
    /** (300C,0080) VR=SQ, VM=1 Referenced Dose Sequence  */
    public static final int REFERENCED_DOSE_SEQUENCE = 0x300C0080;
        
    /** (300C,00A0) VR=IS, VM=1 Referenced Tolerance Table Number  */
    public static final int REFERENCED_TOLERANCE_TABLE_NUMBER = 0x300C00A0;
        
    /** (300C,00B0) VR=SQ, VM=1 Referenced Bolus Sequence  */
    public static final int REFERENCED_BOLUS_SEQUENCE = 0x300C00B0;
        
    /** (300C,00C0) VR=IS, VM=1 Referenced Wedge Number  */
    public static final int REFERENCED_WEDGE_NUMBER = 0x300C00C0;
        
    /** (300C,00D0) VR=IS, VM=1 Referenced Compensator Number  */
    public static final int REFERENCED_COMPENSATOR_NUMBER = 0x300C00D0;
        
    /** (300C,00E0) VR=IS, VM=1 Referenced Block Number  */
    public static final int REFERENCED_BLOCK_NUMBER = 0x300C00E0;
        
    /** (300C,00F0) VR=IS, VM=1 Referenced Control Point Index  */
    public static final int REFERENCED_CONTROL_POINT_INDEX = 0x300C00F0;
        
    /** (300C,00F2) VR=SQ, VM=1 Referenced Control Point Sequence  */
    public static final int REFERENCED_CONTROL_POINT_SEQUENCE = 0x300C00F2;
        
    /** (300C,00F4) VR=IS, VM=1 Referenced Start Control Point Index  */
    public static final int REFERENCED_START_CONTROL_POINT_INDEX = 0x300C00F4;
        
    /** (300C,00F6) VR=IS, VM=1 Referenced Stop Control Point Index  */
    public static final int REFERENCED_STOP_CONTROL_POINT_INDEX = 0x300C00F6;
        
    /** (300C,0100) VR=IS, VM=1 Referenced Range Shifter Number  */
    public static final int REFERENCED_RANGE_SHIFTER_NUMBER = 0x300C0100;
        
    /** (300C,0102) VR=IS, VM=1 Referenced Lateral Spreading Device Number  */
    public static final int REFERENCED_LATERAL_SPREADING_DEVICE_NUMBER = 0x300C0102;
        
    /** (300C,0104) VR=IS, VM=1 Referenced Range Modulator Number  */
    public static final int REFERENCED_RANGE_MODULATOR_NUMBER = 0x300C0104;
        
    /** (300E,0002) VR=CS, VM=1 Approval Status  */
    public static final int APPROVAL_STATUS = 0x300E0002;
        
    /** (300E,0004) VR=DA, VM=1 Review Date  */
    public static final int REVIEW_DATE = 0x300E0004;
        
    /** (300E,0005) VR=TM, VM=1 Review Time  */
    public static final int REVIEW_TIME = 0x300E0005;
        
    /** (300E,0008) VR=PN, VM=1 Reviewer Name  */
    public static final int REVIEWER_NAME = 0x300E0008;
        
    /** (4000,0010) VR=LT, VM=1 Arbitrary RET */
    public static final int ARBITRARY_RET = 0x40000010;
        
    /** (4000,4000) VR=LT, VM=1 Text Comments RET */
    public static final int TEXT_COMMENTS_RET = 0x40004000;
        
    /** (4008,0040) VR=SH, VM=1 Results ID RET */
    public static final int RESULTS_ID_RET = 0x40080040;
        
    /** (4008,0042) VR=LO, VM=1 Results ID Issuer RET */
    public static final int RESULTS_ID_ISSUER_RET = 0x40080042;
        
    /** (4008,0050) VR=SQ, VM=1 Referenced Interpretation Sequence RET */
    public static final int REFERENCED_INTERPRETATION_SEQUENCE_RET = 0x40080050;
        
    /** (4008,0100) VR=DA, VM=1 Interpretation Recorded Date RET */
    public static final int INTERPRETATION_RECORDED_DATE_RET = 0x40080100;
        
    /** (4008,0101) VR=TM, VM=1 Interpretation Recorded Time RET */
    public static final int INTERPRETATION_RECORDED_TIME_RET = 0x40080101;
        
    /** (4008,0102) VR=PN, VM=1 Interpretation Recorder RET */
    public static final int INTERPRETATION_RECORDER_RET = 0x40080102;
        
    /** (4008,0103) VR=LO, VM=1 Reference to Recorded Sound RET */
    public static final int REFERENCE_TO_RECORDED_SOUND_RET = 0x40080103;
        
    /** (4008,0108) VR=DA, VM=1 Interpretation Transcription Date RET */
    public static final int INTERPRETATION_TRANSCRIPTION_DATE_RET = 0x40080108;
        
    /** (4008,0109) VR=TM, VM=1 Interpretation Transcription Time RET */
    public static final int INTERPRETATION_TRANSCRIPTION_TIME_RET = 0x40080109;
        
    /** (4008,010A) VR=PN, VM=1 Interpretation Transcriber RET */
    public static final int INTERPRETATION_TRANSCRIBER_RET = 0x4008010A;
        
    /** (4008,010B) VR=ST, VM=1 Interpretation Text RET */
    public static final int INTERPRETATION_TEXT_RET = 0x4008010B;
        
    /** (4008,010C) VR=PN, VM=1 Interpretation Author RET */
    public static final int INTERPRETATION_AUTHOR_RET = 0x4008010C;
        
    /** (4008,0111) VR=SQ, VM=1 Interpretation Approver Sequence RET */
    public static final int INTERPRETATION_APPROVER_SEQUENCE_RET = 0x40080111;
        
    /** (4008,0112) VR=DA, VM=1 Interpretation Approval Date RET */
    public static final int INTERPRETATION_APPROVAL_DATE_RET = 0x40080112;
        
    /** (4008,0113) VR=TM, VM=1 Interpretation Approval Time RET */
    public static final int INTERPRETATION_APPROVAL_TIME_RET = 0x40080113;
        
    /** (4008,0114) VR=PN, VM=1 Physician Approving Interpretation RET */
    public static final int PHYSICIAN_APPROVING_INTERPRETATION_RET = 0x40080114;
        
    /** (4008,0115) VR=LT, VM=1 Interpretation Diagnosis Description RET */
    public static final int INTERPRETATION_DIAGNOSIS_DESCRIPTION_RET = 0x40080115;
        
    /** (4008,0117) VR=SQ, VM=1 Interpretation Diagnosis Code Sequence RET */
    public static final int INTERPRETATION_DIAGNOSIS_CODE_SEQUENCE_RET = 0x40080117;
        
    /** (4008,0118) VR=SQ, VM=1 Results Distribution List Sequence RET */
    public static final int RESULTS_DISTRIBUTION_LIST_SEQUENCE_RET = 0x40080118;
        
    /** (4008,0119) VR=PN, VM=1 Distribution Name RET */
    public static final int DISTRIBUTION_NAME_RET = 0x40080119;
        
    /** (4008,011A) VR=LO, VM=1 Distribution Address RET */
    public static final int DISTRIBUTION_ADDRESS_RET = 0x4008011A;
        
    /** (4008,0200) VR=SH, VM=1 Interpretation ID RET */
    public static final int INTERPRETATION_ID_RET = 0x40080200;
        
    /** (4008,0202) VR=LO, VM=1 Interpretation ID Issuer RET */
    public static final int INTERPRETATION_ID_ISSUER_RET = 0x40080202;
        
    /** (4008,0210) VR=CS, VM=1 Interpretation Type ID RET */
    public static final int INTERPRETATION_TYPE_ID_RET = 0x40080210;
        
    /** (4008,0212) VR=CS, VM=1 Interpretation Status ID RET */
    public static final int INTERPRETATION_STATUS_ID_RET = 0x40080212;
        
    /** (4008,0300) VR=ST, VM=1 Impressions RET */
    public static final int IMPRESSIONS_RET = 0x40080300;
        
    /** (4008,4000) VR=ST, VM=1 Results Comments RET */
    public static final int RESULTS_COMMENTS_RET = 0x40084000;
        
    /** (4FFE,0001) VR=SQ, VM=1 MAC Parameters Sequence  */
    public static final int MAC_PARAMETERS_SEQUENCE = 0x4FFE0001;
        
    /** (50xx,0005) VR=US, VM=1 Curve Dimensions RET */
    public static final int CURVE_DIMENSIONS_RET = 0x50000005;
        
    /** (50xx,0010) VR=US, VM=1 Number of Points RET */
    public static final int NUMBER_OF_POINTS_RET = 0x50000010;
        
    /** (50xx,0020) VR=CS, VM=1 Type of Data RET */
    public static final int TYPE_OF_DATA_RET = 0x50000020;
        
    /** (50xx,0022) VR=LO, VM=1 Curve Description RET */
    public static final int CURVE_DESCRIPTION_RET = 0x50000022;
        
    /** (50xx,0030) VR=SH, VM=1-n Axis Units RET */
    public static final int AXIS_UNITS_RET = 0x50000030;
        
    /** (50xx,0040) VR=SH, VM=1-n Axis Labels RET */
    public static final int AXIS_LABELS_RET = 0x50000040;
        
    /** (50xx,0103) VR=US, VM=1 Data Value Representation RET */
    public static final int DATA_VALUE_REPRESENTATION_RET = 0x50000103;
        
    /** (50xx,0104) VR=US, VM=1-n Minimum Coordinate Value RET */
    public static final int MINIMUM_COORDINATE_VALUE_RET = 0x50000104;
        
    /** (50xx,0105) VR=US, VM=1-n Maximum Coordinate Value RET */
    public static final int MAXIMUM_COORDINATE_VALUE_RET = 0x50000105;
        
    /** (50xx,0106) VR=SH, VM=1-n Curve Range RET */
    public static final int CURVE_RANGE_RET = 0x50000106;
        
    /** (50xx,0110) VR=US, VM=1-n Curve Data Descriptor RET */
    public static final int CURVE_DATA_DESCRIPTOR_RET = 0x50000110;
        
    /** (50xx,0112) VR=US, VM=1-n Coordinate Start Value RET */
    public static final int COORDINATE_START_VALUE_RET = 0x50000112;
        
    /** (50xx,0114) VR=US, VM=1-n Coordinate Step Value RET */
    public static final int COORDINATE_STEP_VALUE_RET = 0x50000114;
        
    /** (50xx,1001) VR=CS, VM=1 Curve Activation Layer RET */
    public static final int CURVE_ACTIVATION_LAYER_RET = 0x50001001;
        
    /** (50xx,2000) VR=US, VM=1 Audio Type RET */
    public static final int AUDIO_TYPE_RET = 0x50002000;
        
    /** (50xx,2002) VR=US, VM=1 Audio Sample Format RET */
    public static final int AUDIO_SAMPLE_FORMAT_RET = 0x50002002;
        
    /** (50xx,2004) VR=US, VM=1 Number of Channels RET */
    public static final int NUMBER_OF_CHANNELS_RET = 0x50002004;
        
    /** (50xx,2006) VR=UL, VM=1 Number of Samples RET */
    public static final int NUMBER_OF_SAMPLES_RET = 0x50002006;
        
    /** (50xx,2008) VR=UL, VM=1 Sample Rate RET */
    public static final int SAMPLE_RATE_RET = 0x50002008;
        
    /** (50xx,200A) VR=UL, VM=1 Total Time RET */
    public static final int TOTAL_TIME_RET = 0x5000200A;
        
    /** (50xx,200C) VR=OW|OB, VM=1 Audio Sample Data RET */
    public static final int AUDIO_SAMPLE_DATA_RET = 0x5000200C;
        
    /** (50xx,200E) VR=LT, VM=1 Audio Comments RET */
    public static final int AUDIO_COMMENTS_RET = 0x5000200E;
        
    /** (50xx,2500) VR=LO, VM=1 Curve Label RET */
    public static final int CURVE_LABEL_RET = 0x50002500;
        
    /** (50xx,2600) VR=SQ, VM=1 Curve Referenced Overlay Sequence RET */
    public static final int CURVE_REFERENCED_OVERLAY_SEQUENCE_RET = 0x50002600;
        
    /** (50xx,2610) VR=US, VM=1 Referenced Overlay Group RET */
    public static final int REFERENCED_OVERLAY_GROUP_RET = 0x50002610;
        
    /** (50xx,3000) VR=OW|OB, VM=1 Curve Data RET */
    public static final int CURVE_DATA_RET = 0x50003000;
        
    /** (5200,9229) VR=SQ, VM=1 Shared Functional Groups Sequence  */
    public static final int SHARED_FUNCTIONAL_GROUPS_SEQUENCE = 0x52009229;
        
    /** (5200,9230) VR=SQ, VM=1 Per-frame Functional Groups Sequence  */
    public static final int PER_FRAME_FUNCTIONAL_GROUPS_SEQUENCE = 0x52009230;
        
    /** (5400,0100) VR=SQ, VM=1 Waveform Sequence  */
    public static final int WAVEFORM_SEQUENCE = 0x54000100;
        
    /** (5400,0110) VR=OB|OW, VM=1 Channel Minimum Value  */
    public static final int CHANNEL_MINIMUM_VALUE = 0x54000110;
        
    /** (5400,0112) VR=OB|OW, VM=1 Channel Maximum Value  */
    public static final int CHANNEL_MAXIMUM_VALUE = 0x54000112;
        
    /** (5400,1004) VR=US, VM=1 Waveform Bits Allocated  */
    public static final int WAVEFORM_BITS_ALLOCATED = 0x54001004;
        
    /** (5400,1006) VR=CS, VM=1 Waveform Sample Interpretation  */
    public static final int WAVEFORM_SAMPLE_INTERPRETATION = 0x54001006;
        
    /** (5400,100A) VR=OB|OW, VM=1 Waveform Padding Value  */
    public static final int WAVEFORM_PADDING_VALUE = 0x5400100A;
        
    /** (5400,1010) VR=OB|OW, VM=1 Waveform Data  */
    public static final int WAVEFORM_DATA = 0x54001010;
        
    /** (5600,0010) VR=OF, VM=1 First Order Phase Correction Angle  */
    public static final int FIRST_ORDER_PHASE_CORRECTION_ANGLE = 0x56000010;
        
    /** (5600,0020) VR=OF, VM=1 Spectroscopy Data  */
    public static final int SPECTROSCOPY_DATA = 0x56000020;
        
    /** (60xx,0010) VR=US, VM=1 Overlay Rows  */
    public static final int OVERLAY_ROWS = 0x60000010;
        
    /** (60xx,0011) VR=US, VM=1 Overlay Columns  */
    public static final int OVERLAY_COLUMNS = 0x60000011;
        
    /** (60xx,0012) VR=US, VM=1 Overlay Planes  */
    public static final int OVERLAY_PLANES = 0x60000012;
        
    /** (60xx,0015) VR=IS, VM=1 Number of Frames in Overlay  */
    public static final int NUMBER_OF_FRAMES_IN_OVERLAY = 0x60000015;
        
    /** (60xx,0022) VR=LO, VM=1 Overlay Description  */
    public static final int OVERLAY_DESCRIPTION = 0x60000022;
        
    /** (60xx,0040) VR=CS, VM=1 Overlay Type  */
    public static final int OVERLAY_TYPE = 0x60000040;
        
    /** (60xx,0045) VR=LO, VM=1 Overlay Subtype  */
    public static final int OVERLAY_SUBTYPE = 0x60000045;
        
    /** (60xx,0050) VR=SS, VM=2 Overlay Origin  */
    public static final int OVERLAY_ORIGIN = 0x60000050;
        
    /** (60xx,0051) VR=US, VM=1 Image Frame Origin  */
    public static final int IMAGE_FRAME_ORIGIN = 0x60000051;
        
    /** (60xx,0052) VR=US, VM=1 Overlay Plane Origin  */
    public static final int OVERLAY_PLANE_ORIGIN = 0x60000052;
        
    /** (60xx,0060) VR=CS, VM=1 Overlay Compression Code RET */
    public static final int OVERLAY_COMPRESSION_CODE_RET = 0x60000060;
        
    /** (60xx,0100) VR=US, VM=1 Overlay Bits Allocated  */
    public static final int OVERLAY_BITS_ALLOCATED = 0x60000100;
        
    /** (60xx,0102) VR=US, VM=1 Overlay Bit Position  */
    public static final int OVERLAY_BIT_POSITION = 0x60000102;
        
    /** (60xx,0110) VR=CS, VM=1 Overlay Format RET */
    public static final int OVERLAY_FORMAT_RET = 0x60000110;
        
    /** (60xx,0200) VR=US, VM=1 Overlay Location RET */
    public static final int OVERLAY_LOCATION_RET = 0x60000200;
        
    /** (60xx,1001) VR=CS, VM=1 Overlay Activation Layer  */
    public static final int OVERLAY_ACTIVATION_LAYER = 0x60001001;
        
    /** (60xx,1100) VR=US, VM=1 Overlay Descriptor - Gray RET */
    public static final int OVERLAY_DESCRIPTOR_GRAY_RET = 0x60001100;
        
    /** (60xx,1101) VR=US, VM=1 Overlay Descriptor - Red RET */
    public static final int OVERLAY_DESCRIPTOR_RED_RET = 0x60001101;
        
    /** (60xx,1102) VR=US, VM=1 Overlay Descriptor - Green RET */
    public static final int OVERLAY_DESCRIPTOR_GREEN_RET = 0x60001102;
        
    /** (60xx,1103) VR=US, VM=1 Overlay Descriptor - Blue RET */
    public static final int OVERLAY_DESCRIPTOR_BLUE_RET = 0x60001103;
        
    /** (60xx,1200) VR=US, VM=1-n Overlays- Gray RET */
    public static final int OVERLAYS_GRAY_RET = 0x60001200;
        
    /** (60xx,1201) VR=US, VM=1-n Overlays - Red RET */
    public static final int OVERLAYS_RED_RET = 0x60001201;
        
    /** (60xx,1202) VR=US, VM=1-n Overlays - Green RET */
    public static final int OVERLAYS_GREEN_RET = 0x60001202;
        
    /** (60xx,1203) VR=US, VM=1-n Overlays- Blue RET */
    public static final int OVERLAYS_BLUE_RET = 0x60001203;
        
    /** (60xx,1301) VR=IS, VM=1 ROI Area  */
    public static final int ROI_AREA = 0x60001301;
        
    /** (60xx,1302) VR=DS, VM=1 ROI Mean  */
    public static final int ROI_MEAN = 0x60001302;
        
    /** (60xx,1303) VR=DS, VM=1 ROI Standard Deviation  */
    public static final int ROI_STANDARD_DEVIATION = 0x60001303;
        
    /** (60xx,1500) VR=LO, VM=1 Overlay Label  */
    public static final int OVERLAY_LABEL = 0x60001500;
        
    /** (60xx,3000) VR=OB|OW, VM=1 Overlay Data  */
    public static final int OVERLAY_DATA = 0x60003000;
        
    /** (60xx,4000) VR=LT, VM=1 Overlay Comments RET */
    public static final int OVERLAY_COMMENTS_RET = 0x60004000;
        
    /** (7FE0,0010) VR=OW|OB, VM=1 Pixel Data  */
    public static final int PIXEL_DATA = 0x7FE00010;
        
    /** (FFFA,FFFA) VR=SQ, VM=1 Digital Signatures Sequence  */
    public static final int DIGITAL_SIGNATURES_SEQUENCE = 0xFFFAFFFA;
        
    /** (FFFC,FFFC) VR=OB, VM=1 Data Set Trailing Padding  */
    public static final int DATA_SET_TRAILING_PADDING = 0xFFFCFFFC;
        
    /** (FFFE,E000) VR=, VM=1 Item  */
    public static final int ITEM = 0xFFFEE000;
        
    /** (FFFE,E00D) VR=, VM=1 Item Delimitation Item  */
    public static final int ITEM_DELIMITATION_ITEM = 0xFFFEE00D;
        
    /** (FFFE,E0DD) VR=, VM=1 Sequence Delimitation Item  */
    public static final int SEQUENCE_DELIMITATION_ITEM = 0xFFFEE0DD;
}