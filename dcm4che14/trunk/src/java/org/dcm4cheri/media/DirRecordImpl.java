/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4cheri.media;

import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirRecord;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March, 2002
 * @version    $Revision$ $Date$
 */
final class DirRecordImpl implements DirRecord
{

    private final static DcmObjectFactory factory =
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

    final long inUsePos;
    final long nextValPos;
    final long lowerValPos;


    /**
     * Creates a new instance of DirRecordImpl
     *
     * @param  parser           Description of the Parameter
     * @param  pos              Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public DirRecordImpl(DcmParser parser, int pos)
        throws IOException
    {
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

        DcmElement inUseElem = dataset.get(Tags.RecordInUseFlag);
        if (inUseElem == null || inUseElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Record In-use Flag");
        }
        this.inUse = inUseElem.getInt();
        this.inUsePos = inUseElem.getStreamPosition() + 8;

        DcmElement lowerElem = dataset.get(Tags.OffsetOfLowerLevelDirectoryEntity);
        if (lowerElem == null || lowerElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Offset of Referenced Lower-Level Directory Entity");
        }
        this.lower = lowerElem.getInt();
        this.lowerValPos = lowerElem.getStreamPosition() + 8;

        this.refFileIDs = dataset.getStrings(Tags.RefFileID);
        this.refSOPClassUID =
                dataset.getString(Tags.RefSOPClassUIDInFile, null);
        this.refSOPInstanceUID =
                dataset.getString(Tags.RefSOPInstanceUIDInFile, null);
        this.refTransferSyntaxUID =
                dataset.getString(Tags.RefSOPTransferSyntaxUIDInFile, null);
    }


    /**
     *  Gets the dataset attribute of the DirRecordImpl object
     *
     * @return    The dataset value
     */
    public Dataset getDataset()
    {
        return dataset;
    }


    /**
     *  Gets the type attribute of the DirRecordImpl object
     *
     * @return    The type value
     */
    public String getType()
    {
        return type;
    }


    /**
     *  Gets the inUseFlag attribute of the DirRecordImpl object
     *
     * @return    The inUseFlag value
     */
    public int getInUseFlag()
    {
        return inUse;
    }


    /**
     *  Gets the refFileIDs attribute of the DirRecordImpl object
     *
     * @return    The refFileIDs value
     */
    public String[] getRefFileIDs()
    {
        return refFileIDs;
    }


    /**
     *  Gets the refSOPClassUID attribute of the DirRecordImpl object
     *
     * @return    The refSOPClassUID value
     */
    public String getRefSOPClassUID()
    {
        return refSOPClassUID;
    }


    /**
     *  Gets the refSOPInstanceUID attribute of the DirRecordImpl object
     *
     * @return    The refSOPInstanceUID value
     */
    public String getRefSOPInstanceUID()
    {
        return refSOPInstanceUID;
    }


    /**
     *  Gets the refSOPTransferSyntaxUID attribute of the DirRecordImpl object
     *
     * @return    The refSOPTransferSyntaxUID value
     */
    public String getRefSOPTransferSyntaxUID()
    {
        return refTransferSyntaxUID;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        return "DirRecord[inUse:" + inUse + ",type:" + type
                 + ",next: " + next + ",lower: " + lower + "]";
    }


    /**
     *  Gets the nextSibling attribute of the DirRecordImpl object
     *
     * @return                  The nextSibling value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSibling()
        throws IOException
    {
        return getNextSibling(true);
    }


    /**
     *  Gets the nextSibling attribute of the DirRecordImpl object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The nextSibling value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSibling(boolean onlyInUse)
        throws IOException
    {
        if (next == 0) {
            return null;
        }
        DirRecord retval = new DirRecordImpl(parser, next);
        if (onlyInUse && retval.getInUseFlag() == DirRecord.INACTIVE) {
            return retval.getNextSibling(onlyInUse);
        }
        return retval;
    }


    /**
     *  Gets the firstChild attribute of the DirRecordImpl object
     *
     * @return                  The firstChild value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChild()
        throws IOException
    {
        return getFirstChild(true);
    }


    /**
     *  Gets the firstChild attribute of the DirRecordImpl object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The firstChild value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChild(boolean onlyInUse)
        throws IOException
    {
        if (lower == 0) {
            return null;
        }
        DirRecord retval = new DirRecordImpl(parser, lower);
        if (onlyInUse && retval.getInUseFlag() == DirRecord.INACTIVE) {
            return retval.getNextSibling(onlyInUse);
        }
        return retval;
    }


    /**
     *  Description of the Method
     *
     * @param  type          Description of the Parameter
     * @param  keys          Description of the Parameter
     * @param  ignorePNCase  Description of the Parameter
     * @return               Description of the Return Value
     */
    public boolean match(String type, Dataset keys, boolean ignorePNCase)
    {
        return (type == null || type.equals(this.type))
                 && dataset.match(keys, ignorePNCase);
    }


    /**
     *  Gets the firstChildBy attribute of the DirRecordImpl object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The firstChildBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChildBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException
    {
        DirRecord dr = getFirstChild(true);
        return (dr == null || dr.match(type, keys, ignorePNCase))
                 ? dr
                 : dr.getNextSiblingBy(type, keys, ignorePNCase);
    }


    /**
     *  Gets the nextSiblingBy attribute of the DirRecordImpl object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The nextSiblingBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSiblingBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException
    {
        DirRecord dr = this;
        do {
            dr = dr.getNextSibling(true);
        } while (dr != null && !dr.match(type, keys, ignorePNCase));
        return dr;
    }

}

