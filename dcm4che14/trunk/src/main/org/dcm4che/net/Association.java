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
   
   int nextMsgID();

   PDU connect(AAssociateRQ rq, int timeout) throws IOException;
   
   PDU accept(AcceptorPolicy policy, int timeout) throws IOException;

   Dimse read(int timeout) throws IOException;
   
   void write(Dimse dimse) throws IOException;
   
   PDU release(int timeout) throws IOException;
   
   void abort(AAbort aa) throws IOException;

   AssociationState getState();
   
   void setTCPCloseTimeout(int tcpCloseTimeout);
   
   int getTCPCloseTimeout();
   
   String getAcceptedTransferSyntaxUID(int pcid);
   
   PresContext getAcceptedPresContext(String asuid, String tsuid);
}

