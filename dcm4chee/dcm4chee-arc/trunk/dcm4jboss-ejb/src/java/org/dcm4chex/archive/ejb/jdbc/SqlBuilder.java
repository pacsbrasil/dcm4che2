/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.ejb.jdbc;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 26.08.2003
 */
class SqlBuilder {

    public static final boolean TYPE1 = false;
    public static final boolean TYPE2 = true;
    private String[] select;
    private String[] from;
    private String[] leftJoin;
    private String[] pk;
    private String[] fk = {};
    private ArrayList matches = new ArrayList();

    public void setSelect(String[] fields, int length) {
        select = JdbcProperties.getInstance().getProperties(fields, length);
    }

    public void setFrom(String[] entities, int length) {
        JdbcProperties jp = JdbcProperties.getInstance();
        from = jp.getProperties(entities, length);
        pk = jp.getPk(entities);
    }

    public void setLeftJoin(String entity, String fk) {
        JdbcProperties jp = JdbcProperties.getInstance();
        leftJoin =
            new String[] {
                jp.getProperty(entity),
                jp.getProperty(entity + ".pk"),
                jp.getProperty(fk)};
    }

    public void setFk(String[] fk, int length) {
        if (from == null)
            throw new IllegalStateException("from not initalized");
        if (length != from.length - 1)
            throw new IllegalArgumentException(
                "length:" + length + ", from.length:" + from.length);
        this.fk = JdbcProperties.getInstance().getProperties(fk, length);
    }

    private void addMatch(Match match) {
        if (!match.isUniveralMatch())
            matches.add(match);
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
        appendTo(sb, select);
        sb.append(" FROM ");
        appendTo(sb, from);
        if (leftJoin != null)
            appendLeftJoinTo(sb);
        if (fk.length != 0 || !matches.isEmpty()) {
            sb.append(" WHERE ");
            if (fk.length != 0) {
                appendFkTo(sb);
                if (!matches.isEmpty())
                    sb.append(" AND ");
            }
            appendMatchesTo(sb);
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

    private void appendLeftJoinTo(StringBuffer sb) {
        sb.append(" LEFT JOIN ");
        sb.append(leftJoin[0]);
        sb.append(" ON (");
        sb.append(leftJoin[1]);
        sb.append(" = ");
        sb.append(leftJoin[2]);
        sb.append(")");
    }

    private void appendFkTo(StringBuffer sb) {
        for (int i = 0; i < fk.length; i++) {
            if (i > 0)
                sb.append(" AND ");
            sb.append(fk[i]);
            sb.append(" = ");
            sb.append(pk[i]);
        }
    }

    private void appendMatchesTo(StringBuffer sb) {
        for (int i = 0; i < matches.size(); i++) {
            if (i > 0)
                sb.append(" AND ");
            ((Match) matches.get(i)).appendTo(sb);
        }
    }

}
