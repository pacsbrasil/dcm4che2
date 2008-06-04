/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class LazyMap extends HashMap<String, Object> {
   private static final Logger log = LoggerFactory.getLogger(LazyMap.class); 
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
		 if( v!=null ) log.info("Created lazy {} of class {}", key,v.getClass());
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
