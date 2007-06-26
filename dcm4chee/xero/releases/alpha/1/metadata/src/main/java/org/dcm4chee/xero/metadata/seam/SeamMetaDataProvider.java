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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.dcm4chee.xero.metadata.Injector;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataProvider;
import org.dcm4chee.xero.metadata.MetaDataUser;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

/**
 * This class provides meta-data from Seam components registered at the 
 * application level.  Different prefixes can be used by configuring multiple instances
 * of this object, each with their own Seam component prefix search path.
 * @author bwallace
 *
 */
public class SeamMetaDataProvider implements MetaDataProvider, MetaDataUser
{
	private static Logger log = Logger.getLogger(SeamMetaDataProvider.class.getName());
	
	private String prefixPath = "metadata";
	private Map<String,Object> addedMetaData = null;
	
	@MetaData
	public void setPrefixPath(String value) {
		this.prefixPath = value;
		if( prefixPath.length()>0 && !prefixPath.endsWith(".")) prefixPath += '.';
		log.fine("Prefix Path for Seam meta-data provider is "+prefixPath);
	}

	public Map<String, ?> getMetaData(String path, MetaDataBean existingMetaData) {
		if( addedMetaData!=null ) return addedMetaData;
		Context context = Contexts.getApplicationContext();
		if( context==null ) {
			log.warning("The context isn't available for meta-data path "+path);
			return null;
		}
		addedMetaData = new HashMap<String,Object>();
		addMetaDataFromComponents(context);
		addMetaDataFromSeamProperties(context);
		addMetaDataFromAnnotations(context);
		return addedMetaData;
	}

	/** This adds meta-data whose value is a component lookup, according to the 
	 * provided name.
	 * @param context
	 */
	protected void addMetaDataFromComponents(Context context) {
		String[] names = context.getNames();
		for(String name : names) {
			if( ! name.endsWith(".component")) continue;
			name = name.substring(0,name.length()-10);
			if( name.startsWith(prefixPath) ) {
				String componentName = name.substring(prefixPath.length());
				if( componentName.length()==0 ) continue;
				String value = "${"+name+"}";
				log.fine("Adding seam meta-data "+componentName+"="+value);
				addedMetaData.put(componentName, value);
			}
		}
	}

	/** This method adds meta-data tags to name from meta-data tags in the source */
	protected void addMetaDataFromAnnotations(Context context) {
		String[] names = context.getNames();
		for(String name : names) {
			if( ! name.endsWith(".component")) continue;
			String nameNoSuffix = name.substring(0,name.length()-10);
			if( nameNoSuffix.startsWith(prefixPath) ) {
				String componentName = nameNoSuffix.substring(prefixPath.length());
				if( componentName.length()==0 ) continue;
				Component comp = (Component) context.get(name);
				log.finer("Should add annotation meta-data from "+componentName+" which is of type "+comp.getBeanClass());
				Class clazz = comp.getBeanClass();
				Injector injector = Injector.getInjector(clazz);
				Map<String, ?> annotationMetaData = injector.getMetaData(null, null);
				if( annotationMetaData==null ) continue;
				for(Map.Entry<String,?> me : annotationMetaData.entrySet()) {
					String metaName = componentName + "." + me.getKey();
					if( !addedMetaData.containsKey(metaName) ) {
						log.fine("Adding meta-data "+metaName+"="+me.getValue());
						addedMetaData.put(metaName,me.getValue());
					}
				}
				
			}
		}
	}

	/**
	 * This method adds meta-data as defined in the org.jboss.seam.properties instance.
	 * @param name
	 */
	protected void addMetaDataFromSeamProperties(Context context) {
		Map<String,?> properties = (Map<String,?>) context.get("org.jboss.seam.properties");
		for(Map.Entry<String,?> me : properties.entrySet()) {
			String name = me.getKey();
			if( !name.startsWith(prefixPath)) continue;
			String metaName = name.substring(prefixPath.length());
			if( !addedMetaData.containsKey(metaName) ) {
			  log.fine("Adding meta-data "+metaName+"="+me.getValue() + " of type "+me.getValue().getClass());
			  addedMetaData.put(metaName,me.getValue().toString());
			}
		}
	}

	/** Sets the meta data to use for this object.  */
	public void setMetaData(MetaDataBean metaData) {
		log.finest("Called set-meta data on SeamMetaDataProvider");
		metaData.inject(this);
	}
}
