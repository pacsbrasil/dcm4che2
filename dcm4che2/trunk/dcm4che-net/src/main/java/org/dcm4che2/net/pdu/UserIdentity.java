/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

import java.io.UnsupportedEncodingException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class UserIdentity {

    public static final int USERNAME = 1;
    public static final int USERNAME_PASSCODE = 2;
    public static final int KERBEROS = 3;
    
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
    
    public String getUsername() {
         return toString(primaryField);
    }    

    public void setUsername(String username) {
        primaryField = toBytes(username);
    }    
    
    public String getPasscode() {
        return toString(secondaryField);
    }    

    public void setPasscode(String passcode) {
       secondaryField = toBytes(passcode);
    }    
   
    private static byte[] toBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toString(byte[] b) {
        try {
            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public int length() {
        return 6 + primaryField.length + secondaryField.length;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("UserIdentity[type = ").append(userIdentityType);
        if (userIdentityType == USERNAME || userIdentityType == USERNAME_PASSCODE) {
            sb.append(", username = ").append(getUsername());
            if (userIdentityType == USERNAME_PASSCODE) {
                sb.append(", passcode = ");
                for (int i = secondaryField.length; --i >= 0;)
                    sb.append('*');
            }
        } else {
            sb.append(", primaryField(").append(primaryField.length);
            sb.append("), secondaryField(").append(secondaryField.length);
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }

}
