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

package org.dcm4cheri.net;

import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.AAbort;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.dict.Tags;

import org.dcm4cheri.util.LF_ThreadPool;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class AssociationImpl implements Association {
   
   private final FsmImpl fsm;
   private final DimseReaderImpl reader;
   private final DimseWriterImpl writer;
   private int msgID = 0;
   private final byte[] b10 = new byte[10];
   
   /** Creates a new instance of AssociationImpl */
   public AssociationImpl(Socket s, boolean requestor) throws IOException {
      this.fsm = new FsmImpl(this, s, requestor);
      this.reader = new DimseReaderImpl(fsm);
      this.writer = new DimseWriterImpl(fsm);
   }

   public String toString() {
      return fsm.toString();
   }
   
   public void addAssociationListener(AssociationListener l) {
      fsm.addAssociationListener(l);
   }
   
   public void removeAssociationListener(AssociationListener l) {
      fsm.removeAssociationListener(l);
   }

   public final int getState() {
      return fsm.getState();
   }
   
   public synchronized final int nextMsgID() {
      return ++msgID;
   }
   
   public int getMaxOpsInvoked() {    
      return fsm.getMaxOpsInvoked();
   }
    
   public int getMaxOpsPerformed() {
      return fsm.getMaxOpsPerformed();
   }
   
   public void setThreadPool(LF_ThreadPool pool) {
     fsm.setThreadPool(pool);
     reader.setThreadPool(pool);
   }
   
   public final void setTCPCloseTimeout(int tcpCloseTimeout) {
      fsm.setTCPCloseTimeout(tcpCloseTimeout);
   }
   
   public final int getTCPCloseTimeout() {
      return fsm.getTCPCloseTimeout();
   }
      
   public final PDU connect(AAssociateRQ rq, int timeout) throws IOException {
      fsm.write(rq);
      return fsm.read(timeout, b10);
   }
   
   public final PDU accept(AcceptorPolicy policy, int timeout) throws IOException {
      PDU rq = fsm.read(timeout, b10);
      if (!(rq instanceof AAssociateRQ))
         return (AAbort)rq;

      PDU rp = policy.negotiate((AAssociateRQ)rq);
      if (rp instanceof AAssociateAC)
         fsm.write((AAssociateAC)rp);
      else
         fsm.write((AAssociateRJ)rp);
      return rp;
   }

   public final Dimse read(int timeout) throws IOException  {
      Dimse dimse = reader.read(timeout);
      if (dimse != null) {
         msgID = Math.max(
               dimse.getCommand().getInt(Tags.MessageID, msgID),
               msgID);
      }
      return dimse;
   }
   
   public final void write(Dimse dimse) throws IOException  {
      msgID = Math.max(
            dimse.getCommand().getInt(Tags.MessageID, msgID),
            msgID);
      writer.write(dimse);
   }
   
   public final PDU release(int timeout) throws IOException {
      fsm.write(AReleaseRQImpl.getInstance());
      return fsm.read(timeout, b10);
   }
   
   final void writeReleaseRQ() throws IOException {
      fsm.write(AReleaseRQImpl.getInstance());
   }
   
   public final void abort(AAbort aa) throws IOException {
      fsm.write(aa);
   }
   
   public final String getAcceptedTransferSyntaxUID(int pcid) {
      return fsm.getAcceptedTransferSyntaxUID(pcid);
   }
   
   public final PresContext getAcceptedPresContext(String asuid, String tsuid) {
      return fsm.getAcceptedPresContext(asuid, tsuid);
   }
 }
