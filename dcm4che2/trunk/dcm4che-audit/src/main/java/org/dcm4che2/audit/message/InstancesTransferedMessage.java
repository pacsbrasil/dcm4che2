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
 * This message describes the event of the completion of transferring DICOM 
 * SOP Instances  between two Application Entities. This message may only 
 * include information about a single patient.
 * <p>
 * Note: This message may have been preceded by a Begin Transferring Instances 
 * message. The Begin Transferring Instances message conveys the intent to 
 * store SOP Instances, while the InstancesTransferredMessage records the 
 * completion of the transfer.  Any disagreement between the two messages might 
 * indicate a potential security breach.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 23, 2006
 * @see BeginTransferingMessage
 */
public class InstancesTransferedMessage extends AuditMessage {

    public InstancesTransferedMessage(AuditEvent event, Source src, Destination dst,
            Patient patient, Study study) {
        super(event, src);
        super.addActiveParticipant(dst);
        super.addParticipantObject(patient);
        super.addParticipantObject(study);
    }

    public InstancesTransferedMessage addOtherParticipant(ActiveParticipant apart) {
        super.addActiveParticipant(apart);
        return this;
    }
    
    public InstancesTransferedMessage addStudy(Study study) {
        super.addParticipantObject(study);
        return this;
    }

    /**
     * This method is deprecated and should not be used.
     * 
     * @deprecated
     * @see #addOtherParticipant(ActiveParticipant)
     */
    public AuditMessage addActiveParticipant(ActiveParticipant apart) {
        return super.addActiveParticipant(apart);
    }

    /**
     * This method is deprecated and should not be used.
     *
     * @deprecated
     * @exception java.lang.IllegalArgumentException if obj is not a 
     * {@link Study}
     * @see #addStudy(Study)
     */
    public AuditMessage addParticipantObject(ParticipantObject obj) {
        if (obj instanceof Study) {
            return super.addParticipantObject(obj);            
        }
        throw new IllegalArgumentException();
    }
    
    public static class AuditEvent extends org.dcm4che2.audit.message.AuditEvent {

        protected AuditEvent(ActionCode actionCode) {
            super(ID.DICOM_INSTANCES_TRANSFERRED);
            setEventActionCode(actionCode);
        }        

        public static class Create extends AuditEvent {

            public Create() {
                super(ActionCode.CREATE);
            }        
        }

        public static class Read extends AuditEvent {

            public Read() {
                super(ActionCode.READ);
            }        
        }

        public static class Update extends AuditEvent {

            public Update() {
                super(ActionCode.UPDATE);
            }        
        }

        public static class Execute extends AuditEvent {

            public Execute() {
                super(ActionCode.EXECUTE);
            }        
        }
    }    
}