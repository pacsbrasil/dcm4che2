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
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

import org.dcm4cheri.util.StringUtils;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.xml.sax.helpers.DefaultHandler;
import org.apache.log4j.Logger;

abstract class DcmObjectImpl implements org.dcm4che.data.DcmObject {
    private static final String CLASSNAME = "org.dcm4cheri.data.DcmObjectImpl";
    protected static final Logger log = Logger.getLogger(CLASSNAME);
    
    protected ArrayList list = new ArrayList();    
    
    public DcmHandler getDcmHandler() {
        return new DcmObjectHandlerImpl(this);
    }

    public DefaultHandler getSAXHandler() {
        return new SAXHandlerAdapter(getDcmHandler());
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
        return Collections.binarySearch(list, new DcmElementImpl(tag)) >= 0;
    }
    
    public DcmElement get(int tag) {
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

    public Date[] getDates(int tag) throws DcmValueException {
        DcmElement e = get(tag);
        if (e == null)
            return null;
        
        return e.getDates();
    }
    
    public Date getDateTime(int dateTag, int timeTag) throws DcmValueException {
        DcmElement date = get(dateTag);
        if (date == null || date.isEmpty())
            return null;

        DcmElement time = get(dateTag);
        if (time == null || time.isEmpty())
            return date.getDate();
        
        return new Date(date.getDate().getTime() + time.getDate().getTime());
    }

    public Dataset getNestedDataset(int tag) {
        return getNestedDataset(tag, 0);
    }
    
    public Dataset getNestedDataset(int tag, int index) {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index)
            return null;
        
        return e.getDataset(index);
    }
        
    protected DcmElement set(DcmElement newElem) {
        if ((newElem.tag() & 0xffff) == 0)
            return newElem;
        
        final int size = list.size();
        if (size == 0 || newElem.compareTo(list.get(size-1)) > 0)
            list.add(newElem);
        else {
            int index = Collections.binarySearch(list, newElem);
            if (index >= 0)
                list.set(index, newElem);
            else
                list.add(-(index+1),newElem);
        }
        return newElem;
    }
    
    public DcmElement setAE(int tag) {
        return set(StringElement.createAE(tag));
    }
    
    public DcmElement setAE(int tag, String value) {
        return set(value != null
                ? StringElement.createAE(tag, value)
                : StringElement.createAE(tag));
    }
    
    public DcmElement setAE(int tag, String[] values) {
        return set(StringElement.createAE(tag, values));
    }
    
    public DcmElement setAS(int tag, String value) {
        return set(value != null
                ? StringElement.createAS(tag, value)
                : StringElement.createAS(tag));
    }
    
    public DcmElement setAS(int tag, String[] values) {
        return set(StringElement.createAS(tag, values));
    }
    
    public DcmElement setAT(int tag) {
        return set(ValueElement.createAT(tag));
    }

    public DcmElement setAT(int tag, int value) {
        return set(ValueElement.createAT(tag,value));
    }

    public DcmElement setAT(int tag, int[] values) {
        return set(ValueElement.createAT(tag,values));
    }
    
    public DcmElement setCS(int tag) {
        return set(StringElement.createCS(tag));
    }
    
    public DcmElement setCS(int tag, String value) {
        return set(value != null
                ? StringElement.createCS(tag, value)
                : StringElement.createCS(tag));
    }
    
    public DcmElement setCS(int tag, String[] values) {
        return set(StringElement.createCS(tag, values));
    }
    
    public DcmElement setDA(int tag) {
        return set(StringElement.createDA(tag));
    }
    
    public DcmElement setDA(int tag, Date value) {
        return set(value != null
                ? StringElement.createDA(tag, value)
                : StringElement.createDA(tag));
    }
    
    public DcmElement setDA(int tag, Date[] values) {
        return set(StringElement.createDA(tag, values));
    }
    
    public DcmElement setDA(int tag, Date from, Date to) {
        return set(StringElement.createDA(tag, from, to));
    }
    
    public DcmElement setDA(int tag, String value) {
        return set(value != null
                ? StringElement.createDA(tag, value)
                : StringElement.createDA(tag));
    }
    
