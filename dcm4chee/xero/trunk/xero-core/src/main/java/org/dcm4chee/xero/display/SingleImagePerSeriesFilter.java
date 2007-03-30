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

import org.dcm4chee.xero.metadata.filter.FilterBean;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.MetaData;
import org.jboss.seam.annotations.Name;

/**
 * This is a class that is a single image per series filter so that only 1 image gets downloaded
 * on every series lookup rather than searching for all images.
 * @author bwallace
 *
 */
@Name("metadata.seriesFilter.singleImagePerSeriesFilter")
public class SingleImagePerSeriesFilter extends FilterBean<Object>{
	private static Logger log = Logger.getLogger(SingleImagePerSeriesFilter.class.getName());

	private static final String INSTANCE_NUMBER = "InstanceNumber";

	/** Adds an InstanceNumber search criteria */
	@SuppressWarnings("unchecked")
	@Override
	public Object filter(FilterItem filterItem, Map<String, ?> params) {
		if( params.containsKey(INSTANCE_NUMBER) ) {
			log.warning("Search already has instance number, not filtering series.");
			return filterItem.callNextFilter(params);
		}
		// Not strictly safe, but in this case it is easier to manage this than to force a particular type
		// on the params overall.
		log.warning("Filtering by adding an instance number=1");
		((Map) params).put(INSTANCE_NUMBER, "1");
		Object ret = filterItem.callNextFilter(params);
		((Map) params).remove(INSTANCE_NUMBER);
		return ret;
	}

	/** Returns the default priority of this filter. */
	@MetaData
	public int getPriority()
	{
		return 15;
	}
}
