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
 */
package com.tiani.prnscp.print;

import java.io.IOException;
import javax.management.ObjectName;
import javax.print.PrintException;

import org.dcm4che.data.Dataset;
import org.jboss.system.ServiceMBean;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 30, 2003
 * @created    November 3, 2003
 * @version    $Revision$
 */
public interface PrinterServiceMBean extends ServiceMBean
{
    String OBJECT_NAME_PREFIX = "dcm4chex:service=Printer,calledAET=";


    /**
     *  Getter for property printSCP.
     *
     * @return    Value of property printSCP.
     */
    public ObjectName getPrintSCP();


    /**
     *  Setter for property printSCP.
     *
     * @param  printSCP  New value of property printSCP.
     */
    public void setPrintSCP(ObjectName printSCP);


    /**
     *  Getter for property availablePrinters.
     *
     * @return    Value of property availablePrinters.
     */
    public String[] getAvailablePrinters();


    /**
     *  Getter for property printerName.
     *
     * @return    Value of property printerName.
     */
    public String getPrinterName();


    /**
     *  Setter for property printerName.
     *
     * @param  printerName  The new printerName value
     */
    public void setPrinterName(String printerName);


    /**
     *  Getter for property manufacturer.
     *
     * @return    Value of property manufacturer.
     */
    public String getManufacturer();


    /**
     *  Setter for property manufacturer.
     *
     * @param  manufacturer  New value of property manufacturer.
     */
    public void setManufacturer(String manufacturer);


    /**
     *  Getter for property manufacturerModelName.
     *
     * @return    Value of property manufacturerModelName.
     */
    public String getManufacturerModelName();


    /**
     *  Setter for property manufacturerModelName.
     *
     * @param  manufacturerModelName  New value of property
     *      manufacturerModelName.
     */
    public void setManufacturerModelName(String manufacturerModelName);


    /**
     *  Getter for property deviceSerialNumber.
     *
     * @return    Value of property deviceSerialNumber.
     */
    public String getDeviceSerialNumber();


    /**
     *  Getter for property softwareVersion.
     *
     * @return    Value of property softwareVersion.
     */
    public String getSoftwareVersion();


    /**
     *  Setter for property softwareVersion.
     *
     * @param  softwareVersion  New value of property softwareVersion.
     */
    public void setSoftwareVersion(String softwareVersion);


    /**
     *  Setter for property deviceSerialNumber.
     *
     * @param  deviceSerialNumber  New value of property deviceSerialNumber.
     */
    public void setDeviceSerialNumber(String deviceSerialNumber);


    /**
     *  Getter for property supportedAttributeValues.
     *
     * @return    Value of property supportedAttributeValues.
     */
    public String[] getSupportedAttributeValues();


    /**
     *  Getter for property printServiceAttributes.
     *
     * @return    Value of property printServiceAttributes.
     */
    public String[] getPrintServiceAttributes();


    /**
     *  Getter for property printToFile.
     *
     * @return    Value of property printToFile.
     */
    public String getPrintToFilePath();


    /**
     *  Setter for property printToFile.
     *
     * @param  printToFile  New value of property printToFile.
     */
    public void setPrintToFilePath(String printToFile);


    /**
     *  Getter for property printToFile.
     *
     * @return    Value of property printToFile.
     */
    public boolean isPrintToFile();


    /**
     *  Setter for property printToFile.
     *
     * @param  printToFile  New value of property printToFile.
     */
    public void setPrintToFile(boolean printToFile);


    /**
     *  Getter for property supportsColor.
     *
     * @return    Value of property supportsColor.
     */
    public boolean isSupportsColor();


    /**
     *  Setter for property supportsColor.
     *
     * @param  supportsColor  New value of property supportsColor.
     */
    public void setSupportsColor(boolean supportsColor);


    /**
     *  Getter for property supportsPresentationLUT.
     *
     * @return    Value of property supportsPresentationLUT.
     */
    public boolean isSupportsPresentationLUT();


