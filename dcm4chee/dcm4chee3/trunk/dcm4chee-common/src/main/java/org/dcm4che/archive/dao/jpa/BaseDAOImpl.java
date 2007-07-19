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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.DAO;
import org.dcm4che.archive.entity.EntityBase;

/**
 * A base class for data access objects.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */

public abstract class BaseDAOImpl<E extends EntityBase> implements DAO<E> {
    protected Logger logger = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager em;

    /**
     * Set the JPA {@link EntityManager}.
     * 
     * @param entityManager
     *            The EntityManager.
     */
    public void setEntityManager(EntityManager entityManager) {
        this.em = entityManager;
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#remove(org.dcm4che.archive.entity.EntityBase)
     */
    public void remove(E obj) throws ContentDeleteException {
        if (obj == null)
            throw new IllegalArgumentException(
                    "Null object passed into delete()");

        if (logger.isInfoEnabled()) {
            logger.info("Deleting " + obj);
        }

        try {
            em.remove(obj);
        }
        catch (Throwable e) {
            throw new ContentDeleteException(obj.getClass().toString(), e);
        }
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#findAll()
     */
    public List<E> findAll() throws PersistenceException {
        return findAll(-1);
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#findAll(int)
     */
    public List<E> findAll(int maxResults) throws PersistenceException {
        return findAll(0, maxResults);
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#findAll(int, int)
     */
    public List<E> findAll(int offset, int maxResults)
            throws PersistenceException {
        if (logger.isInfoEnabled()) {
            logger.info("Attempting to find all " + getPersistentClass());
            logger.info("offset=" + offset + " maxResults=" + maxResults);
        }

        List<E> results = null;
        Query query = em.createQuery("from " + getPersistentClass().getName());

        if (maxResults > 0)
            query.setMaxResults(maxResults);
        if (offset > 0)
            query.setFirstResult(offset);

        results = query.getResultList();

        if (results == null) {
            if (logger.isInfoEnabled())
                logger.info("Couldn't find any DB results for "
                        + getPersistentClass());
            results = new ArrayList<E>();
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info("Number fetched: " + results.size());
            }
        }

        return results;
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#findByPrimaryKey(java.lang.Long)
     */
    public E findByPrimaryKey(Long pk) throws NoResultException,
            PersistenceException {
        if (pk == null) {
            throw new IllegalArgumentException("PK is required");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Attempting to find " + getClass() + " with pk=" + pk);
        }

        Object obj = em.find(getPersistentClass(), pk);
        if (obj == null) {
            throw new NoResultException("Could not find "
                    + getPersistentClass() + " in the database");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Fetched : " + obj);
        }

        return (E) obj;
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#findByPk(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public E findByPrimaryKey(Object pk) throws NoResultException,
            PersistenceException {
        if (pk == null) {
            throw new IllegalArgumentException("PK is required");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Attempting to find " + getClass() + " with pk=" + pk);
        }

        Object obj = em.find(getPersistentClass(), pk);
        if (obj == null) {
            throw new NoResultException("Could not find "
                    + getPersistentClass() + " in the database");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Fetched : " + obj);
        }

        return (E) obj;
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#save(org.dcm4che.archive.entity.EntityBase)
     */
    public void save(E obj) throws ContentCreateException {
        if (obj == null)
            throw new IllegalArgumentException("Null object passed into save()");

        if (logger.isInfoEnabled()) {
            logger.info("Saving " + obj.getClass().getName());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Object details: " + obj);
        }

        try {
            em.persist(obj);
        }
        catch (Throwable e) {
            throw new ContentCreateException(obj.getClass().toString(), e);
        }
    }

    /**
     * Get the Class of the object the DAO has been built to access.
     * 
     * @return Class of the object the DAO has been built to access.
     */
    public abstract Class getPersistentClass();

}
