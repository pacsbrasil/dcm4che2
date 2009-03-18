package org.dcm4chee.xero.wado;

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/**
 * This class adds the dicom object to the model so that it can be rendered or
 * otherwise used.
 */
public class DSModel implements Filter<ServletResponseItem> {

	/** Add the ds (dicom object) to the model in the params */
	public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
		DicomObject ds = dicomUpdatedHeader.filter(null, params);
		if (ds != null) {
			FilterUtil.getModel(params).put("ds", new DicomObjectMap(ds));
		}
		String template = FilterUtil.getString(params,"template");
		if( template!=null )
			FilterUtil.getModel(params).put("template", template);
		return filterItem.callNextFilter(params);
	}


	private Filter<DicomObject> dicomUpdatedHeader;

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomUpdatedHeader() {
   	return dicomUpdatedHeader;
   }

	@MetaData(out="${ref:dicomUpdatedHeader}")
	public void setDicomUpdatedHeader(Filter<DicomObject> dicomUpdatedHeader) {
   	this.dicomUpdatedHeader = dicomUpdatedHeader;
   }
}
