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

package org.dcm4che2.tool.dcmsnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.config.NetworkApplicationEntity;
import org.dcm4che2.config.NetworkConnection;
import org.dcm4che2.config.TransferCapability;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.io.TranscoderInputHandler;
import org.dcm4che2.net.ApplicationEntity;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Connector;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.PDVOutputStream;
import org.dcm4che2.util.StringUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 13, 2005
 */
public class DcmSnd {

    private static final int KB = 1024;
    private static final int MB = KB * KB;
    private static final int PEEK_LEN = 1024;
    private static final String USAGE = 
        "dcmsnd [Options] <aet>[@<host>[:<port>]] <file>|<directory>...";
    private static final String DESCRIPTION = 
        "Load composite DICOM Object(s) from specified DICOM file(s) and send it " +
        "to the specified remote Application Entity. If a directory is specified," +
        "DICOM Object in files under that directory and further sub-directories " +
        "are sent. If <port> is not specified, DICOM default port 104 is assumed. " +
        "If also no <host> is specified, localhost is assumed.\n" +
        "Options:";
    private static final String EXAMPLE = 
        "\nExample: dcmsnd STORESCP@localhost:11112 image.dcm \n" +
        "=> Send DICOM object image.dcm to Application Entity STORESCP, " +
        "listening on local port 11112.";

    private NetworkApplicationEntity remoteAECfg = new NetworkApplicationEntity();
    private NetworkConnection remoteConnCfg = new NetworkConnection();
    private Device device = new Device("DCMECHO");
    private ApplicationEntity ae = new ApplicationEntity();
    private Connector connector = new Connector();
    
    private HashMap as2ts = new HashMap();
    private ArrayList files = new ArrayList();
    private Association assoc;
    private int priority = 0;
    private int transcoderBufferSize = 1024;
    private int filesSent = 0;
    private long totalSize = 0L;

    public DcmSnd()
    {
        remoteAECfg.setInstalled(true);
        remoteAECfg.setAssociationAcceptor(true);
        remoteAECfg.setNetworkConnection(
                new NetworkConnection[] { remoteConnCfg });

        device.addApplicationEntity(ae);
        ae.addConnector(connector);
        ae.setAssociationInitiator(true);
        ae.setAETitle("DCMSND");
    }

    public final void setLocalHost(String hostname)
    {
        connector.setHostname(hostname);
    }

    public final void setRemoteHost(String hostname)
    {
        remoteConnCfg.setHostname(hostname);
    }

    public final void setRemotePort(int port)
    {
        remoteConnCfg.setPort(port);
    }

    public final void setCalledAET(String called)
    {
        remoteAECfg.setAETitle(called);
    }

    public final void setCalling(String calling)
    {
        ae.setAETitle(calling);
    }

    public final void setConnectTimeout(int connectTimeout)
    {
        connector.setConnectTimeout(connectTimeout);
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
    
    public final void setPriority(int priority)
    {
        this.priority = priority;
    }

    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        connector.setTcpNoDelay(tcpNoDelay);
    }

    public final void setAcceptTimeout(int timeout)
    {
        connector.setAcceptTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout)
    {
        connector.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout)
    {
        connector.setSocketCloseDelay(timeout);
    }

    public final void setMaxPDULengthSend(int maxPDULength)
    {
        ae.setMaxPDULengthSend(maxPDULength);
    }

    public final void setReceiveBufferSize(int bufferSize)
    {
        connector.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize)
    {
        connector.setSendBufferSize(bufferSize);
    }

    public final void setTranscoderBufferSize(int transcoderBufferSize)
    {
        this.transcoderBufferSize = transcoderBufferSize;
    }
    
    public final int getNumberOfFilesToSend()
    {
        return files.size();
    }    

    public final int getNumberOfFilesSent()
    {
        return filesSent;
    }    

