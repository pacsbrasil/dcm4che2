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
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.dcm4che2.util.TagUtils;

abstract class FilteredDicomObject extends AbstractDicomObject {

	static final class Include extends FilteredDicomObject {		
		private static final long serialVersionUID = 1L;
		final int[] tags;
		public Include(DicomObject attrs, int[] tags) {
			super(attrs);
			this.tags = (int[]) tags.clone();
			Arrays.sort(this.tags);
		}

		protected boolean filter(int tag) {
			return Arrays.binarySearch(tags, tag) >= 0;
		}
	}

	static final class Exclude extends FilteredDicomObject {
		private static final long serialVersionUID = 1L;
		final int[] tags;
		public Exclude(DicomObject attrs, int[] tags) {
			super(attrs);
			this.tags = (int[]) tags.clone();
			Arrays.sort(this.tags);
		}

		protected boolean filter(int tag) {
			return Arrays.binarySearch(tags, tag) < 0;
		}
	}

	static final class Range extends FilteredDicomObject {
		private static final long serialVersionUID = 1L;
		final long fromTag;
		final long toTag;
		public Range(DicomObject attrs, int fromTag, int toTag) {
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
	
	static final class ExcludePrivate extends FilteredDicomObject {
		private static final long serialVersionUID = 1L;

		public ExcludePrivate(DicomObject attrs) {
			super(attrs);
		}

		protected boolean filter(int tag) {
			return !TagUtils.isPrivateDataElement(tag);
		}

	}

	static final class FilterSet extends FilteredDicomObject {
		private static final long serialVersionUID = 1L;
		final class FilterItr extends Itr implements Iterator {

			public FilterItr(Iterator itr) {
				super(itr);
			}
			
			public Object next() {
				DicomElement attr = (DicomElement) super.next();
				if (attr.vr() == VR.SQ && attr.hasItems()) {					
					return attr.filterItems(
							filter.getItem(attr.tag()));
				}
				return attr;
			}

		}

		final DicomObject filter;
		
		public FilterSet(DicomObject attrs, DicomObject filter) {
			super(attrs);
			this.filter = filter;
		}

		protected boolean filter(int tag) {
			return filter.contains(tag);
		}
		
		public DicomObject getItem(int tag) {
			DicomObject item = super.getItem(tag);
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


	protected final DicomObject attrs;
	
	public FilteredDicomObject(DicomObject attrs) {
		this.attrs = attrs;
	}
	
	protected abstract boolean filter(int tag);

    public int getItemPosition() {
        return attrs.getItemPosition();
    }
    
    public void setItemPosition(int pos) {
        attrs.setItemPosition(pos);
    }
    
	public long getItemOffset() {
		return attrs.getItemOffset();
	}

	public void setItemOffset(long offset) {
		throw new UnsupportedOperationException();
	}
	
	public boolean accept(final Visitor visitor) {
		return attrs.accept(new Visitor(){
			public boolean visit(DicomElement attr) {
				if (filter(attr.tag()))
					visitor.visit(attr);
				return true;
			}});
	}

	public Iterator iterator() {
		return new Itr(attrs.iterator());
	}

	public Iterator iterator(int fromTag, int toTag) {
		return new Itr(attrs.iterator(fromTag, toTag));
	}

	private class Itr implements Iterator {
		final Iterator itr;
		DicomElement next;
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
				next = (DicomElement) itr.next();
				if (filter(next.tag()))
					return;
			}
			next = null;			
		}

	}
		
	public boolean contains(int tag) {
		return filter(tag) && attrs.contains(tag);
	}

	public DicomElement get(int tag) {
		return filter(tag) ? attrs.get(tag) : null;
	}

	public DicomObject getParent() {
		return attrs.getParent();
	}

	public void setParent(DicomObject parent) {
		throw new UnsupportedOperationException();
	}
	
	public String getPrivateCreator(int privateTag) {
		return filter(privateTag) ? attrs.getPrivateCreator(privateTag) : null;
	}

	public DicomObject getRoot() {
		return attrs.getRoot();
	}

	public SpecificCharacterSet getSpecificCharacterSet() {
		return attrs.getSpecificCharacterSet();
	}

	public TransferSyntax getTransferSyntax() {
		return attrs.getTransferSyntax();
	}

	public boolean cacheGet() {
		return attrs.cacheGet();
	}

	public boolean cachePut() {
		return attrs.cachePut();
	}

	public void add(DicomElement attr) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putBytes(int tag, VR vr, boolean bigEndian, byte[] val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putDouble(int tag, VR vr, double val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putDoubles(int tag, VR vr, double[] val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putNull(int tag, VR vr) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putFloat(int tag, VR vr, float val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putFloats(int tag, VR vr, float[] val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putInt(int tag, VR vr, int val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putInts(int tag, VR vr, int[] val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putItem(int tag, DicomObject item) {
		throw new IllegalArgumentException(TagUtils.toString(tag));
	}

	public DicomElement putString(int tag, VR vr, String val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putStrings(int tag, VR vr, String[] val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putDate(int tag, VR vr, Date val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putDates(int tag, VR vr, Date[] val) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putDateRange(int tag, VR vr, DateRange val) {
		throw new UnsupportedOperationException();
	}
	
	public void clear() {
		throw new UnsupportedOperationException();
	}

	public DicomElement remove(int tag) {
		throw new UnsupportedOperationException();
	}

	public int reservePrivateTag(int privateTag, String privateCreator) {
		throw new UnsupportedOperationException();
	}

	public int resolvePrivateTag(int privateTag, String privateCreator) {
		return attrs.resolvePrivateTag(privateTag, privateCreator);
	}

	public void cacheGet(boolean cached) {
		attrs.cacheGet(cached);
	}

	public void cachePut(boolean cached) {
		attrs.cachePut(cached);
	}

	public void shareElements() {
		throw new UnsupportedOperationException();
	}

	public VR vrOf(int tag) {
		return attrs.vrOf(tag);
	}
    
    public String nameOf(int tag) {
        return attrs.nameOf(tag);
    }

	public DicomElement putFragments(int tag, VR vr, boolean bigEndian, int capacity) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putFragments(int tag, VR vr, boolean bigEndian) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putSequence(int tag, int capacity) {
		throw new UnsupportedOperationException();
	}

	public DicomElement putSequence(int tag) {
		throw new UnsupportedOperationException();
	}
}
