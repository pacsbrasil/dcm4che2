/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.dcm4che2.util.ByteUtils;
import org.dcm4che2.util.TagUtils;

public class DicomInputStream extends FilterInputStream implements
		DicomInputHandler {

	private static Logger log = Logger.getLogger(DicomInputStream.class);

	DicomInputHandler handler = this;

	TransferSyntax ts;

	AttributeSet attrs;

	ArrayList sqStack;

	long pos = 0;

	long tagPos = 0;

	long fmiEndPos = -1;

	boolean ignoreFmiTs = false;

	long markedPos = 0;

	byte[] preamble;

	byte[] header = new byte[8];

	int tag;

	VR vr;

	int vallen;

	long vallenLimit = 40000000L;

	public DicomInputStream(RandomAccessFile raf) throws IOException {
		this(new RAFInputStreamAdapter(raf));
		pos = raf.getFilePointer();
	}

	public DicomInputStream(File f) throws IOException {
		this(new BufferedInputStream(new FileInputStream(f)));
	}

	public DicomInputStream(InputStream in) throws IOException {
		this(in, null);
	}

	public DicomInputStream(InputStream in, TransferSyntax ts)
			throws IOException {
		super(in);
		this.ts = (ignoreFmiTs = ts != null) ? ts : guessTransferSyntax();
	}

	public final long getStreamPosition() {
		return pos;
	}

	public final void setStreamPosition(long pos) {
		this.pos = pos;
	}

	public final void setHandler(DicomInputHandler handler) {
		if (handler == null)
			throw new NullPointerException();
		this.handler = handler;
	}

	public final int tag() {
		return tag;
	}
	
	public final int level() {
		return sqStack != null ? sqStack.size() : 0;
	}
	
	public final int valueLength() {
		return vallen;
	}

	public final VR vr() {
		return vr;
	}
    
    public final Attribute sq() {
        return (Attribute) sqStack.get(sqStack.size() - 1);
    }
    
    public final TransferSyntax getTransferSyntax() {
        return ts;
    }

    public final AttributeSet getAttributeSet() {
        return attrs;
    }

	private TransferSyntax guessTransferSyntax() throws IOException {
		mark(132);
		byte[] b = new byte[128];
		try {
			readFully(b, 0, 128);
			readFully(header, 0, 4);
			if (header[0] == 'D' && header[1] == 'I' && header[2] == 'C'
					&& header[3] == 'M') {
				preamble = b;
				return TransferSyntax.ExplicitVRLittleEndian;
			}
		} catch (IOException ignore) {
		}
		reset();
		try {
			VR.valueOf(((b[4] & 0xff) << 8) | (b[5] & 0xff));
			return b[1] == 0 ? TransferSyntax.ExplicitVRLittleEndian
					: TransferSyntax.ExplicitVRBigEndian;
		} catch (IllegalArgumentException e) {
			return b[1] == 0 ? TransferSyntax.ImplicitVRLittleEndian
					: TransferSyntax.ImplicitVRBigEndian;
		}
	}

	public int read() throws IOException {
		int ch = in.read();
		if (ch != -1) {
			++pos;
		}
		return ch;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int result = in.read(b, off, len);
		if (result != -1) {
			pos += result;
		}
		return result;
	}

	public void mark(int readlimit) {
		in.mark(readlimit);
		markedPos = pos;
	}

	public void reset() throws IOException {
		in.reset();
		pos = markedPos;
	}

	public long skip(long n) throws IOException {
		long result = in.skip(n);
		if (result > 0) {
			pos += result;
		}
		return result;
	}

	public final void readFully(byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}

	public int readHeader() throws IOException {
		tagPos = pos;
		readFully(header, 0, 8);
		tag = ts.bigEndian() ? ByteUtils.bytesBE2tag(header, 0) : ByteUtils
				.bytesLE2tag(header, 0);
		if (tag == Tag.Item 
				|| tag == Tag.ItemDelimitationItem 
				|| tag == Tag.SequenceDelimitationItem) {
			vr = null;
		} else if (!ts.explicitVR()) {
			vr = attrs.vrOf(tag);
		} else {
			try {
				vr = VR.valueOf(((header[4] & 0xff) << 8) | (header[5] & 0xff));
			} catch (IllegalArgumentException e) {
				vr = attrs.vrOf(tag);
			}
			if (vr.explicitVRHeaderLength() == 8) {
				vallen = ts.bigEndian() ? ByteUtils.bytesBE2ushort(header, 6)
						: ByteUtils.bytesLE2ushort(header, 6);
				return tag;
			}
			readFully(header, 4, 4);
		}
		vallen = ts.bigEndian() ? ByteUtils.bytesBE2int(header, 4) : ByteUtils
				.bytesLE2int(header, 4);
		return tag;
	}

	public void readItem(AttributeSet dest)
			throws IOException {
		dest.setItemOffset(pos);
		if (readHeader() != Tag.Item)
			throw new IOException("Expected (FFFE,E000) but read " 
					+ TagUtils.toString(tag));
		readAttributeSet(dest, vallen);
	}
	
	public void readAttributeSet(AttributeSet dest, int len)
			throws IOException {
		AttributeSet oldAttrs = attrs;
		this.attrs = dest;
		try {
			parse(len, Tag.ItemDelimitationItem);
		} finally {
			this.attrs = oldAttrs;
		}
	}
	
	private void parse(int len, int endTag) throws IOException {
		long endPos = len == -1 ? Long.MAX_VALUE : pos + (len & 0xffffffffL);
		boolean quit = false;
		int tag0 = 0;
		while (!quit && tag0 != endTag && pos < endPos) {
			try {
				tag0 = readHeader();
			} catch (EOFException e) {
				if (len != -1)
                    throw e;
                // treat EOF like read of ItemDelimitationItem
                tag0 = tag = Tag.ItemDelimitationItem;
                vr = null;
                vallen = 0;
			}
			quit = !handler.readValue(this);
			if (!ignoreFmiTs && pos == fmiEndPos) {
				switchTransferSyntax(attrs.getTransferSyntax());
			}
		}
	}

	private void switchTransferSyntax(TransferSyntax ts) {
		if (this.ts.isDeflated())
			throw new IllegalStateException(
					"Cannot switch back from Deflated TS");
		if (ts.isDeflated())
			in = new InflaterInputStream(in);
		this.ts = ts;
	}

	public boolean readValue(DicomInputStream dis) throws IOException {
		if (dis != this)
			throw new IllegalArgumentException("dis != this");
		switch (tag) {
		case Tag.Item:
			BasicAttribute sq = (BasicAttribute) sqStack
					.get(sqStack.size() - 1);
			logAttr(sq.countItems() + 1, vr);
			if (vallen == -1) {
				if (sq.vr() != VR.SQ) {
					sq.fragmentsToSequence(attrs);
				}
			}
			if (sq.vr() == VR.SQ) {
				BasicAttributeSet item = new BasicAttributeSet();
				item.setParent(attrs);
				item.setItemOffset(pos-8);
				readAttributeSet(item, vallen);
				sq.addItem(item);
			} else {
				sq.addBytes(readBytes(vallen));
			}
			break;
		case Tag.ItemDelimitationItem:
			logAttr(-1, vr);
			if (vallen > 0) {
				log
						.warn("Item Delimitation Item (FFFE,E00D) with non-zero Item Length:"
								+ vallen
								+ " at pos: "
								+ tagPos
								+ " - try to skip length");
				skip(vallen);
			}
			break;
		case Tag.SequenceDelimitationItem:
			logAttr(-1, vr);
			if (vallen > 0) {
				log
						.warn("Sequence Delimitation Item (FFFE,E0DD) with non-zero Item Length:"
								+ vallen
								+ " at pos: "
								+ tagPos
								+ " - try to skip length");
				skip(vallen);
			}
			break;
		default:
			if (vallen == -1 || vr == VR.SQ) {
				Attribute a = vr == VR.SQ ? attrs.putSequence(tag) : attrs
						.putFragments(tag, vr, ts.bigEndian());
				logAttr(-1, a.vr());
				if (sqStack == null) { // lazy creation
					sqStack = new ArrayList();
				}
				sqStack.add(a);
				try {
					parse(vallen, Tag.SequenceDelimitationItem);
				} finally {
					sqStack.remove(sqStack.size() - 1);
				}
			} else {
				Attribute a = attrs.putBytes(tag, vr, ts.bigEndian(),
						readBytes(vallen));
				logAttr(-1, a.vr());
				if (tag == 0x00020000) {
					fmiEndPos = pos + a.getInt(false);
				}
			}
		}
		return true;
	}

	private void logAttr(int itemIndex, VR vr1) {
		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append(tagPos).append(": ");
			AttributeSet p = attrs;
			while ((p = p.getParent()) != null)
				sb.append('>');            
			TagUtils.toStringBuffer(tag, sb);
			if (itemIndex > 0) {
				sb.append("[").append(itemIndex).append("]");
			}
			if (vr1 != null) {
				sb.append(" ").append(vr);
			}
			sb.append(" #").append(vallen);
			log.debug(sb.toString());
		}
	}

	public byte[] readBytes(int vallen) throws IOException {
		if (vallen == 0)
			return null;
		if (vallen > vallenLimit) {
			if (skip(vallen) != vallen) {
				throw new IOException("Failed to skip " + vallen + " bytes");
			}
			return null;
		}
		byte[] val = new byte[vallen];
		readFully(val, 0, vallen);
		return val;
	}

	public static void main(String[] args) throws IOException {
		readFiles(args, true);
		long start = System.currentTimeMillis();
		readFiles(args, false);
		long end = System.currentTimeMillis();
		System.out.println("Reading " + args.length + " objects takes "
				+ ((end - start) / 1000f) + "s [= "
				+ ((float) (end - start) / args.length) + "ms]");
	}

	private static void readFiles(String[] args, boolean logmem)
			throws IOException {
		if (logmem)
			logmem();
		AttributeSet dataset;
		for (int i = 0; i < args.length; i++) {
			File f = new File(args[i]);
			DicomInputStream dis = new DicomInputStream(f);
			dataset = new BasicAttributeSet();
			dis.readAttributeSet(dataset, -1);
			dis.close();
			if (logmem)
				logmem();
		}
	}

	private static long prevUsed = 0L;

	private static void logmem() {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		long free = rt.freeMemory();
		long total = rt.totalMemory();
		long max = rt.maxMemory();
		long used = total - free;
		System.out.println("used: " + (used / 1024f) + "[+"
				+ +((used - prevUsed) / 1024f) + "]K, free: " + (free / 1024f)
				+ "K, total: " + (total / 1024f) + "K, max: " + (max / 1024f)
				+ "K");
		prevUsed = used;
	}
}