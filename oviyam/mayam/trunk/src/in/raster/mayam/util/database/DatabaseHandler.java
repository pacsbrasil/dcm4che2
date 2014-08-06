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
import in.raster.mayam.facade.ApplicationFacade;
import in.raster.mayam.models.*;
import in.raster.mayam.models.treetable.SeriesNode;
import in.raster.mayam.models.treetable.StudyNode;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.apache.derby.jdbc.EmbeddedSimpleDataSource;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class DatabaseHandler {
    //Named Constants for Database,driver,protocol

    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver", protocol = "jdbc:derby:", databasename = "mayamdb";
    //Named Constants for username and password of Database 
    private static final String username = "mayam", password = "mayam";
    //Database Connection creator and executor variables 
    private Connection conn;
    private Statement statement;
    //Boolean variables 
    private boolean dbExists = false;
    //Datasouce declaration
    private EmbeddedSimpleDataSource ds;
    //Other variables
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"), timeFormat = new SimpleDateFormat("kk:mm:ss");
    ExecutorService executor = null;

    public static DatabaseHandler getInstance() {
        return new DatabaseHandler();
    }

    public boolean checkDBexists(String tem) {
        File[] listFiles = new File(tem).listFiles();
        for (int l = 0; l < listFiles.length; l++) {
            if (listFiles[l].getName().equalsIgnoreCase(databasename)) {
                return true;
            }
        }
        return false;
    }

    public void openOrCreateDB() {
        try {
            System.setProperty("derby.system.home", ApplicationContext.getAppDirectory());
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
            try {
                ds = new org.apache.derby.jdbc.EmbeddedSimpleDataSource();
                ds.setDatabaseName(databasename);
            } catch (NoClassDefFoundError e) {
                ApplicationContext.logger.log(Level.INFO, "DatabaseHandler", e);
                ApplicationFacade.exitApp("ERROR: ClassNotFoundException:" + e.getMessage() + ": Exiting the program");
            }
            openConnection();
            statement = conn.createStatement();
            if (!dbExists) {
                createTables();
                insertDefaultLisenerDetails();
                insertModalities();
                insertDefaultPresets();
                insertDefaultThemes();
                insertButton(new ButtonsModel("Today CT", "CT", "t", "", false, false));
                insertDefaultLocales();
                insertMiscellaneous();
                conn.commit();
            } else {
                upgradeDatabase();
            }
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
        }
    }

    private void openConnection() {
        this.dbExists = checkDBexists(ApplicationContext.getAppDirectory());
        try {
            if (!dbExists) {
                conn = DriverManager.getConnection(protocol + ApplicationContext.getAppDirectory() + File.separator + databasename + ";create=true", username, password);
            } else {
                conn = DriverManager.getConnection(protocol + databasename + ";create=false", username, password);
            }
        } catch (Exception e) {
            if (dbExists && conn == null) {
                ApplicationContext.logger.log(Level.INFO, "ERROR: Database connection cannot be created\nAn instance of application is already running", e);
                ApplicationFacade.exitApp("An instance of Mayam is already running: Exiting the program");
            }
        }
    }

    private void createTables() {
        try {
            statement.executeUpdate("create table patient (PatientId varchar(255) NOT NULL CONSTRAINT PatientId_pk PRIMARY KEY," + "PatientName varchar(255)," + "PatientBirthDate varchar(30)," + "PatientSex varchar(10))");
            statement.executeUpdate("create table study (StudyInstanceUID varchar(255) NOT NULL CONSTRAINT StudyInstanceUID_pk PRIMARY KEY," + "StudyDate varchar(30)," + "StudyTime varchar(30)," + "AccessionNo varchar(50)," + "RefferingPhysicianName varchar(255)," + "StudyDescription varchar(80)," + "ModalitiesInStudy varchar(10)," + "NoOfSeries integer," + "NoOfInstances integer," + "RecdImgCnt integer," + "SendImgCnt integer," + "RetrieveAET varchar(50)," + "StudyType varchar(75)," + "DownloadStatus boolean," + "PatientId varchar(255), foreign key(PatientId) references Patient(PatientId))");
            statement.executeUpdate("create table series (SeriesInstanceUID varchar(255) NOT NULL CONSTRAINT SeriesInstanceUID_pk PRIMARY KEY," + "SeriesNo varchar(50)," + "SeriesDate varchar(30)," + "SeriesTime varchar(30)," + "Modality varchar(10)," + "SeriesDescription varchar(100)," + "BodyPartExamined varchar(100)," + "InstitutionName varchar(255)," + "NoOfSeriesRelatedInstances integer," + "PatientId varchar(255), foreign key(PatientId) references Patient(PatientId)," + "StudyInstanceUID varchar(255),foreign key(StudyInstanceUID) references Study(StudyInstanceUID))");
            statement.executeUpdate("create table image (SopUID varchar(255) NOT NULL CONSTRAINT SopUID_pk PRIMARY KEY," + "SOPClassUID varchar(255)," + "InstanceNo integer," + "multiframe boolean," + "totalframe varchar(50)," + "SendStatus varchar(50)," + "ForwardDateTime varchar(30)," + "ReceivedDateTime varchar(30)," + "ReceiveStatus varchar(50)," + "FileStoreUrl varchar(1000)," + "SliceLocation integer," + "EncapsulatedDocument varchar(50)," + "ThumbnailStatus boolean," + "FrameOfReferenceUID varchar(128)," + "ImagePosition varchar(64)," + "ImageOrientation varchar(128)," + "ImageType varchar(30)," + "PixelSpacing varchar(64)," + "SliceThickness varchar(16)," + "NoOfRows integer," + "NoOfColumns integer," + "ReferencedSopUid varchar(128)," + "PatientId varchar(255),foreign key(PatientId) references Patient(PatientId)," + "StudyInstanceUID varchar(255),foreign key(StudyInstanceUID) references Study(StudyInstanceUID)," + "SeriesInstanceUID varchar(255),foreign key(SeriesInstanceUID) references Series(SeriesInstanceUID))");
            statement.executeUpdate("create table listener (pk integer primary key GENERATED ALWAYS AS IDENTITY,aetitle varchar(255),port varchar(255),storagelocation varchar(255))");
            statement.executeUpdate("create table servers(pk integer primary key GENERATED ALWAYS AS IDENTITY,logicalname varchar(255) NOT NULL UNIQUE,aetitle varchar(255),hostname varchar(255),port integer,retrievetype varchar(100),showpreviews boolean,wadocontext varchar(100),wadoport integer,wadoprotocol varchar(100),retrievets varchar(255))");
            statement.executeUpdate("create table theme(pk integer primary key GENERATED ALWAYS AS IDENTITY,name varchar(255),status boolean)");
            statement.executeUpdate("create table buttons(pk integer primary key GENERATED ALWAYS AS IDENTITY,buttonno integer,description varchar(255),modality varchar(255),datecriteria varchar(255),timecriteria varchar(255),iscustomdate boolean,iscustomtime boolean)");
            statement.executeUpdate("create table modality(pk integer primary key GENERATED ALWAYS AS IDENTITY,logicalname varchar(255),shortname varchar(255),status boolean)");
            statement.executeUpdate("create table presets(pk integer primary key GENERATED ALWAYS AS IDENTITY,presetname varchar(255),windowwidth numeric,windowlevel numeric,modality_fk integer,foreign key(modality_fk) references modality(pk))");
            statement.executeUpdate("create table locale (pk integer primary key GENERATED ALWAYS AS IDENTITY,countrycode varchar(10),country varchar(255),languagecode varchar(10),language varchar(255),localeid varchar(255),status boolean)");
            statement.executeUpdate("create table miscellaneous(Loopback boolean,JNLPRetrieveType varchar(25),AllowDynamicRetrieveType boolean)");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.INFO, "DatabaseHandler", ex);
        }
    }

    public boolean checkRecordExists(String tablename, String fieldname, String compareWith) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("select count(" + fieldname + ") from " + tablename + " where " + fieldname + " = '" + compareWith.trim() + "'");
            rs.next();
            if (rs.getInt(1) > 0) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void upgradeDatabase() {
        try {
            //From version 2.0Beta to 2.0
            conn.createStatement().executeQuery("select SOPClassUID from image");
        } catch (SQLException ex) {
            try {
                conn.createStatement().execute("alter table image add column SOPClassUID varchar(255)");
            } catch (SQLException ex1) {
                ApplicationContext.logger.log(Level.INFO, "DatabaseHandler", ex1);
            }
        }
        try {
            ResultSet tableInfo = conn.createStatement().executeQuery("select * from miscellaneous");
            ResultSetMetaData metaData = tableInfo.getMetaData();
            tableInfo.close();
            if (metaData.getColumnCount() == 1) {
                //From version 2.0 to 2.1
                conn.createStatement().execute("alter table miscellaneous add column JNLPRetrieveType varchar(25)");
                conn.createStatement().execute("alter table miscellaneous add column AllowDynamicRetrieveType boolean");

                conn.createStatement().execute("update miscellaneous set JNLPRetrieveType='C-GET',AllowDynamicRetrieveType=false");
                conn.createStatement().execute("update listener set StorageLocation='" + ApplicationContext.getAppDirectory() + File.separator + "archive'");
                addNewLocale("it_IT");
            }
//            //From version 2.0 to 2.1
//            ResultSet imageTable = conn.createStatement().executeQuery("select SliceLocation from image");
//            ResultSetMetaData metaData1 = imageTable.getMetaData();  
//            if(metaData1.getColumnTypeName(1).equalsIgnoreCase("INTEGER")) {
//                    
//            }
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.INFO, "Database Hanlder", ex);
        }
    }

    //insertions
    private void insertDefaultLisenerDetails() throws SQLException {
        conn.createStatement().execute("insert into listener(aetitle,port,storagelocation) values('MAYAM','1025','" + ApplicationContext.getAppDirectory() + File.separator + "archive')");
    }

    private void insertModalities() throws SQLException {
        String modality[] = {"CT", "MR", "XA", "CR", "SC", "NM", "RF", "DX", "US", "PX", "OT", "DR", "SR", "MG", "RG"};
        for (int i = 0; i < modality.length; i++) {
            conn.createStatement().execute("insert into modality(logicalname,shortname,status) values('Dummy','" + modality[i] + "',true)");
        }
    }

    private void insertDefaultPresets() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("select pk from modality where shortname='CT'");
        rs.next();
        int pk = rs.getInt("pk");
        rs.close();
        conn.createStatement().execute("insert into presets(presetname,windowwidth,windowlevel,modality_fk)values('CT Abdomen',40,350," + pk + ")");
        conn.createStatement().execute("insert into presets(presetname,windowwidth,windowlevel,modality_fk)values('CT Lung',1500,-600," + pk + ")");
        conn.createStatement().execute("insert into presets(presetname,windowwidth,windowlevel,modality_fk)values('CT Brain',80,40," + pk + ")");
        conn.createStatement().execute("insert into presets(presetname,windowwidth,windowlevel,modality_fk)values('CT Bone',2500,480," + pk + ")");
        conn.createStatement().execute("insert into presets(presetname,windowwidth,windowlevel,modality_fk)values('CT Head/Neck',350,90," + pk + ")");
    }

    private void insertDefaultThemes() throws SQLException {
        conn.createStatement().execute("insert into theme(name,status)values('Nimrod',true)");
        conn.createStatement().execute("insert into theme(name,status)values('Motif',false)");
        conn.createStatement().execute("insert into theme(name,status)values('System',false)");
    }

    public void insertServer(ServerModel serverModel) {
        try {
            conn.createStatement().execute("insert into servers(logicalname,aetitle,hostname,port,retrievetype,showpreviews,wadocontext,wadoport,wadoprotocol,retrievets) values('" + serverModel.getDescription() + "','" + serverModel.getAeTitle() + "','" + serverModel.getHostName() + "'," + serverModel.getPort() + ",'" + serverModel.getRetrieveType() + "'," + serverModel.isPreviewEnabled() + ",'" + serverModel.getWadoURL() + "'," + serverModel.getWadoPort() + ",'" + serverModel.getWadoProtocol() + "','" + serverModel.getRetrieveTransferSyntax() + "')");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void insertButton(ButtonsModel buttonsModel) {
        try {
            ResultSet noInfo = conn.createStatement().executeQuery("select max(buttonno) from buttons");
            noInfo.next();
            conn.createStatement().execute("insert into buttons(buttonno,description,modality,datecriteria,timecriteria,iscustomdate,iscustomtime) values(" + (noInfo.getInt(1) + 1) + ",'" + buttonsModel.getButtonlable() + "','" + buttonsModel.getModality() + "','" + buttonsModel.getStudyDate() + "','" + buttonsModel.getStudyTime() + "','" + buttonsModel.isCustomDate() + "','" + buttonsModel.isCustomTime() + "')");
            noInfo.close();
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void insertPreset(PresetModel presetModel, String modality) {
        try {
            ResultSet modalityInfo = conn.createStatement().executeQuery("select pk from modality where shortname='" + modality + "'");
            modalityInfo.next();
            conn.createStatement().execute("insert into presets(presetname,windowwidth,windowlevel,modality_fk)values('" + presetModel.getPresetName() + "'," + presetModel.getWindowWidth() + "," + presetModel.getWindowLevel() + "," + modalityInfo.getInt("pk") + ")");
            modalityInfo.close();
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void insertDefaultLocales() throws SQLException {
        conn.createStatement().execute("insert into locale (countrycode,country,languagecode,language,localeid,status) values('GB','United Kingdom','en','English','en_GB',true)");
        addNewLocale("ta_IN");
        addNewLocale("it_IT");
    }

    private void addNewLocale(String localeid) throws SQLException {
        String languagecode = "", countrycode = "";
        String languageAndCountry[] = localeid.split("_");
        if (languageAndCountry.length >= 2) {
            languagecode = languageAndCountry[0];
            countrycode = languageAndCountry[1];
        }
        Locale locale = new Locale(languagecode, countrycode);
        String language = locale.getDisplayLanguage();
        String country = locale.getDisplayCountry();
        insertLocale(language, country, languagecode, countrycode, localeid);
    }

    private void insertLocale(String language, String country, String languagecode, String countrycode, String localeid) throws SQLException {
        conn.createStatement().execute("insert into locale(countrycode,country,languagecode,language,localeid,status) values('" + countrycode + "','" + country + "','" + languagecode + "','" + language + "','" + localeid + "',false)");
    }

    private void insertMiscellaneous() throws SQLException {
        conn.createStatement().execute("insert into miscellaneous(Loopback,JNLPRetrieveType,AllowDynamicRetrieveType) values(true,'C-GET',false)");
    }

    public synchronized void writeDatasetInfo(DicomObject dataset, boolean saveAsLink, String filePath) {
        try {            
            insertPatientInfo(dataset);
            insertStudyInfo(dataset, saveAsLink);
            insertSeriesInfo(dataset);
            insertImageInfo(dataset, filePath);
            if (ApplicationContext.mainScreenObj != null) {
                if (dataset.getString(Tags.NumberOfFrames) != null) {
                    ApplicationContext.mainScreenObj.setProgressIndeterminate();
                }
                ApplicationContext.mainScreenObj.incrementProgressValue();
            }
        } catch (Exception e) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Failed to update patient information", e);
        }
    }

    public void insertPatientInfo(DicomObject dataset) {
        if (!(checkRecordExists("patient", "PatientId", dataset.getString(Tags.PatientID)))) {
            String date = "";
            if (dataset.getString(Tags.PatientBirthDate) != null && dataset.getString(Tags.PatientBirthDate).length() > 0) {
                date = (dataset.getDate(Tags.PatientBirthDate) != null) ? dateFormat.format(dataset.getDate(Tags.PatientBirthDate)) : "";
            }
            try {
                PreparedStatement insertStmt = conn.prepareStatement("insert into patient values(?,?,?,?)");
                insertStmt.setString(1, dataset.getString(Tags.PatientID));
                insertStmt.setString(2, dataset.getString(Tags.PatientName));
                insertStmt.setString(3, date);
                insertStmt.setString(4, dataset.getString(Tags.PatientSex));
                insertStmt.execute();
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Unable to save patient information", ex);
            }
        }
    }

    public void insertStudyInfo(DicomObject dataset, boolean saveAsLink, String patientID) {
        if (!(checkRecordExists("study", "StudyInstanceUID", dataset.getString(Tags.StudyInstanceUID)))) {
            try {
                String date = (dataset.getDate(Tags.StudyDate) != null && dataset.getString(Tags.StudyDate).length() > 0) ? dateFormat.format(dataset.getDate(Tags.StudyDate)) : "";
                String time = (dataset.getDate(Tags.StudyTime) != null && dataset.getString(Tags.StudyTime).length() > 0) ? timeFormat.format(dataset.getDate(Tags.StudyTime)) : "";
                String accessionNo = (dataset.getString(Tags.AccessionNumber) != null && dataset.getString(Tags.AccessionNumber).length() > 0) ? dataset.getString(Tags.AccessionNumber) : "";
                String refName = (dataset.getString(Tags.ReferringPhysicianName) != null && dataset.getString(Tags.ReferringPhysicianName).length() > 0) ? dataset.getString(Tags.ReferringPhysicianName) : "";
                String retAe = (dataset.getString(Tags.RetrieveAET) != null && dataset.getString(Tags.RetrieveAET).length() > 0) ? dataset.getString(Tags.RetrieveAET) : "";
                String studyDesc = (dataset.getString(Tags.StudyDescription) != null && dataset.getString(Tags.StudyDescription).length() > 0) ? dataset.getString(Tags.StudyDescription) : "";
                String studyType = saveAsLink ? "link" : "local";
                conn.createStatement().execute("insert into study values('" + dataset.getString(Tags.StudyInstanceUID) + "','" + date + "','" + time + "','" + accessionNo + "','" + refName + "','" + studyDesc.replace('/', ' ') + "','" + dataset.getString(Tags.Modality) + "'," + 0 + "," + 0 + "," + 0 + "," + 0 + ",'" + retAe + "','" + studyType + "'," + "false,'" + patientID + "')");
                SwingUtilities.invokeLater(refresher);
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Unable to save patient information", ex);
            }
        }
    }

    public void insertStudyInfo(DicomObject dataset, boolean saveAsLink) {
        if (!(checkRecordExists("study", "StudyInstanceUID", dataset.getString(Tags.StudyInstanceUID)))) {
            try {
                String date = (dataset.getDate(Tags.StudyDate) != null && dataset.getString(Tags.StudyDate).length() > 0) ? dateFormat.format(dataset.getDate(Tags.StudyDate)) : "";
                String time = (dataset.getDate(Tags.StudyTime) != null && dataset.getString(Tags.StudyTime).length() > 0) ? timeFormat.format(dataset.getDate(Tags.StudyTime)) : "";
                String accessionNo = (dataset.getString(Tags.AccessionNumber) != null && dataset.getString(Tags.AccessionNumber).length() > 0) ? dataset.getString(Tags.AccessionNumber) : "";
                String refName = (dataset.getString(Tags.ReferringPhysicianName) != null && dataset.getString(Tags.ReferringPhysicianName).length() > 0) ? dataset.getString(Tags.ReferringPhysicianName) : "";
                String retAe = (dataset.getString(Tags.RetrieveAET) != null && dataset.getString(Tags.RetrieveAET).length() > 0) ? dataset.getString(Tags.RetrieveAET) : "";
                String studyDesc = (dataset.getString(Tags.StudyDescription) != null && dataset.getString(Tags.StudyDescription).length() > 0) ? dataset.getString(Tags.StudyDescription) : "";
                conn.createStatement().execute("insert into study values('" + dataset.getString(Tags.StudyInstanceUID) + "','" + date + "','" + time + "','" + accessionNo + "','" + refName + "','" + studyDesc.replace('/', ' ') + "','" + dataset.getString(Tags.Modality) + "'," + 0 + "," + 0 + "," + 0 + "," + 0 + ",'" + retAe + "','" + (!saveAsLink ? "local" : "link") + "'," + "false,'" + dataset.getString(Tags.PatientID) + "')");
                if (ApplicationContext.isLocal) {
                    SwingUtilities.invokeLater(refresher);
                }
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Unable to save patient information", ex);
            }
        }
    }

    public void insertSeriesInfo(final DicomObject dataset, String patientId, String studyUid) {
        if (!(checkRecordExists("series", "SeriesInstanceUID", dataset.getString(Tags.SeriesInstanceUID)))) {
            String date = (dataset.getString(Tags.SeriesDate) != null && dataset.getString(Tags.SeriesDate).length() > 0) ? dateFormat.format(dataset.getDate(Tags.SeriesDate)) : "";
            String time = (dataset.getString(Tags.SeriesTime) != null && dataset.getString(Tags.SeriesTime).length() > 0) ? timeFormat.format(dataset.getDate(Tags.SeriesTime)) : "";
            int numSeries = (dataset.getString(Tags.NumberOfSeriesRelatedInstances) != null && dataset.getString(Tags.NumberOfSeriesRelatedInstances).length() > 0) ? dataset.getInt(Tags.NumberOfSeriesRelatedInstances) : 0;
            String institution = (dataset.getString(Tags.InstitutionName) != null && dataset.getString(Tags.InstitutionName).length() > 0) ? dataset.getString(Tags.InstitutionName) : "";
            String seriesNo = (dataset.getString(Tags.SeriesNumber) != null && dataset.getString(Tags.SeriesNumber).length() > 0) ? dataset.getString(Tags.SeriesNumber) : "";
            String modality = (dataset.getString(Tags.Modality) != null && dataset.getString(Tags.Modality).length() > 0) ? dataset.getString(Tags.Modality) : "";
            String seriesDesc = (dataset.getString(Tags.SeriesDescription) != null && dataset.getString(Tags.SeriesDescription).length() > 0) ? dataset.getString(Tags.SeriesDescription) : "";
            String bodyPartExamined = (dataset.getString(Tags.BodyPartExamined) != null && dataset.getString(Tags.BodyPartExamined).length() > 0) ? dataset.getString(Tags.BodyPartExamined) : "";
            try {
                conn.createStatement().execute("insert into series values('" + dataset.getString(Tags.SeriesInstanceUID) + "','" + seriesNo + "','" + date + "','" + time + "','" + modality + "','" + seriesDesc + "','" + bodyPartExamined + "','" + institution + "'," + numSeries + ",'" + patientId + "','" + studyUid + "')");
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Unable to save series information", ex);
            }
            update("study", "NoOfSeries", getStudyLevelSeries(studyUid), "StudyInstanceUID", studyUid);
            ApplicationContext.updateSeries(studyUid, new SeriesNode(dataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), dataset.getString(Tags.SeriesNumber), dataset.getString(Tags.SeriesDescription), dataset.getString(Tags.BodyPartExamined), dataset.getString(Tags.SeriesDate), dataset.getString(Tags.SeriesTime), dataset.getString(Tags.NumberOfFrames) != null, (dataset.getString(Tags.SOPInstanceUID)), dataset.getInt(Tags.NumberOfSeriesRelatedInstances)));
        }
    }

    public void insertSeriesInfo(final DicomObject dataset) {
        if (!(checkRecordExists("series", "SeriesInstanceUID", dataset.getString(Tags.SeriesInstanceUID)))) {
            String date = (dataset.getString(Tags.SeriesDate) != null && dataset.getString(Tags.SeriesDate).length() > 0) ? dateFormat.format(dataset.getDate(Tags.SeriesDate)) : "";
            String time = (dataset.getString(Tags.SeriesTime) != null && dataset.getString(Tags.SeriesTime).length() > 0) ? timeFormat.format(dataset.getDate(Tags.SeriesTime)) : "";
            int numSeries = (dataset.getString(Tags.NumberOfSeriesRelatedInstances) != null && dataset.getString(Tags.NumberOfSeriesRelatedInstances).length() > 0) ? dataset.getInt(Tags.NumberOfSeriesRelatedInstances) : 0;
            String institution = (dataset.getString(Tags.InstitutionName) != null && dataset.getString(Tags.InstitutionName).length() > 0) ? dataset.getString(Tags.InstitutionName) : "";
            String seriesNo = (dataset.getString(Tags.SeriesNumber) != null && dataset.getString(Tags.SeriesNumber).length() > 0) ? dataset.getString(Tags.SeriesNumber) : "";
            String modality = (dataset.getString(Tags.Modality) != null && dataset.getString(Tags.Modality).length() > 0) ? dataset.getString(Tags.Modality) : "";
            String seriesDesc = (dataset.getString(Tags.SeriesDescription) != null && dataset.getString(Tags.SeriesDescription).length() > 0) ? dataset.getString(Tags.SeriesDescription) : "";
            String bodyPartExamined = (dataset.getString(Tags.BodyPartExamined) != null && dataset.getString(Tags.BodyPartExamined).length() > 0) ? dataset.getString(Tags.BodyPartExamined) : "";
            try {
                conn.createStatement().execute("insert into series values('" + dataset.getString(Tags.SeriesInstanceUID) + "','" + seriesNo + "','" + date + "','" + time + "','" + modality + "','" + seriesDesc + "','" + bodyPartExamined + "','" + institution + "'," + numSeries + ",'" + dataset.getString(Tags.PatientID) + "','" + dataset.getString(Tags.StudyInstanceUID) + "')");
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Unable to save series information", ex);
            }
        }
    }

    public void insertImageInfo(DicomObject dataset, String filePath) {
        if (!(checkRecordExists("image", "SopUID", dataset.getString(Tags.SOPInstanceUID)))) {
            boolean multiframe = false;
            int totalFrame = 0;
            boolean encapsulatedPDF = false;

            if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                encapsulatedPDF = true;
            }
            if (dataset.getString(Tags.NumberOfFrames) != null && Integer.parseInt(dataset.getString(Tags.NumberOfFrames)) > 1) {
                multiframe = true;
                totalFrame = dataset.getInt(Tags.NumberOfFrames);
            }
            String frameOfRefUid = dataset.getString(Tags.FrameOfReferenceUID) != null ? dataset.getString(Tags.FrameOfReferenceUID) : "";
            String imgPos = dataset.getBytes(Tags.ImagePosition) != null ? new String(dataset.getBytes(Tags.ImagePosition)) : "";
            String imgOrientation = dataset.getBytes(Tags.ImageOrientation) != null ? new String(dataset.getBytes(Tags.ImageOrientation)) : "null";
            String pixelSpacing = dataset.getBytes(Tags.PixelSpacing) != null ? new String(dataset.getBytes(Tags.PixelSpacing)) : "";
            int row = dataset.getInt(Tags.Rows) != 0 ? dataset.getInt(Tags.Rows) : 1;
            int columns = dataset.getInt(Tags.Columns) != 0 ? dataset.getInt(Tags.Columns) : 1;
            String referSopInsUid = "", image_type = "";
            String sliceThickness = dataset.getBytes(Tags.SliceThickness) != null ? new String(dataset.getBytes(Tags.SliceThickness)) : "";
            //To get the Referenced SOP Instance UID
            DicomElement refImageSeq = dataset.get(Tag.ReferencedImageSequence);
            if (refImageSeq != null) {
                if (refImageSeq.hasItems()) {
                    DicomObject dcmObj1 = refImageSeq.getDicomObject();
                    referSopInsUid = dcmObj1.get(Tag.ReferencedSOPInstanceUID) != null ? new String(dcmObj1.get(Tag.ReferencedSOPInstanceUID).getBytes()) : "";
                }
            }
            //To get the Image Type (LOCALIZER / AXIAL / OTHER)
            image_type = dataset.getBytes(Tags.ImageType) != null ? new String(dataset.getBytes(Tags.ImageType)) : "";
            String[] imageTypes = image_type.split("\\\\");
            if (imageTypes.length >= 3) {
                image_type = imageTypes[2];
            }
            String[] imagePosition = dataset.getStrings(Tags.ImagePosition);
            String sliceLoc = imagePosition != null && imagePosition[2] != null ? imagePosition[2] : "0";
            try {
                conn.createStatement().executeUpdate("insert into image(SopUID,SOPClassUID,InstanceNo,multiframe,totalframe,SendStatus,ForwardDateTime,ReceivedDateTime,ReceiveStatus,FileStoreUrl,SliceLocation,EncapsulatedDocument,ThumbnailStatus,FrameOfReferenceUID,ImagePosition,ImageOrientation,ImageType,PixelSpacing,SliceThickness,NoOfRows,NoOfColumns,ReferencedSopUid,PatientId,StudyInstanceUID,SeriesInstanceUID) values('" + dataset.getString(Tags.SOPInstanceUID) + "','" + dataset.getString(Tags.SOPClassUID) + "'," + dataset.getInt(Tags.InstanceNumber) + ",'" + multiframe + "','" + totalFrame + "','" + "partial" + "','" + " " + "','" + " " + "','" + "partial" + "','" + filePath + "'," + sliceLoc + ",'" + encapsulatedPDF + "',false,'" + frameOfRefUid + "','" + imgPos + "','" + imgOrientation + "','" + image_type + "','" + pixelSpacing + "','" + sliceThickness + "'," + row + "," + columns + ",'" + referSopInsUid.trim() + "','" + dataset.getString(Tags.PatientID) + "','" + dataset.getString(Tags.StudyInstanceUID) + "','" + dataset.getString(Tags.SeriesInstanceUID) + "')");
                conn.commit();
                if (dataset.getString(Tags.SOPClassUID).equals(UID.VideoEndoscopicImageStorage) || dataset.getString(Tags.SOPClassUID).equals(UID.VideoMicroscopicImageStorage) || dataset.getString(Tags.SOPClassUID).equals(UID.VideoPhotographicImageStorage)) {
                    String storeLoc = new File(filePath).getParentFile() + File.separator + dataset.getString(Tags.SOPInstanceUID) + "_V";
                    ApplicationContext.convertVideo(filePath, storeLoc, dataset.getString(Tags.SOPInstanceUID));
                }
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Unable to save instance information", ex);
            }
        }
    }

    public void insertImageInfo(DicomObject dataset, String filePath, boolean isLink, boolean updateMainScreen, String patientID, String studyUid, String seriesUid) {
        if (!(checkRecordExists("image", "SopUID", dataset.getString(Tags.SOPInstanceUID)))) {
            boolean multiframe = false;
            int totalFrame = 0;
            boolean encapsulatedPDF = false;

            if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                encapsulatedPDF = true;
            }

            if (dataset.getString(Tags.NumberOfFrames) != null && Integer.parseInt(dataset.getString(Tags.NumberOfFrames)) > 1) {
                multiframe = true;
                totalFrame = dataset.getInt(Tags.NumberOfFrames);
            }
            String frameOfRefUid = dataset.getString(Tags.FrameOfReferenceUID) != null ? dataset.getString(Tags.FrameOfReferenceUID) : "";
            String imgPos = dataset.getBytes(Tags.ImagePosition) != null ? new String(dataset.getBytes(Tags.ImagePosition)) : "";
            String imgOrientation = dataset.getBytes(Tags.ImageOrientation) != null ? new String(dataset.getBytes(Tags.ImageOrientation)) : "null";
            String pixelSpacing = dataset.getBytes(Tags.PixelSpacing) != null ? new String(dataset.getBytes(Tags.PixelSpacing)) : "";
            int row = dataset.getInt(Tags.Rows) != 0 ? dataset.getInt(Tags.Rows) : 1;
            int columns = dataset.getInt(Tags.Columns) != 0 ? dataset.getInt(Tags.Columns) : 1;
            String referSopInsUid = "", image_type = "";
            String sliceThickness = dataset.getBytes(Tags.SliceThickness) != null ? new String(dataset.getBytes(Tags.SliceThickness)) : "";
            //To get the Referenced SOP Instance UID
            DicomElement refImageSeq = dataset.get(Tag.ReferencedImageSequence);
            if (refImageSeq != null) {
                if (refImageSeq.hasItems()) {
                    DicomObject dcmObj1 = refImageSeq.getDicomObject();
                    referSopInsUid = dcmObj1.get(Tag.ReferencedSOPInstanceUID) != null ? new String(dcmObj1.get(Tag.ReferencedSOPInstanceUID).getBytes()) : "";
                }
            }
            //To get the Image Type (LOCALIZER / AXIAL / OTHER)
            image_type = dataset.getBytes(Tags.ImageType) != null ? new String(dataset.getBytes(Tags.ImageType)) : "";
            String[] imageTypes = image_type.split("\\\\");
            if (imageTypes.length >= 3) {
                image_type = imageTypes[2];
            }
            String[] imagePosition = dataset.getStrings(Tags.ImagePosition);
            String sliceLoc = imagePosition != null && imagePosition[2] != null ? imagePosition[2] : "0";
            try {
                conn.createStatement().executeUpdate("insert into image(SopUID,SOPClassUID,InstanceNo,multiframe,totalframe,SendStatus,ForwardDateTime,ReceivedDateTime,ReceiveStatus,FileStoreUrl,SliceLocation,EncapsulatedDocument,ThumbnailStatus,FrameOfReferenceUID,ImagePosition,ImageOrientation,ImageType,PixelSpacing,SliceThickness,NoOfRows,NoOfColumns,ReferencedSopUid,PatientId,StudyInstanceUID,SeriesInstanceUID) values('" + dataset.getString(Tags.SOPInstanceUID) + "','" + dataset.getString(Tags.SOPClassUID) + "'," + dataset.getInt(Tags.InstanceNumber) + ",'" + multiframe + "','" + totalFrame + "','" + "partial" + "','" + " " + "','" + " " + "','" + "partial" + "','" + filePath + "'," + sliceLoc + ",'" + encapsulatedPDF + "',false,'" + frameOfRefUid + "','" + imgPos + "','" + imgOrientation + "','" + image_type + "','" + pixelSpacing + "','" + sliceThickness + "'," + row + "," + columns + ",'" + referSopInsUid.trim() + "','" + patientID + "','" + studyUid + "','" + seriesUid + "')");
                conn.commit();
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler - Unable to save instance information", ex);
                ex.printStackTrace();
            }
        }
    }
    //Accessing Data  

    public String[] getListenerDetails() {
        String detail[] = new String[3];
        try {
            ResultSet listenerInfo = conn.createStatement().executeQuery("select * from listener");
            while (listenerInfo.next()) {
                detail[0] = listenerInfo.getString("aetitle");
                detail[1] = listenerInfo.getString("port");
                detail[2] = listenerInfo.getString("storagelocation");
            }
            listenerInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return detail;
    }

    public ArrayList<ServerModel> getServerList() {
        ArrayList<ServerModel> serverList = new ArrayList<ServerModel>();
        try {
            ResultSet serverInfo = conn.createStatement().executeQuery("select * from servers");
            while (serverInfo.next()) {
                serverList.add(new ServerModel(serverInfo.getInt("pk"), serverInfo.getString("logicalname"), serverInfo.getString("aetitle"), serverInfo.getString("hostname"), serverInfo.getInt("port"), serverInfo.getString("retrievetype"), serverInfo.getString("wadocontext"), serverInfo.getInt("wadoport"), serverInfo.getString("wadoprotocol"), serverInfo.getString("retrievets"), serverInfo.getBoolean("showpreviews")));
            }
            serverInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return serverList;
    }

    public ArrayList<String> getAllServerNames() {
        ArrayList<String> serverNames = new ArrayList<String>();
        try {
            ResultSet serverInfo = conn.createStatement().executeQuery("select logicalname from servers");
            while (serverInfo.next()) {
                serverNames.add(serverInfo.getString("logicalname"));
            }
            serverInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return serverNames;
    }

    public String getRetrieveType(String serverName) {
        try {
            ResultSet serverNameInfo = conn.createStatement().executeQuery("select retrievetype from servers where logicalname='" + serverName + "'");
            serverNameInfo.next();
            return serverNameInfo.getString("retrievetype");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public boolean isPreviewsEnabled(String serverName) {
        try {
            ResultSet serverInfo = conn.createStatement().executeQuery("select showpreviews from servers where logicalname='" + serverName + "'");
            serverInfo.next();
            return serverInfo.getBoolean("showpreviews");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return false;
    }

    public boolean isDownloadPending(String studyUid) {
        try {
            ResultSet pendingInfo = conn.createStatement().executeQuery("select DownloadStatus from study where StudyInstanceUID='" + studyUid + "'");
            while (pendingInfo.next()) {
                return pendingInfo.getBoolean("DownloadStatus");
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return false;
    }

    public ServerModel getServerDetails(String serverName) {
        try {
            ResultSet serverInfo = conn.createStatement().executeQuery("select * from servers where logicalname='" + serverName + "'");
            while (serverInfo.next()) {
                return new ServerModel(serverInfo.getString("logicalname"), serverInfo.getString("aetitle"), serverInfo.getString("hostname"), serverInfo.getInt("port"), serverInfo.getString("retrievetype"), serverInfo.getString("wadocontext"), serverInfo.getInt("wadoport"), serverInfo.getString("wadoprotocol"), serverInfo.getString("retrievets"), serverInfo.getBoolean("showpreviews"));
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public ArrayList<ButtonsModel> getAllQueryButtons() {
        ArrayList<ButtonsModel> buttons = new ArrayList<ButtonsModel>();
        try {
            ResultSet buttonsInfo = conn.createStatement().executeQuery("select * from buttons order by buttonno");
            while (buttonsInfo.next()) {
                buttons.add(new ButtonsModel(buttonsInfo.getString("description"), buttonsInfo.getString("modality"), buttonsInfo.getString("datecriteria"), buttonsInfo.getString("timecriteria"), buttonsInfo.getBoolean("iscustomdate"), buttonsInfo.getBoolean("iscustomtime")));
            }
            buttonsInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return buttons;
    }

    public ArrayList<String> getAllButtonNames() {
        ArrayList<String> buttonNames = new ArrayList<String>();
        try {
            ResultSet buttonsInfo = conn.createStatement().executeQuery("select description from buttons order by buttonno");
            while (buttonsInfo.next()) {
                buttonNames.add(buttonsInfo.getString("description"));
            }
            buttonsInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return buttonNames;
    }

    public ButtonsModel getButtonDetails(String description) {
        try {
            ResultSet buttonInfo = conn.createStatement().executeQuery("select * from buttons where description='" + description + "'");
            buttonInfo.next();
            return new ButtonsModel(buttonInfo.getString("description"), buttonInfo.getString("modality"), buttonInfo.getString("datecriteria"), buttonInfo.getString("timecriteria"), buttonInfo.getBoolean("iscustomdate"), buttonInfo.getBoolean("iscustomtime"));
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public String getActiveTheme() {
        try {
            ResultSet activeThemeInfo = conn.createStatement().executeQuery("select name from theme where status=true");
            activeThemeInfo.next();
            return activeThemeInfo.getString("name");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public ArrayList<String> getThemes() {
        ArrayList<String> themeNames = new ArrayList<String>();
        try {
            ResultSet themeInfo = conn.createStatement().executeQuery("select name from theme");
            while (themeInfo.next()) {
                if (!themeInfo.getString("name").equals("System")) {
                    themeNames.add(themeInfo.getString("name"));
                } else {
                    themeNames.add(System.getProperty("os.name"));
                }
            }
            themeInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return themeNames;
    }

    public ArrayList<String> getActiveModalities() {
        ArrayList<String> modalities = new ArrayList<String>();
        try {
            ResultSet modalityInfo = conn.createStatement().executeQuery("select shortname from modality where status=true");
            while (modalityInfo.next()) {
                modalities.add(modalityInfo.getString("shortname"));
            }
            modalityInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return modalities;
    }

    public Vector<String> getAllModalities() {
        Vector<String> modalities = new Vector<String>();
        try {
            ResultSet modalityInfo = conn.createStatement().executeQuery("select shortname from modality");
            while (modalityInfo.next()) {
                modalities.add(modalityInfo.getString("shortname"));
            }
            modalityInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return modalities;
    }

    public boolean isModalityActive(String shortname) {
        try {
            ResultSet isActive = conn.createStatement().executeQuery("select status from modality where shortname='" + shortname + "'");
            isActive.next();
            return isActive.getBoolean("status");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return false;
    }

    public ArrayList<PresetModel> getPresetsForModality(String modality) {
        ArrayList<PresetModel> presets = new ArrayList<PresetModel>();
        try {
            ResultSet modalityInfo = conn.createStatement().executeQuery("select pk from modality where shortname='" + modality + "'");
            modalityInfo.next();
            ResultSet presetInfo = conn.createStatement().executeQuery("select * from presets where modality_fk=" + modalityInfo.getInt("pk"));
            while (presetInfo.next()) {
                PresetModel preset = new PresetModel(presetInfo.getInt("pk"), modality, presetInfo.getString("presetname"), presetInfo.getString("windowwidth"), presetInfo.getString("windowlevel"));
                presets.add(preset);
            }
            modalityInfo.close();
            presetInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return presets;
    }

    public String[] getActiveLanguage() {
        try {
            ResultSet resultSet = conn.createStatement().executeQuery("select * from locale where status=true");
            while (resultSet.next()) {
                return new String[]{resultSet.getString("countrycode"), resultSet.getString("country"), resultSet.getString("languagecode"), resultSet.getString("language"), resultSet.getString("localeid")};
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public String[] getCountryList() {
        try {
            ResultSet count = conn.createStatement().executeQuery("select count(distinct country) from locale");
            count.next();
            String[] countryList = new String[count.getInt(1)];
            int index = 0;
            ResultSet result = conn.createStatement().executeQuery("select distinct country from locale");
            while (result.next()) {
                countryList[index] = result.getString("country");
                index++;
            }
            count.close();
            result.close();
            return countryList;
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public String[] getLanguagesOfCountry(String country) {
        try {
            ResultSet count = conn.createStatement().executeQuery("select count(distinct language) from locale where country='" + country + "'");
            count.next();
            String languageList[] = new String[count.getInt(1)];
            ResultSet result = conn.createStatement().executeQuery("select distinct language from locale where country='" + country + "'");
            int index = 0;
            while (result.next()) {
                languageList[index] = result.getString("language");
                index++;
            }
            count.close();
            result.close();
            return languageList;
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public String[] getLocaleIDForCountryAndLanguage(String country, String language) {
        try {
            ResultSet count = conn.createStatement().executeQuery("select count(localeid) from locale where country='" + country + "' and language='" + language + "'");
            count.next();
            String[] localeId = new String[count.getInt(1)];
            ResultSet result = conn.createStatement().executeQuery("select localeid from locale where country='" + country + "' and language='" + language + "'");
            int index = 0;
            while (result.next()) {
                localeId[index] = result.getString("localeid");
                index++;
            }
            count.close();
            result.close();
            return localeId;
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }

        return null;
    }

    public ArrayList<StudyNode> listAllLocalStudies() {
        ArrayList<StudyNode> studies = new ArrayList<StudyNode>();
        try {
            ResultSet studyInfo = conn.createStatement().executeQuery("select * from study");
            while (studyInfo.next()) {
                ResultSet patientInfo = conn.createStatement().executeQuery("select PatientName,PatientBirthDate from patient where PatientId='" + studyInfo.getString("PatientId") + "'");
                patientInfo.next();
                StudyNode studyNode = new StudyNode(studyInfo.getString("PatientId"), patientInfo.getString("PatientName"), patientInfo.getString("PatientBirthDate"), studyInfo.getString("AccessionNo"), studyInfo.getString("StudyDate"), studyInfo.getString("StudyTime"), studyInfo.getString("StudyDescription"), studyInfo.getString("ModalitiesInStudy"), studyInfo.getString("NoOfSeries"), studyInfo.getString("NoOfInstances"), studyInfo.getString("StudyInstanceUID"));
                studies.add(studyNode);
                patientInfo.close();
            }
            studyInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return studies;
    }

    public boolean getLoopbackStatus() {
        try {
            ResultSet loopBackStatus = conn.createStatement().executeQuery("select Loopback from miscellaneous");
            loopBackStatus.next();
            return loopBackStatus.getBoolean("Loopback");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return false;
    }

    public int getTotalInstances(String studyUid) {
        try {
            ResultSet totalInstancesInfo = conn.createStatement().executeQuery("select NoOfInstances from study where StudyInstanceUID='" + studyUid + "'");
            totalInstancesInfo.next();
            return totalInstancesInfo.getInt("NoOfInstances");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return 0;
    }

    public int getStudyLevelInstances(String studyUid) {
        try {
            ResultSet totalInstancesInfo = conn.createStatement().executeQuery("select count(*) from image where StudyInstanceUID='" + studyUid + "'");
            totalInstancesInfo.next();
            return totalInstancesInfo.getInt(1);
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return 0;
    }

    public boolean isLink(String studyUid) {
        try {
            ResultSet linkInfo = conn.createStatement().executeQuery("select StudyType from study where StudyInstanceUID='" + studyUid + "'");
            linkInfo.next();
            return linkInfo.getString("StudyType").equals("link") ? true : false;
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return false;
    }

    public ArrayList<SeriesNode> getSeriesList_SepMultiframe(String studyUid) {
        ArrayList<SeriesNode> arr = new ArrayList<SeriesNode>();
        arr.add(new SeriesNode(null));
        try {
            String sql = "select SeriesInstanceUID,SeriesNo,SeriesDescription,BodyPartExamined,SeriesDate,SeriesTime,NoOfSeriesRelatedInstances from series where StudyInstanceUID='" + studyUid + "' order by SeriesNo";
            ResultSet rs = null;
            rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                SeriesNode series = new SeriesNode(studyUid, rs.getString("SeriesInstanceUID"), rs.getString("SeriesNo"), rs.getString("SeriesDescription"), rs.getString("BodyPartExamined"), rs.getString("SeriesDate"), rs.getString("SeriesTime"), false, null, rs.getInt("NoOfSeriesRelatedInstances"));
                ResultSet rs1 = conn.createStatement().executeQuery("select SopUID,SopClassUID from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + rs.getString("SeriesInstanceUID") + "' and multiframe=false");
                if (rs1.next()) {
                    arr.add(series);
                }
                arr.addAll(getMultiframeSeries(studyUid, series.getSeriesUID(), rs.getString("SeriesNo"), rs.getString("BodyPartExamined"), rs.getString("SeriesDate"), rs.getString("SeriesTime")));
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return arr;
    }

    public ArrayList<SeriesNode> getMultiframeSeries(String studyUID, String seriesUID, String seriesNo, String bodyPart, String seriesDate, String seriesTime) {
        ArrayList<SeriesNode> arr = new ArrayList();
        try {
            ResultSet rs1 = null;
            String sql1 = "select totalframe,SopUID,SOPClassUID from image where StudyInstanceUID='" + studyUID + "' AND " + "SeriesInstanceUID='" + seriesUID + "'" + " AND multiframe=true" + " order by InstanceNo asc";
            rs1 = conn.createStatement().executeQuery(sql1);
            while (rs1.next()) {
                int totalFrames = Integer.parseInt(rs1.getString("totalFrame"));
                SeriesNode series = new SeriesNode(studyUID, seriesUID, seriesNo, null, bodyPart, seriesDate, seriesTime, true, rs1.getString("SopUID"), totalFrames);
                if (rs1.getString("SOPClassUID") != null && (rs1.getString("SOPClassUID").equals(UID.VideoEndoscopicImageStorage) || rs1.getString("SOPClassUID").equals(UID.VideoMicroscopicImageStorage) || rs1.getString("SOPClassUID").equals(UID.VideoPhotographicImageStorage))) {
                    series.setVideoStatus(true);
                    series.setSeriesDesc("Video:" + totalFrames + " Frames");
                } else {
                    series.setSeriesDesc("Multiframe:" + totalFrames + " Frames");
                }
                series.setInstanceUIDIfMultiframe(rs1.getString("SopUID"));
                arr.add(series);
            }
        } catch (SQLException e) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", e);
        }
        return arr;
    }

    public ArrayList<String> getLocationsBasedOnSeries(String studyUid, String seriesUid) {
        ArrayList<String> locations = new ArrayList<String>();
        try {
            ResultSet locationInfo = conn.createStatement().executeQuery("select FileStoreUrl,SopUID from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and SOPClassUID not in('" + UID.VideoEndoscopicImageStorage + "','" + UID.VideoMicroscopicImageStorage + "','" + UID.VideoPhotographicImageStorage + "')");
            while (locationInfo.next()) {
                locations.add(locationInfo.getString("FileStoreUrl") + "," + locationInfo.getString("SopUID"));
            }
            locationInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return locations;
    }

    public int getSeriesLevelInstance(String studyUid, String seriesUid) {
        int totalInstance = 0;
        try {
            ResultSet rs = conn.createStatement().executeQuery("select count(*) from image where StudyInstanceUID='" + studyUid + "' AND " + "SeriesInstanceUID='" + seriesUid + "' AND multiframe = false");
            rs.next();
            totalInstance = rs.getInt(1);
            rs.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return totalInstance;
    }

    public String getInstanceUIDBasedOnSliceLocation(String studyUid, String seriesUid, String sliceLocation, String sliceThickness) {
        String fileStoreUrl = null;
        try {
            double sliceLocTemp = 0, sliceThicknessTemp = 0;
            if (sliceLocation != null && !sliceLocation.equals("")) {
                sliceLocTemp = Double.parseDouble(sliceLocation);
                if (!sliceThickness.equals("")) {
                    sliceThicknessTemp = Double.parseDouble(sliceThickness);
                }
            }
            String sql = "select SopUid,SliceLocation from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and SliceLocation between " + (sliceLocTemp - sliceThicknessTemp) + " and " + (sliceLocTemp + sliceThicknessTemp);
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) {
                fileStoreUrl = rs.getString("SopUid");
            }
            rs.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return fileStoreUrl;
    }

    public ArrayList getSeriesInstancesLocation(String studyUid) {
        ArrayList locations = new ArrayList();
        try {
            ResultSet seriesInfo = conn.createStatement().executeQuery("select SeriesInstanceUID from Series where StudyInstanceUID='" + studyUid + "' order by SeriesNo");
            while (seriesInfo.next()) {
                ResultSet location = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesInfo.getString("SeriesInstanceUID") + "' and multiframe=false" + " order by InstanceNo asc");
                ResultSet multiframesInfo = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesInfo.getString("SeriesInstanceUID") + "' and multiframe=true" + " order by InstanceNo asc");
                if (location.next()) {
                    locations.add(location.getString("FileStoreUrl"));
                }
                while (multiframesInfo.next()) {
                    locations.add(multiframesInfo.getString("FileStoreUrl"));
                }
                location.close();
                multiframesInfo.close();
            }
            seriesInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return locations;
    }

    public String getFirstInstanceLocation(String studyUid, String seriesInstanceUid) {
        try {
            ResultSet locationInfo = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesInstanceUid + "'" + " order by InstanceNo asc");
            locationInfo.next();
            return locationInfo.getString("FileStoreUrl");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public String getFirstInstanceLocation(String studyUid) {
        try {
            ResultSet locationInfo = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "' order by InstanceNo asc");
            locationInfo.next();
            return locationInfo.getString("FileStoreUrl");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public String getFileLocation(String studyUid, String seriesUid, String sopUid) {
        String path = null;
        try {
            ResultSet pathInfo = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and SopUid='" + sopUid + "'");
            if (pathInfo.next()) {
                path = pathInfo.getString("FileStoreUrl");
            }
            pathInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return path;
    }

    public String getFileLocation(String studyUid, String seriesUid, int instanceNumber) {
        int i = -1;
        try {
            ResultSet pathInfo = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' order by InstanceNo");
            while (pathInfo.next()) {
                i++;
                if (i == instanceNumber) {
                    return pathInfo.getString("FileStoreUrl");
                }
            }
            pathInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public ArrayList<StudyNode> listStudies(String patientName, String patientID, String dob, String accNo, String studyDate, String studyDesc, String modality) {
        ArrayList<StudyNode> matchingStudies = new ArrayList<StudyNode>();
        ResultSet matchingInfo;
        try {
            matchingInfo = conn.createStatement().executeQuery("select * from patient inner join study on patient.PatientId=study.PatientId where upper(patient.PatientId) like '" + patientID + "' and upper(patient.PatientName) like '" + patientName + "' and patient.PatientBirthDate like '" + dob + "' and upper(study.AccessionNo) like '" + accNo + "' and study.StudyDate like '" + studyDate + "' and upper(study.StudyDescription) like '" + studyDesc + "' and upper(study.ModalitiesInStudy) like '" + modality + "'");
            while (matchingInfo.next()) {
                StudyNode study = new StudyNode(matchingInfo.getString("PatientId"), matchingInfo.getString("PatientName"), matchingInfo.getString("PatientBirthDate"), matchingInfo.getString("AccessionNo"), matchingInfo.getString("StudyDate"), matchingInfo.getString("StudyTime"), matchingInfo.getString("StudyDescription"), matchingInfo.getString("ModalitiesInStudy"), matchingInfo.getString("NoOfSeries"), matchingInfo.getString("NoOfInstances"), matchingInfo.getString("StudyInstanceUID"));
                matchingStudies.add(study);
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        } catch (NumberFormatException nfe) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", nfe);
        }
        return matchingStudies;
    }

    public String getJNLPRetrieveType() {
        try {
            ResultSet retrieveInfo = conn.createStatement().executeQuery("select JNLPRetrieveType from miscellaneous");
            retrieveInfo.next();
            return retrieveInfo.getString("JNLPRetrieveType");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public boolean getDynamicRetrieveTypeStatus() {
        try {
            ResultSet retrieveInfo = conn.createStatement().executeQuery("select AllowDynamicRetrieveType from miscellaneous");
            retrieveInfo.next();
            return retrieveInfo.getBoolean("AllowDynamicRetrieveType");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return false;
    }

    //Added for Memeory Handling
    public ArrayList<String> getInstanceUidList(String studyUid, String seriesUid) {
        ArrayList<String> locations = new ArrayList<String>();
        try {
            ResultSet imageLocations = conn.createStatement().executeQuery("select SopUid from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and multiframe=false order by InstanceNo,FileStoreUrl");
            while (imageLocations.next()) {
                locations.add(imageLocations.getString("SopUid"));
            }
            imageLocations.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return locations;
    }

    public ScoutLineInfoModel[] getFirstAndLastInstances(String studyUid, String seriesUid) {
        ScoutLineInfoModel[] borderLines = new ScoutLineInfoModel[2];
        try {
            ResultSet scoutDetails = conn.createStatement().executeQuery("select ImagePosition,ImageOrientation,PixelSpacing,NoOfRows,NoOfColumns,FrameOfReferenceUID,ReferencedSopUid,ImageType,SliceLocation from Image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and InstanceNo in(select min(InstanceNo) from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and ImageType not in('LOCALIZER'))");
            scoutDetails.next();
            borderLines[0] = new ScoutLineInfoModel(scoutDetails.getString("ImagePosition"), scoutDetails.getString("ImageOrientation"), scoutDetails.getString("PixelSpacing"), scoutDetails.getInt("NoOfRows"), scoutDetails.getInt("NoOfColumns"), scoutDetails.getString("FrameOfReferenceUID"), scoutDetails.getString("ReferencedSopUid"), scoutDetails.getString("ImageType"), scoutDetails.getString("SliceLocation"));
            scoutDetails = conn.createStatement().executeQuery("select ImagePosition,ImageOrientation,PixelSpacing,NoOfRows,NoOfColumns,FrameOfReferenceUID,ReferencedSopUid,ImageType,SliceLocation from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and InstanceNo in(select max(InstanceNo) from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "') and ImageType not in('LOCALIZER')");
            scoutDetails.next();
            borderLines[1] = new ScoutLineInfoModel(scoutDetails.getString("ImagePosition"), scoutDetails.getString("ImageOrientation"), scoutDetails.getString("PixelSpacing"), scoutDetails.getInt("NoOfRows"), scoutDetails.getInt("NoOfColumns"), scoutDetails.getString("FrameOfReferenceUID"), scoutDetails.getString("ReferencedSopUid"), scoutDetails.getString("ImageType"), scoutDetails.getString("SliceLocation"));
            scoutDetails.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return borderLines;
    }

    public ScoutLineInfoModel getScoutLineDetails(String studyUid, String seriesUid, String instanceUid) {
        try {
            ResultSet scoutDetails = conn.createStatement().executeQuery("select ImagePosition,ImageOrientation,PixelSpacing,NoOfRows,NoOfColumns,FrameOfReferenceUID,ReferencedSopUid,ImageType,SliceLocation from Image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and SopUid='" + instanceUid + "'");
            while (scoutDetails.next()) {
                return new ScoutLineInfoModel(scoutDetails.getString("ImagePosition"), scoutDetails.getString("ImageOrientation"), scoutDetails.getString("PixelSpacing"), scoutDetails.getInt("NoOfRows"), scoutDetails.getInt("NoOfColumns"), scoutDetails.getString("FrameOfReferenceUID"), scoutDetails.getString("ReferencedSopUid"), scoutDetails.getString("ImageType"), scoutDetails.getString("SliceLocation"));
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public String getSlicePosition(String studyUid, String seriesUid, String instanceUid) {
        ResultSet sliceInfo = null;
        try {
            sliceInfo = conn.createStatement().executeQuery("select SliceLocation from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and SopUid='" + instanceUid + "'");
            sliceInfo.next();
            return sliceInfo.getString("SliceLocation");
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        } finally {
            try {
                sliceInfo.close();
            } catch (SQLException ex) {
                ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
            } catch (NullPointerException ex) {
                //ignore
            }
        }
        return null;
    }

    public String getThumbnailLocation(String studyUid, String seriesUid) {
        try {
            ResultSet info = conn.createStatement().executeQuery("select FileStoreUrl,StudyInstanceUID from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "'");
            info.next();
            if (info.getString("FileStoreUrl").contains(ApplicationContext.getAppDirectory())) {
                String location = new File(info.getString("FileStoreUrl")).getParent() + File.separator + "Thumbnails";
                info.close();
                return location;
            } else {
                return ApplicationContext.appDirectory + File.separator + "Thumbnails" + File.separator + info.getString("StudyInstanceUID");
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return null;
    }

    public int getStudyLevelSeries(String studyUid) {
        try {
            ResultSet seriesCount = conn.createStatement().executeQuery("select count(SeriesInstanceUID) from series where StudyInstanceUID='" + studyUid + "'");
            seriesCount.next();
            int toReturn = seriesCount.getInt(1);
            seriesCount.close();
            return toReturn;
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return 0;
    }

    //Export
    public HashSet<ServerModel> getServersToSend(String selectionStr) {
        HashSet<ServerModel> serversToSend = new HashSet<ServerModel>(0, 1);
        try {
            ResultSet serverInfo = conn.createStatement().executeQuery("select logicalname,aetitle,hostname,port from servers where logicalname in(" + selectionStr + ")");
            while (serverInfo.next()) {
                serversToSend.add(new ServerModel(serverInfo.getString("aetitle"), serverInfo.getString("hostname"), serverInfo.getInt("port")));
            }
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return serversToSend;
    }

    public ArrayList<String> getInstances(String studyUid, String seriesUid, String multiframe) {
        ArrayList<String> instances = new ArrayList<String>();
        String sql = "select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "'";
        if (seriesUid != null) {
            sql += " and SeriesInstanceUID='" + seriesUid + "'";
        }
        if (multiframe != null) {
            sql += " and multiframe=" + multiframe;
        }
        sql += " order by InstanceNo";
        try {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                instances.add(rs.getString("FileStoreUrl"));
            }
            rs.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return instances;
    }

    public ArrayList<String> getInstancesLocation(String studyUid, String seriesUid) {
        ArrayList<String> locations = new ArrayList<String>();
        try {
            ResultSet instanceInfo = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "'" + " order by InstanceNo asc");
            while (instanceInfo.next()) {
                locations.add(instanceInfo.getString("FileStoreUrl"));
            }
            instanceInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return locations;
    }

    public ArrayList<String> getInstancesLoc(String studyUid, String seriesUid) {
        ArrayList<String> locations = new ArrayList<String>();
        try {
            ResultSet instanceInfo = conn.createStatement().executeQuery("select FileStoreUrl,SopUID from image where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "'" + " order by InstanceNo asc");
            while (instanceInfo.next()) {
                locations.add(instanceInfo.getString("FileStoreUrl") + "," + instanceInfo.getString("SopUID"));
            }
            instanceInfo.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
        return locations;
    }

    //Updations
    public void update(String tableName, String fieldName, int fieldValue, String whereField, String whereValue) {
        try {
            conn.createStatement().executeUpdate("update " + tableName + " set " + fieldName + "=" + fieldValue + " where " + whereField + "='" + whereValue + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void update(String tableName, String fieldName, boolean fieldValue, String whereField, String whereValue) {
        try {
            conn.createStatement().executeUpdate("update " + tableName + " set " + fieldName + "=" + fieldValue + " where " + whereField + "='" + whereValue + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void update(String tableName, String fieldName, String fieldValue, String whereField, String whereValue) {
        try {
            conn.createStatement().executeUpdate("update " + tableName + " set " + fieldName + "='" + fieldValue + "' where " + whereField + "='" + whereValue + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void addInstanceCount(String studyUid, String seriesUid) {
        try {
            ResultSet studyLevelInstances = conn.createStatement().executeQuery("select NoOfInstances from study where StudyInstanceUID='" + studyUid + "'");
            studyLevelInstances.next();
            ResultSet seriesLevelInstances = conn.createStatement().executeQuery("select NoOfSeriesRelatedInstances from series where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "'");
            seriesLevelInstances.next();
            conn.createStatement().executeUpdate("update study set NoOfInstances=" + (studyLevelInstances.getInt(1) + 1) + "where StudyInstanceUID='" + studyUid + "'");
            conn.createStatement().executeUpdate("update series set NoOfSeriesRelatedInstances=" + (seriesLevelInstances.getInt("NoOfSeriesRelatedInstances") + 1) + "where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "'");
            ApplicationContext.updateInstances(studyUid, studyLevelInstances.getInt(1) + 1);
            seriesLevelInstances.close();
            studyLevelInstances.close();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateThumbnailStatus(String studyUid, String seriesUid, String sopUid) {
        try {
            conn.createStatement().executeUpdate("update image set ThumbnailStatus=true where StudyInstanceUID='" + studyUid + "' and SeriesInstanceUID='" + seriesUid + "' and SopUID='" + sopUid + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateListener(String aetitle, String port) {
        try {
            ResultSet pk = conn.createStatement().executeQuery("select pk from listener");
            pk.next();
            conn.createStatement().executeUpdate("update listener set aetitle='" + aetitle + "',port='" + port + "' where pk=" + pk.getInt("pk"));
            pk.close();
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateTheme(String themeName) {
        try {
            ResultSet activeInfo = conn.createStatement().executeQuery("select name from theme where status=true");
            activeInfo.next();
            conn.createStatement().executeUpdate("update theme set status=false where name='" + activeInfo.getString("name") + "'");
            conn.createStatement().executeUpdate("update theme set status=true where name='" + themeName + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void reArrangeButtons(String buttonToMove, String buttonToReplace) {
        String selectQuery = "select buttonno from buttons where description='";
        try {
            ResultSet buttonToMoveInfo = conn.createStatement().executeQuery(selectQuery + buttonToMove + "'");
            buttonToMoveInfo.next();
            ResultSet buttonToReplaceInfo = conn.createStatement().executeQuery(selectQuery + buttonToReplace + "'");
            buttonToReplaceInfo.next();
            conn.createStatement().executeUpdate("update buttons set buttonno=" + buttonToMoveInfo.getInt("buttonno") + " where description='" + buttonToReplace + "'");
            conn.createStatement().executeUpdate("update buttons set buttonno=" + buttonToReplaceInfo.getInt("buttonno") + " where description='" + buttonToMove + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public boolean updateServer(ServerModel serverModel) {
        boolean duplicate = false;
        try {
            conn.createStatement().executeUpdate("update servers set logicalname='" + serverModel.getDescription() + "',aetitle='" + serverModel.getAeTitle() + "',hostname='" + serverModel.getHostName() + "',port=" + serverModel.getPort() + ",retrievetype='" + serverModel.getRetrieveType() + "',showpreviews=" + serverModel.isPreviewEnabled() + ",wadocontext='" + serverModel.getWadoURL() + "',wadoport=" + serverModel.getWadoPort() + ",wadoprotocol='" + serverModel.getWadoProtocol() + "',retrievets='" + serverModel.getRetrieveTransferSyntax() + "' where pk=" + serverModel.getPk());
            conn.commit();
        } catch (SQLException ex) {
            duplicate = true;
        }
        return duplicate;
    }

    public void updatePreset(PresetModel presetModel) {
        try {
            conn.createStatement().execute("update presets set presetname='" + presetModel.getPresetName() + "',windowwidth=" + presetModel.getWindowWidth() + ",windowlevel=" + presetModel.getWindowLevel() + " where pk=" + presetModel.getPk());
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateModalitiesStatus(String modality, boolean status) {
        try {
            conn.createStatement().execute("update modality set status=" + status + " where shortname='" + modality + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void setAllModalitiesIdle() {
        try {
            conn.createStatement().execute("update modality set status=false");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateDefaultLocale(String localeName) {
        try {
            conn.createStatement().execute("update locale set status=false");
            conn.createStatement().execute("update locale set status=true where localeid='" + localeName + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateLoopBack(boolean isLoopback) {
        try {
            conn.createStatement().executeUpdate("update miscellaneous set Loopback=" + isLoopback);
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateJNLPRetrieveType(String retrieveType) {
        try {
            conn.createStatement().executeUpdate("update miscellaneous set JNLPRetrieveType='" + retrieveType + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateDynamicRetrieveTypeStatus(boolean allow) {
        try {
            conn.createStatement().executeUpdate("update miscellaneous set AllowDynamicRetrieveType=" + allow);
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void updateStudy(String studyUid) {
        try {
            conn.createStatement().execute("update study set DownloadStatus=true,NoOfInstances=" + getStudyLevelInstances(studyUid) + ",NoOfSeries=" + getStudyLevelSeries(studyUid) + " where StudyInstanceUID='" + studyUid + "'");
            conn.createStatement().execute("update image set ThumbnailStatus=true where StudyInstanceUID='" + studyUid + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    //Deletions
    public void deleteButton(String description) {
        try {
            conn.createStatement().execute("delete from buttons where description='" + description + "'");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void deletePreset(PresetModel presetModel) {
        try {
            conn.createStatement().execute("delete from presets where pk=" + presetModel.getPk());
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void deleteServer(ServerModel serverModel) {
        try {
            conn.createStatement().execute("delete from servers where pk=" + serverModel.getPk());
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void rebuild() {
        deleteRows();
        ApplicationContext.deleteDir(new File(ApplicationContext.listenerDetails[2]));
    }

    private void deleteRows() {
        try {
            Statement statement = conn.createStatement();
            statement.execute("delete from image");
            statement.execute("delete from series");
            statement.execute("delete from study");
            statement.execute("delete from patient");
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void deleteLinkStudies() {
        try {
            ResultSet linkStudies = conn.createStatement().executeQuery("select StudyInstanceUID from study where StudyType='link'");
            while (linkStudies.next()) {
                ResultSet linkSeries = conn.createStatement().executeQuery("select SeriesInstanceUID from series where StudyInstanceUID='" + linkStudies.getString("StudyInstanceUID") + "'");
                while (linkSeries.next()) {
                    ResultSet linkInstances = conn.createStatement().executeQuery("select SopUid from image where StudyInstanceUID='" + linkStudies.getString("StudyInstanceUID") + "' and SeriesInstanceUID='" + linkSeries.getString("SeriesInstanceUID") + "'");
                    while (linkInstances.next()) {
                        deleteRow("image", "SopUid", linkInstances.getString("SopUid"));
                    }
                    deleteRow("series", "SeriesInstanceUID", linkSeries.getString("SeriesInstanceUID"));
                }
                deleteRow("study", "StudyInstanceUID", linkStudies.getString("StudyInstanceUID"));
            }
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }

    public void deleteRow(String tableName, String whereFiled, String whereValue) throws SQLException {
        conn.createStatement().execute("delete from " + tableName + " where " + whereFiled + "='" + whereValue + "'");
    }
    Runnable refresher = new Runnable() {
        @Override
        public void run() {
            ApplicationContext.mainScreenObj.refreshLocalDB();
        }
    };

    public void deleteLocalStudy(String patientID, String studyInstanceUID) {
        try {
            ResultSet fileInfo = conn.createStatement().executeQuery("select FileStoreUrl from image where StudyInstanceUID='" + studyInstanceUID + "'");
            if (fileInfo.next() && fileInfo.getString("FileStoreUrl").contains(ApplicationContext.appDirectory)) {
                ApplicationContext.deleteDir(new File(fileInfo.getString("FileStoreUrl")).getParentFile().getParentFile());
            }
            fileInfo.close();
            conn.createStatement().execute("delete from image where StudyInstanceUID='" + studyInstanceUID + "'");
            conn.createStatement().execute("delete from series where StudyInstanceUID='" + studyInstanceUID + "'");
            conn.createStatement().execute("delete from study where StudyInstanceUID='" + studyInstanceUID + "'");

            if (!checkRecordExists("study", "PatientId", patientID)) {
                conn.createStatement().execute("delete from patient where PatientID='" + patientID + "'");
            }
            conn.commit();
        } catch (SQLException ex) {
            ApplicationContext.logger.log(Level.SEVERE, "DatabaseHandler", ex);
        }
    }
}