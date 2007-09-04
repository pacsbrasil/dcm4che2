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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.SeriesRequest
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "series_req")
public class SeriesRequest extends EntityBase {

    private static final long serialVersionUID = 5657492869941909703L;

    @Column(name = "study_iuid")
    private String studyIuid;

    @Column(name = "req_proc_id")
    private String requestedProcedureId;

    @Column(name = "sps_id")
    private String spsId;

    @Column(name = "req_service")
    private String requestingService;

    @Column(name = "req_physician")
    private String requestingPhysician;

    @Column(name = "req_phys_i_name")
    private String requestingPhysicianIdeographicName;

    @Column(name = "req_phys_p_name")
    private String requestingPhysicianPhoneticName;

    @ManyToOne
    @JoinColumn(name = "series_fk")
    private Series series;

    /**
     * 
     */
    public SeriesRequest() {
    }

    /**
     * @return the requestedProcedureId
     */
    public String getRequestedProcedureId() {
        return requestedProcedureId;
    }

    /**
     * @param requestedProcedureId
     *            the requestedProcedureId to set
     */
    public void setRequestedProcedureId(String requestedProcedureId) {
        this.requestedProcedureId = requestedProcedureId;
    }

    /**
     * @return the requestingPhysician
     */
    public String getRequestingPhysician() {
        return requestingPhysician;
    }

    /**
     * @param requestingPhysician
     *            the requestingPhysician to set
     */
    public void setRequestingPhysician(String requestingPhysician) {
        this.requestingPhysician = requestingPhysician;
    }

    /**
     * @return the requestingPhysicianIdeographicName
     */
    public String getRequestingPhysicianIdeographicName() {
        return requestingPhysicianIdeographicName;
    }

    /**
     * @param requestingPhysicianIdeographicName
     *            the requestingPhysicianIdeographicName to set
     */
    public void setRequestingPhysicianIdeographicName(
            String requestingPhysicianIdeographicName) {
        this.requestingPhysicianIdeographicName = requestingPhysicianIdeographicName;
    }

    /**
     * @return the requestingPhysicianPhoneticName
     */
    public String getRequestingPhysicianPhoneticName() {
        return requestingPhysicianPhoneticName;
    }

    /**
     * @param requestingPhysicianPhoneticName
     *            the requestingPhysicianPhoneticName to set
     */
    public void setRequestingPhysicianPhoneticName(
            String requestingPhysicianPhoneticName) {
        this.requestingPhysicianPhoneticName = requestingPhysicianPhoneticName;
    }

    /**
     * @return the requestingService
     */
    public String getRequestingService() {
        return requestingService;
    }

    /**
     * @param requestingService
     *            the requestingService to set
     */
    public void setRequestingService(String requestingService) {
        this.requestingService = requestingService;
    }

    /**
     * @return the spsId
     */
    public String getSpsId() {
        return spsId;
    }

    /**
     * @param spsId
     *            the spsId to set
     */
    public void setSpsId(String spsId) {
        this.spsId = spsId;
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

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder("SeriesRequestAttribute[pk=").append(getPk())
                .append(", rpid=").append(getRequestedProcedureId())
                .append(", spsid=").append(getSpsId())
                .append(", service=").append(getRequestingService())
                .append(", phys=").append(getRequestingPhysician())
                .append(", series->").append(getSeries()).append("]").toString();

    }
}