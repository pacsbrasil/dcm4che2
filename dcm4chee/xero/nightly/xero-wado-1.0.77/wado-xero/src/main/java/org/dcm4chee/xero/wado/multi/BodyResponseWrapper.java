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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**  
 *  This class wraps the provided HttpServletResponse.
 *  
 *  The output stream or print writer used by this wrapper
 *  will be the same as the wrapped object. This allows responses
 *  being composed in a multi-part response to invisibly work as if
 *  they were part of a non-nested response
 *  
 *  Note: this class doesn't (at the moment) correctly handle character encoding
 *  for individual segments of the multi-part response.
 */
public class BodyResponseWrapper extends HttpServletResponseWrapper 
{
    private static final Logger log = 
    	LoggerFactory.getLogger(BodyResponseWrapper.class);
    
    private HashMap<String, String> headers = new HashMap <String,String> ();
    
    private boolean committed = false;
    
    private BodyServletOutputStream myOutputStream = null;
    
    public BodyResponseWrapper(HttpServletResponse response) 
    {
    	super(response);
    	
		myOutputStream = new BodyServletOutputStream( this );
    }
    
    /** Creates a body response wrapper that allows one to capture the output fully, or otherwise
     * re-direct it.  You are responsible for ensuring headers are flushed or handled on the first
     * write request.
     * 
     * @param response
     * @param os
     */
    public BodyResponseWrapper(HttpServletResponse response, OutputStream os) 
    {
    	this(response);
    	myOutputStream.setBufferedOutputStream(os);
    }
    
    //
    // HttpServletResponseWrapper overrides...
    // 
    
    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }
    
    // convert time to a string
    @Override
    public void addDateHeader(String name, long d) {
        Date date = new Date(d);
        DateFormat df = DateFormat.
            getDateInstance(DateFormat.LONG);
        String dateStr = "\"" + df.format(date) + "\"";
        addHeader(name, dateStr );
    }
    
    @Override
    public void addIntHeader(String name, int value) {
        String valueStr = Integer.toString(value);
        addHeader(name, valueStr);
    }
    
    // append, if necessary
    @Override
    public void addHeader(String name, String value) {
        String oldValue = headers.get(name);
        headers.put(name, (oldValue == null )? value :
                        oldValue + "," + value );
    }

    @Override
    public boolean containsHeader(String name){
        return (headers.get(name) != null);
    }
    
    // we can't overrule our wrapping object, so we
    // just swallow this...
    // TODO: what's the correct behaviour here?
    @Override
    public void sendError(int sc) {
        log.debug("suppressing sendError within multipart/mixed component");
    }
    @Override
    public void sendError(int sc, String msg) {
        log.debug("suppressing sendError within multipart/mixed component");
    }
    @Override
    public void sendRedirect(String location) {
        log.debug("suppressing sendRedirect within multipart/mixed component");
    }
    
    @Override
    public void setDateHeader(String name, long d) {
        Date date = new Date(d);
        DateFormat df = DateFormat.
            getDateInstance(DateFormat.LONG);
        String dateStr = "\"" + df.format(date) + "\"";
        setHeader(name, dateStr );        
    }

    @Override
    public void setIntHeader(String name, int value) {
        String valueStr = Integer.toString(value);
        setHeader(name, valueStr);
    }
    
    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }
    
    @Override
    public void setStatus(int sc) {
        // status only makes sense if this were HTTP contents
        // however, it's not -- it's part of a single HTTP response
        // that happens to have multipart non-HTTP content.
        // so, quietly swallow the status
        log.debug("suppressing status of multipart/mixed component");
    }
    
    //
    // ServletResponseWrapper overrides
    //
    @Override
    public void flushBuffer() throws IOException 
    {
        if ( myOutputStream == null ) {
            getOutputStream(); // sets myOutputStream as a side effect
        }
        myOutputStream.flush();
    }

    @Override
    public int getBufferSize() 
    {
    	return myOutputStream.getBufferSize();
    }
    
    @Override
    public String getContentType() 
    {
        return headers.get("Content-Type");
    }
    
    // we're deferring whether or not we're committed
    // with respect to writing to the actual output 
    // stream.  isn't that special?
    @Override
    public ServletOutputStream getOutputStream() 
    {
        return myOutputStream;
    }
        
    @Override
    public PrintWriter getWriter() 
    {
        return new PrintWriter(getOutputStream());
    }

    @Override
    public boolean isCommitted() 
    {
        return committed;
    }
    
    @Override
    public void reset() 
    {
        headers.clear();
        resetBuffer();
    }
    
    @Override
    public void resetBuffer() 
    { 
        if ( committed ) throw new IllegalStateException();
    }
    
    @Override
    public void setBufferSize(int size) 
    {
    	myOutputStream.setBufferSize(size);
    }
    
    // TODO: what to do here
    @Override
    public void setCharacterEncoding(String charset) 
    {
    }
    
    @Override
    public void setContentLength(int len) 
    {
        setIntHeader("Content-Length", len);
    }
    
    @Override
    public void setContentType(String ct) 
    {
        setHeader("Content-Type", ct);
    }
    
    // TODO: what to do here...
    @Override
    public void setLocale(Locale loc) 
    {
        log.debug("suppressing setLocale within multipart/mixed component");
    }
    
    void setIsCommited()
    {
    	this.committed = true;
    }
    
    HashMap<String, String> getHeaders() 
    {
		return headers;
	}
    
    OutputStream getOriginalOutputStream() throws IOException
    {
    	return super.getOutputStream();
    }
}
