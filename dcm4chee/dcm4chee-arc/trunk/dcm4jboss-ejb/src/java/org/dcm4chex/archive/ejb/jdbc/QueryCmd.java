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
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.ejb.EJBException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.util.DatasetUtil;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class QueryCmd extends BaseQueryCmd
{
    private static final String[] QRLEVEL =
        { "PATIENT", "STUDY", "SERIES", "IMAGE", };
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";
    private static final String WHERE = " WHERE ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String IS_NULL = "=NULL";
    private static final String UPPER = "UPPER";
    private static final String LIKE = " LIKE ";
    private static final String BETWEEN = " BETWEEN ";
    private static final String SELECT_PATIENT = "SELECT patient.pat_attrs";
    private static final String FROM_PATIENT = " FROM patient";
    private static final String FROM_PATIENT_WHERE_PRINCIPAL =
        " FROM patient, link_principal_patient, principal"
            + " WHERE patient.pk=link_principal_patient.patient_fk"
            + " AND principal.pk=link_principal_patient.principal_fk"
            + " AND principal.name='";
    private static final String SELECT_STUDY =
        "SELECT patient.pat_attrs, study.study_attrs";
    private static final String FROM_STUDY_WHERE =
        " FROM patient, study"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=link_principal_study.study_fk";
    private static final String FROM_STUDY_WHERE_PRINCIPAL =
        " FROM patient, study, link_principal_study, principal"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=link_principal_study.study_fk"
            + " AND principal.pk=link_principal_study.principal_fk"
            + " AND principal.name='";
    private static final String WHERE_MODALITY_IN_STUDY_HEAD =
        " AND (SELECT count(*) FROM series"
            + " WHERE study.pk=series.study_fk AND modality='";
    private static final String WHERE_MODALITY_IN_STUDY_TAIL = "')!=0";
    private static final String SELECT_SERIES =
        "SELECT patient.pat_attrs, study.study_attrs, series.series_attrs";
    private static final String FROM_SERIES_WHERE =
        " FROM patient, study, series"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk";
    private static final String FROM_SERIES_WHERE_PRINCIPAL =
        " FROM patient, study, series, link_principal_series, principal"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk"
            + " AND series.pk=link_principal_series.series_fk"
            + " AND principal.pk=link_principal_series.principal_fk"
            + " AND principal.name='";
    private static final String SELECT_INSTANCE =
        "SELECT patient.pat_attrs, study.study_attrs, series.series_attrs, instance.inst_attrs";
    private static final String FROM_INSTANCE_WHERE =
        " FROM patient, study, series, instance"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk"
            + " AND series.pk=instance.series_fk";
    private static final String FROM_INSTANCE_WHERE_PRINCIPAL =
        " FROM patient, study, series, instance, link_principal_series, principal"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk"
            + " AND series.pk=instance.series_fk"
            + " AND series.pk=link_principal_series.series_fk"
            + " AND principal.pk=link_principal_series.principal_fk"
            + " AND principal.name='";
    private static final String FROM_SR_WHERE =
        " FROM patient, study, series, instance LEFT JOIN code ON (code.pk=instance.srcode_fk)"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk"
            + " AND series.pk=instance.series_fk";
    private static final String FROM_SR_WHERE_PRINCIPAL =
        " FROM patient, study, series, instance LEFT JOIN code ON (code.pk=instance.srcode_fk), link_principal_series, principal"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk"
            + " AND series.pk=instance.series_fk"
            + " AND series.pk=link_principal_series.series_fk"
            + " AND principal.pk=link_principal_series.principal_fk"
            + " AND principal.name='";
    private static final String STUDIES_PER_PATIENT =
        ", (SELECT count(*) FROM study" + " WHERE patient.pk=study.patient_fk)";
    private static final String SERIES_PER_PATIENT =
        ", (SELECT count(*) FROM study, series"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk)";
    private static final String INST_PER_PATIENT =
        ", (SELECT count(*) FROM study, series, instance"
            + " WHERE patient.pk=study.patient_fk"
            + " AND study.pk=series.study_fk"
            + " AND series.pk=instance.series_fk)";
    private static final String SERIES_PER_STUDY =
        ", (SELECT count(*) FROM series" + " WHERE study.pk=series.study_fk)";
    private static final String INST_PER_STUDY =
        ", (SELECT count(*) FROM series, instance"
            + " WHERE study.pk=series.study_fk"
            + " AND series.pk=instance.series_fk)";
    private static final String INST_PER_SERIES =
        ", (SELECT count(*) FROM instance"
            + " WHERE series.pk=instance.series_fk)";

    public static QueryCmd create(Dataset keys, String principal)
        throws DcmServiceException
    {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel))
        {
            case 0 :
                return new PatientQueryCmd(keys, principal);
            case 1 :
                return new StudyQueryCmd(keys, principal);
            case 2 :
                return new SeriesQueryCmd(keys, principal);
            case 3 :
                return new ImageQueryCmd(keys, principal);
            default :
                throw new DcmServiceException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    "QueryRetrieveLevel=" + qrLevel);
        }
    }

    protected final Dataset keys;
    protected final String principal;

    protected QueryCmd(Dataset keys, String principal)
    {
        this.keys = keys;
        this.principal = principal;
        // ensure keys contains (8,0005) for use as result filter  
        if (!keys.contains(Tags.SpecificCharacterSet))
        {
            keys.putCS(Tags.SpecificCharacterSet);
        }
    }

    public void execute()
    {
        execute(buildSQL());
    }

    protected abstract String buildSQL();

    public abstract Dataset getDataset();

    static class PatientQueryCmd extends QueryCmd
    {
        PatientQueryCmd(Dataset keys, String principal)
        {
            super(keys, principal);
        }

        protected String buildSQL()
        {
            StringBuffer sql = new StringBuffer(SELECT_PATIENT);
            appendNumPerPatient(sql);
            if (principal == null)
            {
                sql.append(FROM_PATIENT);
                appendMatchPatient(sql, WHERE);
            }
            else
            {
                sql.append(FROM_PATIENT_WHERE_PRINCIPAL);
                sql.append(principal);
                sql.append('\'');
                appendMatchPatient(sql, AND);
            }
            return sql.toString();
        }

        public Dataset getDataset()
        {
            try
            {
                Dataset ds = DatasetUtil.fromByteArray(rs.getBytes(1));
                fillNumPerPatient(ds, 2);
                ds.putCS(Tags.QueryRetrieveLevel, "PATIENT");
                return ds.subSet(keys);
            }
            catch (SQLException e)
            {
                throw new EJBException(e);
            }

        }
    }

    static class StudyQueryCmd extends QueryCmd
    {
        StudyQueryCmd(Dataset keys, String principal)
        {
            super(keys, principal);
        }

        protected String buildSQL()
        {
            StringBuffer sql = new StringBuffer(SELECT_STUDY);
            appendNumPerPatient(sql);
            appendNumPerStudy(sql);
            if (principal == null)
            {
                sql.append(FROM_STUDY_WHERE);
            }
            else
            {
                sql.append(FROM_STUDY_WHERE_PRINCIPAL);
                sql.append(principal);
                sql.append('\'');
            }
            appendMatchStudy(sql);
            return sql.toString();
        }

        public Dataset getDataset()
        {
            try
            {
                int index = 1;
                Dataset ds = DatasetUtil.fromByteArray(rs.getBytes(index++));
                ds.putAll(DatasetUtil.fromByteArray(rs.getBytes(index++)));
                index = fillNumPerPatient(ds, index);
                index = fillNumPerStudy(ds, index);
                ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
                return ds.subSet(keys);
            }
            catch (SQLException e)
            {
                throw new EJBException(e);
            }
        }
    }

    static class SeriesQueryCmd extends QueryCmd
    {
        SeriesQueryCmd(Dataset keys, String principal)
        {
            super(keys, principal);
        }

        protected String buildSQL()
        {
            StringBuffer sql = new StringBuffer(SELECT_SERIES);
            appendNumPerPatient(sql);
            appendNumPerStudy(sql);
            appendNumPerSeries(sql);
            if (principal == null)
            {
                sql.append(FROM_SERIES_WHERE);
            }
            else
            {
                sql.append(FROM_SERIES_WHERE_PRINCIPAL);
                sql.append(principal);
                sql.append('\'');
            }
            appendMatchPatient(sql, AND);
            appendMatchStudy(sql);
            appendMatchSeries(sql);
            return sql.toString();
        }

        public Dataset getDataset()
        {
            try
            {
                int index = 1;
                Dataset ds = DatasetUtil.fromByteArray(rs.getBytes(index++));
                ds.putAll(DatasetUtil.fromByteArray(rs.getBytes(index++)));
                ds.putAll(DatasetUtil.fromByteArray(rs.getBytes(index++)));
                index = fillNumPerPatient(ds, index);
                index = fillNumPerStudy(ds, index);
                index = fillNumPerSeries(ds, index);
                ds.putCS(Tags.QueryRetrieveLevel, "SERIES");
                return ds.subSet(keys);
            }
            catch (SQLException e)
            {
                throw new EJBException(e);
            }
        }
    }

    static class ImageQueryCmd extends QueryCmd
    {
        ImageQueryCmd(Dataset keys, String principal)
        {
            super(keys, principal);
        }

        protected String buildSQL()
        {
            StringBuffer sql = new StringBuffer(SELECT_INSTANCE);
            appendNumPerPatient(sql);
            appendNumPerStudy(sql);
            appendNumPerSeries(sql);
            if (principal == null)
            {
                sql.append(
                    isMatchSrCode() ? FROM_INSTANCE_WHERE : FROM_SR_WHERE);
            }
            else
            {
                sql.append(
                    isMatchSrCode()
                        ? FROM_INSTANCE_WHERE_PRINCIPAL
                        : FROM_SR_WHERE_PRINCIPAL);
                sql.append(principal);
                sql.append('\'');
            }
            appendMatchPatient(sql, AND);
            appendMatchStudy(sql);
            appendMatchSeries(sql);
            appendMatchInstance(sql);
            return sql.toString();
        }

        public Dataset getDataset()
        {
            try
            {
                int index = 1;
                Dataset ds = DatasetUtil.fromByteArray(rs.getBytes(index++));
                ds.putAll(DatasetUtil.fromByteArray(rs.getBytes(index++)));
                ds.putAll(DatasetUtil.fromByteArray(rs.getBytes(index++)));
                ds.putAll(DatasetUtil.fromByteArray(rs.getBytes(index++)));
                ds.putAll(DatasetUtil.fromByteArray(rs.getBytes(index++)));
                index = fillNumPerPatient(ds, index);
                index = fillNumPerStudy(ds, index);
                index = fillNumPerSeries(ds, index);
                ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
                return ds.subSet(keys);
            }
            catch (SQLException e)
            {
                throw new EJBException(e);
            }
        }
    }

    protected void appendNumPerPatient(StringBuffer sql)
    {
        if (keys.contains(Tags.NumberOfPatientRelatedStudies))
        {
            sql.append(STUDIES_PER_PATIENT);
        }
        if (keys.contains(Tags.NumberOfPatientRelatedSeries))
        {
            sql.append(SERIES_PER_PATIENT);
        }
        if (keys.contains(Tags.NumberOfPatientRelatedInstances))
        {
            sql.append(INST_PER_PATIENT);
        }
    }

    protected int fillNumPerPatient(Dataset ds, int index) throws SQLException
    {
        if (keys.contains(Tags.NumberOfPatientRelatedStudies))
        {
            ds.putIS(Tags.NumberOfPatientRelatedStudies, rs.getInt(index++));
        }
        if (keys.contains(Tags.NumberOfPatientRelatedSeries))
        {
            ds.putIS(Tags.NumberOfPatientRelatedSeries, rs.getInt(index++));
        }
        if (keys.contains(Tags.NumberOfPatientRelatedInstances))
        {
            ds.putIS(Tags.NumberOfPatientRelatedSeries, rs.getInt(index++));
        }
        return index;
    }

    protected void appendNumPerStudy(StringBuffer sql)
    {
        if (keys.contains(Tags.NumberOfStudyRelatedSeries))
        {
            sql.append(SERIES_PER_STUDY);
        }
        if (keys.contains(Tags.NumberOfStudyRelatedInstances))
        {
            sql.append(INST_PER_STUDY);
        }
    }

    protected int fillNumPerStudy(Dataset ds, int index) throws SQLException
    {
        if (keys.contains(Tags.NumberOfStudyRelatedSeries))
        {
            ds.putIS(Tags.NumberOfStudyRelatedSeries, rs.getInt(index++));
        }
        if (keys.contains(Tags.NumberOfStudyRelatedInstances))
        {
            ds.putIS(Tags.NumberOfStudyRelatedInstances, rs.getInt(index++));
        }
        return index;
    }

    protected void appendNumPerSeries(StringBuffer sql)
    {
        if (keys.contains(Tags.NumberOfSeriesRelatedInstances))
        {
            sql.append(INST_PER_SERIES);
        }
    }

    protected int fillNumPerSeries(Dataset ds, int index) throws SQLException
    {
        if (keys.contains(Tags.NumberOfSeriesRelatedInstances))
        {
            ds.putIS(Tags.NumberOfSeriesRelatedInstances, rs.getInt(index++));
        }
        return index;
    }

    protected boolean isMatchSrCode()
    {
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        return code != null
            && (code.vm(Tags.CodeValue) > 0
                || code.vm(Tags.CodingSchemeDesignator) > 0);
    }

    protected void appendMatchPatient(StringBuffer sql, String where)
    {
        String token = where;
        if (appendMatchString(sql,
            token,
            "patient.pat_id",
            keys.getStrings(Tags.PatientID),
            false))
        {
            token = AND;
        }
        appendMatchString(
            sql,
            token,
            "patient.pat_name",
            keys.getStrings(Tags.PatientName),
            true);
    }

    protected void appendMatchStudy(StringBuffer sql)
    {
        appendMatchString(
            sql,
            AND,
            "study.study_iuid",
            keys.getStrings(Tags.StudyInstanceUID),
            false);
        appendMatchString(
            sql,
            AND,
            "study.study_id",
            keys.getStrings(Tags.StudyID),
            false);
        appendMatchDateRange(
            sql,
            AND,
            "study.study_datetime",
            keys.getDateRange(Tags.StudyDate, Tags.StudyTime));
        appendMatchString(
            sql,
            AND,
            "study.accession_no",
            keys.getStrings(Tags.AccessionNumber),
            false);
        appendMatchString(
            sql,
            AND,
            "study.ref_physician",
            keys.getStrings(Tags.ReferringPhysicianName),
            true);
        String modality = keys.getString(Tags.ModalitiesInStudy);
        if (modality != null)
        {
            sql.append(WHERE_MODALITY_IN_STUDY_HEAD);
            sql.append(modality);
            sql.append(WHERE_MODALITY_IN_STUDY_TAIL);
        }
    }

    protected void appendMatchSeries(StringBuffer sql)
    {
        appendMatchString(
            sql,
            AND,
            "series.series_iuid",
            keys.getStrings(Tags.SeriesInstanceUID),
            false);
        appendMatchString(
            sql,
            AND,
            "series.series_no",
            keys.getStrings(Tags.SeriesNumber),
            false);
        appendMatchString(
            sql,
            AND,
            "series.modality",
            keys.getStrings(Tags.Modality),
            false);
        appendMatchDateRange(
            sql,
            AND,
            "series.pps_start",
            keys.getDateRange(Tags.PPSStartDate, Tags.PPSStartTime));
    }

    protected void appendMatchInstance(StringBuffer sql)
    {
        appendMatchString(
            sql,
            AND,
            "instance.sop_iuid",
            keys.getStrings(Tags.SOPInstanceUID),
            false);
        appendMatchString(
            sql,
            AND,
            "instance.sop_cuid",
            keys.getStrings(Tags.SOPClassUID),
            false);
        appendMatchString(
            sql,
            AND,
            "instance.inst_no",
            keys.getStrings(Tags.InstanceNumber),
            false);
        appendMatchString(
            sql,
            AND,
            "instance.sr_complete",
            keys.getStrings(Tags.CompletionFlag),
            false);
        appendMatchString(
            sql,
            AND,
            "instance.sr_verified",
            keys.getStrings(Tags.VerificationFlag),
            false);
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        if (code != null)
        {
            appendMatchString(
                sql,
                AND,
                "code.code_value",
                code.getStrings(Tags.CodeValue),
                false);
            appendMatchString(
                sql,
                AND,
                "code.code_designator",
                code.getStrings(Tags.CodingSchemeDesignator),
                false);
        }
    }

    private boolean appendMatchString(
        StringBuffer sql,
        String prefix,
        String column,
        String[] values,
        boolean ignoreCase)
    {
        if (values == null || values.length == 0)
        {
            return false;
        }
        sql.append(prefix);
        sql.append('(');
        sql.append(column);
        sql.append(IS_NULL);
        for (int i = 0; i < values.length; ++i)
        {
            appendPrefixUpperValue(sql, OR, column, ignoreCase);
            appendPrefixUpperValue(sql, LIKE, toPattern(values[i]), ignoreCase);
        }
        sql.append(')');
        return true;
    }

    private String toPattern(String value)
    {
        char[] a = value.toCharArray();
        StringBuffer pattern = new StringBuffer(a.length + 1);
        char c;
        for (int i = 0; i < a.length; i++)
        {
            switch (c = a[i])
            {
                case '?' :
                    c = '_';
                    break;
                case '*' :
                    c = '%';
                    break;
                case '\'' :
                    pattern.append('\'');
                    break;
                case '_' :
                case '%' :
                    pattern.append('\\');
                    break;
            }
            pattern.append(c);
        }
        return pattern.toString();
    }

    private void appendPrefixUpperValue(
        StringBuffer sql,
        String prefix,
        String value,
        boolean ignoreCase)
    {
        sql.append(prefix);
        if (ignoreCase)
        {
            sql.append(UPPER);
            sql.append('(');
        }
        sql.append(value);
        if (ignoreCase)
        {
            sql.append(')');
        }
    }

    private boolean appendMatchDateRange(
        StringBuffer sql,
        String prefix,
        String column,
        java.util.Date[] range)
    {
        if (range == null)
        {
            return false;
        }
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        sql.append(prefix);
        sql.append('(');
        sql.append(column);
        sql.append(IS_NULL);
        sql.append(OR);
        sql.append(column);
        if (range[0] == null)
        {
            sql.append("<=");
            sql.append(df.format(range[1]));
        }
        else if (range[1] == null)
        {
            sql.append(">=");
            sql.append(df.format(range[0]));
        }
        else
        {
            sql.append(BETWEEN);
            sql.append(df.format(range[0]));
            sql.append(AND);
            sql.append(df.format(range[1]));
        }
        sql.append(')');
        return false;
    }

}
