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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4cheri.util.StringUtils;
import org.hibernate.annotations.Cascade;

/**
 * org.dcm4che.archive.entity.Instance
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "instance")
public class Instance extends EntityBase {

    private static final long serialVersionUID = -1636129289496485916L;

    private static final Logger logger = Logger.getLogger(Instance.class);

    private static final Class[] STRING_PARAM = new Class[] { String.class };

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "sop_iuid")
    private String sopIuid;

    @Column(name = "sop_cuid")
    private String sopCuid;

    @Column(name = "inst_no")
    private String instanceNumber;

    @Column(name = "content_datetime")
    private Timestamp contentDateTime;

    @Column(name = "sr_complete")
    private String srCompletionFlag;

    @Column(name = "sr_verified")
    private String srVerificationFlag;

    @Column(name = "inst_attrs")
    private byte[] encodedAttributes;

    @Column(name = "availability")
    private Integer availability;

    @Column(name = "inst_status")
    private int instanceStatus;

    @Column(name = "all_attrs")
    private boolean allAttributes;

    @Column(name = "commitment")
    private Boolean commitment;

    @Column(name = "ext_retr_aet")
    private String externalRetrieveAET;

    @Column(name = "retrieve_aets")
    private String retrieveAETs;

    @Column(name = "inst_custom1")
    private String instanceCustomAttribute1;

    @Column(name = "inst_custom2")
    private String instanceCustomAttribute2;

    @Column(name = "inst_custom3")
    private String instanceCustomAttribute3;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Series.class)
    @JoinColumn(name = "series_fk", nullable = false)
    private Series series;

    @OneToMany(mappedBy = "instance")
    private Set<File> files;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Code.class)
    @JoinColumn(name = "srcode_fk")
    private Code srCode;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Media.class)
    @JoinColumn(name = "media_fk")
    private Media media;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE }, mappedBy = "instance")
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<VerifyingObserver> verifyingObservers;

    /**
     * Default constructor
     */
    public Instance() {
        super();
    }

    /**
     * Constructor which sets this instance's internal attributes, and series
     * relationship. It does not set the SR Code or Verifying Observer though,
     * as these are persistent entities which need to be looked up and set
     * separately.
     */
    public Instance(Dataset ds, Series series) {
        setAttributes(ds);
        setSeries(series);
    }

    /**
     * Create an instance with the specified attributes.
     * 
     * @param attrs
     *            A {@link Dataset} containing the instance attributes.
     */
    public Instance(Dataset attrs) {
        setAttributes(attrs);
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp time) {
        this.createdTime = time;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp time) {
        this.updatedTime = time;
    }

    public String getSopIuid() {
        return sopIuid;
    }

    public void setSopIuid(String iuid) {
        this.sopIuid = iuid;
    }

    public String getSopCuid() {
        return sopCuid;
    }

    public void setSopCuid(String cuid) {
        this.sopCuid = cuid;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(String no) {
        this.instanceNumber = no;
    }

    public Timestamp getContentDateTime() {
        return contentDateTime;
    }

    public void setContentDateTime(Timestamp dateTime) {
        this.contentDateTime = dateTime;
    }

    private void setContentDateTime(java.util.Date date) {
        setContentDateTime(date != null ? new java.sql.Timestamp(date.getTime())
                : null);
    }

    public String getSrCompletionFlag() {
        return srCompletionFlag;
    }

    public void setSrCompletionFlag(String flag) {
        this.srCompletionFlag = flag;
    }

    public String getSrVerificationFlag() {
        return srVerificationFlag;
    }

    public void setSrVerificationFlag(String flag) {
        this.srVerificationFlag = flag;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public void setEncodedAttributes(byte[] bytes) {
        this.encodedAttributes = bytes;
    }

    public Integer getAvailability() {
        return availability == null ? 0 : availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    public int getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(int status) {
        this.instanceStatus = status;
    }

    public boolean getAllAttributes() {
        return allAttributes;
    }

    public void setAllAttributes(boolean allAttributes) {
        this.allAttributes = allAttributes;
    }

    public Boolean getCommitment() {
        return commitment == null ? Boolean.FALSE : commitment;
    }

    public void setCommitment(Boolean commitment) {
        this.commitment = commitment;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Series getSeries() {
        return series;
    }

    public Set<File> getFiles() {
        return files;
    }

    public void setFiles(Set<File> file) {
        this.files = file;
    }

    /**
     * @return the srCode
     */
    public Code getSrCode() {
        return srCode;
    }

    /**
     * @param srCode
     *            the srCode to set
     */
    public void setSrCode(Code srCode) {
        this.srCode = srCode;
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
     * Add the specified AE title to the internal list of retrieve AE titles.
     * 
     * @param aet
     *            The AE title string
     */
    public void addRetrieveAET(String aet) {
        String s = getRetrieveAETs();
        if (s == null) {
            setRetrieveAETs(aet);
        }
        else {
            final Set<String> aetSet = new HashSet<String>(Arrays
                    .asList(StringUtils.split(s, '\\')));
            if (aetSet.add(aet))
                setRetrieveAETs(toString(aetSet));
        }
    }

    /**
     * @return the media
     */
    public Media getMedia() {
        return media;
    }

    /**
     * @param media
     *            the media to set
     */
    public void setMedia(Media media) {
        this.media = media;
    }

    private static String toString(Set<String> s) {
        if (s.isEmpty())
            return null;
        String[] a = s.toArray(new String[s.size()]);
        return StringUtils.toString(a, '\\');
    }

    public boolean updateAvailability(Long key) {
        int avail = Availability.UNAVAILABLE;
        // Media instanceMedia;
        // if ((instanceMedia = getMedia()) != null &&
        // instanceMedia.getMediaStatus() == Media.COMPLETED)
        // avail = Availability.OFFLINE;
        boolean updated = (avail != getAvailability());
        if (updated) {
            setAvailability(avail);
        }
        return updated;
    }

    public Dataset getAttributes(boolean supplement) {
        Dataset ds;
        try {
            ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        }
        catch (IllegalArgumentException x) {
            // BLOB size not sufficient to store Attributes
            logger
                    .warn("Instance (pk:"
                            + getPk()
                            + ") Attributes truncated in database! (BLOB size not sufficient to store Attributes correctly) !");
            ds = DcmObjectFactory.getInstance().newDataset();
            ds.putUI(Tags.SOPInstanceUID, this.getSopIuid());
            ds.putUI(Tags.SOPClassUID, this.getSopCuid());
        }
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putOB(PrivateTags.InstancePk, Convert.toBytes(getPk()
                    .longValue()));
            Media m = getMedia();
            if (m != null && m.getMediaStatus() == MediaDTO.COMPLETED) {
                ds.putSH(Tags.StorageMediaFileSetID, m.getFilesetId());
                ds.putUI(Tags.StorageMediaFileSetUID, m.getFilesetIuid());
            }
            DatasetUtils.putRetrieveAET(ds, getRetrieveAETs(),
                    getExternalRetrieveAET());
            ds.putCS(Tags.InstanceAvailability, Availability
                    .toString(getAvailability()));
        }
        return ds;
    }

    public void setAttributes(Dataset ds) {
        String cuid = ds.getString(Tags.SOPClassUID);
        AttributeFilter filter = AttributeFilter
                .getInstanceAttributeFilter(cuid);
        setAllAttributes(filter.isNoFilter());
        setAttributesInternal(filter.filter(ds), filter.getTransferSyntaxUID());
        int[] fieldTags = filter.getFieldTags();
        for (int i = 0; i < fieldTags.length; i++) {
            setField(filter.getField(fieldTags[i]), ds.getString(fieldTags[i]));
        }
    }

    private void setAttributesInternal(Dataset ds, String tsuid) {
        setSopIuid(ds.getString(Tags.SOPInstanceUID));
        setSopCuid(ds.getString(Tags.SOPClassUID));
        setInstanceNumber(ds.getString(Tags.InstanceNumber));
        try {
            setContentDateTime(ds.getDateTime(Tags.ContentDate,
                    Tags.ContentTime));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Illegal Content Date/Time format: " + e.getMessage());
        }
        setSrCompletionFlag(ds.getString(Tags.CompletionFlag));
        setSrVerificationFlag(ds.getString(Tags.VerificationFlag));
        byte[] b = DatasetUtils.toByteArray(ds, tsuid);
        if (logger.isDebugEnabled()) {
            logger.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    /**
     * @throws DcmServiceException
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

    public String toString() {
        return "Instance[pk=" + getPk() + ", iuid=" + getSopIuid() + ", cuid="
                + getSopCuid() + ", series->" + getSeries() + "]";
    }

    /**
     * @return the verifyingObservers
     */
    public Set<VerifyingObserver> getVerifyingObservers() {
        return verifyingObservers;
    }

    /**
     * @param verifyingObservers
     *            the verifyingObservers to set
     */
    public void setVerifyingObservers(Set<VerifyingObserver> verifyingObservers) {
        this.verifyingObservers = verifyingObservers;
    }

    /**
     * @return the instanceCustomAttribute1
     */
    public String getInstanceCustomAttribute1() {
        return instanceCustomAttribute1;
    }

    /**
     * @param instanceCustomAttribute1
     *            the instanceCustomAttribute1 to set
     */
    public void setInstanceCustomAttribute1(String instanceCustomAttribute1) {
        this.instanceCustomAttribute1 = instanceCustomAttribute1;
    }

    /**
     * @return the instanceCustomAttribute2
     */
    public String getInstanceCustomAttribute2() {
        return instanceCustomAttribute2;
    }

    /**
     * @param instanceCustomAttribute2
     *            the instanceCustomAttribute2 to set
     */
    public void setInstanceCustomAttribute2(String instanceCustomAttribute2) {
        this.instanceCustomAttribute2 = instanceCustomAttribute2;
    }

    /**
     * @return the instanceCustomAttribute3
     */
    public String getInstanceCustomAttribute3() {
        return instanceCustomAttribute3;
    }

    /**
     * @param instanceCustomAttribute3
     *            the instanceCustomAttribute3 to set
     */
    public void setInstanceCustomAttribute3(String instanceCustomAttribute3) {
        this.instanceCustomAttribute3 = instanceCustomAttribute3;
    }

    private void setField(String field, String value) {
        try {
            Method m = Patient.class.getMethod("set"
                    + Character.toUpperCase(field.charAt(0))
                    + field.substring(1), STRING_PARAM);
            m.invoke(this, new Object[] { value });
        }
        catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }
}
