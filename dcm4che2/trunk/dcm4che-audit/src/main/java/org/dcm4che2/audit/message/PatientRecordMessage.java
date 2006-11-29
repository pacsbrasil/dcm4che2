package org.dcm4che2.audit.message;

/**
 * This message describes the event of a patient record being created,
 * modified, accessed, or deleted.
 * <p>
 * Note: There are several types of patient records managed by both DICOM
 * and non-DICOM system.  DICOM applications often manipulate patient
 * records managed by a variety of systems, and thus may be obligated by
 * site security policies to record such events in the audit logs. This
 * audit event can be used to record the access or manipulation of patient
 * records where specific DICOM SOP Instances are not involved.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 21, 2006
 */
public class PatientRecordMessage extends AuditMessage {
    
    public PatientRecordMessage(AuditEvent event, ActiveParticipant user,
            Patient patient) {
        super(event, user);
        super.addParticipantObject(patient);
    }
    
    public PatientRecordMessage(AuditEvent event, ActiveParticipant user1,
            ActiveParticipant user2, Patient patient) {
        super(event, user1);
        super.addActiveParticipant(user2); 
        super.addParticipantObject(patient);
    }

    /**
     * This method is deprecated and should not be used.
     * 
     * @deprecated
     * @exception java.lang.IllegalArgumentException if this method is invoked
     */
    public AuditMessage addActiveParticipant(ActiveParticipant apart) {
        throw new IllegalArgumentException();
    }

    /**
     * This method is deprecated and should not be used.
     *
     * @deprecated
     * @exception java.lang.IllegalArgumentException if this method is invoked
     */
    public AuditMessage addParticipantObject(ParticipantObject obj) {
        throw new IllegalArgumentException();
    }
    
    public static class AuditEvent extends org.dcm4che2.audit.message.AuditEvent {

        protected AuditEvent(ActionCode actionCode) {
            super(ID.PATIENT_RECORD);
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

        public static class Delete extends AuditEvent {

            public Delete() {
                super(ActionCode.DELETE);
            }        
        }
    }

}