package org.dcm4che2.audit.message;

/**
 * This message describes the event of an order being  created, modified,
 * accessed, or deleted.  This message may only include information about 
 * a single patient.
 * <p>
 * Note: An order record typically is managed by a non-DICOM system.
 * However, DICOM applications often manipulate order records, and thus
 * may be obligated by site security policies to record such events in 
 * the audit logs.
 * </p>
 */
public class OrderRecordMessage extends AuditMessage {

    public OrderRecordMessage(AuditEvent event, ActiveParticipant user,
            Patient patient) {
        super(event, user);
        super.addParticipantObject(patient);
    }
    
    public OrderRecordMessage(AuditEvent event, ActiveParticipant user1,
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
            super(ID.ORDER_RECORD);
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