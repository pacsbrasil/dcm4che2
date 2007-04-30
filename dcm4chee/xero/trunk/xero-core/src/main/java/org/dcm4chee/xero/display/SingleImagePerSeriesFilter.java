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
package org.dcm4chee.xero.display;

import java.util.Map;
import java.util.logging.Logger;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.MetaData;
import org.jboss.seam.annotations.Name;

/**
 * Modify the query parameters on series searches so that image data for 1 image
 * is returned on every query - this allows a thumbnail to be shown for that
 * single image.
 * @author bwallace
 *
 */
@Name("metadata.seriesFilter.singleImagePerSeriesFilter")
public class SingleImagePerSeriesFilter implements Filter<Object>{
	private static Logger log = Logger.getLogger(SingleImagePerSeriesFilter.class.getName());

	private static final String INSTANCE_NUMBER = "InstanceNumber";

	/** Adds an InstanceNumber search criteria */
	public Object filter(FilterItem filterItem, Map<String, Object> params) {
		if( params.containsKey(INSTANCE_NUMBER) ) {
			log.warning("Search already has instance number, not filtering series.");
			return filterItem.callNextFilter(params);
		}
		// TODO This needs to be changed to ensure all series are correctly
		// retrieved.  It is a useful/fast initial approximation.
		log.warning("Filtering by adding an instance number=1");
		params.put(INSTANCE_NUMBER, "1");
		Object ret = filterItem.callNextFilter(params);
		params.remove(INSTANCE_NUMBER);
		return ret;
	}

	/** Returns the default priority of this filter. */
	@MetaData
	public int getPriority()
	{
		return 15;
	}
}
