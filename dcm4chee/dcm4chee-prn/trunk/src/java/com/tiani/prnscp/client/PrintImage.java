/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.client;

import java.util.Hashtable;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

/**
 *  Description of the Class
 *
 * @author    gunter
 * @since     March 31, 2003
 */
public class PrintImage
{
    private final static String USAGE =
            "Usage: java -jar print-image.jar [OPTIONS] <file>\n\n" +
            "Print DICOM image <file>\n\n" +
            "Options:\n" +
            " -r --remote <host> print on remote host instead local.\n" +
            " -a --aet    <aet>  printer Application Entity Title. Default: TIANI_PRINT\n" +
            " -c --cfg    <info> print image with specified Configuration Information\n" +
            " -h --help          show this help and exit\n";

    private final static String NAME_PREFIX = "dcm4chex:service=Printer,calledAET=";

    private String host = "localhost";
    private String aet = "TIANI_PRINT";
    private String cfg = "";


    /**
     *  Sets the host attribute of the PrintImage object
     *
     * @param  host  The new host value
     */
    public void setHost(String host)
    {
        this.host = host;
    }


    /**
     *  Sets the aET attribute of the PrintImage object
     *
     * @param  aet  The new aET value
     */
    public void setAET(String aet)
    {
        this.aet = aet;
    }


    /**
     *  Sets the configInfo attribute of the PrintImage object
     *
     * @param  cfg  The new configInfo value
     */
    public void setConfigInfo(String cfg)
    {
        this.cfg = cfg;
    }


    /**
     *  Description of the Method
     *
     * @param  fname  Description of the Parameter
     */
    public void print(String fname) throws Exception
    {
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        env.put("java.naming.provider.url", host);
        InitialContext ic = new InitialContext(env);
        RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
        server.invoke(new ObjectName(NAME_PREFIX + aet), "printImage",
                new Object[]{
                fname,
                cfg
                },
                new String[]{
                String.class.getName(),
                String.class.getName(),
                });
    }


    /**
     *  The main program for the PrintImage class
     *
     * @param  args           The command line arguments
     * @exception  Exception  Description of the Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        LongOpt[] longopts = {
                new LongOpt("remote", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
                new LongOpt("aet", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
                new LongOpt("cfg", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
                };

        Getopt g = new Getopt("plut.jar", args, "r:a:c:h", longopts, true);
        PrintImage inst = new PrintImage();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'r':
                    inst.setHost(g.getOptarg());
                    break;
                case 'a':
                    inst.setAET(g.getOptarg());
                    break;
                case 'c':
                    inst.setConfigInfo(g.getOptarg());
                    break;
                case 'h':
                case '?':
                    exit("");
                    break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc != 1) {
            exit("print-image.jar: wrong number of arguments\n");
        }
        inst.print(args[optind]);
    }


    private static void exit(String prompt)
    {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }
}

