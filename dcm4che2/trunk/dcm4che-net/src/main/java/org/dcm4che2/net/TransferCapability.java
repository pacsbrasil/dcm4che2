/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.net;

import java.util.Arrays;

import org.dcm4che2.net.pdu.ExtendedNegotiation;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 7, 2005
 * 
 */
public class TransferCapability {
    
    public static final String SCU = "SCU";
    public static final String SCP = "SCP";
    private static byte[] NO_EXT_INFO = {};
    private static int[] NO_TS_GRP = {};

    protected String commonName;
    protected String sopClass;
    protected boolean scp;
    protected String[] transferSyntax = {};
    protected int[] transferSyntaxGroup = {};
    protected byte[] extInfo = {};

    public TransferCapability() {
    }

    public TransferCapability(String sopClass, String[] transferSyntax,
            String role) {
        setSopClass(sopClass);
        setTransferSyntax(transferSyntax);
        setRole(role);
    }

    public final String getCommonName() {
        return commonName;
    }

    public final void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public final String getRole() {
        return scp ? SCP : SCU;
    }

    public final void setRole(String role) {
        if (role == null)
            throw new NullPointerException("Role");

        if (role.equals(SCP))
            scp = true;
        else if (role.equals(SCU))
            scp = false;
        else
            throw new IllegalArgumentException("Role:" + role);
    }

    public final boolean isSCP() {
        return scp;
    }

    public final boolean isSCU() {
        return !scp;
    }

    public final String getSopClass() {
        return sopClass;
    }

    public final void setSopClass(String sopClass) {
        if (sopClass == null)
            throw new NullPointerException("sopClass");
        this.sopClass = sopClass;
    }

    public final String[] getTransferSyntax() {
        return (String[]) transferSyntax.clone();
    }

    public boolean containsTransferSyntax(String ts) {
        for (int i = 0; i < transferSyntax.length; i++) {
            if (transferSyntax[i].equals(ts)) {
                return true;
            }
        }
        return false;
    }
    
    public final void setTransferSyntax(String[] transferSyntax) {
        if (transferSyntax.length == 0)
            throw new IllegalArgumentException("transferSyntax.length = 0");
        for (int i = 0; i < transferSyntax.length; i++) {
            if (transferSyntax[i] == null) {
                throw new NullPointerException("transferSyntax[" + i + "]");
            }
        }
        this.transferSyntax = (String[]) transferSyntax.clone();
    }
    
    public final void setTransferSyntaxGroup(int[] transferSyntaxGroup) {
        this.transferSyntaxGroup = transferSyntaxGroup != null 
                ? (int[]) transferSyntaxGroup.clone() : NO_TS_GRP;
    }

    public final int[] getTransferSyntaxGroup() {
        int[] a = new int[Math.min(transferSyntaxGroup.length, transferSyntax.length)];
        System.arraycopy(transferSyntaxGroup, 0, a, 0, a.length);
        return a;
    }

    int[] getTransferSyntaxGroups() {
        if (transferSyntaxGroup.length == 0) {
            return new int[] { 0 };
        }
        int[] a = getTransferSyntaxGroup();
        Arrays.sort(a);
        int last = 0;
        boolean addZero = transferSyntaxGroup.length < transferSyntax.length;
        for (int i = 1; i < a.length; i++) {
            addZero = addZero && a[i] != 0;
            if (a[last] != a[i]) {
                a[++last] = a[i];
            }
        }
        int n = last + 1;
        if (!addZero && n == a.length) {
            return a;
        }
        int[] b = new int[addZero ? n + 1 : n];
        System.arraycopy(a, 0, b, addZero ? 1 : 0, n);
        return b;
    }
    
    String[] getTransferSyntaxOfGroup(int group) {
        int n = 0;
        int grlen = Math.min(transferSyntax.length, transferSyntaxGroup.length);
        for (int i = 0; i < grlen; i++) {
            if (transferSyntaxGroup[i] == group) {
                ++n;
            }
        }
        if (group == 0) {
            n += transferSyntax.length - grlen;
        }
        if (n == 0) {
            return null;
        }
        String[] ts = new String[n];
        int index = 0;
        for (int i = 0; i < grlen; i++) {
            if (transferSyntaxGroup[i] == group) {
                ts[index++] = transferSyntax[i];
            }
        }
        if (index < n) {
            System.arraycopy(transferSyntax, grlen, ts, index, n - index);
        }
        return ts;
    }
    

    public final byte[] getExtInfo() {
        return (byte[]) extInfo.clone();
    }

    public final void setExtInfo(byte[] info) {
        extInfo = info != null ? (byte[]) info.clone() : NO_EXT_INFO;
    }

    public boolean getExtInfoBoolean(int field) {
        return extInfo != null && extInfo.length > field && extInfo[field] != 0;
    }

    public int getExtInfoInt(int field) {
        return extInfo != null && extInfo.length > field ? extInfo[field] & 0xff
                : 0;
    }

    public void setExtInfoBoolean(int field, boolean b) {
        setExtInfoInt(field, b ? 1 : 0);
    }

    public void setExtInfoInt(int field, int value) {
        extInfo[field] = (byte) value;
    }

    protected ExtendedNegotiation negotiate(ExtendedNegotiation offered) {
        if (offered == null || extInfo == null)
            return null;
        byte[] info = offered.getInformation();
        for (int i = 0; i < info.length; i++) {
            info[i] &= getExtInfoInt(i);
        }
        return new ExtendedNegotiation(sopClass, info);
    }
}
