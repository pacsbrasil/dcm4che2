/*
 * Created on Feb 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.dcm4chex.archive.web.maverick;

/**
 * @author umberto.cappellini@tiani.com
 * Created: Feb 24, 2004 - 11:16:30 AM
 * Module: dcm4jboss-web
 * 
 */
public interface Errable
{
	public static String ERROR_VIEW = "error";

	public static String DEFAULT_TYPE = "Unknown Error";
	public static String DEFAULT_MESSAGE = "An Unrecognized Error Has Been Thrown";
	public static String DEFAULT_BACK_URL = "default.jsp";
	
	public String getMessage();
	public String getErrorType();
	public String getBackURL();
}
