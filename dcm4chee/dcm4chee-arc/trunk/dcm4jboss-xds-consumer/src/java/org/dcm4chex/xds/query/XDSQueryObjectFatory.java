package org.dcm4chex.xds.query;

import java.util.List;

public abstract class XDSQueryObjectFatory {

    public static XDSQueryObjectFatory getInstance( boolean forceSQL ) {
        if ( forceSQL ) {
            return new SQLQueryObjectFactory();
        } else {
            return new StoredQueryObjectFactory();
        }
    }
    public abstract XDSQueryObject newFindDocumentQuery(String patId,
            String status);

    public abstract XDSQueryObject newFindDocumentQuery(String patId,
            String status, String dateTimeAtt, String dateTimeFrom,
            String dateTimeTo, List classCodes, List psCodes, List hcftCodes,
            List evCodes);

    public abstract XDSQueryObject newGetDocumentQuery(String[] uuids);
    
}