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

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStream;
import org.xml.sax.ContentHandler;

/** Defines behavior of <code>DataSet</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
final class FileMetaInfoImpl extends DcmObjectImpl
        implements org.dcm4che.data.FileMetaInfo {
            
    static final byte[] DICM_PREFIX = {
        (byte)'D',
        (byte)'I',
        (byte)'C',
        (byte)'M'
    };
    static final byte[] VERSION = { 0, 1 };

    private final byte[] preamble = new byte[128];
    
    public String toString() {
        return "FileMetaInfo[tsUID= " + getTransferSyntaxUID() + "]";
    }

    public final byte[] getPreamble() {
        return preamble;
    }
    
    FileMetaInfoImpl init(String sopClassUID, String sopInstUID, String tsUID,
        String implClassUID, String  implVersName) {
        setOB(Tags.FileMetaInformationVersion, (byte[])VERSION.clone());
        setUI(Tags.MediaStorageSOPClassUID, sopClassUID);
        setUI(Tags.MediaStorageSOPInstanceUID, sopInstUID);
        setUI(Tags.TransferSyntaxUID, tsUID);
        setUI(Tags.ImplementationClassUID, implClassUID);
        if (implVersName != null) {
            setSH(Tags.ImplementationVersionName, implVersName);
        }
        return this;
    }
    
    public String getMediaStorageSOPClassUID() {
        try {
            return getString(Tags.MediaStorageSOPClassUID);
        } catch (DcmValueException ex) {
            return null;
        }
    }
    
    public String getMediaStorageSOPInstanceUID() {
        try {
            return getString(Tags.MediaStorageSOPInstanceUID);
        } catch (DcmValueException ex) {
            return null;
        }
    }
    
    public String getTransferSyntaxUID() {
        try {
            return getString(Tags.TransferSyntaxUID);
        } catch (DcmValueException ex) {
            return null;
        }
    }
    
    protected DcmElement set(DcmElement newElem) {
        if ((newElem.tag() & 0xFFFF0000) != 0x00020000)
            throw new IllegalArgumentException(newElem.toString());
        
        return super.set(newElem);
    }

    public int length() {
        return grLen() + 12;
    }
    
    private int grLen() {
        int len = 0;
        for (int i = 0, n = list.size(); i < n; ++i) {
            DcmElement e = (DcmElement)list.get(i);
            len += e.length() + (VRs.isLengthField16Bit(e.vr()) ? 8 : 12);
        }
        return len;
    }
    
    public void write(DcmHandler handler) throws IOException {
        handler.startFileMetaInfo(preamble);
        handler.setDcmDecodeParam(DcmDecodeParam.EVR_LE);
        write(0x00020000, grLen(), handler);
        handler.endFileMetaInfo();
    }
    
    public void write(OutputStream out) throws IOException {
        write(new DcmStreamHandlerImpl(out));
    }
    
    public void write(ImageOutputStream out) throws IOException {
        write(new DcmStreamHandlerImpl(out));
    }

    public void write(ContentHandler ch, TagDictionary dict)
            throws IOException {                
         write(new DcmHandlerAdapter(ch, dict));
    }
}

