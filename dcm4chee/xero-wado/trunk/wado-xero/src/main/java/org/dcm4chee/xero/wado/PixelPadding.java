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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds pixel padding to a returned image, only if there exists pixel padding in the header.
 * @author bwallace
 *
 */
public class PixelPadding implements Filter<WadoImage> {
	private static Logger log = LoggerFactory.getLogger(PixelPadding.class);
	
	private static final byte[] BLACK = new byte[]{0,0,0};
	
	/** Add pixel padding to the returned image. */
	public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
		String padding = (String) params.get("padding");
		if( padding==null ) {
			log.debug("No padding specified, so just calling next method.");
			return filterItem.callNextFilter(params);
		}
		DicomObject ds = dicomImageHeader.filter(null,params);
		if( ds==null ) return null;
		if( !ds.contains(Tag.PixelPaddingValue) ) {
			log.debug("The image does not contain pixel padding, so just returning the next call.");
			return filterItem.callNextFilter(params);
		}
		int pixpad = ds.getInt(Tag.PixelPaddingValue);
		
		byte[] clr = ImageDisplayRelative.getRGBFromString(padding, BLACK);
		List<byte[]> clrs = ImageDisplayRelative.getColours(params);
		clrs.add(clr);
		int useClr = clrs.size();
		params.put(ImageDisplayRelative.NEEDS_IMAGE, Boolean.TRUE);
		WadoImage ret = filterItem.callNextFilter(params);
		if( ret==null ) return null;

		BufferedImage biRet = ret.getValue();
		WritableRaster rret = biRet.getRaster();

		WadoImage wi = (WadoImage) ret.getParameter(ImageDisplayRelative.NEEDS_IMAGE);
		BufferedImage bi = wi.getValue();
        if( bi==null ) {
            log.error("No pixel data found for image/frame - can't read pixel padding.");
            return null;
        }
		WritableRaster r = bi.getRaster();
		
		int w = biRet.getWidth();
		int h = biRet.getHeight();
		int[] row = null;
		int[] retrow = null;
		for(int y=0; y<h; y++) {
			row = r.getPixels(0, y, w, 1, row);
			retrow = rret.getPixels(0,y,w,1,retrow);
			boolean rowChanged = false;
			for(int x=0; x<w; x++) {
				if( row[x]==pixpad ) {
					rowChanged = true;
					retrow[x] = useClr;
				}
			}
			if( rowChanged ) {
				rret.setPixels(0,y,w,1,retrow);
				//for(int x=0; x<10; x++) {
				//log.info("Colour at "+x+","+y+" raster "+retrow[x]+" rgb "+Long.toString(biRet.getRGB(x, y) & 0xFFFFFFFFl,16)+" should be "+(clr[0] & 0xFF)+","+(clr[1] & 0xFF)+","+(clr[2] & 0xFF));
			}
		}
				
		ret = new WadoImage(ret);
		ret.setFilename(ret.getFilename()+"-padding");
	   return ret;
   }

   private Filter<DicomObject> dicomImageHeader;

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomImageHeader() {
   	return dicomImageHeader;
   }

	@MetaData(out="${ref:dicomImageHeader}")
	public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
   	this.dicomImageHeader = dicomImageHeader;
   }

}
