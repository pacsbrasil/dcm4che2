package org.dcm4chee.xero.wado;

import java.io.IOException;

import javax.imageio.ImageReader;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.testng.annotations.Test;

import static org.dcm4chee.xero.wado.DicomFileLocationFilterTest.callFilter;

/**
 * Tests reading the header data for image, SR, PR and KO type objects. Uses
 * data from src/test/resources from the Mesa project.
 */
public class DicomFilterTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("dicom.metadata");

	@Test
	public void imageHeaderReadTest() throws IOException {
		Object dobj = callFilter("dicom", "org/dcm4chee/xero/wado/CR3S1IM1.dcm");
		assert dobj != null;
		DicomObject dicom = ((DicomStreamMetaData) ((ImageReader) dobj).getStreamMetadata()).getDicomObject();
		assert dicom.contains(Tag.PixelData) == false;
		assert dicom.getString(Tag.PhotometricInterpretation).equals(
				"MONOCHROME1");
		assert dicom.getDouble(Tag.WindowWidth) == 1024;
	}

	@Test
	public void koHeaderReadTest() throws IOException {
		Object dobj = callFilter("dicom", "sr/513/sr_513_mr.dcm");
		assert dobj != null;
		assert dobj instanceof ImageReader;
		ImageReader ir = (ImageReader) dobj;
		DicomObject dicom = ((DicomStreamMetaData) ir.getStreamMetadata()).getDicomObject();
		assert dicom.contains(Tag.PixelData) == false;
		assert dicom.getString(Tag.Modality).equals("KO");
		String refUid = dicom.getString(new int[] {
				Tag.CurrentRequestedProcedureEvidenceSequence, 0,
				Tag.ReferencedSeriesSequence, 0, Tag.ReferencedSOPSequence, 0,
				Tag.ReferencedSOPInstanceUID });
		assert refUid != null;
		assert "1.2.840.113674.950809133207067.100".equals(refUid);
	}
}
