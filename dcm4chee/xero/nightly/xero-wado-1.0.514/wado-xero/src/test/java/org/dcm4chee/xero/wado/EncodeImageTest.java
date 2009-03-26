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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.servlet.CaptureServletOutputStream;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests that images can be encoded as JPEGs, GIF and PNG files.
 * Uses the GradedWadoImage as an image source.
 *
 * @author bwallace
 *
 */
public class EncodeImageTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("dicom.metadata"); 

	@SuppressWarnings("unchecked")
   @Test
	public void testJpegEncoding() throws Exception
	{
		assert mdb!=null;
		EncodeImage ei = (EncodeImage) mdb.getValue("encode");
		assert ei!=null;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("contentType", "image/jpeg");
		Filter<DicomObject> dicomImageHeader = createMock(Filter.class);
		DicomObject ds = new BasicDicomObject();
		ds.putInt(Tag.PixelRepresentation, VR.IS, 1);
		expect(dicomImageHeader.filter(null,map)).andReturn(ds);
		ei.setDicomImageHeader(dicomImageHeader);
		replay(dicomImageHeader);
		ei.setWadoImageFilter(new GradedWadoImage());
		
		ServletResponseItem sri = (ServletResponseItem) ei.filter(null,map);
		verify(dicomImageHeader);
		
		assert sri!=null;
		HttpServletResponse mock = createMock(HttpServletResponse.class);
		mock.setContentType("image/jpeg");
		mock.setHeader((String) anyObject(), (String) anyObject());
		expectLastCall().anyTimes();
		CaptureServletOutputStream csos = new CaptureServletOutputStream();
		mock.setContentLength(2250);
		expect(mock.getOutputStream()).andReturn(csos);
		replay(mock);
		sri.writeResponse(null,mock);
		csos.close();
		byte[] data = csos.getByteArrayOutputStream().toByteArray();
		assert data!=null;
		// Not sure how big it should be, but it had better have some length. 
		assert data.length>16;
		verify(mock);
	}
	
	@Test
	public void testGetTransferSyntaxFromFileFormat(){
		String output;
		
		output = EncodeImage.getTransferSyntaxFromFileFormat("jp12");	;
		assert output.equalsIgnoreCase(UID.forName("JPEGExtended24"));		
		
		output = EncodeImage.getTransferSyntaxFromFileFormat("jpls");
		if (!(output.equalsIgnoreCase(UID.forName("JPEGLSLossless")) ||
				output.equalsIgnoreCase(UID.forName("JPEGLSLossyNearLossless")))){
			Assert.fail();
		}
		
		output = EncodeImage.getTransferSyntaxFromFileFormat("jpll");
		assert output.equalsIgnoreCase(UID.forName("JPEGLossless"));
		
		output = EncodeImage.getTransferSyntaxFromFileFormat("jpeg");
		assert output.equalsIgnoreCase(UID.forName("JPEGBaseline1"));
		
		output = EncodeImage.getTransferSyntaxFromFileFormat("jp2");
		if (!(output.equalsIgnoreCase(UID.forName("JPEG2000")) ||
				output.equalsIgnoreCase(UID.forName("JPEG2000LosslessOnly")))){
			Assert.fail();
		}
		
		List<String> fileFormatsOther = new ArrayList<String>();
		fileFormatsOther.add("bmp");
		fileFormatsOther.add("gif");
		fileFormatsOther.add("png");
		fileFormatsOther.add("png16");			
		for (String input : fileFormatsOther){
			output = EncodeImage.getTransferSyntaxFromFileFormat(input);
			Assert.assertNull(output);
		}
		
		List<String> fileFormatsUnknown = new ArrayList<String>();
		fileFormatsUnknown.add("jpg");
		fileFormatsUnknown.add("test");
		for (String input : fileFormatsUnknown){
			output = EncodeImage.getTransferSyntaxFromFileFormat(input);			
			Assert.assertNull(output);
		}		
	}
}
 
