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

package org.dcm4chex.archive.mbean;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DataSource;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.common.FileSystemStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.notif.StudyDeleted;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileDataSource;
import org.dcm4chex.archive.util.FileSystemUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 12.09.2004
 *
 */
public class FileSystemMgtService extends ServiceMBeanSupport {

    private final TimerSupport timer = new TimerSupport(this);

    private static final long MIN_FREE_DISK_SPACE = 20 * FileUtils.MEGA;    
    
    private long minFreeDiskSpace = MIN_FREE_DISK_SPACE;
    
    private long checkFreeDiskSpaceInterval = 60000L;

    private float checkFreeDiskSpaceThreshold = 5f;
    
    private String retrieveAET = "DCM4JBOSS";
    
    private String defStorageDir = "archive";

	private float freeDiskSpaceLowerThreshold = 1.5f;
	
	private float freeDiskSpaceUpperThreshold = 2.5f;
	
	private boolean flushExternalRetrievable = false;
	
	private boolean flushOnMedia = false;
	
	private boolean flushOnROFsAvailable = false;
    
	private int validFileStatus = 0;
	
	private boolean deleteUncommited = false;
	
	private long studyCacheTimeout = 0L;
    
    private long purgeFilesInterval = 0L;

	private int limitNumberOfFilesPerTask = 1000;
    
    private long freeDiskSpaceInterval = 0L;
    
    private Integer purgeFilesListenerID;

    private Integer freeDiskSpaceListenerID;
    
    private boolean autoPurge = true;
    
    private boolean freeDiskSpaceOnDemand = true;

	private boolean isPurging = false;
	
	private int bufferSize = 8192;
    
    private String mountFailedCheckFile = "NO_MOUNT";
    
    private boolean makeStorageDirectory = true;

    private FileSystemDTO storageFileSystem;
    
    private long checkStorageFileSystem = 0L;
	        
