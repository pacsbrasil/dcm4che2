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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlTransient;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.ResultFromDicom;

public class SeriesBean extends SeriesType implements Series, ResultFromDicom
{
	static Logger log = Logger.getLogger(SeriesBean.class.getName());
	
	@XmlTransient
	Map<String,DicomObjectType> children = new HashMap<String,DicomObjectType>();
	
	public SeriesBean() { }
	
	/** Construct a series bean object from the dicom data. */
	public SeriesBean(DicomObject data) {
		initAttributes(data);
		addResult(data);
	}

	/** Initialize the series level attributes */
	protected void initAttributes(DicomObject data) {
		setModality(data.getString(Tag.Modality));
		setSeriesDescription(data.getString(Tag.SeriesDescription));
		setSeriesInstanceUID(data.getString(Tag.SeriesInstanceUID));
		setSeriesNumber(data.getInt(Tag.SeriesNumber));
	}
	
	/** Add any image level information to this series. */
	public void addResult(DicomObject data) {
		String sopInstanceUID = data.getString(Tag.SOPInstanceUID);
		if( sopInstanceUID==null ) return;
		if( children.containsKey(sopInstanceUID) ) {
			log.warning("Series "+getSeriesInstanceUID()+" already contains a child "+sopInstanceUID);
		}
		else {
			DicomObjectType dobj = createChildByModality(data);
			if( dobj==null ) {
				log.warning("No object created for child "+sopInstanceUID+" of modality "+modality);
				return;
			}
			children.put(sopInstanceUID, dobj);			
			getDicomObject().add(dobj);
		}
	}
	
	/** Create different types of children based on the modality of the series */
	protected DicomObjectType createChildByModality(DicomObject data) {
		if( modality.equals("KO") ) {
			log.warning("Modality KO objects not yet defined (Key Objects).");
			return null;
		}
		if( modality.equals("PR")) {
			log.warning("Modality PR objects not yet defined (GSPS).");
			return null;
		}
		return new ImageBean(data);
	}

}
