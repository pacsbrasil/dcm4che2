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
import java.util.Map;

import org.dcm4chee.xero.display.ZoomPanAction;
import org.dcm4chee.xero.metadata.filter.CacheItem;
import org.dcm4chee.xero.metadata.filter.FilterReturn;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;

/**
 * A wado image is a buffered image return object that includes methods to
 * retrieve window level information.
 * 
 * @author bwallace
 * 
 */
public class WadoImage extends FilterReturn<BufferedImage> implements CacheItem {
	public static String WINDOW_CENTER = "windowCenter";
	public static String WINDOW_WIDTH = "windowWidth";
	/** The current pixel range from min being darkest to max being brightest.
	 * corresponding to 0 and 2^n-1
	 */
	public static String MIN_VALUE = "minValue";
	public static String MAX_VALUE = "maxValue";

	/** Create a wado image on the given buffered image object
	 * For images where 1 is black, minValue will be 1, and maxValue 0.
	 * minValue and maxValue should be the min/max AFTER the previous lookup table is applied,
	 * or the raw min/max values in the source data if no lookups have yet been applied.
	 * @param value is the source image
	 * @param is the current query information (key to find this object)
	 * @param minValue is the minimum pixel value, corresponding to ushort internal value 0
	 * @param maxVluae is the maximum pixel value, corresponding to ushort 2^n-1  
     */
	public WadoImage(BufferedImage value, String minValue,
			String maxValue) {
		super(value);
		setParameter(MIN_VALUE, minValue);
		setParameter(MAX_VALUE, maxValue);
	}

	/** Used for clone operations */
	protected WadoImage(WadoImage fr) {
		super(fr);
	}

	/** Get the window width */
	public double getWindowWidth() {
		double d = getParameter(WINDOW_WIDTH,Double.NaN);
		if( Double.isNaN(d) ) return getMaxValue()-getMinValue();
		return d;
	}

	/** Get the window center */
	public double getWindowCenter() {
		double d = getParameter(WINDOW_CENTER,Double.NaN);
		if( Double.isNaN(d) ) return (getMaxValue()+getMinValue())/2;
		return d;
	}

	/**
	 * The min value corresponds to the 0 pixel value - however, it does not
	 * need to be an integer.
	 * 
	 * @return
	 */
	public double getMinValue() {
		return getParameter(MIN_VALUE,0.0);
	}

	/** The max value corresponds to 2^bits-1 for the pixel values. */
	public double getMaxValue() {
		return getParameter(MAX_VALUE,1.0);
	}

	/** Clone this object */
	public WadoImage clone() {
		WadoImage ret = new WadoImage(this);
		return ret;
	}

	/**
	 * This class removes the provided strings from the query string, updating
	 * the map in place.
	 */
	public static Object[] removeFromQuery(Map<String, Object> map,
			String... removals) {
		Object[] ret = new Object[removals.length];
		String queryStr = (String) map.get(MemoryCacheFilter.KEY_NAME);
		if (queryStr == null)
			throw new IllegalArgumentException("Initiale query string must not be null.");
		boolean removed = false;
		StringBuffer sb = new StringBuffer(queryStr);
		int i=0;
		for (String remove : removals) {
			ret[i++] = map.remove(remove);
			int pos = sb.indexOf(remove + "=");
			if (pos == -1)
				continue;
			int end = pos + remove.length();
			if (pos > 0) {
				if (sb.charAt(pos - 1) != '&')
					continue;
				pos--;
			}
			if (end < sb.length()) {
				int nextAmp = sb.indexOf("&", end);
				if (nextAmp == -1)
					end = sb.length();
				else
					end = nextAmp;
			}
			sb.delete(pos, end);
			removed = true;
		}
		// Clean it up at the beginning and end
		if (sb.length() > 0) {
			if (sb.charAt(0) == '&') {
				sb.delete(0, 1);
				removed = true;
			}
			if (sb.charAt(sb.length() - 1) == '&') {
				sb.delete(sb.length()-1, sb.length());
				removed = true;
			}
		}
		if (removed) {
			queryStr = sb.toString();
			map.put(MemoryCacheFilter.KEY_NAME, queryStr);
		}
		return ret;
	}

	/**
	 * Figure out the size of this object - somewhat of a hueristic, as we don't
	 * want to go through all the map data, but assuming that the map isn't too
	 * large, then the size is some multiple of the parameter map size, plus a
	 * multiple of the width and height of the pixel data.
	 * @return number of bytes that this image takes up, plus heuristic amount of bytes for space for the parameters.
	 */
	public long getSize() {
		long size = getParameterMap().size() * 128l;
		BufferedImage image = getValue();
		if (image != null) {
			int width = image.getWidth();
			int height = image.getHeight();
			int bits = image.getColorModel().getPixelSize();
			int channels = image.getColorModel().getNumComponents();
			int totalBits = bits * channels;
			if (totalBits <= 8)
				size += width * height;
			else if (totalBits <= 16)
				size += width * height * 2;
			else if (totalBits <= 32)
				size += width * height * 2;
			else
				size += width * height * channels * 2;
		}
		return size;
	}

	/** Splits region into sub-parts 
	 */
	public static double[] splitRegion(String region) {
		return splitDouble(region,4);
	}

	/** Splits region into sub-parts */
	public static double[] splitDouble(String region, int size) {
		ZoomPanAction.log.info("Trying to split '"+region+"'");
		double ret[] = new double[size];
		int start = 0;		
		region = region.trim();
		for(int i=0; i<ret.length; i++ ) {
			if( start>=region.length() ) throw new IllegalArgumentException("Too few arguments in "+region);
			int end = region.indexOf(',',start);
			if( end<0 ) end = region.length();
			ret[i] = Double.parseDouble(region.substring(start,end));
			start = end+1;
		}
		if( start<region.length() ) throw new IllegalArgumentException("Too many arguments in "+region);
		return ret;
	}
}
