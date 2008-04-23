package org.dcm4chee.xero.model;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LazyMap extends HashMap<String, Object> {
   Map<String, Object> lazy;

   public LazyMap(Map<String, Object> lazy) {
	  this.lazy = lazy;
   }
   
   public LazyMap() {
   }

   public Object get(Object key) {
	  Object ret = super.get(key);
	  if (ret != null)
		 return ret;
	  ret = checkLazy(key);
	  return ret;
   }

   public boolean containsKey(Object key) {
	  if (super.containsKey(key))
		 return true;
	  Object v = checkLazy(key);
	  if (v != null)
		 return true;
	  return false;
   }

   /** Check to see if there is a lazy created object, and if so, see if it is a map factory,
    * and use the factory if so, and put the result into this map.
    * Used by containsKey and get to retrieve the values.
    */
   protected Object checkLazy(Object key) {
	  Object v = getLazy(key);
	  if (v == null)
		 return null;
	  if (v instanceof MapFactory) {
		 v = ((MapFactory) v).create(this);
	  }
	  this.put((String) key, v);
	  return v;
   }

   /**
    * Get the lazily created object.
    * 
    * @param key
    * @return
    */
   protected Object getLazy(Object key) {
	  if (lazy == null)
		 return null;
	  return lazy.get(key);
   }
}
