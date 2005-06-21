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

public class VRMapTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(VRMapTest.class);
	}

	public VRMapTest(String name) {
		super(name);
	}

	public final void testGetVRMap() {
		VRMap vrMap = VRMap.getVRMap();
		assertNull(vrMap.getPrivateCreator());
		assertEquals(VR.UL, vrMap.vrOf(0x00000000));
		assertEquals(VR.UI, vrMap.vrOf(0x00000002));
		assertEquals(VR.UL, vrMap.vrOf(0x00020000));
		assertEquals(VR.OB, vrMap.vrOf(0x00020001));
		assertEquals(VR.CS, vrMap.vrOf(0x00080005));
		assertEquals(VR.LO, vrMap.vrOf(0x00090010));
		assertEquals(VR.UN, vrMap.vrOf(0x00091010));
		assertEquals(VR.US, vrMap.vrOf(0x60000010));
		assertEquals(VR.US, vrMap.vrOf(0x60020010));
		assertEquals(VR.UL, vrMap.vrOf(0x7FE00000));
		assertEquals(VR.OW, vrMap.vrOf(0x7FE00010));
	}

	public final void testGetPrivateVRMap() {
		VRMap vrMap = VRMap.getPrivateVRMap("dcm4che2");
		assertEquals("dcm4che2", vrMap.getPrivateCreator());
		assertEquals(VR.LO, vrMap.vrOf(0x00990010));
		assertEquals(VR.LO, vrMap.vrOf(0x00990011));
		assertEquals(VR.LO, vrMap.vrOf(0x009900E0));
		assertEquals(VR.UL, vrMap.vrOf(0x00991010));
		assertEquals(VR.UL, vrMap.vrOf(0x00991110));
		assertEquals(VR.OB, vrMap.vrOf(0x009910E0));
		assertEquals(VR.OB, vrMap.vrOf(0x009911E0));
		assertEquals(VR.UN, vrMap.vrOf(0x00991111));
	}
		
}
