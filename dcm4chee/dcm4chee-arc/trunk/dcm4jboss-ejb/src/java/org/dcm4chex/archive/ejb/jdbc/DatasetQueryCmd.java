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
/* 
 * File: $Source$
 * Author: gunter
 * Date: 20.07.2003
 * Time: 16:21:45
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.ResultSet;

import org.dcm4che.data.Dataset;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class DatasetQueryCmd extends BaseQueryCmd
{
    private Dataset keys;
    private int level;
    
    /**
     * @param keys
     */
    public void execute(Dataset keys)
    {
        this.keys = keys;
        execute(toSQL(keys));        
    }

    /**
     * @return
     */
    public Dataset getDataset()
    {
        return toDataset(rs);
    }

    private Dataset toSQL(Dataset keys)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param rs
     * @return
     */
    private Dataset toDataset(ResultSet rs)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
