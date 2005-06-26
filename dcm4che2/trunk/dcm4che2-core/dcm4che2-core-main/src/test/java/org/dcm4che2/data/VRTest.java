package org.dcm4che2.data;

import junit.framework.TestCase;

public class VRTest extends TestCase {

	private static final byte[] SHORT_4095_LE = { 
			(byte) 0xff, 0x0f };
	private static final byte[] SHORT_MINUS_4095_LE = { 
			0x01, (byte) 0xf0 };
	private static final byte[] SHORT_4095_MINUS_4095_LE = { 
			(byte) 0xff, 0x0f, 
			0x01, (byte) 0xf0 };
	private static final byte[] INT_4095_LE = { 
			(byte) 0xff, 0x0f, 0, 0 };
	private static final byte[] INT_MINUS_4095_LE = { 
			0x01, (byte) 0xf0, (byte) 0xff, (byte) 0xff };
	private static final byte[] INT_4095_MINUS_4095_LE = { 
			(byte) 0xff, 0x0f, 0, 0, 
			0x01, (byte) 0xf0, (byte) 0xff, (byte) 0xff };
	private static final byte[] SHORT_4095_BE = { 
			0x0f, (byte) 0xff };
	private static final byte[] SHORT_MINUS_4095_BE = { 
			(byte) 0xf0, 0x01 };
	private static final byte[] SHORT_4095_MINUS_4095_BE = { 
			0x0f, (byte) 0xff, 
			(byte) 0xf0, 0x01 };
	private static final byte[] INT_4095_BE = { 
			0, 0, 0x0f, (byte) 0xff };
	private static final byte[] INT_MINUS_4095_BE = { 
			(byte) 0xff, (byte) 0xff, (byte) 0xf0, 0x01 };
	private static final byte[] INT_4095_MINUS_4095_BE = { 
			0, 0, 0x0f, (byte) 0xff, 
			(byte) 0xff, (byte) 0xff, (byte) 0xf0, 0x01 };

	public static void main(String[] args) {
		junit.textui.TestRunner.run(VRTest.class);
	}

	public VRTest(String arg0) {
		super(arg0);
	}

	private void assertEquals(byte[] expected, byte[] value) {
		assertEquals("byte[].length", expected.length, value.length);
		for (int i = 0; i < value.length; i++) {
			assertEquals("byte[" + i + "]", expected[i], value[i]);
		}
	}

