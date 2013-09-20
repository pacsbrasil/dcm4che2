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
import in.raster.mayam.facade.ApplicationFacade;
import in.raster.mayam.models.InputArgumentValues;
import in.raster.mayam.models.Series;
import in.raster.mayam.util.core.DcmQR;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * @author Devishree
 * @version 2.0
 */
public class CGetDelegate extends Thread {

    String getArg[];
    String patientId, modalitiesInStudy, StudyInstanceUid;
    int totalInstances;
    ArrayList<Series> seriesList;
    String[] patientInfo;

    public CGetDelegate(String getArg[], String patientId, String studyInstanceUid, String modalitiesInStudy, ArrayList<Series> seriesList, int totalInstances, String[] patientInfo) {
        this.getArg = getArg;
        this.patientId = patientId;
        this.StudyInstanceUid = studyInstanceUid;
        this.modalitiesInStudy = modalitiesInStudy;
        this.seriesList = seriesList;
        this.totalInstances = totalInstances;
        this.patientInfo = patientInfo;
        this.start();
    }

    public CGetDelegate(InputArgumentValues inputArgumentValues) {
        doQuery(inputArgumentValues);
    }

    @Override
    public void run() {
        get();
    }

    private void get() {
        DcmQR.main(getArg); //Retrieves the study from server
        //Database Updations after finishing the whole study        
        ApplicationContext.databaseRef.update("study", "NoOfSeries", seriesList.size(), "StudyInstanceUID", StudyInstanceUid);
        ApplicationContext.databaseRef.update("study", "NoOfInstances", ApplicationContext.databaseRef.getTotalInstances(StudyInstanceUid), "StudyInstanceUID", StudyInstanceUid);
        //Constructs the JPG files for the study
        for (int i = 0; i < seriesList.size(); i++) {
            if (!seriesList.get(i).isVideo()) {
                ApplicationContext.databaseRef.update("series", "NoOfSeriesRelatedInstances", seriesList.get(i).getSeriesRelatedInstance(), "SeriesInstanceUID", seriesList.get(i).getSeriesInstanceUID());
                ConstructThumbnails constructThumbnails = new ConstructThumbnails(StudyInstanceUid, seriesList.get(i).getSeriesInstanceUID());
                ApplicationContext.mainScreenObj.increaseProgressValue();
            } else {
                ArrayList<String> instancesLocation = ApplicationContext.databaseRef.getInstancesLocation(StudyInstanceUid, seriesList.get(i).getSeriesInstanceUID());
                for (int j = 0; j < instancesLocation.size(); j++) {
                    if (!instancesLocation.get(j).contains("_V")) {
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
                            Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParserConfigurationException ex) {
                            Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SAXException ex) {
                            Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        ApplicationContext.databaseRef.update("study", "DownloadStatus", true, "StudyInstanceUID", StudyInstanceUid);
        String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(StudyInstanceUid, seriesList.get(0).getSeriesInstanceUID());
        if (!seriesList.get(0).isVideo()) {
            ApplicationContext.openImageView(filePath, StudyInstanceUid, patientInfo, 0);
        } else {
            ApplicationContext.openVideo(filePath, StudyInstanceUid, patientInfo);
        }
        //Decides whether all studies completed
        boolean studiesPending = ApplicationContext.databaseRef.isDownloadPending();
        if (!studiesPending) {
            ApplicationContext.mainScreenObj.hideProgressBar();
        }
        seriesList = null;
    }

    private void doQuery(InputArgumentValues inputArgumentValues) {
        DcmURL url = new DcmURL("dicom://" + inputArgumentValues.getAeTitle() + "@" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getPort());
        QueryService queryService = new QueryService();
        QuerySeriesService querySeriesService = new QuerySeriesService();
        QueryInstanceService queryInstanceService = new QueryInstanceService();
        queryService.callFindWithQuery(inputArgumentValues.getPatientID(), inputArgumentValues.getPatientName(), "", inputArgumentValues.getSearchDate(), inputArgumentValues.getModality(), "", inputArgumentValues.getAccessionNumber(), "", "", inputArgumentValues.getStudyUID(), url);
        if (queryService.getDatasetVector().size() == 0) {
            System.err.println("No Matching Studies Found");
            System.exit(0);
        }
        for (int dataSetCount = 0; dataSetCount < queryService.getDatasetVector().size(); dataSetCount++) {
            seriesList = new ArrayList<Series>();
            ArrayList<String> sopClassList = new ArrayList<String>();
            Dataset dataSet = (Dataset) queryService.getDatasetVector().elementAt(dataSetCount);
            constructPatientInfo(dataSet);
            querySeriesService.callFindWithQuery(patientId, StudyInstanceUid, url);
            for (int i = 0; i < querySeriesService.getDatasetVector().size(); i++) {
                Series series = new Series((Dataset) querySeriesService.getDatasetVector().get(i));
                seriesList.add(series);
                queryInstanceService.callFindWithQuery(patientId, StudyInstanceUid, series.getSeriesInstanceUID(), url);
                for (int j = 0; j < queryInstanceService.getDatasetVector().size(); j++) {
                    Dataset dataSet2 = (Dataset) queryInstanceService.getDatasetVector().elementAt(j);
                    if (!sopClassList.contains(dataSet2.getString(Tags.SOPClassUID))) {
                        sopClassList.add(dataSet2.getString(Tags.SOPClassUID));
                    }
                    if (dataSet2.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || dataSet2.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || dataSet2.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) {
                        series.setVideoStatus(true);
                    }
                }
            }
            ArrayList<String> cgetParam = new ArrayList<String>();
            cgetParam.add(url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort());
            cgetParam.add("-L " + ApplicationContext.listenerDetails[0]);
            cgetParam.add("-cget");
            cgetParam.add("-I");
            cgetParam.add("-qStudyInstanceUID=" + StudyInstanceUid);
            for (int i = 0; i < sopClassList.size(); i++) {
                cgetParam.add("-cstore");
                cgetParam.add(sopClassList.get(i) + ":1.2.840.10008.1.2.1");
                if (sopClassList.contains(UID.VideoEndoscopicImageStorage) || sopClassList.contains(UID.VideoPhotographicImageStorage) || sopClassList.contains(UID.VideoMicroscopicImageStorage)) {
                    cgetParam.add(",");
                    cgetParam.add(sopClassList.get(i) + ":" + UID.MPEG2);
                    cgetParam.add(",");
                    cgetParam.add(sopClassList.get(i) + ":" + UID.MPEG2MainProfileHighLevel);
                    cgetParam.add(",");
                    cgetParam.add(sopClassList.get(i) + ":" + UID.MPEG4AVCH264HighProfileLevel41);
                    cgetParam.add(",");
                    cgetParam.add(sopClassList.get(i) + ":" + UID.MPEG4AVCH264BDCompatibleHighProfileLevel41);
                }
            }
            cgetParam.add("-cstoredest");
            cgetParam.add(ApplicationContext.listenerDetails[2]);
            String[] cgetParam1 = cgetParam.toArray(new String[cgetParam.size()]);
            directLaunchRetrieve(cgetParam1);
        }
    }

    private void directLaunchRetrieve(String[] cgetParam) {
        DcmQR.main(cgetParam);
        ApplicationContext.databaseRef.update("study", "NoOfSeries", seriesList.size(), "StudyInstanceUID", StudyInstanceUid);
        ApplicationContext.databaseRef.update("study", "NoOfInstances", totalInstances, "StudyInstanceUID", StudyInstanceUid);
        for (int i = 0; i < seriesList.size(); i++) {
            ApplicationContext.databaseRef.update("series", "NoOfSeriesRelatedInstances", seriesList.get(i).getSeriesRelatedInstance(), "SeriesInstanceUID", seriesList.get(i).getSeriesInstanceUID());
            if (!seriesList.get(i).isVideo()) {
                ConstructThumbnails constructThumbnails = new ConstructThumbnails(StudyInstanceUid, seriesList.get(i).getSeriesInstanceUID());
            } else {
                ArrayList<String> instancesLocation = ApplicationContext.databaseRef.getInstancesLocation(StudyInstanceUid, seriesList.get(i).getSeriesInstanceUID());
                for (int j = 0; j < instancesLocation.size(); j++) {
                    if (!instancesLocation.get(j).contains("_V")) {
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
                            Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParserConfigurationException ex) {
                            Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SAXException ex) {
                            Logger.getLogger(CGetDelegate.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        ApplicationContext.databaseRef.update("study", "DownloadStatus", true, "StudyInstanceUID", StudyInstanceUid);
        String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(StudyInstanceUid);
        ApplicationFacade.hideSplash();
        if (!filePath.contains("_V")) {
            ApplicationContext.openImageView(filePath, StudyInstanceUid, patientInfo, 0);
        } else {
            ApplicationContext.openVideo(filePath, StudyInstanceUid, patientInfo);
        }
    }

    private void constructPatientInfo(Dataset dataSet) {
        patientInfo = new String[5];
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat timeFormat = new SimpleDateFormat("hhmmss");
        DateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat displayTimeFormat = new SimpleDateFormat("hh:mm:ss");
        StudyInstanceUid = dataSet.getString(Tags.StudyInstanceUID);
        patientId = dataSet.getString(Tags.PatientID);
        totalInstances = dataSet.getInteger(Tags.NumberOfStudyRelatedInstances);
        patientInfo[0] = dataSet.getString(Tags.PatientName);
        patientInfo[1] = patientId;
        patientInfo[2] = dataSet.getString(Tags.StudyDescription);
        Date date = null, time = null;
        try {
            date = dateFormat.parse(dataSet.getString(Tags.StudyDate).replace("-", ""));
            time = timeFormat.parse(dataSet.getString(Tags.StudyTime).replace(":", ""));
        } catch (ParseException ex) {
            // ignore the parse exception
        }
        try {
            patientInfo[3] = displayDateFormat.format(date) + " " + displayTimeFormat.format(time);
        } catch (NullPointerException nullPtr) {
            patientInfo[3] = "Unknown";
        }
        patientInfo[4] = String.valueOf(dataSet.getInteger(Tags.NumberOfStudyRelatedSeries) + " Series");
    }
}
