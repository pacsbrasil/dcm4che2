/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

package org.dcm4che.net;

import org.dcm4che.dict.UIDs;

import java.io.*;
import java.util.*;

import junit.framework.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class AAssociateRQACTest extends ExtTestCase {

    public AAssociateRQACTest(java.lang.String testName) {
        super(testName);
    }        
    
    protected static final String CALLING_AET = "CALLING_AET";
    protected static final String CALLED_AET = "CALLED_AET";
    protected static final String AS_UID1 = UIDs.Verification;
    protected static final String AS_UID2 = UIDs.ComputedRadiographyImageStorage;
    protected static final String TS_UID1 = UIDs.ImplicitVRLittleEndian;
    protected static final String TS_UID2 = UIDs.ExplicitVRLittleEndian;
    protected static final String TS_UID3 = UIDs.JPEGExtended;
    protected static final int MAX_LENGTH = 12345;
    protected static final int MAX_OPS_INVOKED = 0;
    protected static final int MAX_OPS_PERFORMED = 2;
    protected static final byte[] EXT_NEG_INFO = new byte[0xe];
    
    protected PDUFactory fact;
        
    protected void setUp() throws Exception {
        fact = PDUFactory.getInstance();
        Arrays.fill(EXT_NEG_INFO, (byte)0xee);
    }
    
    protected void check(AAssociateRQAC rqac) {
        assertEquals(1, rqac.getProtocolVersion());
        assertEquals(CALLING_AET, rqac.getCallingAET());
        assertEquals(CALLED_AET, rqac.getCalledAET());
        assertEquals(UIDs.DICOMApplicationContextName,
                rqac.getApplicationContextUID());
        assertEquals(MAX_LENGTH, rqac.getMaxLength());
        AsyncOpsWindow aow = rqac.getAsyncOpsWindow();
        assertNotNull(aow);
        assertEquals(MAX_OPS_INVOKED, aow.getMaxOpsInvoked());
        assertEquals(MAX_OPS_PERFORMED, aow.getMaxOpsPerformed());
        assertEquals(2, rqac.countRoleSelections());
        Iterator rsit = rqac.iterateRoleSelections();
        RoleSelection rs1 = (RoleSelection)rsit.next();
        assertEquals(AS_UID1, rs1.getSOPClassUID());
        assertTrue(rs1.scu());
        assertTrue(rs1.scp());
        assertEquals(rs1, rqac.getRoleSelection(AS_UID1));
        RoleSelection rs2 = (RoleSelection)rsit.next();
        assertEquals(AS_UID2, rs2.getSOPClassUID());
        assertTrue(rs2.scu());
        assertTrue(!rs2.scp());
        assertEquals(rs2, rqac.getRoleSelection(AS_UID2));
        assertTrue(!rsit.hasNext());
        assertEquals(1, rqac.countExtNegotiations());
        Iterator enit = rqac.iterateExtNegotiations();
        ExtNegotiation en = (ExtNegotiation)enit.next();
        assertEquals(AS_UID2, en.getSOPClassUID());
        assertEquals(EXT_NEG_INFO, en.info());
        assertEquals(en, rqac.getExtNegotiation(AS_UID2));
        assertTrue(!enit.hasNext());
    }
        
    protected void set(AAssociateRQAC rqac) {
        rqac.setCallingAET(CALLING_AET);
        rqac.setCalledAET(CALLED_AET);
        rqac.setMaxLength(MAX_LENGTH);
        rqac.setAsyncOpsWindow(fact.newAsyncOpsWindow(
                MAX_OPS_INVOKED, MAX_OPS_PERFORMED));
        rqac.addRoleSelection(fact.newRoleSelection(AS_UID1, true, true));
        rqac.addRoleSelection(fact.newRoleSelection(AS_UID2, true, false));
        rqac.addExtNegotiation(fact.newExtNegotiation(AS_UID2, EXT_NEG_INFO));
    }
}
