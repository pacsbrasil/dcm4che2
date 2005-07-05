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

	public void testFindNextSiblingRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		AttributeSet rec = r.findFirstRootRecord();
		int count = 0;
		while (rec != null) {
			rec = r.findNextSiblingRecord(rec);
			++count;
		}
		r.close();
		assertEquals(81, count);
	}

	public void testFindFirstMatchingRootRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		AttributeSet filter = new BasicAttributeSet();
		filter.putString(Tag.DirectoryRecordType, VR.CS, "PATIENT");
		filter.putString(Tag.PatientsName, VR.PN, "CHEST*");
		AttributeSet rec = r.findFirstMatchingRootRecord(filter, true);
		assertEquals("Chest^Portable", rec.getString(Tag.PatientsName));
	}

	public void testFindNextMatchingSiblingRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		AttributeSet filter = new BasicAttributeSet();
		filter.putString(Tag.DirectoryRecordType, VR.CS, "STUDY");
		filter.putString(Tag.StudyDate, VR.DA, "-19931213");
		AttributeSet pat = r.findFirstRootRecord();
		int count = 0;
		while (pat != null) {
			AttributeSet sty = r.findFirstMatchingChildRecord(pat, filter, true);
			while (sty != null) {
				++count;
				sty = r.findNextMatchingSiblingRecord(sty, filter, true);
			}
			pat = r.findNextSiblingRecord(pat);
		}
		r.close();
		assertEquals(3, count);
	}

	public void testFindFirstChildRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		AttributeSet rec = r.findFirstRootRecord();
		int count = 0;
		while (rec != null) {
			rec = r.findFirstChildRecord(rec);
			++count;
		}
		r.close();
		assertEquals(4, count);
	}

}
