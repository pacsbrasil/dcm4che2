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

package tiani.dcm4che.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.zip.InflaterInputStream;

import javax.imageio.stream.ImageInputStream;

import java.util.logging.*;

/** Implementation of <code>Dataset</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
final class DatasetImpl extends BaseDatasetImpl
        implements org.dcm4che.data.Dataset {

    private final Dataset parent;
    private Charset charset = null;
    private String privateCreatorID = null;
    private long itemOffset = -1L;
    DatasetImpl() {
        this(null);
    }
    
    DatasetImpl(Dataset parent) {
        this.parent = parent;
    }
    
    public String toString() {
        return "Dataset[" + size() + " elements]";
    }
    
    public Charset getCharset() {
        return charset != null ? charset
                : parent != null ? parent.getCharset() : null;
    }

    public final Dataset getParent() {
        return parent;
    }

    public final Dataset setItemOffset(long itemOffset) {
        this.itemOffset = itemOffset;
        return this;
    }

    public final long getItemOffset() {
        if (itemOffset != -1L || list.isEmpty())
            return itemOffset;
        
        long elm1pos = ((DcmElement)list.get(0)).getStreamPosition();
        return elm1pos == -1L ? -1L : elm1pos - 8L;
    }

    public DcmElement setSQ(int tag) {
        return set(new SQElement(tag, this));
    }

    protected DcmElement set(DcmElement newElem) {
        if ((newElem.tag() >>> 16) < 4)
            throw new IllegalArgumentException(newElem.toString());

        if (newElem.tag() == Tags.SpecificCharacterSet) {
            try {
                this.charset = Charsets.lookup(newElem.getStrings(null));
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to consider specified Charset!", ex);
                this.charset = null;
            }
        }

        return super.set(newElem);
    }
    
    public DcmElement remove(int tag) {
        if (tag == Tags.SpecificCharacterSet)
            charset = null;
        return super.remove(tag);
    }
    
    public void clear() {
        super.clear();
        charset = null;
        totLen = 0;
    }
        
    public void read(InputStream in, FileFormat format, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDcmFile(format, stopTag);
    }
    
    public void read(ImageInputStream in, FileFormat format, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDcmFile(format, stopTag);
    }
}