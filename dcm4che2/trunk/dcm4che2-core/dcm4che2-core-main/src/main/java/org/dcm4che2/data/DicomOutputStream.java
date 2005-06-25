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
import java.util.LinkedList;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.dcm4che2.util.ByteUtils;

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

	public void writeCommand(AttributeSet attrs)
			throws IOException {
		this.ts = TransferSyntax.ImplicitVRLittleEndian;
		writeAttributes(attrs.iterator(CMD_FIRST_TAG, CMD_LAST_TAG), 
				new ItemInfo(attrs.iterator(CMD_FIRST_TAG, CMD_LAST_TAG)), true);		
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
		writeAttributes(attrs.iterator(FMI_FIRST_TAG, FMI_LAST_TAG), 
				new ItemInfo(attrs.iterator(FMI_FIRST_TAG, FMI_LAST_TAG)), true);
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
		writeAttributes(attrs.iterator(DATASET_FIRST_TAG, DATASET_LAST_TAG),
				createItemInfo(attrs), includeGroupLength);
	}

	private boolean needItemInfo() {
		return includeGroupLength 
				|| explicitItemLength 
				|| explicitSequenceLength;
	}
		
	private ItemInfo createItemInfo(AttributeSet attrs) {
		if (needItemInfo())
			return new ItemInfo(attrs.iterator(DATASET_FIRST_TAG,
					DATASET_LAST_TAG));
		return null;
	}

	public void writeItem(AttributeSet item, TransferSyntax ts)
			throws IOException {
		this.ts = ts;
		writeItem(item, createItemInfo(item));
	}

	private void writeItem(AttributeSet item, ItemInfo itemInfo)
			throws IOException {
		// TODO
		// item.setItemOffset(getStreamPosition());
		int len;
		if (item.isEmpty()) {
			len = explicitItemLengthIfZero ? 0 : -1;
		} else {
			len = explicitItemLength ? itemInfo.len : -1;
		}
		writeHeader(ITEM_TAG, null, len);
		writeAttributes(item.iterator(), itemInfo, includeGroupLength);
		if (len == -1) {
			writeHeader(ITEM_DELIM_TAG, null, 0);
		}
	}
	
	void writeAttributes(Iterator itr, ItemInfo itemInfo, boolean includeGroupLength)
			throws IOException {
		int gggg0 = -1;
		int gri = -1;
		int sqi = -1;
		while (itr.hasNext()) {
			Attribute a = (Attribute) itr.next();
			int gggg = a.tag() & 0xffff0000;
			if (includeGroupLength) {
				if (gggg != gggg0) {
					gggg0 = gggg;
					writeGroupLength(gggg, itemInfo.grlen[++gri]);
				}
			}
			final VR vr = a.vr();
			int len = a.length();
			if (vr == VR.SQ) {
				if (len == -1 && explicitSequenceLength) {
					len = itemInfo.sqlen[++sqi];
				} else if (len == 0 && !explicitSequenceLengthIfZero) {
					len = -1;
				}
			}
			writeHeader(a.tag(), vr, len);
			a.bigEndian(ts.bigEndian());
			if (a.hasItems()) {
				if (vr == VR.SQ) {
					for (int i = 0, n = a.countItems(); i < n; i++) {
						AttributeSet item = a.getItem(i);
						ItemInfo childItemInfo = itemInfo != null ? 
								(ItemInfo) itemInfo.childs.removeFirst() : null;
						writeItem(item, childItemInfo);
					}
				} else {
					for (int i = 0, n = a.countItems(); i < n; i++) {
						byte[] val = a.getBytes(i);
						writeHeader(ITEM_TAG, null, (val.length + 1) & ~1);
						write(val);
						if ((val.length & 1) != 0)
							write(0);
					}
				}
			} else if (len > 0) {
				byte[] val = a.getBytes();
				write(val);
				if ((val.length & 1) != 0)
					write(vr.padding());
			}
			if (len == -1) {
				writeHeader(SEQ_DELIM_TAG, null, 0);			
			}
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

	private class ItemInfo {
		int len = 0;
		int[] grlen = { 0 };
		int[] sqlen = {};
		LinkedList childs = null;
		
		ItemInfo(Iterator it) {
			int gggg0 = -1;
			int gri = -1;
			int sqi = -1;
			while (it.hasNext()) {
				Attribute a = (Attribute) it.next();
				final VR vr = a.vr();
				int vlen = a.length();
				if (vlen == -1) {
					if (a.vr() == VR.SQ) {
						vlen = calcItemSqLen(a);
						if (explicitSequenceLength) {
							if (++sqi >= sqlen.length) {
								sqlen = realloc(sqlen);
							}
							sqlen[sqi] = vlen;
						}
					} else {
						vlen = calcFragSqLen(a);
					}
				} else if (a.vr() == VR.SQ) { // vlen == 0
					if (!explicitSequenceLengthIfZero)
						vlen = 8;
				}
				final int alen = 
					(ts.explicitVR() ? vr.explicitVRHeaderLength() : 8) + vlen;
				len += alen;
				final int gggg = a.tag() & 0xffff0000;
				if (gggg == CMD_GROUPLENGTH_TAG || gggg == FMI_GROUPLENGTH_TAG 
						|| includeGroupLength) {
					if (gggg != gggg0) {
						gggg0 = gggg;
						len += 12;
						if (++gri >= grlen.length) {
							grlen = realloc(grlen);
						}
					}
					grlen[gri] += alen;
				}
			}
			if (!(len == 0 ? explicitItemLengthIfZero : explicitItemLength)) {
				len += 8;
			}
		}

		private int calcFragSqLen(Attribute a) {
			int l = 8;
			for (int i = 0, n = a.countItems(); i < n; ++i) {
				byte[] b = a.getBytes(i);
				l += 8 + (b.length + 1) & ~1; 
			}
			return l;
		}

		private int calcItemSqLen(Attribute a) {
			int l = explicitSequenceLength ? 0 : 8;
			for (int i = 0, n = a.countItems(); i < n; ++i) {
				AttributeSet item = a.getItem(i);
				ItemInfo itemInfo = new ItemInfo(item.iterator());
				if (childs == null) // lazy allocation
					childs = new LinkedList();
				childs.add(itemInfo);
				l += 8 + itemInfo.len;
			}
			return l;
		}

		private int[] realloc(int[] src) {
			int[] dest = new int[src.length + 10];
			System.arraycopy(src, 0, dest, 0,  src.length);
			return dest;
		}
	}

}
