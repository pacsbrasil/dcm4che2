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
package org.dcm4chee.xero.display;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;

/** This class has information about the study being viewed currently.
 * There could easily be multiple instances of this bean, in separate
 * conversations for multiple studies at once.
 * @author bwallace
 *
 */
@Name("ImageViewed")
@Scope(ScopeType.EVENT)
public class ImageViewed {

	public static final int DEFAULT_IMAGE_COUNT = 32;
	/** The position is the ordinal within a given series view of which image is to be displayed.
	 * This may or may not correspond with the object or image number.
	 */
	int position = 0;
	
	/** The object UID contains the UID of the object being modified.  In general, this is used when 
	 * modifying an image presentation, whereas position is used for displaying an image.
	 */
	String objectUID;
	
	/** Get the position to view */
	public int getPosition() {
		return position;
	}
	
	/** Set the image position (ie p'th position) to view.
	 * 
	 * @param 1st position to view.
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
	/** Returns a computed value that re-uses the same positional information
	 * many times for the meta-data calls, to avoid having too many calls.
	 */
	public int getComputedPosition() {
		int cp = getPosition() - (getPosition() % getCount());
		return cp;
	}
	
	/** 
	 * Return the number of images for which to retrieve meta-data
	 * @return
	 */
	public int getCount() {
		return DEFAULT_IMAGE_COUNT;
	}

	/** Get the object UID for the object being modified.  */
	public String getObjectUID() {
		return objectUID;
	}

	/** Sets the object UID for the object being modified.  */
	public void setObjectUID(String objectUID) {
		this.objectUID = objectUID;
	}
}
