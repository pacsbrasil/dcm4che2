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


import java.util.HashMap;
import java.util.Map;

import static org.easymock.classextension.EasyMock.*;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.testng.annotations.*;


/**
 * Tests the multi-part content type return.
 * @author tcollins
 *
 */
@SuppressWarnings("unchecked")
public class MultiPartContentTypeFilterTest 
{
	FilterItem<ServletResponseItem> filterItemMock;
	
	MultiPartContentTypeFilter filter;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception 
	{
		filterItemMock = createMock( FilterItem.class );
		
		filter = new MultiPartContentTypeFilter();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.dcm4chee.xero.wado.MultiPartContentTypeFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
	 */
	@Test
	public void testFilter_WhenObjectUIDIsNull_ShouldReturnNull() 
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put( MultiPartContentTypeFilter.CONTENT_TYPE, "myType" );
		
		assert( filter.filter(filterItemMock, params) == null );
	}
	

	/**
	 * Test method for {@link org.dcm4chee.xero.wado.MultiPartContentTypeFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
	 */
	@Test
	public void testFilter_WhenContentTypeIsNull_ShouldReturnNull() 
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put( MultiPartContentTypeFilter.OBJECT_UID, "1.2.3" );
		
		assert( filter.filter(filterItemMock, params) == null );
	}

	
	@Test
	public void testFilter_WhenContentTypeOnlyHasOneItem_ShouldReturnNull()
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put( MultiPartContentTypeFilter.CONTENT_TYPE, "myType");
		params.put( MultiPartContentTypeFilter.OBJECT_UID, "1.2.3" );
		
		assert( filter.filter(filterItemMock, params) == null );
	}
	
	@Test
	public void testFilter_WhenContentTypeOnlyHasMultiPart_ShouldReturnNull()
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put( MultiPartContentTypeFilter.CONTENT_TYPE, filter.MULTIPART_MIXED );
		params.put( MultiPartContentTypeFilter.OBJECT_UID, "1.2.3" );
		
		assert( filter.filter(filterItemMock, params) == null );
	}
	
	
	@Test
	public void testFilter_WhenContentTypeDoesNotHaveMultiPartFirst_ShouldReturnNull()
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put( MultiPartContentTypeFilter.CONTENT_TYPE, "myType," + filter.MULTIPART_MIXED );
		params.put( MultiPartContentTypeFilter.OBJECT_UID, "1.2.3" );
		
		assert( filter.filter(filterItemMock, params) == null );
	}
	
	@Test
	public void testFilter_WhenContentTypeHasMultiPartFirstAndMoreParts_ShouldReturnFiltersForThoseMoreParts()
	{
		final ServletResponseItem servletItem1 = createMock( ServletResponseItem.class );
		replay( servletItem1);
		final ServletResponseItem servletItem2 = createMock( ServletResponseItem.class );
		replay( servletItem2);
		
		final MultiPartContentTypeResponseItem multiResponseItem = 
			createMock(MultiPartContentTypeResponseItem.class);
		multiResponseItem.add( servletItem1 );
		multiResponseItem.add( servletItem2 );
		replay( multiResponseItem );
		
		
		filter = new MultiPartContentTypeFilter() 
		{
			@Override
			protected ServletResponseItem getResponseItem(
					FilterItem<ServletResponseItem> filterItem,
					Map<String, Object> newParams) 
			{
				if ( newParams.get(MultiPartContentTypeFilter.CONTENT_TYPE).equals("Filter1"))
				{
					return servletItem1;
				}
				else if ( newParams.get(MultiPartContentTypeFilter.CONTENT_TYPE).equals("Filter2"))
				{
					return servletItem2;
				}
				return null;
			}
			
			@Override
		    protected MultiPartContentTypeResponseItem getMultiPartResponseItem()
		    {
		    	return multiResponseItem;
		    }

		};


		Map<String, Object> params = new HashMap<String, Object>();
		params.put( MultiPartContentTypeFilter.CONTENT_TYPE, filter.MULTIPART_MIXED + ",Filter1,Filter2,Filter3" );
		params.put( MultiPartContentTypeFilter.OBJECT_UID, "1.2.3" );
		
		assert( filter.filter(filterItemMock, params) == multiResponseItem );
		
		verify( multiResponseItem );
	}
	
	@Test
	public void testGetResponseItem_WithFilterAvailable_ShouldReturnValidServerResponse()
	{
		Map<String, Object> params = new HashMap<String, Object>();
		
		ServletResponseItem serverResponseItem = createNiceMock( ServletResponseItem.class );
		replay( serverResponseItem );
		
		reset( filterItemMock );
		expect( filterItemMock.callNamedFilter(eq("choose"), eq(params)) ).andStubReturn(serverResponseItem);
		replay( filterItemMock );
		
		assert( filter.getResponseItem( filterItemMock, params ) == serverResponseItem );
	}
	
	@Test
	public void testGetResponseItem_WithFilterException_ShouldReturnNull()
	{
		Map<String, Object> params = new HashMap<String, Object>();
		
		reset( filterItemMock );
		expect( filterItemMock.callNamedFilter(eq("choose"), eq(params)) ).andThrow( new NullPointerException());
		replay( filterItemMock );
		
		assert( filter.getResponseItem( filterItemMock, params ) == null );
	}
	
	public void testGetResponseItem_WithNoFilterAvailable_ShouldReturnNull()
	{
		Map<String, Object> params = new HashMap<String, Object>();
		
		reset( filterItemMock );
		expect( filterItemMock.callNamedFilter(eq("choose"), eq(params)) ).andStubReturn(null);
		replay( filterItemMock );
		
		assert( filter.getResponseItem( filterItemMock, params ) == null );
	}
	
	
	@Test
	public void testGetResponseItem_WithErrorResponseItem_ShouldReturnNull()
	{
		Map<String, Object> params = new HashMap<String, Object>();
		
		ErrorResponseItem errorResponseItem = createNiceMock( ErrorResponseItem.class );
		replay( errorResponseItem );
		
		reset( filterItemMock );
		expect( filterItemMock.callNamedFilter(eq("choose"), eq(params)) ).andStubReturn(errorResponseItem);
		replay( filterItemMock );
		
		assert( filter.getResponseItem( filterItemMock, params ) == null );
	}
	
}
