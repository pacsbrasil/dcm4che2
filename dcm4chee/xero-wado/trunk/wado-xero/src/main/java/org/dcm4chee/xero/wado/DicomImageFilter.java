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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageReadParam;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.image.OverlayUtils;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.metadata.filter.FilterUtil.getFloats;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getInt;
import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

/**
 * This class takes a URL to either a file location or another WADO service, and
 * uses it to read an image, providing the raw, buffered image data (without
 * modalty LUT/rescale slope & intercept, and without window levelling). It DOES
 * handle the region encoding and resolution (rows/cols) fields, as this needs
 * to be done as part of the initial read.
 * 
 * @author bwallace
 */
public class DicomImageFilter implements Filter<WadoImage> {
   public static final String FRAME_NUMBER = "frameNumber";

   public static final String COLOR_MODEL_PARAM = "ColorModelParam";

   private static Logger log = LoggerFactory.getLogger(DicomImageFilter.class);

   /**
     * Read the raw DICOM image object
     * 
     * @return WADO image.
     */
   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  WadoImage ret = null;
	  DicomImageReader reader = DicomFilter.filterDicomImageReader(filterItem, params, null);
	  boolean readRawBytes = params.containsKey(WadoImage.IMG_AS_BYTES);
	  if (reader == null) {
		 log.warn("Couldn't find reader for DICOM object.");
		 return null;
	  }
	  // In case a size has been added, remove it as the size is changing.
	  params.remove(MemoryCacheFilter.CACHE_SIZE);
	  ImageReadParam param = reader.getDefaultReadParam();
	  DicomImageReadParam dParam = (DicomImageReadParam) param;
	  dParam.setOverlayRGB((String) params.get("rgb"));

	  String strFrame = (String) params.get(FRAME_NUMBER);
	  int frame = 0;
	  if (strFrame != null) {
		 frame = Integer.parseInt(strFrame) - 1;
		 // Overlays are specified as the actual overlay number, not as an
		 // offset value.
		 if ((frame & 0x60000000) != 0)
			frame++;
	  }
	  try {
		 long start = System.nanoTime();
		 String op = "decompress";
		 synchronized (reader) {
			int width = reader.getWidth(0);
			int height = reader.getHeight(0);
			updateParamFromRegion(param, params, width, height);
			BufferedImage bi;
			DicomStreamMetaData streamData = (DicomStreamMetaData) reader.getStreamMetadata();
			DicomObject ds = streamData.getDicomObject();
			ret = new WadoImage(streamData.getDicomObject(), ds.getInt(Tag.BitsStored), null);
			if (readRawBytes) {
			   byte[] img = reader.readBytes(frame, param);
			   ret.setParameter(WadoImage.IMG_AS_BYTES, img);
			   bi = null;
			   op="read raw";
			} else if (OverlayUtils.isOverlay(frame) || !ColorModelFactory.isMonochrome(ds)) {
			   // This object can't be window levelled, so just get it as
			   // a buffered image directly.
			   bi = reader.read(frame, param);
			   log.debug("ColorSpace of returned buffered image is " + bi.getColorModel().getColorSpace());
			   op="read overlay/colour";
			}  else {
			   WritableRaster r = (WritableRaster) reader.readRaster(frame, param);
			   ColorModel cm = ColorModelFactory.createColorModel(ds);
			   log.debug("Color model for image is " + cm);
			   bi = new BufferedImage(cm, r, false, null);
			}
		    Object input = reader.getInput();
			if( ds.getInt(Tag.NumberOfFrames,1)==1 && (input instanceof ReopenableImageInputStream) ) {
			   log.info("Closing re-openable stream for {}",params.get("objectUID"));
			  ((ReopenableImageInputStream) input).close();
			}
			else log.info("Not closing re-openable multi-frame stream {}",params.get("objectUID"));

			ret.setValue(bi);
			log.info("Time to "+op+" image "+params.get("objectUID")+" ts=" + ds.getString(Tag.TransferSyntaxUID) + " only is "
				  + nanoTimeToString(System.nanoTime() - start));
		 }
	  } catch (IOException e) {
		 log.error("Caught I/O exception reading image.", e);
	  }
	  // This might happen if the instance UID under request comes from
	  // another
	  // system and needs to be read in another way.
	  if (ret == null)
		 return (WadoImage) filterItem.callNextFilter(params);
	  return ret;
   }

   /**
     * Compute the source, sub-sampling and destination regions appropriately
     * from the region, rows and cols in params.
     * 
     * @param read
     * @param params
     * @param width
     * @param height
     */
   protected void updateParamFromRegion(ImageReadParam read, Map<String, Object> params, int width, int height) {
	  float[] region = getFloats(params, "region", null);
	  int rows = getInt(params, "rows");
	  int cols = getInt(params, "cols");
	  log.debug("DicomImageFilter rows=" + rows + " cols=" + cols);
	  int xOffset = 0;
	  int yOffset = 0;
	  int sWidth = width;
	  int sHeight = height;

	  if (region != null) {
		 // Figure out the sub-region to use
		 xOffset = (int) (region[0] * width);
		 yOffset = (int) (region[1] * height);
		 sWidth = (int) ((region[2] - region[0]) * width);
		 sHeight = (int) ((region[3] - region[1]) * height);
		 Rectangle rect = new Rectangle(xOffset, yOffset, sWidth, sHeight);
		 read.setSourceRegion(rect);
		 log.debug("Source region " + rect);
	  }

	  if (rows == 0 && cols == 0)
		 return;

	  // Now figure out the size of the final region
	  int subsampleX = 1;
	  int subsampleY = 1;
	  if (cols != 0) {
		 subsampleX = sWidth / cols;
		 subsampleY = subsampleX;
	  }
	  if (rows != 0) {
		 subsampleY = sHeight / rows;
		 if (cols == 0)
			subsampleX = subsampleY;
	  }
	  // Can't over-sample the data...
	  if (subsampleX < 1)
		 subsampleX = 1;
	  if (subsampleY < 1)
		 subsampleY = 1;
	  if (subsampleX == 1 && subsampleY == 1)
		 return;
	  log.debug("Sub-sampling " + subsampleX + "," + subsampleY + " sHeight=" + sHeight);
	  read.setSourceSubsampling(subsampleX, subsampleY, 0, 0);
   }

   /** Get the default priority for this filter. */
   @MetaData
   public int getPriority() {
	  return 0;
   }

}
