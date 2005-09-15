/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.Implementation;
import org.dcm4che2.data.UID;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public abstract class AAssociateRQAC implements PDU {

    private static final int DEF_MAX_PDU_LENGTH = 16384;
    private static final String DEF_CALLED_AET = "ANONYMOUS";
    private static final String DEF_CALLING_AET = "ANONYMOUS";
    
    private byte[] reservedBytes = new byte[32];
    private int maxPDULength = DEF_MAX_PDU_LENGTH;
    private int maxOpsInvoked = 1;
    private int maxOpsPerformed = 1;
    private String calledAET = DEF_CALLED_AET;
    private String callingAET = DEF_CALLING_AET;
    private String applicationContext = UID.DICOMApplicationContextName;
    private String implClassUID = Implementation.classUID();
    private String implVersionName = Implementation.versionName();
    private final List pcList = new ArrayList();
    private final Map roleSelMap = new LinkedHashMap();
    private final Map extNegMap = new LinkedHashMap();
    private final Map commonExtNegMap = new LinkedHashMap();
    private UserIdentity userIdentity;

    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String calledAET) {
        if (calledAET.length() > 16)
            throw new IllegalArgumentException("calledAET: " + calledAET);
        this.calledAET = calledAET;
    }

    public final String getCallingAET() {
        return callingAET;
    }

    public final void setCallingAET(String callingAET) {
        if (callingAET.length() > 16)
            throw new IllegalArgumentException("callingAET: " + callingAET);
        this.callingAET = callingAET;
    }

    public final int getMaxPDULength() {
        return maxPDULength;
    }

    public final void setMaxPDULength(int maxPDULength) {
        this.maxPDULength = maxPDULength;
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }
    
    public final boolean isAsyncOps() {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }

    public final String getImplClassUID() {
        return implClassUID;
    }

    public final void setImplClassUID(String implClassUID) {
        this.implClassUID = implClassUID;
    }

    public final String getImplVersionName() {
        return implVersionName;
    }

    public final void setImplVersionName(String implVersionName) {
        this.implVersionName = implVersionName;
    }

    public final byte[] getReservedBytes() {
        return reservedBytes.clone();
    }

    public final void setReservedBytes(byte[] reservedBytes) {
        if (reservedBytes.length != 32)
            throw new IllegalArgumentException("reservedBytes.length: "
                    + reservedBytes.length);
        this.reservedBytes = reservedBytes.clone();
    }

    public final String getApplicationContext() {
        return applicationContext;
    }

    public final void setApplicationContext(String applicationContext) {
        if (applicationContext != null)
            throw new NullPointerException();
        
        this.applicationContext = applicationContext;
    }

    public final Collection getPresentationContexts() {
         return Collections.unmodifiableList(pcList) ;
    }
    
    public Collection getRoleSelections() {
        return Collections.unmodifiableCollection(roleSelMap.values());
    }

    public Collection getExtendedNegotiations() {
        return Collections.unmodifiableCollection(extNegMap.values());
    }

    public Collection getCommonExtendedNegotiations() {
        return Collections.unmodifiableCollection(commonExtNegMap.values());
    }

    public final UserIdentity getUserIdentity() {
        return userIdentity;
    }

    public final void setUserIdentity(UserIdentity userIdentity) {
        this.userIdentity = userIdentity;
    }
    
    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int userInfoLength() {
        // TODO Auto-generated method stub
        return 0;
    }

}
