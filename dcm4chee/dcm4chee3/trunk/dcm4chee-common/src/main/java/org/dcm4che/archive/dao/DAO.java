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
package org.dcm4che.archive.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.EntityBase;

/**
 * Base interface for data access objects.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface DAO<E extends EntityBase> {
    /**
     * Save an object's data into the database.
     * 
     * @param obj
     *            The <code>PersistentInterface</code> object to save.
     * @throws ContentCreateException
     */
    public void save(E obj) throws ContentCreateException;

    /**
     * Delete an object's data from the database.
     * 
     * @param obj
     *            The <code>PersistentInterface</code> object to delete.
     * @throws ContentDeleteException
     */
    public void remove(E obj) throws ContentDeleteException;

    /**
     * Find all instances of this class in the database.
     * 
     * @return List of persistent objects
     * @throws PersistenceException
     */
    public List<E> findAll() throws PersistenceException;

    /**
     * Find all instances of this class in the database, limiting the size of
     * the result set.
     * 
     * @param maxResults
     *            An int value defining the max size of the result set.
     * @return List of persistent objects
     * @throws PersistenceException
     */
    public List<E> findAll(int maxResults) throws PersistenceException;

    /**
     * Find all instances of this class in the database, limiting the size of
     * the result set.
     * 
     * @param offset
     *            An int value specifying the first result to return in the
     *            result set for paging.
     * @param maxResults
     *            An int value defining the max size of the result set.
     * @return List of persistent objects
     * @throws PersistenceException
     */
    public List<E> findAll(int offset, int maxResults)
            throws PersistenceException;

    /**
     * Find an instance of this class in the database by its primary key.
     * 
     * @param pk
     *            A Long containing the primary key.
     * @return The <code>PersistentInterface</code> implementation that was
     *         found in the database.
     * @throws NoResultException
     *             If the object was not found.
     */
    public E findByPrimaryKey(Long pk) throws NoResultException,
            PersistenceException;

    /**
     * Find an instance of this class in the database by its primary key.
     * 
     * @param pk
     *            A Long containing the primary key.
     * @return The <code>PersistentInterface</code> implementation that was
     *         found in the database.
     * @throws NoResultException
     *             If the object was not found.
     */
    public E findByPrimaryKey(Object pk) throws NoResultException,
            PersistenceException;
}
