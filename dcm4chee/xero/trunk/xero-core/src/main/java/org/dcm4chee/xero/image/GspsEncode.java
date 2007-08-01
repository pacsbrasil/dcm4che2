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
package org.dcm4chee.xero.image;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.iod.module.pr.DisplayShutterModule;
import org.dcm4che2.iod.module.pr.GraphicAnnotationModule;
import org.dcm4che2.iod.module.pr.GraphicLayerModule;
import org.dcm4che2.iod.module.pr.GraphicObject;
import org.dcm4che2.iod.module.pr.TextObject;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.svg.CircleType;
import org.w3.svg.ClipPathType;
import org.w3.svg.EllipseType;
import org.w3.svg.GType;
import org.w3.svg.PathType;
import org.w3.svg.RectType;
import org.w3.svg.SvgType;
import org.w3.svg.TextType;
import org.w3.svg.UseType;

/**
 * This class enocdes GSPS as XML in either VML or SVG formats. This class will
 * encode data based on GSPS UID's for all GSPS UID's referenced from ImageBean
 * objects.
 * 
 * There are several types of data that can be encoded:
 * <ol>
 * <li>C.11.12 Presentation State Shutter defines the colour of the shutter.
 * This is encoded as rgb values on the shutter svg object.</li>
 * <li>C.11.13 Presentation State Mask TODO - not encoded right now.</li>
 * <li>C.7.6.10 Mask (subtraction) TODO - not encoded right now.</li>
 * <li>C.7.6.11 Display Shutter is encoded as an svg shape under the study
 * level.</li>
 * <li>C.7.6.15 Bitmap Display Shutter - svg shape referencing WADO url for the
 * bitmap image</li>
 * <li>C.9.2 Overlay Plane and C.11.7 Overlay Activation - encoded as image
 * object.</li>
 * <li>C.10.4 Displayed area - encoded as topLeft, bottomRight on image node it
 * applies to.</li>
 * <li>C.10.5 Graphic Annotation - encoded as shape at study level as a shape.</li>
 * <li>C.10.6 Spatial Transformation - encoded as rotate and flip flags on
 * image.</li>
 * <li>C.10.7 Graphic Layer encoded as use elements to the overlays,
 * annotations etc.</li>
 * <li>C.11.1 Modality LUT - only encoded if it differs from image modality
 * LUT, and if so, encoded as lut=UID of GSPS object on image.</li>
 * <li>C.11.8 VOI LUT - encoded as window level/width, OR, if there is a real
 * VOI LUT in the GSPS that differs from the original image, then encoded as
 * lut=UID as above. </li>
 * <li>C.11.6 Presentation LUT - Encoded as inverse=true/false</li>
 * </ol>
 * 
 * Different parts are encoded by different classes, and the order of encoding
 * is somewhat important. As far as the figuring out what to encode, it is
 * assumed that the objects have gspsUID attached to them by the point that this
 * filter is used.
 * 
 * @author bwallace
 * 
 */
public class GspsEncode implements Filter<ResultsBean> {
   private static final int X_OFFSET = 0;
   private static final int Y_OFFSET = 1;
   private static final int ELLIPSE_MAJOR_X1 = X_OFFSET;
   private static final int ELLIPSE_MAJOR_Y1 = Y_OFFSET;
   private static final int ELLIPSE_MAJOR_X2 = 2+X_OFFSET;
   private static final int ELLIPSE_MAJOR_Y2 = 2+Y_OFFSET;
   private static final int ELLIPSE_MINOR_X1 = 4+X_OFFSET;
   private static final int ELLIPSE_MINOR_X2 = 6+X_OFFSET;

   private static final Logger log = LoggerFactory.getLogger(GspsEncode.class);

   private static final ICC_ColorSpace lab = new ICC_ColorSpace(ICC_Profile.getInstance(ICC_ColorSpace.CS_sRGB));

