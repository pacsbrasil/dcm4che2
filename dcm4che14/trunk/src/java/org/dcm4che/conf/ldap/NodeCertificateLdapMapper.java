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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.InitialDirContext;

import org.dcm4che.conf.ConfigInfo;
import org.dcm4che.conf.NodeCertificateInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 07.09.2003
 */
public class NodeCertificateLdapMapper extends ConfigLdapMapper {

    private CertificateFactory certFact;

    public NodeCertificateLdapMapper(InitialDirContext ctx, String baseDN) {
        super(ctx, baseDN);
        try {
            certFact = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public NodeCertificateInfo load(String dn) throws NamingException {
        return (NodeCertificateInfo) load(
            dn,
            new NodeCertificateInfo(),
            new String[] { USER_CERTIFICATE });
    }

    protected void putAttributes(Attributes attrs, ConfigInfo info)
        throws NamingException {
        NodeCertificateInfo nc = (NodeCertificateInfo) info;
        Attribute attr = new BasicAttribute(USER_CERTIFICATE);
        X509Certificate[] certs = nc.getCertificate();
        for (int i = 0; i < certs.length; i++) {
            try {
                attr.add(certs[i].getEncoded());
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        attrs.put(attr);
    }

    protected void setValue(ConfigInfo info, String attrID, Object value)
        throws NamingException {
        NodeCertificateInfo nc = (NodeCertificateInfo) info;
        if (attrID.equalsIgnoreCase(USER_CERTIFICATE)) {
            ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) value);
            try {
                nc.addCertificate(
                    (X509Certificate) certFact.generateCertificate(bis));
            } catch (CertificateException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    protected String makeRDN(ConfigInfo info) {
        throw new UnsupportedOperationException();
    }
}
