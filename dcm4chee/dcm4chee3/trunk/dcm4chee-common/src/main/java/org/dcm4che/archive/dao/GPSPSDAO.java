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

import java.sql.Timestamp;
import java.util.List;

import javax.ejb.Local;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.GPSPS;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.data.Dataset;

/**
 * Data access contract for general purpose scheduled procedure steps (GPSPS).
 * 
 * @author <a href="mailto:jfalkmu@gmail.com">Justin Falk</a>
 */
@Local
public interface GPSPSDAO extends DAO<GPSPS> {

    /**
     * Find GPSPS by the item's DICOM UID.
     * 
     * @param iuid
     *            The GPSPS instance UID.
     * @return {@link GPSPS}
     * @throws NoResultException
     * @throws PersistenceException
     */
    public GPSPS findBySopIuid(String iuid) throws NoResultException,
            PersistenceException;

    public List<GPSPS> find(Long patientFk, String accessionNumber, List<Integer> statusList, Timestamp afterTimestamp) throws PersistenceException;

    /**
     * @param dataset
     * @param patient
     */
    public void create(Dataset dataset, Patient patient)
            throws ContentCreateException;

    /**
     * @param scheduled
     * @param codeValue
     * @param codeDesignator
     * @param rpid
     * @return
     */
    public List<GPSPS> findByReqProcId(int scheduled, String codeValue,
            String codeDesignator, String rpid) throws PersistenceException;

    /**
     * Get the site-specific prefix for created SPS IDs
     * 
     * @return String The prefix
     */
    public String getSpsIdPrefix();

    /**
     * Set the site-specific prefix for created SPS IDs
     * 
     * @param prefix
     *            A String containing the prefix.
     */
    public void setSpsIdPrefix(String prefix);
}