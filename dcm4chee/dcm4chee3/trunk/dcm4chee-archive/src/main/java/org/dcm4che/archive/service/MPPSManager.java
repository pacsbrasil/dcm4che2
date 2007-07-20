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

package org.dcm4che.archive.service;

import java.util.Collection;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.data.Dataset;
import org.dcm4che.net.DcmServiceException;

/**
 * org.dcm4che.archive.service.impl.MPPSManager
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface MPPSManager {

    /**
     * 
     */
    public void createMPPS(Dataset ds) throws DcmServiceException;

    /**
     * 
     */
    public Dataset getMPPS(String iuid) throws PersistenceException;

    /**
     * 
     */
    public void updateMPPS(Dataset ds) throws DcmServiceException;

    /**
     * Links a mpps to a mwl entry (LOCAL).
     * <p>
     * This method can be used if MWL entry is locally available.
     * <p>
     * Sets SpsID and AccessionNumber from mwl entry.
     * <P>
     * Returns a Map with following key/value pairs.
     * <dl>
     * <dt>mppsAttrs: (Dataset)</dt>
     * <dd> Attributes of mpps entry. (for notification)</dd>
     * <dt>mwlPat: (Dataset)</dt>
     * <dd> Patient of MWL entry.</dd>
     * <dd> (The dominant patient of patient merge).</dd>
     * <dt>mppsPat: (Dataset)</dt>
     * <dd> Patient of MPPS entry.</dd>
     * <dd> (The merged patient).</dd>
     * </dl>
     * 
     * @param mwlPk
     *            pk to select MWL entry
     * @param mppsIUID
     *            Instance UID of mpps.
     * 
     * @return A map with mpps attributes and patient attributes to merge.
     * 
     * 
     */
    public Map linkMppsToMwl(String rpid, String spsid, String mppsIUID)
            throws DcmServiceException;

    /**
     * Links a mpps to a mwl entry (external).
     * <p>
     * This Method can be used to link a MPPS entry with an MWL entry from an
     * external Modality Worklist.
     * <p>
     * Sets SpsID and AccessionNumber from mwlDs.
     * <P>
     * Returns a Map with following key/value pairs.
     * <dl>
     * <dt>mppsAttrs: (Dataset)</dt>
     * <dd> Attributes of mpps entry. (for notification)</dd>
     * <dt>mwlPat: (Dataset)</dt>
     * <dd> Patient of MWL entry.</dd>
     * <dd> (The dominant patient of patient merge).</dd>
     * <dt>mppsPat: (Dataset)</dt>
     * <dd> Patient of MPPS entry.</dd>
     * <dd> (The merged patient).</dd>
     * </dl>
     * 
     * @param mwlDs
     *            Datset of MWL entry
     * @param mppsIUID
     *            Instance UID of mpps.
     * 
     * @return A map with mpps attributes and patient attributes to merge.
     * @throws PersistenceException
     * @throws ContentCreateException
     * 
     * 
     */
    public Map linkMppsToMwl(Dataset mwlAttrs, String mppsIUID)
            throws DcmServiceException, PersistenceException,
            ContentCreateException;

    /**
     * 
     */
    public void unlinkMpps(String mppsIUID) throws PersistenceException;

    /**
     * Delete a list of mpps entries.
     * 
     * 
     */
    public boolean deleteMPPSEntries(String[] iuids);

    /**
     * 
     */
    public Collection getSeriesIUIDs(String mppsIUID)
            throws PersistenceException;

    /**
     * 
     */
    public Collection getSeriesAndStudyDS(String mppsIUID)
            throws PersistenceException;

    /**
     * Returns a StudyMgt Dataset.
     * 
     * 
     */
    public Dataset updateSeriesAndStudy(Collection seriesDS)
            throws PersistenceException, ContentCreateException;

}