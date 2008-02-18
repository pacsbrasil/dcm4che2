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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package com.tiani.prnscp.client;

import java.io.File;

import java.util.Hashtable;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

/**
 *  Description of the Class
 *
 * @author    <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since     March 31, 2003
 */
public class PrintImage
{
    private final static String USAGE =
            "Usage: java -jar print-image.jar [OPTIONS] FILE\n\n" +
            "Print DICOM image in FILE\n\n" +
            "Options:\n" +
            " -a, --aet=AET   print on printer bound to specified AET. Default: TIANI_PRINT\n" +
            " -i, --info=CFG  print image with specified Configuration Information\n" +
            " -c, --color     print image in color print mode (default grayscale)\n" +
            " -h, --help      show this help and exit\n";

    private final static String NAME_PREFIX = "dcm4chex:service=Printer,calledAET=";

    private String aet = "TIANI_PRINT";
    private String cfg = "";
    private boolean color = false;


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
     *  Sets the color attribute of the PrintImage object
     *
     * @param  color  The new color value
     */
    public void setColor(boolean color)
    {
        this.color = color;
    }


    /**
     *  Description of the Method
     *
     * @param  file           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public void print(File file)
        throws Exception
    {
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        env.put("java.naming.provider.url", "localhost");
        InitialContext ic = new InitialContext(env);
        RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
        server.invoke(new ObjectName(NAME_PREFIX + aet), "printImage",
                new Object[]{
                file.getCanonicalPath(),
                cfg,
                new Boolean(color)
                },
                new String[]{
                String.class.getName(),
                String.class.getName(),
                Boolean.class.getName(),
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
                new LongOpt("aet", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
                new LongOpt("info", LongOpt.REQUIRED_ARGUMENT, null, 'i'),
                new LongOpt("color", LongOpt.NO_ARGUMENT, null, 'c'),
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
                };

        Getopt g = new Getopt("plut.jar", args, "a:i:ch", longopts, true);
        PrintImage inst = new PrintImage();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'a':
                    inst.setAET(g.getOptarg());
                    break;
                case 'i':
                    inst.setConfigInfo(g.getOptarg());
                    break;
                case 'c':
                    inst.setColor(true);
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
        inst.print(new File(args[optind]));
    }


    /**
     *  Description of the Method
     *
     * @param  prompt  Description of the Parameter
     */
    private static void exit(String prompt)
    {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }
}

