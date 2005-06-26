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
import org.dcm4che2.util.IntHashtable;
import org.dcm4che2.util.TagUtils;

public class BasicAttributeSet extends AbstractAttributeSet {

	private static final long serialVersionUID = 1L;

	private static final int INIT_FRAGMENT_CAPACITY = 2;

	private static final int INIT_SEQUENCE_CAPACITY = 10;

	private static final Logger log = Logger.getLogger(BasicAttributeSet.class);

	private transient final IntHashtable table;

	private transient AttributeSet parent;

	private transient TransferSyntax ts = TransferSyntax.ImplicitVRLittleEndian;

	private transient SpecificCharacterSet charset = null;

	private transient long itemOffset = -1L;

	private transient boolean cache = false;

	public BasicAttributeSet() {
		this(10);
	}

	public BasicAttributeSet(int capacity) {
		this.table = new IntHashtable(capacity);
	}
	
	public void clear() {
		table.clear();
		charset = null;
	}

	public final boolean isCacheAttributeValues() {
		return cache;
	}

	public final void setCacheAttributeValues(final boolean cache) {
		this.cache = cache;
		accept(new Visitor(){
			public boolean visit(Attribute attr) {
				if (attr.vr() == VR.SQ && attr.hasItems()) {
					for (int i = 0, n = attr.countItems(); i < n; ++i) {
						attr.getItem(i).setCacheAttributeValues(cache);
					}
				}
				return true;
			}});
	}
	
	public int resolvePrivateTag(int privateTag, String privateCreator) {
		return resolvePrivateTagInternal(privateTag, privateCreator, false);
	}

	public int reservePrivateTag(int privateTag, String privateCreatorID) {
		return resolvePrivateTagInternal(privateTag, privateCreatorID, true);
	}

	public void shareAttributes() {
		table.accept(new IntHashtable.Visitor() {
			public boolean visit(int key, Object value) {
				table.put(key, ((Attribute) value).share());
				return true;
			}
		});
	}

	public Iterator iterator() {
		return iterator(0, 0xffffffff);
	}

	public Iterator iterator(int fromTag, int toTag) {
		if ((fromTag & 0xffffffffL) > (toTag & 0xffffffffL)) {
			throw new IllegalArgumentException("fromTag:"
					+ TagUtils.toString(fromTag) + " > toTag:"
					+ TagUtils.toString(toTag));
		}
		return table.iterator(fromTag, toTag);
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
		return getAndCacheString(creatorIDtag);
	}

	private String getAndCacheString(int creatorIDtag) {
		Attribute a = getAttribute(creatorIDtag);
		return a == null ? null : a.getString(getSpecificCharacterSet(), true);
	}

	private int resolvePrivateTagInternal(int privateTag,
			String privateCreator, boolean reserve) {
		if (!TagUtils.isPrivateDataElement(privateTag)
				|| TagUtils.isPrivateCreatorDataElement(privateTag))
			throw new IllegalArgumentException(TagUtils.toString(privateTag));
		int gggg0000 = privateTag & 0xffff0000;
		int idTag = gggg0000 | 0x10;
		int maxIdTag = gggg0000 | 0xff;
		String id;
		while (!privateCreator.equals(id = getAndCacheString(idTag))) {
			if (id == null) {
				if (!reserve)
					return -1;
				add(new BasicAttribute(idTag, VR.LO, false, VR.LO.toBytes(
						privateCreator, false, getSpecificCharacterSet()), 
						privateCreator));
				break;
			}
			if (++idTag > maxIdTag)
				throw new IllegalStateException(
						"No free block to reserve in group "
								+ TagUtils.toString(gggg0000));
		}
		return (privateTag & 0xffff00ff) | ((idTag & 0xff) << 8);
	}

	public boolean isEmpty() {
		return table.isEmpty();
	}
	
	public int size() {
		return table.size();
	}
	
	public boolean contains(int tag) {
		return table.get(tag) != null;
	}

	public boolean containsValue(int tag) {
		Attribute attr = getAttribute(tag);
		return attr != null && !attr.isNull();
	}

