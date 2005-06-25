/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AttributeSetTest extends TestCase {

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public AttributeSetTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AttributeSetTest.class);
	}

	public void testSerialize() throws IOException, ClassNotFoundException {
		AttributeSet dicomdir = load("DICOMDIR");
		File ofile = new File("target/test-out/DICOMDIR.dcm.ser");
		ofile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(ofile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(dicomdir);
		oos.close();
		FileInputStream fis = new FileInputStream(ofile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		AttributeSet dicomdir2 = (AttributeSet) ois.readObject();
		ois.close();
		assertEquals(dicomdir, dicomdir2);
	}

	public void testSerializeAttributes() throws IOException,
			ClassNotFoundException {
		AttributeSet dicomdir = load("DICOMDIR");
		File ofile = new File("target/test-out/DICOMDIR.attr.ser");
		ofile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(ofile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		dicomdir.serializeAttributes(oos);
		oos.close();
		FileInputStream fis = new FileInputStream(ofile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		AttributeSet dicomdir2 = (AttributeSet) ois.readObject();
		ois.close();
		assertEquals(dicomdir, dicomdir2);
	}

	private AttributeSet load(String fname) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		DicomInputStream dis = new DicomInputStream(new BufferedInputStream(cl
				.getResourceAsStream(fname)));
		try {
			AttributeSet attrs = new BasicAttributeSet();
			dis.readAttributeSet(attrs, -1);
			return attrs;
		} finally {
			dis.close();
		}
	}
}
