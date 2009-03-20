package org.dcm4chee.xero.wado.multi;

import static org.dcm4chee.xero.wado.WadoParams.OBJECT_UID;
import static org.dcm4chee.xero.wado.WadoParams.STUDY_UID;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.isNull;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.search.study.GspsType;
import org.dcm4chee.xero.search.study.ImageType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SeriesUidItFilterTest {

	SeriesUidItFilter filter = new SeriesUidItFilter();
	Map<String,Object> params;
	FilterItem<Iterator<ServletResponseItem>> filterItem;
	Filter<ResultsBean> resultsFilter;
	Iterator<ServletResponseItem> iterator;
	ResultsBean rt;
	
   @BeforeMethod
	@SuppressWarnings("unchecked")
	void init() {
		params = new HashMap<String,Object>();
		filterItem = createMock( FilterItem.class );
		iterator = createMock( Iterator.class );
		resultsFilter = createMock(Filter.class);
		filter.setImageFilter(resultsFilter);
		rt = new ResultsBean();
	}
	
	@Test
	public void testFilter_withObjectUid_returnNext() {
		params.put(OBJECT_UID, "1.2.3");
		expect(filterItem.callNextFilter(params)).andReturn(iterator);
		replay(filterItem);
		assert filter.filter(filterItem,params)==iterator;
		verify(filterItem);
	}
	
	@SuppressWarnings("unchecked")
    @Test
	public void testFilter_withEmptyResults_returnNull() {
		params.put(STUDY_UID,"1.2.3");
		expect(resultsFilter.filter((FilterItem) isNull(),(Map) notNull())).andReturn(rt);
		replay(resultsFilter);
		assert filter.filter(filterItem,params)==null;
		verify(resultsFilter);
	}
	
	@SuppressWarnings("unchecked")
    @Test
	public void testFilter_withTwoUids_returnNextIterator() {
		params.put(STUDY_UID,"1.2.3");
		PatientType pt = new PatientType();
		rt.getPatient().add(pt);
		StudyType st =new StudyType();
		pt.getStudy().add(st);
		SeriesType set = new SeriesType();
		st.getSeries().add(set);
		ImageType img = new ImageType();
		img.setObjectUID("1.2.3.a");
		GspsType gst = new GspsType();
		gst.setObjectUID("1.2.3.b");
		set.getDicomObject().add(img);
		set.getDicomObject().add(gst);
        expect(resultsFilter.filter((FilterItem) isNull(),(Map) notNull())).andReturn(rt);
		expect(filterItem.callNextFilter(params)).andReturn(iterator);
		replay(resultsFilter, filterItem);
		assert filter.filter(filterItem,params)==iterator;
		assert params.get(OBJECT_UID).equals("1.2.3.a\\1.2.3.b");
		verify(resultsFilter,filterItem);
	}
}
