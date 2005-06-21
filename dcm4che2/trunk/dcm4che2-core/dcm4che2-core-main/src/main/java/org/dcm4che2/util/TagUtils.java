/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.util;

public class TagUtils {

	public static boolean isCommandElement(int tag) {
		return (tag & 0xffff0000) == 0;
	}

	public static boolean isFileMetaInfoElement(int tag) {
		return (tag & 0xffff0000) == 0x00020000;
	}

	public static boolean isGroupLengthElement(int tag) {
		return (tag & 0x0000ffff) == 0;
	}

	public static boolean isPrivateDataElement(int tag) {
		return (tag & 0x00010000) != 0;
	}
	
	public static boolean isPrivateCreatorDataElement(int tag) {
		return (tag & 0x00010000) != 0 && (tag & 0x0000ff00) == 0;
	}

	public static StringBuffer toStringBuffer(int tag, StringBuffer sb) {
		sb.append('(');
		StringUtils.shortToHex(tag >> 16, sb);
		sb.append(',');
		StringUtils.shortToHex(tag, sb);
		sb.append(')');
		return sb;
	}

	public static String toString(int tag) {
		return toStringBuffer(tag, new StringBuffer(11)).toString();
	}
}
