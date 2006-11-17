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

package org.dcm4che2.tool.dcmgpwl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Mar 18, 2006
 *
 */
public class DcmGPWL {

    private static final int KB = 1024;
    private static final String USAGE = 
            "dcmgpwl [Options] <aet>[@<host>[:<port>]]";
    private static final String DESCRIPTION = 
            "Query specified remote Application Entity (=General Purpose Worklist SCP) " + 
            "If <port> is not specified, DICOM default port 104 is assumed. " +
            "If also no <host> is specified localhost is assumed.\n" +
            "Options:";
    private static final String EXAMPLE = 
            "\nExample: dcmgpwl GPWLSCP@localhost:11112 -status SCHEDULED\n" +
            "=> Query Application Entity GPWLSCP listening on local port 11112 for " +
            "all scheduled GP-SPS";

    private static final String[] SOP_CUIDS = {
        UID.GENERAL_PURPOSE_WORKLIST_INFORMATION_MODEL_FIND,
        UID.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_SOP_CLASS,
        UID.GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_SOP_CLASS};
    
    private static final String[] METASOP_CUID = {
        UID.GENERAL_PURPOSE_WORKLIST_MANAGEMENT_META_SOP_CLASS };

    private static final int[] RETURN_KEYS = {
        Tag.SOP_CLASS_UID,
        Tag.SOP_INSTANCE_UID,
        Tag.PATIENTS_NAME,
        Tag.PATIENT_ID,
        Tag.PATIENTS_BIRTH_DATE,
        Tag.PATIENTS_SEX,
        Tag.STUDY_INSTANCE_UID,
        Tag.SCHEDULED_PROCEDURE_STEP_ID,
        Tag.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_STATUS,
        Tag.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_PRIORITY,
        Tag.SCHEDULED_PROCEDURE_STEP_START_DATE_AND_TIME,
        Tag.MULTIPLE_COPIES_FLAG,
        Tag.SCHEDULED_PROCEDURE_STEP_MODIFICATION_DATE_AND_TIME,
        Tag.EXPECTED_COMPLETION_DATE_AND_TIME,
        Tag.INPUT_AVAILABILITY_FLAG,
    };

    private static final int[] REQUEST_RETURN_KEYS = {
        Tag.ACCESSION_NUMBER,
        Tag.STUDY_INSTANCE_UID,
        Tag.REQUESTING_PHYSICIAN,
        Tag.REQUESTED_PROCEDURE_DESCRIPTION,
        Tag.REQUESTED_PROCEDURE_ID,
    };

    private static final int[] RETURN_SQ_KEYS = {
        Tag.REFERENCED_PERFORMED_PROCEDURE_STEP_SEQUENCE,
        Tag.SCHEDULED_PROCESSING_APPLICATIONS_CODE_SEQUENCE,
        Tag.RESULTING_GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEPS_SEQUENCE,
        Tag.SCHEDULED_WORKITEM_CODE_SEQUENCE,
        Tag.INPUT_INFORMATION_SEQUENCE,
        Tag.RELEVANT_INFORMATION_SEQUENCE,
        Tag.SCHEDULED_STATION_NAME_CODE_SEQUENCE,
        Tag.SCHEDULED_STATION_CLASS_CODE_SEQUENCE,
        Tag.SCHEDULED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE,
        Tag.SCHEDULED_HUMAN_PERFORMERS_SEQUENCE,
        Tag.ACTUAL_HUMAN_PERFORMERS_SEQUENCE,
        Tag.REFERENCED_REQUEST_SEQUENCE,
    };

    private static final int[] PPS_CREATE_TYPE2 = {
        Tag.PATIENTS_NAME,
        Tag.PATIENT_ID,
        Tag.PATIENTS_BIRTH_DATE,
        Tag.PATIENTS_SEX,
        Tag.PERFORMED_PROCEDURE_STEP_DESCRIPTION,
    };

    private static final int[] PPS_SQ_CREATE_TYPE2 = {
        Tag.PERFORMED_PROCESSING_APPLICATIONS_CODE_SEQUENCE,
        Tag.PERFORMED_WORKITEM_CODE_SEQUENCE,
        Tag.PERFORMED_STATION_NAME_CODE_SEQUENCE,
        Tag.PERFORMED_STATION_CLASS_CODE_SEQUENCE,
        Tag.PERFORMED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE,
        Tag.OUTPUT_INFORMATION_SEQUENCE,
        Tag.ACTUAL_HUMAN_PERFORMERS_SEQUENCE,
        Tag.REFERENCED_REQUEST_SEQUENCE,
    };
    
