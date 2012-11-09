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

import in.raster.mayam.facade.Platform;
import in.raster.mayam.util.database.DatabaseHandler;
import in.raster.mayam.form.SendingProgress;
import in.raster.mayam.form.AnnotationPanel;
import in.raster.mayam.form.ImagePanel;
import in.raster.mayam.form.ImageView;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.form.SeriesPanel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ApplicationContext {

    //Database Interaction Referrence object
    public static DatabaseHandler databaseRef = DatabaseHandler.getInstance();
    //Application specific log file reference
    public static File logFile;
    //Context reference for ImageView
    public static ImageView imgView;
    //Context reference for ImagePanel
    public static ImagePanel imgPanel;
    //Context reference for annotation overlay
    public static AnnotationPanel annotationPanel;
    //Context reference for Tile
    public static LayeredCanvas layeredCanvas;
    public static DcmURL moveScuUrl;
    //MainScreen singleton object
    public static MainScreen mainScreen;
    //Context reference for SeriesPanel
    public static SeriesPanel selectedSeriesPanel = null;
    //Context reference for SendingProgress
    public static SendingProgress sendingProgress;

    public static String applicationName="Mayam";

    public static Locale currentLocale=null;
    
    //For Upgrade
    public static boolean isUpgrade = false;

    private ApplicationContext() {
    }

    /**
     * This routine used to create a new image view
     */
    public static void createImageView() {
        imgView = new ImageView();
        imgView.setVisible(true);
    }

    /**
     * This routine used to init the driver and open database if it is already created,
     * otherwise it will create a new database
     */
    public static void openOrCreateDB() {
        databaseRef.openOrCreateDB();
    }

    /**
     * This routine used to create log file if does not exist
     */
    public static void createLogFile() {
        logFile = new File(getAppDirectory() + File.separator, "log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException ex) {
                logFile = new File(System.getProperty("java.io.tmpdir") + File.separator, "log.txt");
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException x) {
                        Logger.getLogger(ApplicationContext.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * This routine used to check whether the image view exist or not
     * @return
     */
    public static boolean imageViewExist() {
        if (imgView != null) {
            return true;
        } else {
            return false;
        }
    }

    public static String getAppDirectory()
    {
        return Platform.getAppDirectory(applicationName).getAbsolutePath();
    }
    /**
     * This routine used to write a log message
     * @param logMsg
     */
    public static void writeLog(String logMsg) {
        DateFormat df = new SimpleDateFormat("kk:mm:ss");
        Date d = new Date();
        FileOutputStream fileOutputStream = null;
        try {
            String logMessage = df.format(d) + ", " + logMsg;
            fileOutputStream = new FileOutputStream(logFile, true);
            fileOutputStream.write(logMessage.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static void setAppLocale()
    {
        //openOrCreateDB();
        String appLocale[]=databaseRef.getActiveLanguageAndCountry();     
        currentLocale=new Locale(appLocale[0],appLocale[1]);       
    }
}
