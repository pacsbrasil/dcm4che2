/*$Id$*/
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

package org.dcm4che.data;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;

import org.xml.sax.helpers.DefaultHandler;

/** Defines common behavior of <code>Command</code>, <code>Dataset</code>,
 * and <code>FileMetaInfo</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 5: Data Structures and Encoding, 6.2 Value Representation"
 */
public interface DcmObject {
    
    public Charset getCharset();
    
    public boolean isEmpty();

    public int size();
    
    public int length();

    public void clear();
    
    public DcmHandler getDcmHandler();

    public DefaultHandler getSAXHandler();
    
    public Iterator iterator();

    public boolean contains(int tag);

    public DcmElement get(int tag);
    
    public DcmElement remove(int tag);

    public ByteBuffer getByteBuffer(int tag);

    public String getString(int tag) throws DcmValueException;

    public String getString(int tag, String defVal) throws DcmValueException;

    public String getString(int tag, int index)
            throws DcmValueException;

    public String getString(int tag, int index, String defVal)
            throws DcmValueException;

    public String[] getStrings(int tag) throws DcmValueException;

    public int getInt(int tag, int defVal) throws DcmValueException;

    public int getInt(int tag, int index, int defVal)
            throws DcmValueException;

    public int[] getInts(int tag) throws DcmValueException;

    public float getFloat(int tag, float defVal) throws DcmValueException;

    public float getFloat(int tag, int index, float defVal)
            throws DcmValueException;

    public float[] getFloats(int tag) throws DcmValueException;

    public double getDouble(int tag, double defVal)
            throws DcmValueException;

    public double getDouble(int tag, int index, double defVal)
            throws DcmValueException;

    public double[] getDoubles(int tag) throws DcmValueException;

    public Date getDate(int tag) throws DcmValueException;

    public Date getDate(int tag, int index) throws DcmValueException;

    public Date[] getDates(int tag) throws DcmValueException;
    
    public Date getDateTime(int dateTag, int timeTag) throws DcmValueException;

    public Dataset getNestedDataset(int tag);

    public Dataset getNestedDataset(int tag, int index);

    public DcmElement setAE(int tag);
    
    public DcmElement setAE(int tag, String value);
    
    public DcmElement setAE(int tag, String[] values);
    
    public DcmElement setAS(int tag, String value);
    
    public DcmElement setAS(int tag, String[] values);
    
    public DcmElement setAT(int tag);

    public DcmElement setAT(int tag, int value);

    public DcmElement setAT(int tag, int[] values);
    
    public DcmElement setCS(int tag);
    
    public DcmElement setCS(int tag, String value);
    
    public DcmElement setCS(int tag, String[] values);
    
    public DcmElement setDA(int tag);
    
    public DcmElement setDA(int tag, Date value);
    
    public DcmElement setDA(int tag, Date[] values);
    
    public DcmElement setDA(int tag, Date from, Date to);
    
    public DcmElement setDA(int tag, String value);
    
    public DcmElement setDA(int tag, String[] values);
    
    public DcmElement setDS(int tag);
    
    public DcmElement setDS(int tag, float value);
    
    public DcmElement setDS(int tag, float[] values);
    
    public DcmElement setDS(int tag, String value);
    
    public DcmElement setDS(int tag, String[] values);

    public DcmElement setDT(int tag);
    
    public DcmElement setDT(int tag, Date value);
    
    public DcmElement setDT(int tag, Date[] values);
    
    public DcmElement setDT(int tag, Date from, Date to);
    
    public DcmElement setDT(int tag, String value);
    
    public DcmElement setDT(int tag, String[] values);

    public DcmElement setFL(int tag);
    
    public DcmElement setFL(int tag, float value);
    
    public DcmElement setFL(int tag, float[] values);
    
    public DcmElement setFL(int tag, String value);
    
    public DcmElement setFL(int tag, String[] values);

    public DcmElement setFD(int tag);
    
    public DcmElement setFD(int tag, double value);
    
    public DcmElement setFD(int tag, double[] values);
    
    public DcmElement setFD(int tag, String value);
    
    public DcmElement setFD(int tag, String[] values);

    public DcmElement setIS(int tag);
    
    public DcmElement setIS(int tag, int value);
    
    public DcmElement setIS(int tag, int[] values);
    
    public DcmElement setIS(int tag, String value);
    
    public DcmElement setIS(int tag, String[] values);

    public DcmElement setLO(int tag);
    
    public DcmElement setLO(int tag, String value);
    
    public DcmElement setLO(int tag, String[] values);
    
    public DcmElement setLT(int tag);
    
    public DcmElement setLT(int tag, String value);
    
    public DcmElement setLT(int tag, String[] values);
    
    public DcmElement setOB(int tag, byte[] value);

    public DcmElement setOB(int tag, ByteBuffer value);

    public DcmElement setOBsq(int tag);
    
    public DcmElement setOF(int tag, float[] value);

    public DcmElement setOF(int tag, ByteBuffer value);

    public DcmElement setOFsq(int tag);

    public DcmElement setOW(int tag, short[] value);

    public DcmElement setOW(int tag, ByteBuffer value);

    public DcmElement setOWsq(int tag);

    public DcmElement setPN(int tag);
    
    public DcmElement setPN(int tag, PersonName value);
    
    public DcmElement setPN(int tag, PersonName[] values);

    public DcmElement setPN(int tag, String value);
    
    public DcmElement setPN(int tag, String[] values);

    public DcmElement setSH(int tag);
    
    public DcmElement setSH(int tag, String value);
    
    public DcmElement setSH(int tag, String[] values);

    public DcmElement setSL(int tag);
    
    public DcmElement setSL(int tag, int value);
    
    public DcmElement setSL(int tag, int[] values);

    public DcmElement setSL(int tag, String value);
    
    public DcmElement setSL(int tag, String[] values);

    public DcmElement setSQ(int tag);
    
    public DcmElement setSS(int tag);
    
    public DcmElement setSS(int tag, int value);
    
    public DcmElement setSS(int tag, int[] values);

    public DcmElement setSS(int tag, String value);
    
    public DcmElement setSS(int tag, String[] values);

    public DcmElement setST(int tag);
    
    public DcmElement setST(int tag, String value);
    
    public DcmElement setST(int tag, String[] values);
    
    public DcmElement setTM(int tag);
    
    public DcmElement setTM(int tag, Date value);
    
    public DcmElement setTM(int tag, Date[] values);
    
    public DcmElement setTM(int tag, Date from, Date to);
    
    public DcmElement setTM(int tag, String value);
    
    public DcmElement setTM(int tag, String[] values);

    public DcmElement setUI(int tag);
    
    public DcmElement setUI(int tag, String value);
    
    public DcmElement setUI(int tag, String[] values);
    
    public DcmElement setUL(int tag);
    
    public DcmElement setUL(int tag, int value);
    
    public DcmElement setUL(int tag, int[] values);
    
    public DcmElement setUL(int tag, String value);
    
    public DcmElement setUL(int tag, String[] values);

    public DcmElement setUN(int tag, byte[] value);

    public DcmElement setUNsq(int tag);
    
    public DcmElement setUS(int tag);
    
    public DcmElement setUS(int tag, int value);
    
    public DcmElement setUS(int tag, int[] values);

    public DcmElement setUS(int tag, String value);
    
    public DcmElement setUS(int tag, String[] values);

    public DcmElement setUT(int tag);
    
    public DcmElement setUT(int tag, String value);
    
    public DcmElement setUT(int tag, String[] values);

    public DcmElement setXX(int tag, int vr, ByteBuffer bytes);
    
    public DcmElement setXXsq(int tag, int vr);
}

