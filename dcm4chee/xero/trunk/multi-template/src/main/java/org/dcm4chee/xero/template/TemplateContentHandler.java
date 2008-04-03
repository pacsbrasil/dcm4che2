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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class handles the creation of the template as a SAX input handler.
 * 
 * @author bwallace
 */
public class TemplateContentHandler extends DefaultHandler {
   private static final Logger log = LoggerFactory.getLogger(TemplateContentHandler.class);
   
   /** A ListTemplate that is being created/setup currently, if any */
   protected ListTemplate listTemplate;
   
   /** The buffer that is being used to accumulate text output */
   protected StringBuffer buffer = new StringBuffer();
   
   /** The template mapper maps the URI/qName to the template to render.  A mapper must be found but it can 
    * return null for some or all names. 
    */
   protected TemplateMapper templateMapper;
   
   /**
    * Contains a stack of tags that have been opened and not yet closed.
    */
   protected List<TagContent> openedTags = new ArrayList<TagContent>();

   /** Indicates if there is an open element tag hanging around. */
   protected boolean open = false;

   public TemplateContentHandler(TemplateMapper mapper) {
	  this.templateMapper = mapper;
   }
   
   /** Finishes writing a start tag -used so that start tags containing no children can be written in the shorter form. */
   protected void completeOpen() {
	  if( open ) {
		 buffer.append(">");
		 open = false;
	  }
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
	  completeOpen();
	  TemplateCreator tcreator = templateMapper.getTemplateCreator(uri, localName);
	  
	  if( tcreator==null ) {
		 log.debug("No template mapper found for "+uri+" "+localName);
		 openedTags.add(new TagContent(qName));
		 buffer.append("<").append(qName);
		 for (int i = 0, n = atts.getLength(); i < n; i++) {
			String aq = atts.getQName(i);
			String av = atts.getValue(i);
			buffer.append(" ").append(aq).append("=\"");
			appendAttribute(buffer, av);
			buffer.append('"');
		 }
		 open = true;
		 return;
	  }
	  
	 bufferToTemplate();
	 TagContent tc = new TagContent(qName);
	 openedTags.add(tc);
	 tc.template = tcreator.createInitialTemplate(uri,localName,qName,atts);
	 tc.templateCreator = tcreator;
	 tc.listTemplate = listTemplate;
	 listTemplate = null;
   }
   
   /** Appends a attribute to the string buffer, encoding special characters */
   public static void appendAttribute(StringBuffer buf, String av) {
	  int start = 0;
	  int n = av.length();
	  for(int i=0; i<n; i++ ) {
		 char ch = av.charAt(i);
		 switch(ch) {
		 	case '"':
		 	   buf.append(av.substring(start,i)).append("&quot;");
		 	   start = i+1;
		 	   break;
		 	case '&':
		 	   buf.append(av.substring(start,i)).append("&amp;");
		 	   start = i+1;
		 	   break;
		 	case '<':
		 	   buf.append(av.substring(start,i)).append("&lt;");
		 	   start = i+1;
		 	   break;
		 }
	  }
	  if( start < n ) {
		 buf.append(av.substring(start));
	  }
   }

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
	  completeOpen();
	  buffer.append(ch,start,length);
   }

   @SuppressWarnings("unchecked")
   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
	  TagContent tc = openedTags.remove(openedTags.size()-1);
	  if( open ) {
		 buffer.append("/>");
		 open = false;
	  }
	  else if( tc.template != null) {
		bufferToTemplate();
		// It isn't clear how to cast this to agree with all the types, so just leave it unchecked.
		tc.templateCreator.setChild(tc.template, getTemplate());
		listTemplate = tc.listTemplate;
		appendTemplate(tc.template);
	  } else {
		 buffer.append("</").append(tc.qName).append(">");
	  }
   }

   @Override
   public void endDocument() {
	  bufferToTemplate();
   }
   
   /** Converts any characters in buffer into a TextElement and then adds the text element to the 
    * end of an ongoing list element (if any).  Make template equal to the current overall template.
    */
   protected void bufferToTemplate() {
	  if( buffer.length()>0 ) {
		 TextTemplate text = new TextTemplate(buffer.toString());
		 appendTemplate(text);
		 buffer.delete(0,buffer.length());
	  }
   }
   
   protected void appendTemplate(Template tmpl) {
	  if( listTemplate==null ) {
		 listTemplate = new ListTemplate();
	  }
	  listTemplate.getTemplates().add(tmpl);
   }

   /** Gets the template at the end of parsing */
   public Template getTemplate() {
	  if( listTemplate==null ) return null;
	  return listTemplate.crush();
   }
}

class TagContent {
   public TagContent(String qName) {
	  this.qName = qName;
   }
   
   @SuppressWarnings("unchecked")
   TemplateCreator templateCreator;
   String qName;
   Template template;
   ListTemplate listTemplate;
}