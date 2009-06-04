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

import static org.dcm4chee.xero.wado.WadoParams.COLOUR_SEPARATOR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverlayUtils {
	
	private static Logger log = LoggerFactory.getLogger(OverlayUtils.class);
	protected static final byte[] WHITE = new byte[] { (byte) 255, (byte) 255, (byte) 255 };
	
	/** Finds all embedded overlays */
	protected static List<OverlayInfo> findEmbeddedOverlays(String sclr, DicomObject ds) {
		byte[] clr = ImageDisplayRelative.getRGBFromString(sclr, WHITE);
		Iterator<DicomElement> it = ds.iterator(Tag.OverlayType, Tag.OverlayType | 0x1000000);
		if (it == null)
			return null;
		List<OverlayInfo> ret = null;
		while (it.hasNext()) {
			DicomElement de = it.next();
			log.info("Found tag {}", de);
			int base = 0x60FF0000 & de.tag();
			if (de.tag() != (base | Tag.OverlayType))
				continue;
			int bitPosition = ds.getInt(base | Tag.OverlayBitPosition);
			if ( bitPosition == 0 ) {
				continue;
			}
			ret = addOverlay(ret, ds, base, clr);
		}
		return ret;
	}

	/** Finds all overlays */
	protected static List<OverlayInfo> findAllOverlays(String sclr, DicomObject ds) {
		byte[] clr = ImageDisplayRelative.getRGBFromString(sclr, WHITE);
		Iterator<DicomElement> it = ds.iterator(Tag.OverlayType, Tag.OverlayType | 0x1000000);
		if (it == null)
			return null;
		List<OverlayInfo> ret = null;
		while (it.hasNext()) {
			DicomElement de = it.next();
			log.info("Found tag {}", de);
			int base = 0x60FF0000 & de.tag();
			if (de.tag() != (base | Tag.OverlayType))
				continue;
			ret = addOverlay(ret, ds, base, clr);
		}
		return ret;
	}

	/** Adds an overlay to the list, creating if if required. */
	protected static List<OverlayInfo> addOverlay(List<OverlayInfo> ret, DicomObject ds, int base, byte[] clr) {
		String type = ds.getString(base | Tag.OverlayType);
		if (!"G".equals(type))
			return ret;
		if (ret == null)
			ret = new ArrayList<OverlayInfo>();

		int overlayNumber = (base >> 16) & 0xFF;
		ret.add(  createOverlayInfo(ds, overlayNumber, clr ) );
	
		return ret;
	}
	
	protected static OverlayInfo createOverlayInfo( DicomObject ds, int overlayNumber, byte[] clr ) {
		OverlayInfo oi = new OverlayInfo(ds, overlayNumber);
		oi.clr = clr;
		
		return oi;
	}

	/** Finds overlays from a specified list of overlays */
	protected static List<OverlayInfo> findSelectedOverlays(String lst, DicomObject ds) {
		String[] split = StringUtil.split(lst, ',', true);
		List<OverlayInfo> ret = null;
		for (String over : split) {
			byte[] clr;
			int startClr = over.indexOf(COLOUR_SEPARATOR);
			int iover;
			if (startClr > 0) {
				clr = ImageDisplayRelative.getRGBFromString(over.substring(startClr), WHITE);
				iover = Integer.parseInt(over.substring(0, startClr));
			} else {
				clr = WHITE;
				iover = Integer.parseInt(over);
			}
			int base = 0x60000000 | (iover << 16);
			ret = addOverlay(ret, ds, base, clr);
		}
		return ret;
	}
	
}
