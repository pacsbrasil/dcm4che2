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

package com.tiani.prnscp.scp;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;

import java.io.File;
import java.io.IOException;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 9, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class FilmSessionService
   extends DcmServiceBase 
   implements AssociationListener
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private final PrintScpService scp;
   
   // Static --------------------------------------------------------
      
   // Constructors --------------------------------------------------
   public FilmSessionService(PrintScpService scp) {
      this.scp = scp;
   }      
   
   // Public --------------------------------------------------------

   // AssociationListener implementation ----------------------------
   public void close(Association as) {
      FilmSession session = (FilmSession) as.getProperty("FilmSession");
      if (session != null) {
         scp.unlockSessionSpoolDir(session.dir());
      }
   }
   
   public void error(Association association, IOException iOException) {
   }
   
   public void received(Association association, Dimse dimse) {
   }
   
   public void received(Association association, PDU pDU) {
   }
   
   public void write(Association association, Dimse dimse) {
   }
   
   public void write(Association association, PDU pDU) {
   }
   
   // DcmServiceBase overrides --------------------------------------
   protected Dataset doNCreate(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException 
   {
      try {
         Dataset ds = rq.getDataset(); // read out dataset
         if (scp.getFilmSession(as) != null) {
            throw new DcmServiceException(Status.ProcessingFailure,
               "Only support one Basic Film Session SOP Instance on an Association.");
         }
         String uid = rq.getCommand().getAffectedSOPInstanceUID();
         File dir = scp.getSessionSpoolDir(uid);
         if (dir.exists()) {
            throw new DcmServiceException(Status.DuplicateSOPInstance);
         }         
         Association a = as.getAssociation();
         AAssociateRQ aarq = a.getAAssociateRQ();
         PresContext pc =aarq.getPresContext(rq.pcid());
         String asuid = pc.getAbstractSyntaxUID();
         FilmSession session = new FilmSession(scp, asuid, uid, ds, dir, rspCmd);
         scp.initSessionSpoolDir(dir);
         a.putProperty("FilmSession", session);
         a.addAssociationListener(this);
         return null;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to create Basic Film Session SOP Instance", e);
         throw e;
      }
   }
      
   protected Dataset doNSet(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException 
   {
      try {
         Dataset ds = rq.getDataset(); // read out dataset
         String uid = rq.getCommand().getRequestedSOPInstanceUID();
         FilmSession session = scp.getFilmSession(as);
         if (session == null || !uid.equals(session.uid())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
         }
         session.updateAttributes(ds, rspCmd);
         return null;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to update Basic Film Session SOP Instance", e);
         throw e;
      }
   }
   
   protected Dataset doNAction(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException
   {
      try {
         String uid = rq.getCommand().getRequestedSOPInstanceUID();
         FilmSession session = scp.getFilmSession(as);
         if (session == null || !uid.equals(session.uid())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
         }
         scp.createPrintJob(session, true);
         return null;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to print Basic Film Session SOP Instance", e);
         throw e;
      }
   }
   
   protected Dataset doNDelete(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException 
   {
      try {
         String uid = rq.getCommand().getRequestedSOPInstanceUID();
         FilmSession session = scp.getFilmSession(as);
         if (session == null || !uid.equals(session.uid())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
         }
         scp.unlockSessionSpoolDir(session.dir());
         Association a = as.getAssociation();
         a.putProperty("FilmSession", null);
         a.removeAssociationListener(this);
         return null;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to delete Basic Film Session SOP Instance", e);
         throw e;
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
