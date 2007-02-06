package org.dcm4che2.audit.message;

import java.util.Date;

import org.dcm4che2.audit.message.AuditEvent.OutcomeIndicator;

/**
 * This message describes the event of a patient record being created,
 * modified, accessed, or deleted.
 * 
 * <blockquote>
 * Note: There are several types of patient records managed by both DICOM
 * and non-DICOM system.  DICOM applications often manipulate patient
 * records managed by a variety of systems, and thus may be obligated by
 * site security policies to record such events in the audit logs. This
 * audit event can be used to record the access or manipulation of patient
 * records where specific DICOM SOP Instances are not involved.
 * </blockquote>
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 21, 2006
 * @see <a href="ftp://medical.nema.org/medical/dicom/supps/sup95_fz.pdf">
 * DICOM Supp 95: Audit Trail Messages, A.1.3.11 Patient Record</a>
 */
public class PatientRecordMessage extends AuditMessageSupport {
    
    public PatientRecordMessage(AuditEvent.ActionCode action, Date eventDT,
            OutcomeIndicator outcome) {
        super(AuditEvent.ID.PATIENT_RECORD, action, eventDT, outcome);
    }
}