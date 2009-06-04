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

import static org.testng.Assert.assertEquals;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WadoImageTest {
	private DicomObject ds;
	private BufferedImage image;

	@BeforeMethod
	public void initializeWadoImage() {
		ds = new BasicDicomObject();
		ds.putInt(Tag.Columns, VR.US, 640);
		ds.putInt(Tag.Rows, VR.US, 480);
		image = new BufferedImage(320, 240, BufferedImage.TYPE_BYTE_GRAY);
	}
	
	@Test
	public void testGetBufferDimension_whenBufferedImageAvailable_shouldReturnBufferedImageDimensionsOnly() {
		int stored = 12;
		WadoImage wi = new WadoImage(ds, stored, image );
		wi.setParameter(WadoImage.AS_BYTES_RETURNED_SUBSAMPLE_FACTOR, new Point(4,4));
		wi.setParameter(WadoImage.AS_BYTES_RETURNED_TRANSFER_SYNTAX, UID.JPEGExtended24);
		
		Dimension dimension = wi.getBufferDimension();
		assertEquals(dimension.width,320);
		assertEquals(dimension.height,240);
	}
	
	@Test
	public void testGetBufferDimension_whenBufferedImageNotAvailable_shouldReturnAsBytesDimensions() {
		int stored = 12;
		WadoImage wi = new WadoImage(ds, stored, null );
		wi.setParameter(WadoImage.AS_BYTES_RETURNED_SUBSAMPLE_FACTOR, new Point(4,4));
		wi.setParameter(WadoImage.AS_BYTES_RETURNED_TRANSFER_SYNTAX, UID.JPEGExtended24);
		wi.setParameter(WadoImage.IMG_AS_BYTES, new byte[160*120*2]);
		
		Dimension dimension = wi.getBufferDimension();
		assertEquals(dimension.width,160);
		assertEquals(dimension.height,120);
	}
	
	@Test
	public void testGetBufferDimension_whenNoImagesAvailable_shouldReturnNull() {
		int stored = 12;
		WadoImage wi = new WadoImage(ds, stored, null );
		wi.setParameter(WadoImage.AS_BYTES_RETURNED_TRANSFER_SYNTAX, UID.JPEGExtended24);
		
		Dimension dimension = wi.getBufferDimension();
		assertEquals(dimension,null);
	}
	
	
}
