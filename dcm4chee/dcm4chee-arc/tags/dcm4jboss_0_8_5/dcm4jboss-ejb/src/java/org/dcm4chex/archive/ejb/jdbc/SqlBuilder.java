/*
 * $Id$ Copyright (c)
 * 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.ejb.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @author Harald.Metterlein@heicare.com
 * @version $Revision$ $Date$
 * @since 26.08.2003
 */
class SqlBuilder {

    public static final boolean TYPE1 = false;
    public static final boolean TYPE2 = true;
    public static final String DESC = " DESC";
    public static final String ASC = " ASC";
    public static final String WHERE = " WHERE ";
    public static final String AND = " AND ";
    public static final String[] SELECT_COUNT = { "count(*)" };
    private String[] select;
    private String[] from;
    private String[] leftJoin;
    private String[] relations;
    private ArrayList matches = new ArrayList();
    private ArrayList orderby = new ArrayList();
    private int limit = 0;
    private int offset = 0;
    private String whereOrAnd = WHERE;

    private static int getDatabase() {
        return JdbcProperties.getInstance().getDatabase();
    }

    public void setSelect(String[] fields) {
        select = JdbcProperties.getInstance().getProperties(fields);
    }

    public void setSelectCount() {
        select = SELECT_COUNT;
    }

    public void setFrom(String[] entities) {
        JdbcProperties jp = JdbcProperties.getInstance();
        from = jp.getProperties(entities);
    }

    public void setLeftJoin(String[] leftJoin) {
        if (leftJoin == null) {
            this.leftJoin = null;
            return;
        }
        if (leftJoin.length != 3) {
            throw new IllegalArgumentException("" + Arrays.asList(leftJoin));
        }
        this.leftJoin = JdbcProperties.getInstance().getProperties(leftJoin);
    }

    public void addOrderBy(String field, String order) {
        orderby.add(JdbcProperties.getInstance().getProperty(field) + order);
    }

    public final void setLimit(int limit) {
        this.limit = Math.max(0, limit);
    }

    public final void setOffset(int offset) {
        this.offset = Math.max(0, offset);
    }

    public void setRelations(String[] relations) {
        if (relations == null) {
            this.relations = null;
            return;
        }
        if ((relations.length & 1) != 0) {
            throw new IllegalArgumentException(
                "relations[" + relations.length + "]");
        }
        this.relations = JdbcProperties.getInstance().getProperties(relations);
    }

    private void addMatch(Match match) {
        if (!match.isUniveralMatch())
            matches.add(match);
    }

    public void addSingleValueMatch(
        String field,
        boolean type2,
        String value) {
        addMatch(new Match.SingleValue(field, type2, value));
    }

    public void addLiteralMatch(
            String field,
            boolean type2,
            String literal) {
        addMatch(new Match.AppendLiteral(field, type2, literal));
    }
    
    public void addBooleanMatch(
            String field,
            boolean type2,
            boolean value) {
        addMatch(new Match.AppendLiteral(field, type2, toBooleanLiteral(value)));
    }
    
    private String toBooleanLiteral(boolean value) {
        switch (getDatabase()) {
        case JdbcProperties.DB2 :
        case JdbcProperties.ORACLE :
        case JdbcProperties.MYSQL :
            return value ? " != 0" : " = 0";
        default:
            return value ? " = true" : " = false";
        }
    }

    public void addListOfUidMatch(String field, boolean type2, String[] uids) {
        addMatch(new Match.ListOfUID(field, type2, uids));
    }

    public void addWildCardMatch(
        String field,
        boolean type2,
        String wc,
        boolean ignoreCase) {
        addMatch(new Match.WildCard(field, type2, wc, ignoreCase));
    }

    public void addRangeMatch(String field, boolean type2, Date[] range) {
        addMatch(new Match.Range(field, type2, range));
    }

    public void addModalitiesInStudyMatch(String md) {
        addMatch(new Match.ModalitiesInStudy(md));
    }

    public String getSql() {
        if (select == null)
            throw new IllegalStateException("select not initalized");
        if (from == null)
            throw new IllegalStateException("from not initalized");

        StringBuffer sb = new StringBuffer("SELECT ");
        if (limit > 0 || offset > 0) {
            switch (getDatabase()) {
                case JdbcProperties.HSQL :
                    sb.append("LIMIT ");
                    sb.append(offset);
                    sb.append(" ");
                    sb.append(limit);
                    sb.append(" ");
                    appendTo(sb, select);
                    break;
                case JdbcProperties.DB2 :
                    sb.append("* FROM ( SELECT ");
                    appendTo(sb, select);
                    sb.append(", ROW_NUMBER() OVER (ORDER BY ");
                    appendTo(
                        sb,
                        (String[]) orderby.toArray(new String[orderby.size()]));
                    sb.append(") AS rownum ");
                    break;
                case JdbcProperties.ORACLE :
                    sb.append("* FROM ( SELECT ");
                    appendTo(sb, select);
                    sb.append(", ROWNUM as r1 FROM ( SELECT ");
                    appendTo(sb, select);
                    break;
                default:
                    appendTo(sb, select);
                    break;
            }
        } else {
            appendTo(sb, select);            
        }
        sb.append(" FROM ");
      	appendInnerJoinsToFrom(sb);
        appendLeftJoinToFrom(sb);
        whereOrAnd = WHERE;
       	appendInnerJoinsToWhere(sb);
        appendLeftJoinToWhere(sb);
        appendMatchesTo(sb);
        if (!orderby.isEmpty()) {
            sb.append(" ORDER BY ");
            appendTo(
                sb,
                (String[]) orderby.toArray(new String[orderby.size()]));
        }
        if (limit > 0 || offset > 0) {
            switch (getDatabase()) {
                case JdbcProperties.PSQL :
                case JdbcProperties.MYSQL :
                    sb.append(" LIMIT ");
                    sb.append(limit);
                    sb.append(" OFFSET ");
                    sb.append(offset);
                    break;
                case JdbcProperties.DB2 :
                    sb.append(" ) AS foo WHERE rownum > ");
                    sb.append(offset);
                    sb.append(" AND rownum <= ");
                    sb.append(offset + limit);
                    break;
                case JdbcProperties.ORACLE :
                    sb.append(" ) WHERE ROWNUM <= ");
                    sb.append(offset + limit);
                    sb.append(" ) WHERE r1 > ");
                    sb.append(offset);
                    break;
            }
        }
        return sb.toString();
    }

