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

/*$Id$*/

package org.dcm4che.data;

import org.dcm4che.dict.TagDictionary;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.imageio.stream.ImageOutputStream;
import org.xml.sax.ContentHandler;

/** Defines behavior of <code>DataSet</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 10: Media Storage and File Format for Media Interchange,
 * 7.1 File Meta Information"
 */
public interface FileMetaInfo extends DcmObject{
    
    public byte[] getPreamble();
        
    public String getMediaStorageSOPClassUID();
    
    public String getMediaStorageSOPInstanceUID();
    
    public String getTransferSyntaxUID();

    public String getImplementationClassUID();
    
    public String getImplementationVersionName();
    
    public void write(DcmHandler handler) throws IOException;

    public void write(OutputStream out) throws IOException;

    public void write(ImageOutputStream out) throws IOException;
    
    public void write(ContentHandler ch, TagDictionary dict) throws IOException;
    
    public void read(InputStream in) throws IOException;
}