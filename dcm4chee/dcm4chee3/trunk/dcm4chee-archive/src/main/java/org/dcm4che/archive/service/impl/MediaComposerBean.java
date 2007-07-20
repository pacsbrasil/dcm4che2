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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.daddy@gmail.com>
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4che.archive.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.MediaDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Media;
import org.dcm4che.archive.entity.MediaDTO;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.service.MediaComposer;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.UIDGenerator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2007/06/23 18:59:01 $
 * @since 14.12.2004
 */
@Transactional(propagation = Propagation.REQUIRED)
public class MediaComposerBean implements MediaComposer {

    private static Logger log = Logger.getLogger(MediaComposerBean.class
            .getName());

    private MediaDAO mediaDAO;

    private StudyDAO studyDAO;

    private SeriesDAO seriesDAO;

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#getStudiesReceivedBefore(long)
     */
    public Collection getStudiesReceivedBefore(long time)
            throws PersistenceException {
        return studyDAO.findStudiesNotOnMedia(new Timestamp(time));
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#assignStudyToMedia(org.dcm4che.archive.entity.Study, java.util.List, long, java.lang.String)
     */
    public List assignStudyToMedia(Study study, List mediaPool,
            long maxMediaSize, String prefix) throws PersistenceException,
            ContentCreateException {
        if (mediaPool == null) {
            mediaPool = getCollectingMedia();
        }
        Map instanceFiles = new HashMap();
        Instance instance;
        File file, file2;
        int avail, avail2;
        Collection files;
        long size = 0;
        for (Iterator iter = studyDAO.findInstancesNotOnMedia(study).iterator(); iter
                .hasNext();) {
            instance = (Instance) iter.next();
            files = instance.getFiles();
            if (files.isEmpty()) {
                log.warn("Instance " + instance.getPk() + "("
                        + instance.getSopIuid() + ") has no files! ignored!");
                continue;
            }
            Iterator it = files.iterator();
            for (file = (File) it.next(); it.hasNext();) {
                file2 = (File) it.next();
                avail = file.getFileSystem().getAvailability();
                avail2 = file2.getFileSystem().getAvailability();
                if (avail2 < avail) {
                    file = file2;
                }
                else if (file2.getPk().longValue() > file.getPk().longValue()) {
                    file = file2;
                }
            }
            size += file.getFileSize();
            instanceFiles.put(instance, file);
        }
        if (size > maxMediaSize) {
            log.info("Study size (" + size + ") exceed maxMediaSize ("
                    + maxMediaSize + ")! Split study is necessary!");
            splitStudy(instanceFiles, mediaPool, maxMediaSize, prefix);
        }
        else {
            log.debug("assign study " + study.getStudyIuid() + " (size=" + size
                    + ") to media");
            assignInstancesToMedia(instanceFiles.keySet(), size, getMedia(
                    mediaPool, maxMediaSize - size, prefix));
        }
        return mediaPool;
    }

    /**
     * 
     * @param instanceFiles
     *            map with all instances for a media.
     * @param maxUsed
     *            The max mediaUsage a media must have to can store all
     *            instances.
     * @param mediaPool
     *            List of COLLECTING media
     * @param prefix
     *            Prefix for Fileset ID. (Used if a new media must be created.
     * @throws ContentCreateException
     */
    private void assignInstancesToMedia(Set instances, long size, Media media)
            throws ContentCreateException {
        log.debug("assign instances (" + instances + ") to media " + media);
        for (Iterator iter = instances.iterator(); iter.hasNext();) {
            ((Instance) iter.next()).setMedia(media);
        }
        media.setMediaUsage(media.getMediaUsage() + size);
    }

    private Media getMedia(List mediaPool, long maxUsed, String prefix)
            throws ContentCreateException {
        maxUsed++;
        if (mediaPool.size() > 0) {
            Media media;
            for (Iterator iter = mediaPool.iterator(); iter.hasNext();) {
                media = (Media) iter.next();
                if (media.getMediaUsage() < maxUsed)
                    return media;
            }
        }
        log.debug("We need new media! mediaPool:" + mediaPool);
        Media media = createMedia(prefix);
        mediaPool.add(media);
        return media;

    }

    /**
     * @param instanceFiles
     * @param maxMediaSize
     * @param prefix
     * @throws ContentCreateException
     */
    private void splitStudy(Map instanceFiles, List mediaPool,
            long maxMediaSize, String prefix) throws ContentCreateException {
        Map.Entry entry;
        log.info("Split study!");
        long fileSize, size = 0;
        Set instances = new HashSet();
        for (Iterator iter = instanceFiles.entrySet().iterator(); iter
                .hasNext();) {
            entry = (Map.Entry) iter.next();
            fileSize = ((File) entry.getValue()).getFileSize();
            size += fileSize;
            if (size > maxMediaSize) {
                log.debug("assign instances (" + instances.size() + "/"
                        + instanceFiles.size() + ") to new media!");
                assignInstancesToMedia(instances, size, createMedia(prefix));
                size = fileSize;
                instances.clear();
            }
            instances.add(entry.getKey());
        }
        if (!instances.isEmpty()) {
            log.debug("assign remaining instances (" + instances.size() + "/"
                    + instanceFiles.size() + ") to new media!");
            assignInstancesToMedia(instances, size, getMedia(mediaPool,
                    maxMediaSize - size, prefix));
        }
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#getCollectingMedia()
     */
    public List getCollectingMedia() throws PersistenceException {
        List mediaCollection = mediaDAO.findByStatus(MediaDTO.OPEN);
        Comparator comp = new Comparator() {
            public int compare(Object arg0, Object arg1) {
                Media ml1 = (Media) arg0;
                Media ml2 = (Media) arg1;
                return (int) (ml2.getMediaUsage() - ml1.getMediaUsage());// more
                // usage
                // before
                // lower
                // usage!
            }
        };
        Collections.sort(mediaCollection, comp);
        log.debug("Number of 'COLLECTING' media found:"
                + mediaCollection.size());
        return mediaCollection;
    }

    /**
     * Creates a new media.
     * <p>
     * Set the fileset id with given prefix and the pk of the new media.
     * 
     * @param prefix
     *            A prefix for fileset id.
     * 
     * @return The new created Media bean.
     * @throws ContentCreateException
     */
    private Media createMedia(String prefix) throws ContentCreateException {
        Media ml = mediaDAO.create(UIDGenerator.getInstance().createUID());
        ml.setFilesetId(prefix + ml.getPk());
        ml.setMediaStatus(MediaDTO.OPEN);
        if (log.isInfoEnabled())
            log.info("New media created:" + ml.getFilesetId());
        return ml;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#getWithStatus(int)
     */
    public List getWithStatus(int status) throws PersistenceException {
        return toMediaDTOs(mediaDAO.findByStatus(status));
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#findByCreatedTime(java.util.Collection, java.lang.Long, java.lang.Long, int[], java.lang.Integer, java.lang.Integer, boolean)
     */
    public int findByCreatedTime(Collection col, Long after, Long before,
            int[] stati, Integer offset, Integer limit, boolean desc)
            throws PersistenceException {
        Timestamp tsAfter = null;
        if (after != null)
            tsAfter = new Timestamp(after.longValue());
        Timestamp tsBefore = null;
        if (before != null)
            tsBefore = new Timestamp(before.longValue());
        col.addAll(toMediaDTOs(mediaDAO.listByCreatedTime(stati, tsAfter,
                tsBefore, offset, limit, desc)));
        return mediaDAO.countByCreatedTime(stati, tsAfter, tsBefore);
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#findByUpdatedTime(java.util.Collection, java.lang.Long, java.lang.Long, int[], java.lang.Integer, java.lang.Integer, boolean)
     */
    public int findByUpdatedTime(Collection col, Long after, Long before,
            int[] stati, Integer offset, Integer limit, boolean desc)
            throws PersistenceException {
        Timestamp tsAfter = null;
        if (after != null)
            tsAfter = new Timestamp(after.longValue());
        Timestamp tsBefore = null;
        if (before != null)
            tsBefore = new Timestamp(before.longValue());
        col.addAll(toMediaDTOs(mediaDAO.listByUpdatedTime(stati, tsAfter,
                tsBefore, offset, limit, desc)));
        return mediaDAO.countByUpdatedTime(stati, tsAfter, tsBefore);
    }

    /**
     * Converts a collection of Media objects to a list of MediaDTO objects.
     * 
     * @param c
     *            Collection with Media objects.
     * 
     * @return List of MediaDTO objects.
     */
    private List toMediaDTOs(Collection c) {
        ArrayList list = new ArrayList();
        for (Iterator it = c.iterator(); it.hasNext();) {
            list.add(toMediaDTO((Media) it.next()));
        }
        return list;
    }

    /**
     * Creates a MediaDTO object for given given Media object.
     * 
     * @param media
     *            A Media object.
     * 
     * @return The MediaDTO object for given Media.
     */
    private MediaDTO toMediaDTO(Media media) {
        MediaDTO dto = new MediaDTO();
        dto.setPk(media.getPk().longValue());
        dto.setCreatedTime(media.getCreatedTime());
        dto.setUpdatedTime(media.getUpdatedTime());
        dto.setMediaUsage(media.getMediaUsage());
        dto.setMediaStatus(media.getMediaStatus());
        dto.setMediaStatusInfo(media.getMediaStatusInfo());
        dto.setFilesetId(media.getFilesetId());
        dto.setFilesetIuid(media.getFilesetIuid());
        dto.setMediaCreationRequestIuid(media.getMediaCreationRequestIuid());
        try {
            dto.setInstancesAvailable(mediaDAO.checkInstancesAvailable(media
                    .getPk()));
        }
        catch (PersistenceException e) { /* ignore */
        }
        return dto;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#setMediaCreationRequestIuid(long, java.lang.String)
     */
    public void setMediaCreationRequestIuid(long pk, String iuid)
            throws PersistenceException {
        Media media = mediaDAO.findByPrimaryKey(new Long(pk));
        media.setMediaCreationRequestIuid(iuid);
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#setMediaStatus(long, int, java.lang.String)
     */
    public void setMediaStatus(long pk, int status, String info)
            throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug("setMediaStatus: pk=" + pk + ", status:" + status
                    + ", info" + info);
        Media media = mediaDAO.findByPrimaryKey(new Long(pk));
        media.setMediaStatus(status);
        media.setMediaStatusInfo(info);
        if (status == MediaDTO.COMPLETED)
            updateSeriesAndStudies(media);
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#getStudyUIDSForMedia(long)
     */
    public Collection<String> getStudyUIDSForMedia(long pk)
            throws PersistenceException {
        Collection<String> c = new ArrayList();
        Media media = mediaDAO.findByPrimaryKey(new Long(pk));
        Collection<Study> studies = studyDAO.findStudiesOnMedia(media);
        for (Iterator iter = studies.iterator(); iter.hasNext();) {
            c.add(((Study) iter.next()).getStudyIuid());
        }
        return c;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#prepareMediaCreationRequest(long)
     */
    public Dataset prepareMediaCreationRequest(long pk)
            throws PersistenceException {
        Media media = mediaDAO.findByPrimaryKey(new Long(pk));
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        ds.putSH(Tags.StorageMediaFileSetID, media.getFilesetId());
        ds.putUI(Tags.StorageMediaFileSetUID, media.getFilesetIuid());
        Collection c = media.getInstances();
        Instance il;
        DcmElement refSOPSeq = ds.putSQ(Tags.RefSOPSeq);
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            il = (Instance) iter.next();
            Dataset item = refSOPSeq.addNewItem();
            item.putUI(Tags.RefSOPInstanceUID, il.getSopIuid());
            item.putUI(Tags.RefSOPClassUID, il.getSopCuid());
        }
        return ds;
    }

    private void updateSeriesAndStudies(Media media)
            throws PersistenceException {
        Collection<Series> series = seriesDAO.findSeriesOnMedia(media);
        Iterator iter = series.iterator();
        while (iter.hasNext()) {
            final Series ser = ((Series) iter.next());
            seriesDAO
                    .updateDerivedFields(ser, false, false, false, true, false);
        }
        Collection studies = studyDAO.findStudiesOnMedia(media);
        iter = studies.iterator();
        while (iter.hasNext()) {
            final Study sty = ((Study) iter.next());
            studyDAO.updateDerivedFields(sty, false, false, false, true, false,
                    false);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#deleteMedia(java.lang.Long)
     */
    public void deleteMedia(Long mediaPk) throws ContentDeleteException,
            PersistenceException {
        Media media = mediaDAO.findByPrimaryKey(mediaPk);
        Collection<Series> series = seriesDAO.findSeriesOnMedia(media);
        Collection<Study> studies = studyDAO.findStudiesOnMedia(media);
        String filesetId = media.getFilesetId();
        mediaDAO.remove(mediaPk);
        log.info("Media " + filesetId + " removed!");
        Iterator iter = series.iterator();
        while (iter.hasNext()) {
            Series ser = (Series) iter.next();
            seriesDAO.updateDerivedFields(ser, false, false, false, true, true);
        }
        if (log.isDebugEnabled())
            log.debug("Series updated after media " + filesetId
                    + " was deleted!");
        iter = studies.iterator();
        while (iter.hasNext()) {
            Study sty = (Study) iter.next();
            studyDAO.updateDerivedFields(sty, false, false, false, true, true,
                    false);
        }
        if (log.isDebugEnabled())
            log.debug("Studies updated after media " + filesetId
                    + " was deleted!");

    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#checkInstancesAvailable(java.lang.Long)
     */
    public boolean checkInstancesAvailable(Long mediaPk)
            throws PersistenceException {
        return mediaDAO.checkInstancesAvailable(mediaPk);
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#getMediaDAO()
     */
    public MediaDAO getMediaDAO() {
        return mediaDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#setMediaDAO(org.dcm4che.archive.dao.MediaDAO)
     */
    public void setMediaDAO(MediaDAO mediaDAO) {
        this.mediaDAO = mediaDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.MediaComposer#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }
}