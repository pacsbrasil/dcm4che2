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
import java.io.Writer;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.dcm4chee.xero.metadata.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * Handles loading/usage of internationalized templates
 * @author bwallace
 */
public class I18NTemplate extends TemplateSelfCreator<I18NTemplate> {
   static final Logger log = LoggerFactory.getLogger(I18NTemplate.class);
   
   String key;
   String resource = "messages";

   @Override
   public void merge(TemplateContext context, Writer writer) throws IOException {
	  Locale locale = (Locale) context.getELContext().getVariables().get("locale");
	  if( locale==null ) locale = Locale.getDefault();
	  ResourceBundle rb = ResourceBundle.getBundle(resource,locale);
	  if( rb==null ) {
		 log.warn("Can't find resource bundle "+resource);
		 child.merge(context,writer);
		 return;
	  }
	  try {
		 String value =  rb.getString(key);
		 writer.write(value);
	  }
	  catch(MissingResourceException e) {
		 child.merge(context,writer);
		 return;
	  }
   }

   @Override
   public I18NTemplate createInitialTemplate(String uri, String localName, String qName, Attributes atts) {
	  I18NTemplate i18nTemplate = super.createInitialTemplate(uri,localName,qName,atts);
	  i18nTemplate.key = atts.getValue("key");
	  if( i18nTemplate.key==null ) {
		 throw new IllegalArgumentException("I18N tag must have a key.");
	  }
	  i18nTemplate.resource = this.resource;
	  return i18nTemplate;
   }

   @MetaData
   public void setResource(String resource) {
	  System.out.println("Setting resource to "+resource);
	  this.resource = resource;
   }
   
}
