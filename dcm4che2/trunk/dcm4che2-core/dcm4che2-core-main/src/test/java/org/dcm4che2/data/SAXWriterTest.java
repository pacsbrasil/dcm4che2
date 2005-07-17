package org.dcm4che2.data;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class SAXWriterTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SAXWriterTest.class);
    }

    public SAXWriterTest(String arg0) {
        super(arg0);
    }

    private File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }
    
    public final void testWrite() 
            throws IOException, TransformerConfigurationException, 
            TransformerFactoryConfigurationError, SAXException {
        DicomInputStream dis = new DicomInputStream(locateFile("DICOMDIR"));
        AttributeSet attrs = new BasicAttributeSet();
        dis.readAttributeSet(attrs, -1);
        dis.close();
        File ofile = new File("target/test-out/DICOMDIR1.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        new SAXWriter(th, th).write(attrs);
    }

    public final void testReadValue() throws IOException,
            TransformerConfigurationException,
            TransformerFactoryConfigurationError, SAXException {
        File ofile = new File("target/test-out/DICOMDIR2.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        SAXWriter w = new SAXWriter(th, th);
        DicomInputStream dis = new DicomInputStream(locateFile("DICOMDIR"));
        dis.setHandler(w);
        AttributeSet attrs = new BasicAttributeSet();
        dis.readAttributeSet(attrs, -1);
        dis.close();
    }

}
