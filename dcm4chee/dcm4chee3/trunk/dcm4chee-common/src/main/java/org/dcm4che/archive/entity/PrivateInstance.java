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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

/**
 * Persistent entity representing a deleted DICOM instance.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "priv_instance")
public class PrivateInstance extends EntityBase {
    private static final long serialVersionUID = 4585548270468382557L;

    private static final Logger log = Logger.getLogger(PrivateInstance.class.getName());
    
    @Column(name = "priv_type", nullable = false)
    private Integer privateType;
    
    @Column(name = "sop_iuid", nullable = false)
    private String sopIuid;
    
    @Column(name = "inst_attrs")
    private byte[] encodedAttributes;
    
    @ManyToOne(targetEntity = org.dcm4che.archive.entity.PrivateSeries.class)
    @JoinColumn(name = "series_fk")
    private PrivateSeries series;
    
    @OneToMany(mappedBy = "instance")
    private Set<PrivateFile> files;

    /**
     * 
     */
    public PrivateInstance() {
    }

    /**
     * @return the encodedAttributes
     */
    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    /**
     * @param encodedAttributes the encodedAttributes to set
     */
    public void setEncodedAttributes(byte[] encodedAttributes) {
        this.encodedAttributes = encodedAttributes;
    }

    /**
     * @return the files
     */
    public Set<PrivateFile> getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(Set<PrivateFile> files) {
        this.files = files;
    }

    /**
     * @return the privateType
     */
    public Integer getPrivateType() {
        return privateType;
    }

    /**
     * @param privateType the privateType to set
     */
    public void setPrivateType(Integer privateType) {
        this.privateType = privateType;
    }

    /**
     * @return the series
     */
    public PrivateSeries getSeries() {
        return series;
    }

    /**
     * @param series the series to set
     */
    public void setSeries(PrivateSeries series) {
        this.series = series;
    }

    /**
     * @return the sopIuid
     */
    public String getSopIuid() {
        return sopIuid;
    }

    /**
     * @param sopIuid the sopIuid to set
     */
    public void setSopIuid(String sopIuid) {
        this.sopIuid = sopIuid;
    }
    
    public Dataset getAttributes() {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        return ds;
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSopIuid(ds.getString(Tags.SOPInstanceUID));
        byte[] b = DatasetUtils.toByteArray(ds,
                UIDs.DeflatedExplicitVRLittleEndian);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }

}
