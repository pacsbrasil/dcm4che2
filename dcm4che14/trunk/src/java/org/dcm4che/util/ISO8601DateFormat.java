package org.dcm4che.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * @author joseph foraci
 *
 * the parse(String, ParsePosition) is working in this one...
 */
public final class ISO8601DateFormat extends org.apache.log4j.helpers.ISO8601DateFormat {
	private int p = 0;

	public ISO8601DateFormat()
	{
		super();
	}
	public ISO8601DateFormat(TimeZone timeZone)
	{
		super(timeZone);
	}

	public Date parse(String s, ParsePosition pos)
	{
		int original = pos.getIndex();
		try {
			//get substring starting starting at pos
			s = s.substring(original);
			Date dt = parseDateTime(s);
			pos.setIndex(original + p);
			return dt;
		}
		// total failure cases
		catch (NoSuchElementException ne) {
			pos.setIndex(original);
			return null;
		}
		catch (ParseException pe) {
			pos.setIndex(original);
			return null;
		}
		catch (NumberFormatException nfe) {
			pos.setIndex(original);
			return null;
		}
	}

	/**
	 * Checks <code>s</code> for a parsable <i>ISO 8601</i>-compliant time
	 *  format.
	 * 
	 * If parser is not set to be lenient, then it will <i>still</i>
	 *  allow:
	 *  - Improper (extra) leading zeros on year portion.
	 *  If parser is set to be lenient it will allow anything above and:
	 *  - Improper length of elements, if they are not out of range (ie '1'
	 *    would be allowed in addition to '01', but '33' would not).
	 *  - Lowercase 'z' would be acceptable for the end of string.
	 *  - Extra data at the end of string is ignored.
	 * 
	 * The (approximate) position at which parsing stopped is left in
	 *  <code>p></code>.
	 *
	 * @param s <code>String</code> to parse for date/time
	 * @param pos The <code>ParsePosition</code> representing the position
	 *  where parsing begins.
	 * @return <code>Date</code> representing the parsed date or
	 *  <code>null</code> if an exception is thrown.
	 * @throws NoSuchElementException
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	private Date parseDateTime(String s)
		throws NoSuchElementException, NumberFormatException, ParseException
	{
		//tokenize based on allowed delims
		StringTokenizer st = new StringTokenizer(s, ".+-:TZz", true);
		String tok; //a token
		boolean strict = !isLenient();
		int f = 1, year, month, day, hour, min;
		int sec, msec = 0, off = 0;

		p = 0;
		tok = st.nextToken();
		p += tok.length();
		//get year
		if ("+".equals(tok) || "-".equals(tok)) { //watch for leading +/-
			if ("-".equals(tok)) {
				f = -1;
			}
			tok = st.nextToken();
		}
		year = f * Integer.parseInt(tok);
		if ((tok.length()<4 && strict) ||
			!(tok = st.nextToken()).equals("-"))
			throw new ParseException("invalid year",p);
		p += tok.length();
		//get month
		tok = st.nextToken();
		p += tok.length();
		month = Integer.parseInt(tok);
		if ((tok.length()!=2 && strict) || month==0 || month>12 ||
			!(tok = st.nextToken()).equals("-"))
			throw new ParseException("invalid month",p);
		p += tok.length();
		//get day
		tok = st.nextToken();
		p += tok.length();
		day = Integer.parseInt(tok);
		if ((tok.length()!=2 && strict) || day==0 || day>31 ||
			!(tok = st.nextToken()).equals("T"))
			throw new ParseException("invalid day",p);
		p += tok.length();
		//get hour
		tok = st.nextToken();
		p += tok.length();
		hour = Integer.parseInt(tok);
		if ((tok.length()!=2 && strict) || hour>23 ||
			!(tok = st.nextToken()).equals(":"))
			throw new ParseException("invalid hour",p);
		p += tok.length();
		//get minute
		tok = st.nextToken();
		p += tok.length();
		min = Integer.parseInt(tok);
		if ((tok.length()!=2 && strict) || min>59 ||
			!(tok = st.nextToken()).equals(":"))
			throw new ParseException("invalid minute",p);
		p += tok.length();
		//get second
		tok = st.nextToken();
		p += tok.length();
		sec = Integer.parseInt(tok);
		if ((tok.length()!=2 && strict) || sec>59)
			throw new ParseException("invalid second",p);
		p += tok.length();
		//
		if (st.hasMoreTokens()) {
			//get fractional second portion, if exists
			tok = st.nextToken();
			p += tok.length();
			if (".".equals(tok)) {
				tok = st.nextToken();
				p += tok.length();
				msec = Integer.parseInt(tok);
				if (tok.length()!=3)
					msec = (int)((double)msec * Math.pow(10,(3-tok.length())) + 0.5);
				tok = st.nextToken();
				p += tok.length();
			}
			//get time zone offset, if exists
			if ("-".equals(tok) || "+".equals(tok)) {
				if ("-".equals(tok)) f = -1;
				else f = 1;
				tok = st.nextToken();
				p += tok.length();
				off = Integer.parseInt(tok) * 3600 * 1000;
				if ((tok.length()!=2 && strict) || !(tok = st.nextToken()).equals(":"))
					throw new ParseException("invalid zone hour offset length",p);
				p += tok.length();
				tok = st.nextToken();
				p += tok.length();
				off += Integer.parseInt(tok) * 60 * 1000;
				if (tok.length()!=2 && strict)
					throw new ParseException("invalid zone min offset length",p);
				off *= f;
			}
			else if ((!"Z".equals(tok) && strict) ||
				(tok.charAt(0)!='Z' && tok.charAt(0)!='z')) {
				throw new ParseException("invalid time zone",p);
			}
			//check for extra tokens exception
			if (st.hasMoreTokens() && strict) {
				throw new ParseException("extra tokens",p);
			}
		}
		else if (strict) {
			throw new ParseException("missing time zone",p);
		}
		//return
		Calendar cal = new GregorianCalendar(year,month-1,day,hour,min,sec);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.ZONE_OFFSET,off);
		return cal.getTime();
	}
}

