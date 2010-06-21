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

package org.dcm4chee.web.war.folder.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.archive.common.PrivateTag;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 12, 2008
 */
public class SeriesModel extends AbstractEditableDicomModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String sourceAET;
    private String availability;
    private int numberOfSeriesRelatedInstances;
    
    private List<InstanceModel> instances = new ArrayList<InstanceModel>();

    public SeriesModel(Series series, PPSModel ppsModel) {
        if (series == null) {
            setPk(-1);
            dataset = new BasicDicomObject();
            dataset.putString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        } else {
            setPk(series.getPk());
            updateModel(series);
        }
        setParent(ppsModel);
    }

    protected void setPPS(PPSModel ppsModel) {
       setParent(ppsModel);
    }

    public PPSModel getPPS() {
        return (PPSModel)getParent();
    }

    public String getSeriesInstanceUID() {
        return dataset.getString(Tag.SeriesInstanceUID);
    }
    
    public Date getDatetime() {
        return toDate(Tag.SeriesDate, Tag.SeriesTime);
    }

    public String getSeriesDate() {
        return dataset.getString(Tag.SeriesDate);
    }

    public String getSeriesNumber() {
        return dataset.getString(Tag.SeriesNumber);
    }

    public String getModality() {
        return dataset.getString(Tag.Modality);
    }

    public String getStationName() {
        return dataset.getString(Tag.StationName);
    }

    public String getManufacturerModelName() {
        return dataset.getString(Tag.ManufacturerModelName);
    }

    public String getManufacturer() {
        return dataset.getString(Tag.Manufacturer);
    }

    public String getInstitutionalDepartmentName() {
        return dataset.getString(Tag.InstitutionalDepartmentName);
    }

    public String getInstitutionName() {
        return dataset.getString(Tag.InstitutionName);
    }

    public String getSourceAET() {
        return sourceAET;
    }

   public String getDescription() {
        return dataset.getString(Tag.SeriesDescription);
    }

    public int getNumberOfInstances() {
        return numberOfSeriesRelatedInstances;
    }

    public Date getPPSStartDatetime() {
        return toDate(Tag.PerformedProcedureStepStartDate, Tag.PerformedProcedureStepStartTime);
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
        return availability;
    }

    public List<InstanceModel> getInstances() {
        return instances;
    }

    @Override
    public int getRowspan() {
        int rowspan = isDetails() ? 2 : 1;
        for (InstanceModel inst : instances) {
            rowspan += inst.getRowspan();
        }
        return rowspan;
    }

    @Override
    public void collapse() {
        instances.clear();
    }

    @Override
    public boolean isCollapsed() {
        return instances.isEmpty();
    }

    public void retainSelectedInstances() {
        for (Iterator<InstanceModel> it = instances.iterator(); it.hasNext();) {
            InstanceModel inst = it.next();
            inst.retainSelectedFiles();
            if (inst.isCollapsed() && !inst.isSelected()) {
                it.remove();
            }
        }
    }

    @Override
    public void expand() {
        instances.clear();
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        for (Instance inst : dao.findInstancesOfSeries(getPk())) {
            this.instances.add(new InstanceModel(inst, this));
        }
    }

    @Override
    public int levelOfModel() {
        return SERIES_LEVEL;
    }
   
    @Override
    public List<? extends AbstractDicomModel> getDicomModelsOfNextLevel() {
        return instances;
    }

    @Override
    public void update(DicomObject dicomObject) {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        Series s;
        if ( getPk() == -1) {
            s = dao.addSeries(getPPS().getStudy().getPk(), dicomObject);
            setPk(s.getPk());
        }  else {
            s = dao.updateSeries(getPk(), dicomObject);
        }
        updateModel(s);
    }

    private void updateModel(Series s) {
        dataset = s.getAttributes(false);
        availability = s.getAvailability().name();
        numberOfSeriesRelatedInstances = s.getNumberOfSeriesRelatedInstances();
        sourceAET = s.getSourceAET();
    }
    
    @Override
    public void refresh() {
        StudyListLocal dao = (StudyListLocal)
        JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        updateModel(dao.getSeries(getPk()));
    }
    
    public boolean containedBySamePPS(SeriesModel series) {
        String ppsuid1 = getPPSUid();
        String ppsuid2 = series.getPPSUid();
        if (ppsuid1 != null) {
            return ppsuid1.equals(ppsuid2);
        }
        if (ppsuid2 != null) {
            return false;
        }
        return equals(getSourceAET(), series.getSourceAET())
                && equals(getSeriesDate(), series.getSeriesDate())
                && equals(getStationName(), series.getStationName())
                && equals(getManufacturerModelName(),
                        series.getManufacturerModelName())
                && equals(getManufacturer(), series.getManufacturer())
                && equals(getInstitutionalDepartmentName(),
                        series.getInstitutionalDepartmentName())
                && equals(getInstitutionName(), series.getInstitutionName());
    }

    private static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}
