/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

import javax.management.Notification;
import javax.management.NotificationListener;

import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAttributeEvent;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 3, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class PrinterService
   extends ServiceMBeanSupport
   implements PrinterServiceMBean, NotificationListener, Runnable,
      PrintServiceAttributeListener, PrintJobAttributeListener, PrintJobListener
{
   
   // Constants -----------------------------------------------------
   static final double PTS_PER_MM = 72/25.4;
   private static final String[] CODE_STRING = {
      null, "NORMAL", "WARNING", "FAILURE"
   };
   private static final String ADF_FILE_EXT = ".adf";
   private static final String LUT_FILE_EXT = ".lut";      
   
   // Attributes ----------------------------------------------------
   /** Holds value of property printerName. */
   private String printerName;
   
   /** Holds value of property printToFilePath. */
   private String printToFilePath;
   
   /** Holds value of property supportsColor. */
   private boolean supportsColor;
   
   /** Holds value of property supportsPresentationLUT. */
   private boolean supportsPresentationLUT;
   
   /** Holds value of property defaultPortrait. */
   private boolean defaultPortrait;
   
   /** Holds value of property displayFormat. */
   private String displayFormat;
   
   /** Holds value of property filmSizeID. */
   private LinkedHashMap filmSizeIDMap = new LinkedHashMap();
   
   /** Holds value of property resolutionID. */
   private String resolutionID;
   
   /** Holds value of property magnificationType. */
   private String magnificationType;
   
   /** Holds value of property smoothingType. */
   private String smoothingType;
   
   /** Holds value of property borderDensity. */
   private String borderDensity;
   
   /** Holds value of property emptyImageDensity. */
   private String emptyImageDensity;
   
   /** Holds value of property minDensity. */
   private int minDensity;
   
   /** Holds value of property maxDensity. */
   private int maxDensity;
   
   /** Holds value of property filmDestination. */
   private String filmDestination;
   
   /** Holds value of property pageMargin. */
   private float[] pageMargin;

   /** Holds value of property reverseLandscape. */
   private boolean reverseLandscape;
   
   /** Holds value of property borderThickness. */
   private float borderThickness;
   
   /** Holds value of property resolution. */
   private String resolution;
   
   /** Holds value of property mediumType. */
   private String mediumType;
   
   /** Holds value of property illumination. */
   private int illumination;
   
   /** Holds value of property reflectedAmbientLight. */
   private int reflectedAmbientLight;
   
   /** Holds value of property decimateCropBehavior. */
   private String decimateCropBehavior;
      
   /** Holds value of property graySteps. */
   private int graySteps;

   /** Holds value of property grayStepGap. */
   private float grayStepGap;
   
   private int status = NORMAL;
   private String statusInfo = "NORMAL";

   private final PrinterCalibration calibration = new PrinterCalibration();
   private final ScannerCalibration scanner = new ScannerCalibration(log);
   
   private long notifCount = 0;
   private LinkedList highPriorQueue = new LinkedList();
   private LinkedList medPriorQueue = new LinkedList();
   private LinkedList lowPriorQueue = new LinkedList();
   private Object queueMonitor = new Object();
   private Thread scheduler;
   private PrintService printService;
   
   /** Holds value of property printToFile. */
   private boolean printToFile;
   
   /** Holds value of property annotationDir. */
   private String annotationDir;
   
   /** Holds value of property lutDir. */
   private String lutDir;
   
   /** Holds value of property supportsAnnotationBox. */
   private boolean supportsAnnotationBox;
   
   /** Holds value of property defaultLUT. */
   private String defaultLUT;
   
   /** Holds value of property defaultAnnotation. */
   private String defaultAnnotation;
   
   /** Holds value of property grayStepAnnotation. */
   private String grayStepAnnotation;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // PrinterMBean implementation -----------------------------------
   
   /** Getter for property status.
    * @return Value of property status.
    */
   public int getStatusID() {
      return status;
   }
   
   /** Getter for property statusInfo.
    * @return Value of property statusInfo.
    */
   public String getStatusInfo() {
      return statusInfo;
   }
   
   /** Getter for string value for property status.
    * @return String value of property status.
    */
   public String getStatus() {
      return CODE_STRING[status];
   }
   
   /** Getter for property printerName.
    * @return Value of property printerName.
    */
   public String getPrinterName() {
      return printerName;
   }
   
   /** Setter for property printerName.
    * @param printerName New value of property printerName.
    */
   public void setPrinterName(String printerName) {
      if (!printerName.equals(this.printerName)) {
         this.printerName = printerName;
         try {
            getPrintService(); // to register Attribute Listener
         } catch (PrintException e) {
            log.warn(e, e);
         }
      }
   }
   
   private PrintService getPrintService() throws PrintException {
      if (printService != null) {
         if (printService.getName().equals(printerName)) {
            return printService;
         }
         printService.removePrintServiceAttributeListener(this);
         printService = null;         
      }
      PrintService[] services =  PrintServiceLookup.lookupPrintServices(
         DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
      for (int i = 0; i < services.length; ++i) {
         if (services[i].getName().equals(printerName)) {
            printService = services[i];
            printService.addPrintServiceAttributeListener(this);
            return printService;
         }
      }
      throw new PrintException("Failed to access Printer " + printerName);
   }
   
   /** Getter for property printToFile.
    * @return Value of property printToFile.
    */
   public boolean isPrintToFile() {
      return this.printToFile;
   }
   
   /** Setter for property printToFile.
    * @param printToFile New value of property printToFile.
    */
   public void setPrintToFile(boolean printToFile) {
      this.printToFile = printToFile;
   }
   
   /** Getter for property printToFilePath.
    * @return Value of property printToFilePath.
    */
   public String getPrintToFilePath() {
      return this.printToFilePath;
   }
   
   /** Setter for property printToFilePath.
    * @param printToFilePath New value of property printToFilePath.
    */
   public void setPrintToFilePath(String printToFilePath) {
      this.printToFilePath = printToFilePath;
   }
   
   /** Getter for property availableDestinations.
    * @return Value of property availableDestinations.
    */
   public String[] getAvailablePrinters() {
      PrintService[] services =  PrintServiceLookup.lookupPrintServices(
         DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
      String[] names = new String[services.length];      
      for (int i = 0; i < services.length; ++i) {
         names[i] = services[i].getName();
      }
      return names;
   }
   
   /** Getter for property printServiceAttributes.
    * @return Value of property printServiceAttributes.
    */
   public String[] getPrintServiceAttributes() {
      try {
         return toStringArray(getPrintService().getAttributes());
      } catch (PrintException e) {
         return new String[] { e.getMessage() };
      }
   }
   
   static String[] toStringArray(AttributeSet as) {
      Attribute[] a = as.toArray();
      String[] result = new String[a.length];
      for (int i = 0; i < a.length; ++i) {
         result[i] =
            org.jboss.util.Classes.stripPackageName(a[i].getCategory()) + "=" + a[i];
      }
      return result;
   }
   
   /** Getter for property supportedAttributeValues.
    * @return Value of property supportedAttributeValues.
    */
   public String[] getSupportedAttributeValues() {
      try {
         PrintService ps = getPrintService();
         Class[] c = ps.getSupportedAttributeCategories();
         String[] result = new String[c.length];
         AttributeSet aset = new HashPrintRequestAttributeSet();
//         aset.add(toOrientationRequested(getFilmOrientation()));
//         aset.add(toMediaSizeName(getDefaultFilmSizeID()));
         for (int i = 0; i < c.length; ++i) {
            Object value = ps.getSupportedAttributeValues(c[i],
               DocFlavor.SERVICE_FORMATTED.PAGEABLE, aset);            
            result[i] = org.jboss.util.Classes.stripPackageName(c[i]) + "="
               + (value instanceof Object[]
                  ? Arrays.asList((Object[]) value) : value);
         }
         return result;
      } catch (PrintException e) {
         return new String[] { e.getMessage() };
      }
   }
   
   /** Getter for property supportsColor.
    * @return Value of property supportsColor.
    */
   public boolean isSupportsColor() {
      return this.supportsColor;
   }
   
   /** Setter for property supportsColor.
    * @param supportsColor New value of property supportsColor.
    */
   public void setSupportsColor(boolean supportsColor) {
      this.supportsColor = supportsColor;
   }
   
   /** Getter for property supportsPresentationLUT.
    * @return Value of property supportsPresentationLUT.
    */
   public boolean isSupportsPresentationLUT() {
      return this.supportsPresentationLUT;
   }
   
   /** Setter for property supportsPresentationLUT.
    * @param supportsPresentationLUT New value of property supportsPresentationLUT.
    */
   public void setSupportsPresentationLUT(boolean supportsPresentationLUT) {
      this.supportsPresentationLUT = supportsPresentationLUT;
   }
   
   /** Getter for property mediaType.
    * @return Value of property mediaType.
    */
   public String getMediumType() {
      return this.mediumType;
   }
   
   /** Setter for property mediaType.
    * @param mediaType New value of property mediaType.
    */
   public void setMediumType(String mediumType) {
      this.mediumType = mediumType;
   }
   
   /** Getter for property defaultMediumType.
    * @return Value of property defaultMediumType.
    */
   public String getDefaultMediumType() {
      return firstOf(mediumType);
   }
   
   public boolean isSupportsMediumType(String mediumType) {
      return contains(this.mediumType, mediumType);
   }
   
   /** Getter for property filmDestination.
    * @return Value of property filmDestination.
    */
   public String getFilmDestination() {
      return this.filmDestination;
   }
   
   /** Setter for property filmDestination.
    * @param filmDestination New value of property filmDestination.
    */
   public void setFilmDestination(String filmDestination) {
      this.filmDestination = filmDestination;
   }
   
   /** Getter for property defaultFilmDestination.
    * @return Value of property defaultFilmDestination.
    */
   public String getDefaultFilmDestination() {
      return firstOf(filmDestination);
   }
   
   public boolean isSupportsFilmDestination(String filmDestination) {
      return contains(this.filmDestination, filmDestination);
   }
   
   /** Getter for property defaultPortrait.
    * @return Value of property defaultPortrait.
    */
   public boolean isDefaultPortrait() {
      return this.defaultPortrait;
   }
   
   /** Setter for property defaultPortrait.
    * @param defaultPortrait New value of property defaultPortrait.
    */
   public void setDefaultPortrait(boolean defaultPortrait) {
      this.defaultPortrait = defaultPortrait;
   }

   /** Getter for property defaultFilmOrientation.
    * @return Value of property defaultFilmOrientation.
    */
   public String getDefaultFilmOrientation() {
      return defaultPortrait ? "PORTRAIT" : "LANDSCAPE";
   }
      
   /** Getter for property displayFormat.
    * @return Value of property displayFormat.
    */
   public String getDisplayFormat() {
      return this.displayFormat;
   }

   /** Setter for property displayFormat.
    * @param displayFormat New value of property displayFormat.
    */
   public void setDisplayFormat(String displayFormat) {
      this.displayFormat = displayFormat;
   }
   
   public boolean isSupportsDisplayFormat(String displayFormat,
      String filmOrientation)
   {
      if (!displayFormat.startsWith("STANDARD\\")) {
         return false;
      }
      if (filmOrientation.equals("PORTRAIT")) {
         return contains(this.displayFormat, displayFormat.substring(9));
      }
      int pos = displayFormat.lastIndexOf(',');
      return contains(this.displayFormat, 
         displayFormat.substring(pos+1) + ',' + displayFormat.substring(9,pos));
   }

   private static String firstOf(String list) {
      if (list == null || list.length() == 0) {
         return null;
      }
      int pos = list.indexOf('\\');
      return pos == -1 ? list : list.substring(0, pos);
   }
   
   private static boolean contains(String list, String value) {
      if (list == null || list.length() == 0) {
         return false;
      }
      StringTokenizer tk = new StringTokenizer(list, "\\");
      while (tk.hasMoreTokens()) {
         if (value.equals(tk.nextToken())) {
            return true;
         }
      }
      return false;
   }
   
   /** Getter for property filmSizeID.
    * @return Value of property filmSizeID.
    */
   public String getFilmSizeID() {
      StringBuffer sb = new StringBuffer();
      for (Iterator it = filmSizeIDMap.entrySet().iterator(); it.hasNext();) {
         Map.Entry item = (Map.Entry) it.next();
         float[] wh = (float[]) item.getValue();
         sb.append(item.getKey());
         sb.append(':');
         sb.append(wh[0]);
         sb.append('x');
         sb.append(wh[1]);
         sb.append('\\');
      }
      sb.setLength(sb.length()-1);
      return sb.toString();
   }
   
   /** Setter for property filmSizeID.
    * @param filmSizeID New value of property filmSizeID.
    */
   public void setFilmSizeID(String filmSizeID) {
      LinkedHashMap tmp = new LinkedHashMap();
      StringTokenizer tk = new StringTokenizer(filmSizeID, "\\");
      while (tk.hasMoreTokens()) {
         String s = tk.nextToken();
         int c1 = s.indexOf(':');
         int xpos = s.indexOf('x', c1+1);
         float[] wh = {
            Float.parseFloat(s.substring(c1+1, xpos)),
            Float.parseFloat(s.substring(xpos+1))
         };
         Arrays.sort(wh);
         if (wh[0] <= 0) {
            throw new IllegalArgumentException(s);
         }
         tmp.put(s.substring(0, c1), wh);
      }
      filmSizeIDMap = tmp;
   }
      
   /** Getter for property defaultFilmSizeID.
    * @return Value of property defaultFilmSizeID.
    */
   public String getDefaultFilmSizeID() {
      if (filmSizeIDMap.isEmpty()) {
         return null;
      }
      return (String) filmSizeIDMap.keySet().iterator().next();
   }
   
   public boolean isSupportsFilmSizeID(String filmSizeID) {
      return filmSizeIDMap.containsKey(filmSizeID);
   }
   
   Paper toPaper(float[] wh) {
      Paper paper = new Paper();
      paper.setSize(wh[0] * PTS_PER_MM, wh[1] * PTS_PER_MM);
      paper.setImageableArea(
         pageMargin[0] * PTS_PER_MM,
         pageMargin[1] * PTS_PER_MM,
         (wh[0] - (pageMargin[0] + pageMargin[2])) * PTS_PER_MM,
         (wh[1] - (pageMargin[1] + pageMargin[3])) * PTS_PER_MM);
      return paper;
   }
   
   Paper getPaper(String filmSizeID) {
      return toPaper((float[]) filmSizeIDMap.get(filmSizeID));
   }

   Paper getDefaultPaper() {
      if (filmSizeIDMap.isEmpty()) {
         return null;
      }
      return toPaper((float[]) filmSizeIDMap.values().iterator().next());
   }
   
   /** Getter for property resolutionID.
    * @return Value of property resolutionID.
    */
   public String getResolutionID() {
      return this.resolutionID;
   }
   
   /** Setter for property resolutionID.
    * @param resolutionID New value of property resolutionID.
    */
   public void setResolutionID(String resolutionID) {
      this.resolutionID = resolutionID;
   }
   
   /** Getter for property defaultResolutionID.
    * @return Value of property defaultResolutionID.
    */
   public String getDefaultResolutionID() {
      return firstOf(resolutionID);
   }
   
   public boolean isSupportsResolutionID(String resolutionID) {
      return contains(this.resolutionID, resolutionID);
   }
      
   /** Getter for property magnificationType.
    * @return Value of property magnificationType.
    */
   public String getMagnificationType() {
      return this.magnificationType;
   }
   
   /** Setter for property magnificationType.
    * @param magnificationType New value of property magnificationType.
    */
   public void setMagnificationType(String magnificationType) {
      this.magnificationType = magnificationType;
   }
   
   /** Getter for property defaultMagnificationType.
    * @return Value of property defaultMagnificationType.
    */
   public String getDefaultMagnificationType() {
      return firstOf(magnificationType);
   }
   
   public boolean isSupportsMagnificationType(String magnificationType) {
      return contains(this.magnificationType, magnificationType);
   }
   
   /** Getter for property smoothingType.
    * @return Value of property smoothingType.
    */
   public String getSmoothingType() {
      return this.smoothingType;
   }
   
   /** Setter for property smoothingType.
    * @param smoothingType New value of property smoothingType.
    */
   public void setSmoothingType(String smoothingType) {
      this.smoothingType = smoothingType;
   }
   
   public boolean isSupportsSmoothingType(String smoothingType) {
      return contains(this.smoothingType, smoothingType);
   }
   
   /** Getter for property defaultSmoothingType.
    * @return Value of property defaultSmoothingType.
    */
   public String getDefaultSmoothingType() {
      return firstOf(smoothingType);
   }
   
   /** Getter for property decimateCropBehavior.
    * @return Value of property decimateCropBehavior.
    */
   public String getDecimateCropBehavior() {
      return this.decimateCropBehavior;
   }
   
   /** Setter for property decimateCropBehavior.
    * @param decimateCropBehavior New value of property decimateCropBehavior.
    */
   public void setDecimateCropBehavior(String decimateCropBehavior) {
      this.decimateCropBehavior = decimateCropBehavior;
   }
   
   /** Getter for property borderDensity.
    * @return Value of property borderDensity.
    */
   public String getBorderDensity() {
      return this.borderDensity;
   }
   
   /** Setter for property borderDensity.
    * @param borderDensity New value of property borderDensity.
    */
   public void setBorderDensity(String borderDensity) {
      this.borderDensity = borderDensity;
   }
   
   /** Getter for property emptyImageDensity.
    * @return Value of property emptyImageDensity.
    */
   public String getEmptyImageDensity() {
      return this.emptyImageDensity;
   }
   
   /** Setter for property emptyImageDensity.
    * @param emptyImageDensity New value of property emptyImageDensity.
    */
   public void setEmptyImageDensity(String emptyImageDensity) {
      this.emptyImageDensity = emptyImageDensity;
   }
   
   /** Getter for property minDensity.
    * @return Value of property minDensity.
    */
   public int getMinDensity() {
      return this.minDensity;
   }
   
   /** Setter for property minDensity.
    * @param minDensity New value of property minDensity.
    */
   public void setMinDensity(int minDensity) {
      this.minDensity = minDensity;
   }
   
   /** Getter for property maxDensity.
    * @return Value of property maxDensity.
    */
   public int getMaxDensity() {
      return this.maxDensity;
   }
   
   /** Setter for property maxDensity.
    * @param maxDensity New value of property maxDensity.
    */
   public void setMaxDensity(int maxDensity) {
      this.maxDensity = maxDensity;
   }
   
   /** Getter for property margin.
    * @return Value of property margin.
    */
   public String getPageMargin() {
      return "" + pageMargin[0] + "," + pageMargin[1] + ","
         + pageMargin[2] + "," + pageMargin[3];
   }
   
   /** Setter for property margin.
    * @param margin New value of property margin.
    */
   public void setPageMargin(String pageMargin) {
      StringTokenizer tk = new StringTokenizer(pageMargin, ", ");
      if (tk.countTokens() != 4) {
         throw new IllegalArgumentException("pageMargin: " + pageMargin);
      }
      float[] tmp = {
         Float.parseFloat(tk.nextToken()),
         Float.parseFloat(tk.nextToken()),
         Float.parseFloat(tk.nextToken()),
         Float.parseFloat(tk.nextToken())
      };
      this.pageMargin = tmp;
   }

   /** Getter for property reverseLandscape.
    * @return Value of property reverseLandscape.
    */
   public boolean isReverseLandscape() {
      return this.reverseLandscape;
   }
   
   /** Setter for property reverseLandscape.
    * @param reverseLandscape New value of property reverseLandscape.
    */
   public void setReverseLandscape(boolean reverseLandscape) {
      this.reverseLandscape = reverseLandscape;
   }
   
   /** Getter for property borderThickness.
    * @return Value of property borderThickness.
    */
   public float getBorderThickness() {
      return this.borderThickness;
   }
   
   /** Setter for property borderThickness.
    * @param borderThickness New value of property borderThickness.
    */
   public void setBorderThickness(float borderThickness) {
      this.borderThickness = borderThickness;
   }
   
   /** Getter for property illumination.
    * @return Value of property illumination.
    */
   public int getIllumination() {
      return this.illumination;
   }
   
   /** Setter for property illumination.
    * @param illumination New value of property illumination.
    */
   public void setIllumination(int illumination) {
      this.illumination = illumination;
   }
   
   /** Getter for property reflectedAmbientLight.
    * @return Value of property reflectedAmbientLight.
    */
   public int getReflectedAmbientLight() {
      return this.reflectedAmbientLight;
   }
   
   /** Setter for property reflectedAmbientLight.
    * @param reflectedAmbientLight New value of property reflectedAmbientLight.
    */
   public void setReflectedAmbientLight(int reflectedAmbientLight) {
      this.reflectedAmbientLight = reflectedAmbientLight;
   }
   
   /** Getter for property annotationDir.
    * @return Value of property annotationDir.
    */
   public String getAnnotationDir() {
      return this.annotationDir;
   }
   
   /** Setter for property annotationDir.
    * @param annotationDir New value of property annotationDir.
    */
   public void setAnnotationDir(String annotationDir) {
      this.annotationDir = toFile(annotationDir).getAbsolutePath();
   }
   
   private static int parseAnnotationBoxCount(String id) {
      return Integer.parseInt(id.substring(id.lastIndexOf('_') + 1));
   }
   
   private static final FilenameFilter ADF_FILENAME_FILTER = 
      new FilenameFilter() {
         public boolean accept(File dir, String name) {
            if (!name.endsWith(ADF_FILE_EXT)) {
               return false;
            }
            String id = name.substring(0, name.length()-ADF_FILE_EXT.length());
            try {
               return parseAnnotationBoxCount(id) > 0;
            } catch (RuntimeException e) {
              return false;
            }
         }
      };
   
   private static void skipFileExt(String[] fnames, String ext) {
      int extlen = ext.length();
      for (int i = 0; i < fnames.length; ++i) {
         fnames[i] = fnames[i].substring(0, fnames[i].length() - extlen);
      }
   }
   
   /** Getter for property annotationDisplayFormatIDs.
    * @return Value of property annotationDisplayFormatIDs.
    */
   public String[] getAnnotationDisplayFormatIDs() {
      File dir = toFile(annotationDir);
      if (!dir.isDirectory()) {
         return new String[]{};
      }
      String[] fnames = dir.list(ADF_FILENAME_FILTER);
      skipFileExt(fnames, ADF_FILE_EXT);
      return  fnames;
   }
   
   public int countAnnotationBoxes(String annotationID) {
      String[] ids = getLUTs();
      if (Arrays.asList(ids).indexOf(annotationID) == -1) {
         return -1;
      }
      return parseAnnotationBoxCount(annotationID);
   }
   
   Annotation getAnnotation(String id) throws IOException {
      File f = new File(annotationDir, id + PrinterService.ADF_FILE_EXT);
      Properties props = new Properties();
      FileInputStream in = new FileInputStream(f);
      try {
         props.load(in);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      return new Annotation(this, props);
   }
   
   /** Getter for property lutDir.
    * @return Value of property lutDir.
    */
   public String getLUTDir() {
      return this.lutDir;
   }
   
   /** Setter for property lutDir.
    * @param lutDir New value of property lutDir.
    */
   public void setLUTDir(String lutDir) {
      this.lutDir = toFile(lutDir).getAbsolutePath();
   }
   
   private static final FilenameFilter LUT_FILENAME_FILTER =
      new FilenameFilter() {
         public boolean accept(File dir, String name) {
            return name.endsWith(LUT_FILE_EXT);
         }
      };
               
   /** Getter for property LUTs.
    * @return Value of property LUTs.
    */
      public String[] getLUTs() {
      File dir = toFile(lutDir);
      if (!dir.isDirectory()) {
         return new String[]{};
      }
      String[] fnames = dir.list(LUT_FILENAME_FILTER);
      skipFileExt(fnames, LUT_FILE_EXT);
      return  fnames;
   }
   
   public boolean isSupportsConfigurationInformation(String configInfo) {
      String[] ids = getLUTs();
      return Arrays.asList(ids).indexOf(configInfo) != -1;
   }
   
   /** Getter for property supportsAnnotationBox.
    * @return Value of property supportsAnnotationBox.
    */
   public boolean isSupportsAnnotationBox() {
      return this.supportsAnnotationBox;
   }
   
   /** Setter for property supportsAnnotationBox.
    * @param supportsAnnotationBox New value of property supportsAnnotationBox.
    */
   public void setSupportsAnnotationBox(boolean supportsAnnotationBox) {
      this.supportsAnnotationBox = supportsAnnotationBox;
   }
   
   /** Getter for property defaultLUT.
    * @return Value of property defaultLUT.
    */
   public String getDefaultLUT() {
      return this.defaultLUT;
   }
   
   /** Setter for property defaultLUT.
    * @param defaultLUT New value of property defaultLUT.
    */
   public void setDefaultLUT(String defaultLUT) {
      this.defaultLUT = defaultLUT;
   }
   
   /** Getter for property defaultAnnotation.
    * @return Value of property defaultAnnotation.
    */
   public String getDefaultAnnotation() {
      return this.defaultAnnotation;
   }
   
   /** Setter for property defaultAnnotation.
    * @param defaultAnnotation New value of property defaultAnnotation.
    */
   public void setDefaultAnnotation(String defaultAnnotation) {
      this.defaultAnnotation = defaultAnnotation;
   }
   
   /** Getter for property grayStepAnnotation.
    * @return Value of property grayStepAnnotation.
    */
   public String getGrayStepAnnotation() {
      return this.grayStepAnnotation;
   }
   
   /** Setter for property grayStepAnnotation.
    * @param grayStepAnnotation New value of property grayStepAnnotation.
    */
   public void setGrayStepAnnotation(String grayStepAnnotation) {
      this.grayStepAnnotation = grayStepAnnotation;
   }
   
   /** Getter for property grayStepODs.
    * @return Value of property grayStepODs.
    */
   public float[] getGrayStepODs() {
      return calibration.getGrayStepODs();
   }
   
   /** Setter for property grayStepODs.
    * @param grayStepODs New value of property grayStepODs.
    */
   public void setGrayStepODs(float[] grayStepODs) {
      calibration.setGrayStepODs(grayStepODs);
   }
   

   /** Setter for property grayStepODsAsText.
    * @param grayStepODsAsText New value of property grayStepODsAsText.
    */
   public void setGrayStepODsAsText(String grayStepODsAsText) {
      calibration.setGrayStepODs(toFloatArray(grayStepODsAsText));
   }
   
   private static float[] toFloatArray(String text) {
      StringTokenizer stk = new StringTokenizer(text, ",; \t\r\n");
      float[] a = new float[stk.countTokens()];
      for (int i = 0; i < a.length; ++i) {
         a[i] = Float.parseFloat(stk.nextToken());
      }
      return a;
   }

   /** Getter for property graySteps.
    * @return Value of property graySteps.
    */
   public int getGraySteps() {
      return graySteps;
   }
   
   /** Setter for property graySteps.
    * @param graySteps New value of property graySteps.
    */
   public void setGraySteps(int graySteps) {
      if (graySteps < 4 || graySteps > 64) {
         throw new IllegalArgumentException("steps: " + graySteps);
      }
      this.graySteps = graySteps;
   }
   
   /** Getter for property grayStepGap.
    * @return Value of property grayStepGap.
    */
   public float getGrayStepGap() {
      return grayStepGap;
   }
   
   /** Setter for property grayStepGap.
    * @param grayStepGap New value of property grayStepGap.
    */
   public void setGrayStepGap(float grayStepGap) {
      this.grayStepGap = grayStepGap;
   }
   
   /** Getter for property refGrayStepODs.
    * @return Value of property refGrayStepODs.
    */
   public float[] getRefGrayStepODs() {
      return scanner.getRefGrayStepODs();
   }
   
   /** Setter for property refGrayStepODs.
    * @param refGrayStepODs New value of property refGrayStepODs.
    */
   public void setRefGrayStepODs(float[] refGrayStepODs) {
      scanner.setRefGrayStepODs(refGrayStepODs);
   }
   
   /** Setter for property refGrayStepODsAsText.
    * @param refGrayStepODsAsText New value of property refGrayStepODsAsText.
    */
   public void setRefGrayStepODsAsText(String refGrayStepODsAsText) {
      setRefGrayStepODs(toFloatArray(refGrayStepODsAsText));
   }
   
   /** Getter for property scanGrayStepDir.
    * @return Value of property scanGrayStepDir.
    */
   public String getCalibrationDir() {
      return scanner.getCalibrationDir().getAbsolutePath();
   }
   
   /** Setter for property scanGrayStepDir.
    * @param scanGrayStepDir New value of property scanGrayStepDir.
    */
   public void setCalibrationDir(String scanGrayStepDir) {
      scanner.setCalibrationDir(toFile(scanGrayStepDir));
   }
   
   /** Getter for property refGrayStepFileName.
    * @return Value of property refGrayStepFileName.
    */
   public String getRefGrayStepFileName() {
      return scanner.getRefGrayStepFileName();
   }
   
   /** Setter for property refGrayStepFileName.
    * @param refGrayStepFileName New value of property refGrayStepFileName.
    */
   public void setRefGrayStepFileName(String refGrayStepFileName) {
      scanner.setRefGrayStepFileName(refGrayStepFileName);
   }
   
   /** Getter for property scanArea.
    * @return Value of property scanArea.
    */
   public String getScanPointExtension() {
      return scanner.getScanPointExtension();
   }
   
   /** Setter for property scanPointExtension.
    * @param extension New value of property scanPointExtension.
    */
   public void setScanPointExtension(String extension) {
      scanner.setScanPointExtension(extension);
   }
   
   /** Getter for property scanThreshold.
    * @return Value of property scanThreshold.
    */
   public String getScanThreshold() {
      return scanner.getScanThreshold();
   }
   
   /** Setter for property scanThreshold.
    * @param scanThreshold New value of property scanThreshold.
    */
   public void setScanThreshold(String scanThreshold) {
      scanner.setScanThreshold(scanThreshold);
   }
   
   public void printGraySteps() throws PrintException, IOException {
      log.info("Printing gray steps");
      print(new GrayStep(this, calibration.getIdentityPValToDDL(), printerName));
      log.info("Printed gray steps");
   }
   
   public void printGrayStepsWithGSDF() throws PrintException, IOException {
      log.info("Printing gray steps [GSDF]");
      print(new GrayStep(this, calibration.getPValToDDLwGSDF(8,
               minDensity/100.f, maxDensity/100.f,
               illumination, reflectedAmbientLight),
            printerName + "[GSDF]"));
      log.info("Printed gray steps [GSDF]");
   }
   
   public void printGrayStepsWithLinOD() throws PrintException, IOException {
      log.info("Printing gray steps [LIN OD]");
      print(new GrayStep(this, calibration.getPValToDDLwLinOD(8,
               minDensity/100.f, maxDensity/100.f),
            printerName + "[LIN OD]"));
      log.info("Printed gray steps [LIN OD]");
   }
   
   public void calibrate(boolean force) throws CalibrationException {
      log.info("Calibrating " + printerName);
      setGrayStepODs(scanner.calculateGrayStepODs(printerName, force));
      log.info("Calibrated " + printerName);
   }
   
   // ServiceMBeanSupport overrides ------------------------------------
   public void startService()
   throws Exception {
      scheduler = new Thread(this);
      scheduler.start();
   }
   
   public void stopService()
   throws Exception {
      Thread tmp = scheduler;
      scheduler = null;
      tmp.interrupt();
   }
   
   // NotificationListener implementation -----------------------------------
   public void handleNotification(Notification notif, Object obj) {
      log.info("Scheduling job - " + new File(notif.getMessage()).getName());
      Dataset sessionAttr = (Dataset)notif.getUserData();
      String prior = sessionAttr.getString(Tags.PrintPriority);
      synchronized (queueMonitor) {
         if ("LOW".equals(prior)) {
            lowPriorQueue.add(notif);
         } else if ("HIGH".equals(prior)) {
            highPriorQueue.add(notif);
         } else {
            medPriorQueue.add(notif);
         }
         queueMonitor.notify();
      }
   }
   
   // Runnable implementation -----------------------------------
   public void run() {
      log.info("Scheduler Started");
      while (scheduler != null) {
         try {
            Notification notif;
            synchronized (queueMonitor) {
               while ((notif = nextNotification()) == null) {
                  queueMonitor.wait();
               }
            }
            processNotification(notif);
         } catch (InterruptedException ignore) {
         }
      }
      log.info("Scheduler Stopped");
   }
   
   private Notification nextNotification() {
      if (!highPriorQueue.isEmpty()) {
         return (Notification) highPriorQueue.removeFirst();
      }
      if (!medPriorQueue.isEmpty()) {
         return (Notification) medPriorQueue.removeFirst();
      }
      if (!lowPriorQueue.isEmpty()) {
         return (Notification) lowPriorQueue.removeFirst();
      }
      return null;
   }
   
   private void processNotification(Notification notif) {
      String job = notif.getMessage();
      File jobDir = new File(job);
      String jobID = new File(job).getName();
      log.info("Start processing job - " + jobID);
      sendNotification(
         new Notification(NOTIF_PRINTING, this, ++notifCount, job));
      Dataset sessionAttr = (Dataset)notif.getUserData();
      try {
         doPrint(jobDir, (Dataset)notif.getUserData());
         log.info("Finished processing job - " + jobID);
         sendNotification(
            new Notification(NOTIF_DONE, this, ++notifCount, job));
      } catch (Exception e) {
         log.error("Failed processing job - " + jobID, e);
         sendNotification(
            new Notification(NOTIF_FAILURE, this, ++notifCount, job));
      }
   }
   
   // PrintServiceAttributeListener implementation -------------------------
   static String toMsg(String prompt, AttributeSet set) {
      return prompt + Arrays.asList(toStringArray(set));
   }

   static String toMsg(String prompt, PrintJobEvent pje) {
      return toMsg(prompt, pje.getPrintJob().getAttributes());
   }
   
   public void attributeUpdate(PrintServiceAttributeEvent psae) {
      if (log.isDebugEnabled())
         log.debug(toMsg("printServiceAttributeUpdate: ", psae.getAttributes()));
   }
   
   // PrintJobAttributeListener implementation -------------------------   
   public void attributeUpdate(PrintJobAttributeEvent pjae) {
      if (log.isDebugEnabled())
      log.debug(toMsg("printJobAttributeUpdate: ", pjae.getAttributes()));
   }
   
   // PrintJobListener implementation -------------------------   
   public void printDataTransferCompleted(PrintJobEvent pje) {
      if (log.isDebugEnabled())
         log.debug(toMsg("printDataTransferCompleted: ", pje));
   }
   
   public void printJobCanceled(PrintJobEvent pje) {
      if (log.isDebugEnabled())
         log.debug(toMsg("printJobCanceled: ", pje));
   }
   
   public void printJobCompleted(PrintJobEvent pje) {
      if (log.isDebugEnabled())
         log.debug(toMsg("printJobCompleted: ", pje));
   }
   
   public void printJobFailed(PrintJobEvent pje) {
      if (log.isDebugEnabled())
         log.info(toMsg("printJobFailed: ", pje));
   }
   
   public void printJobNoMoreEvents(PrintJobEvent pje) {
      if (log.isDebugEnabled())
         log.debug(toMsg("printJobNoMoreEvents: ", pje));
   }
   
   public void printJobRequiresAttention(PrintJobEvent pje) {
      if (log.isDebugEnabled())
         log.debug(toMsg("printJobRequiresAttention: ", pje));
   }
      
   private void doPrint(File jobDir, Dataset sessionAttr)
      throws Exception
   {
      if (!jobDir.exists()) {
         throw new RuntimeException("Missing job dir - " + jobDir);
      }
      File rootDir = jobDir.getParentFile().getParentFile();
      File hcDir = new File(rootDir, "HC");
      if (!hcDir.exists()) {
         throw new RuntimeException("Missing hardcopy dir - " + hcDir);
      }

      File[] spFiles = jobDir.listFiles();
      Arrays.sort(spFiles,
         new Comparator() {
            public int compare(Object o1, Object o2) {
               return (int)(((File)o1).lastModified()
                          - ((File)o2).lastModified());
            }
         });
      // simulate Print Process
      try {
         Thread.sleep(10000);
      } catch (InterruptedException ignore) {}
   }
   
   // Package protected ---------------------------------------------
   static File toFile(String path) {
      if (path == null || path.trim().length() == 0) {
         return null;
      }
      File f = new File(path);
      if (f.isAbsolute()) {
         return f;
      }
      File systemHomeDir = ServerConfigLocator.locate().getServerHomeDir();
      return new File(systemHomeDir, path);
   }
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   private void print(Pageable printData)
      throws PrintException
   {
      PrintService ps = getPrintService();
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      if (printToFile) {
         aset.add(new Destination(toFile(printToFilePath).toURI()));
      }
      DocPrintJob pj = ps.createPrintJob();         
      Doc doc = new SimpleDoc(printData, 
         DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);

      pj.addPrintJobAttributeListener(this, null);
      pj.addPrintJobListener(this);
      try {
         pj.print(doc, aset);
      } finally {
         pj.removePrintJobAttributeListener(this);
         pj.removePrintJobListener(this);
      }
   }
               
   // Inner classes -------------------------------------------------
}
