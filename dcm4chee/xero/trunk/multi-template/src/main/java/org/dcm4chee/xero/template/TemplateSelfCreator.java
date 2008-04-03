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
package org.dcm4chee.xero.template;

import org.xml.sax.Attributes;

/**
 * Allow a template to create others of the same type - this is often convenient to combine the creation
 * and templating itself when the creation is fairly simple.  For complex creation, having these separated
 * can be better.
 * 
 * @author bwallace
 *
 */
public abstract class TemplateSelfCreator<T extends Template> extends Template implements TemplateCreator<T> {

   /**
    * Creates an instance of the same class that this object is - allows a template to be self creating
    * which is convenient to allow the definition to be included along with the template itself.
    */
   @SuppressWarnings("unchecked")
   public T createInitialTemplate(String uri, String localName, String qName, Attributes atts) {
	  try {
		 return (T) getClass().newInstance();
		 // Convert the exceptions to runtime exceptions, as this is really a programming bug, not 
		 // something that should be seen in practice.
	  } catch (InstantiationException e) {
		 throw new RuntimeException(e);
	  } catch (IllegalAccessException e) {
		 throw new RuntimeException(e);
	  }
   }

   /** Store the child element by default */
   public void setChild(T created, Template child) {
	  created.child = child;
   }

}
