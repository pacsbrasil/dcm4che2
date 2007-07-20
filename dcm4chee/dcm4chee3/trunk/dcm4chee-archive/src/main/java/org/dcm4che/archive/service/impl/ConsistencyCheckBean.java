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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.service.ConsistencyCheck;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1.1 $ $Date: 2007/06/23 18:59:01 $
 * @since 25.03.2005
 */
@Transactional(propagation = Propagation.REQUIRED)
public class ConsistencyCheckBean implements ConsistencyCheck {

    private StudyDAO studyDAO;

    private SeriesDAO seriesDAO;

    private InstanceDAO instanceDAO;

    private static final Logger log = Logger
            .getLogger(ConsistencyCheckBean.class);

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#findStudiesToCheck(java.sql.Timestamp, java.sql.Timestamp, java.sql.Timestamp, int)
     */
    public long[] findStudiesToCheck(Timestamp createdAfter,
            Timestamp createdBefore, Timestamp checkedBefore, int limit)
            throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug("findStudiesToCheck: created between " + createdAfter
                    + " - " + createdBefore + " checkedBefore" + checkedBefore
                    + " limit:" + limit);
        Collection c = studyDAO.findStudyToCheck(createdAfter, createdBefore,
                checkedBefore, limit);
        if (c.size() < 1)
            return new long[0];
        Iterator iter = c.iterator();
        long[] ia = new long[c.size()];
        int i = 0;
        while (iter.hasNext()) {
            ia[i++] = ((Study) iter.next()).getPk().longValue();
        }
        return ia;
    }

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#updateStudy(long)
     */
    public boolean updateStudy(long study_pk) {
        boolean updated = false;
        try {
            Study study = studyDAO.findByPrimaryKey(new Long(study_pk));
            Collection col = study.getSeries();
            Iterator iter = col.iterator();
            Series series;
            Collection instances;
            Instance instance;
            while (iter.hasNext()) {
                series = (Series) iter.next();
                instances = series.getInstances();
                Iterator iter1 = instances.iterator();
                while (iter1.hasNext()) {
                    instance = (Instance) iter1.next();
                    if (instanceDAO.updateDerivedFields(instance, true, true)) {
                        log.info("Instance " + instance.getSopIuid()
                                + " updated!");
                        updated = true;
                    }
                }
                if (seriesDAO.updateDerivedFields(series, true, true, true,
                        true, true)) {
                    log.info("Series " + series.getSeriesIuid() + " updated!");
                    updated = true;
                }
            }
            if (studyDAO.updateDerivedFields(study, true, true, true, true,
                    true, true)) {
                log.info("Study " + study.getStudyIuid() + " updated!");
                updated = true;
            }
            study.setTimeOfLastConsistencyCheck(new Timestamp(System
                    .currentTimeMillis()));
            return updated;
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#getInstanceDAO()
     */
    public InstanceDAO getInstanceDAO() {
        return instanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#setInstanceDAO(org.dcm4che.archive.dao.InstanceDAO)
     */
    public void setInstanceDAO(InstanceDAO instanceDAO) {
        this.instanceDAO = instanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ConsistencyCheck#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

}