/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

/* $Id$ */

package org.dcm4che.srom;

import org.dcm4che.srom.*;
import org.dcm4che.data.*;
import junit.framework.*;

import java.io.*;
import javax.imageio.stream.*;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0
 */
public class SRDocumentFactoryTest extends TestCase {

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    public static Test suite() {
        return new TestSuite(SRDocumentFactoryTest.class);
    }
    
    /** 
     * Creates new ExtractorTest 
     */
    public SRDocumentFactoryTest(String name) {
        super(name);
    }
    
    private DcmObjectFactory dsf = null;
    private SRDocumentFactory srf = null;
    
    protected void setUp() throws Exception {    
        dsf = DcmObjectFactory.getInstance();
        srf = SRDocumentFactory.getInstance();
    }
    
    private static final String SR_DCM =
//            "../testdata/sr/examplef9.dcm";
            "../testdata/sr/sr_601cr.dcm";
    private static final String KO_DCM =
            "../testdata/sr/sr_511_cr.dcm";
    
    private void doIterate(Content parent) {
        System.out.println(parent);
        for (Content child = parent.getFirstChild(); child != null;
                child = child.getNextSibling()) {
            doIterate(child);
        }
    }
    
    public void testSR() throws Exception {
        Dataset ds = dsf.newDataset();
        InputStream in = new BufferedInputStream(
                new FileInputStream(new File(SR_DCM)));
        try {
            ds.read(in, null, -1);
        } finally {
            in.close();
        }
        SRDocument sr = srf.newSRDocument(ds);
        System.out.println(sr);
        System.out.println(sr.getPatient());
        System.out.println(sr.getStudy());
        System.out.println(sr.getSeries());
        System.out.println(sr.getEquipment());        
        doIterate(sr);
    }
    
    public void testKO() throws Exception {
        Dataset ds = dsf.newDataset();
        InputStream in = new BufferedInputStream(
                new FileInputStream(new File(KO_DCM)));
        try {
            ds.read(in, null, -1);
        } finally {
            in.close();
        }
        KeyObject ko = srf.newKeyObject(ds);
        System.out.println(ko);
        System.out.println(ko.getPatient());
        System.out.println(ko.getStudy());
        System.out.println(ko.getSeries());
        System.out.println(ko.getEquipment());        
        doIterate(ko);
    }
}
