/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since March 2002
 * @version $Revision$ $Date$
 */
class DcmElementImpl implements DcmElement {
    static final Logger log = Logger.getLogger(DcmElementImpl.class);
    static final TagDictionary DICT = 
            DictionaryFactory.getInstance().getDefaultTagDictionary();

    static final byte[] BYTE0 = {};
    static final ByteBuffer EMPTY_VALUE =
            ByteBuffer.wrap(BYTE0).order(ByteOrder.LITTLE_ENDIAN);
    
    int tag;
    long streamPos = -1L;

    /** Creates a new instance of ElementImpl */
    public DcmElementImpl(int tag) {
        this.tag = tag;
    }

    public DcmElement intern() {
        return this;
    }
       
    public final int tag() {
        return tag;
    }
    
    public int vr() {
        return VRs.NONE;
    }

    public int vm() {
        return 0;
    }
    
    public boolean isEmpty() {
        return vm() == 0;
    }

    public int length() {
        return -1;
    }

    public final DcmElement setStreamPosition(long streamPos) {
        this.streamPos = streamPos;
        return this;
    }        

    public final long getStreamPosition() {
        return streamPos;
    }
    
    
    public int hashCode() {
        return tag;
    }

    public String toString() {
        return toString(tag, vr(), vm(), length(),
            StringUtils.promptValue(vr(), getByteBuffer(), 64));
    }
    
    static String toString(int tag, int vr, int vm, int len, String val) {
        return DICT.toString(tag) + "," + VRs.toString(vr)
                + ",*" + vm + ",#" + len + ",[" + val + "]" ;
    }
    
    boolean match(DcmElement key, boolean ignorePNCase, Charset keyCS, Charset dsCS) {
        return key == null || (key.tag() == tag && key.vr() == vr()
                && (isEmpty() || key.isEmpty()
                || matchValue(key, ignorePNCase, keyCS, dsCS)));
    }
    
    protected boolean matchValue(DcmElement key, boolean ignorePNCase,
            Charset keyCS, Charset dsCS) {
        throw new UnsupportedOperationException("" + this);
    }
        
    public ByteBuffer getByteBuffer() {
        throw new UnsupportedOperationException("" + this);
    }

    public ByteBuffer getByteBuffer(ByteOrder byteOrder) {
        throw new UnsupportedOperationException("" + this);
    }

    public boolean hasDataFragments() {
       return false;
    }
    
    public ByteBuffer getDataFragment(int index) {
        throw new UnsupportedOperationException("" + this);
    }

    public ByteBuffer getDataFragment(int index, ByteOrder byteOrder) {
        throw new UnsupportedOperationException("" + this);
    }

    public int getDataFragmentLength(int index) {
        throw new UnsupportedOperationException("" + this);
    }

    public final PersonName getPersonName(Charset cs)  throws DcmValueException {
        return getPersonName(0, cs);
    }
    
    public PersonName getPersonName(int index, Charset cs) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public final String getString(Charset cs)  throws DcmValueException {
        return getString(0, cs);
    }
    
    public String getString(int index, Charset cs) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public String[] getStrings(Charset cs) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public final String getBoundedString(int maxLen, Charset cs) 
    throws DcmValueException {
        return getBoundedString(maxLen, 0, cs);
    }
    
    public String getBoundedString(int maxLen, int index, Charset cs)
    throws DcmValueException {
        return getString(index, cs);
    }
 
    public String[] getBoundedStrings(int maxLen, Charset cs)
    throws DcmValueException {
        return getStrings(cs);
    }
 
    public final int getInt() throws DcmValueException {
        return getInt(0);
    }
    
    public int getInt(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public int[] getInts() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public final int getTag() throws DcmValueException {
        return getTag(0);
    }
    
    public int getTag(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public int[] getTags() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public final float getFloat() throws DcmValueException {
        return getFloat(0);
    }
    
    public float getFloat(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public float[] getFloats() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
 
    public final double getDouble() throws DcmValueException {
        return getDouble(0);
    }
    
    public double getDouble(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public double[] getDoubles() {
        throw new UnsupportedOperationException("" + this);
    }
    
    public final Date getDate() throws DcmValueException {
        return getDate(0);
    }
    
    public Date getDate(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public Date[] getDates() throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }
    
    public final Date[] getDateRange() throws DcmValueException {
        return getDateRange(0);
    }
    
    public Date[] getDateRange(int index) throws DcmValueException {
        throw new UnsupportedOperationException("" + this);
    }

    public void addDataFragment(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException("" + this);
    }
    
    public boolean hasItems() {
       return false;
    }

    public Dataset addNewItem() {
        throw new UnsupportedOperationException("" + this);
    }

    public void addItem(Dataset item) {
        throw new UnsupportedOperationException("" + this);
    }

    public Dataset getItem() {
        return getItem(0);
    }
    
    public Dataset getItem(int index) {
        throw new UnsupportedOperationException("" + this);
    }

    static ByteOrder swap(ByteOrder from) {
        return from == ByteOrder.LITTLE_ENDIAN ? ByteOrder.BIG_ENDIAN
                                               : ByteOrder.LITTLE_ENDIAN;
    }
    
    static void swapWords(ByteBuffer bb) {
        if ((bb.limit() & 1) != 0)
            throw new IllegalArgumentException("illegal value length: " + bb);

        final ByteOrder from = bb.order();
        final ByteOrder to = swap(from);
        short tmp;
        for (int i = 0, n = bb.limit(); i < n; i+=2) {
            tmp = bb.getShort(i);
            bb.order(to).putShort(i, tmp).order(from);
        }
        bb.order(to);
    }
    
    static void swapInts(ByteBuffer bb) {
        if ((bb.limit() & 3) != 0)
            throw new IllegalArgumentException("illegal value length " + bb);

        final ByteOrder from = bb.order();
        final ByteOrder to = swap(from);
        int tmp;
        for (int i = 0, n = bb.limit(); i < n; i+=4) {
            tmp = bb.getInt(i);
            bb.order(to).putInt(i, tmp).order(from);
        }
        bb.order(to);
    }
    
    static void swapLongs(ByteBuffer bb) {
        if ((bb.limit() & 7) != 0)
            throw new IllegalArgumentException("illegal value length " + bb);

        final ByteOrder from = bb.order();
        final ByteOrder to = swap(from);
        long tmp;
        for (int i = 0, n = bb.limit(); i < n; i+=8) {
            tmp = bb.getLong(i);
            bb.order(to).putLong(i, tmp).order(from);
        }
        bb.order(to);
    }
}
