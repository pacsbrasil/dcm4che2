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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.dcm4che.conf.DeviceInfo;
import org.dcm4che.conf.NetworkAEInfo;
import org.dcm4che.conf.NetworkConnectionInfo;
import org.dcm4che.conf.TransferCapabilityInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 12.09.2003
 */
public class LdapConfig {

    static final String OBJECTCLASS = "objectclass";
    static final String TOP = "top";
    static final String CONFIGURATION_ROOT = "dicomConfigurationRoot";
    static final String UNIQUE_AE_TITLES_REGISTRY_ROOT =
        "dicomUniqueAETitlesRegistryRoot";
    static final String UNIQUE_AE_TITLE = "dicomUniqueAETitle";
    static final String DEVICES_ROOT = "dicomDevicesRoot";
    static final String DEVICE = "dicomDevice";
    static final String NETWORK_AE = "dicomNetworkAE";
    static final String NETWORK_CONNECTION = "dicomNetworkConnection";
    static final String TRANSFER_CAPABILITY = "dicomTransferCapability";

    static final String COMMON_NAME = "cn";
    static final String DEVICE_NAME = "dicomDeviceName";
    static final String DESCRIPTION = "dicomDescription";
    static final String MANUFACTURER = "dicomManufacturer";
    static final String MANUFACTURER_MODEL_NAME = "dicomManufacturerModelName";
    static final String VERSION = "dicomVersion";
    static final String VENDOR_DATA = "dicomVendorData;binary";
    static final String AE_TITLE = "dicomAETitle";
    static final String NETWORK_CONNECTION_REFERENCE =
        "dicomNetworkConnectionReference";
    static final String APPLICATION_CLUSTER = "dicomApplicationCluster";
    static final String ASSOCIATION_INITIATOR = "dicomAssociationInitiator";
    static final String ASSOCIATION_ACCEPTOR = "dicomAssociationAcceptor";
    static final String HOSTNAME = "dicomHostname";
    static final String PORT = "dicomPort";
    static final String SOP_CLASS = "dicomSOPClass";
    static final String TRANSFER_ROLE = "dicomTransferRole";
    static final String TRANSFER_SYNTAX = "dicomTransferSyntax";
    static final String PRIMARY_DEVICE_TYPE = "dicomPrimaryDeviceType";
    static final String RELATED_DEVICE_REFERENCE =
        "dicomRelatedDeviceReference";
    static final String PEER_AE_TITLE = "dicomPeerAETitle";
    static final String TLS_CIPHER_SUITE = "dicomTLSCipherSuite";
    static final String AUTHORIZED_NODE_CERTIFICATE_REFERENCE =
        "dicomAuthorizedNodeCertificateReference";
    static final String THIS_NODE_CERTIFICATE_REFERENCE =
        "dicomThisNodeCertificateReference";
    static final String INSTALLED = "dicomInstalled";

    static final String USER_CERTIFICATE = "userCertificate;binary";

    static final String CONFIGURATION = "DICOM Configuration";
    static final String DEVICES = "Devices";
    static final String UNIQUE_AE_TITLES_REGISTRY = "Unique AE Titles Registry";

    static final String MATCH_CONFIGURATION =
        "(&(objectclass=dicomConfigurationRoot)(cn=DICOM Configuration))";
    static final String MATCH_NETWORK_AE = "(objectclass=dicomNetworkAE)";
    static final String MATCH_NETWORK_CONNECTION =
        "(objectclass=dicomNetworkConnection)";
    static final String MATCH_TRANSFER_CAPABILITY =
        "(objectclass=dicomTransferCapability)";

    static final String matchDeviceByName(String name) {
        return "(&(objectclass=dicomDevice)(dicomDeviceName=" + name + "))";
    }

    static final String matchNetworkAeByAet(String aet) {
        return "(&(objectclass=dicomNetworkAE)(dicomAETitle=" + aet + "))";
    }

    static final Object toString(Boolean b) {
        return toString(b.booleanValue());
    }

    static final Object toString(boolean b) {
        return b ? "TRUE" : "FALSE";
    }

