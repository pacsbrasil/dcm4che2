/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.util.Date;
import java.util.List;

import org.dcm4che2.util.ByteUtils;
import org.dcm4che2.util.DateUtils;
import org.dcm4che2.util.StringUtils;

public abstract class VR {
	
	public interface Fragment {

	}

	private static final String[] EMPTY_STRING_ARRAY = {};
	private static final int[] EMPTY_INT_ARRAY = {};
	private static final float[] EMPTY_FLOAT_ARRAY = {};
	private static final double[] EMPTY_DOUBLE_ARRAY = {};
	private static final Date[] EMPTY_DATE_ARRAY = {};
	
	private static byte[] str2bytes(String val, SpecificCharacterSet cs) {
		return val == null ? null
				: cs == null ? val.getBytes() : cs.encode(val);
	}
	
	private static String bytes2str(byte[] val, SpecificCharacterSet cs) {
		return val == null ? null
				: cs == null ? new String(val) : cs.decode(val);
	}

	private static byte[] strs2bytes(String[] val, SpecificCharacterSet cs) {
		return VR.str2bytes(StringUtils.join(val, '\\'), cs);
	}
		
	private static String[] bytes2strs(byte[] val, SpecificCharacterSet cs) {
		return StringUtils.split(VR.bytes2str(val, cs), '\\');
	}

	private static String bytes2str1(byte[] val, SpecificCharacterSet cs) {
		return StringUtils.first(VR.bytes2str(val, cs), '\\');
	}

	private static class ASCIIVR extends VR {

		private ASCIIVR(int code, int padding, int valueLengthBytes) {
			super(code, padding, valueLengthBytes);
		}

		public byte[] toBytes(String val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return VR.str2bytes(val, null);
		}
		
		public byte[] toBytes(String[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return VR.strs2bytes(val, null);
		}

		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return StringUtils.trim(VR.bytes2str1(val, null));
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			return StringUtils.trim(VR.bytes2strs(val, null));
		}
	}

	private static class StringVR extends VR {

		protected StringVR(int code, int padding, int valueLengthBytes) {
			super(code, padding, valueLengthBytes);
		}
		
		public byte[] toBytes(String val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return VR.str2bytes(val, cs);
		}
		
		public byte[] toBytes(String[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return VR.strs2bytes(val, cs);
		}

		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return StringUtils.trim(VR.bytes2str1(val, cs));
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			return StringUtils.trim(VR.bytes2strs(val, cs));
		}

	}

	private static class TextVR extends VR {

		protected TextVR(int code, int padding, int valueLengthBytes) {
			super(code, padding, valueLengthBytes);
		}
		
