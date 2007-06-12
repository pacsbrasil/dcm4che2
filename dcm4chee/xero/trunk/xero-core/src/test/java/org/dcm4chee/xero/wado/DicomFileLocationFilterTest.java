package org.dcm4chee.xero.wado;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterList;
import org.testng.annotations.Test;

/** Tests the dicom file location filter */
public class DicomFileLocationFilterTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("dicom.metadata"); 

	/** Call the dicom.metadata named filter, providing the given filename */
	public static Object callFilter(String mdbName, String filename) {
		assert mdb != null;
		MetaDataBean wado = mdb.get(mdbName);
		assert wado != null;
		FilterList<?> fl = (FilterList<?>) wado.getValue();
		assert (fl != null);
		FilterItem fi = new FilterItem(wado);
		Map<String, Object> params = new HashMap<String, Object>();
		URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		assert url != null;
		File f = new File(url.getFile());
		assert f.canRead();
		params.put(DicomFileLocationFilter.DICOM_FILE_LOCATION, f);
		return fl.filter(fi, params);
	}
	
	@Test
	public void testProvidedFile() {
		File f = (File) callFilter("fileLocation","org/dcm4chee/xero/wado/CR3S1IM1.dcm"); 
		assert f!=null;
	}
}
