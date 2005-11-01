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
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.AssociationRequestor;
import org.dcm4che2.net.CommandFactory;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.PresentationContext;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 13, 2005
 */
public class DcmEcho {

    private static final String USAGE = 
        "dcmecho [Options] <aet>[@<host>[:<port>]]";
    private static final String DESCRIPTION = 
        "Send DICOM Echo to the specified remote Application Entity. " +
        "If <port> is not specified, DICOM default port 104 is assumed. " +
        "If also no <host> is specified localhost is assumed.\n" +
        "Options:";
    private static final String EXAMPLE = 
        "\nExample: dcmecho STORESCP@localhost:11112 \n" +
        "=> Verify connection to Application Entity STORESCP, " +
        "listening on local port 11112.";
    
    private static final int PCID = 1;
    private static final int MSGID = 1;

    private AAssociateRQ aarq = new AAssociateRQ();
    private AssociationRequestor requestor = new AssociationRequestor();
    private Association assoc;
    private InetSocketAddress remoteAddress;
    private InetSocketAddress localAddress;

    public DcmEcho()
    {
        PresentationContext pc = new PresentationContext();
        pc.setPCID(PCID);
        pc.setAbstractSyntax(UID.VerificationSOPClass);
        pc.addTransferSyntax(UID.ImplicitVRLittleEndian);
        aarq.addPresentationContext(pc);        
    }
    
    public final void setLocalAddress(InetSocketAddress localAddress)
    {
        this.localAddress = localAddress;
    }

    public final void setRemoteAddress(InetSocketAddress remoteAddress)
    {
        this.remoteAddress = remoteAddress;
    }

    public final void setCalledAET(String called)
    {
        aarq.setCalledAET(called);
    }

    public final void setCalling(String calling)
    {
        aarq.setCallingAET(calling);
    }


    private static CommandLine parse(String[] args)
    {
        Options opts = new Options();
        Option localAddr = new Option("L", "local", true,
                "set AET, local address and port of local Application Entity," +
                "use ANONYMOUS and pick up an ephemeral port and a valid local" +
                "address to bind the socket by default");
        localAddr.setArgName("calling[@host[:port]]");
        opts.addOption(localAddr);
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try
        {
            cl = new PosixParser().parse(opts, args);
        } catch (ParseException e)
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
        dcmecho.setRemoteAddress(calledAETAddress[1] == null
                ? new InetSocketAddress("localhost", 104)
                : toSocketAddress(calledAETAddress[1], 104));
        if (cl.hasOption("L"))
        {
            String localAE = (String) cl.getOptionValue("L");
            String[] callingAETAddress = split(localAE, '@');
            dcmecho.setCalling(callingAETAddress[0]);
            if (callingAETAddress[1] != null)
                dcmecho.setLocalAddress(toSocketAddress(callingAETAddress[1], 0));
        }

        long t1 = System.currentTimeMillis();
        try
        {
            dcmecho.open();
        }
        catch (IOException e)
        {
            System.err.println("ERROR: Failed to establish association:" 
                    + e.getMessage());
            System.exit(2);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Connected to " + remoteAE + " in "
                + ((t2 - t1) / 1000F) + "s");       
        
        dcmecho.echo();
        dcmecho.close();
        System.out.println("Released connection to " + remoteAE);     
    }

    private static InetSocketAddress toSocketAddress(String s, int defPort)
    {
        String[] s2 = split(s, ':');
        return new InetSocketAddress(s2[0], 
                s2[1] != null 
                    ? parseInt(s2[1], "illegal port number", 1, 0xffff)
                    : defPort);
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
        System.err.println("Try 'dcmecho -h' for more information.");
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
        

    public void open() throws IOException
    {
        assoc = requestor.connect(aarq, remoteAddress, localAddress);
    }

    public void echo()
    {
            AAssociateAC aaac = assoc.getAssociateAC();
            PresentationContext pc = aaac.getPresentationContext(PCID);
            if (pc.getResult() == PresentationContext.ACCEPTANCE)
            {
                DicomObject rq = CommandFactory.newCEchoRQ(MSGID);
                System.out.println("Sending:\n" + rq);
                DimseRSP rsp;
                try
                {
                    rsp = assoc.invoke(PCID, rq);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    System.err.println("ERROR: Failed to send C-ECHO-RQ: "
                            + e.getMessage());
                    return;
                }
                try
                {
                    rsp.next();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    System.err.println("ERROR: Failed to receive C-ECHO-RSP: "
                            + e.getMessage());
                    return;
                }
                System.out.println("Received:\n" + rsp.getCommand());
            }
            else 
            {
                aarq = assoc.getAssociateRQ();
                System.err.println("WARNING: " + aarq.getCalledAET()
                        + " rejected " + aarq.getPresentationContext(PCID));
            }
      }        
    
    public void close()
    {
        assoc.release();
    }    
}
