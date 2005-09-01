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

public interface DicomObject 
		extends Serializable {
	public interface Visitor {
		boolean visit(DicomElement attr);
	}
	DicomObject getRoot();
	DicomObject getParent();
	void setParent(DicomObject parent);
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
    int vm(int tag);
	boolean contains(int tag);
	boolean containsValue(int tag);
	boolean accept(Visitor visitor);
	void add(DicomElement attr);
	DicomElement remove(int tag);
	DicomElement get(int tag);
    DicomElement get(int[] tagPath);
    DicomElement get(String tagPath);
	byte[] getBytes(int tag, boolean bigEndian);
	byte[] getBytes(int[] tagPath, boolean bigEndian);
    byte[] getBytes(String tagPath, boolean bigEndian);
	DicomObject getItem(int tag);
	DicomObject getItem(int[] itemPath);
    DicomObject getItem(String itemPath);
	int getInt(int tag);
	int getInt(int[] tagPath);
    int getInt(String tagPath);
    int[] getInts(int tag);
	int[] getInts(int[] tagPath);
    int[] getInts(String tagPath);
    float getFloat(int tag);
	float getFloat(int[] tagPath);
    float getFloat(String tagPath);
	float[] getFloats(int tag);
	float[] getFloats(int[] tagPath);
    float[] getFloats(String tagPath);
	double getDouble(int tag);
	double getDouble(int[] tagPath);
    double getDouble(String tagPath);
	double[] getDoubles(int tag);
	double[] getDoubles(int[] tagPath);
    double[] getDoubles(String tagPath);
    String getString(int tag);
	String getString(int[] tagPath);
    String getString(String tagPath);
	String[] getStrings(int tag);
	String[] getStrings(int[] tagPath);
    String[] getStrings(String tagPath);
    Date getDate(int tag);
	Date getDate(int daTag, int tmTag);
	Date getDate(int[] tagPath);
	Date getDate(int[] itemPath, int daTag, int tmTag);
    Date getDate(String tagPath);
    Date getDate(String itemPath, int daTag, int tmTag);
	Date[] getDates(int tag);
	Date[] getDates(int daTag, int tmTag);
	Date[] getDates(int[] tagPath);
	Date[] getDates(int[] itemPath, int daTag, int tmTag);
    Date[] getDates(String tagPath);
    Date[] getDates(String itemPath, int daTag, int tmTag);
	DateRange getDateRange(int tag);
	DateRange getDateRange(int daTag, int tmTag);
	DateRange getDateRange(int[] tagPath);
	DateRange getDateRange(int[] itemPath, int daTag, int tmTag);
    DateRange getDateRange(String tagPath);
    DateRange getDateRange(String itemPath, int daTag, int tmTag);
	DicomElement putNull(int tag, VR vr);
	DicomElement putBytes(int tag, VR vr, boolean bigEndian, byte[] val);
	DicomElement putItem(int tag, DicomObject item);
	DicomElement putInt(int tag, VR vr, int val);
	DicomElement putInts(int tag, VR vr, int[] val);
	DicomElement putFloat(int tag, VR vr, float val);
	DicomElement putFloats(int tag, VR vr, float[] val);
	DicomElement putDouble(int tag, VR vr, double val);
	DicomElement putDoubles(int tag, VR vr, double[] val);
	DicomElement putString(int tag, VR vr, String val);
	DicomElement putStrings(int tag, VR vr, String[] val);
	DicomElement putDate(int tag, VR vr, Date val);
	DicomElement putDates(int tag, VR vr, Date[] val);
	DicomElement putDateRange(int tag, VR vr, DateRange val);
	DicomElement putSequence(int tag);
	DicomElement putSequence(int tag, int capacity);
	DicomElement putFragments(int tag, VR vr, boolean bigEndian);
	DicomElement putFragments(int tag, VR vr, boolean bigEndian, int capacity);
	void shareElements();
	void serializeElements(ObjectOutputStream oos) throws IOException;
	void copyTo(DicomObject destination);
	boolean matches(DicomObject keys, boolean ignoreCaseOfPN);
	boolean cacheGet();
	void cacheGet(boolean cacheGet);
	boolean cachePut();
	void cachePut(boolean cachePut);
	DicomObject command();
	DicomObject dataset();
	DicomObject fileMetaInfo();
	DicomObject subSet(DicomObject filter);
	DicomObject subSet(int fromTag, int toTag);
	DicomObject subSet(int[] tags);
	DicomObject exclude(int[] tags);
	DicomObject excludePrivate();
    void initFileMetaInformation(String tsuid);
}
