package org.dcm4chee.xero.template;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class Phase {
   public static final int INITIAL_PHASE = 0;
   public static final int BASE_PHASE = 1;
   public static final int CLIENT_PHASE = 2;
   
   public static final Map<String,Object> LATER = new AbstractMap<String,Object>() {
	  /** Always return this for all child elements... */
	  @Override
	  public Object get(Object key) { return this; }; 

	  @Override
	  public Set<java.util.Map.Entry<String, Object>> entrySet() {
		 throw new UnsupportedOperationException("Only get supported.");
	  }
	  
   };
}
