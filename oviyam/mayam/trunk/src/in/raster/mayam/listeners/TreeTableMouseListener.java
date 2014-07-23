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
package in.raster.mayam.listeners;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegates.QueryInstanceService;
import in.raster.mayam.delegates.SeriesRetriever;
import in.raster.mayam.delegates.WadoRetriever;
import in.raster.mayam.form.ImagePreviewPanel;
import in.raster.mayam.form.PreviewPanel;
import in.raster.mayam.form.Thumbnail;
import in.raster.mayam.form.ViewerJPanel;
import in.raster.mayam.form.ViewerPreviewPanel;
import in.raster.mayam.models.*;
import in.raster.mayam.models.treetable.SeriesNode;
import in.raster.mayam.models.treetable.StudyNode;
import in.raster.mayam.models.treetable.TreeTable;
import in.raster.mayam.models.treetable.TreeTableModelAdapter;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.UID;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class TreeTableMouseListener extends MouseAdapter {
    //New variables

    private TreeTable treeTable = null;
    private ImagePreviewPanel imagePreviewPanel = null;
    private boolean wasDoubleClick = false;
    QueryInstanceService queryInstanceService = new QueryInstanceService();
    String[] patientInfo;
    Timer timer;
    ServerModel serverDetails = null;
    HashSet<String> sopClassList = null;
    ExecutorService executor = Executors.newFixedThreadPool(3);
    int pos = 0, size = 0;
    StudyNode studyNode;

    public TreeTableMouseListener(TreeTable treeTable) {
        this.treeTable = treeTable;
    }

    public TreeTableMouseListener(TreeTable treeTable, ImagePreviewPanel imagePreviewPanel) {
        this.treeTable = treeTable;
        this.imagePreviewPanel = imagePreviewPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            this.studyNode = (StudyNode) ((TreeTableModelAdapter) treeTable.getModel()).getValueAt(treeTable.rowAtPoint(e.getPoint()), 12);
            if (studyNode.isRoot() != 0) {
                patientInfo = constructPatientInfo(treeTable.rowAtPoint(e.getPoint()));
                setPatientInfo();
                serverDetails = ApplicationContext.databaseRef.getServerDetails(ApplicationContext.currentServer);
                if (e.getClickCount() == 2) {
                    wasDoubleClick = true;
                    if (ApplicationContext.isLocal) {
                        createPreviewLocal();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(studyNode.getStudyUID(), studyNode.getFirstChild().getSeriesUID());
                                ApplicationContext.createCanvas(filePath, studyNode.getStudyUID(), 0);
                                ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).displayPreviews();
                                ApplicationContext.createMultiframePreviews(studyNode.getStudyUID());
                                showPreviews();
                            }
                        });
                    } else {
                        CursorController.createListener(ApplicationContext.mainScreenObj, previewsThread).run();
                        Runnable retrieveThread = new Runnable() {
                            @Override
                            public void run() {
                                retrieve();
                            }
                        };
                        retrieveThread.run();
                    }
                } else {
                    if (imagePreviewPanel != null) {
                        if (treeTable.getSelectedRows().length > 1) {
                            imagePreviewPanel.resetImagePreviewPanel();
                        } else {
                            Integer timeInterval = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
                            timer = new Timer(timeInterval, CursorController.createListener(ApplicationContext.mainScreenObj, singleClickPreviewListener));
                            timer.setRepeats(false);
                            timer.start();
                        }
                    }
                }
            }
        } catch (NullPointerException npe) {
            if (treeTable.getSelectedRows().length > 1) {  // To select multiple studies to delete
                imagePreviewPanel.resetImagePreviewPanel();
            } else {
                int i = treeTable.getSelectedRow();
                String studyUid = (String) ((TreeTableModelAdapter) ApplicationContext.currentTreeTable.getModel()).getValueAt(i, 10);
                while (studyUid == null) {
                    i--;
                    studyUid = (String) ((TreeTableModelAdapter) ApplicationContext.currentTreeTable.getModel()).getValueAt(i, 10);
                }
                if (studyNode != null && !studyNode.getStudyUID().equals(imagePreviewPanel.getPreviousStudyUid())) {
                    imagePreviewPanel.resetImagePreviewPanel();
                }
            }
            //Ignore - Null pointer exception occurs when user clicks the series information
        }
    }

    public String[] constructPatientInfo(int row) {
        String labelInfo[] = new String[5];
        pos = 0;
        size = 0;
        labelInfo[0] = studyNode.getPatientName();
        labelInfo[1] = "ID: " + studyNode.getPatientId();
        labelInfo[2] = studyNode.getStudyDescription();
        labelInfo[3] = studyNode.getStudyDate() + " " + studyNode.getStudyTime();
        labelInfo[4] = studyNode.getStudyRelatedSeries() - 1 + " Series";
        return labelInfo;
    }

    private void constructQuery() {
        sopClassList = new HashSet<String>();
        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);

        List<SeriesNode> children = studyNode.getChildren();
        for (int i = 1; i < children.size(); i++) {
            Thumbnail[] threeThumbnails = null;
            SeriesNode currentSeries = children.get(i);
            queryInstanceService.callFindWithQuery(studyNode.getPatientId(), studyNode.getStudyUID(), currentSeries.getSeriesUID(), ApplicationContext.currentQueryUrl);
            Vector<Dataset> instanceDS = queryInstanceService.getDatasetVector();
            Vector<Dataset> multiframesAndVideos = new Vector<Dataset>(0);
            sortInstances(instanceDS);
            for (int j = 0; j < instanceDS.size(); j++) {
                Dataset instanceData = instanceDS.elementAt(j);
                sopClassList.add(instanceData.getString(Tags.SOPClassUID));

                if (instanceData.getString(Tags.NumberOfFrames) != null) { //Multiframe or video                    
                    if (instanceData.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) { //Video
                        threeThumbnails = new Thumbnail[1];
                        threeThumbnails[0] = new Thumbnail(instanceData.getString(Tags.SOPInstanceUID));
                        currentSeries.setVideoStatus(true);
                        constructSeriesPanel(currentSeries, "Video:" + instanceData.getString(Tags.NumberOfFrames) + " Frames", 1, threeThumbnails);
                    } else { //Multiframe
                        threeThumbnails = new Thumbnail[1];
                        currentSeries.setMultiframe(true);
                        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyNode.getStudyUID() + File.separator + currentSeries.getSeriesUID() + File.separator + "Thumbnails" + File.separator + instanceData.getString(Tags.SOPInstanceUID);
                        String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyNode.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesUID() + "&objectUID=" + instanceData.getString(Tags.SOPInstanceUID) + "&rows=75" + "&columns=75";
                        threeThumbnails[0] = new Thumbnail(wadoRequest, dest, instanceData.getString(Tags.SOPInstanceUID));
                        constructSeriesPanel(currentSeries, "Multiframe:" + instanceData.getString(Tags.NumberOfFrames) + " Frames", 1, threeThumbnails);
                        instanceDS.remove(instanceData);
                    }
                    currentSeries.setInstanceUIDIfMultiframe(instanceData.getString(Tags.SOPInstanceUID));
                    multiframesAndVideos.add(instanceData);
                }//if
            } // end of instance iteration
            if (multiframesAndVideos.size() < Integer.parseInt(currentSeries.getSeriesRelatedInstance())) { //Not all instances are multiframe
                currentSeries.setSeriesRelatedInstance(String.valueOf(instanceDS.size()));
                if (instanceDS.size() > 3) {
                    threeThumbnails = new Thumbnail[3];
                    //First Thumbnail
                    String sopUid = instanceDS.get(0).getString(Tags.SOPInstanceUID);
                    String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyNode.getStudyUID() + File.separator + currentSeries.getSeriesUID() + File.separator + "Thumbnails" + File.separator + sopUid;
                    String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyNode.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesUID() + "&objectUID=" + sopUid + "&rows=75" + "&columns=75";
                    threeThumbnails[0] = new Thumbnail(wadoRequest, dest, sopUid);
                    //Middle Thumbnail
                    sopUid = instanceDS.get(instanceDS.size() / 2).getString(Tags.SOPInstanceUID);
                    dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyNode.getStudyUID() + File.separator + currentSeries.getSeriesUID() + File.separator + "Thumbnails" + File.separator + sopUid;
                    wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyNode.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesUID() + "&objectUID=" + sopUid + "&rows=75" + "&columns=75";
                    threeThumbnails[1] = new Thumbnail(wadoRequest, dest, sopUid);
                    //Last Thumbnail
                    sopUid = instanceDS.get(instanceDS.size() - 1).getString(Tags.SOPInstanceUID);
                    dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyNode.getStudyUID() + File.separator + currentSeries.getSeriesUID() + File.separator + "Thumbnails" + File.separator + sopUid;
                    wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyNode.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesUID() + "&objectUID=" + sopUid + "&rows=75" + "&columns=75";
                    threeThumbnails[2] = new Thumbnail(wadoRequest, dest, sopUid);
                } else {
                    threeThumbnails = new Thumbnail[instanceDS.size()];
                    for (int k = 0; k < instanceDS.size(); k++) {
                        String sopUid = instanceDS.get(k).getString(Tags.SOPInstanceUID);
                        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyNode.getStudyUID() + File.separator + currentSeries.getSeriesUID() + File.separator + "Thumbnails" + File.separator + sopUid;
                        String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyNode.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesUID() + "&objectUID=" + sopUid + "&rows=75" + "&columns=75";
                        threeThumbnails[k] = new Thumbnail(wadoRequest, dest, sopUid);
                    }
                }
                constructSeriesPanel(currentSeries, currentSeries.getSeriesDesc(), instanceDS.size(), threeThumbnails);
            }
            currentSeries.setInstances_Data(instanceDS);
            currentSeries.setMultiframesAndVideos(multiframesAndVideos);
        } //end of series iteration

    }

    public void constructSeriesPanel(SeriesNode series, String seriesDescription, int totalImgaes, Thumbnail[] threeThumbnails) {
        if (!"PR".equals(series.getModality())) {
            final PreviewPanel preview = new PreviewPanel(studyNode.getStudyUID(), series.getSeriesUID(), seriesDescription, totalImgaes, threeThumbnails);
            preview.setVisible(true);
            int height = preview.getTotalHeight();
            size += height + 5;
            imagePreviewPanel.addPreviewPanel(pos, height, preview, size);
            pos += height + 5;
        }
    }

    private void showPreviews() {
        if (imagePreviewPanel != null && imagePreviewPanel.parent.getComponentCount() == 0 || !studyNode.getStudyUID().equals(imagePreviewPanel.getPreviousStudyUid()) || imagePreviewPanel.parent.getComponentCount() < studyNode.getChildCount()) {
            imagePreviewPanel.setPreviousStudyUid(studyNode.getStudyUID());
            imagePreviewPanel.resetPreviewPanel();
            if (ApplicationContext.isLocal) {
                constructSeriesByDB();
            } else {
                constructQuery();
            }
        }
    }

    private void constructSeriesByDB() {
        for (int i = 1; i < studyNode.getChildCount(); i++) {
            SeriesNode currentSeries = (SeriesNode) studyNode.getChild(i);

            Thumbnail[] threeThumbnails = null;
            String dest = null;
            if (!currentSeries.isVideo()) {
                if (!currentSeries.isMultiframe()) {
                    ArrayList<String> imageList = ApplicationContext.databaseRef.getInstancesLoc(studyNode.getStudyUID(), currentSeries.getSeriesUID());
                    dest = imageList.get(0).contains(ApplicationContext.appDirectory) ? imageList.get(0).substring(0, imageList.get(0).lastIndexOf(File.separator)) + File.separator + "Thumbnails" : ApplicationContext.appDirectory + File.separator + "Thumbnails" + File.separator + studyNode.getStudyUID();
                    if (imageList.size() >= 3) {
                        threeThumbnails = new Thumbnail[3];
                        threeThumbnails[0] = new Thumbnail(imageList.get(0), dest);
                        threeThumbnails[1] = new Thumbnail(imageList.get(imageList.size() / 2), dest);
                        threeThumbnails[2] = new Thumbnail(imageList.get(imageList.size() - 1), dest);
                    } else {
                        threeThumbnails = new Thumbnail[imageList.size()];
                        for (int k = 0; k < imageList.size(); k++) {
                            threeThumbnails[k] = new Thumbnail(imageList.get(k), dest);
                        }
                    }
                } else {
                    String fileLocation = ApplicationContext.databaseRef.getFileLocation(studyNode.getStudyUID(), currentSeries.getSeriesUID(), currentSeries.getInstanceUIDIfMultiframe());
                    dest = fileLocation.contains(ApplicationContext.appDirectory) ? fileLocation.substring(0, fileLocation.lastIndexOf(File.separator)) + File.separator + "Thumbnails" : ApplicationContext.appDirectory + File.separator + "Thumbnails" + File.separator + studyNode.getStudyUID();
                    threeThumbnails = new Thumbnail[1];
                    threeThumbnails[0] = new Thumbnail(fileLocation + "," + currentSeries.getInstanceUIDIfMultiframe(), dest);
                }
            } else {
                threeThumbnails = new Thumbnail[1];
                threeThumbnails[0] = new Thumbnail(currentSeries.getInstanceUIDIfMultiframe());
            }
            constructSeriesPanel(currentSeries, currentSeries.getSeriesDesc(), Integer.parseInt(currentSeries.getSeriesRelatedInstance()), threeThumbnails);
        }
    }

    private void setPatientInfo() {
        if (imagePreviewPanel != null) {
            imagePreviewPanel.setPatientInfo(patientInfo);
        }
    }
    /*
     * For Cursor Controlling Operations
     */
    ActionListener singleClickPreviewListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (wasDoubleClick) {
                wasDoubleClick = false;
            } else {
                showPreviews();
            }
        }
    };
    Runnable previewsThread = new Runnable() {
        @Override
        public void run() {
            if (imagePreviewPanel != null) {
                showPreviews();
            }
            ApplicationContext.mainScreenObj.setProgressbarVisibility(true);
        }
    };

    public void addSopClassList() {
        SeriesNode currentSeries;
        sopClassList = new HashSet<String>();

        for (int series = 0; series < studyNode.getChildCount(); series++) {
            currentSeries = (SeriesNode) studyNode.getChild(series);
            queryInstanceService.callFindWithQuery(studyNode.getPatientId(), studyNode.getStudyUID(), currentSeries.getSeriesUID(), ApplicationContext.currentQueryUrl);
            Vector datasetVector = queryInstanceService.getDatasetVector();
            for (int dataset = 0; dataset < datasetVector.size(); dataset++) {
                Dataset instanceData = (Dataset) datasetVector.elementAt(dataset);
                sopClassList.add(instanceData.getString(Tags.SOPClassUID));
            }
        }
    }

    private void retrieve() {
        try {
            if (!ApplicationContext.databaseRef.checkRecordExists("study", "StudyInstanceUID", studyNode.getStudyUID())) {
                ApplicationContext.mainScreenObj.setProgressText("Downloading");
                ApplicationContext.mainScreenObj.initializeProgressBar(Integer.parseInt(studyNode.getStudyReleatedInstances()));
                final ImagePreviewPanel viewerPreview = new ImagePreviewPanel();
                viewerPreview.setPatientInfo(patientInfo);

                if (!serverDetails.isPreviewEnabled()) {
                    doInstanceLevelQuery();
                }

                ApplicationContext.openImageView(patientInfo[0], studyNode.getStudyUID(), viewerPreview);
                SwingWorker swingWorker = new SwingWorker() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        int previewID = 0, position = 0, size = 0;
                        for (int i = 1; i < studyNode.getChildCount(); i++) {
                            SeriesNode currentSeries = (SeriesNode) studyNode.getChild(i);
                            Vector<Dataset> instances_Data = currentSeries.getInstances_Data();
                            Vector<Dataset> multiframesAndVideos = currentSeries.getMultiframesAndVideos();
                            if (!currentSeries.isAllMulltiframe()) { //Construct Normal Previews
                                ViewerPreviewPanel normalPreview = new ViewerPreviewPanel(previewID);
                                normalPreview.normalPreview(studyNode.getStudyUID(), currentSeries.getSeriesUID(), currentSeries.getSeriesDesc(), Integer.parseInt(currentSeries.getSeriesRelatedInstance()));
                                int height = normalPreview.getTotalHeight();
                                size += (height + 5);
                                normalPreview.setBounds(0, position, 230, height);
                                viewerPreview.addViewerPanel(position, height, normalPreview, size);
                                position += (height + 5);
                                previewID++;
                                if (serverDetails.isPreviewEnabled() || serverDetails.getRetrieveType().equals("WADO")) {
                                    normalPreview.loadThumbnails(instances_Data, serverDetails.getWadoProtocol(), serverDetails.getHostName(), serverDetails.getWadoPort());
                                    normalPreview.load(false);
                                }
                            }

                            for (int m = 0; m < multiframesAndVideos.size(); m++) {
                                Dataset instanceData = multiframesAndVideos.elementAt(m);
                                if (instanceData.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) { //Video
                                    ViewerPreviewPanel video = new ViewerPreviewPanel(previewID);
                                    video.videoPreview(studyNode.getStudyUID(), currentSeries.getSeriesUID(), instanceData.getString(Tags.SOPInstanceUID) + "," + instanceData.getString(Tags.NumberOfFrames));
                                    int height = video.getTotalHeight();
                                    size += height + 5;
                                    video.setBounds(0, position, 230, height);
                                    viewerPreview.addViewerPanel(position, height, video, size);
                                    position += (height + 5);
                                    previewID++;
                                } else { //Multiframe
                                    ViewerPreviewPanel multiframe = new ViewerPreviewPanel(previewID);
                                    multiframe.multiframePreview(studyNode.getStudyUID(), currentSeries.getSeriesUID(), instanceData.getString(Tags.SOPInstanceUID) + "," + instanceData.getString(Tags.NumberOfFrames));
                                    int height = multiframe.getTotalHeight();
                                    size += height;
                                    multiframe.setBounds(0, position, 230, height);
                                    viewerPreview.addViewerPanel(position, height, multiframe, size);
                                    position += (height + 5);
                                    previewID++;
                                    if (serverDetails.isPreviewEnabled()) {
                                        multiframe.load(true);
                                    } else if (serverDetails.getRetrieveType().equals("WADO")) {
                                        multiframe.loadThumbnail(instanceData, serverDetails.getWadoProtocol(), serverDetails.getHostName(), serverDetails.getWadoPort());
                                    }
                                }
                            }
                            if (serverDetails.getRetrieveType().equals("C-MOVE")) {
                                executor.submit(new SeriesRetriever(new String[]{ApplicationContext.currentQueryUrl.getProtocol() + "://" + ApplicationContext.currentQueryUrl.getCalledAET() + "@" + ApplicationContext.currentQueryUrl.getHost() + ":" + ApplicationContext.currentQueryUrl.getPort(), "--dest", ApplicationContext.listenerDetails[0], "--pid", patientInfo[1], "--suid", studyNode.getStudyUID(), "--Suid", currentSeries.getSeriesUID()}, studyNode.getStudyUID(), currentSeries.getSeriesUID(), Integer.parseInt(studyNode.getStudyReleatedInstances()), i == 1, !serverDetails.isPreviewEnabled(), true));
                            } else if (serverDetails.getRetrieveType().equals("C-GET")) { //C-GET
                                executor.submit(new SeriesRetriever(constructCGetParam(currentSeries.getSeriesUID()), studyNode.getStudyUID(), currentSeries.getSeriesUID(), Integer.parseInt(studyNode.getStudyReleatedInstances()), i == 1, !serverDetails.isPreviewEnabled(), false));
                            } else {
                                executor.submit(new WadoRetriever(studyNode.getStudyUID(), currentSeries.getSeriesUID(), instances_Data, i == 1, serverDetails.getWadoProtocol(), serverDetails.getHostName(), serverDetails.getWadoPort(), serverDetails.getRetrieveTransferSyntax(), Integer.parseInt(studyNode.getStudyReleatedInstances())));
                                executor.submit(new WadoRetriever(studyNode.getStudyUID(), currentSeries.getSeriesUID(), multiframesAndVideos, instances_Data.isEmpty(), serverDetails.getWadoProtocol(), serverDetails.getHostName(), serverDetails.getWadoPort(), serverDetails.getRetrieveTransferSyntax(), Integer.parseInt(studyNode.getStudyReleatedInstances())));
                            }

                        }//end of series iteration
                        return null;
                    }
                };
                swingWorker.execute();
            } else if (ApplicationContext.mainScreenObj.getCurrentProgressValue() == 0) {
                ApplicationContext.mainScreenObj.hideProgressBar();

                createPreviewLocal();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(studyNode.getStudyUID(), studyNode.getFirstChild().getSeriesUID());
                        ApplicationContext.createCanvas(filePath, studyNode.getStudyUID(), 0);
                        ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).displayPreviews();
                        ApplicationContext.createMultiframePreviews(studyNode.getStudyUID());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] constructCGetParam(String seriesInstanceUid) {
        ArrayList<String> cgetParam = new ArrayList<String>();
        cgetParam.add(ApplicationContext.currentQueryUrl.getCalledAET() + "@" + ApplicationContext.currentQueryUrl.getHost() + ":" + ApplicationContext.currentQueryUrl.getPort());
        cgetParam.add("-L " + ApplicationContext.listenerDetails[0]);
        cgetParam.add("-cget");
        cgetParam.add("-I");
        cgetParam.add("-qStudyInstanceUID=" + studyNode.getStudyUID());
        cgetParam.add("-qSeriesInstanceUID=" + seriesInstanceUid);
        Iterator<String> iterator = sopClassList.iterator();
        while (iterator.hasNext()) {
            String sopClassUID = iterator.next();
            cgetParam.add("-cstore");
            cgetParam.add(sopClassUID + ":" + UID.ExplicitVRLittleEndian);
            if (sopClassList.contains(UID.VideoEndoscopicImageStorage) || sopClassList.contains(UID.VideoMicroscopicImageStorage) || sopClassList.contains(UID.VideoPhotographicImageStorage)) {
                cgetParam.add(",");
                cgetParam.add(sopClassUID + ":" + UID.MPEG2);
                cgetParam.add(",");
                cgetParam.add(sopClassUID + ":" + UID.MPEG2MainProfileHighLevel);
                cgetParam.add(",");
                cgetParam.add(sopClassUID + ":" + UID.MPEG4AVCH264HighProfileLevel41);
                cgetParam.add(",");
                cgetParam.add(sopClassUID + ":" + UID.MPEG4AVCH264BDCompatibleHighProfileLevel41);
            }
        }

        cgetParam.add("-cstoredest");
        cgetParam.add(ApplicationContext.listenerDetails[2]);
        return cgetParam.toArray(new String[cgetParam.size()]);
    }

    public void createPreviewLocal() {
        ImagePreviewPanel viewerPreview = new ImagePreviewPanel();
        viewerPreview.setPatientInfo(patientInfo);
        int position = 0, totalSize = 0;
        for (int i = 1; i < studyNode.getChildCount(); i++) {
            SeriesNode curr = (SeriesNode) studyNode.getChild(i);
            if (!curr.isVideo()) {
                ViewerPreviewPanel viewerPreviewPanel = null;
                if (curr.getSeriesDesc().contains("Multiframe")) {
                    viewerPreviewPanel = new ViewerPreviewPanel(studyNode.getStudyUID(), curr, curr.getInstanceUIDIfMultiframe() + "," + curr.getSeriesRelatedInstance());
                } else {
                    viewerPreviewPanel = new ViewerPreviewPanel(studyNode.getStudyUID(), curr, null);
                }
                viewerPreviewPanel.setVisible(true);
                viewerPreviewPanel.setName(String.valueOf(i));
                int height = viewerPreviewPanel.getTotalHeight();
                totalSize += height + 5;
                viewerPreviewPanel.setBounds(0, position, 230, height);
                viewerPreview.addViewerPanel(position, height, viewerPreviewPanel, totalSize);
                position += (height + 5);
            } else {
                ViewerPreviewPanel viewerPreviewPanel = new ViewerPreviewPanel(studyNode.getStudyUID(), curr, curr.getInstanceUIDIfMultiframe() + "," + curr.getSeriesRelatedInstance());
                viewerPreviewPanel.setVisible(true);
                viewerPreviewPanel.setName(String.valueOf(i));
                int height = viewerPreviewPanel.getTotalHeight();
                totalSize += height + 5;
                viewerPreviewPanel.setBounds(0, position, 230, height);
                viewerPreview.addViewerPanel(position, height, viewerPreviewPanel, totalSize);
                position += (height + 5);
                viewerPreviewPanel.loadVideoImage();
            }
        }
        ApplicationContext.openImageView(patientInfo[0], studyNode.getStudyUID(), viewerPreview);
    }

    private void doInstanceLevelQuery() {
        sopClassList = new HashSet<String>();
        for (int i = 1; i < studyNode.getChildCount(); i++) {
            SeriesNode currentSeries = (SeriesNode) studyNode.getChild(i);
            queryInstanceService.callFindWithQuery(studyNode.getPatientId(), studyNode.getStudyUID(), currentSeries.getSeriesUID(), ApplicationContext.currentQueryUrl);
            Vector<Dataset> instanceDS = queryInstanceService.getDatasetVector();
            Vector<Dataset> multiframesAndVideos = new Vector<Dataset>(0);
            sortInstances(instanceDS);
            for (int j = 0; j < instanceDS.size(); j++) {
                Dataset instanceData = instanceDS.elementAt(j);
                sopClassList.add(instanceData.getString(Tags.SOPClassUID));
                if (instanceData.getString(Tags.NumberOfFrames) != null) { //Multiframe or video                    
                    if (instanceData.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) { //Video
                        multiframesAndVideos.add(instanceData);
                    } else {
                        multiframesAndVideos.add(instanceData);
                        instanceDS.remove(instanceData);
                    }
                }
            }
            currentSeries.setInstances_Data(instanceDS);
            currentSeries.setMultiframesAndVideos(multiframesAndVideos);
            currentSeries.setSeriesRelatedInstance(String.valueOf(instanceDS.size()));
        }//end of series iteration
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
}