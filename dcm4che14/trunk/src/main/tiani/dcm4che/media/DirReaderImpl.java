/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package tiani.dcm4che.media;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirRecord;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DirReaderImpl implements DirReader {
    
    static final DcmParserFactory pfactory = DcmParserFactory.getInstance();
    static final DcmObjectFactory factory = DcmObjectFactory.getInstance();
    protected final File file;
    protected final ImageInputStream in;
    protected final DcmParser parser;
    protected Dataset fsi;
    protected int offFirstRootRec;
    protected int offLastRootRec;
    protected int seqLength;
    protected long offFirstRootRecValPos;
    protected long offLastRootRecValPos;
    protected long seqValuePos;

    /** Creates a new instance of DirReaderImpl */
    DirReaderImpl(File file, ImageInputStream in) {
        this.file = file != null ? file.getAbsoluteFile() : null;
        this.in = in;
        this.parser = pfactory.newDcmParser(in);
    }
    
    DirReaderImpl initReader() throws IOException {
        this.fsi = factory.newDataset();
        parser.setDcmHandler(fsi.getDcmHandler());
        this.seqValuePos =
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.DirectoryRecordSeq);
        parser.setDcmHandler(null);
        if (parser.getReadTag() != Tags.DirectoryRecordSeq) {
            throw new DcmValueException("Missing Directory Record Sequence");
        }
        this.seqLength = parser.getReadLength();

        DcmElement offFirstRootRecElem =
                fsi.get(Tags.OffsetOfFirstRootDirectoryRecord);
        if (offFirstRootRecElem == null || offFirstRootRecElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Offset of First Directory Record");
        }
        this.offFirstRootRec = offFirstRootRecElem.getInt();
        this.offFirstRootRecValPos = offFirstRootRecElem.getStreamPosition() + 8;

        DcmElement offLastRootRecElem =
                fsi.get(Tags.OffsetOfLastRootDirectoryRecord);
        if (offLastRootRecElem == null || offLastRootRecElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Offset of Last Directory Record");
        }
        this.offLastRootRec = offLastRootRecElem.getInt();
        this.offLastRootRecValPos = offLastRootRecElem.getStreamPosition() + 8;
        return this;
    }

    public Dataset getFileSetInfo() {
        return fsi;
    }

    public File getRefFile(File root, String[] fileIDs) {
        File retval = new File(root, fileIDs[0]);
        for (int i = 1; i < fileIDs.length; ++i) {
            retval = new File(retval, fileIDs[i]);
        }
        return retval;
    }

    public File getRefFile(String[] fileIDs) {
        if (file == null) {
            throw new IllegalStateException("Unkown root directory");
        }
        return getRefFile(file.getParentFile(), fileIDs);
    }

    public File getDescriptorFile(File root) throws DcmValueException {
        String[] fileID = fsi.getStrings(Tags.FileSetDescriptorFileID);
        if (fileID == null || fileID.length == 0) {
            return null;
        }
        return getRefFile(root, fileID);
    }

    public File getDescriptorFile() throws DcmValueException {
        if (file == null) {
            throw new IllegalStateException("Unkown root directory");
        }
        return getDescriptorFile(file.getParentFile());
    }

    public boolean isEmpty() {
        return offFirstRootRec == 0;
    }

    public DirRecord getFirstRecord() throws IOException {
        return offFirstRootRec != 0 
                ? new DirRecordImpl(parser, offFirstRootRec) : null;
    }

    public void close() throws IOException {
        in.close();
    }
    
}
