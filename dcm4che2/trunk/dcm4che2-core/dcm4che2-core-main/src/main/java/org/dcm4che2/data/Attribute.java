/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.Serializable;
import java.util.Date;

public interface Attribute extends Serializable {
	int tag();
	VR vr();
	boolean bigEndian();
	Attribute bigEndian(boolean bigEndian);
	int length();
	boolean isNull();
	boolean hasItems();
	int countItems();
	byte[] getBytes();
	AttributeSet getItem();
	AttributeSet getItem(int index);
	AttributeSet removeItem(int index);
	AttributeSet addItem(AttributeSet item);
	AttributeSet addItem(int index, AttributeSet item);
	AttributeSet setItem(int index, AttributeSet item);
	byte[] getBytes(int index);
	byte[] removeBytes(int index);
	byte[] addBytes(byte[] b);
	byte[] addBytes(int index, byte[] b);
	byte[] setBytes(int index, byte[] b);	
	int getInt(boolean cache);
	int[] getInts(boolean cache);
	float getFloat(boolean cache);
	float[] getFloats(boolean cache);
	double getDouble(boolean cache);
	double[] getDoubles(boolean cache);
	String getString(SpecificCharacterSet cs, boolean cache);
	String[] getStrings(SpecificCharacterSet cs, boolean cache);
	Date getDate(boolean cache);
	Date[] getDates(boolean cache);
	DateRange getDateRange(boolean cache);
    Attribute share();
	Attribute filterItems(AttributeSet filter);
}
