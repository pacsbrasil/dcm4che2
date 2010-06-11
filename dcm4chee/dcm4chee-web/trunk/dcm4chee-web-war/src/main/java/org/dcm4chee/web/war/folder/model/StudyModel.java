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
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 12, 2008
 */
public class StudyModel extends AbstractEditableDicomModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<PPSModel> ppss = new ArrayList<PPSModel>();

    private PatientModel parent;

    private String availability;
    private int numberOfStudyRelatedSeries;
    private int numberOfStudyRelatedInstances;
    
    public StudyModel(Study study, PatientModel patModel) {
        if (study == null) {
            setPk(-1);
            dataset = new BasicDicomObject();
            dataset.putString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        } else {
            setPk(study.getPk());
            updateModel(study);
        }
        setParent(patModel);
    }

    private void setParent(PatientModel patModel) {
        parent = patModel;
    }

    public PatientModel getParent() {
        return parent;
    }

    public String getStudyInstanceUID() {
        return dataset.getString(Tag.StudyInstanceUID, "");
    }

    public Date getDatetime() {
        return toDate(Tag.StudyDate, Tag.StudyTime);
    }

    public void setDatetime(Date datetime) {
        
        dataset.putDate(Tag.StudyDate, VR.DA, datetime);
        dataset.putDate(Tag.StudyTime, VR.TM, datetime);
    }
    
    public String getId() {
        return dataset.getString(Tag.StudyID);
    }

    public String getAccessionNumber() {
        return dataset.getString(Tag.AccessionNumber, "");
    }

    public String getModalities() {
        return toString(dataset.getStrings(Tag.ModalitiesInStudy));
    }

    private String toString(String[] ss) {
        if (ss == null || ss.length == 0) {
            return null;
        }
        if (ss.length == 1) {
            return ss[0];
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ss[0]);
        for (int i = 1; i < ss.length; i++) {
            sb.append('\\').append(ss[i]);
        }
        return sb.toString();
    }

    public String getDescription() {
        return dataset.getString(Tag.StudyDescription);
    }

    public void setDescription(String description) {
        dataset.putString(Tag.StudyDescription, VR.LO, description);
    }

    public int getNumberOfSeries() {
        return numberOfStudyRelatedSeries;
    }

    public int getNumberOfInstances() {
        return numberOfStudyRelatedInstances;
    }

    public String getAvailability() {
        return availability;
    }

    public List<PPSModel> getPPSs() {
        return ppss;
    }

    @Override
    public int getRowspan() {
        int rowspan = isDetails() ? 2 : 1;
        for (PPSModel pps : ppss) {
            rowspan += pps.getRowspan();
        }
        return rowspan;
    }

    @Override
    public void collapse() {
        ppss.clear();
    }

    @Override
    public boolean isCollapsed() {
        return ppss.isEmpty();
    }

    public void retainSelectedPPSs() {
        for (Iterator<PPSModel> it = ppss.iterator(); it.hasNext();) {
            PPSModel pps = it.next();
            pps.retainSelectedSeries();
            if (pps.isCollapsed() && !pps.isSelected()) {
                it.remove();
            }
        }
    }

    @Override
    public void expand() {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        for (Series series : dao.findSeriesOfStudy(getPk())) {
            add(series);
        }
    }

    @Override
    public int levelOfModel() {
        return STUDY_LEVEL;
    }
   
    @Override
    public List<? extends AbstractDicomModel> getDicomModelsOfNextLevel() {
        return ppss;
    }
    
    private void add(Series series) {
        MPPS mpps = series.getModalityPerformedProcedureStep();
        SeriesModel seriesModel = new SeriesModel(series,null);
        for (PPSModel pps : ppss) {
            if (mpps != null ? mpps.getPk() == pps.getPk()
                    : pps.getDataset() == null 
                            && seriesModel.containedBySamePPS(pps.getSeries1())) {
                seriesModel.setParent(pps);
                pps.getSeries().add(seriesModel);
                return;
            }
        }
        PPSModel pps = new PPSModel(mpps, seriesModel, this);
        ppss.add(pps);
    }

    @Override
    public void update(DicomObject dicomObject) {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        Study s;
        if (getPk() == -1) {
            s = dao.addStudy(parent.getPk(), dicomObject);
            setPk(s.getPk());
        } else {
            s = dao.updateStudy(getPk(), dicomObject);
        }
        updateModel(s);
    }
    
    @Override
    public void refresh() {
        StudyListLocal dao = (StudyListLocal)
        JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        updateModel(dao.getStudy(getPk()));
    }    
    
    private void updateModel(Study s) {
        dataset = s.getAttributes(false);
        availability = s.getAvailability().name();
        numberOfStudyRelatedSeries = s.getNumberOfStudyRelatedSeries();
        numberOfStudyRelatedInstances = s.getNumberOfStudyRelatedInstances();
    }
    
    
}
