/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DicomOutputStreamTest extends TestCase {

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
	public DicomOutputStreamTest(String testName) {
		super(testName);
	}

    public static Test suite() {
        return new TestSuite( DicomOutputStreamTest.class );
    }

    public void testWriteDICOMDIR() throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String fpath = cl.getResource("DICOMDIR").getPath();
		File ifile = new File(fpath);
		DicomInputStream dis = new DicomInputStream(ifile);
		DicomObject attrs = new BasicDicomObject();
		dis.readDicomObject(attrs, -1);
		dis.close();
		attrs.putString(0x00020010, VR.CS, TransferSyntax.ExplicitVRLittleEndian.uid());
		File ofile = new File("target/test-out/DICOMDIR");
		ofile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(ofile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		DicomOutputStream dos = new DicomOutputStream(bos);
		dos.writeDicomFile(attrs);
		dos.close();
    }
}
