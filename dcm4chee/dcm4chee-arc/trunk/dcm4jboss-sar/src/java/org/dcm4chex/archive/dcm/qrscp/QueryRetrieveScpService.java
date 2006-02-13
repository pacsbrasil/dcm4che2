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

package org.dcm4chex.archive.dcm.qrscp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.JMException;
import javax.management.ObjectName;

import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.ExtNegotiator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 31.08.2003
 */
public class QueryRetrieveScpService extends AbstractScpService {

    private static final String NONE = "NONE";

    protected static final String[] DEFLATED_OR_NATIVE_LE_TS = {
			UIDs.DeflatedExplicitVRLittleEndian, UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian,};
	
    private static String transactionIsolationLevelAsString(int level) {
        switch (level) {
        	case 0:
        	    return "DEFAULT";
        	case Connection.TRANSACTION_READ_UNCOMMITTED:
        	    return "READ_UNCOMMITTED";
        	case Connection.TRANSACTION_READ_COMMITTED:
        	    return "READ_COMMITTED";
        	case Connection.TRANSACTION_REPEATABLE_READ:
        	    return "REPEATABLE_READ";
        	case Connection.TRANSACTION_SERIALIZABLE:
        	    return "SERIALIZABLE";
        }
        throw new IllegalArgumentException("level:" + level);
    }
    
    private static int transactionIsolationLevelOf(String s) {
        String uc = s.trim().toUpperCase();
        if ("READ_UNCOMMITTED".equals(uc))
            return Connection.TRANSACTION_READ_UNCOMMITTED;
        if ("READ_COMMITTED".equals(uc))
            return Connection.TRANSACTION_READ_COMMITTED;
        if ("REPEATABLE_READ".equals(uc))
            return Connection.TRANSACTION_REPEATABLE_READ;
        if ("SERIALIZABLE".equals(uc))
            return Connection.TRANSACTION_SERIALIZABLE;
        return 0;
    }
    
    private String[] sendNoPixelDataToAETs = null;
    
    private String[] sendDecompressedToAETs = null;

    private String[] ignoreUnsupportedSOPClassFailuresByAETs = null;
    
    private LinkedHashMap requestStgCmtFromAETs = new LinkedHashMap();
    
    private ObjectName fileSystemMgtName;

    private ObjectName stgCmtScuScpName;
    
    private ObjectName aeServiceName; 
    
    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
    
    private boolean sendPendingMoveRSP = true;

    private boolean forwardAsMoveOriginator = true;
	
	private boolean recordStudyAccessTime = true;

    private int acTimeout = 5000;

    private int dimseTimeout = 0;

    private int soCloseDelay = 500;
    
    private int maxStoreOpsInvoked = 0;

    private boolean patientRootFind;

    private boolean studyRootFind;

    private boolean patientStudyOnlyFind;

    private boolean tianiPatientRootFind;

    private boolean tianiStudyRootFind;

    private boolean tianiPatientStudyOnlyFind;

    private boolean tianiBlockedPatientRootFind;

    private boolean tianiBlockedStudyRootFind;

    private boolean tianiBlockedPatientStudyOnlyFind;
	
    private boolean acceptDeflatedBlockedFind;

    private boolean patientRootMove;

    private boolean studyRootMove;

    private boolean patientStudyOnlyMove;

    private FindScp dicomFindScp = new FindScp(this, true, false);

    private FindScp tianiFindScp = new FindScp(this, false, false);

    private FindScp tianiBlockedFindScp = new FindScp(this, false, true);
	
    private MoveScp moveScp = new MoveScp(this);
    
    private int maxUIDsPerMoveRQ = 100;

    private int maxBlockedFindRSP = 10000;