    public DcmElement setDA(int tag, String[] values) {
        return set(StringElement.createDA(tag, values));
    }

    public DcmElement setDS(int tag) {
        return set(StringElement.createDS(tag));
    }
    
    public DcmElement setDS(int tag, float value) {
        return set(StringElement.createDS(tag, value));
    }
    
    public DcmElement setDS(int tag, float[] values) {
        return set(StringElement.createDS(tag, values));
    }
    
    public DcmElement setDS(int tag, String value) {
        return set(value != null
                ? StringElement.createDS(tag, value)
                : StringElement.createDS(tag));
    }
    
    public DcmElement setDS(int tag, String[] values) {
        return set(StringElement.createDS(tag, values));
    }

    public DcmElement setDT(int tag) {
        return set(StringElement.createDT(tag));
    }
    
    public DcmElement setDT(int tag, Date value) {
        return set(value != null
                ? StringElement.createDT(tag, value)
                : StringElement.createDT(tag));
    }
    
    public DcmElement setDT(int tag, Date[] values) {
        return set(StringElement.createDT(tag, values));
    }
    
    public DcmElement setDT(int tag, Date from, Date to) {
        return set(StringElement.createDT(tag, from, to));
    }
    
    public DcmElement setDT(int tag, String value) {
        return set(value != null
                ? StringElement.createDT(tag, value)
                : StringElement.createDT(tag));
    }
    
    public DcmElement setDT(int tag, String[] values) {
        return set(StringElement.createDT(tag, values));
    }

    public DcmElement setFL(int tag) {
        return set(ValueElement.createFL(tag));
    }
    
    public DcmElement setFL(int tag, float value) {
        return set(ValueElement.createFL(tag, value));
    }
    
    public DcmElement setFL(int tag, float[] values) {
        return set(ValueElement.createFL(tag, values));
    }
    
    public DcmElement setFL(int tag, String value) {
        return set(value != null
                ? StringElement.createFL(tag, Float.parseFloat(value))
                : StringElement.createFL(tag));
    }
    
    public DcmElement setFL(int tag, String[] values) {
        return set(StringElement.createFL(tag, StringUtils.parseFloats(values)));
    }

     public DcmElement setFD(int tag) {
        return set(ValueElement.createFD(tag));
    }
    
    public DcmElement setFD(int tag, double value) {
        return set(ValueElement.createFD(tag, value));
    }
    
    public DcmElement setFD(int tag, double[] values) {
        return set(ValueElement.createFD(tag, values));
    }
    
    public DcmElement setFD(int tag, String value) {
        return set(value != null
                ? StringElement.createFD(tag, Double.parseDouble(value))
                : StringElement.createFD(tag));
    }
    
    public DcmElement setFD(int tag, String[] values) {
        return set(StringElement.createFD(tag,
                StringUtils.parseDoubles(values)));
    }

    public DcmElement setIS(int tag) {
        return set(StringElement.createIS(tag));
    }
    
    public DcmElement setIS(int tag, int value) {
        return set(StringElement.createIS(tag, value));
    }
    
    public DcmElement setIS(int tag, int[] values) {
        return set(StringElement.createIS(tag, values));
    }
    
    public DcmElement setIS(int tag, String value) {
        return set(value != null
                ? StringElement.createIS(tag, value)
                : StringElement.createIS(tag));
    }
    
    public DcmElement setIS(int tag, String[] values) {
        return set(StringElement.createIS(tag, values));
    }

    public DcmElement setLO(int tag) {
        return set(StringElement.createLO(tag));
    }
    
    public DcmElement setLO(int tag, String value) {
        return set(value != null
                ? StringElement.createLO(tag, value, getCharset())
                : StringElement.createLO(tag));
    }
    
    public DcmElement setLO(int tag, String[] values) {
         return set(StringElement.createLO(tag, values, getCharset()));
    }
    
    public DcmElement setLT(int tag) {
        return set(StringElement.createLT(tag));
    }
    
    public DcmElement setLT(int tag, String value) {
        return set(value != null
                ? StringElement.createLT(tag, value, getCharset())
                : StringElement.createLT(tag));
    }
    
    public DcmElement setLT(int tag, String[] values) {
         return set(StringElement.createLT(tag, values, getCharset()));
    }
    
