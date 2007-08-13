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
 * Gunter Zeilinger <gunterze@gmail.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che.archive.mbean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.archive.entity.AE;
import org.dcm4che.archive.exceptions.UnknownAETException;
import org.dcm4che.archive.service.AEManager;
import org.dcm4che.archive.util.ejb.EJBReferenceCache;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.SecurityAlertMessage;

/**
 * <description>
 * 
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since July 24, 2002
 * @version $Revision: 1.3 $ $Date: 2007/07/19 06:22:57 $
 */
public class AEService extends MBeanServiceBase {

    private AuditLoggerDelegate auditLogger = new AuditLoggerDelegate(this);

    private ObjectName echoServiceName;

    private boolean dontSaveIP = true;

    private int[] portNumbers;

    /**
     * @return Returns the echoServiceName.
     */
    public ObjectName getEchoServiceName() {
        return echoServiceName;
    }

    /**
     * @param echoServiceName
     *            The echoServiceName to set.
     */
    public void setEchoServiceName(ObjectName echoServiceName) {
        this.echoServiceName = echoServiceName;
    }

    public ObjectName getAuditLoggerName() {
        return auditLogger.getAuditLoggerName();
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogger.setAuditLoggerName(auditLogName);
    }

    /**
     * @return Returns the autoConfig.
     */
    public boolean isDontSaveIP() {
        return dontSaveIP;
    }

    /**
     * @param dontSaveIP
     *            The dontSaveIP to set.
     */
    public void setDontSaveIP(boolean dontSaveIP) {
        this.dontSaveIP = dontSaveIP;
    }

    /**
     * @return Returns the portNumbers.
     */
    public String getPortNumbers() {
        if (portNumbers == null || portNumbers.length < 1)
            return "NONE";
        int len = portNumbers.length;
        String first = String.valueOf(portNumbers[0]);
        if (len == 1)
            return first;
        StringBuilder sb = new StringBuilder(first);
        for (int i = 1; i < len; i++)
            sb.append(",").append(portNumbers[i]);
        return sb.toString();
    }

    /**
     * @param portNumbers
     *            The portNumbers to set.
     */
    public void setPortNumbers(String ports) {
        if (ports == null || "NONE".equalsIgnoreCase(ports)) {
            portNumbers = null;
        }
        else {
            StringTokenizer st = new StringTokenizer(ports, ",");
            portNumbers = new int[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                portNumbers[i] = Integer.parseInt(st.nextToken());
            }
        }
    }

