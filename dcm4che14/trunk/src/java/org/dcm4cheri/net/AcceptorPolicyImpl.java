/* $Id$
 * 
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4cheri.net;

import org.dcm4che.Implementation;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.PDU;
import org.dcm4che.net.AsyncOpsWindow;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.ExtNegotiator;
import org.dcm4che.net.PDataTF;
import org.dcm4che.net.RoleSelection;
import org.dcm4che.dict.UIDs;

import org.dcm4cheri.util.StringUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Defines association acceptance/rejection behavior.
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020518 gunter zeilinger:</b>
 * <ul>
 * <li> Initial import
 * </ul>
 * <p><b>20030425 gunter zeilinger:</b>
 * <ul>
 * <li> Fix Permanent-reject with reason "CallingAE-not-recognized".
 *      Thanx to Jie from INPHACT for sending me the Bug Fix
 * </ul>
 * <p><b>20031202 gunter zeilinger:</b>
 * <ul>
 * <li> Use empty AET HashSet as NULL object (=>no AET check)
 *      to avoid checks for null value.
 * </ul>
 */
class AcceptorPolicyImpl implements AcceptorPolicy {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private int maxLength = PDataTF.DEF_MAX_PDU_LENGTH;
    
    private AsyncOpsWindow aow = null;
    
    private String implClassUID = Implementation.getClassUID();
    
    private String implVers = Implementation.getVersionName();
    
    private HashMap appCtxMap = new HashMap();
    
    private HashSet calledAETs = new HashSet();
    
    private HashSet callingAETs = new HashSet();
    
    private HashMap policyForCalledAET = new HashMap();
    
    private HashMap policyForCallingAET = new HashMap();
    
    private LinkedHashMap presCtxMap = new LinkedHashMap();
    
    private HashMap roleSelectionMap = new HashMap();
    
    private HashMap extNegotiaionMap = new HashMap();
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public AcceptorPolicyImpl() {
        putPresContext(UIDs.Verification,
            new String[] { UIDs.ImplicitVRLittleEndian });
    }
    
    // Public --------------------------------------------------------
    
    // AcceptorPolicy implementation ---------------------------------
    public void setMaxPDULength(int maxLength) {
        if (maxLength < 0)
            throw new IllegalArgumentException("maxLength:" + maxLength);
        
        this.maxLength = maxLength;
    }
    
    public int getMaxPDULength() {
        return maxLength;
    }
    
    public AsyncOpsWindow getAsyncOpsWindow() {
        return aow;
    }
    
    public void setAsyncOpsWindow(int maxOpsInvoked, int maxOpsPerformed) {
        if (maxOpsInvoked == 1 && maxOpsPerformed == 1) {
            aow = null;
        } else if (aow == null 
                || aow.getMaxOpsInvoked() != maxOpsInvoked
                || aow.getMaxOpsPerformed() != maxOpsPerformed) {
            aow = new AsyncOpsWindowImpl(maxOpsInvoked, maxOpsPerformed);
        }
    }
    
    public void setImplClassUID(String implClassUID) {
        this.implClassUID = StringUtils.checkUID(implClassUID);
    }
    
    public String getImplClassUID() {
        return implClassUID;
    }
    
    public void setImplVersionName(String implVers) {
        this.implVers = implVers != null
        ? StringUtils.checkAET(implVers) : null;
    }
    
    public String getImplVersionName() {
        return implVers;
    }
    
    public String putApplicationContextName(String proposed, String returned) {
        return (String)appCtxMap.put(StringUtils.checkUID(proposed),
        StringUtils.checkUID(returned));
    }
    
    public boolean addCalledAET(String aet) {
        StringUtils.checkAET(aet);
        return calledAETs.add(aet);
    }
    
    public boolean removeCalledAET(String aet) {
        return calledAETs.remove(aet);
    }
    
    public void setCalledAETs(String[] aets) {
        if (aets == null) {
            calledAETs.clear();
        } else {
            StringUtils.checkAETs(aets); 
            calledAETs.clear();
            calledAETs.addAll(Arrays.asList(aets));
        }
    }
    
    public String[] getCalledAETs() {
        return (String[])calledAETs.toArray(new String[calledAETs.size()]);
    }
    
    public boolean addCallingAET(String aet) {
        StringUtils.checkAET(aet);
        return callingAETs.add(aet);
    }
    
    public boolean removeCallingAET(String aet) {
        return callingAETs.remove(aet);
    }
    
