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

import org.dcm4che.dict.UIDs;

/**
 * Represents a Transfer Capability of a Network AE.
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup67_fz.pdf">
 * DICOM Supplement 67 - Configuration Management.</a>   
 * 
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 31.08.2003
 */
public class TransferCapabilityInfo extends ConfigInfo {

    /** Value of property {@link #getRole role} indicating 
     * support of Service Class User role. */
    public static final String SCU = "SCU";

    /** Value of property {@link #getRole role} indicating
     * support of Service Class Provider role. */
    public static final String SCP = "SCP";

    private String cn;

    private String sopClass;

    private String role;

    private ArrayList tsList = new ArrayList();

    public TransferCapabilityInfo() {}

    public TransferCapabilityInfo(
        String cn,
        String sop,
        String role,
        String[] ts) {
        setCommonName(cn);
        setSopClass(sop);
        setRole(role);
        for (int i = 0; i < ts.length; i++) {
            addTransferSyntax(ts[i]);
        }
    }

    public String toString() {
        return getClass().getName()
            + "[cn="
            + cn
            + ", sopClass="
            + sopClass
            + ", role="
            + role
            + ", transferSyntax="
            + tsList
            + "]";
    }

    /**
     * Returns name of the Transfer Capability object or <code>null</code>.
     */
    public final String getCommonName() {
        return cn;
    }

    public final void setCommonName(String cn) {
        this.cn = cn;
    }

    public final String getSopClass() {
        return sopClass;
    }

    public final void setSopClass(String sopClass) {
        if (sopClass == null)
            throw new NullPointerException("sopClass");

        if (!UIDs.isValid(sopClass))
            throw new IllegalArgumentException("sopClass: " + sopClass);

        this.sopClass = sopClass;
    }

    /**
     * Returns supported Transfer Role
     * (either {@link #SCU SCU} or {@link #SCP SCU}).
     */
    public final String getRole() {
        return role;
    }

    public final void setRole(String role) {
        if (sopClass == null)
            throw new NullPointerException("role");

        if (role.equalsIgnoreCase(SCU))
            this.role = SCU;
        else if (role.equalsIgnoreCase(SCP))
            this.role = SCP;
        else
            throw new IllegalArgumentException("role: " + role);
    }

    /**
     * Returns Transfer syntax(es) requested as an SCU or offered as an SCP.
     */
    public final String[] getTransferSyntax() {
        return (String[]) tsList.toArray(new String[tsList.size()]);
    }

    public final void addTransferSyntax(String transferSyntax) {
        if (transferSyntax == null)
            throw new NullPointerException("transferSyntax");

        if (!UIDs.isValid(transferSyntax))
            throw new IllegalArgumentException(
                "transferSyntax: " + transferSyntax);

        tsList.add(transferSyntax);
    }

    public final boolean removeTransferSyntax(String transferSyntax) {
        return tsList.remove(transferSyntax);
    }

    /**
     * @return
     */
    public boolean isValid() {
        return sopClass != null && role != null && !tsList.isEmpty();
    }

}
