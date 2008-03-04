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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chee.archive.conf;

import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.exceptions.ConfigurationException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 26, 2008
 */
public final class AttributeFilter {
    private static final String CONFIG_URL =
            "resource:dcm4chee-attribute-filter.xml";
    private static final int[] PATIENT_TAGS = {
        Tag.ReferencedPatientSequence,
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
    };
    static AttributeFilter patientFilter;
    static AttributeFilter studyFilter;
    static AttributeFilter seriesFilter;
    static HashMap<String,AttributeFilter> instanceFilters =
            new HashMap<String,AttributeFilter>();
    private int[] tags = {};
    private int[] noCoercion = {};
    private int[] fieldTags;
    private String[] fields = {};
    private String tsuid;
    private boolean overwrite;
    private boolean merge;

    static {
        AttributeFilterLoader.loadFrom(CONFIG_URL);
    }

    public static long lastModified() {
        URLConnection conn;
        try {
            conn = new URL(CONFIG_URL).openConnection();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        return conn.getLastModified();
    }

    public static DicomObject exludePatientAttributes(DicomObject ds) {
        return ds.exclude(PATIENT_TAGS);
    }

    public static AttributeFilter getPatientAttributeFilter() {
        return patientFilter;
    }

    public static AttributeFilter getStudyAttributeFilter() {
        return studyFilter;
    }

    public static AttributeFilter getSeriesAttributeFilter() {
        return seriesFilter;
    }

    public static AttributeFilter getInstanceAttributeFilter(String cuid) {
        AttributeFilter filter = (AttributeFilter) instanceFilters.get(cuid);
        if (filter == null) {
            filter = (AttributeFilter) instanceFilters.get(null);
        }
        return filter;
    }

    final void setNoCoercion(int[] noCoercion) {
        this.noCoercion = noCoercion;
    }

    final void setTags(int[] tags) {
        this.tags = tags;
    }

    final void setFields(String[] fields) {
        this.fields = fields;
    }

    final void setFieldTags(int[] fieldTags) {
        this.fieldTags = fieldTags;
    }

    public final int[] getFieldTags() {
        return this.fieldTags;
    }

    public String getField(int tag) {
        int index = Arrays.binarySearch(fieldTags, tag);
        return index < 0 ? null : fields[index];
    }

    public boolean isCoercionForbidden(int tag) {
        return Arrays.binarySearch(noCoercion, tag) >= 0;
    }

    public final String getTransferSyntaxUID() {
        return tsuid;
    }

    final void setTransferSyntaxUID(String tsuid) {
        this.tsuid = tsuid;
    }

    public final boolean isOverwrite() {
        return overwrite;
    }

    final void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public final boolean isMerge() {
        return merge;
    }

    final void setMerge(boolean merge) {
        this.merge = merge;
    }

    public DicomObject filter(DicomObject ds) {
        return ds.subSet(tags);
    }

}
