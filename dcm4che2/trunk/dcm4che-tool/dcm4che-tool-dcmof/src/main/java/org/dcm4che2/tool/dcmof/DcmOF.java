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

package org.dcm4che2.tool.dcmof;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.VerificationService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jan 22, 2006
 *
 */
public class DcmOF
{
    private static final int MEGABYTE = 1048576;
    private static final String USAGE = 
        "dcmof [Options] [<aet>[@<ip>]:]<port>";
    private static final String DESCRIPTION = 
        "DICOM Server providing DICOM service of IHE actor Order Filler:\n" +
        "- Modality Worklist (MWL SCP),\n" +
        "- Modality Performed Procedure Step (MPPS SCP)\n" +
        "- Image Availability Notification (IAN SCP)\n" +
        "- Basic Study Content Notification (SCN SCP) {not specified by IHE}\n" +
        "listening on specified <port> for incoming association requests. " +
        "If no local IP address of the network interface is specified " +
        "connections on  any/all local addresses are accepted. " +
        "If <aet> is specified, only requests with matching called AE " +
        "title will be accepted.\n" +
        "Options:";
    private static final String EXAMPLE = 
        "\nExample 1: dcmof DCM4CHE_OF:11112 --mwl /var/local/dcmof/mwl\n" +
        "=> Starts MWL SCP listening on port 11112, accepting association " +
        "requests with DCM4CHE_OF as called AE title, provides worklist items " +
        "stored in files in directory /var/local/dcmof/mwl as MWL SCP.\n" +
        "Example 2: dcmof DCM4CHE_OF:11112 --mpps /tmp --ian /tmp --scn /tmp\n" +
        "=> Starts MPPS+IAN+SCN SCP listening on port 11112, accepting association " +
        "requests with DCM4CHE_OF as called AE title, storing received messages " +
        "to /tmp.";
    
    private static final String[] ONLY_DEF_TS =
    {
        UID.ImplicitVRLittleEndian
    };

    private static final String[] NATIVE_TS =
    {
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian
    };

    private static final String[] NATIVE_LE_TS =
    {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian
    };
    

    
    private static Executor executor = new NewThreadExecutor("DCM4CHE_OF");
    private Device device = new Device("DCM4CHE_OF");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private NetworkConnection nc = new NetworkConnection();
    private String[] tsuids = NATIVE_LE_TS;
    
    public DcmOF()
    {
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(nc);
        ae.setNetworkConnection(nc);
        ae.setAssociationAcceptor(true);
        ae.register(new VerificationService());
    }
       
    public final void setAEtitle(String aet)
    {
        ae.setAETitle(aet);
    }

    public final void setHostname(String hostname)
    {
        nc.setHostname(hostname);
    }
    
    public final void setPort(int port)
    {
        nc.setPort(port);
    }
    
