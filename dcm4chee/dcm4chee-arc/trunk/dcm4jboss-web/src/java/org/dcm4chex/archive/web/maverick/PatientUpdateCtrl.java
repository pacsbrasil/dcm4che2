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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.infohazard.maverick.ctl.ThrowawayBean2;
import org.infohazard.maverick.ctl.ThrowawayFormBeanUser;

/**
 * @author umberto.cappellini@tiani.com
 */
public class PatientUpdateCtrl extends ThrowawayBean2
{
	private int pk;
	private String patientName=null;
	private String patientSex=null;
	private String patientBirthDay;
	private String patientBirthMonth;
	private String patientBirthYear;
	
	protected String perform() throws Exception
	{
		executeUpdate();
		return SUCCESS;
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
			PatientDTO to_update = FolderForm.getFolderForm(getCtx().getRequest()).getPatientByPk(pk);
			to_update.setPatientSex(patientSex);
			to_update.setPatientName(patientName);
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(patientBirthDay));
			c.set(Calendar.MONTH, Integer.parseInt(patientBirthMonth)-1);
			c.set(Calendar.YEAR, Integer.parseInt(patientBirthYear));			
			
			to_update.setPatientBirthDate( new SimpleDateFormat(PatientDTO.DATE_FORMAT).format(c.getTime()));
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
	/**
	 * @param patientBirthDay The patientBirthDay to set.
	 */
	public final void setPatientBirthDay(String patientBirthDay)
	{
		this.patientBirthDay = patientBirthDay;
	}

	/**
	 * @param patientBirthMonth The patientBirthMonth to set.
	 */
	public final void setPatientBirthMonth(String patientBirthMonth)
	{
		this.patientBirthMonth = patientBirthMonth;
	}

	/**
	 * @param patientBirthYear The patientBirthYear to set.
	 */
	public final void setPatientBirthYear(String patientBirthYear)
	{
		this.patientBirthYear = patientBirthYear;
	}

	/**
	 * @param patientName The patientName to set.
	 */
	public final void setPatientName(String patientName)
	{
		this.patientName = patientName;
	}

	/**
	 * @param patientSex The patientSex to set.
	 */
	public final void setPatientSex(String patientSex)
	{
		this.patientSex = patientSex;
	}

	/**
	 * @param pk The pk to set.
	 */
	public final void setPk(int pk)
	{
		this.pk = pk;
	}

}
