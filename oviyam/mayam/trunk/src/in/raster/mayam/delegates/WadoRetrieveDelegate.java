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
 * Devihree V
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
import in.raster.mayam.facade.ApplicationFacade;
import in.raster.mayam.models.InputArgumentValues;
import in.raster.mayam.models.Series;
import in.raster.mayam.models.ServerModel;
import in.raster.mayam.param.WadoParam;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.UID;
import org.dcm4che2.tool.dcm2xml.Dcm2Xml;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author BabuHussain
 * @version 0.7
 *
 */
public class WadoRetrieveDelegate extends Thread {

    private Vector<WadoParam> wadoUrls;
    private HttpURLConnection httpURLConnection;
    private String studyUID, serverName, patientID, destinationPath, seriesUid, patientName, studyDesc, studyDate, studyTime;
    private ServerModel serverModel = null;
    InfoUpdateDelegate infoUpdateDelegate = new InfoUpdateDelegate();
    int numberOfStudyRelatedInstances = 0;
    String[] patientInfo;
    ArrayList<Series> seriesList = null;
    //To format the display of date
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    DateFormat timeFormat = new SimpleDateFormat("hhmmss");
    DateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    DateFormat displayTimeFormat = new SimpleDateFormat("hh:mm:ss");

    public WadoRetrieveDelegate() {
        this.wadoUrls = new Vector();
    }

    @Override
    public void run() {
        getWadoURLList();
        doDownloadStudy();
        updateDatabase();
    }

    public void retrieveStudy(String serverName, String patientID, String studyInstanceUID, int totalInstances, String[] patientInfo) {
        this.serverName = serverName;
        this.patientID = patientID;
        this.studyUID = studyInstanceUID;
        this.patientInfo = patientInfo;
        this.numberOfStudyRelatedInstances = totalInstances;
        ApplicationContext.isJnlp = false;
        this.start();
    }

    public void retrieveStudy(ServerModel serverModel) {
        this.serverModel = serverModel;
        this.patientInfo = new String[5];
        InputArgumentValues inputArgumentValues = null;
        if (serverModel.getAeTitle() != null) {
            inputArgumentValues = InputArgumentsParser.inputArgumentValues;
            DcmURL dcmurl = new DcmURL("dicom://" + inputArgumentValues.getAeTitle() + "@" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getPort());
            QueryService queryService = new QueryService();
            queryService.callFindWithQuery(inputArgumentValues.getPatientID(), inputArgumentValues.getPatientName(), "", inputArgumentValues.getSearchDate(), inputArgumentValues.getModality(), "", inputArgumentValues.getAccessionNumber(), "", "", inputArgumentValues.getStudyUID(), dcmurl);
            if (queryService.getDatasetVector().size() == 0) {
                System.err.println("No Matching Studies Found");
                System.exit(0);
            }
            for (int dataSetCount = 0; dataSetCount < queryService.getDatasetVector().size(); dataSetCount++) {
                try {
                    Dataset dataSet = (Dataset) queryService.getDatasetVector().elementAt(dataSetCount);
                    this.patientID = dataSet.getString(Tags.PatientID) != null ? dataSet.getString(Tags.PatientID) : "";
                    this.studyUID = dataSet.getString(Tags.StudyInstanceUID) != null ? dataSet.getString(Tags.StudyInstanceUID) : "";
                    this.numberOfStudyRelatedInstances = dataSet.getInteger(Tags.NumberOfStudyRelatedInstances);
                    this.patientName = dataSet.getString(Tags.PatientName);
                    this.studyDesc = dataSet.getString(Tags.StudyDescription);
                    this.studyDate = dataSet.getString(Tags.StudyDate);
                    this.studyTime = dataSet.getString(Tags.StudyTime);
                    getWadoURLList();
                    doDownloadStudy();
                    updateDatabase();
                } catch (Exception e) {
                    System.out.println("Exeption in retrieve study(Server Model) " + e.getMessage());
                }
            }
        }
    }

