// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.location;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;

/**
 * Filter that will do a string replacement on a URL in the URL filter pipeline.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class LocationURLReplacementFilter implements Filter<URL>
{
   public static final String PATH_MATCH = "pathMatch";
   public static final String PATH_REPLACE = "pathReplace";
   

   /**
    * Intercept a URL passing through the filter chain and update the path that it contains
    * if necessary.
    * @see org.dcm4chee.xero.metadata.filter.Filter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)
    */
   @Override
   public URL filter(FilterItem<URL> filterItem, Map<String, Object> params)
   {
      // Call forward so we have something to work with...
      URL url = filterItem.callNextFilter(params);

      if(url!=null)
      {
         String ae = FilterUtil.getString(params, "ae","local");
         Map<String,Object> aeMap = AEProperties.getInstance().getAE(ae);
         String match = FilterUtil.getString(aeMap, PATH_MATCH);
         String replace = FilterUtil.getString(aeMap, PATH_REPLACE);
         
         if(match != null && replace != null)
         {
            String urlStr = url.toString();
            String updatedStr = urlStr.replace(match, replace);
            
            try
            {
               url = new URL(url,updatedStr);
            }
            catch(MalformedURLException e)
            {
               throw new RuntimeException("Path replacement failed for "+match+"/"+replace,e);
            }
         }
      }
      
      return url;
   }
}
