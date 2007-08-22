
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
 * Gunter Zeilinger <gunterze@gmail.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 * Justin Falk <jfalkmu@gmail.com>
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

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
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
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;

/**
 * DICOM series representation.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "series")
public class Series extends EntityBase {

    private static final long serialVersionUID = 260534793147719547L;

    private static final Logger logger = Logger.getLogger(Series.class);

    private static final Class[] STRING_PARAM = new Class[] { String.class };

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "series_iuid", length = 64)
    private String seriesIuid;

    @Column(name = "series_no")
    private String seriesNumber;

    @Column(name = "modality")
    private String modality;

    @Column(name = "department")
    private String institutionalDepartmentName;

    @Column(name = "institution")
    private String institutionName;
    
    @Column(name = "station_name")
    private String stationName;

    @Column(name = "num_instances")
    private int numberOfSeriesRelatedInstances;

    @Column(name = "series_attrs")
    private byte[] encodedAttributes;

    @Column(name = "src_aet", length = 16)
    private String sourceAET;

    @Column(name = "availability")
    private Integer availability;

    @Column(name = "series_status")
    private int seriesStatus;

    @Column(name = "body_part")
    private String bodyPartExamined;

    @Column(name = "laterality")
    private String laterality;

    @Column(name = "series_custom1")
    private String seriesCustomAttribute1;

    @Column(name = "series_custom2")
    private String seriesCustomAttribute2;

    @Column(name = "series_custom3")
    private String seriesCustomAttribute3;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Study.class)
    @JoinColumn(name = "study_fk", nullable = false)
    private Study study;

    @OneToMany(mappedBy = "series")
    private Set<Instance> instances;

    @Column(name = "pps_start")
    private Timestamp ppsStartDateTime;

    @Column(name = "pps_iuid")
    private String ppsIuid;

    @Column(name = "fileset_iuid")
    private String filesetIuid;

    @Column(name = "fileset_id")
    private String filesetId;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.MPPS.class)
    @JoinColumn(name = "mpps_fk")
    private MPPS mpps;

    @OneToMany(mappedBy = "series")
    private Set<SeriesRequest> requestAttributes;

    @Column(name = "retrieve_aets")
    private String retrieveAETs;

    @Column(name = "ext_retr_aet")
    private String externalRetrieveAET;

    public Series() {
        super();
    }

    /**
     * Functional constructor for the Series. <strong>Note</strong> that this
     * constructor will not update the related series attributes (series request, mpps), you'll need to
     * go through the DAO for that (or do the same thing another way).
     * 
     * @param ds
     *            The {@link Dataset} containing the series attributes.
     * @param study
     *            The {@link Study} that this series belongs to.
     */
    public Series(Dataset ds, Study study) {
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        setSourceAET(ds.getString(PrivateTags.CallingAET));
        setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        setStudy(study);
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
     * @return the instances
     */
    public Set<Instance> getInstances() {
        return instances;
    }

    /**
     * @param instances
     *            the instances to set
     */
    public void setInstances(Set<Instance> instances) {
        this.instances = instances;
    }

    /**
     * @return the institutionalDepartmentName
     */
    public String getInstitutionalDepartmentName() {
        return institutionalDepartmentName;
    }

    /**
     * @param institutionalDepartmentName
     *            the institutionalDepartmentName to set
     */
    public void setInstitutionalDepartmentName(
            String institutionalDepartmentName) {
        this.institutionalDepartmentName = institutionalDepartmentName;
    }

    /**
     * @return the institutionName
     */
    public String getInstitutionName() {
        return institutionName;
    }

    /**
     * @param institutionName
     *            the institutionName to set
     */
    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    /**
     * @return the stationName
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * @param stationName the stationName to set
     */
    public void setStationName(String stationName) {
        this.stationName = stationName;
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
     * @return the numberOfSeriesRelatedInstances
     */
    public int getNumberOfSeriesRelatedInstances() {
        return numberOfSeriesRelatedInstances;
    }

    /**
     * @param numberOfSeriesRelatedInstances
     *            the numberOfSeriesRelatedInstances to set
     */
    public void setNumberOfSeriesRelatedInstances(
            int numberOfSeriesRelatedInstances) {
        this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
    }

    /**
     * @return the seriesNumber
     */
    public String getSeriesNumber() {
        return seriesNumber;
    }

    /**
     * @param seriesNumber
     *            the seriesNumber to set
     */
    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    /**
     * @return the seriesStatus
     */
    public int getSeriesStatus() {
        return seriesStatus;
    }

    /**
     * @param seriesStatus
     *            the seriesStatus to set
     */
    public void setSeriesStatus(int seriesStatus) {
        this.seriesStatus = seriesStatus;
    }

    /**
     * @return the seriesIuid
     */
    public String getSeriesIuid() {
        return seriesIuid;
    }

    /**
     * @param seriesIuid
     *            the seriesIuid to set
     */
    public void setSeriesIuid(String seriesIuid) {
        this.seriesIuid = seriesIuid;
    }

    /**
     * @return the sourceAET
     */
    public String getSourceAET() {
        return sourceAET;
    }

    /**
     * @param sourceAET
     *            the sourceAET to set
     */
    public void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET;
    }

    /**
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * @param study
     *            the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
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
    public void setFilesetId(String filesetID) {
        this.filesetId = filesetID;
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
    public void setFilesetIuid(String filesetUID) {
        this.filesetIuid = filesetUID;
    }

    /**
     * @return the mpps
     */
    public MPPS getMpps() {
        return mpps;
    }

    /**
     * @param mpps
     *            the mpps to set
     */
    public void setMpps(MPPS mpps) {
        this.mpps = mpps;
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
     * @return the ppsIuid
     */
    public String getPpsIuid() {
        return ppsIuid;
    }

    /**
     * @param ppsIuid
     *            the ppsIuid to set
     */
    public void setPpsIuid(String ppsUID) {
        this.ppsIuid = ppsUID;
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
    public void setRetrieveAETs(String retrieveAET) {
        this.retrieveAETs = retrieveAET;
    }

    /**
     * @return the requestAttributes
     */
    public Set<SeriesRequest> getRequestAttributes() {
        return requestAttributes;
    }

    /**
     * @param requestAttributes
     *            the requestAttributes to set
     */
    public void setRequestAttributes(Set<SeriesRequest> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    /**
     * @return the bodyPartExamined
     */
    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    /**
     * @param bodyPartExamined
     *            the bodyPartExamined to set
     */
    public void setBodyPartExamined(String bodyPartExamined) {
        this.bodyPartExamined = bodyPartExamined;
    }

    /**
     * @return the laterality
     */
    public String getLaterality() {
        return laterality;
    }

    /**
     * @param laterality
     *            the laterality to set
     */
    public void setLaterality(String laterality) {
        this.laterality = laterality;
    }

    /**
     * @return the seriesCustomAttribute1
     */
    public String getSeriesCustomAttribute1() {
        return seriesCustomAttribute1;
    }

    /**
     * @param seriesCustomAttribute1
     *            the seriesCustomAttribute1 to set
     */
    public void setSeriesCustomAttribute1(String seriesCustomAttribute1) {
        this.seriesCustomAttribute1 = seriesCustomAttribute1;
    }

    /**
     * @return the seriesCustomAttribute2
     */
    public String getSeriesCustomAttribute2() {
        return seriesCustomAttribute2;
    }

    /**
     * @param seriesCustomAttribute2
     *            the seriesCustomAttribute2 to set
     */
    public void setSeriesCustomAttribute2(String seriesCustomAttribute2) {
        this.seriesCustomAttribute2 = seriesCustomAttribute2;
    }

    /**
     * @return the seriesCustomAttribute3
     */
    public String getSeriesCustomAttribute3() {
        return seriesCustomAttribute3;
    }

    /**
     * @param seriesCustomAttribute3
     *            the seriesCustomAttribute3 to set
     */
    public void setSeriesCustomAttribute3(String seriesCustomAttribute3) {
        this.seriesCustomAttribute3 = seriesCustomAttribute3;
    }

    public void setAttributes(Dataset ds) {
        String cuid = ds.getString(Tags.SOPClassUID);
        AttributeFilter filter = AttributeFilter.getSeriesAttributeFilter(cuid);
        setAttributesInternal(filter.filter(ds), filter.getTransferSyntaxUID());
        int[] fieldTags = filter.getFieldTags();
        for (int i = 0; i < fieldTags.length; i++) {
            setField(filter.getField(fieldTags[i]), ds.getString(fieldTags[i]));
        }
    }

    private void setField(String field, String value) {
        try {
            Method m = Series.class.getMethod("set"
                    + Character.toUpperCase(field.charAt(0))
                    + field.substring(1), STRING_PARAM);
            m.invoke(this, new Object[] { value });
        }
        catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private void setAttributesInternal(Dataset ds, String tsuid) {
        setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        setSeriesNumber(ds.getString(Tags.SeriesNumber));
        setModality(ds.getString(Tags.Modality));
        setInstitutionName(toUpperCase(ds.getString(Tags.InstitutionName)));
        setInstitutionalDepartmentName(toUpperCase(ds
                .getString(Tags.InstitutionalDepartmentName)));
        setStationName(toUpperCase(ds.getString(Tags.StationName)));
        try {
            setPpsStartDateTime(ds.getDateTime(Tags.PPSStartDate,
                    Tags.PPSStartTime));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Illegal PPS Date/Time format: " + e.getMessage());
        }
        Dataset refPPS = ds.getItem(Tags.RefPPSSeq);
        if (refPPS != null) {
            setPpsIuid(refPPS.getString(Tags.RefSOPInstanceUID));
        }
        byte[] b = DatasetUtils.toByteArray(ds, tsuid);
        if (logger.isDebugEnabled()) {
            logger.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    private void setPpsStartDateTime(Date dateTime) {
        setPpsStartDateTime(new Timestamp(dateTime.getTime()));
    }

    /**
     * @throws DcmServiceException
     * @ejb.interface-method
     */
    public void coerceAttributes(Dataset ds, Dataset coercedElements)
            throws DcmServiceException {
        Dataset attrs = getAttributes(false);
        String cuid = ds.getString(Tags.SOPClassUID);
        AttributeFilter filter = AttributeFilter.getSeriesAttributeFilter(cuid);
        AttrUtils.coerceAttributes(attrs, ds, coercedElements, filter, logger);
        if (AttrUtils.mergeAttributes(attrs, filter.filter(ds), logger)) {
            setAttributesInternal(attrs, filter.getTransferSyntaxUID());
        }
    }

    private static String toUpperCase(String s) {
        return s != null ? s.toUpperCase() : null;
    }

    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds
                    .putOB(PrivateTags.SeriesPk, Convert.toBytes(getPk()
                            .longValue()));
            ds.putAE(PrivateTags.CallingAET, getSourceAET());
            ds.putIS(Tags.NumberOfSeriesRelatedInstances,
                    getNumberOfSeriesRelatedInstances());
            ds.putSH(Tags.StorageMediaFileSetID, getFilesetId());
            ds.putUI(Tags.StorageMediaFileSetUID, getFilesetIuid());
            DatasetUtils.putRetrieveAET(ds, getRetrieveAETs(),
                    getExternalRetrieveAET());
            ds.putCS(Tags.InstanceAvailability, Availability
                    .toString(getAvailability()));
        }
        return ds;
    }

    public String toString() {
        return "Series[pk=" + getPk() + ", uid=" + getSeriesIuid()
                + ", study->" + getStudy() + "]";
    }

}