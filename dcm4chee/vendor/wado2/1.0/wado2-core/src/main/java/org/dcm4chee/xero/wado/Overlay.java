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

import static org.dcm4chee.xero.wado.OverlayUtils.findAllOverlays;
import static org.dcm4chee.xero.wado.OverlayUtils.findSelectedOverlays;
import static org.dcm4chee.xero.wado.WadoParams.COLUMNS;
import static org.dcm4chee.xero.wado.WadoParams.FRAME_NUMBER;
import static org.dcm4chee.xero.wado.WadoParams.ROWS;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds overlays to the index image being returned.
 * 
 * @author bwallace
 * 
 */
public class Overlay implements Filter<WadoImage> {
	private static Logger log = LoggerFactory.getLogger(Overlay.class);

	public static final String OVERLAY_KEY = "overlay";

	/** Add pixel padding to the returned image. */
	public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
		String overlay = (String) params.get(OVERLAY_KEY);
		if (overlay == null) {
			log.debug("No overlay specified, so just calling next method.");
			return filterItem.callNextFilter(params);
		}

		DicomObject ds = dicomImageHeader.filter(null, params);
		if (ds == null)
			return null;
		List<OverlayInfo> overlays = findOverlays(overlay, ds);
		if (overlays == null) {
			log.debug("The image does not contain any of the specified overlays.");
			return filterItem.callNextFilter(params);
		}

		List<byte[]> clrs = ImageDisplayRelative.getColours(params);
		boolean needsImage = false;
		int width = ds.getInt(Tag.Columns);
		int height = ds.getInt(Tag.Rows);
		if (width == 0) {
			width = FilterUtil.getInt(params, COLUMNS);
			height = FilterUtil.getInt(params, ROWS);
			if (width == 0) {
				width = overlays.get(0).width;
				params.put(COLUMNS, width);
			}
			if (height == 0) {
				height = overlays.get(0).height;
				params.put(ROWS, height);
			}
		}
		for (OverlayInfo oi : overlays) {
			clrs.add(oi.clr);
			oi.useClr = clrs.size();
			needsImage = needsImage || oi.embedded;
		}
		if (needsImage)
			params.put(ImageDisplayRelative.NEEDS_IMAGE, Boolean.TRUE);
		WadoImage ret = filterItem.callNextFilter(params);
		if (ret == null)
			return null;

		BufferedImage biRet = ret.getValue();
		WritableRaster rret = biRet.getRaster();

		WadoImage wi = (WadoImage) ret.getParameter(ImageDisplayRelative.NEEDS_IMAGE);
		BufferedImage bi = wi == null ? null : wi.getValue();
		WritableRaster r = wi == null ? null : bi.getRaster();

		int w = biRet.getWidth();
		int h = biRet.getHeight();
		int[] row = null;
		int[] retrow = null;

		for (int y = 0; y < h; y++) {
			if (r != null)
				row = r.getPixels(0, y, w, 1, row);
			retrow = rret.getPixels(0, y, w, 1, retrow);
			boolean rowChanged = false;
			for (OverlayInfo oi : overlays) {
				if (oi.embedded) {
					int bit = (1 << oi.bit);
					for (int x = 0; x < h; x++) {
						if ((row[x] & bit) != 0) {
							rowChanged = true;
							retrow[x] = oi.useClr;
						}
					}
				} else {
					int start = w * y;
					if (oi.multiframe) {
						log.debug("Returning a multi-frame embedded overlay.");
						int frame = FilterUtil.getInt(params, FRAME_NUMBER, 1) - 1;
						start = start + frame * w * h;
					}
                    int bitIgnore = start % 8;
                    start = start/8;
					for (int x = -bitIgnore; x < w;) {
						byte datum = oi.data[start++];
						for (int b = 1; b < 256 && x<w; b = b << 1, x++) {
						    if( x<0 ) continue;
							if ((datum & b) != 0) {
								rowChanged = true;
								retrow[x] = oi.useClr;
							}
						}
					}
				}
			}
			if (rowChanged) {
				rret.setPixels(0, y, w, 1, retrow);
			}
		}

		for (OverlayInfo oi : overlays) {
			ret.setFilename(ret.getFilename()+"-overlay"+oi.number);
		}

		return ret;
	}

	/**
	 * This will find the overlays to use, or return null if no overlays are
	 * found.
	 */
	protected List<OverlayInfo> findOverlays(String overlay, DicomObject ds) {
		if ("true".equalsIgnoreCase(overlay) || overlay.startsWith(":")) {
			return findAllOverlays(overlay, ds);
		}
		return findSelectedOverlays(overlay, ds);
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
