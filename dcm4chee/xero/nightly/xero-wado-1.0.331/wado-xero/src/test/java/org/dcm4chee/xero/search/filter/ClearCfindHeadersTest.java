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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

import java.io.InputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.*;

/** Tests that the CFind headers are correctly cleared from the Image DICOM Object */
public class ClearCfindHeadersTest {
   
   @Test
   @SuppressWarnings("unchecked")
   public void test_in_image_filter_not_in_image() throws Exception {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("imgconsistency/cplx_p01.dcm");
      DicomInputStream dis = new DicomInputStream(is);
      DicomObject ds = dis.readDicomObject();
      ResultsBean rb = new ResultsBean();
      rb.addResult(ds);
      
      // In Image
      ImageBean ib = (ImageBean) rb.getChildren().get(ImageBean.key(ds.getString(Tag.SOPInstanceUID)));
      assert ib!=null;
      assert ib.getCfindHeader()==ds;
      
      // Filter
      FilterItem<ResultsBean> filterItem = createMock(FilterItem.class);
      expect(filterItem.callNextFilter(null)).andReturn(rb);
      
      replay(filterItem);
      
      new ClearCfindHeaders().filter(filterItem,null);
      verify(filterItem);
      
      // Not in image
      assert ib.getCfindHeader()==null;
   }
}
