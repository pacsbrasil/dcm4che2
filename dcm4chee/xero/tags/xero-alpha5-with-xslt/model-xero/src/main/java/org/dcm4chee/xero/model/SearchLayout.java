package org.dcm4chee.xero.model;

/** A search box is a search results information layout object - it has to be provided it's
 * parent query and knows how to actually go out and get it's data if it is null.
 * @author bwallace
 */
public class SearchLayout extends Layout {
   XmlModel search;
   XmlModel columns;
   
   public SearchLayout(XmlModel search) {
	  super("search/studyTable");
	  this.search = search;
   }
   
   public XmlModel getSearch() {
	  return search;
   }

   public void setColumns(XmlModel columns) {
	  this.columns = columns;	  
   }
   
   /** Get the columsn to display for this study box. */
   public XmlModel getColumns() {
	  return this.columns;
   }

   /** Indicate that columns and search are both available keys. */
   @Override
   public boolean containsKey(Object key) {
	  if( "columns".equals(key) ) return true;
	  if( "search".equals(key) ) return true;
	  return super.containsKey(key);
   }

   /** Return search and columns as key values, or any other hashmap keys. */
   @Override
   public Object get(Object key) {
	  if( "columns".equals(key) ) return getColumns();
	  if( "search".equals(key) ) return getSearch();
	  return super.get(key);
   }
   
   
}
