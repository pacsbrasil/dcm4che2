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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.wado.WadoParams.*;

/**
 * This class creates a WadoImage that contains an indexed colour image source
 * with the correct number of colours. It will ALSO call the image method if
 * appropriate to read the raw image data for the current frame, if the correct
 * header is set.
 * 
 * @author bwallace
 * 
 */
public class IndexedImageSource implements Filter<WadoImage> {
	private static Logger log = LoggerFactory.getLogger(IndexedImageSource.class);

	/** Create an indexed colour model as required */
	public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
		List<byte[]> colours = ImageDisplayRelative.getColours(params);
		if (colours == null || colours.size() == 0) {
			log.info("No image/display relative objects to include, so returning null.");
			return null;
		}

		boolean needOriginalImage = params.get(ImageDisplayRelative.NEEDS_IMAGE) == Boolean.TRUE;
		DicomObject ds;
		WadoImage orig = null;
		if (needOriginalImage) {
		    Object [] vals = FilterUtil.removeFromQuery(params, ROWS, COLUMNS, REGION);
		    Object readRaw = params.remove(WadoImage.IMG_AS_BYTES);
			orig = wadoImageFilter.filter(null, params);
            if( orig==null ) {
               log.error("Original source image must not be null, memory cache query string is {}",params.get(MemoryCacheFilter.KEY_NAME));
            }
			FilterUtil.restoreQuery(params, vals, ROWS, COLUMNS, REGION );
			if( readRaw!=null ) params.put(WadoImage.IMG_AS_BYTES, readRaw);
			ds = orig.getDicomObject();
		} else {
			ds = dicomImageHeader.filter(null, params);
		}

		int stored = 0;
		int clrCount = colours.size();
		// CLIB PNG writer can't handle 2 or 4 bits.
		if (clrCount == 1)
			stored = 1;
		else
			stored = 8;
		// while(clrCount>0) {
		// stored++;
		// clrCount >>= 1;
		// }
		// if( stored==3 ) stored = 4;
		// else if( stored>4 && stored < 8 ) stored = 8;
		// else if( stored>8 ) throw new IllegalArgumentException("Too many
		// colours - currently only support up to 255 colours and you have
		// "+colours.size());

		// TODO - use the specified width/height in some way, particularly for
		// display relative.
		int width = ds.getInt(Tag.Columns);
		int height = ds.getInt(Tag.Rows);
		if (width == 0) {
			width = FilterUtil.getInt(params, COLUMNS);
			if (width == 0)
				throw new IllegalArgumentException("Unknown image size.");
		}
		if (height == 0) {
			height = FilterUtil.getInt(params, ROWS);
			if (height == 0)
				throw new IllegalArgumentException("Unknown image height.");
		}
		clrCount = colours.size() + 1;
		byte[] r = new byte[clrCount];
		byte[] g = new byte[clrCount];
		byte[] b = new byte[clrCount];
		byte[] a = new byte[clrCount];
		for (int i = 1; i < clrCount; i++) {
			byte[] clr = colours.get(i - 1);
			a[i] = (byte) 255;
			r[i] = clr[0];
			g[i] = clr[1];
			b[i] = clr[2];
		}
		IndexColorModel icm = new IndexColorModel(stored, clrCount, r, g, b, a);
		int rowLength = (width * stored+7) / 8;
		byte[] data = new byte[rowLength * height];
		DataBuffer db = new DataBufferByte(data, data.length);
		WritableRaster wr = Raster.createPackedRaster(db, width, height, stored, null);
		BufferedImage bi = new BufferedImage(icm, wr, false, null);
		WadoImage ret = new WadoImage(ds, stored, bi);
		ret.setFilename(ds.getString(Tag.SOPInstanceUID));
		if( params.containsKey(FRAME_NUMBER)) ret.setFilename(ret.getFilename()+"-f"+params.get(FRAME_NUMBER));
		if (needOriginalImage)
			ret.setParameter(ImageDisplayRelative.NEEDS_IMAGE, orig);

		log.info("Created an indexed buffered image on {} bites with {} colours.", stored, clrCount - 1);

		return ret;
	}

   private Filter<WadoImage> wadoImageFilter;

	public Filter<WadoImage> getWadoImageFilter() {
   	return wadoImageFilter;
   }

	/**
	 * Sets the filter to use for the wado image data.
	 * @param wadoImageFilter
	 */
	@MetaData(out="${ref:dcmImg}")
	public void setWadoImageFilter(Filter<WadoImage> wadoImageFilter) {
   	this.wadoImageFilter = wadoImageFilter;
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
