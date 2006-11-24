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

    public ApplicationActivityMessage(AuditEvent event, Application app) {
        super(event, app);
    }
    
    /**
     * Adds ID of person or process that started the Application.
     * @param launcher ID of person or process that started the Application
     * @return this <tt>ApplicationActivityMessage</tt> obejct.
     */
    public ApplicationActivityMessage addApplicationLauncher(
            ApplicationLauncher launcher) {
        super.addActiveParticipant(launcher);
        return this;
    }
    
    /**
     * This method is deprecated and should not be used.
     * 
     * @deprecated
     * @exception java.lang.IllegalArgumentException if apart is not an
     * instance of <tt>ApplicationLauncher</tt>
     * @see #addApplicationLauncher(ApplicationLauncher)
     */
    public AuditMessage addActiveParticipant(ActiveParticipant apart) {
        if (apart instanceof ApplicationLauncher) {
            return super.addActiveParticipant(apart);
        }
        throw new IllegalArgumentException();
    }

    /**
     * This method is deprecated and should not be used because Application
     * Activity Audit Messages do not have a Participant Object.
     *
     * @deprecated
     * @exception java.lang.IllegalArgumentException if this method is invoked
     */
    public AuditMessage addParticipantObject(ParticipantObject obj) {
        throw new IllegalArgumentException();
    }
    
    /**
     * Event of {@link ApplicationActivityMessage}.
     * <p>
     * Value Constraints:
     * <ul>
     *   <li>eventID: {@link AuditEvent.ID#APPLICATION_ACTIVITY}</li>
     *   <li>eventActionCode: {@link AuditEvent.ActionCode#EXECUTE}</li>
     *   <li>eventTypeCode: defined values:
     *     <ul>
     *       <li>{@link AuditEvent.TypeCode#APPLICATION_START}</li>
     *       <li>{@link AuditEvent.TypeCode#APPLICATION_STOP}</li>
     *     </ul>
     *   </li>
     * </ul>
     */
    public static class AuditEvent extends org.dcm4che2.audit.message.AuditEvent {

        public AuditEvent(TypeCode code) {
            super(ID.APPLICATION_ACTIVITY);
            setEventActionCode(ActionCode.EXECUTE);
            addEventTypeCode(code);
        }
        
        public static class Start extends AuditEvent {

            public Start() {
                super(TypeCode.APPLICATION_START);
            }        
        }

        public static class Stop extends AuditEvent {

            public Stop() {
                super(TypeCode.APPLICATION_STOP);
            }        
        }       
    }
        
}