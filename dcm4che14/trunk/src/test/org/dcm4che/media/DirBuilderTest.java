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
import java.util.*;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public class DirBuilderTest extends TestCase {
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    private static final String FILE_ID = "../testdata/dir/DD_NEW";
    private static final String INST_UID = "1.2.40.0.13.1.1.99";
    private static final String FILE_SET_ID = "FILE_SET_ID";
    private static final String[] FILE_IDs = {
        "../testdata/dir/DICOM/PICKER/6AF8_10",
        "../testdata/dir/DICOM/PICKER/6AF8_30",
        "../testdata/dir/DICOM/PHILIPS/MR4_5/MRABDO",
        "../testdata/dir/DICOM/PHILIPS/MR4_5/MRABDOR",
    };
    
    public static Test suite() {
        return new TestSuite(DirBuilderTest.class);
    }
    
    public DirBuilderTest(String name) {
        super(name);
    }
    
    private DcmObjectFactory objFact = DcmObjectFactory.getInstance();
    private DirBuilderFactory fact;
    private DirBuilderPref pref;
    
    private Dataset getPatientFilter() {
        Dataset retval = objFact.newDataset();
        retval.setCS(Tags.SpecificCharacterSet);
        retval.setPN(Tags.PatientName);
        retval.setLO(Tags.PatientID);
        retval.setDA(Tags.PatientBirthDate);
        retval.setCS(Tags.PatientSex);
        return retval;
    }
    
    private Dataset getStudyFilter() {
        Dataset retval = objFact.newDataset();
        retval.setCS(Tags.SpecificCharacterSet);
        retval.setDA(Tags.StudyDate);
        retval.setTM(Tags.StudyTime);
        retval.setSH(Tags.AccessionNumber);
        retval.setPN(Tags.ReferringPhysicianName);
        retval.setLO(Tags.StudyDescription);
        retval.setSQ(Tags.ProcedureCodeSeq);
        retval.setUI(Tags.StudyInstanceUID);
        retval.setSH(Tags.StudyID);
        return retval;
    }

    private Dataset getSeriesFilter() {
        Dataset retval = objFact.newDataset();
        retval.setCS(Tags.SpecificCharacterSet);
        retval.setDA(Tags.SeriesDate);
        retval.setTM(Tags.SeriesTime);
        retval.setCS(Tags.Modality);
        retval.setLO(Tags.Manufacturer);
        retval.setLO(Tags.SeriesDescription);
        retval.setCS(Tags.BodyPartExamined);
        retval.setUI(Tags.SeriesInstanceUID);
        retval.setIS(Tags.SeriesNumber);
        retval.setCS(Tags.Laterality);
        return retval;
    }
    
    private Dataset getImageFilter() {
        Dataset retval = objFact.newDataset();
        retval.setCS(Tags.SpecificCharacterSet);
        retval.setDA(Tags.ContentDate);
        retval.setTM(Tags.ContentTime);
        retval.setSQ(Tags.RefImageSeq);
        retval.setLO(Tags.ContrastBolusAgent);
        retval.setIS(Tags.InstanceNumber);
        retval.setIS(Tags.NumberOfFrames);
        return retval;
    }
    
    protected void setUp() throws Exception {
        fact = DirBuilderFactory.getInstance();
        pref = fact.newDirBuilderPref();
        pref.setFilterForRecordType("PATIENT", getPatientFilter());
        pref.setFilterForRecordType("STUDY", getStudyFilter());
        pref.setFilterForRecordType("SERIES", getSeriesFilter());
        pref.setFilterForRecordType("IMAGE", getImageFilter());
    }
    
    public void testAddFileRef() throws Exception {
        DirWriter w1 = fact.newDirWriter(new File(FILE_ID), INST_UID, 
                FILE_SET_ID, null, null, null);
        DirBuilder b1 = fact.newDirBuilder(w1, pref); 
        try {
            int c = 0;
            c += b1.addFileRef(new File(FILE_IDs[0]));
            c += b1.addFileRef(new File(FILE_IDs[1]));
            c += b1.addFileRef(new File(FILE_IDs[2]));
            c += b1.addFileRef(new File(FILE_IDs[3]));
            assertEquals(15, c);
        } finally {
            b1.close();
        }
       
     }   
}
