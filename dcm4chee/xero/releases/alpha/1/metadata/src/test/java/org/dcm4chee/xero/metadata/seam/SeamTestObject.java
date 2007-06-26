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

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.jboss.seam.annotations.Name;

@Name("testmeta.SeamTestObject")
public class SeamTestObject implements MetaDataUser {
	public String metaValue, configValue, both;

	/** This value will be set by a components.xml file. */
	public void setConfigValue(String value) { configValue = value; };
	
	/** This value will be set by the meta data value.  It will NOT be injected until
	 * and unless the object is looked up as meta-data.  If the object is looked up in
	 * different locations in the meta-data, it will be incorreclty injected.
	 */
	@MetaData(out="fromMeta")
	public void setMetaValue(String value) { metaValue = value; };
	
	/** This value will be set from both components.xml and meta, and should default
	 * to a final value from components.xml.
	 */
	@MetaData(out="fromMetaButOverride")
	public void setBoth(String value) { both = value; }

	/** Inject the meta-data values. */
	public void setMetaData(MetaDataBean metaDataBean) {
		System.out.println("Called setMetaData...");
		metaDataBean.inject(this);
	};
	
	
}
