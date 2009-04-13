package org.dcm4chee.xero.wado;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.filter.WadoFileLocation;
import org.testng.annotations.Test;

/** Tests the dicom file location filter */
public class DicomFileLocationFilterTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("dicom.metadata"); 

	/** Call the dicom.metadata named filter, providing the given filename */
   public static Object callFilter(String mdbName, String filename) {
		Map<String, Object> params = new HashMap<String, Object>();
		return callFilter(mdbName,filename,params);
	}

   /** Call the dicom.metadata named filter, providing the given filename */
   public static Object callFilter(String mdbName, String filename, Map<String,Object> params) {
		assert mdb != null;
		MetaDataBean wado = mdb.getChild(mdbName);
		assert wado != null;
		Filter<?> fi = (Filter<?>) wado.getValue();
		assert (fi != null);
		params.put(MemoryCacheFilter.KEY_NAME,filename);
		URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		if( url==null ) throw new IllegalArgumentException("Resource not found "+filename);
		//File f = new File(url.getFile());
		//assert f.canRead();
		params.put(WadoFileLocation.DICOM_FILE_LOCATION, url);
		return fi.filter(null, params);
	}
   
	@Test
	public void testProvidedFile() {
		URL f = (URL) callFilter("fileLocation","org/dcm4chee/xero/wado/CR3S1IM1.dcm"); 
		assert f!=null;
	}
}
