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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;

import java.util.LinkedHashMap;
import java.util.Iterator;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 14, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class FilmBox {
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private final Dataset dataset;
   private final LinkedHashMap imageBoxes = new LinkedHashMap();
   
   // Static --------------------------------------------------------
   private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   private static final UIDGenerator uidgen = UIDGenerator.getInstance();
   
   // Constructors --------------------------------------------------
   public FilmBox(Dataset dataset) {
      this.dataset = dataset;
   }
   
   // Public --------------------------------------------------------
   public Dataset getDataset() {
      return dataset;
   }

   public void setDataset(Dataset dataset) {
      this.dataset.putAll(dataset);
   }

   public void setImageBox(String imageBoxUID, Dataset imageBox) {
      imageBoxes.remove(imageBoxUID);
      imageBoxes.put(imageBoxUID, imageBox);
   }
   
   public Dataset createStoredPrint(FilmSession session)
      throws DcmValueException
   {
      Dataset result = dof.newDataset();
      String cuid = UIDs.StoredPrintStorage;
      String iuid = uidgen.createUID();
      result.setFileMetaInfo(
      dof.newFileMetaInfo(cuid, iuid, UIDs.ExplicitVRLittleEndian));
      result.putUI(Tags.SOPClassUID, cuid);
      result.putUI(Tags.SOPInstanceUID, iuid);
      result.putDA(Tags.StudyDate);
      result.putTM(Tags.StudyTime);
      result.putSH(Tags.AccessionNumber);
      result.putCS(Tags.Modality, "STORED_PRINT");
      result.putLO(Tags.Manufacturer, "TIANI MEDGRAPH AG");
      result.putPN(Tags.PatientName);
      result.putLO(Tags.PatientID);
      result.putDA(Tags.PatientBirthDate);
      result.putCS(Tags.PatientSex);
      // use session uid as Study UID
      result.putUI(Tags.StudyInstanceUID, session.uid());
      result.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
      result.putSH(Tags.StudyID);
      result.putIS(Tags.SeriesNumber);
      result.putIS(Tags.InstanceNumber);
      
      DcmElement pmcs = result.putSQ(Tags.PrintManagementCapabilitiesSeq);
      pmcs.addNewItem().putUI(Tags.RefSOPClassUID, UIDs.BasicFilmSession);
      pmcs.addNewItem().putUI(Tags.RefSOPClassUID, UIDs.BasicFilmBoxSOP);
      pmcs.addNewItem().putUI(Tags.RefSOPClassUID, session.getImageBoxCUID());
      pmcs.addNewItem().putUI(Tags.RefSOPClassUID, session.getHardcopyCUID());
      
      result.putSQ(Tags.PrinterCharacteristicsSeq);
      
      Dataset fbContent = result.putSQ(Tags.FilmBoxContentSeq).addNewItem();
      fbContent.putAll(dataset);
      fbContent.remove(Tags.RefFilmSessionSeq);
      DcmElement refImageBoxSeq = fbContent.remove(Tags.RefImageBoxSeq);
      
      Dataset[] boxes = new Dataset[refImageBoxSeq.vm()];
      Iterator it = imageBoxes.values().iterator();
      while (it.hasNext()) {
         Dataset box = (Dataset) it.next();
         int ipos = box.getInt(Tags.ImagePositionOnFilm, -1);
         boxes[ipos-1] = box;
      }
      
      DcmElement ibContentSeq = result.putSQ(Tags.ImageBoxContentSeq);
      for (int i = 0; i < boxes.length; ++i) {
         if (boxes[i] != null && boxes[i].contains(Tags.RefImageSeq)) {
            ibContentSeq.addItem(boxes[i]);
         }
      }
      return result;
   }
   
}
