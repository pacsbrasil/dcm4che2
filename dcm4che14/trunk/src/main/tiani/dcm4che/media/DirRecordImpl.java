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
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirRecord;

import java.io.IOException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DirRecordImpl implements DirRecord {
    
    private static final DcmObjectFactory factory = 
            DcmObjectFactory.getInstance();
    
    final DcmParser parser;
    final Dataset dataset;
    final String type;
    final int next;
    final int lower;

    final int inUse;
    final String[] refFileIDs;
    final String refSOPClassUID;
    final String refSOPInstanceUID;
    final String refTransferSyntaxUID;    
    
    final long nextValPos;
    final long lowerValPos;

    /** Creates a new instance of DirRecordImpl */
    public DirRecordImpl(DcmParser parser, int pos) throws IOException {
        this.parser = parser;
        this.dataset = factory.newDataset();
        parser.seek(pos & 0xFFFFFFFFL);
        parser.setDcmHandler(dataset.getDcmHandler());
        try {
            parser.parseItemDataset();
        } finally {
            parser.setDcmHandler(null);
        }
        this.type = dataset.getString(Tags.DirectoryRecordType, null);
        if (type == null) {
            throw new DcmValueException("Missing Directory Record Type");
        }

        DcmElement nextElem = dataset.get(Tags.OffsetOfNextDirectoryRecord);
        if (nextElem == null || nextElem.isEmpty()) {
            throw new DcmValueException(
                "Missing Offset of Referenced Next Directory Record");
        }
        this.next = nextElem.getInt();
        this.nextValPos = nextElem.getStreamPosition() + 8;
        DcmElement lowerElem = dataset.get(Tags.OffsetOfLowerLevelDirectoryEntity);
        if (lowerElem == null || lowerElem.isEmpty()) {
            throw new DcmValueException(
                "Missing Offset of Referenced Lower-Level Directory Entity");
        }
        this.lower = lowerElem.getInt();
        this.lowerValPos = lowerElem.getStreamPosition() + 8;
        
        this.inUse = dataset.getInt(Tags.RecordInUseFlag, -1);
        if (inUse == -1) {
            throw new DcmValueException("Missing Record In-use Flag");
        }

        this.refFileIDs = dataset.getStrings(Tags.RefFileID);
        this.refSOPClassUID =
                dataset.getString(Tags.RefSOPClassUIDInFile, null);
        this.refSOPInstanceUID =
                dataset.getString(Tags.RefSOPInstanceUIDInFile, null);
        this.refTransferSyntaxUID =
                dataset.getString(Tags.RefSOPTransferSyntaxUIDInFile, null);
    }
    
    public Dataset getDataset() {
        return dataset;
    }

    public String getType() {
        return type;
    }
        
    public int getInUseFlag() {
        return inUse;
    }             
    
    public String[] getRefFileIDs() {
        return refFileIDs;
    }

    public String getRefSOPClassUID() {
        return refSOPClassUID;
    }

    public String getRefSOPInstanceUID() {
        return refSOPInstanceUID;
    }

    public String getRefSOPTransferSyntaxUID() {
        return refTransferSyntaxUID;
    }
    
    public DirRecord getNextSibling() throws IOException {
        return next != 0 ? new DirRecordImpl(parser, next) : null;
    }
    
    public DirRecord getFirstChild() throws IOException {
        return lower != 0 ? new DirRecordImpl(parser, lower) : null;
    }
    
}
