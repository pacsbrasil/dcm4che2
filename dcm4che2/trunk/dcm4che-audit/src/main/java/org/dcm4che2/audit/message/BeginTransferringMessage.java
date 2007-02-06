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

import java.util.Date;

import org.dcm4che2.audit.message.AuditEvent.OutcomeIndicator;

/**
 * This message describes the event of a system begining to transfer a set 
 * of DICOM instances from one node to another node within control of the
 * system's security domain. This message may only include information about
 * a single patient.
 * 
 * <blockquote>
 * Note: A separate Instances Transferred message is defined for transfer
 * completion, allowing comparison of what was intended to be sent and what was
 * actually sent.
 * </blockquote>
 * 
 * <h4>Message Structure</h4>
 * <table border="0" cellpadding="1" cellspacing="0">
 * <tr align="left" bgcolor="#ccccff">
 * <th>Field Name</th>
 * <th>Opt.</th>
 * <th>Value Constraints</th>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Event</th></tr>
 * <tr valign="top">
 * <td>EventID</td>
 * <td>M</td>
 * <td>EV (110102, DCM, "Begin Transferring DICOM Instances")</td>
 * </tr>
 * <tr valign="top">
 * <td>EventActionCode</td>
 * <td>M</td>
 * <td>EV "E" (Execute)</td>
 * </tr>
 * <tr valign="top">
 * <td>EventDateTime</td>
 * <td>M</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>EventOutcomeIndicator</td>
 * <td>M</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>EventTypeCode</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Source (1)</th></tr>
 * <tr valign="top">
 * <td>UserID</td>
 * <td>M</td>
 * <td>The identity of the process requesting that data be transferred.</td>
 * </tr>
 * <tr valign="top">
 * <td>AlternateUserID</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>UserName</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>UserIsRequestor</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>RoleIDCode</td>
 * <td>M</td>
 * <td>EV (110153, DCM, "Source")</td>
 * </tr>
 * <tr valign="top">
 * <td>NetworkAccessPointTypeCode</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>NetworkAccessPointID</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Destination (1)</th></tr>
 * <tr valign="top">
 * <td>UserID</td>
 * <td>M</td>
 * <td>The identity of the process receiving the data.</td>
 * </tr>
 * <tr valign="top">
 * <td>AlternateUserID</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>UserName</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>UserIsRequestor</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>RoleIDCode</td>
 * <td>M</td>
 * <td>EV (110152, DCM, "Destination")</td>
 * </tr>
 * <tr valign="top">
 * <td>NetworkAccessPointTypeCode</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>NetworkAccessPointID</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Other Participants (0..N)</th></tr>
 * <tr valign="top">
 * <td>UserID</td>
 * <td>M</td>
 * <td>The identity of any other participants that might be involved and known,
 * especially third parties that are the requestor</td>
 * </tr>
 * <tr valign="top">
 * <td>AlternateUserID</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>UserName</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>UserIsRequestor</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>RoleIDCode</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>NetworkAccessPointTypeCode</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>NetworkAccessPointID
 * <td>U
 * <td>not specialized
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Studies being transferred (1..N)</th></tr>
 * <tr valign="top">
 * <td>ParticipantObjectTypeCode</td>
 * <td>M</td>
 * <td>EV 2 (system)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectTypeCodeRole</td>
 * <td>M</td>
 * <td>EV 3 (report)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectDataLifeCycle</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectIDTypeCode</td>
 * <td>M</td>
 * <td>EV (110180, DCM, "Study Instance UID")</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectSensitivity</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectID</td>
 * <td>M</td>
 * <td>The Study Instance UID</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectName</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectQuery</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectDetail</td>
 * <td>U</td>
 * <td>Element "ContainsSOPClass" with one or more SOP Class UID values </td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectDescription</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>SOPClass</td>
 * <td>MC</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>Accession</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>NumberOfInstances</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>Instances</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>Encrypted</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>Anonymized</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Patients (1)</th></tr>
 * <tr valign="top">
 * <td>ParticipantObjectTypeCode</td>
 * <td>M</td>
 * <td>EV 1 (person)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectTypeCodeRole</td>
 * <td>M</td>
 * <td>EV 1 (patient)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectDataLifeCycle</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectIDTypeCode</td>
 * <td>M</td>
 * <td>EV 2 (patient ID)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectSensitivity</td>
 * <td>U</td>
 * <td>not specialized</td>
 * <tr valign="top">
 * <td>ParticipantObjectSensitivity</td>
 * <td>ParticipantObjectID</td>
 * <td>M</td>
 * <td>The patient ID</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectName</td>
 * <td>U</td>
 * <td>The patient name</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectQuery</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectDetail</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectDescription</td>
 * <td>U</td>
 * <td>notEncrypted</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>Anonymized</td>
 * <td>U</td>
 * <td>not further specialized</td>
 * </tr>
 * </table>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.3 Begin Transferring DICOM Instances</a>
 */
public class BeginTransferringMessage extends AuditMessage {

    public BeginTransferringMessage(Date eventDT, OutcomeIndicator outcome) {
        super(new AuditEvent(AuditEvent.ID.BEGIN_TRANSFERRING_DICOM_INSTANCES, 
                AuditEvent.ActionCode.EXECUTE, eventDT, outcome));
    }

    public ActiveParticipant addSourceProcess(String processID, String[] aets,
            String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.SOURCE));
    }
    
    public ActiveParticipant addDestinationProcess(String processID, String[] aets, 
            String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.DESTINATION));
    }

    public ActiveParticipant addOtherParticipantPerson(String userID,
            String altUserID, String userName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, requestor));
    }
    
    public ActiveParticipant addOtherParticipantProcess(String processID,
            String[] aets, String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, requestor));
    }
            
    public ParticipantObject addPatient(String id, String name) {
        return addParticipantObject(ParticipantObject.createPatient(id, name));
    }

    public ParticipantObject addStudy(String uid,
            ParticipantObjectDescription desc) {
        return addParticipantObject(ParticipantObject.createStudy(uid, desc));
    }


}