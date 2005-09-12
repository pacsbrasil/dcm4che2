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

public class ElementDictionaryTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ElementDictionaryTest.class);
	}

	public ElementDictionaryTest(String name) {
		super(name);
	}

	public final void testGetDictionary() {
		ElementDictionary dict = ElementDictionary.getDictionary();
		assertNull(dict.getPrivateCreator());
		assertEquals(ElementDictionary.GROUP_LENGTH, dict.nameOf(0x00000000));
		assertEquals("Affected SOP Class UID", dict.nameOf(0x00000002));
		assertEquals(ElementDictionary.GROUP_LENGTH, dict.nameOf(0x00020000));
		assertEquals("File Meta Information Version", dict.nameOf(0x00020001));
		assertEquals("Specific Character Set", dict.nameOf(0x00080005));
		assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x00090010));
		assertEquals(ElementDictionary.UNKOWN, dict.nameOf(0x00091010));
		assertEquals("Overlay Rows", dict.nameOf(0x60000010));
		assertEquals("Overlay Rows", dict.nameOf(0x60020010));
		assertEquals(ElementDictionary.GROUP_LENGTH, dict.nameOf(0x7FE00000));
		assertEquals("Pixel Data", dict.nameOf(0x7FE00010));
	}

	public final void testGetPrivateDictionary() {
		ElementDictionary dict = ElementDictionary.getPrivateDictionary("dcm4che2");
		assertEquals("dcm4che2", dict.getPrivateCreator());
		assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x00990010));
		assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x00990011));
		assertEquals(ElementDictionary.PRIVATE_CREATOR, dict.nameOf(0x009900E0));
		assertEquals("Private UL", dict.nameOf(0x00991010));
		assertEquals("Private UL", dict.nameOf(0x00991110));
		assertEquals("Private OB", dict.nameOf(0x009910E0));
		assertEquals("Private OB", dict.nameOf(0x009911E0));
		assertEquals(ElementDictionary.UNKOWN, dict.nameOf(0x00991111));
	}

}
