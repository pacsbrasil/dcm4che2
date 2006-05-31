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

package org.dcm4chex.archive.ejb.session;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.ActionOrder;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.FileSystemStatus;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.JmsSenderLocal;
import org.dcm4chex.archive.ejb.interfaces.JmsSenderLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PrivateFileLocal;
import org.dcm4chex.archive.ejb.interfaces.PrivateFileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 12.09.2004
 * 
 * @ejb.bean name="FileSystemMgt" type="Stateless" view-type="remote"
 *           jndi-name="ejb/FileSystemMgt"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="File" view-type="local" ref-name="ejb/File"
 * @ejb.ejb-ref ejb-name="PrivateFile" view-type="local"
 *              ref-name="ejb/PrivateFile"
 * @ejb.ejb-ref ejb-name="FileSystem" ref-name="ejb/FileSystem"
 *              view-type="local"
 * @ejb.ejb-ref ejb-name="Study" ref-name="ejb/Study" view-type="local"
 * @ejb.ejb-ref ejb-name="StudyOnFileSystem" ref-name="ejb/StudyOnFileSystem"
 *              view-type="local"
 *
 * @ejb.ejb-ref ejb-name="JmsSender" view-type="local" ref-name="ejb/JmsSender"
 * 
 * @ejb.env-entry name="FileSystemMgtBean/PurgeFilesQueueName" 
 * 			value="PurgeFiles"
 *
 */
public abstract class FileSystemMgtBean implements SessionBean {

    private static Logger log = Logger.getLogger(FileSystemMgtBean.class);
	private static final int[] IAN_PAT_TAGS = {
		Tags.SpecificCharacterSet, Tags.PatientName, Tags.PatientID
	};

    private StudyLocalHome studyHome;

    private StudyOnFileSystemLocalHome sofHome;

    private FileLocalHome fileHome;

    private PrivateFileLocalHome privFileHome;

    private FileSystemLocalHome fileSystemHome;
    
    private JmsSenderLocalHome jmsSenderHome;
    
