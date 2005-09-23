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

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 27, 2005
 *
 */
public class DateRange {

	private final Date start;
	private final Date end;
	
	public DateRange(Date start, Date end) {
		this.start = start;
		this.end = end;
	}
	
	public final Date getStart() {
		return start;
	}
	
	public final Date getEnd() {
		return end;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (start != null) sb.append(start);
		sb.append("-");
		if (end != null) sb.append(end);
		return sb.toString();
	}
	
}
