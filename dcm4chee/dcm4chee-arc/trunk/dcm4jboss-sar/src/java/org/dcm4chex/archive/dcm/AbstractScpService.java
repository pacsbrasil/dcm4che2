/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4cheri.util.StringUtils;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 31.08.2003
 */
public abstract class AbstractScpService extends ServiceMBeanSupport {

    private static final String ANY = "ANY";

    private static final Map dumpParam = new HashMap(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }

    protected static final String[] ONLY_DEFAULT_TS = { UIDs.ImplicitVRLittleEndian,};

    protected static final String[] NATIVE_LE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian,};

    protected final static AssociationFactory asf = AssociationFactory.getInstance();

    protected ObjectName dcmServerName;

    protected ObjectName auditLogName;
    
    protected DcmHandler dcmHandler;

    protected String calledAETs;

    protected String callingAETs;
    
    protected boolean acceptExplicitVRLE = true;
        
    public final ObjectName getDcmServerName() {
        return dcmServerName;
    }

    public final void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    public final ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public final void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public final String getCalledAETs() {
        return calledAETs;
    }
    
    public final void setCalledAETs(String calledAETs) {
        if (getState() == STARTED)
            updatePolicy(null);
        this.calledAETs = calledAETs;
        updatePolicy();
    }

    protected void updatePolicy() {
        if (getState() == STARTED)
            updatePolicy(makeAcceptorPolicy());
    }

    public final String getCallingAETs() {
        return callingAETs != null ? callingAETs : ANY;
    }

    public final void setCallingAETs(String callingAETs) {
        this.callingAETs = ANY.equalsIgnoreCase(callingAETs) ? null : callingAETs;
        updatePolicy();
    }
    
    public final boolean isAcceptExplicitVRLE() {
        return acceptExplicitVRLE;
    }

    public final void setAcceptExplicitVRLE(boolean acceptExplicitVRLE) {
        this.acceptExplicitVRLE = acceptExplicitVRLE;
        updatePolicy();
    }
    
    protected void startService() throws Exception {
//        auditLogger = (AuditLogger) server.invoke(dcmServerName,
//                "getAuditLogger", null, null);
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, "getDcmHandler",
                null, null);
        bindDcmServices(dcmHandler.getDcmServiceRegistry());
        updatePolicy(makeAcceptorPolicy());
    }

    protected void updatePolicy(AcceptorPolicy policy) {
        String[] aets = StringUtils.split(calledAETs, '\\');
        for (int i = 0; i < aets.length; ++i) {
            dcmHandler.getAcceptorPolicy().putPolicyForCalledAET(aets[i],
                    policy);
        }
    }

    protected void stopService() throws Exception {
        updatePolicy(null);
        unbindDcmServices(dcmHandler.getDcmServiceRegistry());
        dcmHandler = null;
    }

    protected abstract void bindDcmServices(DcmServiceRegistry services);

    protected abstract void unbindDcmServices(DcmServiceRegistry services);

    protected abstract void initPresContexts(AcceptorPolicy policy);
    
    protected AcceptorPolicy makeAcceptorPolicy() {
        AcceptorPolicy policy = asf.newAcceptorPolicy();
        policy.setCallingAETs(callingAETs != null ? StringUtils.split(callingAETs,'\\') : null);
        initPresContexts(policy);
        return policy;        
    }

    protected String[] getTransferSyntaxUIDs() {
        return acceptExplicitVRLE ? NATIVE_LE_TS : ONLY_DEFAULT_TS;
    }
    
    protected static void addPresContexts(AcceptorPolicy policy, String[] asuids, String[] tsuids) {
        for (int i = 0; i < asuids.length; i++)
	        policy.putPresContext(asuids[i], tsuids);
    }        
    
    public void logDataset(String prompt, Dataset ds) {
        if (!log.isDebugEnabled()) { return; }
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, dumpParam);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }

    public Semaphore getCodecSemaphore() throws Exception {
        return (Semaphore) server.invoke(dcmServerName,
                "getCodecSemaphore", null, null);
    }
}
