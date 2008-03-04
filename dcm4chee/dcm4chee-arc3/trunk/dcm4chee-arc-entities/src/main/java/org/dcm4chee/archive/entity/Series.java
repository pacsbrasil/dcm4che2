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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.PersonName;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.common.PrivateTag;
import org.dcm4chee.archive.common.StorageStatus;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.exceptions.ConfigurationException;
import org.dcm4chee.archive.util.DicomObjectUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 25, 2008
 */

@Entity
@EntityListeners( { EntityLogger.class })
@Table(name = "series")
public class Series implements Serializable {

    private static final long serialVersionUID = -5882522097745649285L;

    @Id
    @GeneratedValue
    @Column(name = "pk")
    private long pk;

    @Column(name = "created_time")
    private Date createdTime;

    @Column(name = "updated_time")
    private Date updatedTime;

    @Column(name = "series_iuid", nullable = false, unique = true)
    private String seriesInstanceUID;

    @Column(name = "series_no")
    private String seriesNumber;

    @Column(name = "series_desc")
    private String seriesDescription;

    @Column(name = "modality")
    private String modality;

    @Column(name = "department")
    private String institutionalDepartmentName;

    @Column(name = "institution")
    private String institutionName;

    @Column(name = "station_name")
    private String stationName;

    @Column(name = "body_part")
    private String bodyPartExamined;

    @Column(name = "laterality")
    private String laterality;

    @Column(name = "perf_physician")
    private String performingPhysicianName;

    @Column(name = "perf_phys_i_name")
    private String performingPhysicianIdeographicName;

    @Column(name = "perf_phys_p_name")
    private String performingPhysicianPhoneticName;

    @Column(name = "pps_start")
    private Date performedProcedureStepStartDateTime;

    @Column(name = "pps_iuid")
    private String performedProcedureStepInstanceUID;

    @Column(name = "series_custom1")
    private String seriesCustomAttribute1;

    @Column(name = "series_custom2")
    private String seriesCustomAttribute2;

    @Column(name = "series_custom3")
    private String seriesCustomAttribute3;

    @Column(name = "series_attrs")
    private byte[] encodedAttributes;

    @Column(name = "num_instances")
    private int numberOfSeriesRelatedInstances;

    @Column(name = "src_aet")
    private String sourceAET;

    @Column(name = "retrieve_aets")
    private String retrieveAETs;

    @Column(name = "ext_retr_aet")
    private String externalRetrieveAET;

    @Column(name = "fileset_iuid")
    private String fileSetUID;

    @Column(name = "fileset_id")
    private String fileSetID;

    @Column(name = "availability")
    private Availability availability;

    @Column(name = "series_status")
    private StorageStatus storageStatus;

    @OneToMany(mappedBy = "series")
    private Set<RequestAttributes> requestAttributes;

    @ManyToOne
    @JoinColumn(name = "mpps_fk")
    private MPPS modalityPerformedProcedureStep;

    @ManyToOne
    @JoinColumn(name = "study_fk", nullable = false)
    private Study study;

    @OneToMany(mappedBy = "series")
    private Set<Instance> instances;

    public final long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public String getModality() {
        return modality;
    }

