package org.dcm4che2.audit.message;

/**
 * This message describes the event of a procedure record being created,
 * accessed, modified, accessed, or deleted.  This message may only include
 * information about a single patient.
 * <p>
 * Notes:<ol>
 * <li>DICOM applications often manipulate procedure records, e.g. with
 * MPPS update.  Modality Worklist query events are described by the Query
 * event.</li>
 * <li>The same accession number may appear with several order numbers.
 * The Study participant fields or the entire message may be repeated to
 * capture such many to many relationships.</li>
 * </ol>

 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 23, 2006
 */
public class ProcedureRecordMessage extends AuditMessage {
    
    public ProcedureRecordMessage(AuditEvent event, ActiveParticipant user,
            Patient patient) {
        super(event, user);
        super.addParticipantObject(patient);
    }
    
    public ProcedureRecordMessage(AuditEvent event, ActiveParticipant user1,
            ActiveParticipant user2, Patient patient) {
        super(event, user1);
        super.addActiveParticipant(user2); 
        super.addParticipantObject(patient);
    }
    
    public ProcedureRecordMessage addStudy(Study study) {
        super.addParticipantObject(study);
        return this;
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
            super(ID.PROCEDURE_RECORD);
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