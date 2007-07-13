/*
 * org.dcm4che.archive.entity.MWLItem.java
 * Created on May 31, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.entity;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.archive.common.SPSStatus;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

/**
 * org.dcm4che.archive.entity.MWLItem
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "mwl_item")
public class MWLItem extends EntityBase {

    private static final long serialVersionUID = -6381545907820724306L;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static Logger log = Logger.getLogger(MWLItem.class.getName());

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "sps_id")
    private String spsId;

    @Column(name = "start_datetime")
    private Timestamp spsStartDateTime;

    @Column(name = "station_aet")
    private String scheduledStationAET;

    @Column(name = "modality")
    private String modality;

    @Column(name = "perf_physician")
    private String performingPhysicianName;

    @Column(name = "perf_phys_i_name")
    private String performingPhysicianIdeographicName;

    @Column(name = "perf_phys_p_name")
    private String performingPhysicianPhoneticName;

    @Column(name = "req_proc_id")
    private String requestedProcedureId;

    @Column(name = "accession_no")
    private String accessionNumber;

    @Column(name = "study_iuid")
    private String studyIuid;

    @Column(name = "item_attrs")
    private byte[] encodedAttributes;

    @Column(name = "sps_status")
    private Integer spsStatusAsInt;

    @Column(name = "station_name")
    private String scheduledStationName;

    @ManyToOne
    @JoinColumn(name = "patient_fk", nullable = false)
    private Patient patient;

    /**
     * 
     */
    public MWLItem() {
    }

    public MWLItem(Dataset ds, Patient patient) {
        setAttributes(ds);
        setPatient(patient);
    }

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
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
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
     * @return the modality
     */
    public String getModality() {
        return modality;
    }

    /**
     * @param modality
     *            the modality to set
     */
    public void setModality(String modality) {
        this.modality = modality;
    }

    /**
     * @return the performingPhysicianIdeographicName
     */
    public String getPerformingPhysicianIdeographicName() {
        return performingPhysicianIdeographicName;
    }

    /**
     * @param performingPhysicianIdeographicName
     *            the performingPhysicianIdeographicName to set
     */
    public void setPerformingPhysicianIdeographicName(
            String performingPhysicianIdeographicName) {
        this.performingPhysicianIdeographicName = performingPhysicianIdeographicName;
    }

    /**
     * @return the performingPhysicianName
     */
    public String getPerformingPhysicianName() {
        return performingPhysicianName;
    }

    /**
     * @param performingPhysicianName
     *            the performingPhysicianName to set
     */
    public void setPerformingPhysicianName(String performingPhysicianName) {
        this.performingPhysicianName = performingPhysicianName;
    }

    /**
     * @return the performingPhysicianPhoneticName
     */
    public String getPerformingPhysicianPhoneticName() {
        return performingPhysicianPhoneticName;
    }

    /**
     * @param performingPhysicianPhoneticName
     *            the performingPhysicianPhoneticName to set
     */
    public void setPerformingPhysicianPhoneticName(
            String performingPhysicianPhoneticName) {
        this.performingPhysicianPhoneticName = performingPhysicianPhoneticName;
    }

    /**
     * @return the requestedProcedureId
     */
    public String getRequestedProcedureId() {
        return requestedProcedureId;
    }

    /**
     * @param requestedProcedureId
     *            the requestedProcedureId to set
     */
    public void setRequestedProcedureId(String requestedProcedureId) {
        this.requestedProcedureId = requestedProcedureId;
    }

    /**
     * @return the scheduledStationAET
     */
    public String getScheduledStationAET() {
        return scheduledStationAET;
    }

    /**
     * @param scheduledStationAET
     *            the scheduledStationAET to set
     */
    public void setScheduledStationAET(String scheduledStationAET) {
        this.scheduledStationAET = scheduledStationAET;
    }

    /**
     * @return the spsId
     */
    public String getSpsId() {
        return spsId;
    }

    /**
     * @param spsId
     *            the spsId to set
     */
    public void setSpsId(String spsId) {
        this.spsId = spsId;
    }

    /**
     * @return the spsStartDateTime
     */
    public Timestamp getSpsStartDateTime() {
        return spsStartDateTime;
    }

    /**
     * @param spsStartDateTime
     *            the spsStartDateTime to set
     */
    public void setSpsStartDateTime(Timestamp spsStartDateTime) {
        this.spsStartDateTime = spsStartDateTime;
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
    public void setStudyIuid(String studyIuid) {
        this.studyIuid = studyIuid;
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    /**
     * @return the spsStatusAsInt
     */
    public Integer getSpsStatusAsInt() {
        return spsStatusAsInt;
    }

    /**
     * @param spsStatusAsInt
     *            the spsStatusAsInt to set
     */
    public void setSpsStatusAsInt(Integer spsStatusAsInt) {
        this.spsStatusAsInt = spsStatusAsInt;
    }

    private void setSpsStartDateTime(Date date) {
        setSpsStartDateTime(date != null ? new Timestamp(date.getTime()) : null);
    }

    public String getSpsStatus() {
        return SPSStatus.toString(getSpsStatusAsInt());
    }

    public void setSpsStatus(String status) {
        setSpsStatusAsInt(SPSStatus.toInt(status));
    }

    /**
     * @return the scheduledStationName
     */
    public String getScheduledStationName() {
        return scheduledStationName;
    }

    /**
     * @param scheduledStationName
     *            the scheduledStationName to set
     */
    public void setScheduledStationName(String scheduledStationName) {
        this.scheduledStationName = scheduledStationName;
    }

    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(getEncodedAttributes());
    }

    public void setAttributes(Dataset ds) {

        Dataset spsItem = ds.getItem(Tags.SPSSeq);
        if (spsItem == null) {
            throw new IllegalArgumentException(
                    "Missing Scheduled Procedure Step Sequence (0040,0100) Item");
        }
        setSpsId(spsItem.getString(Tags.SPSID));
        setSpsStatus(spsItem.getString(Tags.SPSStatus, "SCHEDULED"));
        setSpsStartDateTime(spsItem.getDateTime(Tags.SPSStartDate,
                Tags.SPSStartTime));
        setScheduledStationAET(spsItem.getString(Tags.ScheduledStationAET));
        setScheduledStationName(spsItem.getString(Tags.ScheduledStationName));
        PersonName pn = spsItem.getPersonName(Tags.PerformingPhysicianName);
        if (pn != null) {
            setPerformingPhysicianName(toUpperCase(pn
                    .toComponentGroupString(false)));
            PersonName ipn = pn.getIdeographic();
            if (ipn != null) {
                setPerformingPhysicianIdeographicName(ipn
                        .toComponentGroupString(false));
            }
            PersonName ppn = pn.getPhonetic();
            if (ppn != null) {
                setPerformingPhysicianPhoneticName(ppn
                        .toComponentGroupString(false));
            }
        }
        setModality(spsItem.getString(Tags.Modality));
        setRequestedProcedureId(ds.getString(Tags.RequestedProcedureID));
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        setStudyIuid(ds.getString(Tags.StudyInstanceUID));
        byte[] b = DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    private static String toUpperCase(String s) {
        return s != null ? s.toUpperCase() : null;
    }

    public void updateSpsStatus(int status) {
        if (status == getSpsStatusAsInt())
            return;
        Dataset ds = getAttributes();
        Dataset spsItem = ds.getItem(Tags.SPSSeq);
        spsItem.putCS(Tags.SPSStatus, SPSStatus.toString(status));
        setSpsStatusAsInt(status);
        byte[] b = DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    public String toString() {
        java.sql.Timestamp spsDT = getSpsStartDateTime();
        return "MWLItem[pk="
                + getPk()
                + ", spsId="
                + getSpsId()
                + ", spsStartDateTime="
                + (spsDT != null ? new SimpleDateFormat(DATE_FORMAT)
                        .format(spsDT) : "") + ", spsStatus=" + getSpsStatus()
                + ", stationAET=" + getScheduledStationAET() + ", rqProcId="
                + getRequestedProcedureId() + ", modality=" + getModality()
                + ", accessionNo=" + getAccessionNumber() + ", patient->"
                + getPatient() + "]";
    }
}