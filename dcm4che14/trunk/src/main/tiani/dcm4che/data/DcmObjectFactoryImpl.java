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

import tiani.dcm4che.util.Impl;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class DcmObjectFactoryImpl extends DcmObjectFactory {

    /** Creates a new instance of DcmParserFactoryImpl */
    public DcmObjectFactoryImpl() {
    }

    public Dataset newDataset() {
        return new DatasetImpl();
    }    

    public FileMetaInfo newFileMetaInfo(String sopClassUID,
            String sopInstanceUID, String transferSyntaxUID,
            String implClassUID, String implVersName) {
        return new FileMetaInfoImpl().init(sopClassUID, sopInstanceUID,
                transferSyntaxUID, implClassUID, implVersName);
    }

    public FileMetaInfo newFileMetaInfo(String sopClassUID,
            String sopInstanceUID, String transferSyntaxUID) {
        return new FileMetaInfoImpl().init(sopClassUID, sopInstanceUID,
                transferSyntaxUID, Impl.CLASS_UID, Impl.VERSION_NAME);
    }

    public PersonName newPersonName(String s) {
        return new PersonNameImpl(s);
    }
    
    public FileMetaInfo newFileMetaInfo(Dataset ds, String transferSyntaxUID) {
        try {
            return new FileMetaInfoImpl().init(
                    ds.getString(Tags.SOPClassUID, null),
                    ds.getString(Tags.SOPInstanceUID, null),
                    transferSyntaxUID, Impl.CLASS_UID, Impl.VERSION_NAME);
        } catch (DcmValueException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
   }
}
