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

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public class DirReaderTest extends TestCase {
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    private static final String DICOMDIR_ID = "../testdata/dir/DICOMDIR";
    private static final String CLASS_UID = "1.2.840.10008.1.3.10";
    
    public static Test suite() {
        return new TestSuite(DirReaderTest.class);
    }
    
    /** 
     * Creates new DictionaryTest 
     */
    public DirReaderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
    }
    
    public void testIterateRoot() throws Exception {
        DirReader r = DirBuilderFactory.getInstance()
                .newDirReader(new File(DICOMDIR_ID));
        try {
            Dataset fsi = r.getFileSetInfo();
            assertEquals(0,fsi.getInt(Tags.FileSetConsistencyFlag, -1));
            FileMetaInfo fmi = fsi.getFileMetaInfo();
            assertEquals(CLASS_UID,
                    fmi.getString(Tags.MediaStorageSOPClassUID));
            assertEquals("NEMA97CD", fsi.getString(Tags.FileSetID));
            DirRecord rec = r.getFirstRecord();
            assertNotNull(rec);
            assertEquals("PATIENT",rec.getType());
            assertEquals(DirRecord.IN_USE,rec.getInUseFlag());
            assertEquals("TXSP-H-035",
                    rec.getDataset().getString(Tags.PatientID,null));
            int count = 1;
            while ((rec = rec.getNextSibling()) != null) {
                assertEquals("PATIENT",rec.getType());
                assertEquals(DirRecord.IN_USE,rec.getInUseFlag());
                ++count;
            }
            assertEquals(81,count);
        } finally {
            r.close();
        }
    }
    
    public void testIterateChilds() throws Exception {
        DirReader r = DirBuilderFactory.getInstance().
                newDirReader(new File(DICOMDIR_ID));
        try {
            int count = 0;
            for (DirRecord pat = r.getFirstRecord(); pat != null;
                    pat = pat.getNextSibling()) {
                ++count;
                assertEquals("PATIENT",pat.getType());
                assertEquals(DirRecord.IN_USE,pat.getInUseFlag());
                for (DirRecord study = pat.getFirstChild(); study != null;
                        study = study.getNextSibling()) {
                    ++count;
                    assertEquals("STUDY",study.getType());
                    assertEquals(DirRecord.IN_USE,study.getInUseFlag());
                    for (DirRecord series = study.getFirstChild();
                            series != null; series = series.getNextSibling()) {
                        ++count;
                        assertEquals("SERIES",series.getType());
                        assertEquals(DirRecord.IN_USE,series.getInUseFlag());
                        for (DirRecord image = series.getFirstChild();
                                image != null; image = image.getNextSibling()) {
                            ++count;
                            assertEquals("IMAGE",image.getType());
                            assertEquals(DirRecord.IN_USE,image.getInUseFlag());
                        }
                    }
                }
            }
            assertEquals(1203, count);
        } finally {
            r.close();
        }
    }
    
}//end class DirReaderTest
