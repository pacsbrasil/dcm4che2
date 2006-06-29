package org.dcm4che2.iod.module.composite;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

public class GeneralEquipmentModule extends Module {

    public GeneralEquipmentModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getManufacturer() {
        return dcmobj.getString(Tag.Manufacturer);
    }

    public void setManufacturer(String s) {
        dcmobj.putString(Tag.Manufacturer, VR.LO, s);
    }

    public String getInstitutionName() {
        return dcmobj.getString(Tag.InstitutionName);
    }

    public void setInstitutionName(String s) {
        dcmobj.putString(Tag.InstitutionName, VR.LO, s);
    }

    public String getInstitutionAddress() {
        return dcmobj.getString(Tag.InstitutionAddress);
    }

    public void setInstitutionAddress(String s) {
        dcmobj.putString(Tag.InstitutionAddress, VR.ST, s);
    }

    public String getStationName() {
        return dcmobj.getString(Tag.StationName);
    }
    
    public void setStationName(String s) {
        dcmobj.putString(Tag.StationName, VR.SH, s);
    }
    
    public String getInstitutionalDepartmentName() {
        return dcmobj.getString(Tag.InstitutionalDepartmentName);
    }

    public void setInstitutionalDepartmentName(String s) {
        dcmobj.putString(Tag.InstitutionalDepartmentName, VR.LO, s);
    }

    public String getManufacturersModelName() {
        return dcmobj.getString(Tag.ManufacturersModelName);
    }

    public void setManufacturersModelName(String s) {
        dcmobj.putString(Tag.ManufacturersModelName, VR.LO, s);
    }

    public String getDeviceSerialNumber() {
        return dcmobj.getString(Tag.DeviceSerialNumber);
    }

    public void setDeviceSerialNumber(String s) {
        dcmobj.putString(Tag.DeviceSerialNumber, VR.LO, s);
    }

    public String[] getSoftwareVersions() {
        return dcmobj.getStrings(Tag.SoftwareVersions);
    }

    public void setSoftwareVersions(String[] ss) {
        dcmobj.putStrings(Tag.SoftwareVersions, VR.LO, ss);
    }

    public float[] getSpatialResolution() {
        return dcmobj.getFloats(Tag.SpatialResolution);
    }

    public void setSoftwareVersions(float[] floats) {
        dcmobj.putFloats(Tag.SpatialResolution, VR.DS, floats);
    }

    public Date getDateTimeofLastCalibration() {
        return dcmobj.getDate(Tag.DateofLastCalibration,
                Tag.TimeofLastCalibration);
    }

    public void setDateTimeofLastCalibration(Date d) {
        dcmobj.putDate(Tag.DateofLastCalibration, VR.DA, d);
        dcmobj.putDate(Tag.TimeofLastCalibration, VR.TM, d);
    }

    public int getLargestPixelValueinSeries() {
        return dcmobj.getInt(Tag.PixelPaddingValue);
    }

    public void setLargestPixelValueinSeries(int s) {
        dcmobj.putInt(Tag.PixelPaddingValue,
                isSignedPixelValues() ? VR.SS : VR.US, s);
    }

}
