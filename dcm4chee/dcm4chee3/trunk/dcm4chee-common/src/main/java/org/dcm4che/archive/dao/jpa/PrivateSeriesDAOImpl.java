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
import org.dcm4che.archive.dao.PrivateSeriesDAO;
import org.dcm4che.archive.entity.PrivateSeries;
import org.dcm4che.archive.entity.PrivateStudy;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.dao.jpa.PrivateSeriesDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class PrivateSeriesDAOImpl extends BaseDAOImpl<PrivateSeries> implements
        PrivateSeriesDAO {

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return PrivateSeries.class;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivateSeriesDAO#create(int,
     *      org.dcm4che.data.Dataset, org.dcm4che.archive.entity.PrivateStudy)
     */
    public PrivateSeries create(int type, Dataset ds, PrivateStudy study)
            throws ContentCreateException {
        
        PrivateSeries series = new PrivateSeries();
        series.setPrivateType(type);
        series.setAttributes(ds);
        series.setStudy(study);
        save(series);
        if (logger.isInfoEnabled())
            logger.info("Created: " + series);
        
        return series;
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#remove(org.dcm4che.archive.entity.EntityBase)
     */
    public void remove(PrivateSeries obj) throws ContentDeleteException {
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#save(org.dcm4che.archive.entity.EntityBase)
     */
    public void save(PrivateSeries obj) throws ContentCreateException {
    }

    /**
     * @see org.dcm4che.archive.dao.PrivateSeriesDAO#findBySeriesIuid(int,
     *      java.lang.String)
     */
    public Collection<PrivateSeries> findBySeriesIuid(int type,
            String seriesIuid) throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Searching for PrivateSeries entities with series iuid="
                            + seriesIuid);
        }

        Collection<PrivateSeries> series = null;

        Query query = em
                .createQuery("select ps from PrivateSeries as ps where ps.privateType=:type and ps.seriesIuid=:siuid");
        query.setParameter("type", type);
        query.setParameter("siuid", seriesIuid);
        series = query.getResultList();

        return series;
    }
}