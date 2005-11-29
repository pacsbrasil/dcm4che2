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

package org.dcm4chex.archive.dcm.storescp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.management.JMException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.config.CompressionRules;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.mbean.FileSystemInfo;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.notif.SeriesStored;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 03.08.2003
 */
public class StoreScpService extends AbstractScpService {

    private static final long MIN_OUT_OF_RESOURCES_THRESHOLD = 20 * FileUtils.MEGA;    

    /** Map containing all image SOP Class UID. (key is name (as in config string), value is real uid) */
    private Map imageCUIDS = new TreeMap();

    /** Map containing all NOT image SOP Class UID. (key is name (as in config string), value is real uid) */
    private Map otherCUIDS = new TreeMap();

    private ObjectName fileSystemMgtName;

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
    
    private boolean acceptJPEGBaseline = true;

    private boolean acceptJPEGExtended = true;

    private boolean acceptJPEGLossless = true;

    private boolean acceptJPEGLossless14 = true;

    private boolean acceptJPEGLSLossless = true;

    private boolean acceptJPEGLSLossy = true;

    private boolean acceptJPEG2000Lossless = true;

    private boolean acceptJPEG2000Lossy = true;

    private boolean acceptRLELossless = false;

    private int bufferSize = 8192;

    private StoreScp scp = new StoreScp(this);