    public final long getTotalSizeSent()
    {
        return totalSize;
    }    

    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        Option localAddr = new Option("L", "local", true,
                "set AET, local address and port of local Application Entity, " +
                "use ANONYMOUS as calling AET and pick up an ephemeral port " +
                "and a valid local address to bind the socket by default");
        localAddr.setArgName("calling[@host[:port]]");
        opts.addOption(localAddr);
        Option maxOpsInvoked = new Option("a", "async", true,
                "maximum number of outstanding operations it may invoke " +
                "asynchronously, 1 (=synchronous) by default.");
        maxOpsInvoked.setArgName("max-ops");
        opts.addOption(maxOpsInvoked);
        opts.addOption("k", "pack-pdv", false, 
                "pack command and data PDV in one P-DATA-TF PDU, " +
        "send only one PDV in one P-Data-TF PDU by default.");
        opts.addOption("y", "tcp-no-delay", false, 
                "set TCP_NODELAY socket option to true, false by default");
        Option conTimeout = new Option("C", "connect-timeout", true,
                "timeout in ms for TCP connect, no timeout by default");
        conTimeout.setArgName("timeout");
        opts.addOption(conTimeout);
        Option closeDelay = new Option("c", "close-delay", true,
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        closeDelay.setArgName("delay");
        opts.addOption(closeDelay);
        Option checkPeriod = new Option("D", "reaper-period", true,
                "period in ms to check for outstanding DIMSE-RSP, 10s by default");
        checkPeriod.setArgName("period");
        Option rspTimeout = new Option("R", "rsp-timeout", true,
                "timeout in ms for receiving DIMSE-RSP, 60s by default");
        rspTimeout.setArgName("timeout");
        opts.addOption(rspTimeout);
        Option acTimeout = new Option("T", "accept-timeout", true,
                "timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
        acTimeout.setArgName("timeout");
        opts.addOption(acTimeout);
        Option rpTimeout = new Option("t", "release-timeout", true,
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        rpTimeout.setArgName("timeout");
        opts.addOption(rpTimeout);
        Option rcvPduLen = new Option("u", "rcv-pdu-len", true,
                "maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        rcvPduLen.setArgName("max-len");
        opts.addOption(rcvPduLen);
        Option sndPduLen = new Option("U", "snd-pdu-len", true,
                "maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        sndPduLen.setArgName("max-len");
        opts.addOption(sndPduLen);
        Option soRcvBufSize = new Option("s", "so-rcv-buf-size", true,
                "set SO_RCVBUF socket option to specified value in KB");
        soRcvBufSize.setArgName("size");
        opts.addOption(soRcvBufSize);
        Option soSndBufSize = new Option("S", "so-snd-buf-size", true,
                "set SO_SNDBUF socket option to specified value in KB");
        soSndBufSize.setArgName("size");
        opts.addOption(soSndBufSize);
        Option transcoderBufSize = new Option("b", "transcoder-buf-size", true,
                "transcoder buffer size in KB, 1KB by default");
        transcoderBufSize.setArgName("size");
        opts.addOption(transcoderBufSize);
        opts.addOption("p", "low-priority", false, 
                "LOW priority of the C-STORE operation, MEDIUM by default");
        opts.addOption("P", "high-priority", false,
                "HIGH priority of the C-STORE operation, MEDIUM by default");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try
        {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e)
        {
            exit("dcmsnd: " + e.getMessage());
        }
        if (cl.hasOption('V'))
        {
            Package p = DcmSnd.class.getPackage();
            System.out.println("dcmsnd v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() < 2)
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
        DcmSnd dcmsnd = new DcmSnd();
        final List argList = cl.getArgList();
        String remoteAE = (String) argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmsnd.setCalledAET(calledAETAddress[0]);
        if (calledAETAddress[1] == null)
        {
            dcmsnd.setRemoteHost("127.0.0.1");
            dcmsnd.setRemotePort(104);
        }
        else
        {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmsnd.setRemoteHost(hostPort[0]);
            dcmsnd.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L"))
        {
            String localAE = (String) cl.getOptionValue("L");
            String[] callingAETHost = split(localAE, '@');
            dcmsnd.setCalling(callingAETHost[0]);
            dcmsnd.setLocalHost(toHostname(callingAETHost[1]));
        }
        if (cl.hasOption("C"))
            dcmsnd.setConnectTimeout(
                    parseInt(cl.getOptionValue("C"),
                    "illegal argument of option -C", 1, Integer.MAX_VALUE));
        if (cl.hasOption("D"))
            dcmsnd.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue("D"),
                    "illegal argument of option -D", 1, Integer.MAX_VALUE));
        if (cl.hasOption("R"))
            dcmsnd.setDimseRspTimeout(
                    parseInt(cl.getOptionValue("R"),
                    "illegal argument of option -R", 1, Integer.MAX_VALUE));
        if (cl.hasOption("T"))
            dcmsnd.setAcceptTimeout(
                    parseInt(cl.getOptionValue("T"),
                    "illegal argument of option -T", 1, Integer.MAX_VALUE));
        if (cl.hasOption("t"))
            dcmsnd.setReleaseTimeout(
                    parseInt(cl.getOptionValue("t"),
                    "illegal argument of option -t", 1, Integer.MAX_VALUE));
        if (cl.hasOption("c"))
            dcmsnd.setSocketCloseDelay(
                    parseInt(cl.getOptionValue("c"),
                    "illegal argument of option -c", 1, 10000));
        if (cl.hasOption("u"))
            dcmsnd.setMaxPDULengthReceive(
                    parseInt(cl.getOptionValue("u"),
                    "illegal argument of option -u", 1, 10000) * KB);
        if (cl.hasOption("U"))
            dcmsnd.setMaxPDULengthSend(
                    parseInt(cl.getOptionValue("U"),
                    "illegal argument of option -U", 1, 10000) * KB);
        if (cl.hasOption("S"))
            dcmsnd.setSendBufferSize(
                    parseInt(cl.getOptionValue("S"),
                    "illegal argument of option -S", 1, 10000) * KB);
        if (cl.hasOption("s"))
            dcmsnd.setReceiveBufferSize(
                    parseInt(cl.getOptionValue("s"),
                    "illegal argument of option -s", 1, 10000) * KB);
        if (cl.hasOption("b"))
            dcmsnd.setTranscoderBufferSize(
                    parseInt(cl.getOptionValue("b"),
                    "illegal argument of option -b", 1, 10000) * KB);
        dcmsnd.setPackPDV(cl.hasOption("k"));
        dcmsnd.setTcpNoDelay(cl.hasOption("y"));
        if (cl.hasOption("a"))
            dcmsnd.setMaxOpsInvoked(parseInt(cl.getOptionValue("a"),
                    "illegal max-opts", 1, 0xffff));
        if (cl.hasOption("p"))
            dcmsnd.setPriority(CommandUtils.LOW);
        if (cl.hasOption("P"))
            dcmsnd.setPriority(CommandUtils.HIGH);
        System.out.println("Scanning files to send");
        long t1 = System.currentTimeMillis();
        for (int i = 1, n = argList.size(); i < n; ++i)
            dcmsnd.addFile(new File((String) argList.get(i)));
        long t2 = System.currentTimeMillis();
        if (dcmsnd.getNumberOfFilesToSend() == 0)
        {
            System.exit(2);
        }
        System.out.println("\nScanned " + dcmsnd.getNumberOfFilesToSend()
                + " files in " + ((t2 - t1) / 1000F)
                + "s (=" + ((t2 - t1) / dcmsnd.getNumberOfFilesToSend())
                + "ms/file)");
        dcmsnd.configureTransferCapability();
        t1 = System.currentTimeMillis();
        try
        {
            dcmsnd.open();
        }
        catch (Exception e)
        {
            System.err.println("ERROR: Failed to establish association:" 
                    + e.getMessage());
            System.exit(2);
        }
        t2 = System.currentTimeMillis();
        System.out.println("Connected to " + remoteAE + " in "
                + ((t2 - t1) / 1000F) + "s");       
        
        t1 = System.currentTimeMillis();
        dcmsnd.send();
        t2 = System.currentTimeMillis();
        prompt(dcmsnd, (t2 - t1) / 1000F);
        dcmsnd.close();
        System.out.println("Released connection to " + remoteAE);     
    }

    private static void prompt(DcmSnd dcmsnd, float seconds)
    {
        System.out.print("\nSent ");
        System.out.print(dcmsnd.getNumberOfFilesSent());
        System.out.print(" objects (=");
        promptBytes(dcmsnd.getTotalSizeSent());
        System.out.print(") in ");
        System.out.print(seconds);
        System.out.print("s (=");
        promptBytes(dcmsnd.getTotalSizeSent() / seconds);
        System.out.println("/s)");
    }

    private static void promptBytes(float totalSizeSent)
    {
        if (totalSizeSent > MB)
        {
            System.out.print(totalSizeSent / MB);
            System.out.print("MB");
        }
        else
        {
            System.out.print(totalSizeSent / KB);
            System.out.print("KB");
        }
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

    private static String[] split(String s, char delim)
    {
        String[] s2 = { s, null };
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
        System.err.println("Try 'dcmsnd -h' for more information.");
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
        
    public void addFile(File f)
    {
        if (f.isDirectory())
        {            
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++)
                addFile(fs[i]);
            return;
        }
        FileInfo info = new FileInfo(f);
        DicomObject dcmObj = new BasicDicomObject();
        try
        {
            DicomInputStream in = new DicomInputStream(f);
            try
            {
                in.setHandler(new StopTagInputHandler(Tag.StudyDate));
                in.readDicomObject(dcmObj, PEEK_LEN);
                info.tsuid = in.getTransferSyntax().uid();
                info.fmiEndPos = in.getEndOfFileMetaInfoPosition();
            }
            finally
            {
                try
                {
                    in.close();
                } catch (IOException ignore) {}
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("WARNING: Failed to parse " + f + " - skipped.");
            System.out.print('F');
            return;
        }
        info.cuid = dcmObj.getString(Tag.SOPClassUID);
        if (info.cuid == null)
        {
            System.err.println("WARNING: Missing SOP Class UID in " + f 
                    + " - skipped.");
            System.out.print('F');
            return;
        }   
        info.iuid = dcmObj.getString(Tag.SOPInstanceUID);
        if (info.iuid == null)
        {
            System.err.println("WARNING: Missing SOP Instance UID in " + f 
                    + " - skipped.");
            System.out.print('F');
            return;
        }   
        addTransferCapability(info.cuid, info.tsuid);
        files.add(info);
        System.out.print('.');
    }

    private void addTransferCapability(String cuid, String tsuid)
    {
        HashSet ts = (HashSet) as2ts.get(cuid);
        if (ts == null)
        {
            ts = new HashSet();
            ts.add(UID.ImplicitVRLittleEndian);
            as2ts.put(cuid, ts);
        }
        ts.add(tsuid);        
    }

    private void configureTransferCapability()
    {
        Iterator iter = as2ts.entrySet().iterator();
        TransferCapability[] tc = new TransferCapability[as2ts.size()];
        for (int i = 0; i < tc.length; i++)
        {
            Map.Entry e = (Map.Entry) iter.next();
            String cuid = (String) e.getKey();
            HashSet ts = (HashSet) e.getValue();
            tc[i] = new TransferCapability(cuid, 
                    (String[]) ts.toArray(new String[ts.size()]), false);
        }
        ae.setTransferCapability(tc);
    }

    
    public void open()
    throws IOException, ConfigurationException, InterruptedException
    {
        assoc = ae.connect(remoteAECfg);
    }

    public void send()
    {
        String[] s1 = new String[1];
        String[] s3 = new String[3];
        for (int i = 0, n = files.size(); i < n; ++i)
        {
            FileInfo info = (FileInfo) files.get(i);
            try
            {
                DimseRSPHandler rspHandler = new DimseRSPHandler(){
                    public void onDimseRSP(Association as, DicomObject cmd, 
                            DicomObject data)
                    {
                        DcmSnd.this.onDimseRSP(as, cmd, data);
                    }
                };
                assoc.cstore(info.cuid, info.iuid, priority, new DataWriter(info), 
                        compatibleTS(info.tsuid, s1, s3), rspHandler);
            }
            catch (NoPresentationContextException e)
            {
                System.err.println("WARNING: " + e.getMessage()
                        + " - cannot send " + info.f);
                System.out.print('F');
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.err.println("ERROR: Failed to send - " + info.f
                        + ": " + e.getMessage());
                System.out.print('F');
            }
            catch (InterruptedException e)
            {
                // should not happen
                e.printStackTrace();
            }
        }
        try
        {
            assoc.waitForDimseRSP();
        }
        catch (InterruptedException e)
        {
            // should not happen
            e.printStackTrace();
        }
    }        
    
    private String[] compatibleTS(String tsuid, String[] s1, String[] s3)
    {
        if (tsuid.equals(UID.ExplicitVRLittleEndian))
        {
            s3[0] = UID.ExplicitVRLittleEndian;
            s3[1] = UID.ImplicitVRLittleEndian;
            s3[2] = UID.ExplicitVRBigEndian;
            return s3;
        }
        if (tsuid.equals(UID.ImplicitVRLittleEndian))
        {
            s3[0] = UID.ImplicitVRLittleEndian;
            s3[1] = UID.ExplicitVRLittleEndian;
            s3[2] = UID.ExplicitVRBigEndian;
            return s3;
        }
        if (tsuid.equals(UID.ExplicitVRBigEndian))
        {
            s3[0] = UID.ExplicitVRBigEndian;
            s3[1] = UID.ExplicitVRLittleEndian;
            s3[2] = UID.ImplicitVRLittleEndian;
            return s3;
        }
        s1[0] = tsuid;
        return s1;
    }

    public void close()
    {
        try
        {
            assoc.release(false);
        }
        catch (InterruptedException e)
        {
             e.printStackTrace();
        }
    }    
    
    private static final class FileInfo
    {
        File f;
        String cuid;
        String iuid;
        String tsuid;
        long fmiEndPos;
        long length;

        public FileInfo(File f)
        {
            this.f = f;
            this.length = f.length();
        }
        
    }

    private class DataWriter implements org.dcm4che2.net.DataWriter
    {

        private FileInfo info;

        public DataWriter(FileInfo info)
        {
            this.info = info;
        }
        
        public void writeTo(PDVOutputStream out, String tsuid) throws IOException
        {
            if (tsuid.equals(info.tsuid))
            {
                FileInputStream fis = new FileInputStream(info.f);
                try
                {
                    long skip = info.fmiEndPos;
                    while (skip > 0)
                        skip -= fis.skip(skip);
                    out.copyFrom(fis);
                }
                finally
                {
                    fis.close();
                }
            }
            else
            {
                DicomInputStream dis = new DicomInputStream(info.f);
                try
                {
                    DicomOutputStream dos = new DicomOutputStream(out);
                    dos.setTransferSyntax(tsuid);
                    TranscoderInputHandler h = 
                        new TranscoderInputHandler(dos, transcoderBufferSize);
                    dis.setHandler(h);
                    dis.readDicomObject();
                }
                finally
                {
                    dis.close();
                }
            }
        }

    }

    private void promptErrRSP(String prefix, int status, FileInfo info,
            DicomObject cmd)
    {
        System.err.println(prefix + StringUtils.shortToHex(status) + "H for "
                + info.f + ", cuid=" + info.cuid + ", tsuid=" + info.tsuid);
        System.err.println(cmd.toString());
    }

    private void onDimseRSP(Association as, DicomObject cmd, DicomObject data)
    {
        int status = cmd.getInt(Tag.Status);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        FileInfo info = (FileInfo) files.get(msgId  - 1);
        switch(status)
        {
        case 0:
            totalSize += info.length;
            ++filesSent;
            System.out.print('.');
            break;
        case 0xB000:
        case 0xB006:
        case 0xB007:
            totalSize += info.length;
            ++filesSent;
            promptErrRSP("WARNING: Received RSP with Status ", status, info, cmd);
            System.out.print('W');
            break;
        default:
            promptErrRSP("ERROR: Received RSP with Status ", status, info, cmd);
            System.out.print('F');
        }
    }

}
