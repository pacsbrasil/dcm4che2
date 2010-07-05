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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.entity.UserRoleAssignment;
import org.dcm4chee.usr.sar.UserAccessService;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.system.ServiceMBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 19.08.2009
 */
@Stateless
@LocalBinding(jndiBinding=UserAccess.JNDI_NAME)
public class UserAccessBean implements UserAccess {

    private static Logger log = LoggerFactory.getLogger(UserAccessBean.class);
    
    @PersistenceContext(unitName="dcm4chee-usr")
    private EntityManager em;

    private ObjectName serviceObjectName;
//    private final MBeanServer server = MBeanServerLocator.locate();
    private MBeanServerConnection server = null;

    private String userRoleName;
    private String adminRoleName;

    @SuppressWarnings("unused")
    @PostConstruct
    private void initMBeanServer() {
        if (server == null) {
            List<?> servers = MBeanServerFactory.findMBeanServer(null);
            if (servers != null && !servers.isEmpty()) {
                this.server = (MBeanServerConnection) servers.get(0);
                log.debug("Found MBeanServer:"+server);
            } else {
                log.error("Failed to get MBeanServerConnection! MbeanDelegate class:"+getClass().getName());
                return;
            }
        }   
    }
    
    public void init(String serviceObjectName) {

        BufferedReader reader = null;
        try {
            this.serviceObjectName = new ObjectName(serviceObjectName);
            userRoleName = 
                (String) this.server.getAttribute(
                    this.serviceObjectName, 
                    "userRoleName");

            adminRoleName = 
                (String) this.server.getAttribute(
                    this.serviceObjectName, 
                    "adminRoleName");
        
            List<Role> roleList = new ArrayList<Role>();

            if (new File(this.getRoleFilename()).exists()) {
                String line;
                reader = new BufferedReader(new FileReader(this.getRoleFilename()));
                while ((line = reader.readLine()) != null)
                    roleList.add((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class));

                if (roleList.size() > 0) {
                    boolean haveUserRoleName = false;
                    boolean haveAdminRoleName = false;
                    
                    for (Role role : roleList) {
                        if (!role.isSystemRole()) break;                       
                        if (role.getRolename().equals(userRoleName))
                            haveUserRoleName = true;
                        if (role.getRolename().equals(adminRoleName))
                            haveAdminRoleName = true;
                    }
                    if (!haveUserRoleName) addRole(new Role(userRoleName, true));
                    if (!haveAdminRoleName) addRole(new Role(adminRoleName, true));
                    return;
                }
            }
            addRole(new Role(userRoleName, true));
            addRole(new Role(adminRoleName, true));
        } catch (Exception e) {
            log.debug("Exception: ", e);
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {}
        }           
    }

    public String getUserRoleName() {
        return this.userRoleName;
    }

    public String getAdminRoleName() {
        return this.adminRoleName;
    }

