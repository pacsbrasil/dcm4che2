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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
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
    private static final String USAGE = "dcmgpwl [Options] <aet>[@<host>[:<port>]]";
    private static final String DESCRIPTION = 
            "Query specified remote Application Entity (=General Purpose Worklist SCP) " + 
            "If <port> is not specified, DICOM default port 104 is assumed. " +
            "If also no <host> is specified localhost is assumed.\n" +
            "Options:";
    private static final String EXAMPLE = 
            "\nExample: dcmgpwl GPWLSCP@localhost:11112 -scheduled\n" +
            "=> Query Application Entity GPWLSCP listening on local port 11112 for " +
            "all scheduled GP-SPS";

    private static final String[] SOP_CUIDS = {
        UID.GeneralPurposeWorklistInformationModelFIND,
        UID.GeneralPurposeScheduledProcedureStepSOPClass };
    
    private static final String[] METASOP_CUID = {
        UID.GeneralPurposeWorklistManagementMetaSOPClass };

    private static final int[] RETURN_KEYS = {
        Tag.SOPClassUID,
        Tag.SOPInstanceUID,
        Tag.PatientsName,
        Tag.PatientID,
        Tag.PatientsBirthDate,
        Tag.PatientsSex,
        Tag.StudyInstanceUID,
        Tag.ScheduledProcedureStepID,
        Tag.GeneralPurposeScheduledProcedureStepStatus,
        Tag.GeneralPurposeScheduledProcedureStepPriority,
        Tag.ScheduledProcedureStepStartDateandTime,
        Tag.MultipleCopiesFlag,
        Tag.ScheduledProcedureStepModificationDateandTime,
        Tag.ExpectedCompletionDateandTime,
        Tag.InputAvailabilityFlag,
    };

    private static final int[] REQUEST_RETURN_KEYS = {
        Tag.AccessionNumber,
        Tag.StudyInstanceUID,
        Tag.RequestingPhysician,
        Tag.RequestedProcedureDescription,
        Tag.RequestedProcedureID,
    };

    private static final int[] RETURN_SQ_KEYS = {
        Tag.ReferencedPerformedProcedureStepSequence,
        Tag.ScheduledProcessingApplicationsCodeSequence,
        Tag.ResultingGeneralPurposePerformedProcedureStepsSequence,
        Tag.ScheduledWorkitemCodeSequence,
        Tag.InputInformationSequence,
        Tag.RelevantInformationSequence,
        Tag.ScheduledStationNameCodeSequence,
        Tag.ScheduledStationClassCodeSequence,
        Tag.ScheduledStationGeographicLocationCodeSequence,
        Tag.ScheduledHumanPerformersSequence,
        Tag.ActualHumanPerformersSequence,
        Tag.ReferencedRequestSequence,
    };
    
    private static final String[] IVRLE_TS = {
        UID.ImplicitVRLittleEndian };
    
    private static final String[] LE_TS = {
        UID.ExplicitVRLittleEndian, 
        UID.ImplicitVRLittleEndian };  
    
    private Executor executor = new NewThreadExecutor("DCMGPWL");
    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private NetworkConnection remoteConn = new NetworkConnection();
    private Device device = new Device("DCMGPWL");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private NetworkConnection conn = new NetworkConnection();
    private Association assoc;
    private int priority = 0;
    private int cancelAfter = Integer.MAX_VALUE;
    private final DicomObject keys = new BasicDicomObject();
    private final DicomObject rqKeys = new BasicDicomObject();
    private final DicomObject actionAttrs = new BasicDicomObject();
    
    public DcmGPWL() {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle("DCMGPWL");
        for (int i = 0; i < RETURN_KEYS.length; i++) {
            keys.putNull(RETURN_KEYS[i], null);
        }
        for (int i = 0; i < RETURN_SQ_KEYS.length; i++) {
            keys.putNestedDicomObject(RETURN_SQ_KEYS[i],
                    new BasicDicomObject());
        }
        keys.putNestedDicomObject(Tag.ReferencedRequestSequence, rqKeys);
        for (int i = 0; i < REQUEST_RETURN_KEYS.length; i++) {
            rqKeys.putNull(REQUEST_RETURN_KEYS[i], null);
        }
        rqKeys.putNestedDicomObject(Tag.RequestedProcedureCodeSequence,
                new BasicDicomObject());
        actionAttrs.putString(Tag.GeneralPurposeScheduledProcedureStepStatus,
                VR.CS, "IN PROGRESS");        

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
    
    public void addKey(int tag, String value) {
        keys.putString(tag, null, value);
    }
 
    public void addRefRequestKey(int tag, String value) {
        rqKeys.putString(tag, null, value);
    }
    
    public void addCodeValueAndScheme(int tag, String[] code) {
        setCodeValueAndScheme(keys.getNestedDicomObject(tag), code);
    }

    private void setCodeValueAndScheme(DicomObject codeItem, String[] code) {
        codeItem.putString(Tag.CodeValue, VR.SH, code[0]);
        codeItem.putString(Tag.CodingSchemeDesignator, VR.SH, code[1]);
        codeItem.putNull(Tag.CodeMeaning, VR.LO);
    }

    public void setScheduledHumanPerformerCodeValueAndScheme(String[] valueAndScheme) {
        DicomObject performerKeys = keys.getNestedDicomObject(
                Tag.ScheduledHumanPerformersSequence);
        DicomObject codeItem = new BasicDicomObject();
        setCodeValueAndScheme(codeItem, valueAndScheme);        
        performerKeys.putNestedDicomObject(Tag.HumanPerformerCodeSequence, codeItem);
        performerKeys.putNull(Tag.HumanPerformersName, VR.PN);
        performerKeys.putNull(Tag.HumanPerformersOrganization, VR.LO);
    }
    
    public void setActualHumanPerformer(String[] code, String name, String org) {
        DicomObject performerKeys = new BasicDicomObject();
        actionAttrs.putNestedDicomObject(Tag.ActualHumanPerformersSequence,
                performerKeys);
        BasicDicomObject codeItem = new BasicDicomObject();
        codeItem.putString(Tag.CodeValue, VR.SH, code[0]);
        codeItem.putString(Tag.CodingSchemeDesignator, VR.SH, code[1]);
        codeItem.putString(Tag.CodeMeaning, VR.LO, code[2]);
        performerKeys.putNestedDicomObject(Tag.HumanPerformerCodeSequence, codeItem);
        if (name != null) {
            performerKeys.putString(Tag.HumanPerformersName, VR.PN, name);
        }
        if (org != null) {
            performerKeys.putString(Tag.HumanPerformersOrganization,VR.LO, org);
        }
    }
    
    public void setStatus(String status) {
        keys.putString(Tag.GeneralPurposeScheduledProcedureStepStatus,
                VR.CS, status);        
        actionAttrs.putString(Tag.GeneralPurposeScheduledProcedureStepStatus,
                VR.CS, status);        
    }    
    
    public void configureTransferCapability(String[] cuids, String[] ts) {
        TransferCapability[] tc = new TransferCapability[cuids.length];
        for (int i = 0; i < tc.length; i++) {
            tc[i] = new TransferCapability(cuids[i], ts, TransferCapability.SCU);
        }
        ae.setTransferCapability(tc);       
    }

    public void setTransactionUID(String uid) {
        actionAttrs.putString(Tag.TransactionUID, VR.UI, uid);
    }
    
    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    public List query() throws IOException, InterruptedException {
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(
                UID.GeneralPurposeWorklistManagementMetaSOPClass);
        if (tc == null) {
            tc = assoc.getTransferCapabilityAsSCU(
                    UID.GeneralPurposeWorklistInformationModelFIND);
            if (tc == null) {
                throw new NoPresentationContextException(
                        "General Purpose Worklist not supported by " 
                        + remoteAE.getAETitle());
            }
        }
        System.out.println("Send Query Request:");
        System.out.println(keys.toString());
        DimseRSP rsp = assoc.cfind(UID.GeneralPurposeWorklistInformationModelFIND,
                priority, keys, tc.getTransferSyntax()[0], cancelAfter);
        List result = new ArrayList();
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                result.add(data);
                System.out.println("\nReceived Query Response #"
                        + result.size() + ":");
                System.out.println(data.toString());
            }
        }
        return result;
    }
    
    private void action(String iuid) throws IOException, InterruptedException {
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(
                UID.GeneralPurposeWorklistManagementMetaSOPClass);
        if (tc == null) {
            tc = assoc.getTransferCapabilityAsSCU(
                    UID.GeneralPurposeScheduledProcedureStepSOPClass);
            if (tc == null) {
                throw new NoPresentationContextException(
                        "General Purpose Scheduled Procedure Step SOP Class " +
                        "not supported by " + remoteAE.getAETitle());
            }
        }
        System.out.println("Send GP-SPS Modify Request:");
        System.out.println(actionAttrs.toString());
        DimseRSP rsp = assoc.naction(
                UID.GeneralPurposeScheduledProcedureStepSOPClass, iuid,
                1, actionAttrs, tc.getTransferSyntax()[0]);
        rsp.next();
        DicomObject cmd = rsp.getCommand();
        System.out.println("\nReceived GP-PS Modify Response:");
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
            dcmgpwl.setLocalHost(toHostname(callingAETHost[1]));
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
        dcmgpwl.setTcpNoDelay(cl.hasOption("tcpnodelay"));
        
        String[] action = null;
        if (cl.hasOption("scheduled")) {
            dcmgpwl.setStatus("SCHEDULED");
        } else if (cl.hasOption("inprogress")) {
            dcmgpwl.setStatus("IN PROGRESS");
        } else if (cl.hasOption("suspended")) {
            dcmgpwl.setStatus("SUSPENDED");
        } else if (cl.hasOption("completed")) {
            dcmgpwl.setStatus("COMPLETED");
        } else if (cl.hasOption("discontinued")) {
            dcmgpwl.setStatus("DISCONTINUED");
        }
        
        if (cl.hasOption("action")) {
            action = cl.getOptionValues("action");
            dcmgpwl.setTransactionUID(action[1]);
            if (cl.hasOption("performer")) {
                dcmgpwl.setActualHumanPerformer(cl.getOptionValues("performer"),
                        cl.getOptionValue("perfname"), cl.getOptionValue("perforg"));
            }
        } else {
            if (cl.hasOption("C"))
                dcmgpwl.setCancelAfter(parseInt(cl.getOptionValue("C"),
                        "illegal argument of option -C", 1, Integer.MAX_VALUE));
            if (cl.hasOption("lowprior"))
                dcmgpwl.setPriority(CommandUtils.LOW);
            if (cl.hasOption("highprior"))
                dcmgpwl.setPriority(CommandUtils.HIGH);
            if (cl.hasOption("q")) {
                String[] matchingKeys = cl.getOptionValues("q");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addKey(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("d")) {
                dcmgpwl.addKey(Tag.ScheduledProcedureStepStartDateandTime,
                        cl.getOptionValue("d"));
            }
            if (cl.hasOption("r")) {
                String[] returnKeys = cl.getOptionValues("r");
                for (int i = 0; i < returnKeys.length; i++)
                    dcmgpwl.addKey(toTag(returnKeys[i]), null);
            }
            if (cl.hasOption("Q")) {
                String[] matchingKeys = cl.getOptionValues("Q");
                for (int i = 1; i < matchingKeys.length; i++, i++)
                    dcmgpwl.addRefRequestKey(toTag(matchingKeys[i - 1]), matchingKeys[i]);
            }
            if (cl.hasOption("R")) {
                String[] returnKeys = cl.getOptionValues("R");
                for (int i = 0; i < returnKeys.length; i++)
                    dcmgpwl.addRefRequestKey(toTag(returnKeys[i]), null);
            }
            if (cl.hasOption("workitem")) {
                dcmgpwl.addCodeValueAndScheme(Tag.ScheduledWorkitemCodeSequence,
                        cl.getOptionValues("workitem"));
            }
            if (cl.hasOption("application")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.ScheduledProcessingApplicationsCodeSequence,
                        cl.getOptionValues("application"));
            }
            if (cl.hasOption("station")) {
                dcmgpwl.addCodeValueAndScheme(Tag.ScheduledStationNameCodeSequence,
                        cl.getOptionValues("station"));
            }
            if (cl.hasOption("class")) {
                dcmgpwl.addCodeValueAndScheme(Tag.ScheduledStationClassCodeSequence,
                        cl.getOptionValues("class"));
            }
            if (cl.hasOption("location")) {
                dcmgpwl.addCodeValueAndScheme(
                        Tag.ScheduledStationGeographicLocationCodeSequence,
                        cl.getOptionValues("location"));
            }
            if (cl.hasOption("sperformer")) {
                dcmgpwl.setScheduledHumanPerformerCodeValueAndScheme(
                        cl.getOptionValues("sperformer"));
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
            if (action == null) {
                List result = dcmgpwl.query();
                long t3 = System.currentTimeMillis();
                System.out.println("Received " + result.size()
                        + " matching entries in " + ((t3 - t2) / 1000F) + "s");
            } else {
                dcmgpwl.action(action[0]);
                long t3 = System.currentTimeMillis();
                System.out.println("Modify GP_-PS in " + ((t3 - t2) / 1000F) + "s");
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
        opts.addOption("tcpnodelay", false,
                "set TCP_NODELAY socket option to true, false by default");
        
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

        OptionGroup gr = new OptionGroup();       
        OptionBuilder.withDescription(
            "match SCHEDULED GP-SPS or (if -a) set GP-SPS status to SCHEDULED.");
        gr.addOption(OptionBuilder.create("scheduled"));
        OptionBuilder.withDescription(
            "match IN PROGRESS GP-SPS or (if -a) set GP-SPS status to IN PROGRESS.");
        gr.addOption(OptionBuilder.create("inprogress"));
        OptionBuilder.withDescription(
            "match SUSPENDED GP-SPS or (if -a) set GP-SPS status to SUSPENDED.");
        gr.addOption(OptionBuilder.create("suspended"));
        OptionBuilder.withDescription(
            "match COMPLETED GP-SPS or (if -a) set GP-SPS status to COMPLETED.");
        gr.addOption(OptionBuilder.create("completed"));
        OptionBuilder.withDescription(
            "match DISCONTINUED GP-SPS or (if -a) set GP-SPS status to DISCONTINUED.");
        gr.addOption(OptionBuilder.create("discontinued"));

        opts.addOptionGroup(gr);

        OptionBuilder.withArgName("iuid:tuid");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "modify status of GP-SPS with SOP Instance UID <iuid> " +
                "using Transaction UID <tuid>.");
        opts.addOption(OptionBuilder.create("action"));
        
        OptionBuilder.withArgName("attr=value");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription(
                "specify matching key. attr can be specified by name or tag " +
                "value (in hex), e.g. PatientsName or 00100010.");
        opts.addOption(OptionBuilder.create("q"));
        
        OptionBuilder.withArgName("datetime");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify matching SPS start datetime (range)");
        opts.addOption(OptionBuilder.create("d"));        

        OptionBuilder.withArgName("attr");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify additional return key. attr can be specified by name " +
                "or tag value (in hex).");
        opts.addOption(OptionBuilder.create("r"));

        OptionBuilder.withArgName("attr=value");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription(
                "specify matching Referenced Request key. attr can be " +
                "specified by name or tag value (in hex)");
        opts.addOption(OptionBuilder.create("Q"));

        OptionBuilder.withArgName("attr");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "specify additional return Referenced Request key. attr can " +
                "be specified by name or tag value (in hex).");
        opts.addOption(OptionBuilder.create("R"));

        OptionBuilder.withArgName("code:scheme");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Workitem Code");
        opts.addOption(OptionBuilder.create("workitem"));
        
        OptionBuilder.withArgName("code:scheme");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Processing Application Code");
        opts.addOption(OptionBuilder.create("application"));
        
        OptionBuilder.withArgName("code:scheme");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Name Code");
        opts.addOption(OptionBuilder.create("station"));
        
        OptionBuilder.withArgName("code:scheme");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Class Code");
        opts.addOption(OptionBuilder.create("class"));
        
        OptionBuilder.withArgName("code:scheme");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Station Geographic Location Code");
        opts.addOption(OptionBuilder.create("location"));
        
        OptionBuilder.withArgName("code:scheme");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify matching Scheduled Human Performer Code");
        opts.addOption(OptionBuilder.create("sperformer"));

        OptionBuilder.withArgName("code:scheme:name");
        OptionBuilder.hasArgs(3);
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription(
                "specify Scheduled Actual Human Performer Code");
        opts.addOption(OptionBuilder.create("performer"));

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
            System.out.println("dcmqr v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() != 1) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }

        return cl;
    }

    private static String toHostname(String host) {
        return host != null ? host : "127.0.0.1";
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