    public DcmElement setOB(int tag, byte[] value) {
        return set(ValueElement.createOB(tag, value));
    }
    
    public DcmElement setOB(int tag, ByteBuffer value) {
        return set(ValueElement.createOB(tag, value));
    }

    public DcmElement setOBsq(int tag) {
        return set(FragmentElement.createOB(tag));
    }    

    public DcmElement setOF(int tag, float[] value) {
        return set(ValueElement.createOF(tag, value));
    }

    public DcmElement setOF(int tag, ByteBuffer value) {
        return set(ValueElement.createOF(tag, value));
    }

    public DcmElement setOFsq(int tag) {
        return set(FragmentElement.createOF(tag));
    }    

    public DcmElement setOW(int tag, short[] value) {
        return set(ValueElement.createOW(tag, value));
    }

    public DcmElement setOW(int tag, ByteBuffer value) {
        return set(ValueElement.createOW(tag, value));
    }

    public DcmElement setOWsq(int tag) {
        return set(FragmentElement.createOW(tag));
    }    

    public DcmElement setPN(int tag) {
        return set(StringElement.createSH(tag));
    }
    
    public DcmElement setPN(int tag, PersonName value) {
        return set(value != null
                ? StringElement.createPN(tag, value, getCharset())
                : StringElement.createPN(tag));
    }
    
    public DcmElement setPN(int tag, PersonName[] values) {
        return set(StringElement.createPN(tag, values, getCharset()));
    }

    public DcmElement setPN(int tag, String value) {
        return set(value != null
                ? StringElement.createPN(tag,
                        new PersonNameImpl(value), getCharset())
                : StringElement.createPN(tag));
    }
    
    public DcmElement setPN(int tag, String[] values) {
        PersonName[] a = new PersonName[values.length];
        for (int i = 0; i < a.length; ++i) {
            a[i] = new PersonNameImpl(values[i]);
        }
        return set(StringElement.createPN(tag, a, getCharset()));
    }
    
    public DcmElement setSH(int tag) {
        return set(StringElement.createSH(tag));
    }
    
    public DcmElement setSH(int tag, String value) {
        return set(value != null
                ? StringElement.createSH(tag, value, getCharset())
                : StringElement.createSH(tag));
    }
    
    public DcmElement setSH(int tag, String[] values) {
        return set(StringElement.createSH(tag, values, getCharset()));
    }

    public DcmElement setSL(int tag) {
        return set(ValueElement.createSL(tag));
    }
    
    public DcmElement setSL(int tag, int value) {
        return set(ValueElement.createSL(tag, value));
    }
    
    public DcmElement setSL(int tag, int[] values) {
        return set(ValueElement.createSL(tag, values));
    }

    public DcmElement setSL(int tag, String value) {
        return set(value != null
                ? StringElement.createSL(tag, StringUtils.parseInt(value,
                        Integer.MIN_VALUE,  Integer.MAX_VALUE))
                : StringElement.createSL(tag));
    }
    
    public DcmElement setSL(int tag, String[] values) {
        return set(StringElement.createSL(tag, StringUtils.parseInts(values,
                        Integer.MIN_VALUE,  Integer.MAX_VALUE)));
    }

    public DcmElement setSQ(int tag) {
        throw new UnsupportedOperationException();
    }
    
    public DcmElement setSS(int tag) {
        return set(ValueElement.createSS(tag));
    }
    
    public DcmElement setSS(int tag, int value) {
        return set(ValueElement.createSS(tag, value));
    }
    
    public DcmElement setSS(int tag, int[] values) {
        return set(ValueElement.createSS(tag, values));
    }

    public DcmElement setSS(int tag, String value) {
        return set(value != null
                ? StringElement.createSS(tag, StringUtils.parseInt(value,
                        Short.MIN_VALUE,  Short.MAX_VALUE))
                : StringElement.createSS(tag));
    }
    
    public DcmElement setSS(int tag, String[] values) {
        return set(StringElement.createSS(tag, StringUtils.parseInts(values,
                        Short.MIN_VALUE,  Short.MAX_VALUE)));
    }

    public DcmElement setST(int tag) {
        return set(StringElement.createST(tag));
    }
    
