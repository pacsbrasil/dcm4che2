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


import java.util.Vector;

import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdate;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdateHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.infohazard.maverick.ctl.ThrowawayBean2;

/**
 * @author umberto.cappellini@tiani.com
 */
public class PatientMergeCtrl extends ThrowawayBean2
{
	private int pk;
	private int[] to_be_merged;

	private String merge = null;
	private String cancel = null;
	
	protected String perform() throws Exception
	{
		if (merge!=null)
			executeMerge();
		return SUCCESS;
	}

	private void executeMerge() throws Exception
	{
		PatientDTO[] priors;
		Vector temp = new Vector();
		PatientDTO principal;

		principal = getPatient(pk);
		for (int i=0; i<to_be_merged.length;i++)
		{
			if (to_be_merged[i] != pk)
				temp.add(getPatient(to_be_merged[i]));
		}
		priors = new PatientDTO[temp.size()];
		temp.copyInto(priors);

		lookupPatientUpdate().mergePatient(principal,priors);
	} 	
	
	private PatientUpdate lookupPatientUpdate() throws Exception
	{
		PatientUpdateHome home =
			(PatientUpdateHome) EJBHomeFactory.getFactory().lookup(
					PatientUpdateHome.class,
					PatientUpdateHome.JNDI_NAME);
		return home.create();
	}

	public final void setPk(int pk)
	{
		this.pk = pk;
	}

	public final void setToBeMerged(int[] tbm)
	{
		this.to_be_merged = tbm;
	}
	
	public final void setMerge(String merge)
	{
		this.merge= merge;
	}

	public PatientDTO getPatient(int ppk) 
	{
		return FolderForm.getFolderForm(getCtx().getRequest()).getPatientByPk(ppk);
	}
	
}
