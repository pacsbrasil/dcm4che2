package org.dcm4chee.xero.controller;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.access.MapWithDefaults;
import org.dcm4chee.xero.model.ResourceURIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class XmlModelFactoryTest {
   @SuppressWarnings("unused")
   private static final Logger log = LoggerFactory.getLogger(XmlModelFactoryTest.class);
   MetaDataBean mdb = StaticMetaData.getMetaData("xero-model.metadata");
   MetaDataBean model = mdb.get("model");

   /** Tests to see that the study rows object can be successfully read, if the
    * correct URIResolver is provided.
    */
   @SuppressWarnings("unchecked")
   @Test
   public void studyRowsCreateTest() {
	  MapWithDefaults mwd = new MapWithDefaults(model);
	  Map<String,Object> parameters = new HashMap<String,Object>();
	  mwd.put(RequestValidator.PARAMETERS, parameters);
	  parameters.put("ModalitiesInStudy", "CR");
	  mwd.put(XmlModelFactory.URIRESOLVER, new ResourceURIResolver("studyRows.xml"));
	  Map<String,Object> search = (Map<String,Object>) mwd.get("search");
	  assert search!=null;
   }
}
