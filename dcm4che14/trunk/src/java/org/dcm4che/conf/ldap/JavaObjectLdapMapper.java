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
import org.dcm4che.conf.JavaObjectInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 10.09.2003
 */
public class JavaObjectLdapMapper extends ConfigLdapMapper {

    public JavaObjectLdapMapper(InitialDirContext ctx, String baseDN) {
        super(ctx, baseDN);
    }

    public JavaObjectInfo load(String dn) throws NamingException {
        JavaObjectInfo info = new JavaObjectInfo(dn);
        info.setObject(ctx.lookup(dn));
        return info;
    }

    public void store(JavaObjectInfo info)
        throws NamingException {
        ctx.bind(info.getDN(), info.getObject());
    }

    protected String makeRDN(ConfigInfo info) {
        throw new UnsupportedOperationException();
    }

    protected void putAttributes(Attributes attrs, ConfigInfo info) throws NamingException {
        throw new UnsupportedOperationException();
    }


    protected void setValue(ConfigInfo info, String attrID, Object value) throws NamingException {
        throw new UnsupportedOperationException();
    }
}
