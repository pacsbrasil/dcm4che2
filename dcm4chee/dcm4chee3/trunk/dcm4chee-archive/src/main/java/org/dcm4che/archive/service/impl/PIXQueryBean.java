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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4che.archive.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.service.PIXQueryLocal;
import org.dcm4che.archive.service.PIXQueryRemote;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public abstract class PIXQueryBean implements PIXQueryLocal, PIXQueryRemote {

    @EJB private PatientDAO patDAO;

    /** 
     * @see org.dcm4che.archive.service.PIXQuery#queryCorrespondingPIDs(java.lang.String, java.lang.String, java.lang.String[])
     */
    public List<String[]> queryCorrespondingPIDs(String patientID,
            String issuer, String[] domains) {
        Collection<Patient> c = isWildCard(patientID) ? patDAO
                .findCorrespondingLike(toLIKE(patientID), issuer) : patDAO
                .findCorresponding(patientID, issuer);
        return toPIDs(c, domains);
    }

    /** 
     * @see org.dcm4che.archive.service.PIXQuery#queryCorrespondingPIDsByPrimaryPatientID(java.lang.String, java.lang.String, java.lang.String[])
     */
    public List<String[]> queryCorrespondingPIDsByPrimaryPatientID(
            String patientID, String issuer, String[] domains) {
        Collection<Patient> c = isWildCard(patientID) ? patDAO
                .findCorrespondingByPrimaryPatientIDLike(toLIKE(patientID),
                        issuer) : patDAO.findCorrespondingByPrimaryPatientID(
                patientID, issuer);
        return toPIDs(c, domains);
    }

    /** 
     * @see org.dcm4che.archive.service.PIXQuery#queryCorrespondingPIDsByOtherPatientID(java.lang.String, java.lang.String, java.lang.String[])
     */
    public List<String[]> queryCorrespondingPIDsByOtherPatientID(String patientID,
            String issuer, String[] domains) {
        Collection<Patient> c = isWildCard(patientID) ? patDAO
                .findCorrespondingByOtherPatientIDLike(toLIKE(patientID),
                        issuer) : patDAO.findCorrespondingByOtherPatientID(
                patientID, issuer);
        return toPIDs(c, domains);
    }

    private List<String[]> toPIDs(Collection pats, String[] domains) {
        List<String[]> l = new ArrayList<String[]>(pats.size());
        List<String> domainList = domains != null ? Arrays.asList(domains)
                : null;
        for (Iterator iter = pats.iterator(); iter.hasNext();) {
            Patient pat = (Patient) iter.next();
            String iss = pat.getIssuerOfPatientId();
            if (domainList == null || domainList.contains(iss)) {
                l.add(new String[] { pat.getPatientId(), iss });
            }
        }
        return l;
    }

    private String toLIKE(String patientID) {
        StringBuilder sb = new StringBuilder(patientID);
        for (int i = 0; i < sb.length(); i++) {
            switch (sb.charAt(i)) {
            case '?':
                sb.setCharAt(i, '_');
                break;
            case '*':
                sb.setCharAt(i, '%');
                break;
            case '\\':
            case '_':
            case '%':
                sb.insert(i++, '\\');
                break;
            }
        }
        return sb.toString();
    }

    private boolean isWildCard(String s) {
        return s.indexOf('*') != -1 || s.indexOf('?') != -1;
    }

    /** 
     * @see org.dcm4che.archive.service.PIXQuery#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.PIXQuery#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

}
