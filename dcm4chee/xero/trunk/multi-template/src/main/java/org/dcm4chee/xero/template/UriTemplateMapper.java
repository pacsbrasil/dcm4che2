package org.dcm4chee.xero.template;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriTemplateMapper extends TemplateMapper implements MetaDataUser {
   static Logger log = LoggerFactory.getLogger(UriTemplateMapper.class);
   
   Map<String, TemplateMapper> templateMappers = new HashMap<String,TemplateMapper>();
   
   @Override
   public TemplateCreator<?> getTemplateCreator(String uri, String qName) {
	  TemplateMapper tm = templateMappers.get(uri);
	  log.debug("Looking for template mapper for uri="+uri+" found "+tm);
	  if( tm!=null ) return tm.getTemplateCreator(uri, qName);
	  return null;
   }

   /** Read the child template mappers by namespace */
   public void setMetaData(MetaDataBean mdb) {
	  for(Map.Entry<String,MetaDataBean> me : mdb.entrySet()) {
		 Object val = me.getValue().getValue();
		 if( val instanceof TemplateMapper ) {
			TemplateMapper tm = (TemplateMapper) val;
			String ns = (String) me.getValue().getValue("namespace");
			log.info("Found a template mapper for namespace "+ns);
			templateMappers.put(ns,tm);
		 }
	  }
   }

}
