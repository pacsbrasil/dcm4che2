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
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.notif.FileInfo;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Jan 16, 2006
 */
public class FTPArchiverService extends AbstractFileCopyService {

    private static final long MIN_FREE_DISK_SPACE = 20 * FileUtils.MEGA;       
    private long minFreeDiskSpace = MIN_FREE_DISK_SPACE;
    
	private static final int TAR_HEADER_SIZE = 2560;
	private static final int TAR_ENTRY_SIZE = 680;
	private boolean passiveMode = true;
	private boolean allocate = false;
	

	/* (non-Javadoc)
	 * @see org.dcm4chex.archive.hsm.AbstractFileCopyService#process(org.dcm4chex.archive.hsm.FileCopyOrder)
	 */
	protected void process(FileCopyOrder order) throws Exception {
		List fileInfos = order.getFileInfos();		
		int tarSize = estimateTarSize(fileInfos);
		FTPClient ftp = new FTPClient();
		try {
			init(ftp);
			if (allocate && !ftp.allocate(tarSize))
					throw new Exception("Failed to alloc " + tarSize);
			FileInfo file1Info = (FileInfo) fileInfos.get(0);
			String[] file1Path = StringUtils.split(file1Info.getFilePath(), '/');
			String dirName = mkdir(ftp, file1Path);
			String tarName = mkTarName(file1Path);
			OutputStream out = ftp.storeFileStream(tarName);
			try {
				TarOutputStream tar = new TarOutputStream(out);
				for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
					FileInfo fileInfo = (FileInfo) iter.next();
					File file = FileUtils.toFile(
							fileInfo.getFileSystemPath(),
							fileInfo.getFilePath());
					tar.putNextEntry(toTarEntry(file));
					FileInputStream fis = new FileInputStream(file);
					try {
						tar.copyEntryContents(fis);
					} finally {
						fis.close();
					}
					tar.closeEntry();
				}
				tar.finish();
			} finally {
				out.close();
			}
		} finally {
			try { ftp.logout(); } catch (IOException ignore) {}
			try { ftp.disconnect(); } catch (IOException ignore) {}
		}
	}

	private TarEntry toTarEntry(File file) {
		TarEntry entry = new TarEntry(file);
		File dir = file.getParentFile();
		entry.setName(dir.getName() + '/' + file.getName());
		return null;
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
		int size = TAR_HEADER_SIZE;
		for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
			FileInfo fileinfo = (FileInfo) iter.next();
			size += TAR_ENTRY_SIZE;
			size += fileinfo.getFileSize();
		}
		return size;
	}

	private void init(FTPClient f) throws IOException {
		/*
		String host = destPath.getHost();
		int port = destPath.getPort();
		if (port == -1)
			port = FTPClient.DEFAULT_PORT;
		f.connect(host, port);
		int reply = f.getReplyCode();
	    if (!FTPReply.isPositiveCompletion(reply)) {
	    	throw new IOException("FTP Server " + host + ":" +  port
	    			+ " refused connection: " + f.getReplyString());
	    }
		String user = destPath.getUserInfo();
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
		String path = destPath.getPath();
		if (!f.changeWorkingDirectory(path)) {
	    	throw new IOException("FTP Server " + host + ":" +  port
	    			+ " failed to change WOrking Directory to " +  path);			
		}
		if (passiveMode)
			f.enterLocalPassiveMode();
		f.setFileType(FTPClient.IMAGE_FILE_TYPE);
		*/
	}

}
