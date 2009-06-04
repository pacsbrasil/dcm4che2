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

package org.dcm4chee.xero.wado;

import static org.easymock.EasyMock.eq;
import static org.easymock.classextension.EasyMock.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dcm4chee.xero.wado.multi.BodyResponseWrapper;
import org.dcm4chee.xero.wado.multi.BodyServletOutputStream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author tcollins
 *
 */
public class BodyServletOutputStreamTest 
{
	BodyServletOutputStream stream;
	
	BufferedOutputStream bufferedOutputStream;
	
	OutputStream os;

	private BodyResponseWrapper wrapper;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception 
	{
		bufferedOutputStream = createMock( BufferedOutputStream.class );
		replay(bufferedOutputStream );
		
		os = createNiceMock( OutputStream.class );
		replay( os );
		
		wrapper = createNiceMock( BodyResponseWrapper.class );
		replay(wrapper);
		
		stream = new BodyServletOutputStream( wrapper )
		{
			@Override
			protected OutputStream getOriginalOutputStream() throws IOException {
				return os;
			}

			@Override
			protected boolean initialize() throws IOException
			{
				super.initialize();
				
				return false;
			}

			@Override
			protected OutputStream createOutputStream(OutputStream os, int bufferSize ) 
			{
				return bufferedOutputStream;
			}
		};
	}

	@Test
	public void testConstructor_WithNullWrapper_ShouldThrowIllegalAE()
	{
		try
		{
			new BodyServletOutputStream(null);
			
			assert(false);
		}
		catch ( IllegalArgumentException ex ) {}
	}
	
	
	@Test
	public void testCallToFlush_WhenBufferedStreamNotInitialized_ShouldCallFlushOnProvidedStream() 
		throws Exception
	{
		reset( os );
		os.flush();
		replay( os );
		
		stream.flush();
		
		verify( os );
	}
	
	
	@Test
	public void testCallToFlush_WhenBufferedStreamInitialized_ShouldCallFlushOnStream() 
		throws Exception
	{
		
		initializeTestStream();
		
		reset( bufferedOutputStream );
		bufferedOutputStream.flush();
		replay( bufferedOutputStream );
		
		stream.flush();
		
		verify( bufferedOutputStream );
	}
	
	@Test
	public void testCallToClose_WhenBufferedStreamNotInitialized_ShouldCallFlushOnProvidedStream() 
		throws Exception
	{
		reset( os );
		os.flush();
		replay( os );
		
		stream.close();
		
		verify( os );
	}
	
	@Test
	public void testCallToClose_WhenBufferedStreamInitialized_ShouldCallFlushOnStream() 
		throws Exception
	{
		initializeTestStream();
		
		reset( bufferedOutputStream );
		bufferedOutputStream.flush();
		replay( bufferedOutputStream );
		
		stream.close();
		
		verify( bufferedOutputStream );
	}
	
	
	@Test
	public void testCallToWrite_WhenOffsetAndLengthProvided_ShouldForwardTheWriteRequest()
		throws Exception
	{
		byte[] b = new byte[0];
		int off = 1;
		int len = 1;
		
		reset( bufferedOutputStream );
		bufferedOutputStream.write( eq(b), eq(off), eq(len) );
		replay( bufferedOutputStream );
		
		stream.write(b, off, len);
		
		verify( bufferedOutputStream );
	}
	
	
	@Test
	public void testCallToWrite_WhenByteArrayProvided_ShouldForwardWriteRequestWithDefaults()
		throws Exception
	{
		int arrayLength = 1;
		int offset = 0;
		byte[] b = new byte[arrayLength];
		
		reset( bufferedOutputStream );
		bufferedOutputStream.write( eq(b), eq(offset), eq(arrayLength) );
		replay( bufferedOutputStream );
		
		stream.write(b);
		
		verify( bufferedOutputStream );
	}
	
	
	@Test
	public void testCallToGetBufferSize_WhenInitialized_ShouldReturn2K()
	{
		assert( stream.getBufferSize() == BodyServletOutputStream.DEFAULT_BUFFER_SIZE );
	}
	
	@Test
	public void testCallToSetBufferSize_WhenValueGreaterThanZero_ShouldReturnAssignedValue()
	{
		final int assignedBufferSize = 1; 
		stream.setBufferSize( assignedBufferSize );
		
		assert( stream.getBufferSize() == assignedBufferSize );
	}
	
	
	@Test
	public void testCallToSetBufferSize_WhenSizeIsLessThanZero_ShouldThrowIllegalAE()
	{
		try
		{
			stream.setBufferSize( -1 );
			
			assert( false );
		}
		catch( IllegalArgumentException ex ) {}
		
	}
	
	@Test
	public void testCallToSetBufferSize_WhenWriteCommitted_ShouldThrowIllegalStateException()
	{
		try
		{
			reset( wrapper );
			expect( wrapper.isCommitted() ).andStubReturn(true);
			replay( wrapper );
			
			stream.setBufferSize(1);
			
			assert(false);
		}
		catch ( IllegalStateException ex ) {}
		
	}
	
	
	protected void initializeTestStream() throws Exception
	{
		byte[] b = new byte[0];
		
		reset( bufferedOutputStream );
		bufferedOutputStream.write( eq(b), eq(0), eq(0));
		replay( bufferedOutputStream );
		
		stream.write( b );
	}

}
