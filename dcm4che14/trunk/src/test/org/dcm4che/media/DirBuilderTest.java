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
public class DirBuilderTest extends TestCase {
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    private static final String PREF_FILE_ID = 
            "../testdata/dir/DirBuilderPref.xml";
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
    
    private DirBuilderFactory fact;
    private DirBuilderPref pref;
    protected void setUp() throws Exception {
        fact = DirBuilderFactory.getInstance();
        pref = fact.loadDirBuilderPref(new File(PREF_FILE_ID));
    }
    
    public void testLoadDirBuilderPref() throws Exception {
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
