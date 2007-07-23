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
 * See listed authors below.
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

package org.dcm4che2.tool.dcm2jpg;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jul 11, 2007
 */
public class Dcm2Jpg {

    public void convert(File src, File dest) throws IOException {
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        ImageReader reader = (ImageReader) iter.next();
        DicomImageReadParam param = 
                (DicomImageReadParam) reader.getDefaultReadParam();
        ImageInputStream iis = ImageIO.createImageInputStream(src);
        BufferedImage bi;
        try {
            reader.setInput(iis, false);
            bi = reader.read(0, param);
            if (bi == null) {
                System.out.println("\nError: " + src + " - couldn't read!");
                return;
            }
        } finally {
            try {
                iis.close();
            } catch (IOException ignore) {
            }
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
        try {
            JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
            enc.encode(bi);
        } finally {
            out.close();
        }
        System.out.print('.');
    }

    public int mconvert(String[] args, int optind, File destDir)
            throws IOException {
        int count = 0;
        for (int i = optind, n = args.length - 1; i < n; ++i) {
            File src = new File(args[i]);
            count += mconvert(src, new File(destDir, src.getName()));
        }
        return count;
    }

    public int mconvert(File src, File dest) throws IOException {
        if (src.isFile()) {
            convert(src, dest);
            return 1;
        }
        File[] files = src.listFiles();
        if (files.length > 0 && !dest.exists()) {
            dest.mkdirs();
        }
        int count = 0;
        for (int i = 0; i < files.length; ++i) {
            count += mconvert(files[i], new File(dest, files[i].getName()));
        }
        return count;
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[]) throws Exception {
        Dcm2Jpg dcm2jpg = new Dcm2Jpg();

        int argc = args.length;
        if (argc < 2) {
            exit("dcm2jpg: missing argument\n");
        }

        File dest = new File(args[args.length - 1]);
        long t1 = System.currentTimeMillis();
        int count = 1;
        if (dest.isDirectory()) {
            count = dcm2jpg.mconvert(args, 0, dest);
        } else {
            File src = new File(args[0]);
            if (argc > 2 || src.isDirectory()) {
                exit("dcm2jpg: when converting several files, "
                        + "last argument must be a directory\n");
            }
            dcm2jpg.convert(src, dest);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("\nconverted " + count + " files in " + (t2 - t1)
                / 1000f + " s.");
    }

    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE = "Usage: dcm2jpg SOURCE DEST\n"
            + "    or dcm2jpg SOURCE... DIRECTORY\n\n"
            + "Convert DICOM image(s) to JPEG(s).\n\n";
}
