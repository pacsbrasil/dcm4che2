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
import java.util.logging.Level;
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
    StudyModel studyDetails = null;
    ArrayList<Series> allSeriesOfStudy = null;
    String[] patientInfo;
    Timer timer;
    ServerModel serverDetails = null;
    ArrayList<String> sopUidList = null;
//    HashMap<String, HashSet<String>> instances = null;
    HashMap<String, NavigableMap<Integer, String>> instances = null;
    HashSet<String> videoInstances = null;
    HashSet<String> multiframesInstances = null;
    ExecutorService executor = Executors.newFixedThreadPool(3);
    int pos = 0, size = 0;

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
            studyDetails = (StudyModel) ((TreeTableModelAdapter) treeTable.getModel()).getValueAt(treeTable.rowAtPoint(e.getPoint()), 11);
            allSeriesOfStudy = (ArrayList<Series>) ((TreeTableModelAdapter) treeTable.getModel()).getValueAt(treeTable.getSelectedRow(), 12);
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
                            String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(studyDetails.getStudyUID(), allSeriesOfStudy.get(0).getSeriesInstanceUID());
                            ApplicationContext.createCanvas(filePath, studyDetails.getStudyUID(), 0);
                            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).displayPreviews();
                            ApplicationContext.createMultiframePreviews(studyDetails.getStudyUID());
                            showPreviews();
                        }
                    });
                } else {
                    CursorController.createListener(ApplicationContext.mainScreenObj, previewsThread).run();
                    Runnable retrieveThread = new Runnable() {
                        @Override
                        public void run() {
                            retrieveStudyToLocal();
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
                if (!studyUid.equals(imagePreviewPanel.getPreviousStudyUid())) {
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
        labelInfo[0] = studyDetails.getPatientName();
        labelInfo[1] = "ID: " + studyDetails.getPatientId();
        labelInfo[2] = studyDetails.getStudyDescription();
        labelInfo[3] = ((TreeTableModelAdapter) treeTable.getModel()).getValueAt(row, 6) + " " + ((StudyModel) ((TreeTableModelAdapter) treeTable.getModel()).getValueAt(row, 11)).getStudyTime();
        labelInfo[4] = allSeriesOfStudy.size() + " Series";
        return labelInfo;
    }

    private void constructSeriesByQuery() {
//        instances = new HashMap<String, HashSet<String>>(0, 5);
        instances = new HashMap<String, NavigableMap<Integer, String>>(0, 5);
        videoInstances = new HashSet<String>(0, 1);
        multiframesInstances = new HashSet<String>(0, 1);
        sopUidList = new ArrayList<String>();
        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
        for (int i = 0; i < allSeriesOfStudy.size(); i++) {
            NavigableMap<Integer, String> wadoInstances = new TreeMap<Integer, String>();
            int multiframes = 0;
            Thumbnail[] threeThumbnails = null;
            Series currentSeries = allSeriesOfStudy.get(i);
            queryInstanceService.callFindWithQuery(studyDetails.getPatientId(), studyDetails.getStudyUID(), allSeriesOfStudy.get(i).getSeriesInstanceUID(), ApplicationContext.currentQueryUrl);
            for (int j = 0; j < queryInstanceService.getDatasetVector().size(); j++) {
                Dataset instanceData = (Dataset) queryInstanceService.getDatasetVector().get(j);
                if (!sopUidList.contains(instanceData.getString(Tags.SOPClassUID))) {
                    sopUidList.add(instanceData.getString(Tags.SOPClassUID));
                }
                if (instanceData.getInteger(Tags.NumberOfFrames) == null) {
                    wadoInstances.put(instanceData.getInteger(Tags.InstanceNumber) != null ? instanceData.getInteger(Tags.InstanceNumber) : 0, instanceData.getString(Tags.SeriesInstanceUID) + "," + instanceData.getString(Tags.SOPInstanceUID));
                } else {
                    if (instanceData.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || instanceData.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) {
                        currentSeries.setVideoStatus(true);
                        threeThumbnails = new Thumbnail[1];
                        threeThumbnails[0] = new Thumbnail(instanceData.getString(Tags.SOPInstanceUID));
                        constructSeriesPanel(currentSeries, "Video:" + instanceData.getString(Tags.NumberOfFrames) + " Frames", 1, threeThumbnails);
                        videoInstances.add(instanceData.getString(Tags.SeriesInstanceUID) + "," + instanceData.getString(Tags.SOPInstanceUID) + "," + instanceData.getString(Tags.NumberOfFrames));
                        multiframes++;
                    } else {
                        threeThumbnails = new Thumbnail[1];
                        currentSeries.setMultiframe(true);
                        currentSeries.setInstanceUID(instanceData.getString(Tags.SOPInstanceUID));
                        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyDetails.getStudyUID() + File.separator + currentSeries.getSeriesInstanceUID() + File.separator + "Thumbnails" + File.separator + instanceData.getString(Tags.SOPInstanceUID);
                        String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyDetails.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesInstanceUID() + "&objectUID=" + instanceData.getString(Tags.SOPInstanceUID) + "&rows=75" + "&columns=75";
                        threeThumbnails[0] = new Thumbnail(wadoRequest, dest, instanceData.getString(Tags.SOPInstanceUID));
                        constructSeriesPanel(currentSeries, "Multiframe:" + instanceData.getString(Tags.NumberOfFrames) + " Frames", 1, threeThumbnails);
                        multiframesInstances.add(instanceData.getString(Tags.SeriesInstanceUID) + "," + instanceData.getString(Tags.SOPInstanceUID) + "," + instanceData.getString(Tags.NumberOfFrames));
                        multiframes++;
                    }
                }
            }
            if (multiframes < currentSeries.getSeriesRelatedInstance()) {
                currentSeries.setSeriesRelatedInstance(currentSeries.getSeriesRelatedInstance() - multiframes);
                if (wadoInstances.size() > 3) {
                    threeThumbnails = new Thumbnail[3];
                    String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyDetails.getStudyUID() + File.separator + currentSeries.getSeriesInstanceUID() + File.separator + "Thumbnails" + File.separator + wadoInstances.firstEntry().getValue().split(",")[1];
                    String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyDetails.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesInstanceUID() + "&objectUID=" + wadoInstances.firstEntry().getValue().split(",")[1] + "&rows=75" + "&columns=75";
                    threeThumbnails[0] = new Thumbnail(wadoRequest, dest, wadoInstances.firstEntry().getValue().split(",")[1]);
                    try {
                        dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyDetails.getStudyUID() + File.separator + currentSeries.getSeriesInstanceUID() + File.separator + "Thumbnails" + File.separator + wadoInstances.get(wadoInstances.lastKey() / 2).split(",")[1];
                        wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyDetails.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesInstanceUID() + "&objectUID=" + wadoInstances.get(wadoInstances.lastKey() / 2).split(",")[1] + "&rows=75" + "&columns=75";
                        threeThumbnails[1] = new Thumbnail(wadoRequest, dest, wadoInstances.get(wadoInstances.lastKey() / 2).split(",")[1]);

                    } catch (NullPointerException ex) {
                        threeThumbnails[1] = new Thumbnail(null);
                        ApplicationContext.logger.log(Level.INFO, "TreeTableMouseListeber - Unable to construct middle image.", ex);
                    }
                    dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyDetails.getStudyUID() + File.separator + currentSeries.getSeriesInstanceUID() + File.separator + "Thumbnails" + File.separator + wadoInstances.get(wadoInstances.lastKey()).split(",")[1];
                    wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyDetails.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesInstanceUID() + "&objectUID=" + wadoInstances.get(wadoInstances.lastKey()).split(",")[1] + "&rows=75" + "&columns=75";
                    threeThumbnails[2] = new Thumbnail(wadoRequest, dest, wadoInstances.get(wadoInstances.lastKey()).split(",")[1]);
                } else {
                    threeThumbnails = new Thumbnail[wadoInstances.size()];
                    int k = 0;
                    Set<Integer> keySet = wadoInstances.keySet();
                    Iterator<Integer> iterator = keySet.iterator();
                    while (iterator.hasNext()) {
                        int key = iterator.next();
                        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyDetails.getStudyUID() + File.separator + currentSeries.getSeriesInstanceUID() + File.separator + "Thumbnails" + File.separator + wadoInstances.get(key).split(",")[1];
                        String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyDetails.getStudyUID() + "&seriesUID=" + currentSeries.getSeriesInstanceUID() + "&objectUID=" + wadoInstances.get(key).split(",")[1] + "&rows=75" + "&columns=75";
                        threeThumbnails[k] = new Thumbnail(wadoRequest, dest, wadoInstances.get(key).split(",")[1]);
                        k++;
                    }
                }
                constructSeriesPanel(currentSeries, currentSeries.getSeriesDesc(), wadoInstances.size(), threeThumbnails);
//                HashSet<String> instancesToRetrieve = new HashSet<String>(0, 1);
//                instancesToRetrieve.addAll(wadoInstances.values());
//                instances.put(currentSeries.getSeriesInstanceUID(), instancesToRetrieve);
                instances.put(currentSeries.getSeriesInstanceUID(), wadoInstances);
            }
        }
    }

    public void constructSeriesPanel(Series series, String seriesDescription, int totalImgaes, Thumbnail[] threeThumbnails) {
        if (!"PR".equals(series.getModality())) {
            final PreviewPanel preview = new PreviewPanel(studyDetails.getStudyUID(), series.getSeriesInstanceUID(), seriesDescription, totalImgaes, threeThumbnails);
            preview.setVisible(true);
            int height = preview.getTotalHeight();
            size += height + 5;
            imagePreviewPanel.addPreviewPanel(pos, height, preview, size);
            pos += height + 5;
        }
    }

    private void showPreviews() {
        if (imagePreviewPanel != null && imagePreviewPanel.parent.getComponentCount() == 0 || !studyDetails.getStudyUID().equals(imagePreviewPanel.getPreviousStudyUid()) || imagePreviewPanel.parent.getComponentCount() < allSeriesOfStudy.size()) {
            imagePreviewPanel.setPreviousStudyUid(studyDetails.getStudyUID());
            imagePreviewPanel.resetPreviewPanel();
            if (ApplicationContext.isLocal) {
                constructSeriesByDB();
            } else {
                constructSeriesByQuery();
            }
        }
    }

    private void constructSeriesByDB() {
        for (int i = 0; i < allSeriesOfStudy.size(); i++) {
            Thumbnail[] threeThumbnails = null;
            String dest = null;
            if (!allSeriesOfStudy.get(i).isVideo()) {
                if (!allSeriesOfStudy.get(i).isMultiframe()) {
                    ArrayList<String> imageList = ApplicationContext.databaseRef.getInstancesLoc(studyDetails.getStudyUID(), allSeriesOfStudy.get(i).getSeriesInstanceUID());
                    dest = imageList.get(0).contains(ApplicationContext.appDirectory) ? imageList.get(0).substring(0, imageList.get(0).lastIndexOf(File.separator)) + File.separator + "Thumbnails" : ApplicationContext.appDirectory + File.separator + "Thumbnails" + File.separator + studyDetails.getStudyUID();
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
                    String fileLocation = ApplicationContext.databaseRef.getFileLocation(studyDetails.getStudyUID(), allSeriesOfStudy.get(i).getSeriesInstanceUID(), allSeriesOfStudy.get(i).getInstanceUID());
                    dest = fileLocation.contains(ApplicationContext.appDirectory) ? fileLocation.substring(0, fileLocation.lastIndexOf(File.separator)) + File.separator + "Thumbnails" : ApplicationContext.appDirectory + File.separator + "Thumbnails" + File.separator + studyDetails.getStudyUID();
                    threeThumbnails = new Thumbnail[1];
                    threeThumbnails[0] = new Thumbnail(fileLocation + "," + allSeriesOfStudy.get(i).getInstanceUID(), dest);
                }
            } else {
                threeThumbnails = new Thumbnail[1];
                threeThumbnails[0] = new Thumbnail(allSeriesOfStudy.get(i).getInstanceUID());
            }
            constructSeriesPanel(allSeriesOfStudy.get(i), allSeriesOfStudy.get(i).getSeriesDesc(), allSeriesOfStudy.get(i).getSeriesRelatedInstance(), threeThumbnails);
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
        Series currentSeries;
        sopUidList = new ArrayList<String>();
        for (int series = 0; series < allSeriesOfStudy.size(); series++) {
            currentSeries = allSeriesOfStudy.get(series);
            queryInstanceService.callFindWithQuery(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), ApplicationContext.currentQueryUrl);
            Vector datasetVector = queryInstanceService.getDatasetVector();
            for (int dataset = 0; dataset < datasetVector.size(); dataset++) {
                Dataset instanceData = (Dataset) datasetVector.elementAt(dataset);
                if (!sopUidList.contains(instanceData.getString(Tags.SOPClassUID))) {
                    sopUidList.add(instanceData.getString(Tags.SOPClassUID));
                }
            }
        }
    }

    private void retrieveStudyToLocal() {
        if (!ApplicationContext.databaseRef.checkRecordExists("study", "StudyInstanceUID", studyDetails.getStudyUID())) {
            ApplicationContext.mainScreenObj.setProgressText("Downloading");
            ApplicationContext.mainScreenObj.initializeProgressBar(Integer.parseInt(studyDetails.getStudyLevelInstances()));
            final ImagePreviewPanel viewerPreview = new ImagePreviewPanel();
            viewerPreview.setPatientInfo(patientInfo);
            final boolean previewsEnabled = ApplicationContext.databaseRef.isPreviewsEnabled(serverDetails.getDescription());
            if (!previewsEnabled) {
                doInstanceLevelQuery();
            }
            ApplicationContext.openImageView(patientInfo[0], studyDetails.getStudyUID(), viewerPreview);
            SwingWorker swingWorker = new SwingWorker() {
                @Override
                protected Void doInBackground() {
                    try {
                        int position = 0, totalSize = 0;
                        for (int i = 0; i < allSeriesOfStudy.size(); i++) {
                            Series curr = allSeriesOfStudy.get(i);
                            if (!curr.isVideo()) {
                                if (curr.isMultiframe()) {
                                    Iterator<String> iterator = multiframesInstances.iterator();
                                    while (iterator.hasNext()) {
                                        String next = iterator.next();
                                        if (curr.getSeriesInstanceUID().equals(next.split(",")[0])) {
                                            ViewerPreviewPanel multiframePanel = new ViewerPreviewPanel();
//                                            multiframePanel.multiframePreview(studyDetails.getStudyUID(), curr, next.substring(next.indexOf(",", 0) + 1));                                            
                                            multiframePanel.multiframePreview(studyDetails.getStudyUID(), curr.getSeriesInstanceUID(), next.substring(next.indexOf(",", 0) + 1));
                                            multiframePanel.setVisible(true);
                                            multiframePanel.setName(String.valueOf(i));
                                            int h = multiframePanel.getTotalHeight();
                                            totalSize += h + 5;
                                            multiframePanel.setBounds(0, position, 230, h);
                                            viewerPreview.addViewerPanel(position, h, multiframePanel, totalSize);
                                            position += (h + 5);
                                        }
                                    }
                                } else {
                                    ViewerPreviewPanel viewerPreviewPanel = new ViewerPreviewPanel();
                                    viewerPreviewPanel.normalPreview(studyDetails.getStudyUID(), curr.getSeriesInstanceUID(), curr.getSeriesDesc(), curr.getSeriesRelatedInstance());
                                    viewerPreviewPanel.loadThumbnails(instances.get(curr.getSeriesInstanceUID()).values(), serverDetails);
                                    viewerPreviewPanel.setVisible(true);
                                    viewerPreviewPanel.setName(String.valueOf(i));
                                    int height = viewerPreviewPanel.getTotalHeight();
                                    totalSize += height + 5;
                                    viewerPreviewPanel.setBounds(0, position, 230, height);
                                    viewerPreview.addViewerPanel(position, height, viewerPreviewPanel, totalSize);
                                    position += (height + 5);
                                }

                            } else {
                                Iterator<String> iterator = videoInstances.iterator();
                                while (iterator.hasNext()) {
                                    String next = iterator.next();
                                    if (next.split(",")[0].equals(curr.getSeriesInstanceUID())) {
                                        ViewerPreviewPanel viewerPreviewPanel = new ViewerPreviewPanel();
                                        viewerPreviewPanel.videoPreview(studyDetails.getStudyUID(), curr.getSeriesInstanceUID(), next.substring(next.indexOf(",", 0) + 1));
                                        viewerPreviewPanel.setVisible(true);
                                        viewerPreviewPanel.setName(String.valueOf(i));
                                        int height = viewerPreviewPanel.getTotalHeight();
                                        totalSize += height + 5;
                                        viewerPreviewPanel.setBounds(0, position, 230, height);
                                        viewerPreview.addViewerPanel(position, height, viewerPreviewPanel, totalSize);
                                        position += (height + 5);
                                    }
                                }
                            }
                        }
                        if (serverDetails.getRetrieveType().equals("WADO")) {
                            WadoRetriever wadoRetriever = new WadoRetriever();
                            if (!multiframesInstances.isEmpty()) {
                                if (!previewsEnabled) {
                                    wadoRetriever.reteriveMultiframeThumbnails(studyDetails.getStudyUID(), serverDetails, multiframesInstances);
                                }
                                wadoRetriever.reteriveVideoAndMultiframes(studyDetails.getStudyUID(), serverDetails, multiframesInstances, instances.isEmpty());
                            }
                            if (!videoInstances.isEmpty()) {
                                wadoRetriever.reteriveVideoAndMultiframes(studyDetails.getStudyUID(), serverDetails, videoInstances, instances.isEmpty());
                            }
                        }

                        boolean firstSeries = true;
                        for (int i = 0; i < viewerPreview.parent.getComponentCount(); i++) {
                            String seriesUID = null;
                            if (serverDetails.getRetrieveType().equals("WADO")) {
                                seriesUID = ((ViewerPreviewPanel) viewerPreview.parent.getComponent(i)).load(previewsEnabled);
                                try {
                                    executor.submit(new WadoRetriever(studyDetails.getStudyUID(), seriesUID, instances.get(seriesUID).values(), serverDetails, true, firstSeries, Integer.parseInt(studyDetails.getStudyLevelInstances())));
                                } catch (NullPointerException ex) {
                                    if (!previewsEnabled) {
                                        ((ViewerPreviewPanel) viewerPreview.parent.getComponent(i)).displayMultiframes();
                                    }
                                }
                            } else {
                                if (previewsEnabled) {
                                    seriesUID = ((ViewerPreviewPanel) viewerPreview.parent.getComponent(i)).load(previewsEnabled);
                                } else {
                                    seriesUID = ((ViewerPreviewPanel) viewerPreview.parent.getComponent(i)).getSeriesInstanceUid();
                                }
                                if (serverDetails.getRetrieveType().equals("C-MOVE")) {
                                    executor.submit(new SeriesRetriever(new String[]{ApplicationContext.currentQueryUrl.getProtocol() + "://" + ApplicationContext.currentQueryUrl.getCalledAET() + "@" + ApplicationContext.currentQueryUrl.getHost() + ":" + ApplicationContext.currentQueryUrl.getPort(), "--dest", ApplicationContext.listenerDetails[0], "--pid", patientInfo[1], "--suid", studyDetails.getStudyUID(), "--Suid", seriesUID}, studyDetails.getStudyUID(), seriesUID, Integer.parseInt(studyDetails.getStudyLevelInstances()), firstSeries, !previewsEnabled, true));
                                } else if (serverDetails.getRetrieveType().equals("C-GET")) {
                                    executor.submit(new SeriesRetriever(constructCGetParam(seriesUID), studyDetails.getStudyUID(), seriesUID, Integer.parseInt(studyDetails.getStudyLevelInstances()), firstSeries, !previewsEnabled, false));
                                }
                            }
                            firstSeries = false;
                        }

                    } catch (Exception ex) {
                        ApplicationContext.logger.log(Level.INFO, "TreeTableMouseListener", ex);
                    }
                    return null;
                }
            };
            swingWorker.execute();
        } else if (ApplicationContext.mainScreenObj.getCurrentProgressValue() == 0) {
            ApplicationContext.mainScreenObj.hideProgressBar();
        }
    }

    private String[] constructCGetParam(String seriesInstanceUid) {
        if (imagePreviewPanel == null) {
            addSopClassList();
        }
        ArrayList<String> cgetParam = new ArrayList<String>();
        cgetParam.add(ApplicationContext.currentQueryUrl.getCalledAET() + "@" + ApplicationContext.currentQueryUrl.getHost() + ":" + ApplicationContext.currentQueryUrl.getPort());
        cgetParam.add("-L " + ApplicationContext.listenerDetails[0]);
        cgetParam.add("-cget");
        cgetParam.add("-I");
        cgetParam.add("-qStudyInstanceUID=" + studyDetails.getStudyUID());
        cgetParam.add("-qSeriesInstanceUID=" + seriesInstanceUid);
        for (int i = 0; i < sopUidList.size(); i++) {
            cgetParam.add("-cstore");
            cgetParam.add(sopUidList.get(i) + ":" + UID.ExplicitVRLittleEndian);
            if (sopUidList.contains(UID.VideoEndoscopicImageStorage) || sopUidList.contains(UID.VideoMicroscopicImageStorage) || sopUidList.contains(UID.VideoPhotographicImageStorage)) {
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG2);
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG2MainProfileHighLevel);
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG4AVCH264HighProfileLevel41);
                cgetParam.add(",");
                cgetParam.add(sopUidList.get(i) + ":" + UID.MPEG4AVCH264BDCompatibleHighProfileLevel41);
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
        for (int i = 0; i < allSeriesOfStudy.size(); i++) {
            Series curr = allSeriesOfStudy.get(i);
            if (!curr.isVideo()) {
                ViewerPreviewPanel viewerPreviewPanel = null;
                if (curr.getSeriesDesc().contains("Multiframe")) {
                    viewerPreviewPanel = new ViewerPreviewPanel(studyDetails.getStudyUID(), curr, curr.getInstanceUID() + "," + curr.getSeriesRelatedInstance());
                } else {
                    viewerPreviewPanel = new ViewerPreviewPanel(studyDetails.getStudyUID(), curr, null);
                }
                viewerPreviewPanel.setVisible(true);
                viewerPreviewPanel.setName(String.valueOf(i));
                int height = viewerPreviewPanel.getTotalHeight();
                totalSize += height + 5;
                viewerPreviewPanel.setBounds(0, position, 230, height);
                viewerPreview.addViewerPanel(position, height, viewerPreviewPanel, totalSize);
                position += (height + 5);
            } else {
                ViewerPreviewPanel viewerPreviewPanel = new ViewerPreviewPanel(studyDetails.getStudyUID(), curr, curr.getInstanceUID() + "," + curr.getSeriesRelatedInstance());
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
        ApplicationContext.openImageView(patientInfo[0], studyDetails.getStudyUID(), viewerPreview);
    }

    private void doInstanceLevelQuery() {
        instances = new HashMap<String, NavigableMap<Integer, String>>(0, 5);
        videoInstances = new HashSet<String>(0, 1);
        multiframesInstances = new HashSet<String>(0, 1);

        QueryInstanceService queryInstanceService = new QueryInstanceService();
        for (int i = 0; i < allSeriesOfStudy.size(); i++) {
            Series currentSeries = allSeriesOfStudy.get(i);
            NavigableMap<Integer, String> instanceList = new TreeMap<Integer, String>();
            queryInstanceService.callFindWithQuery(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), ApplicationContext.currentQueryUrl);
            for (int j = 0; j < queryInstanceService.getDatasetVector().size(); j++) {
                Dataset dataset = (Dataset) queryInstanceService.getDatasetVector().get(j);
                if (dataset.getInteger(Tags.NumberOfFrames) != null) {
                    if (dataset.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || dataset.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || dataset.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) {
                        currentSeries.setVideoStatus(true);
                        videoInstances.add(dataset.getString(Tags.SeriesInstanceUID) + "," + dataset.getString(Tags.SOPInstanceUID) + "," + dataset.getString(Tags.NumberOfFrames));
                    } else {
                        currentSeries.setMultiframe(true);
                        multiframesInstances.add(dataset.getString(Tags.SeriesInstanceUID) + "," + dataset.getString(Tags.SOPInstanceUID) + "," + dataset.getString(Tags.NumberOfFrames));
                    }
                } else {
                    instanceList.put(dataset.getInteger(Tags.InstanceNumber), dataset.getString(Tags.SeriesInstanceUID) + "," + dataset.getString(Tags.SOPInstanceUID));
                }
            }
            currentSeries.setSeriesRelatedInstance(currentSeries.getSeriesRelatedInstance() - multiframesInstances.size() - videoInstances.size());

            if (!instanceList.isEmpty()) {
                instances.put(currentSeries.getSeriesInstanceUID(), instanceList);
            }
        }
    }
}