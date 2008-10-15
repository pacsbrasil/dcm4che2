package org.dcm4chee.xero.model;

import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.access.ValueList;
import org.testng.annotations.Test;

/**
 * This tests the plugins for the static data - ensuring that meta-data is plugged into the various 
 * model elements correctly.
 * The data MUST be plugged into the "static" model to be useable in that way.
 * 
 * @author bwallace
 *
 */
public class StaticJSModelTest {

	MetaDataBean mdb = StaticMetaData.getMetaData("test-model.metadata");
	MetaDataBean stat = mdb.getChild("model");
	JSTemplate jst = new JSTemplate(stat,"xeroModelTests", "xeroModel");

	/**
	 * Tests that the scripts value is well defined and contains the value required for the script plugin test
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void valueScriptPluginTest() {
		assert stat!=null;
		
		Map<String,Object> model = jst.getModel();
		assert model!=null;
		model = (Map<String, Object>) model.get("static");
		
		MetaDataBean scripts = (MetaDataBean) model.get("scripts");
		assert scripts!=null;
		ValueList scriptsV = (ValueList) scripts.get("value");
		assert scriptsV!=null;
		assert scriptsV.contains("js/sarissa.js");
	}
	
	/**
	 * Tests that scripts can be correctly plugged in.
	 * This runs all the tests in the staticPluginTest
	 */
	@Test
	public void staticPluginTest() {
		runTest("staticPluginTest");
	}
	
	
	/** Uses the static meta-data as the model and then just calls the generic  
	 * runTest for JavaScript.
	 * @param jsname
	 */
	public void runTest(String jsname) {
		jst.runTest(jsname);
	}
	public void runTestD(String jsname) {
		jst.runTest(jsname,true);
	}
}