   public ResultsBean filter(FilterItem filterItem, Map<String, Object> params) {
	  ResultsBean results = (ResultsBean) filterItem.callNextFilter(params);
	  Map<String, List<ImageBean>> gspsUids = new HashMap<String, List<ImageBean>>();
	  if (results == null)
		 return null;
	  initGspsUidsMap(results, gspsUids);
	  if (gspsUids.size() == 0) {
		 log.info("No GSPS UID's referenced for study results.");
		 return results;
	  }
	  for (Map.Entry<String, List<ImageBean>> me : gspsUids.entrySet()) {
		 String gspsUid = me.getKey();
		 List<ImageBean> images = me.getValue();
		 DicomObject dcmobj = readDicomHeader(filterItem, params, gspsUid);
		 if (dcmobj == null) {
			log.warn("Couldn't read GSPS for uid " + gspsUid);
			continue;
		 }
		 GspsType gspsType = addGspsTypeToXml(results, dcmobj);
		 log.debug("Parsing DICOM object.");
		 addMinMaxPixelInfo(dcmobj, images);
		 addAnnotationToResults(dcmobj, gspsType, images);
		 addShutterToResults(dcmobj, gspsType, images);
	  }
	  return results;
   }

   /**
     * Adds annotation results to the gsps object.
     * 
     * @param dcmobj
     * @param gspsType
     * @param images
     */
   @SuppressWarnings("unchecked")
   protected void addAnnotationToResults(DicomObject dcmobj, GspsType gspsType, List<ImageBean> images) {
	  GraphicAnnotationModule[] grans = GraphicAnnotationModule.toGraphicAnnotationModules(dcmobj);
	  // This is type-safe, but can't be case right now because of Java 1.4 restrictions.
	  Map<String,GraphicLayerModule> grals = GraphicLayerModule.toGraphicLayerMap(dcmobj);
	  if (grans == null)
		 return;
	  // TODO - sort this by graphic layer order.
	  for (GraphicAnnotationModule gran : grans) {
		 String graphicLayerName = gran.getGraphicLayer();
		 GraphicLayerModule gral = grals.get(graphicLayerName);
		 GType g = getG(gspsType, gral.getGraphicLayer());
		 String rgb = toRGB(gral.getGraphicLayerRecommendedDisplayGrayscaleValue(), gral.getFloatLab(), gral.getGraphicLayerRecommendedDisplayRGBValueRET());
		 log.info("Graphic layer recommended display grayscale value is "+gral.getGraphicLayerRecommendedDisplayGrayscaleValue() + " rgb is "+rgb
			   + " for layer "+gral.getGraphicLayer()+" description "+gral.getGraphicLayerDescription());
		 // To not fill, over-ride these values in children.
		 g.setStyle("fill: "+rgb+"; stroke: "+rgb+";" );
		 GraphicObject[] gos = gran.getGraphicObjects();
		 if (gos != null) {
			for (GraphicObject go : gos) {
			   addGraphicObject(g, go);
			}
		 }

		 TextObject[] txos = gran.getTextObjects();
		 if (txos != null) {
			for (TextObject txo : txos) {
			   addTextObject(g, txo);
			}
		 }

		 // TODO - check to see which objects to add this to - only add them
            // to specified images.
		 UseType useG = new UseType();
		 useG.setHref("#" + g.getId());
		 for (ImageBean image : images) {
			image.getUse().add(useG);
		 }
	  }
   }

   /** Adds the text object txo to svg:g. 
    * TODO add child elements for bounding box and anchor point display.
    */
   protected void addTextObject(GType g, TextObject txo) {
	  TextType text = new TextType();
	  String unformatted = txo.getUnformattedTextValue();
	  text.setContent(unformatted);
	  // TODO handle multi-line text blocks.
	  int lineLen = unformatted.length();
	  if(lineLen==0) return;
	  
	  float[] topLeft = txo.getBoundingBoxTopLeftHandCorner();
	  if( topLeft!=null ) {
		 float[] bottomRight = txo.getBoundingBoxBottomRightHandCorner();
		 float x = topLeft[X_OFFSET];
		 float y = topLeft[Y_OFFSET];
		 text.setX(Float.toString(topLeft[X_OFFSET]));
		 text.setY(Float.toString(topLeft[Y_OFFSET]));
		 int rotation = toRotation(topLeft, bottomRight);
		 if( rotation!=0 ) text.setTransform("rotate("+rotation+","+x+','+y+")");
		 float xlen = Math.abs(bottomRight[X_OFFSET] - topLeft[X_OFFSET]);
		 // At least 5 pixels are needed, otherwise don't bother trying to figure out font size.
		 if( xlen>5 ) text.setFontSize(Float.toString(xlen*2 / lineLen));
	  } else {
 	     float[] anchor = txo.getAnchorPoint();
		 text.setX(Float.toString(anchor[X_OFFSET]));
		 text.setY(Float.toString(anchor[Y_OFFSET]));
		 // Specify some default font size.
		 text.setFontSize("12");
	  }
	  g.getChildren().add(text);
   }

