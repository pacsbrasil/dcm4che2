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

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.LookupTable;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class WLFilterTest {
	
	@Test
	public void localCreateLutForImageWithPRTest_With_PreviousPixelrange(){
		DicomObject header = new BasicDicomObject();
		WadoImage wi = new WadoImage(header, 0, null);
		
		wi.setParameter(ReduceBitsFilter.SMALLEST_IMAGE_PIXEL_VALUE, 2000);
		wi.setParameter(ReduceBitsFilter.LARGEST_IMAGE_PIXEL_VALUE, 10000);
		wi.setParameter(ReduceBitsFilter.REDUCED_BITS, 12);
		
		header.putInt(Tag.BitsAllocated, null, 16);
		header.putInt(Tag.BitsStored, null, 15);
		header.putInt(Tag.HighBit, null, 14);
		header.putInt(Tag.PixelRepresentation, null, 0);
		header.putString(Tag.PhotometricInterpretation, null, "MONOCHROME2");

		header.putFloat(Tag.RescaleSlope, null, 2.0f);
		header.putFloat(Tag.RescaleIntercept, null, -1000.0f);

		float windowCenter = 11000;
		float windowWidth = 16000;
		header.putFloat(Tag.WindowCenter, null, windowCenter);
		header.putFloat(Tag.WindowWidth, null, windowWidth);

		header.putString(Tag.PresentationLUTShape, null, "IDENTITY");
		
		LookupTable lut = WLFilter.localCreateLutForImageWithPR(wi, null, 0, windowCenter, windowWidth, LookupTable.LINEAR, 8, null);
		short[] input = new short[]{0, 2048, 4095};
		byte[] output = new byte[3];
		
		lut.lookup(input, output);
		assertEquals(output[0]&0xff,0);
		assertEquals(output[1]&0xff,128);
		assertEquals(output[2]&0xff,255);
	}

}
