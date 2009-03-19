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
 * CHE Healthcare Solutions, LLC.
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
package org.dcm4chee.archive.dao;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.entity.OtherPatientID;
import org.dcm4chee.archive.entity.Patient;
import org.jboss.annotation.ejb.LocalBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session bean facade for the Patient entity.
 * 
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@LocalBinding(jndiBinding = PatientDAO.JNDI_NAME)
public class PatientDAOBean implements PatientDAO {
    private static Logger log = LoggerFactory.getLogger(PatientDAOBean.class);

    @EJB
    private OtherPatientIDDAO opidDAO;

    /**
     * @see org.dcm4chee.archive.dao.PatientDAO#updateOtherPatientIDs(org.dcm4chee.archive.entity.Patient,
     *      org.dcm4che2.data.DicomObject)
     */
    public boolean updateOtherPatientIDs(Patient p, DicomObject dcm) {
        DicomObject attrs = p.getAttributes();
        DicomElement opidsq = attrs.remove(Tag.OtherPatientIDsSequence);
        DicomElement nopidsq = dcm.get(Tag.OtherPatientIDsSequence);
        boolean update = false;
        if (opidsq != null) {
            for (int i = 0, n = opidsq.countItems(); i < n; i++) {
                DicomObject opid = opidsq.getDicomObject(i);
                String pid = opid.getString(Tag.PatientID);
                String issuer = opid.getString(Tag.IssuerOfPatientID);
                if (nopidsq == null || !containsPID(pid, issuer, nopidsq)) {
                    OtherPatientID otherPatientId = opidDAO
                            .findByPatientIdAndIssuer(pid, issuer);
                    p.getOtherPatientIDs().remove(otherPatientId);
                    if (otherPatientId.getPatients().isEmpty()) {
                        opidDAO.remove(otherPatientId);
                    }
                    update = true;
                    log.info("Remove Other Patient ID: {}^^^{} from " + prompt(p), pid, issuer);
                }
            }
        }
        if (nopidsq != null) {
            for (int i = 0, n = nopidsq.countItems(); i < n; i++) {
                DicomObject nopid = nopidsq.getDicomObject(i);
                String pid = nopid.getString(Tag.PatientID);
                String issuer = nopid.getString(Tag.IssuerOfPatientID);
                if (opidsq == null || !containsPID(pid, issuer, opidsq)) {
                    p.getOtherPatientIDs().add(
                            opidDAO.findOrCreate(pid, issuer));
                    update = true;
                    log.info("Add additional Other Patient ID: {}^^^{} to "
                            + prompt(p), pid, issuer);
                }
            }
            if (update) {
                int numItems = nopidsq.countItems();
                opidsq = attrs.putSequence(Tag.OtherPatientIDsSequence,
                        numItems);
                for (int i = 0, n = numItems; i < n; i++) {
                    opidsq.addDicomObject(nopidsq.getDicomObject(i));
                }
            }
        }
        if (update) {
            p.setAttributes(attrs);
        }
        return update;
    }

    private boolean containsPID(String pid, String issuer, DicomElement opidsq) {
        for (int i = 0, n = opidsq.countItems(); i < n; i++) {
            DicomObject opid = opidsq.getDicomObject(i);
            if (opid.getString(Tag.PatientID).equals(pid)
                    && opid.getString(Tag.IssuerOfPatientID).equals(issuer)) {
                return true;
            }
        }
        return false;
    }

    private String prompt(Patient p) {
        return "Patient[pk=" + p.getPk() + ", pid=" + p.getPatientID()
                + ", name=" + p.getPatientName() + "]";
    }
}
