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

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Andrew Cowan (amidx)
 */
public class SeriesBeanTest
{
   @Test
   public void testClone_ShouldUseDefaultPrefixWhenNullPrefixIsPassed()
   {
      SeriesBean series = new SeriesBean();
      series.setSeriesUID("1.2.3.4.5");
      SeriesBean newSeries = series.clone(null);
      
      assertFalse(newSeries.getSeriesUID().equals(series.getSeriesUID()));
      assertTrue(newSeries.getSeriesUID().contains(series.getSeriesUID()));
   }
   
   @Test
   public void testClone_MustCopyAttributesAndParent()
   {

      StudyBean study = new StudyBean();
      SeriesBean series = new SeriesBean(study);
      series.setBodyPartExamined("HEAD");
      series.setLaterality("L");
      series.setModality("MG");
      series.setSeriesDescription("My test series");
      series.setSeriesUID("1.2.3.4.5");

      String prefix = "CC";
      SeriesBean newSeries = series.clone(prefix);
      
      assertEquals(newSeries.getBodyPartExamined(),series.getBodyPartExamined());
      assertEquals(newSeries.getLaterality(),series.getLaterality());
      assertEquals(newSeries.getModality(),series.getModality());
      assertEquals(newSeries.getSeriesDescription(),series.getSeriesDescription());
      
      assertTrue(newSeries.getSeriesUID().contains(prefix));
      assertTrue(newSeries.getSeriesUID().contains(series.getSeriesUID()));
      
      assertEquals(newSeries.getStudyBean(),series.getStudyBean());
      assertTrue(study.getSeries().contains(newSeries));
   }
   
   @Test
   public void testMove_ReparentsTheImage()
   {
      StudyBean study = new StudyBean();
      SeriesBean series1 = new SeriesBean(study);
      ImageBean image1 = new ImageBean(series1);
      ImageBean image2 = new ImageBean(series1);
      series1.getDicomObject().add(image1);
      series1.getDicomObject().add(image2);
      
      SeriesBean series2 = new SeriesBean(study);
      
      assertEquals(series1.getDicomObject().size(),2);
      assertEquals(series2.getDicomObject().size(),0);      

      series2.move(image1);
      
      assertEquals(series1.getDicomObject().size(),1);
      assertEquals(series2.getDicomObject().size(),1);
      assertFalse(series1.getDicomObject().contains(image1));
      assertTrue(series2.getDicomObject().contains(image1));
      assertEquals(image1.getSeriesBean(),series2);
   }
   
   @Test
   public void testMove_RemovesEmptySeriesFromStudy()
   {
      StudyBean study = new StudyBean();
      SeriesBean series1 = new SeriesBean(study);
      study.getSeries().add(series1);
      study.children.put(series1.getId(), series1);
      ImageBean image1 = new ImageBean(series1);
      series1.getDicomObject().add(image1);
      
      SeriesBean series2 = new SeriesBean(study);
      study.getSeries().add(series2);
      study.children.put(series2.getId(), series2);
      
      assertTrue(series1.getDicomObject().contains(image1));
      assertFalse(series2.getDicomObject().contains(image1));
      assertTrue(study.getSeries().contains(series1));

      series2.move(image1);
      
      assertFalse(series1.getDicomObject().contains(image1));
      assertTrue(series2.getDicomObject().contains(image1));
      assertFalse(study.getSeries().contains(series1));
      assertNull(study.getChildById(series1.getId()));
   }
}
