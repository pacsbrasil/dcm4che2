/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.prefs.Preferences;

/**
 * Defines association acceptance/rejection behavior.
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020518 gunter zeilinger:</b>
 * <ul>
 * <li> Initial import
 * </ul>
 */
class AcceptorPolicyImpl implements AcceptorPolicy
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------   
   private int maxLength = PDataTF.DEF_MAX_PDU_LENGTH;
   
   private AsyncOpsWindow aow = null;

   private String implClassUID = Implementation.getClassUID();

   private String implVers = Implementation.getVersionName();
   
   private HashMap appCtxMap = new HashMap();

   private HashSet calledAETs = null;

   private HashSet callingAETs = null;
   
   private HashMap policyForCalledAET = new HashMap();

   private HashMap policyForCallingAET = new HashMap();
   
   private HashMap presCtxMap = new HashMap();
   
   private HashMap roleSelectionMap = new HashMap();
      
   private HashMap extNegotiaionMap = new HashMap();
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public AcceptorPolicyImpl()
   {
      addPresContext(UIDs.Verification,
            new String[] { UIDs.ImplicitVRLittleEndian });
   }
   
   // Public --------------------------------------------------------
      
   // AcceptorPolicy implementation ---------------------------------
   public void setMaxPDULength(int maxLength)
   {
      if (maxLength < 0)
         throw new IllegalArgumentException("maxLength:" + maxLength);

      this.maxLength = maxLength;
   }

   public int getMaxPDULength()
   {
        return maxLength;
   }
   
   public AsyncOpsWindow getAsyncOpsWindow()
   {
      return aow;
   }
        
   public void setAsyncOpsWindow(int maxOpsInvoked, int maxOpsPerformed)
   {
      this.aow = new AsyncOpsWindowImpl(maxOpsInvoked, maxOpsPerformed);
   }

   public void setAsyncOpsWindow(AsyncOpsWindow aow)
   {
      this.aow = aow;
   }
   
   public void setImplClassUID(String implClassUID)
   {
      this.implClassUID = StringUtils.checkUID(implClassUID);
   }

   public String getImplClassUID()
   {
      return implClassUID;
   }
   
   public void setImplVersionName(String implVers)
   {
      this.implVers = implVers != null
            ? StringUtils.checkAET(implVers) : null;
   }
   
   public String getImplVersionName()
   {
      return implVers;
   }
   
   public String putApplicationContextName(String proposed, String returned)
   {
      return (String)appCtxMap.put(StringUtils.checkUID(proposed),
                    StringUtils.checkUID(returned));
   }
   
   public boolean addCalledAET(String aet)
   {
      StringUtils.checkAET(aet);

      if (calledAETs == null)
         calledAETs = new HashSet();

      calledAETs.add(aet);
      return calledAETs.add(aet);
   }

   public boolean removeCalledAET(String aet)
   {
      return calledAETs != null && calledAETs.remove(aet);
   }

   public void setCalledAETs(String[] aets)
   {
      calledAETs = aets != null
            ? new HashSet(Arrays.asList(StringUtils.checkAETs(aets)))
            : null;
   }
   
   public String[] getCalledAETs()
   {
      return calledAETs != null
         ? (String[])calledAETs.toArray(new String[calledAETs.size()])
         : null;
   }
   
   public boolean addCallingAET(String aet)
   {
      StringUtils.checkAET(aet);

      if (callingAETs == null)
         callingAETs = new HashSet();

      return callingAETs.add(aet);
   }

   public boolean removeCallingAET(String aet)
   {
      return callingAETs != null && callingAETs.remove(aet);
   }
   
   public void setCallingAETs(String[] aets)
   {
      callingAETs = aets != null
            ? new HashSet(Arrays.asList(StringUtils.checkAETs(aets)))
            : null;
   }
   
   public String[] getCallingAETs()
   {
      return callingAETs != null
         ? (String[])callingAETs.toArray(new String[callingAETs.size()])
         : null;
   }
   
   public AcceptorPolicy addPolicyForCalledAET(String aet,
         AcceptorPolicy policy)
   {
      if (policy == null)
         throw new NullPointerException();
      
      return (AcceptorPolicy)policyForCalledAET.put(
            StringUtils.checkAET(aet), policy);
   }
   
   public AcceptorPolicy getPolicyForCalledAET(String aet)
   {
      return (AcceptorPolicy)policyForCalledAET.get(aet);
   }      

   public AcceptorPolicy addPolicyForCallingAET(String aet,
         AcceptorPolicy policy)
   {
      if (policy == null)
         throw new NullPointerException();
      
      return (AcceptorPolicy)policyForCallingAET.put(
            StringUtils.checkAET(aet), policy);
   }

   public AcceptorPolicy getPolicyForCallingAET(String aet)
   {
      return (AcceptorPolicy)policyForCallingAET.get(aet);
   }
   
   public final PresContext addPresContext(String asuid, String[] tsuids)
   {      
      return (PresContext)presCtxMap.put(asuid,
            new PresContextImpl(0x020, 1, 0,
                  StringUtils.checkUID(asuid), StringUtils.checkUIDs(tsuids)));
   }

   public PresContext getPresContext(String as)
   {
      return (PresContext)presCtxMap.get(as);
   }

   public PresContext removePresContext(String as)
   {
      return (PresContext)presCtxMap.remove(as);
   }
      
   public RoleSelection addRoleSelection(RoleSelection rs)
   {
      return (RoleSelection)roleSelectionMap.put(rs.getSOPClassUID(), rs);
   }
   
   public RoleSelection getRoleSelection(String uid)
   {
      return (RoleSelection)roleSelectionMap.get(uid);
   }

   public RoleSelection removeRoleSelection(String uid)
   {
      return (RoleSelection)roleSelectionMap.remove(uid);
   }

   public ExtNegotiator addExtNegPolicy(String uid, ExtNegotiator en)
   {
      return (ExtNegotiator)extNegotiaionMap.put(uid, en);
   }
   
   public ExtNegotiator getExtNegPolicy(String uid)
   {
      return (ExtNegotiator)extNegotiaionMap.get(uid);
   }

   public ExtNegotiator removeExtNegPolicy(String uid)
   {
      return (ExtNegotiator)extNegotiaionMap.remove(uid);
   }
   
   public PDU negotiate(AAssociateRQ rq)
   {
      if ((rq.getProtocolVersion() & 0x0001) == 0)
      {
         return new AAssociateRJImpl(
               AAssociateRJ.REJECTED_PERMANENT,
               AAssociateRJ.SERVICE_PROVIDER_ACSE,
               AAssociateRJ.PROTOCOL_VERSION_NOT_SUPPORTED);
      }
     String calledAET = rq.getCalledAET();
      if (calledAETs != null && !calledAETs.contains(calledAET))
      {
         return new AAssociateRJImpl(
               AAssociateRJ.REJECTED_PERMANENT,
               AAssociateRJ.SERVICE_USER,
               AAssociateRJ.CALLED_AE_TITLE_NOT_RECOGNIZED);
      }
      AcceptorPolicyImpl policy1 =
            (AcceptorPolicyImpl)getPolicyForCalledAET(calledAET);
      if (policy1 == null)
         policy1 = this;

      String callingAET = rq.getCalledAET();
      if (policy1.callingAETs != null
            && !policy1.callingAETs.contains(callingAET))
      {
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
   private PDU doNegotiate(AAssociateRQ rq)
   {
      String appCtx = negotiateAppCtx(rq.getApplicationContext());
      if (appCtx == null)
      {
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
   
   private String negotiateAppCtx(String proposed)
   {
      String retval = (String)appCtxMap.get(proposed);
      if (retval != null)
         return retval;
      
      if (UIDs.DICOMApplicationContextName.equals(proposed))
         return proposed;
      
      return null;
   }
   
   private void negotiatePresCtx(AAssociateRQ rq, AAssociateAC ac)
   {
      for (Iterator it = rq.listPresContext().iterator(); it.hasNext();)
         ac.addPresContext(negotiatePresCtx((PresContext)it.next()));
   }
   
   private PresContext negotiatePresCtx(PresContext offered)
   {
      int result = PresContext.ABSTRACT_SYNTAX_NOT_SUPPORTED;
      String tsuid = UIDs.ImplicitVRLittleEndian;

      PresContext accept = getPresContext(offered.getAbstractSyntaxUID());      
      if (accept != null) {
         result = PresContext.TRANSFER_SYNTAXES_NOT_SUPPORTED;
         for (Iterator it = accept.getTransferSyntaxUIDs().iterator();
               it.hasNext();)
         {
            tsuid = (String)it.next();
            if (offered.getTransferSyntaxUIDs().indexOf(tsuid) != -1)
            {
               result = PresContext.ACCEPTANCE;
               break;
            }
         }
      }
      return new PresContextImpl(0x021, offered.pcid(), result, null,
                new String[]{ tsuid } );
   }
   
   private void negotiateRoleSelection(AAssociateRQ rq, AAssociateAC ac)
   {
      for (Iterator it = rq.listRoleSelections().iterator(); it.hasNext();)
         ac.addRoleSelection(negotiateRoleSelection((RoleSelection)it.next()));
   }
   
   private RoleSelection negotiateRoleSelection(RoleSelection offered)
   {
      boolean scu = offered.scu();
      boolean scp = false;
            
      RoleSelection accept = getRoleSelection(offered.getSOPClassUID());      
      if (accept != null) {
         scu = offered.scu() && accept.scu();
         scp = offered.scp() && accept.scp();
      }
      return new RoleSelectionImpl(offered.getSOPClassUID(), scu, scp);
   }
   
   private void negotiateExt(AAssociateRQ rq, AAssociateAC ac)
   {
      for (Iterator it = rq.listExtNegotiations().iterator(); it.hasNext();)
      {
         ExtNegotiation offered = (ExtNegotiation)it.next();
         String uid = offered.getSOPClassUID();
         ExtNegotiator enp = getExtNegPolicy(uid);
         if (enp != null)
            ac.addExtNegotiation(
               new ExtNegotiationImpl(uid, enp.negotiate(offered.info())));
      }
   }

   private AsyncOpsWindow negotiateAOW(AsyncOpsWindow offered)
   {
      if (offered == null)
         return null;
      
      if (aow == null)
         return AsyncOpsWindowImpl.DEFAULT;
      
      return new AsyncOpsWindowImpl(
         minAOW(offered.getMaxOpsInvoked(), aow.getMaxOpsInvoked()),
         minAOW(offered.getMaxOpsPerformed(), aow.getMaxOpsPerformed()));         
   }
   
   static int minAOW(int a, int b)
   {
      return a == 0 ? b : b == 0 ? a : Math.min(a,b);
   }
   
   // Inner classes -------------------------------------------------
}
