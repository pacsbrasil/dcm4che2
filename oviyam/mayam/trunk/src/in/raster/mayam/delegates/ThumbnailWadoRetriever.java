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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.ServerModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class ThumbnailWadoRetriever implements Runnable {

    String studyUid;
    ServerModel serverDetails;
    private HttpURLConnection httpURLConnection;
    HashMap<String, HashSet<String>> instances = null;

    public ThumbnailWadoRetriever(String studyUid, HashMap<String, HashSet<String>> instances, ServerModel serverDetails) {
        this.studyUid = studyUid;
        this.instances = instances;
        this.serverDetails = serverDetails;
        studyUid = studyUid != null ? studyUid : "";
    }

    public ThumbnailWadoRetriever(String studyUid, ServerModel serverDetails) {
        this.studyUid = studyUid;
        this.serverDetails = serverDetails;
    }

    public ThumbnailWadoRetriever() {
    }

    private void doDownload() {
        Iterator<String> iterator = instances.keySet().iterator();
        while (iterator.hasNext()) {
            Iterator<String> iterator1 = instances.get(iterator.next()).iterator();
            while (iterator1.hasNext()) {
                String next = iterator1.next();
                retrieve(next.split(",")[0], next.split(",")[1]);
            }
        }
    }

    private void retrieve(String seriesUid, String iuid) {
        String queryString = null;
        queryString = serverDetails.getWadoProtocol() + "://";
        queryString += serverDetails.getHostName() != null ? serverDetails.getHostName() : "localhost";
        queryString += serverDetails.getWadoPort() != 0 ? ":" + serverDetails.getWadoPort() : ":8080";
        queryString += "/wado?requestType=WADO&studyUID=" + studyUid;
        queryString += "&seriesUID=" + seriesUid + "&objectUID=" + iuid;
        queryString += "&rows=" + 75;
        queryString += "&columns=" + 75;
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
                responseSuccess(seriesUid, iuid);
            } else {
                ApplicationContext.logger.log(Level.WARNING, "Response Error : " + httpURLConnection.getResponseMessage());
            }
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.SEVERE, null, ex);
        }
    }

    public void retrieveThumbnail(String thumbnailReq, String study_UID, String ser_UID, String inst_UID) {
        try {
            this.studyUid = study_UID;
            URL wadoUrl = new URL(thumbnailReq);
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
                responseSuccess(ser_UID, inst_UID);
            } else {
                ApplicationContext.logger.log(Level.WARNING, "Response Error : " + httpURLConnection.getResponseMessage());
            }
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.SEVERE, null, ex);
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

    private void responseSuccess(String seriesUid, String iuid) {
        InputStream in = null;
        try {
            OutputStream out = null;
            in = httpURLConnection.getInputStream();
            Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
            String destinationPath = ApplicationContext.listenerDetails[2];
            File struturedDestination = new File(destinationPath + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUid + File.separator + seriesUid + File.separator + "Thumbnails");
            String child[] = struturedDestination.list();
            if (child == null) {
                struturedDestination.mkdirs();
            }
            File storeLocation = new File(struturedDestination, iuid);
            out = new FileOutputStream(storeLocation);
            copy(in, out);
        } catch (IOException ex) {
            ApplicationContext.logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                ApplicationContext.logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void run() {
        doDownload();
    }

    public void retrieveThumbnail(String iuid) {
        retrieve(iuid.split(",")[0], iuid.split(",")[1]);
    }
}