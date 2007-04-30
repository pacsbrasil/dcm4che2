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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.ResultFromDicom;

public class PatientBean extends PatientType implements Patient, ResultFromDicom 
{
	static DatatypeFactory datatypeFactory;
	static {
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	};

	@XmlTransient
	Map<String,StudyBean> children = new HashMap<String,StudyBean>();

	public PatientBean(DicomObject cmd) {
		initAttributes(cmd);
		addResult(cmd);
	}
	
	protected static String excludeZeroEnd(String str) {
		if( str==null ) return null;
		if( str.length()==0 ) return str;
		if( str.charAt(str.length()-1)==0 ) {
			return str.substring(0,str.length()-1);
		}
		return str;
	}
	
	/** Initialize the primary attributes of this object */
	protected void initAttributes(DicomObject cmd) {	
		setPatientID(cmd.getString(Tag.PatientID));
		setPatientName(excludeZeroEnd(cmd.getString(Tag.PatientName)));
		String strSex = cmd.getString(Tag.PatientSex);
		if(strSex!=null ) {
			setPatientSex( SexEnum.fromValue(strSex));
		}
		Date date = cmd.getDate(Tag.PatientBirthDate);
		if( date!=null ) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			setPatientBirthDate(datatypeFactory.newXMLGregorianCalendar(cal));
		}
	}

	/* (non-Javadoc)
	 * @see org.dcm4chee.xero.search.study.ResultFromDicom#addResult(org.dcm4che2.data.DicomObject)
	 */
	public void addResult(DicomObject data)
	{
		String studyUID = data.getString(Tag.StudyInstanceUID);
		if( children.containsKey(studyUID)) {
			children.get(studyUID).addResult(data);
		}
		else {
			StudyBean child = new StudyBean(data);
			getStudy().add(child);
			children.put(studyUID,child);
		}
	}
}
