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
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.11 Patient Record</a>
 */
public class PatientRecordMessage extends AuditMessage {
    
    public PatientRecordMessage(AuditEvent.ActionCode action) {
        super(new AuditEvent(AuditEvent.ID.PATIENT_RECORD, check(action)));
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