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

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Supports dicom retrieval to XML for SR report type objects */
@XmlRootElement
public class ReportBean extends ReportType implements DicomObjectInterface {
   private static final Logger log = LoggerFactory.getLogger(ReportBean.class);

   /** Used to define the DICOM macro tables included in this object */
   @XmlTransient
   MacroItems macroItems;
   
   @XmlTransient
   SeriesBean series;

   /**
    * Create an empty report bean object, typically for JAXB
    * @deprecated
    */
   public ReportBean() {
   };

   /**
    * Create an report bean from the given dicom data
    * 
    * @param data
    *           to use for the DICOM information
    */
   public ReportBean(SeriesBean series, DicomObject data) {
      this.series = series;
      addResult(data);
   }

   /**
    * Initialize the report level attributes by copying the DicomObject's report
    * level data.
    * 
    * @param data
    *           to copy report level data into this from.
    */
   public void addResult(DicomObject data) {
      if (data == null)
         throw new IllegalArgumentException("A valid dicom object must be supplied to initialize the report from.");

      setObjectUID(data.getString(Tag.SOPInstanceUID));
      setInstanceNumber(data.getInt(Tag.InstanceNumber));
      setCompletion(data.getString(Tag.CompletionFlag));
      setVerification(data.getString(Tag.VerificationFlag));
      initConcept(data.get(Tag.ConceptNameCodeSequence));
   }

   /**
    * Initialize the concept code values from the given element.
    * 
    * @param element
    *           containing the sequences to look at. Maybe null.
    */
   protected void initConcept(DicomElement element) {
      if (element == null) {
         log.warn("Concept code sequence is null.");
         return;
      }
      DicomObject item = element.getDicomObject();
      if (item == null) {
         log.debug("Get dicom object in concept code sequence is null.");
         return;
      }
      setConceptMeaning(item.getString(Tag.CodeMeaning));
      setConceptCode(item.getString(Tag.CodeValue));
      log.info("Report has code meaning and value:" + getConceptCode() + "," + getConceptMeaning());
   }

   /** Reports have no current modifications, so return empty all the time. */
   public boolean clearEmpty() {
      return true;
   }

   /** Gets additional attributes and child elements defined in other objects */
   public MacroItems getMacroItems() {
      if (macroItems == null)
         macroItems = new MacroItems();
      return macroItems;
   }
   
   /** Get the attributes from the macro items that are included in this object. */
   @XmlAnyAttribute
   public Map<QName, String> getOtherAttributes() {
      Map<QName, String> ret = null;
      if (macroItems != null)
         ret = macroItems.getAnyAttributes();
      if( log.isDebugEnabled() ) log.debug("Getting other attributes="+ret);
      return ret;
   }
   
   @XmlAnyElement(lax=true)
   public List<Object> getOtherElements() {
      if( this.macroItems==null ) return null;
      return this.macroItems.getOtherElements();
   }

   /** Adds the given macro to this objects set of macro items */
   public void addMacro(Macro m) {
      clearMacro(m.getClass());
      getMacroItems().addMacro(m);
   }
   
   /** Clears the macro from this class, and any children of this class.
    * This uses getMacroItems rather than direct access to the variable in order to work with both
    * frame and non-framed images.  It still needs extra work for the multi-frame single object, however. */
   public void clearMacro(Class<? extends Macro> clazz) {
      Macro item = getMacroItems().findMacro(clazz);
      if (item != null)
         getMacroItems().removeMacro(item);
   }

   public SeriesBean getSeriesBean() {
      return series;
   }

   /** Return the id for this element, in this case the SOP Instance UID */
   public String getId() {
     return ImageBean.key(getObjectUID());
   }
   

}