    public void setCallingAETs(String[] aets) {
        if (aets == null) {
            callingAETs.clear();
        } else {
            StringUtils.checkAETs(aets); 
            callingAETs.clear();
            callingAETs.addAll(Arrays.asList(aets));
        }
    }
    
    public String[] getCallingAETs() {
        return (String[])callingAETs.toArray(new String[callingAETs.size()]);
    }
    
    public AcceptorPolicy getPolicyForCallingAET(String aet) {
        return (AcceptorPolicy)policyForCallingAET.get(aet);
    }

    public AcceptorPolicy putPolicyForCallingAET (String aet,
            AcceptorPolicy policy) {
        return putPolicyForXXXAET(aet, policy, policyForCallingAET);
    }
    
    public AcceptorPolicy getPolicyForCalledAET(String aet) {
        return (AcceptorPolicy)policyForCalledAET.get(aet);
    }
    
    public AcceptorPolicy putPolicyForCalledAET (String aet,
            AcceptorPolicy policy) {
        return putPolicyForXXXAET(aet, policy, policyForCalledAET);
    }

    private AcceptorPolicy putPolicyForXXXAET(String aet, 
            AcceptorPolicy policy, HashMap map) {
        if (policy != null) {
            return (AcceptorPolicy)map.put(StringUtils.checkAET(aet), policy);
        } else {
            return (AcceptorPolicy)map.remove(aet);
        }
    }
        
    public final PresContext putPresContext(String asuid, String[] tsuids) {
        if (tsuids != null) {
            return (PresContext)presCtxMap.put(asuid,
                new PresContextImpl(0x020, 1, 0,
                    StringUtils.checkUID(asuid), 
                    StringUtils.checkUIDs(tsuids)));
        } else {
            return (PresContext)presCtxMap.remove(asuid);
        }
    }
    
    public PresContext getPresContext(String as) {
        return (PresContext)presCtxMap.get(as);
    }
    
    public RoleSelection putRoleSelection(String uid, boolean scu, boolean scp) {
        return (RoleSelection)roleSelectionMap.put(
            StringUtils.checkUID(uid), new RoleSelectionImpl(uid, scu, scp));
    }
    
    public RoleSelection getRoleSelection(String uid) {
        return (RoleSelection)roleSelectionMap.get(uid);
    }
    
    public RoleSelection removeRoleSelection(String uid) {
        return (RoleSelection)roleSelectionMap.remove(uid);
    }
    
    public ExtNegotiator putExtNegPolicy(String uid, ExtNegotiator en) {
        if (en != null) {
            return (ExtNegotiator)extNegotiaionMap.put(uid, en);
        } else {
            return (ExtNegotiator)extNegotiaionMap.remove(uid);
        }
    }
    
    public ExtNegotiator getExtNegPolicy(String uid) {
        return (ExtNegotiator)extNegotiaionMap.get(uid);
    }
    
