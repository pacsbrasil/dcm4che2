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

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * Entity representing a DICOM device.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "device")
public class Device extends EntityBase {
    private static final long serialVersionUID = -87880548682758215L;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "station_aet", nullable = false)
    private String stationAET;

    @Column(name = "modality", nullable = false)
    private String modality;

    @ManyToMany
    @JoinTable(
            name="rel_dev_proto",
            joinColumns=
                @JoinColumn(name="prcode_fk", referencedColumnName="pk"),
            inverseJoinColumns=
                @JoinColumn(name="device_fk", referencedColumnName="pk")
        )
    private Collection<Code> protocolCodes;

    /**
     * 
     */
    public Device() {
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
     * @return the protocolCodes
     */
    public Collection<Code> getProtocolCodes() {
        return protocolCodes;
    }

    /**
     * @param protocolCodes
     *            the protocolCodes to set
     */
    public void setProtocolCodes(Collection<Code> protocolCodes) {
        this.protocolCodes = protocolCodes;
    }

    /**
     * @return the stationAET
     */
    public String getStationAET() {
        return stationAET;
    }

    /**
     * @param stationAET
     *            the stationAET to set
     */
    public void setStationAET(String stationAET) {
        this.stationAET = stationAET;
    }

    /**
     * @return the stationName
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * @param stationName
     *            the stationName to set
     */
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

}