    public DcmElement setST(int tag, String value) {
        return set(value != null
                ? StringElement.createST(tag, value, getCharset())
                : StringElement.createST(tag));
    }
    
    public DcmElement setST(int tag, String[] values) {
         return set(StringElement.createST(tag, values, getCharset()));
    }
    
    public DcmElement setTM(int tag) {
        return set(StringElement.createTM(tag));
    }
    
    public DcmElement setTM(int tag, Date value) {
        return set(value != null
                ? StringElement.createTM(tag, value)
                : StringElement.createTM(tag));
    }
    
    public DcmElement setTM(int tag, Date[] values) {
        return set(StringElement.createTM(tag, values));
    }
    
    public DcmElement setTM(int tag, Date from, Date to) {
        return set(StringElement.createTM(tag, from, to));
    }
    
    public DcmElement setTM(int tag, String value) {
        return set(value != null
                ? StringElement.createTM(tag, value)
                : StringElement.createTM(tag));
    }
    
    public DcmElement setTM(int tag, String[] values) {
        return set(StringElement.createTM(tag, values));
    }

    public DcmElement setUI(int tag) {
        return set(StringElement.createUI(tag));
    }
    
    public DcmElement setUI(int tag, String value) {
        return set(value != null
                ? StringElement.createUI(tag, value)
                : StringElement.createUI(tag));
    }
    
    public DcmElement setUI(int tag, String[] values) {
        return set(StringElement.createUI(tag, values));
    }

    public DcmElement setUL(int tag) {
        return set(ValueElement.createUL(tag));
    }
    
    public DcmElement setUL(int tag, int value) {
        return set(ValueElement.createUL(tag, value));
    }
    
    public DcmElement setUL(int tag, int[] values) {
        return set(ValueElement.createUL(tag, values));
    }
    
    public DcmElement setUL(int tag, String value) {
        return set(value != null
                ? StringElement.createUL(tag, StringUtils.parseInt(value,
                        0L,  0xFFFFFFFFL))
                : StringElement.createUL(tag));
    }
    
    public DcmElement setUL(int tag, String[] values) {
        return set(StringElement.createUL(tag, StringUtils.parseInts(values,
                        0L,  0xFFFFFFFFL)));
    }

    public DcmElement setUN(int tag, byte[] value) {
        return set(ValueElement.createUN(tag, value));
    }

    public DcmElement setUN(int tag, ByteBuffer value) {
        return set(ValueElement.createUN(tag, value));
    }

    public DcmElement setUNsq(int tag) {
        return set(FragmentElement.createUN(tag));
    }    
    
    public DcmElement setUS(int tag) {
        return set(ValueElement.createUS(tag));
    }
    
    public DcmElement setUS(int tag, int value) {
        return set(ValueElement.createUS(tag, value));
    }
    
    public DcmElement setUS(int tag, int[] values) {
        return set(ValueElement.createUS(tag, values));
    }

    public DcmElement setUS(int tag, String value) {
        return set(value != null
                ? StringElement.createUS(tag, StringUtils.parseInt(value,
                        0L,  0xFFFFL))
                : StringElement.createUL(tag));
    }
    
    public DcmElement setUS(int tag, String[] values) {
        return set(StringElement.createUS(tag, StringUtils.parseInts(values,
                        0L,  0xFFFFL)));
    }

    public DcmElement setUT(int tag) {
        return set(StringElement.createUT(tag));
    }
    
    public DcmElement setUT(int tag, String value) {
        return set(value != null
                ? StringElement.createUT(tag, value, getCharset())
                : StringElement.createUT(tag));
    }
    
    public DcmElement setUT(int tag, String[] values) {
         return set(StringElement.createUT(tag, values, getCharset()));
    }
    
