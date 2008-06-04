package org.dcm4chee.xero.model;

/** A Query Box is a simple model object that is used to layout a query search criteria object. */
public class QueryLayout extends Layout {
   LazyMap query;

   public QueryLayout(LazyMap query) {
	  super("query/query");
	  this.query = query;
   }
   
   public LazyMap getQuery() {
	  return this.query;
   }
}
