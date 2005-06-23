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
import java.util.Iterator;

class AttributeSerializer implements Serializable {

	private static final long serialVersionUID = 4051046376018292793L;

	private static final int ITEM_DELIM_TAG = 0xfffee00d;
	
	private static final Attribute END_OF_SET = 
		new BasicAttribute(ITEM_DELIM_TAG, VR.UN, false, null, null);

	private transient AttributeSet attrs;

	public AttributeSerializer(AttributeSet attrs) {
		this.attrs = attrs;
	}
		
	private Object readResolve()
			throws ObjectStreamException {
		return attrs;
	}

	private void writeObject(ObjectOutputStream s)
			throws IOException {
		s.defaultWriteObject();
		s.writeLong(attrs.getItemOffset());
		Iterator it = attrs.iterator();
		while (it.hasNext()) {
			s.writeObject(it.next());			
		}
		s.writeObject(END_OF_SET);
	}

	private void readObject(ObjectInputStream s)
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		attrs = new BasicAttributeSet();
		attrs.setItemOffset(s.readLong());
		Attribute attr = (Attribute) s.readObject();
		while (attr.tag() != ITEM_DELIM_TAG) {
			if (attr.vr() == VR.SQ && attr.hasItems()) {
				for (int i = 0, n = attr.countItems(); i < n; ++i) {
					attr.getItem(i).setParent(attrs);
				}
			}
			((BasicAttributeSet) attrs).putAttribute(attr);
			attr = (Attribute) s.readObject();
		}
	}
}
