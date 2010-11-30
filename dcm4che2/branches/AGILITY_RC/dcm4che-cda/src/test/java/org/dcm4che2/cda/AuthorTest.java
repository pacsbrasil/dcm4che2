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
public class AuthorTest extends TestCase {

    static final String AUTHOR_TIME = "19990522";
    static final String AUTHOR_ROOT = "1.3.5.35.1.4436.7";
    static final String AUTHOR_EXT = "11111111";
    static final String AUTHOR = "<author><time value=\"" + AUTHOR_TIME
            + "\"/><assignedAuthor><id extension=\"" + AUTHOR_EXT
            + "\" root=\"" + AUTHOR_ROOT + "\"/><assignedPerson>"
            + NameTest.AUTHOR_NAME + "</assignedPerson>"
            + RepresentedOrganizationTest.AUTHOR_ORG
            + "</assignedAuthor></author>";

    static final String SCAN_TIME = "20050329224411+0500";
    static final String DEVICE_ROOT = "1.3.6.4.1.4.1.2835.2.1234";
    static final String DEVICE = "<author><time value=\"" + SCAN_TIME
            + "\"/><assignedAuthor><id root=\"" + DEVICE_ROOT + "\"/>"
            + AssignedAuthoringDeviceTest.DEVICE
            + RepresentedOrganizationTest.DEVICE_ORG
            + "</assignedAuthor></author>";

    public void testToXML() {
        assertEquals(AUTHOR, AuthorTest.createAuthor().toXML());
        assertEquals(DEVICE, AuthorTest.createDevice().toXML());
    }

    static Author createDevice() {
        return new Author()
            .setTime(new Time(SCAN_TIME))
            .setAssignedAuthor(new AssignedAuthor()
                    .setID(new ID(DEVICE_ROOT))
                    .setAssignedAuthoringDevice(
                            AssignedAuthoringDeviceTest.createDevice())
                    .setRepresentedOrganization(
                            RepresentedOrganizationTest.createDeviceOrg()));
    }

    static Author createAuthor() {
        return new Author()
            .setTime(new Time(AUTHOR_TIME))
            .setAssignedAuthor(new AssignedAuthor()
                    .setID(new ID(AUTHOR_EXT, AUTHOR_ROOT))
                    .setAssignedPerson(new AssignedPerson()
                        .setName(NameTest.createAuthorName()))
                    .setRepresentedOrganization(
                            RepresentedOrganizationTest.createAuthorOrg()));
    }

}
