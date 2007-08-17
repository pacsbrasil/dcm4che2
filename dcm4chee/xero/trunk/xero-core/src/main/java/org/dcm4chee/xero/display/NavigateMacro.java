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

import javax.xml.namespace.QName;

import org.dcm4chee.xero.search.study.Macro;

/** The navigate macro allows a patient/study/series to have a defined "start" position for the next level.
 * This is a simple navigation scheme that allows navigation at one level at a time.  More complex navigation
 * schemes use other mechanisms to define positions.
 * @author bwallace
 *
 */
public class NavigateMacro implements Macro {
   public static QName Q_VIEW_START = new QName("viewStart");
   String viewStart;
   
   /** Setup an object to navigate to the given start position. */
   public NavigateMacro(String viewStart) {
	  if( viewStart==null || viewStart.isEmpty()) throw new IllegalArgumentException("Navigate must be provided with a start position.");
	  this.viewStart = viewStart;
   }

   /** Add to the array of extra attributes */
   public int updateAny(Map<QName, String> attrs) {
	  attrs.put(Q_VIEW_START,viewStart);
	  return 1;
   }

   public String toString() {
	  return "nav("+viewStart+")";
   }
}
