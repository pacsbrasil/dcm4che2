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
import java.util.List;

import javax.persistence.PersistenceException;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.MediaDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.service.impl.MediaComposer
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface MediaComposer {

    /**
     * 
     */
    public Collection getStudiesReceivedBefore(long time)
            throws PersistenceException;

    /**
     * @throws PersistenceException
     * @throws ContentCreateException
     * 
     */
    public List assignStudyToMedia(Study study, List mediaPool,
            long maxMediaSize, String prefix) throws PersistenceException,
            ContentCreateException;

    /**
     * 
     * 
     */
    public List getCollectingMedia() throws PersistenceException;

    /**
     * Returns a list of all media with the given media status.
     * <p>
     * The list contains a MediaDTO object for each media with the given status.
     * 
     * @param status
     *            The media status
     * 
     * @return A list of MediaDTO objects.
     * 
     * 
     */
    public List getWithStatus(int status) throws PersistenceException;

    /**
     * Find media for given search params.
     * <p>
     * Add all founded media to the given collection.<br>
     * This allows to fill a collection with sequential calls without clearing
     * the collection.<br>
     * 
     * @param col
     *            The collection to store the result.
     * @param after
     *            'created after' Timestamp in milliseconds
     * @param before
     *            'created before' Timestamp in milliseconds
     * @param stati
     *            Media status (<code>null</code> to get all media for given
     *            time range)
     * @param offset
     *            Offset of the find result. (used for paging.
     * @param limit
     *            Max. number of results to return. (used for paging)
     * @param desc
     *            Sort order. if true descending, false ascending order.
     * 
     * @return The total number of search results.
     * 
     * 
     */
    public int findByCreatedTime(Collection col, Long after, Long before,
            int[] stati, Integer offset, Integer limit, boolean desc)
            throws PersistenceException;

    /**
     * Find media for given search params.
     * <p>
     * Add all founded media to the given collection.<br>
     * This allows to fill a collection with sequential calls without clearing
     * the collection.<br>
     * 
     * @param col
     *            The collection to store the result.
     * @param after
     *            'updated after' Timestamp in milliseconds
     * @param before
     *            'updated before' Timestamp in milliseconds
     * @param stati
     *            Media status (<code>null</code> to get all media for given
     *            time range)
     * @param offset
     *            Offset of the find result. (used for paging.
     * @param limit
     *            Max. number of results to return. (used for paging)
     * @param desc
     *            Sort order. if true descending, false ascending order.
     * 
     * @return The total number of search results.
     * 
     * 
     */
    public int findByUpdatedTime(Collection col, Long after, Long before,
            int[] stati, Integer offset, Integer limit, boolean desc)
            throws PersistenceException;

    /**
     * Set the media creation request IUID.
     * 
     * @param pk
     *            Primary key of media.
     * @param iuid
     *            Media creation request IUID to set.
     * 
     * 
     */
    public void setMediaCreationRequestIuid(long pk, String iuid)
            throws PersistenceException;

    /**
     * Set media staus and status info.
     * 
     * @param pk
     *            Primary key of media.
     * @param status
     *            Status to set.
     * @param info
     *            Status info to set.
     * 
     * 
     */
    public void setMediaStatus(long pk, int status, String info)
            throws PersistenceException;

    /**
     * Returns a collection of study IUIDs of a given media.
     * 
     * @param pk
     *            Primary key of the media.
     * 
     * @return Collection with study IUIDs.
     * 
     * 
     */
    public Collection<String> getStudyUIDSForMedia(long pk)
            throws PersistenceException;

    /**
     * Returns a dataset for media creation request for given media.
     * <p>
     * <DL>
     * <DT>Set following Tags in dataset.</DT>
     * <DD>SpecificCharacterSet</DD>
     * <DD>StorageMediaFileSetID</DD>
     * <DD>StorageMediaFileSetUID</DD>
     * <DD>RefSOPSeq with instances of the media.</DD>
     * </DL>
     * 
     * @param pk
     *            Primary key of the media.
     * 
     * @return Prepared Dataset for media creation request.
     * 
     * 
     */
    public Dataset prepareMediaCreationRequest(long pk)
            throws PersistenceException;

    /**
     * Deletes a media.
     * <p>
     * Update derived fields from series and studies after media is successfully
     * deleted.
     * 
     * @param mediaPk
     *            Primary key of the media.
     * 
     * 
     */
    public void deleteMedia(Long mediaPk) throws ContentDeleteException,
            PersistenceException;

    /**
     * Checks if all instances of a media are locally available (online).
     * <p>
     * Update derived fields from series and studies after media is successfully
     * deleted.
     * 
     * @param mediaPk
     *            Primary key of the media.
     * 
     * 
     */
    public boolean checkInstancesAvailable(Long mediaPk)
            throws PersistenceException;

    /**
     * @return the mediaDAO
     */
    public MediaDAO getMediaDAO();

    /**
     * @param mediaDAO
     *            the mediaDAO to set
     */
    public void setMediaDAO(MediaDAO mediaDAO);

    /**
     * @return the seriesDAO
     */
    public SeriesDAO getSeriesDAO();

    /**
     * @param seriesDAO
     *            the seriesDAO to set
     */
    public void setSeriesDAO(SeriesDAO seriesDAO);

    /**
     * @return the studyDAO
     */
    public StudyDAO getStudyDAO();

    /**
     * @param studyDAO
     *            the studyDAO to set
     */
    public void setStudyDAO(StudyDAO studyDAO);

}