    /**
     *  Setter for property supportsPresentationLUT.
     *
     * @param  supportsPresentationLUT  New value of property
     *      supportsPresentationLUT.
     */
    public void setSupportsPresentationLUT(boolean supportsPresentationLUT);


    /**
     *  Getter for property supportsAnnotationBox.
     *
     * @return    Value of property supportsAnnotationBox.
     */
    public boolean isSupportsAnnotationBox();


    /**
     *  Setter for property supportsAnnotationBox.
     *
     * @param  supportsAnnotationBox  New value of property
     *      supportsAnnotationBox.
     */
    public void setSupportsAnnotationBox(boolean supportsAnnotationBox);


    /**
     *  Gets the dateFormat attribute of the PrinterServiceMBean object
     *
     * @return    The dateFormat value
     */
    public String getDateFormat();


    /**
     *  Sets the dateFormat attribute of the PrinterService object
     *
     * @param  dateFormat  The new dateFormat value
     */
    public void setDateFormat(String dateFormat);


    /**
     *  Gets the timeFormat attribute of the PrinterService object
     *
     * @return    The timeFormat value
     */
    public String getTimeFormat();


    /**
     *  Sets the timeFormat attribute of the PrinterService object
     *
     * @param  timeFormat  The new timeFormat value
     */
    public void setTimeFormat(String timeFormat);


    /**
     *  Gets the sessionLabel attribute of the PrinterServiceMBean object
     *
     * @return    The sessionLabel value
     */
    public String getSessionLabel();


    /**
     *  Sets the sessionLabel attribute of the PrinterServiceMBean object
     *
     * @param  sessionLabel  The new sessionLabel value
     */
    public void setSessionLabel(String sessionLabel);


    /**
     *  Gets the maxNumberOfCopies attribute of the PrinterServiceMBean object
     *
     * @return    The maxNumberOfCopies value
     */
    public int getMaxNumberOfCopies();


    /**
     *  Sets the maxNumberOfCopies attribute of the PrinterServiceMBean object
     *
     * @param  maxNumberOfCopies  The new maxNumberOfCopies value
     */
    public void setMaxNumberOfCopies(int maxNumberOfCopies);


    /**
     *  Getter for property mediaType.
     *
     * @return    Value of property mediaType.
     */
    public String getMediumType();


    /**
     *  Setter for property mediaType.
     *
     * @param  mediaType  New value of property mediaType.
     */
    public void setMediumType(String mediaType);


    /**
     *  Getter for property reverseLandscap.
     *
     * @return    Value of property reverseLandscap.
     */
    public boolean isReverseLandscape();


    /**
     *  Setter for property reverseLandscape.
     *
     * @param  reverseLandscape  New value of property reverseLandscape.
     */
    public void setReverseLandscape(boolean reverseLandscape);


    /**
     *  Getter for property defaultLandscapee.
     *
     * @return    Value of property defaultLandscape.
     */
    public boolean isDefaultPortrait();


    /**
     *  Setter for property defaultLandscape.
     *
     * @param  defaultLandscape  New value of property defaultLandscape.
     */
    public void setDefaultPortrait(boolean defaultLandscape);


    /**
     *  Getter for property defaultFilmOrientation.
     *
     * @return    Value of property defaultFilmOrientation.
     */
    public String getDefaultFilmOrientation();


    /**
     *  Getter for property displayFormat.
     *
     * @return    Value of property displayFormat.
     */
    public String getDisplayFormat();


    /**
     *  Setter for property displayFormat.
     *
     * @param  displayFormat  New value of property displayFormat.
     */
    public void setDisplayFormat(String displayFormat);


    /**
     *  Getter for property filmSizeID.
     *
     * @return    Value of property filmSizeID.
     */
    public String getFilmSizeID();


