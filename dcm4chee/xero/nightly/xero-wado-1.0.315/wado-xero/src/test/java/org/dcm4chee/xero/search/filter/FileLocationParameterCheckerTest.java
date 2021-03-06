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
package org.dcm4chee.xero.search.filter;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

/**
 *
 * @author Andrew Cowan (amidx)
 */
public class FileLocationParameterCheckerTest
{

   /**
    * Test method for {@link org.dcm4chee.xero.search.filter.FileLocationParameterChecker#isLocationTypeInParameters(java.util.Map)}.
    */
   @Test
   public void testIsLocationInParameters_NullLocation_IsAValidLocationType()
   {
      String ae = "noType";
      FileLocationParameterChecker checker = new FileLocationParameterChecker(null,"FOO");
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("ae", ae);
      assertTrue(checker.isLocationTypeInParameters(params));
   }
   
   @Test
   public void testIsLocationInParameters_ShouldBeFalseWhenTypeIsNotDefined()
   {
      String expectedType = "locationType";
      FileLocationParameterChecker checker = new FileLocationParameterChecker(expectedType);
      Map<String,Object> params = new HashMap<String, Object>();
      assertFalse(checker.isLocationTypeInParameters(params));
   }
   
   @Test
   public void testIsLocationTypeInParameters_ShouldBeTrueWhenTypeIsEqual()
   {
      String expectedType = "cmove";
      String ae = "cmoveType";
      FileLocationParameterChecker checker = new FileLocationParameterChecker(expectedType);
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("ae", ae);
      assertTrue(checker.isLocationTypeInParameters(params));
   }
   
   @Test
   public void testIsLocationTypeInParameters_ShouldBeFalseWhenTypeIsDifferent()
   {
      String expectedType = "file";
      String ae = "cmoveType";
      FileLocationParameterChecker checker = new FileLocationParameterChecker(expectedType);
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("ae", ae);
      assertFalse(checker.isLocationTypeInParameters(params));
   }
}
