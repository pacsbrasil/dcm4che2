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
 * Date: 17.07.2003
 * Time: 17:49:18
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class DatasetUtil
{
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private static final HashMap filterMap = new HashMap();

    public static Dataset fromByteArray(byte[] data)
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Dataset ds = dof.newDataset();
        try
        {
            ds.readDataset(bin, DcmDecodeParam.EVR_LE, -1);
        }
        catch (IOException e)
        {
            new EJBException(e);
        }
        return ds;
    }

    public static byte[] toByteArray(Dataset ds)
    {
        ByteArrayOutputStream bos =
            new ByteArrayOutputStream(ds.calcLength(DcmDecodeParam.IVR_LE));
        try
        {
            ds.writeDataset(bos, DcmDecodeParam.EVR_LE);
        }
        catch (IOException e)
        {
            new EJBException(e);
        }
        return bos.toByteArray();
    }

    public static CodeLocal toCode(Dataset item, CodeLocalHome codeHome)
    {
        if (item == null)
            return null;

        final String value = item.getString(Tags.CodeValue);
        final String designator = item.getString(Tags.CodingSchemeDesignator);
        final String version = item.getString(Tags.CodingSchemeVersion);
        final String meaning = item.getString(Tags.CodeMeaning);
        try
        {
            Collection c = codeHome.findByValueAndDesignator(value, designator);
            for (Iterator it = c.iterator(); it.hasNext();)
            {
                final CodeLocal code = (CodeLocal) it.next();
                if (version == null)
                {
                    return code;
                }
                final String version2 = code.getCodingSchemeVersion();
                if (version2 == null || version2.equals(version))
                {
                    return code;
                }
            }
            return codeHome.create(value, designator, version, meaning);
        }
        catch (FinderException e)
        {
            throw new EJBException(e);
        }
        catch (CreateException e)
        {
            throw new EJBException(e);
        }
    }

    public static Dataset getFilter(String attrs_cfg)
    {
        Dataset ds = (Dataset) filterMap.get(attrs_cfg);
        if (ds == null)
        {
            ds = dof.newDataset();
            StringTokenizer st = new StringTokenizer(attrs_cfg, "+");            
            while (st.hasMoreTokens())
            {
                loadFilter(st.nextToken(), ds);
            }
            filterMap.put(attrs_cfg, ds);
        }
        return ds;
    }

    private static void loadFilter(String attrs_cfg, Dataset ds)
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(attrs_cfg);
        if (is == null)
        {
            throw new EJBException("Missing " + attrs_cfg);
        }
        Reader r = new BufferedReader(new InputStreamReader(is));
        StreamTokenizer st = new StreamTokenizer(r);
        try
        {
            while (st.nextToken() != StreamTokenizer.TT_EOF)
            {
                if (st.ttype == StreamTokenizer.TT_WORD)
                {
                    ds.putXX(Tags.forName(st.sval));
                }
            }
        }
        catch (Exception e)
        {
            throw new EJBException("Failed to parse " + attrs_cfg, e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException ignore)
            {
            }
        }
    }

    private DatasetUtil()
    {
    }
}