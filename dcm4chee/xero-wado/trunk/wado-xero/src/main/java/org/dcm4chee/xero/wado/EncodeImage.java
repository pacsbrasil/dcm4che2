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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

/**
 * EncodeImage transforms a WadoImage object into a JPEG, PNG or any other
 * supported image format.
 * 
 * @author bwallace
 * 
 */
public class EncodeImage implements Filter<ServletResponseItem> {
   private static final Logger log = LoggerFactory.getLogger(EncodeImage.class);
   public static final String MAX_BITS = "maxBits";

   private static final float DEFAULT_QUALITY = -1.0f;

   protected static Map<String, EncodeResponseInfo> contentTypeMap = new HashMap<String, EncodeResponseInfo>();
   // TODO - replace the bit information with a set of additional params requested....
   static {
	  new EncodeResponseInfo("image/jp12", "image/jpeg", true, 12, null, UID.JPEGExtended24);
	  new EncodeResponseInfo("image/jpls", "image/jpeg", false, 16, "JPEG-LS", UID.JPEGLSLossless, UID.JPEGLSLossyNearLossless);
	  new EncodeResponseInfo("image/jpll", "image/jpeg", false, 16, "JPEG-LOSSLESS", UID.JPEGLossless);
	  new EncodeResponseInfo("image/png", null, false, 0, null);
	  new EncodeResponseInfo("image/png16", "image/png", false, 16, null);
	  // image/jpeg is the default, so add a image/* as an additional mapping.
	  new EncodeResponseInfo("image/jpeg", null, true, 0, null, UID.JPEGBaseline1, "image/*");
	  new EncodeResponseInfo("image/jp2", null, false, 16, null, UID.JPEG2000, UID.JPEG2000LosslessOnly);
	  new EncodeResponseInfo("image/gif", null, false, 0, null);
	  new EncodeResponseInfo("image/bmp", null, false, 0, null);
   };

   /*
     * Include the WADO parameters that are handled further down the chain -
     * These could be explicitly added by handlers, but it is useful to include
     * them here right now.
     */
   String[] wadoParameters = new String[] { "windowCenter", "windowWidth", "imageUID", "studyUID", "seriesUID", "objectUID",
		 "frameNumber", "rgb", // Could be re-calculated fairly easily, but
		 // right now it isn't worthwhile.
		 "region", "rows", "cols", "presentationUID", };

