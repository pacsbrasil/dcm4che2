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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.wicket;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.wicket.Request;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAAS based Wicket Session to use JBoss web security.
 */
public class JaasWicketSession extends WicketSession {

    public static final String ROLES_NAME = "Roles";

    /** The name of the application-policy (JBoss login-config.xml) */
    public static final String APPLICATION_POLICY_NAME = "dcm4chee";

    private final static Logger log = LoggerFactory.getLogger(JaasWicketSession.class);

    private Subject subject;
    private Roles roles = new Roles();

    @SuppressWarnings("deprecation")
    public JaasWicketSession(Request req) {
        super(req);
    }

    public boolean authenticate(String username, String password) {
        if (log.isDebugEnabled()) 
            log.debug("authenticate(" + username + ", " + password + ")");

        boolean authenticated = false;
        LoginCallbackHandler handler = new LoginCallbackHandler(username, password);
        try {
            LoginContext ctx = new LoginContext(APPLICATION_POLICY_NAME, handler);
            ctx.login();
            authenticated = true;
            subject = ctx.getSubject();

            for (Principal p : subject.getPrincipals()) {
                if (log.isDebugEnabled()) log.debug("Principal for " + username + ": " + p.toString());
                // Group is a subclass of Principal, and the members are the names of the roles
                if ((p instanceof Group) && (ROLES_NAME.equalsIgnoreCase(p.getName()))) {
                    Group g = (Group) p;
                    Enumeration<? extends Principal> members = g.members();
                    while (members.hasMoreElements()) {
                        Principal member = members.nextElement();
                        roles.add(member.getName());
                        if (log.isDebugEnabled()) log.debug("Added role user:"+ username + " role:" + member.getName());
                    }
                }
            }
        } catch (LoginException e) {
            authenticated = false;
        }
        return authenticated;
    }

    public Roles getRoles() {
        return roles;
    }

    private class LoginCallbackHandler implements CallbackHandler {

        private String user;
        private String passwd;

        public LoginCallbackHandler(String user, String passwd) {
            this.user = user;
            this.passwd = passwd;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback cb : callbacks) {
                if (cb instanceof NameCallback) {
                    ((NameCallback) cb).setName(user);
                } else if (cb instanceof PasswordCallback) {
                    ((PasswordCallback)cb).setPassword(passwd.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(cb, "Callback not supported! (only Name and Password Callback are supported)");
                }
            }
        }

    }
}
