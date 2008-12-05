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

import static org.dcm4chee.xero.wado.WadoParams.CONTENT_DISPOSITION;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageWriter;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageWriterSpi;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a dicom header in (potentially) another transfer syntax. This class
 * WILL use the raw image data if available and if no images change
 * 
 * @author bwallace
 * 
 */
public class DicomImageServletResponse implements ServletResponseItem {
   private static Logger log = LoggerFactory.getLogger(DicomImageServletResponse.class);

   /** The list of frame numbers, in order to send to the output, 1 based. */
   protected List<Integer> frames;

   protected Map<String, Object> params;

   protected DicomObject ds;

   protected String tsuid;

   protected boolean readRaw;
   
   protected String sop;
   
   protected Filter<WadoImage> wadoImageFilter;

   /** Create the dicom image writers directly rather than trying to find them. */
   static ImageWriterSpi dicomImageWriterCreator = new DicomImageWriterSpi();

   public DicomImageServletResponse(DicomObject ds, String tsuid, List<Integer> frames, Filter<WadoImage> wadoImageFilter, Map<String, Object> params) {
	  this.ds = ds;
	  this.tsuid = tsuid;
	  if (frames != null && ds.getInt(Tag.NumberOfFrames, 1) == frames.size()) {
		 frames = null;
	  }
	  this.frames = frames;
	  this.params = params;
	  this.wadoImageFilter = wadoImageFilter;
	  this.sop = ds.getString(Tag.SOPInstanceUID);
	  readRaw = true;
	  if (!tsuid.equals(ds.getString(Tag.TransferSyntaxUID))) {
		 log.info("Can't read raw image data - transfer syntaxes are different.");
		 readRaw = false;
	  }
	  EncodeImage.addEncodingInfo(tsuid,params);
   }

   /**
     * Write the desired images out to the response.
     */
   public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  log.info("Writing a DICOM image response for "+sop + " to "+response);
	  response.setContentType("application/dicom");
	  response.setHeader(CONTENT_DISPOSITION,"attachment;filename="+ds.getString(Tag.SOPInstanceUID)+".dcm");
	  DicomObject dsUse = new BasicDicomObject();
	  ds.copyTo(dsUse);
	  dsUse.putString(Tag.TransferSyntaxUID, VR.UI, tsuid);
	  int numberOfFrames = dsUse.getInt(Tag.NumberOfFrames);
	  if( numberOfFrames<=0 ) numberOfFrames = 1;
	  if (frames != null) {
		 dsUse.putInt(Tag.NumberOfFrames, VR.IS, numberOfFrames);
	  }
	  DicomImageWriter diw = (DicomImageWriter) dicomImageWriterCreator.createWriterInstance();
	  OutputStream os = response.getOutputStream();
	  MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(os); 
	  diw.setOutput(mcios);
	  DicomStreamMetaData dsmd = new DicomStreamMetaData();
	  dsmd.setDicomObject(dsUse);
	  log.debug("Preparing to write sequence for "+sop+" with " + dsUse.size() + " items.");
	  diw.prepareWriteSequence(dsmd);

	  try {
		 if (this.frames != null) {
			log.debug("Writing "+this.frames.size()+" images for sop "+sop);
			for (int frameNumber : this.frames) {
			   frameToSequence(diw, frameNumber);
			}
		 } else {
			log.debug("Writing "+numberOfFrames+" images for sop "+sop);
			for (int frameNumber = 1; frameNumber <= numberOfFrames; frameNumber++) {
			   frameToSequence(diw, frameNumber);
			}
		 }
	  } catch (Exception e) {
		 log.error("Caught exception:", e);
		 return;
	  }

	  log.debug("Completed writing image items to "+sop+" output "+response);
	  diw.endWriteSequence();
	  mcios.close();
	  os.close();
   }

   /**
     * Writes the given frame to the output sequence
     * 
     * @param frameNumber
     */
   protected void frameToSequence(DicomImageWriter diw, int frameNumber) throws IOException {
	  log.debug("Writing image " + sop+"["+frameNumber+"]");
	  WadoImage img = readImage(frameNumber);
	  if (img == null)
		 throw new NullPointerException("Image " + frameNumber + " should not be null.");
	  if (img.getValue() != null) {
		 WadoImage wi = (WadoImage) img;
		 diw.writeToSequence(new IIOImage(wi.getValue(), null, null), null);
	  } else if (img.getParameter(WadoImage.IMG_AS_BYTES) != null) {
		 byte[] bimg = (byte[]) img.getParameter(WadoImage.IMG_AS_BYTES);
		 diw.writeBytesToSequence(bimg, null);
	  } else {
		 throw new IllegalArgumentException("Type of image isn't valid for writing to sequence:" + img.getClass());
	  }
   }

   /**
     * Reads the image from the filterItem. Tries to read it as raw bytes if the
     * transfer syntax matches, and there aren't any confounding parameters.
     * 
     * @param frameNumber
     * @return
     */
   protected WadoImage readImage(int frameNumber) {
	  if( readRaw ) {
		 log.info("Reading raw bytes.");
		 FilterUtil.addToQuery(params, WadoImage.FRAME_NUMBER, Integer.toString(frameNumber), WadoImage.IMG_AS_BYTES, "true");
	  } else{
		 log.info("Reading images.");
		 FilterUtil.addToQuery(params, WadoImage.FRAME_NUMBER, Integer.toString(frameNumber));
	  }
	  return wadoImageFilter.filter(null, params);
   }
}
