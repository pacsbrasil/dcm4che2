package org.dcm4chee.xero.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.iod.module.pr.DisplayShutterModule;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsBean;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class enocdes GSPS as XML in either VML or SVG formats.  This class will encode
 * data based on GSPS UID's for all GSPS UID's referenced from ImageBean objects.
 * 
 * There are several types of data that can be encoded:
 * <ol>
 * <li>C.11.12 Presentation State Shutter defines the colour of the shutter.  This is encoded as rgb values on the shutter svg object.</li>
 * <li>C.11.13 Presentation State Mask  TODO - not encoded right now.</li>
 * <li>C.7.6.10 Mask (subtraction) TODO - not encoded right now.</li>
 * <li>C.7.6.11 Display Shutter is encoded as an svg shape under the study level.</li>
 * <li>C.7.6.15 Bitmap Display Shutter - svg shape referencing WADO url for the bitmap image</li>
 * <li>C.9.2 Overlay Plane and C.11.7 Overlay Activation - encoded as image object.</li>
 * <li>C.10.4 Displayed area - encoded as topLeft, bottomRight on image node it applies to.</li>
 * <li>C.10.5 Graphic Annotation - encoded as shape at study level as a shape.</li>
 * <li>C.10.6 Spatial Transformation - encoded as rotate and flip flags on image.</li>
 * <li>C.10.7 Graphic Layer encoded as use elements to the overlays, annotations etc.</li>
 * <li>C.11.1 Modality LUT - only encoded if it differs from image modality LUT, and if so, encoded as lut=UID of GSPS object on image.</li>
 * <li>C.11.8 VOI LUT - encoded as window level/width, OR, if there is a real VOI LUT in the GSPS that differs from the original image, then encoded as lut=UID as above. </li>
 * <li>C.11.6 Presentation LUT - Encoded as inverse=true/false</li>
 * </ol>
 * 
 * Different parts are encoded by different classes, and the order of encoding is somewhat important.
 * As far as the figuring out what to encode, it is assumed that the objects have gspsUID attached to them
 * by the point that this filter is used.
 * 
 * @author bwallace
 *
 */
public class GspsEncode  implements Filter<ResultsBean> {
	private static final Logger log = LoggerFactory.getLogger(GspsEncode.class);

	public ResultsBean filter(FilterItem filterItem, Map<String, Object> params) {
		ResultsBean results = (ResultsBean) filterItem.callNextFilter(params);
		Map<String,List<ImageBean>> gspsUids = new HashMap<String,List<ImageBean>>();
		if( results==null ) return null;
		initGspsUidsMap(results, gspsUids);
		if( gspsUids.size()==0 ) {
			log.info("No GSPS UID's referenced for study results.");
			return results;
		}
		for(Map.Entry<String,List<ImageBean> > me : gspsUids.entrySet()) {
			String gspsUid = me.getKey();
			List<ImageBean> images = me.getValue();
			DicomObject dcmobj = readDicomHeader(filterItem, params, gspsUid);
			if( dcmobj==null ) {
				log.warn("Couldn't read GSPS for uid "+gspsUid);
				continue;
			}
			log.info("Parsing DICOM object - can we create some IOD's for this?");
			addShutterToResults(dcmobj, results, images);
		}
		return results;
	}

	protected void addShutterToResults(DicomObject dcmobj, ResultsBean results, List<ImageBean> images) {
		DisplayShutterModule shutter = new DisplayShutterModule(dcmobj);
		String[] shapes = shutter.getShutterShapes();
		if( shapes==null || shapes.length==0 ) {
			log.info("No display shutters.");
			return;
		}
		for(int i=0; i<shapes.length; i++ ) {
			String shape = shapes[i];
			if( shape.equalsIgnoreCase("circular") ) {
				log.info("Found a circular shutter.");
				float[] center = shutter.getCenterOfCircularShutter();
				float radius = shutter.getRadiusOfCircularShutter();
				if( center==null || radius==0.0 || center.length!=2 ) {
					log.warn("Invalid circular shutter on "+dcmobj.getString(Tag.SOPInstanceUID));
					continue;
				}
				log.info("Should add circular shutter to object.");
			}
		}
	}

	/** Reads the dicom header for the specified SOP Instance UID and returns it. */
	public static DicomObject readDicomHeader(FilterItem filterItem, Map<String, Object> params, String uid) {
		// This might come from a different series or even study, so don't assume anything here.
		Map<String,Object> newParams = new HashMap<String,Object>();
		newParams.put("objectUID", uid);
		return (DicomObject) filterItem.callNamedFilter("dicom",newParams);
	}


	/**
	 * Initialize the map of gspsUids from the results, where every gsps uid in an image
	 * gets mapped to the list of image beans containing that image.
	 * @param results
	 * @param gspsUids
	 */
	private void initGspsUidsMap(ResultsBean results, Map<String, List<ImageBean>> gspsUids) {
		for( PatientType pt : results.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				for(SeriesType se : st.getSeries() ) {
					for(DicomObjectType dot : se.getDicomObject()) {
						if( ! (dot instanceof ImageBean) ) continue;
						ImageBean image = (ImageBean) dot;
						if( image.getGspsUID()!=null ) {
							List<ImageBean> l = gspsUids.get(image.getGspsUID());
							if( l==null ) {
								l = new ArrayList<ImageBean>();
								gspsUids.put(image.getGspsUID(),l);
							}
							l.add(image);
							log.info("Image "+image.getSOPInstanceUID() +" references GSPS "+image.getGspsUID());
						}
					}
				}
			}
		}
	}
}
