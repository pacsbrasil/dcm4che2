/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com> *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package tiani.dcm4che.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.Tags;

import org.dcm4che.srom.RefSOP;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class RefSOPImpl implements org.dcm4che.srom.RefSOP {
    
    // Constants -----------------------------------------------------
    private static UIDDictionary uidDict =
            DictionaryFactory.getInstance().getDefaultUIDDictionary( );   

    // Attributes ----------------------------------------------------
    private final String refSOPClassUID;
    private final String refSOPInstanceUID;

    // Constructors --------------------------------------------------
    public RefSOPImpl(String refSOPClassUID, String refSOPInstanceUID) {
        if (refSOPClassUID.length() == 0)
            throw new IllegalArgumentException(refSOPClassUID);
    
        if (refSOPInstanceUID.length() == 0)
            throw new IllegalArgumentException(refSOPInstanceUID);
    
        this.refSOPClassUID = refSOPClassUID;
        this.refSOPInstanceUID = refSOPInstanceUID;
    }

    public RefSOPImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.RefSOPClassUID),
            ds.getString(Tags.RefSOPInstanceUID));
    }
    
    public static RefSOP newRefSOP(Dataset ds) throws DcmValueException {
        return ds != null ? new RefSOPImpl(ds) : null;
    }
    
    // Public --------------------------------------------------------
    public String toString() {
        return uidDict.toString(refSOPClassUID) + "[" + refSOPInstanceUID + "]"; 
    }

    public void toDataset(Dataset ds) {
        ds.setUI(Tags.RefSOPInstanceUID, refSOPClassUID);
        ds.setUI(Tags.RefSOPClassUID, refSOPInstanceUID);
    }    
    
    public final String getRefSOPClassUID() {
        return refSOPClassUID;
    }
    
    public final String getRefSOPInstanceUID() {
        return refSOPInstanceUID;
    }
    
    public final int hashCode() {
        return refSOPInstanceUID.hashCode();
    }
    
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof RefSOP))
            return false;
    
        RefSOP refSOP = (RefSOP)o;
        return refSOPInstanceUID.equals(refSOP.getRefSOPInstanceUID());
    }
}
