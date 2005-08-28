/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.io;

import java.io.IOException;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.junit.BaseTestCase;

public class DicomInputStreamTest extends BaseTestCase {

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
	
}
