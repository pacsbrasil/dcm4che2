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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class QueryCmd extends BaseCmd {
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private static final String[] QRLEVEL =
        { "PATIENT", "STUDY", "SERIES", "IMAGE" };

    private static final String[] ENTITY =
        { "Patient", "Study", "Series", "Instance" };

    private static final String[] SELECT_ATTRIBUTE =
        {
            "Patient.encodedAttributes",
            "Study.encodedAttributes",
            "Series.encodedAttributes",
            "Instance.encodedAttributes" };

    private static final String[] FK =
        { "Study.patient_fk", "Series.study_fk", "Instance.series_fk" };

    public static QueryCmd create(DataSource ds, Dataset keys) throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel)) {
            case 0 :
                return new PatientQueryCmd(ds, keys);
            case 1 :
                return new StudyQueryCmd(ds, keys);
            case 2 :
                return new SeriesQueryCmd(ds, keys);
            case 3 :
                return new ImageQueryCmd(ds, keys);
            default :
                throw new IllegalArgumentException(
                    "QueryRetrieveLevel=" + qrLevel);
        }
    }

    protected final Dataset keys;
    protected final SqlBuilder sqlBuilder = new SqlBuilder();

    protected QueryCmd(DataSource ds, Dataset keys) throws SQLException {
        super(ds);
        this.keys = keys;
        // ensure keys contains (8,0005) for use as result filter  
        if (!keys.contains(Tags.SpecificCharacterSet)) {
            keys.putCS(Tags.SpecificCharacterSet);
        }
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    protected void addPatientMatch() {
        sqlBuilder.addWildCardMatch(
            "Patient.patientId",
            SqlBuilder.TYPE2,
            keys.getString(Tags.PatientID),
            false);
        sqlBuilder.addWildCardMatch(
            "Patient.patientName",
            SqlBuilder.TYPE2,
            keys.getString(Tags.PatientName),
            true);
        sqlBuilder.addRangeMatch(
            "Patient.patientBirthDate",
            SqlBuilder.TYPE2,
            keys.getDateTimeRange(
                Tags.PatientBirthDate,
                Tags.PatientBirthTime));
        sqlBuilder.addWildCardMatch(
            "Patient.patientSex",
            SqlBuilder.TYPE2,
            keys.getString(Tags.PatientSex),
            false);
    }

    protected void addStudyMatch() {
        sqlBuilder.addListOfUidMatch(
            "Study.studyIuid",
            SqlBuilder.TYPE1,
            keys.getStrings(Tags.StudyInstanceUID));
        sqlBuilder.addWildCardMatch(
            "Study.studyId",
            SqlBuilder.TYPE2,
            keys.getString(Tags.StudyID),
            false);
        sqlBuilder.addRangeMatch(
            "Study.studyDateTime",
            SqlBuilder.TYPE2,
            keys.getDateTimeRange(Tags.StudyDate, Tags.StudyTime));
        sqlBuilder.addWildCardMatch(
            "Study.accessionNumber",
            SqlBuilder.TYPE2,
            keys.getString(Tags.AccessionNumber),
            false);
        sqlBuilder.addWildCardMatch(
            "Study.referringPhysicianName",
            SqlBuilder.TYPE2,
            keys.getString(Tags.ReferringPhysicianName),
            true);
        sqlBuilder.addModalitiesInStudyMatch(
            keys.getString(Tags.ModalitiesInStudy));
    }

    protected void addSeriesMatch() {
        sqlBuilder.addListOfUidMatch(
            "Series.seriesIuid",
            SqlBuilder.TYPE1,
            keys.getStrings(Tags.SeriesInstanceUID));
        sqlBuilder.addWildCardMatch(
            "Series.seriesNumber",
            SqlBuilder.TYPE2,
            keys.getString(Tags.SeriesNumber),
            false);
        sqlBuilder.addWildCardMatch(
            "Series.modality",
            SqlBuilder.TYPE1,
            keys.getString(Tags.Modality),
            false);
        sqlBuilder.addRangeMatch(
            "Series.ppsStartDateTime",
            SqlBuilder.TYPE2,
            keys.getDateRange(Tags.PPSStartDate, Tags.PPSStartTime));
    }

    protected void addInstanceMatch() {
        sqlBuilder.addListOfUidMatch(
            "Instance.sopIuid",
            SqlBuilder.TYPE1,
            keys.getStrings(Tags.SOPInstanceUID));
        sqlBuilder.addListOfUidMatch(
            "Instance.sopCuid",
            SqlBuilder.TYPE1,
            keys.getStrings(Tags.SOPClassUID));
        sqlBuilder.addWildCardMatch(
            "Instance.instanceNumber",
            SqlBuilder.TYPE2,
            keys.getString(Tags.InstanceNumber),
            false);
        sqlBuilder.addWildCardMatch(
            "Instance.srCompletionFlag",
            SqlBuilder.TYPE2,
            keys.getString(Tags.CompletionFlag),
            false);
        sqlBuilder.addWildCardMatch(
            "Instance.srVerificationFlag",
            SqlBuilder.TYPE2,
            keys.getString(Tags.VerificationFlag),
            false);
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        if (code != null) {
            sqlBuilder.addWildCardMatch(
                "Code.codeValue",
                SqlBuilder.TYPE2,
                code.getString(Tags.CodeValue),
                false);
            sqlBuilder.addWildCardMatch(
                "Code.codingSchemeDesignator",
                SqlBuilder.TYPE2,
                code.getString(Tags.CodingSchemeDesignator),
                false);
            sqlBuilder.addWildCardMatch(
                "Code.codingSchemeVersion",
                SqlBuilder.TYPE2,
                code.getString(Tags.CodingSchemeVersion),
                false);
        }
    }

    public Dataset getDataset() throws SQLException, IOException {
        Dataset ds = dof.newDataset();
        fillDataset(ds);
        adjustDataset(ds, keys);
        return ds;
    }

    private void adjustDataset(Dataset ds, Dataset keys) {
        for (Iterator it = keys.iterator(); it.hasNext();) {
            DcmElement key = (DcmElement) it.next();
            final int tag = key.tag();            
            if (tag == Tags.SpecificCharacterSet)
                continue;
            
            final int vr = key.vr();
            DcmElement el = ds.get(tag);
            if (el == null) {
                ds.putXX(tag, vr);
                continue;
            }
            if (vr == VRs.SQ) {
                Dataset keyItem = key.getItem();
                if (keyItem != null) {
                    for (int i = 0, n = el.vm(); i < n; ++i) {
                        adjustDataset(el.addNewItem(), keyItem);
                    }
                }
            }
        }
    }

    protected abstract void fillDataset(Dataset ds) throws SQLException, IOException;

    static class PatientQueryCmd extends QueryCmd {
        PatientQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
            sqlBuilder.setSelect(SELECT_ATTRIBUTE, 1);
            sqlBuilder.setFrom(ENTITY, 1);
            addPatientMatch();
        }

        protected void fillDataset(Dataset ds) throws IOException, SQLException {
            ds.putAll(toDataset(rs.getBytes(1)).subSet(keys));
            ds.putCS(Tags.QueryRetrieveLevel, "PATIENT");
        }
    }

    static class StudyQueryCmd extends QueryCmd {
        StudyQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
            sqlBuilder.setSelect(SELECT_ATTRIBUTE, 2);
            sqlBuilder.setFrom(ENTITY, 2);
            sqlBuilder.setFk(FK, 1);
            addPatientMatch();
            addStudyMatch();
        }

        protected void fillDataset(Dataset ds) throws IOException, SQLException {
            ds.putAll(toDataset(rs.getBytes(1)).subSet(keys));
            ds.putAll(toDataset(rs.getBytes(2)).subSet(keys));
            ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
        }
    }

    static class SeriesQueryCmd extends QueryCmd {
        SeriesQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
            sqlBuilder.setSelect(SELECT_ATTRIBUTE, 3);
            sqlBuilder.setFrom(ENTITY, 3);
            sqlBuilder.setFk(FK, 2);
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
        }

        protected void fillDataset(Dataset ds) throws IOException, SQLException {
            ds.putAll(toDataset(rs.getBytes(1)).subSet(keys));
            ds.putAll(toDataset(rs.getBytes(2)).subSet(keys));
            ds.putAll(toDataset(rs.getBytes(3)).subSet(keys));
            ds.putCS(Tags.QueryRetrieveLevel, "SERIES");
        }
    }

    static class ImageQueryCmd extends QueryCmd {
        ImageQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
            sqlBuilder.setSelect(SELECT_ATTRIBUTE, 4);
            sqlBuilder.setFrom(ENTITY, 4);
            if (isMatchSrCode()) {
                sqlBuilder.setLeftJoin("Code", "Instance.srcode_fk");
            }
            sqlBuilder.setFk(FK, 3);
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
            addInstanceMatch();
        }

        protected void fillDataset(Dataset ds) throws IOException, SQLException {
            ds.putAll(toDataset(rs.getBytes(1)).subSet(keys));
            ds.putAll(toDataset(rs.getBytes(2)).subSet(keys));
            ds.putAll(toDataset(rs.getBytes(3)).subSet(keys));
            ds.putAll(toDataset(rs.getBytes(4)).subSet(keys));
            ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        }
    }

    protected boolean isMatchSrCode() {
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        return code != null
            && (code.vm(Tags.CodeValue) > 0
                || code.vm(Tags.CodingSchemeDesignator) > 0);
    }

    private static Dataset toDataset(byte[] data) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Dataset ds = dof.newDataset();
        ds.readDataset(bin, DcmDecodeParam.IVR_LE, -1);
        return ds;
    }
}
