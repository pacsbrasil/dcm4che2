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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.MediaDAO;
import org.dcm4che.archive.entity.Media;

/**
 * org.dcm4che.archive.dao.jpa.MediaDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class MediaDAOImpl extends BaseDAOImpl<Media> implements MediaDAO {

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return Media.class;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#create(java.lang.String)
     */
    public Media create(String uid) throws ContentCreateException {
        Media media = new Media();
        media.setFilesetIuid(uid);
        save(media);
        logger.info("Created " + media.toString());
        return media;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#remove(java.lang.Long)
     */
    public void remove(Long mediaPk) throws ContentDeleteException {
        remove(findByPrimaryKey(mediaPk));
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#checkInstancesAvailable(java.lang.Long)
     */
    public boolean checkInstancesAvailable(Long mediaPk)
            throws PersistenceException {
        Query q = em
                .createQuery("select max(i.availability) from Instance i where i.media.pk=:mediaPk");
        q.setParameter("mediaPk", mediaPk);
        Number result = (Number) q.getSingleResult();

        return result != null && result.intValue() == Availability.ONLINE;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#findByStatus(int)
     */
    public List<Media> findByStatus(int open) throws PersistenceException {
        Query q = em
                .createQuery("select m from Media m where m.mediaStatus=:mediaStatus");
        q.setParameter("mediaStatus", open);
        List<Media> results = q.getResultList();
        return results;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#countByCreatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp)
     */
    public int countByCreatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore) throws PersistenceException {
        return countBy("m.createdTime", stati, tsAfter, tsBefore);
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#countByUpdatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp)
     */
    public int countByUpdatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore) throws PersistenceException {
        return countBy("m.updatedTime", stati, tsAfter, tsBefore);
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#listByCreatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp, java.lang.Integer,
     *      java.lang.Integer, boolean)
     */
    public Collection<Media> listByCreatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore, Integer offset, Integer limit, boolean desc)
            throws PersistenceException {
        return findBy("m.createdTime", stati, tsAfter, tsBefore, offset, limit,
                desc);
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#listByUpdatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp, java.lang.Integer,
     *      java.lang.Integer, boolean)
     */
    public Collection listByUpdatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore, Integer offset, Integer limit, boolean desc)
            throws PersistenceException {
        return findBy("m.updatedTime", stati, tsAfter, tsBefore, offset, limit,
                desc);
    }

    private Collection findBy(String attrName, int[] status, Timestamp after,
            Timestamp before, Integer offset, Integer limit, boolean desc) {
        StringBuffer jpaql = new StringBuffer("select m from Media m");
        appendWhere(jpaql, attrName, status, after, before);
        jpaql.append(" order by ");
        jpaql.append(attrName);
        jpaql.append(desc ? " desc" : " asc");

        Query q = em.createQuery(jpaql.toString());
        if (after != null)
            q.setParameter("after", after, TemporalType.TIMESTAMP);
        if (before != null)
            q.setParameter("before", before, TemporalType.TIMESTAMP);

        if (offset != null) {
            q.setFirstResult(offset);
        }
        if (limit != null) {
            q.setMaxResults(limit);
        }

        return q.getResultList();
    }

    private int countBy(String attrName, int[] status, Timestamp after,
            Timestamp before) {
        StringBuffer jpaql = new StringBuffer("select count(m) from Media m");
        appendWhere(jpaql, attrName, status, after, before);

        Query q = em.createQuery(jpaql.toString());
        if (after != null)
            q.setParameter("after", after, TemporalType.TIMESTAMP);
        if (before != null)
            q.setParameter("before", before, TemporalType.TIMESTAMP);

        Number results = (Number) q.getSingleResult();
        return results == null ? 0 : results.intValue();
    }

    private void appendWhere(StringBuffer jpaql, String attrName, int[] status,
            Timestamp after, Timestamp before) {
        boolean addedWhere = false;

        if (!isNull(status)) {
            jpaql.append(" where m.mediaStatus in (").append(status[0]);
            for (int i = 1; i < status.length; i++) {
                jpaql.append(", ").append(status[i]);
            }
            jpaql.append(")");
            addedWhere = true;
        }

        if (after != null) {
            jpaql.append(addedWhere ? " and " : " where ");
            jpaql.append(attrName);
            jpaql.append(" > :after");
            addedWhere = true;
        }

        if (before != null) {
            jpaql.append(addedWhere ? " and " : " where ");
            jpaql.append(attrName);
            jpaql.append(" > :before");
        }
    }

    private static boolean isNull(int[] status) {
        return status == null || status.length == 0;
    }
}