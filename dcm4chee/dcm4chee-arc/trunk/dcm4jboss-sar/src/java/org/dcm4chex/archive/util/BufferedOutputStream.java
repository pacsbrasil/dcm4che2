package org.dcm4chex.archive.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.stream.ImageInputStream;

public class BufferedOutputStream extends FilterOutputStream
{

    protected final byte[] buf;
    protected int count;

    public BufferedOutputStream(OutputStream out)
    {
        this(out, new byte[8192]);
    }

    public BufferedOutputStream(OutputStream out, byte[] buf)
    {
        super(out);
        if (buf.length == 0)
        {
            throw new IllegalArgumentException("Buffer size == 0");
        }
        this.buf = buf;
    }

    private void flushBuffer() throws IOException
    {
        if (count > 0)
        {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    public synchronized void write(int b) throws IOException
    {
        if (count >= buf.length)
        {
            flushBuffer();
        }
        buf[count++] = (byte) b;
    }

    public synchronized void write(byte b[], int off, int len)
            throws IOException
    {
        if (len >= buf.length)
        {
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        if (len > buf.length - count)
        {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    public synchronized void write(InputStream in, int len)
    throws IOException
    {
        for (int toWrite = len, read = 0; toWrite > 0; toWrite -= read)
        {
            read = in.read(buf, count, Math.min(toWrite, buf.length - count));
            count += read;
            if (count >= buf.length)
            {
                flushBuffer();
            }
        }
    }


    public synchronized void write(ImageInputStream in, int len)
    throws IOException
    {
        for (int toWrite = len, read = 0; toWrite > 0; toWrite -= read)
        {
            read = in.read(buf, count, Math.min(toWrite, buf.length - count));
            count += read;
            if (count >= buf.length)
            {
                flushBuffer();
            }
        }
    }
    
    public synchronized void flush() throws IOException
    {
        flushBuffer();
        out.flush();
    }
}
