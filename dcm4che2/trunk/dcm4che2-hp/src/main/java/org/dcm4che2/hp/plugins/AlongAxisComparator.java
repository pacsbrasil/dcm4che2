/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp.plugins;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.hp.HPComparator;
import org.dcm4che2.hp.SortingDirection;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Aug 7, 2005
 *
 */
public class AlongAxisComparator implements HPComparator {

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private static final int RX = 0;
    private static final int RY = 1;
    private static final int RZ = 2;
    private static final int CX = 3;
    private static final int CY = 4;
    private static final int CZ = 5;

    private final int sortingDirection;

    public AlongAxisComparator(DicomObject sortOp) {
        sortingDirection = SortingDirection.toSign(
                sortOp.getString(Tag.SortingDirection));
    }

    public int compare(DicomObject o1, int frame1, DicomObject o2, int frame2) {
        try {
            float v1 = dot(o1, frame1);
            float v2 = dot(o2, frame2);
            if (v1 < v2)
                return sortingDirection;
            if (v1 > v2)
                return -sortingDirection;
        } catch (RuntimeException ignore) {
        }
        return 0;
    }

    private float dot(DicomObject o, int frame) {
        float[] ipp = o.getFloats(Tag.ImagePositionPatient);
        float[] iop = o.getFloats(Tag.ImageOrientationPatient);
        float nx = iop[RY] * iop[CZ] - iop[RZ] * iop[CY];
        float ny = iop[RZ] * iop[CX] - iop[RX] * iop[CZ];
        float nz = iop[RX] * iop[CY] - iop[RY] * iop[CX];
        return ipp[X] * nx + ipp[Y] * ny + ipp[Z] * nz;
    }

}
