/*
 * Created on 09.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;

/**
 * @author franz.willer 
 *
 * The MBean to manage the extended WADO service.
 * <p>
 * This class use ExtendedWADOSupport for the WADOext methods.
 */
public class ExtendedWADOService extends AbstractCacheService {

	private ExtendedWADOSupport support = new ExtendedWADOSupport( this.server );

	public ExtendedWADOService() {
	}
	
	/**
	 * Getter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @return Name of the MBean
	 */
	public String getFileSystemMgtName() {
		return support.getFileSystemMgtName().toString();
	}
	
	/**
	 * Setter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @param Name of the MBean
	 */
	public void setFileSystemMgtName( String name ) {
		try {
			ObjectName on = new ObjectName( name );
			support.setFileSystemMgtName( on );
		} catch (MalformedObjectNameException e) {
		}
	}
	
	/**
	 * Getter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @return Name of the MBean
	 */
	public String getStudyInfoServiceName() {
		return support.getStudyInfoServiceName().toString();
	}
	
	/**
	 * Setter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @param Name of the MBean
	 */
	public void setStudyInfoServiceName( String name ) {
		try {
			ObjectName on = new ObjectName( name );
			support.setStudyInfoServiceName( on );
		} catch (MalformedObjectNameException e) {
		}
	}
	
	/**
	 * Get the requested DICOM object as File packed in a WADOResponseObject.
	 * <p>
	 *  
	 * @param reqVO The request parameters packed in an value object.
	 * 
	 * @return The value object containing the retrieved object or an error.
	 */
	public WADOResponseObject getWADOObject( WADORequestObject reqVO ) {
		return support.getWADOObject( reqVO );
	}

}
