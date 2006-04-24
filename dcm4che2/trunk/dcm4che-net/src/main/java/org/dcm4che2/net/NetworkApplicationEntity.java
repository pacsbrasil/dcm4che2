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
 * Damien Evans <damien@theevansranch.com>
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

package org.dcm4che2.net;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.ExtendedNegotiation;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.RoleSelection;
import org.dcm4che2.net.service.DicomService;

/**
 * DICOM Supplement 67 compliant description of a DICOM network service.
 * <p>
 * A Network AE is an application entity that provides services on a network. A
 * Network AE will have the 16 same functional capability regardless of the
 * particular network connection used. If there are functional differences based
 * on selected network connection, then these are separate Network AEs. If there
 * are 18 functional differences based on other internal structures, then these
 * are separate Network AEs.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 * 
 */
public class NetworkApplicationEntity
{
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

    private int maxOpsInvoked;

    private int maxOpsPerformed;

    private int maxPDULengthReceive = 0x4000; // =16384

    private int maxPDULengthSend = 0x4000;

    private boolean packPDV;

    private int dimseRspTimeout = 60000;

    private int moveRspTimeout = 600000;

    private int idleTimeout = 60000;

    private String[] reuseAssocationToAETitle = {};

    private String[] reuseAssocationFromAETitle = {};

    private NetworkConnection[] networkConnection = {};

    private TransferCapability[] transferCapability = {};

    private final DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();

    private final List pool = new ArrayList();

    private Device device;

    /**
     * Get the device that is identified by this application entity.
     * 
     * @return The owning <code>Device</code>.
     */
    public final Device getDevice()
    {
        return device;
    }

    /**
     * Set the device that is identified by this application entity.
     * 
     * @param device The owning <code>Device</code>.
     */
    final void setDevice(Device device)
    {
        this.device = device;
    }

    /**
     * Get the AE title for this Network AE.
     * 
     * @return A String containing the AE title.
     */
    public final String getAETitle()
    {
        return aeTitle;
    }

    /**
     * Set the AE title for this Network AE.
     * 
     * @param aetitle A String containing the AE title.
     */
    public final void setAETitle(String aetitle)
    {
        this.aeTitle = aetitle;
    }

    /**
     * Get the locally defined names for a subset of related applications. E.g.
     * neuroradiology.
     * 
     * @return A String array containing the names.
     */
    public final String[] getApplicationCluster()
    {
        return applicationCluster;
    }

    /**
     * Set the locally defined names for a subset of related applications. E.g.
     * neuroradiology.
     * 
     * @param cluster A String array containing the names.
     */
    public final void setApplicationCluster(String[] cluster)
    {
        this.applicationCluster = cluster;
    }

    /**
     * Determine whether or not this network AE can accept associations.
     * 
     * @return A boolean value. True if the Network AE can accept associations,
     *         false otherwise.
     */
    public final boolean isAssociationAcceptor()
    {
        return associationAcceptor;
    }

    /**
     * Set whether or not this network AE can accept associations.
     * 
     * @param acceptor A boolean value. True if the Network AE can accept
     *            associations, false otherwise.
     */
    public final void setAssociationAcceptor(boolean acceptor)
    {
        this.associationAcceptor = acceptor;
    }

    /**
     * Determine whether or not this network AE can initiate associations.
     * 
     * @return A boolean value. True if the Network AE can accept associations,
     *         false otherwise.
     */
    public final boolean isAssociationInitiator()
    {
        return associationInitiator;
    }

    /**
     * Set whether or not this network AE can initiate associations.
     * 
     * @param initiator A boolean value. True if the Network AE can accept
     *            associations, false otherwise.
     */
    public final void setAssociationInitiator(boolean initiator)
    {
        this.associationInitiator = initiator;
    }

    /**
     * Get the description of this network AE
     * 
     * @return A String containing the description.
     */
    public final String getDescription()
    {
        return description;
    }

    /**
     * Set a description of this network AE.
     * 
     * @param description A String containing the description.
     */
    public final void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Determine whether or not this network AE is installed on a network.
     * 
     * @return A Boolean value. True if the AE is installed on a network. If not
     *         present, information about the installed status of the AE is
     *         inherited from the device
     */
    public final boolean isInstalled()
    {
        return installed != null ? installed.booleanValue() : device == null
                || device.isInstalled();
    }

