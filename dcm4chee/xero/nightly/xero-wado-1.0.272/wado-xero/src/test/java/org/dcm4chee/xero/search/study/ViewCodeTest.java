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

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.search.filter.DicomTestData;
import org.testng.annotations.Test;

/**
 *
 * @author Andrew Cowan (amidx)
 */
public class ViewCodeTest
{

    /**
     * Test method for {@link org.dcm4chee.xero.search.study.ViewCode#initAttributes(org.dcm4che2.data.DicomObject)}.
     * @throws IOException 
     */
    @Test
    public void testInitAttributes_MustReadViewCodeValueFromDICOMFile() throws IOException
    {
        DicomObject dcm = DicomTestData.findDicomObject("org/dcm4chee/xero/search/study/MG0001.dcm");
        ViewCode vc = new ViewCode(dcm);
        assertEquals("R-10242",vc.getCodeValue());
    }
    
    @Test
    public void testContainsViewCodeSequence_TrueIfThereIsViewCodeSequence() throws IOException
    {
       DicomObject dcm = DicomTestData.findDicomObject("org/dcm4chee/xero/search/study/MG0001.dcm");
       assertTrue(ViewCode.containsViewCodeSequence(dcm));
    }
    
    @Test
    public void testGetDescription_MapsToTheRightDescription() throws IOException
    {
       DicomObject dcm = DicomTestData.findDicomObject("org/dcm4chee/xero/search/study/MG0001.dcm");
       ViewCode vc = new ViewCode(dcm);
       assertEquals(vc.getDescription(),"CC");
    }

    @Test
    public void testContainsViewCodeSequence_FalseIfNull() throws IOException
    {
       assertFalse(ViewCode.containsViewCodeSequence(null));
    }

    @Test
    public void getDescription_ShouldReturnCodeValueIfMeaningIsNotKnown() throws IOException
    {
       DicomObject dcm = DicomTestData.findDicomObject("org/dcm4chee/xero/search/study/MG0001.dcm");
       ViewCode vc = new ViewCode(dcm);
       vc.setCodeValue("R-MyNewCode");
       
       assertNotNull(vc.getDescription());
       assertEquals(vc.getDescription(),vc.getCodeValue());
    }
}
