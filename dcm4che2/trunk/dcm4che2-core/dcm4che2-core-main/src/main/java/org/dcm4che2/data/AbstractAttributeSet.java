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
		return a == null ? 0 : a.getInt(cache());
	}

	public int[] getInts(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getInts(cache());
	}

	public float getFloat(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getFloat(cache());
	}

	public float[] getFloats(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getFloats(cache());
	}

	public double getDouble(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getDouble(cache());
	}

	public double[] getDoubles(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDoubles(cache());
	}

	public String getString(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getString(getSpecificCharacterSet(), cache());
	}

	public String[] getStrings(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a
				.getStrings(getSpecificCharacterSet(), cache());
	}

	public Date getDate(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDate(cache());
	}

	public Date[] getDates(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDates(cache());
	}

	public DateRange getDateRange(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDateRange(cache());
	}

	public Date getDate(int daTag, int tmTag) {
		return DateUtils.toDateTime(getDate(daTag), getDate(tmTag));
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
