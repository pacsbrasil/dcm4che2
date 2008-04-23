package org.dcm4chee.xero.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet takes a stringtemplate object and uses it as a DATA structure and applies the 
 * given, named template.
 * 
 * @author bwallace
 *
 */
public class JSTemplateServlet extends HttpServlet {
   static final Logger log = LoggerFactory.getLogger(JSTemplateServlet.class);
   static ClassLoader cl = Thread.currentThread().getContextClassLoader();

   /**
    * The stg group contains the templates to use to render OTHER templates into JavaScript
    */
   protected StringTemplateGroup stg;
   
   /**
    * The stgData group contains the OTHER template to be rendered into JavaScript by stg.
    */
   protected StringTemplateGroup stgData;

   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  long start = System.nanoTime();
	  StringTemplate st = stg.getInstanceOf("jstemplate");	  
	  List<String> templates = new ArrayList<String>(1);
	  templates.add("xeroBody");
	  st.setAttribute("templates", FindAllTemplates.findAllTemplates(stgData, templates));
	  st.setAttribute("templatesName","xeroTemplates");
	  st.setAttribute("nameJSTemplate", new NameJSTemplate());
	  String stProg = st.toString();
	  log.info("Generating xeroBody templates took "+((System.nanoTime()-start)/1e6)+" ms");

	  start = System.nanoTime();
	  StringTemplate stClientJS = stgData.getInstanceOf("clientjs");
	  String clientJS = stClientJS.toString();
	  log.info("Generating clientjs body took "+((System.nanoTime()-start)/1e6)+" ms");
	  
	  response.setContentType("text/javascript");
	  response.setCharacterEncoding("UTF-8");
	  PrintWriter pw = response.getWriter();
	  pw.println(stProg);
	  pw.println(clientJS);
	  pw.close();
   }



   /** Initialize the string template groups to be used to render the JavaScript text */
   public void init(ServletConfig config) throws ServletException {
	  super.init(config);
	  String dataDir = cl.getResource("xero").getFile();
	  stgData = new StringTemplateGroup("xero", dataDir);
	  String s = config.getInitParameter("refreshInterval");
	  String rootDir = cl.getResource("jstemplate").getFile();
	  stg = new StringTemplateGroup("jstemplate", rootDir);
	  if (s != null && s.length() > 0) {
		 int refreshInterval = Integer.parseInt(s);
		 log.info("Set JS Template Servlet refresh interval to " + refreshInterval);
		 stgData.setRefreshInterval(refreshInterval);
		 stg.setRefreshInterval(refreshInterval);
	  }
	  else {
		 stgData.setRefreshInterval(15);
		 stg.setRefreshInterval(15);
	  }

	  stg.setAttributeRenderers(JSStringSafeRenderer.RENDERERS);
   }
   

}
