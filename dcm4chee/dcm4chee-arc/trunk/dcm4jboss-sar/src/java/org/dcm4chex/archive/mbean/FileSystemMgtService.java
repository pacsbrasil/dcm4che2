/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 12.09.2004
 *
 */
public class FileSystemMgtService extends ServiceMBeanSupport {

    private static final long MIN_FREE_DISK_SPACE = 20 * FileUtils.MEGA;    
    private static final String LOCAL = "local";
    private static final String CHECK_STRING_ONMEDIA = "ON_MEDIA";
    private static final String CHECK_STRING_EXTERNAL = "EXTERNAL";
    private static final String CHECK_STRING_NOTHING = "DONT_CHECK";

	/** Milliseconds of one day. Is used to calculate search date. */
	private static final long ONE_DAY_IN_MILLIS = 86400000;// one day has 86400000 milli seconds

    private long minFreeDiskSpace = MIN_FREE_DISK_SPACE;

    private List dirPathList = Arrays.asList(new File[] { new File("archive")});

    private Set fsPathSet = Collections.singleton("archive");

    private List rodirPathList = Collections.EMPTY_LIST;

    private Set rofsPathSet = Collections.EMPTY_SET;

    private String retrieveAET = "QR_SCP";

    private int curDirIndex = 0;
    
    private String mountFailedCheckFile = "NO_MOUNT";

    private boolean makeStorageDirectory = true;

	private float freeDiskSpaceLowerThreshold = 1.5f;
	
	private float freeDiskSpaceUpperThreshold = 2.5f;
	
	private boolean flushStudiesExternalRetrievable = false;
	
	private boolean flushStudiesOnMedia = true;
	
	private boolean deleteStudiesUnconditional = false;
	
	private int flushStudiesNotAccessedForDays = 3560;
	
	/** holds available disk space over all file systems. this value is set in getAvailableDiskspace ( and isFreeDiskSpaceNecessary ). */
	private long availableDiskSpace = 0L;

    private static String null2local(String s) {
        return s == null ? LOCAL : s;
    }

    private static String local2null(String s) {
        return LOCAL.equals(s) ? null : s;
    }

    public String getEjbProviderURL() {
        return null2local(EJBHomeFactory.getEjbProviderURL());
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(local2null(ejbProviderURL));
    }

