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

import java.io.IOException;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter just knows how to convert a dicom image reader into a dicom
 * object
 */
public class DicomImageReaderToDicomObject implements Filter<DicomObject> {
	private static final Logger log = LoggerFactory.getLogger(DicomImageReaderToDicomObject.class);
	Filter<DicomImageReader> imageReaderFilter;

	/** Just read the dicom object from the header */
	@Override
	public DicomObject filter(FilterItem<DicomObject> filterItem, Map<String, Object> params) {
		DicomImageReader dir = imageReaderFilter.filter(null, params);
		if (dir == null)
			return null;
		try {
			return ((DicomStreamMetaData) dir.getStreamMetadata()).getDicomObject();
		} catch (IOException e) {
			log.warn("Unable to read dicom file:", e);
			throw new RuntimeException("Unalbe to read dicom file", e);
		}
	}

	/** Gets the image reader filter that reads the dicom object in */
	public Filter<DicomImageReader> getImageReaderFilter() {
		return imageReaderFilter;
	}

	/** Sets the default dicom reader to be "dicom" */
	@MetaData(out = "${ref:dicom}")
	public void setImageReaderFilter(Filter<DicomImageReader> imageReaderFilter) {
		this.imageReaderFilter = imageReaderFilter;
	}

}
