/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.util;

public class StringUtils {

	private static final String[] EMPTY_STRING_ARRAY = {};
	private static final char[] HEX_DIGITS = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'A' , 'B' ,
		'C' , 'D' , 'E' , 'F'
	};

	public static String join(String[] ss, char delim) {
		if (ss == null)
			return null;
		if (ss.length == 0)
			return "";
		if (ss.length == 1)
			return ss[0];
		int sumlen = 0;
		for (int i = 0; i < ss.length; i++) {
			if (ss[i] != null)
				sumlen += ss[i].length();
		}
		StringBuffer sb = new StringBuffer(sumlen + ss.length);
		for (int i = 0; i < ss.length; i++) {
			if (ss[i] != null)
				sb.append(ss[i]);
			sb.append('\\');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public static String[] split(String s, char delim) {
        if (s == null)
            return null;
        if (s.length() == 0)
            return EMPTY_STRING_ARRAY;
        final int r0 = s.indexOf(delim);
        if (r0 == -1)
            return new String[]{s};
        int i = 2;
        int l, r = r0;
        for (; (r = s.indexOf(delim, l = r + 1)) != -1; ++i);
        String[] ss = new String[i];
        i = l = 0;
        r = r0;
        do ss[i++] = s.substring(l, r);
        while ((r = s.indexOf(delim, l = r + 1)) != -1);
        ss[i] = s.substring(l);
        return ss;
    }

	public static String first(String s, char delim) {
        if (s == null || s.length() == 0)
            return null;
        final int r0 = s.indexOf(delim);
        return r0 == -1 ? s : s.substring(0, r0);
	}
	
    private static String trim(String s, char lead, char tail1, char tail2) {
		if (s == null)
			return null;		
		int len = s.length();
		int st = 0;
		char c;
		while ((st < len) && (s.charAt(st) == lead))
		    st++;
		while ((st < len) && ((c = s.charAt(len - 1)) == tail1 || c == tail2))
		    len--;		
		return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
	}

    private static String[] trim(String[] ss, char lead, char tail1, char tail2) {
		if (ss == null)
			return null;		
		for (int i = 0; i < ss.length; i++)
			ss[i] = trim(ss[i], lead, tail1, tail2);
		return ss;
    }
	
    public static String trim(String s) {
		return trim(s, ' ', '\0', ' ');
	}

    public static String[] trim(String[] ss) {
		return trim(ss, ' ', '\0', ' ');
    }

    public static String trimPN(String s) {
		return trim(s, ' ', '^', ' ');
	}

    public static String[] trimPN(String[] ss) {
		return trim(ss, ' ', '^', ' ');
    }

    public static String trimEnd(String s) {
		return trim(s, '\0', '\0', ' ');
	}

    public static String[] trimEnd(String[] ss) {
		return trim(ss, '\0', '\0', ' ');
    }

	public static StringBuffer intToHex(int val, StringBuffer sb) {
		sb.append(HEX_DIGITS[(val >> 28) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 24) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 20) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 16) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 12) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 8) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 4) & 0xf]);
		sb.append(HEX_DIGITS[val & 0xf]);
		return sb;
	}

	public static StringBuffer shortToHex(int val, StringBuffer sb) {
		sb.append(HEX_DIGITS[(val >> 12) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 8) & 0xf]);
		sb.append(HEX_DIGITS[(val >> 4) & 0xf]);
		sb.append(HEX_DIGITS[val & 0xf]);
		return sb;
	}

	public static StringBuffer byteToHex(int val, StringBuffer sb) {
		sb.append(HEX_DIGITS[(val >> 4) & 0xf]);
		sb.append(HEX_DIGITS[val & 0xf]);
		return sb;
	}

	public static String shortToHex(int val) {
		return shortToHex(val, new StringBuffer(4)).toString();
	}


}
