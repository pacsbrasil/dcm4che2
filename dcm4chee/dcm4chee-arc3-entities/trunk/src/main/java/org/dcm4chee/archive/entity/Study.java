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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chee.archive.entity;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.PersonName;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.exceptions.ConfigurationException;
import org.dcm4chee.archive.util.DicomObjectUtils;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 25, 2008
 */
@Entity
@Table(name = "study")
public class Study implements Serializable {

    private static final long serialVersionUID = -5851890695263668359L;

    // JPA definition in orm.xml
    private long pk;

    @Column(name = "created_time")
    private Date createdTime;

    @Column(name = "updated_time")
    private Date updatedTime;

    @Column(name = "study_iuid", nullable = false)
    private String studyInstanceUID;

    @Column(name = "study_id")
    private String studyID;

    @Column(name = "study_datetime")
    private Date studyDateTime;

    @Column(name = "accession_no")
    private String accessionNumber;

    // JPA definition in orm.xml
    private String referringPhysicianName;

    // JPA definition in orm.xml
    private String referringPhysicianIdeographicName;

    // JPA definition in orm.xml
    private String referringPhysicianPhoneticName;

    // JPA definition in orm.xml
    private String studyDescription;

    @Column(name = "study_custom1")
    private String studyCustomAttribute1;

    @Column(name = "study_custom2")
    private String studyCustomAttribute2;

    @Column(name = "study_custom3")
    private String studyCustomAttribute3;

    // JPA definition in orm.xml
    private byte[] encodedAttributes;

    @Column(name = "num_series", nullable = false)
    private int numberOfStudyRelatedSeries;

    @Column(name = "num_instances", nullable = false)
    private int numberOfStudyRelatedInstances;

    @Column(name = "mods_in_study")
    private String modalitiesInStudy;

    @Column(name = "cuids_in_study")
    private String sopClassesInStudy;

    @Column(name = "retrieve_aets")
    private String retrieveAETs;

    @Column(name = "ext_retr_aet")
    private String externalRetrieveAET;

    @Column(name = "fileset_id")
    private String fileSetID;

    @Column(name = "fileset_iuid")
    private String fileSetUID;

    @Column(name = "availability", nullable = false)
    private Availability availability;

    @Column(name = "study_status", nullable = false)
    private int studyStatus;

    @Column(name = "study_status_id")
    private String studyStatusID;
    
