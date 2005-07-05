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

import org.dcm4che2.util.IntHashtable;

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
	protected IntHashtable cache = new IntHashtable();
	
	public DicomDirReader(File f) throws FileNotFoundException {
		this(new RandomAccessFile(f, "r"));
	}
	
	public DicomDirReader(RandomAccessFile raf) {
		this.raf = raf;		
	}
	
	public void clearCache() {
		cache.clear();
	}
	
	public AttributeSet getFileSetInfo()
			throws IOException {
		init();
		return fileSetInfo;
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
	
	public AttributeSet findFirstRootRecord()
			throws IOException {
		return findFirstMatchingRootRecord(null, false);
	}

	public AttributeSet findFirstMatchingRootRecord(AttributeSet filter,
			boolean ignoreCaseOfPN)
			throws IOException {
		init();
		return readRecord(fileSetInfo.getInt(
					Tag.OffsetoftheFirstDirectoryRecordoftheRootDirectoryEntity),
					filter, ignoreCaseOfPN);
	}

	public AttributeSet findNextSiblingRecord(AttributeSet prevRecord)
			throws IOException {
		return findNextMatchingSiblingRecord(prevRecord, null, false);
	}
	
	public AttributeSet findNextMatchingSiblingRecord(AttributeSet prevRecord,
			AttributeSet filter, boolean ignoreCaseOfPN)
			throws IOException {
		init();
		return readRecord(prevRecord.getInt(
					Tag.OffsetoftheNextDirectoryRecord),
					filter, ignoreCaseOfPN);
	}

	public AttributeSet findFirstChildRecord(AttributeSet parentRecord)
			throws IOException {
		return findFirstMatchingChildRecord(parentRecord, null, false);
	}

	public AttributeSet findFirstMatchingChildRecord(AttributeSet parentRecord,
			AttributeSet filter, boolean ignoreCaseOfPN)
			throws IOException {
		init();
		return readRecord(parentRecord.getInt(
				Tag.OffsetofReferencedLowerLevelDirectoryEntity),
				filter, ignoreCaseOfPN);
	}

	private AttributeSet readRecord(int offset, AttributeSet filter,
			boolean ignoreCaseOfPN)
			throws IOException {
		while (offset != 0) {
			AttributeSet item = (AttributeSet) cache.get(offset);
			if (item == null) {
				final long l = offset & 0xffffffffL;
				raf.seek(l);
				in.setStreamPosition(l);
				item = new BasicAttributeSet();
				in.readItem(item);
			}
			if ((showInactiveRecords || item.getInt(Tag.RecordInuseFlag) != 0)
					&& (filter == null || filter.match(item, ignoreCaseOfPN)))
				return item;
			offset = item.getInt(Tag.OffsetoftheNextDirectoryRecord);
		}
		return null;
	}

	public void close() throws IOException {
		raf.close();
	}

}
