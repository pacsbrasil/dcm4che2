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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.dcm4che.conf.ConfigInfo;
import org.dcm4che.conf.DeviceInfo;
import org.dcm4che.conf.JavaObjectInfo;
import org.dcm4che.conf.NetworkAEInfo;
import org.dcm4che.conf.NetworkConnectionInfo;
import org.dcm4che.conf.NodeCertificateInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 05.09.2003
 */
public class DeviceLdapMapper extends ConfigLdapMapper {

    public DeviceLdapMapper(InitialDirContext ctx, String baseDN) {
        super(ctx, baseDN);
    }

    public DeviceInfo load(String dn) throws NamingException {
        DeviceInfo device =
            (DeviceInfo) load(dn,
                new DeviceInfo(),
                new String[] {
                    DICOM_DEVICE_NAME,
                    DICOM_DESCRIPTION,
                    DICOM_MANUFACTURER,
                    DICOM_MANUFACTURER_MODEL_NAME,
                    DICOM_VERSION,
                    DICOM_VENDOR_DATA,
                    DICOM_PRIMARY_DEVICE_TYPE,
                    DICOM_RELATED_DEVICE_REFERENCE,
                    DICOM_AUTHORIZED_NODE_CERTIFICATE_REFERENCE,
                    DICOM_THIS_NODE_CERTIFICATE_REFERENCE,
                    DICOM_INSTALLED });

        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningAttributes(new String[0]);
        ctls.setReturningObjFlag(false);
        NetworkConnectionLdapMapper ncMapper =
            new NetworkConnectionLdapMapper(ctx, baseDN);
        for (NamingEnumeration ne =
            ctx.search(dn, MATCH_NETWORK_CONNECTION, ctls);
            ne.hasMore();
            ) {
            SearchResult sr = (SearchResult) ne.next();
            device.addNetworkConnection(ncMapper.load(sr.getName() + "," + dn));
        }

        NetworkAELdapMapper aeMapper = new NetworkAELdapMapper(ctx, baseDN);
        aeMapper.setDevice(device);
        for (NamingEnumeration ne = ctx.search(dn, MATCH_NETWORK_AE, ctls);
            ne.hasMore();
            ) {
            SearchResult sr = (SearchResult) ne.next();
            device.addNetworkAE(aeMapper.load(sr.getName() + "," + dn));
        }

        return device;
    }

    public DeviceInfo findDeviceByName(String name) throws NamingException {
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(1);
        //        ctls.setReturningAttributes(new String[0]);
        //        ctls.setReturningObjFlag(false);
        String filter =
            MessageFormat.format(MATCH_DEVICE_BY_NAME, new String[] { name });
        NamingEnumeration answer =
            ctx.search(getDicomDevicesDN(), filter, ctls);
        if (!answer.hasMore()) {
            throw new NameNotFoundException();
        }
        SearchResult sr = (SearchResult) answer.next();
        return load(sr.getName() + "," + getDicomDevicesDN());
    }

    public DeviceInfo findDeviceByAET(String aet) throws NamingException {
        String dn = findNetworkAEDNByAET(aet);
        return load(dn.substring(dn.indexOf(',') + 1));
    }

    public void store(String parentDN, DeviceInfo device)
        throws NamingException {
        store(parentDN, device, new String[] { TOP, DICOM_DEVICE, PKI_USER });
        storeRelatedDeviceDescription(device.getRelatedDeviceDescription());
        storeNetworkConnections(device.getNetworkConnection(), device.getDN());
        storeNetworkAE(device.getNetworkAE(), device.getDN());
    }

    private void storeRelatedDeviceDescription(JavaObjectInfo[] infos)
        throws NamingException {
        JavaObjectLdapMapper joMapper = new JavaObjectLdapMapper(ctx, baseDN);
        for (int i = 0; i < infos.length; i++) {
            joMapper.store(infos[i]);
        }
    }

    private void storeNetworkAE(NetworkAEInfo[] ae, String deviceDN)
        throws NamingException {
        NetworkAELdapMapper aeMapper = new NetworkAELdapMapper(ctx, baseDN);
        for (int i = 0; i < ae.length; i++) {
            aeMapper.store(deviceDN, ae[i]);
        }
    }

