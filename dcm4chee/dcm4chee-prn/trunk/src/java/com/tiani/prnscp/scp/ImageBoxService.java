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
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.util.UIDGenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
class ImageBoxService extends DcmServiceBase
{
   // Constants -----------------------------------------------------
   private static final int BUFFER_LEN = 512;
   
   // Attributes ----------------------------------------------------
   private final DcmParserFactory dpf = DcmParserFactory.getInstance();
   private final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   private final UIDGenerator uidgen = UIDGenerator.getInstance();
   private final PrintScpService scp;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public ImageBoxService(PrintScpService scp) {
      this.scp = scp;
   }      
   
   // Public --------------------------------------------------------
   
      
   // DcmServiceBase overrides --------------------------------------   
   protected Dataset doNSet(ActiveAssociation as, Dimse rq, Command rspCmd)
      throws IOException, DcmServiceException
   {
      Command cmd = rq.getCommand();
      String cuid = cmd.getRequestedSOPClassUID();
      String iuid = cmd.getRequestedSOPInstanceUID();
      String tuid = rq.getTransferSyntaxUID();
      String suid = FilmSessionService.getFilmSessionUID(as);
      String fuid = FilmBoxService.getFilmBoxUID(as);
      int stopTag = Tags.BasicGrayscaleImageSeq;
      String hcuid = UIDs.HardcopyGrayscaleImageStorage;
      if (cuid.equals(UIDs.BasicColorImageBox)) {
         stopTag = Tags.BasicColorImageSeq;
         hcuid = UIDs.HardcopyColorImageStorage;
      }
      InputStream in = rq.getDataAsStream();
      try {
         DcmParser parser = dpf.newDcmParser(in);
         Dataset box = dof.newDataset();
         parser.setDcmHandler(box.getDcmHandler());
         parser.parseDataset(tuid, stopTag);
         if (stopTag != parser.getReadTag()) {
            throw new DcmServiceException(Status.MissingAttribute,
               "Missing " + Tags.toString(stopTag));
         }
         int sqLen = parser.getReadLength();
         if (sqLen != 0) {
            int itemTag = parser.parseHeader();
            if (sqLen == -1 && itemTag == Tags.SeqDelimitationItem) {
               sqLen = 0;
            }
         }
         if (sqLen != 0) {
            int itemLen = parser.getReadLength();
            String hiuid = uidgen.createUID();
            File f = new File(scp.getHardCopyDir(suid), hiuid);
            Dataset hc = createHC(suid, fuid, hcuid, hiuid, box);
            parser.setDcmHandler(hc.getDcmHandler());
            parser.parseDataset(tuid, Tags.PixelData);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
            try {
               hc.writeFile(out, null);
               hc.writeHeader(out, DcmEncodeParam.EVR_LE,
                   parser.getReadTag(),
                   parser.getReadVR(),
                   parser.getReadLength());               
               copy(in, out, parser.getReadLength());
            } finally {
               try { out.close(); } catch (IOException ignore) {}
            }
            if (itemLen == -1) {
               parser.parseHeader(); // skip Item Delim
            }
            if (sqLen == -1) {
               parser.parseHeader(); // skip Seq Delim
            }
            parser.setDcmHandler(box.getDcmHandler());
         }
         parser.parseDataset(tuid, -1);
         File boxfile = new File(scp.getFilmBoxDir(suid, fuid), iuid);
         scp.writeDataset(boxfile, box);
      } finally {
         try { in.close(); } catch (IOException ignore) {}
      }
      return null;
   }
   
   private void copy(InputStream in, OutputStream out, int len)
      throws IOException, DcmServiceException
   {
      byte[] buffer = new byte[BUFFER_LEN];      
      int c, toread = len;
      while (toread > 0) {
         c = in.read(buffer, 0, Math.min(toread, BUFFER_LEN));
         if (c == -1) {
            throw new DcmServiceException(Status.InvalidAttributeValue);
         }
         out.write(buffer, 0, c);
         toread -= c;
      }      
   }
   
   private Dataset createHC(String suid, String fuid, String cuid, String iuid,
         Dataset box) {
      Dataset ds = dof.newDataset();
      ds.setFileMetaInfo(
         dof.newFileMetaInfo(cuid, iuid, UIDs.ExplicitVRLittleEndian));
      Dataset item = box.putSQ(Tags.RefImageSeq).addNewItem();
      item.putAE(Tags.RetrieveAET, "UNKOWN");
      ds.putCS(Tags.ImageType, new String[]{ "DERIVED", "SECONDARY" });
      ds.putUI(Tags.SOPClassUID, cuid);
      item.putUI(Tags.RefSOPClassUID, cuid);      
      ds.putUI(Tags.SOPInstanceUID, iuid);
      item.putUI(Tags.RefSOPInstanceUID, iuid);      
      ds.putDA(Tags.StudyDate);
      ds.putTM(Tags.StudyTime);
      ds.putSH(Tags.AccessionNumber);
      ds.putCS(Tags.Modality, "HC");
      ds.putLO(Tags.Manufacturer, "TIANI MEDGRAPH AG");
      ds.putPN(Tags.PatientName);
      ds.putLO(Tags.PatientID);
      item.putLO(Tags.PatientID);
      ds.putDA(Tags.PatientBirthDate);
      ds.putCS(Tags.PatientSex);
      ds.putUI(Tags.StudyInstanceUID, suid);
      item.putUI(Tags.StudyInstanceUID, suid);
      ds.putUI(Tags.SeriesInstanceUID, fuid);
      item.putUI(Tags.SeriesInstanceUID, fuid);
      ds.putSH(Tags.StudyID);
      ds.putIS(Tags.SeriesNumber);
      ds.putIS(Tags.InstanceNumber);
      ds.putCS(Tags.PatientOrientation);
      return ds;
   }
      
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
