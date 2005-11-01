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
public class NetworkAE
{
    private static final boolean DEFAULT_INSTALLED = true;
    
    private Device device;
    private Boolean installed;
    private boolean associationAcceptor;
    private boolean associationInitiator;
    private String aetitle;
    private String description;
    private List vendorData = new ArrayList();
    private List applicationClusters = new ArrayList();
    private List preferredCallingAETitles = new ArrayList();
    private List preferredCalledAETitles = new ArrayList();
    private List networkConnections = new ArrayList();
    private List supportedCharacterSets = new ArrayList();

    private List transferCapabilities = new ArrayList();
    
    public final Device getDevice()
    {
        return device;
    }
    
    void setDevice(Device device)
    {
        this.device = device;        
    }

    public final String getAEtitle()
    {
        return aetitle;
    }

    public final void setAEtitle(String aetitle)
    {
        this.aetitle = aetitle;
    }

    public final List getApplicationClusters()
    {
        return Collections.unmodifiableList(applicationClusters);
    }

    public final void addApplicationCluster(String cluster)
    {
        this.applicationClusters.add(cluster);
    }

    public final boolean isAssociationAcceptor()
    {
        return associationAcceptor;
    }

    public final void setAssociationAcceptor(boolean associationAcceptor)
    {
        this.associationAcceptor = associationAcceptor;
    }

    public final boolean isAssociationInitiator()
    {
        return associationInitiator;
    }

    public final void setAssociationInitiator(boolean associationInitiator)
    {
        this.associationInitiator = associationInitiator;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isInstalled()
    {
        Boolean b = getInstalled();
        return b != null ? b.booleanValue() : DEFAULT_INSTALLED;
    }
    
    public final Boolean getInstalled()
    {
        return installed == null && device != null
            ? Boolean.valueOf(device.isInstalled()) : installed;
    }

    public final void setInstalled(Boolean installed)
    {
        this.installed = installed;
    }

    public final List getNetworkConnections()
    {
        return Collections.unmodifiableList(networkConnections);
    }

    public final void addNetworkConnection(NetworkConnection conn)
    {
        conn.addNetworkAE(this);
        this.networkConnections.add(conn);
    }

    public final List getPreferredCalledAETitles()
    {
        return Collections.unmodifiableList(preferredCalledAETitles);
    }

    public final void addPreferredCalledAETitle(String aet)
    {
        this.preferredCalledAETitles.add(aet);
    }

    public final List getPreferredCallingAETitles()
    {
        return Collections.unmodifiableList(preferredCallingAETitles);
    }

    public final void setPreferredCallingAETitles(List preferredCallingAETitles)
    {
        this.preferredCallingAETitles = preferredCallingAETitles;
    }

    public final List getSupportedCharacterSets()
    {
        return Collections.unmodifiableList(supportedCharacterSets);
    }

    public final void addSupportedCharacterSet(String characterSet)
    {
        this.supportedCharacterSets.add(characterSet);
    }

    public final List getTransferCapabilities()
    {
        return Collections.unmodifiableList(transferCapabilities);
    }

    public final void addTransferCapability(TransferCapability transferCapability)
    {
        this.transferCapabilities.add(transferCapability);
    }

    public final List getVendorData()
    {
        return Collections.unmodifiableList(vendorData);
    }

    public final void addVendorData(VendorData vendorData)
    {
        this.vendorData.add(vendorData);
    }
}
