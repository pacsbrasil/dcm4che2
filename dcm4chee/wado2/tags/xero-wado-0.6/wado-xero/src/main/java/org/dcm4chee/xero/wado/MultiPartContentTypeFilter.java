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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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

package org.dcm4chee.xero.wado;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When the response content-type is to be multi-part/mixed, 
 * coordinates multiple responses and assembles them into a single 
 * response object.
 */
public class MultiPartContentTypeFilter implements Filter<ServletResponseItem> 
{
    private static final Logger log = 
    	LoggerFactory.getLogger(MultiPartContentTypeFilter.class);

    static String OBJECT_UID = "objectUID";
    static String CONTENT_TYPE = "contentType";
    String MULTIPART_MIXED = "multipart/mixed";
    
    private static char CONTENT_DELIMITER = ',';
    private static char OBJECT_DELIMITER = '\\';
    
    
    /**
     *  Responds with a multi-part response item appropriate to the given multi-part content type request type
     * 
     *  Game plan:  First, dissect the contentType field, which contains multiple comman-separated return types
     *              (including the multipart/mixed type that caused us to be invoked
     *              Second, create response items for each return type, and add them as components to 
     *              a multi-part return item              
     */
    // first, dissect the contentType field.  It will contain multiple comma-separated return types
    // and the first type will be the multi-part that got us here
    public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) 
    {
        String objectUIDs = (String) params.get(OBJECT_UID);
        String contentTypes = (String) params.get(CONTENT_TYPE);

        if ( contentTypes == null )
        {
            log.warn("No content type found, expected to see multipart/mixed plus others");
            return null;
        }
        
        if ( objectUIDs == null )
        {
            log.warn("No objectUID specified");
            return null;
        }

        String[] contentTypeList = parseString( contentTypes, CONTENT_DELIMITER );
        
        if ( contentTypeList.length <= 1 || 
        	 !MULTIPART_MIXED.equals(contentTypeList[0]) ) 
        {
            log.warn("Actual content types were not specified, expected to see multipart/mixed plus others");
            return null;
        }
        
        String[] objectUIDList = parseString( objectUIDs, OBJECT_DELIMITER );
        
        MultiPartContentTypeResponseItem ri = getMultiPartResponseItem();
        
        for ( String objectUID: objectUIDList ) 
        {
            for ( String contentType: contentTypeList ) 
            {
                if ( contentType.equals(MULTIPART_MIXED) ) continue;
                
                // the content type field has all return types.  we want to isolate just the
                // one that this filter should be dealing with in this part of the response.
                Map<String,Object> newParams = new HashMap<String,Object>(params);
                newParams.put(OBJECT_UID, objectUID);
                newParams.put(CONTENT_TYPE, contentType);

                ServletResponseItem sri = getResponseItem( filterItem, newParams );
                if ( sri != null )
                	ri.add(sri);

            }
        }
        
        return ri;
    }
    
    protected ServletResponseItem getResponseItem( 
    		FilterItem<ServletResponseItem> filterItem, 
    		Map<String,Object> newParams )
    {
        // rather than understand all the variations for all return types, we'll invoke the
        // ChooseContentTypeFilter to sort everything out, because it does understand all the variations
        ServletResponseItem sri = null;
        try {
            sri = (ServletResponseItem) filterItem.callNamedFilter("choose", newParams);
        } 
        catch ( Exception e ) 
        {
            // we don't want one bad response to spoil the entire result.  
            // swallow it and move on
            log.warn("WADO Filter Exception for parameters " + newParams + "...skipping this one");
        }
        
        if ( sri == null ) 
        {
            log.warn("WADO Filter not found for parameters " + newParams);
        }
        else if (sri instanceof ErrorResponseItem) 
        {
            log.warn("WADO Filter for parameters " + newParams + "fails with error response...skipping");
            sri = null;
        }
        
    	return sri;
    }
    
    protected String[] parseString( String stringToParse, char delimiter )
    {
    	return StringUtil.split(stringToParse, delimiter, true);
    }
    
    protected MultiPartContentTypeResponseItem getMultiPartResponseItem()
    {
    	return new MultiPartContentTypeResponseItem();
    }
}

