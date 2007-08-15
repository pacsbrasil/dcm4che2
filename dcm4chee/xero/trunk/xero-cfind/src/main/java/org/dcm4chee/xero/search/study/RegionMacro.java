package org.dcm4chee.xero.search.study;

import java.util.Map;

import javax.xml.namespace.QName;

public class RegionMacro implements Macro {
   public static final QName Q_PRESENTATION_SIZE_MODE = new QName(null, "size");
   public static final QName Q_TOP_LEFT = new QName(null,"topLeft");
   public static final QName Q_BOTTOM_RIGHT = new QName(null,"bottomRight");
   public static final QName Q_MAGNIFY = new QName(null,"magnify");
   private PresentationSizeMode presentationSizeMode;
   private String topLeft, bottomRight;
   private float magnify;
   
   public RegionMacro(PresentationSizeMode presentationSizeMode, String topLeft, String bottomRight, float magnify) {
	  this.presentationSizeMode = presentationSizeMode;
	  this.topLeft = topLeft;
	  this.bottomRight = bottomRight;
	  this.magnify = magnify;
   }
   
   public int updateAny(Map<QName, String> attrs) {
	 attrs.put(Q_PRESENTATION_SIZE_MODE, presentationSizeMode.toString());
	 attrs.put(Q_TOP_LEFT, topLeft);
	 attrs.put(Q_BOTTOM_RIGHT, bottomRight);
	 attrs.put(Q_MAGNIFY,Float.toString(magnify));
	 return 4;
   }

}
