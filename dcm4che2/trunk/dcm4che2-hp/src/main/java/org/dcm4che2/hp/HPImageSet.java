/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;

import java.util.List;

import org.dcm4che2.data.DicomObject;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Jul 30, 2005
 *
 */
public class HPImageSet {

    private final DicomObject dcmobj;
    private final List selectors;

    HPImageSet(List selectors, DicomObject dcmobj) {
        this.selectors = selectors; 
        this.dcmobj = dcmobj; 
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;
    }
    
    public boolean contains(DicomObject o, int frame) {
        for (int i = 0, n = selectors.size(); i < n; i++) {
            HPSelector selector = (HPSelector) selectors.get(i);
            if (!selector.matches(o, frame))
                return false;
        }
        return true;
    }
 
}
