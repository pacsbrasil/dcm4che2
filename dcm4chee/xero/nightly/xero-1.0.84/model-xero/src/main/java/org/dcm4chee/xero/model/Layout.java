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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.xero.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dcm4chee.xero.metadata.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A layout object is a block of structure containing information about the size/shape/model
 * that is used to display objects.
 * Do NOT attempt to "put" any element that has a setter - the put will not have any affect
 * on those elements.
 * To sub-class this, over-ride the get method with any local field getters and then 
 * call the super.get method.
 *
 * @author bwallace
 */
@SuppressWarnings("serial")
public class Layout extends HashMap<String,Object> {
   private static final Logger log = LoggerFactory.getLogger(Layout.class);
   protected String template;
   
   protected List<Layout> layouts;
   
   protected boolean used = true;
   
   public Layout() {
	  template = "html/layout";
   }
   
   public Layout(String template) {
	  if( template==null ) template = "html/layout";
	  this.template = template;
   }
   
   /**
    * Handle the values returned directly by this object, and then the
    * regular hashmap values.  A simple comparison is more efficient than going through
    * reflection for up to around 10-20 values, so this is better than using reflection
    * while still providing complete support for arbitrary sub-elements.  
    */
   @Override
   public Object get(Object key) {
	  log.info("Getting key "+key);
	  if( "template".equals(key) ) return getTemplate();
	  if( "layouts".equals(key) ) return getLayouts();
	  if( "used".equals(key) ) return used;
	  return super.get(key);
   }
   
   /** Include the template/layouts keys 
    */
   public boolean containsKey(Object key) {
	  if( "template".equals(key) ) return true;
	  if( "layouts".equals(key) ) return true;
	  if( "used".equals(key) ) return true;
	  return super.containsKey(key);
   }

   @MetaData
   public void setTemplate(String template) {
	  this.template = template;
   }
   
   /** Get the template to render this object with. */
   public String getTemplate() {
	  return template;
   }
   
   /** Returns the array of layouts - maybe null if none have been added. */
   public List<Layout> getLayouts() {
	  return layouts;
   }
   
   /** Add a new layout to the child layouts */
   public void add(Layout lay) {
	  if( layouts==null ) layouts = new ArrayList<Layout>(2);
	  layouts.add(lay);
   }
   
   /** Indicate the layout object for this layout */
   @Override
   public String toString() {
	  return "Layout("+template+")";  
   }
}
