/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package org.dcm4che.data;

import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Dataset;

import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.DictionaryFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.*;
import junit.framework.*;

/**
 *
 * @author gunter.zeilinger@tiani.com
 */                                
public class DatasetTest extends TestCase {
    
    public DatasetTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DatasetTest.class);
        return suite;
    }

    private static final String EVR_LE = "../testdata/sr/examplef9.dcm";
    private static final String EVR_LE_XML = "../testdata/sr/examplef9.xml";
    private static final String DICOMDIR = "../testdata/dir/DICOMDIR";
    private static final String DICOMDIR_XML = "../testdata/dir/DICOMDIR.xml";
    private static final String PART10_EVR_LE = "../testdata/img/6AF8_10";
    private Dataset ds;
    private TagDictionary dict;
    
    protected void setUp() throws Exception {
        dict = DictionaryFactory.getInstance().getDefaultTagDictionary();
        ds = DcmObjectFactory.getInstance().newDataset();
    }
   
    public void testEVR_LE() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(EVR_LE)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testDICOMDIR() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(DICOMDIR)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testPART10_EVR_LE() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(PART10_EVR_LE)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testWriteXML() throws Exception {
        testEVR_LE();
        SAXTransformerFactory tf =
            (SAXTransformerFactory)TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT,"yes");
        th.setResult(new StreamResult(new File(EVR_LE_XML)));
        ds.writeFile(th, dict);        
    }
    
    public void testWriteXML2() throws Exception {
        testDICOMDIR();
        SAXTransformerFactory tf =
            (SAXTransformerFactory)TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT,"yes");
        th.setResult(new StreamResult(new File(DICOMDIR_XML)));
        ds.writeFile(th, dict);      
    }

    public void testSAXHandler() throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        p.parse(new File(EVR_LE_XML), ds.getSAXHandler());
    }

    public void testSAXHandler2() throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        p.parse(new File(DICOMDIR_XML), ds.getSAXHandler());
    }

    private static final String[] SCHEDULED_STATION_AET = { "AET1", "AET2" };
    private static final String[] PATIENT_AGE = { "040Y" };
    private static final String[] IMAGE_TYPE = { "ORIGINAL", "PRIMARY" };
    private static final String[] STUDY_DATE = { "19700101" };
    private static final String[] STUDY_TIME = { "010140" };
    private static final String[] ACQUISITION_DATETIME = { "19700101010140" };
    private static final String[] IMAGE_POSITION = { "1.23E+02", "-456", "78.9" };
    private static final String[] ANCHOR_POINT = {
                String.valueOf(123.4f), String.valueOf(-56.78f) };
    private static final String[] TABLE_OF_Y_BREAK_POINTS = {
                String.valueOf(1.2345), String.valueOf(-6.78901) };
    private static final String[] REF_FRAME_NUMBER = { "3", "7", "13" };
    private static final String[] OTHER_PATIENT_IDS = { "PAT_ID1", "PAT_ID2"};
    private static final String[] ADDITIONAL_PATIENT_HISTORY = { 
                "ADDITIONAL PATIENT HISTORY" };
    private static final String[] OTHER_PATIENT_NAMES = {
                "PAT1^NAME", "PAT2^NAME"};
    private static final String[] ACCESSION_NUMBER = { "A-23456" };
    private static final String[] DISPLAYED_AREA_BRHC = {
                String.valueOf(123000), String.valueOf(-456000) };
    private static final String[] OVERLAY_ORIGIN = {
                String.valueOf(123), String.valueOf(-456) };
    private static final String[] DERIVATION_DESCRIPTION = {
                "Derivation Description" };
    private static final String[] SOP_CLASSES_SUPPORTED = {
                "1.2.840.10008.5.1.1.14", "1.2.840.10008.5.1.1.16" };
    private static final String[] REF_SAMPLE_POSITIONS = {
                String.valueOf(123000), String.valueOf(456000) };
    private static final String[] TEXT_VALUE = { "Text Value" };
         
    private void setStrings() {
        ds.putAE(Tags.ScheduledStationAET, SCHEDULED_STATION_AET);
        ds.putAS(Tags.PatientAge, PATIENT_AGE);
        ds.putCS(Tags.ImageType, IMAGE_TYPE);
        ds.putDA(Tags.StudyDate, STUDY_DATE);
        ds.putDS(Tags.ImagePosition, IMAGE_POSITION);
        ds.putDT(Tags.AcquisitionDatetime, ACQUISITION_DATETIME);
        ds.putFL(Tags.AnchorPoint, ANCHOR_POINT);
        ds.putFD(Tags.TableOfYBreakPoints, TABLE_OF_Y_BREAK_POINTS);
        ds.putIS(Tags.RefFrameNumber, REF_FRAME_NUMBER);
        ds.putLO(Tags.OtherPatientIDs, OTHER_PATIENT_IDS);
        ds.putLT(Tags.AdditionalPatientHistory, ADDITIONAL_PATIENT_HISTORY);
        ds.putPN(Tags.OtherPatientNames, OTHER_PATIENT_NAMES);
        ds.putSH(Tags.AccessionNumber, ACCESSION_NUMBER);
        ds.putSL(Tags.DisplayedAreaBottomRightHandCorner, DISPLAYED_AREA_BRHC);
        ds.putSS(Tags.OverlayOrigin, OVERLAY_ORIGIN);
        ds.putST(Tags.DerivationDescription, DERIVATION_DESCRIPTION);
        ds.putTM(Tags.StudyTime, STUDY_TIME);
        ds.putUI(Tags.SOPClassesSupported, SOP_CLASSES_SUPPORTED);
        ds.putUL(Tags.RefSamplePositions, REF_SAMPLE_POSITIONS);
        ds.putUS(Tags.RefFrameNumbers, REF_FRAME_NUMBER);
        ds.putUT(Tags.TextValue, TEXT_VALUE);
    }
    
    public void testGetString() throws Exception {
        setStrings();
        assertEquals(SCHEDULED_STATION_AET[0],
                ds.getString(Tags.ScheduledStationAET));
        assertEquals(PATIENT_AGE[0], ds.getString(Tags.PatientAge));
        assertEquals(IMAGE_TYPE[0], ds.getString(Tags.ImageType));
        assertEquals(STUDY_DATE[0], ds.getString(Tags.StudyDate));
        assertEquals(STUDY_TIME[0], ds.getString(Tags.StudyTime).substring(0,6));
        assertEquals(ACQUISITION_DATETIME[0],
                ds.getString(Tags.AcquisitionDatetime));
        assertEquals(IMAGE_POSITION[0], ds.getString(Tags.ImagePosition));
        assertEquals(ANCHOR_POINT[0], ds.getString(Tags.AnchorPoint));
        assertEquals(TABLE_OF_Y_BREAK_POINTS[0],
                ds.getString(Tags.TableOfYBreakPoints));
        assertEquals(REF_FRAME_NUMBER[0], ds.getString(Tags.RefFrameNumber));
        assertEquals(OTHER_PATIENT_IDS[0], ds.getString(Tags.OtherPatientIDs));
        assertEquals(ADDITIONAL_PATIENT_HISTORY[0],
                ds.getString(Tags.AdditionalPatientHistory));
        assertEquals(OTHER_PATIENT_NAMES[0],
                ds.getString(Tags.OtherPatientNames));
        assertEquals(ACCESSION_NUMBER[0], ds.getString(Tags.AccessionNumber));
        assertEquals(DISPLAYED_AREA_BRHC[0],
                ds.getString(Tags.DisplayedAreaBottomRightHandCorner));
        assertEquals(OVERLAY_ORIGIN[0], ds.getString(Tags.OverlayOrigin));
        assertEquals(DERIVATION_DESCRIPTION[0],
                ds.getString(Tags.DerivationDescription));
        assertEquals(SOP_CLASSES_SUPPORTED[0],
                ds.getString(Tags.SOPClassesSupported));
        assertEquals(REF_SAMPLE_POSITIONS[0],
                ds.getString(Tags.RefSamplePositions));
        assertEquals(REF_FRAME_NUMBER[0], ds.getString(Tags.RefFrameNumbers));
        assertEquals(TEXT_VALUE[0], ds.getString(Tags.TextValue));
    }
    
    public void testGetStrings() throws Exception {
        setStrings();
        assertEquals(SCHEDULED_STATION_AET,
                ds.getStrings(Tags.ScheduledStationAET));
        assertEquals(PATIENT_AGE, ds.getStrings(Tags.PatientAge));
        assertEquals(IMAGE_TYPE, ds.getStrings(Tags.ImageType));
        assertEquals(STUDY_DATE, ds.getStrings(Tags.StudyDate));
        assertEquals(STUDY_TIME.length, ds.getStrings(Tags.StudyTime).length);
        assertEquals(ACQUISITION_DATETIME,
                ds.getStrings(Tags.AcquisitionDatetime));
        assertEquals(IMAGE_POSITION, ds.getStrings(Tags.ImagePosition));
        assertEquals(ANCHOR_POINT, ds.getStrings(Tags.AnchorPoint));
        assertEquals(TABLE_OF_Y_BREAK_POINTS,
                ds.getStrings(Tags.TableOfYBreakPoints));
        assertEquals(REF_FRAME_NUMBER, ds.getStrings(Tags.RefFrameNumber));
        assertEquals(OTHER_PATIENT_IDS, ds.getStrings(Tags.OtherPatientIDs));
        assertEquals(ADDITIONAL_PATIENT_HISTORY,
                ds.getStrings(Tags.AdditionalPatientHistory));
        assertEquals(OTHER_PATIENT_NAMES,
                ds.getStrings(Tags.OtherPatientNames));
        assertEquals(ACCESSION_NUMBER, ds.getStrings(Tags.AccessionNumber));
        assertEquals(DISPLAYED_AREA_BRHC,
                ds.getStrings(Tags.DisplayedAreaBottomRightHandCorner));
        assertEquals(OVERLAY_ORIGIN, ds.getStrings(Tags.OverlayOrigin));
        assertEquals(DERIVATION_DESCRIPTION,
                ds.getStrings(Tags.DerivationDescription));
        assertEquals(SOP_CLASSES_SUPPORTED,
                ds.getStrings(Tags.SOPClassesSupported));
        assertEquals(REF_SAMPLE_POSITIONS,
                ds.getStrings(Tags.RefSamplePositions));
        assertEquals(REF_FRAME_NUMBER, ds.getStrings(Tags.RefFrameNumbers));
        assertEquals(TEXT_VALUE, ds.getStrings(Tags.TextValue));
    }
    
    private void assertEquals(String[] expected, String[] value) {
        assertNotNull(value);
        assertEquals(expected.length, value.length);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], value[i]);
        }
    }

}
