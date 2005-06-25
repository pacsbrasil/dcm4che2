/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Jun 25, 2005
 *
 */
public class DicomDirReader {
	protected final RandomAccessFile raf;
	protected DicomInputStream in;
	protected AttributeSet fileSetInfo;
	protected boolean showInactiveRecords;
	
	public DicomDirReader(File f) throws FileNotFoundException {
		this(new RandomAccessFile(f, "r"));
	}
	
	public DicomDirReader(RandomAccessFile raf) {
		this.raf = raf;		
	}

	public final boolean isShowInactiveRecords() {
		return showInactiveRecords;
	}

	public final void setShowInactiveRecords(boolean showInactiveRecords) {
		this.showInactiveRecords = showInactiveRecords;
	}

	private void init()
			throws IOException {
		if (in != null)
			return;
		in = new DicomInputStream(raf);
		in.setHandler(new StopTagInputHandler(Tag.DirectoryRecordSequence));
		fileSetInfo = new BasicAttributeSet();
		in.readAttributeSet(fileSetInfo, -1);
		in.setHandler(in);
	}
	
	public AttributeSet readFirstRootRecord()
			throws IOException {
		init();
		return readRecord(fileSetInfo.getInt(
				Tag.OffsetoftheFirstDirectoryRecordoftheRootDirectoryEntity));
	}

	public AttributeSet readNextSiblingRecord(AttributeSet prevRecord)
			throws IOException {
		init();
		return readRecord(prevRecord.getInt(
				Tag.OffsetoftheNextDirectoryRecord));
	}
	
	public AttributeSet readFirstChildRecord(AttributeSet parentRecord)
			throws IOException {
		init();
		return readRecord(parentRecord.getInt(
				Tag.OffsetofReferencedLowerLevelDirectoryEntity));
	}

	private AttributeSet readRecord(int offset)
			throws IOException {
		BasicAttributeSet attrs = new BasicAttributeSet();
		while (offset != 0) {
			final long l = offset & 0xffffffffL;
			raf.seek(l);
			in.setStreamPosition(l);
			BasicAttributeSet item = new BasicAttributeSet();
			in.readItem(item);
			if (showInactiveRecords || item.getInt(Tag.RecordInuseFlag) != 0)
				return item;
			offset = item.getInt(Tag.OffsetoftheNextDirectoryRecord);
		}
		return null;
	}

	public void close() throws IOException {
		raf.close();
	}

}
