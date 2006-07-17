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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4che2.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.FileSetInformation;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.util.ByteUtils;
import org.dcm4che2.util.TagUtils;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since 06.07.2006
 */

public class DicomDirWriter extends DicomDirReader {

    protected final boolean explRecordSeqLen;    
    protected final long firstRecordPos;
    protected final byte[] dirInfoHeader = { 
            0x04, 0x00, 0x00, 0x12, 'U', 'L', 4, 0, 0, 0, 0, 0,
            0x04, 0x00, 0x02, 0x12, 'U', 'L', 4, 0, 0, 0, 0, 0,
            0x04, 0x00, 0x12, 0x12, 'U', 'S', 2, 0, 0, 0,
            0x04, 0x00, 0x30, 0x12, 'S', 'Q', 0, 0, 0, 0, 0, 0
    };
    protected final byte[] dirRecordHeader = {
            0x04, 0x00, 0x00, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0,
            0x04, 0x00, 0x10, 0x14, 'U', 'S', 2, 0, 0, 0,
            0x04, 0x00, 0x20, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0,            
    };
    protected long recordSeqLen;
    protected long rollbackLen = -1;
    protected ArrayList dirtyRecords = new ArrayList();
    protected final DicomOutputStream out;
    private static final Comparator offsetComparator = new Comparator(){

        public int compare(Object o1, Object o2) {
            DicomObject item1 = (DicomObject) o1; 
            DicomObject item2 = (DicomObject) o2;
            long d = item1.getItemOffset() - item2.getItemOffset();
            return d < 0 ? -1 : d > 0 ? 1 : 0;
        }};
    
    public DicomDirWriter(File file) throws IOException {
        super(new RandomAccessFile(checkExists(file), "rw"));
        this.file = file;
        out = new DicomOutputStream(raf);
        offsetFirstRootRecord(fileSetInfo.getOffsetFirstRootRecord());
        offsetLastRootRecord(fileSetInfo.getOffsetLastRootRecord());
        this.firstRecordPos = in.getStreamPosition();
        this.recordSeqLen = in.valueLength();
        this.explRecordSeqLen = recordSeqLen != -1;
        if (fileSetInfo.isEmpty()) {
            this.recordSeqLen = 0;
        }
    }

    private static File checkExists(File f) throws FileNotFoundException {
        if (!f.isFile()) {
            throw new FileNotFoundException(f.getPath());
        }
        return f;
    }

    public DicomDirWriter(File file, FileSetInformation fileSetInfo,
            boolean explRecordSeqLen)
        throws IOException {
        super(new RandomAccessFile(file, "rw"), fileSetInfo);
        this.file = file;
        raf.setLength(0);
        out = new DicomOutputStream(raf);
        out.writeDicomFile(fileSetInfo.getDicomObject());
        out.writeHeader(Tag.DirectoryRecordSequence, VR.SQ,
                explRecordSeqLen ? 0 : -1);
        this.firstRecordPos = (int) out.getStreamPosition();
        this.explRecordSeqLen = explRecordSeqLen;
        this.recordSeqLen = 0;
        if (!explRecordSeqLen) {
            out.writeHeader(Tag.SequenceDelimitationItem, null, 0);                
        }
    }

    private void offsetFirstRootRecord(int val) {
        ByteUtils.int2bytesLE(val, dirInfoHeader, 8);        
    }

    private int offsetFirstRootRecord() {
        return ByteUtils.bytesLE2int(dirInfoHeader, 8);        
    }
    
    private void offsetLastRootRecord(int val) {
        ByteUtils.int2bytesLE(val, dirInfoHeader, 20);        
    }

    private int offsetLastRootRecord() {
        return ByteUtils.bytesLE2int(dirInfoHeader, 20);        
    }
    
    private void recordSeqLen(int val) {
        ByteUtils.int2bytesLE(val, dirInfoHeader, 42);        
    }
        
    public synchronized void addRootRecord(DicomObject rec) throws IOException {
        DicomObject lastRootRecord = findLastRootRecord();
        if (lastRootRecord == null) {
            writeRecord(firstRecordPos, rec);
            fileSetInfo.setOffsetFirstRootRecord((int) firstRecordPos);
            fileSetInfo.setOffsetLastRootRecord((int) firstRecordPos);
        } else {
            addSiblingRecord(lastRootRecord, rec);
        }
    }
    
    public synchronized DicomObject addPatientRecord(DicomObject patrec)
    throws IOException {
        DicomObject other = findPatientRecord(patrec.getString(Tag.PatientID));
        if (other != null) {
            return other;
        }
        addRootRecord(patrec);
        return patrec;
    }

    public synchronized void addSiblingRecord(DicomObject prevRec,
            DicomObject dcmobj)
    throws IOException {
        addRecord(Tag.OffsetoftheNextDirectoryRecord, prevRec, dcmobj);
        if (prevRec.getItemOffset() == fileSetInfo.getOffsetLastRootRecord()) {
            fileSetInfo.setOffsetLastRootRecord((int) dcmobj.getItemOffset());
        }
    }

    public synchronized void addChildRecord(DicomObject parentRec,
            DicomObject dcmobj) throws IOException {
        addRecord(Tag.OffsetofReferencedLowerLevelDirectoryEntity, parentRec,
                dcmobj);
    }

    public synchronized DicomObject addStudyRecord(DicomObject patrec, 
            DicomObject styrec) throws IOException {
        DicomObject other = findStudyRecord(patrec,
                styrec.getString(Tag.StudyInstanceUID));
        if (other != null) {
            return other;
        }
        addChildRecord(patrec, styrec);
        return styrec;
    }

