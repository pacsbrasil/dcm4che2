/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4cheri.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 05.11.2003
 */
public class DatasetUtils {
    
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    public static Dataset fromByteArray(byte[] data, DcmDecodeParam decodeParam) throws IOException
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Dataset ds = dof.newDataset();
        ds.readDataset(bin, decodeParam, -1);
        return ds;
    }

    public static byte[] toByteArray(Dataset ds, DcmEncodeParam encodeParam) throws IOException
    {
        ByteArrayOutputStream bos =
            new ByteArrayOutputStream(ds.calcLength(encodeParam));
        ds.writeDataset(bos, encodeParam);
        return bos.toByteArray();
    }

    private DatasetUtils() {} // no instance
}