    private static final int[] SPS_SQ_PPS_SQ_MAP = {
        Tag.SCHEDULED_PROCESSING_APPLICATIONS_CODE_SEQUENCE,
        Tag.PERFORMED_PROCESSING_APPLICATIONS_CODE_SEQUENCE,
        Tag.SCHEDULED_WORKITEM_CODE_SEQUENCE,
        Tag.PERFORMED_WORKITEM_CODE_SEQUENCE,
        Tag.SCHEDULED_STATION_NAME_CODE_SEQUENCE,
        Tag.PERFORMED_STATION_NAME_CODE_SEQUENCE,
        Tag.SCHEDULED_STATION_CLASS_CODE_SEQUENCE,
        Tag.PERFORMED_STATION_CLASS_CODE_SEQUENCE,
        Tag.SCHEDULED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE,
        Tag.PERFORMED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE,
        Tag.SCHEDULED_HUMAN_PERFORMERS_SEQUENCE,
        Tag.ACTUAL_HUMAN_PERFORMERS_SEQUENCE,
    };

    private static final int[] PPS_CREATE_FROM_SPS = {
        Tag.SPECIFIC_CHARACTER_SET,
        Tag.PATIENTS_NAME,
        Tag.PATIENT_ID,
        Tag.PATIENTS_BIRTH_DATE,
        Tag.PATIENTS_SEX,
        Tag.ACTUAL_HUMAN_PERFORMERS_SEQUENCE,
        Tag.REFERENCED_REQUEST_SEQUENCE,
    };
    
    private static final String[] IVRLE_TS = {
        UID.IMPLICIT_VR_LITTLE_ENDIAN };
    
    private static final String[] LE_TS = {
        UID.EXPLICIT_VR_LITTLE_ENDIAN, 
        UID.IMPLICIT_VR_LITTLE_ENDIAN };
    
    private Executor executor = new NewThreadExecutor("DCMGPWL");
    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private NetworkConnection remoteConn = new NetworkConnection();
    private Device device = new Device("DCMGPWL");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private NetworkConnection conn = new NetworkConnection();
    private File outDir;
    private String retrieveAET;
    private Association assoc;
    private int priority = 0;
    private int cancelAfter = Integer.MAX_VALUE;
    private final DicomObject attrs = new BasicDicomObject();
     
    public DcmGPWL() {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle("DCMGPWL");
    }

    public void initQuery() {
        for (int i = 0; i < RETURN_KEYS.length; i++) {
            attrs.putNull(RETURN_KEYS[i], null);
        }
        for (int i = 0; i < RETURN_SQ_KEYS.length; i++) {
            attrs.putNestedDicomObject(RETURN_SQ_KEYS[i],
                    new BasicDicomObject());
        }
        DicomObject rqAttrs = new BasicDicomObject();
        attrs.putNestedDicomObject(Tag.REFERENCED_REQUEST_SEQUENCE, rqAttrs );
        for (int i = 0; i < REQUEST_RETURN_KEYS.length; i++) {
            rqAttrs.putNull(REQUEST_RETURN_KEYS[i], null);
        }
        rqAttrs.putNestedDicomObject(Tag.REQUESTED_PROCEDURE_CODE_SEQUENCE,
                new BasicDicomObject());
    }

    public void initAction() {
        attrs.putString(Tag.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_STATUS,
                VR.CS, "IN PROGRESS");
    }

