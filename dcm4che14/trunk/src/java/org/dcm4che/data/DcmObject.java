/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import javax.imageio.stream.ImageOutputStream;

import org.xml.sax.helpers.DefaultHandler;

/** Defines common behavior of <code>Command</code>, <code>Dataset</code>,
 * and <code>FileMetaInfo</code> container objects.
 *
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020722 gunter:</b>
 * <ul>
 * <li> add Private Data Elements access functions:<br>
 *      consider new Private Creator ID property
 * </ul>
 */
public interface DcmObject {
   
   void setPrivateCreatorID(String privateCreatorID);

   String getPrivateCreatorID();
   
   Charset getCharset();
   
   boolean isEmpty();
   
   int size();
   
   int length();
   
   void clear();
   
   DcmHandler getDcmHandler();
   
   DefaultHandler getSAXHandler();
   
   Iterator iterator();
   
   boolean contains(int tag);
   
   int vm(int tag);

   DcmElement get(int tag);
   
   DcmElement remove(int tag);
   
   ByteBuffer getByteBuffer(int tag);
   
   String getString(int tag) throws DcmValueException;
   
   String getString(int tag, String defVal) throws DcmValueException;
   
   String getString(int tag, int index)
   throws DcmValueException;
   
   String getString(int tag, int index, String defVal)
   throws DcmValueException;
   
   String[] getStrings(int tag) throws DcmValueException;
   
   String getBoundedString(int maxLen, int tag)
   throws DcmValueException;
   
   String getBoundedString(int maxLen, int tag, String defVal)
   throws DcmValueException;
   
   String getBoundedString(int maxLen, int tag, int index)
   throws DcmValueException;
   
   String getBoundedString(int maxLen, int tag, int index, String defVal)
   throws DcmValueException;
   
   String[] getBoundedStrings(int maxLen, int tag)
   throws DcmValueException;
   
   Integer getInteger(int tag) throws DcmValueException;

   Integer getInteger(int tag, int index) throws DcmValueException;
   
   int getInt(int tag, int defVal) throws DcmValueException;
   
   int getInt(int tag, int index, int defVal)
   throws DcmValueException;
   
   int[] getInts(int tag) throws DcmValueException;
   
   float getFloat(int tag, float defVal) throws DcmValueException;
   
   float getFloat(int tag, int index, float defVal)
   throws DcmValueException;
   
   float[] getFloats(int tag) throws DcmValueException;
   
   double getDouble(int tag, double defVal)
   throws DcmValueException;
   
   double getDouble(int tag, int index, double defVal)
   throws DcmValueException;
   
   double[] getDoubles(int tag) throws DcmValueException;
   
   Date getDate(int tag) throws DcmValueException;
   
   Date getDate(int tag, int index) throws DcmValueException;
   
   Date[] getDates(int tag) throws DcmValueException;
   
   Date getDateTime(int dateTag, int timeTag) throws DcmValueException;
   
   Date[] getDateRange(int tag) throws DcmValueException;
   
   Date[] getDateRange(int tag, int index) throws DcmValueException;
   
   Date[] getDateTimeRange(int dateTag, int timeTag) throws DcmValueException;

   Dataset getItem(int tag);
   
   Dataset getItem(int tag, int index);
   
   DcmElement putAE(int tag);
   
   DcmElement putAE(int tag, String value);
   
   DcmElement putAE(int tag, String[] values);
   
   DcmElement putAS(int tag);
   
   DcmElement putAS(int tag, String value);
   
   DcmElement putAS(int tag, String[] values);
   
   DcmElement putAT(int tag);
   
   DcmElement putAT(int tag, int value);
   
   DcmElement putAT(int tag, int[] values);
   
   DcmElement putAT(int tag, String value);
   
   DcmElement putAT(int tag, String[] values);
   
   DcmElement putCS(int tag);
   
   DcmElement putCS(int tag, String value);
   
   DcmElement putCS(int tag, String[] values);
   
   DcmElement putDA(int tag);
   
   DcmElement putDA(int tag, Date value);
   
   DcmElement putDA(int tag, Date[] values);
   
   DcmElement putDA(int tag, Date from, Date to);
   
   DcmElement putDA(int tag, String value);
   
   DcmElement putDA(int tag, String[] values);
   
   DcmElement putDS(int tag);
   
   DcmElement putDS(int tag, float value);
   
   DcmElement putDS(int tag, float[] values);
   
   DcmElement putDS(int tag, String value);
   
   DcmElement putDS(int tag, String[] values);
   
   DcmElement putDT(int tag);
   
   DcmElement putDT(int tag, Date value);
   
   DcmElement putDT(int tag, Date[] values);
   
   DcmElement putDT(int tag, Date from, Date to);
   
   DcmElement putDT(int tag, String value);
   
   DcmElement putDT(int tag, String[] values);
   
   DcmElement putFL(int tag);
   
   DcmElement putFL(int tag, float value);
   
