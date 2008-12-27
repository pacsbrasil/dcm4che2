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

package org.dcm4chee.xero.search.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.CloseUtils;
import org.dcm4chee.xero.search.study.ResultsBean;

/**
 * Class that helps to find the dicom files in the test data repository.
 * <p>
 * TODO: Add a directory or dicomdir load
 * 
 * @author Andrew Cowan (amidx)
 */
public class DicomTestData
{

   /**
    * Create a new ResultsBean based on the file path provided.  A single file
    * will be 
    * @param path
    * @return
    * @throws IOException 
    */
   public static ResultsBean createResultsBeanFromPath(String path) throws IOException
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL u = cl.getResource(path);
      File f = (File)u.getContent();
      if(!f.exists())
         return null;
      
      if(f.isDirectory())
      {
         
      }
      else if(f.isFile())
      {
         
      }

      return null;
//      ResultsBean results = new ResultsBean();
//      results.addResult(DicomTestData.findDicomObject("org/dcm4chee/xero/search/study/MG0001.dcm"));
//      results.addResult(DicomTestData.findDicomObject("org/dcm4chee/xero/search/study/MG0002.dcm"));
//      results.addResult(DicomTestData.findDicomObject("org/dcm4chee/xero/search/study/MG0003.dcm"));
   }

   public static DicomObject findDicomObject(String relativePath) throws IOException
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream is = null;
      DicomInputStream din = null;

      try
      {
         is = cl.getResourceAsStream(relativePath);
         if(is==null) 
            throw new FileNotFoundException("Could not find the file "+relativePath);
         
         din = new DicomInputStream(is);
         return din.readDicomObject();
      }
      finally
      {
         CloseUtils.safeClose(is);
         CloseUtils.safeClose(din);
      }
   }
}
