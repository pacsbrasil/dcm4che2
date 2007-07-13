/*
 * org.dcm4che.archive.dao.helper.QueryUtils.java
 * Created on Jun 27, 2007 by jfalk
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.dao.helper;

import java.util.List;

public class QueryUtils {

    /**
     * Generate a string to be used in an in clause of a query.
     * 
     * ex.  a.status in(1,2,3)
     * 
     * @param values
     * @return
     */
    public static String buildQueryList(List values) {
        Object obj = values.get(0);

        String start = "";
        String end = "";
        String delimeter = ",";

        if (obj instanceof String) {
            start = "'";
            end = "'";
            delimeter = "','";
        }

        StringBuilder sb = new StringBuilder(start);
        for (int i = 0; i < values.size(); i++) {
            if (i != 0)
                sb.append(delimeter);
            sb.append(values.get(i).toString());
        }
        sb.append(end);
        return sb.toString();
    }
}
