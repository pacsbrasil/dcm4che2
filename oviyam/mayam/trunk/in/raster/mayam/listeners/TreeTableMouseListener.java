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
import in.raster.mayam.delegates.ConstructThumbnails;
import in.raster.mayam.delegates.QueryInstanceService;
import in.raster.mayam.form.ImagePreviewPanel;
import in.raster.mayam.form.PreviewPanel;
import in.raster.mayam.form.Thumbnail;
import in.raster.mayam.models.*;
import in.raster.mayam.models.treetable.TreeTable;
import in.raster.mayam.models.treetable.TreeTableModelAdapter;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class TreeTableMouseListener extends MouseAdapter {

    //To format the display of date
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    DateFormat timeFormat = new SimpleDateFormat("hhmmss");
    DateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    DateFormat displayTimeFormat = new SimpleDateFormat("hh:mm:ss");
    //New variables
    private TreeTable treeTable = null;
    private ImagePreviewPanel imagePreviewPanel = null;
    private boolean wasDoubleClick = false;
    String retrieveType;
    QueryInstanceService queryInstanceService = new QueryInstanceService();
    StudyModel studyDetails = null;
    int pos = 0, size = 0;
    ArrayList<Series> allSeriesOfStudy = null;
    ArrayList<InstanceDisplayModel> instanceModels = null;
    InstanceDisplayModel[] threeInstances = null;
    Thumbnail[] threeThumbnails = null;
    String[] patientInfo;
    Timer timer;
    ServerModel serverDetails = null;
    ArrayList<String> sopUidList = null;

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
            patientInfo = constructPatientInfo();
            setPatientInfo();
            serverDetails = ApplicationContext.databaseRef.getServerDetails(ApplicationContext.currentServer);
            if (e.getClickCount() == 2) {
                wasDoubleClick = true;
                if (ApplicationContext.isLocal) {
                    String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(studyDetails.getStudyUID(), allSeriesOfStudy.get(0).getSeriesInstanceUID());
                    ApplicationContext.openImageView(filePath, studyDetails.getStudyUID(), patientInfo, 0);
                    showPreviews();
                } else {
                    CursorController.createListener(ApplicationContext.mainScreenObj, previewsThread).run();
                    Runnable retrieveThread = new Runnable() {
                        @Override
                        public void run() {
                            retrieveType = ApplicationContext.databaseRef.getRetrieveType(ApplicationContext.currentServer);
                            retrieveStudyToLocal();
                        }
                    };
                    retrieveThread.run();
                }
            } else {
                if (imagePreviewPanel != null) {
                    Integer timeInterval = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
                    timer = new Timer(timeInterval, CursorController.createListener(ApplicationContext.mainScreenObj, singleClickPreviewListener));
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        } catch (NullPointerException npe) {
            //Ignore - Null pointer exception occurs when user clicks the series information
        }
    }

    public String[] constructPatientInfo() {
        String labelInfo[] = new String[5];
        pos = 0;
        size = 0;
        labelInfo[0] = studyDetails.getPatientName();
        labelInfo[1] = studyDetails.getPatientId();
        labelInfo[2] = studyDetails.getStudyDescription();
        Date date = null, time = null;
        try {
            date = dateFormat.parse(studyDetails.getStudyDate().replace("-", ""));
            time = timeFormat.parse(studyDetails.getStudyTime().replace(":", ""));
        } catch (ParseException ex) {
            // ignore the parse exception
        }
        try {
            labelInfo[3] = displayDateFormat.format(date) + " " + displayTimeFormat.format(time);
        } catch (NullPointerException nullPtr) {
            labelInfo[3] = "Unknown";
        }
        labelInfo[4] = allSeriesOfStudy.size() + " Series";
        return labelInfo;
    }

    private void constructSeriesByQuery() {
        Series currentSeries;
        sopUidList = new ArrayList<String>();
        for (int series = 0; series < allSeriesOfStudy.size(); series++) {
            currentSeries = allSeriesOfStudy.get(series);
            instanceModels = new ArrayList<InstanceDisplayModel>();
            queryInstanceService.callFindWithQuery(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), ApplicationContext.currentQueryUrl);
            Vector datasetVector = queryInstanceService.getDatasetVector();
            for (int dataset = 0; dataset < datasetVector.size(); dataset++) {
                Dataset instanceData = (Dataset) datasetVector.elementAt(dataset);
                if (!sopUidList.contains(instanceData.getString(Tags.SOPClassUID))) {
                    sopUidList.add(instanceData.getString(Tags.SOPClassUID));
                }
                if (instanceData.getInteger(Tags.NumberOfFrames) != null) {
                    InstanceDisplayModel[] threeMultiframeInstances = new InstanceDisplayModel[1];
                    currentSeries.setMultiframe(true);
                    currentSeries.setInstanceUID(instanceData.getString(Tags.SOPInstanceUID));
                    InstanceDisplayModel instance = new InstanceDisplayModel(currentSeries.getSeriesInstanceUID(), instanceData.getString(Tags.SOPInstanceUID), instanceData.getInteger(Tags.InstanceNumber));
                    threeMultiframeInstances[0] = instance;
                    ApplicationContext.retrieveDelegate.wado(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), instanceData.getString(Tags.SOPInstanceUID), serverDetails);
                    constructSeriesPanel(currentSeries, "Multiframe", instanceData.getInteger(Tags.NumberOfFrames), threeMultiframeInstances);
                } else {
                    instanceModels.add(new InstanceDisplayModel(currentSeries.getSeriesInstanceUID(), instanceData.getString(Tags.SOPInstanceUID), instanceData.getInteger(Tags.InstanceNumber)));
                }
            }
            if (instanceModels.size() > 0) {
                sortInstances(instanceModels);
                if (instanceModels.size() >= 3) {
                    ApplicationContext.retrieveDelegate.wado(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), instanceModels.get(0).getIuid(), serverDetails);
                    ApplicationContext.retrieveDelegate.wado(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), instanceModels.get(instanceModels.size() / 2).getIuid(), serverDetails);
                    ApplicationContext.retrieveDelegate.wado(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), instanceModels.get(instanceModels.size() - 1).getIuid(), serverDetails);
                    threeInstances = new InstanceDisplayModel[3];
                    threeInstances[0] = instanceModels.get(0);
                    threeInstances[1] = instanceModels.get(instanceModels.size() / 2);
                    threeInstances[2] = instanceModels.get(instanceModels.size() - 1);
                } else {
                    threeInstances = new InstanceDisplayModel[instanceModels.size()];
                    for (int i = 0; i < instanceModels.size(); i++) {
                        ApplicationContext.retrieveDelegate.wado(studyDetails.getPatientId(), studyDetails.getStudyUID(), currentSeries.getSeriesInstanceUID(), instanceModels.get(i).getIuid(), serverDetails);
                        threeInstances[i] = instanceModels.get(i);
                    }
                }
                constructSeriesPanel(currentSeries, currentSeries.getSeriesDesc(), instanceModels.size(), threeInstances);
            }
        }
    }

    private void sortInstances(ArrayList<InstanceDisplayModel> instanceModels) {
        Collections.sort(instanceModels, new Comparator() {
            @Override
            public int compare(Object t, Object t1) {
                InstanceDisplayModel i1 = (InstanceDisplayModel) t;
                InstanceDisplayModel i2 = (InstanceDisplayModel) t1;

                if (new Integer(i1.getInstanceNo()) == null) {
                    return (-1);
                } else if (new Integer(i2.getInstanceNo()) == null) {
                    return (1);
                } else {
                    int a = i1.getInstanceNo();
                    int b = i2.getInstanceNo();
                    int temp = (a == b ? 0 : (a > b ? 1 : -1));
                    return temp;
                }
            }
        });
    }

    public void constructSeriesPanel(Series series, String seriesDescription, int totalImgaes, InstanceDisplayModel[] instanceDisplayModels) {
        if (!"PR".equals(series.getModality())) {
            final PreviewPanel preview = new PreviewPanel(studyDetails.getStudyUID(), series.getSeriesInstanceUID(), seriesDescription, totalImgaes, instanceDisplayModels);
            preview.setVisible(true);
            int height = preview.getTotalHeight();
            size += height + 5;
            imagePreviewPanel.addPreviewPanel(pos, height, preview, size);
            pos += height + 5;
        }
    }

    public void constructSeriesPanelFromLocalDB(Series series, String seriesDescription, int totalImgaes) {
        if (!"PR".equals(series.getModality())) {
            final PreviewPanel preview = new PreviewPanel(studyDetails.getStudyUID(), series.getSeriesInstanceUID(), seriesDescription, totalImgaes, threeThumbnails);
            preview.setVisible(true);
            int height = preview.getTotalHeight();
            size += height + 5;
            imagePreviewPanel.addPreviewPanel(pos, height, preview, size);
            pos += height + 5;
        }
    }

    private void retrieveStudyToLocal() {
        if (retrieveType.equalsIgnoreCase("C-Move")) {
            ApplicationContext.retrieveDelegate.cMove(studyDetails.getPatientId(), studyDetails.getStudyUID(), studyDetails.getModalitiesInStudy(), allSeriesOfStudy, Integer.parseInt(studyDetails.getStudyLevelInstances()), patientInfo, ApplicationContext.currentQueryUrl);
        } else if (retrieveType.equalsIgnoreCase("C-GET")) {
            if (imagePreviewPanel == null) {
                addSopList();
            }
            ApplicationContext.retrieveDelegate.cGet(studyDetails.getPatientId(), studyDetails.getStudyUID(), studyDetails.getModalitiesInStudy(), allSeriesOfStudy, Integer.parseInt(studyDetails.getStudyLevelInstances()), patientInfo, ApplicationContext.currentQueryUrl, sopUidList);
        } else if (retrieveType.equalsIgnoreCase("WADO")) {
            ApplicationContext.retrieveDelegate.wado(studyDetails.getPatientId(), studyDetails.getStudyUID(), studyDetails.getModalitiesInStudy(), allSeriesOfStudy, Integer.parseInt(studyDetails.getStudyLevelInstances()), patientInfo, ApplicationContext.currentServer, ApplicationContext.currentQueryUrl);
        }
    }

    private void showPreviews() {
        if (imagePreviewPanel != null && imagePreviewPanel.parent.getComponentCount() == 0 || !studyDetails.getStudyUID().equals(imagePreviewPanel.getPreviousStudyUid()) || imagePreviewPanel.parent.getComponentCount() != allSeriesOfStudy.size()) {
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
        File file;
        String dest;
        ConstructThumbnails constructThumbnails = null;
        for (int i = 0; i < allSeriesOfStudy.size(); i++) {
            List<Instance> imageList = allSeriesOfStudy.get(i).getImageList();
            constructThumbnails = new ConstructThumbnails(studyDetails.getStudyUID(), allSeriesOfStudy.get(i).getSeriesInstanceUID());
            if (!ApplicationContext.databaseRef.isLink(studyDetails.getStudyUID())) {
                dest = new File(imageList.get(0).getFilepath()).getParent() + File.separator + "Thumbnails";
            } else {
                dest = ApplicationContext.getAppDirectory() + File.separator + "Thumbnails";
            }
            if (imageList.size() >= 3) {
                threeThumbnails = new Thumbnail[3];
                try {
                    file = new File(dest + File.separator + imageList.get(0).getSop_iuid());
                    threeThumbnails[0] = new Thumbnail(imageList.get(0).getSop_iuid(), ImageIO.read(file));
                } catch (IOException ex) {
                    threeThumbnails[0] = new Thumbnail(imageList.get(0).getSop_iuid(), null);
                }
                try {
                    file = new File(dest + File.separator + imageList.get(imageList.size() / 2).getSop_iuid());
                    threeThumbnails[1] = new Thumbnail(imageList.get(imageList.size() / 2).getSop_iuid(), ImageIO.read(file));
                } catch (IOException ex) {
                    threeThumbnails[1] = new Thumbnail(imageList.get(imageList.size() / 2).getSop_iuid(), null);
                }
                try {
                    file = new File(dest + File.separator + imageList.get(imageList.size() - 1).getSop_iuid());
                    threeThumbnails[2] = new Thumbnail(imageList.get(imageList.size() - 1).getSop_iuid(), ImageIO.read(file));
                } catch (IOException ex) {
                    threeThumbnails[2] = new Thumbnail(imageList.get(imageList.size() - 1).getSop_iuid(), null);
                }

            } else {
                threeThumbnails = new Thumbnail[imageList.size()];
                for (int j = 0; j < imageList.size(); j++) {
                    file = new File(dest + File.separator + imageList.get(j).getSop_iuid());
                    try {
                        threeThumbnails[j] = new Thumbnail(imageList.get(j).getSop_iuid(), ImageIO.read(file));
                    } catch (IOException ex) {
                        threeThumbnails[j] = new Thumbnail(imageList.get(j).getSop_iuid(), null);
                    }
                }
            }
            constructSeriesPanelFromLocalDB(allSeriesOfStudy.get(i), allSeriesOfStudy.get(i).getSeriesDesc(), imageList.size());
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
            ApplicationContext.mainScreenObj.setProgressText("Downloading");
            ApplicationContext.mainScreenObj.initializeProgressBar(Integer.parseInt(studyDetails.getStudyLevelInstances()) + Integer.parseInt(studyDetails.getNumberOfSeries()));
        }
    };

    public void addSopList() {
        Series currentSeries;
        sopUidList = new ArrayList<String>();
        for (int series = 0; series < allSeriesOfStudy.size(); series++) {
            currentSeries = allSeriesOfStudy.get(series);
            instanceModels = new ArrayList<InstanceDisplayModel>();
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
}
