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
 * @navassoc - use - AuditLogger
 * @navassoc - use - ECHOService
 */
class AE {}

/**
 * @view
 * @opt hide
 * 
 * @match context AE
 * @opt !hide
 */
class ae {}

class AuditLogger {}

/**
 * @view
 * @opt hide
 * 
 * @match context AuditLogger
 * @opt !hide
 */
class auditLogger {}

/**
 * @depend - notifiedBy - Scheduler
 */
class CheckStudyPatientService {}

/**
 * @view
 * @opt hide
 * 
 * @match context CheckStudyPatientService
 * @opt !hide
 */
class checkStudyPatientService {}

/**
 * @navassoc - use - FileSystemMgt
 * @depend - notifiedBy - Scheduler
 */
class CompressionService {}

/**
 * @view
 * @opt hide
 * 
 * @match context CompressionService
 * @opt !hide
 */
class compressionService {}

/**
 * @depend - notifiedBy - Scheduler
 */
class ConsistencyService {}

/**
 * @view
 * @opt hide
 * 
 * @match context ConsistencyService
 * @opt !hide
 */
class consistencyService {}

/**
 * @navassoc - use - FileSystemMgt
 * @navassoc - use - HL7Send
 * @navassoc - use - StoreScp
 * @navassoc - use - StudyMgtScu
 */
class ContentEditService {}

/**
 * @view
 * @opt hide
 * 
 * @match context ContentEditService
 * @opt !hide
 */
class contentEditService {}

/**
 * @navassoc - use - AuditLogger
 * @navassoc - use - TLSConfig
 */
class DcmServer {}

/**
 * @view
 * @opt hide
 * 
 * @match context DcmServer
 * @opt !hide
 */
class dcmServer {}

class DeviceService {}

/**
 * @view
 * @opt hide
 * 
 * @match context DeviceService
 * @opt !hide
 */
class deviceService {}

/**
 * @navassoc - use - TLSConfig
 */
class ECHOService {}

/**
 * @view
 * @opt hide
 * 
 * @match context ECHOService
 * @opt !hide
 */
class echoService {}

/**
 * @navassoc - use - AuditLogger
 * @depend - notifiedBy - StoreScp
 * @depend - listenTo - ExportManagerQueue
 * @navassoc - use - JMS
 */
class ExportManager {}

/**
 * @view
 * @opt hide
 * 
 * @match context ExportManager
 * @opt !hide
 */
class exportManager {}

/**
 * @navassoc - use - FileSystemMgt
 * @depend - notifiedBy - StoreScp
 * @depend - listenTo - FileCopyQueue
 * @navassoc - use - JMS
 */
class FileCopy {}

/**
 * @view
 * @opt hide
 * 
 * @match context FileCopy
 * @opt !hide
 */
class fileCopy {}

/**
 * @depend - notifiedBy - Scheduler
 * @depend - listenTo - PurgeStudyQueue
 * @navassoc - use - JMS
 */
class FileSystemMgt {}

/**
 * @view
 * @opt hide
 * 
 * @match context FileSystemMgt
 * @opt !hide
 */
class fileSystemMgt {}

class FixPatientAttributesService {}

/**
 * @view
 * @opt hide
 * 
 * @match context FixPatientAttributesService
 * @opt !hide
 */
class fixPatientAttributesService {}

/**
 * @depend - notifiedBy - StoreScp
 * @navassoc - use - MoveScu
 * @depend - notifiedBy - ContentEditService
 */
class Forward {}

/**
 * @view
 * @opt hide
 * 
 * @match context Forward
 * @opt !hide
 */
class forward {}

/**
 * @navassoc - use - AuditLogger
 * @depend - notifiedBy - MPPSScp
 */
class GPWLFeed {}

/**
 * @view
 * @opt hide
 * 
 * @match context GPWLFeed
 * @opt !hide
 */
class gpwlFeed {}

/**
 * @navassoc - attachedTo - DcmServer
 */
class GPWLScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context GPWLScp
 * @opt !hide
 */
class gpwlScp {}

class GPWLScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context GPWLScu
 * @opt !hide
 */
class gpwlScu {}

/**
 * @depend - notifiedBy - HL7Server
 * @navassoc - use - TLSConfig
 * @depend - listenTo - HL7SendQueue
 * @navassoc - use - JMS
 */
class HL7Send {}

/**
 * @view
 * @opt hide
 * 
 * @match context HL7Send
 * @opt !hide
 */
class hl7Send {}

/**
 * @navassoc - use - TLSConfig
 * @navassoc - use - AuditLogger
 */
class HL7Server {}

/**
 * @view
 * @opt hide
 * 
 * @match context HL7Server
 * @opt !hide
 */
