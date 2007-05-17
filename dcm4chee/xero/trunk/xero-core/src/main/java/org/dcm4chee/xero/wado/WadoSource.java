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
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WadoSource reads the object specified in the search criteria as a Java
 * BufferedImage object from another WADO provider (which is configurable). It
 * also supplies the minimum and maximum values, and the default window level
 * values.
 * 
 * This class is a temporary implementation used until the Dcm4che2 version is available.
 * 
 * @author bwallace
 * 
 */
public class WadoSource implements Filter<WadoImage> {
	private static Logger log = LoggerFactory.getLogger(WadoSource.class);
	
	protected String wadoArgs[] = new String[]{
			"studyUID",
			"seriesUID", 
			"objectUID",
			"rows",
			"columns",
			"region",
			"windowCenter",
			"windowWidth",
			"frameNumber"
	};
	protected static final String wadoRequired[] = new String[]{
			"studyUID",
			"seriesUID", 
			"objectUID",
	};
	
	/**
	 * Using the query string, this method looks up the object against the
	 * remote DICOM service, and provides a buffered image.
	 */
	public WadoImage filter(FilterItem filterItem, Map<String, Object> args) {
		String newWadoReq = createWadoUrl(args);
		try {
			long start = System.currentTimeMillis();
			URL url = new URL(newWadoReq);
			BufferedImage image = ImageIO.read(url);
			long dur = System.currentTimeMillis()-start;
			log.info("Initial WADO load took "+dur+" ms on URL:"+url);
			// TODO - remove this once we can directly read the raw data...
			image = convertToByteGray(image);
			// TODO - get actual values from the dicom header instead of constants 0 and 65536
			WadoImage ret = new WadoImage(image, newWadoReq, "0", "65536");
			return ret;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** Figures out the URL to use for the WADO request */
	protected String createWadoUrl(Map<String, ?> args) {
		StringBuffer ret = new StringBuffer(
				"http://localhost:8080/wado?requestType=WADO");
		for(String key : wadoRequired ) {
			Object value = args.get(key);
			if( value==null || value.toString().equals("")) {
				throw new IllegalArgumentException("Required value "+key+" is missing for request.");
			}
		}
		for(String key : wadoArgs) {
			Object value = args.get(key);
			if( value!=null ) {
				ret.append("&").append(key).append("=").append(value);
			}
		}
		return ret.toString();
	}

	/** This method artificially converts the image to 16 bits.
	 * @deprecated This shouldn't really be used - it is for testing only.
	 */
	static public BufferedImage convertToUShortGray(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		log.debug("Source image " + width+" x "+height);
		BufferedImage ret = new BufferedImage(image.getWidth(), image
				.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
		WritableRaster raster = ret.getRaster();
		int[] pix = new int[1];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = image.getRGB(x, y);
				pix[0] = (rgb & 0xFF) << 8;
				raster.setPixel(x, y, pix);
				//if( y==255 ) log.info("Pixel "+x+","+y+"="+pix[0]+" was "+Integer.toString(rgb,16));
			}
		}
		return ret;
	}

	/** This method artificially converts the image to 8 bits.
	 * @deprecated This shouldn't really be used - it is for testing only.
	 */
	static public BufferedImage convertToByteGray(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		log.debug("Source image " + width+" x "+height);
		BufferedImage ret = new BufferedImage(image.getWidth(), image
				.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = ret.getRaster();
		int[] pix = new int[1];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = image.getRGB(x, y);
				pix[0] = (rgb & 0xFF);
				raster.setPixel(x, y, pix);
				//if( y==255 ) log.info("Pixel "+x+","+y+"="+pix[0]+" was "+Integer.toString(rgb,16));
			}
		}
		return ret;
	}
}
