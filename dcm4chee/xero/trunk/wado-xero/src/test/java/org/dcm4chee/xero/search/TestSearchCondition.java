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
package org.dcm4chee.xero.search;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.search.study.StudySearchConditionParser;
import org.testng.annotations.Test;

/**
 * Tests the search condition generation of XML, search parameters for a GET style query.
 * @author bwallace
 *
 */
public class TestSearchCondition {
	static SearchConditionParser scp = new StudySearchConditionParser();
	static Map<String,Object> scpMap = new HashMap<String,Object>();
	static {
		scpMap.put("StudyInstanceUID", "1.2.3");
		scpMap.put("PatientID", "5");
		scpMap.put("ModalitiesInStudy", "CT");
	}

	/** Tests generation of Xml from the search condition */
	@Test
	public void testSearchConditionXml() {
		SearchCriteria sc = scp.parseFromMap(scpMap);
		assert sc!=null;
		String xml = sc.getXml();
		assert xml!=null;
		// This code should not have the xml indicator in it
		assert xml.indexOf("<?xml")<0;
		// Make sure it is in the right namespace...
		assert xml.indexOf("xmlns=\"http://www.dcm4chee.org/xero/search/\"")>0;
		System.out.println("xml="+xml);
	}
	
	/** Tests generation of simple search parameters from the search condition */
	@Test
	public void testSearchParameters() {
		SearchCriteria sc = scp.parseFromMap(scpMap);
		assert sc!=null;
		String parameters = sc.getURLParameters();
		assert parameters!=null;
		assert parameters.length()>0;
		System.out.println("Parameters="+parameters);
	}
	
	
}
