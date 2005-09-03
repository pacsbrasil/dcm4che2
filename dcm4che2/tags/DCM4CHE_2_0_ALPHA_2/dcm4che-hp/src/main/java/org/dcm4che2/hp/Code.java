/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.hp;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 14, 2005
 *
 */
public class Code {
    private final DicomObject dcmobj;

    public Code(DicomObject dcmobj) {
        this.dcmobj = dcmobj;
    }
    
    public DicomObject getDicomObject() {
         return dcmobj;
    }
    
    public String getCodeValue() {
        return dcmobj.getString(Tag.CodeValue);
    }
    
    public String getCodingSchemeDesignator() {
        return dcmobj.getString(Tag.CodingSchemeDesignator);
    }
    
    public String getCodingSchemeVersion() {
        return dcmobj.getString(Tag.CodingSchemeVersion);
    }
    
    public String getCodeMeaning() {
        return dcmobj.getString(Tag.CodeMeaning);
    }

    public static Code[] toArray(DicomElement sq) {
        Code[] a = new Code[sq.countItems()];
        for (int i = 0; i < a.length; i++) {
            a[i] = new Code(sq.getItem(i));
        }
        return a;
    }
}
