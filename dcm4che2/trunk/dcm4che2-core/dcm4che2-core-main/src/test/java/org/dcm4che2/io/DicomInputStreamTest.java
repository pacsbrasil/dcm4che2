/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.io;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;

import junit.framework.TestCase;

public class DicomInputStreamTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(DicomInputStreamTest.class);
	}

	public DicomInputStreamTest(String arg0) {
		super(arg0);
	}

	public final void testReadExplicitVRLE() throws IOException {
		DicomObject attrs = load("DICOMDIR");		
		DicomElement attr = attrs.get(0x00041220);
		assertEquals(1203, attr.countItems());
	}

	public final void testReadRawImplicitVRLE() throws IOException {
		DicomObject attrs = load("OT-PAL-8-face");		
	}
	
	private DicomObject load(String fname) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		DicomInputStream dis = new DicomInputStream(
				new BufferedInputStream(cl.getResourceAsStream(fname)));
		try {
			DicomObject attrs = new BasicDicomObject();
			dis.readDicomObject(attrs, -1);
			return attrs;
		} finally {
			dis.close();
		}
	}

	public final void testSetHandler() {
		//TODO Implement setHandler().
	}

}
