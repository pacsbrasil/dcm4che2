package org.dcm4che2.data;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class ContentHandlerAdapterTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ContentHandlerAdapterTest.class);
    }

    public ContentHandlerAdapterTest(String arg0) {
        super(arg0);
    }

    private File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }
    
    public void testContentHandlerAdapter()
            throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        BasicAttributeSet attrs = new BasicAttributeSet();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        p.parse(locateFile("DICOMDIR.xml"), ch);
        Attribute attr = attrs.getAttribute(0x00041220);
        assertEquals(1203, attr.countItems());
   }

}
