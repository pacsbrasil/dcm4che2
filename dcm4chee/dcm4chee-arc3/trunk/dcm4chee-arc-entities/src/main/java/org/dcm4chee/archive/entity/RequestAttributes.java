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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chee.archive.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.PersonName;
import org.dcm4che2.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 29, 2008
 */
@Entity
@EntityListeners( { EntityLogger.class })
@Table(name = "series_req")
public class RequestAttributes implements Serializable {

    private static final long serialVersionUID = -5693026277386978780L;

    @Id
    @GeneratedValue
    @Column(name = "pk")
    private long pk;

    @Column(name = "study_iuid")
    private String studyInstanceUID;

    @Column(name = "req_proc_id")
    private String requestedProcedureID;

    @Column(name = "sps_id")
    private String scheduledProcedureStepID;

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

    public long getPk() {
        return pk;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getRequestedProcedureID() {
        return requestedProcedureID;
    }

    public String getScheduledProcedureStepID() {
        return scheduledProcedureStepID;
    }

    public String getRequestingService() {
        return requestingService;
    }

    public String getRequestingPhysician() {
        return requestingPhysician;
    }

    public String getRequestingPhysicianIdeographicName() {
        return requestingPhysicianIdeographicName;
    }

    public String getRequestingPhysicianPhoneticName() {
        return requestingPhysicianPhoneticName;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return "RequestAttributes[pk=" + getPk() + ", suid=" + studyInstanceUID
                + ", rpid=" + requestedProcedureID + ", spsid="
                + scheduledProcedureStepID + "]->" + series;
    }

    public void setAttributes(DicomObject attrs) {
        this.studyInstanceUID = attrs.getString(Tag.StudyInstanceUID, "");
        this.requestedProcedureID = attrs.getString(Tag.RequestedProcedureID,
                "");
        this.scheduledProcedureStepID = attrs.getString(
                Tag.ScheduledProcedureStepID, "");
        this.requestingService = attrs.getString(Tag.RequestingService, "");
        PersonName pn = new PersonName(attrs.getString(Tag.RequestingPhysician));
        this.requestingPhysician = pn.componentGroupString(
                PersonName.SINGLE_BYTE, false).toUpperCase();
        this.requestingPhysicianIdeographicName = pn.componentGroupString(
                PersonName.IDEOGRAPHIC, false);
        this.requestingPhysicianPhoneticName = pn.componentGroupString(
                PersonName.PHONETIC, false);
    }

}
