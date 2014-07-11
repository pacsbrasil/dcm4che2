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
 * Portions created by the Initial Developer are Copyright (C) 2014
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
package in.raster.mayam.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author BabuHussain
 * @version 0.9
 *
 */
public class InputArgumentValues {
    //Remote server related variables

    private String aeTitle;
    private int port;
    private String hostName;
    //Query filter parameter.
    private String patientID;
    private String patientName;
    private String studyUID;
    private String studyDate;
    private String modality;
    private String accessionNumber;
    //Wado related variables
    private int wadoPort;
    private String wadoContext;
    private String wadoProtocol;
    private String from;
    private String to;
    //Added to include C-Move,C-GET Retrieve methods
    private String retrieveType;
    
    //Added to avoid DICOM query in client side
    private String xmlFilePath = null;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getAeTitle() {
        return aeTitle;
    }

    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public String getWadoContext() {
        return wadoContext;
    }

    public void setWadoContext(String wadoContext) {
        this.wadoContext = wadoContext;
    }

    public int getWadoPort() {
        return wadoPort;
    }

    public void setWadoPort(int wadoPort) {
        this.wadoPort = wadoPort;
    }

    public String getWadoProtocol() {
        return wadoProtocol;
    }

    public void setWadoProtocol(String wadoProtocol) {
        this.wadoProtocol = wadoProtocol;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getRetrieveType() {
        return retrieveType;
    }

    public void setRetrieveType(String retrieveType) {
        this.retrieveType = retrieveType;
    }

    public String getSearchDate() {
        String searchDate = "";
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = dateFormat.format(date);
        if (studyDate.equalsIgnoreCase("lastweek")) {
            String lastWeek = getLastWeek();
            lastWeek = lastWeek.replace("-", "");
            todayDate = todayDate.replace("-", "");
            searchDate = lastWeek + "-" + todayDate;
        } else if (studyDate.equalsIgnoreCase("today")) {
            todayDate = todayDate.replace("-", "");
            searchDate = todayDate + "-" + todayDate;
        } else if (studyDate.equalsIgnoreCase("lastmonth")) {
            String lastMonth = getLastMonth();
            lastMonth = lastMonth.replace("-", "");
            todayDate = todayDate.replace("-", "");
            searchDate = lastMonth + "-" + todayDate;
        } else if (studyDate.equalsIgnoreCase("yesterday")) {
            String yesterDay = getYesterday();
            yesterDay = yesterDay.replace("-", "");
            searchDate = yesterDay + "-" + yesterDay;
        } else if (studyDate.equalsIgnoreCase("between")) {
            from = from.replace("/", "");
            to = to.replace("/", "");
            from = from.replace("-", "");
            to = to.replace("-", "");
            searchDate = from + "-" + to;
        } else {
            studyDate = studyDate.replace("/", "");
            studyDate = studyDate.replace("-", "");
            searchDate = studyDate;
        }
        return searchDate;

    }

    public String getLastWeek() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar currDate = Calendar.getInstance();
        currDate.add(Calendar.DATE, -7);
        return dateFormat.format(currDate.getTime());
    }

    public String getLastMonth() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar currDate = Calendar.getInstance();
        currDate.add(Calendar.DATE, -31);
        return dateFormat.format(currDate.getTime());
    }

    public String getYesterday() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar currDate = Calendar.getInstance();
        currDate.add(Calendar.DATE, -1);
        return dateFormat.format(currDate.getTime());
    }   

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public void setXmlFilePath(String xmlFileLocation) {
        this.xmlFilePath = xmlFileLocation;
    }
}