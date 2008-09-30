package org.dcm4chee.xero.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a view layout - a container that may have children, a size, view type and possibly a
 * child content object.
 * @author bwallace
 *
 */
public class View {
   /** The actual size when rendered of the overall component - typically recomputed to fit in the overall size. */
   int actualSizeX = 1024, actualSizeY=768;
   
   int sizeX=100, sizeY=100;
   
   enum SizeType { FIXED, STRETCH };
   SizeType sizeTypeX=SizeType.STRETCH, sizeTypeY=SizeType.STRETCH;
   
   List<View> children;
      
   /** Use a view box to nest children in a way that treats the entire box as 1 area for sizing purposes,
    * eg to allow spanning the box over multiple parent elements.
    */
   public static final String VIEW_BOX = "html/box";
   
   /** Use an end of row to start a new line - used to find size etc information.
    */
   public static final String END_OF_ROW = "html/endOfRow";
   
   String viewType = VIEW_BOX;
   
   public View() {
   }
   
   public View(View parent) {
	  parent.addChild(this);
   }
   

   /**
    * Records information about the row/column sizes.
    * @author bwallace
    *
    */
   static class SizeInfo {
	  int rowHeightVary, rowHeightFix, rowLengthVary, rowLengthFix;
	  
	  public void addInfo(View v) {
		 if( v.sizeTypeX== SizeType.FIXED ) {
			rowLengthFix += v.sizeX;
		 }
		 else {
			rowLengthVary += v.sizeX;
		 }
		 if( rowHeightFix!=0 || v.sizeTypeY==SizeType.FIXED ) {
			rowHeightFix = Math.max(rowHeightFix, v.sizeY);
			rowHeightVary = 0;
		 }
		 else {
			rowHeightVary = Math.max(rowHeightVary,v.sizeY);
		 }
	  }
   }
   
   /** Setup the sizes of the child elements.
    */
   public void init() {
	  if( children==null || children.size()==0 ) return;
	  int n=children.size();
	  SizeInfo si = new SizeInfo();
	  List<SizeInfo> sizes = new ArrayList<SizeInfo>();
	  int rowHeightFix = 0;
	  int rowHeightVary = 0;
	  for(int i=0; i<n; i++) {
		 View child = children.get(i);
		 if( child.viewType==END_OF_ROW || i==n-1) {
			sizes.add(si);
			rowHeightFix += si.rowHeightFix;
			rowHeightVary += si.rowHeightVary;
			si = new SizeInfo();
		 }
	  }

	  int sp = 0;
	  si = sizes.get(sp++);
	  int rowHeight = si.rowHeightFix;
	  if( rowHeight==0 ) rowHeight = ((actualSizeX-rowHeightFix) * si.rowHeightVary)/rowHeightVary;
	  for(int i=0; i<n; i++) {
		 View child = children.get(i);
		 if( child.viewType==END_OF_ROW ) {
			si = sizes.get(sp++);
			rowHeight = si.rowHeightFix;
			if( rowHeight==0 ) rowHeight = ((actualSizeY-rowHeightFix) * si.rowHeightVary)/rowHeightVary;
		 }
		 else {
			int width = child.sizeX;
			if( child.sizeTypeX==SizeType.STRETCH ) {
			   width = ((actualSizeY-si.rowLengthFix)*width)/si.rowLengthVary;
			}
			child.setActualSizeX(width);
			child.setActualSizeY(rowHeight);
		 }
		 child.init();
	  }
   }
   
   /** Creates a standard horizontal box as a child of the given parent */
   public static void createEndOfRow(View parent) {
	  View view = new View();
	  view.viewType = END_OF_ROW;
	  parent.children.add(view);
   }

   public int getSizeX() {
      return sizeX;
   }

   public void setSizeX(int sizeX) {
      this.sizeX = sizeX;
   }

   public int getSizeY() {
      return sizeY;
   }

   public void setSizeY(int sizeY) {
      this.sizeY = sizeY;
   }

   public SizeType getSizeTypeX() {
      return sizeTypeX;
   }

   public void setSizeTypeX(SizeType sizeTypeX) {
      this.sizeTypeX = sizeTypeX;
   }

   public SizeType getSizeTypeY() {
      return sizeTypeY;
   }

   public void setSizeTypeY(SizeType sizeTypeY) {
      this.sizeTypeY = sizeTypeY;
   }

   public String getViewType() {
      return viewType;
   }

   public void setViewType(String viewType) {
      this.viewType = viewType;
   }

   public List<View> getChildren() {
      return children;
   }
   
   /**
    * Returns the model data to display for this object.
    * @return object to display in this box.  Returns null by default unless overridden.
    */
   public Object getModel() {
	  return null;
   }
   
   public void addChild(View child) {
	  if( this.children==null ) {
		 children = new ArrayList<View>();
	  }
	  children.add(child);
   }

   public int getActualSizeX() {
      return actualSizeX;
   }

   public void setActualSizeX(int actualSizeX) {
      this.actualSizeX = actualSizeX;
   }

   public int getActualSizeY() {
      return actualSizeY;
   }

   public void setActualSizeY(int actualSizeY) {
      this.actualSizeY = actualSizeY;
   }
}

