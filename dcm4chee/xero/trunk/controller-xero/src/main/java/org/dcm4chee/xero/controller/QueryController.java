package org.dcm4chee.xero.controller;

import java.util.Map;

import javax.xml.transform.URIResolver;

import org.dcm4chee.xero.metadata.access.LazyMap;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.model.Layout;
import org.dcm4chee.xero.model.QueryLayout;
import org.dcm4chee.xero.model.SearchLayout;
import org.dcm4chee.xero.model.TabsLayout;
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
		TabsLayout tabs = new TabsLayout();
		Layout findLayout = new Layout();

		LazyMap query = (LazyMap) model.get("query");

		QueryLayout queryBox = new QueryLayout(query);
		// Accessing search here ensures the controller reads the XML, not the
		// view
		URIResolver resolver = FilterUtil.getURIResolver(params);
		String url = FilterUtil.getString(query, "url");
		XmlModel search;
		search = new XmlModel(resolver, url);
		model.put("search",search);
		SearchLayout searchLayout = new SearchLayout(search);
		searchLayout.setColumns((XmlModel) model.get("studyColumns"));

		findLayout.add(queryBox);
		findLayout.add(searchLayout);
		tabs.addTab("Find", findLayout);

		tabs.addTab("Display", new Layout());
		tabs.put("tabMenu", new Layout("userMenu"));

		model.put("layout", tabs);

		return filterItem.callNextFilter(params);
	}

}
