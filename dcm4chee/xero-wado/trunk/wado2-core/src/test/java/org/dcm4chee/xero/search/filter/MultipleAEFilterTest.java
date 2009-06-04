package org.dcm4chee.xero.search.filter;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.EasyMock.expect;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.wado.WadoParams;
import org.testng.annotations.BeforeMethod;

/** Tests the multiple AE filter */
public class MultipleAEFilterTest {

	MultipleAEFilter multipleFilter = new MultipleAEFilter();
	FilterItem<ResultFromDicom> filterItem;
	ResultFromDicom rfd;
	Map<String,Object> params = new HashMap<String,Object>();
	
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void init() throws Exception {
		filterItem = createMock(FilterItem.class);
		rfd = createMock(ResultFromDicom.class);
	}
	
	public void test_localAE_expectSingleCall() {
		expect(filterItem.callNextFilter(params)).andReturn(rfd);
		replay(filterItem,rfd);
		assert rfd==multipleFilter.filter(filterItem, params);
		verify(filterItem,rfd);
	}

	public void test_singleAE_expectSingleCall() {
		params.put(WadoParams.AE,"local");
		expect(filterItem.callNextFilter(params)).andReturn(rfd);
		replay(filterItem,rfd);
		assert rfd==multipleFilter.filter(filterItem, params);
		verify(filterItem,rfd);
	}

	public void test_doubleAE_expectDoubleCall() {
		params.put(WadoParams.AE,"XERO\\tls");
		expect(filterItem.callNextFilter(params)).andReturn(rfd);
		expect(filterItem.callNextFilter(params)).andReturn(rfd);
		replay(filterItem,rfd);
		assert rfd==multipleFilter.filter(filterItem, params);
		verify(filterItem,rfd);
	}

	public void test_aeMultiple_expectDoubleCall() {
		params.put(WadoParams.AE,"multiple");
		expect(filterItem.callNextFilter(params)).andReturn(rfd);
		expect(filterItem.callNextFilter(params)).andReturn(rfd);
		replay(filterItem,rfd);
		assert rfd==multipleFilter.filter(filterItem, params);
		verify(filterItem,rfd);
	}
}
