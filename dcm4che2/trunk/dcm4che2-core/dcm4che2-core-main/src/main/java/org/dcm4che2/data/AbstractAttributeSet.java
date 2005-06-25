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
	
	public AttributeSet exclude(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilterAttributeSet.Exclude(this, tags) : this;
	}

	public AttributeSet excludePrivate() {
		return new FilterAttributeSet.ExcludePrivate(this);
	}

	public AttributeSet subSet(AttributeSet filter) {
		return filter != null ?
				new FilterAttributeSet.FilterSet(this, filter) : null;
	}

	public AttributeSet subSet(int fromTag, int toTag) {
		return new FilterAttributeSet.Range(this, fromTag, toTag);
	}

	public AttributeSet subSet(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilterAttributeSet.Include(this, tags) : this;
	}

}
