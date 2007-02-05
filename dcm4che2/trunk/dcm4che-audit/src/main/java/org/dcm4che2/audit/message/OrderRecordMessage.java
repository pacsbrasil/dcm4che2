package org.dcm4che2.audit.message;

/**
 * This message describes the event of an order being  created, modified,
 * accessed, or deleted.  This message may only include information about 
 * a single patient.
 * <blockquote>
 * Note: An order record typically is managed by a non-DICOM system.
 * However, DICOM applications often manipulate order records, and thus
 * may be obligated by site security policies to record such events in 
 * the audit logs.
 * </blockquote>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.10 Order Record</a>
 */
public class OrderRecordMessage extends AuditMessage {

    public OrderRecordMessage(AuditEvent.ActionCode action) {
        super(new AuditEvent(AuditEvent.ID.ORDER_RECORD, check(action)));
    }
    
    private static AuditEvent.ActionCode check(AuditEvent.ActionCode action) {
        if (action == AuditEvent.ActionCode.EXECUTE) {
            throw new IllegalArgumentException("action=Execute");
        }
        return action;
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
        
    public ParticipantObject addPatient(String id, String name) {
        return addParticipantObject(ParticipantObject.createPatient(id, name));
    }

}