    /**
     *  Setter for property filmSizeID.
     *
     * @param  filmSizeID  New value of property filmSizeID.
     */
    public void setFilmSizeID(String filmSizeID);


    /**
     *  Getter for property resolutionID.
     *
     * @return    Value of property resolutionID.
     */
    public String getResolutionID();


    /**
     *  Setter for property resolutionID.
     *
     * @param  resolutionID  New value of property resolutionID.
     */
    public void setResolutionID(String resolutionID);


    /**
     *  Getter for property magnificationType.
     *
     * @return    Value of property magnificationType.
     */
    public String getMagnificationType();


    /**
     *  Setter for property magnificationType.
     *
     * @param  magnificationType  New value of property magnificationType.
     */
    public void setMagnificationType(String magnificationType);


    /**
     *  Getter for property decimateCropBehavior.
     *
     * @return    Value of property decimateCropBehavior.
     */
    public String getDecimateCropBehavior();


    /**
     *  Setter for property decimateCropBehavior.
     *
     * @param  decimateCropBehavior  New value of property decimateCropBehavior.
     */
    public void setDecimateCropBehavior(String decimateCropBehavior);


    /**
     *  Getter for property borderDensity.
     *
     * @return    Value of property borderDensity.
     */
    public String getBorderDensity();


    /**
     *  Setter for property borderDensity.
     *
     * @param  borderDensity  New value of property borderDensity.
     */
    public void setBorderDensity(String borderDensity);


    /**
     *  Gets the trim attribute of the PrinterService object
     *
     * @return    The trim value
     */
    public String getTrim();


    /**
     *  Sets the trim attribute of the PrinterService object
     *
     * @param  trim  The new trim value
     */
    public void setTrim(String trim);


    /**
     *  Getter for property minDensity.
     *
     * @return    Value of property minDensity.
     */
    public int getMinDensity();


    /**
     *  Getter for property maxDensity.
     *
     * @return    Value of property maxDensity.
     */
    public int getMaxDensity();


    /**
     *  Getter for property borderThickness.
     *
     * @return    Value of property borderThickness.
     */
    public float getBorderThickness();


    /**
     *  Setter for property borderThickness.
     *
     * @param  borderThickness  New value of property borderThickness.
     */
    public void setBorderThickness(float borderThickness);


    /**
     *  Getter for property margin.
     *
     * @return    Value of property margin.
     */
    public String getPageMargin();


    /**
     *  Setter for property margin.
     *
     * @param  margin  New value of property margin.
     */
    public void setPageMargin(String margin);


    /**
     *  Getter for property illumination.
     *
     * @return    Value of property illumination.
     */
    public int getIllumination();


    /**
     *  Setter for property illumination.
     *
     * @param  illumination  New value of property illumination.
     */
    public void setIllumination(int illumination);


    /**
     *  Getter for property reflectedAmbientLight.
     *
     * @return    Value of property reflectedAmbientLight.
     */
    public int getReflectedAmbientLight();


    /**
     *  Setter for property reflectedAmbientLight.
     *
     * @param  reflectedAmbientLight  New value of property
     *      reflectedAmbientLight.
     */
    public void setReflectedAmbientLight(int reflectedAmbientLight);


    /**
     *  Getter for property defaultMediumType.
     *
     * @return    Value of property defaultMediumType.
     */
    public String getDefaultMediumType();


    /**
     *  Getter for property defaultMagnificationType.
     *
     * @return    Value of property defaultMagnificationType.
     */
    public String getDefaultMagnificationType();


    /**
     *  Getter for property defaultResolutionID.
     *
     * @return    Value of property defaultResolutionID.
     */
    public String getDefaultResolutionID();


    /**
     *  Getter for code string for property status.
     *
     * @return    String value of property status.
     */
    PrinterStatus getStatus();


    /**
     *  Getter for property statusInfo.
     *
     * @return    Value of property statusInfo.
     */
    PrinterStatusInfo getStatusInfo();


