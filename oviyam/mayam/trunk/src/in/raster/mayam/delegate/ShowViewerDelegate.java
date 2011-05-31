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
import in.raster.mayam.form.tab.component.ButtonTabComp;
import in.raster.mayam.form.Canvas;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.model.Series;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ShowViewerDelegate extends Thread {

    private ArrayList tempRef;
    private int gridRowCount;
    private int gridColCount;
    private String studyUID;
    private String modalitiesInStudy;

    public ShowViewerDelegate() {
    }

    public ShowViewerDelegate(String studyUID, ArrayList tempRef, int gridRowCount, int gridColCount,String modalitiesInStudy) {
        this.studyUID = studyUID;
        this.tempRef = tempRef;
        this.gridColCount = gridColCount;
        this.gridRowCount = gridRowCount;
        this.modalitiesInStudy=modalitiesInStudy;
        this.start();
    }

    public void run() {
        showViewer();
    }

    /**
     * This routine used to show the image view with the layout updated based on modality in the database.
     */
    private void showViewer() {
        JPanel container = new JPanel();
        container.setBackground(Color.BLACK);
        GridLayout g = new GridLayout(gridRowCount, gridColCount);
        container.setLayout(g);
        for (int i = 0; i < (gridColCount * gridRowCount); i++) {
            if (i < tempRef.size()) {
                File file = (File) tempRef.get(i);
//                DestinationFinder destFinder=new DestinationFinder();
//                LayeredCanvas canvas=new LayeredCanvas(destFinder.getFileDestination(file));
                LayeredCanvas canvas = new LayeredCanvas(file.getAbsolutePath());
                container.add(canvas, i);
                container.setName(canvas.imgpanel.getTextOverlayParam().getPatientName());
            } else {
                LayeredCanvas j = new LayeredCanvas();
                j.setStudyUID(studyUID);
                container.add(j, i);
            }
        }
        setSelectedImagePanel(container);
//        try{
//        checkPR();
//        }catch(IOException ex){
//            ex.printStackTrace();
//        }
    }
    /**
     * This routine used to check whether PR modality exist in the current study.
     */
   private void checkPR() throws IOException{
       int index=modalitiesInStudy.indexOf("PR");
       if(index==-1)return;
       System.out.println("PR Found.");
       ArrayList<Series> series= ApplicationContext.databaseRef.listAllSeriesOfStudy(studyUID, "PR");
       for (Series series1 : series) {
           ArrayList<String> fileUrlList=ApplicationContext.databaseRef.getSeriesLevelInstanceUrl(studyUID, series1.getSeriesInstanceUID());
           for (String files : fileUrlList) {
               //System.out.println(files);
               File file=new File(files.trim());
               System.out.println(file.getAbsolutePath());
               DicomInputStream dis = new DicomInputStream(file);
               DicomObject dO=dis.readDicomObject();
               dis.close();
           }
       }
   }
    /**
     * This routine used to update the selected panel info to ApplicationContext.
     * @param container
     */
    private void setSelectedImagePanel(JPanel container) {
        ApplicationContext.imgView.jTabbedPane1.add(container);
        //The following lines are used for tab close button and event
        ButtonTabComp tabComp = new ButtonTabComp(ApplicationContext.imgView.jTabbedPane1);
        ApplicationContext.imgView.jTabbedPane1.setTabComponentAt(ApplicationContext.imgView.jTabbedPane1.getTabCount() - 1, tabComp);
        ApplicationContext.imgView.jTabbedPane1.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        ApplicationContext.imgPanel = ((LayeredCanvas) ((JPanel) container).getComponent(0)).imgpanel;
        ApplicationContext.imgView.getImageToolbar().setWindowingTool();
        ApplicationContext.annotationPanel = ((LayeredCanvas) ((JPanel) container).getComponent(0)).annotationPanel;
        ApplicationContext.layeredCanvas = ((LayeredCanvas) ((JPanel) container).getComponent(0));
        ((Canvas) ApplicationContext.imgPanel.getCanvas()).setSelection();
        ApplicationContext.imgView.jTabbedPane1.setSelectedComponent(container);
        ApplicationContext.imgView.getImageToolbar().setWindowing();
        ApplicationContext.imgView.setVisible(true);
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }
}