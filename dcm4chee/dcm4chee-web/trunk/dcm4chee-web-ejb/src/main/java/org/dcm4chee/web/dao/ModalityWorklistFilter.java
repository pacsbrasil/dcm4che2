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

package org.dcm4chee.web.dao;

import java.io.Serializable;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 20.04.2010
 */
public class ModalityWorklistFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String patientName = "*";
    private String patientID = "*";
    private String issuerOfPatientID = "*";
    private String startDate = "*";
    private String accessionNumber = "*";
    private boolean extendedQuery;
    private String studyInstanceUID = "*";
    private String modality = "*";
    private String scheduledStationAET = "*";
    private boolean latestItemsFirst;

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

    public void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = nullToAsterisk(startDate);
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = nullToAsterisk(accessionNumber);
    }

    public void setExtendedQuery(boolean extendedQuery) {
        this.extendedQuery = extendedQuery;
    }

    public boolean isExtendedQuery() {
        return extendedQuery;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = nullToAsterisk(modality);
    }
   
    public boolean isLatestItemsFirst() {
        return latestItemsFirst;
    }

    public void setLatestItemsFirst(boolean latestItemsFirst) {
        this.latestItemsFirst = latestItemsFirst;
    }

    public String getStartDateMin() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStartDateMax() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getBirthDateMin() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getBirthDateMax() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getScheduledStationAET() {
        return this.scheduledStationAET;
    }
    
    public void setScheduledStationAET() {
        this.scheduledStationAET = nullToAsterisk(scheduledStationAET);
    }
}
