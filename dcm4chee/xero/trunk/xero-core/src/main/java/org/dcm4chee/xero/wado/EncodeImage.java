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
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EncodeImage transforms a WadoImage object into a JPEG, PNG or any other
 * supported image format.
 * @author bwallace
 *
 */
public class EncodeImage implements Filter<ServletResponseItem> {
	private static final float DEFAULT_QUALITY = -1.0f;
	/* Include the WADO parameters that are handled further down the chain - 
	 * These could be explicitly added by handlers, but it is useful to include
	 * them here right now.
	 */
	String[] wadoParameters = new String[]{
			"windowCenter",
			"windowWidth",
			"imageUID",
			"studyUID",
			"seriesUID",
			"objectUID",
			"region",
			"rows",
			"columns",
	};
	
	/** Filter the image by returning an JPEG type image object
	 * @param filterItem is the information about what to filter.
	 * @param map contains the parameters used to determine the encoding type.
	 * @return A response that can be used to write the image to a stream in the
	 * provided encoding type, or image/jpeg if none.
	 */
	public ServletResponseItem filter(FilterItem filterItem, Map<String, Object> map) {
		String contentType = (String) map.get("contentType");
		if( contentType==null ) contentType = "image/jpeg";
		float quality = DEFAULT_QUALITY;
		String sQuality = (String) map.get("imageQuality");
		if( sQuality!=null ) quality = Float.parseFloat(sQuality);
		String queryStr = computeQueryStr(map);
		map.put(org.dcm4chee.xero.metadata.filter.MemoryCacheFilter.KEY_NAME, queryStr);
		WadoImage image = (WadoImage) filterItem.callNextFilter(map);
		return new ImageServletResponseItem(image, contentType, quality);
	}
	
	/** Figures out what the query string should be - in a fixed order, using
	 * just the handled items.
	 * @param map to get the WADO parameters from
	 * @param A URI parameter string with the relevant WADO parameters in it, as defined by wadoParameters
	 */
	String computeQueryStr(Map<String,?> map) {
		StringBuffer ret = new StringBuffer();
		for(String key : wadoParameters) {
			Object value = map.get(key);
			if( value!=null ) {
				ret.append("&").append(key).append("=").append(value);
			}
		}
		return ret.toString();
	}

}

/** Does the actual writing to the stream */
class ImageServletResponseItem implements ServletResponseItem {
	private static Logger log = LoggerFactory.getLogger(ImageServletResponseItem.class);
	String contentType;
	ImageWriter writer;
	ImageWriteParam imageWriteParam;
	WadoImage wadoImage;

	/** Create an image servlet response to write the given image to the response stream.
	 * @param image is the data to write to the stream
	 * @param contentType is the type of image encoding to use (image/jpeg etc) - any available encoder will be used
	 * @param quality is the JPEG lossy quality (may eventually be other types as well, but currently that is the only one available) 
	 * */
	public ImageServletResponseItem(WadoImage image, String contentType, float quality)
	{		
		writer = ImageIO.getImageWritersByMIMEType(contentType).next();
		this.contentType = contentType;
		this.wadoImage = image;
		if( quality >=0f && quality <=1f && contentType.equals("image/jpeg")) {
			imageWriteParam = writer.getDefaultWriteParam();
			imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriteParam.setCompressionType("JPEG");
			imageWriteParam.setCompressionQuality(quality);
		}
	}

	/** Write the response to the provided stream.  Sets the content type and writes to the
	 * output stream.
	 * @param arg0 unused
	 * @param response that the image is written to.  Also sets the content type.
	 */
	public void writeResponse(HttpServletRequest arg0, HttpServletResponse response) 
	throws IOException 
	{
		long start = System.currentTimeMillis();
		response.setContentType(contentType);
		ImageOutputStream ios = ImageIO.createImageOutputStream(response.getOutputStream());
		writer.setOutput(ios);			
		IIOImage iioimage = new IIOImage(wadoImage.getValue(), null, null);			
		writer.write(null,iioimage,imageWriteParam);
		ios.close();
		long dur = System.currentTimeMillis() - start;
		log.info("Encoding image took "+dur+" ms");
	}
	
}