    /**
     * Set whether or not this network AE is installed on a network.
     * 
     * @param installed A Boolean value. True if the AE is installed on a
     *            network. If not present, information about the installed
     *            status of the AE is inherited from the device
     */
    public final void setInstalled(boolean installed)
    {
        this.installed = Boolean.valueOf(installed);
    }

    /**
     * Get the <code>NetworkConnection</code> objects associated with this
     * network AE.
     * 
     * @return An array of <code>NetworkConnection</code> objects.
     */
    public final NetworkConnection[] getNetworkConnection()
    {
        return networkConnection;
    }

    /**
     * Set the <code>NetworkConnection</code> object associated with this
     * network AE.
     * 
     * @param nc A <code>NetworkConnection</code> object.
     */
    public final void setNetworkConnection(NetworkConnection nc)
    {
        setNetworkConnection(new NetworkConnection[] { nc });
    }

    /**
     * Set the <code>NetworkConnection</code> objects associated with this
     * network AE.
     * 
     * @param nc An array of <code>NetworkConnection</code> objects.
     */
    public final void setNetworkConnection(NetworkConnection[] nc)
    {
        this.networkConnection = nc;
    }

    /**
     * Get the AE Title(s) (SCPs) that are preferred for initiating associations
     * from this network AE (SCU).
     * 
     * @return A String array of the preferred called AE titles.
     */
    public final String[] getPreferredCalledAETitle()
    {
        return preferredCalledAETitle;
    }

    /**
     * Determine whether or not this network AE (SCU) has an AE Title (SCP) that
     * is preferred for initiating associations to.
     * 
     * @return A boolean value. True if there is a preferred called AE title.
     */
    public final boolean hasPreferredCalledAETitle()
    {
        return preferredCalledAETitle != null
                && preferredCalledAETitle.length > 0;
    }

    /**
     * Determine whether or not the parameter is a preferred called AE title
     * (SCP) for this network AE (SCU). The called AE title defines authorized
     * SCPs that this network AE title may interact with.
     * 
     * @param aet A String containing the AE title to test.
     * @return A boolean value. True if the parameter is a preferred called AE
     *         title.
     */
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

    /**
     * Set the AE title(s) (SCPs) that are preferred for initiating associations
     * from this network AE (SCU). The called AE title(s) defines authorized
     * SCPs that this network AE title may interact with.
     * 
     * @param aets A String array containing the preferred called AE titles.
     */
    public final void setPreferredCalledAETitle(String[] aets)
    {
        this.preferredCalledAETitle = aets;
    }

    /**
     * Get the AE title(s) that are preferred for accepting associations from.
     * The calling AE title(s) defines authorized SCUs that this network AE
     * title may interact with, but does not prohibit other SCUs from making
     * associations.
     * 
     * @return A String array containing the preferred calling AE titles.
     */
    public final String[] getPreferredCallingAETitle()
    {
        return preferredCallingAETitle;
    }

    /**
     * Determine whether or not this network AE has a preferred calling AE
     * title. The calling AE title(s) defines authorized SCUs that this network
     * AE title may interact with, but does not prohibit other SCUs from making
     * associations.
     * 
     * @return A boolean value. True if there is a preferred AE title.
     */
    public final boolean hasPreferredCallingAETitle()
    {
        return preferredCallingAETitle != null
                && preferredCallingAETitle.length > 0;
    }

    /**
     * Determine whether or not the parameter is a preferred calling AE title
     * for accepting associations from. The calling AE title(s) defines
     * authorized SCUs that this network AE title may interact with, but does
     * not prohibit other SCUs from making associations.
     * 
     * @param aet A String containing the AE title to test.
     * @return A boolean value. True if the parameter is a preferred calling AE
     *         title.
     */
    public boolean isPreferredCallingAETitle(String aet)
    {
        return contains(preferredCallingAETitle, aet);
    }

    /**
     * Set the AE title(s) that are preferred for accepting associations from.
     * The calling AE title(s) defines authorized SCUs that this network AE
     * title may interact with, but does not prohibit other SCUs from making
     * associations.
     * 
     * @param aets A String array containing the preferred calling AE titles.
     */
    public final void setPreferredCallingAETitle(String[] aets)
    {
        this.preferredCallingAETitle = aets;
    }

