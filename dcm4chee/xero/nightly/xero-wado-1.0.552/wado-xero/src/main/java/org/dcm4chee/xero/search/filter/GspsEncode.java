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
package org.dcm4chee.xero.search.filter;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.VOIUtils;
import org.dcm4che2.iod.module.macro.ImageSOPInstanceReference;
import org.dcm4che2.iod.module.pr.DisplayedAreaModule;
import org.dcm4che2.iod.module.pr.SpatialTransformationModule;
import org.dcm4che2.iod.module.pr.DisplayShutterModule;
import org.dcm4che2.iod.module.pr.GraphicAnnotationModule;
import org.dcm4che2.iod.module.pr.GraphicLayerModule;
import org.dcm4che2.iod.module.pr.GraphicObject;
import org.dcm4che2.iod.module.pr.TextObject;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.macro.AspectMacro;
import org.dcm4chee.xero.search.macro.FlipRotateMacro;
import org.dcm4chee.xero.search.macro.GspsEncoded;
import org.dcm4chee.xero.search.macro.MinMaxPixelMacro;
import org.dcm4chee.xero.search.macro.PixelSpacingMacro;
import org.dcm4chee.xero.search.macro.RegionMacro;
import org.dcm4chee.xero.search.macro.WindowLevelMacro;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageBeanMultiFrame;
import org.dcm4chee.xero.search.study.Macro;
import org.dcm4chee.xero.search.study.MacroItems;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.DicomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.svg.GType;
import org.w3.svg.ImageType;
import org.w3.svg.PathType;
import org.w3.svg.SvgType;
import org.w3.svg.TextType;
import org.w3.svg.Use;
import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

