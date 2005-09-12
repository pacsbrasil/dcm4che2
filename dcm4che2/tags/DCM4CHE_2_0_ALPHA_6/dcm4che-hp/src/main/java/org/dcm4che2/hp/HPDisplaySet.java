/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 *
 */
public class HPDisplaySet {

    public static final String YES = "YES";
    public static final String NO = "NO";

    public static final String COLOR = "COLOR"; 
    
    public static final String MPR = "MPR";    
    public static final String _3D_RENDERING = "3D_RENDERING";    
    public static final String SLAB = "SLAB";
    
    public static final String SAGITTAL = "SAGITTAL";
    public static final String AXIAL = "AXIAL";
    public static final String CORONAL = "CORONAL";
    public static final String OBLIQUE = "OBLIQUE";
    
    public static final String LUNG = "LUNG";
    public static final String MEDIASTINUM = "MEDIASTINUM";
    public static final String ABDO_PELVIS = "ABDO_PELVIS";
    public static final String LIVER = "LIVER";
    public static final String SOFT_TISSUE = "SOFT_TISSUE";
    public static final String BONE = "BONE";
    public static final String BRAIN = "BRAIN";
    public static final String POST_FOSSA = "POST_FOSSA";

    public static final String BLACK_BODY = "BLACK_BODY";
    public static final String HOT_IRON = "HOT_IRON";
    public static final String DEFAULT = "DEFAULT";
    
    
    private final DicomObject dcmobj;
    private final HPImageSet imageSet;
    private final List imageBoxes;
    private final List filters;
    private final List cmps;

    HPDisplaySet(DicomObject dcmobj, HPImageSet imageSet) {
        this.imageSet = imageSet;
        this.dcmobj = dcmobj;
        DicomElement imageBoxesSeq = dcmobj.get(Tag.ImageBoxesSequence);
        if (imageBoxesSeq == null || imageBoxesSeq.isEmpty())
            throw new IllegalArgumentException(
                "Missing (0072,0300) Image Boxes Sequence");
        int numImageBoxes = imageBoxesSeq.countItems();
        this.imageBoxes = new ArrayList(numImageBoxes);
        for (int i = 0; i < numImageBoxes; i++) {
            imageBoxes.add(new HPImageBox(imageBoxesSeq.getDicomObject(i), numImageBoxes));
        }
        DicomElement filterOpSeq = dcmobj.get(Tag.FilterOperationsSequence);
        if (filterOpSeq == null || filterOpSeq.isEmpty()) {
            this.filters = Collections.EMPTY_LIST;
        } else {
            int n = filterOpSeq.countItems();
            this.filters = new ArrayList(n);
            for (int i = 0; i < n; i++) {
                filters.add(AbstractHPSelector.createDisplaySetFilter(
                        filterOpSeq.getDicomObject(i)));
            }
        }
        DicomElement sortingOpSeq = dcmobj.get(Tag.SortingOperationsSequence);
        if (sortingOpSeq == null || sortingOpSeq.isEmpty()) {
            this.cmps = Collections.EMPTY_LIST;
        } else {
            int n = sortingOpSeq.countItems();
            this.cmps = new ArrayList();
            for (int i = 0; i < n; i++) {
                cmps.add(AbstractHPComparator.valueOf(sortingOpSeq.getDicomObject(i)));
            }
        }
    }
    
    public final DicomObject getDicomObject() {
        return dcmobj;
    }

    public final HPImageSet getImageSet() {
        return imageSet;
    }

    public List getImageBoxes() {
        return Collections.unmodifiableList(imageBoxes);
    }
    
    public boolean contains(DicomObject o, int frame) {
        for (int i = 0, n = filters.size(); i < n; i++) {
            HPSelector selector = (HPSelector) filters.get(i);
            if (!selector.matches(o, frame))
                return false;
        }
        return true;
    }
 
    public int compare(DicomObject o1, int frame1, DicomObject o2, int frame2) {
        int result = 0;
        for (int i = 0, n = cmps.size(); result == 0 && i < n; i++) {
            HPComparator cmp = (HPComparator) cmps.get(i);
            result = cmp.compare(o1, frame1, o2, frame2);
        }
        return result;
    }

    public int getDisplaySetNumber() {
        return dcmobj.getInt(Tag.DisplaySetNumber);
    }
    
    public int getDisplaySetPresentationGroup() {
        return dcmobj.getInt(Tag.DisplaySetPresentationGroup);
    }

    public String getBlendingOperationType() {
        return dcmobj.getString(Tag.BlendingOperationType);
    }

    public boolean isBlendingOperationType(String type) {
        return type.equals(getBlendingOperationType());
    }

    public String getReformattingOperationType() {
        return dcmobj.getString(Tag.ReformattingOperationType);
    }

    public boolean isReformattingOperationType(String type) {
        return type.equals(getReformattingOperationType());
    }

    public double getReformattingThickness() {
        return dcmobj.getDouble(Tag.ReformattingThickness);
    }    

    public double getReformattingInterval() {
        return dcmobj.getDouble(Tag.ReformattingInterval);
    }    

    public String getReformattingOperationInitialViewDirection() {
        return dcmobj.getString(Tag.ReformattingOperationInitialViewDirection);
    }

    public boolean isReformattingOperationInitialViewDirection(String direction) {
        return direction.equals(getReformattingOperationInitialViewDirection());
    }

    public String get3DRenderingType() {
        return dcmobj.getString(Tag._3DRenderingType);
    }

    public boolean is3DRenderingType(String type) {
        return type.equals(get3DRenderingType());
    }

    public String[] getDisplaySetPatientOrientation() {
        return dcmobj.getStrings(Tag.DisplaySetPatientOrientation);
    }

    public String getVOIType() {
        return dcmobj.getString(Tag.VOIType);
    }

    public boolean isVOIType(String type) {
        return type.equals(getVOIType());
    }

    public String getPseudoColorType() {
        return dcmobj.getString(Tag.PseudocolorType);
    }

    public boolean isPseudoColorType(String type) {
        return type.equals(getPseudoColorType());
    }

    public String getShowGrayscaleInverted() {
        return dcmobj.getString(Tag.ShowGrayscaleInverted);
    }

    public boolean isShowGrayscaleInverted(String flag) {
        return flag.equals(getShowGrayscaleInverted());
    }

    public String getShowImageTrueSizeFlag() {
        return dcmobj.getString(Tag.ShowImageTrueSizeFlag);
    }

    public boolean isShowImageTrueSize(String flag) {
        return flag.equals(getShowImageTrueSizeFlag());
    }

    public String getShowGraphicAnnotationFlag() {
        return dcmobj.getString(Tag.ShowGraphicAnnotationFlag);
    }

    public boolean isShowGraphicAnnotation(String flag) {
        return flag.equals(getShowGraphicAnnotationFlag());
    }

    public String getShowAcquisitionTechniquesFlag() {
        return dcmobj.getString(Tag.ShowAcquisitionTechniquesFlag);
    }

    public boolean isSShowAcquisitionTechniques(String flag) {
        return flag.equals(getShowAcquisitionTechniquesFlag());
    }

    public String getDisplaySetPresentationGroupDescription() {
        return dcmobj.getString(Tag.DisplaySetPresentationGroupDescription);
    }
}
