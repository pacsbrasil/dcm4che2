package org.dcm4chee.xero.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple URI resolver that always returns a resource for all resolutions.
 * 
 * @author bwallace
 */
public class ResourceURIResolver implements URIResolver {
   private static final Logger log = LoggerFactory.getLogger(ResourceURIResolver.class);

   static ClassLoader cl = Thread.currentThread().getContextClassLoader();

   URL resource;

   public ResourceURIResolver(String name) {
	  this.resource = cl.getResource(name);
   }

   public Source resolve(String href, String base) throws TransformerException {
	  log.info("Resolving " + href + " to " + resource);
	  InputStream is;
	  try {
		 is = resource.openStream();
		 assert is != null;
		 return new StreamSource(new InputStreamReader(is));
	  } catch (IOException e) {
		 e.printStackTrace();
		 return null;
	  }
   }

}
