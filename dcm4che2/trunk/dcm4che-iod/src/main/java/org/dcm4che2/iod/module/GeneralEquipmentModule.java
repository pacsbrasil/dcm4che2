package org.dcm4che2.iod.module;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class GeneralEquipmentModule extends Module {

    public GeneralEquipmentModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getManufacturer() {
        return dcmobj.getString(Tag.Manufacturer);
    }

    public void getManufacturer(String s) {
        dcmobj.putString(Tag.Manufacturer, VR.LO, s);
    }

    public String getInstitutionName() {
        return dcmobj.getString(Tag.InstitutionName);
    }

    public void getInstitutionName(String s) {
        dcmobj.putString(Tag.InstitutionName, VR.LO, s);
    }

    public String getIInstitutionAddress() {
        return dcmobj.getString(Tag.InstitutionAddress);
    }

    public void getIInstitutionAddress(String s) {
        dcmobj.putString(Tag.InstitutionAddress, VR.ST, s);
    }

    public String getInstitutionalDepartmentName() {
        return dcmobj.getString(Tag.InstitutionalDepartmentName);
    }

    public void getIInstitutionalDepartmentName(String s) {
        dcmobj.putString(Tag.InstitutionalDepartmentName, VR.LO, s);
    }

    public String getManufacturersModelName() {
        return dcmobj.getString(Tag.ManufacturersModelName);
    }

    public void getManufacturersModelName(String s) {
        dcmobj.putString(Tag.ManufacturersModelName, VR.LO, s);
    }

    public String getDeviceSerialNumber() {
        return dcmobj.getString(Tag.DeviceSerialNumber);
    }

    public void getDeviceSerialNumber(String s) {
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
