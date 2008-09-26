package org.dcm4chee.xero.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This layout has a number of tabs, of which one is active at a time.
 * @author bwallace
 */
@SuppressWarnings("serial")
public class TabsLayout extends Layout {
   private static final Logger log = LoggerFactory.getLogger(TabsLayout.class);
   
   protected List<Layout> tabs = new ArrayList<Layout>(2);
   protected Layout activeTab;
   
   public TabsLayout() {
	  super("html/tabs");
   }
   
   /** Add tabs and activeTab as getters supported by this method */
   @Override
   public Object get(Object key) {
	  log.info("TabsLayout looking for {}",key);
	  if( "activeTab".equals(key) ) return activeTab;
	  Object ret = super.get(key);
	  log.info("Didn't find direct key {} returning {}",key,ret);
	  return ret;
   }
   
   /** Add tabs and activeTab to the valid keys. */
   @Override
   public boolean containsKey(Object key) {
	  if( "activeTab".equals(key) ) return true;
	  return super.containsKey(key);
   }
   
   /** Add a tab to the tabs layout. */
   public void addTab(String name, Layout lay) {
	  this.add(lay);
	  lay.put("tabName", name);
	  if( activeTab==null ) {
		 activeTab = lay;
		 lay.used = true;
	  }
	  else lay.used = false;
   }
   
   /** Return the currently active/displayed tab. */
   public Layout getActiveTab() {
	  return activeTab;
   }
   
}
