package org.dcm4che2.io;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.junit.BaseTestCase;
import org.xml.sax.SAXException;

public class SAXWriterTest extends BaseTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SAXWriterTest.class);
    }

    public SAXWriterTest(String arg0) {
        super(arg0);
    }

    public final void testWrite() 
            throws IOException, TransformerConfigurationException, 
            TransformerFactoryConfigurationError, SAXException {
        DicomObject attrs = load("sr_511_ct.dcm");
        File ofile = new File("target/test-out/sr_511_ct-1.xml");
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
        File ofile = new File("target/test-out/sr_511_ct-2.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        SAXWriter w = new SAXWriter(th, th);
        DicomInputStream dis = new DicomInputStream(locateFile("sr_511_ct.dcm"));
        dis.setHandler(w);
        DicomObject attrs = new BasicDicomObject();
        dis.readDicomObject(attrs, -1);
        dis.close();
    }


    public final void testReadValue2() throws IOException,
            TransformerConfigurationException,
            TransformerFactoryConfigurationError, SAXException {
        File ofile = new File("target/test-out/view400.xml");
        ofile.getParentFile().mkdirs();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult(new StreamResult(ofile));
        SAXWriter w = new SAXWriter(th, th);
        DicomInputStream dis = new DicomInputStream(locateFile("view400.dcm"));
        dis.setHandler(w);
        DicomObject attrs = new BasicDicomObject();
        dis.readDicomObject(attrs, -1);
        dis.close();
    }
}
