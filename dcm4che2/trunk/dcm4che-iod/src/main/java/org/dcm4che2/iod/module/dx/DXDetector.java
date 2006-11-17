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
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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
package org.dcm4che2.iod.module.dx;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version Revision $Date$
 * @since 30.06.2006
 */

public class DXDetector extends Module {

    public DXDetector(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getDetectorType() {
        return dcmobj.getString(Tag.DETECTOR_TYPE);
    }
    
    public void setDetectorType(String s) {
        dcmobj.putString(Tag.DETECTOR_TYPE, VR.CS, s);
    }

    public String getDetectorConfiguration() {
        return dcmobj.getString(Tag.DETECTOR_CONFIGURATION);
    }
    
    public void setDetectorConfiguration(String s) {
        dcmobj.putString(Tag.DETECTOR_CONFIGURATION, VR.CS, s);
    }

    public String getDetectorDescription() {
        return dcmobj.getString(Tag.DETECTOR_DESCRIPTION);
    }
    
    public void setDetectorDescription(String s) {
        dcmobj.putString(Tag.DETECTOR_DESCRIPTION, VR.LT, s);
    }

    public String getDetectorMode() {
        return dcmobj.getString(Tag.DETECTOR_MODE);
    }
    
    public void setDetectorMode(String s) {
        dcmobj.putString(Tag.DETECTOR_MODE, VR.LT, s);
    }

    public String getDetectorID() {
        return dcmobj.getString(Tag.DETECTOR_ID);
    }
    
    public void setDetectorID(String s) {
        dcmobj.putString(Tag.DETECTOR_ID, VR.SH, s);
    }

    public Date getDateTimeofLastDetectorCalibration() {
        return dcmobj.getDate(Tag.DATE_OF_LAST_DETECTOR_CALIBRATION,
                Tag.TIME_OF_LAST_DETECTOR_CALIBRATION);
    }

    public void setDateTimeofLastDetectorCalibration(Date d) {
        dcmobj.putDate(Tag.DATE_OF_LAST_DETECTOR_CALIBRATION, VR.DA, d);
        dcmobj.putDate(Tag.TIME_OF_LAST_DETECTOR_CALIBRATION, VR.TM, d);
    }

    public int getExposuresonDetectorSinceLastCalibration() {
        return dcmobj.getInt(Tag.EXPOSURES_ON_DETECTOR_SINCE_LAST_CALIBRATION);
    }
    
    public void setExposuresonDetectorSinceLastCalibration(int i) {
        dcmobj.putInt(Tag.EXPOSURES_ON_DETECTOR_SINCE_LAST_CALIBRATION, VR.IS, i);
    }

    public int getExposuresonDetectorSinceManufactured() {
        return dcmobj.getInt(Tag.EXPOSURES_ON_DETECTOR_SINCE_MANUFACTURED);
    }
    
    public void setExposuresonDetectorSinceManufactured(int i) {
        dcmobj.putInt(Tag.EXPOSURES_ON_DETECTOR_SINCE_MANUFACTURED, VR.IS, i);
    }

    public float getDetectorTimeSinceLastExposure() {
        return dcmobj.getFloat(Tag.DETECTOR_TIME_SINCE_LAST_EXPOSURE);
    }
    
    public void setDetectorTimeSinceLastExposure(float f) {
        dcmobj.putFloat(Tag.DETECTOR_TIME_SINCE_LAST_EXPOSURE, VR.DS, f);
    }

    public float[] getDetectorBinning() {
        return dcmobj.getFloats(Tag.DETECTOR_BINNING);
    }
    
    public void setDetectorBinning(float[] f) {
        dcmobj.putFloats(Tag.DETECTOR_BINNING, VR.DS, f);
    }

    public String getDetectorManufacturerName() {
        return dcmobj.getString(Tag.DETECTOR_MANUFACTURER_NAME);
    }
    
    public void setDetectorManufacturerName(String s) {
        dcmobj.putString(Tag.DETECTOR_MANUFACTURER_NAME, VR.LO, s);
    }

    public String getDetectorManufacturersModelName() {
        return dcmobj.getString(Tag.DETECTOR_MANUFACTURERS_MODEL_NAME);
    }
    
    public void setDetectorManufacturersModelName(String s) {
        dcmobj.putString(Tag.DETECTOR_MANUFACTURERS_MODEL_NAME, VR.LO, s);
    }

    public String getDetectorConditionsNominalFlag() {
        return dcmobj.getString(Tag.DETECTOR_CONDITIONS_NOMINAL_FLAG);
    }
    
    public void setDetectorConditionsNominalFlag(String s) {
        dcmobj.putString(Tag.DETECTOR_CONDITIONS_NOMINAL_FLAG, VR.CS, s);
    }

    public float getDetectorTemperature() {
        return dcmobj.getFloat(Tag.DETECTOR_TEMPERATURE);
    }
    
    public void setDetectorTemperature(float f) {
        dcmobj.putFloat(Tag.DETECTOR_TEMPERATURE, VR.DS, f);
    }

    public float getSensitivity() {
        return dcmobj.getFloat(Tag.SENSITIVITY);
    }
    
    public void setSensitivity(float s) {
        dcmobj.putFloat(Tag.SENSITIVITY, VR.DS, s);
    }

    public float[] getDetectorElementPhysicalSize() {
        return dcmobj.getFloats(Tag.DETECTOR_ELEMENT_PHYSICAL_SIZE);
    }
    
    public void setDetectorElementPhysicalSize(float[] f) {
        dcmobj.putFloats(Tag.DETECTOR_ELEMENT_PHYSICAL_SIZE, VR.DS, f);
    }

    public float[] getDetectorElementSpacing() {
        return dcmobj.getFloats(Tag.DETECTOR_ELEMENT_SPACING);
    }
    
    public void setDetectorElementSpacing(float[] f) {
        dcmobj.putFloats(Tag.DETECTOR_ELEMENT_SPACING, VR.DS, f);
    }

    public String getDetectorActiveShape() {
        return dcmobj.getString(Tag.DETECTOR_ACTIVE_SHAPE);
    }
    
    public void setDetectorActiveShape(String s) {
        dcmobj.putString(Tag.DETECTOR_ACTIVE_SHAPE, VR.CS, s);
    }

    public float[] getDetectorActiveDimensions() {
        return dcmobj.getFloats(Tag.DETECTOR_ACTIVE_DIMENSIONS);
    }
    
    public void setDetectorActiveDimensions(float[] f) {
        dcmobj.putFloats(Tag.DETECTOR_ACTIVE_DIMENSIONS, VR.DS, f);
    }

    public float[] getDetectorActiveOrigin() {
        return dcmobj.getFloats(Tag.DETECTOR_ACTIVE_ORIGIN);
    }
    
    public void setDetectorActiveOrigin(float[] f) {
        dcmobj.putFloats(Tag.DETECTOR_ACTIVE_ORIGIN, VR.DS, f);
    }
}
