package org.dcm4chee.xero.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.dcm4chee.xero.model.MapFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlFactory implements MapFactory, MetaDataUser {
   private static final Logger log = LoggerFactory.getLogger(UrlFactory.class);
   
   /** The base of the URL */
   String base;
   List<String> queryParams = new ArrayList<String>();
   
   /** Create a URL from the base query, plus the set of keys used to search on.
    * TODO implement this to return something dynamic instead of the static version it has right now.
    */
   public Object create(Map<String, Object> src) {
	  StringBuffer ret = new StringBuffer(base);
	  boolean first = true;
	  log.info("Creating a URL starting from "+base);
	  for(String key : queryParams) {
		 Object value = src.get(key);
		 log.info("Checking "+key+"="+value);
		 if( value==null ) continue;
		 ret.append(first ? '?' : '&');
		 first = false;
		 ret.append(key).append('=').append(value.toString());
	  }
	  return ret.toString();
   }

   /**
    * Sets the base name for the query - not including the method (http: or file: etc) or the server/port
    * but rather just the service name, eg /wado2/study.xml
    * @param base
    */
   @MetaData
   public void setBase(String base) {
	  this.base = base;
   }

   /** Gets the set of query parameters from the metadata. */
   public void setMetaData(MetaDataBean mdb) {
	  for(Map.Entry<String,MetaDataBean> me : mdb.entrySet() ) {		 
		 Object value = me.getValue().getValue();
		 if( "query".equals(value) ) {
			queryParams.add(me.getKey());
		 }
	  }
   }
}
