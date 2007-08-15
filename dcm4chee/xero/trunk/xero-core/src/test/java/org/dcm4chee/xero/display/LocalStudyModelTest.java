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
package org.dcm4chee.xero.display;

import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsType;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.testng.annotations.Test;

/** Tests the local study model */
public class LocalStudyModelTest extends DisplayVars
{
	
	@Test
	public void testGetStudy()
	{
		StudyType study = model.getStudy();
		assert study!=null;
		assert study==model.getStudy();
		assert study.getStudyInstanceUID().equals(studyUid);
		PatientType patient = model.getPatient();
		assert patient!=null;
		assert patient.getStudy().contains(study);
	}
	
	@Test
	public void testGetSeries()
	{
		SeriesType series = model.getSeries();
		assert series!=null;
		// Should get the same series again.
		assert series==model.getSeries();
		assert series.getSeriesInstanceUID().equals(seriesUid);
		StudyType study = model.getStudy();
		assert study.getSeries().contains(series);
	}
	
	@Test
	public void testGetImage()
	{
		ImageBean image = model.getImage();
		assert image!=null;
		assert image==model.getImage();
		assert image.getSOPInstanceUID().equals(objectUid);
		assert image.getFrame()==null;
		SeriesType series = model.getSeries();
		assert series!=null;
		assert series.getDicomObject().contains(image);
	}
	
	/** Tests to see that objects that have no children or attributes other than the UID
	 * get removed on a clearChildless.
	 */
	@Test
	public void testClearChildless() {
	   ImageType image = model.getImage();
	   assert image!=null;
	   ResultsType results = model.getResults();
	   assert results.getPatient().size()>0;
	   model.clearEmpty();
	   assert results.getPatient().size()==0;
	}
	
}
