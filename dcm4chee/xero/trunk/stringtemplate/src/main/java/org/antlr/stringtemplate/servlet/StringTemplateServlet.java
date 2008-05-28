package org.antlr.stringtemplate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

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
 * An abstract string template servlet.  Configurable in the config file for:
 * 	Resource path name for the top level string template group.
 *  Refresh rate for the top level group
 *  Content Type of the response
 * By default, the servlet will look for a template the same name as the path after the base path, without
 * any dot extensions.
 * 
 * It is possible to extend this class to define toString methods, parent template groups, data model etc.
 * @author bwallace
 *
 */
@SuppressWarnings("serial")
public class StringTemplateServlet extends HttpServlet{
   private static final Logger log = LoggerFactory.getLogger(StringTemplateServlet.class);

   /**
    * How long between page refreshes, in seconds.  Defaults to 5 minutes (300 seconds).
    */
   protected int refreshIntervalInSeconds = 300;

   /** The template group to use to get response templates */
   protected StringTemplateGroup stg;
   
   protected String characterEncoding = "UTF-8";
   protected String contentType = "text/html";
   protected String templates;
   
   /** Instantiate the requested template and return it */
   @SuppressWarnings("unchecked")
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  long start = System.nanoTime();
	  String templateName = getTemplateName(req);
	  if( templateName==null ) {
		 resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		 return;
	  }
	  if( contentType.equals("xhtml") ) {
		 if( req.getHeader("USER-AGENT").indexOf("MSIE")>=0 ) {
			resp.setContentType("text/html");
		 }
		 else {
			resp.setContentType("text/xml");
		 }
	  } else {
	          resp.setContentType(getContentType(req));
   	  }
	  resp.setCharacterEncoding(characterEncoding);
	  PrintWriter pw = resp.getWriter();
	  StringTemplatePrintWriter stpw = new StringTemplatePrintWriter(pw);
	  Map model = createModel(req,resp);
	  StringTemplateGroup group = getStringTemplateGroup(req);
	  StringTemplate st = group.getInstanceOf(templateName,model);
	  st.write(stpw);
	  pw.close();
	  long end = System.nanoTime();
	  log.info("Templating/writing {} took {} ms",templateName,(end - start) / (1e6));
   }

   /**
    * Gets the string template group to use for the specific request - allows different
    * groups to be used for particular browsers.
    */
   protected StringTemplateGroup getStringTemplateGroup(HttpServletRequest req) {
	  return stg;
   }

   /** Given the request, figure out what the template name is.  Return null for not found. */
   protected String getTemplateName(HttpServletRequest req) {
	  String name = req.getServletPath().substring(1);
	  int dot = name.lastIndexOf('.');
	  if( dot>0 ) name = name.substring(0,dot);
	  log.info("Templating path {}",name);
	  return name;
   }


   /** Creates the model to use for the given request.  By default just returns the parameter map. */
   @SuppressWarnings("unchecked")
   protected Map createModel(HttpServletRequest req, HttpServletResponse resp) {
	  return req.getParameterMap();
   }


   /** Returns the content type to use for this request - this version just returns the contentType variable,
    * but in general, it can depend on the request
    * @param req
    * @return
    */
   protected String getContentType(HttpServletRequest req) {
	  return contentType;
   }


   /** Just call doGet, as posting/getting are going to be handled the same way. */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  doGet(req, resp);
   }

   /** Creates a string template group object from the given resource name.
    * If name is comma-separated,   
    * @param name
    * @return
    */
   protected StringTemplateGroup createStringTemplateGroup(String name) {
	  ClassLoader cl = Thread.currentThread().getContextClassLoader();
	  String[] parents = name.split(",");
	  StringTemplateGroup parentSTG = null;
	  for(int i=parents.length-1; i>=0; i--) {
		  parents[i] = parents[i].trim();
		  URL url = cl.getResource(parents[i]);
		  if( url==null ) {
			 throw new IllegalArgumentException("No resource named "+name);
		  }
		  String rootDir = url.getFile();
		  StringTemplateGroup ret = new StringTemplateGroup(parents[i], rootDir);
		  log.info("root resource for {} is {}", parents[i],rootDir);
		  if( rootDir==null || rootDir.indexOf('!')>0) {
			 log.info("Using root resource {}",parents[i]);
			 // Just use the name directly as the root resource.
			 ret.setRootResource(parents[i]);
			 ret.setRootDir(null);
		  }
		  ret.setRefreshInterval(refreshIntervalInSeconds);
		  if( parentSTG!=null ) ret.setSuperGroup(parentSTG);
		  parentSTG = ret;
	  }
	  return parentSTG;
   };
   
   
   @Override
   public void init(ServletConfig config) throws ServletException {
	  super.init(config);
	  String test = config.getInitParameter("refreshInterval");
	  if (test != null && test.length() > 0) {
		 refreshIntervalInSeconds = Integer.parseInt(test);
		 log.info("Set refresh interval to " + refreshIntervalInSeconds);
	  }
	  
	  test = config.getInitParameter("templates");
	  if( test!=null ) templates = test;
	  // Can pre-define a set of templates name in the constructor, so this assert isn't unnecessary
	  assert templates!=null;
	  stg = createStringTemplateGroup(templates);
	  stg.setAttributeRenderers(StringSafeRenderer.RENDERERS);
	  
	  test = config.getInitParameter("contentType");
	  if( test!=null ) contentType = test;
   }
}
