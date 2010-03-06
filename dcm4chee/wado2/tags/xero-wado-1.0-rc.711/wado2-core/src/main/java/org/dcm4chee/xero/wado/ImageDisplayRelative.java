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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.wado.WadoParams.*;

/**
 * This filter allows extraction of image relative features into a buffered
 * image containing 1 entry per pixel type/value. Any pixels containing no
 * information are set to fully transparent(0) pixel value. The convention for
 * filters in imageRel is that filters add colours to the colours key on the
 * incoming set of filters, and then the second to last filter creates a
 * buffered image of the correct type. As well, needs image should be set - if
 * so, then the returned image will be available by calling the last filter
 * (which could be through a memory cache for performance reasons). The raw
 * image data will be available as NEEDS_IMAGE in the extra parameters of the
 * WadoImage being returned.
 * 
 * Specify the type of return required with relative=display or relative=image
 * to get an image relative or display relative return object - these have
 * different sets of filters, but otherwise are handled identically (it could be
 * possible to just plugin the available filter lists as well or instead.)
 * 
 * @author bwallace
 */
public class ImageDisplayRelative implements Filter<WadoImage> {
   private static final Logger log = LoggerFactory.getLogger(ImageDisplayRelative.class);
	public static final String DISPLAY_VALUE = "display";
	public static final String IMAGE_VALUE = "image";
	public static final String RELATIVE_KEY = "relative";
	public static final String COLOURS = "_colours";
	public static final String NEEDS_IMAGE = "_Image";

	Filter<WadoImage> displayRel, imageRel;

	/** Convert an RGB string to an array of transparency values */
	public static byte[] getRGBFromString(String rgbs, byte[] def) {
		if ("true".equalsIgnoreCase(rgbs))
			return def;
		if (rgbs != null && rgbs.length() > 0) {
			if (rgbs.startsWith(COLOUR_SEPARATOR))
				rgbs = rgbs.substring(1);
			int rgb = Integer.parseInt(rgbs, 16);
			return new byte[] { (byte) ((rgb >> 16) & 0xFF), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF) };
		}
		return def;
	}

	/** Extract the image relative objects. */
	public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
		if (!params.containsKey(RELATIVE_KEY)) {
			return filterItem.callNextFilter(params);
		}
		String relType = (String) params.get(RELATIVE_KEY);
		params.put(COLOURS, new ArrayList<byte[]>());
		params.put(NEEDS_IMAGE, Boolean.FALSE);
		if (DISPLAY_VALUE.equalsIgnoreCase(relType)) {
		   log.info("Return display relative image.");
			return displayRel.filter(null, params);
		} else {
		   log.info("Return image relative filter.");
			return imageRel.filter(null, params);
		}
	}

	/** Get the list of indexed colours to be generated */
	@SuppressWarnings("unchecked")
	public static List<byte[]> getColours(Map<String, Object> params) {
		return (List<byte[]>) params.get(COLOURS);
	}

	/** Gets the filter used to generate the display relative image */
	public Filter<WadoImage> getDisplayRel() {
		return displayRel;
	}

	@MetaData(out = "${ref:displayRel}")
	public void setDisplayRel(Filter<WadoImage> displayRel) {
		this.displayRel = displayRel;
	}

	/** Gets the filter used to generate the image relative image */
	public Filter<WadoImage> getImageRel() {
		return imageRel;
	}

	@MetaData(out = "${ref:imageRel}")
	public void setImageRel(Filter<WadoImage> imageRel) {
		this.imageRel = imageRel;
	}

}
