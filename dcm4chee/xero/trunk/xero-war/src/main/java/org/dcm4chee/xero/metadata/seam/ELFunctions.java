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

import java.util.Collections;
import java.util.List;

import org.dcm4chee.xero.metadata.MetaDataBean;

/** 
 * This class contains static functions for the expression language to use.
 * @author bwallace
 *
 */
public class ELFunctions {
   public static List<MetaDataBean> sorted(MetaDataBean parent, String having)
   {
	   if( parent==null ) return Collections.EMPTY_LIST;
	   return parent.sorted(having);
   }

   public static List<MetaDataBean> sortedBy(MetaDataBean parent, String key)
   {
	   return parent.sorted(null,key,0);
   }
   
   /** Figure out what type an object is.
    * @param obj to get the type of
    * @return A string name containing the type of obj - useable as a static function.
    */
   public static String typeof(Object obj) {
	   if( obj==null ) return "null";
	   return obj.getClass().getName();
   }
   
   /** 
    * Get the meta-data value, to allow getting methods off the value.
    * @param metaDataBean to get the value object form.
    * @return value object from the meta-data bean (or null if none)
    */
   public static Object value(MetaDataBean mdb) {
	   return mdb.getValue();
   }
}
