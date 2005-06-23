/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dcm4che2.util.TagUtils;

public class BasicAttributeSet extends AbstractAttributeSet {

	private static final long serialVersionUID = 1L;

	private static final int INIT_FRAGMENT_CAPACITY = 2;

	private static final int INIT_SEQUENCE_CAPACITY = 10;
		
	private static final int TRANSFER_SYNTAX_TAG = 0x00020010;
	
	private static final int CHARSET_TAG = 0x00080005;

	private static final class TagStringPair {
		int tag;
		String value;
	}

	private static final Logger log = Logger.getLogger(BasicAttributeSet.class);
	
	private transient ArrayList attrs = new ArrayList();
	private transient TransferSyntax ts = TransferSyntax.ImplicitVRLittleEndian;
	private transient SpecificCharacterSet charset = null;
	private transient AttributeSet parent;
	private transient long itemOffset = -1L;
	private transient TagStringPair privateCreatorCache = new TagStringPair();
	private transient boolean cache = false;

	public final boolean isCacheAttributeValues() {
		return cache;
	}

	public final void setCacheAttributeValues(boolean cache) {
		this.cache = cache;
	}

	public int resolvePrivateTag(int privateTag, String privateCreator) {
		return resolvePrivateTagInternal(privateTag, privateCreator, false);
	}

	public int reservePrivateTag(int privateTag, String privateCreatorID) {
		return resolvePrivateTagInternal(privateTag, privateCreatorID, true);
	}

	public void shareAttributes() {
		for (int i = 0, n = attrs.size(); i < n; ++i) {
			attrs.set(i, ((Attribute) attrs.get(i)).share());
		}
	}
	
	public Iterator iterator() {
		return attrs.iterator();
	}

	public Iterator iterator(int fromTag, int toTag) {
		if ((fromTag & 0xffffffffL) > (toTag & 0xffffffffL)) {
			throw new IllegalArgumentException("fromTag:"
					+ TagUtils.toString(fromTag) 
					+ " > toTag:"
					+ TagUtils.toString(toTag));
		}
		int fromPos = binarySearch(fromTag);
		if (fromPos < 0) {
			fromPos = -(fromPos+1);
		}
		int toPos = binarySearch(toTag);
		if (toPos < 0) {
			toPos = -(toPos+1);
		}
		return attrs.subList(fromPos, toPos).iterator();
	}
	
	public final AttributeSet getParent() {
		return parent;		
	}

	public final void setParent(AttributeSet parent) {
		this.parent = parent;		
	}

	public AttributeSet getRoot() {
		return parent == null ? this : parent.getRoot();
	}
		
	public final long getItemOffset() {
		return itemOffset;
	}

	public final void setItemOffset(long itemOffset) {
		this.itemOffset = itemOffset;
	}
	
	public SpecificCharacterSet getSpecificCharacterSet() {
		if (charset != null)
			return charset;
		if (parent != null)
			return parent.getSpecificCharacterSet();
		return null;
	}
	
	public TransferSyntax getTransferSyntax() {
		if (parent != null)
			return parent.getTransferSyntax();
		return ts;
	}
	
	public VR vrOf(int tag) {
		VRMap vrmap;
		if (TagUtils.isPrivateDataElement(tag)) {
			if (TagUtils.isPrivateCreatorDataElement(tag))
				return VR.LO;
			
			final String privateCreatorID = getPrivateCreator(tag);
			if (privateCreatorID == null)
				return VR.UN;
			
			vrmap = VRMap.getPrivateVRMap(privateCreatorID);
		} else {
			vrmap = VRMap.getVRMap();
		}
		return vrmap.vrOf(tag);
					
	}

	public String getPrivateCreator(int tag) {
		if (!TagUtils.isPrivateDataElement(tag)
				|| TagUtils.isPrivateCreatorDataElement(tag))
			throw new IllegalArgumentException(TagUtils.toString(tag));
		int creatorIDtag = (tag & 0xffff0000) | ((tag >> 8) & 0xff);
		synchronized (privateCreatorCache) {
			if (privateCreatorCache.tag == creatorIDtag)
				return privateCreatorCache.value;
			this.privateCreatorCache.tag = creatorIDtag;
			return privateCreatorCache.value = getString(creatorIDtag);
		}
	}

