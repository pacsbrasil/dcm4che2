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

import javax.xml.bind.annotation.XmlRootElement;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.LocalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Supports dicom retrieval to XML for SR report type objects */
@XmlRootElement
public class ReportBean extends ReportType implements DicomObjectInterface, LocalModel<String> {
	private static final Logger log = LoggerFactory.getLogger(ReportBean.class);

	/** Create an empty image bean object.
	 */
	public ReportBean() {};
	
	/** Create an report bean from the given dicom data 
	 * @param data to use for the DICOM information
	 */
	public ReportBean(DicomObject data) {
		initAttributes(data);
	}

	/** Initialize the report level attributes by copying the DicomObject's
	 * report level data.
	 * @param data to copy report level data into this from.
	 */
	protected void initAttributes(DicomObject data) {
		if( data==null ) throw new IllegalArgumentException("A valid dicom object must be supplied to initialize the report from.");
		setSOPInstanceUID(data.getString(Tag.SOPInstanceUID));
		setInstanceNumber(data.getInt(Tag.InstanceNumber));
		setCompletion(data.getString(Tag.CompletionFlag));
		setVerification(data.getString(Tag.VerificationFlag));
		initConcept( data.get(Tag.ConceptNameCodeSequence) );
		// TODO - parse the image references IF they are supplied.
		// They are required to be present if references are present, but it doesn't
		// look like they normally actually are included except for key objects.
	}

	/** 
	 * Initialize the concept code values from the given element.
	 * @param element containing the sequences to look at.  Maybe null.
	 */
	protected void initConcept(DicomElement element) {
		if( element==null ) {
			log.debug("Concept code sequence is null.");
			return;
		}
		DicomObject item = element.getDicomObject();
		if( item==null ) {
			log.debug("Get dicom object in concept code sequence is null.");
			return;			
		}
		setConceptMeaning(item.getString(Tag.CodeMeaning));
		setConceptCode(item.getString(Tag.CodeValue));
		log.debug("Report has code meaning and value:"+getConceptCode()+","+getConceptMeaning());
	}

	/** Reports have no current modifications, so return empty all the time. */
	public boolean clearEmpty() {
		return true;
	}

	/** Return the SOP Instance UID for this object as the ID */
	public String getId() {
		return getSOPInstanceUID();
	}

}