    /**
     * Get the Character Set(s) supported by the Network AE for data sets it
     * receives. The value shall be selected from the Defined Terms for Specific
     * Character Set (0008,0005) in PS3.3. If no values are present, this
     * implies that the Network AE supports only the default character
     * repertoire (ISO IR 6).
     * 
     * @return A String array of the supported character sets.
     */
    public final String[] getSupportedCharacterSet()
    {
        return supportedCharacterSet;
    }

    /**
     * Set the Character Set(s) supported by the Network AE for data sets it
     * receives. The value shall be selected from the Defined Terms for Specific
     * Character Set (0008,0005) in PS3.3. If no values are present, this
     * implies that the Network AE supports only the default character
     * repertoire (ISO IR 6).
     * 
     * @param characterSets A String array of the supported character sets.
     */
    public final void setSupportedCharacterSet(String[] characterSets)
    {
        this.supportedCharacterSet = characterSets;
    }

    /**
     * Get the transfer capabilities (presentation contexts, extended
     * information, etc.) that this network AE may make use of.
     * 
     * @return An array of <code>TransferCapability</code> objects.
     */
    public final TransferCapability[] getTransferCapability()
    {
        return transferCapability;
    }

    /**
     * Set the transfer capabilities (presentation contexts, extended
     * information, etc.) that this network AE may make use of.
     * 
     * @param transferCapability An array of <code>TransferCapability</code>
     *            objects.
     */
    public final void setTransferCapability(
            TransferCapability[] transferCapability)
    {
        this.transferCapability = transferCapability;
    }

    /**
     * Get any vendor information or configuration specific to this network AE.
     * 
     * @return An Object array of the vendor data.
     */
    public final Object[] getVendorData()
    {
        return vendorData;
    }

    /**
     * Set any vendor information or configuration specific to this network AE
     * 
     * @param vendorData An Object array of the vendor data.
     */
    public final void setVendorData(Object[] vendorData)
    {
        this.vendorData = vendorData;
    }

    /**
     * Get maximum number of outstanding operations this network AE may invoke
     * asynchronously as an SCU. Default is 0 (unlimited).
     * 
     * @return An int value containing the max ops.
     */
    public final int getMaxOpsInvoked()
    {
        return maxOpsInvoked;
    }

