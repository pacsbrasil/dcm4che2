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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

class AttributeSetSerializer implements Serializable {

	private static final long serialVersionUID = 3257002159626139955L;

	private transient AttributeSet attrs;

	private static final int ITEM_DELIM_TAG = 0xfffee00d;
		
	public AttributeSetSerializer(AttributeSet attrs) {
		this.attrs = attrs;
	}
		
	private Object readResolve()
			throws ObjectStreamException {
		return attrs;
	}

	private void writeObject(ObjectOutputStream s)
			throws IOException {
		s.defaultWriteObject();
		DicomOutputStream dos = new DicomOutputStream(s);
		dos.writeAttributes(attrs.iterator(), false, null);
		dos.writeHeader(ITEM_DELIM_TAG, null, 0);
	}

	private void readObject(ObjectInputStream s)
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		DicomInputStream dis = 
				new DicomInputStream(s, TransferSyntax.ExplicitVRLittleEndian);
		attrs = dis.readAttributeSet(-1);
	}
}
