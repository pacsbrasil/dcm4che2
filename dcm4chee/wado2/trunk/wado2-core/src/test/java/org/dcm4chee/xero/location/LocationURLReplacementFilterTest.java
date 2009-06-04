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
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.search.filter.SimpleFilterItem;

import static org.testng.Assert.*;
import org.testng.annotations.*;

/**
 *
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class LocationURLReplacementFilterTest
{
   private Map<String,Object> params;
   
   @BeforeMethod
   public void setup()
   {
      this.params = new HashMap<String, Object>();
   }
   
   
   @Test
   public void filter_MustReplaceContentOfURLPath() throws MalformedURLException
   {
      params.put("ae", "FilePrefix");
      URL url = new URL("file:////samba1/xero/online/2009/08/03/1.2.3.4.6.dcm");
      SimpleFilterItem<URL> filterItem = new SimpleFilterItem<URL>(url);
      LocationURLReplacementFilter filter = new LocationURLReplacementFilter();
      URL result = filter.filter(filterItem, params);
      assertEquals(result.toString(),"file://Y:/2009/08/03/1.2.3.4.6.dcm");
   }
   
   @Test
   public void filter_MustLeaveFilesWhenAENotConfigured() throws MalformedURLException
   {
      params.put("ae", null);
      URL url = new URL("file:////samba1/xero/online/2009/08/03/1.2.3.4.6.dcm");
      SimpleFilterItem<URL> filterItem = new SimpleFilterItem<URL>(url);
      LocationURLReplacementFilter filter = new LocationURLReplacementFilter();
      URL result = filter.filter(filterItem, params);
      assertEquals(result.toString(),url.toString());
   }

}
