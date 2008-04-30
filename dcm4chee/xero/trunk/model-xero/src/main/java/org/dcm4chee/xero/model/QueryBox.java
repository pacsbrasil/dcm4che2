package org.dcm4chee.xero.model;

/** A Query Box is a simple model object that is used to layout a query search criter object. */
public class QueryBox extends Layout {
   LazyMap query;

   public QueryBox(LazyMap query) {
	  super("query/query");
	  this.query = query;
   }
   
   public LazyMap getQuery() {
	  return this.query;
   }
}
