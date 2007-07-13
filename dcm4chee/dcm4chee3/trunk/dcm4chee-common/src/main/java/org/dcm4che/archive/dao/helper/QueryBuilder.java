/*
 * org.dcm4che.archive.dao.helper.QueryBuilder.java
 * Created on Jun 27, 2007 by jfalk
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.dao.helper;

public class QueryBuilder {
    private StringBuilder query;

    private boolean first = true;

    public QueryBuilder(String selectStatement) {
        this.query = new StringBuilder(selectStatement);
    }

    public void addJoin(String join) {
        query.append(" join ");
        query.append(join);
    }

    public void addCondition(String condition) {
        if (first)
            query.append(" where ");
        else
            query.append(" and ");
        query.append(condition);
        first = false;
    }

    public String getQueryString() {
        return query.toString();
    }
}