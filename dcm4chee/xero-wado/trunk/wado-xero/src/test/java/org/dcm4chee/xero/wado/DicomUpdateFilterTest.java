package org.dcm4chee.xero.wado;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.stream.ImageInputStream;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterItemSingleton;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.search.StudyInfo;
import org.dcm4chee.xero.search.StudyInfoCache;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the DicomUpdateFilter knows when an update is required, knows how to
 * get an update and knows how to apply the update.
 * 
 * @author bwallace
 * 
 */
public class DicomUpdateFilterTest {

	DicomUpdateFilter duf;
	FilterItem<DicomImageReader> fi;
	DicomObject cfindSame, cfindDiff, dicom;
	StudyInfoCache sic;
	static final DicomImageReaderSpi dicomImageReaderSpi = new DicomImageReaderSpi();
	static final String dicomFile = "sr/601/sr_601cr.dcm";
	URL url;
	DicomImageReader dir;
	ImageInputStream iis;
	StudyInfo si;

	/** This initializes the variables for a test run with some common values. */
	@BeforeMethod
	public void init() throws IOException {
		duf = new DicomUpdateFilter();
		sic = new StudyInfoCache();
		duf.setStudyInfoCache(sic);
		dir = (DicomImageReader) dicomImageReaderSpi.createReaderInstance();
		url = Thread.currentThread().getContextClassLoader().getResource(
				dicomFile);
		iis = new ReopenableImageInputStream(url);
		dir.setInput(iis);
		dicom = ((DicomStreamMetaData) dir.getStreamMetadata())
				.getDicomObject();

		cfindSame = new BasicDicomObject();
		// Note that CS type strings need to be an even length, padded with
		// spaces.
		cfindSame.putString(Tag.PatientSex, VR.CS, "M ");
		cfindSame.putString(Tag.StudyID, VR.SH, "");
		cfindSame.putString(Tag.StudyInstanceUID, VR.UI, dicom
				.getString(Tag.StudyInstanceUID));
		cfindSame.putString(Tag.QueryRetrieveLevel, VR.CS, "SERIES");
		cfindSame.putString(Tag.SeriesInstanceUID, VR.UI, dicom
				.getString(Tag.SeriesInstanceUID));
		cfindSame.putString(Tag.PatientName, VR.PN, dicom.getString(Tag.PatientName));
		cfindDiff = new BasicDicomObject(cfindSame);
		cfindDiff.putString(Tag.PatientName, VR.PN, "CR^Trois^");
		si = sic.get(dicom.getString(Tag.StudyInstanceUID));
	}

	/** Tests whether an update is required */
	@Test
	public void updateRequiredTest() throws Exception {
		assert !DicomUpdateFilter.needsUpdate(dicom, cfindSame);
		assert DicomUpdateFilter.needsUpdate(dicom, cfindDiff);
	}

	/** Tests that reading the series headers works as expected */
	@Test
	public void readSeriesHeaderTest() throws Exception {
		TestFilterSeriesCFind seriesCFind = new TestFilterSeriesCFind(cfindSame);
		assert !si.containsKey("seriesQuery");
		DicomObject readHeader = DicomUpdateFilter.readSeriesHeader(si, dicom
				.getString(Tag.SeriesInstanceUID), seriesCFind);
		assert readHeader == cfindSame;
	}

	/** Tests that updating the dicom header works as expected */
	@Test
	public void updateHeaderTest() throws Exception {
		TestFilterSeriesCFind seriesCFind = new TestFilterSeriesCFind(cfindDiff);
		DicomObject readHeader = DicomUpdateFilter.readSeriesHeader(si, dicom
				.getString(Tag.SeriesInstanceUID), seriesCFind);
		assert readHeader==cfindDiff;
		
		FilterItem<DicomImageReader> filterItemSingleton = new FilterItemSingleton<DicomImageReader>(dir);
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("studyUID", dicom.getString(Tag.StudyInstanceUID));
		params.put("seriesUID", dicom.getString(Tag.SeriesInstanceUID));
		dir = duf.filter(filterItemSingleton, params);
		DicomObject d2 = ((DicomStreamMetaData) dir.getStreamMetadata()).getDicomObject();
		assert DicomUpdateFilter.needsUpdate(cfindDiff,d2);
		
		params.put(DicomUpdateFilter.UPDATE_HEADER,"TRUE");
		dir = duf.filter(filterItemSingleton,params);
		d2 = ((DicomStreamMetaData) dir.getStreamMetadata()).getDicomObject();
		assert !DicomUpdateFilter.needsUpdate(d2, cfindDiff);
		assert DicomUpdateFilter.needsUpdate(d2, cfindSame);
		assert d2.contains(Tag.VerificationFlag);
	}

	/** Implement a simple test filter that returns the given, fixed items. */
	static class TestFilterSeriesCFind implements Filter<ResultFromDicom> {
		DicomObject dicom;

		public TestFilterSeriesCFind(DicomObject dicom) {
			this.dicom = dicom;
		}

		/** Handles calling a named filter - reports the given header */
		public ResultFromDicom filter(FilterItem<ResultFromDicom> filterItem,
				Map<String, Object> params) {
			ResultFromDicom rfd = (ResultFromDicom) params
					.get(DicomCFindFilter.EXTEND_RESULTS_KEY);
			assert params.get("studyUID").equals(
					dicom.getString(Tag.StudyInstanceUID));
			rfd.addResult(dicom);
			return rfd;
		}

	}
}
