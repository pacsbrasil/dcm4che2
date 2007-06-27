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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;
import org.dcm4che.imageio.plugins.DcmImageReadParam;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes a URL to either a file location or another WADO service, and uses it to read an image, providing the raw, buffered
 * image data (without modalty LUT/rescale slope & intercept, and without window levelling).  It DOES handle the region encoding and resolution 
 * (rows/cols) fields, as this needs to be done as part of the initial read.
 * @author bwallace
 */
public class DicomImageFilter implements Filter<WadoImage> {
	public static final String COLOR_MODEL_PARAM = "ColorModelParam";
	private static Logger log = LoggerFactory.getLogger(DicomImageFilter.class);
	private ColorModelFactory colorModelFactory = ColorModelFactory.getInstance();

	/** Read the raw DICOM image object 
	 * @return WADO image.
	 */
	public WadoImage filter(FilterItem filterItem, Map<String, Object> params) {
		URL location = (URL) filterItem.callNamedFilter("fileLocation", params);
		if (location == null)
			return null;
		WadoImage ret = null;
		Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
		if (!it.hasNext())
			throw new UnsupportedOperationException(
					"The DICOM image I/O filter must be available to read images.");
		log.debug("Found DICOM image reader - trying to read image now.");
		ImageReader reader = (ImageReader) it.next();
		DcmImageReadParam param = (DcmImageReadParam) reader.getDefaultReadParam();
		param.setAutoWindowing(true);
		ImageInputStream in = null;
		try {
			String surl = location.toString();
			if (surl.startsWith("file:") ) {
				String fileName = location.getFile();
				log.info("Reading DICOM image from local cache file "+surl);
				in = new FileImageInputStream(new File(fileName));
			} else {
				// TODO change to FileCacheInputStream once we can configure the locaiton.
				log.info("Reading DICOM image from remote WADO url:"+surl);
				in = new MemoryCacheImageInputStream(location.openStream());
			}
			reader.setInput(in);
			int width = reader.getWidth(0);
			int height = reader.getHeight(0);
			updateParamFromRegion(param, params, width, height);
			BufferedImage bi = reader.read(0,param);
			ret = new WadoImage(bi,null,null);
            Dataset data = ((DcmMetadata) reader.getStreamMetadata())
            .getDataset();
            // It would be nice to get this from the underlying image data, but it isn't clear
            // how to do that.
            try {
            	ColorModelParam cmParam = colorModelFactory.makeParam(data);
            	ret.setParameter(COLOR_MODEL_PARAM, cmParam);
            } catch(UnsupportedOperationException e) {
            	log.warn("Can't make color model parameter for some images.");
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
	
	/** Compute the source, sub-sampling and destination regions appropriately from the
	 * region, rows and cols in params.  
	 * @param read
	 * @param params
	 * @param width
	 * @param height
	 */
	protected void updateParamFromRegion(DcmImageReadParam read, Map<String,Object> params, int width, int height) {
		String region = (String) params.get("region");
		String sRows = (String) params.get("rows");
		String sCols = (String) params.get("cols");
		int xOffset = 0;
		int yOffset = 0;
		int sWidth = width;
		int sHeight = height;

		if( region!=null ) {
			// Figure out the sub-region to use
			double[] dregion = WadoImage.splitRegion(region);
			xOffset = (int) (dregion[0] * width);
			yOffset = (int) (dregion[1] * height);
			sWidth = (int) ((dregion[2] - dregion[0] ) * width);
			sHeight = (int) ((dregion[3] - dregion[1]) * height);
			Rectangle rect = new Rectangle(xOffset, yOffset, sWidth, sHeight);
			read.setSourceRegion(rect);
			log.info("Source region "+rect);
		}
		
		if( sRows==null && sCols==null ) return;

		// Now figure out the size of the final region
		int subsampleX = 1;
		int subsampleY = 1;
		if( sCols!=null ) {
			subsampleX = sWidth / Integer.parseInt(sCols);
			subsampleY = subsampleX;
		}
		if( sRows!=null ) {
			subsampleY = sHeight / Integer.parseInt(sRows);
			if( sCols==null ) subsampleX = subsampleY;
		}
		// Can't over-sample the data...
		if( subsampleX<1 ) subsampleX = 1;
		if( subsampleY<1 ) subsampleY = 1;
		if( subsampleX==1 && subsampleY==1 ) return;
		log.info("Sub-sampling "+subsampleX+","+subsampleY + " sHeight="+sHeight);
		read.setSourceSubsampling(subsampleX, subsampleY, 0,0);
	}

	/** Get the default priority for this filter. */
	@MetaData
	public int getPriority() {
		return 0;
	}

}