    public void initCreatePPS(String[] refsps) {
        long ts = System.currentTimeMillis();
        Date now = new Date(ts);
        attrs.putString(Tag.PERFORMED_PROCEDURE_STEP_ID, VR.SH, Long.toString(ts));
        attrs.putDate(Tag.PERFORMED_PROCEDURE_STEP_START_DATE, VR.DA, now);
        attrs.putDate(Tag.PERFORMED_PROCEDURE_STEP_START_TIME, VR.TM, now);
        attrs.putString(Tag.GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_STATUS,
                VR.CS, "IN PROGRESS");
        for (int i = 0; i < PPS_CREATE_TYPE2.length; i++) {
            attrs.putNull(PPS_CREATE_TYPE2[i], null);
        }
        for (int i = 0; i < PPS_SQ_CREATE_TYPE2.length; i++) {
            attrs.putSequence(PPS_SQ_CREATE_TYPE2[i]);
        }
        if (refsps != null) {            
            DicomObject item = new BasicDicomObject();
            item.putString(Tag.REFERENCED_SOP_CLASS_UID, VR.UI,
                    UID.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_SOP_CLASS);
            item.putString(Tag.REFERENCED_SOP_INSTANCE_UID, VR.UI, refsps[0]);
            item.putString(
                    Tag.REFERENCED_GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_TRANSACTION_UID,
                    VR.UI, refsps[1]);
            attrs.putNestedDicomObject(
                    Tag.REFERENCED_GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_SEQUENCE,
                    item);
            if (outDir != null) {
                File f = new File(outDir, refsps[0]);
                if (f.isFile()) {
                    DicomInputStream din = null;
                    try {
                        din = new DicomInputStream(f);
                        DicomObject sps = din.readDicomObject();
                        for (int i = 1; i < SPS_SQ_PPS_SQ_MAP.length; i++,i++) {
                            DicomObject codeItem = sps.getNestedDicomObject(SPS_SQ_PPS_SQ_MAP[i-1]);
                            if (codeItem != null) {
                                attrs.putNestedDicomObject(SPS_SQ_PPS_SQ_MAP[i], codeItem);
                            }
                        }
                        sps.subSet(PPS_CREATE_FROM_SPS).copyTo(attrs);
                    } catch (IOException e) {
                        System.out.println("WARNING: Failed to read " + f + ": "
                                + e.getMessage());
                    } finally {
                        if (din != null) {
                            try {
                                din.close();
                            } catch (IOException ignore) {}
                        }
                    }
                }
            }
        }
    }
    
    public void addOutput(DicomObject inst) {
        DicomObject studyRef = findOrCreateItem(
                attrs.get(Tag.OUTPUT_INFORMATION_SEQUENCE),
                Tag.STUDY_INSTANCE_UID, inst.getString(Tag.STUDY_INSTANCE_UID),
                Tag.REFERENCED_SERIES_SEQUENCE);
        DicomObject seriesRef = findOrCreateItem(
                studyRef.get(Tag.REFERENCED_SERIES_SEQUENCE),
                Tag.SERIES_INSTANCE_UID, inst.getString(Tag.SERIES_INSTANCE_UID),
                Tag.REFERENCED_SOP_SEQUENCE);
        DicomObject refSOP = new BasicDicomObject();
        refSOP.putString(Tag.REFERENCED_SOP_CLASS_UID, VR.UI, 
                inst.getString(Tag.SOP_CLASS_UID));
        refSOP.putString(Tag.REFERENCED_SOP_INSTANCE_UID, VR.UI,
                inst.getString(Tag.SOP_INSTANCE_UID));
        refSOP.putString(Tag.RETRIEVE_AE_TITLE, VR.AE, retrieveAET);
        seriesRef.get(Tag.REFERENCED_SOP_SEQUENCE).addDicomObject(refSOP);
    }
        
