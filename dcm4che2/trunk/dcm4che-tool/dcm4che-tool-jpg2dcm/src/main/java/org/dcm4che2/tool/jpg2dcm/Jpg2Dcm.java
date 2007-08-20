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

package org.dcm4che2.tool.jpg2dcm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.util.UIDUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Apr 1, 2006
 *
 */
public class Jpg2Dcm {

    private static final String USAGE = 
        "jpg2dcm [Options] <jpgfile> <dcmfile>";
    private static final String DESCRIPTION = 
        "Encapsulate JPEG Image into DICOM Object.\nOptions:";
    private static final String EXAMPLE = 
        "jpg2dcm -c jpg2dcm.cfg report.jpg report.dcm\n" +
        "=> Encapulate JEPG Image image.jpg into DICOM Object stored to " +
        "image.dcm using DICOM Attribute values specified in Configuration " +
        "file jpg2dcm.cfg.";
    
    private String charset = "ISO_IR 100";
    private int bufferSize = 8192;
    private String transferSyntax = UID.JPEGBaseline1;
    private Properties cfg = new Properties();

    public Jpg2Dcm() {
        try {
            cfg.load(Jpg2Dcm.class.getResourceAsStream("jpg2dcm.cfg"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public final void setCharset(String charset) {
        this.charset = charset;
    }

    public final void setBufferSize(int bufferSize) {
        if (bufferSize < 64) {
            throw new IllegalArgumentException("bufferSize: " + bufferSize);
        }
        this.bufferSize = bufferSize;
    }

    private final void setTransferSyntax(String uid) {
        this.transferSyntax = uid;
    }

    
    private void loadConfiguration(File cfgFile) throws IOException {
        Properties tmp = new Properties(cfg);
        InputStream in = new BufferedInputStream(new FileInputStream(cfgFile));
        try {
            tmp.load(in);
        } finally {
            in.close();
        }
        cfg = tmp;
    }
    
    public void convert(File jpgFile, File dcmFile) throws IOException { 
        FileImageInputStream jpgInput = new FileImageInputStream(jpgFile);
        try {
            DicomObject attrs = new BasicDicomObject();
            attrs.putString(Tag.SpecificCharacterSet, VR.CS, charset);
            for (Enumeration en = cfg.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                attrs.putString(Tag.toTagPath(key), null, cfg.getProperty(key));           
            }
            if (missingImagePixelAttr(attrs)) {
                detectImagePixelAttrs(attrs, jpgInput);
            }
            ensureUID(attrs, Tag.StudyInstanceUID);
            ensureUID(attrs, Tag.SeriesInstanceUID);
            ensureUID(attrs, Tag.SOPInstanceUID);
            Date now = new Date();
            attrs.putDate(Tag.InstanceCreationDate, VR.DA, now);
            attrs.putDate(Tag.InstanceCreationTime, VR.TM, now);
            attrs.initFileMetaInformation(transferSyntax);
            FileOutputStream fos = new FileOutputStream(dcmFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DicomOutputStream dos = new DicomOutputStream(bos);
            try {
                dos.writeDicomFile(attrs);
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
                int jpgLen = (int) jpgFile.length();
                dos.writeHeader(Tag.Item, null, (jpgLen+1)&~1);
                byte[] b = new byte[bufferSize];
                int r;
                while ((r = jpgInput.read(b)) > 0) {
                    dos.write(b, 0, r);
                }
                if ((jpgLen&1) != 0) {
                    dos.write(0);
                }
                dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
            } finally {
                dos.close();
            }
        } finally {
            jpgInput.close();            
        }
    }    

    private boolean missingImagePixelAttr(DicomObject attrs) {
        return !(attrs.containsValue(Tag.Rows) 
                && attrs.containsValue(Tag.Columns)
                && attrs.containsValue(Tag.SamplesPerPixel)
                && attrs.containsValue(Tag.PhotometricInterpretation)
                && attrs.containsValue(Tag.BitsAllocated)
                && attrs.containsValue(Tag.BitsStored)
                && attrs.containsValue(Tag.HighBit)
                && attrs.containsValue(Tag.PixelRepresentation)
                );
    }

    private void detectImagePixelAttrs(DicomObject attrs, ImageInputStream iis)
    throws IOException {
        Iterator iter = ImageIO.getImageReaders(iis);
        if (!iter.hasNext()) {
            throw new IOException("Failed to detect image format");
        }                
        ImageReader reader = (ImageReader) iter.next();
        reader.setInput(iis);
        ensureUS(attrs, Tag.Rows, reader.getHeight(0));
        ensureUS(attrs, Tag.Columns, reader.getWidth(0));
        if (!(attrs.containsValue(Tag.SamplesPerPixel)
                && attrs.containsValue(Tag.PhotometricInterpretation))) {
            ImageTypeSpecifier type =
                (ImageTypeSpecifier) reader.getImageTypes(0).next();
            if (type.getNumBands() == 3) {
                attrs.putInt(Tag.SamplesPerPixel, VR.US, 3);
                attrs.putString(Tag.PhotometricInterpretation, VR.CS, 
                        "YBR_FULL_422");
                attrs.putInt(Tag.PlanarConfiguration, VR.US, 0);
            } else {
                attrs.putInt(Tag.SamplesPerPixel, VR.US, 1);
                attrs.putString(Tag.PhotometricInterpretation, VR.CS, 
                        "MONOCHROME2");                
            }
        }
        ensureUS(attrs, Tag.BitsAllocated, 8);
        ensureUS(attrs, Tag.BitsStored, attrs.getInt(Tag.BitsAllocated));
        ensureUS(attrs, Tag.HighBit, attrs.getInt(Tag.BitsStored) - 1);
        ensureUS(attrs, Tag.PixelRepresentation, 0);
        reader.dispose();
        iis.seek(0);
    }

    private void ensureUID(DicomObject attrs, int tag) {
        if (!attrs.containsValue(tag)) {
            attrs.putString(tag, VR.UI, UIDUtils.createUID());
        }        
    }

    private void ensureUS(DicomObject attrs, int tag, int val) {
        if (!attrs.containsValue(tag)) {
            attrs.putInt(tag, VR.US, val);
        }        
    }    
    public static void main(String[] args) {
        try {
            CommandLine cl = parse(args);
            Jpg2Dcm jpg2Dcm = new Jpg2Dcm();
            if (cl.hasOption("ts")) {
                jpg2Dcm.setTransferSyntax(cl.getOptionValue("ts"));
            }
            if (cl.hasOption("cs")) {
                jpg2Dcm.setCharset(cl.getOptionValue("cs"));
            }
            if (cl.hasOption("bs")) {
                jpg2Dcm.setBufferSize(Integer.parseInt(cl.getOptionValue("bs")));
            }
            if (cl.hasOption("c")) {
                jpg2Dcm.loadConfiguration(new File(cl.getOptionValue("c")));
            }
            if (cl.hasOption("uid")) {
                UIDUtils.setUseHostAddress(false);
                UIDUtils.setRoot(cl.getOptionValue("uid"));
            }
            List argList = cl.getArgList();
            File jpgFile = new File((String) argList.get(0));
            File dcmFile = new File((String) argList.get(1));
            long start = System.currentTimeMillis();
            jpg2Dcm.convert(jpgFile, dcmFile);
            long fin = System.currentTimeMillis();
            System.out.println("Encapsulated " + jpgFile + " to " 
                    + dcmFile + " in " + (fin - start) +  "ms.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        
        OptionBuilder.withArgName("charset");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Specific Character Set, ISO_IR 100 by default");
        opts.addOption(OptionBuilder.create("cs"));

        OptionBuilder.withArgName("size");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Buffer size used for copying JPEG to DICOM file, 8192 by default");
        opts.addOption(OptionBuilder.create("bs"));
        
        OptionBuilder.withArgName("file");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Configuration file specifying DICOM attribute values");
        opts.addOption(OptionBuilder.create("c"));
        
        OptionBuilder.withArgName("uid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Transfer Syntax; 1.2.840.10008.1.2.4.50 (JPEG Baseline) by default.");
        opts.addOption(OptionBuilder.create("ts"));

        OptionBuilder.withArgName("prefix");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Generate UIDs with given prefix," +
                "1.2.40.0.13.1.<host-ip> by default.");
        opts.addOption(OptionBuilder.create("uid"));
        
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("jpg2dcm: " + e.getMessage());
        }
        if (cl.hasOption('V')) {
            Package p = Jpg2Dcm.class.getPackage();
            System.out.println("jpg2dcm v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() != 2) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }

        return cl;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'jpg2dcm -h' for more information.");
        System.exit(1);
    }
    
}