    /**
     * Set maximum number of outstanding operations this network AE may invoke
     * asynchronously as an SCU. Default is 0 (unlimited).
     * 
     * @param maxOpsInvoked An int value containing the max ops.
     */
    public final void setMaxOpsInvoked(int maxOpsInvoked)
    {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    /**
     * Get the maximum number of operation this network AE may perform
     * asynchronously as an SCP. Default is 0 (unlimited).
     * 
     * @return An int value containing the max ops.
     */
    public final int getMaxOpsPerformed()
    {
        return maxOpsPerformed;
    }

    /**
     * Set the maximum number of operation this network AE may perform
     * asynchronously as an SCP. Default is 0 (unlimited).
     * 
     * @param maxOpsPerformed An int value containing the max ops.
     */
    public final void setMaxOpsPerformed(int maxOpsPerformed)
    {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    /**
     * Determine whether or not this network AE is capable of asynchronous
     * operations.
     * 
     * @return A boolean value. True if this network AE is capable of async
     *         operations.
     */
    public final boolean isAsyncOps()
    {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }

    /**
     * Get the maximum length (in bytes) of P-DATA-TF PDUs that can be received.
     * Defaults to 16364.
     * 
     * @return An int signifying the max PDU length.
     */
    public final int getMaxPDULengthReceive()
    {
        return maxPDULengthReceive;
    }

    /**
     * Get the maximum length (in bytes) of P-DATA-TF PDUs that can be received.
     * Defaults to 16364.
     * 
     * @param maxPDULengthReceive An int signifying the max PDU length.
     */
    public final void setMaxPDULengthReceive(int maxPDULengthReceive)
    {
        this.maxPDULengthReceive = maxPDULengthReceive;
    }

    /**
     * Set the maximum length (in bytes) of P-DATA-TF PDUs that will be sent.
     * Defaults to 16364.
     * 
     * @return An int signifying the max PDU length.
     */
    public final int getMaxPDULengthSend()
    {
        return maxPDULengthSend;
    }

    /**
     * Get the maximum length (in bytes) of P-DATA-TF PDUs that will be sent.
     * Defaults to 16364.
     * 
     * @param maxPDULengthSend An int signifying the max PDU length.
     */
    public final void setMaxPDULengthSend(int maxPDULengthSend)
    {
        this.maxPDULengthSend = maxPDULengthSend;
    }

    /**
     * Get whether or not this network AE will send only one PDV in one
     * P-Data-TF PDU. Defaults to false.
     * 
     * @return A boolean value. If true, this network AE will pack command and
     *         data PDV in one P-DATA-TF PDU when sending.
     */
    public final boolean isPackPDV()
    {
        return packPDV;
    }

    /**
     * Set whether or not to send only one PDV in one P-Data-TF PDU. Defaults to
     * false.
     * 
     * @param packPDV A boolean value. If true, this network AE will pack
     *            command and data PDV in one P-DATA-TF PDU when sending.
     */
    public final void setPackPDV(boolean packPDV)
    {
        this.packPDV = packPDV;
    }

    /**
     * Get the timeout in milliseconds for receiving DIMSE-RSP on an open
     * association. Default 60 seconds (60000 milliseconds).
     * 
     * @return An int value signifying the timeout in milliseconds.
     */
    public final int getDimseRspTimeout()
    {
        return dimseRspTimeout;
    }

    /**
     * Set the timeout in milliseconds for receiving DIMSE-RSP on an open
     * association. Default 60 seconds (60000 milliseconds).
     * 
     * @param dimseRspTimeout An int value signifying the timeout in
     *            milliseconds.
     */
    public final void setDimseRspTimeout(int dimseRspTimeout)
    {
        this.dimseRspTimeout = dimseRspTimeout;
    }

    /**
     * Get the maximum time in milliseconds that an association may remain idle.
     * Default 60 seconds (60000 milliseconds).
     * 
     * @return An int value signifying the max idle period in milliseconds.
     */
    public final int getIdleTimeout()
    {
        return idleTimeout;
    }

    /**
     * Set the maximum time in milliseconds that an association may remain idle.
     * Default 60 seconds (60000 milliseconds).
     * 
     * @param dimseRspTimeout An int value signifying the max idle period in
     *            milliseconds.
     */
    public final void setIdleTimeout(int idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Get the timeout in milliseconds for receiving DIMSE-RSP on an open C-MOVE
     * association. Other types of associations use the DimseRspTimeout. Default
     * 60 seconds (60000 milliseconds).
     * 
     * @return An int value signifying the timeout in milliseconds.
     */
    public final int getMoveRspTimeout()
    {
        return moveRspTimeout;
    }

    /**
     * Set the timeout in milliseconds for receiving DIMSE-RSP on an open C-MOVE
     * association. Other types of associations use the DimseRspTimeout. Default
     * 60 seconds (60000 milliseconds).
     * 
     * @param dimseRspTimeout An int value signifying the timeout in
     *            milliseconds.
     */
    public final void setMoveRspTimeout(int moveRspTimeout)
    {
        this.moveRspTimeout = moveRspTimeout;
    }

    /**
     * Get an array of AE titles. If there is an open association from an AE
     * title (the SCU) contained in this array, this network AE (the SCP) will
     * reuse an existing association object as opposed to creating a new one.
     * 
     * @return A String array containing the AE titles that association reuse
     *         will be enabled for.
     */
    public final String[] getReuseAssocationFromAETitle()
    {
        return reuseAssocationFromAETitle;
    }

    /**
     * Set an array of AE titles. If there is an open association from an AE
     * title (the SCU) contained in this array, this network AE (the SCP) will
     * reuse an existing association object as opposed to creating a new one.
     * 
     * @param reuseAssocationFromAETitle String array containing the AE titles
     *            that association reuse will be enabled for.
     */
    public final void setReuseAssocationFromAETitle(
            String[] reuseAssocationFromAETitle)
    {
        this.reuseAssocationFromAETitle = reuseAssocationFromAETitle;
    }

    /**
     * Get an array of AE titles. If there is an open association to an AE title
     * (the SCP) contained in this array, this network AE (the SCU) will reuse
     * an existing association object as opposed to creating a new one.
     * 
     * @return String array containing the AE titles that association reuse will
     *         be enabled for.
     */
    public final String[] getReuseAssocationToAETitle()
    {
        return reuseAssocationToAETitle;
    }

    /**
     * Get an array of AE titles. If there is an open association to an AE title
     * (the SCP) contained in this array, this network AE (the SCU) will reuse
     * an existing association object as opposed to creating a new one.
     * 
     * @param reuseAssocationToAETitle String array containing the AE titles
     *            that association reuse will be enabled for.
     */
    public final void setReuseAssocationToAETitle(
            String[] reuseAssocationToAETitle)
    {
        this.reuseAssocationToAETitle = reuseAssocationToAETitle;
    }

    /**
     * Open a connection to the remote AE, using the passed in threading model.
     * This method will result in an association being opened (or re-used if so
     * configured). This association is then returned for use.
     * 
     * @param remoteAE A <code>NetworkApplicationEntity</code> to connect to.
     * @param executor An <code>Executor</code> implementation containing the
     *            threading model to use for this connection/association.
     * @return An open <code>Association</code> object.
     * @throws ConfigurationException If there is no compatible network
     *             connection between this AE title and the one that it is
     *             connecting to.
     * @throws IOException
     * @throws InterruptedException
     */
    public Association connect(NetworkApplicationEntity remoteAE,
            Executor executor) throws ConfigurationException, IOException,
            InterruptedException
    {
        return connect(remoteAE, executor, false);
    }

    /**
     * Open a connection to the remote AE, using the passed in threading model.
     * This method will result in an association being opened (or reused if so
     * configured, and the "forceNew" parameter is false). This association is
     * then returned for use.
     * 
     * @param remoteAE A <code>NetworkApplicationEntity</code> to connect to.
     * @param executor An <code>Executor</code> implementation containing the
     *            threading model to use for this connection/association.
     * @param forceNew A boolean value. If true, always create a new
     *            association, ignoring any existing association re-use
     *            configuration that has been set.
     * @return An open <code>Association</code> object.
     * @throws ConfigurationException If there is no compatible network
     *             connection between this AE title and the one that it is
     *             connecting to.
     * @throws IOException
     * @throws InterruptedException
     */
    public Association connect(NetworkApplicationEntity remoteAE,
            Executor executor, boolean forceNew) throws ConfigurationException,
            IOException, InterruptedException
    {
        final String remoteAET = remoteAE.getAETitle();
        if (!forceNew
                && !pool.isEmpty()
                && (reuseAssocationToAETitle.length > 0 || reuseAssocationFromAETitle.length > 0))
        {
            final boolean reuseAssocationTo = Arrays.asList(
                    reuseAssocationToAETitle).indexOf(remoteAET) != -1;
            final boolean reuseAssocationFrom = Arrays.asList(
                    reuseAssocationFromAETitle).indexOf(remoteAET) != -1;
            synchronized (pool)
            {
                for (Iterator iter = pool.iterator(); iter.hasNext();)
                {
                    Association as = (Association) iter.next();
                    if (!remoteAET.equals(as.getRemoteAET()))
                        continue;
                    if (as.isReadyForDataTransfer()
                            && (as.isRequestor() ? reuseAssocationTo
                                    : reuseAssocationFrom))
                        return as;
                }
            }
        }
        NetworkConnection[] remoteConns = remoteAE.getNetworkConnection();
        for (int i = 0; i < networkConnection.length; i++)
        {
            NetworkConnection c = networkConnection[i];
            if (!networkConnection[i].isInstalled())
                continue;
            for (int j = 0; j < remoteConns.length; j++)
            {
                NetworkConnection nc = remoteConns[j];
                if (nc.isInstalled() && nc.isListening()
                        && c.isTLS() == nc.isTLS())
                {
                    AAssociateRQ rq = makeAAssociateRQ(remoteAE);
                    Socket s = c.connect(nc);
                    Association a = Association.request(s, c, this);
                    executor.execute(a);
                    a.negotiate(rq);
                    addToPool(a);
                    return a;
                }
            }
        }
        throw new ConfigurationException(
                "No compatible Network Connection between local AE " + aeTitle
                        + " and remote AE " + remoteAET);
    }

    private AAssociateRQ makeAAssociateRQ(NetworkApplicationEntity remoteAE)
            throws ConfigurationException
    {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCallingAET(aeTitle);
        aarq.setCalledAET(remoteAE.getAETitle());
        aarq.setMaxPDULength(maxPDULengthReceive);
        aarq.setMaxOpsInvoked(maxOpsInvoked);
        aarq.setMaxOpsPerformed(maxOpsPerformed);

        LinkedHashMap as2ts = new LinkedHashMap();
        HashSet asscp = new LinkedHashSet();
        HashSet asscu = new HashSet();
        TransferCapability[] remoteTCs = remoteAE.getTransferCapability();
        for (int i = 0; i < transferCapability.length; i++)
        {
            TransferCapability localTC = transferCapability[i];
            String cuid = localTC.getSopClass();
            List ts = Arrays.asList(localTC.getTransferSyntax());
            // consider Transfer Capabilities of Remote AE if available
            if (remoteTCs.length != 0)
            {
                TransferCapability remoteTC = findTC(remoteTCs, cuid, localTC
                        .isSCU());
                if (remoteTC == null)
                    continue;
                ts.retainAll(Arrays.asList(localTC.getTransferSyntax()));
            }
            List prevTS = (List) as2ts.get(cuid);
            if (prevTS == null)
            {
                as2ts.put(cuid, ts);
            } else
            {
                for (Iterator iter = ts.iterator(); iter.hasNext();)
                {
                    String tsuid = (String) iter.next();
                    if (!prevTS.contains(tsuid))
                        prevTS.add(ts);
                }
            }
            (localTC.isSCP() ? asscp : asscu).add(cuid);
            byte[] extInfo = localTC.getExtInfo();
            if (extInfo != null)
            {
                ExtendedNegotiation extneg = new ExtendedNegotiation(cuid,
                        extInfo);
                aarq.addExtendedNegotiation(extneg);
            }
        }
        if (as2ts.isEmpty())
            throw new ConfigurationException(
                    "No common Transfer Capability between local AE "
                            + getAETitle() + " and remote AE "
                            + remoteAE.getAETitle());
        int available = 128 - as2ts.size();
        int pcid = 1;
        for (Iterator iter = as2ts.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry e = (Map.Entry) iter.next();
            String asuid = (String) e.getKey();
            List ts = (List) e.getValue();
            int expand = Math.min(available, ts.size() - 1);
            PresentationContext pc = new PresentationContext();
            pc.setAbstractSyntax(asuid);
            for (Iterator it = ts.iterator(); it.hasNext(); --expand)
            {
                if (expand > 0)
                {
                    PresentationContext pc1 = new PresentationContext();
                    pc1.setPCID(pcid);
                    pc1.setAbstractSyntax(asuid);
                    pc1.addTransferSyntax((String) it.next());
                    aarq.addPresentationContext(pc1);
                    ++pcid;
                    ++pcid;
                } else
                {
                    pc.addTransferSyntax((String) it.next());
                }
            }
            pc.setPCID(pcid);
            aarq.addPresentationContext(pc);
            ++pcid;
            ++pcid;
        }
        for (Iterator iter = asscp.iterator(); iter.hasNext();)
        {
            String cuid = (String) iter.next();
            aarq.addRoleSelection(new RoleSelection(cuid, asscu.contains(cuid),
                    true));
        }
        return aarq;
    }

    private TransferCapability findTC(TransferCapability[] tcs, String cuid,
            boolean scp)
    {
        TransferCapability tc;
        for (int i = 0; i < tcs.length; i++)
        {
            tc = tcs[i];
            if (tc.isSCP() == scp && tc.getSopClass().equals(cuid))
                return tc;
        }
        return null;
    }

    /**
     * Register a <code>DicomService</code> with this network AE.
     * 
     * @param service The <code>DicomService</code> that will respond to DICOM
     *            requests.
     */
    public void register(DicomService service)
    {
        serviceRegistry.register(service);
    }

    /**
     * Unregister (remove) a <code>DicomService</code> from this network AE.
     * 
     * @param service The <code>DicomService</code> to unregister.
     */
    public void unregister(DicomService service)
    {
        serviceRegistry.unregister(service);
    }

    /**
     * Add the given <code>Association</code> object to this network AE's pool
     * of associations.
     * 
     * @param a The <code>Association</code> to add.
     */
    void addToPool(Association a)
    {
        synchronized (pool)
        {
            pool.add(a);
        }
    }

    /**
     * Remove the given <code>Association</code> object from this network AE's
     * pool of associations.
     * 
     * @param a The <code>Association</code> to remove.
     */
    void removeFromPool(Association a)
    {
        synchronized (pool)
        {
            pool.remove(a);
        }
    }

    /**
     * Perform the action associated with the given DICOM command object.
     * 
     * @param as The <code>Association</code> to perform the operation within.
     * @param pcid The presentation context ID for this operation.
     * @param cmd The <code>DicomObject</code> representing the command to
     *            execute.
     * @param dataStream The <code>PDVInputStream</code> used to interpret the
     *            incoming/outgoing PDUs.
     * @param tsuid A String containing the transfer syntax that will be used in
     *            this operation.
     * @throws IOException
     */
    void perform(Association as, int pcid, DicomObject cmd,
            PDVInputStream dataStream, String tsuid) throws IOException
    {
        serviceRegistry.process(as, pcid, cmd, dataStream, tsuid);
    }

    /**
     * Negotiate a DICOM association as an SCP.
     * 
     * @param a The <code>Association</code> object containing the SCU and
     *            network information.
     * @param rq The <code>AAssociationRQ</code> object that was created when
     *            the <code>PDVInputStream</code> sensed an association
     *            request.
     * @return An <code>AAssociateAC</code> response object.
     * @throws AAssociateRJ Thrown if the association request is rejected.
     */
    AAssociateAC negotiate(Association a, AAssociateRQ rq) throws AAssociateRJ
    {
        if (!isAssociationAcceptor())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_NO_REASON_GIVEN);
        String[] calling = getPreferredCallingAETitle();
        if (calling.length != 0
                && Arrays.asList(calling).indexOf(rq.getCallingAET()) == -1)
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        if (!isInstalled())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_NO_REASON_GIVEN);
        AAssociateAC ac = new AAssociateAC();
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        ac.setMaxPDULength(maxPDULengthReceive);
        ac
                .setMaxOpsInvoked(minZeroAsMax(rq.getMaxOpsInvoked(),
                        maxOpsPerformed));
        ac.setMaxOpsPerformed(minZeroAsMax(rq.getMaxOpsPerformed(),
                maxOpsInvoked));
        Collection pcs = rq.getPresentationContexts();
        for (Iterator iter = pcs.iterator(); iter.hasNext();)
        {
            PresentationContext rqpc = (PresentationContext) iter.next();
            PresentationContext acpc = negPresCtx(rq, ac, rqpc);
            ac.addPresentationContext(acpc);
        }
        return ac;
    }

    private PresentationContext negPresCtx(AAssociateRQ rq, AAssociateAC ac, 
            PresentationContext rqpc) {
        String asuid = rqpc.getAbstractSyntax();
        RoleSelection rqrs = rq.getRoleSelectionFor(asuid);
        TransferCapability tcscp = findTC(transferCapability, asuid, true);
        TransferCapability tcscu = findTC(transferCapability, asuid, false);
        if (rqrs != null && ac.getRoleSelectionFor(asuid) != null) {
            boolean scp = rqrs.isSCP() && tcscu != null;
            boolean scu = rqrs.isSCU() && tcscp != null;
            RoleSelection rs = new RoleSelection(asuid, scu , scp);
            ac.addRoleSelection(rs);            
        }
        TransferCapability tc = rqrs == null || rqrs.isSCU() ? tcscp : tcscu;
        
        PresentationContext acpc = new PresentationContext();
        acpc.setPCID(rqpc.getPCID());            
        if (tc != null) {
            Set rqts = rqpc.getTransferSyntaxes();
            String[] acts = tc.getTransferSyntax();
            for (int i = 0; i < acts.length; i++) {
                if (rqts.contains(acts[i])) {
                    acpc.addTransferSyntax(acts[i]);
                    if (ac.getExtendedNegotiationFor(asuid) == null) {
                        ExtendedNegotiation extNeg = 
                            tc.negotiate(rq.getExtendedNegotiationFor(asuid));
                        if (extNeg != null)
                            ac.addExtendedNegotiation(extNeg);
                    }
                    return acpc;
                }
            }
            acpc.setResult(PresentationContext.TRANSFER_SYNTAX_NOT_SUPPORTED);          
        } else {
            acpc.setResult(PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED);            
        }
        acpc.addTransferSyntax(rqpc.getTransferSyntax());
        return acpc;
    }    

    private int minZeroAsMax(int i1, int i2)
    {
        return i1 == 0 ? i2 : i2 == 0 ? i1 : Math.min(i1, i2);
    }
}
