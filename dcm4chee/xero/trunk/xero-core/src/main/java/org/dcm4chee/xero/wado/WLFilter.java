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

import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorModel;
import java.awt.image.LookupOp;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.wado.WadoImage.WINDOW_WIDTH;
import static org.dcm4chee.xero.wado.WadoImage.WINDOW_CENTER;

/**
 * Window level the WadoImage.
 * This always converts down to 8 bit images, and handles up to 16 bit
 * incoming images, in both RGB and Gray.  At some point L*a*b* should also
 * be handled by window levelling L* only.
 * @author bwallace
 *
 */
public class WLFilter implements Filter<WadoImage> {
	private static Logger log = LoggerFactory.getLogger(WLFilter.class);
	
	public static final double MIN_WIDTH = 0.0001;

	/** Read a WadoImage object from the next filter, and window level it,
	 * using the minimum/maximum pixel values, and the provided window level.
     * @param filterItem used to get the image to window level
     * @param map is the parameters used to get the initial image, and then
     * to provide window width and center.
     * @return WadoImage or null if it isn't found.
	 */
	public WadoImage filter(FilterItem filterItem, Map<String, Object> map) {
		Object[] values = WadoImage.removeFromQuery(map,WINDOW_WIDTH, WINDOW_CENTER);
		WadoImage wi = (WadoImage) filterItem.callNextFilter(map);
		if( wi==null ) return null;
		BufferedImage bi = wi.getValue();
		int bits = getBitsPerPixel(bi);
		if( bits==0 ) {
			log.warn("Image cannot be window levelled, as it has different bit lengths: " + map.get(MemoryCacheFilter.KEY_NAME) );
			return wi;

		}
		log.debug("Number of bits for WL filter is "+bits);
		long start = System.currentTimeMillis();
		if( bi==null ) return null;
		BufferedImage dest = createCompatible8BitImage(bi);
		double minValue = wi.getMinValue();
		double maxValue = wi.getMaxValue();
		if( minValue==maxValue ) throw new IllegalArgumentException("min and max pixel values must be different.");
		
		// Get the window width/center information, defaulting to show everything.
		double windowWidth = wi.getWindowWidth();
		double windowCenter = wi.getWindowCenter();
		if( values[0]!=null ) windowWidth = Double.parseDouble((String) values[0]);
		if( values[1]!=null ) windowCenter = Double.parseDouble((String) values[1]);
		log.debug("WL  W:"+windowWidth+" C:"+windowCenter + " pixel range: ["+minValue+"-"+maxValue+")");
		LookupOp op = computeLookupOp(bits,windowWidth, windowCenter, minValue, maxValue);
		
		op.filter(bi,dest);
		WadoImage ret = wi.clone();
		ret.setValue(dest);
		long dur = System.currentTimeMillis()-start;
		log.info("Window levelling took "+dur+" ms");
		return ret;
	}
	
	/** Returns the number of bits per pixel, assuming the values are constant and uniform. 
	 * WARNING: If more standard types are defined that are indexed, or anything where luminance
	 * isn't evenly spread across channels, then this will need to be updated.
	 * 
	 * @param bi to get bits per pixel from.
	 * @return number of bits per pixel, assumign they are all  the same (excluding alpha), or 0 otherwise.
	 */
	public static int getBitsPerPixel(BufferedImage bi) {
		if( bi==null ) throw new NullPointerException("Buffered image must not be null.");
		ColorModel cm = bi.getColorModel();
		int type = bi.getType();
		if( type==BufferedImage.TYPE_CUSTOM || type==BufferedImage.TYPE_BYTE_INDEXED  || type==BufferedImage.TYPE_USHORT_565_RGB) return 0;
		if( cm.getNumComponents()==1 ) {
			return cm.getComponentSize(0);
		}
		// The second component is never the alpha, so it should be safe to use.
		return cm.getComponentSize(1);
	}

	/** This method creates an 8 bit compatible, default buffered image.  Only
	 * gray and RGB images are currently supported. 
	 * @param src is the image data to start with and to create the window levelled image from.
	 */
	protected BufferedImage createCompatible8BitImage(BufferedImage src) {
		if( src.getColorModel().getNumComponents()==1 ) {
			return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		}
		else {
			return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		}
	}
	
	/**
	 * Computes a lookupOp for the window level, based on the given window width/center values,
	 * and the min/max values.  The assumption is that 0 corresponds to minValue and that
	 * 2^n-1 corresponds to max value.  This will still work fine if min and max are
	 * exchanged, for a negative type image.
	 * The assumption is that the OUTPUT is always 0..255
	 * @param bits is how many bits in the source image
	 * @param windowWidth
	 * @param windowCenter
	 * @param minVlaue is the minimum pixel input value
	 * @param maxValue is the maximum pixel input value
	 * @return a lookup table to use to apply window level to the image.
	 */
	LookupOp computeLookupOp(int bits, double windowWidth, double windowCenter, 
			double minValue, double maxValue) {
		if (windowWidth < MIN_WIDTH)
			windowWidth = MIN_WIDTH;
		if( bits < 0 || bits>16 ) throw new UnsupportedOperationException("Number of bits must be between 1 and 16, but is "+bits);
		byte[] data = new byte[1 << bits];
		// Multiply by this value to convert from pixel values to raw ushort internal values
		double pixWidthConvert =  data.length/(maxValue - minValue);
		// Compute the start and end rather than center and width
		double winStart = windowCenter - windowWidth/2;
		double winEnd = winStart + windowWidth;

		// The raw values - in terms of the ushort values, but still doubles.
		double winStartRaw = (winStart - minValue) * pixWidthConvert;
		double winEndRaw = (winEnd - minValue) * pixWidthConvert;
		double winWidthRaw = windowWidth * pixWidthConvert;
		
		int start = Math.max((int) winStartRaw, 0);
		// The item AFTER the last one.
		int end = Math.min((int) winEndRaw + 1, data.length);
		
		log.debug("winStart="+winStart+" winEnd="+winEnd);
		log.debug("bits="+bits+" start="+start+" end="+end+" winStartRaw="+winStartRaw+" winWidthRaw="+winWidthRaw+" pixWidthConvert="+pixWidthConvert);
		log.debug("window level W:"+windowWidth+" C:"+windowCenter+" pixel range:["+minValue+","+maxValue+")");
		for (int i = start; i < end; i++) {
			double value = 256.0 * (i - winStartRaw) / winWidthRaw;
			if (value < 0)
				value = 0;
			else if (value > 255)
				value = 255;
			// Convert to integer first
			int ivalue = (int) value;
			// Convert to byte after converting to integer becausing the
			// underlying value is unsigned.
			data[i] = (byte) ivalue;
			//if( i % 64 == 0 ) System.out.println("Mapping "+i+" to "+ivalue);
		}
		for (int i = end; i < data.length; i++) {
			data[i] = (byte) 255;
		}
		return new LookupOp(new ByteLookupTable(0, data), null);
	}

}
