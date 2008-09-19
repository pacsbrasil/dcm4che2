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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.metadata.filter;

import java.util.Map;

/**
 * Provides static utilities to get access to various elements from the params
 * map etc.
 * 
 * @author bwallace
 * 
 */
public class FilterUtil {

   public static String getString(Map<String,Object> params, String key, String def) {
	  Object v = params.get(key);
	  if (v == null)
		 return def;
	  if (v instanceof String)
		 return (String) v;
	  throw new IllegalArgumentException("Expected String value for " + key + " got class " + v.getClass() + " value " + v);
   }
   
   public static boolean getBoolean(Map<String,Object> params, String key) {
	  Object v = params.get(key);
	  if( v==null ) return false;
	  if( v instanceof String ) {
		 String s= (String) v;
		 return "1".equals(s) || "true".equalsIgnoreCase(s);
	  }
	  if( v instanceof Boolean ) return (Boolean) v;
	  throw new IllegalArgumentException("Expected boolean value for " + key + " got class " + v.getClass() + " value " + v);
   }
   
   public static int getInt(Map<String, Object> params, String key) {
	  return getInt(params, key, 0);
   }

   public static int getInt(Map<String, Object> params, String key, int def) {
	  Object v = params.get(key);
	  if (v == null)
		 return def;
	  if (v instanceof String)
		 return Integer.parseInt((String) v);
	  if (v instanceof Integer)
		 return ((Integer) v).intValue();
	  throw new IllegalArgumentException("Expected integer value for " + key + " got class " + v.getClass() + " value " + v);
   }

   public static long getLong(Map<String, Object> params, String key) {
	  return getLong(params, key, 0l);
   }

   public static long getLong(Map<String, Object> params, String key, long def) {
	  Object v = params.get(key);
	  if (v == null)
		 return def;
	  if (v instanceof String)
		 return Long.parseLong((String) v);
	  if (v instanceof Integer)
		 return ((Integer) v).intValue();
	  if (v instanceof Long)
		 return ((Long) v).longValue();
	  throw new IllegalArgumentException("Expected long value for " + key + " got class " + v.getClass() + " value " + v);
   }

   /** Get the float values from params */
   public static float[] getFloats(Map<String, Object> params, String key, float[] def) {
	  Object v = params.get(key);
	  if (v == null)
		 return def;
	  if (v instanceof String) {
		 String s = (String) v;
		 return splitFloat(s, 0);
	  }
	  if (v instanceof float[])
		 return (float[]) v;
	  if (v instanceof Float)
		 return new float[] { (Float) v };
	  throw new IllegalArgumentException("Expected float[] value for " + key + " got class " + v.getClass() + " value " + v);
   }

   /** Splits region into sub-parts */
   public static double[] splitDouble(String region, int size) {
	  double ret[] = new double[size];
	  int start = 0;
	  region = region.trim();
	  for (int i = 0; i < ret.length; i++) {
		 if (start >= region.length())
			throw new IllegalArgumentException("Too few arguments in " + region);
		 int end = region.indexOf(',', start);
		 if (end < 0)
			end = region.length();
		 ret[i] = Double.parseDouble(region.substring(start, end));
		 start = end + 1;
	  }
	  if (start < region.length())
		 throw new IllegalArgumentException("Too many arguments in " + region);
	  return ret;
   }

   /** Splits region into sub-parts */
   public static float[] splitFloat(String region, int size) {
	  if (size == 0) {
		 int start = region.indexOf(',');
		 size++;
		 while (start > 0) {
			size++;
			start = region.indexOf(',', start + 1);
		 }
	  }
	  float ret[] = new float[size];
	  int start = 0;
	  region = region.trim();
	  for (int i = 0; i < ret.length; i++) {
		 if (start >= region.length())
			throw new IllegalArgumentException("Too few arguments in " + region);
		 int end = region.indexOf(',', start);
		 if (end < 0)
			end = region.length();
		 ret[i] = Float.parseFloat(region.substring(start, end));
		 start = end + 1;
	  }
	  if (start < region.length())
		 throw new IllegalArgumentException("Too many arguments in " + region);
	  return ret;
   }
}
