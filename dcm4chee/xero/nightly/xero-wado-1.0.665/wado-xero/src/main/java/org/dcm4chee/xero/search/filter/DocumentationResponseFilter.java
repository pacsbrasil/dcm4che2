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
package org.dcm4chee.xero.search.filter;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.wado.UrlServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Return the documentation for a particular WADO responseType.  Documentation 
 * is read from the first location in the classpath that corresponds to:
 * "help/requestType.html" 
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DocumentationResponseFilter implements Filter<ServletResponseItem>
{
   private static Logger log = LoggerFactory.getLogger(DocumentationResponseFilter.class);
   
   public DocumentationResponseFilter()
   {
      log.debug("Created DocumentationResponseFilter");
   }
   
   @Override
   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params)
   {
      boolean help = FilterUtil.getBoolean(params, "help");
      String requestType = FilterUtil.getString(params, "requestType");

      if(help || requestType == null)
      {
         log.debug("Showing documentation page for requestType={}",requestType);
         try
         {
            HttpServletResponse response = (HttpServletResponse)params.get("_response");
            redirectToHelp(response,requestType);
         }
         catch(IOException e)
         {
            throw new RuntimeException("Unable to redirect to documentation for "+requestType,e);
         }
      }
      
      return filterItem.callNextFilter(params);
   }

   private void redirectToHelp(HttpServletResponse response,String requestType) 
      throws IOException
   {
      String relativePath = "help/";
      if(requestType!=null)
         relativePath += requestType.toLowerCase() + ".html";
      
      response.sendRedirect(relativePath);
   }
}
