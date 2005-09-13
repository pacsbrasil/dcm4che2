/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Jun 22, 2005
 */
public final class QueryUsersCmd extends BaseReadCmd {

	public static final int transactionIsolationLevel = 0;

	public QueryUsersCmd(String dsJndiName) throws SQLException {
		super(dsJndiName, transactionIsolationLevel,
				JdbcProperties.getInstance().getProperty("QueryUsersCmd"));
 	}

	public String getUser() throws SQLException {
		return rs.getString(1);
	}

}
