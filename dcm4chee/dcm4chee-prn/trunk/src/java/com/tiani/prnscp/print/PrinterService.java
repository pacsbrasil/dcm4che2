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
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.util.DAFormat;
import org.dcm4che.util.TMFormat;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import org.jboss.logging.Logger;

import java.awt.Color;
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
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.attribute.standard.SheetCollate;
import javax.print.event.PrintJobAttributeEvent;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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
   implements PrinterServiceMBean, Runnable,
      PrintServiceAttributeListener, PrintJobAttributeListener, PrintJobListener {
         
   static {
      javax.imageio.spi.IIORegistry.getDefaultInstance().registerServiceProvider(
      new org.dcm4cheri.imageio.plugins.DcmImageReaderSpi());
   }
   
   // Constants -----------------------------------------------------
   static final String WHITE = "WHITE";
   static final String BLACK = "BLACK";
   static final String NONE = "NONE";
   static final String REPLICATE = "REPLICATE";
   static final String BILINEAR = "BILINEAR";
   static final String CUBIC = "CUBIC";
   static final String PAPER = "PAPER";
   static final String CROP = "CROP";
   static final String DECIMATE = "DECIMATE";
  
   static final double PTS_PER_MM = 72/25.4;
   private static final String[] CODE_STRING = {
      null, "NORMAL", "WARNING", "FAILURE"
   };
   static final String ADF_FILE_EXT = ".adf";
   static final String LUT_FILE_EXT = ".lut";
   private static final String[] LITTLE_ENDIAN_TS = {
      UIDs.ExplicitVRLittleEndian,
      UIDs.ImplicitVRLittleEndian
   };
   private static final String[] ONLY_DEFAULT_TS = {
      UIDs.ImplicitVRLittleEndian
   };
   // Attributes ----------------------------------------------------
   private String[] ts_uids = LITTLE_ENDIAN_TS;
   private final static AssociationFactory asf =
   AssociationFactory.getInstance();
   
   private String aet;
   
   /** Holds value of property printSCP. */
   private ObjectName printSCP;
   
   /** Holds value of property printerName. */
   private String printerName;
   
   /** Holds value of property printToFilePath. */
   private String printToFilePath;
   
   /** Holds value of property supportsColor. */
   private boolean supportsColor;
   
   /** Holds value of property supportsGrayscale. */
   private boolean supportsGrayscale = true;
   
   /** Holds value of property supportsPresentationLUT. */
   private boolean supportsPresentationLUT;
   
   /** Holds value of property defaultPortrait. */
   private boolean defaultPortrait;
   
   /** Holds value of property displayFormat. */
   private String displayFormat;
   
   /** Holds value of property filmSizeID. */
   private LinkedHashMap filmSizeIDMap = new LinkedHashMap();
   
   /** Holds value of property resolutionID. */
   private LinkedHashMap resolutionIDMap = new LinkedHashMap();
   
   /** Holds value of property magnificationType. */
   private String magnificationType = BILINEAR;
   
   /** Holds value of property smoothingType. */
   private String smoothingType;
   
   /** Holds value of property borderDensity. */
   private String borderDensity = WHITE;
   
   /** Holds value of property emptyImageDensity. */
   private String emptyImageDensity = WHITE;
   
   /** Holds value of property trimBoxDensity. */
   private String trimBoxDensity = BLACK;
   
   /** Holds value of property printGrayAsColor. */
   private boolean printGrayAsColor;
   
   /** Holds value of property maxQueuedJobCount. */
   private int maxQueuedJobCount = 10;
   
   /** Holds value of property minDensity. */
   private int minDensity = 0;
   
   /** Holds value of property maxDensity. */
   private int maxDensity = 200;
   
   /** Holds value of property filmDestination. */
   private String filmDestination;
   
   /** Holds value of property pageMargin. */
   private float[] pageMargin;
   
   /** Holds value of property reverseLandscape. */
   private boolean reverseLandscape = true;
   
   /** Holds value of property borderThickness. */
   private float borderThickness = 1;
   
   /** Holds value of property resolution. */
   private String resolution;
   
   /** Holds value of property mediumType. */
   private String mediumType = PAPER;
   
   /** Holds value of property illumination. */
   private int illumination = 150;
   
   /** Holds value of property reflectedAmbientLight. */
   private int reflectedAmbientLight = 0;
   
   /** Holds value of property decimateCropBehavior. */
   private String decimateCropBehavior = DECIMATE;
   
   /** Holds value of property grayscales. */
   private int grayscales = 32;
   
   /** Holds value of property grayscaleGap. */
   private float grayscaleGap = 1;
   
   /** Holds value of property autoCalibration. */
   private boolean autoCalibration = false;
   
   /** Holds value of property printGrayscaleAtStartup. */
   private boolean printGrayscaleAtStartup = false;
   
   /** Holds value of property printToFile. */
   private boolean printToFile = false;
   
   /** Holds value of property annotationDir. */
   private String annotationDir;
   
   /** Holds value of property lutDir. */
   private String lutDir;
   
   /** Holds value of property supportsAnnotationBox. */
   private boolean supportsAnnotationBox = false;
   
   /** Holds value of property lutForCallingAET. */
   private LinkedHashMap lutForCallingAETMap;
   
   /** Holds value of property defaultAnnotation. */
   private String defaultAnnotation;
   
   /** Holds value of property grayscaleAnnotation. */
   private String grayscaleAnnotation;
   
   /** Holds value of property chunkSize. */
   private double chunkSize = 2.;
      
   /** Holds value of property minimizeJobsize. */
   private boolean minimizeJobsize;
   
   /** Holds value of property decimateByNearestNeighbor. */
   private boolean decimateByNearestNeighbor;
   
   
   private int status = NORMAL;
   private String statusInfo = "NORMAL";
   
   private final PrinterCalibration calibration = new PrinterCalibration();
   private final ScannerCalibration scanner = new ScannerCalibration(log);
   
   private long notifCount = 0;
   private LinkedList highPriorQueue = new LinkedList();
   private LinkedList medPriorQueue = new LinkedList();
   private LinkedList lowPriorQueue = new LinkedList();
   private Object queueMonitor = new Object();
   private Object printerMonitor = new Object();
   private Thread scheduler;
   private PrintService printService;
   
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
   
   /** Getter for property printSCP.
    * @return Value of property printSCP.
    */
   public ObjectName getPrintSCP() {
      return this.printSCP;
   }
   
   /** Setter for property printSCP.
    * @param printSCP New value of property printSCP.
    */
   public void setPrintSCP(ObjectName printSCP) {
      this.printSCP = printSCP;
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
   
   /** Getter for property minimizeJobsize.
    * @return Value of property minimizeJobsize.
    */
   public boolean isMinimizeJobsize() {
      return this.minimizeJobsize;
   }
   
   /** Setter for property minimizeJobsize.
    * @param minimizeJobsize New value of property minimizeJobsize.
    */
   public void setMinimizeJobsize(boolean minimizeJobsize) {
      this.minimizeJobsize = minimizeJobsize;
   }
   
   /** Getter for property decimateByNearestNeighbor.
    * @return Value of property decimateByNearestNeighbor.
    */
   public boolean isDecimateByNearestNeighbor() {
      return this.decimateByNearestNeighbor;
   }
   
   /** Setter for property decimateByNearestNeighbor.
    * @param decimateByNearestNeighbor New value of property decimateByNearestNeighbor.
    */
   public void setDecimateByNearestNeighbor(boolean decimateByNearestNeighbor) {
      this.decimateByNearestNeighbor = decimateByNearestNeighbor;
   }
   
   /** Getter for property chunkSize.
    * @return Value of property chunkSize.
    */
   public double getChunkSize() {
      return this.chunkSize;
   }
   
   /** Setter for property chunkSize.
    * @param chunkSize New value of property chunkSize.
    */
   public void setChunkSize(double chunkSize) {
      this.chunkSize = chunkSize;
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
         for (int i = 0; i < c.length; ++i) {
            Object value = ps.getSupportedAttributeValues(c[i],
            DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
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
   
   /** Getter for property supportsGrayscale.
    * @return Value of property supportsGrayscale.
    */
   public boolean isSupportsGrayscale() {
      return this.supportsGrayscale;
   }
   
   /** Setter for property supportsGrayscale.
    * @param supportsGrayscale New value of property supportsGrayscale.
    */
   public void setSupportsGrayscale(boolean supportsGrayscale) {
      this.supportsGrayscale = supportsGrayscale;
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
   String filmOrientation) {
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
      String[] strings = toStringArray(filmSizeID);
      for (int i = 0; i < strings.length; ++i) {
         String s = strings[i];
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
   
   public boolean isSupportsAnnotationDisplayFormatID(String annotationID) {
      return supportsAnnotationBox && countAnnotationBoxes(annotationID) != -1;
   }
   
   private Paper toPaper(float[] wh) {
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
      StringBuffer sb = new StringBuffer();
      for (Iterator it = resolutionIDMap.entrySet().iterator(); it.hasNext();) {
         Map.Entry item = (Map.Entry) it.next();
         PrinterResolution pr = (PrinterResolution) item.getValue();
         sb.append(item.getKey());
         sb.append(':');
         sb.append(pr.getFeedResolution(PrinterResolution.DPI));
         sb.append('x');
         sb.append(pr.getCrossFeedResolution(PrinterResolution.DPI));
         sb.append('\\');
      }
      sb.setLength(sb.length()-1);
      return sb.toString();
   }
   
   /** Setter for property resolutionID.
    * @param resolutionID New value of property resolutionID.
    */
   public void setResolutionID(String resolutionID) {
      LinkedHashMap tmp = new LinkedHashMap();
      String[] strings = toStringArray(resolutionID);
      for (int i = 0; i < strings.length; ++i) {
         String s = strings[i];
         int c1 = s.indexOf(':');
         int xpos = s.indexOf('x', c1+1);
         PrinterResolution pr = new PrinterResolution(
         Integer.parseInt(s.substring(c1+1, xpos)),
         Integer.parseInt(s.substring(xpos+1)),
         PrinterResolution.DPI);
         tmp.put(s.substring(0, c1), pr);
      }
      resolutionIDMap = tmp;
   }
   
   
   /** Getter for property defaultResolutionID.
    * @return Value of property defaultResolutionID.
    */
   public String getDefaultResolutionID() {
      if (resolutionIDMap.isEmpty()) {
         return null;
      }
      return (String) resolutionIDMap.keySet().iterator().next();
   }
   
   public PrinterResolution getDefaultPrinterResolution() {
      if (resolutionIDMap.isEmpty()) {
         return null;
      }
      return (PrinterResolution) resolutionIDMap.values().iterator().next();
   }
   
   public boolean isSupportsResolutionID(String resolutionID) {
      return resolutionIDMap.containsKey(resolutionID);
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
   
   /** Getter for property trimBoxDensity.
    * @return Value of property trimBoxDensity.
    */
   public String getTrimBoxDensity() {
      return this.trimBoxDensity;
   }
   
   /** Setter for property trimBoxDensity.
    * @param trimBoxDensity New value of property trimBoxDensity.
    */
   public void setTrimBoxDensity(String trimBoxDensity) {
      this.trimBoxDensity = trimBoxDensity;
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
      return ""
      + pageMargin[0] + ','
      + pageMargin[1] + ','
      + pageMargin[2] + ','
      + pageMargin[3];
   }
   
   /** Setter for property margin.
    * @param margin New value of property margin.
    */
   public void setPageMargin(String pageMargin) {
      float[] tmp = toFloatArray(pageMargin);
      if (tmp.length != 4) {
         throw new IllegalArgumentException("pageMargin: " + pageMargin);
      }
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

   // used by testdriver
   void setAnnotationDir(File annotationDir) {
      this.annotationDir = annotationDir.getAbsolutePath();
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
   
   /** Getter for property lutForCallingAET.
    * @return Value of property lutForCallingAET.
    */
   public String getLUTForCallingAET() {
      StringBuffer sb = new StringBuffer();
      for (Iterator it = lutForCallingAETMap.entrySet().iterator(); it.hasNext();) {
         Map.Entry item = (Map.Entry) it.next();
         sb.append(item.getKey());
         sb.append(':');
         sb.append(item.getValue());
         sb.append('\\');
      }
      sb.setLength(sb.length()-1);
      return sb.toString();
   }

   String getLUTForCallingAET(String aet) {
      if (lutForCallingAETMap.isEmpty()) {
         log.error("Configuration Error: missing attribute LUTForCallingAET value!");
         return null;
      }
      String lut = (String) lutForCallingAETMap.get(aet);
      return  lut != null
         ? lut
         : (String) lutForCallingAETMap.values().iterator().next();
   }
   
   /** Setter for property lutForCallingAET.
    * @param lutForCallingAET New value of property lutForCallingAET.
    */
   public void setLUTForCallingAET(String lutForCallingAET) {
      LinkedHashMap tmp = new LinkedHashMap();
      String[] strings = toStringArray(lutForCallingAET);
      for (int i = 0; i < strings.length; ++i) {
         String s = strings[i];
         int c1 = s.indexOf(':');
         if (c1 == -1) {
            throw new IllegalArgumentException(s);
         }
         tmp.put(s.substring(0, c1), s.substring(c1 + 1));
      }
      lutForCallingAETMap = tmp;
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
   
   /** Getter for property grayscaleAnnotation.
    * @return Value of property grayscaleAnnotation.
    */
   public String getGrayscaleAnnotation() {
      return this.grayscaleAnnotation;
   }
   
   /** Setter for property grayscaleAnnotation.
    * @param grayscaleAnnotation New value of property grayscaleAnnotation.
    */
   public void setGrayscaleAnnotation(String grayscaleAnnotation) {
      this.grayscaleAnnotation = grayscaleAnnotation;
   }
   
   /** Getter for property grayscaleODs.
    * @return Value of property grayscaleODs.
    */
   public float[] getGrayscaleODs() {
      return calibration.getGrayscaleODs();
   }
   
   /** Setter for property grayscaleODs.
    * @param grayscaleODs New value of property grayscaleODs.
    */
   public void setGrayscaleODs(float[] grayscaleODs) {
      calibration.setGrayscaleODs(grayscaleODs);
   }
   
   
   /** Setter for property grayscaleODsAsText.
    * @param grayscaleODsAsText New value of property grayscaleODsAsText.
    */
   public void setGrayscaleODsAsText(String grayscaleODsAsText) {
      calibration.setGrayscaleODs(toFloatArray(grayscaleODsAsText));
   }
   
   /** Getter for property dateOfLastCalibration.
    * @return Value of property dateOfLastCalibration.
    */
   public String getDateOfLastCalibration() {
      return calibration.getDateOfLastCalibration();
   }
   
   /** Setter for property dateOfLastCalibration.
    * @param dateOfLastCalibration New value of property dateOfLastCalibration.
    */
   public void setDateOfLastCalibration(String dateOfLastCalibration) {
      try {
         new DAFormat().parse(dateOfLastCalibration);
      } catch (ParseException e) {
         throw new IllegalArgumentException();
      }
      calibration.setDateOfLastCalibration(dateOfLastCalibration);
   }
   
   /** Getter for property timeOfLastCalibration.
    * @return Value of property timeOfLastCalibration.
    */
   public String getTimeOfLastCalibration() {
      return calibration.getTimeOfLastCalibration();
   }
   
   /** Setter for property timeOfLastCalibration.
    * @param timeOfLastCalibration New value of property timeOfLastCalibration.
    */
   public void setTimeOfLastCalibration(String timeOfLastCalibration) {
      try {
         new TMFormat().parse(timeOfLastCalibration);
      } catch (ParseException e) {
         throw new IllegalArgumentException();
      }
      calibration.setTimeOfLastCalibration(timeOfLastCalibration);
   }
   
   private static String[] toStringArray(String text) {
      StringTokenizer stk = new StringTokenizer(text, ",; \t\r\n\\");
      String[] a = new String[stk.countTokens()];
      for (int i = 0; i < a.length; ++i) {
         a[i] = stk.nextToken();
      }
      return a;
   }
   
   private static float[] toFloatArray(String text) {
      StringTokenizer stk = new StringTokenizer(text, ",; \t\r\n\\");
      float[] a = new float[stk.countTokens()];
      for (int i = 0; i < a.length; ++i) {
         a[i] = Float.parseFloat(stk.nextToken());
      }
      return a;
   }
   
   private static int[] toIntArray(String text) {
      StringTokenizer stk = new StringTokenizer(text, ",; \t\r\n\\");
      int[] a = new int[stk.countTokens()];
      for (int i = 0; i < a.length; ++i) {
         a[i] = Integer.parseInt(stk.nextToken());
      }
      return a;
   }
   
   
   /** Getter for property grayscales.
    * @return Value of property grayscales.
    */
   public int getGrayscales() {
      return grayscales;
   }
   
   /** Setter for property grayscales.
    * @param grayscales New value of property grayscales.
    */
   public void setGrayscales(int grayscales) {
      if (grayscales < 4 || grayscales > 64) {
         throw new IllegalArgumentException("grayscales: " + grayscales);
      }
      this.grayscales = grayscales;
   }
   
   /** Getter for property grayscaleGap.
    * @return Value of property grayscaleGap.
    */
   public float getGrayscaleGap() {
      return grayscaleGap;
   }
   
   /** Setter for property grayscaleGap.
    * @param grayscaleGap New value of property grayscaleGap.
    */
   public void setGrayscaleGap(float grayscaleGap) {
      this.grayscaleGap = grayscaleGap;
   }
   
   /** Getter for property refGrayscaleODs.
    * @return Value of property refGrayscaleODs.
    */
   public float[] getRefGrayscaleODs() {
      return scanner.getRefGrayscaleODs();
   }
   
   /** Setter for property refGrayscaleODs.
    * @param refGrayscaleODs New value of property refGrayscaleODs.
    */
   public void setRefGrayscaleODs(float[] refGrayscaleODs) {
      scanner.setRefGrayscaleODs(refGrayscaleODs);
   }
   
   /** Setter for property refGrayscaleODsAsText.
    * @param refGrayscaleODsAsText New value of property refGrayscaleODsAsText.
    */
   public void setRefGrayscaleODsAsText(String refGrayscaleODsAsText) {
      setRefGrayscaleODs(toFloatArray(refGrayscaleODsAsText));
   }
   
   /** Getter for property scanGrayscaleDir.
    * @return Value of property scanGrayscaleDir.
    */
   public String getCalibrationDir() {
      return scanner.getCalibrationDir().getAbsolutePath();
   }
   
   /** Setter for property scanGrayscaleDir.
    * @param scanGrayscaleDir New value of property scanGrayscaleDir.
    */
   public void setCalibrationDir(String scanGrayscaleDir) {
      scanner.setCalibrationDir(toFile(scanGrayscaleDir));
   }
   
   /** Getter for property refGrayscaleFileName.
    * @return Value of property refGrayscaleFileName.
    */
   public String getRefGrayscaleFileName() {
      return scanner.getRefGrayscaleFileName();
   }
   
   /** Setter for property refGrayscaleFileName.
    * @param refGrayscaleFileName New value of property refGrayscaleFileName.
    */
   public void setRefGrayscaleFileName(String refGrayscaleFileName) {
      scanner.setRefGrayscaleFileName(refGrayscaleFileName);
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
   
   
   /** Getter for property autoCalibration.
    * @return Value of property autoCalibration.
    */
   public boolean isAutoCalibration() {
      return this.autoCalibration;
   }
   
   /** Setter for property autoCalibration.
    * @param autoCalibration New value of property autoCalibration.
    */
   public void setAutoCalibration(boolean autoCalibration) {
      this.autoCalibration = autoCalibration;
   }
   
   /** Getter for property printGrayscaleAtStartup.
    * @return Value of property printGrayscaleAtStartup.
    */
   public boolean isPrintGrayscaleAtStartup() {
      return this.printGrayscaleAtStartup;
   }
   
   /** Setter for property printGrayscaleAtStartup.
    * @param printGrayscaleAtStartup New value of property printGrayscaleAtStartup.
    */
   public void setPrintGrayscaleAtStartup(boolean printGrayscaleAtStartup) {
      this.printGrayscaleAtStartup = printGrayscaleAtStartup;
   }
   
   protected PrinterCalibration getPrinterCalibration(){return calibration;}
   
   public byte[] getPValToDDL(int n, float dmin, float dmax,
         float l0, float la, Dataset plut)
   {
      return calibration.getPValToDDL(n, dmin, dmax, l0, la, plut);
   }
   
   public void printGrayscaleWithLinDDL() throws PrintException, IOException {
      log.info("Printing grayscale [LIN DDL]");
      print(new Grayscale(this, calibration.getIdentityPValToDDL(),
         printerName + "[LIN DDL]"), null, false);
      log.info("Printed grayscale [LIN DDL]");
   }
   
   public void printGrayscaleWithGSDF() throws PrintException, IOException {
      log.info("Printing grayscale [GSDF]");
      print(new Grayscale(this, calibration.getPValToDDLwGSDF(8,
         minDensity/100.f, maxDensity/100.f,
         illumination, reflectedAmbientLight),
         printerName + "[GSDF]"), null, false);
      log.info("Printed grayscale [GSDF]");
   }
   
   public void printGrayscaleWithLinOD() throws PrintException, IOException {
      log.info("Printing grayscale [LIN OD]");
      print(new Grayscale(this, calibration.getPValToDDLwLinOD(8,
         minDensity/100.f, maxDensity/100.f),
         printerName + "[LIN OD]"), null, false);
      log.info("Printed grayscale [LIN OD]");
   }
   
   public void calibrate(boolean force) throws CalibrationException {
      log.info("Calibrating " + printerName);
      setGrayscaleODs(scanner.calculateGrayscaleODs(printerName, force));
      setDateOfLastCalibration(scanner.getDateOfLastCalibration());
      setTimeOfLastCalibration(scanner.getTimeOfLastCalibration());
      log.info("Calibrated " + printerName);
   }
   
   // ServiceMBeanSupport overrides ------------------------------------
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws MalformedObjectNameException {
      aet = name.getKeyProperty("aet");
      if (!new ObjectName(OBJECT_NAME_PREFIX + aet).equals(name)) {
         throw new MalformedObjectNameException("name: " + name);
      }
      return name;
   }
   
   public void startService()
   throws Exception {
      scheduler = new Thread(this);
      scheduler.start();
      if (scanner.getScanDir(printerName).mkdirs()) {
         log.warn("Created new calibration sub-directory "
         + scanner.getScanDir(printerName));
      }
      if (printGrayscaleAtStartup) {
         printGrayscaleWithLinDDL();
      }
      putAcceptorPolicy(getAcceptorPolicy());
   }
   
   private AcceptorPolicy getAcceptorPolicy() {
      AcceptorPolicy policy = asf.newAcceptorPolicy();
      if (supportsGrayscale) {
         policy.putPresContext(UIDs.BasicGrayscalePrintManagement, ts_uids);
         if (supportsPresentationLUT) {
            policy.putPresContext(UIDs.PresentationLUT, ts_uids);
         }
      }
      if (supportsColor) {
         policy.putPresContext(UIDs.BasicColorPrintManagement, ts_uids);
      }
      if (supportsAnnotationBox) {
         policy.putPresContext(UIDs.BasicAnnotationBox, ts_uids);
      }
      return policy;
   }
   
   public void stopService()
   throws Exception {
      putAcceptorPolicy(null);
      Thread tmp = scheduler;
      scheduler = null;
      tmp.interrupt();
   }
   
   private void invokeOnPrintSCP(String methode, String arg)
   throws Exception {
      server.invoke(printSCP, methode,
      new Object[] { arg },
      new String[] { String.class.getName() });
   }
   
   private void putAcceptorPolicy(AcceptorPolicy policy)
   throws Exception {
      server.invoke(printSCP, "putAcceptorPolicy",
      new Object[] {
         aet,
         getAcceptorPolicy()
      },
      new String[] {
         String.class.getName(),
         AcceptorPolicy.class.getName()
      });
   }
   
   public void scheduleJob(Boolean color, String job, Dataset sessionAttr)
   {
      PageableJob pageableJob =
         new PageableJob(job, sessionAttr, color.booleanValue());
      log.info("Scheduling job - " + pageableJob.getJobID());
      String prior = sessionAttr.getString(Tags.PrintPriority);
      synchronized (queueMonitor) {
         if ("LOW".equals(prior)) {
            lowPriorQueue.add(pageableJob);
         } else if ("HIGH".equals(prior)) {
            highPriorQueue.add(pageableJob);
         } else {
            medPriorQueue.add(pageableJob);
         }
         queueMonitor.notify();
      }
   }
   
   // Runnable implementation -----------------------------------
   public void run() {
      log.info("Scheduler Started");
      while (scheduler != null) {
         try {
            PageableJob job;
            synchronized (queueMonitor) {
               while ((job = nextJobFromQueue()) == null) {
                  queueMonitor.wait();
               }
            }
            processJob(job);
         } catch (InterruptedException ignore) {
         }
      }
      log.info("Scheduler Stopped");
   }
   
   private int getQueuedJobCount() throws PrintException {
      PrintService ps = getPrintService();
      QueuedJobCount qjc = (QueuedJobCount) ps.getAttribute(QueuedJobCount.class);
      if (qjc == null) {
         return 0;
      }
      return qjc.getValue();
   }
   
   private PageableJob nextJobFromQueue() {
      if (!highPriorQueue.isEmpty()) {
         return (PageableJob) highPriorQueue.removeFirst();
      }
      if (!medPriorQueue.isEmpty()) {
         return (PageableJob) medPriorQueue.removeFirst();
      }
      if (!lowPriorQueue.isEmpty()) {
         return (PageableJob) lowPriorQueue.removeFirst();
      }
      return null;
   }
   
   private void processJob(PageableJob pageableJob) {
      try {
         synchronized (printerMonitor) {
            while (getQueuedJobCount() > maxQueuedJobCount) {
               log.info("Maximal number of Printer Job reached - "
               + getQueuedJobCount() + " > " + maxQueuedJobCount);
               printerMonitor.wait();
            }
         }
         invokeOnPrintSCP("onJobStartPrinting", pageableJob.getJob());
         pageableJob.initFilmBoxes(this);
         print(pageableJob, pageableJob.getSession(), pageableJob.isColor());
         log.info("Finished processing job - " + pageableJob.getJobID());
         try {
            invokeOnPrintSCP("onJobDone", pageableJob.getJob());
         } catch (Exception ignore) {}
      } catch (Throwable e) {
         log.error("Failed processing job - " + pageableJob.getJobID(), e);
         try {
            invokeOnPrintSCP("onJobFailed", pageableJob.getJob());
         } catch (Throwable ignore) {}
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
      synchronized (printerMonitor) {
         printerMonitor.notify();
      }
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
   private void setPrintRequestAttribute(PrintService ps, Attribute attr,
   PrintRequestAttributeSet aset) {
      if (ps.isAttributeValueSupported(attr,
      DocFlavor.SERVICE_FORMATTED.PAGEABLE, aset)) {
         aset.add(attr);
      } else {
         log.warn("Attribute " +  attr + " not supported by printer "
         + printerName);
      }
   }
   
   private void print(Pageable printData, Dataset sessionAttr, boolean color)
   throws PrintException {
      PrintService ps = getPrintService();
      if (autoCalibration) {
         try {
            calibrate(false);
         } catch (CalibrationException e) {
            log.warn("Calibration fails, continue printing", e);
         }
      }
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      if (printToFile) {
         setPrintRequestAttribute(ps,
            new Destination(toFile(printToFilePath).toURI()), aset);
      }
      String resId = sessionAttr == null ? null
         : sessionAttr.getString(Tags.RequestedResolutionID);
      PrinterResolution res = resId != null
         ? (PrinterResolution) this.resolutionIDMap.get(resId)
         : getDefaultPrinterResolution();
      if (res != null) {
         setPrintRequestAttribute(ps, res, aset);
      }
      setPrintRequestAttribute(ps,
         color || printGrayAsColor
            ? Chromaticity.COLOR
            : Chromaticity.MONOCHROME,
         aset);
      int copies = sessionAttr == null ? 1
         : sessionAttr.getInt(Tags.NumberOfCopies, 1);
      if (copies > 1) {
         setPrintRequestAttribute(ps, new Copies(copies), aset);
         setPrintRequestAttribute(ps, SheetCollate.COLLATED, aset);
      }
      String sessionLabel = sessionAttr == null ? null
         : sessionAttr.getString(Tags.FilmSessionLabel);
      if (sessionLabel!= null) {
         setPrintRequestAttribute(ps, new JobName(sessionLabel, null), aset);
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
   
   /** Getter for property printGrayAsColor.
    * @return Value of property printGrayAsColor.
    */
   public boolean isPrintGrayAsColor() {
      return this.printGrayAsColor;
   }
   
   /** Setter for property printGrayAsColor.
    * @param printGrayAsColor New value of property printGrayAsColor.
    */
   public void setPrintGrayAsColor(boolean printGrayAsColor) {
      this.printGrayAsColor = printGrayAsColor;
   }
   
   /** Getter for property maxQueuedJobCount.
    * @return Value of property maxQueuedJobCount.
    */
   public int getMaxQueuedJobCount() {
      return this.maxQueuedJobCount;
   }
   
   /** Setter for property maxQueuedJobCount.
    * @param maxQueuedJobCount New value of property maxQueuedJobCount.
    */
   public void setMaxQueuedJobCount(int maxQueuedJobCount) {
      this.maxQueuedJobCount = maxQueuedJobCount;
   }
   
   /** Getter for property license.
    * @return Value of property license.
    */
   public X509Certificate getLicense() {
      try {
         return (X509Certificate) server.getAttribute(printSCP, "License");
      } catch (Exception e) {
         throw new RuntimeException("JMX error", e);
      }
   }
   
   String getLicenseCN() {
      X509Certificate license = getLicense();
      if (license == null) {
         return "nobody";
      }
      String dn = license.getSubjectX500Principal().getName();
      int start = dn.indexOf("CN=");
      int end = dn.indexOf(',', start + 3);
      return dn.substring(start + 3, end);
   }
   
   Date getLicenseEndDate() {
      X509Certificate license = getLicense();
      if (license == null) {
         return new Date();
      }
      return license.getNotAfter();
   }
   
   Color toColor(String density) {
      if (WHITE.equals(density)) {
         return Color.WHITE;
      }
      if (BLACK.equals(density)) {
         return Color.BLACK;
      }
      int val = Integer.parseInt(density);
      int ddl = calibration.toDDL(val/100);
      return new Color(ddl, ddl, ddl);
   }
   
   // Inner classes -------------------------------------------------
}
