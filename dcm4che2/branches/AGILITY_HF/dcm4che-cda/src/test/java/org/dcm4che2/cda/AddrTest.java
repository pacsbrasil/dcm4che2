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
 * @since Mar 12, 2008
 */
public class AddrTest extends TestCase {
    static final String _21_NORTH_AVE = "21 North Ave";
    static final String BURLINGTON = "Burlington";
    static final String MA = "MA";
    static final String _01803 = "01803";
    static final String USA = "USA";
    static final String CUSTODIAN_ADDR = "<addr><streetAddressLine>"
            + _21_NORTH_AVE + "</streetAddressLine><city>" + BURLINGTON
            + "</city><state>" + MA + "</state><postalCode>" + _01803
            + "</postalCode><country>" + USA + "</country></addr>";

    static final String _17_DAWS_RD = "17 Daws Rd.";
    static final String BLUE_BELL = "Blue Bell";
    static final String _02368 = "02368";
    static final String PATIENT_ADDR = "<addr><streetAddressLine>"
            + _17_DAWS_RD + "</streetAddressLine><city>" + BLUE_BELL
            + "</city><state>" + MA + "</state><postalCode>" + _02368
            + "</postalCode><country>" + USA + "</country></addr>";

    static Addr createCustodianAddr() {
        return new Addr()
                .setStreetAddressLine(_21_NORTH_AVE)
                .setCity(BURLINGTON)
                .setState(MA)
                .setPostalCode(_01803)
                .setCountry(USA);
    }

    static Addr createPatientAddr() {
        return new Addr()
                .setStreetAddressLine(_17_DAWS_RD)
                .setCity(BLUE_BELL)
                .setState(MA)
                .setPostalCode(_02368)
                .setCountry(USA);
    }

    public void testToXML() {
        assertEquals(PATIENT_ADDR,
                AddrTest.createPatientAddr().toXML());
        assertEquals(CUSTODIAN_ADDR,
                AddrTest.createCustodianAddr().toXML());
    }

}
