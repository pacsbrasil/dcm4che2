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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.dcm4chee.archive.entity.OtherPatientID;
import org.jboss.annotation.ejb.LocalBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session bean facade for the OtherPatientID entity.
 * 
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@LocalBinding(jndiBinding = OtherPatientIDDAO.JNDI_NAME)
public class OtherPatientIDDAOBean implements OtherPatientIDDAO {
    private static Logger log = LoggerFactory
            .getLogger(OtherPatientIDDAOBean.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * @see org.dcm4chee.archive.dao.OtherPatientIDDAO#findByPatientIdAndIssuer(java.lang.String,
     *      java.lang.String)
     */
    public OtherPatientID findByPatientIdAndIssuer(String pid, String issuer) {
        log.debug("Looking up OtherPatientID by pid {} and issuer {}", pid,
                issuer);

        return (OtherPatientID) em.createNamedQuery(
                "OtherPatientID.findByPatientIdAndIssuer").getSingleResult();
    }

    /**
     * @see org.dcm4chee.archive.dao.OtherPatientIDDAO#findOrCreate(java.lang.String,
     *      java.lang.String)
     */
    public OtherPatientID findOrCreate(String pid, String issuer) {
        OtherPatientID opid = null;
        try {
            opid = findByPatientIdAndIssuer(pid, issuer);
        } catch (NoResultException nre) {
            opid = null;
        }

        if (opid == null) {
            opid = create(pid, issuer);
        }

        return opid;
    }

    /**
     * @see org.dcm4chee.archive.dao.OtherPatientIDDAO#create(java.lang.String,
     *      java.lang.String)
     */
    public OtherPatientID create(String pid, String issuer) {
        OtherPatientID opid = new OtherPatientID();
        opid.setPatientID(pid);
        opid.setIssuerOfPatientID(issuer);
        em.persist(opid);
        return opid;
    }

    /**
     * @see org.dcm4chee.archive.dao.OtherPatientIDDAO#remove(org.dcm4chee.archive.entity.OtherPatientID)
     */
    public void remove(OtherPatientID otherPatientId) {
        log.debug("Deleting OtherPatientID: {}", otherPatientId);

        em.remove(otherPatientId);
    }

}
