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
import in.raster.mayam.form.VideoPanel;
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
import java.util.logging.Logger;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.UID;
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

    public WadoRetriever(String studyUid, HashMap<String, HashSet<String>> instances, ServerModel serverDetails, boolean useTransferSynatx, boolean showImageView) {
        this.studyUid = studyUid;
        this.instances = instances;
        this.serverDetails = serverDetails;
        studyUid = studyUid != null ? studyUid : "";
        this.useTransferSyntax = useTransferSynatx;
        this.firstSeries = showImageView;
    }

    private void doDownloadSeries() {
//        boolean firstSeries = true, isVideo = false;
        boolean isVideo = false;
        String firstInstanceWithSOPCls = null;
        Iterator<String> iterator = instances.keySet().iterator();
        while (iterator.hasNext()) {
            String seriesUid = iterator.next();
            Iterator<String> iterator1 = instances.get(seriesUid).iterator();
            while (iterator1.hasNext()) {
                String next = iterator1.next();
                retrieve(next.split(",")[0], next.split(",")[1]);
            }
            if (firstSeries) {
                firstInstanceWithSOPCls = ApplicationContext.databaseRef.getFirstInstanceWithSOPCls(studyUid, seriesUid);
                String sopCls = firstInstanceWithSOPCls.split(",")[1];
                if (sopCls.equals(UID.VideoEndoscopicImageStorage) || sopCls.equals(UID.VideoMicroscopicImageStorage) || sopCls.equals(UID.VideoPhotographicImageStorage)) {
                    isVideo = true;
                    ApplicationContext.createVideoCanvas(studyUid, firstInstanceWithSOPCls.split(",")[0]);
                } else {
                    ApplicationContext.createLayeredCanvas(ApplicationContext.databaseRef.getFileLocation(firstInstanceWithSOPCls.split(",")[0]), studyUid, 0, true);
                }
                firstSeries = false;
            }
            ApplicationContext.displayPreview(studyUid, seriesUid);
        }
        ApplicationContext.createVideoPreviews(studyUid);
        showSeries();
        if (isVideo) {
            ((VideoPanel) ApplicationContext.selectedPanel).playMedia(ApplicationContext.databaseRef.getFileLocation(firstInstanceWithSOPCls.split(",")[0]));
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
                System.out.println("Response Error:" + httpURLConnection.getResponseMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(ThumbnailWadoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void responseSuccess(String seriesUid, String iuid) {
        InputStream in = null;
        try {
            OutputStream out = null;
            in = httpURLConnection.getInputStream();
            Calendar today = Calendar.getInstance();
            String destinationPath = ApplicationContext.listenerDetails[2];
            File struturedDestination = new File(destinationPath + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUid + File.separator + seriesUid);
            struturedDestination.mkdirs();
            File storeLocation = new File(struturedDestination, iuid);
            out = new FileOutputStream(storeLocation);
            copy(in, out);
            ApplicationContext.databaseRef.writeDatasetInfo(new DicomInputStream(storeLocation).readDicomObject(), false, storeLocation.getAbsolutePath(), false);
        } catch (IOException ex) {
            Logger.getLogger(WadoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(WadoRetriever.class.getName()).log(Level.SEVERE, null, ex);
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

    private void showSeries() {
        ApplicationContext.databaseRef.update("study", "DownloadStatus", true, "StudyInstanceUID", studyUid);
        ApplicationContext.imgView.getImageToolbar().enableMultiSeriesTools();
        ApplicationContext.databaseRef.updateStudies(studyUid);
        if (ApplicationContext.mainScreenObj != null && !ApplicationContext.databaseRef.isDownloadPending()) {
            ApplicationContext.mainScreenObj.hideProgressBar();
        }
        ApplicationContext.setCorrespondingPreviews();
        ApplicationContext.setAllSeriesIdentification(studyUid);
    }
}
