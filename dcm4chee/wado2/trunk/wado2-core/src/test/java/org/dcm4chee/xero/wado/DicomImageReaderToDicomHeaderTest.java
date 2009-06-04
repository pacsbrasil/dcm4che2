package org.dcm4chee.xero.wado;

import org.dcm4che2.data.DicomObject;
import org.testng.annotations.Test;

import static org.dcm4chee.xero.wado.DicomFileLocationFilterTest.callFilter;

public class DicomImageReaderToDicomHeaderTest {

	/** Tests that the dicom image reader correctly converts to a dicom object.
	 * This also tests that filters can be plugged in as @MetaData
	 */
	@Test
	public void imageReaderToHeaderTest() {
		DicomObject dobj = (DicomObject) callFilter("dicomImageHeader", "sr/605/sr_605ct.dcm");
		assert dobj!=null;
	}
}
