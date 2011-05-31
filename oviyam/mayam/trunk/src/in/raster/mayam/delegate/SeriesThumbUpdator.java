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
import in.raster.mayam.util.DicomTags;
import in.raster.mayam.util.DicomTagsReader;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.form.SeriesPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class SeriesThumbUpdator{

    private String studyUID;
    private boolean canRun = true;
    public static final int thumbnailHeight = 100;
    public static final int thumbnailWidth = 100;

    public SeriesThumbUpdator(String studyUID) {
        this.studyUID = studyUID;
        run();
    }

    public void run() {
        showThumbnailDisplay();
    }

    public boolean isCanRun() {
        return canRun;
    }

    public void setCanRun(boolean canRun) {
        this.canRun = canRun;
    }

    /**
     * This routine used to set the thumbnail display panel size.
     * @param totalInstance
     */
    private void setThumbnailDisplayPanelSize(int totalInstance) {
        MainScreen mainScreenRef = ApplicationContext.mainScreen;
        int thumbDisplayPanelHeight = totalInstance * thumbnailHeight;
        int thumbDisplayPanelWidth = (int) mainScreenRef.getThumbnailDisplay().getSize().getWidth();
        mainScreenRef.getThumbnailDisplay().setPreferredSize(new Dimension(thumbDisplayPanelWidth, thumbDisplayPanelHeight));
    }

    /**
     * This routine used to display the thumbnail images in the thumbnail display area.
     */
    public void showThumbnailDisplay() {
        synchronized(this){
        int i = 0;
        MainScreen mainScreenRef = ApplicationContext.mainScreen;
        ApplicationContext.mainScreen.getThumbnailDisplay().setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        ArrayList<File> tempRef = ApplicationContext.databaseRef.getUrlBasedOnStudyIUID(studyUID);
        setThumbnailDisplayPanelSize(tempRef.size());
        for (File f : tempRef) {
            final SeriesPanel thumbnail = new SeriesPanel(f.getAbsolutePath());
            thumbnail.setPreferredSize(new Dimension((int) mainScreenRef.getThumbnailDisplay().getSize().getWidth() - 1, 96));
            thumbnail.setVisible(true);
            if (i == 0) {
                WindowingPanelLoader.loadImageOnWindowingPanel(thumbnail.getFileUrl());
                if (ApplicationContext.selectedSeriesPanel != null) {
                    ApplicationContext.selectedSeriesPanel.setNoSelectionColoring();
                }
                ApplicationContext.selectedSeriesPanel = thumbnail;
                ApplicationContext.selectedSeriesPanel.setSelectionColoring();
                thumbnail.updateInstanceList();
                if (MainScreen.dicomTagsViewer.isVisible()) {
                    ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(thumbnail.getFileUrl()));
                    MainScreen.dicomTagsViewer.setDataModelOnTable(dcmTags);
                }
            }
            if (canRun) {
                mainScreenRef.getThumbnailDisplay().add(thumbnail);
                mainScreenRef.getThumbnailDisplay().revalidate();
                mainScreenRef.getThumbnailDisplay().repaint();
            }
            i++;
        }
        }
    }
}