    private String baseDN = "";
    private String host = "localhost";
    private String port = "389";
    private boolean createRelatedDeviceDescription = true;
    private InitialDirContext ctx = null;
    private String configurationDN = null;
    private String devicesDN = null;
    private String aetRegistryDN = null;

    public final String getBaseDN() {
        return baseDN;
    }

    public final void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public final String getHost() {
        return host;
    }

    public final void setHost(String host) {
        this.host = host;
    }

    public final String getPort() {
        return port;
    }

    public final void setPort(String port) {
        this.port = port;
    }

    public final boolean isCreateRelatedDeviceDescription() {
        return createRelatedDeviceDescription;
    }

    public final void setCreateRelatedDeviceDescription(boolean createRelatedDevice) {
        this.createRelatedDeviceDescription = createRelatedDevice;
    }

    public void connect() throws NamingException {
        ctx = new InitialDirContext(initEnv());
    }

    public void connect(String principal, String passwd)
        throws NamingException {
        ctx = new InitialDirContext(initEnv(principal, passwd));
    }

    public void close() {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                e.printStackTrace();
            }
            ctx = null;
        }
    }

    private Hashtable initEnv() {
        Hashtable env = new Hashtable();
        env.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port);
        return env;
    }

    private Hashtable initEnv(String principal, String passwd) {
        Hashtable env = initEnv();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, passwd);
        return env;
    }

    private void checkConnection() {
        if (ctx == null) {
            throw new IllegalStateException("No connection");
        }
    }

    public void createConfiguration(String parentDN) throws NamingException {
        checkConnection();
        if (configurationDN != null) {
            throw new IllegalStateException(
                "Existing Configuration:" + configurationDN);
        }
        try {
            configurationDN =
                createSubcontext(
                    parentDN,
                    COMMON_NAME,
                    CONFIGURATION,
                    new String[] { TOP, CONFIGURATION_ROOT });
            devicesDN =
                createSubcontext(
                    configurationDN,
                    COMMON_NAME,
                    DEVICES,
                    new String[] { TOP, DEVICES_ROOT });
            aetRegistryDN =
                createSubcontext(
                    configurationDN,
                    COMMON_NAME,
                    UNIQUE_AE_TITLES_REGISTRY,
                    new String[] { TOP, UNIQUE_AE_TITLES_REGISTRY_ROOT });
        } catch (NamingException ne) {
            try {
                if (devicesDN != null) {
                    ctx.destroySubcontext(devicesDN);
                }
                if (configurationDN != null) {
                    ctx.destroySubcontext(configurationDN);
                }
            } catch (Exception ignore) {}
            devicesDN = null;
            configurationDN = null;
            throw ne;
        }
    }

    private String createSubcontext(
        String parentDN,
        String attrId,
        String attrVal,
        String[] objclassIDs)
        throws NamingException {
        Attributes attrs = new BasicAttributes(true); // case-ignore
        Attribute objclass = new BasicAttribute(OBJECTCLASS);
        for (int i = 0; i < objclassIDs.length; i++) {
            objclass.add(objclassIDs[i]);
        }
        attrs.put(objclass);
        attrs.put(attrId, attrVal);
        String dn = attrId + "=" + attrVal + "," + parentDN;
        ctx.createSubcontext(dn, attrs).close();
        return dn;
    }

    public void findConfiguration() throws NamingException {
        checkConnection();
        try {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setCountLimit(1);
            ctls.setReturningAttributes(new String[0]);
            ctls.setReturningObjFlag(false);
            NamingEnumeration ne =
                ctx.search(baseDN, MATCH_CONFIGURATION, ctls);
            if (!ne.hasMore()) {
                throw new NameNotFoundException();
            }
            SearchResult sr = (SearchResult) ne.next();
            configurationDN = sr.getName() + "," + baseDN;
            devicesDN = "cn=Devices," + configurationDN;
            aetRegistryDN = "cn=Unique AE Titles Registry," + configurationDN;
            ((DirContext) ctx.lookup(devicesDN)).close();
            ((DirContext) ctx.lookup(aetRegistryDN)).close();
        } catch (NamingException e) {
            configurationDN = null;
            devicesDN = null;
            aetRegistryDN = null;
            throw e;
        }
    }

    private void prepare() throws NamingException {
        checkConnection();
        if (devicesDN == null) {
            findConfiguration();
        }
    }

    public void registerAET(String aet) throws NamingException {
        prepare();
        createSubcontext(
            aetRegistryDN,
            AE_TITLE,
            aet,
            new String[] { TOP, UNIQUE_AE_TITLE });
    }

    public void unregisterAET(String aet) throws NamingException {
        prepare();
        ctx.unbind(AE_TITLE + "=" + aet + "," + aetRegistryDN);
    }

    private String dnOfNetworkAE(String aet) throws NamingException {
        prepare();
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(1);
        ctls.setReturningAttributes(new String[0]);
        ctls.setReturningObjFlag(false);
        NamingEnumeration ne =
            ctx.search(devicesDN, matchNetworkAeByAet(aet), ctls);
        if (!ne.hasMore()) {
            throw new NameNotFoundException();
        }
        SearchResult sr = (SearchResult) ne.next();
        return sr.getName() + "," + devicesDN;
    }

    public NetworkAEInfo getNetworkAE(String aet) throws NamingException {
        return loadNetworkAE(dnOfNetworkAE(aet));
    }

    public DeviceInfo getDevice(String name)
        throws CertificateException, NamingException {
        prepare();
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setCountLimit(1);
        ctls.setReturningAttributes(new String[0]);
        ctls.setReturningObjFlag(false);
        NamingEnumeration answer =
            ctx.search(devicesDN, matchDeviceByName(name), ctls);
        if (!answer.hasMore()) {
            throw new NameNotFoundException();
        }
        SearchResult sr = (SearchResult) answer.next();
        return loadDevice(sr.getName() + "," + devicesDN);
    }

    public DeviceInfo getDeviceWithAET(String aet)
        throws CertificateException, NamingException {
        String dn = dnOfNetworkAE(aet);
        return loadDevice(dn.substring(dn.indexOf(',') + 1));
    }

    public void createDevice(DeviceInfo info, Map refDNs)
        throws NamingException {
        prepare();
        store(makeRDN(info) + "," + devicesDN, info, refDNs);
    }

    private DeviceInfo loadDevice(String dn)
        throws CertificateException, NamingException {
        final DeviceInfo device = new DeviceInfo();
        Attributes attribs =
            ctx.getAttributes(
                dn,
                new String[] {
                    DEVICE_NAME,
                    DESCRIPTION,
                    MANUFACTURER,
                    MANUFACTURER_MODEL_NAME,
                    VERSION,
                    VENDOR_DATA,
                    PRIMARY_DEVICE_TYPE,
                    RELATED_DEVICE_REFERENCE,
                    AUTHORIZED_NODE_CERTIFICATE_REFERENCE,
                    THIS_NODE_CERTIFICATE_REFERENCE,
                    INSTALLED });
        Attribute attr;
        if ((attr = attribs.get(DEVICE_NAME)) != null) {
            device.setDeviceName((String) attr.get());
        }
        if ((attr = attribs.get(DESCRIPTION)) != null) {
            device.setDescription((String) attr.get());
        }
        if ((attr = attribs.get(MANUFACTURER)) != null) {
            device.setManufacturer((String) attr.get());
        }
        if ((attr = attribs.get(MANUFACTURER_MODEL_NAME)) != null) {
            device.setManufacturerModelName((String) attr.get());
        }
        if ((attr = attribs.get(VERSION)) != null) {
            device.addVersion((String) attr.get());
        }
        if ((attr = attribs.get(VENDOR_DATA)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                device.addVendorData((byte[]) e.next());
            }
        }
        if ((attr = attribs.get(PRIMARY_DEVICE_TYPE)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                device.addPrimaryDeviceType((String) e.next());
            }
        }
        if ((attr = attribs.get(RELATED_DEVICE_REFERENCE)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                device.addRelatedDeviceDescription(
                    ctx.lookup((String) e.next()));
            }
        }
        if ((attr = attribs.get(AUTHORIZED_NODE_CERTIFICATE_REFERENCE))
            != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                new AddNodeCertificate() {
                    void doAdd(X509Certificate certificate) {
                        device.addAuthorizedNodeCertificate(certificate);
                    }
                }
                .execute((String) e.next());
            }
        }
        if ((attr = attribs.get(THIS_NODE_CERTIFICATE_REFERENCE)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                new AddNodeCertificate() {
                    void doAdd(X509Certificate certificate) {
                        device.addThisNodeCertificate(certificate);
                    }
                }
                .execute((String) e.next());
            }
        }
        if ((attr = attribs.get(INSTALLED)) != null) {
            device.setInstalled(
                Boolean.valueOf((String) attr.get()).booleanValue());
        }

        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningAttributes(new String[0]);
        ctls.setReturningObjFlag(false);
        for (NamingEnumeration ne =
            ctx.search(dn, MATCH_NETWORK_CONNECTION, ctls);
            ne.hasMore();
            ) {
            SearchResult sr = (SearchResult) ne.next();
            device.addNetworkConnection(
                loadNetworkConnection(sr.getName() + "," + dn));
        }

        for (NamingEnumeration ne = ctx.search(dn, MATCH_NETWORK_AE, ctls);
            ne.hasMore();
            ) {
            SearchResult sr = (SearchResult) ne.next();
            device.addNetworkAE(loadNetworkAE(sr.getName() + "," + dn));
        }

        return device;
    }

    private void store(String dn, DeviceInfo device, Map objDNs)
        throws NamingException {
        if (!device.isValid()) {
            throw new IllegalArgumentException(device.toString());
        }
        Attributes attrs = new BasicAttributes(true); // case-ignore
        {
            Attribute classAttr = new BasicAttribute(OBJECTCLASS);
            classAttr.add(TOP);
            classAttr.add(DEVICE);
            attrs.put(classAttr);
        }
        attrs.put(DEVICE_NAME, device.getDeviceName());
        if (device.getDescription() != null) {
            attrs.put(DESCRIPTION, device.getDescription());
        }
        if (device.getManufacturer() != null) {
            attrs.put(MANUFACTURER, device.getManufacturer());
        }
        if (device.getManufacturerModelName() != null) {
            attrs.put(
                MANUFACTURER_MODEL_NAME,
                device.getManufacturerModelName());
        }
        if (device.hasVersion()) {
            Attribute attr = new BasicAttribute(VERSION);
            String[] val = device.getVersions();
            for (int i = 0; i < val.length; i++) {
                attr.add(val[i]);
            }
            attrs.put(attr);
        }
        if (device.hasVendorData()) {
            Attribute byteAttr = new BasicAttribute(VENDOR_DATA);
            byte[][] byteVal = device.getVendorData();
            for (int i = 0; i < byteVal.length; i++) {
                byteAttr.add(byteVal[i]);
            }
            attrs.put(byteAttr);
        }
        if (device.hasPrimaryDeviceType()) {
            Attribute attr = new BasicAttribute(PRIMARY_DEVICE_TYPE);
            String[] val = device.getPrimaryDeviceType();
            for (int i = 0; i < val.length; i++) {
                attr.add(val[i]);
            }
            attrs.put(attr);
        }
        if (device.hasRelatedDeviceReference()) {
            Object[] rdd = device.getRelatedDeviceDescription();
            attrs.put(makeRefAttribute(RELATED_DEVICE_REFERENCE, rdd, objDNs));
            if (createRelatedDeviceDescription) {
                for (int i = 0; i < rdd.length; i++) {
                    String rddDN = (String) objDNs.get(rdd[i]);
                    ctx.bind(rddDN, rdd[i]);
                }
            }
        }
        if (device.hasAuthorizedNodeCertificate()) {
            X509Certificate[] certs = device.getAuthorizedNodeCertificate();
            attrs.put(
                makeRefAttribute(
                    AUTHORIZED_NODE_CERTIFICATE_REFERENCE,
                    certs,
                    objDNs));
        }
        if (device.hasThisNodeCertificate()) {
            X509Certificate[] certs = device.getThisNodeCertificate();
            attrs.put(
                makeRefAttribute(
                    THIS_NODE_CERTIFICATE_REFERENCE,
                    certs,
                    objDNs));
        }
        attrs.put(INSTALLED, toString(device.isInstalled()));
        ctx.createSubcontext(dn, attrs).close();

        IdentityHashMap ncDNs = new IdentityHashMap();
        NetworkConnectionInfo[] nc = device.getNetworkConnection();
        for (int i = 0; i < nc.length; i++) {
            NetworkConnectionInfo info = nc[i];
            ncDNs.put(info, store(makeRDN(info) + "," + dn, info));
        }
        NetworkAEInfo[] ae = device.getNetworkAE();
        for (int i = 0; i < ae.length; i++) {
            NetworkAEInfo info = ae[i];
            String storeAsChild = store(makeRDN(info) + "," + dn, info, ncDNs);
        }
    }

    private Attribute makeRefAttribute(
        String attrID,
        Object[] objs,
        Map objDNs) {
        Attribute attr = new BasicAttribute(attrID);
        Object refDn;
        for (int i = 0; i < objs.length; i++) {
            if (!((refDn = objDNs.get(objs[i])) instanceof String)) {
                throw new IllegalArgumentException(
                    "objDNs contains no DN of " + objs[i]);
            }
            attr.add(refDn);
        }
        return attr;
    }

    private abstract class AddNodeCertificate {

        abstract void doAdd(X509Certificate certificate);
        void execute(String dn) throws CertificateException, NamingException {
            Attributes attribs =
                ctx.getAttributes(dn, new String[] { USER_CERTIFICATE });
            Attribute attr = attribs.get(USER_CERTIFICATE);
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                doAdd(createCertificate((byte[]) e.next()));
            }
        }
    }

    private X509Certificate createCertificate(byte[] ba)
        throws CertificateException {
        return (X509Certificate) CertificateFactory
            .getInstance("X509")
            .generateCertificate(new ByteArrayInputStream(ba));
    }

    private static String makeRDN(DeviceInfo device) {
        return DEVICE_NAME + "=" + device.getDeviceName();
    }

    private NetworkAEInfo loadNetworkAE(String dn) throws NamingException {
        NetworkAEInfo aeInfo = new NetworkAEInfo();
        Attributes attribs =
            ctx.getAttributes(
                dn,
                new String[] {
                    AE_TITLE,
                    DESCRIPTION,
                    VENDOR_DATA,
                    APPLICATION_CLUSTER,
                    PEER_AE_TITLE,
                    ASSOCIATION_ACCEPTOR,
                    ASSOCIATION_INITIATOR,
                    NETWORK_CONNECTION_REFERENCE,
                    INSTALLED });
        Attribute attr;
        if ((attr = attribs.get(AE_TITLE)) != null) {
            aeInfo.setAETitle((String) attr.get());
        }
        if ((attr = attribs.get(DESCRIPTION)) != null) {
            aeInfo.setDescription((String) attr.get());
        }
        if ((attr = attribs.get(VENDOR_DATA)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                aeInfo.addVendorData((byte[]) e.next());
            }
        }
        if ((attr = attribs.get(APPLICATION_CLUSTER)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                aeInfo.addApplicationCluster((String) e.next());
            }
        }
        if ((attr = attribs.get(PEER_AE_TITLE)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                aeInfo.addPeerAeTitle((String) e.next());
            }
        }
        if ((attr = attribs.get(ASSOCIATION_ACCEPTOR)) != null) {
            aeInfo.setAssociationAcceptor(
                Boolean.valueOf((String) attr.get()).booleanValue());
        }
        if ((attr = attribs.get(ASSOCIATION_INITIATOR)) != null) {
            aeInfo.setAssociationInitiator(
                Boolean.valueOf((String) attr.get()).booleanValue());
        }
        if ((attr = attribs.get(NETWORK_CONNECTION_REFERENCE)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                aeInfo.addNetworkConnection(
                    loadNetworkConnection((String) e.next()));
            }
        }
        if ((attr = attribs.get(INSTALLED)) != null) {
            aeInfo.setInstalled(Boolean.valueOf((String) attr.get()));
        }

        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningAttributes(new String[0]);
        ctls.setReturningObjFlag(false);
        for (NamingEnumeration ne =
            ctx.search(dn, MATCH_TRANSFER_CAPABILITY, ctls);
            ne.hasMore();
            ) {
            SearchResult sr = (SearchResult) ne.next();
            aeInfo.addTransferCapability(
                loadTransferCapability(sr.getName() + "," + dn));
        }
        return aeInfo;
    }

    private String store(String dn, NetworkAEInfo aeInfo, Map objDNs)
        throws NamingException {
        if (objDNs == null) {
            throw new NullPointerException("objDNs");
        }
        if (!aeInfo.isValid()) {
            throw new IllegalArgumentException(aeInfo.toString());
        }
        Attributes attrs = new BasicAttributes(true); // case-ignore
        Attribute classAttr = new BasicAttribute(OBJECTCLASS);
        classAttr.add(TOP);
        classAttr.add(NETWORK_AE);
        attrs.put(classAttr);
        attrs.put(AE_TITLE, aeInfo.getAETitle());
        if (aeInfo.getDescription() != null) {
            attrs.put(DESCRIPTION, aeInfo.getDescription());
        }
        if (aeInfo.hasVendorData()) {
            Attribute byteAttr = new BasicAttribute(VENDOR_DATA);
            byte[][] byteVal = aeInfo.getVendorData();
            for (int i = 0; i < byteVal.length; i++) {
                byteAttr.add(byteVal[i]);
            }
            attrs.put(byteAttr);
        }
        if (aeInfo.hasApplicationCluster()) {
            Attribute attr = new BasicAttribute(APPLICATION_CLUSTER);
            String[] val = aeInfo.getApplicationCluster();
            for (int i = 0; i < val.length; i++) {
                attr.add(val[i]);
            }
            attrs.put(attr);
        }
        if (aeInfo.hasPeerAeTitle()) {
            Attribute attr = new BasicAttribute(PEER_AE_TITLE);
            String[] val = aeInfo.getPeerAeTitle();
            for (int i = 0; i < val.length; i++) {
                attr.add(val[i]);
            }
            attrs.put(attr);
        }
        attrs.put(
            ASSOCIATION_ACCEPTOR,
            toString(aeInfo.isAssociationAcceptor()));
        attrs.put(
            ASSOCIATION_INITIATOR,
            toString(aeInfo.isAssociationInitiator()));
        {
            Attribute attr = new BasicAttribute(NETWORK_CONNECTION_REFERENCE);
            NetworkConnectionInfo[] nc = aeInfo.getNetworkConnection();
            Object refDn;
            for (int i = 0; i < nc.length; i++) {
                if (!((refDn = objDNs.get(nc[i])) instanceof String)) {
                    throw new IllegalArgumentException(
                        "objDNs contains no DN of " + nc[i]);
                }
                attr.add(refDn);
            }
            attrs.put(attr);
        }
        if (aeInfo.isInstalled() != null) {
            attrs.put(INSTALLED, toString(aeInfo.isInstalled()));
        }
        ctx.createSubcontext(dn, attrs).close();

        TransferCapabilityInfo[] capabilities = aeInfo.getTransferCapability();
        for (int i = 0; i < capabilities.length; i++) {
            TransferCapabilityInfo capability = capabilities[i];
            store(makeRDN(capability) + "," + dn, capability);
        }
        return dn;
    }

    private static String makeRDN(NetworkAEInfo ae) {
        return AE_TITLE + "=" + ae.getAETitle();
    }

    private NetworkConnectionInfo loadNetworkConnection(String dn)
        throws NamingException {
        NetworkConnectionInfo con = new NetworkConnectionInfo();
        Attributes attribs =
            ctx.getAttributes(
                dn,
                new String[] {
                    COMMON_NAME,
                    HOSTNAME,
                    PORT,
                    TLS_CIPHER_SUITE,
                    INSTALLED });
        Attribute attr;
        if ((attr = attribs.get(COMMON_NAME)) != null) {
            con.setCommonName((String) attr.get());
        }
        if ((attr = attribs.get(HOSTNAME)) != null) {
            con.setHostname((String) attr.get());
        }
        if ((attr = attribs.get(PORT)) != null) {
            con.setPort(Integer.parseInt((String) attr.get()));
        }
        if ((attr = attribs.get(TLS_CIPHER_SUITE)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                con.addTlsCipherSuite((String) e.next());
            }
        }
        if ((attr = attribs.get(INSTALLED)) != null) {
            con.setInstalled(Boolean.valueOf((String) attr.get()));
        }
        return con;
    }

    private static String makeRDN(NetworkConnectionInfo con) {
        return (con.getCommonName() != null)
            ? (COMMON_NAME + "=" + con.getCommonName())
            : (HOSTNAME
                + "="
                + con.getHostname()
                + (con.isListening() ? ("+" + PORT + "=" + con.getPort()) : ""));
    }

    private String store(String dn, NetworkConnectionInfo con)
        throws NamingException {
        if (!con.isValid()) {
            throw new IllegalArgumentException(con.toString());
        }
        Attributes attrs = new BasicAttributes(true); // case-ignore
        Attribute classAttr = new BasicAttribute(OBJECTCLASS);
        classAttr.add(TOP);
        classAttr.add(NETWORK_CONNECTION);
        attrs.put(classAttr);
        if (con.getCommonName() != null) {
            attrs.put(COMMON_NAME, con.getCommonName());
        }
        attrs.put(HOSTNAME, con.getHostname());
        if (con.isListening()) {
            attrs.put(PORT, Integer.toString(con.getPort()));
        }
        if (con.isTLS()) {
            Attribute attr = new BasicAttribute(TLS_CIPHER_SUITE);
            String[] val = con.getTlsCipherSuite();
            for (int i = 0; i < val.length; i++) {
                attr.add(val[i]);
            }
            attrs.put(attr);
        }
        if (con.isInstalled() != null) {
            attrs.put(INSTALLED, toString(con.isInstalled()));
        }
        ctx.createSubcontext(dn, attrs).close();
        return dn;
    }

    private TransferCapabilityInfo loadTransferCapability(String dn)
        throws NamingException {
        TransferCapabilityInfo capability = new TransferCapabilityInfo();
        Attributes attribs =
            ctx.getAttributes(
                dn,
                new String[] {
                    COMMON_NAME,
                    SOP_CLASS,
                    TRANSFER_ROLE,
                    TRANSFER_SYNTAX });
        Attribute attr;
        if ((attr = attribs.get(COMMON_NAME)) != null) {
            capability.setCommonName((String) attr.get());
        }
        if ((attr = attribs.get(SOP_CLASS)) != null) {
            capability.setSopClass((String) attr.get());
        }
        if ((attr = attribs.get(TRANSFER_ROLE)) != null) {
            capability.setRole((String) attr.get());
        }
        if ((attr = attribs.get(TRANSFER_SYNTAX)) != null) {
            for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
                capability.addTransferSyntax((String) e.next());
            }
        }
        return capability;
    }

    private static String makeRDN(TransferCapabilityInfo capability) {
        return (capability.getCommonName() != null)
            ? (COMMON_NAME + "=" + capability.getCommonName())
            : (SOP_CLASS
                + "="
                + capability.getSopClass()
                + "+"
                + TRANSFER_ROLE
                + "="
                + capability.getRole());
    }

    private void store(String dn, TransferCapabilityInfo capability)
        throws NamingException {
        if (!capability.isValid()) {
            throw new IllegalArgumentException(capability.toString());
        }
        Attributes attrs = new BasicAttributes(true); // case-ignore
        Attribute classAttr = new BasicAttribute(OBJECTCLASS);
        classAttr.add(TOP);
        classAttr.add(TRANSFER_CAPABILITY);
        attrs.put(classAttr);
        if (capability.getCommonName() != null) {
            attrs.put(COMMON_NAME, capability.getCommonName());
        }
        attrs.put(SOP_CLASS, capability.getSopClass());
        attrs.put(TRANSFER_ROLE, capability.getRole());
        Attribute tsAttr = new BasicAttribute(TRANSFER_SYNTAX);
        String[] ts = capability.getTransferSyntax();
        for (int i = 0; i < ts.length; i++) {
            tsAttr.add(ts[i]);
        }
        attrs.put(tsAttr);
        ctx.createSubcontext(dn, attrs).close();
    }
}
