/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 14.01.2004
 */
public class QueryStudiesCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] SELECT_ATTRIBUTE = { "Patient.pk",
            "Patient.encodedAttributes", "Study.pk", "Study.encodedAttributes",
            "Study.modalitiesInStudy", "Study.numberOfStudyRelatedSeries",
            "Study.numberOfStudyRelatedInstances", "Study.retrieveAETs",
            "Study.availability", "Study.filesetId", "Patient.hidden", "Study.hidden"};

    private static final String[] ENTITY = {"Patient"};
    private static final String[] ENTITY_FOR_HIDDEN = {"Patient", "Study", "Series", "Instance"};

    private static final String[] LEFT_JOIN = { 
            "Study", null, "Patient.pk", "Study.patient_fk",};
    private static final String[] LEFT_JOIN_FOR_HIDDEN = new String[] { 
    	"Study", null, "Patient.pk", "Study.patient_fk",
    	"Series", null, "Study.pk", "Series.study_fk",
    	"Instance", null, "Series.pk", "Instance.series_fk"};

    private final SqlBuilder sqlBuilder = new SqlBuilder();
    
    public QueryStudiesCmd(Dataset filter, boolean showHidden, boolean hideMissingStudies)
            throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
        if ( showHidden ) {
        	sqlBuilder.setFrom(ENTITY);
        	sqlBuilder.setLeftJoin(LEFT_JOIN_FOR_HIDDEN);
        	Match.Node node = sqlBuilder.addNodeMatch("OR",false);
        	node.addMatch( sqlBuilder.getBooleanMatch(null,"Study.hidden",SqlBuilder.TYPE2,true));
        	node.addMatch( sqlBuilder.getBooleanMatch(null,"Series.hidden",SqlBuilder.TYPE2,true));
        	node.addMatch( sqlBuilder.getBooleanMatch(null,"Instance.hidden",SqlBuilder.TYPE2,true));
        } else {
        	sqlBuilder.setFrom(ENTITY);
            sqlBuilder.setLeftJoin(LEFT_JOIN);
    		sqlBuilder.addBooleanMatch(null, "Study.hidden", SqlBuilder.TYPE2, false );
        }
        sqlBuilder.addLiteralMatch(null, "Patient.merge_fk", false, "IS NULL");
        sqlBuilder.addWildCardMatch(null, "Patient.patientId",
                SqlBuilder.TYPE2,
                filter.getString(Tags.PatientID),
                false);
        sqlBuilder.addWildCardMatch(null, "Patient.patientName",
                SqlBuilder.TYPE2,
                filter.getString(Tags.PatientName),
                true);
        sqlBuilder.addWildCardMatch(null, "Study.studyId", SqlBuilder.TYPE2,
                filter.getString(Tags.StudyID), false);
        sqlBuilder.addSingleValueMatch(null, "Study.studyIuid",
                SqlBuilder.TYPE1, filter.getString( Tags.StudyInstanceUID));
        sqlBuilder.addRangeMatch(null, "Study.studyDateTime",
                SqlBuilder.TYPE2,
                filter.getDateTimeRange(Tags.StudyDate, Tags.StudyTime));
        sqlBuilder.addWildCardMatch(null, "Study.accessionNumber",
                SqlBuilder.TYPE2,
                filter.getString(Tags.AccessionNumber),
                false);
        sqlBuilder.addModalitiesInStudyMatch(null, filter
                .getString(Tags.ModalitiesInStudy));
        if ( hideMissingStudies && !showHidden )
        	sqlBuilder.addNULLValueMatch(null,"Study.encodedAttributes", true);
    }

    public int count() throws SQLException {
        try {
            sqlBuilder.setSelectCount();
            execute(sqlBuilder.getSql());
            next();
            return rs.getInt(1);
        } finally {
            close();
        }
    }

	
    public List list(int offset, int limit) throws SQLException {
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.addOrderBy("Patient.patientName", SqlBuilder.ASC);
        sqlBuilder.addOrderBy("Patient.pk", SqlBuilder.ASC);
        sqlBuilder.addOrderBy("Study.studyDateTime", SqlBuilder.ASC);
        sqlBuilder.setOffset(offset);
        sqlBuilder.setLimit(limit);
        try {
            execute(sqlBuilder.getSql());
            ArrayList result = new ArrayList();
            
            while (next()) {
                final byte[] patAttrs = getBytes(2);
                final byte[] styAttrs = getBytes(4);
                Dataset ds = dof.newDataset();
                ds.setPrivateCreatorID(PrivateTags.CreatorID);
                ds.putUL(PrivateTags.PatientPk, rs.getInt(1));
                DatasetUtils.fromByteArray(patAttrs, DcmDecodeParam.EVR_LE, ds);
                if (styAttrs != null) {
                    ds.putUL(PrivateTags.StudyPk, rs.getInt(3));
                    DatasetUtils.fromByteArray(styAttrs,
                            DcmDecodeParam.EVR_LE,
                            ds);
                    ds.putCS(Tags.ModalitiesInStudy, StringUtils.split(rs
                            .getString(5), '\\'));
                    ds.putIS(Tags.NumberOfStudyRelatedSeries, rs.getInt(6));
                    ds.putIS(Tags.NumberOfStudyRelatedInstances, rs.getInt(7));
                    ds.putAE(Tags.RetrieveAET, StringUtils.split(rs
                            .getString(8), '\\'));
                    ds.putCS(Tags.InstanceAvailability, Availability
                            .toString(rs.getInt(9)));
                    ds.putSH(Tags.StorageMediaFileSetID, rs.getString(10));
                    if ( rs.getBoolean(12) ) 
                       	ds.putSS(PrivateTags.HiddenStudy,1);
                }
                if ( rs.getBoolean(11) ) 
                	ds.putSS(PrivateTags.HiddenPatient,1);
                result.add(ds);
            }
            return result;
        } finally {
            close();
        }
    }
}