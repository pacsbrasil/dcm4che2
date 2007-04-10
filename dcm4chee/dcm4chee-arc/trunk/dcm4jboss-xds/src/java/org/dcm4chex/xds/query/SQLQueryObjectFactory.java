package org.dcm4chex.xds.query;

import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.query.match.ClassificationMatch;

public class SQLQueryObjectFactory extends XDSQueryObjectFatory  {

    private static Logger log = Logger.getLogger(SQLQueryObjectFactory.class);
    
    /* (non-Javadoc)
     * @see org.dcm4chex.xds.query.XDSQueryObjectFatory#newFindDocumentQuery(java.lang.String, java.lang.String)
     */
    public SQLQueryObject newFindDocumentQuery(String patId, String status) {
        return newFindDocumentQuery(patId, status, null, null, null, null, null, null ,null);
    }
    /* (non-Javadoc)
     * @see org.dcm4chex.xds.query.XDSQueryObjectFatory#newFindDocumentQuery(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    public SQLQueryObject newFindDocumentQuery(String patId, String status, 
                    String dateTimeAtt, String dateTimeFrom, String dateTimeTo, 
                    List classCodes, List psCodes, List hcftCodes, List evCodes) {
        StringBuffer sbSelect = new StringBuffer();
        StringBuffer sbWhere = new StringBuffer();
        sbSelect.append("SELECT eo.id  FROM ExtrinsicObject eo, ExternalIdentifier ei");
        sbWhere.append(" WHERE eo.id = ei.registryobject");
        sbWhere.append(" AND ei.identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427'");
        sbWhere.append(" AND ei.value='").append( patId ).append("'");
        if ( status != null ) sbWhere.append(" AND eo.status = '"+status+"'");
        addTimeMatch(dateTimeAtt, dateTimeFrom, dateTimeTo, sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getClassCodeMatch(classCodes), sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getPSCodeMatch(psCodes), sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getHFTCodeMatch(hcftCodes), sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getEVCodeMatch(evCodes), sbSelect, sbWhere);
        sbSelect.append(sbWhere);
        log.info("getDocument SQL String:"+sbSelect);
       return new SQLQueryObject(sbSelect.toString());
    }
    
    public SQLQueryObject newGetDocumentQuery( String[] uuids ) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT doc.id FROM ExtrinsicObject doc WHERE doc.id IN ( ");
        addListString(sb, uuids);
        sb.append(" )");
        return new SQLQueryObject(sb.toString(),XDSQueryObject.RETURN_TYPE_LEAF,true);
    }
    
    private static void addTimeMatch(String dateTimeAtt, String dateTimeFrom, String dateTimeTo, StringBuffer sbSelect, StringBuffer sbWhere) {
        if (dateTimeAtt == null || ( dateTimeFrom == null && dateTimeTo == null)) return;
        sbSelect.append(", Slot dateTime");
        sbWhere.append(" AND ( dateTime.parent = doc.id AND dateTime.name = ").append(dateTimeAtt);
        if ( dateTimeFrom != null ) sbWhere.append(" AND dateTime.value &gt;=  ").append(dateTimeFrom);
        if ( dateTimeTo != null ) sbWhere.append(" AND dateTime.value &lt;  ").append(dateTimeTo);
    }
    private static void addCodeMatch(ClassificationMatch classMatch, StringBuffer sbSelect, StringBuffer sbWhere) {
        if (classMatch == null ) return;
        sbSelect.append(", Classification ").append(classMatch.getName());
        sbWhere.append(" AND ").append(classMatch);
    }
    
    private void addListString( StringBuffer sb, String[] values ) {
        if ( values == null || values.length < 1 ) return;
        sb.append('\'').append( values[0] ).append('\'');
        for ( int i = 1 ; i < values.length ; i++ ) {
            sb.append(",'").append(values[i]).append('\'');
        }
    }

}
