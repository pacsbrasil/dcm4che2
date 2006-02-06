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

package org.dcm4che2.tool.dcmqr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 13, 2005
 */
public class DcmQR
{
    private static final int KB = 1024;
    private static final String USAGE = "dcmqr [Options] <aet>[@<host>[:<port>]]";
    private static final String DESCRIPTION = 
            "Query specified remote Application Entity (=Query/Retrieve SCP) " +
            "and optional (s. option -m) retrieve instances of matching entities. " + 
            "If <port> is not specified, DICOM default port 104 is assumed. " +
            "If also no <host> is specified localhost is assumed.\n" +
            "Options:";
    private static final String EXAMPLE = 
            "\nExample: dcmqr QRSCP@localhost:11112 -fStudyDate=20060204 -m STORESCP\n" +
            "=> Query Application Entity QRSCP listening on local port 11112 for " +
            "studies from Feb 4, 2006 and retrieve instances of matching studies to " +
            "Application Entity STORESCP.";

    private static final int PATIENT = 0;
    private static final int STUDY = 1;
    private static final int SERIES = 2;
    private static final int IMAGE = 3;
    private static final String[] QRLEVEL = {
        "PATIENT", 
        "STUDY", 
        "SERIES", 
        "IMAGE"
    };
    private static final String[] PATIENT_LEVEL_FIND_CUID = {
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFIND
    };
    private static final String[] STUDY_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFIND
    };
    private static final String[] SERIES_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND,
    };
    private static final String[][] FIND_CUID = {
        PATIENT_LEVEL_FIND_CUID,
        STUDY_LEVEL_FIND_CUID,
        SERIES_LEVEL_FIND_CUID,
        SERIES_LEVEL_FIND_CUID
    };
    private static final String[] PATIENT_LEVEL_MOVE_CUID = {
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVE
    };
    private static final String[] STUDY_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVE
    };
    private static final String[] SERIES_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE
    };
    private static final String[][] MOVE_CUID = {
        PATIENT_LEVEL_MOVE_CUID,
        STUDY_LEVEL_MOVE_CUID,
        SERIES_LEVEL_MOVE_CUID,
        SERIES_LEVEL_MOVE_CUID
    };
    private static final int[] PATIENT_RETURN_KEYS = {
        Tag.PatientsName,
        Tag.PatientID,
        Tag.PatientsBirthDate,
        Tag.PatientsSex,
        Tag.NumberofPatientRelatedStudies,
        Tag.NumberofPatientRelatedSeries,
        Tag.NumberofPatientRelatedInstances
    };
    private static final int[] STUDY_RETURN_KEYS = {
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.StudyID,
        Tag.StudyInstanceUID,
        Tag.NumberofStudyRelatedSeries,
        Tag.NumberofStudyRelatedInstances
    };
    private static final int[] SERIES_RETURN_KEYS = {
        Tag.Modality,
        Tag.SeriesNumber,
        Tag.SeriesInstanceUID,
        Tag.NumberofSeriesRelatedInstances
    };
    private static final int[] INSTANCE_RETURN_KEYS = {
        Tag.InstanceNumber,
        Tag.SOPClassUID,
        Tag.SOPInstanceUID,
    };
    private static final int[][] RETURN_KEYS = {
        PATIENT_RETURN_KEYS,
        STUDY_RETURN_KEYS,
        SERIES_RETURN_KEYS,
        INSTANCE_RETURN_KEYS
    };
    private static final int[] MOVE_KEYS = {
        Tag.QueryRetrieveLevel,
        Tag.PatientID,
        Tag.StudyInstanceUID,
        Tag.SeriesInstanceUID,
        Tag.SOPInstanceUID,
    };
    private static final String[] DEF_TS = { UID.ImplicitVRLittleEndian };
    private static final String[] EMPTY_STRING = {};

    private Executor executor = new NewThreadExecutor("DCMQR");
    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private NetworkConnection remoteConn = new NetworkConnection();
    private Device device = new Device("DCMQR");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private NetworkConnection conn = new NetworkConnection();
    private Association assoc;
    private int priority = 0;
    private String moveDest;
    private String[] tsuids = DEF_TS;
    private int qrlevel = STUDY;
    private DicomObject filter = new BasicDicomObject();
    private int limit = Integer.MAX_VALUE;

    public DcmQR()
    {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(
                new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle("DCMQR");
    }

    public final void setLocalHost(String hostname)
    {
        conn.setHostname(hostname);
    }

    public final void setRemoteHost(String hostname)
    {
        remoteConn.setHostname(hostname);
    }

    public final void setRemotePort(int port)
    {
        remoteConn.setPort(port);
    }

    public final void setCalledAET(String called)
    {
        remoteAE.setAETitle(called);
    }

    public final void setCalling(String calling)
    {
        ae.setAETitle(calling);
    }

    public final void setPriority(int priority)
    {
        this.priority = priority;
    }

    public final void setConnectTimeout(int connectTimeout)
    {
        conn.setConnectTimeout(connectTimeout);
    }
    
    public final void setMaxPDULengthReceive(int maxPDULength)
    {
        ae.setMaxPDULengthReceive(maxPDULength);
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked)
    {
        ae.setMaxOpsInvoked(maxOpsInvoked);
    }

    public final void setPackPDV(boolean packPDV)
    {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period)
    {
        device.setAssociationReaperPeriod(period);
    }

    public final void setDimseRspTimeout(int timeout)
    {
        ae.setDimseRspTimeout(timeout);
    }
    
    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        conn.setTcpNoDelay(tcpNoDelay);
    }

    public final void setAcceptTimeout(int timeout)
    {
        conn.setAcceptTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout)
    {
        conn.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout)
    {
        conn.setSocketCloseDelay(timeout);
    }

    public final void setMaxPDULengthSend(int maxPDULength)
    {
        ae.setMaxPDULengthSend(maxPDULength);
    }

    public final void setReceiveBufferSize(int bufferSize)
    {
        conn.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize)
    {
        conn.setSendBufferSize(bufferSize);
    }
    
    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        Option localAddr = new Option("L", "local", true,
                "set AET and local address of local Application Entity, use " +
                "ANONYMOUS and pick up any valid local address to bind the " +
                "socket by default");
        localAddr.setArgName("calling[@host]");
        opts.addOption(localAddr);
        Option maxOpsInvoked = new Option("a", "async", true,
                "maximum number of outstanding C-MOVE-RQ it may invoke " +
                "asynchronously, 1 by default.");
        maxOpsInvoked.setArgName("max-ops");
        opts.addOption(maxOpsInvoked);
        opts.addOption(" ", "pack-pdv", false, 
                "pack command and data PDV in one P-DATA-TF PDU, " +
                "send only one PDV in one P-Data-TF PDU by default.");
        opts.addOption(" ", "tcp-no-delay", false, 
                "set TCP_NODELAY socket option to true, false by default");
        Option conTimeout = new Option(" ", "connect-timeout", true,
                "timeout in ms for TCP connect, no timeout by default");
        conTimeout.setArgName("timeout");
        opts.addOption(conTimeout);
        Option closeDelay = new Option(" ", "close-delay", true,
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        closeDelay.setArgName("delay");
        opts.addOption(closeDelay);
        Option checkPeriod = new Option(" ", "reaper-period", true,
                "period in ms to check for outstanding DIMSE-RSP, 10s by default");
        checkPeriod.setArgName("period");
        Option rspTimeout = new Option(" ", "rsp-timeout", true,
                "timeout in ms for receiving DIMSE-RSP, 60s by default");
        rspTimeout.setArgName("timeout");
        opts.addOption(rspTimeout);
        Option acTimeout = new Option(" ", "accept-timeout", true,
                "timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
        acTimeout.setArgName("timeout");
        opts.addOption(acTimeout);
        Option rpTimeout = new Option(" ", "release-timeout", true,
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        rpTimeout.setArgName("timeout");
        opts.addOption(rpTimeout);
        Option rcvPduLen = new Option(" ", "rcv-pdu-len", true,
                "maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        rcvPduLen.setArgName("max-len");
        opts.addOption(rcvPduLen);
        Option sndPduLen = new Option(" ", "snd-pdu-len", true,
                "maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        sndPduLen.setArgName("max-len");
        opts.addOption(sndPduLen);
        Option soRcvBufSize = new Option(" ", "so-rcv-buf", true,
                "set SO_RCVBUF socket option to specified value in KB");
        soRcvBufSize.setArgName("size");
        opts.addOption(soRcvBufSize);
        Option soSndBufSize = new Option(" ", "so-snd-buf", true,
                "set SO_SNDBUF socket option to specified value in KB");
        soSndBufSize.setArgName("size");
        opts.addOption(soSndBufSize);
        OptionGroup qrlevel = new OptionGroup();
        qrlevel.addOption(new Option("p", "patient", false,
                "perform patient level query, multiple exclusive with -s and -i, " +
                "perform study level query by default."));
        qrlevel.addOption(new Option("s", "series", false,
                "perform series level query, multiple exclusive with -p and -i, " +
                "perform study level query by default."));
        qrlevel.addOption(new Option("i", "image", false,
                "perform instance level query, multiple exclusive with -p and -s, " +
                "perform study level query by default."));
        opts.addOptionGroup(qrlevel);
        Option filter = new Option("f", "filter", true,
                "specify query filter element. attr can be specified by " +
                "name or tag value (in hex), e.g. PatientsName or 00100010.");
        filter.setArgName("attr=value");
        filter.setValueSeparator('=');
        opts.addOption(filter);
        Option limit = new Option("l", "limit", true,
                "cancel query after receive of specified number of responses," +
                " no cancel by default");
        limit.setArgName("max");
        opts.addOption(limit);
        Option move = new Option("d", "dest", true,
                "retrieve matching objects to specified destination.");
        move.setArgName("aet");
        opts.addOption(move);
        opts.addOption(" ", "low-prior", false, 
            "LOW priority of the C-FIND/C-MOVE operation, MEDIUM by default");
        opts.addOption(" ", "high-prior", false,
            "HIGH priority of the C-FIND/C-MOVE operation, MEDIUM by default");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try
        {
            cl = new PosixParser().parse(opts, args);
        }
        catch (ParseException e)
        {
            exit("dcmqr: " + e.getMessage());
        }
        if (cl.hasOption('V'))
        {
            Package p = DcmQR.class.getPackage();
            System.out.println("dcmqr v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() != 1)
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }

        return cl;
    }

    public static void main(String[] args)
    {
        CommandLine cl = parse(args);
        DcmQR dcmqr = new DcmQR();
        final List argList = cl.getArgList();
        String remoteAE = (String) argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmqr.setCalledAET(calledAETAddress[0]);
        if (calledAETAddress[1] == null)
        {
            dcmqr.setRemoteHost("127.0.0.1");
            dcmqr.setRemotePort(104);
        }
        else
        {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmqr.setRemoteHost(hostPort[0]);
            dcmqr.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L"))
        {
            String localAE = (String) cl.getOptionValue("L");
            String[] callingAETHost = split(localAE, '@');
            dcmqr.setCalling(callingAETHost[0]);
            dcmqr.setLocalHost(toHostname(callingAETHost[1]));
        }
        if (cl.hasOption("connect-timeout"))
            dcmqr.setConnectTimeout(
                    parseInt(cl.getOptionValue("connect-timeout"),
                    "illegal argument of option --connect-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("reaper-period"))
            dcmqr.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue("reaper-period"),
                    "illegal argument of option --reaper-period", 1, Integer.MAX_VALUE));
        if (cl.hasOption("rsp-timeout"))
            dcmqr.setDimseRspTimeout(
                    parseInt(cl.getOptionValue("rsp-timeout"),
                    "illegal argument of option --rsp-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("accept-timeout"))
            dcmqr.setAcceptTimeout(
                    parseInt(cl.getOptionValue("accept-timeout"),
                    "illegal argument of option --accept-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("release-timeout"))
            dcmqr.setReleaseTimeout(
                    parseInt(cl.getOptionValue("release-timeout"),
                    "illegal argument of option --release-timeout", 1, Integer.MAX_VALUE));
        if (cl.hasOption("close-delay"))
            dcmqr.setSocketCloseDelay(
                    parseInt(cl.getOptionValue("close-delay"),
                    "illegal argument of option --close-delay", 1, 10000));
        if (cl.hasOption("rcv-pdu-len"))
            dcmqr.setMaxPDULengthReceive(
                    parseInt(cl.getOptionValue("rcv-pdu-len"),
                    "illegal argument of option --rcv-pdu-len", 1, 10000) * KB);
        if (cl.hasOption("snd-pdu-len"))
            dcmqr.setMaxPDULengthSend(
                    parseInt(cl.getOptionValue("snd-pdu-len"),
                    "illegal argument of option --snd-pdu-len", 1, 10000) * KB);
        if (cl.hasOption("so-snd-buf"))
            dcmqr.setSendBufferSize(
                    parseInt(cl.getOptionValue("so-snd-buf"),
                    "illegal argument of option --so-snd-buf", 1, 10000) * KB);
        if (cl.hasOption("so-rcv-buf"))
            dcmqr.setReceiveBufferSize(
                    parseInt(cl.getOptionValue("so-rcv-buf"),
                    "illegal argument of option --so-rcv-buf", 1, 10000) * KB);
        dcmqr.setPackPDV(cl.hasOption("pack-pdv"));
        dcmqr.setTcpNoDelay(cl.hasOption("tcp-no-delay"));
        dcmqr.setMaxOpsInvoked(cl.hasOption("a")
                ? zeroAsMaxInt(parseInt(
                    cl.getOptionValue("a"), "illegal argument of option -a", 0, 0xffff))
                : 1);        
        if (cl.hasOption("l"))
            dcmqr.setLimit(parseInt(cl.getOptionValue("l"),
                    "illegal argument of option -l", 1, Integer.MAX_VALUE));
        if (cl.hasOption("low-prior"))
            dcmqr.setPriority(CommandUtils.LOW);
        if (cl.hasOption("high-prior"))
            dcmqr.setPriority(CommandUtils.HIGH);
        if (cl.hasOption("d"))
            dcmqr.setMoveDest((String) cl.getOptionValue("d"));
        if (cl.hasOption("p"))
            dcmqr.setQueryLevel(PATIENT);
        else if (cl.hasOption("s"))
            dcmqr.setQueryLevel(SERIES);
        else if (cl.hasOption("i"))
            dcmqr.setQueryLevel(IMAGE);
        else
            dcmqr.setQueryLevel(STUDY);

        if (cl.hasOption("f"))
        {
            String[] filters = cl.getOptionValues("f");
            for (int i = 1; i < filters.length; i++,i++)
                dcmqr.addFilter(toTag(filters[i-1]), filters[i]);
        }
         
        dcmqr.configureTransferCapability();
        long t1 = System.currentTimeMillis();
        try
        {
            dcmqr.open();
        }
        catch (Exception e)
        {
            System.err.println("ERROR: Failed to establish association:"
                    + e.getMessage());
            System.exit(2);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Connected to " + remoteAE + " in "
                + ((t2 - t1) / 1000F) + "s");

        try
        {
            List result = dcmqr.query();
            if (dcmqr.isMove())
                dcmqr.move(result);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try
        {
            dcmqr.close();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Released connection to " + remoteAE);
    }
    
    private void setLimit(int limit)
    {
        this.limit = limit;        
    }

    private static int zeroAsMaxInt(int val)
    {
        return val > 0 ? val : Integer.MAX_VALUE;
    }

    private void addFilter(int tag, String value)
    {
        filter.putString(tag, null, value);        
    }

    private static int toTag(String nameOrHex)
    {
        try
        {
            return (int) Long.parseLong(nameOrHex, 16);
        } catch (NumberFormatException e)
        {
            return Tag.forName(nameOrHex);
        }
    }

    private void configureTransferCapability()
    {        
        String[] findcuids = FIND_CUID[qrlevel];
        String[] movecuids = moveDest != null 
                ? MOVE_CUID[qrlevel] : EMPTY_STRING;
        TransferCapability[] tc = 
                new TransferCapability[findcuids.length + movecuids.length];
        for (int i = 0; i < findcuids.length; i++)
            tc[i] = new TransferCapability(findcuids[i], tsuids, TransferCapability.SCU);
        for (int i = 0; i < movecuids.length; i++)
            tc[findcuids.length + i] = 
                new TransferCapability(movecuids[i], tsuids, TransferCapability.SCU);
        ae.setTransferCapability(tc);       
    }

    private void setQueryLevel(int qrlevel)
    {
        this.qrlevel = qrlevel;
        filter.putString(Tag.QueryRetrieveLevel, VR.CS, QRLEVEL[qrlevel]);
        int[] tags = RETURN_KEYS[qrlevel];
        for (int i = 0; i < tags.length; i++)
            filter.putNull(tags[i], null);
    }

    private void setMoveDest(String aet)
    {
        moveDest = aet;        
    }
    
    private boolean isMove()
    {
        return moveDest != null;
    }

    private static String toHostname(String host)
    {
        return host != null ? host : "127.0.0.1";
    }

    private static int toPort(String port)
    {
        return port != null ? parseInt(port, "illegal port number", 1, 0xffff)
                : 104;
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {}
        exit(errPrompt);
        throw new RuntimeException();
    }
    
    private static String[] split(String s, char delim)
    {
        String[] s2 =
        { s, null };
        int pos = s.indexOf(delim);
        if (pos != -1)
        {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg)
    {
        System.err.println(msg);
        System.err.println("Try 'dcmqr -h' for more information.");
        System.exit(1);
    }

    public void open()
            throws IOException, ConfigurationException, InterruptedException
    {
        assoc = ae.connect(remoteAE, executor);
    }

    public List query() throws IOException, InterruptedException
    {
        TransferCapability tc = selectTransferCapability(FIND_CUID[qrlevel]);
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        System.out.println("Send Query Request using " 
                + UIDDictionary.getDictionary().prompt(cuid) + ":");
        System.out.println(filter.toString());
        System.out.println("using " + UIDDictionary.getDictionary().prompt(cuid));
        DimseRSP rsp = assoc.cfind(cuid, priority, filter, tsuid, limit);
        List result = new ArrayList();
        while (rsp.next())
        {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd))
            {
                DicomObject data = rsp.getDataset();
                System.out.println("Received Query Response:");
                System.out.println(data.toString());
                result.add(data);
            }
        }
        return result;
    }

    private String selectTransferSyntax(TransferCapability tc)
    {
        return tc.getTransferSyntax()[0];
    }

    public void move(List findResults) throws IOException, InterruptedException
    {
        if (moveDest == null)
            throw new IllegalStateException("moveDest == null");
        TransferCapability tc = selectTransferCapability(MOVE_CUID[qrlevel]);
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        for (int i = 0, n = Math.min(findResults.size(), limit); i < n; ++i)
        {
            DicomObject keys = ((DicomObject) findResults.get(i)).subSet(MOVE_KEYS);
            System.out.println("Send Retrieve Request using "
                    + UIDDictionary.getDictionary().prompt(cuid) + ":");
            System.out.println(keys.toString());
            DimseRSPHandler rspHandler = new DimseRSPHandler(){
                public void onDimseRSP(Association as, DicomObject cmd, 
                        DicomObject data)
                {
                    DcmQR.this.onMoveRSP(as, cmd, data);
                }
            };
            assoc.cmove(cuid, priority, keys, tsuid, moveDest, rspHandler);
        }        
    }

    
    protected void onMoveRSP(Association as, DicomObject cmd, DicomObject data)
    {
        // TODO Auto-generated method stub
        
    }

    private TransferCapability selectTransferCapability(String[] cuid)
    throws NoPresentationContextException
    {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++)
        {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        throw new NoPresentationContextException(
                UIDDictionary.getDictionary().prompt(cuid[0])
                + " not supported by" + remoteAE.getAETitle() );
    }

    public void close() throws InterruptedException
    {
        assoc.release(true);
    }
}
