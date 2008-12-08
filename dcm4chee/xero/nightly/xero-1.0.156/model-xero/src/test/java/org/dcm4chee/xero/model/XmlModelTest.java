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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.xero.model;

import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Tests the XmlModel object can be created from resources and that it can be customized/overridden
 * to have additional specific behaviour.
 * 
 * @author bwallace
 */
public class XmlModelTest {
   private static final Logger log = LoggerFactory.getLogger(XmlModelTest.class);
   MetaDataBean mdb = StaticMetaData.getMetaData("test-model.metadata");
   MetaDataBean studyColumns = mdb.getChild("model").getChild("studyColumns");
   
   /** Test that a simple model can be parsed and has some header data. */
   @SuppressWarnings("unchecked")
   @Test
   public void simpleXmlModelTest() throws Exception {
	  assert studyColumns!=null;
	  Map<String,Object> scValue = (Map<String,Object>) studyColumns.getValue();
	  assert scValue!=null;
	  XmlModel xm = (XmlModel) scValue;
	  List<XmlModel> headers = (List<XmlModel>) xm.get("header");
	  assert headers.size()>1;
	  XmlModel header = headers.get(0);
	  assert header.get("name").equals("Patient Name");
   }
   
   /** Test parsing a slightly more complex object that is a study query XML result */
   @SuppressWarnings("unchecked")
   @Test
   public void studyRowsTest() throws Exception {
	  MetaDataBean studyRows = mdb.getChild("model").getChild("studyRows");
	  Map<String,Object> srValue = (Map<String,Object>) studyRows.getValue();
	  assert srValue!=null;
	  List<XmlModel> patients = (List<XmlModel>) srValue.get("patient");
	  assert patients.size()>1;
	  XmlModel patient = patients.get(0);
	  assert patient.get("PatientID").equals("14");
   }
   
   /** Tests that setting the XML object as a Source object instead of reading it from the meta-data */
   @SuppressWarnings("unchecked")
   @Test
   public void sourceLoadTest() throws Exception {
	  ClassLoader cl = Thread.currentThread().getContextClassLoader();
	  StreamSource ss = new StreamSource(cl.getResource("studyRows.xml").openStream());
	  XmlModel xmod = new XmlModel();
	  xmod.setSource(ss);
	  List<XmlModel> patients = (List<XmlModel>) xmod.get("patient");
	  assert patients.size()>1;
	  XmlModel patient = patients.get(0);
	  assert patient.get("PatientID").equals("14");
   }
   
   /** Displays performance information about the XmlModelTest.  Parsing time for 120 query results
    * is about 8 ms, including reading the results from disk each time and constructing the resulting
    * data structure.  Since the time take to perform the C-FIND portion of the query is about 130 ms,
    * this is a relatively inconsequential amount of time.
    */
   @SuppressWarnings("unchecked")
   public static void main(String[] args) {
	  XmlModel xmlModel = new XmlModel();
	  xmlModel.setResource("studyRows.xml");
	  List<XmlModel> studies = (List<XmlModel>) ((List<XmlModel>)xmlModel.get("patient")).get(0).get("study");
	  assert studies.size()>0;
	  
	  long start = System.nanoTime();
	  int cnt = 500;
	  for(int i=0; i<cnt; i++) {
		 xmlModel = new XmlModel();
		 xmlModel.setResource("studyRows.xml");
		 studies = (List<XmlModel>) ((List<XmlModel>)xmlModel.get("patient")).get(0).get("study");
		 assert studies.size()>0;
	  }
	  log.info("Time taken for parsing studyRows "+(System.nanoTime()-start)/(1e6*cnt)+" ms/item");
   }
}
