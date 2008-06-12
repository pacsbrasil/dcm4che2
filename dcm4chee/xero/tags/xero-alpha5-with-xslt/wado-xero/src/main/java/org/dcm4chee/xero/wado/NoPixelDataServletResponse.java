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
package org.dcm4chee.xero.wado;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/**
 * Implements a servlet response item that writes a DICOM header only to the
 * output stream. Any encoding is allowed, but only items actually in the
 * provided DICOM object will be written, and no transcoding will take place
 * between formats.
 * 
 * @author bwallace
 * 
 */
public class NoPixelDataServletResponse implements ServletResponseItem {

   protected DicomObject ds;

   protected String tsuid;

   public NoPixelDataServletResponse(DicomObject ds, String tsuid) {
	  this.tsuid = tsuid;
	  this.ds = ds;
   }

   /** Write whatever dicom object was provided, in it's raw form - do not convert any image
    * pixel data that happens to be included.
    */
   public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  response.setContentType("application/dicom");
	  DicomObject fmiAttrs = getFileMetaInformation();
	  OutputStream os = response.getOutputStream();
	  DicomOutputStream dos = new DicomOutputStream(os);
	  dos.writeFileMetaInformation(fmiAttrs);
	  String useTsuid = tsuid;
	  // The NoPixelDataDeflate isn't yet fully defined, so use an internal value.
	  // TODO remove this when Supplement 119 gets real UIDs to use as it will happen automatically.
	  if( tsuid== RecodeDicom.NoPixelDataDeflateUid ) 
		 useTsuid = UID.DeflatedExplicitVRLittleEndian;
	  dos.writeDataset(ds, useTsuid);
	  dos.finish();
   }

   /**
     * Gets the file meta information attributes into a new DICOM object, so
     * that they can be updated and then the entire object returned.
     * 
     * @return
     */
   private DicomObject getFileMetaInformation() {
	  DicomObject ret = new BasicDicomObject();
	  ds.fileMetaInfo().copyTo(ret);
	  ret.putString(Tag.TransferSyntaxUID, VR.UI, tsuid);
	  return ds;
   }

}