    public String getAEs() throws Exception {
        Collection c = aeMgr().findAll();
        StringBuilder sb = new StringBuilder();
        AE ae;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            ae = (AE) iter.next();
            sb.append(ae.toString()).append(" cipher:").append(
                    ae.getCipherSuites()).append("\r\n");
        }
        return sb.toString();
    }

    public List listAEs() throws Exception {
        return aeMgr().findAll();
    }

    public AE getAE(String title) throws Exception {
        return aeMgr().findByAET(title);
    }

    public boolean updateAETitle(String prevAET, String newAET)
            throws Exception {
        if (prevAET.equals(newAET)) {
            return false;
        }
        AEManager aeManager = aeMgr();
        try {
            AE aeData = aeManager.findByAET(prevAET);
            aeData.setTitle(newAET);
            aeManager.updateAE(aeData);
            return true;
        }
        catch (UnknownAETException e) {
            return false;
        }
    }

    public AE getAE(String title, String host) throws RemoteException,
            Exception {
        return getAE(title, host == null ? null : InetAddress.getByName(host));
    }

    public AE getAE(String aet, InetAddress addr) throws Exception {
        AEManager aetMgr = aeMgr();
        try {
            return aetMgr.findByAET(aet);
        }
        catch (UnknownAETException e) {
            return autoConfigAE(aet, addr, aetMgr);
        }
    }

    private AE autoConfigAE(String aet, InetAddress addr, AEManager aetMgr)
            throws Exception {
        if (portNumbers == null || addr == null) {
            return null;
        }
        String aeHost = addr.getHostName();
        for (int i = 0; i < portNumbers.length; i++) {
            AE ae = new AE(null, aet, aeHost, portNumbers[i], null, null, null);
            if (echo(ae)) {
                if (dontSaveIP) {
                    if (!aeHost.equals(addr.getHostAddress()))
                        aetMgr.newAE(ae);
                }
                else {
                    aetMgr.newAE(ae);
                }
                logActorConfig("Add new auto-configured AE " + ae,
                        SecurityAlertMessage.NETWORK_CONFIGURATION);
                return ae;
            }
        }
        return null;
    }

    /**
     * Adds or updates an AE Title.
     * 
     * @param pk
     *            The primary key of the AE title in the database if this is an
     *            update.
     * @param aet
     *            Application Entity Title
     * @param host
     *            Hostname or IP addr.
     * @param port
     *            port number
     * @param cipher
     *            String with cypher(s) to create a secure connection (seperated
     *            with ',') or null
     * @param issuer
     *            A string containing the issuer of the patient id used at this
     *            AE title.
     * @param desc
     *            A description string for this AE title.
     * @param checkHost
     *            Enable/disable checking if the host can be resolved.
     * 
     * @throws Exception
     */
    public void updateAE(Long pk, String title, String host, int port,
            String cipher, String issuer, String desc, boolean checkHost)
            throws Exception {
        if (checkHost) {
            try {
                host = InetAddress.getByName(host).getCanonicalHostName();
            }
            catch (UnknownHostException x) {
                throw new IllegalArgumentException(
                        "Host "
                                + host
                                + " can't be resolved! Disable hostname check to force addition of new AE!");
            }
        }

        AEManager aeManager = aeMgr();
        if (pk == null) {
            AE aeNew = new AE(null, title, host, port, cipher, issuer, desc);
            aeManager.newAE(aeNew);
            logActorConfig("Add AE " + aeNew + " cipher:"
                    + aeNew.getCipherSuites(),
                    SecurityAlertMessage.NETWORK_CONFIGURATION);
        }
        else {
            AE aeOld = aeManager.findByPrimaryKey(pk);
            if (!aeOld.getTitle().equals(title)) {
                try {
                    AE aeOldByTitle = aeManager.findByAET(title);
                    throw new IllegalArgumentException("AE Title " + title
                            + " already exists!:" + aeOldByTitle);
                }
                catch (UnknownAETException e) {
                }
            }
            AE aeNew = new AE(pk, title, host, port, cipher, issuer, desc);
            aeManager.updateAE(aeNew);
            logActorConfig("Modify AE " + aeOld + " -> " + aeNew,
                    SecurityAlertMessage.NETWORK_CONFIGURATION);
        }
    }

    public void addAE(String title, String host, int port, String cipher,
            String issuer, String desc, boolean checkHost) throws Exception {
        updateAE(null, title, host, port, cipher, issuer, desc, checkHost);
    }

    /**
     * Remove the specified AE titles from the database.
     * 
     * @param titles
     *            A delimited (space, comma, semi-colon, tab, new line) string
     *            containing the AE titles to remove.
     * @throws Exception
     */
    public void removeAE(String titles) throws Exception {
        StringTokenizer st = new StringTokenizer(titles, " ,;\t\r\n");
        AE ae;
        AEManager aeManager = aeMgr();
        while (st.hasMoreTokens()) {
            ae = aeManager.findByAET(st.nextToken());
            aeManager.removeAE(ae.getPk());
            logActorConfig("Remove AE " + ae,
                    SecurityAlertMessage.NETWORK_CONFIGURATION);
        }
    }

    private void logActorConfig(String desc, AuditEvent.TypeCode eventTypeCode) {
        log.info(desc);
        try {
            if (auditLogger.isAuditLogIHEYr4()) {
                server.invoke(auditLogger.getAuditLoggerName(),
                        "logActorConfig", new Object[] { desc, "NetWorking" },
                        new String[] { String.class.getName(),
                                String.class.getName(), });
            }
            else {
                HttpUserInfo userInfo = new HttpUserInfo(AuditMessage
                        .isEnableDNSLookups());
                SecurityAlertMessage msg = new SecurityAlertMessage(
                        eventTypeCode);
                msg.addReportingProcess(AuditMessage.getProcessID(),
                        AuditMessage.getLocalAETitles(), AuditMessage
                                .getProcessName(), AuditMessage
                                .getLocalHostName());
                msg.addPerformingPerson(userInfo.getUserId(), null, null,
                        userInfo.getHostName());
                msg.addAlertSubjectWithNodeID(AuditMessage.getLocalNodeID(),
                        desc);
                msg.validate();
                Logger.getLogger("auditlog").info(msg);
            }
        }
        catch (Exception e) {
            log.warn("Failed to log ActorConfig:", e);
        }
    }

    private boolean echo(AE ae) {
        try {
            Boolean result = (Boolean) server.invoke(this.echoServiceName,
                    "checkEcho", new Object[] { ae }, new String[] { AE.class
                            .getName() });
            return result.booleanValue();
        }
        catch (Exception e) {
            log.warn("Failed to use echo service:", e);
            return false;
        }

    }

    protected AEManager aeMgr() {
        return (AEManager) EJBReferenceCache.getInstance().lookup("AEManager/Local");
    }
}