    public DcmElement setXXsq(int tag, int vr) {
        switch (vr) {
            case VRs.OB:
                return setOBsq(tag);
            case VRs.OF:
                return setOFsq(tag);
            case VRs.OW:
                return setOWsq(tag);
            case VRs.UN:
                return setUNsq(tag);
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                                    + " " + VRs.toString(vr));
        }
    }

    public DcmElement setXX(int tag, int vr) {
        switch (vr) {
            case VRs.AE:
                return set(StringElement.createAE(tag));
            case VRs.AS:
                return set(StringElement.createAS(tag));
            case VRs.AT:
                return set(ValueElement.createAT(tag));
            case VRs.CS:
                return set(StringElement.createCS(tag));
            case VRs.DA:
                return set(StringElement.createDA(tag));
            case VRs.DS:
                return set(StringElement.createDS(tag));
            case VRs.DT:
                return set(StringElement.createDT(tag));
            case VRs.FL:
                return set(ValueElement.createFL(tag));
            case VRs.FD:
                return set(ValueElement.createFD(tag));
            case VRs.IS:
                return set(StringElement.createIS(tag));
            case VRs.LO:
                return set(StringElement.createLO(tag));
            case VRs.LT:
                return set(StringElement.createLT(tag));
            case VRs.OB:
                return set(ValueElement.createOB(tag));
            case VRs.OF:
                return set(ValueElement.createOF(tag));
            case VRs.OW:
                return set(ValueElement.createOW(tag));
            case VRs.PN:
                return set(StringElement.createPN(tag));
            case VRs.SH:
                return set(StringElement.createSH(tag));
            case VRs.SL:
                return set(ValueElement.createSL(tag));
            case VRs.SQ:
                return ((Dataset)this).setSQ(tag);
            case VRs.SS:
                return set(ValueElement.createSS(tag));
            case VRs.ST:
                return set(StringElement.createST(tag));
            case VRs.TM:
                return set(StringElement.createTM(tag));
            case VRs.UI:
                return set(StringElement.createUI(tag));
            case VRs.UN:
                return set(ValueElement.createUN(tag));
            case VRs.UL:
                return set(ValueElement.createUL(tag));
            case VRs.US:
                return set(ValueElement.createUS(tag));
            case VRs.UT:
                return set(StringElement.createUT(tag));
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                                    + " " + VRs.toString(vr));
        }
    }
    
    public DcmElement setXX(int tag, int vr, ByteBuffer value) {
        switch (vr) {
            case VRs.AE:
                return set(StringElement.createAE(tag, value));
            case VRs.AS:
                return set(StringElement.createAS(tag, value));
            case VRs.AT:
                return set(ValueElement.createAT(tag, value));
            case VRs.CS:
                return set(StringElement.createCS(tag, value));
            case VRs.DA:
                return set(StringElement.createDA(tag, value));
            case VRs.DS:
                return set(StringElement.createDS(tag, value));
            case VRs.DT:
                return set(StringElement.createDT(tag, value));
            case VRs.FL:
                return set(ValueElement.createFL(tag, value));
            case VRs.FD:
                return set(ValueElement.createFD(tag, value));
            case VRs.IS:
                return set(StringElement.createIS(tag, value));
            case VRs.LO:
                return set(StringElement.createLO(tag, value));
            case VRs.LT:
                return set(StringElement.createLT(tag, value));
            case VRs.OB:
                return set(ValueElement.createOB(tag, value));
            case VRs.OF:
                return set(ValueElement.createOF(tag, value));
            case VRs.OW:
                return set(ValueElement.createOW(tag, value));
            case VRs.PN:
                return set(StringElement.createPN(tag, value));
            case VRs.SH:
                return set(StringElement.createSH(tag, value));
            case VRs.SL:
                return set(ValueElement.createSL(tag, value));
            case VRs.SS:
                return set(ValueElement.createSS(tag, value));
            case VRs.ST:
                return set(StringElement.createST(tag, value));
            case VRs.TM:
                return set(StringElement.createTM(tag, value));
            case VRs.UI:
                return set(StringElement.createUI(tag, value));
            case VRs.UN:
                return set(ValueElement.createUN(tag, value));
            case VRs.UL:
                return set(ValueElement.createUL(tag, value));
            case VRs.US:
                return set(ValueElement.createUS(tag, value));
            case VRs.UT:
                return set(StringElement.createUT(tag, value));
            default:
                throw new IllegalArgumentException(Tags.toString(tag)
                                    + " " + VRs.toString(vr));
        }
    }
    
    public Iterator iterator() {
        return list.iterator();
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
}