/*
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
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;

import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.interfaces.Storage;

import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 03.08.2003
 */
public class StoreScp extends DcmServiceBase implements AssociationListener {
    private static final int[] TYPE1_ATTR =
        {
            Tags.StudyInstanceUID,
            Tags.SeriesInstanceUID,
            Tags.SOPInstanceUID,
            Tags.SOPClassUID,
            };

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final DcmParserFactory pf = DcmParserFactory.getInstance();

    private static final String YYYYMMDD = "yyyyMMdd";

    private StorageHome storageHome;

    private final StoreScpService scp;
    private final Logger log;
    private String host;
    private String mnt;
    private File archiveDir;
    private String providerURL;

    public StoreScp(StoreScpService scp) {
        this.scp = scp;
        this.log = scp.getLog();
    }

    public String getProviderURL() {
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    public String getMountPoint() {
        return mnt;
    }

    public void setMountPoint(String mnt) {        
        try {
            File tmp = new File(new URI("file:" + mnt));
            if (!tmp.isDirectory() || tmp.canWrite()) {
                throw new IllegalArgumentException("mnt:" + mnt + " not a writeable directory");                
            }
            this.archiveDir = tmp;
            this.mnt = mnt;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("mnt:" + mnt);
        }
    }

    public File getArchiveDir() {
        return archiveDir;
    }

    protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        InputStream in = rq.getDataAsStream();
        try {
            String instUID = rqCmd.getAffectedSOPInstanceUID();
            String classUID = rqCmd.getAffectedSOPClassUID();
            DcmDecodeParam decParam =
                DcmDecodeParam.valueOf(rq.getTransferSyntaxUID());
            Dataset ds = objFact.newDataset();
            DcmParser parser = pf.newDcmParser(in);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDataset(decParam, Tags.PixelData);
            checkDataset(ds, classUID, instUID);
            ds.setFileMetaInfo(
                objFact.newFileMetaInfo(
                    classUID,
                    instUID,
                    rq.getTransferSyntaxUID()));
            File file = makeFile(ds);
            MessageDigest md = MessageDigest.getInstance("MD5");
            storeToFile(parser, ds, file, (DcmEncodeParam) decParam, md);
            storeToDB(ds, file, md.digest());
            rspCmd.putUS(Tags.Status, Status.Success);
        } catch (DcmServiceException e) {
            throw e;
        } catch (Exception e) {
            scp.getLog().error(e.getMessage(), e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        } finally {
            in.close();
        }
    }

    private void storeToDB(Dataset ds, File file, byte[] md5)
        throws
            NamingException,
            CreateException,
            DcmServiceException,
            RemoteException,
            RemoveException {
        File seriesDir = file.getParentFile();
        File studyDir = seriesDir.getParentFile();
        File dateDir = studyDir.getParentFile();
        String path =
            dateDir.getName()
                + "/"
                + studyDir.getName()
                + "/"
                + seriesDir.getName()
                + "/"
                + file.getName();
        Storage storage = storageHome().create();
        storage.store(ds, host, mnt, path, file.length(), md5);
        storage.remove();
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
            if (providerURL != null && providerURL.length() > 0) {
                env.put("java.naming.provider", providerURL);
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

    private void checkDataset(Dataset ds, String classUID, String instUID)
        throws DcmServiceException {
        for (int i = 0; i < TYPE1_ATTR.length; ++i) {
            if (ds.vm(TYPE1_ATTR[i]) <= 0) {
                throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "Missing Type 1 Attribute " + Tags.toString(TYPE1_ATTR[i]));
            }
            if (!instUID.equals(ds.getString(Tags.SOPInstanceUID))) {
                throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Instance UID in Dataset differs from Affected SOP Instance UID");
            }
            if (!classUID.equals(ds.getString(Tags.SOPClassUID))) {
                throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Class UID in Dataset differs from Affected SOP Class UID");
            }
        }
    }

    private File makeFile(Dataset ds) throws DcmServiceException {
        File file, dir = archiveDir;
        String id1 = new SimpleDateFormat(YYYYMMDD).format(new Date());
        String id2 = toHex(ds.getString(Tags.StudyInstanceUID).hashCode());
        String id3 = toHex(ds.getString(Tags.SeriesInstanceUID).hashCode());
        int i4 = ds.getString(Tags.SOPInstanceUID).hashCode();
        mkdir(dir = new File(dir, id1));
        mkdir(dir = new File(dir, id2));
        mkdir(dir = new File(dir, id3));
        while ((file = new File(dir, toHex(i4))).exists()) {
            ++i4;
        }
        return file;
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

    public void close(Association src) {}
}
