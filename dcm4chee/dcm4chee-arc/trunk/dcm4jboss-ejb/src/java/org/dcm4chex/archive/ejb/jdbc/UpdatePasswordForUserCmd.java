/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Jun 22, 2005
 */
public final class UpdatePasswordForUserCmd extends BaseUpdateCmd {

	public static final int transactionIsolationLevel = 0;

	public UpdatePasswordForUserCmd(String dsJndiName) throws SQLException {
		super(dsJndiName, transactionIsolationLevel,
		JdbcProperties.getInstance().getProperty("UpdatePasswordForUserCmd"));
	}

	public void setUser(String user) throws SQLException {
		((PreparedStatement) stmt).setString(1, user);		
	}

	public void setPassword(String passwd) throws SQLException {
		((PreparedStatement) stmt).setString(2, passwd);		
	}
}
