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

import org.dcm4che2.data.DicomObject.Visitor;

class ElementSerializer implements Serializable {

	private static final long serialVersionUID = 4051046376018292793L;

	private static final DicomElement END_OF_SET = 
		new BasicDicomElement(Tag.ItemDelimitationItem, VR.UN, false, null, null);

	private transient DicomObject attrs;

	public ElementSerializer(DicomObject attrs) {
		this.attrs = attrs;
	}
		
	private Object readResolve()
			throws ObjectStreamException {
		return attrs;
	}

	private void writeObject(final ObjectOutputStream s)
			throws IOException {
		s.defaultWriteObject();
		s.writeLong(attrs.getItemOffset());
		try {
			attrs.accept(new Visitor() {
				public boolean visit(DicomElement attr) {
					try {
						s.writeObject(attr);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return true;
				}
			});
		} catch (Exception e) {
			if (e.getCause() instanceof IOException)
				throw (IOException) e.getCause();
		}
		s.writeObject(END_OF_SET);
	}

	private void readObject(ObjectInputStream s)
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		attrs = new BasicDicomObject();
		attrs.setItemOffset(s.readLong());
		DicomElement attr = (DicomElement) s.readObject();
		while (attr.tag() != Tag.ItemDelimitationItem) {
			if (attr.vr() == VR.SQ && attr.hasItems()) {
				for (int i = 0, n = attr.countItems(); i < n; ++i) {
					attr.getItem(i).setParent(attrs);
				}
			}
			((BasicDicomObject) attrs).addInternal(attr);
			attr = (DicomElement) s.readObject();
		}
	}
}
