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

/** 
 * Compares two series - used to order the series.  Designed to order
 * the series by increasing series number, putting GSPS, reports etc after everything else.
 * @author bwallace
 */
public class SeriesComparator implements Comparator<SeriesType> {
   /** Indicate if the given series a non-image containing */
   public static boolean isNonImageSeries(SeriesType ser) {
	  String mod = ser.getModality();
	  if( "PR".equals(mod) || "KO".equals(mod) || "SR".equals(mod) ) return true;
	  return false;
   }

	/**
	 * Compare two series by series number.
	 */
	public int compare(SeriesType ser1, SeriesType ser2) {
	   boolean ser1NonImage = isNonImageSeries(ser1);
	   boolean ser2NonImage = isNonImageSeries(ser2);
	    if( ser1NonImage ) {
	       if( !ser2NonImage ) {
	    	  return 2;
	       }
	    }
	    else if( ser2NonImage ) {
	       return -2;
	    }
		Integer sn1 = ser1.getSeriesNumber();
		Integer sn2 = ser2.getSeriesNumber();
		// Put series without series numbers at the end.
		if( sn1==null && sn2!=null ) return 1;
		if( sn2==null && sn1!=null ) return -1;
		if( sn1==null && sn2==null ) return 0;
		// Use reverse order for non image related series.
		int ret = sn1-sn2;
		if( ret==0 ) {
		   // TODO - when we have real SR report creators with multiple instances at
		   // various statii, use a better comparator.
		   ret = ser1.getSeriesUID().compareTo(ser2.getSeriesUID());
		}
		if( ser1NonImage && ser2NonImage ) return -ret;
		return ret;
	}
}