    public final int getBufferSize() {
        return bufferSize ;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    public final boolean isStudyDateInFilePath() {
		return scp.isStudyDateInFilePath();
	}

	public final void setStudyDateInFilePath(boolean enable) {
		scp.setStudyDateInFilePath(enable);
	}

    public final boolean isYearInFilePath() {
		return scp.isYearInFilePath();
	}

	public final void setYearInFilePath(boolean enable) {
		scp.setYearInFilePath(enable);
	}

    public final boolean isMonthInFilePath() {
		return scp.isMonthInFilePath();
	}

	public final void setMonthInFilePath(boolean enable) {
		scp.setMonthInFilePath(enable);
	}

    public final boolean isDayInFilePath() {
		return scp.isDayInFilePath();
	}

	public final void setDayInFilePath(boolean enable) {
		scp.setDayInFilePath(enable);
	}

    public final boolean isHourInFilePath() {
		return scp.isHourInFilePath();
	}

	public final void setHourInFilePath(boolean enable) {
		scp.setHourInFilePath(enable);
	}

    public final boolean isAcceptMissingPatientID() {
		return scp.isAcceptMissingPatientID();
	}

	public final void setAcceptMissingPatientID(boolean accept) {
		scp.setAcceptMissingPatientID(accept);
	}

    public final boolean isAcceptMissingPatientName() {
		return scp.isAcceptMissingPatientName();
	}

	public final void setAcceptMissingPatientName(boolean accept) {
		scp.setAcceptMissingPatientName(accept);
	}

    public final boolean isSerializeDBUpdate() {
		return scp.isSerializeDBUpdate();
	}

	public final void setSerializeDBUpdate(boolean serialize) {
		scp.setSerializeDBUpdate(serialize);
	}

    public final String getGeneratePatientID() {
		return scp.getGeneratePatientID();
	}

	public final void setGeneratePatientID(String pattern) {
		scp.setGeneratePatientID(pattern);
	}

	public final String getIssuerOfPatientIDRules() {
		return scp.getIssuerOfPatientIDRules();
	}

	public final void setIssuerOfPatientIDRules(String rules) {
		scp.setIssuerOfPatientIDRules(rules);
	}

	public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public String getCoerceWarnCallingAETs() {
        return scp.getCoerceWarnCallingAETs();
    }

    public void setCoerceWarnCallingAETs(String aets) {
        scp.setCoerceWarnCallingAETs(aets);
    }

    public boolean isStoreDuplicateIfDiffHost() {
        return scp.isStoreDuplicateIfDiffHost();
    }
    
    public void setStoreDuplicateIfDiffHost(boolean storeDuplicate) {
        scp.setStoreDuplicateIfDiffHost(storeDuplicate);
    }

    public boolean isStoreDuplicateIfDiffMD5() {
        return scp.isStoreDuplicateIfDiffMD5();
    }
    
    public void setStoreDuplicateIfDiffMD5(boolean storeDuplicate) {
        scp.setStoreDuplicateIfDiffMD5(storeDuplicate);
    }
    
    public final String getCompressionRules() {
        return scp.getCompressionRules().toString();
    }

    public void setCompressionRules(String rules) {
        scp.setCompressionRules(new CompressionRules(rules));
    }

    public final int getUpdateDatabaseMaxRetries() {
        return scp.getUpdateDatabaseMaxRetries();
    }

    public final void setUpdateDatabaseMaxRetries(int updateDatabaseMaxRetries) {
        scp.setUpdateDatabaseMaxRetries(updateDatabaseMaxRetries);
    }

    public final int getMaxCountUpdateDatabaseRetries() {
        return scp.getMaxCountUpdateDatabaseRetries();
    }

    public final void resetMaxCountUpdateDatabaseRetries() {
        scp.setMaxCountUpdateDatabaseRetries(0);
    }
    
    public final long getUpdateDatabaseRetryInterval() {
        return scp.getUpdateDatabaseRetryInterval();
    }
    
    public final void setUpdateDatabaseRetryInterval(long interval) {
        scp.setUpdateDatabaseRetryInterval(interval);
    }
    
    public final String getOutOfResourcesThreshold() {
        return FileUtils.formatSize(scp.getOutOfResourcesThreshold());
    }
    
    public final void setOutOfResourcesThreshold(String str) {
        scp.setOutOfResourcesThreshold(FileUtils.parseSize(str,
        		MIN_OUT_OF_RESOURCES_THRESHOLD));
    }
    
    public final boolean isAcceptJPEG2000Lossless() {
        return acceptJPEG2000Lossless;
    }

    public final void setAcceptJPEG2000Lossless(boolean accept) {
        if ( this.acceptJPEG2000Lossless == accept ) return;
        this.acceptJPEG2000Lossless = accept;
        enableService();
    }

    public final boolean isAcceptJPEG2000Lossy() {
        return acceptJPEG2000Lossy;
    }

    public final void setAcceptJPEG2000Lossy(boolean accept) {
       if ( this.acceptJPEG2000Lossy == accept ) return;
       this.acceptJPEG2000Lossy = accept;
       enableService();
    }

    public final boolean isAcceptJPEGBaseline() {
        return acceptJPEGBaseline;
    }

    public final void setAcceptJPEGBaseline(boolean accept) {
        if ( this.acceptJPEGBaseline == accept ) return;
        this.acceptJPEGBaseline = accept;
        enableService();
    }

    public final boolean isAcceptJPEGExtended() {
        return acceptJPEGExtended;
    }

    public final void setAcceptJPEGExtended(boolean accept) {
        if ( this.acceptJPEGExtended == accept ) return;
        this.acceptJPEGExtended = accept;
        enableService();
    }

    public final boolean isAcceptJPEGLossless14() {
        return acceptJPEGLossless14;
    }

    public final void setAcceptJPEGLossless14(boolean accept) {
        if ( this.acceptJPEGLossless14 == accept ) return;
        this.acceptJPEGLossless14 = accept;
        enableService();
    }

    public final boolean isAcceptJPEGLossless() {
        return acceptJPEGLossless;
    }

    public final void setAcceptJPEGLossless(boolean accept) {
        if ( this.acceptJPEGLossless == accept ) return;
        this.acceptJPEGLossless = accept;
        enableService();
    }

    public final boolean isAcceptJPEGLSLossless() {
        return acceptJPEGLSLossless;
    }

    public final void setAcceptJPEGLSLossless(boolean accept) {
       if ( this.acceptJPEGLSLossless == accept ) return;
       this.acceptJPEGLSLossless = accept;
       enableService();
    }

    public final boolean isAcceptJPEGLSLossy() {
        return acceptJPEGLSLossy;
    }

    public final void setAcceptJPEGLSLossy(boolean accept) {
        if ( this.acceptJPEGLSLossy == accept ) return;
        this.acceptJPEGLSLossy = accept;
        enableService();
    }

    public final boolean isAcceptRLELossless() {
        return acceptRLELossless;
    }

    public final void setAcceptRLELossless(boolean accept) {
        if ( this.acceptRLELossless == accept ) return;
        this.acceptRLELossless = accept;
        enableService();
    }

    public String getImageCUIDs() {
    	return toString(imageCUIDS);
    }
 
	public void setImageCUIDs( String uids ) {
		if ( getImageCUIDs().equals(uids)) return;
    	Map map =parseUIDs(uids);
    	updateBindings(imageCUIDS.values(), map.values());
    	putPresContexts(null,imageCUIDS,null);
    	putPresContexts(null,map,getImageTS());
    	imageCUIDS.clear();
    	imageCUIDS = map;
    }

    public String getOtherCUIDs() {
    	return toString(otherCUIDS);
    }
    public void setOtherCUIDs( String uids ) {
		if ( getOtherCUIDs().equals(uids)) return;
    	Map map = parseUIDs(uids);
    	updateBindings(otherCUIDS.values(), map.values());
    	putPresContexts(null,otherCUIDS,null);
    	putPresContexts(null,map,getTransferSyntaxUIDs());
    	otherCUIDS.clear();
    	otherCUIDS = map;
    }

    /**
     * Updates the service bindigs.
     * <p>
     * Add this storeSCP service for all SOP Class UIDs contained in newCUIDS.
     * <p>
     * Removes all bindings for CUIDs in oldCUIDS that are not in newCUIDS.
     * <p>
     * A <code>null</code> value means an empty list. (see <code>bindDcmServices, unbindDcmServices</code>)
     * <p>
     * Caution: oldCUIDS will be changed in this method if an item is also in newCUIDS! (item will be removed)
     * 
	 * @param oldCUIDS List of cuids already bind to this service.
	 * @param newCUIDS List of cuids that should be bind to this service.
	 * 
	 */
	private void updateBindings( Collection oldCUIDS, Collection newCUIDS) {
		if ( dcmHandler == null ) return; //nothing to do!
		DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
		if ( oldCUIDS == null ) oldCUIDS = new ArrayList();
		if ( newCUIDS == null ) newCUIDS = new ArrayList();
		Iterator iter = newCUIDS.iterator();
		String cuid;
		while ( iter.hasNext() ) {
			cuid = iter.next().toString();
			if ( ! oldCUIDS.remove(cuid) ) {
	            services.bind(cuid, scp);
			}
		}
		iter = oldCUIDS.iterator();
		while ( iter.hasNext() ) {
	            services.unbind(iter.next().toString());
		}
	}

	/**
	 * @param imageCUIDS2
	 * @return
	 */
	private String toString(Map uids) {
		if ( uids == null || uids.size() < 1 ) return "";
		StringBuffer sb = new StringBuffer( uids.size() << 5);//StringBuffer initial size: nrOfUIDs x 32
		Iterator iter = uids.keySet().iterator();
		sb.append(iter.next());
		while ( iter.hasNext() ) {
			sb.append('\r').append('\n').append(iter.next());
		}
		return sb.toString();
	}
    
    /**
	 * @param uids
	 * @return
	 */
	private Map parseUIDs(String uids) {
        StringTokenizer st = new StringTokenizer(uids, "\r\n;");
        String uid,name;
        Map map = new TreeMap();
        while ( st.hasMoreTokens() ) {
        	uid = st.nextToken().trim();
    		name = uid;
        	if ( isDigit(uid.charAt(0) ) ) {
        		if ( ! UIDs.isValid(uid) ) 
        			throw new IllegalArgumentException("UID "+uid+" isn't a valid UID!");
        	} else {
        		uid = UIDs.forName( name );
        	}
        	map.put(name,uid);
        }
		return map;
	}
	
	
	/**
	 * Simple digit check.
	 * <p>
	 * Checks only if char is between 0x30 and 0x39. (Not if type is DECIMAL_DIGIT_NUMBER)
	 * 
	 * @param c Char to test.
	 * 
	 * @return true if char is '0'-'9'
	 */
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

	protected void startService() throws Exception {
        super.startService();
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        updateBindings(null,imageCUIDS.values());
        updateBindings(null,otherCUIDS.values());
        dcmHandler.addAssociationListener(scp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        updateBindings(imageCUIDS.values(),null);
        updateBindings(otherCUIDS.values(),null);
        dcmHandler.removeAssociationListener(scp);
    }

    private String[] getImageTS() {
        ArrayList list = new ArrayList();
        if (acceptJPEGLossless14) {
            list.add(UIDs.JPEGLossless14);
        }
        if (acceptJPEGLossless) {
            list.add(UIDs.JPEGLossless);
        }
        if (acceptJPEGLSLossless) {
            list.add(UIDs.JPEGLSLossless);
        }
        if (acceptRLELossless) {
            list.add(UIDs.RLELossless);
        }
        if (acceptJPEG2000Lossless) {
            list.add(UIDs.JPEG2000Lossless);
        }
        if (acceptExplicitVRLE) {
            list.add(UIDs.ExplicitVRLittleEndian);
        }
        list.add(UIDs.ImplicitVRLittleEndian);
        if (acceptJPEGBaseline) {
            list.add(UIDs.JPEGBaseline);
        }
        if (acceptJPEGExtended) {
            list.add(UIDs.JPEGExtended);
        }
        if (acceptJPEGLSLossy) {
            list.add(UIDs.JPEGLSLossy);
        }
        if (acceptJPEG2000Lossy) {
            list.add(UIDs.JPEG2000Lossy);
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        putPresContexts(policy, imageCUIDS, enable ? getImageTS() : null);
        putPresContexts(policy, otherCUIDS, enable ? getTransferSyntaxUIDs() : null);
    }

    private void putPresContexts(AcceptorPolicy policy, Map cuids, String[] tsuids) {
    	Iterator iter = cuids.values().iterator();
        while (iter.hasNext()) {
        	if ( policy == null ) {
        		if ( dcmHandler == null ) return;
        		AcceptorPolicy policies = dcmHandler.getAcceptorPolicy();
        		String cuid = iter.next().toString();
    	        for (int i = 0; i < calledAETs.length; ++i) {
    	            policy = policies.getPolicyForCalledAET(calledAETs[i]);
            		policy.putPresContext(cuid, tsuids);
    	        }
        	} else {
        		policy.putPresContext(iter.next().toString(), tsuids);
        	}
        }
     }

    FileSystemInfo selectStorageFileSystem() {
        try {
            return (FileSystemInfo) server.invoke(fileSystemMgtName,
                    "selectStorageFileSystem",
                    null,
                    null);
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke selectStorageFileSystem", e);
        }
    }

    boolean isLocalFileSystem(String dirpath) {
        try {
            Boolean b = (Boolean) server.invoke(fileSystemMgtName,
                    "isLocalFileSystem",
                    new Object[] { dirpath},
                    new String[] { String.class.getName()});
            return b.booleanValue();
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke isLocalFileSystem", e);
        }
    }
    
	boolean isFreeDiskSpaceOnDemand() {
        try {
            Boolean b = (Boolean) server.getAttribute(fileSystemMgtName,
                    "FreeDiskSpaceOnDemand");
            return b.booleanValue();
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke getAttribute 'FreeDiskSpaceOnDemand'", e);
        }
	}
	
	void callFreeDiskSpace() {
        try {
            server.invoke(fileSystemMgtName,
                    "freeDiskSpace",
                    null,
                    null);
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke freeDiskSpace", e);
        }
		
	}

	void sendJMXNotification(Object o) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this, eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
	}

	void logInstancesStored(Socket s, SeriesStored seriesStored) {
        if (auditLogName == null) return;
	    final AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
	    InstancesAction action = alf.newInstancesAction("Create", 
				seriesStored.getStudyInstanceUID(),
	            alf.newPatient(
						seriesStored.getPatientID(),
						seriesStored.getPatientName()));
		action.setMPPSInstanceUID(seriesStored.getPPSInstanceUID());
	    action.setAccessionNumber(seriesStored.getAccessionNumber());
		DcmElement sq = seriesStored.getRefSOPSeq();
		int n = sq.vm();
		for (int i = 0; i < n; i++) {
			action.addSOPClassUID(sq.getItem(i).getString(Tags.SOPClassUID));
		}
		action.setNumberOfInstances(seriesStored.getNumberOfInstances());
	    RemoteNode remoteNode = alf.newRemoteNode(s, seriesStored.getCallingAET());
        try {
            server.invoke(auditLogName,
                    "logInstancesStored",
                    new Object[] { remoteNode, action},
                    new String[] { RemoteNode.class.getName(), 
                    	InstancesAction.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }		
	}
    
	
}