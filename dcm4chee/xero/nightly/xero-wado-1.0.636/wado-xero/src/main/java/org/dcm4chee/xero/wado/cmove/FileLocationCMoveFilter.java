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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.wado.cmove;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Map;

import org.dcm4chee.xero.dicom.DicomURLHandler;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.filter.FileLocationParameterChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File location filter that will generate a URL with the 'cmove' protocol
 * that will negotiate a connection with a remote AE and transfer the 
 * requested data with a C-MOVE.
 * <p>
 * The URL generated will be associated with a URLStreamHandler which can 
 * create a DICOM association and retrieve the data.  The URL is generated
 * with the standard Agility URI:
 * <p>
 * dicom://<AE Title>@<host>:<port>/&uid=<study instance UID>[& level=STUDY]
 * @author Andrew Cowan (amidx)
 */
public class FileLocationCMoveFilter implements Filter<URL>
{
   private static final Logger log = LoggerFactory.getLogger(FileLocationCMoveFilter.class);
   private FileLocationParameterChecker checker;
   private DicomURLHandler dicomURLHandler;
   
   public FileLocationCMoveFilter(URLStreamHandler dicomHandler)
   {
      this.checker = new FileLocationParameterChecker("cmove");
      this.dicomURLHandler = new DicomURLHandler(dicomHandler);
   }
   
   public FileLocationCMoveFilter()
   {
      this(new DicomURLStreamHandler());
   }
   
   /**
    * Generate a URL for the requested file.
    * @throws RuntimeException when the URL cannot be generated.
    * @see org.dcm4chee.xero.metadata.filter.Filter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)
    */
   public URL filter(FilterItem<URL> filterItem, Map<String, Object> params)
   {
      if(!checker.isLocationTypeInParameters(params)) {
    	  log.info("Not performing c-move - not configured.");
          return filterItem.callNextFilter(params);
      }
	  log.info("In file location c-move.");
      
      try
      {
         URL dicomURL = dicomURLHandler.createURL(params);
         
         // Store the size of the URL in the cache.  Not sure how to estimate the size of the DICOM file yet...
         // TODO: Introduce a CacheSizeParameter utility to read/write the value.  org.dcm4chee.xero.params.* ?
         int size = dicomURL.toString().length() * 2 + 64;
         params.put(MemoryCacheFilter.CACHE_SIZE, size);
         
         return dicomURL;
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException("Unable to generate DICOM URL",e);
      }
   }

}