   /**
     * Adds the graphic object go to svg:g. There are 5 subtypes of graphic
     * objects: POINT, POLYLINE, INTERPOLATED, CIRCLE and ELLIPSE Each one of
     * these needs to be handled separately.
     */
   protected void addGraphicObject(GType g, GraphicObject go) {
	  String type = go.getGraphicType();
	  if( type.equalsIgnoreCase(GraphicObject.POLYLINE) ) {
		 addPolyline(g, go);
	  } else if( type.equalsIgnoreCase(GraphicObject.CIRCLE) ) {
		 addCircle(g,go);
	  } else if( type.equalsIgnoreCase(GraphicObject.ELLIPSE)) {
		 addEllipse(g,go);
	  } else if( type.equalsIgnoreCase(GraphicObject.POINT)) {
		 addPoint(g,go);
	  } else if( type.equalsIgnoreCase(GraphicObject.INTERPOLATED)) {
		 // TODO replace this with something better.
		 addPolyline(g,go);
	  } else {
		 log.warn("Unsupported graphic object type:" + type);
	  }
   }

   /** Adds an ellipse SVG to the graphic object */
   protected void addEllipse(GType g, GraphicObject go) {
	  float[] points = go.getGraphicData();
	  if (points == null || points.length != 8) {
		 log.warn("Invalid ellipse data provided in graphic object.");
		 return;
	  }
	  EllipseType ellipse = new EllipseType();
	  ellipse.setCx(Float.toString((points[ELLIPSE_MAJOR_X1]+points[ELLIPSE_MAJOR_X2])/2));
	  ellipse.setCy(Float.toString((points[ELLIPSE_MAJOR_Y1]+points[ELLIPSE_MAJOR_Y2])/2));
	  ellipse.setRx(Float.toString(length(points,ELLIPSE_MAJOR_X1,ELLIPSE_MAJOR_X2)/2));
	  ellipse.setRy(Float.toString(length(points,ELLIPSE_MINOR_X1,ELLIPSE_MINOR_X2)/2));
	  if( !go.getGraphicFilled() ) {
		 ellipse.setStyle("fill: none;");
	  }
	  g.getChildren().add(ellipse);
   }
   
   /** Converts a bounding box to a rotation amount.   Returns a multiple of 90, as that is the only
    * bounding information that we can tell from just the bounding box. */
   public static int toRotation(float[] topLeft, float[] bottomRight) {
	  float x1 = topLeft[X_OFFSET];
	  float x2 = bottomRight[X_OFFSET];
	  float y1 = topLeft[Y_OFFSET];
	  float y2 = bottomRight[Y_OFFSET];
	  if( x1 < x2 ) {
		 if( y1<y2 ) {
			return 0;
		 }
		 return 90;
	  } else {
		 if( y1 < y2 ) {
			return 180;
		 }
		 return 270;
	  }
   }
   
   /** Returns the length between the points in data specified by p1 and p2. */
   public static float length(float[] data, int p1, int p2) {
	  float dx = data[p1] - data[p2];
	  float dy = data[p1+1] - data[p2+1];
	  return (float) Math.sqrt(dx*dx+dy*dy);
   }

   /** Adds a point SVG to the graphic object, as a circle of radius 0.5 centered on the given position. */
   protected void addPoint(GType g, GraphicObject go) {
	  float[] points = go.getGraphicData();
	  if (points == null || points.length != 2) {
		 log.warn("Invalid point data provided in graphic object.");
		 return;
	  }
	  CircleType circle = new CircleType();
	  circle.setCx(Float.toString(points[0]));
	  circle.setCy(Float.toString(points[1]));
	  circle.setR("0.5");
 	  circle.setStyle("fill: none;");
	  g.getChildren().add(circle);
   }

