package org.dcm4chee.xero.search.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RegroupNonImage filter takes series of type SR, KO and PR and regroups
 * them into a single series for each time, and then adds information on the
 * most recent one of each named/grouped set. This regrouping directly modifies
 * the source data, and as such should be included as a cache key item.
 * 
 * Regrouping is not done across studies, EXCEPT that if a KO references any
 * object in another study, and that particular KO is active, the referenced
 * objects will be included as though they were in the local study. This
 * inclusion needs to have already occurred before this filter is run.
 * 
 * @author bwallace
 */
public class RegroupNonImage implements Filter<ResultsBean> {
   static Logger log = LoggerFactory.getLogger(RegroupNonImage.class);
   
   public RegroupNonImage() {
	  log.info("RegroupNonImage created.");
   }
   /**
    * Regroups KO, SR and PR into a single series with the Modality type being
    * the series UID for the synthetic series. Also arranges the new series with
    * links between newer and older versions of the object. KO and SR are
    * assumed to be atomic - that is, a newer one references an older object and
    * replaces it in toto. PR is NOT atomic - it is entirely possible to replace
    * part of an earlier version. The eventual goal for PR is that there will be
    * 1 set created for every complete grouping - thus, as soon as a single
    * image has a second PR on it, it will create a second grouping.
    */
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
	  ResultsBean rb = (ResultsBean) filterItem.callNextFilter(params);
	  if (rb == null || !params.containsKey("regroup")) {
		 log.info("Not regrouping SR,KO,PR.");
		 return rb;
	  }
	  log.info("Regrouping SR,KO,PR.");

	  List<SeriesBean> addedSeries = new ArrayList<SeriesBean>(2);
	  /** Map the type to the series bean that includes it */
	  for (PatientType pt : rb.getPatient()) {
		 for (StudyType st : pt.getStudy()) {
			StudyBean sb = (StudyBean) st;
			List<SeriesType> sel = sb.getSeries();
			int n = sel.size();
			for (int i = 0; i < n; i++) {
			   SeriesType se = sel.get(i);
			   String modality = se.getModality();
			   if (("PR".equals(modality) || "SR".equals(modality) || "KO".equals(modality))
					 && !se.getSeriesInstanceUID().startsWith(modality)) {
				  SeriesBean ser = addSeries(sb, modality, (SeriesBean) se);
				  if (ser != null)
					 addedSeries.add(ser);
				  sel.remove(i);
				  i--;
				  n--;
			   }
			}
		 }
	  }
	  log.info("Found {} series to regroup.", addedSeries.size());
	  if (addedSeries.size() == 0)
		 return rb;
	  // Process the added series
	  return rb;
   }

   /**
    * Add the series and remove all objects from the provided series, removing
    * it. Return any new series (if more than 1 series exists of the same type,
    * don't add them)
    */
   SeriesBean addSeries(StudyBean sb, String modality, SeriesBean origSer) {
	  String key = modality + ":" + sb.getStudyInstanceUID();
	  SeriesBean seb = (SeriesBean) sb.getChildById(key);
	  SeriesBean ret = null;
	  if (seb == null) {
		 seb = new SeriesBean(sb);
		 seb.setSeriesInstanceUID(key);
		 seb.setModality(modality);
		 seb.getChildren().put(key, seb);
		 sb.getSeries().add(seb);
		 ret = seb;
	  }
	  seb.getDicomObject().addAll(origSer.getDicomObject());
	  seb.getChildren().remove(origSer.getId());
	  return ret;
   }
}
