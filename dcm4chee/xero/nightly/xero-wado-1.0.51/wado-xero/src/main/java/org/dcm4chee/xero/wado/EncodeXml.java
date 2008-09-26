package org.dcm4chee.xero.wado;

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/**
 * Calls the filter to get the DICOM header and then returns an XML encoder for the object.
 * @author bwallace
 *
 */
public class EncodeXml implements Filter<ServletResponseItem>{

   /** Encodes the raw dicom object as XML */
   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
	  DicomObject ds = DicomFilter.filterDicomObject(filterItem, params,null);
	  if( ds==null ) return filterItem.callNextFilter(params);
	  return new XmlServletResponseItem(ds);
   }

}