    private void appendTo(StringBuffer sb, String[] a) {
        for (int i = 0; i < a.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(a[i]);
        }
    }

    private void appendLeftJoinToFrom(StringBuffer sb) {
        if (leftJoin == null) return;
        if (getDatabase() == JdbcProperties.ORACLE) {
            sb.append(", ");
            sb.append(leftJoin[0]);            
        } else {
	        sb.append(" LEFT JOIN ");
	        sb.append(leftJoin[0]);
	        sb.append(" ON (");
	        sb.append(leftJoin[1]);
	        sb.append(" = ");
	        sb.append(leftJoin[2]);
	        sb.append(")");
        }
    }

    private void appendLeftJoinToWhere(StringBuffer sb) {
        if (leftJoin == null || getDatabase() != JdbcProperties.ORACLE) return;
        sb.append(whereOrAnd);
        whereOrAnd = AND;
        sb.append(leftJoin[1]);
        sb.append(" = ");
        sb.append(leftJoin[2]);
        sb.append("(+)");
    }
        
	private void appendInnerJoinsToFrom(StringBuffer sb) {
		if (relations == null || getDatabase() == JdbcProperties.ORACLE
		        || getDatabase() == JdbcProperties.HSQL) {
			appendTo(sb,from);
		} else {
			sb.append(from[0]);
			for (int i = 1; i < from.length; i++) {
				sb.append(" JOIN ");
				sb.append(from[i]);
				sb.append(" ON (");
				sb.append(relations[(i-1)*2]);
				sb.append(" = ");
				sb.append(relations[(i-1)*2+1]);
				sb.append(")");
			}
		}
	}
	
	private void appendInnerJoinsToWhere(StringBuffer sb) {
		if (relations == null || !(getDatabase() == JdbcProperties.ORACLE 
		        || getDatabase() == JdbcProperties.HSQL)) return;
        for (int i = 0; i < relations.length; i++, i++) {
            sb.append(whereOrAnd);
            whereOrAnd = AND;
            sb.append(relations[i]);
            sb.append(" = ");
            sb.append(relations[i + 1]);
        }
	}

    private void appendMatchesTo(StringBuffer sb) {
        if (matches == null) return;
        for (int i = 0; i < matches.size(); i++) {
            sb.append(whereOrAnd);
            whereOrAnd = AND;
            ((Match) matches.get(i)).appendTo(sb);
        }
    }

    public void setStudyFilterMatch(StudyFilterDTO filter) {
        addWildCardMatch(
            "Patient.patientId",
            SqlBuilder.TYPE2,
            filter.getPatientID(),
            false);
        addWildCardMatch(
            "Patient.patientName",
            SqlBuilder.TYPE2,
            filter.getPatientName(),
            true);
        addWildCardMatch(
            "Study.studyId",
            SqlBuilder.TYPE2,
            filter.getStudyID(),
            false);
        addRangeMatch(
            "Study.studyDateTime",
            SqlBuilder.TYPE2,
            toDateTimeRange(filter.getStudyDateRange()));
        addWildCardMatch(
            "Study.accessionNumber",
            SqlBuilder.TYPE2,
            filter.getAccessionNumber(),
            false);
        addModalitiesInStudyMatch(filter.getModality());
    }

    private static final long MS_PER_DAY = 24 * 3600000L;

    private static Date[] toDateTimeRange(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        try {
            Date[] retval = new Date[2];
            if (s.length() < 4) {
                retval[0] =
                    new Date(
                        (System.currentTimeMillis() / MS_PER_DAY
                            - Integer.parseInt(s))
                            * MS_PER_DAY);
            } else {
                Calendar cal = Calendar.getInstance();
                final int hypen = s.indexOf('-');
                int field = 0;
                if (hypen != 0) {
                    field =
                        setCalendar(
                            cal,
                            hypen != -1 ? s.substring(0, hypen) : s);
                    retval[0] = cal.getTime();
                }
                if (hypen != s.length() - 1) {
                    if (hypen != -1) {
                        field = setCalendar(cal, s.substring(hypen + 1));
                    }
                    cal.add(field, 1);
                    retval[1] = cal.getTime();
                }
            }
            return retval;
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    private static int setCalendar(Calendar cal, String s) {
        StringTokenizer stk = new StringTokenizer(s, "/");
        cal.clear();
        cal.set(Calendar.YEAR, Integer.parseInt(stk.nextToken()));
        if (!stk.hasMoreElements()) {
            return Calendar.YEAR;
        }
        cal.set(Calendar.MONTH, Integer.parseInt(stk.nextToken()) - 1);
        if (!stk.hasMoreElements()) {
            return Calendar.MONTH;
        }
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(stk.nextToken()));
        return Calendar.DAY_OF_MONTH;
    }
}
