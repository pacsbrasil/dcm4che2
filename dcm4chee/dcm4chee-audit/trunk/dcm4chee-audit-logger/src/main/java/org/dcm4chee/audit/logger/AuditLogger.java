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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chee.audit.logger;

import java.security.Principal;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.ApplicationActivityMessage;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.AuditSource;
import org.jboss.security.SecurityAssociation;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 25, 2007
 */
public class AuditLogger extends ServiceMBeanSupport {

    private static Logger auditlog = Logger.getLogger("auditlog");
    private AuditSource auditSource = AuditSource.getDefaultAuditSource();
    private String[] aets;

    public boolean isIHEYr4() {
        return false;
    }

    public String getAuditSourceID() {
        return auditSource.getAuditSourceID();
    }
    
    public void setAuditSourceID(String id) {
        auditSource.setAuditSourceID(id);
    }
    
    private static String maskNull(String val, String nullVal) {
        return val == null ? nullVal : val;
    }

    private static String umaskNull(String val, String nullVal) {
        return nullVal.equals(val) ? null : val;
    }

    public String getAuditEnterpriseSiteID() {
        return maskNull(auditSource.getAuditEnterpriseSiteID(), "-");
    }
    
    public void setAuditEnterpriseSiteID(String id) {
        auditSource.setAuditEnterpriseSiteID(umaskNull(id, "-"));
    }

    public  String getAuditSourceTypeCodes() {
        List l = auditSource.getAuditSourceTypeCodes();
        if (l.isEmpty()) {
            return "-";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = l.size(); i < n; i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(((AuditSource.TypeCode) l.get(i)).getCode());
        }
        return sb.toString();
    }

    public void setAuditSourceTypeCodes(String codes) {
        if (codes == null || codes.length() == 0 || "-".equals(codes)) {
            auditSource.clearAuditSourceTypeCodes();
            return;
        }
        String[] ss = codes.split(",");
        AuditSource.TypeCode[] types = new AuditSource.TypeCode[ss.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = AuditSource.TypeCode.valueOf(ss[i]);
        }
        auditSource.clearAuditSourceTypeCodes();
        for (int i = 0; i < types.length; i++) {
            auditSource.addAuditSourceTypeCode(types[i]);
        }
    }
    
    public String getProcessID() {
        return AuditMessage.getProcessID();
    }

    public String getProcessName() {
        return AuditMessage.getProcessName();
    }

    public void setProcessName(String processName) {
        AuditMessage.setProcessName(processName);
    }
    
    public String getAETitles() {
        if (aets == null || aets.length == 0) {
            return "-";
        }
        if (aets.length == 1) {
            return aets[0];
        }
        StringBuffer sb = new StringBuffer(aets[0]);
        for (int i = 1; i < aets.length; i++) {
            sb.append('\\').append(aets[i]);
        }
        return sb.toString();
    }

    public void setAETitles(String aets) {
        if (aets == null || aets.length() == 0 || "-".equals(aets)) {
            this.aets = null;
        } else {
            this.aets = aets.split("\\\\");
        }
    }
    
    public boolean isEnableDNSLookups() {
        return AuditMessage.isEnableDNSLookups();
    }

    public void setEnableDNSLookups(boolean enableDNSLookups) {
        AuditMessage.setEnableDNSLookups(enableDNSLookups);
    }
    
    public boolean isIncludeXMLDeclaration() {
        return AuditMessage.isIncludeXMLDeclaration();
    }

    public void setIncludeXMLDeclaration(boolean incXMLDecl) {
        AuditMessage.setIncludeXMLDeclaration(incXMLDecl);
    }
    
    public boolean isTimezonedDateTime() {
        return AuditMessage.isTimezonedDateTime();
    }

    public void setTimezonedDateTime(boolean timezonedDateTime) {
        AuditMessage.setTimezonedDateTime(timezonedDateTime);
    }

    public boolean isUtcDateTime() {
        return AuditMessage.isUtcDateTime();
    }

    public void setUtcDateTime(boolean utcDateTime) {
        AuditMessage.setUtcDateTime(utcDateTime);
    }    
    
    protected void startService() {
        auditApplicationActivity(ApplicationActivityMessage.APPLICATION_START);
    }

    protected void stopService() {
        auditApplicationActivity(ApplicationActivityMessage.APPLICATION_STOP);
    }

    private void auditApplicationActivity(AuditEvent.TypeCode type) {
        ApplicationActivityMessage msg = new ApplicationActivityMessage(type);
        msg.addApplication(AuditMessage.getProcessID(), aets,
                AuditMessage.getProcessName(),
                AuditMessage.getLocalHostName());
        msg.addApplicationLauncher(getPrincipal(), null, null, null);
        auditlog.info(msg);
    }

    private String getPrincipal() {
        Principal p = SecurityAssociation.getPrincipal();
        return p != null ? p.getName() : System.getProperty("user.name");
    }
}
