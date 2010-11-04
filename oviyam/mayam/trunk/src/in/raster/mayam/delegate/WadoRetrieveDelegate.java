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
package in.raster.mayam.delegate;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.model.ServerModel;
import in.raster.mayam.param.WadoParam;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author  BabuHussain
 * @version 0.7
 *
 */
public class WadoRetrieveDelegate extends Thread {

    private Vector<WadoParam> wadoUrls;
    private HttpURLConnection httpURLConnection;
    private String studyUID;
    private String serverName;
    private String patientID;
    private String destinationPath;

    public WadoRetrieveDelegate() {
        this.wadoUrls = new Vector();
    }
    public void run()
    {
        getWadoURLList();
        doDownloadStudy();
    }

    public void retrieveStudy(String serverName, String patientID, String studyInstanceUID) {
        this.serverName = serverName;
        this.patientID = patientID;
        this.studyUID = studyInstanceUID;
        this.start();
    }

    private void getWadoURLList() {
        String seriesInstanceUID;
        String instanceUID = "";
        if (wadoUrls != null) {
            wadoUrls.clear();
        }
        ServerModel serverModel = ApplicationContext.databaseRef.getServerModel(serverName);
        DcmURL url = new DcmURL("dicom://" + serverModel.getAeTitle() + "@" + serverModel.getHostName() + ":" + serverModel.getPort());
        QuerySeriesService querySeriesService = new QuerySeriesService();
        querySeriesService.callFindWithQuery(patientID, studyUID, url);
        for (int dataSetCount = 0; dataSetCount < querySeriesService.getDatasetVector().size(); dataSetCount++) {
            try {
                Dataset dataSet = (Dataset) querySeriesService.getDatasetVector().elementAt(dataSetCount);
                seriesInstanceUID = dataSet.getString(Tags.SeriesInstanceUID) != null ? dataSet.getString(Tags.SeriesInstanceUID) : "";
                QueryInstanceService queryInstanceService = new QueryInstanceService();
                queryInstanceService.callFindWithQuery(patientID, studyUID, seriesInstanceUID, url);                
                for (int instanceCount = 0; instanceCount < queryInstanceService.getDatasetVector().size(); instanceCount++) {
                    Dataset instanceDataset=(Dataset) queryInstanceService.getDatasetVector().elementAt(instanceCount);
                    instanceUID = instanceDataset.getString(Tags.SOPInstanceUID) != null ? instanceDataset.getString(Tags.SOPInstanceUID) : "";
                    WadoParam wadoParam = getWadoParam(serverModel.getWadoProtocol(),serverModel.getAeTitle(), serverModel.getHostName(), serverModel.getWadoPort(), studyUID, seriesInstanceUID, instanceUID,serverModel.getRetrieveTransferSyntax());
                    wadoUrls.add(wadoParam);
                }
                
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private WadoParam getWadoParam(String wadoProtocol,String aeTitle, String hostName, int port, String studyUID, String seriesUID, String instanceUID,String retrieveTransferSyntax) {
        WadoParam wadoParam = new WadoParam();       
        if(wadoProtocol.equalsIgnoreCase("https"))
        wadoParam.setSecureQuery(true);
        else
            wadoParam.setSecureQuery(false);
        wadoParam.setAeTitle(aeTitle);
        wadoParam.setRemoteHostName(hostName);
        wadoParam.setRemotePort(port);
        wadoParam.setStudy(studyUID);
        wadoParam.setSeries(seriesUID);      
        wadoParam.setObject(instanceUID);
        wadoParam.setRetrieveTrasferSyntax(retrieveTransferSyntax);
        return wadoParam;
    }

    public void doDownloadStudy() {
        for (WadoParam wadoParam : wadoUrls) {
            String queryString = "";
            if (wadoParam != null) {
                queryString = wadoParam.getWadoUrl();               
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
    }

    private void responseSuccess(WadoParam wadoParam) {

        InputStream in = null;
        try {
            OutputStream out = null;
            in = httpURLConnection.getInputStream();
            Calendar today = Calendar.getInstance();
            setDestination();
            File struturedDestination = new File(destinationPath + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUID);
            String child[] = struturedDestination.list();
            if (child == null) {
                struturedDestination.mkdirs();
            }
            File storeLocation = new File(struturedDestination, wadoParam.getObject());
            out = new FileOutputStream(storeLocation);
            copy(in, out);
            NetworkQueueUpdateDelegate networkQueueUpdateDelegate = new NetworkQueueUpdateDelegate();
            networkQueueUpdateDelegate.updateReceiveTable(storeLocation, wadoParam.getAeTitle());
            if (!MainScreen.sndRcvFrm.isVisible()) {
                MainScreen.sndRcvFrm.setVisible(true);
            }
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

    public void setDestination() {
        String storageLocation = ApplicationContext.databaseRef.getListenerDetails()[2];
        if (!ApplicationContext.canWrite(System.getProperty("user.dir"))) {
            destinationPath = System.getProperty("java.io.tmpdir") + File.separator + storageLocation;
        } else {
            destinationPath = System.getProperty("user.dir") + File.separator + storageLocation;
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
