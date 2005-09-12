/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.junit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.ContentHandlerAdapter;
import org.dcm4che2.io.DicomInputStream;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 3, 2005
 *
 */
public class BaseTestCase extends TestCase {

    public BaseTestCase(String testName) {
        super(testName);
    }
    
    protected static DicomObject load(String fname) throws IOException {
    	ClassLoader cl = Thread.currentThread().getContextClassLoader();
    	DicomInputStream dis = new DicomInputStream(new BufferedInputStream(cl
    			.getResourceAsStream(fname)));
    	try {
    		DicomObject attrs = new BasicDicomObject();
    		dis.readDicomObject(attrs, -1);
    		return attrs;
    	} finally {
    		dis.close();
    	}
    }
    
    protected static DicomObject loadXML(String fname)
            throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        BasicDicomObject attrs = new BasicDicomObject();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        p.parse(locateFile(fname), ch);
        return attrs;        
    }

    protected static File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }

}
