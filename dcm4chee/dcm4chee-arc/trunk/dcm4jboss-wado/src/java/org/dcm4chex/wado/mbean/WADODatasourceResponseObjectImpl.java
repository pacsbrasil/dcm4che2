/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerConfigurationException;

import org.dcm4che.net.DataSource;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADODatasourceResponseObjectImpl implements WADOResponseObject {

	private DataSource datasource;
	String transferSyntax = null;
	private String contentType;
	private int returnCode;
	private String errorMessage;

	public WADODatasourceResponseObjectImpl( DataSource ds, String ts, String contentType, int retCode, String errMsg ) {
		this.datasource = ds;
		this.transferSyntax = ts;
		this.contentType = contentType;
		returnCode = retCode;
		errorMessage = errMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getFile()
	 */
	public void execute( OutputStream out ) throws TransformerConfigurationException, SAXException, IOException {
		datasource.writeTo( out, transferSyntax );
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
