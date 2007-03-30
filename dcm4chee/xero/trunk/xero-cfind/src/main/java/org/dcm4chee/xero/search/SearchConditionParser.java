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
package org.dcm4chee.xero.search;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;

/** This class parses search conditions from a Map of name to String array
 * values, typically from the get/post parameter values.  It ignores any provided
 * value that isn't found, and throws an exception on certain invalid types.
 * @author bwallace
 *
 */
public class SearchConditionParser implements Filter<SearchCriteria> {
	private static Logger log = Logger.getLogger(SearchConditionParser.class.getName());
	
	List<TableColumn> searchColumns;
	
	Map<String,TableColumn> searchColumnsMap;

	/**
	 * Create a search condition parser that looks up the request parameters in the faces context and
	 * parses those into search conditions.
	 * @param searchColumns
	 */
	public SearchConditionParser(List<TableColumn> searchColumns)
	{
		this.searchColumns = searchColumns;
		this.searchColumnsMap = TableColumn.toMap(searchColumns);
	}

	/**
	 * For each key that is a search column, convert it to the appropriate type and 
	 * add it to the search condition.
	 * @param parameterValues
	 * @return A non-empty search condition that matches the parameter values supplied.
	 */
	public SearchCriteria parseFromMap( Map<String,?> parameterValues )
	{
		SearchCriteria searchCondition = new SearchCriteria();
		for(String key : parameterValues.keySet())
		{
			int index = key.indexOf('.');
			if( index>0 ) {
				key = key.substring(0,index);
			}
			TableColumn tc = searchColumnsMap.get(key);
			if( tc==null ) {
				log.finer("Ignoring parameter "+key+" as it isn't a table column.");
				continue;
			}
			if( index>0 ) {
				throw new UnsupportedOperationException("No parsing of multi-valued objects yet.");
			}
			Object value = parameterValues.get(key);
			TableColumn clone = cloneSimple(tc,value);
			searchCondition.getAttributeByName().put(key,clone);
		}
		return searchCondition;
	}
	
	protected TableColumn cloneSimple(TableColumn prototype, Object value) {
		TableColumn ret = new TableColumn(prototype);
		if( (value instanceof String) || (value instanceof Number) ) {
			ret.setValue(value.toString());
		}
		else if( value instanceof String[] ) {
			ret.setValue((String[]) value);
		}
		else {
			throw new UnsupportedOperationException("Can't assign value "+value+" of type "+value.getClass());
		}
		
		return ret;
	}

	public SearchCriteria filter(FilterItem filterItem, Map<String, ?> params) {
		return parseFromMap(params);
	}

}
