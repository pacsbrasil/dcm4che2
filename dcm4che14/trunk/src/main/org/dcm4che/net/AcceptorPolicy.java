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
   public void setReceivedPDUMaxLength(int maxLength);
   
   public int getReceivedPDUMaxLength();
   
   public AsyncOpsWindow getAsyncOpsWindow();
    
   public void setAsyncOpsWindow(AsyncOpsWindow aow);
   
   public void setImplClassUID(String implClassUID);

   public String getImplClassUID();
   
   public void setImplVersionName(String implVers);
   
   public String getImplVersionName();
   
   public String putApplicationContextName(String proposed, String returned);
   
   public boolean addCalledAET(String aet);

   public boolean removeCalledAET(String aet);

   public void setCalledAETs(String[] aets);
   
   public String[] getCalledAETs();
   
   public boolean addCallingAET(String aet);

   public boolean removeCallingAET(String aet);
   
   public void setCallingAETs(String[] aets);
   
   public String[] getCallingAETs();
   
   public AcceptorPolicy addPolicyForCalledAET(String aet,
         AcceptorPolicy policy);
   
   public AcceptorPolicy getPolicyForCalledAET(String aet);
   
   public AcceptorPolicy addPolicyForCallingAET(String aet,
         AcceptorPolicy policy);

   public AcceptorPolicy getPolicyForCallingAET(String aet);

   public PresContext addPresContext(String asuid, String[] tsuids);

   public PresContext getPresContext(String as);

   public PresContext removePresContext(String as);
      
   public RoleSelection addRoleSelection(RoleSelection rs);
   
   public RoleSelection getRoleSelection(String uid);

   public RoleSelection removeRoleSelection(String uid);

   public ExtNegotiator addExtNegPolicy(String uid, ExtNegotiator en);
   
   public ExtNegotiator getExtNegPolicy(String uid);

   public ExtNegotiator removeExtNegPolicy(String uid);
   
   public PDU negotiate(AAssociateRQ rq);
  
}
