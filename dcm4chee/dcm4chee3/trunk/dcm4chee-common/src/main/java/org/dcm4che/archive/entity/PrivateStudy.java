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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * Persistent entity representing a deleted DICOM study.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "priv_study")
public class PrivateStudy extends EntityBase {

    private static final long serialVersionUID = -6132842219752625784L;

    @Column(name = "priv_type", nullable = false)
    private Integer privateType;

    @Column(name = "study_attrs")
    private byte[] encodedAttributes;

    @Column(name = "study_iuid", nullable = false)
    private String studyIuid;

    @Column(name = "accession_no")
    private String accessionNumber;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.PrivatePatient.class)
    @JoinColumn(name = "patient_fk", nullable = false)
    private PrivatePatient patient;

    @OneToMany(mappedBy = "study")
    private Set<PrivateSeries> series;

    /**
     * 
     */
    public PrivateStudy() {
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
     * @return the patient
     */
    public PrivatePatient getPatient() {
        return patient;
    }

    /**
     * @param patient
     *            the patient to set
     */
    public void setPatient(PrivatePatient patient) {
        this.patient = patient;
    }

    /**
     * @return the privateType
     */
    public Integer getPrivateType() {
        return privateType;
    }

    /**
     * @param privateType
     *            the privateType to set
     */
    public void setPrivateType(Integer privateType) {
        this.privateType = privateType;
    }

    /**
     * @return the series
     */
    public Set<PrivateSeries> getSeries() {
        return series;
    }

    /**
     * @param series
     *            the series to set
     */
    public void setSeries(Set<PrivateSeries> series) {
        this.series = series;
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

    public Dataset getAttributes() {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        return ds;
    }

    public void setAttributes(Dataset ds) {
        setStudyIuid(ds.getString(Tags.StudyInstanceUID));
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        Dataset tmp = ds.excludePrivate();
        setEncodedAttributes(DatasetUtils.toByteArray(tmp));
    }

}
