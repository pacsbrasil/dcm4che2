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
public class ClinicalDocumentTest extends TestCase {

    static final String XDS_SD_ROOT = "1.3.6.4.1.4.1.2835.2.7777";
    static final String _34133_9 = "34133-9";
    static final String SUMMARIZATION_OF_EPISODE_NOTE =
            "SUMMARIZATION OF EPISODE NOTE";
    static final String GOOD_HEALTH_CLINIC_CARE_RECORD_SUMMARY =
            "Good Health Clinic Care Record Summary";
    static final String XDS_SC_TIME = "20050329224411+0500";
    static final String XDS_SD = "<ClinicalDocument xmlns=\"urn:hl7-org:v3\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "classCode=\"DOCCLIN\" moodCode=\"EVN\" "
            + "xsi:schemaLocation=\"urn:hl7-org:v3 CDA.xsd\">"
            + "<typeId extension=\"POCD_HD000040\" "
            + "root=\"2.16.840.1.113883.1.3\"/><id root=\"" + XDS_SD_ROOT
            + "\"/><code code=\"" + _34133_9
            + "\" codeSystem=\"2.16.840.1.113883.6.1\" "
            + "codeSystemName=\"LOINC\" displayName=\""
            + SUMMARIZATION_OF_EPISODE_NOTE + "\"/><title>"
            + GOOD_HEALTH_CLINIC_CARE_RECORD_SUMMARY
            + "</title><effectiveTime value=\"" + XDS_SC_TIME
            + "\"/><confidentialityCode code=\"N\" "
            + "codeSystem=\"2.16.840.1.113883.5.25\"/>"
            + "<languageCode code=\"en-US\"/>" + RecordTargetTest.RECORD_TARGET
            + AuthorTest.AUTHOR + AuthorTest.DEVICE
            + DataEntererTest.DATA_ENTERER + CustodianTest.CUSTODIAN
            + LegalAuthenticatorTest.AUTHENTICATOR
            + DocumentationOfTest.DOCUMENTATION_OF + "<component><nonXMLBody>"
            + TextTest.APP_PDF_B64
            + "</nonXMLBody></component></ClinicalDocument>";

    public void testToXML() {
        assertEquals(XDS_SD, createXDS_SD().toXML());
    }

    static ClinicalDocument createXDS_SD() {
        return new ClinicalDocument()
                .setID(new ID(XDS_SD_ROOT))
                .setCode(new Code.LOINC(
                        _34133_9, SUMMARIZATION_OF_EPISODE_NOTE))
                .setTitle(GOOD_HEALTH_CLINIC_CARE_RECORD_SUMMARY)
                .setEffectiveTime(
                        new ClinicalDocument.EffectiveTime(XDS_SC_TIME))
                .setConfidentialityCode(ConfidentialityCode.NORMAL)
                .setLanguageCode(LanguageCode.EN_US)
                .setRecordTarget(RecordTargetTest.createRecordTarget())
                .addAuthor(AuthorTest.createAuthor())
                .addAuthor(AuthorTest.createDevice())
                .setDataEnterer(DataEntererTest.createDataEnterer())
                .setCustodian(CustodianTest.createCustodian())
                .setLegalAuthenticator(
                        LegalAuthenticatorTest.createAuthenticator())
                .setDocumentationOf(
                        DocumentationOfTest.createDocumentationOf())
                .setComponent(new Component()
                        .setNonXMLBody(new NonXMLBody()
                                .setText(TextTest.createAPP_PDF_B64())));
    }

}
