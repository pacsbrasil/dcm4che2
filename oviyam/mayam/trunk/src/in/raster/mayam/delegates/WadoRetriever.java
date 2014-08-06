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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class WadoRetriever implements Runnable {

    private String studyUid;
    private HttpURLConnection httpURLConnection;
    private boolean useTransferSyntax = true;
    private boolean firstSeries = true;
    private String seriesUid = null;
    private Collection<String> instanceList = null;
    private int totalInstances = 0;
    String wadoProtocol, hostName;
    int wadoPort;
    String transferSyntax;
    Vector<Dataset> instanceData = null;

    public WadoRetriever(String studyUid, String seriesUid, Vector<Dataset> instances, boolean showImageView, String wadoProtocol, String hostName, int wadoPort, String transferSyntax, int totalInstances) {
        this.studyUid = studyUid;
        this.seriesUid = seriesUid;
        this.instanceData = instances;
        this.firstSeries = showImageView;
        this.wadoProtocol = wadoProtocol;
        this.hostName = hostName;
        this.wadoPort = wadoPort;
        this.transferSyntax = transferSyntax;
        this.totalInstances = totalInstances;
    }

    public WadoRetriever(String studyUid, Collection<String> instanceList, String wadoProtocol, String wadoContext, String hostName, int wadoPort, boolean showImageView) {
        this.studyUid = studyUid;
        this.instanceList = instanceList;
        useTransferSyntax = true;
        this.firstSeries = showImageView;
        this.totalInstances = totalInstances;
        this.wadoProtocol = wadoProtocol;
        this.wadoPort = wadoPort;
        this.hostName = hostName;
    }

    public WadoRetriever() {
    }

    private void retrieve(String seriesUid, String iuid) {
        String queryString = null;
        queryString = wadoProtocol + "://";
        queryString += hostName;
        queryString += ":" + wadoPort;
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
            ex.printStackTrace();
            ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever", ex);
        }
    }

    private void responseSuccess(String seriesUid, String iuid) {
        InputStream in = null;
        try {
            OutputStream out = null;
            in = httpURLConnection.getInputStream();
            Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
            String destinationPath = ApplicationContext.listenerDetails[2];
            File structuredDestination = new File(destinationPath + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUid + File.separator + seriesUid);
            structuredDestination.mkdirs();
            File storeLocation = new File(structuredDestination, iuid);
            out = new FileOutputStream(storeLocation);
            copy(in, out);
            ApplicationContext.databaseRef.writeDatasetInfo(new DicomInputStream(storeLocation).readDicomObject(), false, storeLocation.getAbsolutePath());
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
//        String transferSyntax = "&transferSyntax=";
//        if (serverDetails.getRetrieveTransferSyntax() != null && !serverDetails.getRetrieveTransferSyntax().equalsIgnoreCase("")) {
//            if (serverDetails.getRetrieveTransferSyntax().equalsIgnoreCase("Explicit VR Little Endian")) {
//                transferSyntax += TransferSyntax.ExplicitVRLittleEndian.uid();
//                return transferSyntax;
//            } else if (serverDetails.getRetrieveTransferSyntax().equalsIgnoreCase("Implicit VR Little Endian")) {
//                transferSyntax += TransferSyntax.ImplicitVRLittleEndian.uid();
//                return transferSyntax;
//            }
//        }
//        return "";
        String transferSyntax1 = "&transferSyntax=";
        if (transferSyntax != null && !transferSyntax.equalsIgnoreCase("")) {
            if (transferSyntax.equalsIgnoreCase("Explicit VR Little Endian")) {
                transferSyntax1 += TransferSyntax.ExplicitVRLittleEndian.uid();
                return transferSyntax1;
            } else if (transferSyntax != null && transferSyntax.equalsIgnoreCase("Implicit VR Little Endian")) {
                transferSyntax += TransferSyntax.ImplicitVRLittleEndian.uid();
                return transferSyntax1;
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
        doDownload();
    }

    private void doDownload() {
        if (instanceData != null) { //Not Direct Launch
            for (int i = 0; i < instanceData.size(); i++) {
                retrieve(instanceData.elementAt(i));
            }
            if (totalInstances == ApplicationContext.databaseRef.getStudyLevelInstances(studyUid)) {
                ApplicationContext.studyRetirivalCompleted(studyUid);
            }
            ApplicationContext.databaseRef.update("series", "NoOfSeriesRelatedInstances", ApplicationContext.databaseRef.getSeriesLevelInstance(studyUid, seriesUid), "SeriesInstanceUID", seriesUid);
        } else { //Direct launch
            Iterator<String> iterator = instanceList.iterator();
            while (iterator.hasNext()) {
                String[] next = iterator.next().split(",");
                this.seriesUid = next[0];
                retrieve(next[0], next[1]);
            }
        }
        if (firstSeries) {
            ApplicationContext.createCanvas(ApplicationContext.databaseRef.getFirstInstanceLocation(studyUid, seriesUid), studyUid, 0);
        }
    }

    public void retrieve(Dataset instance) {
        String queryString = null;
        queryString = wadoProtocol + "://";
        queryString += hostName;
        queryString += ":" + wadoPort;
        queryString += "/wado?requestType=WADO&contentType=application/dicom&studyUID=" + studyUid;
        queryString += "&seriesUID=" + seriesUid + "&objectUID=" + instance.getString(Tags.SOPInstanceUID);

        if (instance.getInteger(Tags.NumberOfFrames) == null) {
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
                responseSuccess(seriesUid, instance.getString(Tags.SOPInstanceUID));
            } else {
                ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever - Response Error ", httpURLConnection.getResponseMessage());
            }
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.SEVERE, "WadoRetriever", ex);
        }
    }

    public void setData(String studyUid, String seriesUid, String wadoProtocol, String hostName, int wadoPort, String transferSyntax) {
        this.studyUid = studyUid;
        this.seriesUid = seriesUid;
        this.wadoProtocol = wadoProtocol;
        this.hostName = hostName;
        this.wadoPort = wadoPort;
        this.transferSyntax = transferSyntax;
    }
}