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
import org.jboss.system.ServiceMBean;

import javax.management.ObjectName;
import javax.print.PrintException;

import java.io.IOException;

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
public interface PrinterServiceMBean extends ServiceMBean {
   
   String NOTIF_PENDING = "tiani.prnscp.pending";
   String NOTIF_PRINTING = "tiani.prnscp.printing";
   String NOTIF_DONE = "tiani.prnscp.done";
   String NOTIF_FAILURE = "tiani.prnscp.failure";
   
   int NORMAL  = 1;
   int WARNING = 2;
   int FAILURE = 3;
      
   /** Getter for property availableDestinations.
    * @return Value of property availableDestinations.
    */
   public String[] getAvailablePrinters();
   
   /** Getter for property destination.
    * @return Value of property destination.
    */
   public String getPrinterName();
   
   /** Setter for property destination.
    * @param destination New value of property destination.
    */
   public void setPrinterName(String destination);
   
   /** Getter for property supportedAttributeValues.
    * @return Value of property supportedAttributeValues.
    */
   public String[] getSupportedAttributeValues();
   
   /** Getter for property printServiceAttributes.
    * @return Value of property printServiceAttributes.
    */
   public String[] getPrintServiceAttributes();
   
   /** Getter for property printToFile.
    * @return Value of property printToFile.
    */
   public String getPrintToFilePath();
   
   /** Setter for property printToFile.
    * @param printToFile New value of property printToFile.
    */
   public void setPrintToFilePath(String printToFile);
   
   /** Getter for property printToFile.
    * @return Value of property printToFile.
    */
   public boolean isPrintToFile();
   
   /** Setter for property printToFile.
    * @param printToFile New value of property printToFile.
    */
   public void setPrintToFile(boolean printToFile);
   
   /** Getter for property supportsColor.
    * @return Value of property supportsColor.
    */
   public boolean isSupportsColor();
   
   /** Setter for property supportsColor.
    * @param supportsColor New value of property supportsColor.
    */
   public void setSupportsColor(boolean supportsColor);
   
   /** Getter for property supportsPresentationLUT.
    * @return Value of property supportsPresentationLUT.
    */
   public boolean isSupportsPresentationLUT();
   
   /** Setter for property supportsPresentationLUT.
    * @param supportsPresentationLUT New value of property supportsPresentationLUT.
    */
   public void setSupportsPresentationLUT(boolean supportsPresentationLUT);
   
   /** Getter for property supportsAnnotationBox.
    * @return Value of property supportsAnnotationBox.
    */
   public boolean isSupportsAnnotationBox();
   
   /** Setter for property supportsAnnotationBox.
    * @param supportsAnnotationBox New value of property supportsAnnotationBox.
    */
   public void setSupportsAnnotationBox(boolean supportsAnnotationBox);
   
   /** Getter for property mediaType.
    * @return Value of property mediaType.
    */
   public String getMediumType();
   
   /** Setter for property mediaType.
    * @param mediaType New value of property mediaType.
    */
   public void setMediumType(String mediaType);
   
   /** Getter for property filmDestination.
    * @return Value of property filmDestination.
    */
   public String getFilmDestination();
   
   /** Setter for property filmDestination.
    * @param filmDestination New value of property filmDestination.
    */
   public void setFilmDestination(String filmDestination);
   
   /** Getter for property displayFormat.
    * @return Value of property displayFormat.
    */
   public String getDisplayFormat();
   
   /** Setter for property displayFormat.
    * @param displayFormat New value of property displayFormat.
    */
   public void setDisplayFormat(String displayFormat);
   
   /** Getter for property filmOrientation.
    * @return Value of property filmOrientation.
    */
   public String getFilmOrientation();
   
   /** Setter for property filmOrientation.
    * @param filmOrientation New value of property filmOrientation.
    */
   public void setFilmOrientation(String filmOrientation);
   
   /** Getter for property filmSizeID.
    * @return Value of property filmSizeID.
    */
   public String getFilmSizeID();
   
   /** Setter for property filmSizeID.
    * @param filmSizeID New value of property filmSizeID.
    */
   public void setFilmSizeID(String filmSizeID);
   
   /** Getter for property resolutionID.
    * @return Value of property resolutionID.
    */
   public String getResolutionID();
   
   /** Setter for property resolutionID.
    * @param resolutionID New value of property resolutionID.
    */
   public void setResolutionID(String resolutionID);
   
   /** Getter for property resolution.
    * @return Value of property resolution.
    */
   public String getResolution();
   
   /** Setter for property resolution.
    * @param resolution New value of property resolution.
    */
   public void setResolution(String resolution);
      
   /** Getter for property magnificationType.
    * @return Value of property magnificationType.
    */
   public String getMagnificationType();
   
