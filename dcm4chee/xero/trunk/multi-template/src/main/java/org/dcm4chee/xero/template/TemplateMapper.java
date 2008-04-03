package org.dcm4chee.xero.template;

import org.dcm4chee.xero.metadata.MetaDataUser;

public abstract class TemplateMapper {

   public TemplateMapper() {
	  super();
   }

   public abstract TemplateCreator<?> getTemplateCreator(String uri, String localName);

}