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
     * @see org.dcm4che.archive.dao.MediaDAO#checkInstancesAvailable(java.lang.Long)
     */
    public boolean checkInstancesAvailable(Long mediaPk)
            throws PersistenceException {
        // TODO
        return false;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#countByCreatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp)
     */
    public int countByCreatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore) throws PersistenceException {
        // TODO
        return 0;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#countByUpdatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp)
     */
    public int countByUpdatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore) throws PersistenceException {
        // TODO
        return 0;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#create(java.lang.String)
     */
    public Media create(String uid) throws ContentCreateException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#findByStatus(int)
     */
    public List<Media> findByStatus(int open) throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#listByCreatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp, java.lang.Integer,
     *      java.lang.Integer, boolean)
     */
    public Collection<Media> listByCreatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore, Integer offset, Integer limit, boolean desc)
            throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#listByUpdatedTime(int[],
     *      java.sql.Timestamp, java.sql.Timestamp, java.lang.Integer,
     *      java.lang.Integer, boolean)
     */
    public Collection listByUpdatedTime(int[] stati, Timestamp tsAfter,
            Timestamp tsBefore, Integer offset, Integer limit, boolean desc)
            throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.MediaDAO#remove(java.lang.Long)
     */
    public void remove(Long mediaPk) throws ContentDeleteException {
        // TODO
    }

}
