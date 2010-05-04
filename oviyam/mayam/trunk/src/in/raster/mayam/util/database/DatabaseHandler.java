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
package in.raster.mayam.util.database;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.model.AEModel;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.PresetModel;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.ServerModel;
import in.raster.mayam.model.Study;
import in.raster.mayam.model.StudyModel;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.jdbc.EmbeddedSimpleDataSource;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class DatabaseHandler {

    //Named Constants for Database,driver,protocol
    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String protocol = "jdbc:derby:";
    private static final String databasename = "viewerdb";
    // Named Constants for Table name
    private static final String patientTable = "patient";
    private static final String studyTable = "study";
    private static final String seriesTable = "series";
    private static final String instanceTable = "image";
    private static final String aeTitleTable = "aetitles";
    //Named Constants for username and password of Database 
    private static final String username = "mayam";
    private static final String password = "mayam";
    //Database Connection creator and executor variables 
    private Connection conn;
    private Statement statement;
    //Datasouce declaration
    private EmbeddedSimpleDataSource ds;
    //Boolean variables 
    private boolean dbExists = false;
    //Data formater
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String DicomServersHeaderDisplayName = "Dicom Servers";

    public DatabaseHandler() {
    }

    /**it checks whether database available in local folder or not.
     */
    public boolean checkDBexists(String tem) {

        File temp = new File(tem);
        boolean dbexists = false;
        for (int l = 0; l < temp.listFiles().length; l++) {
            if (temp.listFiles()[l].getName().equalsIgnoreCase(
                    databasename)) {
                dbexists = true;
            }
        }
        return dbexists;
    }

    /** It initializes the Java Derby Databse.It creates the database if it is not available. */
    public void initDrivers() {
        try {
            try {
                Class.forName(driver).newInstance();
            } catch (InstantiationException e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
            } catch (IllegalAccessException e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
            } catch (ClassNotFoundException e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
            }
            ds = new org.apache.derby.jdbc.EmbeddedSimpleDataSource();
            this.dbExists = checkDBexists(System.getProperty("user.dir"));
            ds.setDatabaseName(databasename);

            if (!dbExists) {
                conn = DriverManager.getConnection(protocol + databasename + ";create=true",
                        username, password);
            } else {
                conn = DriverManager.getConnection(protocol + databasename + ";create=false", username, password);
            }
            statement = conn.createStatement();
            if (!dbExists) {
                createTables();
                insertModalities();
                insertDefaultListenerDetail();
                insertDefaultLayoutDetail();
                insertDefaultPresets();
            }
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
        }
    }

    /**
     * It closes the local java derby database connection.
     */
    public void closeDrivers() {
        try {
            statement.close();
            conn.commit();
            conn.close();

        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
        }

    }

    public void rebuild() {
        // resetStudyDB();
        // recreateTable();
        deleteRows();
        ApplicationContext.mainScreen.restartReceiver();
    }

    public void createAndInsertDefaults() {
        createTables();
        insertModalities();
        insertDefaultListenerDetail();
        insertDefaultLayoutDetail();
        insertDefaultPresets();
    }

    /**
     * Deletes all files and subdirectories under "dir".
     * @param dir Directory to be deleted
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so now it can be smoked
        return dir.delete();
    }

    /**
     * It commits the database.
     */
    public void doCommit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
        }
    }

    public void insertAE(String serverName, String host, String location, String aeTitle, int port, int headerPort, int imagePort) {
        try {
            conn.createStatement().execute("insert into" + "ae values('" + serverName + "','" + host + "','" + location + "','" + aeTitle + "'," + port + "," + headerPort + "," + imagePort + ")");
            conn.commit();
        } catch (SQLException ex) {
            //  Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public void insertListenerDetail(String aeTitle, int port, String storageLocation) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("select pk from listener");
            rs.next();
            int pk = rs.getInt("pk");
            int n = conn.createStatement().executeUpdate("update listener set aetitle='" + aeTitle + "',port=" + port + " where pk=" + pk);
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String[] getListenerDetails() {
        ResultSet rs = null;
        String detail[] = new String[3];
        try {
            rs = conn.createStatement().executeQuery("select * from listener");
            while (rs.next()) {
                detail[0] = rs.getString("aetitle");
                detail[1] = "" + rs.getInt("port");
                detail[2] = rs.getString("storagelocation");
            }

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detail;
    }

    // checks the existence of the file
    public boolean TableExists(String TableName) throws SQLException {
        String[] types = {"TABLE"};
        ResultSet rset = conn.getMetaData().getTables(null, null, "%", types);
        while (rset.next()) {
            if (rset.getString("TABLE_NAME").equalsIgnoreCase(TableName)) {
                return true;
            }
        }
        return false;

    }

    public void createTables() {
        try {
            String sql = "";
            //creating patient table if it is not exist already
            sql = "create table " + patientTable + " (PatientId varchar(255) NOT NULL CONSTRAINT PatientId_Pk PRIMARY KEY ," + "PatientName varchar(255)," + "PatientBirthDate varchar(30)," + "PatientSex varchar(10))";
            statement.executeUpdate(sql);

            //creating study table if it is not exist already
            sql = "create table " + studyTable + "(StudyInstanceUID varchar(255)  NOT NULL CONSTRAINT StudyInstanceUID_pk PRIMARY KEY ," + "StudyDate varchar(30), " + "AccessionNo varchar(50), " + "ReferringPhysicianName varchar(255), " + "StudyDescription  varchar(80), " + "ModalityInStudy varchar(10), " + "NoOfSeries integer," + "NoOfInstances integer," + "RecdImgCnt Integer," + "SendImgCnt integer," + "RetrieveAET varchar(50)," + "PatientId varchar(255), foreign key(PatientId) references Patient(PatientId))";
            statement.executeUpdate(sql);

            //creating series table if it is not exist already
            sql = "create table " + seriesTable + " (SeriesInstanceUID varchar(255) NOT NULL CONSTRAINT SeriesInstanceUID_pk PRIMARY KEY ," + "SeriesNo varchar(50)," + "Modality varchar(10)," + "SeriesDescription varchar(100)," + "InstitutionName varchar(255)," + "NoOfSeriesRelatedInstances integer," + "PatientId varchar(255), foreign key(PatientId) references Patient(PatientId)," + "StudyInstanceUID varchar(255), foreign key (StudyInstanceUID) references Study(StudyInstanceUID))";
            statement.executeUpdate(sql);

            //creating is instance table if it is not exist already
            sql = "create table " + instanceTable + " (SopUID varchar(255) NOT NULL CONSTRAINT SopUID_pk PRIMARY KEY ," + "InstanceNo varchar(50)," + "SendStatus varchar(50)," + "ForwardDateTime varchar(30)," + "ReceivedDateTime varchar(30)," + "ReceiveStatus varchar(50)," + "FileStoreUrl varchar(750)," + "PatientId varchar(255), foreign key(PatientId) references Patient(PatientId)," + "StudyInstanceUID varchar(255), foreign key (StudyInstanceUID) references Study(StudyInstanceUID)," + "SeriesInstanceUID varchar(255), foreign key (SeriesInstanceUID) references Series(SeriesInstanceUID))";
            statement.executeUpdate(sql);

            sql = "create table ae(pk integer primary key GENERATED ALWAYS AS IDENTITY,logicalname varchar(255),hostname varchar(255),aetitle varchar(255),port integer)";
            statement.executeUpdate(sql);

            sql = "create table modality(pk integer primary key GENERATED ALWAYS AS IDENTITY,logicalname varchar(255),shortname varchar(255))";
            statement.executeUpdate(sql);

            sql = "create table layout(pk integer primary key GENERATED ALWAYS AS IDENTITY,rowcount smallint,columncount smallint,modality_fk integer,foreign key(modality_fk) references modality(pk))";
            statement.executeUpdate(sql);

            sql = "create table preset(pk integer primary key GENERATED ALWAYS AS IDENTITY,presetname varchar(255),windowwidth numeric,windowlevel numeric,modality_fk integer,foreign key(modality_fk) references modality(pk))";
            statement.executeUpdate(sql);

            sql = "create table listener(pk integer primary key GENERATED ALWAYS AS IDENTITY,aetitle varchar(255),port integer,storagelocation varchar(255))";
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            // Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    /**
     * This routine is used to list studies of local db
     * @return
     */
    public Vector listAllStudiesOfDB() {
        ResultSet rs = null;
        Vector studyList = new Vector();
        try {
            String sql = "Select * from study";
            rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                StudyModel st = new StudyModel();
                st.setStudyDate(rs.getString("StudyDate"));
                st.setAccessionNo(rs.getString("AccessionNo").equalsIgnoreCase("null") ? "" : rs.getString("AccessionNo"));
                st.setPatientId(rs.getString("PatientId"));
                st.setStudyDescription(rs.getString("StudyDescription").equalsIgnoreCase("null") ? "" : rs.getString("StudyDescription"));
                st.setStudyUID(rs.getString("StudyInstanceUID"));
                st.setModalitiesInStudy(rs.getString("ModalityInStudy"));

                String sql2 = "select PatientName,PatientBirthDate from patient where PatientId='" + rs.getString("PatientId") + "'";
                ResultSet rs1 = conn.createStatement().executeQuery(sql2);
                rs1.next();

                st.setPatientName(rs1.getString("PatientName"));
                st.setDob(rs1.getString("PatientBirthDate"));
                studyList.addElement(st);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return studyList;
    }

    public Vector listAllSeriesOfStudy(String siuid) {
        ResultSet rs = null;
        Vector seriesList = new Vector();
        try {
            String sql = "Select * from series where StudyInstanceUID='" + siuid + "'";
            rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Series series = new Series();
                series.setSeriesDesc(rs.getString("SeriesDescription"));
                series.setSeriesInstanceUID(rs.getString("SeriesInstanceUID"));
                series.setModality(rs.getString("Modality"));
                series.setSeriesNumber(rs.getString("SeriesNo"));
                series.setStudyInstanceUID(rs.getString("StudyInstanceUID"));
                series.setInstitutionName(rs.getString("InstitutionName"));
                series.setSeriesRelatedInstance(this.getSeriesLevelInstance(siuid, series.getSeriesInstanceUID()));
                seriesList.addElement(series);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return seriesList;
    }

    public void insertModalities() {
        String modality[] = {"CT", "MR", "XA", "CR", "SC", "NM", "RF", "DX", "US", "PX", "OT"};
        for (int i = 0; i < modality.length; i++) {
            try {
                conn.createStatement().execute("insert into modality(logicalname,shortname) values('Dummy','" + modality[i] + "')");
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void insertDefaultPresets() {
        try {
            String sql = "select pk from modality where shortname='CT'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            rs.next();
            String sql1 = "insert into preset(presetname,windowwidth,windowlevel,modality_fk)values('CT Abdomen',40,350," + rs.getString("pk") + ")";
            String sql2 = "insert into preset(presetname,windowwidth,windowlevel,modality_fk)values('CT Lung',1500,-600," + rs.getString("pk") + ")";
            String sql3 = "insert into preset(presetname,windowwidth,windowlevel,modality_fk)values('CT Brain',80,40," + rs.getString("pk") + ")";
            String sql4 = "insert into preset(presetname,windowwidth,windowlevel,modality_fk)values('CT Bone',2500,480," + rs.getString("pk") + ")";
            String sql5 = "insert into preset(presetname,windowwidth,windowlevel,modality_fk)values('CT Head/Neck',350,90," + rs.getString("pk") + ")";

            conn.createStatement().execute(sql1);
            conn.createStatement().execute(sql2);
            conn.createStatement().execute(sql3);
            conn.createStatement().execute(sql4);
            conn.createStatement().execute(sql5);

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String[] getModalities() {
        String[] modalities = null;
        try {
            String sql2 = "Select shortname from modality";
            String sql1 = "select count(*) from modality";
            int count;
            int index = 0;
            ResultSet rs1 = conn.createStatement().executeQuery(sql1);
            rs1.next();
            count = rs1.getInt(1);
            modalities = new String[count];
            ResultSet rs2 = conn.createStatement().executeQuery(sql2);
            while (rs2.next()) {
                modalities[index] = rs2.getString("shortname");
                index++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modalities;
    }

    public void insertDefaultListenerDetail() {
        try {
            boolean insertStatus = conn.createStatement().execute("insert into" + " listener(aetitle,port,storagelocation) values('MAYAM',1025,'archive')");
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertDefaultLayoutDetail() {
        try {
            String sql1 = "select pk,shortname from modality";
            ResultSet rs = conn.createStatement().executeQuery(sql1);
            while (rs.next()) {
                int pk = rs.getInt("pk");
                boolean insertStatus = conn.createStatement().execute("insert into" + " layout(rowcount,columncount,modality_fk) values(1,2," + pk + ")");
                conn.commit();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertLayoutDetail(int rowCount, int columnCount, String modality) {
        try {
            String sql1 = "select pk from modality where shortname='" + modality + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql1);
            rs.next();
            int pk = rs.getInt("pk");

            String sql2 = "select pk from layout where modality_fk=" + pk;
            ResultSet rs1 = conn.createStatement().executeQuery(sql2);
            rs1.next();
            int layoutPK = rs1.getInt("pk");
            if (layoutPK != -1) {
                int n = conn.createStatement().executeUpdate("update layout set rowcount=" + rowCount + ",columncount=" + columnCount + ",modality_fk=" + pk + " where pk=" + layoutPK);
                conn.commit();
            } else {
                boolean insertStatus = conn.createStatement().execute("insert into" + " layout(rowcount,columncount,modality_fk) values(1,2," + pk + ")");
                conn.commit();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertPreset(PresetModel presetModel, String modality) {
        try {
            String sql1 = "select pk from modality where shortname='" + modality + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql1);
            rs.next();
            int pk = rs.getInt("pk");
            boolean insertStatus = conn.createStatement().execute("insert into" + " preset(presetname,windowwidth,windowlevel,modality_fk) values('" + presetModel.getPresetName() + "'," + presetModel.getWindowWidth() + "," + presetModel.getWindowLevel() + "," + pk + ")");
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList getPresetValueForModality(String modality) {
        ArrayList presetList = new ArrayList();
        try {
            String sql1 = "select pk from modality where shortname='" + modality + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql1);
            rs.next();
            int pk = rs.getInt("pk");
            String sql2 = "select pk,presetname,windowwidth,windowlevel from preset where modality_fk=" + pk;
            ResultSet rs1 = conn.createStatement().executeQuery(sql2);
            while (rs1.next()) {
                PresetModel presetModel = new PresetModel();
                presetModel.setPk(rs1.getInt("pk"));
                presetModel.setPresetName(rs1.getString("presetname"));
                presetModel.setWindowWidth(rs1.getString("windowwidth"));
                presetModel.setWindowLevel(rs1.getString("windowlevel"));
                presetList.add(presetModel);
            }

        } catch (Exception e) {
        }
        return presetList;

    }

    public void updatePresetValues(PresetModel presetModel) {
        try {
            int n = conn.createStatement().executeUpdate("update preset set presetname='" + presetModel.getPresetName() + "',windowwidth=" + presetModel.getWindowWidth() + ",windowlevel=" + presetModel.getWindowLevel() + " where pk=" + presetModel.getPk());
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePreset(PresetModel presetModel) {
        try {
            String sql = "delete from preset where pk=" + presetModel.getPk();
            conn.createStatement().execute(sql);
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateServerListValues(ServerModel serverModel) {
        try {
            int n = conn.createStatement().executeUpdate("update ae set logicalname='" + serverModel.getServerName() + "',hostname='" + serverModel.getHostName() + "',aetitle='" + serverModel.getAeTitle() + "',port=" + serverModel.getPort() + " where pk=" + serverModel.getPk());
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteServer(ServerModel serverModel) {
        try {
            String sql = "delete from ae where pk=" + serverModel.getPk();
            conn.createStatement().execute(sql);
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertServer(ServerModel serverModel) {
        try {
            boolean insertStatus = conn.createStatement().execute("insert into" + " ae(logicalname,hostname,aetitle,port) values('" + serverModel.getServerName() + "','" + serverModel.getHostName() + "','" + serverModel.getAeTitle() + "'," + serverModel.getPort() + ")");
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList getServerList() {
        ArrayList serverList = new ArrayList();
        try {
            String sql2 = "select pk,logicalname,hostname,aetitle,port from ae";
            ResultSet rs1 = conn.createStatement().executeQuery(sql2);
            while (rs1.next()) {
                ServerModel serverModel = new ServerModel();
                serverModel.setPk(rs1.getInt("pk"));
                serverModel.setServerName(rs1.getString("logicalname"));
                serverModel.setHostName(rs1.getString("hostname"));
                serverModel.setAeTitle(rs1.getString("aetitle"));
                serverModel.setPort("" + rs1.getInt("port"));
                serverList.add(serverModel);
            }

        } catch (Exception e) {
        }
        return serverList;
    }

    public int[] getRowColumnForModality(String modality) {
        int rowColumn[] = new int[2];
        try {

            String sql = "select pk,shortname from modality where shortname='" + modality + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            rs.next();
            int pk = rs.getInt("pk");
            String sql1 = "select rowcount,columncount from layout where modality_fk=" + pk;
            ResultSet rs1 = conn.createStatement().executeQuery(sql1);
            rs1.next();
            rowColumn[0] = rs1.getInt("rowcount");
            rowColumn[1] = rs1.getInt("columncount");

        } catch (SQLException ex) {
            rowColumn[0] = 1;
            rowColumn[1] = 1;
            // Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rowColumn;
    }

    public int[] getRowColumnBasedStudyUID(String suid) {
        return getRowColumnForModality(getModalityBasedonStudyUID(suid));
    }

    public String getModalityBasedonStudyUID(String suid) {
        String modality = null;
        try {
            String sql = "select ModalityInStudy from study where StudyInstanceUID='" + suid + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            rs.next();
            modality = rs.getString("ModalityInStudy");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modality;
    }

    public String getPatientNameBasedonStudyUID(String suid) {
        String patientName = null;
        try {

            String sql = "select PatientId from study where StudyInstanceUID='" + suid + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            rs.next();
            String patientid = rs.getString("PatientId");
            String sql1 = "select PatientName from patient where PatientId='" + patientid + "'";
            ResultSet rs1 = conn.createStatement().executeQuery(sql1);
            rs1.next();
            patientName = rs1.getString("PatientName");

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientName;
    }

    /** It checks the particular record exists or not. */
    public boolean checkRecordExists(String tablename, String fieldname,
            String compareWith) {
        ResultSet rs = null;
        boolean result = false;
        try {
            rs = conn.createStatement().executeQuery("select count(" + fieldname + ") from " + tablename + " where " + fieldname + " = '" + compareWith.trim() + "'");
            rs.next();
            if (rs.getInt(1) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            return false;
        }
    }

    public void parseDCMFile(File parseFile) {
        try {
            DicomInputStream dis = new DicomInputStream(parseFile);
            DicomObject data = new BasicDicomObject();
            try {
                data = dis.readDicomObject();
            } catch (EOFException e) {
            }
            if (data != null) {
                writeDataToDatabase(data);
            }

            dis.close();
            parseFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeDataToDatabase(DicomObject dataset) {
        insertPatientData(dataset);
        insertStudyData(dataset);
        insertSeriesData(dataset);
        insertImageData(dataset);
    }

    public void importDataToDatabase(DicomObject dataset, File dicomFile) {
        insertPatientData(dataset);
        insertStudyData(dataset);
        insertSeriesData(dataset);
        insertImageData(dataset, dicomFile);
    }

    public void insertPatientData(DicomObject dataset) {

        if (checkRecordExists(patientTable, "PatientId", dataset.getString(Tag.PatientID))) {
        } else {
            try {

                String dat = null;
                if ((dataset.getString(Tag.PatientBirthDate) != null) && dataset.getString(Tag.PatientBirthDate).length() > 0) {

                    dat = "date('" + dateFormat.format(dataset.getDate(Tags.PatientBirthDate)) + "')";
                }
                conn.createStatement().execute("insert into " + patientTable + " values('" + dataset.getString(Tag.PatientID) + "','" + dataset.getString(Tag.PatientName) + "'," + dat + ",'" + dataset.getString(Tag.PatientSex) + "')");
                conn.commit();
            } catch (SQLException ex) {
                // Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void insertStudyData(DicomObject dataset) {
        if (checkRecordExists(studyTable, "StudyInstanceUID", dataset.getString(Tag.StudyInstanceUID))) {
        } else {
            try {
                String dat = "null";
                String accessionno = "null";
                String refName = "null";
                int noSeries = 0;
                int noInstance = 0;
                String retAe = "null";
                String studyDescription = "null";
                if ((dataset.getString(Tag.StudyDate) != null) && dataset.getString(Tag.StudyDate).length() > 0) {
                    dat = "date('" + dateFormat.format(dataset.getDate(Tag.StudyDate)) + "')";
                }
                if (dataset.getString(Tag.AccessionNumber) != null && dataset.getString(Tag.AccessionNumber).length() > 0) {
                    accessionno = dataset.getString(Tag.AccessionNumber);
                }
                if (dataset.getString(Tag.ReferringPhysicianName) != null && dataset.getString(Tag.ReferringPhysicianName).length() > 0) {
                    refName = dataset.getString(Tag.ReferringPhysicianName);
                }
                if (dataset.getString(Tag.NumberOfStudyRelatedSeries) != null && dataset.getString(Tag.NumberOfStudyRelatedSeries).length() > 0) {
                    noSeries = Integer.parseInt(dataset.getString(Tag.NumberOfStudyRelatedSeries));
                }
                if (dataset.getString(Tag.NumberOfStudyRelatedInstances) != null && dataset.getString(Tag.NumberOfStudyRelatedInstances).length() > 0) {
                    noInstance = Integer.parseInt(dataset.getString(Tag.NumberOfStudyRelatedInstances));
                }
                if (dataset.getString(Tag.StudyDescription) != null && dataset.getString(Tag.StudyDescription).length() > 0) {
                    studyDescription = dataset.getString(Tag.StudyDescription);
                }
                conn.createStatement().execute("insert into " + studyTable + " values('" + dataset.getString(Tag.StudyInstanceUID) + "'," + dat + ",'" + accessionno + "','" + refName + "','" + studyDescription.replace('\'', ' ') + "','" + dataset.getString(Tag.Modality) + "'," + noSeries + "," + noInstance + "," + 0 + "," + 1 + ",'" + retAe + "','" + dataset.getString(Tag.PatientID) + "')");
                conn.commit();
            } catch (SQLException ex) {
                //Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void insertSeriesData(DicomObject dataset) {
        if (checkRecordExists(seriesTable, "SeriesInstanceUID", dataset.getString(Tag.SeriesInstanceUID))) {
        } else {
            try {
                String dat = "null";
                int noSeries = 0;
                String institution = "null";
                String seriesNo = "null";
                String modality = "null";
                String studyDesc = "null";
                if (dataset.getString(Tag.SeriesDate) != null && dataset.getString(Tag.SeriesDate).length() > 0) {
                    dat = "date('" + dateFormat.format(dataset.getDate(Tag.SeriesDate)) + "')";
                }
                if (dataset.getString(Tag.NumberOfSeriesRelatedInstances) != null && dataset.getString(Tag.NumberOfSeriesRelatedInstances).length() > 0) {
                    noSeries = Integer.parseInt(dataset.getString(Tag.NumberOfSeriesRelatedInstances));
                }
                if (dataset.getString(Tag.InstitutionName) != null && dataset.getString(Tag.InstitutionName).length() > 0) {
                    institution = dataset.getString(Tag.InstitutionName).replace('\'', ' ');
                }
                if (dataset.getString(Tag.SeriesNumber) != null && dataset.getString(Tag.SeriesNumber).length() > 0) {
                    seriesNo = dataset.getString(Tag.SeriesNumber);
                }
                if (dataset.getString(Tag.Modality) != null && dataset.getString(Tag.Modality).length() > 0) {
                    modality = dataset.getString(Tag.Modality);
                }
                if (dataset.getString(Tag.SeriesDescription) != null && dataset.getString(Tag.SeriesDescription).length() > 0) {
                    studyDesc = dataset.getString(Tag.SeriesDescription).replace('\'', ' ');
                }
                conn.createStatement().execute("insert into " + seriesTable + " values('" + dataset.getString(Tag.SeriesInstanceUID) + "','" + seriesNo + "','" + modality + "','" + studyDesc + "','" + institution + "'," + noSeries + ",'" + dataset.getString(Tag.PatientID) + "','" + dataset.getString(Tag.StudyInstanceUID) + "')");
                conn.commit();
            } catch (SQLException ex) {
                // Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void insertImageData(DicomObject dataset) {
        if (checkRecordExists(instanceTable, "SopUID", dataset.getString(Tag.SOPInstanceUID))) {
        } else {
            try {
                Calendar today = Calendar.getInstance();                
                String struturedDestination = "archive" + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + dataset.getString(Tag.StudyInstanceUID) + File.separator + dataset.getString(Tag.SOPInstanceUID);
                conn.createStatement().execute("insert into " + instanceTable + " values('" + dataset.getString(Tag.SOPInstanceUID) + "','" + dataset.getString(Tag.InstanceNumber) + "','" + "partial" + "','" + " " + "','" + " " + "','" + "partial" + "','" + struturedDestination + "','" + dataset.getString(Tag.PatientID) + "','" + dataset.getString(Tag.StudyInstanceUID) + "','" + dataset.getString(Tag.SeriesInstanceUID) + "')");
                conn.commit();
                int receivedCount = this.getReceiveCount(dataset.getString(Tag.StudyInstanceUID));
                receivedCount = receivedCount + 1;
                this.update("study", "RecdImgCnt", receivedCount, "StudyInstanceUID", dataset.getString(Tag.StudyInstanceUID));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void insertImageData(DicomObject dataset, File dicomFile) {
        if (checkRecordExists(instanceTable, "SopUID", dataset.getString(Tag.SOPInstanceUID))) {
        } else {
            try {
                Calendar today = Calendar.getInstance();
                String struturedDestination = dicomFile.getAbsolutePath();
                conn.createStatement().execute("insert into " + instanceTable + " values('" + dataset.getString(Tag.SOPInstanceUID) + "','" + dataset.getString(Tag.InstanceNumber) + "','" + "partial" + "','" + " " + "','" + " " + "','" + "partial" + "','" + struturedDestination + "','" + dataset.getString(Tag.PatientID) + "','" + dataset.getString(Tag.StudyInstanceUID) + "','" + dataset.getString(Tag.SeriesInstanceUID) + "')");
                conn.commit();
                int receivedCount = this.getReceiveCount(dataset.getString(Tag.StudyInstanceUID));
                receivedCount = receivedCount + 1;
                this.update("study", "RecdImgCnt", receivedCount, "StudyInstanceUID", dataset.getString(Tag.StudyInstanceUID));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Object[] retrieveAEList() {
        Object Root[];
        ArrayList list = new ArrayList();
        try {
            String sql = "Select * from AE";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Object[] value = {rs.getString("logicalname")};
                list.add(value);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        Root = list.toArray();
        return Root;
    }

    public Object[] retrieveAEListWithHeader() {
        Object Root[];
        ArrayList list = new ArrayList();
        list.add(DicomServersHeaderDisplayName);
        try {
            String sql = "Select * from AE";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Object[] value = {rs.getString("logicalname")};
                list.add(value);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        Root = list.toArray();
        return Root;
    }

    public AEModel getServerDetail(String serverName) {
        AEModel ae = null;
        try {
            String sql = "Select * from AE where logicalname='" + serverName + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                ae = new AEModel(rs.getString("logicalname"), rs.getString("hostname"), rs.getString("aetitle"), rs.getInt("port"));
            }
        } catch (Exception ex) {
            // Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ae;

    }

    public ArrayList getPresets() {
        ArrayList presetList = new ArrayList();
        presetList.add(getNonePreset());
        try {
            String sql = "select * from preset";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                PresetModel presetModel = new PresetModel();
                presetModel.setPresetName(rs.getString("presetname"));
                presetModel.setWindowWidth(rs.getString("windowwidth"));
                presetModel.setWindowLevel(rs.getString("windowlevel"));
                presetList.add(presetModel);
            }
        } catch (Exception e) {
        }
        return presetList;
    }

    private PresetModel getNonePreset() {
        PresetModel presetModel = new PresetModel();
        presetModel.setPresetName("Default");
        return presetModel;
    }

    public PresetModel getPreset(String presetName) {
        PresetModel presetModel = null;
        try {
            String sql = "select * from preset where presetname='" + presetName + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            rs.next();
            presetModel = new PresetModel();
            presetModel.setPk(rs.getInt("pk"));
            presetModel.setPresetName(rs.getString("presetname"));
            presetModel.setWindowLevel(rs.getString("windowlevel"));
            presetModel.setWindowWidth(rs.getString("windowwidth"));
        } catch (Exception e) {
        }
        return presetModel;
    }

    public ArrayList getUrlBasedOnStudyIUID(String siuid) {

        ArrayList fileArray = new ArrayList();
        try {
            int i = 0;
            String sql = "select SeriesInstanceUID from series where StudyInstanceUID='" + siuid + "'";
            ResultSet rs = null;
            rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                ResultSet rs1 = null;
                String sql1 = "select FileStoreUrl from image where StudyInstanceUID='" + siuid + "' AND " + "SeriesInstanceUID='" + rs.getString("SeriesInstanceUID") + "' AND " + "InstanceNo='1'";
                rs1 = conn.createStatement().executeQuery(sql1);
                File imageUrl = null;
                if (rs1.next()) {
                    imageUrl = new File(rs1.getString("FileStoreUrl"));
                } else {
                    ResultSet rs2 = null;
                    String sql2 = "select FileStoreUrl from image where StudyInstanceUID='" + siuid + "' AND " + "SeriesInstanceUID='" + rs.getString("SeriesInstanceUID") + "'";
                    rs2 = conn.createStatement().executeQuery(sql2);
                    if (rs2.next()) {
                        imageUrl = new File(rs2.getString("FileStoreUrl"));
                    }
                }                
                fileArray.add(imageUrl);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileArray;
    }

    public ArrayList getUrlBasedOnStudyAndSeriesUID(String studyUID, String seriesUID) {
        ArrayList fileArray = new ArrayList();
        try {
            ResultSet rs1 = null;
            String sql1 = "select FileStoreUrl from image where StudyInstanceUID='" + studyUID + "' AND " + "SeriesInstanceUID='" + seriesUID + "'";
            rs1 = conn.createStatement().executeQuery(sql1);
            while (rs1.next()) {
                File imageUrl = new File(rs1.getString("FileStoreUrl"));
                fileArray.add(imageUrl);
            }

        } catch (Exception e) {
        }
        return fileArray;
    }

    public ArrayList<Series> getSeriesList(String siuid) {
        ArrayList<Series> arr = new ArrayList();
        try {
            String sql = "select SeriesInstanceUID,SeriesNo,SeriesDescription from series where StudyInstanceUID='" + siuid + "'";
            ResultSet rs = null;
            rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Series series = new Series();
                series.setStudyInstanceUID(siuid);
                series.setSeriesInstanceUID(rs.getString("SeriesInstanceUID"));
                series.setSeriesNumber(rs.getString("SeriesNo"));
                series.setSeriesDesc(rs.getString("SeriesDescription"));
                ResultSet rs1 = null;
                String sql1 = "select FileStoreUrl,SopUID,InstanceNo from image where StudyInstanceUID='" + siuid + "' AND " + "SeriesInstanceUID='" + rs.getString("SeriesInstanceUID") + "'";
                rs1 = conn.createStatement().executeQuery(sql1);
                while (rs1.next()) {
                    Instance img = new Instance();
                    img.setFilepath(rs1.getString("FileStoreUrl"));
                    img.setSop_iuid(rs1.getString("SopUID"));
                    img.setInstance_no(rs1.getString("InstanceNo"));
                    series.getImageList().add(img);

                }
                arr.add(series);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arr;
    }

    public Series getSeries(String studyUID, String seriesUID) {
        Series series = null;
        try {
            String sql = "select SeriesInstanceUID,SeriesNo,SeriesDescription from series where StudyInstanceUID='" + studyUID + "' AND " + "SeriesInstanceUID='" + seriesUID + "'";
            ResultSet rs = null;
            rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                series = new Series();
                series.setStudyInstanceUID(studyUID);
                series.setSeriesInstanceUID(rs.getString("SeriesInstanceUID"));
                series.setSeriesNumber(rs.getString("SeriesNo"));
                series.setSeriesDesc(rs.getString("SeriesDescription"));
                ResultSet rs1 = null;
                String sql1 = "select FileStoreUrl,SopUID,InstanceNo from image where StudyInstanceUID='" + studyUID + "' AND " + "SeriesInstanceUID='" + rs.getString("SeriesInstanceUID") + "'";
                rs1 = conn.createStatement().executeQuery(sql1);
                while (rs1.next()) {
                    Instance img = new Instance();
                    img.setFilepath(rs1.getString("FileStoreUrl"));
                    img.setSop_iuid(rs1.getString("SopUID"));
                    img.setInstance_no(rs1.getString("InstanceNo"));
                    series.getImageList().add(img);

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return series;
    }

    public int getStudyLevelInstance(String siuid) {
        int size = 0;
        try {
            String sql = "select SeriesInstanceUID from series where StudyInstanceUID='" + siuid + "'";
            ResultSet rs = null;
            rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                ResultSet rs1 = null;
                String sql1 = "select count(*) from image where StudyInstanceUID='" + siuid + "' AND " + "SeriesInstanceUID='" + rs.getString("SeriesInstanceUID") + "'";
                rs1 = conn.createStatement().executeQuery(sql1);
                rs1.next();
                size = size + rs1.getInt(1);
            }
        } catch (Exception ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return size;
    }

    public String getStudyDescBasedOnStudyUID(String studyUID) {
        String studyDesc = "";
        try {
            String sql = "select studyDesc from Study where StudyInstanceUID='" + studyUID + "'";
        } catch (Exception e) {
        }
        return studyDesc;
    }

    public String getSeriesDesc(String studyUID, String seriesUID) {
        String seriesDesc = "";
        try {
            String sql = "select SeriesDescription from Series where StudyInstanceUID='" + studyUID + "'" + "SeriesInstanceUID='" + seriesUID + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            rs.next();
            seriesDesc = rs.getString("SeriesDescription");
        } catch (Exception e) {
        }

        return seriesDesc;
    }

    public int getSeriesLevelInstance(String studyuid, String seriesuid) {
        int size = 0;
        try {
            String sql1 = "select count(*) from image where StudyInstanceUID='" + studyuid + "' AND " + "SeriesInstanceUID='" + seriesuid + "'";
            ResultSet rs = conn.createStatement().executeQuery(sql1);
            rs.next();
            size = rs.getInt(1);
        } catch (Exception e) {
        }
        return size;
    }

    /**
     * It updates the particular field on  table.
     */
    public void update(String tablename, String fieldname, String fieldvalue, String whereField, String whereValue) {
        try {
            int value = conn.createStatement().executeUpdate("update " + tablename + " set " + fieldname + "='" + fieldvalue + "' where " + whereField + "='" + whereValue + "'");
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
        }
    }

    public void update(String tablename, String fieldname, int fieldvalue, String whereField, String whereValue) {
        try {
            int value = conn.createStatement().executeUpdate("update " + tablename + " set " + fieldname + "=" + fieldvalue + " where " + whereField + "='" + whereValue + "'");
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
        }
    }

    /**
     * It deletes all rows in patient,study,series and image.
     */
    public void deleteRows() {
        try {
            statement.execute("delete from " + instanceTable);
            statement.execute("delete from " + seriesTable);
            statement.execute("delete from " + studyTable);
            statement.execute("delete from " + patientTable);
            /*Following lines to be uncommented to delete ae,layout,preset,listener,modality table while resetDB
            statement.execute("delete from ae");
            statement.execute("delete from layout");
            statement.execute("delete from preset");
            statement.execute("delete from listener");
            statement.execute("delete from modality");*/

            conn.commit();
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
        }
    }

    public void instanceTableRowDelete(String instanceUID) {
        try {
            String sql = "delete from image where SopUID='" + instanceUID + "'";
            conn.createStatement().execute(sql);
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void seriesTableRowDelete(String seriesUID) {
        try {
            String sql = "delete from series where SeriesInstanceUID='" + seriesUID + "'";
            conn.createStatement().execute(sql);
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void studyTableRowDelete(String studyUID) {
        try {
            String sql = "delete from study where StudyInstanceUID='" + studyUID + "'";
            conn.createStatement().execute(sql);
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dropTables() {
        try {
            statement.execute("drop table " + instanceTable);
            statement.execute("drop table " + seriesTable);
            statement.execute("drop table " + studyTable);
            statement.execute("drop table " + patientTable);
            statement.execute("drop table ae");
            statement.execute("drop table listener");
            statement.execute("drop table preset");
            statement.execute("drop table layout");
            statement.execute("drop table modality");
            conn.commit();
        } catch (SQLException e) {
           // e.printStackTrace();
        }
    }

    public void resetStudyDB() {
        try {
            statement.execute("drop table " + instanceTable);
            statement.execute("drop table " + seriesTable);
            statement.execute("drop table " + studyTable);
            statement.execute("drop table " + patientTable);
            conn.commit();

        } catch (SQLException e) {
            // e.printStackTrace();            
        }
    }

    public String getSendStatus(String studyIUID) {
        String status = null;
        try {
            String str = "select sendstatus " +
                    "from study where studyinstanceuid='" + studyIUID + "'";
            ResultSet rs = conn.createStatement().executeQuery(str);
            while (rs.next()) {
                status = rs.getString("sendstatus");
            }
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            str = null;
        }
        return status;
    }

    public int getSendCount(String studyIUID) {
        int count = 0;
        try {
            String str = "select SendImgCnt " +
                    "from study where StudyInstanceUID='" + studyIUID + "'";
            ResultSet rs = conn.createStatement().executeQuery(str);
            while (rs.next()) {
                count = rs.getInt("SendImgCnt");
            }
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            str = null;
        }
        return count;
    }

    public String getReceiveStatus(String studyIUID) {
        String status = null;
        try {
            String str = "select receivestatus " +
                    "from study where studyinstanceuid='" + studyIUID + "'";
            ResultSet rs = conn.createStatement().executeQuery(str);
            while (rs.next()) {
                status = rs.getString("receivestatus");
            }
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            str = null;
        }
        return status;
    }

    public int getReceiveCount(String studyIUID) {
        int count = 0;
        try {
            String str = "select RecdImgCnt " +
                    "from study where StudyInstanceUID='" + studyIUID + "'";
            ResultSet rs = conn.createStatement().executeQuery(str);
            while (rs.next()) {
                count = rs.getInt("RecdImgCnt");
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            str = null;
        }
        return count;
    }

    public void doCreateServerRecords(String serverName, String host, String aeTitle, int port) {
        try {
            String sql = "insert into AE(logicalname,hostname,aetitle,port)values('" + serverName + "','" + host + "','" + aeTitle + "'," + port + ")";
            boolean b = conn.createStatement().execute(sql);
            conn.commit();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void doEditServerRecords(String serverName, String host, String aeTitle, int port, String keyName) {
        try {
            String getPKSql = "select pk from ae where logicalname='" + keyName + "'";

            ResultSet rs = conn.createStatement().executeQuery(getPKSql);
            while (rs.next()) {
                String pk = rs.getString("pk");
                String sql = "update AE SET logicalname='" + serverName + "',hostname='" + host + "',aetitle='" + aeTitle + "',port=" + port + "where pk=" + pk;
                int n = conn.createStatement().executeUpdate(sql);
            }
            conn.commit();
        } catch (Exception e) {
           // e.printStackTrace();
            }
    }

    public void doDeleteRecords(String serverName) {
        try {
            String sql = "delete from ae where logicalname='" + serverName + "'";
            boolean b = conn.createStatement().execute(sql);
            conn.commit();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public ArrayList getStudyUIDBasedOnPatientName(String patientName) {
        ArrayList<Study> studyList = new ArrayList<Study>();
        try {
            String getIDSql = "select PatientId from patient where patientName='" + patientName + "'";
            ResultSet rs = conn.createStatement().executeQuery(getIDSql);
            while (rs.next()) {
                String id = rs.getString("PatientId");
                String sql = "select StudyInstanceUID,StudyDescription from study where study.PatientId='" + id + "'";
                ResultSet rs1 = conn.createStatement().executeQuery(sql);
                while (rs1.next()) {
                    Study study = new Study();
                    study.setStudyInstanceUID(rs1.getString("StudyInstanceUID"));
                    study.setStudyDesc(rs1.getString("StudyDescription"));
                    studyList.add(study);
                }
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return studyList;
    }

    public static DatabaseHandler getInstance() {
        DatabaseHandler DatabaseUpdateRef = new DatabaseHandler();
        return DatabaseUpdateRef;
    }
}
