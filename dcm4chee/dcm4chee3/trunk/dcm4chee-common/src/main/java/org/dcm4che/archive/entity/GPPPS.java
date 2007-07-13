/*
 * org.dcm4che.archive.entity.GPPPS.java
 * Created on May 31, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.entity;

import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.archive.common.PPSStatus;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

/**
 * org.dcm4che.archive.entity.GPPPS
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "gppps")
public class GPPPS extends EntityBase {
    private static final long serialVersionUID = -9191775482339656876L;
    
    private static final Logger log = Logger.getLogger(GPPPS.class.getName());

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "pps_iuid", nullable = false)
    private String sopIuid;

    @Column(name = "pps_start")
    private Timestamp ppsStartDateTime;

    @Column(name = "pps_status", nullable = false)
    private Integer ppsStatusAsInt;

    @Column(name = "pps_attrs")
    private byte[] encodedAttributes;

    @ManyToOne
    private Patient patient;

    @ManyToMany
    @JoinTable(name = "rel_gpsps_gppps", joinColumns = @JoinColumn(name = "gppps_fk", referencedColumnName = "pk"), inverseJoinColumns = @JoinColumn(name = "gpsps_fk", referencedColumnName = "pk"))
    private Collection<GPSPS> gpsps;

    /**
     * 
     */
    public GPPPS() {
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
     * @return the gpsps
     */
    public Collection<GPSPS> getGpsps() {
        return gpsps;
    }

    /**
     * @param gpsps
     *            the gpsps to set
     */
    public void setGpsps(Collection<GPSPS> gpsps) {
        this.gpsps = gpsps;
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
     * @return the ppsStartDateTime
     */
    public Timestamp getPpsStartDateTime() {
        return ppsStartDateTime;
    }

    /**
     * @param ppsStartDateTime
     *            {@link Timestamp} object containing the date-time when the
     *            procedure step started.
     */
    public void setPpsStartDateTime(Timestamp ppsStartDateTime) {
        this.ppsStartDateTime = ppsStartDateTime;
    }

    /**
     * Convenience method to set the timestamp directly from a {@link Dataset}
     * object.
     * 
     * @param date
     *            {@link java.util.Date} object containing the date-time when
     *            the procedure step started.
     */
    public void setPpsStartDateTime(java.util.Date date) {
        setPpsStartDateTime(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
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

    /**
     * Determine if this GPPPS is in progress.
     * 
     * @return True if the procedure step is in progress.
     */
    public boolean isInProgress() {
        return getPpsStatusAsInt() == PPSStatus.IN_PROGRESS;
    }

    /**
     * Determine if this GPPPS is completed.
     * 
     * @return True if the procedure step is completed.
     */
    public boolean isCompleted() {
        return getPpsStatusAsInt() == PPSStatus.COMPLETED;
    }

    /**
     * 
     * Determine if this GPPPS is discontinued.
     * 
     * @return True if the procedure step is discontinued.
     */
    public boolean isDiscontinued() {
        return getPpsStatusAsInt() == PPSStatus.DISCONTINUED;
    }

    /**
     * Get the PPS status as a string.
     * 
     * @return The status string.
     */
    public String getPpsStatus() {
        return PPSStatus.toString(getPpsStatusAsInt());
    }

    /**
     * Set the PPS status as a string.
     * 
     * @param status
     *            A String containing the status.
     */
    public void setPpsStatus(String status) {
        setPpsStatusAsInt(PPSStatus.toInt(status));
    }

    /**
     * Set the attributes of this GPPPS as a DICOM dataset.
     * 
     * @param ds
     *            {@link Dataset}
     */
    public void setAttributes(Dataset ds) {
        setPpsStartDateTime(ds
                .getDateTime(Tags.PPSStartDate, Tags.PPSStartTime));
        setPpsStatus(ds.getString(Tags.GPPPSStatus));
        byte[] b = DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    /**
     * Get the attributes of this GPPPS as a DICOM dataset.
     * 
     * @return {@link Dataset}
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(getEncodedAttributes());
    }

}
