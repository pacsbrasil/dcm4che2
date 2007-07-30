package org.dcm4chee.xero.image;

import java.awt.color.ColorSpace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.iod.module.pr.DisplayShutterModule;
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
import org.w3.svg.ClipFillRuleType;
import org.w3.svg.ClipPathType;
import org.w3.svg.DefsType;
import org.w3.svg.GType;
import org.w3.svg.RectType;
import org.w3.svg.SvgType;
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
	private static final Logger log = LoggerFactory.getLogger(GspsEncode.class);

	private static final ColorSpace lab = null; // ColorSpace.getInstance(ColorSpace.TYPE_Lab);

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
			log
					.info("Parsing DICOM object - can we create some IOD's for this?");
			addShutterToResults(dcmobj, gspsType, images);
		}
		return results;
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
		GspsType gspsType = (GspsType) results.getChildren().get(
				dcmobj.getString(Tag.SOPInstanceUID));
		if (gspsType == null)
			throw new NullPointerException(
					"Something went wrong in adding results object - null gspsType.");
		return gspsType;
	}

	protected void addShutterToResults(DicomObject dcmobj, GspsType gspsType,
			List<ImageBean> images) {
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
		for (int i = 0; i < shapes.length; i++) {
			String shape = shapes[i];
			if (shape.equalsIgnoreCase("circular")) {
				log.info("Found a circular shutter.");
				int[] center = shutter.getCenterOfCircularShutter();
				int radius = shutter.getRadiusOfCircularShutter();
				if (center == null || radius == 0.0 || center.length != 2) {
					log.warn("Invalid circular shutter on "
							+ dcmobj.getString(Tag.SOPInstanceUID));
					continue;
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
				String rgb = toRGB(shutter.getShutterPresentationValue(),
						shutter.getFloatLab());
				rect.setStyle("clip-path:url(#" + clip.getId() + "); fill: "
						+ rgb + ";");
				rect.setClipPath("url(#" + clip.getId() + ")");
				GType g = getG(gspsType, "shutter.circular");
				g.getChildren().add(rect);

				UseType useG = new UseType();
				useG.setHref("#" + g.getId());
				for (ImageBean image : images) {
					image.getUse().add(useG);
				}
			}
		}
	}

	/**
	 * Converts the gray or colour values to RGB values . TODO implement this
	 * correctly.
	 * 
	 * @return String representation of the colour.
	 */
	public static String toRGB(int pGray, float[] labColour) {
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
			}
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
	public static DicomObject readDicomHeader(FilterItem filterItem,
			Map<String, Object> params, String uid) {
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
	private void initGspsUidsMap(ResultsBean results,
			Map<String, List<ImageBean>> gspsUids) {
		for (PatientType pt : results.getPatient()) {
			for (StudyType st : pt.getStudy()) {
				for (SeriesType se : st.getSeries()) {
					for (DicomObjectType dot : se.getDicomObject()) {
						if (!(dot instanceof ImageBean))
							continue;
						ImageBean image = (ImageBean) dot;
						if (image.getGspsUID() != null) {
							List<ImageBean> l = gspsUids
									.get(image.getGspsUID());
							if (l == null) {
								l = new ArrayList<ImageBean>();
								gspsUids.put(image.getGspsUID(), l);
							}
							l.add(image);
							log.info("Image " + image.getSOPInstanceUID()
									+ " references GSPS " + image.getGspsUID());
						}
					}
				}
			}
		}
	}
}