    /**
     *  Getter for property measuredODs.
     *
     * @return    Value of property measuredODs.
     */
    public float[] getGrayscaleODs();


    /**
     *  Setter for property measuredODs.
     *
     * @param  measuredODs  New value of property measuredODs.
     */
    public void setGrayscaleODs(float[] measuredODs);


    /**
     *  Setter for property measuredODsAsText.
     *
     * @param  measuredODsAsText  New value of property measuredODsAsText.
     */
    public void setGrayscaleODsAsText(String measuredODsAsText);


    /**
     *  Getter for property refGrayscaleODs.
     *
     * @return    Value of property refGrayscaleODs.
     */
    public float[] getRefGrayscaleODs();


    /**
     *  Setter for property refGrayscaleODs.
     *
     * @param  refGrayscaleODs  New value of property refGrayscaleODs.
     */
    public void setRefGrayscaleODs(float[] refGrayscaleODs);


    /**
     *  Setter for property refGrayscaleODsAsText.
     *
     * @param  refGrayscaleODsAsText  New value of property
     *      refGrayscaleODsAsText.
     */
    public void setRefGrayscaleODsAsText(String refGrayscaleODsAsText);


    /**
     *  Getter for property scanGrayscaleDir.
     *
     * @return    Value of property scanGrayscaleDir.
     */
    public String getCalibrationDir();


    /**
     *  Setter for property scanGrayscaleDir.
     *
     * @param  scanGrayscaleDir  New value of property scanGrayscaleDir.
     */
    public void setCalibrationDir(String scanGrayscaleDir);


    /**
     *  Getter for property refFileName.
     *
     * @return    Value of property refFileName.
     */
    public String getRefGrayscaleFileName();


    /**
     *  Setter for property refFileName.
     *
     * @param  refFileName  New value of property refFileName.
     */
    public void setRefGrayscaleFileName(String refFileName);


    /**
     *  Getter for property scanPointExtension.
     *
     * @return    Value of property scanPointExtension.
     */
    public int getScanPointExtension();


    /**
     *  Setter for property scanPointExtension.
     *
     * @param  extension  New value of property scanPointExtension.
     */
    public void setScanPointExtension(int extension);


    /**
     *  Getter for property scanGradientThreshold.
     *
     * @return    Value of property scanGradientThreshold.
     */
    public String getScanThreshold();


    /**
     *  Setter for property scanGradientThreshold.
     *
     * @param  scanGradientThreshold  New value of property
     *      scanGradientThreshold.
     */
    public void setScanThreshold(String scanGradientThreshold);


    /**
     *  Getter for property annotationDir.
     *
     * @return    Value of property annotationDir.
     */
    public String getAnnotationDir();


    /**
     *  Setter for property annotationDir.
     *
     * @param  annotationDir  New value of property annotationDir.
     */
    public void setAnnotationDir(String annotationDir);


    /**
     *  Getter for property lutDir.
     *
     * @return    Value of property lutDir.
     */
    public String getLUTDir();


    /**
     *  Setter for property lutDir.
     *
     * @param  lutDir  New value of property lutDir.
     */
    public void setLUTDir(String lutDir);


    /**
     *  Getter for property annotationDisplayFormatIDs.
     *
     * @return    Value of property annotationDisplayFormatIDs.
     */
    public String[] getAnnotationDisplayFormatIDs();


    /**
     *  Getter for property LUTs.
     *
     * @return    Value of property LUTs.
     */
    public String[] getLUTs();


    /**
     *  Getter for property lutForCallingAET.
     *
     * @return    Value of property lutForCallingAET.
     */
    public String getLUTForCallingAET();


    /**
     *  Setter for property lutForCallingAET.
     *
     * @param  lutForCallingAET  New value of property lutForCallingAET.
     */
    public void setLUTForCallingAET(String lutForCallingAET);


