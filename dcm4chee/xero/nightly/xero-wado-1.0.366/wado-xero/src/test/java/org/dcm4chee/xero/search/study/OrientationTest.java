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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.Collection;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.search.filter.DicomTestData;
import org.testng.annotations.Test;


/**
 *
 * @author Andrew Cowan (amidx)
 */
public class OrientationTest
{

   /**
    * Test method for {@link org.dcm4chee.xero.search.study.Orientation#parsePatientOrientation(org.dcm4che2.data.DicomObject)}.
    * @throws IOException 
    */
   @Test
   public void testParsePatientOrientation_ReadOrientationFromFile_ShouldInterpetPLasR() throws IOException
   {
      DicomObject dcm = DicomTestData.findDicomObject("regroup/MG/MG0001.dcm");
      Collection<Orientation> orientations = Orientation.parsePatientOrientation(dcm);
      assertEquals(orientations.size(),2);
      assertTrue(orientations.contains(Orientation.POSTERIOR));
      assertTrue(orientations.contains(Orientation.LEFT));      
   }
   
   @Test
   public void testParsePatientOrientation_ReadOrientationFromFile_ShouldInterpretARasR()
       throws IOException
   {   
      DicomObject dcm = DicomTestData.findDicomObject("regroup/MG/MG0002.dcm");
      Collection<Orientation> orientations = Orientation.parsePatientOrientation(dcm);
      assertEquals(orientations.size(),2);
      assertTrue(orientations.contains(Orientation.ANTERIOR));
      assertTrue(orientations.contains(Orientation.RIGHT));
   }
   
   @Test
   public void testParsePatientOrientation_ReadOrientationFromFile_ShouldHandleBadlyFormedSequences()
       throws IOException
   {   
      DicomObject dcm = DicomTestData.findDicomObject("regroup/MG/MG0003.dcm");
      Collection<Orientation> orientations = Orientation.parsePatientOrientation(dcm);
      assertEquals(orientations.size(),3);
      assertTrue(orientations.contains(Orientation.POSTERIOR));
      assertTrue(orientations.contains(Orientation.FEET));
      assertTrue(orientations.contains(Orientation.LEFT));
   }
   
   @Test
   public void testParsePatientOrientation_NullDicomObjectsShouldParseAsEmptyCollection()
   {
      Collection<Orientation> orientations = Orientation.parsePatientOrientation(null);
      assertEquals(orientations.size(),0);
   }
   
   @Test
   public void testParsePatientOrientation_NullPatientOrientationTagShouldReturnEmptyCollection()
   {
      DicomObject dicom = createNiceMock(DicomObject.class);
      Collection<Orientation> orientations = Orientation.parsePatientOrientation(dicom);
      assertEquals(orientations.size(),0);
   }

//   /**
//    * Test method for {@link org.dcm4chee.xero.search.study.Orientation#parseDicomCode(java.lang.String)}.
//    */
//   @Test
//   public void testParseDicomCodeString()
//   {
//      fail("Not yet implemented");
//   }
//
//   /**
//    * Test method for {@link org.dcm4chee.xero.search.study.Orientation#parseDicomCode(char)}.
//    */
//   @Test
//   public void testParseDicomCodeChar()
//   {
//      fail("Not yet implemented");
//   }
//
//   /**
//    * Test method for {@link org.dcm4chee.xero.search.study.Orientation#flip(org.dcm4chee.xero.search.study.Orientation)}.
//    */
//   @Test
//   public void testFlip()
//   {
//      fail("Not yet implemented");
//   }

}
