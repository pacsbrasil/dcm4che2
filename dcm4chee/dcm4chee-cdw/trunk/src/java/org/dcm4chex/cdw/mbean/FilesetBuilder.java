/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirRecord;
import org.dcm4che.media.DirWriter;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.FileUtils;
import org.dcm4chex.cdw.common.Flag;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.dcm4chex.cdw.common.SpoolDirDelegate;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 29.06.2004
 *
 */
class FilesetBuilder {

    private static final int[] patKeys = { Tags.SpecificCharacterSet,
            Tags.PatientName, Tags.PatientID};

    private static final int[] styKeys = { Tags.SpecificCharacterSet,
            Tags.StudyDate, Tags.StudyTime, Tags.StudyDescription,
            Tags.AccessionNumber, Tags.StudyDate, Tags.StudyInstanceUID,
            Tags.StudyID,};

    private static final int[] serKeys = { Tags.SpecificCharacterSet,
            Tags.Modality, Tags.SeriesInstanceUID, Tags.SeriesNumber};

    private static final int[] imgKeys = { Tags.SpecificCharacterSet,
            Tags.ImageType, Tags.RefImageSeq, Tags.InstanceNumber};

    private static final int[] prKeys = { Tags.SpecificCharacterSet,
            Tags.RefSeriesSeq, Tags.InstanceNumber, Tags.PresentationLabel,
            Tags.PresentationLabel, Tags.PresentationDescription,
            Tags.PresentationCreationDate, Tags.PresentationCreationTime,
            Tags.PresentationCreatorName};

    private static final int[] srKeys = { Tags.SpecificCharacterSet,
            Tags.ContentDate, Tags.ContentTime, Tags.InstanceNumber,
            Tags.VerificationDateTime, Tags.ConceptNameCodeSeq,
            Tags.CompletionFlag, Tags.VerificationFlag,};

    private static final int[] koKeys = { Tags.SpecificCharacterSet,
            Tags.ContentDate, Tags.ContentTime, Tags.InstanceNumber,
            Tags.ConceptNameCodeSeq};

    private static final HashMap keysOfType = new HashMap();
    static {
        keysOfType.put("IMAGE", imgKeys);
        keysOfType.put("KEY OBJECT DOC", koKeys);
        keysOfType.put("PRESENTATION", prKeys);
        keysOfType.put("SR DOCUMENT", srKeys);
    }

    private static String toInstanceRecordType(String cuid) {
        if (cuid.equals(UIDs.BasicTextSR) || cuid.equals(UIDs.EnhancedSR)
                || cuid.equals(UIDs.ComprehensiveSR)
                || cuid.equals(UIDs.MammographyCADSR)) return "SR DOCUMENT";
        if (cuid.equals(UIDs.GrayscaleSoftcopyPresentationStateStorage))
                return "PRESENTATION";
        if (cuid.equals(UIDs.KeyObjectSelectionDocument))
                return "KEY OBJECT DOC";
        return "IMAGE";
    }

    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final UIDGenerator uidgen = UIDGenerator.getInstance();

    private static final DirBuilderFactory dbf = DirBuilderFactory
            .getInstance();

    private final MediaComposerService service;

    private final SpoolDirDelegate spoolDir;

    private final MediaCreationRequest rq;

    private final Dataset attrs;

    private final Logger log;

    private final boolean preserveInstances;

    private final boolean includeDisplayApplication;

