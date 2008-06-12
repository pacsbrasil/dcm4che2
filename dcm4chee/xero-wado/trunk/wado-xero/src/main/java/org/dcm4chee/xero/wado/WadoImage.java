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

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.filter.CacheItem;
import org.dcm4chee.xero.metadata.filter.FilterReturn;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wado image is a buffered image return object that includes methods to
 * retrieve window level information.
 * 
 * @author bwallace
 * 
 */
public class WadoImage extends FilterReturn<BufferedImage> implements CacheItem {
   private static final Logger log = LoggerFactory.getLogger(WadoImage.class);
   
	public static String WINDOW_CENTER = "windowCenter";
	public static String WINDOW_WIDTH = "windowWidth";
	public static String FRAME_NUMBER= "frameNumber";
	
	public static String IMG_AS_BYTES = " asBytes";

	private DicomObject ds;
	private int stored;
	
	/** Create a wado image on the given buffered image object
	 * For images where 1 is black, minValue will be 1, and maxValue 0.
	 * minValue and maxValue should be the min/max AFTER the previous lookup table is applied,
	 * or the raw min/max values in the source data if no lookups have yet been applied.
	 * @param value is the source image
	 * @param is the current query information (key to find this object)
	 * @param minValue is the minimum pixel value, corresponding to ushort internal value 0
	 * @param maxVluae is the maximum pixel value, corresponding to ushort 2^n-1  
     */
	public WadoImage(DicomObject ds, int stored, BufferedImage value) {
		super(value);
		this.ds = ds;
		this.stored = stored;
	}

	/** Used for clone operations */
	protected WadoImage(WadoImage fr) {
		super(fr);
		this.ds = fr.ds;
		this.stored = fr.stored;
	}


	/** Clone this object */
	public WadoImage clone() {
		WadoImage ret = new WadoImage(this);
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
		log.debug("Size from parameter map only ",size);
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
			log.debug("Size from parameter map + image ",size);
		}
	    byte[] raw = (byte[]) getParameter(IMG_AS_BYTES);
		if( raw!=null ) {
		   size += raw.length;
		   log.debug("Size adding from image as bytes ",size);
		}
		return size;
	}

	/** Splits region into sub-parts 
	 */
	public static double[] splitRegion(String region) {
		return FilterUtil.splitDouble(region,4);
	}

	/** Returns the dicom object associated with this image */
   public DicomObject getDicomObject() {
      return ds;
   }

   /** Returns the number of bits stored for this image.  May not agree with the header value
    * if this is computed based on other factors to get the right value.
    */
   public int getStored() {
      return stored;
   }

}