   DcmElement putFL(int tag, float[] values);
   
   DcmElement putFL(int tag, String value);
   
   DcmElement putFL(int tag, String[] values);
   
   DcmElement putFD(int tag);
   
   DcmElement putFD(int tag, double value);
   
   DcmElement putFD(int tag, double[] values);
   
   DcmElement putFD(int tag, String value);
   
   DcmElement putFD(int tag, String[] values);
   
   DcmElement putIS(int tag);
   
   DcmElement putIS(int tag, int value);
   
   DcmElement putIS(int tag, int[] values);
   
   DcmElement putIS(int tag, String value);
   
   DcmElement putIS(int tag, String[] values);
   
   DcmElement putLO(int tag);
   
   DcmElement putLO(int tag, String value);
   
   DcmElement putLO(int tag, String[] values);
   
   DcmElement putLT(int tag);
   
   DcmElement putLT(int tag, String value);
   
   DcmElement putLT(int tag, String[] values);
   
   DcmElement putOB(int tag);
   
   DcmElement putOB(int tag, byte[] value);
   
   DcmElement putOB(int tag, ByteBuffer value);
   
   DcmElement putOBsq(int tag);
   
   DcmElement putOF(int tag);
   
   DcmElement putOF(int tag, float[] value);
   
   DcmElement putOF(int tag, ByteBuffer value);
   
   DcmElement putOFsq(int tag);
   
   DcmElement putOW(int tag);
   
   DcmElement putOW(int tag, short[] value);
   
   DcmElement putOW(int tag, ByteBuffer value);
   
   DcmElement putOWsq(int tag);
   
   DcmElement putPN(int tag);
   
   DcmElement putPN(int tag, PersonName value);
   
   DcmElement putPN(int tag, PersonName[] values);
   
   DcmElement putPN(int tag, String value);
   
   DcmElement putPN(int tag, String[] values);
   
   DcmElement putSH(int tag);
   
   DcmElement putSH(int tag, String value);
   
   DcmElement putSH(int tag, String[] values);
   
   DcmElement putSL(int tag);
   
   DcmElement putSL(int tag, int value);
   
   DcmElement putSL(int tag, int[] values);
   
   DcmElement putSL(int tag, String value);
   
   DcmElement putSL(int tag, String[] values);
   
   DcmElement putSQ(int tag);
   
   DcmElement putSS(int tag);
   
   DcmElement putSS(int tag, int value);
   
   DcmElement putSS(int tag, int[] values);
   
   DcmElement putSS(int tag, String value);
   
   DcmElement putSS(int tag, String[] values);
   
   DcmElement putST(int tag);
   
   DcmElement putST(int tag, String value);
   
   DcmElement putST(int tag, String[] values);
   
   DcmElement putTM(int tag);
   
   DcmElement putTM(int tag, Date value);
   
   DcmElement putTM(int tag, Date[] values);
   
   DcmElement putTM(int tag, Date from, Date to);
   
   DcmElement putTM(int tag, String value);
   
   DcmElement putTM(int tag, String[] values);
   
   DcmElement putUI(int tag);
   
   DcmElement putUI(int tag, String value);
   
   DcmElement putUI(int tag, String[] values);
   
   DcmElement putUL(int tag);
   
   DcmElement putUL(int tag, int value);
   
   DcmElement putUL(int tag, int[] values);
   
   DcmElement putUL(int tag, String value);
   
   DcmElement putUL(int tag, String[] values);
   
   DcmElement putUN(int tag);
   
   DcmElement putUN(int tag, byte[] value);
   
   DcmElement putUNsq(int tag);
   
   DcmElement putUS(int tag);
   
   DcmElement putUS(int tag, int value);
   
   DcmElement putUS(int tag, int[] values);
   
   DcmElement putUS(int tag, String value);
   
   DcmElement putUS(int tag, String[] values);
   
   DcmElement putUT(int tag);
   
   DcmElement putUT(int tag, String value);
   
   DcmElement putUT(int tag, String[] values);
   
   DcmElement putXX(int tag, int vr);
   
   DcmElement putXX(int tag, int vr, ByteBuffer value);
   
   DcmElement putXX(int tag, int vr, String value);
   
   DcmElement putXX(int tag, int vr, String[] values);
   
   DcmElement putXXsq(int tag, int vr);
   
   DcmElement putXX(int tag);
   
   DcmElement putXX(int tag, ByteBuffer value);
   
   DcmElement putXX(int tag, String value);
   
   DcmElement putXX(int tag, String[] values);
   
   DcmElement putXXsq(int tag);
   
   void putAll(DcmObject dcmObj);
   
   void writeHeader(OutputStream out, DcmEncodeParam encParam, int tag, int vr, int len)
   throws IOException;
   
   void writeHeader(ImageOutputStream iout, DcmEncodeParam encParam, int tag, int vr, int len)
   throws IOException;
}

