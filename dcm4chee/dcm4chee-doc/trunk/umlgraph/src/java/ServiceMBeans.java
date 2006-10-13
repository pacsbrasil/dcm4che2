class ExportManagerQueue {}

class FileCopyQueue {}

class HL7SendQueue {}

class IANScuQueue {}

class MCMScuQueue {}

class MPPSScuQueue {}

class MoveScuQueue {}

class PPSExceptionMgtQueue {}

class PPSScuQueue {}

class PurgeStudyQueue {}

class SendmailQueue {}

class StgCmtScuScpQueue {}

class StudyMgtScuQueue {}

/**
 * @depend - - - AuditLogger
 * @depend - - - ECHOService
 */
class AE {}

class AuditLogger {}

/**
 * @depend - - - Scheduler
 */
class CheckStudyPatientService {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - Scheduler
 */
class CompressionService {}

/**
 * @depend - - - Scheduler
 */
class ConsistencyService {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - HL7Send
 * @depend - - - StoreScp
 * @depend - - - StudyMgtScu
 */
class ContentEditService {}

/**
 * @depend - - - AuditLogger
 * @depend - - - TLSConfig
 */
class DcmServer {}

class DeviceService {}

/**
 * @depend - - - TLSConfig
 */
class ECHOService {}

/**
 * @depend - - - AuditLogger
 * @depend - - - StoreScp
 * @depend - - - ExportManagerQueue
 * @depend - - - JMS
 */
class ExportManager {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - StoreScp
 * @depend - - - FileCopyQueue
 * @depend - - - JMS
 */
class FileCopy {}

/**
 * @depend - - - Scheduler
 * @depend - - - PurgeStudyQueue
 * @depend - - - JMS
 */
class FileSystemMgt {}

class FixPatientAttributesService {}

/**
 * @depend - - - StoreScp
 * @depend - - - MoveScu
 * @depend - - - ContentEditService
 */
class Forward {}

/**
 * @depend - - - AuditLogger
 * @depend - - - MPPSScp
 */
class GPWLFeed {}

/**
 * @depend - - - DcmServer
 */
class GPWLScp {}

class GPWLScu {}

/**
 * @depend - - - HL7Server
 * @depend - - - TLSConfig
 * @depend - - - HL7SendQueue
 * @depend - - - JMS
 */
class HL7Send {}

/**
 * @depend - - - TLSConfig
 * @depend - - - AuditLogger
 */
class HL7Server {}

/**
 * @depend - - - HL7Server
 */
class ADT_HL7Service {}

/**
 * @depend - - - HL7Server
 * @depend - - - DeviceService
 */
class ORM_HL7Service {}

/**
 * @depend - - - HL7Server
 * @depend - - - ExportManager
 */
class ORU_HL7Service {}

/**
 * @depend - - - AE
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 * @depend - - - TLSConfig
 */
class HPScp {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - StoreScp
 * @depend - - - MPPSScp
 * @depend - - - TLSConfig
 * @depend - - - IANScuQueue
 * @depend - - - JMS
 */
class IANScu {}

class JMS {}

class KeyObjectService {}

/**
 * @depend - - - Scheduler
 * @depend - - - Sendmail
 * @depend - - - MCMScuQueue
 * @depend - - - JMS
 */
class MCMScu {}

/**
 * @depend - - - Scheduler
 */
class MD5CheckService {}

/**
 * @depend - - - MPPSScp
 * @depend - - - HL7Send
 */
class MPPS2ORM {}

/**
 * @depend - - - Scheduler
 * @depend - - - MPPSScu
 */
class MPPSEmulator {}

/**
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 */
class MPPSScp {}

/**
 * @depend - - - MPPSScp
 * @depend - - - TLSConfig
 * @depend - - - MPPSScuQueue
 * @depend - - - JMS
 */
class MPPSScu {}

/**
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 * @depend - - - MPPSScp
 */
class MWLFindScp {}

/**
 * @depend - - - TLSConfig
 */
class MWLScu {}

/**
 * @depend - - - QueryRetrieveScp
 * @depend - - - TLSConfig
 * @depend - - - MoveScuQueue
 * @depend - - - JMS
 */
class MoveScu {}

/**
 * @depend - - - MPPSScp
 * @depend - - - PPSExceptionMgtQueue
 * @depend - - - JMS
 */
class PPSExceptionMgtService {}

/**
 * @depend - - - GPWLScp
 * @depend - - - TLSConfig
 * @depend - - - PPSScuQueue
 * @depend - - - JMS
 */
class PPSScu {}

/**
 * @depend - - - AE
 * @depend - - - DcmServer
 * @depend - - - TLSConfig
 * @depend - - - AuditLogger
 * @depend - - - FileSystemMgt
 * @depend - - - StgCmtScuScp
 * @depend - - - TarRetriever
 */
class QueryRetrieveScp {}

/**
 * @depend - - - AuditLogger
 * @depend - - - FileSystemMgt
 * @depend - - - Scheduler
 */
class RIDService {}

class Scheduler {}

/**
 * @depend - - - SendmailQueue
 * @depend - - - JMS
 */
class Sendmail {}

/**
 * @depend - - - AE
 * @depend - - - DcmServer
 * @depend - - - FileSystemMgt
 * @depend - - - TLSConfig
 * @depend - - - StgCmtScuScpQueue
 * @depend - - - JMS
 */
class StgCmtScuScp {}

/**
 * @depend - - - DcmServer
 * @depend - - - FileSystemMgt
 * @depend - - - MWLScu
 * @depend - - - Scheduler
 */
class StoreScp {}

class StudyInfoService {}

/**
 * @depend - - - DcmServer
 */
class StudyMgtScp {}

/**
 * @depend - - - StudyMgtScp
 * @depend - - - StudyMgtScuQueue
 * @depend - - - JMS
 */
class StudyMgtScu {}

/**
 * @depend - - - TLSConfig
 * @depend - - - Scheduler
 */
class StudyReconciliation {}

/**
 * @depend - - - GPWLScp
 */
class StudyStatus {}

/**
 * @depend - - - Scheduler
 */
class SyncFileStatus {}

/**
 * @depend - - - AuditLogger
 */
class TLSConfig {}

class TarRetriever {}

/**
 * @depend - - - Scheduler
 */
class WADOExtService {}

/**
 * @depend - - - Scheduler
 */
class WADOService {}

/**
 * @depend - - - FileSystemMgt
 */
class XDS_I {}

class XDSService {}
