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
import org.dcm4che.data.DcmValueException;
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
   private final UIDGenerator gen = UIDGenerator.getInstance();
   private final PrintScpService scp;
   
   // Static --------------------------------------------------------
   static String getFilmBoxUID(ActiveAssociation as) {
      return (String) as.getAssociation().getProperty("FilmBox");
   }

   static String checkFilmBoxUID(ActiveAssociation as, Dimse rq)
      throws DcmServiceException 
   {
      String uid = rq.getCommand().getRequestedSOPInstanceUID();
      if (!uid.equals(getFilmBoxUID(as))) {
         throw new DcmServiceException(Status.NoSuchObjectInstance);
      }
      return uid;
   }
   
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
         String suid = FilmSessionService.getFilmSessionUID(as);
         String fuid = rq.getCommand().getAffectedSOPInstanceUID();
         File dir = scp.getFilmBoxDir(suid, fuid);
         if (dir.exists()) {
            throw new DcmServiceException(Status.DuplicateSOPInstance);
         }
         createImageBoxes(ds, getImageBoxCUID(as.getAssociation(), rq.pcid()));
         dir.mkdir();
         try {
            scp.writeDataset(new File(dir, "ATTR"), ds);
         } catch (DcmServiceException e) {
            scp.rmdir(dir);
            throw e;
         }
         Association a = as.getAssociation();
         a.putProperty("FilmBox", fuid);
         return ds;
      } catch (DcmServiceException e) {
         scp.getLog().warn("Failed to create Basic Film Box SOP Instance", e);
         throw e;
      }
   }
   
   private String getImageBoxCUID(Association a, int pcid)
      throws DcmServiceException 
   {
      AAssociateRQ rq = a.getAAssociateRQ();
      PresContext pc =rq.getPresContext(pcid);
      String as = pc.getAbstractSyntaxUID();
      if (as.equals(UIDs.BasicGrayscalePrintManagement)) {
         return UIDs.BasicGrayscaleImageBox;
      }
      if (as.equals(UIDs.BasicColorPrintManagement)) {
         return UIDs.BasicColorImageBox;
      }
      throw new DcmServiceException(Status.ProcessingFailure,
               "Wrong Presentation Context - " + as);
   }
   
   private void createImageBoxes(Dataset data, String cuid)
      throws DcmServiceException
   {
      int n = numImageBoxes(data);
      DcmElement sq = data.putSQ(Tags.RefImageBoxSeq);
      for (int i = 0; i < n; ++i) {
         Dataset item = sq.addNewItem();
         item.putUI(Tags.RefSOPClassUID, cuid);
         item.putUI(Tags.RefSOPInstanceUID, gen.createUID());
      }
   }
      
   private int numImageBoxes(Dataset data) throws DcmServiceException {
      try {
         String format = data.getString(Tags.ImageDisplayFormat);
         if (format == null) {
            throw new DcmServiceException(Status.MissingAttribute,
               "Missing Image Display Format (2010,0010)");
         }
         if (!format.startsWith("STANDARD")) {
            throw new DcmServiceException(Status.InvalidArgumentValue,
               "Unsupported Image Display Format (2010,0010) - " + format);
         }      
         try {
            int del = format.indexOf(',');
            int c = Integer.parseInt(format.substring(9,del));
            int r = Integer.parseInt(format.substring(del+1));
            if (c <= 0 || r <= 0) {
               throw new IllegalArgumentException();
            }
            return c * r;
         } catch (Exception e) {
            throw new DcmServiceException(Status.InvalidArgumentValue,
               "Invalid Image Display Format (2010,0010) - " + format);
         }
      } catch (DcmValueException e) {
         throw new DcmServiceException(Status.InvalidArgumentValue, e);
      }
   }
   
   protected Dataset doNSet(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException 
   {
      try {
         Dataset ds = rq.getDataset(); // read out dataset
         File dir = scp.getFilmBoxDir(
            FilmSessionService.getFilmSessionUID(as), 
            checkFilmBoxUID(as, rq));
         // TO DO
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
         File dir = scp.getFilmBoxDir(
            FilmSessionService.getFilmSessionUID(as), 
            checkFilmBoxUID(as, rq));
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
         String suid = FilmSessionService.getFilmSessionUID(as);
         String fuid = checkFilmBoxUID(as, rq);
         File dir = scp.getFilmBoxDir(suid, fuid);
         scp.rmdir(dir);
         Association a = as.getAssociation();
         a.putProperty("FilmBox", null);
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
