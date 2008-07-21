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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.PersonName;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4chee.archive.common.SPSStatus;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.util.DicomObjectUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 29, 2008
 */
public class MWLItem implements Serializable {

    private static final long serialVersionUID = 5655030469102270878L;

    private long pk;

    private Date createdTime;

    private Date updatedTime;

    private String scheduledProcedureStepID;

    private String requestedProcedureID;

    private String studyInstanceUID;

    private String accessionNumber;

    private String modality;

    private String scheduledStationAET;

    private String scheduledStationName;

    private Date startDateTime;

    private String scheduledPerformingPhysicianName;

    private String scheduledPerformingPhysicianIdeographicName;

    private String scheduledPerformingPhysicianPhoneticName;

    private SPSStatus status;

    private byte[] encodedAttributes;

    private Patient patient;

    public long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getScheduledProcedureStepID() {
        return scheduledProcedureStepID;
    }

    public String getRequestedProcedureID() {
        return requestedProcedureID;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public String getModality() {
        return modality;
    }

    public String getScheduledStationAET() {
        return scheduledStationAET;
    }

    public String getScheduledStationName() {
        return scheduledStationName;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public String getScheduledPerformingPhysicianName() {
        return scheduledPerformingPhysicianName;
    }

    public String getScheduledPerformingPhysicianIdeographicName() {
        return scheduledPerformingPhysicianIdeographicName;
    }

    public String getScheduledPerformingPhysicianPhoneticName() {
        return scheduledPerformingPhysicianPhoneticName;
    }

    public SPSStatus getStatus() {
        return status;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public String toString() {
        return "MWLItem[pk=" + pk
                + ", spsid=" + scheduledProcedureStepID
                + ", rpid=" + requestedProcedureID
                + ", suid=" + studyInstanceUID
                + ", accno=" + accessionNumber
                + ", modality=" + modality
                + ", aet=" + scheduledStationAET
                + ", station=" + scheduledStationName
                + ", performer=" + scheduledPerformingPhysicianName
                + ", start=" + startDateTime
                + ", status=" + status
                + (patient != null 
                        ? ", Patient[pk=" + patient.getPk()
                                + ", pid=" + patient.getPatientID()
                                + (patient.getIssuerOfPatientID() != null
                                        ? "^^^" + patient.getIssuerOfPatientID()
                                        : "")
                                + ", name=" + patient.getPatientName()
                                + "]"
                        : "")
                + "]";
    }

    public void onPrePersist() {
        createdTime = new Date();
    }

    public void onPreUpdate() {
        updatedTime = new Date();
    }

    public DicomObject getAttributes() throws IOException {
        return DicomObjectUtils.decode(encodedAttributes);
    }

    public void setAttributes(DicomObject attrs) {
        DicomObject spsItem = attrs
                .getNestedDicomObject(Tag.ScheduledProcedureStepSequence);
        if (spsItem == null) {
            throw new IllegalArgumentException(
                    "Missing Scheduled Procedure Step Sequence (0040,0100) Item");
        }
        this.scheduledProcedureStepID = spsItem
                .getString(Tag.ScheduledProcedureStepID);
        this.requestedProcedureID = attrs.getString(Tag.RequestedProcedureID);
        this.studyInstanceUID = attrs.getString(Tag.StudyInstanceUID);
        this.accessionNumber = attrs.getString(Tag.AccessionNumber);
        this.scheduledStationAET = spsItem
                .getString(Tag.ScheduledStationAETitle);
        this.scheduledStationName = spsItem.getString(Tag.ScheduledStationName,
                "");
        this.modality = spsItem.getString(Tag.Modality);
        this.startDateTime = spsItem.getDate(
                Tag.ScheduledProcedureStepStartDate,
                Tag.ScheduledProcedureStepStartTime);
        PersonName pn = new PersonName(attrs
                .getString(Tag.ScheduledPerformingPhysicianName));
        this.scheduledPerformingPhysicianName = pn.componentGroupString(
                PersonName.SINGLE_BYTE, false).toUpperCase();
        this.scheduledPerformingPhysicianIdeographicName = pn
                .componentGroupString(PersonName.IDEOGRAPHIC, false);
        this.scheduledPerformingPhysicianPhoneticName = pn
                .componentGroupString(PersonName.PHONETIC, false);
        this.status = SPSStatus.valueOf(spsItem
                .getString(Tag.ScheduledProcedureStepStatus));
        this.encodedAttributes = DicomObjectUtils.encode(AttributeFilter
                .exludePatientAttributes(attrs),
                UID.DeflatedExplicitVRLittleEndian);
    }
}
