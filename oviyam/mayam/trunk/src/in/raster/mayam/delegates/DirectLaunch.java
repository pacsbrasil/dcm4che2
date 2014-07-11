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
import in.raster.mayam.form.ImagePreviewPanel;
import in.raster.mayam.form.ViewerPreviewPanel;
import in.raster.mayam.models.InputArgumentValues;
import in.raster.mayam.models.Series;
import in.raster.mayam.models.ServerModel;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.UID;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class DirectLaunch extends SwingWorker<Void, Void> {

    InputArgumentValues inputArgumentValues = null;
    ExecutorService executor = Executors.newFixedThreadPool(3);

    public DirectLaunch(InputArgumentValues inputArgumentValues) {
        this.inputArgumentValues = inputArgumentValues;
//        run();
    }

    private void doQuery1(InputArgumentValues inputArgumentValues) {

        DcmURL url = new DcmURL("dicom://" + inputArgumentValues.getAeTitle() + "@" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getPort());
        QueryService queryService = new QueryService();
        QuerySeriesService querySeriesService = new QuerySeriesService();
        QueryInstanceService queryInstanceService = new QueryInstanceService();
        ArrayList<String> sopUidList = new ArrayList<String>();

        queryService.callFindWithQuery(inputArgumentValues.getPatientID(), inputArgumentValues.getPatientName(), "", inputArgumentValues.getSearchDate(), inputArgumentValues.getModality(), "", inputArgumentValues.getAccessionNumber(), "", "", inputArgumentValues.getStudyUID(), url);
        Vector<Dataset> studyVector = queryService.getDatasetVector();
        if (studyVector.isEmpty()) {
            System.err.println("No Data Found.");
            System.exit(0);
        }

        for (int study_Iter = 0; study_Iter < studyVector.size(); study_Iter++) {
            ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel();
            Dataset study = studyVector.get(study_Iter);
            imagePreviewPanel.setPatientInfo(constructPatientInfo(study));
            ApplicationContext.openImageView(study.getString(Tags.PatientName), study.getString(Tags.StudyInstanceUID), imagePreviewPanel);

            querySeriesService.callFindWithQuery(study.getString(Tags.PatientID), study.getString(Tags.StudyInstanceUID), url);
            Vector<Dataset> seriesVector = querySeriesService.getDatasetVector();

            HashSet<String> multiframesAndVideos = new HashSet<String>();
            for (int ser_Iter = 0; ser_Iter < seriesVector.size(); ser_Iter++) {
                Dataset ser_Dataset = seriesVector.get(ser_Iter);
//                ViewerPreviewPanel preview = new ViewerPreviewPanel();
                int multiframeCnt = 0;
                ArrayList<String> instances = new ArrayList<String>();

                queryInstanceService.callFindWithQuery(study.getString(Tags.PatientID), study.getString(Tags.StudyInstanceUID), ser_Dataset.getString(Tags.SeriesInstanceUID), url);
                Vector<Dataset> inst_Vector = queryInstanceService.getDatasetVector();

                for (int inst_iter = 0; inst_iter < inst_Vector.size(); inst_iter++) {
                    Dataset inst_Dataset = inst_Vector.get(inst_iter);
                    if (!sopUidList.contains(inst_Dataset.getString(Tags.SOPInstanceUID))) {
                        sopUidList.add(inst_Dataset.getString(Tags.SOPInstanceUID));
                    }

                    if (inst_Dataset.getString(Tags.NumberOfFrames) != null) { //Multiframe
                        multiframeCnt++;
                        if (inst_Dataset.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || inst_Dataset.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || inst_Dataset.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) { //Video
                            ViewerPreviewPanel preview = new ViewerPreviewPanel();
                            preview.videoPreview(study.getString(Tags.StudyInstanceUID), ser_Dataset.getString(Tags.SeriesInstanceUID), ser_Dataset.getString(Tags.SOPInstanceUID) + "," + ser_Dataset.getString(Tags.NumberOfFrames));
                            setPreviewPosition(preview, ser_Iter, imagePreviewPanel);
                        } else { //Multiframe
                            ViewerPreviewPanel preview = new ViewerPreviewPanel();
                            preview.multiframePreview(study.getString(Tags.StudyInstanceUID), ser_Dataset.getString(Tags.SeriesInstanceUID), ser_Dataset.getString(Tags.SOPInstanceUID) + "," + ser_Dataset.getString(Tags.NumberOfFrames));
                            setPreviewPosition(preview, ser_Iter, imagePreviewPanel);
                        }
                    } else {
                        instances.add(inst_Dataset.getString(Tags.SeriesInstanceUID) + "," + inst_Dataset.getString(Tags.SOPInstanceUID));
                    }
                }

                if (ser_Dataset.getInteger(Tags.NumberOfSeriesRelatedInstances) > multiframeCnt) {
                    ViewerPreviewPanel preview = new ViewerPreviewPanel();
                    preview.normalPreview(study.getString(Tags.StudyInstanceUID), ser_Dataset.getString(Tags.SeriesInstanceUID), ser_Dataset.getString(Tags.SeriesDescription), ser_Dataset.getInteger(Tags.NumberOfSeriesRelatedInstances));
                    setPreviewPosition(preview, ser_Iter, imagePreviewPanel);
                    if (inputArgumentValues.getRetrieveType().equals("WADO")) {
                        ServerModel serverModel = new ServerModel();
                        serverModel.setWadoInformation(inputArgumentValues.getWadoProtocol(), inputArgumentValues.getWadoContext(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort());
                        preview.loadThumbnails(instances, serverModel);
                    } else {
                        preview.loadBackground();
                    }
                }
            }
        }

    }

    private void doQuery(InputArgumentValues inputArgumentValues) {
        HashMap<String, HashSet<String>> instances = null;
        HashSet<String> sopClassList = new HashSet<String>(0, 1);
        HashSet<String> videoInstances = null;
        ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel();
        boolean useMoveRQ = false;
        if (inputArgumentValues.getRetrieveType().equalsIgnoreCase("WADO")) {
            useMoveRQ = false;
            instances = new HashMap<String, HashSet<String>>(3, 5);
            videoInstances = new HashSet<String>(0, 1);
        } else if (inputArgumentValues.getRetrieveType().equalsIgnoreCase("C-MOVE")) {
            useMoveRQ = true;
        }
        DcmURL url = new DcmURL("dicom://" + inputArgumentValues.getAeTitle() + "@" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getPort());
        QueryService queryService = new QueryService();
        QuerySeriesService querySeriesService = new QuerySeriesService();
        QueryInstanceService queryInstanceService = new QueryInstanceService();
        queryService.callFindWithQuery(inputArgumentValues.getPatientID(), inputArgumentValues.getPatientName(), "", inputArgumentValues.getSearchDate(), inputArgumentValues.getModality(), "", inputArgumentValues.getAccessionNumber(), "", "", inputArgumentValues.getStudyUID(), url);
        if (queryService.getDatasetVector().isEmpty()) {
            System.err.println("No Matching Studies Found");
            System.exit(0);
        }
        Dataset studyDataset = (Dataset) queryService.getDatasetVector().get(0);
        imagePreviewPanel.setPatientInfo(constructPatientInfo(studyDataset));
//        ApplicationContext.ImageView(studyDataset.getString(Tags.PatientName), studyDataset.getString(Tags.StudyInstanceUID), imagePreviewPanel);
        ApplicationContext.openImageView(studyDataset.getString(Tags.PatientName), studyDataset.getString(Tags.StudyInstanceUID), imagePreviewPanel);
        for (int i = 0; i < queryService.getDatasetVector().size(); i++) {
            HashSet<String> instanceList = new HashSet<String>(3, 5);
            querySeriesService.callFindWithQuery(inputArgumentValues.getPatientID(), inputArgumentValues.getStudyUID(), url);
            for (int cnt = 0; cnt < querySeriesService.getDatasetVector().size(); cnt++) {
                int multiframes = 0;
                Dataset dataset = (Dataset) querySeriesService.getDatasetVector().elementAt(cnt);
                queryInstanceService.callFindWithQuery(studyDataset.getString(Tags.PatientID), studyDataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), url);
                for (int k = 0; k < queryInstanceService.getDatasetVector().size(); k++) {
                    Dataset instanceSet = (Dataset) queryInstanceService.getDatasetVector().get(k);
                    sopClassList.add(instanceSet.getString(Tags.SOPClassUID));
                    if (instanceSet.getInteger(Tags.NumberOfFrames) != null) {
                        if (instanceSet.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || instanceSet.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || instanceSet.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) {
                            createMultiframeOrVideoPreview(dataset, imagePreviewPanel, multiframes, instanceSet.getString(Tags.SOPInstanceUID) + "," + instanceSet.getString(Tags.NumberOfFrames), "Video");
                            multiframes++;
                            videoInstances.add(instanceSet.getString(Tags.SeriesInstanceUID) + "," + instanceSet.getString(Tags.SOPInstanceUID));
                        } else {
                            createMultiframeOrVideoPreview(dataset, imagePreviewPanel, multiframes, instanceSet.getString(Tags.SOPInstanceUID) + "," + instanceSet.getString(Tags.NumberOfFrames), "Multiframe");
                            multiframes++;
                            instanceList.add(instanceSet.getString(Tags.SeriesInstanceUID) + "," + instanceSet.getString(Tags.SOPInstanceUID));
                        }
                    } else {
                        instanceList.add(instanceSet.getString(Tags.SeriesInstanceUID) + "," + instanceSet.getString(Tags.SOPInstanceUID));
                    }
                }
                if (instances != null) {
                    instances.put(dataset.getString(Tags.SeriesInstanceUID), instanceList);
                }
                if (multiframes < dataset.getInteger(Tags.NumberOfSeriesRelatedInstances)) {
                    createPreviews(dataset, imagePreviewPanel, cnt, dataset.getInteger(Tags.NumberOfSeriesRelatedInstances) - multiframes);
                }
                if (useMoveRQ) {
                    if (cnt == 0) {
                        executor.submit(new SeriesRetriever(new String[]{url.getProtocol() + "://" + url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort(), "--dest", ApplicationContext.listenerDetails[0], "--pid", dataset.getString(Tags.PatientID), "--suid", dataset.getString(Tags.StudyInstanceUID), "--Suid", dataset.getString(Tags.SeriesInstanceUID)}, inputArgumentValues.getStudyUID(), dataset.getString(Tags.SeriesInstanceUID), studyDataset.getInteger(Tags.NumberOfStudyRelatedInstances), true, true, true));
                    } else {
                        executor.submit(new SeriesRetriever(new String[]{url.getProtocol() + "://" + url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort(), "--dest", ApplicationContext.listenerDetails[0], "--pid", dataset.getString(Tags.PatientID), "--suid", dataset.getString(Tags.StudyInstanceUID), "--Suid", dataset.getString(Tags.SeriesInstanceUID)}, inputArgumentValues.getStudyUID(), dataset.getString(Tags.SeriesInstanceUID), studyDataset.getInteger(Tags.NumberOfStudyRelatedInstances), false, true, true));
                    }
                } else if (instances == null) {
                    ArrayList<String> cgetParamList = new ArrayList<String>();
                    cgetParamList.add(url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort());
                    cgetParamList.add("-L " + ApplicationContext.listenerDetails[0]);
                    cgetParamList.add("-cget");
                    cgetParamList.add("-I");
                    cgetParamList.add("-qStudyInstanceUID=" + studyDataset.getString(Tags.StudyInstanceUID));
                    cgetParamList.add("-qSeriesInstanceUID=" + dataset.getString(Tags.SeriesInstanceUID));
                    Iterator<String> iterator = sopClassList.iterator();
                    while (iterator.hasNext()) {
                        String sopUid = iterator.next();
                        cgetParamList.add("-cstore");
                        cgetParamList.add(sopUid + ":" + UID.ExplicitVRLittleEndian);
                        if (sopClassList.contains(UID.VideoEndoscopicImageStorage) || sopClassList.contains(UID.VideoMicroscopicImageStorage) || sopClassList.contains(UID.VideoPhotographicImageStorage)) {
                            cgetParamList.add(",");
                            cgetParamList.add(sopUid + ":" + UID.MPEG2);
                            cgetParamList.add(",");
                            cgetParamList.add(sopUid + ":" + UID.MPEG2MainProfileHighLevel);
                            cgetParamList.add(",");
                            cgetParamList.add(sopUid + ":" + UID.MPEG4AVCH264HighProfileLevel41);
                            cgetParamList.add(",");
                            cgetParamList.add(sopUid + ":" + UID.MPEG4AVCH264BDCompatibleHighProfileLevel41);
                        }
                    }
                    cgetParamList.add("-cstoredest");
                    cgetParamList.add(ApplicationContext.listenerDetails[2]);
                    String[] cGetParam = cgetParamList.toArray(new String[cgetParamList.size()]);
                    if (cnt == 0) {
                        executor.submit(new SeriesRetriever(cGetParam, studyDataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), studyDataset.getInteger(Tags.NumberOfStudyRelatedInstances), true, true, useMoveRQ));
                    } else {
                        executor.submit(new SeriesRetriever(cGetParam, studyDataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), studyDataset.getInteger(Tags.NumberOfStudyRelatedInstances), false, true, useMoveRQ));
                    }
                }
            }
            if (instances != null) { //It should be wado
                ServerModel serverModel = new ServerModel();
                serverModel.setAeTitle(inputArgumentValues.getAeTitle());
                serverModel.setHostName(inputArgumentValues.getHostName());
                serverModel.setPort(inputArgumentValues.getPort());
                serverModel.setWadoContextPath(inputArgumentValues.getWadoContext());
                serverModel.setWadoPort(inputArgumentValues.getWadoPort());
                serverModel.setWadoProtocol(inputArgumentValues.getWadoProtocol());
                executor.submit(new ThumbnailWadoRetriever(studyDataset.getString(Tags.StudyInstanceUID), instances, serverModel));
                if (!instances.isEmpty()) {
                    executor.submit(new WadoRetriever(studyDataset.getString(Tags.StudyInstanceUID), instances, serverModel, true, true));
                }
                if (!videoInstances.isEmpty()) {
                    HashMap<String, HashSet<String>> videos = new HashMap<String, HashSet<String>>(0, 1);
                    videos.put(videoInstances.iterator().next().split(",")[0], videoInstances);
                    executor.submit(new WadoRetriever(studyDataset.getString(Tags.StudyInstanceUID), videos, serverModel, false, false));
                }
            }
        }
    }
    int position = 0, size = 0;

    private void createPreviews(Dataset seriesDataset, ImagePreviewPanel imagePreviewPanel, int id, int noOfInstances) {
        Series series = new Series(seriesDataset);
        series.setSeriesRelatedInstance(noOfInstances);
        ViewerPreviewPanel viewerPreviewPanel = new ViewerPreviewPanel(seriesDataset.getString(Tags.StudyInstanceUID), series, null);
        viewerPreviewPanel.setVisible(true);
        viewerPreviewPanel.setName(String.valueOf(id));
        int height = viewerPreviewPanel.getTotalHeight();
        size += height + 5;
        viewerPreviewPanel.setBounds(0, position, 230, height);
        imagePreviewPanel.addViewerPanel(position, height, viewerPreviewPanel, size);
        position += (height + 5);
    }

    private void setPreviewPosition(ViewerPreviewPanel preview, int id, ImagePreviewPanel imagePreviewPanel) {
        preview.setVisible(true);
        preview.setName(String.valueOf(id));
        int height = preview.getTotalHeight();
        size += height + 5;
        preview.setBounds(0, position, 230, height);
        imagePreviewPanel.addViewerPanel(position, height, preview, size);
        position += (height + 5);
    }

    private void createMultiframeOrVideoPreview(Dataset seriesDataset, ImagePreviewPanel imagePreviewPanel, int id, String iuid, String seriesDesc) {
        Series series = new Series(seriesDataset);
        series.setSeriesRelatedInstance(1);
        series.setSeriesDesc(seriesDesc);
        series.setVideoStatus(seriesDesc.equals("Video"));
        ViewerPreviewPanel viewerPreviewPanel = new ViewerPreviewPanel(seriesDataset.getString(Tags.StudyInstanceUID), series, iuid);
        viewerPreviewPanel.setVisible(true);
        viewerPreviewPanel.setName(String.valueOf(id));
        int height = viewerPreviewPanel.getTotalHeight();
        size += height + 5;
        viewerPreviewPanel.setBounds(0, position, 230, height);
        imagePreviewPanel.addViewerPanel(position, height, viewerPreviewPanel, size);
        if (series.isVideo()) {
            viewerPreviewPanel.loadVideoImage();
        }
        position += (height + 5);
    }

    private String[] constructPatientInfo(Dataset dataset) {
        String[] patientInfo = new String[5];
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat timeFormat = new SimpleDateFormat("hhmmss");
        DateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat displayTimeFormat = new SimpleDateFormat("hh:mm:ss");
        patientInfo[0] = dataset.getString(Tags.PatientName);
        patientInfo[1] = dataset.getString(Tags.PatientID);
        patientInfo[2] = dataset.getString(Tags.StudyDescription);
        Date date = null, time = null;
        try {
            date = dateFormat.parse(dataset.getString(Tags.StudyDate).replace("-", ""));
            time = timeFormat.parse(dataset.getString(Tags.StudyTime).replace(":", ""));
        } catch (ParseException ex) {
            ApplicationContext.logger.log(Level.INFO, "Direct Launch - Unable to parse data and time");
        }
        try {
            patientInfo[3] = displayDateFormat.format(date) + " " + displayTimeFormat.format(time);
        } catch (NullPointerException nullPtr) {
            patientInfo[3] = "Unknown";
        }
        patientInfo[4] = String.valueOf(dataset.getInteger(Tags.NumberOfStudyRelatedSeries) + " Series");
        return patientInfo;
    }

    private String[] constructPatientInfo(Element patient, Element study) {
        String[] patientInfo = new String[5];
        patientInfo[0] = patient.getAttribute("PatientName");
        patientInfo[1] = patient.getAttribute("PatientID");
        patientInfo[2] = study.getAttribute("StudyDescription");
        try {
            patientInfo[3] = DateFormat.getDateInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(new SimpleDateFormat("yyyyMMdd").parse(study.getAttribute("StudyDate")));
        } catch (ParseException ex) {
            patientInfo[3] = "Unknown";
        }

        try {
            patientInfo[3] += " " + DateFormat.getTimeInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(new SimpleDateFormat("hhmmss").parse(study.getAttribute("StudyTime")));
        } catch (ParseException ex) {
            patientInfo[3] += "Unknown";
        }
        patientInfo[4] = String.valueOf(study.getAttribute("SeriesCount") + " Series");
        return patientInfo;
    }

