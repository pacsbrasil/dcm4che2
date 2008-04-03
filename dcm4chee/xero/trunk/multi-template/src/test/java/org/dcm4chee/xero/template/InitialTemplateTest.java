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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.xero.template;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * Test the initial templating defaults - these are the templates that are
 * expanded in the initial phase. These are tests of templates that are not
 * runnable in JavaScript.
 * 
 * @author bwallace
 * 
 */
public class InitialTemplateTest {

   static TemplateContext tc = new TemplateContext();
   
   static ELContextImpl testContext = tc.getELContext();
   
   static Map<String, Object> vars = testContext.getVariables();
   static {
	  vars.put("v", 5);
	  vars.put("locale", Locale.CANADA);
   };
   
   static String[] evalTests = new String[]{
	 "<a:eval v='4' xmlns:a='http://www.dcm4che.org/xero/multi-template/initial'/>","4",
	 "<a:eval v='${v}' xmlns:a='http://www.dcm4che.org/xero/multi-template/initial'/>","5",
	 "<a:eval v='${vNot}' xmlns:a='http://www.dcm4che.org/xero/multi-template/initial'>6</a:eval>","6",
	 "<html xmlns:a='http://www.dcm4che.org/xero/multi-template/initial'><a:eval v='${vNot}'><body><a:eval v='${v}' /></body></a:eval></html>","<html xmlns:a=\"http://www.dcm4che.org/xero/multi-template/initial\"><body>5</body></html>",
   };
   
   @Test
   public void evalTest() throws Exception {
	  testExpansion(false,evalTests);
   }
   
   static String[] i18nTests = new String[]{
	 "<i:test key='NoTestKey' xmlns:i='http://www.dcm4che.org/xero/multi-template/i18n'>Fallback</i:test>", "Fallback", 
	 "<i:test key='TestKey' xmlns:i='http://www.dcm4che.org/xero/multi-template/i18n'>Don't find this fallback text as the key SHOULD be found.</i:test>", "Simple Test", 
   };

   /** Tests some internationalization expansions, defaulting to English, and then French. */
   @Test public void i18nTest() throws Exception {
	  testExpansion(false,i18nTests);
   }
   
   static String[] literalTests = new String[]{
	  // First phase test - should directly execute.
	  "<html xmlns:a='http://www.dcm4che.org/xero/multi-template/initial'>"
	  +"<a:literal name='lit' v='5' />"
	  +"<a:eval v='${lit.v}'/>"
	  +"</html>",
	  "<html xmlns:a=\"http://www.dcm4che.org/xero/multi-template/initial\">5</html>",
	  
	  // Second phase test - should just write out the literal data again...
//	  "<html xmlns:a='http://www.dcm4che.org/xero/multi-template/initial'>"
//	  +"<a:literal name='lit' v='5' phase='1'/>"
//	  +"<a:eval v='${lit.v}'/>"
//	  +"</html>",
//	  "<html xmlns:a=\"http://www.dcm4che.org/xero/multi-template/initial\">"
//	  +"<a:literal name='lit' v='5' phase='1'/>"
//	  +"<a:eval v='${lit.v}'/>"
//	  +"</html>",

   };
   /** Tests the literal, in-line assignment of values. */
   @Test public void literalTest() throws Exception {
	  testExpansion(true,literalTests);
   }

   /** Compares templates with their expected outputs, as pairs of strings with the tests array.  Uses the
    * static eval context to run the tests.
    */
   public static void testExpansion(boolean print, String[] tests) throws ParserConfigurationException, SAXException, IOException {
	  for (int i = 0; i < tests.length; i += 2) {
		 Template t = TemplateUtil.parseString(tests[i]);
		 assert t != null;
		 String result = TemplateUtil.mergeToString(tc, t);
		 assert result != null;
		 if (print) {
			System.out.println("Source for template=" + tests[i]);
			System.out.println("Expected result    =" + tests[i+1]);
			System.out.println("Result for template=" + result);
		 }
		 assert result.equals(tests[i+1]);
	  }
   }

}
