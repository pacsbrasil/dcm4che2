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
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.wado.WadoParams.*;

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

   /**
     * Returns a servlet response item that will encode the DICOM object in the
     * desired transfer syntax, and with the given frames. TODO - have some way
     * to figure out whether the DICOM object can be used as-is, that is
     * directly returned without any changes at all.
     */
   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
      log.info("Recoding the DICOM - updating headers etc.");
	  try {
		 String tsuid = (String) params.get(TRANSFER_SYNTAX);
		 DicomImageReader reader = dicomImageReaderFilter.filter(null, params);
		 if( reader==null ) {
			log.warn("No image/dicom object found for objectUID="+params.get(OBJECT_UID));
			return new ErrorResponseItem(HttpServletResponse.SC_NOT_FOUND,"Object not found.");
		 }
		 DicomObject ds = null;
		 synchronized (reader) {
			 if( reader.getStreamMetadata()==null ) {
				 log.warn("No image/dicom stream found for objectUID="+params.get(OBJECT_UID));
				 return new ErrorResponseItem(HttpServletResponse.SC_NOT_FOUND,"Object not found.");
			 }
			 DicomStreamMetaData streamData = (DicomStreamMetaData) reader.getStreamMetadata();
			 ds = streamData.getDicomObject();
		 }
		 if (ds == null) {
			 log.warn("Unable to parse dicom object for objectUID="+params.get(OBJECT_UID));
			 return new ErrorResponseItem(HttpServletResponse.SC_NOT_FOUND,"No dicom object.");
		 }
		 tsuid = selectTransferSyntax(ds, tsuid);
		 log.info("Transfer syntax chosen "+tsuid);
		 if (isNoPixelData(tsuid)) {
			return new NoPixelDataServletResponse(ds, tsuid);
		 }

		 List<Integer> frames = null;
		 frames = computeFrames(ds, params);

		 return new DicomImageServletResponse(ds, tsuid, frames, wadoImageFilter, params);
	  } catch (IOException e) {
		 throw new RuntimeException("Caught unexpected exception ", e);
	  }
   }

   /** Computes the frames to return as frame numbers (1 based)
    * TODO - handle overlay simple frame list modifications as well...
    * null return means ALL frames, while empty list return means no frames, any other means the selected frames.
    */
   public static List<Integer> computeFrames(DicomObject ds, Map<String, Object> params) {
	  String sframes = (String) params.get(SIMPLE_FRAME_LIST);
	  if( sframes==null ) {
		  sframes = (String) params.get(FRAME_NUMBER);
		  if( sframes==null ) return null;
		  params.put(SIMPLE_FRAME_LIST, sframes);
		  params.remove(FRAME_NUMBER);
	  }
	  int startFrame = 1, endFrame = ds.getInt(Tag.NumberOfFrames,1);
	  if( sframes!=null ) {
		 List<Integer> ret = new ArrayList<Integer>();
		 String[] parts = sframes.split(",");
		 for(String part : parts ) {
            // Shortcut once all frames are included - prevents huge lists being created in memory
            if( ret.size()>endFrame ) return ret;
			int dash = part.indexOf('-');
			if( dash<0 ) {
			   Integer frame = Integer.parseInt(part.trim());
			   if( frame>=startFrame && frame <=endFrame ) ret.add(frame);
			}
			else {
			   // TODO - add sanity checking on bounds.
			   int start = Integer.parseInt(part.substring(0,dash).trim());
			   if( start < 1 ) start = 1;
			   if( start > endFrame ) continue;
			   int end = Integer.parseInt(part.substring(dash+1).trim());
			   if( end < start ) continue;
			   if( end > endFrame ) end = endFrame;
			   
			   log.info("Adding frames "+start+"-"+end);
			   for(int j=start; j<=end; j++) {
				  ret.add(new Integer(j));
			   }
			}
		 }
		 return ret;
	  }
	  return null;
   }

   /** Determine if the tsuid is exactly one of the no pixel data deflate syntaxes - 
    * by exactly, it means the same value as in the class, not an aribtrary value.
    * @param tsuid
    * @return
    */
   public static boolean isNoPixelData(String tsuid) {
	  return tsuid == UID.NoPixelData || tsuid == UID.NoPixelDataDeflate;
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
	  if (tsuid.indexOf(UID.NoPixelDataDeflate) >= 0)
		 return UID.NoPixelDataDeflate;
	  if (tsuid.indexOf(UID.NoPixelData) >= 0)
		 return UID.NoPixelData;
	  // Otherwise see if we can retain the existing transfer syntax to avoid
        // recoding images.
	  if (tsuid.indexOf(currentUid) >= 0)
		 return currentUid;
	  int firstSyntax = tsuid.indexOf('\\');
	  if (firstSyntax >= 0)
		 return tsuid.substring(0, firstSyntax);
	  return tsuid;
   }

   private Filter<DicomImageReader> dicomImageReaderFilter;

   public Filter<DicomImageReader> getDicomImageReaderFilter() {
      return dicomImageReaderFilter;
   }

   /**
    * Set the filter that reads the dicom image reader objects for a given SOP UID
    * @param dicomFilter
    */
   @MetaData(out="${ref:dicomImageReader}")
   public void setDicomImageReaderFilter(Filter<DicomImageReader> dicomImageReaderFilter) {
      this.dicomImageReaderFilter = dicomImageReaderFilter;
   }


   private Filter<WadoImage> wadoImageFilter;

	public Filter<WadoImage> getWadoImageFilter() {
   	return wadoImageFilter;
   }

	/**
	 * Sets the filter to use for the wado image data.
	 * @param wadoImageFilter
	 */
	@MetaData(out="${ref:dcmImg}")
	public void setWadoImageFilter(Filter<WadoImage> wadoImageFilter) {
   	this.wadoImageFilter = wadoImageFilter;
   }
}