   /** Adds a circle SVG to the graphic object */
   protected void addCircle(GType g, GraphicObject go) {
	  float[] points = go.getGraphicData();
	  if (points == null || points.length != 4) {
		 log.warn("Invalid circle data provided in graphic object.");
		 return;
	  }
	  CircleType circle = new CircleType();
	  circle.setCx(Float.toString(points[0]));
	  circle.setCy(Float.toString(points[1]));
	  float r = length(points,0,2);
	  circle.setR(Float.toString(r));
	  if( !go.getGraphicFilled() ) {
		 circle.setStyle("fill: none;");
	  }
	  g.getChildren().add(circle);
   }
   

   /**
     * Adds a polyline graphic object as a child of g.
     * 
     * @param g
     *            to add the svg to
     * @param go
     *            to add the polyline from.
     */
   protected void addPolyline(GType g, GraphicObject go) {
	  float[] points = go.getGraphicData();
	  if (points == null || points.length < 2) {
		 log.warn("Invalid polyline provided in graphic object.");
		 return;
	  }
	  PathType path = new PathType();
	  StringBuffer d = new StringBuffer();
	  d.append("M ").append(points[0]).append(' ').append(points[1]);
 	  d.append(" L");
	  // Start at 3 so that if the length happens to be odd, we don't throw an
        // exception, but just ignore the extra value.
	  for (int i = 3; i < points.length; i += 2) {
		 // Note the order is x,y NOT y,x as it is for shutters (shudder)
		 d.append(' ').append(points[i - 1]).append(' ').append(points[i]);
	  }
	  path.setD(d.toString());
	  if( !go.getGraphicFilled() ) {
		 path.setStyle("fill: none;");
	  }
	  g.getChildren().add(path);
   }

   /**
     * Adds min/max pixel information from the GSPS instead of from the image
     * header.
     */
   protected void addMinMaxPixelInfo(DicomObject dcmobj, List<ImageBean> images) {
	  for (ImageBean ib : images) {
		 MinMaxPixelInfo.updatePixelRange(dcmobj, ib);
	  }
   }

   /**
     * Adds the GSPS xml object, returning the instance created/added.
     * 
     * @param results
     * @param dcmobj
     * @return
     */
   protected GspsType addGspsTypeToXml(ResultsBean results, DicomObject dcmobj) {
	  results.addResult(dcmobj);
	  GspsType gspsType = (GspsType) results.getChildren().get(dcmobj.getString(Tag.SOPInstanceUID));
	  if (gspsType == null)
		 throw new NullPointerException("Something went wrong in adding results object - null gspsType.");
	  return gspsType;
   }

   /**
     * This method causes all the shutter objects to be applied to every
     * applicable image. Makes the assumption that the image dimensions are the
     * same for every image. This isn't necessarily a good assumption, but
     * should be good enough for now. It could be tested explicitly if required.
     * 
     * @param dcmobj
     * @param gspsType
     * @param images
     */
   protected void addShutterToResults(DicomObject dcmobj, GspsType gspsType, List<ImageBean> images) {
	  if (images == null || images.size() == 0)
		 return;
	  ImageBean exemplar = images.get(0);
	  String width = exemplar.getColumns().toString();
	  String height = exemplar.getRows().toString();
	  DisplayShutterModule shutter = new DisplayShutterModule(dcmobj);
	  String[] shapes = shutter.getShutterShapes();
	  if (shapes == null || shapes.length == 0) {
		 log.info("No display shutters.");
		 return;
	  }
	  GType g = getG(gspsType, "shutter");
	  for (int i = 0; i < shapes.length; i++) {
		 String shape = shapes[i];
		 if (shape.equalsIgnoreCase("CIRCULAR")) {
			addCircularShutter(gspsType, shutter, g, width, height);
		 } else if (shape.equalsIgnoreCase("POLYGONAL")) {
			addPolygonalShutter(gspsType, shutter, false, g, width, height);
		 } else if (shape.equalsIgnoreCase("RECTANGULAR")) {
			addPolygonalShutter(gspsType, shutter, true, g, width, height);
		 } else if (shape.equalsIgnoreCase("BITMAP")) {
			log.warn("Can't handle bitmap shutters yet.");
		 } else {
			log.warn("Unknown shutter shape:" + shape + " on " + dcmobj.getString(Tag.SOPInstanceUID));
		 }
	  }
	  UseType useG = new UseType();
	  useG.setHref("#" + g.getId());
	  for (ImageBean image : images) {
		 image.getUse().add(useG);
	  }
   }

