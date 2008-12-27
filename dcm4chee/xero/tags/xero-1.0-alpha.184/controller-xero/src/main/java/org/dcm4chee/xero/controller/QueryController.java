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
package org.dcm4chee.xero.controller;

import java.util.List;
import java.util.Map;

import javax.xml.transform.URIResolver;

import org.dcm4chee.xero.metadata.access.LazyMap;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.model.XmlModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The QueryController has methods to create/setup the query and search results.
 * 
 * @author bwallace
 * 
 */
public class QueryController<T> implements Filter<T> {

	private static final Logger log = LoggerFactory.getLogger(QueryController.class);

	/**
	 * Handle the creation of the query box. Does this by validating the query
	 * parameters, copying the parameters into the URL for the query, and then
	 * generating the overall layout/query to use.
	 */
	public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
		Map<String, Object> model = FilterUtil.getModel(params);
		log.info("QueryController is creating the layout with query URL and search XML results.");
		assertNotNull(model,"js");
		assertNotNull(model,"js.model");
		Map<String,Object> idMap = (Map<String, Object>) assertNotNull(model,"js.model.idMap");
		for(Map.Entry<String,Object> me : idMap.entrySet() ) {
			log.info("idMap entry {}", me.getKey());
		}
		Map<String,Object> queryBox = assertNotNull(model, "js.model.idMap.QueryLayout");
		Map<String,Object> searchLayout = assertNotNull(model, "js.model.idMap.SearchLayout");

		LazyMap query = (LazyMap) model.get("query");

		// Use the Java version of the query - this takes parameters from the URL
		queryBox.put("query", query);

		// Accessing search here ensures the controller reads the XML, not the
		// view
		URIResolver resolver = FilterUtil.getURIResolver(params);
		String url = FilterUtil.getString(query, "url");
		String ae = (String) model.get(AESelector.AE);
		if( ae!=null ) {
		   url = url+ae;
		   log.info("Querying ae {}", ae);
		} else log.info("Not querying with a specific AE");
		log.info("URL to query for:{}",url);
		XmlModel search;
		search = new XmlModel(resolver, url);
		manyStudy(search);
		model.put("search",search);
		searchLayout.put("search",search);

		return filterItem.callNextFilter(params);
	}

	/** Sets up the hasMany patient level attribute */
	@SuppressWarnings("unchecked")
   private void manyStudy(XmlModel search) {
	   List<XmlModel> patients = (List<XmlModel>) search.get("patient");
	   if( patients==null ) return;
	   for(XmlModel patient : patients) {
	      List<?> studies = (List<?>) patient.get("study");
	      if( studies.size()>2 ) patient.put("manyStudy", true);
	   }
    }

   /** Gets the given map isn't null */
	public static Map<String,Object> assertNotNull(Map<?,?> map, String key) {
		Map<String,Object> ret = FilterUtil.getMap(map, key);
		if( ret==null ) throw new NullPointerException("The path "+key+" was null.");
		return ret;
	}
}
