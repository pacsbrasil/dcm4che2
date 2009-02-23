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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.xero.wado.multi;

import static org.dcm4chee.xero.wado.WadoParams.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a ZIP response from the iterated items.
 * 
 * @author bwallace
 */
public class ZipContentTypeResponseItem implements MultiPartHandler {
	private static Logger log = LoggerFactory.getLogger(ZipContentTypeResponseItem.class);

	/** Store the iterator that is used to get the responses.
	 */
	private Iterator<ServletResponseItem> multiResponses;
	

	/** Store the iterator that is used to get the responses.  An iterator is used instead of a list because the memory size and computational
	 * complexity of computing the entire list up front may exceed the available memory size or allowed response time for the output.  As well, 
	 * asynchronous item generation and writing of the thread can be implemented by a set of these response items.
	 * @return 
	 */
	public void setResponseIterator(Iterator<ServletResponseItem> multiResponses) {
		this.multiResponses = multiResponses;
	}
	
	/** Get the response iterator for the Servlet responses - don't touch this iterator 
	 * in terms of using the next method as that will remove responses from the queue.
	 * @return
	 */
	public Iterator<ServletResponseItem> getResponseIterator() {
		return this.multiResponses;
	}
	
	/**
	 * Write the multi-part response to the provided stream.
	 * 
	 * @param httpRequest
	 *            unused
	 * @param response
	 *            that the multipart/mixed results are written to. Also sets the content type.
	 */
	public void writeResponse(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {
		
		if (multiResponses==null || ! multiResponses.hasNext())
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested content not found.");
			log.warn("Requested content not found.");
			return;
		}

		response.setContentType(MultiPartContentTypeFilter.APPLICATION_ZIP);
		response.setHeader(CONTENT_DISPOSITION, "attachment;filename=wado.zip");

		ServletOutputStream os = response.getOutputStream();
		ZipOutputStream zos = new ZipOutputStream(os);

		int fcnt=1;
		// each child *can't* be allowed to modify the response headers by itself.
		while( multiResponses.hasNext())  
		{
			ServletResponseItem sri = multiResponses.next();
			// This is perfectly acceptable to return a null response - it just means that particular content type isn't found.
			if( sri==null ) {
				log.info("Skipping a servlet response with an empty response item.");
				continue;
			}
			if ( sri instanceof ErrorResponseItem ) {
				log.warn("Skipping an ErrorResponseItem with code: " + ((ErrorResponseItem)sri).getCode());
				continue;
			}
			log.debug("Found a response item to add.");
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BodyResponseWrapper brw = new BodyResponseWrapper(response,baos);
				sri.writeResponse(httpRequest, brw);
				byte[] data = baos.toByteArray();
				Map<String,String> headers = brw.getHeaders();
				String filename = getFilename(headers);
				if( filename==null ) filename="wado-unknown-"+fcnt++;
				log.debug("Writing response named {}", filename);
				ZipEntry ze = new ZipEntry(filename);
				ze.setSize(data.length);
				ze.setMethod(ZipEntry.STORED);
				String comment = headersToString(headers);
				if( comment!=null ) ze.setComment(comment);
				log.debug("Putting next entry {}", ze);
				CRC32 crc = new CRC32();
				crc.update(data);
				ze.setCrc(crc.getValue());
				zos.putNextEntry(ze);
				log.debug("Write data of size {}", data.length);
				zos.write(data);
			} catch ( Exception e ) {
				e.printStackTrace();
				log.warn("response for content type=\"" + response.getContentType() + "has failed:"+e);
			}
		}
		zos.close();
		os.close();
	}

	public String headersToString(Map<String,String> headers) {
		if( headers.size()==0 ) return null;
		StringBuffer ret = new StringBuffer();
		boolean first = true;
		for(Map.Entry<String,String> me : headers.entrySet()) {
			if( first ) {
				first = false;
			}
			else {
				ret.append("\n");
			}
			ret.append(me.getKey()).append(": ").append(me.getValue());
		}
		return ret.toString();
	}
	
	/** Gets the filename from the content-disposition */
	public static String getFilename(Map<String,?> headers) {
		String cd = (String) headers.get(CONTENT_DISPOSITION);
		if( cd==null ) {
			log.info("No content disposition found for child item - using default name.");
			return null;
		}
		int start = cd.indexOf("filename=");
		if( start==-1 ) {
			log.info("No filename in content disposition found for child item - using default name:{}",cd);
			return null;
		}
		int end = cd.indexOf(';',start);
		if( end==-1 ) end = cd.length();
		if( end-start <= 9 ) {
			log.info("Filename specified in content disposition, but was empty:{}",cd);
			return null;
		}
		return cd.substring(start+9,end);
	}
}
