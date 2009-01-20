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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.web.wicket.folder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.web.dao.StudyListLocal;
import org.dcm4chee.web.wicket.util.DateUtils;
import org.dcm4chee.web.wicket.util.JNDIUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 12, 2008
 */
public class SeriesModel implements Serializable {

    private long pk;
    private boolean selected;
    private boolean details;
    private String sourceAET;
    private DicomObject dataset;
    private List<InstanceModel> instances = new ArrayList<InstanceModel>();

    public SeriesModel(Series series) throws IOException {
        this.pk = series.getPk();
        this.sourceAET = series.getSourceAET();
        this.dataset = series.getAttributes(true);
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public DicomObject getDataset() {
        return dataset;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDetails() {
        return details;
    }

    public void setDetails(boolean details) {
        this.details = details;
    }

    public String getDatetime() {
        return DateUtils.datm2str(
                dataset.getString(Tag.SeriesDate, ""),
                dataset.getString(Tag.SeriesTime, ""));
    }

    public void setDatetime(String datetime) {
        String[] datm = DateUtils.str2datm(datetime);
        dataset.putString(Tag.SeriesDate, VR.DA, datm[0]);
        dataset.putString(Tag.SeriesDate, VR.TM, datm[1]);
    }

    public String getSeriesNumber() {
        return dataset.getString(Tag.SeriesNumber, "");
    }

    public void setSeriesNumber(String seriesNumber) {
        dataset.putString(Tag.SeriesNumber, VR.IS, seriesNumber);
    }

    public String getModality() {
        return dataset.getString(Tag.Modality, "");
    }

    public void setModality(String modality) {
        dataset.putString(Tag.Modality, VR.CS, modality);
    }

    public String getStationName() {
        return dataset.getString(Tag.StationName, "");
    }

    public void setStationName(String stationName) {
        dataset.putString(Tag.StationName, VR.SH, stationName);
    }

    public String getSourceAET() {
        return sourceAET;
    }

    public void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET;
    }

   public String getDescription() {
        return dataset.getString(Tag.SeriesDescription, "");
    }

    public void setDescription(String description) {
        dataset.putString(Tag.SeriesDescription, VR.LO, description);
    }

    public int getNumberOfInstances() {
        return dataset.getInt(Tag.NumberOfSeriesRelatedInstances);
    }

    public String getPPSStartDatetime() {
        return DateUtils.datm2str(
                dataset.getString(Tag.PerformedProcedureStepStartDate, ""),
                dataset.getString(Tag.PerformedProcedureStepStartTime, ""));
    }

    public String getPPSId() {
        return dataset.getString(Tag.PerformedProcedureStepID);
    }

    public String getPPSUid() {
        return dataset.getString(new int[] { 
                Tag.ReferencedPerformedProcedureStepSequence, 0,
                Tag.ReferencedSOPInstanceUID });
    }

    public String getPPSDescription() {
        return dataset.getString(Tag.PerformedProcedureStepDescription);
    }

    public String getAvailability() {
        return dataset.getString(Tag.InstanceAvailability);
    }

    public List<InstanceModel> getInstances() {
        return instances;
    }

    public int getRowspan() {
        int rowspan = details ? 2 : 1;
        for (InstanceModel inst : instances) {
            rowspan += inst.getRowspan();
        }
        return rowspan;
    }

    public void collapse() {
        instances.clear();
    }

    public boolean isCollapsed() {
        return instances.isEmpty();
    }

    public void retainSelectedInstances() {
        for (Iterator<InstanceModel> it = instances.iterator(); it.hasNext();) {
            InstanceModel inst = it.next();
            if (!inst.isSelected()) {
                it.remove();
            }
        }
    }

    public void expand() throws Exception {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        for (Instance inst : dao.findInstancesOfSeries(pk)) {
            this.instances.add(new InstanceModel(inst));
        }
    }

    public void update(DicomObject dicomObject) {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        try {
            dataset = dao.updateSeries(pk, dicomObject).getAttributes(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
