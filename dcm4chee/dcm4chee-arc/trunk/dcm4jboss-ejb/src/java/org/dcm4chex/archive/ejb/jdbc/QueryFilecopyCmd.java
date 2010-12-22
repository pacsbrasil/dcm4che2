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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Oct 5, 2010
 */
public final class QueryFilecopyCmd extends BaseReadCmd {

    private static final int SELECT_LEN = 7;
    public static int transactionIsolationLevel = 0;
    
    private QueryFilecopyCmd(String sql, int fetchSize) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
                transactionIsolationLevel, sql);
        setFetchSize(fetchSize);
        try {
            close();
        } catch (Throwable t) {
            log.warn("Initial close failed:"+t.getLocalizedMessage());
        }
    }
    
    public static QueryFilecopyCmd getInstance( String sql, int limit, int fetchSize) throws SQLException {
        sql = prepareSql(sql, limit);
        return new QueryFilecopyCmd(sql, fetchSize < 0 ? limit : fetchSize);
    }
    
    public static String prepareSql(String sql, int limit) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sql = sql.trim();
        if ( sql.endsWith(";")) {
            sql = sql.substring(0, sql.length()-1);
        }
        sql = sql.replaceAll("\\s\\s+", " ");
        log.debug("Original SQL (formatted):"+sql);
        if (limit > 0 ) {
            sql = addSeriesPkCheck(sql);
            String sql1 = sql.toUpperCase();
            int pos0 = sql1.indexOf("DISTINCT");
            if (pos0 != -1) {
                sqlBuilder.setDistinct(true);
                pos0 += 9;
            } else {
                pos0 = SELECT_LEN;
            }
            int pos1 = sql1.indexOf("FROM");
            StringBuffer sb = new StringBuffer(sql.length()+30);
            sb.append(sql.substring(0, SELECT_LEN));
            sqlBuilder.setLimit(limit);
            String[] fields = toFields(sql.substring(pos0, pos1)); 
            sqlBuilder.setFieldNamesForSelect(fields);
            sqlBuilder.addOrderBy(fields[0], SqlBuilder.ASC);
            sqlBuilder.appendLimitbeforeFrom(sb);
            sb.append(' ');
            int pos2 = sql1.indexOf("FOR READ ONLY", pos1); //DB2?
            if (pos2 > 0) {
                sb.append(sql.substring(pos1, pos2));
                sqlBuilder.appendLimitAtEnd(sb);
                sb.append(' ').append(sql.substring(pos2));
            } else {
                sb.append(sql.substring(pos1));
                sqlBuilder.appendLimitAtEnd(sb);
            }
            log.debug("SQL with LIMIT:"+sb);
            return sb.toString();
        } else {
            return sql;
        }
    }
    
    private static String addSeriesPkCheck(String sql) {
        log.debug("Add SERIES.pk check to sql:"+sql);
        String sqlUC = sql.toUpperCase();
        int posFrom = sqlUC.indexOf("FROM");
        posFrom += 5;
        int posSeries = sqlUC.indexOf("SERIES", posFrom);
        if (posSeries == -1) {
            log.warn("SERIES table not found is SELECT statement! SERIES.pk check not added!");
            return sql;
        }
        int posWhere = sqlUC.indexOf("WHERE", posSeries);
        posWhere += 6;
        if (sqlUC.indexOf("PK > ?", posWhere) != -1) {
            log.info("'pk > ?' found in WHERE clause! Use this as SERIES.pk check!");
            return sql;
        }
        String alias = sql.substring(posSeries, posSeries+=6);
        while (sqlUC.charAt(posSeries)==' ') 
            posSeries++;
        if (sqlUC.charAt(posSeries++)=='A' && sqlUC.charAt(posSeries++)=='S') {
            posSeries++;
            int posEndAlias = posSeries;
            while (sqlUC.charAt(posEndAlias) != ',' && sqlUC.charAt(posEndAlias) != ' ')
                posEndAlias++;
            alias = sql.substring(posSeries, posEndAlias);
        }
        StringBuffer sb = new StringBuffer(sql.length()+alias.length()+10);
        sb.append(sql.substring(0, posWhere)).append(alias).append(".pk > ? AND ");
        if (sqlUC.lastIndexOf("ORDER BY") == -1) {
            int posDB2 = sqlUC.indexOf("FOR READ ONLY", posWhere);
            if (posDB2 == -1) {
                sb.append(sql.substring(posWhere)).append(" ORDER BY ").append(alias).append(".pk ");
            } else {
                sb.append(sql.subSequence(posWhere, posDB2)).append(" ORDER BY ")
                .append(alias).append(".pk ").append(sql.substring(posDB2));
            }
        } else {
            sb.append(sql.substring(posWhere));
        }
        log.debug("SQL with SERIES.pk check:"+sb);
        return sb.toString();
    }

    private static String[] toFields(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        String[] fields = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            fields[i++] = st.nextToken();
        }
        return fields;
    }

    public List<Long> getSeriesPKs(Long updatedBefore, long lastSeriesPk) throws SQLException {
        if (stmt == null) 
            open();
        if ( updatedBefore != null ) {
            int paraIdx = 1;
            if (new StringTokenizer(sql, "?").countTokens() > 2) {
                log.debug("Set parameter 1 (SERIES.pk > lastSeriesPk) to:"+lastSeriesPk);
                ((PreparedStatement) stmt).setLong(paraIdx++, lastSeriesPk);
            }
            if ( log.isDebugEnabled() )
                log.debug("Set parameter "+paraIdx+" (updatedBefore) to:"+updatedBefore+" Date:"+new Date(updatedBefore));
            ((PreparedStatement) stmt).setDate(paraIdx, new java.sql.Date(updatedBefore));
        } else if (log.isDebugEnabled()) {
            log.debug("Use of updatedBefore WHERE clause disabled! Dont set parameter of prepared statement");
        }
        execute();
        List<Long> seriesPKs = new ArrayList<Long>();
        try {
            while (next()) {
                seriesPKs.add(rs.getLong(1));
            }
        } catch (Exception x) {
            log.error("QueryFilecopyCmd failed!",x);
        } finally {
            try {
                close();
            } catch (Exception ignore) {
                log.warn("Error closing connection!");
            }
        }
        return seriesPKs;
    }
    
    public String getSQL() {
        return sql;
    }
}
