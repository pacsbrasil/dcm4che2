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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4che2.net;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 6, 2005
 *
 */
public class CommandUtils
{
    public static final int SUCCESS = 0;
    public static final int PENDING = 0xFF00;
    
    public static final int NORMAL = 0;
    public static final int HIGH = 1;
    public static final int LOW = 2;
    
    public static final int C_STORE_RQ = 0x0001;
    public static final int C_STORE_RSP = 0x8001;
    public static final int C_GET_RQ = 0x0010;
    public static final int C_GET_RSP = 0x8010;
    public static final int C_FIND_RQ = 0x0020;
    public static final int C_FIND_RSP = 0x8020;
    public static final int C_MOVE_RQ = 0x0021;
    public static final int C_MOVE_RSP = 0x8021;
    public static final int C_ECHO_RQ = 0x0030;
    public static final int C_ECHO_RSP = 0x8030;
    public static final int N_EVENT_REPORT_RQ = 0x0100;
    public static final int N_EVENT_REPORT_RSP = 0x8100;
    public static final int N_GET_RQ = 0x0110;
    public static final int N_GET_RSP = 0x8110;
    public static final int N_SET_RQ = 0x0120;
    public static final int N_SET_RSP = 0x8120;
    public static final int N_ACTION_RQ = 0x0130;
    public static final int N_ACTION_RSP = 0x8130;
    public static final int N_CREATE_RQ = 0x0140;
    public static final int N_CREATE_RSP = 0x8140;
    public static final int N_DELETE_RQ = 0x0150;
    public static final int N_DELETE_RSP = 0x8150;
    public static final int C_CANCEL_RQ = 0x0FFF;
    private static final int RSP = 0x8000;

    public static final int NO_DATASET = 0x0101;
    private static final String NL = System.getProperty("line.separator");
    private static int withDatasetType = 0x0000;
    
    private static boolean includeUIDinRSP;

    
    public static boolean isResponse(DicomObject dcmobj)
    {
        return (dcmobj.getInt(Tag.COMMAND_FIELD) & RSP) != 0;
    }

    public static boolean isCancelRQ(DicomObject dcmobj)
    {
        return dcmobj.getInt(Tag.COMMAND_FIELD) == C_CANCEL_RQ;
    }

    public static DicomObject mkCStoreRQ(int msgId, String cuid, String iuid,
            int priority)
    {
       DicomObject rq = mkRQ(msgId, C_STORE_RQ, withDatasetType);
       rq.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
       rq.putString(Tag.AFFECTED_SOP_INSTANCE_UID, VR.UI, iuid);
       rq.putInt(Tag.PRIORITY, VR.US, priority);
       return rq;
    }
    
    public static DicomObject mkCStoreRQ(int msgId, String cuid, String iuid,
            int priority, String moveOriginatorAET, int moveOriginatorMsgId)
    {
       DicomObject rq = mkCStoreRQ(msgId, cuid, iuid, priority);
       rq.putString(Tag.MOVE_ORIGINATOR_APPLICATION_ENTITY_TITLE, VR.AE,
               moveOriginatorAET);
       rq.putInt(Tag.MOVE_ORIGINATOR_MESSAGE_ID, VR.US, moveOriginatorMsgId);
       return rq;
    }

    public static DicomObject mkCFindRQ(int msgId, String cuid, int priority)
    {
       DicomObject rq = mkRQ(msgId, C_FIND_RQ, withDatasetType);
       rq.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
       rq.putInt(Tag.PRIORITY, VR.US, priority);
       return rq;
    }

    public static DicomObject mkCGetRQ(int msgId, String cuid, int priority)
    {
       DicomObject rq = mkRQ(msgId, C_GET_RQ, withDatasetType);
       rq.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
       rq.putInt(Tag.PRIORITY, VR.US, priority);
       return rq;
    }
    
    public static DicomObject mkCMoveRQ(int msgId, String cuid, int priority,
            String destination)
    {
       DicomObject rq = mkRQ(msgId, C_MOVE_RQ, withDatasetType);
       rq.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
       rq.putInt(Tag.PRIORITY, VR.US, priority);
       rq.putString(Tag.MOVE_DESTINATION, VR.AE, destination);
       return rq;
    }

