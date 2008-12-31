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

import static org.dcm4chee.xero.wado.DicomImageFilter.updateParamFromRegion;
import static org.dcm4chee.xero.wado.WadoParams.FRAME_NUMBER;
import static org.dcm4chee.xero.wado.WadoParams.OBJECT_UID;

import java.util.Map;

import javax.imageio.ImageReadParam;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCache extends MemoryCacheFilter<WadoImage> {
	private static Logger log = LoggerFactory.getLogger(ImageCache.class);

	/** Add a key computation that includes the region/row sizes.  This modifies the returned file name. */
	@Override
	public String computeKey(Map<String, Object> params) {
 	  if (params == null)
 		 throw new IllegalArgumentException("Params to filter and compute key should not be null.");
 	  StringBuffer key = new StringBuffer((String) params.get(OBJECT_UID));

 	  int frameNumber = FilterUtil.getInt(params,FRAME_NUMBER,1);
 	  key.append("-f").append(frameNumber+1);

 	  DicomObject ds = dicomImageHeader.filter(null,params);
 	  if( ds==null ) return null;
 	  int width = ds.getInt(Tag.Columns);
 	  int height = ds.getInt(Tag.Rows);
 	  if( width==0 || height==0 ) {
 		  log.warn("No width/height found in image - not clear what can be cached:{}",key);
 		  return key.toString();
 	  }

 	  key.append(updateParamFromRegion(new ImageReadParam(), params, width, height));
	  log.debug("ImageCache key is {}",key);
 	  return key.toString();
   }

   private Filter<DicomObject> dicomImageHeader;

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomImageHeader() {
   	return dicomImageHeader;
   }

	@MetaData(out="${ref:dicomImageHeader}")
	public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
   	this.dicomImageHeader = dicomImageHeader;
   }
}
