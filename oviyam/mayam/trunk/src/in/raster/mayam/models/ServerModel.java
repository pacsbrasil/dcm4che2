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
package in.raster.mayam.models;

import org.dcm4che2.data.TransferSyntax;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ServerModel {

    int pk;
    String description = "";
    String aeTitle = "";
    String hostName = "";
    int port;
    String retrieveType = "";
    boolean previewEnabled;
    private int wadoPort;
    private String wadoContextPath = "";
    private String wadoProtocol = "";
    private String retrieveTransferSyntax = "";

    public ServerModel() {
    }

    public ServerModel(String aet, String host, int port) {
        this.aeTitle = aet;
        this.hostName = host;
        this.port = port;
    }

    public ServerModel(String serverName, String aeTitle, String host, int port, String retrieveType, String wadoContext, int wadoPort, String wadoProtocol, String retrieveTS, boolean isPreviewEnabled) {
        this.description = serverName;
        this.hostName = host != null ? host : "localhost";
        this.aeTitle = aeTitle;
        this.port = port;
        this.retrieveType = retrieveType;
        this.previewEnabled = false;
        this.wadoContextPath = wadoContext;
        this.wadoPort = wadoPort != 0 ? wadoPort : 8080;
        this.wadoProtocol = wadoProtocol;
        if (retrieveTS.equals("Explicit VR Little Endian")) {
            this.retrieveTransferSyntax = TransferSyntax.ExplicitVRLittleEndian.uid();
        } else if (retrieveTS.equals("Implicit VR Little Endian")) {
            this.retrieveTransferSyntax = TransferSyntax.ImplicitVRLittleEndian.uid();
        }
        this.previewEnabled = isPreviewEnabled;
    }

    public ServerModel(int pk, String serverName, String aeTitle, String host, int port, String retrieveType, String wadoContext, int wadoPort, String wadoProtocol, String retrieveTS, boolean isPreviewEnabled) {
        this.pk = pk;
        this.description = serverName;
        this.hostName = host;
        this.aeTitle = aeTitle;
        this.port = port;
        this.retrieveType = retrieveType;
        this.previewEnabled = isPreviewEnabled;
        this.wadoContextPath = wadoContext;
        this.wadoPort = wadoPort;
        this.wadoProtocol = wadoProtocol;
        this.retrieveTransferSyntax = retrieveTS;
    }

    public void setWadoInformation(String wadoProtocol, String wadocontext, String hostName, int wadoPort) {
        this.wadoProtocol = wadoProtocol;
        this.wadoContextPath = wadocontext;
        this.hostName = hostName;
        this.wadoPort = wadoPort;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRetrieveTransferSyntax() {
        return retrieveTransferSyntax;
    }

    public void setRetrieveTransferSyntax(String retrieveTransferSyntax) {
        this.retrieveTransferSyntax = retrieveTransferSyntax;
    }

    public String getRetrieveType() {
        return retrieveType;
    }

    public void setRetrieveType(String retrieveType) {
        this.retrieveType = retrieveType;
    }

    public String getWadoURL() {
        return wadoContextPath;
    }

    public void setWadoContextPath(String wadoContextPath) {
        this.wadoContextPath = wadoContextPath;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isPreviewEnabled() {
        return previewEnabled;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        this.previewEnabled = previewEnabled;
    }
}