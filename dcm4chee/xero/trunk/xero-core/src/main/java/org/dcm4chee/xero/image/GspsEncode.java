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

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.iod.module.macro.ImageSOPInstanceReference;
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
import org.dcm4chee.xero.search.study.ImageBeanMultiFrame;
import org.dcm4chee.xero.search.study.MacroItems;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.svg.GType;
import org.w3.svg.PathType;
import org.w3.svg.SvgType;
import org.w3.svg.TextType;
import org.w3.svg.Use;

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

   private static final int ELLIPSE_MAJOR_X2 = 2 + X_OFFSET;

   private static final int ELLIPSE_MAJOR_Y2 = 2 + Y_OFFSET;

   private static final int ELLIPSE_MINOR_X1 = 4 + X_OFFSET;

   private static final int ELLIPSE_MINOR_X2 = 6 + X_OFFSET;

   private static final Logger log = LoggerFactory.getLogger(GspsEncode.class);

   private static final ICC_ColorSpace lab = new ICC_ColorSpace(ICC_Profile.getInstance(ICC_ColorSpace.CS_sRGB));

   /**
     * This class adds information about the GSPS objects to the filter results.
     * The GSPS objects that are read/included are the set of distinct GSPS
     * objects referenced from images included in the return set, not already
     * parsed. The CHANGES are made to the original study, NOT the current
     * study. The original study is retrieved via getOriginalStudy on the
     * StudyBean object, and consist of: 1. Inclusion of the GSPS
     * series/objects. 2. Inclusion of the SVG objects for the series
     * information. 3. Changes to image/frame level data for window levels (VOI,
     * Modality and Presentation) 4. Changes to study/series/image level data
     * for region information (RegionMacro), and size/rotation.
     * 
     * Currently, an image will have been considered to be processed if it
     * contains a MinMaxPixelMacro. This could be problematic if this
     * information can ever be read from the CFIND response (eg if the C-FIND
     * response contained the Modality LUT rescale slope/intercept information
     * and there was custom processing to use that version instead of reading
     * the image header.) This header is created both by GSPS (first), and
     * secondly, if no GSPS is applicable, by reading the image header.
     * 
     * The reason for splitting the work up this way is that it minimizes the
     * number of GSPS objects read on any request to only those actually
     * required to process the request, while also only doing the work of
     * processing a GSPS once per series (assuming series level requests - which
     * is the norm for most of this work.) Given that, GSPS objects should not
     * be cached for very long, if at all, and not at all if they only apply to
     * one series.
     */
   public ResultsBean filter(FilterItem filterItem, Map<String, Object> params) {
	  ResultsBean results = (ResultsBean) filterItem.callNextFilter(params);
	  long startTime = System.currentTimeMillis();

	  if (results == null)
		 return null;

	  Map<String, StudyBean> gspsUids = initGspsUidsMap(results);

	  if (gspsUids.size() == 0) {
		 log.debug("No GSPS UID's referenced for study results.");
		 return results;
	  }
	  for (Map.Entry<String, StudyBean> me : gspsUids.entrySet()) {
		 String gspsUid = me.getKey();
		 StudyBean study = me.getValue();
		 DicomObject dcmobj = readDicomHeader(filterItem, params, gspsUid);
		 if (dcmobj == null) {
			log.warn("Couldn't read GSPS for uid " + gspsUid);
			continue;
		 }

		 long startItem = System.currentTimeMillis();
		 Map<String, MacroItems> images = initImagesForGsps(gspsUid, dcmobj, study);

		 GspsType gspsType = addGspsTypeToStudy(results, study, dcmobj);
		 log.debug("Parsing DICOM object.");
		 addMinMaxPixelInfo(dcmobj, images);
		 addAnnotationToResults(dcmobj, gspsType, study, images);
		 addShutterToResults(dcmobj, gspsType, study, images);
		 long dur = System.currentTimeMillis() - startItem;
		 log.info("Processing 1 GSPS took " + dur + " ms");
	  }
	  log.info("All GSPS time took:" + (System.currentTimeMillis() - startTime) + " ms");
	  return results;
   }

   /**
     * This method returns a map of the available images in the dicom object
     * that are referenced and available.
     * 
     * @param dcmobj
     * @param study
     * @return map of UID to object. Multiframes are all represented by 1
     *         object. That may need expansion later.
     */
   private Map<String, MacroItems> initImagesForGsps(String gspsUid, DicomObject dcmobj, StudyBean study) {
	  Map<String, MacroItems> ret = new HashMap<String, MacroItems>();
	  DicomElement sq = dcmobj.get(Tag.ReferencedSeriesSequence);
	  int seriesSize = sq.countItems();
	  for (int i = 0; i < seriesSize; i++) {
		 DicomObject dcmSeries = sq.getDicomObject(i);
		 DicomElement sqImage = dcmSeries.get(Tag.ReferencedImageSequence);
		 int imageSize = sqImage.countItems();
		 for (int j = 0; j < imageSize; j++) {
			DicomObject dcmImage = sqImage.getDicomObject(j);
			String objectUid = dcmImage.getString(Tag.ReferencedSOPInstanceUID);
			int[] frames = dcmImage.getInts(Tag.ReferencedFrameNumber);
			Object child = study.getChildById(objectUid);
			if (child == null)
			   continue;
			if (!(child instanceof ImageBean)) {
			   log.warn("In study " + study.getStudyInstanceUID() + " the gsps reference " + objectUid
					 + " does not reference an image, but references some other type of object.");
			   continue;
			}
			ImageBean image = (ImageBean) child;
			if (!image.getGspsUID().equals(gspsUid))
			   continue;
			// Try to handle the case where all frames are referenced in the one
			// GSPS as a standard case.
			if (frames == null || image.getNumberOfFrames() == 1 || image.getNumberOfFrames() == frames.length) {
			   ret.put(objectUid, image.getMacroItems());
			} else {
			   ImageBeanMultiFrame imageFrames = (ImageBeanMultiFrame) image;
			   for (int k = 0; k < frames.length; k++) {
				  int frame = frames[k];
				  if (frame < 1 || frame > image.getNumberOfFrames()) {
					 log.warn("Invalid frame number specified in GSPS " + image.getGspsUID() + " frame " + frame);
					 continue;
				  }
				  ret.put(objectUid + "," + frame, imageFrames.getFrameMacroItems(frame));
			   }
			}
		 }
	  }
	  return ret;
   }

   /**
     * Adds annotation results to the gsps object.
     * 
     * @param dcmobj
     * @param gspsType
     * @param images
     *            d.append(" A").append(radius).append(',').append(radius);
     *            d.append(" 0 1,0 ");
     *            d.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] +
     *            radius); d.append("
     *            A").append(radius).append(',').append(radius); d.append(" 0
     *            1,0 ");
     *            d.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] -
     *            radius);
     * 
     * v.append(" at ").append(center[X_OFFSET] - radius).append(',');
     * v.append(center[Y_OFFSET] - radius).append(' ');
     * v.append(center[X_OFFSET] + radius).append(',');
     * v.append(center[Y_OFFSET] + radius).append(' ');
     * v.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] -
     * radius).append(' ');
     * v.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] - radius);
     */
   @SuppressWarnings( { "unchecked", "deprecation" })
   protected void addAnnotationToResults(DicomObject dcmobj, GspsType gspsType, StudyBean study, Map<String, MacroItems> images) {
	  GraphicAnnotationModule[] grans = GraphicAnnotationModule.toGraphicAnnotationModules(dcmobj);
	  // This is type-safe, but can't be case right now because of Java 1.4
	  // restrictions.
	  Map<String, GraphicLayerModule> grals = GraphicLayerModule.toGraphicLayerMap(dcmobj);
	  if (grans == null)
		 return;
	  int id = 0;
	  // TODO - sort this by graphic layer order.
	  for (GraphicAnnotationModule gran : grans) {
		 String graphicLayerName = gran.getGraphicLayer();
		 GraphicLayerModule gral = grals.get(graphicLayerName);
		 // The layer name doesn't uniquely distinguish this correctly, as
		 // there can be multiple
		 // graphic annotation modules for one layer, applying to different
		 // image sets.
		 GType g = getG(gspsType, "a" + id);
		 id++;
		 String rgb = toRGB(gral.getGraphicLayerRecommendedDisplayGrayscaleValue(), gral.getFloatLab(), gral
			   .getGraphicLayerRecommendedDisplayRGBValueRET());
		 log.info("Graphic layer recommended display grayscale value is " + gral.getGraphicLayerRecommendedDisplayGrayscaleValue()
			   + " rgb is " + rgb + " for layer " + gral.getGraphicLayer() + " description " + gral.getGraphicLayerDescription());
		 // To not fill, over-ride these values in children.
		 g.setStyle("fill: " + rgb + "; stroke: " + rgb + ";");
		 g.setColor(rgb);
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
		 Use use = new Use();
		 use.setHref("#" + g.getId());
		 use.setId(ResultsBean.createId("u"));
		 addUse(study, images, use, gran.getImageSOPInstanceReferences());
	  }
   }

   /**
     * Adds a use to every macro items use clause that is referenced in the
     * image sop instance references.
     * 
     * @param images
     * @param useG
     * @param imageSOPInstanceReferences,
     *            if null, apply to all images in images.
     */
   private void addUse(StudyBean study, Map<String, MacroItems> images, Use use,
		 ImageSOPInstanceReference[] imageSOPInstanceReferences) {
	  if (imageSOPInstanceReferences == null || imageSOPInstanceReferences.length == 0) {
		 for (MacroItems macros : images.values()) {
			macros.addElement(use);
		 }
		 return;
	  }
	  for (int i = 0; i < imageSOPInstanceReferences.length; i++) {
		 ImageSOPInstanceReference imageRef = imageSOPInstanceReferences[i];
		 ImageBean image = (ImageBean) study.getChildById(imageRef.getReferencedSOPInstanceUID());
		 if (image == null)
			continue;
		 int[] frames = imageRef.getReferencedFrameNumber();
		 if (image.getNumberOfFrames() == 1 || frames == null || image.getNumberOfFrames() == frames.length) {
			// Single-frame case, or multi-frame referencing every element, AND
            // the gsps references every frame (implied but not tested here)
			MacroItems macros = images.get(image.getSOPInstanceUID());
			if (macros == null)
			   continue;
			macros.addElement(use);
		 } else {
			// Multi-frame case, referencing a sub-set of images.
			// 2 cases, either GSPS references only a sub-set of frames, or it
            // references all frames.
			MacroItems allFrames = images.get(imageRef.getReferencedSOPInstanceUID());
			boolean subset = allFrames == null;
			for (int j = 0; j < frames.length; j++) {
			   MacroItems macros;
			   if (subset)
				  macros = images.get(image.getSOPInstanceUID() + "," + frames[j]);
			   else
				  macros = image.getFrameMacroItems(frames[j]);
			   if (macros == null) {
				  log.warn("Frame doesn't seem to be in scope for GSPS.");
				  continue; // Must be a frame not referenced by this GSPS -
                            // must not be in scope
			   }
			   macros.addElement(use);
			}
		 }
	  }
   }

   /**
     * Adds the text object txo to svg:g. TODO add child elements for bounding
     * box and anchor point display.
     */
   protected void addTextObject(GType g, TextObject txo) {
	  TextType text = new TextType();
	  String unformatted = txo.getUnformattedTextValue();
	  text.setContent(unformatted);
	  // TODO handle multi-line text blocks.
	  int lineLen = unformatted.length();
	  if (lineLen == 0)
		 return;

	  float[] topLeft = txo.getBoundingBoxTopLeftHandCorner();
	  if (topLeft != null) {
		 float[] bottomRight = txo.getBoundingBoxBottomRightHandCorner();
		 float x = topLeft[X_OFFSET];
		 float y = topLeft[Y_OFFSET];
		 text.setX(Integer.toString((int) topLeft[X_OFFSET]));
		 text.setY(Integer.toString((int) topLeft[Y_OFFSET]));
		 int rotation = toRotation(topLeft, bottomRight);
		 if (rotation != 0)
			text.setTransform("rotate(" + rotation + "," + x + ',' + y + ")");
		 float xlen = Math.abs(bottomRight[X_OFFSET] - topLeft[X_OFFSET]);
		 // At least 5 pixels are needed, otherwise don't bother trying to
		 // figure out font size.
		 if (xlen > 5)
			text.setFontSize(Float.toString(xlen * 2 / lineLen));
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
	  if (type.equalsIgnoreCase(GraphicObject.POLYLINE)) {
		 addPolyline(g, go);
	  } else if (type.equalsIgnoreCase(GraphicObject.CIRCLE)) {
		 addCircle(g, go);
	  } else if (type.equalsIgnoreCase(GraphicObject.ELLIPSE)) {
		 addEllipse(g, go);
	  } else if (type.equalsIgnoreCase(GraphicObject.POINT)) {
		 addPoint(g, go);
	  } else if (type.equalsIgnoreCase(GraphicObject.INTERPOLATED)) {
		 // TODO replace this with something better.
		 addPolyline(g, go);
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

	  float cx = (points[ELLIPSE_MAJOR_X1] + points[ELLIPSE_MAJOR_X2]) / 2;
	  float cy = (points[ELLIPSE_MAJOR_Y1] + points[ELLIPSE_MAJOR_Y2]) / 2;
	  float rx = length(points, ELLIPSE_MAJOR_X1, ELLIPSE_MAJOR_X2) / 2;
	  float ry = length(points, ELLIPSE_MINOR_X1, ELLIPSE_MINOR_X2) / 2;
	  float rotation;
	  if (points[ELLIPSE_MAJOR_X1] == points[ELLIPSE_MAJOR_X2])
		 rotation = 90f;
	  else if (points[ELLIPSE_MAJOR_Y1] == points[ELLIPSE_MAJOR_Y2])
		 rotation = 0f;
	  else {
		 rotation = (float) (180 * Math.atan2(points[ELLIPSE_MAJOR_Y2] - cy, points[ELLIPSE_MAJOR_X2] - cx) / Math.PI);
	  }
	  PathType ellipse = createEllipsePath(cx, cy, rx, ry, rotation);
	  if (!go.getGraphicFilled()) {
		 ellipse.setStyle("fill: none;");
		 ellipse.setFill("false");
	  }
	  g.getChildren().add(ellipse);
   }

   /**
     * Converts a bounding box to a rotation amount. Returns a multiple of 90,
     * as that is the only bounding information that we can tell from just the
     * bounding box.
     */
   public static int toRotation(float[] topLeft, float[] bottomRight) {
	  float x1 = topLeft[X_OFFSET];
	  float x2 = bottomRight[X_OFFSET];
	  float y1 = topLeft[Y_OFFSET];
	  float y2 = bottomRight[Y_OFFSET];
	  if (x1 < x2) {
		 if (y1 < y2) {
			return 0;
		 }
		 return 90;
	  } else {
		 if (y1 < y2) {
			return 180;
		 }
		 return 270;
	  }
   }

   /** Returns the length between the points in data specified by p1 and p2. */
   public static float length(float[] data, int p1, int p2) {
	  float dx = data[p1] - data[p2];
	  float dy = data[p1 + 1] - data[p2 + 1];
	  return (float) Math.sqrt(dx * dx + dy * dy);
   }

   /**
     * Generates an ellipse, centered at the given point, with both path and d
     * elements for both SVG and VML. Starts and stops at the top of the circle
     * (useful to know in case this is part of a shutter - if so, then append or
     * prepend the rest of the shutter information).
     * 
     * @param rotation
     *            in degrees. Doesn't work for VML if rotation isn't 0 or 90.
     */
   public static PathType createEllipsePath(float cx, float cy, float rx, float ry, float rotation) {
	  PathType circle = new PathType();
	  // TODO handle rotation here as well.
	  if (rotation == 90f) {
		 float tmp = rx;
		 rx = ry;
		 ry = tmp;
		 rotation = 0f;
	  }
	  float sx = 0;
	  float sy = ry;
	  if (rotation != 0f) {
		 sx = (float) (Math.sin(rotation * Math.PI / 180f) * rx);
		 sy = (float) (Math.cos(rotation * Math.PI / 180f) * ry);
		 log.warn("TODO: broken for displaying rotated ellipses in IE.");
	  }
	  float topX = cx - sx;
	  float topY = cy - sy;
	  StringBuffer d = new StringBuffer("M");
	  d.append(topX).append(",").append(topY);

	  StringBuffer v = new StringBuffer(d);
	  d.append(" A").append(rx).append(',').append(ry);
	  d.append(" 0 1,0 ");
	  d.append(cx + sx).append(',').append(cy + sy);
	  d.append(" A").append(rx).append(',').append(ry);
	  d.append(" 0 1,0 ");
	  d.append(topX).append(',').append(topY);

	  // TODO fix this for arbitrary rotations around cx,cy
	  v.append(" at ").append(cx - rx).append(',');
	  v.append(cy - ry).append(' ');
	  v.append(cx + rx).append(',');
	  v.append(cy + ry).append(' ');
	  v.append(topX).append(',').append(topY).append(' ');
	  v.append(topX).append(',').append(topY);

	  circle.setD(d.toString());
	  circle.setPath(v.toString());
	  return circle;
   }

   /**
     * Adds a point SVG to the graphic object, as a circle of radius 0.5
     * centered on the given position.
     */
   protected void addPoint(GType g, GraphicObject go) {
	  float[] points = go.getGraphicData();
	  if (points == null || points.length != 2) {
		 log.warn("Invalid point data provided in graphic object.");
		 return;
	  }
	  PathType circle = createEllipsePath(points[0], points[1], 0.5f, 0.5f, 0f);
	  g.getChildren().add(circle);
   }

   /** Adds a circle SVG to the graphic object */
   protected void addCircle(GType g, GraphicObject go) {
	  float[] points = go.getGraphicData();
	  if (points == null || points.length != 4) {
		 log.warn("Invalid circle data provided in graphic object.");
		 return;
	  }
	  float r = length(points, 0, 2);
	  PathType circle = createEllipsePath(points[0], points[1], r, r, 0f);
	  circle.setPrType("Circle");
	  if (!go.getGraphicFilled()) {
		 circle.setStyle("fill: none;");
		 circle.setFill("false");
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
	  d.append("M").append((int) points[0]).append(',').append((int) points[1]);
	  // Start at 3 so that if the length happens to be odd, we don't throw an
	  // exception, but just ignore the extra value.
	  for (int i = 3; i < points.length; i += 2) {
		 // Note the order is x,y NOT y,x as it is for shutters (shudder)
		 d.append(" L").append((int) points[i - 1]).append(',').append((int) points[i]);
	  }
	  path.setD(d.toString());
	  path.setId(ResultsBean.createId("p"));
	  if (!go.getGraphicFilled()) {
		 path.setStyle("fill: none;");
		 path.setFill("false");
	  }
	  g.getChildren().add(path);
   }

   /**
     * Adds min/max pixel information from the GSPS instead of from the image
     * header.
     */
   protected void addMinMaxPixelInfo(DicomObject dcmobj, Map<String, MacroItems> items) {
	  for (MacroItems macros : items.values()) {
		 MinMaxPixelInfo.updatePixelRange(dcmobj, macros);
	  }
   }

   /**
     * Adds the GSPS xml object, returning the instance created/added.
     * 
     * @param results
     * @param dcmobj
     * @return
     */
   protected GspsType addGspsTypeToStudy(ResultsBean results, StudyBean study, DicomObject dcmobj) {
	  study.addResult(dcmobj);
	  GspsType gspsType = (GspsType) study.getChildById(dcmobj.getString(Tag.SOPInstanceUID));
	  if (gspsType == null)
		 throw new NullPointerException("Something went wrong in adding results object - null gspsType.");
	  // Add it into the results as well.
	  for (PatientType patient : results.getPatient()) {
		 for (StudyType studyIt : patient.getStudy()) {
			if (studyIt.getStudyInstanceUID() == study.getStudyInstanceUID()) {
			   if (study == studyIt)
				  return gspsType;
			   String seriesUid = dcmobj.getString(Tag.SeriesInstanceUID);
			   for (SeriesType series : studyIt.getSeries()) {
				  if (series.getSeriesInstanceUID().equals(seriesUid)) {
					 // It has the series, so just add the gsps object, which
					 // it should not have.
					 for (DicomObjectType dot : series.getDicomObject()) {
						if (dot.getSOPInstanceUID().equals(gspsType.getSOPInstanceUID())) {
						   log.warn("Should not already have GSPS type for the same type -- something has gone wrong here.");
						   series.getDicomObject().remove(dot);
						   break;
						}
					 }
					 series.getDicomObject().add(gspsType);
					 return gspsType;
				  }
			   }
			   // Didn't find the series - might as well add it completely.
			   studyIt.getSeries().add((SeriesType) study.getChildById(seriesUid));
			   return gspsType;
			}
		 }
	  }
	  log.error("Didn't find study uid in the results - shouldn't happen.");
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
   protected void addShutterToResults(DicomObject dcmobj, GspsType gspsType, StudyBean study, Map<String, MacroItems> images) {
	  if (images == null || images.size() == 0)
		 return;

	  // Since we need the width and height to display the shutter, an example
	  // will be computed here.
	  // This isn't strictly legal, as the images don't all have to be the
	  // same size, but logically they
	  // had better be in order for the shutter to be meaningful.
	  String exemplarUid = images.entrySet().iterator().next().getKey();
	  int commaPos = exemplarUid.indexOf(",");
	  if (commaPos > 0)
		 exemplarUid = exemplarUid.substring(0, commaPos);
	  ImageBean exemplar = (ImageBean) study.getChildById(exemplarUid);
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
	  Use use = new Use();
	  use.setHref("#" + g.getId());
	  use.setId(ResultsBean.createId("u"));
	  addUse(study, images, use, null);
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
	  PathType path = new PathType();

	  StringBuffer d = new StringBuffer("M0,0");
	  d.append(" L0,").append(height);
	  d.append(" L").append(width).append(',').append(height);
	  d.append(" L").append(width).append(",0").append(" L0,0");

	  // Then remove the polygon in the center
	  if (useRect) {
		 d.append(" L").append(shutter.getShutterLeftVerticalEdge()).append(',').append(shutter.getShutterUpperHorizontalEdge());
		 d.append(" L").append(shutter.getShutterRightVerticalEdge()).append(',').append(shutter.getShutterUpperHorizontalEdge());
		 d.append(" L").append(shutter.getShutterRightVerticalEdge()).append(',').append(shutter.getShutterLowerHorizontalEdge());
		 d.append(" L").append(shutter.getShutterLeftVerticalEdge()).append(',').append(shutter.getShutterLowerHorizontalEdge());
		 d.append(" L").append(shutter.getShutterLeftVerticalEdge()).append(',').append(shutter.getShutterUpperHorizontalEdge());
	  } else {
		 int[] vertices = shutter.getVerticesOfThePolygonalShutter();
		 if (vertices == null | vertices.length < 2)
			return;
		 for (int i = 1; i < vertices.length; i += 2) {
			d.append(" L").append(vertices[i]).append(',').append(vertices[i - 1]);
		 }
		 if (vertices[vertices.length - 1] != vertices[1] || vertices[vertices.length - 2] != vertices[0]) {
			d.append(" L").append(vertices[1]).append(',').append(vertices[0]);
		 }
	  }
	  d.append(" Z");
	  log.info("Set rect shutter path to " + d);

	  String rgb = toRGB(shutter.getShutterPresentationValue(), shutter.getFloatLab(), null);
	  path.setColor(rgb);
	  path.setStyle("fill:" + rgb + ";");
	  path.setId(ResultsBean.createId("shre"));
	  path.setD(d.toString());

	  g.getChildren().add(path);
   }

   /**
     * Adds a circular shutter to the group g provided. Defines the clip path
     * inside the svg child of gspsType. Defines both SVG and VML parameters on
     * the svg path, so that it is easy to convert to a VML shutter if required.
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
	  PathType path = new PathType();
	  StringBuffer d = new StringBuffer("M");
	  d.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] - radius);
	  d.append(" L0,0 L");
	  d.append(width).append(",0 L").append(width).append(',').append(height);
	  d.append(" L0,").append(height).append(" L0,0 L");
	  d.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] - radius);
	  StringBuffer v = new StringBuffer(d);
	  d.append(" A").append(radius).append(',').append(radius);
	  d.append(" 0 1,0 ");
	  d.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] + radius);
	  d.append(" A").append(radius).append(',').append(radius);
	  d.append(" 0 1,0 ");
	  d.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] - radius);

	  v.append(" at ").append(center[X_OFFSET] - radius).append(',');
	  v.append(center[Y_OFFSET] - radius).append(' ');
	  v.append(center[X_OFFSET] + radius).append(',');
	  v.append(center[Y_OFFSET] + radius).append(' ');
	  v.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] - radius).append(' ');
	  v.append(center[X_OFFSET]).append(',').append(center[Y_OFFSET] - radius);
	  String rgb = toRGB(shutter.getShutterPresentationValue(), shutter.getFloatLab(), null);
	  path.setColor(rgb);
	  path.setStyle("fill:" + rgb + ";");
	  path.setId(ResultsBean.createId("shcr"));
	  path.setD(d.toString());
	  path.setPath(v.toString());

	  g.getChildren().add(path);
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
				  + " colour space type id " + lab.getType());
		 }
	  } else if (rgbColour != null) {
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
	  g.setId(gspsType.getSOPInstanceUID() + "-" + name);
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
     * Initialize the map of gspsUids to the original study bean that is
     * associated with them. That allows the parent-original to be updated for
     * all applicable images.
     * 
     * @param results
     * @param gspsUids
     */
   private Map<String, StudyBean> initGspsUidsMap(ResultsBean results) {
	  Map<String, StudyBean> gspsUids = new HashMap<String, StudyBean>();
	  for (PatientType pt : results.getPatient()) {
		 for (StudyType st : pt.getStudy()) {
			for (SeriesType se : st.getSeries()) {
			   for (DicomObjectType dot : se.getDicomObject()) {
				  if (!(dot instanceof ImageBean))
					 continue;
				  ImageBean image = (ImageBean) dot;
				  if (image.getGspsUID() == null)
					 continue;
				  if (image.getMacroItems().findMacro(MinMaxPixelMacro.class) != null)
					 continue;
				  StudyBean studyBean = gspsUids.get(image.getGspsUID());
				  if (studyBean == null) {
					 StudyBean originalStudy = ((StudyBean) st).getOriginalStudy();
					 gspsUids.put(image.getGspsUID(), originalStudy);
					 log.info("GSPS " + image.getGspsUID() + " references study " + originalStudy.getStudyInstanceUID());
				  }
			   }
			}
		 }
	  }
	  return gspsUids;
   }
}
