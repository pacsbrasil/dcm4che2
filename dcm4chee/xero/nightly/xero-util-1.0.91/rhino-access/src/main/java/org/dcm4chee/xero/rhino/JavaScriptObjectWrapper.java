package org.dcm4chee.xero.rhino;

import java.util.AbstractMap;
import java.util.Set;

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
