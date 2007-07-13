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
 * Damien Evans <damien.daddy@gmail.com>
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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.archive.common.GPSPSPriority;
import org.dcm4che.archive.common.GPSPSStatus;
import org.dcm4che.archive.common.InputAvailabilityFlag;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

/**
 * org.dcm4che.archive.entity.GPSPS
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "gpsps")
public class GPSPS extends EntityBase{

    private static final long serialVersionUID = 3486845756388838735L;

    @Column(name = "gpsps_iuid")
    private String sopIuid;

    @Column(name = "gpsps_tuid")
    private String transactionUid;

    @Column(name = "start_datetime")
    private Timestamp spsStartDateTime;

    @Column(name = "end_datetime")
    private Timestamp expectedCompletionDateTime;

    @Column(name = "gpsps_status")
    private Integer gpspsStatusAsInt;

    @Column(name = "gpsps_prior")
    private Integer gpspsPriorityAsInt;

    @Column(name = "in_availability")
    private Integer inputAvailability;

    @Column(name = "item_attrs")
    private byte[] encodedAttributes;

    @ManyToOne
    @JoinColumn(name = "patient_fk")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "code_fk")
    private Code scheduledWorkItemCode;
    
    @ManyToMany
    @JoinTable(
            name="rel_gpsps_appcode",
            joinColumns=
                @JoinColumn(name="appcode_fk", referencedColumnName="pk"),
            inverseJoinColumns=
                @JoinColumn(name="gpsps_fk", referencedColumnName="pk")
        )
    private Collection <Code> scheduledProcessingApplicationsCodes;
    
    @ManyToMany
    @JoinTable(
            name="rel_gpsps_devname",
            joinColumns=
                @JoinColumn(name="devname_fk", referencedColumnName="pk"),
            inverseJoinColumns=
                @JoinColumn(name="gpsps_fk", referencedColumnName="pk")
        )
    private Collection <Code> scheduledStationNameCodes;
    
    @ManyToMany
    @JoinTable(
            name="rel_gpsps_devclass",
            joinColumns=
                @JoinColumn(name="devclass_fk", referencedColumnName="pk"),
            inverseJoinColumns=
                @JoinColumn(name="gpsps_fk", referencedColumnName="pk")
        )
    private Collection <Code> scheduledStationClassCodes;
    
    @ManyToMany
    @JoinTable(
            name="rel_gpsps_devloc",
            joinColumns=
                @JoinColumn(name="devloc_fk", referencedColumnName="pk"),
            inverseJoinColumns=
                @JoinColumn(name="gpsps_fk", referencedColumnName="pk")
        )
    private Collection <Code> scheduledStationGeographicLocationCodes;

    @OneToMany(mappedBy = "gpsps")
    private List<GPSPSRequest> refRequests;
    
    @OneToMany(mappedBy = "gpsps")
    private List<GPSPSPerformer> scheduledHumanPerformers;
    
    @ManyToMany
    @JoinTable(
            name="rel_gpsps_gppps",
            joinColumns=
                @JoinColumn(name="gpsps_fk", referencedColumnName="pk"),
            inverseJoinColumns=
                @JoinColumn(name="gppps_fk", referencedColumnName="pk")
        )
    private Collection<GPPPS> gppps;

    /**
     * 
     */
    public GPSPS() {
    }

    public Integer getInputAvailability() {
        return inputAvailability;
    }

    public void setInputAvailability(Integer availability) {
        this.inputAvailability = availability;
    }

    /**
     * @ejb.interface-method
     */
    public String getInputAvailabilityFlag() {
        return InputAvailabilityFlag.toString(getInputAvailability());
    }

    public void setInputAvailabilityFlag(String availability) {
        setInputAvailability(InputAvailabilityFlag.toInt(availability));
    }

    public Code getScheduledWorkItemCode() {
        return scheduledWorkItemCode;
    }

    public void setScheduledWorkItemCode(Code code) {
        this.scheduledWorkItemCode = code;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public void setEncodedAttributes(byte[] encodedAttributes) {
        this.encodedAttributes = encodedAttributes;
    }

    public Timestamp getExpectedCompletionDateTime() {
        return expectedCompletionDateTime;
    }

    public void setExpectedCompletionDateTime(Timestamp endDate) {
        this.expectedCompletionDateTime = endDate;
    }

    public String getSopIuid() {
        return sopIuid;
    }

    public void setSopIuid(String gpspsIuid) {
        this.sopIuid = gpspsIuid;
    }

    public String getGpspsPriority() {
        return GPSPSPriority.toString(getGpspsPriorityAsInt());
    }

    public void setGpspsPriority(String prior) {
        setGpspsPriorityAsInt(GPSPSPriority.toInt(prior));
    }

    public Integer getGpspsPriorityAsInt() {
        return gpspsPriorityAsInt;
    }

    public void setGpspsPriorityAsInt(Integer gpspsPriority) {
        this.gpspsPriorityAsInt = gpspsPriority;
    }

    public String getGpspsStatus() {
        return GPSPSStatus.toString(gpspsStatusAsInt);
    }

    public void setGpspsStatus(String status) {
        this.gpspsStatusAsInt = GPSPSStatus.toInt(status);
    }

    public Integer getGpspsStatusAsInt() {
        return gpspsStatusAsInt;
    }

    public void setGpspsStatusAsInt(Integer gpspsStatus) {
        this.gpspsStatusAsInt = gpspsStatus;
    }

    public String getTransactionUid() {
        return transactionUid;
    }

    public void setTransactionUid(String gpspsTuid) {
        this.transactionUid = gpspsTuid;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Timestamp getSpsStartDateTime() {
        return spsStartDateTime;
    }

    public void setSpsStartDateTime(Timestamp startDate) {
        this.spsStartDateTime = startDate;
    }

    public List<GPSPSRequest> getRefRequests() {
        return refRequests;
    }

    public void setRefRequests(List<GPSPSRequest> refRequests) {
        this.refRequests = refRequests;
    }

    public List<GPSPSPerformer> getScheduledHumanPerformers() {
        return scheduledHumanPerformers;
    }

    public void setScheduledHumanPerformers(
            List<GPSPSPerformer> scheduledHumanPerformers) {
        this.scheduledHumanPerformers = scheduledHumanPerformers;
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(getEncodedAttributes());
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSopIuid(ds.getString(Tags.SOPInstanceUID));
        setGpspsStatus(ds.getString(Tags.GPSPSStatus));
        setGpspsPriority(ds.getString(Tags.GPSPSPriority));
        setInputAvailabilityFlag(ds.getString(Tags.InputAvailabilityFlag));
        setSpsStartDateTime(toTimestamp(ds.getDate(Tags.SPSStartDateAndTime)));
        setExpectedCompletionDateTime(toTimestamp(ds
                .getDate(Tags.ExpectedCompletionDateAndTime)));
        setEncodedAttributes(DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian));
    }
    
    public boolean isScheduled() {
        return getGpspsStatusAsInt() == GPSPSStatus.SCHEDULED;
    }

    public boolean isInProgress() {
        return getGpspsStatusAsInt() == GPSPSStatus.IN_PROGRESS;
    }

    public boolean isSuspended() {
        return getGpspsStatusAsInt() == GPSPSStatus.SUSPENDED;
    }

    public boolean isCompleted() {
        return getGpspsStatusAsInt() == GPSPSStatus.COMPLETED;
    }

    public boolean isDiscontinued() {
        return getGpspsStatusAsInt() == GPSPSStatus.DISCONTINUED;
    }
    
    private static java.sql.Timestamp toTimestamp(java.util.Date date) {
        return date != null ? new java.sql.Timestamp(date.getTime()) : null;
    }

    public String toString() {
        return "GPSPS[pk=" + getPk() + ", iuid=" + getSopIuid() + ", pat->"
                + getPatient() + "]";
    }

    /**
     * @return the gppps
     */
    public Collection<GPPPS> getGppps() {
        return gppps;
    }

    /**
     * @param gppps the gppps to set
     */
    public void setGppps(Collection<GPPPS> gppps) {
        this.gppps = gppps;
    }

    /**
     * @return the scheduledProcessingApplicationsCodes
     */
    public Collection<Code> getScheduledProcessingApplicationsCodes() {
        return scheduledProcessingApplicationsCodes;
    }

    /**
     * @param scheduledProcessingApplicationsCodes the scheduledProcessingApplicationsCodes to set
     */
    public void setScheduledProcessingApplicationsCodes(
            Collection<Code> scheduledProcessingApplicationsCodes) {
        this.scheduledProcessingApplicationsCodes = scheduledProcessingApplicationsCodes;
    }

    /**
     * @return the scheduledStationClassCodes
     */
    public Collection<Code> getScheduledStationClassCodes() {
        return scheduledStationClassCodes;
    }

    /**
     * @param scheduledStationClassCodes the scheduledStationClassCodes to set
     */
    public void setScheduledStationClassCodes(
            Collection<Code> scheduledStationClassCodes) {
        this.scheduledStationClassCodes = scheduledStationClassCodes;
    }

    /**
     * @return the scheduledStationGeographicLocationCodes
     */
    public Collection<Code> getScheduledStationGeographicLocationCodes() {
        return scheduledStationGeographicLocationCodes;
    }

    /**
     * @param scheduledStationGeographicLocationCodes the scheduledStationGeographicLocationCodes to set
     */
    public void setScheduledStationGeographicLocationCodes(
            Collection<Code> scheduledStationGeographicLocationCodes) {
        this.scheduledStationGeographicLocationCodes = scheduledStationGeographicLocationCodes;
    }

    /**
     * @return the scheduledStationNameCodes
     */
    public Collection<Code> getScheduledStationNameCodes() {
        return scheduledStationNameCodes;
    }

    /**
     * @param scheduledStationNameCodes the scheduledStationNameCodes to set
     */
    public void setScheduledStationNameCodes(
            Collection<Code> scheduledStationNameCodes) {
        this.scheduledStationNameCodes = scheduledStationNameCodes;
    }
}