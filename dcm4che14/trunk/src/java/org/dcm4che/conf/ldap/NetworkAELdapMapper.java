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

package org.dcm4che.conf.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.dcm4che.conf.ConfigInfo;
import org.dcm4che.conf.NetworkAEInfo;
import org.dcm4che.conf.TransferCapabilityInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 05.09.2003
 */
public class NetworkAELdapMapper extends ConfigLdapMapper {

    private final NetworkConnectionLdapMapper ncMapper;
    
    public NetworkAELdapMapper(InitialDirContext ctx) {
        super(ctx);
        ncMapper = new NetworkConnectionLdapMapper(ctx);

    }

    public NetworkAEInfo load(String dn) throws NamingException {
        NetworkAEInfo aeInfo = (NetworkAEInfo) load(
            dn,
            new NetworkAEInfo(),
            new String[] {
                DICOM_AE_TITLE,
                DICOM_DESCRIPTION,
                DICOM_VENDOR_DATA,
                DICOM_APPLICATION_CLUSTER,
                DICOM_PEER_AE_TITLE,
                DICOM_ASSOCIATION_ACCEPTOR,
                DICOM_ASSOCIATION_INITIATOR,
                DICOM_NETWORK_CONNECTION_REFERENCE,
                DICOM_INSTALLED });
                
        TransferCapabilityLdapMapper tcMapper = new TransferCapabilityLdapMapper(ctx);                
        for (NamingEnumeration ne =
            ctx.search(dn, matchObjectClass(DICOM_TRANSFER_CAPABILITY), new String[0]);
            ne.hasMore();
            ) {
            SearchResult sr = (SearchResult) ne.next();
            aeInfo.addTransferCapability(tcMapper.load(sr.getName() + "," + aeInfo.getDN()));
        }
        return aeInfo;
    }

    public void store(String parentDN, NetworkAEInfo ae) throws NamingException {
        store(parentDN, ae, new String[] { TOP, DICOM_NETWORK_AE });
        storeTransferCapability(ae.getTransferCapability(), ae.getDN());
    }

    private void storeTransferCapability(TransferCapabilityInfo[] tc, String aeDN) throws NamingException {
        TransferCapabilityLdapMapper tcMapper = new TransferCapabilityLdapMapper(ctx);
        for (int i = 0; i < tc.length; i++) {
            tcMapper.store(aeDN, tc[i]);
        }
    }

    protected void putAttributes(Attributes attrs, ConfigInfo info)
        throws NamingException {
        NetworkAEInfo ae = (NetworkAEInfo) info;

        putAttribute(attrs, DICOM_AE_TITLE, ae.getAETitle());
        putAttribute(attrs, DICOM_DESCRIPTION, ae.getDescription());
        putAttribute(attrs, DICOM_VENDOR_DATA, ae.getVendorData());
        putAttribute(attrs, DICOM_APPLICATION_CLUSTER, ae.getApplicationCluster());
        putAttribute(attrs, DICOM_PEER_AE_TITLE, ae.getPeerAeTitle());
        putAttribute(attrs, DICOM_ASSOCIATION_ACCEPTOR, new Boolean(ae.isAssociationAcceptor()));
        putAttribute(attrs, DICOM_ASSOCIATION_INITIATOR, new Boolean(ae.isAssociationInitiator()));
        putAttribute(attrs, DICOM_NETWORK_CONNECTION_REFERENCE, ae.getNetworkConnection());
        putAttribute(attrs, DICOM_INSTALLED, ae.isInstalled());
    }

    protected String makeRDN(ConfigInfo info) {
        return makeRDN((NetworkAEInfo) info);
    }

    public static String makeRDN(NetworkAEInfo ae) {
        return DICOM_AE_TITLE + "=" + ae.getAETitle();
    }

    protected void setValue(ConfigInfo info, String attrID, Object value)
        throws NamingException {
        NetworkAEInfo ae = (NetworkAEInfo) info;
        if (attrID.equalsIgnoreCase(DICOM_AE_TITLE))
            ae.setAETitle((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_DESCRIPTION))
            ae.setDescription((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_VENDOR_DATA))
            ae.addVendorData((byte[]) value);
        else if (attrID.equalsIgnoreCase(DICOM_APPLICATION_CLUSTER))
            ae.addApplicationCluster((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_PEER_AE_TITLE))
            ae.addPeerAeTitle((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_ASSOCIATION_ACCEPTOR))
            ae.setAssociationAcceptor(
                Boolean.valueOf((String) value).booleanValue());
        else if (attrID.equalsIgnoreCase(DICOM_ASSOCIATION_INITIATOR))
            ae.setAssociationInitiator(
                Boolean.valueOf((String) value).booleanValue());
        else if (attrID.equalsIgnoreCase(DICOM_INSTALLED))
            ae.setInstalled(Boolean.valueOf((String) value));
        else if (attrID.equalsIgnoreCase(DICOM_NETWORK_CONNECTION_REFERENCE)) {
            ae.addNetworkConnection(ncMapper.load((String) value));
        }
    }
}
