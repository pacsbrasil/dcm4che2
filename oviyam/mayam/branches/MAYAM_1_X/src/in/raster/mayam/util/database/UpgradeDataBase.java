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
package in.raster.mayam.util.database;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.ImportOnUpgradeDelegate;
import in.raster.mayam.facade.ApplicationFacade;
import in.raster.mayam.model.PresetModel;
import in.raster.mayam.model.ServerModel;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author devishree
 */
public class UpgradeDataBase {

    Connection connection;
    private static final String protocol = "jdbc:derby:";
    private static final String username = "mayam";
    private static final String password = "mayam";
    public static boolean firstTime = true;

    public UpgradeDataBase() {
    }

    public static UpgradeDataBase getInstance() {
        UpgradeDataBase upgradeDataBaseref = new UpgradeDataBase();
        return upgradeDataBaseref;
    }

    public ArrayList<String> getUrlList() {
        ArrayList<String> studyuid = new ArrayList<String>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("select FileStoreUrl from image");
            while (rs.next()) {
                studyuid.add(rs.getString("FileStoreUrl"));
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(UpgradeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return studyuid;
    }

    public void openOlderConnection() {
        try {
            connection = DriverManager.getConnection(protocol + System.getProperty("user.dir") + File.separator + "viewerdb" + ";create=false", username, password);
        } catch (Exception e) {
            System.out.println("Cannot open older connection");
        }
    }

    public void savePreferences() {
        try {
            
            if (connection == null) {
                System.out.println("Connection null");
            }
            //Listener
            ResultSet listener = connection.createStatement().executeQuery("select * from listener");
            while (listener.next()) {
                ApplicationContext.databaseRef.insertListenerDetail(listener.getString("aetitle"), listener.getInt("port"), listener.getString("storagelocation"));
            }

            //Layouts            

            ResultSet layout = connection.createStatement().executeQuery("select * from layout");
            while (layout.next()) {
                ResultSet modalitylist = connection.createStatement().executeQuery("select * from modality where pk=" + layout.getInt("modality_fk"));
                while (modalitylist.next()) {
                    ApplicationContext.databaseRef.insertLayoutDetail(layout.getInt("rowcount"), layout.getInt("columncount"), modalitylist.getString("shortname"));
                }
            }

            //Presets

            ResultSet modality = connection.createStatement().executeQuery("select * from modality");
            while (modality.next()) {
                ResultSet presets = connection.createStatement().executeQuery("select * from preset where modality_fk=" + modality.getInt("pk"));
                while (presets.next()) {
                    PresetModel presetModel = new PresetModel();
                    presetModel.setPresetName(presets.getString("presetname"));
                    presetModel.setWindowWidth(presets.getString("windowwidth"));
                    presetModel.setWindowLevel(presets.getString("windowlevel"));
                    presetModel.setModalityFk(presets.getInt("modality_fk"));
                    ApplicationContext.databaseRef.insertPreset(presetModel, modality.getString("shortname"));
                }
            }

            //Servers
            ResultSet servers = connection.createStatement().executeQuery("select * from ae");
            while (servers.next()) {
                ServerModel servermodel = new ServerModel(servers.getString("logicalname"), servers.getString("hostname"), servers.getString("aetitle"), servers.getInt("port"), "C-GET", "wado", 0, "http", "");
                ApplicationContext.databaseRef.insertServer(servermodel);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UpgradeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<String> getStudyUIDList() {
        ArrayList<String> studyuid = new ArrayList<String>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("select StudyInstanceUID from study");
            while (rs.next()) {
                studyuid.add(rs.getString("StudyInstanceUID"));
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(UpgradeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return studyuid;
    }

    public ArrayList<String> getSeriesUIDList(String studyuid) {
        ArrayList<String> seriesuid = new ArrayList<String>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("select SeriesInstanceUID from series where StudyInstanceUID='" + studyuid + "'");
            while (rs.next()) {
                seriesuid.add(rs.getString("SeriesInstanceUID"));
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(UpgradeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seriesuid;
    }

    public int getSeriesLevelInstance(String studyuid, String seriesuid) {
        int size = 0;
        try {
            String sql1 = "";
            sql1 = "select count(*) from image where StudyInstanceUID='" + studyuid + "' AND " + "SeriesInstanceUID='" + seriesuid + "'";
            ResultSet rs = connection.createStatement().executeQuery(sql1);
            rs.next();
            size = rs.getInt(1);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public ArrayList getUrlBasedOnStudyIUID(String siuid) {
        ArrayList fileArray = new ArrayList();
        try {
            int i = 0;
            String sql = "select SeriesInstanceUID from series where StudyInstanceUID='" + siuid + "'";
            ResultSet rs = null;
            rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                ResultSet rs1 = null;
                String sql1 = "select FileStoreUrl from image where StudyInstanceUID='" + siuid + "' AND " + "SeriesInstanceUID='" + rs.getString("SeriesInstanceUID") + "'" + " order by InstanceNo asc";
                rs1 = connection.createStatement().executeQuery(sql1);
                File imageUrl = null;
                if (rs1.next()) {
                    imageUrl = new File(rs1.getString("FileStoreUrl"));
                }
                fileArray.add(imageUrl);
                rs1.close();
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(UpgradeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileArray;
    }

    public void saveURLs() {
        int instancecount = 0;
        ArrayList<String> studyUIDList = getStudyUIDList();
        for (int i = 0; i < studyUIDList.size(); i++) {
            ArrayList<String> seriesUIDList = getSeriesUIDList(studyUIDList.get(i));
            for (int j = 0; j < seriesUIDList.size(); j++) {
                instancecount = getSeriesLevelInstance(studyUIDList.get(i), seriesUIDList.get(j));
            }
            startImport(getUrlBasedOnStudyIUID(studyUIDList.get(i)), instancecount);
        }
    }

    public void startImport(ArrayList<File> filearray, int instancecount) {
        ImportOnUpgradeDelegate importDelegate;
        if (filearray.get(0).getParent().contains("archive")) {
            if(ApplicationFacade.version7){
                if (UpgradeDataBase.firstTime) {
                    UpgradeDataBase.firstTime = false;
                    File dir = new File(System.getProperty("user.dir") + File.separator + "archive");
                    importDelegate = new ImportOnUpgradeDelegate(dir, true, "Importing archieve...");
                } 
            } else{
                if (UpgradeDataBase.firstTime) {
                    UpgradeDataBase.firstTime = false;
                    File dir = new File(ApplicationContext.getAppDirectory() + File.separator + "archive");
                    importDelegate = new ImportOnUpgradeDelegate(dir, true, "Importing archieve...");
                } 
            }
        } else if (instancecount > 1 && !(filearray.get(0).getParent().contains("archive"))) {
            importDelegate = new ImportOnUpgradeDelegate(filearray.get(0).getParentFile(), true, "Upgrading...");
        } else {
            importDelegate = new ImportOnUpgradeDelegate(filearray.get(0), false, "Upgrading...");
        }
    }
}
