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

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.filter.CacheItem;
import org.dcm4chee.xero.search.LocalModel;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a patient object, plus any macros and other customizations.
 * 
 * @author bwallace
 */
@XmlRootElement(namespace = "http://www.dcm4chee.org/xero/search/study/", name = "patient")
public class PatientBean extends PatientType implements Patient,
		ResultFromDicom, CacheItem, LocalModel<PatientIdentifier> {
	private static final Logger log = LoggerFactory
			.getLogger(PatientBean.class);
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
	final Map<Object, Object> children;

	/** This is the object based patient identifier, not the string PatientId */
	@XmlTransient
	PatientIdentifier idPatientIdentifier;

	/** Create a patient bean from the dicom object */
	public PatientBean(Map<Object, Object> children, DicomObject cmd) {
		this.children = children;
		initAttributes(cmd);
		addResult(cmd);
	}

	/** Create an empty patient bean */
	public PatientBean(Map<Object, Object> children) {
		this.children = children;
	}

	/** A really empty patient bean, without children recording */
	public PatientBean() {
		children = new HashMap<Object, Object>();
	};

	/**
	 * Create a copy of the patient object by copying the attributes and
	 * children (shallow copy).
	 * 
	 * @param patient
	 */
	public PatientBean(Map<Object, Object> children, PatientType patient) {
		this.children = children;
		setPatientID(patient.getPatientID());
		if (patient instanceof PatientBean) {
			setId(((PatientBean) patient).getId());
		} else {
			setPatientIdentifier(patient.getPatientIdentifier());
		}
		setPatientName(patient.getPatientName());
		setPatientSex(patient.getPatientSex());
		setPatientBirthDate(patient.getPatientBirthDate());
		setOtherPatientIDs(patient.getOtherPatientIDs());
		setPatientAge(patient.getPatientAge());
		setAdditionalPatientHistory(patient.getAdditionalPatientHistory());
		setCurrentPatientLocation(patient.getCurrentPatientLocation());
		setConfidentialityCode(patient.getConfidentialityCode());
		getStudy().addAll(patient.getStudy());
	}

	/** Excludes a zero at the end of the string from being included. Empty
	 * strings become nulls, and some characters (0 and 0x1b) are removed/spaced out
	 */
	public static String sanitizeString(String str) {
		if (str == null)
			return null;
        if (str.length() == 0)
           return null;
		if (str.charAt(str.length() - 1) == 0) {
		    int posn = str.indexOf(0);
		    assert posn>=0;
			return str.substring(0, posn);
		}
        if (str.indexOf(0x1B) >= 0) {
           str = str.replace((char) 0x1B, ' ');
        }
		return str;
	}

	/** Initialize the primary attributes of this object */
	protected void initAttributes(DicomObject cmd) {
		setOtherPatientIDs(cmd.getString(Tag.OtherPatientIDs));
		setPatientAge(cmd.getString(Tag.PatientAge));
		setAdditionalPatientHistory(cmd.getString(Tag.AdditionalPatientHistory));
		setCurrentPatientLocation(cmd.getString(Tag.CurrentPatientLocation));
		setConfidentialityCode(cmd.getString(Tag.ConfidentialityCode));
		setPatientID(cmd.getString(Tag.PatientID));
		// For now, just use the ID as the patient identifier
		// This maybe changed further up by patient linking etc.
		setId(new PatientIdentifier(getPatientID()));
		setPatientName(sanitizeString(cmd.getString(Tag.PatientName)));
		String strSex = cmd.getString(Tag.PatientSex);
		if (strSex != null) {
			try {
				setPatientSex(SexEnum.fromValue(strSex.toUpperCase()));
			} catch (IllegalArgumentException e) {
				log.warn("Caught illegal sex value " + strSex);
				setPatientSex(SexEnum.O);
			}
		}
		Date date;
		try {
			date = cmd.getDate(Tag.PatientBirthDate);
		} catch (Exception e) {
			date = null;
		}
		if (date != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			setPatientBirthDate(datatypeFactory.newXMLGregorianCalendar(cal));
		}

		String patComments = cmd.getString(Tag.PatientComments);
		if (patComments != null)
			setPatientComments(patComments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dcm4chee.xero.search.study.ResultFromDicom#addResult(org.dcm4che2
	 * .data.DicomObject)
	 */
	public void addResult(DicomObject data) {
		String key = StudyBean.key(data.getString(Tag.StudyInstanceUID));
		if (children.containsKey(key)) {
			((ResultFromDicom) children.get(key)).addResult(data);
		} else {
			StudyBean child = new StudyBean(children, data);
			getStudy().add(child);
			children.put(key, child);
		}
	}

	/** Figure out how many bytes this consumes */
	public long getSize() {
		// Some amount of space for this item
		long ret = 128;
		for (StudyType study : getStudy()) {
			ret += ((CacheItem) study).getSize();
		}
		return ret;
	}

	@XmlAttribute(name = "patientNameF")
	public String getPatientNameF() {
		return ResultsBean.formatDicomName(this.patientName);
	}

	@XmlAttribute(name = "birthDateF")
	public String getBirthDateFormatted() {
		if (this.patientBirthDate == null)
			return null;
		GregorianCalendar gc = patientBirthDate.toGregorianCalendar();
		Date time = gc.getTime();
		DateFormat df = DateFormat.getDateInstance();
		return df.format(time);
	}

	/**
	 * Clear the study child attributes and reutrn true if there are no study
	 * children
	 */
	public boolean clearEmpty() {
		return ResultsBean.clearEmpty(children, getStudy());
	}

	public void setId(PatientIdentifier pi) {
		patientIdentifier = pi.toString();
		this.idPatientIdentifier = pi;
	}

	public PatientIdentifier getId() {
		if (idPatientIdentifier == null) {
			idPatientIdentifier = new PatientIdentifier(getPatientIdentifier());
		}
		return idPatientIdentifier;
	}

	@Override
	public void setPatientIdentifier(String value) {
		super.setPatientIdentifier(value);
		idPatientIdentifier = new PatientIdentifier(value);
	}

	/**
	 * Use the idPatientIdentifier primarily, and otherwise use the regular
	 * patient identifier or finally the patient ID if none of the above are
	 * available.
	 */
	@Override
	public String getPatientIdentifier() {
		if (idPatientIdentifier != null)
			return idPatientIdentifier.toString();
		if (super.getPatientIdentifier() != null)
			return super.getPatientIdentifier();
		return getPatientID();
	}

}
