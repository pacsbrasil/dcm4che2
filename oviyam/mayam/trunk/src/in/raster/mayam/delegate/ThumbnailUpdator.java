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


import in.raster.mayam.util.DicomTags;
import in.raster.mayam.util.DicomTagsReader;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.form.ThumbnailImage;
import in.raster.mayam.form.WindowingLayeredCanvas;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.Study;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ThumbnailUpdator extends Thread {

    private String studyUID;
    private String seriesUID;
    private MainScreen mainScreenRef;
    private boolean canRun = true;
    public static final int thumbnailHeight = 128;
    public static final int thumbnailWidth = 128;
    private int totalInstance;

    public ThumbnailUpdator() {
    }

    public ThumbnailUpdator(String studyUID, String seriesUId, MainScreen mainScreenRef) {
        super("Thumbnail");
        this.studyUID = studyUID;
        this.seriesUID = seriesUId;
        this.mainScreenRef = mainScreenRef;
        this.start();
    }

    public void run() {   
        if(canRun)
        showThumbnailDisplay();
    }

    public boolean isCanRun() {
        return canRun;
    }

    public void setCanRun(boolean canRun) {
        this.canRun = canRun;
    }

    private void setThumbnailDisplayPanelSize(int totalInstance) {
        this.totalInstance=totalInstance;
        int thumbInSingleRow = (int) Math.ceil(mainScreenRef.getThumbnailDisplay().getSize().getWidth() / thumbnailWidth);
        int rowCount = totalInstance / thumbInSingleRow;
        int thumbDisplayPanelHeight = rowCount * thumbnailHeight;
        int thumbDisplayPanelWidth = (int) mainScreenRef.getThumbnailDisplay().getSize().getWidth();
        System.out.println("thumbnail panel width"+thumbDisplayPanelWidth+"hieght"+thumbDisplayPanelHeight);
        mainScreenRef.getThumbnailDisplay().setPreferredSize(new Dimension(thumbDisplayPanelWidth, thumbDisplayPanelHeight));
        calculateCurrentInstanceTobeShown();

    }
    public void showThumbnailDisplay() {
        int i=0;
        mainScreenRef.getThumbnailDisplay().setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));        
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                tempStudy = study;                
            }
        }
        if (tempStudy != null) {
            for (Series series : tempStudy.getSeriesList()) {
                if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                    setThumbnailDisplayPanelSize(series.getImageList().size());
                    
                    for (Instance instance : series.getImageList()) {
                         if (canRun) {
                final ThumbnailImage thumbnail = new ThumbnailImage(instance);
                thumbnail.setSize(128, 128);
                thumbnail.setVisible(true);
                if (i == 0) {
                    loadImageOnWindowingPanel(thumbnail.getFileUrl());
                    thumbnail.requestFocus();
                    ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(thumbnail.getFileUrl()));
                    MainScreen.dicomTagsViewer.setDataModelOnTable(dcmTags);
                }
                thumbnail.addMouseListener(new MouseListener() {              

                    public void mousePressed(MouseEvent e) {
                        loadImageOnWindowingPanel(thumbnail.getFileUrl());
                        ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(thumbnail.getFileUrl()));
                        MainScreen.dicomTagsViewer.setDataModelOnTable(dcmTags);
                    }

                    public void mouseReleased(MouseEvent e) {
                    }

                    public void mouseEntered(MouseEvent e) {
                    }

                    public void mouseExited(MouseEvent e) {
                    }

                    public void mouseClicked(MouseEvent e) {
                    }
                });
                if (canRun) {
                    mainScreenRef.getThumbnailDisplay().add(thumbnail);
                    mainScreenRef.getThumbnailDisplay().revalidate();
                    mainScreenRef.getThumbnailDisplay().repaint();
                }
            }
                     i++;
                    }
                }
            }
        }
        System.gc();
        System.gc();
    }
    
    int currentInstance=0;
    int instancePointer=19;
    Study tempStudy=null;
    Series tempSeries=null;
    public void showNextOrPreviousInstance()
    {
        instancePointer=instancePointer+20;
         for (;currentInstance<instancePointer;currentInstance++) {
                         if (canRun) {
                final ThumbnailImage thumbnail = new ThumbnailImage(tempSeries.getImageList().get(currentInstance));
                thumbnail.setSize(128, 128);
                thumbnail.setVisible(true);
                thumbnail.addMouseListener(new MouseListener() {
                    public void mousePressed(MouseEvent e) {
                        loadImageOnWindowingPanel(thumbnail.getFileUrl());
                        ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(thumbnail.getFileUrl()));
                        MainScreen.dicomTagsViewer.setDataModelOnTable(dcmTags);
                    }
                    public void mouseReleased(MouseEvent e) {
                    }

                    public void mouseEntered(MouseEvent e) {
                    }

                    public void mouseExited(MouseEvent e) {
                    }

                    public void mouseClicked(MouseEvent e) {
                    }
                });
                if (canRun) {
                    mainScreenRef.getThumbnailDisplay().add(thumbnail);
                    mainScreenRef.getThumbnailDisplay().revalidate();
                    mainScreenRef.getThumbnailDisplay().repaint();
                }
            }
                    
                    }
    }
    public void calculateCurrentInstanceTobeShown()
    {
        int thumbInSingleRow = (int) Math.ceil(mainScreenRef.getThumbnailDisplay().getSize().getWidth() / thumbnailWidth);
        int rowCount = totalInstance / thumbInSingleRow;

        int thumbDisplayPanelHeight = rowCount * thumbnailHeight;
        int thumbDisplayPanelWidth = (int) mainScreenRef.getThumbnailDisplay().getSize().getWidth();

        int viewPortHeight=mainScreenRef.getThumbnailScroll().getViewport().getHeight();
        int viewPortWidth=mainScreenRef.getThumbnailScroll().getViewport().getWidth();

        int scrollBarPosition=mainScreenRef.getThumbnailScroll().getVerticalScrollBar().getValue();

        int firstThumbnailTobeDisplayed;
        int lastThumbnailTobeDisplayed;

        System.out.println("view port height"+mainScreenRef.getThumbnailScroll().getViewport().getHeight());
        System.out.println("maximum"+mainScreenRef.getThumbnailScroll().getVerticalScrollBar().getMaximum());
        System.out.println("Value"+mainScreenRef.getThumbnailScroll().getVerticalScrollBar().getValue());
    }

    public void showThumbnailDisplay(ArrayList tempRef) {

        mainScreenRef.getThumbnailDisplay().setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        //  mainScreenRef.getThumbnailDisplay().setPreferredSize(new Dimension(, MIN_PRIORITY));
        setThumbnailDisplayPanelSize(tempRef.size());
        for (int i = 0; i < tempRef.size(); i++) {
            if (canRun) {
                File file = (File) tempRef.get(i);

                //  final ThumbnailImage thumbnail = new ThumbnailImage(file.getAbsolutePath());
                DestinationFinder destFinder = new DestinationFinder();
                final ThumbnailImage thumbnail = new ThumbnailImage(destFinder.getFileDestination(file));
                thumbnail.setSize(128, 128);
                thumbnail.setVisible(true);
                if (i == 0) {
                    loadImageOnWindowingPanel(destFinder.getFileDestination(file));
                    thumbnail.requestFocus();
                    ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(thumbnail.getFileUrl()));
                    MainScreen.dicomTagsViewer.setDataModelOnTable(dcmTags);
                }
                thumbnail.addMouseListener(new MouseListener() {
                    //listener methods for the thumbnail panel

                    public void mousePressed(MouseEvent e) {
                        loadImageOnWindowingPanel(thumbnail.getFileUrl());                     
                        ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(thumbnail.getFileUrl()));
                        MainScreen.dicomTagsViewer.setDataModelOnTable(dcmTags);
                    }
                    
                    public void mouseReleased(MouseEvent e) {
                    }

                    public void mouseEntered(MouseEvent e) {
                    }

                    public void mouseExited(MouseEvent e) {
                    }

                    public void mouseClicked(MouseEvent e) {
                    }
                });
                if (canRun) {
                    mainScreenRef.getThumbnailDisplay().add(thumbnail);
                    mainScreenRef.getThumbnailDisplay().revalidate();
                    mainScreenRef.getThumbnailDisplay().repaint();
                }
            }
        }
        System.gc();
        System.gc();
    }

    public void loadImageOnWindowingPanel(String url) {
        for (int i = mainScreenRef.getWindowingPanelCanvas().getComponentCount() - 1; i >= 0; i--) {
            mainScreenRef.getWindowingPanelCanvas().remove(i);
        }
        GridLayout g = new GridLayout(1, 1);
        WindowingLayeredCanvas canvas = new WindowingLayeredCanvas(url);
        canvas.imgpanel.tool = "windowing";
        mainScreenRef.setCanvas(canvas);
        mainScreenRef.getWindowingPanelCanvas().setLayout(g);
        mainScreenRef.getWindowingPanelCanvas().add(canvas);
        mainScreenRef.getWindowingPanelCanvas().revalidate();
        mainScreenRef.getWindowingPanelCanvas().repaint();
    }
}