   /** Setter for property magnificationType.
    * @param magnificationType New value of property magnificationType.
    */
   public void setMagnificationType(String magnificationType);
   
   /** Getter for property smoothingType.
    * @return Value of property smoothingType.
    */
   public String getSmoothingType();
   
   /** Setter for property smoothingType.
    * @param smoothingType New value of property smoothingType.
    */
   public void setSmoothingType(String smoothingType);
   
   /** Getter for property decimateCropBehavior.
    * @return Value of property decimateCropBehavior.
    */
   public String getDecimateCropBehavior();
   
   /** Setter for property decimateCropBehavior.
    * @param decimateCropBehavior New value of property decimateCropBehavior.
    */
   public void setDecimateCropBehavior(String decimateCropBehavior);
   
   /** Getter for property borderDensity.
    * @return Value of property borderDensity.
    */
   public String getBorderDensity();
   
   /** Setter for property borderDensity.
    * @param borderDensity New value of property borderDensity.
    */
   public void setBorderDensity(String borderDensity);
   
   /** Getter for property emptyImageDensity.
    * @return Value of property emptyImageDensity.
    */
   public String getEmptyImageDensity();
   
   /** Setter for property emptyImageDensity.
    * @param emptyImageDensity New value of property emptyImageDensity.
    */
   public void setEmptyImageDensity(String emptyImageDensity);
   
   /** Getter for property minDensity.
    * @return Value of property minDensity.
    */
   public int getMinDensity();
   
   /** Setter for property minDensity.
    * @param minDensity New value of property minDensity.
    */
   public void setMinDensity(int minDensity);
   
   /** Getter for property maxDensity.
    * @return Value of property maxDensity.
    */
   public int getMaxDensity();
   
   /** Setter for property maxDensity.
    * @param maxDensity New value of property maxDensity.
    */
   public void setMaxDensity(int maxDensity);
   
   /** Getter for property borderThickness.
    * @return Value of property borderThickness.
    */
   public float getBorderThickness();
   
   /** Setter for property borderThickness.
    * @param borderThickness New value of property borderThickness.
    */
   public void setBorderThickness(float borderThickness);

   /** Getter for property margin.
    * @return Value of property margin.
    */
   public String getPageMargin();
   
   /** Setter for property margin.
    * @param margin New value of property margin.
    */
   public void setPageMargin(String margin);
   
   /** Getter for property illumination.
    * @return Value of property illumination.
    */
   public int getIllumination();
   
   /** Setter for property illumination.
    * @param illumination New value of property illumination.
    */
   public void setIllumination(int illumination);
   
   /** Getter for property reflectedAmbientLight.
    * @return Value of property reflectedAmbientLight.
    */
   public int getReflectedAmbientLight();
   
   /** Setter for property reflectedAmbientLight.
    * @param reflectedAmbientLight New value of property reflectedAmbientLight.
    */
   public void setReflectedAmbientLight(int reflectedAmbientLight);
   
   /** Getter for property defaultMediumType.
    * @return Value of property defaultMediumType.
    *
    */
   public String getDefaultMediumType();
   
   /** Getter for property defaultFilmDestination.
    * @return Value of property defaultFilmDestination.
    *
    */
   public String getDefaultFilmDestination();
   
   /** Getter for property defaultFilmSizeID.
    * @return Value of property defaultFilmSizeID.
    *
    */
   public String getDefaultFilmSizeID();
   
   /** Getter for property defaultMagnificationType.
    * @return Value of property defaultMagnificationType.
    *
    */
   public String getDefaultMagnificationType();
   
   /** Getter for property defaultSmoothingType.
    * @return Value of property defaultSmoothingType.
    *
    */
   public String getDefaultSmoothingType();
   
   /** Getter for property defaultResolutionID.
    * @return Value of property defaultResolutionID.
    *
    */
   public String getDefaultResolutionID();
   
   /** Getter for property status.
    * @return Value of property status.
    */
   int getStatusID();
   
   /** Getter for code string for property status.
    * @return String value of property status.
    */
   String getStatus();
       
   /** Getter for property statusInfo.
    * @return Value of property statusInfo.
    */
   String getStatusInfo();
   
   /** Getter for property measuredODs.
    * @return Value of property measuredODs.
    */
   public float[] getGrayStepODs();
   
   /** Setter for property measuredODs.
    * @param measuredODs New value of property measuredODs.
    */
   public void setGrayStepODs(float[] measuredODs);
   
   /** Setter for property measuredODsAsText.
    * @param measuredODsAsText New value of property measuredODsAsText.
    */
   public void setGrayStepODsAsText(String measuredODsAsText);
   
   /** Getter for property graySteps.
    * @return Value of property graySteps.
    */
   public int getGraySteps();
   
   /** Setter for property graySteps.
    * @param graySteps New value of property graySteps.
    */
   public void setGraySteps(int graySteps);
   
