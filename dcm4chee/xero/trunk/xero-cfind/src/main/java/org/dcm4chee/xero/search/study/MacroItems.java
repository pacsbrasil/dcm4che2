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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

public class MacroItems {
   private List<Macro> macros;

   /** Returns a map containing all the attributes provided by the macros. */
   public Map<QName,String> getAnyAttributes() {
	  if( macros==null || macros.size()==0) return null;
	  Map<QName, String> attrs = new HashMap<QName,String>();
	  int count = 0;
	  for(Macro m : macros) {
		 count += m.updateAny(attrs,null);
	  }
	  if( count==0 ) return null;
	  return attrs;
   }
   
   /** Adds a macro to this set of macro items */
   public void addMacro(Macro macro) {
	  getMacros().add(macro);
   }
   
   /** Removes the given macro */
   public void removeMacro(Macro macro) {
	  if( macros==null ) return;
	  macros.remove(macro);
   }
   
   /** Finds the macro by class */
   public Macro findMacro(Class clazz) {
	  if( macros==null ) return null;
	  for(Macro m : macros) {
		 if( clazz.isInstance(m) ) return m;
	  }
	  return null;
   }

   /** Clears any non-information providing children (none in this case), and return true
    * if the object is empty after the clear.  For macro items, returns true only if no
    * children exist.
    * @return boolean true if this object provides no attributes or children.
    */
   public boolean clearEmpty() {
	  if( macros==null || macros.size()==0 ) {
		 macros = null;
		 return true;
	  }
	  return false;
   }

   /** Get the underlying list of macros - this is a live list that modifies the internal representaiton */
   public List<Macro> getMacros() {
	  if( macros==null ) macros = new ArrayList<Macro>(1);
	  return macros;
   }
}
