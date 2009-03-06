package org.dcm4chee.xero.wado;

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/** Returns encapsulated object responses */
public class EncapsulatedFilter implements Filter<ServletResponseItem> {

	public ServletResponseItem filter(
			FilterItem<ServletResponseItem> filterItem,
			Map<String, Object> params) {
		if( params.containsKey("contentType") ) return filterItem.callNextFilter(params);
		DicomObject dobj = dicomImageHeader.filter(null, params);
		boolean encapsulated = dobj.contains(Tag.MIMETypeOfEncapsulatedDocument);
		if( !encapsulated ) return filterItem.callNextFilter(params);
		byte[] data = dobj.getBytes(Tag.EncapsulatedDocument);
		return new ByteServletResponseItem(data,dobj.getString(Tag.MIMETypeOfEncapsulatedDocument));
	}

	private Filter<DicomObject> dicomImageHeader;

	/** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomImageHeader() {
		return dicomImageHeader;
	}

	@MetaData(out = "${ref:dicomImageHeader}")
	public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
		this.dicomImageHeader = dicomImageHeader;
	}

}
