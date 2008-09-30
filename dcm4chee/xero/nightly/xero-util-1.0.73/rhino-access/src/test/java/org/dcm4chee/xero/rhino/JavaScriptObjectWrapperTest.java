package org.dcm4chee.xero.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests that JavaScript objects can be correctly wrapped by Java.
 * @author bwallace
 *
 */
public class JavaScriptObjectWrapperTest {
	private static final Logger log = LoggerFactory.getLogger(JavaScriptObjectWrapperTest.class);
	
	ContextFactory cf;
	Context cx;
	static final String jsFixed = ""
		+"var simpleString='Simple String';\n"
		+"\n"
		+"function TestObj(a,b) {\n"
		+"   this.a = a;\n"
		+"   this.b = b;\n"
		+"}\n"
		+"TestObj.prototype.nul = null;\n"
		+"TestObj.prototype.dbl = 1.1;\n"
		+"TestObj.prototype.f = function(x,y) { return x+y+this.b; };\n"
		+"var statObj = new TestObj('eh',3);\n"
		;
	static final String jsDynamic = ""
		+"var dynamicString='dynamic';\n"
		+"var dynObj = new TestObj('ay',-3);\n"
		;
	ScriptableObject scopeFixed;
	Scriptable scopeDynamic;
	
	JavaScriptObjectWrapper jsow;
	
   @BeforeMethod
	void init() {
	   cf = new ContextFactory();
	   cx = cf.enterContext();
	   scopeFixed = cx.initStandardObjects();
	   cx.evaluateString(scopeFixed, jsFixed, "<jsFixed>", 1, null);
	   scopeFixed.sealObject();
	   scopeDynamic = cx.newObject(scopeFixed);
	   scopeDynamic.setPrototype(scopeFixed);
	   scopeDynamic.setParentScope(scopeFixed);
	   cx.evaluateString(scopeDynamic,jsDynamic, "<jsDynamic>", 1, null);
	   jsow = new JavaScriptObjectWrapper(scopeDynamic);
   }

   @Test 
   public void test_get_returnValues() {
   	assert "Simple String".equals(jsow.get("simpleString"));
   	assert "Simple String".equals(jsow.getString("simpleString"));
   	JavaScriptObjectWrapper dynObj = (JavaScriptObjectWrapper) jsow.get("dynObj");
   	assert dynObj!=null;
   	assert "ay".equals(dynObj.get("a"));
   	assert dynObj.get("b").equals(-3);
   	assert dynObj.get("nul")==null;
   }
   
   @Test
   public void test_callMethod_returnSum() {
   	JavaScriptObjectWrapper dynObj = jsow.getObject("dynObj");
   	dynObj.callMethod("f", 1,2).equals(0);
   }

   @Test
   public void test_put_sameValue() {
   	jsow.put("putV", "Put Value");
   	assert jsow.get("putV")=="Put Value";
   	JavaScriptObjectWrapper dynObj = jsow.getObject("dynObj");
   	jsow.put("dynCopy", dynObj);
   	JavaScriptObjectWrapper dynCopy = jsow.getObject("dynCopy");
   	assert dynCopy.scriptable==dynObj.scriptable;
   }
   
   @Test
   public void test_contains_returnsBoolean() {
   	assert jsow.containsKey("dynObj");
   	assert !jsow.containsKey("notFound");
   }
   
   @Test
   public void testJsFixed_simpleValues() {
   	String str;
   	str = (String) ScriptableObject.getProperty(scopeFixed,"simpleString");
   	assert str=="Simple String";

   	str = (String) ScriptableObject.getProperty(scopeDynamic,"simpleString");
   	assert str=="Simple String";

   	str = (String) ScriptableObject.getProperty(scopeDynamic,"dynamicString");
   	assert str=="dynamic";
   	
   	Scriptable statObj = (Scriptable) ScriptableObject.getProperty(scopeDynamic,"statObj");
   	assert statObj!=null;
   	Object stata = ScriptableObject.getProperty(statObj,"a");
   	log.info("Typeof statObj.a={}", stata.getClass());
   	Object statb = ScriptableObject.getProperty(statObj,"b");
   	log.info("Typeof statObj.b={}", statb.getClass());
   	Object statnul = ScriptableObject.getProperty(statObj,"nul");
   	assert statnul==null;
   	Object statq = ScriptableObject.getProperty(statObj,"q");
   	log.info("Typeof statObj.q={}", statq.getClass());
   	Object statdbl = ScriptableObject.getProperty(statObj,"dbl");
   	log.info("Typeof statObj.dbl={}", statdbl.getClass());
   	Scriptable dynObj = (Scriptable) ScriptableObject.getProperty(scopeDynamic,"dynObj");
   	assert dynObj!=null;
   }
   
}
