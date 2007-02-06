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
import org.dcm4che2.audit.message.AuditEvent.TypeCode;

/**
 * This audit message describes the event of an Application Entity 
 * starting or stopping.
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
 * <td>EV (110100, DCM,"Application Activity")</td>
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
 * <tr valign="top">
 * <td>EventTypeCode</td>
 * <td>M</td>
 * <td>DT (110120, DCM, "Application Start")<br/>
 * DT (110121, DCM, "Application Stop")</td>
 * </tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="3">ID of the Application started (1)</th></tr>
 * <tr valign="top">
 * <td>UserID</td>
 * <td>M</td>
 * <td>The identity of the process started or stopped formatted as specified in A.1.2.1.
 * </tr>
 * <tr valign="top">
 * <td>AlternateUserID</td>
 * <td>MC</td>
 * <td>If the process supports DICOM, then the AE Titles supported as specified in A.1.2.2.</td>
 * </tr>
 * <tr valign="top">
 * <td>UserName</td>
 * <td>U</td>
 * <td>not specialized</td>
 * </tr>
 * <tr valign="top">
 * <td>UserIsRequestor</td>
 * <td>U</td>
 * <td>EV FALSE</td>
 * </tr>
 * <tr valign="top">
 * <td>RoleIDCode</td>
 * <td>M</td>
 * <td>EV (110150, DCM, "Application")</td>
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
 * <tr align="left"><th colspan="3">ID of person or process that started the Application (0..N)</th></tr>
 * <tr valign="top">
 * <td>UserID</td>
 * <td>M</td>
 * <td>The person or process starting or stopping the Application</td>
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
 * <td>M</td>
 * <td>EV (110151, DCM, "Application Launcher")</td>
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
 * </table>
 * 
 * <p>No Participant Objects are needed for this message.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 21, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.1 Application Activity</a>
 */
public class ApplicationActivityMessage extends AuditMessage {

    public static final AuditEvent.TypeCode APPLICATION_START =
            AuditEvent.TypeCode.APPLICATION_START;
    public static final AuditEvent.TypeCode APPLICATION_STOP =
            AuditEvent.TypeCode.APPLICATION_STOP;
    
    
    public ApplicationActivityMessage(AuditEvent.TypeCode typeCode, 
            Date eventDT, OutcomeIndicator outcome) {
        super(new AuditEvent(AuditEvent.ID.APPLICATION_ACTIVITY, 
                AuditEvent.ActionCode.EXECUTE, eventDT, outcome)
            .addEventTypeCode(check(typeCode)));
    }
       
    private static TypeCode check(TypeCode typeCode) {
        if (typeCode == null) {
            throw new NullPointerException("typeCode");
        }
        return typeCode;
    }

    public ActiveParticipant addApplication(String processID, String[] aets, 
            String processName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActiveProcess(processID, aets, 
                        processName, hostname, false)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.APPLICATION));
    }

    public ActiveParticipant addApplicationLauncher(String userID,
            String altUserID, String userName, String hostname) {
        return addActiveParticipant(
                ActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, hostname, true)
                .addRoleIDCode(ActiveParticipant.RoleIDCode.APPLICATION_LAUNCHER));
    }
}