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
import junit.framework.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public class DirBuilderPrefTest extends TestCase {
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    private static final String PREF_FILE_ID = 
            "../testdata/dir/DirBuilderPref.xml";
    
    public static Test suite() {
        return new TestSuite(DirBuilderPrefTest.class);
    }
    
    public DirBuilderPrefTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
    }
    
    public void testLoadDirBuilderPref() throws Exception {
        DirBuilderPref pref = DirBuilderFactory.getInstance()
                .loadDirBuilderPref(new File(PREF_FILE_ID));
        
        assertTrue(Arrays.equals(PAT_TAGS,
                pref.getTagsForRecordType("PATIENT")));
        assertTrue(Arrays.equals(STUDY_TAGS,
                pref.getTagsForRecordType("STUDY")));
        assertTrue(Arrays.equals(SERIES_TAGS,
                pref.getTagsForRecordType("SERIES")));
        assertTrue(Arrays.equals(IMAGE_TAGS,
                pref.getTagsForRecordType("IMAGE")));
     }
    
    private static final int[] PAT_TAGS = {
        Tags.SpecificCharacterSet,
        Tags.PatientName,
        Tags.PatientID,
        Tags.PatientBirthDate,
        Tags.PatientSex
    };

    private static final int[] STUDY_TAGS = {
        Tags.SpecificCharacterSet,
        Tags.StudyDate,
        Tags.StudyTime,
        Tags.AccessionNumber,
        Tags.ReferringPhysicianName,
        Tags.StudyDescription,
        Tags.ProcedureCodeSeq,
        Tags.StudyInstanceUID,
        Tags.StudyID
    };

    private static final int[] SERIES_TAGS = {
        Tags.SpecificCharacterSet,
        Tags.SeriesDate,
        Tags.SeriesTime,
        Tags.Modality,
        Tags.Manufacturer,
        Tags.InstitutionName,
        Tags.StationName,
        Tags.ManufacturerModelName,
        Tags.PerformingPhysicianName,
        Tags.SeriesDescription,
        Tags.BodyPartExamined,
        Tags.ProtocolName,
        Tags.SeriesInstanceUID,
        Tags.SeriesNumber,
        Tags.Laterality
    };

    private static final int[] IMAGE_TAGS = {
        Tags.SpecificCharacterSet,
        Tags.ImageType,
        Tags.AcquisitionDate,
        Tags.ContentDate,
        Tags.AcquisitionTime,
        Tags.ContentTime,
        Tags.RefImageSeq,
        Tags.ContrastBolusAgent,
        Tags.SequenceName,
        Tags.RepetitionTime,
        Tags.EchoTime,
        Tags.AcquisitionNumber,
        Tags.InstanceNumber,
        Tags.ImagePosition,
        Tags.ImageOrientation,
        Tags.NumberOfFrames
    };

}
