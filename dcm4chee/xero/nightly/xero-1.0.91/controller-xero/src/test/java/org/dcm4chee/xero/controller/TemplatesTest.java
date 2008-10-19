package org.dcm4chee.xero.controller;

import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TemplatesTest {
   private static final Logger log = LoggerFactory.getLogger(TemplatesTest.class);
   MetaDataBean mdb = StaticMetaData.getMetaData("xero.metadata");
   MetaDataBean model = mdb.getChild("model");

	@Test
	public void allTemplatesTest() {
		assert model!=null;
		MetaDataBean templatesMdb = model.getChild("templates");
		assert templatesMdb!=null;
		assert templatesMdb.getValue()!=null;
		Map<?,?> mapModel = (Map<?,?>) model.getValue();
		assert mapModel!=null;
		Map<?,?> templates = (Map<?,?>) mapModel.get("templates");
		assert templates!=null;
		assert templates.size()>=3;
		assert templates.keySet().size()==templates.size();
	}
	
	@Test
	public void renderAllTemplatesTest() {
		assert mdb.getChild("jscontroller")!=null;
		assert mdb.getChild("jscontroller").getChild("stringTemplate")!=null;
		assert mdb.getChild("jscontroller").getChild("stringTemplate").getChild("model")!=null;
		MetaDataBean templatesMdb = mdb.getChild("jscontroller").getChild("stringTemplate").getChild("model").getChild("templates");
		assert templatesMdb!=null;
		assert templatesMdb.getValue()!=null;
		Map<?,?> mapModel = (Map<?,?>) model.getValue();
		
		StringTemplateGroup stg = (StringTemplateGroup) model.getValue("templateGroup");
		assert stg!=null;
		StringTemplate st = stg.getInstanceOf("xeroView",mapModel);
		String viewjs = st.toString();
		assert viewjs!=null;
		// TODO - add more viewjs tests - perhaps parse it to ensure it is valid JS.
		log.debug("xeroView="+viewjs);
	}
	
	
}
