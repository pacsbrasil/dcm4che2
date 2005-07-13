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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

public interface AttributeSet 
		extends Serializable {
	public interface Visitor {
		boolean visit(Attribute attr);
	}
	AttributeSet getRoot();
	AttributeSet getParent();
	void setParent(AttributeSet parent);
	TransferSyntax getTransferSyntax();	
	SpecificCharacterSet getSpecificCharacterSet();
	Iterator iterator();
	Iterator iterator(int fromTag, int toTag);
	Iterator commandIterator();
	Iterator fileMetaInfoIterator();
	Iterator datasetIterator();
    int getItemPosition();
    void setItemPosition(int pos);
	long getItemOffset();
	void setItemOffset(long offset);
	VR vrOf(int tag);
    String nameOf(int tag);
	int resolvePrivateTag(int privateTag, String privateCreator);
	int reservePrivateTag(int privateTag, String privateCreator);
	String getPrivateCreator(int privateTag);
    boolean isRoot();
	boolean isEmpty();
	int size();
	void clear();
	boolean contains(int tag);
	boolean containsValue(int tag);
	boolean accept(Visitor visitor);
	void addAttribute(Attribute attr);
	Attribute getAttribute(int tag);
	Attribute removeAttribute(int tag);
	byte[] getBytes(int tag, boolean bigEndian);
	AttributeSet getItem(int tag);
	int getInt(int tag);
	int[] getInts(int tag);
	float getFloat(int tag);
	float[] getFloats(int tag);
	double getDouble(int tag);
	double[] getDoubles(int tag);
	String getString(int tag);
	String[] getStrings(int tag);
	Date getDate(int tag);
	Date[] getDates(int tag);
	DateRange getDateRange(int tag);
	Date getDate(int daTag, int tmTag);
	Date[] getDates(int daTag, int tmTag);
	DateRange getDateRange(int daTag, int tmTag);
	AttributeSet getItem(int[] itemPath);
	byte[] getBytes(int[] itemPath, int tag, boolean bigEndian);
	int getInt(int[] itemPath, int tag);
	int[] getInts(int[] itemPath, int tag);
	float getFloat(int[] itemPath, int tag);
	float[] getFloats(int[] itemPath, int tag);
	double getDouble(int[] itemPath, int tag);
	double[] getDoubles(int[] itemPath, int tag);
	String getString(int[] itemPath, int tag);
	String[] getStrings(int[] itemPath, int tag);
	Date getDate(int[] itemPath, int tag);
	Date[] getDates(int[] itemPath, int tag);
	DateRange getDateRange(int[] itemPath, int tag);
	Date getDate(int[] itemPath, int daTag, int tmTag);
	Date[] getDates(int[] itemPath, int daTag, int tmTag);
	DateRange getDateRange(int[] itemPath, int daTag, int tmTag);
	Attribute putNull(int tag, VR vr);
	Attribute putBytes(int tag, VR vr, boolean bigEndian, byte[] val);
	Attribute putItem(int tag, AttributeSet item);
	Attribute putInt(int tag, VR vr, int val);
	Attribute putInts(int tag, VR vr, int[] val);
	Attribute putFloat(int tag, VR vr, float val);
	Attribute putFloats(int tag, VR vr, float[] val);
	Attribute putDouble(int tag, VR vr, double val);
	Attribute putDoubles(int tag, VR vr, double[] val);
	Attribute putString(int tag, VR vr, String val);
	Attribute putStrings(int tag, VR vr, String[] val);
	Attribute putDate(int tag, VR vr, Date val);
	Attribute putDates(int tag, VR vr, Date[] val);
	Attribute putDateRange(int tag, VR vr, DateRange val);
	Attribute putSequence(int tag);
	Attribute putSequence(int tag, int capacity);
	Attribute putFragments(int tag, VR vr, boolean bigEndian);
	Attribute putFragments(int tag, VR vr, boolean bigEndian, int capacity);
	void shareAttributes();
	void serializeAttributes(ObjectOutputStream oos) throws IOException;
	void copyTo(AttributeSet destination);
	boolean match(AttributeSet attrs, boolean ignoreCaseOfPN);
	boolean cacheGet();
	void cacheGet(boolean cacheGet);
	boolean cachePut();
	void cachePut(boolean cachePut);
	AttributeSet command();
	AttributeSet dataset();
	AttributeSet fileMetaInfo();
	AttributeSet subSet(AttributeSet filter);
	AttributeSet subSet(int fromTag, int toTag);
	AttributeSet subSet(int[] tags);
	AttributeSet exclude(int[] tags);
	AttributeSet excludePrivate();
}
