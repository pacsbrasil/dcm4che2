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

import java.sql.Timestamp;

import javax.persistence.PersistenceException;

import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;

/**
 * org.dcm4che.archive.service.impl.ConsistencyCheck
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface ConsistencyCheck {

    /**
     * Return studies to check consistency..
     * <p>
     * <DL>
     * <DD>1) Find (0-<code>limit</code>) studies with a creation date
     * between <code>createdAfter and createdBefore</code> and not checked
     * before checkedAfter</DD>
     * </DL>
     * 
     * @param createdAfter
     *            Timestamp: studies must be created after this timestamp.
     * @param createdBefore
     *            Timestamp: studies must be created before this timestamp.
     * @param checkedBefore
     *            Timestamp: studies must be checked before this timestamp.
     * @param limit
     *            Max number of returned studies.
     * 
     * @return int array with pk of studies to check.
     * 
     */
    public long[] findStudiesToCheck(Timestamp createdAfter,
            Timestamp createdBefore, Timestamp checkedBefore, int limit)
            throws PersistenceException;

    /**
     * 
     */
    public boolean updateStudy(long study_pk);

    /**
     * @return the studyDAO
     */
    public StudyDAO getStudyDAO();

    /**
     * @param studyDAO
     *            the studyDAO to set
     */
    public void setStudyDAO(StudyDAO studyDAO);

    /**
     * @return the instanceDAO
     */
    public InstanceDAO getInstanceDAO();

    /**
     * @param instanceDAO
     *            the instanceDAO to set
     */
    public void setInstanceDAO(InstanceDAO instanceDAO);

    /**
     * @return the seriesDAO
     */
    public SeriesDAO getSeriesDAO();

    /**
     * @param seriesDAO
     *            the seriesDAO to set
     */
    public void setSeriesDAO(SeriesDAO seriesDAO);

}