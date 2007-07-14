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
import org.dcm4che.archive.common.PrivateTags;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * Persistent entity representing a deleted DICOM series.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "priv_series")
public class PrivateSeries extends EntityBase {
    private static final long serialVersionUID = -5632657266056795671L;

    @Column(name = "priv_type", nullable = false)
    private Integer privateType;

    @Column(name = "series_attrs")
    private byte[] encodedAttributes;

    @Column(name = "series_iuid", nullable = false)
    private String seriesIuid;

    @Column(name = "src_aet")
    private String sourceAET;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.PrivateStudy.class)
    @JoinColumn(name = "study_fk", nullable = false)
    private PrivateStudy study;

    @OneToMany(mappedBy = "series")
    private Set<PrivateInstance> instances;

    /**
     * 
     */
    public PrivateSeries() {
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
    public Set<PrivateInstance> getInstances() {
        return instances;
    }

    /**
     * @param instances
     *            the instances to set
     */
    public void setInstances(Set<PrivateInstance> instances) {
        this.instances = instances;
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
     * Get the DICOM UID of this deleted series.
     * 
     * @return the seriesIuid
     */
    public String getSeriesIuid() {
        return seriesIuid;
    }

    /**
     * Set the DICOM UID of this deleted series.
     * 
     * @param seriesIuid
     *            the seriesIuid to set
     */
    public void setSeriesIuid(String seriesIuid) {
        this.seriesIuid = seriesIuid;
    }

    /**
     * Get the AET that this deleted series came from.
     * 
     * @return the sourceAET
     */
    public String getSourceAET() {
        return sourceAET;
    }

    /**
     * Set the AET that this deleted series came from.
     * 
     * @param sourceAET
     *            the sourceAET to set
     */
    public void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET;
    }

    /**
     * Get the study to which this deleted series belongs.
     * 
     * @return the study
     */
    public PrivateStudy getStudy() {
        return study;
    }

    /**
     * Set the study which this deleted series belongs to.
     * 
     * @param study
     *            the study to set
     */
    public void setStudy(PrivateStudy study) {
        this.study = study;
    }

    /**
     * Get the encoded attributes of this deleted series as a DICOM Dataset.
     * 
     * @return {@link Dataset}
     */
    public Dataset getAttributes() {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        ds.putAE(PrivateTags.CallingAET, getSourceAET());
        return ds;
    }

    /**
     * Set the encoded attributes of this deleted series as a DICOM Dataset.
     * 
     * @param ds
     *            {@link Dataset}
     */
    public void setAttributes(Dataset ds) {
        setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        setSourceAET(ds.getString(PrivateTags.CallingAET));
        Dataset tmp = ds.excludePrivate();
        setEncodedAttributes(DatasetUtils.toByteArray(tmp));
    }

}
