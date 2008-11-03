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
 * Sebastian Mohan <sebastian.mohan@agfa.com>
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

import java.io.InputStream;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KeyObjectBeanTest {

   private DicomObject keyObject = null;

   @BeforeMethod
   public void init() throws Exception {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream is = cl
            .getResourceAsStream("org/dcm4chee/xero/wado/sr_512_ct.dcm");
      DicomInputStream dis = new DicomInputStream(is);

      keyObject = new BasicDicomObject();
      dis.readDicomObject(keyObject, -1);

   }

   @AfterMethod
   public void clear() throws Exception {
      keyObject = null;
   }

   @Test
   public void keyObjectInitTest() throws Exception {
      ResultsBean ret = new ResultsBean();

      ret.addResult(keyObject);

      KeyObjectBean kob = (KeyObjectBean) ret.getChildren().get("1.113654.1.2001.20.512.1.2.109999");
      List<KeySelection> keys = kob.getKeySelection();
            
      KeySelection key1 = keys.get(0);
      assert key1.getObjectUid().equals("1.3.12.2.1107.5.1.4.24072.4.0.1934796515801817");
      
      KeySelection key2 = keys.get(1);
      assert key2.getObjectUid().equals("1.3.12.2.1107.5.1.4.24072.202.0.743531425431591");
      
      List<ObjectRef> keyValues = kob.getObjectRef();
      
      ObjectRef objRef1 = keyValues.get(0);
      assert objRef1.getObjectUID().equals("1.3.12.2.1107.5.1.4.24072.4.0.1934796515801817");
      assert objRef1.getGspsUID() == null;
      assert objRef1.getFrame() == null;

      ObjectRef objRef2 = keyValues.get(1);
      assert objRef2.getObjectUID().equals("1.3.12.2.1107.5.1.4.24072.202.0.743531425431591");
      assert objRef2.getGspsUID() == null;
      assert objRef2.getFrame() == null;
      
   }
}
