/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG                             *
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Date;

/** Element in <code>DcmObject</code>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 5: Data Structures and Encoding, 7.1 Data Elements"
 * @see "DICOM Part 7: Message Exchange, 6.3.1 Command Set Structure"
 */
public interface DcmElement extends Comparable {
    
    public int tag();
    
    public int vr();
    
    public int vm();

    public int length();
    
    public boolean isEmpty();

    public int hashCode();
    
    public int compareTo(Object o);
        
    public ByteBuffer getByteBuffer();

    public ByteBuffer getByteBuffer(ByteOrder byteOrder);

    public ByteBuffer getDataFragment(int index);

    public ByteBuffer getDataFragment(int index, ByteOrder byteOrder);

    public int getDataFragmentLength(int index);

    public String getString(Charset cs) throws DcmValueException;
    
    public String getString(int index, Charset cs) throws DcmValueException;
 
    public String[] getStrings(Charset cs) throws DcmValueException;
    
    public int getInt() throws DcmValueException;
    
    public int getInt(int index) throws DcmValueException;
 
    public int[] getInts() throws DcmValueException;
 
    public int getTag() throws DcmValueException;
    
    public int getTag(int index) throws DcmValueException;
 
    public int[] getTags() throws DcmValueException;
 
    public float getFloat() throws DcmValueException;
    
    public float getFloat(int index) throws DcmValueException;

    public float[] getFloats() throws DcmValueException;
 
    public double getDouble() throws DcmValueException;
    
    public double getDouble(int index) throws DcmValueException;

    public double[] getDoubles() throws DcmValueException;
 
    public Date getDate() throws DcmValueException;
    
    public Date getDate(int index) throws DcmValueException;

    public Date[] getDates() throws DcmValueException;

    public void addDataFragment(ByteBuffer byteBuffer);
    
    public Dataset addNewDataset();

    public Dataset getDataset();

    public Dataset getDataset(int index);
    
    public DcmElement setStreamPosition(long streamPos);

    public long getStreamPosition();
}
