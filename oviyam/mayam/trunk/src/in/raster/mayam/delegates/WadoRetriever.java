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
import in.raster.mayam.form.Thumbnail;
import in.raster.mayam.models.ServerModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class WadoRetriever implements Runnable {

    private String studyUid;
    private ServerModel serverDetails;
    private HttpURLConnection httpURLConnection;
    private HashMap<String, HashSet<String>> instances = null;
    private boolean useTransferSyntax = true;
    private boolean firstSeries = true;
    private String seriesUid = null;
    private Collection<String> instanceList = null;
    private int totalInstances = 0;

    public WadoRetriever(String studyUid, HashMap<String, HashSet<String>> instances, ServerModel serverDetails, boolean useTransferSynatx, boolean showImageView) {
        this.studyUid = studyUid != null ? studyUid : "";
        this.instances = instances;
        this.serverDetails = serverDetails;
        this.useTransferSyntax = useTransferSynatx;
        this.firstSeries = showImageView;
    }

    public WadoRetriever(String studyUid, String seriesUid, Collection<String> instanceList, ServerModel serverDetails, boolean useTransferSyntax, boolean showImageView, int totalInstances) {
        this.studyUid = studyUid != null ? studyUid : "";
        this.seriesUid = seriesUid;
        this.serverDetails = serverDetails;
        this.instanceList = instanceList;
        this.useTransferSyntax = useTransferSyntax;
        this.firstSeries = showImageView;
        this.totalInstances = totalInstances;
    }

    public WadoRetriever(String studyUid, String seriesUid, Collection<String> instanceList, String wadoProtocol, String wadoContext, String hostName, int wadoPort, boolean showImageView, int totalInstances) {
        this.studyUid = studyUid;
        this.seriesUid = seriesUid;
        this.instanceList = instanceList;
        useTransferSyntax = true;
        this.firstSeries = showImageView;
        this.totalInstances = totalInstances;
        serverDetails = new ServerModel();
        serverDetails.setWadoInformation(wadoProtocol, wadoContext, hostName, wadoPort);
    }

    public WadoRetriever() {
    }

    private void doDownloadSeries() {
        Iterator<String> iterator = instanceList.iterator();
        while (iterator.hasNext()) {
            String[] next = iterator.next().split(",");
            retrieve(next[0], next[1]);
        }
        if (firstSeries) {
            ApplicationContext.createCanvas(ApplicationContext.databaseRef.getFirstInstanceLocation(studyUid, seriesUid), studyUid, 0);
        }
        if (totalInstances == ApplicationContext.databaseRef.getStudyLevelInstances(studyUid)) {
            ApplicationContext.studyRetirivalCompleted(studyUid);
        }
        ApplicationContext.databaseRef.update("series", "NoOfSeriesRelatedInstances", ApplicationContext.databaseRef.getSeriesLevelInstance(studyUid, seriesUid), "SeriesInstanceUID", seriesUid);
    }

    public void reteriveVideoAndMultiframes(String studyUid, ServerModel serverDetails, HashSet<String> instances, boolean showImageView) {
        this.useTransferSyntax = false;
        this.serverDetails = serverDetails;
        this.studyUid = studyUid;
        Iterator<String> iterator = instances.iterator();
        while (iterator.hasNext()) {
            String[] next = iterator.next().split(",");
            retrieve(next[0], next[1]);
        }
        if (showImageView) {
            ApplicationContext.createCanvas(ApplicationContext.databaseRef.getFirstInstanceLocation(studyUid), studyUid, 0);
        }
        ApplicationContext.studyRetirivalCompleted(studyUid);
    }

    public void reteriveMultiframeThumbnails(String studyUid, ServerModel serverDetails, HashSet<String> instances) {
        Iterator<String> iterator = instances.iterator();
        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUid + File.separator;
        while (iterator.hasNext()) {
            String[] next = iterator.next().split(",");
            String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyUid + "&seriesUID=" + next[0] + "&objectUID=" + next[1] + "&rows=75" + "&columns=75";
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(wadoRequest).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
                try {
                    httpURLConnection.connect();
                } catch (RuntimeException e) {
                    ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever - Error while querying ", e);
                }
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    //setIcon(new ImageIcon(ImageIO.read(inputStream)));
                    File destination = new File(dest + next[0] + File.separator + "Thumbnails" + File.separator + next[1]);
                    destination.getParentFile().mkdirs();
                    OutputStream outputStream = new FileOutputStream(destination);
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        if (outputStream != null) {
                            outputStream.write(buffer, 0, read);
                        }
                    }
                    inputStream.close();
                    outputStream.close();
                } else {
                    ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever - Response Error ", httpURLConnection.getResponseMessage());
                }
            } catch (IOException ex) {
                ApplicationContext.logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void retrieve(String seriesUid, String iuid) {
        String queryString = null;
        queryString = serverDetails.getWadoProtocol() + "://";
        queryString += serverDetails.getHostName() != null ? serverDetails.getHostName() : "localhost";
        queryString += serverDetails.getWadoPort() != 0 ? ":" + serverDetails.getWadoPort() : ":8080";
        queryString += "/wado?requestType=WADO&contentType=application/dicom&studyUID=" + studyUid;
        queryString += "&seriesUID=" + seriesUid + "&objectUID=" + iuid;
        if (useTransferSyntax) {
            queryString += appendTransferSyntax();
        }
//        //For MAC
//        queryString += "&transferSyntax=" + TransferSyntax.ImplicitVRLittleEndian;
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
                ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever - Error while querying ", e);
            }
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseSuccess(seriesUid, iuid);
            } else {
                ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever - Response Error ", httpURLConnection.getResponseMessage());
            }
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever", ex);
        }
    }

    //For Direct Launch
    public void reterive(String dicom_Req, String study_UID, String ser_UID, String inst_UID) {
        try {
            this.studyUid = study_UID;
            URL wadoUrl = new URL(dicom_Req);
            httpURLConnection = (HttpURLConnection) wadoUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
            try {
                httpURLConnection.connect();
            } catch (RuntimeException e) {
                ApplicationContext.logger.log(Level.SEVERE, "Wado Retriever - Error while querying ", e);
            }
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseSuccess(ser_UID, inst_UID);
            } else {
                ApplicationContext.logger.log(Level.SEVERE, "Wado Retriever - Response Error ", httpURLConnection.getResponseMessage());
            }
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.SEVERE, "Wado Retriever", ex);
        }
    }

    private void responseSuccess(String seriesUid, String iuid) {
        InputStream in = null;
        try {
            OutputStream out = null;
            in = httpURLConnection.getInputStream();
            Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
            String destinationPath = ApplicationContext.listenerDetails[2];
            File struturedDestination = new File(destinationPath + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUid + File.separator + seriesUid);
            struturedDestination.mkdirs();
            File storeLocation = new File(struturedDestination, iuid);
            out = new FileOutputStream(storeLocation);
            copy(in, out);
            ApplicationContext.databaseRef.writeDatasetInfo(new DicomInputStream(storeLocation).readDicomObject(), storeLocation.getAbsolutePath());
        } catch (IOException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "Wado Retriever", ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "Wado Retriever", ex);
            }
        }
    }

    private String appendTransferSyntax() {
        String transferSyntax = "&transferSyntax=";
        if (serverDetails.getRetrieveTransferSyntax() != null && !serverDetails.getRetrieveTransferSyntax().equalsIgnoreCase("")) {
            if (serverDetails.getRetrieveTransferSyntax().equalsIgnoreCase("Explicit VR Little Endian")) {
                transferSyntax += TransferSyntax.ExplicitVRLittleEndian.uid();
                return transferSyntax;
            } else if (serverDetails.getRetrieveTransferSyntax().equalsIgnoreCase("Implicit VR Little Endian")) {
                transferSyntax += TransferSyntax.ImplicitVRLittleEndian.uid();
                return transferSyntax;
            }
        }
        return "";
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

    @Override
    public void run() {
        doDownloadSeries();
    }
}