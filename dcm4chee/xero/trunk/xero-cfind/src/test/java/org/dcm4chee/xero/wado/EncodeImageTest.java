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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterList;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

/** Tests that images can be encoded as JPEGs, GIF and PNG files.
 * Indirectly tests window levelling as well.  Uses the GradedWadoImage as an
 * image source.
 *
 * @author bwallace
 *
 */
public class EncodeImageTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("WLFilter.metadata"); 

	@Test
	public void testJpegEncoding() throws Exception
	{
		assert mdb!=null;
		MetaDataBean mdbEncode = mdb.get("encode");
		assert mdbEncode!=null;
		FilterList<?> fl = (FilterList<?>) mdbEncode.getValue();
		assert fl!=null;
		FilterItem fi = new FilterItem(mdbEncode);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("contentType", "image/jpeg");
		ServletResponseItem sri = (ServletResponseItem) fl.filter(fi,map);
		assert sri!=null;
		HttpServletResponse mock = EasyMock.createMock(HttpServletResponse.class);
		mock.setContentType("image/jpeg");
		CaptureServletOutputStream csos = new CaptureServletOutputStream();
		EasyMock.expect(mock.getOutputStream()).andReturn(csos);
		EasyMock.expect(mock.getOutputStream()).andReturn(csos);
		EasyMock.replay(mock);
		sri.writeResponse(null,mock);
		EasyMock.verify(mock);
		csos.close();
		byte[] data = csos.getByteArrayOutputStream().toByteArray();
		assert data!=null;
		// Not sure how big it should be, but it had better have some length. 
		assert data.length>16;
	}
}
 
