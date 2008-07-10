package org.dcm4chee.xero.controller;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.template.MapWithDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class RequestValidatorTest {
   private static final Logger log = LoggerFactory.getLogger(RequestValidatorTest.class);
   MetaDataBean mdb = StaticMetaData.getMetaData("xero-model.metadata");
   MetaDataBean model = mdb.get("model");

   /**
    * Tests that the default study query tests succeeds on valid data.
    */
   @SuppressWarnings("unchecked")
   @Test
   public void succeedQueryTest() {
	  assert mdb!=null;
	  assert model!=null;
	  assert model.get("query")!=null;
	  MapWithDefaults mwd = new MapWithDefaults(model);
	  Map<String,Object> request = new HashMap<String,Object>();
	  mwd.put(RequestValidator.PARAMETERS, request);
	  request.put("ModalitiesInStudy", "CT");
	  Map<String,Object> rv = (Map<String,Object>) mwd.get("query");
	  assert rv!=null;
	  assert "CT".equals(rv.get("ModalitiesInStudy"));
	  String url = (String) rv.get("url");
	  assert url!=null;
	  log.info("URL for request validator="+url);
	  assert "/wado2/study.xml?ModalitiesInStudy=CT".equals(url);
   }
}
