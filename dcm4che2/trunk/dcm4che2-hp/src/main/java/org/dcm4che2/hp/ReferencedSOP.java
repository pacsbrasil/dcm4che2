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
 * @since Aug 15, 2005
 *
 */
public class ReferencedSOP {
    private final DicomObject dcmobj;

    public ReferencedSOP(DicomObject dcmobj) {
         this.dcmobj = dcmobj;
    }
    
    public String getReferencedSOPInstanceUID() {    
        return dcmobj.getString(Tag.ReferencedSOPInstanceUID);
    }
    
    public String getReferencedSOPClassUID() {    
        return dcmobj.getString(Tag.ReferencedSOPClassUID);
    }

}