    public String getInstitutionalDepartmentName() {
        return institutionalDepartmentName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public String getStationName() {
        return stationName;
    }

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public String getLaterality() {
        return laterality;
    }

    public String getPerformingPhysicianName() {
        return performingPhysicianName;
    }

    public String getPerformingPhysicianIdeographicName() {
        return performingPhysicianIdeographicName;
    }

    public String getPerformingPhysicianPhoneticName() {
        return performingPhysicianPhoneticName;
    }

    public Date getPerformedProcedureStepStartDateTime() {
        return performedProcedureStepStartDateTime;
    }

    public String getPerformedProcedureStepInstanceUID() {
        return performedProcedureStepInstanceUID;
    }

    public String getSeriesCustomAttribute1() {
        return seriesCustomAttribute1;
    }

    public String getSeriesCustomAttribute2() {
        return seriesCustomAttribute2;
    }

    public String getSeriesCustomAttribute3() {
        return seriesCustomAttribute3;
    }

    public int getNumberOfSeriesRelatedInstances() {
        return numberOfSeriesRelatedInstances;
    }

    public void setNumberOfSeriesRelatedInstances(
            int numberOfSeriesRelatedInstances) {
        this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
    }

    public String getSourceAET() {
        return sourceAET;
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

    public String getFileSetUID() {
        return fileSetUID;
    }

    public void setFileSetUID(String fileSetUID) {
        this.fileSetUID = fileSetUID;
    }

    public String getFileSetID() {
        return fileSetID;
    }

    public void setFileSetID(String fileSetID) {
        this.fileSetID = fileSetID;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public StorageStatus getStorageStatus() {
        return storageStatus;
    }

    public void setStorageStatus(StorageStatus storageStatus) {
        this.storageStatus = storageStatus;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public Set<RequestAttributes> getRequestAttributes() {
        return requestAttributes;
    }

    public void setRequestAttributes(Set<RequestAttributes> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    public MPPS getModalityPerformedProcedureStep() {
        return modalityPerformedProcedureStep;
    }

    public void setModalityPerformedProcedureStep(
            MPPS modalityPerformedProcedureStep) {
        this.modalityPerformedProcedureStep = modalityPerformedProcedureStep;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Set<Instance> getInstances() {
        return instances;
    }

    @Override
    public String toString() {
        return "Series[pk=" + pk
                + ", uid=" + seriesInstanceUID
                + ", serno=" + seriesNumber
                + ", desc=" + seriesDescription
                + ", mod=" + modality
                + ", station=" + stationName
                + ", department=" + institutionalDepartmentName
                + ", institution=" + institutionName
                + ", srcaet=" + sourceAET
                + ", bodypart=" + bodyPartExamined
                + ", laterality=" + laterality
                + ", performer=" + performingPhysicianName
                + ", start=" + performedProcedureStepStartDateTime
                + ", ppsuid=" + performedProcedureStepInstanceUID
                + (modalityPerformedProcedureStep != null
                        ? ", MPPS[pk=" + modalityPerformedProcedureStep.getPk()
                                + "]"
                        : "")
                + ", numinsts=" + numberOfSeriesRelatedInstances
                + ", status=" + storageStatus
                + ", avail=" + availability
                + ", aets=" + retrieveAETs
                + ", extaet=" + externalRetrieveAET
                + ", fsid=" + fileSetID
                + ", fsuid=" + fileSetUID
                + (study != null
                        ? ", Study[pk=" + study.getPk()
                                + ", uid=" + study.getStudyInstanceUID()
                                + "]"
                        : "")
                + "]";
    }

    @PrePersist
    public void onPrePersist() {
        createdTime = new Date();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = new Date();
    }

    public DicomObject getAttributes() throws IOException {
        return DicomObjectUtils.decode(encodedAttributes);
    }

    public void setAttributes(DicomObject attrs) {
        this.seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID);
        this.seriesNumber = attrs.getString(Tag.SeriesNumber, "");
        this.seriesDescription = attrs.getString(Tag.SeriesDescription, "");
        this.modality = attrs.getString(Tag.Modality, "");
        this.institutionalDepartmentName = attrs.getString(
                Tag.InstitutionalDepartmentName, "");
        this.institutionName = attrs.getString(Tag.InstitutionName, "");
        this.stationName = attrs.getString(Tag.StationName, "");
        this.sourceAET = attrs.getString(attrs.resolveTag(
                PrivateTag.CallingAET, PrivateTag.CreatorID));
        this.bodyPartExamined = attrs.getString(Tag.BodyPartExamined, "");
        this.laterality = attrs.getString(Tag.Laterality, "");
        PersonName pn = new PersonName(attrs
                .getString(Tag.PerformingPhysicianName));
        this.performingPhysicianName = pn.componentGroupString(
                PersonName.SINGLE_BYTE, false).toUpperCase();
        this.performingPhysicianIdeographicName = pn.componentGroupString(
                PersonName.IDEOGRAPHIC, false);
        this.performingPhysicianPhoneticName = pn.componentGroupString(
                PersonName.PHONETIC, false);
        this.performedProcedureStepStartDateTime = attrs.getDate(
                Tag.PerformedProcedureStepStartDate,
                Tag.PerformedProcedureStepStartTime);
        this.performedProcedureStepInstanceUID = attrs.getString(new int[] {
                Tag.ReferencedPerformedProcedureStepSequence, 0,
                Tag.ReferencedSOPInstanceUID }, "");
        AttributeFilter filter = AttributeFilter.getSeriesAttributeFilter();
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
