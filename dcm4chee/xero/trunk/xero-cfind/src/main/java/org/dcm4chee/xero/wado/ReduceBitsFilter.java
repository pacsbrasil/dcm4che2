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
package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.ShortLookupTable;
import org.dcm4che2.image.VOIUtils;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter reduces the number of stored bits, down to 12 bits so that the
 * image can be encoded using 12 bit JPEG. Also adds response headers to
 * indicate the transformation.
 * 
 * @author bwallace
 */
public class ReduceBitsFilter implements Filter<WadoImage> {
   public static final String RESPONSE_HEADERS = "responseHeaders";

   public static final String LARGEST_IMAGE_PIXEL_VALUE = "LargestImagePixelValue";

   public static final String SMALLEST_IMAGE_PIXEL_VALUE = "SmallestImagePixelValue";

   private static final Logger log = LoggerFactory.getLogger(ReduceBitsFilter.class);

   static ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);

   static int[] bits12 = new int[] { 12 };

   static ColorModel cm12 = new ComponentColorModel(gray, bits12, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_USHORT);

   /**
    * Reduces the number of bits from 13-16 to 12, only for contentType=image/jp12
    */
   @SuppressWarnings("unchecked")
   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  WadoImage wi = (WadoImage) filterItem.callNextFilter(params);
	  if( params.containsKey(WadoImage.IMG_AS_BYTES) ) return wi;
	  int bits = FilterUtil.getInt(params,EncodeImage.MAX_BITS,8); 
	  if ( bits<=8 || bits >= 16 ) {
		 log.debug("Not 9..15 bit image - returning wado image directly, bits="+wi.getDicomObject().getInt(Tag.BitsStored));
		 return wi;
	  }
	  long start = System.nanoTime();
	  DicomObject ds = wi.getDicomObject();
	  int stored = ds.getInt(Tag.BitsStored);
	  if (stored <= bits) {
		 log.debug("Only "+stored+" bits -not decimating window level.");
		 return wi;
	  }
	  WritableRaster r = wi.getValue().getRaster();
	  // Check to see if this has already been done.
	  if (r.getSampleModel().getSampleSize(0) <= bits) {
		 log.warn("Hmm - actual sample model is already smaller, just returning.");
		 return wi;
	  }
	  boolean signed = ds.getInt(Tag.PixelRepresentation) == 1;
	  int smallest = ds.getInt(Tag.SmallestImagePixelValue);
	  int largest = ds.getInt(Tag.LargestImagePixelValue);
	  if (smallest >= largest) {
		 int[] mm = VOIUtils.calcMinMax(ds, r.getDataBuffer());
		 smallest = mm[0];
		 largest = mm[1];
		 ds.putInt(Tag.SmallestImagePixelValue, VR.IS, smallest);
		 ds.putInt(Tag.LargestImagePixelValue, VR.IS, largest);
	  }

	  // TODO handle number of bits !=12  - for now, just go to 12 bits only.
	  if (smallest >= 0 && largest < 4096) {
		 // This is actually just a 12 bit raw image - don't need to
         // transcode it.
		 ds.putInt(Tag.BitsStored, VR.IS, 12);
		 BufferedImage bi = new BufferedImage(cm12, r, false, null);
		 wi.setValue(bi);
		 log.info("12 bit actual image change " + nanoTimeToString(System.nanoTime() - start));

		 return wi;
	  }

	  BufferedImage bi = new BufferedImage(cm12, r, false, null);
	  int range = largest - smallest;
	  int entries = range + 1;
	  short[] slut = new short[entries];
	  for (int i = 0; i < entries; i++) {
		 slut[i] = (short) ((4095 * i) / range);
	  }
	  LookupTable lut = new ShortLookupTable(stored, signed, -smallest, 12, slut);
	  lut.lookup(r.getDataBuffer(), r.getDataBuffer());

	  WadoImage wiRet = wi.clone();
	  wiRet.setValue(bi);
	  log.info("12 bit scaled " + nanoTimeToString(System.nanoTime() - start)+" small/large="+smallest+","+largest);
	  Collection<String> headers = (Collection<String>) wiRet.getParameter(RESPONSE_HEADERS);
	  if( headers==null ) {
		 headers = new HashSet<String>(2);
		 wiRet.setParameter(RESPONSE_HEADERS, headers);
	  }
	  headers.add(SMALLEST_IMAGE_PIXEL_VALUE);
	  headers.add(LARGEST_IMAGE_PIXEL_VALUE);
	  
	  wiRet.setParameter(SMALLEST_IMAGE_PIXEL_VALUE, ""+smallest);
	  wiRet.setParameter(LARGEST_IMAGE_PIXEL_VALUE, ""+largest);

	  return wiRet;
   }

}
