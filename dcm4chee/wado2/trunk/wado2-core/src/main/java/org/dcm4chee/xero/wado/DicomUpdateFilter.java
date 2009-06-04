package org.dcm4chee.xero.wado;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.search.StudyInfo;
import org.dcm4chee.xero.search.StudyInfoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter causes the DICOM object to be updated. This will update the
 * header in the cache itself, but will not affect the original version of the
 * cached object (assuming that the cache is part of the filter chain).
 * 
 * The updated header information will be stored in the DicomStreamMetaData
 * directly, so there isn't any need to otherwise replace this object.
 * 
 * @author bwallace
 * 
 */
public class DicomUpdateFilter implements Filter<DicomImageReader> {

	private static Logger log = LoggerFactory.getLogger(DicomUpdateFilter.class);

	/** Skip these tags on the comparison - MUST be sorted */
	static int[] skipUpdate = new int[] { Tag.QueryRetrieveLevel, Tag.RetrieveAETitle, Tag.InstanceAvailability,
	      Tag.ModalitiesInStudy, Tag.SOPClassesInStudy, Integer.MAX_VALUE };

	/**
	 * Set this value in the params to cause the header to be updated by this
	 * filter. In general this is not set by end-users, but is rather set by
	 * calls in DicomFilter.
	 */
	public static final String UPDATE_HEADER = "updateHeader";

	/** The study cache contains study infos for various objects */
	StudyInfoCache studyCache = StudyInfoCache.getSingleton();
	
	/**
	 * This method updates the dicom image reader if required to update the
	 * header data. To do this, it uses both the StudyInfo/StudyInfoCache to get
	 * the header data, and a customized series-level C-Find filter. This method
	 * is thread-safe at the study level - that is, only 1 request can go through
	 * to get the updated series/study level information at a time, and that will
	 * provide information for all series at once, although individual objects
	 * only get updated as required.
	 */
	public DicomImageReader filter(FilterItem<DicomImageReader> filterItem, Map<String, Object> params) {
		DicomImageReader ret = filterItem.callNextFilter(params);
		if (ret == null) {
			log.debug("Not applying dicom update - no object found.");
			return null;
		}
		if (!params.containsKey(UPDATE_HEADER)) {
			log.debug("Not applying dicom update - no key value.");
			return ret;
		}
		try {
			DicomStreamMetaData dsmd;
			DicomObject header;
			String studyUid, seriesUid;
			synchronized (ret) {
				dsmd = (DicomStreamMetaData) ret.getStreamMetadata();
				header = dsmd.getDicomObject();
				studyUid = header.getString(Tag.StudyInstanceUID);
				assert studyUid != null;
				seriesUid = header.getString(Tag.SeriesInstanceUID);
				assert seriesUid != null;
			}
			StudyInfo si = studyCache.get(studyUid);
			DicomObject series = readSeriesHeader(si, seriesUid, seriesCFind);
			if (series == null) {
				log.warn("Unable to find series info - may not update all fields.");
				return ret;
			}
			synchronized (ret) {
				// Re-load the header in case someone else updated it
				header = dsmd.getDicomObject();
				if (!needsUpdate(header, series)) {
					log.debug("Not applying dicom header update - no changes required.");
					return ret;
				}
				log.debug("Applying DICOM update header as there are changes.");
				DicomObject newHeader = new BasicDicomObject();
				header.copyTo(newHeader);
				series.copyTo(newHeader);
				dsmd.setDicomObject(newHeader);
			}
		} catch (IOException e) {
			log.warn("Can't read dicom object to get header.");
			throw new RuntimeException("Can't read DICOM object to update header:" + e, e);
		}
		// Return the updated header - this i done indirectly already.
		return ret;
	}

	/**
	 * Figure out if the header provided needs an update compared to the provided
	 * series information.
	 * 
	 * @param header
	 * @param series
	 * @return
	 */
	public static boolean needsUpdate(DicomObject header, DicomObject series) {
		Iterator<DicomElement> it = series.datasetIterator();
		int skipi = 0, skip = skipUpdate[0];
		while (it.hasNext()) {
			DicomElement de = it.next();
			if (de.tag() >= skip) {
				while (de.tag() > skip)
					skip = skipUpdate[++skipi];
				if (de.tag() == skip)
					continue;
			}
			DicomElement hde = header.get(de.tag());
			if (hde == null) {
				log.debug("Need to add dicom element {} as it isn't present.", de);
				return true;
			}
			if (!hde.equals(de)) {
				log.debug("Tags {} and {} differ.", de, hde);
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads the series level information into the provided series info object,
	 * and return the header for the given series UID.
	 * 
	 * @param studyUid
	 * @param seriesUid
	 * @param si
	 * @param filterItem
	 */
	public static DicomObject readSeriesHeader(StudyInfo si, String seriesUid, Filter<ResultFromDicom> seriesCFind) {
		DicomObject series = si.getSeriesHeader(seriesUid);
		if (series != null)
			return series;
		synchronized (si) {
			// Check for a race condition and avoid it
			series = si.getSeriesHeader(seriesUid);
			if (series != null)
				return series;
			Map<String, Object> params = new HashMap<String, Object>();
			String ae = (String) si.get("ae");
			if (ae != null)
				params.put("ae", ae);
			// If the query has already been performed, then redo the query at
			// the series level.
			if (si.containsKey("seriesQuery")) {
				log.info("Putting seriesUID as seriesQuery is present.");
				params.put("seriesUID", seriesUid);
			} else {
				log.info("Putting studyUID as seriesQuery has not yet been performed.");
				params.put("studyUID", si.getStudyUID());
				si.put("seriesQuery", Boolean.TRUE);
			}
			FilterUtil.computeQueryString(params);
			params.put(DicomCFindFilter.EXTEND_RESULTS_KEY, new StudyInfoHeaderRecord(si));
			seriesCFind.filter(null,params);
			series = si.getSeriesHeader(seriesUid);
			if (series == null && params.containsKey("studyUID")) {
				log.info("Didn't find series UID in requested study - trying series level query.");
				series = readSeriesHeader(si, seriesUid, seriesCFind);
			}
		}
		return series;
	}

	/**
	 * Records the DICOM results for the given series level query - this contain
	 * all the information required to update the DICOM header objects below this
	 * level.
	 * 
	 * @author bwallace
	 * 
	 */
	static class StudyInfoHeaderRecord implements ResultFromDicom {
		StudyInfo si;

		/** Records the study info so that the result can be recorded. */
		public StudyInfoHeaderRecord(StudyInfo si) {
			this.si = si;
		}

		/** Records each result in the correct study info */
		public void addResult(DicomObject data) {
			String series = data.getString(Tag.SeriesInstanceUID);
			if (series == null) {
				log.warn("Unable to find series UID for query.");
				return;
			}
			si.putSeriesHeader(series, data);
		}

	}

	/** Sets the cache to use for this object's study info. */
	public void setStudyInfoCache(StudyInfoCache sic) {
		this.studyCache = sic;
	}

	/** The filter to get study level information */
	private Filter<ResultFromDicom> seriesCFind;

	public Filter<ResultFromDicom> getSeriesCFind() {
   	return seriesCFind;
   }

	/** Sets the C-Find series level search object to use */
   @MetaData(out="${class:org.dcm4chee.xero.search.study.SeriesSearch}")
   public void setSeriesCFind(Filter<ResultFromDicom> seriesCFind) {
   	this.seriesCFind = seriesCFind;
   }
}
