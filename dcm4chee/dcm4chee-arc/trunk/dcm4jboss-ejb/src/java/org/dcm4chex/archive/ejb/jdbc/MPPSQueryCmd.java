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
import org.dcm4chex.archive.common.DatasetUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 10.02.2004
 */
public class MPPSQueryCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "Patient", "MPPS"};

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "MPPS.encodedAttributes"};

    private static final String[] RELATIONS = { "Patient.pk",
    		"MPPS.patient_fk"};

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    /**
     * @param ds
     * @throws SQLException
     */
    public MPPSQueryCmd(MPPSFilter filter) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
        // ensure keys contains (8,0005) for use as result filter
        sqlBuilder.setSelect(SELECT);
        sqlBuilder.setFrom(FROM);
        sqlBuilder.setRelations(RELATIONS);
        sqlBuilder.addSingleValueMatch(null, "MPPS.sopIuid",
                SqlBuilder.TYPE1,
                filter.getSopIuid() );
        if ( filter.isEmptyAccNo() ) {
        	sqlBuilder.addNULLValueMatch(null, "MPPS.accessionNumber", false );
        } else {
	        sqlBuilder.addSingleValueMatch(null, "MPPS.accessionNumber",
	                SqlBuilder.TYPE2,
	                filter.getAccessionNumber() );
        }
        sqlBuilder.addSingleValueMatch(null, "Patient.patientId",
                SqlBuilder.TYPE1,
                filter.getPatientID() );
		String patientName = filter.getPatientName();
    	if ( patientName != null && 
       		 patientName.length() > 0 && 
   			 patientName.indexOf('*') == -1 &&
   			 patientName.indexOf('?') == -1) patientName+="*";
        sqlBuilder.addWildCardMatch(null, "Patient.patientName",
                SqlBuilder.TYPE1,
                patientName,
                true);
        sqlBuilder.addSingleValueMatch(null, "MPPS.modality",
                SqlBuilder.TYPE1,
                filter.getModality());
        sqlBuilder.addSingleValueMatch(null, "MPPS.performedStationAET",
                SqlBuilder.TYPE1,
                filter.getStationAET());
        sqlBuilder.addRangeMatch(null, "MPPS.ppsStartDateTime",
                SqlBuilder.TYPE1,
                filter.dateTimeRange());
        sqlBuilder.addSingleValueMatch(null, "MPPS.ppsStatusAsInt",
                SqlBuilder.TYPE1,
				filter.getStatus());
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();       
        DatasetUtils.fromByteArray( getBytes(1), ds);
        DatasetUtils.fromByteArray( getBytes(2), ds);
        return ds;
    }
    
 
}