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
public final class QueryPasswordForUserCmd extends BaseReadCmd {

	public static final int transactionIsolationLevel = 0;

	public QueryPasswordForUserCmd(String dsJndiName) throws SQLException {
		super(dsJndiName, transactionIsolationLevel,
				JdbcProperties.getInstance().getProperty("QueryPasswordForUserCmd"));
	}

	public void setUser(String user) throws SQLException {
		((PreparedStatement) stmt).setString(1, user);		
	}

	public String getPassword() throws SQLException {
		return rs.getString(1);
	}

}