    public PDU negotiate(AAssociateRQ rq) {
        if ((rq.getProtocolVersion() & 0x0001) == 0) {
            return new AAssociateRJImpl(
                AAssociateRJ.REJECTED_PERMANENT,
                AAssociateRJ.SERVICE_PROVIDER_ACSE,
                AAssociateRJ.PROTOCOL_VERSION_NOT_SUPPORTED);
        }
        String calledAET = rq.getCalledAET();
        if (!calledAETs.isEmpty() && !calledAETs.contains(calledAET)) {
            return new AAssociateRJImpl(
                AAssociateRJ.REJECTED_PERMANENT,
                AAssociateRJ.SERVICE_USER,
                AAssociateRJ.CALLED_AE_TITLE_NOT_RECOGNIZED);
        }
        AcceptorPolicyImpl policy1 =
        (AcceptorPolicyImpl)getPolicyForCalledAET(calledAET);
        if (policy1 == null)
            policy1 = this;
        
        String callingAET = rq.getCallingAET();
        if (!policy1.callingAETs.isEmpty()
        && !policy1.callingAETs.contains(callingAET)) {
            return new AAssociateRJImpl(
                AAssociateRJ.REJECTED_PERMANENT,
                AAssociateRJ.SERVICE_USER,
                AAssociateRJ.CALLING_AE_TITLE_NOT_RECOGNIZED);
        }
        AcceptorPolicyImpl policy2 =
        (AcceptorPolicyImpl)policy1.getPolicyForCallingAET(callingAET);
        if (policy2 == null)
            policy2 = policy1;
        
        return policy2.doNegotiate(rq);
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    private PDU doNegotiate(AAssociateRQ rq) {
        String appCtx = negotiateAppCtx(rq.getApplicationContext());
        if (appCtx == null) {
            return new AAssociateRJImpl(
            AAssociateRJ.REJECTED_PERMANENT,
            AAssociateRJ.SERVICE_USER,
            AAssociateRJ.APPLICATION_CONTEXT_NAME_NOT_SUPPORTED);
        }
        AAssociateAC ac = new AAssociateACImpl();
        ac.setApplicationContext(appCtx);
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        ac.setMaxPDULength(this.maxLength);
        ac.setImplClassUID(this.implClassUID);
        ac.setImplVersionName(this.implVers);
        ac.setAsyncOpsWindow(negotiateAOW(rq.getAsyncOpsWindow()));
        negotiatePresCtx(rq, ac);
        negotiateRoleSelection(rq, ac);
        negotiateExt(rq, ac);
        return ac;
    }
    
    private String negotiateAppCtx(String proposed) {
        String retval = (String)appCtxMap.get(proposed);
        if (retval != null)
            return retval;
        
        if (UIDs.DICOMApplicationContextName.equals(proposed))
            return proposed;
        
        return null;
    }
    
    private void negotiatePresCtx(AAssociateRQ rq, AAssociateAC ac) {
        for (Iterator it = rq.listPresContext().iterator(); it.hasNext();)
            ac.addPresContext(negotiatePresCtx((PresContext)it.next()));
    }
    
    private PresContext negotiatePresCtx(PresContext offered) {
        int result = PresContext.ABSTRACT_SYNTAX_NOT_SUPPORTED;
        String tsuid = UIDs.ImplicitVRLittleEndian;
        
        PresContext accept = getPresContext(offered.getAbstractSyntaxUID());
        if (accept != null) {
            result = PresContext.TRANSFER_SYNTAXES_NOT_SUPPORTED;
            for (Iterator it = accept.getTransferSyntaxUIDs().iterator();
                    it.hasNext();) {
                tsuid = (String)it.next();
                if (offered.getTransferSyntaxUIDs().indexOf(tsuid) != -1) {
                    result = PresContext.ACCEPTANCE;
                    break;
                }
            }
        }
        return new PresContextImpl(0x021, offered.pcid(), result, null,
        new String[]{ tsuid } );
    }
    
    private void negotiateRoleSelection(AAssociateRQ rq, AAssociateAC ac) {
        for (Iterator it = rq.listRoleSelections().iterator(); it.hasNext();)
            ac.addRoleSelection(negotiateRoleSelection((RoleSelection)it.next()));
    }
    
    private RoleSelection negotiateRoleSelection(RoleSelection offered) {
        boolean scu = offered.scu();
        boolean scp = false;
        
        RoleSelection accept = getRoleSelection(offered.getSOPClassUID());
        if (accept != null) {
            scu = offered.scu() && accept.scu();
            scp = offered.scp() && accept.scp();
        }
        return new RoleSelectionImpl(offered.getSOPClassUID(), scu, scp);
    }
    
    private void negotiateExt(AAssociateRQ rq, AAssociateAC ac) {
        for (Iterator it = rq.listExtNegotiations().iterator(); it.hasNext();) {
            ExtNegotiation offered = (ExtNegotiation)it.next();
            String uid = offered.getSOPClassUID();
            ExtNegotiator enp = getExtNegPolicy(uid);
            if (enp != null)
                ac.addExtNegotiation(
                new ExtNegotiationImpl(uid, enp.negotiate(offered.info())));
        }
    }
    
    private AsyncOpsWindow negotiateAOW(AsyncOpsWindow offered) {
        if (offered == null)
            return null;
        
        if (aow == null)
            return AsyncOpsWindowImpl.DEFAULT;
        
        return new AsyncOpsWindowImpl(
            minAOW(offered.getMaxOpsInvoked(), aow.getMaxOpsInvoked()),
            minAOW(offered.getMaxOpsPerformed(), aow.getMaxOpsPerformed()));
    }
    
    static int minAOW(int a, int b) {
        return a == 0 ? b : b == 0 ? a : Math.min(a,b);
    }
    
    public List listPresContext() {
        return Collections.unmodifiableList(
            new ArrayList(presCtxMap.values()));       
    }
    
    // Inner classes -------------------------------------------------
}
