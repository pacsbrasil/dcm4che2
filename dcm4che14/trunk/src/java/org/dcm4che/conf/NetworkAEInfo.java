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
 * Represents a Network AE.
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup67_fz.pdf">
 * DICOM Supplement 67 - Configuration Management.</a>
 *    
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 30.08.2003
 */
public class NetworkAEInfo extends ConfigInfo {

    /** Unique AE title for this Network AE */
    private String aeTitle;

    /** Unconstrained text description of the application entity. */
    private String description;

    /** AE specific vendor configuration information */
    private ArrayList vendorData = new ArrayList();

    /** Locally defined names for a subset of related applications. */
    private ArrayList clusters = new ArrayList();

    /** Peer AE Titles that are potential workflow partners for this AE. */
    private ArrayList peers = new ArrayList();

    /** True if the Network AE can accept associations, false otherwise. */
    private boolean acceptor;

    /** True if the Network AE can accept associations, false otherwise. */
    private boolean initiator;

    private ArrayList ncList = new ArrayList();
    private ArrayList tcList = new ArrayList();
    private Boolean installed = null;

    public String toString() {
        return getClass().getName()
            + "[\n\taeTitle="
            + aeTitle
            + "\n\tdescription="
            + description
            + "\n\tvendorData=#"
            + vendorData.size()
            + "\n\tclusters="
            + clusters
            + "\n\tpeers="
            + peers
            + "\n\tacceptor="
            + acceptor
            + "\n\tinitiator="
            + initiator
            + "\n\tinstalled="
            + installed
            + "\n\tnetworkConnection="
            + toString(ncList)
            + "\n\ttransferCapability="
            + toString(tcList)
            + "]";
    }

    public void setAETitle(String aeTitle) {
        if (aeTitle == null)
            throw new NullPointerException("aeTitle");

        String tmp = aeTitle.trim();
        if (tmp.length() == 0 || tmp.length() > 16)
            throw new IllegalArgumentException("aeTitle:" + aeTitle);

        this.aeTitle = tmp;
    }

    public String getAETitle() {
        return aeTitle;
    }

    public String[] getApplicationCluster() {
        return (String[]) clusters.toArray(new String[clusters.size()]);
    }

    public void addApplicationCluster(String cluster) {
        if (cluster == null)
            throw new NullPointerException("cluster");

        clusters.add(cluster);
    }

    public boolean removeApplicationCluster(String cluster) {
        return clusters.remove(cluster);
    }

    public boolean hasApplicationCluster() {
        return !clusters.isEmpty();
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

    public boolean isAssociationAcceptor() {
        return acceptor;
    }

    public void setAssociationAcceptor(boolean acceptor) {
        this.acceptor = acceptor;
    }

    public boolean isAssociationInitiator() {
        return initiator;
    }

    public void setAssociationInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getPeerAeTitle() {
        return (String[]) peers.toArray(new String[peers.size()]);
    }

    public void addPeerAeTitle(String peerAeTitle) {
        if (peerAeTitle == null)
            throw new NullPointerException("peerAeTitle");

        peers.add(peerAeTitle);
    }

    public boolean removePeerAeTitle(String peerAeTitle) {
        return peers.remove(peerAeTitle);
    }

    public boolean hasPeerAeTitle() {
        return !peers.isEmpty();
    }

    public NetworkConnectionInfo[] getNetworkConnection() {
        return (NetworkConnectionInfo[]) ncList.toArray(
            new NetworkConnectionInfo[ncList.size()]);
    }

    public void addNetworkConnection(NetworkConnectionInfo connInfo) {
        if (connInfo == null)
            throw new NullPointerException("connInfo");

        ncList.add(connInfo);
    }

    public boolean removeNetworkConnection(NetworkConnectionInfo connInfo) {
        return ncList.remove(connInfo);
    }

    public TransferCapabilityInfo[] getTransferCapability() {
        return (TransferCapabilityInfo[]) tcList.toArray(
            new TransferCapabilityInfo[tcList.size()]);
    }

    public void addTransferCapability(TransferCapabilityInfo capability) {
        if (capability == null)
            throw new NullPointerException("capability");

        tcList.add(capability);
    }

    public boolean removeTransferCapability(TransferCapabilityInfo capability) {
        return tcList.remove(capability);
    }

    public Boolean isInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public boolean isValid() {
        return aeTitle != null && !ncList.isEmpty() && !tcList.isEmpty();
    }

}
