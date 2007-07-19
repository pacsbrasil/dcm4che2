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

package org.dcm4che.archive.dao.jpa;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.PrivatePatientDAO;
import org.dcm4che.archive.entity.PrivatePatient;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.dao.jpa.PrivatePatientDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class PrivatePatientDAOImpl extends BaseDAOImpl<PrivatePatient>
        implements PrivatePatientDAO {

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return PrivatePatient.class;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivatePatientDAO#create(int,
     *      org.dcm4che.data.Dataset)
     */
    public PrivatePatient create(int type, Dataset ds)
            throws ContentCreateException {
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#remove(org.dcm4che.archive.entity.EntityBase)
     */
    public void remove(PrivatePatient obj) throws ContentDeleteException {
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#save(org.dcm4che.archive.entity.EntityBase)
     */
    public void save(PrivatePatient obj) throws ContentCreateException {
    }

    /**
     * @see org.dcm4che.archive.dao.PrivatePatientDAO#findByPatientId(int,
     *      java.lang.String)
     */
    public Collection<PrivatePatient> findByPatientId(int type, String patientId)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching for PrivatePatients with patient id="
                    + patientId);
        }

        Collection<PrivatePatient> patients = null;

        Query query = em
                .createQuery("select pp from PrivatePatient as pp where pp.privateType=:type and pp.patientId=:pid");
        query.setParameter("type", type);
        query.setParameter("pid", patientId);
        patients = query.getResultList();

        return patients;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivatePatientDAO#findByPatientIdWithIssuer(int,
     *      java.lang.String, java.lang.String)
     */
    public Collection<PrivatePatient> findByPatientIdWithIssuer(int type,
            String patientId, String issuerOfPatientId)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching for PrivatePatients with patient id="
                    + patientId + " and issuer=" + issuerOfPatientId);
        }

        Collection<PrivatePatient> patients = null;

        Query query = em
                .createQuery("select pp from PrivatePatient as pp where pp.privateType=:type and pp.patientId=:pid and (a.issuerOfPatientId is null or pp.issuerOfPatientId = :issuer)");
        query.setParameter("type", type);
        query.setParameter("pid", patientId);
        query.setParameter("issuer", issuerOfPatientId);
        patients = query.getResultList();

        return patients;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivatePatientDAO#findByPrivateType(int)
     */
    public Collection<PrivatePatient> findByPrivateType(int privateType)
            throws PersistenceException {

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Searching for PrivatePatient entities with privateType="
                            + privateType);
        }

        Collection<PrivatePatient> patients = null;

        Query query = em
                .createQuery("select pp from PrivatePatient as pp where pp.privateType=:type");
        query.setParameter("type", privateType);
        patients = query.getResultList();

        return patients;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivatePatientDAO#remove(java.lang.Long)
     */
    public void remove(Long long1) throws ContentDeleteException {
    }

}
