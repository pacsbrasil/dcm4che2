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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.svg.P;

/** Provides information about a key object type object. */
public class KeyObjectBean extends KeyObjectType implements DicomObjectInterface, SearchableDicomObject {
   static Logger log = LoggerFactory.getLogger(KeyObjectBean.class);

   /**
     * This is serialized sometimes, but not always so keep it as transient, and
     * then serialize at need. Maps the object UID to a list of key selections
     * for that UID.
     */
   @XmlTransient
   List<KeySelection> keySelection = new ArrayList<KeySelection>();
   
   @XmlTransient
   SeriesBean parent;
   
   @XmlTransient
   MacroItems macroItems;
   
   /** Contains both the date and time for the content date/time */
   @XmlTransient 
   Date contentDate;
   
   /** Constructor for JAXB
    * @deprecated
    */
   public KeyObjectBean() {
      
   }
   
   /** Initialize the key object bean */
   public KeyObjectBean(SeriesBean series, DicomObject dobj) {
	  this.parent = series;
	  addResult(dobj);
   }

   /** Create an empty key object bean */
   public KeyObjectBean(SeriesBean series) {
	  this.parent = series;
   }

   /**
    * Initialize the attributes of this series from the dicom header data
    * @param data
    */
   public void addResult(DicomObject data) {
	  setObjectUID(data.getString(Tag.SOPInstanceUID));
	  setInstanceNumber(data.getInt(Tag.InstanceNumber));
	  setCompletion(data.getString(Tag.CompletionFlag));
	  setVerification(data.getString(Tag.VerificationFlag));
	  setContentDate(data.getDate(Tag.ContentDate,Tag.ContentTime));
	  initConcept(data.get(Tag.ConceptNameCodeSequence));
	  initKeySelection(data);
   }


   /**
    * Initialize the key object reference text values
    * @param data
    */
   private void initKeyValues(DicomObject data) {
      DicomElement tContentSeq = data.get(Tag.ContentSequence);
      if (tContentSeq == null || !tContentSeq.hasItems()) {
         return;
      }
      int size = tContentSeq.countItems();
      
      for (int i = 0; i < size; i++) {
         DicomObject conDcm = tContentSeq.getDicomObject(i);
         String tValueType = conDcm.getString(Tag.ValueType);
         if (tValueType.equals("TEXT"))  {
            P para = new P();
            para.setContent(conDcm.getString(Tag.TextValue));
            if(p == null)    {
               p = new ArrayList<P>();
            }
            p.add(para);
         }
      }
   }
   /** Initialize the key seleciton from the given dicom object */
   public void initKeySelection(DicomObject data) {
      if (objectRef == null)    {
         objectRef = new ArrayList<ObjectRef>();   
      }
      initKeyValues(data);   
	  initKeySelection(data.get(Tag.ContentSequence));
   }
   
   /** Get the series parent for this object */
   public SeriesBean getSeriesBean() {
	  return parent;
   }

   /** Initializes the key selection from a sequence of selected images */
   private void initKeySelection(DicomElement sq) {
	  if (sq == null || !sq.hasItems()) {
		 log.debug("Didn't find any selected images.");
		 return;
	  }
	  log.info("Found a selected image.");
	  int size = sq.countItems();
	  for (int i = 0; i < size; i++) {
		 DicomObject conDcm = sq.getDicomObject(i);
		 DicomElement imgs = conDcm.get(Tag.ReferencedSOPSequence);
		 initKeySelectionFromImages(imgs);
	  }
   }

   private void initKeySelectionFromImages(DicomElement imgs) {
	  if (imgs == null || !imgs.hasItems()) {
		 log.debug("No images referenced from key objects.");
		 return;
	  }
	  int size = imgs.countItems();
	  for (int i = 0; i < size; i++) {
		 DicomObject dcmObj = imgs.getDicomObject(i);
		 String uid = dcmObj.getString(Tag.ReferencedSOPInstanceUID);
		 log.info("Adding referenced image " + uid);
		 int[] frames = dcmObj.getInts(Tag.ReferencedFrameNumber);
		 String gspsUid = null;
		 DicomObject gspsObj = dcmObj.getNestedDicomObject(Tag.ReferencedSOPSequence);
		 if (gspsObj != null) {
			gspsUid = gspsObj.getString(Tag.ReferencedSOPInstanceUID);
			log.info("Key object with GSPS "+gspsUid+" applied to image "+uid+" found ");
		 }
		 if (frames == null || frames.length == 0) {
			KeySelection keySel = new KeySelection(uid, gspsUid, 0);
	        addToObjectRef(uid, gspsUid, null);
			keySelection.add(keySel);
		 } else {
			for (int f = 0; f < frames.length; f++) {
			   KeySelection keySel = new KeySelection(uid, gspsUid, frames[f]);
			   keySelection.add(keySel);
	           addToObjectRef(uid, gspsUid, frames[f]);
			}
		 }
	  }
   }

   /**
    * Creates <tt>ObjectRef</tt> and adds to the objectRef list of not present
    * @param uid
    * @param gspsUid
    * @param frame
    */
   private void addToObjectRef(String uid, String gspsUid, Integer frame) {
      ObjectRef objRef = new ObjectRef();
       objRef.setObjectUID(uid);
       if (gspsUid != null)  {
          objRef.setGspsUID(gspsUid);
       }
       if (frame != null)   {
          objRef.setFrame(frame);
       }
      Iterator<ObjectRef> iter = objectRef.iterator();
      boolean contains = false;
      while (iter.hasNext())    {
         ObjectRef ref = iter.next();
         if (ref.getObjectUID().equals(uid))    {
            contains = true;
            break;
         }
      }
      if (!contains)    {
         objectRef.add(objRef);
      }
   }

   /**
     * Initialize the concept code values from the given element.
     * 
     * @param element
     *            containing the sequences to look at. Maybe null.
     */
   protected void initConcept(DicomElement element) {
	  if (element == null) {
		 log.debug("Concept code sequence is null.");
		 return;
	  }
	  DicomObject item = element.getDicomObject();
	  if (item == null) {
		 log.debug("Get dicom object in concept code sequence is null.");
		 return;
	  }
	  setConceptMeaning(item.getString(Tag.CodeMeaning));
	  setConceptCode(item.getString(Tag.CodeValue));
	  log.debug("Report has code meaning and value:" + getConceptCode() + "," + getConceptMeaning());
   }

   /**
     * KeyObjectBean's are always considered empty - they don't have any
     * customizations yet.
     */
   public boolean clearEmpty() {
	  return true;
   }

   /** The SOP Instance is the ID for a KO object */
   public String getId() {
	  return getObjectUID();
   }

   public List<KeySelection> getKeySelection() {
	  return keySelection;
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

   /** Add other elements from the macro items */
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

   /** Returns the date the content was created */
   public Date getContentDate() {
      return contentDate;
   }

   public void setContentDate(Date contentDate) {
      this.contentDate = contentDate;
   }

   /** The content name is the concept meaning for key objects */
   public String getContentName() {
	  return conceptMeaning;
   }


}