   /**
     * Adds a rectangular shutter to the group g provided. Defines the clip path
     * inside the svg child of gspsType.
     * 
     * @param gspsType
     *            to add the clip path to.
     * @param shutter
     *            information
     * @param useRect
     *            to use the rectangle as the polygon.
     * @param g
     *            to add the shutter to.
     * @param width
     *            of the image to apply to.
     * @param height
     *            of the image to apply to.
     */
   protected void addPolygonalShutter(GspsType gspsType, DisplayShutterModule shutter, boolean useRect, GType g, String width,
		 String height) {
	  log.info("Found a rectangular shutter.");
	  ClipPathType clip = new ClipPathType();
	  clip.setId(useRect ? "rectangularClipPath" : "polygonalClipPath");
	  clip.setClipPathUnits("userSpaceOnUse");
	  getSvg(gspsType).getChildren().add(clip);
	  PathType path = new PathType();
	  clip.getChildren().add(path);
	  path.setStyle("clip-rule: evenodd;");
	  // Bounding box first
	  StringBuffer sbd = new StringBuffer("M 0 0");
	  sbd.append(" h ").append(width);
	  sbd.append(" v ").append(height);
	  sbd.append(" h -").append(width);
	  sbd.append(" Z");
	  // Then remove the polygon in the center
	  if (useRect) {
		 sbd.append(" M ").append(shutter.getShutterLeftVerticalEdge()).append(' ').append(shutter.getShutterUpperHorizontalEdge());
		 sbd.append(" L ").append(shutter.getShutterRightVerticalEdge()).append(' ')
			   .append(shutter.getShutterUpperHorizontalEdge());
		 sbd.append(" ").append(shutter.getShutterRightVerticalEdge()).append(' ').append(shutter.getShutterLowerHorizontalEdge());
		 sbd.append(" ").append(shutter.getShutterLeftVerticalEdge()).append(' ').append(shutter.getShutterLowerHorizontalEdge());
	  } else {
		 int[] vertices = shutter.getVerticesOfThePolygonalShutter();
		 sbd.append(" M ").append(vertices[0]).append(' ');
		 sbd.append(vertices[1]);
		 sbd.append(" L");
		 for (int i = 3; i < vertices.length; i += 2) {
			sbd.append(' ').append(vertices[i]).append(' ').append(vertices[i - 1]);
		 }
	  }
	  sbd.append(" Z");
	  path.setD(sbd.toString());
	  log.info("Set clip path to " + sbd);

	  RectType rect = new RectType();
	  rect = new RectType();
	  rect.setWidth(width);
	  rect.setHeight(height);
	  String rgb = toRGB(shutter.getShutterPresentationValue(), shutter.getFloatLab(),null);
	  rect.setStyle("clip-path:url(#" + clip.getId() + "); fill: " + rgb + ";");
	  rect.setClipPath("url(#" + clip.getId() + ")");
	  g.getChildren().add(rect);
   }

   /**
     * Adds a circular shutter to the group g provided. Defines the clip path
     * inside the svg child of gspsType.
     * 
     * @param gspsType
     *            to add the clip path to.
     * @param shutter
     *            information
     * @param g
     *            to add the shutter to.
     * @param width
     *            of the image to apply to.
     * @param height
     *            of the image to apply to.
     */
   protected void addCircularShutter(GspsType gspsType, DisplayShutterModule shutter, GType g, String width, String height) {
	  log.info("Found a circular shutter.");
	  int[] center = shutter.getCenterOfCircularShutter();
	  int radius = shutter.getRadiusOfCircularShutter();
	  if (center == null || radius == 0.0 || center.length != 2) {
		 log.warn("Invalid circular shutter.");
		 return;
	  }
	  log.info("Adding circular shutter to object.");
	  ClipPathType clip = new ClipPathType();
	  clip.setId("circularClipPath");
	  clip.setClipPathUnits("userSpaceOnUse");
	  getSvg(gspsType).getChildren().add(clip);
	  GType combinedClip = new GType();
	  combinedClip.setStyle("clip-rule: evenodd;");

	  clip.getChildren().add(combinedClip);
	  RectType rect = new RectType();
	  rect.setWidth(width);
	  rect.setHeight(height);
	  combinedClip.getChildren().add(rect);
	  CircleType circle = new CircleType();
	  circle.setCx(Integer.toString(center[0]));
	  circle.setCy(Integer.toString(center[1]));
	  circle.setR(Integer.toString(radius - 1));
	  combinedClip.getChildren().add(circle);
	  rect = new RectType();
	  rect.setWidth(width);
	  rect.setHeight(height);
	  String rgb = toRGB(shutter.getShutterPresentationValue(), shutter.getFloatLab(),null);
	  rect.setStyle("clip-path:url(#" + clip.getId() + "); fill: " + rgb + ";");
	  rect.setClipPath("url(#" + clip.getId() + ")");
	  g.getChildren().add(rect);
   }

