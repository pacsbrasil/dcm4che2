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

import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 07.09.2003
 */
public class NodeCertificateInfo extends ConfigInfo {

    private ArrayList list = new ArrayList(1);

    public X509Certificate[] getCertificate() {
        return (X509Certificate[]) list.toArray(
            new X509Certificate[list.size()]);
    }

    public void addCertificate(X509Certificate certificate) {
        if (certificate == null)
            throw new NullPointerException("certificate");
            
        list.add(certificate);
    }

    public boolean removeCertificate(X509Certificate certificate) {
        return list.remove(certificate);
    }

    public boolean isValid() {
        return !list.isEmpty();
    }

}
