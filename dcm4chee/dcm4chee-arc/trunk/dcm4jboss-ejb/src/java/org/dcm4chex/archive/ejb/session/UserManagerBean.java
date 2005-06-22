/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;

import org.dcm4chex.archive.ejb.jdbc.AddRoleToUserCmd;
import org.dcm4chex.archive.ejb.jdbc.AddUserCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryPasswordForUserCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryRolesForUserCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryUsersCmd;
import org.dcm4chex.archive.ejb.jdbc.RemoveRoleFromUserCmd;
import org.dcm4chex.archive.ejb.jdbc.RemoveUserCmd;
import org.dcm4chex.archive.ejb.jdbc.UpdatePasswordForUserCmd;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Jun 22, 2005
 * 
 * @ejb.bean name="UserManager" 
 *           type="Stateless"
 *           view-type="remote" 
 * 	         jndi-name="ejb/UserManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb:resource-ref res-name="jdbc/DS"
 * 	                 res-type="javax.sql.DataSource"
 * 					 res-auth="Container"
 * @jboss:resource-ref res-ref-name="jdbc/DS" 
 *                     jndi-name="java:/DefaultDS"
 *
 */
public abstract class UserManagerBean
		implements SessionBean {
		
	/**
     * @ejb.interface-method
     */
	public Collection getUsers() {
		try {
			QueryUsersCmd cmd = new QueryUsersCmd("jdbc/DS");
			try {
				cmd.execute();
				ArrayList users = new ArrayList();
				while (cmd.next()) {
					users.add(cmd.getUser());
				}
				return users;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public void addUser(String user, String passwd) {		
		try {
			AddUserCmd cmd = new AddUserCmd("jdbc/DataSource");
			try {
				cmd.setUser(user);
				cmd.setPassword(passwd);
				cmd.execute();
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public boolean removeUser(String user) {		
		try {
			RemoveUserCmd cmd = new RemoveUserCmd("jdbc/DataSource");
			try {
				cmd.setUser(user);
				return cmd.execute() != 0;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public String getPasswordForUser(String user) {
		try {
			QueryPasswordForUserCmd cmd = new QueryPasswordForUserCmd("jdbc/DataSource");
			try {
				cmd.setUser(user);
				cmd.execute();
				return cmd.next() ? cmd.getPassword() : null;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public boolean setPasswordForUser(String user, String passwd) {
		try {
			UpdatePasswordForUserCmd cmd = new UpdatePasswordForUserCmd("jdbc/DataSource");
			try {
				cmd.setUser(user);
				cmd.setPassword(passwd);
				return cmd.execute() != 0;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public Collection getRolesOfUser(String user) {
		try {
			QueryRolesForUserCmd cmd = new QueryRolesForUserCmd("jdbc/DataSource");
			try {
				cmd.setUser(user);
				cmd.execute();
				ArrayList roles = new ArrayList();
				while (cmd.next()) {
					roles.add(cmd.getRole());
				}
				return roles;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}
	
	/**
     * @ejb.interface-method
     */
	public void addRoleToUser(String user, String role) {
		try {
			AddRoleToUserCmd cmd = new AddRoleToUserCmd("jdbc/DataSource");
			try {
				cmd.setUser(user);
				cmd.setRole(role);
				cmd.execute();
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public boolean removeRoleFromUser(String user, String role) {
		try {
			RemoveRoleFromUserCmd cmd = new RemoveRoleFromUserCmd("jdbc/DataSource");
			try {
				cmd.setUser(user);
				cmd.setRole(role);
				return cmd.execute() != 0;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}
	
}
