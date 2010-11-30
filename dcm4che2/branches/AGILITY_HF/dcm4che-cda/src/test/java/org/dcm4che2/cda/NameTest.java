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
public class NameTest extends TestCase {

    static final String MRS = "Mrs.";
    static final String BERNICE = "Bernice";
    static final String SMITH = "Smith";
    static final String DATA_ENTERER_NAME = "<name><prefix>" + MRS
            + "</prefix><given>" + BERNICE + "</given><family>" + SMITH
            + "</family></name>";

    static final String DR = "Dr.";
    static final String BERNARD = "Bernard";
    static final String WISEMAN = "Wiseman";
    static final String SR = "Sr.";
    static final String AUTHOR_NAME = "<name><prefix>" + DR
            + "</prefix><given>" + BERNARD + "</given><family>" + WISEMAN
            + "</family><suffix>" + SR + "</suffix></name>";

    static final String ELLEN = "Ellen";
    static final String ROSS = "Ross";
    static final String PATIENT_NAME = "<name><prefix>" + MRS + "</prefix><given>"
            + ELLEN + "</given><family>" + ROSS + "</family></name>";

    static Name createPatientName() {
        return new Name()
                .setPrefix(MRS)
                .setGiven(ELLEN)
                .setFamily(ROSS);
    }

    static Name createDataEntererName() {
        return new Name()
                .setPrefix(MRS)
                .setGiven(BERNICE)
                .setFamily(SMITH);
    }

    static Name createAuthorName() {
        return new Name()
                .setPrefix(DR)
                .setGiven(BERNARD)
                .setFamily(WISEMAN)
                .setSuffix(SR);
    }

    public void testToXML() {
        assertEquals(PATIENT_NAME,
                NameTest.createPatientName().toXML());
        assertEquals(AUTHOR_NAME,
                NameTest.createAuthorName().toXML());
        assertEquals(DATA_ENTERER_NAME,
                NameTest.createDataEntererName().toXML());
    }

}
