package org.dcm4chee.xero.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.dcm4chee.xero.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * This class tests rendering the xero templates into JavaScript.
 * 
 * @author bwallace
 */
public class JSTemplateTest {
   static final Logger log = LoggerFactory.getLogger(JSTemplateTest.class);

   static ClassLoader cl = Thread.currentThread().getContextClassLoader();

   static String rootDir = cl.getResource("jstemplate").getFile();

   static String dataDir = cl.getResource("jstest").getFile();

   // This copy of stgData is used as the base data to render.
   static StringTemplateGroup stgData = new StringTemplateGroup("jstest", dataDir);

   static StringTemplateGroup stgRender = new StringTemplateGroup("jstest", dataDir);

   static StringTemplateGroup stg = new StringTemplateGroup("jstemplate", rootDir);
   static {
	  stg.setAttributeRenderers(StringSafeRenderer.JS_RENDERERS);
	  stgRender.setAttributeRenderers(StringSafeRenderer.JS_RENDERERS);
	  stgData.setAttributeRenderers(StringSafeRenderer.JS_RENDERERS);
   }

   @Test
   public void simpleTextTest() throws Exception {
	  runTest("simpleText",true);
   }
   @Test
   public void elseifTest() throws Exception {
	  runTest("elseif",false);
   }
   @Test
   public void ifTest() throws Exception {
	  runTest("if",false);
   }
   @Test
   public void includeTest() throws Exception {
	  runTest("include",false);
   }
   @Test
   public void idTest() throws Exception {
	  runTest("id",false);
   }
   @Test
   public void includeArgTest() throws Exception {
	  runTest("includeArg",false);
   }
   @Test
   public void includeArgsTest() throws Exception {
	  runTest("includeArgs",false);
   }
   @Test
   public void anonTemplateTest() throws Exception {
	  runTest("anonTemplate",false);
   }
   @Test
   public void anonTemplatesTest() throws Exception {
	  runTest("anonTemplates",false);
   }
   @Test
   public void anonsTemplateTest() throws Exception {
	  runTest("anonsTemplate",false);
   }
   @Test
   public void anonTemplateTemplateTest() throws Exception {
	  runTest("anonTemplateTemplate",false);
   }
   @Test
   public void applyTemplateTest() throws Exception {
	  runTest("applyTemplate",false);
   }
   @Test
   public void separatorTest() throws Exception {
	  runTest("separator",false);
   }
   @Test
   public void dotTest() throws Exception {
	  runTest("dot",false);
   }
   @Test
   public void valueTest() throws Exception {
	  runTest("value",false);
   }
   @Test
   public void separatorAnonTest() throws Exception {
	  runTest("separatorAnon",false);
   }
   @Test
   public void formatTest() throws Exception {
	  runTest("format",false);
   }
   
   public void runTest(String testKey, boolean verbose) throws Exception {
	  ScriptEngineManager sem = new ScriptEngineManager();
	  ScriptEngine se = sem.getEngineByExtension("js");
	  assert se != null;

	  String expected = generateJavaResult(testKey);
	  String js = generateScript(testKey,expected);
	  if( verbose ) {
		 log.info("Testing "+testKey+" expected result "+expected+" using script\n"+js);
	  }
	  try {
		 se.eval(js);
	  } catch (ScriptException e) {
		 log.warn("Caught exception {} on line {}", e.getMessage(), e.getLineNumber());
		 int line = e.getLineNumber();
		 String[] splits = StringUtil.split(js, '\n', true);
		 for (int i = Math.max(0, line - 5), n = Math.min(splits.length - 1, line + 5); i < n; i++) {
			log.info("{}: {}", i + 1, splits[i]);
		 }
		 assert false;
	  }
   }

   /** Generates the Java side of the string template tests */
   public static String generateJavaResult(String testKey) {
	  StringTemplate testTemplate = stgRender.getInstanceOf(testKey);
	  testTemplate.setAttribute("a", "eh");
	  testTemplate.setAttribute("b", 2);
	  testTemplate.setAttribute("c", "sea");
	  testTemplate.setAttribute("lst", 1);
	  testTemplate.setAttribute("lst", 2);
	  testTemplate.setAttribute("lst", 3);
	  testTemplate.setAttribute("lst2", 2);
	  testTemplate.setAttribute("lst2", 3);
	  testTemplate.setAttribute("lst2", 4);
	  testTemplate.setAttribute("ampValue","This & string has two & and two << signs.");
	  Map<String,Object> person = new HashMap<String,Object>();
	  person.put("name","Bill");
	  testTemplate.setAttribute("person", person);
	  String javaResult = testTemplate.toString();
	  return javaResult;
   }
   
   /** Reads the given resource into a string */
   public static String readResource(String name) throws IOException {
	  InputStream is = cl.getResource(name).openStream();
	  int size = is.available();
	  byte[] data = new byte[size];
	  is.read(data);
	  is.close();
	  return new String(data,"UTF-8");
   }

   /** Output the javascript that performs the required test. 
    * @throws IOException */
   public static String generateScript(String testKey, String expected) throws IOException {
	  StringTemplate st = stg.getInstanceOf("jstemplate");
	  List<String> templates = new ArrayList<String>(1);
	  templates.add(testKey);
	  st.setAttribute("templates", FindAllTemplates.findAllTemplates(stgData, templates));
	  st.setAttribute("templatesName","testTemplates");
	  st.setAttribute("nameJSTemplate", new NameJSTemplate());
	  
	  StringTemplate stgTest = stgData.getInstanceOf("testtemplate");
	  stgTest.setAttribute("expected", expected);
	  stgTest.setAttribute("template", testKey);
	  String log = readResource("model/rhino.js") + "\n" + readResource("model/logging.js");
	  String stProg = st.toString();
	  String testProg = stgTest.toString();
	  return log+"\n"+stProg+"\n"+testProg;
   }
}
