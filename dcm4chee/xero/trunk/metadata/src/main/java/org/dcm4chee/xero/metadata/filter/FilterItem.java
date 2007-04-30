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
import java.util.logging.Logger;

import org.dcm4chee.xero.metadata.MetaDataBean;

/** A filter item is used to define the items in a filter list.  
 * @author bwallace
 *
 */
public class FilterItem implements Comparable<FilterItem> {
	String name;
	public Filter<?> filter;
	private int priority;
	MetaDataBean metaData;
	Object config;
	FilterItem nextFilterItem;
	static Logger log = Logger.getLogger(FilterItem.class.getName());
	
	/**
	 * Create a filter item based on the given meta-data node.
	 * @param mdb to get information such as priority.
	 */
	public FilterItem(MetaDataBean mdb)
	{
		this.name = mdb.getChildName();
		this.filter = (Filter<?>) mdb.getValue();
		String strPriority = (String) mdb.getValue("priority");
		if( strPriority ==null ) strPriority = "50";
		priority = Integer.parseInt(strPriority);
		metaData = mdb;
	}
	
	/** Compare this filter item to another filter item.
	 * @return int comparison value +ve/0/-ve for <, = and >
	 */
	public int compareTo(FilterItem o) {
		int ret = o.priority - this.priority;
		if( ret!=0 ) return ret;
		ret = o.name.compareTo(this.name);
		if( ret!=0 ) return ret;
		return 0;
	}
	
	/**
	 * Return the configuration information for this filter item - if any.
	 * @return
	 */
	public Object getConfig()
	{
		if( config==null && metaData!=null ) {
			config = metaData.getValueConfig();
		}
		return config;
	}
	
	/** Calls the next filter item in the chain.
	 * @param params to pass to the next filter
	 * @return The filtered return value.
	 */
	public Object callNextFilter(Map<String,Object> params)
	{
		if( nextFilterItem==null ) return null;
		if( nextFilterItem.priority < 0 ) return null;
		return nextFilterItem.filter.filter(nextFilterItem, params);
	}
	
	/** Calls the given, named filter item from this chain.
	 * @param filterName is the local name to look for.
	 * @param params are the parameters to pass.
	 * @return value from the filter.
	 */
	public Object callNamedFilter(String filterName, Map<String,Object> params)
	 {
		 FilterListConfig fl = (FilterListConfig) metaData.getParent().getValueConfig();
		 if( fl==null ) {
			 log.warning("Parent "+metaData.getParent().getPath()+" did not have a filter list configuration item.");
			 return null;
		 }
		 FilterItem namedFilter = fl.getNamedFilter(filterName);
		 if( namedFilter==null ) {
			 log.warning("No filter named "+filterName+" in "+metaData.getParent().getPath());
			 return null;			 
		 }
		 return namedFilter.filter.filter(namedFilter, params);
	 }

	/** Return the next filters name/metadata path, if any */
	public String getNextFilterName() {
		if( nextFilterItem==null ) return null;
		return nextFilterItem.getName();
	}
	
	/** Returns the filter name - that is, the path, for the filter */
	public String getName() {
		if( metaData==null ) return null;
		return metaData.getPath();
	}
}