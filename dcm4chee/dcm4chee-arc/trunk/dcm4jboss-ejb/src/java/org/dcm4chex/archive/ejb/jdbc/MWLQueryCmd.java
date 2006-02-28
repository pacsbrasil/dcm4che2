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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.SPSStatus;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 10.02.2004
 */
public class MWLQueryCmd extends BaseReadCmd {


    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "Patient", "MWLItem"};

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "MWLItem.encodedAttributes"};

    private static final String[] RELATIONS = { "Patient.pk",
    		"MWLItem.patient_fk"};

    private static final int[] OPEN_STATI = new int[] { SPSStatus.SCHEDULED, SPSStatus.ARRIVED };

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    private final Dataset keys;

    /**
     * @param ds
     * @throws SQLException
     */
    public MWLQueryCmd(Dataset keys) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
        this.keys = keys;
        // ensure keys contains (8,0005) for use as result filter
        if (!keys.contains(Tags.SpecificCharacterSet)) {
            keys.putCS(Tags.SpecificCharacterSet);
        }
        sqlBuilder.setSelect(SELECT);
        sqlBuilder.setFrom(FROM);
        sqlBuilder.setRelations(RELATIONS);
        Dataset spsItem = keys.getItem(Tags.SPSSeq);
        String status = null;
        if (spsItem != null) {
        	status = spsItem.getString(Tags.SPSStatus);
            sqlBuilder.addSingleValueMatch(null, "MWLItem.spsId",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.SPSID));
            sqlBuilder.addRangeMatch(null, "MWLItem.spsStartDateTime",
                    SqlBuilder.TYPE1,
                    spsItem.getDateTimeRange(Tags.SPSStartDate,
                            Tags.SPSStartTime));
            sqlBuilder.addSingleValueMatch(null, "MWLItem.modality",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.Modality));
            sqlBuilder.addSingleValueMatch(null, "MWLItem.scheduledStationAET",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.ScheduledStationAET));
            PersonName pn = spsItem.getPersonName(Tags.PerformingPhysicianName);
            if (pn != null) {
                sqlBuilder.addWildCardMatch(null,
                        "MWLItem.performingPhysicianFamilyName",
                        SqlBuilder.TYPE2,
                        pn.get(PersonName.FAMILY),
                        true);
                sqlBuilder.addWildCardMatch(null,
                        "MWLItem.performingPhysicianGivenName",
                        SqlBuilder.TYPE2,
                        pn.get(PersonName.GIVEN),
                        true);
                sqlBuilder.addWildCardMatch(null,
                        "MWLItem.performingPhysicianMiddleName",
                        SqlBuilder.TYPE2,
                        pn.get(PersonName.MIDDLE),
                        true);
                PersonName ipn = pn.getIdeographic();
                if (ipn != null) {
                    sqlBuilder.addWildCardMatch(null,
                            "MWLItem.performingPhysicianIdeographicFamilyName",
                            SqlBuilder.TYPE2,
                            ipn.get(PersonName.FAMILY),
                            false);
                    sqlBuilder.addWildCardMatch(null,
                            "MWLItem.performingPhysicianIdeographicGivenName",
                            SqlBuilder.TYPE2,
                            ipn.get(PersonName.GIVEN),
                            false);
                    sqlBuilder.addWildCardMatch(null,
                            "MWLItem.performingPhysicianIdeographicMiddleName",
                            SqlBuilder.TYPE2,
                            ipn.get(PersonName.MIDDLE),
                            false);
                }
                PersonName ppn = pn.getPhonetic();
                if (ppn != null) {
                    sqlBuilder.addWildCardMatch(null,
                            "MWLItem.performingPhysicianPhoneticFamilyName",
                            SqlBuilder.TYPE2,
                            ppn.get(PersonName.FAMILY),
                            false);
                    sqlBuilder.addWildCardMatch(null,
                            "MWLItem.performingPhysicianPhoneticGivenName",
                            SqlBuilder.TYPE2,
                            ppn.get(PersonName.GIVEN),
                            false);
                    sqlBuilder.addWildCardMatch(null,
                            "MWLItem.performingPhysicianPhoneticMiddleName",
                            SqlBuilder.TYPE2,
                            ppn.get(PersonName.MIDDLE),
                            false);
                }
            }        
        }
    	if (status != null) {
            sqlBuilder.addIntValueMatch(null, "MWLItem.spsStatusAsInt",
                    SqlBuilder.TYPE1,
                    SPSStatus.toInt(status));
    	} else {
    		sqlBuilder.addListOfIntMatch(null, "MWLItem.spsStatusAsInt",
                    SqlBuilder.TYPE1,
                    OPEN_STATI );
    	}
        sqlBuilder.addSingleValueMatch(null, "MWLItem.requestedProcedureId",
                SqlBuilder.TYPE1,
                keys.getString(Tags.RequestedProcedureID));
        sqlBuilder.addSingleValueMatch(null, "MWLItem.accessionNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.AccessionNumber));
        sqlBuilder.addSingleValueMatch(null, "MWLItem.studyIuid",
                SqlBuilder.TYPE1,
                keys.getString(Tags.StudyInstanceUID));
        sqlBuilder.addSingleValueMatch(null, "Patient.patientId",
                SqlBuilder.TYPE1,
                keys.getString(Tags.PatientID));
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
            sqlBuilder.addWildCardMatch(null,
                    "Patient.patientMiddleName",
                    SqlBuilder.TYPE2,
                    pn.get(PersonName.MIDDLE),
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
                sqlBuilder.addWildCardMatch(null,
                        "Patient.patientIdeographicMiddleName",
                        SqlBuilder.TYPE2,
                        ipn.get(PersonName.MIDDLE),
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
                sqlBuilder.addWildCardMatch(null,
                        "Patient.patientPhoneticMiddleName",
                        SqlBuilder.TYPE2,
                        ppn.get(PersonName.MIDDLE),
                        false);
            }
        }        
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();       
        DatasetUtils.fromByteArray( getBytes(1), ds);
        DatasetUtils.fromByteArray(getBytes(2), ds);
        QueryCmd.adjustDataset(ds, keys);
        return ds.subSet(keys);
    }
}