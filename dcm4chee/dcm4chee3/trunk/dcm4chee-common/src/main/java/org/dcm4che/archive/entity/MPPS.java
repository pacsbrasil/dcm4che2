/*
 * org.dcm4che.archive.entity.MPPS.java
 * Created on May 27, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.entity;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.archive.common.PPSStatus;
import org.dcm4che.archive.dao.CodeDAO;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.spring.BeanId;
import org.dcm4che.util.spring.SpringContext;

/**
 * org.dcm4che.archive.entity.MPPS
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "mpps")
public class MPPS extends EntityBase {

    private static final long serialVersionUID = -2211473862085614582L;

    private static final Logger log = Logger.getLogger(MPPS.class.getName());

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "mpps_iuid", nullable = false)
    private String sopIuid;

    @Column(name = "pps_start")
    private Timestamp ppsStartDateTime;

    @Column(name = "station_aet")
    private String performedStationAET;

    @Column(name = "modality")
    private String modality;

    @Column(name = "accession_no")
    private String accessionNumber;

    @Column(name = "mpps_status")
    private Integer ppsStatusAsInt;

    @Column(name = "mpps_attrs")
    private byte[] encodedAttributes;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Patient.class)
    @JoinColumn(name = "patient_fk", nullable = false)
    private Patient patient;

    @OneToMany(mappedBy = "mpps")
    private Set<Series> series;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Code.class)
    @JoinColumn(name = "drcode_fk")
    private Code drCode;

    /**
     * 
     */
    public MPPS() {
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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
     * @return the drCode
     */
    public Code getDrCode() {
        return drCode;
    }

    /**
     * @param drCode
     *            the drCode to set
     */
    public void setDrCode(Code drCode) {
        this.drCode = drCode;
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
     * @return the performedStationAET
     */
    public String getPerformedStationAET() {
        return performedStationAET;
    }

    /**
     * @param performedStationAET
     *            the performedStationAET to set
     */
    public void setPerformedStationAET(String performedStationAET) {
        this.performedStationAET = performedStationAET;
    }

    /**
     * @return the ppsStartDateTime
     */
    public Timestamp getPpsStartDateTime() {
        return ppsStartDateTime;
    }

    /**
     * @param ppsStartDateTime
     *            the ppsStartDateTime to set
     */
    public void setPpsStartDateTime(Timestamp ppsStartDateTime) {
        this.ppsStartDateTime = ppsStartDateTime;
    }

    /**
     * @return the ppsStatusAsInt
     */
    public Integer getPpsStatusAsInt() {
        return ppsStatusAsInt;
    }

    /**
     * @param ppsStatusAsInt
     *            the ppsStatusAsInt to set
     */
    public void setPpsStatusAsInt(Integer ppsStatusAsInt) {
        this.ppsStatusAsInt = ppsStatusAsInt;
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
     * @return the sopIuid
     */
    public String getSopIuid() {
        return sopIuid;
    }

    /**
     * @param sopIuid
     *            the sopIuid to set
     */
    public void setSopIuid(String sopIuid) {
        this.sopIuid = sopIuid;
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

    public void setPpsStartDateTime(java.util.Date date) {
        setPpsStartDateTime(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
    }

    public boolean isInProgress() {
        return getPpsStatusAsInt() == PPSStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return getPpsStatusAsInt() == PPSStatus.COMPLETED;
    }

    public boolean isDiscontinued() {
        return getPpsStatusAsInt() == PPSStatus.DISCONTINUED;
    }

    public String getPpsStatus() {
        return PPSStatus.toString(getPpsStatusAsInt());
    }

    public void setPpsStatus(String status) {
        setPpsStatusAsInt(PPSStatus.toInt(status));
    }

    public String toString() {
        return "MPPS[pk=" + getPk() + ", iuid=" + getSopIuid() + ", status="
                + getPpsStatus() + ", patient->" + getPatient() + "]";
    }

    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(getEncodedAttributes());
    }

    public void setAttributes(Dataset ds) {
        setPpsStartDateTime(ds
                .getDateTime(Tags.PPSStartDate, Tags.PPSStartTime));
        setPerformedStationAET(ds.getString(Tags.PerformedStationAET));
        setModality(ds.getString(Tags.Modality));
        setPpsStatus(ds.getString(Tags.PPSStatus));
        Dataset ssa = ds.getItem(Tags.ScheduledStepAttributesSeq);
        setAccessionNumber(ssa.getString(Tags.AccessionNumber));
        setDrCode(Code.valueOf(getCodeDAO(), ds
                .getItem(Tags.PPSDiscontinuationReasonCodeSeq)));
        byte[] b = DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    private CodeDAO getCodeDAO() {
        return (CodeDAO) SpringContext.getApplicationContext().getBean(
                BeanId.CODE_DAO.getId());
    }

    public boolean isIncorrectWorklistEntrySelected() {
        Code drcode = getDrCode();
        return drcode != null && "110514".equals(drcode.getCodeValue())
                && "DCM".equals(drcode.getCodingSchemeDesignator());
    }

}
