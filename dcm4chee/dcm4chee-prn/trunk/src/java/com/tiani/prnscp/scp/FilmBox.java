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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.util.UIDGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

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
   private final PrintScpService scp;
   private final String uid;
   private final Dataset dataset;
   private final LinkedHashMap imageBoxes = new LinkedHashMap();
   private final HashMap pluts = new HashMap();
   
   // Static --------------------------------------------------------
   private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   private static final UIDGenerator uidgen = UIDGenerator.getInstance();
   
   // Constructors --------------------------------------------------
   public FilmBox(PrintScpService scp, String uid, Dataset dataset,
         Command rspCmd, HashMap pluts)
      throws DcmServiceException
   {
      this.scp = scp;
      this.uid = uid;
      this.dataset = dataset;
      checkCreateData(dataset, rspCmd);
      addPLUT(dataset, pluts);
   }
   
   // Public --------------------------------------------------------
   public String toString() {
      return "FilmBox[uid=" + uid + ", " + imageBoxes.size() + " ImageBoxes]";
   }      
   
   public Dataset getAttributes() {
      return dataset;
   }

   public void updateAttributes(Dataset modification, Command rspCmd, HashMap pluts)
      throws DcmServiceException
   {
//      check(modification, rspCmd);
      addPLUT(modification, pluts);
      dataset.putAll(modification);
   }

   public void setImageBox(String imageBoxUID, Dataset imageBox, HashMap pluts)
      throws DcmServiceException
   {
      checkDecimateCropBehavior(
         scp.getCSorDefConfig(imageBox, Tags.RequestedDecimateCropBehavior, 
            "DecimateCropBehavior"));
      addPLUT(imageBox, pluts);
      imageBoxes.remove(imageBoxUID);
      imageBoxes.put(imageBoxUID, imageBox);
   }
   
   public Dataset createStoredPrint(FilmSession session)
   {
      boolean debug = scp.getLog().isDebugEnabled();
      
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
      if (!pluts.isEmpty()) {
         pmcs.addNewItem().putUI(Tags.RefSOPClassUID, UIDs.PresentationLUT);
      }
      result.putSQ(Tags.PrinterCharacteristicsSeq);
      
      Dataset fbContent = result.putSQ(Tags.FilmBoxContentSeq).addNewItem();
      fbContent.putAll(dataset);
      fbContent.remove(Tags.RefFilmSessionSeq);
      DcmElement refImageBoxSeq = fbContent.remove(Tags.RefImageBoxSeq);
      
      Dataset[] boxes = new Dataset[refImageBoxSeq.vm()];
      for (Iterator it = imageBoxes.values().iterator(); it.hasNext();) {
         Dataset box = (Dataset) it.next();
         int ipos = box.getInt(Tags.ImagePositionOnFilm, -1);
         boxes[ipos-1] = box;
      }
      
      DcmElement boxSeq = result.putSQ(Tags.ImageBoxContentSeq);
      for (int i = 0; i < boxes.length; ++i) {
         if (boxes[i] != null && boxes[i].contains(Tags.RefImageSeq)) {
            boxSeq.addItem(boxes[i]);
         }
      }
      
      if (!pluts.isEmpty()) {
         DcmElement plutSeq = result.putSQ(Tags.PresentationLUTContentSeq);
         for (Iterator it = pluts.values().iterator(); it.hasNext();) {
            plutSeq.addItem((Dataset) it.next());
         }
      }
      return result;
   }
   
   private void addPLUT(Dataset dataset, HashMap pluts)
      throws DcmServiceException
   {
      Dataset ref = dataset.getItem(Tags.RefPresentationLUTSeq);
      if (ref == null) {
         return;
      }
      String cuid = ref.getString(Tags.RefSOPClassUID);
      if (cuid == null) {
         throw new DcmServiceException(Status.MissingAttributeValue);
      }
      if (!UIDs.PresentationLUT.equals(cuid)) {
         throw new DcmServiceException(Status.InvalidAttributeValue);
      }
      String iuid = ref.getString(Tags.RefSOPInstanceUID);
      if (iuid == null) {
         throw new DcmServiceException(Status.MissingAttributeValue);
      }
      Dataset plut = (Dataset) pluts.get(iuid);
      if (plut == null) {
         throw new DcmServiceException(Status.InvalidAttributeValue,
            "No such Presentation LUT");
      }
      this.pluts.put(iuid, plut);
   }
      
   private String checkFilmOrientation(String val)
      throws DcmServiceException
   {
      if (!val.equals("PORTRAIT") && !val.equals("LANDSCAPE")) {
         throw new DcmServiceException(Status.InvalidAttributeValue);
      }
      return val;
   }

   private String checkDecimateCropBehavior(String val)
      throws DcmServiceException
   {
      if (!val.equals("DECIMATE") && !val.equals("CROP") && !val.equals("FAIL")) {
         throw new DcmServiceException(Status.InvalidAttributeValue);
      }
      return val;
   }
   
   private String checkDensity(String val)
      throws DcmServiceException
   {
      if (!val.equals("WHITE") && !val.equals("BLACK")) {
         try {
            Integer.parseInt(val);
         } catch (NumberFormatException e) {
            throw new DcmServiceException(Status.InvalidAttributeValue);
         }
      }
      return val;
   }
   
   private void checkCreateData(Dataset ds, Command rsp)
      throws DcmServiceException
   {
      scp.checkImageDisplayFormat(ds.getString(Tags.ImageDisplayFormat),
         checkFilmOrientation(
            scp.getCSorDefConfig(ds, Tags.FilmOrientation, "DefaultFilmOrientation")));
      scp.checkAttributeValue("isSupportsFilmSizeID",
         scp.getCSorDefConfig(ds, Tags.FilmSizeID, "DefaultFilmSizeID"));
      scp.checkAttributeValue("isSupportsMagnificationType",
         scp.getCSorDefConfig(ds, Tags.MagnificationType, "DefaultMagnificationType"));
      scp.checkAttributeValue("isSupportsSmoothingType",
         scp.getCSorDefConfig(ds, Tags.SmoothingType, "DefaultSmoothingType"));
      scp.getUSorDefConfig(ds, Tags.Illumination, "Illumination");
      scp.getUSorDefConfig(ds, Tags.ReflectedAmbientLight, "ReflectedAmbientLight");
      scp.getUSorDefConfig(ds, Tags.MinDensity, "MinDensity");
      scp.getUSorDefConfig(ds, Tags.MaxDensity, "MaxDensity");
      checkDensity(
         scp.getCSorDefConfig(ds, Tags.BorderDensity, "BorderDensity"));
      checkDensity(
         scp.getCSorDefConfig(ds, Tags.EmptyImageDensity, "EmptyImageDensity"));
      scp.checkAttributeValue("isSupportsResolutionID",
         scp.getCSorDefConfig(ds, Tags.RequestedResolutionID, "DefaultResolutionID"));
   }
}
