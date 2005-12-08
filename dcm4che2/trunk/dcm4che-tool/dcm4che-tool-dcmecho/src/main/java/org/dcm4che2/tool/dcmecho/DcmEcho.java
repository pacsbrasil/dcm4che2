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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.config.DeviceConfiguration;
import org.dcm4che2.config.NetworkApplicationEntity;
import org.dcm4che2.config.NetworkConnection;
import org.dcm4che2.config.TransferCapability;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.ApplicationEntity;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;

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

    private static final String[] DEF_TS = { UID.ImplicitVRLittleEndian };
    private static final TransferCapability VERIFICATION_SCU = 
            new TransferCapability(UID.VerificationSOPClass, DEF_TS, false);

    private DeviceConfiguration remoteDeviceCfg = new DeviceConfiguration();
    private NetworkApplicationEntity remoteAECfg = new NetworkApplicationEntity();
    private NetworkConnection remoteConnCfg = new NetworkConnection();
    private DeviceConfiguration localDeviceCfg = new DeviceConfiguration();
    private NetworkApplicationEntity localAECfg = new NetworkApplicationEntity();
    private NetworkConnection localConnCfg = new NetworkConnection();
    private Association assoc;

    public DcmEcho()
    {
        remoteDeviceCfg.setNetworkApplicationEntity(
                new NetworkApplicationEntity[] { remoteAECfg });
        remoteDeviceCfg.setNetworkConnection(
                new NetworkConnection[] { remoteConnCfg });
        remoteAECfg.setAssociationAcceptor(true);
        remoteAECfg.setNetworkConnection(
                new NetworkConnection[] { remoteConnCfg });

        localDeviceCfg.setDeviceName("DCMECHO");
        localDeviceCfg.setNetworkApplicationEntity(
                new NetworkApplicationEntity[] { localAECfg });
        localDeviceCfg.setNetworkConnection(
                new NetworkConnection[] { localConnCfg });
        localAECfg.setAssociationInitiator(true);
        localAECfg.setAEtitle("DCMECHO");
        localAECfg.setNetworkConnection(
                new NetworkConnection[] { localConnCfg });
        localAECfg.setTransferCapability(
                new TransferCapability[] { VERIFICATION_SCU });

    }

    public final void setLocalHost(String hostname)
    {
        localConnCfg.setHostname(hostname);
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
        remoteAECfg.setAEtitle(called);
    }

    public final void setCalling(String calling)
    {
        localAECfg.setAEtitle(calling);
    }

    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        Option localAddr = new Option(
                "L",
                "local",
                true,
                "set AET, local address and port of local Application Entity,"
                        + "use ANONYMOUS and pick up an ephemeral port and a valid local"
                        + "address to bind the socket by default");
        localAddr.setArgName("calling[@host]");
        opts.addOption(localAddr);
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
        dcmecho.setCalledAET(calledAETAddress[0]);
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
            dcmecho.setLocalHost(toHostname(callingAETHost[1]));
        }

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

        try
        {
            dcmecho.echo();
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
            dcmecho.close();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Released connection to " + remoteAE);
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
        System.err.println("Try 'dcmecho -h' for more information.");
        System.exit(1);
    }

    public void open()
            throws IOException, ConfigurationException, InterruptedException
    {
        Device device = new Device(localDeviceCfg);
        ApplicationEntity ae = device.getApplicationEntity(localAECfg);

        assoc = ae.connect(remoteAECfg);
    }

    public void echo() throws IOException, InterruptedException
    {
        assoc.cecho();
    }

    public void close() throws InterruptedException
    {
        assoc.release(true);
    }
}
