/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.iod.module.spatial;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;

/**
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class GraphicCoordinatesData extends Module {

    public GraphicCoordinatesData(DicomObject dcmobj) {
        super(dcmobj);
        // TODO Auto-generated constructor stub
    }

    public static GraphicCoordinatesData[] toGraphicCoordinatesData(DicomElement sq) {
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        GraphicCoordinatesData[] a = new GraphicCoordinatesData[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new GraphicCoordinatesData(sq.getDicomObject(i));
        }
        return a;

    }

    /**
     * Graphic point coordinates of the fiducial.
     * 
     * Graphic point coordinates of the fiducial points in the image of the
     * Referenced Image Sequence. If Fiducial’s Contour Data (3006,0050) is
     * present, these points correlate to the points in the Contour Data, one
     * row-column pair for each point and in the same order. See C.10.5.1.2 for
     * further explanation.
     * <p>
     * Tpe 1
     * 
     * @return
     */
    public float getGraphicData() {
        return dcmobj.getFloat(Tag.GraphicData);
    }

    /**
     * Graphic point coordinates of the fiducial.
     * 
     * Graphic point coordinates of the fiducial points in the image of the
     * Referenced Image Sequence. If Fiducial’s Contour Data (3006,0050) is
     * present, these points correlate to the points in the Contour Data, one
     * row-column pair for each point and in the same order. See C.10.5.1.2 for
     * further explanation.
     * <p>
     * Type 1
     * 
     * @param ds
     */
    public void setGraphicData(float ds) {
        dcmobj.putFloat(Tag.GraphicData, VR.DS, ds);
    }

    /**
     * Image containing the fiducial’s graphic coordinates.
     * 
     * A sequence that specifies the image containing the fiducial’s graphic
     * coordinates. Only one item shall be present. Shall be an image within the
     * set of the images in the Referenced Image Sequence (0008,1140) of the
     * encapsulating Fiducial Set Sequence (0070,031C) item.
     * 
     * <p>
     * Type 1
     * 
     * @param sop
     */
    public void setReferencedImage(SOPInstanceReference sop) {
        updateSequence(Tag.ReferencedImageSequence, sop);
    }

    /**
     * Image containing the fiducial’s graphic coordinates.
     * 
     * A sequence that specifies the image containing the fiducial’s graphic
     * coordinates. Only one item shall be present. Shall be an image within the
     * set of the images in the Referenced Image Sequence (0008,1140) of the
     * encapsulating Fiducial Set Sequence (0070,031C) item.
     * 
     * <p>
     * Type 1
     * 
     * @return
     */
    public SOPInstanceReference getReferencedImage() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.ReferencedImageSequence);
        return item != null ? new SOPInstanceReference(item) : null;
    }

}
