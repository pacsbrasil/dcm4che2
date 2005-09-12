/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.IntHashtable;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 25, 2005
 *
 */
public class DicomDirReader {
	protected final RandomAccessFile raf;
	protected DicomInputStream in;
	protected DicomObject fileSetInfo;
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
	
	public DicomObject getFileSetInfo()
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
		fileSetInfo = new BasicDicomObject();
		in.readDicomObject(fileSetInfo, -1);
		in.setHandler(in);
	}
	
	public DicomObject findFirstRootRecord()
			throws IOException {
		return findFirstMatchingRootRecord(null, false);
	}

	public DicomObject findFirstMatchingRootRecord(DicomObject keys,
			boolean ignoreCaseOfPN)
			throws IOException {
		init();
		return readRecord(fileSetInfo.getInt(
					Tag.OffsetoftheFirstDirectoryRecordoftheRootDirectoryEntity),
					keys, ignoreCaseOfPN);
	}

	public DicomObject findNextSiblingRecord(DicomObject prevRecord)
			throws IOException {
		return findNextMatchingSiblingRecord(prevRecord, null, false);
	}
	
	public DicomObject findNextMatchingSiblingRecord(DicomObject prevRecord,
			DicomObject keys, boolean ignoreCaseOfPN)
			throws IOException {
		init();
		return readRecord(prevRecord.getInt(
					Tag.OffsetoftheNextDirectoryRecord),
					keys, ignoreCaseOfPN);
	}

	public DicomObject findFirstChildRecord(DicomObject parentRecord)
			throws IOException {
		return findFirstMatchingChildRecord(parentRecord, null, false);
	}

	public DicomObject findFirstMatchingChildRecord(DicomObject parentRecord,
			DicomObject keys, boolean ignoreCaseOfPN)
			throws IOException {
		init();
		return readRecord(parentRecord.getInt(
				Tag.OffsetofReferencedLowerLevelDirectoryEntity),
				keys, ignoreCaseOfPN);
	}

	private DicomObject readRecord(int offset, DicomObject keys,
			boolean ignoreCaseOfPN)
			throws IOException {
		while (offset != 0) {
			DicomObject item = (DicomObject) cache.get(offset);
			if (item == null) {
				final long l = offset & 0xffffffffL;
				raf.seek(l);
				in.setStreamPosition(l);
				item = new BasicDicomObject();
				in.readItem(item);
				cache.put(offset, item);
			}
			if ((showInactiveRecords || item.getInt(Tag.RecordInuseFlag) != 0)
					&& (keys == null || item.matches(keys, ignoreCaseOfPN)))
				return item;
			offset = item.getInt(Tag.OffsetoftheNextDirectoryRecord);
		}
		return null;
	}

	public void close() throws IOException {
		raf.close();
	}

}
