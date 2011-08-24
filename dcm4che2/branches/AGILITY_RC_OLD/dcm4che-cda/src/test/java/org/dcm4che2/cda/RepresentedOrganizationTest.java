/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che2.cda;

import junit.framework.TestCase;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 13, 2008
 */
public class RepresentedOrganizationTest extends TestCase {

    static final String AUTHOR_ORG_EXT = "aaaaabbbbb";
    static final String AUTHOR_ORG_ROOT = "1.3.5.35.1.4436.7";
    static final String AUTHOR_ORG_NAME = "Dr. Wiseman’s Clinic";
    static final String AUTHOR_ORG = "<representedOrganization><id extension=\""
            + AUTHOR_ORG_EXT + "\" root=\"" + AUTHOR_ORG_ROOT + "\"/><name>"
            + AUTHOR_ORG_NAME + "</name></representedOrganization>";

    static final String DEVICE_ORG_ROOT = "1.3.6.4.1.4.1.2835.2";
    static final String DEVICE_ORG_NAME = "SOME Scanning Facility";
    static final String DEVICE_ORG = "<representedOrganization><id root=\""
            + DEVICE_ORG_ROOT + "\"/><name>" + DEVICE_ORG_NAME + "</name>"
            + AddrTest.CUSTODIAN_ADDR + "</representedOrganization>";

    static RepresentedOrganization createAuthorOrg() {
        return new RepresentedOrganization()
                .setID(new ID(AUTHOR_ORG_EXT, AUTHOR_ORG_ROOT))
                .setName(AUTHOR_ORG_NAME);
    }

    static RepresentedOrganization createDeviceOrg() {
        return new RepresentedOrganization()
                .setID(new ID(DEVICE_ORG_ROOT))
                .setName(DEVICE_ORG_NAME)
                .setAddr(AddrTest.createCustodianAddr());
    }

    public void testToXML() {
        assertEquals(AUTHOR_ORG,
                RepresentedOrganizationTest.createAuthorOrg().toXML());
        assertEquals(DEVICE_ORG,
                RepresentedOrganizationTest.createDeviceOrg().toXML());
    }

}
