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

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataProvider;
import org.dcm4chee.xero.metadata.ValueProvider;
import org.jboss.seam.core.Expressions;

/** This class provides seam-based meta-data, from EL values of the form ${...}
 * It uses the Expressions ValueBinding to pre-compute the binding, and then re-uses
 * that every time the object is looked up.
 * @author bwallace
 */
public class SeamELValueProvider implements ValueProvider
{

	/** Convert a value of type ValueBinding into an actual value to use */
	public Object convertValue(MetaDataBean mdb, Object sourceValue) {
		return ((Expressions.ValueBinding) sourceValue).getValue();
	}

	/** Convert the string to a ValueBinding.
	 */
	public Object preConvertValue(MetaDataBean mdb, Object sourceValue) {
		if( !(sourceValue instanceof String) ) return null;
		String sourceValueStr = ((String) sourceValue).trim();
		if( !sourceValueStr.startsWith("${") ) return null;
		if( !sourceValueStr.endsWith("}") ) return null;
		return Expressions.instance().createValueBinding(sourceValueStr);
	}

	public MetaDataProvider getExtraMetaDataProvider(MetaDataBean mdb, Object convertedValue, Object originalValue) {
		return null;
	}

}
