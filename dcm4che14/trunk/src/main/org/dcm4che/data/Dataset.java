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

import org.xml.sax.ContentHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/** Defines behavior of <code>Dataset</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
public interface Dataset extends DcmObject, java.io.Serializable {
    
    public Dataset setFileMetaInfo(FileMetaInfo fmi);

    public FileMetaInfo getFileMetaInfo();

    public Dataset getParent();

    public Dataset setItemOffset(long itemOffset);

    public long getItemOffset();

    public int calcLength(DcmEncodeParam param);
    
    public void writeDataset(DcmHandler handler, DcmEncodeParam param)
            throws IOException;
    
    public void writeDataset(OutputStream out, DcmEncodeParam param)
            throws IOException;

    public void writeDataset(ImageOutputStream out, DcmEncodeParam param)
            throws IOException;

    public void readDataset(InputStream in, DcmDecodeParam param, int stopTag)
            throws IOException;

    public void readFile(InputStream in, FileFormat format, int stopTag)
            throws IOException;

    public void readFile(ImageInputStream iin, FileFormat format, int stopTag)
            throws IOException;

    public void writeFile(OutputStream out, DcmEncodeParam param)
            throws IOException;

    public void writeFile(ImageOutputStream iout, DcmEncodeParam param)
            throws IOException;

    public void writeFile(ContentHandler handler, TagDictionary dict)
            throws IOException;

    public void writeDataset(ContentHandler handler, TagDictionary dict)
            throws IOException;

    public Dataset subset(int fromTag, int toTag);
    
    public Dataset subset(Dataset filter);    
}
