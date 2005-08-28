/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp.plugins;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.hp.HPComparator;
import org.dcm4che2.hp.SortingDirection;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 7, 2005
 *
 */
public class ByAcqTimeComparator implements HPComparator {

    private final int sortingDirection;
    
    public ByAcqTimeComparator(DicomObject sortOp) {
        sortingDirection = SortingDirection.toSign(
                sortOp.getString(Tag.SortingDirection));
    }

    public int compare(DicomObject o1, int frame1, DicomObject o2, int frame2) {
        Date t1 = toAcqTime(o1, frame1);
        Date t2 = toAcqTime(o2, frame2);
        if (t1 == null || t2 == null)
            return 0;
        return t1.compareTo(t2) * sortingDirection;
    }

    private Date toAcqTime(DicomObject o, int frame) {
        Date t = o.getDate(Tag.AcquisitionDate, Tag.AcquisitionTime);
        if (t == null) {
            t = o.getDate(Tag.AcquisitionDatetime);
            if (t == null) {
                t = o.getDate(Tag.ContentDate, Tag.ContentTime);
            }
        }
        return t;
    }

}
