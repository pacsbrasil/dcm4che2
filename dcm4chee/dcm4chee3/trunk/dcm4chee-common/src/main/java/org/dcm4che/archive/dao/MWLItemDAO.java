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

import javax.ejb.Local;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.MWLItem;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.data.Dataset;

/**
 * Data access contract for modality worklist items.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface MWLItemDAO extends DAO<MWLItem> {

    /**
     * Create an {@link MWLItem} in the database.
     * 
     * @param ds
     *            The {@link Dataset} containing all of the necessary
     *            information for the item.
     * @param patient
     *            The {@link Patient} that the item is being performed for.
     * @return
     * @return The created {@link MWLItem}
     * @throws ContentCreateException
     */
    public MWLItem create(Dataset ds, Patient patient)
            throws ContentCreateException;

    /**
     * Find an {@link MWLItem} in the database.
     * 
     * @param rpid
     *            String containing the requested procedure id.
     * @param spsid
     *            String containing the scheduled procedure step id.
     * @return The found {@link MWLItem}
     * @throws NoResultException
     *             If the item was not found.
     */
    public MWLItem findByRpIdAndSpsId(String rpid, String spsid) throws NoResultException, PersistenceException;
    
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
    
    /**
     * Get the site-specific prefix for created Requested Procedure IDs
     * 
     * @return String The prefix
     */
    public String getRpIdPrefix();

    /**
     * Set the site-specific prefix for created Requested Procedure IDs
     * 
     * @param prefix
     *            A String containing the prefix.
     */
    public void setRpIdPrefix(String prefix);

}
