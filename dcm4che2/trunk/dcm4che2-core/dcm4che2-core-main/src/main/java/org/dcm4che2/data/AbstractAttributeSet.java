/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.dcm4che2.util.DateUtils;

abstract class AbstractAttributeSet implements AttributeSet {

	
	
	protected Object writeReplace() throws ObjectStreamException {
		return new AttributeSetSerializer(this);
	}

	public void serializeAttributes(ObjectOutputStream oos)
			throws IOException {
		oos.writeObject(new AttributeSerializer(this));
	}
	
	public void copyTo(final AttributeSet dest) {
		accept(new Visitor(){
			public boolean visit(Attribute attr) {
				dest.addAttribute(attr);
				return true;
			}});		
	}

	public boolean match(final AttributeSet attrs, final boolean ignoreCaseOfPN) {
		return accept(new Visitor(){
			public boolean visit(Attribute test) {
				if (test.isNull()) // Universal Matching
					return true;
				final int tag = test.tag();
				Attribute attr = attrs.getAttribute(tag);
				if (attr == null || attr.isNull())
					return true;	// Missing Attribute (Value) match always
				
				final VR vr = test.vr();
				if (vr instanceof VR.Fragment)
					return true; 	// ignore OB,OW,OF,UN filter attrs

				if (vr == VR.SQ)
					return matchSQ(attr, test.getItem(), ignoreCaseOfPN);
				
				if (vr == VR.DA) {
					int tmTag = DA_TM.getTMTag(tag);
					return tmTag != 0 
							? matchRange(attrs.getDates(tag, tmTag), 
									getDateRange(tag, tmTag)) 
							: matchRange(attr.getDates(attrs.cacheGet()), 
									test.getDateRange(cacheGet()));
				}
				if (vr == VR.TM) {
					int daTag = DA_TM.getDATag(tag);
					return daTag != 0 && containsValue(daTag) 
							? true // considered by visit of daTag
							: matchRange(attr.getDates(attrs.cacheGet()), 
									test.getDateRange(cacheGet()));
				}
				if (vr == VR.DT) {
					return matchRange(attr.getDates(attrs.cacheGet()), 
							test.getDateRange(cacheGet()));
				}
				return matchValue(
						attr.getStrings(attrs.getSpecificCharacterSet(),
								attrs.cacheGet()),
						test.getPattern(getSpecificCharacterSet(), 
								vr == VR.PN ? ignoreCaseOfPN : false,
								cacheGet()));				
			}});		
		
	}
	
	private boolean matchValue(String[] value, Pattern pattern) {
		for (int i = 0; i < value.length; i++) {
			if (pattern.matcher(value[i]).matches())
				return true;
		}
		return false;
	}

	private boolean matchRange(Date[] dates, DateRange dateRange) {
		for (int i = 0; i < dates.length; i++) {
			if (matchRange(dates[i], dateRange.getStart(), dateRange.getEnd()))
				return true;
		}
		return false;
	}

	private boolean matchRange(Date date, Date start, Date end) {
		if (start != null && start.after(date))
			return false;
		if (end != null && end.before(date))
			return false;
		return true;
	}

	private boolean matchSQ(Attribute sq, AttributeSet filter, boolean ignoreCaseOfPN) {
		if (filter.isEmpty())
			return true;
		for (int i = 0, n = sq.countItems(); i < n; i++) {
			if (!sq.getItem(i).match(filter, ignoreCaseOfPN))
				return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return accept(new Visitor(){
			public boolean visit(Attribute attr) {
				return false;
			}});
	}
	
	public int size() {
		final int[] count = { 0 };
		accept(new Visitor(){
			public boolean visit(Attribute attr) {
				++count[0];
				return true;
			}});
		return count[0];
	}

	public Iterator commandIterator() {
		return iterator(0x00000000, 0x0000ffff);
	}
	
	public Iterator fileMetaInfoIterator() {
		return iterator(0x00020000, 0x0002ffff);
	}
	
	public Iterator datasetIterator() {
		return iterator(0x00030000, 0xffffffff);
	}
	
	public AttributeSet command() {
		return subSet(0x00000000, 0x0000ffff);
	}
	
	public AttributeSet dataset() {
		return subSet(0x00030000, 0xffffffff);		
	}
	
	public AttributeSet fileMetaInfo() {
		return subSet(0x00020000, 0x0002ffff);
	}
	
	public AttributeSet exclude(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilteredAttributeSet.Exclude(this, tags) : this;
	}

	public AttributeSet excludePrivate() {
		return new FilteredAttributeSet.ExcludePrivate(this);
	}

	public AttributeSet subSet(AttributeSet filter) {
		return filter != null ?
				new FilteredAttributeSet.FilterSet(this, filter) : null;
	}

	public AttributeSet subSet(int fromTag, int toTag) {
		return new FilteredAttributeSet.Range(this, fromTag, toTag);
	}

	public AttributeSet subSet(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilteredAttributeSet.Include(this, tags) : this;
	}

	public boolean containsValue(int tag) {
		Attribute attr = getAttribute(tag);
		return attr != null && !attr.isNull();
	}

	public byte[] getBytes(int tag, boolean bigEndian) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.bigEndian(bigEndian).getBytes();
	}

	public AttributeSet getItem(int tag) {
		Attribute a = getAttribute(tag);
		return a == null && !a.isNull() ? null : a.getItem();
	}

	public int getInt(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getInt(cacheGet());
	}

	public int[] getInts(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getInts(cacheGet());
	}

	public float getFloat(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getFloat(cacheGet());
	}

	public float[] getFloats(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getFloats(cacheGet());
	}

	public double getDouble(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getDouble(cacheGet());
	}

	public double[] getDoubles(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDoubles(cacheGet());
	}

	public String getString(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getString(getSpecificCharacterSet(), cacheGet());
	}

	public String[] getStrings(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a
				.getStrings(getSpecificCharacterSet(), cacheGet());
	}

	public Date getDate(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDate(cacheGet());
	}

	public Date[] getDates(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDates(cacheGet());
	}

	public DateRange getDateRange(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDateRange(cacheGet());
	}

	public Date getDate(int daTag, int tmTag) {
		return DateUtils.toDateTime(getDate(daTag), getDate(tmTag));
	}

	public Date[] getDates(int daTag, int tmTag) {
		Date[] da = getDates(daTag);
		Date[] tm = getDates(tmTag);
		if (da == null)
			return null;
		if (tm != null) {
			for (int i = 0, n = Math.min(da.length, tm.length); i < n; ++i) {
				da[i] = DateUtils.toDateTime(da[i], tm[i]);
			}
		}
		return da;
	}
	
	public DateRange getDateRange(int daTag, int tmTag) {
		DateRange da = getDateRange(daTag);
		if (da == null) return null;
		DateRange tm = getDateRange(tmTag);
		if (tm == null) return da;
		return new DateRange(
				DateUtils.toDateTime(da.getStart(), tm.getStart()),
				DateUtils.toDateTime(da.getEnd(), tm.getEnd()));
	}
}
