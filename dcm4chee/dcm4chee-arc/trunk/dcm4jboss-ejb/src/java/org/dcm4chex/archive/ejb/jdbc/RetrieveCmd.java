/*
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
/* 
 * File: $Source$
 * Author: gunter
 * Date: 20.07.2003
 * Time: 16:21:45
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class RetrieveCmd extends BaseCmd {
    /**
     * @author Gunter.Zeilinger@tiani.com
     * @version $Revision$
     * @since 26.08.2003
     */
    private static final String[] QRLEVEL =
        { "PATIENT", "STUDY", "SERIES", "IMAGE" };

    private static final String[] ENTITY =
        { "Patient", "Study", "Series", "Instance", "File" };

    private static final String[] SELECT_ATTRIBUTE =
        {
            "Instance.sopIuid",
            "Instance.sopCuid",
            "File.retrieveAet",
            "File.filePath",
            "File.fileTsuid",
            "File.fileMd5Field",
            "Media.filesetIuid" };

    private static final String[] FK =
        {
            "Study.patient_fk",
            "Series.study_fk",
            "Instance.series_fk",
            "File.instance_fk" };

    public static RetrieveCmd create(Dataset keys) throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel)) {
            case 0 :
                return new PatientRetrieveCmd(keys);
            case 1 :
                return new StudyRetrieveCmd(keys);
            case 2 :
                return new SeriesRetrieveCmd(keys);
            case 3 :
                return new ImageRetrieveCmd(keys);
            default :
                throw new IllegalArgumentException(
                    "QueryRetrieveLevel=" + qrLevel);
        }
    }

    protected final SqlBuilder sqlBuilder = new SqlBuilder();

    private RetrieveCmd() throws SQLException {
        sqlBuilder.setSelect(SELECT_ATTRIBUTE, SELECT_ATTRIBUTE.length);
        sqlBuilder.setFrom(ENTITY, ENTITY.length);
        sqlBuilder.setLeftJoin("Media", "File.media_fk");
        sqlBuilder.setFk(FK, FK.length);
    }

    public FileInfo[] execute() throws SQLException {
        try {
            execute(sqlBuilder.getSql());
            ArrayList result = new ArrayList();
            while (next()) {
                result.add(
                    new FileInfo(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getInt(7),
                        rs.getString(8)));
            }
            return (FileInfo[]) result.toArray(new FileInfo[result.size()]);
        } finally {
            close();
        }
    }

    static class PatientRetrieveCmd extends RetrieveCmd {
        PatientRetrieveCmd(Dataset keys) throws SQLException {
            String pid = keys.getString(Tags.PatientID);
            if (pid == null)
                throw new IllegalArgumentException("Missing PatientID");

            sqlBuilder.addWildCardMatch(
                "Patient.patientId",
                SqlBuilder.TYPE2,
                pid,
                false);
        }
    }

    static class StudyRetrieveCmd extends RetrieveCmd {
        StudyRetrieveCmd(Dataset keys) throws SQLException {
            String[] uid = keys.getStrings(Tags.StudyInstanceUID);
            if (uid.length == 0)
                throw new IllegalArgumentException("Missing StudyInstanceUID");

            sqlBuilder.addListOfUidMatch(
                "Study.studyIuid",
                SqlBuilder.TYPE1,
                uid);
        }
    }

    static class SeriesRetrieveCmd extends RetrieveCmd {
        SeriesRetrieveCmd(Dataset keys) throws SQLException {
            String[] uid = keys.getStrings(Tags.SeriesInstanceUID);
            if (uid.length == 0)
                throw new IllegalArgumentException("Missing SeriesInstanceUID");

            sqlBuilder.addListOfUidMatch(
                "Series.seriesIuid",
                SqlBuilder.TYPE1,
                uid);
        }
    }

    static class ImageRetrieveCmd extends RetrieveCmd {
        ImageRetrieveCmd(Dataset keys) throws SQLException {
            String[] uid = keys.getStrings(Tags.SOPInstanceUID);
            if (uid.length == 0)
                throw new IllegalArgumentException("Missing SOPInstanceUID");

            sqlBuilder.addListOfUidMatch(
                "Instance.sopIuid",
                SqlBuilder.TYPE1,
                uid);
        }
    }
}
