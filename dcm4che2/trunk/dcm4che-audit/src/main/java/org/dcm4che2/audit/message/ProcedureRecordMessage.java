package org.dcm4che2.audit.message;

/**
 * This message describes the event of a procedure record being created,
 * accessed, modified, accessed, or deleted.  This message may only include
 * information about a single Procedure.
 * 
 * <blockquote>
 * Notes:<ol>
 * <li>DICOM applications often manipulate procedure records, e.g. with
 * MPPS update.  Modality Worklist query events are described by the Query
 * event.</li>
 * <li>The same accession number may appear with several order numbers.
 * The Study participant fields or the entire message may be repeated to
 * capture such many to many relationships.</li>
 * </ol>
 * </blockquote>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 23, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.12 Procedure Record</a>
 */
public class ProcedureRecordMessage extends AuditMessage {
    
    public ProcedureRecordMessage(AuditEvent.ActionCode action) {
        super(new AuditEvent(AuditEvent.ID.PROCEDURE_RECORD, check(action)));
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


    public ParticipantObject addStudy(String uid,
            ParticipantObjectDescription desc) {
        return addParticipantObject(ParticipantObject.createStudy(uid, desc));
    }
}