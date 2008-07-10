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

import java.net.URL;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.servlet.StringSafeRenderer;
import org.dcm4chee.xero.metadata.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * The template group name can be set as a comma delimited set of names, or from the meta-data itself to allow auto-
 * configuration.  The parent can also be set as a child element, allowing nested definitions with customized setup
 * per-definition.
 * 
 * @author bwallace
 *
 */
public class AutoStringTemplateGroup extends StringTemplateGroup {
	private static final Logger log = LoggerFactory.getLogger(AutoStringTemplateGroup.class);
	
	/** Create a StringTempalteGroup */
	public AutoStringTemplateGroup() {
		super("unknown");
		this.setAttributeRenderers(StringSafeRenderer.RENDERERS);
	}
	
	@MetaData(required=false)
	public void setSuperGroup(StringTemplateGroup superGroup) {
		super.setSuperGroup(superGroup);
	}
	
	/** Sets the group names - if this has only 1 name, then the super group won't be set, otherwise the
	 * super group will also be set.
	 * @param names
	 */
	@MetaData(required=false)
	public void setGroupNames(String names) {
		  ClassLoader cl = Thread.currentThread().getContextClassLoader();
		  String[] parents = names.split(",");
		  StringTemplateGroup parentSTG = null;
		  for(int i=parents.length-1; i>=0; i--) {
			  parents[i] = parents[i].trim();
			  URL url = cl.getResource(parents[i]);
			  if( url==null ) {
				 throw new IllegalArgumentException("No resource named "+parents[i]);
			  }
			  String rootDir = url.getFile();
			  StringTemplateGroup ret;
			  if( i>0 ) ret = new StringTemplateGroup(parents[i], rootDir);
			  else {
				  ret = this;
				  setName(parents[i]);
				  setRootDir(rootDir);
			  }
			  log.info("root resource for {} is {}", parents[i],rootDir);
			  if( rootDir==null || rootDir.indexOf('!')>0) {
				 log.info("Using root resource {}",parents[i]);
				 // Just use the name directly as the root resource.
				 ret.setRootResource(parents[i]);
				 ret.setRootDir(null);
			  }
			  if( parentSTG!=null ) ret.setSuperGroup(parentSTG);
			  parentSTG = ret;
		  }
		  log.info("Name is "+this.getName()+" parents[0]="+parents[0]);
	}
	
	@MetaData(required=false)
	public void setName(String name) {
		super.setName(name);
	}
	
	@MetaData(required=false)
	public void setRefreshInterval(int refreshInterval) {
		super.setRefreshInterval(refreshInterval);
	}
	
	/** This is not declared in the parent class, so add it here */
	@SuppressWarnings("unchecked")
	public Map getAttributeRenderers() {
		return this.attributeRenderers;
	}
	
	/** Sets the attribute renderers object - should be able to do this dynamically, but that isn't so easy because
	 * the types are based on class names.  The better, long term solution is to have it auto-register for a variety of
	 * types, but register by format name.
	 * @param name
	 */
	@MetaData(required=false)
	public void setAttributeRenderersName(String name) {
		if( name.equals("js") ) {
			this.setAttributeRenderers(StringSafeRenderer.JS_RENDERERS);
		}
		else throw new IllegalArgumentException("Unknown renderers name - TODO implement a pluggable mechanism, name:"+name);
	}
}
