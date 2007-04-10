package org.dcm4chex.xds.query;

import java.util.ArrayList;
import java.util.List;

public class StoredQueryObjectFactory extends XDSQueryObjectFatory {

    public static final String V3_STATUS_PREFIX = "urn:oasis:names:tc:ebxml-regrep:StatusType:";
    public static final String V3_STATUS_SUBMITTED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted";
    public static final String V3_STATUS_APPROVED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved";
    public static final String V3_STATUS_DEPRECATED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated";

    public static final String STORED_QUERY_FIND_DOCUMENTS = "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d";
    public static final String STORED_QUERY_FIND_SUBMISSIONSETS = "urn:uuid:f26abbcb-ac74-4422-8a30-edb644bbc1a9";
    public static final String STORED_QUERY_FIND_FOLDERS = "urn:uuid:958f3006-baad-4929-a4deff1114824431";
    public static final String STORED_QUERY_GET_ALL = "urn:uuid:10b545ea-725c-446d-9b95-8aeb444eddf3";
    public static final String STORED_QUERY_GET_DOCUMENTS = "urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4";
    public static final String STORED_QUERY_GET_FOLDERS = "urn:uuid:5737b14c-8a1a-4539-b659-e03a34a5e1e4";
    public static final String STORED_QUERY_GET_SUBMISSIONSETS = "urn:uuid:51224314-5390-4169-9b91-b1980040715a";

    public XDSQueryObject newFindDocumentQuery(String patId, String status) {
        StoredQueryObject sqo = new StoredQueryObject(STORED_QUERY_FIND_DOCUMENTS);
        sqo.addQueryParameter("$XDSDocumentEntryPatientId", patId);
        ArrayList l = new ArrayList();
        if ( status != null ) {
            if ( ! status.startsWith(V3_STATUS_PREFIX)) {
                status = V3_STATUS_PREFIX + status;
            }
            l.add(status);
        } else {
            l.add(V3_STATUS_SUBMITTED);
            l.add(V3_STATUS_APPROVED);
            l.add(V3_STATUS_DEPRECATED);
        }
        sqo.addQueryParameter("$XDSDocumentEntryStatus", l);
        return sqo;
    }
    public XDSQueryObject newFindDocumentQuery(String patId, String status, 
                    String dateTimeAtt, String dateTimeFrom, String dateTimeTo, 
                    List classCodes, List psCodes, List hcftCodes, List evCodes) {
        StoredQueryObject sqo = (StoredQueryObject) newFindDocumentQuery(patId, status);
        if ( dateTimeAtt != null ) sqo.addQueryParameter("$XDSDocumentEntryCreationTimeFrom", dateTimeAtt);
        if ( classCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryClassCode", classCodes);
        if ( psCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryPracticeSettingCode", psCodes);
        if ( hcftCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryHealthcareFacilityTypeCode", hcftCodes);
        if ( evCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryEventCodeList", evCodes);
        return sqo;
    }

    public XDSQueryObject newGetDocumentQuery(String[] uuids) {
        StoredQueryObject sqo = new StoredQueryObject(STORED_QUERY_GET_DOCUMENTS);
        ArrayList l = new ArrayList();
        for ( int i = 0 ; i < uuids.length ; i++) {
            l.add(uuids[i]);
        }
        sqo.addQueryParameter("$XDSDocumentEntryEntryUUID", l);
        sqo.setReturnType(XDSQueryObject.RETURN_TYPE_LEAF);
        return sqo;
    }
    

}
