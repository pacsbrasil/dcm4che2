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
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.dcm4che.conf.ConfigInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 05.09.2003
 */
public abstract class ConfigLdapMapper {
    public static final String OBJECTCLASS = "objectclass";
    public static final String TOP = "top";
    public static final String PKI_USER = "pkiUser";
    public static final String DICOM_CONFIGURATION_ROOT =
        "dicomConfigurationRoot";
    public static final String DICOM_UNIQUE_AE_TITLES_REGISTRY_ROOT =
        "dicomUniqueAETitlesRegistryRoot";
    public static final String DICOM_UNIQUE_AE_TITLE = "dicomUniqueAETitle";
    public static final String DICOM_DEVICES_ROOT = "dicomDevicesRoot";
    public static final String DICOM_DEVICE = "dicomDevice";
    public static final String DICOM_NETWORK_AE = "dicomNetworkAE";
    public static final String DICOM_NETWORK_CONNECTION =
        "dicomNetworkConnection";
    public static final String DICOM_TRANSFER_CAPABILITY =
        "dicomTransferCapability";

    public static final String COMMON_NAME = "cn";
    public static final String DICOM_DEVICE_NAME = "dicomDeviceName";
    public static final String DICOM_DESCRIPTION = "dicomDescription";
    public static final String DICOM_MANUFACTURER = "dicomManufacturer";
    public static final String DICOM_MANUFACTURER_MODEL_NAME =
        "dicomManufacturerModelName";
    public static final String DICOM_VERSION = "dicomVersion";
    public static final String DICOM_VENDOR_DATA = "dicomVendorData;binary";
    public static final String DICOM_AE_TITLE = "dicomAETitle";
    public static final String DICOM_NETWORK_CONNECTION_REFERENCE =
        "dicomNetworkConnectionReference";
    public static final String DICOM_APPLICATION_CLUSTER =
        "dicomApplicationCluster";
    public static final String DICOM_ASSOCIATION_INITIATOR =
        "dicomAssociationInitiator";
    public static final String DICOM_ASSOCIATION_ACCEPTOR =
        "dicomAssociationAcceptor";
    public static final String DICOM_HOSTNAME = "dicomHostname";
    public static final String DICOM_PORT = "dicomPort";
    public static final String DICOM_SOP_CLASS = "dicomSOPClass";
    public static final String DICOM_TRANSFER_ROLE = "dicomTransferRole";
    public static final String DICOM_TRANSFER_SYNTAX = "dicomTransferSyntax";
    public static final String DICOM_PRIMARY_DEVICE_TYPE =
        "dicomPrimaryDeviceType";
    public static final String DICOM_RELATED_DEVICE_REFERENCE =
        "dicomRelatedDeviceReference";
    public static final String DICOM_PEER_AE_TITLE = "dicomPeerAETitle";
    public static final String DICOM_TLS_CIPHER_SUITE = "dicomTLSCipherSuite";
    public static final String DICOM_AUTHORIZED_NODE_CERTIFICATE_REFERENCE =
        "dicomAuthorizedNodeCertificateReference";
    public static final String DICOM_THIS_NODE_CERTIFICATE_REFERENCE =
        "dicomThisNodeCertificateReference";
    public static final String DICOM_INSTALLED = "dicomInstalled";

    public static final String USER_CERTIFICATE = "userCertificate;binary";

    protected static final String SEARCH_DICOM_CONFIGURATION =
        "(&(objectclass=dicomConfigurationRoot)(cn=DICOM Configuration))";
    protected static final String SEARCH_DEVICE_BY_NAME =
        "(&(objectclass=dicomDevice)(dicomDeviceName=%1))";

    protected final InitialDirContext ctx;
    protected String dicomConfigurationDN;

    protected ConfigLdapMapper(InitialDirContext ctx) {
        if (ctx == null)
            throw new NullPointerException("ctx");

        this.ctx = ctx;
    }

    public String getDicomConfigurationDN(String baseDN)
        throws NamingException {
        if (dicomConfigurationDN == null) {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration answer =
                ctx.search(baseDN, SEARCH_DICOM_CONFIGURATION, ctls);
            if (!answer.hasMore()) {
                throw new RuntimeException("DicomConfiguratio Not Found");
            }
            SearchResult sr = (SearchResult) answer.next();
            dicomConfigurationDN = sr.getName() + "," + baseDN;
        }
        return dicomConfigurationDN;
    }

    public String getDicomDevicesDN(String baseDN) throws NamingException {
        return "cn=Devices," + getDicomConfigurationDN(baseDN);
    }

    protected ConfigInfo load(String dn, ConfigInfo info, String[] attrIDs)
        throws NamingException {
        Attributes attribs = ctx.getAttributes(dn, attrIDs);
        info.setDN(dn);
        for (NamingEnumeration ae = attribs.getAll(); ae.hasMore();) {
            Attribute attr = (Attribute) ae.next();
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                setValue(info, attr.getID(), e.next());
            }
        }
        return info;
    }

    protected void store(
        String parentDN,
        ConfigInfo info,
        String[] objectclass)
        throws NamingException {
        if (parentDN == null) {
            throw new NullPointerException("parentDN");
        }
        if (!info.isValid()) {
            throw new IllegalArgumentException("info: " + info);
        }
        if (info.getDN() == null) {
            info.setDN(makeRDN(info) + "," + parentDN);
        }
        Attributes attrs = new BasicAttributes(true); // case-ignore
        Attribute objclass = new BasicAttribute(OBJECTCLASS);
        for (int i = 0; i < objectclass.length; i++) {
            objclass.add(objectclass[i]);
        }
        attrs.put(objclass);
        putAttributes(attrs, info);
        ctx.createSubcontext(info.getDN(), attrs).close();
    }

    protected abstract String makeRDN(ConfigInfo info);

    protected abstract void putAttributes(Attributes attrs, ConfigInfo info)
        throws NamingException;

    protected abstract void setValue(
        ConfigInfo info,
        String attrID,
        Object value)
        throws NamingException;

    protected static void putAttribute(
        Attributes attrs,
        String attrID,
        String value) {
        if (value != null)
            attrs.put(attrID, value);
    }

    protected static void putAttribute(
        Attributes attrs,
        String attrID,
        Boolean value) {
        if (value != null)
            attrs.put(attrID, value.booleanValue() ? "TRUE" : "FALSE");
    }

    protected static void putAttribute(
        Attributes attrs,
        String attrID,
        String[] values) {
        if (values.length > 0) {
            Attribute attr = new BasicAttribute(attrID);
            for (int i = 0; i < values.length; i++) {
                attr.add(values[i]);
            }
            attrs.put(attr);
        }
    }

    protected static void putAttribute(
        Attributes attrs,
        String attrID,
        byte[][] values) {
        if (values.length > 0) {
            Attribute attr = new BasicAttribute(attrID);
            for (int i = 0; i < values.length; i++) {
                attr.add(values[i]);
            }
            attrs.put(attr);
        }
    }

    protected static Attributes matchObjectClass(String objclass) {
        Attributes attrs = new BasicAttributes(true); // case-ignore
        attrs.put(OBJECTCLASS, objclass);
        return attrs;
    }

    protected void putAttribute(
        Attributes attrs,
        String attrID,
        ConfigInfo[] values)
        throws NamingException {
        if (values.length > 0) {
            Attribute attr = new BasicAttribute(attrID);
            for (int i = 0; i < values.length; i++) {
                attr.add(values[i].getDN());
            }
            attrs.put(attr);
        }
    }
}
