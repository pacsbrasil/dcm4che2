/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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
 *                                                                           *
 *****************************************************************************/


import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmParseException;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilder;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirBuilderPref;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirRecord;
import org.dcm4che.media.DirWriter;
import org.dcm4che.util.UIDGenerator;

import java.io.*;
import java.nio.ByteOrder;
import java.net.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class DcmDir {

    private static final DirBuilderFactory fact = 
            DirBuilderFactory.getInstance();
    private final TagDictionary dict =
            DictionaryFactory.getInstance().getDefaultTagDictionary();

    private DirBuilderPref pref = null;
    private File dirFile = null;
    private File readMeFile = null;
    private String readMeCharset = null;
    private boolean skipGroupLen = true;
    private boolean undefSeqLen = true;
    private boolean undefItemLen = true;
    private String id = "";
    private String uid = null;
    private Integer maxlen = new Integer(79);
    private Integer vallen = new Integer(64);
    private boolean onlyInUse = false;
    private HashSet patientIDs = new HashSet();
    private HashSet studyUIDs = new HashSet();
    private HashSet seriesUIDs = new HashSet();
    private HashSet sopInstUIDs = new HashSet();
    private LinkedList fileIDs = new LinkedList();
    private boolean delFiles = false;
    
    /** Creates a new instance of DcmDir */
    public DcmDir() {
    }
    
    public void setDirFile(File dirFile) {
        this.dirFile = dirFile;
    }

    public void setReadMeFile(File readMeFile) {
        this.readMeFile = readMeFile;
    }
        
    public void setReadMeCharset(String readMeCharset) {
        this.readMeCharset = readMeCharset;
    }
        
    public void setFilesetID(String id) {
        this.id = id;
    }
    
    public void setFilesetUID(String uid) {
        this.uid = uid;
    }
            
    public void setMaxLen(Integer maxlen) {
        this.maxlen = maxlen;
    }

    public void setValLen(Integer vallen) {
        this.vallen = vallen;
    }

    public void setSkipGroupLen(boolean grlen) {
        this.skipGroupLen = skipGroupLen;
    }
    
    public void setUndefSeqLen(boolean sqlen) {
        this.undefSeqLen = undefSeqLen;
    }
    
    public void setUndefItemLen(boolean itemlen) {
        this.undefItemLen = undefItemLen;
    }
    
    public void setOnlyInUse(boolean onlyInUse) {
        this.onlyInUse = onlyInUse;
    }
    
    public void setDelFiles(boolean delFiles) {
        this.delFiles = delFiles;
    }
    
    public void setDirBuilderPref(DirBuilderPref pref) {
        this.pref = pref;
    }
    
    public void addPatientID(String id) {
        patientIDs.add(id);
    }
    
    public void addStudyUID(String uid) {
        studyUIDs.add(uid);
    }
    
    public void addSeriesUID(String uid) {
        seriesUIDs.add(uid);
    }
    
    public void addSOPInstUID(String uid) {
        sopInstUIDs.add(uid);
    }
    
    private TransformerHandler getTransformerHandler(SAXTransformerFactory tf,
            Templates tpl) throws TransformerConfigurationException, IOException {
        TransformerHandler th = tf.newTransformerHandler(tpl);
        th.setResult(new StreamResult(System.out));
        Transformer t = th.getTransformer();
        t.setParameter("maxlen", maxlen);
        t.setParameter("vallen", vallen);
        return th;
    }
        
    public void list() throws IOException, TransformerConfigurationException {
        SAXTransformerFactory tf =
            (SAXTransformerFactory)TransformerFactory.newInstance();
        URL url = DcmDir.class.getResource( "/resources/DcmDir.xsl");
        Templates xslt = tf.newTemplates(
                    new StreamSource(url.openStream(),url.toExternalForm()));
        DirReader reader = fact.newDirReader(dirFile);
        reader.getFileSetInfo().writeFile(getTransformerHandler(tf, xslt), dict);
        try {            
            list("", reader.getFirstRecord(onlyInUse), tf, xslt);
         } finally {
            reader.close();
        }
    }

    private static final DecimalFormat POS_FORMAT =
            new DecimalFormat("0000 DIRECTORY RECORD - ");
    public void list(String prefix, DirRecord first,
            SAXTransformerFactory tf, Templates xslt)
            throws IOException, TransformerConfigurationException {
        int count = 1;
        for(DirRecord rec = first; rec != null;
                rec = rec.getNextSibling(onlyInUse)) {
            Dataset ds = rec.getDataset();
            System.out.println(POS_FORMAT.format(ds.getItemOffset())
                + prefix + count + " [" + rec.getType() + "]");
            ds.writeDataset(getTransformerHandler(tf, xslt), dict);
            list(prefix + count + '.', rec.getFirstChild(onlyInUse), tf, xslt);
            ++count;
        }
    }
    
    private DcmEncodeParam encodeParam() {
        return new DcmEncodeParam(ByteOrder.LITTLE_ENDIAN,
                true, false, skipGroupLen, undefSeqLen, undefItemLen);
    }
    
    public void create(String[] args, int off) throws IOException  {
        if (uid == null) {
            uid = UIDGenerator.getInstance().createUID();
        }
        DirWriter writer = fact.newDirWriter(dirFile, uid,  id,
                readMeFile, readMeCharset, encodeParam());
        try {
            build(writer, args, off);
        } finally {
            writer.close();
        }
    }
    
    public void append(String[] args, int off) throws IOException  {
        DirWriter writer = fact.newDirWriter(dirFile, encodeParam());
        try {
            build(writer, args, off);
        } finally {
            writer.close();
        }
    }
    
    private void build(DirWriter w, String[] args, int off) throws IOException {
        if (pref == null) {
            pref = fact.loadDirBuilderPref(DcmDir.class.getResourceAsStream(
                    "/resources/DirBuilderPref.xml"));
        }
        long t1 = System.currentTimeMillis();
        int[] counter = new int[2];
        DirBuilder builder = fact.newDirBuilder(w, pref);
        for (int i = off; i < args.length; ++i) {
            append(builder, new File(args[i]), counter);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("\nInsert " + counter[1] + " records with "
                + counter[0] + " file references in "
                + (t2-t1)/1000f + " s.");
    }

    public void append(DirBuilder builder, File file, int[] counter)
            throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; ++i) {
                append(builder, files[i], counter);
            }
        } else {
            try {
                counter[1] += builder.addFileRef(file);
                ++counter[0];
                System.out.print('.');
            } catch (DcmParseException e) {
                System.out.println("\nFailed to add reference to "
                    + file);
                e.printStackTrace(System.out);
            } catch (IllegalArgumentException e) {
                System.out.println("\nFailed to add reference to "
                    + file);
                e.printStackTrace(System.out);
            }
        }
    }

    public void compact() throws IOException  {
        DirWriter writer = fact.newDirWriter(dirFile, encodeParam());
        long t1 = System.currentTimeMillis();
        long len1 = dirFile.length();
        try {
            writer = writer.compact();
        } finally {
            writer.close();
        }
        long t2 = System.currentTimeMillis();
        long len2 = dirFile.length();
        System.out.println("\nCompact " + dirFile + " from "
                + len1 + " to " + len2 + " Bytes in "
                + (t2-t1)/1000f + " s.");
    }
    
    private void addFileIDs(DirWriter w, File file)
            throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; ++i) {
                addFileIDs(w, files[i]);
            }
        } else {
            fileIDs.add(w.toFileIDs(file));
        }
    }
    
    public void remove(String[] args, int off) throws IOException  {
        long t1 = System.currentTimeMillis();
        int[] counter = new int[2];
        DirWriter w = fact.newDirWriter(dirFile, encodeParam());    
        try {
            for (int i = off; i < args.length; ++i) {
                addFileIDs(w, new File(args[i]));
            }
            doRemove(w, counter);
        } finally {
            w.close();
        }
        long t2 = System.currentTimeMillis();
        System.out.println("\nRemove " + counter[1] + " records and delete "
                + counter[0] + " files in " + (t2-t1)/1000f + " s.");
    }

    private void doRemove(DirWriter w, int[] counter) throws IOException  {
        for (DirRecord rec = w.getFirstRecord(true); rec != null;
                rec = rec.getNextSibling(true)) {
            if (patientIDs.contains(
                    rec.getDataset().getString(Tags.PatientID))) {
                if (delFiles) {
                    deleteRefFiles(w, rec, counter);
                }
                counter[1] += w.remove(rec);
            } else if (doRemoveStudy(w, rec, counter)) {
                counter[1] += w.remove(rec);
            }
        }
    }
    
    private boolean doRemoveStudy(DirWriter w, DirRecord parent, int[] counter)
            throws IOException  {
        boolean matchAll = true;
        LinkedList toRemove = new LinkedList();
        for (DirRecord rec = parent.getFirstChild(true); rec != null;
                rec = rec.getNextSibling(true)) {
            if (studyUIDs.contains(
                    rec.getDataset().getString(Tags.StudyInstanceUID))) {
                if (delFiles) {
                    deleteRefFiles(w, rec, counter);
                }
                toRemove.add(rec);
            } else if (doRemoveSeries(w, rec, counter)) {
                toRemove.add(rec);
            } else {
                matchAll = false;
            }
        }
        if (matchAll) {
            return true;
        }
        for (Iterator it = toRemove.iterator(); it.hasNext();) {
             counter[1] += w.remove((DirRecord)it.next());
        }
        return false;
    }
    
    private boolean doRemoveSeries(DirWriter w, DirRecord parent, int[] counter)
            throws IOException  {
        boolean matchAll = true;
        LinkedList toRemove = new LinkedList();
        for (DirRecord rec = parent.getFirstChild(true); rec != null;
                rec = rec.getNextSibling(true)) {
            if (seriesUIDs.contains(
                    rec.getDataset().getString(Tags.SeriesInstanceUID))) {
                if (delFiles) {
                    deleteRefFiles(w, rec, counter);
                }
                toRemove.add(rec);
            } else if (doRemoveInstances(w, rec, counter)) {
                toRemove.add(rec);
            } else {
                matchAll = false;
            }
        }
        if (matchAll) {
            return true;
        }
        for (Iterator it = toRemove.iterator(); it.hasNext();) {
             counter[1] += w.remove((DirRecord)it.next());
        }
        return false;
    }

    private boolean doRemoveInstances(DirWriter w, DirRecord parent,
            int[] counter) throws IOException  {
        boolean matchAll = true;
        LinkedList toRemove = new LinkedList();
        for (DirRecord rec = parent.getFirstChild(true); rec != null;
                rec = rec.getNextSibling(true)) {
            if (sopInstUIDs.contains(rec.getRefSOPInstanceUID())
                    || matchFileIDs(rec.getRefFileIDs())) {
                if (delFiles) {
                    deleteRefFiles(w, rec, counter);
                }
                toRemove.add(rec);
            } else {
                matchAll = false;
            }
        }
        if (matchAll) {
            return true;
        }
        for (Iterator it = toRemove.iterator(); it.hasNext();) {
             counter[1] += w.remove((DirRecord)it.next());
        }
        return false;
    }

    private boolean matchFileIDs(String[] ids) {
        if (ids == null || fileIDs.isEmpty()) {
            return false;
        }
        for (Iterator iter = fileIDs.iterator(); iter.hasNext();) {
            if (Arrays.equals((String[])iter.next(), ids)) {
                return true;
            }
        }
        return false;
    }
    
    private void deleteRefFiles(DirWriter w, DirRecord rec, int[] counter)
            throws IOException  {
        String[] fileIDs = rec.getRefFileIDs();
        if (fileIDs != null) {
            File f = w.getRefFile(fileIDs);
            if (!f.delete()) {
                System.out.println("Failed to delete " + f);
            } else {
                ++counter[0];
            }
        }
        for (DirRecord child = rec.getFirstChild(true); child != null;
                child = child.getNextSibling(true)) {
            deleteRefFiles(w, child, counter);
        }
    }
    
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        LongOpt[] longopts = new LongOpt[15];
        longopts[0] = new LongOpt("grouplen", LongOpt.NO_ARGUMENT, null, 'G');
        longopts[1] = new LongOpt("seqlen", LongOpt.NO_ARGUMENT, null, 'S');
        longopts[2] = new LongOpt("itemlen", LongOpt.NO_ARGUMENT, null, 'I');
        longopts[3] = new LongOpt("uid", LongOpt.REQUIRED_ARGUMENT, null, 'U');
        longopts[4] = new LongOpt("id", LongOpt.REQUIRED_ARGUMENT, null, 'u');
        longopts[5] = new LongOpt("readme", LongOpt.REQUIRED_ARGUMENT, null, 'R');
        longopts[6] = new LongOpt("readme-charset", LongOpt.REQUIRED_ARGUMENT, null, 'C');
        longopts[7] = new LongOpt("pref", LongOpt.REQUIRED_ARGUMENT, null, 'p');
        longopts[8] = new LongOpt("maxlen", LongOpt.REQUIRED_ARGUMENT, null, 'L');
        longopts[9] = new LongOpt("vallen", LongOpt.REQUIRED_ARGUMENT, null, 'l');
        longopts[10] = new LongOpt("pat", LongOpt.REQUIRED_ARGUMENT, null, 'P');
        longopts[11] = new LongOpt("study", LongOpt.REQUIRED_ARGUMENT, null, 'Y');
        longopts[12] = new LongOpt("series", LongOpt.REQUIRED_ARGUMENT, null, 'E');
        longopts[13] = new LongOpt("sop", LongOpt.REQUIRED_ARGUMENT, null, 'O');
        longopts[14] = new LongOpt("onlyInUse", LongOpt.NO_ARGUMENT, null, 'o');
        Getopt g = new Getopt("dcmdir.jar", args, "c:t:a:x:X:z:", longopts, true);
        
        DcmDir dcmdir = new DcmDir();
        int cmd = 0;
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {                
                case 'c':
                case 't':
                case 'a':
                case 'x':
                case 'X':
                case 'z':
                    cmd = c;
                    dcmdir.setDirFile(new File(g.getOptarg()));
                    break;
                case 'G':
                   dcmdir.setSkipGroupLen(false);
                    break;
                case 'S':
                    dcmdir.setUndefSeqLen(false);
                    break;
                case 'I':
                    dcmdir.setUndefItemLen(false);
                    break;
                case 'u':
                    dcmdir.setFilesetID(g.getOptarg());
                    break;
                case 'U':
                    dcmdir.setFilesetUID(g.getOptarg());
                    break;
                case 'R':
                    dcmdir.setReadMeFile(new File(g.getOptarg()));
                    break;
                case 'C':
                    dcmdir.setReadMeCharset(g.getOptarg());
                    break;
                case 'L':
                    dcmdir.setMaxLen(new Integer(g.getOptarg()));
                    break;
                case 'l':
                    dcmdir.setValLen(new Integer(g.getOptarg()));
                    break;
                case 'p':
                    dcmdir.setDirBuilderPref(
                            fact.loadDirBuilderPref(new File(g.getOptarg())));
                    break;
                case 'P':
                    dcmdir.addPatientID(g.getOptarg());
                    break;
                case 'Y':
                    dcmdir.addStudyUID(g.getOptarg());
                    break;
                case 'E':
                    dcmdir.addSeriesUID(g.getOptarg());
                    break;
                case 'O':
                    dcmdir.addSOPInstUID(g.getOptarg());
                    break;
                case 'o':
                    dcmdir.setOnlyInUse(true);
                    break;
                case '?':
                    exit("");
                    break;
                }
        }
        switch (cmd) {
            case 0:
                exit("Missing command -{tcaxXz}");
                break;
            case 'c':
                dcmdir.create(args, g.getOptind());
                break;
            case 't':
                dcmdir.list();
                break;
            case 'a':
                dcmdir.append(args, g.getOptind());
                break;
            case 'x':
            case 'X':
                dcmdir.setDelFiles(cmd == 'X');
                dcmdir.remove(args, g.getOptind());
                break;
            case 'z':
                dcmdir.compact();
                break;
            default:
                throw new RuntimeException();
        }
    }
    
    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE =
