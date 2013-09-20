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
package in.raster.mayam.context;

import in.raster.mayam.delegates.CommunicationDelegate;
import in.raster.mayam.delegates.ImageBuffer;
import in.raster.mayam.delegates.ImageGenerator;
import in.raster.mayam.delegates.LocalCineTimer;
import in.raster.mayam.delegates.RetrieveDelegate;
import in.raster.mayam.delegates.ShowImageView;
import in.raster.mayam.facade.Platform;
import in.raster.mayam.form.ImageView;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.form.ViewerPreviewPanel;
import in.raster.mayam.models.QueryInformation;
import in.raster.mayam.models.SeriesDisplayModel;
import in.raster.mayam.models.treetable.TreeTable;
import in.raster.mayam.util.database.DatabaseHandler;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ApplicationContext {
    //Reference Objects and Variables    

    public static MainScreen mainScreenObj = null;
    public static Locale currentLocale = null;
    public static ResourceBundle currentBundle = null;
    public static DatabaseHandler databaseRef = DatabaseHandler.getInstance();
    public static CommunicationDelegate communicationDelegate = new CommunicationDelegate();
    public static RetrieveDelegate retrieveDelegate = new RetrieveDelegate();
    public static String applicationName = "Mayam2";
    public static String[] listenerDetails;
    public static String activeTheme = null;
    public static ArrayList<JButton> searchButtons = new ArrayList<JButton>();//To keep track of instant query buttons
    public static String currentServer;
    public static DcmURL currentQueryUrl;
    public static Font textFont = new Font("Lucida Grande", Font.BOLD, 12);
    public static Font labelFont = new Font("Lucida Grande", Font.BOLD, 14);
    //To keep track of studies without request
    public static java.util.Timer timer = new java.util.Timer();
    public static LocalCineTimer cineTimer = null;
    public static int noOfInstancesReceived = 0;
    public static int lastInstanceCount = -1;
    public static ArrayList<String> studiesInProgress = null;
    public static JLabel appNameLabel = null;
    public static JButton moreButton = null;
    public static boolean serverStarted = false;
    public static boolean isLocal = true, isJnlp = false;
    public static TreeTable currentTreeTable = null;
    public static ArrayList<QueryInformation> queryInformations = new ArrayList<QueryInformation>();//To preserve the studies found and selected button details of the server
    //For viewer
    public static ImageView imgView = null;
    public static JPanel selectedPanel = null;
    public static LayeredCanvas layeredCanvas = null;
    public static JTabbedPane tabbedPane;
    public static ImageBuffer imgBuffer = null;
    public static ImageGenerator imageUpdator = null;

    public static String getAppDirectory() {
        return Platform.getAppDirectory(applicationName).getAbsolutePath();
    }

    public static void setAppLocale() {
        databaseRef.openOrCreateDB();
        String[] appLocale = databaseRef.getActiveLanguage();
        currentLocale = new Locale(appLocale[2], appLocale[0]);
        currentBundle = ResourceBundle.getBundle("in/raster/mayam/form/i18n/Bundle", currentLocale);
    }

    /*
     * Used to disable the quick search buttons when the local tab was selected
     */
    public static void setButtonsDisabled() {
        for (int i = 0; i < searchButtons.size(); i++) {
            searchButtons.get(i).setVisible(false);
        }
    }

    /*
     * Will creates the ImageView Form
     */
    public static void createImageView() {
        imgView = new ImageView();
        GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (screenDevices.length > 1) {
            imgView.setLocation((screenDevices[0].getDisplayMode().getWidth()) + 1, 0);
            imgView.setSize(screenDevices[1].getDisplayMode().getWidth(), screenDevices[1].getDisplayMode().getHeight());
        }
        imgView.setVisible(true);
    }

    /*
     * Returns True if the ImageView form exist
     */
    public static boolean isImageViewExist() {
        if (imgView != null) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Sets the RED Identification for the series that were displayed in the
     * Viewer
     */
    public static void setSeriesContext() {
        try {
            //To uncolor the series which were not selected
            SeriesDisplayModel selectedStudy = imgView.selectedSeriesDisplays.get(imgView.selectedStudy);
            for (int i = 0; i < selectedStudy.getAllSeriesDisplays().length; i++) {
                selectedStudy.getAllSeriesDisplays()[i].clearAllSelectedColor();
            }
            //To identify the selected study and to color the labels according to the series in viewer component
            for (int i = 0; i < imgView.selectedSeriesDisplays.size(); i++) { //Contains all studies opened in JtabbedPane
                if (layeredCanvas.imgpanel.getStudyUID().equals(imgView.selectedSeriesDisplays.get(i).getStudyUid())) { //Matches study by study uid                                
                    selectedStudy = imgView.selectedSeriesDisplays.get(i); //Sets the selected study  
                    selectedStudy.setSelectedSeriesDisplays(null);
                    ArrayList<ViewerPreviewPanel> selectedSeriesList = new ArrayList<ViewerPreviewPanel>();
                    JPanel panel = ((JPanel) ((JSplitPane) tabbedPane.getSelectedComponent()).getRightComponent());
                    for (int j = 0; j < panel.getComponentCount(); j++) { //Decides the number of layout components
                        if (((JPanel) panel.getComponent(j)).getComponent(0) instanceof LayeredCanvas) { //The viewer may also contain other components 
                            try {
                                String seriesUid = ((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0)).imgpanel.getSeriesUID();//To match the series by series uid              
                                String sopUid = ((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0)).imgpanel.getInstanceUidIfMultiframe(); //To match the Multiframes since the Multiframes were treated as a separate series
                                for (int m = 0; m < selectedStudy.getAllSeriesDisplays().length; m++) {
                                    if (seriesUid.equals(selectedStudy.getAllSeriesDisplays()[m].getSeriesInstanceUid())) { // Matches the series by series uid                                    
                                        if (selectedStudy.getAllSeriesDisplays()[m].isMultiframe() && selectedStudy.getAllSeriesDisplays()[m].getSopUid().equals(sopUid)) { // If Multiframe was selected
                                            selectedSeriesList.add(selectedStudy.getAllSeriesDisplays()[m]);//sets the selected series
                                        } else if (selectedStudy.getAllSeriesDisplays()[m].getSopUid() == null && sopUid == null) { //If the series was not Multiframe
                                            selectedSeriesList.add(selectedStudy.getAllSeriesDisplays()[m]);//sets the selected series                                                                      
                                        }
                                    }
                                }
                                selectedStudy.setSelectedSeriesDisplays(selectedSeriesList); //Used to set series identification further for individual instances
                            } catch (NullPointerException npe) {
//                            Null pointer exception occures when there is no image to display in the number of layouts
                            }
                        }
                    }
                    //To color the labels
                    if (!imgView.getImageToolbar().isImageLayout) {
                        for (int j = 0; j < panel.getComponentCount(); j++) { //Decides the number of layout components
                            if (((JPanel) panel.getComponent(j)).getComponent(0) instanceof LayeredCanvas) {
                                try {
                                    for (int m = 0; m < selectedStudy.getSelectedSeriesDisplays().size(); m++) {
                                        if (selectedStudy.getSelectedSeriesDisplays().get(m).getSeriesInstanceUid().equals(((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0)).imgpanel.getSeriesUID())) {
                                            selectedStudy.getSelectedSeriesDisplays().get(m).setSelectedInstance(((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0)).imgpanel.getCurrentInstanceNo());//Colors the labels of selected instance 
                                        }
                                    }
                                } catch (NullPointerException npe) {
                                }
                            }
                        }
                    } else {
                        setImageIdentification();
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            //ignore [Index out of bounds will occurs when the selected study not yet determined]
        }
    }

    /*
     * Creates New tab for the study in the Image View using
     * ShowImageViewDelegate
     */
    public static boolean openImageView(String filePath, String studyUid, String[] patientInfo, int startBufferingFrom) {
        if (!isImageViewExist()) {
            createImageView();
        }
        ShowImageView showImageViewDelegate = new ShowImageView(filePath, studyUid, patientInfo, startBufferingFrom);
        return showImageViewDelegate.isAlreadyOpenedStudy();
    }

    public static void openVideo(String filePath, String studyUid, String[] patientInfo) {
        if (!isImageViewExist()) {
            createImageView();
        }
        ShowImageView showImageViewDelegate = new ShowImageView(filePath, studyUid, patientInfo);
    }

    public static void applyLocaleChange() {
        appNameLabel.setText(ApplicationContext.currentBundle.getString("MainScreen.appNameLabel.text"));
        if (moreButton != null) {
            moreButton.setText(ApplicationContext.currentBundle.getString("MainScreen.moreButton.text"));
        }
    }

    public static void setImageIdentification() { // Colors the selected instances to red        
        try {
            ArrayList<ViewerPreviewPanel> selectedSeriesDisplays = ApplicationContext.imgView.selectedSeriesDisplays.get(imgView.selectedStudy).getSelectedSeriesDisplays();
            JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
            for (int i = 0; i < selectedSeriesDisplays.size(); i++) {
                if (!selectedSeriesDisplays.get(i).isMultiframe()) {
                    selectedSeriesDisplays.get(i).clearSelectedInstances();
                    try {
                        for (int j = 0; j < panel.getComponentCount(); j++) {
                            JPanel seriesLevelPanel = (JPanel) panel.getComponent(j);
                            for (int k = 0; k < seriesLevelPanel.getComponentCount(); k++) {
                                if (seriesLevelPanel.getComponent(k) instanceof LayeredCanvas && selectedSeriesDisplays.get(i).getSeriesInstanceUid().equals(((LayeredCanvas) seriesLevelPanel.getComponent(k)).imgpanel.getSeriesUID()) && ((LayeredCanvas) seriesLevelPanel.getComponent(k)).imgpanel.isVisible() && ((LayeredCanvas) seriesLevelPanel.getComponent(k)).imgpanel.getInstanceUidIfMultiframe() == null) {
                                    selectedSeriesDisplays.get(i).setSelectedInstance(((LayeredCanvas) seriesLevelPanel.getComponent(k)).imgpanel.getCurrentInstanceNo());
                                }
                            }
                        }
                    } catch (NullPointerException ex) {
//                        System.out.println("Null pointer exception in setImageIdentifiaction() : " + ex.getMessage());
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            //ignore [Index out of bounds will occurs when the selected study not yet determined]
        }
    }

    public static void setVideoThumbnailIdentification() {
        try {
            JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
            //To uncolor the series which were not selected
            SeriesDisplayModel selectedStudy = imgView.selectedSeriesDisplays.get(imgView.selectedStudy);
            for (int i = 0; i < selectedStudy.getAllSeriesDisplays().length; i++) {
                selectedStudy.getAllSeriesDisplays()[i].clearAllSelectedColor();
            }
            //To identify the selected study and to color the labels according to the series in viewer component
            for (int i = 0; i < imgView.selectedSeriesDisplays.size(); i++) { //Contains all studies opened in JtabbedPane
                if (panel.getName().equals(imgView.selectedSeriesDisplays.get(i).getStudyUid())) {
                    selectedStudy = imgView.selectedSeriesDisplays.get(i);
                    selectedStudy.setSelectedSeriesDisplays(null);
                    ArrayList<ViewerPreviewPanel> selectedSeriesList = new ArrayList<ViewerPreviewPanel>();
                    for (int j = 0; j < selectedStudy.getAllSeriesDisplays().length; j++) {
                        if (selectedStudy.getAllSeriesDisplays()[j].getSopUid() != null && selectedStudy.getAllSeriesDisplays()[j].getSopUid().equals(((JPanel) panel.getComponent(0)).getName())) {
                            selectedStudy.getAllSeriesDisplays()[j].setSelectedInstance(0);
                            selectedSeriesList.add(selectedStudy.getAllSeriesDisplays()[j]);
                            selectedStudy.setSelectedSeriesDisplays(selectedSeriesList);
                            break;
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            //ignore [Index out of bounds will occurs when the selected study not yet determined]
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}