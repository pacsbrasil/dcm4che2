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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.dao.CodeDAO;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.VerifyingObserverDAO;
import org.dcm4che.archive.entity.Code;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Media;
import org.dcm4che.archive.entity.MediaDTO;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.VerifyingObserver;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;

/**
 * org.dcm4che.archive.dao.jpa.InstanceDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class InstanceDAOImpl extends BaseDAOImpl<Instance> implements
        InstanceDAO {

    private static final String FIND_BY_STUDY_AND_SR_CODE = "select instance from Instance as instance where instance.series.study.studyIuid = :studyUid "
            + "AND instance.sopCuid = :cuid AND instance.srCode.codeValue = :code AND instance.srCode.codingSchemeDesignator = :designator";

    @EJB
    private CodeDAO codeDAO;

    @EJB
    private VerifyingObserverDAO observerDAO;

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return Instance.class;
    }

    /**
     * @see org.dcm4che.archive.dao.InstanceDAO#create(org.dcm4che.data.Dataset,
     *      org.dcm4che.archive.entity.Series)
     */
    public Instance create(Dataset ds, Series series)
            throws ContentCreateException {
        Instance inst = new Instance(ds, series);
        inst.setSrCode(Code.valueOf(codeDAO, ds
                .getItem(Tags.ConceptNameCodeSeq)));

        DcmElement sq = ds.get(Tags.VerifyingObserverSeq);
        if (sq != null) {
            Set<VerifyingObserver> obs = new HashSet<VerifyingObserver>();
            for (int i = 0, n = sq.countItems(); i < n; i++) {
                obs.add(observerDAO.create(sq.getItem(i)));
            }
        }

        save(inst);
        logger.info("Created " + inst.toString());
        return inst;
    }

    /**
     * @see org.dcm4che.archive.dao.InstanceDAO#findBySeriesPk(java.lang.Long)
     */
    public List<Instance> findBySeriesPk(Long seriesPk)
            throws PersistenceException {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for instances with series pk=" + seriesPk);
        }

        List<Instance> instances = null;

        Query query = em
                .createQuery("select instance from Instance as instance where instance.series.pk=:fk");
        query.setParameter("fk", seriesPk);
        instances = query.getResultList();

        return instances;
    }

    /**
     * @see org.dcm4che.archive.dao.InstanceDAO#findBySopIuid(java.lang.String)
     */
    public Instance findBySopIuid(String uid) throws NoResultException,
            PersistenceException {
        Query query = em
                .createQuery("select i from Instance as i where i.sopIuid =:sopiuid");
        query.setParameter("sopiuid", uid);
        Instance instance = (Instance) query.getSingleResult();
        if (instance == null) {
            throw new NoResultException("Instance with iuid=" + uid);
        }

        return instance;
    }

    /**
     * @see org.dcm4che.archive.dao.InstanceDAO#findByStudyAndSrCode(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public List<Instance> findByStudyAndSrCode(String suid, String cuid,
            String code, String designator) throws PersistenceException {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for instances for study iuid=" + suid
                    + " and SR code=" + code);
        }

        List<Instance> instances = null;

        Query query = em.createQuery(FIND_BY_STUDY_AND_SR_CODE);
        query.setParameter("studyUid", suid);
        query.setParameter("cuid", cuid);
        query.setParameter("code", code);
        query.setParameter("designator", designator);
        instances = query.getResultList();

        return instances;
    }

    /**
     * @see org.dcm4che.archive.dao.InstanceDAO#listByIUIDs(java.lang.String[])
     */
    public List<Instance> listByIUIDs(String[] iuids)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching for instances matching uid list");
        }

        List<Instance> instances = null;

        StringBuilder strQuery = new StringBuilder(
                "from Instance i where sopIuid ");
        addIN(strQuery, iuids);
        Query query = em.createQuery(strQuery.toString());

        instances = query.getResultList();

        return instances;
    }

    /**
     * @see org.dcm4che.archive.dao.InstanceDAO#updateDerivedFields(org.dcm4che.archive.entity.Instance,
     *      boolean, boolean)
     */
    public boolean updateDerivedFields(Instance instance, boolean retrieveAETs,
            boolean availability) {
        boolean updated = false;
        if (retrieveAETs)
            if (updateRetrieveAETs(instance))
                updated = true;
        if (availability)
            if (updateAvailability(instance, instance.getRetrieveAETs()))
                updated = true;
        return updated;
    }

    private boolean updateAvailability(Instance instance, String retrieveAETs) {
        int availability = Availability.UNAVAILABLE;
        Media media;
        if (retrieveAETs != null)
            availability = selectLocalAvailability(instance.getPk());
        else if (instance.getExternalRetrieveAET() != null)
            availability = Availability.NEARLINE;
        else if ((media = instance.getMedia()) != null
                && media.getMediaStatus() == MediaDTO.COMPLETED)
            availability = Availability.OFFLINE;
        boolean updated = (availability != instance.getAvailability());
        if (updated) {
            instance.setAvailability(availability);
        }
        return updated;
    }

    private int selectLocalAvailability(Long pk) {
        Number n = (Number) em
                .createQuery(
                        "select min(f.fileSystem.availability) from Instance i join i.files f where i.pk = :pk")
                .setParameter("pk", pk).getSingleResult();
        return n == null ? Availability.UNAVAILABLE : n.intValue();
    }

    private boolean updateRetrieveAETs(Instance instance)
            throws PersistenceException {
        final Set aetSet = selectRetrieveAETs(instance.getPk());
        if (aetSet.remove(null))
            logger.warn("Instance[iuid=" + instance.getSopIuid()
                    + "] reference File(s) with unspecified Retrieve AET");
        final String aets = asString(aetSet);
        boolean updated = (aets == null ? instance.getRetrieveAETs() != null
                : !aets.equals(instance.getRetrieveAETs()));
        if (updated)
            instance.setRetrieveAETs(aets);
        return updated;
    }

    private Set<String> selectRetrieveAETs(Long pk) {
        String jpaql = "select distinct f.fileSystem.retrieveAET from Instance i join i.files f where i.pk = :pk";
        Query q = em.createQuery(jpaql);
        q.setParameter("pk", pk);
        List results = q.getResultList();
        return new HashSet<String>(results);
    }

    private static String asString(Set s) {
        if (s.isEmpty())
            return null;
        String[] a = (String[]) s.toArray(new String[s.size()]);
        return StringUtils.toString(a, '\\');
    }

    /**
     * @see org.dcm4che.archive.dao.InstanceDAO#listByPatientAndSRCode(org.dcm4che.archive.entity.Patient,
     *      java.util.List, java.util.Collection)
     */
    public List<Instance> listByPatientAndSRCode(Patient pat,
            List<String> srCodes, Collection<String> cuids)
            throws PersistenceException {
        return null;
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
     * @return the observerDAO
     */
    public VerifyingObserverDAO getObserverDAO() {
        return observerDAO;
    }

    /**
     * @param observerDAO
     *            the observerDAO to set
     */
    public void setObserverDAO(VerifyingObserverDAO observerDAO) {
        this.observerDAO = observerDAO;
    }

    private void addIN(StringBuilder query, String[] elements) {
        if (elements.length > 1) {
            query.append(" IN (");
            for (int i = 0; i < elements.length; i++) {
                if (i > 0)
                    query.append(",");
                query.append("'").append(elements[i]).append("'");
            }
            query.append(")");
        }
        else {
            query.append(" = '").append(elements[0]).append("'");
        }
    }
}
