/* Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4cheri.data;

import java.util.ArrayList;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.dict.VRs;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision$ $Date$
 */
class SQElement extends DcmElementImpl
{

    private final ArrayList list = new ArrayList();
    private final Dataset parent;
    private int totlen = -1;


    /**
     * Creates a new instance of ElementImpl
     *
     * @param  tag     Description of the Parameter
     * @param  parent  Description of the Parameter
     */
    public SQElement(int tag, Dataset parent)
    {
        super(tag);
        this.parent = parent;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public final int vr()
    {
        return VRs.SQ;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public final int vm()
    {
        return list.size();
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public final boolean hasItems()
    {
        return true;
    }


    /**
     *  Gets the item attribute of the SQElement object
     *
     * @param  index  Description of the Parameter
     * @return        The item value
     */
    public Dataset getItem(int index)
    {
        if (index >= vm()) {
            return null;
        }
        return (Dataset) list.get(index);
    }


    /**
     *  Adds a feature to the Item attribute of the SQElement object
     *
     * @param  item  The feature to be added to the Item attribute
     */
    public void addItem(Dataset item)
    {
        list.add(item);
    }


    /**
     *  Adds a feature to the NewItem attribute of the SQElement object
     *
     * @return    Description of the Return Value
     */
    public Dataset addNewItem()
    {
        Dataset item = new DatasetImpl(parent);
        list.add(item);
        return item;
    }


    /**
     *  Description of the Method
     *
     * @param  param  Description of the Parameter
     * @return        Description of the Return Value
     */
    public int calcLength(DcmEncodeParam param)
    {
        totlen = param.undefSeqLen ? 8 : 0;
        for (int i = 0, n = vm(); i < n; ++i) {
            totlen += getItem(i).calcLength(param) +
                    (param.undefItemLen ? 16 : 8);
        }
        return totlen;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public int length()
    {
        return totlen;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(DICT.toString(tag));
        sb.append(",SQ");
        if (!isEmpty()) {
            for (int i = 0, n = vm(); i < n; ++i) {
                sb.append("\n\tItem-").append(i + 1).append(getItem(i));
            }
        }
        return sb.toString();
    }
}