    public final void setPackPDV(boolean packPDV)
    {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period)
    {
        device.setAssociationReaperPeriod(period);
    }
    
    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        nc.setTcpNoDelay(tcpNoDelay);
    }

    public final void setRequestTimeout(int timeout)
    {
        nc.setRequestTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout)
    {
        nc.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int delay)
    {
        nc.setSocketCloseDelay(delay);
    }

    public final void setIdleTimeout(int timeout)
    {
        ae.setIdleTimeout(timeout);        
    }
    
    public final void setDimseRspTimeout(int timeout)
    {
        ae.setDimseRspTimeout(timeout);        
    }
    
    public final void setMaxPDULengthSend(int maxLength)
    {
        ae.setMaxPDULengthSend(maxLength);        
    }
    
    public void setMaxPDULengthReceive(int maxLength)
    {
        ae.setMaxPDULengthReceive(maxLength);        
    }
    
    public final void setReceiveBufferSize(int bufferSize)
    {
        nc.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize)
    {
        nc.setSendBufferSize(bufferSize);
    }

    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        Option mwl = new Option(" ", "mwl", true,
                "Activate MWL SCP, providing MWL Items stored in specified directory.");
        mwl.setArgName("dir");
        opts.addOption(mwl);
        Option mpps = new Option(" ", "mpps", true,
                "Activate MPPS SCP, storing received MPPS in specified directory.");
        mpps.setArgName("dir");
        opts.addOption(mpps);
        Option ian = new Option(" ", "ian", true,
                "Activate IAN SCP, storing received IAN in specified directory.");
        ian.setArgName("dir");
        opts.addOption(ian);
        Option scn = new Option(" ", "scn", true,
                "Activate SCN SCP, storing received SCN in specified directory.");
        scn.setArgName("dir");
        opts.addOption(scn);
        opts.addOption("k", "pack-pdv", false, 
                "pack command and data PDV in one P-DATA-TF PDU, " +
                "send only one PDV in one P-Data-TF PDU by default.");
        opts.addOption("y", "tcp-no-delay", false, 
                "set TCP_NODELAY socket option to true, false by default");
        Option closeDelay = new Option("c", "close-delay", true,
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        closeDelay.setArgName("delay");
        opts.addOption(closeDelay);
        Option acTimeout = new Option("T", "request-timeout", true,
                "timeout in ms for receiving A-ASSOCIATE-RQ, 5s by default");
        acTimeout.setArgName("timeout");
        opts.addOption(acTimeout);
        Option rpTimeout = new Option("t", "release-timeout", true,
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        rpTimeout.setArgName("timeout");
        opts.addOption(rpTimeout);
        Option checkPeriod = new Option("R", "reaper-period", true,
                "period in ms to check idleness, 10s by default");
        checkPeriod.setArgName("period");
        Option idleTimeout = new Option("I", "idle-timeout", true,
                "timeout in ms for receiving DIMSE-RQ, 60s by default");
        acTimeout.setArgName("timeout");
        opts.addOption(idleTimeout);
        Option soRcvBufSize = new Option("s", "so-rcv-buf-size", true,
                "set SO_RCVBUF socket option to specified value");
        soRcvBufSize.setArgName("size");
        opts.addOption(soRcvBufSize);
        Option soSndBufSize = new Option("S", "so-snd-buf-size", true,
                "set SO_SNDBUF socket option to specified value");
        soSndBufSize.setArgName("size");
        opts.addOption(soSndBufSize);
        Option rcvPduLen = new Option("u", "rcv-pdu-len", true,
                "maximal length of received P-DATA-TF PDUs, 16384 by default");
        rcvPduLen.setArgName("max-len");
        opts.addOption(rcvPduLen);
        Option sndPduLen = new Option("U", "snd-pdu-len", true,
                "maximal length of sent P-DATA-TF PDUs, 16384 by default");
        sndPduLen.setArgName("max-len");
        opts.addOption(sndPduLen);
        Option maxOpsInvoked = new Option("a", "async", true,
                "maximum number of outstanding operations performed " +
                "asynchronously, unlimited by default.");
        maxOpsInvoked.setArgName("max-ops");
        opts.addOption(maxOpsInvoked);
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try
        {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e)
        {
            exit("dcmof: " + e.getMessage());
        }
        if (cl.hasOption('V'))
        {
            Package p = DcmOF.class.getPackage();
            System.out.println("dcmof v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() == 0)
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
        DcmOF dcmof = new DcmOF();
        final List argList = cl.getArgList();
        String port = (String) argList.get(0);
        String[] aetPort = split(port, ':', 1);
        dcmof.setPort(parseInt(aetPort[1], "illegal port number", 1, 0xffff));
        if (aetPort[0] != null)
        {
            String[] aetHost = split(aetPort[0], '@', 0);
            dcmof.setAEtitle(aetHost[0]);
            dcmof.setHostname(aetHost[1]);
        }

        if (cl.hasOption("T"))
            dcmof.setRequestTimeout(
                    parseInt(cl.getOptionValue("T"),
                    "illegal argument of option -T", 1, Integer.MAX_VALUE));
        if (cl.hasOption("t"))
            dcmof.setReleaseTimeout(
                    parseInt(cl.getOptionValue("t"),
                    "illegal argument of option -t", 1, Integer.MAX_VALUE));
        if (cl.hasOption("c"))
            dcmof.setSocketCloseDelay(
                    parseInt(cl.getOptionValue("c"),
                    "illegal argument of option -c", 1, 10000));
        if (cl.hasOption("R"))
            dcmof.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue("R"),
                    "illegal argument of option -R", 1, Integer.MAX_VALUE));
        if (cl.hasOption("I"))
            dcmof.setIdleTimeout(
                    parseInt(cl.getOptionValue("I"),
                    "illegal argument of option -I", 1, Integer.MAX_VALUE));
        if (cl.hasOption("u"))
            dcmof.setMaxPDULengthReceive(
                    parseInt(cl.getOptionValue("u"),
                    "illegal argument of option -u", 256, MEGABYTE));
        if (cl.hasOption("U"))
            dcmof.setMaxPDULengthSend(
                    parseInt(cl.getOptionValue("U"),
                    "illegal argument of option -U", 256, MEGABYTE));
        if (cl.hasOption("S"))
            dcmof.setSendBufferSize(
                    parseInt(cl.getOptionValue("S"),
                    "illegal argument of option -S", 256, MEGABYTE));
        if (cl.hasOption("s"))
            dcmof.setReceiveBufferSize(
                    parseInt(cl.getOptionValue("s"),
                    "illegal argument of option -s", 256, MEGABYTE));
        dcmof.setPackPDV(cl.hasOption("k"));
        dcmof.setTcpNoDelay(cl.hasOption("y"));
        if (cl.hasOption("a"))
            dcmof.setMaxOpsPerformed(zeroAsMaxInt(parseInt(
                    cl.getOptionValue("a"), "illegal max-opts", 0, 0xffff)));

        ArrayList tc = new ArrayList();
        tc.add(new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS, true));
        if (cl.hasOption("mwl"))
            dcmof.registerMWLSCP(new File(cl.getOptionValue("mwl")), tc);
        if (cl.hasOption("mwl"))
            dcmof.registerMPPSSCP(new File(cl.getOptionValue("mpps")), tc);
        if (cl.hasOption("ian"))
            dcmof.registerIANSCP(new File(cl.getOptionValue("ian")), tc);
        if (cl.hasOption("scn"))
            dcmof.registerSCNSCP(new File(cl.getOptionValue("scn")), tc);
        dcmof.setTransferCapability(
                (TransferCapability[]) tc.toArray(new TransferCapability[tc.size()]));
        try
        {
            dcmof.start();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void registerMWLSCP(File dir, ArrayList tc)
    {
        MWLSCP mwlscp = new MWLSCP(executor);
        mwlscp.setSource(dir);
        ae.register(mwlscp);
        tc.add(new TransferCapability(mwlscp.getSopClass(), tsuids, true));
    }

    private void registerMPPSSCP(File dir, ArrayList tc)
    {
        MPPSSCP mppsscp = new MPPSSCP();
        mppsscp.setDestination(dir);
        ae.register(mppsscp.getNCreateSCP());
        ae.register(mppsscp.getNSetSCP());
        tc.add(new TransferCapability(mppsscp.getNCreateSCP().getSopClass(), tsuids, true));
    }

    private void registerIANSCP(File dir, ArrayList tc)
    {
        IANSCP ianscp = new IANSCP();
        ianscp.setDestination(dir);
        ae.register(ianscp);
        tc.add(new TransferCapability(ianscp.getSopClass(), tsuids, true));
    }

    private void registerSCNSCP(File dir, ArrayList tc)
    {
        SCNSCP scnscp = new SCNSCP();
        scnscp.setDestination(dir);
        ae.register(scnscp);
        tc.add(new TransferCapability(scnscp.getSopClass(), tsuids, true));
    }

    private static int zeroAsMaxInt(int val)
    {
        return val > 0 ? val : Integer.MAX_VALUE;
    }

    private void setTransferCapability(TransferCapability[] tc)
    {
        ae.setTransferCapability(tc);        
    }

    private void setMaxOpsPerformed(int maxOps)
    {
        ae.setMaxOpsPerformed(maxOps);        
    }

    public void start() throws IOException
    {        
        device.startListening(executor );
        System.out.println("Start Server listening on port " + nc.getPort());
    }

    private static String[] split(String s, char delim, int defPos)
    {
        String[] s2 = new String[2];
        s2[defPos] = s;
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
        System.err.println("Try 'dcmof -h' for more information.");
        System.exit(1);
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
}
