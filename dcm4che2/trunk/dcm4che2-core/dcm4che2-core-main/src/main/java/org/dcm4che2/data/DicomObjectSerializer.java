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

import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

class DicomObjectSerializer implements Serializable {

	private static final long serialVersionUID = 3257002159626139955L;

	private transient DicomObject attrs;

	public DicomObjectSerializer(DicomObject attrs) {
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
		dos.writeDicomObject(attrs);
	}

	private void readObject(ObjectInputStream s)
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		DicomInputStream dis = 
				new DicomInputStream(s, TransferSyntax.ExplicitVRLittleEndian);
		
		attrs = new BasicDicomObject();
		dis.readDicomObject(attrs, -1);
	}
}
