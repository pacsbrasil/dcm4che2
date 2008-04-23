package org.dcm4chee.xero.model;

import java.util.Map;
/**
 * A MapFactory takes a Map of values and produces one or more additional values computed from them.
 * 
 * @author bwallace
 */
public interface MapFactory {

   /** Create a computed value from the src value */
   public Object create(Map<String,Object> src);
}
