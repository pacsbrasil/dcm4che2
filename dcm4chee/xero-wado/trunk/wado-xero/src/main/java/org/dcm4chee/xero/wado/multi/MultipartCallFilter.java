package org.dcm4chee.xero.wado.multi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.util.ExtendibleIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.wado.WadoParams.*;

/**
 * This class knows how to call other filter types, setting and un-setting
 * various parameters
 * 
 * @author bwallace
 * 
 */
public class MultipartCallFilter implements Filter<Iterator<ServletResponseItem>> {
	private static final Logger log = LoggerFactory.getLogger(MultipartCallFilter.class);
	
	Filter<ServletResponseItem> childFilter;
	String childName, childNameDot;
	Map<String, Object> addDefaults;
	boolean isDefault = false;

	/**
	 * Calls the named multi-part filters as children elements, when they are
	 * listed in the multipart=... list, or all of them otherwise.
	 */
	public Iterator<ServletResponseItem> filter(FilterItem<Iterator<ServletResponseItem>> filterItem,
	      Map<String, Object> params) {
		String multipart = (String) params.get(MULTIPART_KEY);
		Iterator<ServletResponseItem> it = null;
		// Skip all other iterations if this is the only iterator.
		if( !childName.equals(multipart) )
			it = filterItem.callNextFilter(params);
		if (multipart == null) {
			if (!isDefault)
				return it;
		} else {
			int pos = multipart.indexOf(childName);
			if( pos<0 ) return it;
			if (pos > 0 && multipart.charAt(pos - 1) != ',')
				return it;
			if (pos + childName.length() < multipart.length() && multipart.charAt(pos + childName.length()) != ',')
				return it;
		}

		ServletResponseItem sri = callChildFilter(params);
		if (sri == null)
			return it;
		ExtendibleIterator<ServletResponseItem> ei;
		if (it instanceof ExtendibleIterator)
			ei = (ExtendibleIterator<ServletResponseItem>) it;
		else
			ei = new ExtendibleIterator<ServletResponseItem>(it);
		ei.add(sri);
		return ei;
	}

	/**
	 * Calls the child filter to get an item to add - may return null if the
	 * child returns null. If the child returns a not-found error, then null will
	 * be returned instead.
	 * 
	 * @param params
	 * @return
	 */
	protected ServletResponseItem callChildFilter(Map<String, Object> params) {
		params = createModifiedParams(params);
		ServletResponseItem ret = childFilter.filter(null, params);
		if (ret instanceof ErrorResponseItem) {
			ErrorResponseItem eri = (ErrorResponseItem) ret;
			if (eri.getCode() == HttpServletResponse.SC_NO_CONTENT)
				return null;
		}
		return ret;
	}

	/**
	 * Creates a modified parameter map - by default, this starts with params,
	 * adds everything from addDefaults, (if not null), and then every entry
	 * starting with childName.X as X Thus, it is possible to set specific child
	 * type values for specific filters. Returns a new params map iff any item is
	 * modified. Override this and call the super method if specific behaviour is
	 * indicated.
	 */
	protected Map<String, Object> createModifiedParams(Map<String, Object> params) {
		Map<String, Object> ret = params;
		if (addDefaults != null && addDefaults.size() > 0) {
			ret = new HashMap<String, Object>(params);
			log.debug("Adding initial set of defaults {}",addDefaults);
			ret.putAll(addDefaults);
		}
		log.debug("Looking for child key {}",childNameDot);
		for (Map.Entry<String, Object> me : params.entrySet()) {
			if (me.getKey().startsWith(childNameDot)) {
				String newKey = me.getKey().substring(childNameDot.length());
				if (newKey.equals(""))
					continue;
				if (ret == params)
					ret = new HashMap<String, Object>(params);
				log.debug("Adding over-ride key '{}'='{}'",newKey,me.getValue());
				ret.put(newKey, me.getValue());
			}
		}
		return ret;
	}

	/** Gets the filter to call to get a servlet response item. */
	public Filter<ServletResponseItem> getChildFilter() {
		return childFilter;
	}

	@MetaData
	public void setChildFilter(Filter<ServletResponseItem> childFilter) {
		this.childFilter = childFilter;
	}

	/** Gets the name to lookup in the multipart trigger name */
	public String getChildName() {
		return childName;
	}

	@MetaData
	public void setChildName(String childName) {
		this.childName = childName;
		this.childNameDot = childName + ".";
	}

	/**
	 * Gets the map of items to add to the parameters when calling a child -
	 * before any specific over-rides.
	 * 
	 * @return
	 */
	public Map<String, Object> getAddDefaults() {
		return addDefaults;
	}

	@MetaData(required = false)
	public void setAddDefaults(Map<String, Object> addDefaults) {
		this.addDefaults = addDefaults;
		// Call the entry set creator to ensure any intialiazation has been done,
		// ala MapWithDefaults,
		// otherwise synchronization might be required.
		if (addDefaults != null)
			addDefaults.entrySet();
	}

	@MetaData(required = false)
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