/**
 * This class encodes GSPS as XML in either VML or SVG formats. This class will
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
	private static final float WIDTH_CORRECTION = 0.55f;

	private static final float HEIGHT_CORRECTION = 1.5f;

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

	private static final int MIN_FONT_SIZE = 12;
	private static final int MIN_DISPLAY_FONT_SIZE = 20;

	/** The string to use for the wado image references */
	private String wadoUrl = "/wado2/wado";
	
   private Filter<DicomObject> dicomFullHeader;

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomFullHeader() {
   	return dicomFullHeader;
   }

   /** Sets the full header filter - this returns all the fields, but not updated. */
	@MetaData(out="${ref:dicomFullHeader}")
	public void setDicomFullHeader(Filter<DicomObject> dicomFullHeader) {
   	this.dicomFullHeader = dicomFullHeader;
   }

	/**
	 * This class adds information about the GSPS objects to the filter results.
	 * The GSPS objects that are read/included are the set of distinct GSPS
	 * objects referenced from images included in the return set, not already
	 * parsed. The CHANGES are made to the original study, NOT the current study.
	 * The original study is retrieved via getOriginalStudy on the StudyBean
	 * object, and consist of: 1. Inclusion of the GSPS series/objects. 2.
	 * Inclusion of the SVG objects for the series information. 3. Changes to
	 * image/frame level data for window levels (VOI, Modality and Presentation)
	 * 4. Changes to study/series/image level data for region information
	 * (RegionMacro), and size/rotation.
	 * 
	 * Currently, an image will have been considered to be processed if it
	 * contains a MinMaxPixelMacro. This could be problematic if this information
	 * can ever be read from the CFIND response (eg if the C-FIND response
	 * contained the Modality LUT rescale slope/intercept information and there
	 * was custom processing to use that version instead of reading the image
	 * header.) This header is created both by GSPS (first), and secondly, if no
	 * GSPS is applicable, by reading the image header.
	 * 
	 * The reason for splitting the work up this way is that it minimizes the
	 * number of GSPS objects read on any request to only those actually required
	 * to process the request, while also only doing the work of processing a
	 * GSPS once per series (assuming series level requests - which is the norm
	 * for most of this work.) Given that, GSPS objects should not be cached for
	 * very long, if at all, and not at all if they only apply to one series.
	 */
	public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
		log.debug("GspsEncoding to add XML to image.");
		ResultsBean results = (ResultsBean) filterItem.callNextFilter(params);
		long startTime = System.nanoTime();

		if (results == null)
			return null;
		Object modality = params.get("Modality");
		// Sometimes searches for PR, SR or KO are made - if so, there isn't any
		// point running this filter as no images will be returned.
		if ("PR".equals(modality) || "SR".equals(modality) || "KO".equals(modality))
			return results;

		Map<String, StudyBean> gspsUids = initGspsUidsMap(results);

		if (gspsUids.size() == 0) {
			log.debug("No GSPS UID's referenced for study results.");
			return results;
		}
		for (Map.Entry<String, StudyBean> me : gspsUids.entrySet()) {
			String gspsUid = me.getKey();
			StudyBean study = me.getValue();
			DicomObject dcmobj = DicomFilter.callInstanceFilter(dicomFullHeader, params, gspsUid);
			if (dcmobj == null) {
				log.warn("Couldn't read GSPS for uid " + gspsUid);
				continue;
			}

			long startItem = System.nanoTime();
			Map<String, ImageBean> images = initImagesForGsps(gspsUid, dcmobj, study);

			GspsType gspsType = addGspsTypeToStudy(results, study, dcmobj);
			log.debug("Parsing DICOM object.");
			addDisplayAreaRotate(dcmobj, images);
			addMinMaxPixelInfo(dcmobj, images);
			addShutterToResults(dcmobj, gspsType, images);
			addAnnotationToResults(dcmobj, gspsType, images);
			long dur = System.nanoTime() - startItem;
			log.info("Processing 1 GSPS took " + nanoTimeToString(dur));
		}
		log.info("All GSPS time took:" + nanoTimeToString(System.nanoTime() - startTime));
		return results;
	}

	/** Adds display area, rotation and flip information to the image data */
	private void addDisplayAreaRotate(DicomObject dcmobj, Map<String, ImageBean> images) {
		DisplayedAreaModule[] dams = DisplayedAreaModule.toDisplayedAreaModules(dcmobj);
		SpatialTransformationModule spat = new SpatialTransformationModule(dcmobj);
		FlipRotateMacro flipRotateMacro = null;
		if (spat.getRotation() != 0 || spat.isHorizontalFlip()) {
			flipRotateMacro = new FlipRotateMacro(spat.getRotation(), spat.isHorizontalFlip());
			addMacro(images, flipRotateMacro, null);
		}

		for (DisplayedAreaModule dam : dams) {
			String presentationMode = dam.getPresentationSizeMode();
			int[] tlhc = dam.getDisplayedAreaTopLeftHandCorner();
			int[] brhc = dam.getDisplayedAreaBottomRightHandCorner();
			float[] spacing = dam.getPresentationPixelSpacing();
			int[] aspectPair = dam.getPresentationPixelAspectRatio();
			float aspect = 1.0f;
			ImageSOPInstanceReference[] sops = dam.getImageSOPInstanceReferences();
			if (spacing != null) {
				if (spacing[0] != spacing[1]) {
					aspect = spacing[0] / spacing[1];
				}
				PixelSpacingMacro spacingMacro = new PixelSpacingMacro(spacing);
				addMacro(images, spacingMacro, sops);
			} else if (aspectPair != null && aspectPair[0] != aspectPair[1]) {
				aspect = aspectPair[0] / (float) aspectPair[1];
			}

			if (aspect != 1.0f) {
				AspectMacro aspectMacro = new AspectMacro(aspect);
				addMacro(images, aspectMacro, sops);
			}
			ImageBean exemplar = null;
			if (sops != null) {
				for (ImageSOPInstanceReference sop : sops) {
					exemplar = (ImageBean) images.get(sop.getReferencedSOPInstanceUID());
					if (exemplar != null)
						break;
				}
			} else if (images.keySet().size() > 0) {
				String uid = images.keySet().iterator().next();
				exemplar = images.get(uid);
			}
			// It isn't clear how else to get an exemplar, so skip the test for
			// default if we don't have one.
			if (exemplar != null && "SCALE TO FIT".equalsIgnoreCase(presentationMode) && tlhc[0] == 1 && tlhc[1] == 1
			      && brhc[1] == exemplar.getColumns() && brhc[0] == exemplar.getRows()) {
				// Don't both adding this - it is a complete default item
				continue;
			}
			RegionMacro region = new RegionMacro(presentationMode, tlhc, brhc, dam
			      .getPresentationPixelMagnificationRatio());
			addMacro(images, region, sops);
		}
	}

	/**
	 * Get the macro items associated with a given image bean. Will return the
	 * correct macro items for both framed and un-framed items. The un-framed
	 * item will only be available if the GSPS references the entire object,
	 * otherwise only the individual frames can be set.
	 * 
	 * @param images
	 * @param key
	 * @return
	 */
	private static MacroItems getMacroItems(Map<String, ImageBean> images, String key) {
		int framePos = key.indexOf(",");
		if (framePos < 0) {
			ImageBean image = images.get(key);
			if (image != null)
				return image.getMacroItems();
			return null;
		}
		String uid = key.substring(0, framePos);
		int frame = Integer.parseInt(key.substring(framePos + 1));
		ImageBean image = images.get(uid);
		if (image == null) {
			image = images.get(key);

		}
		if (image == null)
			return null;
		return image.getFrameMacroItems(frame);
	}

	/**
	 * This method returns a map of the available images in the dicom object that
	 * are referenced and available. For multi-frames, this returns the
	 * ImageBeanMultiFrame object, NOT the child objects - it is necessary to use
	 * the frame level MacroItems retrieve if a frame level attribute is being
	 * set, that is whenever the key contains -FRAME at the end.
	 * 
	 * @param dcmobj
	 * @param study
	 * @return map of UID to object. Multiframes are all represented by 1 object.
	 *         That may need expansion later.
	 */
	private Map<String, ImageBean> initImagesForGsps(String gspsUid, DicomObject dcmobj, StudyBean study) {
		Map<String, ImageBean> ret = new HashMap<String, ImageBean>();
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
					log.warn("In study " + study.getStudyUID() + " the gsps reference " + objectUid
					      + " does not reference an image, but references some other type of object.");
					continue;
				}
				ImageBean image = (ImageBean) child;
				if (image.getGspsUID() == null) {
					log.warn("Image {} has null gspsuid", image.getObjectUID());
					continue;
				}
				if (!image.getGspsUID().equals(gspsUid))
					continue;
				// Try to handle the case where all frames are referenced in the one
				// GSPS as a standard case.
				if (frames == null || image.getNumberOfFrames() == 1 || image.getNumberOfFrames() == frames.length) {
					ret.put(objectUid, image);
				} else {
					ImageBeanMultiFrame imageFrames = (ImageBeanMultiFrame) image;
					for (int k = 0; k < frames.length; k++) {
						int frame = frames[k];
						if (frame < 1 || frame > image.getNumberOfFrames()) {
							log.warn("Invalid frame number specified in GSPS " + image.getGspsUID() + " frame " + frame);
							continue;
						}
						ret.put(objectUid + "," + frame, imageFrames);
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
	 */
	@SuppressWarnings( { "unchecked" })
	protected void addAnnotationToResults(DicomObject dcmobj, GspsType gspsType, Map<String, ImageBean> images) {
		GraphicAnnotationModule[] grans = GraphicAnnotationModule.toGraphicAnnotationModules(dcmobj);
		Map<String, GraphicLayerModule> grals = GraphicLayerModule.toGraphicLayerMap(dcmobj);
		addMarkup(gspsType, images, grans, grals, "Y".equalsIgnoreCase(dcmobj.getString(Tag.ImageHorizontalFlip)));

		addOverlays(dcmobj, gspsType, images, grals);
	}

	/** Adds markup information to the study */
	@SuppressWarnings("deprecation")
	protected void addMarkup(GspsType gspsType, Map<String, ImageBean> images, GraphicAnnotationModule[] grans,
	      Map<String, GraphicLayerModule> grals, boolean flip) {
		int id = 0;
		// TODO - sort this by graphic layer order.
		if (grans == null || grans.length == 0)
			return;
		for (GraphicAnnotationModule gran : grans) {
			String graphicLayerName = gran.getGraphicLayer();
			GraphicLayerModule gral = grals.get(graphicLayerName);
			// The layer name doesn't uniquely distinguish this correctly, as
			// there can be multiple
			// graphic annotation modules for one layer, applying to different
			// image sets.
			GType gimg = getG(gspsType, "ai" + id);
			GType gdisp = getG(gspsType, "ad" + id);
			gdisp.setClazz("DISPLAY");
			id++;
			String rgb = toRGB(gral.getGraphicLayerRecommendedDisplayGrayscaleValue(), gral.getFloatLab(), gral
			      .getGraphicLayerRecommendedDisplayRGBValueRET());
			log.info("Graphic layer recommended display grayscale value is "
			      + gral.getGraphicLayerRecommendedDisplayGrayscaleValue() + " rgb is " + rgb + " for layer "
			      + gral.getGraphicLayer() + " description " + gral.getGraphicLayerDescription());
			// To not fill, over-ride these values in children.
			gimg.setFill(rgb);
			gimg.setStroke(rgb);
			gdisp.setFill(rgb);
			gdisp.setStroke(rgb);
			GraphicObject[] gos = gran.getGraphicObjects();
			if (gos != null) {
				for (GraphicObject go : gos) {
					boolean isDisp = ("DISPLAY".equalsIgnoreCase(go.getGraphicAnnotationUnits()));
					addGraphicObject(isDisp ? gdisp : gimg, go, isDisp ? 1000 : 1);
				}
			}

			TextObject[] txos = gran.getTextObjects();
			if (txos != null) {
				for (TextObject txo : txos) {
					addTextObject(gdisp, gimg, txo, flip);
				}
			}

			if (gimg.getChildren().size() > 0) {
				Use use = new Use();
				use.setHref("#" + gimg.getId());
				use.setId(ResultsBean.createId("u"));
				addUse(images, use, gran.getImageSOPInstanceReferences());
			} else {
				gspsType.getSvg().getChildren().remove(gimg);
			}
			if (gdisp.getChildren().size() > 0) {
				Use use = new Use();
				use.setClazz("DISPLAY");
				use.setHref("#" + gdisp.getId());
				use.setId(ResultsBean.createId("u"));
				addUse(images, use, gran.getImageSOPInstanceReferences());
			} else {
				gspsType.getSvg().getChildren().remove(gdisp);
			}
		}
	}

	/**
	 * Adds all the overlay annotations to the object. There are 3 basic types of
	 * overlays: GSPS Overlays - these are contained in the GSPS header, and
	 * apply anywhere that the particular header type applies. Image Overlays -
	 * these look the same as teh GSPS overlays except they exist in the image
	 * object. Embedded Overlays - these are hard-coded into the GSPS image
	 * objects. Additionally, overlays can be single-frame or multi-frame.
	 * Embedded overalays are always multi-frame, that is a different image
	 * applies to each frame, while the iamge and GSPS overlays can be of either
	 * type.
	 * 
	 * If the activated overlay is multi-frame, then a different use/reference is
	 * required for every image object.
	 * 
	 * @param dcmobj
	 * @param gspsType
	 * @param study
	 * @param images
	 * @param grals
	 * @param overlays
	 * @param id
	 */
	@SuppressWarnings("deprecation")
	private void addOverlays(DicomObject dcmobj, GspsType gspsType, Map<String, ImageBean> images,
	      Map<String, GraphicLayerModule> grals) {
		Map<String, List<Integer>> overlays = findOverlays(dcmobj);
		if (overlays != null) {
			for (GraphicLayerModule gral : grals.values()) {
				String rgb = toRGB(gral.getGraphicLayerRecommendedDisplayGrayscaleValue(), gral.getFloatLab(), gral
				      .getGraphicLayerRecommendedDisplayRGBValueRET());
				if (overlays.containsKey(gral.getGraphicLayer())) {
					log.debug("Adding overlay to layer " + gral.getGraphicLayer());
					List<Integer> layers = overlays.get(gral.getGraphicLayer());
					for (Integer l : layers) {
						addOverlay(dcmobj, l, rgb, images);
					}
				}
			}
		}
	}

	/**
	 * Returns a map of layer name to list of overlay number, null if no overlays
	 * are found. Overlays are assumed to start at 60001001 and proceed up by
	 * 0x20000 Any skips will cause missing values. Any empty string overlays are
	 * ignored.
	 * 
	 * @param dcmobj
	 * @return
	 */
	private Map<String, List<Integer>> findOverlays(DicomObject dcmobj) {
		String layer = dcmobj.getString(Tag.OverlayActivationLayer);
		if (layer == null)
			return null;
		Map<String, List<Integer>> ret = new HashMap<String, List<Integer>>();
		for (int i = Tag.OverlayActivationLayer; i < 0x60FF0000; i += 0x20000) {
			layer = dcmobj.getString(i);
			if (layer == null)
				break;
			List<Integer> add = ret.get(layer);
			if (add == null) {
				add = new ArrayList<Integer>();
				ret.put(layer, add);
			}
			// We only want the layer id, not the entire activation code.
			int layerCode = (i & 0xFFFF0000);
			log.info("Found activation for layer " + layerCode + " on " + layer);
			add.add(layerCode);
		}
		return ret;
	}

	/**
	 * Adds a use to every macro items use clause that is referenced in the image
	 * sop instance references.
	 * 
	 * @param images
	 * @param useG
	 * @param imageSOPInstanceReferences,
	 *           if null, apply to all images in images.
	 */
	private void addUse(Map<String, ImageBean> images, Use use, ImageSOPInstanceReference[] imageSOPInstanceReferences) {

		if (imageSOPInstanceReferences == null || imageSOPInstanceReferences.length == 0) {
			for (String uid : images.keySet()) {
				getMacroItems(images, uid).addElement(use);
			}
			return;
		}
		for (int i = 0; i < imageSOPInstanceReferences.length; i++) {
			ImageSOPInstanceReference imageRef = imageSOPInstanceReferences[i];
			String uid = imageRef.getReferencedSOPInstanceUID();
			ImageBean image = images.get(uid);
			int[] frames = imageRef.getReferencedFrameNumber();
			if (image == null && frames == null)
				continue;
			if (image != null && (frames == null || image.getNumberOfFrames() == frames.length)) {
				image.getMacroItems().addElement(use);
			} else {
				for (int f : frames) {
					ImageBean imageFrame = image;
					if (imageFrame == null)
						imageFrame = images.get(uid + "," + f);
					if (imageFrame == null) {
						continue;
					}
					imageFrame.getFrameMacroItems(f).addElement(use);
				}
			}
		}
	}

	/**
	 * Adds a macro to all the referenced images image sop instance references.
	 * 
	 * @param study
	 * @param images
	 * @param macro
	 * @param imageSOPInstanceReferences,
	 *           if null, apply to all images in images.
	 */
	private void addMacro(Map<String, ImageBean> images, Macro macro,
	      ImageSOPInstanceReference[] imageSOPInstanceReferences) {
		if (imageSOPInstanceReferences == null || imageSOPInstanceReferences.length == 0) {
			for (String uid : images.keySet()) {
				getMacroItems(images, uid).addMacro(macro);
			}
			return;
		}
		for (int i = 0; i < imageSOPInstanceReferences.length; i++) {
			ImageSOPInstanceReference imageRef = imageSOPInstanceReferences[i];
			String uid = imageRef.getReferencedSOPInstanceUID();
			ImageBean image = images.get(uid);
			int[] frames = imageRef.getReferencedFrameNumber();
			if (image == null && frames == null) {
				continue;
			}
			if (image != null && (frames == null || image.getNumberOfFrames() == frames.length)) {
				image.getMacroItems().addMacro(macro);
			} else {
				for (int f : frames) {
					ImageBean imageFrame = image;
					if (imageFrame == null)
						imageFrame = images.get(uid + "," + f);
					if (imageFrame == null) {
						continue;
					}
					imageFrame.getFrameMacroItems(f).addMacro(macro);
				}
			}
		}
	}

	/**
	 * Adds the text object txo to svg:g. TODO add child elements for bounding
	 * box and anchor point display.
	 */
	protected void addTextObject(GType gdisp, GType gimg, TextObject txo, boolean flip) {
		String unformatted = txo.getUnformattedTextValue();
		String[] lines = convertToLines(unformatted);
		int maxLen = lineLength(lines);
		if (maxLen <= 0)
			return;

		float[] topLeft = txo.getBoundingBoxTopLeftHandCorner();
		float[] anchor = txo.getAnchorPoint();
		GType g = gimg;
		int pixScale = 1;
		if (topLeft != null) {
			boolean isDisplay = ("DISPLAY".equalsIgnoreCase(txo.getBoundingBoxAnnotationUnits())); 
			if ( isDisplay) {
				g = gdisp;
				pixScale = 1000;
				flip = false;
			}
			float[] bottomRight = txo.getBoundingBoxBottomRightHandCorner();
			int rotation = toRotation(topLeft, bottomRight, flip);
			int offset = X_OFFSET;

			// Get the size of the text box we are working with...
			if (rotation == 90 || rotation == 270)
				offset = Y_OFFSET;
			float boxWidth = (bottomRight[offset] - topLeft[offset]) * pixScale;
			offset = 1 - offset;
			float boxHeight = (bottomRight[offset] - topLeft[offset]) * pixScale;

			// Figure out the font size, in pixels/unit lengths
			int fontSizeW = (int) Math.abs(boxWidth / (maxLen ) );
			int fontSizeH = (int) Math.abs(boxHeight / (lines.length * HEIGHT_CORRECTION));
			if (fontSizeW > fontSizeH)
				fontSizeW = fontSizeH;
			if ( (!isDisplay) && fontSizeW < MIN_FONT_SIZE)
				fontSizeW = MIN_FONT_SIZE;
			if( isDisplay && fontSizeW < MIN_DISPLAY_FONT_SIZE )
				fontSizeW = MIN_DISPLAY_FONT_SIZE;

			String just = txo.getBoundingBoxTextHorizontalJustification();
			boolean isCenter = "CENTER".equalsIgnoreCase(just);
			boolean isRight = "RIGHT".equalsIgnoreCase(just);
			float linePosOffset = fontSizeW * HEIGHT_CORRECTION;
			if (boxHeight < 0)
				linePosOffset = -linePosOffset;
			for (int i = 0; i < lines.length; i++) {
				float x = 0;
				int lineLen = lines[i].length();
				if (lineLen == 0)
					continue;
				if (isCenter) {
					x = (boxWidth - lineLen * fontSizeW * WIDTH_CORRECTION) / 2;
				} else if (isRight) {
					x = boxWidth - lineLen * fontSizeW * WIDTH_CORRECTION;
				}
				float y = (i + 1) * linePosOffset;
				if (rotation == 90 || rotation == 270) {
					float xy = x;
					x = y;
					y = xy;
				}
				x += topLeft[X_OFFSET] * pixScale;
				y += topLeft[Y_OFFSET] * pixScale;
				TextType text = new TextType();
				text.setId(ResultsBean.createId("t"));
				text.setContent(lines[i]);
				text.setX(Integer.toString((int) x));
				text.setY(Integer.toString((int) y));
				text.setTextLength(Integer.toString((int) (lineLen * fontSizeW * WIDTH_CORRECTION)));
				text.setLengthAdjust("spacingAndGlyphs");
				if (rotation != 0 || flip) {
					StringBuffer transform = new StringBuffer();
					if (flip) {
						transform.append("translate(").append((int) (2 * x)).append(",0) scale(-1,1)");
					}
					if (rotation != 0)
						transform.append(" rotate(").append(rotation).append(",").append((int) x).append(',').append((int) y)
						      .append(")");
					text.setTransform(transform.toString());
				}
				text.setFontSize(Integer.toString(fontSizeW));
				g.getChildren().add(text);
			}
		} else {
			if ("DISPLAY".equalsIgnoreCase(txo.getAnchorPointAnnotationUnits())) {
				g = gdisp;
				pixScale = 1000;
				flip = false;
			}
			for (int i = 0; i < lines.length; i++) {
				TextType text = new TextType();
				text.setId(ResultsBean.createId("t"));
				text.setContent(lines[i]);
				text.setTextLength(Integer.toString((int) (MIN_FONT_SIZE * lines[i].length() * WIDTH_CORRECTION)));
				// Don't use X/Y offsets - this is column/row ordered.
				text.setX(Float.toString(anchor[X_OFFSET] * pixScale + MIN_FONT_SIZE));
				text.setY(Float.toString(anchor[Y_OFFSET] * pixScale + i * MIN_FONT_SIZE * HEIGHT_CORRECTION));
				text.setFontSize(Integer.toString(MIN_FONT_SIZE));
				g.getChildren().add(text);
			}
		}
		if (txo.getAnchorPointVisibility()) {
			// Need to render the anchor point
			g = gimg;
			pixScale = 1;
			if ("DISPLAY".equalsIgnoreCase(txo.getAnchorPointAnnotationUnits())) {
				g = gdisp;
				pixScale = 1000;
			}
			TextType text = new TextType();
			text.setTextLength(Integer.toString(MIN_FONT_SIZE));
			text.setId(ResultsBean.createId("t"));
			text.setContent("*");
			text.setX(Integer.toString((int) (anchor[X_OFFSET] * pixScale)));
			text.setY(Integer.toString((int) (anchor[Y_OFFSET] * pixScale)));
			text.setFontSize(Integer.toString(MIN_FONT_SIZE));
			g.getChildren().add(text);
		}
	}

	/** Get the maximum line length of the set of lines provided */
	public static int lineLength(String[] lines) {
		int ret = lines[0].length();
		for (int i = 1; i < lines.length; i++) {
			if (ret < lines[i].length())
				ret = lines[i].length();
		}
		return ret;
	}

	/**
	 * Converts a string contain LF, CR/LF, LF/CR or CR into a set of lines by
	 * themselves.
	 * 
	 * @param unformatted
	 * @return Array of strings, one per line.
	 */
	public static String[] convertToLines(String unformatted) {
		int lfPos = unformatted.indexOf('\n');
		int crPos = unformatted.indexOf('\r');
		if (crPos < 0 && lfPos < 0) {
			return new String[] { unformatted };
		}
		String regex;
		if (lfPos == -1)
			regex = "\r";
		else if (crPos == -1)
			regex = "\n";
		else if (crPos < lfPos)
			regex = "\r\n";
		else
			regex = "\n\r";
		String[] ret = unformatted.split(regex);
		return ret;
	}

	/**
	 * Adds the graphic object go to svg:g. There are 5 subtypes of graphic
	 * objects: POINT, POLYLINE, INTERPOLATED, CIRCLE and ELLIPSE Each one of
	 * these needs to be handled separately.
	 */
	protected void addGraphicObject(GType g, GraphicObject go, float pixSize) {
		String type = go.getGraphicType();
		if (type.equalsIgnoreCase(GraphicObject.POLYLINE)) {
			addPolyline(g, go, pixSize,false);
		} else if (type.equalsIgnoreCase(GraphicObject.CIRCLE)) {
			addCircle(g, go, pixSize);
		} else if (type.equalsIgnoreCase(GraphicObject.ELLIPSE)) {
			addEllipse(g, go, pixSize);
		} else if (type.equalsIgnoreCase(GraphicObject.POINT)) {
			addPoint(g, go, pixSize);
		} else if (type.equalsIgnoreCase(GraphicObject.INTERPOLATED)) {
			// TODO replace this with something better.
			addPolyline(g, go, pixSize, true);
		} else {
			log.warn("Unsupported graphic object type:" + type);
		}
	}

	/** Adds an ellipse SVG to the graphic object */
	protected void addEllipse(GType g, GraphicObject go, float pixSize) {
		float[] points = go.getGraphicData();
		if (points == null || points.length != 8) {
			log.warn("Invalid ellipse data provided in graphic object.");
			return;
		}
		for (int i = 0; i < points.length; i++)
			points[i] *= pixSize;

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
			ellipse.setFill("none");
		}
		g.getChildren().add(ellipse);
	}

	/**
	 * Converts a bounding box to a rotation amount. Returns a multiple of 90, as
	 * that is the only bounding information that we can tell from just the
	 * bounding box.
	 */
	public static int toRotation(float[] topLeft, float[] bottomRight, boolean flip) {
		float x1 = topLeft[X_OFFSET];
		float x2 = bottomRight[X_OFFSET];
		if (flip) {
			float xt = x1;
			x1 = x2;
			x2 = xt;
		}
		float y1 = topLeft[Y_OFFSET];
		float y2 = bottomRight[Y_OFFSET];
		log.info("toRotation x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2=" + y2);
		if (x1 < x2) {
			if (y1 < y2) {
				return 0;
			}
			return 270;
		} else {
			if (y1 > y2) {
				return 180;
			}
			return 90;
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
	 *           in degrees. Doesn't work for VML if rotation isn't 0 or 90.
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
			sx = (float) (Math.cos(rotation * Math.PI / 180f) * rx);
			sy = (float) (Math.sin(rotation * Math.PI / 180f) * rx);
			log.warn("TODO: broken for displaying rotated ellipses in IE, rotation={}", rotation);
		}
		float topX = cx - sx;
		float topY = cy - sy;
		StringBuffer d = new StringBuffer("M");
		d.append((int) topX).append(",").append((int) topY);

		StringBuffer v = new StringBuffer(d);
		d.append(" A").append((int) rx).append(',').append((int) ry);
		d.append(" ").append((int)rotation).append(" 1,0 ");
		d.append((int) (cx + sx)).append(',').append((int) (cy + sy));
		d.append(" A").append((int) rx).append(',').append((int) ry);
		d.append(" ").append((int) rotation).append(" 1,0 ");
		d.append((int) topX).append(',').append((int) topY);

		// TODO fix this for arbitrary rotations around cx,cy
		v.append(" at ").append((int) (cx - rx)).append(',');
		v.append((int) (cy - ry)).append(' ');
		v.append((int) (cx + rx)).append(',');
		v.append((int) (cy + ry)).append(' ');
		v.append((int) topX).append(',').append((int) topY).append(' ');
		v.append((int) topX).append(',').append((int) topY);

		circle.setD(d.toString());
		circle.setV(v.toString());
		circle.setId(ResultsBean.createId("el"));
		return circle;
	}

	/**
	 * Adds a point SVG to the graphic object, as a circle of radius 0.5 centered
	 * on the given position.
	 */
	protected void addPoint(GType g, GraphicObject go, float pixSize) {
		float[] points = go.getGraphicData();
		if (points == null || points.length != 2) {
			log.warn("Invalid point data provided in graphic object.");
			return;
		}
		for (int i = 0; i < points.length; i++)
			points[i] *= pixSize;
		PathType circle = createEllipsePath(points[X_OFFSET], points[Y_OFFSET], 1f, 1f, 0f);
		g.getChildren().add(circle);
	}

	/** Adds a circle SVG to the graphic object */
	protected void addCircle(GType g, GraphicObject go, float pixSize) {
		float[] points = go.getGraphicData();
		if (points == null || points.length != 4) {
			log.warn("Invalid circle data provided in graphic object.");
			return;
		}
		for (int i = 0; i < points.length; i++)
			points[i] *= pixSize;
		float r = length(points, 0, 2);
		PathType circle = createEllipsePath(points[0], points[1], r, r, 0f);
		circle.setPrType("Circle");
		if (!go.getGraphicFilled()) {
			circle.setFill("none");
		}
		g.getChildren().add(circle);
	}

	/**
	 * Adds a polyline graphic object as a child of g.
	 * 
	 * @param g
	 *           to add the svg to
	 * @param go
	 *           to add the polyline from.
	 */
	protected void addPolyline(GType g, GraphicObject go, float pixSize, boolean smooth) {
		float[] points = go.getGraphicData();
		if (points == null || points.length < 2) {
			log.warn("Invalid polyline provided in graphic object.");
			return;
		}
		for (int i = 0; i < points.length; i++)
			points[i] *= pixSize;
		PathType path = new PathType();
		StringBuffer v = new StringBuffer();
		appendPoint(v,"M",points,0);
        StringBuffer d = null;
        boolean closed = false;
        if( smooth ) {
            d = new StringBuffer(v);
            closed = (points[0]==points[points.length-2] && points[1]==points[points.length-1]); 
        }
		// Start at 3 so that if the length happens to be odd, we don't throw an
		// exception, but just ignore the extra value.
		for (int i = 3; i < points.length; i += 2) {
			// Note the order is x,y NOT y,x as it is for shutters (shudder)
			appendPoint(v," L",points,i - 1);
			if( smooth ) {
			    float[] c1 = new float[]{points[i-3],points[i-2]};
			    float[] c2 = new float[]{points[i-1],points[i]};
			    int prevPosn = i-5;
			    int nextPosn = i+1;
			    if( prevPosn < 0 && closed ) prevPosn = points.length-4;
			    if( nextPosn >= points.length && closed) nextPosn = 2;
			    if( nextPosn < points.length ) {
                    c2[0] = c2[0]+(c2[0] - points[nextPosn])/10;
                    c2[1] = c2[1]+(c2[1] - points[nextPosn+1])/10;
			    }
			    if( prevPosn >=0 ) {
			        c1[0] = c1[0]+(c1[0] - points[prevPosn])/10;
			        c1[1] = c1[1]+(c1[1] - points[prevPosn+1])/10;
			    }
			    appendPoint(d,"C ", c1, 0);
			    appendPoint(d," ", c2, 0);
			    appendPoint(d," ", points, i-1);
	            appendPoint(d," L",points,i - 1);
			}
		}
		if( smooth ) {
	        path.setD(d.toString());
		    path.setV(v.toString());
		} else {
		    path.setD(v.toString());
		}
		path.setId(ResultsBean.createId("p"));
		if (!go.getGraphicFilled()) {
			path.setFill("none");
			path.setStrokeWidth("2");
		}
		g.getChildren().add(path);
	}

	/** Add a point to the end of d, along with base. */
	public static void appendPoint(StringBuffer d, String base, float[] points, int i) {
	    d.append(base);
	    d.append(points[i]);
	    d.append(",");
	    d.append(points[i+1]);
    }

    /**
	 * Adds min/max pixel information from the GSPS instead of from the image
	 * header.
	 */
	protected void addMinMaxPixelInfo(DicomObject pr, Map<String, ImageBean> items) {
		MinMaxPixelMacro minMaxPixelMacro = null;
		DicomObject mLut = pr.getNestedDicomObject(Tag.ModalityLUTSequence);
		if (mLut != null) {
			float[] cw = VOIUtils.getMinMaxWindowCenterWidth(null, pr, 1, null);
			minMaxPixelMacro = new MinMaxPixelMacro(cw[0] - cw[1] / 2, cw[0] + cw[1] / 2);
		}
		// else if( pr.getFloat(Tag.RescaleSlope)!=0f ) {
		// TODO - Has an explicit rescale slope/intercept - store it for later
		// use on a per-image basis.
		// }

		WindowLevelMacro globalWL = null;
		boolean globalVOI = false;
		DicomElement voisq = pr.get(Tag.SoftcopyVOILUTSequence);
		if (voisq == null)
			globalVOI = true;
		else if (voisq.countItems() == 1) {
			DicomObject softVoi = voisq.getDicomObject();
			if (!softVoi.contains(Tag.ReferencedImageSequence)) {
				globalVOI = true;
				float center = softVoi.getFloat(Tag.WindowCenter);
				float width = softVoi.getFloat(Tag.WindowWidth);
				if (width != 0f) {
					globalWL = new WindowLevelMacro(center, width, softVoi.getString(Tag.WindowCenterWidthExplanation, ""));
				}
			}
		}
		for (String uid : items.keySet()) {
			log.info("Adding min/max pixel information to " + uid);

			ImageBean image = items.get(uid);
			MacroItems macroItems = getMacroItems(items, uid);
			int frame = 0;
			int frameIndex = uid.lastIndexOf(",");
			if (frameIndex > 0) {
				frame = Integer.parseInt(uid.substring(1 + frameIndex));
				uid = uid.substring(0, frameIndex);
			} else if (image.getNumberOfFrames() == 1)
				frame = 1;

			if (minMaxPixelMacro != null)
				macroItems.addMacro(minMaxPixelMacro);
			if (globalWL != null)
				macroItems.addMacro(globalWL);
			if (!globalVOI) {
				DicomObject voiObj = VOIUtils.selectVoiItemFromPr(uid, pr, frame);
				if (voiObj != null) {
					float center = voiObj.getFloat(Tag.WindowCenter);
					float width = voiObj.getFloat(Tag.WindowWidth);
					if (width != 0f) {
						macroItems.addMacro(new WindowLevelMacro(center, width, voiObj.getString(
						      Tag.WindowCenterWidthExplanation, "")));
					}
				} else if (frame == 0) {
					// Must be a multi-frame, referenced in it's entirety by the
					// GSPS, but partially by the VOI items
					Map<int[], DicomObject> framesVoi = selectVoiForFrames(uid, pr);
					if (framesVoi == null)
						continue;
					for (Map.Entry<int[], DicomObject> me : framesVoi.entrySet()) {
						int[] frames = me.getKey();
						voiObj = me.getValue();
						float center = voiObj.getFloat(Tag.WindowCenter);
						float width = voiObj.getFloat(Tag.WindowWidth);
						if (width == 0f)
							continue;
						WindowLevelMacro wlm = new WindowLevelMacro(center, width, voiObj.getString(
						      Tag.WindowCenterWidthExplanation, ""));
						for (int f : frames) {
							image.getFrameMacroItems(f).addMacro(wlm);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns a map of frame number to Voi area dicom objects for the given UID.
	 * May return null if no Voi is to be applied.
	 */
	public static Map<int[], DicomObject> selectVoiForFrames(String iuid, DicomObject pr) {
		DicomElement voisq = pr.get(Tag.SoftcopyVOILUTSequence);
		if (voisq == null) {
			return null;
		}
		Map<int[], DicomObject> ret = null;
		for (int i = 0, n = voisq.countItems(); i < n; i++) {
			DicomObject item = voisq.getDicomObject(i);
			DicomElement refImgs = item.get(Tag.ReferencedImageSequence);
			for (int j = 0, m = refImgs.countItems(); j < m; j++) {
				DicomObject refImage = refImgs.getDicomObject(j);
				if (iuid.equals(refImage.getString(Tag.ReferencedSOPInstanceUID))) {
					int[] frames = refImage.getInts(Tag.ReferencedFrameNumber);
					if (ret == null)
						ret = new HashMap<int[], DicomObject>();
					ret.put(frames, item);
				}
			}
		}
		return ret;
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
				if (studyIt.getStudyUID() == study.getStudyUID()) {
					if (study == studyIt)
						return gspsType;
					String seriesUid = dcmobj.getString(Tag.SeriesInstanceUID);
					for (SeriesType series : studyIt.getSeries()) {
						if (series.getSeriesUID().equals(seriesUid)) {
							// It has the series, so just add the gsps object, which
							// it should not have.
							for (DicomObjectType dot : series.getDicomObject()) {
								if (dot.getObjectUID().equals(gspsType.getObjectUID())) {
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
					SeriesType sb = (SeriesType) study.getChildById(SeriesBean.key(seriesUid));
					assert sb != null;
					studyIt.getSeries().add(sb);
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
	 * same for every image. This isn't necessarily a good assumption, but should
	 * be good enough for now. It could be tested explicitly if required.
	 * 
	 * @param dcmobj
	 * @param gspsType
	 * @param images
	 */
	protected void addShutterToResults(DicomObject dcmobj, GspsType gspsType, Map<String, ImageBean> images) {
		if (images == null || images.size() == 0)
			return;

		// Since we need the width and height to display the shutter, an example
		// will be computed here.
		// This isn't strictly legal, as the images don't all have to be the
		// same size, but logically they
		// had better be in order for the shutter to be meaningful.
		ImageBean exemplar = images.values().iterator().next();
		String width = exemplar.getColumns().toString();
		String height = exemplar.getRows().toString();

		DisplayShutterModule shutter = new DisplayShutterModule(dcmobj);
		String[] shapes = shutter.getShutterShapes();
		if (shapes == null || shapes.length == 0) {
			log.info("No display shutters.");
			return;
		}
		GType g = getG(gspsType, "shutter");
		Use use = new Use();
		for (int i = 0; i < shapes.length; i++) {
			String shape = shapes[i];
			if (shape.equalsIgnoreCase("CIRCULAR")) {
				addCircularShutter(gspsType, shutter, g, width, height);
			} else if (shape.equalsIgnoreCase("POLYGONAL")) {
				addPolygonalShutter(gspsType, shutter, false, g, width, height);
			} else if (shape.equalsIgnoreCase("RECTANGULAR")) {
				addPolygonalShutter(gspsType, shutter, true, g, width, height);
			} else if (shape.equalsIgnoreCase("BITMAP")) {
				use.setClazz("Overlay");
				int layer = dcmobj.getInt(Tag.ShutterOverlayGroup);
				String rgb = toRGB(shutter.getShutterPresentationValue(), shutter.getFloatLab(), null);
				if (layer == 0) {
					log.warn("Specified invalid layer 0 for bitmap.");
					continue;
				}
				if (layer < 0x10000)
					layer <<= 16;
				addOverlay(dcmobj, layer, rgb, images);
			} else {
				log.warn("Unknown shutter shape:" + shape + " on " + dcmobj.getString(Tag.SOPInstanceUID));
			}
		}
		use.setHref("#" + g.getId());
		use.setId(ResultsBean.createId("u"));
		addUse(images, use, null);
	}

	/**
	 * Adds a bitmap shutter - directly to the image objects as required. The
	 * reasoning is that it needs to be able to specify the object UID in with
	 * the image if necessary.
	 * 
	 * @param dcmObj
	 *           is the Dicom object that includes the shutter information.
	 * @param g
	 * @param width
	 * @param height
	 * @return the class of the overlay - either Overlay or ImageOverlay - if it
	 *         is ImageOverlay, then it should be included differently as the
	 *         objectUID and frameNumber need to be added. Can also be
	 *         OverlayMultiframe, in which case the frameNumber is needed, but
	 *         not the objectUID.
	 */
	private void addOverlay(DicomObject dcmObj, int layer, String rgb, Map<String, ImageBean> images) {
		StringBuffer href = new StringBuffer(wadoUrl).append("?studyUID=1&seriesUID=1&relative=image");
		if (images.size() == 0) {
			log.warn("Can't add overlays when no images specified.");
			return;
		}
		boolean fromGsps = dcmObj.containsValue(Tag.OverlayRows | layer);
		String presentationUid = dcmObj.getString(Tag.SOPInstanceUID);
		ImageType bitmap = null;
		layer = (layer >> 16) & 0xFF;
		href.append("&overlay=").append(layer);
		if (rgb != null && !rgb.equals("")) {
			// It doesn't work well to have a # in a URL...
			if (rgb.startsWith("#"))
				rgb = rgb.substring(1);
			// Doesn't seem to work with all-black in the browser for some
			// reason....
			if (rgb.equalsIgnoreCase("ffffff"))
				rgb = "fefefe";
			href.append(":").append(rgb);
		}
		if (fromGsps) {
			bitmap = new ImageType();
			bitmap.setId(presentationUid+"-l"+layer);
			href.append("&objectUID=");
			href.append(presentationUid);
			bitmap.setHref(href.toString());
			ImageBean exemplar = images.entrySet().iterator().next().getValue();
			bitmap.setWidth(exemplar.getColumns().toString());
			bitmap.setHeight(exemplar.getRows().toString());
		}
		String baseHref = href.toString();

		// Add the bitmaps to the image.
        // TODO - add multiframe support
		int i=0;
		for (Map.Entry<String, ImageBean> me : images.entrySet()) {
			ImageType addOvly;
			ImageBean ib = me.getValue();
			if(fromGsps) {
				addOvly = bitmap; 
			} else {
				addOvly = new ImageType();
				addOvly.setHref(baseHref+"&objectUID="+ib.getObjectUID());
				addOvly.setWidth(ib.getColumns().toString());
				addOvly.setHeight(ib.getRows().toString());
				addOvly.setId(presentationUid+"-l"+layer+"-i"+i);
			}
			ib.getMacroItems().addElement(addOvly);
			i++;
		}
	}

	/**
	 * Adds a rectangular shutter to the group g provided. Defines the clip path
	 * inside the svg child of gspsType.
	 * 
	 * @param gspsType
	 *           to add the clip path to.
	 * @param shutter
	 *           information
	 * @param useRect
	 *           to use the rectangle as the polygon.
	 * @param g
	 *           to add the shutter to.
	 * @param width
	 *           of the image to apply to.
	 * @param height
	 *           of the image to apply to.
	 */
	protected void addPolygonalShutter(GspsType gspsType, DisplayShutterModule shutter, boolean useRect, GType g,
	      String width, String height) {
		log.info("Found a rectangular shutter.");
		PathType path = new PathType();

		StringBuffer d = new StringBuffer("M0,0");
		d.append(" L0,").append(height);
		d.append(" L").append(width).append(',').append(height);
		d.append(" L").append(width).append(",0").append(" L0,0");

		// Then remove the polygon in the center
		if (useRect) {
			d.append(" L").append(shutter.getShutterLeftVerticalEdge()).append(',').append(
			      shutter.getShutterUpperHorizontalEdge());
			d.append(" L").append(shutter.getShutterRightVerticalEdge()).append(',').append(
			      shutter.getShutterUpperHorizontalEdge());
			d.append(" L").append(shutter.getShutterRightVerticalEdge()).append(',').append(
			      shutter.getShutterLowerHorizontalEdge());
			d.append(" L").append(shutter.getShutterLeftVerticalEdge()).append(',').append(
			      shutter.getShutterLowerHorizontalEdge());
			d.append(" L").append(shutter.getShutterLeftVerticalEdge()).append(',').append(
			      shutter.getShutterUpperHorizontalEdge());
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
		path.setStroke(rgb);
		path.setFill(rgb);
		path.setId(ResultsBean.createId("shre"));
		path.setD(d.toString());
		path.setStrokeWidth("5");

		g.getChildren().add(path);
	}

	/**
	 * Adds a circular shutter to the group g provided. Defines the clip path
	 * inside the svg child of gspsType. Defines both SVG and VML parameters on
	 * the svg path, so that it is easy to convert to a VML shutter if required.
	 * 
	 * @param gspsType
	 *           to add the clip path to.
	 * @param shutter
	 *           information
	 * @param g
	 *           to add the shutter to.
	 * @param width
	 *           of the image to apply to.
	 * @param height
	 *           of the image to apply to.
	 */
	protected void addCircularShutter(GspsType gspsType, DisplayShutterModule shutter, GType g, String width,
	      String height) {
		log.debug("Found a circular shutter.");
		int[] center = shutter.getCenterOfCircularShutter();
		int radius = shutter.getRadiusOfCircularShutter();
		if (center == null || radius <= 2 || center.length != 2) {
			log.warn("Invalid circular shutter.");
			return;
		}
		int centerX = center[1];
		int centerY = center[0];
		
		log.debug("Adding circular shutter to object.");
		PathType path = new PathType();
		StringBuffer d = new StringBuffer("M");
		d.append(centerX).append(',').append(centerY - radius);
		d.append(" L0,0 L");
		d.append(width).append(",0 L").append(width).append(',').append(height);
		d.append(" L0,").append(height).append(" L0,0 L");
		d.append(centerX).append(',').append(centerY - radius);
		StringBuffer v = new StringBuffer(d);
		d.append(" A").append(radius).append(',').append(radius);
		d.append(" 0 1,0 ");
		d.append(centerX).append(',').append(centerY + radius);
		d.append(" A").append(radius).append(',').append(radius);
		d.append(" 0 1,0 ");
		d.append(centerX).append(',').append(centerY - radius);

		v.append(" at ").append(centerX - radius).append(',');
		v.append(centerY - radius).append(' ');
		v.append(centerX + radius).append(',');
		v.append(centerY + radius).append(' ');
		v.append(centerX).append(',').append(centerY - radius).append(' ');
		v.append(centerX).append(',').append(centerY - radius);
		String rgb = toRGB(shutter.getShutterPresentationValue(), shutter.getFloatLab(), null);
		path.setStroke(rgb);
		path.setFill(rgb);
		path.setId(ResultsBean.createId("shcr"));
		path.setD(d.toString());
		path.setV(v.toString());
		path.setStrokeWidth("4");
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
				log.info("L*a*b* " + labColour[0] + "," + labColour[1] + "," + labColour[2] + " sRGB " + r + "," + g + ","
				      + b + " colour space type id " + lab.getType());
			}
		} else if (rgbColour != null) {
			r = rgbColour[0];
			g = rgbColour[1];
			b = rgbColour[2];
			if( r>255 || g>255 || b>255 ) {
			    r >>= 8;
				g >>= 8;
			    b >>= 8;
			}
		} else {
			r = g = b = (pGray >> 8);
		}
		r &= 0xFF;
		g &= 0xFF;
		b &= 0xFF;
		int conv = (r << 16) | (g << 8) | b | 0x1000000;
		String ret = "#" + Integer.toHexString(conv).substring(1);
		return ret;
	}

	/** Creates an instance of a grouping object. */
	protected GType getG(GspsType gspsType, String name) {
		SvgType svg = getSvg(gspsType);
		GType g = new GType();
		g.setId(gspsType.getObjectUID() + "-" + name);
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
			svg.setId("svg"+gspsType.getObjectUID());
			gspsType.setSvg(svg);
		}
		return svg;
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
				GspsEncoded encoded = (GspsEncoded) ((StudyBean) st).getMacroItems().findMacro(GspsEncoded.class);
				if (encoded == null) {
					encoded = new GspsEncoded();
					((StudyBean) st).getMacroItems().addMacro(encoded);
				}
				GspsEncoded updated = new GspsEncoded();
				for (SeriesType se : st.getSeries()) {
					for (DicomObjectType dot : se.getDicomObject()) {
						if (!(dot instanceof ImageBean))
							continue;
						ImageBean image = (ImageBean) dot;
						if (image.getGspsUID() == null)
							continue;
						String gsps = image.getGspsUID();
						if (encoded.encoded(gsps)) {
							continue;
						}
						updated.addGspsUID(gsps);
						StudyBean studyBean = gspsUids.get(image.getGspsUID());
						if (studyBean == null) {
							StudyBean originalStudy = ((StudyBean) st).getOriginalStudy();
							gspsUids.put(image.getGspsUID(), originalStudy);
							log.info("GSPS " + image.getGspsUID() + " references study " + originalStudy.getStudyUID());
						}
					}
				}
				encoded.addAll(updated);
			}
		}
		return gspsUids;
	}

	public String getWadoUrl() {
   	return wadoUrl;
   }

	/** Sets the WADO URL - defaults to /wado2/wado */
	@MetaData(required=false)
	public void setWadoUrl(String wadoUrl) {
   	this.wadoUrl = wadoUrl;
   }
}
