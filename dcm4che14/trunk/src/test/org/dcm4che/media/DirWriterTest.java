/* $Id$ */
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.media;

import org.dcm4che.dict.*;
import org.dcm4che.data.*;
import junit.framework.*;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Date;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public class DirWriterTest extends TestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    private static final String CLASS_UID = "1.2.840.10008.1.3.10";
    private static final String INST_UID = "1.2.40.0.13.1.1.99";
    private static final String TS_UID = "1.2.840.10008.1.2.1";
    private static final String FILE_SET_ID = "FILE_SET_ID";
    
    public static Test suite() {
        return new TestSuite(DirWriterTest.class);
    }
    
    /**
     * Creates new DictionaryTest
     */
    public DirWriterTest(String name) {
        super(name);
    }
    
    private File theFile;
    private File IMG1_FILE;
    private File IMG2_FILE;
    
    protected void setUp() throws Exception {
        theFile = File.createTempFile("dcm4che",".dcm");
        File dir = theFile.getParentFile();
        IMG1_FILE = new File(new File(dir, "DIR1"),"IMG1_FILE");
        IMG2_FILE = new File(new File(dir, "DIR2"),"IMG2_FILE");
    }
    
    protected void tearDown() throws Exception {
        theFile.delete();
    }
    
    private void checkFilesetIDs(DirReader r) throws Exception {
        Dataset fsi = r.getFileSetInfo();
        FileMetaInfo fmi = fsi.getFileMetaInfo();
        assertEquals(INST_UID, fmi.getMediaStorageSOPInstanceUID());
        assertEquals(CLASS_UID, fmi.getMediaStorageSOPClassUID());
        assertEquals(TS_UID, fmi.getTransferSyntaxUID());
        assertEquals(FILE_SET_ID, fsi.getString(Tags.FileSetID));
        assertEquals(0,fsi.getInt(Tags.FileSetConsistencyFlag, -1));
    }
    
    private static final String CHARSET = "ISO_IR 100";
    private static final String PAT1_ID = "P1234";
    private static final String PAT2_ID = "P5678";
    private static final String PAT3_ID = "P9999";
    private static final String PAT1_NAME = "PAT1^NAME";
    private static final String PAT2_NAME = "PAT2^NAME";
    private static final String PAT3_NAME = "PAT3^NAME";
    private static final String STUDY1_ID = "S1111";
    private static final String STUDY2_ID = "S2222";
    private static final String STUDY1_UID = "1.2.40.0.13.1.1.99.1111";
    private static final String STUDY2_UID = "1.2.40.0.13.1.1.99.2222";
    private static final String STUDY1_DESC = "STUDY1_DESC";
    private static final String STUDY2_DESC = "STUDY2_DESC";
    private static final String ACC_NO = "A7777";
    private static final int SERIES1_NO = 1;
    private static final int SERIES2_NO = 2;
    private static final int SERIES3_NO = 3;
    private static final String SERIES1_UID = "1.2.40.0.13.1.1.99.1111.1";
    private static final String SERIES2_UID = "1.2.40.0.13.1.1.99.1111.2";
    private static final String SERIES3_UID = "1.2.40.0.13.1.1.99.1111.3";
    private static final int IMG1_NO = 1;
    private static final int IMG2_NO = 2;
    private static final String IMG1_UID = "1.2.40.0.13.1.1.99.1111.1.1";
    private static final String IMG2_UID = "1.2.40.0.13.1.1.99.1111.1.2";
    private static final String CT_UID = "1.2.840.10008.5.1.4.1.1.2";

    private final DirBuilderFactory wfact = DirBuilderFactory.getInstance();
    private final DcmObjectFactory dsfact = DcmObjectFactory.getInstance();
    private Dataset newPatient(String id, String name) throws Exception {
        Dataset pat = dsfact.newDataset();
        pat.setCS(Tags.SpecificCharacterSet, CHARSET);
        pat.setLO(Tags.PatientID, id);
        pat.setPN(Tags.PatientName, name);
        return pat;
    }        
    
    private void checkPatient(Dataset pat, String id, String name)
            throws Exception {
        assertEquals(CHARSET, pat.getString(Tags.SpecificCharacterSet, null));
        assertEquals(id, pat.getString(Tags.PatientID, null));
        assertEquals(name, pat.getString(Tags.PatientName, null));
    }        

    private Dataset newStudy(String id, String uid, Date date, String desc,
            String accNo) throws Exception {
        Dataset study = dsfact.newDataset();
        study.setCS(Tags.SpecificCharacterSet, CHARSET);
        study.setSH(Tags.StudyID, id);
        study.setUI(Tags.StudyInstanceUID, uid);
        study.setLO(Tags.StudyDescription, desc);
        study.setSH(Tags.AccessionNumber, accNo);
        study.setDA(Tags.StudyDate, date);
        study.setTM(Tags.StudyTime, date);
        return study;
    }        

    private void checkStudy(Dataset study, String id, String uid, String desc,
            String accNo) throws Exception {
        assertEquals(CHARSET, study.getString(Tags.SpecificCharacterSet, null));
        assertEquals(id, study.getString(Tags.StudyID, null));
        assertEquals(uid, study.getString(Tags.StudyInstanceUID, null));
        assertEquals(desc, study.getString(Tags.StudyDescription, null));
        assertEquals(accNo, study.getString(Tags.AccessionNumber, null));
    }        

    private Dataset newSeries(String md, int no, String uid)
            throws Exception {
        Dataset series = dsfact.newDataset();
        series.setCS(Tags.Modality, md);
        series.setIS(Tags.SeriesNumber, no);
        series.setUI(Tags.SeriesInstanceUID, uid);
        return series;
    }
    
    private void checkSeries(Dataset series, String md, int no, String uid)
            throws Exception {
        assertEquals(md, series.getString(Tags.Modality, null));
        assertEquals(no, series.getInt(Tags.SeriesNumber, -1));
        assertEquals(uid, series.getString(Tags.SeriesInstanceUID, null));
    }
    
    private Dataset newImage(int no)
            throws Exception {
        Dataset img = dsfact.newDataset();
        img.setIS(Tags.InstanceNumber, no);
        return img;
    }
    
    private void checkImage(Dataset img, int no)
            throws Exception {
        assertEquals(no, img.getInt(Tags.InstanceNumber, -1));
    }

    public void testAddRecord000() throws Exception {
        doTestAddRecord(false, false, false);
    }

    public void testAddRecord001() throws Exception {
        doTestAddRecord(false, false, true);
    }

    public void testAddRecord010() throws Exception {
        doTestAddRecord(false, true, false);
    }

    public void testAddRecord011() throws Exception {
        doTestAddRecord(false, true, true);
    }

    public void testAddRecord100() throws Exception {
        doTestAddRecord(true, false, false);
    }

    public void testAddRecord101() throws Exception {
        doTestAddRecord(true, false, true);
    }

    public void testAddRecord110() throws Exception {
        doTestAddRecord(true, true, false);
    }

    public void testAddRecord111() throws Exception {
        doTestAddRecord(true, true, true);
    }


    private void doTestAddRecord(boolean skipGroupLen, boolean undefSeqLen,
            boolean undefItemLen) throws Exception {
        DcmEncodeParam encParam = new DcmEncodeParam(ByteOrder.LITTLE_ENDIAN,
                true, false, skipGroupLen, undefSeqLen, undefItemLen);
        DirWriter w1 = wfact.newDirWriter(theFile, INST_UID,  FILE_SET_ID,
                null, null, encParam);
        try {
            DirRecord patRec1 = w1.addRecord(null, "PATIENT",
                    newPatient(PAT1_ID, PAT1_NAME));
            DirRecord studyRec1 = w1.addRecord(patRec1, "STUDY",
                    newStudy(STUDY1_ID, STUDY1_UID, new Date(), STUDY1_DESC,
                            ACC_NO));
            DirRecord seriesRec1 = w1.addRecord(studyRec1, "SERIES",
                    newSeries("CT", SERIES1_NO, SERIES1_UID));
            DirRecord imgRec1 = w1.addRecord(seriesRec1, "IMAGE",
                    newImage(IMG1_NO),  w1.toFileIDs(IMG1_FILE), CT_UID,
                    IMG1_UID, TS_UID);
            DirRecord seriesRec2 = w1.addRecord(studyRec1, "SERIES",
                    newSeries("CT", SERIES2_NO, SERIES2_UID));
            DirRecord imgRec2 = w1.addRecord(seriesRec2, "IMAGE",
                    newImage(IMG2_NO), w1.toFileIDs(IMG2_FILE), CT_UID,
                    IMG2_UID,  TS_UID);
            DirRecord patRec2 = w1.addRecord(null, "PATIENT",
                    newPatient(PAT2_ID, PAT2_NAME));
        } finally {
            w1.close();
        }
        DirWriter w2 = wfact.newDirWriter(theFile, encParam);
        try {
            DirRecord patRec1 = w2.getFirstRecord();
            DirRecord patRec2 = patRec1.getNextSibling();
            DirRecord studyRec1 = patRec1.getFirstChild();
            Dataset study2 =
                    newStudy(STUDY2_ID, STUDY2_UID, new Date(), STUDY2_DESC,
                            ACC_NO);
            Dataset series3 = newSeries("MR", SERIES3_NO, SERIES3_UID);
            w2.addRecord(null, "PATIENT", newPatient(PAT3_ID, PAT3_NAME));
            w2.addRecord(patRec2, "STUDY", study2);
            w2.addRecord(studyRec1, "SERIES", series3);
            w2.rollback();
            DirRecord studyRec2 = w2.addRecord(patRec1, "STUDY", study2);
            w2.addRecord(studyRec2, "SERIES", series3);
       } finally {
            w2.close();
        }
        DirWriter w3 = wfact.newDirWriter(theFile, null);
        try {
            checkFilesetIDs(w3);
            DirRecord patRec1 = w3.getFirstRecord();
            assertNotNull(patRec1);
            assertEquals("PATIENT",patRec1.getType());
            assertEquals(DirRecord.IN_USE,patRec1.getInUseFlag());
            checkPatient(patRec1.getDataset(), PAT1_ID, PAT1_NAME);
            DirRecord studyRec1 = patRec1.getFirstChild();
            assertNotNull(studyRec1);
            assertEquals("STUDY",studyRec1.getType());
            assertEquals(DirRecord.IN_USE,studyRec1.getInUseFlag());
            checkStudy(studyRec1.getDataset(), STUDY1_ID, STUDY1_UID,
                    STUDY1_DESC, ACC_NO);
            DirRecord seriesRec1 = studyRec1.getFirstChild();
            assertNotNull(seriesRec1);
            assertEquals("SERIES",seriesRec1.getType());
            assertEquals(DirRecord.IN_USE,seriesRec1.getInUseFlag());
            checkSeries(seriesRec1.getDataset(), "CT", SERIES1_NO, SERIES1_UID);
            DirRecord imageRec1 = seriesRec1.getFirstChild();
            assertNotNull(imageRec1);
            assertEquals("IMAGE",imageRec1.getType());
            assertEquals(DirRecord.IN_USE,imageRec1.getInUseFlag());
            assertEquals(IMG1_FILE,w3.getRefFile(imageRec1.getRefFileIDs()));
            assertEquals(CT_UID,imageRec1.getRefSOPClassUID());
            assertEquals(IMG1_UID,imageRec1.getRefSOPInstanceUID());
            assertEquals(TS_UID,imageRec1.getRefSOPTransferSyntaxUID());
            checkImage(imageRec1.getDataset(), IMG1_NO);
            assertNull(imageRec1.getNextSibling());
            DirRecord seriesRec2 = seriesRec1.getNextSibling();
            assertNotNull(seriesRec2);
            assertEquals("SERIES",seriesRec2.getType());
            assertEquals(DirRecord.IN_USE,seriesRec2.getInUseFlag());
            checkSeries(seriesRec2.getDataset(), "CT", SERIES2_NO, SERIES2_UID);
            DirRecord imageRec2 = seriesRec2.getFirstChild();
            assertNotNull(imageRec2);
            assertEquals("IMAGE",imageRec2.getType());
            assertEquals(DirRecord.IN_USE,imageRec2.getInUseFlag());
            assertEquals(IMG2_FILE,w3.getRefFile(imageRec2.getRefFileIDs()));
            assertEquals(CT_UID,imageRec2.getRefSOPClassUID());
            assertEquals(IMG2_UID,imageRec2.getRefSOPInstanceUID());
            assertEquals(TS_UID,imageRec2.getRefSOPTransferSyntaxUID());
            checkImage(imageRec2.getDataset(), IMG2_NO);
            assertNull(imageRec2.getNextSibling());
            assertNull(seriesRec2.getNextSibling());
            DirRecord studyRec2 = studyRec1.getNextSibling();
            assertNotNull(studyRec2);
            assertEquals("STUDY",studyRec2.getType());
            assertEquals(DirRecord.IN_USE,studyRec2.getInUseFlag());
            checkStudy(studyRec2.getDataset(), STUDY2_ID, STUDY2_UID,
                    STUDY2_DESC, ACC_NO);
            DirRecord seriesRec3 = studyRec2.getFirstChild();
            assertNotNull(seriesRec3);
            assertEquals("SERIES",seriesRec3.getType());
            assertEquals(DirRecord.IN_USE,seriesRec3.getInUseFlag());
            checkSeries(seriesRec3.getDataset(), "MR", SERIES3_NO, SERIES3_UID);
            assertNull(seriesRec3.getFirstChild());
            assertNull(seriesRec3.getNextSibling());
            assertNull(studyRec2.getNextSibling());
            DirRecord patRec2 = patRec1.getNextSibling();
            assertNotNull(patRec2);
            assertEquals("PATIENT",patRec2.getType());
            assertEquals(DirRecord.IN_USE,patRec2.getInUseFlag());
            checkPatient(patRec2.getDataset(), PAT2_ID, PAT2_NAME);
            assertNull(patRec2.getFirstChild());
            assertNull(patRec2.getNextSibling());
        } finally {
            w3.close();
        }
    }
    
    
}//end class DirReaderTest
