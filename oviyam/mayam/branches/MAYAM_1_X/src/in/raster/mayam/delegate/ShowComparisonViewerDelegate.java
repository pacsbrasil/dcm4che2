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
package in.raster.mayam.delegate;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.tab.component.ButtonTabComp;
import in.raster.mayam.form.Canvas;
import in.raster.mayam.form.ImagePanel;
import in.raster.mayam.form.LayeredCanvas;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author  BabuHussain
 * @version 0.9
 *
 */
public class ShowComparisonViewerDelegate extends Thread {

    private ArrayList tempRef;
    private int gridRowCount=1;
    private int gridColCount=2;
    private int numberOfStudies;
    private String[] studies;
    public ShowComparisonViewerDelegate(int numberOfStudies,String[] studies,ArrayList studyURLs) {
        this.numberOfStudies=numberOfStudies;
        if((numberOfStudies>=3) && (numberOfStudies<=4))
        {
            gridColCount=2;
            gridRowCount=2;
        }
        tempRef=studyURLs;
        this.studies=studies;
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
        String patientNameLabel="";
        for (int i = 0; i < (gridColCount * gridRowCount); i++) {
            if (i < tempRef.size()) {
                File file = (File) tempRef.get(i);
                LayeredCanvas canvas = new LayeredCanvas(file.getAbsolutePath());
                canvas.setComparedWithStudies(studies);
                container.add(canvas, i);
                if(i!=0)
                    patientNameLabel+=" - ";
                patientNameLabel+=canvas.imgpanel.getTextOverlayParam().getPatientName();

            } else {
                LayeredCanvas j = new LayeredCanvas();
                j.setStudyUID(studies[0]);
                container.add(j, i);
            }
        }
         container.setName(patientNameLabel);
        setSelectedImagePanel(container);
        zoom(container); 
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

    public String[] getStudies() {
        return studies;
    }

    public void setStudies(String[] studies) {
        this.studies = studies;
    }
    
    private void zoom(JPanel container) {
        for(int i=0;i<container.getComponentCount();i++){
            ImagePanel imgPanel = ((LayeredCanvas) ((JPanel) container).getComponent(i)).imgpanel;
            if(imgPanel!=null){
                imgPanel.setScaleFactor(container.getComponent(i).getWidth(), container.getComponent(i).getHeight());
            }           
        }
    }  

   
}
