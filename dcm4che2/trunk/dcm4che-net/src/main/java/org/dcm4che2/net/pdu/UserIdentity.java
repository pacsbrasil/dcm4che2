/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class UserIdentity {

    private int userIdentityType;
    private boolean positiveResponseRequested;
    private byte[] primaryField = {};
    private byte[] secondaryField = {};
        
    public final int getUserIdentityType() {
        return userIdentityType;
    }

    public final void setUserIdentityType(int userIdentityType) {
        this.userIdentityType = userIdentityType;
    }

    public final boolean isPositiveResponseRequested() {
        return positiveResponseRequested;
    }

    public final void setPositiveResponseRequested(boolean positiveResponseRequested) {
        this.positiveResponseRequested = positiveResponseRequested;
    }

    public final byte[] getPrimaryField() {
        return primaryField.clone();
    }

    public final void setPrimaryField(byte[] primaryField) {
        this.primaryField = primaryField.clone();
    }

    public final byte[] getSecondaryField() {
        return secondaryField.clone();
    }

    public final void setSecondaryField(byte[] secondaryField) {
        this.secondaryField = secondaryField.clone();
    }

    public int length() {
        return 6 + primaryField.length + secondaryField.length;
    }

}
