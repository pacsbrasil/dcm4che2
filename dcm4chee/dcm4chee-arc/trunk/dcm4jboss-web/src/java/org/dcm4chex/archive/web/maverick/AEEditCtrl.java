/* $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.infohazard.maverick.ctl.ThrowawayBean2;

/**
 * @author umberto.cappellini@tiani.com
 */
public class AEEditCtrl extends ThrowawayBean2 implements Errable
{
	private final String CALL_PARAMETER="call";
	private final String CALL_PARAMETER_VALUE_EDIT="edit";
	private final String CALL_PARAMETER_VALUE_NEW="new";
	
	private String message=Errable.DEFAULT_MESSAGE;
	private String errorType=Errable.DEFAULT_TYPE;
	private String backURL =Errable.DEFAULT_BACK_URL;
	
	
	public AEData getAE()
	{
		int port = 0;
		try
		{
			port = Integer.parseInt(getValue("port"));
		} catch (Throwable e)
		{
			//do nothing
		};
		return new AEData( 
			getValue("title"),
			getValue("hostName"),
			port,
			getValue("chiperSuites")); 
	}

	private String getValue(String name)
	{
		return getCtx().getRequest().getParameter(name) != null
			? getCtx().getRequest().getParameter(name)
			: "";
	}

	protected String perform() throws Exception 
	{
		String call =getCtx().getRequest().getParameter(CALL_PARAMETER);
		if (call != null)
		{
			this.errorType = "";
			this.message = "";
			this.backURL= "";
			
			if (call.equals(CALL_PARAMETER_VALUE_EDIT))
				return "edit";
			else if (call.equals(CALL_PARAMETER_VALUE_NEW))
				return "new";
			else
			{
				this.errorType = "Unrecognized Value of Parameter \'call\'";
				this.message = call;
				this.backURL= "default.jsp";
				return ERROR_VIEW;
			}
		}
		else
		{
			this.errorType = "Missing Parameter";
			this.message = CALL_PARAMETER;
			this.backURL= "default.jsp";
			return ERROR_VIEW;
		}
	}
	
	/**
	 * @return Returns the backURL.
	 */
	public final String getBackURL()
	{
		return backURL;
	}

	/**
	 * @return Returns the errorType.
	 */
	public final String getErrorType()
	{
		return errorType;
	}

	/**
	 * @return Returns the message.
	 */
	public final String getMessage()
	{
		return message;
	}

}


