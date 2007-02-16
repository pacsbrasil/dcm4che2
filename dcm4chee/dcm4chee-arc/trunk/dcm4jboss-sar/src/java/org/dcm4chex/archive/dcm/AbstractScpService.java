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

package org.dcm4chex.archive.dcm;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.PDataTF;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.util.DTFormat;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.XSLTUtils;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 31.08.2003
 */
public abstract class AbstractScpService extends ServiceMBeanSupport {

    protected static final String ANY = "ANY";

    protected static final String NONE = "NONE";

    protected ObjectName dcmServerName;

    protected ObjectName auditLogName;

    protected DcmHandler dcmHandler;

    protected String[] calledAETs;

    protected String[] callingAETs;

    /**
     * Map containing accepted Transfer Syntax UIDs. key is name (as in config
     * string), value is real uid)
     */
    protected Map tsuidMap = new LinkedHashMap();

    protected int maxPDULength = PDataTF.DEF_MAX_PDU_LENGTH;

    protected int maxOpsInvoked = 1;

    protected int maxOpsPerformed = 1;

    protected String[] logCallingAETs = {};

    protected File logDir;

    protected File coerceConfigDir;

    protected Hashtable templates = new Hashtable();

    private final NotificationListener callingAETChangeListener = 
        new NotificationListener() {
            public void handleNotification(Notification notif, Object handback) {
                try {
                    log.debug("Handle callingAET change notification!");
                    String[] affectedCalledAETs = (String[]) notif.getUserData();
                    if ( areCalledAETsAffected(affectedCalledAETs)) {
                        AcceptorPolicy policy1 = dcmHandler.getAcceptorPolicy()
                            .getPolicyForCalledAET(affectedCalledAETs[0]);
                        String[] calledCallingAETs = policy1.getCallingAETs();
                        String newCallingAETs = calledCallingAETs.length > 0 ? StringUtils.toString(calledCallingAETs,'\\') : "ANY";
                        log.debug("newCallingAETs:"+newCallingAETs);
                        server.setAttribute(serviceName, new Attribute("CallingAETitles", newCallingAETs));
                    }
                } catch (Throwable th) {
                   log.warn("Failed to process callingAET change notification: ", th);       
                }
            }

            private boolean areCalledAETsAffected(String[] affectedCalledAETs) {
                if ( calledAETs == null) return true;
                if ( affectedCalledAETs != null ) {
                    for ( int i = 0 ; i < affectedCalledAETs.length ; i++ ) {
                        for ( int j = 0 ; j < calledAETs.length ; j++) {
                            if ( affectedCalledAETs[i].equals(calledAETs[j])) return true;
                        }
                    }
                }
                return false;
            }
        };
        
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
        return calledAETs == null ? "" : StringUtils.toString(calledAETs, '\\');
    }

    public final void setCalledAETs(String calledAETs) {
        if (getCalledAETs().equals(calledAETs))
            return;
        disableService();
        this.calledAETs = StringUtils.split(calledAETs, '\\');
        enableService();
    }

    public final String getLogCallingAETs() {
        return StringUtils.toString(logCallingAETs, '\\');
    }

    public final void setLogCallingAETs(String aets) {
        logCallingAETs = StringUtils.split(aets, '\\');
    }

    public final int getMaxPDULength() {
        return maxPDULength;
    }

    public final void setMaxPDULength(int maxPDULength) {
        if (this.maxPDULength == maxPDULength)
            return;
        this.maxPDULength = maxPDULength;
        enableService();
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        if (this.maxOpsInvoked == maxOpsInvoked)
            return;
        this.maxOpsInvoked = maxOpsInvoked;
        enableService();
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        if (this.maxOpsPerformed == maxOpsPerformed)
            return;
        this.maxOpsPerformed = maxOpsPerformed;
        enableService();
    }

    public final String getCoerceConfigDir() {
        return coerceConfigDir.getPath();
    }

    public final void setCoerceConfigDir(String path) {
        this.coerceConfigDir = new File(path.replace('/', File.separatorChar));
    }

    protected boolean enableService() {
        if (dcmHandler == null)
            return false;
        boolean changed = false;
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < calledAETs.length; ++i) {
            AcceptorPolicy policy1 = policy
                    .getPolicyForCalledAET(calledAETs[i]);
            if (policy1 == null) {
                policy1 = AssociationFactory.getInstance().newAcceptorPolicy();
                policy1.setCallingAETs(callingAETs);
                policy.putPolicyForCalledAET(calledAETs[i], policy1);
                policy.addCalledAET(calledAETs[i]);
                changed = true;
            } else {
                String[] aets = policy1.getCallingAETs();
                if (aets.length == 0 ) {
                    if (callingAETs != null) {
                        policy1.setCallingAETs(callingAETs);
                        changed = true;
                    }
                } else {
                    if ( ! haveSameItems(aets, callingAETs) ) {
                        policy1.setCallingAETs(callingAETs);
                        changed = true;
                    }
                }
            }
            policy1.setMaxPDULength(maxPDULength);
            policy1.setAsyncOpsWindow(maxOpsInvoked, maxOpsPerformed);
            updatePresContexts(policy1, true);
        }
        return changed;
    }

    // Only check if all items in o1 are also in o2! (and same length) 
    // e.g. {"a","a","d"}, {"a","d","d"} will also return true!
    private boolean haveSameItems(Object[] o1, Object[] o2) {
        if ( o1 == null || o2 == null || o1.length != o2.length ) return false;
        if ( o1.length == 1 ) 
            return o1[0].equals(o2[0]);
        iloop:for ( int i = 0, len = o1.length ; i < len ; i++ ) {
            for ( int j = 0 ; j < len ; j++ ) {
                if ( o1[i].equals( o2[j]))
                    continue iloop;
            }
            return false;
        }
        return true;
    }

    private void disableService() {
        if (dcmHandler == null)
            return;
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < calledAETs.length; ++i) {
            AcceptorPolicy policy1 = policy
                    .getPolicyForCalledAET(calledAETs[i]);
            if (policy1 != null) {
                updatePresContexts(policy1, false);
                if (policy1.listPresContext().isEmpty()) {
                    policy.putPolicyForCalledAET(calledAETs[i], null);
                    policy.removeCalledAET(calledAETs[i]);
                }
            }
        }
    }

    public final String getCallingAETs() {
        return callingAETs != null ? StringUtils.toString(callingAETs, '\\')
                : ANY;
    }

    public final void setCallingAETs(String callingAETs) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if (getCallingAETs().equals(callingAETs))
            return;
        this.callingAETs = ANY.equalsIgnoreCase(callingAETs) ? null
                : StringUtils.split(callingAETs, '\\');
        if ( enableService() ) {
            server.invoke(dcmServerName, "notifyCallingAETchange",
                    new Object[] {calledAETs} , 
                    new String[] {String[].class.getName()});
        }
    }

    protected void updateAcceptedSOPClass(Map cuidMap, String newval,
            DcmService scp) {
        Map tmp = parseUIDs(newval);
        if (cuidMap.keySet().equals(tmp.keySet()))
            return;
        disableService();
        if (scp != null)
            unbindAll(valuesToStringArray(cuidMap));
        cuidMap.clear();
        cuidMap.putAll(tmp);
        if (scp != null)
            bindAll(valuesToStringArray(cuidMap), scp);
        enableService();
    }

    // protected String[] getTransferSyntaxUIDs() {
    // return valuesToStringArray(tsuids);
    // }

    protected static String[] valuesToStringArray(Map tsuid) {
        return (String[]) tsuid.values().toArray(new String[tsuid.size()]);
    }

    protected void bindAll(String[] cuids, DcmService scp) {
        if (dcmHandler == null)
            return; // nothing to do!
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++) {
            services.bind(cuids[i], scp);
        }
    }

    protected void unbindAll(String[] cuids) {
        if (dcmHandler == null)
            return; // nothing to do!
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++) {
            services.unbind(cuids[i]);
        }
    }

    public String getAcceptedTransferSyntax() {
        return toString(tsuidMap);
    }

    public void setAcceptedTransferSyntax(String s) {
        updateAcceptedTransferSyntax(tsuidMap, s);
    }

    protected void updateAcceptedTransferSyntax(Map tsuidMap, String newval) {
        Map tmp = parseUIDs(newval);
        if (tsuidMap.keySet().equals(tmp.keySet()))
            return;
        tsuidMap.clear();
        tsuidMap.putAll(tmp);
        enableService();
    }

    protected String toString(Map uids) {
        if (uids == null || uids.isEmpty())
            return "";
        String nl = System.getProperty("line.separator", "\n");
        StringBuffer sb = new StringBuffer();
        Iterator iter = uids.keySet().iterator();
        while (iter.hasNext()) {
            sb.append(iter.next()).append(nl);
        }
        return sb.toString();
    }

    protected static Map parseUIDs(String uids) {
        StringTokenizer st = new StringTokenizer(uids, " \t\r\n;");
        String uid, name;
        Map map = new LinkedHashMap();
        while (st.hasMoreTokens()) {
            uid = st.nextToken().trim();
            name = uid;

            if (isDigit(uid.charAt(0))) {
                if (!UIDs.isValid(uid))
                    throw new IllegalArgumentException("UID " + uid
                            + " isn't a valid UID!");
            } else {
                uid = UIDs.forName(name);
            }
            map.put(name, uid);
        }
        return map;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    protected void startService() throws Exception {
        logDir = new File(ServerConfigLocator.locate().getServerHomeDir(),
                "log");
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, "dcmHandler",
                null, null);
        bindDcmServices(dcmHandler.getDcmServiceRegistry());
        server.addNotificationListener(dcmServerName, callingAETChangeListener, null, null);
        enableService();
    }

    protected void stopService() throws Exception {
        disableService();
        unbindDcmServices(dcmHandler.getDcmServiceRegistry());
        dcmHandler = null;
        templates.clear();
        server.removeNotificationListener(dcmServerName, callingAETChangeListener);
     }

    protected abstract void bindDcmServices(DcmServiceRegistry services);

    protected abstract void unbindDcmServices(DcmServiceRegistry services);

    protected abstract void updatePresContexts(AcceptorPolicy policy,
            boolean enable);

    protected void putPresContexts(AcceptorPolicy policy, String[] cuids,
            String[] tsuids) {
        for (int i = 0; i < cuids.length; i++) {
            policy.putPresContext(cuids[i], tsuids);
        }
    }

    protected void putRoleSelections(AcceptorPolicy policy, String[] cuids,
            boolean scu, boolean scp) {
        for (int i = 0; i < cuids.length; i++) {
            policy.putRoleSelection(cuids[i], scu, scp);
        }
    }

    public File getLogFile(Date now, String callingAET, String suffix) {
        File dir = new File(logDir, callingAET);
        dir.mkdir();
        return new File(dir, new DTFormat().format(now) + suffix);
    }

    public Templates getCoercionTemplatesFor(String aet, String fname) {
        // check AET specific attribute coercion configuration
        File f = FileUtils.resolve(new File(new File(coerceConfigDir, aet),
                fname));
        if (!f.exists()) {
            // check general attribute coercion configuration
            f = FileUtils.resolve(new File(coerceConfigDir, fname));
            if (!f.exists()) {
                return null;
            }
        }
        Templates tpl = (Templates) templates.get(f);
        if (tpl == null) {
            try {
                tpl = TransformerFactory.newInstance().newTemplates(
                        new StreamSource(f));
            } catch (Exception e) {
                log.error("Compiling Stylesheet " + f + " failed:", e);
                return null;
            }
            templates.put(f, tpl);
        }
        return tpl;
    }

    public void reloadStylesheets() {
        templates.clear();
    }

    private boolean contains(Object[] a, Object e) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(e)) {
                return true;
            }
        }
        return false;
    }

    public void logDIMSE(Association a, String suffix, Dataset ds) {
        String callingAET = a.getCallingAET();
        if (contains(logCallingAETs, callingAET)) {
            try {
                XSLTUtils.writeTo(ds,
                        getLogFile(new Date(), callingAET, suffix));
            } catch (Exception e) {
                log.warn("Logging of attributes failed:", e);
            }
        }
    }

    public Dataset getCoercionAttributesFor(Association a, String xsl,
            Dataset in) {
        String callingAET = a.getCallingAET();
        Templates stylesheet = getCoercionTemplatesFor(callingAET, xsl);
        if (stylesheet == null) {
            return null;
        }
        Dataset out = DcmObjectFactory.getInstance().newDataset();
        try {
            XSLTUtils.xslt(in, stylesheet, a, out);
        } catch (Exception e) {
            log.error("Attribute coercion failed:", e);
            return null;
        }
        return out;
    }

    public void coerceAttributes(DcmObject ds, DcmObject coerce) {
        coerceAttributes(ds, coerce, null);
    }

    private void coerceAttributes(DcmObject ds, DcmObject coerce,
            DcmElement parent) {
        boolean coerced = false;
        for (Iterator it = coerce.iterator(); it.hasNext();) {
            DcmElement el = (DcmElement) it.next();
            DcmElement oldEl = ds.get(el.tag());
            if (el.isEmpty()) {
                coerced = oldEl != null && !oldEl.isEmpty();
                if (oldEl == null || coerced) {
                    ds.putXX(el.tag(), el.vr());
                }
            } else {
                Dataset item;
                DcmElement sq = oldEl;
                switch (el.vr()) {
                case VRs.SQ:
                    coerced = oldEl != null && sq.vr() != VRs.SQ;
                    if (oldEl == null || coerced) {
                        sq = ds.putSQ(el.tag());
                    }
                    for (int i = 0, n = el.countItems(); i < n; ++i) {
                        item = sq.getItem(i);
                        if (item == null) {
                            item = sq.addNewItem();
                        }
                        Dataset coerceItem = el.getItem(i);
                        coerceAttributes(item, coerceItem, el);
                        if (!coerceItem.isEmpty()) {
                            coerced = true;
                        }
                    }
                    break;
                case VRs.OB:
                case VRs.OF:
                case VRs.OW:
                case VRs.UN:
                    if (el.hasDataFragments()) {
                        coerced = true;
                        sq = ds.putXXsq(el.tag(), el.vr());
                        for (int i = 0, n = el.countItems(); i < n; ++i) {
                            sq.addDataFragment(el.getDataFragment(i));
                        }
                        break;
                    }
                default:
                    coerced = oldEl != null && !oldEl.equals(el);
                    if (oldEl == null || coerced) {
                        ds.putXX(el.tag(), el.vr(), el.getByteBuffer());
                    }
                    break;
                }
            }
            if (coerced) {
                log
                        .info(parent == null ? ("Coerce " + oldEl + " to " + el)
                                : ("Coerce " + oldEl + " to " + el
                                        + " in item of " + parent));
            } else {
                if (oldEl == null && log.isDebugEnabled()) {
                    log.debug(parent == null ? ("Add " + el) : ("Add " + el
                            + " in item of " + parent));
                }
                it.remove();
            }
        }
    }
    

    public void sendJMXNotification(Object o) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this,
                eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
    }
    
}
