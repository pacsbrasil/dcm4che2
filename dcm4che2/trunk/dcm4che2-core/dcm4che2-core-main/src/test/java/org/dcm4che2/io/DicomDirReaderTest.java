package org.dcm4che2.io;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomDirReader;

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
		DicomObject rec = r.findFirstRootRecord();
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
		DicomObject filter = new BasicDicomObject();
		filter.putString(Tag.DirectoryRecordType, VR.CS, "PATIENT");
		filter.putString(Tag.PatientsName, VR.PN, "CHEST*");
		DicomObject rec = r.findFirstMatchingRootRecord(filter, true);
		assertEquals("Chest^Portable", rec.getString(Tag.PatientsName));
	}

	public void testFindNextMatchingSiblingRecord() throws IOException {
		DicomDirReader r = new DicomDirReader(locateFile("DICOMDIR"));
		DicomObject filter = new BasicDicomObject();
		filter.putString(Tag.DirectoryRecordType, VR.CS, "STUDY");
		filter.putString(Tag.StudyDate, VR.DA, "-19931213");
		DicomObject pat = r.findFirstRootRecord();
		int count = 0;
		while (pat != null) {
			DicomObject sty = r.findFirstMatchingChildRecord(pat, filter, true);
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
		DicomObject rec = r.findFirstRootRecord();
		int count = 0;
		while (rec != null) {
			rec = r.findFirstChildRecord(rec);
			++count;
		}
		r.close();
		assertEquals(4, count);
	}

}