"Usage: java -jar dcmdir.jar -{tcaxXz} dir-file [-GSI] [-u id] [-U uid]\n" +
"         [--readme readme-file] [--readme-charset code] [--pref pref-file]\n" +
"         [--pat id] [--study uid] [--series uid] [--sop uid] [--onlyInUse]\n" +
"         [--maxlen line-len] [--vallen val-len] [files...]\n\n" +
"Options:\n" +
"  -t dir-file  list content of DICOMDIR file\n" +
"  -c dir-file  create new DICOMDIR file with references to files...\n" +
"  -a dir-file  add file references to existing DICOMDIR file\n" +
"  -x dir-file  remove record(s) from existing DICOMDIR, with specified\n" +
"               --pat    id   Patient ID\n" +
"               --study  uid  Study Instance UID\n" +
"               --series uid  Series Instance UID\n" +
"               --sop    uid  SOP Instance UID\n" +
"               files...      referenced files\n" +
"  -X dir-file  same as -x dir-file, but also deletes referenced files\n" +
"  -z dir-file  compact existing DICOMDIR file by removing inactive records\n" +
" --onlyInUse   hide inactive records in content list\n" +
" --id id       defines File-set ID of created DICOMDIR file\n" +
" --uid uid     defines SOP Instance UID of created DICOMDIR file\n" +
" --grouplen    encode with (gggg,0000) group length attributes\n" +
" --seqlen      encode sequence attributes with explicit length\n" +
" --itemlen     encode sequence items with explicit length\n" +
" --readme readme-file   add README file reference to created DICOMDIR file\n" +
" --readme-charset code  specifies character set used in README file\n" +
" --pref pref-file       specifies preferences for DICOMDIR generation\n" +
" --maxlen line-len      maximal line length in listing; default=79\n" +
" --vallen val-len       displayed value length in listing; default=64\n";
}
