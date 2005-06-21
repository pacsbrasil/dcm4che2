/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.dcm4che2.util.ByteUtils;
import org.dcm4che2.util.TagUtils;

public class DicomOutputStream 
	extends FilterOutputStream {
	
	private static Logger log = Logger.getLogger(DicomOutputStream.class);

	private static final int CMD_GROUPLENGTH_TAG = 0x00000000;
	private static final int CMD_FIRST_TAG = 0x00000001;
	private static final int CMD_LAST_TAG = 0x0000ffff;
	private static final int FMI_GROUPLENGTH_TAG = 0x00020000;	
	private static final int FMI_FIRST_TAG = 0x00020001;
	private static final int FMI_LAST_TAG = 0x0002ffff;
	private static final int DATASET_FIRST_TAG = 0x00030001;
	private static final int DATASET_LAST_TAG = 0xffffffff;
	private static final int SEQ_DELIM_TAG = 0xfffee0dd;
	private static final int ITEM_DELIM_TAG = 0xfffee00d;
	private static final int ITEM_TAG = 0xfffee000;
	private static final int PREAMBLE_LENGTH = 128;
	
	private TransferSyntax ts = TransferSyntax.ExplicitVRLittleEndian;
	private boolean includeGroupLength = false;
	private boolean explicitItemLength = false;
	private boolean explicitSequenceLength = false;
	private boolean explicitItemLengthIfZero = true;
	private boolean explicitSequenceLengthIfZero = true;
	
	byte[] header = new byte[8];

	byte[] preamble = new byte[PREAMBLE_LENGTH];

	public DicomOutputStream(OutputStream out) {
		super(out);
	}
	
	public final boolean isExplicitItemLength() {
		return explicitItemLength;
	}

	public final void setExplicitItemLength(boolean explicitItemLength) {
		this.explicitItemLength = explicitItemLength;
	}

	public final boolean isExplicitItemLengthIfZero() {
		return explicitItemLengthIfZero;
	}

	public final void setExplicitItemLengthIfZero(boolean explicitItemLengthIfZero) {
		this.explicitItemLengthIfZero = explicitItemLengthIfZero;
	}

	public final boolean isExplicitSequenceLength() {
		return explicitSequenceLength;
	}

	public final void setExplicitSequenceLength(boolean explicitSequenceLength) {
		this.explicitSequenceLength = explicitSequenceLength;
	}

	public final boolean isExplicitSequenceLengthIfZero() {
		return explicitSequenceLengthIfZero;
	}

	public final void setExplicitSequenceLengthIfZero(
			boolean explicitSequenceLengthIfZero) {
		this.explicitSequenceLengthIfZero = explicitSequenceLengthIfZero;
	}

	public final boolean isIncludeGroupLength() {
		return includeGroupLength;
	}

	public final void setIncludeGroupLength(boolean includeGroupLength) {
		this.includeGroupLength = includeGroupLength;
	}

	void prepareWriteDataset(AbstractAttributeSet attrs) {
		if (includeGroupLength 
				|| explicitItemLength 
				|| explicitSequenceLength
				|| explicitItemLengthIfZero 
				|| explicitSequenceLengthIfZero) {
			calcItemLength(attrs);
		} else {
			resetItemLength(attrs);
		}
	}
		
	public void writeCommand(AttributeSet attrs)
			throws IOException {
		this.ts = TransferSyntax.ImplicitVRLittleEndian;
		writeGroupLength(CMD_GROUPLENGTH_TAG, calcCommandGroupLength(attrs));
		writeAttributes(attrs.iterator(CMD_FIRST_TAG, CMD_LAST_TAG));		
	}

	private void writeGroupLength(int tag, int length)
			throws IOException {
		writeHeader(tag, VR.UL, 4);
		write(VR.UL.toBytes(length, ts.bigEndian()), 0, 4);		
	}

	public void writeDicomFile(AttributeSet attrs)
			throws IOException {
		writeFileMetaInformation(attrs);
		writeDataset(attrs);		
	}
	
	public void writeFileMetaInformation(AttributeSet attrs)
			throws IOException {
		write(preamble, 0, 128);
		write('D');
		write('I');
		write('C');
		write('M');
		this.ts = TransferSyntax.ExplicitVRLittleEndian;
		writeGroupLength(FMI_GROUPLENGTH_TAG, calcFMIGroupLength(attrs));
		writeAttributes(attrs.iterator(FMI_FIRST_TAG, FMI_LAST_TAG));
	}

	public void writeDataset(AttributeSet attrs)
			throws IOException {
		writeDataset(attrs, attrs.getTransferSyntax());		
	}
	
	public void writeDataset(AttributeSet attrs, TransferSyntax ts)
			throws IOException {
		if (ts.isDeflated())
			out = new DeflaterOutputStream(out);
		this.ts = ts;
		prepareWriteDataset((AbstractAttributeSet) attrs);
		writeAttributes(attrs.iterator(DATASET_FIRST_TAG, DATASET_LAST_TAG));	
	}

	public void writeItem(AttributeSet item, TransferSyntax ts)
			throws IOException {
		this.ts = ts;
		prepareWriteDataset((AbstractAttributeSet) item);
		writeItem(item, ((AbstractAttributeSet) item).getItemLength());
	}

	private void writeItem(AttributeSet item, int itemLen)
			throws IOException {
		// TODO
		// item.setItemOffset(getStreamPosition());
		int len = itemLen;
		if (!(len == 0 ? explicitItemLengthIfZero : explicitItemLength)) {
			len = -1;
		}		
		writeHeader(ITEM_TAG, null, len);
		writeAttributes(item.iterator());
		if (len == -1) {
			writeHeader(ITEM_DELIM_TAG, null, 0);
		}
	}
	
	void writeAttributes(Iterator itr)
			throws IOException {
		while (itr.hasNext()) {
			writeAttribute((Attribute) itr.next());
		}
	}

	public void writeAttribute(Attribute attr)
			throws IOException {
		final int tag = attr.tag();
		if (TagUtils.isGroupLengthElement(tag)
				&& !includeGroupLength
				&& tag != CMD_GROUPLENGTH_TAG
				&& tag != FMI_GROUPLENGTH_TAG)
			return;
		final VR vr = attr.vr();
		int len = attr.length();
		if (vr == VR.SQ) {
			if (len == -1 && explicitSequenceLength) {
				len = calcSeqLength(attr, false);
			} else if (len == 0 && !explicitSequenceLengthIfZero) {
				len = -1;
			}
		}
		writeHeader(tag, vr, len);
		writeValue(attr, vr, len);
		if (len == -1) {
			writeHeader(SEQ_DELIM_TAG, null, 0);			
		}
	}

	private void writeValue(Attribute attr, final VR vr, int len)
			throws IOException {
		attr.bigEndian(ts.bigEndian());
		if (attr.hasItems()) {
			if (vr == VR.SQ) {
				for (int i = 0, n = attr.countItems(); i < n; i++) {
					AttributeSet item = attr.getItem(i);
					writeItem(item, ((AbstractAttributeSet) item).getItemLength());
				}
			} else {
				for (int i = 0, n = attr.countItems(); i < n; i++) {
					byte[] val = attr.getBytes(i);
					writeHeader(ITEM_TAG, null, (val.length + 1) & ~1);
					write(val);
					if ((val.length & 1) != 0)
						write(0);
				}
			}
		} else if (len > 0) {
			byte[] val = attr.getBytes();
			write(val);
			if ((val.length & 1) != 0)
				write(vr.padding());
		}
	}

	public void writeHeader(int tag, VR vr, int len)
			throws IOException {
		if (ts.bigEndian()) {
			ByteUtils.tag2bytesBE(tag, header, 0);
		} else {
			ByteUtils.tag2bytesLE(tag, header, 0);
		}
		int off = 0;
		if (vr != null && ts.explicitVR()) {
			ByteUtils.ushort2bytesBE(vr.code(), header, 4);
			if (vr.explicitVRHeaderLength() == 8) {
				if (ts.bigEndian()) {
					ByteUtils.ushort2bytesBE(len, header, 6);
				} else {
					ByteUtils.ushort2bytesLE(len, header, 6);
				}
				write(header, 0, 8);
				return;
			}
			header[6] = header[7] = 0;
			write(header, 0, 8);
			off = 4;
		}
		if (ts.bigEndian()) {
			ByteUtils.int2bytesBE(len, header, 4);
		} else {
			ByteUtils.int2bytesLE(len, header, 4);
		}
		write(header, off, 8 - off);
	}

	private int calcCommandGroupLength(AttributeSet attrs) {
		int len = 0;
		Attribute a;
		Iterator itr = attrs.iterator(0x00000001,0x0000ffff);
		while (itr.hasNext()) {
			a = (Attribute) itr.next();
			len += 8;
			len += a.length();
		}
		return len;
	}

	private int calcFMIGroupLength(AttributeSet attrs) {
		int len = 0;
		Attribute a;
		Iterator itr = attrs.iterator(0x00020001,0x0002ffff);
		while (itr.hasNext()) {
			a = (Attribute) itr.next();
			len += a.vr().explicitVRHeaderLength();
			len += a.length();
		}
		return len;
	}
	
	private void resetItemLength(AbstractAttributeSet attrs) {
		for (Iterator itr = attrs.iterator(); itr.hasNext();) {
			Attribute a = (Attribute) itr.next();
			final VR vr = a.vr();
			if (a.vr() == VR.SQ && a.hasItems()) {
				for (int i = 0, n = a.countItems(); i < n; ++i) {	
					resetItemLength((AbstractAttributeSet) a.getItem(i));
				}
			}
		}
		attrs.setItemLength(-1);
	}

	private int calcSeqLength(Attribute attr, boolean calcItemLength) {
		int len = 0;
		for (int i = 0, n = attr.countItems(); i < n; i++) {
			AbstractAttributeSet item = (AbstractAttributeSet) attr.getItem(i);
			int itemLen = calcItemLength ? calcItemLength(item)
					: item.getItemLength();
			len += 8;
			len += itemLen; 
			if (!(itemLen == 0 ? explicitItemLengthIfZero : explicitItemLength)) {
				len += 8;
			}
		}
		return len;
	}
	
	private int calcItemLength(AbstractAttributeSet attrs) {
		int len = 0;
		int glen = 0;
		int gggg0 = 0;
		AttributeSet glAttrs = null;
		if (includeGroupLength) {
			glAttrs = new BasicAttributeSet(); 
		}
		
		for (Iterator itr = attrs.iterator(); itr.hasNext();) {
			Attribute a = (Attribute) itr.next();
			if (TagUtils.isGroupLengthElement(a.tag())) continue;
			if (includeGroupLength) {
				final int gggg1 = a.tag() & 0xffff0000;
				if (gggg0 != gggg1) {
					if (glen > 0) {
						glAttrs.putInt(gggg0, VR.UL, glen);
						len += 12;
					}
					gggg0 = gggg1;
					glen = 0;
				}
			}
			final VR vr = a.vr();
			int l = ts.explicitVR() ? vr.explicitVRHeaderLength() : 8;
			int alen = a.length();
			if (vr == VR.SQ) {
				if (alen == -1) {
					l += calcSeqLength(a, true);
				}
				if (!(alen == 0 ? explicitSequenceLengthIfZero : explicitSequenceLength)) {
					l += 8;
				}
			} else if (alen == -1) { // data fragments
				for (int i = 0, n = a.countItems(); i < n; ++i) {
					byte[] b = a.getBytes(i);
					l += 8;
					l += (b.length + 1) & ~1; 
				}
				l += 8;
			} else {
				l += alen;
			}
			len += l;
			glen += l;
		}
		if (includeGroupLength) {
			if (glen > 0) {
				glAttrs.putInt(gggg0, VR.UL, glen);
				len += 12;
			}
			attrs.addAll(glAttrs);
		}
		attrs.setItemLength(len);
		return len;
	}

}