    public final String getDirectoryPathList() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = dirPathList.size(); i < n; i++) {
            sb.append(dirPathList.get(i));
            if (i == curDirIndex)
                sb.append('*');
            sb.append(File.pathSeparatorChar);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public final void setDirectoryPathList(String str) {
        StringTokenizer st = new StringTokenizer(str, File.pathSeparator);
        ArrayList list = new ArrayList();
        HashSet set = new HashSet();
        int dirIndex = 0;
        for (int i = 0; st.hasMoreTokens(); ++i) {
            String tk = st.nextToken();
            int len = tk.length();
            if (tk.charAt(len-1) == '*') {
                dirIndex = i;
                tk = tk.substring(0, len-1);
            }                
            set.add(tk.replace(File.separatorChar, '/'));
            list.add(new File(tk));
        }
        if (list.isEmpty())
                throw new IllegalArgumentException(
                        "DirectoryPathList must NOT be emtpy");
        dirPathList = list;
        fsPathSet = set;
        curDirIndex = dirIndex;
    }

    public final String getReadOnlyDirectoryPathList() {
        if (rodirPathList.isEmpty())
            return "NONE";
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = rodirPathList.size(); i < n; i++) {
            sb.append(rodirPathList.get(i)).append(File.pathSeparatorChar);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public final void setReadOnlyDirectoryPathList(String str) {
        if ("NONE".equals(str)) {
            rodirPathList = Collections.EMPTY_LIST;
            rofsPathSet = Collections.EMPTY_SET;
            return;
        }
        StringTokenizer st = new StringTokenizer(str, File.pathSeparator);
        ArrayList list = new ArrayList();
        HashSet set = new HashSet();
        int dirIndex = 0;
        for (int i = 0; st.hasMoreTokens(); ++i) {
            String tk = st.nextToken();
            set.add(tk.replace(File.separatorChar, '/'));
            list.add(new File(tk));
        }
        rodirPathList = list;
        rofsPathSet = set;
    }
    
    public final String getRetrieveAET() {
        return retrieveAET;
    }

    public final void setRetrieveAET(String aet) {
        this.retrieveAET = aet;
    }

    public final String getMinFreeDiskSpace() {
        return FileUtils.formatSize(minFreeDiskSpace);
    }

    public final void setMinFreeDiskSpace(String str) {
        this.minFreeDiskSpace = FileUtils.parseSize(str, MIN_FREE_DISK_SPACE);
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
	 * Returns true if the freeDiskSpace policy flushStudiesExternalRetrievable is enabled.
	 * <p>
	 * If this policy is active studies must be external retrievable for deletion.
	 * 
	 * @return Returns true if flushStudiesExternalRetrievable policy is active.
	 */
	public boolean isFlushStudiesExternalRetrievable() {
		return flushStudiesExternalRetrievable;
	}
	/**
	 * Set the freeDiskSpace policy flushStudiesExternalRetrievable.
	 * <p>
	 * Set this policy active if studies must be external retrievable for deletion.
	 * 
	 * @param b The flushStudiesExternalRetrievable to set.
	 * 
	 * @throws IllegalArgumentException if deleteStudiesUnconditional is active
	 */
	public void setFlushStudiesExternalRetrievable(boolean b) {
		if ( b && deleteStudiesUnconditional ) {
			this.flushStudiesExternalRetrievable = false;
			throw new IllegalArgumentException("This policy is not allowed if deleteStudiesUnconditional is active!"); 
		}
		this.flushStudiesExternalRetrievable = b;
	}
	/**
	 * Returns true if the freeDiskSpace policy deleteStudiesUnconditional is enabled.
	 * <p>
	 * If this policy is active studies are deleted immedatly without any check.
	 * 
	 * @return Returns true if deleteStudiesUnconditional is active.
	 */
	public boolean isDeleteStudiesUnconditional() {
		return deleteStudiesUnconditional;
	}
	
	/**
	 * Set the freeDiskSpace policy deleteStudiesUnconditional.
	 * <p>
	 * If this policy is active studies are deleted immedatly without any check.
	 *
	 * @param b The deleteStudiesUnconditional to set.
	 * 
	 * @throws IllegalArgumentException if flushStudiesExternalRetrievable or flushStudiesOnMedia is active
	 */
	public void setDeleteStudiesUnconditional(boolean b) {
		if ( b && ( flushStudiesExternalRetrievable || flushStudiesOnMedia )) {
			deleteStudiesUnconditional = false;
			throw new IllegalArgumentException("This policy is not allowed in combination with flushStudiesExternalRetrievable or flushStudiesOnMedia!"); 
		}
		deleteStudiesUnconditional = b;
	}
	
	/**
	 * Returns true if the freeDiskSpace policy flushStudiesOnMedia is enabled.
	 * <p>
	 * If this policy is active studies must be stored on media (offline storage) for deletion.
	 * 
	 * @return Returns true if flushStudiesOnMedia policy is active.
	 */
	public boolean isFlushStudiesOnMedia() {
		return flushStudiesOnMedia;
	}
	
	/**
	 * Set the freeDiskSpace policy flushStudiesOnMedia.
	 * <p>
	 * Set this policy active if studies must be on media (offline storage) for deletion.
	 * 
	 * @param b The flushStudiesOnMedia to set.
	 *
	 * @throws IllegalArgumentException if deleteStudiesUnconditional is active
	 */
	public void setFlushStudiesOnMedia(boolean b) {
		if ( b && deleteStudiesUnconditional ) {
			this.flushStudiesOnMedia = false;
			throw new IllegalArgumentException("This policy is not allowed if deleteStudiesUnconditional is active!"); 
		}
		this.flushStudiesOnMedia = b;
	}
	
	/**
	 * Return number of days a study is not accessed for freeDiskSpace process.
	 * <p>
	 * This value is used by <code>freeDiskSpace</code> if the <code>freeDiskSpaceWaterMark</code> is not reached to 
	 * release studies that are older (not accessed) as this number of days.
	 * <p>
	 * In this case the freeDiskSpace policies (flushStudiesExternalRetrievable, flushStudiesOnMedia 
	 * and deleteStudiesUnconditional) are used to determine if the files of a study can be deleted.  
	 * 
	 * @return Returns the flushStudiesNotAccessedForDays.
	 */
	public int getFlushStudiesNotAccessedForDays() {
		return flushStudiesNotAccessedForDays;
	}
	
	/**
	 * Set number of days a study is not accessed for freeDiskSpace.
	 * 
	 * @param flushStudiesNotAccessedForDays The flushStudiesNotAccessedForDays to set in days.
	 */
	public void setFlushStudiesNotAccessedForDays(
			int freeDiskSpaceNotAccessedForDays) {
		this.flushStudiesNotAccessedForDays = freeDiskSpaceNotAccessedForDays;
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
    

    public final boolean isLocalFileSystem(String fsdir) {
        return fsPathSet.contains(fsdir) || rofsPathSet.contains(fsdir);
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

    public String showAvailableDiskSpace() throws IOException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = dirPathList.size(); i < n; i++) {
            FileSystemInfo info = initFileSystemInfo((File) dirPathList.get(i));
            sb.append(info).append("\r\n");
        }
        return sb.toString();
    }
    
    private FileSystemInfo initFileSystemInfo(File dir) throws IOException {
        File d = FileUtils.resolve(dir);
        if (!d.isDirectory()) {
            if (!makeStorageDirectory) {
                throw new IOException("Storage Directory " + d
	                    + " does not exists.");
            } else {
                if (d.mkdirs()) {
                    log.warn("M-CREATE Storage Directory: " + d);
                } else {
                    throw new IOException("Failed to create Storage Directory " + d);
                }
            }
        } else {
            if (new File(d, mountFailedCheckFile).exists()) {
	            throw new IOException("Mount check of Storage Directory " + d
	                    + " failed: Found " + mountFailedCheckFile);
            }
        }
        long available = new se.mog.io.File(d).getDiskSpaceAvailable();
        return new FileSystemInfo(FileUtils.slashify(dir), d, available, retrieveAET);
    }

    public FileSystemInfo selectStorageFileSystem() throws IOException {
        File curDir = (File) dirPathList.get(curDirIndex);
        FileSystemInfo info = initFileSystemInfo(curDir);
        if (info.getAvailable() > minFreeDiskSpace)
            return info;
        for (int i = 1, n = dirPathList.size(); i < n; ++i) {
            int dirIndex = (curDirIndex + i) % n;
            File dir = (File) dirPathList.get(dirIndex);
            info = initFileSystemInfo(dir);
            if (info.getAvailable() > minFreeDiskSpace) {
                log.info("High Water Mark reached on current Storage Directory "
                        + curDir + " - switch Storage Directory to " + dir);
                curDirIndex = dirIndex;
                return info;
            }
        }
        log.error("High Water Mark reached on Storage Directory " + curDir
                + " - no alternative Storage Directory available");
        return info;
    }

    public void purgeFiles() {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            FileDTO[] toDelete;
            for (int i = 0, n = dirPathList.size(); i < n; ++i) {
                try {
                    File f = (File) dirPathList.get(i);
                    toDelete = fsMgt.getDereferencedFiles(FileUtils.slashify(f));
                } catch (Exception e) {
                    log.warn("Failed to query dereferenced files:", e);
                    break;
                }
                for (int j = 0; j < toDelete.length; j++) {
                    FileDTO fileDTO = toDelete[j];
                    if (delete(FileUtils.toFile(fileDTO.getDirectoryPath(),
                            fileDTO.getFilePath()))) {
                        try {
                            fsMgt.deleteFile(fileDTO.getPk());
                        } catch (Exception e) {
                            log
                                    .warn("Failed to remove entry from list of dereferenced files:",
                                            e);
                        }
                    }
                }
            }
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }

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
        List list = new QueryFilesCmd(iuid).execute();
        if (list.isEmpty())
            return null;
        for (int i = 0, n = list.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) list.get(i);
            String dirPath = dto.getDirectoryPath();
            if (isLocalFileSystem(dirPath))
                return FileUtils.toFile(dirPath, dto.getFilePath());
        }
        FileDTO dto = (FileDTO) list.get(0);
        AEData aeData = new AECmd(dto.getRetrieveAET()).execute();
        return aeData.getHostName();
    }
    
    /**
     * Delete studies that fullfill freeDiskSpacePolicy to free disk space.
     * <p>
     * Checks available disk space if free disk space is necessary.
     * <p>
     * Remove old files until the stopFreeDiskSpaceWatermark is reached.
     * <p>
     * The real deletion is done in the purge process! This method removes only the reference to the file system.  
     *
     * @return The released size in bytes.
     * 
     * @throws IOException
     * @throws FinderException
     * @throws RemoveException
     * @throws EJBException
     */
    public long freeDiskSpace() throws IOException, FinderException, EJBException, RemoveException {
    	Long olderThan = null;
    	String prefix;
    	if ( ! isFreeDiskSpaceNecessary() ) {
    		olderThan = new Long( System.currentTimeMillis() - this.flushStudiesNotAccessedForDays * ONE_DAY_IN_MILLIS );
    		prefix = "FreeDiskSpace Status: Available disk space: OK ; Delete old files:";
    	} else {
    		prefix = "FreeDiskSpace Status: filesystems full! ; free disk space:";
    	}
    	
        FileSystemMgt fsMgt = newFileSystemMgt();
		long maxSizeToDel = (long) ( (float) this.minFreeDiskSpace * freeDiskSpaceUpperThreshold ) * dirPathList.size() - availableDiskSpace;
		long releasedSize = fsMgt.releaseStudies( fsPathSet, maxSizeToDel, deleteStudiesUnconditional, flushStudiesOnMedia, flushStudiesExternalRetrievable, olderThan );
    	return releasedSize;
    }
    
 
	
	/**
     * Check if a cleaning process is ncessary.
     * <p>
     * <OL>
     * <LI>Calculate the total space that should be available on all file systems. (<code>minAvail = minFreeDiskSpace * cleanWaterMarkFactor * dirPathList.size() </code>)</LI>
     * <LI>Cumulate available space from all file systems to get current available space on all file systems (=currAvail).</LI>
     * </OL>
     * <p>
     * Creates a directory if a defined file system path doesnt exist and makeStorageDirectory is true.
     * <p>
     * This method doesnt check if the defined file systems are on different disk/partitions!
     * 
     * @return True if clean is necessary ( currAvail < minAvail )
     * @throws IOException
     */
    public boolean isFreeDiskSpaceNecessary() throws IOException {
    	long minAvail = (long) ( (float) this.minFreeDiskSpace * freeDiskSpaceLowerThreshold ) * dirPathList.size();
    	long currAvail = getAvailableDiskSpace();
    	if ( log.isDebugEnabled() ) log.debug( "currAvail:"+currAvail+" < minAvail:"+minAvail);
    	return currAvail < minAvail; 
    }
    
    public long getAvailableDiskSpace() throws IOException {
    	Iterator iter = dirPathList.iterator();
    	FileSystemInfo info;
    	availableDiskSpace = 0L;
    	while ( iter.hasNext() ) {
    		info = initFileSystemInfo( (File) iter.next() );
    		availableDiskSpace += info.getAvailable();
    	}
    	return availableDiskSpace;
    }
}