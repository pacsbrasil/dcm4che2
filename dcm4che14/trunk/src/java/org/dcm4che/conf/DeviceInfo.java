/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4che.conf;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents a Device.
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup67_fz.pdf">
 * DICOM Supplement 67 - Configuration Management.</a>   
 * 
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 30.08.2003
 */
public class DeviceInfo {

    private String deviceName;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private ArrayList versions = new ArrayList(1);
    private ArrayList types = new ArrayList(1);
    private ArrayList relDeviceDesc = new ArrayList(1);
    private ArrayList authNodeCerts = new ArrayList();
    private ArrayList thisNodeCerts = new ArrayList();
    private ArrayList vendorData = new ArrayList();
    private boolean installed = false;
    private ArrayList aeList = new ArrayList();
    private ArrayList conList = new ArrayList();

    /**
     * Returns a String representation of this device.
     * @return a String representation of this device.
     */
    public String toString() {
        return getClass().getName()
            + "[\n\tdeviceName="
            + deviceName
            + "\n\tdescription="
            + description
            + "\n\tmanufacturer="
            + manufacturer
            + "\n\tmanufacturerModelName="
            + manufacturerModelName
            + "\n\tversions="
            + versions
            + "\n\tprimaryDeviceType="
            + types
            + "\n\trelatedDeviceInfo=#"
            + relDeviceDesc.size()
            + "\n\tauthorizedNodeCertificate=#"
            + authNodeCerts.size()
            + "\n\tthisNodeCertificate=#"
            + thisNodeCerts.size()
            + "\n\tvendorData=#"
            + vendorData.size()
            + "\n\tinstalled="
            + installed
            + "\n\tnetworkConnection="
            + toString(conList)
            + "\n\tnetworkAE="
            + toString(aeList)
            + "]";
    }

