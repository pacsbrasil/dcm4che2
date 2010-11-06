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

import org.dcm4che2.data.TransferSyntax;

/**
 *
 * @author  BabuHussain
 * @version 0.7
 *
 */
public class WadoParam {

    private String study;
    private String series;
    private String object;
    private String remoteHostName;
    private int remotePort;
    private boolean secureQuery;
    private String aeTitle;
    private String retrieveTrasferSyntax;

    public WadoParam() {
    }   
    public String getWadoUrl() {
        String queryString = "";
        queryString = getProtocol()+appendHostName();
        queryString += remotePort != 0 ? ":" + remotePort : "8080";
        queryString += "/wado?requestType=WADO&studyUID="+appendStudy();
        queryString += "&seriesUID="+appendSeries();
        queryString += "&objectUID="+appendObject();
        queryString += "&contentType=application/dicom"+appendTransferSyntax();
        return queryString;
    }

    private String getProtocol() {
        if (secureQuery) {
            return "https://";
        } else {
            return "http://";
        }
    }

    private String appendHostName() {
        if (remoteHostName != null && !remoteHostName.equalsIgnoreCase("")) {
            return remoteHostName;
        } else {
            return "localhost";
        }
    }

    private String appendStudy() {
        if (study != null && !study.equalsIgnoreCase("")) {
            return study;
        }
        return "";
    }

    private String appendSeries() {
        if (series != null && !series.equalsIgnoreCase("")) {
            return series;
        }
        return "";
    }

    private String appendObject() {
        if (object != null && !object.equalsIgnoreCase("")) {
            return object;
        }
        return "";
    }

    private String appendTransferSyntax() {
        String transferSyntax="&transferSyntax=";
        if (retrieveTrasferSyntax != null && !retrieveTrasferSyntax.equalsIgnoreCase("")) {
            if (retrieveTrasferSyntax.equalsIgnoreCase("Explicit VR Little Endian")) {
                transferSyntax+= TransferSyntax.ExplicitVRLittleEndian.uid();
                return  transferSyntax;
            } else if (retrieveTrasferSyntax.equalsIgnoreCase("Implicit VR Little Endian")) {
                transferSyntax+= TransferSyntax.ImplicitVRLittleEndian.uid();
                return  transferSyntax;
            }
        }
        return "";
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getRemoteHostName() {
        return remoteHostName;
    }

    public void setRemoteHostName(String remoteHostName) {
        this.remoteHostName = remoteHostName;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public boolean isSecureQuery() {
        return secureQuery;
    }

    public void setSecureQuery(boolean secureQuery) {
        this.secureQuery = secureQuery;
    }

    public String getAeTitle() {
        return aeTitle;
    }

    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String getRetrieveTrasferSyntax() {
        return retrieveTrasferSyntax;
    }

    public void setRetrieveTrasferSyntax(String retrieveTrasferSyntax) {
        this.retrieveTrasferSyntax = retrieveTrasferSyntax;
    }
}
