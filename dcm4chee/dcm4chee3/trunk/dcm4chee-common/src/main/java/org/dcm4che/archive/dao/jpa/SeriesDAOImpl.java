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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.common.PrivateTags;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.MPPSDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.SeriesRequestDAO;
import org.dcm4che.archive.entity.AttrUtils;
import org.dcm4che.archive.entity.MPPS;
import org.dcm4che.archive.entity.Media;
import org.dcm4che.archive.entity.MediaDTO;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.SeriesRequest;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.util.AttributeFilter;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * Data access object used for managing Series records in the database.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class SeriesDAOImpl extends BaseDAOImpl<Series> implements SeriesDAO {

    @EJB private MPPSDAO mppsDAO;

    @EJB private SeriesRequestDAO requestDAO;

    /**
     * 
     */
    public SeriesDAOImpl() {
    }

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return Series.class;
    }

    /**
     * @throws ContentCreateException
     * @see org.dcm4che.archive.dao.SeriesDAO#create(org.dcm4che.data.Dataset,
     *      org.dcm4che.archive.entity.Study)
     */
    public Series create(Dataset ds, Study study) throws ContentCreateException {
        Series series = new Series();
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        series.setSourceAET(ds.getString(PrivateTags.CallingAET));
        series.setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        updateAttributes(series, ds, false);
        save(series);
        series.setStudy(study);
        updateMpps(series);
        logger.info("Created " + series);
        return series;
    }

    public void updateAttributes(Series series, Dataset newAttrs,
            boolean overwriteReqAttrSQ) throws ContentCreateException {
        Dataset oldAttrs = series.getAttributes(false);
        updateSeriesRequest(series, oldAttrs, newAttrs, overwriteReqAttrSQ);
        if (oldAttrs == null) {
            series.setAttributes(newAttrs);
        }
        else {
            String cuid = newAttrs.getString(Tags.SOPClassUID);
            AttributeFilter filter = AttributeFilter
                    .getSeriesAttributeFilter(cuid);
            if (AttrUtils.mergeAttributes(oldAttrs, filter.filter(newAttrs),
                    logger)) {
                series.setAttributes(oldAttrs);
            }
        }
    }

    private boolean updateSeriesRequest(Series series, Dataset oldAttrs,
            Dataset newAttrs, boolean overwriteReqAttrSQ)
            throws ContentCreateException {
        DcmElement newReqAttrSQ = newAttrs.get(Tags.RequestAttributesSeq);
        if (newReqAttrSQ == null)
            return false;
        DcmElement oldReqAttrSQ = oldAttrs == null ? null : oldAttrs
                .get(Tags.RequestAttributesSeq);
        if (newReqAttrSQ.equals(oldReqAttrSQ)) {
            return false;
        }
        Collection c = series.getRequestAttributes();
        if (overwriteReqAttrSQ && !c.isEmpty()) {
            oldAttrs.remove(Tags.RequestAttributesSeq);// remove to force
            // update of
            // RequestAttributesSeq
            SeriesRequest[] srls = new SeriesRequest[c.size()];
            srls = (SeriesRequest[]) c.toArray(srls);
            for (int i = 0; i < srls.length; i++) {
                try {
                    requestDAO.remove(srls[i]);
                }
                catch (Exception ignore) {
                    logger.warn(
                            "Cant delete SeriesRequest! Ignore deletion of "
                                    + srls[i], ignore);
                }
            }
            c.clear();
        }

        for (int i = 0, len = newReqAttrSQ.countItems(); i < len; i++) {
            c.add(requestDAO.create(newReqAttrSQ.getItem(i), series));
        }
        return true;
    }

    private void updateMpps(Series series) throws PersistenceException {
        final String ppsiuid = series.getPpsIuid();
        MPPS mpps = null;
        if (ppsiuid != null) {
            try {
                mpps = mppsDAO.findBySopIuid(ppsiuid);
            }
            catch (NoResultException ignore) {
            }
        }
        series.setMpps(mpps);
    }

    /**
     * @see org.dcm4che.archive.dao.SeriesDAO#findBySeriesIuid(java.lang.String)
     */
    public Series findBySeriesIuid(String uid) throws NoResultException {

        return (Series) em
                .createQuery(
                        "select series from Series as series where series.seriesUID=:seriesUID")
                .setParameter("seriesUID", uid).getSingleResult();
    }

    /**
     * @see org.dcm4che.archive.dao.SeriesDAO#findByStatusReceivedBefore(int,
     *      java.sql.Timestamp)
     */
    @SuppressWarnings("unchecked")
    public List<Series> findByStatusReceivedBefore(int received,
            Timestamp before) {

        return em
                .createQuery(
                        "select s from Series as s where s.seriesStatus = :status and s.createdTime < :ct")
                .setParameter("status", received).setParameter("ct", before,
                        TemporalType.TIMESTAMP).getResultList();
    }

    public boolean updateDerivedFields(Series series, boolean numOfInstances,
            boolean retrieveAETs, boolean externalRettrieveAETs,
            boolean filesetId, boolean availability) {
        boolean updated = false;
        if (numOfInstances)
            if (updateNumberOfInstances(series))
                updated = true;
        final int numI = series.getNumberOfSeriesRelatedInstances();
        if (retrieveAETs)
            if (updateRetrieveAETs(series, numI))
                updated = true;
        if (externalRettrieveAETs)
            if (updateExternalRetrieveAET(series, numI))
                updated = true;
        if (filesetId)
            if (updateFilesetId(series, numI))
                updated = true;
        if (availability)
            if (updateAvailability(series, numI))
                updated = true;
        return updated;
    }

    /**
     * @param series
     * @param numI
     * @return
     */
    private boolean updateRetrieveAETs(Series series, int numI) {
        String aets = null;
        if (numI > 0) {
            Long pk = series.getPk();
            StringBuffer sb = new StringBuffer();
            Set iAetSet = selectInternalRetrieveAETs(pk);
            if (iAetSet.remove(null))
                logger
                        .warn("Series[iuid="
                                + series.getSeriesIuid()
                                + "] contains Instance(s) with unspecified Retrieve AET");
            for (Iterator it = iAetSet.iterator(); it.hasNext();) {
                final String aet = (String) it.next();
                if (selectNumberOfSeriesRelatedInstancesWithInternalRetrieveAET(
                        pk, aet) == numI)
                    sb.append(aet).append('\\');
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
                aets = sb.toString();
            }
        }

        boolean updated = (aets == null ? series.getRetrieveAETs() != null
                : !aets.equals(series.getRetrieveAETs()));
        if (updated) {
            series.setRetrieveAETs(aets);
        }
        return updated;
    }

    /**
     * @param pk
     * @param aet
     * @return
     */
    private int selectNumberOfSeriesRelatedInstancesWithInternalRetrieveAET(
            Long pk, String aet) {
        // TODO
        // SELECT COUNT(DISTINCT i) FROM Series s, IN(s.instances) i,
        // IN(i.files) f WHERE s.pk = ?1 AND f.fileSystem.retrieveAET = ?2
        return 0;
    }

    /**
     * @param pk
     * @return
     */
    private Set selectInternalRetrieveAETs(Long pk) {
        // TODO
        // SELECT DISTINCT i.externalRetrieveAET FROM Series s, IN(s.instances)
        // i WHERE s.pk = ?1
        return null;
    }

    /**
     * @param series
     * @return
     */
    private boolean updateExternalRetrieveAET(Series series, int numI) {
        String aet = null;
        if (numI > 0) {
            Set eAetSet = selectExternalRetrieveAETs(series.getPk());
            if (eAetSet.size() == 1)
                aet = (String) eAetSet.iterator().next();
        }

        boolean updated = (aet == null ? series.getExternalRetrieveAET() != null
                : !aet.equals(series.getExternalRetrieveAET()));
        if (updated) {
            series.setExternalRetrieveAET(aet);
        }
        return updated;
    }

    /**
     * @param pk
     * @return
     */
    private Set selectExternalRetrieveAETs(Long pk) {
        // TODO
        // SELECT DISTINCT i.externalRetrieveAET FROM Series s, IN(s.instances)
        // i WHERE s.pk = ?1
        return null;
    }

    /**
     * @param series
     * @param numI
     * @return
     */
    private boolean updateFilesetId(Series series, int numI) {
        boolean updated = false;
        String fileSetId = null;
        String fileSetIuid = null;
        if (numI > 0) {
            Long pk = series.getPk();
            if (selectNumberOfSeriesRelatedInstancesOnMediaWithStatus(pk,
                    MediaDTO.COMPLETED) == numI) {
                Set<Media> c = selectMediaWithStatus(pk, MediaDTO.COMPLETED);
                if (c.size() == 1) {
                    Media media = c.iterator().next();
                    fileSetId = media.getFilesetId();
                    fileSetIuid = media.getFilesetIuid();
                }
            }
        }
        if (fileSetId == null ? series.getFilesetId() != null : !fileSetId
                .equals(series.getFilesetId())) {
            series.setFilesetId(fileSetId);
            updated = true;
        }
        if (fileSetIuid == null ? series.getFilesetIuid() != null
                : !fileSetIuid.equals(series.getFilesetIuid())) {
            series.setFilesetIuid(fileSetIuid);
            updated = true;
        }
        return updated;
    }

    /**
     * @param pk
     * @param completed
     * @return
     */
    private int selectNumberOfSeriesRelatedInstancesOnMediaWithStatus(Long pk,
            int completed) {
        // TODO
        // SELECT COUNT(i) FROM Instance i WHERE i.series.pk = ?1 AND
        // i.media.mediaStatus = ?2
        return 0;
    }

    /**
     * @param pk
     * @param completed
     * @return
     */
    private Set<Media> selectMediaWithStatus(Long pk, int completed) {
        // TODO:
        // SELECT DISTINCT i.media FROM Series s, IN(s.instances) i WHERE s.pk =
        // ?1 AND i.media.mediaStatus = ?2
        return null;
    }

    private boolean updateNumberOfInstances(Series series) {
        boolean updated = false;
        final int numI = selectNumberOfSeriesRelatedInstances(series.getPk());
        if (series.getNumberOfSeriesRelatedInstances() != numI) {
            series.setNumberOfSeriesRelatedInstances(numI);
            updated = true;
        }
        return updated;
    }

    private int selectNumberOfSeriesRelatedInstances(Long seriesPk) {

        Number n = (Number) em.createQuery(
                "select count(i) from Instance i where i.series.pk = :pk")
                .setParameter("pk", seriesPk).getSingleResult();
        return n == null ? 0 : n.intValue();
    }

    private boolean updateAvailability(Series series, int numI) {
        int availability = numI > 0 ? selectAvailability(series.getPk())
                : Availability.UNAVAILABLE;
        boolean updated = availability != series.getAvailability();
        if (updated) {
            series.setAvailability(availability);
        }
        return updated;
    }

    private int selectAvailability(Long seriesPk) {
        Number n = (Number) em
                .createQuery(
                        "select max(i.availability) from Instance i where i.series.pk = :pk")
                .setParameter("pk", seriesPk).getSingleResult();
        return n == null ? Availability.UNAVAILABLE : n.intValue();
    }

    /**
     * @return the mppsDAO
     */
    public MPPSDAO getMppsDAO() {
        return mppsDAO;
    }

    /**
     * @param mppsDAO
     *            the mppsDAO to set
     */
    public void setMppsDAO(MPPSDAO mppsDAO) {
        this.mppsDAO = mppsDAO;
    }

    /**
     * @see org.dcm4che.archive.dao.SeriesDAO#findByPpsIuid(java.lang.String)
     */
    public Set<Series> findByPpsIuid(String mppsIUID)
            throws PersistenceException {
        return null;
    }

    /**
     * @return the requestDAO
     */
    public SeriesRequestDAO getRequestDAO() {
        return requestDAO;
    }

    /**
     * @param requestDAO
     *            the requestDAO to set
     */
    public void setRequestDAO(SeriesRequestDAO requestDAO) {
        this.requestDAO = requestDAO;
    }

    /**
     * @see org.dcm4che.archive.dao.SeriesDAO#findWithNoPpsIuidFromSrcAETReceivedBefore(java.lang.String,
     *      java.sql.Timestamp)
     */
    public List<Series> findWithNoPpsIuidFromSrcAETReceivedBefore(
            String sourceAET, Timestamp timestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting to find series from src " + sourceAET
                    + " with no pps uid");
        }

        Query query = em
                .createQuery("from org.dcm4che.archive.entity.Series as s where s.sourceAET=:aet and s.createdTime < :ts");
        query.setParameter("aet", sourceAET);
        query.setParameter("ts", timestamp, TemporalType.TIMESTAMP);

        List<Series> seriess = query.getResultList();

        if (seriess == null) {
            if (logger.isInfoEnabled()) {
                logger.info("Could not find series from src " + sourceAET
                        + " with no pps uid");
            }
        }
        else {
            if (logger.isDebugEnabled())
                logger.debug("Found series results.");
        }

        return seriess;
    }

    /**
     * @see org.dcm4che.archive.dao.SeriesDAO#findSeriesOnMedia(org.dcm4che.archive.entity.Media)
     */
    public Collection<Series> findSeriesOnMedia(Media media)
            throws PersistenceException {
        return null;
    }
}
