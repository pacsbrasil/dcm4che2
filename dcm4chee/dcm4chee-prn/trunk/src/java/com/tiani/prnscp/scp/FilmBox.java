/*
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
 */
package com.tiani.prnscp.scp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.util.UIDGenerator;

import org.jboss.logging.Logger;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  November 14, 2002
 * @version  $Revision$
 */
class FilmBox
{

    // Constants -----------------------------------------------------    
    private static final String[] FILM_ORIENTATION = {
        "PORTRAIT",
        "LANDSCAPE"
    };

    private static final String[] BLACK_WHITE = {
        "BLACK",
        "WHITE"
    };

    private static final String[] YES_NO = {
        "YES",
        "NO"
    };
    
    private static final String[] DECIMATE_CROP = {
        "DECIMATE",
        "CROP"
    };
    
    // Attributes ----------------------------------------------------
    private final PrintScpService scp;
    private final Logger log;
    private final String aet;
    private final String uid;
    private final Dataset dataset;
    private final boolean color;
    private final LinkedHashMap imageBoxes = new LinkedHashMap();
    private final LinkedHashMap annotationBoxes = new LinkedHashMap();
    private final HashMap pluts = new HashMap();

    // Static --------------------------------------------------------
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private static final UIDGenerator uidgen = UIDGenerator.getInstance();


    // Constructors --------------------------------------------------
    /**
     *  Constructor for the FilmBox object
     *
     * @param  scp Description of the Parameter
     * @param  aet Description of the Parameter
     * @param  uid Description of the Parameter
     * @param  dataset Description of the Parameter
     * @param  pluts Description of the Parameter
     * @param  session Description of the Parameter
     * @exception  DcmServiceException Description of the Exception
     */
    public FilmBox(PrintScpService scp, String aet, String uid, Dataset dataset,
            HashMap pluts, FilmSession session, Command rspCmd)
        throws DcmServiceException
    {
        this.scp = scp;
        this.log = scp.getLog();;
        this.aet = aet;
        this.uid = uid;
        this.dataset = dataset;
        this.color = session.isColor();
        checkCreateAttributes(dataset, rspCmd);
        addRefImageBox(dataset, session.getImageBoxCUID());
        addRefAnnotationBox(dataset);
        addPLUT(dataset, pluts);
    }

