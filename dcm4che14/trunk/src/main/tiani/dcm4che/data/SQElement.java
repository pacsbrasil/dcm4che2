/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

/*$Id$*/

package tiani.dcm4che.data;

import org.dcm4che.dict.VRs;
import org.dcm4che.data.*;

import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class SQElement extends DcmElementImpl {
    
    private final ArrayList list = new ArrayList();
    private final Dataset parent;
    private int totlen = -1;

    /** Creates a new instance of ElementImpl */
    public SQElement(int tag, Dataset parent) {
        super(tag);
        this.parent = parent;
    }
    
    public final int vr() {
        return VRs.SQ;
    }

    public final int vm() {
        return list.size();
    }
    
    public Dataset getDataset(int index) {
        return (Dataset)list.get(index);
    }
    
    public Dataset addNewDataset() {
        Dataset ds = new DatasetImpl(parent);
        list.add(ds);
        return ds;
    }

    public int calcLength(DcmEncodeParam param) {
        totlen = param.undefSeqLen ? 8 : 0;
        for (int i = 0, n = vm(); i < n; ++i)
            totlen += getDataset(i).calcLength(param) +
                    (param.undefItemLen ? 16 : 8);
        return totlen;
    }
    
    public int length() {
        return totlen;
    }        
}
