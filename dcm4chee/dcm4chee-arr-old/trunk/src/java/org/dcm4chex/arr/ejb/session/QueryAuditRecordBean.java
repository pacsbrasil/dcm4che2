/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4chex.arr.ejb.session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import org.dcm4chex.arr.ejb.entity.AuditRecordLocalHome;

/**
 *  QueryAuditRecord bean.
 *
 * @ejb:bean
 *  name="QueryAuditRecord"
 *  type="Stateless"
 *  view-type="local"
 *  local-jndi-name="ejb/QueryAuditRecord"
 * 
 * @ejb:transaction
 *  type="NotSupported"
 * 
 * @ejb:transaction-type
 *  type="Container"
 * 
 * @ejb:ejb-ref
 *  ref-name="ejb/AuditRecord"
 *  view-type="local"
 *  ejb-name="AuditRecord"
 * 
 * @ejb.env-entry 
 * 	name="Database"
 * 	type="java.lang.String"
 *  value="Hypersonic SQL"
 * 
 * @ejb:resource-ref
 *  res-name="jdbc/DefaultDS"
 *  res-type="javax.sql.DataSource"
 *  res-auth="Container"
 * 
 * @jboss:resource-ref 
 *  res-ref-name="jdbc/DefaultDS"
 *  resource-name="java:/DefaultDS"
 * 
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @created  February 15, 2003
 * @version  $Revision$ $Date$
 */
public abstract class QueryAuditRecordBean implements SessionBean {

    static final Logger log = Logger.getLogger(QueryAuditRecordBean.class);

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

    static final String[] DATABASE = { "Hypersonic SQL", "PostgreSQL 7.2",
            "DB2", "Oracle9i"};

    static final int HSQL = 0;

    static final int PSQL = 1;

    static final int DB2 = 2;

    static final int ORACLE = 3;

    private AuditRecordLocalHome home;

    private Connection con = null;

    private Statement stmt = null;

    private int database = -1;

    /**
     *  Sets the sessionContext attribute of the QueryAuditRecordBean object
     *
     * @param  sessionContext The new sessionContext value
     */
    public void setSessionContext(SessionContext sessionContext) {
        DataSource ds = null;
        try {
            Context jndiCtx = new InitialContext();
            home = (AuditRecordLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/AuditRecord");
            ds = (DataSource) jndiCtx.lookup("java:comp/env/jdbc/DefaultDS");
            String envVal = (String) jndiCtx.lookup("java:comp/env/Database");
            database = Arrays.asList(DATABASE).indexOf(envVal);
            if (database == -1) { throw new EJBException(
                    "Undefined Database Type: " + envVal); }
        } catch (NamingException e) {
            log.error("Failed lookup ns:", e);
            throw new EJBException(e);
        }
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
        } catch (SQLException e) {
            log.error("Failed to connect to db:", e);
            unsetSessionContext();
            throw new EJBException(e);
        }
    }

    /**
     *  Description of the Method
     */
    public void unsetSessionContext() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * @param  pk Description of the Parameter
     * @return  The timeStamp value
     * @ejb:interface-method
     */
    public Timestamp getTimestamp(int pk) {
        try {
            return home.findByPrimaryKey(new Integer(pk)).getTimestamp();
        } catch (FinderException e) {
            log.error("Failed to access AuditRecord with pk=" + pk, e);
            throw new EJBException(e);
        }
    }

    /**
     * @param  pk Description of the Parameter
     * @return  The Type value
     * @ejb:interface-method
     */
    public String getType(int pk) {
        try {
            return home.findByPrimaryKey(new Integer(pk)).getType();
        } catch (FinderException e) {
            log.error("Failed to access AuditRecord with pk=" + pk, e);
            throw new EJBException(e);
        }
    }

    /**
     * @param  pk Description of the Parameter
     * @return  The xmlData value
     * @ejb:interface-method
     */
    public String getXmlData(int pk) {
        try {
            return home.findByPrimaryKey(new Integer(pk)).getXmlData();
        } catch (FinderException e) {
            log.error("Failed to access AuditRecord with pk=" + pk, e);
            throw new EJBException(e);
        }
    }

