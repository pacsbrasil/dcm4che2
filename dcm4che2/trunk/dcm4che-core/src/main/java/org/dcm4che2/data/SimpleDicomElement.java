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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Date;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 3, 2005
 *
 */
class SimpleDicomElement extends AbstractDicomElement {

    private static final long serialVersionUID = 4049072757025092152L;
    private static final WeakHashMap shared = new WeakHashMap();
    private static final ThreadLocal cbuf = new ThreadLocal(){
        protected Object initialValue() {
            return new char[64];
        }
    };
    private static final byte[] NULL_VALUE = {};
    private transient byte[] value;
    private transient Object cachedValue;

    public SimpleDicomElement(int tag, VR vr, boolean bigEndian,
            byte[] value, Object cachedValue) {
        super(tag, vr, bigEndian);
        this.value = value == null ? NULL_VALUE : value;
        this.cachedValue = cachedValue;
    }
    
    private void writeObject(ObjectOutputStream s)
    throws IOException {
        s.defaultWriteObject();
        s.writeInt(tag);
        s.writeShort(vr.code());
        s.writeBoolean(bigEndian);
        s.writeInt(value.length);
        if (value.length != 0) {
            s.write(value);
        }
    }

    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        tag = s.readInt();
        vr = VR.valueOf(s.readUnsignedShort());
        bigEndian = s.readBoolean();
        int len = s.readInt();
        if (len != 0) {
            value = new byte[len];
            s.read(value);
        } else {
            value = NULL_VALUE;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SimpleDicomElement)) {
            return false;
        }
        SimpleDicomElement other = (SimpleDicomElement) o;
        return tag == other.tag 
             && vr == other.vr 
             && Arrays.equals(value, other.value);     
    }
    
    public DicomElement share() {
        WeakReference wr = (WeakReference) shared.get(this);
        if (wr != null) {
            DicomElement e = (DicomElement) wr.get();
            if (e != null) {
                return e;
            }
        }
        shared.put(this, new WeakReference(this));
        return this;
    }
    
    protected void appendValue(StringBuffer sb, int maxValLen) {
        vr.promptValue(value, bigEndian, null, (char[]) cbuf.get(),
                maxValLen, sb);        
    }
    
    protected void toggleEndian() {
        vr.toggleEndian(value);
    }

    public final int length() {
        return (value.length + 1) & ~1;
    }

    public final boolean isEmpty() {
        return value.length == 0;
    }
    
    public int vm(SpecificCharacterSet cs) {
        return vr.vm(value, cs);
    }
    
    public byte[] getBytes() {
        return value;
    }

    public int getInt(boolean cache) {
        if (cache && cachedValue instanceof Integer)
            return ((Integer) cachedValue).intValue();
        int val = vr.toInt(value, bigEndian);
        if (cache)
            cachedValue = new Integer(val);
        return val;
    }

    public int[] getInts(boolean cache) {
        if (cache && cachedValue instanceof int[])
            return (int[]) cachedValue;
        int[] val = vr.toInts(value, bigEndian);
        if (cache)
            cachedValue = val;
        return val;
    }

    public float getFloat(boolean cache) {
        if (cache && cachedValue instanceof Float)
            return ((Float) cachedValue).floatValue();
        float val = vr.toFloat(value, bigEndian);
        if (cache)
            cachedValue = new Float(val);
        return val;
    }

    public float[] getFloats(boolean cache) {
        if (cache && cachedValue instanceof float[])
            return (float[]) cachedValue;
        float[] val = vr.toFloats(value, bigEndian);
        if (cache)
            cachedValue = val;
        return val;
    }

    public double getDouble(boolean cache) {
        if (cache && cachedValue instanceof Double)
            return ((Double) cachedValue).doubleValue();
        double val = vr.toDouble(value, bigEndian);
        if (cache)
            cachedValue = new Double(val);
        return val;
    }

    public double[] getDoubles(boolean cache) {
        if (cache && cachedValue instanceof double[])
            return (double[]) cachedValue;
        double[] val = vr.toDoubles(value, bigEndian);
        if (cache)
            cachedValue = val;
        return val;
    }

    public String getString(SpecificCharacterSet cs, boolean cache) {
        if (cache && cachedValue instanceof String)
            return (String) cachedValue;
        String val = vr.toString(value, bigEndian, cs);
        if (cache)
            cachedValue = val;
        return val;
    }

    public String[] getStrings(SpecificCharacterSet cs, boolean cache) {
        if (cache && cachedValue instanceof String[])
            return (String[]) cachedValue;
        String[] val = vr.toStrings(value, bigEndian, cs);
        if (cache)
            cachedValue = val;
        return val;
    }

    public Date getDate(boolean cache) {
        if (cache && cachedValue instanceof Date)
            return (Date) cachedValue;
        Date val = vr.toDate(value);
        if (cache)
            cachedValue = val;
        return val;
    }

    public Date[] getDates(boolean cache) {
        if (cache && cachedValue instanceof Date[])
            return (Date[]) cachedValue;
        Date[] val = vr.toDates(value);
        if (cache)
            cachedValue = val;
        return val;
    }
    
    public DateRange getDateRange(boolean cache) {
        if (cache && cachedValue instanceof Date)
            return (DateRange) cachedValue;
        DateRange val = vr.toDateRange(value);
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
        Pattern val = vr.toPattern(value, bigEndian, cs, ignoreCase);
        if (cache)
            cachedValue = val;
        return val;
    }

    public final boolean hasItems() {
        return false;
    }
    
    public int countItems() {
        return -1;
    }

    public DicomObject getDicomObject() {
        throw new UnsupportedOperationException();
    }

    public DicomObject getDicomObject(int index) {
        throw new UnsupportedOperationException();
    }

    public DicomObject removeDicomObject(int index) {
        throw new UnsupportedOperationException();
    }

    private void updateItemPositions(int index) {
        throw new UnsupportedOperationException();
    }

    public DicomObject addDicomObject(DicomObject item) {
        throw new UnsupportedOperationException();
    }

    public DicomObject addDicomObject(int index, DicomObject item) {
        throw new UnsupportedOperationException();
    }

    public DicomObject setDicomObject(int index, DicomObject item) {
        throw new UnsupportedOperationException();
    }

    public byte[] getBytes(int index) {
        throw new UnsupportedOperationException();
    }

    public byte[] removeBytes(int index) {
        throw new UnsupportedOperationException();
    }

    public byte[] addBytes(byte[] b) {
        throw new UnsupportedOperationException();
    }

    public byte[] addBytes(int index, byte[] b) {
        throw new UnsupportedOperationException();
    }

    public byte[] setBytes(int index, byte[] b) {
        throw new UnsupportedOperationException();
    }

    public DicomElement filterItems(DicomObject filter) {
        throw new UnsupportedOperationException();
    }
    
}
