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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.filter.CacheItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyBean extends StudyType implements Study, CacheItem
{
	private static Logger log = LoggerFactory.getLogger(StudyBean.class);
	
	@XmlTransient
	Map<String,SeriesBean> children =  new HashMap<String,SeriesBean>();
	
    /** Create a new study bean object from the given data */
	public StudyBean(DicomObject data) {
		initAttributes(data);
		addResult(data);
	}
	
	/** Create a new study bean object, with no data in it */
	public StudyBean() {	
	}

	/** Create a study instance by copying attributes and children (shallow children copy.)
	 * Does not update the children map.
	 * @param study to copy from.
	 */
	public StudyBean(StudyType study) {
		setAccessionNumber(study.getAccessionNumber());
		setInstanceAvailability(study.getInstanceAvailability());
		setModalitiesInStudy(study.getModalitiesInStudy());
		setNumberOfStudyRelatedInstances(study.getNumberOfStudyRelatedInstances());
		setNumberOfStudyRelatedSeries(study.getNumberOfStudyRelatedSeries());
		setReferringPhysicianName(study.getReferringPhysicianName());				
		setStudyDateTime(study.getStudyDateTime());		
		setStudyDescription(study.getStudyDescription());
		setStudyID(study.getStudyID());
		setStudyInstanceUID(study.getStudyInstanceUID());
		setStudyStatusID(study.getStudyStatusID());
		getSeries().addAll(study.getSeries());
	}

	/** Initialize the attributes for this study bean object from the dicom
	 * object provided.
	 * @param data
	 */
	protected void initAttributes(DicomObject data) {
		setAccessionNumber(data.getString(Tag.AccessionNumber));
		setInstanceAvailability(data.getString(Tag.InstanceAvailability));
		setModalitiesInStudy(data.getString(Tag.ModalitiesInStudy));
		setNumberOfStudyRelatedInstances(data.getInt(Tag.NumberOfStudyRelatedInstances));
		setNumberOfStudyRelatedSeries(data.getInt(Tag.NumberOfStudyRelatedSeries));
		setReferringPhysicianName(data.getString(Tag.ReferringPhysicianName));
			
		Date date = null;
		try {
		   date = data.getDate(Tag.StudyDate, Tag.StudyTime);
		}
		catch(NumberFormatException nfe) {
			log.warn("Illegal study date or time:"+nfe);
		}
		if( date!=null ) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			setStudyDateTime(PatientBean.datatypeFactory.newXMLGregorianCalendar(cal));
		}
		
		setStudyDescription(data.getString(Tag.StudyDescription));
		setStudyID(data.getString(Tag.StudyID));
		setStudyInstanceUID(data.getString(Tag.StudyInstanceUID));
		setStudyStatusID(data.getString(Tag.StudyStatusIDRET));		
	}

	/** Add any series and sub-series information to this study object */
	public void addResult(DicomObject data) {
		String seriesUID = data.getString(Tag.SeriesInstanceUID);
		log.debug("Adding information to study seriesUID="+seriesUID);
		if( seriesUID!=null ) {
			log.debug("Adding child to study "+seriesUID);
			if( children.containsKey(seriesUID) ) {
				children.get(seriesUID).addResult(data);
			}
			else {
				SeriesBean child = new SeriesBean(data);
				children.put(seriesUID,child);
				getSeries().add(child);
			}
		} else log.debug("Study "+studyInstanceUID+" does not contain a series information.");
	}

	/** Figure out how many bytes this consumes */
	public long getSize() {
		// Some amount of space for this item
		long ret = 128;
		for(SeriesType series : getSeries()) {
			ret += ((CacheItem) series).getSize();
		}
		return ret;
	}

}
