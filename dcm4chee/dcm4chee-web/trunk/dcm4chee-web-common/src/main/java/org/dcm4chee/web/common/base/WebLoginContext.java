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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import net.sf.json.JSONObject;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.security.authentication.LoginException;
import org.apache.wicket.security.hive.authentication.DefaultSubject;
import org.apache.wicket.security.hive.authentication.UsernamePasswordContext;
import org.apache.wicket.security.hive.authorization.SimplePrincipal;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.dcm4chee.web.common.secure.SecureSession;
import org.jboss.system.server.ServerConfigLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 31.08.2010
 */
public class WebLoginContext extends UsernamePasswordContext {

    protected static Logger log = LoggerFactory.getLogger(BaseMBeanDelegate.class);
    
    public WebLoginContext(String username, String password) {
        super(username, password);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected org.apache.wicket.security.hive.authentication.Subject getSubject(String username, String password) throws LoginException {

        List<?> servers = MBeanServerFactory.findMBeanServer(null);
        MBeanServerConnection server = (MBeanServerConnection) servers.get(0);

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
            secureSession.setUsername(username);
            boolean useStudyPermissions = Boolean.parseBoolean(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("useStudyPermissions"));
            secureSession.setRoot(username.equals(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("root")));
            if (useStudyPermissions) {
                server.invoke(
                        new ObjectName(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("WebCfgServiceName")), 
                        "updateDicomRoles",
                        new Object[] {},
                        new String[] {}
                );                    
                server.invoke(
                        new ObjectName(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("DicomSecurityService")), 
                        "isValid",
                        new Object[] { username, password, new javax.security.auth.Subject() },
                        new String[] { String.class.getName(), 
                                String.class.getName(), 
                                javax.security.auth.Subject.class.getName()}
                );
            }
            secureSession.setUseStudyPermissions(useStudyPermissions);
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
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
            log.error("Exception (error processing hive file): " + e.getMessage());
            ((SecureSession) RequestCycle.get().getSession()).invalidate();
            return null;
        }
        
        DefaultSubject subject = new DefaultSubject();
        try {
            for (Principal principal : context.getSubject().getPrincipals()) {
                if ((principal instanceof Group) && (rolesGroupName.equalsIgnoreCase(principal.getName()))) {
                    Enumeration<? extends Principal> members = ((Group) principal).members();                    

                    String mappingFilename = 
                        ServerConfigLocator.locate().getServerConfigURL().getPath() +
                        (String) server.getAttribute(
                                new ObjectName(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("WebCfgServiceName")), 
                                "rolesMappingFilename"
                        );
                    File file = new File(mappingFilename);
                    if (!file.exists())
                        file.createNewFile();
                    Map<String, Set<String>> mappings = new HashMap<String, Set<String>>();
                    String line;
                    BufferedReader reader = new BufferedReader(new FileReader(mappingFilename));
                    while ((line = reader.readLine()) != null) {
                        JSONObject jsonObject = JSONObject.fromObject(line);
                        Set<String> set = new HashSet<String>();
                        Iterator<String> i = jsonObject.getJSONArray("swarmPrincipals").iterator();
                        while (i.hasNext())
                            set.add(i.next());
                        mappings.put(jsonObject.getString("rolename"), set);
                    }
                    Set<String> swarmPrincipals = new HashSet<String>();
                    while (members.hasMoreElements()) {
                        Principal member = members.nextElement();
                        if (mappings.containsKey(member.getName())) {
                            Iterator<String> i = mappings.get(member.getName()).iterator();
                            while (i.hasNext()) {
                                String appRole = i.next();
                                subject.addPrincipal(new SimplePrincipal(appRole));
                                swarmPrincipals.add(appRole);
                            }
                        }
                    }
                }
            }
            if (!subject.getPrincipals().contains(new SimplePrincipal(
                (String) server.getAttribute(
                        new ObjectName(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("WebCfgServiceName")),  
                        "loginAllowedRolename"
                )
            )))                              
              ((SecureSession) RequestCycle.get().getSession()).invalidate();
        } catch (Exception e) {
            log.error("Exception: ", e);
            ((SecureSession) RequestCycle.get().getSession()).invalidate();
        }
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
}
