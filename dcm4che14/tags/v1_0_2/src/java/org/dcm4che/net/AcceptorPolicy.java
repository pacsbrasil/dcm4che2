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

package org.dcm4che.net;

/**
 * Defines association acceptance/rejection behavior.
 *
 * @see Association#accept
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
public interface AcceptorPolicy
{
   void setMaxPDULength(int maxLength);
   
   int getMaxPDULength();
   
   AsyncOpsWindow getAsyncOpsWindow();
    
   void setAsyncOpsWindow(int maxOpsInvoked, int maxOpsPerformed);
   
   void setAsyncOpsWindow(AsyncOpsWindow aow);
   
   void setImplClassUID(String implClassUID);

   String getImplClassUID();
   
   void setImplVersionName(String implVers);
   
   String getImplVersionName();
   
   String putApplicationContextName(String proposed, String returned);
   
   boolean addCalledAET(String aet);

   boolean removeCalledAET(String aet);

   void setCalledAETs(String[] aets);
   
   String[] getCalledAETs();
   
   boolean addCallingAET(String aet);

   boolean removeCallingAET(String aet);
   
   void setCallingAETs(String[] aets);
   
   String[] getCallingAETs();
   
   AcceptorPolicy addPolicyForCalledAET(String aet, AcceptorPolicy policy);
   
   AcceptorPolicy getPolicyForCalledAET(String aet);
   
   AcceptorPolicy addPolicyForCallingAET(String aet, AcceptorPolicy policy);

   AcceptorPolicy getPolicyForCallingAET(String aet);

   PresContext addPresContext(String asuid, String[] tsuids);

   PresContext getPresContext(String as);

   PresContext removePresContext(String as);
      
   RoleSelection addRoleSelection(RoleSelection rs);
   
   RoleSelection getRoleSelection(String uid);

   RoleSelection removeRoleSelection(String uid);

   ExtNegotiator addExtNegPolicy(String uid, ExtNegotiator en);
   
   ExtNegotiator getExtNegPolicy(String uid);

   ExtNegotiator removeExtNegPolicy(String uid);
   
   PDU negotiate(AAssociateRQ rq);
  
}
