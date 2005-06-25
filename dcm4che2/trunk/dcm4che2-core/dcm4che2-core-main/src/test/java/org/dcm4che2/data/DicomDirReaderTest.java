package org.dcm4che2.data;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class DicomDirReaderTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(DicomDirReaderTest.class);
	}

	public DicomDirReaderTest(String arg0) {
		super(arg0);
	}

	private File locateFile(String name) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return new File(cl.getResource(name).toString().substring(5));
	}

	public void testReadNextSiblingRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		AttributeSet rec = r.readFirstRootRecord();
		int count = 0;
		while (rec != null) {
			rec = r.readNextSiblingRecord(rec);
			++count;
		}
		r.close();
		assertEquals(81, count);
	}

	public void testReadFirstChildRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		AttributeSet rec = r.readFirstRootRecord();
		int count = 0;
		while (rec != null) {
			rec = r.readFirstChildRecord(rec);
			++count;
		}
		r.close();
		assertEquals(4, count);
	}

}
