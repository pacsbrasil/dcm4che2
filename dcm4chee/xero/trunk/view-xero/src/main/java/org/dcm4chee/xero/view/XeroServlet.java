/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.model.MapWithDefaults;
import org.dcm4chee.xero.model.UrlUriResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves up the Xero display as HTML/JavaScript or as straight HTML
 * for standard browsers, mobile browsers or IE. Standard browsers are assumed
 * to support SVG and generic HTML/CSS 2 and JavaScript Mobile browsers are
 * assumed not to support VML or SVG, but do support JavaScript (usually) IE
 * supports a somewhat broken HTML/CSS/JavaScript
 * 
 * The view component is rendered using stringtemplate, a templating library
 * based on antlr. The model that is used with this view is based on the
 * meta-data configured for the xero view being requested. Different views can
 * request different templates/versions.
 * 
 * @author bwallace
 * 
 */
@SuppressWarnings("serial")
public class XeroServlet extends HttpServlet {
   static final Logger log = LoggerFactory.getLogger(XeroServlet.class);

   static ClassLoader cl = Thread.currentThread().getContextClassLoader();

   MetaDataBean mdb;

   /**
    * How long between page refreshes, in seconds TODO change this to a big
    * value later on.
    */
   protected int refreshIntervalInSeconds = 30;

   protected StringTemplateGroup stg;
   
   /** Instantiate the requested template and return it */
   @SuppressWarnings("unchecked")
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  long start = System.nanoTime();
	  resp.setContentType("text/html");
	  resp.setCharacterEncoding("UTF-8");
	  PrintWriter pw = resp.getWriter();
	  StringTemplatePrintWriter stpw = new StringTemplatePrintWriter(pw);
	  MapWithDefaults mwd = new MapWithDefaults(mdb);
	  mwd.put("parameters", req.getParameterMap());
	  mwd.put("URIResolver", new UrlUriResolver(req,resp,getServletContext()));
	  StringTemplate st = stg.getInstanceOf("xero",mwd);
	  st.write(stpw);
	  pw.close();
	  long end = System.nanoTime();
	  log.info("Templating/writing took " + ((end - start) / (1e6)) + " ms");
   }

   /**
    * Returns the requested language - currently only default is supported
    * (English)
    */
   protected String getLanguage(HttpServletRequest req) {
	  return "default";
   }

   /**
    * Returns the browser to use for looking up the template - currently returns
    * ONLY html or ie - that is, a standard html browser or IE. May in the
    * future support mobile and potentially other devices, and or specific
    * versions of browsers.
    */
   protected String getBrowser(HttpServletRequest req) {
	  return "html";
   }

   /**
    * Returns whether the browser supports JavaScript
    */
   protected boolean getJavaScript(HttpServletRequest req) {
	  return true;
   }

   /** Just call doGet, as posting/getting are going to be handled the same way. */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  doGet(req, resp);
   }

   @Override
   public void init(ServletConfig config) throws ServletException {
	  super.init(config);
	  String s = config.getInitParameter("refreshInterval");
	  if (s != null && s.length() > 0) {
		 refreshIntervalInSeconds = Integer.parseInt(s);
		 log.info("Set refresh interval to " + refreshIntervalInSeconds);
	  }
	  mdb = StaticMetaData.getMetaData("xero-view.metadata").get("model");
	  log.info("Creating template group.");
	  String rootDir = cl.getResource("xero").getFile();
	  stg = new StringTemplateGroup("xero", rootDir);
	  stg.setRefreshInterval(refreshIntervalInSeconds);
   }

}
