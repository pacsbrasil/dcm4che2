/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General License as published           *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General License for more details.                                 *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General                *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4che.data;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.dcm4che.dict.TagDictionary;

import org.xml.sax.ContentHandler;

/**
 *  Defines behavior of <code>Dataset</code> container objects.
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since  March, 2002
 * @see  "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 * @version  $Revision$ $Date$ <p>
 *
 *
 */
public interface Dataset extends DcmObject, Serializable
{

    Dataset setFileMetaInfo(FileMetaInfo fmi);


    FileMetaInfo getFileMetaInfo();


    Dataset getParent();


    Dataset setItemOffset(long itemOffset);


    long getItemOffset();


    int calcLength(DcmEncodeParam param);


    void writeDataset(DcmHandler handler, DcmEncodeParam param)
        throws IOException;


    void writeDataset(OutputStream out, DcmEncodeParam param)
        throws IOException;


    void writeDataset(ImageOutputStream out, DcmEncodeParam param)
        throws IOException;


    void readDataset(InputStream in, DcmDecodeParam param, int stopTag)
        throws IOException;


    void readFile(InputStream in, FileFormat format, int stopTag)
        throws IOException;


    void readFile(ImageInputStream iin, FileFormat format, int stopTag)
        throws IOException;


    void writeFile(OutputStream out, DcmEncodeParam param)
        throws IOException;


    void writeFile(ImageOutputStream iout, DcmEncodeParam param)
        throws IOException;


    void writeFile(ContentHandler handler, TagDictionary dict)
        throws IOException;


    void writeDataset(ContentHandler handler, TagDictionary dict)
        throws IOException;


    Dataset subSet(int fromTag, int toTag);


    Dataset subSet(Dataset filter);


    /**
     *  Description of the Method
     *
     * @param  out Description of the Parameter
     * @param  map Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void dumpDataset(OutputStream out, Map map)
        throws IOException;


    /**
     *  Description of the Method
     *
     * @param  w Description of the Parameter
     * @param  map Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public void dumpDataset(Writer w, Map map)
        throws IOException;
}

