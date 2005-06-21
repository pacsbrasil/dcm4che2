/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.BufferedInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class DicomInputStreamTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(DicomInputStreamTest.class);
	}

	public DicomInputStreamTest(String arg0) {
		super(arg0);
	}

	public final void testReadExplicitVRLE() throws IOException {
		AttributeSet attrs = load("DICOMDIR");		
		Attribute attr = attrs.getAttribute(0x00041220);
		assertEquals(1203, attr.countItems());
	}

	public final void testReadRawImplicitVRLE() throws IOException {
		AttributeSet attrs = load("OT-PAL-8-face");		
	}
	
	private AttributeSet load(String fname) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		DicomInputStream dis = new DicomInputStream(
				new BufferedInputStream(cl.getResourceAsStream(fname)));
		try {
			return dis.readAttributeSet(-1);
		} finally {
			dis.close();
		}
	}

	public final void testSetHandler() {
		//TODO Implement setHandler().
	}

}
