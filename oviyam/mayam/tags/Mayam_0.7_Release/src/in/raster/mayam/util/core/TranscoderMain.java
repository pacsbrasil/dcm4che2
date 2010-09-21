package in.raster.mayam.util.core;
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
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.dcm4che.dict.UIDs;
/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1.1 $
 * @since 01.11.2003
 */
public final class TranscoderMain
{

    private static ResourceBundle rb =
        ResourceBundle.getBundle(TranscoderMain.class.getName());

    public static void main(String[] args)
    {
        int c;
        String arg;
        LongOpt[] longopts =
            {
                new LongOpt("trunc-post-pixeldata", LongOpt.NO_ARGUMENT, null, 't'),
                new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
                new LongOpt("ivle", LongOpt.NO_ARGUMENT, null, 'd'),
                new LongOpt("evle", LongOpt.NO_ARGUMENT, null, 'e'),
                new LongOpt("evbe", LongOpt.NO_ARGUMENT, null, 'b'),
                new LongOpt("jpll", LongOpt.NO_ARGUMENT, null, 'l'),
                new LongOpt("jlsl", LongOpt.NO_ARGUMENT, null, 's'),
                new LongOpt("j2kr", LongOpt.NO_ARGUMENT, null, 'r'),
                new LongOpt("jply", LongOpt.OPTIONAL_ARGUMENT, null, 'y'),
                new LongOpt("j2ki", LongOpt.OPTIONAL_ARGUMENT, null, 'i'),
                };
        // 
        Getopt g = new Getopt("dcm4chex-codec", args, "jhv", longopts, true);
        Transcoder t = new Transcoder();
        while ((c = g.getopt()) != -1)
            switch (c)
            {
                case 'd' :
                    t.setTransferSyntax(UIDs.ImplicitVRLittleEndian);
                    break;
                case 'e' :
                    t.setTransferSyntax(UIDs.ExplicitVRLittleEndian);
                    break;
                case 'b' :
                    t.setTransferSyntax(UIDs.ExplicitVRBigEndian);
                    break;
                case 'l' :
                    t.setTransferSyntax(UIDs.JPEGLossless);
                    break;
                case 's' :
                    t.setTransferSyntax(UIDs.JPEGLSLossless);
                    break;
                case 'r' :
                    t.setTransferSyntax(UIDs.JPEG2000Lossless);
                    break;
                case 'y' :
                    t.setTransferSyntax(UIDs.JPEGBaseline);
                    t.setCompressionQuality(
                        toCompressionQuality(g.getOptarg()));
                    break;
                case 'i' :
                    t.setTransferSyntax(UIDs.JPEG2000Lossy);
                    t.setEncodingRate(toEncodingRate(g.getOptarg()));
                    break;
                case 't' :
                    t.setTruncatePostPixelData(true);
                    break;
                case 'v' :
                    System.out.println(
                        MessageFormat.format(
                            rb.getString("version"),
                            new Object[] {
                                Package
                                    .getPackage("org.dcm4chex.codec")
                                    .getImplementationVersion()}));
                    return;
                case '?' :
                case 'h' :
                    System.out.println(rb.getString("usage"));
                    return;
            }
        if (!checkArgs(g.getOptind(), args))
        {
            System.out.println(rb.getString("usage"));
            return;
        }
        File dest = new File(args[args.length - 1]);
        for (int i = g.getOptind(); i + 1 < args.length; ++i)
        {
            transcode(t, new File(args[i]), dest);
        }
    }

    private static void transcode(Transcoder t, File src, File dest)
    {
        if (src.isDirectory())
        {
            File[] file = src.listFiles();
            for (int i = 0; i < file.length; i++)
            {
                transcode(t, file[i], dest);
            }
        } else
        {
            try
            {
                File outFile =
                    dest.isDirectory() ? new File(dest, src.getName()) : dest;
                long srcLength = src.length();            
                long begin = System.currentTimeMillis();
                t.transcode(src, outFile);
                long end = System.currentTimeMillis();
                long destLength = outFile.length();
              
            } catch (Exception e)
            {
                e.printStackTrace(System.out);
            }
        }
    }

    private static boolean checkArgs(int off, String[] args)
    {
        switch (args.length - off)
        {
            case 0 :
                System.out.println(rb.getString("missingArgs"));
                return false;
            case 1 :
                System.out.println(rb.getString("missingDest"));
                return false;
            case 2 :
                if (!(new File(args[off])).isDirectory())
                    break;
            default :
                if (!(new File(args[args.length - 1])).isDirectory())
                {
                    System.out.println(
                        MessageFormat.format(
                            rb.getString("needDir"),
                            new Object[] { args[args.length - 1] }));
                    return false;
                }
        }
        return true;
    }

    private static float toCompressionQuality(String s)
    {
        if (s != null)
        {
            try
            {
                int quality = Integer.parseInt(s);
                if (quality >= 0 && quality <= 100)
                {
                    return quality / 100.f;
                }
            } catch (IllegalArgumentException e)
            {e.printStackTrace();}
            System.out.println(
                MessageFormat.format(
                    rb.getString("ignoreQuality"),
                    new Object[] { s }));
        }
        return .75f;
    }

    private static double toEncodingRate(String s)
    {
        if (s != null)
        {
            try
            {
                double rate = Double.parseDouble(s);
                if (rate > 0)
                {
                    return rate;
                }
            } catch (IllegalArgumentException e)
            {e.printStackTrace();}
            System.out.println(
                MessageFormat.format(
                    rb.getString("ignoreRate"),
                    new Object[] { s }));
        }
        return 1.;
    }
}
