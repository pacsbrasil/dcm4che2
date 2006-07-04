package org.dcm4che2.iod.module.spatial;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.SOPInstanceReference;

/**
 * This class represents a set of Fiducials.
 * <p>
 * Since there is no Fiducial Set without Fiducials, this class extends
 * {@link org.dcm4che2.iod.module.spatial.Fiducial}.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class FiducialSet extends Fiducial {

    public FiducialSet(DicomObject dcmobj) {
        super(dcmobj);
        // TODO Auto-generated constructor stub
    }
    
    public static FiducialSet[] toFiducialSets(DicomElement sq) {
        if (sq == null || !sq.hasItems()) {
            return null;
        }
        FiducialSet[] a = new FiducialSet[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new FiducialSet(sq.getDicomObject(i));
        }
        return a;

    }

    /**
     * Identifies a Frame of Reference that may or may not be an image set.
     * 
     * (e.g. an atlas or physical space).
     * 
     * See C.7.4.1.1.1 for further explanation. Required if Referenced Image
     * Sequence (0008,1140) is absMay be present otherwise.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public String getFrameofRerenceUID() {
        return dcmobj.getString(Tag.FrameofReferenceUID);
    }

    /**
     * Identifies a Frame of Reference that may or may not be an image set.
     * 
     * (e.g. an atlas or physical space).
     * 
     * See C.7.4.1.1.1 for further explanation. Required if Referenced Image
     * Sequence (0008,1140) is absMay be present otherwise.
     * <p>
     * Type 1C
     * 
     * @param ui
     */
    public void setFrameofReferenceUID(String ui) {
        dcmobj.putString(Tag.FrameofReferenceUID, VR.UI, ui);
    }

    /**
     * Identifies the set of images in which the fiducials are located.
     * 
     * Required if Frame of Reference UID (0020,0052) is absent. May be present
     * otherwise. One or more Items shall be present. All referenced images
     * shall have the same Frame of Reference UID if present in the images.
     * <p>
     * Type 1C
     * 
     * @return
     */
    public SOPInstanceReference[] getReferencedImages() {
        return SOPInstanceReference.toSOPInstanceReferences(dcmobj
                .get(Tag.ReferencedImageSequence));
    }

    /**
     * Identifies the set of images in which the fiducials are located.
     * 
     * Required if Frame of Reference UID (0020,0052) is absent. May be present
     * otherwise. One or more Items shall be present. All referenced images
     * shall have the same Frame of Reference UID if present in the images.
     * <p>
     * Type 1C
     * 
     * @param sops
     */
    public void setReferencedImages(SOPInstanceReference[] sops) {
        updateSequence(Tag.ReferencedImageSequence, sops);
    }

    /**
     * A sequence that specifies one or more fiducials, one item per fiducial.
     * <p>
     * Type 1
     * 
     * @return
     */
    public Fiducial[] getFiducials() {
        return Fiducial.toFiducial(dcmobj.get(Tag.FiducialSequence));
    }

    /**
     * A sequence that specifies one or more fiducials, one item per fiducial.
     * <p>
     * Type 1
     * 
     * @param fids
     */
    public void setFiducials(Fiducial[] fids) {
        updateSequence(Tag.FiducialSequence, fids);
    }
}
