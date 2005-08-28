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
import java.util.regex.Pattern;

public interface DicomElement extends Serializable {
	int tag();
	VR vr();
    int vm(SpecificCharacterSet cs);
	boolean bigEndian();
	DicomElement bigEndian(boolean bigEndian);
	int length();
	boolean isEmpty();
	boolean hasItems();
	int countItems();
	byte[] getBytes();
	DicomObject getItem();
	DicomObject getItem(int index);
	DicomObject removeItem(int index);
	DicomObject addItem(DicomObject item);
	DicomObject addItem(int index, DicomObject item);
	DicomObject setItem(int index, DicomObject item);
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
	Pattern getPattern(SpecificCharacterSet cs, boolean ignoreCase,
			boolean cache);
    DicomElement share();
	DicomElement filterItems(DicomObject filter);
}
