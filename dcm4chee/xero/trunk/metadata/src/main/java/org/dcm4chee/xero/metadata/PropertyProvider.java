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
package org.dcm4chee.xero.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class provides meta-data from a given string-string map.
 * 
 * @author bwallace
 * 
 */
public class PropertyProvider implements MetaDataProvider, MetaDataUser {
	Map<String, ?> properties;

	/**
	 * This constructor is used outside of the MetaDataBean to create the
	 * initial property provider values.
	 * 
	 * @param properties
	 */
	public PropertyProvider(Map<String, ?> properties) {
		this.properties = new HashMap<String, Object>(properties);
	}

	/** Returns the meta-data information available from the properties file */
	public Map<String, ?> getMetaData(String path, MetaDataBean existingMetaData) {
		// This is more than is needed, as we really only need properties
		// starting with
		// path, but it doesn't matter that much as the meta-data gets parsed
		// just
		// once for each level.
		return properties;
	}

	/** Read all the listed property files and combine them. */
	public void setMetaData(MetaDataBean metaDataBean) {
		List<MetaDataBean> children = metaDataBean.sorted();
		Map<String, String> combined = new HashMap<String, String>();
		properties = combined;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		// Reverse in-place so as to add least-relevent items first, and
		// override them later.
		Collections.reverse(children);
		for (MetaDataBean child : children) {
			try {
				InputStream is = cl.getResourceAsStream((String) child
						.getValue());
				Properties props = new Properties();
				props.load(is);
				for (Map.Entry me : props.entrySet()) {
					combined.put((String) me.getKey(), (String) me.getValue());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
