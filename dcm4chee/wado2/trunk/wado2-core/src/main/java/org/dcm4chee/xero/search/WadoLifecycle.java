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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.search;

import java.security.Principal;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.ApplicationActivityMessage;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.servlet.Lifecycle;
import org.jboss.security.SecurityAssociation;

/** Handles the audit and initiates cache cleanup */
public class WadoLifecycle implements Lifecycle {
    private static Logger auditlog = Logger.getLogger("auditlog");

    Lifecycle cacheCleanup;
    
    /** Start this xero wado2 process. */
    public void start(String name) {
        auditEvent(AuditEvent.TypeCode.APPLICATION_START);
        cacheCleanup.start(name);
    }

    /** Gets the name of the user running this process */
    public static String getPrincipal() {
        Principal p = SecurityAssociation.getPrincipal();
        return p != null ? p.getName() : System.getProperty("user.name");
    }
  
    /** Audit a startup/shutdown event */
    public static void auditEvent(AuditEvent.TypeCode typeCode) {
        ApplicationActivityMessage msg = new ApplicationActivityMessage(typeCode);
        msg.addApplication(AuditMessage.getProcessID(), new String[]{"XERO-WADO2"}, AuditMessage.getProcessName(),AuditMessage.getLocalHostName());
        msg.addApplicationLauncher(getPrincipal(), null, null, AuditMessage.getLocalHostName());
        auditlog.info(msg);
    }

    /** Stops the wado2 application and audits it */
    public void stop(String name) {
        auditEvent(AuditEvent.TypeCode.APPLICATION_STOP);
        cacheCleanup.stop(name);
    }

    @MetaData(out="${ref:wado.CLEARCACHE}")
    public void setCacheCleanup(Lifecycle cacheCleanup) {
        this.cacheCleanup = cacheCleanup;
    }

    
}
