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
package org.dcm4chee.xero.dicom;

import static org.testng.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.AEProperties;
import org.testng.annotations.Test;


public class DicomURLHandlerTest
{
   private final String expectedAE = "test";
   private final String expectedHost = "marlin";
   private final int expectedPort = 105;
   private final String expectedStudyUID = "1.2.840.113543.6.6.1.1.4.2684384646680552649.20104256288";
   private final String expectedSeriesUID = "1.2.840.113543.6.6.1.1.5.2684384646680552649.20104256288";
   private final String expectedObjectUID = "1.2.5.6.840.113543.6.6.1.1.5.2684384646680552649.20104256288";

   private final String queryStr = "studyUID="+expectedStudyUID+"&seriesUID="+expectedSeriesUID+"&objectUID="+expectedObjectUID;
   private final String fullDicomURLStr = "dicom://"+expectedAE+"@"+expectedHost+":"+expectedPort+"/?"+queryStr;
   
   private DicomURLHandler handler = new DicomURLHandler();
   
   @Test
   public void testCreateFromParameters() throws MalformedURLException
   {
      String ae = "cmoveType";
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("ae", ae);
      params.put("queryStr", queryStr);
      params.put("objectUID", expectedObjectUID);

      URL dicomURL = handler.createURL(params);
      assertEquals(dicomURL.getProtocol(),"dicom");
      assertEquals(dicomURL.getHost(),"cmoveType@marlin");
      assertEquals(dicomURL.getPort(),11112);
      assertTrue(dicomURL.getQuery().contains("objectUID="+expectedObjectUID));
   }

   @Test
   public void createDicomQuery_ShouldIncludeAllUIDsInDicomObject() throws MalformedURLException
   {
      URL dicomURL = handler.createURL(fullDicomURLStr);
      DicomObject dcm = handler.createDicomQuery(dicomURL);
      assertEquals(dcm.getString(Tag.StudyInstanceUID),expectedStudyUID);
      assertEquals(dcm.getString(Tag.SeriesInstanceUID),expectedSeriesUID);
      assertEquals(dcm.getString(Tag.SOPInstanceUID), expectedObjectUID);
   }
   
   @Test
   public void parseAETitle_ShouldReturnAETitle_FromValidURL() throws MalformedURLException
   {
      URL dicomURL = handler.createURL(fullDicomURLStr);
      assertEquals(DicomURLHandler.parseAETitle(dicomURL),"test");
   }
   
   @Test
   public void parseAETitle_ShouldReturnNULL_WhenAETitleIsMissing() throws MalformedURLException
   {
      URL dicomURL = handler.createURL("dicom://marlin:8080/?studyUID="+expectedStudyUID);
      assertNull(DicomURLHandler.parseAETitle(dicomURL));
   }
   
   @Test
   public void parseQueryParameters_ShouldReadAllUIDsProperly() throws MalformedURLException
   {
      URL dicomURL = handler.createURL(fullDicomURLStr);
      Map<String,String> parameters = DicomURLHandler.parseQueryParameters(dicomURL);
      assertEquals(parameters.get("objectUID"),expectedObjectUID);
      assertEquals(parameters.get("seriesUID"),expectedSeriesUID);
      assertEquals(parameters.get("studyUID"),expectedStudyUID);      
   }
   
   @Test
   public void createURL_NoQueryParams_ShouldCreateNonConstrainedQuery() throws MalformedURLException
   {
      String ae = "cmoveType";
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("ae", ae);
      params.put("queryStr", queryStr);

      URL dicomURL = handler.createURL(params);
      assertEquals(dicomURL.getProtocol(),"dicom");
      assertEquals(dicomURL.getHost(),"cmoveType@marlin");
      assertEquals(dicomURL.getPort(),11112);
      assertEquals(dicomURL.getQuery(),null);
   }
   
}
