package org.dcm4che2.io;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.junit.BaseTestCase;
import org.xml.sax.SAXException;

public class ContentHandlerAdapterTest extends BaseTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ContentHandlerAdapterTest.class);
    }

    public ContentHandlerAdapterTest(String arg0) {
        super(arg0);
    }

    public void testContentHandlerAdapter()
            throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        BasicDicomObject attrs = new BasicDicomObject();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        p.parse(locateFile("sr_511_ct-1.xml"), ch);
        assertEquals("ISO639_2", attrs.getString("/0040A730/0040A168/00080102"));
   }

}