class hl7Server {}

/**
 * @navassoc - attachedTo - HL7Server
 */
class HL7ServiceADT {}

/**
 * @view
 * @opt hide
 * 
 * @match context HL7ServiceADT
 * @opt !hide
 */
class hl7ServiceADT {}

/**
 * @navassoc - attachedTo - HL7Server
 * @navassoc - use - DeviceService
 */
class HL7ServiceORM {}

/**
 * @view
 * @opt hide
 * 
 * @match context HL7ServiceORM
 * @opt !hide
 */
class hl7ServiceORM {}

/**
 * @navassoc - attachedTo - HL7Server
 * @navassoc - use - ExportManager
 */
class HL7ServiceORU {}

/**
 * @view
 * @opt hide
 * 
 * @match context HL7ServiceORU
 * @opt !hide
 */
class hl7ServiceORU {}

/**
 * @navassoc - use - AE
 * @navassoc - use - AuditLogger
 * @navassoc - attachedTo - DcmServer
 * @navassoc - use - TLSConfig
 */
class HPScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context HPScp
 * @opt !hide
 */
class hpScp {}

/**
 * @navassoc - use - FileSystemMgt
 * @depend - notifiedBy - StoreScp
 * @depend - notifiedBy - MPPSScp
 * @navassoc - use - TLSConfig
 * @depend - listenTo - IANScuQueue
 * @navassoc - use - JMS
 */
class IANScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context IANScu
 * @opt !hide
 */
class ianScu {}

class JMS {}

/**
 * @view
 * @opt hide
 * 
 * @match context JMS
 * @opt !hide
 */
class jms {}

class KeyObjectService {}

/**
 * @view
 * @opt hide
 * 
 * @match context KeyObjectService
 * @opt !hide
 */
class keyObjectService {}

/**
 * @depend - notifiedBy - Scheduler
 * @navassoc - use - Sendmail
 * @depend - listenTo - MCMScuQueue
 * @navassoc - use - JMS
 */
class MCMScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context MCMScu
 * @opt !hide
 */
class mcmScu {}

/**
 * @depend - notifiedBy - Scheduler
 */
class MD5CheckService {}

/**
 * @view
 * @opt hide
 * 
 * @match context MD5CheckService
 * @opt !hide
 */
class md5CheckService {}

/**
 * @depend - notifiedBy - MPPSScp
 * @navassoc - use - HL7Send
 */
class MPPS2ORM {}

/**
 * @view
 * @opt hide
 * 
 * @match context MPPS2ORM
 * @opt !hide
 */
class mpps2ORM {}

/**
 * @depend - notifiedBy - Scheduler
 * @navassoc - use - MPPSScu
 */
class MPPSEmulator {}

/**
 * @view
 * @opt hide
 * 
 * @match context MPPSEmulator
 * @opt !hide
 */
class mppsEmulator {}

/**
 * @navassoc - use - AuditLogger
 * @navassoc - attachedTo - DcmServer
 */
class MPPSScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context MPPSScp
 * @opt !hide
 */
class mppsScp {}

/**
 * @depend - notifiedBy - MPPSScp
 * @navassoc - use - TLSConfig
 * @depend - listenTo - MPPSScuQueue
 * @navassoc - use - JMS
 */
class MPPSScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context MPPSScu
 * @opt !hide
 */
class mppsScu {}

/**
 * @navassoc - use - AuditLogger
 * @navassoc - attachedTo - DcmServer
 * @depend - notifiedBy - MPPSScp
 */
class MWLFindScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context MWLFindScp
 * @opt !hide
 */
class mwlFindScp {}

/**
 * @navassoc - use - TLSConfig
 */
class MWLScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context MWLScu
 * @opt !hide
 */
class mwlScu {}

/**
 * @depend - connect - QueryRetrieveScp
 * @navassoc - use - TLSConfig
 * @depend - listenTo - MoveScuQueue
 * @navassoc - use - JMS
 */
class MoveScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context MoveScu
 * @opt !hide
 */
class moveScu {}

/**
 * @depend - notifiedBy - MPPSScp
 * @depend - listenTo - PPSExceptionMgtQueue
 * @navassoc - use - JMS
 */
class PPSExceptionMgtService {}

/**
 * @view
 * @opt hide
 * 
 * @match context PPSExceptionMgtService
 * @opt !hide
 */
class ppsExceptionMgtService {}

/**
 * @depend - notifiedBy - GPWLScp
 * @navassoc - use - TLSConfig
 * @depend - listenTo - PPSScuQueue
 * @navassoc - use - JMS
 */
class PPSScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context PPSScu
 * @opt !hide
 */
