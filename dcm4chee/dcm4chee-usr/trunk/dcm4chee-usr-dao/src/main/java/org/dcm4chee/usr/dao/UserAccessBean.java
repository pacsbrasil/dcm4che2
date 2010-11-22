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

package org.dcm4chee.usr.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import net.sf.json.JSONObject;

import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.entity.UserRoleAssignment;
import org.dcm4chee.usr.model.Role;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.system.server.ServerConfigLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 19.08.2009
 */
@Stateful
@LocalBinding(jndiBinding=UserAccess.JNDI_NAME)
public class UserAccessBean implements UserAccess {

    private static Logger log = LoggerFactory.getLogger(UserAccessBean.class);
    
    @PersistenceContext(unitName="dcm4chee-usr")
    private EntityManager em;

    private ObjectName serviceObjectName;
    private MBeanServerConnection server = null;
    private String mappingFilename;
    
    @SuppressWarnings("unused")
    @PostConstruct
    private void initMBeanServerAndMappingFile() {

        if (this.server == null) {
            List<?> servers = MBeanServerFactory.findMBeanServer(null);
            if (servers != null && !servers.isEmpty()) {
                this.server = (MBeanServerConnection) servers.get(0);
                log.debug("Found MBeanServer:"+this.server);
            } else {
                log.error("Failed to get MBeanServerConnection! MbeanDelegate class:"+getClass().getName());
                return;
            }
        }   

        // TODO: put the filename into the config service
        mappingFilename = 
            ServerConfigLocator.locate().getServerConfigURL().getPath() + 
            Role.mappingFilename;
        
        File file = new File(mappingFilename);
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error(getClass().getName() + ": " + e.getLocalizedMessage());
            }
    }
    
    public void init(String serviceObjectName) {
        try {
            this.serviceObjectName = new ObjectName(serviceObjectName);
            String userRoleName = (String) this.server.getAttribute(
                this.serviceObjectName, 
                "userRoleName");
            String adminRoleName = (String) this.server.getAttribute(
                this.serviceObjectName, 
                "adminRoleName");
            
            List<Role> roleList = getAllRoles();
            if (roleList.size() > 0) {
                boolean haveUserRoleName = false;
                boolean haveAdminRoleName = false;
                
                for (Role role : roleList) {               
                    if (role.getRolename().equals(userRoleName))
                        haveUserRoleName = true;
                    if (role.getRolename().equals(adminRoleName))
                        haveAdminRoleName = true;
                }
                if (!haveUserRoleName) 
                    addRole(new Role(userRoleName));
                if (!haveAdminRoleName) 
                    addRole(new Role(adminRoleName));
            } else {
                addRole(new Role(userRoleName));
                addRole(new Role(adminRoleName));
            }
        } catch (Exception e) {
            log.debug("Exception: ", e);
        }
    }

    public String getUserRoleName() {
        try {
            return
                (String) this.server.getAttribute(
                    this.serviceObjectName, 
                    "userRoleName");
        } catch (Exception e) {
            log.debug("Exception: ", e);
            return null;
        }
    }

    public String getAdminRoleName() {
        try {
            return 
                (String) this.server.getAttribute(
                    this.serviceObjectName, 
                    "adminRoleName");
        } catch (Exception e) {
            log.debug("Exception: ", e);
            return null;
        }
    }

    // TODO: change this to generic version using JPA 2.0 implementation
    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() {
        return this.em.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles ORDER BY u.userID")
        .getResultList();
    }

    public User getUser(String userId) {
        return this.em.find(User.class, userId);
    }

    public void createUser(User user) {
        this.em.persist(user);
    }

    public void updateUser(String userId, String password) {
        User managedUser = this.em.find(User.class, userId);
        managedUser.setPassword(password);
    }
    
    public void deleteUser(String userId) {
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.userID = :userID")
        .setParameter("userID", userId)
        .executeUpdate();
        this.em.createQuery("DELETE FROM User u WHERE u.userID = :userID")
        .setParameter("userID", userId)
        .executeUpdate();
    }

    public Boolean userExists(String username) {
        try {
            this.em.createQuery("SELECT DISTINCT u FROM User u WHERE u.userID = :userID")
            .setParameter("userID", username)
            .getSingleResult();
            return true;
        } catch (NoResultException nre) {
            return false;
        }
    }

    public Boolean hasPassword(String username, String password) {
        try {
            this.em.createQuery("SELECT DISTINCT u FROM User u WHERE u.userID = :userID AND u.password = :password")
            .setParameter("userID", username)
            .setParameter("password", password)
            .getSingleResult();
            return true;
        } catch (NoResultException nre) {
            return false;
        }
    }

    public void assignRole(UserRoleAssignment ura) {
        this.em.persist(ura);
    }

    public void unassignRole(UserRoleAssignment ura) {
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.userID = :userID AND ura.role = :rolename")
        .setParameter("userID", ura.getUserID())
        .setParameter("rolename", ura.getRole())
        .executeUpdate();
    }

    public List<String> getAllRolenames() {
        try {
            List<String> rolenameList = new ArrayList<String>();
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(mappingFilename));
            while ((line = reader.readLine()) != null)
                rolenameList.add(((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class)).getRolename());
            return rolenameList;
        } catch (Exception e) {
            log.debug("Exception: ", e);
            return null;
        }
    }

    public List<Role> getAllRoles() {
        try {
            List<Role> roleList = new ArrayList<Role>();
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(mappingFilename));
            while ((line = reader.readLine()) != null)
                roleList.add((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class));
            return roleList;
        } catch (Exception e) {
            log.debug("Exception: ", e);
            return null;
        }
    }

    public void addRole(Role role) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(mappingFilename, true));
            JSONObject jsonObject = JSONObject.fromObject(role);
            writer.write(jsonObject.toString());
            writer.newLine();
            writer.close();
            sort(mappingFilename);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }
    }

    public void updateRole(Role role) {
        String oldRolename = null;
        for(Role current : getAllRoles())
            if (role.getUuid().equals(current.getUuid()))
                oldRolename = current.getRolename();
        if (oldRolename != null && !oldRolename.equals(role.getRolename()))
            this.em.createQuery("UPDATE UserRoleAssignment ura SET ura.role = :newRolename WHERE ura.role = :oldRolename")
            .setParameter("oldRolename", oldRolename)
            .setParameter("newRolename", role.getRolename())
            .executeUpdate();
        modifyRole(role, false);
    }

    public void removeRole(Role role) {
        this.em.createQuery("DELETE FROM StudyPermission sp WHERE sp.role = :rolename")
        .setParameter("rolename", role.getRolename())
        .executeUpdate();
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.role = :rolename")
        .setParameter("rolename", role.getRolename())
        .executeUpdate();
        modifyRole(role, true);
    }
    
    private void modifyRole(Role role, boolean delete) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mappingFilename));
            File mappingFile = new File(mappingFilename);
            String tempFilename = mappingFile.getAbsolutePath().substring(0, mappingFile.getAbsolutePath().length() - mappingFile.getName().length()) 
                                + UUID.randomUUID().toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class)).getRolename().equals(role.getRolename())) {
                  writer.write(line);
                  writer.newLine();
                } else {
                    if (!delete) {
                        JSONObject jsonObject = JSONObject.fromObject(role);
                        writer.write(jsonObject.toString());
                        writer.newLine();
                    }
                }
            }
            reader.close();
            writer.close();
            mappingFile.delete();
            new File(tempFilename).renameTo(mappingFile);
            sort(mappingFilename);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }
    }

    private void sort(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            File mappingFile = new File(filename);            
            String tempFilename = mappingFile.getAbsolutePath().substring(0, mappingFile.getAbsolutePath().length() - mappingFile.getName().length()) 
                                + UUID.randomUUID().toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            List<Role> roleList = new ArrayList<Role>();
            while ((line = reader.readLine()) != null) 
                roleList.add((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class));             
            Collections.sort(roleList);
            for (Role role : roleList) {
                JSONObject jsonObject = JSONObject.fromObject(role);
                writer.write(jsonObject.toString());
                writer.newLine();
            }
            reader.close();
            writer.close();
            mappingFile.delete();
            new File(tempFilename).renameTo(mappingFile);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }       
    }

    public Boolean roleExists(String rolename) {
        return getAllRoles().contains(new Role(rolename));
    }    
}
