package org.dcm4chee.xero.wado;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class OverlayUtilsTest {
	
	private DicomObject ds;

	@BeforeMethod
	public void setup() throws URISyntaxException, IOException {
		String filename = "org/dcm4chee/xero/wado/ovly_p01.dcm";
		URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		if( url==null ) throw new IllegalArgumentException("Resource not found "+filename);
		File f = new File(url.toURI());
		DicomInputStream reader = new DicomInputStream(f);
		ds = reader.readDicomObject();
	}
	
	
	@Test
	public void testFindAllOverlays() throws IOException, URISyntaxException {
		
		List<OverlayInfo> findAllOverlays = OverlayUtils.findAllOverlays("", ds);
		
		assertEquals( findAllOverlays.size(), 4 );
	}
	
	
	@Test
	public void testFindEmbeddedOverlays() throws IOException, URISyntaxException {
		
		List<OverlayInfo> findEmbeddedOverlays = OverlayUtils.findEmbeddedOverlays("", ds);
		
		assertEquals( findEmbeddedOverlays.size(), 2 );
	}

}