    private void checkCreateAttributes(Dataset ds, Command rsp)
        throws DcmServiceException
    {
        try {
            scp.checkImageDisplayFormat(ds, aet, rsp);
            scp.checkAttribute(ds, Tags.AnnotationDisplayFormatID,
                aet, "isSupportsAnnotationDisplayFormatID", rsp);
            scp.checkAttribute(ds, Tags.FilmSizeID,
                aet, "isSupportsFilmSizeID", rsp);
            scp.checkAttribute(ds, Tags.FilmOrientation, FILM_ORIENTATION, rsp);
            checkAttributes(ds, rsp);
        } catch (DcmServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Processing Failure:", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private void checkSetAttributes(Dataset ds, Command rsp)
        throws DcmServiceException
    {
        try {
            scp.ignoreAttribute(ds, Tags.ImageDisplayFormat, rsp);
            scp.ignoreAttribute(ds, Tags.AnnotationDisplayFormatID, rsp);
            scp.ignoreAttribute(ds, Tags.FilmSizeID, rsp);
            scp.ignoreAttribute(ds, Tags.FilmOrientation, rsp);
            checkAttributes(ds, rsp);
        } catch (Exception e) {
            log.error("Processing Failure:", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private void checkAttributes(Dataset ds, Command rsp)
    throws Exception
    {
        scp.checkAttribute(ds, Tags.BorderDensity, BLACK_WHITE, rsp);
        scp.ignoreAttribute(ds, Tags.EmptyImageDensity, rsp);
//            checkIllumination(ds, rsp);
//            checkReflectedAmbientLight(ds, rsp);
        scp.checkAttribute(ds, Tags.RequestedResolutionID,
            aet, "isSupportsResolutionID", rsp);
        scp.checkMinMaxDensity(ds, aet, color, rsp);
        checkImageBoxAttributes(ds, rsp);
    }
    
    private void checkImageBoxAttributes(Dataset ds, Command rsp)
    throws Exception
    {
        scp.checkAttribute(ds, Tags.MagnificationType,
            aet, "isSupportsMagnificationType", rsp);
        scp.ignoreAttribute(ds, Tags.SmoothingType, rsp);
        scp.checkAttribute(ds, Tags.Trim, YES_NO, rsp);
        scp.checkAttribute(ds, Tags.ConfigurationInformation,
            aet, "isSupportsConfigurationInformation", rsp);
    }
    
    private int countImageBoxes(String imageDisplayFormat)
    {
        StringTokenizer tok = new StringTokenizer(imageDisplayFormat, ",\\");
        try {
            String type = tok.nextToken();
            if (type.equals("STANDARD")) {
                int c = Integer.parseInt(tok.nextToken());
                int r = Integer.parseInt(tok.nextToken());
                return c * r;
            }
            if (type.equals("ROW") || type.equals("COL")) {
                int sum = 0;
                while (tok.hasMoreTokens()) {
                    sum += Integer.parseInt(tok.nextToken());
                }
                return sum;
            }
            // TO DO support of other types: SLIDE, SUPERSLIDE, CUSTOM/i
        } catch (RuntimeException e) {
        }
        throw new IllegalArgumentException(imageDisplayFormat);
    }


    void addRefImageBox(Dataset data, String cuid)
        throws DcmServiceException
    {
        int n = countImageBoxes(data.getString(Tags.ImageDisplayFormat));
        DcmElement sq = data.putSQ(Tags.RefImageBoxSeq);
        for (int i = 0; i < n; ++i) {
            Dataset item = sq.addNewItem();
            item.putUI(Tags.RefSOPClassUID, cuid);
            item.putUI(Tags.RefSOPInstanceUID, uidgen.createUID());
        }
    }


    void addRefAnnotationBox(Dataset data)
        throws DcmServiceException
    {
        String annotationID = data.getString(Tags.AnnotationDisplayFormatID);
        if (annotationID == null) {
            return;
        }
        int n = scp.countAnnotationBoxes(aet, annotationID);
        DcmElement sq = data.putSQ(Tags.RefBasicAnnotationBoxSeq);
        for (int i = 0; i < n; ++i) {
            Dataset item = sq.addNewItem();
            item.putUI(Tags.RefSOPClassUID, UIDs.BasicAnnotationBox);
            item.putUI(Tags.RefSOPInstanceUID, uidgen.createUID());
        }
    }


    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     */
    public String toString()
    {
        return "FilmBox[uid=" + uid + ", "
                 + imageBoxes.size() + " ImageBoxes, "
                 + annotationBoxes.size() + " AnnotationBoxes]";
    }


    /**
     *  Gets the attributes attribute of the FilmBox object
     *
     * @return  The attributes value
     */
    public Dataset getAttributes()
    {
        return dataset;
    }


    /**
     *  Description of the Method
     *
     * @param  modification Description of the Parameter
     * @param  pluts Description of the Parameter
     * @param  sessionAttr Description of the Parameter
     * @exception  DcmServiceException Description of the Exception
     */
    public void updateAttributes(Dataset modification, HashMap pluts,
            Dataset sessionAttr, Command rspCmd)
        throws DcmServiceException
    {
        checkSetAttributes(modification, rspCmd);
        addPLUT(modification, pluts);
        dataset.putAll(modification);
    }


    /**
     *  Sets the imageBox attribute of the FilmBox object
     *
     * @param  imageBoxUID The new imageBox value
     * @param  imageBox The new imageBox value
     * @param  pluts The new imageBox value
     * @exception  DcmServiceException Description of the Exception
     */
    public void setImageBox(String imageBoxUID, Dataset imageBox,
            HashMap pluts, Command rsp)
        throws DcmServiceException
    {
        try {
            checkImageBoxAttributes(imageBox, rsp);
            scp.checkAttribute(imageBox, Tags.RequestedDecimateCropBehavior,
                DECIMATE_CROP, rsp);
            addPLUT(imageBox, pluts);
            imageBoxes.remove(imageBoxUID);
            imageBoxes.put(imageBoxUID, imageBox);
        } catch (DcmServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Processing Failure:", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }


    /**
     *  Sets the annotationBox attribute of the FilmBox object
     *
     * @param  annotationBoxUID The new annotationBox value
     * @param  annotationBox The new annotationBox value
     * @exception  DcmServiceException Description of the Exception
     */
    public void setAnnotationBox(String annotationBoxUID, Dataset annotationBox)
        throws DcmServiceException
    {
        annotationBoxes.remove(annotationBoxUID);
        annotationBoxes.put(annotationBoxUID, annotationBox);
    }


    /**
     *  Description of the Method
     *
     * @param  session Description of the Parameter
     * @return  Description of the Return Value
     */
    public Dataset createStoredPrint(FilmSession session)
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
        if (!pluts.isEmpty()) {
            pmcs.addNewItem().putUI(Tags.RefSOPClassUID, UIDs.PresentationLUT);
        }
        result.putSQ(Tags.PrinterCharacteristicsSeq);

        Dataset fbContent = result.putSQ(Tags.FilmBoxContentSeq).addNewItem();
        fbContent.putAll(dataset);
        fbContent.remove(Tags.RefFilmSessionSeq);
        copyImageBoxSeq(fbContent.remove(Tags.RefImageBoxSeq), result);
		copyAnnotationBoxSeq(fbContent.remove(Tags.RefBasicAnnotationBoxSeq), result);

        if (!pluts.isEmpty()) {
            DcmElement plutSeq = result.putSQ(Tags.PresentationLUTContentSeq);
            for (Iterator it = pluts.values().iterator(); it.hasNext(); ) {
                plutSeq.addItem((Dataset) it.next());
            }
        }
        return result;
    }


    private void copyImageBoxSeq(DcmElement refImageBoxSeq, Dataset result)
    {
        Dataset[] boxes = new Dataset[refImageBoxSeq.countItems()];
        for (Iterator it = imageBoxes.values().iterator(); it.hasNext(); ) {
            Dataset box = (Dataset) it.next();
            int ipos = box.getInt(Tags.ImagePositionOnFilm, -1);
            boxes[ipos - 1] = box;
        }

        DcmElement boxSeq = result.putSQ(Tags.ImageBoxContentSeq);
        for (int i = 0; i < boxes.length; ++i) {
            if (boxes[i] != null && boxes[i].contains(Tags.RefImageSeq)) {
                boxSeq.addItem(boxes[i]);
            }
        }
    }


    private void copyAnnotationBoxSeq(DcmElement refAnnotationBoxSeq,
            Dataset result)
    {
        if (refAnnotationBoxSeq == null) {
            return;
        }
        Dataset[] boxes = new Dataset[refAnnotationBoxSeq.countItems()];
        for (Iterator it = annotationBoxes.values().iterator(); it.hasNext(); ) {
            Dataset box = (Dataset) it.next();
            int apos = box.getInt(Tags.AnnotationPosition, -1);
            boxes[apos - 1] = box;
        }

        DcmElement contSeq = result.putSQ(Tags.AnnotationContentSeq);
        for (int i = 0; i < boxes.length; ++i) {
            if (boxes[i] != null && boxes[i].vm(Tags.TextString) > 0) {
                contSeq.addItem(boxes[i]);
            }
        }
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
            log.error("Missing SOP Class UID in Ref Presentation LUT Seq Item");
            throw new DcmServiceException(Status.MissingAttributeValue);
        }
        if (!UIDs.PresentationLUT.equals(cuid)) {
            log.error("Unrecognized SOP Class UID in Ref Presentation LUT Seq Item - " + cuid);
            throw new DcmServiceException(Status.InvalidAttributeValue);
        }
        String iuid = ref.getString(Tags.RefSOPInstanceUID);
        if (iuid == null) {
            log.error("Missing SOP Instance UID in Ref Presentation LUT Seq Item");
            throw new DcmServiceException(Status.MissingAttributeValue);
        }
        Dataset plut = (Dataset) pluts.get(iuid);
        if (plut == null) {
            log.error("No Presentation LUT with referenced uid - " + iuid);
            throw new DcmServiceException(Status.InvalidAttributeValue,
                    "No such Presentation LUT");
        }
        this.pluts.put(iuid, plut);
    }

    private String checkDecimateCropBehavior(String val)
        throws DcmServiceException
    {
        if (val == null) {
            return null;
        }
        if (!val.equals("DECIMATE")
                 && !val.equals("CROP")
                 && !val.equals("FAIL")) {
            throw new DcmServiceException(Status.InvalidAttributeValue);
        }
        return val;
    }
}

