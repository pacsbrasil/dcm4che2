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
package org.dcm4chee.xero.wado.cmove;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.search.filter.SimpleFilterItem;
import org.testng.annotations.Test;

public class FileLocationCMoveFilterTest
{

   @Test
   public void filter_WhenTypeIsNotCMove_ShouldPassToTheNextFilter() throws MalformedURLException
   {
      URL expected = new URL("http://agfa.com");
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("ae", "mvfType"); // 'cmove' type

      FileLocationCMoveFilter locationFilter = new FileLocationCMoveFilter();
      URL actual = locationFilter.filter(new SimpleFilterItem<URL>(expected), params);
      
      assertEquals(expected,actual,"When the type does not match, the URL should be untouched");
   }
   
   @Test
   public void filter_WhenTypeIsCMove_ShouldGenerateURLWithAppropriateStreamHandler() throws MalformedURLException
   {
      String queryStr = "?studyUID=1.2.3.4&seriesUID=1.3.5.7";
      URL originalURL = new URL("http://localhost:8080/wado2/"+queryStr);

      String ae = "cmoveType";
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("ae", ae);
      params.put("queryStr", queryStr);
      
      final String expectedExternalForm = "TestExternalForm";
      URLStreamHandler mockHandler = new URLStreamHandler() {

         @Override
         protected URLConnection openConnection(URL u) throws IOException
         {
            return null;
         }
         
         @Override
         protected String toExternalForm(URL u)
         {
            return expectedExternalForm;
         }
         
      };
      FileLocationCMoveFilter locationFilter = new FileLocationCMoveFilter(mockHandler);
      URL dicomURL = locationFilter.filter(new SimpleFilterItem<URL>(originalURL),params);
      
      assertEquals(dicomURL.toExternalForm(),expectedExternalForm, "Must embed the CMove Stream Handler");
   }
}
