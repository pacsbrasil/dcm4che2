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
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.StudyListLocal;
import org.dcm4chee.web.wicket.util.DateUtils;
import org.dcm4chee.web.wicket.util.JNDIUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 12, 2008
 */
public class PatientModel implements Serializable {

    private long pk;
    private boolean selected;
    private boolean details;
    private DicomObject dataset;
    private List<StudyModel> studies = new ArrayList<StudyModel>();

    public PatientModel(Patient patient) throws IOException {
        this.pk = patient.getPk();
        this.dataset = patient.getAttributes();
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

    public String getName() {
        return dataset.getString(Tag.PatientName);
    }

    public void setName(String name) {
        dataset.putString(Tag.PatientName, VR.PN, name);
    }

    public String getId() {
        return dataset.getString(Tag.PatientID);
    }

    public void setId(String id) {
        dataset.putString(Tag.PatientID, VR.LO, id);
    }

    public String getIssuer() {
        return dataset.getString(Tag.IssuerOfPatientID);
    }

    public void setIssuer(String issuer) {
        dataset.putString(Tag.IssuerOfPatientID, VR.LO, issuer);
    }

    public String getSex() {
        return dataset.getString(Tag.PatientSex, "");
    }

    public void setSex(String sex) {
        dataset.putString(Tag.PatientSex, VR.CS, sex);
    }

    public String getBirthdate() {
        return DateUtils.da2str(dataset.getString(Tag.PatientBirthDate));
    }

    public void setBirthdate(String birthdate) {
        dataset.putString(Tag.PatientBirthDate, VR.DA,
                DateUtils.str2da(birthdate));
    }

    public List<StudyModel> getStudies() {
        return studies;
    }

    public int getRowspan() {
        int rowspan = details ? 2 : 1;
        for (StudyModel study : studies) {
            rowspan += study.getRowspan();
        }
        return rowspan;
    }

    public void collapse() {
        studies.clear();
    }

    public boolean isCollapsed() {
        return studies.isEmpty();
    }

    public void retainSelectedStudies() {
        for (Iterator<StudyModel> it = studies.iterator(); it.hasNext();) {
            StudyModel study = it.next();
            study.retainSelectedPPSs();
            if (study.isCollapsed() && !study.isSelected()) {
                it.remove();
            }
        }
    }

    public void expand(boolean latestStudyFirst) throws Exception {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        for (Study study : dao.findStudiesOfPatient(pk, latestStudyFirst)) {
            this.studies.add(new StudyModel(study));
        }
    }

    public void update(DicomObject dicomObject) {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        try {
            dataset = dao.updatePatient(pk, dicomObject).getAttributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
