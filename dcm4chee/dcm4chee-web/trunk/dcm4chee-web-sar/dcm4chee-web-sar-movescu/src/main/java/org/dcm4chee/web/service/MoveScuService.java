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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
package org.dcm4chee.web.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.Executor;

import javax.naming.InitialContext;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.ExtRetrieveTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.util.StringUtils;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.web.dao.AEHomeLocal;
import org.jboss.system.ServiceMBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since Jul 29, 2009
 */
public class MoveScuService extends ServiceMBeanSupport {

    private Device device;
    private NetworkConnection localConn;
    private NetworkApplicationEntity localNAE;
    
    private String calledAET;
    private boolean relationQR = true;
    private int priority;

    private String keyStoreURL;
    private String trustStoreURL;
    private char[] keyStorePassword;
    private char[] trustStorePassword;
    private char[] keyPassword;
    private String keyStoreType;
    private String trustStoreType;
    private String[] tlsProtocol;
    private boolean needClientAuth;
    
    private AEHomeLocal aeHome;
    
    private Executor executor = new NewThreadExecutor("MoveSCU");
    
    private static final Logger LOG = LoggerFactory.getLogger(MoveScuService.class);
    
    private String NONE ="NONE";
    
    private static final String[] ASSOC_CUIDS = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired,
        UID.VerificationSOPClass };
    
    private static final String[] PATIENT_LEVEL_MOVE_CUID = {
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] STUDY_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] SERIES_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE };
    
    private String[] QR_LEVELS = {"IMAGE", "SERIES", "STUDY", "PATIENT"};
    private String[][] QR_MOVE_CUIDS = {SERIES_LEVEL_MOVE_CUID, SERIES_LEVEL_MOVE_CUID,
            STUDY_LEVEL_MOVE_CUID, PATIENT_LEVEL_MOVE_CUID};

    private static final String[] NATIVE_LE_TS = {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};
    
    private static final String[] PRIORITIES = {"MEDIUM", "HIGH", "LOW"};
        
    public MoveScuService() {
        device = new Device("MoveSCU");
        localConn = new NetworkConnection();
        localNAE = new NetworkApplicationEntity();
        localNAE.setNetworkConnection(localConn);
        localNAE.setAssociationInitiator(true);
        device.setNetworkApplicationEntity(localNAE);
        device.setNetworkConnection(localConn);
        configureTransferCapability(localNAE, ASSOC_CUIDS, NATIVE_LE_TS);
    }

    public int getMaxPDULengthReceive() {
        return localNAE.getMaxPDULengthReceive();
    }

    public void setMaxPDULengthReceive(int maxPDULength) {
        localNAE.setMaxPDULengthReceive(maxPDULength);
    }

    public int getMaxOpsInvoked() {
        return localNAE.getMaxOpsInvoked();
    }

    public void setMaxOpsInvoked(int maxOpsInvoked) {
        localNAE.setMaxOpsInvoked(maxOpsInvoked);
    }

    public int getRetrieveRspTimeout() {
        return localNAE.getRetrieveRspTimeout();
    }

    public void setRetrieveRspTimeout(int retrieveRspTimeout) {
        localNAE.setRetrieveRspTimeout(retrieveRspTimeout);
    }

    public boolean isPackPDV() {
        return localNAE.isPackPDV();
    }

    public void setPackPDV(boolean packPDV) {
        localNAE.setPackPDV(packPDV);
    }

    public int getAcceptTimeout() {
        return localConn.getAcceptTimeout();
    }

    public void setAcceptTimeout(int timeout) {
        localConn.setAcceptTimeout(timeout);
    }

    public int getConnectTimeout() {
        return localConn.getConnectTimeout();
    }

    public void setConnectTimeout(int timeout) {
        localConn.setConnectTimeout(timeout);
    }

    public int getReleaseTimeout() {
        return localConn.getReleaseTimeout();
    }

    public void setReleaseTimeout(int timeout) {
        localConn.setReleaseTimeout(timeout);
    }

    public int getRequestTimeout() {
        return localConn.getRequestTimeout();
    }

    public void setRequestTimeout(int timeout) {
        localConn.setRequestTimeout(timeout);
    }
    
    public int getSocketCloseDelay() {
        return localConn.getSocketCloseDelay();
    }

    public void setSocketCloseDelay(int timeout) {
        localConn.setSocketCloseDelay(timeout);
    }

    public boolean isTcpNoDelay() {
        return localConn.isTcpNoDelay();
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        localConn.setTcpNoDelay(tcpNoDelay);
    }

    public String getCallingAET() {
        return localNAE.getAETitle();
    }

    public void setCallingAET(String callingAET) {
        localNAE.setAETitle(callingAET);
        device.setDeviceName(callingAET);
    }

    public String getCalledAET() {
        return calledAET;
    }

    public void setCalledAET(String calledAET) {
        this.calledAET = calledAET;
    }

    public boolean isRelationQR() {
        return relationQR;
    }

    public void setRelationQR(boolean relationQR) {
        this.relationQR = relationQR;
    }

    public String getPriority() {
        return PRIORITIES[priority];
    }

    public void setPriority(String priorityName) {
        this.priority = PRIORITIES[1].equals(priorityName) ? 1 : PRIORITIES[2].equals(priorityName) ? 2 : 0;
    }

    public String getKeyStoreURL() {
        return keyStoreURL;
    }

    public void setKeyStoreURL(String keyStoreURL) {
        this.keyStoreURL = keyStoreURL;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = none2null(keyStorePassword);
    }

    public String getTrustStoreURL() {
        return trustStoreURL;
    }

    public void setTrustStoreURL(String trustStoreURL) {
        this.trustStoreURL = trustStoreURL;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = none2null(trustStorePassword);
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = none2null(keyPassword);
    }
    public String getKeyPassword() {
        return keyPassword == null ? NONE : "******";
    }

    public String getTlsProtocol() {
        return StringUtils.join(tlsProtocol, ',');
    }

    public void setTlsProtocol(String tlsProtocol) {
        this.tlsProtocol = StringUtils.split(tlsProtocol, ',');
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String type) {
        this.keyStoreType = type;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String type) {
        this.trustStoreType = type;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    public boolean move(String retrieveAET, String moveDest, String patId, String studyIUID, String seriesIUID) throws IOException, InterruptedException, GeneralSecurityException {
        MoveRspHandler rspHandler = new MoveRspHandler();
        this.move(retrieveAET, moveDest, patId, 
                "".equals(studyIUID) ? null : new String[]{studyIUID}, 
                "".equals(seriesIUID) ? null : new String[]{seriesIUID}, null, rspHandler, true); 
        return rspHandler.getStatus() == 0;
    }
    
    private char[] none2null(String s) {
        return NONE.equals(s) ? null : s.toCharArray();
    }
    /**
     * Perform a DICOM Echo to given Application Entity Title.
     * @throws IOException 
     * @throws InterruptedException 
     * @throws GeneralSecurityException 
     */
    public void move(String retrieveAET, String moveDest, String patId, String[] studyIUIDs, 
            String[] seriesIUIDs, String[] sopIUIDs, DimseRSPHandler rspHandler, boolean waitAndCloseAssoc ) throws IOException, InterruptedException, GeneralSecurityException {
        if ( retrieveAET == null || "".equals(retrieveAET) )
            retrieveAET = calledAET;
        int qrLevelIdx = sopIUIDs != null ? 0 : seriesIUIDs != null ? 1
                : studyIUIDs != null ? 2 : 3;
        String qrLevel = QR_LEVELS[qrLevelIdx];
        String[] moveCUIDs = QR_MOVE_CUIDS[qrLevelIdx];
        Association assoc = open(retrieveAET);
        TransferCapability tc = selectTransferCapability(assoc, moveCUIDs);
        if ( tc == null ) {
            throw new NoPresentationContextException(UIDDictionary.getDictionary().prompt(moveCUIDs[0]));
        }
        String cuid = tc.getSopClass();
        String tsuid = tc.getTransferSyntax()[0];
        DicomObject keys = new BasicDicomObject();
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, qrLevel);
        if (patId != null ) keys.putString(Tag.PatientID, VR.LO, patId);
        if (studyIUIDs != null ) keys.putStrings(Tag.StudyInstanceUID, VR.UI, studyIUIDs);
        if (seriesIUIDs != null ) keys.putStrings(Tag.SeriesInstanceUID, VR.UI, seriesIUIDs);
        if (sopIUIDs != null ) keys.putStrings(Tag.SOPInstanceUID, VR.UI, sopIUIDs);
        LOG.info("Send C-MOVE request using {}:\n{}",cuid, keys);
        if (rspHandler == null)
            rspHandler = new MoveRspHandler();
        assoc.cmove(cuid, priority, keys, tsuid, moveDest, rspHandler);
        if (waitAndCloseAssoc) {
            assoc.waitForDimseRSP();
            try {
                assoc.release(true);
            } catch (InterruptedException t) {
                LOG.error("Association release failed! aet:"+retrieveAET, t);
            }
        }
    }

    private Association open(String aet)
            throws IOException, GeneralSecurityException {
        AE ae = lookupAEHome().findByTitle(aet);
        NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
        NetworkConnection remoteConn = new NetworkConnection();
   
        remoteAE.setAETitle(ae.getTitle());
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
   
        remoteConn.setHostname(ae.getHostName());
        remoteConn.setPort(ae.getPort());
        List<String> ciphers = ae.getCipherSuites();
        LOG.info("C-MOVE open associatien to {} url:{} Ciphers:{}", new Object[]{ae.getTitle(), ae, ciphers});
        if (ciphers.size() > 0) {
            String[] ciphers1 = (String[]) ciphers.toArray(new String[ciphers.size()]);
            remoteConn.setTlsCipherSuite(ciphers1);
            localConn.setTlsCipherSuite(ciphers1);
            localConn.setTlsProtocol(tlsProtocol);
            localConn.setTlsNeedClientAuth(needClientAuth);
            KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword, keyStoreType);
            KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword, trustStoreType);
            device.initTLS(keyStore, keyPassword == null ? keyStorePassword : keyPassword, trustStore);
        }
        
        try {
            return localNAE.connect(remoteAE, executor);
        } catch (Throwable t) {
            log.error("localNAE.connect failed!",t);
            throw new IOException("Failed to establish Association aet:"+ae.getTitle());
        }
    }
    
    private KeyStore loadKeyStore(String keyStoreURL, char[] password, String type) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        InputStream in;
        try {
            in = new URL(keyStoreURL).openStream();
        } catch (MalformedURLException e) {
            in = new FileInputStream(keyStoreURL);
        }
        KeyStore key = KeyStore.getInstance(type);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    public void configureTransferCapability(NetworkApplicationEntity ae, String[] cuids, String[] ts) {
        TransferCapability[] tcs = new TransferCapability[cuids.length];
        ExtRetrieveTransferCapability tc;
        for (int i = 0 ; i < cuids.length ; i++) {
            tc = new ExtRetrieveTransferCapability(
                    cuids[i], ts, TransferCapability.SCU);
            tc.setExtInfoBoolean(
                    ExtRetrieveTransferCapability.RELATIONAL_RETRIEVAL, relationQR);
            tcs[i] = tc;
        }    
        ae.setTransferCapability(tcs);
    }
    
    public TransferCapability selectTransferCapability(Association assoc, String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }

    /**
     * Perform a DICOM Echo to given Application Entity Title.
     */
    public boolean echo(String title) {
        Association assoc = null;
        try {
            assoc = open(title);
        } catch (Throwable t) {
            log.error("Failed to establish Association aet:"+title, t);
            return false;
        }
        try {
            assoc.cecho().next();
        } catch (Throwable t) {
            log.error("Echo failed! aet:"+title, t);
            return false;
        }
        try {
            assoc.release(true);
        } catch (InterruptedException t) {
            log.error("Association release failed! aet:"+title, t);
        }
        return true;
    }

    public AEHomeLocal lookupAEHome() {
        if ( aeHome == null ) {
            try {
                InitialContext jndiCtx = new InitialContext();
                aeHome = (AEHomeLocal) jndiCtx.lookup(AEHomeLocal.JNDI_NAME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return aeHome;
    }
/*_*/   
    private class MoveRspHandler extends DimseRSPHandler {
        private int status;

        public int getStatus() {
            return status;
        }

        @Override
        public void onDimseRSP(Association as, DicomObject cmd,
                DicomObject data) {
            log.info("received C-MOVE-RSP:"+cmd);
            if (!CommandUtils.isPending(cmd)) {
                status = cmd.getInt(Tag.Status);
            }
        }
    }
    
}

