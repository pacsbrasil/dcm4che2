package org.dcm4chee.xero.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.model.Layout;
import org.dcm4chee.xero.model.LazyMap;
import org.dcm4chee.xero.model.MapFactory;
import org.dcm4chee.xero.model.QueryBox;
import org.dcm4chee.xero.model.SearchBox;
import org.dcm4chee.xero.model.XmlModel;

/**
 * The QueryController has methods to create/setup the query and search results.
 * 
 * @author bwallace
 *
 */
public class QueryController implements MapFactory, MultiAction {
   
   Map<String,Action> actions = new HashMap<String,Action>();
   
   public QueryController() {
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
	  List<Layout> ret = new ArrayList<Layout>(2);
	  LazyMap query = (LazyMap) src.get("query");
	  QueryBox queryBox = new QueryBox(query);
	  XmlModel search = (XmlModel) src.get("search");
	  SearchBox searchBox = new SearchBox(search);
	  searchBox.setColumns( (XmlModel) src.get("studyColumns"));
	  ret.add(queryBox);
	  ret.add(searchBox);
	  return ret;
   }

   public Map<String, Action> getActions() {
	  return actions;
   }

}
