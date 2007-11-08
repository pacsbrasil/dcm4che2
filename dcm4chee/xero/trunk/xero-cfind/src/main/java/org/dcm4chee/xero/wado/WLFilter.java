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

import static org.dcm4chee.xero.wado.WadoImage.WINDOW_CENTER;
import static org.dcm4chee.xero.wado.WadoImage.WINDOW_WIDTH;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.VOIUtils;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilterBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class applies the DICOM window level operation directly to the pixel
 * data transform, so that no new copy of the data is required.
 * 
 * @author bwallace
 * 
 */
public class WLFilter implements Filter<WadoImage> {
   private static Logger log = LoggerFactory.getLogger(WLFilter.class);

   ColorSpace csGray = ColorSpace.getInstance(ColorSpace.CS_GRAY);

   public static final double MIN_WIDTH = 0.0001;

   /**
    * Used to specify a presentation UID to apply to an image
    */
   public static String PRESENTATION_UID = "presentationUID";
   
   /** Used to flag inverting the image display to true/false.  Modifes the value
    * in the GSPS or image.
    */
   public static String INVERT = "invert";
   
   private static short[] pval2out_inverse = new short[256];
   static {
	 for(int i=0; i<256; i++ )pval2out_inverse[i] = (short) ((255-i) << 8); 
   };

   public WadoImage filter(FilterItem filterItem, Map<String, Object> params) {
	  Object[] values = MemoryCacheFilterBase.removeFromQuery(params, WINDOW_WIDTH, WINDOW_CENTER, PRESENTATION_UID, INVERT);
	  WadoImage wi = (WadoImage) filterItem.callNextFilter(params);
	  if (wi == null) {
		 log.debug("No wado image found.");
		 return null;
	  }
	  if (wi.getDicomObject() == null) {
		 log.debug("Skipping window levelling as no dicom object header found.");
		 return wi;
	  }
	  if( "raw".equals(values[0]) || "raw".equals(values[1])) {
		 log.info("Skipping window level as the RAW image is being requested.");
		 return wi;
	  }

	  long start = System.currentTimeMillis();
	  BufferedImage bi = wi.getValue();

	  short[] pval2out = null;
	  if( "true".equalsIgnoreCase((String) values[3]) ) {
		 pval2out = pval2out_inverse;
	  }
	  
	  // The lookup op, either computed by hand, or retrieved from the colour
	  // model.
	  float windowWidth = 0;
	  float windowCenter = 0;
	  String func = null;
	  try {
		 if (values[0] != null && values[1] != null) {
			windowWidth = Float.parseFloat((String) values[0]);
			windowCenter = Float.parseFloat((String) values[1]);
			func = LookupTable.LINEAR;
		 }
	  } catch (NumberFormatException nfe) {
		 log.debug("Caught number format exception, ignoring and using default values:" + nfe);
	  }
	  log.debug("WL  W:" + windowWidth + " C:" + windowCenter + ")");
	  
	  DicomObject img = wi.getDicomObject();
	  if( img==null ) {
		 log.error("Dicom object must not be null for window-level to work.");
		 return wi;
	  }
	  DicomObject pr = null;
	  if( values[2]!=null ) {
		 pr = DicomFilter.filterDicomObject(filterItem, params, (String) values[2]);
		 if( pr==null ) log.warn("Coudn't find presentation state "+values[2]);
	  }

	  LookupTable lut;

	  int frame = 1;
	  String sFrame = (String) params.get(DicomImageFilter.FRAME_NUMBER);
	  if( sFrame!=null ) {
		 frame = Integer.parseInt(sFrame);
	  }
	  
	  DicomObject voiObj = VOIUtils.selectVoiObject(img,pr,frame);
	  if( voiObj==null ){
		 throw new NullPointerException("voiObj should not be null.");
	  }
	  if( func==null && !VOIUtils.containsVOIAttributes(voiObj)) {
		 float[] cw = VOIUtils.getMinMaxWindowCenterWidth(img, pr, frame, bi.getRaster().getDataBuffer());
		 windowCenter = cw[0];
		 windowWidth = cw[1];
		 func = LookupTable.LINEAR;
	  }

	  lut = LookupTable.createLutForImageWithPR(wi.getDicomObject(), pr, frame, windowCenter, windowWidth, func, 8, pval2out);
	
	  //if (lut != null) {
		// for (int i = 0; i < lut.length(); i += 1 + (lut.length() / 10)) {
		//	int v = i + lut.getOffset();
		//	System.out.println("LUT maps " + v + " to " + lut.lookup(v));
		// }
	  //} else {
		// System.out.println("Null LUT.");
	  //}

	  DataBuffer srcData = bi.getRaster().getDataBuffer();
	  BufferedImage dest = createCompatible8BitImage(bi);
	  // Can't window level if a custom type is being used.
	  if( dest==null ) return wi;
	  DataBuffer destData = dest.getRaster().getDataBuffer();
	  if (lut != null)
		 lut.lookup(srcData, destData);

	  WadoImage ret = wi.clone();
	  ret.setValue(dest);
	  long dur = System.currentTimeMillis() - start;
	  log.info("Window levelling took " + dur + " ms");
	  return ret;
   }

   /**
     * Returns the number of bits per pixel, assuming the values are constant
     * and uniform. WARNING: If more standard types are defined that are
     * indexed, or anything where luminance isn't evenly spread across channels,
     * then this will need to be updated.
     * 
     * @param bi
     *            to get bits per pixel from.
     * @return number of bits per pixel, assumign they are all the same
     *         (excluding alpha), or 0 otherwise.
     */
   public static int getBitsPerPixel(BufferedImage bi) {
	  if (bi == null)
		 throw new NullPointerException("Buffered image must not be null.");
	  ColorModel cm = bi.getColorModel();
	  int type = bi.getType();
	  if (type == BufferedImage.TYPE_CUSTOM || type == BufferedImage.TYPE_BYTE_INDEXED || type == BufferedImage.TYPE_USHORT_565_RGB)
		 return 0;
	  if (cm.getNumComponents() == 1) {
		 return cm.getComponentSize(0);
	  }
	  // The second component is never the alpha, so it should be safe to use.
	  return cm.getComponentSize(1);
   }

   /**
     * This method creates an 8 bit compatible, default buffered image. Only
     * gray and RGB images are currently supported.
     * 
     * @param src
     *            is the image data to start with and to create the window
     *            levelled image from.
     */
   protected BufferedImage createCompatible8BitImage(BufferedImage src) {
	  int type = src.getType();
	  if (src.getColorModel().getNumComponents() == 1) {
		 return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	  } else if( type==BufferedImage.TYPE_INT_RGB || type==BufferedImage.TYPE_3BYTE_BGR ) {
		 return new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
	  }
	  log.warn("Unsupported buffered image type "+type);
	  return null;
   }

}
