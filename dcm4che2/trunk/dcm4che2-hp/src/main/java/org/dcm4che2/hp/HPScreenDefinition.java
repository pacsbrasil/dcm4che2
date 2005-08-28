/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 8, 2005
 *
 */
public class HPScreenDefinition {
    private final DicomObject dcmobj;
    
    public HPScreenDefinition(DicomObject item) {
        this.dcmobj = item;
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;        
    }
    
    public int getNumberOfVerticalPixels() {
        return dcmobj.getInt(Tag.NumberofVerticalPixels);
    }

    public int getNumberOfHorizontalPixels() {
        return dcmobj.getInt(Tag.NumberofHorizontalPixels);
    }

    public double[] getDisplayEnvironmentSpatialPosition() {
        return dcmobj.getDoubles(Tag.DisplayEnvironmentSpatialPosition);
    }

    public int getScreenMinimumColorBitDepth() {
        return dcmobj.getInt(Tag.ScreenMinimumColorBitDepth);
    }

    public int getScreenMinimumGrayscaleBitDepth() {
        return dcmobj.getInt(Tag.ScreenMinimumGrayscaleBitDepth);
    }

    public int ApplicationMaximumRepaintTime() {
        return dcmobj.getInt(Tag.ApplicationMaximumRepaintTime);
    }
}
