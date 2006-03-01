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


package org.dcm4chex.archive.hsm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.dcm4che.util.MD5Utils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.FileSystemStatus;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.notif.FileInfo;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileSystemUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Jan 16, 2006
 */
public class FTPArchiverService extends AbstractFileCopyService {

    private static final String NONE = "NONE";

    private static final int MD5SUM_ENTRY_LEN = 52;
    
	private static final int TAR_HEADER_SIZE = 2560;
	private static final int TAR_ENTRY_SIZE = 680;
	private boolean passiveMode = true;
	private long minFreeDiskSpace = -1;
    private String defaultPassword;
    private String defaultUser;
	

    public final String getDefaultUser() {
        return defaultUser;
    }

    public final void setDefaultUser(String defaultUser) {
        this.defaultUser = defaultUser;
    }

    public final String getDefaultPassword() {
        return defaultPassword;
    }
    
    public final void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }
    
    public final String getMinFreeDiskSpace() {
        return minFreeDiskSpace > 0 ? FileUtils.formatSize(minFreeDiskSpace)
                : NONE;
    }

    public final void setMinFreeDiskSpace(String s) {
        this.minFreeDiskSpace = NONE.equalsIgnoreCase(s) ? -1 : FileUtils.parseSize(s, 0);
    }

    public final boolean isPassiveMode() {
        return passiveMode;
    }

    public final void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    /* (non-Javadoc)
	 * @see org.dcm4chex.archive.hsm.AbstractFileCopyService#process(org.dcm4chex.archive.hsm.FileCopyOrder)
	 */
	protected void process(FileCopyOrder order) throws Exception {
		List fileInfos = order.getFileInfos();
        String fsPath;
        String tarPath;
        FTPClient ftp = new FTPClient();
        FileSystemMgt fsmgt = getFileSystemMgtHome().create();
        FileSystemDTO[] fsDTOs = fsmgt.findFileSystemsLikeDirectoryPath(
                order.getDestinationFileSystemPath(),
                Availability.NEARLINE, FileSystemStatus.DEF_RW);
        if (fsDTOs.length == 0) {
            throw new ConfigurationException(
                    "No such active file system - " 
                    + order.getDestinationFileSystemPath());
        }
        if (fsDTOs.length > 1) {
            throw new ConfigurationException(
                    "More than one active file system - " 
                    + order.getDestinationFileSystemPath());
        }
		try {
			fsPath = fsDTOs[0].getDirectoryPath();
			URL ftpURL = new URL(fsPath);
			String ftpdir = ftpURL.getPath().substring(1);
			connect(ftp, ftpURL);
            final int tarSize = estimateTarSize(fileInfos);
            if (!checkAvailableDiskSpace(ftp, ftpURL, tarSize)) {
                ftp.logout();
                ftp.disconnect();
                fsPath = fsDTOs[0].getNext();
                if (fsPath == null || fsPath.length() == 0)
                    throw new ConfigurationException(
                            "Run out of disk space on active file system - " +
                            "no further file system configured");
                FileSystemDTO nextFsDTO = fsmgt.getFileSystem(fsPath);
                if (nextFsDTO.getAvailability() != Availability.NEARLINE
                        || nextFsDTO.getStatus() != FileSystemStatus.RW) {
                    throw new ConfigurationException(
                            "Unexpected Availability or Status of "
                            + nextFsDTO);
                }
    			ftpURL = new URL(fsPath);
    			ftpdir = ftpURL.getPath().substring(1);
                connect(ftp, ftpURL);
                if (!checkAvailableDiskSpace(ftp, ftpURL, tarSize)) {
                    throw new ConfigurationException(
                            "Unexpected Short of available Disk Space on "
                            + nextFsDTO);                    
                }
                fsDTOs[0].setStatus(FileSystemStatus.RO);
                nextFsDTO.setStatus(FileSystemStatus.DEF_RW);
                fsmgt.updateFileSystem2(fsDTOs[0], nextFsDTO);
            }
			if (!ftp.changeWorkingDirectory(ftpdir)) {
		    	throw new IOException("FTP Server " + ftpURL.getAuthority()
		    			+ " failed to change WOrking Directory to " +  ftpdir);			
			}
			FileInfo file1Info = (FileInfo) fileInfos.get(0);
			String[] file1Path = StringUtils.split(file1Info.getFilePath(), '/');
			String tarName = mkTarName(file1Path);
			tarPath = mkdir(ftp, file1Path) + tarName;
            log.info("FTP: PUT " + fsPath + '/' + tarPath);
            TarOutputStream tar = new TarOutputStream(ftp.storeFileStream(tarName));
			try {
                writeMD5SUM(tar, fileInfos);
				for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
					writeFile(tar, (FileInfo) iter.next());
				}
			} finally {
                tar.close();
                ftp.completePendingCommand();
			}
			if (verifyCopy) {
				VerifyTar verifyTar = new VerifyTar();
				log.info("Start verifying " + tarName);
				verifyTar.verify(ftp.retrieveFileStream(tarName), tarName);
                ftp.completePendingCommand();
				log.info("Finished verifying " + tarName);
			}
		} finally {
			try { ftp.logout(); } catch (IOException ignore) {}
			try { ftp.disconnect(); } catch (IOException ignore) {}
		}
        Storage storage = getStorageHome().create();
        for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
            FileInfo finfo = (FileInfo) iter.next();
            storage.storeFile(finfo .getSOPInstanceUID(),
                    finfo.getTransferSyntaxUID(), fsPath, 
                    tarPath + '!' + mkTarEntryName(finfo.getFilePath()),
                    (int) finfo.getFileSize(), finfo.getMd5sum(), fileStatus);
        }
	}

	private boolean checkAvailableDiskSpace(FTPClient ftp, URL ftpurl, 
    		int tarSize) throws IOException {
    	if (minFreeDiskSpace < 0) // check disabled
    		return true;
        String ftpdir = ftpurl.getPath().substring(1);
		final String cmd = "EXEC df -k " + ftpdir;
	    log.info("FTP Server " + ftpurl.getHost() + ": SITE " + cmd);
		ftp.sendSiteCommand(cmd );
		int reply = ftp.getReplyCode();
	    if (!FTPReply.isPositiveCompletion(reply)) {
	    	throw new IOException("FTP Server " + ftpurl.getHost()
	    			+ ": refused SITE " + cmd  + " - " + ftp.getReplyString());
	    }
	    String[] lines = ftp.getReplyStrings();
	    long free = FileSystemUtils.parseDF_k(ftpdir, 
	    		trimLine(lines, 1),
	    		trimLine(lines, 2),
	    		trimLine(lines, 3));
	    log.info("FTP Server: " + ftpurl.getHost() + ": "
	    		+ FileUtils.formatSize(free) + " free on " + ftpdir);
        return free - tarSize > minFreeDiskSpace;
    }

	private String trimLine(String[] lines, int i) {
		try {
			final String line = lines[i];
			return line.startsWith("200-") ? line.substring(4) : line;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	private void writeFile(TarOutputStream tar, FileInfo fileInfo) 
    throws IOException, FileNotFoundException {
        final String filePath = fileInfo.getFilePath();
		File file = FileUtils.toFile(
        		fileInfo.getFileSystemPath(),
        		filePath);
        TarEntry entry = new TarEntry(mkTarEntryName(filePath));
        entry.setSize(fileInfo.getFileSize());
        tar.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(file);
        try {
        	tar.copyEntryContents(fis);
        } finally {
        	fis.close();
        }
        tar.closeEntry();
    }

    private void writeMD5SUM(TarOutputStream tar, List fileInfos) throws IOException {
        byte[] md5sum = new byte[fileInfos.size() * MD5SUM_ENTRY_LEN];
        final TarEntry tarEntry = new TarEntry("MD5SUM");
        tarEntry.setSize(md5sum.length);
        tar.putNextEntry(tarEntry);
        
        int i = 0;
        for (Iterator iter = fileInfos.iterator(); iter.hasNext(); 
                i += MD5SUM_ENTRY_LEN) {
            FileInfo fileInfo = (FileInfo) iter.next();
            MD5Utils.toHexChars(fileInfo.getMd5sum(), md5sum, i);
            md5sum[i+32] = ' ';
            md5sum[i+33] = ' ';
            System.arraycopy(
                    mkTarEntryName(fileInfo.getFilePath()).getBytes("US-ASCII"),
                    0, md5sum, i+34, 17);
            md5sum[i+51] = '\n';
        }
        tar.write(md5sum);
        tar.closeEntry();
    }

	private String mkTarEntryName(String filePath) {
        return filePath.substring(filePath.length() - 17);
    }

    private String mkTarName(String[] fPath) {
		return fPath[fPath.length - 2] + '-' + fPath[fPath.length - 1] + ".tar";
	}

	private String mkdir(FTPClient f, String[] file1Path) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < file1Path.length-2; i++) {
			sb.append(file1Path[i]).append('/');
			f.makeDirectory(file1Path[i]);
			f.changeWorkingDirectory(file1Path[i]);
		}
        return sb.toString();
	}

	private int estimateTarSize(List fileInfos) {
		int size = TAR_HEADER_SIZE + TAR_ENTRY_SIZE;
		for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
			FileInfo fileinfo = (FileInfo) iter.next();
			size += MD5SUM_ENTRY_LEN + TAR_ENTRY_SIZE + fileinfo.getFileSize();
		}
		return size;
	}

	private void connect(FTPClient f, URL ftpURL) throws IOException {
        String host = ftpURL.getHost();
		int port = ftpURL.getPort();
		if (port == -1)
			port = FTPClient.DEFAULT_PORT;
		f.connect(host, port);
		int reply = f.getReplyCode();
	    if (!FTPReply.isPositiveCompletion(reply)) {
	    	throw new IOException("FTP Server " + host + ":" +  port
	    			+ " refused connection: " + f.getReplyString());
	    }
		String user = ftpURL.getUserInfo();
        String pass = defaultPassword;
		if (user == null)
			user = defaultUser;
		else
		{
			int colon = user.indexOf(':');
			if (colon != -1)
			{
				pass = user.substring(colon+1);
				user = user.substring(0,colon);
			}
		}
		if (!f.login(user, pass)) {
	    	throw new IOException("FTP Server " + host + ":" +  port
	    			+ " refused login of user " +  user);			
		}
		if (passiveMode)
			f.enterLocalPassiveMode();
		f.setFileType(FTPClient.IMAGE_FILE_TYPE);
	}

    private FileSystemMgtHome getFileSystemMgtHome()
            throws HomeFactoryException {
        return (FileSystemMgtHome) EJBHomeFactory.getFactory().lookup(
                FileSystemMgtHome.class, FileSystemMgtHome.JNDI_NAME);
    }
}
