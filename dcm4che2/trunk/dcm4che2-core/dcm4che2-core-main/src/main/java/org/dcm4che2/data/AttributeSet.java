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
import java.util.Iterator;

public interface AttributeSet 
		extends Serializable {
	AttributeSet getRoot();
	AttributeSet getParent();
	void setParent(AttributeSet parent);
	TransferSyntax getTransferSyntax();	
	SpecificCharacterSet getSpecificCharacterSet();
	Iterator iterator();
	Iterator iterator(int fromTag, int toTag);
	long getItemOffset();
	void setItemOffset(long offset);
	VR vrOf(int tag);
	int resolvePrivateTag(int privateTag, String privateCreator);
	int reservePrivateTag(int privateTag, String privateCreator);
	String getPrivateCreator(int privateTag);
	boolean contains(int tag);
	boolean containsValue(int tag);
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
	Attribute putSequence(int tag);
	Attribute putSequence(int tag, int capacity);
	Attribute putFragments(int tag, VR vr, boolean bigEndian);
	Attribute putFragments(int tag, VR vr, boolean bigEndian, int capacity);
	void shareAttributes();
	void serializeAttributes(ObjectOutputStream oos) throws IOException;
	void addAll(AttributeSet other);
	boolean isCached();
	void setCached(boolean cached);
	AttributeSet subSet(AttributeSet filter);
	AttributeSet subSet(int fromTag, int toTag);
	AttributeSet subSet(int[] tags);
	AttributeSet exclude(int[] tags);
	AttributeSet excludePrivate();
}
