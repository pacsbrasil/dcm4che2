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
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;

/**
 */
public class RetrievePatientDatasetCmd extends BaseCmd
{

	private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

	private static final String[] SELECT_ATTRIBUTE = { "Patient.pk", "Patient.encodedAttributes" };
	private static final String[] ENTITY = { "Patient" };

	private final SqlBuilder sqlBuilder = new SqlBuilder();

	public RetrievePatientDatasetCmd(DataSource ds, String patID) throws SQLException
	{
		super(ds);
		sqlBuilder.setSelect(SELECT_ATTRIBUTE);
		sqlBuilder.setFrom(ENTITY);
		sqlBuilder.setOffset(0);
		sqlBuilder.setLimit(0);
		StudyFilterDTO filter = new StudyFilterDTO();
		filter.setPatientID(patID);
		sqlBuilder.setStudyFilterMatch(filter);
	}

	public Dataset execute() throws SQLException
	{
		try
		{
			execute(sqlBuilder.getSql());
			ArrayList result = new ArrayList();
			Dataset oldPatient = null;
			if (next())
				oldPatient = toDataset(2);

			return oldPatient;
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw e;
		}
		finally
		{
			close();
		}

		//			DTO2Dataset.updtateDataset(oldPatient,updatedPatientDTO);
	}

	private Dataset toDataset(int column) throws SQLException
	{
		return DatasetUtils.fromByteArray(rs.getBytes(column), DcmDecodeParam.EVR_LE);
	}
}
