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
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.util.UIDGenerator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
class FilmBoxService extends DcmServiceBase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private final PrintScpService scp;
   
   // Static --------------------------------------------------------
   private static final UIDGenerator uidgen = UIDGenerator.getInstance();
   
   // Constructors --------------------------------------------------
   public FilmBoxService(PrintScpService scp) {
      this.scp = scp;
   }      
   
   // Public --------------------------------------------------------
   
      
   // DcmServiceBase overrides --------------------------------------

   protected Dataset doNCreate(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException 
   {
      try {
         Dataset ds = rq.getDataset(); // read out dataset
         FilmSession session = scp.getFilmSession(as);
         HashMap pluts = scp.getPresentationLUTs(as);
         checkRefFilmSession(ds, session);
         String uid = rq.getCommand().getAffectedSOPInstanceUID();
         if (session.containsFilmBox(uid)) {
            throw new DcmServiceException(Status.DuplicateSOPInstance);
         }
         addRefImageBox(ds, session.getImageBoxCUID());
         session.addFilmBox(uid, new FilmBox(scp, uid, ds, pluts));
         return ds;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to create Basic Film Box SOP Instance", e);
         throw e;
      }
   }

   private void checkRefFilmSession(Dataset ds, FilmSession session)
      throws DcmServiceException
   {
      if (session == null) {
         throw new DcmServiceException(Status.ProcessingFailure,
            "No Film Session");
      }
      try {
         Dataset ref = ds.getItem(Tags.RefFilmSessionSeq);
         if (!ref.getString(Tags.RefSOPClassUID).equals(UIDs.BasicFilmSession)) {
            throw new DcmServiceException(Status.InvalidAttributeValue);
         }
         if (!ref.getString(Tags.RefSOPInstanceUID).equals(session.uid())) {
            throw new DcmServiceException(Status.InvalidAttributeValue);
         }         
      } catch (NullPointerException e) {
         throw new DcmServiceException(Status.MissingAttribute);
      }
   }       
   
   void addRefImageBox(Dataset data, String cuid)
      throws DcmServiceException
   {
      int n = scp.countImageBoxes(data.getString(Tags.ImageDisplayFormat));
      DcmElement sq = data.putSQ(Tags.RefImageBoxSeq);
      for (int i = 0; i < n; ++i) {
         Dataset item = sq.addNewItem();
         item.putUI(Tags.RefSOPClassUID, cuid);
         item.putUI(Tags.RefSOPInstanceUID, uidgen.createUID());
      }
   }
   
   protected Dataset doNSet(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException 
   {
      try {
         Dataset ds = rq.getDataset(); // read out dataset
         String uid = rq.getCommand().getRequestedSOPInstanceUID();
         FilmSession session = scp.getFilmSession(as);
         HashMap pluts = scp.getPresentationLUTs(as);
         if (session == null || !uid.equals(session.getCurrentFilmBoxUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
         }
         session.getCurrentFilmBox().updateAttributes(ds, pluts);
         return null;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to update Basic Film Box SOP Instance", e);
         throw e;
      }
   }
   
   protected Dataset doNAction(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException
   {
      try {
         String uid = rq.getCommand().getRequestedSOPInstanceUID();
         FilmSession session = scp.getFilmSession(as);
         if (session == null || !uid.equals(session.getCurrentFilmBoxUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
         }
         scp.createPrintJob(session, false);
         return null;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to print Basic Film Box SOP Instance", e);
         throw e;
      }
   }
   
   protected Dataset doNDelete(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException 
   {
      try {
         String uid = rq.getCommand().getRequestedSOPInstanceUID();
         FilmSession session = scp.getFilmSession(as);
         if (session == null || !uid.equals(session.getCurrentFilmBoxUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
         }
         session.deleteFilmBox();
         return null;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to delete Basic Film Box SOP Instance", e);
         throw e;
      }
      
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
