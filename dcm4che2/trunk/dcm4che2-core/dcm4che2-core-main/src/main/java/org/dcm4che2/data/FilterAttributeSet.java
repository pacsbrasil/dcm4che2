/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.dcm4che2.util.TagUtils;

abstract class FilterAttributeSet extends AbstractAttributeSet {

	static final class Include extends FilterAttributeSet {		
		private static final long serialVersionUID = 1L;
		final int[] tags;
		public Include(AttributeSet attrs, int[] tags) {
			super(attrs);
			this.tags = (int[]) tags.clone();
			Arrays.sort(this.tags);
		}

		protected boolean filter(int tag) {
			return Arrays.binarySearch(tags, tag) >= 0;
		}
	}

	static final class Exclude extends FilterAttributeSet {
		private static final long serialVersionUID = 1L;
		final int[] tags;
		public Exclude(AttributeSet attrs, int[] tags) {
			super(attrs);
			this.tags = (int[]) tags.clone();
			Arrays.sort(this.tags);
		}

		protected boolean filter(int tag) {
			return Arrays.binarySearch(tags, tag) < 0;
		}
	}

	static final class Range extends FilterAttributeSet {
		private static final long serialVersionUID = 1L;
		final long fromTag;
		final long toTag;
		public Range(AttributeSet attrs, int fromTag, int toTag) {
			super(attrs);
			if ((fromTag & 0xffffffffL) > (toTag & 0xffffffffL)) {
				throw new IllegalArgumentException("fromTag:"
						+ TagUtils.toString(fromTag) 
						+ " > toTag:"
						+ TagUtils.toString(toTag));
			}
			this.fromTag = fromTag & 0xffffffffL;
			this.toTag = toTag & 0xffffffffL;
			if (this.fromTag > this.toTag) {
				throw new IllegalArgumentException("fromTag:"
						+ TagUtils.toString(fromTag) 
						+ " > toTag:"
						+ TagUtils.toString(toTag));
			}
		}

		protected boolean filter(int tag) {
			long ltag = tag & 0xffffffffL;
			return fromTag <= ltag && ltag <= toTag;
		}

		public Iterator iterator() {
			return new Itr(attrs.iterator((int) fromTag, (int) toTag));
		}

		public Iterator iterator(int fromTag, int toTag) {
			final long maxFromTag = Math.max(fromTag & 0xffffffff, this.fromTag);
			final long minToTag = Math.min(toTag & 0xffffffff, this.toTag);
			return new Itr(attrs.iterator((int) maxFromTag, (int) minToTag));
		}
	}

	
	static final class ExcludePrivate extends FilterAttributeSet {
		private static final long serialVersionUID = 1L;

		public ExcludePrivate(AttributeSet attrs) {
			super(attrs);
		}

		protected boolean filter(int tag) {
			return !TagUtils.isPrivateDataElement(tag);
		}

	}

	static final class FilterSet extends FilterAttributeSet {
		private static final long serialVersionUID = 1L;
		final class FilterItr extends Itr implements Iterator {

			public FilterItr(Iterator itr) {
				super(itr);
			}
			
			public Object next() {
				Attribute attr = (Attribute) super.next();
				if (attr.vr() == VR.SQ && attr.hasItems()) {					
					return attr.filterItems(
							filter.getItem(attr.tag()));
				}
				return attr;
			}

		}

		final AttributeSet filter;
		
		public FilterSet(AttributeSet attrs, AttributeSet filter) {
			super(attrs);
			this.filter = filter;
		}

		protected boolean filter(int tag) {
			return filter.contains(tag);
		}
		
		public AttributeSet getItem(int tag) {
			AttributeSet item = super.getItem(tag);
			if (item == null)
				return null;
			
			return item.subSet(filter.getItem(tag));
		}

		public Iterator iterator() {
			return new FilterItr(attrs.iterator());
		}

		public Iterator iterator(int fromTag, int toTag) {
			return new FilterItr(attrs.iterator(fromTag, toTag));
		}
	}


	protected final AttributeSet attrs;
	
	public FilterAttributeSet(AttributeSet attrs) {
		this.attrs = attrs;
	}
	
	protected abstract boolean filter(int tag);

	public long getItemOffset() {
		return attrs.getItemOffset();
	}

	public void setItemOffset(long offset) {
		throw new UnsupportedOperationException();
	}
	
	public Iterator iterator() {
		return new Itr(attrs.iterator());
	}

	public Iterator iterator(int fromTag, int toTag) {
		return new Itr(attrs.iterator(fromTag, toTag));
	}