		public byte[] toBytes(String val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return VR.str2bytes(val, cs);
		}
		
		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return StringUtils.trimEnd(VR.bytes2str(val, cs));
		}
	}
	
	private static class ShortVR extends VR {

		private ShortVR(int code, int padding, int valueLengthBytes) {
			super(code, padding, valueLengthBytes);
		}

		public byte[] toBytes(int val, boolean bigEndian) {
			byte[] b = new byte[2];
			return bigEndian ? ByteUtils.ushort2bytesBE(val, b, 0) 
					: ByteUtils.ushort2bytesLE(val, b, 0);
		}
		
		public byte[] toBytes(int[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.ushorts2bytesBE(val) 
					: ByteUtils.ushorts2bytesLE(val);
		}

		public byte[] toBytes(String val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return toBytes(Integer.parseInt(val), bigEndian);
		}
		
		public byte[] toBytes(String[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			int[] t = new int[val.length];
			for (int i = 0; i < val.length; i++) {
				t[i] = Integer.parseInt(val[i]);
			}
			return toBytes(t, bigEndian);
		}

		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return null;
			return Integer.toString(toInt(val, bigEndian));
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			int[] t1 = toInts(val, bigEndian);
			String[] t2 = new String[t1.length];
			for (int i = 0; i < t2.length; i++) {
				t2[i] = Integer.toString(t1[i]);
			}
			return t2;
		}
			
		public void toggleEndian(Object val) {
			ByteUtils.toggleShortEndian((byte[]) val);
		}
	}

	private static class IntVR extends VR {

		private IntVR(int code, int padding, int valueLengthBytes) {
			super(code, padding, valueLengthBytes);
		}

		public byte[] toBytes(int val, boolean bigEndian) {
			byte[] b = new byte[4];
			return bigEndian ? ByteUtils.int2bytesBE(val, b, 0) 
					: ByteUtils.int2bytesLE(val, b, 0);
		}
		
		public byte[] toBytes(int[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.ints2bytesBE(val) 
					: ByteUtils.ints2bytesLE(val);
		}

		public byte[] toBytes(String val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return toBytes((int) Long.parseLong(val), bigEndian);
		}
		
		public byte[] toBytes(String[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			int[] t = new int[val.length];
			for (int i = 0; i < val.length; i++) {
				t[i] = (int) Long.parseLong(val[i]);
			}
			return toBytes(t, bigEndian);
		}

		public int toInt(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0;
			return bigEndian ? ByteUtils.bytesBE2int(val, 0) 
					: ByteUtils.bytesLE2int(val, 0);
		}

		public int[] toInts(byte[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.bytesBE2ints(val) 
					: ByteUtils.bytesLE2ints(val);
		}
				
		public void toggleEndian(Object val) {
			ByteUtils.toggleIntEndian((byte[]) val);
		}
	}
	
	private static final class AE extends ASCIIVR {

		private AE() {
			super(0x4145, ' ', 8);
		}
	}

	private static final class AS extends ASCIIVR {

		private AS() {
			super(0x4153, ' ', 8);
		}
	}

	private static final class AT extends VR {
		
		private AT() {
			super(0x4154, 0, 8);
		}

		public byte[] toBytes(int val, boolean bigEndian) {
			byte[] b = new byte[4];
			return bigEndian ? ByteUtils.tag2bytesBE(val, b, 0)
					: ByteUtils.tag2bytesLE(val, b, 0);
		}
		
		public byte[] toBytes(int[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.tags2bytesBE(val) 
					: ByteUtils.tags2bytesLE(val);
		}

		public int toInt(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0;
			return bigEndian ? ByteUtils.bytesBE2tag(val, 0) 
					: ByteUtils.bytesLE2tag(val, 0);
		}

		public int[] toInts(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return EMPTY_INT_ARRAY;
			return bigEndian ? ByteUtils.bytesBE2tags(val) 
					: ByteUtils.bytesLE2tags(val);
		}
		
		public void toggleEndian(Object val) {
			ByteUtils.toggleShortEndian((byte[]) val);
		}
	}
	
	private static final class CS extends ASCIIVR {

		private CS() {
			super(0x4353, ' ', 8);
		}
	}

	private static final class DA extends ASCIIVR {

		private DA() {
			super(0x4441, ' ', 8);
		}

		public byte[] toBytes(Date d) {
			return VR.str2bytes(DateUtils.formatDA(d), null);
		}

		public byte[] toBytes(Date[] d) {
			if (d == null || d.length == 0)
				return null;
			String[] ss = new String[d.length];
			for (int i = 0; i < ss.length; i++) {
				ss[i] = DateUtils.formatDA(d[i]);
			}
			return VR.strs2bytes(ss, null);
		}

		public byte[] toBytes(DateRange dr) {
			if (dr == null)
				return null;
			StringBuffer sb = new StringBuffer(9);
			if (dr.getStart() != null)
				sb.append(DateUtils.formatDA(dr.getStart()));
			sb.append("-");
			if (dr.getEnd() != null)
				sb.append(DateUtils.formatDA(dr.getEnd()));
			return VR.str2bytes(sb.toString(), null);
		}
		
		public Date toDate(byte[] val) {
			return DateUtils.parseDA(
					StringUtils.trim(VR.bytes2str1(val, null)), false);
		}

		public Date[] toDates(byte[] val) {
			if (val == null || val.length == 0)
				return EMPTY_DATE_ARRAY;
			String[] ss = StringUtils.trim(VR.bytes2strs(val, null));
			Date[] ds = new Date[ss.length];
			for (int i = 0; i < ds.length; i++) {
				ds[i] = DateUtils.parseDA(ss[i], false);
			}
			return ds;
		}
		
		public DateRange toDateRange(byte[] val) {
			String s = StringUtils.trim(VR.bytes2str1(val, null));
			int l;
			if (s == null || (l = s.length()) == 0 || s.equals("-"))
				return null;
			int hypen = s.indexOf('-');
			Date start = hypen == 0 ? null : DateUtils.parseDA(
					hypen == -1 ? s : s.substring(0, hypen), false);
			Date end = hypen + 1 == l ? null 
					: DateUtils.parseDA(s.substring(hypen+1), true);
			return new DateRange(start, end);
		}
	}

	private static final class DS extends ASCIIVR {

		private DS() {
			super(0x4453, ' ', 8);
		}
		
		public byte[] toBytes(float val, boolean bigEndian) {
			return toBytes(Float.toString(val), bigEndian, null);
		}
		
		public byte[] toBytes(float[] val, boolean bigEndian) {
			if (val == null)
				return null;
			String[] ss = new String[val.length];
			for (int i = 0; i < ss.length; i++)
				ss[i] = Float.toString(val[i]);
			return toBytes(ss, bigEndian, null);
		}
		
		public float toFloat(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0f;
			return Float.parseFloat(toString(val, bigEndian, null));
		}

		public float[] toFloats(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return EMPTY_FLOAT_ARRAY;
			String[] ss = toStrings(val, bigEndian, null);
			float[] fs = new float[ss.length];
			for (int i = 0; i < fs.length; i++)
				if (ss[i].length() > 0)
					fs[i] = Float.parseFloat(ss[i]);
			return fs;
		}		
	}

	private static final class DT extends ASCIIVR {

		private DT() {
			super(0x4454, ' ', 8);
		}

		public byte[] toBytes(Date d) {
			return VR.str2bytes(DateUtils.formatDT(d), null);
		}

		public byte[] toBytes(Date[] d) {
			if (d == null || d.length == 0)
				return null;
			String[] ss = new String[d.length];
			for (int i = 0; i < ss.length; i++) {
				ss[i] = DateUtils.formatDT(d[i]);
			}
			return VR.strs2bytes(ss, null);
		}

		public byte[] toBytes(DateRange dr) {
			if (dr == null)
				return null;
			StringBuffer sb = new StringBuffer(36);
			if (dr.getStart() != null)
				sb.append(DateUtils.formatDT(dr.getStart()));
			sb.append("-");
			if (dr.getEnd() != null)
				sb.append(DateUtils.formatDT(dr.getEnd()));
			return VR.str2bytes(sb.toString(), null);
		}
		
		public Date toDate(byte[] val) {
			return DateUtils.parseDT(
					StringUtils.trim(VR.bytes2str1(val, null)), false);
		}

		public Date[] toDates(byte[] val) {
			if (val == null || val.length == 0)
				return EMPTY_DATE_ARRAY;
			String[] ss = StringUtils.trim(VR.bytes2strs(val, null));
			Date[] ds = new Date[ss.length];
			for (int i = 0; i < ds.length; i++) {
				ds[i] = DateUtils.parseDT(ss[i], false);
			}
			return ds;
		}
		
		public DateRange toDateRange(byte[] val) {
			String s = StringUtils.trim(VR.bytes2str1(val, null));
			int l;
			if (s == null || (l = s.length()) == 0 || s.equals("-"))
				return null;
			int hypen = s.indexOf('-');
			Date start = hypen == 0 ? null : DateUtils.parseDT(
					hypen == -1 ? s : s.substring(0, hypen), false);
			Date end = hypen + 1 == l ? null 
					: DateUtils.parseDT(s.substring(hypen+1), true);
			return new DateRange(start, end);
		}
	}

	private static final class FL extends VR {

		private FL() {
			super(0x464c, 0, 8);
		}

		public byte[] toBytes(float val, boolean bigEndian) {
			byte[] b = new byte[4];
			return bigEndian ? ByteUtils.float2bytesBE(val, b, 0)
					: ByteUtils.float2bytesLE(val, b, 0);
		}
		
		public byte[] toBytes(float[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.floats2bytesBE(val)
					: ByteUtils.floats2bytesLE(val);
		}
		
		public byte[] toBytes(String val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return toBytes(Float.parseFloat(val), bigEndian);
		}
		
		public byte[] toBytes(String[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			float[] t = new float[val.length];
			for (int i = 0; i < val.length; i++) {
				t[i] = Float.parseFloat(val[i]);
			}
			return toBytes(t, bigEndian);
		}

		public float toFloat(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0f;
			return bigEndian ? ByteUtils.bytesBE2float(val, 0)
					: ByteUtils.bytesLE2float(val, 0);
		}

		public float[] toFloats(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return EMPTY_FLOAT_ARRAY;
			return bigEndian ? ByteUtils.bytesBE2floats(val)
					: ByteUtils.bytesLE2floats(val);
		}		

		public double toDouble(byte[] val, boolean bigEndian) {
			return toFloat(val, bigEndian);
		}

		public double[] toDoubles(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return EMPTY_DOUBLE_ARRAY;
			return bigEndian ? ByteUtils.bytesBE2floats2doubles(val)
					: ByteUtils.bytesLE2floats2doubles(val);
		}		
		
		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return null;
			return Float.toString(toFloat(val, bigEndian));
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			float[] t1 = toFloats(val, bigEndian);
			String[] t2 = new String[t1.length];
			for (int i = 0; i < t2.length; i++) {
				t2[i] = Float.toString(t1[i]);
			}
			return t2;
		}
		
		public void toggleEndian(byte[] b) {
			ByteUtils.toggleIntEndian(b);
		}
	}

	private static final class FD extends VR {

		private FD() {
			super(0x4644, 0, 8);
		}

		public byte[] toBytes(double val, boolean bigEndian) {
			byte[] b = new byte[4];
			return bigEndian ? ByteUtils.double2bytesBE(val, b, 0)
					: ByteUtils.double2bytesLE(val, b, 0);
		}
		
		public byte[] toBytes(double[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.doubles2bytesBE(val)
					: ByteUtils.doubles2bytesLE(val);
		}
		
		public byte[] toBytes(String val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return toBytes(Double.parseDouble(val), bigEndian);
		}
		
		public byte[] toBytes(String[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			double[] t = new double[val.length];
			for (int i = 0; i < val.length; i++) {
				t[i] = Double.parseDouble(val[i]);
			}
			return toBytes(t, bigEndian);
		}

		public double toDouble(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0f;
			return bigEndian ? ByteUtils.bytesBE2double(val, 0)
					: ByteUtils.bytesLE2double(val, 0);
		}

		public double[] toDoubles(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return EMPTY_DOUBLE_ARRAY;
			return bigEndian ? ByteUtils.bytesBE2doubles(val)
					: ByteUtils.bytesLE2doubles(val);
		}		
		
		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return null;
			return Double.toString(toDouble(val, bigEndian));
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			double[] t1 = toDoubles(val, bigEndian);
			String[] t2 = new String[t1.length];
			for (int i = 0; i < t2.length; i++) {
				t2[i] = Double.toString(t1[i]);
			}
			return t2;
		}
		
		public void toggleEndian(byte[] b) {
			ByteUtils.toggleLongEndian(b);
		}
	}

	private static final class IS extends ASCIIVR {

		private IS() {
			super(0x4953, ' ', 8);
		}

		public byte[] toBytes(int val, boolean bigEndian) {
			return toBytes(String.valueOf(val), bigEndian, null);
		}
		
		public byte[] toBytes(int[] val, boolean bigEndian) {
			if (val == null)
				return null;
			String[] ss = new String[val.length];
			for (int i = 0; i < ss.length; i++)
				ss[i] = Integer.toString(val[i]);
			return toBytes(ss, bigEndian, null);
		}
		
		public int toInt(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0;
			return (int) Long.parseLong(toString(val, bigEndian, null));
		}

		public int[] toInts(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return EMPTY_INT_ARRAY;
			String[] ss = toStrings(val, bigEndian, null);
			int[] is = new int[ss.length];
			for (int i = 0; i < is.length; i++)
				if (ss[i].length() > 0)
					is[i] = (int) Long.parseLong(ss[i]);
			return is;
		}		
	}

	private static final class LO extends StringVR {

		private LO() {
			super(0x4c4f, ' ', 8);
		}
	}

	private static final class LT extends TextVR {

		private LT() {
			super(0x4c54, ' ', 8);
		}
	}

	private static class OB extends VR implements Fragment {
		private OB() {
			super(0x4f42, 0, 12);
		}
	}
	
	private static final class OF extends VR implements Fragment {
		private OF() {
			super(0x4f46, 0, 12);
		}
		
		public byte[] toBytes(float val, boolean bigEndian) {
			byte[] b = new byte[4];
			return bigEndian ? ByteUtils.float2bytesBE(val, b, 0)
					: ByteUtils.float2bytesLE(val, b, 0);
		}
		
		public byte[] toBytes(float[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.floats2bytesBE(val)
					: ByteUtils.floats2bytesLE(val);
		}
		
		public float toFloat(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0f;
			return bigEndian ? ByteUtils.bytesBE2float(val, 0)
					: ByteUtils.bytesLE2float(val, 0);
		}

		public float[] toFloats(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return EMPTY_FLOAT_ARRAY;
			return bigEndian ? ByteUtils.bytesBE2floats(val)
					: ByteUtils.bytesLE2floats(val);
		}		
		
		public void toggleEndian(Object val) {
			if (val instanceof List) {
				List items = (List) val;
				for (int i = 0, n = items.size(); i < n; i++) {
					ByteUtils.toggleIntEndian((byte[]) items.get(i));
				}				
			} else {
				ByteUtils.toggleIntEndian((byte[]) val);
			}
		}
	}

	private static final class OW extends VR implements Fragment {

		private OW() {
			super(0x4f57, 0, 12);
		}
		
		public byte[] toBytes(int val, boolean bigEndian) {
			byte[] b = new byte[4];
			return bigEndian ? ByteUtils.ushort2bytesBE(val, b, 0) 
					: ByteUtils.ushort2bytesLE(val, b, 0);
		}
		
		public byte[] toBytes(int[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.ushorts2bytesBE(val) 
					: ByteUtils.ushorts2bytesLE(val);
		}

		public int toInt(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0;
			return bigEndian ? ByteUtils.bytesBE2ushort(val, 0) 
					: ByteUtils.bytesLE2ushort(val, 0);
		}

		public int[] toInts(byte[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.bytesBE2ushorts(val) 
					: ByteUtils.bytesLE2ushorts(val);
		}
		
		public void toggleEndian(Object val) {
			if (val instanceof List) {
				List items = (List) val;
				for (int i = 0, n = items.size(); i < n; i++) {
					ByteUtils.toggleShortEndian((byte[]) items.get(i));
				}				
			} else {
				ByteUtils.toggleShortEndian((byte[]) val);
			}
		}
	}

	private static final class PN extends StringVR {

		private PN() {
			super(0x504e, ' ', 8);
		}

		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			return StringUtils.trimPN(VR.bytes2str1(val, cs));
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			return StringUtils.trimPN(VR.bytes2strs(val, cs));
		}
	}

	private static final class SH extends StringVR {

		private SH() {
			super(0x5348, ' ', 8);
		}
	}

	private static final class SL extends IntVR {

		private SL() {
			super(0x534c, ' ', 8);
		}
		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return null;
			return Integer.toString(toInt(val, bigEndian));
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			int[] t1 = toInts(val, bigEndian);
			String[] t2 = new String[t1.length];
			for (int i = 0; i < t2.length; i++) {
				t2[i] = Integer.toString(t1[i]);
			}
			return t2;
		}
		
	}

	private static final class SQ extends VR {		

		private SQ() {
			super(0x5351, 0, 12);
		}
	}
	
	private static final class SS extends ShortVR {

		private SS() {
			super(0x5353, 0, 8);
		}

		public int toInt(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0;
			return bigEndian ? ByteUtils.bytesBE2sshort(val, 0) 
					: ByteUtils.bytesLE2sshort(val, 0);
		}

		public int[] toInts(byte[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.bytesBE2sshorts(val) 
					: ByteUtils.bytesLE2sshorts(val);
		}
	}

	private static final class ST extends TextVR {

		private ST() {
			super(0x5354, ' ', 8);
		}

	}

	private static final class TM extends ASCIIVR {

		private TM() {
			super(0x544d, ' ', 8);
		}

		public byte[] toBytes(Date d) {
			return VR.str2bytes(DateUtils.formatTM(d), null);
		}

		public byte[] toBytes(Date[] d) {
			if (d == null || d.length == 0)
				return null;
			String[] ss = new String[d.length];
			for (int i = 0; i < ss.length; i++) {
				ss[i] = DateUtils.formatTM(d[i]);
			}
			return VR.strs2bytes(ss, null);
		}

		public byte[] toBytes(DateRange dr) {
			if (dr == null)
				return null;
			StringBuffer sb = new StringBuffer(20);
			if (dr.getStart() != null)
				sb.append(DateUtils.formatTM(dr.getStart()));
			sb.append("-");
			if (dr.getEnd() != null)
				sb.append(DateUtils.formatTM(dr.getEnd()));
			return VR.str2bytes(sb.toString(), null);
		}
		
		public Date toDate(byte[] val) {
			return DateUtils.parseTM(
					StringUtils.trim(VR.bytes2str1(val, null)), false);
		}

		public Date[] toDates(byte[] val) {
			if (val == null || val.length == 0)
				return EMPTY_DATE_ARRAY;
			String[] ss = StringUtils.trim(VR.bytes2strs(val, null));
			Date[] ds = new Date[ss.length];
			for (int i = 0; i < ds.length; i++) {
				ds[i] = DateUtils.parseTM(ss[i], false);
			}
			return ds;
		}
		
		public DateRange toDateRange(byte[] val) {
			String s = StringUtils.trim(VR.bytes2str1(val, null));
			int l;
			if (s == null || (l = s.length()) == 0 || s.equals("-"))
				return null;
			int hypen = s.indexOf('-');
			Date start = hypen == 0 ? null : DateUtils.parseTM(
					hypen == -1 ? s : s.substring(0, hypen), false);
			Date end = hypen + 1 == l ? null 
					: DateUtils.parseTM(s.substring(hypen+1), true);
			return new DateRange(start, end);
		}
	}

	private static final class UI extends ASCIIVR {

		private UI() {
			super(0x5549, 0, 8);
		}
	}

	private static final class UL extends IntVR {

		private UL() {
			super(0x554c, 0, 8);
		}

		public String toString(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return null;
			return Long.toString(toInt(val, bigEndian) & 0xffffffffL);
		}

		public String[] toStrings(byte[] val, boolean bigEndian,
				SpecificCharacterSet cs) {
			if (val == null || val.length == 0)
				return EMPTY_STRING_ARRAY;
			int[] t1 = toInts(val, bigEndian);
			String[] t2 = new String[t1.length];
			for (int i = 0; i < t2.length; i++) {
				t2[i] = Long.toString(t1[i] & 0xffffffffL);
			}
			return t2;
		}
	}

	private static final class UN extends VR implements Fragment {

		private UN() {
			super(0x554e, 0, 12);
		}
	}

	private static final class US extends ShortVR {

		private US() {
			super(0x5553, 0, 8);
		}

		public int toInt(byte[] val, boolean bigEndian) {
			if (val == null || val.length == 0)
				return 0;
			return bigEndian ? ByteUtils.bytesBE2ushort(val, 0) 
					: ByteUtils.bytesLE2ushort(val, 0);
		}

		public int[] toInts(byte[] val, boolean bigEndian) {
			return bigEndian ? ByteUtils.bytesBE2ushorts(val) 
					: ByteUtils.bytesLE2ushorts(val);
		}
	}

	private static final class UT extends TextVR {

		private UT() {
			super(0x5554, ' ', 12);
		}
	}
	
	public static final VR AE = new AE();
	public static final VR AS = new AS();
	public static final VR AT = new AT();
	public static final VR CS = new CS();
	public static final VR DA = new DA();
	public static final VR DS = new DS();
	public static final VR DT = new DT();
	public static final VR FL = new FL();
	public static final VR FD = new FD();
	public static final VR IS = new IS();
	public static final VR LO = new LO();
	public static final VR LT = new LT();
	public static final VR OB = new OB();
	public static final VR OF = new OF();
	public static final VR OW = new OW();
	public static final VR PN = new PN();
	public static final VR SH = new SH();
	public static final VR SL = new SL();
	public static final VR SQ = new SQ();
	public static final VR SS = new SS();
	public static final VR ST = new ST();
	public static final VR TM = new TM();
	public static final VR UI = new UI();
	public static final VR UL = new UL();
	public static final VR UN = new UN();
	public static final VR US = new US();
	public static final VR UT = new UT();
	
	public static VR valueOf(int code) {
		switch (code) {
		case 0x4145:
			return AE;
		case 0x4153:
			return AS;
		case 0x4154:
			return AT;
		case 0x4353:
			return CS;
		case 0x4441:
			return DA;
		case 0x4453:
			return DS;
		case 0x4454:
			return DT;
		case 0x4644:
			return FD;
		case 0x464c:
			return FL;
		case 0x4953:
			return IS;
		case 0x4c4f:
			return LO;
		case 0x4c54:
			return LT;
		case 0x4f42:
			return OB;
		case 0x4f46:
			return OF;
		case 0x4f57:
			return OW;
		case 0x504e:
			return PN;
		case 0x5348:
			return SH;
		case 0x534c:
			return SL;
		case 0x5351:
			return SQ;
		case 0x5353:
			return SS;
		case 0x5354:
			return ST;
		case 0x544d:
			return TM;
		case 0x5549:
			return UI;
		case 0x554c:
			return UL;
		case 0x554E:
			return UN;
		case 0x5553:
			return US;
		case 0x5554:
			return UT;
		}
		throw new IllegalArgumentException(
				"vr:" + StringUtils.shortToHex(code));
	}

	protected final int code;
	protected final int headerLength;
	protected final int padding;

	private VR(int code, int padding, int headerLength) {
		this.code = code;
		this.padding = padding;
		this.headerLength = headerLength;
	}
	
	public final String toString() {
		return new String(
				new char[]{ 
						(char) (code >> 8),
						(char) (code & 0xff) });
	}
	
	public final int code() {
		return code;
	}
	
	public final int padding() {
		return padding;
	}

	public final int explicitVRHeaderLength() {
		return headerLength;
	}
	
	public byte[] toBytes(int val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}
	
	public byte[] toBytes(int[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}
	
	public byte[] toBytes(float val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}
	
	public byte[] toBytes(float[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}
	
	public byte[] toBytes(double val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}
	
	public byte[] toBytes(double[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}
	
	public byte[] toBytes(String val, boolean bigEndian,
			SpecificCharacterSet cs) {
		throw new UnsupportedOperationException();
	}
	
	public byte[] toBytes(String[] val, boolean bigEndian,
			SpecificCharacterSet cs) {
		throw new UnsupportedOperationException();
	}

	public byte[] toBytes(Date val) {
		throw new UnsupportedOperationException();
	}

	public byte[] toBytes(Date[] val) {
		throw new UnsupportedOperationException();
	}

	public byte[] toBytes(DateRange val) {
		throw new UnsupportedOperationException();
	}
	
	public int toInt(byte[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}

	public int[] toInts(byte[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}

	public float toFloat(byte[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}

	public float[] toFloats(byte[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}
	
	public double toDouble(byte[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}

	public double[] toDoubles(byte[] val, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}

	public String toString(byte[] val, boolean bigEndian, 
			SpecificCharacterSet cs) {
		throw new UnsupportedOperationException();
	}

	public String[] toStrings(byte[] val, boolean bigEndian,
			SpecificCharacterSet cs) {
		throw new UnsupportedOperationException();
	}

	public Date toDate(byte[] val) {
		throw new UnsupportedOperationException();
	}

	public Date[] toDates(byte[] val) {
		throw new UnsupportedOperationException();
	}
	
	public DateRange toDateRange(byte[] val) {
		throw new UnsupportedOperationException();
	}

	public void toggleEndian(Object val) {
		// NO OP		
	}
	
}
