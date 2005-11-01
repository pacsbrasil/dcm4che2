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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 7, 2005
 *
 */
public class Device
{
    private boolean installed;
    private String deviceName;
    private String description;
    private String manufactorer;
    private String manufactorerModelName;
    private String stationName;
    private String deviceSerialNumber;
    private String issuerOfPatientID;
    private List softwareVersions = new ArrayList();
    private List primaryDeviceTypes = new ArrayList();
    private List institutionNames = new ArrayList();
    private List institutionAddresses = new ArrayList();
    private List institutionalDepartmentNames = new ArrayList();
    private List authorizedNodeCertificateRefs = new ArrayList();
    private List thisNodeCertificatesRefs = new ArrayList();
    private List relatedDeviceRefs = new ArrayList();
    private List vendorDeviceData = new ArrayList();

    private List networkConnections = new ArrayList();
    private List networkAEs = new ArrayList();
    
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
    
    public final String getStationName()
    {
        return stationName;
    }
    
    public final void setStationName(String stationName)
    {
        this.stationName = stationName;
    }
    
    public final List getAuthorizedNodeCertificateRefs()
    {
        return Collections.unmodifiableList(authorizedNodeCertificateRefs);
    }
    
    public final void addAuthorizedNodeCertificateRef(CertificateReference certRef)
    {
        if (certRef == null)
            throw new NullPointerException();
        this.authorizedNodeCertificateRefs.add(certRef);
    }
    
    public final String getDeviceSerialNumber()
    {
        return deviceSerialNumber;
    }
    
    public final void setDeviceSerialNumber(String deviceSerialNumber)
    {
        this.deviceSerialNumber = deviceSerialNumber;
    }
    
    public final boolean isInstalled()
    {
        return installed;
    }
    
    public final void setInstalled(boolean installed)
    {
        this.installed = installed;
    }
    
    public final List getInstitutionAddresses()
    {
        return Collections.unmodifiableList(institutionAddresses);
    }
    
    public final void addInstitutionAddresses(String addr)
    {
        this.institutionAddresses.add(addr);
    }
    
    public final List getInstitutionalDepartmentNames()
    {
        return Collections.unmodifiableList(institutionalDepartmentNames);
    }
    
    public final void addInstitutionalDepartmentName(String name)
    {
        this.institutionalDepartmentNames.add(name);
    }
    
    public final List getInstitutionNames()
    {
        return Collections.unmodifiableList(institutionNames);
    }
    
    public final void addInstitutionName(String name)
    {
        this.institutionNames.add(name);
    }
    
    public final String getIssuerOfPatientID()
    {
        return issuerOfPatientID;
    }
    public final void setIssuerOfPatientID(String issuerOfPatientID)
    {
        this.issuerOfPatientID = issuerOfPatientID;
    }
    
    public final List getNetworkAEs()
    {
        return Collections.unmodifiableList(networkAEs);
    }
    
    public final void addNetworkAEs(NetworkAE networkAE)
    {
        networkAE.setDevice(this);
        this.networkAEs.add(networkAE);
    }
    
    public final List getNetworkConnections()
    {
        return Collections.unmodifiableList(networkConnections);
    }
    
    public final void addNetworkConnections(NetworkConnection networkConnection)
    {
        networkConnection.setDevice(this);
        this.networkConnections.add(networkConnection);
    }
    
    public final List getPrimaryDeviceTypes()
    {
        return Collections.unmodifiableList(primaryDeviceTypes);
    }
    
    public final void addPrimaryDeviceType(String primaryDeviceType)
    {
        this.primaryDeviceTypes.add(primaryDeviceType);
    }
    
    public final List getRelatedDeviceRefs()
    {
        return Collections.unmodifiableList(relatedDeviceRefs);
    }
    
    public final void addRelatedDeviceRefs(DNReference deviceRef)
    {
        this.relatedDeviceRefs.add(deviceRef);
    }
    
    public final List getSoftwareVersions()
    {
        return Collections.unmodifiableList(softwareVersions);
    }
    
    public final void addSoftwareVersion(String softwareVersion)
    {
        this.softwareVersions.add(softwareVersion);
    }
    
    public final List getThisNodeCertificatesRefs()
    {
        return Collections.unmodifiableList(thisNodeCertificatesRefs);
    }
    
    public final void addThisNodeCertificateRef(CertificateReference certRef)
    {
        this.thisNodeCertificatesRefs.add(certRef);
    }
    
    public final List getVendorDeviceData()
    {
        return Collections.unmodifiableList(vendorDeviceData);
    }
    
    public final void addVendorDeviceData(VendorData vendorDeviceData)
    {
        this.vendorDeviceData.add(vendorDeviceData);
    }
    
}