   /** Getter for property grayStepGap.
    * @return Value of property grayStepGap.
    */
   public double getGrayStepGap();
   
   /** Setter for property grayStepGap.
    * @param grayStepGap New value of property grayStepGap.
    */
   public void setGrayStepGap(double grayStepGap);
   
   /** Getter for property refGrayStepODs.
    * @return Value of property refGrayStepODs.
    */
   public float[] getRefGrayStepODs();
   
   /** Setter for property refGrayStepODs.
    * @param refGrayStepODs New value of property refGrayStepODs.
    */
   public void setRefGrayStepODs(float[] refGrayStepODs);
   
   /** Setter for property refGrayStepODsAsText.
    * @param refGrayStepODsAsText New value of property refGrayStepODsAsText.
    */
   public void setRefGrayStepODsAsText(String refGrayStepODsAsText);
   
   /** Getter for property scanGrayStepDir.
    * @return Value of property scanGrayStepDir.
    */
   public String getCalibrationDir();
   
   /** Setter for property scanGrayStepDir.
    * @param scanGrayStepDir New value of property scanGrayStepDir.
    */
   public void setCalibrationDir(String scanGrayStepDir);
   
   /** Getter for property scanPointExtension.
    * @return Value of property scanPointExtension.
    */
   public String getScanPointExtension();
   
   /** Setter for property scanPointExtension.
    * @param extension New value of property scanPointExtension.
    */
   public void setScanPointExtension(String extension);
   
   /** Getter for property scanGradientThreshold.
    * @return Value of property scanGradientThreshold.
    */
   public String getScanThreshold();
   
   /** Setter for property scanGradientThreshold.
    * @param scanGradientThreshold New value of property scanGradientThreshold.
    */
   public void setScanThreshold(String scanGradientThreshold);
   
   /** Getter for property annotationDir.
    * @return Value of property annotationDir.
    */
   public String getAnnotationDir();
   
   /** Setter for property annotationDir.
    * @param annotationDir New value of property annotationDir.
    */
   public void setAnnotationDir(String annotationDir);
   
   /** Getter for property pLUTDir.
    * @return Value of property pLUTDir.
    */
   public String getPLUTDir();
   
   /** Setter for property pLUTDir.
    * @param pLUTDir New value of property pLUTDir.
    */
   public void setPLUTDir(String pLUTDir);
   
   /** Getter for property annotationDisplayFormatIDs.
    * @return Value of property annotationDisplayFormatIDs.
    */
   public String[] getAnnotationDisplayFormatIDs();
   
   /** Getter for property pLUTs.
    * @return Value of property pLUTs.
    */
   public String[] getPLUTs();

   /** Getter for property defaultPLUT.
    * @return Value of property defaultPLUT.
    */
   public String getDefaultPLUT();
   
   /** Setter for property defaultPLUT.
    * @param defaultPLUT New value of property defaultPLUT.
    */
   public void setDefaultPLUT(String defaultPLUT);
   
   /** Getter for property defaultAnnotation.
    * @return Value of property defaultAnnotation.
    */
   public String getDefaultAnnotation();
   
   /** Setter for property defaultAnnotation.
    * @param defaultAnnotation New value of property defaultAnnotation.
    */
   public void setDefaultAnnotation(String defaultAnnotation);
   
   /** Getter for property grayStepAnnotation.
    * @return Value of property grayStepAnnotation.
    */
   public String getGrayStepAnnotation();
   
   /** Setter for property grayStepAnnotation.
    * @param grayStepAnnotation New value of property grayStepAnnotation.
    */
   public void setGrayStepAnnotation(String grayStepAnnotation);
   
   public boolean isSupportsFilmDestination(String filmDestination);
   
   public boolean isSupportsFilmSizeID(String filmSizeID);

   public boolean isSupportsDisplayFormat(String displayFormat, String filmOrientation);
   
   public boolean isSupportsMagnificationType(String magnificationType);
   
   public boolean isSupportsMediumType(String mediumType);
   
   public boolean isSupportsSmoothingType(String smoothingType);

   public boolean isSupportsResolutionID(String resolutionID);   

   public boolean isSupportsConfigurationInformation(String configInfo);
 
   public int countAnnotationBoxes(String annotationID);
   
   public void printGraySteps() throws PrintException;
   
   public void printGrayStepsWithGSDF() throws PrintException;
   
   public void printGrayStepsWithLinOD() throws PrintException;
   
   public void calibrate(boolean force) throws CalibrationException;            
   
   /** Getter for property refFileName.
    * @return Value of property refFileName.
    *
    */
   public String getRefGrayStepFileName();
   
   /** Setter for property refFileName.
    * @param refFileName New value of property refFileName.
    *
    */
   public void setRefGrayStepFileName(String refFileName);
   
}
