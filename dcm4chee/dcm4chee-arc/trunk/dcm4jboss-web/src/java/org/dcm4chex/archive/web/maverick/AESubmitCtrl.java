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

import javax.ejb.EJBException;

import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.infohazard.maverick.ctl.ThrowawayBean2;

/**
 * @author umberto.cappellini@tiani.com
 */
public class AESubmitCtrl extends ThrowawayBean2 implements Errable
{
	private final String NEW_PARAMETER = "new";
	private final String DELETE_PARAMETER = "delete";
	private final String UPDATE_PARAMETER = "update";
	private final String CANCEL_PARAMETER = "cancel";

	private String oldtitle, title, hostName, chiperSuites;
	private String message = Errable.DEFAULT_MESSAGE;
	private String errorType = Errable.DEFAULT_TYPE;
	private String backURL = Errable.DEFAULT_BACK_URL;
	private int port;

	/**
	 * @param chiperSuites The chiperSuites to set.
	 */
	public final void setChiperSuites(String chiperSuites)
	{
		this.chiperSuites = chiperSuites;
	}

	/**
	 * @param hostName The hostName to set.
	 */
	public final void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	/**
	 * @param port The port to set.
	 */
	public final void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @param title The title to set.
	 */
	public final void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @param oldtitle The oldtitle to set.
	 */
	public final void setOldtitle(String oldtitle)
	{
		this.oldtitle = oldtitle;
	}

	private AEData getAE()
	{
		return new AEData(
			this.title,
			this.hostName,
			this.port,
			this.chiperSuites);
	}

	protected String perform() throws Exception
	{
		if (getCtx().getRequest().getParameter(CANCEL_PARAMETER) != null)
			return "success";

		if (getCtx().getRequest().getParameter(NEW_PARAMETER) != null)
		{
			try
			{
				AEData newAE = getAE();
				lookupContentManager().newAE(newAE);
				return "success";
			} catch (Throwable e)
			{
				this.errorType = e.getClass().getName();
				this.message = e.getMessage();
				this.backURL = "aeedit.m?call=new";
				return ERROR_VIEW;				
			}
		}

		if (getCtx().getRequest().getParameter(DELETE_PARAMETER) != null)
		{
			AEData newAE = getAE();
			lookupContentManager().removeAE(newAE.getTitle());
			return "success";
		}
		
		if (getCtx().getRequest().getParameter(UPDATE_PARAMETER) != null)
		{
			AEData newAE = getAE();
			try
			{
				lookupContentManager().updateAE(this.oldtitle, newAE);
				return "success";
			} catch (Throwable e)
			{
				this.errorType = e.getClass().getName();
				this.message = e.getMessage();
				this.backURL =
					"aeedit.m?title="
						+ newAE.getTitle()
						+ "&hostName="
						+ newAE.getHostName()
						+ "&port="
						+ newAE.getPort()
						+ "&cipherSuites="
						+ newAE.getCipherSuitesAsString()
						+ "&call=edit";
				return ERROR_VIEW;
			}
		}
		return ERROR_VIEW;
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

	private ContentManager lookupContentManager() throws Exception
	{
		ContentManagerHome home =
			(ContentManagerHome) EJBHomeFactory.getFactory().lookup(
				ContentManagerHome.class,
				ContentManagerHome.JNDI_NAME);
		return home.create();
	}

}