    private void getWadoURLList() {
        String seriesInstanceUID;
        String instanceUID = "";
        seriesList = new ArrayList<Series>();
        if (wadoUrls != null) {
            wadoUrls.clear();
        }
        if (serverModel == null) {
            serverModel = ApplicationContext.databaseRef.getServerDetails(serverName);
        }
        DcmURL url = new DcmURL("dicom://" + serverModel.getAeTitle() + "@" + serverModel.getHostName() + ":" + serverModel.getPort());
        QuerySeriesService querySeriesService = new QuerySeriesService();
        if (patientID != null || studyUID != null) {
            querySeriesService.callFindWithQuery(patientID, studyUID, url);
        }
        for (int dataSetCount = 0; dataSetCount < querySeriesService.getDatasetVector().size(); dataSetCount++) {
            try {
                Dataset dataSet = (Dataset) querySeriesService.getDatasetVector().elementAt(dataSetCount);
                Series series = new Series(dataSet);
                seriesInstanceUID = dataSet.getString(Tags.SeriesInstanceUID) != null ? dataSet.getString(Tags.SeriesInstanceUID) : "";
                seriesList.add(series);
                QueryInstanceService queryInstanceService = new QueryInstanceService();
                queryInstanceService.callFindWithQuery(patientID, studyUID, seriesInstanceUID, url);
                for (int instanceCount = 0; instanceCount < queryInstanceService.getDatasetVector().size(); instanceCount++) {
                    Dataset instanceDataset = (Dataset) queryInstanceService.getDatasetVector().elementAt(instanceCount);
                    instanceUID = instanceDataset.getString(Tags.SOPInstanceUID) != null ? instanceDataset.getString(Tags.SOPInstanceUID) : "";
                    if (!instanceDataset.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) && !instanceDataset.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) && !instanceDataset.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) {
                        WadoParam wadoParam = getWadoParam(serverModel.getWadoProtocol(), serverModel.getAeTitle(), serverModel.getHostName(), serverModel.getWadoPort(), studyUID, seriesInstanceUID, instanceUID, serverModel.getRetrieveTransferSyntax(), false);
                        wadoUrls.add(wadoParam);
                    } else {
                        series.setVideoStatus(true);
                        WadoParam wadoParam = getWadoParam(serverModel.getWadoProtocol(), serverModel.getAeTitle(), serverModel.getHostName(), serverModel.getWadoPort(), studyUID, seriesInstanceUID, instanceUID, serverModel.getRetrieveTransferSyntax(), true);
                        wadoUrls.add(wadoParam);
                    }

                }

            } catch (Exception e) {
                System.out.println("Exception in getting url list : " + e.getMessage());
            }
        }
    }

    private WadoParam getWadoParam(String wadoProtocol, String aeTitle, String hostName, int port, String studyUID, String seriesUID, String instanceUID, String retrieveTransferSyntax, boolean useDefault_TS) {
        WadoParam wadoParam = new WadoParam();
        if (wadoProtocol.equalsIgnoreCase("https")) {
            wadoParam.setSecureQuery(true);
        } else {
            wadoParam.setSecureQuery(false);
        }
        wadoParam.setAeTitle(aeTitle);
        wadoParam.setRemoteHostName(hostName);
        wadoParam.setRemotePort(port);
        wadoParam.setStudy(studyUID);
        wadoParam.setSeries(seriesUID);
        wadoParam.setObject(instanceUID);
        if (!useDefault_TS) {
            wadoParam.setRetrieveTrasferSyntax(retrieveTransferSyntax);
        } else {
            wadoParam.setRetrieveTrasferSyntax("");
        }
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
            destinationPath = ApplicationContext.listenerDetails[2];
            File struturedDestination = new File(destinationPath + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyUID + File.separator + wadoParam.getSeries());
            String child[] = struturedDestination.list();
            if (child == null) {
                struturedDestination.mkdirs();
            }
            File storeLocation = new File(struturedDestination, wadoParam.getObject());
            out = new FileOutputStream(storeLocation);
            copy(in, out);
            infoUpdateDelegate.updateFileDetails(storeLocation);
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

    private void updateDatabase() {
        sortSeries();
        ApplicationContext.databaseRef.update("study", "NoOfSeries", seriesList.size(), "StudyInstanceUID", studyUID);
        ApplicationContext.databaseRef.update("study", "NoOfInstances", numberOfStudyRelatedInstances, "StudyInstanceUID", studyUID);
        for (int i = 0; i < seriesList.size(); i++) {
            if (!seriesList.get(i).isVideo()) {
                ApplicationContext.databaseRef.update("series", "NoOfSeriesRelatedInstances", seriesList.get(i).getSeriesRelatedInstance(), "SeriesInstanceUID", seriesList.get(i).getSeriesInstanceUID());
                ConstructThumbnails constructThumbnails = new ConstructThumbnails(studyUID, seriesList.get(i).getSeriesInstanceUID());
                ApplicationContext.mainScreenObj.increaseProgressValue();
            } else {
                ArrayList<String> instancesLocation = ApplicationContext.databaseRef.getInstancesLocation(studyUID, seriesList.get(i).getSeriesInstanceUID());
                for (int j = 0; j < instancesLocation.size(); j++) {
                    if (!instancesLocation.get(j).contains("V")) {
                        File videoFile = new File(instancesLocation.get(j) + "_V" + File.separator + "video.xml");
                        videoFile.getParentFile().mkdirs();
                        try {
                            videoFile.createNewFile();
                        } catch (IOException ex) {
                            Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Dcm2Xml.main(new String[]{instancesLocation.get(j), "-X", "-o", videoFile.getAbsolutePath()});
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder;
                        try {
                            dBuilder = dbFactory.newDocumentBuilder();
                            Document doc = dBuilder.parse(instancesLocation.get(j) + "_V" + File.separator + "video.xml");
                            NodeList elementsByTagName1 = doc.getElementsByTagName("item");
                            for (int k = 0; k < elementsByTagName1.getLength(); k++) {
                                Node item = elementsByTagName1.item(k);
                                NamedNodeMap attributes = item.getAttributes();
                                if (attributes.getNamedItem("src") != null) {
                                    Node namedItem = attributes.getNamedItem("src");
                                    videoFile = new File(instancesLocation.get(j) + "_V" + File.separator + namedItem.getNodeValue());
                                    videoFile.renameTo(new File(videoFile.getAbsolutePath() + ".mpg"));
                                    ApplicationContext.databaseRef.update("image", "FileStoreUrl", videoFile.getAbsolutePath() + ".mpg", "SopUID", instancesLocation.get(j).substring(instancesLocation.get(j).lastIndexOf(File.separator) + 1));
                                }
                            }
                            dBuilder = null;
                            dbFactory = null;
                        } catch (IOException ex) {
                            Logger.getLogger(WadoRetrieveDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParserConfigurationException ex) {
                            Logger.getLogger(WadoRetrieveDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SAXException ex) {
                            Logger.getLogger(WadoRetrieveDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        ApplicationContext.databaseRef.update("study", "DownloadStatus", true, "StudyInstanceUID", studyUID);


        if (!ApplicationContext.isJnlp) {
            //Decides whether all studies completed
            boolean studiesPending = ApplicationContext.databaseRef.isDownloadPending();
            if (!studiesPending) {
                ApplicationContext.mainScreenObj.hideProgressBar();
            }
        } else {
            patientInfo[0] = patientName;
            patientInfo[1] = patientID;
            patientInfo[2] = studyDesc;
            Date date = null, time = null;
            try {
                date = dateFormat.parse(studyDate.replace("-", ""));
                time = timeFormat.parse(studyTime.replace(":", ""));
            } catch (ParseException ex) {
                // ignore the parse exception
            }
            try {
                patientInfo[3] = displayDateFormat.format(date) + " " + displayTimeFormat.format(time);
            } catch (NullPointerException nullPtr) {
                patientInfo[3] = "Unknown";
            }
            patientInfo[4] = String.valueOf(seriesList.size() + " Series");
        }
        ApplicationFacade.hideSplash();
        String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(studyUID, seriesList.get(0).getSeriesInstanceUID());
        if (!seriesList.get(0).isVideo()) {
            ApplicationContext.openImageView(filePath, studyUID, patientInfo, 0);
        } else {
            ApplicationContext.openVideo(filePath, studyUID, patientInfo);
        }
    }

    private void sortSeries() {
        Collections.sort(seriesList, new Comparator() {
            @Override
            public int compare(Object t, Object t1) {
                Series i1 = (Series) t;
                Series i2 = (Series) t1;

                if (!i1.getSeriesNumber().equals("") && !i2.getSeriesNumber().equals("")) {
                    if (new Integer(i1.getSeriesNumber()) == null) {
                        return (-1);
                    } else if (new Integer(i2.getSeriesNumber()) == null) {
                        return (1);
                    } else {
                        int a = new Integer(i1.getSeriesNumber());
                        int b = new Integer(i2.getSeriesNumber());
                        int temp = (a == b ? 0 : (a > b ? 1 : -1));
                        return temp;
                    }
                } else {
                    return 0;
                }
            }
        });
    }
}
