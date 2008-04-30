package org.dcm4chee.xero.model;

/** A search box is a search results information layout object - it has to be provided it's
 * parent query and knows how to actually go out and get it's data if it is null.
 * @author bwallace
 */
public class SearchBox extends Layout {
   XmlModel search;
   XmlModel columns;
   
   public SearchBox(XmlModel search) {
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
}
