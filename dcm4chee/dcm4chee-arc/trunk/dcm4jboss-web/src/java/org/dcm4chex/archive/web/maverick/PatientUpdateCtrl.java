/*
 * $Id$ Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick;


import java.rmi.RemoteException;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.infohazard.maverick.ctl.ThrowawayFormBeanUser;

/**
 * @author umberto.cappellini@tiani.com
 */
public class PatientUpdateCtrl extends ThrowawayFormBeanUser
{
	static final String PATIENT_PK_PARAMETER = "pk";
	static final String PATIENT_UPDATE = "patient_update";	
	
	protected Object makeFormBean()
	{
		FolderForm form =	(FolderForm) getCtx().getRequest().getSession().getAttribute(FolderForm.FOLDER_ATTRNAME);
		int pk =  getCtx().getRequest().getParameter(PATIENT_PK_PARAMETER)!=null ? Integer.parseInt(getCtx().getRequest().getParameter(PATIENT_PK_PARAMETER)):-1;
		return form.getPatientByPk(pk);
	}

	protected String perform() throws Exception
	{
		executeUpdate();
		return PATIENT_UPDATE;
	}

	private ContentEdit lookupContentEdit() throws Exception
	{
		ContentEditHome home =
			(ContentEditHome) EJBHomeFactory.getFactory().lookup(
					ContentEditHome.class,
					ContentEditHome.JNDI_NAME);
		return home.create();
	}
	
	private void executeUpdate()
	{
		try
		{
			PatientDTO to_update = (PatientDTO)getForm();
			
			//updating data model
			ContentEdit ce = lookupContentEdit();
			ce.updatePatient(to_update);
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
