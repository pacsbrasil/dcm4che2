/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.File;

import org.dcm4chex.wado.common.WADOResponseObject;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOResponseObjectImpl implements WADOResponseObject {

	private File file;
	private String contentType;
	private int returnCode;
	private String errorMessage;

	public WADOResponseObjectImpl( File pFile, String contentType, int retCode, String errMsg ) {
		file = pFile;
		this.contentType = contentType;
		returnCode = retCode;
		errorMessage = errMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getFile()
	 */
	public File getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getContentType()
	 */
	public String getContentType() {
		return contentType;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getReturnCode()
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getErrorMessage()
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
