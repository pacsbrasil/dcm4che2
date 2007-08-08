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

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.LocalModel;

@XmlRootElement
public class ImageBean extends ImageType implements Image, LocalModel<String> {

	@XmlTransient
	protected Map<Object,Object> children;

	/**
	 * Create an empty image bean object.
	 */
	public ImageBean() {
	};

	/**
	 * Create an image bean from the given dicom data
	 * 
	 * @param data
	 *            to use for the DICOM information
	 */
	public ImageBean(DicomObject data) {
		initAttributes(data);
	}

	/**
	 * Initialize the image level attributes by copying the DicomObject's image
	 * level data for Columns, Rows, SOP Instance UID and Instance Number.
	 * 
	 * @param data
	 *            to copy image level data into this from.
	 */
	protected void initAttributes(DicomObject data) {
		setColumns(data.getInt(Tag.Columns));
		setRows(data.getInt(Tag.Rows));
		// setSOPClassUID(data.getString(Tag.SOPClassUID));
		setSOPInstanceUID(data.getString(Tag.SOPInstanceUID));
		setInstanceNumber(data.getInt(Tag.InstanceNumber));
	}

	/** Indicate if there are any interesting children, clearing any empty ones first.
	 * Note that size is used to drive emptiness for all size related attributes,
	 * as is WindowCenter for all window level related attributes.
	 */
	public boolean clearEmpty() {
		boolean emptyChildren = getAny()==null || ResultsBean.clearEmpty(children,getAny().getAny());
		return emptyChildren && getOtherAttributes().isEmpty()
				&& getPresentationSizeMode() == null && getGspsUID() == null
				&& getWindowCenter() == null;
	}

	/** Return the id for this element, in this case the SOP  Instance UID */
	public String getId() {
		return getSOPInstanceUID()+","+getFrame();
	}
	
	/** A single set command for all the presentation size attributes */
	public void setPresentationSize(PresentationSizeMode size, String topLeft, String bottomRight, Float magnify)
	{
		this.setPresentationSizeMode(size);
		this.setTopLeft(topLeft);
		this.setBottomRight(bottomRight);
		this.setMagnify(magnify);
	}
	
	/** Clears all the presentation size mode information */
	public void clearPresentationSize() {
		setPresentationSize(null,null,null,null);
	}

   /** Removes all the SVG use elements contained in this object */
   public void clearUse() {
      this.use = null;
   }
	
}