    private final NotificationListener purgeFilesListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                purgeFiles();
            }};

    private final NotificationListener freeDiskSpaceListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                freeDiskSpace();
            }};

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final boolean isMakeStorageDirectory() {
        return makeStorageDirectory;
    }

    public final void setMakeStorageDirectory(boolean makeStorageDirectory) {
        this.makeStorageDirectory = makeStorageDirectory;
    }

    public final String getMountFailedCheckFile() {
        return mountFailedCheckFile;
    }

    public final void setMountFailedCheckFile(String mountFailedCheckFile) {
        this.mountFailedCheckFile = mountFailedCheckFile;
    }
    
    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
        
    public final String getRetrieveAET() {
        return retrieveAET;
    }

    /**
     * Set Retrieve AE title associated with this DICOM Node. 
     * There must be at least one configured file system suitable for storage 
     * (ONLINE, RW) associated with this AE title.
     * 
     * @param aet The AE Title to set.
     */
    public final void setRetrieveAET(String aet) 
    throws FinderException, IOException {
        this.retrieveAET = aet;
        this.storageFileSystem = null;
    }

	public final String getDefStorageDir() {
        return defStorageDir;
    }

    public final void setDefStorageDir(String defStorageDir) {
        this.defStorageDir = defStorageDir;
    }

    public final String getMinFreeDiskSpace() {
        return FileUtils.formatSize(minFreeDiskSpace);
    }

    public final void setMinFreeDiskSpace(String str) {
        this.minFreeDiskSpace = FileUtils.parseSize(str, MIN_FREE_DISK_SPACE);
    }

	public final String getCheckFreeDiskSpaceInterval() {
        return RetryIntervalls.formatInterval(checkFreeDiskSpaceInterval);
    }

    public final void setCheckFreeDiskSpaceInterval(String s) {
        this.checkFreeDiskSpaceInterval = RetryIntervalls.parseInterval(s);
    }

    public final float getCheckFreeDiskSpaceThreshold() {
        return checkFreeDiskSpaceThreshold;
    }

    public final void setCheckFreeDiskSpaceThreshold(float threshold) {
        if ( threshold < 1.0f ) throw new IllegalArgumentException("CheckFreeDiskSpaceThreshold must NOT be smaller than 1!");
        this.checkFreeDiskSpaceThreshold = threshold;
    }

    /**
	 * Return the factor to calculate watermark for free disk space process.
	 * <p>
	 * The threshold for freeDiskSpace process is calculated: <code>minFreeDiskSpace * freeDiskSpaceLowerThreshold * numberOfFilesystems</code>
	 * 
	 * @return Returns the cleanWaterMarkFactor.
	 */
	public float getFreeDiskSpaceLowerThreshold() {
		return freeDiskSpaceLowerThreshold;
	}
	/**
	 * Set the factor to calculate watermark for free disk space process.
	 * @param freeDiskSpaceLowerThreshold The freeDiskSpaceLowerThreshold to set.
	 */
	public void setFreeDiskSpaceLowerThreshold(float threshold) {
		if ( threshold < 1.0f ) throw new IllegalArgumentException("FreeDiskSpaceLowerThreshold must NOT be smaller than 1!");
		this.freeDiskSpaceLowerThreshold = threshold;
	}
	
	/**
	 * Returns the factor to calculate the watermark to stop free disk space process.
	 * <p>
	 * The threshold to stop freeDiskSpace process is calculated: <code>minFreeDiskSpace * freeDiskSpaceUpperThreshold * numberOfFileSytsems</code>
	 * 
	 * @return Returns the stopCleanWaterMarkFactor.
	 */
	public float getFreeDiskSpaceUpperThreshold() {
		return freeDiskSpaceUpperThreshold;
	}
	/**
	 * Set the factor to calculate the watermark to stop free disk space process.
	 * <p>
	 * The watermark to stop freeDiskSpace process is calculated: <code>minFreeDiskSpace * freeDiskSpaceUpperThreshold * numberOfFileSytsems</code>
	 * 
	 * @param stopCleanWaterMarkFactor The stopCleanWaterMarkFactor to set.
	 */
	public void setFreeDiskSpaceUpperThreshold(float threshold) {
		if ( threshold < freeDiskSpaceLowerThreshold ) throw new IllegalArgumentException("FreeDiskSpaceUpperThreshold must be higher than FreeDiskSpaceLowerThreshold");
		this.freeDiskSpaceUpperThreshold = threshold;
	}

	/**
	 * Returns true if the freeDiskSpace policy flushExternalRetrievable is enabled.
	 * <p>
	 * If this policy is active studies must be external retrievable for deletion.
	 * 
	 * @return Returns true if flushExternalRetrievable policy is active.
	 */
	public boolean isFlushStudiesExternalRetrievable() {
		return flushExternalRetrievable;
	}
	/**
	 * Set the freeDiskSpace policy flushExternalRetrievable.
	 * <p>
	 * Set this policy active if studies must be external retrievable for deletion.
	 * 
	 * @param b The flushExternalRetrievable to set.
	 */
	public void setFlushStudiesExternalRetrievable(boolean b) {
		this.flushExternalRetrievable = b;
	}
	/**
	 * Returns true if the freeDiskSpace policy deleteUncommited is enabled.
	 * <p>
	 * If this policy is active studies are deleted immedatly without any check.
	 * 
	 * @return Returns true if deleteUncommited is active.
	 */
	public boolean isDeleteStudiesStorageNotCommited() {
		return deleteUncommited;
	}
	
	/**
	 * Set the freeDiskSpace policy deleteUncommited.
	 * <p>
	 * If this policy is active studies are deleted immedatly without any check.
	 *
	 * @param b The deleteUncommited to set.
	 */
	public void setDeleteStudiesStorageNotCommited(boolean b) {
		deleteUncommited = b;
	}
	
	/**
	 * Returns true if the freeDiskSpace policy flushOnMedia is enabled.
	 * <p>
	 * If this policy is active studies must be stored on media (offline storage) for deletion.
	 * 
	 * @return Returns true if flushOnMedia policy is active.
	 */
	public boolean isFlushStudiesOnMedia() {
		return flushOnMedia;
	}
	
	/**
	 * Set the freeDiskSpace policy flushOnMedia.
	 * <p>
	 * Set this policy active if studies must be on media (offline storage) for deletion.
	 * 
	 * @param b The flushOnMedia to set.
	 */
	public void setFlushStudiesOnMedia(boolean b) {
		this.flushOnMedia = b;
	}
	
	/**
	 * @return Returns the flushOnHSM.
	 */
	public boolean isFlushOnROFsAvailable() {
		return flushOnROFsAvailable;
	}
	/**
	 * Set the freeDiskSpace policy flushOnHSM.
	 * <p>
	 * Set this policy active if studies must be on media (offline storage) for deletion.
	 * 
	 * @param flushOnROAvailable The flushOnHSM to set.
	 */
	public void setFlushOnROFsAvailable(boolean flushOnROAvailable) {
		this.flushOnROFsAvailable = flushOnROAvailable;
	}
	/**
	 * @return Returns the validFileStatus.
	 */
	public String getValidFileStatus() {
		return FileStatus.toString(validFileStatus);
	}
	/**
	 * @param validFileStatus The validFileStatus to set.
	 */
	public void setValidFileStatus(String validFileStatus) {
		this.validFileStatus = FileStatus.toInt(validFileStatus);
	}
	/**
	 * Return string representation 
	 * 
	 * @return Returns the StudyCacheTimeout.
	 */
	public String getStudyCacheTimeout() {
        return RetryIntervalls.formatIntervalZeroAsNever(studyCacheTimeout);
	}
	
	/**
	 * Set number of days a study is not accessed for freeDiskSpace.
	 * 
	 * @param StudyCacheTimeoutDays The StudyCacheTimeoutDays to set in days.
	 */
	public void setStudyCacheTimeout(String interval) {
        this.studyCacheTimeout = RetryIntervalls.parseIntervalOrNever(interval);
	}
	
    public final String getFreeDiskSpaceInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(freeDiskSpaceInterval);
    }
    
    public void setFreeDiskSpaceInterval(String interval) {
        this.freeDiskSpaceInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            timer.stopScheduler("CheckFreeDiskSpace", freeDiskSpaceListenerID,
            		freeDiskSpaceListener);
            freeDiskSpaceListenerID = timer.startScheduler("CheckFreeDiskSpace",
            		freeDiskSpaceInterval, freeDiskSpaceListener);
        }
    }
    
	/**
	 * @return Returns the freeDiskSpaceOnDemand.
	 */
	public boolean isFreeDiskSpaceOnDemand() {
		return freeDiskSpaceOnDemand;
	}
	/**
	 * @param freeDiskSpaceOnDemand The freeDiskSpaceOnDemand to set.
	 */
	public void setFreeDiskSpaceOnDemand(boolean freeDiskSpaceOnDemand) {
		this.freeDiskSpaceOnDemand = freeDiskSpaceOnDemand;
	}
	/**
	 * @return Returns the autoPurge.
	 */
	public boolean isPurgeFilesAfterFreeDiskSpace() {
		return autoPurge;
	}
	/**
	 * @param autoPurge The autoPurge to set.
	 */
	public void setPurgeFilesAfterFreeDiskSpace(boolean autoPurge) {
		this.autoPurge = autoPurge;
	}
    
    public final String getPurgeFilesInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(purgeFilesInterval);
    }
    
    public void setPurgeFilesInterval(String interval) {
        this.purgeFilesInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            timer.stopScheduler("CheckFilesToPurge", purgeFilesListenerID,
            		purgeFilesListener);
            purgeFilesListenerID = timer.startScheduler("CheckFilesToPurge",
            		purgeFilesInterval, purgeFilesListener);
        }
    }
    
    public final int getLimitNumberOfFilesPerTask() {
    	return limitNumberOfFilesPerTask;
    }
    
    public void setLimitNumberOfFilesPerTask( int limit ) {
    	limitNumberOfFilesPerTask = limit;
    }
    
    protected void startService() throws Exception {
         timer.init();
         freeDiskSpaceListenerID = timer.startScheduler("CheckFreeDiskSpace",
         		freeDiskSpaceInterval, freeDiskSpaceListener);
         purgeFilesListenerID = timer.startScheduler("CheckFilesToPurge",
         		purgeFilesInterval, purgeFilesListener);
         
    }
    
    private void initStorageFileSystem() throws Exception {
        FileSystemMgt fsmgt = newFileSystemMgt();
        FileSystemDTO[] c = fsmgt.findFileSystems(retrieveAET, 
                Availability.ONLINE, FileSystemStatus.DEF_RW);
        if (c.length > 0) {
            storageFileSystem = c[0];
        } else {
            c = fsmgt.findFileSystems(retrieveAET, 
                    Availability.ONLINE, FileSystemStatus.RW);
            if (c.length > 0) {
                storageFileSystem = c[0];
                storageFileSystem.setStatus(FileSystemStatus.DEF_RW);
                fsmgt.updateFileSystem(storageFileSystem);
            } else {
                storageFileSystem = addFileSystem(defStorageDir, retrieveAET,
                        Availability.ONLINE, FileSystemStatus.DEF_RW, null);
                log.warn("No writeable Storage Directory configured for retrieve AET " +
                        retrieveAET + "- initalize default " + storageFileSystem);
            }
        }
        checkStorageFileSystem = 0;
    }

    public FileSystemDTO selectStorageFileSystem() throws Exception {
        if (storageFileSystem == null) {
            initStorageFileSystem();
        }
        if (checkStorageFileSystem == 0L
                || checkStorageFileSystem < System.currentTimeMillis())
            if (!checkStorageFileSystem(storageFileSystem))
                if (!switchStorageFileSystem(storageFileSystem))
                    return null;
        return storageFileSystem;
    }

    private synchronized boolean switchStorageFileSystem(FileSystemDTO fsDTO)
            throws RemoteException, FinderException, IOException {
        if (storageFileSystem != fsDTO)
            return true; // already switched by another thread
        String dirPath0 = fsDTO.getDirectoryPath();
        FileSystemMgt fsmgt = newFileSystemMgt();
        do {
            String dirPath = fsDTO.getNext();
            if (dirPath == null || dirPath.equals(dirPath0)) {
                log.error("High Water Mark reached on storage directory " 
                        + FileUtils.toFile(dirPath0)
                        + " - no alternative storage directory available");
                return false;
            }
            fsDTO = fsmgt.getFileSystem(dirPath);
        } while (!checkStorageFileSystem(fsDTO));
        storageFileSystem.setStatus(FileSystemStatus.RW);
        fsDTO.setStatus(FileSystemStatus.DEF_RW);
        fsmgt.updateFileSystem2(storageFileSystem, fsDTO);
        storageFileSystem = fsDTO;
        return true;
    }

    private boolean checkStorageFileSystem(FileSystemDTO fsDTO) throws IOException {
        int availability = fsDTO.getAvailability();
        int status = fsDTO.getStatus();
        if (availability != Availability.ONLINE 
                || status != FileSystemStatus.RW 
                && status != FileSystemStatus.DEF_RW) {
            log.info("" + fsDTO + " no longer available for storage" +
                    " - try to switch to next configured storage directory");
            return false;
        }
        File dir = FileUtils.toFile(fsDTO.getDirectoryPath());
        if (!dir.exists()) {
            if (!makeStorageDirectory) {
                log.warn("No such directory " + dir
                        + " - try to switch to next configured storage directory");
                return false;
            }
            log.info("M-WRITE " + dir);
            if (!dir.mkdirs()) {
                log.warn("Failed to create directory " + dir
                        + " - try to switch to next configured storage directory");
                return false;
            }
        }
        File nomount = new File(dir, mountFailedCheckFile);
        if (nomount.exists()) {
            log.warn("Mount on " + dir
                 + " seems broken - try to switch to next configured storage directory");
            return false;
        }
        final long freeSpace = FileSystemUtils.freeSpace(dir.getPath());
        log.info("Free disk space on " + dir + ": " + FileUtils.formatSize(freeSpace));
        if (freeSpace < minFreeDiskSpace) {
            log.info("High Water Mark reached on current storage directory "
                    + dir + " - try to switch to next configured storage directory");
            return false;
        }
        checkStorageFileSystem = 
            (freeSpace > minFreeDiskSpace * checkFreeDiskSpaceThreshold)
                ? (System.currentTimeMillis() + checkFreeDiskSpaceInterval)
                : 0L;
        return true;
    }
    
    protected void stopService() throws Exception {
        timer.stopScheduler("CheckFreeDiskSpace", freeDiskSpaceListenerID,
        		freeDiskSpaceListener);
        timer.stopScheduler("CheckFilesToPurge", purgeFilesListenerID,
        		purgeFilesListener);
        super.stopService();
    }

    
    private FileSystemMgt newFileSystemMgt() {
        try {
            FileSystemMgtHome home = (FileSystemMgtHome) EJBHomeFactory
                    .getFactory().lookup(FileSystemMgtHome.class,
                            FileSystemMgtHome.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access File System Mgt EJB:",
                    e);
        }
    }

    public String showAvailableDiskSpace() throws IOException, FinderException {
        return FileUtils.formatSize(
                getAvailableDiskSpace(listLocalOnlineRWFileSystems()));
    }
    
    public int purgeFiles() {
        log.info("Check for unreferenced files to delete");
        synchronized (this) {
            if (isPurging) {
                log.info("A purge task is already in progress! Ignore this purge order!");
                return 0;
            }
            isPurging = true;
        }
        FileSystemMgt fsMgt = newFileSystemMgt();
        int deleted, total = 0;
        FileSystemDTO[] list;
        try {
            list = listLocalOnlineRWFileSystems(fsMgt);
        } catch (Exception e) {
            log.error("Failed to query DB for file system configuration:", e);
            return 0;
        }
        for (int i = 0; i < list.length; ++i) {
            deleted = purgeFiles(list[i].getDirectoryPath(), fsMgt);
            if (deleted < 0)
                break;
            total += deleted;
            if (total >= this.getLimitNumberOfFilesPerTask())
                break;
        }
        isPurging = false;
        return total;
    }
    
    public int purgeFiles( String purgeDirPath ) {
    	int total;
    	if ( purgeDirPath == null ) {
    		total = purgeFiles();
    	} else {
            synchronized (this) {  
    	        if ( isPurging ) {
    	        	log.info("A purge task is already in progress! Ignore this purge order!");
    	        	return 0;
    	        }
    	        isPurging = true;
            }
            log.info("Check for unreferenced files to delete in filesystem:"+purgeDirPath);
		    FileSystemMgt fsMgt = newFileSystemMgt();
			total = purgeFiles(purgeDirPath,fsMgt);
			isPurging = false;
		    try {
		        fsMgt.remove();
		    } catch (Exception ignore) {
		    }
    	}
    	return total;
    }
    
    private int purgeFiles( String path, FileSystemMgt fsMgt ) {
    	int limit = getLimitNumberOfFilesPerTask();
    	int deleted = purgeFiles( path, fsMgt, false, limit );
    	if ( deleted < 0 ) return -1;//mark error
    	int total = deleted;
    	if ( total < limit ) { //try also in trash (PrivateFiles) for remaining number of files per task
    		deleted = purgeFiles( path, fsMgt, true, limit - total );
    		if ( deleted > 0 ) {
    			total += deleted;
    		}
    	}
    	return total;
    }
    
    private int purgeFiles( String purgeDirPath, FileSystemMgt fsMgt, boolean fromPrivate, int limit ) {
        FileDTO[] toDelete;
    	try {
    		toDelete = fromPrivate ? fsMgt.getDereferencedPrivateFiles( purgeDirPath, limit ) :
    					fsMgt.getDereferencedFiles( purgeDirPath, limit );
            if ( log.isDebugEnabled()) 
            	log.debug("purgeFiles: found "+toDelete.length+" files to delete on dirPath:"+purgeDirPath);
        } catch (Exception e) {
            log.warn("Failed to query dereferenced files:", e);
            return -1;
        }
        for (int j = 0; j < toDelete.length; j++) {
            FileDTO fileDTO = toDelete[j];
			File file = FileUtils.toFile(fileDTO.getDirectoryPath(),
                    fileDTO.getFilePath());
            try {
            	if ( fromPrivate ) {
                    fsMgt.deletePrivateFile(fileDTO.getPk());
            	} else {
            		fsMgt.deleteFile(fileDTO.getPk());
            	}
            } catch (Exception e) {
                log.warn("Failed to remove File Record[pk=" 
						+ fileDTO.getPk() + "] from DB:", e);
                log.info("-> Keep dereferenced file: " + file);
				continue;
            }
            delete(file);
        }
        return toDelete.length;
    }

    private boolean delete(File file) {
        log.info("M-DELETE file: " + file);
        if (!file.exists()) {
            log.warn("File: " + file + " was already deleted");
            return true;
        }
        if (!file.delete()) {
            log.warn("Failed to delete file: " + file);
            return false;
        }
        // purge empty series and study directory
        File seriesDir = file.getParentFile();
        if (seriesDir.delete()) {
            seriesDir.getParentFile().delete();
        }
        return true;
    }
    
    public Object locateInstance(String iuid) throws SQLException {
        List list = new QueryFilesCmd(iuid).getFileDTOs();
        if (list.isEmpty())
            return null;
        for (int i = 0, n = list.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) list.get(i);
            if (retrieveAET.equals(dto.getRetrieveAET()))
                return FileUtils.toFile(dto.getDirectoryPath(), dto.getFilePath());
        }
        FileDTO dto = (FileDTO) list.get(0);
        AEData aeData = new AECmd(dto.getRetrieveAET()).getAEData();
        return aeData.getHostName();
    }
    
    public DataSource getDatasourceOfInstance(String iuid) throws SQLException {
    	Dataset dsQ = DcmObjectFactory.getInstance().newDataset();
    	dsQ.putUI(Tags.SOPInstanceUID, iuid);
    	dsQ.putCS(Tags.QueryRetrieveLevel, "IMAGE");
    	RetrieveCmd retrieveCmd = RetrieveCmd.create(dsQ);
    	FileInfo infoList[][] = retrieveCmd.getFileInfos();
    	if (infoList.length == 0)
    		return null;
    	FileInfo[] fileInfos = infoList[0];
        for (int i = 0; i < fileInfos.length; ++i) {
            final FileInfo info = fileInfos[i];
            if (retrieveAET.equals(info.fileRetrieveAET))
            {
                File f = FileUtils.toFile(info.basedir, info.fileID);
                Dataset mergeAttrs = DatasetUtils.fromByteArray(info.patAttrs,
                        DatasetUtils.fromByteArray(info.studyAttrs,
                                DatasetUtils.fromByteArray(info.seriesAttrs,
                                        DatasetUtils.fromByteArray(info.instAttrs))));
                FileDataSource ds = new FileDataSource(f, mergeAttrs, new byte[bufferSize]);
            	ds.setWriteFile(true);//write FileMetaInfo!
            	return ds;
            }
        }
        return null;
//        String aet = fileInfos[0].fileRetrieveAET;
//        if (aet == null)
//        	aet = fileInfos[0].extRetrieveAET;
//        AEData aeData = new AECmd(fileInfos[0].).getAEData();
//        return aeData.getHostName();
    }
    
  
	/**
     * Delete studies that fullfill freeDiskSpacePolicy to free disk space.
     * <p>
     * Checks available disk space if free disk space is necessary.
     * <p>
     * Remove old files until the stopFreeDiskSpaceWatermark is reached.
     * <p>
     * The real deletion is done in the purge process! This method removes only the reference to the file system.  
     * <p>
     * If PurgeFilesAfterFreeDiskSpace is enabled, the purge process will be called immediately.
     * 
     * @return The number of released studies.
     */
    public int freeDiskSpace() {
        log.info("Check available Disk Space");
        try {
            FileSystemDTO[] fs = listLocalOnlineRWFileSystems();
            long available = getAvailableDiskSpace(fs);
            if (available < (minFreeDiskSpace * freeDiskSpaceLowerThreshold * fs.length)) {
                long maxSizeToDel = (long)((minFreeDiskSpace * freeDiskSpaceUpperThreshold * fs.length)) - available;
                FileSystemMgt fsMgt = newFileSystemMgt();
                try {
                    Map ians = fsMgt.freeDiskSpace(retrieveAET, deleteUncommited, flushOnMedia,
                            flushExternalRetrievable, flushOnROFsAvailable, validFileStatus,
                            maxSizeToDel);
                    sendIANs(ians);
                    if ( autoPurge ) {
                    	if ( log.isDebugEnabled() ) log.debug("call purgeFiles after freeDiskSpace");
                    	this.purgeFiles();
                    }
                    return ians.size();
                } finally {
                    fsMgt.remove();
                }            
            } else if (studyCacheTimeout > 0L) {
                long accessedBefore = System.currentTimeMillis() - studyCacheTimeout;
                FileSystemMgt fsMgt = newFileSystemMgt();
                try {
                	Map ians = fsMgt.releaseStudies(retrieveAET, deleteUncommited, flushOnMedia,
                            flushExternalRetrievable, flushOnROFsAvailable, validFileStatus, accessedBefore);
                    sendIANs(ians);
                    return ians.size();
                } finally {
                    fsMgt.remove();
                }
            } else {
                return 0;
            }
        } catch (Exception e) {
            log.error("Free Disk Space failed:", e);
            return -1;
        }
    }
    

	private void sendIANs(Map ians) {
		for (Iterator iter = ians.values().iterator(); iter.hasNext();) {
			Dataset ian = (Dataset) iter.next();
			sendJMXNotification(new StudyDeleted(ian));
		}		
	}

	void sendJMXNotification(Object o) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this, eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
	}
    
    private long getAvailableDiskSpace(FileSystemDTO[] fs)
    throws IOException, FinderException {
        long result = 0L;
        for (int i = 0; i < fs.length; i++) {
            final File dir = FileUtils.toFile(fs[i].getDirectoryPath());
            if (dir.isDirectory())
                result += FileSystemUtils.freeSpace(dir.getPath());
        }
    	return result;
    }

    public FileSystemDTO[] listLocalOnlineRWFileSystems()
            throws FinderException, RemoteException {
        return listLocalOnlineRWFileSystems(newFileSystemMgt());
    }
    
    private FileSystemDTO[] listLocalOnlineRWFileSystems(FileSystemMgt fsmgt)
            throws FinderException, RemoteException {
        return fsmgt.findFileSystems2(retrieveAET, Availability.ONLINE,
                FileSystemStatus.DEF_RW, FileSystemStatus.RW);
    }

    public long showStudySize( Integer pk ) throws RemoteException, FinderException {
        FileSystemMgt fsMgt = newFileSystemMgt();
    	return fsMgt.getStudySize(pk);
    }
    
	private static String toString(FileSystemDTO[] dtos) {
		StringBuffer sb = new StringBuffer();
		String nl = System.getProperty("line.separator", "\n");
		for (int i = 0; i < dtos.length; i++) {
			sb.append(dtos[i]).append(nl);
		}
		return sb.toString();
	}
    
    public String showAllFileSystems() throws RemoteException, FinderException {
        return toString(listAllFileSystems());    	
    }

    public FileSystemDTO[] listAllFileSystems() throws RemoteException, FinderException {
        return newFileSystemMgt().getAllFileSystems();    	
    }

    public FileSystemDTO addFileSystem(String dirPath, String retrieveAET,
            String availability, String status, String userInfo)
    throws RemoteException, CreateException {        
        return addFileSystem(dirPath, retrieveAET,
                Availability.toInt(availability),
                FileSystemStatus.toInt(status),
                userInfo);       
    }
    
    private FileSystemDTO addFileSystem(String dirPath, String retrieveAET,
    		int availability, int status, String userInfo)
    throws RemoteException, CreateException {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setDirectoryPath(dirPath);
        dto.setRetrieveAET(retrieveAET);
        dto.setAvailability(availability);
        dto.setStatus(status);
        dto.setUserInfo(userInfo);
		return newFileSystemMgt().addFileSystem(dto);    	
    }

    public void updateFileSystem(String dirPath, String retrieveAET,
            String availability, String status, String userInfo)
    throws RemoteException, FinderException {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setDirectoryPath(dirPath);
        dto.setRetrieveAET(retrieveAET);
        dto.setAvailability(Availability.toInt(availability));
        dto.setStatus(FileSystemStatus.toInt(status));
        dto.setUserInfo(userInfo);
        newFileSystemMgt().updateFileSystem(dto);      
    }
    
    public void linkFileSystems(String prev, String next)
            throws RemoteException, FinderException {
        newFileSystemMgt().linkFileSystems(prev, next);
    }

    public String addOnlineFileSystem(String dirPath, String userInfo)
    throws RemoteException, FinderException, CreateException {
        return addAndLinkFileSystem(dirPath, Availability.ONLINE,
    			FileSystemStatus.RW, userInfo);
    }

    public String showOnlineFileSystems()
    throws RemoteException, FinderException {
    	return showFileSystems(Availability.ONLINE);
    }

    public String addNearlineFileSystem(String dirPath, String userInfo)
    throws RemoteException, FinderException, CreateException {
    	return addAndLinkFileSystem(dirPath, Availability.NEARLINE,
    			FileSystemStatus.RW, userInfo);
    }

    public String showNearlineFileSystems()
    throws RemoteException, FinderException {
    	return showFileSystems(Availability.NEARLINE);
    }

    public String removeFileSystem(String dirPath)
    throws RemoteException, FinderException, RemoveException {
    	return newFileSystemMgt().removeFileSystem(dirPath).toString();
    }
    
    private String showFileSystems(int availability)
    throws RemoteException, FinderException {
    	FileSystemDTO dto = new FileSystemDTO();
    	dto.setRetrieveAET(retrieveAET);
		dto.setAvailability(availability);
    	List l = newFileSystemMgt().listLinkedFileSystems(dto);
    	StringBuffer sb = new StringBuffer();
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			sb.append(iter.next()).append('\n');			
		}
		return sb.toString();
    }

	private String addAndLinkFileSystem(String dirPath, int availability,
			int status, String userInfo) throws FinderException, 
			CreateException, RemoteException {
    	FileSystemDTO dto = new FileSystemDTO();
    	dto.setDirectoryPath(dirPath);
    	dto.setRetrieveAET(retrieveAET);
		dto.setAvailability(availability);
    	dto.setStatus(status);
    	dto.setUserInfo(userInfo);
    	return newFileSystemMgt().addAndLinkFileSystem(dto).toString();
	}
}