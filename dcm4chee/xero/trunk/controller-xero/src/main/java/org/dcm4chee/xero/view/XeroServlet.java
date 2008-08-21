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

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.servlet.SessionMap;
import org.antlr.stringtemplate.servlet.StringSafeRenderer;
import org.antlr.stringtemplate.servlet.StringTemplateServlet;
import org.dcm4chee.xero.controller.Action;
import org.dcm4chee.xero.controller.RequestValidator;
import org.dcm4chee.xero.controller.XmlModelFactory;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.access.MapWithDefaults;
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
public class XeroServlet extends StringTemplateServlet {
   static final Logger log = LoggerFactory.getLogger(XeroServlet.class);

   /** The name of the controller to use */
   String controllerName="controller";
   
   /** The name of the model to use */
   String modelName = "model";
   
   /** The meta-data name to lookup */
   String metadataName = "xero.metadata";
   
   Action controller;
   
   StringTemplateGroup stgIE;
   
   /** This is the information about the model, including "defaults" - not necessarily
    * the actual model used for each event.
    */
   MetaDataBean model;
   
   
   /**
    * Create the model to use for rendering pages.
    * Initialize the event map with the parameters, URI Resolver and session, and then
    * call the controller with the provided action (if any).
    */
   @Override
   public Map<String,Object> createModel(HttpServletRequest req, HttpServletResponse resp) {
	  MapWithDefaults mwd = new MapWithDefaults(model);
	  mwd.put(RequestValidator.PARAMETERS,req.getParameterMap());
	  mwd.put(XmlModelFactory.URIRESOLVER, new UrlUriResolver(req,resp,getServletContext()));
	  mwd.put("_session", new SessionMap(req.getSession()));
	  String userAgent = req.getHeader("USER-AGENT").toLowerCase();
	  mwd.put("IS_IE", (userAgent.indexOf("msie")>=0) && (userAgent.indexOf("opera")==-1) );
	  String userName = req.getRemoteUser();
	  mwd.put("userName", userName);
	  
	  log.info("Creating model for user {}",userName);
	  return controller.action(mwd);
   }
   
   
   /** Chooses the group for IE if the user agent includes MSIE, otherwise use the regular,
    * SVG style group.
    */
   @Override
   protected StringTemplateGroup getStringTemplateGroup(HttpServletRequest req) {
	  	if( req.getHeader("USER-AGENT").indexOf("MSIE")>=0 ) {
	  	   return stgIE;
	  	}
	  	return stg;
   }


   /** Initialize the data and ie specific stg. */
   @Override
   public void init(ServletConfig config) throws ServletException {
	  super.init(config);
	  String test = config.getInitParameter("metaData");
	  if( test!=null ) metadataName = test;
	  MetaDataBean root = StaticMetaData.getMetaData(metadataName);
	  if( root==null ) throw new ServletException("Unable to find metadata '"+metadataName+"'");
	  
	  model = root.getChild(modelName);
	  if( model==null ) throw new ServletException("Unable to find model "+modelName+" in metadata "+metadataName);
	  controller = (Action) root.getValue(controllerName);
	  if( controller==null ) throw new ServletException("Unable to find controller "+controllerName+" in metadata "+metadataName);
	  stgIE = createStringTemplateGroup("ie");
	  stgIE.setSuperGroup(stg);
	  stgIE.setAttributeRenderers(StringSafeRenderer.RENDERERS);
   }
}
