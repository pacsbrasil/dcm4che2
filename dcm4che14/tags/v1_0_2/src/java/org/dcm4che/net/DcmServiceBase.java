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

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.UIDGenerator;
import org.dcm4che.dict.Tags;

import java.io.IOException;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
public class DcmServiceBase implements DcmService
{   
   // Constants -----------------------------------------------------
   public static final int SUCCESS               = 0x0000;
   public static final int NO_SUCH_SOP_CLASS     = 0x0118;
   public static final int UNRECOGNIZE_OPERATION = 0x0211;
   
   public static final DcmService VERIFICATION_SCP = new DcmServiceBase()
   {
      protected void doCEcho(ActiveAssociation assoc, Dimse rq, Command rspCmd)
      throws IOException
      {
         rspCmd.putUS(Tags.Status, SUCCESS);
      }
   };
   public static final DcmService NO_SUCH_SOP_CLASS_SCP =
         new DcmServiceBase(NO_SUCH_SOP_CLASS);
   
   // Attributes ----------------------------------------------------
   protected static final DcmObjectFactory objFact =
         DcmObjectFactory.getInstance();

   protected static final Factory fact = Factory.getInstance();

   protected static final UIDGenerator uidGen = UIDGenerator.getInstance();
   
   protected final int defStatus;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public DcmServiceBase(int defStatus)
   {
      this.defStatus = defStatus;
   }
   
   public DcmServiceBase()
   {
      this.defStatus = UNRECOGNIZE_OPERATION;
   }
   
   // Public --------------------------------------------------------
      
   // DcmService implementation -------------------------------------
   public void c_store(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();
      Command rspCmd = objFact.newCommand();
      rspCmd.initCStoreRSP(
         rqCmd.getMessageID(),
         rqCmd.getAffectedSOPClassUID(),
         rqCmd.getAffectedSOPInstanceUID(),
         defStatus);
      doCStore(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
      assoc.getAssociation().write(rsp);
   }
   
   public void c_get(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initCGetRSP(
         rqCmd.getMessageID(),
         rqCmd.getAffectedSOPClassUID(),
         defStatus);
      doMultiRsp(assoc, rq, rspCmd, doCGet(assoc, rq, rspCmd));
   }
   
   public void c_find(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initCFindRSP(
         rqCmd.getMessageID(),
         rqCmd.getAffectedSOPClassUID(),
         defStatus);
      doMultiRsp(assoc, rq, rspCmd, doCFind(assoc, rq, rspCmd));
   }
   
   public void c_move(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initCMoveRSP(
         rqCmd.getMessageID(),
         rqCmd.getAffectedSOPClassUID(),
         defStatus);
      doMultiRsp(assoc, rq, rspCmd, doCMove(assoc, rq, rspCmd));
   }
   
   public void c_echo(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();
      Command rspCmd = objFact.newCommand();
      rspCmd.initCEchoRSP(
         rqCmd.getMessageID(),
         rqCmd.getAffectedSOPClassUID(),
         defStatus);
      doCEcho(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
      assoc.getAssociation().write(rsp);
   }

   public void n_event_report(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initNEventReportRSP(
         rqCmd.getMessageID(),
         rqCmd.getAffectedSOPClassUID(),
         rqCmd.getAffectedSOPInstanceUID(),
         defStatus);
      Dataset rspData = doNEventReport(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
      assoc.getAssociation().write(rsp);
   }
   
   public void n_get(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initNGetRSP(
         rqCmd.getMessageID(),
         rqCmd.getRequestedSOPClassUID(),
         rqCmd.getRequestedSOPInstanceUID(),
         defStatus);
      Dataset rspData = doNGet(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
      assoc.getAssociation().write(rsp);
   }
   
   public void n_set(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initNSetRSP(
         rqCmd.getMessageID(),
         rqCmd.getRequestedSOPClassUID(),
         rqCmd.getRequestedSOPInstanceUID(),
         0);
      Dataset rspData = doNSet(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
      assoc.getAssociation().write(rsp);
   }
   
   public void n_action(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initNActionRSP(
         rqCmd.getMessageID(),
         rqCmd.getRequestedSOPClassUID(),
         rqCmd.getRequestedSOPInstanceUID(),
         defStatus);
      Dataset rspData = doNAction(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
      assoc.getAssociation().write(rsp);
   }
   
   public void n_create(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initNCreateRSP(
         rqCmd.getMessageID(),
         rqCmd.getAffectedSOPClassUID(),
         createUID(rqCmd.getAffectedSOPInstanceUID()),
         defStatus);
      Dataset rspData = doNCreate(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
      assoc.getAssociation().write(rsp);
   }
   
   public void n_delete(ActiveAssociation assoc, Dimse rq)
   throws IOException
   {
      Command rqCmd = rq.getCommand();      
      Command rspCmd = objFact.newCommand();
      rspCmd.initNDeleteRSP(
         rqCmd.getMessageID(),
         rqCmd.getRequestedSOPClassUID(),
         rqCmd.getRequestedSOPInstanceUID(),
         defStatus);
      Dataset rspData = doNDelete(assoc, rq, rspCmd);
      Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
      assoc.getAssociation().write(rsp);
   }
      
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
   }
   
   protected MultiDimseRsp doCGet(ActiveAssociation assoc, Dimse rq,
         Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }

   protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq,
         Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }

   protected MultiDimseRsp doCMove(ActiveAssociation assoc, Dimse rq,
         Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }
   
   protected void doCEcho(ActiveAssociation assoc, Dimse rq, Command rspCmd)
   throws IOException
   {
//      rq.getDataset(); // read out dataset
   }

   protected Dataset doNEventReport(ActiveAssociation assoc, Dimse rq,
         Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }
   
   protected Dataset doNGet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }
   
   protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }
   
   protected Dataset doNAction(ActiveAssociation assoc, Dimse rq,
         Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }
   
   protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
         Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }

   protected Dataset doNDelete(ActiveAssociation assoc, Dimse rq,
         Command rspCmd)
   throws IOException
   {
      rq.getDataset(); // read out dataset
      return null;
   }

   // Private -------------------------------------------------------
   private void doMultiRsp(ActiveAssociation assoc, Dimse rq, Command rspCmd,
         MultiDimseRsp mdr)
   throws IOException
   {
      if (mdr == null) {
         assoc.getAssociation().write(fact.newDimse(rq.pcid(), rspCmd));
         return;
      }
      assoc.addCancelListener(rspCmd.getMessageIDToBeingRespondedTo(),
            mdr.getCancelListener());
      do {
         Dataset rspData = mdr.next(assoc, rq, rspCmd);
         Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
         assoc.getAssociation().write(rsp);
      } while (rspCmd.isPending());
   }
      
   private static String createUID(String uid)
   {
      return uid != null ? uid : uidGen.createUID();
   }
   
   // Inner classes -------------------------------------------------
   public static interface MultiDimseRsp
   {
      DimseListener getCancelListener();

      Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd);   
   }
}