	private class Itr implements Iterator {
		final Iterator itr;
		Attribute next;
		public Itr(Iterator itr) {
			this.itr = itr;
			findNext();
		}
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		public boolean hasNext() {
			return next != null;
		}
		public Object next() {
			if (next == null)
				return new NoSuchElementException();
			Object tmp = next;
			findNext();
			return tmp;
		}
		private void findNext() {
			while (itr.hasNext()) {
				next = (Attribute) itr.next();
				if (filter(next.tag()))
					return;
			}
			next = null;			
		}

	}
		
	public void addAll(AttributeSet other) {
		throw new UnsupportedOperationException();
	}

	public boolean contains(int tag) {
		return filter(tag) && attrs.contains(tag);
	}

	public boolean containsValue(int tag) {
		return filter(tag) && attrs.containsValue(tag);
	}

	public Attribute getAttribute(int tag) {
		return filter(tag) ? attrs.getAttribute(tag) : null;
	}

	public byte[] getBytes(int tag, boolean bigEndian) {
		return filter(tag) ? attrs.getBytes(tag, bigEndian) : null;
	}

	public double getDouble(int tag) {
		return filter(tag) ? attrs.getDouble(tag) : 0.;
	}

	public double[] getDoubles(int tag) {
		return filter(tag) ? attrs.getDoubles(tag) : null;
	}

	public float getFloat(int tag) {
		return filter(tag) ? attrs.getFloat(tag) : 0f;
	}

	public float[] getFloats(int tag) {
		return filter(tag) ? attrs.getFloats(tag) : null;
	}

	public int getInt(int tag) {
		return filter(tag) ? attrs.getInt(tag) : 0;
	}

	public int[] getInts(int tag) {
		return filter(tag) ? attrs.getInts(tag) : null;
	}

	public AttributeSet getItem(int tag) {
		return filter(tag) ? attrs.getItem(tag) : null;
	}

	public AttributeSet getParent() {
		return attrs.getParent();
	}

	public void setParent(AttributeSet parent) {
		throw new UnsupportedOperationException();
	}
	
	public String getPrivateCreator(int privateTag) {
		return filter(privateTag) ? attrs.getPrivateCreator(privateTag) : null;
	}

	public AttributeSet getRoot() {
		return attrs.getRoot();
	}

	public SpecificCharacterSet getSpecificCharacterSet() {
		return attrs.getSpecificCharacterSet();
	}

	public String getString(int tag) {
		return filter(tag) ? attrs.getString(tag) : null;
	}

	public String[] getStrings(int tag) {
		return filter(tag) ? attrs.getStrings(tag) : null;
	}

	public TransferSyntax getTransferSyntax() {
		return attrs.getTransferSyntax();
	}

	public boolean isCacheAttributeValues() {
		return attrs.isCacheAttributeValues();
	}

	public Attribute putBytes(int tag, VR vr, boolean bigEndian, byte[] val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putDouble(int tag, VR vr, double val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putDoubles(int tag, VR vr, double[] val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putNull(int tag, VR vr) {
		throw new UnsupportedOperationException();
	}

	public Attribute putFloat(int tag, VR vr, float val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putFloats(int tag, VR vr, float[] val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putInt(int tag, VR vr, int val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putInts(int tag, VR vr, int[] val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putItem(int tag, AttributeSet item) {
		throw new IllegalArgumentException(TagUtils.toString(tag));
	}

	public Attribute putString(int tag, VR vr, String val) {
		throw new UnsupportedOperationException();
	}

	public Attribute putStrings(int tag, VR vr, String[] val) {
		throw new UnsupportedOperationException();
	}

	public Attribute removeAttribute(int tag) {
		throw new UnsupportedOperationException();
	}

	public int reservePrivateTag(int privateTag, String privateCreator) {
		throw new UnsupportedOperationException();
	}

	public int resolvePrivateTag(int privateTag, String privateCreator) {
		return attrs.resolvePrivateTag(privateTag, privateCreator);
	}

	public void setCacheAttributeValues(boolean cached) {
		attrs.setCacheAttributeValues(cached);
	}

	public void shareAttributes() {
		throw new UnsupportedOperationException();
	}

	public VR vrOf(int tag) {
		return attrs.vrOf(tag);
	}

	public Attribute putFragments(int tag, VR vr, boolean bigEndian, int capacity) {
		throw new UnsupportedOperationException();
	}

	public Attribute putFragments(int tag, VR vr, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}

	public Attribute putSequence(int tag, int capacity) {
		throw new UnsupportedOperationException();
	}

	public Attribute putSequence(int tag) {
		throw new UnsupportedOperationException();
	}
}
