package org.dcm4chee.xero.search.dicom;

import java.util.Map;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageInSeriesSearch  extends DicomCFindFilter {
   private static Logger log = LoggerFactory.getLogger(ImageInSeriesSearch.class);
   
   private boolean foundInSeries = true;
   
   static final String SERIES_SEARCH_LEVEL = "SERIES";
	public static final int InstanceSeq =  0x00430040;
	
	private static final String[] ImageOnlyFilters = {
	  "SOPInstanceUID", "objectUID", "InstanceNumber", "instanceNumber", 
	};

   /** Try using the series level return all filter to get image level data. */
   @Override
   public ResultFromDicom filter(FilterItem<ResultFromDicom> filterItem, Map<String, Object> params) {
      log.info("Using series level instance search.");
      for(String key : ImageOnlyFilters ) {
    	 if( params.containsKey(key) ) {
    		return (ResultFromDicom) filterItem.callNextFilter(params);
    	 }
      }
      long start = System.nanoTime();
	  ResultFromDicom ret = (ResultFromDicom) super.filter(filterItem, params);
	  if( foundInSeries ) {
		 log.info("Found results from series level returns in " +(System.nanoTime()-start)/1e6 + " ms");
		 return ret;
	  }
 	  return (ResultFromDicom) filterItem.callNextFilter(params);
   }

   /** Skip all series that don't have instance data. */
   @Override
   protected void addResult(ResultFromDicom resultFromDicom, DicomObject data) {
	  DicomElement seq = data.get(InstanceSeq);
	  if( seq==null || !seq.hasDicomObjects() ) {
		 log.info("Skipping series - it doesn't contain any instance data.");
		 foundInSeries = false;
	  }
	  log.info("Found a series containing some data.");
	  super.addResult(resultFromDicom, data);
   }

   private static final String[] SERIES_LEVEL_FIND_CUID = {
    	UID.PrivateStudyRootQueryRetrieveInformationModelFIND,
        };
 
    private static final int[] SERIES_RETURN_KEYS = {
    	Tag.PatientID,
    	Tag.StudyInstanceUID,
        Tag.Modality,
        Tag.SeriesNumber,
        Tag.SeriesInstanceUID,
        Tag.NumberOfSeriesRelatedInstances,
        Tag.NumberOfStudyRelatedSeries,
        Tag.PatientName,
        InstanceSeq,};

	@Override
	protected String[] getCuids() {
		return SERIES_LEVEL_FIND_CUID;
	}

	@Override
	protected String getQueryLevel() {
		return SERIES_SEARCH_LEVEL;
	}

	@Override
	protected int[] getReturnKeys() {
		return SERIES_RETURN_KEYS;
	}
}
