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
 * Justin Falk <jfalkmu@gmail.com>
 * Jeremy Vosters <jlvosters@gmail.com>
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

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che.archive.common.DatasetUtils;
import org.dcm4che.archive.dao.CodeDAO;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.GPSPSDAO;
import org.dcm4che.archive.dao.GPSPSPerformerDAO;
import org.dcm4che.archive.dao.GPSPSRequestDAO;
import org.dcm4che.archive.dao.helper.QueryBuilder;
import org.dcm4che.archive.dao.helper.QueryUtils;
import org.dcm4che.archive.entity.Code;
import org.dcm4che.archive.entity.EntityBase;
import org.dcm4che.archive.entity.GPSPS;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class GPSPSDAOImpl extends BaseDAOImpl<GPSPS> implements GPSPSDAO {
    // TODO: JNDI Resource
    private String spsIdPrefix;

    @EJB private CodeDAO codeDAO;

    @EJB private GPSPSRequestDAO rqDAO;

    @EJB private GPSPSPerformerDAO performerDAO;

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return GPSPS.class;
    }

    public GPSPS findBySopIuid(String iuid) throws NoResultException {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting to search for GPSPS by instance uid: "
                    + iuid);
        }

        GPSPS gpsps = (GPSPS) em.createQuery(
                "select gpsps from GPSPS as gpsps where gpsps.sopIuid=:uid")
                .setParameter("uid", iuid).getSingleResult();

        if (gpsps != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found GPSPS pk=" + gpsps.getPk());
            }
        }

        return gpsps;
    }
    
    /**
     * @see org.dcm4che.archive.dao.GPSPSDAO#create(org.dcm4che.data.Dataset,
     *      org.dcm4che.archive.entity.Patient)
     */
    public void create(Dataset ds, Patient patient)
            throws ContentCreateException {
        GPSPS gpsps = new GPSPS();
        gpsps.setAttributes(ds);
        gpsps.setPatient(patient);
        save(gpsps);
        if (ds.getString(Tags.SPSID) == null) {
            String id = spsIdPrefix + gpsps.getPk();
            ds.putSH(Tags.SPSID, id);
            gpsps.setEncodedAttributes(DatasetUtils.toByteArray(ds,
                    UIDs.DeflatedExplicitVRLittleEndian));
        }

        try {
            gpsps.setScheduledWorkItemCode(Code.valueOf(codeDAO, ds
                    .getItem(Tags.ScheduledWorkitemCodeSeq)));
            Code.addCodesTo(codeDAO, ds
                    .get(Tags.ScheduledProcessingApplicationsCodeSeq), gpsps
                    .getScheduledProcessingApplicationsCodes());
            Code.addCodesTo(codeDAO, ds.get(Tags.ScheduledStationNameCodeSeq),
                    gpsps.getScheduledStationNameCodes());
            Code.addCodesTo(codeDAO, ds.get(Tags.ScheduledStationClassCodeSeq),
                    gpsps.getScheduledStationClassCodes());
            Code.addCodesTo(codeDAO, ds
                    .get(Tags.ScheduledStationGeographicLocationCodeSeq), gpsps
                    .getScheduledStationGeographicLocationCodes());
            createScheduledHumanPerformers(gpsps, ds
                    .get(Tags.ScheduledHumanPerformersSeq));
            createRefRequests(gpsps, ds.get(Tags.RefRequestSeq));
        }
        catch (ContentCreateException e) {
            throw e;
        }
        catch (PersistenceException e) {
            throw new ContentCreateException(e);
        }

        logger.info("Created " + gpsps);
        if (logger.isDebugEnabled()) {
            logger.debug(ds);
        }
    }

    private void createScheduledHumanPerformers(GPSPS gpsps, DcmElement sq)
            throws ContentCreateException {
        if (sq == null)
            return;
        Collection c = gpsps.getScheduledHumanPerformers();
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            c.add(performerDAO.create(sq.getItem(i), gpsps));
        }
    }

    private void createRefRequests(GPSPS gpsps, DcmElement sq)
            throws ContentCreateException {
        if (sq == null)
            return;
        Collection c = gpsps.getRefRequests();
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            c.add(rqDAO.create(sq.getItem(i), gpsps));
        }
    }

    /**
     * @see org.dcm4che.archive.dao.GPSPSDAO#findByReqProcId(int,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public List<GPSPS> findByReqProcId(int scheduled, String codeValue,
            String codeDesignator, String rpid) throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting to search for GPSPS by rpid: " + rpid);
        }

        List<GPSPS> results = null;
        StringBuilder sb = new StringBuilder(
                "select gpsps from GPSPS as gpsps join gpsps.refRequests as rq")
                .append(" where gpsps.gpspsStatusAsInt = :scheduled")
                .append(" and scheduledWorkItemCode.codeValue=:codeValue")
                .append(
                        " and gpsps.scheduledWorkItemCode.codingSchemeDesignator = :codeDesignator")
                .append(" and rq.requestedProcedureId = :rpid");

        Query q = em.createQuery(sb.toString());
        q.setParameter("scheduled", scheduled);
        q.setParameter("codeValue", codeValue);
        q.setParameter("codeDesignator", codeDesignator);
        q.setParameter("rpid", rpid);

        results = q.getResultList();

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + results == null ? 0 : results.size()
                    + " results.");
        }

        return results;
    }

    /**
     * @see org.dcm4che.archive.dao.GPSPSDAO#getSpsIdPrefix()
     */
    public String getSpsIdPrefix() {
        return spsIdPrefix;
    }

    /**
     * @see org.dcm4che.archive.dao.GPSPSDAO#setSpsIdPrefix(java.lang.String)
     */
    public void setSpsIdPrefix(String prefix) {
        this.spsIdPrefix = prefix;
    }

    /**
     * @return the codeDAO
     */
    public CodeDAO getCodeDAO() {
        return codeDAO;
    }

    /**
     * @param codeDAO
     *            the codeDAO to set
     */
    public void setCodeDAO(CodeDAO codeDAO) {
        this.codeDAO = codeDAO;
    }

    /**
     * @param performerDAO
     *            the performerDAO to set
     */
    public void setPerformerDAO(GPSPSPerformerDAO performerDAO) {
        this.performerDAO = performerDAO;
    }

    /**
     * @param rqDAO
     *            the rqDAO to set
     */
    public void setRqDAO(GPSPSRequestDAO rqDAO) {
        this.rqDAO = rqDAO;
    }

    /**
     * @return the performerDAO
     */
    public GPSPSPerformerDAO getPerformerDAO() {
        return performerDAO;
    }

    /**
     * @return the rqDAO
     */
    public GPSPSRequestDAO getRqDAO() {
        return rqDAO;
    }
}