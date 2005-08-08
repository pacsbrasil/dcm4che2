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
 * @version $Reversion$ $Date$
 * @since Jul 30, 2005
 *
 */
public class HPDisplaySet {

    private final DicomObject dcmobj;
    private final HPImageSet imageSet;
    private final List imageBoxes;
    private final List filters;
    private final List cmps;

    HPDisplaySet(DicomObject dcmobj, HPImageSet imageSet) {
        this.imageSet = imageSet;
        this.dcmobj = dcmobj;
        DicomElement imageBoxesSeq = dcmobj.get(Tag.ImageBoxesSequence);
        if (imageBoxesSeq == null || imageBoxesSeq.isNull())
            throw new IllegalArgumentException(
                "Missing (0072,0300) Image Boxes Sequence");
        int numImageBoxes = imageBoxesSeq.countItems();
        this.imageBoxes = new ArrayList(numImageBoxes);
        for (int i = 0; i < numImageBoxes; i++) {
            imageBoxes.add(new HPImageBox(imageBoxesSeq.getItem(i), numImageBoxes));
        }
        DicomElement filterOpSeq = dcmobj.get(Tag.FilterOperationsSequence);
        if (filterOpSeq == null || filterOpSeq.isNull()) {
            this.filters = Collections.EMPTY_LIST;
        } else {
            int n = filterOpSeq.countItems();
            this.filters = new ArrayList(n);
            for (int i = 0; i < n; i++) {
                filters.add(AbstractHPSelector.createDisplaySetFilter(
                        filterOpSeq.getItem(i)));
            }
        }
        DicomElement sortingOpSeq = dcmobj.get(Tag.SortingOperationsSequence);
        if (sortingOpSeq == null || sortingOpSeq.isNull()) {
            this.cmps = Collections.EMPTY_LIST;
        } else {
            int n = sortingOpSeq.countItems();
            this.cmps = new ArrayList();
            for (int i = 0; i < n; i++) {
                cmps.add(AbstractHPComparator.valueOf(sortingOpSeq.getItem(i)));
            }
        }
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public int countImageBoxes() {
        return imageBoxes.size();
    }
    
    public HPImageBox getImageBox(int imageBoxNumber) {
        return (HPImageBox) imageBoxes.get(imageBoxNumber-1);
    }    
    
    public boolean contains(DicomObject o, int frame, float minCosine) {
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

    public int getDisplaySetPresentationGroup() {
        return dcmobj.getInt(Tag.DisplaySetPresentationGroup);
    }

}
