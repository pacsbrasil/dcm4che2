class FilesystemMgt {}
class DcmServer {}
/**
 * @navassoc - dispatch - DcmServer
 * @navassoc - invoke - FilesystemMgt
 */
class StoreScp {}
/**
 * @navassoc - dispatch - DcmServer
 */
class MPPSScp {}
/**
 * @navassoc - dispatch - DcmServer
 * @navassoc - notify - MPPSScp
 */
class MWLScp {}
/**
 * @navassoc - notify - MPPSScp
 */
class MPPSScu {}
/**
 * @navassoc - notify - StoreScp
 * @navassoc - notify - MPPSScp
 */
class IANScu {}