    /**
     * @param  type Description of the Parameter
     * @param  host Description of the Parameter
     * @param  from Description of the Parameter
     * @param  to Description of the Parameter
     * @return  Description of the Return Value
     * @ejb:interface-method
     */
    public String query(String[] type, String host, String from, String to,
            String aet, String userName, String patientName, String patientId,
            int offset, int limit, String[] orderBy, String[] orderDir) {
        ResultSet rs = null;
        String sql = buildSQL(type, host, from, to, aet, userName, patientName,
                patientId, offset, limit, orderBy, orderDir);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Execute Query: " + sql);
            }
            ResultSet countrs = stmt.executeQuery(buildCountSQL(type, host, from,
                    to, aet, userName, patientName, patientId));
            countrs.next();
            int ntot = countrs.getInt(1);
            rs = stmt.executeQuery(sql);
            return toXML(type, host, from, to, aet, userName, patientName,
                    patientId, orderBy, orderDir, rs, offset, limit, ntot);
        } catch (SQLException e) {
            log.error("Failed to query db:", e);
            throw new EJBException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private final static String COLUMNS = "pk, msg_type, host_name, time_stamp, aet, user_name, patient_name, patient_id";

    private String buildCountSQL(String[] type, String host, String from,
            String to, String aet, String userName, String patientName,
            String patientId) {
        return appendFromWhere(new StringBuffer("SELECT COUNT(pk)"), type,
                host, from, to, aet, userName, patientName, patientId)
                .toString();
    }

    private String buildSQL(String[] type, String host, String from, String to,
            String aet, String userName, String patientName, String patientId,
            int offset, int limit, String[] orderBy, String[] orderDir) {
        StringBuffer sb = new StringBuffer("SELECT ");
        switch (database) {
        case HSQL:
            sb.append("LIMIT ");
            sb.append(offset);
            sb.append(" ");
            sb.append(limit);
            sb.append(" ");
            sb.append(COLUMNS);
            break;
        case DB2:
            sb.append("* FROM ( SELECT ");
            sb.append(COLUMNS);
            sb.append(", ROW_NUMBER() OVER (");
            appendOrderBy(sb, orderBy, orderDir);
            sb.append(") AS rownum ");
            break;
        case ORACLE:
            sb.append("* FROM ( SELECT ");
            sb.append(COLUMNS);
            sb.append(", ROWNUM as r1 FROM ( SELECT ");
            sb.append(COLUMNS);
            break;
        default:
            sb.append(COLUMNS);
            break;
        }
        appendFromWhere(sb, type, host, from, to, aet, userName, patientName,
                patientId);
        appendOrderBy(sb, orderBy, orderDir);
        switch (database) {
        case PSQL:
            sb.append(" OFFSET ");
            sb.append(offset);
            sb.append(" LIMIT ");
            sb.append(limit);
            break;
        case DB2:
            sb.append(" ) AS foo WHERE rownum > ");
            sb.append(offset);
            sb.append(" AND rownum <= ");
            sb.append(offset + limit);
            break;
        case ORACLE:
            sb.append(" ) WHERE ROWNUM <= ");
            sb.append(offset + limit);
            sb.append(" ) WHERE ROWNUM r1 > ");
            sb.append(offset);
            break;
        }
        return sb.toString();
    }

    private StringBuffer appendOrderBy(StringBuffer sb, String[] orderBy,
            String[] orderDir) {
        if (orderBy != null) {
            for (int i = 0; i < orderBy.length; i++) {
                sb.append(i == 0 ? "ORDER BY " : ", ").append(orderBy[i])
                        .append(" ").append(orderDir[i]);
            }
        }
        return sb;
    }

    private StringBuffer appendFromWhere(StringBuffer sb, String[] type,
            String host, String from, String to, String aet, String userName,
            String patientName, String patientId) {
        sb.append(" FROM audit_record WHERE ");
        if (type != null && type.length > 0) {
            sb.append('(');
            for (int i = 0; i < type.length; i++) {
                sb.append("audit_record.msg_type='").append(
                        type[i].replaceAll("'", "''")).append("' OR ");
            }
            sb.setLength(sb.length() - 4);
            sb.append(") AND");
        }
        if (host.length() != 0) {
            sb.append(" UPPER(host_name)=UPPER('").append(
                    host.replaceAll("'", "''")).append("') AND");
        }
        boolean matchFrom = from.length() != 0;
        boolean matchTo = to.length() != 0;
        if (matchFrom && matchTo) {
            sb.append(" time_stamp BETWEEN '").append(
                    from.replaceAll("'", "''")).append("' AND '").append(
                    to.replaceAll("'", "''")).append("' AND ");
        } else if (matchFrom) {
            sb.append(" time_stamp>='").append(from.replaceAll("'", "''"))
                    .append("' AND ");
        } else if (matchTo) {
            sb.append(" time_stamp<='").append(to.replaceAll("'", "''"))
                    .append("' AND ");
        }
        if (aet.length() != 0) {
            sb.append(" aet='").append(aet.replaceAll("'", "''")).append(
                    "' AND ");
        }
        if (userName.length() != 0) {
            sb.append(" user_name='").append(userName.replaceAll("'", "''"))
                    .append("' AND ");
        }
        if (patientName.length() != 0) {
            sb.append(" UPPER(patient_name)=UPPER('").append(
                    patientName.replaceAll("'", "''")).append("') AND ");
        }
        if (patientId.length() != 0) {
            sb.append(" patient_id='").append(patientId.replaceAll("'", "''"))
                    .append("' AND ");
        }
        sb.setLength(sb.length()
                - ((sb.charAt(sb.length() - 2) == 'D') ? 5 : 7));
        return sb;
    }

    private String maskNull(String s) {
        return s != null ? s.trim() : "";
    }

    private String toXML(String[] type, String host, String from, String to,
            String aet, String userName, String patientName, String patientId,
            String[] orderBy, String[] orderDir, ResultSet rs, int start,
            int pageSize, int ntot) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("<query type=\"");
        if (type != null) {
            for (int i = 0; i < type.length; i++)
                sb.append(type[i]).append(",");
            sb.setLength(sb.length() - 1);
        }
        String orderDirType = "", orderDirHost = "", orderDirTimestamp = "";
        if (orderBy != null) {
            for (int i = 0; i < orderBy.length; i++) {
                if (orderBy[i].equalsIgnoreCase("msg_type")) {
                    orderDirType = orderDir[i];
                } else if (orderBy[i].equalsIgnoreCase("host_name")) {
                    orderDirHost = orderDir[i];
                } else if (orderBy[i].equalsIgnoreCase("time_stamp")) {
                    orderDirTimestamp = orderDir[i];
                }
            }
        }
        sb.append("\" host=\"").append(host).append("\" from=\"").append(from)
                .append("\" to=\"").append(to).append("\" start=\"").append(
                        start).append("\" pagesize=\"").append(pageSize)
                .append("\" aet=\"").append(aet).append("\" username=\"")
                .append(userName).append("\" patientname=\"").append(
                        patientName).append("\" patientid=\"")
                .append(patientId).append("\" sortdir-type=\"").append(
                        orderDirType).append("\" sortdir-host=\"").append(
                        orderDirHost).append("\" sortdir-timestamp=\"").append(
                        orderDirTimestamp);
        int n = 0; //to count number of records in this fetch
        StringBuffer sbRecs = new StringBuffer();
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        while (rs.next()) {
            sbRecs.append("\n<record pk=\"").append(rs.getInt(1));
            sbRecs.append("\" type=\"").append(maskNull(rs.getString(2)));
            sbRecs.append("\" host=\"").append(maskNull(rs.getString(3)));
            sbRecs.append("\" timestamp=\"").append(
                    maskNull(df.format(rs.getTimestamp(4))));
            sbRecs.append("\" aet=\"").append(maskNull(rs.getString(5)));
            sbRecs.append("\" username=\"").append(maskNull(rs.getString(6)));
            sbRecs.append("\" patientname=\"")
                    .append(maskNull(rs.getString(7)));
            sbRecs.append("\" patientid=\"").append(maskNull(rs.getString(8)));
            sbRecs.append("\" />");
            ++n;
        }
        sb.append("\" nrecords=\"").append(n);
        sb.append("\" tot-nrecords=\"").append(ntot);
        sb.append("\" eof=\"").append(start + pageSize >= ntot).append("\">");
        sb.append(sbRecs);
        sb.append("\n</query>");
        if (log.isDebugEnabled()) {
            log.debug("Found " + n + " matching Audit Records:\n"
                    + sb.toString());
        }
        return sb.toString();
    }

}
