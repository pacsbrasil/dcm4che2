/* $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 03.08.2003
 */
public class StoreScp extends DcmServiceBase implements AssociationListener {

    private static final String STORESCP = "org.dcm4chex.service.StoreScp";
    private static final int[] TYPE1_ATTR =
        {
            Tags.StudyInstanceUID,
            Tags.SeriesInstanceUID,
            Tags.SOPInstanceUID,
            Tags.SOPClassUID,
            };

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final DcmParserFactory pf = DcmParserFactory.getInstance();

    private StorageHome storageHome;

    private final Logger log;
    private final DataSourceFactory dsf;
    private String ejbHostName;
    private int forwardPriority = Command.LOW;
    private String aet;
    private String[] retrieveAETs;
    private String[] forwardAETs;
    private String[] storageDirs;

    public StoreScp(Logger log, DataSourceFactory dsf) {
        this.log = log;
        this.dsf = dsf;
    }

    public void setAET(String aet) {
        this.aet = aet;
    }

    public String getEjbHostName() {
        return ejbHostName;
    }

    public void setEjbHostName(String ejbHostName) {
        this.ejbHostName = ejbHostName;
    }

    public final String[] getRetrieveAETs() {
        return retrieveAETs;
    }

    public final void setRetrieveAETs(String[] aets) {
        if (aets == null || aets.length == 0) {
            throw new IllegalArgumentException();
        }
        this.retrieveAETs = aets;
    }

    public final String[] getForwardAETs() {
        return forwardAETs;
    }

    public final void setForwardAETs(String[] aets) {
        this.forwardAETs = aets;
    }

    public final int getForwardPriority() {
        return forwardPriority;
    }

    public final void setForwardPriority(int forwardPriority) {
        this.forwardPriority = forwardPriority;
    }

    public final String[] getStorageDirs() {
        return storageDirs;
    }

    public final void setStorageDirs(String[] dirs) throws IOException {
        if (dirs == null || dirs.length == 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < dirs.length; i++) {
            dirs[i] = checkStorageDir(dirs[i]);
        }
        this.storageDirs = (String[]) dirs.clone();
    }

    private String checkStorageDir(String dir) throws IOException {
        File f = new File(dir);
        if (!f.exists()) {
            log.warn("directory " + dir + " does not exist - create new one");
            if (!f.mkdirs()) {
                String prompt = "Failed to create directory " + dir;
                log.error(prompt);
                throw new IOException(prompt);
            }
        }
        if (!f.isDirectory() || !f.canWrite()) {
            String prompt = dir + " is not a writeable directory";
            log.error(prompt);
            throw new IOException(prompt);
        }
        return f.getCanonicalPath();
    }

    void checkReadyToStart() {
        if (storageDirs == null || storageDirs.length == 0) {
            throw new IllegalStateException("No Storage Directory configured!");
        }
        if (retrieveAETs == null || retrieveAETs.length == 0) {
            throw new IllegalStateException("No Retrieve AET configured!");
        }
    }

    protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        InputStream in = rq.getDataAsStream();
        Storage storage = null;
        try {
            storage = storageHome().create();
            DcmDecodeParam decParam =
                DcmDecodeParam.valueOf(rq.getTransferSyntaxUID());
            Dataset ds = objFact.newDataset();
            DcmParser parser = pf.newDcmParser(in);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDataset(decParam, Tags.PixelData);
            ds.setFileMetaInfo(
                objFact.newFileMetaInfo(
                    rqCmd.getAffectedSOPClassUID(),
                    rqCmd.getAffectedSOPInstanceUID(),
                    rq.getTransferSyntaxUID()));
            checkDataset(ds);

            Calendar today = Calendar.getInstance();
            final int day = today.get(Calendar.DAY_OF_MONTH);
            String basedir = storageDirs[day % storageDirs.length];
            String[] fileIDs =
                {
                    String.valueOf(today.get(Calendar.YEAR)),
                    toDec(today.get(Calendar.MONTH) + 1),
                    toDec(day),
                    toHex(ds.getString(Tags.StudyInstanceUID).hashCode()),
                    toHex(ds.getString(Tags.SeriesInstanceUID).hashCode()),
                    toHex(ds.getString(Tags.SOPInstanceUID).hashCode())};
            File file = toFile(basedir, fileIDs);
            file.getParentFile().mkdirs();
            MessageDigest md = MessageDigest.getInstance("MD5");
            storeToFile(parser, ds, file, (DcmEncodeParam) decParam, md);
            storage.store(
                ds,
                retrieveAETs,
                basedir.replace(File.separatorChar, '/'),
                fileIDs[0]
                    + '/'
                    + fileIDs[1]
                    + '/'
                    + fileIDs[2]
                    + '/'
                    + fileIDs[3]
                    + '/'
                    + fileIDs[4]
                    + '/'
                    + fileIDs[5],
                (int) file.length(),
                md.digest());
            rspCmd.putUS(Tags.Status, Status.Success);
            updateStoredStudiesInfo(assoc.getAssociation(), ds);
        } catch (DcmServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        } finally {
            if (storage != null) {
                try {
                    storage.remove();
                } catch (Exception ignore) {}
            }
            in.close();
        }
    }

    private File toFile(String basedir, String[] fileIDs) {
        File dir =
            new File(
                basedir,
                fileIDs[0]
                    + File.separatorChar
                    + fileIDs[1]
                    + File.separatorChar
                    + fileIDs[2]
                    + File.separatorChar
                    + fileIDs[3]
                    + File.separatorChar
                    + fileIDs[4]);
        File file;
        while ((file = new File(dir, fileIDs[5])).exists()) {
            fileIDs[5] = toHex(Integer.parseInt(fileIDs[5], 16) + 1);
        }
        return file;
    }

    private StorageHome storageHome() throws NamingException {
        if (storageHome == null) {
            Hashtable env = new Hashtable();
            env.put(
                "java.naming.factory.initial",
                "org.jnp.interfaces.NamingContextFactory");
            env.put(
                "java.naming.factory.url.pkgs",
                "org.jboss.naming:org.jnp.interfaces");
            if (ejbHostName != null && ejbHostName.length() > 0) {
                env.put("java.naming.provider", ejbHostName);
            }
            Context jndiCtx = new InitialContext(env);
            try {
                Object o = jndiCtx.lookup(StorageHome.JNDI_NAME);
                storageHome =
                    (StorageHome) PortableRemoteObject.narrow(
                        o,
                        StorageHome.class);
            } finally {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
        return storageHome;
    }

    private void storeToFile(
        DcmParser parser,
        Dataset ds,
        File file,
        DcmEncodeParam encParam,
        MessageDigest md)
        throws IOException {
        log.info("M-WRITE file:" + file);
        BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(file));
        DigestOutputStream dos = new DigestOutputStream(out, md);
        try {
            ds.writeFile(dos, encParam);
            if (parser.getReadTag() == Tags.PixelData) {
                ds.writeHeader(
                    dos,
                    encParam,
                    parser.getReadTag(),
                    parser.getReadVR(),
                    parser.getReadLength());
                copy(parser.getInputStream(), dos);
            }
        } finally {
            try {
                dos.close();
            } catch (IOException ignore) {}
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[512];
        int c;
        while ((c = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, c);
        }
    }

    private void checkDataset(Dataset ds) throws DcmServiceException {
        for (int i = 0; i < TYPE1_ATTR.length; ++i) {
            if (ds.vm(TYPE1_ATTR[i]) <= 0) {
                throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "Missing Type 1 Attribute " + Tags.toString(TYPE1_ATTR[i]));
            }
            FileMetaInfo fmi = ds.getFileMetaInfo();
            if (!fmi
                .getMediaStorageSOPInstanceUID()
                .equals(ds.getString(Tags.SOPInstanceUID))) {
                throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Instance UID in Dataset differs from Affected SOP Instance UID");
            }
            if (!fmi
                .getMediaStorageSOPClassUID()
                .equals(ds.getString(Tags.SOPClassUID))) {
                throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Class UID in Dataset differs from Affected SOP Class UID");
            }
        }
    }

    private static char[] HEX_DIGIT =
        {
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F' };

    private String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4) {
            ch8[i] = HEX_DIGIT[val & 0xf];
        }
        return String.valueOf(ch8);
    }

    private String toDec(int val) {
        return String.valueOf(
            new char[] { HEX_DIGIT[val / 10], HEX_DIGIT[val % 10] });
    }

    private void mkdir(File dir) {
        if (dir.mkdir()) {
            log.info("M-WRITE dir:" + dir);
        }
    }

    // Implementation of AssociationListener
    public void write(Association src, PDU pdu) {}

    public void received(Association src, PDU pdu) {}

    public void write(Association src, Dimse dimse) {}

    public void received(Association src, Dimse dimse) {}

    public void error(Association src, IOException ioe) {}

    // preload ForwardTask class to avoid NullPointerException
    // by jboss's UnifiedClassLoader called from close() 
    private final Class clazz = ForwardTask.class;

    public void close(Association assoc) {
        Map storedStudiesInfo = (Map) assoc.getProperty(STORESCP);
        if (storedStudiesInfo == null) {
            return;
        }
        updateStudies(storedStudiesInfo);
        if (forwardAETs != null && forwardAETs.length != 0) {
            AEData retrieveAE = null;
            try {
                retrieveAE = getRetrieveAE();
            } catch (Exception e) {
                log.error("Failed to get Retrieve AE configuration from DB", e);
            }
            if (retrieveAE == null) {
                log.error(
                    "Cannot forward received objects without Retrieve AE configuration");
            } else {
                try {
                    new ForwardTask(
                        log,
                        aet,
                        retrieveAE,
                        storedStudiesInfo.values(),
                        forwardAETs,
                        forwardPriority)
                        .run();
                } catch (Exception e1) {
                    log.error("Failed to forward received objects:", e1);
                }
            }
        }
    }

    private void updateStudies(Map storedStudiesInfo) {
        Storage storage;
        try {
            storage = storageHome().create();
        } catch (Exception e) {
            log.error("Failed to update Studies", e);
            return;
        }
        for (Iterator it = storedStudiesInfo.keySet().iterator();
            it.hasNext();
            ) {
            final String suid = (String) it.next();
            try {
                storage.updateStudy(suid);
            } catch (Exception e) {
                log.error("Failed to update Study with UID:" + suid, e);
            }
        }
        try {
            storage.remove();
        } catch (Exception ignore) {}
    }

    private void updateStoredStudiesInfo(Association assoc, Dataset ds) {
        Map storedStudiesInfo = (Map) assoc.getProperty(STORESCP);
        if (storedStudiesInfo == null) {
            assoc.putProperty(STORESCP, storedStudiesInfo = new HashMap());
        }
        Dataset refSOP =
            getRefImageSeq(ds, getRefSeriesSeq(ds, storedStudiesInfo))
                .addNewItem();
        refSOP.putUI(Tags.RefSOPClassUID, ds.getString(Tags.SOPClassUID));
        refSOP.putUI(Tags.RefSOPInstanceUID, ds.getString(Tags.SOPInstanceUID));
    }

    private DcmElement getRefSeriesSeq(Dataset ds, Map storedStudiesInfo) {
        final String siud = ds.getString(Tags.StudyInstanceUID);
        Dataset info = (Dataset) storedStudiesInfo.get(siud);
        if (info != null) {
            return info.get(Tags.RefSeriesSeq);
        }
        storedStudiesInfo.put(siud, info = dof.newDataset());
        info.putLO(Tags.PatientID, ds.getString(Tags.PatientID));
        info.putPN(Tags.PatientName, ds.getString(Tags.PatientName));
        info.putSH(Tags.StudyID, ds.getString(Tags.StudyID));
        info.putUI(Tags.StudyInstanceUID, siud);
        return info.putSQ(Tags.RefSeriesSeq);
    }

    private DcmElement getRefImageSeq(Dataset ds, DcmElement seriesSq) {
        final String siud = ds.getString(Tags.SeriesInstanceUID);
        Dataset info;
        for (int i = 0, n = seriesSq.vm(); i < n; ++i) {
            info = seriesSq.getItem(i);
            if (siud.equals(info.getString(Tags.SeriesInstanceUID))) {
                return info.get(Tags.RefImageSeq);
            }
        }
        info = seriesSq.addNewItem();
        info.putUI(Tags.SeriesInstanceUID, siud);
        return info.putSQ(Tags.RefImageSeq);
    }

    private AEData getRetrieveAE() throws SQLException, NamingException {
        AEData aeData = null;
        for (int i = 0; aeData == null && i < retrieveAETs.length; i++) {
            aeData = queryAEData(retrieveAETs[i]);
            if (aeData == null) {
                log.warn("Unkown Retrieve AET " + retrieveAETs[i]);
            }
        }
        return aeData;
    }

    private AEData queryAEData(String aet)
        throws SQLException, NamingException {
        return new AECmd(dsf.getDataSource(), aet).execute();
    }

}
