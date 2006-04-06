package org.dcm4che2.hp;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class HangingProtocolTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HangingProtocolTest.class);
    }

    public HangingProtocolTest(String arg0) {
        super(arg0);
    }

    public void testGetHPSelectorSpi() {
        assertNotNull(HangingProtocol.getHPSelectorSpi("IMAGE_PLANE"));
    }

    public void testGetHPComparatorSpi() {
        assertNotNull(HangingProtocol.getHPComparatorSpi("ALONG_AXIS"));
        assertNotNull(HangingProtocol.getHPComparatorSpi("BY_ACQ_TIME"));
    }

    public void testGetSupportedHPSelectorCategories() {
        String[] ss = HangingProtocol.getSupportedHPSelectorCategories();
        List list = Arrays.asList(ss);
        assertEquals(true, list.contains("IMAGE_PLANE"));
    }

    public void testGetSupportedHPComparatorCategories() {
        String[] ss = HangingProtocol.getSupportedHPComparatorCategories();
        List list = Arrays.asList(ss);
        assertEquals(true, list.contains("ALONG_AXIS"));
        assertEquals(true, list.contains("BY_ACQ_TIME"));
    }
}
