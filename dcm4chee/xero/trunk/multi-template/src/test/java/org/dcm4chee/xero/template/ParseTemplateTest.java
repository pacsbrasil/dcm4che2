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

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * Tests that XML parsing of templates works correctly.
 * 
 * @author bwallace
 */
public class ParseTemplateTest {
   
   static final String simpleText = "This is some simple text without any XML in it.";
   static final String simpleHtml = "<html><body><h1>This is some simple HTML text without namespaces or any expansions.</h1><p>This should expand to a simple TextTemplate or ListTemplate containing a single TextTemplate.</p></body></html>";
   static final String attributeHtml = "<html><body a=\"1\" c=\"&amp;\" b=\"\\\" d=\"4\"><h1 size=\"huge\" style=\"background:green;\">This is some simple HTML text without namespaces or any expansions.</h1><p>This should expand to a simple TextTemplate or ListTemplate containing a single TextTemplate.</p></body></html>";
   static final String xmlnsHtml = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body xmlns:svg=\"http://www.w3.org/2000/svg\"><h1>This is some simple HTML text without namespaces or any expansions.</h1><p>This should expand to a simple TextTemplate or ListTemplate containing a single TextTemplate.</p></body></html>";
   
   /** Tests that the XML parser can handle simple text. */
   @Test
   public void simpleTextTest() throws Exception {
	  testSame(false,simpleText);
   }
   
   /** Test that the XML parser can handle next XML structure, AND when it does it produces a single output */
   @Test
   public void simpleHtmlTest() throws Exception {
	  testSame(false,simpleHtml);
	  
	  Template t= TemplateUtil.parseString(simpleHtml);
	  // It doesn't matter if the result is a list of a single text template or a text template itself.
	  if( t instanceof ListTemplate ) {
		 ListTemplate lt = (ListTemplate) t;
		 assert lt.getTemplates().size()==1;
		 assert lt.getTemplates().get(0) instanceof TextTemplate;
	  }
	  else {
		 assert t instanceof TextTemplate;
	  }
   }

   /** Test that the parser can handle attribute encodings in the html text */
   @Test
   public void attributeHtmlTest() throws Exception {
	  testSame(false,attributeHtml);
   }
   
   /** Tests that XMLNS name spaces are maintained */
   @Test
   public void xmlnsTest() throws Exception {
	  testSame(false,xmlnsHtml);
   }

   public String testSame(boolean print, String text) throws ParserConfigurationException, SAXException, IOException {
	  Template t= TemplateUtil.parseString(text);
	  assert t!=null;
	  String result = TemplateUtil.mergeToString(t);
	  assert result!=null;
	  if( print ) {
		 System.out.println("Source for template="+text);
		 System.out.println("Result for template="+text);
	  }
	  assert result.equals(text);
	  
	  return result;
   }
}
