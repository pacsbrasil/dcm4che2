package org.dcm4chee.xero.display;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/** Just a convenient holder class for display variables */
public class DisplayVars implements  NamespaceContext {
	String pid = "pid";
	String studyUid = "1";
	String seriesUid = "1.1";
	String objectUid = "1.1.1";
	Integer frame = new Integer(0);
	
	StudyLevel studyLevel = new StudyLevel();
	DisplayMode mode = new DisplayMode();
	
	LocalStudyModel model = new LocalStudyModel();
	
	XPathFactory factory = XPathFactory.newInstance();
	XPath xpath = factory.newXPath();
	DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder;
	
	public DisplayVars() {
		studyLevel.setPid(pid);
		studyLevel.setStudyUID("1");
		studyLevel.setSeriesUID(seriesUid);
		studyLevel.setObjectUID(objectUid);
		studyLevel.setFrame(frame);
		model.setStudyLevel(studyLevel);
		xpath.setNamespaceContext(this);
		try {
			builder = domFactory.newDocumentBuilder();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	String getPatientXml() {
		return model.getPatientXml();
	}
	
	Number getXpathNum(String xpathStr) throws XPathExpressionException, SAXException, IOException {
	    String xmlStr = getPatientXml();
		if( xmlStr==null || xmlStr.length()==0 ) return null;
		ByteArrayInputStream bais = new ByteArrayInputStream(xmlStr.getBytes());
		Document doc = builder.parse(bais);
		Number ret = (Number) xpath.evaluate(xpathStr,doc, XPathConstants.NUMBER);
		return ret;
	}

	public String getNamespaceURI(String prefix) {
		return "http://www.dcm4chee.org/xero/search/study/";
	}

	public String getPrefix(String namespaceURI) {
		return "se";
	}

	public Iterator getPrefixes(String namespaceURI) {
		throw new RuntimeException("No iterator for prefixes...");
	}
}