    private DicomObject findOrCreateItem(DicomElement sq, int tag, String uid,
            int sqtag) {
        DicomObject item;
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            item = sq.getDicomObject(i);
            if (uid.equals(item.getString(tag))) {
                return item;
            }
        }
        item = new BasicDicomObject();
        item.putString(tag, VR.UI, uid);
        item.putSequence(sqtag);
        sq.addDicomObject(item);
        return item;
    }

    public void initSetPPS() {
        attrs.putSequence(Tag.OUTPUT_INFORMATION_SEQUENCE);
    }
    
    public final void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

    public final void setRemoteHost(String hostname) {
        remoteConn.setHostname(hostname);
    }

    public final void setRemotePort(int port) {
        remoteConn.setPort(port);
    }

    public final void setCalledAET(String called) {
        remoteAE.setAETitle(called);
    }

    public final void setCalling(String calling) {
        ae.setAETitle(calling);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setOutDir(File out) {
        if (out.mkdirs()) {
            System.out.println("M-WRITE " + out);
        }
        this.outDir = out;
    }

    public final void setConnectTimeout(int connectTimeout) {
        conn.setConnectTimeout(connectTimeout);
    }

    public final void setMaxPDULengthReceive(int maxPDULength) {
        ae.setMaxPDULengthReceive(maxPDULength);
    }

    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        conn.setTcpNoDelay(tcpNoDelay);
    }

    public final void setAcceptTimeout(int timeout) {
        conn.setAcceptTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        conn.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout) {
        conn.setSocketCloseDelay(timeout);
    }

    public final void setMaxPDULengthSend(int maxPDULength) {
        ae.setMaxPDULengthSend(maxPDULength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        conn.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        conn.setSendBufferSize(bufferSize);
    }

    public final void setCancelAfter(int limit) {
        this.cancelAfter = limit;
    }

    public final void setRetrieveAET(String aet) {
        this.retrieveAET = aet;        
    }
    
    public void addAttr(int tag, String value) {
        attrs.putString(tag, null, value);
    }
 
    public void addRefRequestAttr(int tag, String value) {
        DicomObject rqAttrs = 
            attrs.getNestedDicomObject(Tag.REFERENCED_REQUEST_SEQUENCE);
        if (rqAttrs == null) {
            rqAttrs = new BasicDicomObject();
            attrs.putNestedDicomObject(Tag.REFERENCED_REQUEST_SEQUENCE, rqAttrs);
        }
        rqAttrs .putString(tag, null, value);
    }
    
    public void addCodeValueAndScheme(int tag, String[] code) {
        DicomObject item = attrs.getNestedDicomObject(tag);
        if (item == null) {
            item = new BasicDicomObject();
            attrs.putNestedDicomObject(tag, item);
        }
        setCodeValueAndScheme(item, code);
    }

    private void setCodeValueAndScheme(DicomObject codeItem, String[] code) {
        codeItem.putString(Tag.CODE_VALUE, VR.SH, code[0]);
        codeItem.putString(Tag.CODING_SCHEME_DESIGNATOR, VR.SH, code[1]);
        codeItem.putString(Tag.CODE_MEANING, VR.LO, code[2]);
    }

    public void setScheduledHumanPerformerCodeValueAndScheme(String[] valueAndScheme) {
        DicomObject performerKeys = attrs.getNestedDicomObject(
                Tag.SCHEDULED_HUMAN_PERFORMERS_SEQUENCE);
        DicomObject codeItem = new BasicDicomObject();
        setCodeValueAndScheme(codeItem, valueAndScheme);        
        performerKeys.putNestedDicomObject(Tag.HUMAN_PERFORMER_CODE_SEQUENCE, codeItem);
        performerKeys.putNull(Tag.HUMAN_PERFORMERS_NAME, VR.PN);
        performerKeys.putNull(Tag.HUMAN_PERFORMERS_ORGANIZATION, VR.LO);
    }
    
    public void setActualHumanPerformer(String[] code, String name, String org) {
        DicomObject performerKeys = new BasicDicomObject();
        attrs.putNestedDicomObject(Tag.ACTUAL_HUMAN_PERFORMERS_SEQUENCE,
                performerKeys);
        BasicDicomObject codeItem = new BasicDicomObject();
        codeItem.putString(Tag.CODE_VALUE, VR.SH, code[0]);
        codeItem.putString(Tag.CODING_SCHEME_DESIGNATOR, VR.SH, code[1]);
        codeItem.putString(Tag.CODE_MEANING, VR.LO, code[2]);
        performerKeys.putNestedDicomObject(Tag.HUMAN_PERFORMER_CODE_SEQUENCE, codeItem);
        if (name != null) {
            performerKeys.putString(Tag.HUMAN_PERFORMERS_NAME, VR.PN, name);
        }
        if (org != null) {
            performerKeys.putString(Tag.HUMAN_PERFORMERS_ORGANIZATION,VR.LO, org);
        }
    }
    
    public void setSPSStatus(String status) {
        attrs.putString(Tag.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_STATUS,
                VR.CS, status);        
    }    

    public void setPPSStatus(String status) {
        attrs.putString(Tag.GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_STATUS,
                VR.CS, status);
        if ("COMPLETED".equals(status) || "DISCONTINUED".equals(status)) {
            Date now = new Date();
            attrs.putDate(Tag.PERFORMED_PROCEDURE_STEP_END_DATE, VR.DA, now);
            attrs.putDate(Tag.PERFORMED_PROCEDURE_STEP_END_TIME, VR.TM, now);
        }
    }    
    
    public void configureTransferCapability(String[] cuids, String[] ts) {
        TransferCapability[] tc = new TransferCapability[cuids.length];
        for (int i = 0; i < tc.length; i++) {
            tc[i] = new TransferCapability(cuids[i], ts, TransferCapability.SCU);
        }
        ae.setTransferCapability(tc);       
    }

    public void setTransactionUID(String uid) {
        attrs.putString(Tag.TRANSACTION_UID, VR.UI, uid);
    }
    
    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    private TransferCapability selectTransferCapability(String cuid)
    throws NoPresentationContextException {
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(
                UID.GENERAL_PURPOSE_WORKLIST_MANAGEMENT_META_SOP_CLASS);
        if (tc == null) {
            tc = assoc.getTransferCapabilityAsSCU(cuid);
            if (tc == null) {
                throw new NoPresentationContextException(
                        UIDDictionary.getDictionary().prompt(cuid) +
                        "not supported by " + remoteAE.getAETitle());
            }
        }
        return tc;
    }

    public int query() throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GENERAL_PURPOSE_WORKLIST_INFORMATION_MODEL_FIND);
        System.out.println("Send Query Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.cfind(UID.GENERAL_PURPOSE_WORKLIST_INFORMATION_MODEL_FIND,
                priority, attrs, tc.getTransferSyntax()[0], cancelAfter);
        int count = 0;
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                count++;
                System.out.println("\nReceived Query Response #" + count + ":");
                System.out.println(data.toString());
                if (outDir != null) {
                    String iuid = data.getString(Tag.SOP_INSTANCE_UID);
                    File f = new File(outDir, iuid);
                    System.out.println("M-WRITE " + f);
                    FileOutputStream fos = new FileOutputStream(f);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    DicomOutputStream dos = new DicomOutputStream(bos);
                    try {
                        data.initFileMetaInformation(UID.EXPLICIT_VR_LITTLE_ENDIAN);
                        dos.writeDicomFile(data);
                    } finally {
                        dos.close();
                    }
                }
            }
        }
        return count;
    }
    
    private void action(String iuid) throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_SOP_CLASS);
        System.out.println("Send GP-SPS Modify Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.naction(
                UID.GENERAL_PURPOSE_SCHEDULED_PROCEDURE_STEP_SOP_CLASS, iuid,
                1, attrs, tc.getTransferSyntax()[0]);
        rsp.next();
        DicomObject cmd = rsp.getCommand();
        System.out.println("\nReceived GP-PS Modify Response:");
        System.out.println(cmd.toString());
    }

    public void createpps(String iuid)throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_SOP_CLASS);
        System.out.println("Send GP-PPS Create Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.ncreate(
                UID.GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_SOP_CLASS, iuid,
                attrs, tc.getTransferSyntax()[0]);
        rsp.next();
        DicomObject cmd = rsp.getCommand();
        System.out.println("\nReceived GP-PPS Create Response:");
        System.out.println(cmd.toString());
    }
    
    public void setpps(String iuid) throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(
                UID.GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_SOP_CLASS);
        System.out.println("Send GP-PPS Update Request:");
        System.out.println(attrs.toString());
        DimseRSP rsp = assoc.nset(
                UID.GENERAL_PURPOSE_PERFORMED_PROCEDURE_STEP_SOP_CLASS, iuid,
                attrs, tc.getTransferSyntax()[0]);
        rsp.next();
        DicomObject cmd = rsp.getCommand();
        System.out.println("\nReceived GP-PPS Update Response:");
        System.out.println(cmd.toString());
    }

    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DcmGPWL dcmgpwl = new DcmGPWL();
        final List argList = cl.getArgList();
        String remoteAE = (String) argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmgpwl.setCalledAET(calledAETAddress[0]);
        if (calledAETAddress[1] == null) {
            dcmgpwl.setRemoteHost("127.0.0.1");
            dcmgpwl.setRemotePort(104);
        } else {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmgpwl.setRemoteHost(hostPort[0]);
            dcmgpwl.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L")) {
            String localAE = (String) cl.getOptionValue("L");
            String[] callingAETHost = split(localAE, '@');
            dcmgpwl.setCalling(callingAETHost[0]);
            if (callingAETHost[1] != null) {
                dcmgpwl.setLocalHost(callingAETHost[1]);
            }
        }
        if (cl.hasOption("connectTO"))
            dcmgpwl.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("reaper"))
            dcmgpwl.setAssociationReaperPeriod(parseInt(cl.getOptionValue("reaper"),
                    "illegal argument of option -reaper", 1, Integer.MAX_VALUE));
        if (cl.hasOption("rspTO"))
            dcmgpwl.setDimseRspTimeout(parseInt(cl.getOptionValue("rspTO"),
                    "illegal argument of option -rspTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmgpwl.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmgpwl.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmgpwl.setSocketCloseDelay(parseInt(cl.getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption("rcvpdulen"))
            dcmgpwl.setMaxPDULengthReceive(parseInt(cl.getOptionValue("rcvpdulen"),
                    "illegal argument of option -rcvpdulen", 1, 10000) * KB);
        if (cl.hasOption("sndpdulen"))
            dcmgpwl.setMaxPDULengthSend(parseInt(cl.getOptionValue("sndpdulen"),
                    "illegal argument of option -sndpdulen", 1, 10000) * KB);
        if (cl.hasOption("sosndbuf"))
            dcmgpwl.setSendBufferSize(parseInt(cl.getOptionValue("sosndbuf"),
                    "illegal argument of option -sosndbuf", 1, 10000) * KB);
        if (cl.hasOption("sorcvbuf"))
            dcmgpwl.setReceiveBufferSize(parseInt(cl.getOptionValue("sorcvbuf"),
                    "illegal argument of option -sorcvbuf", 1, 10000) * KB);
        dcmgpwl.setPackPDV(!cl.hasOption("pdv1"));
        dcmgpwl.setTcpNoDelay(!cl.hasOption("tcpdelay"));
        
        if (cl.hasOption("o")) {
            dcmgpwl.setOutDir(new File(cl.getOptionValue("o")));
        }
        if (cl.hasOption("retrieve")) {
            dcmgpwl.setRetrieveAET(cl.getOptionValue("retrieve"));
        }
        if (cl.hasOption("action")) {
            dcmgpwl.initAction();        
            dcmgpwl.setTransactionUID(cl.getOptionValues("action")[1]);
            if (cl.hasOption("status")) {
                dcmgpwl.setSPSStatus(cl.getOptionValue("status").toUpperCase());
            }            
            if (cl.hasOption("perfcode")) {
                dcmgpwl.setActualHumanPerformer(cl.getOptionValues("perfcode"),
                        cl.getOptionValue("perfname"), cl.getOptionValue("perforg"));
            }
        } else if (cl.hasOption("createpps")) {
            dcmgpwl.initCreatePPS(cl.getOptionValues("refsps"));      
            if (cl.hasOption("A")) {
                String[] matchingKeys = cl.getOptionValues("A");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("rqA")) {
                String[] matchingKeys = cl.getOptionValues("rqA");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addRefRequestAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("workitem")) {
                dcmgpwl.addCodeValueAndScheme(Tag.PERFORMED_WORKITEM_CODE_SEQUENCE,
                        cl.getOptionValues("workitem"));
            }
            if (cl.hasOption("application")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.PERFORMED_PROCESSING_APPLICATIONS_CODE_SEQUENCE,
                        cl.getOptionValues("application"));
            }
            if (cl.hasOption("station")) {
                dcmgpwl.addCodeValueAndScheme(Tag.PERFORMED_STATION_NAME_CODE_SEQUENCE,
                        cl.getOptionValues("station"));
            }
            if (cl.hasOption("class")) {
                dcmgpwl.addCodeValueAndScheme(Tag.PERFORMED_STATION_CLASS_CODE_SEQUENCE,
                        cl.getOptionValues("class"));
            }
            if (cl.hasOption("location")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.PERFORMED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE,
                        cl.getOptionValues("location"));
            }
            if (cl.hasOption("perfcode")) {
                dcmgpwl.setActualHumanPerformer(cl.getOptionValues("perfcode"),
                        cl.getOptionValue("perfname"), cl.getOptionValue("perforg"));
            }
            for (int i = 1, n = argList.size(); i < n; i++) {
                addOutput(dcmgpwl, new File((String) argList.get(i)));
            }
        } else if (cl.hasOption("setpps")) {
            dcmgpwl.initSetPPS();
            if (cl.hasOption("status")) {
                dcmgpwl.setPPSStatus(cl.getOptionValue("status").toUpperCase());
            }
            for (int i = 1, n = argList.size(); i < n; i++) {
                addOutput(dcmgpwl, new File((String) argList.get(i)));
            }
        } else {
            dcmgpwl.initQuery();
            if (cl.hasOption("status")) {
                dcmgpwl.setSPSStatus(cl.getOptionValue("status").toUpperCase());
            }            
            if (cl.hasOption("C"))
                dcmgpwl.setCancelAfter(parseInt(cl.getOptionValue("C"),
                        "illegal argument of option -C", 1, Integer.MAX_VALUE));
            if (cl.hasOption("lowprior"))
                dcmgpwl.setPriority(CommandUtils.LOW);
            if (cl.hasOption("highprior"))
                dcmgpwl.setPriority(CommandUtils.HIGH);
            if (cl.hasOption("A")) {
                String[] matchingKeys = cl.getOptionValues("A");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("rqA")) {
                String[] matchingKeys = cl.getOptionValues("rqA");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addRefRequestAttr(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("D")) {
                dcmgpwl.addAttr(Tag.SCHEDULED_PROCEDURE_STEP_START_DATE_AND_TIME,
                        cl.getOptionValue("D"));
            }
            if (cl.hasOption("workitem")) {
                dcmgpwl.addCodeValueAndScheme(Tag.SCHEDULED_WORKITEM_CODE_SEQUENCE,
                        cl.getOptionValues("workitem"));
            }
            if (cl.hasOption("application")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.SCHEDULED_PROCESSING_APPLICATIONS_CODE_SEQUENCE,
                        cl.getOptionValues("application"));
            }
            if (cl.hasOption("station")) {
                dcmgpwl.addCodeValueAndScheme(Tag.SCHEDULED_STATION_NAME_CODE_SEQUENCE,
                        cl.getOptionValues("station"));
            }
            if (cl.hasOption("class")) {
                dcmgpwl.addCodeValueAndScheme(Tag.SCHEDULED_STATION_CLASS_CODE_SEQUENCE,
                        cl.getOptionValues("class"));
            }
            if (cl.hasOption("location")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.SCHEDULED_STATION_GEOGRAPHIC_LOCATION_CODE_SEQUENCE,
                        cl.getOptionValues("location"));
            }
            if (cl.hasOption("performer")) {
                dcmgpwl.setScheduledHumanPerformerCodeValueAndScheme(
                        cl.getOptionValues("performer"));
            }
        }
  
        dcmgpwl.configureTransferCapability(
                cl.hasOption("metasop") ? METASOP_CUID : SOP_CUIDS,
                cl.hasOption("ivrle") ? IVRLE_TS : LE_TS);
        
        long t1 = System.currentTimeMillis();
        try {
            dcmgpwl.open();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to establish association:");
            e.printStackTrace(System.err);
            System.exit(2);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Connected to " + remoteAE + " in "
                + ((t2 - t1) / 1000F) + "s");

        try {
            if (cl.hasOption("action")) {
                dcmgpwl.action(cl.getOptionValues("action")[0]);
                long t3 = System.currentTimeMillis();
                System.out.println("Modified GP-SPS in "
                        + ((t3 - t2) / 1000F) + "s");
            } else if (cl.hasOption("createpps")) {
                dcmgpwl.createpps(cl.getOptionValue("createpps"));
                long t3 = System.currentTimeMillis();
                System.out.println("Create GP-PPS in "
                        + ((t3 - t2) / 1000F) + "s");
            } else if (cl.hasOption("setpps")) {
                dcmgpwl.setpps(cl.getOptionValue("setpps"));
                long t3 = System.currentTimeMillis();
                System.out.println("Update GP-PPS in "
                        + ((t3 - t2) / 1000F) + "s");
            } else {
                int n = dcmgpwl.query();
                long t3 = System.currentTimeMillis();
                System.out.println("Received " + n  + " matching entries in " 
                        + ((t3 - t2) / 1000F) + "s");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            dcmgpwl.close();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Released connection to " + remoteAE);
    }

    private static void addOutput(DcmGPWL dcmgpwl, File file) {
        if (file.isDirectory()) {
            String[] ss = file.list();
            for (int i = 0; i < ss.length; i++) {
                addOutput(dcmgpwl, new File(file, ss[i]));
            }
        } else {
            DicomInputStream din = null;
            try {
                din = new DicomInputStream(file);
                din.setHandler(new StopTagInputHandler(Tag.STUDY_ID));
                dcmgpwl.addOutput(din.readDicomObject());
            } catch (IOException e) {
                System.out.println("WARNING: Failed to read " + file + ": "
                        + e.getMessage());
            } finally {
                if (din != null) {
                    try {
                        din.close();
                    } catch (IOException ignore) {}
                }
            }
        }        
    }

    private static int toTag(String nameOrHex) {
        try {
            return (int) Long.parseLong(nameOrHex, 16);
        } catch (NumberFormatException e) {
            return Tag.forName(nameOrHex);
        }
    }    
   
    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        OptionBuilder.withArgName("aet[@host]");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set AET and local address of local Application Entity, use " +
                "ANONYMOUS and pick up any valid local address to bind the " +
                "socket by default");
        opts.addOption(OptionBuilder.create("L"));
        
        opts.addOption("metasop", false,
                "offer General Purpose Worklist Management Meta SOP Class.");
        opts.addOption("ivrle", false,
                "offer only Implicit VR Little Endian Transfer Syntax.");
        
        opts.addOption("pdv1", false,
                "send only one PDV in one P-Data-TF PDU, pack command and data " +
                "PDV in one P-DATA-TF PDU by default.");
        opts.addOption("tcpdelay", false,
                "set TCP_NODELAY socket option to false, true by default");
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for TCP connect, no timeout by default");
        opts.addOption(OptionBuilder.create("connectTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("delay in ms for Socket close after sending A-ABORT, 50ms by default");
        opts.addOption(OptionBuilder.create("soclosedelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("period in ms to check for outstanding DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("reaper"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for receiving DIMSE-RSP, 60s by default");
        opts.addOption(OptionBuilder.create("rspTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
        opts.addOption(OptionBuilder.create("acceptTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("timeout in ms for receiving A-RELEASE-RP, 5s by default");
        opts.addOption(OptionBuilder.create("releaseTO"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("rcvpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("sndpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("set SO_RCVBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sorcvbuf"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_SNDBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sosndbuf"));

        OptionBuilder.withArgName("status");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "match/set GP-SPS/GP-PPS to specified <status>");
        opts.addOption(OptionBuilder.create("status"));
        
        OptionBuilder.withArgName("iuid:tuid");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "modify status of GP-SPS with SOP Instance UID <iuid> " +
                "using Transaction UID <tuid>.");
        opts.addOption(OptionBuilder.create("action"));

        OptionBuilder.withArgName("iuid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "create GP-PPS with SOP Instance UID <iuid>.");
        opts.addOption(OptionBuilder.create("createpps"));

        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "retrieve AET used in SOP references in Output Information" +
                "Sequence in created or updated GP-PPS.");
        opts.addOption(OptionBuilder.create("retrieve"));
        
        OptionBuilder.withArgName("iuid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "update GP-PPS with SOP Instance UID <iuid>.");
        opts.addOption(OptionBuilder.create("setpps"));

        OptionBuilder.withArgName("iuid:tuid");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "reference GP-SPS with SOP Instance UID <iuid> and " +
                "Transaction UID <tuid> in created GP-PPS.");
        opts.addOption(OptionBuilder.create("refsps"));
        
        OptionBuilder.withArgName("attr=value");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription(
                "specify matching key or PPS attribute. attr can be specified " +
                "by name or tag value (in hex), e.g. PATIENTS_NAME or 00100010.");
        opts.addOption(OptionBuilder.create("A"));
        
        OptionBuilder.withArgName("datetime");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify matching SPS start datetime (range)");
        opts.addOption(OptionBuilder.create("D"));        

        OptionBuilder.withArgName("attr=value");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription(
                "specify matching Referenced Request key or PPS attribute. " +
                "attr can be specified by name or tag value (in hex)");
        opts.addOption(OptionBuilder.create("rqA"));

        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Workitem Code");
        opts.addOption(OptionBuilder.create("workitem"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Processing Application Code");
        opts.addOption(OptionBuilder.create("application"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Name Code");
        opts.addOption(OptionBuilder.create("station"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Class Code");
        opts.addOption(OptionBuilder.create("class"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Geographic Location Code");
        opts.addOption(OptionBuilder.create("location"));
        
        OptionBuilder.withArgName("code:scheme:[name]");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Human Performer Code");
        opts.addOption(OptionBuilder.create("performer"));

        OptionBuilder.withArgName("code:scheme:name");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify Actual Human Performer Code");
        opts.addOption(OptionBuilder.create("perfcode"));

        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify Actual Human Performer Name");
        opts.addOption(OptionBuilder.create("perfname"));
        
        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify Actual Human Performer Organisation");
        opts.addOption(OptionBuilder.create("perforg"));
        
        OptionBuilder.withArgName("num");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "cancel query after receive of specified number of responses, " +
                "no cancel by default");
        opts.addOption(OptionBuilder.create("C"));

        OptionBuilder.withArgName("dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "store query results in DICOM files in directory <dir>.");
        opts.addOption(OptionBuilder.create("o"));

        opts.addOption("lowprior", false,
                "LOW priority of the C-FIND operation, MEDIUM by default");
        opts.addOption("highprior", false,
                "HIGH priority of the C-FIND operation, MEDIUM by default");
        
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmgpwl: " + e.getMessage());
        }
        if (cl.hasOption('V')) {
            Package p = DcmGPWL.class.getPackage();
            System.out.println("dcmgpwl v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() < 1) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }

        return cl;
    }

    private static int toPort(String port) {
        return port != null ? parseInt(port, "illegal port number", 1, 0xffff)
                : 104;
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
        }
        exit(errPrompt);
        throw new RuntimeException();
    }

    private static String[] split(String s, char delim) {
        String[] s2 = { s, null };
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }
    
    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmgpwl -h' for more information.");
        System.exit(1);
    }

}
