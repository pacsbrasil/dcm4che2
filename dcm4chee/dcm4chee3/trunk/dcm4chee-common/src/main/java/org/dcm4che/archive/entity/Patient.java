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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.archive.common.PrivateTags;
import org.dcm4che.archive.dao.OtherPatientIDDAO;
import org.dcm4che.archive.exceptions.ConfigurationException;
import org.dcm4che.archive.util.AttributeFilter;
import org.dcm4che.archive.util.Convert;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;

/**
 * org.dcm4che.archive.entity.Patient
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "patient")
public class Patient extends EntityBase {

    private static final long serialVersionUID = 7761514302900652394L;

    private static final Logger log = Logger.getLogger(Patient.class.getName());

    private static final int[] OTHER_PID_SQ = { Tags.OtherPatientIDSeq };

    private static final Class[] STRING_PARAM = new Class[] { String.class };

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "pat_id", nullable = false)
    private String patientId;

    @Column(name = "pat_id_issuer")
    private String issuerOfPatientId;

    @Column(name = "pat_name")
    private String patientName;

    @Column(name = "pat_i_name")
    private String patientIdeographicName;

    @Column(name = "pat_p_name")
    private String patientPhoneticName;
    
    @Column(name = "pat_custom1")
    private String patientCustomAttribute1;
    
    @Column(name = "pat_custom2")
    private String patientCustomAttribute2;
    
    @Column(name = "pat_custom3")
    private String patientCustomAttribute3;

    @Column(name = "pat_birthdate")
    private Timestamp patientBirthDate;

    @Column(name = "pat_sex")
    private String patientSex;

    @Column(name = "pat_attrs")
    private byte[] encodedAttributes;

    @OneToMany(mappedBy = "patient")
    private Set<Study> studies;

    @OneToMany(mappedBy = "patient")
    private Set<GPPPS> gppps;

    @OneToMany(mappedBy = "patient")
    private Set<GPSPS> gpsps;

    @OneToMany(mappedBy = "patient")
    private Set<MPPS> mpps;

    @OneToMany(mappedBy = "patient")
    private Set<MWLItem> mwlItems;

    @ManyToOne
    @JoinColumn(name = "merge_fk")
    private Patient mergedWith;

    @OneToMany(mappedBy = "mergedWith")
    private Set<Patient> merged;

    @ManyToMany
    @JoinTable(name = "rel_pat_other_pid", joinColumns = @JoinColumn(name = "patient_fk", referencedColumnName = "pk"), inverseJoinColumns = @JoinColumn(name = "other_pid_fk", referencedColumnName = "pk"))
    private Set<OtherPatientID> otherPatientIds;

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
     * @return the issuerOfPatientId
     */
    public String getIssuerOfPatientId() {
        return issuerOfPatientId;
    }

    /**
     * @param issuerOfPatientId
     *            the issuerOfPatientId to set
     */
    public void setIssuerOfPatientId(String issuerOfPatientId) {
        this.issuerOfPatientId = issuerOfPatientId;
    }

    /**
     * @return the patientBirthDate
     */
    public Timestamp getPatientBirthDate() {
        return patientBirthDate;
    }

    /**
     * @param patientBirthDate
     *            the patientBirthDate to set
     */
    public void setPatientBirthDate(Timestamp patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    /**
     * @return the patientId
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * @param patientId
     *            the patientId to set
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    /**
     * @return the patientIdeographicName
     */
    public String getPatientIdeographicName() {
        return patientIdeographicName;
    }

    /**
     * @param patientIdeographicName
     *            the patientIdeographicName to set
     */
    public void setPatientIdeographicName(String patientIdeographicName) {
        this.patientIdeographicName = patientIdeographicName;
    }

    /**
     * @return the patientName
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * @param patientName
     *            the patientName to set
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /**
     * @return the patientPhoneticName
     */
    public String getPatientPhoneticName() {
        return patientPhoneticName;
    }

    /**
     * @param patientPhoneticName
     *            the patientPhoneticName to set
     */
    public void setPatientPhoneticName(String patientPhoneticName) {
        this.patientPhoneticName = patientPhoneticName;
    }

    /**
     * @return the patientSex
     */
    public String getPatientSex() {
        return patientSex;
    }

    /**
     * @param patientSex
     *            the patientSex to set
     */
    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    /**
     * @return the studies
     */
    public Set<Study> getStudies() {
        return studies;
    }

    /**
     * @param studies
     *            the studies to set
     */
    public void setStudies(Set<Study> studies) {
        this.studies = studies;
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

    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putOB(PrivateTags.PatientPk, Convert
                    .toBytes(getPk().longValue()));
        }
        return ds;
    }

    public void setAttributes(Dataset ds) {
        AttributeFilter filter = AttributeFilter.getPatientAttributeFilter();
        setAttributesInternal(filter.filter(ds), filter.getTransferSyntaxUID());
        int[] fieldTags = filter.getFieldTags();
        for (int i = 0; i < fieldTags.length; i++) {
            setField(filter.getField(fieldTags[i]), ds.getString(fieldTags[i]));
        }
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

    private void setAttributesInternal(Dataset ds, String tsuid) {
        setPatientId(ds.getString(Tags.PatientID));
        setIssuerOfPatientId(ds.getString(Tags.IssuerOfPatientID));
        PersonName pn = ds.getPersonName(Tags.PatientName);
        if (pn != null) {
            setPatientName(toUpperCase(pn.toComponentGroupString(false)));
            PersonName ipn = pn.getIdeographic();
            if (ipn != null) {
                setPatientIdeographicName(ipn.toComponentGroupString(false));
            }
            PersonName ppn = pn.getPhonetic();
            if (ppn != null) {
                setPatientPhoneticName(ppn.toComponentGroupString(false));
            }
        }
        try {
            setPatientBirthDate(ds.getDate(Tags.PatientBirthDate));
        }
        catch (IllegalArgumentException e) {
            log.warn("Illegal Patient Birth Date format: " + e.getMessage());
        }
        setPatientSex(ds.getString(Tags.PatientSex));
        byte[] b = DatasetUtils.toByteArray(ds, tsuid);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

    /**
     * @throws DcmServiceException
     * @throws DAOException
     */
    public void coerceAttributes(Dataset ds, Dataset coercedElements, OtherPatientIDDAO opidDAO)
            throws DcmServiceException {
        Dataset attrs = getAttributes(false);
        boolean b = appendOtherPatientIds(attrs, ds, opidDAO);
        AttributeFilter filter = AttributeFilter.getPatientAttributeFilter();
        AttrUtils.coerceAttributes(attrs, ds, coercedElements, filter, log);
        if (AttrUtils.mergeAttributes(attrs, filter.filter(ds), log) || b) {
            setAttributesInternal(attrs, filter.getTransferSyntaxUID());
        }
    }

    public void updateAttributes(Dataset ds, OtherPatientIDDAO opidDAO) {
        Dataset attrs = getAttributes(false);
        boolean b = appendOtherPatientIds(attrs, ds, opidDAO);
        if (AttrUtils.updateAttributes(attrs, ds.exclude(OTHER_PID_SQ), log)
                || b) {
            setAttributes(attrs);
        }
    }

    private boolean appendOtherPatientIds(Dataset attrs, Dataset ds, OtherPatientIDDAO opidDAO) {
        DcmElement nopidsq = ds.get(Tags.OtherPatientIDSeq);
        if (nopidsq == null || nopidsq.isEmpty() || nopidsq.getItem().isEmpty()) {
            return false;
        }

        boolean update = false;
        DcmElement opidsq = attrs.get(Tags.OtherPatientIDSeq);
        if (opidsq == null) {
            opidsq = attrs.putSQ(Tags.OtherPatientIDSeq);
        }


        for (int n = 0; n < nopidsq.countItems(); n++) {
            Dataset nopid = nopidsq.getItem();
            String pid = nopid.getString(Tags.PatientID);
            String issuer = nopid.getString(Tags.IssuerOfPatientID);
            if (!containsPID(pid, issuer, opidsq)) {
                opidsq.addItem(nopid);
                getOtherPatientIds().add(opidDAO.valueOf(pid, issuer));
                update = true;
                log.info("Add additional Other Patient ID: " + pid + "^^^"
                        + issuer + " to " + toString());
            }
        }
        return update;
    }

    public static boolean containsPID(String pid, String issuer,
            DcmElement opidsq) {
        for (int n = 0; n < opidsq.countItems(); n++) {
            Dataset opid = opidsq.getItem();
            if (opid.getString(Tags.PatientID).equals(pid)
                    && opid.getString(Tags.IssuerOfPatientID).equals(issuer)) {
                return true;
            }
        }
        return false;
    }

    private static String toUpperCase(String s) {
        return s != null ? s.toUpperCase() : null;
    }

    public void setPatientBirthDate(java.util.Date date) {
        setPatientBirthDate(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Patient[pk=" + getPk() + ", pid=" + getPatientId() + ", name="
                + getPatientName() + "]";
    }

    /**
     * @return the gppps
     */
    public Set<GPPPS> getGppps() {
        return gppps;
    }

    /**
     * @param gppps
     *            the gppps to set
     */
    public void setGppps(Set<GPPPS> gppps) {
        this.gppps = gppps;
    }

    /**
     * @return the mpps
     */
    public Set<MPPS> getMpps() {
        return mpps;
    }

    /**
     * @param mpps
     *            the mpps to set
     */
    public void setMpps(Set<MPPS> mpps) {
        this.mpps = mpps;
    }

    /**
     * @return the mwlItems
     */
    public Set<MWLItem> getMwlItems() {
        return mwlItems;
    }

    /**
     * @param mwlItems
     *            the mwlItems to set
     */
    public void setMwlItems(Set<MWLItem> mwlItems) {
        this.mwlItems = mwlItems;
    }

    /**
     * @return the gpsps
     */
    public Set<GPSPS> getGpsps() {
        return gpsps;
    }

    /**
     * @param gpsps
     *            the gpsps to set
     */
    public void setGpsps(Set<GPSPS> gpsps) {
        this.gpsps = gpsps;
    }

    /**
     * @return the mergedWith
     */
    public Patient getMergedWith() {
        return mergedWith;
    }

    /**
     * @param mergedWith
     *            the mergedWith to set
     */
    public void setMergedWith(Patient mergedWith) {
        this.mergedWith = mergedWith;
    }

    /**
     * Get a collection of all patients merged with this patient.
     * 
     * @return Set of patient objects.
     */
    public Set<Patient> getMerged() {
        return merged;
    }

    /**
     * Set a collection of all patients merged with this patient.
     * 
     * @param patients
     *            Set of patient objects.
     */
    public void setMerged(Set<Patient> patients) {
        this.merged = patients;
    }

    /**
     * @return the otherPatientIDs
     */
    public Set<OtherPatientID> getOtherPatientIds() {
        return otherPatientIds;
    }

    /**
     * @param otherPatientIDs
     *            the otherPatientIDs to set
     */
    public void setOtherPatientIds(Set<OtherPatientID> otherPatientIDs) {
        this.otherPatientIds = otherPatientIDs;
    }

    /**
     * @return the patientCustomAttribute1
     */
    public String getPatientCustomAttribute1() {
        return patientCustomAttribute1;
    }

    /**
     * @param patientCustomAttribute1 the patientCustomAttribute1 to set
     */
    public void setPatientCustomAttribute1(String patientCustomAttribute1) {
        this.patientCustomAttribute1 = patientCustomAttribute1;
    }

    /**
     * @return the patientCustomAttribute2
     */
    public String getPatientCustomAttribute2() {
        return patientCustomAttribute2;
    }

    /**
     * @param patientCustomAttribute2 the patientCustomAttribute2 to set
     */
    public void setPatientCustomAttribute2(String patientCustomAttribute2) {
        this.patientCustomAttribute2 = patientCustomAttribute2;
    }

    /**
     * @return the patientCustomAttribute3
     */
    public String getPatientCustomAttribute3() {
        return patientCustomAttribute3;
    }

    /**
     * @param patientCustomAttribute3 the patientCustomAttribute3 to set
     */
    public void setPatientCustomAttribute3(String patientCustomAttribute3) {
        this.patientCustomAttribute3 = patientCustomAttribute3;
    }
}