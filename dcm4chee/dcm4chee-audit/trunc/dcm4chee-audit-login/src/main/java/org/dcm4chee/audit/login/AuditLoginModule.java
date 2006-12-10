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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
 
package org.dcm4chee.audit.login;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.NetworkAccessPoint;
import org.dcm4che2.audit.message.UserAuthenticationMessage;
import org.dcm4che2.audit.message.UserWithLocation;
import org.dcm4che2.audit.message.AuditEvent.OutcomeIndicator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 29, 2006
 */
public class AuditLoginModule implements LoginModule {

    /** The JACC PolicyContext key for the current Subject */
    private static final String WEB_REQUEST_KEY = 
            "javax.servlet.http.HttpServletRequest";
    
    private static final Logger auditlog = Logger.getLogger("audit");
    private static final Logger log = Logger.getLogger(AuditLoginModule.class);
    
    private CallbackHandler cbh;
    
    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    public void initialize(Subject subject, CallbackHandler cbh,
            Map sharedState, Map options) {
        this.cbh = cbh;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#login()
     */
    public boolean login() throws LoginException {
        return true;
    }
    
    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    public boolean commit() throws LoginException {
        auditlog.info(new UserAuthenticationMessage(
                new UserAuthenticationMessage.AuditEvent.Login(),
                getUserWithLocation()));
        return true;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    public boolean logout() throws LoginException {
        auditlog.info(new UserAuthenticationMessage(
                new UserAuthenticationMessage.AuditEvent.Logout(),
                getUserWithLocation()));
        return true;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    public boolean abort() throws LoginException {
        UserAuthenticationMessage.AuditEvent event = 
                new UserAuthenticationMessage.AuditEvent.Login();
        event.setEventOutcomeIndicator(OutcomeIndicator.MINOR_FAILURE);
        auditlog.warn(
                new UserAuthenticationMessage(event, getUserWithLocation()));
        return true;
    } 
   
    private UserWithLocation getUserWithLocation() {
        return new UserWithLocation(getUserID(), getNetworkAccessPoint());
    }
    
    private String getUserID() {
        NameCallback nc = new NameCallback("prompt");
        try {
            cbh.handle(new Callback[] {nc});
        } catch (Exception e) {
            log.error("Failed to access User ID:", e);
            return "???";
        }
        return nc.getName();
    }

    private NetworkAccessPoint getNetworkAccessPoint() {
        HttpServletRequest request;
        try {
            request = (HttpServletRequest) 
                    PolicyContext.getContext(WEB_REQUEST_KEY);
            String addr = request.getRemoteAddr();
            String host = request.getRemoteHost();
            return host.equals(addr) 
                    ? (NetworkAccessPoint) new NetworkAccessPoint.IPAddress(addr)
                    : (NetworkAccessPoint) new NetworkAccessPoint.HostName(host);
        } catch (PolicyContextException e) {
            log.error("Failed to determine Network Access Point ID:", e);
            return new NetworkAccessPoint.HostName("???");
        }
    }

}
