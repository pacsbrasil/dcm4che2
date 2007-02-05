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


/**
 * This message describes the event of a person or process accessing a log 
 * of audit trail information.
 * 
 * <blockquote>
 * Note: For example, an implementation that maintains a local cache of
 * audit information that has not been transferred to a central collection
 * point might generate this message if its local cache were accessed by a user.
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
 * <td>EV (110101, DCM, "Audit Log Used")</td>
 * </tr>
 * <tr valign="top">
 * <td>EventActionCode</td>
 * <td>M</td>
 * <td>EV "R" (Read))</td>
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
 * <tr valign="top">
 * <td>EventTypeCode</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Active Participant (1..2)</th></tr>
 * <tr valign="top">
 * <td>UserID</td>
 * <td>M</td>
 * <td>The person or process accessing the audit trail. If both are known, then 
 * two active participants shall be included (both the person and the process).
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
 * <td>EV TRUE</td>
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
 * <td>NetworkAccessPointID</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">Identity of the audit log (1)</th></tr>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectTypeCode</td>
 * <td>M</td>
 * <td>EV 2 (system)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectTypeCodeRole</td>
 * <td>M</td>
 * <td>EV 13 (security resource)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectDataLifeCycle</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectIDTypeCode</td>
 * <td>M</td>
 * <td>EV 12 (URI)</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectSensitivity</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectID</td>
 * <td>M</td>
 * <td>The URI of the audit log</td>
 * </tr>
 * <tr valign="top">
 * <td>ParticipantObjectName</td>
 * <td>U</td>
 * <td>EV "Security Audit Log" </td>
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
 * <td>not further specialized</td>
 * </tr>
 * </table>
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 21, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.2 Audit Log Used</a>
 */
public class AuditLogUsedMessage extends AuditMessage {

    public AuditLogUsedMessage() {
        super(new AuditEvent(AuditEvent.ID.AUDIT_LOG_USED, 
                AuditEvent.ActionCode.READ));
    }
    
    public ActiveParticipant addUserPerson(String userID, String altUserID, 
            String userName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, true));
    }
    
    public ActiveParticipant addUserProcess(String processID, String[] aets, 
            String processName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, true));
    }
        
    public ParticipantObject addSecurityAuditLog(String uri) {
        ParticipantObject obj = ParticipantObject.createSecurityAuditLog(uri);
        addParticipantObject(obj);
        return obj;
    }

}