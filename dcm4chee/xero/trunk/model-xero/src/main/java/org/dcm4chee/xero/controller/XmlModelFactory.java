package org.dcm4chee.xero.controller;

import java.io.IOException;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.model.MapFactory;
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

   String urlKey = "url";

   /** Get the URL and from that construct the return XML object */
   @SuppressWarnings("unchecked")
   public Object create(Map<String, Object> src) {
	  String url = (String) ((Map<String,Object>) src.get(urlKey)).get("url");
	  if (url != null) {
		 XmlModel ret = new XmlModel();
		 log.info("Getting XML model from " + url);
		 URIResolver resolver = (URIResolver) src.get("URIResolver");
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
