/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import junit.framework.TestCase;

public class UIDDictionaryTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(UIDDictionaryTest.class);
	}

	public UIDDictionaryTest(String name) {
		super(name);
	}

	public final void testGetDictionary() {
		UIDDictionary dict = UIDDictionary.getDictionary();
        assertEquals("Implicit VR Little Endian", dict.nameOf(UID.ImplicitVRLittleEndian));
		assertEquals("Explicit VR Little Endian", dict.nameOf(UID.ExplicitVRLittleEndian));
	}

}