	public Attribute getAttribute(int tag) {
		return (Attribute) table.get(tag);
	}

	public Attribute removeAttribute(int tag) {
		Attribute attr = (Attribute) table.remove(tag);
		if (attr != null) {
			if (tag == Tag.SpecificCharacterSet) {
				charset = null;
			}
		}
		return attr;
	}

	Attribute add(Attribute a) {
		final int tag = a.tag();
		if ((tag & 0x0000ffff) == 0) {
			// do not include group length elements
			return a;
		}
		table.put(tag, a);
		if (tag == Tag.TransferSyntaxUID) {
			ts = TransferSyntax.valueOf(a.getString(null, false));
		} else if (tag == Tag.SpecificCharacterSet) {
			charset = SpecificCharacterSet.valueOf(a.getStrings(null, false));
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

	public boolean accept(final Visitor visitor) {
		return table.accept(new IntHashtable.Visitor() {
			public boolean visit(int key, Object value) {
				return visitor.visit((Attribute) value);
			}
		});
	}

	public void addAttribute(Attribute a) {
		if (a.hasItems()) {
			final int n = a.countItems();
			Attribute t;
			if (a.vr() == VR.SQ) {
				t = putSequence(a.tag(), n);
				for (int i = 0; i < n; i++) {
					BasicAttributeSet item = new BasicAttributeSet(n);
					item.setParent(this);
					a.getItem(i).copyTo(item);
					t.addItem(item);
				}
			} else {
				t = putFragments(a.tag(), a.vr(), a.bigEndian(), n);
				for (int i = 0; i < n; i++) {
					t.addBytes(a.getBytes(i));
				}
			}
			a = t;
		}
		add(a);
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
		return a == null ? null : a
				.getStrings(getSpecificCharacterSet(), cache);
	}

	public Attribute putNull(int tag, VR vr) {
		return add(new BasicAttribute(tag, vr, false, null, null));
	}

	public Attribute putBytes(int tag, VR vr, boolean bigEndian, byte[] val) {
		return add(new BasicAttribute(tag, vr, bigEndian, val, null));
	}

	public Attribute putItem(int tag, AttributeSet item) {
		Attribute a = putSequence(tag, 1);
		a.addItem(item);
		return a;
	}

	public Attribute putInt(int tag, VR vr, int val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? new Integer(val) : null));
	}

	public Attribute putInts(int tag, VR vr, int[] val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? val : null));
	}

	public Attribute putFloat(int tag, VR vr, float val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? new Float(val) : null));
	}

	public Attribute putFloats(int tag, VR vr, float[] val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? val : null));
	}

	public Attribute putDouble(int tag, VR vr, double val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? new Double(val) : null));
	}

	public Attribute putDoubles(int tag, VR vr, double[] val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val, be),
				cache ? val : null));
	}

	public Attribute putString(int tag, VR vr, String val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val,
				be, getSpecificCharacterSet()), cache ? val : null));
	}

	public Attribute putStrings(int tag, VR vr, String[] val) {
		final boolean be = getTransferSyntax().bigEndian();
		return add(new BasicAttribute(tag, vr, be, vr.toBytes(val,
				be, getSpecificCharacterSet()), cache ? val : null));
	}

	public Attribute putSequence(int tag) {
		return putSequence(tag, 10);
	}

	public Attribute putSequence(int tag, int capacity) {
		return add(new BasicAttribute(tag, VR.SQ, false,
				new ArrayList(capacity), null));
	}

	public Attribute putFragments(int tag, VR vr, boolean bigEndian) {
		return putFragments(tag, vr, bigEndian, INIT_FRAGMENT_CAPACITY);
	}

	public Attribute putFragments(int tag, VR vr, boolean bigEndian,
			int capacity) {
		if (!(vr instanceof VR.Fragment))
			throw new UnsupportedOperationException();
		return add(new BasicAttribute(tag, vr, bigEndian, new ArrayList(
				capacity), null));
	}
}
