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
import org.dcm4che.conf.TransferCapabilityInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 05.09.2003
 */
public class TransferCapabilityLdapMapper extends ConfigLdapMapper {

    public TransferCapabilityLdapMapper(InitialDirContext ctx, String baseDN) {
        super(ctx, baseDN);
    }

    public TransferCapabilityInfo load(String dn) throws NamingException {
        return (TransferCapabilityInfo) load(
            dn,
            new TransferCapabilityInfo(),
            new String[] {
                COMMON_NAME,
                DICOM_SOP_CLASS,
                DICOM_TRANSFER_ROLE,
                DICOM_TRANSFER_SYNTAX });
    }

    public void store(String parentDN, TransferCapabilityInfo tc)
        throws NamingException {
        store(parentDN, tc, new String[] { TOP, DICOM_TRANSFER_CAPABILITY });
    }

    protected void putAttributes(Attributes attrs, ConfigInfo info) {
        TransferCapabilityInfo tc = (TransferCapabilityInfo) info;
        putAttribute(attrs, COMMON_NAME, tc.getCommonName());
        putAttribute(attrs, DICOM_SOP_CLASS, tc.getSopClass());
        putAttribute(attrs, DICOM_TRANSFER_ROLE, tc.getRole());
        putAttribute(attrs, DICOM_TRANSFER_SYNTAX, tc.getTransferSyntax());
    }

    protected String makeRDN(ConfigInfo info) {
        TransferCapabilityInfo tc = (TransferCapabilityInfo) info;
        return (tc.getCommonName() != null)
            ? (COMMON_NAME + "=" + tc.getCommonName())
            : (DICOM_SOP_CLASS
                + "="
                + tc.getSopClass()
                + "+"
                + DICOM_TRANSFER_ROLE
                + "="
                + tc.getRole());
    }

    protected void setValue(ConfigInfo info, String attrID, Object value) {
        TransferCapabilityInfo tc = (TransferCapabilityInfo) info;
        if (attrID.equalsIgnoreCase(COMMON_NAME))
            tc.setCommonName((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_SOP_CLASS))
            tc.setSopClass((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_TRANSFER_ROLE))
            tc.setRole((String) value);
        else if (attrID.equalsIgnoreCase(DICOM_TRANSFER_SYNTAX))
            tc.addTransferSyntax((String) value);
    }
}
