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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2005
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

package org.dcm4che2.config;

import java.security.cert.X509Certificate;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 7, 2005
 *
 */
public class DeviceConfiguration
{
    private String deviceName;
    private String description;
    private String manufactorer;
    private String manufactorerModelName;
    private String stationName;
    private String deviceSerialNumber;
    private String issuerOfPatientID;
    private String[] softwareVersion = {};
    private String[] primaryDeviceType = {};
    private String[] institutionName = {};
    private String[] institutionAddress = {};
    private String[] institutionalDepartmentName = {};
    private X509Certificate[] authorizedNodeCertificate = {};
    private X509Certificate[] thisNodeCertificate = {};
    private Object[] relatedDevice = {};
    private Object[] vendorDeviceData = {};
    private int associationReaperPeriod = 10000;
    private boolean installed = true;

    private NetworkConnection[] networkConnection = {};
    private NetworkApplicationEntity[] networkAE = {};
    
    public DeviceConfiguration()
    {
    }
    
    public DeviceConfiguration(String deviceName)
    {
        setDeviceName(deviceName);
    }

    public final String getDeviceName()
    {
        return deviceName;
    }
    
    public final void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }
    
    public final String getDescription()
    {
        return description;
    }
    
    public final void setDescription(String description)
    {
        this.description = description;
    }
    
    public final String getManufactorer()
    {
        return manufactorer;
    }
    
    public final void setManufactorer(String manufactorer)
    {
        this.manufactorer = manufactorer;
    }
    
    public final String getManufactorerModelName()
    {
        return manufactorerModelName;
    }
    
    public final void setManufactorerModelName(String manufactorerModelName)
    {
        this.manufactorerModelName = manufactorerModelName;
    }
    
    public final String[] getSoftwareVersion()
    {
        return softwareVersion;
    }
    
    public final void setSoftwareVersion(String[] softwareVersion)
    {
        this.softwareVersion = softwareVersion;
    }
    
    public final String getStationName()
    {
        return stationName;
    }
    
    public final void setStationName(String stationName)
    {
        this.stationName = stationName;
    }
    
    public final String getDeviceSerialNumber()
    {
        return deviceSerialNumber;
    }
    
    public final void setDeviceSerialNumber(String deviceSerialNumber)
    {
        this.deviceSerialNumber = deviceSerialNumber;
    }
    
    public final String[] getPrimaryDeviceType()
    {
        return primaryDeviceType;
    }
    
    public final void setPrimaryDeviceType(String[] primaryDeviceType)
    {
        this.primaryDeviceType = primaryDeviceType;
    }
    
    public final String[] getInstitutionName()
    {
        return institutionName;
    }
    
    public final void setInstitutionName(String[] name)
    {
        this.institutionName = name;
    }
    
    public final String[] getInstitutionAddress()
    {
        return institutionAddress;
    }
    
    public final void setInstitutionAddresses(String[] addr)
    {
        this.institutionAddress = addr;
    }
    
    public final String[] getInstitutionalDepartmentName()
    {
        return institutionalDepartmentName;
    }
    
    public final void setInstitutionalDepartmentName(String[] name)
    {
        this.institutionalDepartmentName = name;
    }
    
    public final String getIssuerOfPatientID()
    {
        return issuerOfPatientID;
    }
    
    public final void setIssuerOfPatientID(String issuerOfPatientID)
    {
        this.issuerOfPatientID = issuerOfPatientID;
    }
    
    public final Object[] getRelatedDevice()
    {
        return relatedDevice;
    }
    
    public final void setRelatedDeviceReference(Object[] relatedDevice)
    {
        this.relatedDevice = relatedDevice;
    }
    
    public final X509Certificate[] getAuthorizedNodeCertificate()
    {
        return authorizedNodeCertificate;
    }
    
    public final void setAuthorizedNodeCertificate(X509Certificate[] cert)
    {
        this.authorizedNodeCertificate = cert;
    }
    
    public final X509Certificate[] getThisNodeCertificate()
    {
        return thisNodeCertificate;
    }
    
    public final void setThisNodeCertificate(X509Certificate[] cert)
    {
        this.thisNodeCertificate = cert;
    }
    
    public final Object[] getVendorDeviceData()
    {
        return vendorDeviceData;
    }
    
    public final void setVendorDeviceData(Object[] vendorDeviceData)
    {
        this.vendorDeviceData = vendorDeviceData;
    }
    
    public final boolean isInstalled()
    {
        return installed;
    }
    
    public final void setInstalled(boolean installed)
    {
        this.installed = installed;
    }
    
    public final NetworkApplicationEntity[] getNetworkApplicationEntity()
    {
        return networkAE;
    }
    
    public final void setNetworkApplicationEntity(
            NetworkApplicationEntity[] networkAE)
    {
        for (int i = 0; i < networkAE.length; i++)
            networkAE[i].setDevice(this);
        
        this.networkAE = networkAE;
    }
    
    public final NetworkConnection[] getNetworkConnection()
    {
        return networkConnection;
    }
    
    public final void setNetworkConnection(NetworkConnection[] networkConnection)
    {
        for (int i = 0; i < networkConnection.length; i++)
            networkConnection[i].setDevice(this);
        
        this.networkConnection = networkConnection;
    }

    public final int getAssociationReaperPeriod()
    {
        return associationReaperPeriod;
    }

    public final void setAssociationReaperPeriod(int associationReaperPeriod)
    {
        this.associationReaperPeriod = associationReaperPeriod;
    }
    
}
