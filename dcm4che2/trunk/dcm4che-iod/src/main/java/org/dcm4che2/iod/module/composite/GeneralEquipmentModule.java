package org.dcm4che2.iod.module.composite;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.value.PixelRepresentation;

public class GeneralEquipmentModule extends Module {

    public GeneralEquipmentModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getManufacturer() {
        return dcmobj.getString(Tag.MANUFACTURER);
    }

    public void setManufacturer(String s) {
        dcmobj.putString(Tag.MANUFACTURER, VR.LO, s);
    }

    public String getInstitutionName() {
        return dcmobj.getString(Tag.INSTITUTION_NAME);
    }

    public void setInstitutionName(String s) {
        dcmobj.putString(Tag.INSTITUTION_NAME, VR.LO, s);
    }

    public String getInstitutionAddress() {
        return dcmobj.getString(Tag.INSTITUTION_ADDRESS);
    }

    public void setInstitutionAddress(String s) {
        dcmobj.putString(Tag.INSTITUTION_ADDRESS, VR.ST, s);
    }

    public String getStationName() {
        return dcmobj.getString(Tag.STATION_NAME);
    }
    
    public void setStationName(String s) {
        dcmobj.putString(Tag.STATION_NAME, VR.SH, s);
    }
    
    public String getInstitutionalDepartmentName() {
        return dcmobj.getString(Tag.INSTITUTIONAL_DEPARTMENT_NAME);
    }

    public void setInstitutionalDepartmentName(String s) {
        dcmobj.putString(Tag.INSTITUTIONAL_DEPARTMENT_NAME, VR.LO, s);
    }

    public String getManufacturersModelName() {
        return dcmobj.getString(Tag.MANUFACTURERS_MODEL_NAME);
    }

    public void setManufacturersModelName(String s) {
        dcmobj.putString(Tag.MANUFACTURERS_MODEL_NAME, VR.LO, s);
    }

    public String getDeviceSerialNumber() {
        return dcmobj.getString(Tag.DEVICE_SERIAL_NUMBER);
    }

    public void setDeviceSerialNumber(String s) {
        dcmobj.putString(Tag.DEVICE_SERIAL_NUMBER, VR.LO, s);
    }

    public String[] getSoftwareVersions() {
        return dcmobj.getStrings(Tag.SOFTWARE_VERSIONS);
    }

    public void setSoftwareVersions(String[] ss) {
        dcmobj.putStrings(Tag.SOFTWARE_VERSIONS, VR.LO, ss);
    }

    public float[] getSpatialResolution() {
        return dcmobj.getFloats(Tag.SPATIAL_RESOLUTION);
    }

    public void setSoftwareVersions(float[] floats) {
        dcmobj.putFloats(Tag.SPATIAL_RESOLUTION, VR.DS, floats);
    }

    public Date getDateTimeofLastCalibration() {
        return dcmobj.getDate(Tag.DATE_OF_LAST_CALIBRATION,
                Tag.TIME_OF_LAST_CALIBRATION);
    }

    public void setDateTimeofLastCalibration(Date d) {
        dcmobj.putDate(Tag.DATE_OF_LAST_CALIBRATION, VR.DA, d);
        dcmobj.putDate(Tag.TIME_OF_LAST_CALIBRATION, VR.TM, d);
    }

    public int getLargestPixelValueinSeries() {
        return dcmobj.getInt(Tag.PIXEL_PADDING_VALUE);
    }

    public void setLargestPixelValueinSeries(int s) {
        dcmobj.putInt(Tag.PIXEL_PADDING_VALUE,
                PixelRepresentation.isSigned(dcmobj) ? VR.SS : VR.US, s);
    }

}
