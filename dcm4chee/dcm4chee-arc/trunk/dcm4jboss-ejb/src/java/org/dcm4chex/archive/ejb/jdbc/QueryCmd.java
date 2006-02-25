/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.DatasetUtils;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision$ $Date$
 */
public abstract class QueryCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] AVAILABILITY = { "ONLINE", "NEARLINE",
            "OFFLINE", "UNAVAILABLE"};

    private static final String SR_CODE = "sr_code";

    private static final String[] SERIES_REQUEST_LEFT_JOIN = {
        "SeriesRequest", null, "Series.pk", "SeriesRequest.series_fk"};
    
    public static QueryCmd create(Dataset keys, boolean filterResult)
            throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        if ("IMAGE".equals(qrLevel))
        	return createInstanceQuery(keys, filterResult);
        if ("SERIES".equals(qrLevel))
        	return createSeriesQuery(keys, filterResult);
        if ("STUDY".equals(qrLevel))
            return createStudyQuery(keys, filterResult);
        if ("PATIENT".equals(qrLevel))
        	return createPatientQuery(keys, filterResult);
        throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
    }

	public static PatientQueryCmd createPatientQuery(Dataset keys, 
			boolean filterResult) throws SQLException {
		final PatientQueryCmd cmd = new PatientQueryCmd(keys, filterResult);
		cmd.init();
		return cmd;
	}

	private static StudyQueryCmd createStudyQuery(Dataset keys,
			boolean filterResult) throws SQLException {
		final StudyQueryCmd cmd = new StudyQueryCmd(keys, filterResult);
		cmd.init();
		return cmd;
	}

	public static SeriesQueryCmd createSeriesQuery(Dataset keys,
			boolean filterResult) throws SQLException {
		final SeriesQueryCmd cmd = new SeriesQueryCmd(keys, filterResult);
		cmd.init();
		return cmd;
	}

	public static ImageQueryCmd createInstanceQuery(Dataset keys,
			boolean filterResult) throws SQLException {
		final ImageQueryCmd cmd = new ImageQueryCmd(keys, filterResult);
		cmd.init();
		return cmd;
	}

    protected final Dataset keys;

    protected final SqlBuilder sqlBuilder = new SqlBuilder();

	private final boolean filterResult;
	
    protected QueryCmd(Dataset keys, boolean filterResult)
    		throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
        this.keys = keys;
        this.filterResult = filterResult;
    }

    protected void init() {
        sqlBuilder.setSelect(getSelectAttributes());
        sqlBuilder.setFrom(getTables());
        sqlBuilder.setLeftJoin(getLeftJoin());
        sqlBuilder.setRelations(getRelations());
    }

    protected abstract String[] getSelectAttributes();

    protected abstract String[] getTables();

    protected String[] getLeftJoin() {
        return null;
    }

    protected String[] getRelations() {
        return null;
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    protected void addPatientMatch() {
		sqlBuilder.addWildCardMatch(null, "Patient.patientId", SqlBuilder.TYPE2,
                keys.getString(Tags.PatientID), false);
        PersonName pn = keys.getPersonName(Tags.PatientName);
        if (pn != null) {
            sqlBuilder.addWildCardMatch(null,
                    "Patient.patientFamilyName",
                    SqlBuilder.TYPE2,
                    pn.get(PersonName.FAMILY),
                    true);
            sqlBuilder.addWildCardMatch(null,
                    "Patient.patientGivenName",
                    SqlBuilder.TYPE2,
                    pn.get(PersonName.GIVEN),
                    true);
            PersonName ipn = pn.getIdeographic();
            if (ipn != null) {
                sqlBuilder.addWildCardMatch(null,
                        "Patient.patientIdeographicFamilyName",
                        SqlBuilder.TYPE2,
                        ipn.get(PersonName.FAMILY),
                        false);
                sqlBuilder.addWildCardMatch(null,
                        "Patient.patientIdeographicGivenName",
                        SqlBuilder.TYPE2,
                        ipn.get(PersonName.GIVEN),
                        false);
            }
            PersonName ppn = pn.getPhonetic();
            if (ppn != null) {
                sqlBuilder.addWildCardMatch(null,
                        "Patient.patientPhoneticFamilyName",
                        SqlBuilder.TYPE2,
                        ppn.get(PersonName.FAMILY),
                        false);
                sqlBuilder.addWildCardMatch(null,
                        "Patient.patientPhoneticGivenName",
                        SqlBuilder.TYPE2,
                        ppn.get(PersonName.GIVEN),
                        false);
            }
        }        
        sqlBuilder.addRangeMatch(null, "Patient.patientBirthDate",
                SqlBuilder.TYPE2,
                keys.getDateTimeRange(Tags.PatientBirthDate,
                        Tags.PatientBirthTime));
        sqlBuilder.addWildCardMatch(null, "Patient.patientSex",
                SqlBuilder.TYPE2,
                keys.getString(Tags.PatientSex),
                false);
    }

    protected void addStudyMatch() {
        sqlBuilder.addListOfUidMatch(null, "Study.studyIuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.StudyInstanceUID));
        sqlBuilder.addWildCardMatch(null, "Study.studyId", SqlBuilder.TYPE2,
                keys.getString(Tags.StudyID), false);
        sqlBuilder.addRangeMatch(null, "Study.studyDateTime", SqlBuilder.TYPE2,
                keys.getDateTimeRange(Tags.StudyDate, Tags.StudyTime));
        sqlBuilder.addWildCardMatch(null, "Study.accessionNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.AccessionNumber),
                false);
        PersonName pn = keys.getPersonName(Tags.ReferringPhysicianName);
        if (pn != null) {
            sqlBuilder.addWildCardMatch(null,
                    "Study.referringPhysicianFamilyName",
                    SqlBuilder.TYPE2,
                    pn.get(PersonName.FAMILY),
                    true);
            sqlBuilder.addWildCardMatch(null,
                    "Study.referringPhysicianGivenName",
                    SqlBuilder.TYPE2,
                    pn.get(PersonName.GIVEN),
                    true);
            PersonName ipn = pn.getIdeographic();
            if (ipn != null) {
                sqlBuilder.addWildCardMatch(null,
                        "Study.referringPhysicianIdeographicFamilyName",
                        SqlBuilder.TYPE2,
                        ipn.get(PersonName.FAMILY),
                        false);
                sqlBuilder.addWildCardMatch(null,
                        "Study.referringPhysicianIdeographicGivenName",
                        SqlBuilder.TYPE2,
                        ipn.get(PersonName.GIVEN),
                        false);
            }
            PersonName ppn = pn.getPhonetic();
            if (ppn != null) {
                sqlBuilder.addWildCardMatch(null,
                        "Study.referringPhysicianPhoneticFamilyName",
                        SqlBuilder.TYPE2,
                        ppn.get(PersonName.FAMILY),
                        false);
                sqlBuilder.addWildCardMatch(null,
                        "Study.referringPhysicianPhoneticGivenName",
                        SqlBuilder.TYPE2,
                        ppn.get(PersonName.GIVEN),
                        false);
            }
        }        
        sqlBuilder.addWildCardMatch(null, "Study.studyDescription",
                SqlBuilder.TYPE2,
                keys.getString(Tags.StudyDescription),
                true);
        sqlBuilder.addSingleValueMatch(null, "Study.studyStatusId",
        		SqlBuilder.TYPE2,
        		keys.getString(Tags.StudyStatusID));
        sqlBuilder.addModalitiesInStudyMatch(null,
                keys.getString(Tags.ModalitiesInStudy));
    }

    protected void addSeriesMatch() {
        sqlBuilder.addListOfUidMatch(null, "Series.seriesIuid",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.SeriesInstanceUID));
        sqlBuilder.addWildCardMatch(null, "Series.seriesNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.SeriesNumber),
                false);
        sqlBuilder.addWildCardMatch(null, "Series.modality", SqlBuilder.TYPE1,
                keys.getString(Tags.Modality), false);
        sqlBuilder.addWildCardMatch(null, "Series.institutionName",
                SqlBuilder.TYPE2,
                keys.getString(Tags.InstitutionName),
                true);
        sqlBuilder.addWildCardMatch(null, "Series.institutionalDepartmentName",
                SqlBuilder.TYPE2,
                keys.getString(Tags.InstitutionalDepartmentName),
                true);
        sqlBuilder.addRangeMatch(null, "Series.ppsStartDateTime",
                SqlBuilder.TYPE2,
                keys.getDateRange(Tags.PPSStartDate, Tags.PPSStartTime));
        Dataset rqAttrs = keys.getItem(Tags.RequestAttributesSeq);
        if (rqAttrs != null) {
            sqlBuilder.addWildCardMatch(null,
                    "SeriesRequest.requestedProcedureId",
                    SqlBuilder.TYPE2,
                    rqAttrs.getString(Tags.RequestedProcedureID),
                    false);
            sqlBuilder.addWildCardMatch(null,
                    "SeriesRequest.spsId",
                    SqlBuilder.TYPE2,
                    rqAttrs.getString(Tags.SPSID),
                    false);
        }

    }

    protected void addInstanceMatch() {
        sqlBuilder.addListOfUidMatch(null, "Instance.sopIuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPInstanceUID));
        sqlBuilder.addListOfUidMatch(null, "Instance.sopCuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPClassUID));
        sqlBuilder.addWildCardMatch(null, "Instance.instanceNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.InstanceNumber),
                false);
        sqlBuilder.addRangeMatch(null, "Instance.contentDateTime", SqlBuilder.TYPE2,
                keys.getDateTimeRange(Tags.ContentDate, Tags.ContentTime));
        sqlBuilder.addSingleValueMatch(null, "Instance.srCompletionFlag",
                SqlBuilder.TYPE2,
                keys.getString(Tags.CompletionFlag));
        sqlBuilder.addSingleValueMatch(null, "Instance.srVerificationFlag",
                SqlBuilder.TYPE2,
                keys.getString(Tags.VerificationFlag));
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        if (code != null) {
            sqlBuilder.addSingleValueMatch(SR_CODE, "Code.codeValue",
                    SqlBuilder.TYPE2,
                    code.getString(Tags.CodeValue));
            sqlBuilder.addSingleValueMatch(SR_CODE, "Code.codingSchemeDesignator",
                    SqlBuilder.TYPE2,
                    code.getString(Tags.CodingSchemeDesignator));
        }
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = dof.newDataset();
        fillDataset(ds);
        adjustDataset(ds, keys);
        if (!filterResult) return ds;
        keys.putCS(Tags.SpecificCharacterSet);
        keys.putAE(Tags.RetrieveAET);
        keys.putSH(Tags.StorageMediaFileSetID);
        keys.putUI(Tags.StorageMediaFileSetUID);
        keys.putCS(Tags.InstanceAvailability);
        return ds.subSet(keys);
    }

    static void adjustDataset(Dataset ds, Dataset keys) {
        for (Iterator it = keys.iterator(); it.hasNext();) {
            DcmElement key = (DcmElement) it.next();
            final int tag = key.tag();
            if (tag == Tags.SpecificCharacterSet) continue;

            final int vr = key.vr();
            DcmElement el = ds.get(tag);
            if (el == null) {
                el = ds.putXX(tag, vr);
            }
            if (vr == VRs.SQ) {
                Dataset keyItem = key.getItem();
                if (keyItem != null) {
                	if (el.isEmpty()) el.addNewItem();
                    for (int i = 0, n = el.vm(); i < n; ++i) {
                        adjustDataset(el.getItem(i), keyItem);
                    }
                }
            }
        }
    }

    protected abstract void fillDataset(Dataset ds) throws SQLException;

    protected void fillDataset(Dataset ds, int column) throws SQLException {
		DatasetUtils.fromByteArray(getBytes(column), ds);
	}

    static class PatientQueryCmd extends QueryCmd {

        PatientQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
        }

        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            ds.putCS(Tags.QueryRetrieveLevel, "PATIENT");
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes"};
        }

        protected String[] getTables() {
            return new String[] { "Patient"};
        }

    }

    static class StudyQueryCmd extends QueryCmd {

        StudyQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes",
                    "Study.encodedAttributes", "Study.modalitiesInStudy",
                    "Study.studyStatusId", "Study.numberOfStudyRelatedSeries",
                    "Study.numberOfStudyRelatedInstances",
                    "Study.filesetId", "Study.filesetIuid",                    
                    "Study.retrieveAETs", "Study.externalRetrieveAET",
                    "Study.availability"};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study"};
        }

        protected String[] getRelations() {
            return new String[] { "Patient.pk", "Study.patient_fk"};
        }

        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            fillDataset(ds, 2);
            ds.putCS(Tags.ModalitiesInStudy, StringUtils.split(rs.getString(3),
                    '\\'));
            ds.putCS(Tags.StudyStatusID, rs.getString(4));
            ds.putIS(Tags.NumberOfStudyRelatedSeries, rs.getInt(5));
            ds.putIS(Tags.NumberOfStudyRelatedInstances, rs.getInt(6));
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(7));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(8));
            DatasetUtils.putRetrieveAET(ds, rs.getString(9), rs.getString(10));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(11)]);
            ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
        }

    }

    static class SeriesQueryCmd extends QueryCmd {

        SeriesQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes",
                    "Study.encodedAttributes", "Series.encodedAttributes",
                    "Series.numberOfSeriesRelatedInstances",
                    "Series.filesetId", "Series.filesetIuid",                    
                    "Series.retrieveAETs", "Series.externalRetrieveAET",
                    "Series.availability"};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study", "Series"};
        }

        protected String[] getRelations() {
            return new String[] { "Patient.pk", "Study.patient_fk", "Study.pk",
                    "Series.study_fk"};
        }

        protected String[] getLeftJoin() {
            if  (!isMatchRequestAttributes())
                return null;
            sqlBuilder.setDistinct(true);
            return SERIES_REQUEST_LEFT_JOIN;
        }
        
        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            fillDataset(ds, 2);
            fillDataset(ds, 3);
            ds.putIS(Tags.NumberOfSeriesRelatedInstances, rs.getInt(4));
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(5));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(6));
            DatasetUtils.putRetrieveAET(ds, rs.getString(7), rs.getString(8));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(9)]);
            ds.putCS(Tags.QueryRetrieveLevel, "SERIES");
        }
    }

    static class ImageQueryCmd extends QueryCmd {

        ImageQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
            addInstanceMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes",
                    "Study.encodedAttributes", "Series.encodedAttributes",
                    "Instance.encodedAttributes", "Instance.retrieveAETs",
                    "Instance.externalRetrieveAET", "Instance.availability",
                    "Media.filesetId", "Media.filesetIuid"};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study", "Series", "Instance"};
        }

        protected String[] getLeftJoin() {
            ArrayList list = new ArrayList(12);
            if (isMatchRequestAttributes()) {
                sqlBuilder.setDistinct(true);
                list.add("SeriesRequest");
                list.add(null);
                list.add("Series.pk");
                list.add("SeriesRequest.series_fk");
            }
            if (isMatchSrCode()) {
                list.add("Code");
                list.add(SR_CODE);
                list.add("Instance.srcode_fk");
                list.add("Code.pk");
            }
            list.add("Media");
            list.add(null);
            list.add("Instance.media_fk");
            list.add("Media.pk");
            return (String[]) list.toArray(new String[list.size()]);
        }

        protected String[] getRelations() {
            return new String[] { "Patient.pk", "Study.patient_fk", "Study.pk",
                    "Series.study_fk", "Series.pk", "Instance.series_fk"};
        }

        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            fillDataset(ds, 2);
            fillDataset(ds, 3);
            fillDataset(ds, 4);
            DatasetUtils.putRetrieveAET(ds, rs.getString(5), rs.getString(6));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(7)]);
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(8));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(9));
            ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        }

    }

    protected boolean isMatchSrCode() {
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        return code != null
                && (code.vm(Tags.CodeValue) > 0 || code
                        .vm(Tags.CodingSchemeDesignator) > 0);
    }

    protected boolean isMatchRequestAttributes() {
        Dataset rqAttrs = keys.getItem(Tags.RequestAttributesSeq);
        return rqAttrs != null
                && (rqAttrs.vm(Tags.RequestedProcedureID) > 0
                        || rqAttrs.vm(Tags.SPSID) > 0);
    }
}