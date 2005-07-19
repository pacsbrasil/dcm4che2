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

import org.dcm4che2.io.DicomInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DicomObjectTest extends TestCase {

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public DicomObjectTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(DicomObjectTest.class);
	}

	public void testSerialize() throws IOException, ClassNotFoundException {
		DicomObject dicomdir = load("DICOMDIR");
		File ofile = new File("target/test-out/DICOMDIR.dcm.ser");
		ofile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(ofile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(dicomdir);
		oos.close();
		FileInputStream fis = new FileInputStream(ofile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		DicomObject dicomdir2 = (DicomObject) ois.readObject();
		ois.close();
		assertEquals(dicomdir, dicomdir2);
	}

	public void testSerializeElements() throws IOException,
			ClassNotFoundException {
		DicomObject dicomdir = load("DICOMDIR");
		File ofile = new File("target/test-out/DICOMDIR.attr.ser");
		ofile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(ofile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		dicomdir.serializeElements(oos);
		oos.close();
		FileInputStream fis = new FileInputStream(ofile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		DicomObject dicomdir2 = (DicomObject) ois.readObject();
		ois.close();
		assertEquals(dicomdir, dicomdir2);
	}

	private DicomObject load(String fname) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		DicomInputStream dis = new DicomInputStream(new BufferedInputStream(cl
				.getResourceAsStream(fname)));
		try {
			DicomObject attrs = new BasicDicomObject();
			dis.readDicomObject(attrs, -1);
			return attrs;
		} finally {
			dis.close();
		}
	}
}
