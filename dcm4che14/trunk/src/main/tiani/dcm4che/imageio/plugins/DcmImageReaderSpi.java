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

package tiani.dcm4che.imageio.plugins;

import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "TIANI MEDGRAPH AG";

    private static final String version = "1.0";

    private static final String[] names = { "DICOM" };

    private static final String[] suffixes = { "dcm" };

    private static final String[] MIMETypes = { "Application/dicom" };

    private static final String readerClassName =
        "tiani.dcm4che.imageio.plugins.DcmImageReader";

    private static final String[] writerSpiNames = null;

    public DcmImageReaderSpi() {
        super(vendorName,
              version,
              names,
              suffixes,
              MIMETypes,
              readerClassName,
              STANDARD_INPUT_TYPE,
              writerSpiNames,
              false,
              org.dcm4che.imageio.plugins.DcmMetadata.nativeMetadataFormatName,
              "org.dcm4che.imageio.plugins.DcmMetadataFormat",
              null, null,
              false,
              null, null,
              null, null
              );
    }

    public String getDescription(Locale locale) {
        return "DICOM image reader";
    }

    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        
        DcmParser parser = DcmParserFactory.getInstance().newDcmParser();
        parser.setInput((ImageInputStream)input);

        return parser.detectFileFormat() != null;
    }

    public ImageReader createReaderInstance(Object extension) {
        return new DcmImageReader(this);
    }

}
