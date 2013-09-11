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
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
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
package in.raster.mayam.param;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class QueryParam {

    private String patientId = "";
    private String patientName = "";
    private String birthDate = "";
    private String searchDate = "";
    private String modality = "";
    private String accessionNo = "";
    private String searchTime = "";
    private String studyDescription = "";
    private String referringPhysicianName = "";

    public String getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(String searchTime) {
        this.searchTime = searchTime;
    }
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String s = dateFormat.format(date);

    public QueryParam() {
    }

    /**
     * Setter for property patientId.
     *
     * @param patientId The String object registers the patientId.
     */
    public void setPatientId(String patientId) {
        if (patientId == null) {
            this.patientId = "";
        } else {
            this.patientId = patientId;
        }
    }

    /**
     * Setter for property patientName.
     *
     * @param patientName The String object registers the patientName.
     */
    public void setPatientName(String patientName) {
        if (patientName == null) {
            this.patientName = "";
        } else {
            this.patientName = patientName;
        }
    }

    /**
     * Setter for property birthDate.
     *
     * @param birthDate The String object registers the birthDate.
     */
    public void setBirthDate(String birthDate) {
        if (birthDate == null) {
            this.birthDate = "";
        } else {
            this.birthDate = birthDate;
        }
    }

    /**
     * Setter for property searchDate.
     *
     * @param searchDate The String object registers the searchDate.
     */
    public void setSearchDate(String searchDate) {
        if (searchDate == null) {
            this.searchDate = "";
        } else {
            this.searchDate = searchDate;
        }
    }

    /**
     * Setter for property modality.
     *
     * @param modality The String object registers the modality.
     */
    public void setModality(String modality) {
        if (modality == null) {
            this.modality = "";
        } else {
            this.modality = modality;
        }
    }

    public String getSearchDate() {
        return searchDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getModality() {
        return modality;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getAccessionNo() {
        return accessionNo;
    }

    public void setAccessionNo(String accessionNo) {
        this.accessionNo = accessionNo;
    }

    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }
}
