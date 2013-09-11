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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.ServerModel;
import in.raster.mayam.param.WadoParam;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class WadoInstanceRetrieveDelegate {

    String studyUid, seriesUid, instanceUid, destinationPath;
    ServerModel serverModel;
    private HttpURLConnection httpURLConnection;
    WadoParam wadoParam = new WadoParam();

    public WadoInstanceRetrieveDelegate(String studyUid, String seriesUid, String instanceUid, ServerModel serverModel) {
        this.studyUid = studyUid;
        this.seriesUid = seriesUid;
        this.instanceUid = instanceUid;
        this.serverModel = serverModel;
        doDownloadInstance();
    }

    private void doDownloadInstance() {
        constructWadoParam();
        String queryString = "";
        if (wadoParam != null) {
            queryString = wadoParam.getWadoUrlJpg();
        }
        try {
            URL wadoUrl = new URL(queryString);
            httpURLConnection = (HttpURLConnection) wadoUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
            try {
                httpURLConnection.connect();
            } catch (RuntimeException e) {
                System.out.println("Error while querying " + e.getMessage());
            }
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseSuccess(wadoParam);
            } else {
                System.out.println("Response Error:" + httpURLConnection.getResponseMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(WadoRetrieveDelegate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void constructWadoParam() {
        if (serverModel.getWadoProtocol().equalsIgnoreCase("https")) {
            wadoParam.setSecureQuery(true);
        } else {
            wadoParam.setSecureQuery(false);
        }
        wadoParam.setAeTitle(serverModel.getAeTitle());
        wadoParam.setRemoteHostName(serverModel.getHostName());
        wadoParam.setRemotePort(serverModel.getWadoPort());
        wadoParam.setStudy(studyUid);
        wadoParam.setSeries(seriesUid);
        wadoParam.setObject(instanceUid);
        wadoParam.setRetrieveTrasferSyntax(serverModel.getRetrieveTransferSyntax());
    }

    private void responseSuccess(WadoParam wadoParam) {
        InputStream in = null;
        try {
            OutputStream out = null;
            in = httpURLConnection.getInputStream();
            Calendar today = Calendar.getInstance();
            destinationPath = ApplicationContext.listenerDetails[2];
            File struturedDestination = new File(destinationPath + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUid + File.separator + seriesUid + File.separator + "Thumbnails");
            String child[] = struturedDestination.list();
            if (child == null) {
                struturedDestination.mkdirs();
            }
            File storeLocation = new File(struturedDestination, wadoParam.getObject());
            out = new FileOutputStream(storeLocation);
            copy(in, out);
        } catch (IOException ex) {
            Logger.getLogger(WadoRetrieveDelegate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(WadoRetrieveDelegate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private synchronized void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[4 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            if (out != null) {
                out.write(buffer, 0, read);
            }
        }
    }
}
