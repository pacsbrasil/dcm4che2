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

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;

/**
 * @author lpeters
 *
 */
public class ReduceBitsFilterTest {
	
	@SuppressWarnings("unchecked")
	protected void checkSmallestLargest(WadoImage wi, int smallestPixelValue, int largestPixelValue, int bitsAfterResampling ) {
		String filename = wi.getFilename();
		assertTrue(filename.contains("-pixelRange"+smallestPixelValue+","+largestPixelValue+","+bitsAfterResampling));	
		assertEquals(smallestPixelValue, ReduceBitsFilter.getPreviousSmallestPixelValue(wi));
		assertEquals(largestPixelValue, ReduceBitsFilter.getPreviousLargestPixelValue(wi));
		assertEquals(bitsAfterResampling, ReduceBitsFilter.getPreviousReducedBits(wi));
		
		Collection<String> headers = (Collection<String>) wi.getParameter(ReduceBitsFilter.RESPONSE_HEADERS);
		assertTrue(headers.contains(ReduceBitsFilter.SMALLEST_IMAGE_PIXEL_VALUE));
		assertTrue(headers.contains(ReduceBitsFilter.LARGEST_IMAGE_PIXEL_VALUE));
		assertTrue(headers.contains(ReduceBitsFilter.REDUCED_BITS));
	}
		
	@Test
	public void addSmallestLargestToWadoImageTest() throws IOException, ParserConfigurationException {
		WadoImage wi = new WadoImage(null, 0, null);
		
		ReduceBitsFilter.addSmallestLargestToWadoImage(wi, -2000, 789932, 11 );
		checkSmallestLargest(wi,-2000,789932,11);
		
		ReduceBitsFilter.addSmallestLargestToWadoImage(wi, -4000, 2111, 10 );
		checkSmallestLargest(wi,-4000,2111,10);
		
		String filename = wi.getFilename();
		assertFalse(filename.contains("-pixelRange-2000,79889,11"));			
	}
}
