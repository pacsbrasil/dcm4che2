/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

/*$Id$*/

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.VRMap;
import org.dcm4che.dict.VRs;

import org.dcm4cheri.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import javax.imageio.stream.ImageOutputStream;

import org.xml.sax.helpers.DefaultHandler;
import org.apache.log4j.Logger;

abstract class DcmObjectImpl implements DcmObject {
    static UIDDictionary DICT =
    DictionaryFactory.getInstance().getDefaultUIDDictionary();
    
    protected static final Logger log =
    Logger.getLogger("dcm4che.data.DcmObject");
    
    protected ArrayList list = new ArrayList();
    private static final int MIN_TRUNCATE_STRING_LEN = 16;
    
    public DcmHandler getDcmHandler() {
        return new DcmObjectHandlerImpl(this);
    }
    
    public DefaultHandler getSAXHandler() {
        return new SAXHandlerAdapter(getDcmHandler());
    }
    
    public String getPrivateCreatorID() {
        return null;
    }
    
    public void setPrivateCreatorID(String privateCreatorID) {
        throw new UnsupportedOperationException();
    }
    
    public Charset getCharset() {
        return null;
    }
    
    public int size() {
        return list.size();
    }
    
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    public void clear() {
        list.clear();
    }
    
    public boolean contains(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return false;
            }
            if (tag == 0) {
                return false;
            }
        }
        return Collections.binarySearch(list, new DcmElementImpl(tag)) >= 0;
    }
    
    public int vm(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return -1;
            }
            if (tag == 0) {
                return -1;
            }
        }
        int index = Collections.binarySearch(list, new DcmElementImpl(tag));
        return index >= 0 ? ((DcmElement)list.get(index)).vm() : -1;
    }
    
    private int adjustPrivateTag(int tag, boolean create)
    throws DcmValueException {
        String creatorID = getPrivateCreatorID();
        // no adjustments, if creatorID not set
        if (creatorID == null) {
            return tag;
        }
        int gr = tag & 0xffff0000;
        int el = 0x10;
        int index = Collections.binarySearch(list,
        new DcmElementImpl(gr | el));
        if (index >= 0) {
            DcmElement elm = (DcmElement)list.get(index);
            while (++index < list.size()) {
                if (creatorID.equals(elm.getString(getCharset()))) {
                    return gr | (el << 8) | (tag & 0xff);
                }
                elm = (DcmElement)list.get(index);
                if (elm.tag() != (gr | ++el)) {
                    break;
                }
            }
        }
        if (!create) {
            return 0;
        }
        doPut(StringElement.createLO(gr | el, creatorID, getCharset()));
        return gr | (el << 8) | (tag & 0xff);
    }
    
    public DcmElement get(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return null;
            }
            if (tag == 0) {
                return null;
            }
        }
        int index = Collections.binarySearch(list, new DcmElementImpl(tag));
        return index >= 0 ? (DcmElement)list.get(index) : null;
    }
    
    public DcmElement remove(int tag) {
        int index = Collections.binarySearch(list, new DcmElementImpl(tag));
        return index >= 0 ? (DcmElement)list.remove(index) : null;
    }
    
    public ByteBuffer getByteBuffer(int tag) {
        DcmElement e = get(tag);
        return e != null ? e.getByteBuffer() : null;
    }
    
    public String getString(int tag, String defVal) throws DcmValueException {
        return getString(tag, 0, defVal);
    }
    
    public String getString(int tag) throws DcmValueException {
        return getString(tag, 0, null);
    }
    
    public String getString(int tag, int index)
    throws DcmValueException {
        return getString(tag, index, null);
    }
    
    public String getString(int tag, int index, String defVal)
    throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return defVal;
        
        return e.getString(index, getCharset());
    }
    
    public String[] getStrings(int tag) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null)
            return null;
        
        return e.getStrings(getCharset());
    }
    
    public String getBoundedString(int maxLen, int tag, String defVal)
    throws DcmValueException {
        return getBoundedString(maxLen, tag, 0, defVal);
    }
    
    public String getBoundedString(int maxLen, int tag)
    throws DcmValueException {
        return getBoundedString(maxLen, tag, 0, null);
    }
    
    public String getBoundedString(int maxLen, int tag, int index)
    throws DcmValueException {
        return getBoundedString(maxLen, tag, index, null);
    }
    
    public String getBoundedString(int maxLen, int tag, int index, String defVal)
    throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return defVal;
        
        return e.getBoundedString(maxLen, index, getCharset());
    }
    
    public String[] getBoundedStrings(int maxLen, int tag)
    throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null)
            return null;
        
        return e.getBoundedStrings(maxLen, getCharset());
    }
    
    public Integer getInteger(int tag) throws DcmValueException {
        return getInteger(tag, 0);
    }
    
    public Integer getInteger(int tag, int index) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return null;
        
        return new Integer(e.getInt(index));
    }
    
    public int getInt(int tag, int defVal) throws DcmValueException {
        return getInt(tag, 0, defVal);
    }
    
    public int getInt(int tag, int index, int defVal)
    throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return defVal;
        
        return e.getInt(index);
    }
    
    public int[] getInts(int tag) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null)
            return null;
        
        return e.getInts();
    }
    
    public float getFloat(int tag, float defVal) throws DcmValueException {
        return getFloat(tag, 0, defVal);
    }
    
    public float getFloat(int tag, int index, float defVal)
    throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return defVal;
        
        return e.getFloat(index);
    }
    
    public float[] getFloats(int tag) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null)
            return null;
        
        return e.getFloats();
    }
    
    public double getDouble(int tag, double defVal) throws DcmValueException {
        return getDouble(tag, 0, defVal);
    }
    
    public double getDouble(int tag, int index, double defVal)
    throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return defVal;
        
        return e.getDouble(index);
    }
    
    public double[] getDoubles(int tag) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null)
            return null;
        
        return e.getDoubles();
    }
    
    public Date getDate(int tag) throws DcmValueException {
        return getDate(tag, 0);
    }
    
    public Date getDate(int tag, int index) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return null;
        
        return e.getDate(index);
    }
    
    public Date[] getDateRange(int tag) throws DcmValueException {
        return getDateRange(tag, 0);
    }
    
    public Date[] getDateRange(int tag, int index) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return null;
        
        return e.getDateRange(index);
    }
    
    public Date[] getDates(int tag) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null)
            return null;
        
        return e.getDates();
    }
    
    public Date getDateTime(int dateTag, int timeTag)
    throws DcmValueException {
        DcmElement date = get(dateTag);
        if (date == null || date.isEmpty())
            return null;
        
        DcmElement time = get(timeTag);
        if (time == null || time.isEmpty())
            return date.getDate();
        
        return new Date(date.getDate().getTime() + time.getDate().getTime());
    }
    
    public Date[] getDateTimeRange(int dateTag, int timeTag)
    throws DcmValueException {
        DcmElement date = get(dateTag);
        if (date == null || date.isEmpty())
            return null;
        
        Date[] dateRange = date.getDateRange();
        DcmElement time = get(timeTag);
        if (time == null || time.isEmpty())
            return dateRange;
        
        Date[] timeRange = time.getDateRange();
        return new Date[] {
            new Date(dateRange[0].getTime() + timeRange[0].getTime()),
            new Date(dateRange[1].getTime() + timeRange[1].getTime())
        };
    }
    
    public Dataset getItem(int tag) {
        return getItem(tag, 0);
    }
    
    public Dataset getItem(int tag, int index) {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return null;
        
        return e.getItem(index);
    }
    
    protected DcmElement put(DcmElement newElem) {
        if (log.isDebugEnabled()) {
            log.debug("put " + newElem);
        }
        if ((newElem.tag() & 0xffff) == 0)
            return newElem;
        
        if (Tags.isPrivate(newElem.tag())) {
            try {
                ((DcmElementImpl)newElem).tag =
                adjustPrivateTag(newElem.tag(), true);
            } catch (DcmValueException e) {
                log.warn("Could not access creator ID - ignore " + newElem, e);
                return newElem;
            }
        }
        return doPut(newElem);
    }
    
    private DcmElement doPut(DcmElement newElem) {
        final int size = list.size();
        if (size == 0 || newElem.compareTo(list.get(size-1)) > 0) {
            list.add(newElem);
        } else {
            int index = Collections.binarySearch(list, newElem);
            if (index >= 0) {
                list.set(index, newElem);
            } else {
                list.add(-(index+1),newElem);
            }
        }
        return newElem;
    }
    
    public DcmElement putAE(int tag) {
        return put(StringElement.createAE(tag));
    }
    
    public DcmElement putAE(int tag, String value) {
        return put(value != null
        ? StringElement.createAE(tag, value)
        : StringElement.createAE(tag));
    }
    
    public DcmElement putAE(int tag, String[] values) {
        return put(StringElement.createAE(tag, values));
    }
    
    public DcmElement putAS(int tag) {
        return put(StringElement.createAS(tag));
    }
    
    public DcmElement putAS(int tag, String value) {
        return put(value != null
        ? StringElement.createAS(tag, value)
        : StringElement.createAS(tag));
    }
    
    public DcmElement putAS(int tag, String[] values) {
        return put(StringElement.createAS(tag, values));
    }
    
    public DcmElement putAT(int tag) {
        return put(ValueElement.createAT(tag));
    }
    
    public DcmElement putAT(int tag, int value) {
        return put(ValueElement.createAT(tag,value));
    }
    
    public DcmElement putAT(int tag, int[] values) {
        return put(ValueElement.createAT(tag,values));
    }
    
    public DcmElement putAT(int tag, String value) {
        return putAT(tag, Integer.parseInt(value,16));
    }
    
    public DcmElement putAT(int tag, String[] values) {
        int[] a = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            a[i] = Integer.parseInt(values[i],16);
        }
        return putAT(tag, a);
    }
    
    public DcmElement putCS(int tag) {
        return put(StringElement.createCS(tag));
    }
    
    public DcmElement putCS(int tag, String value) {
        return put(value != null
        ? StringElement.createCS(tag, value)
        : StringElement.createCS(tag));
    }
    
    public DcmElement putCS(int tag, String[] values) {
        return put(StringElement.createCS(tag, values));
    }
    
    public DcmElement putDA(int tag) {
        return put(StringElement.createDA(tag));
    }
    
    public DcmElement putDA(int tag, Date value) {
        return put(value != null
        ? StringElement.createDA(tag, value)
        : StringElement.createDA(tag));
    }
    
    public DcmElement putDA(int tag, Date[] values) {
        return put(StringElement.createDA(tag, values));
    }
    
    public DcmElement putDA(int tag, Date from, Date to) {
        return put(StringElement.createDA(tag, from, to));
    }
    
    public DcmElement putDA(int tag, String value) {
        return put(value != null
        ? StringElement.createDA(tag, value)
        : StringElement.createDA(tag));
    }
    
    public DcmElement putDA(int tag, String[] values) {
        return put(StringElement.createDA(tag, values));
    }
    
    public DcmElement putDS(int tag) {
        return put(StringElement.createDS(tag));
    }
    
    public DcmElement putDS(int tag, float value) {
        return put(StringElement.createDS(tag, value));
    }
    
    public DcmElement putDS(int tag, float[] values) {
        return put(StringElement.createDS(tag, values));
    }
    
    public DcmElement putDS(int tag, String value) {
        return put(value != null
        ? StringElement.createDS(tag, value)
        : StringElement.createDS(tag));
    }
    
    public DcmElement putDS(int tag, String[] values) {
        return put(StringElement.createDS(tag, values));
    }
    
    public DcmElement putDT(int tag) {
        return put(StringElement.createDT(tag));
    }
    
    public DcmElement putDT(int tag, Date value) {
        return put(value != null
        ? StringElement.createDT(tag, value)
        : StringElement.createDT(tag));
    }
    
    public DcmElement putDT(int tag, Date[] values) {
        return put(StringElement.createDT(tag, values));
    }
    
    public DcmElement putDT(int tag, Date from, Date to) {
        return put(StringElement.createDT(tag, from, to));
    }
    
    public DcmElement putDT(int tag, String value) {
        return put(value != null
        ? StringElement.createDT(tag, value)
        : StringElement.createDT(tag));
    }
    
    public DcmElement putDT(int tag, String[] values) {
        return put(StringElement.createDT(tag, values));
    }
    
    public DcmElement putFL(int tag) {
        return put(ValueElement.createFL(tag));
    }
    
    public DcmElement putFL(int tag, float value) {
        return put(ValueElement.createFL(tag, value));
    }
    
    public DcmElement putFL(int tag, float[] values) {
        return put(ValueElement.createFL(tag, values));
    }
    
    public DcmElement putFL(int tag, String value) {
        return put(value != null
        ? ValueElement.createFL(tag, Float.parseFloat(value))
        : ValueElement.createFL(tag));
    }
    
    public DcmElement putFL(int tag, String[] values) {
        return put(ValueElement.createFL(tag, StringUtils.parseFloats(values)));
    }
    
    public DcmElement putFD(int tag) {
        return put(ValueElement.createFD(tag));
    }
    
    public DcmElement putFD(int tag, double value) {
        return put(ValueElement.createFD(tag, value));
    }
    
    public DcmElement putFD(int tag, double[] values) {
        return put(ValueElement.createFD(tag, values));
    }
    
    public DcmElement putFD(int tag, String value) {
        return put(value != null
        ? ValueElement.createFD(tag, Double.parseDouble(value))
        : ValueElement.createFD(tag));
    }
    
    public DcmElement putFD(int tag, String[] values) {
        return put(ValueElement.createFD(tag, StringUtils.parseDoubles(values)));
    }
    
    public DcmElement putIS(int tag) {
        return put(StringElement.createIS(tag));
    }
    
    public DcmElement putIS(int tag, int value) {
        return put(StringElement.createIS(tag, value));
    }
    
    public DcmElement putIS(int tag, int[] values) {
        return put(StringElement.createIS(tag, values));
    }
    
    public DcmElement putIS(int tag, String value) {
        return put(value != null
        ? StringElement.createIS(tag, value)
        : StringElement.createIS(tag));
    }
    
    public DcmElement putIS(int tag, String[] values) {
        return put(StringElement.createIS(tag, values));
    }
    
    public DcmElement putLO(int tag) {
        return put(StringElement.createLO(tag));
    }
    
    public DcmElement putLO(int tag, String value) {
        return put(value != null
        ? StringElement.createLO(tag, value, getCharset())
        : StringElement.createLO(tag));
    }
    
    public DcmElement putLO(int tag, String[] values) {
        return put(StringElement.createLO(tag, values, getCharset()));
    }
    
    public DcmElement putLT(int tag) {
        return put(StringElement.createLT(tag));
    }
    
    public DcmElement putLT(int tag, String value) {
        return put(value != null
        ? StringElement.createLT(tag, value, getCharset())
        : StringElement.createLT(tag));
    }
    
    public DcmElement putLT(int tag, String[] values) {
        return put(StringElement.createLT(tag, values, getCharset()));
    }
    
    public DcmElement putOB(int tag) {
        return put(ValueElement.createOB(tag));
    }
    
    public DcmElement putOB(int tag, byte[] value) {
        return put(ValueElement.createOB(tag, value));
    }
    
    public DcmElement putOB(int tag, ByteBuffer value) {
        return put(ValueElement.createOB(tag, value));
    }
    
    public DcmElement putOBsq(int tag) {
        return put(FragmentElement.createOB(tag));
    }
    
    public DcmElement putOF(int tag) {
        return put(ValueElement.createOF(tag));
    }
    
    public DcmElement putOF(int tag, float[] value) {
        return put(ValueElement.createOF(tag, value));
    }
    
    public DcmElement putOF(int tag, ByteBuffer value) {
        return put(ValueElement.createOF(tag, value));
    }
    
    public DcmElement putOFsq(int tag) {
        return put(FragmentElement.createOF(tag));
    }
    
    public DcmElement putOW(int tag) {
        return put(ValueElement.createOW(tag));
    }
    
    public DcmElement putOW(int tag, short[] value) {
        return put(ValueElement.createOW(tag, value));
    }
    
    public DcmElement putOW(int tag, ByteBuffer value) {
        return put(ValueElement.createOW(tag, value));
    }
    
    public DcmElement putOWsq(int tag) {
        return put(FragmentElement.createOW(tag));
    }
    
    public DcmElement putPN(int tag) {
        return put(StringElement.createSH(tag));
    }
    
    public DcmElement putPN(int tag, PersonName value) {
        return put(value != null
        ? StringElement.createPN(tag, value, getCharset())
        : StringElement.createPN(tag));
    }
    
    public DcmElement putPN(int tag, PersonName[] values) {
        return put(StringElement.createPN(tag, values, getCharset()));
    }
    
    public DcmElement putPN(int tag, String value) {
        return put(value != null
        ? StringElement.createPN(tag, new PersonNameImpl(value), getCharset())
        : StringElement.createPN(tag));
    }
    
    public DcmElement putPN(int tag, String[] values) {
        PersonName[] a = new PersonName[values.length];
        for (int i = 0; i < a.length; ++i) {
            a[i] = new PersonNameImpl(values[i]);
        }
        return put(StringElement.createPN(tag, a, getCharset()));
    }
    
    public DcmElement putSH(int tag) {
        return put(StringElement.createSH(tag));
    }
    
    public DcmElement putSH(int tag, String value) {
        return put(value != null
        ? StringElement.createSH(tag, value, getCharset())
        : StringElement.createSH(tag));
    }
    
    public DcmElement putSH(int tag, String[] values) {
        return put(StringElement.createSH(tag, values, getCharset()));
    }
    
    public DcmElement putSL(int tag) {
        return put(ValueElement.createSL(tag));
    }
    
    public DcmElement putSL(int tag, int value) {
        return put(ValueElement.createSL(tag, value));
    }
    
    public DcmElement putSL(int tag, int[] values) {
        return put(ValueElement.createSL(tag, values));
    }
    
    public DcmElement putSL(int tag, String value) {
        return put(value != null
        ? ValueElement.createSL(tag, StringUtils.parseInt(value,
        Integer.MIN_VALUE,  Integer.MAX_VALUE))
        : ValueElement.createSL(tag));
    }
    
    public DcmElement putSL(int tag, String[] values) {
        return put(ValueElement.createSL(tag, StringUtils.parseInts(values,
        Integer.MIN_VALUE,  Integer.MAX_VALUE)));
    }
    
    public DcmElement putSQ(int tag) {
        throw new UnsupportedOperationException();
    }
    
    public DcmElement putSS(int tag) {
        return put(ValueElement.createSS(tag));
    }
    
    public DcmElement putSS(int tag, int value) {
        return put(ValueElement.createSS(tag, value));
    }
    
    public DcmElement putSS(int tag, int[] values) {
        return put(ValueElement.createSS(tag, values));
    }
    
    public DcmElement putSS(int tag, String value) {
        return put(value != null
        ? ValueElement.createSS(tag, StringUtils.parseInt(value,
        Short.MIN_VALUE,  Short.MAX_VALUE))
        : ValueElement.createSS(tag));
    }
    
    public DcmElement putSS(int tag, String[] values) {
        return put(ValueElement.createSS(tag, StringUtils.parseInts(values,
        Short.MIN_VALUE,  Short.MAX_VALUE)));
    }
    
    public DcmElement putST(int tag) {
        return put(StringElement.createST(tag));
    }
    
    public DcmElement putST(int tag, String value) {
        return put(value != null
        ? StringElement.createST(tag, value, getCharset())
        : StringElement.createST(tag));
    }
    
    public DcmElement putST(int tag, String[] values) {
        return put(StringElement.createST(tag, values, getCharset()));
    }
    
    public DcmElement putTM(int tag) {
        return put(StringElement.createTM(tag));
    }
    
    public DcmElement putTM(int tag, Date value) {
        return put(value != null
        ? StringElement.createTM(tag, value)
        : StringElement.createTM(tag));
    }
    
    public DcmElement putTM(int tag, Date[] values) {
        return put(StringElement.createTM(tag, values));
    }
    
    public DcmElement putTM(int tag, Date from, Date to) {
        return put(StringElement.createTM(tag, from, to));
    }
    
    public DcmElement putTM(int tag, String value) {
        return put(value != null
        ? StringElement.createTM(tag, value)
        : StringElement.createTM(tag));
    }
    
    public DcmElement putTM(int tag, String[] values) {
        return put(StringElement.createTM(tag, values));
    }
    
    public DcmElement putUI(int tag) {
        return put(StringElement.createUI(tag));
    }
    
    public DcmElement putUI(int tag, String value) {
        return put(value != null
        ? StringElement.createUI(tag, value)
        : StringElement.createUI(tag));
    }
    
    public DcmElement putUI(int tag, String[] values) {
        return put(StringElement.createUI(tag, values));
    }
    
    public DcmElement putUL(int tag) {
        return put(ValueElement.createUL(tag));
    }
    
    public DcmElement putUL(int tag, int value) {
        return put(ValueElement.createUL(tag, value));
    }
    
    public DcmElement putUL(int tag, int[] values) {
        return put(ValueElement.createUL(tag, values));
    }
    
    public DcmElement putUL(int tag, String value) {
        return put(value != null
        ? ValueElement.createUL(tag, StringUtils.parseInt(value,
        0L,  0xFFFFFFFFL))
        : ValueElement.createUL(tag));
    }
    
    public DcmElement putUL(int tag, String[] values) {
        return put(ValueElement.createUL(tag, StringUtils.parseInts(values,
        0L,  0xFFFFFFFFL)));
    }
    
    public DcmElement putUN(int tag) {
        return put(ValueElement.createUN(tag));
    }
    
    public DcmElement putUN(int tag, byte[] value) {
        return put(ValueElement.createUN(tag, value));
    }
    
    public DcmElement putUN(int tag, ByteBuffer value) {
        return put(ValueElement.createUN(tag, value));
    }
    
    public DcmElement putUNsq(int tag) {
        return put(FragmentElement.createUN(tag));
    }
    
    public DcmElement putUS(int tag) {
        return put(ValueElement.createUS(tag));
    }
    
    public DcmElement putUS(int tag, int value) {
        return put(ValueElement.createUS(tag, value));
    }
    
    public DcmElement putUS(int tag, int[] values) {
        return put(ValueElement.createUS(tag, values));
    }
    
    public DcmElement putUS(int tag, String value) {
        return put(value != null
        ? ValueElement.createUS(tag, StringUtils.parseInt(value,
        0L,  0xFFFFL))
        : ValueElement.createUS(tag));
    }
    
    public DcmElement putUS(int tag, String[] values) {
        return put(ValueElement.createUS(tag, StringUtils.parseInts(values,
        0L,  0xFFFFL)));
    }
    
    public DcmElement putUT(int tag) {
        return put(StringElement.createUT(tag));
    }
    
    public DcmElement putUT(int tag, String value) {
        return put(value != null
        ? StringElement.createUT(tag, value, getCharset())
        : StringElement.createUT(tag));
    }
    
    public DcmElement putUT(int tag, String[] values) {
        return put(StringElement.createUT(tag, values, getCharset()));
    }
    
    public DcmElement putXX(int tag) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag));
    }
    
    public DcmElement putXX(int tag, ByteBuffer bytes) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), bytes);
    }
    
    public DcmElement putXX(int tag, String value) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), value);
    }
    
    public DcmElement putXX(int tag, String[] values) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), values);
    }
    
    public DcmElement putXXsq(int tag) {
        return putXXsq(tag, VRMap.DEFAULT.lookup(tag));
    }
    
    public DcmElement putXXsq(int tag, int vr) {
        switch (vr) {
            case VRs.OB:
                return putOBsq(tag);
            case VRs.OF:
                return putOFsq(tag);
            case VRs.OW:
                return putOWsq(tag);
            case VRs.UN:
                return putUNsq(tag);
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                + " " + VRs.toString(vr));
        }
    }
    
    public DcmElement putXX(int tag, int vr) {
        switch (vr) {
            case VRs.AE:
                return putAE(tag);
            case VRs.AS:
                return putAS(tag);
            case VRs.AT:
                return putAT(tag);
            case VRs.CS:
                return putCS(tag);
            case VRs.DA:
                return putDA(tag);
            case VRs.DS:
                return putDS(tag);
            case VRs.DT:
                return putDT(tag);
            case VRs.FL:
                return putFL(tag);
            case VRs.FD:
                return putFD(tag);
            case VRs.IS:
                return putIS(tag);
            case VRs.LO:
                return putLO(tag);
            case VRs.LT:
                return putLT(tag);
            case VRs.OB:
                return putOB(tag);
            case VRs.OF:
                return putOF(tag);
            case VRs.OW:
                return putOW(tag);
            case VRs.PN:
                return putPN(tag);
            case VRs.SH:
                return putSH(tag);
            case VRs.SL:
                return putSL(tag);
            case VRs.SQ:
                return ((Dataset)this).putSQ(tag);
            case VRs.SS:
                return putSS(tag);
            case VRs.ST:
                return putST(tag);
            case VRs.TM:
                return putTM(tag);
            case VRs.UI:
                return putUI(tag);
            case VRs.UN:
                return putUN(tag);
            case VRs.UL:
                return putUL(tag);
            case VRs.US:
                return putUS(tag);
            case VRs.UT:
                return putUT(tag);
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                + " " + VRs.toString(vr));
        }
    }
    
    public DcmElement putXX(int tag, int vr, ByteBuffer value) {
        switch (vr) {
            case VRs.AE:
                return put(StringElement.createAE(tag, value));
            case VRs.AS:
                return put(StringElement.createAS(tag, value));
            case VRs.AT:
                return put(ValueElement.createAT(tag, value));
            case VRs.CS:
                return put(StringElement.createCS(tag, value));
            case VRs.DA:
                return put(StringElement.createDA(tag, value));
            case VRs.DS:
                return put(StringElement.createDS(tag, value));
            case VRs.DT:
                return put(StringElement.createDT(tag, value));
            case VRs.FL:
                return put(ValueElement.createFL(tag, value));
            case VRs.FD:
                return put(ValueElement.createFD(tag, value));
            case VRs.IS:
                return put(StringElement.createIS(tag, value));
            case VRs.LO:
                return put(StringElement.createLO(tag, value));
            case VRs.LT:
                return put(StringElement.createLT(tag, value));
            case VRs.OB:
                return put(ValueElement.createOB(tag, value));
            case VRs.OF:
                return put(ValueElement.createOF(tag, value));
            case VRs.OW:
                return put(ValueElement.createOW(tag, value));
            case VRs.PN:
                return put(StringElement.createPN(tag, value));
            case VRs.SH:
                return put(StringElement.createSH(tag, value));
            case VRs.SL:
                return put(ValueElement.createSL(tag, value));
            case VRs.SS:
                return put(ValueElement.createSS(tag, value));
            case VRs.ST:
                return put(StringElement.createST(tag, value));
            case VRs.TM:
                return put(StringElement.createTM(tag, value));
            case VRs.UI:
                return put(StringElement.createUI(tag, value));
            case VRs.UN:
                return put(ValueElement.createUN(tag, value));
            case VRs.UL:
                return put(ValueElement.createUL(tag, value));
            case VRs.US:
                return put(ValueElement.createUS(tag, value));
            case VRs.UT:
                return put(StringElement.createUT(tag, value));
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                + " " + VRs.toString(vr));
        }
    }
    
    public DcmElement putXX(int tag, int vr, String value) {
        switch (vr) {
            case VRs.AE:
                return putAE(tag, value);
            case VRs.AS:
                return putAS(tag, value);
            case VRs.AT:
                return putAT(tag, value);
            case VRs.CS:
                return putCS(tag, value);
            case VRs.DA:
                return putDA(tag, value);
            case VRs.DS:
                return putDS(tag, value);
            case VRs.DT:
                return putDT(tag, value);
            case VRs.FL:
                return putFL(tag, value);
            case VRs.FD:
                return putFD(tag, value);
            case VRs.IS:
                return putIS(tag, value);
            case VRs.LO:
                return putLO(tag, value);
            case VRs.LT:
                return putLT(tag, value);
                //            case VRs.OB:
                //                return putOB(tag, value);
                //            case VRs.OF:
                //                return putOF(tag, value);
                //            case VRs.OW:
                //                return putOW(tag, value);
            case VRs.PN:
                return putPN(tag, value);
            case VRs.SH:
                return putSH(tag, value);
            case VRs.SL:
                return putSL(tag, value);
            case VRs.SS:
                return putSS(tag, value);
            case VRs.ST:
                return putST(tag, value);
            case VRs.TM:
                return putTM(tag, value);
            case VRs.UI:
                return putUI(tag, value);
                //            case VRs.UN:
                //                return putUN(tag, value);
            case VRs.UL:
                return putUL(tag, value);
            case VRs.US:
                return putUS(tag, value);
            case VRs.UT:
                return putUT(tag, value);
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                + " " + VRs.toString(vr));
        }
    }
    
    public DcmElement putXX(int tag, int vr, String[] values) {
        switch (vr) {
            case VRs.AE:
                return putAE(tag, values);
            case VRs.AS:
                return putAS(tag, values);
            case VRs.AT:
                return putAT(tag, values);
            case VRs.CS:
                return putCS(tag, values);
            case VRs.DA:
                return putDA(tag, values);
            case VRs.DS:
                return putDS(tag, values);
            case VRs.DT:
                return putDT(tag, values);
            case VRs.FL:
                return putFL(tag, values);
            case VRs.FD:
                return putFD(tag, values);
            case VRs.IS:
                return putIS(tag, values);
            case VRs.LO:
                return putLO(tag, values);
            case VRs.LT:
                return putLT(tag, values);
                //            case VRs.OB:
                //                return putOB(tag, values);
                //            case VRs.OF:
                //                return putOF(tag, values);
                //            case VRs.OW:
                //                return putOW(tag, values);
            case VRs.PN:
                return putPN(tag, values);
            case VRs.SH:
                return putSH(tag, values);
            case VRs.SL:
                return putSL(tag, values);
            case VRs.SS:
                return putSS(tag, values);
            case VRs.ST:
                return putST(tag, values);
            case VRs.TM:
                return putTM(tag, values);
            case VRs.UI:
                return putUI(tag, values);
                //            case VRs.UN:
                //                return putUN(tag, values);
            case VRs.UL:
                return putUL(tag, values);
            case VRs.US:
                return putUS(tag, values);
            case VRs.UT:
                return putUT(tag, values);
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                + " " + VRs.toString(vr));
        }
    }
    
    public Iterator iterator() {
        return list.iterator();
    }
    
    public void putAll(DcmObject dcmObj) {
        for (Iterator it = dcmObj.iterator(); it.hasNext();) {
            DcmElement el = (DcmElement)it.next();
            if (el.isEmpty()) {
                putXX(el.tag(), el.vr());
            } else {
                DcmElement sq;
                switch (el.vr()) {
                    case VRs.SQ:
                        sq = putSQ(el.tag());
                        for (int i = 0, n = el.vm(); i < n; ++i) {
                            sq.addItem(el.getItem(i));
                        }
                        break;
                    case VRs.OB:
                    case VRs.OF:
                    case VRs.OW:
                    case VRs.UN:
                        if (el.hasDataFragments()) {
                            sq = putXXsq(el.tag(), el.vr());
                            for (int i = 0, n = el.vm(); i < n; ++i) {
                                sq.addDataFragment(el.getDataFragment(i));
                            }
                            break;
                        }
                    default:
                        putXX(el.tag(), el.vr(), el.getByteBuffer());
                        break;
                }
            }
        }
    }
    
    protected void write(int grTag, int grLen, DcmHandler handler)
    throws IOException {
        byte[] b4 = {
            (byte)grLen,
            (byte)(grLen >>> 8),
            (byte)(grLen >>> 16),
            (byte)(grLen >>> 24)
        };
        long el1Pos = ((DcmElement)list.get(0)).getStreamPosition();
        handler.startElement(grTag, VRs.UL, el1Pos == -1L ? -1L : el1Pos - 12);
        handler.value(b4,0,4);
        handler.endElement();
        for (int i = 0, n = list.size(); i < n; ++i) {
            DcmElement el = (DcmElement)list.get(i);
            int len = el.length();
            handler.startElement(el.tag(), el.vr(), el.getStreamPosition());
            ByteBuffer bb = el.getByteBuffer(ByteOrder.LITTLE_ENDIAN);
            handler.value(bb.array(), bb.arrayOffset(), bb.limit());
            handler.endElement();
        }
    }
    
    public void writeHeader(ImageOutputStream out, DcmEncodeParam encParam,
    int tag, int vr, int len)
    throws IOException {
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(tag >> 16);
            out.write(tag >> 24);
            out.write(tag >> 0);
            out.write(tag >> 8);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(tag >> 24);
            out.write(tag >> 16);
            out.write(tag >> 8);
            out.write(tag >> 0);
        }
        if (vr != VRs.NONE && encParam.explicitVR) {
            out.write(vr >> 8);
            out.write(vr >> 0);
            if (VRs.isLengthField16Bit(vr)) {
                if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    out.write(len >> 0);
                    out.write(len >> 8);
                } else {
                    out.write(len >> 8);
                    out.write(len >> 0);
                }
                return;
            } else {
                out.write(0);
                out.write(0);
            }
        }
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(len >> 0);
            out.write(len >> 8);
            out.write(len >> 16);
            out.write(len >> 24);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(len >> 24);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len >> 0);
        }
    }
    
    public void writeHeader(OutputStream out, DcmEncodeParam encParam,
    int tag, int vr, int len)
    throws IOException {
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(tag >> 16);
            out.write(tag >> 24);
            out.write(tag >> 0);
            out.write(tag >> 8);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(tag >> 24);
            out.write(tag >> 16);
            out.write(tag >> 8);
            out.write(tag >> 0);
        }
        if (encParam.explicitVR) {
            out.write(vr >> 8);
            out.write(vr >> 0);
            if (VRs.isLengthField16Bit(vr)) {
                if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    out.write(len >> 0);
                    out.write(len >> 8);
                } else {
                    out.write(len >> 8);
                    out.write(len >> 0);
                }
                return;
            } else {
                out.write(0);
                out.write(0);
            }
        }
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(len >> 0);
            out.write(len >> 8);
            out.write(len >> 16);
            out.write(len >> 24);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(len >> 24);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len >> 0);
        }
    }
    
}