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