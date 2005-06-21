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

	private int itemLength = -1;

	final int getItemLength() {
		return itemLength;
	}

	final void setItemLength(int itemLength) {
		this.itemLength = itemLength;
	}

	protected Object writeReplace() throws ObjectStreamException {
		return new AttributeSetSerializer(this);
	}

	public void serializeAttributes(ObjectOutputStream oos)
			throws IOException {
		oos.writeObject(new AttributeSerializer(this));
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
