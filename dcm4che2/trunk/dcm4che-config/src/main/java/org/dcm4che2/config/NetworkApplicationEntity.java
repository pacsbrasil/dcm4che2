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

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 7, 2005
 *
 */
public class NetworkApplicationEntity
{
    private DeviceConfiguration device;
    private boolean associationAcceptor;
    private boolean associationInitiator;
    private String aeTitle;
    private String description;
    private Object[] vendorData = {};
    private String[] applicationCluster = {};
    private String[] preferredCallingAETitle = {};
    private String[] preferredCalledAETitle = {};
    private String[] supportedCharacterSet = {};
    private Boolean installed;

    private int maxOpsInvoked = 1;
    private int maxOpsPerformed = 1;
    private int maxPDULengthReceive = 0x4000; //=16384
    private int maxPDULengthSend = 0x4000;
    private boolean packPDV;
    private int dimseRspTimeout = 60000;
    private int moveRspTimeout = 600000;
    private int idleTimeout = 60000;
    
    private NetworkConnection[] networkConnection = {};
    private TransferCapability[] transferCapability = {};
    
    public final DeviceConfiguration getDevice()
    {
        return device;
    }

    public final void setDevice(DeviceConfiguration device)
    {
        this.device = device;
    }

    public final String getAETitle()
    {
        return aeTitle;
    }

    public final void setAETitle(String aetitle)
    {
        this.aeTitle = aetitle;
    }

    public final String[] getApplicationCluster()
    {
        return applicationCluster;
    }

    public final void setApplicationCluster(String[] cluster)
    {
        this.applicationCluster = cluster;
    }

    public final boolean isAssociationAcceptor()
    {
        return associationAcceptor;
    }

    public final void setAssociationAcceptor(boolean acceptor)
    {
        this.associationAcceptor = acceptor;
    }

    public final boolean isAssociationInitiator()
    {
        return associationInitiator;
    }

    public final void setAssociationInitiator(boolean initiator)
    {
        this.associationInitiator = initiator;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    public final boolean isInstalled()
    {
        return installed != null ? installed.booleanValue() 
                                 : device == null || device.isInstalled();
    }

    public final void setInstalled(boolean installed)
    {
        this.installed = Boolean.valueOf(installed);
    }

    public final NetworkConnection[] getNetworkConnection()
    {
        return networkConnection;
    }

    public final void setNetworkConnection(NetworkConnection[] conn)
    {
        this.networkConnection = conn;
    }

    public final String[] getPreferredCalledAETitle()
    {
        return preferredCalledAETitle;
    }

    public final boolean hasPreferredCalledAETitle()
    {
        return preferredCalledAETitle != null
                && preferredCalledAETitle.length > 0;
    }
    
    public boolean isPreferredCalledAETitle(String aet)
    {
        return contains(preferredCalledAETitle, aet);
    }
    
    private static boolean contains(String[] a, String s)
    {
        for (int i = 0; i < a.length; i++)
            if (s.equals(a[i]))
                return true;
        return false;
    }

    public final void setPreferredCalledAETitle(String[] aets)
    {
        this.preferredCalledAETitle = aets;
    }

    public final String[] getPreferredCallingAETitle()
    {
        return preferredCallingAETitle;
    }

    public final boolean hasPreferredCallingAETitle()
    {
        return preferredCallingAETitle != null
                && preferredCallingAETitle.length > 0;
    }
    
    public boolean isPreferredCallingAETitle(String aet)
    {
        return contains(preferredCallingAETitle, aet);
    }
    
    public final void setPreferredCallingAETitle(String[] aets)
    {
        this.preferredCallingAETitle = aets;
    }

    public final String[] getSupportedCharacterSet()
    {
        return supportedCharacterSet;
    }

    public final void setSupportedCharacterSet(String[] characterSets)
    {
        this.supportedCharacterSet = characterSets;
    }

    public final TransferCapability[] getTransferCapability()
    {
        return transferCapability;
    }

    public final void setTransferCapability(TransferCapability[] transferCapability)
    {
        this.transferCapability = transferCapability;
    }

    public final Object[] getVendorData()
    {
        return vendorData;
    }

    public final void setVendorData(Object[] vendorData)
    {
        this.vendorData = vendorData;
    }

    public final int getMaxOpsInvoked()
    {
        return maxOpsInvoked;
    }
    
    public final void setMaxOpsInvoked(int maxOpsInvoked)
    {
        this.maxOpsInvoked = maxOpsInvoked;
    }
    
    public final int getMaxOpsPerformed()
    {
        return maxOpsPerformed;
    }
    
    public final void setMaxOpsPerformed(int maxOpsPerformed)
    {
        this.maxOpsPerformed = maxOpsPerformed;
    }
    
    public final boolean isAsyncOps()
    {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }
    
    public final int getMaxPDULengthReceive()
    {
        return maxPDULengthReceive;
    }
    
    public final void setMaxPDULengthReceive(int maxPDULengthReceive)
    {
        this.maxPDULengthReceive = maxPDULengthReceive;
    }
    
    public final int getMaxPDULengthSend()
    {
        return maxPDULengthSend;
    }
    
    public final void setMaxPDULengthSend(int maxPDULengthSend)
    {
        this.maxPDULengthSend = maxPDULengthSend;
    }

    public final boolean isPackPDV()
    {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV)
    {
        this.packPDV = packPDV;
    }

    public final int getDimseRspTimeout()
    {
        return dimseRspTimeout ;
    }

    public final void setDimseRspTimeout(int dimseRspTimeout)
    {
        this.dimseRspTimeout = dimseRspTimeout;
    }

    public final int getIdleTimeout()
    {
        return idleTimeout;
    }

    public final void setIdleTimeout(int idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }

    public final int getMoveRspTimeout()
    {
        return moveRspTimeout;
    }

    public final void setMoveRspTimeout(int moveRspTimeout)
    {
        this.moveRspTimeout = moveRspTimeout;
    }

}
