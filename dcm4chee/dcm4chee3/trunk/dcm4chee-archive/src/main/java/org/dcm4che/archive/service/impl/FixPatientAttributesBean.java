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
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che.archive.service.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.service.FixPatientAttributesLocal;
import org.dcm4che.archive.service.FixPatientAttributesRemote;
import org.dcm4che.archive.util.AttributeFilter;
import org.dcm4che.data.Dataset;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * FixPatientAttributes Bean
 * 
 * @author <a href="mailto:franz.willer@gwi-ag.com">Franz Willer </a>
 * @version $Revision: 1.1 $ $Date: 2007/06/23 18:59:01 $
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class FixPatientAttributesBean implements FixPatientAttributesLocal, FixPatientAttributesRemote {

    private static Logger log = Logger
            .getLogger(FixPatientAttributesBean.class);

    @EJB private PatientDAO patHome;

    /** 
     * @see org.dcm4che.archive.service.FixPatientAttributes#checkPatientAttributes(int, int, boolean)
     */
    public int[] checkPatientAttributes(int offset, int limit, boolean doUpdate)
            throws PersistenceException {
        Collection<Patient> col = patHome.findAll(offset, limit);
        if (col.isEmpty())
            return null;
        Patient patient;
        Dataset patAttrs, filtered;
        int[] result = { 0, 0 };
        AttributeFilter filter = AttributeFilter
                .getPatientAttributeFilter(null);
        for (Iterator iter = col.iterator(); iter.hasNext(); result[1]++) {
            patient = (Patient) iter.next();
            patAttrs = patient.getAttributes(false);
            filtered = filter.filter(patAttrs);
            if (patAttrs.size() > filtered.size()) {
                log.warn("Detect Patient Record [pk= " + patient.getPk()
                        + "] with non-patient attributes:");
                log.warn(patAttrs);
                if (doUpdate) {
                    patient.setAttributes(filtered);
                    log
                            .warn("Remove non-patient attributes from Patient Record [pk= "
                                    + patient.getPk() + "]");
                }
                result[0]++;
            }
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.FixPatientAttributes#getPatHome()
     */
    public PatientDAO getPatHome() {
        return patHome;
    }

    /** 
     * @see org.dcm4che.archive.service.FixPatientAttributes#setPatHome(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatHome(PatientDAO patHome) {
        this.patHome = patHome;
    }

}