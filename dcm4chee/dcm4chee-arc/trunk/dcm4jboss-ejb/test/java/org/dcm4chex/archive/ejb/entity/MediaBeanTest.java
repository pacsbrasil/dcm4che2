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
package org.dcm4chex.archive.ejb.entity;

import org.apache.cactus.ServletTestCase;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.MediaLocalHome;
import org.dcm4chex.archive.ejb.util.EJBLocalHomeFactory;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class MediaBeanTest extends ServletTestCase
{

    public static final String UID_ = "1.2.40.0.13.1.1.9999.2.";
    private MediaLocalHome mediaHome;
    private Object[] mediaPks;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MediaBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        EJBLocalHomeFactory factory = EJBLocalHomeFactory.getInstance();
        mediaHome = (MediaLocalHome) factory.lookup(MediaLocalHome.class);
        mediaPks = new Object[5];
        for (int i = 0; i < 5; ++i)
        {
            mediaPks[i] = mediaHome.create(UID_ + i).getPrimaryKey();
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        for (int i = 0; i < 5; ++i)
        {
            mediaHome.remove(mediaPks[i]);
        }
    }

    /**
     * Constructor for StudyBeanTest.
     * @param arg0
     */
    public MediaBeanTest(String arg0)
    {
        super(arg0);
    }

    public void testFindByFilesetIuid() throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            MediaLocal media = mediaHome.findByFilesetIuid(UID_ + i);
        }
    }
}