	public final void testVR_SL() {
		assertEquals(4095, VR.SL.toInt(INT_4095_LE, false));
		assertEquals(-4095, VR.SL.toInt(INT_MINUS_4095_LE, false));
		int[] is = VR.SL.toInts(INT_4095_MINUS_4095_LE, false);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(-4095, is[1]);
		assertEquals(INT_4095_LE, VR.SL.toBytes(4095, false));
		assertEquals(INT_MINUS_4095_LE, VR.SL.toBytes(-4095, false));
		assertEquals(INT_4095_MINUS_4095_LE, VR.SL.toBytes(is, false));

		assertEquals(4095, VR.SL.toInt(INT_4095_BE, true));
		assertEquals(-4095, VR.SL.toInt(INT_MINUS_4095_BE, true));
		is = VR.SL.toInts(INT_4095_MINUS_4095_BE, true);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(-4095, is[1]);
		assertEquals(INT_4095_BE, VR.SL.toBytes(4095, true));
		assertEquals(INT_MINUS_4095_BE, VR.SL.toBytes(-4095, true));
		assertEquals(INT_4095_MINUS_4095_BE, VR.SL.toBytes(is, true));

		assertEquals("4095", VR.SL.toString(INT_4095_LE, false, null));
		assertEquals("-4095", VR.SL.toString(INT_MINUS_4095_LE, false, null));
		String[] ss = VR.SL.toStrings(INT_4095_MINUS_4095_LE, false, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("-4095", ss[1]);
		assertEquals(INT_4095_LE, VR.SL.toBytes("4095", false, null));
		assertEquals(INT_MINUS_4095_LE, VR.SL.toBytes("-4095", false, null));
		assertEquals(INT_4095_MINUS_4095_LE, VR.SL.toBytes(ss, false, null));

		assertEquals("4095", VR.SL.toString(INT_4095_BE, true, null));
		assertEquals("-4095", VR.SL.toString(INT_MINUS_4095_BE, true, null));
		ss = VR.SL.toStrings(INT_4095_MINUS_4095_BE, true, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("-4095", ss[1]);
		assertEquals(INT_4095_BE, VR.SL.toBytes("4095", true, null));
		assertEquals(INT_MINUS_4095_BE, VR.SL.toBytes("-4095", true, null));
		assertEquals(INT_4095_MINUS_4095_BE, VR.SL.toBytes(ss, true, null));
	}

	public final void testVR_SS() {
		assertEquals(4095, VR.SS.toInt(SHORT_4095_LE, false));
		assertEquals(-4095, VR.SS.toInt(SHORT_MINUS_4095_LE, false));
		int[] is = VR.SS.toInts(SHORT_4095_MINUS_4095_LE, false);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(-4095, is[1]);
		assertEquals(SHORT_4095_LE, VR.SS.toBytes(4095, false));
		assertEquals(SHORT_MINUS_4095_LE, VR.SS.toBytes(-4095, false));
		assertEquals(SHORT_4095_MINUS_4095_LE, VR.SS.toBytes(is, false));

		assertEquals(4095, VR.SS.toInt(SHORT_4095_BE, true));
		assertEquals(-4095, VR.SS.toInt(SHORT_MINUS_4095_BE, true));
		is = VR.SS.toInts(SHORT_4095_MINUS_4095_BE, true);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(-4095, is[1]);
		assertEquals(SHORT_4095_BE, VR.SS.toBytes(4095, true));
		assertEquals(SHORT_MINUS_4095_BE, VR.SS.toBytes(-4095, true));
		assertEquals(SHORT_4095_MINUS_4095_BE, VR.SS.toBytes(is, true));

		assertEquals("4095", VR.SS.toString(SHORT_4095_LE, false, null));
		assertEquals("-4095", VR.SS.toString(SHORT_MINUS_4095_LE, false, null));
		String[] ss = VR.SS.toStrings(SHORT_4095_MINUS_4095_LE, false, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("-4095", ss[1]);
		assertEquals(SHORT_4095_LE, VR.SS.toBytes("4095", false, null));
		assertEquals(SHORT_MINUS_4095_LE, VR.SS.toBytes("-4095", false, null));
		assertEquals(SHORT_4095_MINUS_4095_LE, VR.SS.toBytes(ss, false, null));

		assertEquals("4095", VR.SS.toString(SHORT_4095_BE, true, null));
		assertEquals("-4095", VR.SS.toString(SHORT_MINUS_4095_BE, true, null));
		ss = VR.SS.toStrings(SHORT_4095_MINUS_4095_BE, true, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("-4095", ss[1]);
		assertEquals(SHORT_4095_BE, VR.SS.toBytes("4095", true, null));
		assertEquals(SHORT_MINUS_4095_BE, VR.SS.toBytes("-4095", true, null));
		assertEquals(SHORT_4095_MINUS_4095_BE, VR.SS.toBytes(ss, true, null));
	}

	public final void testVR_UL() {
		assertEquals(4095, VR.UL.toInt(INT_4095_LE, false));
		assertEquals(-4095, VR.UL.toInt(INT_MINUS_4095_LE, false));
		int[] is = VR.UL.toInts(INT_4095_MINUS_4095_LE, false);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(-4095, is[1]);
		assertEquals(INT_4095_LE, VR.UL.toBytes(4095, false));
		assertEquals(INT_MINUS_4095_LE, VR.UL.toBytes(-4095, false));
		assertEquals(INT_4095_MINUS_4095_LE, VR.UL.toBytes(is, false));

		assertEquals(4095, VR.UL.toInt(INT_4095_BE, true));
		assertEquals(-4095, VR.UL.toInt(INT_MINUS_4095_BE, true));
		is = VR.UL.toInts(INT_4095_MINUS_4095_BE, true);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(-4095, is[1]);
		assertEquals(INT_4095_BE, VR.UL.toBytes(4095, true));
		assertEquals(INT_MINUS_4095_BE, VR.UL.toBytes(-4095, true));
		assertEquals(INT_4095_MINUS_4095_BE, VR.UL.toBytes(is, true));

		assertEquals("4095", VR.UL.toString(INT_4095_LE, false, null));
		assertEquals("4294963201", VR.UL.toString(INT_MINUS_4095_LE, false, null));
		String[] ss = VR.UL.toStrings(INT_4095_MINUS_4095_LE, false, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("4294963201", ss[1]);
		assertEquals(INT_4095_LE, VR.UL.toBytes("4095", false, null));
		assertEquals(INT_MINUS_4095_LE, VR.UL.toBytes("4294963201", false, null));
		assertEquals(INT_4095_MINUS_4095_LE, VR.UL.toBytes(ss, false, null));

		assertEquals("4095", VR.UL.toString(INT_4095_BE, true, null));
		assertEquals("4294963201", VR.UL.toString(INT_MINUS_4095_BE, true, null));
		ss = VR.UL.toStrings(INT_4095_MINUS_4095_BE, true, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("4294963201", ss[1]);
		assertEquals(INT_4095_BE, VR.UL.toBytes("4095", true, null));
		assertEquals(INT_MINUS_4095_BE, VR.UL.toBytes("4294963201", true, null));
		assertEquals(INT_4095_MINUS_4095_BE, VR.UL.toBytes(ss, true, null));
	}

	public final void testVR_US() {
		assertEquals(4095, VR.US.toInt(SHORT_4095_LE, false));
		assertEquals(61441, VR.US.toInt(SHORT_MINUS_4095_LE, false));
		int[] is = VR.US.toInts(SHORT_4095_MINUS_4095_LE, false);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(61441, is[1]);
		assertEquals(SHORT_4095_LE, VR.US.toBytes(4095, false));
		assertEquals(SHORT_MINUS_4095_LE, VR.US.toBytes(-4095, false));
		assertEquals(SHORT_4095_MINUS_4095_LE, VR.US.toBytes(is, false));

		assertEquals(4095, VR.US.toInt(SHORT_4095_BE, true));
		assertEquals(61441, VR.US.toInt(SHORT_MINUS_4095_BE, true));
		is = VR.US.toInts(SHORT_4095_MINUS_4095_BE, true);
		assertEquals(2, is.length);
		assertEquals(4095, is[0]);
		assertEquals(61441, is[1]);
		assertEquals(SHORT_4095_BE, VR.US.toBytes(4095, true));
		assertEquals(SHORT_MINUS_4095_BE, VR.US.toBytes(-4095, true));
		assertEquals(SHORT_4095_MINUS_4095_BE, VR.US.toBytes(is, true));

		assertEquals("4095", VR.US.toString(SHORT_4095_LE, false, null));
		assertEquals("61441", VR.US.toString(SHORT_MINUS_4095_LE, false, null));
		String[] ss = VR.US.toStrings(SHORT_4095_MINUS_4095_LE, false, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("61441", ss[1]);
		assertEquals(SHORT_4095_LE, VR.US.toBytes("4095", false, null));
		assertEquals(SHORT_MINUS_4095_LE, VR.US.toBytes("61441", false, null));
		assertEquals(SHORT_4095_MINUS_4095_LE, VR.SS.toBytes(ss, false, null));

		assertEquals("4095", VR.US.toString(SHORT_4095_BE, true, null));
		assertEquals("61441", VR.US.toString(SHORT_MINUS_4095_BE, true, null));
		ss = VR.US.toStrings(SHORT_4095_MINUS_4095_BE, true, null);
		assertEquals(2, ss.length);
		assertEquals("4095", ss[0]);
		assertEquals("61441", ss[1]);
		assertEquals(SHORT_4095_BE, VR.US.toBytes("4095", true, null));
		assertEquals(SHORT_MINUS_4095_BE, VR.US.toBytes("61441", true, null));
		assertEquals(SHORT_4095_MINUS_4095_BE, VR.SS.toBytes(ss, true, null));
	}
}
