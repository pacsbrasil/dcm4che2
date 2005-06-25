/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Jun 25, 2005
 *
 */
public class RAFInputStreamAdapter extends InputStream {

	private final RandomAccessFile raf;
	private long markedPos;
	private IOException markException;
	
	public RAFInputStreamAdapter(RandomAccessFile raf) {
		this.raf = raf;
	}

	public int read() throws IOException {
		return raf.read();
	}

	public synchronized void mark(int readlimit) {
		try {
			this.markedPos = raf.getFilePointer();
			this.markException = null;
		} catch (IOException e) {
			this.markException = e;
		}
	}

	public boolean markSupported() {
		return true;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return raf.read(b, off, len);
	}

	public synchronized void reset() throws IOException {
		if (markException != null)
			throw markException;
		raf.seek(markedPos);		
	}

	public long skip(long n) throws IOException {
		return raf.skipBytes((int) n);
	}

}
