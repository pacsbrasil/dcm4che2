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

package org.dcm4cheri.data;

import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.data.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import org.dcm4cheri.util.StringUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class FragmentElement extends DcmElementImpl {
    
    private static final byte[] EMPTY_BYTE_ARRAY = {};
    
    private final ArrayList list = new ArrayList();

    /** Creates a new instance of ElementImpl */
    public FragmentElement(int tag) {
        super(tag);
    }
    
    public final int vm() {
        return list.size();
    }
    
    public final ByteBuffer getDataFragment(int index) {
        return (ByteBuffer)list.get(index);
    }
    
    public final ByteBuffer getDataFragment(int index, ByteOrder byteOrder) {
        ByteBuffer data = (ByteBuffer)list.get(index);
        if (data.order() != byteOrder) {
            swapOrder(data);
        }
        return data;
    }
    
    public final int getDataFragmentLength(int index) {
        ByteBuffer data = (ByteBuffer)list.get(index);
        return (data.limit()+1)&(~1);
    }
    
    int calcLength() {
        int len = 8;
        for (int i = 0, n = vm(); i < n; ++i)
            len += getDataFragmentLength(i) + 8;
        return len;
    }
    
    public void addDataFragment(ByteBuffer data) {
        list.add(data != null ? data : EMPTY_VALUE);
    }

    public void addDataFragment(byte[] data, ByteOrder byteOrder) {
        list.add(data != null ? ByteBuffer.wrap(data).order(byteOrder)
                              : EMPTY_VALUE);
    }

    protected void swapOrder(ByteBuffer data) {
        data.order(swap(data.order()));
    }
    
    private static final class OB extends FragmentElement {
        OB(int tag) {
            super(tag);
        }
        public final int vr() {
            return VRs.OB;
        }
    }

    public static DcmElement createOB(int tag) {
        return new FragmentElement.OB(tag);
    }
 
    private static final class OW extends FragmentElement {
        OW(int tag) {
            super(tag);
        }
        public final int vr() {
            return VRs.OW;
        }

        public void addDataFragment(byte[] data, ByteOrder byteOrder) {
            if (data != null && (data.length & 1) != 0)
                throw new IllegalArgumentException("odd fragment length: "
                        + data.length);
            super.addDataFragment(data, byteOrder);
        }
        
        public void addDataFragment(ByteBuffer data) {
            if ((data.limit() & 1) != 0) {
                log.warning("Ignore odd length fragment of "
                    + Tags.toString(tag) + " OW #" + data.limit());
                data = null;
            }
            super.addDataFragment(data);
        }

        protected void swapOrder(ByteBuffer data) {
            swapWords(data);
        }
    }

    public static DcmElement createOW(int tag) {
        return new FragmentElement.OW(tag);
    }
 
    private static final class UN extends FragmentElement {
        UN(int tag) {
            super(tag);
        }
        public final int vr() {
            return VRs.UN;
        }
    }

    public static DcmElement createUN(int tag) {
        return new FragmentElement.UN(tag);
    }
}