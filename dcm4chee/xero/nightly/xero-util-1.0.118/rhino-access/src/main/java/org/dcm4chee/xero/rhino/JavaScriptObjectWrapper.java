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
package org.dcm4chee.xero.rhino;

import java.util.AbstractMap;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Wraps JavaScript objects to make them look like a Map - also see the JavaScriptArrayWrapper to
 * wrap JS arrays.
 * @author bwallace
 *
 */
public class JavaScriptObjectWrapper extends AbstractMap<String,Object> {
	protected Scriptable scriptable;
	
	private static final ContextFactory contextFactory = new ContextFactory();

	public JavaScriptObjectWrapper(Scriptable scriptable) {
		this.scriptable = scriptable;
	}
	
	/** Returns the element key from the underlying JavaScript object.
	 * JS Strings are returned as Java Strings, js integers as Integer, floating as Double,
	 * and everything else as a Map or List implementation.
	 * @param key
	 * @return
	 */
	public Object get(Object key) {
		Object ret = ScriptableObject.getProperty(scriptable,(String) key);
		return wrapScriptable(ret);
	}
	
	/** Returns an object - that is a JavaScriptObjectWraper for the named property */
	public JavaScriptObjectWrapper getObject(String key) {
		return (JavaScriptObjectWrapper) get(key);
	}
	
	/** Returns a String */
	public String getString(String key) {
		return (String) get(key);
	}
	
	/** Calls the named method, and returns the result, if any. */
	public Object callMethod(String methodName, Object... args) {
		return ScriptableObject.callMethod(scriptable,methodName,args);
	}
	
	/** Executes the given script, returning the return value (wrapped appropriately) */
	public Object call(String script) {
		Context ctx = contextFactory.enterContext();
		ctx.setWrapFactory(JavaScriptMapFactory.SUGAR_WRAP_FACTORY);
		return wrapScriptable(ctx.evaluateString(scriptable, script, "<script>", 1, null));
	}
	
	/** Indicates if the given property exists */
	@Override
	public boolean containsKey(Object key) {
		return ScriptableObject.hasProperty(scriptable, (String) key);
	}
	
	/** Sets the given value, returning null - NOT the previous value.
	 * Puts wrapped objects as their base, underlying values, NOT as the JSOW object.
	 */
	@Override
	public Object put(String key, Object value) {
		value = unwrap(value);
		ScriptableObject.putProperty(scriptable,key, value);
		return null;
	}
	
	/** Wrap an object with a wrapper to make it look like a Java Map, Array or regular (non-Rhino)
	 * Java object.
	 * @param s
	 * @return
	 */
	public static Object wrapScriptable(Object s) {
		if(s==null || !( s instanceof Scriptable) ) {
			return s;
		}
		return new JavaScriptObjectWrapper((Scriptable) s);
	}
	
	/** Unwraps a wrapped object to return the original value */
	public static Object unwrap(Object s) {
		if(s==null || !(s instanceof JavaScriptObjectWrapper) ) return s;
		return ((JavaScriptObjectWrapper) s).scriptable;
	}

	@Override
   public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException("Entry set not yet defined.");
   }
}
