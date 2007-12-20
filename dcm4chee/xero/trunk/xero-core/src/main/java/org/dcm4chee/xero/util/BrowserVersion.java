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
package org.dcm4chee.xero.util;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Unwrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("browserVersion")
public class BrowserVersion {
   static Logger log = LoggerFactory.getLogger(BrowserVersion.class);

   @In
   FacesContext facesContext;

   @Unwrap
   public float getBrowser() {
	  String userAgent = (String) facesContext.getExternalContext().getRequestHeaderMap().get("USER-AGENT");
	  System.out.println("User agent=" + userAgent);
	  float version;
	  if ((version = parseVersion(userAgent, "Firefox/")) != 0)
		 return version;
	  if ((version = parseVersion(userAgent, "MSIE ")) != 0)
		 return version;
	  if ((version = parseVersion(userAgent, "Version/")) != 0)
		 return version;
	  if ((version = parseVersion(userAgent, "Opera/")) != 0)
		 return version;
	  log.warn("Unknown browser version for agent " + userAgent);

	  return 1.0f;
   }

   /** Parses the user agent for the version string, starting with versionStart */
   private float parseVersion(String userAgent, String versionStart) {
	  int posn = userAgent.indexOf(versionStart);
	  if (posn < 0)
		 return 0f;
	  int dotPosn = posn + versionStart.length();
	  boolean firstDot = false;
	  while (dotPosn < userAgent.length()) {
		 char ch = userAgent.charAt(dotPosn);
		 if (ch == '.') {
			if (firstDot) {
			   break;
			}
			firstDot = true;
		 } else if (!Character.isDigit(ch)) {
			break;
		 }
		 dotPosn++;
	  }
	  return Float.parseFloat(userAgent.substring(posn + versionStart.length(), dotPosn));
   }
}
