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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.dcm4chee.xero.search.filter.DicomTestData;
import org.dcm4chee.xero.search.macro.FlipRotateMacro;
import org.testng.annotations.Test;

/**
 * 
 * @author Andrew Cowan (amidx)
 */
@SuppressWarnings("deprecation")
public class MammoImageFlipperTest {
   private MammoImageFlipper flipper = new MammoImageFlipper();

   /**
    * Test method for
    * {@link org.dcm4chee.xero.search.study.MammoImageFlipper#isFlipRequired(org.dcm4chee.xero.search.study.ImageBean)}
    * .
    */
   @Test
   public void testIsFlipRequired() {
      Collection<Orientation> posterior = Collections.singleton(Orientation.POSTERIOR);
      Collection<Orientation> anterior = Collections.singleton(Orientation.ANTERIOR);

      assertFalse(flipper.isFlipRequired(Laterality.RIGHT, posterior));
      assertTrue(flipper.isFlipRequired(Laterality.RIGHT, anterior));
      assertTrue(flipper.isFlipRequired(Laterality.LEFT, posterior));
      assertFalse(flipper.isFlipRequired(Laterality.LEFT, anterior));

   }

   /**
    * Test method for
    * {@link org.dcm4chee.xero.search.study.MammoImageFlipper#applyFlipIfNecessary(org.dcm4chee.xero.search.study.ImageBean)}
    * .
    * 
    * @throws IOException
    */
   @Test
   public void testApplyFlipIfNecessary_PosteriorRight_ShouldNotFlip() throws IOException {
      ImageBean posteriorRight = new ImageBean();
      posteriorRight.addResult(DicomTestData.findDicomObject("regroup/MG/MG0001.dcm"));
      assertFalse(flipper.applyFlipIfNecessary(posteriorRight));
      assertFalse(containsHorizontalFlip(posteriorRight));
   }

   /**
    * Test method for
    * {@link org.dcm4chee.xero.search.study.MammoImageFlipper#applyFlipIfNecessary(org.dcm4chee.xero.search.study.ImageBean)}
    * .
    * 
    * @throws IOException
    */
   @Test
   public void testApplyFlipIfNecessary_AnteriorLeft_ShouldNotFlip() throws IOException {
      ImageBean anteriorLeft = new ImageBean();
      anteriorLeft.addResult(DicomTestData.findDicomObject("regroup/MG/MG0002.dcm"));
      assertFalse(flipper.applyFlipIfNecessary(anteriorLeft));
      assertFalse(containsHorizontalFlip(anteriorLeft));
   }

   /**
    * Test method for
    * {@link org.dcm4chee.xero.search.study.MammoImageFlipper#applyFlipIfNecessary(org.dcm4chee.xero.search.study.ImageBean)}
    * .
    * @TODO renable this on the next release of the test data set.
    * 
    * @throws IOException
    */
   //@Test
   public void testApplyFlipIfNecessary_AnteriorRight_ShouldFlip() throws IOException {
      ImageBean anteriorRight = new ImageBean();
      anteriorRight.addResult(DicomTestData.findDicomObject("regroup/MG/MG0001_PosteriorLeft.dcm"));
      assertTrue(flipper.applyFlipIfNecessary(anteriorRight));
      assertTrue(containsHorizontalFlip(anteriorRight));
   }

   private boolean containsHorizontalFlip(ImageBean image) {
      for (Object o : image.getMacroItems().getMacros()) {
         if (o instanceof FlipRotateMacro) {
            FlipRotateMacro macro = (FlipRotateMacro) o;
            if (macro.getFlip() && macro.getRotation() == 0)
               return true;
         }
      }

      return false;
   }
}
