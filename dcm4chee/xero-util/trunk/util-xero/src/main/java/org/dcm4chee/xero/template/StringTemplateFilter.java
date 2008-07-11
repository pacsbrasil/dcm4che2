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
package org.dcm4chee.xero.template;

import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a filter based on StringTemplate.  It just calls the next filter, and uses the stringtemplate
 * named "template" and from the set of stringtemplates "stringtemplategroup".  It is possible to define a static
 * stringetemplategroup to use if none is otherwise defined, as well as a default view if no view is defined, and a default
 * error page view if an exception is thrown.
 * 
 * The model used is the returned map from the next filter - thus, the view always has access to the name of the template,
 * and 
 * @author bwallace
 *
 */
public class StringTemplateFilter implements Filter<ServletResponseItem> {
	private static Logger log = LoggerFactory.getLogger(StringTemplateFilter.class);

	/** The filter that returns the model object */
	public static final String MODEL_FILTER = "model";
	public static final String TEMPLATE_NAME = "template";
	public static final String TEMPLATE_GROUP = "templateGroup";
	public static final String CONTENT_TYPE_NAME="contentType";
	/** The default view is the one to show if no other view is defined */
	String defaultView;
	
	/** The default content type (xhtml unless otherwise specified) */
	String defaultContentType;
	
	/** The error view is to be shown when an exception is thrown */
	String errorView;
	
	/** The default group is the string template set to use if no other one is specified. */
	StringTemplateGroup defaultGroup;
	
	/** This method will return an object that renders the specified view */
	@SuppressWarnings("unchecked")
	public ServletResponseItem filter(
			FilterItem<ServletResponseItem> filterItem,
			Map<String, Object> params) {
		try {
			Map<String,Object> model = (Map<String,Object>) filterItem.callNamedFilter(MODEL_FILTER, params);
			if( model==null ) throw new NullPointerException("Model pointer is null for filter named "+MODEL_FILTER);
			String view = (String) model.get(TEMPLATE_NAME);
			StringTemplateGroup stg = (StringTemplateGroup) model.get(TEMPLATE_GROUP);
			String contentType = (String) model.get(CONTENT_TYPE_NAME);
			if( contentType==null ) contentType = defaultContentType;
			if( stg==null ) stg = defaultGroup;
			if( view==null ) view = defaultView;
			if( view==null ) view = templateFromParams(params); 
			if( view==null ) {
				log.warn("View {} not found - returning 404", view);
				return new ErrorResponseItem(404,"View name not defined.");				
			}
			if( stg==null ) {
				log.warn("String template group {} not found - returning 404", TEMPLATE_GROUP);
				return new ErrorResponseItem(404,"String template group not defined.");
			}
			log.info("Templating view {} from {}", view, stg.getName());
			StringTemplate st = stg.getInstanceOf(view,model);
			StringTemplateResponseItem stri = new StringTemplateResponseItem(st,stg,errorView);
			if( contentType!=null ) stri.setContentType(contentType);
			return stri;
		}
		catch(Exception e){
			log.error("Caugt exception "+e,e);
			return new ErrorResponseItem(404,"Internal server error:"+e);
		}
	}
	
	/** Generates the template name based on the URI used to call the object */
	public String templateFromParams(Map<String,Object> params) {
		String uri = (String) params.get(MetaDataServlet.REQUEST_URI);
		log.info("Template name is based on {}", uri);
		if( uri==null ) return null;
		int pos = uri.lastIndexOf('.');
		if( pos>0 ) uri = uri.substring(0,pos);
		pos = uri.indexOf('/',1);
		if( pos<0 ) return null;
		uri = uri.substring(pos+1);
		uri = uri.replace('/','.');
		log.info("Final template name is {}", uri);
		return uri;
	}

	public String getDefaultView() {
		return defaultView;
	}

	@MetaData(required=false)
	public void setDefaultView(String defaultView) {
		this.defaultView = defaultView;
	}

	public String getDefaultContentType() {
		return defaultContentType;
	}

	@MetaData(required=false)
	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
	}

	public String getErrorView() {
		return errorView;
	}

	@MetaData(required=false)
	public void setErrorView(String errorView) {
		this.errorView = errorView;
	}

	public StringTemplateGroup getDefaultGroup() {
		return defaultGroup;
	}

	@MetaData(required=false)
	public void setDefaultGroup(StringTemplateGroup defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public void setDefaultGroupName(String defaultGroupName) {
		
	}
}