	private int bufferSize = 8192;
	
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }
    
    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }
    
    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final ObjectName getStgCmtScuScpName() {
        return stgCmtScuScpName;
    }
    
    public final void setStgCmtScuScpName(ObjectName stgCmtScuScpName) {
        this.stgCmtScuScpName = stgCmtScuScpName;
    }
    
	/**
	 * @return Returns the aeService.
	 */
	public ObjectName getAEServiceName() {
		return aeServiceName;
	}
	/**
	 * @param aeService The aeService to set.
	 */
	public void setAEServiceName(ObjectName aeServiceName) {
		this.aeServiceName = aeServiceName;
	}
    
    
    public final String getQueryTransactionIsolationLevel() {
        return transactionIsolationLevelAsString(QueryCmd.transactionIsolationLevel);
    }
    
    public final void setQueryTransactionIsolationLevel(String level) {
        QueryCmd.transactionIsolationLevel = transactionIsolationLevelOf(level);
    }
    
    public final String getRetrieveTransactionIsolationLevel() {
        return transactionIsolationLevelAsString(RetrieveCmd.transactionIsolationLevel);
    }
    
    public final void setRetrieveTransactionIsolationLevel(String level) {
        RetrieveCmd.transactionIsolationLevel = transactionIsolationLevelOf(level);
    }

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public final boolean isAcceptPatientRootFind() {
        return patientRootFind;
    }

    public final void setAcceptPatientRootFind(boolean enable) {
	    if ( this.patientRootFind == enable ) return;
        this.patientRootFind = enable;
        enableService();
    }

    public final boolean isAcceptTianiPatientRootFind() {
        return tianiPatientRootFind;
    }

    public final void setAcceptTianiPatientRootFind(boolean enable) {
	    if ( this.tianiPatientRootFind == enable ) return;
        this.tianiPatientRootFind = enable;
        enableService();
    }

	public final boolean isAcceptTianiBlockedPatientRootFind() {
		return tianiBlockedPatientRootFind;
	}

	public final void setAcceptTianiBlockedPatientRootFind(boolean enable) {
	    if ( this.tianiBlockedPatientRootFind == enable ) return;
		this.tianiBlockedPatientRootFind = enable;
        enableService();
	}

	public final boolean isAcceptPatientRootMove() {
        return patientRootMove;
    }

    public final void setAcceptPatientRootMove(boolean enable) {
	    if ( this.patientRootMove == enable ) return;
        this.patientRootMove = enable;
        enableService();
    }

    public final boolean isAcceptPatientStudyOnlyFind() {
        return patientStudyOnlyFind;
    }

    public final void setAcceptPatientStudyOnlyFind(boolean enable) {
	    if ( this.patientStudyOnlyFind == enable ) return;
        this.patientStudyOnlyFind = enable;
        enableService();
    }

    public final boolean isAcceptTianiPatientStudyOnlyFind() {
        return tianiPatientStudyOnlyFind;
    }

    public final void setAcceptTianiPatientStudyOnlyFind(boolean enable) {
	    if ( this.tianiPatientStudyOnlyFind == enable ) return;
        this.tianiPatientStudyOnlyFind = enable;
        enableService();
    }

	public final boolean isAcceptTianiBlockedPatientStudyOnlyFind() {
		return tianiBlockedPatientStudyOnlyFind;
	}

	public final void setAcceptTianiBlockedPatientStudyOnlyFind(boolean enable) {
	    if ( this.tianiBlockedPatientStudyOnlyFind == enable ) return;
		this.tianiBlockedPatientStudyOnlyFind = enable;
        enableService();
	}

    public final boolean isAcceptPatientStudyOnlyMove() {
        return patientStudyOnlyMove;
    }

    public final void setAcceptPatientStudyOnlyMove(boolean patientStudyOnlyMove) {
	    if ( this.patientStudyOnlyMove == patientStudyOnlyMove ) return;
        this.patientStudyOnlyMove = patientStudyOnlyMove;
        enableService();
    }

    public final boolean isAcceptStudyRootFind() {
        return studyRootFind;
    }

    public final void setAcceptStudyRootFind(boolean enable) {
        if ( this.studyRootFind == enable ) return;
        this.studyRootFind = enable;
        enableService();
    }

    public final boolean isAcceptTianiStudyRootFind() {
        return tianiStudyRootFind;
    }

    public final void setAcceptTianiStudyRootFind(boolean enable) {
       if ( this.tianiStudyRootFind == enable ) return;
       this.tianiStudyRootFind = enable;
        enableService();
    }

	public final boolean isAcceptTianiBlockedStudyRootFind() {
		return tianiBlockedStudyRootFind;
	}

	public final void setAcceptTianiBlockedStudyRootFind(boolean enable) {
	    if ( this.tianiBlockedStudyRootFind == enable ) return;
		this.tianiBlockedStudyRootFind = enable;
        enableService();
	}

    public final boolean isAcceptStudyRootMove() {
        return studyRootMove;
    }

    public final void setAcceptStudyRootMove(boolean studyRootMove) {
	    if ( this.studyRootMove == studyRootMove ) return;
        this.studyRootMove = studyRootMove;
        enableService();
    }

    public final boolean isAcceptDeflatedBlockedFind() {
		return acceptDeflatedBlockedFind;
	}

	public final void setAcceptDeflatedBlockedFind(boolean enable) {
	    if ( this.acceptDeflatedBlockedFind == enable ) return;
		this.acceptDeflatedBlockedFind = enable;
        enableService();
	}

	public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final int getMaxStoreOpsInvoked()
    {
        return maxStoreOpsInvoked;
    }

    public final void setMaxStoreOpsInvoked(int maxStoreOpsInvoked)
    {
        this.maxStoreOpsInvoked = maxStoreOpsInvoked;
    }

    public final boolean isSendPendingMoveRSP() {
        return sendPendingMoveRSP;
    }

    public final void setSendPendingMoveRSP(boolean sendPendingMoveRSP) {
        this.sendPendingMoveRSP = sendPendingMoveRSP;
    }

    public final boolean isForwardAsMoveOriginator() {
        return forwardAsMoveOriginator;
    }

    public final void setForwardAsMoveOriginator(boolean forwardAsMoveOriginator) {
        this.forwardAsMoveOriginator = forwardAsMoveOriginator;
    }
    
    public final boolean isRecordStudyAccessTime() {
		return recordStudyAccessTime;
	}

	public final void setRecordStudyAccessTime(boolean updateAccessTime) {
		this.recordStudyAccessTime = updateAccessTime;
	}

	public final String getSendNoPixelDataToAETs() {
        return sendNoPixelDataToAETs == null ? NONE
                : StringUtils.toString(sendNoPixelDataToAETs, '\\');
    }

    public final void setSendNoPixelDataToAETs(String aets) {
        this.sendNoPixelDataToAETs = NONE.equalsIgnoreCase(aets) ? null 
                : StringUtils.split(aets, '\\');
    }
    
    public final String getSendDecompressedToAETs() {
        return sendDecompressedToAETs == null ? NONE
                : StringUtils.toString(sendDecompressedToAETs, '\\');
    }

    public final void setSendDecompressedToAETs(String aets) {
        this.sendDecompressedToAETs = NONE.equalsIgnoreCase(aets) ? null 
                : StringUtils.split(aets, '\\');
    }
    
    public final String getIgnoreUnsupportedSOPClassFailuresByAETs() {
        return ignoreUnsupportedSOPClassFailuresByAETs == null ? NONE
            : StringUtils.toString(ignoreUnsupportedSOPClassFailuresByAETs, '\\');
    }
    
    public final void setIgnoreUnsupportedSOPClassFailuresByAETs(String aets) {
        this.ignoreUnsupportedSOPClassFailuresByAETs = NONE.equalsIgnoreCase(aets) ? null 
                : StringUtils.split(aets, '\\');
    }
    
    public final String getRequestStgCmtFromAETs() {
        if (requestStgCmtFromAETs.isEmpty()) return NONE;        
        StringBuffer sb = new StringBuffer();
        Iterator it = requestStgCmtFromAETs.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry entry = (Entry) it.next();
            final String key = (String) entry.getKey();
            final String value = (String) entry.getValue();
            sb.append(key);
            if (!key.equals(value))
                sb.append(':').append(value);
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public final void setRequestStgCmtFromAETs(String aets) {
        requestStgCmtFromAETs.clear();
        if (aets != null && aets.length() > 0 
                && !aets.equalsIgnoreCase(NONE)) {
            String[] a = StringUtils.split(aets, '\\');
            String s;
            int c;
            for (int i = 0; i < a.length; i++) {
                s = a[i];
                c = s.indexOf(':');
                if (c == -1)
                    requestStgCmtFromAETs.put(s, s);
                else if (c > 0 && c < s.length() -1)
                    requestStgCmtFromAETs.put(s.substring(0, c), s.substring(c+1));
            }
        }
    }
    
	public final int getMaxUIDsPerMoveRQ() {
		return maxUIDsPerMoveRQ;
	}
	
	public final void setMaxUIDsPerMoveRQ(int max) {
		this.maxUIDsPerMoveRQ = max;
	}

	public final void setMaxBlockedFindRSP(int max) {
		this.maxBlockedFindRSP = max;
	}

    public final int getMaxBlockedFindRSP() {
		return maxBlockedFindRSP;
	}

	protected String[] getBlockedFindTransferSyntaxUIDs() {
        return acceptDeflatedBlockedFind ? DEFLATED_OR_NATIVE_LE_TS
				: getTransferSyntaxUIDs();
    }
	
	protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelFIND,
                dicomFindScp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelFIND,
                dicomFindScp);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
                dicomFindScp);

        services.bind(UIDs.TianiPatientRootQueryRetrieveInformationModelFIND,
                tianiFindScp);
        services.bind(UIDs.TianiStudyRootQueryRetrieveInformationModelFIND,
                tianiFindScp);
        services.bind(UIDs.TianiPatientStudyOnlyQueryRetrieveInformationModelFIND,
                tianiFindScp);
        
        services.bind(UIDs.TianiBlockedPatientRootQueryRetrieveInformationModelFIND,
                tianiBlockedFindScp);
        services.bind(UIDs.TianiBlockedStudyRootQueryRetrieveInformationModelFIND,
                tianiBlockedFindScp);
        services.bind(UIDs.TianiBlockedPatientStudyOnlyQueryRetrieveInformationModelFIND,
                tianiBlockedFindScp);
        
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelMOVE,
                moveScp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelMOVE, moveScp);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE,
                moveScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.PatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.StudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);

        services.unbind(UIDs.TianiPatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.TianiStudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.TianiPatientStudyOnlyQueryRetrieveInformationModelFIND);

        services.unbind(UIDs.TianiBlockedPatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.TianiBlockedStudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.TianiBlockedPatientStudyOnlyQueryRetrieveInformationModelFIND);

        services.unbind(UIDs.PatientRootQueryRetrieveInformationModelMOVE);
        services.unbind(UIDs.StudyRootQueryRetrieveInformationModelMOVE);
        services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE);
    }

    private static final ExtNegotiator ECHO_EXT_NEG = new ExtNegotiator() {
        public byte[] negotiate(byte[] offered) {
            return offered;
        }
    };

	private void updatePC(AcceptorPolicy policy, String cuid, boolean enable) {
        policy.putPresContext(cuid, enable ? getTransferSyntaxUIDs() : null);
        policy.putExtNegPolicy(cuid, enable ? ECHO_EXT_NEG : null);
    }
    
    private void updateBlockedFindPC(AcceptorPolicy policy, String cuid, boolean enable) {
        policy.putPresContext(cuid, enable ? getBlockedFindTransferSyntaxUIDs() : null);
        policy.putExtNegPolicy(cuid, enable ? ECHO_EXT_NEG : null);
    }
    
    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        updatePC(policy, UIDs.PatientRootQueryRetrieveInformationModelFIND,
                enable && patientRootFind);
        updatePC(policy, UIDs.StudyRootQueryRetrieveInformationModelFIND,
                enable && studyRootFind);
        updatePC(policy, UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
                enable && patientStudyOnlyFind);
        updatePC(policy, UIDs.TianiPatientRootQueryRetrieveInformationModelFIND,
                enable && tianiPatientRootFind);
        updatePC(policy, UIDs.TianiStudyRootQueryRetrieveInformationModelFIND,
                enable && tianiStudyRootFind);
        updatePC(policy, UIDs.TianiPatientStudyOnlyQueryRetrieveInformationModelFIND,
                enable && tianiPatientStudyOnlyFind);
        updateBlockedFindPC(policy, UIDs.TianiBlockedPatientRootQueryRetrieveInformationModelFIND,
                enable && tianiBlockedPatientRootFind);
		updateBlockedFindPC(policy, UIDs.TianiBlockedStudyRootQueryRetrieveInformationModelFIND,
                enable && tianiBlockedStudyRootFind);
		updateBlockedFindPC(policy, UIDs.TianiBlockedPatientStudyOnlyQueryRetrieveInformationModelFIND,
                enable && tianiBlockedPatientStudyOnlyFind);
        updatePC(policy, UIDs.PatientRootQueryRetrieveInformationModelMOVE,
                enable && patientRootMove);
        updatePC(policy, UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                enable && studyRootMove);
        updatePC(policy, UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE,
                enable && patientStudyOnlyMove);
    }

    public AEData queryAEData(String aet, InetAddress address) throws DcmServiceException,
            UnkownAETException {
		//String host = address != null ? address.getCanonicalHostName() : null;
        try {
            Object o = server.invoke(aeServiceName, "getAE", 
            		new Object[] {aet, address}, 
					new String[] {String.class.getName(), InetAddress.class.getName()});
            if ( o == null ) 
            	throw new UnkownAETException("Unkown AET: " + aet);
            return (AEData) o;
        } catch (JMException e) {
            log.error("Failed to query AEData", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    boolean isLocalFileSystem(String dirpath) throws DcmServiceException {
        try {
            Boolean b = (Boolean) server.invoke(fileSystemMgtName,
                    "isLocalFileSystem",
                    new Object[] { dirpath},
                    new String[] { String.class.getName()});
            return b.booleanValue();
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    public final int getBufferSize() {
        return bufferSize ;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    boolean isWithoutPixelData(String moveDest) {
        return sendNoPixelDataToAETs != null
            && Arrays.asList(sendNoPixelDataToAETs).contains(moveDest);
    }
    
    boolean isDecompressed(String moveDest) {
        return sendDecompressedToAETs != null
            && Arrays.asList(sendDecompressedToAETs).contains(moveDest);
    }
    
    boolean isIgnoreUnsupportedSOPClassFailures(String moveDest) {
        return ignoreUnsupportedSOPClassFailuresByAETs != null
            && Arrays.asList(ignoreUnsupportedSOPClassFailuresByAETs).contains(moveDest);
    }
    
    String getStgCmtAET(String moveDest) {
        return (String) requestStgCmtFromAETs.get(moveDest);
    }    

    void logInstancesSent(RemoteNode node, InstancesAction action) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName,
                    "logInstancesSent",
                    new Object[] { node, action},
                    new String[] { RemoteNode.class.getName(),
                    	InstancesAction.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }

    void logDicomQuery(Dataset keys, RemoteNode node, String cuid) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName,
                    "logDicomQuery",
                    new Object[] { keys, node, cuid},
                    new String[] { Dataset.class.getName(), 
                    	RemoteNode.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }

    Socket createSocket(AEData aeData) throws IOException {
        return tlsConfig.createSocket(aeData);
    }

    public void queueStgCmtOrder(String calling, String called,
            Dataset actionInfo) {
        try {
            server.invoke(stgCmtScuScpName, "queueStgCmtOrder", new Object[] {
                    calling, called, actionInfo, Boolean.FALSE }, new String[] {
                    String.class.getName(), String.class.getName(),
                    Dataset.class.getName(), boolean.class.getName() });
        } catch (JMException e) {
            log.error("Failed to queue Storage C0mmitment Request", e);
        }
    }

	FileSystemMgtHome getFileSystemMgtHome()
	        throws HomeFactoryException {
	    return (FileSystemMgtHome) EJBHomeFactory.getFactory().lookup(
	            FileSystemMgtHome.class, FileSystemMgtHome.JNDI_NAME);
	}

	void updateStudyAccessTime(Set studyInfos) {
		if (!recordStudyAccessTime)
			return;
		
	    FileSystemMgt fsMgt;
	    try {
	        fsMgt = getFileSystemMgtHome().create();
	    } catch (Exception e) {
	        log.fatal("Failed to access FileSystemMgt EJB");
	        return;
	    }
	    try {
	        for (Iterator it = studyInfos.iterator(); it.hasNext();) {
	            String studyInfo = (String) it.next();
	            int delim = studyInfo.indexOf('@');
	            try {
	                fsMgt.touchStudyOnFileSystem(studyInfo.substring(0, delim),
	                        studyInfo.substring(delim + 1));
	            } catch (Exception e) {
	                log.warn("Failed to update access time for study "
	                        + studyInfo, e);
	            }
	        }
	    } finally {
	        try {
	            fsMgt.remove();
	        } catch (Exception ignore) {
	        }
	    }
	}
}
