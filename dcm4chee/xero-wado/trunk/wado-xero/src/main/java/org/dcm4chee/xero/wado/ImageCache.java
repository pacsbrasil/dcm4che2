/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.metadata.filter.FilterUtil.getFloats;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getInt;
import static org.dcm4chee.xero.wado.WadoParams.*;

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCache extends MemoryCacheFilter<WadoImage> {
	private static Logger log = LoggerFactory.getLogger(ImageCache.class);

	Filter<DicomObject> dicomImageObject;


	/** Add a key computation that includes the region/row sizes.  This modifies the returned file name. */
	@Override
	public String computeKey(Map<String, Object> params) {
 	  if (params == null)
 		 throw new IllegalArgumentException("Params to filter and compute key should not be null.");
 	  StringBuffer key = new StringBuffer((String) params.get(OBJECT_UID));

 	  DicomObject ds = dicomImageObject.filter(null,params);
 	  int width = ds.getInt(Tag.Columns);
 	  int height = ds.getInt(Tag.Rows);
 	  if( width==0 || height==0 ) {
 		  log.warn("No width/height found in image - not clear what can be cached:{}",key);
 		  return key.toString();
 	  }
 	  float[] region = getFloats(params, REGION, null);
	  int rows = getInt(params, ROWS);
	  int cols = getInt(params, COLUMNS);
	  log.debug("rows=" + rows + " cols=" + cols+" region="+region);

	  int sWidth = width, sHeight = height;
	  if (region != null) {
		 // Figure out the sub-region to use
		 int xOffset = (int) (region[0] * width);
		 int yOffset = (int) (region[1] * height);
		 sWidth = (int) ((region[2] - region[0]) * width);
		 sHeight = (int) ((region[3] - region[1]) * height);
		 if( xOffset>0 && yOffset>0 && sWidth < width && sHeight<height) {
			 key.append("-r").append(xOffset).append(',').append(yOffset).append(',').append(sWidth).append(',').append(sHeight);
		 }
	  }
	  
	  int subsampleX = 1;
	  int subsampleY = 1;
	  if (cols != 0) {
		 subsampleX = sWidth / cols;
		 subsampleY = subsampleX;
	  }
	  if (rows != 0) {
		 subsampleY = sHeight / rows;
		 if (cols == 0)
			subsampleX = subsampleY;
	  }
	  // Can't over-sample the data...
	  if (subsampleX < 1)
		 subsampleX = 1;
	  if (subsampleY < 1)
		 subsampleY = 1;
	  if( subsampleX>1 && subsampleY>1 ) { 
		  key.append("-s").append(subsampleX).append(",").append(subsampleY);
	  }
 	  return key.toString();
   }

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomImageObject() {
   	return dicomImageObject;
   }

	@MetaData(out="${ref:dicomImageObject}")
	public void setDicomImageObject(Filter<DicomObject> dicomImageObject) {
   	this.dicomImageObject = dicomImageObject;
   }

}
