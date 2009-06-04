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

package org.dcm4chee.xero.wado.multi;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;


/**
 * Given an HTTPServletResponse (BodyResponseWrapper)
 * object, this output stream buffers any changes requested, and any 
*  written output, until the buffer is full, or until
*  a flush is requested.  At that point, the contents
*  of the response are written to the body of the underlying
*  response, and this wrapped response is considered to
*  be committed.
*  **/
public class BodyServletOutputStream extends ServletOutputStream 
{
	public static final int DEFAULT_BUFFER_SIZE = 2048;
	
	final byte[] singleByteBuffer = new byte[1];
	
	private OutputStream bufferedOutputStream;
	
	private OutputStream originalOutputStream;
	
	private BodyResponseWrapper wrapper;
	
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	
	public BodyServletOutputStream( BodyResponseWrapper wrapper )
	{
		if( wrapper == null )
			throw new IllegalArgumentException();
		
		this.wrapper = wrapper;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException 
	{
		if( initialize() )
		{
			HashMap<String, String> headers = wrapper.getHeaders();
			writeHeaders( headers );
			wrapper.setIsCommited();
		}

		bufferedOutputStream.write(b, off, len);
	}
	
	
	protected boolean initialize() throws IOException
	{
		if( bufferedOutputStream == null )
		{
			bufferedOutputStream = createOutputStream( getOriginalOutputStream(), bufferSize );
			return true;
		}
		
		return false;
	}

	protected void writeHeaders(HashMap<String, String> headers) throws IOException
	{
        for ( Map.Entry<String, String> e: headers.entrySet() ) 
        {
        	println(e.getKey() + ": " + e.getValue());
        }
       
        println();
	}

	@Override
	public void write(byte[] b) throws IOException 
	{
		write(b, 0, b.length);
	}

	@Override
	public void write(int b) throws IOException 
	{
		singleByteBuffer[0] = (byte) (b & 255); // need the bottom byte;
        write(singleByteBuffer, 0, 1);
	}
	
	@Override
	public void close() throws IOException 
	{
		flush();
		// The actual close has to be done by the body wrapper.
	}

	@Override
	public void flush() throws IOException 
	{
		if( bufferedOutputStream == null )
		{
			getOriginalOutputStream().flush();
			return;
		}
			
		bufferedOutputStream.flush();
	}
	
	public int getBufferSize() 
	{
		return bufferSize;
	}

	public void setBufferSize(int assignedBufferSize) 
	{
		if( assignedBufferSize < 0 ) throw new IllegalArgumentException();
		if( wrapper.isCommitted() ) throw new IllegalStateException();
		
		bufferSize = assignedBufferSize;
	}

	protected OutputStream createOutputStream( OutputStream os, int bufferSize )
	{
		return new BufferedOutputStream( os, bufferSize );
	}

	protected OutputStream getOriginalOutputStream() throws IOException
	{
		if ( originalOutputStream == null )
			originalOutputStream = wrapper.getOriginalOutputStream();
		
		return originalOutputStream;
	}
	
	/** This can be used to capture the output rather than sending it directly out. */
	public void setBufferedOutputStream(OutputStream os) {
		// Better not have already written anything...
		assert this.bufferedOutputStream == null;
		this.bufferedOutputStream = os;
	}

}
