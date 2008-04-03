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
 * Creates a template.
 *  
 * @author bwallace
 */
public interface TemplateCreator<T extends Template> {
   
   /** Creates the initial template instance - this can be a place holder
    * if the final type isn't known.  The return value will be passed to all of the set/initialize methods.
    * @return a new template to put into the object.
    */
   public T createInitialTemplate(String uri, String localName, String qName, Attributes atts);

   /** Sets a template child element - this will usually be either a TextTemplate or a ListTemplate
    * if there is 1 child or multiple children.  This child is owned by this template - it can be
    * merged multiple times, with the same or different contexts, parts removed from the list and
    * stored for later use or ignored entirely.  The setChild happens before initialize is called.
    * Expect a null if there are no children of this tag.
    * @param child
    */
   public void setChild(T created, Template child);
   
}
