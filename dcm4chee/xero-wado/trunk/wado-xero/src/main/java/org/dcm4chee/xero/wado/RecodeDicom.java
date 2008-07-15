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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes an existing DICOM file and encodes it as the same or a different
 * transfer syntax. This may involve converting the images from one format to
 * another, or stripping out the images entirely or partially. Supports:
 * useOrig=true - means to ignore this filter entirely and just call the next
 * filter (EncodeDicom) simpleFrameList=1,3,5-7
 * calculatedFrameList={firstFrame,lastFrame,increment}+ (with commas between
 * additional elements) timeRange=startTime,endTime (in seconds)
 * 
 * @author bwallace
 * 
 */
public class RecodeDicom implements Filter<ServletResponseItem> {
   private static Logger log = LoggerFactory.getLogger(RecodeDicom.class);

   public static final String NoPixelDataUid = "1.2.840.10008.1.2.4.XX";

   public static final String NoPixelDataDeflateUid = "1.2.840.10008.1.2.4.YY";

   /**
     * Returns a servlet response item that will encode the DICOM object in the
     * desired transfer syntax, and with the given frames. TODO - have some way
     * to figure out whether the DICOM object can be used as-is, that is
     * directly returned without any changes at all.
     */
   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
	  if (params.get("useOrig") != null) {
		 return (ServletResponseItem) filterItem.callNextFilter(params);
	  }

	  try {
		 String tsuid = (String) params.get("transferSyntax");
		 DicomImageReader reader = DicomFilter.filterDicomImageReader(filterItem, params, null);
		 if( reader==null || reader.getStreamMetadata()==null ) {
			log.warn("No image/dicom object found for objectUID="+params.get("objectUID"));
			return new ErrorResponseItem(HttpServletResponse.SC_NOT_FOUND,"Object not found.");
		 }
		 DicomStreamMetaData streamData = (DicomStreamMetaData) reader.getStreamMetadata();
		 DicomObject ds = streamData.getDicomObject();

		 tsuid = selectTransferSyntax(ds, tsuid);
		 log.info("Transfer syntax chosen "+tsuid);
		 if (isNoPixelData(tsuid)) {
			return new NoPixelDataServletResponse(ds, tsuid);
		 }

		 List<Integer> frames = null;
		 frames = computeFrames(ds, params);

		 return new DicomImageServletResponse(ds, tsuid, frames, filterItem, params);
	  } catch (IOException e) {
		 throw new RuntimeException("Caught unexpected exception ", e);
	  }
   }

   /** Computes the frames to return */
   protected static List<Integer> computeFrames(DicomObject ds, Map<String, Object> params) {
	  String sframes = (String) params.get("simpleFrameList");
	  if( sframes!=null ) {
		 List<Integer> ret = new ArrayList<Integer>();
		 String[] parts = sframes.split(",");
		 for(String part : parts ) {
			int dash = part.indexOf('-');
			if( dash<0 ) {
			   Integer frame = Integer.parseInt(part.trim())-1;
			   ret.add(frame);
			}
			else {
			   // TODO - add sanity checking on bounds.
			   int start = Integer.parseInt(part.substring(0,dash).trim());
			   int end = Integer.parseInt(part.substring(dash+1).trim());
			   log.info("Adding frames "+start+"-"+end);
			   end = end-1;
			   for(int j=start-1; j<=end; j++) {
				  ret.add(new Integer(j));
			   }
			}
		 }
		 return ret;
	  }
	  return null;
   }

   public static boolean isNoPixelData(String tsuid) {
	  return tsuid == NoPixelDataDeflateUid || tsuid == NoPixelDataUid;
   }

   /** Selects the transfer syntax to use */
   protected String selectTransferSyntax(DicomObject ds, String tsuid) {
	  String currentUid = ds.getString(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
	  if (tsuid == null) {
		 // Don't recode this
		 // TODO add a SAR to set whether to recode or not by default, and if
            // so what
		 // are the allowed recodings.
		 return currentUid;
	  }
	  // Choose no pixel data by preference at this end, and prefer deflated.
	  if (tsuid.indexOf(NoPixelDataDeflateUid) >= 0)
		 return NoPixelDataDeflateUid;
	  if (tsuid.indexOf(NoPixelDataUid) >= 0)
		 return NoPixelDataUid;
	  // Otherwise see if we can retain the existing transfer syntax to avoid
        // recoding images.
	  if (tsuid.indexOf(currentUid) >= 0)
		 return currentUid;
	  int firstSyntax = tsuid.indexOf('\\');
	  if (firstSyntax >= 0)
		 return tsuid.substring(0, firstSyntax);
	  return tsuid;
   }
}