	private int resolvePrivateTagInternal(int privateTag, String privateCreator, 
			boolean reserve) {
		if (!TagUtils.isPrivateDataElement(privateTag)
				|| TagUtils.isPrivateCreatorDataElement(privateTag))
			throw new IllegalArgumentException(TagUtils.toString(privateTag));
		int gggg0000 = privateTag & 0xffff0000;
		synchronized (privateCreatorCache) {
			if (gggg0000 != (privateCreatorCache.tag & 0xffff0000) 
					|| !privateCreator.equals(privateCreatorCache.value)) {
				int idTag = gggg0000 | 0x10;
				int maxIdTag = gggg0000 | 0xff;
				String id;
				while (!privateCreator.equals(id = getString(idTag))) {
					if (id == null) {
						if (!reserve)
							return -1;
						putString(idTag, VR.LO, id = privateCreator);
						break;
					}
					if (++idTag > maxIdTag)
						throw new IllegalStateException(
								"No free block to reserve in group " 
								+ TagUtils.toString(gggg0000));
				}
				privateCreatorCache.tag = idTag;
				privateCreatorCache.value = id;
			}
			return (privateTag & 0xffff00ff)
					| ((privateCreatorCache.tag & 0xff) << 8);
		}
	}
	
    private int binarySearch(int tag) {
		long ltag = tag & 0xFFFFFFFFL;
		int low = 0;
		int high = attrs.size() - 1;
		while (low <= high) {
		    int mid = (low + high) >> 1;
		    Attribute attr = (Attribute) attrs.get(mid);
			long midTag = attr.tag() & 0xFFFFFFFFL;
		    if (midTag < ltag)
				low = mid + 1;
		    else if (midTag > ltag)
				high = mid - 1;
		    else
				return mid; // key found
		}
		return -(low + 1);  // key not found.
	}

	public boolean contains(int tag) {
		return binarySearch(tag) >= 0;
	}
	
	public boolean containsValue(int tag) {
		Attribute attr = getAttribute(tag);
		return attr != null && !attr.isNull();
	}
	
	public Attribute getAttribute(int tag) {
		int index = binarySearch(tag);
		return (Attribute) (index >= 0 ? attrs.get(index) : null);
	}
	
	public Attribute removeAttribute(int tag) {
		int index = binarySearch(tag);
		if (index < 0) return null;
		if (tag == CHARSET_TAG) {
			charset = null;
		} else if (privateCreatorCache.tag == tag) {
			synchronized (privateCreatorCache) {
				privateCreatorCache.tag = 0;
				privateCreatorCache.value = null;
			}
		}
		return (Attribute) attrs.remove(index);
	}
	
	private int lastTag() {
		if (attrs.isEmpty()) return 0;
		Attribute last = (Attribute) attrs.get(attrs.size() - 1);
		return last.tag();
	}

