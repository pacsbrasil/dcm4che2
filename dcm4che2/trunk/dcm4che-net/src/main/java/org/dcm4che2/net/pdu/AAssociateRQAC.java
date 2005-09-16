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
import java.util.Iterator;
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
    private int protocolVersion = 1;
    private int maxPDULength = DEF_MAX_PDU_LENGTH;
    private int maxOpsInvoked = 1;
    private int maxOpsPerformed = 1;
    private String calledAET = DEF_CALLED_AET;
    private String callingAET = DEF_CALLING_AET;
    private String applicationContext = UID.DICOMApplicationContextName;
    private String implClassUID = Implementation.classUID();
    private String implVersionName = Implementation.versionName();
    private final List pcs = new ArrayList();
    private final Map roleSelMap = new LinkedHashMap();
    private final Map extNegMap = new LinkedHashMap();
    private final Map commonExtNegMap = new LinkedHashMap();
    private UserIdentity userIdentity;

    public final int getProtocolVersion() {
        return protocolVersion;
    }

    public final void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public final byte[] getReservedBytes() {
        return reservedBytes.clone();
    }

    public final void setReservedBytes(byte[] reservedBytes) {
        if (reservedBytes.length != 32)
            throw new IllegalArgumentException("reservedBytes.length: "
                    + reservedBytes.length);
        System.arraycopy(reservedBytes, 0, this.reservedBytes, 0, 32);
    }

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

    public final String getApplicationContext() {
        return applicationContext;
    }

    public final void setApplicationContext(String applicationContext) {
        if (applicationContext != null)
            throw new NullPointerException();
        
        this.applicationContext = applicationContext;
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
        if (implClassUID == null)
            throw new NullPointerException();
        
        this.implClassUID = implClassUID;
    }

    public final String getImplVersionName() {
        return implVersionName;
    }

    public final void setImplVersionName(String implVersionName) {
        this.implVersionName = implVersionName;
    }

    public Collection getPresentationContexts() {
         return Collections.unmodifiableCollection(pcs) ;
    }
    
    public void addPresentationContext(PresentationContext pc) {
        if (pc == null)
            throw new NullPointerException();
        if (pcs.size() >= 128)
            throw new IllegalStateException(
                    "Maximal Number (128) of Presentation Context obtained.");
            
        pcs.add(pc);
    }

    public boolean removePresentationContext(PresentationContext pc) {
        return pcs.remove(pc);
    }
    
    public Collection getRoleSelections() {
        return Collections.unmodifiableCollection(roleSelMap.values());
    }
    
    public RoleSelection getRoleSelectionFor(String cuid) {
        return (RoleSelection) roleSelMap.get(cuid);
    }

    public RoleSelection addRoleSelection(RoleSelection rs) {
        return (RoleSelection) roleSelMap.put(rs.getSOPClassUID(), rs);
    }

    public RoleSelection removeRoleSelectionFor(String cuid) {
        return (RoleSelection) roleSelMap.remove(cuid);
    }
    
    public Collection getExtendedNegotiations() {
        return Collections.unmodifiableCollection(extNegMap.values());
    }

    public ExtendedNegotiation getExtendedNegotiationFor(String cuid) {
        return (ExtendedNegotiation) extNegMap.get(cuid);
    }

    public ExtendedNegotiation addExtendedNegotiation(ExtendedNegotiation extNeg) {
        return (ExtendedNegotiation) extNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public ExtendedNegotiation removeExtendedNegotiationFor(String cuid) {
        return (ExtendedNegotiation) extNegMap.remove(cuid);
    }
    
    public Collection getCommonExtendedNegotiations() {
        return Collections.unmodifiableCollection(commonExtNegMap.values());
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiationFor(String cuid) {
        return (CommonExtendedNegotiation) commonExtNegMap.get(cuid);
    }

    public CommonExtendedNegotiation addCommonExtendedNegotiation(CommonExtendedNegotiation extNeg) {
        return (CommonExtendedNegotiation) commonExtNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public CommonExtendedNegotiation removeCommonExtendedNegotiationFor(String cuid) {
        return (CommonExtendedNegotiation) commonExtNegMap.remove(cuid);
    }
    
    public final UserIdentity getUserIdentity() {
        return userIdentity;
    }

    public final void setUserIdentity(UserIdentity userIdentity) {
        this.userIdentity = userIdentity;
    }
    
    public int length() {
        int len = 74;   // Fix AA-RQ/AC PDU fields
        len += 4 + applicationContext.length();
        for (Iterator it = pcs.iterator(); it.hasNext();)
            len += 4 + ((PresentationContext) it.next()).length();        
        len += 4 + userInfoLength();
        return len;
    }

    public int userInfoLength() {
        int len = 8;    // Max Length Sub-Item
        len += 4 + implClassUID.length();
        if (isAsyncOps())
            len += 8;   // Asynchronous Operations Window Sub-Item
        for (Iterator it = roleSelMap.values().iterator(); it.hasNext();)
            len += 4 + ((RoleSelection) it.next()).length();        
        if (implVersionName != null)
            len += 4 + implVersionName.length();
        for (Iterator it = extNegMap.values().iterator(); it.hasNext();)
            len += 4 + ((ExtendedNegotiation) it.next()).length();        
        for (Iterator it = commonExtNegMap.values().iterator(); it.hasNext();)
            len += 4 + ((CommonExtendedNegotiation) it.next()).length();        
        if (userIdentity != null)
            len += 4 + userIdentity.length();        
        return len;
    }

}