    @Column(name = "checked_time")
    private Timestamp timeOfLastConsistencyCheck;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
            name="rel_study_pcode",
            joinColumns=
                @JoinColumn(name="study_fk", referencedColumnName="pk"),
            inverseJoinColumns=
                @JoinColumn(name="pcode_fk", referencedColumnName="pk")
        )
    private Set<Code> procedureCodes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_fk")
    private Patient patient;

    @OneToMany(mappedBy = "study", fetch = FetchType.LAZY)
    private Set<Series> series;

    public final long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getStudyID() {
        return studyID;
    }

    public Date getStudyDateTime() {
        return studyDateTime;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    public String getReferringPhysicianIdeographicName() {
        return referringPhysicianIdeographicName;
    }

    public String getReferringPhysicianPhoneticName() {
        return referringPhysicianPhoneticName;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public String getStudyCustomAttribute1() {
        return studyCustomAttribute1;
    }

    public String getStudyCustomAttribute2() {
        return studyCustomAttribute2;
    }

    public String getStudyCustomAttribute3() {
        return studyCustomAttribute3;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public int getNumberOfStudyRelatedSeries() {
        return numberOfStudyRelatedSeries;
    }

    public void setNumberOfStudyRelatedSeries(int numberOfStudyRelatedSeries) {
        this.numberOfStudyRelatedSeries = numberOfStudyRelatedSeries;
    }

    public int getNumberOfStudyRelatedInstances() {
        return numberOfStudyRelatedInstances;
    }

    public void setNumberOfStudyRelatedInstances(
            int numberOfStudyRelatedInstances) {
        this.numberOfStudyRelatedInstances = numberOfStudyRelatedInstances;
    }

    public String getModalitiesInStudy() {
        return modalitiesInStudy;
    }

    public void setModalitiesInStudy(String modalitiesInStudy) {
        this.modalitiesInStudy = modalitiesInStudy;
    }

    public String getSopClassesInStudy() {
        return sopClassesInStudy;
    }

    public void setSopClassesInStudy(String sopClassesInStudy) {
        this.sopClassesInStudy = sopClassesInStudy;
    }

    public String getRetrieveAETs() {
        return retrieveAETs;
    }

    public void setRetrieveAETs(String retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
    }

    public String getExternalRetrieveAET() {
        return externalRetrieveAET;
    }

    public void setExternalRetrieveAET(String externalRetrieveAET) {
        this.externalRetrieveAET = externalRetrieveAET;
    }

    public String getFileSetID() {
        return fileSetID;
    }

    public void setFileSetID(String fileSetID) {
        this.fileSetID = fileSetID;
    }

    public String getFileSetUID() {
        return fileSetUID;
    }

    public void setFileSetUID(String fileSetUID) {
        this.fileSetUID = fileSetUID;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    @Deprecated
    public String getStudyStatusID() {
        return studyStatusID;
    }

    @Deprecated
    public void setStudyStatusID(String studyStatusID) {
        this.studyStatusID = studyStatusID;
    }

    @Deprecated
    public int getStudyStatus() {
        return studyStatus;
    }

    @Deprecated
    public void setStudyStatus(int studyStatus) {
        this.studyStatus = studyStatus;
    }
    
    public Timestamp getTimeOfLastConsistencyCheck() {
        return timeOfLastConsistencyCheck;
    }

    public void setTimeOfLastConsistencyCheck(Timestamp timeOfLastConsistencyCheck) {
        this.timeOfLastConsistencyCheck = timeOfLastConsistencyCheck;
    }

    public Set<Code> getProcedureCodes() {
        return procedureCodes;
    }

    public void setProcedureCodes(Set<Code> procedureCodes) {
        this.procedureCodes = procedureCodes;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Set<Series> getSeries() {
        return series;
    }

    @Override
    public String toString() {
        return "Study[pk=" + pk
        + ", uid=" + studyInstanceUID
        + ", id=" + studyID
        + ", accno=" + accessionNumber
        + ", date=" + studyDateTime
        + ", desc=" + studyDescription
        + ", mods=" + modalitiesInStudy
        + ", cuids=" + sopClassesInStudy
        + ", refphys=" + referringPhysicianName
        + ", numseries=" + numberOfStudyRelatedSeries
        + ", numinsts=" + numberOfStudyRelatedInstances
        + ", avail=" + availability
        + ", aets=" + retrieveAETs
        + ", extaet=" + externalRetrieveAET
        + ", fsid=" + fileSetID
        + ", fsuid=" + fileSetUID
        + "]";
    }

    public void onPrePersist() {
        createdTime = new Date();
    }

    public void onPreUpdate() {
        updatedTime = new Date();
    }

    public DicomObject getAttributes(boolean cfindrsp) {
        DicomObject dataset = DicomObjectUtils.decode(encodedAttributes);
        if (cfindrsp) {
            dataset.putInt(Tag.NumberOfStudyRelatedSeries, VR.IS,
                    numberOfStudyRelatedSeries);
            dataset.putInt(Tag.NumberOfStudyRelatedInstances, VR.IS,
                    numberOfStudyRelatedInstances);
            dataset.putString(Tag.ModalitiesInStudy, VR.CS, modalitiesInStudy);
            dataset.putString(Tag.SOPClassesInStudy, VR.UI, sopClassesInStudy);
            if (fileSetUID != null && fileSetID != null) {
                dataset.putString(Tag.StorageMediaFilesetUID, VR.UI, fileSetUID);
                dataset.putString(Tag.StorageMediaFilesetID, VR.SH, fileSetID);
            }
            if (retrieveAETs != null || externalRetrieveAET != null) {
                dataset.putString(Tag.RetrieveAETitle, VR.AE, 
                        externalRetrieveAET == null ? retrieveAETs
                                : retrieveAETs == null ? externalRetrieveAET
                                : retrieveAETs + '\\' + externalRetrieveAET);
            }
            dataset.putString(Tag.InstanceAvailability, VR.CS,
                    availability.name());
        }
        return dataset;
    }


    public void setAttributes(DicomObject attrs) {
        this.studyInstanceUID = attrs.getString(Tag.StudyInstanceUID);
        this.studyID = attrs.getString(Tag.StudyID, "");
        this.studyDateTime = attrs.getDate(Tag.StudyDate, Tag.StudyTime);
        this.accessionNumber = attrs.getString(Tag.AccessionNumber, "");
        PersonName pn = new PersonName(attrs
                .getString(Tag.ReferringPhysicianName));
        this.referringPhysicianName = pn.componentGroupString(
                PersonName.SINGLE_BYTE, false).toUpperCase();
        this.referringPhysicianIdeographicName = pn.componentGroupString(
                PersonName.IDEOGRAPHIC, false);
        this.referringPhysicianPhoneticName = pn.componentGroupString(
                PersonName.PHONETIC, false);
        this.studyDescription = attrs.getString(Tag.StudyDescription, "");
        AttributeFilter filter = AttributeFilter.getStudyAttributeFilter();
        int[] fieldTags = filter.getFieldTags();
        for (int i = 0; i < fieldTags.length; i++) {
            try {
                Study.class.getField(filter.getField(fieldTags[i])).set(this,
                        attrs.getString(fieldTags[i], ""));
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }
        }
        this.encodedAttributes = DicomObjectUtils.encode(filter.filter(attrs),
                filter.getTransferSyntaxUID());
    }
}
