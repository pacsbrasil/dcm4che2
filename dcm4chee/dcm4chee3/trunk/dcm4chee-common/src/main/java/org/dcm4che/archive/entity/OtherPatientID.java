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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che.archive.entity;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.OtherPatientID
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "other_pid")
public class OtherPatientID extends EntityBase {
    private static final long serialVersionUID = -9032324407711990564L;

    @Column(name = "pat_id", nullable = false)
    private String patientId;

    @Column(name = "pat_id_issuer", nullable = false)
    private String issuerOfPatientId;

    @ManyToMany
    @JoinTable(name = "rel_pat_other_pid", joinColumns = @JoinColumn(name = "other_pid_fk", referencedColumnName = "pk"), inverseJoinColumns = @JoinColumn(name = "patient_fk", referencedColumnName = "pk"))
    private Collection<Patient> patients;

    /**
     * 
     */
    public OtherPatientID() {
    }

    /**
     * @return the issuerOfPatientId
     */
    public String getIssuerOfPatientId() {
        return issuerOfPatientId;
    }

    /**
     * @param issuerOfPatientId
     *            the issuerOfPatientId to set
     */
    public void setIssuerOfPatientId(String issuerOfPatientId) {
        this.issuerOfPatientId = issuerOfPatientId;
    }

    /**
     * @return the patientId
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * @param patientId
     *            the patientId to set
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "OtherPatientID(id=" + getPatientId() + ", issuer="
                + getIssuerOfPatientId() + ")";
    }

    /**
     * @return the patients
     */
    public Collection<Patient> getPatients() {
        return patients;
    }

    /**
     * @param patients
     *            the patients to set
     */
    public void setPatients(Collection<Patient> patients) {
        this.patients = patients;
    }

}
