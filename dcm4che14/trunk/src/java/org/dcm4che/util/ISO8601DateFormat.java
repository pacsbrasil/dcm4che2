package org.dcm4che.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * @author joseph foraci
 *
 * the parse(String, ParsePosition) is working in this one...
 */
public final class ISO8601DateFormat extends DateFormat
{
	private int p = 0;

	public ISO8601DateFormat()
	{
        super();
        setCalendar(new GregorianCalendar());
	}

	public ISO8601DateFormat(TimeZone timeZone)
	{
        this();
        setTimeZone(timeZone);
	}

    /**
     * Formats a <code>Date</code> into a <code>StringBuffer</code>
     * 
     * @param date The date to format into a string
     * @param toAppendTo <code>StringBuffer</code> to which the formatted date will be appended
     * @param pos Keeps track of the position of the field within the returned
     * string. On input: an alignment field, if desired. On output: the offsets
     * of the alignment field.Unsupported field positions are ignored.
     * @see java.text.DateFormat#format(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition)
     */
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos)
    {
        StringBuffer fmtsb = new StringBuffer(20);
        int fieldStart, fieldEnd;
        Calendar cal = getCalendar();
        
        cal.setTime(date);
        //year
        fieldStart = 0;
        fieldEnd = fmtsb.append(pad(cal.get(Calendar.YEAR), 4)).length();
        if (pos.getField() == DateFormat.YEAR_FIELD)
            setFieldPosition(pos, fieldStart, fieldEnd);
        //month
        fmtsb.append("-");
        fieldStart = fmtsb.length();
        fieldEnd = fmtsb.append(pad(cal.get(Calendar.MONTH) + 1, 2)).length();
        if (pos.getField() == DateFormat.MONTH_FIELD)
            setFieldPosition(pos, fieldStart, fieldEnd);
        //day
        fmtsb.append("-");
        fieldStart = fmtsb.length();
        fieldEnd = fmtsb.append(pad(cal.get(Calendar.DAY_OF_MONTH), 2)).length();
        if (pos.getField() == DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD)
            setFieldPosition(pos, fieldStart, fieldEnd);
        //hour
        fmtsb.append("T");
        fieldStart = fmtsb.length();
        fieldEnd = fmtsb.append(pad(cal.get(Calendar.HOUR_OF_DAY), 2)).length();
        if (pos.getField() == DateFormat.HOUR_OF_DAY0_FIELD)
            setFieldPosition(pos, fieldStart, fieldEnd);
        //minute
        fmtsb.append(":");
        fieldStart = fmtsb.length();
        fieldEnd = fmtsb.append(pad(cal.get(Calendar.MINUTE), 2)).length();
        if (pos.getField() == DateFormat.MINUTE_FIELD)
            setFieldPosition(pos, fieldStart, fieldEnd);
        //second
        fmtsb.append(":");
        fieldStart = fmtsb.length();
        fieldEnd = fmtsb.append(pad(cal.get(Calendar.SECOND), 2)).length();
        if (pos.getField() == DateFormat.SECOND_FIELD)
            setFieldPosition(pos, fieldStart, fieldEnd);
        //timezone
        fieldStart = fieldEnd;
        int zoneOffsetMinutes = cal.get(Calendar.ZONE_OFFSET) / (1000 * 60);
        if (zoneOffsetMinutes == 0) {
            fieldEnd = fieldStart + 1;
            fmtsb.append("Z");
        }
        else {
            final boolean neg;
            if (zoneOffsetMinutes < 0) {
                zoneOffsetMinutes = Math.abs(zoneOffsetMinutes);
                neg = true;
            }
            else
                neg = false;
            int hourOff = zoneOffsetMinutes / 60;
            int minOff = zoneOffsetMinutes % 60;
            fieldEnd = fmtsb.append((neg ? "-" : "+") + pad(hourOff, 2) + ":"
                                    + pad(minOff, 2)).length();
        }
        if (pos.getField() == DateFormat.TIMEZONE_FIELD)
            setFieldPosition(pos, fieldStart, fieldEnd);
        else if (pos.getField() == DateFormat.DATE_FIELD)
            setFieldPosition(pos, 0, fieldEnd);
        //append
        toAppendTo.append(fmtsb);
        return fmtsb;
    }

    //throws exception if num < 0
    //if num formats to a larger string than size, the former is returned
    //base 10
    private static StringBuffer pad(int num, int size)
    {
        if (num < 0)
            throw new IllegalArgumentException("num can not be negative");
        String snum = Integer.toString(num);
        StringBuffer sb = new StringBuffer(size);
        for (int i = 0, n = size - snum.length(); i < n; i++)
            sb.append('0');
        sb.append(snum);
        return sb;
    }

    private static void setFieldPosition(FieldPosition pos, int startPos, int endPos)
    {
        pos.setBeginIndex(startPos);
        pos.setEndIndex(endPos);
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
	 *  - Improper length of elements, if they are not out of range (ie, '1'
	 *    would be allowed in addition to '01', but '33' would not be allowed).
	 *  - Extra data at the end of string may be ignored.
	 * 
	 * The (approximate) position at which parsing stopped is left in
	 *  <code>p</code>.
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
        else {  //not strict and missing timezone (assume this timezone)
            off = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
        }
		//return
		Calendar cal = new GregorianCalendar(year,month-1,day,hour,min,sec);
        //set time to be in GMT timezone, by telling the Calendar it's offset from GMT
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.ZONE_OFFSET,off);
        //don't forget to set the milliseconds!
        cal.set(Calendar.MILLISECOND,msec);
		return cal.getTime();
	}
}

