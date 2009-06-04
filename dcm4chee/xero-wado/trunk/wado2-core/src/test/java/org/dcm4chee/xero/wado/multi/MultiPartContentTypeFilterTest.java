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

package org.dcm4chee.xero.wado.multi;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import static org.easymock.classextension.EasyMock.*;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.wado.multi.MultiPartContentTypeFilter;
import org.dcm4chee.xero.wado.multi.MultiPartContentTypeResponseItem;
import org.testng.annotations.*;

import static org.dcm4chee.xero.wado.WadoParams.*;

/**
 * Tests the multi-part content type return.
 * @author tcollins
 *
 */
public class MultiPartContentTypeFilterTest 
{
	Filter<Iterator<ServletResponseItem>> iteratorFilter;
	
	MultiPartContentTypeFilter filter;
	Map<String,Object> params;


	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings("unchecked")
   @BeforeMethod
	public void setUp() throws Exception 
	{
		iteratorFilter = createMock(Filter.class);
		filter = new MultiPartContentTypeFilter();
		filter.setIteratorFilter(iteratorFilter);
		params = new HashMap<String,Object>();
		params.put(MemoryCacheFilter.KEY_NAME, "");
	}

	/**
	 * Test method for {@link org.dcm4chee.xero.wado.multi.MultiPartContentTypeFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
	 */
	@Test
	public void testFilter_WhenObjectUIDIsNull_ShouldReturnNull() 
	{
		params.put( CONTENT_TYPE, "myType" );
		
		assert( filter.filter(null, params) == null );
	}
	

	/**
	 * Test method for {@link org.dcm4chee.xero.wado.multi.MultiPartContentTypeFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
	 */
	@Test
	public void testFilter_WhenContentTypeIsNull_ShouldReturnNull() 
	{
		params.put( OBJECT_UID, "1.2.3" );
		
		assert( filter.filter(null, params) == null );
	}
	
	@Test
	public void testFilter_WhenContentTypeDoesNotHaveMultiPartFirst_ShouldReturnNull()
	{
		params.put( CONTENT_TYPE, "myType," + MultiPartContentTypeFilter.MULTIPART_MIXED );
		params.put( OBJECT_UID, "1.2.3" );
		
		assert( filter.filter(null, params) == null );
	}
	
	@Test
	public void testFilter_WhenIteratorFilterReturnsEmpty_ShouldReturnNotFound() 
	{
		params.put(OBJECT_UID, "1.2.3");
		params.put(CONTENT_TYPE, MultiPartContentTypeFilter.MULTIPART_MIXED);
		expect(iteratorFilter.filter(null, params)).andReturn(new ArrayList<ServletResponseItem>().iterator());
		replay(iteratorFilter);
		ServletResponseItem sri = filter.filter(null,params);
		assert sri!=null;
		ErrorResponseItem eri = (ErrorResponseItem) sri;
		assert eri.getCode()==HttpServletResponse.SC_NO_CONTENT;
	}
	
	@Test
	public void testFilter_WhenIteratorFilterReturnsResponses_ShouldReturnMultiPartWithResponses()
	{
		params.put(OBJECT_UID, "1.2.3");
		params.put(CONTENT_TYPE, MultiPartContentTypeFilter.MULTIPART_MIXED);
		List<ServletResponseItem> returns = new ArrayList<ServletResponseItem>();
		ServletResponseItem sri1 = createMock(ServletResponseItem.class);
		ServletResponseItem sri2 = createMock(ServletResponseItem.class);
		returns.add(sri1);
		returns.add(null);
		returns.add(sri2);
		expect(iteratorFilter.filter(null, params)).andReturn(returns.iterator());
		replay(iteratorFilter);
		ServletResponseItem sri = filter.filter(null,params);
		assert sri!=null;
		MultiPartContentTypeResponseItem mri = (MultiPartContentTypeResponseItem) sri;
		Iterator<ServletResponseItem> it = mri.getResponseIterator();
		assert it.hasNext();
		assert it.next()==sri1;
		assert it.hasNext();
		assert it.next()==null;
		assert it.hasNext();
		assert it.next()==sri2;
		assert it.hasNext()==false;
		
	}
}
