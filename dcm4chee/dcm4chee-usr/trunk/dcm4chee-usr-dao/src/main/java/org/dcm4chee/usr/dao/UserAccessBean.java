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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.dcm4chee.usr.entity.Role;
import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.entity.UserRoleAssignment;
import org.jboss.annotation.ejb.LocalBinding;
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

    @SuppressWarnings("unused")
    @PostConstruct
    private void initMBeanServer() {
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
            
            List<Role> roleList = getAllRolenames();
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
                    addRole(new Role(userRoleName, true));
                if (!haveAdminRoleName) 
                    addRole(new Role(adminRoleName, true));
            } else {
                addRole(new Role(userRoleName, true));
                addRole(new Role(adminRoleName, true));
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

    // TODO: change this to generic version using JPA 2.0 implementation
    @SuppressWarnings("unchecked")
    public List<Role> getAllRolenames() {
        return this.em.createQuery("SELECT DISTINCT r FROM Role r ORDER BY r.isSystemRole DESC, r.rolename")
        .getResultList();        
    }

    public void addRole(Role role) {
        this.em.persist(role);
    }

    public void updateRole(Role role) {
        this.em.createQuery("UPDATE UserRoleAssignment ura SET ura.role = :newRolename WHERE ura.role = :oldRolename")
        .setParameter("oldRolename", this.em.find(Role.class, role.getPk()).getRolename())
        .setParameter("newRolename", role.getRolename())
        .executeUpdate();
        this.em.merge(role);
    }

    public void removeRole(Role role) {
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.role = :rolename")
        .setParameter("rolename", role.getRolename())
        .executeUpdate();
        this.em.createQuery("DELETE FROM Role r WHERE r.rolename = :rolename")
        .setParameter("rolename", role.getRolename())
        .executeUpdate();
    }

    public Boolean roleExists(String rolename) {
        try {
            this.em.createQuery("SELECT DISTINCT r FROM Role r WHERE r.rolename = :rolename")
            .setParameter("rolename", rolename)
            .getSingleResult();
            return true;
        } catch (NoResultException nre) {
            return false;
        }
    }    
}