   /**
     * Filter the image by returning an JPEG type image object
     * 
     * @param filterItem
     *            is the information about what to filter.
     * @param map
     *            contains the parameters used to determine the encoding type.
     * @return A response that can be used to write the image to a stream in the
     *         provided encoding type, or image/jpeg if none.
     */
   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> map) {
	  String contentType = (String) map.get("contentType");
	  if (contentType == null)
		 contentType = "image/jpeg";
	  DicomObject ds = DicomFilter.filterImageDicomObject(filterItem, map, null);
	  if( ds!=null && !ds.contains(Tag.PixelRepresentation) ) {
		 log.info("DICOM does not contain pixel representation.");
		 return filterItem.callNextFilter(map);
	  }

	  float quality = DEFAULT_QUALITY;
	  String sQuality = (String) map.get("imageQuality");
	  if (sQuality != null)
		 quality = Float.parseFloat(sQuality);
	  String queryStr = computeQueryStr(map);
	  map.put(org.dcm4chee.xero.metadata.filter.MemoryCacheFilter.KEY_NAME, queryStr);
	  EncodeResponseInfo eri = contentTypeMap.get(contentType);
	  boolean multipleEncoding = contentType.indexOf(',')>=0;
	  if ((eri != null && eri.maxBits > 8) || multipleEncoding) {
		 String tsuid = ds.getString(Tag.TransferSyntaxUID);
		 EncodeResponseInfo tsEri = contentTypeMap.get(tsuid);
		 log.info("Source tsuid="+tsuid);
		 if (tsEri != null && contentType.indexOf(tsEri.mimeType) >= 0) {
			contentType = tsEri.mimeType;
			log.info("Trying to read raw image for ",map.get("objectUID"));
			MemoryCacheFilter.addToQuery(map, WadoImage.IMG_AS_BYTES, "true");
			eri = tsEri;
			contentType = eri.mimeType;
			multipleEncoding = false;
		 }
		 if( eri!=null && eri.maxBits!=0 && map.containsKey(MAX_BITS)==false) map.put(MAX_BITS, eri.maxBits);
	  }
	  if( multipleEncoding ) {
		 // This won't happen if we found the encoding that exists in the actual image, eg IMG_AS_BYTES return.
		 for( String testType : contentType.split(",") ) {
			int semi = testType.indexOf(';');
			if( semi>0 ) testType = testType.substring(0,semi);
			testType = testType.trim();
			eri = contentTypeMap.get(testType);
			if( eri!=null ) break;
		 }
	  }
	  if( eri==null ) {
		 return new ErrorResponseItem(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,"Content type "+contentType+" isn't supported.");
	  }
	  WadoImage image = (WadoImage) filterItem.callNamedFilter("wadoImg", map);
	  return new ImageServletResponseItem(image, eri, quality);
   }

   /**
     * Figures out what the query string should be - in a fixed order, using
     * just the handled items.
     * 
     * @param map
     *            to get the WADO parameters from
     * @param A
     *            URI parameter string with the relevant WADO parameters in it,
     *            as defined by wadoParameters
     */
   String computeQueryStr(Map<String, ?> map) {
	  StringBuffer ret = new StringBuffer();
	  for (String key : wadoParameters) {
		 Object value = map.get(key);
		 if (value != null) {
			ret.append("&").append(key).append("=").append(value);
		 }
	  }
	  return ret.toString();
   }

   /** Adds encoding information to the params map based on the transfer syntax or (single)
    * contentType tsuid.
    * 
    * @param tsuid  The transfer syntax UID or single mime type with no parameters.
    * @param params
    */
   public static void addEncodingInfo(String tsuid, Map<String, Object> params) {
	  EncodeResponseInfo eri = contentTypeMap.get(tsuid);
	  if( eri==null ) return;
	  params.put(MAX_BITS, eri.maxBits);
   }

}

/** Does the actual writing to the stream */
class ImageServletResponseItem implements ServletResponseItem {
   private static Logger log = LoggerFactory.getLogger(ImageServletResponseItem.class);

   String contentType;

   ImageWriter writer;

   ImageWriteParam imageWriteParam;

   IIOMetadata iiometadata;

   WadoImage wadoImage;

   private int maxAge = 3600;

   // TODO Make this come from metadata
   // CLIB version
   static String preferred_name_start2 = "com.sun.media.imageioimpl.plugins";

   // Agfa proprietary version
   static String preferred_name_start = "com.agfa";
   // Pure Java version
   // static String preferred_name_start = "com.sun.imageio.plugins";

   /**
     * Create an image servlet response to write the given image to the response
     * stream.
     * 
     * @param image
     *            is the data to write to the stream
     * @param contentType
     *            is the type of image encoding to use (image/jpeg etc) - any
     *            available encoder will be used
     * @param quality
     *            is the JPEG lossy quality (may eventually be other types as
     *            well, but currently that is the only one available)
     */
   public ImageServletResponseItem(WadoImage image, EncodeResponseInfo eri, float quality) {
	  Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(eri.lookupMimeType);
	  ImageWriter writerIt = writers.next();
	  while (writers.hasNext()) {
		 String name = writerIt.getClass().getName();
		 if( name.startsWith(preferred_name_start) ) {
			writer = writerIt;
			break;
		 }
		 else if( name.startsWith(preferred_name_start2) ) {
			writer = writerIt;
		 }
		 log.debug("Skipping {}", name);
		 writerIt = writers.next();
	  }
	  if( writer==null ) {
		 writer = writerIt;
		 log.warn("Couldn't find preferred writer, using "+writer.getClass()+" instead.");
	  }
	  this.contentType = eri.mimeType;
	  this.wadoImage = image;
	  if (eri!=null && quality >= 0f && quality <= 1f && eri.isLossyQuality) {
		 imageWriteParam = writer.getDefaultWriteParam();
		 imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		 imageWriteParam.setCompressionType("JPEG");
		 imageWriteParam.setCompressionQuality(quality);
	  }
	  if (eri.compressionType != null) {
		 if (imageWriteParam == null)
			imageWriteParam = writer.getDefaultWriteParam();
		 imageWriteParam.setCompressionType(eri.compressionType);
	  }

   }

