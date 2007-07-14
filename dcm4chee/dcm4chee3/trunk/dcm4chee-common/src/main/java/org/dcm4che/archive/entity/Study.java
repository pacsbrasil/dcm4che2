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
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Justin Falk <jfalkmu@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
 * Gunter Zeilinger <gunterze@gmail.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
package org.dcm4che.archive.entity;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.archive.common.PrivateTags;
import org.dcm4che.archive.exceptions.ConfigurationException;
import org.dcm4che.archive.util.AttributeFilter;
import org.dcm4che.archive.util.Convert;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4cheri.util.StringUtils;
import org.hibernate.annotations.CollectionOfElements;

/**
 * org.dcm4che.archive.entity.Study
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "study")
public class Study extends EntityBase {

    private static final long serialVersionUID = -4119704363889016956L;

    private static final Logger log = Logger.getLogger(Study.class.getName());

    private static final Class[] STRING_PARAM = new Class[] { String.class };

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "study_iuid", nullable = false)
    private String studyIuid;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "study_datetime")
    private Timestamp studyDateTime;

    @Column(name = "accession_no")
    private String accessionNumber;

    @Column(name = "ref_physician")
    private String referringPhysicianName;

    @Column(name = "ref_phys_i_name")
    private String referringPhysicianIdeographicName;

    @Column(name = "ref_phys_p_name")
    private String referringPhysicianPhoneticName;

    @Column(name = "study_desc")
    private String studyDescription;

    @Column(name = "study_status", nullable = false)
    private int studyStatus;

    @Column(name = "study_status_id")
    private String studyStatusId;

    @Column(name = "num_series", nullable = false)
    private int numberOfStudyRelatedSeries;

    @Column(name = "num_instances", nullable = false)
    private int numberOfStudyRelatedInstances;

    @Column(name = "study_attrs")
    private byte[] encodedAttributes;

    @Column(name = "availability", nullable = false)
    private Integer availability;

    @Column(name = "mods_in_study")
    private String modalitiesInStudy;

    @Column(name = "checked_time")
    private Timestamp timeOfLastConsistencyCheck;

    @Column(name = "ext_retr_aet")
    private String externalRetrieveAET;

    @Column(name = "retrieve_aets")
    private String retrieveAETs;

    @Column(name = "fileset_id")
    private String filesetId;

    @Column(name = "fileset_iuid")
    private String filesetIuid;

    @Column(name = "study_custom1")
    private String studyCustomAttribute1;

    @Column(name = "study_custom2")
    private String studyCustomAttribute2;

    @Column(name = "study_custom3")
    private String studyCustomAttribute3;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Patient.class)
    @JoinColumn(name = "patient_fk", nullable = false)
    private Patient patient;

    @OneToMany(mappedBy = "study")
    private Set<Series> series;

    @OneToMany(mappedBy = "study")
    private Set<StudyPermission> studyPermissions;

    @CollectionOfElements(fetch = FetchType.EAGER)
    @JoinTable(name = "series", joinColumns = @JoinColumn(name = "study_fk"))
    @Column(name = "institution", insertable = false, updatable = false)
    private Set<String> institutions;

    @CollectionOfElements(fetch = FetchType.EAGER)
    @JoinTable(name = "series", joinColumns = @JoinColumn(name = "study_fk"))
    @Column(name = "body_part", insertable = false, updatable = false)
    private Set<String> bodyParts;

    /**
     * @return the accessionNumber
     */
    public String getAccessionNumber() {
        return accessionNumber;
    }

    /**
     * @param accessionNumber
     *            the accessionNumber to set
     */
    public void setAccessionNumber(String accecssionNumber) {
        this.accessionNumber = accecssionNumber;
    }

    /**
     * @return the availability
     */
    public Integer getAvailability() {
        return availability == null ? 0 : availability;
    }

    /**
     * @param availability
     *            the availability to set
     */
    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    /**
     * @return the createdTime
     */
    public Timestamp getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createdTime
     *            the createdTime to set
     */
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return the encodedAttributes
     */
    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    /**
     * @param encodedAttributes
     *            the encodedAttributes to set
     */
    public void setEncodedAttributes(byte[] encodedAttributes) {
        this.encodedAttributes = encodedAttributes;
    }

    /**
     * @return the modalitiesInStudy
     */
    public String getModalitiesInStudy() {
        return modalitiesInStudy;
    }

    /**
     * @param modalitiesInStudy
     *            the modalitiesInStudy to set
     */
    public void setModalitiesInStudy(String modalitiesInStudy) {
        this.modalitiesInStudy = modalitiesInStudy;
    }

    /**
     * @return the numberOfStudyRelatedInstances
     */
    public int getNumberOfStudyRelatedInstances() {
        return numberOfStudyRelatedInstances;
    }

    /**
     * @param numberOfStudyRelatedInstances
     *            the numberOfStudyRelatedInstances to set
     */
    public void setNumberOfStudyRelatedInstances(
            int numberOfStudyRelatedInstances) {
        this.numberOfStudyRelatedInstances = numberOfStudyRelatedInstances;
    }

    /**
     * @return the numberOfStudyRelatedSeries
     */
    public int getNumberOfStudyRelatedSeries() {
        return numberOfStudyRelatedSeries;
    }

    /**
     * @param numberOfStudyRelatedSeries
     *            the numberOfStudyRelatedSeries to set
     */
    public void setNumberOfStudyRelatedSeries(int numberOfStudyRelatedSeries) {
        this.numberOfStudyRelatedSeries = numberOfStudyRelatedSeries;
    }

    /**
     * @return the patient
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * @param patient
     *            the patient to set
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    /**
     * @return the referringPhysicianIdeographicName
     */
    public String getReferringPhysicianIdeographicName() {
        return referringPhysicianIdeographicName;
    }

    /**
     * @param referringPhysicianIdeographicName
     *            the referringPhysicianIdeographicName to set
     */
    public void setReferringPhysicianIdeographicName(
            String referringPhysicianIdeographicName) {
        this.referringPhysicianIdeographicName = referringPhysicianIdeographicName;
    }

    /**
     * @return the referringPhysicianName
     */
    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    /**
     * @param referringPhysicianName
     *            the referringPhysicianName to set
     */
    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    /**
     * @return the referringPhysicianPhoneticName
     */
    public String getReferringPhysicianPhoneticName() {
        return referringPhysicianPhoneticName;
    }

    /**
     * @param referringPhysicianPhoneticName
     *            the referringPhysicianPhoneticName to set
     */
    public void setReferringPhysicianPhoneticName(
            String referringPhysicianPhoneticName) {
        this.referringPhysicianPhoneticName = referringPhysicianPhoneticName;
    }

    /**
     * @return the series
     */
    public Set<Series> getSeries() {
        return series;
    }

    /**
     * @param series
     *            the series to set
     */
    public void setSeries(Set<Series> series) {
        this.series = series;
    }

    /**
     * @return the studyDateTime
     */
    public Timestamp getStudyDateTime() {
        return studyDateTime;
    }

    /**
     * @param studyDateTime
     *            the studyDateTime to set
     */
    public void setStudyDateTime(Timestamp studyDateTime) {
        this.studyDateTime = studyDateTime;
    }

    /**
     * @return the studyDescription
     */
    public String getStudyDescription() {
        return studyDescription;
    }

    /**
     * @param studyDescription
     *            the studyDescription to set
     */
    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    /**
     * @return the studyId
     */
    public String getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            the studyId to set
     */
    public void setStudyId(String studyID) {
        this.studyId = studyID;
    }

    /**
     * @return the studyStatus
     */
    public int getStudyStatus() {
        return studyStatus;
    }

    /**
     * @param studyStatus
     *            the studyStatus to set
     */
    public void setStudyStatus(int studyStatus) {
        this.studyStatus = studyStatus;
    }

    /**
     * @return the studyStatusId
     */
    public String getStudyStatusId() {
        return studyStatusId;
    }

    /**
     * @param studyStatusId
     *            the studyStatusId to set
     */
    public void setStudyStatusId(String studyStatusID) {
        this.studyStatusId = studyStatusID;
    }

    /**
     * @return the studyIuid
     */
    public String getStudyIuid() {
        return studyIuid;
    }

    /**
     * @param studyIuid
     *            the studyIuid to set
     */
    public void setStudyIuid(String studyUID) {
        this.studyIuid = studyUID;
    }

    /**
     * @return the timeOfLastConsistencyCheck
     */
    public Timestamp getTimeOfLastConsistencyCheck() {
        return timeOfLastConsistencyCheck;
    }

    /**
     * @param timeOfLastConsistencyCheck
     *            the timeOfLastConsistencyCheck to set
     */
    public void setTimeOfLastConsistencyCheck(
            Timestamp timeOfLastConsistencyCheck) {
        this.timeOfLastConsistencyCheck = timeOfLastConsistencyCheck;
    }

    /**
     * @return the updatedTime
     */
    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    /**
     * @param updatedTime
     *            the updatedTime to set
     */
    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * @return the filesetId
     */
    public String getFilesetId() {
        return filesetId;
    }

    /**
     * @param filesetId
     *            the filesetId to set
     */
    public void setFilesetId(String filesetId) {
        this.filesetId = filesetId;
    }

    /**
     * @return the filesetIuid
     */
    public String getFilesetIuid() {
        return filesetIuid;
    }

    /**
     * @param filesetIuid
     *            the filesetIuid to set
     */
    public void setFilesetIuid(String filesetIuid) {
        this.filesetIuid = filesetIuid;
    }

    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putOB(PrivateTags.StudyPk, Convert.toBytes(getPk().longValue()));
            ds.putCS(Tags.ModalitiesInStudy, StringUtils.split(
                    getModalitiesInStudy(), '\\'));
            ds.putIS(Tags.NumberOfStudyRelatedSeries,
                    getNumberOfStudyRelatedSeries());
            ds.putIS(Tags.NumberOfStudyRelatedInstances,
                    getNumberOfStudyRelatedInstances());
            ds.putSH(Tags.StorageMediaFileSetID, getFilesetId());
            ds.putUI(Tags.StorageMediaFileSetUID, getFilesetIuid());
            DatasetUtils.putRetrieveAET(ds, getRetrieveAETs(),
                    getExternalRetrieveAET());
            ds.putCS(Tags.InstanceAvailability, Availability
                    .toString(getAvailability()));
            ds.putCS(Tags.StudyStatusID, getStudyStatusId());
        }
        return ds;
    }

    public void setAttributes(Dataset ds) {
        String cuid = ds.getString(Tags.SOPClassUID);
        AttributeFilter filter = AttributeFilter.getStudyAttributeFilter(cuid);
        setAttributesInternal(filter.filter(ds), filter.getTransferSyntaxUID());
        int[] fieldTags = filter.getFieldTags();
        for (int i = 0; i < fieldTags.length; i++) {
            setField(filter.getField(fieldTags[i]), ds.getString(fieldTags[i]));
        }
    }

    private void setField(String field, String value) {
        try {
            Method m = Study.class.getMethod("set"
                    + Character.toUpperCase(field.charAt(0))
                    + field.substring(1), STRING_PARAM);
            m.invoke(this, new Object[] { value });
        }
        catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private void setAttributesInternal(Dataset ds, String tsuid) {
        setStudyIuid(ds.getString(Tags.StudyInstanceUID));
        setStudyId(ds.getString(Tags.StudyID));
        try {
            setStudyDateTime(ds.getDateTime(Tags.StudyDate, Tags.StudyTime));
        }
        catch (IllegalArgumentException e) {
            log.warn("Illegal Study Date/Time format: " + e.getMessage());
        }
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        PersonName pn = ds.getPersonName(Tags.ReferringPhysicianName);
        if (pn != null) {
            setReferringPhysicianName(toUpperCase(pn
                    .toComponentGroupString(false)));
            PersonName ipn = pn.getIdeographic();
            if (ipn != null) {
                setReferringPhysicianIdeographicName(ipn
                        .toComponentGroupString(false));
            }
            PersonName ppn = pn.getPhonetic();
            if (ppn != null) {
                setReferringPhysicianPhoneticName(ppn
                        .toComponentGroupString(false));
            }
        }
        setStudyDescription(toUpperCase(ds.getString(Tags.StudyDescription)));
        byte[] b = DatasetUtils.toByteArray(ds, tsuid);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    public void coerceAttributes(Dataset ds, Dataset coercedElements)
            throws DcmServiceException {
        Dataset attrs = getAttributes(false);
        String cuid = ds.getString(Tags.SOPClassUID);
        AttributeFilter filter = AttributeFilter.getStudyAttributeFilter(cuid);
        AttrUtils.coerceAttributes(attrs, ds, coercedElements, filter, log);
        if (AttrUtils.mergeAttributes(attrs, filter.filter(ds), log)) {
            setAttributesInternal(attrs, filter.getTransferSyntaxUID());
        }
    }

    private void setStudyDateTime(java.util.Date date) {
        setStudyDateTime(date != null ? new java.sql.Timestamp(date.getTime())
                : null);
    }

    /**
     * @return the externalRetrieveAET
     */
    public String getExternalRetrieveAET() {
        return externalRetrieveAET;
    }

    /**
     * @param externalRetrieveAET
     *            the externalRetrieveAET to set
     */
    public void setExternalRetrieveAET(String externalRetrieveAET) {
        this.externalRetrieveAET = externalRetrieveAET;
    }

    /**
     * @return the retrieveAETs
     */
    public String getRetrieveAETs() {
        return retrieveAETs;
    }

    /**
     * @param retrieveAETs
     *            the retrieveAETs to set
     */
    public void setRetrieveAETs(String retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Study[pk=" + getPk() + ", uid=" + studyIuid + ", patient->"
                + getPatient() + "]";
    }

    private static String toUpperCase(String s) {
        return s != null ? s.toUpperCase() : null;
    }

    public Set<String> getInstitutions() {
        if (institutions != null) {
            institutions.remove(null);
        }
        return institutions;
    }

    public void setInstitutions(Set<String> institutions) {
        this.institutions = institutions;
    }

    public Set<String> getBodyParts() {
        if (bodyParts != null) {
            bodyParts.remove(null);
        }
        return bodyParts;
    }

    public void setBodyParts(Set<String> bodyParts) {
        this.bodyParts = bodyParts;
    }

    /**
     * @return the studyCustomAttribute1
     */
    public String getStudyCustomAttribute1() {
        return studyCustomAttribute1;
    }

    /**
     * @param studyCustomAttribute1
     *            the studyCustomAttribute1 to set
     */
    public void setStudyCustomAttribute1(String studyCustomAttribute1) {
        this.studyCustomAttribute1 = studyCustomAttribute1;
    }

    /**
     * @return the studyCustomAttribute2
     */
    public String getStudyCustomAttribute2() {
        return studyCustomAttribute2;
    }

    /**
     * @param studyCustomAttribute2
     *            the studyCustomAttribute2 to set
     */
    public void setStudyCustomAttribute2(String studyCustomAttribute2) {
        this.studyCustomAttribute2 = studyCustomAttribute2;
    }

    /**
     * @return the studyCustomAttribute3
     */
    public String getStudyCustomAttribute3() {
        return studyCustomAttribute3;
    }

    /**
     * @param studyCustomAttribute3
     *            the studyCustomAttribute3 to set
     */
    public void setStudyCustomAttribute3(String studyCustomAttribute3) {
        this.studyCustomAttribute3 = studyCustomAttribute3;
    }

    /**
     * @return the studyPermissions
     */
    public Set<StudyPermission> getStudyPermissions() {
        return studyPermissions;
    }

    /**
     * @param studyPermissions
     *            the studyPermissions to set
     */
    public void setStudyPermissions(Set<StudyPermission> studyPermissions) {
        this.studyPermissions = studyPermissions;
    }

}