/* $Id$
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
package org.dcm4chex.archive.ejb.conf;

import java.util.ArrayList;
import java.util.Properties;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 28.12.2003
 */
public class AttributeCoercion {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    Dataset values = dof.newDataset();
    ArrayList fromTo = new ArrayList();
    Properties props = new Properties();
    
    public void coerce(Dataset ds, Dataset coercedElements) {
        ds.putAll(values);
        coercedElements.putAll(values);
        for (int i = 0, n = fromTo.size(); i < n; ++i) {
            int[] int2 = (int[]) fromTo.get(i);
            ds.putXX(int2[1], ds.getStrings(int2[0]));
            coercedElements.putXX(int2[1]);            
        }
    }
}
