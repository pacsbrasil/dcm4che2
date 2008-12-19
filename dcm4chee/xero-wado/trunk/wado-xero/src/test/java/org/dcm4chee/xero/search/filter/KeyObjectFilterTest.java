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
package org.dcm4chee.xero.search.filter;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.filter.KeyObjectFilter;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.KeyObjectBean;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.DicomImageReaderToDicomObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KeyObjectFilterTest {

   static DicomImageReaderSpi dicomImageReaderSpi = new DicomImageReaderSpi();

   static MetaDataBean mdb = StaticMetaData.getMetaData("wado2.metadata");
   static MetaDataBean komdb = mdb.getChild("imageSource");

   private Filter<ResultsBean> ko = null;
   private ResultsBean rb = null;
   private FilterItem<ResultsBean> fI = null;

   @SuppressWarnings("unchecked")
   @BeforeMethod
   public void init() throws Exception {
      rb = new ResultsBean();
      ko = (Filter<ResultsBean>) komdb.getValue("ko");
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      DicomImageReader dir = (DicomImageReader) dicomImageReaderSpi
            .createReaderInstance();

      InputStream is = cl
            .getResourceAsStream("org/dcm4chee/xero/wado/001eb7b3.dcm");
      dir.setInput(new MemoryCacheImageInputStream(is));
      DicomObject imageOne = ((DicomStreamMetaData) dir.getStreamMetadata())
            .getDicomObject();
      rb.addResult(imageOne);

      is = cl.getResourceAsStream("org/dcm4chee/xero/wado/001eb851.dcm");
      dir.setInput(new MemoryCacheImageInputStream(is));
      DicomObject imageTwo = ((DicomStreamMetaData) dir.getStreamMetadata())
            .getDicomObject();
      rb.addResult(imageTwo);

      is = cl.getResourceAsStream("org/dcm4chee/xero/wado/001eb7b4.dcm");
      dir.setInput(new MemoryCacheImageInputStream(is));
      DicomObject imageThree = ((DicomStreamMetaData) dir.getStreamMetadata())
            .getDicomObject();
      rb.addResult(imageThree);

      is = cl.getResourceAsStream("org/dcm4chee/xero/wado/sr_512_ct.dcm");
      DicomInputStream dis = new DicomInputStream(is);

      final DicomObject keyObject = new BasicDicomObject();
      dis.readDicomObject(keyObject, -1);

      rb.addResult(keyObject);

      fI = new FilterItem<ResultsBean>() {
         public ResultsBean callNextFilter(Map<String, Object> params) {
            return rb;
         }
      };

      DicomImageReaderToDicomObject dO = new DicomImageReaderToDicomObject() {
         public DicomObject filter(FilterItem<DicomObject> filterItem,
               Map<String, Object> params) {
            return keyObject;
         }
      };
      ((KeyObjectFilter) ko).setDicomFullHeader(dO);
   }

   @AfterMethod
   public void clear() {
      rb = null;
      fI = null;
      ko = null;
   }

   @Test
   public void displayKOModeAllTest() throws Exception  {

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("ko", "all");
      params.put("studyUID", "1.2.124.113532.193.190.36.23.20020315.210813.1712247");
      
      ResultsBean rbOut = ko.filter(fI, params);

      List<PatientType> pats = rbOut.getPatient();
      PatientType pat = pats.get(0);
      List<StudyType> studies = pat.getStudy();
      StudyType studyType = studies.get(0);
      List<SeriesType> seriesList = studyType.getSeries();
      
      assert ((SeriesType) seriesList.get(0)).getDicomObject().size() == 1;
      
      Object obj = ((SeriesType) seriesList.get(3)).getDicomObject().get(0);
      assert obj instanceof KeyObjectBean;
   }

   //@Test
   public void displayKOModeUIDAllTest() throws Exception {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("koUID", "1.113654.1.2001.20.512.1.2.109999");
      params.put("ko", "all");
      params.put("studyUID", "1.2.124.113532.193.190.36.23.20020315.210813.1712247");

      ResultsBean rbOut = ko.filter(fI, params);

      List<PatientType> pats = rbOut.getPatient();
      PatientType pat = pats.get(0);
      List<StudyType> studies = pat.getStudy();
      StudyType studyType = studies.get(0);
      List<SeriesType> seriesList = studyType.getSeries();
      
      ImageBean img = (ImageBean) ((SeriesType) seriesList.get(0))
            .getDicomObject().get(0);
      assert img.getId().equals(
            "1.3.12.2.1107.5.1.4.24072.4.0.1934796515801817");
      assert img.getOtherAttributes().get(QName.valueOf("koUID")).equals(
            "1.113654.1.2001.20.512.1.2.109999");

      
      img = (ImageBean) ((SeriesType) seriesList.get(1)).getDicomObject()
            .get(0);
      assert img.getId().equals(
            "1.3.12.2.1107.5.1.4.24072.202.0.743531425431591");
      assert img.getOtherAttributes().get(QName.valueOf("koUID")).equals(
            "1.113654.1.2001.20.512.1.2.109999");

      Object obj = ((SeriesType) seriesList.get(3)).getDicomObject().get(0);
      assert obj instanceof KeyObjectBean;
   }
   
   
   //@Test
   public void displayKOModeReferenced() throws Exception {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("koUID", "1.113654.1.2001.20.512.1.2.109999");
      params.put("studyUID", "1.2.124.113532.193.190.36.23.20020315.210813.1712247");

      ResultsBean rbOut = ko.filter(fI, params);

      List<PatientType> pats = rbOut.getPatient();
      PatientType pat = pats.get(0);
      List<StudyType> studies = pat.getStudy();
      StudyType studyType = studies.get(0);
      List<SeriesType> seriesList = studyType.getSeries();
      
      ImageBean img = (ImageBean) ((SeriesType) seriesList.get(0))
            .getDicomObject().get(0);
      assert img.getId().equals(
            "1.3.12.2.1107.5.1.4.24072.4.0.1934796515801817");
      assert img.getOtherAttributes().get(QName.valueOf("koUID")).equals(
            "1.113654.1.2001.20.512.1.2.109999");

      img = (ImageBean) ((SeriesType) seriesList.get(1)).getDicomObject()
            .get(0);
      assert img.getId().equals(
            "1.3.12.2.1107.5.1.4.24072.202.0.743531425431591");
      assert img.getOtherAttributes().get(QName.valueOf("koUID")).equals(
            "1.113654.1.2001.20.512.1.2.109999");

      Object obj = ((SeriesType) seriesList.get(2)).getDicomObject().get(0);
      assert obj instanceof KeyObjectBean;
   }
   
   public void displayResult(ResultsBean rbout) throws Exception    {
      JAXBContext context = JAXBContext.newInstance("org.dcm4chee.xero.search.study");
      Marshaller marshaller = context.createMarshaller();
      StringWriter  writer = new StringWriter();
      marshaller.marshal(rbout, writer);
   }
}
