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
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4chex.archive.ejb.interfaces.DTOFactory;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 14.01.2004
 */
public class QueryStudiesCmd extends BaseCmd {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] SELECT_ATTRIBUTE = { "Patient.pk",
            "Patient.encodedAttributes", "Study.pk", "Study.encodedAttributes",
            "Study.modalitiesInStudy", "Study.numberOfStudyRelatedSeries",
            "Study.numberOfStudyRelatedInstances", "Study.retrieveAETs",
            "Study.availability" };

    private static final String[] ENTITY = { "Patient" };

    private static final String[] LEFT_JOIN = { "Study", "Patient.pk",
            "Study.patient_fk", };

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    public QueryStudiesCmd(DataSource ds, StudyFilterDTO filter, int offset,
            int limit) throws SQLException {
        super(ds);
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setLeftJoin(LEFT_JOIN);
        sqlBuilder.addLiteralMatch("Patient.merge_fk", false, "IS NULL");
        sqlBuilder.setStudyFilterMatch(filter);
        sqlBuilder.addOrderBy("Patient.patientName", SqlBuilder.ASC);
        sqlBuilder.addOrderBy("Patient.pk", SqlBuilder.ASC);
        sqlBuilder.addOrderBy("Study.studyDateTime", SqlBuilder.ASC);
        sqlBuilder.setOffset(offset);
        sqlBuilder.setLimit(limit);
    }

    public List execute() throws SQLException {
        try {
            execute(sqlBuilder.getSql());
            ArrayList result = new ArrayList();
            PatientDTO pat = null;
            while (next()) {
                int patPk = rs.getInt(1);
                if (pat == null || pat.getPk() != patPk) {
                    byte[] patAttrs = rs.getBytes(2);

                    result.add(pat = DTOFactory.newPatientDTO(patPk,
                            DatasetUtils.fromByteArray(patAttrs,
                                    DcmDecodeParam.EVR_LE)));
                }
                byte[] styAttrs = rs.getBytes(4);
                if (styAttrs != null) {
                    pat.getStudies().add(
                            DTOFactory.newStudyDTO(rs.getInt(3), DatasetUtils
                                    .fromByteArray(styAttrs,
                                            DcmDecodeParam.EVR_LE), rs
                                    .getString(5), rs.getInt(6), rs.getInt(7),
                                    rs.getString(8), rs.getInt(9)));
                }
            }
            return result;
        } finally {
            close();
        }
    }
}