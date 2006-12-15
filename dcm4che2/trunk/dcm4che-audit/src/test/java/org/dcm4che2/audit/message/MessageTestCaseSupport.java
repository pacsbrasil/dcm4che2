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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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
 
package org.dcm4che2.audit.message;

import java.io.InputStream;

import junit.framework.TestCase;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 14, 2006
 */
class MessageTestCaseSupport extends TestCase {

    private static final String AUDIT_SOURCE_ID = "PACS_DCM4CHEE";
    private static final AuditSource.TypeCode AUDIT_SOURCE_TYPE_CODE = 
            AuditSource.TypeCode.APPLICATION_SERVER_PROCESS;

    private static final String USER_ID = "user@dcm4che.org";
    private static final String PACS_ID = "PACS_DCM4CHEE";
    private static final String PACS_AET = "DCM4CHEE";
    private static final String MESA = "MESA";
    private static final String MESA_WS_AET = "MESA_WKSTATION";
    private static final String PAT_ID = "GE1115";
    private static final String PAT_NAME = "DAVIDSON^JOSHUA";
    private static final String STUDY_UID = "1.2.840.113674.1115.261.200";
    private static final String SOP_CLASS = "1.2.840.10008.5.1.4.1.1.4";
    private static final String MEDIA_ID = "DISK1";
    private static final String HOST = "www.dcm4che.org";
    
    
    public MessageTestCaseSupport() {
    }

    public MessageTestCaseSupport(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        AuditSource auditSource = new AuditSource(AUDIT_SOURCE_ID);
        auditSource.addAuditSourceTypeCode(AUDIT_SOURCE_TYPE_CODE);
        AuditMessage.setDefaultAuditSource(auditSource);
        AuditMessage.setEncoding("ISO-8859-1");
    }

    protected void assertXML(AuditMessage msg) throws Exception {
        String xml = msg.toString();
        String ref = load(
                msg.getAuditEvent().getClass().getName().replace('.', '/') 
                                + ".xml");
        assertEquals(ref.substring(0, 94), xml.substring(0, 94));
        // skip value of EventDateTime
        assertEquals(ref.substring(123), xml.substring(123));
    }

    private String load(String name) throws Exception {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        InputStream in = ccl.getResourceAsStream(name);
        byte[] b = new byte[in.available()];
        in.read(b);
        in.close();
        return new String(b, "ISO-8859-1");
    }

    static Source mkArchiveSource() {
        Source src = new Source(PACS_ID);
        src.setAETitle(PACS_AET);
        return src;
    }

    static Source mkMesaSource() {
        Source src = new Source(MESA);
        src.setAETitle(MESA_WS_AET);
        return src;
    }

    static Destination mkArchiveDestination() {
        Destination dst = new Destination(PACS_ID);
        dst.setAETitle(PACS_AET);
        return dst;
    }

    static Destination mkMesaDestination() {
        Destination dst = new Destination(MESA);
        dst.setAETitle(MESA_WS_AET);
        return dst;
    }

    static DestinationMedia mkDestinationMedia() {
        DestinationMedia dst = new DestinationMedia(MEDIA_ID);
        return dst;
    }
    
    static ActiveParticipant mkUser() {
        ActiveParticipant user = new ActiveParticipant(USER_ID);
        return user;
    }    

    static UserWithLocation mkUserWithLocation() {
        return new UserWithLocation(USER_ID, 
                new NetworkAccessPoint.HostName(HOST));
    }
    
    static Patient mkPatient() {
        Patient pat = new Patient(PAT_ID);
        pat.setPatientName(PAT_NAME);
        return pat;
    }
    
    static Study mkStudy() {
        Study study = new Study(STUDY_UID);
        ParticipantObjectDescription desc = new ParticipantObjectDescription();
        ParticipantObjectDescription.SOPClass sopClass = 
                new ParticipantObjectDescription.SOPClass(SOP_CLASS);
        sopClass.setNumberOfInstances(10);
        study.addParticipantObjectDescription(desc);
        return study;
    }


    static QuerySOPClass mkQuerySOPClass() throws Exception {
        DicomObject keys = new BasicDicomObject();
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        keys.putString(Tag.PatientID, VR.LO, PAT_ID);
        return new QuerySOPClass(
                UID.StudyRootQueryRetrieveInformationModelFIND, keys);
    }


}
