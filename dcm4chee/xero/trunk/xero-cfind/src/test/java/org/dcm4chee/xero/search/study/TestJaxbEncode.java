/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.search.study;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Tests the JAXB encoding of studies, series and images.
 * 
 * @author bwallace
 * 
 */
public class TestJaxbEncode {
   private static Logger log = LoggerFactory.getLogger(TestJaxbEncode.class);
   static XPathFactory factory = XPathFactory.newInstance();

   static XPath xpath = factory.newXPath();

   static DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

   static DocumentBuilder builder;
   static {
	  try {
		 builder = domFactory.newDocumentBuilder();
	  } catch (ParserConfigurationException e) {
		 // TODO Auto-generated catch block
		e.printStackTrace();
	  }
   }

   static final URL singleUrl = Thread.currentThread().getContextClassLoader().getResource("org/dcm4chee/xero/search/study/singleFrame.dcm");

   static final StopTagInputHandler stopHandler = new StopTagInputHandler(
   Tag.PixelData);

   @Test
   public void testPatientEncode() throws Exception {
	  String studyStr = getStudyStr(singleUrl);
	  log.debug("study=" + studyStr);

	  assert getXpathStr(studyStr, "study/@AccessionNumber").equals("THU9948");
	  assert getXpathStr(studyStr, "study/series/@SeriesNumber").equals("1");
   }
   
   public static String getStudyStr(URL url) throws Exception {
	  PatientBean patient = TestJaxbEncode.loadPatient(url);
	  StudyBean study = (StudyBean) patient.getStudy().get(0);
	  JAXBContext context = JAXBContext.newInstance(ResultsBean.class, StudyBean.class, SeriesBean.class, ImageBean.class);
	  Marshaller m = context.createMarshaller();
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  m.marshal(study, baos);
	  return baos.toString("UTF-8");
   }

   public static String getXpathStr(String xmlStr, String xpathStr) throws XPathExpressionException, SAXException, IOException {
	  if (xmlStr == null || xmlStr.length() == 0)
		 return null;
	  ByteArrayInputStream bais = new ByteArrayInputStream(xmlStr.getBytes());
	  Document doc = builder.parse(bais);
	  String ret = (String) xpath.evaluate(xpathStr, doc, XPathConstants.STRING);
	  log.debug("XPath "+xpathStr+"="+ret);
	  return ret;
   }
   
   public static Number getXpathNum(String xmlStr, String xpathStr) throws XPathExpressionException, SAXException, IOException {
	  if (xmlStr == null || xmlStr.length() == 0)
		 return null;
	  ByteArrayInputStream bais = new ByteArrayInputStream(xmlStr.getBytes());
	  Document doc = builder.parse(bais);
	  Number ret = (Number) xpath.evaluate(xpathStr, doc, XPathConstants.NUMBER);
	  log.debug("XPath "+xpathStr+"="+ret);
	  return ret;
   }

   public static DicomObject loadDicomObject(URL url) {
      DicomInputStream dis;
      if(url==null ) {
    	 TestMultiFrameImage.log.warn("No URL provided.");
    	 return null;
      }
      try {
    	 dis = new DicomInputStream(url.openStream());
         dis.setHandler(TestJaxbEncode.stopHandler);
    	 return (dis.readDicomObject());
      } catch (IOException e) {
    	  TestMultiFrameImage.log.warn("Unable to load object for "+url+" tests will not be run.");
    	  return null;
      }
   }

   public static PatientBean loadPatient(URL url) {
      Map<Object,Object> children = new HashMap<Object,Object>();
      DicomObject dcmObj = TestJaxbEncode.loadDicomObject(url);
      if( dcmObj==null ) return null;
      PatientBean patient = new PatientBean(children,dcmObj);
      return patient;
   }

}
