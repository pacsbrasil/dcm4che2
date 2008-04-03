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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps from one XML namespace to Template beans and to XML template defintions.
 * 
 * @author bwallace
 *
 */
public class BeanXmlTemplateMapper extends TemplateMapper implements MetaDataUser {
   static Logger log = LoggerFactory.getLogger(BeanXmlTemplateMapper.class);
   
   private String namespace;
   
   protected Map<String,TemplateCreator<?> > templates = new HashMap<String,TemplateCreator<?> >();

   /** Returns the namespace that gets mapped by this template handler. A single handler can only handle 1
    * namespace.  If it is ever necessary to handle multiple namespaces, some sort of combiner will be required.
    */
   public String getNamespace() {
	  return namespace;
   }
   
   /** Sets the namespace to handle - typically set by the metadata handler. */
   protected void setNamespace(String namespace) {
	  this.namespace = namespace;
   }
   
   /**
    * Return a new template object to handle the given node.
    * @param uri
    * @param localName
    * @param qName
    * @return a newly created template node that handles the given node.
    */
   @Override
   public TemplateCreator<?> getTemplateCreator(String uri, String qName) {
	  if( ! uri.equals(namespace) ) {
		 log.info("Looking for uri="+uri+" namespace="+namespace);
		 return null;
	  }
	  TemplateCreator<?> tc = templates.get(qName);
	  if( tc==null ) {
		 log.warn("Template not found for name "+qName);
		 return null;
	  }
	  return tc;
   }
   
   /**
    * Returns a set of the templates handled by this object.
    * @return
    */
   public Set<String> getTemplatesHandled() {
	  return templates.keySet();
   }

   /**
    * Use the child meta-data to figure out what the direct template creators are.
    * 
    */
   public void setMetaData(MetaDataBean mdb) {
	  this.namespace = (String) mdb.getValue("namespace");
	  for(Map.Entry<String,MetaDataBean> me : mdb.entrySet()) {
		 String key = me.getKey();
		 Object value = me.getValue().getValue();
		 if( value instanceof TemplateCreator ) {
			log.info("Found template bean for "+key+" of type "+value.getClass());
			templates.put(key,(TemplateCreator<?>) value);
		 }
		 else if( me.getValue().getValue("tml")!=null ) {
			parseTemplateFile((String) me.getValue().getValue("tml"));
		 }
	  }
   }

   /**
    * This parses the named template file and adds the templates from the file into the map of template
    * handlers.
    * @param value
    */
   protected void parseTemplateFile(String tml) {
	  log.info("Parsing template tml file"+tml);
	  
   }
}
