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
 * Date: 08.07.2003
 * Time: 21:34:41
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.entity;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.util.EJBLocalHomeFactory;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class PatientBeanTest extends ServletTestCase {

    private static final String PID_ = "P-9999";
    private static final String[] PAT_NAME =
        { "Marinescu^Floyd", "Cavaness^Chuck", "Keeton^Brian", };
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientLocalHome home;
    private Object[] pk = new Object[PAT_NAME.length];

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PatientBeanTest.class);
    }

    public static Test suite()
    {
        return new TestSuite(PatientBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        EJBLocalHomeFactory factory = EJBLocalHomeFactory.getInstance();
        home = (PatientLocalHome) factory.lookup(PatientLocalHome.class);
        for (int i = 0; i < PAT_NAME.length; i++) {
            Dataset ds = dof.newDataset();
            ds.putLO(Tags.PatientID, PID_ + i);
            ds.putPN(Tags.PatientName, PAT_NAME[i]);
            PatientLocal p = home.create(ds);
            pk[i] = p.getPrimaryKey();
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        for (int i = 0; i < pk.length; i++) {
            home.remove(pk[i]);
        }
    }

    /**
     * Constructor for PatientBeanTest.
     * @param arg0
     */
    public PatientBeanTest(String arg0) {
        super(arg0);
    }

    public void testFindByPatientId() throws Exception {
        for (int i = 0; i < PAT_NAME.length; i++) {
            Collection c = home.findByPatientId(PID_ + i);
            assertEquals(1, c.size());
            PatientLocal pat = (PatientLocal) c.iterator().next();
            if (!PAT_NAME[i].equals(pat.getPatientName())) {
                fail("expected:" + PAT_NAME[i] + ", value:" + pat.getPatientName());
            }
        }
    }
}
