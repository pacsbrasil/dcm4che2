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
package org.dcm4chee.xero.controller;

import java.io.IOException;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.access.MapFactory;
import org.dcm4chee.xero.model.XmlModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Given a url, this factory knows how to create an XmlModel that has the
 * contents of the URL. Also needs the request in order to allow getting the URL
 * information.
 * 
 * @author bwallace
 * 
 */
public class XmlModelFactory implements MapFactory {
   private static final Logger log = LoggerFactory.getLogger(XmlModelFactory.class);
   
   public static final String URIRESOLVER = "_URIResolver";

   String urlKey = "url";

   /** Get the URL and from that construct the return XML object */
   @SuppressWarnings("unchecked")
   public Object create(Map<String, Object> src) {
	  String url = (String) ((Map<String,Object>) src.get(urlKey)).get("url");
	  if (url != null) {
		 XmlModel ret = new XmlModel();
		 log.info("Getting XML model from " + url);
		 URIResolver resolver = (URIResolver) src.get(URIRESOLVER);
		 try {
			Source source = resolver.resolve(url, "");
			log.info("Resolved "+url+" to "+source);
			ret.setSource(source);
			return ret;
		 } catch (TransformerException e) {
			log.warn("Failed to resolve " + url + " reason:" + e, e);
			return null;
		 }
		 catch(IOException e) {
			log.warn("Failed to read "+url+" reason:"+e,e);
			return null;
		 }
		 catch(SAXException e) {
			log.warn("Failed to parse "+url+" reason:"+e,e);
			return null;
		 }
	  }
	  log.info("No url found for " + urlKey);
	  return null;
   }

   /**
    * Sets the key to look for the query URL string from.
    * 
    * @param urlKey
    */
   @MetaData
   public void setUrl(String urlKey) {
	  this.urlKey = urlKey;
   }
}
