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
 * Represents a Network Connection.
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup67_fz.pdf">
 * DICOM Supplement 67 - Configuration Management.</a>
 *    
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 30.08.2003
 */
public class NetworkConnectionInfo {

    private String cn;
    private String hostname;
    private int port;
    private ArrayList csList = new ArrayList();
    private Boolean installed;

    public String toString() {
        return getClass().getName()
            + "[cn="
            + cn
            + ", hostname="
            + hostname
            + ", port="
            + port
            + ", cipherSuites="
            + csList
            + ", installed="
            + installed
            + "]";
    }

    public String getCommonName() {
        return cn;
    }

    public void setCommonName(String cn) {
        this.cn = cn;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String[] getTlsCipherSuite() {
        return (String[]) csList.toArray(new String[csList.size()]);
    }

    public void addTlsCipherSuite(String cipherSuite) {
        if (cipherSuite == null)
            throw new NullPointerException("cipherSuite");

        csList.add(cipherSuite);
    }

    public boolean removeCipherSuite(String cipherSuite) {
        return csList.remove(cipherSuite);
    }

    public boolean isTLS() {
        return !csList.isEmpty();
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public Boolean isInstalled() {
        return installed;
    }

    public boolean isValid() {
        return hostname != null;
    }

    public boolean isListening() {
        return port > 0;
    }
}
