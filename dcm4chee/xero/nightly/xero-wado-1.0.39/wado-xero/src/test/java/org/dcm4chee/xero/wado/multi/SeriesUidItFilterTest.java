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
import org.dcm4chee.xero.search.study.GspsType;
import org.dcm4chee.xero.search.study.ImageType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsType;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SeriesUidItFilterTest {

	SeriesUidItFilter filter = new SeriesUidItFilter();
	Map<String,Object> params;
	FilterItem<Iterator<ServletResponseItem>> filterItem;
	Filter<ResultsType> resultsFilter;
	Iterator<ServletResponseItem> iterator;
	ResultsType rt;
	
   @BeforeMethod
	@SuppressWarnings("unchecked")
	void init() {
		params = new HashMap<String,Object>();
		filterItem = createMock( FilterItem.class );
		iterator = createMock( Iterator.class );
		resultsFilter = createMock(Filter.class);
		filter.setImageFilter(resultsFilter);
		rt = new ResultsType();
	}
	
	@Test
	public void testFilter_withObjectUid_returnNext() {
		params.put(OBJECT_UID, "1.2.3");
		expect(filterItem.callNextFilter(params)).andReturn(iterator);
		replay(filterItem);
		assert filter.filter(filterItem,params)==iterator;
		verify(filterItem);
	}
	
	@Test
	public void testFilter_withEmptyResults_returnNull() {
		params.put(STUDY_UID,"1.2.3");
		expect(resultsFilter.filter(null,params)).andReturn(rt);
		replay(resultsFilter);
		assert filter.filter(filterItem,params)==null;
		verify(resultsFilter);
	}
	
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
		expect(resultsFilter.filter(null,params)).andReturn(rt);
		expect(filterItem.callNextFilter(params)).andReturn(iterator);
		replay(resultsFilter, filterItem);
		assert filter.filter(filterItem,params)==iterator;
		assert params.get(OBJECT_UID).equals("1.2.3.a\\1.2.3.b");
		verify(resultsFilter,filterItem);
	}
}