    public synchronized DicomObject addSeriesRecord(DicomObject styrec, 
            DicomObject serrec) throws IOException {
        DicomObject other = findSeriesRecord(styrec,
                serrec.getString(Tag.SeriesInstanceUID));
        if (other != null) {
            return other;
        }
        addChildRecord(styrec, serrec);
        return serrec;
    }
    
    public synchronized void deleteRecord(DicomObject rec)
    throws IOException {
        for (DicomObject child = findFirstChildRecord(rec);
            child != null; child = findNextSiblingRecord(child)) {
            deleteRecord(rec);
        }
        rec.putInt(Tag.RecordInuseFlag, VR.US, 0xffff);
        markAsDirty(rec);
    }
    
    public synchronized void rollback() throws IOException {
        fileSetInfo.setOffsetFirstRootRecord(offsetFirstRootRecord());
        fileSetInfo.setOffsetLastRootRecord(offsetLastRootRecord());
        cache.clear();
        dirtyRecords.clear();
        if (rollbackLen != -1) {
            recordSeqLen = rollbackLen - firstRecordPos;
            raf.seek(rollbackLen);
            if (!explRecordSeqLen) {
                out.writeHeader(Tag.SequenceDelimitationItem, null, 0);                
            }
            raf.setLength(raf.getFilePointer());
            rollbackLen = -1;
        }
    }
    
    public synchronized void commit() throws IOException {
        if (rollbackLen != -1 && !explRecordSeqLen) {
            raf.seek(endPos());
            out.writeHeader(Tag.SequenceDelimitationItem, null, 0);                
        }
        if (updateDirInfoHeader()) {
            raf.seek(firstRecordPos - dirInfoHeader.length);
            raf.write(dirInfoHeader, 0, 
                    explRecordSeqLen ? dirInfoHeader.length : 24);
        } else if (rollbackLen != -1 && explRecordSeqLen) {
            raf.seek(firstRecordPos - 4);
            raf.write(dirInfoHeader, dirInfoHeader.length - 4, 4); 
        }
        rollbackLen = -1;
        for (int i = 0, n = dirtyRecords.size(); i < n; i++) {
            writeDirRecordHeader((DicomObject) dirtyRecords.get(i));
        }
        dirtyRecords.clear();
    }

    public void close() throws IOException {
        commit();
        super.close();
    }
    
    private void writeDirRecordHeader(DicomObject rec) throws IOException {
        ByteUtils.int2bytesLE(rec.getInt(Tag.OffsetoftheNextDirectoryRecord),
                dirRecordHeader, 8);        
        ByteUtils.ushort2bytesLE(rec.getInt(Tag.RecordInuseFlag),
                dirRecordHeader, 20);        
        ByteUtils.int2bytesLE(
                rec.getInt(Tag.OffsetofReferencedLowerLevelDirectoryEntity),
                dirRecordHeader, 30);        
        raf.seek(rec.getItemOffset());
        raf.write(dirRecordHeader); 
    }

    private boolean updateDirInfoHeader() {
        boolean update = false;
        if (offsetFirstRootRecord() != fileSetInfo.getOffsetFirstRootRecord()) {
            offsetFirstRootRecord(fileSetInfo.getOffsetFirstRootRecord());
            update = true;
        }
        if (offsetLastRootRecord() != fileSetInfo.getOffsetLastRootRecord()) {
            offsetLastRootRecord(fileSetInfo.getOffsetLastRootRecord());
            update = true;
        }
        recordSeqLen((int) recordSeqLen);
        return update;
    }    
    
    private void addRecord(int tag, DicomObject prevRecord, DicomObject dcmobj) throws IOException {
        long endPos = endPos();
        writeRecord(endPos, dcmobj);
        prevRecord.putInt(tag, VR.UL, (int) endPos);
        markAsDirty(prevRecord);
        cache.put((int) dcmobj.getItemOffset(), dcmobj);
    }

    private long endPos() throws IOException {
        if (recordSeqLen == -1) {
            long endPos = raf.length() - 12;
            raf.seek(endPos);
            if (in.readHeader() == Tag.SequenceDelimitationItem) {
                recordSeqLen = (int) (endPos - firstRecordPos);
            } else {
                endPos = fileSetInfo.getOffsetLastRootRecord();
                raf.seek(endPos);
                in.setStreamPosition(endPos);
                DicomObject dcmobj = new BasicDicomObject();
                while (in.readHeader() == Tag.Item) {
                    in.readDicomObject(dcmobj, in.valueLength());
                    dcmobj.clear();
                    endPos = in.getStreamPosition();
                }
                if (in.tag() != Tag.SequenceDelimitationItem) {
                    throw new IOException("Unexpected Tag "
                            + TagUtils.toString(in.tag()) + " at offset " 
                            + endPos);
                }
                recordSeqLen = (int) (endPos - firstRecordPos);
            }
        }
        return firstRecordPos + recordSeqLen;
    }
        
    private void markAsDirty(DicomObject rec) {
        int index = Collections.binarySearch(dirtyRecords, rec, 
                offsetComparator);
        if (index < 0) {
            dirtyRecords.add(-(index+1), rec);
        }        
    }

    private void writeRecord(long offset, DicomObject dcmobj) throws IOException {
        raf.seek(offset);
        out.setStreamPosition(offset);
        if (rollbackLen == -1) {
            rollbackLen = offset;
        }
        dcmobj.putInt(Tag.OffsetoftheNextDirectoryRecord, VR.UL, 0);
        dcmobj.putInt(Tag.RecordInuseFlag, VR.US, 0);
        dcmobj.putInt(Tag.OffsetofReferencedLowerLevelDirectoryEntity, VR.UL, 0);
        out.writeItem(dcmobj, in.getTransferSyntax());
        recordSeqLen = (int) (out.getStreamPosition() - firstRecordPos);        
    }


}
