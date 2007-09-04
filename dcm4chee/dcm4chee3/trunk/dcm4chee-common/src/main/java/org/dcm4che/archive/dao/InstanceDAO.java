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

import java.util.Collection;
import java.util.List;

import javax.ejb.Local;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.data.Dataset;

/**
 * Data access interface for DICOM instance records in the database.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface InstanceDAO extends DAO<Instance> {
    public static final String JNDI_NAME = "dcm4cheeArchive/InstanceDAOImpl/local";

    /**
     * @param uid
     * @return The found Instance.
     * @throws NoResultException
     */
    public Instance findBySopIuid(String uid) throws NoResultException,
            PersistenceException;

    /**
     * Create an Instance record in the database with the given attributes.
     * 
     * @param ds
     *            A {@link Dataset} containing the instance attributes.
     * @param series
     *            The {@link Series} object which owns the series.
     * @return The persisted Instance object.
     * @throws ContentCreateException
     *             If there was an error creating the database record.
     */
    public Instance create(Dataset ds, Series series)
            throws ContentCreateException;

    /**
     * @param instance
     * @param retrieveAETs
     * @param availability
     * @return
     */
    public boolean updateDerivedFields(Instance instance, boolean retrieveAETs,
            boolean availability);

    /**
     * @param suid
     * @param cuid
     * @param code
     * @param designator
     * @return
     */
    public List<Instance> findByStudyAndSrCode(String suid, String cuid,
            String code, String designator) throws PersistenceException;

    /**
     * @param iuids
     * @return
     */
    public List<Instance> listByIUIDs(String[] iuids)
            throws PersistenceException;

    /**
     * @param pat
     * @param srCodes
     * @param cuids
     * @return
     */
    public List<Instance> listByPatientAndSRCode(Patient pat,
            List<String> srCodes, Collection<String> cuids)
            throws PersistenceException;

    /**
     * Find instances by their containing series.
     * 
     * @param seriesPk
     *            The primary key of the series record.
     * @return A List of {@link Instance} objects
     * @throws PersistenceException
     */
    public List<Instance> findBySeriesPk(Long seriesPk)
            throws PersistenceException;

    /**
     * Find instances by their containing series.
     * 
     * @param seriesIuid
     *            The Series Instance UID
     * @return A List of {@link Instance} objects
     * @throws PersistenceException
     */
    public List<Instance> findBySeriesIuid(String seriesIuid)
            throws PersistenceException;

}
