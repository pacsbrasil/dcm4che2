package org.dcm4chee.xero.wado;

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/** This class adds the dicom object to the model so that it can be rendered or otherwise used. */
public class DSModel  implements Filter<ServletResponseItem>{

	/** Add the ds (dicom object) to the model in the params */
	@SuppressWarnings("unchecked")
	public ServletResponseItem filter(
			FilterItem<ServletResponseItem> filterItem,
			Map<String, Object> params) {
		DicomObject ds = DicomFilter
		.filterDicomObject(filterItem, params, null);
		if( ds!=null ) {
			((Map<String,Object>) params.get("model")).put("ds", new DicomObjectMap(ds));
		}
		return filterItem.callNextFilter(params);
	}

}
