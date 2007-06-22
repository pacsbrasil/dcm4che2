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

import org.dcm4chee.xero.metadata.filter.FixedItem;

/** This class is a filter that provides a graded WADO image used for filter tests. */
public class GradedWadoImage extends FixedItem<WadoImage> {

	public GradedWadoImage() {
		super(createImage());
	}
	
	/** Create a 16 bit WadoImage that has increase high-byte equal to y, and
	 * low-byte equal to x, as a 256x256 image.
	 * @return WadoImage containing the specified image, on -1..1
	 */
	static WadoImage createImage() {
		BufferedImage bi = new BufferedImage(256, 256, BufferedImage.TYPE_USHORT_GRAY);
		WritableRaster wr = bi.getRaster();
		int[] iArray = new int[1];
		for(int i=0; i<256; i++ ) {
			for(int j=0; j<256; j++ ) {
				iArray[0] = (j << 8) + i;
				wr.setPixel(i,j, iArray);
			}
		}
		WadoImage wi = new WadoImage(bi, "-1.0", "1.0f");
		wi.setParameter(WadoImage.WINDOW_CENTER, 0.5d);
		wi.setParameter(WadoImage.WINDOW_WIDTH, 1.0d);
		return wi;
	}

}
