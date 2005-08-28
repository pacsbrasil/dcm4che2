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
 * @since Aug 8, 2005
 *
 */
public class HPDefinition {
    private final DicomObject dcmobj;
    
    public HPDefinition(DicomObject item) {
        this.dcmobj = item;
    }
    
    public DicomObject getDicomObject() {
        return dcmobj;        
    }
    
    public String getModality() {
        return dcmobj.getString(Tag.Modality);
    }
    
    public String getLaterality() {
        return dcmobj.getString(Tag.Laterality);
    }
    
    public Code[] getAnatomicRegionCodes() {
        DicomElement sq = dcmobj.get(Tag.AnatomicRegionSequence);
        return sq != null && sq.hasItems() ? Code.toArray(sq) : null;
    }

    public Code[] getProcedureCodes() {
        DicomElement sq = dcmobj.get(Tag.ProcedureCodeSequence);
        return sq != null && sq.hasItems() ? Code.toArray(sq) : null;
    }

    public Code[] getReasonforRequestedProcedureCodes() {
        DicomElement sq = 
                dcmobj.get(Tag.ReasonforRequestedProcedureCodeSequence);
        return sq != null && sq.hasItems() ? Code.toArray(sq) : null;
    }

}
