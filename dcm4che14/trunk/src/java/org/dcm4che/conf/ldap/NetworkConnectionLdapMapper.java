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

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.dcm4che.conf.ConfigInfo;
import org.dcm4che.conf.NetworkConnectionInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 05.09.2003
 */
public class NetworkConnectionLdapMapper extends ConfigLdapMapper {

    public NetworkConnectionLdapMapper(InitialDirContext ctx, String baseDN) {
        super(ctx, baseDN);
    }

    public NetworkConnectionInfo load(String dn) throws NamingException {
        return (NetworkConnectionInfo) load(
            dn,
            new NetworkConnectionInfo(),
            new String[] {
                COMMON_NAME,
                DICOM_HOSTNAME,
                DICOM_PORT,
                DICOM_TLS_CIPHER_SUITE,
                DICOM_INSTALLED });
    }

    public void store(String parentDN, NetworkConnectionInfo con)
        throws NamingException {
        store(parentDN, con, new String[] { TOP, DICOM_NETWORK_CONNECTION });
    }

    protected void putAttributes(Attributes attrs, ConfigInfo info) {
        NetworkConnectionInfo con = (NetworkConnectionInfo) info;

        putAttribute(attrs, COMMON_NAME, con.getCommonName());
        putAttribute(attrs, DICOM_HOSTNAME, con.getHostname());
        if (con.isListening())
            putAttribute(attrs, DICOM_PORT, Integer.toString(con.getPort()));
        putAttribute(attrs, DICOM_TLS_CIPHER_SUITE, con.getTlsCipherSuite());
        putAttribute(attrs, DICOM_INSTALLED, con.isInstalled());
    }

    protected String makeRDN(ConfigInfo info) {
        return NetworkConnectionLdapMapper.makeRDN((NetworkConnectionInfo) info);
    }

    public static String makeRDN(NetworkConnectionInfo con) {
        return (con.getCommonName() != null)
            ? (COMMON_NAME + "=" + con.getCommonName())
            : (DICOM_HOSTNAME
                + "="
                + con.getHostname()
                + (con.isListening()
                    ? ("+" + DICOM_PORT + "=" + con.getPort())
                    : ""));
    }

    protected void setValue(ConfigInfo info, String attrID, Object value) {
        NetworkConnectionInfo con = (NetworkConnectionInfo) info;
        if (attrID.equalsIgnoreCase(COMMON_NAME))
            con.setCommonName((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_HOSTNAME))
            con.setHostname((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_PORT))
            con.setPort(Integer.parseInt((String) value));
        else if (attrID.equalsIgnoreCase(DICOM_TLS_CIPHER_SUITE))
            con.addTlsCipherSuite((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_INSTALLED))
            con.setInstalled(Boolean.valueOf((String) value));
    }
}
