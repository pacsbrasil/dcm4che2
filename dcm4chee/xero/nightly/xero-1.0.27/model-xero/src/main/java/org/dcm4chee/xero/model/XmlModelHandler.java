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
package org.dcm4chee.xero.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles the SAX events required to parse the XmlModel object.
 * @author bwallace
 */
public class XmlModelHandler extends DefaultHandler {
   static final Logger log = LoggerFactory.getLogger(XmlModelHandler.class);
   
   protected List<XmlModel> xmlModels = new ArrayList<XmlModel>();
   protected XmlModel root;
   
   /** Sets up an Xml model handler, with all default parameters/values */
   public XmlModelHandler(XmlModel root) {
	  this.root = root;
   }
   
   /** Starts a new child element - registers the element with the parent element to ensure it gets parsed. */
   @SuppressWarnings("unchecked")
   @Override
   public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
	  XmlModel parent = null;
	  XmlModel child;
	  // Use the unqualified name...
	  if( localName!=null ) name = localName;
	  if( xmlModels.size()>0 ) {
		 parent = xmlModels.get(xmlModels.size()-1);
		 List<XmlModel> children = (List<XmlModel>) parent.get(name);
		 child = new XmlModel(attributes.getLength()+1);
		 if( children==null ) {
			log.debug("Created nested child "+name+" on "+parent);
			children = new ArrayList<XmlModel>();
			parent.put(name,children);
			child.put("xmlFirst", true);
		 }
		 else {
			child.put("xmlFirst", false);
		 }
		 child.setParent(parent);
		 children.add(child);
	  }
	  else {
		 child = root;
	  }
	  for(int i=0, n=attributes.getLength(); i<n; i++) {
		 if( log.isDebugEnabled() ) log.debug("Setting attribute "+attributes.getQName(i)+"="+attributes.getValue(i) + " on "+child);
		 child.put(attributes.getQName(i), attributes.getValue(i));
	  }
	  xmlModels.add(child);
   }

   /** Remove the last element */
   @Override
   public void endElement(String uri, String localName, String name) throws SAXException {
	  xmlModels.remove(xmlModels.size()-1);
   }

}
