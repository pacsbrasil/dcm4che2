/*$Id$*/
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

import java.io.IOException;
import java.util.List;

/**
 *
 * @author  <a href="mailto:gunter.zeilinger@tiani.com">gunter zeilinger</a>
 * @version 1.0.0
 */
public interface Association {
   
   public static int IDLE = 1;
   
   public static int AWAITING_READ_ASS_RQ = 2;
   public static int AWAITING_WRITE_ASS_RP = 3;
   public static int AWAITING_WRITE_ASS_RQ = 4;
   public static int AWAITING_READ_ASS_RP = 5;
   
   public static int ASSOCIATION_ESTABLISHED = 6;
   
   public static int AWAITING_READ_REL_RP = 7;
   public static int AWAITING_WRITE_REL_RP = 8;
   public static int RCRS_AWAITING_WRITE_REL_RP = 9;
   public static int RCAS_AWAITING_READ_REL_RP = 10;
   public static int RCRS_AWAITING_READ_REL_RP = 11;
   public static int RCAS_AWAITING_WRITE_REL_RP = 12;
   
   public static int ASSOCIATION_TERMINATING = 13;
   
   String getName();

   void setName(String name);
   
   int getState();

   String getStateAsString();

   void addAssociationListener(AssociationListener l);
   
   void removeAssociationListener(AssociationListener l);
   
   int nextMsgID();
   
   PDU connect(AAssociateRQ rq, int timeout) throws IOException;
   
   PDU accept(AcceptorPolicy policy, int timeout) throws IOException;
   
   Dimse read(int timeout) throws IOException;
   
   void write(Dimse dimse) throws IOException;
   
   PDU release(int timeout) throws IOException;
   
   void abort(AAbort aa) throws IOException;
   
   void setTCPCloseTimeout(int tcpCloseTimeout);
   
   int getTCPCloseTimeout();
   
   int getMaxOpsInvoked();
   
   int getMaxOpsPerformed();
   
   String getAcceptedTransferSyntaxUID(int pcid);
   
   PresContext getAcceptedPresContext(String asuid, String tsuid);
   
   AAssociateRQ getAAssociateRQ();
   
   AAssociateAC getAAssociateAC();
   
   AAssociateRJ getAAssociateRJ();
   
   AAbort getAAbort();
   
}