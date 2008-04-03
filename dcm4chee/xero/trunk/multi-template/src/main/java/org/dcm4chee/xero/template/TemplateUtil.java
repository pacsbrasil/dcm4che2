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
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Some helpful utilities to test and deal with templates 
 * 
 * @author bwallace
 *
 */
public class TemplateUtil {

   public static TemplateMapper INITIAL_TEMPLATES = (TemplateMapper) StaticMetaData.getMetaData("multi-templates.metadata").getValue("templates.initial");
   
   /** Merge a simple, context free template to a string */
   public static String mergeToString(Template t) throws IOException {
	  return mergeToString(null, t);
   }
   
   /** Merge a template with a context to a string */
   public static String mergeToString(TemplateContext context, Template t) throws IOException {
	  StringWriter sw = new StringWriter();
	  t.merge(context, sw);
	  return sw.toString();
   }
   
   /** Returns a template generated from the given text. Text must be XML or raw string data not containing
    * any < characters.
    * @throws SAXException 
    * @throws ParserConfigurationException 
    * @throws IOException 
    */
   public static Template parseString(String s) throws ParserConfigurationException, SAXException, IOException {
	  if( s.indexOf('<')<0 ) return new TextTemplate(s);
	  return parse(INITIAL_TEMPLATES, new InputSource(new StringReader(s)));
   }
   
   public static Template parse(TemplateMapper templates, InputSource is) throws ParserConfigurationException, SAXException, IOException {
	  SAXParserFactory spf = SAXParserFactory.newInstance();
	  spf.setFeature("http://xml.org/sax/features/namespaces", true);
	  spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
	  SAXParser sp = spf.newSAXParser();
	  TemplateContentHandler tch = new TemplateContentHandler(templates);
	  sp.parse(is, tch);
	  return tch.getTemplate();
   }
}
