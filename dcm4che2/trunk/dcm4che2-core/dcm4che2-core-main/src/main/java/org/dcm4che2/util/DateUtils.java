/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Jun 27, 2005
 *
 */
public class DateUtils {
	
	public static Date parseDA(String s, boolean end) {
		if (s == null || s.length() == 0)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		if (end) {
			setToDec31(c);
		}
		parseDA(c, s, 0, s.length());
		return c.getTime();
	}

	public static Date parseTM(String s, boolean end) {
		if (s == null || s.length() == 0)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		if (end) {
			setTo2359(c);
		}
		parseTM(c, s, 0, s.length());
		return c.getTime();
	}

	public static Date parseDT(String s, boolean end) {
		if (s == null || s.length() == 0)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		if (end) {
			setToDec31(c);
			setTo2359(c);
		}
		int len = s.length();
		final char tzsign = s.charAt(len - 5);
		if (tzsign == '+' || tzsign == '-') {
			len -= 5;
			c.setTimeZone(TimeZone.getTimeZone("GMT" + s.substring(len)));
		}
		int pos = parseDA(c, s, 0, len);
		if (pos + 2 < len) {
			parseTM(c, s, pos, len);
		}
		return c.getTime();		
	}
	
	public static String formatDA(Date d) {
		if (d == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return formatDA(c, new StringBuffer(8)).toString();
	}

	public static String formatTM(Date d) {
		if (d == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return formatTM(c, new StringBuffer(10)).toString();
	}

	public static String formatDT(Date d) {
		if (d == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		StringBuffer sb = new StringBuffer(18);
		formatDA(c, sb);
		formatTM(c, sb);
		return sb.toString();
	}

	private static void setToDec31(Calendar c) {
		c.set(Calendar.MONTH, 11);
		c.set(Calendar.DAY_OF_MONTH, 31);		
	}

	private static void setTo2359(Calendar c) {
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);		
		c.set(Calendar.SECOND, 59);		
		c.set(Calendar.MILLISECOND, 999);		
	}

	private static int parseDA(Calendar c, String s, int off, int len) {
		int pos = off;
		c.set(Calendar.YEAR, Integer.parseInt(s.substring(pos, pos += 4)));
		if (pos < len) {
			if (!Character.isDigit(s.charAt(pos)))
				++pos;
			if (pos + 2 < len) {
				c.set(Calendar.MONTH, Integer.parseInt(
						s.substring(pos, pos +=2 )) - 1);
				if (pos < len) {
					if (!Character.isDigit(s.charAt(pos)))
						++pos;
					if (pos + 2 < len) {
						c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
								s.substring(pos, pos += 2)) - 1);
					}					
				}
			}			
		}
		return pos;
	}


	private static int parseTM(Calendar c, String s, int off, int len) {
		int pos = off;
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(pos, pos += 2)));
		if (pos < len) {
			if (!Character.isDigit(s.charAt(pos)))
				++pos;
			if (pos + 2 < len) {
				c.set(Calendar.MINUTE, Integer.parseInt(
						s.substring(pos, pos +=2 )) - 1);
				if (pos < len) {
					if (!Character.isDigit(s.charAt(pos)))
						++pos;
					if (pos + 2 < len) {
						c.set(Calendar.SECOND, Integer.parseInt(
								s.substring(pos, pos += 2)) - 1);
						if (pos + 1 < len) {
							c.set(Calendar.MILLISECOND, 
									(int) (Float.parseFloat(
											s.substring(pos, len)) * 1000));
						}
					}					
				}
			}			
		}
		return pos;
	}

	private static StringBuffer formatDA(Calendar c, StringBuffer sb) {
		int yyyy = c.get(Calendar.YEAR);
		int mm = c.get(Calendar.MONTH) + 1;
		int dd = c.get(Calendar.DAY_OF_MONTH);
		sb.append(yyyy);
		if (mm < 10) sb.append("0");
		sb.append(mm);
		if (dd < 10) sb.append("0");
		sb.append(dd);
		return sb;
	}

	private static StringBuffer formatTM(Calendar c, StringBuffer sb) {
		int hh = c.get(Calendar.HOUR_OF_DAY);
		int mm = c.get(Calendar.MINUTE);
		int ss = c.get(Calendar.SECOND);
		int ms = c.get(Calendar.MILLISECOND);
		if (hh < 10) sb.append("0");
		sb.append(hh);
		if (mm < 10) sb.append("0");
		sb.append(mm);
		if (ss < 10) sb.append("0");
		sb.append(ss);
		sb.append(".");
		if (ms < 100) sb.append("0");
		if (ms < 10) sb.append("0");
		sb.append(ms);
		return sb;
	}

	public static Date toDateTime(Date date, Date time) {
		if (date == null)
			return null;
		if (time == null)
			return date;
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		Calendar t = Calendar.getInstance();
		t.setTime(time);
		t.set(Calendar.YEAR, d.get(Calendar.YEAR));
		t.set(Calendar.MONTH, d.get(Calendar.MONTH));
		t.set(Calendar.DAY_OF_MONTH, d.get(Calendar.DAY_OF_MONTH));
		return t.getTime();
	}

}
