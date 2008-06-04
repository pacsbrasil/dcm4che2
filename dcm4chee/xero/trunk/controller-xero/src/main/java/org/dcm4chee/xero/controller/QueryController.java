package org.dcm4chee.xero.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.model.Layout;
import org.dcm4chee.xero.model.LazyMap;
import org.dcm4chee.xero.model.MapFactory;
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
public class QueryController implements MapFactory, MultiAction {
   private static final Logger log = LoggerFactory.getLogger(QueryController.class);
   Map<String,Action> actions = new HashMap<String,Action>();
   
   public QueryController() {
	  log.info("Instantiated a QueryController to manage the Query Layout.");
	  actions.put("displayQuery", new DisplayQueryAction());
   }
   
   /** Display the given query results */
   class DisplayQueryAction implements Action {
	  public Map<String, Object> action(Map<String, Object> model) {
		 return model;
	  }	  
   };

   /** Handle the creation of the query box - only happens if no action is run. */
   public Object create(Map<String, Object> src) {
	  TabsLayout tabs = new TabsLayout();
	  Layout findLayout = new Layout();
	  
	  LazyMap query = (LazyMap) src.get("query");
	  QueryLayout queryBox = new QueryLayout(query);
	  XmlModel search = (XmlModel) src.get("search");
	  SearchLayout searchLayout = new SearchLayout(search);
	  searchLayout.setColumns( (XmlModel) src.get("studyColumns"));

	  findLayout.add(queryBox);
	  findLayout.add(searchLayout);
	  tabs.addTab("Find", findLayout);
	  
	  tabs.addTab("Display", new Layout());
	  log.info("Returning a TabsLayout object.");
	  return tabs;
   }

   public Map<String, Action> getActions() {
	  return actions;
   }

   public String toString() {
	  return "QueryController";
   }
}