	protected Attribute putAttribute(Attribute a) {
		final int tag = a.tag();
		if ((lastTag() & 0xffffffffL) < (tag & 0xffffffffL)) {
			attrs.add(a);
		} else {
			int index = binarySearch(tag);
			if (index < 0) {
				attrs.add(-(index + 1), a);
			} else {
				attrs.set(index, a);
			}
		}
		if (tag == TRANSFER_SYNTAX_TAG) {
			ts = TransferSyntax.valueOf(a.getString(null, false));
		} else if (tag == CHARSET_TAG) {
			charset = SpecificCharacterSet.valueOf(a.getStrings(null, false));
		} else if (privateCreatorCache.tag == tag) {
			synchronized (privateCreatorCache) {
				privateCreatorCache.tag = 0;
				privateCreatorCache.value = null;
			}
		}
		return a;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AttributeSet)) {
			return false;
		}
		AttributeSet other = (AttributeSet) o;
		Iterator it = iterator();
		Iterator otherIt = other.iterator();
		while (it.hasNext() && otherIt.hasNext()) {
			if (!it.next().equals(otherIt.next()))
				return false;
		}
		return !it.hasNext() && !otherIt.hasNext();
	}

	public void addAll(AttributeSet other) {
		for (Iterator it = other.iterator(); it.hasNext();) {
			Attribute src = (Attribute) it.next();
			if (src.hasItems()) {
				final int count = src.countItems();
				if (src.vr() == VR.SQ) {
					Attribute dst = putSequence(src.tag(), count);
					for (int i = 0; i < count; i++) {
						dst.addItem(new BasicAttributeSet())
								.addAll(src.getItem(i));
					}
				} else {
					Attribute dst = putFragments(
							src.tag(), src.vr(), src.bigEndian(), count);
					for (int i = 0; i < count; i++) {
						dst.addBytes(src.getBytes(i));
					}
				}
			} else {
				putAttribute(src);
			}
		}
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
		return a == null ? 0 : a.getInt(cache);
	}

	public int[] getInts(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getInts(cache);
	}

	public float getFloat(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getFloat(cache);
	}

	public float[] getFloats(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getFloats(cache);
	}

	public double getDouble(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getDouble(cache);
	}

	public double[] getDoubles(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDoubles(cache);
	}

	public String getString(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getString(getSpecificCharacterSet(), cache);
	}

	public String[] getStrings(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null
				: a.getStrings(getSpecificCharacterSet(), cache);
	}

	private Attribute putAttribute(int tag, VR vr, boolean explicitVR, Object val, Object cache) {
		return putAttribute(new BasicAttribute(tag, vr, explicitVR, val, cache));
	}

	public Attribute putNull(int tag, VR vr) {
		return putAttribute(tag, vr, false, null, null);
	}

	public Attribute putBytes(int tag, VR vr, boolean bigEndian, byte[] val) {
		return putAttribute(tag, vr, bigEndian, val, null);
	}

	public Attribute putItem(int tag, AttributeSet item) {
		Attribute a = putSequence(tag, 1);
		a.addItem(item);
		return a;
	}

	public Attribute putInt(int tag, VR vr, int val) {
		final boolean be = getTransferSyntax().bigEndian();
		Attribute attr = putAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? new Integer(val) : null);
		return attr;
	}

	public Attribute putInts(int tag, VR vr, int[] val) {
		final boolean be = getTransferSyntax().bigEndian();
		Attribute attr = putAttribute(tag, vr, be, vr.toBytes(val, be),						
				cache ? val : null);
		return attr;
	}

	public Attribute putFloat(int tag, VR vr, float val) {
		final boolean be = getTransferSyntax().bigEndian();
		Attribute attr = putAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? new Float(val) : null);
		return attr;
	}

	public Attribute putFloats(int tag, VR vr, float[] val) {
		final boolean be = getTransferSyntax().bigEndian();
		Attribute attr = putAttribute(tag, vr, be, vr.toBytes(val, be),						
				cache ? val : null);
		return attr;
	}

	public Attribute putDouble(int tag, VR vr, double val) {
		final boolean be = getTransferSyntax().bigEndian();
		Attribute attr = putAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? new Double(val) : null);
		return attr;
	}

	public Attribute putDoubles(int tag, VR vr, double[] val) {
		final boolean be = getTransferSyntax().bigEndian();
		Attribute attr = putAttribute(tag, vr, be, vr.toBytes(val, be),						
				cache ? val : null);
		return attr;
	}

	public Attribute putString(int tag, VR vr, String val) {
		Attribute attr = putAttribute(tag, vr, false,
				vr.toBytes(val, getSpecificCharacterSet()),						
				cache ? val : null);
		return attr;
	}

	public Attribute putStrings(int tag, VR vr, String[] val) {
		Attribute attr = putAttribute(tag, vr, false,
				vr.toBytes(val, getSpecificCharacterSet()),						
				cache ? val : null);
		return attr;
	}

	public Attribute putSequence(int tag) {
		return putSequence(tag, 10);
	}

	public Attribute putSequence(int tag, int capacity) {
		return putAttribute(tag, VR.SQ, false, new ArrayList(capacity), null);
	}

	public Attribute putFragments(int tag, VR vr, boolean bigEndian) {
		return putFragments(tag, vr, bigEndian, INIT_FRAGMENT_CAPACITY);
	}

	public Attribute putFragments(int tag, VR vr, boolean bigEndian, int capacity) {
		if (!(vr instanceof VR.Fragment))
			throw new UnsupportedOperationException();
		return putAttribute(tag, vr, bigEndian, new ArrayList(capacity), null);
	}
}
