/*
$Id: Query.java,v $
$Log: Query.java,v $

*/

package com.agfa.db.tools;

public class Query {
    public String select = null;
    public String from = null;
    public String where = null;
    public String links = null;
    
    public Query (String a, String b, String c, String d) {
        select = a;
        from = b;
        where = c;
        links = d;
    }
}
