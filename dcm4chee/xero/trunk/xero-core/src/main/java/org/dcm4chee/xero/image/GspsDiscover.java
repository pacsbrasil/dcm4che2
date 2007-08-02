package org.dcm4chee.xero.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a series level filter that performs lookups to figure out the default GSPS object to apply to a study, if any.
 * There are 3 ways to specify GSPS status:
 * gsps=*
 * will find the latest GSPS and attempt to apply it.
 * 
 * gsps=<NAME>{,<NAME>}*
 * will find the first named GSPS matching the pattern, and attempt to apply it.
 * 
 * koUID=UID
 * will find a KO document or a structured report containing objects and apply those GSPS objects.
 * This over-rides gsps for those images where both exist.  TODO implement this one.
 * 
 * @author bwallace
 */
public class GspsDiscover implements Filter<ResultsBean> {
    private static final String[] STRING_ARRAY_TYPE = new String[0];

	private static final Logger log = LoggerFactory.getLogger(GspsDiscover.class);

	public static final String GSPS_KEY = "gsps";

	public ResultsBean filter(FilterItem filterItem, Map<String, Object> params) {
		String gspsNames = (String) params.get(GSPS_KEY);
		if( gspsNames==null ) {
			log.debug("Not applying GSPS discovery.");
			return (ResultsBean) filterItem.callNextFilter(params);
		}
		log.debug("Looking for GSPS "+gspsNames+" - TODO fix name, looking for * right now.");
		ResultsBean rb = (ResultsBean) filterItem.callNextFilter(params);
		if( rb==null ) return null;
		ResultsBean gspsResults = queryForGsps(filterItem,rb);
		if( gspsResults==null ) return rb;
		for( PatientType pt : rb.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				GspsType gspsType = findMostRecentGsps(st, gspsResults,gspsNames);
				if( gspsType!=null ) {
					log.debug("Setting GSPS to apply for "+st.getStudyInstanceUID()+" to "+gspsType.getContentLabel());
					st.setGspsLabel(gspsType.getContentLabel());
				}
			}
		}
		return rb;
	}
	
	/** Finds the most recent GSPS matching the given content names */
	private GspsType findMostRecentGsps(StudyType st, ResultsBean gspsResults, String gspsNames) {
		log.info("Looking for GSPS for study "+st.getStudyInstanceUID());
		StudyBean gspsSb = (StudyBean) gspsResults.getChildren().get(st.getStudyInstanceUID());
		if( gspsSb==null ) {
			log.debug("Didn't find any GSPS objects for study.");
			return null;
		}
		int count = 0;
		GspsType foundGsps = null;
		for(SeriesType set : gspsSb.getSeries() ) {
			for(DicomObjectType dot : set.getDicomObject() ) {
				if( ! (dot instanceof GspsType) ) { 
					log.warn("Found a non-GSPS child when looking at most recent PR:"+dot.getClass());
					continue;
				}
				GspsType gsps = (GspsType) dot;
				count++;
				if( foundGsps==null ) {
					foundGsps = gsps;
				}
				else {
					if( gsps.getPresentationDateTime().compare(foundGsps.getPresentationDateTime()) > 0 ) {
						foundGsps = gsps;
					}
				}
			}
		}
		log.debug("Found "+count+" gsps objects for study "+st.getStudyInstanceUID()+" name:"+(foundGsps!=null?foundGsps.getContentLabel():" no GSPS"));
		return foundGsps;
	}

	/** This method queries for PR objects associated with any study in the results bean, 
	 * returning a new results bean object.
	 */
	public static ResultsBean queryForGsps(FilterItem filterItem, ResultsBean rb) {
		Map<String,Object> prParams = new HashMap<String,Object>();
		List<String> uids = new ArrayList<String>();
		for( PatientType pt : rb.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				uids.add(st.getStudyInstanceUID());
			}
		}
		if( uids.size()==0 ) return null;
		prParams.put("Modality", "PR");
		prParams.put("StudyInstanceUID", uids.toArray(STRING_ARRAY_TYPE));
		log.debug("Doing a search on "+uids.size()+" Study UID's for PR objects.");
		ResultsBean gspsRB = (ResultsBean) filterItem.callNamedFilter("source", prParams);
		return gspsRB;
	}

}
