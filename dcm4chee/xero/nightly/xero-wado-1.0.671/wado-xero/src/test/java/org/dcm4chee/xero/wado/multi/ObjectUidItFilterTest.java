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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ObjectUidItFilterTest {
	Filter<Iterator<ServletResponseItem>> filter = new ObjectUidItFilter();
	Map<String,Object> params;
	FilterItem<Iterator<ServletResponseItem>> filterItem;
	List<ServletResponseItem> l1, l3;
	ServletResponseItem sri1a, sri1b, sri3;
	Iterator<ServletResponseItem> it1, it2, it3;
	
   @BeforeMethod
	@SuppressWarnings("unchecked")
	void init() {
		params = new HashMap<String,Object>();
		filterItem = createMock( FilterItem.class );
		l1 = new ArrayList<ServletResponseItem>();
		l3 = new ArrayList<ServletResponseItem>();
		sri1a = createMock(ServletResponseItem.class);
		sri1b = createMock(ServletResponseItem.class);
		l1.add(sri1a);
		l1.add(sri1b);
		sri3 = createMock(ServletResponseItem.class);
		l3.add(sri3);
		it1 = l1.iterator();
		it2 = null;
		it3 = l3.iterator();
	}
	
	@Test
	public void testFilter_withNoUid_returnNull() {
		assert filter.filter(null,params)==null;
	}
	
	@Test
	public void testFilter_withSingleUid_returnNextFilter() {
		expect(filterItem.callNextFilter(eq(params))).andReturn(it1);
		replay(filterItem);
		params.put(OBJECT_UID,"1.2.3");
		Iterator<ServletResponseItem> it = filter.filter(filterItem,params);
		verify(filterItem);
		assert it==it1;
	}
	
	@Test
	public void testFilter_withMultipleUid_returnCombinedNextFilters() {
		expect(filterItem.callNextFilter(eq(params))).andReturn(it1);
		expect(filterItem.callNextFilter(eq(params))).andReturn(it2);
		expect(filterItem.callNextFilter(eq(params))).andReturn(it3);		
		replay(filterItem);
		params.put(OBJECT_UID,"1.2.3\\1.2.4\\1.2.5");
		Iterator<ServletResponseItem> it = filter.filter(filterItem,params);
		assert it!=it1;
		assert it.hasNext();
		assert it.next()==sri1a;
		assert it.next()==sri1b;
		assert it.hasNext();
		assert it.next()==sri3;
		assert !it.hasNext();
		verify(filterItem);
	}
}
