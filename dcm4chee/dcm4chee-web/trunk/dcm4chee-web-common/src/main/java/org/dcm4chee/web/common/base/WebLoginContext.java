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

package org.dcm4chee.web.common.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.security.authentication.LoginException;
import org.apache.wicket.security.hive.authentication.DefaultSubject;
import org.apache.wicket.security.hive.authentication.UsernamePasswordContext;
import org.apache.wicket.security.hive.authorization.SimplePrincipal;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.dcm4chee.web.common.secure.SecureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 31.08.2010
 */
public class WebLoginContext extends UsernamePasswordContext {

    protected static Logger log = LoggerFactory.getLogger(BaseMBeanDelegate.class);
    
    private Roles roles = new Roles();

    public WebLoginContext(String username, String password) {
        super(username, password);
    }

    public Roles getRoles() {
        return roles;
    }

    @Override
    protected org.apache.wicket.security.hive.authentication.Subject getSubject(String username, String password) throws LoginException {

        WebApplication app = (WebApplication) RequestCycle.get().getApplication();
        String webApplicationPolicy = app.getInitParameter("webApplicationPolicy");
        if ( webApplicationPolicy == null) webApplicationPolicy = "dcm4chee";
        String rolesGroupName = app.getInitParameter("rolesGroupName");
        if ( rolesGroupName == null) rolesGroupName = "Roles";
        LoginCallbackHandler handler = new LoginCallbackHandler(username, password);
            LoginContext context;
            try {
                context = new LoginContext(webApplicationPolicy, handler);
                context.login();
                
                SecureSession secureSession = ((SecureSession) RequestCycle.get().getSession());
                secureSession.setRoot(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("root"));
//                secureSession.setUsername(username);
                secureSession.setDicomSubject(
                        getDicomSecuritySubject(
                                new ObjectName(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("DicomSecurityService")), 
                                username, 
                                password
                        )
                );
            } catch (Exception e) {
                log.error("Error: " + e.getMessage());
                throw new LoginException();
            }

            try {
                InputStream in = ((WebApplication) RequestCycle.get().getApplication()).getServletContext().getResource("/WEB-INF/dcm4chee.hive").openStream();
                BufferedReader dis = new BufferedReader (new InputStreamReader (in));

                HashMap<String, String> principals = new LinkedHashMap<String, String>();
                String line;
                String principal = null;
                while ((line = dis.readLine()) != null) 
                    if (line.startsWith("grant principal ")) {
                        principal = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                        principals.put(principal, null);
                    } else if ((principal != null) && (line.trim().startsWith("// DOC:"))) { 
                        principals.put(principal, line.substring(line.indexOf("// DOC:") + 7).trim());
                        principal = null;
                    }
                in.close();
                ((SecureSession) RequestCycle.get().getSession()).setSwarmPrincipals(principals);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                return null;
            }
            
            DefaultSubject subject = new DefaultSubject();
            for (Principal principal : context.getSubject().getPrincipals()) {
                if ((principal instanceof Group) && (rolesGroupName.equalsIgnoreCase(principal.getName()))) {
                    Enumeration<? extends Principal> members = ((Group) principal).members();
                    
                    Set<String> swarmPrincipals = new HashSet<String>();
                    while (members.hasMoreElements()) {
                        Principal member = members.nextElement();
                        subject.addPrincipal(new SimplePrincipal(member.getName()));
                        swarmPrincipals.add(member.getName());
                    }
                }
            }

            // TODO: put this in some properties file or config service
          if (!subject.getPrincipals().contains(new SimplePrincipal("LoginAllowed"))) 
              ((SecureSession) RequestCycle.get().getSession()).invalidate();
          return subject;
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
    
    public javax.security.auth.Subject getDicomSecuritySubject(ObjectName serviceName, String userId, String passwd) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        javax.security.auth.Subject subject = new javax.security.auth.Subject();
        List<?> servers = MBeanServerFactory.findMBeanServer(null);
        MBeanServerConnection server = (MBeanServerConnection) servers.get(0);
        server.invoke(
                serviceName, "isValid",
                new Object[] { userId, passwd, subject },
                new String[] { String.class.getName(), 
                        String.class.getName(), javax.security.auth.Subject.class.getName()});
        return subject;
    }
}