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

import java.util.ArrayList;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 30.08.2003
 */
public class DeviceInfo extends ConfigInfo {

    private String deviceName;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private ArrayList versions = new ArrayList(1);
    private ArrayList types = new ArrayList(1);
    private ArrayList relDeviceRef = new ArrayList(1);
    private ArrayList authNodeCerts = new ArrayList();
    private ArrayList thisNodeCerts = new ArrayList();
    private ArrayList vendorData = new ArrayList();
    private boolean installed = false;
    private ArrayList aeList = new ArrayList();
    private ArrayList conList = new ArrayList();

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
            + "\n\trelatedDeviceReference="
            + relDeviceRef
            + "\n\tauthorizedNodeCertificate="
            + authNodeCerts
            + "\n\tthisNodeCertificate="
            + thisNodeCerts
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        if (deviceName == null)
            throw new NullPointerException("deviceName");

        this.deviceName = deviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturerModelName() {
        return manufacturerModelName;
    }

    public void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    public String[] getVersions() {
        return (String[]) versions.toArray(new String[versions.size()]);
    }

    public void addVersion(String version) {
        if (version == null)
            throw new NullPointerException("version");

        versions.add(version);
    }

    public boolean removeVersion(String version) {
        return versions.remove(version);
    }

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

    public String[] getRelatedDeviceReference() {
        return (String[]) relDeviceRef.toArray(new String[relDeviceRef.size()]);
    }

    public void addRelatedDeviceReference(String dn) {
        if (dn == null)
            throw new NullPointerException("dn");

        relDeviceRef.add(dn);
    }

    public boolean removeRelatedDeviceReference(String dn) {
        return relDeviceRef.remove(dn);
    }

    public boolean hasRelatedDeviceReference() {
        return !relDeviceRef.isEmpty();
    }

    public NodeCertificateInfo[] getAuthorizedNodeCertificate() {
        return (NodeCertificateInfo[]) authNodeCerts.toArray(
            new NodeCertificateInfo[authNodeCerts.size()]);
    }

    public void addAuthorizedNodeCertificate(NodeCertificateInfo cert) {
        if (cert == null)
            throw new NullPointerException("cert");

        authNodeCerts.add(cert);
    }

    public boolean removeAuthorizedNodeCertificate(NodeCertificateInfo cert) {
        return authNodeCerts.remove(cert);
    }

    public boolean hasAuthorizedNodeCertificate() {
        return !authNodeCerts.isEmpty();
    }

    public NodeCertificateInfo[] getThisNodeCertificate() {
        return (NodeCertificateInfo[]) thisNodeCerts.toArray(
            new NodeCertificateInfo[authNodeCerts.size()]);
    }

    public void addThisNodeCertificate(NodeCertificateInfo cert) {
        if (cert == null)
            throw new NullPointerException("cert");

        thisNodeCerts.add(cert);
    }

    public boolean removeThisNodeCertificate(NodeCertificateInfo cert) {
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

    public NetworkAEInfo[] getNetworkAE() {
        return (NetworkAEInfo[]) aeList.toArray(
            new NetworkAEInfo[aeList.size()]);
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
        return deviceName != null;
    }

}