//    private void readXML() {
//        File xmlFile = new File(inputArgumentValues.getXmlFilePath());
//        Document xmlDoc = null;
//        if (xmlFile.exists()) {
//            try {
//                xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
//            } catch (ParserConfigurationException ex) {
//                Logger.getLogger(DirectLaunch.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (SAXException ex) {
//                Logger.getLogger(DirectLaunch.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(DirectLaunch.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            Element patient = (Element) xmlDoc.getElementsByTagName("Patient").item(0);
//            Element study = (Element) xmlDoc.getElementsByTagName("Study").item(0);
//
//            ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel();
//            imagePreviewPanel.setPatientInfo(constructPatientInfo(patient, study));
//            ApplicationContext.openImageView(patient.getAttribute("PatientName"), study.getAttribute("StudyInstanceUID"), imagePreviewPanel);
//
//            NodeList seriesList = xmlDoc.getElementsByTagName("Series");
//            ThumbnailWadoRetriever thumbnail_reteriver = new ThumbnailWadoRetriever();
//            WadoRetriever wado_reteriver = new WadoRetriever();
//
//            for (int ser_Iter = 0; ser_Iter < seriesList.getLength(); ser_Iter++) {
//                Node child = seriesList.item(ser_Iter);
//                Element ser_Item = (Element) child;
//                String sopClassUID = ser_Item.getAttribute("SOPClassUID");
//
////                Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);                
////                String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + study.getAttribute("StudyInstanceUID") + File.separator + ser_Item.getAttribute("SeriesInstanceUID") + File.separator + "Thumbnails" + File.separator;
//                createPreviews(study.getAttribute("StudyInstanceUID"), ser_Item, ser_Iter, imagePreviewPanel, sopClassUID);
//
//                if (!ser_Item.getAttribute("SeriesDescription").equals("Multiframe")) {
//                    NodeList instances = ser_Item.getChildNodes();
////                    System.out.println("Len : " +instances.getLength());
////                    Thumbnail[] thumbnails = new Thumbnail[instances.getLength()];
//
//                    for (int inst_Iter = 0; inst_Iter < instances.getLength(); inst_Iter++) {
//                        Node item1 = instances.item(inst_Iter);
//
//                        if (item1.getNodeType() == Node.ELEMENT_NODE) {
//                            Element inst_Item = (Element) instances.item(inst_Iter);
//                            String thumbnailReq = inputArgumentValues.getWadoProtocol() + "://" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getWadoPort() + "/wado?requestType=WADO&studyUID=" + study.getAttribute("StudyInstanceUID") + "&seriesUID=" + ser_Item.getAttribute("SeriesInstanceUID") + "&objectUID=" + inst_Item.getAttribute("SOPInstanceUID") + "&rows=75&columns=75";
//                            String dicom_Req = inputArgumentValues.getWadoProtocol() + "://" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getWadoPort() + "/wado?requestType=WADO&contentType=application/dicom&studyUID=" + study.getAttribute("StudyInstanceUID") + "&seriesUID=" + ser_Item.getAttribute("SeriesInstanceUID") + "&objectUID=" + inst_Item.getAttribute("SOPInstanceUID");
////                            thumbnails[inst_Iter] = new Thumbnail(thumbnailReq, dest + inst_Item.getAttribute("SOPInstanceUID"), inst_Item.getAttribute("SOPInstanceUID"));
//                            thumbnail_reteriver.retrieveThumbnail(thumbnailReq, study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"), inst_Item.getAttribute("SOPInstanceUID"));
//                            wado_reteriver.reterive(dicom_Req, study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"), inst_Item.getAttribute("SOPInstanceUID"));
//                        }
//                    }
////                    createPreview(study.getAttribute("StudyInstanceUID"), ser_Item, ser_Iter, imagePreviewPanel, thumbnails);
//                    ApplicationContext.displayPreviewOfSeries(study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"));
//                    if (ser_Iter == 0) {
//                        ApplicationContext.createCanvas(ApplicationContext.databaseRef.getFirstInstanceLocation(study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID")), study.getAttribute("StudyInstanceUID"), ser_Iter);
//                    }
//                } else {
//                    String dicom_Req = inputArgumentValues.getWadoProtocol() + "://" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getWadoPort() + "/wado?requestType=WADO&contentType=application/dicom&studyUID=" + study.getAttribute("StudyInstanceUID") + "&seriesUID=" + ser_Item.getAttribute("SeriesInstanceUID") + "&objectUID=" + ser_Item.getAttribute("SOPInstanceUID");
//                    wado_reteriver.reterive(dicom_Req, study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"), ser_Item.getAttribute("SOPInstanceUID"));
//
//                    if (!(sopClassUID.equals(UID.VideoEndoscopicImageStorage) || sopClassUID.equals(UID.VideoMicroscopicImageStorage) || sopClassUID.equals(UID.VideoPhotographicImageStorage))) {
//                        String thumbnailReq = inputArgumentValues.getWadoProtocol() + "://" + inputArgumentValues.getHostName() + ":" + inputArgumentValues.getWadoPort() + "/wado?requestType=WADO&studyUID=" + study.getAttribute("StudyInstanceUID") + "&seriesUID=" + ser_Item.getAttribute("SeriesInstanceUID") + "&objectUID=" + ser_Item.getAttribute("SOPInstanceUID") + "&rows=75&columns=75";
////                        Thumbnail[] thumbnail = new Thumbnail[]{new Thumbnail(thumbnailReq, dest + ser_Item.getAttribute("SOPInstanceUID"), ser_Item.getAttribute("SOPInstanceUID"))};
//                        thumbnail_reteriver.retrieveThumbnail(thumbnailReq, study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"), ser_Item.getAttribute("SOPInstanceUID"));
////                        createMultiframePreview(study.getAttribute("studyInstanceUID"), ser_Item, ser_Iter, imagePreviewPanel, thumbnail);
//                    }
////                    else {
////                        createVideoPreview(study.getAttribute("StudyInstanceUID"), ser_Item, ser_Iter, imagePreviewPanel);
////                    }
//                    if (ser_Iter == 0) {
//                        ApplicationContext.createCanvas(ApplicationContext.databaseRef.getFileLocation(ser_Item.getAttribute("SOPInstanceUID")), study.getAttribute("StudyInstanceUID"), ser_Iter);
//                    }
//                }
//            }
//            ApplicationContext.createMultiframePreviews(study.getAttribute("StudyInstanceUID"));
//            ApplicationContext.databaseRef.updateStudy(study.getAttribute("StudyInstanceUID"));
//        } else {
//            System.err.println("ERROR : Unable to parse XML File.");
//            System.exit(0);
//        }
//    }
    private void readXMLFile() {
        File xmlFile = new File(inputArgumentValues.getXmlFilePath());
        Document xmlDoc = null;
        if (xmlFile.exists()) {
            try {
                xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
            } catch (ParserConfigurationException ex) {
                ApplicationContext.logger.log(Level.INFO, "Direct Launch", ex);
            } catch (SAXException ex) {
                ApplicationContext.logger.log(Level.INFO, "Direct Launch", ex);
            } catch (IOException ex) {
                ApplicationContext.logger.log(Level.INFO, "Direct Launch", ex);
            }
            Element patient = (Element) xmlDoc.getElementsByTagName("Patient").item(0);
            Element study = (Element) xmlDoc.getElementsByTagName("Study").item(0);

            ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel();
            imagePreviewPanel.setPatientInfo(constructPatientInfo(patient, study));
            ApplicationContext.openImageView(patient.getAttribute("PatientName"), study.getAttribute("StudyInstanceUID"), imagePreviewPanel);
            HashMap<String, Collection<String>> instancesToRetrieve = new HashMap<String, Collection<String>>();

            HashSet<String> multiframesAndVideos = new HashSet<String>();
            NodeList seriesList = xmlDoc.getElementsByTagName("Series");
            for (int ser_Iter = 0; ser_Iter < seriesList.getLength(); ser_Iter++) {
                Node child = seriesList.item(ser_Iter);
                Element ser_Item = (Element) child;
                String sopClassUID = ser_Item.getAttribute("SOPClassUID");


                NodeList instances = ser_Item.getChildNodes();
                ViewerPreviewPanel preview = new ViewerPreviewPanel();
                if (ser_Item.getAttribute("SeriesDescription").equals("Multiframe")) {
                    if (sopClassUID.equals(UID.VideoEndoscopicImageStorage) || sopClassUID.equals(UID.VideoMicroscopicImageStorage) || sopClassUID.equals(UID.VideoPhotographicImageStorage)) {
                        preview.videoPreview(study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"), ser_Item.getAttribute("SOPInstanceUID") + "," + ser_Item.getAttribute("NumberOfFrames"));
                    } else {
                        preview.multiframePreview(study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"), ser_Item.getAttribute("SOPInstanceUID") + "," + ser_Item.getAttribute("NumberOfFrames"));
                        preview.multiframes(inputArgumentValues.getWadoProtocol(), inputArgumentValues.getWadoContext(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort());
                    }
                    multiframesAndVideos.add(ser_Item.getAttribute("SesriesInstanceUID") + "," + ser_Item.getAttribute("SOPInstanceUID"));
                } else {
                    ArrayList<String> instance_List = new ArrayList<String>();
                    for (int inst_Iter = 0; inst_Iter < instances.getLength(); inst_Iter++) {
                        Node item = instances.item(inst_Iter);
                        if (item.getNodeType() == Node.ELEMENT_NODE) {
                            Element inst_Item = (Element) instances.item(inst_Iter);
                            instance_List.add(ser_Item.getAttribute("SeriesInstanceUID") + "," + inst_Item.getAttribute("SOPInstanceUID"));
                            preview.normalPreview(study.getAttribute("StudyInstanceUID"), ser_Item.getAttribute("SeriesInstanceUID"), ser_Item.getAttribute("SeriesDescription"), Integer.parseInt(ser_Item.getAttribute("InstanceCount")));
                            preview.loadThumbnails(instance_List, inputArgumentValues.getWadoProtocol(), inputArgumentValues.getWadoContext(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort());
                        }
                    }
                    instancesToRetrieve.put(ser_Item.getAttribute("SeriesInstanceUID"), instance_List);
                }

                preview.setVisible(true);
                preview.setName(String.valueOf(ser_Iter));
                int height = preview.getTotalHeight();
                size += height + 5;
                preview.setBounds(0, position, 230, height);
                imagePreviewPanel.addViewerPanel(position, height, preview, size);
                position += (height + 5);
            }

            for (int i = 0; i < imagePreviewPanel.parent.getComponentCount(); i++) {
                String seriesUID = ((ViewerPreviewPanel) imagePreviewPanel.parent.getComponent(i)).load(false);
                Collection<String> instances = instancesToRetrieve.get(seriesUID);
                if (instances != null) {
                    new WadoRetriever(study.getAttribute("StudyInstanceUID"), seriesUID, instances, inputArgumentValues.getWadoProtocol(), inputArgumentValues.getWadoContext(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort(), (i == 0), instances.size()).run();
                }
            }

            if (!multiframesAndVideos.isEmpty()) {
                WadoRetriever wadoRetriever = new WadoRetriever();
                ServerModel server = new ServerModel();
                server.setWadoInformation(inputArgumentValues.getWadoProtocol(), inputArgumentValues.getWadoContext(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort());
                wadoRetriever.reteriveVideoAndMultiframes(study.getAttribute("StudyInstanceUID"), server, multiframesAndVideos, instancesToRetrieve.isEmpty());
            }

            ApplicationContext.databaseRef.update("study", "NoOfInstances", ApplicationContext.databaseRef.getStudyLevelInstances(study.getAttribute("StudyInstanceUID")), "StudyInstanceUID", study.getAttribute("StudyInstanceUID"));
            ApplicationContext.databaseRef.update("study", "NoOfSeries", Integer.parseInt(study.getAttribute("SeriesCount")), "StudyInstanceUID", study.getAttribute("StudyInstanceUID"));
        } else {
            System.err.println("ERROR : Unable to parse XML File.");
            System.exit(0);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            if (inputArgumentValues.getXmlFilePath() != null) {
//                readXML();
                readXMLFile();
            } else {
                doQuery(inputArgumentValues);
            }
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.INFO, "Direct Launch - Unable to launch study", ex);
        }
        return null;
    }
}