    private static String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4)
            ch8[i] = HEX_DIGIT[val & 0xf];

        return String.valueOf(ch8);
    }

    private static String[] makeFileIDs(String pid, String suid, String seruid,
            String iuid) {
        return new String[] { "DICOM", toHex(pid.hashCode()),
                toHex(suid.hashCode()), toHex(seruid.hashCode()),
                toHex(iuid.hashCode()),};
    }

    public FilesetBuilder(MediaComposerService service,
            MediaCreationRequest rq, Dataset attrs) {
        this.service = service;
        this.spoolDir = service.getSpoolDir();
        this.log = service.getLog();
        this.rq = rq;
        this.attrs = attrs;
        this.preserveInstances = Flag.isYes(attrs
                .getString(Tags.PreserveCompositeInstancesAfterMediaCreation));
        this.includeDisplayApplication = Flag.isYes(attrs
                .getString(Tags.IncludeDisplayApplication));
    }

    public void build() throws MediaCreationException {
        try {
            File rootDir = mkRootDir(spoolDir);
            rq.setFilesetDir(rootDir);
            File readmeFile = new File(rootDir, service
                    .getFileSetDescriptorFile());
            File ddFile = new File(rootDir, "DICOMDIR");
            HashMap patRecs = new HashMap();
            HashMap styRecs = new HashMap();
            HashMap serRecs = new HashMap();
            DirWriter dirWriter = dbf.newDirWriter(ddFile, rootDir.getName(),
                    rq.getFilesetID(), readmeFile, service
                            .getCharsetOfFileSetDescriptorFile(), null);
            try {
                DcmElement refSOPs = attrs.get(Tags.RefSOPSeq);
                for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
                    addFile(rootDir, refSOPs.getItem(i), dirWriter, patRecs,
                            styRecs, serRecs);
                }

            } finally {
                dirWriter.close();
            }
            makeSymLink(service.getFileSetDescriptorSrcFile(), readmeFile);
            if (includeDisplayApplication)
                    makeSymLink(service.getDisplayApplicationSrcDir(),
                            new File(rootDir, service
                                    .getDisplayApplicationDir()));
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    private void addFile(File rootDir, Dataset item, DirWriter dirWriter,
            HashMap patRecs, HashMap styRecs, HashMap serRecs)
            throws IOException {
        String iuid = item.getString(Tags.RefSOPInstanceUID);
        File src = spoolDir.getInstanceFile(iuid);
        Dataset ds = FileUtils.readDataset(src, log);
        FileMetaInfo fmi = ds.getFileMetaInfo();
        String tsuid = fmi.getTransferSyntaxUID();
        String cuid = fmi.getMediaStorageSOPClassUID();
        String pid = ds.getString(Tags.PatientID);
        String suid = ds.getString(Tags.StudyInstanceUID);
        String seruid = ds.getString(Tags.SeriesInstanceUID);
        String[] fileIDs = makeFileIDs(pid, suid, seruid, iuid);
        DirRecord serRec = (DirRecord) serRecs.get(seruid);
        if (serRec == null) {
            DirRecord styRec = (DirRecord) styRecs.get(suid);
            if (styRec == null) {
                DirRecord patRec = (DirRecord) patRecs.get(pid);
                if (patRec == null) {
                    patRec = dirWriter.add(null, "PATIENT", ds.subSet(patKeys));
                    patRecs.put(pid, patRec);
                }
                styRec = dirWriter.add(patRec, "STUDY", ds.subSet(styKeys));
                styRecs.put(suid, styRec);
            }
            serRec = dirWriter.add(styRec, "SERIES", ds.subSet(serKeys));
            serRecs.put(seruid, serRec);
        }
        String recType = FilesetBuilder.toInstanceRecordType(cuid);
        dirWriter.add(serRec, recType, ds.subSet((int[]) keysOfType
                .get(recType)), fileIDs, cuid, iuid, tsuid);

        File dest = new File(rootDir, StringUtils.toString(fileIDs,
                File.separatorChar));
        File parent = dest.getParentFile();
        if (!parent.exists() && !parent.mkdirs())
                throw new IOException("Failed to mkdirs " + parent);
        if (preserveInstances)
            makeSymLink(src, dest);
        else
            move(src, dest);
    }

    private File mkRootDir(SpoolDirDelegate spoolDir) throws IOException {
        String uid = attrs.getString(Tags.StorageMediaFileSetUID);
        if (uid == null) uid = uidgen.createUID();

        File d;
        while ((d = spoolDir.getMediaFilesetRootDir(uid)).exists()) {
            log.warn("Duplicate file set UID:" + uid + " - generate new uid");
            uid = uidgen.createUID();
        }
        if (!d.mkdir()) throw new IOException("Failed to mkdir " + d);
        return d;
    }

    private void move(File src, File dst) throws IOException {
        if (log.isDebugEnabled()) log.debug("M-MOVE " + src + " => " + dst);
        if (!src.renameTo(dst))
                throw new IOException("M-MOVE " + src + " => " + dst
                        + " failed!");
    }

    private void makeSymLink(File src, File dst) throws IOException {
        String[] cmd = new String[] { "ln", "-s", src.getAbsolutePath(),
                dst.getAbsolutePath()};
        if (log.isDebugEnabled()) log.debug("M-LINK " + src + " => " + dst);
        int exitCode;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            exitCode = p.waitFor();
        } catch (Exception e) {
            throw new IOException("M-LINK " + src + " => " + dst + " failed!"
                    + e);
        }
        if (exitCode != 0) { throw new IOException("M-LINK " + src + " => "
                + dst + " failed!" + " failed! Exit Code: " + exitCode); }
    }

}
