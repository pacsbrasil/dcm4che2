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
   
   String OBJECT_NAME_PREFIX = "dcm4chex:service=Printer,aet=";
   
   int NORMAL  = 1;
   int WARNING = 2;
   int FAILURE = 3;
      
   /** Getter for property printSCP.
    * @return Value of property printSCP.
    */
   public ObjectName getPrintSCP();
   
   /** Setter for property printSCP.
    * @param printSCP New value of property printSCP.
    */
   public void setPrintSCP(ObjectName printSCP);
   
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
   
   /** Getter for property reverseLandscap.
    * @return Value of property reverseLandscap.
    */
   public boolean isReverseLandscape();
   
   /** Setter for property reverseLandscape.
    * @param reverseLandscape New value of property reverseLandscape.
    */
   public void setReverseLandscape(boolean reverseLandscape);
   
   /** Getter for property defaultLandscapee.
    * @return Value of property defaultLandscape.
    */
   public boolean isDefaultPortrait();
   
   /** Setter for property defaultLandscape.
    * @param defaultLandscape New value of property defaultLandscape.
    *
    */
   public void setDefaultPortrait(boolean defaultLandscape);
   
   /** Getter for property defaultFilmOrientation.
    * @return Value of property defaultFilmOrientation.
    */
   public String getDefaultFilmOrientation();
   
   /** Getter for property displayFormat.
    * @return Value of property displayFormat.
    */
   public String getDisplayFormat();
   
   /** Setter for property displayFormat.
    * @param displayFormat New value of property displayFormat.
    */
   public void setDisplayFormat(String displayFormat);
   
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
   
   /** Getter for property trimBoxDensity.
    * @return Value of property trimBoxDensity.
    */
   public String getTrimBoxDensity();
   
   /** Setter for property trimBoxDensity.
    * @param trimBoxDensity New value of property trimBoxDensity.
    */
   public void setTrimBoxDensity(String trimBoxDensity);
   
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
   public float[] getGrayscaleODs();
   
   /** Setter for property measuredODs.
    * @param measuredODs New value of property measuredODs.
    */
   public void setGrayscaleODs(float[] measuredODs);
   
   /** Setter for property measuredODsAsText.
    * @param measuredODsAsText New value of property measuredODsAsText.
    */
   public void setGrayscaleODsAsText(String measuredODsAsText);
   
   /** Getter for property grayscales.
    * @return Value of property grayscales.
    */
   public int getGrayscales();
   
   /** Setter for property grayscales.
    * @param grayscales New value of property grayscales.
    */
   public void setGrayscales(int grayscales);
   
   /** Getter for property grayscaleGap.
    * @return Value of property grayscaleGap.
    */
   public float getGrayscaleGap();
   
   /** Setter for property grayscaleGap.
    * @param grayscaleGap New value of property grayscaleGap.
    */
   public void setGrayscaleGap(float grayscaleGap);
   
   /** Getter for property refGrayscaleODs.
    * @return Value of property refGrayscaleODs.
    */
   public float[] getRefGrayscaleODs();
   
   /** Setter for property refGrayscaleODs.
    * @param refGrayscaleODs New value of property refGrayscaleODs.
    */
   public void setRefGrayscaleODs(float[] refGrayscaleODs);
   
   /** Setter for property refGrayscaleODsAsText.
    * @param refGrayscaleODsAsText New value of property refGrayscaleODsAsText.
    */
   public void setRefGrayscaleODsAsText(String refGrayscaleODsAsText);
   
   /** Getter for property scanGrayscaleDir.
    * @return Value of property scanGrayscaleDir.
    */
   public String getCalibrationDir();
   
   /** Setter for property scanGrayscaleDir.
    * @param scanGrayscaleDir New value of property scanGrayscaleDir.
    */
   public void setCalibrationDir(String scanGrayscaleDir);
   
   /** Getter for property refFileName.
    * @return Value of property refFileName.
    */
   public String getRefGrayscaleFileName();
   
   /** Setter for property refFileName.
    * @param refFileName New value of property refFileName.
    */
   public void setRefGrayscaleFileName(String refFileName);
   
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
   
   /** Getter for property lutDir.
    * @return Value of property lutDir.
    */
   public String getLUTDir();
   
   /** Setter for property lutDir.
    * @param lutDir New value of property lutDir.
    */
   public void setLUTDir(String lutDir);
   
   /** Getter for property annotationDisplayFormatIDs.
    * @return Value of property annotationDisplayFormatIDs.
    */
   public String[] getAnnotationDisplayFormatIDs();
   
   /** Getter for property LUTs.
    * @return Value of property LUTs.
    */
   public String[] getLUTs();

   /** Getter for property defaultLUT.
    * @return Value of property defaultLUT.
    */
   public String getDefaultLUT();
   
   /** Setter for property defaultLUT.
    * @param defaultLUT New value of property defaultLUT.
    */
   public void setDefaultLUT(String defaultLUT);
   
   /** Getter for property defaultAnnotation.
    * @return Value of property defaultAnnotation.
    */
   public String getDefaultAnnotation();
   
   /** Setter for property defaultAnnotation.
    * @param defaultAnnotation New value of property defaultAnnotation.
    */
   public void setDefaultAnnotation(String defaultAnnotation);
   
   /** Getter for property grayscaleAnnotation.
    * @return Value of property grayscaleAnnotation.
    */
   public String getGrayscaleAnnotation();
   
   /** Setter for property grayscaleAnnotation.
    * @param grayscaleAnnotation New value of property grayscaleAnnotation.
    */
   public void setGrayscaleAnnotation(String grayscaleAnnotation);
   
   public boolean isSupportsFilmDestination(String filmDestination);
   
   public boolean isSupportsFilmSizeID(String filmSizeID);

   public boolean isSupportsDisplayFormat(String displayFormat, String filmOrientation);
   
   public boolean isSupportsMagnificationType(String magnificationType);
   
   public boolean isSupportsMediumType(String mediumType);
   
   public boolean isSupportsSmoothingType(String smoothingType);

   public boolean isSupportsResolutionID(String resolutionID);   

   public boolean isSupportsConfigurationInformation(String configInfo);
 
   public int countAnnotationBoxes(String annotationID);
   
   public void printGrayscaleWithLinDDL() throws PrintException, IOException;
   
   public void printGrayscaleWithGSDF() throws PrintException, IOException;
   
   public void printGrayscaleWithLinOD() throws PrintException, IOException;
   
   public void calibrate(boolean force) throws CalibrationException;            
   
   /** Getter for property dateOfLastCalibration.
    * @return Value of property dateOfLastCalibration.
    *
    */
   public String getDateOfLastCalibration();
   
   /** Setter for property dateOfLastCalibration.
    * @param dateOfLastCalibration New value of property dateOfLastCalibration.
    *
    */
   public void setDateOfLastCalibration(String dateOfLastCalibration);
   
   /** Getter for property timeOfLastCalibration.
    * @return Value of property timeOfLastCalibration.
    *
    */
   public String getTimeOfLastCalibration();
   
   /** Setter for property timeOfLastCalibration.
    * @param timeOfLastCalibration New value of property timeOfLastCalibration.
    *
    */
   public void setTimeOfLastCalibration(String timeOfLastCalibration);
   
   /** Getter for property autoCalibration.
    * @return Value of property autoCalibration.
    */
   public boolean isAutoCalibration();
   
   /** Setter for property autoCalibration.
    * @param autoCalibration New value of property autoCalibration.
    */
   public void setAutoCalibration(boolean autoCalibration);
   
   /** Getter for property printGrayscaleAtStartup.
    * @return Value of property printGrayscaleAtStartup.
    */
   public boolean isPrintGrayscaleAtStartup();
   
   /** Setter for property printGrayscaleAtStartup.
    * @param printGrayscaleAtStartup New value of property printGrayscaleAtStartup.
    */
   public void setPrintGrayscaleAtStartup(boolean printGrayscaleAtStartup);
   
   /** Getter for property trimBoxThickness.
    * @return Value of property trimBoxThickness.
    *
    */
   public float getTrimBoxThickness();
   
   /** Setter for property trimBoxThickness.
    * @param trimBoxThickness New value of property trimBoxThickness.
    *
    */
   public void setTrimBoxThickness(float trimBoxThickness);
   
   /** Getter for property colorVis.
    * @return Value of property colorVis.
    *
    */
   public String getColorVis();
   
   /** Setter for property colorVis.
    * @param colorVis New value of property colorVis.
    *
    */
   public void setColorVis(String colorVis);
   
   /** Getter for property colorAllOfPage.
    * @return Value of property colorAllOfPage.
    *
    */
   public String getColorAllOfPage();
   
   /** Setter for property colorAllOfPage.
    * @param colorAllOfPage New value of property colorAllOfPage.
    *
    */
   public void setColorAllOfPage(String colorAllOfPage);
   
   /** Getter for property colorTrimBox.
    * @return Value of property colorTrimBox.
    *
    */
   public String getColorTrimBox();
   
   /** Setter for property colorTrimBox.
    * @param colorTrimBox New value of property colorTrimBox.
    *
    */
   public void setColorTrimBox(String colorTrimBox);
   
   /** Getter for property spaceBetweenVisW.
    * @return Value of property spaceBetweenVisW.
    *
    */
   public float getSpaceBetweenVisW();
   
   /** Setter for property spaceBetweenVisW.
    * @param spaceBetweenVisW New value of property spaceBetweenVisW.
    *
    */
   public void setSpaceBetweenVisW(float spaceBetweenVisW);
   
   /** Getter for property spaceBetweenVisH.
    * @return Value of property spaceBetweenVisH.
    *
    */
   public float getSpaceBetweenVisH();
   
   /** Setter for property spaceBetweenVisH.
    * @param spaceBetweenVisH New value of property spaceBetweenVisH.
    *
    */
   public void setSpaceBetweenVisH(float spaceBetweenVisH);
   
   /** Getter for property useBorderDensForGrid.
    * @return Value of property useBorderDensForGrid.
    *
    */
   public boolean isUseBorderDensForGrid();
   
   /** Setter for property useBorderDensForGrid.
    * @param useBorderDensForGrid New value of property useBorderDensForGrid.
    *
    */
   public void setUseBorderDensForGrid(boolean useBorderDensForGrid);
   
   /** Getter for property puzzleScaleStartSize.
    * @return Value of property puzzleScaleStartSize.
    *
    */
   public int getPuzzleScaleStartSize();
   
   /** Setter for property puzzleScaleStartSize.
    * @param puzzleScaleStartSize New value of property puzzleScaleStartSize.
    *
    */
   public void setPuzzleScaleStartSize(int puzzleScaleStartSize);
   
   /** Getter for property puzzleScalePackageSizeMin.
    * @return Value of property puzzleScalePackageSizeMin.
    *
    */

   
   public String getPuzzleScalePackageSize();
   
   public void setPuzzleScalePackageSize(String s);
   
   /** Getter for property printGrayAsColor.
    * @return Value of property printGrayAsColor.
    *
    */
   public boolean isPrintGrayAsColor();
   
   /** Setter for property printGrayAsColor.
    * @param printGrayAsColor New value of property printGrayAsColor.
    *
    */
   public void setPrintGrayAsColor(boolean printGrayAsColor);
   
   /** Getter for property maxQueuedJobCount.
    * @return Value of property maxQueuedJobCount.
    *
    */
   public int getMaxQueuedJobCount();
   
   /** Setter for property maxQueuedJobCount.
    * @param maxQueuedJobCount New value of property maxQueuedJobCount.
    *
    */
   public void setMaxQueuedJobCount(int maxQueuedJobCount);
   
   /** Getter for property supportsMonochrome.
    * @return Value of property supportsMonochrome.
    *
    */
   public boolean isSupportsGrayscale();
   
   /** Setter for property supportsMonochrome.
    * @param supportsMonochrome New value of property supportsMonochrome.
    *
    */
   public void setSupportsGrayscale(boolean supportsMonochrome);
   
   public void scheduleJob(Boolean color, String job, Dataset sessionAttr);
   
   /** Getter for property chunkSize.
    * @return Value of property chunkSize.
    */
   public double getChunkSize();
   
   /** Setter for property chunkSize.
    * @param chunkSize New value of property chunkSize.
    *
    */
   public void setChunkSize(double chunkSize);
   
}
