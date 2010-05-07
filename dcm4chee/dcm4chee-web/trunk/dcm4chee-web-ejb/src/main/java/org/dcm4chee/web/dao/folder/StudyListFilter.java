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

package org.dcm4chee.web.dao.folder;

import java.io.Serializable;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 30, 2008
 */
public class StudyListFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String patientName = "*";
    private String patientID = "*";
    private String issuerOfPatientID = "*";
    private boolean extendedPatQuery;
    private String birthDateMin = "*";
    private String birthDateMax = "*";
    private String accessionNumber = "*";
    private String studyDateMin = "*";
    private String studyDateMax = "*";
    private boolean extendedStudyQuery;
    private String studyInstanceUID = "*";
    private String modality = "*";
    private String sourceAET = "*";
    private boolean patientsWithoutStudies;
    private boolean latestStudiesFirst;

    private static String nullToAsterisk(String s) {
        return s == null ? "*" : s;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = nullToAsterisk(patientName);
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = nullToAsterisk(patientID);
    }

    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = nullToAsterisk(issuerOfPatientID);
    }

    public boolean isExtendedPatQuery() {
        return extendedPatQuery;
    }

    public void setExtendedPatQuery(boolean extendedPatQuery) {
        this.extendedPatQuery = extendedPatQuery;
    }

    public String getBirthDateMin() {
        return birthDateMin;
    }

    public void setBirthDateMin(String birthdateMin) {
        this.birthDateMin = nullToAsterisk(birthdateMin);
    }

    public String getBirthDateMax() {
        return birthDateMax;
    }

    public void setBirthDateMax(String birthdateMax) {
        this.birthDateMax = nullToAsterisk(birthdateMax);
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = nullToAsterisk(accessionNumber);
    }

    public String getStudyDateMin() {
        return studyDateMin;
    }

    public void setStudyDateMin(String studyDate) {
        this.studyDateMin = nullToAsterisk(studyDate);
    }

    public String getStudyDateMax() {
        return studyDateMax;
    }

    public void setStudyDateMax(String studyDate) {
        this.studyDateMax = nullToAsterisk(studyDate);
    }

    public boolean isExtendedStudyQuery() {
        return extendedStudyQuery;
    }

    public void setExtendedStudyQuery(boolean extendedStudyQuery) {
        this.extendedStudyQuery = extendedStudyQuery;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = nullToAsterisk(studyInstanceUID);
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = nullToAsterisk(modality);
    }

    public String getSourceAET() {
        return sourceAET;
    }

    public void setSourceAET(String sourceAET) {
        this.sourceAET = nullToAsterisk(sourceAET);
    }

    public boolean isPatientsWithoutStudies() {
        return patientsWithoutStudies;
    }

    public void setPatientsWithoutStudies(boolean patientsWithoutStudies) {
        this.patientsWithoutStudies = patientsWithoutStudies;
    }

    public boolean isLatestStudiesFirst() {
        return latestStudiesFirst;
    }

    public void setLatestStudiesFirst(boolean latestStudiesFirst) {
        this.latestStudiesFirst = latestStudiesFirst;
    }

}