    private static String toString(ArrayList list) {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0, n = list.size(); i < n; ++i) {
            sb.append("\n\t\t").append(list.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Gets the unique name (within the scope of the LDAP database)
     * for this device. It is restricted to legal LDAP names, and not
     * constrained by DICOM AE Title limitations.
     * @return the unique name for this device.
     * @see #setDeviceName
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Sets the unique name (within the scope of the LDAP database)
     * for this device. It is restricted to legal LDAP names, and not
     * constrained by DICOM AE Title limitations. Mandatory for valid devices.
     * @param deviceName unique name for this device.
     * @see #getDeviceName
     * @see #isValid
     */
    public void setDeviceName(String deviceName) {
        if (deviceName == null)
            throw new NullPointerException("deviceName");

        this.deviceName = deviceName;
    }

    /**
     * Gets unconstrained text description of this device. 
     * @return unconstrained text description of this device.
     * @see #setDescription
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets unconstrained text description of this device.
     * @param description text description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets value of Manufacturer (0008,0070) attribute in SOP instances
     * created by this device.
     * @return value of Manufacturer (0008,0070) attribute.
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets value of Manufacturer (0008,0070) attribute in SOP instances
     * created by this device.
     * @param manufacturer value of Manufacturer (0008,0070) attribute.
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Gets the value of Manufacturer Model Name (0008,1090) attribute 
     * in SOP instances created by this device.
     * @return the value of Manufacturer Model Name (0008,1090) attribute.
     */
    public String getManufacturerModelName() {
        return manufacturerModelName;
    }

    /**
     * Sets the value of Manufacturer Model Name (0008,1090) attribute 
     * in SOP instances created by this device.
     * @param manufacturerModelName value of Manufacturer Model Name
     * (0008,1090) attribute.
     */
    public void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    /**
     * Gets Version of the device and/or its subcomponents
     * (manufacturer specific).
     * @return Version of the device.
     */
    public String[] getVersions() {
        return (String[]) versions.toArray(new String[versions.size()]);
    }

    /**
     * Add version of the device and/or its subcomponent.
     * @param version version of the device/subcomponent.
     */
    public void addVersion(String version) {
        if (version == null)
            throw new NullPointerException("version");

        versions.add(version);
    }

    /**
     * Remove version of the device and/or its subcomponent.
     * @param version version to remove.
     * @return <code>true</code> if the devices was associated
     * with the specified version.
     */
    public boolean removeVersion(String version) {
        return versions.remove(version);
    }

    /**
     * @return <code>true</code> if a version of the device is specified. 
     */
    public boolean hasVersion() {
        return !versions.isEmpty();
    }

    public String[] getPrimaryDeviceType() {
        return (String[]) types.toArray(new String[types.size()]);
    }

    public void addPrimaryDeviceType(String type) {
        if (type == null)
            throw new NullPointerException("type");

        types.add(type);
    }

    public boolean removePrimaryDeviceType(String type) {
        return types.remove(type);
    }

    public boolean hasPrimaryDeviceType() {
        return !types.isEmpty();
    }

    public Object[] getRelatedDeviceDescription() {
        return relDeviceDesc.toArray();
    }

    public void addRelatedDeviceDescription(Object info) {
        if (info == null)
            throw new NullPointerException("info");

        relDeviceDesc.add(info);
    }

    public boolean removeRelatedDeviceDescription(Object info) {
        return relDeviceDesc.remove(info);
    }

    public boolean hasRelatedDeviceReference() {
        return !relDeviceDesc.isEmpty();
    }

    public X509Certificate[] getAuthorizedNodeCertificate() {
        return (X509Certificate[]) authNodeCerts.toArray(
            new X509Certificate[authNodeCerts.size()]);
    }

    public void addAuthorizedNodeCertificate(X509Certificate cert) {
        if (cert == null)
            throw new NullPointerException("cert");

        authNodeCerts.add(cert);
    }

    public boolean removeAuthorizedNodeCertificate(X509Certificate cert) {
        return authNodeCerts.remove(cert);
    }

    public boolean hasAuthorizedNodeCertificate() {
        return !authNodeCerts.isEmpty();
    }

    public X509Certificate[] getThisNodeCertificate() {
        return (X509Certificate[]) thisNodeCerts.toArray(
            new X509Certificate[authNodeCerts.size()]);
    }

    public void addThisNodeCertificate(X509Certificate cert) {
        if (cert == null)
            throw new NullPointerException("cert");

        thisNodeCerts.add(cert);
    }

    public boolean removeThisNodeCertificate(X509Certificate cert) {
        return thisNodeCerts.remove(cert);
    }

    public boolean hasThisNodeCertificate() {
        return !thisNodeCerts.isEmpty();
    }

    public byte[][] getVendorData() {
        return (byte[][]) vendorData.toArray(new byte[vendorData.size()][]);
    }

    public void addVendorData(byte[] data) {
        if (data == null)
            throw new NullPointerException("data");

        vendorData.add(data);
    }

    public boolean removeVendorData(byte[] data) {
        return vendorData.remove(data);
    }

    public boolean hasVendorData() {
        return !vendorData.isEmpty();
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public NetworkConnectionInfo[] getNetworkConnection() {
        return (NetworkConnectionInfo[]) conList.toArray(
            new NetworkConnectionInfo[conList.size()]);
    }

    public void addNetworkConnection(NetworkConnectionInfo connInfo) {
        if (connInfo == null)
            throw new NullPointerException("connInfo");

        conList.add(connInfo);
    }

    public boolean removeNetworkConnection(NetworkConnectionInfo connInfo) {
        return conList.remove(connInfo);
    }

    public NetworkAEInfo[] getNetworkAEs() {
        return (NetworkAEInfo[]) aeList.toArray(
            new NetworkAEInfo[aeList.size()]);
    }

    public NetworkAEInfo getNetworkAE(String aet) {
        NetworkAEInfo info;
        for (Iterator it = aeList.iterator(); it.hasNext();) {
            info = (NetworkAEInfo) it.next();
            if (aet.equals(info.getAETitle())) {
                return info;
            }
        }
        return null;
    }

    public void addNetworkAE(NetworkAEInfo aeInfo) {
        if (aeInfo == null)
            throw new NullPointerException("aeInfo");

        aeList.add(aeInfo);
    }

    public boolean removeNetworkAEInfo(NetworkAEInfo aeInfo) {
        return aeList.remove(aeInfo);
    }

    public boolean isValid() {
        return deviceName != null
            && !conList.isEmpty()
            && !aeList.isEmpty()
            && isValid(getNetworkConnection())
            && isValid(getNetworkAEs());
    }

    private boolean isValid(NetworkConnectionInfo[] nc) {
        for (int i = 0; i < nc.length; i++) {
            if (!nc[i].isValid()) {
                return false;
            }
        }
        return true;
    }

    private boolean isValid(NetworkAEInfo[] ae) {
        for (int i = 0; i < ae.length; i++) {
            if (!ae[i].isValid()) {
                return false;
            }
        }
        return true;
    }
}
