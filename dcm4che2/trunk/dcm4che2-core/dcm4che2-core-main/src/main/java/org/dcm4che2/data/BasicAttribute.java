/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.dcm4che2.util.TagUtils;

class BasicAttribute implements Attribute {

	private static final long serialVersionUID = 3256439218229556788L;

	private static final WeakHashMap shared = new WeakHashMap();

	private transient int tag;
	
	private transient VR vr;
	
	private transient boolean bigEndian;
	
	private transient Object value;
	
	private transient Object cachedValue;
	
	BasicAttribute(int tag, VR vr, boolean bigEndian, Object value, 
			Object cachedValue) {
		this.tag = tag;
		this.vr = vr;
		this.bigEndian = bigEndian;
		this.value = value;
		this.cachedValue = cachedValue;
	}
	
	private void writeObject(ObjectOutputStream s)
			throws IOException {
		s.defaultWriteObject();
		s.writeInt(tag);
		s.writeShort(vr.code());
		s.writeBoolean(bigEndian);
		if (value == null) {
			s.writeInt(0);
		} else if (value instanceof byte[]) {
			byte[] b = (byte[]) value;
			s.writeInt(b.length);
			s.write(b);
		} else {
			s.writeInt(-1);
			List l = (List) value;
			int size = l.size();
			s.writeInt(size);
			for (int i = 0; i < size; ++i) {
				Object item = l.get(i);
				if (item instanceof AttributeSet) {
					s.writeObject(new AttributeSerializer((AttributeSet) item));
				} else {
					s.writeObject(item);
				}
			}
		}
	}
	
