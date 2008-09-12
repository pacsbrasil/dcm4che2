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
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
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
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.PersonName;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.exceptions.ConfigurationException;
import org.dcm4chee.archive.util.DicomObjectUtils;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 23, 2008
 */
public class Patient implements Serializable {

    private static final long serialVersionUID = -1348274766865261645L;

    private long pk;

    private Date createdTime;

    private Date updatedTime;

    private String patientID;

    private String issuerOfPatientID;

    private String patientName;

    private String patientIdeographicName;

    private String patientPhoneticName;

    private Date patientBirthDate;

    private String patientSex;

    private String patientCustomAttribute1;

    private String patientCustomAttribute2;

    private String patientCustomAttribute3;

    private byte[] encodedAttributes;

    private Set<OtherPatientID> otherPatientIDs;

    private Patient mergedWith;

    private Set<Patient> previous;

    private Set<Study> studies;

    private Set<MWLItem> modalityWorklistItems;

    private Set<MPPS> modalityPerformedProcedureSteps;

    private Set<GPSPS> generalPurposeScheduledProcedureSteps;

    private Set<GPPPS> generalPurposePerformedProcedureSteps;

    public long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getPatientID() {
        return patientID;
    }

    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientIdeographicName() {
        return patientIdeographicName;
    }

    public String getPatientPhoneticName() {
        return patientPhoneticName;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public String getPatientCustomAttribute1() {
        return patientCustomAttribute1;
    }

    public String getPatientCustomAttribute2() {
        return patientCustomAttribute2;
    }

    public String getPatientCustomAttribute3() {
        return patientCustomAttribute3;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public Set<OtherPatientID> getOtherPatientIDs() {
        return otherPatientIDs;
    }

    public void setOtherPatientIDs(Set<OtherPatientID> otherPatientIDs) {
        this.otherPatientIDs = otherPatientIDs;
    }

    public Patient getMergedWith() {
        return mergedWith;
    }

    public void setMergedWith(Patient mergedWith) {
        this.mergedWith = mergedWith;
    }

    public Set<Patient> getPrevious() {
        return previous;
    }

    public void setPrevious(Set<Patient> previous) {
        this.previous = previous;
    }

    public Set<Study> getStudies() {
        return studies;
    }

    public Set<MWLItem> getModalityWorklistItems() {
        return modalityWorklistItems;
    }

    public Set<MPPS> getModalityPerformedProcedureSteps() {
        return modalityPerformedProcedureSteps;
    }

    public Set<GPSPS> getGeneralPurposeScheduledProcedureSteps() {
        return generalPurposeScheduledProcedureSteps;
    }

    public Set<GPPPS> getGeneralPurposePerformedProcedureSteps() {
        return generalPurposePerformedProcedureSteps;
    }

    @Override
    public String toString() {
        return "Patient[pk=" + pk
                + ", pid=" + (issuerOfPatientID != null
                        ? patientID + "^^^" + issuerOfPatientID
                        : patientID)
                + ", otherpids=" + otherPatientIDs
                + ", name=" + patientName
                + ", birthdate=" + patientBirthDate
                + ", sex=" + patientSex
                + (mergedWith != null
                        ? ", MergedWith[pk=" + mergedWith.pk + ", pid="
                                + (mergedWith.issuerOfPatientID != null
                                        ? mergedWith.patientID
                                                + "^^^"
                                                + mergedWith.issuerOfPatientID
                                        : mergedWith.patientID)
                                + ", otherpids=" + mergedWith.otherPatientIDs
                                + ", name=" + mergedWith.patientName
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
        this.patientID = attrs.getString(Tag.PatientID, "");
        this.issuerOfPatientID = attrs.getString(Tag.IssuerOfPatientID, "");
        PersonName pn = new PersonName(attrs.getString(Tag.PatientName));
        this.patientName = pn.componentGroupString(PersonName.SINGLE_BYTE,
                false).toUpperCase();
        this.patientIdeographicName = pn.componentGroupString(
                PersonName.IDEOGRAPHIC, false);
        this.patientPhoneticName = pn.componentGroupString(PersonName.PHONETIC,
                false);
        this.patientBirthDate = attrs.getDate(Tag.PatientBirthDate);
        this.patientSex = attrs.getString(Tag.PatientSex, "");
        AttributeFilter filter = AttributeFilter.getPatientAttributeFilter();
        int[] fieldTags = filter.getFieldTags();
        for (int i = 0; i < fieldTags.length; i++) {
            try {
                Patient.class.getField(filter.getField(fieldTags[i])).set(this,
                        attrs.getString(fieldTags[i], ""));
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }
        }
        this.encodedAttributes = DicomObjectUtils.encode(filter.filter(attrs),
                filter.getTransferSyntaxUID());
    }

}
