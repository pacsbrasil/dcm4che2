/*
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
 */
package com.tiani.prnscp.print;

import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
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
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.attribute.standard.SheetCollate;
import javax.print.event.PrintJobAttributeEvent;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 30, 2003
 * @created    November 3, 2002
 * @version    $Revision$ <b>Revisions:</b> <p>
 */
public class PrinterService
         extends ServiceMBeanSupport
         implements PrinterServiceMBean, Runnable,
        PrintServiceAttributeListener, PrintJobAttributeListener, PrintJobListener
{

    // Constants -----------------------------------------------------
    final static String NO = "NO";
    final static String YES = "YES";
    final static String WHITE = "WHITE";
    final static String BLACK = "BLACK";
    final static String NONE = "NONE";
    final static String REPLICATE = "REPLICATE";
    final static String BILINEAR = "BILINEAR";
    final static String CUBIC = "CUBIC";
    final static String PAPER = "PAPER";
    final static String CROP = "CROP";
    final static String DECIMATE = "DECIMATE";

    final static double PTS_PER_MM = 72 / 25.4;
    final static String ADF_FILE_EXT = ".adf";
    private final static String[] LITTLE_ENDIAN_TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };
    private final static String[] ONLY_DEFAULT_TS = {
            UIDs.ImplicitVRLittleEndian
            };

    // Attributes ----------------------------------------------------
    private String[] ts_uids = LITTLE_ENDIAN_TS;
    private final static AssociationFactory asf =
            AssociationFactory.getInstance();

    private String calledAET;

    /**  Holds value of property printSCP. */
    private ObjectName printSCP;

    /**  Holds value of property printerName. */
    private String printerName;

    /**  Holds value of property manufacturer. */
    private String manufacturer;

    /**  Holds value of property manufacturerModelName. */
    private String manufacturerModelName;

    /**  Holds value of property deviceSerialNumber. */
    private String deviceSerialNumber;

    /**  Holds value of property softwareVersion. */
    private String softwareVersion;

    /**  Holds value of property ignorePrinterIsAcceptingJobs. */
    private boolean ignorePrinterIsAcceptingJobs;

    /**  Holds value of property printToFilePath. */
    private String printToFilePath;

    /**  Holds value of property supportsColor. */
    private boolean supportsColor;

    /**  Holds value of property supportsGrayscale. */
    private boolean supportsGrayscale = true;

    /**  Holds value of property supportsPresentationLUT. */
    private boolean supportsPresentationLUT;

    /**  Holds value of property defaultPortrait. */
    private boolean defaultPortrait;

    /**  Holds value of property displayFormat. */
    private String displayFormat;

    /**  Holds value of property filmSizeID. */
    private LinkedHashMap filmSizeIDMap = new LinkedHashMap();

    /**  Holds value of property resolutionID. */
    private LinkedHashMap resolutionIDMap = new LinkedHashMap();

    /**  Holds value of property magnificationType. */
    private String magnificationType = BILINEAR;

    /**  Holds value of property borderDensity. */
    private String borderDensity = WHITE;

    private String trim = NO;

    /**  Holds value of property printGrayAsColor. */
    private boolean printGrayAsColor;

    /**  Holds value of property maxQueuedJobCount. */
    private int maxQueuedJobCount = 10;

    /**  Holds value of property pageMargin. */
    private float[] pageMargin;

    /**  Holds value of property reverseLandscape. */
    private boolean reverseLandscape = true;

    /**  Holds value of property borderThickness. */
    private float borderThickness = 1;

    /**  Holds value of property resolution. */
    private String resolution;

    private int maxNumberOfCopies;

    private String sessionLabel;

    /**  Holds value of property mediumType. */
    private String mediumType = PAPER;

    /**  Holds value of property illumination. */
    private int illumination = 150;

    /**  Holds value of property reflectedAmbientLight. */
    private int reflectedAmbientLight = 0;

    /**  Holds value of property decimateCropBehavior. */
    private String decimateCropBehavior = DECIMATE;

    /**  Holds value of property autoCalibration. */
    private boolean autoCalibration = false;

    private boolean calibrationErr = false;

    /**  Holds value of property printToFile. */
    private boolean printToFile = false;

    /**  Holds value of property annotationDir. */
    private String annotationDir;

    /**  Holds value of property lutDir. */
    private String lutDir;

    private File odFile;

    /**  Holds value of property supportsAnnotationBox. */
    private boolean supportsAnnotationBox = false;

    private String dateFormat = "yyyy-MM-dd";
    private String timeFormat = "hh:mm:ss";

    /**  Holds value of property lutForCallingAET. */
    private LinkedHashMap cfgInfoForAETMap;

    /**  Holds value of property annotationForCallingAET. */
    private LinkedHashMap annotationForCallingAETMap;

    /**  Holds value of property annotationForPrintImage. */
    private String annotationForPrintImage;

    /**  Holds value of property chunkSize. */
    private double chunkSize = 2.;

    /**  Holds value of property minimizeJobsize. */
    private boolean minimizeJobsize;

    /**  Holds value of property decimateByNearestNeighbor. */
    private boolean decimateByNearestNeighbor;

    private final PrinterCalibration calibration = new PrinterCalibration(log);
    private final ScannerCalibration scanner =
            new ScannerCalibration(log.getCategory());

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

    /**
     *  Gets the calledAET attribute of the PrinterService object
     *
     * @return    The calledAET value
     */
    public String getCalledAET()
    {
        return calledAET;
    }


    /**
     *  Getter for string value for property status.
     *
     * @return    String value of property status.
     */
    public PrinterStatus getStatus()
    {
        try {
            if (!ignorePrinterIsAcceptingJobs && !isPrinterIsAcceptingJobs()) {
                return PrinterStatus.FAILURE;
            }
            if (getQueuedJobCount() > 0 || calibrationErr) {
                return PrinterStatus.WARNING;
            }
            return PrinterStatus.NORMAL;
        } catch (PrintException e) {
            return PrinterStatus.FAILURE;
        }
    }


    /**
     *  Getter for property statusInfo.
     *
     * @return    Value of property statusInfo.
     */
    public PrinterStatusInfo getStatusInfo()
    {
        try {
            getPrintService();
            if (!ignorePrinterIsAcceptingJobs && !isPrinterIsAcceptingJobs()) {
                return PrinterStatusInfo.CHECK_PRINTER;
            }
            if (getQueuedJobCount() > 0) {
                return PrinterStatusInfo.QUEUED;
            }
            if (calibrationErr) {
                return PrinterStatusInfo.CALIBRATION_ERR;
            }
            return PrinterStatusInfo.NORMAL;
        } catch (PrintException e) {
            return PrinterStatusInfo.ELEC_SW_ERROR;
        }
    }


    /**
     *  Getter for property printSCP.
     *
     * @return    Value of property printSCP.
     */
    public ObjectName getPrintSCP()
    {
        return this.printSCP;
    }


    /**
     *  Setter for property printSCP.
     *
     * @param  printSCP  New value of property printSCP.
     */
    public void setPrintSCP(ObjectName printSCP)
    {
        this.printSCP = printSCP;
    }


    /**
     *  Getter for property printerName.
     *
     * @return    Value of property printerName.
     */
    public String getPrinterName()
    {
        return printerName;
    }


    /**
     *  Setter for property printerName.
     *
     * @param  printerName  New value of property printerName.
     */
    public void setPrinterName(String printerName)
    {
        if (!printerName.equals(this.printerName)) {
            this.printerName = printerName;
            try {
                getPrintService();
                // to register Attribute Listener
            } catch (PrintException e) {
                log.warn(e, e);
            }
        }
    }


    /**
     *  Getter for property manufacturer.
     *
     * @return    Value of property manufacturer.
     */
    public String getManufacturer()
    {
        return this.manufacturer;
    }


    /**
     *  Setter for property manufacturer.
     *
     * @param  manufacturer  New value of property manufacturer.
     */
    public void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }


    /**
     *  Getter for property manufacturerModelName.
     *
     * @return    Value of property manufacturerModelName.
     */
    public String getManufacturerModelName()
    {
        return this.manufacturerModelName;
    }


    /**
     *  Setter for property manufacturerModelName.
     *
     * @param  manufacturerModelName  New value of property
     *      manufacturerModelName.
     */
    public void setManufacturerModelName(String manufacturerModelName)
    {
        this.manufacturerModelName = manufacturerModelName;
    }


    /**
     *  Getter for property deviceSerialNumber.
     *
     * @return    Value of property deviceSerialNumber.
     */
    public String getDeviceSerialNumber()
    {
        return this.deviceSerialNumber;
    }


    /**
     *  Getter for property softwareVersion.
     *
     * @return    Value of property softwareVersion.
     */
    public String getSoftwareVersion()
    {
        return this.softwareVersion;
    }


    /**
     *  Setter for property softwareVersion.
     *
     * @param  softwareVersion  New value of property softwareVersion.
     */
    public void setSoftwareVersion(String softwareVersion)
    {
        this.softwareVersion = softwareVersion;
    }


    /**
     *  Setter for property deviceSerialNumber.
     *
     * @param  deviceSerialNumber  New value of property deviceSerialNumber.
     */
    public void setDeviceSerialNumber(String deviceSerialNumber)
    {
        this.deviceSerialNumber = deviceSerialNumber;
    }


    /**
     *  Getter for property ignorePrinterIsAcceptingJobs.
     *
     * @return    Value of property ignorePrinterIsAcceptingJobs.
     */
    public boolean isIgnorePrinterIsAcceptingJobs()
    {
        return this.ignorePrinterIsAcceptingJobs;
    }


    /**
     *  Setter for property ignorePrinterIsAcceptingJobs.
     *
     * @param  ignorePrinterIsAcceptingJobs  New value of property
     *      ignorePrinterIsAcceptingJobs.
     */
    public void setIgnorePrinterIsAcceptingJobs(boolean ignorePrinterIsAcceptingJobs)
    {
        this.ignorePrinterIsAcceptingJobs = ignorePrinterIsAcceptingJobs;
    }


    /**
     *  Getter for property minimizeJobsize.
     *
     * @return    Value of property minimizeJobsize.
     */
    public boolean isMinimizeJobsize()
    {
        return this.minimizeJobsize;
    }


    /**
     *  Setter for property minimizeJobsize.
     *
     * @param  minimizeJobsize  New value of property minimizeJobsize.
     */
    public void setMinimizeJobsize(boolean minimizeJobsize)
    {
        this.minimizeJobsize = minimizeJobsize;
    }


    /**
     *  Getter for property decimateByNearestNeighbor.
     *
     * @return    Value of property decimateByNearestNeighbor.
     */
    public boolean isDecimateByNearestNeighbor()
    {
        return this.decimateByNearestNeighbor;
    }


    /**
     *  Setter for property decimateByNearestNeighbor.
     *
     * @param  decimateByNearestNeighbor  New value of property
     *      decimateByNearestNeighbor.
     */
    public void setDecimateByNearestNeighbor(boolean decimateByNearestNeighbor)
    {
        this.decimateByNearestNeighbor = decimateByNearestNeighbor;
    }


    /**
     *  Getter for property chunkSize.
     *
     * @return    Value of property chunkSize.
     */
    public double getChunkSize()
    {
        return this.chunkSize;
    }


    /**
     *  Setter for property chunkSize.
     *
     * @param  chunkSize  New value of property chunkSize.
     */
    public void setChunkSize(double chunkSize)
    {
        this.chunkSize = chunkSize;
    }


    private PrintService getPrintService()
        throws PrintException
    {
        if (printService != null) {
            if (printService.getName().equals(printerName)) {
                return printService;
            }
            printService.removePrintServiceAttributeListener(this);
            printService = null;
        }
        PrintService[] services = PrintServiceLookup.lookupPrintServices(
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


    /**
     *  Getter for property printToFile.
     *
     * @return    Value of property printToFile.
     */
    public boolean isPrintToFile()
    {
        return this.printToFile;
    }


    /**
     *  Setter for property printToFile.
     *
     * @param  printToFile  New value of property printToFile.
     */
    public void setPrintToFile(boolean printToFile)
    {
        this.printToFile = printToFile;
    }


    /**
     *  Getter for property printToFilePath.
     *
     * @return    Value of property printToFilePath.
     */
    public String getPrintToFilePath()
    {
        return this.printToFilePath;
    }


    /**
     *  Setter for property printToFilePath.
     *
     * @param  printToFilePath  New value of property printToFilePath.
     */
    public void setPrintToFilePath(String printToFilePath)
    {
        this.printToFilePath = printToFilePath;
    }


    /**
     *  Getter for property availableDestinations.
     *
     * @return    Value of property availableDestinations.
     */
    public String[] getAvailablePrinters()
    {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(
                DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
        String[] names = new String[services.length];
        for (int i = 0; i < services.length; ++i) {
            names[i] = services[i].getName();
        }
        return names;
    }


    /**
     *  Getter for property printServiceAttributes.
     *
     * @return    Value of property printServiceAttributes.
     */
    public String[] getPrintServiceAttributes()
    {
        try {
            return toStringArray(getPrintService().getAttributes());
        } catch (PrintException e) {
            return new String[]{e.getMessage()};
        }
    }


    static String[] toStringArray(AttributeSet as)
    {
        Attribute[] a = as.toArray();
        String[] result = new String[a.length];
        for (int i = 0; i < a.length; ++i) {
            result[i] =
                    org.jboss.util.Classes.stripPackageName(a[i].getCategory()) + "=" + a[i];
        }
        return result;
    }


    /**
     *  Getter for property supportedAttributeValues.
     *
     * @return    Value of property supportedAttributeValues.
     */
    public String[] getSupportedAttributeValues()
    {
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
            return new String[]{e.getMessage()};
        }
    }


    /**
     *  Getter for property supportsColor.
     *
     * @return    Value of property supportsColor.
     */
    public boolean isSupportsColor()
    {
        return this.supportsColor;
    }


    /**
     *  Setter for property supportsColor.
     *
     * @param  supportsColor  New value of property supportsColor.
     */
    public void setSupportsColor(boolean supportsColor)
    {
        this.supportsColor = supportsColor;
    }


    /**
     *  Getter for property supportsGrayscale.
     *
     * @return    Value of property supportsGrayscale.
     */
    public boolean isSupportsGrayscale()
    {
        return this.supportsGrayscale;
    }


    /**
     *  Setter for property supportsGrayscale.
     *
     * @param  supportsGrayscale  New value of property supportsGrayscale.
     */
    public void setSupportsGrayscale(boolean supportsGrayscale)
    {
        this.supportsGrayscale = supportsGrayscale;
    }


    /**
     *  Getter for property supportsPresentationLUT.
     *
     * @return    Value of property supportsPresentationLUT.
     */
    public boolean isSupportsPresentationLUT()
    {
        return this.supportsPresentationLUT;
    }


    /**
     *  Setter for property supportsPresentationLUT.
     *
     * @param  supportsPresentationLUT  New value of property
     *      supportsPresentationLUT.
     */
    public void setSupportsPresentationLUT(boolean supportsPresentationLUT)
    {
        this.supportsPresentationLUT = supportsPresentationLUT;
    }


    /**
     *  Gets the dateFormat attribute of the PrinterService object
     *
     * @return    The dateFormat value
     */
    public String getDateFormat()
    {
        return dateFormat;
    }


    /**
     *  Sets the dateFormat attribute of the PrinterService object
     *
     * @param  dateFormat  The new dateFormat value
     */
    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }


    /**
     *  Gets the timeFormat attribute of the PrinterService object
     *
     * @return    The timeFormat value
     */
    public String getTimeFormat()
    {
        return timeFormat;
    }


    /**
     *  Sets the timeFormat attribute of the PrinterService object
     *
     * @param  timeFormat  The new timeFormat value
     */
    public void setTimeFormat(String timeFormat)
    {
        this.timeFormat = timeFormat;
    }


    /**
     *  Gets the sessionLabel attribute of the PrinterService object
     *
     * @return    The sessionLabel value
     */
    public String getSessionLabel()
    {
        return sessionLabel;
    }


    /**
     *  Sets the sessionLabel attribute of the PrinterService object
     *
     * @param  sessionLabel  The new sessionLabel value
     */
    public void setSessionLabel(String sessionLabel)
    {
        this.sessionLabel = sessionLabel;
    }


    /**
     *  Gets the maxNumberOfCopies attribute of the PrinterService object
     *
     * @return    The maxNumberOfCopies value
     */
    public int getMaxNumberOfCopies()
    {
        return maxNumberOfCopies;
    }


    /**
     *  Sets the maxNumberOfCopies attribute of the PrinterService object
     *
     * @param  maxNumberOfCopies  The new maxNumberOfCopies value
     */
    public void setMaxNumberOfCopies(int maxNumberOfCopies)
    {
        if (maxNumberOfCopies <= 0) {
            throw new IllegalArgumentException("max:" + maxNumberOfCopies);
        }
        this.maxNumberOfCopies = maxNumberOfCopies;
    }


    /**
     *  Getter for property mediaType.
     *
     * @return    Value of property mediaType.
     */
    public String getMediumType()
    {
        return this.mediumType;
    }


    /**
     *  Setter for property mediaType.
     *
     * @param  mediumType  The new mediumType value
     */
    public void setMediumType(String mediumType)
    {
        this.mediumType = mediumType;
    }


    /**
     *  Getter for property defaultMediumType.
     *
     * @return    Value of property defaultMediumType.
     */
    public String getDefaultMediumType()
    {
        return firstOf(mediumType);
    }


    /**
     *  Gets the supportsMediumType attribute of the PrinterService object
     *
     * @param  mediumType  Description of the Parameter
     * @return             The supportsMediumType value
     */
    public boolean isSupportsMediumType(String mediumType)
    {
        return contains(this.mediumType, mediumType);
    }


    /**
     *  Getter for property defaultPortrait.
     *
     * @return    Value of property defaultPortrait.
     */
    public boolean isDefaultPortrait()
    {
        return this.defaultPortrait;
    }


    /**
     *  Setter for property defaultPortrait.
     *
     * @param  defaultPortrait  New value of property defaultPortrait.
     */
    public void setDefaultPortrait(boolean defaultPortrait)
    {
        this.defaultPortrait = defaultPortrait;
    }


    /**
     *  Getter for property defaultFilmOrientation.
     *
     * @return    Value of property defaultFilmOrientation.
     */
    public String getDefaultFilmOrientation()
    {
        return defaultPortrait ? "PORTRAIT" : "LANDSCAPE";
    }


    /**
     *  Getter for property displayFormat.
     *
     * @return    Value of property displayFormat.
     */
    public String getDisplayFormat()
    {
        return this.displayFormat;
    }


    /**
     *  Setter for property displayFormat.
     *
     * @param  displayFormat  New value of property displayFormat.
     */
    public void setDisplayFormat(String displayFormat)
    {
        this.displayFormat = displayFormat;
    }


    /**
     *  Gets the supportsDisplayFormat attribute of the PrinterService object
     *
     * @param  displayFormat    Description of the Parameter
     * @param  filmOrientation  Description of the Parameter
     * @return                  The supportsDisplayFormat value
     */
    public boolean isSupportsDisplayFormat(String displayFormat,
            String filmOrientation)
    {
        if (!displayFormat.startsWith("STANDARD\\")) {
            return false;
        }
        if (filmOrientation != null
                 ? filmOrientation.equals("PORTRAIT")
                 : defaultPortrait) {
            return contains(this.displayFormat, displayFormat.substring(9));
        }
        int pos = displayFormat.lastIndexOf(',');
        return contains(this.displayFormat,
                displayFormat.substring(pos + 1) + ',' + displayFormat.substring(9, pos));
    }


    private static String firstOf(String list)
    {
        if (list == null || list.length() == 0) {
            return null;
        }
        int pos = list.indexOf('\\');
        return pos == -1 ? list : list.substring(0, pos);
    }


    private static boolean contains(String list, String value)
    {
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


    /**
     *  Getter for property filmSizeID.
     *
     * @return    Value of property filmSizeID.
     */
    public String getFilmSizeID()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = filmSizeIDMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry item = (Map.Entry) it.next();
            float[] wh = (float[]) item.getValue();
            sb.append(item.getKey());
            sb.append(':');
            sb.append(wh[0]);
            sb.append('x');
            sb.append(wh[1]);
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    /**
     *  Setter for property filmSizeID.
     *
     * @param  filmSizeID  New value of property filmSizeID.
     */
    public void setFilmSizeID(String filmSizeID)
    {
        LinkedHashMap tmp = new LinkedHashMap();
        String[] strings = toStringArray(filmSizeID);
        for (int i = 0; i < strings.length; ++i) {
            String s = strings[i];
            int c1 = s.indexOf(':');
            int xpos = s.indexOf('x', c1 + 1);
            float[] wh = {
                    Float.parseFloat(s.substring(c1 + 1, xpos)),
                    Float.parseFloat(s.substring(xpos + 1))
                    };
            Arrays.sort(wh);
            if (wh[0] <= 0) {
                throw new IllegalArgumentException(s);
            }
            tmp.put(s.substring(0, c1), wh);
        }
        filmSizeIDMap = tmp;
    }


    /**
     *  Getter for property defaultFilmSizeID.
     *
     * @return    Value of property defaultFilmSizeID.
     */
    public String getDefaultFilmSizeID()
    {
        if (filmSizeIDMap.isEmpty()) {
            return null;
        }
        return (String) filmSizeIDMap.keySet().iterator().next();
    }


    /**
     *  Gets the supportsFilmSizeID attribute of the PrinterService object
     *
     * @param  filmSizeID  Description of the Parameter
     * @return             The supportsFilmSizeID value
     */
    public boolean isSupportsFilmSizeID(String filmSizeID)
    {
        return filmSizeIDMap.containsKey(filmSizeID);
    }


    /**
     *  Gets the supportsAnnotationDisplayFormatID attribute of the
     *  PrinterService object
     *
     * @param  annotationID  Description of the Parameter
     * @return               The supportsAnnotationDisplayFormatID value
     */
    public boolean isSupportsAnnotationDisplayFormatID(String annotationID)
    {
        return supportsAnnotationBox && countAnnotationBoxes(annotationID) != -1;
    }


    private Paper toPaper(float[] wh)
    {
        Paper paper = new Paper();
        paper.setSize(wh[0] * PTS_PER_MM, wh[1] * PTS_PER_MM);
        paper.setImageableArea(
                pageMargin[0] * PTS_PER_MM,
                pageMargin[1] * PTS_PER_MM,
                (wh[0] - (pageMargin[0] + pageMargin[2])) * PTS_PER_MM,
                (wh[1] - (pageMargin[1] + pageMargin[3])) * PTS_PER_MM);
        return paper;
    }


    Paper getPaper(String filmSizeID)
    {
        return toPaper((float[]) filmSizeIDMap.get(filmSizeID));
    }


    Paper getDefaultPaper()
    {
        if (filmSizeIDMap.isEmpty()) {
            return null;
        }
        return toPaper((float[]) filmSizeIDMap.values().iterator().next());
    }


    /**
     *  Getter for property resolutionID.
     *
     * @return    Value of property resolutionID.
     */
    public String getResolutionID()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = resolutionIDMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry item = (Map.Entry) it.next();
            PrinterResolution pr = (PrinterResolution) item.getValue();
            sb.append(item.getKey());
            sb.append(':');
            sb.append(pr.getFeedResolution(PrinterResolution.DPI));
            sb.append('x');
            sb.append(pr.getCrossFeedResolution(PrinterResolution.DPI));
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    /**
     *  Setter for property resolutionID.
     *
     * @param  resolutionID  New value of property resolutionID.
     */
    public void setResolutionID(String resolutionID)
    {
        LinkedHashMap tmp = new LinkedHashMap();
        String[] strings = toStringArray(resolutionID);
        for (int i = 0; i < strings.length; ++i) {
            String s = strings[i];
            int c1 = s.indexOf(':');
            int xpos = s.indexOf('x', c1 + 1);
            PrinterResolution pr = new PrinterResolution(
                    Integer.parseInt(s.substring(c1 + 1, xpos)),
                    Integer.parseInt(s.substring(xpos + 1)),
                    PrinterResolution.DPI);
            tmp.put(s.substring(0, c1), pr);
        }
        resolutionIDMap = tmp;
    }


    /**
     *  Getter for property defaultResolutionID.
     *
     * @return    Value of property defaultResolutionID.
     */
    public String getDefaultResolutionID()
    {
        if (resolutionIDMap.isEmpty()) {
            return null;
        }
        return (String) resolutionIDMap.keySet().iterator().next();
    }


    /**
     *  Gets the defaultPrinterResolution attribute of the PrinterService object
     *
     * @return    The defaultPrinterResolution value
     */
    public PrinterResolution getDefaultPrinterResolution()
    {
        if (resolutionIDMap.isEmpty()) {
            return null;
        }
        return (PrinterResolution) resolutionIDMap.values().iterator().next();
    }


    /**
     *  Gets the supportsResolutionID attribute of the PrinterService object
     *
     * @param  resolutionID  Description of the Parameter
     * @return               The supportsResolutionID value
     */
    public boolean isSupportsResolutionID(String resolutionID)
    {
        return resolutionIDMap.containsKey(resolutionID);
    }


    /**
     *  Getter for property magnificationType.
     *
     * @return    Value of property magnificationType.
     */
    public String getMagnificationType()
    {
        return this.magnificationType;
    }


    /**
     *  Setter for property magnificationType.
     *
     * @param  magnificationType  New value of property magnificationType.
     */
    public void setMagnificationType(String magnificationType)
    {
        this.magnificationType = magnificationType;
    }


    /**
     *  Getter for property defaultMagnificationType.
     *
     * @return    Value of property defaultMagnificationType.
     */
    public String getDefaultMagnificationType()
    {
        return firstOf(magnificationType);
    }


    /**
     *  Gets the supportsMagnificationType attribute of the PrinterService
     *  object
     *
     * @param  magnificationType  Description of the Parameter
     * @return                    The supportsMagnificationType value
     */
    public boolean isSupportsMagnificationType(String magnificationType)
    {
        return contains(this.magnificationType, magnificationType);
    }


    /**
     *  Getter for property decimateCropBehavior.
     *
     * @return    Value of property decimateCropBehavior.
     */
    public String getDecimateCropBehavior()
    {
        return this.decimateCropBehavior;
    }


    /**
     *  Setter for property decimateCropBehavior.
     *
     * @param  decimateCropBehavior  New value of property decimateCropBehavior.
     */
    public void setDecimateCropBehavior(String decimateCropBehavior)
    {
        this.decimateCropBehavior = decimateCropBehavior;
    }


    /**
     *  Getter for property borderDensity.
     *
     * @return    Value of property borderDensity.
     */
    public String getBorderDensity()
    {
        return this.borderDensity;
    }


    /**
     *  Setter for property borderDensity.
     *
     * @param  borderDensity  New value of property borderDensity.
     */
    public void setBorderDensity(String borderDensity)
    {
        this.borderDensity = borderDensity;
    }


    /**
     *  Gets the trim attribute of the PrinterService object
     *
     * @return    The trim value
     */
    public String getTrim()
    {
        return trim;
    }


    /**
     *  Sets the trim attribute of the PrinterService object
     *
     * @param  trim  The new trim value
     */
    public void setTrim(String trim)
    {
        this.trim = trim;
    }


    /**
     *  Gets the minDensity attribute of the PrinterService object
     *
     * @return    The minDensity value
     */
    public int getMinDensity()
    {
        return calibration.getMinDensity();
    }


    /**
     *  Gets the maxDensity attribute of the PrinterService object
     *
     * @return    The maxDensity value
     */
    public int getMaxDensity()
    {
        return calibration.getMaxDensity();
    }


    /**
     *  Getter for property margin.
     *
     * @return    Value of property margin.
     */
    public String getPageMargin()
    {
        return ""
                 + pageMargin[0] + ','
                 + pageMargin[1] + ','
                 + pageMargin[2] + ','
                 + pageMargin[3];
    }


    /**
     *  Setter for property margin.
     *
     * @param  pageMargin  The new pageMargin value
     */
    public void setPageMargin(String pageMargin)
    {
        float[] tmp = toFloatArray(pageMargin);
        if (tmp.length != 4) {
            throw new IllegalArgumentException("pageMargin: " + pageMargin);
        }
        this.pageMargin = tmp;
    }


    /**
     *  Getter for property reverseLandscape.
     *
     * @return    Value of property reverseLandscape.
     */
    public boolean isReverseLandscape()
    {
        return this.reverseLandscape;
    }


    /**
     *  Setter for property reverseLandscape.
     *
     * @param  reverseLandscape  New value of property reverseLandscape.
     */
    public void setReverseLandscape(boolean reverseLandscape)
    {
        this.reverseLandscape = reverseLandscape;
    }


    /**
     *  Getter for property borderThickness.
     *
     * @return    Value of property borderThickness.
     */
    public float getBorderThickness()
    {
        return this.borderThickness;
    }


    /**
     *  Setter for property borderThickness.
     *
     * @param  borderThickness  New value of property borderThickness.
     */
    public void setBorderThickness(float borderThickness)
    {
        this.borderThickness = borderThickness;
    }


    /**
     *  Getter for property illumination.
     *
     * @return    Value of property illumination.
     */
    public int getIllumination()
    {
        return this.illumination;
    }


    /**
     *  Setter for property illumination.
     *
     * @param  illumination  New value of property illumination.
     */
    public void setIllumination(int illumination)
    {
        this.illumination = illumination;
    }


    /**
     *  Getter for property reflectedAmbientLight.
     *
     * @return    Value of property reflectedAmbientLight.
     */
    public int getReflectedAmbientLight()
    {
        return this.reflectedAmbientLight;
    }


    /**
     *  Setter for property reflectedAmbientLight.
     *
     * @param  reflectedAmbientLight  New value of property
     *      reflectedAmbientLight.
     */
    public void setReflectedAmbientLight(int reflectedAmbientLight)
    {
        this.reflectedAmbientLight = reflectedAmbientLight;
    }


    /**
     *  Getter for property annotationDir.
     *
     * @return    Value of property annotationDir.
     */
    public String getAnnotationDir()
    {
        return this.annotationDir;
    }


    /**
     *  Setter for property annotationDir.
     *
     * @param  annotationDir  New value of property annotationDir.
     */
    public void setAnnotationDir(String annotationDir)
    {
        this.annotationDir = toFile(annotationDir).getAbsolutePath();
    }


    // used by testdriver
    void setAnnotationDir(File annotationDir)
    {
        this.annotationDir = annotationDir.getAbsolutePath();
    }


    private static int parseAnnotationBoxCount(String fname)
    {
        if (!fname.endsWith(ADF_FILE_EXT)) {
            throw new IllegalArgumentException("fname:" + fname);
        }
        int extPos = fname.length() - ADF_FILE_EXT.length();
        int numPos = fname.lastIndexOf('.', extPos - 1);
        return Integer.parseInt(fname.substring(numPos + 1, extPos));
    }


    private final FilenameFilter ADF_FILENAME_FILTER =
        new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                try {
                    return parseAnnotationBoxCount(name) >= 0;
                } catch (RuntimeException e) {
                    log.warn("Illegal ADF Filename - " + name);
                    return false;
                }
            }
        };


    private static void skipFileExt(String[] fnames, String ext)
    {
        int extlen = ext.length();
        for (int i = 0; i < fnames.length; ++i) {
            fnames[i] = fnames[i].substring(0, fnames[i].length() - extlen);
        }
    }


    /**
     *  Getter for property annotationDisplayFormatIDs.
     *
     * @return    Value of property annotationDisplayFormatIDs.
     */
    public String[] getAnnotationDisplayFormatIDs()
    {
        File dir = toFile(annotationDir);
        String[] fnames = dir.list(ADF_FILENAME_FILTER);
        for (int i = 0; i < fnames.length; ++i) {
            String fname = fnames[i];
            int extPos = fname.length() - ADF_FILE_EXT.length();
            int numPos = fname.lastIndexOf('.', extPos - 1);
            fnames[i] = fname.substring(0, numPos);
        }
        return fnames;
    }


    /**
     *  Description of the Method
     *
     * @param  annotationID  Description of the Parameter
     * @return               Description of the Return Value
     */
    public int countAnnotationBoxes(String annotationID)
    {
        File f = getAnnotationFile(annotationID);
        if (f == null) {
            return -1;
        }

        return parseAnnotationBoxCount(f.getName());
    }


    File getAnnotationFile(String annotationID)
    {
        File dir = toFile(annotationDir);
        File[] files = dir.listFiles(ADF_FILENAME_FILTER);
        for (int i = 0; i < files.length; ++i) {
            if (files[i].getName().startsWith(annotationID)) {
                return files[i];
            }
        }
        return null;
    }


    /**
     *  Getter for property lutDir.
     *
     * @return    Value of property lutDir.
     */
    public String getLUTDir()
    {
        return this.lutDir;
    }


    /**
     *  Setter for property lutDir.
     *
     * @param  lutDir  New value of property lutDir.
     */
    public void setLUTDir(String lutDir)
    {
        this.lutDir = toFile(lutDir).getAbsolutePath();
    }


    /**
     *  Gets the supportsConfigurationInformation attribute of the
     *  PrinterService object
     *
     * @param  configInfo  Description of the Parameter
     * @return             The supportsConfigurationInformation value
     */
    public boolean isSupportsConfigurationInformation(String configInfo)
    {
        try {
            new PLutBuilder(configInfo, lutDir);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    /**
     *  Getter for property supportsAnnotationBox.
     *
     * @return    Value of property supportsAnnotationBox.
     */
    public boolean isSupportsAnnotationBox()
    {
        return this.supportsAnnotationBox;
    }


    /**
     *  Setter for property supportsAnnotationBox.
     *
     * @param  supportsAnnotationBox  New value of property
     *      supportsAnnotationBox.
     */
    public void setSupportsAnnotationBox(boolean supportsAnnotationBox)
    {
        this.supportsAnnotationBox = supportsAnnotationBox;
    }


    /**
     *  Gets the configurationInformationForCallingAET attribute of the PrinterService object
     *
     * @return    The configurationInformationForCallingAET value
     */
    public String getConfigurationInformationForCallingAET()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = cfgInfoForAETMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry item = (Map.Entry) it.next();
            sb.append(item.getKey());
            sb.append(':');
            sb.append(item.getValue());
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    String getConfigurationInformationForCallingAET(String callingAET)
    {
        if (cfgInfoForAETMap.isEmpty()) {
            log.error("Configuration Error: missing attribute ConfigurationInformationForCallingAET value!");
            return null;
        }
        String cfgInfo = (String) cfgInfoForAETMap.get(callingAET);
        return cfgInfo != null
                 ? cfgInfo
                 : (String) cfgInfoForAETMap.values().iterator().next();
    }


    /**
     *  Sets the configurationInformationForCallingAET attribute of the PrinterService object
     *
     * @param  ciForCallingAET  The new configurationInformationForCallingAET value
     */
    public void setConfigurationInformationForCallingAET(String ciForCallingAET)
    {
        LinkedHashMap tmp = new LinkedHashMap();
        String[] strings = toStringArray(ciForCallingAET);
        for (int i = 0; i < strings.length; ++i) {
            String s = strings[i];
            int c1 = s.indexOf(':');
            if (c1 == -1) {
                throw new IllegalArgumentException(s);
            }
            tmp.put(s.substring(0, c1), s.substring(c1 + 1));
        }
        cfgInfoForAETMap = tmp;
    }


    /**
     *  Getter for property annotationForCallingAET.
     *
     * @return    Value of property annotationForCallingAET.
     */
    public String getAnnotationForCallingAET()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = annotationForCallingAETMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry item = (Map.Entry) it.next();
            sb.append(item.getKey());
            sb.append(':');
            sb.append(item.getValue());
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    String getAnnotationForCallingAET(String callingAET)
    {
        if (annotationForCallingAETMap.isEmpty()) {
            log.error("Configuration Error: missing attribute AnnotationForCallingAET value!");
            return null;
        }
        String adfID = (String) annotationForCallingAETMap.get(callingAET);
        return adfID != null
                 ? adfID
                 : (String) annotationForCallingAETMap.values().iterator().next();
    }


    /**
     *  Setter for property annotationForCallingAET.
     *
     * @param  annotationForCallingAET  New value of property
     *      annotationForCallingAET.
     */
    public void setAnnotationForCallingAET(String annotationForCallingAET)
    {
        LinkedHashMap tmp = new LinkedHashMap();
        String[] strings = toStringArray(annotationForCallingAET);
        for (int i = 0; i < strings.length; ++i) {
            String s = strings[i];
            int c1 = s.indexOf(':');
            if (c1 == -1) {
                throw new IllegalArgumentException(s);
            }
            tmp.put(s.substring(0, c1), s.substring(c1 + 1));
        }
        annotationForCallingAETMap = tmp;
    }


    /**
     *  Getter for property annotationForPrintImage.
     *
     * @return    Value of property annotationForPrintImage.
     */
    public String getAnnotationForPrintImage()
    {
        return this.annotationForPrintImage;
    }


    /**
     *  Setter for property annotationForPrintImage.
     *
     * @param  annotationForPrintImage  New value of property annotationForPrintImage.
     */
    public void setAnnotationForPrintImage(String annotationForPrintImage)
    {
        this.annotationForPrintImage = annotationForPrintImage;
    }


    /**
     *  Getter for property dateOfLastCalibration.
     *
     * @return    Value of property dateOfLastCalibration.
     */
    public String getDateOfLastCalibration()
    {
        return calibration.getDateOfLastCalibration();
    }


    /**
     *  Getter for property timeOfLastCalibration.
     *
     * @return    Value of property timeOfLastCalibration.
     */
    public String getTimeOfLastCalibration()
    {
        return calibration.getTimeOfLastCalibration();
    }


    private static String[] toStringArray(String text)
    {
        StringTokenizer stk = new StringTokenizer(text, " \t\r\n\\");
        String[] a = new String[stk.countTokens()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = stk.nextToken();
        }
        return a;
    }


    private static float[] toFloatArray(String text)
    {
        StringTokenizer stk = new StringTokenizer(text, ",; \t\r\n\\");
        float[] a = new float[stk.countTokens()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = Float.parseFloat(stk.nextToken());
        }
        return a;
    }


    private static int[] toIntArray(String text)
    {
        StringTokenizer stk = new StringTokenizer(text, ",; \t\r\n\\");
        int[] a = new int[stk.countTokens()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = Integer.parseInt(stk.nextToken());
        }
        return a;
    }


    /**
     *  Gets the calibrationDir attribute of the PrinterService object
     *
     * @return    The calibrationDir value
     */
    public String getCalibrationDir()
    {
        return odFile.getParentFile().getAbsolutePath();
    }


    /**
     *  Sets the calibrationDir attribute of the PrinterService object
     *
     * @param  dir  The new calibrationDir value
     */
    public void setCalibrationDir(String dir)
        throws IOException
    {
        File d = toFile(dir);
        odFile = new File(d, calledAET + ".ods");
        if (!odFile.isFile()) {
            log.warn("Could not find file " + odFile
                     + " required for basis calibration");
        }
        scanner.setScanDir(new File(d, calledAET));
    }


    /**
     *  Gets the scanPointExtension attribute of the PrinterService object
     *
     * @return    The scanPointExtension value
     */
    public int getScanPointExtension()
    {
        return scanner.getScanPointExtension();
    }


    /**
     *  Sets the scanPointExtension attribute of the PrinterService object
     *
     * @param  extension  The new scanPointExtension value
     */
    public void setScanPointExtension(int extension)
    {
        scanner.setScanPointExtension(extension);
    }


    /**
     *  Gets the scanThreshold attribute of the PrinterService object
     *
     * @return    The scanThreshold value
     */
    public String getScanThreshold()
    {
        return scanner.getScanThreshold();
    }


    /**
     *  Sets the scanThreshold attribute of the PrinterService object
     *
     * @param  scanThreshold  The new scanThreshold value
     */
    public void setScanThreshold(String scanThreshold)
    {
        scanner.setScanThreshold(scanThreshold);
    }


    /**
     *  Gets the autoCalibration attribute of the PrinterService object
     *
     * @return    The autoCalibration value
     */
    public boolean isAutoCalibration()
    {
        return this.autoCalibration;
    }


    /**
     *  Sets the autoCalibration attribute of the PrinterService object
     *
     * @param  autoCalibration  The new autoCalibration value
     */
    public void setAutoCalibration(boolean autoCalibration)
    {
        this.autoCalibration = autoCalibration;
    }



    /**
     *  Gets the printGrayAsColor attribute of the PrinterService object
     *
     * @return    The printGrayAsColor value
     */
    public boolean isPrintGrayAsColor()
    {
        return this.printGrayAsColor;
    }


    /**
     *  Sets the printGrayAsColor attribute of the PrinterService object
     *
     * @param  printGrayAsColor  The new printGrayAsColor value
     */
    public void setPrintGrayAsColor(boolean printGrayAsColor)
    {
        this.printGrayAsColor = printGrayAsColor;
    }


    /**
     *  Gets the maxQueuedJobCount attribute of the PrinterService object
     *
     * @return    The maxQueuedJobCount value
     */
    public int getMaxQueuedJobCount()
    {
        return this.maxQueuedJobCount;
    }


    /**
     *  Sets the maxQueuedJobCount attribute of the PrinterService object
     *
     * @param  maxQueuedJobCount  The new maxQueuedJobCount value
     */
    public void setMaxQueuedJobCount(int maxQueuedJobCount)
    {
        this.maxQueuedJobCount = maxQueuedJobCount;
    }


    /**
     *  Gets the license attribute of the PrinterService object
     *
     * @return    The license value
     */
    public X509Certificate getLicense()
    {
        try {
            return (X509Certificate) server.getAttribute(printSCP, "License");
        } catch (Exception e) {
            throw new RuntimeException("JMX error", e);
        }
    }


    String getLicenseCN()
    {
        X509Certificate license = getLicense();
        if (license == null) {
            return "nobody";
        }
        String dn = license.getSubjectX500Principal().getName();
        int start = dn.indexOf("CN=");
        int end = dn.indexOf(',', start + 3);
        return dn.substring(start + 3, end);
    }


    Date getLicenseEndDate()
    {
        X509Certificate license = getLicense();
        if (license == null) {
            return new Date();
        }
        return license.getNotAfter();
    }


    /*
    protected PrinterCalibration getPrinterCalibration()
    {
        return calibration;
    }
*/
    /**
     *  Gets the pValToDDL attribute of the PrinterService object
     *
     * @param  n     Description of the Parameter
     * @param  dmin  Description of the Parameter
     * @param  dmax  Description of the Parameter
     * @param  l0    Description of the Parameter
     * @param  la    Description of the Parameter
     * @param  plut  Description of the Parameter
     * @return       The pValToDDL value
     */
    public byte[] getPValToDDL(int n, float dmin, float dmax,
            float l0, float la, Dataset plut)
    {
        return calibration.getPValToDDL(n, dmin, dmax, l0, la, plut);
    }


    /**
     *  Description of the Method
     *
     * @param  force                     Description of the Parameter
     * @exception  CalibrationException  Description of the Exception
     */
    public void calibrate(boolean force)
        throws CalibrationException
    {
        log.info("Calibrating " + calledAET + "/" + printerName);
        calibration.setODs(scanner.calculateGrayscaleODs(force));
        calibration.setODsTS(scanner.getLastScanFileModified());
        log.info("Calibrated " + calledAET + "/" + printerName);
    }


    /**
     *  Description of the Method
     *
     * @param  fname               Description of the Parameter
     * @param  configInfo          Description of the Parameter
     * @exception  IOException     Description of the Exception
     * @exception  PrintException  Description of the Exception
     */
    public void printImage(String fname, String configInfo)
        throws IOException, PrintException
    {
        PrintImageJob job = new PrintImageJob(this, new File(fname), configInfo);
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        PrintService ps = getPrintService();
        setPrintRequestAttribute(ps, getDefaultPrinterResolution(), aset);
        setPrintRequestAttribute(ps,
                printGrayAsColor
                 ? Chromaticity.COLOR
                 : Chromaticity.MONOCHROME,
                aset);
        log.info("Printing " + fname);
        setPrintRequestAttribute(ps,
                new JobName(job.getName(), null), aset);
        print(job, aset, autoCalibration && configInfo.length() > 0);
        log.info("Printed " + fname);
    }

    // ServiceMBeanSupport overrides ------------------------------------
    /**
     *  Gets the objectName attribute of the PrinterService object
     *
     * @param  server                            Description of the Parameter
     * @param  name                              Description of the Parameter
     * @return                                   The objectName value
     * @exception  MalformedObjectNameException  Description of the Exception
     */
    protected ObjectName getObjectName(MBeanServer server, ObjectName name)
        throws MalformedObjectNameException
    {
        calledAET = name.getKeyProperty("calledAET");
        if (!new ObjectName(OBJECT_NAME_PREFIX + calledAET).equals(name)) {
            throw new MalformedObjectNameException("name: " + name);
        }
        return name;
    }


    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    public void startService()
        throws Exception
    {
        scheduler = new Thread(this);
        scheduler.start();
        putAcceptorPolicy(getAcceptorPolicy());
    }


    private AcceptorPolicy getAcceptorPolicy()
    {
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


    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    public void stopService()
        throws Exception
    {
        putAcceptorPolicy(null);
        Thread tmp = scheduler;
        scheduler = null;
        tmp.interrupt();
    }


    private void invokeOnPrintSCP(String methode, String arg)
        throws Exception
    {
        server.invoke(printSCP, methode,
                new Object[]{arg},
                new String[]{String.class.getName()});
    }


    private void putAcceptorPolicy(AcceptorPolicy policy)
        throws Exception
    {
        server.invoke(printSCP, "putAcceptorPolicy",
                new Object[]{
                calledAET,
                getAcceptorPolicy()
                },
                new String[]{
                String.class.getName(),
                AcceptorPolicy.class.getName()
                });
    }


    /**
     *  Description of the Method
     *
     * @param  color        Description of the Parameter
     * @param  job          Description of the Parameter
     * @param  sessionAttr  Description of the Parameter
     */
    public void scheduleJob(Boolean color, String job, Dataset sessionAttr)
    {
        ScheduledJob pageableJob =
                new ScheduledJob(job, sessionAttr, color.booleanValue());
        log.info("Scheduling job - " + pageableJob.getJobID());
        String sessionLabel = sessionAttr.getString(Tags.FilmSessionLabel);
        if (sessionLabel == null) {
            sessionAttr.putLO(Tags.FilmSessionLabel,
                    Annotation.makeDefaultSessionLabel(this, pageableJob.getCallingAET()));
        }
        String prior = sessionAttr.getString(Tags.PrintPriority);
        synchronized (queueMonitor) {
            if ("MED".equals(prior)) {
                medPriorQueue.add(pageableJob);
            } else if ("HIGH".equals(prior)) {
                highPriorQueue.add(pageableJob);
            } else {
                lowPriorQueue.add(pageableJob);
            }
            queueMonitor.notify();
        }
    }


    // Runnable implementation -----------------------------------
    /**  Main processing method for the PrinterService object */
    public void run()
    {
        log.info("Scheduler Started");
        while (scheduler != null) {
            try {
                ScheduledJob job;
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


    private int getQueuedJobCount()
        throws PrintException
    {
        PrintService ps = getPrintService();
        QueuedJobCount qjc = (QueuedJobCount) ps.getAttribute(QueuedJobCount.class);
        if (qjc == null) {
            return 0;
        }
        return qjc.getValue();
    }


    private boolean isPrinterIsAcceptingJobs()
        throws PrintException
    {
        PrintService ps = getPrintService();
        PrinterIsAcceptingJobs piaj =
                (PrinterIsAcceptingJobs) ps.getAttribute(PrinterIsAcceptingJobs.class);
        if (piaj == null) {
            return true;
        }
        return piaj == PrinterIsAcceptingJobs.ACCEPTING_JOBS;
    }


    private ScheduledJob nextJobFromQueue()
    {
        if (!highPriorQueue.isEmpty()) {
            return (ScheduledJob) highPriorQueue.removeFirst();
        }
        if (!medPriorQueue.isEmpty()) {
            return (ScheduledJob) medPriorQueue.removeFirst();
        }
        if (!lowPriorQueue.isEmpty()) {
            return (ScheduledJob) lowPriorQueue.removeFirst();
        }
        return null;
    }


    private void processJob(ScheduledJob scheduledJob)
    {
        try {
            synchronized (printerMonitor) {
                while (getQueuedJobCount() > maxQueuedJobCount) {
                    log.info("Maximal number of Printer Job reached - "
                             + getQueuedJobCount() + " > " + maxQueuedJobCount);
                    printerMonitor.wait();
                }
            }
            invokeOnPrintSCP("onJobStartPrinting", scheduledJob.getJob());
            scheduledJob.initFilmBoxes(this);
            PrintService ps = getPrintService();
            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            String resId = scheduledJob.getRequestedResolutionID();
            setPrintRequestAttribute(ps,
                    resId != null
                     ? (PrinterResolution) this.resolutionIDMap.get(resId)
                     : getDefaultPrinterResolution(),
                    aset);
            setPrintRequestAttribute(ps,
                    scheduledJob.isColor() || printGrayAsColor
                     ? Chromaticity.COLOR
                     : Chromaticity.MONOCHROME,
                    aset);
            int copies = scheduledJob.getCopies();
            if (copies > 1) {
                setPrintRequestAttribute(ps, new Copies(copies), aset);
                setPrintRequestAttribute(ps, SheetCollate.COLLATED, aset);
            }
            setPrintRequestAttribute(ps,
                    new JobName(scheduledJob.getName(), null), aset);
            print(scheduledJob, aset, autoCalibration);
            log.info("Finished processing job - " + scheduledJob.getJobID());
            try {
                invokeOnPrintSCP("onJobDone", scheduledJob.getJob());
            } catch (Exception ignore) {}
        } catch (Throwable e) {
            log.error("Failed processing job - " + scheduledJob.getJobID(), e);
            try {
                invokeOnPrintSCP("onJobFailed", scheduledJob.getJob());
            } catch (Throwable ignore) {}
        }
    }


    // PrintServiceAttributeListener implementation -------------------------
    static String toMsg(String prompt, AttributeSet set)
    {
        return prompt + Arrays.asList(toStringArray(set));
    }


    static String toMsg(String prompt, PrintJobEvent pje)
    {
        return toMsg(prompt, pje.getPrintJob().getAttributes());
    }


    /**
     *  Description of the Method
     *
     * @param  psae  Description of the Parameter
     */
    public void attributeUpdate(PrintServiceAttributeEvent psae)
    {
        if (log.isDebugEnabled()) {
            log.debug(toMsg("printServiceAttributeUpdate: ", psae.getAttributes()));
        }
        synchronized (printerMonitor) {
            printerMonitor.notify();
        }
    }


    // PrintJobAttributeListener implementation -------------------------
    /**
     *  Description of the Method
     *
     * @param  pjae  Description of the Parameter
     */
    public void attributeUpdate(PrintJobAttributeEvent pjae)
    {
        if (log.isDebugEnabled()) {
            log.debug(toMsg("printJobAttributeUpdate: ", pjae.getAttributes()));
        }
    }


    // PrintJobListener implementation -------------------------
    /**
     *  Description of the Method
     *
     * @param  pje  Description of the Parameter
     */
    public void printDataTransferCompleted(PrintJobEvent pje)
    {
        if (log.isDebugEnabled()) {
            log.debug(toMsg("printDataTransferCompleted: ", pje));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  pje  Description of the Parameter
     */
    public void printJobCanceled(PrintJobEvent pje)
    {
        if (log.isDebugEnabled()) {
            log.debug(toMsg("printJobCanceled: ", pje));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  pje  Description of the Parameter
     */
    public void printJobCompleted(PrintJobEvent pje)
    {
        if (log.isDebugEnabled()) {
            log.debug(toMsg("printJobCompleted: ", pje));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  pje  Description of the Parameter
     */
    public void printJobFailed(PrintJobEvent pje)
    {
        if (log.isDebugEnabled()) {
            log.info(toMsg("printJobFailed: ", pje));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  pje  Description of the Parameter
     */
    public void printJobNoMoreEvents(PrintJobEvent pje)
    {
        if (log.isDebugEnabled()) {
            log.debug(toMsg("printJobNoMoreEvents: ", pje));
        }
    }


    /**
     *  Description of the Method
     *
     * @param  pje  Description of the Parameter
     */
    public void printJobRequiresAttention(PrintJobEvent pje)
    {
        if (log.isDebugEnabled()) {
            log.debug(toMsg("printJobRequiresAttention: ", pje));
        }
    }


    // Package protected ---------------------------------------------
    static File toFile(String path)
    {
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
            PrintRequestAttributeSet aset)
    {
        if (ps.isAttributeValueSupported(attr,
                DocFlavor.SERVICE_FORMATTED.PAGEABLE, aset)) {
            aset.add(attr);
        } else {
            log.warn("Attribute " + attr + " not supported by printer "
                     + printerName);
        }
    }


    private void print(Pageable printData, PrintRequestAttributeSet aset,
            boolean calibrate)
        throws PrintException
    {
        PrintService ps = getPrintService();
        if (calibrate) {
            try {
                calibrate(false);
                calibrationErr = false;
            } catch (CalibrationException e) {
                calibrationErr = true;
                try {
                    calibration.setODs(scanner.readODs(odFile));
                    calibration.setODsTS(odFile.lastModified());
                } catch (Exception e2) {
                    throw new PrintException("Calibration fails", e2);
                }
                log.warn("Calibration fails, continue printing", e);
            }
        }

        if (printToFile) {
            setPrintRequestAttribute(ps,
                    new Destination(toFile(printToFilePath).toURI()), aset);
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
}

