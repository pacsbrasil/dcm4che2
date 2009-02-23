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
 * Sebastian Mohan, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Sebastian Mohan <sebastian.mnohan@agfa.com>
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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.wado.WadoParams;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PartialImageInfoFixTest {

   PartialImageInfoFix piif = new PartialImageInfoFix();
   
   ResultsBean results = new ResultsBean();
   ImageBean imageBeanToFix;
   ImageBean imageBeanWasOk;
   FilterItem<ResultsBean> filterItem;
   DicomObject mg1;
   Map<String,Object> params = null;
   Filter<DicomObject> dsFilter;
   
   @SuppressWarnings("unchecked")
   @BeforeMethod
   public void init() throws Exception {
	  
	  params = new HashMap<String,Object>();
	   
      filterItem = createMock(FilterItem.class);
      expect(filterItem.callNextFilter(params)).andReturn(results);
      replay(filterItem);
      
      mg1 = DicomTestData.findDicomObject("regroup/MG/MG0001.dcm");
      results.addResult(mg1);
      results.addResult(DicomTestData.findDicomObject("regroup/MG/MG0002.dcm"));
      List<DicomObjectType> dots = results.getPatient().get(0).getStudy().get(0).getSeries().get(0).getDicomObject();
      imageBeanToFix = (ImageBean) dots.get(0);
      imageBeanWasOk = (ImageBean) dots.get(1);
      imageBeanToFix.setRows(-1);
      imageBeanToFix.setColumns(-1);
      dsFilter = createMock(Filter.class);
      expect(dsFilter.filter((FilterItem) isNull(), (Map<String, Object>) anyObject())).andReturn(mg1);
      replay(dsFilter);
      piif.setDicomImageHeader(dsFilter);
   }
   
   /** Tests to see that the default AE doesn't run the filter */
   @Test
   public void test_filter_aeNoFix_returnNoChanges() {
      piif.filter(filterItem,params);
      verify(filterItem);
      assert imageBeanToFix.getRows()==-1;
   }
   
   @Test
   public void test_filter_aeConfigured_returnFixedImage() {
      params.put(WadoParams.AE, "imageFix");
      piif.filter(filterItem,params);      
      verify(filterItem,dsFilter);
      assert imageBeanToFix.getRows()>0;
      assert imageBeanToFix.getColumns()>0;
   }
   
}
