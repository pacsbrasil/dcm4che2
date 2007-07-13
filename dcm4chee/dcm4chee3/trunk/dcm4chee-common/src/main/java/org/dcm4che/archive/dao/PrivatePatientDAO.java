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

package org.dcm4che.archive.dao;

import java.util.Collection;

import javax.ejb.Local;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.PrivatePatient;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.dao.PrivatePatientDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface PrivatePatientDAO extends DAO<PrivatePatient> {
    public PrivatePatient create(int type, Dataset ds) throws ContentCreateException;

    /**
     * @param long1
     */
    public void remove(Long long1) throws ContentDeleteException;

    /**
     * @param privateType
     * @return
     */
    public Collection<PrivatePatient> findByPrivateType(int privateType) throws PersistenceException;

    /**
     * @param type
     * @param patientId
     * @param issuerOfPatientId
     * @return
     */
    public Collection<PrivatePatient> findByPatientIdWithIssuer(int type, String patientId, String issuerOfPatientId) throws PersistenceException;

}
