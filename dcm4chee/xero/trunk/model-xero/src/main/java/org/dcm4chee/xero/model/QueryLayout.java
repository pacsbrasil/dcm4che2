package org.dcm4chee.xero.model;

import org.dcm4chee.xero.metadata.access.LazyMap;

/** A Query Box is a simple model object that is used to layout a query search criteria object. */
public class QueryLayout extends Layout {
   private static final long serialVersionUID = -5124326692363339163L;
   
	LazyMap query;

   public QueryLayout(LazyMap query) {
	  super("query/query");
	  this.query = query;
   }
   
   public LazyMap getQuery() {
	  return this.query;
   }
}
