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
 * Time: 18:50:40
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.entity;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.NodeLocal;
import org.dcm4chex.archive.ejb.interfaces.NodeLocalHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class FileBeanTest extends ServletTestCase
{
    public static final String STORAGE_AET = "STORE_AET";
    public static final String RETRIEVE_AET = "RETRIEVE_AET";
    public static final String URI = "file://hostname/var/local/archive/";
    public static final String PATH = "2003/07/11/12345678/9ABCDEF0";
    public static final String TSUID = "1.2.40.0.13.1.1.9999.3";
    public static final long SIZE = 567890L;
    public static final byte[] MD5 =
        { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

    private NodeLocalHome nodeHome;
    private FileLocalHome fileHome;
    private NodeLocal node;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(FileBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        Context ctx = new InitialContext();
        nodeHome = (NodeLocalHome) ctx.lookup("java:comp/env/ejb/Node");
        fileHome = (FileLocalHome) ctx.lookup("java:comp/env/ejb/File");
        ctx.close();
        node = nodeHome.create(URI, STORAGE_AET, RETRIEVE_AET);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        node.remove();
    }

    /**
     * Constructor for StudyBeanTest.
     * @param arg0
     */
    public FileBeanTest(String arg0)
    {
        super(arg0);
    }

    public void testCreate() throws Exception
    {
        FileLocal file = fileHome.create(node, PATH, TSUID, SIZE, MD5, null);
        file.remove();
    }
}
