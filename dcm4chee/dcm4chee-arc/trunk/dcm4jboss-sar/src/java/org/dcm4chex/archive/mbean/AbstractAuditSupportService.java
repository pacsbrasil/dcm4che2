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

package org.dcm4chex.archive.mbean;

import javax.management.ObjectName;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision$ $Date$
 * @since 17.02.2005
 */
public abstract class AbstractAuditSupportService extends ServiceMBeanSupport {

    private static final String WEB_REQUEST_KEY = "javax.servlet.http.HttpServletRequest";
    
    protected ObjectName auditLogName;

    private Boolean auditLogIHEYr4;

	public AbstractAuditSupportService() {
    }
   
    public final ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public final void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }
    
    protected boolean isAuditLogIHEYr4() {
        if (auditLogName == null) {
            return false;
        }
        if (auditLogIHEYr4 == null) {
            try {
                this.auditLogIHEYr4 = (Boolean) server.getAttribute(
                        auditLogName, "IHEYr4");
            } catch (Exception e) {
                log.warn("JMX failure: ", e);
                this.auditLogIHEYr4 = Boolean.FALSE;
            }
        }
        return auditLogIHEYr4.booleanValue();
    }

    public HttpUserInfo getHttpUserInfo() {
        return new HttpUserInfo();
    }
    
    class HttpUserInfo {
        private String userId, hostName;
        
        HttpUserInfo() {
            try {
                HttpServletRequest rq = (HttpServletRequest) PolicyContext.getContext(WEB_REQUEST_KEY);
                userId = rq.getRemoteUser();
                hostName = rq.getRemoteHost();
            } catch (PolicyContextException e) {
                log.warn("Cant get http request to get User Participiant!",e);
            }
        }
        
        protected String getUserId() {
            return userId;
        }
        
        protected String getHostName() {
            return hostName;
        }
    }
}
