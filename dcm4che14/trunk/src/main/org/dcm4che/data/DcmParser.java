/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package org.dcm4che.data;

import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.VRMap;

import org.xml.sax.ContentHandler;

import java.io.IOException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface DcmParser {

    public long getStreamPosition();
    
    public void seek(long pos) throws IOException;

    public void setDcmHandler(DcmHandler handler);

    public void setSAXHandler(ContentHandler hc, TagDictionary dict);
    
    public void setVRMap(VRMap vrMap);
    
    public void setDcmDecodeParam(DcmDecodeParam decodeParam);
    
    public DcmDecodeParam getDcmDecodeParam();

    public FileFormat detectFileFormat() throws IOException;

    public int parseHeader() throws IOException;

    public long parseFileMetaInfo(boolean preamble, DcmDecodeParam param)
            throws IOException;

    public long parseFileMetaInfo() throws IOException;
    
    public long parseCommand(boolean preamble, DcmDecodeParam param)
            throws IOException;
    
    public long parseDataset(DcmDecodeParam param, int stopTag)
            throws IOException;

    public long parseDcmFile(FileFormat format, int stopTag)
            throws IOException;
    
    public long parseItemDataset() throws IOException;

    public int getReadTag();
        
    public int getReadVR();
        
    public int getReadLength();

    public boolean hasSeenEOF();
}

