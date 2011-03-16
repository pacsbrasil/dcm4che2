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
import java.io.FileNotFoundException;
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
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import net.sf.json.JSONObject;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.security.authentication.LoginException;
import org.apache.wicket.security.hive.authentication.DefaultSubject;
import org.apache.wicket.security.hive.authentication.LoginContext;
import org.apache.wicket.security.hive.authorization.SimplePrincipal;
import org.dcm4chee.web.common.delegate.BaseCfgDelegate;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.dcm4chee.web.common.secure.SecureSession;
import org.jboss.system.server.ServerConfigLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug. 31, 2010
 */
public class SSOLoginContext extends LoginContext {

    protected static Logger log = LoggerFactory.getLogger(BaseMBeanDelegate.class);

    SecureSession session;
    private Subject jaasSubject;
    
    public SSOLoginContext() {
        
    }
    public SSOLoginContext(SecureSession secureSession, Subject jaasSubject) {
        this.session = secureSession;
        this.jaasSubject = jaasSubject;
    }

    @Override
    public final org.apache.wicket.security.hive.authentication.Subject login() throws LoginException
    {
            if (jaasSubject == null)
                    throw new LoginException("Insufficient information to login");
            return getSubject(jaasSubject);
    }
    
    protected org.apache.wicket.security.hive.authentication.Subject getSubject(Subject jaasSubject) throws LoginException {

        WebApplication app = (WebApplication) RequestCycle.get().getApplication();
        String webApplicationPolicy = app.getInitParameter("webApplicationPolicy");
        if (webApplicationPolicy == null) webApplicationPolicy = "dcm4chee";
        String rolesGroupName = app.getInitParameter("rolesGroupName");
        if (rolesGroupName == null) rolesGroupName = "Roles";
        if (session == null) {
            try {
                session = ((SecureSession) RequestCycle.get().getSession());
            } catch (Exception e) {
                log.warn("SSO Login failed. Reason: " + e.getMessage());
                throw new LoginException();
            }
        }
        session.setManageUsers(BaseCfgDelegate.getInstance().getManageUsers());
        if (!readHiveFile())
            return null;

        DefaultSubject subject;
        try {
            subject = toSwarmSubject(jaasSubject, session, rolesGroupName);
            checkLoginAllowed(subject);
            session.extendedLogin(subject);
        } catch (Exception e) {
            log.error("Login failed for JAAS subject: "+jaasSubject, e);
            session.invalidate();
            subject = new DefaultSubject();
        }
        return subject;
    }

    private boolean readHiveFile() {
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
            session.setSwarmPrincipals(principals);
            return true;
        } catch (Exception e) {
            log.error("Exception (error processing hive file): " + e.getMessage());
            session.invalidate();
            return false ;
        }
    }

    private DefaultSubject toSwarmSubject(Subject jaasSubject, SecureSession session, String rolesGroupName) throws IOException {
        DefaultSubject subject = new DefaultSubject();
        Map<String, Set<String>> mappings = null;
        Set<String> swarmPrincipals = new HashSet<String>();
        for (Principal principal : jaasSubject.getPrincipals()) {
            if (!(principal instanceof Group)) 
                session.setUsername(principal.getName());
            if ((principal instanceof Group) && (rolesGroupName.equalsIgnoreCase(principal.getName()))) {
                Enumeration<? extends Principal> members = ((Group) principal).members();                    
                if (mappings == null) {
                    mappings = readRolesFile();
                }
                Set<String> set;
                while (members.hasMoreElements()) {
                    Principal member = members.nextElement();
                    if ((set = mappings.get(member.getName()) ) != null) {
                        for (Iterator<String> i = set.iterator() ; i.hasNext() ;) {
                            String appRole = i.next();
                            if (swarmPrincipals.add(appRole))
                                subject.addPrincipal(new SimplePrincipal(appRole));
                        }
                    }
                }
            }
        }
        return subject;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Set<String>> readRolesFile() throws IOException {
        String fn = System.getProperty("dcm4chee-usr.cfg.roles-filename");
        if (fn == null) {
            throw new FileNotFoundException("Roles file not found! Not specified with System property 'dcm4chee-usr.cfg.roles-filename'");
        }
        File mappingFile = new File(fn);
        if (!mappingFile.isAbsolute())
            mappingFile = new File(ServerConfigLocator.locate().getServerHomeDir(), mappingFile.getPath());
        Map<String, Set<String>> mappings = new HashMap<String, Set<String>>();
        String line;
        BufferedReader reader = null;
        try { 
            reader = new BufferedReader(new FileReader(mappingFile));
            while ((line = reader.readLine()) != null) {
                JSONObject jsonObject = JSONObject.fromObject(line);
                Set<String> set = new HashSet<String>();
                Iterator<String> i = jsonObject.getJSONArray("swarmPrincipals").iterator();
                while (i.hasNext())
                    set.add(i.next());
                mappings.put(jsonObject.getString("rolename"), set);
            }
            return mappings;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {}
            }
        }
    }

    private void checkLoginAllowed(DefaultSubject subject) {
        String rolename = BaseCfgDelegate.getInstance().getLoginAllowedRolename();
        if (!subject.getPrincipals().contains(new SimplePrincipal(rolename))) {
          session.invalidate();
          log.warn("Failed to authorize subject for login, denied. See 'LoginAllowed' rolename attribute in Web Config Service.");
        }
    }
}
