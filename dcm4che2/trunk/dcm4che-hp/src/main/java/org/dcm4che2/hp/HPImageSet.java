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

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 30, 2005
 *
 */
public class HPImageSet {

    public static final String ABSTRACT_PRIOR = "ABSTRACT_PRIOR";
    public static final String RELATIVE_TIME = "RELATIVE_TIME";
    
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
    
    public int getImageSetNumber() {
        return dcmobj.getInt(Tag.ImageSetNumber);
    }
     
    public String getImageSetLabel() {
        return dcmobj.getString(Tag.ImageSetLabel);
    }
    
    public String getImageSetSelectorCategory() {
        return dcmobj.getString(Tag.ImageSetSelectorCategory);
    }
    
    public boolean isRelativeTime() {
        return RELATIVE_TIME.equals(getImageSetSelectorCategory());
    }
    
    public boolean isAbstractPrior() {
        return ABSTRACT_PRIOR.equals(getImageSetSelectorCategory());
    }
    
    public int[] getRelativeTime() {
        return dcmobj.getInts(Tag.RelativeTime);
    }
 
    public String getRelativeTimeUnits() {
        return dcmobj.getString(Tag.RelativeTimeUnits);
    }

    public boolean hasAbstractPriorValue() {
        return dcmobj.containsValue(Tag.AbstractPriorValue);
    }
 
    public int[] getAbstractPriorValue() {
        return dcmobj.getInts(Tag.AbstractPriorValue);
    }
 
    public boolean hasAbstractPriorCode() {
        return dcmobj.containsValue(Tag.AbstractPriorCodeSequence);
    }
 
    public Code getAbstractPriorCode() {
        return new Code(dcmobj.getNestedDicomObject(Tag.AbstractPriorCodeSequence));
    }

    public DicomElement getImageSetSelectorSequence() {
        return dcmobj.getParent().get(Tag.ImageSetSelectorSequence);
    }
}
