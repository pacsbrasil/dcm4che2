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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The SearchCriteria defines the specific values for a search, including
 * sorting information and any multi-valued or custom searches.  
 * @author bwallace
 */
@XmlRootElement(name="searchCriteria")
public class SearchCriteria extends SearchCriteriaType
{
	/**
	 * This value is required every time except on initial creation, 
	 */
	@XmlTransient
	Map<String, TableColumn> searchColumnsMap;
	
	public SearchCriteria(Map<String,TableColumn> searchColumnsMap)
	{		
		this.searchColumnsMap = searchColumnsMap;
	}
	
	/** Create a search criteria with no search values initially */
	public SearchCriteria() {
		searchColumnsMap = new HashMap<String,TableColumn>();
	}
	
	/**
	 * @param attributeName is the attribute being looked up.
	 * @return The table column object with the given attribute name
	 */
	protected TableColumn lookupColumn(String key)
	{
		return searchColumnsMap.get(key);
	}

	/**
	 * Return true if the search conditions match object.
	 * @param object to be matched.
	 */
	public boolean matches(Object object) {
		if ( object==null ) return false;
		Map<String,TableColumn> map = getAttributeByName();
		for( Map.Entry<String,TableColumn> entry : map.entrySet() )
		{
			String key = entry.getKey();
			TableColumn tc = entry.getValue();
			if ( ! tc.isSearchable() ) throw new RuntimeException("Column "+key+" exists, but isn't searchable.");
			Object objectValue = tc.readValue(object);
			if ( ! tc.matches(objectValue) )
			{
				return false;
			}
		}
		return true;
	}

	/** Appends the value as a parameter, adding & and encoding any illegal
	 * values appropriately for URL get encoding.
	 * @param sb
	 * @param key
	 * @param value
	 */
	protected void append(StringBuffer sb, String key, String value) {
		if( value==null || value.length()==0 ) return;
	    if( sb.length()!=0 ) sb.append('&');
	    sb.append(key).append('=').append(value);
	}
	
	/** Get the search criteria as a set of URL parameters, appropriate for
	 * decoding with the regular SearchCriteriaParser object.
	 * @return
	 */
	public String getURLParameters() {		
		StringBuffer sb = new StringBuffer();
		Map<String,TableColumn> map = getAttributeByName();
		for(Map.Entry<String,TableColumn> me : map.entrySet()) {
			String key = me.getKey();
			TableColumn tc = me.getValue();
			for(Object tcval : tc.getContent()) {
				if( tcval instanceof String ) {
					append(sb,key,(String) tcval);					
				}
				else {
					throw new UnsupportedOperationException("Can't append anything other than simple strings yet.");
				}
			}
		}
		return sb.toString();
	}

	public String getXml() {
		try {
			JAXBContext context = JAXBContext.newInstance(org.dcm4chee.xero.search.SearchCriteria.class);
			Marshaller marshaller = context.createMarshaller();
			StringWriter sw = new StringWriter();
  		    attribute = new ArrayList<AttributeType>(getAttributeByName().values());
			marshaller.marshal(this, sw);
			String ret = sw.toString();
			int index = ret.indexOf("?>");
			if( index<0 ) return ret;
			return ret.substring(index+2);
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Get a map of the attributes, provided their names. */
	public Map<String, TableColumn> getAttributeByName() {
		return searchColumnsMap;
	}

	/**
	 * Returns true if all search criteria are empty, ie there are no
	 * specified criteria.
	 * @return
	 */
	public boolean getEmpty() {
		if( searchColumnsMap.isEmpty() ) return true;
		for(TableColumn tc : searchColumnsMap.values() ) {
			if( !tc.isEmpty() ) return false;
		}
		return true;
	}

	public String getEmptyStr() {
		return Boolean.toString(getEmpty());
	}

}