class ppsScu {}

/**
 * @navassoc - use - AE
 * @navassoc - attachedTo - DcmServer
 * @navassoc - use - TLSConfig
 * @navassoc - use - AuditLogger
 * @navassoc - use - FileSystemMgt
 * @navassoc - use - StgCmtScuScp
 * @navassoc - use - TarRetriever
 */
class QueryRetrieveScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context QueryRetrieveScp
 * @opt !hide
 */
class queryRetrieveScp {}

/**
 * @navassoc - use - AuditLogger
 * @navassoc - use - FileSystemMgt
 * @depend - notifiedBy - Scheduler
 */
class RIDService {}

/**
 * @view
 * @opt hide
 * 
 * @match context RIDService
 * @opt !hide
 */
class ridService {}

class Scheduler {}

/**
 * @view
 * @opt hide
 * 
 * @match context Scheduler
 * @opt !hide
 */
class scheduler {}

/**
 * @depend - listenTo - SendmailQueue
 * @navassoc - use - JMS
 */
class Sendmail {}

/**
 * @view
 * @opt hide
 * 
 * @match context Sendmail
 * @opt !hide
 */
class sendmail {}

/**
 * @navassoc - use - AE
 * @navassoc - attachedTo - DcmServer
 * @navassoc - use - FileSystemMgt
 * @navassoc - use - TLSConfig
 * @depend - listenTo - StgCmtScuScpQueue
 * @navassoc - use - JMS
 */
class StgCmtScuScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context StgCmtScuScp
 * @opt !hide
 */
class stgCmtScuScp {}

/**
 * @navassoc - use - AuditLogger
 * @navassoc - attachedTo - DcmServer
 * @navassoc - use - FileSystemMgt
 * @navassoc - use - MWLScu
 * @depend - notifiedBy - Scheduler
 */
class StoreScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context StoreScp
 * @opt !hide
 */
class storeScp {}

class StudyInfoService {}

/**
 * @view
 * @opt hide
 * 
 * @match context StudyInfoService
 * @opt !hide
 */
class studyInfoService {}

/**
 * @navassoc - use - AuditLogger
 * @navassoc - attachedTo - DcmServer
 */
class StudyMgtScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context StudyMgtScp
 * @opt !hide
 */
class studyMgtScp {}

/**
 * @depend - notifiedBy - StudyMgtScp
 * @navassoc - use - TLSConfig
 * @depend - listenTo - StudyMgtScuQueue
 * @navassoc - use - JMS
 */
class StudyMgtScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context StudyMgtScu
 * @opt !hide
 */
class studyMgtScu {}

/**
 * @navassoc - use - TLSConfig
 * @depend - notifiedBy - Scheduler
 */
class StudyReconciliation {}

/**
 * @view
 * @opt hide
 * 
 * @match context StudyReconciliation
 * @opt !hide
 */
class studyReconciliation {}

/**
 * @depend - notifiedBy - GPWLScp
 */
class StudyStatus {}

/**
 * @view
 * @opt hide
 * 
 * @match context StudyStatus
 * @opt !hide
 */
class studyStatus {}

/**
 * @depend - notifiedBy - Scheduler
 */
class SyncFileStatus {}

/**
 * @view
 * @opt hide
 * 
 * @match context SyncFileStatus
 * @opt !hide
 */
class syncFileStatus {}

/**
 * @navassoc - use - AuditLogger
 */
class TLSConfig {}

/**
 * @view
 * @opt hide
 * 
 * @match context TLSConfig
 * @opt !hide
 */
class tlsConfig {}

class TarRetriever {}

/**
 * @view
 * @opt hide
 * 
 * @match context TarRetriever
 * @opt !hide
 */
class tarRetriever {}

/**
 * @navassoc - use - FileSystemMgt
 * @depend - notifiedBy - Scheduler
 * @navassoc - use - StudyInfoService
 */
class WADOExtService {}

/**
 * @view
 * @opt hide
 * 
 * @match context WADOExtService
 * @opt !hide
 */
class wadoExtService {}

/**
 * @navassoc - use - FileSystemMgt
 * @depend - notifiedBy - Scheduler
 */
class WADOService {}

/**
 * @view
 * @opt hide
 * 
 * @match context WADOService
 * @opt !hide
 */
class wadoService {}

/**
 * @navassoc - use - AuditLogger
 * @navassoc - use - HL7Send
 */
class XDS_I {}

/**
 * @view
 * @opt hide
 * 
 * @match context XDS_I
 * @opt !hide
 */
class xds_I {}

class XDSService {}

/**
 * @view
 * @opt hide
 * 
 * @match context XDSService
 * @opt !hide
 */
class xdsService {}
