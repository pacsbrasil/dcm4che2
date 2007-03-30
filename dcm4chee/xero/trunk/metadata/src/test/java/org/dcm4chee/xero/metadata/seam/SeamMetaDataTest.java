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

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

/**
 * This class tests the seam meta-data provider, for the following:
 * <ul>
 * <li>Lookup of Seam components as meta-data values.</li>
 * <li>Reading of the Seam configuration elements as meta-data vluaes for the
 * above seam components.</li>
 * <li>Reading of MetaData annotation elements on Seam components in addition
 * to the configuration elements.</li>
 * </ul>
 * Note that seam components CANNOT be used in more than 1 location if they use
 * meta-data injection, and values cannot be over-ridden in the meta-data
 * properties files.
 * 
 * @author bwallace
 * 
 */
public class SeamMetaDataTest extends SeamTest {
	static Map<String, Object> prop = new HashMap<String, Object>();
	static {
		prop.put("metaDataProvider.seam",
				"${org.dcm4chee.xero.metadata.seam.SeamMetaDataProvider}");
		prop.put("metaDataProvider.seam.priority", "5");
		prop.put("metaDataProvider.seam.prefixPath", "testmeta");
		prop.put("valueProvider.seam",
				"${org.dcm4chee.xero.metadata.seam.SeamELValueProvider}");
		prop.put("testel", "${testmeta.SeamTestObject}");
	}

	/** The meta-data instance is initialized the first time it is used, in order
	 * to ensure it is contained inside a context.
	 */
	static MetaDataBean mdb;

	/**
	 * Test that the seam meta-data provider can lookup items relative to the
	 * seam path.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSeamComponentLookup() throws Exception {
		new FacesRequest() {
			@Override
			protected void invokeApplication() throws Exception {
				if( mdb==null ) mdb = new MetaDataBean(prop);
				Object obj = mdb.getValue("SeamTestObject");
				assert obj != null;
				assert obj instanceof SeamTestObject;
			}
		}.run();
	}

	/**
	 * Test that Seam property configuration values can be looked up.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSeamConfigurationMetaData() throws Exception {
		new FacesRequest() {
			@Override
			protected void invokeApplication() throws Exception {
				if( mdb==null ) mdb = new MetaDataBean(prop);
				MetaDataBean child = mdb.get("SeamTestObject");
				assert child != null;
				assert child.getValue("configValue") != null;
				assert "fromConfiguration"
						.equals(child.getValue("configValue"));
				assert child.getValue("metaValue") != null;
				assert "fromMeta".equals(child.getValue("metaValue"));
				assert child.getValue("both") != null;
				assert "fromSeamConfigOverride".equals(child.getValue("both"));
			}
		}.run();
	}

	/**
	 * Test that seam EL values can be looked up.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSeamELValueLookup() throws Exception {
		new FacesRequest() {
			@Override
			protected void invokeApplication() throws Exception {
				if( mdb==null ) mdb = new MetaDataBean(prop);
				Object obj = mdb.getValue("testel");
				assert obj != null;
				assert obj instanceof SeamTestObject;
			}
		}.run();
	}
}
