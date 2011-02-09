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

package org.dcm4chex.archive.web.maverick;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.Convert;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.InstanceModel;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.SeriesModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.WeasisLauncher;
import org.weasis.launcher.wado.Patient;
import org.weasis.launcher.wado.SOPInstance;
import org.weasis.launcher.wado.Series;
import org.weasis.launcher.wado.Study;

public class FolderSubmitCtrl2 extends FolderSubmitCtrl {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderSubmitCtrl2.class);

    private WeasisLauncher weasisLauncher = null;

    @Override
    protected String perform() throws Exception {
        String folder = super.perform();
        // Fix WEA-27, only when folder
        if ("folder".equals(folder)) {
            HttpServletRequest rq = getCtx().getRequest();
            Set allowedMethods = getPermissions().getPermissionsForApp("folder");
            if (allowedMethods.contains("folder.view")) {
                if (rq.getParameter("view") != null || rq.getParameter("view.x") != null) {
                    return view();
                }
            }
        }
        return folder;
    }

    private String view() {
        FolderForm folderForm = (FolderForm) getForm();
        try {
            viewPatients(folderForm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return FOLDER;
    }

    private void viewPatients(FolderForm folderForm) throws Exception {
        List patients = folderForm.getPatients();
        ArrayList<Patient> patientsToView = new ArrayList<Patient>();
        if (patients != null) {
            for (int i = 0; i < patients.size(); i++) {
                PatientModel pat = (PatientModel) patients.get(i);
                Dataset pdata = pat.toDataset();
                Patient patient = new Patient(pdata.getString(Tags.PatientID));
                patient.setPatientName(pdata.getString(Tags.PatientName));
                patient.setPatientBirthDate(pdata.getString(Tags.PatientBirthDate));
                patient.setPatientBirthTime(pdata.getString(Tags.PatientBirthTime));
                patient.setPatientSex(pdata.getString(Tags.PatientSex));
                if (folderForm.isSticky(pat)) {
                    addStudiesOfPatientForView(patient, pat.getPk());
                } else {
                    List studies = pat.getStudies();
                    for (int j = 0; j < studies.size(); j++) {
                        final StudyModel studyModel = (StudyModel) studies.get(j);
                        Study study = getStudyForView(patient, studyModel.toDataset());
                        if (folderForm.isSticky(studyModel)) {
                            addAllSeriesForView(study, studyModel.getPk());
                            if (!study.isEmpty()) {
                                patient.addStudy(study);
                            }
                        } else {
                            List series = studyModel.getSeries();
                            for (int k = 0; k < series.size(); k++) {
                                final SeriesModel seriesModel = (SeriesModel) series.get(k);
                                Series s = getSeriesForView(study, seriesModel.toDataset());
                                if (folderForm.isSticky(seriesModel)) {
                                    addAllInstancesForView(s, seriesModel.getPk());
                                    if (!s.isEmpty()) {
                                        study.addSeries(s);
                                        patient.addStudy(study);
                                    }
                                } else {
                                    List instances = seriesModel.getInstances();
                                    for (int m = 0; m < instances.size(); m++) {
                                        final InstanceModel inst = (InstanceModel) instances.get(m);
                                        if (folderForm.isSticky(inst)) {
                                            s.addSOPInstance(new SOPInstance(inst.getSopIUID()));
                                        }
                                    }
                                    if (!s.isEmpty()) {
                                        study.addSeries(s);
                                        patient.addStudy(study);
                                    }
                                }

                            }
                        }
                    }
                }
                if (!patient.isEmpty()) {
                    patientsToView.add(patient);
                }
            }
        }
        if (weasisLauncher == null) {
            weasisLauncher = new WeasisLauncher(getCtx());
        }
        weasisLauncher.displayImages(patientsToView, getCtx());
        folderForm.removeStickies();
    }

    private void addStudiesOfPatientForView(Patient patient, long patPk) throws Exception {
        List studies = listStudiesOfPatient2(patPk);
        for (int i = 0; i < studies.size(); i++) {
            final Dataset ds = (Dataset) studies.get(i);
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            Study s = getStudyForView(patient, ds);
            ByteBuffer bb = ds.getByteBuffer(PrivateTags.StudyPk);
            long studyPk = bb == null ? -1 : Convert.toLong(bb.array());
            addAllSeriesForView(s, studyPk);
            patient.addStudy(s);
        }
    }

    private Study getStudyForView(Patient patient, final Dataset studyDataset) throws Exception {
        String uid = studyDataset.getString(Tags.StudyInstanceUID);
        Study s = patient.getStudy(uid);
        if (s == null) {
            s = new Study(uid);
            s.setStudyDescription(studyDataset.getString(Tags.StudyDescription));
            s.setStudyDate(studyDataset.getString(Tags.StudyDate));
            s.setStudyTime(studyDataset.getString(Tags.StudyTime));

        }
        return s;
    }

    private void addAllSeriesForView(Study study, long studyPk) throws Exception {
        List series = listSeriesOfStudy(studyPk);
        for (int i = 0; i < series.size(); i++) {
            final Dataset ds = (Dataset) series.get(i);
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            Series s = getSeriesForView(study, ds);
            ByteBuffer bb = ds.getByteBuffer(PrivateTags.SeriesPk);
            long seriesPk = bb == null ? -1 : Convert.toLong(bb.array());
            addAllInstancesForView(s, seriesPk);
            study.addSeries(s);
        }
    }

    private Series getSeriesForView(Study study, final Dataset seriesDataset) throws Exception {
        String uid = seriesDataset.getString(Tags.SeriesInstanceUID);
        Series s = study.getSeries(uid);
        if (s == null) {
            s = new Series(uid);
            s.setModality(seriesDataset.getString(Tags.Modality));
            s.setSeriesNumber(seriesDataset.getString(Tags.SeriesNumber));
            s.setSeriesDescription(seriesDataset.getString(Tags.SeriesDescription));
        }
        return s;
    }

    private void addAllInstancesForView(Series series, long seriesPk) throws Exception {
        List instances = listInstancesOfSeries(seriesPk);
        for (int i = 0; i < instances.size(); i++) {
            final Dataset ds = (Dataset) instances.get(i);
            SOPInstance sop = new SOPInstance(ds.getString(Tags.SOPInstanceUID));
            sop.setInstanceNumber(ds.getString(Tags.InstanceNumber));
            series.addSOPInstance(sop);
        }
    }

    private List listStudiesOfPatient2(long patPk) throws Exception {
        ContentManagerHome home =
            (ContentManagerHome) EJBHomeFactory.getFactory().lookup(ContentManagerHome.class,
                ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            return cm.listStudiesOfPatient(patPk);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
    }

    private List listSeriesOfStudy(long studyPk) throws Exception {
        ContentManagerHome home =
            (ContentManagerHome) EJBHomeFactory.getFactory().lookup(ContentManagerHome.class,
                ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            return cm.listSeriesOfStudy(studyPk);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
    }

    private List listInstancesOfSeries(long seriesPk) throws Exception {
        ContentManagerHome home =
            (ContentManagerHome) EJBHomeFactory.getFactory().lookup(ContentManagerHome.class,
                ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            return cm.listInstancesOfSeries(seriesPk);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
    }

}