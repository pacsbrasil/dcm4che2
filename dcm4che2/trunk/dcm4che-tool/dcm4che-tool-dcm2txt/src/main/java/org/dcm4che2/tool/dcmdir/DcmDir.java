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

package org.dcm4che2.tool.dcmdir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.media.ApplicationProfile;
import org.dcm4che2.media.BasicApplicationProfile;
import org.dcm4che2.media.DicomDirReader;
import org.dcm4che2.media.DicomDirWriter;
import org.dcm4che2.media.FilesetInformation;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 20, 2006
 */
public class DcmDir {

    private static final String USAGE = "dcmdir -{cru} <dicomdir> [Options] [<file>..][<directory>..]";
    private static final String DESCRIPTION = "Create/Read/Update DICOM File-Set with DICOM directory file\nOptions:";
    private static final String EXAMPLE = null;

    private final File file;
    private DicomDirReader dicomdir;
    private FilesetInformation fsinfo;
    private ApplicationProfile ap = new BasicApplicationProfile();

    public DcmDir(String fname) {
        file = new File(fname);
    }

    public void create(boolean explSeqLen) throws IOException {
        dicomdir = new DicomDirWriter(file, fsinfo(), explSeqLen);        
    }

    private FilesetInformation fsinfo() {
        if (fsinfo == null) {
            fsinfo = new FilesetInformation();
            fsinfo.init();
        }
        return fsinfo;
    }

    public void update() throws IOException {
        dicomdir = new DicomDirWriter(file);        
    }

    public void setSpecificCharacterSetofFilesetDescriptorFile(String cs) {       
        fsinfo().setSpecificCharacterSetofFilesetDescriptorFile(cs);
    }

    public void setFilesetDescriptorFileID(String fname) throws IOException {
        fsinfo().setFilesetDescriptorFileID(FilesetInformation.toFileID(
                new File(fname), file.getParentFile()));
    }

    public void setMediaStorageSOPInstanceUID(String ui) {
        fsinfo().setMediaStorageSOPInstanceUID(ui);
    }

    public void setFilesetID(String cs) {
        fsinfo().setFilesetID(cs);
    }

    public void open() throws IOException {
        dicomdir = new DicomDirReader(file);        
    }

    public int addFile(File f) throws IOException {
        if (f.isDirectory()) {
            int n = 0;
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                n += addFile(fs[i]);
            }
            return n;
        }
        DicomInputStream in = new DicomInputStream(f);
        in.setHandler(new StopTagInputHandler(Tag.PixelData));
        DicomObject dcmobj =  in.readDicomObject();
        DicomDirWriter w = (DicomDirWriter) dicomdir;
        DicomObject patrec = ap.makePatientDirectoryRecord(dcmobj);
        DicomObject styrec = ap.makeStudyDirectoryRecord(dcmobj);
        DicomObject serrec = ap.makeSeriesDirectoryRecord(dcmobj);
        DicomObject instrec = ap.makeInstanceDirectoryRecord(dcmobj, w.toFileID(f));
        DicomObject rec = w.addPatientRecord(patrec);
        rec = w.addStudyRecord(rec, styrec);
        rec = w.addSeriesRecord(rec, serrec);
        w.addChildRecord(rec, instrec);
        return 1;
    }
    
    public void close() throws IOException {
        dicomdir.close();
    }

    public void dump() {
        // TODO Auto-generated method stub
        
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        OptionGroup cmdOpt = new OptionGroup();
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder
                .withDescription("create new directory file <dicomdir> for DICOM file-set "
                        + "specified by file.. or directory.. arguments");
        cmdOpt.addOption(OptionBuilder.create("c"));
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder
                .withDescription("read directory file <dicomdir> and dump content to stdout");
        cmdOpt.addOption(OptionBuilder.create("r"));
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder
                .withDescription("update exisitng directory file <dicomdir> with DICOM file-set "
                        + "specified by file.. or directory.. arguments");
        cmdOpt.addOption(OptionBuilder.create("u"));
        opts.addOptionGroup(cmdOpt);
        opts.addOption("sqlen", false, 
                "encode Directory Record Sequence with explicit length," +
                "encode with undefined length by deafult.");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmdir: " + e.getMessage());
        }
        if (cl.hasOption('V')) {
            Package p = DcmDir.class.getPackage();
            System.out.println("dcmdir v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmdir -h' for more information.");
        System.exit(1);
    }

    public static void main(String[] args) throws IOException {
        CommandLine cl = parse(args);
        if (cl.hasOption("r")) {
            DcmDir dcmdir = new DcmDir(cl.getOptionValue("r"));
            dcmdir.open();
            dcmdir.dump();
            dcmdir.close();
        } else {
            DcmDir dcmdir;
            if (cl.hasOption("c")) {
                dcmdir = new DcmDir(cl.getOptionValue("c"));
                if (cl.hasOption("fsid")) {
                    dcmdir.setFilesetID(cl.getOptionValue("fsid"));
                }
                if (cl.hasOption("fsuid")) {
                    dcmdir.setMediaStorageSOPInstanceUID(
                            cl.getOptionValue("fsuid"));
                }
                if (cl.hasOption("fsdesc")) {
                    dcmdir.setFilesetDescriptorFileID(
                            cl.getOptionValue("fsdesc"));
                }
                if (cl.hasOption("fsdesc-cs")) {
                    dcmdir.setSpecificCharacterSetofFilesetDescriptorFile(
                            cl.getOptionValue("fsdesc-cs"));
                }
                dcmdir.create(cl.hasOption("sqlen"));
            } else { // cl.hasOption("u")
                dcmdir = new DcmDir(cl.getOptionValue("u"));
                dcmdir.update();
            }
            List argList = cl.getArgList();
            int num = 0;
            for (int i = 0, n = argList.size(); i < n; ++i) {
                num += dcmdir.addFile(new File((String) argList.get(i)));
            }
            dcmdir.close();
            
        }
    }
}
