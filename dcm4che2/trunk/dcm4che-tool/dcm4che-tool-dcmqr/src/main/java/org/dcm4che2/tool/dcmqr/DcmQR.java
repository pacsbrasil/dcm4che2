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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.ExtQueryTransferCapability;
import org.dcm4che2.net.ExtRetrieveTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jan, 2006
 */
public class DcmQR {
    private static final int KB = 1024;

    private static final String USAGE = "dcmqr [Options] <aet>[@<host>[:<port>]]";

    private static final String DESCRIPTION = 
        "Query specified remote Application Entity (=Query/Retrieve SCP) "
        + "and optional (s. option -dest) retrieve instances of matching entities. "
        + "If <port> is not specified, DICOM default port 104 is assumed. "
        + "If also no <host> is specified localhost is assumed.\n"
        + "Options:";
    
    private static final String EXAMPLE = 
        "\nExample: dcmqr QRSCP@localhost:11112 -qStudyDate=20060204 -dest STORESCP\n"
        + "=> Query Application Entity QRSCP listening on local port 11112 for "
        + "studies from Feb 4, 2006 and retrieve instances of matching studies to "
        + "Application Entity STORESCP.";

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };

    private static final int PATIENT = 0;
    private static final int STUDY = 1;
    private static final int SERIES = 2;
    private static final int IMAGE = 3;

    private static final String[] QRLEVEL = {
        "PATIENT",
        "STUDY",
        "SERIES",
        "IMAGE" };
    
    private static final String[] PATIENT_LEVEL_FIND_CUID = {
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };
    
    private static final String[] STUDY_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };
    
    private static final String[] SERIES_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND, };
    
    private static final String[][] FIND_CUID = { PATIENT_LEVEL_FIND_CUID,
        STUDY_LEVEL_FIND_CUID,
        SERIES_LEVEL_FIND_CUID,
        SERIES_LEVEL_FIND_CUID };
    
    private static final String[] PATIENT_LEVEL_MOVE_CUID = {
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };
    
    private static final String[] STUDY_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };
    
    private static final String[] SERIES_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE };
    
    private static final String[][] MOVE_CUID = {
        PATIENT_LEVEL_MOVE_CUID,
        STUDY_LEVEL_MOVE_CUID,
        SERIES_LEVEL_MOVE_CUID,
        SERIES_LEVEL_MOVE_CUID };
    
    private static final int[] PATIENT_RETURN_KEYS = { Tag.PatientName,
        Tag.PatientID, Tag.PatientBirthDate, Tag.PatientSex,
        Tag.NumberOfPatientRelatedStudies,
        Tag.NumberOfPatientRelatedSeries,
        Tag.NumberOfPatientRelatedInstances };
    
    private static final int[] STUDY_RETURN_KEYS = {
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.StudyID,
        Tag.StudyInstanceUID,
        Tag.NumberOfStudyRelatedSeries,
        Tag.NumberOfStudyRelatedInstances };
    
    private static final int[] SERIES_RETURN_KEYS = {
        Tag.Modality,
        Tag.SeriesNumber,
        Tag.SeriesInstanceUID,
        Tag.NumberOfSeriesRelatedInstances };
    
    private static final int[] INSTANCE_RETURN_KEYS = {
        Tag.InstanceNumber,
        Tag.SOPClassUID,
        Tag.SOPInstanceUID, };
    
    private static final int[][] RETURN_KEYS = {
        PATIENT_RETURN_KEYS,
        STUDY_RETURN_KEYS,
        SERIES_RETURN_KEYS,
        INSTANCE_RETURN_KEYS };
    
    private static final int[] MOVE_KEYS = {
        Tag.QueryRetrieveLevel,
        Tag.PatientID,
        Tag.StudyInstanceUID,
        Tag.SeriesInstanceUID,
        Tag.SOPInstanceUID, };
    
    private static final String[] IVRLE_TS = {
        UID.ImplicitVRLittleEndian };
    
    private static final String[] NATIVE_LE_TS = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian  };
    
    private static final String[] DEFLATED_TS = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian,
        UID.DeflatedExplicitVRLittleEndian };

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
    
    private boolean evalRetrieveAET = false;

    private int qrlevel = STUDY;

    private ArrayList privateFind = new ArrayList();

    private DicomObject keys = new BasicDicomObject();

    private int cancelAfter = Integer.MAX_VALUE;

    private int completed;

    private int warning;

    private int failed;

    private boolean relationQR;

    private boolean dateTimeMatching;

    private boolean semanticPersonNameMatching;

    private boolean caseSensitivePersonNameMatching;

    private boolean noExtNegotiation;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET; 
    
    public DcmQR() {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle("DCMQR");
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

    public final void setTlsWithoutEncyrption() {
        conn.setTlsWithoutEncyrption();
        remoteConn.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        conn.setTls3DES_EDE_CBC();
        remoteConn.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        conn.setTlsAES_128_CBC();
        remoteConn.setTlsAES_128_CBC();
    }
    
    public final void setKeyStoreURL(String url) {
        keyStoreURL = url;
    }
    
    public final void setKeyStorePassword(String pw) {
        keyStorePassword = pw.toCharArray();
    }
    
    public final void setKeyPassword(String pw) {
        keyPassword = pw.toCharArray();
    }
    
    public final void setTrustStorePassword(String pw) {
        trustStorePassword = pw.toCharArray();
    }
    
    public final void setTrustStoreURL(String url) {
        trustStoreURL = url;
    }

    public final void setCalledAET(String called) {
        remoteAE.setAETitle(called);
    }

    public final void setCalling(String calling) {
        ae.setAETitle(calling);
    }

    public final void setUserIdentity(UserIdentity userIdentity) {
        ae.setUserIdentity(userIdentity);
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

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        ae.setMaxOpsInvoked(maxOpsInvoked);
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

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        OptionBuilder.withArgName("aet[@host]");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("set AET and local address of local " +
                "Application Entity, use ANONYMOUS and pick up any valid\n" +
                "local address to bind the socket by default");
        opts.addOption(OptionBuilder.create("L"));

        OptionBuilder.withArgName("username");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "enable User Identity Negotiation with specified username and "
                + " optional passcode");
        opts.addOption(OptionBuilder.create("username"));

        OptionBuilder.withArgName("passcode");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "optional passcode for User Identity Negotiation, "
                + "only effective with option -username");
        opts.addOption(OptionBuilder.create("passcode"));

        opts.addOption("uidnegrsp", false,
                "request positive User Identity Negotation response, "
                + "only effective with option -username");
        
        OptionBuilder.withArgName("NULL|3DES|AES");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "enable TLS connection without, 3DES or AES encryption");
        opts.addOption(OptionBuilder.create("tls"));

        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of P12 or JKS keystore, resource:tls/test_sys_1.p12 by default");
        opts.addOption(OptionBuilder.create("keystore"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for keystore file, 'secret' by default");
        opts.addOption(OptionBuilder.create("keystorepw"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for accessing the key in the keystore, keystore password by default");
        opts.addOption(OptionBuilder.create("keypw"));

        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of JKS truststore, resource:tls/mesa_certs.jks by default");
        opts.addOption(OptionBuilder.create("truststore"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for truststore file, 'secret' by default");
        opts.addOption(OptionBuilder.create("truststorepw"));
        
        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "retrieve instances of matching entities to specified destination.");
        opts.addOption(OptionBuilder.create("dest"));

        opts.addOption("ivrle", false, "offer only Implicit VR Little Endian Transfer Syntax.");

        OptionBuilder.withArgName("maxops");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("maximum number of outstanding C-MOVE-RQ " +
                "it may invoke asynchronously, 1 by default.");
        opts.addOption(OptionBuilder.create("async"));

        opts.addOption("noextneg", false, "disable extended negotiation.");
        opts.addOption("rel", false,
                "negotiate support of relational queries and retrieval.");
        opts.addOption("datetime", false,
                "negotiate support of combined date and time attribute range matching.");
        opts.addOption("case", false, 
                "negotiate support of case-sensitive person name attribute matching.");
        opts.addOption("semantic", false, 
                "negotiate support of semantic person name attribute matching.");

        opts.addOption("retall", false, "negotiate private FIND SOP Classes " +
                "to fetch all available attributes of matching entities.");
        opts.addOption("blocked", false, "negotiate private FIND SOP Classes " +
                "to return attributes of several matching entities per FIND\n" +
                "response.");
        opts.addOption("vmf", false, "negotiate private FIND SOP Classes to " +
                "return attributes of legacy CT/MR images of one series as\n" +
                "virtual multiframe object.");
        opts.addOption("pdv1", false,
                "send only one PDV in one P-Data-TF PDU, pack command and data " +
                "PDV in one P-DATA-TF PDU by default.");
        opts.addOption("tcpdelay", false,
                "set TCP_NODELAY socket option to false, true by default");

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for TCP connect, no timeout by default");
        opts.addOption(OptionBuilder.create("connectTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        opts.addOption(OptionBuilder.create("soclosedelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "period in ms to check for outstanding DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("reaper"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving DIMSE-RSP, 60s by default");
        opts.addOption(OptionBuilder.create("rspTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
        opts.addOption(OptionBuilder.create("acceptTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        opts.addOption(OptionBuilder.create("releaseTO"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("rcvpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("sndpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_RCVBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sorcvbuf"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_SNDBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sosndbuf"));

        OptionGroup qrlevel = new OptionGroup();

        OptionBuilder.withDescription("perform patient level query, multiple " +
                "exclusive with -S and -I, perform study level query by default.");
        OptionBuilder.withLongOpt("patient");
        opts.addOption(OptionBuilder.create("P"));

        OptionBuilder.withDescription("perform series level query, multiple " +
                "exclusive with -P and -I, perform study level query by default.");
        OptionBuilder.withLongOpt("series");
        opts.addOption(OptionBuilder.create("S"));

        OptionBuilder.withDescription("perform instance level query, multiple " +
                "exclusive with -P and -S, perform study level query by default.");
        OptionBuilder.withLongOpt("image");
        opts.addOption(OptionBuilder.create("I"));

        opts.addOptionGroup(qrlevel);

        OptionBuilder.withArgName("[seq/]attr=value");
        OptionBuilder.hasArgs(2);
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription("specify matching key. attr can be " +
                "specified by name or tag value (in hex), e.g. PatientName\n" +
                "or 00100010. Attributes in nested Datasets can\n" +
                "be specified by including the name/tag value of\n" +
                "the sequence attribute, e.g. 00400275/00400009\n" +
                "for Scheduled Procedure Step ID in the Request\n" +
                "Attributes Sequence");
        opts.addOption(OptionBuilder.create("q"));

        OptionBuilder.withArgName("attr");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify additional return key. attr can " +
                "be specified by name or tag value (in hex).");
        opts.addOption(OptionBuilder.create("r"));

        OptionBuilder.withArgName("num");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("cancel query after receive of specified " +
                "number of responses, no cancel by default");
        opts.addOption(OptionBuilder.create("C"));

        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("retrieve matching objects to specified " +
                "move destination.");
        opts.addOption(OptionBuilder.create("dest"));
        
        opts.addOption("evalRetrieveAET", false,
                "Only Move studies not allready stored on destination AET");
        opts.addOption("lowprior", false,
                "LOW priority of the C-FIND/C-MOVE operation, MEDIUM by default");
        opts.addOption("highprior", false,
                "HIGH priority of the C-FIND/C-MOVE operation, MEDIUM by default");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmqr: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = DcmQR.class.getPackage();
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

    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DcmQR dcmqr = new DcmQR();
        final List argList = cl.getArgList();
        String remoteAE = (String) argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmqr.setCalledAET(calledAETAddress[0]);
        if (calledAETAddress[1] == null) {
            dcmqr.setRemoteHost("127.0.0.1");
            dcmqr.setRemotePort(104);
        } else {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmqr.setRemoteHost(hostPort[0]);
            dcmqr.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L")) {
            String localAE = cl.getOptionValue("L");
            String[] callingAETHost = split(localAE, '@');
            dcmqr.setCalling(callingAETHost[0]);
            if (callingAETHost[1] != null) {
                dcmqr.setLocalHost(callingAETHost[1]);
            }
        }
        if (cl.hasOption("username")) {
            String username = cl.getOptionValue("username");
            UserIdentity userId;
            if (cl.hasOption("passcode")) {
                String passcode = cl.getOptionValue("passcode");
                userId = new UserIdentity.UsernamePasscode(username,
                        passcode.toCharArray());
            } else {
                userId = new UserIdentity.Username(username);
            }
            userId.setPositiveResponseRequested(cl.hasOption("uidnegrsp"));
            dcmqr.setUserIdentity(userId);
        }
        if (cl.hasOption("connectTO"))
            dcmqr.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("reaper"))
            dcmqr.setAssociationReaperPeriod(parseInt(cl.getOptionValue("reaper"),
                            "illegal argument of option -reaper", 1,
                            Integer.MAX_VALUE));
        if (cl.hasOption("rspTO"))
            dcmqr.setDimseRspTimeout(parseInt(cl.getOptionValue("rspTO"),
                    "illegal argument of option -rspTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmqr.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmqr.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmqr.setSocketCloseDelay(parseInt(cl
                    .getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption("rcvpdulen"))
            dcmqr.setMaxPDULengthReceive(parseInt(cl
                    .getOptionValue("rcvpdulen"),
                    "illegal argument of option -rcvpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sndpdulen"))
            dcmqr.setMaxPDULengthSend(parseInt(cl.getOptionValue("sndpdulen"),
                    "illegal argument of option -sndpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sosndbuf"))
            dcmqr.setSendBufferSize(parseInt(cl.getOptionValue("sosndbuf"),
                    "illegal argument of option -sosndbuf", 1, 10000)
                    * KB);
        if (cl.hasOption("sorcvbuf"))
            dcmqr.setReceiveBufferSize(parseInt(cl.getOptionValue("sorcvbuf"),
                    "illegal argument of option -sorcvbuf", 1, 10000)
                    * KB);
        dcmqr.setPackPDV(!cl.hasOption("pdv1"));
        dcmqr.setTcpNoDelay(!cl.hasOption("tcpdelay"));
        dcmqr.setMaxOpsInvoked(cl.hasOption("async") ? parseInt(cl
                .getOptionValue("async"), "illegal argument of option -async",
                0, 0xffff) : 1);
        if (cl.hasOption("C"))
            dcmqr.setCancelAfter(parseInt(cl.getOptionValue("C"),
                    "illegal argument of option -C", 1, Integer.MAX_VALUE));
        if (cl.hasOption("lowprior"))
            dcmqr.setPriority(CommandUtils.LOW);
        if (cl.hasOption("highprior"))
            dcmqr.setPriority(CommandUtils.HIGH);
        if (cl.hasOption("dest"))
            dcmqr.setMoveDest(cl.getOptionValue("dest"));
        if (cl.hasOption("evalRetrieveAET"))
            dcmqr.setEvalRetrieveAET(true);
        if (cl.hasOption("P"))
            dcmqr.setQueryLevel(PATIENT);
        else if (cl.hasOption("S"))
            dcmqr.setQueryLevel(SERIES);
        else if (cl.hasOption("I"))
            dcmqr.setQueryLevel(IMAGE);
        else
            dcmqr.setQueryLevel(STUDY);
        if (cl.hasOption("noextneg"))
            dcmqr.setNoExtNegotiation(true);
        if (cl.hasOption("rel"))
            dcmqr.setRelationQR(true);
        if (cl.hasOption("datetime"))
            dcmqr.setDateTimeMatching(true);
        if (cl.hasOption("case"))
            dcmqr.setCaseSensitivePersonNameMatching(true);
        if (cl.hasOption("semantic"))
            dcmqr.setSemanticPersonNameMatching(true);

        if (cl.hasOption("retall"))
            dcmqr.addPrivate(
                    UID.PrivateStudyRootQueryRetrieveInformationModelFIND);
        if (cl.hasOption("blocked"))
            dcmqr.addPrivate(
                    UID.PrivateBlockedStudyRootQueryRetrieveInformationModelFIND);
        if (cl.hasOption("vmf"))
            dcmqr.addPrivate(
                    UID.PrivateVirtualMultiframeStudyRootQueryRetrieveInformationModelFIND);
        if (cl.hasOption("q")) {
            String[] matchingKeys = cl.getOptionValues("q");
            for (int i = 1; i < matchingKeys.length; i++, i++)
                dcmqr.addMatchingKey(Tag.toTagPath(matchingKeys[i - 1]), matchingKeys[i]);
        }
        if (cl.hasOption("r")) {
            String[] returnKeys = cl.getOptionValues("r");
            for (int i = 0; i < returnKeys.length; i++)
                dcmqr.addReturnKey(Tag.toTagPath(returnKeys[i]));
        }

        dcmqr.configureTransferCapability(cl.hasOption("ivrle"));
        

        if (cl.hasOption("tls")) {
            String cipher = cl.getOptionValue("tls");
            if ("NULL".equalsIgnoreCase(cipher)) {
                dcmqr.setTlsWithoutEncyrption();
            } else if ("3DES".equalsIgnoreCase(cipher)) {
                dcmqr.setTls3DES_EDE_CBC();
            } else if ("AES".equalsIgnoreCase(cipher)) {
                dcmqr.setTlsAES_128_CBC();
            } else {
                exit("Invalid parameter for option -tls: " + cipher);
            }
            if (cl.hasOption("keystore")) {
                dcmqr.setKeyStoreURL(cl.getOptionValue("keystore"));
            }
            if (cl.hasOption("keystorepw")) {
                dcmqr.setKeyStorePassword(
                        cl.getOptionValue("keystorepw"));
            }
            if (cl.hasOption("keypw")) {
                dcmqr.setKeyPassword(cl.getOptionValue("keypw"));
            }
            if (cl.hasOption("truststore")) {
                dcmqr.setTrustStoreURL(
                        cl.getOptionValue("truststore"));
            }
            if (cl.hasOption("truststorepw")) {
                dcmqr.setTrustStorePassword(
                        cl.getOptionValue("truststorepw"));
            }
            long t1 = System.currentTimeMillis();
            try {
                dcmqr.initTLS();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to initialize TLS context:"
                        + e.getMessage());
                System.exit(2);
            }
            long t2 = System.currentTimeMillis();
            System.out.println("Initialize TLS context in "
                    + ((t2 - t1) / 1000F) + "s");
        }
        
        long t1 = System.currentTimeMillis();
        try {
            dcmqr.open();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to establish association:");
            e.printStackTrace(System.err);
            System.exit(2);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Connected to " + remoteAE + " in "
                + ((t2 - t1) / 1000F) + "s");

        try {
            List result = dcmqr.query();
            long t3 = System.currentTimeMillis();
            System.out.println("Received " + result.size()
                    + " matching entries in " + ((t3 - t2) / 1000F) + "s");
            if (dcmqr.isMove()) {
                dcmqr.move(result);
                long t4 = System.currentTimeMillis();
                System.out.println("Retrieved " + dcmqr.getTotalRetrieved()
                        + " objects (warning: " + dcmqr.getWarning()
                        + ", failed: " + dcmqr.getFailed() + ") in "
                        + ((t4 - t3) / 1000F) + "s");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            dcmqr.close();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Released connection to " + remoteAE);
    }

    private void setEvalRetrieveAET(boolean evalRetrieveAET) {
       this.evalRetrieveAET = evalRetrieveAET;
    }

    private boolean isEvalRetrieveAET() {
        return evalRetrieveAET;
    }

    private void setNoExtNegotiation(boolean b) {
        this.noExtNegotiation = b;
    }

    private void setSemanticPersonNameMatching(boolean b) {
        this.semanticPersonNameMatching = b;
    }

    private void setCaseSensitivePersonNameMatching(boolean b) {
        this.caseSensitivePersonNameMatching = b;
    }

    private void setDateTimeMatching(boolean b) {
        this.dateTimeMatching = b;
    }

    private void setRelationQR(boolean b) {
        this.relationQR = b;
    }

    public final int getFailed() {
        return failed;
    }

    public final int getWarning() {
        return warning;
    }

    private final int getTotalRetrieved() {
        return completed + warning;
    }

    private void setCancelAfter(int limit) {
        this.cancelAfter = limit;
    }

    private void addMatchingKey(int[] tagPath, String value) {
        keys.putString(tagPath, null, value);
    }

    private void addReturnKey(int[] tagPath) {
        keys.putNull(tagPath, null);
    }

    private void configureTransferCapability(boolean ivrle) {
        String[] findcuids = FIND_CUID[qrlevel];
        String[] movecuids = moveDest != null ? MOVE_CUID[qrlevel]
                : EMPTY_STRING;
        final int numPrivateFind = qrlevel != PATIENT ? privateFind.size() : 0;
        TransferCapability[] tc = new TransferCapability[findcuids.length
                + movecuids.length + numPrivateFind];
        int i = 0;
        for (int j = 0; j < findcuids.length; j++)
            tc[i++] = mkFindTC(findcuids[j],
                    ivrle ? IVRLE_TS : NATIVE_LE_TS);
        for (int j = 0; j < movecuids.length; j++)
            tc[i++] = mkMoveTC(movecuids[j],
                    ivrle ? IVRLE_TS : NATIVE_LE_TS);
        for (int j = 0; j < numPrivateFind; j++)
            tc[i++] = mkFindTC((String) privateFind.get(j),
                    ivrle ? IVRLE_TS : DEFLATED_TS);
        ae.setTransferCapability(tc);
    }

    private TransferCapability mkMoveTC(String cuid, String[] ts) {
        ExtRetrieveTransferCapability tc = new ExtRetrieveTransferCapability(
                cuid, ts, TransferCapability.SCU);
        tc.setExtInfoBoolean(
                ExtRetrieveTransferCapability.RELATIONAL_RETRIEVAL, relationQR);
        if (noExtNegotiation)
            tc.setExtInfo(null);
        return tc;
    }

    private TransferCapability mkFindTC(String cuid, String[] ts) {
        ExtQueryTransferCapability tc = new ExtQueryTransferCapability(cuid,
                ts, TransferCapability.SCU);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.RELATIONAL_QUERIES,
                relationQR);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.DATE_TIME_MATCHING,
                dateTimeMatching);
        tc.setExtInfoBoolean(
                ExtQueryTransferCapability.CASE_SENSITIVE_PN_MATCHING,
                caseSensitivePersonNameMatching);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.SEMANTIC_PN_MATCHING,
                semanticPersonNameMatching);
        if (noExtNegotiation)
            tc.setExtInfo(null);
        return tc;
    }

    private void setQueryLevel(int qrlevel) {
        this.qrlevel = qrlevel;
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, QRLEVEL[qrlevel]);
        int[] tags = RETURN_KEYS[qrlevel];
        for (int i = 0; i < tags.length; i++)
            keys.putNull(tags[i], null);
    }

    public final void addPrivate(String cuid) {
        this.privateFind.add(cuid);
    }

    private void setMoveDest(String aet) {
        moveDest = aet;
    }

    private boolean isMove() {
        return moveDest != null;
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
        System.err.println("Try 'dcmqr -h' for more information.");
        System.exit(1);
    }

    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public List query() throws IOException, InterruptedException {
        TransferCapability tc = selectFindTransferCapability();
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        System.out.println("Send Query Request using "
                + UIDDictionary.getDictionary().prompt(cuid) + ":");
        System.out.println(keys.toString());
        DimseRSP rsp = assoc.cfind(cuid, priority, keys, tsuid, cancelAfter);
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

    private TransferCapability selectFindTransferCapability()
            throws NoPresentationContextException {
        TransferCapability tc;
        if (qrlevel != PATIENT
                && (tc = selectTransferCapability(privateFind)) != null)
            return tc;
        if ((tc = selectTransferCapability(FIND_CUID[qrlevel])) != null)
            return tc;
        throw new NoPresentationContextException(UIDDictionary.getDictionary()
                .prompt(FIND_CUID[qrlevel][0])
                + " not supported by" + remoteAE.getAETitle());
    }

    private String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1)
            return UID.DeflatedExplicitVRLittleEndian;
        return tcuids[0];
    }

    public void move(List findResults) throws IOException, InterruptedException {
        if (moveDest == null)
            throw new IllegalStateException("moveDest == null");
        TransferCapability tc = selectTransferCapability(MOVE_CUID[qrlevel]);
        if (tc == null)
            throw new NoPresentationContextException(UIDDictionary
                    .getDictionary().prompt(MOVE_CUID[qrlevel][0])
                    + " not supported by" + remoteAE.getAETitle());
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        for (int i = 0, n = Math.min(findResults.size(), cancelAfter); i < n; ++i) {
            DicomObject keys = ((DicomObject) findResults.get(i))
            .subSet(MOVE_KEYS);
            if (isEvalRetrieveAET()
                    && moveDest.equals(((DicomObject) findResults.get(i))
                            .getString(Tag.RetrieveAETitle))) {
                System.out.println("Skipping "
                        + UIDDictionary.getDictionary().prompt(cuid) + ":");
                System.out.println(keys.toString());
            } else {
                System.out.println("Send Retrieve Request using "
                        + UIDDictionary.getDictionary().prompt(cuid) + ":");
                System.out.println(keys.toString());
                DimseRSPHandler rspHandler = new DimseRSPHandler() {
                    public void onDimseRSP(Association as, DicomObject cmd,
                            DicomObject data) {
                        DcmQR.this.onMoveRSP(as, cmd, data);
                    }
                };
                assoc.cmove(cuid, priority, keys, tsuid, moveDest, rspHandler);
            }
        }
        assoc.waitForDimseRSP();
    }

    protected void onMoveRSP(Association as, DicomObject cmd, DicomObject data) {
        if (!CommandUtils.isPending(cmd)) {
            completed += cmd.getInt(Tag.NumberOfCompletedSuboperations);
            warning += cmd.getInt(Tag.NumberOfWarningSuboperations);
            failed += cmd.getInt(Tag.NumberOfFailedSuboperations);
        }

    }

    private TransferCapability selectTransferCapability(String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }

    private TransferCapability selectTransferCapability(List cuid) {
        TransferCapability tc;
        for (int i = 0, n = cuid.size(); i < n; i++) {
            tc = assoc.getTransferCapabilityAsSCU((String) cuid.get(i));
            if (tc != null)
                return tc;
        }
        return null;
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    public void initTLS() throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
        device.initTLS(keyStore,
                keyPassword != null ? keyPassword : keyStorePassword,
                trustStore);
    }
    
    private static KeyStore loadKeyStore(String url, char[] password)
            throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    private static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return DcmQR.class.getClassLoader().getResourceAsStream(
                    url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                 ? "PKCS12" : "JKS";
    }
}