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

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import static org.dcm4chee.xero.search.study.TestJaxbEncode.getXpathNum;

public class TestMacroMixIn 
{
   private static final Logger log = LoggerFactory.getLogger(TestMacroMixIn.class);
   
   @Test
   public void testWindowLevelImage() {
	  ImageBean image = new ImageBean();
	  assert image.getOtherAttributes()==null;
	  image.getMacroItems().addMacro(new WindowLevelMacro(128,256,"Full 8 bit WL"));
	  assert image.getOtherAttributes()!=null;
	  assert Float.parseFloat(image.getOtherAttributes().get(WindowLevelMacro.Q_WINDOW_CENTER))==128f;
	  WindowLevelMacro wl = (WindowLevelMacro) image.getMacroItems().findMacro(WindowLevelMacro.class);
	  assert wl!=null;
	  assert wl.getWidth()==256f;
   }
   
   @Test
   public void testEncodeMixIn() throws Exception {
	  PatientBean patient = TestJaxbEncode.loadPatient(TestJaxbEncode.singleUrl);
	  StudyBean study = (StudyBean) patient.getStudy().get(0);
	  
	  SeriesBean series = (SeriesBean) study.getSeries().get(0);
	  ImageBean image = (ImageBean) series.getDicomObject().get(0);
	  assert image!=null;
	  image.getMacroItems().addMacro(new WindowLevelMacro(128,256,"Full 8 bit WL"));
	  assert Float.parseFloat(image.getOtherAttributes().get(WindowLevelMacro.Q_WINDOW_WIDTH))==256f;
	  
	  JAXBContext context = JAXBContext.newInstance("org.dcm4chee.xero.search.study");
	  Marshaller m = context.createMarshaller();
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  m.marshal(study, baos);
	  String studyStr = baos.toString("UTF-8");
      log.info("Study str with window level macro="+studyStr);
	  assert getXpathNum(studyStr,"study/series/image/@windowWidth").floatValue()==256f;
   }
}
