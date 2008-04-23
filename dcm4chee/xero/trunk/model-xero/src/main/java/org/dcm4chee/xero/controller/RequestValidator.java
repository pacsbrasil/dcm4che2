package org.dcm4chee.xero.controller;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.dcm4chee.xero.model.LazyMap;
import org.dcm4chee.xero.model.MapFactory;
import org.dcm4chee.xero.parsers.Parser;
import org.dcm4chee.xero.parsers.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller takes an http request obect, along with the definitions for parameters, and
 * creates or updates a map containing parsed/validated request parameters.  
 * If the parsing or validation fails, it can create an alternative map that contains safe strings with
 * the values, plus an indication per-item of the error (or that this item is ok).
 * It ignores all attributes that aren't specified in the request.
 * 
 * @author bwallace
 *
 */
public class RequestValidator implements MetaDataUser, MapFactory {
   private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);
   Map<String,Parser> parsers = new HashMap<String,Parser>();
   Map<String,Object> factories;
   public static final String PARSERS_KEY = "_parsers";
   
   /** Creates the validated request object */
   @SuppressWarnings("unchecked")
   public Object create(Map<String,Object> src) {
	  // We actually want the request map itself, not the root map.
	  Map<String,Object> requestMap = (Map<String,Object>) src.get("parameters");
	  if( requestMap==null ) throw new IllegalArgumentException("src map must have a parameters map containing the http request parameters.");
	  Object ret = lookupInstanceValue();
	  Map<String,Object> map = getMapView(ret);
	  log.info("Creating validated request object.");
	  for(Map.Entry<String,Parser> me : parsers.entrySet()) {
		 Parser p = me.getValue();
		 log.info("Looking for value "+me.getKey());
		 Object v = null;
		 try {
			v = p.parse(requestMap);
		 } catch (ParserException e) {
			// TODO - handle this exception somehow in the return object....
			e.printStackTrace();
			continue;
		 }
		 map.put(me.getKey(),v);
	  }
	  return ret;
   }

   /** Gets an instance value for this object.  Just creates a hashmap by default for now.
    */
   protected Object lookupInstanceValue() {
	  return new LazyMap(factories);
   }
   
   /**
    * Creates a map view over the given object, supporting set/get and contains.  Currently
    * assumes a Map of some type is directly returned, but in the future can do something else to support
    * JavaBeans etc.
    * @param ret
    * @return
    */
   @SuppressWarnings("unchecked")
   protected Map<String,Object> getMapView(Object ret) {
	  return (Map<String,Object>) ret;
   }

   /**
    * Search the meta-data for parsers and factories
    */
   public void setMetaData(MetaDataBean mdb) {
	  log.info("Set meta data on request validator.");
	  for(Map.Entry<String,MetaDataBean> me : mdb.entrySet() ) {		 
		 Object value = me.getValue().getValue();
		 log.info("Testing "+me.getKey()+"="+value + " instanceof MapFactory "+(value instanceof MapFactory));
		 if( value instanceof Parser ) {
			log.info("Adding parser "+me.getKey());
			parsers.put(me.getKey(), (Parser) value);			
		 }
		 else if( value instanceof MapFactory ) {
			if( factories==null ) {
			   factories = new HashMap<String,Object>();
			}
			log.info("Putting factory "+me.getKey());
			factories.put(me.getKey(), (MapFactory) value);
		 }
	  }
   }
   

}