    public String getRoleFilename() {
        try {
            return (String) this.server.getAttribute(
                    this.serviceObjectName, 
                    "roleFilename");
        } catch (Exception e) {
            log.debug("Exception: ", e);
        }
        return null;
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

    // TODO: change this to generic version using JPA 2.0 implementation
    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        return this.em.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles ORDER BY u.userID")
        .getResultList();
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
    
    
    public List<Role> getAllRolenames() {
        List<Role> roleList = new ArrayList<Role>();
        BufferedReader reader = null;
        try {
            if (new File(this.getRoleFilename()).exists()) {
                String line;
                reader = new BufferedReader(new FileReader(this.getRoleFilename()));
                while ((line = reader.readLine()) != null)
                    roleList.add((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class));
            }
        } catch (FileNotFoundException e) {
            log.debug("Exception: ", e);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {}
        }
        return roleList;
    }

    public void addRole(String rolename) {
        BufferedWriter writer = null; 
        try {
            writer = new BufferedWriter(new FileWriter(this.getRoleFilename(), true));
            JSONObject jsonObject = JSONObject.fromObject(new Role(rolename));
            if (rolename == null) jsonObject.put("rolename", JSONNull.getInstance());
            writer.write(jsonObject.toString());
            writer.newLine();          
        } catch (FileNotFoundException e) {
            log.debug("Exception: ", e);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        } finally {
            try {
                writer.close();
            } catch (Exception ignore) {}
        }
        sort();
    }

    public void addRole(Role role) {
        BufferedWriter writer = null; 
        try {
            if (roleExists(role.getRolename())) return;
            writer = new BufferedWriter(new FileWriter(this.getRoleFilename(), true));
            JSONObject jsonObject = JSONObject.fromObject(role);
            if (role.getRolename() == null) jsonObject.put("rolename", JSONNull.getInstance());
            writer.write(jsonObject.toString());
            writer.newLine();          
        } catch (FileNotFoundException e) {
            log.debug("Exception: ", e);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        } finally {
            try {
                writer.close();
            } catch (Exception ignore) {}
        }
        sort();
    }

    public void removeRole(Role role) {
        modifyRole(role, true);
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.role = :rolename")
        .setParameter("rolename", role.getRolename())
        .executeUpdate();       
    }

    private void modifyRole(Role role, boolean delete) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(this.getRoleFilename()));
            File roleFile = new File(this.getRoleFilename());            
            String tempFilename = roleFile.getAbsolutePath().substring(0, roleFile.getAbsolutePath().length() - roleFile.getName().length()) 
                                + UUID.randomUUID().toString();
            writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class)).getRolename().equals(role.getRolename())) {
                  writer.write(line);
                  writer.newLine();
                } else {
                    if (!delete) {
                        JSONObject jsonObject = JSONObject.fromObject(role);
                        if (role.getRolename() == null) jsonObject.put("rolename", JSONNull.getInstance());
                        writer.write(jsonObject.toString());
                        writer.newLine();
                    }
                }
            }
            reader.close();
            writer.close();
            roleFile.delete();
            new File(tempFilename).renameTo(roleFile);
        } catch (FileNotFoundException e) {
            log.debug("Exception: ", e);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        } finally {
            try {
                reader.close();
                writer.close();
            } catch (Exception ignore) {}
        }
        sort();
    }

    private void sort() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(this.getRoleFilename()));
            File reportFile = new File(this.getRoleFilename());
            String tempFilename = reportFile.getAbsolutePath().substring(0, reportFile.getAbsolutePath().length() - reportFile.getName().length()) 
                                + UUID.randomUUID().toString();
            writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            
            List<Role> systemRoles = new ArrayList<Role>();
            List<Role> roles = new ArrayList<Role>();
            while ((line = reader.readLine()) != null) {
                Role role = (Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class);
                if (role.isSystemRole()) systemRoles.add(role);
                else roles.add((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class));
            }
            
            Comparator<Role> comparator = new Comparator<Role>() {
                @Override
                public int compare(Role r1, Role r2) {
                    return (r1.getRolename().toUpperCase().compareTo(r2.getRolename().toUpperCase()));
                }
            };
            Collections.sort(systemRoles, comparator);
            Collections.sort(roles, comparator);
            
            roles.addAll(0, systemRoles);
            for (Role role : roles) {
                JSONObject jsonObject = JSONObject.fromObject(role);
                writer.write(jsonObject.toString());
                writer.newLine();
            }
            reader.close();
            writer.close();
            reportFile.delete();
            new File(tempFilename).renameTo(reportFile);
        } catch (FileNotFoundException e) {
            log.debug("Exception: ", e);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        } finally {
            try {
                reader.close();
                writer.close();
            } catch (Exception ignore) {}
        }
    }
    
    public Boolean roleExists(String rolename) {
        BufferedReader reader = null;
        try {
            if (new File(this.getRoleFilename()).exists()) {
                String line;
                reader = new BufferedReader(new FileReader(this.getRoleFilename()));
                while ((line = reader.readLine()) != null) 
                    if (((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class)).getRolename().equals(rolename)) 
                        return true;
            }
        } catch (FileNotFoundException e) {
            log.debug("Exception: ", e);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {}
        }
        return false;
    }    
}