    public static DicomObject mkCCancelRQ(int msgId)
    {
        DicomObject rq = new BasicDicomObject();
        rq.putInt(Tag.COMMAND_FIELD, VR.US, C_CANCEL_RQ);
        rq.putInt(Tag.DATA_SET_TYPE, VR.US, NO_DATASET);
        rq.putInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO, VR.US, msgId);
        return rq;
    }
    
    public static DicomObject mkCEchoRQ(int msgId, String cuid)
    {
       DicomObject rq = mkRQ(msgId, C_ECHO_RQ, NO_DATASET);
       rq.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
       return rq;
    }
    
    public static DicomObject mkNEventReportRQ(int msgId, String cuid,
            String iuid, int eventTypeID, DicomObject data)
    {
       DicomObject rq = mkRQ(msgId, N_EVENT_REPORT_RQ, 
               data == null ? NO_DATASET : withDatasetType);
       rq.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
       rq.putString(Tag.AFFECTED_SOP_INSTANCE_UID, VR.UI, iuid);
       rq.putInt(Tag.EVENT_TYPE_ID, VR.US, eventTypeID);
       return rq;
    }
    
    public static DicomObject mkNGetRQ(int msgId, String cuid, String iuid,
            DicomObject data)
    {
       DicomObject rq = mkRQ(msgId, N_GET_RQ, 
               data == null ? NO_DATASET : withDatasetType);
       rq.putString(Tag.REQUESTED_SOP_CLASS_UID, VR.UI, cuid);
       rq.putString(Tag.REQUESTED_SOP_INSTANCE_UID, VR.UI, iuid);
       return rq;
    }
    
    public static DicomObject mkNSetRQ(int msgId, String cuid, String iuid)
    {
        DicomObject rq = mkRQ(msgId, N_SET_RQ, withDatasetType);
        rq.putString(Tag.REQUESTED_SOP_CLASS_UID, VR.UI, cuid);
        rq.putString(Tag.REQUESTED_SOP_INSTANCE_UID, VR.UI, iuid);
        return rq;
    }

    public static DicomObject mkNActionRQ(int msgId, String cuid,
            String iuid, int actionTypeID, DicomObject data)
    {
       DicomObject rq = mkRQ(msgId, N_ACTION_RQ, 
               data == null ? NO_DATASET : withDatasetType);
       rq.putString(Tag.REQUESTED_SOP_CLASS_UID, VR.UI, cuid);
       rq.putString(Tag.REQUESTED_SOP_INSTANCE_UID, VR.UI, iuid);
       rq.putInt(Tag.ACTION_TYPE_ID, VR.US, actionTypeID);
       return rq;
    }
       
    public static DicomObject mkNCreateRQ(int msgId, String cuid, String iuid)
    {
        DicomObject rq = mkRQ(msgId, N_CREATE_RQ, withDatasetType);
        if (cuid != null)
            rq.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
        rq.putString(Tag.AFFECTED_SOP_INSTANCE_UID, VR.UI, iuid);
        return rq;
    }

    public static DicomObject mkNDeleteRQ(int msgId, String cuid, String iuid)
    {
        DicomObject rq = mkRQ(msgId, N_DELETE_RQ, NO_DATASET);
        rq.putString(Tag.REQUESTED_SOP_CLASS_UID, VR.UI, cuid);
        rq.putString(Tag.REQUESTED_SOP_INSTANCE_UID, VR.UI, iuid);
        return rq;
    }    
    
    private static DicomObject mkRQ(int msgId, int cmdfield, int datasetType)
    {
        DicomObject rsp = new BasicDicomObject();
        rsp.putInt(Tag.MESSAGE_ID, VR.US, msgId);
        rsp.putInt(Tag.COMMAND_FIELD, VR.US, cmdfield);
        rsp.putInt(Tag.DATA_SET_TYPE, VR.US, datasetType);
        return rsp;
    }

    public static DicomObject mkRSP(DicomObject rq, int status)
    {
        DicomObject rsp = new BasicDicomObject();
        rsp.putInt(Tag.COMMAND_FIELD, VR.US, rq.getInt(Tag.COMMAND_FIELD) | RSP);
        rsp.putInt(Tag.STATUS, VR.US, status);
        rsp.putInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO, VR.US, rq.getInt(Tag.MESSAGE_ID));
        if (includeUIDinRSP) {
            String cuid = rq.getString(Tag.AFFECTED_SOP_CLASS_UID);
            if (cuid == null)
                cuid = rq.getString(Tag.REQUESTED_SOP_CLASS_UID);
            rsp.putString(Tag.AFFECTED_SOP_CLASS_UID, VR.UI, cuid);
            String iuid = rq.getString(Tag.AFFECTED_SOP_INSTANCE_UID);
            if (iuid == null)
                iuid = rq.getString(Tag.REQUESTED_SOP_INSTANCE_UID);
            if (iuid != null) {
                rsp.putString(Tag.AFFECTED_SOP_INSTANCE_UID, VR.UI, iuid);
            }
        }
        return rsp;
    }
    
    public static boolean isIncludeUIDinRSP()
    {
        return includeUIDinRSP;
    }

    public static void setIncludeUIDinRSP(boolean includeUIDinRSP)
    {
        CommandUtils.includeUIDinRSP = includeUIDinRSP;
    }

    public static int getWithDatasetType()
    {
        return withDatasetType;
    }

    public static void setWithDatasetType(int withDatasetType)
    {
        if (withDatasetType == NO_DATASET || (withDatasetType & 0xffff0000) != 0)
            throw new IllegalArgumentException("withDatasetType: " 
                    + Integer.toHexString(withDatasetType) + "H");
        CommandUtils.withDatasetType = withDatasetType;
    }

    public static boolean hasDataset(DicomObject dcmobj)
    {
        return dcmobj.getInt(Tag.DATA_SET_TYPE) != NO_DATASET;
    }

    public static boolean isPending(DicomObject cmd)
    {
        return (cmd.getInt(Tag.STATUS) & PENDING) == PENDING;
    }
    
    public static String toString(DicomObject cmd, int pcid, String tsuid)
    {
        UIDDictionary dict = UIDDictionary.getDictionary();
        StringBuffer sb = new StringBuffer(64);
        switch (cmd.getInt(Tag.COMMAND_FIELD))
        {
            case C_STORE_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":C-STORE-RQ[pcid=").append(pcid);
                sb.append(", prior=").append(cmd.getInt(Tag.PRIORITY));
                sb.append(NL).append("\tcuid=").append(dict.prompt(cmd.getString(Tag.AFFECTED_SOP_CLASS_UID)));
                sb.append(NL).append("\tiuid=").append(cmd.getString(Tag.AFFECTED_SOP_INSTANCE_UID));
                break;
            case C_GET_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":C-GET-RQ[pcid=").append(pcid);
                sb.append(", prior=").append(cmd.getInt(Tag.PRIORITY));
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.AFFECTED_SOP_CLASS_UID)));
                break;
            case C_FIND_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":C-FIND-RQ[pcid=").append(pcid);
                sb.append(", prior=").append(cmd.getInt(Tag.PRIORITY));
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.AFFECTED_SOP_CLASS_UID)));
                break;
            case C_MOVE_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":C-MOVE-RQ[pcid=").append(pcid);
                sb.append(", aet=").append(dict.prompt(cmd.getString(Tag.MOVE_DESTINATION)));
                sb.append(", prior=").append(cmd.getInt(Tag.PRIORITY));
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.AFFECTED_SOP_CLASS_UID)));
                break;
            case C_ECHO_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":C-ECHO-RQ[pcid=").append(pcid);
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.AFFECTED_SOP_CLASS_UID)));
                break;
            case N_EVENT_REPORT_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":N-EVENT-REPORT-RQ[pcid=").append(pcid);
                sb.append(", eventID=").append(cmd.getInt(Tag.EVENT_TYPE_ID));
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.AFFECTED_SOP_CLASS_UID)));
                sb.append(NL).append("\tiuid=");
                sb.append(cmd.getString(Tag.AFFECTED_SOP_INSTANCE_UID));
                break;
             case N_GET_RQ:
                 sb.append(cmd.getInt(Tag.MESSAGE_ID));
                 sb.append(":N-GET-RQ[pcid=").append(pcid);
                 sb.append(NL).append("\tcuid=");
                 sb.append(dict.prompt(cmd.getString(Tag.REQUESTED_SOP_CLASS_UID)));
                 sb.append(NL).append("\tiuid=");
                 sb.append(cmd.getString(Tag.REQUESTED_SOP_INSTANCE_UID));
                 break;
            case N_SET_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":N-SET-RQ[pcid=").append(pcid);
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.REQUESTED_SOP_CLASS_UID)));
                sb.append(NL).append("\tiuid=");
                sb.append(cmd.getString(Tag.REQUESTED_SOP_INSTANCE_UID));
                break;
           case N_ACTION_RQ:
               sb.append(cmd.getInt(Tag.MESSAGE_ID));
               sb.append(":N-ACTION-RQ[pcid=").append(pcid);
               sb.append(", actionID=").append(cmd.getInt(Tag.ACTION_TYPE_ID));
               sb.append(NL).append("\tcuid=");
               sb.append(dict.prompt(cmd.getString(Tag.REQUESTED_SOP_CLASS_UID)));
               sb.append(NL).append("\tiuid=");
               sb.append(cmd.getString(Tag.REQUESTED_SOP_INSTANCE_UID));
               break;
            case N_CREATE_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":N-CREATE-RQ[pcid=").append(pcid);
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.AFFECTED_SOP_CLASS_UID)));
                sb.append(NL).append("\tiuid=");
                sb.append(cmd.getString(Tag.AFFECTED_SOP_INSTANCE_UID));
                break;
            case N_DELETE_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID));
                sb.append(":N-DELETE-RQ[pcid=").append(pcid);
                sb.append(NL).append("\tcuid=");
                sb.append(dict.prompt(cmd.getString(Tag.REQUESTED_SOP_CLASS_UID)));
                sb.append(NL).append("\tiuid=");
                sb.append(cmd.getString(Tag.REQUESTED_SOP_INSTANCE_UID));
                break;
            case C_CANCEL_RQ:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":C-CANCEL-RQ[pcid=").append(pcid);
                break;
            case C_STORE_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":C-STORE-RSP[pcid=").append(pcid);
                break;
            case C_GET_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":C-GET-RSP[pcid=").append(pcid);
                sb.append(", remaining=").append(
                        cmd.getString(Tag.NUMBER_OF_REMAINING_SUB_OPERATIONS));
                sb.append(", completed=").append(
                        cmd.getString(Tag.NUMBER_OF_COMPLETED_SUB_OPERATIONS));
                sb.append(", failed=").append(
                        cmd.getString(Tag.NUMBER_OF_FAILED_SUB_OPERATIONS));
                sb.append(", warning=").append(
                        cmd.getString(Tag.NUMBER_OF_WARNING_SUB_OPERATIONS));
                break;
            case C_FIND_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":C-FIND-RSP[pcid=").append(pcid);
                break;
            case C_MOVE_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":C-MOVE-RSP[pcid=").append(pcid);
                sb.append(", remaining=").append(
                        cmd.getString(Tag.NUMBER_OF_REMAINING_SUB_OPERATIONS));
                sb.append(", completed=").append(
                        cmd.getString(Tag.NUMBER_OF_COMPLETED_SUB_OPERATIONS));
                sb.append(", failed=").append(
                        cmd.getString(Tag.NUMBER_OF_FAILED_SUB_OPERATIONS));
                sb.append(", warning=").append(
                        cmd.getString(Tag.NUMBER_OF_WARNING_SUB_OPERATIONS));
                break;
            case C_ECHO_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":C-ECHO-RSP[pcid=").append(pcid);
                break;
            case N_EVENT_REPORT_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":N-EVENT-REPORT-RSP[pcid=").append(pcid);
                sb.append(", eventID=").append(cmd.getString(Tag.EVENT_TYPE_ID));
                break;
            case N_GET_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":N-GET-RSP[pcid=").append(pcid);
                break;
            case N_SET_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":N-SET-RSP[pcid=").append(pcid);
                break;
            case N_ACTION_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":N-ACTION-RSP[pcid=").append(pcid);
                sb.append(", actionID=").append(cmd.getString(Tag.ACTION_TYPE_ID));
                break;
            case N_CREATE_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":N-CREATE-RSP[pcid=").append(pcid);
                break;
            case N_DELETE_RSP:
                sb.append(cmd.getInt(Tag.MESSAGE_ID_BEING_RESPONDED_TO));
                sb.append(":N-DELETE-RSP[pcid=").append(pcid);
                break;
           default:
                throw new IllegalArgumentException("CommandField:"
                        + cmd.get(Tag.COMMAND_FIELD));
        }
        if (hasDataset(cmd))
            sb.append(NL).append("\tts=").append(dict.prompt(tsuid));
        if (isResponse(cmd))
        {
            sb.append(", status=")
                .append(Integer.toHexString(cmd.getInt(Tag.STATUS)))
                .append('H');
            if (cmd.contains(Tag.ERROR_ID))
                sb.append(", errorID=").append(cmd.getInt(Tag.ERROR_ID));            
            if (cmd.contains(Tag.ERROR_COMMENT))
                sb.append(NL).append("\terror=").append(
                        cmd.getString(Tag.ERROR_COMMENT));
        }
        sb.append(']');
        return sb.toString();
    }
    
}
