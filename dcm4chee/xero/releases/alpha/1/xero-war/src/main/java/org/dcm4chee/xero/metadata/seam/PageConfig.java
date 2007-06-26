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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.metadata.seam;

import java.util.AbstractMap;
import java.util.Set;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

/**
 * Page configuration is the lookup into the right meta-data information for 
 * the given page.  The right one means the right one by name, but also taking
 * into account other factors such as which browser is in use, whether Javascript
 * is enabled etc.  
 * The general usage is to setup a page variable, like this:
 * &lt;x:documents  pageConfig="#{PageConfig.tabsFrame}" &gt;
 * This would be the configuration for both the tabs frame and the tabs frame XSL pages,
 * and might be a configuration for another page (but does not have to be.)
 * 
 * Some of the page configuration is common between configurations, while others
 * are determined dynamically. 
 * @author bwallace
 *
 */
@Name("PageConfig")
public class PageConfig extends AbstractMap<String,MetaDataBean> 
{

	Boolean javaScriptEnabled = Boolean.TRUE;
	Boolean svgEnabled = Boolean.TRUE;
	Boolean framesEnabled = Boolean.TRUE;
	
	/** This is the overall configuration name chosen - there might be a set of these
	 * that are looked at in order.
	 */
	String configName = "";
	
	@In(create=true)
	MetaDataBean metadata;
	
	/** Figure out what metadata node to use to render the given page. */
	@Override
	public MetaDataBean get(Object forPage) {
		return metadata.get(forPage);
	}
	
	public String test() {
		return "test result";
	}
	
	/** Indicate if SVG is enabled in the browser */
	public Boolean getSvgEnabled() {
		return svgEnabled;
	}
	
	/** Indicate if Javascript is enabled in the browser */
	public Boolean getJavaScriptEnabled() {
		return javaScriptEnabled;
	}
	
	/** Indicate if frames are enabled in the browser */
	public Boolean getFramesEnabled() {
		return framesEnabled;
	}

	@Override
	public Set<java.util.Map.Entry<String, MetaDataBean>> entrySet() {
		return metadata.entrySet();
	}
}
