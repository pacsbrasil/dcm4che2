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
 * Date: 11.07.2003
 * Time: 14:50:40
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.session;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import javax.ejb.ObjectNotFoundException;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.HostNameUtils;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class StorageBeanTest extends TestCase
{

    public static final String DIR = "storage";
    public static final String AET = "StorageBeanTest";

    private Storage storage;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(StorageBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        Context ctx = new InitialContext();
        StorageHome home = (StorageHome) ctx.lookup(StorageHome.JNDI_NAME);
        ctx.close();
        storage = home.create();
        try {
            storage.getNodeURI(AET);
        } catch (ObjectNotFoundException e) {
            String host = HostNameUtils.getLocalHostName();
            URI tmp = new File(AET).toURI();
            String uri = "file://" + host + tmp.getPath();
            storage.createNode(uri, AET, AET);
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        storage.remove();
    }

    /**
     * Constructor for StudyBeanTest.
     * @param name
     */
    public StorageBeanTest(String name)
    {
        super(name);
    }

    public void testStore() throws Exception
    {
        doStore("", new File(DIR));
    }

    private void doStore(String path, File file) throws Exception
    {
        if (file.isDirectory())
        {
            File[] f = file.listFiles();
            for (int i = 0; i < f.length; i++)
            {
                doStore(path + '/' + f[i].getName(), f[i]);
            }
            return;
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        Dataset ds = loadDataset(file, md);
        storage.store(ds, AET, path.substring(1), file.length(), md.digest());
    }

    private Dataset loadDataset(File file, MessageDigest md) throws IOException
    {
        InputStream is = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        DigestInputStream dis = new DigestInputStream(is, md);
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        try
        {
            ds.readFile(dis, FileFormat.DICOM_FILE, -1);
        }
        finally
        {
            try
            {
                dis.close();
            }
            catch (IOException ignore)
            {
            }
        }
        ds.remove(Tags.PixelData);
        return ds;
    }
}
