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
package org.dcm4chee.xero.search.study;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compare two dicom objects by image instance number.
 * @author bwallace
 */
public class DicomObjectComparator implements Comparator<DicomObjectType> {
	private static final Logger log = LoggerFactory.getLogger(DicomObjectComparator.class);
	
	/** Compare two dicom objects by instance number, then by
	 * date/time if they have the same instance number so as to ensure
	 * multi-frames are ordered correctly when split over multiple objects.
	 */
	public int compare(DicomObjectType o1, DicomObjectType o2) {
		Integer i1 = o1.getInstanceNumber();
		Integer i2 = o2.getInstanceNumber();
		if( i1==null || i2==null ) {
			log.error("Instance values are null.");
			return 0;
		}
		int ret = o1.getInstanceNumber() - o2.getInstanceNumber();
		if( ret!=0 ) return ret;
		// This should ONLY happen for multi-frame objects
		// It probably requires a custom read of the DB to read
		// Concatenation Frame Offset Number
		log.warn("Identical instance numbers on different objects in same series - probably means multi frame concatenation or DICOM error.");
		return ret;
	}
}
