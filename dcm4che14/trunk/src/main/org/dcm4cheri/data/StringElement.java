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

import org.dcm4che.data.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class StringElement extends ValueElement {
    private interface Trim {
        public String trim(String s);
    }

    private static final Trim NO_TRIM = new Trim() {
        public String trim(String s) {
            return s;
        }
    };

    private static final Trim TRAIL_TRIM = new Trim() {
        public String trim(String s) {
            char ch;
            for (int r = s.length(); r > 0; --r)
                if ((ch = s.charAt(r-1)) != '\0' && ch != ' ')
                    return s.substring(0,r);
            return "";
        }
    };

    private static final Trim TOT_TRIM = new Trim() {
        public String trim(String s) {
            for (int r = s.length(); r > 0; --r)
                if (s.charAt(r-1) != ' ')
                    for (int l = 0; l < r; ++l)
                        if (s.charAt(l) != ' ')
                            return s.substring(l,r);
            return "";
        }
    };
    
    private interface Check {
        public String check(String s);
    }
    
    private static final Check NO_CHECK = new Check() {
        public String check(String s) {
            return s;
        }
    };
        
    private static class CheckImpl implements Check {
        protected final int maxLen;
        protected final boolean text;
        CheckImpl(int maxLen, boolean text) {
            this.maxLen = maxLen;
            this.text = text;
        }
        public String check(String s) {
            char[] a = s.toCharArray();
            if (a.length > maxLen)
                throw new IllegalArgumentException(s);
            for (int i = 0; i < a.length; ++i) {
                if (!check(a[i]))
                    throw new IllegalArgumentException(s);
            }
            return s;
        }
        protected boolean check(char c) {
            return Character.isISOControl(c) ? (text && isDICOMControl(c))
                                             : (text || c != '\\');
        }
    }

    private static boolean isDICOMControl(char c) {
        switch (c) {
            case '\n': case '\f': case '\r': case '\033':
                return true;
        }
        return false;
    }    
    
    private static ByteBuffer toByteBuffer(String value, Trim trim, Check check,
            Charset cs) {
        if (value == null || value.length() == 0)
            return EMPTY_VALUE;
        try {
            return (cs != null ? cs : Charsets.ASCII).newEncoder().encode(
                    CharBuffer.wrap(check.check(trim.trim(value))));
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException(value);
        }
    }
    
    private static ByteBuffer toByteBuffer(ByteBuffer[] bbs, int totLen) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[totLen]);
        bb.put(bbs[0]);
        for (int i = 1; i < bbs.length; ++i) {
            bb.put(DELIM);
            bb.put(bbs[i]);
        }
        return bb;        
    }
    
    private static ByteBuffer toByteBuffer(String[] values, Trim trim, 
            Check check, Charset cs) {
        if (values.length == 0)
            return EMPTY_VALUE;

        if (values.length == 1)
            return toByteBuffer(values[0], trim, check, cs);
        
        ByteBuffer[] bbs = new ByteBuffer[values.length];
        int totLen = -1;        
        for (int i = 0; i < values.length; ++i) {
            bbs[i] = toByteBuffer(values[i], trim, check, cs);
            totLen += bbs[i].limit() + 1;
        }
        return toByteBuffer(bbs, totLen);
    }
    
    private final Trim trim;

    StringElement(int tag, ByteBuffer data, Trim trim) {
        super(tag, data);
        this.trim = trim;
    }

    public String getString(int index, Charset cs)
            throws DcmValueException {
        if (index >= vm())
            return index == 0 ? "" : null;
        try {
            return trim.trim((cs != null ? cs : Charsets.ASCII).newDecoder()
                    .decode(getByteBuffer(index)).toString());
        } catch (CharacterCodingException ex) {
            throw new DcmValueException(ex.getMessage(),ex);
        }
    }

    public String[] getStrings(Charset cs) throws DcmValueException {
        String[] a = new String[vm()];
        for (int i = 0; i < a.length; ++i)
            a[i] = getString(i, cs);
        return a;
    }
    
    public ByteBuffer getByteBuffer(int index) {
        checkIndex(index);
        return (ByteBuffer)data.rewind();
    }

    // LT -------------------------------------------------------------        
    private static final class LT extends StringElement {
        LT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }
        public final int vr() {
            return 0x4C54;
        }
    }
    
    private static final Check LT_CHECK = new CheckImpl(10240,true);

    static DcmElement createLT(int tag, ByteBuffer data) {
        return new LT(tag, data);
    }

    static DcmElement createLT(int tag) {
        return new LT(tag, EMPTY_VALUE);
    }       

    static DcmElement createLT(int tag, String value, Charset cs) {
        return new LT(tag,
                toByteBuffer(value, TRAIL_TRIM, LT_CHECK, cs));
    }

    static DcmElement createLT(int tag, String[] values, Charset cs) {
        return new LT(tag,
                toByteBuffer(values, TRAIL_TRIM, LT_CHECK, cs));
    }

    // ST -------------------------------------------------------------        
    private static final class ST extends StringElement {
        ST(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }
        public final int vr() {
            return 0x5354;
        }
    }
    
    private static final Check ST_CHECK = new CheckImpl(1024,true);

    static DcmElement createST(int tag, ByteBuffer data) {
        return new ST(tag, data);
    }

    static DcmElement createST(int tag) {
        return new ST(tag, EMPTY_VALUE);
    }       

    static DcmElement createST(int tag, String value, Charset cs) {
        return new ST(tag, 
                toByteBuffer(value, TRAIL_TRIM, ST_CHECK, cs));
    }

    static DcmElement createST(int tag, String[] values, Charset cs) {
        return new ST(tag,
                toByteBuffer(values, TRAIL_TRIM, ST_CHECK, cs));
    }

    // UT -------------------------------------------------------------        
    private static final class UT extends StringElement {
        UT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }
        public final int vr() {
            return 0x554C;
        }
    }
    
    static final Check UT_CHECK = new CheckImpl(Integer.MAX_VALUE, true);

    static DcmElement createUT(int tag, ByteBuffer data) {
        return new UT(tag, data);
    }

    static DcmElement createUT(int tag) {
        return new UT(tag, EMPTY_VALUE);
    }       

    static DcmElement createUT(int tag, String value, Charset cs) {
        return new UT(tag,
                toByteBuffer(value, TRAIL_TRIM, UT_CHECK, cs));
    }

    static DcmElement createUT(int tag, String[] values, Charset cs) {
        return new UT(tag,
                toByteBuffer(values, TRAIL_TRIM, UT_CHECK, cs));
    }

    // MultiStringElements ---------------------------------------------------       
    private static final byte DELIM = 0x5c;
    private static abstract class MultiStringElement extends StringElement {
        private int[] delimPos = null;

        MultiStringElement(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }

        public final int vm() {
            if (delimPos != null) {
                return delimPos.length - 1;
            }
            byte[] a = data.array();
            if (a.length == 0) return 0;
            int vm = 1;
            for (int i = 0; i < a.length; ++i) {
                if (a[i] == DELIM) ++vm;
            }
            delimPos = new int[vm+1];
            delimPos[0] = -1;
            delimPos[vm] = a.length;
            for (int i = 0, j = 0; i < a.length; ++i) {
                if (a[i] == DELIM)
                    delimPos[++j] = i;
            }
            return vm;                
        }
        
        public ByteBuffer getByteBuffer(int index) {
            checkIndex(index);           
            return vm() == 1 ? (ByteBuffer)data.rewind() : ByteBuffer.wrap(
                    data.array(), delimPos[index]+1,
                    delimPos[index+1]-delimPos[index]-1);
        }
    }

    // LO ------------------------------------------------------------------
    private static final class LO extends MultiStringElement {
        LO(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }
        public final int vr() {
            return 0x4C4F;
        }
    }       

    private static final Check LO_CHECK = new CheckImpl(64,false);

    static DcmElement createLO(int tag, ByteBuffer data) {
        return new LO(tag, data);
    }

    static DcmElement createLO(int tag) {
        return new LO(tag, EMPTY_VALUE);
    }               

    static DcmElement createLO(int tag, String value, Charset cs) {
        return new LO(tag,
                toByteBuffer(value, TOT_TRIM, LO_CHECK, cs));
    }

    static DcmElement createLO(int tag, String[] values, Charset cs) {
        return new LO(tag,
                toByteBuffer(values, TOT_TRIM, LO_CHECK, cs));
    }
        
    // PN ------------------------------------------------------------------
    private static final class PN extends MultiStringElement {
        PN(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }
        public final int vr() {
            return 0x504E;
        }
        public final PersonName getPersonName(int index, Charset cs) 
                throws DcmValueException {
            return new PersonNameImpl(getString(index, cs));
        }
    }       

    static DcmElement createPN(int tag, ByteBuffer data) {
        return new PN(tag, data);
    }

    static DcmElement createPN(int tag) {
        return new PN(tag, EMPTY_VALUE);
    }               

    static DcmElement createPN(int tag, PersonName value, Charset cs)
    {
        return new PN(tag,
            toByteBuffer(value.toString(), NO_TRIM, NO_CHECK, cs));
    }

    static DcmElement createPN(int tag, PersonName[] values,
            Charset cs) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i)
            tmp[i] = values[i].toString();
        return new PN(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, cs));
    }
        
    // SH ------------------------------------------------------------------
    private static final class SH extends MultiStringElement {
        SH(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }
        public final int vr() {
            return 0x5348;
        }
    }       

    private static final Check SH_CHECK = new CheckImpl(16,false);

    static DcmElement createSH(int tag, ByteBuffer data) {
        return new SH(tag, data);
    }

    static DcmElement createSH(int tag) {
        return new SH(tag, EMPTY_VALUE);
    }       

    static DcmElement createSH(int tag, String value, Charset cs) {
        return new SH(tag,
                toByteBuffer(value, TOT_TRIM, SH_CHECK, cs));
    }

    static DcmElement createSH(int tag, String[] values, Charset cs) {
        return new SH(tag,
                toByteBuffer(values, TOT_TRIM, SH_CHECK, cs));
    }
            
    // AsciiMultiStringElements ----------------------------------------------       
    private static abstract class AsciiMultiStringElement
            extends MultiStringElement {
        AsciiMultiStringElement(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }
        public final String getString(int index, Charset cs)
                throws DcmValueException {
            return super.getString(index, null);
        }
    }

    // AE ------------------------------------------------------------------
    private static final class AE extends AsciiMultiStringElement {
        AE(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }
        public final int vr() {
            return 0x4145;
        }
    }

    private static final Check AE_CHECK = new CheckImpl(16,false);

    static DcmElement createAE(int tag, ByteBuffer data) {
        return new AE(tag, data);
    }

    static DcmElement createAE(int tag) {
        return new AE(tag, EMPTY_VALUE);
    }

    static DcmElement createAE(int tag, String value) {
        return new AE(tag, toByteBuffer(value, TOT_TRIM, AE_CHECK, null));
    }

    static DcmElement createAE(int tag, String[] values) {
        return new AE(tag, toByteBuffer(values, TOT_TRIM, AE_CHECK, null));
    }
    
    // AS ------------------------------------------------------------------
    private static final class AS extends AsciiMultiStringElement {
        AS(int tag, ByteBuffer data) {
            super(tag, data, NO_TRIM);
        }
        public final int vr() {
            return 0x4153;
        }
    }       

    private static final Check AS_CHECK = new Check() {
        public String check(String s) {
            if (s.length() == 4
                    && Character.isDigit(s.charAt(0)) 
                    && Character.isDigit(s.charAt(1)) 
                    && Character.isDigit(s.charAt(2))) {          
                switch (s.charAt(3)) {
                    case 'D':
                    case 'W':
                    case 'M':
                    case 'Y':
                        return s;
                }
            }
            throw new IllegalArgumentException(s);            
        }
    };
    
    static DcmElement createAS(int tag, ByteBuffer data) {
        return new AS(tag, data);
    }

    static DcmElement createAS(int tag) {
        return new AS(tag, EMPTY_VALUE);
    }

    static DcmElement createAS(int tag, String value) {
        return new AS(tag, toByteBuffer(value, NO_TRIM, AS_CHECK, null));
    }

    static DcmElement createAS(int tag, String[] values) {
        return new AS(tag, toByteBuffer(values, NO_TRIM, AS_CHECK, null));
    }
    
    // CS ------------------------------------------------------------------
    private static final class CS extends AsciiMultiStringElement {
        CS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }
        public final int vr() {
            return 0x4353;
        }
    }       

    static final Check CS_CHECK = new CheckImpl(16, false) {    
        protected boolean check(char c) {
            return ((c >= 'A' && c <= 'Z')
                 || (c >= '0' && c <= '9')
                 || c == ' ' || c == '_');
        }
    };

    static DcmElement createCS(int tag, ByteBuffer data) {
        return new CS(tag, data);
    }

    static DcmElement createCS(int tag) {
        return new CS(tag, EMPTY_VALUE);
    }
    
    static DcmElement createCS(int tag, String v) {
        return new CS(tag, toByteBuffer(v, TOT_TRIM, CS_CHECK, null));
    }

    static DcmElement createCS(int tag, String[] a) {
        return new CS(tag, toByteBuffer(a, TOT_TRIM, CS_CHECK, null));
    }
    
    // DS ------------------------------------------------------------------
    private static final class DS extends AsciiMultiStringElement {
        DS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }
        public final int vr() {
            return 0x4453;
        }
        public final float getFloat(int index)
                throws DcmValueException {
            return Float.parseFloat(getString(index, null));
        }

        public final float[] getFloats() throws DcmValueException {
            float[] retval = new float[vm()];
            for (int i = 0; i < retval.length; ++i) {
                retval[i] = getFloat(i);
            }
            return retval;
        }
    }       

    static DcmElement createDS(int tag, ByteBuffer data) {
        return new DS(tag, data);
    }

    static DcmElement createDS(int tag) {
        return new DS(tag, EMPTY_VALUE);
    }
    
    static DcmElement createDS(int tag, float value) {
        return new DS(tag,
                toByteBuffer(String.valueOf(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDS(int tag, float[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i)
            tmp[i] = String.valueOf(values[i]);
        return new DS(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }
    
    // IS ------------------------------------------------------------------
    private static final class IS extends AsciiMultiStringElement {
        IS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4953;
        }

        public final int getInt(int index) throws DcmValueException {
            String s = getString(index, null);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                throw new DcmValueException(s, ex);
            }
        }

        public final int[] getInts() throws DcmValueException {
            int[] retval = new int[vm()];
            for (int i = 0; i < retval.length; ++i) {
                retval[i] = getInt(i);
            }
            return retval;
        }
    }       

    static DcmElement createIS(int tag, ByteBuffer data) {
        return new IS(tag, data);
    }

    static DcmElement createIS(int tag) {
        return new IS(tag, EMPTY_VALUE);
    }              

    static DcmElement createIS(int tag, int value) {
        return new IS(tag,
                toByteBuffer(String.valueOf(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createIS(int tag, int[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i)
            tmp[i] = String.valueOf(values[i]);
        return new IS(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }
       
    // UI ------------------------------------------------------------------
   private static final class UI extends AsciiMultiStringElement {
        UI(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM); 
        }
        public final int vr() {
            return 0x5549;
        }
    }       

    private static final int UID_DIGIT1 = 0;
    private static final int UID_DIGIT = 1;
    private static final int UID_DOT = 2;
    private static final int UID_ERROR = -1;
    private static int nextState(int state, char c) {
        switch (state) {
            case UID_DIGIT1:
                if (c > '0' && c <= '9')
                    return UID_DIGIT;
                if (c == '0')
                    return UID_DOT;
                return UID_ERROR;
            case UID_DIGIT:
                if (c >= '0' && c <= '9')
                     return UID_DIGIT;
                // fall through
            case UID_DOT:
                if (c == '.')
                   return UID_DIGIT1;
        }
        return UID_ERROR;                
    }

    private static final Check UI_CHECK = new CheckImpl(64, false) {
        public String check(String s) {
            char[] a = s.toCharArray();
            if (a.length > maxLen)
                throw new IllegalArgumentException(s);
            int state = UID_DIGIT1;
            for (int i = 0; i < a.length; ++i) {
                if ((state = nextState(state, a[i])) == UID_ERROR)
                    throw new IllegalArgumentException(s);
            }
            if (state == UID_DIGIT1)
                throw new IllegalArgumentException(s);
            return s;
        }
    };

    static DcmElement createUI(int tag, ByteBuffer data) {
        return new UI(tag, data);
    }

    static DcmElement createUI(int tag) {
        return new UI(tag, EMPTY_VALUE);
    }       

    static DcmElement createUI(int tag, String value) {
        return new UI(tag, toByteBuffer(value, NO_TRIM, UI_CHECK, null));
    }

    static DcmElement createUI(int tag, String[] values) {
        return new UI(tag, toByteBuffer(values, NO_TRIM, UI_CHECK, null));
    }

    // DA ---------------------------------------------- 
    private static final byte HYPHEN = 0x2d;
    private static final DateTimeFormatImpl DTF = DateTimeFormatImpl.inst;

    private static abstract class DateString extends AsciiMultiStringElement {
        DateString(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }
        public final boolean isDataRange() {
            for (int i = 0, n = data.limit(); i < n; ++i)
                if (data.get(i) == HYPHEN)
                    return true;
            return false;
        }
    }

    private static final class DA extends DateString {
        DA(int tag, ByteBuffer data) {
            super(tag, data, NO_TRIM);
        }

        public final int vr() {
            return 0x4441;
        }

        public final Date getDate(int index) throws DcmValueException {
            return DTF.parseDate(getString(index, null));
        }

        public final Date[] getDates() throws DcmValueException {
            Date[] a = new Date[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = DTF.parseDate(getString(i, null));
            return a;
        }

        public final Date[] getDateRange(int index)
                throws DcmValueException {
            return DTF.parseDateRange(getString(index, null));
        }
    }

    static DcmElement createDA(int tag, ByteBuffer data) {
        return new DA(tag, data);
    }

    static DcmElement createDA(int tag) {
        return new DA(tag, EMPTY_VALUE);
    }       

    static DcmElement createDA(int tag, Date value) {
        return new DA(tag,
                toByteBuffer(DTF.formatDate(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDA(int tag, Date[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i)
            tmp[i] = DTF.formatDate(values[i]);
        return new DA(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDA(int tag, Date from, Date to) {
        return new DA(tag, toByteBuffer(DTF.formatDateRange(from, to), NO_TRIM,
                NO_CHECK, null));
    }

    private static final class DT extends DateString {
        DT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x4454;
        }

        public final Date getDate(int index) throws DcmValueException {
            return DTF.parseDateTime(getString(index, null));
        }

        public final Date[] getDates() throws DcmValueException {
            Date[] a = new Date[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = DTF.parseDateTime(getString(i, null));
            return a;
        }

        public final Date[] getDateRange(int index)
                throws DcmValueException {
            return DTF.parseDateTimeRange(getString(index, null));
        }
    }

    static DcmElement createDT(int tag, ByteBuffer data) {
        return new DT(tag, data);
    }

    static DcmElement createDT(int tag) {
        return new DT(tag, EMPTY_VALUE);
    }       

    static DcmElement createDT(int tag, Date value) {
        return new DT(tag, toByteBuffer(DTF.formatDateTime(value), NO_TRIM,
                NO_CHECK, null));
    }

    static DcmElement createDT(int tag, Date[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i)
            tmp[i] = DTF.formatDateTime(values[i]);
        return new DT(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDT(int tag, Date from, Date to) {
        return new DT(tag, toByteBuffer(
                DTF.formatDateTimeRange(from, to), NO_TRIM, NO_CHECK, null));
    }

    private static final class TM extends DateString {
        TM(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x544D;
        }

        public final Date getDate(int index) throws DcmValueException {
            return DTF.parseTime(getString(index, null));
        }

        public final Date[] getDates() throws DcmValueException {
            Date[] a = new Date[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = DTF.parseTime(getString(i, null));
            return a;
        }

        public final Date[] getDateRange(int index)
                throws DcmValueException {
            return DTF.parseTimeRange(getString(index, null));
        }
    }

    static DcmElement createTM(int tag, ByteBuffer data) {
        return new TM(tag, data);
    }

    static DcmElement createTM(int tag) {
        return new TM(tag, EMPTY_VALUE);
    }       

    static DcmElement createTM(int tag, Date value) {
        return new TM(tag,
                toByteBuffer(DTF.formatTime(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createTM(int tag, Date[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i)
            tmp[i] = DTF.formatTime(values[i]);
        return new TM(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createTM(int tag, Date from, Date to) {
        return new TM(tag, toByteBuffer(
                DTF.formatTimeRange(from, to), NO_TRIM, NO_CHECK, null));
    }
}
