/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirRecord;
import org.dcm4che.media.DirWriter;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.Flag;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.dcm4chex.cdw.common.SpoolDirDelegate;
import org.jboss.logging.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 29.06.2004
 *
 */
class FilesetBuilder {

    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final UIDGenerator uidgen = UIDGenerator.getInstance();

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final DirBuilderFactory dbf = DirBuilderFactory
            .getInstance();

    private final MediaComposerService service;

    private final SpoolDirDelegate spoolDir;

    private final DirRecordFactory recFact;

    private final MediaCreationRequest rq;

    private final Dataset attrs;

    private final Logger log;

    private final ImageReader reader;

    private final boolean preserveInstances;

    private final String web;

    private BufferedImage imageBI;

    private BufferedImage jpegBI;

    private BufferedImage iconBI;

    private byte[] iconPixelData;

    private boolean viewer;

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
        this.recFact = service.getDirRecordFactory();
        this.log = service.getLog();
        this.reader = service.getImageReader();
        this.rq = rq;
        this.attrs = attrs;
        this.preserveInstances = Flag.isYes(attrs
                .getString(Tags.PreserveCompositeInstancesAfterMediaCreation));
        this.viewer = Flag.isYes(attrs
                .getString(Tags.IncludeDisplayApplication));
        this.web = attrs.getString(Tags.IncludeNonDICOMObjects, "NONE");
    }

    final boolean isWeb() {
        return !"NONE".equals(web);
    }

    public void build() throws MediaCreationException {
        try {
            File rootDir = mkRootDir(spoolDir);
            rq.setFilesetDir(rootDir);
            File ddFile = new File(rootDir, "DICOMDIR");
            HashMap patRecs = new HashMap();
            HashMap styRecs = new HashMap();
            HashMap serRecs = new HashMap();
            File readmeFile = new File(rootDir, service
                    .getFileSetDescriptorFile());
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
            mergeDir(service.getMergeDir(), rootDir);
            if (isWeb()) {
                (new File(rootDir, "IHE_PDI")).mkdir();
                mergeDir(service.getMergeDirWeb(), rootDir);
            }
            if (viewer) mergeDir(service.getMergeDirViewer(), rootDir);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    private void mergeDir(File src, File dest) throws IOException {
        if (log.isDebugEnabled()) log.debug("merge " + src + " to " + dest);
        File[] files = src.listFiles();
        for (int i = 0; i < files.length; ++i) {
            File link = new File(dest, files[i].getName());
            if (link.isDirectory())
                mergeDir(files[i], link);
            else
                makeSymLink(files[i], link);
        }
    }

    private void addFile(File rootDir, Dataset item, DirWriter dirWriter,
            HashMap patRecs, HashMap styRecs, HashMap serRecs)
            throws IOException, MediaCreationException {
        String iuid = item.getString(Tags.RefSOPInstanceUID);
        String cuid = item.getString(Tags.RefSOPClassUID);
        File src = spoolDir.getInstanceFile(iuid);
        log.debug("M-READ " + src);
        ImageInputStream in = new FileImageInputStream(src);
        String[] fileIDs;
        try {
            reader.setInput(in);
            DcmMetadata metadata = (DcmMetadata) reader.getStreamMetadata();
            Dataset ds = metadata.getDataset();
            FileMetaInfo fmi = ds.getFileMetaInfo();
            String tsuid = fmi.getTransferSyntaxUID();

            String pid = ds.getString(Tags.PatientID);
            if (pid == null)
                    throw new MediaCreationException(
                            ExecutionStatusInfo.DIR_PROC_ERR,
                            "Missing Patient ID in instance[uid=" + iuid + "]");
            String suid = ds.getString(Tags.StudyInstanceUID);
            String seruid = ds.getString(Tags.SeriesInstanceUID);
            fileIDs = makeFileIDs(pid, suid, seruid, iuid);
            DirRecord serRec = (DirRecord) serRecs.get(seruid);
            if (serRec == null) {
                DirRecord styRec = (DirRecord) styRecs.get(suid);
                if (styRec == null) {
                    DirRecord patRec = (DirRecord) patRecs.get(pid);
                    if (patRec == null) {
                        patRec = dirWriter.add(null, "PATIENT", recFact
                                .makeRecord(DirRecord.PATIENT, ds));
                        patRecs.put(pid, patRec);
                    }
                    styRec = dirWriter.add(patRec, "STUDY", recFact.makeRecord(
                            DirRecord.STUDY, ds));
                    styRecs.put(suid, styRec);
                }
                serRec = dirWriter.add(styRec, "SERIES", recFact.makeRecord(
                        DirRecord.SERIES, ds));
                serRecs.put(seruid, serRec);
            }
            String recType = DirBuilderFactory.getRecordType(cuid);
            Dataset rec = recFact.makeRecord(recType, ds);
            if (DirRecord.IMAGE.equals(recType)) {
                Dataset iconItem = item.getItem(Tags.IconImageSeq);
                if (iconItem == null && service.isCreateIcon())
                        if (!UIDs.ExplicitVRLittleEndian.equals(tsuid)
                                && !UIDs.ImplicitVRLittleEndian.equals(tsuid)) {
                            log
                                    .warn("Cannot generate icon from compressed image "
                                            + src);
                        } else {
                            iconItem = mkIconItem(ds);
                        }
                if (iconItem != null)
                        rec.putSQ(Tags.IconImageSeq).addItem(iconItem);
                if (!"NONE".equals(web)) {
                    if (!UIDs.ExplicitVRLittleEndian.equals(tsuid)
                            && !UIDs.ImplicitVRLittleEndian.equals(tsuid)) {
                        log
                                .warn("Cannot generate jpeg from compressed DICOM image "
                                        + src);
                    } else {
                        fileIDs[0] = "IHE_PDI";
                        mkJpegs(new File(rootDir, StringUtils.toString(fileIDs,
                                File.separatorChar)));
                        fileIDs[0] = "DICOM";
                    }
                }
            }
            dirWriter.add(serRec, recType, rec, fileIDs, cuid, iuid, tsuid);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }

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

    private void mkJpegs(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs())
                throw new IOException("Failed to mkdirs " + dir);
        int w0 = reader.getWidth(0);
        int h0 = reader.getHeight(0);
        float ratio = reader.getAspectRatio(0);
        int w = Math.min(w0, service.getJpegWidth());
        int h = Math.min(h0, service.getJpegHeight());
        w = (int) Math.min(w, h * ratio);
        h = (int) Math.min(h, w / ratio);
        if (log.isDebugEnabled())
                log.debug("create jpegs[w=" + w + ",h=" + h + "] from image[w="
                        + w0 + ",h=" + h0 + "]");
        ImageReadParam param = reader.getDefaultReadParam();
        imageBI = checkReusable(imageBI, w0, h0);
        AffineTransformOp scaleOp = null;
        if (w != w0 || h != h0) {
            jpegBI = checkReusable(jpegBI, w, h);
            AffineTransform scale = AffineTransform.getScaleInstance((double) w
                    / w0, (double) h / h0);
            scaleOp = new AffineTransformOp(scale,
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        }
        BufferedImage bi;
        for (int frame = 0, n = reader.getNumImages(false); frame < n; ++frame) {
            param.setDestination(imageBI);
            bi = imageBI = reader.read(frame, param);
            if (scaleOp != null) {
                // workaround for ImagingLib.filter(this, bi, dst) == null 
                // if bi.getSampleModel() instanceof BandedSampleModel 
                if (imageBI.getSampleModel() instanceof BandedSampleModel) {
                    log.debug("convert RGB plane to RGB pixel");
                    BufferedImage newbi = new BufferedImage(imageBI.getWidth(),
                            imageBI.getHeight(), BufferedImage.TYPE_INT_RGB);
                    ColorConvertOp ccop = new ColorConvertOp(null);
                    ccop.filter(imageBI, newbi);
                    imageBI.flush();
                    imageBI = newbi;
                    jpegBI = null;
                }
                bi = jpegBI = scaleOp.filter(imageBI, jpegBI);
                imageBI.flush();
            }
            File dest = new File(dir, "" + (frame + 1) + ".JPG");
            OutputStream out = new BufferedOutputStream(new FileOutputStream(
                    dest));
            try {
                JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
                enc.encode(bi);
            } finally {
                out.close();
            }
            bi.flush();
        }
    }

    private Dataset mkIconItem(Dataset ds) throws IOException {
        int frame = ds.getInt(Tags.RepresentativeFrameNumber, 1) - 1;
        frame = Math.max(0, frame);
        frame = Math.min(frame, ds.getInt(Tags.NumberOfFrames, 1) - 1);

        int w0 = reader.getWidth(0);
        int h0 = reader.getHeight(0);
        float ratio = reader.getAspectRatio(0);
        int w = Math.min(w0, service.getIconWidth());
        int h = Math.min(h0, service.getIconHeight());
        w = (int) Math.min(w, h * ratio);
        h = (int) Math.min(h, w / ratio);
        int xSubsampling = (w0 - 1) / w + 1;
        int ySubsampling = (h0 - 1) / h + 1;
        w = w0 / xSubsampling;
        h = h0 / ySubsampling;
        if (log.isDebugEnabled())
                log.debug("create icon[w=" + w + ",h=" + h + "] from image[w="
                        + w0 + ",h=" + h0 + "]");
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceSubsampling(xSubsampling, ySubsampling,
                (xSubsampling - 1) / 2, (ySubsampling - 1) / 2);
        param.setDestination(checkReusable(iconBI, w, h));
        iconBI = reader.read(frame, param);

        if (iconPixelData == null || iconPixelData.length != w * h)
                iconPixelData = new byte[w * h];
        for (int x = 0, i = 0; x < w; ++x)
            for (int y = 0; y < h; ++y, ++i)
                iconPixelData[i] = (byte) iconBI.getRGB(x, y);

        Dataset iconItem = dof.newDataset();
        iconItem.putCS(Tags.PhotometricInterpretation, "MONOCHROME2");
        iconItem.putUS(Tags.Rows, iconBI.getHeight());
        iconItem.putUS(Tags.Columns, iconBI.getWidth());
        iconItem.putUS(Tags.SamplesPerPixel, 1);
        iconItem.putUS(Tags.BitsAllocated, 8);
        iconItem.putUS(Tags.BitsStored, 8);
        iconItem.putUS(Tags.HighBit, 7);
        iconItem.putOB(Tags.PixelData, iconPixelData);
        return iconItem;
    }

    private BufferedImage checkReusable(BufferedImage bi, int w, int h)
            throws IOException {
        if (bi == null || bi.getWidth() != w || bi.getHeight() != h)
                return null;
        ImageTypeSpecifier t1 = reader.getRawImageType(0);
        ImageTypeSpecifier t2 = ImageTypeSpecifier.createFromRenderedImage(bi);
        if (!t1.equals(t2)) return null;
        if (log.isDebugEnabled())
                log.debug("Reuse BufferedImage[w=" + w + ",h=" + h + "]");
        return bi;
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
        if (log.isDebugEnabled()) log.debug("mv " + src + " " + dst);
        if (!src.renameTo(dst))
                throw new IOException("mv " + src + " " + dst + " failed!");
    }

    private void makeSymLink(File src, File dst) throws IOException {
        if (dst.delete())
                if (log.isDebugEnabled()) log.debug("M-DELETE " + dst);
        String[] cmd = new String[] { "ln", "-s", src.getAbsolutePath(),
                dst.getAbsolutePath()};
        try {
            if (new Executer(cmd).waitFor() == 0) return;
        } catch (InterruptedException e) {
        }
        throw new IOException(StringUtils.toString(cmd, ' ') + " failed!");
    }

}