	private void readObject(ObjectInputStream s)
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		tag = s.readInt();
		vr = VR.valueOf(s.readUnsignedShort());
		bigEndian = s.readBoolean();
		int len = s.readInt();
		if (len == -1) {
			int n = s.readInt();
			List l = new ArrayList(n);
			for (int i = 0; i < n; ++i) {
				l.add(s.readObject());
			}
			value = l;
		} else if (len > 0) {
			byte[] b = new byte[len];
			s.read(b);
			value = b;
		}
	}

	public Attribute share() {
		if (value instanceof List) {
			if (vr == VR.SQ) {
				List l = (List) value;
				for (int i = 0, n = l.size(); i < n; ++i) {
					((AttributeSet) l.get(i)).shareAttributes();
				}
			}
			return this;
		}
        WeakReference wr = (WeakReference) shared.get(this);
        if (wr != null) {
			Attribute e = (Attribute) wr.get();
            if (e != null) {
                return e;
            }
        }
		shared.put(this, new WeakReference(this));
        return this;
    }
	
	public final int tag() {
		return tag;
	}
	
	public int hashCode() {
		if (value == null) return tag;
		return tag + value.hashCode();
	}
	
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BasicAttribute)) {
			return false;
		}
		BasicAttribute other = (BasicAttribute) o;
		if (tag != other.tag || vr != other.vr)
			return false;
		if (isNull()) {
			return other.isNull();
		}
		if (other.isNull()) {
			return false;
		}
		if (value instanceof byte[] && other.value instanceof byte[]) {
			return Arrays.equals((byte[]) value, (byte[]) other.value);		
		}
		return value.equals(other.value);
	}

	public final VR vr() {
		return vr;
	}
	
	public String toString() {
		return TagUtils.toString(tag) + " " + vr + " #" + length();
	}	
	
	Attribute fragmentsToSequence(AttributeSet parent)
			throws IOException {
		if (vr != VR.UN)
			throw new IllegalStateException("vr:" + vr);
		List oldval = (List) value;
		List newval = new ArrayList(oldval.size());
		for (int i = 0, n = oldval.size(); i < n; ++i) {
			byte[] b = (byte[]) oldval.get(i);
			InputStream is = new ByteArrayInputStream(b);
			DicomInputStream dis = new DicomInputStream(is, 
					TransferSyntax.ImplicitVRLittleEndian);
			AttributeSet item = new BasicAttributeSet();
			dis.readAttributeSet(item, b.length);
			newval.add(item);
		}
		this.value = newval;
		this.vr = VR.SQ;
		return this;
	}

	public final boolean bigEndian() {
		return bigEndian;
	}

	public Attribute bigEndian(boolean bigEndian) {
		if (this.bigEndian == bigEndian)
			return this;
		vr.toggleEndian(value);
		this.bigEndian = bigEndian;
		return this;
	}

	public final int length() {
		if (value == null)
			return 0;
		if (value instanceof byte[])
			return (((byte[])value).length + 1) & ~1;
		return ((List)value).isEmpty() ? 0 : -1;
	}

	public final boolean isNull() {
		if (value == null)
			return false;
		if (value instanceof byte[])
			return ((byte[])value).length == 0;
		return !((List)value).isEmpty();
	}
	
	public byte[] getBytes() {
		return (byte[]) value;
	}

	public int getInt(boolean cache) {
		if (cache && cachedValue instanceof Integer)
			return ((Integer) cachedValue).intValue();
		int val = vr.toInt((byte[]) value, bigEndian);
		if (cache)
			cachedValue = new Integer(val);
		return val;
	}

	public int[] getInts(boolean cache) {
		if (cache && cachedValue instanceof int[])
			return (int[]) cachedValue;
		int[] val = vr.toInts((byte[]) value, bigEndian);
		if (cache)
			cachedValue = val;
		return val;
	}

	public float getFloat(boolean cache) {
		if (cache && cachedValue instanceof Float)
			return ((Float) cachedValue).floatValue();
		float val = vr.toFloat((byte[]) value, bigEndian);
		if (cache)
			cachedValue = new Float(val);
		return val;
	}

	public float[] getFloats(boolean cache) {
		if (cache && cachedValue instanceof float[])
			return (float[]) cachedValue;
		float[] val = vr.toFloats((byte[]) value, bigEndian);
		if (cache)
			cachedValue = val;
		return val;
	}

	public double getDouble(boolean cache) {
		if (cache && cachedValue instanceof Double)
			return ((Double) cachedValue).doubleValue();
		double val = vr.toDouble((byte[]) value, bigEndian);
		if (cache)
			cachedValue = new Double(val);
		return val;
	}

	public double[] getDoubles(boolean cache) {
		if (cache && cachedValue instanceof double[])
			return (double[]) cachedValue;
		double[] val = vr.toDoubles((byte[]) value, bigEndian);
		if (cache)
			cachedValue = val;
		return val;
	}

	public String getString(SpecificCharacterSet cs, boolean cache) {
		if (cache && cachedValue instanceof String)
			return (String) cachedValue;
		String val = vr.toString((byte[]) value, bigEndian, cs);
		if (cache)
			cachedValue = val;
		return val;
	}

	public String[] getStrings(SpecificCharacterSet cs, boolean cache) {
		if (cache && cachedValue instanceof String[])
			return (String[]) cachedValue;
		String[] val = vr.toStrings((byte[]) value, bigEndian, cs);
		if (cache)
			cachedValue = val;
		return val;
	}

	public Date getDate(boolean cache) {
		if (cache && cachedValue instanceof Date)
			return (Date) cachedValue;
		Date val = vr.toDate((byte[]) value);
		if (cache)
			cachedValue = val;
		return val;
	}

	public Date[] getDates(boolean cache) {
		if (cache && cachedValue instanceof Date[])
			return (Date[]) cachedValue;
		Date[] val = vr.toDates((byte[]) value);
		if (cache)
			cachedValue = val;
		return val;
	}
	
	public DateRange getDateRange(boolean cache) {
		if (cache && cachedValue instanceof Date)
			return (DateRange) cachedValue;
		DateRange val = vr.toDateRange((byte[]) value);
		if (cache)
			cachedValue = val;
		return val;
	}

	public Pattern getPattern(SpecificCharacterSet cs, boolean ignoreCase,
			boolean cache) {
		if (cache && cachedValue instanceof Pattern) {
			Pattern t = (Pattern) cachedValue;
			if (t.flags() == (ignoreCase 
					? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
					: 0))
				return t;
		}
		Pattern val = vr.toPattern((byte[]) value, bigEndian, cs, ignoreCase);
		if (cache)
			cachedValue = val;
		return val;
	}

	public final boolean hasItems() {
		return value instanceof List;
	}
	
	public int countItems() {
		List l = (List) value;
		return l.size();
	}

	public AttributeSet getItem() {
		List l = (List) value;
		return (AttributeSet) (!l.isEmpty() ? l.get(0) : null);
	}

	public AttributeSet getItem(int index) {
		return (AttributeSet) get(index);
	}

	public AttributeSet removeItem(int index) {
		return (AttributeSet) remove(index);
	}

	public AttributeSet addItem(AttributeSet item) {
		if (vr != VR.SQ)
			throw new UnsupportedOperationException();
		add(item);
		return item;
	}

	public AttributeSet addItem(int index, AttributeSet item) {
		if (vr != VR.SQ)
			throw new UnsupportedOperationException();
		add(index, item);
		return item;
	}

	public AttributeSet setItem(int index, AttributeSet item) {
		if (vr != VR.SQ)
			throw new UnsupportedOperationException();
		set(index, item);
		return item;
	}

	public byte[] getBytes(int index) {
		return (byte[]) get(index);
	}

	public byte[] removeBytes(int index) {
		return (byte[]) remove(index);
	}

	public byte[] addBytes(byte[] b) {
		if (vr == VR.SQ)
			throw new UnsupportedOperationException();
		add(b);
		return b;
	}

	public byte[] addBytes(int index, byte[] b) {
		if (vr == VR.SQ)
			throw new UnsupportedOperationException();
		add(index, b);
		return b;
	}

	public byte[] setBytes(int index, byte[] b) {
		if (vr == VR.SQ)
			throw new UnsupportedOperationException();
		set(index, b);
		return b;
	}

	private Object get(int index) {
		List l = (List) value;
		return l.get(index);
	}

	private Object remove(int index) {
		List l = (List) value;
		return l.remove(index);
	}

	private void add(Object item) {
		if (item == null)
			throw new NullPointerException();
		List l = (List) value;
		l.add(item);
	}

	private void add(int index, Object item) {
		if (item == null)
			throw new NullPointerException();
		List l = (List) value;
		l.add(index, item);
	}

	private void set(int index, Object item) {
		if (item == null)
			throw new NullPointerException();
		List l = (List) value;
		l.set(index, item);
	}

	public Attribute filterItems(AttributeSet filter) {
		if (filter == null)
			return this;
		int count = countItems();
		List items = new ArrayList(count);
		for (int i = 0; i < count; i++) {
			items.add(getItem(i).subSet(filter));
		}
		return new BasicAttribute(tag, vr, bigEndian, items, null);
	}

}
