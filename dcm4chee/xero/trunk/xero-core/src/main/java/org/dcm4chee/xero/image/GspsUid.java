package org.dcm4chee.xero.image;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsBean;
import org.dcm4chee.xero.search.study.ImageType;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class is an image level filter that sets gspsUid on image objects.   
 * It takes a gsps=NAME or koUID=uid in the params list and applies the most current GSPS
 * or the named key object GSPS to the images.
 * It does this by looking for:
 * 	Modality=KO
 *  Content Label=<GSPS-NAME>
 *  Study UID=<STUDY-UID>
 * If there is more than one study, it does it as a grouped filter for all studies at once.
 * It then uses the GSPS objects created under the results tree to associate images with GSPS objects.
 * These objects have a list of associated images, by object UID/frame number.
 */
public class GspsUid  implements Filter<ResultsBean> {
	private static final Logger log = LoggerFactory.getLogger(GspsUid.class);

	public ResultsBean filter(FilterItem filterItem, Map<String, Object> params) {
		String gspsName = (String) params.get(GspsDiscover.GSPS_KEY);
		if( gspsName==null ) {
			log.debug("Not applying GSPS Uid lookup.");
			return (ResultsBean) filterItem.callNextFilter(params);
		}
		log.debug("Looking for GSPS "+gspsName);
		ResultsBean rb = (ResultsBean) filterItem.callNextFilter(params);
		if( rb==null ) return null;
		ResultsBean gspsResults = GspsDiscover.queryForGsps(filterItem,rb);
		if( gspsResults==null ) return rb;
		
		Map<String,GspsBean> uidToGsps = new HashMap<String,GspsBean>();
		for( PatientType pt : gspsResults.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				for(SeriesType se : st.getSeries() ) {
					for(DicomObjectType dot : se.getDicomObject()) {
						if( ! (dot instanceof GspsBean) ) continue;
						GspsBean gsps = (GspsBean) dot;
						if( !gsps.getContentLabel().equals(gspsName)) continue;
						log.debug("Add GSPS "+gsps.getSOPInstanceUID() + " to  gspsResults map.");
						for(String uid : gsps.getReferencedSOPInstance() ) {
							GspsBean oldGsps = uidToGsps.get(uid);
							if( oldGsps==null || oldGsps.getPresentationDateTime().compare(gsps.getPresentationDateTime())<0 ) {
								log.debug("Putting a GSPS match for "+uid+" to "+gsps.getSOPInstanceUID());
								uidToGsps.put(uid,gsps);
							}
						}
					}
				}
			}
		}
		
		addGspsUids(rb, uidToGsps);
		return rb;
	}

	private void addGspsUids(ResultsBean rb, Map<String, GspsBean> uidToGsps) {
		for( PatientType pt : rb.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				for(SeriesType se : st.getSeries() ) {
					for(DicomObjectType dot : se.getDicomObject()) {
						if( !(dot instanceof ImageType) ) continue;
						GspsBean gspsBean = uidToGsps.get(dot.getSOPInstanceUID());
						if( gspsBean==null ) {
							log.debug("GSPS not found for "+dot.getSOPInstanceUID());
							continue;
						}
						ImageType image = (ImageType) dot;
						image.setGspsUID(gspsBean.getSOPInstanceUID());
					}
				}
			}
		}
	}

}
