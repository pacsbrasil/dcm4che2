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
package org.dcm4chee.xero.search.macro;

import java.util.Map;

import javax.xml.namespace.QName;

import org.dcm4chee.xero.search.study.Macro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indicates that the given object is a key object, and to display it with a
 * key. The default if a key object instance isn't present is false, but
 * sometimes those are overridden by an explicit value during edits, so
 * sometimes a KeyObject will be used with a false value.
 * Attribute: koUID=true/false (false is default so usually not present)
 * 
 * @author bwallace
 * 
 */
public class KeyObjectMacro implements Macro {
   private static Logger log = LoggerFactory.getLogger(KeyObjectMacro.class);
   public static final QName Q_KEY = new QName(null, "koUID");

   private String keyObject;

   public KeyObjectMacro(String keyObject) {
	  this.keyObject = keyObject;
   }

   public int updateAny(Map<QName, String> attrs) {
	  attrs.put(Q_KEY, keyObject);
	  log.debug("Added koUID attribute "+keyObject);
	  return 1;
   }

   /**
     * Indicates if the given image is a key object
     */
   public String getKeyObject() {
	  return keyObject;
   }
}