    /**
     *  Getter for property annotationForCallingAET.
     *
     * @return    Value of property annotationForCallingAET.
     */
    public String getAnnotationForCallingAET();


    /**
     *  Setter for property annotationForCallingAET.
     *
     * @param  annotationForCallingAET  New value of property
     *      annotationForCallingAET.
     */
    public void setAnnotationForCallingAET(String annotationForCallingAET);


    /**
     *  Getter for property grayscaleAnnotation.
     *
     * @return    Value of property grayscaleAnnotation.
     */
    public String getGrayscaleAnnotation();


    /**
     *  Setter for property grayscaleAnnotation.
     *
     * @param  grayscaleAnnotation  New value of property grayscaleAnnotation.
     */
    public void setGrayscaleAnnotation(String grayscaleAnnotation);


    /**
     *  Gets the supportsFilmSizeID attribute of the PrinterServiceMBean object
     *
     * @param  filmSizeID  Description of the Parameter
     * @return             The supportsFilmSizeID value
     */
    public boolean isSupportsFilmSizeID(String filmSizeID);


    /**
     *  Gets the supportsDisplayFormat attribute of the PrinterServiceMBean
     *  object
     *
     * @param  displayFormat    Description of the Parameter
     * @param  filmOrientation  Description of the Parameter
     * @return                  The supportsDisplayFormat value
     */
    public boolean isSupportsDisplayFormat(String displayFormat, String filmOrientation);


    /**
     *  Gets the supportsMagnificationType attribute of the PrinterServiceMBean
     *  object
     *
     * @param  magnificationType  Description of the Parameter
     * @return                    The supportsMagnificationType value
     */
    public boolean isSupportsMagnificationType(String magnificationType);


    /**
     *  Gets the supportsMediumType attribute of the PrinterServiceMBean object
     *
     * @param  mediumType  Description of the Parameter
     * @return             The supportsMediumType value
     */
    public boolean isSupportsMediumType(String mediumType);


    /**
     *  Gets the supportsResolutionID attribute of the PrinterServiceMBean
     *  object
     *
     * @param  resolutionID  Description of the Parameter
     * @return               The supportsResolutionID value
     */
    public boolean isSupportsResolutionID(String resolutionID);


    /**
     *  Gets the supportsConfigurationInformation attribute of the
     *  PrinterServiceMBean object
     *
     * @param  configInfo  Description of the Parameter
     * @return             The supportsConfigurationInformation value
     */
    public boolean isSupportsConfigurationInformation(String configInfo);


    /**
     *  Gets the supportsAnnotationDisplayFormatID attribute of the
     *  PrinterServiceMBean object
     *
     * @param  annotationID  Description of the Parameter
     * @return               The supportsAnnotationDisplayFormatID value
     */
    public boolean isSupportsAnnotationDisplayFormatID(String annotationID);


    /**
     *  Description of the Method
     *
     * @param  annotationID  Description of the Parameter
     * @return               Description of the Return Value
     */
    public int countAnnotationBoxes(String annotationID);


    /**
     *  Getter for property dateOfLastCalibration.
     *
     * @return    Value of property dateOfLastCalibration.
     */
    public String getDateOfLastCalibration();


    /**
     *  Setter for property dateOfLastCalibration.
     *
     * @param  dateOfLastCalibration  New value of property
     *      dateOfLastCalibration.
     */
    public void setDateOfLastCalibration(String dateOfLastCalibration);


    /**
     *  Getter for property timeOfLastCalibration.
     *
     * @return    Value of property timeOfLastCalibration.
     */
    public String getTimeOfLastCalibration();


    /**
     *  Setter for property timeOfLastCalibration.
     *
     * @param  timeOfLastCalibration  New value of property
     *      timeOfLastCalibration.
     */
    public void setTimeOfLastCalibration(String timeOfLastCalibration);


