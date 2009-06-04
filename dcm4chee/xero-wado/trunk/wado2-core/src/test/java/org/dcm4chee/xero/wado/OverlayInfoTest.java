package org.dcm4chee.xero.wado;

import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OverlayInfoTest {
	
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
	public void testHasOverlayForFrameFromOne() {
		
		OverlayInfo ovlyInfo = new OverlayInfo(ds, 0);
		assertTrue( ovlyInfo.hasOverlayForFrameFromOne(1, 1) );
		assertFalse( ovlyInfo.hasOverlayForFrameFromOne(2, 1) );

		ovlyInfo = new OverlayInfo(ds, 2);
		assertTrue( ovlyInfo.hasOverlayForFrameFromOne(1, 1) );
		assertFalse( ovlyInfo.hasOverlayForFrameFromOne(2, 1) );

		ovlyInfo = new OverlayInfo(ds, 4);
		assertTrue( ovlyInfo.hasOverlayForFrameFromOne(1, 1) );
		assertFalse( ovlyInfo.hasOverlayForFrameFromOne(2, 1) );

		ovlyInfo = new OverlayInfo(ds, 6);
		assertTrue( ovlyInfo.hasOverlayForFrameFromOne(1, 1) );
		assertFalse( ovlyInfo.hasOverlayForFrameFromOne(2, 1) );

		ovlyInfo = new OverlayInfo(ds, 1);
		assertFalse( ovlyInfo.hasOverlayForFrameFromOne(1, 1) );

		ovlyInfo = new OverlayInfo(ds, 8);
		assertFalse( ovlyInfo.hasOverlayForFrameFromOne(1, 1) );
	}

	@Test
	public void testHasValidSeparateOverlayBytes() {
		
		OverlayInfo ovlyInfo = new OverlayInfo(ds, 0);
		assertFalse( ovlyInfo.hasValidSeparateOverlayBytes() );

		ovlyInfo = new OverlayInfo(ds, 2);
		assertFalse( ovlyInfo.hasValidSeparateOverlayBytes() );

		ovlyInfo = new OverlayInfo(ds, 4);
		assertTrue( ovlyInfo.hasValidSeparateOverlayBytes() );

		ovlyInfo = new OverlayInfo(ds, 6);
		assertTrue( ovlyInfo.hasValidSeparateOverlayBytes() );

		ovlyInfo = new OverlayInfo(ds, 17);
		assertFalse( ovlyInfo.hasValidSeparateOverlayBytes() );

	}
	
	
	@Test
	public void testGetNumberOfOverlayFrames() {
		int totalNumberOfImageFrames = 1;
		
		OverlayInfo ovlyInfo = new OverlayInfo(ds, 0);
		assertEquals( ovlyInfo.getNumberOfOverlayFrames(totalNumberOfImageFrames), 1 );
		
		ovlyInfo = new OverlayInfo(ds, 4);
		assertEquals( ovlyInfo.getNumberOfOverlayFrames(totalNumberOfImageFrames), 1 );
		
		ovlyInfo = new OverlayInfo(ds, 17);
		assertEquals( ovlyInfo.getNumberOfOverlayFrames(totalNumberOfImageFrames), 0 );
	}
	
	@Test
	public void testIsEmpty() {
	
		OverlayInfo ovlyInfo = new OverlayInfo(ds, 0);
		assertFalse( ovlyInfo.isEmpty() );
		
		ovlyInfo = new OverlayInfo(ds, 17);
		assertTrue( ovlyInfo.isEmpty() );
	}
}
