package org.dcm4chee.xero.wado.multi;

import static org.easymock.classextension.EasyMock.*;
import static org.dcm4chee.xero.wado.WadoParams.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.easymock.Capture;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MultipartCallFilterTest {
	MultipartCallFilter filter = new MultipartCallFilter();
	Map<String,Object> params;
	Map<String,Object> addDefaults;
	FilterItem<Iterator<ServletResponseItem>> filterItem;
	ServletResponseItem sri1, sri2, sri3;
	List<ServletResponseItem> l1;
	Iterator<ServletResponseItem> it1;
	Filter<ServletResponseItem> childFilter;
	
   @BeforeMethod
	@SuppressWarnings("unchecked")
	void init() {
		params = new HashMap<String,Object>();
		addDefaults = new HashMap<String,Object>();
		filterItem = createMock( FilterItem.class );
		sri1 = createMock(ServletResponseItem.class);
		sri2 = createMock(ServletResponseItem.class);
		sri3 = createMock(ServletResponseItem.class);
		filter.setChildName("child");
		l1 = new ArrayList<ServletResponseItem>();
		l1.add(sri1);
		it1 = l1.iterator();
		childFilter = createMock(Filter.class);
		filter.setChildFilter(childFilter);
	}
	
	@Test
	public void testFilter_withNoMatch_returnNext() {
		// Four names with child in them - should still not all the item.
		params.put(MULTIPART_KEY, "childa,childa,achildb,achild");
		expect(filterItem.callNextFilter(params)).andReturn(it1);	
		replay(filterItem);
		
		assert filter.filter(filterItem,params)==it1;
		verify(filterItem);
	}
	
	@SuppressWarnings("unchecked")
   @Test 
	public void testFilter_withMatch_returnsCombinedChildNext() {
		params.put(MULTIPART_KEY, "child,next");
		params.put("child.contentType", "image/png");
		expect(filterItem.callNextFilter(params)).andReturn(it1);
		Capture<Map<String,Object>> cap = new Capture<Map<String,Object>>();
		expect(childFilter.filter((FilterItem) isNull(),capture(cap))).andReturn(sri2);
		replay(filterItem,childFilter);
		
		Iterator<ServletResponseItem> it = filter.filter(filterItem, params);
		assert it!=it1;
		assert it.hasNext();
		assert it.next()==sri2;
		assert it.hasNext();
		assert it.next()==sri1;
		assert !it.hasNext();

		// Ensure that over-ride values are correctly set.
		Map<String,Object> usedParams = cap.getValue();
		assert usedParams!=null;
		String contentTypeValue = (String) usedParams.get("contentType");
		assert contentTypeValue!=null;
		assert contentTypeValue.equals("image/png");
		
		verify(filterItem);
		verify(childFilter);
	}

	@Test 
	public void testFilter_withMatchButNoReturn_returnsNextIt() {
		params.put(MULTIPART_KEY, "child,next");
		expect(filterItem.callNextFilter(params)).andReturn(it1);
		expect(childFilter.filter(null,params)).andReturn(null);
		replay(filterItem,childFilter);
		
		Iterator<ServletResponseItem> it = filter.filter(filterItem, params);
		assert it==it1;
		assert it.hasNext();
		assert it.next()==sri1;
		assert !it.hasNext();
		verify(filterItem);
		verify(childFilter);
	}
}