   /**
     * Write the response to the provided stream. Sets the content type and
     * writes to the output stream.
     * 
     * @param httpRequest
     *            unused
     * @param response
     *            that the image is written to. Also sets the content type.
     */
   @SuppressWarnings("unchecked")
   public void writeResponse(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {
	  if (wadoImage == null) {
		 response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found.");
		 log.warn("Image not found.");
		 return;
	  }
	  long start = System.nanoTime();
	  log.info("wadoImage is of type {}",wadoImage.getValue().getType());
	  response.setContentType(contentType);
	  response.setHeader("Cache-Control", "max-age=" + maxAge);
	  // Because this is controlled by login, it will have Pragma and Expires
        // set
	  // to different values, and those need to be removed to get this cached.
	  response.setHeader("Pragma", null);
	  response.setHeader("Expires", null);
	  Collection<String> headers = (Collection<String>) wadoImage.getParameter("responseHeaders");
	  if (headers != null) {
		 for (String key : headers) {
			response.setHeader(key, (String) wadoImage.getParameter(key));
		 }
	  }
	  OutputStream os = response.getOutputStream();
	  if( wadoImage.getValue()==null ) {
		 byte[] rawImage = (byte[]) wadoImage.getParameter(WadoImage.IMG_AS_BYTES);
		 os.write(rawImage);
		 os.flush();
		 log.info("Raw image write took " + nanoTimeToString(System.nanoTime() - start));
		 return;
	  }
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
	  writer.setOutput(ios);
	  IIOImage iioimage = new IIOImage(wadoImage.getValue(), null, null);
	  writer.write(iiometadata, iioimage, imageWriteParam);
	  writer.dispose();
	  ios.close();
	  byte[] data = baos.toByteArray();
	  baos.close();
	  long mid = System.nanoTime();
	  log.info("Encoding image took " + nanoTimeToString(mid - start) + " with " + writer.getClass());
	  os.write(data);
	  os.flush();
	  log.info("Writing image took " + nanoTimeToString(System.nanoTime() - mid) + " with " + writer.getClass());
   }

}

class EncodeResponseInfo {
   // Read from this mime type.
   public String mimeType;

   // Lookup the writer from this mime type.
   public String lookupMimeType;

   // Set the lossy quality from the header
   public boolean isLossyQuality;

   // Maximum number of encodeable bits.
   public int maxBits;

   // Set the compression type to this value
   public String compressionType;

   public EncodeResponseInfo(String mimeType, String lookupMimeType, boolean isLossyQuality, int maxBits, String compressionType,
		 String... transferSyntaxes) {
	  this.mimeType = mimeType;
	  if( lookupMimeType==null ) lookupMimeType = mimeType;
	  this.lookupMimeType = lookupMimeType;
	  this.isLossyQuality = isLossyQuality;
	  this.maxBits = maxBits;
	  this.compressionType = compressionType;
	  if (transferSyntaxes != null) {
		 for (String ts : transferSyntaxes) {
			EncodeImage.contentTypeMap.put(ts, this);
		 }
	  }
	  EncodeImage.contentTypeMap.put(mimeType, this);
   }
}