    /**
     *  Getter for property autoCalibration.
     *
     * @return    Value of property autoCalibration.
     */
    public boolean isAutoCalibration();


    /**
     *  Setter for property autoCalibration.
     *
     * @param  autoCalibration  New value of property autoCalibration.
     */
    public void setAutoCalibration(boolean autoCalibration);


    /**
     *  Getter for property printGrayAsColor.
     *
     * @return    Value of property printGrayAsColor.
     */
    public boolean isPrintGrayAsColor();


    /**
     *  Setter for property printGrayAsColor.
     *
     * @param  printGrayAsColor  New value of property printGrayAsColor.
     */
    public void setPrintGrayAsColor(boolean printGrayAsColor);


    /**
     *  Getter for property maxQueuedJobCount.
     *
     * @return    Value of property maxQueuedJobCount.
     */
    public int getMaxQueuedJobCount();


    /**
     *  Setter for property maxQueuedJobCount.
     *
     * @param  maxQueuedJobCount  New value of property maxQueuedJobCount.
     */
    public void setMaxQueuedJobCount(int maxQueuedJobCount);


    /**
     *  Getter for property ignorePrinterIsAcceptingJobs.
     *
     * @return    Value of property ignorePrinterIsAcceptingJobs.
     */
    public boolean isIgnorePrinterIsAcceptingJobs();


    /**
     *  Setter for property ignorePrinterIsAcceptingJobs.
     *
     * @param  ignorePrinterIsAcceptingJobs  New value of property
     *      ignorePrinterIsAcceptingJobs.
     */
    public void setIgnorePrinterIsAcceptingJobs(boolean ignorePrinterIsAcceptingJobs);


    /**
     *  Getter for property supportsMonochrome.
     *
     * @return    Value of property supportsMonochrome.
     */
    public boolean isSupportsGrayscale();


    /**
     *  Setter for property supportsMonochrome.
     *
     * @param  supportsMonochrome  New value of property supportsMonochrome.
     */
    public void setSupportsGrayscale(boolean supportsMonochrome);


    /**
     *  Getter for property chunkSize.
     *
     * @return    Value of property chunkSize.
     */
    public double getChunkSize();


    /**
     *  Setter for property chunkSize.
     *
     * @param  chunkSize  New value of property chunkSize.
     */
    public void setChunkSize(double chunkSize);


    /**
     *  Getter for property minimizeJobsize.
     *
     * @return    Value of property minimizeJobsize.
     */
    public boolean isMinimizeJobsize();


    /**
     *  Setter for property minimizeJobsize.
     *
     * @param  minimizeJobsize  New value of property minimizeJobsize.
     */
    public void setMinimizeJobsize(boolean minimizeJobsize);


    /**
     *  Getter for property decimateByNearestNeighbor.
     *
     * @return    Value of property decimateByNearestNeighbor.
     */
    public boolean isDecimateByNearestNeighbor();


    /**
     *  Setter for property decimateByNearestNeighbor.
     *
     * @param  decimateByNearestNeighbor  New value of property
     *      decimateByNearestNeighbor.
     */
    public void setDecimateByNearestNeighbor(boolean decimateByNearestNeighbor);


    /**
     *  Description of the Method
     *
     * @param  force                     Description of the Parameter
     * @exception  CalibrationException  Description of the Exception
     */
    public void calibrate(boolean force)
        throws CalibrationException;


    /**
     *  Description of the Method
     *
     * @param  fname               Description of the Parameter
     * @param  configInfo          Description of the Parameter
     * @exception  IOException     Description of the Exception
     * @exception  PrintException  Description of the Exception
     */
    public void printImage(String fname, String configInfo)
        throws IOException, PrintException;


    /**
     *  Description of the Method
     *
     * @param  color        Description of the Parameter
     * @param  job          Description of the Parameter
     * @param  sessionAttr  Description of the Parameter
     */
    public void scheduleJob(Boolean color, String job, Dataset sessionAttr);
}