    private void storeNetworkConnections(
        NetworkConnectionInfo[] nc,
        String deviceDN)
        throws NamingException {
        NetworkConnectionLdapMapper ncMapper =
            new NetworkConnectionLdapMapper(ctx, baseDN);
        for (int i = 0; i < nc.length; i++) {
            ncMapper.store(deviceDN, nc[i]);
        }
    }

    protected void putAttributes(Attributes attrs, ConfigInfo info)
        throws NamingException {
        DeviceInfo device = (DeviceInfo) info;
        putAttribute(attrs, DICOM_DEVICE_NAME, device.getDeviceName());
        putAttribute(attrs, DICOM_DESCRIPTION, device.getDescription());
        putAttribute(attrs, DICOM_MANUFACTURER, device.getManufacturer());
        putAttribute(
            attrs,
            DICOM_MANUFACTURER_MODEL_NAME,
            device.getManufacturerModelName());
        putAttribute(attrs, DICOM_VERSION, device.getVersions());
        putAttribute(attrs, DICOM_VENDOR_DATA, device.getVendorData());
        putAttribute(
            attrs,
            DICOM_PRIMARY_DEVICE_TYPE,
            device.getPrimaryDeviceType());
        putAttribute(
            attrs,
            DICOM_RELATED_DEVICE_REFERENCE,
            device.getRelatedDeviceDescription());
        putAttribute(
            attrs,
            DICOM_AUTHORIZED_NODE_CERTIFICATE_REFERENCE,
            device.getAuthorizedNodeCertificate());
        putThisNodeCertAttribute(attrs, device);
        putAttribute(attrs, DICOM_INSTALLED, new Boolean(device.isInstalled()));
    }

    private void putThisNodeCertAttribute(Attributes attrs, DeviceInfo device)
        throws NamingException {
        NodeCertificateInfo[] certInfo = device.getThisNodeCertificate();
        for (int i = 0; i < certInfo.length; i++) {
            if (certInfo[i].getDN() == null) {
                Attribute attr = new BasicAttribute(USER_CERTIFICATE);
                X509Certificate[] certs = certInfo[i].getCertificate();
                for (int j = 0; j < certs.length; j++) {
                    try {
                        attr.add(certs[j].getEncoded());
                    } catch (CertificateEncodingException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                attrs.put(attr);
                certInfo[i].setDN(device.getDN());
            }
        }
        putAttribute(attrs, DICOM_THIS_NODE_CERTIFICATE_REFERENCE, certInfo);
    }

    protected String makeRDN(ConfigInfo info) {
        return makeRDN((DeviceInfo) info);
    }

    public static String makeRDN(DeviceInfo device) {
        return DICOM_DEVICE_NAME + "=" + device.getDeviceName();
    }

    protected void setValue(ConfigInfo info, String attrID, Object value)
        throws NamingException {
        DeviceInfo device = (DeviceInfo) info;
        if (attrID.equalsIgnoreCase(DICOM_DEVICE_NAME))
            device.setDeviceName((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_DESCRIPTION))
            device.setDescription((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_MANUFACTURER))
            device.setManufacturer((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_MANUFACTURER_MODEL_NAME))
            device.setManufacturerModelName((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_VERSION))
            device.addVersion((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_VENDOR_DATA))
            device.addVendorData((byte[]) value);
        else if (attrID.equalsIgnoreCase(DICOM_PRIMARY_DEVICE_TYPE))
            device.addPrimaryDeviceType((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_INSTALLED))
            device.setInstalled(Boolean.valueOf((String) value).booleanValue());
        else if (attrID.equalsIgnoreCase(DICOM_RELATED_DEVICE_REFERENCE))
            device.addRelatedDeviceDescription(
                getRelatedDeviceDescription((String) value));
        else if (
            attrID.equalsIgnoreCase(
                DICOM_AUTHORIZED_NODE_CERTIFICATE_REFERENCE)) {
            device.addAuthorizedNodeCertificate(
                getNodeCertificate((String) value));
        } else if (
            attrID.equalsIgnoreCase(DICOM_THIS_NODE_CERTIFICATE_REFERENCE)) {
            device.addThisNodeCertificate(getNodeCertificate((String) value));
        }
    }

    private NodeCertificateInfo getNodeCertificate(String dn)
        throws NamingException {
        return new NodeCertificateLdapMapper(ctx, baseDN).load(dn);
    }

    private JavaObjectInfo getRelatedDeviceDescription(String dn)
        throws NamingException {
        return new JavaObjectLdapMapper(ctx, baseDN).load(dn);
    }

}
