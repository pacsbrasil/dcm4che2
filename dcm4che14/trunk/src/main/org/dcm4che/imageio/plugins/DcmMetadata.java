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

package org.dcm4che.imageio.plugins;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.TagDictionary;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class DcmMetadata extends javax.imageio.metadata.IIOMetadata {

    public static final String nativeMetadataFormatName = 
            "dcm4che_imageio_dicom_1.0";

    protected DcmMetadata(boolean standardMetadataFormatSupported,
            String[] extraMetadataFormatNames,
            String[] extraMetadataFormatClassNames) {
        super(standardMetadataFormatSupported, 
              nativeMetadataFormatName,
              "org.dcm4che.imageio.plugins.DcmMetadataFormat",
              extraMetadataFormatNames,
              extraMetadataFormatClassNames);
    }

    public abstract Dataset getDataset();

    public abstract void setDictionary(TagDictionary dict);
}
