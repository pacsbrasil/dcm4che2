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


import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MultiPartContentTypeResponseItemTest {

	MultiPartContentTypeResponseItem responseItem;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception 
	{
		responseItem = new MultiPartContentTypeResponseItem();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGet_WhenFirstInitialized_ShouldBeEmpty() 
	{
		List<ServletResponseItem> responseList = responseItem.get();
		assert( responseList.isEmpty() );
	}
	
	@Test
	public void testGet_WhenModifyingReturnedList_ShouldThrowUnsupportedOperationException() 
	{
		try
		{
			responseItem.get().add(null);
			assert(false);
		}
		catch ( UnsupportedOperationException e)
		{
		}
	}
	
	@Test
	public void testAdd_WhenAddingNull_ShouldNotModifyTheList() 
	{
		int initialSize = responseItem.get().size();
		responseItem.add(null);
		assert ( responseItem.get().size() == initialSize );
	}
	
	@Test
	public void testAdd_WhenServerResponseItem_ShouldAppearInList() 
	{
		ServletResponseItem servletItem = createNiceMock( ServletResponseItem.class );
		replay( servletItem );
		
		responseItem.add(servletItem);
		assert ( responseItem.get().contains(servletItem) );
	}
	
	@Test
	public void testWriteResponse_WhenResponseListIsEmpty_SendResponseErrorSC_NOT_FOUND() throws IOException
	{
		HttpServletRequest httpRequest = null;
		HttpServletResponse response = createMock( HttpServletResponse.class );
		response.sendError(eq(HttpServletResponse.SC_NOT_FOUND), isA(String.class));
		replay(response);
		
		responseItem.writeResponse(httpRequest, response);
		verify(response);
	}	
	
	@Test
	public void testWriteResponse_WhenResponseListHasItems_EachItemWillWriteResponse() throws IOException
	{
		MultiPartContentTypeResponseItem responseItem = new MultiPartContentTypeResponseItem() {

			protected void writeEndBoundary(ServletOutputStream os) 
				throws IOException {}

			protected void writeStartBoundary(ServletOutputStream os) 
				throws IOException {}
		};
		
		HttpServletRequest httpRequest = createNiceMock( HttpServletRequest.class );
		replay( httpRequest );
		
		HttpServletResponse response = createNiceMock( HttpServletResponse.class );
		replay(response);
		
		ServletResponseItem servletItem = createMock( ServletResponseItem.class );
		servletItem.writeResponse( eq(httpRequest), isA( BodyResponseWrapper.class ) );
		replay( servletItem );
		
		ServletResponseItem servletItem2 = createMock( ServletResponseItem.class );
		servletItem2.writeResponse( eq(httpRequest), isA( BodyResponseWrapper.class ) );
		replay( servletItem2 );
		
		responseItem.add(servletItem);
		responseItem.add(servletItem2);
		
		responseItem.writeResponse(httpRequest, response);
		
		verify( servletItem );
		verify( servletItem2 );
	}
	
	
	public void testWriteStartBoundary_WhenOutputStreamProvided_ShouldWriteStartBoundary()
		throws IOException
	{
		ServletOutputStream os = createMock( ServletOutputStream.class );
		os.println();
		os.println( eq( responseItem.BOUNDARY_START ) );
		replay(os);
		
		responseItem.writeStartBoundary(os);
		
		verify( os );
	}
	
	@Test
	public void testWriteEndBoundary_WhenOutputStreamProvided_ShouldWriteEndBoundary()
		throws IOException
	{
		ServletOutputStream os = createMock( ServletOutputStream.class );
		os.println();
		os.println( eq( responseItem.BOUNDARY_END ) );
		os.flush();
		replay(os);
		
		responseItem.writeEndBoundary(os);
		
		verify( os );
	}
}

