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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.wado.WadoParams.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns the object as read from the given URL
 * 
 * @author bwallace
 */
public class UrlServletResponseItem implements ServletResponseItem {
	private static final Logger log = LoggerFactory.getLogger(UrlServletResponseItem.class);
	URL url;
	String contentType;
	String filename;
	Boolean memoryMap = null;
	int bufSize = 64*1024;

	/** Record the URL for playback */
	public UrlServletResponseItem(URL url, String contentType, String filename) {
		this.url = url;
		this.contentType = contentType;
		this.filename = filename;
	}

	/** Write the contents from the given URL to the servlet response */
	public void writeResponse(HttpServletRequest arg0, HttpServletResponse response) throws IOException {
		if (url == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			log.warn("No response found for request.");
			return;
		}
		if (contentType != null)
			response.setContentType(contentType);
		if (filename != null)
			response.setHeader(CONTENT_DISPOSITION, "attachment;filename=" + filename);
		InputStream is;
		String surl = url.toString();
		long fileSize;
		OutputStream os = response.getOutputStream();
		if (surl.startsWith("file:")) {
			String fileName = url.getFile();
			File file = new File(fileName);
			fileSize = file.length();
			response.setContentLength((int) fileSize);
			if ( memoryMap==null || memoryMap==false) {
				log.info("Using stream file {} of size {}", fileName, fileSize);
				streamFile(os, new FileInputStream(file), (int) Math.min(fileSize,bufSize));
			} else {
				log.info("Using memory mapped file {} of size {}", fileName, fileSize);
				memoryMapFile(os, fileName, bufSize);
			}
		} else {
			URLConnection conn = url.openConnection();
			log.info("Reading from URL connection " + surl);
			fileSize = conn.getContentLength();
			is = conn.getInputStream();
			if (fileSize != -1) {
				log.info("Returning {} bytes for file {}", fileSize, url);
				response.setContentLength((int) fileSize);
			}
			streamFile(os,is,(int) Math.min(fileSize,bufSize));
		}
	}

	/**
	 * Sends a memory mapped file to the given output stream.
	 * 
	 * @param os
	 * @param fileName
	 * @param bufSize
	 * @throws IOException
	 */
	public static void memoryMapFile(OutputStream os, String fileName, int bufSize) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		FileChannel fc = fis.getChannel();
		ByteBuffer bb = ByteBuffer.allocate(32 * 1024);
		int s = fc.read(bb);
		while (s > 0) {
			os.write(bb.array(), 0, s);
			bb.clear();
			s = fc.read(bb);
		}
		fc.close();
		fis.close();
		os.close();
		return;
	}
	
	/** Streams the input stream to the output stream, reading bufSize elements at a time.
	 * 
	 * @param os
	 * @param is
	 * @param bufSize
	 * @throws IOException
	 */
	public static void streamFile(OutputStream os, InputStream is, int bufSize) throws IOException {
		  byte[] data = new byte[bufSize];
		  int size = is.read(data);
		  while(size>0) {
			 os.write(data,0,size);
			 os.flush();
			 size = is.read(data);
		  }
		  is.close();
		  os.close();
	}

	public Boolean getMemoryMap() {
   	return memoryMap;
   }

	public void setMemoryMap(Boolean memoryMap) {
   	this.memoryMap = memoryMap;
   }

	public int getBufSize() {
   	return bufSize;
   }

	public void setBufSize(int bufSize) {
   	this.bufSize = bufSize;
   }
}
