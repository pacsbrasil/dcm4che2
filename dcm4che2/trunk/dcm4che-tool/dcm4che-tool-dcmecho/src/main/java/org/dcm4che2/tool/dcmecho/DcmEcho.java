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

package org.dcm4che2.tool.dcmecho;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.TransferCapability;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 13, 2005
 */
public class DcmEcho
{

    private static final String USAGE = "dcmecho [Options] <aet>[@<host>[:<port>]]";
    private static final String DESCRIPTION = 
            "Send DICOM Echo to the specified remote Application Entity. "
            + "If <port> is not specified, DICOM default port 104 is assumed. "
            + "If also no <host> is specified localhost is assumed.\n"
            + "Options:";
    private static final String EXAMPLE = 
            "\nExample: dcmecho STORESCP@localhost:11112 \n"
            + "=> Verify connection to Application Entity STORESCP, "
            + "listening on local port 11112.";

    private static final String[] DEF_TS = { UID.IMPLICIT_VR_LITTLE_ENDIAN };
    private static final TransferCapability VERIFICATION_SCU = 
            new TransferCapability(UID.VERIFICATION_SOP_CLASS, DEF_TS, TransferCapability.SCU);

    private Executor executor = new NewThreadExecutor("DCMECHO");
    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private NetworkConnection remoteConn = new NetworkConnection();
    private Device device = new Device("DCMECHO");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private NetworkConnection conn = new NetworkConnection();
    private Association assoc;

    public DcmEcho()
    {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(
                new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAETitle("DCMECHO");
        ae.setTransferCapability(new TransferCapability[] { VERIFICATION_SCU });
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

    public final void setCalledAET(String called, boolean reuse)
    {
        remoteAE.setAETitle(called);
        if (reuse)
            ae.setReuseAssocationToAETitle(new String[]{called});
    }

    public final void setCalling(String calling)
    {
        ae.setAETitle(calling);
    }

    public final void setIdleTimeout(int timeout)
    {
        ae.setIdleTimeout(timeout);        
    }

    public final void setAssociationReaperPeriod(int period)
    {
        device.setAssociationReaperPeriod(period);
    }
    
    public final void setDimseRspTimeout(int timeout)
    {
        ae.setDimseRspTimeout(timeout);        
    }
    
    public final void setConnectTimeout(int connectTimeout)
    {
        conn.setConnectTimeout(connectTimeout);
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
    
    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        OptionBuilder.withArgName("aet[@host]");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set AET and local address of local Application Entity, use " +
                "ANONYMOUS and pick up any valid local address to bind the " +
                "socket by default");
        opts.addOption(OptionBuilder.create("L"));
        
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
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving DIMSE-RQ, 10s by default");
        opts.addOption(OptionBuilder.create("idleTO"));
        
        OptionBuilder.withArgName("num");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "repeat C-ECHO RQ several times");
        opts.addOption(OptionBuilder.create("repeat"));
        
        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms between repeated C-FIND RQ, immediately after C-FIND RSP by default");
        opts.addOption(OptionBuilder.create("repeatdelay"));
        
        opts.addOption("reuseassoc", false, "Reuse association for repeated C-ECHO");
        opts.addOption("closeassoc", false, "Close association after each C-ECHO");
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
            exit("dcmecho: " + e.getMessage());
        }
        if (cl.hasOption('V'))
        {
            Package p = DcmEcho.class.getPackage();
            System.out.println("dcmecho v" + p.getImplementationVersion());
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
        DcmEcho dcmecho = new DcmEcho();
        final List argList = cl.getArgList();
        String remoteAE = (String) argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmecho.setCalledAET(calledAETAddress[0], cl.hasOption("reuse-assoc"));
        if (calledAETAddress[1] == null)
        {
            dcmecho.setRemoteHost("127.0.0.1");
            dcmecho.setRemotePort(104);
        }
        else
        {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmecho.setRemoteHost(hostPort[0]);
            dcmecho.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L"))
        {
            String localAE = (String) cl.getOptionValue("L");
            String[] callingAETHost = split(localAE, '@');
            dcmecho.setCalling(callingAETHost[0]);
            if (callingAETHost[1] != null) {
                dcmecho.setLocalHost(callingAETHost[1]);
            }
        }
        if (cl.hasOption("connectTO"))
            dcmecho.setConnectTimeout(
                    parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1, Integer.MAX_VALUE));
        dcmecho.setIdleTimeout(cl.hasOption("idleTO")
                ? parseInt(cl.getOptionValue("idleTO"),
                    "illegal argument of option -idleTO", 1, Integer.MAX_VALUE)
                : 10000);
        if (cl.hasOption("reaper"))
            dcmecho.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue("reaper"),
                    "illegal argument of option -reaper", 1, Integer.MAX_VALUE));
        if (cl.hasOption("rspTO"))
            dcmecho.setDimseRspTimeout(
                    parseInt(cl.getOptionValue("rspTO"),
                    "illegal argument of option -rspTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmecho.setAcceptTimeout(
                    parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmecho.setReleaseTimeout(
                    parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO", 1, Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmecho.setSocketCloseDelay(
                    parseInt(cl.getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        
        int repeat = cl.hasOption("repeat")
                ? parseInt(cl.getOptionValue("repeat"),
                    "illegal argument of option -repeat", 1, Integer.MAX_VALUE)
                : 0;
        int interval = cl.hasOption("repeatdelay")
                ? parseInt(cl.getOptionValue("repeatdelay"),
                    "illegal argument of option -repeatdelay", 1, Integer.MAX_VALUE)
                : 0;
        boolean closeAssoc = cl.hasOption("closeassoc");

        long t1 = System.currentTimeMillis();
        try
        {
            dcmecho.open();
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

        for (;;) {
            try
            {
                dcmecho.echo();
                long t3 = System.currentTimeMillis();
                System.out.println("Perform Verification in "
                        + ((t2 - t3) / 1000F) + "s");
                if (repeat == 0 || closeAssoc) {
                    dcmecho.close();
                    System.out.println("Released connection to " + remoteAE);
                }
                if (repeat-- == 0)
                    break;
                Thread.sleep(interval);
                long t4 = System.currentTimeMillis();
                dcmecho.open();
                t2 = System.currentTimeMillis();
                System.out.println("Reconnect or reuse connection to "
                        + remoteAE + " in " + ((t4 - t1) / 1000F) + "s");
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
            catch (ConfigurationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
        System.err.println("Try 'dcmecho -h' for more information.");
        System.exit(1);
    }

    public void open()
            throws IOException, ConfigurationException, InterruptedException
    {
        assoc = ae.connect(remoteAE, executor);
    }

    public void echo() throws IOException, InterruptedException
    {
        assoc.cecho().next();
    }

    public void close() throws InterruptedException
    {
        assoc.release(true);
    }
}
