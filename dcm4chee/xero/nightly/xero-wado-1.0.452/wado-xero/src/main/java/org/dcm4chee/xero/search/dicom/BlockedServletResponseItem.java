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
package org.dcm4chee.xero.search.dicom;

import static org.dcm4chee.xero.wado.WadoParams.CONTENT_DISPOSITION;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This returns a blocked set or search queries in a DirectoryRecordSequence.  The return results
 * come from the request queries, defaulting to an image level query.  The results are directly
 * streamed as they are received, and child blocked results are unblocked before being streamed.
 * The queries always try to use the return all private C-FIND SOP classes, or if those are not available,
 * then use the defintions in the {@see org.dcm4chee.xero.search.study} package to define the return
 * results (well, actually whatever is at study/series/image search levels.)
 * @author bwallace
 *
 */
public class BlockedServletResponseItem implements ServletResponseItem, ResultFromDicom {
   private static final Logger log = LoggerFactory.getLogger(BlockedServletResponseItem.class);
   
   protected Filter<ResultFromDicom> cfind;
   protected Map<String, Object> params;
   
   protected ServletOutputStream sos;
   protected DicomOutputStream dos;
   
   protected String filename = "cfind.dcm";

   public BlockedServletResponseItem(Filter<ResultFromDicom> cfind, Map<String,Object> params) {
	  this.cfind = cfind;
	  this.params = params;
   }
   
   public void setFilename(String filename) {
       this.filename =filename;
   }

   public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
      response.setHeader(CONTENT_DISPOSITION, "inline;filename="+filename); 
	  response.setContentType("application/dicom");
	  sos = response.getOutputStream();
	  dos = new DicomOutputStream(sos);
	  dos.writeHeader(Tag.DirectoryRecordSequence, VR.SQ, -1);
	  params.put(DicomCFindFilter.EXTEND_RESULTS_KEY, this);
	  cfind.filter(null, params);
	  dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
	  sos.close();
   }

   /** Write the responses back to the servlet, one at a time */
   public void addResult(DicomObject data) {
	  try {
		 String uid = data.getString(Tag.SOPInstanceUID);
		 if( uid==null ) uid = data.getString(Tag.SeriesInstanceUID);
		 if( uid==null ) uid = data.getString(Tag.StudyInstanceUID);
		 if( uid==null ) uid = data.getString(Tag.PatientID);
		 log.info("Adding cfind result "+uid);
		 dos.writeHeader(Tag.Item, null, -1);
		 dos.writeDataset(data,TransferSyntax.NoPixelData);
		 dos.writeHeader(Tag.ItemDelimitationItem, null, 0);
	  } 
	  catch (IOException e) {
		 log.warn("Caught error on query "+e);
	  }
	  
   }

}
