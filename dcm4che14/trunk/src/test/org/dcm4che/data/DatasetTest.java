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
import org.dcm4che.dict.DictionaryFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
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
            ds.read(in, null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testDICOMDIR() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(DICOMDIR)));
        try {
            ds.read(in, null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testPART10_EVR_LE() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(PART10_EVR_LE)));
        try {
            ds.read(in, null, -1);
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
}
