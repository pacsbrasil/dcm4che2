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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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
import java.sql.Types;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.UPSState;
import org.dcm4chex.archive.common.Priority;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Apr 19, 2010
 */
public class UPSQueryCmd extends BaseDSQueryCmd {

    public static int transactionIsolationLevel = 0;
    public static int blobAccessType = Types.LONGVARBINARY;

    private static final String[] FROM = { "UPS" };

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "UPS.encodedAttributes"};

    private static final String ITEM_CODE = "item_code";
    private static final String APP_CODE = "app_code";
    private static final String DEVNAME_CODE = "devname_code";
    private static final String DEVCLASS_CODE = "devclass_code";
    private static final String DEVLOC_CODE = "devloc_code";
    private static final String PERF_CODE = "perf_code";
    private static final String RELPS_CODE = "relps_code";

    public UPSQueryCmd(Dataset keys) throws SQLException {
        super(keys, true, false, transactionIsolationLevel);
        AttributeFilter patAttrFilter = AttributeFilter.getPatientAttributeFilter();
        defineColumnTypes(new int[] { blobAccessType, blobAccessType });
        String s;
        // ensure keys contains (8,0005) for use as result filter
        if (!keys.contains(Tags.SpecificCharacterSet)) {
            keys.putCS(Tags.SpecificCharacterSet);
        }
        sqlBuilder.setSelect(SELECT);
        sqlBuilder.setFrom(FROM);
        sqlBuilder.setLeftJoin(getLeftJoin());
        sqlBuilder.addListOfUidMatch(null, "UPS.sopInstanceUID",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPInstanceUID));
        if ((s = keys.getString(Tags.UPSState)) != null) {
            sqlBuilder.addIntValueMatch(null, "UPS.stateAsInt",
                    SqlBuilder.TYPE1,
                    UPSState.toInt(s));
        }
        s = keys.getString(Tags.SPSPriority);
        if (s != null) {
            sqlBuilder.addIntValueMatch(null, "UPS.priorityAsInt",
                    SqlBuilder.TYPE1,
                    Priority.toInt(s));
        }
        sqlBuilder.addWildCardMatch(null, "UPS.procedureStepLabel",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.ProcedureStepLabel));
        sqlBuilder.addWildCardMatch(null, "UPS.worklistLabel",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.WorklistLabel));
        sqlBuilder.addRangeMatch(null, "UPS.scheduledStartDateTime",
                SqlBuilder.TYPE1,
                keys.getDateRange(Tags.SPSStartDateAndTime));
        sqlBuilder.addRangeMatch(null, "UPS.expectedCompletionDateTime",
                type2,
                keys.getDateRange(Tags.ExpectedCompletionDateAndTime));
        addCodeMatch(Tags.ScheduledWorkitemCodeSeq, ITEM_CODE);
        addCodeMatch(Tags.ScheduledProcessingApplicationsCodeSeq, APP_CODE);
        addCodeMatch(Tags.ScheduledStationNameCodeSeq, DEVNAME_CODE);
        addCodeMatch(Tags.ScheduledStationClassCodeSeq, DEVCLASS_CODE);
        addCodeMatch(Tags.ScheduledStationGeographicLocationCodeSeq, DEVLOC_CODE);
        Dataset item = keys.getItem(Tags.ScheduledHumanPerformersSeq);
        if (item != null)
            addCodeMatch(item.getItem(Tags.HumanPerformerCodeSeq), PERF_CODE);
        item = keys.getItem(Tags.RefRequestSeq);
        if (item != null) {
            sqlBuilder.addWildCardMatch(null,
                    "UPSRequest.requestedProcedureId",
                    type2,
                    item.getStrings(Tags.RequestedProcedureID));
            sqlBuilder.addWildCardMatch(null,
                    "UPSRequest.accessionNumber",
                    type2,
                    item.getStrings(Tags.AccessionNumber));
            sqlBuilder.addWildCardMatch(null,
                    "UPSRequest.confidentialityCode",
                    type2,
                    item.getStrings(Tags.ConfidentialityCode));
            sqlBuilder.addWildCardMatch(null,
                    "UPSRequest.requestingService",
                    type2,
                    item.getStrings(Tags.RequestingService));
        }
        item = keys.getItem(Tags.RelatedProcedureStepSeq);
        if (item != null) {
            sqlBuilder.addSingleValueMatch(null,
                    "UPSRelatedPS.refSOPInstanceUID",
                    SqlBuilder.TYPE1,
                    item.getString(Tags.RefSOPInstanceUID));
            sqlBuilder.addSingleValueMatch(null,
                    "UPSRelatedPS.refSOPClassUID",
                    SqlBuilder.TYPE1,
                    item.getString(Tags.RefSOPClassUID));
            addCodeMatch(item.getItem(Tags.PurposeOfReferenceCodeSeq),
                    RELPS_CODE);
        }
        sqlBuilder.addWildCardMatch(null, "UPS.admissionID",
                type2,
                keys.getStrings(Tags.AdmissionID));
        Dataset issuer = keys.getItem(Tags.IssuerOfAdmissionIDSeq);
        if (issuer != null) {
            sqlBuilder.addSingleValueMatch(null,
                    "UPS.issuerOfAdmissionIDLocalNamespaceEntityID",
                    type2,
                    issuer.getString(Tags.LocalNamespaceEntityID));
            sqlBuilder.addSingleValueMatch(null,
                    "UPS.issuerOfAdmissionIDUniversalEntityID",
                    type2,
                    issuer.getString(Tags.UniversalEntityID));
        }
        sqlBuilder.addWildCardMatch(null, "Patient.patientId",
                type2,
                patAttrFilter.getStrings(keys, Tags.PatientID));
        sqlBuilder.addSingleValueMatch(null, "Patient.issuerOfPatientId",
                type2,
                patAttrFilter.getString(keys, Tags.IssuerOfPatientID));
        sqlBuilder.addPNMatch(new String[] {
                "Patient.patientName",
                "Patient.patientIdeographicName",
                "Patient.patientPhoneticName"},
                SqlBuilder.TYPE2,
                patAttrFilter.isICase(Tags.PatientName),
                keys.getString(Tags.PatientName));
        sqlBuilder.addRangeMatch(null, "Patient.patientBirthDate", type2,
                keys.getString(Tags.PatientBirthDate));
        sqlBuilder.addWildCardMatch(null, "Patient.patientSex", type2,
                patAttrFilter.getStrings(keys, Tags.PatientSex));
    }

    private void addCodeMatch(int tag, String alias) {
        addCodeMatch(keys.getItem(tag), alias);
    }

    private void addCodeMatch(Dataset item, String alias) {
        if (item != null) {
            sqlBuilder.addSingleValueMatch(alias, "Code.codeValue",
                    SqlBuilder.TYPE1,
                    item.getString(Tags.CodeValue));
            sqlBuilder.addSingleValueMatch(alias, "Code.codingSchemeDesignator",
                    SqlBuilder.TYPE1,
                    item.getString(Tags.CodingSchemeDesignator));
            sqlBuilder.addSingleValueMatch(alias, "Code.codingSchemeVersion",
                    SqlBuilder.TYPE1,
                    item.getString(Tags.CodingSchemeVersion));
        }
    }

    private boolean isMatchCode(int tag) {
        return isMatchCode(keys.getItem(tag));
    }

    private boolean isMatchCode(Dataset code) {
        return code != null
                && (code.containsValue(Tags.CodeValue) 
                        || code.containsValue(Tags.CodingSchemeDesignator));
    }

    private boolean isMatchRefRequest() {
        Dataset refrq = keys.getItem(Tags.RefRequestSeq);
        return refrq != null
                && (refrq.containsValue(Tags.RequestedProcedureID)
                        || refrq.containsValue(Tags.AccessionNumber)
                        || refrq.containsValue(Tags.ConfidentialityCode)
                        || refrq.containsValue(Tags.RequestingService));
    }
    
    private String[] getLeftJoin() {
        boolean workitem, appcode, devname, devclass, devloc, performer, request;
        int index = 4;
        if (workitem = isMatchCode(Tags.ScheduledWorkitemCodeSeq))
            index += 4;
        if (appcode = isMatchCode(Tags.ScheduledProcessingApplicationsCodeSeq))
            index += 8;
        if (devname = isMatchCode(Tags.ScheduledStationNameCodeSeq))
            index += 8;
        if (devclass = isMatchCode(Tags.ScheduledStationClassCodeSeq))
            index += 8;
        if (devloc = isMatchCode(Tags.ScheduledStationGeographicLocationCodeSeq))
            index += 8;
        Dataset performerItem = keys.getItem(Tags.ScheduledHumanPerformersSeq);
        if (performer = performerItem != null 
                && isMatchCode(performerItem.getItem(Tags.HumanPerformerCodeSeq)))
            index += 8;
        if (request = isMatchRefRequest())
            index += 4;
        Dataset relpsitem = keys.getItem(Tags.RelatedProcedureStepSeq);
        boolean relatedps = false;
        boolean relatedpscode = false;
        if (relpsitem != null) {
            if (relatedpscode = isMatchCode(
                    relpsitem.getItem(Tags.PurposeOfReferenceCodeSeq))) {
                relatedps = true;
                index += 8;
            } else if (relatedps = relpsitem.containsValue(Tags.RefSOPInstanceUID)
                    || relpsitem.containsValue(Tags.RefSOPClassUID))
                index += 4;
        }
        String[] leftJoin = new String[index];
        leftJoin[0] = "Patient";
        leftJoin[1] = null;
        leftJoin[2] = "UPS.patient_fk";
        leftJoin[3] = "Patient.pk";
        index = 4;
        if (workitem) {
            leftJoin[index++] = "Code";
            leftJoin[index++] = ITEM_CODE;
            leftJoin[index++] = "UPS.code_fk";
            leftJoin[index++] = "Code.pk";
        }
        if (appcode) {
            sqlBuilder.setDistinct(true);
            leftJoin[index++] = "rel_ups_appcode";
            leftJoin[index++] = null;
            leftJoin[index++] = "UPS.pk";
            leftJoin[index++] = "rel_ups_appcode.ups_fk";
            leftJoin[index++] = "Code";
            leftJoin[index++] = APP_CODE;
            leftJoin[index++] = "rel_ups_appcode.appcode_fk";
            leftJoin[index++] = "Code.pk";
        }
        if (devname) {
            sqlBuilder.setDistinct(true);
            leftJoin[index++] = "rel_ups_devname";
            leftJoin[index++] = null;
            leftJoin[index++] = "UPS.pk";
            leftJoin[index++] = "rel_ups_devname.ups_fk";
            leftJoin[index++] = "Code";
            leftJoin[index++] = DEVNAME_CODE;
            leftJoin[index++] = "rel_ups_devname.devname_fk";
            leftJoin[index++] = "Code.pk";
        }
        if (devclass) {
            sqlBuilder.setDistinct(true);
            leftJoin[index++] = "rel_ups_devclass";
            leftJoin[index++] = null;
            leftJoin[index++] = "UPS.pk";
            leftJoin[index++] = "rel_ups_devclass.ups_fk";
            leftJoin[index++] = "Code";
            leftJoin[index++] = DEVCLASS_CODE;
            leftJoin[index++] = "rel_ups_devclass.devclass_fk";
            leftJoin[index++] = "Code.pk";
        }
        if (devloc) {
            sqlBuilder.setDistinct(true);
            leftJoin[index++] = "rel_ups_devloc";
            leftJoin[index++] = null;
            leftJoin[index++] = "UPS.pk";
            leftJoin[index++] = "rel_ups_devloc.ups_fk";
            leftJoin[index++] = "Code";
            leftJoin[index++] = DEVLOC_CODE;
            leftJoin[index++] = "rel_ups_devloc.devloc_fk";
            leftJoin[index++] = "Code.pk";
        }
        if (performer) {
            sqlBuilder.setDistinct(true);
            leftJoin[index++] = "rel_ups_performer";
            leftJoin[index++] = null;
            leftJoin[index++] = "UPS.pk";
            leftJoin[index++] = "rel_ups_performer.ups_fk";
            leftJoin[index++] = "Code";
            leftJoin[index++] = PERF_CODE;
            leftJoin[index++] = "rel_ups_performer.performer_fk";
            leftJoin[index++] = "Code.pk";
        }
        if (request) {
            sqlBuilder.setDistinct(true);
            leftJoin[index++] = "UPSRequest";
            leftJoin[index++] = null;
            leftJoin[index++] = "UPS.pk";
            leftJoin[index++] = "UPSRequest.ups_fk";
        }
        if (relatedps) {
            sqlBuilder.setDistinct(true);
            leftJoin[index++] = "UPSRelatedPS";
            leftJoin[index++] = null;
            leftJoin[index++] = "UPS.pk";
            leftJoin[index++] = "UPSRelatedPS.ups_fk";
            if (relatedpscode) {
                leftJoin[index++] = "Code";
                leftJoin[index++] = RELPS_CODE;
                leftJoin[index++] = "UPSRelatedPS.code_fk";
                leftJoin[index++] = "Code.pk";
            }
        }
        return leftJoin;
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        DatasetUtils.fromByteArray( rs.getBytes(1), ds);
        DatasetUtils.fromByteArray( rs.getBytes(2), ds);
        adjustDataset(ds, keys);
        return ds.subSet(keys);
    }
}