    private String purgeFileQueueName;
    
    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            this.studyHome = (StudyLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Study");
            this.sofHome = (StudyOnFileSystemLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/StudyOnFileSystem");
            this.fileHome = (FileLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/File");
            this.privFileHome = (PrivateFileLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/PrivateFile");
            this.fileSystemHome = (FileSystemLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/FileSystem");
            this.jmsSenderHome = (JmsSenderLocalHome) jndiCtx
            		.lookup("java:comp/env/ejb/JmsSender");
			this.purgeFileQueueName = (String) jndiCtx
					.lookup("java:comp/env/FileSystemMgtBean/PurgeFilesQueueName");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        studyHome = null;
        sofHome = null;
        fileHome = null;
        fileSystemHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public void deletePrivateFile(long file_pk) throws RemoteException,
            EJBException, RemoveException {
        privFileHome.remove(new Long(file_pk));
    }
    
    /**
     * @ejb.interface-method
     */
    public FileDTO[] getDereferencedPrivateFiles(String dirPath, int limit)
            throws FinderException {
        log.debug("Querying for dereferenced files in " + dirPath);
        Collection c = privFileHome
                .findDereferencedInFileSystem(dirPath, limit);
        log.debug("Found " + c.size() + " dereferenced files in " + dirPath);
        return toFileDTOsPrivate(c);
    }

    private FileDTO[] toFileDTOsPrivate(Collection c) {
        FileDTO[] dto = new FileDTO[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < dto.length; ++i) {
            dto[i] = ((PrivateFileLocal) it.next()).getFileDTO();
        }
        return dto;
    }

    private FileDTO[] toFileDTOs(Collection c) {
        FileDTO[] dto = new FileDTO[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < dto.length; ++i) {
            dto[i] = ((FileLocal) it.next()).getFileDTO();
        }
        return dto;
    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public FileDTO[] findFilesToCompress(String dirPath, String cuid,
            Timestamp before, int limit) throws FinderException {
        if (log.isDebugEnabled())
            log.debug("Querying for files to compress in " + dirPath);
        Collection c = fileHome.findFilesToCompress(dirPath, cuid, before,
                limit);
        if (log.isDebugEnabled())
            log.debug("Found " + c.size() + " files to compress in " + dirPath);
        return toFileDTOs(c);

    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public FileDTO[] findFilesForMD5Check(String dirPath, Timestamp before,
            int limit) throws FinderException {
        if (log.isDebugEnabled())
            log.debug("Querying for files to check md5 in " + dirPath);
        Collection c = fileHome.findToCheckMd5(dirPath, before, limit);
        if (log.isDebugEnabled())
            log
                    .debug("Found " + c.size() + " files to check md5 in "
                            + dirPath);
        return toFileDTOs(c);

    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public FileDTO[] findFilesByStatusAndFileSystem(String dirPath, int status,
            Timestamp before, int limit) throws FinderException {
        if (log.isDebugEnabled())
            log.debug("Querying for files with status " + status + " in "
                    + dirPath);
        Collection c = fileHome.findByStatusAndFileSystem(dirPath, status,
                before, limit);
        if (log.isDebugEnabled())
            log.debug("Found " + c.size() + " files with status " + status
                    + " in " + dirPath);
        return toFileDTOs(c);
    }
    
    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public void updateTimeOfLastMd5Check(long pk) throws FinderException {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        if (log.isDebugEnabled())
            log.debug("update time of last md5 check to " + ts);
        FileLocal fl = fileHome.findByPrimaryKey(new Long(pk));
        fl.setTimeOfLastMd5Check(ts);
    }

    /**
     * @ejb.interface-method
     */
    public void replaceFile(long pk, String path, String tsuid, int size,
            byte[] md5) throws FinderException, CreateException {
        FileLocal oldFile = fileHome.findByPrimaryKey(new Long(pk));
        fileHome.create(path, tsuid, size, md5, 0, oldFile.getInstance(),
                oldFile.getFileSystem());
        oldFile.setInstance(null);
    }

    /**
     * @ejb.interface-method
     */
    public void setFileStatus(long pk, int status) throws FinderException {
        fileHome.findByPrimaryKey(new Long(pk)).setFileStatus(status);
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO addFileSystem(FileSystemDTO dto)
            throws CreateException {
        return fileSystemHome.create(dto.getDirectoryPath(),
                dto.getRetrieveAET(), dto.getAvailability(), dto.getStatus(),
                dto.getUserInfo()).toDTO();
    }

    /**
     * @ejb.interface-method
     */
    public void updateFileSystem(FileSystemDTO dto) throws FinderException {
        FileSystemLocal fs = (dto.getPk() == -1) ? fileSystemHome
                .findByDirectoryPath(dto.getDirectoryPath()) : fileSystemHome
                .findByPrimaryKey(new Long(dto.getPk()));
        fs.fromDTO(dto);
    }
    
    /**
     * @ejb.interface-method
     */
    public void updateFileSystemStatus(String dirPath, int status) throws FinderException {
    	FileSystemLocal fs = fileSystemHome.findByDirectoryPath(dirPath);
    	
    	fs.setStatus(status);
    }

    /**
     * @ejb.interface-method
     */
    public void updateFileSystemAvailability(String dirPath, int availability) throws FinderException {
    	FileSystemLocal fs = fileSystemHome.findByDirectoryPath(dirPath);

    	// If we set the file system to OFFLINE, we need to make sure its status should not
    	// DEF_RW.
    	if(availability >= Availability.OFFLINE && fs.getStatus() == FileSystemStatus.DEF_RW)
    		fs.setStatus(FileSystemStatus.RW);
    	
    	fs.setAvailability(availability);
    }
    
    /**
     * @ejb.interface-method
     */
    public void updateFileSystem2(FileSystemDTO fs1, FileSystemDTO fs2)
    throws FinderException {
        updateFileSystem(fs1);
        updateFileSystem(fs2);
    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public FileSystemDTO getFileSystem(String dirPath) throws FinderException {
        return fileSystemHome.findByDirectoryPath(dirPath).toDTO();
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO removeFileSystem(String dirPath) throws FinderException,
            RemoveException {    	
        FileSystemLocal fs = fileSystemHome.findByDirectoryPath(dirPath);
        FileSystemDTO dto = fs.toDTO();
        removeFileSystem(fs);
        return dto;
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO addAndLinkFileSystem(FileSystemDTO dto)
    throws FinderException, CreateException {    	
        FileSystemLocal prev0 = getRWFileSystem(dto);
        FileSystemLocal fs;
        if (prev0 == null) {
        	fs = fileSystemHome.create(dto.getDirectoryPath(),
                    dto.getRetrieveAET(), dto.getAvailability(),
                    FileSystemStatus.DEF_RW, dto.getUserInfo());
        	if (dto.getAvailability() == Availability.ONLINE) {
    	        fs.setNextFileSystem(fs);
        	}
        } else {
	        FileSystemLocal prev;
	        FileSystemLocal next = prev0;
	        do {
	        	prev = next;
	        	next = prev.getNextFileSystem();
	        } while (next != null && !next.isIdentical(prev0));
	        fs = fileSystemHome.create(dto.getDirectoryPath(),
	        		dto.getRetrieveAET(), dto.getAvailability(),
	        		dto.getStatus(), dto.getUserInfo());
	        prev.setNextFileSystem(fs);
	        fs.setNextFileSystem(next);
        }
        return fs.toDTO();
    }

    /**
     * @ejb.interface-method
     */
    public List listLinkedFileSystems(FileSystemDTO dto)
    throws FinderException {
        FileSystemLocal prev0 = getRWFileSystem(dto);
        if (prev0 == null) {
        	return Collections.EMPTY_LIST;
        }
        ArrayList list = new ArrayList();
        FileSystemLocal prev;
        FileSystemLocal next = prev0;
        do {
            list.add(next.toDTO());
        	prev = next;
        	next = prev.getNextFileSystem();
        } while (next != null && !next.isIdentical(prev0));
        return list;
    }

	private FileSystemLocal getRWFileSystem(FileSystemDTO dto)
	throws FinderException {
		Collection c = fileSystemHome.findByRetrieveAETAndAvailabilityAndStatus(
        		dto.getRetrieveAET(), dto.getAvailability(), FileSystemStatus.DEF_RW);
        if (c.isEmpty())
        	return null;
        if (c.size() > 1)
        	throw new FinderException("More than one RW+ Filesystem found for "
        			+ dto);
		return (FileSystemLocal) c.iterator().next();
	}
    
    private void removeFileSystem(FileSystemLocal fs)
    throws RemoveException, FinderException {
        if (fs.countFiles() > 0 || fs.countPrivateFiles() > 0)
            throw new RemoveException(fs.asString() + " not empty");
		FileSystemLocal next = fs.getNextFileSystem();
		if (next != null && fs.isIdentical(next)) {
			next = null;
		}
        Collection prevs = fs.getPreviousFileSystems();
        for (Iterator iter = new ArrayList(prevs).iterator(); iter.hasNext();) {
			FileSystemLocal prev = (FileSystemLocal) iter.next();			
			prev.setNextFileSystem(next);
		}
        if (fs.getStatus() == FileSystemStatus.DEF_RW && next != null
                && next.getStatus() == FileSystemStatus.RW) {
            next.setStatus(FileSystemStatus.DEF_RW);
        }
        fs.remove();
	}

    /**
     * @ejb.interface-method
     */
    public void linkFileSystems(String prev, String next)
            throws FinderException {
        FileSystemLocal prevfs = fileSystemHome.findByDirectoryPath(prev);
        FileSystemLocal nextfs = (next != null && next.length() != 0) 
                ? fileSystemHome.findByDirectoryPath(next) : null;
        prevfs.setNextFileSystem(nextfs);
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO[] getAllFileSystems() throws FinderException {
        return toDTO(fileSystemHome.findAll());
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO[] findFileSystems(String retrieveAET,
            int availability, int status) throws FinderException {
        return toDTO(fileSystemHome.findByRetrieveAETAndAvailabilityAndStatus(
                retrieveAET, availability, status));
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO[] findFileSystems2(String retrieveAET,
            int availability, int status, int alt) throws FinderException {
        return toDTO(fileSystemHome.findByRetrieveAETAndAvailabilityAndStatus2(
                retrieveAET, availability, status, alt));
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO[] findFileSystemsLikeDirectoryPath(String dirpath,
            int availability, int status) throws FinderException {
        return toDTO(fileSystemHome.findByLikeDirectoryPath(dirpath, 
                availability, status));
    }
    
        
    private FileSystemDTO[] toDTO(Collection c) {
        FileSystemDTO[] dto = new FileSystemDTO[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < dto.length; i++) {
            dto[i] = ((FileSystemLocal) it.next()).toDTO();
        }
        return dto;
    }

    /**
     * @ejb.interface-method
     */
    public void touchStudyOnFileSystem(String siud, String dirPath)
            throws FinderException, CreateException {
        try {
            sofHome.findByStudyAndFileSystem(siud, dirPath).touch();
        } catch (ObjectNotFoundException e) {
            try {
                sofHome.create(studyHome.findByStudyIuid(siud), fileSystemHome
                        .findByDirectoryPath(dirPath));
            } catch (CreateException ignore) {
            	// Check if concurrent create
                sofHome.findByStudyAndFileSystem(siud, dirPath).touch();
            }
        }
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="NotSupported"
     */
    public Map releaseStudies(String retrieveAET, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, boolean checkOnRO,
            int validFileStatus, long accessedBefore) throws IOException,
            FinderException, EJBException, RemoveException, CreateException {
        Timestamp tsBefore = new Timestamp(accessedBefore);
        log.info("Releasing studies not accessed since " + tsBefore);
        Map ians = new HashMap();
        releaseStudies(retrieveAET, ians, checkUncommited, checkOnMedia,
                checkExternal, checkOnRO, validFileStatus, Long.MAX_VALUE,
                new Timestamp(accessedBefore));
        return ians;
    }

    /**
     * Free disk space by delete oldest studies
     * 
     * The excution can be lengthy so we disable the transaction, which is fine 
     * since it instead calls <code>FileSystemMgtSupportBean</code> to release studies
     * which is transactional, i.e., release each study is a separate transaction.
     * 
     * @ejb.interface-method
     * @ejb.transaction type="NotSupported"
     */
    public Map freeDiskSpace(String retrieveAET, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, boolean checkOnRO,
            int validFileStatus, long maxSizeToDel, int deleteStudiesLimit) throws IOException,
            FinderException, EJBException, RemoveException, CreateException {
        Map ians = new HashMap();
        log.info("Free Disk Space: try to release "
                + (maxSizeToDel / 1000000.f) + "MB of DiskSpace");
        
        releaseStudies(retrieveAET, ians, checkUncommited, checkOnMedia,
                checkExternal, checkOnRO, validFileStatus, maxSizeToDel, deleteStudiesLimit);
        return ians;
    }

    /**
     * Release studies that fullfill freeDiskSpacePolicy to free disk space.
     * <p>
     * Remove old files until the <code>maxSizeToDel</code> is reached.
     * <p>
     * The real deletion is done in the purge process! This method removes only
     * the reference to the file system.
     * <DL>
     * <DD>1) Get a list of studies for all filesystems.</DD>
     * <DD>2) Dereference files of studies that fullfill the
     * freeDiskSpacePolicy.</DD>
     * </DL>
     * 
     * @param releaseAET
     *            releaseAET of filesystems.
     * @param maxSizeToDel
     *            Size that should be released.
     * @param checkUncommited
     *            Flag of freeDiskSpacePolicy: Check if storage of study was not
     *            commited.
     * @param checkOnMedia
     *            Flag of freeDiskSpacePolicy: Check if study is stored on media
     *            (offline storage).
     * @param checkExternal
     *            Flag of freeDiskSpacePolicy: Check if study is on an external
     *            AET.
     * @param listOfROFs
     *            List of filesystems int[a][b] with filesystem pk(b=0) and
     *            filestatus(b=1)
     * @param tsBefore
     *            date study have to be accessed.
     * 
     * @return Total size of released studies.
     * 
     * @throws IOException
     * @throws FinderException
     * @throws RemoveException
     * @throws EJBException
     * @throws CreateException
     */
    private long releaseStudies(String retrieveAET, Map ians,
            boolean checkUncommited, boolean checkOnMedia,
            boolean checkExternal, boolean checkOnRO, int validFileStatus,
            long maxSizeToDel, Timestamp tsBefore) throws IOException,
            FinderException, EJBException, RemoveException, CreateException {
    	
        Collection c = sofHome.findByRetrieveAETAndAccessBefore(retrieveAET, tsBefore);
        if (log.isDebugEnabled()) {
            log.debug("Release studies on filesystem(s) accessed before " + tsBefore
                    + " :" + c.size());
            log.debug(" checkUncommited: " + checkUncommited);
            log.debug(" checkOnMedia: " + checkOnMedia);
            log.debug(" checkExternal: " + checkExternal);
            log.debug(" checkCopyAvailable: " + checkOnRO);
            if(maxSizeToDel != Long.MAX_VALUE)
            	log.debug(" maxSizeToDel: " + maxSizeToDel);
        }
        long sizeToDelete = 0L;
        for (Iterator iter = c.iterator(); iter.hasNext()
                && sizeToDelete < maxSizeToDel;) {
            StudyOnFileSystemLocal studyOnFs = (StudyOnFileSystemLocal) iter
                    .next();
            
            sizeToDelete += releaseStudy(studyOnFs, ians,
                    checkUncommited, checkOnMedia, checkExternal,
                    checkOnRO, validFileStatus);
        }

        log.info("Released " + (sizeToDelete / 1000000.f) + "MB of DiskSpace");
        return sizeToDelete;
    }
    
    /**
     * Release studies that fullfill freeDiskSpacePolicy to free disk space.
     * <p>
     * Remove oldest files until the <code>maxSizeToDel</code> is reached.
     * <DL>
     * <DD>1) Get a list of studies for all filesystems.</DD>
     * <DD>2) Dereference files of studies that fullfill the
     * freeDiskSpacePolicy.</DD>
     * </DL>
     * 
     * @param releaseAET
     *            releaseAET of filesystems.
     * @param ians
     *            store IAN information returned            
     * @param checkUncommited
     *            Flag of freeDiskSpacePolicy: Check if storage of study was not
     *            commited.
     * @param checkOnMedia
     *            Flag of freeDiskSpacePolicy: Check if study is stored on media
     *            (offline storage).
     * @param checkExternal
     *            Flag of freeDiskSpacePolicy: Check if study is on an external
     *            AET.
     * @param checkOnRO
     *            List of filesystems int[a][b] with filesystem pk(b=0) and
     *            filestatus(b=1)
     * @param validFileStatus
     *            validate file status
     * @param maxSizeToDel
     *            maximum size to delete in order to free            
     * @param deleteStudiesLimit
     *            the number of studies fetched from database per query
     * 
     * @return Total size of released studies.
     * 
     * @throws IOException
     * @throws FinderException
     * @throws RemoveException
     * @throws EJBException
     * @throws CreateException
     */
    private long releaseStudies(String retrieveAET, Map ians,
            boolean checkUncommited, boolean checkOnMedia,
            boolean checkExternal, boolean checkOnRO, int validFileStatus,
            long maxSizeToDel, int deleteStudiesLimit ) throws IOException,
            FinderException, EJBException, RemoveException, CreateException {
        if (log.isDebugEnabled()) {
        	log.debug("Release oldest studies on " + retrieveAET);
            log.debug(" checkUncommited: " + checkUncommited);
            log.debug(" checkOnMedia: " + checkOnMedia);
            log.debug(" checkExternal: " + checkExternal);
            log.debug(" checkCopyAvailable: " + checkOnRO);
            log.debug(" maxSizeToDel: " + maxSizeToDel);
            log.debug(" deleteStudyBatchSize: " + deleteStudiesLimit);
        }

        // Studies that can't be deleted because they dont' meet criteria
        int notDeleted = 0;
        // Total file size that has been deleted
        long sizeDeleted = 0L;
        for(;sizeDeleted < maxSizeToDel;) {        	
        	// For those studies that can't be deleted, unfortunately they get selected again for subsequent
        	// batch, therefore, in order to retrieve more studies to delete, we have to increase the batch size
        	int thisBatchSize = deleteStudiesLimit + notDeleted;
        	
	        Collection c = sofHome.findByRetrieveAETAndOldest(retrieveAET, thisBatchSize);
	        if(c.size() == notDeleted)
	        	break;
	        
	        if(log.isDebugEnabled())
	        	log.debug("Retrieved the oldest studies on filesystem(s): " + c.size());
	        
	        notDeleted = 0;
	        for (Iterator iter = c.iterator(); iter.hasNext()
	                && sizeDeleted < maxSizeToDel;) {
	            StudyOnFileSystemLocal studyOnFs = (StudyOnFileSystemLocal) iter.next();
	            long size = releaseStudy(studyOnFs, ians,
	                    checkUncommited, checkOnMedia, checkExternal,
	                    checkOnRO, validFileStatus);
	            if(size != 0)
	            	sizeDeleted += size;
	            else
	            	notDeleted++;
	        }
        }
        log.info("Released " + (sizeDeleted / 1000000.f) + "MB of DiskSpace");
        return sizeDeleted;
    }
    
    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public long releaseStudy(StudyOnFileSystemLocal studyOnFs, Map ians, boolean deleteUncommited, boolean flushOnMedia,
            boolean flushExternal, boolean flushOnROFs, int validFileStatus) throws EJBException, RemoveException,
            FinderException {
        long size = 0L;
        Dataset ian = null;

        if ( Thread.interrupted() ) {
        	log.warn("Interrupted state cleared for current thread!");
        }
        
        String studyOnFsStr = log.isInfoEnabled() ? studyOnFs.asString() : null; // For performance            	
        StudyLocal study = studyOnFs.getStudy();
        boolean release = flushExternal && study.isStudyExternalRetrievable()
                || flushOnMedia && study.isStudyAvailableOnMedia()
                || flushOnROFs && study.isStudyAvailableOnROFs(validFileStatus);

        if ( release || deleteUncommited && study.getNumberOfCommitedInstances() == 0 ) {
        	ian = DcmObjectFactory.getInstance().newDataset();
        	ians.put( study.getStudyIuid(), ian);
	        final PatientLocal patient = study.getPatient();
	        Dataset patAttrs = patient.getAttributes(false);
	        ian.putAll(patAttrs.subSet(IAN_PAT_TAGS));
	        ian.putSH(Tags.StudyID,study.getStudyId());
	        ian.putUI(Tags.StudyInstanceUID,study.getStudyIuid());
	        DcmElement ppsSeq = ian.putSQ(Tags.RefPPSSeq);//We dont need this information (if available) at this point.
	        DcmElement refSerSeq = ian.putSQ(Tags.RefSeriesSeq);
        
            Collection c = studyOnFs.getFiles();
            if ( log.isDebugEnabled() ) 
            	log.debug( "Release "+c.size()+" files from "+studyOnFsStr );
            FileLocal fileLocal;
            InstanceLocal il;
            Map seriesLocals = new HashMap();
            Map seriesSopSeq = new HashMap();
            SeriesLocal sl;
            DcmElement refSopSeq;
            String fsPath = studyOnFs.getFileSystem().getDirectoryPath();            
            ArrayList filesToPurge = new ArrayList();            
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                fileLocal = (FileLocal) iter.next();
                if (log.isDebugEnabled())
                    log.debug("Release File:" + fileLocal.asString());
                size += fileLocal.getFileSize();
                il = fileLocal.getInstance();
                sl = il.getSeries();
                if ( ! seriesLocals.containsKey(sl.getPk()) ) {
                	seriesLocals.put(sl.getPk(), sl);
                	Dataset ds = refSerSeq.addNewItem();
                	ds.putUI(Tags.SeriesInstanceUID, sl.getSeriesIuid());
                	seriesSopSeq.put(sl.getPk(), refSopSeq = ds.putSQ(Tags.RefSOPSeq));
                } else {
                	refSopSeq = (DcmElement) seriesSopSeq.get( sl.getPk() );
                }
                Dataset refSOP = refSopSeq.addNewItem();
                refSOP.putAE(Tags.RetrieveAET, StringUtils.split(il.getRetrieveAETs(),'\\') );
                refSOP.putUI(Tags.RefSOPClassUID, il.getSopCuid());
                refSOP.putUI(Tags.RefSOPInstanceUID, il.getSopIuid());

                if ( release ) {
                	// Add this file to purge list
                	filesToPurge.add(fsPath + '/' + fileLocal.getFilePath());
                	
                	// Delete the file record from database
                	fileLocal.remove();
                	
                	il.updateDerivedFields(true, true);
                	int avail = il.getAvailabilitySafe();
                    refSOP.putCS(Tags.InstanceAvailability, Availability.toString(avail));
                    if ( avail == Availability.OFFLINE ) {
                    	refSOP.putSH(Tags.StorageMediaFileSetID, il.getMedia().getFilesetId());
                    	refSOP.putUI(Tags.StorageMediaFileSetUID, il.getMedia().getFilesetIuid());
                    }
                } else {
                    refSOP.putCS(Tags.InstanceAvailability, Availability.toString(Availability.UNAVAILABLE));
                }
            }
            if (release) {
            	for (Iterator iter = seriesLocals.values().iterator(); iter.hasNext();) {
            		final SeriesLocal ser = (SeriesLocal) iter.next();
            		ser.updateDerivedFields(false, true, false, false, true);
            	}
            	study.updateDerivedFields(false, true, false, false, true, false);
            	if(log.isInfoEnabled())
            		log.info("Release Files of " + studyOnFsStr + " - "
            				+ (size / 1000000.f) + "MB");
            	studyOnFs.remove();
            	
                //
                // use a purge delegate to delete list of files
                //
                try {            
                	sendDeleteFilesMsg(filesToPurge);
                }
            	catch(Exception e)
        		{
        			throw new EJBException("Failed to send JMS message to purge files on FileSystem: " + fsPath, e);
        		}
			} else {
				if(log.isInfoEnabled())
					log.info("Delete " + studyOnFsStr + " - " + (size / 1000000.f) + "MB");
				
				// Cascade-delete the study		
				study.remove();
			}            
        }
        else
        {
        	log.debug("Study ["+study.getStudyIuid()+"] can not be deleted!");
        }
        return size;
    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public long getStudySize(Long studyPk) throws FinderException {
        return studyHome.selectStudySize(studyPk);
    }
    
    private void sendDeleteFilesMsg(ArrayList filePaths) throws Exception
    {
    	JmsSenderLocal jmsSender = null;
		try
		{
			jmsSender = jmsSenderHome.create();

			jmsSender.send(new ActionOrder("purgeFiles", filePaths), purgeFileQueueName);
		}		
		finally
		{
			try {
				jmsSender.remove();
			}
			catch(Exception e)
			{
				// ignore
			}
		}
    }
    
}