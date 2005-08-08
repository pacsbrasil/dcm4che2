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
 * @version $Reversion$ $Date$
 * @since Aug 8, 2005
 *
 */
public class HPImageBox {
    private final DicomObject dcmobj;
    
    public HPImageBox(DicomObject item, int tot) {
        if (item.getInt(Tag.ImageBoxNumber) != item.getItemPosition())
            throw new IllegalArgumentException(
                    "" + item.get(Tag.ImageBoxNumber));
        if (tot > 1) {
            if (!"TILED".equals(item.getString(Tag.ImageBoxLayoutType)))
                throw new IllegalArgumentException(
                        "" + item.get(Tag.ImageBoxLayoutType));
        }
        this.dcmobj = item;
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;        
    }

}
