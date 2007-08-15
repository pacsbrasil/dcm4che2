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

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * This class tests the handling of multi-frame expansion of objects. It depends
 * on some non-committed DICOM objects. If these objects are not present, no
 * tests will run, but a warning will be printed to the log.
 * 
 * @author bwallace
 * 
 */
public class TestMultiFrameImage {
   static Logger log = LoggerFactory.getLogger(TestMultiFrameImage.class);

   static final URL multiUrl = Thread.currentThread().getContextClassLoader().getResource(
		 "org/dcm4chee/xero/search/study/multiFrame.dcm");

   /** Tests to see that a single frame load still works normally. */
   @Test
   public void testSingleFrameLoad() {
	  PatientBean patient = TestJaxbEncode.loadPatient(TestJaxbEncode.singleUrl);
	  assert patient != null;
	  log.debug("Ran single frame load successfully.");
	  StudyBean study = (StudyBean) patient.getStudy().get(0);
	  assert study != null;
	  assert study.getStudyInstanceUID().equals("1.2.840.113674.514.212.200");
	  SeriesBean series = (SeriesBean) study.getSeries().get(0);
	  assert series != null;
	  ImageBean image = (ImageBean) series.getDicomObject().get(0);
	  assert image != null;
	  assert !(image instanceof ImageBeanMultiFrame);
   }

   /**
     * Tests to see that a multi-frame load works as expected, and by working,
     * only the frame number and regular ImageBean level attributes are tested -
     * no testing is done of macros in this test.
     */
   @Test
   public void testMultiFrameLoad() {
	  ImageBeanMultiFrame frames = getMultiFrames();
	  if (frames == null)
		 return;
	  assert frames.getNumberOfFrames() == 54;

	  // Two sets of objects can exist for multi-frames - either a regular
        // ImageBean with
	  // a frame number and position set, complete with all the extra
        // MacroItems owned both
	  // by that bean and by this one, or just the macro items that define
        // items that are
	  // different in the child element.
	  // This tests the image beans associated with the base object.
	  ImageBean image = frames.getImageFrame(1);
	  assert image != null;
	  assert image.getFrame() == 1;
	  // Assert object identity, not merely same contents.
	  assert image.getSOPInstanceUID() == frames.getSOPInstanceUID();
	  assert image.getGspsUID() == frames.getGspsUID();
	  assert image.getInstanceNumber() == frames.getInstanceNumber();

	  image = frames.getImageFrame(54);
	  assert image != null;
	  assert image.getFrame() == 54;
   }

   /**
     * This tests that macros defined at the object level get inherited by the
     * individual frame level instances.
     */
   @Test
   public void testMultiFrameMacroInherit() {
	  ImageBeanMultiFrame frames = getMultiFrames();
	  if (frames == null)
		 return;
	  assert frames.getNumberOfFrames() == 54;
	  WindowLevelMacro wl = new WindowLevelMacro(128f, 256f, "Because");
	  frames.getMacroItems().addMacro(wl);
	  ImageBean image = frames.getImageFrame(1);
	  assert image != null;
	  assert image.getFrame() == 1;

	  assert image.getOtherAttributes().get(WindowLevelMacro.Q_WINDOW_CENTER)!=null;

	  image = frames.getImageFrame(frames.getNumberOfFrames()/2);
	  assert image.getOtherAttributes().get(WindowLevelMacro.Q_WINDOW_CENTER)!=null;
   }

   ImageBeanMultiFrame getMultiFrames() {
	  PatientBean patient = TestJaxbEncode.loadPatient(multiUrl);
	  if (patient == null)
		 return null;
	  log.debug("Ran multi frame load successfully.");
	  StudyBean study = (StudyBean) patient.getStudy().get(0);
	  assert study != null;
	  assert study.getStudyInstanceUID().equals("1.3.6.1.4.1.5962.1.2.1.1106149053.29346");
	  SeriesBean series = (SeriesBean) study.getSeries().get(0);
	  assert series != null;
	  ImageBean image = (ImageBean) series.getDicomObject().get(0);
	  assert image != null;
	  assert image instanceof ImageBeanMultiFrame;
	  ImageBeanMultiFrame frames = (ImageBeanMultiFrame) image;
	  return frames;
   }
}