   /**
     * Converts the gray or colour values to RGB values . TODO implement this
     * correctly.
     * 
     * @return String representation of the colour.
     */
   public static String toRGB(int pGray, float[] labColour, int[] rgbColour) {
	  int r, g, b;
	  if (labColour != null) {
		 if (lab == null) {
			r = g = b = (int) (labColour[0] * 2.55f);
			log.warn("Converting L*a*b* colour using only L* component as lab colour space not defined.");
		 } else {
			float[] rgb = lab.toRGB(labColour);
			r = (int) (rgb[0] * 255);
			g = (int) (rgb[1] * 255);
			b = (int) (rgb[2] * 255);
			log.info("L*a*b* " + labColour[0] + "," + labColour[1] + "," + labColour[2] + " sRGB " + r + "," + g + "," + b
				  +" colour space type id "+lab.getType()); 
		 }
	  } else if( rgbColour!=null ) {
		 r = rgbColour[0];
		 g = rgbColour[1];
		 b = rgbColour[2];
	  } else {
		 r = g = b = (pGray >> 8);
	  }
	  int conv = (r << 16) | (g << 8) | b | 0x1000000;
	  String ret = "#" + Integer.toHexString(conv).substring(1);
	  log.info("Returning colour " + ret);
	  return ret;
   }

   /** Creates an instance of a grouping object. */
   protected GType getG(GspsType gspsType, String name) {
	  SvgType svg = getSvg(gspsType);
	  GType g = new GType();
	  // TODO make this name unique in a standardized fashion.
	  g.setId(name);
	  svg.getChildren().add(g);
	  return g;
   }
   
   /**
     * Gets the defs element to be used to add a new child to the gsps display
     * information
     */
   protected SvgType getSvg(GspsType gspsType) {
	  SvgType svg = gspsType.getSvg();
	  if (svg == null) {
		 svg = new SvgType();
		 svg.setWidth("0");
		 svg.setHeight("0");
		 gspsType.setSvg(svg);
	  }
	  return svg;
   }

   /** Reads the dicom header for the specified SOP Instance UID and returns it. */
   public static DicomObject readDicomHeader(FilterItem filterItem, Map<String, Object> params, String uid) {
	  // This might come from a different series or even study, so don't
	  // assume anything here.
	  Map<String, Object> newParams = new HashMap<String, Object>();
	  newParams.put("objectUID", uid);
	  return (DicomObject) filterItem.callNamedFilter("dicom", newParams);
   }

   /**
     * Initialize the map of gspsUids from the results, where every gsps uid in
     * an image gets mapped to the list of image beans containing that image.
     * 
     * @param results
     * @param gspsUids
     */
   private void initGspsUidsMap(ResultsBean results, Map<String, List<ImageBean>> gspsUids) {
	  for (PatientType pt : results.getPatient()) {
		 for (StudyType st : pt.getStudy()) {
			for (SeriesType se : st.getSeries()) {
			   for (DicomObjectType dot : se.getDicomObject()) {
				  if (!(dot instanceof ImageBean))
					 continue;
				  ImageBean image = (ImageBean) dot;
				  if (image.getGspsUID() != null) {
					 List<ImageBean> l = gspsUids.get(image.getGspsUID());
					 if (l == null) {
						l = new ArrayList<ImageBean>();
						gspsUids.put(image.getGspsUID(), l);
					 }
					 l.add(image);
					 log.info("Image " + image.getSOPInstanceUID() + " references GSPS " + image.getGspsUID());
				  }
			   }
			}
		 }
	  }
   }
}
