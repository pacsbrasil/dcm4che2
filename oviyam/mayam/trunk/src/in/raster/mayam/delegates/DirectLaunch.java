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
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

    //Launches viewer either using XML file or query filters
    InputArgumentValues inputArgumentValues = null;
    ExecutorService executor = Executors.newFixedThreadPool(3);

    public DirectLaunch(InputArgumentValues inputArgumentValues) {
        this.inputArgumentValues = inputArgumentValues;
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

    private void readXMLFile() {
        try {
            int size = 0, position = 0;
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
                    ViewerPreviewPanel preview = new ViewerPreviewPanel(ser_Iter);
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
                    wadoRetriever.reteriveVideoAndMultiframes(study.getAttribute("StudyInstanceUID"), multiframesAndVideos, instancesToRetrieve.isEmpty(), inputArgumentValues.getWadoProtocol(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort());
                }

                ApplicationContext.databaseRef.update("study", "NoOfInstances", ApplicationContext.databaseRef.getStudyLevelInstances(study.getAttribute("StudyInstanceUID")), "StudyInstanceUID", study.getAttribute("StudyInstanceUID"));
                ApplicationContext.databaseRef.update("study", "NoOfSeries", Integer.parseInt(study.getAttribute("SeriesCount")), "StudyInstanceUID", study.getAttribute("StudyInstanceUID"));
            } else {
                System.err.println("ERROR : Unable to parse XML File.");
                System.exit(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void query(InputArgumentValues inputArgumentValues) {
        ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel();

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

        ApplicationContext.openImageView(studyDataset.getString(Tags.PatientName), studyDataset.getString(Tags.StudyInstanceUID), imagePreviewPanel);

        HashSet<String> sopClassList = new HashSet<String>(0, 1);
        querySeriesService.callFindWithQuery(studyDataset.getString(Tags.PatientID), studyDataset.getString(Tags.StudyInstanceUID), url);
        Vector seriesDS = querySeriesService.getDatasetVector();
        sortSeries(seriesDS);
        int size = 0, position = 0, previewID = 0;

        for (int ser_Iter = 0; ser_Iter < seriesDS.size(); ser_Iter++) {
            Dataset seriesDataset = (Dataset) seriesDS.elementAt(ser_Iter);
            int multiframes = 0;

            queryInstanceService.callFindWithQuery(studyDataset.getString(Tags.PatientID), studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), url);
            Vector instanceDS = queryInstanceService.getDatasetVector();
            sortInstances(instanceDS);
            for (int inst_Iter = 0; inst_Iter < instanceDS.size(); inst_Iter++) {
                Dataset instanceDataset = (Dataset) instanceDS.elementAt(inst_Iter);
                sopClassList.add(instanceDataset.getString(Tags.SOPClassUID));

                if (instanceDataset.getString(Tags.NumberOfFrames) != null) { //Multiframe or video                    
                    multiframes++;
                    WadoRetriever wadoRetriever = new WadoRetriever();
                    wadoRetriever.setData(studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), inputArgumentValues.getWadoProtocol(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort(), null);
                    if (instanceDataset.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || instanceDataset.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || instanceDataset.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) { //Video
                        ViewerPreviewPanel videoPreview = new ViewerPreviewPanel(previewID);
                        videoPreview.videoPreview(studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), instanceDataset.getString(Tags.SOPInstanceUID) + "," + instanceDataset.getString(Tags.NumberOfFrames));
                        int height = videoPreview.getTotalHeight();
                        size += height;
                        videoPreview.setBounds(0, position, 230, height);
                        imagePreviewPanel.addViewerPanel(position, height, videoPreview, size);
                        position += (height + 5);
                        previewID++;
                    } else { //Multiframe
                        ViewerPreviewPanel multiframePreview = new ViewerPreviewPanel(previewID);
                        multiframePreview.multiframePreview(studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), instanceDataset.getString(Tags.SOPInstanceUID) + "," + instanceDataset.getString(Tags.NumberOfFrames));
                        multiframePreview.loadThumbnail(instanceDataset, inputArgumentValues.getWadoProtocol(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort());
                        int height = multiframePreview.getTotalHeight();
                        size += height;
                        multiframePreview.setBounds(0, position, 230, height);
                        imagePreviewPanel.addViewerPanel(position, height, multiframePreview, size);
                        position += (height + 5);
                        previewID++;
                        wadoRetriever.retrieve(instanceDataset);
                        instanceDS.remove(instanceDataset);
                    }
                }
            }
            if (multiframes < seriesDataset.getInteger(Tags.NumberOfSeriesRelatedInstances)) {
                ViewerPreviewPanel preview = new ViewerPreviewPanel(previewID);
                preview.normalPreview(studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), seriesDataset.getString(Tags.SeriesDescription), seriesDataset.getInteger(Tags.NumberOfSeriesRelatedInstances) - multiframes);
                int height = preview.getTotalHeight();
                size += height;
                preview.setBounds(0, position, 230, height);
                imagePreviewPanel.addViewerPanel(position, height, preview, size);
                position += (height + 5);
                previewID++;
                if (inputArgumentValues.getRetrieveType().equals("WADO")) {
                    preview.loadThumbnails(instanceDS, inputArgumentValues.getWadoProtocol(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort());
                    executor.submit(new WadoRetriever(studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), instanceDS, ser_Iter == 0, inputArgumentValues.getWadoProtocol(), inputArgumentValues.getHostName(), inputArgumentValues.getWadoPort(), null, studyDataset.getInteger(Tags.NumberOfStudyRelatedInstances)));
                    preview.load(false);
                }
            } else if (ser_Iter == 0) {
                ApplicationContext.createCanvas(ApplicationContext.databaseRef.getFirstInstanceLocation(studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID)), studyDataset.getString(Tags.StudyInstanceUID), 0);
            }

            if (inputArgumentValues.getRetrieveType().equals("C-MOVE")) {
                String[] moveArg = new String[]{url.getProtocol() + "://" + url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort(), "--dest", ApplicationContext.listenerDetails[0], "--pid", studyDataset.getString(Tags.PatientID), "--suid", studyDataset.getString(Tags.StudyInstanceUID), "--Suid", seriesDataset.getString(Tags.SeriesInstanceUID)};
                executor.submit(new SeriesRetriever(moveArg, studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), studyDataset.getInteger(Tags.NumberOfStudyRelatedInstances), ser_Iter == 0, true, true));
            } else if (inputArgumentValues.getRetrieveType().equals("C-GET")) {
                ArrayList<String> cgetParamList = new ArrayList<String>();
                cgetParamList.add(url.getCalledAET() + "@" + url.getHost() + ":" + url.getPort());
                cgetParamList.add("-L " + ApplicationContext.listenerDetails[0]);
                cgetParamList.add("-cget");
                cgetParamList.add("-I");
                cgetParamList.add("-qStudyInstanceUID=" + studyDataset.getString(Tags.StudyInstanceUID));
                cgetParamList.add("-qSeriesInstanceUID=" + seriesDataset.getString(Tags.SeriesInstanceUID));
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

                executor.submit(new SeriesRetriever(cGetParam, studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), studyDataset.getInteger(Tags.NumberOfStudyRelatedInstances), ser_Iter == 0, true, false));
            }
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            if (inputArgumentValues.getXmlFilePath() != null) {
                readXMLFile();
            } else {
                query(inputArgumentValues);
            }
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.INFO, "Direct Launch - Unable to launch study", ex);
        }
        return null;
    }

    public void sortInstances(Vector<Dataset> datasets) {
        Collections.sort(datasets, new Comparator<Dataset>() {
            @Override
            public int compare(Dataset o1, Dataset o2) {
                try {
                    final int instanceNo1 = o1.getInteger(Tags.InstanceNumber);
                    final int instanceNo2 = o2.getInteger(Tags.InstanceNumber);
                    return (instanceNo1 == instanceNo2 ? 0 : (instanceNo1 > instanceNo2 ? 1 : -1));
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
                return 0;
            }
        });
    }

    public void sortSeries(Vector<Dataset> datasets) {
        Collections.sort(datasets, new Comparator<Dataset>() {
            @Override
            public int compare(Dataset o1, Dataset o2) {
                try {
                    final int seriesNo1 = o1.getInteger(Tags.SeriesNumber);
                    final int seriesNo2 = o2.getInteger(Tags.SeriesNumber);
                    return (seriesNo1 == seriesNo2 ? 0 : (seriesNo1 > seriesNo2 ? 1 : -1));
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
                return 0;
            }
        });
    }
}