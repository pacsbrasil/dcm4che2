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
import in.raster.mayam.form.ImagePreviewPanel;
import in.raster.mayam.form.ViewerPreviewPanel;
import in.raster.mayam.models.InputArgumentValues;
import in.raster.mayam.models.Series;
import in.raster.mayam.models.ServerModel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.UID;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class JNLPSeriesRetriever {

    InputArgumentValues inputArgumentValues = null;
    ExecutorService executor = Executors.newFixedThreadPool(3);

    public JNLPSeriesRetriever(InputArgumentValues inputArgumentValues) {
        this.inputArgumentValues = inputArgumentValues;
        run();
    }

    public void run() {
        doQuery(inputArgumentValues);
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
        ApplicationContext.ImageView(studyDataset.getString(Tags.PatientName), studyDataset.getString(Tags.StudyInstanceUID), imagePreviewPanel);
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
                    createPreviews(dataset, imagePreviewPanel, multiframes + 1, dataset.getInteger(Tags.NumberOfSeriesRelatedInstances) - multiframes);
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
            // ignore the parse exception
        }
        try {
            patientInfo[3] = displayDateFormat.format(date) + " " + displayTimeFormat.format(time);
        } catch (NullPointerException nullPtr) {
            patientInfo[3] = "Unknown";
        }
        patientInfo[4] = String.valueOf(dataset.getInteger(Tags.NumberOfStudyRelatedSeries) + " Series");
        return patientInfo;
    }
}
