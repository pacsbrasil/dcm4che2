package org.dcm4chee.xero.metadata.access;

import java.util.Enumeration;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** This is a map implementation on top of a resource bundle */
public class ResourceBundleMap extends LazyMap {
	private static final long serialVersionUID = -1L;
	
	ResourceBundle resourceBundle;
	
	public ResourceBundleMap(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	/** Create a resource bundle map with a set of defaults - typically the defaults will contain the theme information
	 * but they can also contain localized over-rides for the internationalized string values.
	 * If so, then NONE of the languages values will be used for that key.
	 * @param resourceBundle
	 * @param baseMap - any type of map will do as long as it has some string keys.
	 */
	@SuppressWarnings("unchecked")
	public ResourceBundleMap(ResourceBundle resourceBundle, Map<?,?> baseMap) {
		super((Map) baseMap);
		this.resourceBundle = resourceBundle;
	}
	
	/** Gets a value from the bundle */
	@Override
	protected Object getLazy(Object key) {
		String skey = key.toString();
		Object ret = super.getLazy(key);
		if( ret!=null ) return ret;
		try{
			resourceBundle.getObject(skey);
		}catch(MissingResourceException mre){
			return null;
		}
		return resourceBundle.getString((String) key);
	}

	/** Adds all the resource bundle items */
	@Override
	protected boolean addAllLazy() {
		if( super.addAllLazy() ) return true;
		Enumeration<String> en = resourceBundle.getKeys();
		while(en.hasMoreElements()) {
			String key = en.nextElement();
			this.get(key);
		}
		return false;
	}
}
