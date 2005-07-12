/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.web.WADOExtRequestObject;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExtendedWADOSupport {
	
public static final String CONTENT_TYPE_DICOM = "application/dicom";

private static Logger log = Logger.getLogger( ExtendedWADOService.class.getName() );

private static ObjectName fileSystemMgtName = null;
private static ObjectName studyInfoServiceName = null;

private static MBeanServer server;

private static final int BUF_LEN = 65536;

public ExtendedWADOSupport( MBeanServer mbServer ) {
	if ( server != null ) {
		server = mbServer;
	} else {
		server = MBeanServerLocator.locate();
	}
}

/**
 * Handles a WADOext request and returns a WADO response object.
 * <p>
 * </DL>
 * <DT>If the request was successfull:</DT>
 * <DD>	The WADO response object contains a File and a corresponding content type.</DD>
 * <DD>	the return code was OK and the error message <code>null</code>.</DD>
 * <DT>If the request was not successfull (not found or an error):</DT>
 * <DD>	The return code of the WADO response object is set to a http error code and an error message was set.</DD>
 * <DD>	The file of the WADO response is <code>null</code>. The content type is not specified for this case.</DD>
 * </DL> 
 * @param req The WADO request object.
 * 
 * @return The WADO response object.
 */
public WADOResponseObject getWADOObject( WADORequestObject req ) {
	String serviceType = ((String[])req.getRequestParams().get("serviceType"))[0];
	if ( "WFIND".equals(serviceType)) {
		return handleGetStudyInfo( req );
	} else {
		return new WADOStreamResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_NOT_IMPLEMENTED, "Service type not implemented! :"+serviceType );
	}
}

private WADOResponseObject handleGetStudyInfo( WADORequestObject req ) {

	Dataset ds = null;
	String ts = req.getTransferSyntax();
	String level, uid;
	if ( (uid = req.getObjectUID()) != null ) {
		level="IMAGE";
	} else if ( (uid = req.getSeriesUID()) != null ) {
		level="SERIES";
	} else if ( (uid = req.getStudyUID()) != null ) {
		level="STUDY";
	} else {
		return new WADOStreamResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_BAD_REQUEST, "Missing uid parameter! Either StudyUID,SeriesUID or ObjectUID must be set!");
	}
	if ( ts == null ) ts = UIDs.ExplicitVRLittleEndian;
	try {
        ds = (Dataset) server.invoke( studyInfoServiceName,
                "retrieveStudyInfo",
                new Object[] { level, uid },
                new String[] { String.class.getName(), String.class.getName() } );
        
        ds.setFileMetaInfo( DcmObjectFactory.getInstance().newFileMetaInfo(ds, ts) );
        return new WADODatasourceResponseObjectImpl( ds, ts, CONTENT_TYPE_DICOM, HttpServletResponse.SC_OK, null);
        
    } catch (Exception e) {
        log.error("Failed to get study information for "+level+" uid:"+uid, e);
		return new WADOStreamResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error! Cant get updated dicom object");
    }
}



/**
 * Set the name of the FileSystemMgtBean.
 * <p>
 * This bean is used to retrieve the DICOM object.
 * 
 * @param fileSystemMgtName The fileSystemMgtName to set.
 */
public void setFileSystemMgtName(ObjectName fileSystemMgtName) {
	ExtendedWADOSupport.fileSystemMgtName = fileSystemMgtName;
}

/**
 * Get the name of the FileSystemMgtBean.
 * <p>
 * This bean is used to retrieve the DICOM object.
 * 
 * @return Returns the fileSystemMgtName.
 */
public ObjectName getFileSystemMgtName() {
	return fileSystemMgtName;
}


/**
 * @return Returns the studyInfoServiceName.
 */
public ObjectName getStudyInfoServiceName() {
	return studyInfoServiceName;
}
/**
 * @param studyInfoServiceName The studyInfoServiceName to set.
 */
public void setStudyInfoServiceName(ObjectName studyInfoServiceName) {
	ExtendedWADOSupport.studyInfoServiceName = studyInfoServiceName;
}
}