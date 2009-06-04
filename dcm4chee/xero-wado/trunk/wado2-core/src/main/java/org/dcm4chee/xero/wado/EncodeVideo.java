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
 * Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2009
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
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodeVideo implements Filter<ServletResponseItem> {

    private static final Logger log = LoggerFactory.getLogger(EncodeVideo.class);

    /** Returns a servlet response containing an MPEG2 video stream, assuming
     * the object is already in that format.
     */
    public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
        DicomObject dobj = dicomImageHeader.filter(null, params);
        String ts = dobj.getString(Tag.TransferSyntaxUID);
        if (!UID.MPEG2.equals(ts))
            return filterItem.callNextFilter(params);
        DicomImageReader dir = dicomImageReaderFilter.filter(null, params);
        if (dir == null)
            return null;
        synchronized (dir) {
            try {
                byte[] data = dir.readBytes(0, null);
                return new ByteServletResponseItem(data, "video/mpeg2",dobj.getString(Tag.SOPInstanceUID)+".m2v");
            } catch (IOException e) {
                log.warn("Unable to read MPEG2 stream:"+e);
                e.printStackTrace();
                return null;
            }
        }
    }

    private Filter<DicomImageReader> dicomImageReaderFilter;

    public Filter<DicomImageReader> getDicomImageReaderFilter() {
        return dicomImageReaderFilter;
    }

    /**
     * Set the filter that reads the dicom image reader objects for a given SOP
     * UID
     * 
     * @param dicomFilter
     */
    @MetaData(out = "${ref:dicomImageReader}")
    public void setDicomImageReaderFilter(Filter<DicomImageReader> dicomImageReaderFilter) {
        this.dicomImageReaderFilter = dicomImageReaderFilter;
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
