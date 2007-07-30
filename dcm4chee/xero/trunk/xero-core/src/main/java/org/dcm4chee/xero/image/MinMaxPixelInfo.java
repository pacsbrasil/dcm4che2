package org.dcm4chee.xero.image;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.iod.module.lut.ILut;
import org.dcm4che2.iod.module.lut.LutModule;
import org.dcm4che2.iod.module.lut.RescaleLut;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If the minimum/maximum pixel value isn't already set, this class reads the appropriate
 * headers (hopefully already in memory), and gets the pixel range, useable for window levelling
 * etc.
 * @author bwallace
 *
 */
public class MinMaxPixelInfo  implements Filter<ResultsBean> {
    private static final Logger log = LoggerFactory.getLogger(MinMaxPixelInfo.class);
    
    /** Update any image beans with min/max pixel range information */
	public ResultsBean filter(FilterItem filterItem, Map<String, Object> params) {
		ResultsBean ret = (ResultsBean) filterItem.callNextFilter(params);
		if( ret==null ) return null;
		for( PatientType pt : ret.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				for(SeriesType set : st.getSeries() ) {
					for(DicomObjectType dot : set.getDicomObject() ) {
						if( ! (dot instanceof ImageBean) ) continue;
						ImageBean ib = (ImageBean) dot;
						if( ib.getMinPixel()!=null ) continue;
						updateImage(filterItem, params, ib);
					}
				}
			}
		}
		return ret;
	}

	/** This method uses the imageHeader filter to read the image header and then calls
	 * the updatePixelRange on the DICOM version of this function.
	 * @param fi
	 * @param params
	 * @param ib
	 */
	protected void updateImage(FilterItem fi, Map<String,Object> params, ImageBean ib) {
		// Since we don't know what the dicom filter might add to the params, create a new one
		Map<String,Object> newParams = new HashMap<String,Object>(params);
		newParams.put("objectUID", ib.getSOPInstanceUID());
		DicomObject dobj = (DicomObject) fi.callNamedFilter("dicom",newParams);
		if( dobj==null ) {
			log.warn("Could not read dicom header for this object.");
			return;
		}
		updatePixelRange(dobj,ib);
	}
	
	/** This method takes an existing dicom object, and reads the appropriate min/max pixel values 
	 * and adds the information to the image bean.
	 */
	protected void updatePixelRange(DicomObject dobj, ImageBean ib) {
		LutModule lm = new LutModule(dobj);
		// It is harder to handle more samples, as then we need to know about the mode - so, ignore this for now.
		if( lm.getSamplesPerPixel()!=1 ) return;
		
		ILut lut = lm.getModalityLutModule().getModalityLut();
		if(lut==null ) {
			log.debug("No modality LUT found for "+ib.getSOPInstanceUID());
			lut = new RescaleLut(1,0, "Identity");
		}
		float minPixelValue = lut.lookup(lm.minPossibleStoredValue());
		float maxPixelValue = lut.lookup(lm.maxPossibleStoredValue());
		ib.setMinPixel(minPixelValue);
		ib.setMaxPixel(maxPixelValue);
	}
}
